import { useEffect } from 'react'

export interface Shortcut {
  /** `event.key`, como "ArrowUp", " " (espaço) ou "m". Comparado sem maiúsculas. */
  key: string
  handler: (event: KeyboardEvent) => void
  /** Repetir enquanto a tecla fica pressionada (útil para ↑ e ↓). */
  allowRepeat?: boolean
}

/** Digitando num campo, os atalhos não podem roubar a tecla. */
function isTyping(target: EventTarget | null): boolean {
  if (!(target instanceof HTMLElement)) return false
  if (target.isContentEditable) return true
  return ['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName)
}

/**
 * Atalhos de teclado (§15 do escopo).
 *
 * Existem para o músico não precisar tocar na tela enquanto toca — e é por isso
 * que também servem para pedal e teclado Bluetooth, que se apresentam ao
 * navegador como teclado comum (§16).
 */
export function useKeyboardShortcuts(shortcuts: Shortcut[], enabled = true): void {
  useEffect(() => {
    if (!enabled) return

    function onKeyDown(event: KeyboardEvent) {
      if (isTyping(event.target)) return
      if (event.metaKey || event.ctrlKey || event.altKey) return

      for (const shortcut of shortcuts) {
        if (event.key.toLowerCase() !== shortcut.key.toLowerCase()) continue
        if (event.repeat && !shortcut.allowRepeat) return

        event.preventDefault()
        shortcut.handler(event)
        return
      }
    }

    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [shortcuts, enabled])
}
