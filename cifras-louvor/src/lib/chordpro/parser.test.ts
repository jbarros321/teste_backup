import { describe, expect, it } from 'vitest'

import { parseChordPro, parseLine } from './parser'
import { serializeChordPro, serializeLine } from './serializer'
import { collectChords } from './types'

const SAMPLE = `{title: Grande é o Senhor}
{artist: Ministério Exemplo}
{key: G}
{tempo: 78}
{time: 4/4}

{start_of_verse}
[G]Grande é o Senhor
[Em]Digno de todo louvor
{end_of_verse}

{start_of_chorus: Refrão}
[C]Sua graça permanece [D]para sempre
{end_of_chorus}
`

describe('parseLine', () => {
  it('separa a letra dos acordes guardando a posição', () => {
    const line = parseLine('[G]Grande é o [Em]Senhor')
    expect(line.lyrics).toBe('Grande é o Senhor')
    expect(line.chords).toEqual([
      { chord: 'G', position: 0 },
      { chord: 'Em', position: 11 },
    ])
  })

  it('aceita linha só de acordes', () => {
    const line = parseLine('[G] [C] [D]')
    expect(line.chords.map((slot) => slot.chord)).toEqual(['G', 'C', 'D'])
  })

  it('aceita linha só de letra', () => {
    expect(parseLine('Sem acordes aqui').chords).toEqual([])
  })

  it('não engasga com colchete sem fechamento', () => {
    const line = parseLine('Isto [não fecha')
    expect(line.lyrics).toBe('Isto [não fecha')
    expect(line.chords).toEqual([])
  })
})

describe('parseChordPro', () => {
  const song = parseChordPro(SAMPLE)

  it('lê os metadados', () => {
    expect(song.title).toBe('Grande é o Senhor')
    expect(song.artist).toBe('Ministério Exemplo')
    expect(song.key).toBe('G')
    expect(song.bpm).toBe(78)
    expect(song.timeSignature).toBe('4/4')
  })

  it('separa as seções e guarda o rótulo', () => {
    expect(song.sections.map((section) => section.type)).toEqual(['verse', 'chorus'])
    expect(song.sections[1]?.name).toBe('Refrão')
  })

  it('coleta todos os acordes na ordem', () => {
    expect(collectChords(song)).toEqual(['G', 'Em', 'C', 'D'])
  })

  it('trata linhas soltas como seção implícita', () => {
    const loose = parseChordPro('[G]Uma linha solta')
    expect(loose.sections).toHaveLength(1)
    expect(loose.sections[0]?.type).toBe('none')
  })

  it('preserva o alinhamento das linhas de tablatura', () => {
    const tab = parseChordPro('{start_of_tab}\ne|---0---|\nB|---1---|\n{end_of_tab}')
    expect(tab.sections[0]?.type).toBe('tab')
    expect(tab.sections[0]?.lines.map((line) => line.lyrics)).toEqual(['e|---0---|', 'B|---1---|'])
  })

  it('guarda diretivas desconhecidas em meta', () => {
    expect(parseChordPro('{ccli: 12345}').meta.ccli).toBe('12345')
  })

  it('lê comentários com # e com {comment}', () => {
    const song = parseChordPro('{start_of_verse}\n# nota do baixista\n{comment: 2x}\n[G]Letra\n{end_of_verse}')
    const kinds = song.sections[0]!.lines.map((line) => line.kind)
    expect(kinds).toEqual(['comment', 'comment', 'lyric'])
  })

  it('aceita as abreviações soc/eoc', () => {
    expect(parseChordPro('{soc}\n[C]Refrão\n{eoc}').sections[0]?.type).toBe('chorus')
  })
})

describe('serializeChordPro', () => {
  it('reinsere os acordes nas posições certas', () => {
    expect(serializeLine(parseLine('[G]Grande é o [Em]Senhor'))).toBe('[G]Grande é o [Em]Senhor')
  })

  it('faz a viagem de ida e volta sem perder informação', () => {
    const original = parseChordPro(SAMPLE)
    const roundTrip = parseChordPro(serializeChordPro(original))
    expect(roundTrip).toEqual(original)
  })
})
