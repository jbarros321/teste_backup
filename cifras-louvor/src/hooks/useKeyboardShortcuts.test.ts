import { renderHook } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'

import { useKeyboardShortcuts } from './useKeyboardShortcuts'

function press(key: string, options: KeyboardEventInit = {}, target: EventTarget = window) {
  const event = new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true, ...options })
  target.dispatchEvent(event)
  return event
}

describe('useKeyboardShortcuts', () => {
  it('dispara o atalho registrado', () => {
    const handler = vi.fn()
    renderHook(() => useKeyboardShortcuts([{ key: 'm', handler }]))

    press('m')
    expect(handler).toHaveBeenCalledOnce()
  })

  it('não diferencia maiúscula de minúscula', () => {
    const handler = vi.fn()
    renderHook(() => useKeyboardShortcuts([{ key: 'm', handler }]))

    press('M')
    expect(handler).toHaveBeenCalledOnce()
  })

  it('ignora a tecla enquanto o usuário digita num campo', () => {
    const handler = vi.fn()
    renderHook(() => useKeyboardShortcuts([{ key: 'm', handler }]))

    const input = document.createElement('input')
    document.body.append(input)
    press('m', {}, input)

    expect(handler).not.toHaveBeenCalled()
    input.remove()
  })

  it('não rouba atalhos do sistema (Cmd/Ctrl)', () => {
    const handler = vi.fn()
    renderHook(() => useKeyboardShortcuts([{ key: 's', handler }]))

    press('s', { metaKey: true })
    expect(handler).not.toHaveBeenCalled()
  })

  it('só repete quando o atalho permite', () => {
    const once = vi.fn()
    const repeatable = vi.fn()
    renderHook(() =>
      useKeyboardShortcuts([
        { key: 'm', handler: once },
        { key: 'ArrowUp', handler: repeatable, allowRepeat: true },
      ]),
    )

    press('m', { repeat: true })
    press('ArrowUp', { repeat: true })

    expect(once).not.toHaveBeenCalled()
    expect(repeatable).toHaveBeenCalledOnce()
  })

  it('impede a rolagem padrão do espaço', () => {
    renderHook(() => useKeyboardShortcuts([{ key: ' ', handler: vi.fn() }]))
    expect(press(' ').defaultPrevented).toBe(true)
  })

  it('não faz nada quando desabilitado', () => {
    const handler = vi.fn()
    renderHook(() => useKeyboardShortcuts([{ key: 'm', handler }], false))

    press('m')
    expect(handler).not.toHaveBeenCalled()
  })
})
