// Trilha de aulas. Widgets dentro do HTML:
//   <div class="w-kb" data-notes="C4 E4 G4" data-label="..."></div>   teclado com as notas marcadas + botão ouvir
//   <div class="w-chords" data-chords="C F G C"></div>              acordes clicáveis
//   <div class="w-field" data-key="C" data-minor="0" data-tet="0"></div> tabela de campo harmônico
//   <div class="w-scale" data-root="C" data-scale="Maior (jônio)"></div>
// practice: sequências para o Modo Acordes Caindo | quiz: perguntas
const LESSONS = [
{ id: 'm1', title: 'Conhecendo o teclado', icon: '🎹', color: '#6ee7b7', lessons: [
  { id: 'm1l1', title: 'Seu Casio CTK-1200', xp: 20, html: `
<p>O CTK-1200 é um teclado de <b>61 teclas</b> (5 oitavas) com centenas de timbres (TONE), ritmos de acompanhamento (RHYTHM) e acompanhamento automático. Antes de tocar, deixe o teclado pronto para estudar:</p>
<ol>
<li><b>Timbre de piano:</b> aperte <kbd>TONE</kbd> e escolha o piano acústico (normalmente o primeiro da lista). Estude sempre no piano — ele não "esconde" erros como os timbres com eco.</li>
<li><b>Volume:</b> deixe o volume em torno de 70%. Para o detector do app funcionar, o som precisa chegar bem no microfone do computador/celular.</li>
<li><b>Ritmo desligado</b> no começo. O acompanhamento automático (ACCOMP) é ótimo, mas depois — primeiro você aprende a tocar os acordes "de verdade".</li>
<li><b>Fonte ou pilhas:</b> com pilha fraca o teclado desafina e distorce. Prefira a fonte.</li>
<li><b>Postura:</b> sente-se na altura em que os antebraços ficam retos em relação às teclas, costas eretas, pés apoiados. O Dó central fica mais ou menos na frente do seu umbigo.</li>
</ol>
<div class="tip">💡 <b>Botões que você vai usar muito:</b> TONE (timbre), RHYTHM (ritmo), TEMPO (velocidade), START/STOP, ACCOMP (acompanhamento na mão esquerda), TRANSPOSE (muda o tom sem mudar a posição dos dedos — use com cuidado!) e o metrônomo, se o seu modelo tiver. Os nomes exatos estão no manual do seu teclado.</div>
<div class="warn">⚠️ <b>TRANSPOSE:</b> se um dia as notas "não baterem" com o app ou com a banda, confira se o TRANSPOSE está em 0.</div>`,
    quiz: [
      { q: 'Quantas teclas tem o CTK-1200?', opts: ['49', '61', '76', '88'], a: 1, why: '61 teclas = 5 oitavas completas.' },
      { q: 'Qual timbre é o melhor para estudar?', opts: ['Órgão com eco', 'Piano acústico', 'Strings', 'Synth pad'], a: 1, why: 'O piano mostra com clareza cada nota e cada erro.' }
    ] },
  { id: 'm1l2', title: 'Encontrando as notas', xp: 25, html: `
<p>As teclas pretas aparecem em grupos de <b>2</b> e de <b>3</b>, se repetindo pelo teclado todo. Esse é o seu mapa:</p>
<ul>
<li><b>Dó (C)</b> — branca logo <b>antes</b> do grupo de 2 pretas.</li>
<li><b>Ré (D)</b> — branca <b>entre</b> as 2 pretas.</li>
<li><b>Mi (E)</b> — branca <b>depois</b> das 2 pretas.</li>
<li><b>Fá (F)</b> — branca <b>antes</b> do grupo de 3 pretas.</li>
<li><b>Sol (G)</b> e <b>Lá (A)</b> — entre as 3 pretas.</li>
<li><b>Si (B)</b> — branca <b>depois</b> das 3 pretas.</li>
</ul>
<div class="w-kb" data-notes="C4 D4 E4 F4 G4 A4 B4" data-label="As 7 notas naturais: Dó Ré Mi Fá Sol Lá Si"></div>
<p>O <b>Dó central (C4)</b> é o Dó mais perto do meio do teclado. Num teclado de 61 teclas ele é o 3º Dó a partir da esquerda. Toda referência deste app usa C4 = Dó central.</p>
<div class="w-kb" data-notes="C4" data-label="Dó central (C4)"></div>`,
    practice: [{ label: 'Treino: encontre as notas', drill: 'notes' }],
    quiz: [
      { q: 'O Dó fica...', opts: ['Entre as 2 pretas', 'Antes do grupo de 2 pretas', 'Depois das 3 pretas', 'Antes das 3 pretas'], a: 1 },
      { q: 'Qual nota fica entre as 2 teclas pretas?', opts: ['Mi', 'Dó', 'Ré', 'Fá'], a: 2 }
    ] },
  { id: 'm1l3', title: 'Dedos e postura das mãos', xp: 20, html: `
<p>Os dedos são numerados de <b>1 a 5</b> nas duas mãos: <b>1 = polegar</b>, 2 = indicador, 3 = médio, 4 = anelar, 5 = mínimo.</p>
<p>Mão em formato de "concha", como se segurasse uma laranja. Toque com a ponta dos dedos, punho solto e alinhado com o antebraço.</p>
<p><b>Posição de Dó</b> (mão direita): polegar no C4, e cada dedo em uma tecla: 1-C, 2-D, 3-E, 4-F, 5-G.</p>
<div class="w-kb" data-notes="C4 D4 E4 F4 G4" data-label="Posição de Dó — mão direita (1 2 3 4 5)"></div>
<p><b>Mão esquerda</b>: o dedo 5 no C3 e o polegar no G3.</p>
<div class="w-kb" data-notes="C3 D3 E3 F3 G3" data-label="Posição de Dó — mão esquerda (5 4 3 2 1)"></div>
<div class="tip">💡 A regra do tecladista de louvor: <b>mão esquerda = baixo/base</b>, <b>mão direita = acordes, melodia e enfeites</b>.</div>`,
    quiz: [{ q: 'Qual o número do polegar?', opts: ['1', '3', '5', 'Depende da mão'], a: 0 }] },
  { id: 'm1l4', title: 'Oitavas e registro', xp: 20, html: `
<p>A sequência Dó-Ré-Mi-Fá-Sol-Lá-Si se repete. De um Dó até o próximo Dó temos uma <b>oitava</b> (12 teclas, contando as pretas).</p>
<div class="w-kb" data-notes="C3 C4 C5" data-label="Três Dós em oitavas diferentes"></div>
<p>Com 61 teclas você tem 5 oitavas. Na prática de louvor:</p>
<ul><li><b>Oitava 2</b> (C2–B2): graves — baixo da mão esquerda.</li><li><b>Oitava 3</b>: acordes graves / base cheia.</li><li><b>Oitava 4</b> (C4–B4): acordes da mão direita. É aqui que o app coloca a MD.</li><li><b>Oitava 5</b>: melodia, enfeites e solos.</li></ul>
<div class="tip">💡 O nome da nota com número (ex.: <b>F#2</b>) diz a nota e a oitava. É o mesmo padrão da sua apostila "Cifra Melódica Gospel" (ME: D2, MD: D4 F#4 A4).</div>`,
    quiz: [{ q: 'Na apostila, "ME: D2" significa...', opts: ['Mão esquerda toca o Ré da oitava 2', 'Mão esquerda toca 2 notas', 'Ré sustenido', 'Dedo 2 no Ré'], a: 0 }] }
] },

{ id: 'm2', title: 'Notas, cifras e ritmo', icon: '📖', color: '#93c5fd', lessons: [
  { id: 'm2l1', title: 'Sustenidos, bemóis e a escala cromática', xp: 25, html: `
<p>As teclas pretas são notas <b>alteradas</b>. O <b>sustenido (#)</b> sobe meio tom; o <b>bemol (b)</b> desce meio tom. A mesma tecla preta tem dois nomes: C# = Db, D# = Eb, F# = Gb, G# = Ab, A# = Bb (isso se chama <b>enarmonia</b>).</p>
<div class="w-kb" data-notes="C4 C#4 D4 D#4 E4 F4 F#4 G4 G#4 A4 A#4 B4" data-label="Escala cromática: as 12 notas"></div>
<p>A escala cromática tem <b>12 notas</b> — todas as teclas, brancas e pretas, em sequência. Toda a música ocidental usa só essas 12!</p>
<div class="tip">💡 Entre Mi-Fá e Si-Dó <b>não há tecla preta</b>. Por isso E# = F e B# = C (raro, mas aparece).</div>`,
    quiz: [
      { q: 'Db é a mesma tecla que...', opts: ['D#', 'C#', 'C', 'E'], a: 1 },
      { q: 'Quantas notas diferentes existem na escala cromática?', opts: ['7', '8', '12', '24'], a: 2 }
    ] },
  { id: 'm2l2', title: 'Tom e semitom', xp: 20, html: `
<p><b>Semitom (meio tom)</b> é a menor distância: de uma tecla para a vizinha (pode ser branca→preta ou branca→branca como Mi→Fá). <b>Tom</b> = 2 semitons.</p>
<div class="w-kb" data-notes="E4 F4" data-label="Mi → Fá = 1 semitom"></div>
<div class="w-kb" data-notes="C4 D4" data-label="Dó → Ré = 1 tom (passa pelo C#)"></div>
<p>Contar semitons é a base para montar <b>qualquer</b> acorde e <b>qualquer</b> escala. Guarde isso: todo o resto é fórmula de semitons.</p>`,
    quiz: [
      { q: 'De Mi para Fá temos...', opts: ['1 tom', '1 semitom', '2 tons', 'Nenhuma distância'], a: 1 },
      { q: 'Quantos semitons tem 1 tom?', opts: ['1', '2', '3', '4'], a: 1 }
    ] },
  { id: 'm2l3', title: 'Lendo cifras', xp: 25, html: `
<p>Cifra é a "sigla" do acorde. A letra é a nota fundamental: <b>A</b>=Lá, <b>B</b>=Si, <b>C</b>=Dó, <b>D</b>=Ré, <b>E</b>=Mi, <b>F</b>=Fá, <b>G</b>=Sol.</p>
<table class="t"><tr><th>Cifra</th><th>Lê-se</th><th>Significado</th></tr>
<tr><td>C</td><td>Dó maior</td><td>tríade maior</td></tr>
<tr><td>Cm</td><td>Dó menor</td><td>tríade menor</td></tr>
<tr><td>C7</td><td>Dó com sétima</td><td>maior + 7ª menor</td></tr>
<tr><td>C7M / Cmaj7</td><td>Dó com sétima maior</td><td>maior + 7ª maior</td></tr>
<tr><td>C4 / Csus4</td><td>Dó com quarta</td><td>troca a 3ª pela 4ª</td></tr>
<tr><td>Cadd9 / C9 / C2</td><td>Dó com nona</td><td>maior + 9ª (a mesma nota que a 2ª)</td></tr>
<tr><td>C° / Cdim</td><td>Dó diminuto</td><td>3ª menor + 5ª diminuta</td></tr>
<tr><td>C/E</td><td>Dó com baixo em Mi</td><td>acorde de Dó, mão esquerda toca Mi</td></tr>
</table>
<p>Na sua apostila, as cifras aparecem <b>em cima da sílaba</b> onde o acorde muda. O app usa essa posição para mostrar a letra junto com os acordes.</p>
<div class="w-chords" data-chords="C Cm C7 C7M Csus4 Cadd9 C° C/E"></div>`,
    quiz: [
      { q: 'O que significa G/B?', opts: ['Sol ou Si', 'Acorde de Sol com baixo em Si', 'Sol menor', 'Si com sétima'], a: 1 },
      { q: 'Em cifra brasileira, "A4" normalmente é...', opts: ['Lá na oitava 4', 'Lá com 4ª (sus4)', 'Lá com 4 notas', 'Lá menor'], a: 1 }
    ] },
  { id: 'm2l4', title: 'Pulso, compasso e metrônomo', xp: 25, html: `
<p>O <b>pulso</b> é a batida constante da música — o que você bate com o pé. O <b>BPM</b> (batidas por minuto) mede a velocidade.</p>
<p><b>Compasso</b> agrupa os pulsos: <b>4/4</b> (1-2-3-4, a maioria dos louvores), <b>3/4</b> (valsa: 1-2-3, hinos como "Porque Ele Vive") e <b>6/8</b> (balanço "1-2-3-4-5-6", muitos louvores congregacionais).</p>
<p>Figuras mais usadas: <b>semibreve</b> (4 tempos), <b>mínima</b> (2), <b>semínima</b> (1), <b>colcheia</b> (½), <b>semicolcheia</b> (¼).</p>
<div class="tip">💡 Estude <b>SEMPRE</b> com metrônomo. Comece lento (60 BPM), só aumente 5 BPM quando tocar 3 vezes seguidas sem erro. Use o metrônomo do app em <b>Ferramentas</b>.</div>`,
    practice: [{ label: 'Troca C–G no pulso (60 BPM)', seq: 'C G C G C G C G', bpm: 60, beats: 4 }],
    quiz: [
      { q: 'Uma semínima em 4/4 vale...', opts: ['4 tempos', '2 tempos', '1 tempo', '½ tempo'], a: 2 },
      { q: 'Quando aumentar o BPM?', opts: ['Todo dia', 'Quando tocar 3x sem erro', 'Quando cansar', 'Nunca'], a: 1 }
    ] }
] },

{ id: 'm3', title: 'Primeiros acordes', icon: '🎶', color: '#fcd34d', lessons: [
  { id: 'm3l1', title: 'Tríade maior', xp: 30, html: `
<p>Acorde é a combinação de 3 ou mais notas tocadas juntas. A <b>tríade maior</b> tem: <b>fundamental (1)</b>, <b>3ª maior</b> e <b>5ª justa</b>.</p>
<p><b>Fórmula em semitons:</b> fundamental → <b>+4</b> → <b>+3</b>. Exemplo em Dó: C (+4) E (+3) G.</p>
<div class="w-kb" data-notes="C4 E4 G4" data-label="C = Dó Mi Sol (4 + 3 semitons)"></div>
<div class="w-kb" data-notes="G4 B4 D5" data-label="G = Sol Si Ré"></div>
<div class="w-kb" data-notes="D4 F#4 A4" data-label="D = Ré Fá# Lá"></div>
<p>Dedilhado da mão direita: <b>1-3-5</b> (polegar, médio, mínimo).</p>
<div class="w-chords" data-chords="C D E F G A B"></div>`,
    practice: [{ label: 'Os 7 acordes maiores', seq: 'C D E F G A B', bpm: 50, beats: 4 }],
    quiz: [
      { q: 'A fórmula da tríade maior é...', opts: ['3 + 4 semitons', '4 + 3 semitons', '4 + 4 semitons', '2 + 5 semitons'], a: 1 },
      { q: 'Quais notas formam o acorde de F?', opts: ['F A C', 'F G# C', 'F A D', 'F Bb C'], a: 0 }
    ] },
  { id: 'm3l2', title: 'Tríade menor', xp: 30, html: `
<p>A <b>tríade menor</b> só muda a 3ª: ela desce meio tom. <b>Fórmula:</b> fundamental → <b>+3</b> → <b>+4</b>.</p>
<div class="w-kb" data-notes="A3 C4 E4" data-label="Am = Lá Dó Mi (3 + 4)"></div>
<div class="w-kb" data-notes="C4 D#4 G4" data-label="Cm = Dó Mib Sol"></div>
<p>Compare C e Cm: só o dedo do meio muda! O acorde maior soa "alegre/aberto", o menor soa "triste/introspectivo" — é a cor mais usada na adoração.</p>
<div class="w-chords" data-chords="C Cm D Dm E Em F Fm G Gm A Am B Bm"></div>`,
    practice: [{ label: 'Maior ↔ menor', seq: 'C Cm D Dm E Em A Am', bpm: 50, beats: 4 }],
    quiz: [{ q: 'Para transformar D em Dm, você...', opts: ['Sobe a fundamental', 'Desce a 3ª meio tom', 'Desce a 5ª', 'Adiciona uma nota'], a: 1 }] },
  { id: 'm3l3', title: 'Os 14 acordes iniciais', xp: 35, html: `
<p>Com 7 acordes maiores e 7 menores naturais você já toca centenas de louvores. Memorize a <b>forma</b> e o <b>nome</b>:</p>
<div class="w-chords" data-chords="C D E F G A B Cm Dm Em Fm Gm Am Bm"></div>
<p>Dicas de memorização:</p>
<ul><li><b>C, F, G</b> — só teclas brancas.</li><li><b>D, E, A</b> — a do meio é preta.</li><li><b>Dm, Em, Am</b> — só brancas.</li><li><b>B</b> — duas pretas (D# F#). <b>Bm</b> — uma preta (F#).</li></ul>`,
    practice: [{ label: 'Todos os 14 (aleatório)', drill: 'chords', set: 'C D E F G A B Cm Dm Em Fm Gm Am Bm' }],
    quiz: [{ q: 'Qual destes acordes usa só teclas brancas?', opts: ['D', 'E', 'Am', 'B'], a: 2 }] },
  { id: 'm3l4', title: 'Trocando acordes: C – F – G', xp: 35, html: `
<p>O segredo para trocar rápido: <b>pense no próximo acorde antes de tirar a mão</b> e mova a mão inteira como um bloco.</p>
<p>A progressão <b>C – F – G – C</b> é a espinha dorsal de milhares de músicas. Pratique com a mão esquerda tocando só a fundamental (C2, F2, G2) e a direita o acorde.</p>
<div class="w-chords" data-chords="C F G C"></div>
<p>Agora no menor: <b>Am – Dm – E – Am</b>.</p>
<div class="w-chords" data-chords="Am Dm E Am"></div>
<div class="tip">💡 Use o <b>Modo Acordes Caindo</b>: ele espera você tocar certo para liberar o próximo acorde.</div>`,
    practice: [{ label: 'C F G C', seq: 'C F G C C F G C', bpm: 60, beats: 4 }, { label: 'Am Dm E Am', seq: 'Am Dm E Am Am Dm E Am', bpm: 60, beats: 4 }],
    quiz: [{ q: 'O melhor jeito de trocar acordes é...', opts: ['Olhar cada dedo', 'Mover a mão como bloco, já pensando no próximo', 'Parar e procurar', 'Tocar mais rápido'], a: 1 }] }
] },

{ id: 'm4', title: 'Intervalos', icon: '📏', color: '#f9a8d4', lessons: [
  { id: 'm4l1', title: 'O que é intervalo', xp: 25, html: `
<p><b>Intervalo</b> é a distância entre duas notas, contada pelos nomes (Dó→Mi = 3ª, porque Dó-Ré-Mi são 3 nomes) e medida em semitons (define se é maior, menor, justa...).</p>
<table class="t"><tr><th>Semitons</th><th>Intervalo</th><th>Referência</th></tr>
${[0,1,2,3,4,5,6,7,8,9,10,11,12].map(i => `<tr><td>${i}</td><td>${M.INTERVALS[i][0]}</td><td>${M.INTERVALS[i][1]}</td></tr>`).join('')}</table>
<div class="w-kb" data-notes="C4 E4" data-label="Dó → Mi: 3ª maior (4 semitons)"></div>
<div class="w-kb" data-notes="C4 G4" data-label="Dó → Sol: 5ª justa (7 semitons)"></div>`,
    practice: [{ label: 'Treino de ouvido: intervalos', drill: 'ear-intervals' }],
    quiz: [
      { q: 'Uma 5ª justa tem quantos semitons?', opts: ['5', '6', '7', '8'], a: 2 },
      { q: 'Dó → Mib é...', opts: ['3ª maior', '3ª menor', '2ª maior', '4ª'], a: 1 }
    ] },
  { id: 'm4l2', title: 'Acordes vistos como intervalos', xp: 25, html: `
<p>Agora tudo faz sentido: <b>maior = 3ª maior + 5ª justa</b>; <b>menor = 3ª menor + 5ª justa</b>. As extensões são mais intervalos empilhados:</p>
<ul><li><b>7ª menor</b> (10 semitons) → C7 = C E G <b>Bb</b></li><li><b>7ª maior</b> (11) → C7M = C E G <b>B</b></li><li><b>9ª</b> (= 2ª uma oitava acima) → Cadd9 = C E G <b>D</b></li><li><b>4ª</b> (5 semitons) → Csus4 = C <b>F</b> G</li></ul>
<div class="w-chords" data-chords="C C7 C7M Cadd9 Csus4 C6"></div>`,
    quiz: [{ q: 'A 9ª de Dó é a nota...', opts: ['Si', 'Ré', 'Fá', 'Lá'], a: 1 }] },
  { id: 'm4l3', title: 'Ouvido: maior x menor', xp: 25, html: `
<p>Treinar o ouvido é o que separa quem "lê cifra" de quem <b>toca de verdade</b>. Comece distinguindo <b>maior</b> (aberto, alegre) de <b>menor</b> (fechado, emotivo).</p>
<div class="w-chords" data-chords="C Cm G Gm D Dm A Am"></div>
<p>Depois, intervalos: 3ª maior vs menor, 4ª vs 5ª. Faça o treino todos os dias por 5 minutos.</p>`,
    practice: [{ label: 'Treino de ouvido: maior x menor', drill: 'ear-quality' }],
    quiz: [{ q: 'O que muda entre C e Cm?', opts: ['A fundamental', 'A 3ª', 'A 5ª', 'Tudo'], a: 1 }] }
] },

{ id: 'm5', title: 'Escala maior e campo harmônico', icon: '🗝️', color: '#a5b4fc', lessons: [
  { id: 'm5l1', title: 'A escala maior', xp: 30, html: `
<p>A escala maior tem 7 notas e segue a fórmula: <b>T – T – S – T – T – T – S</b> (T = tom, S = semitom).</p>
<div class="w-scale" data-root="C" data-scale="Maior (jônio)"></div>
<div class="w-scale" data-root="G" data-scale="Maior (jônio)"></div>
<div class="w-scale" data-root="D" data-scale="Maior (jônio)"></div>
<p>Dedilhado da MD (Dó maior): <b>1-2-3, passa o polegar, 1-2-3-4-5</b>. O polegar passa por baixo do dedo 3.</p>`,
    practice: [{ label: 'Treino: tocar a escala (nota a nota)', drill: 'scale', root: 'C' }],
    quiz: [
      { q: 'Fórmula da escala maior:', opts: ['T T S T T T S', 'T S T T S T T', 'S T T T S T T', 'T T T S T T S'], a: 0 },
      { q: 'Quantos sustenidos tem Sol maior?', opts: ['0', '1 (F#)', '2', '3'], a: 1 }
    ] },
  { id: 'm5l2', title: 'Campo harmônico maior', xp: 40, html: `
<p>Monte uma tríade sobre <b>cada nota</b> da escala, usando só notas da escala. O resultado é o <b>campo harmônico</b> — os 7 acordes "da família" do tom.</p>
<p>Padrão (vale para TODOS os tons maiores): <b>I – ii – iii – IV – V – vi – vii°</b> → maior, menor, menor, maior, maior, menor, diminuto.</p>
<div class="w-field" data-key="C"></div>
<div class="w-field" data-key="G"></div>
<p>90% dos louvores usam só acordes do campo harmônico do tom. Se a música está em G, os acordes prováveis são G, Am, Bm, C, D, Em.</p>`,
    quiz: [
      { q: 'O 6º grau (vi) de um tom maior é...', opts: ['Maior', 'Menor', 'Diminuto', 'Aumentado'], a: 1 },
      { q: 'Qual o IV grau de D?', opts: ['G', 'Em', 'A', 'Bm'], a: 0 }
    ] },
  { id: 'm5l3', title: 'Funções: tônica, subdominante, dominante', xp: 35, html: `
<p>Cada grau tem uma <b>função</b>:</p>
<ul><li><b>Tônica (I, vi, iii)</b> — repouso, "casa".</li><li><b>Subdominante (IV, ii)</b> — afastamento, preparação.</li><li><b>Dominante (V, vii°)</b> — tensão que "pede" para voltar à tônica.</li></ul>
<p>Em C: <b>C</b> (casa) → <b>F</b> (sai) → <b>G</b> (tensão) → <b>C</b> (volta). Toque e sinta a tensão do G resolvendo no C. Com <b>G7</b> a tensão fica ainda maior.</p>
<div class="w-chords" data-chords="C F G C C F G7 C"></div>`,
    practice: [{ label: 'I–IV–V7–I em C, G e D', seq: 'C F G7 C G C D7 G D G A7 D', bpm: 60, beats: 4 }],
    quiz: [{ q: 'Qual acorde tem função de dominante em C?', opts: ['F', 'Am', 'G', 'Em'], a: 2 }] },
  { id: 'm5l4', title: 'Os tons do louvor', xp: 40, html: `
<p>Os tons mais comuns no repertório gospel (e na sua apostila) são <b>G, D, A, E, C, F e Bb</b>. Domine o campo harmônico de cada um:</p>
<div class="w-field" data-key="D"></div><div class="w-field" data-key="A"></div><div class="w-field" data-key="E"></div><div class="w-field" data-key="F"></div><div class="w-field" data-key="Bb"></div>
<div class="tip">💡 Use <b>Ferramentas → Campo harmônico</b> para ver qualquer tom, com tríades ou tétrades.</div>`,
    practice: [{ label: 'Campo de G (tríades)', seq: 'G Am Bm C D Em F#° G', bpm: 60, beats: 4 }, { label: 'Campo de D', seq: 'D Em F#m G A Bm C#° D', bpm: 60, beats: 4 }],
    quiz: [{ q: 'Qual acorde NÃO pertence ao campo de A maior?', opts: ['D', 'F#m', 'E', 'C'], a: 3 }] },
  { id: 'm5l5', title: 'Campo harmônico menor', xp: 35, html: `
<p>Tom menor natural: <b>i – ii° – III – iv – v – VI – VII</b>. Na prática o <b>v</b> vira <b>V</b> (maior, com 7ª) para dar tensão — isso vem da escala menor harmônica.</p>
<div class="w-field" data-key="A" data-minor="1"></div>
<div class="w-field" data-key="E" data-minor="1"></div>
<p><b>Relativa:</b> todo tom maior tem um menor "irmão" com as mesmas notas, 3 semitons abaixo: C ↔ Am, G ↔ Em, D ↔ Bm.</p>`,
    quiz: [{ q: 'A relativa menor de G é...', opts: ['Gm', 'Em', 'Bm', 'Am'], a: 1 }] }
] },

{ id: 'm6', title: 'Inversões e baixo invertido', icon: '🔄', color: '#fdba74', lessons: [
  { id: 'm6l1', title: 'Inversões', xp: 35, html: `
<p>Um acorde tem as mesmas notas em qualquer ordem. Mudar a nota de baixo gera as <b>inversões</b>:</p>
<div class="w-kb" data-notes="C4 E4 G4" data-label="Estado fundamental: C E G"></div>
<div class="w-kb" data-notes="E4 G4 C5" data-label="1ª inversão: E G C"></div>
<div class="w-kb" data-notes="G3 C4 E4" data-label="2ª inversão: G C E"></div>
<p>Por que isso importa? Para <b>não pular</b> pelo teclado. O pianista profissional escolhe a inversão mais próxima do acorde anterior.</p>`,
    practice: [{ label: 'Inversões de C, F e G', drill: 'inversions' }],
    quiz: [{ q: 'A 1ª inversão de G é...', opts: ['G B D', 'B D G', 'D G B', 'G D B'], a: 1 }] },
  { id: 'm6l2', title: 'Condução de vozes', xp: 40, html: `
<p>Condução de vozes = mover o <b>mínimo possível</b> os dedos entre acordes. Veja C – F – G com inversões:</p>
<div class="w-kb" data-notes="C4 E4 G4" data-label="C (fundamental)"></div>
<div class="w-kb" data-notes="C4 F4 A4" data-label="F (2ª inversão) — só 2 dedos se movem"></div>
<div class="w-kb" data-notes="B3 D4 G4" data-label="G (1ª inversão) — mão quase parada"></div>
<p>Regra de ouro: <b>nota em comum fica</b>, as outras andam para a nota mais próxima. O app monta a MD sempre dentro de C4–B4, o que já dá uma condução muito boa.</p>`,
    practice: [{ label: 'I–vi–IV–V conduzido', seq: 'C Am F G C Am F G', bpm: 70, beats: 4 }],
    quiz: [{ q: 'Na condução de vozes, a nota em comum...', opts: ['Sempre muda', 'Fica parada', 'Sobe uma oitava', 'É omitida'], a: 1 }] },
  { id: 'm6l3', title: 'Acordes com barra (baixo invertido)', xp: 40, html: `
<p><b>G/B</b> = acorde de G com a <b>mão esquerda no Si</b>. Isso cria linhas de baixo bonitas, típicas do louvor:</p>
<div class="w-chords" data-chords="C G/B Am G F C/E Dm G"></div>
<p>A mão direita toca o acorde normal; só a esquerda muda. As mais comuns:</p>
<ul><li><b>G/B, D/F#, A/C#, C/E</b> — acorde com a 3ª no baixo (1ª inversão).</li><li><b>C/G, G/D</b> — 5ª no baixo.</li><li><b>D/C, G/F</b> — 7ª no baixo (passagem descendente).</li><li><b>A/G, F/G, D/E</b> — "acordes suspensos" muito usados no gospel (F/G ≈ G7sus4).</li></ul>`,
    practice: [{ label: 'Baixo descendente', seq: 'C G/B Am G F C/E Dm G', bpm: 70, beats: 4 }, { label: 'Em D: D A/C# Bm A G D/F# Em A', seq: 'D A/C# Bm A G D/F# Em A', bpm: 70, beats: 4 }],
    quiz: [{ q: 'Em D/F#, a mão esquerda toca...', opts: ['Ré', 'Fá#', 'Lá', 'Fá'], a: 1 }] },
  { id: 'm6l4', title: 'Linhas de baixo que andam', xp: 35, html: `
<p>Ligue acordes com notas de passagem no baixo. Ex.: de C para Am passando por B (C – C/B – Am), ou subindo de C para F: C – C/E – F.</p>
<div class="w-chords" data-chords="C C/B Am Am/G F C/E D7/F# G"></div>
<p>Esse "baixo caminhando" é o que dá cara de <b>tecladista experiente</b> numa balada.</p>`,
    practice: [{ label: 'Baixo caminhando em C', seq: 'C C/B Am Am/G F C/E D7/F# G', bpm: 70, beats: 4 }],
    quiz: [{ q: 'C – C/B – Am: o baixo faz...', opts: ['Salto', 'Linha descendente C B A', 'Linha ascendente', 'Nada'], a: 1 }] }
] },

{ id: 'm7', title: 'Progressões do louvor', icon: '🔥', color: '#fca5a5', lessons: [
  { id: 'm7l1', title: 'I – V – vi – IV (a mais famosa)', xp: 40, html: `
<p>A progressão mais usada da música moderna (e do gospel!). Em G: <b>G – D – Em – C</b>. Em C: <b>C – G – Am – F</b>.</p>
<div class="w-chords" data-chords="G D Em C"></div>
<p>Treine em todos os tons comuns. Quando dominar, qualquer música que usa esse ciclo sai na hora.</p>`,
    practice: [{ label: 'I–V–vi–IV em G', seq: 'G D Em C G D Em C', bpm: 72, beats: 4 }, { label: 'em D', seq: 'D A Bm G D A Bm G', bpm: 72, beats: 4 }, { label: 'em A', seq: 'A E F#m D A E F#m D', bpm: 72, beats: 4 }],
    quiz: [{ q: 'I–V–vi–IV em D é...', opts: ['D A Bm G', 'D G A Bm', 'D Em G A', 'D A G Em'], a: 0 }] },
  { id: 'm7l2', title: 'vi – IV – I – V e I – IV – vi – V', xp: 40, html: `
<p>Variações do mesmo "DNA", muito comuns em adoração:</p>
<div class="w-chords" data-chords="Em C G D"></div>
<div class="w-chords" data-chords="G C Em D"></div>
<p>Repare: começar pelo <b>vi</b> deixa o clima mais introspectivo; o final no <b>V</b> "puxa" para repetir o ciclo.</p>`,
    practice: [{ label: 'vi–IV–I–V em G', seq: 'Em C G D Em C G D', bpm: 72, beats: 4 }],
    quiz: [{ q: 'vi–IV–I–V em C é...', opts: ['Am F C G', 'Am G F C', 'C F G Am', 'F G Am C'], a: 0 }] },
  { id: 'm7l3', title: 'ii – V – I e a dominante', xp: 45, html: `
<p>O <b>ii – V – I</b> é a cadência mais forte: Dm7 – G7 – C7M. O ii prepara, o V cria tensão, o I resolve.</p>
<div class="w-chords" data-chords="Dm7 G7 C7M"></div>
<p>No louvor é comum preparar o refrão com <b>IV – V</b> ou <b>ii – V</b>: C – F – G – (refrão em C).</p>`,
    practice: [{ label: 'ii–V–I em C, F e G', seq: 'Dm7 G7 C7M C7M Gm7 C7 F7M F7M Am7 D7 G7M G7M', bpm: 70, beats: 4 }],
    quiz: [{ q: 'ii–V–I em F é...', opts: ['Gm7 C7 F7M', 'Dm7 G7 C7M', 'Am7 D7 G7M', 'Gm7 D7 F'], a: 0 }] },
  { id: 'm7l4', title: 'Dominantes secundárias', xp: 50, html: `
<p>Todo acorde pode ter "sua própria dominante". A dominante de Am é <b>E7</b> (V7/vi). Em C, ouvir <b>E7 → Am</b> é aquele momento que arrepia!</p>
<table class="t"><tr><th>Destino</th><th>Dominante secundária (em C)</th></tr>
<tr><td>Am (vi)</td><td>E7</td></tr><tr><td>Dm (ii)</td><td>A7</td></tr><tr><td>Em (iii)</td><td>B7</td></tr><tr><td>F (IV)</td><td>C7</td></tr><tr><td>G (V)</td><td>D7</td></tr></table>
<div class="w-chords" data-chords="C E7 Am C7 F G7 C"></div>
<p>Truque: a dominante fica <b>5ª acima</b> (ou 4ª abaixo) do acorde de destino e é sempre maior com 7ª.</p>`,
    practice: [{ label: 'C E7 Am C7 F G7 C', seq: 'C E7 Am C7 F G7 C C', bpm: 66, beats: 4 }],
    quiz: [{ q: 'Qual a dominante secundária de Em?', opts: ['A7', 'B7', 'E7', 'D7'], a: 1 }] }
] },

{ id: 'm8', title: 'Tétrades e cores', icon: '🎨', color: '#c4b5fd', lessons: [
  { id: 'm8l1', title: '7, 7M, m7, m7(b5) e °', xp: 45, html: `
<p>Tétrades = tríade + 7ª. Os 5 tipos principais:</p>
<div class="w-chords" data-chords="C7M C7 Cm7 Cm7(b5) C°7"></div>
<ul><li><b>7M</b> — suave, sonhador (I e IV).</li><li><b>7</b> — tensão de dominante (V).</li><li><b>m7</b> — menor macio (ii, iii, vi).</li><li><b>m7(b5)</b> — meio-diminuto (vii).</li><li><b>°</b> — diminuto, de passagem.</li></ul>
<div class="w-field" data-key="C" data-tet="1"></div>`,
    quiz: [{ q: 'Qual tétrade é o V grau do campo maior?', opts: ['7M', 'm7', '7', 'm7(b5)'], a: 2 }] },
  { id: 'm8l2', title: 'Acordes sus4 e o "4" das cifras', xp: 40, html: `
<p>No louvor, <b>A4</b>, <b>E4</b>, <b>D4</b> aparecem muito: a 3ª é trocada pela 4ª, criando suspense que resolve no acorde normal.</p>
<div class="w-chords" data-chords="A4 A E4 E D4 D"></div>
<p><b>7(4)</b> = sus4 com 7ª: Ex.: <b>G7(4) → C</b>. Outra forma de escrever: <b>F/G</b>.</p>`,
    practice: [{ label: 'Resoluções sus4', seq: 'A4 A D4 D E4 E A4 A', bpm: 66, beats: 4 }],
    quiz: [{ q: 'Em Dsus4, qual nota substitui a 3ª?', opts: ['F#', 'G', 'E', 'A'], a: 1 }] },
  { id: 'm8l3', title: 'add9: o som "worship"', xp: 45, html: `
<p>O <b>add9</b> (também escrito 9 ou 2) é a assinatura do louvor moderno. Sua apostila está cheia deles: <b>Dadd9, Gadd9, Cadd9, Em7(9)</b>.</p>
<div class="w-chords" data-chords="Cadd9 Gadd9 Dadd9 Em7(9) Am7(9)"></div>
<p>Truque de posição: na MD toque <b>1-2-3-5</b> (fundamental, 9ª, 3ª, 5ª). Ex.: Dadd9 = D E F# A.</p>`,
    practice: [{ label: 'Progressão worship em D', seq: 'Dadd9 Bm7 Gadd9 A4 Dadd9 Bm7 Gadd9 A', bpm: 68, beats: 4 }],
    quiz: [{ q: 'A 9ª de G é...', opts: ['A', 'B', 'D', 'F#'], a: 0 }] },
  { id: 'm8l4', title: '6, 6(9), 11 e 13', xp: 45, html: `
<p>Extensões para encorpar:</p>
<div class="w-chords" data-chords="C6 C6(9) Am7(11) G7(13) F7M(9)"></div>
<p>Use com moderação: numa congregação, clareza é mais importante que complexidade. As extensões brilham em introduções, finais e momentos de ministração.</p>`,
    quiz: [{ q: 'A 6ª de Dó é...', opts: ['A', 'B', 'G', 'F'], a: 0 }] }
] },

{ id: 'm9', title: 'Acompanhamento e levadas', icon: '🥁', color: '#86efac', lessons: [
  { id: 'm9l1', title: 'Mão esquerda: baixo e oitavas', xp: 40, html: `
<p>Padrões da mão esquerda, do mais simples ao mais cheio:</p>
<ol><li><b>Só fundamental</b> (1 tempo por compasso).</li><li><b>Oitava</b>: dedo 5 + polegar (C2 + C3).</li><li><b>1-5-8</b>: fundamental, quinta, oitava (C2 G2 C3) — arpejo que preenche baladas.</li><li><b>1-5-10</b>: avançado, bem aberto.</li></ol>
<div class="w-kb" data-notes="C2 G2 C3" data-label="Padrão 1-5-8 em Dó"></div>
<div class="w-kb" data-notes="G2 D3 G3" data-label="Padrão 1-5-8 em Sol"></div>`,
    practice: [{ label: 'Balada com 1-5-8', seq: 'C G Am F C G Am F', bpm: 64, beats: 4 }],
    quiz: [{ q: 'No padrão 1-5-8 de G, as notas são...', opts: ['G B D', 'G D G', 'G C G', 'G E G'], a: 1 }] },
  { id: 'm9l2', title: 'Levadas: adoração, celebração e valsa', xp: 45, html: `
<ul><li><b>Adoração (balada lenta, 60–75 BPM)</b>: MD segura o acorde (semibreve) ou faz arpejo em colcheias; ME em oitava no tempo 1.</li>
<li><b>Celebração (100–130 BPM)</b>: MD em colcheias com acento nos contratempos ("tum-TA-tum-TA"); ME no 1 e no 3.</li>
<li><b>Valsa 3/4</b>: ME no 1, MD nos tempos 2 e 3 ("tum-pá-pá") — hinos tradicionais.</li>
<li><b>6/8 congregacional</b>: ME no 1 e no 4, MD arpejando.</li></ul>
<div class="tip">💡 Faça cada levada com o metrônomo antes de aplicar em músicas. Grave o áudio no celular e escute — é o melhor professor.</div>`,
    practice: [{ label: 'Celebração em G (110 BPM)', seq: 'G C D G G C D G', bpm: 110, beats: 4 }, { label: 'Valsa em C', seq: 'C F G7 C C F G7 C', bpm: 90, beats: 3 }],
    quiz: [{ q: 'A valsa é em qual compasso?', opts: ['4/4', '3/4', '2/4', '6/8'], a: 1 }] },
  { id: 'm9l3', title: 'Ritmos e ACCOMP do CTK-1200', xp: 35, html: `
<p>O acompanhamento automático toca bateria, baixo e acordes a partir do acorde que sua <b>mão esquerda</b> faz na região de acompanhamento (lado esquerdo do teclado).</p>
<ul><li>Escolha um ritmo (<kbd>RHYTHM</kbd>) — procure ritmos de balada/pop/8 beat para louvor.</li><li>Ligue <kbd>ACCOMP</kbd> e escolha o modo de acorde: <b>FINGERED</b> (você faz o acorde completo — recomendado, pois treina) ou <b>CASIO CHORD</b> (acordes simplificados com 1–2 dedos).</li><li>Ajuste <kbd>TEMPO</kbd> e use <b>INTRO / FILL-IN / ENDING</b> (se disponíveis) para soar como banda.</li></ul>
<div class="warn">⚠️ Use o ACCOMP como banda de treino, mas não como muleta: no culto com banda ele fica <b>desligado</b>.</div>
<p>Ao usar o Modo Acordes Caindo com ritmo ligado, o microfone ouve a bateria também — se a detecção piorar, abaixe o volume do ritmo.</p>`,
    quiz: [{ q: 'Qual modo de acorde treina mais sua técnica?', opts: ['CASIO CHORD', 'FINGERED', 'Nenhum', 'Tanto faz'], a: 1 }] },
  { id: 'm9l4', title: 'Dinâmica e timbres', xp: 35, html: `
<p>Tocar bem é também saber <b>quando tocar menos</b>. Momentos de oração: pad/strings, poucas notas. Refrão final: piano cheio, oitavas.</p>
<ul><li><b>Piano</b>: base de tudo.</li><li><b>Strings / Pad</b>: fundo de ministração — segure acordes longos, sem ritmo.</li><li><b>Órgão</b>: hinos, celebrações tradicionais.</li><li><b>Electric Piano</b>: baladas modernas.</li></ul>
<p>Se o seu teclado não responder à força do toque (confira no manual se o CTK-1200 tem <i>touch response</i>), a dinâmica vem do <b>número de notas</b> e do <b>registro</b>: grave = cheio, agudo = leve.</p>`,
    quiz: [{ q: 'Em um momento de oração, o ideal é...', opts: ['Ritmo rápido', 'Pad com acordes longos', 'Solo no agudo', 'Volume no máximo'], a: 1 }] }
] },

{ id: 'm10', title: 'Tocar de ouvido', icon: '👂', color: '#67e8f9', lessons: [
  { id: 'm10l1', title: 'Descobrindo o tom', xp: 45, html: `
<p>Passos (do seu material "Dicas para tocar de ouvido"):</p>
<ol><li>Cante/escute o <b>final</b> da música: a nota onde ela "descansa" geralmente é a tônica.</li><li>Procure essa nota no teclado tocando junto até bater.</li><li>Teste o acorde maior e o menor dessa nota: qual encaixa?</li><li>Pronto: você tem o tom. Agora abra o campo harmônico dele.</li></ol>
<div class="tip">💡 Na biblioteca do app, cada música mostra o tom estimado e o campo harmônico — compare com o que você achou.</div>`,
    practice: [{ label: 'Ouvido: qual acorde é a tônica?', drill: 'ear-degree' }],
    quiz: [{ q: 'Onde a música geralmente "descansa"?', opts: ['Na dominante', 'Na tônica', 'No IV', 'No diminuto'], a: 1 }] },
  { id: 'm10l2', title: 'Tirando a harmonia com o campo', xp: 50, html: `
<p>Com o tom descoberto, teste os acordes do campo na ordem de probabilidade: <b>I, V, IV, vi</b>, depois ii e iii.</p>
<p>Escute o <b>baixo</b> da música: se ele descer, pode ser um acorde com barra (G/B). Se tiver uma tensão forte antes de um acorde menor, pode ser dominante secundária.</p>
<div class="w-field" data-key="G"></div>`,
    practice: [{ label: 'Ouvido: identifique o grau', drill: 'ear-degree' }],
    quiz: [{ q: 'Quais graus testar primeiro?', opts: ['vii° e iii', 'I, V, IV e vi', 'Só o ii', 'Acordes fora do campo'], a: 1 }] },
  { id: 'm10l3', title: 'Transposição', xp: 45, html: `
<p>Transpor é mudar o tom inteiro mantendo as relações. Se o cantor pede "meio tom abaixo", <b>todos</b> os acordes descem 1 semitom: G D Em C → F# C# D#m B.</p>
<p>Pense em <b>graus</b> (I V vi IV), não em nomes: assim qualquer tom vira só "outra posição".</p>
<div class="tip">💡 Na tela de cada música há os botões <b>−½ / +½</b> para transpor. Evite o TRANSPOSE do teclado enquanto está aprendendo — ele atrapalha a memória visual.</div>`,
    quiz: [{ q: 'G–D–Em–C um tom acima fica...', opts: ['A E F#m D', 'G# D# Fm C#', 'F C Dm Bb', 'A D Em C'], a: 0 }] },
  { id: 'm10l4', title: 'Pentatônica e melodias', xp: 45, html: `
<p>A <b>pentatônica maior</b> (5 notas: 1-2-3-5-6) é a escala dos enfeites e solos no louvor — quase impossível errar!</p>
<div class="w-scale" data-root="G" data-scale="Pentatônica maior"></div>
<div class="w-scale" data-root="E" data-scale="Pentatônica menor"></div>
<p>Toque a harmonia com a ME e improvise com a pentatônica na MD. Use as <b>Cifras Melódicas</b> da sua apostila para tirar a melodia exata de cada louvor.</p>`,
    practice: [{ label: 'Tocar a pentatônica de G', drill: 'scale', root: 'G', scale: 'Pentatônica maior' }],
    quiz: [{ q: 'Quantas notas tem a pentatônica?', opts: ['4', '5', '6', '7'], a: 1 }] }
] },

{ id: 'm11', title: 'Nível avançado', icon: '🚀', color: '#fda4af', lessons: [
  { id: 'm11l1', title: 'Empréstimo modal: iv menor, bVII, bVI', xp: 55, html: `
<p>Acordes "emprestados" do tom menor dão aquele toque emocionante do gospel:</p>
<ul><li><b>iv menor</b> (Fm em C): C – F – <b>Fm</b> – C. Arrepio garantido.</li><li><b>bVII</b> (Bb em C): C – <b>Bb</b> – F – C.</li><li><b>bVI – bVII – I</b> (Ab – Bb – C): final épico.</li></ul>
<div class="w-chords" data-chords="C F Fm C Ab Bb C"></div>`,
    practice: [{ label: 'iv menor e bVI–bVII–I', seq: 'C F Fm C Ab Bb C C', bpm: 64, beats: 4 }],
    quiz: [{ q: 'Em G, o iv menor é...', opts: ['Cm', 'C', 'Am', 'Dm'], a: 0 }] },
  { id: 'm11l2', title: 'Diminutos de passagem', xp: 55, html: `
<p>O diminuto liga dois acordes vizinhos por semitom no baixo: <b>C – C#° – Dm</b>, <b>F – F#° – G</b>. Toque e ouça o "cromatismo" subindo.</p>
<div class="w-chords" data-chords="C C#° Dm D#° Em F F#° G"></div>`,
    practice: [{ label: 'Subida com diminutos', seq: 'C C#° Dm D#° Em F F#° G', bpm: 66, beats: 2 }],
    quiz: [{ q: 'Qual diminuto liga F a G?', opts: ['E°', 'F#°', 'G#°', 'C#°'], a: 1 }] },
  { id: 'm11l3', title: 'Rearmonização básica', xp: 60, html: `
<p>Rearmonizar = trocar acordes mantendo a melodia. Técnicas simples:</p>
<ol><li><b>Substituir por relativa</b>: C ↔ Am, F ↔ Dm, G ↔ Em.</li><li><b>Adicionar ii–V antes do alvo</b>: antes de F, toque Gm7 – C7.</li><li><b>Trocar tríade por tétrade/add9</b>.</li><li><b>Pedal</b>: mantenha o baixo parado e mude os acordes em cima (C – F/C – G/C – C).</li></ol>
<div class="w-chords" data-chords="C F/C G/C C Am7 Dm7 G7(4) G7"></div>`,
    practice: [{ label: 'Pedal em C', seq: 'C F/C G/C C C F/C G/C C', bpm: 66, beats: 4 }],
    quiz: [{ q: 'A relativa de F é...', opts: ['Am', 'Dm', 'Em', 'Gm'], a: 1 }] },
  { id: 'm11l4', title: 'Enfeites, fills e introduções', xp: 60, html: `
<p>O que faz as pessoas perguntarem "quem é esse tecladista?":</p>
<ul><li><b>Appoggiatura</b>: toque a nota vizinha (meio tom abaixo) um instante antes da nota do acorde.</li><li><b>Fill no fim da frase</b>: 3–4 notas da pentatônica descendo até a tônica.</li><li><b>Oitavas na MD</b> tocando a melodia no refrão.</li><li><b>Intro</b>: use a progressão do refrão com add9 e arpejo lento.</li><li><b>Final</b>: IV – iv – I, ou bVI – bVII – I.</li></ul>`,
    quiz: [{ q: 'Um final clássico com empréstimo modal é...', opts: ['I–V–I', 'IV–iv–I', 'ii–iii–I', 'vi–V–I'], a: 1 }] }
] },

{ id: 'm12', title: 'Ministério e rotina', icon: '🙌', color: '#fde68a', lessons: [
  { id: 'm12l1', title: 'Tocando com banda e cantor', xp: 40, html: `
<ul><li><b>Não brigue com o baixista</b>: com baixo na banda, sua ME toca mais leve (ou nada) no grave.</li><li><b>Deixe espaço para o violão/guitarra</b>: a MD fica mais no registro médio-agudo.</li><li><b>Olhe para o ministro</b>: ele indica repetições, finais e mudanças de tom.</li><li><b>Tenha o tom de cada cantor</b> anotado.</li></ul>`,
    quiz: [{ q: 'Com baixista na banda, sua mão esquerda...', opts: ['Toca mais forte no grave', 'Fica mais leve/ausente no grave', 'Toca solos', 'Sai da música'], a: 1 }] },
  { id: 'm12l2', title: 'Montando repertório', xp: 40, html: `
<p>Monte seu repertório em <b>Músicas → Favoritas</b>. Um culto típico: 2 de celebração (rápidas), 1 de transição, 2–3 de adoração. Anote o tom e o BPM de cada uma.</p>
<p>Para cada música: (1) toque a harmonia no Modo Acordes Caindo até 100% de acerto; (2) toque no modo Tempo; (3) toque com o áudio original; (4) crie uma intro e um final.</p>` },
  { id: 'm12l3', title: 'Sua rotina de estudo', xp: 50, html: `
<p>Rotina diária de <b>30 minutos</b> (o app mede sua sequência de dias!):</p>
<table class="t"><tr><th>Tempo</th><th>O quê</th></tr>
<tr><td>5 min</td><td>Aquecimento: escalas e 5 dedos (Exercícios de Técnica)</td></tr>
<tr><td>5 min</td><td>Treino de acordes / ouvido (aba Treinos)</td></tr>
<tr><td>10 min</td><td>Próxima aula da Trilha + exercícios do nível</td></tr>
<tr><td>10 min</td><td>Uma música da apostila no Modo Acordes Caindo</td></tr></table>
<p><b>Constância vence talento.</b> 30 min todo dia rende mais que 4 horas no sábado.</p>` }
] }
];
