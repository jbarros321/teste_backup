-- =====================================================================
-- 0009 — Inteligência Artificial (Google Gemini)
--
-- REGRA R27: tudo que é CONFIGURAÇÃO de IA só pode ser lido/escrito por
-- platform_admin. Tenants apenas CONSOMEM (conversas, mensagens, uso).
-- A chave da API nunca fica no banco nem chega ao cliente: vive em
-- Supabase Secrets/Vault e só é lida dentro das Edge Functions.
-- =====================================================================

-- Configuração global da IA — linha única (singleton).
create table public.ai_settings (
  id                 boolean primary key default true check (id),
  provider           text    not null default 'gemini',
  model_default      text    not null default 'gemini-2.5-flash',
  model_advanced     text    not null default 'gemini-2.5-pro',
  temperature        numeric(3,2) not null default 0.30 check (temperature between 0 and 2),
  max_output_tokens  int     not null default 4096,
  top_p              numeric(3,2) not null default 0.95,
  api_key_ref        text    not null default 'GEMINI_API_KEY',  -- nome do segredo, não o segredo
  is_enabled         boolean not null default true,
  monthly_token_cap  bigint  not null default 5000000,           -- teto global da plataforma
  safety_settings    jsonb   not null default '{}'::jsonb,
  updated_by         uuid references auth.users(id),
  updated_at         timestamptz not null default now()
);
insert into public.ai_settings (id) values (true) on conflict do nothing;

comment on table public.ai_settings is
  'Configuração global de IA. Somente platform_admin lê e escreve (ver policies em 0011).';

-- Habilitação e cota por empresa — também só o platform_admin altera.
create table public.ai_tenant_settings (
  tenant_id          uuid primary key references public.tenants(id) on delete cascade,
  is_enabled         boolean not null default false,
  model_override     text,
  monthly_token_cap  bigint  not null default 200000,
  allow_agent_write  boolean not null default false,  -- a IA pode criar/alterar tarefas?
  updated_by         uuid references auth.users(id),
  updated_at         timestamptz not null default now()
);

-- Prompts de sistema versionados (curadoria do platform_admin).
create table public.ai_prompts (
  id          uuid primary key default gen_random_uuid(),
  key         text not null,                 -- chat_default | project_generator | summarizer | risk_analyst
  version     int  not null default 1,
  content     text not null,
  is_active   boolean not null default true,
  updated_by  uuid references auth.users(id),
  created_at  timestamptz not null default now(),
  unique (key, version)
);

-- ---------------------------------------------------------------------
-- Consumo pelos usuários
-- ---------------------------------------------------------------------
create table public.ai_conversations (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  user_id     uuid not null references auth.users(id) on delete cascade,
  title       text not null default 'Nova conversa',
  context     jsonb not null default '{}'::jsonb,  -- {space_id, project_id, task_id}
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now()
);
create index on public.ai_conversations (tenant_id, user_id, updated_at desc);

create table public.ai_messages (
  id              uuid primary key default gen_random_uuid(),
  tenant_id       uuid not null references public.tenants(id) on delete cascade,
  conversation_id uuid not null references public.ai_conversations(id) on delete cascade,
  role            text not null check (role in ('user','model','system','tool')),
  content         text not null,
  tool_calls      jsonb,
  model           text,
  input_tokens    int not null default 0,
  output_tokens   int not null default 0,
  latency_ms      int,
  created_at      timestamptz not null default now()
);
create index on public.ai_messages (tenant_id, conversation_id, created_at);

-- Telemetria e cota
create table public.ai_usage (
  id            bigserial primary key,
  tenant_id     uuid not null references public.tenants(id) on delete cascade,
  user_id       uuid references auth.users(id) on delete set null,
  feature       text not null,      -- chat | generate_project | summarize | risk_scan | automation
  model         text not null,
  input_tokens  int  not null default 0,
  output_tokens int  not null default 0,
  cost_usd      numeric(12,6) not null default 0,
  success       boolean not null default true,
  error         text,
  created_at    timestamptz not null default now()
);
create index on public.ai_usage (tenant_id, created_at desc);

-- Ações propostas pela IA que exigem confirmação humana antes de gravar.
create table public.ai_actions (
  id              uuid primary key default gen_random_uuid(),
  tenant_id       uuid not null references public.tenants(id) on delete cascade,
  conversation_id uuid references public.ai_conversations(id) on delete cascade,
  requested_by    uuid not null references auth.users(id) on delete cascade,
  action_type     text not null,    -- create_project | create_tasks | update_task | assign | comment
  payload         jsonb not null,
  status          text not null default 'proposed'
                  check (status in ('proposed','approved','applied','rejected','failed')),
  applied_entity  public.entity_type,
  applied_entity_id uuid,
  reviewed_by     uuid references auth.users(id),
  reviewed_at     timestamptz,
  error           text,
  created_at      timestamptz not null default now()
);
create index on public.ai_actions (tenant_id, status, created_at desc);

-- ---------------------------------------------------------------------
-- Cota: consultada pela Edge Function antes de chamar o Gemini
-- ---------------------------------------------------------------------
create or replace function public.ai_quota_left(p_tenant uuid)
returns bigint
language sql
stable
security definer
set search_path = ''
as $$
  select greatest(0,
    coalesce((select ats.monthly_token_cap from public.ai_tenant_settings ats where ats.tenant_id = p_tenant), 0)
    - coalesce((
        select sum(u.input_tokens + u.output_tokens)
        from public.ai_usage u
        where u.tenant_id = p_tenant
          and u.created_at >= date_trunc('month', now())
      ), 0)
  );
$$;

create or replace function public.ai_is_available(p_tenant uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select coalesce((select s.is_enabled from public.ai_settings s where s.id), false)
     and coalesce((select ats.is_enabled from public.ai_tenant_settings ats where ats.tenant_id = p_tenant), false)
     and coalesce((select p.allow_ai from public.tenants t join public.plans p on p.id = t.plan_id
                   where t.id = p_tenant), false)
     and public.ai_quota_left(p_tenant) > 0;
$$;

grant execute on function public.ai_quota_left(uuid), public.ai_is_available(uuid) to authenticated;
