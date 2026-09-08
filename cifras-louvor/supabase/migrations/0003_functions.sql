-- =====================================================================
-- Gatilhos, busca e operações que precisam rodar no servidor
-- =====================================================================

-- --------------------------------------------------------------------
-- Slug legível a partir de um título. Precisa vir primeiro: as funções
-- e gatilhos abaixo chamam esta.
-- --------------------------------------------------------------------
create or replace function public.slugify(p_text text)
returns text
language sql
stable
as $$
  select coalesce(
    nullif(
      trim(both '-' from regexp_replace(lower(unaccent(p_text)), '[^a-z0-9]+', '-', 'g')),
      ''
    ),
    'sem-titulo'
  );
$$;

-- --------------------------------------------------------------------
-- Todo usuário novo ganha perfil e preferências
-- --------------------------------------------------------------------
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, email, name, avatar_url)
  values (
    new.id,
    new.email,
    coalesce(new.raw_user_meta_data ->> 'name', new.raw_user_meta_data ->> 'full_name'),
    new.raw_user_meta_data ->> 'avatar_url'
  )
  on conflict (id) do nothing;

  insert into public.user_settings (user_id)
  values (new.id)
  on conflict (user_id) do nothing;

  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- --------------------------------------------------------------------
-- updated_at automático
-- --------------------------------------------------------------------
create or replace function public.touch_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create trigger songs_touch_updated_at
  before update on public.songs
  for each row execute function public.touch_updated_at();

-- --------------------------------------------------------------------
-- Quem cria a igreja entra como administrador
--
-- Roda com SECURITY DEFINER porque a policy de church_members exigiria
-- que o usuário já fosse membro para se inserir como líder.
-- --------------------------------------------------------------------
create or replace function public.create_church(p_name text)
returns public.churches
language plpgsql
security definer
set search_path = public
as $$
declare
  new_church public.churches;
  base_slug text;
  final_slug text;
  attempt integer := 0;
begin
  if auth.uid() is null then
    raise exception 'É preciso estar autenticado para criar uma igreja';
  end if;

  base_slug := public.slugify(p_name);
  final_slug := base_slug;

  while exists (select 1 from public.churches c where c.slug = final_slug) loop
    attempt := attempt + 1;
    final_slug := base_slug || '-' || attempt;
  end loop;

  insert into public.churches (name, slug, created_by)
  values (p_name, final_slug, auth.uid())
  returning * into new_church;

  insert into public.church_members (church_id, user_id, role)
  values (new_church.id, auth.uid(), 'admin');

  return new_church;
end;
$$;

-- --------------------------------------------------------------------
-- Entrar numa igreja pelo código de convite
-- --------------------------------------------------------------------
create or replace function public.join_church(p_invite_code text)
returns public.churches
language plpgsql
security definer
set search_path = public
as $$
declare
  target public.churches;
begin
  if auth.uid() is null then
    raise exception 'É preciso estar autenticado para entrar numa igreja';
  end if;

  select * into target from public.churches c
  where upper(c.invite_code) = upper(trim(p_invite_code));

  if target.id is null then
    raise exception 'Código de convite inválido';
  end if;

  insert into public.church_members (church_id, user_id, role)
  values (target.id, auth.uid(), 'musician')
  on conflict (church_id, user_id) do nothing;

  return target;
end;
$$;

-- --------------------------------------------------------------------
-- Busca de músicas
--
-- Combina full-text em português com similaridade por trigrama, para que
-- "oce" encontre "Oceans" e um erro de digitação não zere o resultado.
-- --------------------------------------------------------------------
create or replace function public.search_songs(
  p_church_id uuid,
  p_query text,
  p_limit integer default 30
)
returns setof public.songs
language sql
stable
as $$
  select s.*
  from public.songs s
  where s.church_id = p_church_id
    and s.status <> 'archived'
    and (
      coalesce(trim(p_query), '') = ''
      or s.search_vector @@ websearch_to_tsquery('portuguese', p_query)
      or s.title ilike '%' || p_query || '%'
      or similarity(s.title, p_query) > 0.2
    )
  order by
    case when coalesce(trim(p_query), '') = '' then 0
         else greatest(
           ts_rank(s.search_vector, websearch_to_tsquery('portuguese', p_query)),
           similarity(s.title, p_query)
         )
    end desc,
    s.updated_at desc
  limit least(coalesce(p_limit, 30), 100);
$$;

-- --------------------------------------------------------------------
-- Categorias padrão para cada igreja nova
-- --------------------------------------------------------------------
create or replace function public.seed_default_categories()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  item text;
  idx integer := 0;
begin
  foreach item in array array[
    'Adoração', 'Louvor', 'Celebração', 'Entrada', 'Oferta',
    'Santa Ceia', 'Encerramento', 'Ministério Infantil', 'Jovens', 'Especial'
  ]
  loop
    insert into public.categories (church_id, name, slug, sort_order)
    values (new.id, item, public.slugify(item), idx)
    on conflict (church_id, slug) do nothing;
    idx := idx + 1;
  end loop;

  return new;
end;
$$;

create trigger churches_seed_categories
  after insert on public.churches
  for each row execute function public.seed_default_categories();
