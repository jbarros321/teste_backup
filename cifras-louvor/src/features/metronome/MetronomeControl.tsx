import { Minus, Pause, Play, Plus, RotateCcw, Volume2 } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import {
  BPM_MAX,
  BPM_MIN,
  TEMPO_PRESETS,
  formatTimeSignature,
  parseTimeSignature,
  tempoLabel,
  ticksPerBar,
  type Subdivision,
} from '@/lib/audio/metronomeTiming'
import { cn } from '@/lib/utils'

import type { MetronomeController } from './useMetronome'

const SIGNATURES = ['2/4', '3/4', '4/4', '5/4', '6/8', '7/8', '12/8']
const SUBDIVISIONS: Array<{ value: Subdivision; label: string; hint: string }> = [
  { value: 1, label: '♩', hint: 'Só o tempo' },
  { value: 2, label: '♫', hint: 'Colcheias' },
  { value: 3, label: '⅜', hint: 'Tercinas' },
  { value: 4, label: '♬', hint: 'Semicolcheias' },
]

/** Indicador visual: ● no tempo que está soando, ○ nos demais. */
function BeatIndicator({ controller }: { controller: MetronomeController }) {
  const beats = Array.from({ length: controller.signature.beats }, (_, index) => index)

  return (
    <div
      className="flex items-center gap-1.5"
      role="status"
      aria-label={
        controller.currentBeat === null
          ? 'Metrônomo parado'
          : `Tempo ${controller.currentBeat + 1} de ${controller.signature.beats}`
      }
    >
      {beats.map((beat) => (
        <span
          key={beat}
          aria-hidden
          className={cn(
            'size-2.5 rounded-full transition-colors duration-75',
            controller.currentBeat === beat
              ? beat === 0 && controller.accentFirstBeat
                ? 'bg-beat-accent'
                : 'bg-primary'
              : 'bg-beat-normal',
          )}
        />
      ))}
    </div>
  )
}

export function MetronomeControl({ controller }: { controller: MetronomeController }) {
  return (
    <section className="space-y-4 rounded-xl border border-border bg-card p-4">
      <div className="flex flex-wrap items-center gap-3">
        <Button
          type="button"
          size="icon"
          aria-label={controller.isPlaying ? 'Parar metrônomo' : 'Iniciar metrônomo'}
          aria-pressed={controller.isPlaying}
          onClick={controller.toggle}
        >
          {controller.isPlaying ? <Pause aria-hidden /> : <Play aria-hidden />}
        </Button>

        <div className="flex items-center gap-1">
          <Button
            type="button"
            variant="outline"
            size="icon"
            aria-label="Diminuir 1 BPM"
            onClick={() => controller.nudgeBpm(-1)}
            disabled={controller.bpm <= BPM_MIN}
          >
            <Minus aria-hidden />
          </Button>

          <div className="w-20 text-center">
            <span className="block text-2xl font-semibold tabular-nums leading-none">
              {controller.bpm}
            </span>
            <span className="text-[10px] uppercase tracking-wide text-muted-foreground">BPM</span>
          </div>

          <Button
            type="button"
            variant="outline"
            size="icon"
            aria-label="Aumentar 1 BPM"
            onClick={() => controller.nudgeBpm(1)}
            disabled={controller.bpm >= BPM_MAX}
          >
            <Plus aria-hidden />
          </Button>
        </div>

        <Button type="button" variant="secondary" onClick={controller.tap}>
          TAP
        </Button>

        <Button
          type="button"
          variant="ghost"
          size="icon"
          aria-label="Voltar ao BPM da música"
          title="Voltar ao BPM da música"
          onClick={controller.reset}
        >
          <RotateCcw aria-hidden />
        </Button>

        <BeatIndicator controller={controller} />

        <p className="ml-auto text-xs text-muted-foreground" aria-live="polite">
          Andamento: <strong className="text-foreground">{tempoLabel(controller.bpm)}</strong>
        </p>
      </div>

      <div className="space-y-1.5">
        <Label htmlFor="bpm-range" className="sr-only">
          Andamento
        </Label>
        <input
          id="bpm-range"
          type="range"
          className="w-full accent-[var(--primary)]"
          min={BPM_MIN}
          max={BPM_MAX}
          step={1}
          value={controller.bpm}
          onChange={(event) => controller.setBpm(Number(event.target.value))}
        />
        <div className="flex flex-wrap gap-1">
          {TEMPO_PRESETS.map((preset) => (
            <Button
              key={preset.label}
              type="button"
              variant="ghost"
              size="sm"
              className="text-xs"
              onClick={() => controller.setBpm(preset.bpm)}
            >
              {preset.label}
            </Button>
          ))}
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        <div className="space-y-1.5">
          <Label htmlFor="signature">Compasso</Label>
          <select
            id="signature"
            className="h-9 w-full rounded-md border border-input bg-transparent px-2 text-sm"
            value={formatTimeSignature(controller.signature)}
            onChange={(event) => {
              const parsed = parseTimeSignature(event.target.value)
              if (parsed) controller.setSignature(parsed)
            }}
          >
            {SIGNATURES.map((signature) => (
              <option key={signature} value={signature}>
                {signature}
              </option>
            ))}
          </select>
        </div>

        <div className="space-y-1.5">
          <Label>Subdivisão</Label>
          <div className="flex gap-1">
            {SUBDIVISIONS.map((item) => (
              <Button
                key={item.value}
                type="button"
                variant={controller.subdivision === item.value ? 'default' : 'outline'}
                size="icon"
                title={item.hint}
                aria-label={item.hint}
                aria-pressed={controller.subdivision === item.value}
                onClick={() => controller.setSubdivision(item.value)}
              >
                {item.label}
              </Button>
            ))}
          </div>
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="volume" className="flex items-center gap-1.5">
            <Volume2 className="size-4" aria-hidden />
            Volume
          </Label>
          <input
            id="volume"
            type="range"
            className="w-full accent-[var(--primary)]"
            min={0}
            max={1}
            step={0.05}
            value={controller.volume}
            onChange={(event) => controller.setVolume(Number(event.target.value))}
          />
        </div>
      </div>

      <label className="flex items-center gap-2 text-sm">
        <input
          type="checkbox"
          className="size-4 accent-[var(--primary)]"
          checked={controller.accentFirstBeat}
          onChange={(event) => controller.setAccentFirstBeat(event.target.checked)}
        />
        Acentuar o primeiro tempo do compasso
      </label>

      <p className="text-xs text-muted-foreground">
        {ticksPerBar(controller.signature, controller.subdivision)} cliques por compasso.
      </p>
    </section>
  )
}
