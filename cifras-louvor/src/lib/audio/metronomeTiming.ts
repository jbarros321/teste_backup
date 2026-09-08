/**
 * Cálculos do metrônomo.
 *
 * Fica separado do áudio de propósito: aqui não há AudioContext, então tudo
 * isto é testável. O `metronome.ts` só cuida de fazer barulho na hora certa.
 */

export const BPM_MIN = 40
export const BPM_MAX = 240

/** Subdivisões por tempo: 1 = só o tempo, 2 = colcheias, 3 = tercinas, 4 = semicolcheias. */
export type Subdivision = 1 | 2 | 3 | 4

/** O que soa em cada clique. */
export type TickKind = 'accent' | 'beat' | 'subdivision'

export interface TimeSignature {
  /** Tempos por compasso. */
  beats: number
  /** Figura que leva o tempo (4 = semínima, 8 = colcheia). */
  unit: number
}

export const DEFAULT_TIME_SIGNATURE: TimeSignature = { beats: 4, unit: 4 }

/** Faixas de andamento com o nome que o músico usa. */
export const TEMPO_PRESETS = [
  { label: 'Muito lento', min: 40, max: 60, bpm: 50 },
  { label: 'Lento', min: 60, max: 80, bpm: 70 },
  { label: 'Moderado', min: 80, max: 110, bpm: 95 },
  { label: 'Rápido', min: 110, max: 140, bpm: 125 },
  { label: 'Muito rápido', min: 140, max: 180, bpm: 160 },
] as const

export function clampBpm(bpm: number): number {
  if (!Number.isFinite(bpm)) return 80
  return Math.min(BPM_MAX, Math.max(BPM_MIN, Math.round(bpm)))
}

/** Nome da faixa de andamento em que este BPM cai. */
export function tempoLabel(bpm: number): string {
  const preset = TEMPO_PRESETS.find((item) => bpm >= item.min && bpm < item.max)
  return preset?.label ?? (bpm < 40 ? 'Muito lento' : 'Muito rápido')
}

/** Lê "4/4", "6/8", "3/4". Devolve `null` se não for compasso válido. */
export function parseTimeSignature(text: string | null | undefined): TimeSignature | null {
  if (!text) return null

  const match = /^\s*(\d{1,2})\s*\/\s*(\d{1,2})\s*$/.exec(text)
  if (!match) return null

  const beats = Number.parseInt(match[1]!, 10)
  const unit = Number.parseInt(match[2]!, 10)

  if (beats < 1 || beats > 32) return null
  if (![1, 2, 4, 8, 16].includes(unit)) return null

  return { beats, unit }
}

export function formatTimeSignature({ beats, unit }: TimeSignature): string {
  return `${beats}/${unit}`
}

/**
 * Intervalo entre cliques, em segundos.
 *
 * O BPM se refere sempre ao tempo escrito no compasso — em 6/8 a 120, cada
 * colcheia dura meio segundo. É assim que um metrônomo de verdade funciona.
 */
export function tickIntervalSeconds(bpm: number, subdivision: Subdivision): number {
  return 60 / clampBpm(bpm) / subdivision
}

/** Quantos cliques cabem num compasso. */
export function ticksPerBar(signature: TimeSignature, subdivision: Subdivision): number {
  return signature.beats * subdivision
}

/**
 * O que este clique é: primeiro tempo do compasso, tempo comum, ou subdivisão.
 * `accentFirstBeat` desligado transforma o primeiro tempo num tempo comum.
 */
export function tickKind(
  tickIndex: number,
  signature: TimeSignature,
  subdivision: Subdivision,
  accentFirstBeat = true,
): TickKind {
  const perBar = ticksPerBar(signature, subdivision)
  const position = ((tickIndex % perBar) + perBar) % perBar

  if (position % subdivision !== 0) return 'subdivision'
  if (position === 0 && accentFirstBeat) return 'accent'
  return 'beat'
}

/** Em que tempo do compasso (base 0) este clique cai. */
export function beatOfBar(
  tickIndex: number,
  signature: TimeSignature,
  subdivision: Subdivision,
): number {
  const perBar = ticksPerBar(signature, subdivision)
  const position = ((tickIndex % perBar) + perBar) % perBar
  return Math.floor(position / subdivision)
}

/**
 * Tap tempo: calcula o BPM pelos intervalos entre as batidas do usuário.
 *
 * Usa a média das últimas batidas e descarta pausas longas — quando alguém
 * para e recomeça, o intervalo gigante não pode contaminar a conta.
 */
export function bpmFromTaps(timestamps: number[], maxGapMs = 2500): number | null {
  if (timestamps.length < 2) return null

  const intervals: number[] = []
  for (let index = 1; index < timestamps.length; index += 1) {
    const gap = timestamps[index]! - timestamps[index - 1]!
    if (gap > 0 && gap <= maxGapMs) intervals.push(gap)
  }

  if (intervals.length === 0) return null

  const average = intervals.reduce((total, value) => total + value, 0) / intervals.length
  return clampBpm(60_000 / average)
}

/** Mantém só as batidas recentes o bastante para contarem juntas. */
export function pruneTaps(timestamps: number[], now: number, maxGapMs = 2500, keep = 5): number[] {
  const recent = timestamps.filter((time) => now - time <= maxGapMs)
  return [...recent, now].slice(-keep)
}
