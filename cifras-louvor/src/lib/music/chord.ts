/**
 * Reconhecimento e formatação de acordes.
 *
 * A gramática é ESTRITA de propósito: um token só vira acorde se casar 100%.
 * É isso que impede a letra ("Amor", "Deus", "Cada") de virar acorde na importação.
 * O sufixo é preservado como o usuário escreveu — a transposição só mexe na
 * fundamental e no baixo, então `Cmaj7/E` continua `Cmaj7` ao mudar de tom.
 */
import { formatPitchClass, parseNote, type Accidental, type Note } from './notes'

export interface Chord {
  /** Fundamental, como escrita: "F#", "Bb", "C". */
  root: string
  /** Sufixo verbatim depois da fundamental: "m7", "sus4", "maj7", "" para tríade maior. */
  suffix: string
  /** Baixo invertido depois da barra, quando houver: "C/E" tem baixo "E". */
  bass?: string
  /** Texto original recebido. */
  raw: string
}

/**
 * Tokens aceitos no sufixo, do mais longo para o mais curto — a ordem importa
 * porque o casamento é guloso da esquerda para a direita.
 */
const SUFFIX_TOKENS = [
  'maj13',
  'maj11',
  'maj9',
  'maj7',
  'maj',
  'min7',
  'min',
  'dim7',
  'dim',
  'aug',
  'sus2',
  'sus4',
  'sus',
  'add11',
  'add13',
  'add2',
  'add4',
  'add6',
  'add9',
  'add',
  'alt',
  'no3',
  'no5',
  '6/9',
  '13',
  '11',
  'b13',
  'b5',
  'b6',
  'b9',
  '#11',
  '#5',
  '#9',
  'M7',
  'M9',
  'M',
  'm',
  '°',
  'º',
  'ø',
  '+',
  '-',
  '(',
  ')',
  ',',
  '2',
  '4',
  '5',
  '6',
  '7',
  '9',
]

/** O sufixo é válido se puder ser fatiado inteiramente em tokens conhecidos. */
function isValidSuffix(suffix: string): boolean {
  let rest = suffix
  outer: while (rest.length > 0) {
    for (const token of SUFFIX_TOKENS) {
      if (rest.startsWith(token)) {
        rest = rest.slice(token.length)
        continue outer
      }
    }
    return false
  }
  return true
}

const ROOT_PATTERN = /^([A-G][#b♯♭x]{0,2})/

/**
 * Interpreta um acorde escrito. Devolve `null` quando o texto não é um acorde —
 * nunca chuta.
 */
export function parseChord(raw: string): Chord | null {
  const text = raw.trim()
  if (text.length === 0) return null

  // Primeiro tentamos o texto inteiro como fundamental + sufixo. Isso resolve
  // sufixos que contêm barra, como `C6/9`, antes de cogitar baixo invertido.
  const whole = splitRootSuffix(text)
  if (whole) return { ...whole, raw: text }

  const slash = text.lastIndexOf('/')
  if (slash <= 0) return null

  const body = splitRootSuffix(text.slice(0, slash))
  const bass = parseNote(text.slice(slash + 1))
  if (!body || bass === null) return null

  return { ...body, bass: bass.name, raw: text }
}

/** Separa fundamental e sufixo, validando o sufixo. */
function splitRootSuffix(text: string): { root: string; suffix: string } | null {
  const rootMatch = ROOT_PATTERN.exec(text)
  if (!rootMatch) return null

  const written = rootMatch[1]!
  const suffix = text.slice(written.length)
  if (!isValidSuffix(suffix)) return null

  // Normaliza ♯/♭/x para a forma ASCII, para que o resto do sistema
  // não precise conhecer as variantes unicode.
  const root = parseNote(written)?.name ?? written
  return { root, suffix }
}

/** Escreve o acorde de volta como texto. */
export function formatChord(chord: Chord): string {
  return chord.root + chord.suffix + (chord.bass ? `/${chord.bass}` : '')
}

/** O texto é um acorde válido? */
export function isChord(raw: string): boolean {
  return parseChord(raw) !== null
}

/**
 * Normaliza a grafia da fundamental e do baixo para a preferência informada,
 * mantendo o sufixo intacto. `Db` com preferência `sharp` vira `C#`.
 */
export function normalizeChord(chord: Chord, accidental: Accidental = 'sharp'): Chord {
  const root = parseNote(chord.root)
  if (!root) return chord

  const bassNote = chord.bass ? parseNote(chord.bass) : null

  return {
    ...chord,
    root: formatPitchClass(root.pitchClass, accidental),
    bass: bassNote ? formatPitchClass(bassNote.pitchClass, accidental) : chord.bass,
  }
}

/** Nome canônico para agrupar acordes enarmônicos (`Db7` e `C#7` colidem aqui). */
export function normalizedChordName(chord: Chord): string {
  return formatChord(normalizeChord(chord, 'sharp'))
}

/** Fundamental do acorde já decomposta, para quem precisa da classe de altura. */
export function chordRootNote(chord: Chord): Note | null {
  return parseNote(chord.root)
}
