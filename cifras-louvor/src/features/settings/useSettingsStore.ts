import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type ThemePreference = 'light' | 'dark' | 'system'

/** Nomenclatura preferida ao transpor: sustenidos, bemóis ou a do tom de destino. */
export type AccidentalPreference = 'auto' | 'sharp' | 'flat'

export interface SettingsState {
  theme: ThemePreference
  /** Tamanho da fonte da cifra, em rem. */
  fontSize: number
  /** Escala do acorde em relação à letra. */
  chordScale: number
  lineHeight: number
  accidentals: AccidentalPreference
  autoscrollSpeed: number
  metronomeVolume: number
  defaultBpm: number

  setTheme: (theme: ThemePreference) => void
  setAccidentals: (pref: AccidentalPreference) => void
  increaseFontSize: () => void
  decreaseFontSize: () => void
  setFontSize: (size: number) => void
  setAutoscrollSpeed: (speed: number) => void
  setMetronomeVolume: (volume: number) => void
}

export const FONT_SIZE_MIN = 0.75
export const FONT_SIZE_MAX = 3
export const FONT_SIZE_STEP = 0.125

const clamp = (value: number, min: number, max: number) => Math.min(max, Math.max(min, value))

export const useSettingsStore = create<SettingsState>()(
  persist(
    (set) => ({
      theme: 'system',
      fontSize: 1,
      chordScale: 0.85,
      lineHeight: 1.9,
      accidentals: 'auto',
      autoscrollSpeed: 20,
      metronomeVolume: 0.7,
      defaultBpm: 80,

      setTheme: (theme) => set({ theme }),
      setAccidentals: (accidentals) => set({ accidentals }),
      increaseFontSize: () =>
        set((s) => ({ fontSize: clamp(s.fontSize + FONT_SIZE_STEP, FONT_SIZE_MIN, FONT_SIZE_MAX) })),
      decreaseFontSize: () =>
        set((s) => ({ fontSize: clamp(s.fontSize - FONT_SIZE_STEP, FONT_SIZE_MIN, FONT_SIZE_MAX) })),
      setFontSize: (fontSize) => set({ fontSize: clamp(fontSize, FONT_SIZE_MIN, FONT_SIZE_MAX) }),
      setAutoscrollSpeed: (autoscrollSpeed) => set({ autoscrollSpeed: clamp(autoscrollSpeed, 1, 200) }),
      setMetronomeVolume: (metronomeVolume) => set({ metronomeVolume: clamp(metronomeVolume, 0, 1) }),
    }),
    { name: 'cifra-ministerio:settings', version: 1 },
  ),
)
