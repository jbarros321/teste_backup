import { describe, expect, it } from 'vitest'

import { parseChordPro } from '@/lib/chordpro/parser'
import { plainLyrics } from '@/lib/chordpro/types'

import { slugify } from './songsService'

describe('slugify', () => {
  it('gera slug legível, sem acento', () => {
    expect(slugify('Grande é o Senhor')).toBe('grande-e-o-senhor')
    expect(slugify('Sublime Graça')).toBe('sublime-graca')
    expect(slugify('  Ação!  ')).toBe('acao')
  })

  it('não devolve slug vazio', () => {
    expect(slugify('...')).toBe('sem-titulo')
    expect(slugify('')).toBe('sem-titulo')
  })
})

describe('plainLyrics', () => {
  it('extrai a letra sem acordes nem comentários', () => {
    const song = parseChordPro(`{title: Exemplo}
{start_of_verse}
# nota do baixista
[G]Grande é o [Em]Senhor
[C]Digno de louvor
{end_of_verse}
`)
    expect(plainLyrics(song)).toBe('Grande é o Senhor\nDigno de louvor')
  })
})
