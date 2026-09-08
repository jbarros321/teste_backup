import { ChevronsDown, Pause } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { cn } from '@/lib/utils'

import { SPEED_MAX, SPEED_MIN } from './autoScrollDriver'
import type { AutoScrollController } from './useAutoScroll'

export function AutoScrollControl({
  controller,
  compact = false,
  className,
}: {
  controller: AutoScrollController
  /** No modo palco só cabem os controles essenciais. */
  compact?: boolean
  className?: string
}) {
  const toggleButton = (
    <Button
      type="button"
      size="icon"
      variant={controller.isScrolling ? 'default' : 'outline'}
      aria-label={controller.isScrolling ? 'Pausar rolagem' : 'Iniciar rolagem'}
      aria-pressed={controller.isScrolling}
      title="Rolagem automática (S)"
      onClick={controller.toggle}
    >
      {controller.isScrolling ? <Pause aria-hidden /> : <ChevronsDown aria-hidden />}
    </Button>
  )

  if (compact) {
    return (
      <div className={cn('flex items-center gap-1', className)}>
        {toggleButton}
        <Button
          type="button"
          variant="outline"
          size="icon"
          aria-label="Rolar mais devagar"
          onClick={controller.slower}
        >
          −
        </Button>
        <span className="w-10 text-center text-sm tabular-nums">{controller.speed}</span>
        <Button
          type="button"
          variant="outline"
          size="icon"
          aria-label="Rolar mais rápido"
          onClick={controller.faster}
        >
          +
        </Button>
      </div>
    )
  }

  return (
    <section className={cn('space-y-3 rounded-xl border border-border bg-card p-4', className)}>
      <div className="flex flex-wrap items-center gap-3">
        {toggleButton}
        <div className="min-w-0 flex-1">
          <Label htmlFor="scroll-speed">
            Velocidade: {controller.speed} px/s
          </Label>
          <input
            id="scroll-speed"
            type="range"
            className="mt-1 w-full accent-[var(--primary)]"
            min={SPEED_MIN}
            max={SPEED_MAX}
            step={1}
            value={controller.speed}
            onChange={(event) => controller.setSpeed(Number(event.target.value))}
            disabled={controller.syncWithBpm}
          />
        </div>
      </div>

      <label className="flex items-center gap-2 text-sm">
        <input
          type="checkbox"
          className="size-4 accent-[var(--primary)]"
          checked={controller.syncWithBpm}
          onChange={(event) => controller.setSyncWithBpm(event.target.checked)}
        />
        Sincronizar com o BPM
      </label>

      <p className="text-xs text-muted-foreground">
        {controller.syncWithBpm
          ? 'A velocidade acompanha o andamento, estimando uma linha por compasso. Ajuste o BPM para calibrar.'
          : 'Rolar com o dedo ou a roda do mouse pausa a rolagem automática.'}
      </p>
    </section>
  )
}
