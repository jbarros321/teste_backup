/** Categorias (Adoração, Louvor, Santa Ceia…). Cada igreja tem as suas. */
import { requireSupabase } from '@/lib/supabase'
import type { Category } from '@/types/database'

import { slugify } from './songsService'

export const categoriesService = {
  async list(churchId: string): Promise<Category[]> {
    const { data, error } = await requireSupabase()
      .from('categories')
      .select('*')
      .eq('church_id', churchId)
      .order('sort_order', { ascending: true })

    if (error) throw new Error(error.message)
    return data ?? []
  },

  async create(churchId: string, name: string): Promise<Category> {
    const { data, error } = await requireSupabase()
      .from('categories')
      .insert({ church_id: churchId, name: name.trim(), slug: slugify(name), sort_order: 999 })
      .select('*')
      .single()

    if (error) throw new Error(error.message)
    return data as Category
  },

  async remove(id: string): Promise<void> {
    const { error } = await requireSupabase().from('categories').delete().eq('id', id)
    if (error) throw new Error(error.message)
  },

  /** Regrava as categorias de uma música. */
  async setForSong(songId: string, categoryIds: string[]): Promise<void> {
    const client = requireSupabase()
    await client.from('song_categories').delete().eq('song_id', songId)
    if (categoryIds.length === 0) return

    const { error } = await client
      .from('song_categories')
      .insert(categoryIds.map((categoryId) => ({ song_id: songId, category_id: categoryId })))
    if (error) throw new Error(error.message)
  },
}
