-- =====================================================================
-- 0005 — Tarefas, subtarefas, responsáveis, relações e checklists
-- =====================================================================

create table public.tasks (
  id               uuid primary key default gen_random_uuid(),
  tenant_id        uuid not null references public.tenants(id) on delete cascade,
  list_id          uuid not null references public.lists(id) on delete cascade,
  project_id       uuid references public.projects(id) on delete set null,
  parent_task_id   uuid references public.tasks(id) on delete cascade,
  seq              bigint,                                  -- número visível (TASK-128), por tenant
  title            text not null,
  description      jsonb,                                   -- documento rich text (TipTap)
  description_text text,                                    -- versão plana para busca
  status_id        uuid references public.task_statuses(id),
  priority         public.task_priority not null default 'normal',
  start_date       timestamptz,
  due_date         timestamptz,
  completed_at     timestamptz,
  estimate_minutes int,
  time_spent_minutes int not null default 0,
  progress         numeric(5,2) not null default 0,
  position         numeric not null default 1000,
  is_milestone     boolean not null default false,
  created_by       uuid references auth.users(id),
  created_at       timestamptz not null default now(),
  updated_at       timestamptz not null default now(),
  deleted_at       timestamptz,
  search_tsv       tsvector,
  constraint task_dates check (due_date is null or start_date is null or due_date >= start_date),
  constraint task_not_self_parent check (parent_task_id is null or parent_task_id <> id)
);

create index on public.tasks (tenant_id, list_id, position) where deleted_at is null;
create index on public.tasks (tenant_id, status_id)        where deleted_at is null;
create index on public.tasks (tenant_id, due_date)         where deleted_at is null;
create index on public.tasks (tenant_id, project_id)       where deleted_at is null;
create index on public.tasks (tenant_id, parent_task_id);
create index on public.tasks using gin (search_tsv);
create unique index on public.tasks (tenant_id, seq);

create table public.task_assignees (
  task_id    uuid not null references public.tasks(id) on delete cascade,
  user_id    uuid not null references auth.users(id) on delete cascade,
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  is_primary boolean not null default false,
  assigned_by uuid references auth.users(id),
  assigned_at timestamptz not null default now(),
  primary key (task_id, user_id)
);
create index on public.task_assignees (tenant_id, user_id);
-- Apenas um responsável principal por tarefa
create unique index task_one_primary_assignee
  on public.task_assignees (task_id) where is_primary;

create table public.task_watchers (
  task_id   uuid not null references public.tasks(id) on delete cascade,
  user_id   uuid not null references auth.users(id) on delete cascade,
  tenant_id uuid not null references public.tenants(id) on delete cascade,
  primary key (task_id, user_id)
);

create table public.task_tags (
  task_id   uuid not null references public.tasks(id) on delete cascade,
  tag_id    uuid not null references public.tags(id) on delete cascade,
  tenant_id uuid not null references public.tenants(id) on delete cascade,
  primary key (task_id, tag_id)
);
create index on public.task_tags (tenant_id, tag_id);

create table public.task_relations (
  id           uuid primary key default gen_random_uuid(),
  tenant_id    uuid not null references public.tenants(id) on delete cascade,
  source_task_id uuid not null references public.tasks(id) on delete cascade,
  target_task_id uuid not null references public.tasks(id) on delete cascade,
  relation     public.relation_type not null,
  lag_days     int not null default 0,          -- folga para Gantt
  created_by   uuid references auth.users(id),
  created_at   timestamptz not null default now(),
  unique (source_task_id, target_task_id, relation),
  constraint relation_not_self check (source_task_id <> target_task_id)
);
create index on public.task_relations (tenant_id, target_task_id);

-- ---------------------------------------------------------------------
-- Checklists
-- ---------------------------------------------------------------------
create table public.checklists (
  id         uuid primary key default gen_random_uuid(),
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  task_id    uuid not null references public.tasks(id) on delete cascade,
  name       text not null default 'Checklist',
  position   numeric not null default 1000,
  progress   numeric(5,2) not null default 0,   -- mantido por trigger
  created_by uuid references auth.users(id),
  created_at timestamptz not null default now()
);
create index on public.checklists (tenant_id, task_id);

create table public.checklist_items (
  id           uuid primary key default gen_random_uuid(),
  tenant_id    uuid not null references public.tenants(id) on delete cascade,
  checklist_id uuid not null references public.checklists(id) on delete cascade,
  content      text not null,
  is_done      boolean not null default false,
  assignee_id  uuid references auth.users(id),
  due_date     date,
  position     numeric not null default 1000,
  done_by      uuid references auth.users(id),
  done_at      timestamptz,
  created_at   timestamptz not null default now()
);
create index on public.checklist_items (tenant_id, checklist_id, position);

-- ---------------------------------------------------------------------
-- Numeração sequencial visível por tenant (TASK-1, TASK-2, ...)
-- ---------------------------------------------------------------------
create table public.tenant_sequences (
  tenant_id uuid not null references public.tenants(id) on delete cascade,
  name      text not null,
  value     bigint not null default 0,
  primary key (tenant_id, name)
);

create or replace function public.next_seq(p_tenant uuid, p_name text)
returns bigint
language plpgsql
security definer
set search_path = ''
as $$
declare v bigint;
begin
  insert into public.tenant_sequences (tenant_id, name, value)
  values (p_tenant, p_name, 1)
  on conflict (tenant_id, name)
  do update set value = public.tenant_sequences.value + 1
  returning value into v;
  return v;
end;
$$;
