import { Maximize, Minimize, Timer, X } from 'lucide-react'
import { useRef, type RefObject } from 'react'

import { Button } from '@/components/ui/button'
import { AutoScrollControl } from '@/features/autoscroll/AutoScrollControl'
import type { AutoScrollController } from '@/features/autoscroll/useAutoScroll'
import type { MetronomeController } from '@/features/metronome/useMetronome'
import { FontSizeControl } from '@/features/settings/FontSizeControl'
import { ChordRenderer } from '@/features/songs/ChordRenderer'
import { keyLabel, type SongTranspose } from '@/features/transpose/useSongTranspose'
import { formatTimeSignature } from '@/lib/audio/metronomeTiming'
import { cn } from '@/lib/utils'

export interface StageModeProps {
  title: string
  transpose: SongTranspose
  metronome: MetronomeController
  autoScroll: AutoScrollController
  scrollRef: RefObject<HTMLDivElement | null>
  isFullscreen: boolean
  fullscreenSupported: boolean
  onToggleFullscreen: () => void
  onExit: () => void
}

/**
 * Modo palco (§14 e §70 do escopo).
 *
 * A regra aqui é subtração: enquanto o músico toca, só ficam na tela o tom, o
 * BPM, o compasso e o mínimo para mexer neles. Tudo que não ajuda a tocar sai.
 */
export function StageMode({
  title,
  transpose,
  metronome,
  autoScroll,
  scrollRef,
  isFullscreen,
  fullscreenSupported,
  onToggleFullscreen,
  onExit,
}: StageModeProps) {
  const barRef = useRef<HTMLDivElement>(null)

  return (
    <div className="fixed inset-0 z-50 flex flex-col bg-background text-foreground">
      <header
        ref={barRef}
        className="flex shrink-0 items-center gap-4 border-b border-border px-4 py-2"
      >
        <dl className="flex items-center gap-5 text-sm">
          <div className="flex items-baseline gap-1.5">
            <dt className="text-muted-foreground">TOM</dt>
            <dd className="text-lg font-semibold tabular-nums">{keyLabel(transpose.currentKey)}</dd>
          </div>
          <div className="flex items-baseline gap-1.5">
            <dt className="text-muted-foreground">BPM</dt>
            <dd className="text-lg font-semibold tabular-nums">{metronome.bpm}</dd>
          </div>
          <div className="flex items-baseline gap-1.5">
            <dt className="sr-only">Compasso</dt>
            <dd className="text-lg font-semibold tabular-nums">
              {formatTimeSignature(metronome.signature)}
            </dd>
          </div>
        </dl>

        <BeatDots metronome={metronome} />

        <span className="ml-auto hidden max-w-[24ch] truncate text-sm text-muted-foreground sm:inline">
          {title}
        </span>

        {fullscreenSupported && (
          <Button
            type="button"
            variant="ghost"
            size="icon"
            aria-label={isFullscreen ? 'Sair da tela cheia' : 'Tela cheia'}
            onClick={onToggleFullscreen}
          >
            {isFullscreen ? <Minimize aria-hidden /> : <Maximize aria-hidden />}
          </Button>
        )}

        <Button
          type="button"
          variant="ghost"
          size="icon"
          aria-label="Sair do modo palco"
          onClick={onExit}
        >
          <X aria-hidden />
        </Button>
      </header>

      <div ref={scrollRef} className="min-h-0 flex-1 overflow-y-auto px-5 py-6">
        <ChordRenderer song={transpose.song} />
        {/* Espaço no fim para a última linha subir até o centro da tela. */}
        <div className="h-[50vh]" aria-hidden />
      </div>

      <footer className="flex shrink-0 flex-wrap items-center justify-center gap-2 border-t border-border px-4 py-2">
        <Button
          type="button"
          size="icon"
          variant={metronome.isPlaying ? 'default' : 'outline'}
          aria-label={metronome.isPlaying ? 'Parar metrônomo' : 'Iniciar metrônomo'}
          aria-pressed={metronome.isPlaying}
          onClick={metronome.toggle}
        >
          <Timer aria-hidden />
        </Button>

        <Button
          type="button"
          variant="outline"
          size="icon"
          aria-label="Diminuir BPM"
          onClick={() => metronome.nudgeBpm(-1)}
        >
          −
        </Button>
        <Button
          type="button"
          variant="outline"
          size="icon"
          aria-label="Aumentar BPM"
          onClick={() => metronome.nudgeBpm(1)}
        >
          +
        </Button>

        <span className="mx-2 h-6 w-px bg-border" aria-hidden />

        <Button
          type="button"
          variant="outline"
          size="icon"
          aria-label="Baixar meio tom"
          onClick={transpose.transposeDown}
        >
          ♭
        </Button>
        <Button
          type="button"
          variant="outline"
          size="icon"
          aria-label="Subir meio tom"
          onClick={transpose.transposeUp}
        >
          ♯
        </Button>

        <span className="mx-2 h-6 w-px bg-border" aria-hidden />

        <AutoScrollControl controller={autoScroll} compact />

        <span className="mx-2 h-6 w-px bg-border" aria-hidden />

        <FontSizeControl />
      </footer>
    </div>
  )
}

function BeatDots({ metronome }: { metronome: MetronomeController }) {
  return (
    <div
      className="flex items-center gap-1.5"
      role="status"
      aria-label={
        metronome.currentBeat === null
          ? 'Metrônomo parado'
          : `Tempo ${metronome.currentBeat + 1} de ${metronome.signature.beats}`
      }
    >
      {Array.from({ length: metronome.signature.beats }, (_, beat) => (
        <span
          key={beat}
          aria-hidden
          className={cn(
            'size-2.5 rounded-full transition-colors duration-75',
            metronome.currentBeat === beat
              ? beat === 0 && metronome.accentFirstBeat
                ? 'bg-beat-accent'
                : 'bg-primary'
              : 'bg-beat-normal',
          )}
        />
      ))}
    </div>
  )
}
