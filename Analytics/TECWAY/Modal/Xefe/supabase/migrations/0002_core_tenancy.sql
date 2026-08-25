-- =====================================================================
-- 0002 — Núcleo multi-tenant: plataforma, empresas, domínios, usuários,
--        papéis, permissões, telas e compartilhamentos.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Super administradores da plataforma (fora do modelo de tenant).
-- Quem está aqui vê e controla TUDO, inclusive a configuração de IA.
-- ---------------------------------------------------------------------
create table public.platform_admins (
  user_id     uuid primary key references auth.users(id) on delete cascade,
  note        text,
  created_at  timestamptz not null default now()
);
comment on table public.platform_admins is
  'Super admins globais. Única via de acesso às configurações de IA, planos e limites.';

-- ---------------------------------------------------------------------
-- Planos
-- ---------------------------------------------------------------------
create table public.plans (
  id                 uuid primary key default gen_random_uuid(),
  code               text not null unique,               -- FREE | PRO | BUSINESS | ENTERPRISE
  name               text not null,
  price_month        numeric(12,2) not null default 0,
  max_users          int,                                -- null = ilimitado
  max_projects       int,
  max_storage_mb     int,
  max_automations    int,
  max_ai_tokens_month bigint,
  allow_integrations boolean not null default false,
  allow_ai           boolean not null default false,
  features           jsonb not null default '{}'::jsonb,
  is_active          boolean not null default true,
  created_at         timestamptz not null default now()
);

-- ---------------------------------------------------------------------
-- Empresas (tenants)
-- ---------------------------------------------------------------------
create table public.tenants (
  id                uuid primary key default gen_random_uuid(),
  name              text not null,
  slug              text not null unique,
  legal_name        text,
  tax_id            text,                                -- CNPJ
  segment           text,
  logo_url          text,
  status            public.tenant_status not null default 'trial',
  plan_id           uuid references public.plans(id),
  timezone          text not null default 'America/Sao_Paulo',
  locale            text not null default 'pt-BR',
  currency          text not null default 'BRL',
  week_start        int  not null default 1 check (week_start between 0 and 6),
  settings          jsonb not null default '{}'::jsonb,
  trial_ends_at     timestamptz,
  created_by        uuid references auth.users(id),
  created_at        timestamptz not null default now(),
  updated_at        timestamptz not null default now(),
  deleted_at        timestamptz
);
create index on public.tenants (status);
create index on public.tenants (plan_id);

create table public.subscriptions (
  id            uuid primary key default gen_random_uuid(),
  tenant_id     uuid not null references public.tenants(id) on delete cascade,
  plan_id       uuid not null references public.plans(id),
  status        text not null default 'active',           -- active | past_due | canceled
  started_at    timestamptz not null default now(),
  current_period_end timestamptz,
  canceled_at   timestamptz,
  external_ref  text,
  created_at    timestamptz not null default now()
);
create index on public.subscriptions (tenant_id);

create table public.usage_counters (
  tenant_id     uuid not null references public.tenants(id) on delete cascade,
  metric        text not null,                            -- users | projects | storage_mb | ai_tokens
  period        date not null,                            -- primeiro dia do mês
  value         bigint not null default 0,
  updated_at    timestamptz not null default now(),
  primary key (tenant_id, metric, period)
);

-- ---------------------------------------------------------------------
-- Domínios de e-mail -> vínculo automático de usuários ao tenant.
-- O "responsável do domínio" é o membership owner apontado em owner_user_id.
-- ---------------------------------------------------------------------
create table public.tenant_domains (
  id            uuid primary key default gen_random_uuid(),
  tenant_id     uuid not null references public.tenants(id) on delete cascade,
  domain        text not null unique,                     -- 'tecway.com.br' (sem @, minúsculo)
  owner_user_id uuid references auth.users(id),           -- responsável do domínio
  default_role  public.member_role not null default 'member',
  auto_join     boolean not null default true,
  verified_at   timestamptz,
  verify_token  text default encode(extensions.gen_random_bytes(16), 'hex'),
  created_at    timestamptz not null default now(),
  constraint domain_lowercase check (domain = lower(domain)),
  constraint domain_no_at     check (position('@' in domain) = 0)
);
create index on public.tenant_domains (tenant_id);
comment on column public.tenant_domains.default_role is
  'Papel atribuído automaticamente a quem entra com e-mail deste domínio.';

-- ---------------------------------------------------------------------
-- Perfis (espelho de auth.users)
-- ---------------------------------------------------------------------
create table public.profiles (
  id               uuid primary key references auth.users(id) on delete cascade,
  email            text not null,
  full_name        text,
  avatar_url       text,
  phone            text,
  job_title        text,
  timezone         text not null default 'America/Sao_Paulo',
  locale           text not null default 'pt-BR',
  weekly_capacity_hours numeric(5,2) not null default 40,
  last_seen_at     timestamptz,
  created_at       timestamptz not null default now(),
  updated_at       timestamptz not null default now()
);
create index on public.profiles (lower(email));

-- ---------------------------------------------------------------------
-- Papéis customizados por tenant (opcional; os padrão vêm do enum)
-- ---------------------------------------------------------------------
create table public.roles (
  id            uuid primary key default gen_random_uuid(),
  tenant_id     uuid references public.tenants(id) on delete cascade, -- null = papel de sistema
  key           text not null,                            -- owner | admin | ... | custom_xyz
  name          text not null,
  base_role     public.member_role not null default 'member',
  is_system     boolean not null default false,
  description   text,
  created_at    timestamptz not null default now(),
  unique nulls not distinct (tenant_id, key)
);

-- Catálogo global de permissões
create table public.permissions (
  key           text primary key,                          -- 'task.update'
  module        text not null,                             -- 'tasks'
  description   text not null,
  platform_only boolean not null default false             -- true => só platform_admin
);

create table public.role_permissions (
  id              uuid primary key default gen_random_uuid(),
  role_key        text not null,                            -- corresponde a roles.key / member_role
  permission_key  text not null references public.permissions(key) on delete cascade,
  tenant_id       uuid references public.tenants(id) on delete cascade, -- null = padrão global
  -- NULLS NOT DISTINCT: garante uma única linha global por (papel, permissão)
  unique nulls not distinct (role_key, permission_key, tenant_id)
);

-- ---------------------------------------------------------------------
-- Memberships: usuário x empresa x papel
-- ---------------------------------------------------------------------
create table public.memberships (
  id             uuid primary key default gen_random_uuid(),
  tenant_id      uuid not null references public.tenants(id) on delete cascade,
  user_id        uuid not null references auth.users(id) on delete cascade,
  role           public.member_role not null default 'member',
  role_id        uuid references public.roles(id),          -- papel customizado opcional
  status         public.member_status not null default 'active',
  department_id  uuid,                                       -- FK adicionada em 0004
  job_title      text,
  invited_by     uuid references auth.users(id),
  joined_at      timestamptz not null default now(),
  last_access_at timestamptz,
  created_at     timestamptz not null default now(),
  updated_at     timestamptz not null default now(),
  unique (tenant_id, user_id)
);
create index on public.memberships (user_id) where status = 'active';
create index on public.memberships (tenant_id, role);

-- Sobrescrita de permissão por usuário (deny vence allow)
create table public.membership_permissions (
  membership_id  uuid not null references public.memberships(id) on delete cascade,
  permission_key text not null references public.permissions(key) on delete cascade,
  effect         public.perm_effect not null default 'allow',
  primary key (membership_id, permission_key)
);

-- ---------------------------------------------------------------------
-- Convites
-- ---------------------------------------------------------------------
create table public.invitations (
  id           uuid primary key default gen_random_uuid(),
  tenant_id    uuid not null references public.tenants(id) on delete cascade,
  email        text not null,
  role         public.member_role not null default 'member',
  token        text not null unique default encode(extensions.gen_random_bytes(24), 'hex'),
  invited_by   uuid references auth.users(id),
  message      text,
  expires_at   timestamptz not null default (now() + interval '7 days'),
  accepted_at  timestamptz,
  revoked_at   timestamptz,
  created_at   timestamptz not null default now(),
  constraint email_lowercase check (email = lower(email))
);
create index on public.invitations (tenant_id);
create index on public.invitations (lower(email)) where accepted_at is null and revoked_at is null;

-- ---------------------------------------------------------------------
-- Telas: catálogo + liberação por papel (configurado pelo responsável do domínio)
-- ---------------------------------------------------------------------
create table public.screens (
  key           text primary key,                          -- 'tasks.kanban'
  module        text not null,
  name          text not null,
  path          text not null,                             -- rota no front
  platform_only boolean not null default false,            -- só platform_admin
  order_index   int not null default 0
);

create table public.screen_access (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  role        public.member_role not null,
  screen_key  text not null references public.screens(key) on delete cascade,
  can_view    boolean not null default true,
  can_edit    boolean not null default false,
  updated_by  uuid references auth.users(id),
  updated_at  timestamptz not null default now(),
  unique (tenant_id, role, screen_key)
);
comment on table public.screen_access is
  'Quais telas cada papel enxerga dentro do tenant. Mantido pelo owner (responsável do domínio).';

-- ---------------------------------------------------------------------
-- Compartilhamento explícito de recurso (base do acesso de convidados)
-- ---------------------------------------------------------------------
create table public.resource_shares (
  id           uuid primary key default gen_random_uuid(),
  tenant_id    uuid not null references public.tenants(id) on delete cascade,
  entity       public.entity_type not null,
  entity_id    uuid not null,
  user_id      uuid references auth.users(id) on delete cascade,
  team_id      uuid,                                        -- FK adicionada em 0004
  can_edit     boolean not null default false,
  created_by   uuid references auth.users(id),
  created_at   timestamptz not null default now(),
  constraint share_target check (user_id is not null or team_id is not null)
);
create index on public.resource_shares (tenant_id, entity, entity_id);
create index on public.resource_shares (user_id);

-- ---------------------------------------------------------------------
-- Departamentos e Equipes (núcleo organizacional; usados nas funções de RLS)
-- ---------------------------------------------------------------------
create table public.departments (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  parent_id   uuid references public.departments(id) on delete set null,
  name        text not null,
  description text,
  manager_id  uuid references auth.users(id),
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  unique nulls not distinct (tenant_id, parent_id, name)
);
create index on public.departments (tenant_id);

create table public.teams (
  id            uuid primary key default gen_random_uuid(),
  tenant_id     uuid not null references public.tenants(id) on delete cascade,
  department_id uuid references public.departments(id) on delete set null,
  name          text not null,
  description   text,
  color         text,
  manager_id    uuid references auth.users(id),
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now(),
  unique (tenant_id, name)
);
create index on public.teams (tenant_id);

create table public.team_members (
  team_id    uuid not null references public.teams(id) on delete cascade,
  user_id    uuid not null references auth.users(id) on delete cascade,
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  is_leader  boolean not null default false,
  added_at   timestamptz not null default now(),
  primary key (team_id, user_id)
);
create index on public.team_members (user_id);
create index on public.team_members (tenant_id);

alter table public.memberships
  add constraint memberships_department_fk
  foreign key (department_id) references public.departments(id) on delete set null;

alter table public.resource_shares
  add constraint resource_shares_team_fk
  foreign key (team_id) references public.teams(id) on delete cascade;
