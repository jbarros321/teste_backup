/**
 * SongDocument → texto ChordPro.
 *
 * Fecha o ciclo com o parser: o que é lido pode ser gravado de volta sem perda
 * relevante. É o formato que o usuário edita no editor avançado e o que
 * exportamos.
 */
import type { SectionType, SongDocument, SongLine, SongSection } from './types'

const SECTION_DIRECTIVES: Record<Exclude<SectionType, 'none'>, string> = {
  intro: 'intro',
  verse: 'verse',
  pre_chorus: 'pre_chorus',
  chorus: 'chorus',
  bridge: 'bridge',
  solo: 'solo',
  interlude: 'interlude',
  outro: 'outro',
  tab: 'tab',
}

/** Reinsere as marcações `[Acorde]` nas posições guardadas. */
export function serializeLine(line: SongLine): string {
  if (line.kind === 'comment') return `# ${line.lyrics}`
  if (line.kind === 'tab') return line.lyrics
  if (line.kind === 'empty') return ''

  // De trás para frente para que as posições anteriores não se desloquem.
  const ordered = [...line.chords].sort((a, b) => b.position - a.position)
  let text = line.lyrics
  for (const slot of ordered) {
    const at = Math.min(Math.max(slot.position, 0), text.length)
    text = `${text.slice(0, at)}[${slot.chord}]${text.slice(at)}`
  }
  return text
}

function serializeSection(section: SongSection): string[] {
  const body = section.lines.map(serializeLine)
  if (section.type === 'none') return body

  const directive = SECTION_DIRECTIVES[section.type]
  const open = section.name
    ? `{start_of_${directive}: ${section.name}}`
    : `{start_of_${directive}}`
  return [open, ...body, `{end_of_${directive}}`]
}

/** Gera o texto ChordPro da música. */
export function serializeChordPro(song: SongDocument): string {
  const lines: string[] = []

  if (song.title) lines.push(`{title: ${song.title}}`)
  if (song.artist) lines.push(`{artist: ${song.artist}}`)
  if (song.composer) lines.push(`{composer: ${song.composer}}`)
  if (song.key) lines.push(`{key: ${song.key}}`)
  if (song.bpm !== undefined) lines.push(`{tempo: ${song.bpm}}`)
  if (song.timeSignature) lines.push(`{time: ${song.timeSignature}}`)
  if (song.capo !== undefined) lines.push(`{capo: ${song.capo}}`)
  for (const [name, value] of Object.entries(song.meta)) {
    lines.push(`{${name}: ${value}}`)
  }

  for (const section of song.sections) {
    if (lines.length > 0) lines.push('')
    lines.push(...serializeSection(section))
  }

  return `${lines.join('\n')}\n`
}
