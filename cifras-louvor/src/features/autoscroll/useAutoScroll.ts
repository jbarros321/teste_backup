import { useCallback, useEffect, useRef, useState, type RefObject } from 'react'

import { useSettingsStore } from '@/features/settings/useSettingsStore'

import {
  AutoScrollDriver,
  SPEED_MAX,
  SPEED_MIN,
  SPEED_STEP,
  clampSpeed,
  speedFromBpm,
  type Scroller,
} from './autoScrollDriver'

export interface AutoScrollController {
  isScrolling: boolean
  speed: number
  /** Velocidade amarrada ao andamento em vez de escolhida na mão. */
  syncWithBpm: boolean
  toggle: () => void
  stop: () => void
  setSpeed: (speed: number) => void
  faster: () => void
  slower: () => void
  setSyncWithBpm: (sync: boolean) => void
}

/** Rola a janela ou um elemento, conforme a tela em que estamos. */
function makeScroller(element: HTMLElement | null): Scroller {
  if (element) {
    return {
      scrollBy: (pixels) => element.scrollBy(0, pixels),
      isAtEnd: () => element.scrollTop + element.clientHeight >= element.scrollHeight - 1,
    }
  }

  return {
    scrollBy: (pixels) => window.scrollBy(0, pixels),
    isAtEnd: () =>
      window.scrollY + window.innerHeight >= document.documentElement.scrollHeight - 1,
  }
}

/**
 * Rolagem automática da cifra.
 *
 * O laço é `requestAnimationFrame`, não `setInterval`: ele acompanha a taxa de
 * atualização real da tela, e o navegador o suspende quando a aba sai de foco —
 * exatamente o que queremos aqui, ao contrário do metrônomo.
 */
export function useAutoScroll(
  containerRef: RefObject<HTMLElement | null>,
  bpm: number | null,
  beatsPerBar: number,
): AutoScrollController {
  const storedSpeed = useSettingsStore((state) => state.autoscrollSpeed)
  const setStoredSpeed = useSettingsStore((state) => state.setAutoscrollSpeed)

  const [isScrolling, setIsScrolling] = useState(false)
  const [syncWithBpm, setSyncWithBpm] = useState(false)

  const driverRef = useRef<AutoScrollDriver | null>(null)
  const frameRef = useRef<number | null>(null)
  const lastTimeRef = useRef<number | null>(null)

  const stop = useCallback(() => {
    if (frameRef.current !== null) cancelAnimationFrame(frameRef.current)
    frameRef.current = null
    lastTimeRef.current = null
    driverRef.current?.reset()
    setIsScrolling(false)
  }, [])

  const start = useCallback(() => {
    const scroller = makeScroller(containerRef.current)
    driverRef.current = new AutoScrollDriver(scroller, storedSpeed)

    const frame = (now: number) => {
      const last = lastTimeRef.current
      lastTimeRef.current = now

      if (last !== null) {
        driverRef.current!.advance((now - last) / 1000)
        if (scroller.isAtEnd()) {
          stop()
          return
        }
      }

      frameRef.current = requestAnimationFrame(frame)
    }

    frameRef.current = requestAnimationFrame(frame)
    setIsScrolling(true)
  }, [containerRef, storedSpeed, stop])

  // Rolou na mão? A intenção do usuário vence a automática.
  useEffect(() => {
    if (!isScrolling) return

    const target = containerRef.current ?? window
    const onManualScroll = () => stop()

    target.addEventListener('wheel', onManualScroll, { passive: true })
    target.addEventListener('touchstart', onManualScroll, { passive: true })

    return () => {
      target.removeEventListener('wheel', onManualScroll)
      target.removeEventListener('touchstart', onManualScroll)
    }
  }, [isScrolling, containerRef, stop])

  useEffect(() => stop, [stop])

  const setSpeed = useCallback(
    (speed: number) => {
      const value = clampSpeed(speed)
      setStoredSpeed(value)
      driverRef.current?.setSpeed(value)
    },
    [setStoredSpeed],
  )

  // Com a sincronia ligada, mudar o BPM muda a rolagem na hora.
  useEffect(() => {
    if (!syncWithBpm || bpm === null) return

    const element = containerRef.current
    const lineHeight = element
      ? Number.parseFloat(getComputedStyle(element).lineHeight) || 32
      : 32

    setSpeed(speedFromBpm(bpm, lineHeight, beatsPerBar))
  }, [syncWithBpm, bpm, beatsPerBar, containerRef, setSpeed])

  return {
    isScrolling,
    speed: storedSpeed,
    syncWithBpm,
    toggle: useCallback(() => {
      if (isScrolling) stop()
      else start()
    }, [isScrolling, start, stop]),
    stop,
    setSpeed,
    faster: useCallback(
      () => setSpeed(Math.min(SPEED_MAX, storedSpeed + SPEED_STEP)),
      [setSpeed, storedSpeed],
    ),
    slower: useCallback(
      () => setSpeed(Math.max(SPEED_MIN, storedSpeed - SPEED_STEP)),
      [setSpeed, storedSpeed],
    ),
    setSyncWithBpm,
  }
}
