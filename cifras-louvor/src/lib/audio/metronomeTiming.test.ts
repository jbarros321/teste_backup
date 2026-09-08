import { describe, expect, it } from 'vitest'

import {
  beatOfBar,
  bpmFromTaps,
  clampBpm,
  formatTimeSignature,
  parseTimeSignature,
  pruneTaps,
  tempoLabel,
  tickIntervalSeconds,
  tickKind,
  ticksPerBar,
} from './metronomeTiming'

describe('clampBpm', () => {
  it('mantém o BPM entre 40 e 240', () => {
    expect(clampBpm(10)).toBe(40)
    expect(clampBpm(500)).toBe(240)
    expect(clampBpm(120)).toBe(120)
    expect(clampBpm(72.6)).toBe(73)
  })

  it('não quebra com valor inválido', () => {
    expect(clampBpm(Number.NaN)).toBe(80)
  })
})

describe('parseTimeSignature', () => {
  it('lê compassos comuns', () => {
    expect(parseTimeSignature('4/4')).toEqual({ beats: 4, unit: 4 })
    expect(parseTimeSignature('6/8')).toEqual({ beats: 6, unit: 8 })
    expect(parseTimeSignature(' 3 / 4 ')).toEqual({ beats: 3, unit: 4 })
  })

  it('rejeita o que não é compasso', () => {
    expect(parseTimeSignature('4-4')).toBeNull()
    expect(parseTimeSignature('4/5')).toBeNull()
    expect(parseTimeSignature('0/4')).toBeNull()
    expect(parseTimeSignature(null)).toBeNull()
  })

  it('escreve de volta', () => {
    expect(formatTimeSignature({ beats: 6, unit: 8 })).toBe('6/8')
  })
})

describe('tickIntervalSeconds', () => {
  it('a 60 BPM cada tempo dura 1 segundo', () => {
    expect(tickIntervalSeconds(60, 1)).toBe(1)
  })

  it('a 120 BPM cada tempo dura meio segundo', () => {
    expect(tickIntervalSeconds(120, 1)).toBe(0.5)
  })

  it('a subdivisão divide o intervalo', () => {
    expect(tickIntervalSeconds(60, 2)).toBe(0.5)
    expect(tickIntervalSeconds(60, 4)).toBe(0.25)
  })
})

describe('tickKind', () => {
  const quatroPorQuatro = { beats: 4, unit: 4 }

  it('acentua o primeiro tempo do compasso', () => {
    const kinds = [0, 1, 2, 3, 4].map((tick) => tickKind(tick, quatroPorQuatro, 1))
    expect(kinds).toEqual(['accent', 'beat', 'beat', 'beat', 'accent'])
  })

  it('marca as subdivisões entre os tempos', () => {
    const kinds = [0, 1, 2, 3].map((tick) => tickKind(tick, quatroPorQuatro, 2))
    expect(kinds).toEqual(['accent', 'subdivision', 'beat', 'subdivision'])
  })

  it('sem acentuação, o primeiro tempo é um tempo comum', () => {
    expect(tickKind(0, quatroPorQuatro, 1, false)).toBe('beat')
  })

  it('funciona em 3/4', () => {
    const kinds = [0, 1, 2, 3].map((tick) => tickKind(tick, { beats: 3, unit: 4 }, 1))
    expect(kinds).toEqual(['accent', 'beat', 'beat', 'accent'])
  })
})

describe('beatOfBar', () => {
  it('conta os tempos do compasso', () => {
    const signature = { beats: 4, unit: 4 }
    expect([0, 1, 2, 3, 4].map((tick) => beatOfBar(tick, signature, 1))).toEqual([0, 1, 2, 3, 0])
  })

  it('agrupa as subdivisões no mesmo tempo', () => {
    const signature = { beats: 4, unit: 4 }
    expect([0, 1, 2, 3].map((tick) => beatOfBar(tick, signature, 2))).toEqual([0, 0, 1, 1])
  })

  it('conta os cliques por compasso', () => {
    expect(ticksPerBar({ beats: 4, unit: 4 }, 2)).toBe(8)
  })
})

describe('tempoLabel', () => {
  it('nomeia a faixa de andamento', () => {
    expect(tempoLabel(50)).toBe('Muito lento')
    expect(tempoLabel(72)).toBe('Lento')
    expect(tempoLabel(95)).toBe('Moderado')
    expect(tempoLabel(125)).toBe('Rápido')
    expect(tempoLabel(200)).toBe('Muito rápido')
  })
})

describe('bpmFromTaps', () => {
  it('calcula o BPM pela média dos intervalos', () => {
    expect(bpmFromTaps([0, 500, 1000, 1500])).toBe(120)
    expect(bpmFromTaps([0, 1000, 2000])).toBe(60)
  })

  it('precisa de pelo menos duas batidas', () => {
    expect(bpmFromTaps([])).toBeNull()
    expect(bpmFromTaps([1000])).toBeNull()
  })

  it('descarta a pausa longa entre duas sequências', () => {
    // O intervalo de 9 s é uma parada, não um andamento de 6 BPM.
    expect(bpmFromTaps([0, 500, 1000, 10_000, 10_500])).toBe(120)
  })

  it('respeita os limites de BPM', () => {
    expect(bpmFromTaps([0, 100])).toBe(240)
    expect(bpmFromTaps([0, 2400])).toBe(40)
  })
})

describe('pruneTaps', () => {
  it('mantém só as batidas recentes e acrescenta a nova', () => {
    expect(pruneTaps([0, 500], 1000)).toEqual([0, 500, 1000])
  })

  it('recomeça a contagem depois de uma pausa', () => {
    expect(pruneTaps([0, 500], 9000)).toEqual([9000])
  })

  it('guarda no máximo as últimas cinco', () => {
    expect(pruneTaps([100, 200, 300, 400, 500], 600)).toHaveLength(5)
  })
})
