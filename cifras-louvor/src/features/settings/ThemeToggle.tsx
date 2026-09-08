import { Monitor, Moon, Sun } from 'lucide-react'

import { Button } from '@/components/ui/button'

import { useSettingsStore, type ThemePreference } from './useSettingsStore'

const ORDER: ThemePreference[] = ['system', 'light', 'dark']
const META: Record<ThemePreference, { icon: typeof Sun; label: string }> = {
  system: { icon: Monitor, label: 'Tema do sistema' },
  light: { icon: Sun, label: 'Tema claro' },
  dark: { icon: Moon, label: 'Tema escuro' },
}

export function ThemeToggle() {
  const theme = useSettingsStore((s) => s.theme)
  const setTheme = useSettingsStore((s) => s.setTheme)
  const { icon: Icon, label } = META[theme]

  return (
    <Button
      variant="ghost"
      size="icon"
      aria-label={`${label}. Clique para alternar.`}
      title={label}
      onClick={() => setTheme(ORDER[(ORDER.indexOf(theme) + 1) % ORDER.length]!)}
    >
      <Icon aria-hidden />
    </Button>
  )
}
