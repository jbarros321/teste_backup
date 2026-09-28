// 100 exercícios progressivos. act: {seq,bpm,beats} = Modo Acordes Caindo | {drill} = Treinos | {song:n} = música da apostila
const LEVELS = ['', 'Iniciante', 'Básico', 'Intermediário', 'Avançado', 'Mestre'];
const EX = [];
function ex(lvl, cat, t, d, act, bpm) { EX.push({ id: EX.length + 1, lvl, cat, t, d, act: act || null, bpm: bpm || null }); }

// ---------- NÍVEL 1 — INICIANTE ----------
ex(1, 'Técnica', 'Mapa das teclas pretas', 'Toque todos os grupos de 2 pretas do teclado, da esquerda para a direita, depois todos os grupos de 3. Diga em voz alta "dois" e "três".');
ex(1, 'Leitura', 'Caça aos Dós', 'Toque todos os Dós do teclado (5 no CTK-1200 + o último). Depois todos os Fás. Depois todos os Sóis.', { drill: 'notes' });
ex(1, 'Leitura', 'Notas aleatórias', 'No treino de notas, acerte 20 notas seguidas em menos de 3 segundos cada.', { drill: 'notes' });
ex(1, 'Técnica', '5 dedos MD – subida', 'Posição de Dó, MD: C D E F G e volta (1-2-3-4-5-4-3-2-1). Mantenha o som ligado (legato), dedos curvos.', { drill: 'scale', root: 'C', scale: 'five' }, 60);
ex(1, 'Técnica', '5 dedos ME – subida', 'Posição de Dó, ME: C3 D3 E3 F3 G3 (5-4-3-2-1) e volta. Mesma atenção ao legato.', null, 60);
ex(1, 'Técnica', '5 dedos mãos juntas', 'As duas mãos na posição de Dó (ME uma oitava abaixo) subindo e descendo juntas. É a primeira coordenação — vá devagar.', null, 50);
ex(1, 'Ritmo', 'Batendo o pulso', 'Ligue o metrônomo a 70 BPM e toque um Dó em cada batida por 1 minuto sem adiantar nem atrasar. Conte "1-2-3-4".', null, 70);
ex(1, 'Ritmo', 'Semínimas e mínimas', 'Metrônomo 70: toque C em semínimas (1 por batida) por 4 compassos, depois em mínimas (a cada 2 batidas) por 4 compassos. Alterne.', null, 70);
ex(1, 'Acordes', 'Primeiro acorde: C', 'Monte C (C E G) com os dedos 1-3-5 da MD. Toque, solte e toque de novo 10 vezes, sem olhar na 5ª vez em diante.', { seq: 'C C C C C C C C', bpm: 50, beats: 4 });
ex(1, 'Acordes', 'C, F e G', 'Os três acordes que mais aparecem nas músicas. Troque devagar, mantendo o polegar como guia.', { seq: 'C F G C C F G C', bpm: 50, beats: 4 });
ex(1, 'Acordes', 'G, C e D', 'O "trio" do tom de Sol. Muito comum em corinhos.', { seq: 'G C D G G C D G', bpm: 50, beats: 4 });
ex(1, 'Acordes', 'D, G e A', 'O trio de Ré.', { seq: 'D G A D D G A D', bpm: 50, beats: 4 });
ex(1, 'Acordes', 'A, D e E', 'O trio de Lá. Atenção às teclas pretas (C#, F#, G#).', { seq: 'A D E A A D E A', bpm: 50, beats: 4 });
ex(1, 'Acordes', 'Maiores brancos → menores', 'C→Cm, F→Fm, G→Gm: só o dedo do meio desce meio tom. Sinta a mudança de cor.', { seq: 'C Cm F Fm G Gm C Cm', bpm: 50, beats: 4 });
ex(1, 'Acordes', 'Os 3 menores "brancos"', 'Am, Dm e Em — só teclas brancas.', { seq: 'Am Dm Em Am Am Dm Em Am', bpm: 50, beats: 4 });
ex(1, 'Acordes', 'Baixo + acorde', 'ME toca a fundamental (C2, F2, G2) e MD o acorde, juntas. Primeira coordenação real de acompanhamento.', { seq: 'C F G C C F G C', bpm: 55, beats: 4 });
ex(1, 'Acordes', 'Os 14 iniciais', 'Treino relâmpago com os 7 maiores e 7 menores. Meta: acertar todos em menos de 5 s cada.', { drill: 'chords', set: 'C D E F G A B Cm Dm Em Fm Gm Am Bm' });
ex(1, 'Ouvido', 'Maior ou menor?', 'No treino de ouvido, acerte 10 seguidas.', { drill: 'ear-quality' });
ex(1, 'Técnica', 'Escala de Dó MD', 'C D E F G A B C com dedilhado 1-2-3-1-2-3-4-5. O polegar passa por baixo do 3.', { drill: 'scale', root: 'C' }, 60);
ex(1, 'Técnica', 'Escala de Dó ME', 'C3 a C4 com ME: 5-4-3-2-1-3-2-1 (o dedo 3 passa por cima do polegar).', null, 60);
ex(1, 'Repertório', 'Agnus Dei (A D E)', 'Primeira música completa! Só 3 acordes.', { song: 93 });
ex(1, 'Repertório', 'Glória Glória Senhor', 'Três acordes em Sol (G C D).', { song: 47 });
ex(1, 'Repertório', 'És Minha Casa', 'F C G — atenção ao F.', { song: 55 });
ex(1, 'Ritmo', 'Troca no tempo 1', 'Progressão C–G–Am–F, trocando exatamente no tempo 1 de cada compasso, metrônomo 60.', { seq: 'C G Am F C G Am F', bpm: 60, beats: 4 });
ex(1, 'Leitura', 'Cifra → notas', 'Pegue 10 cifras aleatórias da apostila e escreva as notas de cada acorde. Confira no Dicionário de Acordes.');

// ---------- NÍVEL 2 — BÁSICO ----------
ex(2, 'Acordes', 'Todos os maiores', 'Os 12 acordes maiores, pelo ciclo das quintas: C G D A E B F# Db Ab Eb Bb F.', { seq: 'C G D A E B F# Db Ab Eb Bb F', bpm: 50, beats: 4 });
ex(2, 'Acordes', 'Todos os menores', 'Os 12 menores pelo ciclo: Am Em Bm F#m C#m G#m D#m Bbm Fm Cm Gm Dm.', { seq: 'Am Em Bm F#m C#m G#m D#m Bbm Fm Cm Gm Dm', bpm: 50, beats: 4 });
ex(2, 'Acordes', 'Treino relâmpago: 24 acordes', 'Maiores e menores em todos os tons. Meta: média abaixo de 3 s.', { drill: 'chords', set: 'C C# D Eb E F F# G Ab A Bb B Cm C#m Dm Ebm Em Fm F#m Gm G#m Am Bbm Bm' });
ex(2, 'Harmonia', 'Campo harmônico de C', 'Toque os 7 acordes subindo e descendo: C Dm Em F G Am B°.', { seq: 'C Dm Em F G Am B° C', bpm: 60, beats: 2 });
ex(2, 'Harmonia', 'Campo harmônico de G', 'G Am Bm C D Em F#° G.', { seq: 'G Am Bm C D Em F#° G', bpm: 60, beats: 2 });
ex(2, 'Harmonia', 'Campo harmônico de D', 'D Em F#m G A Bm C#° D.', { seq: 'D Em F#m G A Bm C#° D', bpm: 60, beats: 2 });
ex(2, 'Harmonia', 'Campo harmônico de A', 'A Bm C#m D E F#m G#° A.', { seq: 'A Bm C#m D E F#m G#° A', bpm: 60, beats: 2 });
ex(2, 'Harmonia', 'I–V–vi–IV em 4 tons', 'A progressão mais usada do mundo em C, G, D e A.', { seq: 'C G Am F G D Em C D A Bm G A E F#m D', bpm: 70, beats: 4 });
ex(2, 'Técnica', 'Escala de G', 'G maior MD (1 sustenido: F#). Mesmo dedilhado de Dó.', { drill: 'scale', root: 'G' }, 70);
ex(2, 'Técnica', 'Escala de D', 'D maior (F#, C#).', { drill: 'scale', root: 'D' }, 70);
ex(2, 'Técnica', 'Escala de F', 'F maior (Bb). Dedilhado MD diferente: 1-2-3-4-1-2-3-4 (o 4 fica no Bb).', { drill: 'scale', root: 'F' }, 70);
ex(2, 'Técnica', 'Inversões de C', 'Fundamental → 1ª inv → 2ª inv → oitava acima e volta. Mão como bloco.', { drill: 'inversions' }, 60);
ex(2, 'Técnica', 'Inversões de todos os maiores brancos', 'Faça o ex. anterior com F e G. Depois D, A, E.', { drill: 'inversions' });
ex(2, 'Ouvido', 'Intervalos básicos', '3ª maior, 3ª menor, 4ª, 5ª e oitava. Acerte 15 de 20.', { drill: 'ear-intervals' });
ex(2, 'Ritmo', 'Colcheias', 'Metrônomo 70: acorde C tocado 2 vezes por batida (colcheias) por 8 compassos, sem acelerar.', null, 70);
ex(2, 'Ritmo', 'Levada balada', 'ME no tempo 1, MD segura o acorde. C–G–Am–F, 72 BPM.', { seq: 'C G Am F C G Am F', bpm: 72, beats: 4 });
ex(2, 'Repertório', 'Anjos ao Redor do Trono', 'C Am F G — conduza a MD sem pular.', { song: 50 });
ex(2, 'Repertório', 'Eu Navegarei', 'Tom menor: Am G F E. Sinta o E maior (dominante) voltando para Am.', { song: 39 });
ex(2, 'Repertório', 'Quão Lindo Esse Nome É', 'D G Bm A — o clássico de adoração.', { song: 150 });
ex(2, 'Repertório', 'Amém', 'D Bm F#m E A, com o E fora do campo (dominante secundária!).', { song: 100 });

// ---------- NÍVEL 3 — INTERMEDIÁRIO ----------
ex(3, 'Acordes', 'Baixo invertido em C', 'C G/B Am G F C/E Dm G — ME desce enquanto MD conduz.', { seq: 'C G/B Am G F C/E Dm G', bpm: 70, beats: 4 });
ex(3, 'Acordes', 'Baixo invertido em D', 'D A/C# Bm A G D/F# Em A.', { seq: 'D A/C# Bm A G D/F# Em A', bpm: 70, beats: 4 });
ex(3, 'Acordes', 'Baixo invertido em G', 'G D/F# Em D C G/B Am D.', { seq: 'G D/F# Em D C G/B Am D', bpm: 70, beats: 4 });
ex(3, 'Acordes', 'Sus4 resolvendo', 'A4→A, D4→D, E4→E, G4→G. O "4" é o suspense do louvor.', { seq: 'A4 A D4 D E4 E G4 G', bpm: 66, beats: 4 });
ex(3, 'Acordes', 'add9 em todas as posições', 'Cadd9 Gadd9 Dadd9 Aadd9 Eadd9 Fadd9.', { seq: 'Cadd9 Gadd9 Dadd9 Aadd9 Eadd9 Fadd9', bpm: 60, beats: 4 });
ex(3, 'Acordes', 'Tétrades de C', 'C7M Dm7 Em7 F7M G7 Am7 Bm7(b5) C7M.', { seq: 'C7M Dm7 Em7 F7M G7 Am7 Bm7(b5) C7M', bpm: 60, beats: 4 });
ex(3, 'Acordes', 'Tétrades de G', 'G7M Am7 Bm7 C7M D7 Em7 F#m7(b5) G7M.', { seq: 'G7M Am7 Bm7 C7M D7 Em7 F#m7(b5) G7M', bpm: 60, beats: 4 });
ex(3, 'Harmonia', 'Campo de E, F e Bb', 'Os tons "difíceis" do gospel. Um de cada vez.', { seq: 'E F#m G#m A B C#m E F Gm Am Bb C Dm F Bb Cm Dm Eb F Gm Bb', bpm: 60, beats: 2 });
ex(3, 'Harmonia', 'ii–V–I em 6 tons', 'Dm7 G7 C7M / Am7 D7 G7M / Em7 A7 D7M / Gm7 C7 F7M / Bm7 E7 A7M / Cm7 F7 Bb7M.', { seq: 'Dm7 G7 C7M Am7 D7 G7M Em7 A7 D7M Gm7 C7 F7M Bm7 E7 A7M Cm7 F7 Bb7M', bpm: 66, beats: 4 });
ex(3, 'Harmonia', 'Dominantes secundárias', 'C E7 Am A7 Dm G7 C — sinta cada dominante "puxando".', { seq: 'C E7 Am A7 Dm G7 C C', bpm: 64, beats: 4 });
ex(3, 'Técnica', 'Arpejo 1-5-8 na ME', 'C2 G2 C3 em colcheias, em cada acorde de C–Am–F–G.', { seq: 'C Am F G C Am F G', bpm: 64, beats: 4 });
ex(3, 'Técnica', 'Arpejo na MD', 'MD arpeja o acorde (1-3-5-3) em colcheias enquanto ME segura a fundamental.', { seq: 'G Em C D G Em C D', bpm: 66, beats: 4 });
ex(3, 'Técnica', 'Escalas com as duas mãos', 'C, G e D maior, mãos juntas, 1 oitava.', null, 60);
ex(3, 'Ouvido', 'Graus no campo', 'Identifique o grau (I, IV, V, vi) de acordes tocados após a tônica.', { drill: 'ear-degree' });
ex(3, 'Ritmo', 'Levada celebração', 'MD em colcheias com acento no contratempo, 110 BPM: G C D G.', { seq: 'G C D G G C D G', bpm: 110, beats: 4 });
ex(3, 'Ritmo', 'Valsa 3/4', 'ME no 1, MD no 2 e 3: C F G7 C.', { seq: 'C F G7 C C F G7 C', bpm: 90, beats: 3 });
ex(3, 'Repertório', 'Lugar Secreto', 'F Am G Dm Em — tom de F.', { song: 482 });
ex(3, 'Repertório', 'Maranata', 'C G Am F G/B — use a inversão no G/B.', { song: 59 });
ex(3, 'Repertório', 'Ninguém Explica Deus', 'Com B7 (dominante secundária de Em).', { song: 496 });
ex(3, 'Repertório', 'Oceanos', 'Tom de Bm com A/C# no baixo.', { song: 144 });

// ---------- NÍVEL 4 — AVANÇADO ----------
ex(4, 'Acordes', 'Voicings worship', 'Dadd9 Bm7 Gadd9 A4 — use 1-2-3-5 na MD e oitava na ME.', { seq: 'Dadd9 Bm7 Gadd9 A4 Dadd9 Bm7 Gadd9 A', bpm: 68, beats: 4 });
ex(4, 'Acordes', 'Tétrades em todos os tons', 'X7M, X7, Xm7 para as 12 notas (ciclo das quintas). Faça em 3 dias.', { drill: 'chords', set: 'C7M G7M D7M A7M E7M F7M Bb7M C7 G7 D7 A7 E7 B7 F7 Cm7 Gm7 Dm7 Am7 Em7 Bm7 F#m7' });
ex(4, 'Acordes', 'Diminutos de passagem', 'C C#° Dm D#° Em F F#° G.', { seq: 'C C#° Dm D#° Em F F#° G', bpm: 66, beats: 2 });
ex(4, 'Harmonia', 'iv menor', 'C F Fm C / G C Cm G / D G Gm D.', { seq: 'C F Fm C G C Cm G D G Gm D', bpm: 64, beats: 4 });
ex(4, 'Harmonia', 'bVI–bVII–I', 'Final épico: Ab Bb C / Eb F G / Bb C D.', { seq: 'Ab Bb C C Eb F G G Bb C D D', bpm: 70, beats: 4 });
ex(4, 'Harmonia', 'Pedal de tônica', 'C F/C G/C C — o baixo fica parado em Dó.', { seq: 'C F/C G/C C C F/C G/C C', bpm: 66, beats: 4 });
ex(4, 'Harmonia', 'Modulação meio tom acima', 'Último refrão subindo: C G Am F → G7(4)... → Db Ab Bbm Gb.', { seq: 'C G Am F C G Am F Ab7 Db Ab Bbm Gb Db Ab Bbm Gb', bpm: 72, beats: 4 });
ex(4, 'Harmonia', 'Modulação um tom acima com dominante', 'G D Em C → A7 → D A Bm G.', { seq: 'G D Em C G D Em C A7 A7 D A Bm G D A Bm G', bpm: 72, beats: 4 });
ex(4, 'Técnica', 'Escalas em todos os tons (1 oitava)', 'As 12 escalas maiores MD, ciclo das quintas, 80 BPM em colcheias.', null, 80);
ex(4, 'Técnica', 'Pentatônica maior em G e D', 'Suba e desça 2 oitavas. Base para os enfeites.', { drill: 'scale', root: 'G', scale: 'Pentatônica maior' }, 80);
ex(4, 'Improviso', 'Fill de 4 notas', 'Toque G–D–Em–C na ME e, no fim de cada compasso, um fill de 4 notas da pentatônica de G na MD.', { seq: 'G D Em C G D Em C', bpm: 70, beats: 4 });
ex(4, 'Improviso', 'Melodia em oitavas', 'Toque a melodia do refrão de uma música conhecida em oitavas na MD (dedos 1 e 5). Use as Cifras Melódicas da apostila.');
ex(4, 'Ouvido', 'Tirar uma música sozinho', 'Escolha um louvor que você NÃO tem cifra. Descubra o tom e a harmonia com o campo harmônico. Depois compare com uma cifra.');
ex(4, 'Ouvido', 'Ditado de progressão', 'Peça para alguém tocar 4 acordes do campo de C (ou use o treino de graus) e escreva a progressão.', { drill: 'ear-degree' });
ex(4, 'Ritmo', 'Levada 6/8', 'ME no 1 e no 4, MD arpejando em colcheias: D G A D em 6/8.', { seq: 'D G A D D G A D', bpm: 60, beats: 6 });
ex(4, 'Ritmo', 'Contratempo com metrônomo', 'Metrônomo a 90: toque os acordes SÓ no contratempo ("e") durante 16 compassos.', null, 90);
ex(4, 'Repertório', 'Ousado Amor', 'C#m B4 Aadd9 E — tom com 4 sustenidos.', { song: 545 });
ex(4, 'Repertório', 'Lugar Secreto (versão com tétrades)', 'F7M(9) Am G4(6) Dm7(9) Em7.', { song: 483 });
ex(4, 'Repertório', 'Deserto', 'B maior com baixos invertidos e F#4.', { song: 194 });
ex(4, 'Repertório', 'Ninguém Explica Deus (Isadora Pompeo)', 'Versão com 7M, add9 e D/F#.', { song: 520 });

// ---------- NÍVEL 5 — MESTRE ----------
ex(5, 'Harmonia', 'Rearmonize um refrão', 'Pegue um refrão I–V–vi–IV e rearmonize: tétrades, relativas, ii–V antes do IV, iv menor no final.');
ex(5, 'Harmonia', 'Cadência gospel completa', 'C7M – E7 – Am7 – C7 – F7M – Fm6 – C/G – G7(4) – C.', { seq: 'C7M E7 Am7 C7 F7M Fm6 C/G G7(4) C', bpm: 60, beats: 4 });
ex(5, 'Harmonia', 'Ciclo de dominantes', 'E7 A7 D7 G7 C — cada acorde é dominante do próximo.', { seq: 'E7 A7 D7 G7 C C', bpm: 70, beats: 4 });
ex(5, 'Harmonia', 'Substituto tritonal', 'Dm7 Db7 C7M — o Db7 substitui o G7.', { seq: 'Dm7 Db7 C7M C7M Am7 Ab7 G7M G7M', bpm: 66, beats: 4 });
ex(5, 'Acordes', 'Treino relâmpago mestre', 'Acordes com extensões em todos os tons. Meta: média abaixo de 4 s.', { drill: 'chords', set: 'Cadd9 Dadd9 Eadd9 Gadd9 Aadd9 Em7(9) Am7(9) Bm7(11) F7M(9) G7(13) E7(b9) A4 D4 G/B D/F# A/C# F#m7(b5) C#°' });
ex(5, 'Técnica', 'Escalas 2 oitavas mãos juntas', 'Todas as maiores, 2 oitavas, mãos juntas, 90 BPM em colcheias.', null, 90);
ex(5, 'Técnica', 'Escalas menores harmônicas', 'Am, Em, Bm, Dm harmônicas, MD e ME.', { drill: 'scale', root: 'A', scale: 'Menor harmônica' }, 80);
ex(5, 'Improviso', 'Solo de 16 compassos', 'Grave a base (ACCOMP do teclado ou alguém tocando) e improvise 16 compassos na pentatônica + notas do acorde.');
ex(5, 'Improviso', 'Intro autoral', 'Crie uma introdução de 8 compassos para 3 músicas do seu repertório, usando add9 e arpejos.');
ex(5, 'Improviso', 'Final autoral', 'Crie finais com IV–iv–I e bVI–bVII–I para 3 músicas.');
ex(5, 'Ouvido', 'Transposição na hora', 'Toque uma música no tom original e, sem olhar cifra, em +2 e −2 semitons.');
ex(5, 'Ouvido', 'Tirar 5 músicas de ouvido', 'Uma por semana, sem cifra. Anote a harmonia e compare.');
ex(5, 'Repertório', 'Repertório de culto', 'Monte e toque um repertório de 6 músicas (2 celebração, 1 transição, 3 adoração) sem parar, com transições.');
ex(5, 'Repertório', '20 músicas 100%', 'Complete 20 músicas da apostila com 100% de acerto no Modo Acordes Caindo.');
ex(5, 'Repertório', 'Toque com banda/cantor', 'Acompanhe um cantor ao vivo (ou gravação) do início ao fim, seguindo repetições e dinâmica. Você é tecladista! 🙌');
