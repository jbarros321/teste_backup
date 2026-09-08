import { FileUp, Library, Music2, Plus, Search } from 'lucide-react'
import { useDeferredValue, useState } from 'react'
import { Link } from 'react-router-dom'

import { Alert } from '@/components/ui/alert'
import { Button, buttonVariants } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import {
  useImportPublicDomainLibrary,
  useIsDemo,
  useSongList,
} from '@/features/songs/songQueries'
import type { SongListItem } from '@/features/songs/songSource'

export default function HomePage() {
  const [query, setQuery] = useState('')
  // O usuário digita rápido; a lista pode ficar um frame atrás sem travar o campo.
  const deferredQuery = useDeferredValue(query)
  const isDemo = useIsDemo()
  const { data: songs, isPending, isError, error } = useSongList(deferredQuery)

  return (
    <section className="space-y-5">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Minhas músicas</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Pesquise por título ou artista e abra a cifra.
          </p>
        </div>
        {!isDemo && (
          <div className="flex gap-2">
            <Link to="/importar" className={buttonVariants({ variant: 'outline' })}>
              <FileUp aria-hidden />
              Importar
            </Link>
            <Link to="/musica/nova" className={buttonVariants()}>
              <Plus aria-hidden />
              Nova música
            </Link>
          </div>
        )}
      </div>

      <div className="relative">
        <Search
          className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
          aria-hidden
        />
        <Input
          type="search"
          className="h-11 pl-9"
          placeholder="Pesquisar música ou artista…"
          aria-label="Pesquisar música"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
      </div>

      {isDemo && (
        <Alert>
          Modo demonstração: este é o acervo de exemplo em domínio público. Configure o Supabase
          (veja <code>SETUP.md</code>) para cadastrar as músicas do seu ministério.
        </Alert>
      )}

      {isError && (
        <Alert variant="destructive">
          {error instanceof Error ? error.message : 'Não foi possível carregar as músicas.'}
        </Alert>
      )}

      {isPending ? (
        <p className="py-8 text-center text-sm text-muted-foreground" role="status">
          Carregando…
        </p>
      ) : songs && songs.length > 0 ? (
        <ul className="grid gap-3 sm:grid-cols-2">
          {songs.map((song) => (
            <li key={song.slug}>
              <SongCard song={song} />
            </li>
          ))}
        </ul>
      ) : (
        <EmptyState hasQuery={deferredQuery.trim().length > 0} isDemo={isDemo} />
      )}
    </section>
  )
}

function SongCard({ song }: { song: SongListItem }) {
  return (
    <Link to={`/musica/${song.slug}`} className="block rounded-xl focus-visible:outline-none">
      <Card className="transition-colors hover:border-primary/60 hover:bg-accent/40">
        <CardContent className="flex items-center gap-3 p-4">
          <span className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-accent text-accent-foreground">
            <Music2 className="size-5" aria-hidden />
          </span>
          <span className="min-w-0 flex-1">
            <span className="block truncate font-medium">{song.title}</span>
            <span className="block truncate text-sm text-muted-foreground">
              {song.artist ?? 'Sem artista'}
            </span>
          </span>
          <span className="shrink-0 text-right text-xs text-muted-foreground">
            <span className="block text-sm font-semibold text-foreground">{song.key ?? '—'}</span>
            {song.bpm !== null && <span className="block">{song.bpm} BPM</span>}
          </span>
        </CardContent>
      </Card>
    </Link>
  )
}

function EmptyState({ hasQuery, isDemo }: { hasQuery: boolean; isDemo: boolean }) {
  const importLibrary = useImportPublicDomainLibrary()

  if (hasQuery) {
    return (
      <p className="py-10 text-center text-sm text-muted-foreground">
        Nenhuma música encontrada. Tente outro trecho do título ou do artista.
      </p>
    )
  }

  return (
    <div className="space-y-6 py-10 text-center">
      <p className="text-sm text-muted-foreground">O acervo ainda está vazio.</p>

      {!isDemo && (
        <>
          <div className="flex flex-wrap justify-center gap-2">
            <Link to="/musica/nova" className={buttonVariants()}>
              <Plus aria-hidden />
              Cadastrar a primeira música
            </Link>
            <Link to="/importar" className={buttonVariants({ variant: 'outline' })}>
              <FileUp aria-hidden />
              Importar cifra colada
            </Link>
          </div>

          <div className="mx-auto max-w-md space-y-2 rounded-xl border border-dashed border-border p-5">
            <p className="text-sm font-medium">Começar com hinos em domínio público</p>
            <p className="text-xs text-muted-foreground">
              Adiciona ao acervo da sua igreja hinos cujo texto é de domínio público, já cifrados.
              Confira a letra com o seu hinário — traduções variam entre edições.
            </p>
            <Button
              type="button"
              variant="secondary"
              size="sm"
              disabled={importLibrary.isPending}
              onClick={() => importLibrary.mutate()}
            >
              <Library aria-hidden />
              {importLibrary.isPending ? 'Importando…' : 'Importar acervo'}
            </Button>
            {importLibrary.isSuccess && (
              <p className="text-xs text-muted-foreground">
                {importLibrary.data.imported} importada(s)
                {importLibrary.data.skipped > 0 && `, ${importLibrary.data.skipped} já existia(m)`}.
              </p>
            )}
            {importLibrary.isError && (
              <p className="text-xs text-destructive">
                {importLibrary.error instanceof Error
                  ? importLibrary.error.message
                  : 'Falha ao importar.'}
              </p>
            )}
          </div>
        </>
      )}
    </div>
  )
}
