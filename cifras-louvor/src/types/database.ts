/** Tipos das tabelas do Supabase, escritos à mão e alinhados às migrations. */

export type ChurchRole = 'admin' | 'leader' | 'musician' | 'viewer'
export type SongStatus = 'draft' | 'published' | 'archived'
export type SectionTypeRow =
  | 'intro'
  | 'verse'
  | 'pre_chorus'
  | 'chorus'
  | 'bridge'
  | 'solo'
  | 'interlude'
  | 'outro'
  | 'tab'
  | 'none'

export interface Profile {
  id: string
  email: string
  name: string | null
  avatar_url: string | null
  created_at: string
}

export interface Church {
  id: string
  name: string
  slug: string
  logo_url: string | null
  invite_code: string
  created_by: string | null
  created_at: string
}

export interface ChurchMember {
  id: string
  church_id: string
  user_id: string
  role: ChurchRole
  created_at: string
}

export interface Artist {
  id: string
  church_id: string
  name: string
  slug: string
  created_at: string
}

export interface Category {
  id: string
  church_id: string
  name: string
  slug: string
  sort_order: number
}

export interface SongRow {
  id: string
  church_id: string
  title: string
  slug: string
  artist_id: string | null
  original_key: string | null
  bpm: number | null
  time_signature: string | null
  capo: number | null
  chordpro_content: string
  lyrics: string
  tags: string[]
  status: SongStatus
  source: string | null
  created_by: string | null
  created_at: string
  updated_at: string
}

export interface Repertoire {
  id: string
  church_id: string
  name: string
  description: string | null
  scheduled_for: string | null
  created_by: string | null
  created_at: string
}

export interface RepertoireSong {
  id: string
  repertoire_id: string
  song_id: string
  order_index: number
  custom_key: string | null
  custom_bpm: number | null
  notes: string | null
}

export interface Favorite {
  id: string
  user_id: string
  song_id: string
  created_at: string
}

export interface UserSettingsRow {
  user_id: string
  theme: string
  font_size: number
  chord_scale: number
  accidentals: string
  autoscroll_speed: number
  metronome_volume: number
  default_bpm: number
  default_key: string | null
  updated_at: string
}
