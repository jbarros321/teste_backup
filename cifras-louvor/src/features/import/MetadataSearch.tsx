import { Search } from 'lucide-react'
import { useEffect, useState } from 'react'

import { Alert } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { musicBrainzProvider } from '@/lib/providers/musicbrainz'
import type { SongMetadata } from '@/lib/providers/types'

/**
 * Busca metadados numa base aberta para preencher título e artista.
 *
 * Metadado não é obra protegida — título, artista e álbum são fatos. Letra e
 * cifra continuam vindo só do que o usuário traz.
 */
export function MetadataSearch({ onPick }: { onPick: (metadata: SongMetadata) => void }) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<SongMetadata[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  // Debounce: o MusicBrainz pede no máximo uma consulta por segundo.
  useEffect(() => {
    if (query.trim().length < 3) {
      setResults(null)
      return
    }

    const controller = new AbortController()
    const timer = setTimeout(() => {
      setBusy(true)
      setError(null)
      musicBrainzProvider
        .search(query, controller.signal)
        .then(setResults)
        .catch((cause: unknown) => {
          if (controller.signal.aborted) return
          setError(cause instanceof Error ? cause.message : 'Falha na busca.')
        })
        .finally(() => setBusy(false))
    }, 600)

    return () => {
      clearTimeout(timer)
      controller.abort()
    }
  }, [query])

  return (
    <div className="space-y-2">
      <Label htmlFor="metadata-search">Buscar dados da música</Label>
      <div className="relative">
        <Search
          className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
          aria-hidden
        />
        <Input
          id="metadata-search"
          className="pl-9"
          placeholder="Nome da música e do artista"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
      </div>

      {error && <Alert variant="destructive">{error}</Alert>}

      {busy && <p className="text-sm text-muted-foreground">Consultando…</p>}

      {results !== null && !busy && (
        results.length === 0 ? (
          <p className="text-sm text-muted-foreground">Nada encontrado nessa base.</p>
        ) : (
          <>
            <ul className="divide-y divide-border rounded-md border border-border">
              {results.map((item) => (
                <li key={item.externalId}>
                  <Button
                    type="button"
                    variant="ghost"
                    className="h-auto w-full justify-start rounded-none px-3 py-2 text-left"
                    onClick={() => onPick(item)}
                  >
                    <span className="min-w-0">
                      <span className="block truncate font-medium">{item.title}</span>
                      <span className="block truncate text-xs text-muted-foreground">
                        {[item.artist, item.album, item.year].filter(Boolean).join(' · ')}
                      </span>
                    </span>
                  </Button>
                </li>
              ))}
            </ul>
            <p className="text-xs text-muted-foreground">
              {musicBrainzProvider.attribution}. Só metadados — letra e cifra continuam vindo de
              você.
            </p>
          </>
        )
      )}
    </div>
  )
}
