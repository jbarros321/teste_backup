-- =====================================================================
-- 0006 — Comentários, menções, reações, anexos, documentos e atividades
-- =====================================================================

create table public.comments (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  entity      public.entity_type not null default 'task',
  entity_id   uuid not null,
  parent_id   uuid references public.comments(id) on delete cascade,
  author_id   uuid not null references auth.users(id) on delete cascade,
  body        jsonb not null,          -- rich text
  body_text   text not null,           -- versão plana (busca e IA)
  is_edited   boolean not null default false,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  deleted_at  timestamptz
);
create index on public.comments (tenant_id, entity, entity_id, created_at desc);
create index on public.comments (tenant_id, author_id);

create table public.comment_reactions (
  comment_id uuid not null references public.comments(id) on delete cascade,
  user_id    uuid not null references auth.users(id) on delete cascade,
  tenant_id  uuid not null references public.tenants(id) on delete cascade,
  emoji      text not null,
  created_at timestamptz not null default now(),
  primary key (comment_id, user_id, emoji)
);

create table public.mentions (
  id           uuid primary key default gen_random_uuid(),
  tenant_id    uuid not null references public.tenants(id) on delete cascade,
  comment_id   uuid references public.comments(id) on delete cascade,
  entity       public.entity_type not null,
  entity_id    uuid not null,
  mentioned_user_id uuid references auth.users(id) on delete cascade,
  mentioned_team_id uuid references public.teams(id) on delete cascade,
  created_by   uuid references auth.users(id),
  created_at   timestamptz not null default now(),
  constraint mention_target check (mentioned_user_id is not null or mentioned_team_id is not null)
);
create index on public.mentions (tenant_id, mentioned_user_id);

-- ---------------------------------------------------------------------
-- Anexos (metadados; o binário vive no Supabase Storage)
-- ---------------------------------------------------------------------
create table public.attachments (
  id           uuid primary key default gen_random_uuid(),
  tenant_id    uuid not null references public.tenants(id) on delete cascade,
  entity       public.entity_type not null,
  entity_id    uuid not null,
  bucket       text not null default 'attachments',
  path         text not null,          -- tenant/{tenant_id}/{entity}/{entity_id}/{uuid}-{arquivo}
  file_name    text not null,
  mime_type    text,
  size_bytes   bigint not null default 0,
  checksum     text,
  uploaded_by  uuid references auth.users(id),
  created_at   timestamptz not null default now(),
  deleted_at   timestamptz,            -- lixeira: expurgo físico após 30 dias
  unique (bucket, path)
);
create index on public.attachments (tenant_id, entity, entity_id);

-- ---------------------------------------------------------------------
-- Documentos colaborativos
-- ---------------------------------------------------------------------
create table public.documents (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  space_id    uuid references public.spaces(id) on delete set null,
  project_id  uuid references public.projects(id) on delete set null,
  parent_id   uuid references public.documents(id) on delete cascade,
  title       text not null,
  content     jsonb not null default '{}'::jsonb,
  content_text text,
  icon        text,
  is_public   boolean not null default false,
  public_slug text unique,
  version     int not null default 1,
  created_by  uuid references auth.users(id),
  updated_by  uuid references auth.users(id),
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  deleted_at  timestamptz,
  search_tsv  tsvector
);
create index on public.documents (tenant_id, space_id);
create index on public.documents using gin (search_tsv);

create table public.document_versions (
  id          uuid primary key default gen_random_uuid(),
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  document_id uuid not null references public.documents(id) on delete cascade,
  version     int not null,
  content     jsonb not null,
  author_id   uuid references auth.users(id),
  created_at  timestamptz not null default now(),
  unique (document_id, version)
);

-- ---------------------------------------------------------------------
-- Feed de atividades (humano) e auditoria (técnica, imutável)
-- ---------------------------------------------------------------------
create table public.activity_logs (
  id          bigserial primary key,
  tenant_id   uuid not null references public.tenants(id) on delete cascade,
  entity      public.entity_type not null,
  entity_id   uuid not null,
  actor_id    uuid references auth.users(id),
  action      text not null,           -- created | updated | status_changed | commented | ...
  field       text,
  old_value   text,
  new_value   text,
  metadata    jsonb not null default '{}'::jsonb,
  created_at  timestamptz not null default now()
);
create index on public.activity_logs (tenant_id, entity, entity_id, created_at desc);
create index on public.activity_logs (tenant_id, actor_id, created_at desc);

create table public.audit_logs (
  id          bigserial primary key,
  tenant_id   uuid references public.tenants(id) on delete set null,
  actor_id    uuid references auth.users(id),
  table_name  text not null,
  record_id   uuid,
  operation   text not null,           -- INSERT | UPDATE | DELETE
  old_data    jsonb,
  new_data    jsonb,
  ip          inet,
  user_agent  text,
  created_at  timestamptz not null default now()
);
create index on public.audit_logs (tenant_id, table_name, created_at desc);
