/**
 * Importação de texto colado → modelo interno.
 *
 * O caso difícil é a cifra tradicional, em que os acordes ficam numa linha
 * ACIMA da letra, alinhados por coluna:
 *
 *     G          Em
 *  Grande é o Senhor
 *
 * A coluna do acorde vira a posição dele dentro da letra. É por isso que
 * expandimos as tabulações antes de qualquer coisa: um `\t` desalinha tudo.
 */
import { parseChordPro } from '@/lib/chordpro/parser'
import { resolveSongKey } from '@/lib/chordpro/transposeSong'
import type { SectionType, SongDocument, SongLine, SongSection } from '@/lib/chordpro/types'
import { isChord } from '@/lib/music/chord'
import { parseKey } from '@/lib/music/key'

import { detectFormat, isChordLine, isTabLine, type SongFormat } from './detectFormat'

export interface ImportResult {
  document: SongDocument
  format: SongFormat
  /** Pontos que o usuário precisa conferir antes de salvar. */
  warnings: string[]
}

/** Rótulos de seção que aparecem em cifras brasileiras. */
const SECTION_WORDS: Record<string, SectionType> = {
  intro: 'intro',
  introducao: 'intro',
  verso: 'verse',
  verse: 'verse',
  estrofe: 'verse',
  'pre-refrao': 'pre_chorus',
  prerefrao: 'pre_chorus',
  refrao: 'chorus',
  coro: 'chorus',
  chorus: 'chorus',
  ponte: 'bridge',
  bridge: 'bridge',
  solo: 'solo',
  interludio: 'interlude',
  final: 'outro',
  finalizacao: 'outro',
  outro: 'outro',
  coda: 'outro',
  tab: 'tab',
  tablatura: 'tab',
}

/** Rótulos de metadado no cabeçalho da cifra. */
const META_PATTERNS: Array<[RegExp, keyof SongDocument]> = [
  [/^\s*(t[íi]tulo|title|m[úu]sica)\s*[:\-]\s*(.+)$/i, 'title'],
  [/^\s*(artista|artist|int[ée]rprete|banda|cantor[a]?)\s*[:\-]\s*(.+)$/i, 'artist'],
  [/^\s*(compositor|composer|autor)\s*[:\-]\s*(.+)$/i, 'composer'],
  [/^\s*(tom|key|tonalidade)\s*[:\-]\s*(.+)$/i, 'key'],
  [/^\s*(bpm|andamento|tempo)\s*[:\-]\s*(.+)$/i, 'bpm'],
  [/^\s*(compasso|time|f[óo]rmula)\s*[:\-]\s*(.+)$/i, 'timeSignature'],
  [/^\s*(capo|capotraste|braçadeira)\s*[:\-]\s*(.+)$/i, 'capo'],
]

/** Tira acentos e pontuação para comparar rótulos de seção. */
function normalizeLabel(text: string): string {
  return text
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[[\]():.]/g, '')
    .replace(/\d+/g, '')
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/\s/g, '-')
}

/** A linha é um rótulo de seção ("Refrão", "[Intro]", "Verso 2")? */
export function parseSectionHeader(line: string): { type: SectionType; name: string } | null {
  const trimmed = line.trim()
  if (trimmed.length === 0 || trimmed.length > 30) return null
  if (isChordLine(trimmed)) return null

  const normalized = normalizeLabel(trimmed)
  const type = SECTION_WORDS[normalized] ?? SECTION_WORDS[normalized.replace(/-/g, '')]
  if (!type) return null

  return { type, name: trimmed.replace(/^\[|\]$/g, '').trim() }
}

/**
 * Substitui tabulações por espaços preservando as colunas (paradas de 8).
 * Sem isto o alinhamento acorde/letra se perde.
 */
export function expandTabs(line: string, tabSize = 8): string {
  let result = ''
  for (const char of line) {
    if (char === '\t') result += ' '.repeat(tabSize - (result.length % tabSize))
    else result += char
  }
  return result
}

/** Onde cada acorde começa na linha de acordes. */
function chordPositions(chordLine: string): Array<{ chord: string; column: number }> {
  const found: Array<{ chord: string; column: number }> = []
  const pattern = /\S+/g

  let match: RegExpExecArray | null
  while ((match = pattern.exec(chordLine)) !== null) {
    const token = match[0].replace(/^[([]+|[)\],.]+$/g, '')
    if (isChord(token)) found.push({ chord: token, column: match.index })
  }

  return found
}

/** Junta a linha de acordes com a linha de letra logo abaixo. */
function mergeChordLine(chordLine: string, lyricLine: string): SongLine {
  const lyrics = lyricLine.trimEnd()
  return {
    kind: 'lyric',
    lyrics,
    chords: chordPositions(chordLine).map(({ chord, column }) => ({
      chord,
      // Um acorde além do fim da letra é normal (final de frase); o
      // renderizador o trata como acorde solto.
      position: Math.min(column, lyrics.length),
    })),
  }
}

/** Linha só com acordes, sem letra embaixo (introdução, passagem). */
function chordOnlyLine(chordLine: string): SongLine {
  return {
    kind: 'lyric',
    lyrics: '',
    chords: chordPositions(chordLine).map(({ chord }, index) => ({ chord, position: index })),
  }
}

function applyMeta(document: SongDocument, line: string): boolean {
  for (const [pattern, field] of META_PATTERNS) {
    const match = pattern.exec(line)
    if (!match) continue

    const value = match[2]!.trim()
    if (field === 'bpm' || field === 'capo') {
      const parsed = Number.parseInt(value, 10)
      if (Number.isFinite(parsed)) document[field] = parsed
    } else if (field === 'key') {
      // Só aceitamos como tom o que realmente é um tom.
      if (parseKey(value)) document.key = value
    } else if (
      field === 'title' ||
      field === 'artist' ||
      field === 'composer' ||
      field === 'timeSignature'
    ) {
      document[field] = value
    }
    return true
  }
  return false
}

/**
 * Lê o bloco de cabeçalho e devolve onde a música começa.
 *
 * Cifras costumam abrir com um bloco curto — título, artista e rótulos como
 * "Tom: G" — encerrado por uma linha em branco. Linhas soltas nesse bloco são
 * título e artista, nessa ordem; é a convenção de praticamente todo site de
 * cifra. Se o bloco não terminar em branco (nem em acorde ou rótulo de seção),
 * desistimos: é música direto, e chutar título aqui estragaria a letra.
 */
function consumeHeader(lines: string[], document: SongDocument, warnings: string[]): number {
  const bare: string[] = []
  let index = 0
  let terminated = false

  while (index < lines.length) {
    const line = lines[index]!
    const trimmed = line.trim()

    if (trimmed.length === 0) {
      // Branco só encerra depois de já termos visto alguma coisa.
      if (index > 0) terminated = true
      break
    }

    if (isChordLine(line) || isTabLine(line) || parseSectionHeader(trimmed)) {
      terminated = true
      break
    }

    if (applyMeta(document, trimmed)) {
      index += 1
      continue
    }

    // No máximo título e artista; mais que isso já é letra.
    if (bare.length >= 2) return 0
    bare.push(trimmed)
    index += 1
  }

  if (!terminated) return 0

  // Título e artista tirados de linhas soltas são dedução nossa, não algo que
  // o arquivo declarou. O usuário precisa saber disso na tela de revisão.
  if (bare[0] !== undefined && !document.title) {
    document.title = bare[0]
    warnings.push(`Usamos "${bare[0]}" como título, tirado da primeira linha. Confira.`)
  }
  if (bare[1] !== undefined && !document.artist) {
    document.artist = bare[1]
    warnings.push(`Usamos "${bare[1]}" como artista, tirado da segunda linha. Confira.`)
  }

  return index
}

/** Converte cifra tradicional (acordes acima da letra) no modelo interno. */
export function parseChordsAbove(text: string): { document: SongDocument; warnings: string[] } {
  const lines = text.replace(/\r\n?/g, '\n').split('\n').map((line) => expandTabs(line))
  const document: SongDocument = { meta: {}, sections: [] }
  const warnings: string[] = []

  let current: SongSection | null = null
  let sawHeader = false

  const push = (line: SongLine) => {
    if (current === null) {
      current = { type: 'none', lines: [] }
      document.sections.push(current)
    }
    current.lines.push(line)
  }

  const bodyStart = consumeHeader(lines, document, warnings)

  for (let index = bodyStart; index < lines.length; index += 1) {
    const line = lines[index]!
    const trimmed = line.trim()

    if (trimmed.length === 0) {
      if (current !== null) push({ kind: 'empty', lyrics: '', chords: [] })
      continue
    }

    const header = parseSectionHeader(trimmed)
    if (header) {
      sawHeader = true
      current = { type: header.type, name: header.name, lines: [] }
      document.sections.push(current)
      continue
    }

    if (isTabLine(line)) {
      if (current === null || current.type !== 'tab') {
        current = { type: 'tab', lines: [] }
        document.sections.push(current)
      }
      current.lines.push({ kind: 'tab', lyrics: line.trimEnd(), chords: [] })
      continue
    }

    if (isChordLine(line)) {
      const next = lines[index + 1]
      const nextIsLyric =
        next !== undefined &&
        next.trim().length > 0 &&
        !isChordLine(next) &&
        !isTabLine(next) &&
        parseSectionHeader(next) === null

      if (nextIsLyric) {
        push(mergeChordLine(line, next))
        index += 1
      } else {
        push(chordOnlyLine(line))
      }
      continue
    }

    push({ kind: 'lyric', lyrics: line.trimEnd(), chords: [] })
  }

  trimEmptyLines(document)

  if (!sawHeader && document.sections.length > 0) {
    warnings.push(
      'Não identificamos as seções (intro, refrão…). Você pode marcá-las depois no editor.',
    )
  }

  return { document, warnings }
}

function trimEmptyLines(document: SongDocument): void {
  for (const section of document.sections) {
    while (section.lines.length > 0 && section.lines[section.lines.length - 1]!.kind === 'empty') {
      section.lines.pop()
    }
    while (section.lines.length > 0 && section.lines[0]!.kind === 'empty') {
      section.lines.shift()
    }
  }
  document.sections = document.sections.filter((section) => section.lines.length > 0)
}

/** Letra pura, sem acorde nenhum. */
function parseLyricsOnly(text: string): { document: SongDocument; warnings: string[] } {
  const document: SongDocument = { meta: {}, sections: [] }
  const section: SongSection = { type: 'none', lines: [] }
  const warnings: string[] = []
  const lines = text.replace(/\r\n?/g, '\n').split('\n')

  for (let index = consumeHeader(lines, document, warnings); index < lines.length; index += 1) {
    const raw = lines[index]!
    section.lines.push(
      raw.trim().length === 0
        ? { kind: 'empty', lyrics: '', chords: [] }
        : { kind: 'lyric', lyrics: raw.trimEnd(), chords: [] },
    )
  }

  document.sections.push(section)
  trimEmptyLines(document)
  return { document, warnings }
}

/**
 * Ponto de entrada da importação: detecta o formato e devolve o modelo já
 * montado, junto com o que o usuário precisa conferir.
 */
export function importSongText(text: string): ImportResult {
  const format = detectFormat(text)
  const warnings: string[] = []

  let document: SongDocument
  if (format === 'chordpro') {
    document = parseChordPro(text)
  } else if (format === 'lyrics_only') {
    const parsed = parseLyricsOnly(text)
    document = parsed.document
    warnings.push(...parsed.warnings)
    warnings.push('Não encontramos acordes — só a letra foi importada.')
  } else {
    const parsed = parseChordsAbove(text)
    document = parsed.document
    warnings.push(...parsed.warnings)
  }

  if (!document.title) {
    const guessed = guessTitle(text)
    if (guessed) {
      document.title = guessed
      warnings.push(`Usamos "${guessed}" como título, tirado da primeira linha. Confira.`)
    } else {
      warnings.push('Não foi possível deduzir o título.')
    }
  }

  if (!document.key) {
    const { key, source, confidence } = resolveSongKey(document)
    if (source === 'detected' && key) {
      document.key = key
      warnings.push(
        `O tom não estava escrito. Deduzimos ${key} pelos acordes (${Math.round(
          confidence * 100,
        )}% de confiança) — confira antes de transpor.`,
      )
    }
  }

  return { document, format, warnings }
}

/** Primeira linha aproveitável como título. */
function guessTitle(text: string): string | null {
  for (const raw of text.replace(/\r\n?/g, '\n').split('\n')) {
    const trimmed = raw.trim()
    if (trimmed.length === 0) continue
    if (trimmed.length > 70) return null
    if (isChordLine(trimmed) || isTabLine(trimmed)) return null
    if (parseSectionHeader(trimmed)) return null
    if (META_PATTERNS.some(([pattern]) => pattern.test(trimmed))) continue
    return trimmed
  }
  return null
}
