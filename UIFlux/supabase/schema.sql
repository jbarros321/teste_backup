-- =============================================================================
-- UIFlux — Schema multi-tenant para Supabase
-- =============================================================================
-- Como usar:
--   1. Crie um projeto em https://supabase.com
--   2. Abra o "SQL Editor" no painel do projeto
--   3. Cole este arquivo inteiro e clique em "Run"
--   4. Copie a URL e a chave anon (Project Settings > API) para o arquivo .env
--
-- Modelo de acesso: MULTI-EMPRESA (multi-tenant).
--   - Cada empresa (companies) é isolada.
--   - Cada usuário (profiles) pertence a UMA empresa e tem um papel (role).
--   - Projetos e documentos gerados pertencem a uma empresa.
--   - Row Level Security (RLS) garante que um usuário só enxerga dados da sua empresa.
-- =============================================================================

-- Extensão para gerar UUIDs
create extension if not exists "pgcrypto";

-- -----------------------------------------------------------------------------
-- 1. EMPRESAS (tenants)
-- -----------------------------------------------------------------------------
create table if not exists public.companies (
  id          uuid primary key default gen_random_uuid(),
  name        text not null,
  tokens      integer not null default 150,
  created_at  timestamptz not null default now()
);

-- -----------------------------------------------------------------------------
-- 2. PERFIS DE USUÁRIO (ligados ao auth.users do Supabase)
-- -----------------------------------------------------------------------------
do $$ begin
  create type public.user_role as enum ('admin', 'gestor', 'membro');
exception
  when duplicate_object then null;
end $$;

create table if not exists public.profiles (
  id          uuid primary key references auth.users(id) on delete cascade,
  company_id  uuid not null references public.companies(id) on delete cascade,
  email       text not null,
  full_name   text,
  role        public.user_role not null default 'membro',
  created_at  timestamptz not null default now()
);

create index if not exists profiles_company_id_idx on public.profiles(company_id);

-- -----------------------------------------------------------------------------
-- 3. PROJETOS (fluxogramas / organogramas)
--    Os campos ricos (nós, conexões, comentários, versões, auditoria) ficam em
--    colunas jsonb para espelhar a estrutura atual do front-end sem fricção.
-- -----------------------------------------------------------------------------
create table if not exists public.projects (
  id          uuid primary key default gen_random_uuid(),
  company_id  uuid not null references public.companies(id) on delete cascade,
  name        text not null,
  description text default '',
  category    text default '',
  status      text not null default 'draft',
  version     text not null default '1.0.0',
  team        jsonb not null default '[]'::jsonb,
  nodes       jsonb not null default '[]'::jsonb,
  edges       jsonb not null default '[]'::jsonb,
  comments    jsonb not null default '[]'::jsonb,
  versions    jsonb not null default '[]'::jsonb,
  audit_logs  jsonb not null default '[]'::jsonb,
  created_by  uuid references public.profiles(id) on delete set null,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now()
);

create index if not exists projects_company_id_idx on public.projects(company_id);

-- -----------------------------------------------------------------------------
-- 4. DOCUMENTOS GERADOS (histórico de POPs gerados a partir dos fluxos)
-- -----------------------------------------------------------------------------
create table if not exists public.documents (
  id          uuid primary key default gen_random_uuid(),
  company_id  uuid not null references public.companies(id) on delete cascade,
  project_id  uuid references public.projects(id) on delete set null,
  title       text not null,
  doc_type    text not null default 'POP',
  version     text,
  content     jsonb,              -- snapshot do projeto/relatório no momento da geração
  generated_by uuid references public.profiles(id) on delete set null,
  created_at  timestamptz not null default now()
);

create index if not exists documents_company_id_idx on public.documents(company_id);
create index if not exists documents_project_id_idx on public.documents(project_id);

-- =============================================================================
-- FUNÇÃO AUXILIAR: company_id do usuário logado
-- =============================================================================
create or replace function public.current_company_id()
returns uuid
language sql
stable
security definer
set search_path = public
as $$
  select company_id from public.profiles where id = auth.uid();
$$;

-- =============================================================================
-- ROW LEVEL SECURITY
-- =============================================================================
alter table public.companies enable row level security;
alter table public.profiles  enable row level security;
alter table public.projects  enable row level security;
alter table public.documents enable row level security;

-- COMPANIES: usuário vê e edita apenas a própria empresa
drop policy if exists companies_select on public.companies;
create policy companies_select on public.companies
  for select using (id = public.current_company_id());

drop policy if exists companies_update on public.companies;
create policy companies_update on public.companies
  for update using (id = public.current_company_id());

-- PROFILES: usuário vê perfis da própria empresa; edita só o próprio
drop policy if exists profiles_select on public.profiles;
create policy profiles_select on public.profiles
  for select using (company_id = public.current_company_id());

drop policy if exists profiles_update_self on public.profiles;
create policy profiles_update_self on public.profiles
  for update using (id = auth.uid());

-- PROJECTS: acesso total restrito à própria empresa
drop policy if exists projects_all on public.projects;
create policy projects_all on public.projects
  for all
  using (company_id = public.current_company_id())
  with check (company_id = public.current_company_id());

-- DOCUMENTS: acesso total restrito à própria empresa
drop policy if exists documents_all on public.documents;
create policy documents_all on public.documents
  for all
  using (company_id = public.current_company_id())
  with check (company_id = public.current_company_id());

-- =============================================================================
-- TRIGGER: ao criar um usuário no Auth, cria a empresa + o perfil (admin)
-- =============================================================================
-- O nome da empresa e o nome completo são lidos do metadata enviado no signUp:
--   supabase.auth.signUp({ email, password, options: { data: { company_name, full_name } } })
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  new_company_id uuid;
  company_label  text;
begin
  company_label := coalesce(
    nullif(new.raw_user_meta_data ->> 'company_name', ''),
    split_part(new.email, '@', 2),
    'Minha Empresa'
  );

  insert into public.companies (name)
  values (company_label)
  returning id into new_company_id;

  insert into public.profiles (id, company_id, email, full_name, role)
  values (
    new.id,
    new_company_id,
    new.email,
    nullif(new.raw_user_meta_data ->> 'full_name', ''),
    'admin'   -- primeiro usuário da empresa é admin
  );

  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- =============================================================================
-- Pronto. Após rodar, habilite Email/Password em Authentication > Providers.
-- =============================================================================
