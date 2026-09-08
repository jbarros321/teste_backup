/**
 * Modelo interno da música.
 *
 * Este é o nível 2 dos três do projeto: texto bruto → ESTRUTURA → renderização.
 * Nada aqui é HTML. É esta estrutura que permite transpor, editar, exportar
 * em PDF e, no futuro, gerar outros layouts.
 */

export type SectionType =
  | 'intro'
  | 'verse'
  | 'pre_chorus'
  | 'chorus'
  | 'bridge'
  | 'solo'
  | 'interlude'
  | 'outro'
  | 'tab'
  | 'none'

export type LineKind = 'lyric' | 'comment' | 'tab' | 'empty'

/** Um acorde ancorado numa posição da letra (índice do caractere). */
export interface ChordSlot {
  chord: string
  /** Índice em `lyrics` onde o acorde começa. */
  position: number
}

export interface SongLine {
  kind: LineKind
  lyrics: string
  chords: ChordSlot[]
}

export interface SongSection {
  type: SectionType
  /** Rótulo exibido ("Refrão", "Solo 2"). */
  name?: string
  lines: SongLine[]
}

export interface SongDocument {
  title?: string
  artist?: string
  composer?: string
  /** Tom original como escrito no arquivo ("G", "Em"). */
  key?: string
  bpm?: number
  timeSignature?: string
  capo?: number
  /** Diretivas não mapeadas, preservadas para não perder informação do arquivo. */
  meta: Record<string, string>
  sections: SongSection[]
}

/** Documento vazio, usado como ponto de partida do editor e dos testes. */
export function emptySongDocument(): SongDocument {
  return { meta: {}, sections: [] }
}

/** Todos os acordes da música, na ordem em que aparecem. */
export function collectChords(song: SongDocument): string[] {
  return song.sections.flatMap((section) =>
    section.lines.flatMap((line) => line.chords.map((slot) => slot.chord)),
  )
}

/** Letra sem acordes, uma linha por linha — usada na busca e na exportação. */
export function plainLyrics(song: SongDocument): string {
  return song.sections
    .flatMap((section) =>
      section.lines.filter((line) => line.kind === 'lyric').map((line) => line.lyrics),
    )
    .filter((line) => line.trim().length > 0)
    .join('\n')
}
