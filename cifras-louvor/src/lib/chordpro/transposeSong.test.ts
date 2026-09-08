import { describe, expect, it } from 'vitest'

import { parseChordPro } from './parser'
import { resolveSongKey, transposeSong } from './transposeSong'
import { collectChords } from './types'

const SONG = parseChordPro(`{title: Exemplo}
{key: G}

{start_of_verse}
[G]Grande é o [Em]Senhor
[C]Sua graça [D]permanece
{end_of_verse}
`)

describe('transposeSong', () => {
  it('transpõe todos os acordes e o tom declarado', () => {
    const transposed = transposeSong(SONG, 2)
    expect(transposed.key).toBe('A')
    expect(collectChords(transposed)).toEqual(['A', 'F#m', 'D', 'E'])
  })

  it('usa bemóis quando o tom de destino pede', () => {
    const transposed = transposeSong(SONG, 3)
    expect(transposed.key).toBe('Bb')
    expect(collectChords(transposed)).toEqual(['Bb', 'Gm', 'Eb', 'F'])
  })

  it('preserva as posições dos acordes na letra', () => {
    const transposed = transposeSong(SONG, 2)
    expect(transposed.sections[0]?.lines[0]?.chords.map((slot) => slot.position)).toEqual(
      SONG.sections[0]?.lines[0]?.chords.map((slot) => slot.position),
    )
  })

  it('não altera o documento original', () => {
    const before = collectChords(SONG)
    transposeSong(SONG, 5)
    expect(collectChords(SONG)).toEqual(before)
  })
})

describe('resolveSongKey', () => {
  it('prefere o tom declarado no arquivo', () => {
    expect(resolveSongKey(SONG)).toMatchObject({ key: 'G', source: 'declared', confidence: 1 })
  })

  it('detecta o tom quando não há declaração', () => {
    const song = parseChordPro('[Am]Um [F]dois [C]três [G]quatro [Am]cinco')
    const resolved = resolveSongKey(song)
    expect(resolved.source).toBe('detected')
    expect(resolved.key).toBe('Am')
  })

  it('avisa quando não dá para saber', () => {
    expect(resolveSongKey(parseChordPro('Só letra, sem acorde nenhum'))).toMatchObject({
      key: null,
      source: 'unknown',
    })
  })
})
