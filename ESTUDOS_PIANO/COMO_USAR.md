# 🎹 Tecladista PRO — seu sistema de estudo (Casio CTK-1200)

## Como abrir
1. Dê **dois cliques em `ABRIR_TECLADO.command`** (na primeira vez o macOS pode pedir:
   botão direito → Abrir → Abrir).
2. O Chrome abre em `http://localhost:8765`. Clique em **🎤 Ligar microfone** e permita.

> Precisa ser por esse atalho (localhost): abrindo o `index.html` direto, o navegador pode bloquear o microfone.

## Como o app ouve o teclado
O CTK-1200 não tem saída USB/MIDI, então o app **ouve o som do teclado pelo microfone** do Mac,
reconhece as notas e confere se você tocou o acorde certo.
- Timbre **piano**, ritmo desligado, volume ~70%, Mac perto do alto-falante do teclado.
- **Não use fone no teclado** (ele desliga o alto-falante).
- Vá em **Treinos → Microfone e calibração**: calibre o silêncio e ajuste a sensibilidade
  até que, ao tocar C, acendam só C, E e G.

## O que tem dentro
- **Modo Acordes Caindo** — os acordes descem sobre as teclas (azul = mão esquerda, rosa = mão direita) e,
  no modo *Esperar eu tocar*, só passam para o próximo quando você acerta. Também tem os modos *No tempo* e *Manual*.
  Mostra a letra, os próximos acordes, BPM, transposição e a sua pontuação.
- **652 músicas** da sua apostila "Cifra Melódica Gospel", com busca, dificuldade, tom, campo harmônico,
  transposição e simplificação de acordes. Também tem "Pai Nosso" (Pedras Vivas), da apostila "Cifras Melódicas Gospel 1".
- **➕ Adicionar música** (na tela Músicas): cole a cifra de qualquer site (acordes em cima da letra)
  e ela vira uma música tocável no Modo Acordes Caindo.
- **Trilha com 47 aulas** em 12 módulos (do zero ao avançado), com teoria, teclados ilustrados, som, prática e quiz.
- **100 exercícios** em 5 níveis (Técnica, Acordes, Ritmo, Harmonia, Ouvido, Improviso, Repertório).
- **Treinos**: encontrar notas, acordes relâmpago, escalas nota a nota, inversões e treino de ouvido
  (maior/menor, intervalos, graus do campo).
- **Dicionário de acordes** (12 notas × 24 tipos), **metrônomo** com tap, **campo harmônico** de qualquer tom,
  **transpositor**, **círculo das quintas** e **escalas**.
- **Progresso**: XP, 15 níveis, sequência de dias 🔥, calendário de estudo, 20 conquistas e backup.

## Rotina sugerida (30 min/dia)
5 min aquecimento → 5 min treino → 10 min aula + exercícios → 10 min música.

## Arquivos
- `app/` — o aplicativo (HTML/JS, roda offline).
- `app/data/songs.js` — músicas extraídas dos seus PDFs (`parse2.py` é o extrator).
