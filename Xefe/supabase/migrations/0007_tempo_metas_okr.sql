-- =====================================================================
-- 0007 — Apontamento de horas, timesheet, carga, metas e OKRs
-- =====================================================================

create table public.time_entries (
  id            uuid primary key default gen_random_uuid(),
  tenant_id     uuid not null references public.tenants(id) on delete cascade,
  user_id       uuid not null references auth.users(id) on delete cascade,
  task_id       uuid references public.tasks(id) on delete set null,
  project_id    uuid references public.projects(id) on delete set null,
  started_at    timestamptz not null,
  ended_at      timestamptz,
  duration_minutes int generated always as (
    case when ended_at is null then null
         else greatest(0, (extract(epoch from (ended_at - started_at)) / 60)::int)
    end
  ) stored,
  description   text,
  is_billable   boolean not null default false,
  source        text not null default 'timer' check (source in ('timer','manual','import')),
  approved_by   uuid references auth.users(id),
  approved_at   timestamptz,
  rejected_reason text,
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now(),
  constraint time_range check (ended_at is null or ended_at >= started_at)
);
create index on public.time_entries (tenant_id, user_id, started_at desc);
create index on public.time_entries (tenant_id, task_id);
create index on public.time_entries (tenant_id, project_id);
-- Um único cronômetro aberto por usuário
create unique index one_running_timer_per_user
  on public.time_entries (user_id) where ended_at is null;

create table public.timesheets (
  id           uuid primary key default gen_random_uuid(),
  tenant_id    uuid not null references public.tenants(id) on delete cascade,
  user_id      uuid not null references auth.users(id) on delete cascade,
  period_start date not null,
  period_end   date not null,
  status       text not null default 'draft' check (status in ('draft','submitted','approved','rejected')),
  total_minutes int not null default 0,
  submitted_at timestamptz,
  reviewed_by  uuid references auth.users(id),
  reviewed_at  timestamptz,
  notes        text,
  created_at   timestamptz not null default now(),
  unique (tenant_id, user_id, period_start)
);

-- ---------------------------------------------------------------------
-- Metas
-- ---------------------------------------------------------------------
create table public.goals (
  id            uuid primary key default gen_random_uuid(),
  tenant_id     uuid not null references public.tenants(id) on delete cascade,
  name          text not null,
  objective     text,
  owner_id      uuid references auth.users(id),
  team_id       uuid references public.teams(id) on delete set null,
  period_start  date not null,
  period_end    date not null,
  unit          public.goal_unit not null default 'number',
  start_value   numeric(18,4) not null default 0,
  target_value  numeric(18,4) not null,
  current_value numeric(18,4) not null default 0,
  progress      numeric(5,2) not null default 0,
  status        text not null default 'on_track' check (status in ('on_track','at_risk','off_track','done')),
  auto_source   text,                    -- ex.: 'tasks_completed', 'hours_logged'
  created_by    uuid references auth.users(id),
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now()
);
create index on public.goals (tenant_id, period_end);

create table public.goal_updates (
  id         uuid primary key default gen_random_uuid(),
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  goal_id    uuid not null references public.goals(id) on delete cascade,
  value      numeric(18,4) not null,
  note       text,
  created_by uuid references auth.users(id),
  created_at timestamptz not null default now()
);

-- ---------------------------------------------------------------------
-- OKRs
-- ---------------------------------------------------------------------
create table public.okr_objectives (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  title       text not null,
  description text,
  owner_id    uuid references auth.users(id),
  team_id     uuid references public.teams(id) on delete set null,
  cycle       text not null,                 -- '2026-Q3'
  progress    numeric(5,2) not null default 0,
  status      text not null default 'active',
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now()
);
create index on public.okr_objectives (tenant_id, cycle);

create table public.okr_key_results (
  id            uuid primary key default gen_random_uuid(),
  tenant_id     uuid not null references public.tenants(id) on delete cascade,
  objective_id  uuid not null references public.okr_objectives(id) on delete cascade,
  title         text not null,
  owner_id      uuid references auth.users(id),
  unit          public.goal_unit not null default 'number',
  start_value   numeric(18,4) not null default 0,
  target_value  numeric(18,4) not null,
  current_value numeric(18,4) not null default 0,
  progress      numeric(5,2) not null default 0,
  goal_id       uuid references public.goals(id) on delete set null,
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now()
);
create index on public.okr_key_results (tenant_id, objective_id);
