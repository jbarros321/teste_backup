import { buildWords, type LyricWord } from '@/lib/chordpro/segments'
import type { SongDocument, SongLine, SongSection } from '@/lib/chordpro/types'
import { cn } from '@/lib/utils'

const SECTION_LABELS: Record<SongSection['type'], string> = {
  intro: 'Intro',
  verse: 'Verso',
  pre_chorus: 'Pré-refrão',
  chorus: 'Refrão',
  bridge: 'Ponte',
  solo: 'Solo',
  interlude: 'Interlúdio',
  outro: 'Final',
  tab: 'Tablatura',
  none: '',
}

export interface ChordRendererProps {
  song: SongDocument
  /** Chamado ao clicar num acorde — abre o dicionário de acordes (M+). */
  onChordClick?: (chord: string) => void
  className?: string
}

/**
 * Desenha a cifra: acordes posicionados acima da sílaba correspondente.
 *
 * Não usamos `<pre>` monoespaçado: a letra quebraria fora da tela no celular.
 * O alinhamento vem da estrutura (ver `lib/chordpro/segments`), então funciona
 * em qualquer largura e com qualquer tamanho de fonte.
 */
export function ChordRenderer({ song, onChordClick, className }: ChordRendererProps) {
  return (
    <article className={cn('song-sheet space-y-6', className)}>
      {song.sections.map((section, index) => (
        <Section key={index} section={section} onChordClick={onChordClick} />
      ))}
    </article>
  )
}

function Section({
  section,
  onChordClick,
}: {
  section: SongSection
  onChordClick?: (chord: string) => void
}) {
  const label = section.name ?? SECTION_LABELS[section.type]

  return (
    <section className="space-y-1">
      {label && (
        <h2 className="text-[0.75em] font-semibold uppercase tracking-widest text-section-label">
          {label}
        </h2>
      )}
      {section.lines.map((line, index) => (
        <Line key={index} line={line} onChordClick={onChordClick} />
      ))}
    </section>
  )
}

function Line({ line, onChordClick }: { line: SongLine; onChordClick?: (chord: string) => void }) {
  if (line.kind === 'empty') return <div className="h-[0.75em]" aria-hidden />

  if (line.kind === 'comment') {
    return <p className="text-[0.85em] italic text-muted-foreground">{line.lyrics}</p>
  }

  if (line.kind === 'tab') {
    return <pre className="song-tab">{line.lyrics}</pre>
  }

  const words = buildWords(line)
  const hasChords = line.chords.length > 0

  return (
    <div className="song-line">
      {words.map((word, index) => (
        <Word key={index} word={word} reserveChordRow={hasChords} onChordClick={onChordClick} />
      ))}
    </div>
  )
}

function Word({
  word,
  reserveChordRow,
  onChordClick,
}: {
  word: LyricWord
  reserveChordRow: boolean
  onChordClick?: (chord: string) => void
}) {
  return (
    <span className="song-word">
      {word.parts.map((part, index) => (
        <span className="song-part" key={index}>
          <Chord chord={part.chord} reserve={reserveChordRow} onChordClick={onChordClick} />
          <span className="song-lyric">{part.text}</span>
        </span>
      ))}
    </span>
  )
}

function Chord({
  chord,
  reserve,
  onChordClick,
}: {
  chord: string | null
  reserve: boolean
  onChordClick?: (chord: string) => void
}) {
  if (chord === null) {
    // Ocupa a mesma altura de um acorde para a letra não subir e descer.
    return reserve ? <span className="song-chord song-chord--empty" aria-hidden /> : null
  }

  if (!onChordClick) return <span className="song-chord">{chord}</span>

  return (
    <button
      type="button"
      className="song-chord"
      onClick={() => onChordClick(chord)}
      aria-label={`Acorde ${chord}`}
    >
      {chord}
    </button>
  )
}
