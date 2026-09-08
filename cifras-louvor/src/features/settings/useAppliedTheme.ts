import { useEffect } from 'react'

import { usePlayerStore } from '@/features/player/usePlayerStore'

import { useSettingsStore } from './useSettingsStore'

/**
 * Aplica tema e preferências de leitura no <html>.
 * `stage` vence qualquer preferência: no palco o fundo é sempre preto.
 */
export function useAppliedTheme() {
  const stageMode = usePlayerStore((s) => s.stageMode)
  const theme = useSettingsStore((s) => s.theme)
  const fontSize = useSettingsStore((s) => s.fontSize)
  const chordScale = useSettingsStore((s) => s.chordScale)
  const lineHeight = useSettingsStore((s) => s.lineHeight)

  useEffect(() => {
    const root = document.documentElement
    const media = window.matchMedia('(prefers-color-scheme: dark)')

    const apply = () => {
      const dark = theme === 'dark' || (theme === 'system' && media.matches)
      root.classList.toggle('stage', stageMode)
      root.classList.toggle('dark', !stageMode && dark)
    }

    apply()
    media.addEventListener('change', apply)
    return () => media.removeEventListener('change', apply)
  }, [theme, stageMode])

  useEffect(() => {
    const root = document.documentElement
    root.style.setProperty('--song-font-size', `${fontSize}rem`)
    root.style.setProperty('--song-chord-scale', String(chordScale))
    root.style.setProperty('--song-line-height', String(lineHeight))
  }, [fontSize, chordScale, lineHeight])
}
