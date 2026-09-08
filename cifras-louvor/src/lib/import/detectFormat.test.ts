import { describe, expect, it } from 'vitest'

import { detectFormat, isChordLine, isTabLine } from './detectFormat'

describe('isChordLine', () => {
  it('reconhece uma linha de acordes', () => {
    expect(isChordLine('    G       C      D')).toBe(true)
    expect(isChordLine('Am7  F#m7b5  Bb/D')).toBe(true)
    expect(isChordLine('| G | C | D | (2x)')).toBe(true)
  })

  it('não confunde letra com acordes', () => {
    expect(isChordLine('Grande é o Senhor')).toBe(false)
    expect(isChordLine('Digno de todo louvor')).toBe(false)
    expect(isChordLine('Deus é bom')).toBe(false)
    expect(isChordLine('')).toBe(false)
  })
})

describe('isTabLine', () => {
  it('reconhece pauta de tablatura', () => {
    expect(isTabLine('e|----------------|')).toBe(true)
    expect(isTabLine('A|--2-------------|')).toBe(true)
    expect(isTabLine('Grande é o Senhor')).toBe(false)
  })
})

describe('detectFormat', () => {
  it('detecta ChordPro pelas diretivas', () => {
    expect(detectFormat('{title: Exemplo}\nLetra')).toBe('chordpro')
  })

  it('detecta ChordPro pelos acordes embutidos', () => {
    expect(detectFormat('[G]Grande é o [Em]Senhor')).toBe('chordpro')
  })

  it('detecta cifra tradicional com acordes acima', () => {
    expect(
      detectFormat('   G          Em\nGrande é o Senhor\n   C        D\nDigno de louvor'),
    ).toBe('chords_above')
  })

  it('detecta letra sem acorde nenhum', () => {
    expect(detectFormat('Grande é o Senhor\nDigno de todo louvor')).toBe('lyrics_only')
  })

  it('detecta tablatura', () => {
    expect(
      detectFormat('e|---0---|\nB|---1---|\nG|---0---|\nD|---2---|\nA|---3---|'),
    ).toBe('tab')
  })

  it('trata texto vazio', () => {
    expect(detectFormat('   \n\n')).toBe('lyrics_only')
  })
})
