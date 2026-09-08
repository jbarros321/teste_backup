import { describe, expect, it } from 'vitest'

import { collectChords } from '@/lib/chordpro/types'

import { expandTabs, importSongText, parseChordsAbove, parseSectionHeader } from './parseSongText'

/** Uma cifra colada como vem da vida real: metadados, seções, acordes acima. */
const CIFRA = `Sublime Graça
Artista: John Newton
Tom: G
BPM: 72

Intro
G  C  G  D

Verso 1
   G        G7        C
Sublime graça do Senhor
      G          Em
Que um infeliz salvou

Refrão
    C           G
Bem longe eu já cheguei
    Em          D
E cedo estou aqui
`

describe('expandTabs', () => {
  it('preserva as colunas ao trocar tabulação por espaços', () => {
    expect(expandTabs('a\tb')).toBe('a       b')
    expect(expandTabs('\tG')).toBe('        G')
  })
})

describe('parseSectionHeader', () => {
  it('reconhece rótulos de seção em português', () => {
    expect(parseSectionHeader('Refrão')?.type).toBe('chorus')
    expect(parseSectionHeader('[Intro]')?.type).toBe('intro')
    expect(parseSectionHeader('Verso 2')?.type).toBe('verse')
    expect(parseSectionHeader('Ponte')?.type).toBe('bridge')
    expect(parseSectionHeader('Pré-refrão')?.type).toBe('pre_chorus')
  })

  it('não confunde letra nem acorde com rótulo', () => {
    expect(parseSectionHeader('Grande é o Senhor')).toBeNull()
    expect(parseSectionHeader('G  C  D')).toBeNull()
    expect(parseSectionHeader('')).toBeNull()
  })
})

describe('parseChordsAbove', () => {
  const { document } = parseChordsAbove(CIFRA)

  it('lê os metadados do cabeçalho', () => {
    expect(document.title).toBe('Sublime Graça')
    expect(document.artist).toBe('John Newton')
    expect(document.key).toBe('G')
    expect(document.bpm).toBe(72)
  })

  it('separa as seções pelos rótulos', () => {
    expect(document.sections.map((section) => section.type)).toEqual([
      'intro',
      'verse',
      'chorus',
    ])
  })

  it('ancora cada acorde na coluna correspondente da letra', () => {
    const verse = document.sections[1]!
    expect(verse.lines[0]).toMatchObject({
      lyrics: 'Sublime graça do Senhor',
      chords: [
        { chord: 'G', position: 3 },
        { chord: 'G7', position: 12 },
        { chord: 'C', position: 22 },
      ],
    })
  })

  it('trata linha só de acordes como introdução', () => {
    const intro = document.sections[0]!
    expect(intro.lines[0]?.lyrics).toBe('')
    expect(intro.lines[0]?.chords.map((slot) => slot.chord)).toEqual(['G', 'C', 'G', 'D'])
  })

  it('não perde nenhum acorde', () => {
    expect(collectChords(document)).toEqual([
      'G', 'C', 'G', 'D',
      'G', 'G7', 'C', 'G', 'Em',
      'C', 'G', 'Em', 'D',
    ])
  })
})

describe('importSongText', () => {
  it('reconhece ChordPro e não mexe nele', () => {
    const result = importSongText('{title: Já pronto}\n{key: D}\n[D]Letra [A]aqui')
    expect(result.format).toBe('chordpro')
    expect(result.document.title).toBe('Já pronto')
    expect(result.warnings).toEqual([])
  })

  it('importa cifra tradicional', () => {
    const result = importSongText(CIFRA)
    expect(result.format).toBe('chords_above')
    expect(result.document.title).toBe('Sublime Graça')
  })

  it('avisa quando o texto só tem letra', () => {
    const result = importSongText('Minha Música\n\nGrande é o Senhor\nDigno de louvor')
    expect(result.format).toBe('lyrics_only')
    expect(result.warnings.join(' ')).toContain('só a letra')
  })

  it('deduz o título da primeira linha e avisa', () => {
    const result = importSongText('Nome da Música\n\n  G\nLetra qualquer')
    expect(result.document.title).toBe('Nome da Música')
    expect(result.warnings.join(' ')).toContain('como título')
  })

  it('deduz o tom pelos acordes e avisa', () => {
    const result = importSongText('Título Qualquer\n\n G      Em\nUm dois três\n C     D\nQuatro cinco')
    expect(result.document.key).toBe('G')
    expect(result.warnings.join(' ')).toContain('não estava escrito')
  })

  it('avisa quando não identificou as seções', () => {
    const result = importSongText('Título\n\n G\nLetra')
    expect(result.warnings.join(' ')).toContain('Não identificamos as seções')
  })
})
