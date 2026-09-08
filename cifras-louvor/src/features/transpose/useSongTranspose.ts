import { useCallback, useMemo, useState } from 'react'

import { resolveSongKey, transposeSong } from '@/lib/chordpro/transposeSong'
import type { SongDocument } from '@/lib/chordpro/types'
import { formatKey, keysForMode, parseKey, type MusicalKey } from '@/lib/music/key'
import { semitonesToKey, signedSemitones, transposeKey } from '@/lib/music/transpose'
import { useSettingsStore } from '@/features/settings/useSettingsStore'

export interface SongTranspose {
  /** Documento já transposto, pronto para renderizar. */
  song: SongDocument
  /** Tom original da música, como veio do arquivo ou detectado. */
  originalKey: MusicalKey | null
  /** O tom original foi declarado ou é um palpite nosso? */
  keySource: 'declared' | 'detected' | 'unknown'
  keyConfidence: number
  /** Tom em que a música está sendo exibida agora. */
  currentKey: MusicalKey | null
  /** Deslocamento aplicado, de −6 a +5. */
  semitones: number
  isTransposed: boolean
  /** Os 12 tons disponíveis no mesmo modo do original. */
  availableKeys: MusicalKey[]
  transposeUp: () => void
  transposeDown: () => void
  setKey: (key: MusicalKey | string) => void
  reset: () => void
}

/**
 * Estado de transposição de uma música. Fica local à tela: cada música abre no
 * seu tom, e mudar de música não carrega o tom da anterior.
 */
export function useSongTranspose(source: SongDocument): SongTranspose {
  const [semitones, setSemitones] = useState(0)
  const spelling = useSettingsStore((s) => s.accidentals)

  const resolved = useMemo(() => resolveSongKey(source), [source])
  const originalKey = useMemo(
    () => (resolved.key ? parseKey(resolved.key) : null),
    [resolved.key],
  )

  const currentKey = useMemo(
    () => (originalKey ? transposeKey(originalKey, semitones) : null),
    [originalKey, semitones],
  )

  const song = useMemo(
    () =>
      semitones === 0
        ? source
        : transposeSong(source, semitones, { targetKey: currentKey, spelling }),
    [source, semitones, currentKey, spelling],
  )

  const availableKeys = useMemo(
    () => keysForMode(originalKey?.mode ?? 'major'),
    [originalKey?.mode],
  )

  const setKey = useCallback(
    (key: MusicalKey | string) => {
      if (!originalKey) return
      const target = typeof key === 'string' ? parseKey(key) : key
      if (target) setSemitones(semitonesToKey(originalKey, target))
    },
    [originalKey],
  )

  return {
    song,
    originalKey,
    keySource: resolved.source,
    keyConfidence: resolved.confidence,
    currentKey,
    semitones: signedSemitones(semitones),
    isTransposed: signedSemitones(semitones) !== 0,
    availableKeys,
    transposeUp: useCallback(() => setSemitones((value) => value + 1), []),
    transposeDown: useCallback(() => setSemitones((value) => value - 1), []),
    setKey,
    reset: useCallback(() => setSemitones(0), []),
  }
}

/** Rótulo do tom para a interface ("G", "Em", "—"). */
export function keyLabel(key: MusicalKey | null): string {
  return key ? formatKey(key) : '—'
}
