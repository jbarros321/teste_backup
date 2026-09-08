/**
 * Parser ChordPro → SongDocument.
 *
 * Suporta as diretivas usadas na prática ({title}, {key}, {tempo}, blocos
 * {start_of_*}/{end_of_*} e as abreviações {soc}/{eoc}), comentários com `#`,
 * e as marcações de acorde `[G]` embutidas na letra.
 */
import type {
  ChordSlot,
  SectionType,
  SongDocument,
  SongLine,
  SongSection,
} from './types'

const DIRECTIVE_PATTERN = /^\{\s*([a-zA-Z_]+)\s*(?::\s*([\s\S]*?))?\s*\}$/

/** Nome da diretiva → tipo de seção que ela abre. */
const SECTION_ALIASES: Record<string, SectionType> = {
  intro: 'intro',
  soi: 'intro',
  start_of_intro: 'intro',
  verse: 'verse',
  sov: 'verse',
  start_of_verse: 'verse',
  pre_chorus: 'pre_chorus',
  start_of_pre_chorus: 'pre_chorus',
  chorus: 'chorus',
  soc: 'chorus',
  start_of_chorus: 'chorus',
  bridge: 'bridge',
  sob: 'bridge',
  start_of_bridge: 'bridge',
  solo: 'solo',
  start_of_solo: 'solo',
  interlude: 'interlude',
  start_of_interlude: 'interlude',
  outro: 'outro',
  start_of_outro: 'outro',
  tab: 'tab',
  sot: 'tab',
  start_of_tab: 'tab',
}

const END_DIRECTIVES = new Set([
  'eoi',
  'eov',
  'eoc',
  'eob',
  'eot',
  'end_of_intro',
  'end_of_verse',
  'end_of_pre_chorus',
  'end_of_chorus',
  'end_of_bridge',
  'end_of_solo',
  'end_of_interlude',
  'end_of_outro',
  'end_of_tab',
])

/** Diretivas de metadado → campo do documento. */
const META_ALIASES: Record<string, keyof SongDocument> = {
  title: 'title',
  t: 'title',
  subtitle: 'artist',
  st: 'artist',
  artist: 'artist',
  composer: 'composer',
  key: 'key',
  tempo: 'bpm',
  bpm: 'bpm',
  time: 'timeSignature',
  meter: 'timeSignature',
  capo: 'capo',
}

/**
 * Separa a letra dos acordes de uma linha, guardando a posição de cada acorde
 * em relação ao texto JÁ sem as marcações.
 */
export function parseLine(raw: string): SongLine {
  const chords: ChordSlot[] = []
  let lyrics = ''
  let index = 0

  while (index < raw.length) {
    const char = raw[index]!
    if (char === '[') {
      const close = raw.indexOf(']', index)
      if (close !== -1) {
        chords.push({ chord: raw.slice(index + 1, close).trim(), position: lyrics.length })
        index = close + 1
        continue
      }
    }
    lyrics += char
    index += 1
  }

  const trimmed = lyrics.trimEnd()
  return {
    kind: trimmed.length === 0 && chords.length === 0 ? 'empty' : 'lyric',
    lyrics: trimmed,
    chords,
  }
}

/** Interpreta um texto ChordPro completo. */
export function parseChordPro(source: string): SongDocument {
  const song: SongDocument = { meta: {}, sections: [] }

  let current: SongSection | null = null

  /** Abre uma seção nova e passa a ser o destino das próximas linhas. */
  function openSection(type: SectionType, name?: string): SongSection {
    const section: SongSection = { type, lines: [] }
    if (name) section.name = name
    song.sections.push(section)
    return section
  }

  /** Linhas soltas antes de qualquer `{start_of_*}` caem numa seção implícita. */
  function pushLine(line: SongLine): void {
    if (current === null) current = openSection('none')
    current.lines.push(line)
  }

  for (const rawLine of source.replace(/\r\n?/g, '\n').split('\n')) {
    const line = rawLine.trimEnd()
    const trimmed = line.trim()

    if (trimmed.startsWith('#')) {
      pushLine({ kind: 'comment', lyrics: trimmed.slice(1).trim(), chords: [] })
      continue
    }

    const directive = DIRECTIVE_PATTERN.exec(trimmed)
    if (directive) {
      const name = directive[1]!.toLowerCase()
      const value = directive[2]?.trim() ?? ''

      if (END_DIRECTIVES.has(name)) {
        current = null
        continue
      }

      const sectionType = SECTION_ALIASES[name]
      if (sectionType) {
        current = openSection(sectionType, value || undefined)
        continue
      }

      if (name === 'comment' || name === 'c' || name === 'comment_italic') {
        pushLine({ kind: 'comment', lyrics: value, chords: [] })
        continue
      }

      applyMeta(song, name, value)
      continue
    }

    if (trimmed.length === 0) {
      // Linha em branco fecha a seção implícita, mas preserva o respiro visual.
      if (current && current.type !== 'none') {
        pushLine({ kind: 'empty', lyrics: '', chords: [] })
      } else {
        current = null
      }
      continue
    }

    if (current?.type === 'tab') {
      pushLine({ kind: 'tab', lyrics: line, chords: [] })
      continue
    }

    pushLine(parseLine(line))
  }

  return trimTrailingEmptyLines(song)
}

/** Grava a diretiva no campo certo, ou em `meta` quando não conhecemos. */
function applyMeta(song: SongDocument, name: string, value: string): void {
  const field = META_ALIASES[name]

  if (field === 'bpm' || field === 'capo') {
    const parsed = Number.parseInt(value, 10)
    if (Number.isFinite(parsed)) song[field] = parsed
    return
  }

  switch (field) {
    case 'title':
    case 'artist':
    case 'composer':
    case 'key':
    case 'timeSignature':
      song[field] = value
      return
    default:
      break
  }

  if (value.length > 0) song.meta[name] = value
}

/** Tira as linhas em branco do fim de cada seção. */
function trimTrailingEmptyLines(song: SongDocument): SongDocument {
  for (const section of song.sections) {
    while (section.lines.length > 0 && section.lines[section.lines.length - 1]!.kind === 'empty') {
      section.lines.pop()
    }
  }
  song.sections = song.sections.filter((section) => section.lines.length > 0)
  return song
}
