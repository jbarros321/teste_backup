/**
 * Descobre em que formato o texto colado está.
 *
 * A distinção que importa: ChordPro tem os acordes embutidos na letra
 * (`[G]Grande`), enquanto a cifra tradicional os põe numa LINHA ACIMA,
 * alinhados por coluna. Os dois pedem tratamentos completamente diferentes.
 */
import { isChord } from '@/lib/music/chord'

export type SongFormat = 'chordpro' | 'chords_above' | 'lyrics_only' | 'tab'

/** Enfeites comuns em linhas de acorde que não são acordes. */
const DECORATIONS = /^(\||%|\/|-|:|\(|\)|x?\d+x?|\(\d+x\)|\.{2,})$/i

/**
 * Classifica um token de uma possível linha de acordes.
 *
 * O enfeite é testado ANTES de tirar os parênteses, senão um token que é só
 * `|` sobraria vazio e derrubaria a linha inteira.
 */
function classifyToken(token: string): 'chord' | 'decoration' | 'other' {
  if (DECORATIONS.test(token)) return 'decoration'

  const cleaned = token.replace(/^[([]+|[)\],.]+$/g, '')
  if (cleaned.length === 0) return 'other'
  if (isChord(cleaned)) return 'chord'
  return DECORATIONS.test(cleaned) ? 'decoration' : 'other'
}

/**
 * A linha é uma linha de acordes?
 *
 * Exige que TODOS os tokens sejam acordes ou enfeites, e ao menos um acorde de
 * verdade. É estrito de propósito: uma letra virando linha de acordes destrói
 * a música toda.
 */
export function isChordLine(line: string): boolean {
  const tokens = line.trim().split(/\s+/).filter(Boolean)
  if (tokens.length === 0) return false

  let realChords = 0
  for (const token of tokens) {
    const kind = classifyToken(token)
    if (kind === 'other') return false
    if (kind === 'chord') realChords += 1
  }

  return realChords > 0
}

/** A linha é uma pauta de tablatura (`e|---0---|`)? */
export function isTabLine(line: string): boolean {
  return /^\s*[eBGDAE][b#]?\s*[|:]/.test(line) && /[-|]{4,}/.test(line)
}

export function detectFormat(text: string): SongFormat {
  const lines = text.replace(/\r\n?/g, '\n').split('\n')
  const nonEmpty = lines.filter((line) => line.trim().length > 0)
  if (nonEmpty.length === 0) return 'lyrics_only'

  // Diretivas ou acordes embutidos: é ChordPro.
  if (/\{\s*[a-z_]+\s*(:[^}]*)?\}/i.test(text)) return 'chordpro'
  const inlineChords = text.match(/\[[^\]\n]{1,12}\]/g) ?? []
  if (inlineChords.filter((match) => isChord(match.slice(1, -1))).length >= 2) return 'chordpro'

  if (nonEmpty.filter(isTabLine).length >= 4) return 'tab'

  const chordLines = nonEmpty.filter(isChordLine).length
  return chordLines >= 2 || chordLines / nonEmpty.length > 0.25 ? 'chords_above' : 'lyrics_only'
}
