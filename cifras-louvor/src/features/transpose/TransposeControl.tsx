import { Minus, Plus, RotateCcw } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { formatKey } from '@/lib/music/key'
import { formatTransposeLabel } from '@/lib/music/transpose'
import { cn } from '@/lib/utils'

import { keyLabel, type SongTranspose } from './useSongTranspose'

export function TransposeControl({
  transpose,
  className,
}: {
  transpose: SongTranspose
  className?: string
}) {
  const { originalKey, currentKey, availableKeys, semitones, isTransposed } = transpose

  return (
    <div className={cn('flex items-center gap-1', className)}>
      <Button
        variant="outline"
        size="icon"
        aria-label="Baixar meio tom"
        title="Baixar meio tom"
        onClick={transpose.transposeDown}
        disabled={!originalKey}
      >
        <Minus aria-hidden />
      </Button>

      <div className="flex flex-col items-center px-1">
        <label className="sr-only" htmlFor="song-key">
          Tom da música
        </label>
        <select
          id="song-key"
          className="h-8 rounded-md border border-input bg-transparent px-2 text-center text-sm font-semibold"
          value={currentKey ? formatKey(currentKey) : ''}
          onChange={(event) => transpose.setKey(event.target.value)}
          disabled={!originalKey}
        >
          {!originalKey && <option value="">Tom desconhecido</option>}
          {availableKeys.map((key) => (
            <option key={formatKey(key)} value={formatKey(key)}>
              {formatKey(key)}
            </option>
          ))}
        </select>
        <span className="mt-0.5 text-[10px] uppercase tracking-wide text-muted-foreground">
          {isTransposed
            ? `${formatTransposeLabel(semitones)} · de ${keyLabel(originalKey)}`
            : 'Tom original'}
        </span>
      </div>

      <Button
        variant="outline"
        size="icon"
        aria-label="Subir meio tom"
        title="Subir meio tom"
        onClick={transpose.transposeUp}
        disabled={!originalKey}
      >
        <Plus aria-hidden />
      </Button>

      <Button
        variant="ghost"
        size="icon"
        aria-label="Voltar ao tom original"
        title="Voltar ao tom original"
        onClick={transpose.reset}
        disabled={!isTransposed}
      >
        <RotateCcw aria-hidden />
      </Button>
    </div>
  )
}
