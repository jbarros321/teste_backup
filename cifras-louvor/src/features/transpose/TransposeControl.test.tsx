import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'

import { ChordRenderer } from '@/features/songs/ChordRenderer'
import { parseChordPro } from '@/lib/chordpro/parser'

import { TransposeControl } from './TransposeControl'
import { useSongTranspose } from './useSongTranspose'

const SONG = parseChordPro(`{title: Exemplo}
{key: G}
{start_of_verse}
[G]Grande é o [Em]Senhor
[C]Sua graça [D]permanece
{end_of_verse}
`)

function Harness() {
  const transpose = useSongTranspose(SONG)
  return (
    <>
      <TransposeControl transpose={transpose} />
      <ChordRenderer song={transpose.song} />
    </>
  )
}

const chords = (container: HTMLElement) =>
  [...container.querySelectorAll('.song-chord')]
    .map((node) => node.textContent?.trim())
    .filter((text) => text && text.length > 0)

describe('TransposeControl', () => {
  it('sobe meio tom e transpõe a cifra inteira', async () => {
    const { container } = render(<Harness />)
    await userEvent.click(screen.getByRole('button', { name: 'Subir meio tom' }))
    expect(chords(container)).toEqual(['Ab', 'Fm', 'Db', 'Eb'])
  })

  it('muda direto para o tom escolhido', async () => {
    const { container } = render(<Harness />)
    await userEvent.selectOptions(screen.getByLabelText('Tom da música'), 'A')
    expect(chords(container)).toEqual(['A', 'F#m', 'D', 'E'])
  })

  it('volta ao tom original', async () => {
    const { container } = render(<Harness />)
    await userEvent.selectOptions(screen.getByLabelText('Tom da música'), 'Bb')
    await userEvent.click(screen.getByRole('button', { name: 'Voltar ao tom original' }))
    expect(chords(container)).toEqual(['G', 'Em', 'C', 'D'])
    expect(screen.getByText('Tom original')).toBeInTheDocument()
  })

  it('mostra o deslocamento aplicado', async () => {
    render(<Harness />)
    await userEvent.click(screen.getByRole('button', { name: 'Baixar meio tom' }))
    expect(screen.getByText('−1 · de G')).toBeInTheDocument()
  })
})
