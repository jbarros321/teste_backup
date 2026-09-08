/**
 * Motor do metrônomo.
 *
 * O relógio é o do Web Audio, através do Transport do Tone.js — nunca
 * `setInterval`. O timer do JavaScript é interrompido por renderização, por
 * garbage collection e por aba em segundo plano; um metrônomo assim atrasa e
 * some no meio do louvor. O Transport agenda os eventos no clock do áudio, que
 * roda numa thread própria.
 */
import * as Tone from 'tone'

import {
  DEFAULT_TIME_SIGNATURE,
  clampBpm,
  tickIntervalSeconds,
  tickKind,
  beatOfBar,
  type Subdivision,
  type TickKind,
  type TimeSignature,
} from './metronomeTiming'

export interface MetronomeState {
  bpm: number
  signature: TimeSignature
  subdivision: Subdivision
  volume: number
  accentFirstBeat: boolean
}

/** Avisado a cada clique, para a interface piscar o tempo certo. */
export interface TickEvent {
  /** Tempo do compasso, base 0. */
  beat: number
  kind: TickKind
}

/** Timbre de cada tipo de clique: agudo e forte no primeiro tempo. */
const VOICES: Record<TickKind, { frequency: number; gain: number; duration: number }> = {
  accent: { frequency: 1760, gain: 1, duration: 0.035 },
  beat: { frequency: 1174, gain: 0.62, duration: 0.03 },
  subdivision: { frequency: 880, gain: 0.3, duration: 0.02 },
}

export class MetronomeEngine {
  private state: MetronomeState = {
    bpm: 80,
    signature: DEFAULT_TIME_SIGNATURE,
    subdivision: 1,
    volume: 0.7,
    accentFirstBeat: true,
  }

  private gain: Tone.Gain | null = null
  private synth: Tone.Synth | null = null
  private eventId: number | null = null
  private tickIndex = 0
  private running = false

  private listeners = new Set<(event: TickEvent) => void>()

  /** Assina os cliques. Devolve a função de cancelamento. */
  onTick(listener: (event: TickEvent) => void): () => void {
    this.listeners.add(listener)
    return () => this.listeners.delete(listener)
  }

  get isRunning(): boolean {
    return this.running
  }

  getState(): MetronomeState {
    return { ...this.state }
  }

  /**
   * Cria o áudio na primeira vez.
   *
   * Só pode ser chamado a partir de um gesto do usuário: navegadores — Safari
   * e iOS em especial — só liberam o AudioContext depois de um clique ou toque.
   */
  private async ensureAudio(): Promise<void> {
    await Tone.start()

    if (this.gain === null) {
      this.gain = new Tone.Gain(this.state.volume).toDestination()
      this.synth = new Tone.Synth({
        oscillator: { type: 'triangle' },
        envelope: { attack: 0.001, decay: 0.02, sustain: 0, release: 0.01 },
      }).connect(this.gain)
    }
  }

  async start(): Promise<void> {
    if (this.running) return
    await this.ensureAudio()

    this.tickIndex = 0
    const transport = Tone.getTransport()
    transport.bpm.value = this.state.bpm

    this.eventId = transport.scheduleRepeat(
      (time) => this.tick(time),
      tickIntervalSeconds(this.state.bpm, this.state.subdivision),
      0,
    )

    transport.start()
    this.running = true
  }

  stop(): void {
    if (!this.running) return

    const transport = Tone.getTransport()
    if (this.eventId !== null) {
      transport.clear(this.eventId)
      this.eventId = null
    }
    transport.stop()
    transport.position = 0

    this.tickIndex = 0
    this.running = false
  }

  async toggle(): Promise<void> {
    if (this.running) this.stop()
    else await this.start()
  }

  /** Toca um clique e avisa a interface no frame correspondente. */
  private tick(time: number): void {
    const { signature, subdivision, accentFirstBeat } = this.state
    const kind = tickKind(this.tickIndex, signature, subdivision, accentFirstBeat)
    const beat = beatOfBar(this.tickIndex, signature, subdivision)
    this.tickIndex += 1

    const voice = VOICES[kind]
    this.synth?.triggerAttackRelease(voice.frequency, voice.duration, time, voice.gain)

    // Tone.Draw sincroniza o visual com o áudio: o número pisca no tempo que
    // soou, não no frame em que o JavaScript conseguiu rodar.
    Tone.getDraw().schedule(() => {
      for (const listener of this.listeners) listener({ beat, kind })
    }, time)
  }

  /** Mudar o andamento com o metrônomo tocando reagenda sem parar o som. */
  setBpm(bpm: number): void {
    this.state.bpm = clampBpm(bpm)
    if (this.running) this.reschedule()
  }

  setSubdivision(subdivision: Subdivision): void {
    this.state.subdivision = subdivision
    if (this.running) this.reschedule()
  }

  setTimeSignature(signature: TimeSignature): void {
    this.state.signature = signature
    this.tickIndex = 0
  }

  setAccentFirstBeat(accent: boolean): void {
    this.state.accentFirstBeat = accent
  }

  setVolume(volume: number): void {
    this.state.volume = Math.min(1, Math.max(0, volume))
    if (this.gain) this.gain.gain.rampTo(this.state.volume, 0.05)
  }

  private reschedule(): void {
    const transport = Tone.getTransport()
    if (this.eventId !== null) transport.clear(this.eventId)

    transport.bpm.value = this.state.bpm
    this.tickIndex = 0
    this.eventId = transport.scheduleRepeat(
      (time) => this.tick(time),
      tickIntervalSeconds(this.state.bpm, this.state.subdivision),
      Tone.now(),
    )
  }

  /** Solta os recursos de áudio. */
  dispose(): void {
    this.stop()
    this.synth?.dispose()
    this.gain?.dispose()
    this.synth = null
    this.gain = null
    this.listeners.clear()
  }
}

/**
 * Um metrônomo por aplicação: dois AudioContext disputando o mesmo alto-falante
 * produzem cliques desencontrados.
 */
let shared: MetronomeEngine | null = null

export function getMetronome(): MetronomeEngine {
  shared ??= new MetronomeEngine()
  return shared
}
