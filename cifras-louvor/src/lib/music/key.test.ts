import { describe, expect, it } from 'vitest'

import { detectKey, diatonicChords, formatKey, keysForMode, parseKey, preferredAccidental } from './key'

describe('parseKey', () => {
  it('lê tons maiores e menores', () => {
    expect(parseKey('G')).toEqual({ tonic: 'G', mode: 'major' })
    expect(parseKey('Em')).toEqual({ tonic: 'E', mode: 'minor' })
    expect(parseKey('Bbm')).toEqual({ tonic: 'Bb', mode: 'minor' })
    expect(formatKey(parseKey('F#m')!)).toBe('F#m')
  })

  it('rejeita texto que não é tom', () => {
    expect(parseKey('Hm')).toBeNull()
    expect(parseKey('louvor')).toBeNull()
  })
})

describe('grafia preferida', () => {
  it('usa bemóis nos tons de bemol', () => {
    expect(preferredAccidental({ tonic: 'F', mode: 'major' })).toBe('flat')
    expect(preferredAccidental({ tonic: 'Eb', mode: 'major' })).toBe('flat')
    expect(preferredAccidental({ tonic: 'D', mode: 'minor' })).toBe('flat')
  })

  it('usa sustenidos nos tons de sustenido', () => {
    expect(preferredAccidental({ tonic: 'A', mode: 'major' })).toBe('sharp')
    expect(preferredAccidental({ tonic: 'E', mode: 'minor' })).toBe('sharp')
  })

  it('lista os 12 tons de cada modo', () => {
    expect(keysForMode('major')).toHaveLength(12)
    expect(keysForMode('major').map(formatKey)).toContain('Eb')
    expect(keysForMode('minor').map(formatKey)).toContain('F#m')
  })
})

describe('diatonicChords', () => {
  it('gera o campo harmônico maior', () => {
    expect(diatonicChords({ tonic: 'C', mode: 'major' })).toEqual([
      'C', 'Dm', 'Em', 'F', 'G', 'Am', 'Bdim',
    ])
  })

  it('gera o campo harmônico menor', () => {
    expect(diatonicChords({ tonic: 'A', mode: 'minor' })).toEqual([
      'Am', 'Bdim', 'C', 'Dm', 'Em', 'F', 'G',
    ])
  })
})

describe('detectKey', () => {
  it('detecta o tom maior a partir dos acordes', () => {
    expect(formatKey(detectKey(['G', 'Em', 'C', 'D', 'G'])!.key)).toBe('G')
  })

  it('detecta tom menor', () => {
    expect(formatKey(detectKey(['Am', 'F', 'C', 'G', 'Am'])!.key)).toBe('Am')
  })

  it('devolve null sem acordes', () => {
    expect(detectKey([])).toBeNull()
    expect(detectKey(['Amor', 'Deus'])).toBeNull()
  })

  it('reporta confiança entre 0 e 1', () => {
    const detection = detectKey(['C', 'F', 'G', 'Am'])!
    expect(detection.confidence).toBeGreaterThan(0)
    expect(detection.confidence).toBeLessThanOrEqual(1)
  })
})
