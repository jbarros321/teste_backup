/**
 * Notas e classes de altura (pitch classes).
 *
 * Módulo puro: sem React, sem DOM, sem rede.
 * Trabalhamos com cifra americana (A–G), que é o padrão em cifras brasileiras.
 */

/** Preferência de grafia para notas alteradas. */
export type Accidental = 'sharp' | 'flat'

/** Uma nota decomposta: letra base + alteração em semitons. */
export interface Note {
  /** Letra base, sempre maiúscula: C, D, E, F, G, A ou B. */
  letter: string
  /** Deslocamento da alteração: -2 (bb) a +2 (##). */
  alter: number
  /** Classe de altura, 0 = C … 11 = B. */
  pitchClass: number
  /** Grafia normalizada (ex.: "F♯" vira "F#"). */
  name: string
}

export const SEMITONES_PER_OCTAVE = 12

/** Semitom de cada letra dentro da oitava. */
const LETTER_PITCH: Record<string, number> = {
  C: 0,
  D: 2,
  E: 4,
  F: 5,
  G: 7,
  A: 9,
  B: 11,
}

export const NOTE_LETTERS = Object.keys(LETTER_PITCH)

/** Grafia com sustenidos, indexada por classe de altura. */
export const SHARP_NAMES = [
  'C',
  'C#',
  'D',
  'D#',
  'E',
  'F',
  'F#',
  'G',
  'G#',
  'A',
  'A#',
  'B',
] as const

/** Grafia com bemóis, indexada por classe de altura. */
export const FLAT_NAMES = [
  'C',
  'Db',
  'D',
  'Eb',
  'E',
  'F',
  'Gb',
  'G',
  'Ab',
  'A',
  'Bb',
  'B',
] as const

const NOTE_PATTERN = /^([A-Ga-g])([#b♯♭x]{0,2})$/

/** Converte os símbolos unicode e `x` para a forma ASCII usada internamente. */
function normalizeAccidentals(raw: string): { text: string; alter: number } {
  let alter = 0
  let text = ''

  for (const char of raw) {
    switch (char) {
      case '#':
      case '♯':
        alter += 1
        text += '#'
        break
      case 'b':
      case '♭':
        alter -= 1
        text += 'b'
        break
      case 'x':
        alter += 2
        text += '##'
        break
      default:
        break
    }
  }

  return { text, alter }
}

/** Reduz qualquer inteiro à faixa 0–11. */
export function mod12(value: number): number {
  return ((value % SEMITONES_PER_OCTAVE) + SEMITONES_PER_OCTAVE) % SEMITONES_PER_OCTAVE
}

/**
 * Interpreta uma nota escrita. Devolve `null` se o texto não for uma nota válida,
 * o que é como o resto do sistema decide que um token não é acorde.
 */
export function parseNote(raw: string): Note | null {
  const match = NOTE_PATTERN.exec(raw.trim())
  if (!match) return null

  const letter = match[1]!.toUpperCase()
  const { text, alter } = normalizeAccidentals(match[2]!)

  return {
    letter,
    alter,
    pitchClass: mod12(LETTER_PITCH[letter]! + alter),
    name: letter + text,
  }
}

/** Classe de altura de uma nota escrita, ou `null` se não for nota. */
export function pitchClassOf(raw: string): number | null {
  return parseNote(raw)?.pitchClass ?? null
}

/** Escreve uma classe de altura usando sustenidos ou bemóis. */
export function formatPitchClass(pitchClass: number, accidental: Accidental = 'sharp'): string {
  const table = accidental === 'flat' ? FLAT_NAMES : SHARP_NAMES
  return table[mod12(pitchClass)]!
}

/** Duas notas soam iguais? (C# e Db são enarmônicas.) */
export function isEnharmonic(a: string, b: string): boolean {
  const first = pitchClassOf(a)
  const second = pitchClassOf(b)
  return first !== null && first === second
}
