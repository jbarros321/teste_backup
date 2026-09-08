/**
 * MusicBrainz — catálogo aberto de metadados musicais (licença CC0).
 *
 * Traz título, artista, álbum, ano e duração. NÃO traz letra nem cifra, e é
 * exatamente por isso que podemos consultá-lo livremente.
 *
 * A API pede no máximo uma requisição por segundo; como a busca é disparada
 * pelo usuário e com debounce, ficamos dentro do limite.
 */
import type { MetadataProvider, SongMetadata } from './types'

const ENDPOINT = 'https://musicbrainz.org/ws/2/recording'

interface MusicBrainzRecording {
  id: string
  title: string
  length?: number
  'artist-credit'?: Array<{ name?: string }>
  releases?: Array<{ title?: string; date?: string }>
}

export const musicBrainzProvider: MetadataProvider = {
  name: 'MusicBrainz',
  attribution: 'Dados de MusicBrainz (CC0)',

  async search(query: string, signal?: AbortSignal): Promise<SongMetadata[]> {
    const trimmed = query.trim()
    if (trimmed.length < 3) return []

    const url = new URL(ENDPOINT)
    url.searchParams.set('query', trimmed)
    url.searchParams.set('fmt', 'json')
    url.searchParams.set('limit', '8')

    const response = await fetch(url, { signal, headers: { Accept: 'application/json' } })
    if (!response.ok) {
      throw new Error(
        response.status === 503
          ? 'O MusicBrainz está ocupado. Tente de novo em alguns segundos.'
          : `Falha ao consultar o MusicBrainz (${response.status}).`,
      )
    }

    const data = (await response.json()) as { recordings?: MusicBrainzRecording[] }
    return (data.recordings ?? []).map(toMetadata)
  },
}

function toMetadata(recording: MusicBrainzRecording): SongMetadata {
  const release = recording.releases?.[0]
  const year = release?.date ? Number.parseInt(release.date.slice(0, 4), 10) : Number.NaN

  return {
    title: recording.title,
    artist: recording['artist-credit']?.[0]?.name ?? null,
    album: release?.title ?? null,
    year: Number.isFinite(year) ? year : null,
    durationSeconds: recording.length ? Math.round(recording.length / 1000) : null,
    externalId: recording.id,
    provider: 'musicbrainz',
  }
}
