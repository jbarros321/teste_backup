import { describe, expect, it } from 'vitest'

import { isDemoSource, songSource } from './songSource'

// Sem VITE_SUPABASE_URL no ambiente de teste, a fonte é sempre a de demonstração.
describe('songSource em modo demonstração', () => {
  it('trata ausência de igreja como demonstração', () => {
    expect(isDemoSource(null)).toBe(true)
  })

  it('lista o acervo de exemplo em ordem alfabética', async () => {
    const songs = await songSource.list(null, '')
    expect(songs.map((song) => song.title)).toEqual([
      'Castelo Forte',
      'Santo, Santo, Santo',
      'Sublime Graça',
    ])
  })

  it('busca por trecho do título, sem diferenciar maiúsculas', async () => {
    const songs = await songSource.list(null, 'sublime')
    expect(songs).toHaveLength(1)
    expect(songs[0]?.key).toBe('G')
  })

  it('busca por artista', async () => {
    const songs = await songSource.list(null, 'Lutero')
    expect(songs.map((song) => song.title)).toEqual(['Castelo Forte'])
  })

  it('devolve a música completa pelo slug', async () => {
    const song = await songSource.get(null, 'santo-santo-santo')
    expect(song?.title).toBe('Santo, Santo, Santo')
    expect(song?.bpm).toBe(84)
    expect(song?.document.sections.length).toBeGreaterThan(0)
  })

  it('devolve null para slug inexistente', async () => {
    expect(await songSource.get(null, 'nao-existe')).toBeNull()
  })
})
