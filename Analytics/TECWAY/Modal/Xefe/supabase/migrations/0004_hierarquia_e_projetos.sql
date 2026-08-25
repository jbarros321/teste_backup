-- =====================================================================
-- 0004 — Hierarquia de trabalho: espaços, pastas, listas, projetos,
--        status configuráveis, tags, campos personalizados e visões.
-- =====================================================================

create table public.spaces (
  id            uuid primary key default gen_random_uuid(),
  tenant_id     uuid not null references public.tenants(id) on delete cascade,
  department_id uuid references public.departments(id) on delete set null,
  name          text not null,
  description   text,
  color         text default '#4F46E5',
  icon          text,
  is_private    boolean not null default false,
  position      numeric not null default 1000,
  created_by    uuid references auth.users(id),
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now(),
  deleted_at    timestamptz
);
create index on public.spaces (tenant_id, position);

create table public.folders (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  space_id    uuid not null references public.spaces(id) on delete cascade,
  name        text not null,
  description text,
  color       text,
  position    numeric not null default 1000,
  created_by  uuid references auth.users(id),
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  deleted_at  timestamptz
);
create index on public.folders (tenant_id, space_id, position);

create table public.projects (
  id             uuid primary key default gen_random_uuid(),
  tenant_id      uuid not null references public.tenants(id) on delete cascade,
  space_id       uuid references public.spaces(id) on delete set null,
  code           text,
  name           text not null,
  description    text,
  owner_id       uuid references auth.users(id),
  team_id        uuid references public.teams(id) on delete set null,
  client_name    text,
  start_date     date,
  end_date       date,
  status         text not null default 'planning',   -- planning|active|on_hold|done|canceled
  priority       public.task_priority not null default 'normal',
  budget         numeric(14,2),
  cost_actual    numeric(14,2) not null default 0,
  color          text,
  icon           text,
  progress       numeric(5,2) not null default 0,
  is_archived    boolean not null default false,
  created_by     uuid references auth.users(id),
  created_at     timestamptz not null default now(),
  updated_at     timestamptz not null default now(),
  deleted_at     timestamptz,
  constraint project_dates check (end_date is null or start_date is null or end_date >= start_date)
);
create index on public.projects (tenant_id, status);
create index on public.projects (tenant_id, owner_id);
create index on public.projects (tenant_id, space_id);

create table public.project_members (
  project_id uuid not null references public.projects(id) on delete cascade,
  user_id    uuid not null references auth.users(id) on delete cascade,
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  role       text not null default 'member',        -- owner|manager|member|viewer
  added_at   timestamptz not null default now(),
  primary key (project_id, user_id)
);
create index on public.project_members (tenant_id, user_id);

create table public.lists (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  space_id    uuid not null references public.spaces(id) on delete cascade,
  folder_id   uuid references public.folders(id) on delete cascade,
  project_id  uuid references public.projects(id) on delete set null,
  name        text not null,
  description text,
  color       text,
  position    numeric not null default 1000,
  require_subtasks_done boolean not null default false,
  default_assignee_id   uuid references auth.users(id),
  created_by  uuid references auth.users(id),
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  deleted_at  timestamptz
);
create index on public.lists (tenant_id, space_id, position);
create index on public.lists (tenant_id, project_id);

-- ---------------------------------------------------------------------
-- Status configuráveis. Escopo: tenant (space_id e list_id nulos),
-- espaço, ou lista. A resolução usa o mais específico disponível.
-- ---------------------------------------------------------------------
create table public.task_statuses (
  id         uuid primary key default gen_random_uuid(),
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  space_id   uuid references public.spaces(id) on delete cascade,
  list_id    uuid references public.lists(id) on delete cascade,
  name       text not null,
  category   public.status_category not null default 'todo',
  color      text not null default '#94A3B8',
  position   int not null default 0,
  is_default boolean not null default false,
  created_at timestamptz not null default now()
);
create index on public.task_statuses (tenant_id, space_id, list_id, position);

-- ---------------------------------------------------------------------
-- Tags
-- ---------------------------------------------------------------------
create table public.tags (
  id         uuid primary key default gen_random_uuid(),
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  name       text not null,
  color      text not null default '#64748B',
  created_at timestamptz not null default now(),
  unique (tenant_id, name)
);

-- ---------------------------------------------------------------------
-- Campos personalizados
-- ---------------------------------------------------------------------
create table public.custom_fields (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  entity      public.entity_type not null default 'task',
  space_id    uuid references public.spaces(id) on delete cascade,
  list_id     uuid references public.lists(id) on delete cascade,
  key         text not null,
  label       text not null,
  field_type  text not null check (field_type in
              ('text','number','currency','date','select','multiselect','checkbox','user','url','relation')),
  options     jsonb not null default '[]'::jsonb,
  is_required boolean not null default false,
  position    int not null default 0,
  created_at  timestamptz not null default now(),
  unique nulls not distinct (tenant_id, entity, space_id, list_id, key)
);

create table public.custom_field_values (
  id              uuid primary key default gen_random_uuid(),
  tenant_id       uuid not null references public.tenants(id) on delete cascade,
  custom_field_id uuid not null references public.custom_fields(id) on delete cascade,
  entity          public.entity_type not null,
  entity_id       uuid not null,
  value           jsonb,
  updated_by      uuid references auth.users(id),
  updated_at      timestamptz not null default now(),
  unique (custom_field_id, entity_id)
);
create index on public.custom_field_values (tenant_id, entity, entity_id);

-- ---------------------------------------------------------------------
-- Visões salvas (lista, kanban, calendário, gantt, timeline, tabela)
-- ---------------------------------------------------------------------
create table public.views (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  scope       text not null check (scope in ('space','folder','list','project','everything')),
  scope_id    uuid,
  type        text not null check (type in ('list','board','calendar','gantt','timeline','table')),
  name        text not null,
  config      jsonb not null default '{}'::jsonb,   -- filtros, colunas, agrupamento, ordenação
  is_shared   boolean not null default true,
  owner_id    uuid references auth.users(id),
  position    int not null default 0,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now()
);
create index on public.views (tenant_id, scope, scope_id);

-- ---------------------------------------------------------------------
-- Templates reutilizáveis
-- ---------------------------------------------------------------------
create table public.templates (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  type        text not null check (type in ('project','list','task','checklist','document')),
  name        text not null,
  description text,
  payload     jsonb not null,   -- estrutura com datas relativas: "+3d", "+2w"
  created_by  uuid references auth.users(id),
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now()
);
create index on public.templates (tenant_id, type);
