import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'

import { MetronomeControl } from './MetronomeControl'
import type { MetronomeController } from './useMetronome'

/** Controller de mentira: a UI é testada sem tocar em AudioContext. */
function makeController(overrides: Partial<MetronomeController> = {}): MetronomeController {
  return {
    isPlaying: false,
    bpm: 80,
    signature: { beats: 4, unit: 4 },
    subdivision: 1,
    accentFirstBeat: true,
    volume: 0.7,
    currentBeat: null,
    toggle: vi.fn(),
    setBpm: vi.fn(),
    nudgeBpm: vi.fn(),
    setSignature: vi.fn(),
    setSubdivision: vi.fn(),
    setAccentFirstBeat: vi.fn(),
    setVolume: vi.fn(),
    tap: vi.fn(),
    reset: vi.fn(),
    ...overrides,
  }
}

describe('MetronomeControl', () => {
  it('mostra o BPM e a faixa de andamento', () => {
    render(<MetronomeControl controller={makeController({ bpm: 125 })} />)
    expect(screen.getByText('125')).toBeInTheDocument()
    expect(screen.getByText(/^Andamento:/)).toHaveTextContent('Andamento: Rápido')
  })

  it('inicia e para pelo mesmo botão', async () => {
    const toggle = vi.fn()
    const { rerender } = render(<MetronomeControl controller={makeController({ toggle })} />)

    await userEvent.click(screen.getByRole('button', { name: 'Iniciar metrônomo' }))
    expect(toggle).toHaveBeenCalledOnce()

    rerender(<MetronomeControl controller={makeController({ isPlaying: true, toggle })} />)
    expect(screen.getByRole('button', { name: 'Parar metrônomo' })).toBeInTheDocument()
  })

  it('ajusta o BPM de um em um', async () => {
    const nudgeBpm = vi.fn()
    render(<MetronomeControl controller={makeController({ nudgeBpm })} />)

    await userEvent.click(screen.getByRole('button', { name: 'Aumentar 1 BPM' }))
    expect(nudgeBpm).toHaveBeenCalledWith(1)

    await userEvent.click(screen.getByRole('button', { name: 'Diminuir 1 BPM' }))
    expect(nudgeBpm).toHaveBeenCalledWith(-1)
  })

  it('não deixa passar dos limites', () => {
    render(<MetronomeControl controller={makeController({ bpm: 240 })} />)
    expect(screen.getByRole('button', { name: 'Aumentar 1 BPM' })).toBeDisabled()
  })

  it('registra a batida do tap tempo', async () => {
    const tap = vi.fn()
    render(<MetronomeControl controller={makeController({ tap })} />)

    await userEvent.click(screen.getByRole('button', { name: 'TAP' }))
    expect(tap).toHaveBeenCalledOnce()
  })

  it('anuncia o tempo que está soando', () => {
    render(<MetronomeControl controller={makeController({ currentBeat: 2 })} />)
    expect(screen.getByRole('status', { name: 'Tempo 3 de 4' })).toBeInTheDocument()
  })

  it('troca o compasso', async () => {
    const setSignature = vi.fn()
    render(<MetronomeControl controller={makeController({ setSignature })} />)

    await userEvent.selectOptions(screen.getByLabelText('Compasso'), '6/8')
    expect(setSignature).toHaveBeenCalledWith({ beats: 6, unit: 8 })
  })

  it('mostra quantos cliques o compasso terá com a subdivisão escolhida', () => {
    render(<MetronomeControl controller={makeController({ subdivision: 2 })} />)
    expect(screen.getByText('8 cliques por compasso.')).toBeInTheDocument()
  })
})
