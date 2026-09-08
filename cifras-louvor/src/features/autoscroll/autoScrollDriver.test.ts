import { describe, expect, it, vi } from 'vitest'

import {
  AutoScrollDriver,
  MAX_FRAME_SECONDS,
  SPEED_MAX,
  SPEED_MIN,
  clampSpeed,
  speedFromBpm,
  type Scroller,
} from './autoScrollDriver'

/** Simula `seconds` de rolagem a 60 quadros por segundo, como no navegador. */
function simulate(driver: AutoScrollDriver, seconds: number, fps = 60): number {
  let total = 0
  for (let frame = 0; frame < seconds * fps; frame += 1) total += driver.advance(1 / fps)
  return total
}

function makeScroller(atEnd = false) {
  const scrollBy = vi.fn()
  const scroller: Scroller = { scrollBy, isAtEnd: () => atEnd }
  return { scroller, scrollBy }
}

describe('clampSpeed', () => {
  it('mantém a velocidade na faixa útil', () => {
    expect(clampSpeed(0)).toBe(SPEED_MIN)
    expect(clampSpeed(1000)).toBe(SPEED_MAX)
    expect(clampSpeed(42.4)).toBe(42)
  })
})

describe('speedFromBpm', () => {
  it('deriva a velocidade do andamento e da altura da linha', () => {
    // 4/4 a 60 BPM = 4 s por compasso; linha de 40 px ⇒ 10 px/s.
    expect(speedFromBpm(60, 40, 4)).toBe(10)
  })

  it('dobrar o andamento dobra a velocidade', () => {
    expect(speedFromBpm(120, 40, 4)).toBe(20)
  })

  it('não quebra com valores inválidos', () => {
    expect(speedFromBpm(0, 40, 4)).toBe(20)
    expect(speedFromBpm(60, 0, 4)).toBe(20)
  })
})

describe('AutoScrollDriver', () => {
  it('rola aproximadamente a velocidade pedida a cada segundo', () => {
    const { scroller, scrollBy } = makeScroller()
    const driver = new AutoScrollDriver(scroller, 30)

    // A soma de 60 frames acumula erro de ponto flutuante; 1 px de folga.
    expect(simulate(driver, 1)).toBeGreaterThanOrEqual(29)
    expect(scrollBy).toHaveBeenCalled()
  })

  it('acumula frações em vez de perdê-las', () => {
    // A 10 px/s, cada frame de 16 ms pede 0,16 px. Sem acúmulo, `scrollBy`
    // receberia sempre zero e a rolagem lenta simplesmente não aconteceria.
    const { scroller, scrollBy } = makeScroller()
    const driver = new AutoScrollDriver(scroller, 10)

    expect(simulate(driver, 1)).toBeGreaterThanOrEqual(9)
    expect(scrollBy).toHaveBeenCalled()
    expect(scrollBy.mock.calls.every(([pixels]) => Number.isInteger(pixels))).toBe(true)
  })

  it('não rola nada quando a fração ainda não fecha um pixel', () => {
    const { scroller, scrollBy } = makeScroller()
    const driver = new AutoScrollDriver(scroller, 10)

    expect(driver.advance(0.01)).toBe(0)
    expect(scrollBy).not.toHaveBeenCalled()
  })

  it('para no fim do conteúdo', () => {
    const { scroller, scrollBy } = makeScroller(true)
    const driver = new AutoScrollDriver(scroller, 50)

    expect(driver.advance(1)).toBe(0)
    expect(scrollBy).not.toHaveBeenCalled()
  })

  it('limita um frame gigante para não pular a música', () => {
    // Aba que voltou do segundo plano depois de 10 s.
    const { scroller } = makeScroller()
    const driver = new AutoScrollDriver(scroller, 100)

    expect(driver.advance(10)).toBe(100 * MAX_FRAME_SECONDS)
  })

  it('esquece a fração ao ser reiniciado', () => {
    const { scroller } = makeScroller()
    const driver = new AutoScrollDriver(scroller, 10)

    driver.advance(0.09)
    driver.reset()
    expect(driver.advance(0.09)).toBe(0)
  })

  it('respeita a mudança de velocidade', () => {
    const { scroller } = makeScroller()
    const driver = new AutoScrollDriver(scroller, 10)

    driver.setSpeed(60)
    expect(driver.getSpeed()).toBe(60)
    expect(simulate(driver, 1)).toBeGreaterThanOrEqual(59)
  })
})
