/** Hooks de dados das músicas. As telas usam só isto. */
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { useAuth } from '@/features/auth/AuthProvider'
import { artistsService } from '@/services/artistsService'
import { categoriesService } from '@/services/categoriesService'
import { songsService, type SongInput } from '@/services/songsService'

import { PUBLIC_DOMAIN_LIBRARY } from './sampleSongs'
import { isDemoSource, songSource } from './songSource'

/** Igreja ativa, ou `null` em modo demonstração. */
export function useChurchId(): string | null {
  const { activeChurch } = useAuth()
  return activeChurch?.church.id ?? null
}

export function useIsDemo(): boolean {
  return isDemoSource(useChurchId())
}

const keys = {
  songs: (churchId: string | null, query: string) => ['songs', churchId, query] as const,
  song: (churchId: string | null, slug: string) => ['song', churchId, slug] as const,
  artists: (churchId: string | null) => ['artists', churchId] as const,
  categories: (churchId: string | null) => ['categories', churchId] as const,
}

export function useSongList(query: string) {
  const churchId = useChurchId()
  return useQuery({
    queryKey: keys.songs(churchId, query),
    queryFn: () => songSource.list(churchId, query),
  })
}

export function useSong(slug: string) {
  const churchId = useChurchId()
  return useQuery({
    queryKey: keys.song(churchId, slug),
    queryFn: () => songSource.get(churchId, slug),
    enabled: slug.length > 0,
  })
}

export function useArtists() {
  const churchId = useChurchId()
  return useQuery({
    queryKey: keys.artists(churchId),
    queryFn: () => artistsService.list(churchId!),
    enabled: churchId !== null,
  })
}

export function useCategories() {
  const churchId = useChurchId()
  return useQuery({
    queryKey: keys.categories(churchId),
    queryFn: () => categoriesService.list(churchId!),
    enabled: churchId !== null,
  })
}

export interface SaveSongInput extends Omit<SongInput, 'churchId'> {
  /** Quando presente, atualiza; quando ausente, cria. */
  id?: string
  artistName?: string
  categoryIds?: string[]
}

export function useSaveSong() {
  const churchId = useChurchId()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (input: SaveSongInput) => {
      if (churchId === null) {
        throw new Error('Configure o Supabase para cadastrar músicas.')
      }

      // O artista é criado sob demanda: o usuário digita o nome, não escolhe id.
      const artist = input.artistName
        ? await artistsService.findOrCreate(churchId, input.artistName)
        : null

      const payload: SongInput = { ...input, churchId, artistId: artist?.id ?? null }
      const song = input.id
        ? await songsService.update(input.id, payload)
        : await songsService.create(payload)

      if (input.categoryIds) await categoriesService.setForSong(song.id, input.categoryIds)
      return song
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['songs'] })
      void queryClient.invalidateQueries({ queryKey: ['song'] })
      void queryClient.invalidateQueries({ queryKey: keys.artists(churchId) })
    },
  })
}

export function useArchiveSong() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => songsService.archive(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['songs'] })
    },
  })
}

export interface LibraryImportResult {
  imported: number
  skipped: number
}

/**
 * Importa o acervo de domínio público para a igreja ativa.
 *
 * Pula o que já existe (mesmo slug), então pode ser executado de novo sem
 * duplicar nada.
 */
export function useImportPublicDomainLibrary() {
  const churchId = useChurchId()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (): Promise<LibraryImportResult> => {
      if (churchId === null) {
        throw new Error('Configure o Supabase para importar o acervo.')
      }

      const existing = await songSource.list(churchId, '')
      const known = new Set(existing.map((song) => song.slug))

      let imported = 0
      let skipped = 0

      for (const item of PUBLIC_DOMAIN_LIBRARY) {
        if (known.has(item.slug)) {
          skipped += 1
          continue
        }
        await songsService.create({
          churchId,
          chordpro: item.chordpro,
          source: 'Domínio público',
          tags: [item.category],
        })
        imported += 1
      }

      return { imported, skipped }
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['songs'] })
    },
  })
}
