-- =====================================================================
-- Cifra Ministério — schema base
-- Tudo que é conteúdo pertence a uma igreja (church_id). O isolamento
-- entre igrejas é garantido pelas policies em 0002_rls.sql.
-- =====================================================================

create extension if not exists "pgcrypto";
create extension if not exists "pg_trgm";
create extension if not exists "unaccent";

-- ---------------------------------------------------------------------
-- Pessoas e igrejas
-- ---------------------------------------------------------------------

create table public.profiles (
  id          uuid primary key references auth.users (id) on delete cascade,
  email       text not null,
  name        text,
  avatar_url  text,
  created_at  timestamptz not null default now()
);

create table public.churches (
  id          uuid primary key default gen_random_uuid(),
  name        text not null,
  slug        text not null unique,
  logo_url    text,
  -- Código curto que um músico digita para entrar na igreja.
  invite_code text not null unique default upper(substr(encode(gen_random_bytes(6), 'hex'), 1, 8)),
  created_by  uuid references public.profiles (id) on delete set null,
  created_at  timestamptz not null default now()
);

create type public.church_role as enum ('admin', 'leader', 'musician', 'viewer');

create table public.church_members (
  id         uuid primary key default gen_random_uuid(),
  church_id  uuid not null references public.churches (id) on delete cascade,
  user_id    uuid not null references public.profiles (id) on delete cascade,
  role       public.church_role not null default 'musician',
  created_at timestamptz not null default now(),
  unique (church_id, user_id)
);

create index church_members_user_id_idx on public.church_members (user_id);
create index church_members_church_id_idx on public.church_members (church_id);

-- ---------------------------------------------------------------------
-- Acervo musical
-- ---------------------------------------------------------------------

create table public.artists (
  id         uuid primary key default gen_random_uuid(),
  church_id  uuid not null references public.churches (id) on delete cascade,
  name       text not null,
  slug       text not null,
  created_at timestamptz not null default now(),
  unique (church_id, slug)
);

create index artists_name_trgm_idx on public.artists using gin (name gin_trgm_ops);

create table public.categories (
  id         uuid primary key default gen_random_uuid(),
  church_id  uuid not null references public.churches (id) on delete cascade,
  name       text not null,
  slug       text not null,
  sort_order integer not null default 0,
  unique (church_id, slug)
);

create type public.song_status as enum ('draft', 'published', 'archived');

create table public.songs (
  id             uuid primary key default gen_random_uuid(),
  church_id      uuid not null references public.churches (id) on delete cascade,
  title          text not null,
  slug           text not null,
  artist_id      uuid references public.artists (id) on delete set null,
  original_key   text,
  bpm            integer check (bpm is null or bpm between 20 and 400),
  time_signature text default '4/4',
  capo           integer check (capo is null or capo between 0 and 12),
  -- Fonte da verdade textual da música. NUNCA guardamos HTML renderizado.
  chordpro_content text not null default '',
  -- Letra sem acordes, mantida só para busca e leitura rápida.
  lyrics         text not null default '',
  tags           text[] not null default '{}',
  status         public.song_status not null default 'published',
  source         text,
  created_by     uuid references public.profiles (id) on delete set null,
  created_at     timestamptz not null default now(),
  updated_at     timestamptz not null default now(),
  search_vector  tsvector generated always as (
    setweight(to_tsvector('portuguese', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('portuguese', coalesce(lyrics, '')), 'B')
  ) stored,
  unique (church_id, slug)
);

create index songs_church_id_idx on public.songs (church_id);
create index songs_title_trgm_idx on public.songs using gin (title gin_trgm_ops);
create index songs_search_idx on public.songs using gin (search_vector);
create index songs_updated_at_idx on public.songs (church_id, updated_at desc);

create table public.song_categories (
  song_id     uuid not null references public.songs (id) on delete cascade,
  category_id uuid not null references public.categories (id) on delete cascade,
  primary key (song_id, category_id)
);

-- Estrutura normalizada da música. A renderização usa o ChordPro; estas
-- tabelas existem para edição estruturada e consultas por acorde (M4+).
create type public.section_type as enum (
  'intro', 'verse', 'pre_chorus', 'chorus', 'bridge',
  'solo', 'interlude', 'outro', 'tab', 'none'
);

create table public.song_sections (
  id           uuid primary key default gen_random_uuid(),
  song_id      uuid not null references public.songs (id) on delete cascade,
  section_type public.section_type not null default 'none',
  name         text,
  order_index  integer not null default 0
);

create index song_sections_song_id_idx on public.song_sections (song_id, order_index);

create table public.song_lines (
  id          uuid primary key default gen_random_uuid(),
  section_id  uuid not null references public.song_sections (id) on delete cascade,
  order_index integer not null default 0,
  content     text not null default ''
);

create index song_lines_section_id_idx on public.song_lines (section_id, order_index);

create table public.chords (
  id              uuid primary key default gen_random_uuid(),
  song_id         uuid not null references public.songs (id) on delete cascade,
  name            text not null,
  -- Grafia canônica em sustenidos, para que Db7 e C#7 sejam o mesmo acorde na busca.
  normalized_name text not null,
  root            text not null,
  quality         text not null default '',
  bass            text,
  position_data   jsonb
);

create index chords_song_id_idx on public.chords (song_id);
create index chords_normalized_idx on public.chords (normalized_name);

create table public.song_versions (
  id           uuid primary key default gen_random_uuid(),
  song_id      uuid not null references public.songs (id) on delete cascade,
  version_name text not null,
  content      text not null default '',
  key          text,
  bpm          integer,
  notes        text,
  created_by   uuid references public.profiles (id) on delete set null,
  created_at   timestamptz not null default now()
);

create index song_versions_song_id_idx on public.song_versions (song_id);

-- ---------------------------------------------------------------------
-- Repertórios
-- ---------------------------------------------------------------------

create table public.repertoires (
  id            uuid primary key default gen_random_uuid(),
  church_id     uuid not null references public.churches (id) on delete cascade,
  name          text not null,
  description   text,
  scheduled_for date,
  created_by    uuid references public.profiles (id) on delete set null,
  created_at    timestamptz not null default now()
);

create index repertoires_church_id_idx on public.repertoires (church_id, scheduled_for desc);

create table public.repertoire_songs (
  id            uuid primary key default gen_random_uuid(),
  repertoire_id uuid not null references public.repertoires (id) on delete cascade,
  song_id       uuid not null references public.songs (id) on delete cascade,
  order_index   integer not null default 0,
  -- Tom e BPM só deste culto; o padrão da música continua intacto.
  custom_key    text,
  custom_bpm    integer,
  notes         text
);

create index repertoire_songs_repertoire_id_idx
  on public.repertoire_songs (repertoire_id, order_index);

-- ---------------------------------------------------------------------
-- Dados por usuário
-- ---------------------------------------------------------------------

create table public.favorites (
  id         uuid primary key default gen_random_uuid(),
  user_id    uuid not null references public.profiles (id) on delete cascade,
  song_id    uuid not null references public.songs (id) on delete cascade,
  created_at timestamptz not null default now(),
  unique (user_id, song_id)
);

create index favorites_user_id_idx on public.favorites (user_id);

create table public.song_history (
  id        uuid primary key default gen_random_uuid(),
  user_id   uuid not null references public.profiles (id) on delete cascade,
  song_id   uuid not null references public.songs (id) on delete cascade,
  played_at timestamptz not null default now()
);

create index song_history_user_id_idx on public.song_history (user_id, played_at desc);

create table public.user_settings (
  user_id           uuid primary key references public.profiles (id) on delete cascade,
  theme             text not null default 'system',
  font_size         numeric(4, 3) not null default 1.0,
  chord_scale       numeric(4, 3) not null default 0.85,
  accidentals       text not null default 'auto',
  autoscroll_speed  integer not null default 20,
  metronome_volume  numeric(4, 3) not null default 0.7,
  default_bpm       integer not null default 80,
  default_key       text,
  updated_at        timestamptz not null default now()
);

create table public.system_logs (
  id         uuid primary key default gen_random_uuid(),
  church_id  uuid references public.churches (id) on delete cascade,
  user_id    uuid references public.profiles (id) on delete set null,
  event      text not null,
  level      text not null default 'info',
  payload    jsonb,
  created_at timestamptz not null default now()
);

create index system_logs_church_id_idx on public.system_logs (church_id, created_at desc);
