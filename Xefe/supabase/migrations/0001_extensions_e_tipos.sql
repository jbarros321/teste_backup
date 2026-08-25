-- =====================================================================
-- 0001 — Extensões e tipos
-- =====================================================================
create extension if not exists "pgcrypto"    with schema extensions;
create extension if not exists "pg_trgm"     with schema extensions;
create extension if not exists "btree_gin"   with schema extensions;
create extension if not exists "unaccent"    with schema extensions;

-- Papéis dentro de um tenant. platform_admin NÃO aparece aqui: é global,
-- vive na tabela platform_admins e está fora do modelo de tenant.
create type public.member_role as enum (
  'owner',      -- responsável do domínio / proprietário da empresa
  'admin',      -- administrador delegado
  'manager',    -- gestor de área
  'leader',     -- líder de equipe
  'member',     -- colaborador
  'guest'       -- convidado externo
);

create type public.member_status  as enum ('active','inactive','blocked','invited');
create type public.tenant_status  as enum ('trial','active','suspended','canceled');
create type public.task_priority  as enum ('urgent','high','normal','low','none');
create type public.status_category as enum ('todo','in_progress','done','canceled');
create type public.relation_type  as enum ('blocks','blocked_by','relates_to','waits_for','depends_on');
create type public.goal_unit      as enum ('number','currency','percent','boolean');
create type public.perm_effect    as enum ('allow','deny');
create type public.entity_type    as enum (
  'tenant','space','folder','list','project','task','document','dashboard','goal','comment'
);

-- Ordem de precedência dos papéis (maior = mais poder). Usada por has_min_role().
create or replace function public.role_rank(r public.member_role)
returns int
language sql
immutable
parallel safe
as $$
  select case r
    when 'owner'   then 60
    when 'admin'   then 50
    when 'manager' then 40
    when 'leader'  then 30
    when 'member'  then 20
    when 'guest'   then 10
    else 0
  end;
$$;

comment on type public.member_role is
  'Papéis de tenant. O super admin da plataforma é definido em public.platform_admins.';
