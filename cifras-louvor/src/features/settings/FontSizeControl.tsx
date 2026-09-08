import { AArrowDown, AArrowUp } from 'lucide-react'

import { Button } from '@/components/ui/button'

import { FONT_SIZE_MAX, FONT_SIZE_MIN, useSettingsStore } from './useSettingsStore'

export function FontSizeControl() {
  const fontSize = useSettingsStore((s) => s.fontSize)
  const decrease = useSettingsStore((s) => s.decreaseFontSize)
  const increase = useSettingsStore((s) => s.increaseFontSize)

  return (
    <div className="flex items-center gap-1" role="group" aria-label="Tamanho da letra">
      <Button
        variant="outline"
        size="icon"
        aria-label="Diminuir a letra"
        title="Diminuir a letra"
        onClick={decrease}
        disabled={fontSize <= FONT_SIZE_MIN}
      >
        <AArrowDown aria-hidden />
      </Button>
      <Button
        variant="outline"
        size="icon"
        aria-label="Aumentar a letra"
        title="Aumentar a letra"
        onClick={increase}
        disabled={fontSize >= FONT_SIZE_MAX}
      >
        <AArrowUp aria-hidden />
      </Button>
    </div>
  )
}
