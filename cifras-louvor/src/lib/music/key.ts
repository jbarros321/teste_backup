/**
 * Tonalidades: leitura, grafia preferida e detecção a partir dos acordes.
 *
 * A grafia preferida é o que separa uma transposição correta de uma feia:
 * em Mi maior o segundo grau é F#m, nunca Gbm.
 */
import { formatPitchClass, mod12, parseNote, type Accidental } from './notes'
import { parseChord, type Chord } from './chord'

export type KeyMode = 'major' | 'minor'

export interface MusicalKey {
  /** Tônica como escrita: "G", "F#", "Bb". */
  tonic: string
  mode: KeyMode
}

/** Tons maiores tradicionalmente escritos com bemóis. */
const FLAT_MAJOR_KEYS = new Set(['F', 'Bb', 'Eb', 'Ab', 'Db', 'Gb', 'Cb'])
/** Tons menores tradicionalmente escritos com bemóis (relativos dos acima). */
const FLAT_MINOR_KEYS = new Set(['D', 'G', 'C', 'F', 'Bb', 'Eb'])

/**
 * Grafia canônica de cada classe de altura — a que uma cifra usa por padrão.
 * Escolhe a armadura mais simples: Db (5 bemóis) em vez de C# (7 sustenidos),
 * mas F# em vez de Gb, que é como se escreve na prática.
 */
const CANONICAL_MAJOR = ['C', 'Db', 'D', 'Eb', 'E', 'F', 'F#', 'G', 'Ab', 'A', 'Bb', 'B']
const CANONICAL_MINOR = ['C', 'C#', 'D', 'Eb', 'E', 'F', 'F#', 'G', 'G#', 'A', 'Bb', 'B']

/** Graus da escala maior, em semitons a partir da tônica. */
const MAJOR_SCALE = [0, 2, 4, 5, 7, 9, 11]
/** Sufixo esperado de cada grau da harmonia maior. */
const MAJOR_DEGREE_QUALITY = ['', 'm', 'm', '', '', 'm', 'dim']
/** Graus da escala menor natural. */
const MINOR_SCALE = [0, 2, 3, 5, 7, 8, 10]
const MINOR_DEGREE_QUALITY = ['m', 'dim', '', 'm', 'm', '', '']

const KEY_PATTERN = /^([A-G][#b♯♭]{0,2})\s*(m|min|minor|menor)?$/i

/** Lê um tom escrito ("G", "Em", "Bbm"). Devolve `null` se não for tom válido. */
export function parseKey(raw: string): MusicalKey | null {
  const match = KEY_PATTERN.exec(raw.trim())
  if (!match) return null

  const note = parseNote(match[1]!)
  if (!note) return null

  return { tonic: note.name, mode: match[2] ? 'minor' : 'major' }
}

/** Escreve o tom ("G", "Em"). */
export function formatKey(key: MusicalKey): string {
  return key.tonic + (key.mode === 'minor' ? 'm' : '')
}

/** Sustenidos ou bemóis para este tom? */
export function preferredAccidental(key: MusicalKey): Accidental {
  const set = key.mode === 'minor' ? FLAT_MINOR_KEYS : FLAT_MAJOR_KEYS
  if (set.has(key.tonic)) return 'flat'
  return key.tonic.includes('b') ? 'flat' : 'sharp'
}

/** Todos os 12 tons de um modo, cada um já na grafia usual. */
export function keysForMode(mode: KeyMode): MusicalKey[] {
  return Array.from({ length: 12 }, (_, pitchClass) =>
    withPreferredSpelling({ tonic: formatPitchClass(pitchClass, 'sharp'), mode }),
  )
}

/** Classe de altura da tônica. */
export function pitchClassOfKey(key: MusicalKey): number {
  return parseNote(key.tonic)?.pitchClass ?? 0
}

/** Distância em semitons de um tom para outro, sempre de 0 a 11. */
export function semitonesBetween(from: MusicalKey, to: MusicalKey): number {
  return mod12(pitchClassOfKey(to) - pitchClassOfKey(from))
}

/** Os acordes diatônicos do tom, na grafia dele. */
export function diatonicChords(key: MusicalKey): string[] {
  const scale = key.mode === 'minor' ? MINOR_SCALE : MAJOR_SCALE
  const qualities = key.mode === 'minor' ? MINOR_DEGREE_QUALITY : MAJOR_DEGREE_QUALITY
  const accidental = preferredAccidental(key)
  const tonic = pitchClassOfKey(key)

  return scale.map(
    (step, index) => formatPitchClass(tonic + step, accidental) + qualities[index]!,
  )
}

export interface KeyDetection {
  key: MusicalKey
  /** 0–1: proporção de acordes que cabem no tom. */
  confidence: number
}

/**
 * Adivinha o tom a partir da sequência de acordes.
 *
 * Pontua cada um dos 24 tons pela quantidade de acordes diatônicos e dá um bônus
 * para o tom cuja tônica abre ou fecha a música — que é como o ouvido resolve.
 * Nunca substitui um tom informado pelo usuário; serve só como sugestão.
 */
export function detectKey(chords: Array<Chord | string>): KeyDetection | null {
  const parsed = chords
    .map((chord) => (typeof chord === 'string' ? parseChord(chord) : chord))
    .filter((chord): chord is Chord => chord !== null)

  if (parsed.length === 0) return null

  const first = parsed[0]!
  const last = parsed[parsed.length - 1]!

  let best: KeyDetection | null = null

  for (const mode of ['major', 'minor'] as const) {
    for (let pitchClass = 0; pitchClass < 12; pitchClass += 1) {
      const key: MusicalKey = { tonic: formatPitchClass(pitchClass, 'sharp'), mode }
      const diatonic = new Set(
        diatonicChords(key).map((chord) => {
          const note = parseNote(chord.replace(/(dim|m)$/, ''))
          const quality = chord.replace(/^[A-G][#b]?/, '')
          return `${note?.pitchClass}:${quality}`
        }),
      )

      let score = 0
      for (const chord of parsed) {
        const note = parseNote(chord.root)
        if (!note) continue
        const quality = normalizeQualityForMatching(chord.suffix)
        if (diatonic.has(`${note.pitchClass}:${quality}`)) score += 1
        else if (hasSameRoot(diatonic, note.pitchClass)) score += 0.4
      }

      if (matchesTonic(first, pitchClass, mode)) score += 1.2
      if (matchesTonic(last, pitchClass, mode)) score += 1.6

      const confidence = score / (parsed.length + 2.8)
      if (!best || confidence > best.confidence) {
        best = { key: withPreferredSpelling(key), confidence: Math.min(1, confidence) }
      }
    }
  }

  return best
}

/** Reduz o sufixo ao que importa para casar com um grau da escala. */
function normalizeQualityForMatching(suffix: string): string {
  if (/^(dim|°|º)/.test(suffix)) return 'dim'
  if (/^(m|min)(?!aj)/.test(suffix)) return 'm'
  return ''
}

function hasSameRoot(diatonic: Set<string>, pitchClass: number): boolean {
  for (const entry of diatonic) {
    if (entry.startsWith(`${pitchClass}:`)) return true
  }
  return false
}

function matchesTonic(chord: Chord, pitchClass: number, mode: KeyMode): boolean {
  const note = parseNote(chord.root)
  if (!note || note.pitchClass !== pitchClass) return false
  const isMinorChord = normalizeQualityForMatching(chord.suffix) === 'm'
  return mode === 'minor' ? isMinorChord : !isMinorChord
}

/** O tom que costuma nomear esta classe de altura (Db em vez de C#). */
export function canonicalKey(pitchClass: number, mode: KeyMode): MusicalKey {
  const table = mode === 'minor' ? CANONICAL_MINOR : CANONICAL_MAJOR
  return { tonic: table[mod12(pitchClass)]!, mode }
}

/** Reescreve a tônica na grafia canônica, mantendo o modo. */
function withPreferredSpelling(key: MusicalKey): MusicalKey {
  return canonicalKey(pitchClassOfKey(key), key.mode)
}
