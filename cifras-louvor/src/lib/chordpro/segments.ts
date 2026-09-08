/**
 * Transforma uma linha da música em "palavras" prontas para renderizar com o
 * acorde EM CIMA da sílaba certa.
 *
 * O problema: uma cifra tradicional alinha por coluna de caractere, o que só
 * funciona em fonte monoespaçada e quebra feio em tela de celular. A solução é
 * quebrar a linha em palavras — cada palavra é indivisível, mas a linha pode
 * quebrar entre elas. Dentro da palavra, cada acorde abre uma nova "parte",
 * o que permite acorde no meio da palavra ("Gran[D]de").
 */
import type { SongLine } from './types'

/** Um pedaço de texto com, no máximo, um acorde ancorado no seu início. */
export interface ChordPart {
  chord: string | null
  text: string
}

/** Conjunto de partes que nunca pode ser quebrado entre linhas. */
export interface LyricWord {
  parts: ChordPart[]
}

/** Índices onde começa uma palavra nova (depois de espaço). */
function wordStarts(lyrics: string): Set<number> {
  const starts = new Set<number>()
  if (lyrics.length > 0) starts.add(0)

  for (let i = 1; i < lyrics.length; i += 1) {
    const previous = lyrics[i - 1]!
    const current = lyrics[i]!
    if (/\s/.test(previous) && !/\s/.test(current)) starts.add(i)
  }

  return starts
}

/**
 * Monta as palavras da linha. Acordes além do fim da letra (comum em linhas de
 * introdução, que só têm acordes) viram palavras próprias sem texto.
 */
export function buildWords(line: SongLine): LyricWord[] {
  const { lyrics } = line
  const chords = [...line.chords].sort((a, b) => a.position - b.position)

  const inside = chords.filter((slot) => slot.position < lyrics.length)
  const trailing = chords.filter((slot) => slot.position >= lyrics.length)

  const starts = wordStarts(lyrics)
  const chordAt = new Map<number, string>()
  for (const slot of inside) {
    const at = Math.max(0, slot.position)
    // Dois acordes na mesma posição: mantemos os dois lado a lado.
    chordAt.set(at, chordAt.has(at) ? `${chordAt.get(at)} ${slot.chord}` : slot.chord)
  }

  const cuts = [...new Set([0, ...starts, ...chordAt.keys()])]
    .filter((index) => index >= 0 && index < lyrics.length)
    .sort((a, b) => a - b)

  const words: LyricWord[] = []
  let current: LyricWord | null = null

  cuts.forEach((cut, index) => {
    const next = cuts[index + 1] ?? lyrics.length
    const part: ChordPart = { chord: chordAt.get(cut) ?? null, text: lyrics.slice(cut, next) }

    if (current === null || starts.has(cut)) {
      current = { parts: [part] }
      words.push(current)
    } else {
      current.parts.push(part)
    }
  })

  for (const slot of trailing) {
    words.push({ parts: [{ chord: slot.chord, text: '' }] })
  }

  return words
}

/** A linha tem algum acorde? Usado para não reservar a faixa de acordes à toa. */
export function hasChords(line: SongLine): boolean {
  return line.chords.length > 0
}
