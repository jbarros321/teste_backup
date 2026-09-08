/** Artistas do acervo da igreja. */
import { requireSupabase } from '@/lib/supabase'
import type { Artist } from '@/types/database'

import { slugify } from './songsService'

export const artistsService = {
  async list(churchId: string): Promise<Artist[]> {
    const { data, error } = await requireSupabase()
      .from('artists')
      .select('*')
      .eq('church_id', churchId)
      .order('name', { ascending: true })

    if (error) throw new Error(error.message)
    return data ?? []
  },

  /**
   * Devolve o artista com este nome, criando se ainda não existir.
   * Evita que "Ministério Zoe" e "ministério zoe" virem dois cadastros.
   */
  async findOrCreate(churchId: string, name: string): Promise<Artist | null> {
    const trimmed = name.trim()
    if (trimmed.length === 0) return null

    const client = requireSupabase()
    const slug = slugify(trimmed)

    const { data: existing } = await client
      .from('artists')
      .select('*')
      .eq('church_id', churchId)
      .eq('slug', slug)
      .maybeSingle()

    if (existing) return existing as Artist

    const { data, error } = await client
      .from('artists')
      .insert({ church_id: churchId, name: trimmed, slug })
      .select('*')
      .single()

    if (error) throw new Error(error.message)
    return data as Artist
  },
}
