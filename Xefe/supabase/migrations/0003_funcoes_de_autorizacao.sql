-- =====================================================================
-- 0003 — Funções de autorização usadas pelas policies de RLS
--
-- Todas são SECURITY DEFINER (para não recursionar no RLS de memberships)
-- e STABLE (para o planner transformá-las em InitPlan: uma avaliação por
-- query, não por linha). Sempre chame-as dentro de (select fn()).
-- =====================================================================

-- Super admin da plataforma: enxerga e controla tudo.
create or replace function public.is_platform_admin()
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select exists (
    select 1 from public.platform_admins pa
    where pa.user_id = (select auth.uid())
  );
$$;

-- Todos os tenants em que o usuário tem membership ativa (inclui convidados).
create or replace function public.user_tenant_ids()
returns setof uuid
language sql
stable
security definer
set search_path = ''
as $$
  select m.tenant_id
  from public.memberships m
  where m.user_id = (select auth.uid())
    and m.status  = 'active';
$$;

-- Tenants com acesso pleno (exclui convidados, que só veem o que foi compartilhado).
create or replace function public.user_tenant_ids_full()
returns setof uuid
language sql
stable
security definer
set search_path = ''
as $$
  select m.tenant_id
  from public.memberships m
  where m.user_id = (select auth.uid())
    and m.status  = 'active'
    and m.role   <> 'guest';
$$;

create or replace function public.is_member(p_tenant uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select exists (
    select 1 from public.memberships m
    where m.user_id = (select auth.uid())
      and m.tenant_id = p_tenant
      and m.status = 'active'
  );
$$;

create or replace function public.my_role(p_tenant uuid)
returns public.member_role
language sql
stable
security definer
set search_path = ''
as $$
  select m.role
  from public.memberships m
  where m.user_id = (select auth.uid())
    and m.tenant_id = p_tenant
    and m.status = 'active'
  limit 1;
$$;

-- Papel do usuário é >= papel exigido.
create or replace function public.has_min_role(p_tenant uuid, p_role public.member_role)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select public.is_platform_admin()
      or coalesce(public.role_rank(public.my_role(p_tenant)) >= public.role_rank(p_role), false);
$$;

-- Permissão efetiva: deny explícito vence; depois override allow; depois papel.
create or replace function public.has_perm(p_tenant uuid, p_perm text)
returns boolean
language plpgsql
stable
security definer
set search_path = ''
as $$
declare
  v_membership uuid;
  v_role       public.member_role;
  v_effect     public.perm_effect;
  v_platform_only boolean;
begin
  if public.is_platform_admin() then
    return true;
  end if;

  -- Permissões marcadas como platform_only jamais são concedidas a papéis de tenant.
  select p.platform_only into v_platform_only
  from public.permissions p where p.key = p_perm;
  if coalesce(v_platform_only, false) then
    return false;
  end if;

  select m.id, m.role into v_membership, v_role
  from public.memberships m
  where m.user_id = (select auth.uid())
    and m.tenant_id = p_tenant
    and m.status = 'active'
  limit 1;

  if v_membership is null then
    return false;
  end if;

  select mp.effect into v_effect
  from public.membership_permissions mp
  where mp.membership_id = v_membership
    and mp.permission_key = p_perm;

  if v_effect = 'deny'  then return false; end if;
  if v_effect = 'allow' then return true;  end if;

  -- Owner tem tudo dentro do próprio tenant (menos o que é platform_only).
  if v_role = 'owner' then
    return true;
  end if;

  return exists (
    select 1 from public.role_permissions rp
    where rp.permission_key = p_perm
      and rp.role_key = v_role::text
      and (rp.tenant_id = p_tenant or rp.tenant_id is null)
  );
end;
$$;

-- Tela liberada para o papel do usuário no tenant.
create or replace function public.can_see_screen(p_tenant uuid, p_screen text)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select case
    when public.is_platform_admin() then true
    when exists (select 1 from public.screens s where s.key = p_screen and s.platform_only) then false
    else coalesce((
      select sa.can_view
      from public.screen_access sa
      where sa.tenant_id = p_tenant
        and sa.screen_key = p_screen
        and sa.role = public.my_role(p_tenant)
    ), false)
  end;
$$;

-- Recurso compartilhado explicitamente com o usuário (ou com uma equipe dele).
create or replace function public.has_share(p_entity public.entity_type, p_entity_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select exists (
    select 1
    from public.resource_shares rs
    where rs.entity = p_entity
      and rs.entity_id = p_entity_id
      and (
        rs.user_id = (select auth.uid())
        or rs.team_id in (
          select tm.team_id from public.team_members tm
          where tm.user_id = (select auth.uid())
        )
      )
  );
$$;

-- Leitura padrão de conteúdo: admin global, membro pleno do tenant, ou share explícito.
create or replace function public.can_read(p_tenant uuid, p_entity public.entity_type, p_entity_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
  select public.is_platform_admin()
      or p_tenant in (select public.user_tenant_ids_full())
      or public.has_share(p_entity, p_entity_id);
$$;

-- ---------------------------------------------------------------------
-- Entrada automática por domínio de e-mail / convite
-- ---------------------------------------------------------------------
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_email   text := lower(new.email);
  v_domain  text := split_part(lower(new.email), '@', 2);
  v_dom     public.tenant_domains%rowtype;
  v_inv     public.invitations%rowtype;
begin
  insert into public.profiles (id, email, full_name, avatar_url)
  values (
    new.id,
    v_email,
    coalesce(new.raw_user_meta_data->>'full_name', new.raw_user_meta_data->>'name', split_part(v_email,'@',1)),
    new.raw_user_meta_data->>'avatar_url'
  )
  on conflict (id) do update set email = excluded.email;

  -- 1) Domínio corporativo verificado com auto-join
  select * into v_dom
  from public.tenant_domains d
  where d.domain = v_domain
    and d.verified_at is not null
    and d.auto_join
  limit 1;

  if found then
    insert into public.memberships (tenant_id, user_id, role, status)
    values (v_dom.tenant_id, new.id, v_dom.default_role, 'active')
    on conflict (tenant_id, user_id) do nothing;
    return new;
  end if;

  -- 2) Convite pendente
  select * into v_inv
  from public.invitations i
  where lower(i.email) = v_email
    and i.accepted_at is null
    and i.revoked_at is null
    and i.expires_at > now()
  order by i.created_at desc
  limit 1;

  if found then
    insert into public.memberships (tenant_id, user_id, role, status, invited_by)
    values (v_inv.tenant_id, new.id, v_inv.role, 'active', v_inv.invited_by)
    on conflict (tenant_id, user_id) do nothing;

    update public.invitations set accepted_at = now() where id = v_inv.id;
  end if;

  -- 3) Sem domínio e sem convite: usuário fica sem tenant (tela "aguardando convite").
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- ---------------------------------------------------------------------
-- Contexto da sessão consumido pelo front logo após o login
-- ---------------------------------------------------------------------
create or replace function public.me()
returns jsonb
language sql
stable
security definer
set search_path = ''
as $$
  select jsonb_build_object(
    'user_id', (select auth.uid()),
    'is_platform_admin', public.is_platform_admin(),
    'profile', (select to_jsonb(p) from public.profiles p where p.id = (select auth.uid())),
    'memberships', coalesce((
      select jsonb_agg(jsonb_build_object(
        'tenant_id', t.id,
        'tenant_name', t.name,
        'tenant_slug', t.slug,
        'logo_url', t.logo_url,
        'role', m.role,
        'status', m.status,
        'screens', coalesce((
          select jsonb_agg(sa.screen_key order by s.order_index)
          from public.screen_access sa
          join public.screens s on s.key = sa.screen_key
          where sa.tenant_id = t.id and sa.role = m.role and sa.can_view
        ), '[]'::jsonb),
        'permissions', coalesce((
          select jsonb_agg(distinct rp.permission_key)
          from public.role_permissions rp
          where rp.role_key = m.role::text
            and (rp.tenant_id = t.id or rp.tenant_id is null)
        ), '[]'::jsonb)
      ))
      from public.memberships m
      join public.tenants t on t.id = m.tenant_id
      where m.user_id = (select auth.uid()) and m.status = 'active'
    ), '[]'::jsonb)
  );
$$;

grant execute on function
  public.is_platform_admin(), public.user_tenant_ids(), public.user_tenant_ids_full(),
  public.is_member(uuid), public.my_role(uuid), public.has_min_role(uuid, public.member_role),
  public.has_perm(uuid, text), public.can_see_screen(uuid, text),
  public.has_share(public.entity_type, uuid),
  public.can_read(uuid, public.entity_type, uuid), public.me()
to authenticated;
