/**
 * Cálculo da rolagem automática.
 *
 * Separado do `requestAnimationFrame` de propósito: assim a matemática — que é
 * onde os erros doem — pode ser testada sem DOM e sem relógio de verdade.
 *
 * O ponto delicado é o ACÚMULO FRACIONÁRIO. A 10 px/s, um frame de 16 ms pede
 * 0,16 px. `scrollBy(0.16)` é arredondado para zero pelo navegador, e a
 * rolagem lenta simplesmente não acontece. Guardamos a fração e só rolamos
 * quando ela fecha um pixel inteiro.
 */

export const SPEED_MIN = 5
export const SPEED_MAX = 200
export const SPEED_STEP = 5

/**
 * Teto de tempo por chamada. Uma aba que volta do segundo plano entrega um
 * intervalo enorme; sem o teto, a cifra saltaria para o fim de uma vez.
 */
export const MAX_FRAME_SECONDS = 0.25

/** Quem sabe rolar. A interface existe para o teste não precisar de DOM. */
export interface Scroller {
  scrollBy: (pixels: number) => void
  /** Já chegou ao fim do conteúdo? */
  isAtEnd: () => boolean
}

export function clampSpeed(speed: number): number {
  if (!Number.isFinite(speed)) return 20
  return Math.min(SPEED_MAX, Math.max(SPEED_MIN, Math.round(speed)))
}

/**
 * Velocidade derivada do andamento.
 *
 * O modelo: uma linha de cifra vale, em média, um compasso. Então o tempo de
 * uma linha é `beats / (bpm / 60)` segundos, e a velocidade é a altura da linha
 * dividida por esse tempo. É aproximação — a densidade real varia de música
 * para música — e por isso o usuário continua podendo ajustar na mão.
 */
export function speedFromBpm(
  bpm: number,
  lineHeightPx: number,
  beatsPerBar: number,
  linesPerBar = 1,
): number {
  if (bpm <= 0 || lineHeightPx <= 0 || beatsPerBar <= 0) return clampSpeed(20)
  const secondsPerBar = (beatsPerBar * 60) / bpm
  return clampSpeed((lineHeightPx * linesPerBar) / secondsPerBar)
}

export class AutoScrollDriver {
  private remainder = 0
  private readonly scroller: Scroller
  private speed: number

  constructor(scroller: Scroller, speed = 20) {
    this.scroller = scroller
    this.speed = clampSpeed(speed)
  }

  getSpeed(): number {
    return this.speed
  }

  setSpeed(speed: number): void {
    this.speed = clampSpeed(speed)
  }

  /** Zera a fração acumulada — usado ao parar, para não dar um pulo ao voltar. */
  reset(): void {
    this.remainder = 0
  }

  /**
   * Avança o tempo informado e rola o que couber em pixels inteiros.
   * Devolve quantos pixels rolou.
   */
  advance(deltaSeconds: number): number {
    if (deltaSeconds <= 0) return 0
    if (this.scroller.isAtEnd()) {
      this.remainder = 0
      return 0
    }

    // Um frame gigante (aba que voltou do segundo plano) não pode jogar a
    // cifra lá para o fim.
    const delta = Math.min(deltaSeconds, MAX_FRAME_SECONDS)

    this.remainder += this.speed * delta
    const pixels = Math.floor(this.remainder)
    if (pixels <= 0) return 0

    this.remainder -= pixels
    this.scroller.scrollBy(pixels)
    return pixels
  }
}
