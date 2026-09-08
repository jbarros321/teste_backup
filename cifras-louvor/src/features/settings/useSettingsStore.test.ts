import { beforeEach, describe, expect, it } from 'vitest'

import { FONT_SIZE_MAX, FONT_SIZE_MIN, useSettingsStore } from './useSettingsStore'

describe('useSettingsStore', () => {
  beforeEach(() => {
    useSettingsStore.setState({ theme: 'system', fontSize: 1 })
  })

  it('alterna o tema', () => {
    useSettingsStore.getState().setTheme('dark')
    expect(useSettingsStore.getState().theme).toBe('dark')
  })

  it('aumenta a fonte em passos', () => {
    useSettingsStore.getState().increaseFontSize()
    expect(useSettingsStore.getState().fontSize).toBeGreaterThan(1)
  })

  it('nunca passa dos limites de fonte', () => {
    const { setFontSize } = useSettingsStore.getState()
    setFontSize(99)
    expect(useSettingsStore.getState().fontSize).toBe(FONT_SIZE_MAX)
    setFontSize(0)
    expect(useSettingsStore.getState().fontSize).toBe(FONT_SIZE_MIN)
  })
})
