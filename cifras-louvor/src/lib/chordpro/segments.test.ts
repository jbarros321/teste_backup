import { describe, expect, it } from 'vitest'

import { parseLine } from './parser'
import { buildWords } from './segments'

/** Achata as palavras para comparar com facilidade nos testes. */
const flatten = (source: string) =>
  buildWords(parseLine(source)).map((word) =>
    word.parts.map((part) => `${part.chord ?? '-'}|${part.text}`).join('+'),
  )

describe('buildWords', () => {
  it('ancora o acorde no início da palavra', () => {
    expect(flatten('[G]Grande é o Senhor')).toEqual(['G|Grande ', '-|é ', '-|o ', '-|Senhor'])
  })

  it('ancora acordes no meio da linha', () => {
    expect(flatten('Sua [C]graça permanece [D]para sempre')).toEqual([
      '-|Sua ',
      'C|graça ',
      '-|permanece ',
      'D|para ',
      '-|sempre',
    ])
  })

  it('mantém junto o acorde que cai no meio da palavra', () => {
    expect(flatten('Gran[D]de')).toEqual(['-|Gran+D|de'])
  })

  it('trata linha só de acordes (introdução) como acordes soltos', () => {
    expect(flatten('[G] [C] [D]')).toEqual(['G|', 'C|', 'D|'])
  })

  it('preserva os espaços que separam as palavras', () => {
    const words = buildWords(parseLine('[G]um [C]dois'))
    expect(words[0]!.parts[0]!.text).toBe('um ')
  })

  it('coloca acorde além do fim da letra como palavra própria', () => {
    expect(flatten('Fim[G]')).toEqual(['-|Fim', 'G|'])
  })

  it('junta dois acordes na mesma posição', () => {
    expect(flatten('[G][D]Juntos')).toEqual(['G D|Juntos'])
  })

  it('devolve lista vazia para linha vazia', () => {
    expect(flatten('')).toEqual([])
  })

  it('não perde nenhum caractere da letra', () => {
    const source = 'Sua [C]graça per[D]manece para [Em]sempre'
    const lyrics = parseLine(source).lyrics
    const rebuilt = buildWords(parseLine(source))
      .flatMap((word) => word.parts.map((part) => part.text))
      .join('')
    expect(rebuilt).toBe(lyrics)
  })
})
