import { useCallback, useEffect, useRef, useState } from 'react'

import { getMetronome } from '@/lib/audio/metronome'
import {
  DEFAULT_TIME_SIGNATURE,
  bpmFromTaps,
  clampBpm,
  parseTimeSignature,
  pruneTaps,
  type Subdivision,
  type TimeSignature,
} from '@/lib/audio/metronomeTiming'
import { useSettingsStore } from '@/features/settings/useSettingsStore'

export interface MetronomeController {
  isPlaying: boolean
  bpm: number
  signature: TimeSignature
  subdivision: Subdivision
  accentFirstBeat: boolean
  volume: number
  /** Tempo do compasso que está soando agora, ou `null` quando parado. */
  currentBeat: number | null
  toggle: () => void
  setBpm: (bpm: number) => void
  nudgeBpm: (delta: number) => void
  setSignature: (signature: TimeSignature) => void
  setSubdivision: (subdivision: Subdivision) => void
  setAccentFirstBeat: (accent: boolean) => void
  setVolume: (volume: number) => void
  /** Registra uma batida do tap tempo e devolve o BPM calculado. */
  tap: () => void
  /** Volta ao BPM que veio da música. */
  reset: () => void
}

/**
 * Liga o motor do metrônomo ao React.
 *
 * O motor é único e vive fora do React; o hook só reflete o estado dele e
 * garante que a música que está aberta define o BPM e o compasso iniciais.
 */
export function useMetronome(
  songBpm: number | null,
  songTimeSignature: string | null,
): MetronomeController {
  const engine = getMetronome()
  const storedVolume = useSettingsStore((s) => s.metronomeVolume)
  const setStoredVolume = useSettingsStore((s) => s.setMetronomeVolume)
  const defaultBpm = useSettingsStore((s) => s.defaultBpm)

  const initialBpm = clampBpm(songBpm ?? defaultBpm)
  const initialSignature = parseTimeSignature(songTimeSignature) ?? DEFAULT_TIME_SIGNATURE

  const [isPlaying, setIsPlaying] = useState(engine.isRunning)
  const [bpm, setBpmState] = useState(initialBpm)
  const [signature, setSignatureState] = useState(initialSignature)
  const [subdivision, setSubdivisionState] = useState<Subdivision>(1)
  const [accentFirstBeat, setAccentState] = useState(true)
  const [currentBeat, setCurrentBeat] = useState<number | null>(null)

  const taps = useRef<number[]>([])

  // Trocar de música redefine andamento e compasso para os dela.
  useEffect(() => {
    setBpmState(initialBpm)
    setSignatureState(initialSignature)
    engine.setBpm(initialBpm)
    engine.setTimeSignature(initialSignature)
    // `initialSignature` é recriado a cada render; comparamos pelo valor.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialBpm, initialSignature.beats, initialSignature.unit])

  useEffect(() => {
    engine.setVolume(storedVolume)
  }, [engine, storedVolume])

  useEffect(() => engine.onTick(({ beat }) => setCurrentBeat(beat)), [engine])

  // Sai da tela com o metrônomo tocando? Para o som.
  useEffect(() => {
    return () => {
      engine.stop()
      setCurrentBeat(null)
    }
  }, [engine])

  const toggle = useCallback(() => {
    void engine.toggle().then(() => {
      setIsPlaying(engine.isRunning)
      if (!engine.isRunning) setCurrentBeat(null)
    })
  }, [engine])

  const setBpm = useCallback(
    (next: number) => {
      const value = clampBpm(next)
      setBpmState(value)
      engine.setBpm(value)
    },
    [engine],
  )

  return {
    isPlaying,
    bpm,
    signature,
    subdivision,
    accentFirstBeat,
    volume: storedVolume,
    currentBeat,
    toggle,
    setBpm,
    nudgeBpm: useCallback((delta: number) => setBpm(bpm + delta), [bpm, setBpm]),
    setSignature: useCallback(
      (next: TimeSignature) => {
        setSignatureState(next)
        engine.setTimeSignature(next)
        setCurrentBeat(null)
      },
      [engine],
    ),
    setSubdivision: useCallback(
      (next: Subdivision) => {
        setSubdivisionState(next)
        engine.setSubdivision(next)
      },
      [engine],
    ),
    setAccentFirstBeat: useCallback(
      (accent: boolean) => {
        setAccentState(accent)
        engine.setAccentFirstBeat(accent)
      },
      [engine],
    ),
    setVolume: useCallback(
      (value: number) => {
        setStoredVolume(value)
        engine.setVolume(value)
      },
      [engine, setStoredVolume],
    ),
    tap: useCallback(() => {
      const now = performance.now()
      taps.current = pruneTaps(taps.current, now)
      const detected = bpmFromTaps(taps.current)
      if (detected !== null) setBpm(detected)
    }, [setBpm]),
    reset: useCallback(() => setBpm(initialBpm), [initialBpm, setBpm]),
  }
}
