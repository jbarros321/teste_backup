/* ============================================================
   MÚSICAS E EXERCÍCIOS
   ------------------------------------------------------------
   Cada nota é: { c: corda, f: casa(traste), d: dedo, t: tempo, dur: duração }
     c   -> 'E' (4ª, Mi)  'A' (3ª, Lá)  'D' (2ª, Ré)  'G' (1ª, Sol)
     f   -> número da casa (0 = corda solta)
     d   -> dedo da mão esquerda: 0=nenhum(solta) 1=indicador 2=médio 3=anelar 4=mindinho
     t   -> em que tempo (batida) a nota entra. 0 = primeira batida
     dur -> quantas batidas ela dura (1 = semínima, 0.5 = colcheia, 2 = mínima)

   Use seq() para escrever tudo em sequência sem contar tempo na mão,
   e repetir() para repetir um trecho.
   ============================================================ */

/** Monta notas em sequência a partir do tempo t0.
 *  itens: [corda, casa, dedo, duração]  — use null na corda para PAUSA. */
function seq(t0, itens) {
  let t = t0;
  const saida = [];
  for (const [c, f, d, dur] of itens) {
    if (c !== null) saida.push({ c, f, d, t, dur });
    t += dur;
  }
  return saida;
}

/** Repete um bloco de notas `vezes` vezes, deslocando `ciclo` batidas por repetição. */
function repetir(notas, vezes, ciclo) {
  const saida = [];
  for (let i = 0; i < vezes; i++) {
    for (const n of notas) saida.push({ ...n, t: n.t + i * ciclo });
  }
  return saida;
}

/** Um compasso inteiro com a mesma nota repetida (ex.: 4 semínimas na tônica). */
function compassoDe(t0, c, f, d, qtd, dur) {
  const itens = [];
  for (let i = 0; i < qtd; i++) itens.push([c, f, d, dur]);
  return seq(t0, itens);
}

const MUSICAS = [

  /* ---------------------------------------------------------- */
  {
    id: 'cordas-soltas',
    nome: 'Exercício 1 · Cordas soltas',
    artista: 'Aquecimento — primeiro contato',
    nivel: 1,
    bpm: 60,
    compasso: 4,
    dica: 'Não aperte nada com a mão esquerda. Só toque a corda com a mão direita, alternando indicador e médio. Apoie o polegar no captador ou na corda Mi.',
    notas: seq(0, [
      ['E', 0, 0, 2], ['E', 0, 0, 2],
      ['A', 0, 0, 2], ['A', 0, 0, 2],
      ['D', 0, 0, 2], ['D', 0, 0, 2],
      ['G', 0, 0, 2], ['G', 0, 0, 2],
      ['G', 0, 0, 2], ['D', 0, 0, 2],
      ['A', 0, 0, 2], ['E', 0, 0, 2],
    ])
  },

  /* ---------------------------------------------------------- */
  {
    id: 'cromatico',
    nome: 'Exercício 2 · Dedilhado cromático 1-2-3-4',
    artista: 'Aquecimento — um dedo por casa',
    nivel: 1,
    bpm: 60,
    compasso: 4,
    dica: 'Um dedo para cada casa: indicador na 1, médio na 2, anelar na 3, mindinho na 4. Aperte logo ATRÁS do traste, não em cima dele. Doeu? Pare e descanse.',
    notas: seq(0, [
      ['E', 1, 1, 1], ['E', 2, 2, 1], ['E', 3, 3, 1], ['E', 4, 4, 1],
      ['A', 1, 1, 1], ['A', 2, 2, 1], ['A', 3, 3, 1], ['A', 4, 4, 1],
      ['D', 1, 1, 1], ['D', 2, 2, 1], ['D', 3, 3, 1], ['D', 4, 4, 1],
      ['G', 1, 1, 1], ['G', 2, 2, 1], ['G', 3, 3, 1], ['G', 4, 4, 1],
      ['G', 4, 4, 1], ['G', 3, 3, 1], ['G', 2, 2, 1], ['G', 1, 1, 1],
      ['D', 4, 4, 1], ['D', 3, 3, 1], ['D', 2, 2, 1], ['D', 1, 1, 1],
      ['A', 4, 4, 1], ['A', 3, 3, 1], ['A', 2, 2, 1], ['A', 1, 1, 1],
      ['E', 4, 4, 1], ['E', 3, 3, 1], ['E', 2, 2, 1], ['E', 1, 1, 2],
    ])
  },

  /* ---------------------------------------------------------- */
  {
    id: 'escala-sol',
    nome: 'Exercício 3 · Escala de Sol maior',
    artista: 'Sua primeira escala',
    nivel: 2,
    bpm: 70,
    compasso: 4,
    dica: 'Mão na "casa 2": indicador cobre a casa 2, médio a 3, anelar a 4, mindinho a 5. A mão fica parada, só os dedos se mexem. Essa escala é a base de milhares de músicas.',
    notas: seq(0, [
      ['E', 3, 2, 1], ['E', 5, 4, 1], ['A', 2, 1, 1], ['A', 3, 2, 1],
      ['A', 5, 4, 1], ['D', 2, 1, 1], ['D', 4, 3, 1], ['D', 5, 4, 2],
      ['D', 5, 4, 1], ['D', 4, 3, 1], ['D', 2, 1, 1], ['A', 5, 4, 1],
      ['A', 3, 2, 1], ['A', 2, 1, 1], ['E', 5, 4, 1], ['E', 3, 2, 2],
    ])
  },

  /* ---------------------------------------------------------- */
  {
    id: 'blues-mi',
    nome: 'Exercício 4 · Blues de 12 compassos em Mi',
    artista: 'Só as notas fundamentais',
    nivel: 2,
    bpm: 80,
    compasso: 4,
    dica: 'A forma mais tocada do mundo. São 12 compassos: 4 de Mi, 2 de Lá, 2 de Mi, 1 de Si, 1 de Lá, 1 de Mi e 1 de Si. Toque 4 batidas iguais em cada compasso, bem no tempo.',
    notas: [
      ...compassoDe(0,  'E', 0, 0, 4, 1),
      ...compassoDe(4,  'E', 0, 0, 4, 1),
      ...compassoDe(8,  'E', 0, 0, 4, 1),
      ...compassoDe(12, 'E', 0, 0, 4, 1),
      ...compassoDe(16, 'A', 0, 0, 4, 1),
      ...compassoDe(20, 'A', 0, 0, 4, 1),
      ...compassoDe(24, 'E', 0, 0, 4, 1),
      ...compassoDe(28, 'E', 0, 0, 4, 1),
      ...compassoDe(32, 'E', 7, 2, 4, 1),
      ...compassoDe(36, 'A', 0, 0, 4, 1),
      ...compassoDe(40, 'E', 0, 0, 4, 1),
      ...compassoDe(44, 'E', 7, 2, 4, 1),
    ]
  },

  /* ---------------------------------------------------------- */
  {
    id: 'with-or-without-you',
    nome: 'With or Without You',
    artista: 'U2 — versão simplificada (fundamentais)',
    nivel: 1,
    bpm: 110,
    compasso: 4,
    dica: 'A música inteira é a mesma volta de 4 compassos: Ré, Lá, Si, Sol. É a música mais fácil do mundo pra estrear no baixo. No disco o Adam Clayton toca em colcheias (o dobro de notas) — comece assim, com 4 por compasso.',
    notas: repetir([
      ...compassoDe(0,  'A', 5, 4, 4, 1),   // Ré
      ...compassoDe(4,  'E', 5, 4, 4, 1),   // Lá
      ...compassoDe(8,  'A', 2, 1, 4, 1),   // Si
      ...compassoDe(12, 'E', 3, 2, 4, 1),   // Sol
    ], 2, 16)
  },

  /* ---------------------------------------------------------- */
  {
    id: 'seven-nation-army',
    nome: 'Seven Nation Army',
    artista: 'The White Stripes — riff principal',
    nivel: 2,
    bpm: 120,
    compasso: 4,
    dica: 'Tudo na corda Lá. A mão desliza: comece com o indicador na casa 7 e desça deslizando a mão até a casa 2. Deixe cada nota soar até a próxima — o segredo do riff é ele ser "gordo" e preguiçoso.',
    notas: repetir(seq(0, [
      ['A', 7,  2, 1.5],
      ['A', 7,  2, 0.5],
      ['A', 10, 4, 1],
      ['A', 7,  2, 1],
      ['A', 5,  1, 1],
      ['A', 3,  2, 1],
      ['A', 2,  1, 2],
    ]), 3, 8)
  },

  /* ---------------------------------------------------------- */
  {
    id: 'stand-by-me',
    nome: 'Stand By Me',
    artista: 'Ben E. King — versão simplificada (fundamentais)',
    nivel: 2,
    bpm: 100,
    compasso: 4,
    dica: 'Sequência de 8 compassos: Sol, Sol, Mi menor, Mi menor, Dó, Ré, Sol, Sol. Aqui você toca só a nota fundamental de cada acorde — quando estiver fácil, a gente acrescenta o balanço original.',
    notas: [
      ...compassoDe(0,  'E', 3, 2, 4, 1),   // Sol
      ...compassoDe(4,  'E', 3, 2, 4, 1),   // Sol
      ...compassoDe(8,  'E', 0, 0, 4, 1),   // Mi
      ...compassoDe(12, 'E', 0, 0, 4, 1),   // Mi
      ...compassoDe(16, 'A', 3, 2, 4, 1),   // Dó
      ...compassoDe(20, 'A', 5, 4, 4, 1),   // Ré
      ...compassoDe(24, 'E', 3, 2, 4, 1),   // Sol
      ...compassoDe(28, 'E', 3, 2, 4, 1),   // Sol
    ]
  },

  /* ---------------------------------------------------------- */
  {
    id: 'smoke-on-the-water',
    nome: 'Smoke on the Water',
    artista: 'Deep Purple — riff no baixo',
    nivel: 3,
    bpm: 100,
    compasso: 4,
    dica: 'Tudo na corda Mi. Indicador na casa 3; para a casa 6 use o mindinho; depois deslize a mão para pegar 8 e 9. Duas notas curtas e uma longa: "ta-ta-TAAA".',
    notas: repetir([
      ...seq(0,  [['E', 3, 1, 0.5], ['E', 6, 4, 0.5], ['E', 8, 3, 2], [null, 0, 0, 1]]),
      ...seq(4,  [['E', 3, 1, 0.5], ['E', 6, 4, 0.5], ['E', 9, 4, 0.5], ['E', 8, 3, 2.5]]),
      ...seq(8,  [['E', 3, 1, 0.5], ['E', 6, 4, 0.5], ['E', 8, 3, 2], [null, 0, 0, 1]]),
      ...seq(12, [['E', 6, 4, 0.5], ['E', 3, 1, 3.5]]),
    ], 2, 16)
  },

];
