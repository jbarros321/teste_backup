import { useCallback, useEffect, useState } from 'react'

/**
 * Tela cheia de verdade, quando o navegador permite.
 *
 * O Safari do iPhone não deixa nenhum elemento entrar em tela cheia (só
 * vídeo). Por isso `isSupported` existe e o modo palco NÃO depende disto:
 * lá o palco cobre a viewport por CSS, e a tela cheia é um bônus.
 */
export function useFullscreen() {
  const [isFullscreen, setIsFullscreen] = useState(() => document.fullscreenElement !== null)

  useEffect(() => {
    const sync = () => setIsFullscreen(document.fullscreenElement !== null)
    document.addEventListener('fullscreenchange', sync)
    return () => document.removeEventListener('fullscreenchange', sync)
  }, [])

  const enter = useCallback(async () => {
    try {
      await document.documentElement.requestFullscreen()
    } catch {
      // Recusado pelo navegador: seguimos sem tela cheia.
    }
  }, [])

  const exit = useCallback(async () => {
    if (document.fullscreenElement === null) return
    try {
      await document.exitFullscreen()
    } catch {
      // Idem.
    }
  }, [])

  return {
    isFullscreen,
    isSupported: typeof document.documentElement.requestFullscreen === 'function',
    enter,
    exit,
    toggle: useCallback(() => {
      void (document.fullscreenElement === null ? enter() : exit())
    }, [enter, exit]),
  }
}
