import { ArrowLeft, TriangleAlert } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'

import { buttonVariants } from '@/components/ui/button'
import { FontSizeControl } from '@/features/settings/FontSizeControl'
import { ChordRenderer } from '@/features/songs/ChordRenderer'
import { findSampleSong } from '@/features/songs/sampleSongs'
import { TransposeControl } from '@/features/transpose/TransposeControl'
import { useSongTranspose } from '@/features/transpose/useSongTranspose'
import type { SongDocument } from '@/lib/chordpro/types'

export default function SongPage() {
  const { slug = '' } = useParams()
  const sample = findSampleSong(slug)

  if (!sample) {
    return (
      <section className="py-16 text-center">
        <h1 className="text-2xl font-semibold tracking-tight">Música não encontrada</h1>
        <Link to="/" className={`${buttonVariants({ variant: 'outline' })} mt-6`}>
          Voltar para as músicas
        </Link>
      </section>
    )
  }

  return <SongView song={sample.document} />
}

function SongView({ song }: { song: SongDocument }) {
  const transpose = useSongTranspose(song)

  return (
    <div className="pb-24">
      <header className="mb-6 space-y-4">
        <Link
          to="/"
          className={`${buttonVariants({ variant: 'ghost', size: 'sm' })} -ml-2 text-muted-foreground`}
        >
          <ArrowLeft aria-hidden />
          Voltar
        </Link>

        <div className="flex flex-wrap items-end justify-between gap-4">
          <div className="min-w-0">
            <h1 className="truncate text-2xl font-semibold tracking-tight">
              {song.title ?? 'Sem título'}
            </h1>
            {song.artist && <p className="text-sm text-muted-foreground">{song.artist}</p>}
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <TransposeControl transpose={transpose} />
            <FontSizeControl />
          </div>
        </div>

        <dl className="flex flex-wrap gap-x-6 gap-y-1 text-sm text-muted-foreground">
          {song.bpm !== undefined && (
            <div className="flex gap-1">
              <dt>BPM</dt>
              <dd className="font-medium text-foreground">{song.bpm}</dd>
            </div>
          )}
          {song.timeSignature && (
            <div className="flex gap-1">
              <dt>Compasso</dt>
              <dd className="font-medium text-foreground">{song.timeSignature}</dd>
            </div>
          )}
          {song.capo !== undefined && (
            <div className="flex gap-1">
              <dt>Capotraste</dt>
              <dd className="font-medium text-foreground">{song.capo}ª casa</dd>
            </div>
          )}
        </dl>

        {transpose.keySource === 'detected' && (
          <p className="flex items-start gap-2 rounded-md border border-border bg-muted/60 px-3 py-2 text-sm text-muted-foreground">
            <TriangleAlert className="mt-0.5 size-4 shrink-0" aria-hidden />
            <span>
              A música não declara o tom. Deduzimos <strong>{transpose.originalKey?.tonic}</strong>{' '}
              pelos acordes ({Math.round(transpose.keyConfidence * 100)}% de confiança) — confira
              antes de transpor.
            </span>
          </p>
        )}
      </header>

      <ChordRenderer song={transpose.song} />
    </div>
  )
}
