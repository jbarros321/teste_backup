import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import { parseChordPro } from '@/lib/chordpro/parser'

import { ChordRenderer } from './ChordRenderer'

const SONG = parseChordPro(`{title: Exemplo}
{start_of_verse}
[G]Grande é o [Em]Senhor
{end_of_verse}

{start_of_chorus: Refrão}
Sua [C]graça permanece [D]para sempre
{end_of_chorus}
`)

/** Remonta a letra de cada linha a partir do DOM, ignorando os acordes. */
const lyricLines = (container: HTMLElement) =>
  [...container.querySelectorAll('.song-line')].map((line) =>
    [...line.querySelectorAll('.song-lyric')].map((node) => node.textContent).join(''),
  )

describe('ChordRenderer', () => {
  it('mostra a letra inteira, sem perder nem duplicar caractere', () => {
    const { container } = render(<ChordRenderer song={SONG} />)
    expect(lyricLines(container)).toEqual([
      'Grande é o Senhor',
      'Sua graça permanece para sempre',
    ])
  })

  it('mostra todos os acordes', () => {
    render(<ChordRenderer song={SONG} />)
    for (const chord of ['G', 'Em', 'C', 'D']) {
      expect(screen.getAllByText(chord).length).toBeGreaterThan(0)
    }
  })

  it('rotula as seções em português', () => {
    render(<ChordRenderer song={SONG} />)
    expect(screen.getByText('Verso')).toBeInTheDocument()
    expect(screen.getByText('Refrão')).toBeInTheDocument()
  })

  it('coloca cada acorde imediatamente antes da sílaba dele', () => {
    const { container } = render(<ChordRenderer song={SONG} />)
    const parts = [...container.querySelectorAll('.song-part')]
    const withChord = parts.find((part) => part.querySelector('.song-chord')?.textContent === 'Em')
    expect(withChord?.querySelector('.song-lyric')?.textContent).toBe('Senhor')
  })

  it('deixa os acordes clicáveis e acessíveis quando há handler', async () => {
    const onChordClick = vi.fn()
    render(<ChordRenderer song={SONG} onChordClick={onChordClick} />)

    await userEvent.click(screen.getByRole('button', { name: 'Acorde Em' }))
    expect(onChordClick).toHaveBeenCalledWith('Em')
  })

  it('não cria botões quando não há handler', () => {
    render(<ChordRenderer song={SONG} />)
    expect(screen.queryAllByRole('button')).toHaveLength(0)
  })
})
