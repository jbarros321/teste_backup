import { describe, expect, it } from 'vitest'

import { parseKey } from './key'
import {
  formatTransposeLabel,
  semitonesToKey,
  signedSemitones,
  transposeChord,
  transposeChords,
  transposedKeyName,
} from './transpose'

describe('transposeChord', () => {
  it('sobe de G para A mantendo a grafia do tom de destino', () => {
    const semitones = semitonesToKey('G', 'A')
    expect(semitones).toBe(2)

    const options = { targetKey: parseKey('A') }
    expect(transposeChord('G', semitones, options)).toBe('A')
    expect(transposeChord('Em', semitones, options)).toBe('F#m')
    expect(transposeChord('C', semitones, options)).toBe('D')
    expect(transposeChord('D', semitones, options)).toBe('E')
  })

  it('transpõe C para D', () => {
    const options = { targetKey: parseKey('D') }
    expect(transposeChords(['C', 'Am', 'F', 'G'], 2, options)).toEqual(['D', 'Bm', 'G', 'A'])
  })

  it('transpõe F#m para G#m', () => {
    expect(transposeChord('F#m', 2, { targetKey: parseKey('G#m') })).toBe('G#m')
  })

  it('usa bemóis quando o tom de destino é de bemóis', () => {
    const options = { targetKey: parseKey('Eb') }
    expect(transposeChord('C', 3, options)).toBe('Eb')
    expect(transposeChord('Am', 3, options)).toBe('Cm')
    expect(transposeChord('D', 3, options)).toBe('F')
    expect(transposeChord('E', 3, options)).toBe('G')
  })

  it('respeita a preferência explícita do usuário sobre o tom', () => {
    expect(transposeChord('C', 1, { targetKey: parseKey('Db'), spelling: 'sharp' })).toBe('C#')
    expect(transposeChord('C', 1, { targetKey: parseKey('Db'), spelling: 'flat' })).toBe('Db')
  })

  it('preserva o sufixo e transpõe o baixo', () => {
    const options = { targetKey: parseKey('D') }
    expect(transposeChord('Cmaj7/E', 2, options)).toBe('Dmaj7/F#')
    expect(transposeChord('Am7b5', 2, options)).toBe('Bm7b5')
  })

  it('deixa intacto o que não é acorde', () => {
    expect(transposeChord('Amor', 2)).toBe('Amor')
    expect(transposeChord('%', 2)).toBe('%')
  })

  it('volta ao original ao transpor e destranspor', () => {
    for (const chord of ['G', 'Em', 'Cmaj7', 'F#m7b5/C#', 'Bb']) {
      const up = transposeChord(chord, 5, { spelling: 'sharp' })
      const back = transposeChord(up, -5, { spelling: 'sharp' })
      expect(back).toBe(transposeChord(chord, 0, { spelling: 'sharp' }))
    }
  })
})

describe('nome do tom transposto', () => {
  it('escolhe a grafia usual de cada tom', () => {
    expect(transposedKeyName('G', 2)).toBe('A')
    expect(transposedKeyName('C', 1)).toBe('Db')
    expect(transposedKeyName('E', 2)).toBe('F#')
    expect(transposedKeyName('Em', 2)).toBe('F#m')
    expect(transposedKeyName('A', 5)).toBe('D')
  })
})

describe('deslocamento exibido', () => {
  it('mostra descidas como negativas', () => {
    expect(signedSemitones(11)).toBe(-1)
    expect(signedSemitones(2)).toBe(2)
    expect(formatTransposeLabel(0)).toBe('Tom original')
    expect(formatTransposeLabel(2)).toBe('+2')
    expect(formatTransposeLabel(-1)).toBe('−1')
  })
})
