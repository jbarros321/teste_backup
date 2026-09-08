import { ArrowLeft, ChevronsDown, Maximize, Pencil, Timer, TriangleAlert } from 'lucide-react'
import { useMemo, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'

import { Alert } from '@/components/ui/alert'
import { Button, buttonVariants } from '@/components/ui/button'
import { AutoScrollControl } from '@/features/autoscroll/AutoScrollControl'
import { useAutoScroll } from '@/features/autoscroll/useAutoScroll'
import { MetronomeControl } from '@/features/metronome/MetronomeControl'
import { useMetronome } from '@/features/metronome/useMetronome'
import { FontSizeControl } from '@/features/settings/FontSizeControl'
import { ChordRenderer } from '@/features/songs/ChordRenderer'
import { useIsDemo, useSong } from '@/features/songs/songQueries'
import type { SongDetail } from '@/features/songs/songSource'
import { TransposeControl } from '@/features/transpose/TransposeControl'
import { useSongTranspose } from '@/features/transpose/useSongTranspose'
import { usePlayerStore } from '@/features/player/usePlayerStore'
import { StageMode } from '@/features/stage/StageMode'
import { useFullscreen } from '@/hooks/useFullscreen'
import { useKeyboardShortcuts } from '@/hooks/useKeyboardShortcuts'
import { useSettingsStore } from '@/features/settings/useSettingsStore'

/** Referência sempre nula: sinaliza ao auto scroll que ele deve rolar a janela. */
const NO_CONTAINER = { current: null }

export default function SongPage() {
  const { slug = '' } = useParams()
  const { data: song, isPending, isError, error } = useSong(slug)

  if (isPending) {
    return (
      <p className="py-16 text-center text-sm text-muted-foreground" role="status">
        Carregando…
      </p>
    )
  }

  if (isError) {
    return (
      <div className="py-10">
        <Alert variant="destructive">
          {error instanceof Error ? error.message : 'Não foi possível carregar a música.'}
        </Alert>
      </div>
    )
  }

  if (!song) {
    return (
      <section className="py-16 text-center">
        <h1 className="text-2xl font-semibold tracking-tight">Música não encontrada</h1>
        <Link to="/" className={`${buttonVariants({ variant: 'outline' })} mt-6`}>
          Voltar para as músicas
        </Link>
      </section>
    )
  }

  return <SongView song={song} />
}

function SongView({ song }: { song: SongDetail }) {
  const transpose = useSongTranspose(song.document)
  const isDemo = useIsDemo()
  const metronome = useMetronome(song.bpm, song.timeSignature)
  const [showMetronome, setShowMetronome] = useState(false)
  const [showScroll, setShowScroll] = useState(false)

  const stageMode = usePlayerStore((state) => state.stageMode)
  const setStageMode = usePlayerStore((state) => state.setStageMode)
  const fullscreen = useFullscreen()

  // No palco a rolagem é do painel da cifra; fora dele, da janela inteira.
  const stageScrollRef = useRef<HTMLDivElement>(null)
  const autoScroll = useAutoScroll(
    stageMode ? stageScrollRef : NO_CONTAINER,
    metronome.bpm,
    metronome.signature.beats,
  )

  const increaseFont = useSettingsStore((state) => state.increaseFontSize)
  const decreaseFont = useSettingsStore((state) => state.decreaseFontSize)

  const enterStage = () => {
    setStageMode(true)
    if (fullscreen.isSupported) void fullscreen.enter()
  }

  const exitStage = () => {
    autoScroll.stop()
    setStageMode(false)
    void fullscreen.exit()
  }

  // Atalhos que o músico usa com o instrumento na mão (§15).
  const shortcuts = useMemo(
    () => [
      { key: ' ', handler: metronome.toggle },
      { key: 'm', handler: () => setShowMetronome((visible) => !visible) },
      { key: 's', handler: autoScroll.toggle },
      { key: 'ArrowUp', handler: () => metronome.nudgeBpm(1), allowRepeat: true },
      { key: 'ArrowDown', handler: () => metronome.nudgeBpm(-1), allowRepeat: true },
      { key: 'ArrowRight', handler: autoScroll.faster, allowRepeat: true },
      { key: 'ArrowLeft', handler: autoScroll.slower, allowRepeat: true },
      { key: 't', handler: transpose.transposeUp },
      { key: '+', handler: increaseFont, allowRepeat: true },
      { key: '-', handler: decreaseFont, allowRepeat: true },
      { key: 'f', handler: () => (stageMode ? fullscreen.toggle() : enterStage()) },
      { key: 'Escape', handler: () => stageMode && exitStage() },
    ],
    [metronome, autoScroll, transpose, increaseFont, decreaseFont, stageMode, fullscreen],
  )

  useKeyboardShortcuts(shortcuts)

  if (stageMode) {
    return (
      <StageMode
        title={song.title}
        transpose={transpose}
        metronome={metronome}
        autoScroll={autoScroll}
        scrollRef={stageScrollRef}
        isFullscreen={fullscreen.isFullscreen}
        fullscreenSupported={fullscreen.isSupported}
        onToggleFullscreen={fullscreen.toggle}
        onExit={exitStage}
      />
    )
  }

  return (
    <div className="pb-24">
      <header className="mb-6 space-y-4">
        <div className="flex items-center justify-between">
          <Link
            to="/"
            className={`${buttonVariants({ variant: 'ghost', size: 'sm' })} -ml-2 text-muted-foreground`}
          >
            <ArrowLeft aria-hidden />
            Voltar
          </Link>

          {!isDemo && (
            <Link
              to={`/musica/${song.slug}/editar`}
              className={buttonVariants({ variant: 'outline', size: 'sm' })}
            >
              <Pencil aria-hidden />
              Editar cifra
            </Link>
          )}
        </div>

        <div className="flex flex-wrap items-end justify-between gap-4">
          <div className="min-w-0">
            <h1 className="truncate text-2xl font-semibold tracking-tight">{song.title}</h1>
            {song.artist && <p className="text-sm text-muted-foreground">{song.artist}</p>}
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <TransposeControl transpose={transpose} />
            <FontSizeControl />
            <Button
              type="button"
              variant={showMetronome ? 'default' : 'outline'}
              size="icon"
              aria-label="Metrônomo (M)"
              title="Metrônomo (M)"
              aria-pressed={showMetronome}
              onClick={() => setShowMetronome((visible) => !visible)}
            >
              <Timer aria-hidden />
            </Button>

            <Button
              type="button"
              variant={showScroll ? 'default' : 'outline'}
              size="icon"
              aria-label="Rolagem automática (S)"
              title="Rolagem automática (S)"
              aria-pressed={showScroll}
              onClick={() => setShowScroll((visible) => !visible)}
            >
              <ChevronsDown aria-hidden />
            </Button>

            <Button
              type="button"
              variant="outline"
              size="icon"
              aria-label="Modo palco (F)"
              title="Modo palco (F)"
              onClick={enterStage}
            >
              <Maximize aria-hidden />
            </Button>
          </div>
        </div>

        <dl className="flex flex-wrap gap-x-6 gap-y-1 text-sm text-muted-foreground">
          {song.bpm !== null && (
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
          {song.capo !== null && (
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
        {showMetronome && <MetronomeControl controller={metronome} />}
        {showScroll && <AutoScrollControl controller={autoScroll} />}
      </header>

      <ChordRenderer song={transpose.song} />

      <p className="mt-10 text-xs leading-relaxed text-muted-foreground">
        Atalhos: <kbd>Espaço</kbd> metrônomo · <kbd>M</kbd> controles do metrônomo ·{' '}
        <kbd>S</kbd> rolagem · <kbd>↑</kbd> <kbd>↓</kbd> BPM · <kbd>←</kbd> <kbd>→</kbd>{' '}
        velocidade da rolagem · <kbd>T</kbd> sobe meio tom · <kbd>+</kbd> <kbd>−</kbd> fonte ·{' '}
        <kbd>F</kbd> modo palco · <kbd>Esc</kbd> sai do palco.
      </p>
    </div>
  )
}
