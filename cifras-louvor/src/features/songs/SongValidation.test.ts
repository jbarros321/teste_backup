import { describe, expect, it } from 'vitest'

import { parseChordPro } from '@/lib/chordpro/parser'

import { validateSong } from './SongValidation'

const messages = (chordpro: string) =>
  validateSong(parseChordPro(chordpro)).map((warning) => warning.message)

describe('validateSong', () => {
  it('não reclama de uma música bem formada', () => {
    expect(messages('{title: Ok}\n{key: G}\n{tempo: 80}\n[G]Letra [C]boa')).toEqual([])
  })

  it('avisa sobre acorde que não reconhecemos', () => {
    const warnings = messages('{key: G}\n[G]Certo [Xyz]errado')
    expect(warnings.join(' ')).toContain('"Xyz"')
    expect(warnings.join(' ')).toContain('não serão transpostos')
  })

  it('avisa quando o tom foi apenas deduzido', () => {
    expect(messages('[G]Um [Em]dois [C]três [D]quatro').join(' ')).toContain('não foi declarado')
  })

  it('avisa quando não há acorde nenhum', () => {
    expect(messages('{key: G}\nSó letra').join(' ')).toContain('nenhum acorde')
  })

  it('avisa sobre BPM e compasso inválidos', () => {
    const warnings = messages('{key: G}\n{tempo: 999}\n{time: 4-4}\n[G]Letra').join(' ')
    expect(warnings).toContain('BPM fora do razoável')
    expect(warnings).toContain('não parece válido')
  })
})
