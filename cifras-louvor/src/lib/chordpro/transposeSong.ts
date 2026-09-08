/**
 * Transposição no nível do documento.
 *
 * Fica aqui, e não em `lib/music`, porque depende do modelo da música — o motor
 * musical continua sem saber o que é uma seção.
 */
import { detectKey, formatKey, parseKey } from '@/lib/music/key'
import { transposeChord, transposedKeyName, type TransposeOptions } from '@/lib/music/transpose'

import { collectChords, type SongDocument } from './types'

/** Aplica o deslocamento a todos os acordes e ao tom declarado. */
export function transposeSong(
  song: SongDocument,
  semitones: number,
  options: TransposeOptions = {},
): SongDocument {
  const targetKey = options.targetKey ?? (song.key ? transposedKeyName(song.key, semitones) : null)
  const resolved: TransposeOptions = { ...options, targetKey }

  return {
    ...song,
    key: song.key ? transposedKeyName(song.key, semitones) : song.key,
    sections: song.sections.map((section) => ({
      ...section,
      lines: section.lines.map((line) => ({
        ...line,
        chords: line.chords.map((slot) => ({
          ...slot,
          chord: transposeChord(slot.chord, semitones, resolved),
        })),
      })),
    })),
  }
}

/**
 * Tom da música: o declarado no arquivo quando existir, senão o detectado a
 * partir dos acordes. Devolve também de onde veio, para a interface poder
 * avisar que é um palpite.
 */
export function resolveSongKey(song: SongDocument): {
  key: string | null
  source: 'declared' | 'detected' | 'unknown'
  confidence: number
} {
  if (song.key && parseKey(song.key)) {
    return { key: formatKey(parseKey(song.key)!), source: 'declared', confidence: 1 }
  }

  const detection = detectKey(collectChords(song))
  if (!detection) return { key: null, source: 'unknown', confidence: 0 }

  return {
    key: formatKey(detection.key),
    source: 'detected',
    confidence: detection.confidence,
  }
}
