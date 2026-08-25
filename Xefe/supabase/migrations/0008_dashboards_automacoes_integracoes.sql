-- =====================================================================
-- 0008 — Dashboards, automações, notificações, integrações e webhooks
-- =====================================================================

create table public.dashboards (
  id         uuid primary key default gen_random_uuid(),
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  name       text not null,
  description text,
  owner_id   uuid references auth.users(id),
  is_shared  boolean not null default false,
  layout     jsonb not null default '[]'::jsonb,   -- grid: {i,x,y,w,h}
  filters    jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index on public.dashboards (tenant_id, owner_id);

create table public.dashboard_widgets (
  id           uuid primary key default gen_random_uuid(),
  tenant_id    uuid not null references public.tenants(id) on delete cascade,
  dashboard_id uuid not null references public.dashboards(id) on delete cascade,
  widget_type  text not null,     -- kpi | pie | bar | line | area | table | progress | goal
  title        text not null,
  source       text not null,     -- tasks | projects | time_entries | goals
  config       jsonb not null default '{}'::jsonb,  -- métrica, agrupamento, filtros
  position     jsonb not null default '{}'::jsonb,
  created_at   timestamptz not null default now()
);
create index on public.dashboard_widgets (tenant_id, dashboard_id);

-- ---------------------------------------------------------------------
-- Automações: QUANDO -> CONDIÇÃO -> AÇÃO
-- ---------------------------------------------------------------------
create table public.automations (
  id           uuid primary key default gen_random_uuid(),
  tenant_id    uuid not null references public.tenants(id) on delete cascade,
  scope        text not null default 'list' check (scope in ('tenant','space','folder','list','project')),
  scope_id     uuid,
  name         text not null,
  description  text,
  is_active    boolean not null default true,
  trigger_type text not null,   -- task.created | task.status_changed | task.overdue | schedule.cron | ...
  trigger_config jsonb not null default '{}'::jsonb,
  conditions   jsonb not null default '[]'::jsonb,  -- [{field, op, value}] com AND/OR aninhado
  actions      jsonb not null default '[]'::jsonb,  -- [{type, params}]
  run_count    bigint not null default 0,
  last_run_at  timestamptz,
  created_by   uuid references auth.users(id),
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now()
);
create index on public.automations (tenant_id, trigger_type) where is_active;

create table public.automation_runs (
  id            bigserial primary key,
  tenant_id     uuid not null references public.tenants(id) on delete cascade,
  automation_id uuid not null references public.automations(id) on delete cascade,
  entity        public.entity_type,
  entity_id     uuid,
  status        text not null default 'pending' check (status in ('pending','running','success','failed','skipped')),
  input         jsonb,
  output        jsonb,
  error         text,
  attempts      int not null default 0,
  started_at    timestamptz,
  finished_at   timestamptz,
  created_at    timestamptz not null default now()
);
create index on public.automation_runs (tenant_id, automation_id, created_at desc);
create index on public.automation_runs (status) where status in ('pending','running');

-- ---------------------------------------------------------------------
-- Notificações
-- ---------------------------------------------------------------------
create table public.notifications (
  id         uuid primary key default gen_random_uuid(),
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  user_id    uuid not null references auth.users(id) on delete cascade,
  type       text not null,     -- task.assigned | comment.mention | task.overdue | ...
  title      text not null,
  body       text,
  entity     public.entity_type,
  entity_id  uuid,
  link       text,
  actor_id   uuid references auth.users(id),
  read_at    timestamptz,
  created_at timestamptz not null default now()
);
create index on public.notifications (user_id, read_at, created_at desc);
create index on public.notifications (tenant_id, created_at desc);

create table public.notification_preferences (
  user_id    uuid not null references auth.users(id) on delete cascade,
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  type       text not null,
  in_app     boolean not null default true,
  email      boolean not null default true,
  push       boolean not null default false,
  whatsapp   boolean not null default false,
  slack      boolean not null default false,
  teams      boolean not null default false,
  primary key (user_id, tenant_id, type)
);

create table public.notification_channels (
  id         uuid primary key default gen_random_uuid(),
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  channel    text not null check (channel in ('email','whatsapp','slack','teams')),
  config     jsonb not null default '{}'::jsonb,  -- segredos ficam no Vault, aqui só referências
  is_active  boolean not null default true,
  created_at timestamptz not null default now(),
  unique (tenant_id, channel)
);

-- ---------------------------------------------------------------------
-- Integrações, API keys e webhooks
-- ---------------------------------------------------------------------
create table public.integrations (
  id         uuid primary key default gen_random_uuid(),
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  provider   text not null,      -- erp | crm | google_calendar | outlook | powerbi | slack | teams
  name       text not null,
  config     jsonb not null default '{}'::jsonb,
  secret_ref text,               -- nome do segredo no Vault; nunca o segredo em si
  is_active  boolean not null default true,
  created_by uuid references auth.users(id),
  created_at timestamptz not null default now(),
  unique (tenant_id, provider, name)
);

create table public.api_keys (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  name        text not null,
  key_prefix  text not null,                  -- 8 primeiros caracteres, para exibição
  key_hash    text not null,                  -- SHA-256 da chave; a chave só aparece na criação
  scopes      text[] not null default '{}',
  rate_limit_per_min int not null default 120,
  last_used_at timestamptz,
  expires_at  timestamptz,
  revoked_at  timestamptz,
  created_by  uuid references auth.users(id),
  created_at  timestamptz not null default now()
);
create index on public.api_keys (tenant_id) where revoked_at is null;

create table public.webhooks (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  url         text not null,
  events      text[] not null,                -- task.created, task.updated, ...
  secret      text not null default encode(extensions.gen_random_bytes(24), 'hex'),
  is_active   boolean not null default true,
  created_by  uuid references auth.users(id),
  created_at  timestamptz not null default now()
);

create table public.webhook_deliveries (
  id          bigserial primary key,
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  webhook_id  uuid not null references public.webhooks(id) on delete cascade,
  event       text not null,
  payload     jsonb not null,
  status      text not null default 'pending' check (status in ('pending','delivered','failed')),
  attempts    int not null default 0,
  response_code int,
  response_body text,
  next_retry_at timestamptz,
  created_at  timestamptz not null default now()
);
create index on public.webhook_deliveries (status, next_retry_at) where status = 'pending';
