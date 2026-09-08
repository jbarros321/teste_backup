/**
 * Fonte das músicas.
 *
 * Uma única porta para as telas, que não precisam saber se os dados vêm do
 * Supabase ou do acervo de demonstração. É também o ponto onde entram, no
 * futuro, outros provedores (§54 do escopo: `SongProvider`).
 */
import { resolveSongKey } from '@/lib/chordpro/transposeSong'
import type { SongDocument } from '@/lib/chordpro/types'
import { isSupabaseConfigured } from '@/lib/supabase'
import { songsService } from '@/services/songsService'

import { SAMPLE_SONGS, findSampleSong } from './sampleSongs'

/** O que a lista precisa mostrar de cada música. */
export interface SongListItem {
  /** Ausente no acervo de demonstração. */
  id?: string
  slug: string
  title: string
  artist: string | null
  key: string | null
  bpm: number | null
  tags: string[]
}

export interface SongDetail extends SongListItem {
  document: SongDocument
  chordpro: string
  timeSignature: string | null
  capo: number | null
  artistId?: string | null
}

/** Modo demonstração: sem Supabase configurado ou sem igreja ativa. */
export function isDemoSource(churchId: string | null): boolean {
  return !isSupabaseConfigured || churchId === null
}

function fromSample(slug: string): SongDetail | null {
  const sample = findSampleSong(slug)
  if (!sample) return null

  const { document } = sample
  const { key } = resolveSongKey(document)

  return {
    slug,
    title: document.title ?? 'Sem título',
    artist: document.artist ?? null,
    key,
    bpm: document.bpm ?? null,
    tags: [sample.category],
    document,
    chordpro: '',
    timeSignature: document.timeSignature ?? null,
    capo: document.capo ?? null,
  }
}

/** Busca no acervo de demonstração, por título ou artista. */
function searchSamples(query: string): SongListItem[] {
  const needle = query.trim().toLowerCase()

  return SAMPLE_SONGS.map(({ slug }) => fromSample(slug)!)
    .filter(
      (song) =>
        needle.length === 0 ||
        song.title.toLowerCase().includes(needle) ||
        (song.artist ?? '').toLowerCase().includes(needle),
    )
    .sort((a, b) => a.title.localeCompare(b.title, 'pt-BR'))
}

export const songSource = {
  async list(churchId: string | null, query: string): Promise<SongListItem[]> {
    if (isDemoSource(churchId)) return searchSamples(query)

    const songs =
      query.trim().length > 0
        ? await songsService.search(churchId!, query)
        : await songsService.list(churchId!)

    return songs.map((song) => ({
      id: song.id,
      slug: song.slug,
      title: song.title,
      artist: song.artist?.name ?? null,
      key: song.original_key,
      bpm: song.bpm,
      tags: song.tags,
    }))
  },

  async get(churchId: string | null, slug: string): Promise<SongDetail | null> {
    if (isDemoSource(churchId)) return fromSample(slug)

    const song = await songsService.getBySlug(churchId!, slug)
    if (!song) return null

    return {
      id: song.id,
      slug: song.slug,
      title: song.title,
      artist: song.artist?.name ?? null,
      artistId: song.artist_id,
      key: song.original_key,
      bpm: song.bpm,
      tags: song.tags,
      document: song.document,
      chordpro: song.chordpro_content,
      timeSignature: song.time_signature,
      capo: song.capo,
    }
  },
}
