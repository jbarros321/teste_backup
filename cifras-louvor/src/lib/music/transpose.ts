/**
 * Transposição.
 *
 * O deslocamento é trivial; o que dá qualidade ao resultado é a GRAFIA:
 * depois de somar semitons, escolhemos sustenido ou bemol pelo tom de destino.
 * É a diferença entre G→A produzir `F#m` (certo) ou `Gbm` (feio e errado).
 */
import { parseChord, formatChord, type Chord } from './chord'
import {
  canonicalKey,
  formatKey,
  parseKey,
  preferredAccidental,
  semitonesBetween,
  type MusicalKey,
} from './key'
import { formatPitchClass, mod12, parseNote, type Accidental } from './notes'

/** Como escolher a grafia das notas alteradas depois de transpor. */
export type SpellingPreference = Accidental | 'auto'

export interface TransposeOptions {
  /** Tom de destino — quando informado com `auto`, define a grafia. */
  targetKey?: MusicalKey | string | null
  /** Preferência explícita do usuário; `auto` segue o tom de destino. */
  spelling?: SpellingPreference
}

/** Resolve qual grafia usar a partir das opções. */
export function resolveAccidental(options: TransposeOptions = {}): Accidental {
  const { spelling = 'auto', targetKey } = options
  if (spelling !== 'auto') return spelling

  const key = typeof targetKey === 'string' ? parseKey(targetKey) : targetKey
  return key ? preferredAccidental(key) : 'sharp'
}

/** Transpõe uma nota escrita. Devolve o texto original se não for nota. */
export function transposeNote(
  raw: string,
  semitones: number,
  accidental: Accidental = 'sharp',
): string {
  const note = parseNote(raw)
  if (!note) return raw
  return formatPitchClass(note.pitchClass + semitones, accidental)
}

/** Transpõe um acorde já interpretado, preservando o sufixo. */
export function transposeParsedChord(
  chord: Chord,
  semitones: number,
  options: TransposeOptions = {},
): Chord {
  const accidental = resolveAccidental(options)
  return {
    ...chord,
    root: transposeNote(chord.root, semitones, accidental),
    bass: chord.bass ? transposeNote(chord.bass, semitones, accidental) : undefined,
  }
}

/**
 * Transpõe um acorde escrito. Se o texto não for um acorde válido ele volta
 * intacto — nunca inventamos informação musical.
 */
export function transposeChord(
  raw: string,
  semitones: number,
  options: TransposeOptions = {},
): string {
  const chord = parseChord(raw)
  if (!chord) return raw
  return formatChord(transposeParsedChord(chord, semitones, options))
}

/** Transpõe um tom inteiro, já com a grafia usual do tom resultante. */
export function transposeKey(key: MusicalKey, semitones: number): MusicalKey {
  const tonic = parseNote(key.tonic)
  if (!tonic) return key
  return canonicalKey(tonic.pitchClass + semitones, key.mode)
}

/** Quantos semitons separam o tom original do desejado (0–11). */
export function semitonesToKey(
  originalKey: MusicalKey | string,
  targetKey: MusicalKey | string,
): number {
  const from = typeof originalKey === 'string' ? parseKey(originalKey) : originalKey
  const to = typeof targetKey === 'string' ? parseKey(targetKey) : targetKey
  if (!from || !to) return 0
  return semitonesBetween(from, to)
}

/**
 * Deslocamento "amigável" para exibir: -6 a +5 em vez de 0 a 11, para que
 * descer meio tom apareça como −1 e não como +11.
 */
export function signedSemitones(semitones: number): number {
  const normalized = mod12(semitones)
  return normalized > 6 ? normalized - 12 : normalized
}

/** Rótulo do deslocamento atual, pronto para a interface: "+2", "−1", "Tom original". */
export function formatTransposeLabel(semitones: number): string {
  const value = signedSemitones(semitones)
  if (value === 0) return 'Tom original'
  return value > 0 ? `+${value}` : `−${Math.abs(value)}`
}

/** Aplica a transposição a uma lista de acordes de uma vez. */
export function transposeChords(
  chords: string[],
  semitones: number,
  options: TransposeOptions = {},
): string[] {
  return chords.map((chord) => transposeChord(chord, semitones, options))
}

/** Nome do tom resultante ao transpor `originalKey` em `semitones`. */
export function transposedKeyName(originalKey: MusicalKey | string, semitones: number): string {
  const key = typeof originalKey === 'string' ? parseKey(originalKey) : originalKey
  if (!key) return typeof originalKey === 'string' ? originalKey : ''
  return formatKey(transposeKey(key, semitones))
}
