/**
 * Músicas.
 *
 * O ChordPro é a fonte da verdade: guardamos o texto e derivamos dele a letra
 * plana (para busca) e a lista de acordes (para consulta por acorde). Nunca
 * guardamos o resultado visual.
 */
import { parseChordPro } from '@/lib/chordpro/parser'
import { serializeChordPro } from '@/lib/chordpro/serializer'
import { collectChords, plainLyrics, type SongDocument } from '@/lib/chordpro/types'
import { resolveSongKey } from '@/lib/chordpro/transposeSong'
import { normalizedChordName, parseChord } from '@/lib/music/chord'
import { requireSupabase } from '@/lib/supabase'
import type { SongRow } from '@/types/database'

export interface Song extends SongRow {
  /** Documento já interpretado, pronto para renderizar. */
  document: SongDocument
  artist?: { id: string; name: string } | null
}

export interface SongInput {
  churchId: string
  chordpro: string
  /** Sobrescrevem o que estiver nas diretivas do ChordPro. */
  title?: string
  artistId?: string | null
  bpm?: number | null
  status?: SongRow['status']
  source?: string | null
  tags?: string[]
}

const SELECT = '*, artist:artists(id, name)'

export const songsService = {
  async list(churchId: string, limit = 60): Promise<Song[]> {
    const { data, error } = await requireSupabase()
      .from('songs')
      .select(SELECT)
      .eq('church_id', churchId)
      .neq('status', 'archived')
      .order('title', { ascending: true })
      .limit(limit)

    if (error) throw new Error(error.message)
    return (data ?? []).map(hydrate)
  },

  async search(churchId: string, query: string, limit = 30): Promise<Song[]> {
    const { data, error } = await requireSupabase().rpc('search_songs', {
      p_church_id: churchId,
      p_query: query,
      p_limit: limit,
    })

    if (error) throw new Error(error.message)
    return ((data ?? []) as SongRow[]).map(hydrate)
  },

  async getBySlug(churchId: string, slug: string): Promise<Song | null> {
    const { data, error } = await requireSupabase()
      .from('songs')
      .select(SELECT)
      .eq('church_id', churchId)
      .eq('slug', slug)
      .maybeSingle()

    if (error) throw new Error(error.message)
    return data ? hydrate(data) : null
  },

  async create(input: SongInput): Promise<Song> {
    const payload = buildPayload(input)
    const { data, error } = await requireSupabase()
      .from('songs')
      .insert({ ...payload, church_id: input.churchId })
      .select(SELECT)
      .single()

    if (error) throw new Error(error.message)
    const song = hydrate(data)
    await syncChords(song)
    return song
  },

  async update(id: string, input: SongInput): Promise<Song> {
    const payload = buildPayload(input)
    const { data, error } = await requireSupabase()
      .from('songs')
      .update(payload)
      .eq('id', id)
      .select(SELECT)
      .single()

    if (error) throw new Error(error.message)
    const song = hydrate(data)
    await syncChords(song)
    return song
  },

  /** Arquiva em vez de apagar: repertórios passados continuam fazendo sentido. */
  async archive(id: string): Promise<void> {
    const { error } = await requireSupabase()
      .from('songs')
      .update({ status: 'archived' })
      .eq('id', id)
    if (error) throw new Error(error.message)
  },

  async remove(id: string): Promise<void> {
    const { error } = await requireSupabase().from('songs').delete().eq('id', id)
    if (error) throw new Error(error.message)
  },
}

/** Monta as colunas derivadas do ChordPro. */
function buildPayload(input: SongInput) {
  const document = parseChordPro(input.chordpro)
  const title = input.title?.trim() || document.title?.trim() || 'Sem título'
  const { key } = resolveSongKey(document)

  return {
    title,
    slug: slugify(title),
    artist_id: input.artistId ?? null,
    original_key: key,
    bpm: input.bpm ?? document.bpm ?? null,
    time_signature: document.timeSignature ?? '4/4',
    capo: document.capo ?? null,
    chordpro_content: serializeChordPro(document),
    lyrics: plainLyrics(document),
    tags: input.tags ?? [],
    status: input.status ?? 'published',
    source: input.source ?? null,
  }
}

/**
 * Regrava a lista de acordes da música. Guardamos a grafia canônica junto da
 * escrita para que uma busca por "C#7" também ache quem escreveu "Db7".
 */
async function syncChords(song: Song): Promise<void> {
  const client = requireSupabase()
  await client.from('chords').delete().eq('song_id', song.id)

  const unique = new Map<string, ReturnType<typeof parseChord>>()
  for (const raw of collectChords(song.document)) {
    const chord = parseChord(raw)
    if (chord && !unique.has(raw)) unique.set(raw, chord)
  }

  if (unique.size === 0) return

  const rows = [...unique.entries()].map(([name, chord]) => ({
    song_id: song.id,
    name,
    normalized_name: normalizedChordName(chord!),
    root: chord!.root,
    quality: chord!.suffix,
    bass: chord!.bass ?? null,
  }))

  await client.from('chords').insert(rows)
}

function hydrate(row: unknown): Song {
  const song = row as SongRow & { artist?: { id: string; name: string } | null }
  return { ...song, document: parseChordPro(song.chordpro_content) }
}

/** Mesmo algoritmo do `slugify` do banco, para o slug não divergir. */
export function slugify(text: string): string {
  const normalized = text
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
  return normalized || 'sem-titulo'
}
