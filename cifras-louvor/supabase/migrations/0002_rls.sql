-- =====================================================================
-- Row Level Security — isolamento entre igrejas
--
-- Regra central: nenhuma linha de conteúdo é visível fora da igreja dona.
-- As checagens passam por funções SECURITY DEFINER; se as policies
-- consultassem church_members diretamente, a policy de church_members
-- chamaria a si mesma e o Postgres abortaria por recursão.
-- =====================================================================

create or replace function public.is_church_member(target uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1 from public.church_members m
    where m.church_id = target and m.user_id = auth.uid()
  );
$$;

create or replace function public.church_role_of(target uuid)
returns public.church_role
language sql
stable
security definer
set search_path = public
as $$
  select m.role from public.church_members m
  where m.church_id = target and m.user_id = auth.uid()
  limit 1;
$$;

/** Pode criar e editar músicas e repertórios: todos menos o espectador. */
create or replace function public.can_edit_content(target uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select public.church_role_of(target) in ('admin', 'leader', 'musician');
$$;

/** Pode mexer na igreja e nos membros. */
create or replace function public.is_church_admin(target uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select public.church_role_of(target) in ('admin', 'leader');
$$;

/** A igreja dona de uma música — usada pelas tabelas filhas. */
create or replace function public.church_of_song(target uuid)
returns uuid
language sql
stable
security definer
set search_path = public
as $$
  select s.church_id from public.songs s where s.id = target;
$$;

alter table public.profiles          enable row level security;
alter table public.churches          enable row level security;
alter table public.church_members    enable row level security;
alter table public.artists           enable row level security;
alter table public.categories        enable row level security;
alter table public.songs             enable row level security;
alter table public.song_categories   enable row level security;
alter table public.song_sections     enable row level security;
alter table public.song_lines        enable row level security;
alter table public.chords            enable row level security;
alter table public.song_versions     enable row level security;
alter table public.repertoires       enable row level security;
alter table public.repertoire_songs  enable row level security;
alter table public.favorites         enable row level security;
alter table public.song_history      enable row level security;
alter table public.user_settings     enable row level security;
alter table public.system_logs       enable row level security;

-- --------------------------------------------------------------- perfis
create policy "perfil próprio é visível" on public.profiles
  for select using (id = auth.uid());

create policy "membros da mesma igreja se enxergam" on public.profiles
  for select using (
    exists (
      select 1 from public.church_members mine
      join public.church_members theirs on theirs.church_id = mine.church_id
      where mine.user_id = auth.uid() and theirs.user_id = profiles.id
    )
  );

create policy "edita o próprio perfil" on public.profiles
  for update using (id = auth.uid()) with check (id = auth.uid());

-- -------------------------------------------------------------- igrejas
create policy "vê a igreja de que participa" on public.churches
  for select using (public.is_church_member(id));

create policy "qualquer autenticado cria igreja" on public.churches
  for insert with check (auth.uid() is not null and created_by = auth.uid());

create policy "administrador edita a igreja" on public.churches
  for update using (public.is_church_admin(id)) with check (public.is_church_admin(id));

create policy "administrador apaga a igreja" on public.churches
  for delete using (public.church_role_of(id) = 'admin');

-- -------------------------------------------------------------- membros
create policy "vê os membros da própria igreja" on public.church_members
  for select using (user_id = auth.uid() or public.is_church_member(church_id));

-- Entrar sozinho (pelo código de convite) ou ser adicionado por um líder.
create policy "entra na igreja ou é adicionado" on public.church_members
  for insert with check (user_id = auth.uid() or public.is_church_admin(church_id));

create policy "líder muda papéis" on public.church_members
  for update using (public.is_church_admin(church_id))
  with check (public.is_church_admin(church_id));

create policy "sai da igreja ou é removido" on public.church_members
  for delete using (user_id = auth.uid() or public.is_church_admin(church_id));

-- ------------------------------------------- conteúdo ligado à igreja
do $$
declare
  t text;
begin
  foreach t in array array['artists', 'categories', 'songs', 'repertoires']
  loop
    execute format($f$
      create policy "lê o conteúdo da própria igreja" on public.%I
        for select using (public.is_church_member(church_id));

      create policy "cria conteúdo na própria igreja" on public.%I
        for insert with check (public.can_edit_content(church_id));

      create policy "edita o conteúdo da própria igreja" on public.%I
        for update using (public.can_edit_content(church_id))
        with check (public.can_edit_content(church_id));

      create policy "apaga o conteúdo da própria igreja" on public.%I
        for delete using (public.can_edit_content(church_id));
    $f$, t, t, t, t);
  end loop;
end
$$;

-- ------------------------------------------- tabelas filhas de músicas
do $$
declare
  t text;
begin
  foreach t in array array['song_categories', 'song_sections', 'chords', 'song_versions']
  loop
    execute format($f$
      create policy "lê o que pertence às músicas da igreja" on public.%I
        for select using (public.is_church_member(public.church_of_song(song_id)));

      create policy "escreve o que pertence às músicas da igreja" on public.%I
        for all using (public.can_edit_content(public.church_of_song(song_id)))
        with check (public.can_edit_content(public.church_of_song(song_id)));
    $f$, t, t);
  end loop;
end
$$;

-- song_lines chega pela seção, não pela música
create policy "lê as linhas das músicas da igreja" on public.song_lines
  for select using (
    public.is_church_member(
      public.church_of_song((select s.song_id from public.song_sections s where s.id = section_id))
    )
  );

create policy "escreve as linhas das músicas da igreja" on public.song_lines
  for all using (
    public.can_edit_content(
      public.church_of_song((select s.song_id from public.song_sections s where s.id = section_id))
    )
  )
  with check (
    public.can_edit_content(
      public.church_of_song((select s.song_id from public.song_sections s where s.id = section_id))
    )
  );

-- repertoire_songs chega pelo repertório
create policy "lê os itens dos repertórios da igreja" on public.repertoire_songs
  for select using (
    public.is_church_member(
      (select r.church_id from public.repertoires r where r.id = repertoire_id)
    )
  );

create policy "escreve os itens dos repertórios da igreja" on public.repertoire_songs
  for all using (
    public.can_edit_content(
      (select r.church_id from public.repertoires r where r.id = repertoire_id)
    )
  )
  with check (
    public.can_edit_content(
      (select r.church_id from public.repertoires r where r.id = repertoire_id)
    )
  );

-- ------------------------------------------------------ dados pessoais
create policy "só os próprios favoritos" on public.favorites
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());

create policy "só o próprio histórico" on public.song_history
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());

create policy "só as próprias preferências" on public.user_settings
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());

-- ------------------------------------------------------------- logs
create policy "líder lê os logs da igreja" on public.system_logs
  for select using (public.is_church_admin(church_id));

create policy "membro registra log na própria igreja" on public.system_logs
  for insert with check (public.is_church_member(church_id) and user_id = auth.uid());
