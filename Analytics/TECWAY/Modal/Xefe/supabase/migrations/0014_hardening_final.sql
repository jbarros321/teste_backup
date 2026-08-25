-- =====================================================================
-- 0014 — Endurecimento final
--
-- Roda por último de propósito: REVOKE só afeta objetos que já existem,
-- e as views e funções de 0012/0013 são criadas depois do 0010.
-- =====================================================================

-- O papel anon (usado por qualquer visitante sem login) não acessa nada.
-- Todo o produto exige autenticação.
revoke all on all tables    in schema public from anon;
revoke all on all functions in schema public from anon;
revoke all on all sequences in schema public from anon;

-- E nada que for criado no futuro volta a liberar anon por engano.
alter default privileges in schema public revoke all on tables    from anon;
alter default privileges in schema public revoke all on functions from anon;
alter default privileges in schema public revoke all on sequences from anon;

-- Views de leitura usadas pelo app
grant select on public.v_tasks_full, public.v_project_stats, public.v_workload to authenticated;

-- ---------------------------------------------------------------------
-- Rede de segurança: acusa qualquer tabela sem RLS.
-- Chame em CI; deve retornar zero linhas.
-- ---------------------------------------------------------------------
create or replace function public.fn_check_rls_coverage()
returns table (schema_name text, table_name text)
language sql
stable
security definer
set search_path = ''
as $$
  select t.schemaname::text, t.tablename::text
  from pg_tables t
  where t.schemaname = 'public'
    and t.rowsecurity = false;
$$;

comment on function public.fn_check_rls_coverage is
  'Tabelas em public sem RLS. Qualquer linha retornada deve quebrar o build.';
