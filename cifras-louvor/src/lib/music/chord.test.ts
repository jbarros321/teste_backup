import { describe, expect, it } from 'vitest'

import { formatChord, isChord, normalizeChord, parseChord } from './chord'

describe('parseChord', () => {
  const valid = [
    'C',
    'Cm',
    'C7',
    'Cmaj7',
    'Cm7',
    'Cadd9',
    'Csus4',
    'C9',
    'C11',
    'C13',
    'C6',
    'C6/9',
    'Cdim',
    'Cdim7',
    'Caug',
    'C+',
    'Cm7b5',
    'C7#9',
    'F#m7',
    'Bbmaj7',
    'Ebsus2',
    'Abm',
    'Gb',
    'D#',
    'C/E',
    'C/G',
    'Am7/G',
    'F#m7b5/C#',
  ]

  it.each(valid)('reconhece %s', (raw) => {
    const chord = parseChord(raw)
    expect(chord, raw).not.toBeNull()
    expect(formatChord(chord!)).toBe(raw)
  })

  const invalid = [
    '',
    'H',
    'Amor',
    'Deus',
    'Cada',
    'Gente',
    'Faz',
    'Bem',
    'Adorar',
    'Senhor',
    'Grande',
    'Digno',
    'Louvor',
    'C/H',
    'C//E',
    'Xm7',
    '7',
  ]

  it.each(invalid)('rejeita %s', (raw) => {
    expect(parseChord(raw), raw).toBeNull()
  })

  it('separa fundamental, sufixo e baixo', () => {
    expect(parseChord('F#m7/C#')).toMatchObject({ root: 'F#', suffix: 'm7', bass: 'C#' })
  })

  it('trata 6/9 como sufixo e não como baixo', () => {
    expect(parseChord('C6/9')).toMatchObject({ root: 'C', suffix: '6/9' })
    expect(parseChord('C6/9')?.bass).toBeUndefined()
  })

  it('aceita os símbolos unicode de alteração', () => {
    expect(isChord('F♯m')).toBe(true)
    expect(parseChord('B♭')?.root).toBe('Bb')
  })
})

describe('normalizeChord', () => {
  it('reescreve a fundamental na grafia pedida', () => {
    expect(formatChord(normalizeChord(parseChord('Db7')!, 'sharp'))).toBe('C#7')
    expect(formatChord(normalizeChord(parseChord('C#m')!, 'flat'))).toBe('Dbm')
  })

  it('reescreve também o baixo', () => {
    expect(formatChord(normalizeChord(parseChord('A/C#')!, 'flat'))).toBe('A/Db')
  })
})
