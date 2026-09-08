import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createRef } from 'react'
import { describe, expect, it, vi } from 'vitest'

import type { AutoScrollController } from '@/features/autoscroll/useAutoScroll'
import type { MetronomeController } from '@/features/metronome/useMetronome'
import { parseChordPro } from '@/lib/chordpro/parser'
import { parseKey } from '@/lib/music/key'

import { StageMode } from './StageMode'

const SONG = parseChordPro('{title: Exemplo}\n{key: G}\n[G]Grande é o [Em]Senhor')

function makeProps(overrides: Record<string, unknown> = {}) {
  const metronome: MetronomeController = {
    isPlaying: false,
    bpm: 78,
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
  }

  const autoScroll: AutoScrollController = {
    isScrolling: false,
    speed: 20,
    syncWithBpm: false,
    toggle: vi.fn(),
    stop: vi.fn(),
    setSpeed: vi.fn(),
    faster: vi.fn(),
    slower: vi.fn(),
    setSyncWithBpm: vi.fn(),
  }

  return {
    title: 'Exemplo',
    transpose: {
      song: SONG,
      originalKey: parseKey('G'),
      keySource: 'declared' as const,
      keyConfidence: 1,
      currentKey: parseKey('G'),
      semitones: 0,
      isTransposed: false,
      availableKeys: [],
      transposeUp: vi.fn(),
      transposeDown: vi.fn(),
      setKey: vi.fn(),
      reset: vi.fn(),
    },
    metronome,
    autoScroll,
    scrollRef: createRef<HTMLDivElement>(),
    isFullscreen: false,
    fullscreenSupported: true,
    onToggleFullscreen: vi.fn(),
    onExit: vi.fn(),
    ...overrides,
  }
}

describe('StageMode', () => {
  it('mostra tom, BPM e compasso em destaque', () => {
    render(<StageMode {...makeProps()} />)

    // Escopado à barra superior: "G" também aparece como acorde na cifra.
    const bar = within(screen.getByRole('banner'))
    expect(bar.getByText('G')).toBeInTheDocument()
    expect(bar.getByText('78')).toBeInTheDocument()
    expect(bar.getByText('4/4')).toBeInTheDocument()
  })

  it('mostra a cifra', () => {
    render(<StageMode {...makeProps()} />)
    expect(screen.getByText('Em')).toBeInTheDocument()
  })

  it('só oferece os controles essenciais para tocar', () => {
    render(<StageMode {...makeProps()} />)

    for (const label of [
      'Iniciar metrônomo',
      'Aumentar BPM',
      'Diminuir BPM',
      'Subir meio tom',
      'Baixar meio tom',
      'Iniciar rolagem',
      'Aumentar a letra',
      'Sair do modo palco',
    ]) {
      expect(screen.getByRole('button', { name: label }), label).toBeInTheDocument()
    }

    // Nada de editar, favoritar ou navegar enquanto se toca.
    expect(screen.queryByRole('button', { name: /editar/i })).not.toBeInTheDocument()
  })

  it('sai do palco pelo botão', async () => {
    const onExit = vi.fn()
    render(<StageMode {...makeProps({ onExit })} />)

    await userEvent.click(screen.getByRole('button', { name: 'Sair do modo palco' }))
    expect(onExit).toHaveBeenCalledOnce()
  })

  it('esconde a tela cheia quando o navegador não suporta', () => {
    render(<StageMode {...makeProps({ fullscreenSupported: false })} />)
    expect(screen.queryByRole('button', { name: 'Tela cheia' })).not.toBeInTheDocument()
  })

  it('anuncia o tempo que está soando', () => {
    const props = makeProps()
    props.metronome.currentBeat = 0
    render(<StageMode {...props} />)
    expect(screen.getByRole('status', { name: 'Tempo 1 de 4' })).toBeInTheDocument()
  })
})
