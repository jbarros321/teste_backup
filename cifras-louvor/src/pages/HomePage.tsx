import { Music2 } from 'lucide-react'
import { Link } from 'react-router-dom'

import { Card, CardContent } from '@/components/ui/card'
import { SAMPLE_SONGS } from '@/features/songs/sampleSongs'
import { resolveSongKey } from '@/lib/chordpro/transposeSong'

export default function HomePage() {
  return (
    <section className="space-y-5">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">Minhas músicas</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Acervo de exemplo em domínio público. O cadastro real chega com o Supabase (M3).
        </p>
      </div>

      <ul className="grid gap-3 sm:grid-cols-2">
        {SAMPLE_SONGS.map(({ slug, category, document }) => {
          const { key } = resolveSongKey(document)

          return (
            <li key={slug}>
              <Link to={`/musica/${slug}`} className="block rounded-xl focus-visible:outline-none">
                <Card className="transition-colors hover:border-primary/60 hover:bg-accent/40">
                  <CardContent className="flex items-center gap-3 p-4">
                    <span className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-accent text-accent-foreground">
                      <Music2 className="size-5" aria-hidden />
                    </span>
                    <span className="min-w-0 flex-1">
                      <span className="block truncate font-medium">{document.title}</span>
                      <span className="block truncate text-sm text-muted-foreground">
                        {document.artist}
                      </span>
                    </span>
                    <span className="shrink-0 text-right text-xs text-muted-foreground">
                      <span className="block text-sm font-semibold text-foreground">{key ?? '—'}</span>
                      {document.bpm !== undefined && <span className="block">{document.bpm} BPM</span>}
                      <span className="block">{category}</span>
                    </span>
                  </CardContent>
                </Card>
              </Link>
            </li>
          )
        })}
      </ul>
    </section>
  )
}
