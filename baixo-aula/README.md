# 🎸 Aula de Contrabaixo — 4 cordas

Um professor de baixo que roda no navegador. Você escolhe a música, ele mostra
**qual corda, qual casa e qual dedo usar**, e **escuta você tocando pelo microfone**
para dizer se acertou ou errou.

Não precisa instalar nada nem ter internet. Não precisa de cabo, captador nem
interface: o microfone do notebook/celular já resolve.

---

## Como abrir

Dê **dois cliques em `iniciar.bat`**. Ele sobe um servidorzinho local e abre o
navegador em `http://localhost:8765`.

> Por que não abrir o `index.html` direto? Porque os navegadores só liberam o
> microfone em `http://localhost` ou `https://`. Abrindo o arquivo direto, a
> parte de "ouvir você tocando" não funciona.

Na primeira vez o navegador vai pedir permissão do microfone — clique em
**Permitir**. Depois clique no botão **🎤 Ligar microfone** dentro da página.

### Escolher qual microfone usar

Ao lado do botão tem a lista **Entrada de áudio**. Escolha ali qual microfone
(ou interface de áudio) o app deve escutar — dá para trocar a qualquer momento,
sem recarregar a página.

- Os **nomes** das entradas só aparecem depois que você permite o microfone uma
  primeira vez; antes disso o navegador esconde por privacidade. Se aparecer
  "Entrada de áudio 1", ligue o microfone e a lista se completa sozinha.
- Plugou uma interface/microfone novo com a página aberta? Clique no **⟳** ao
  lado da lista (ele também atualiza sozinho quando detecta a mudança).
- Se você tem interface de áudio, escolha ela e ligue o baixo direto no cabo:
  a detecção fica muito mais precisa que pelo microfone do notebook.

Para fechar: `Ctrl+C` na janela preta.

---

## Os 7 modos

| Modo | O que faz |
|---|---|
| **1 · Aprender** | Sem tempo, sem pressa. Mostra uma nota por vez e **só avança quando você acerta**. Se errar, ele diz o que você tocou e se está grave ou agudo demais. É por aqui que se começa. |
| **2 · Tocar no tempo** | Metrônomo rodando, 4 batidas de contagem e a música inteira. Ele marca em verde cada nota que você pegou no tempo certo e no fim dá uma nota de 0 a 100%. |
| **3 · Afinador** | Afinador cromático. Toque cada corda solta e ele diz se está alta ou baixa, e para que lado girar a tarraxa. **Afine sempre antes de estudar.** |
| **4 · Metrônomo** | Só o tempo, sem música nenhuma. |
| **5 · Escalas e tons** | Você escolhe o tom; ele escreve a partitura e toca com metrônomo. |
| **6 · Tocar junto** | Toca uma música do seu computador com velocidade, tom e loop A–B. |
| **7 · Buscar partitura** | Digite o nome da música e escolha o instrumento — ele acha a tablatura. |

Atalhos: `Espaço` começa/para (nos modos 1, 2, 4 e 6) · `→` pula a nota · `←` volta uma nota.

---

## 4 · Metrônomo

Um metrônomo de verdade, separado da aula:

- **BPM de 20 a 280** — slider, botões de ±1 e ±5, atalhos rápidos e **tap** (bata o
  tempo com o dedo no botão e ele descobre a velocidade).
- **Batidas por compasso** de 1 a 12. Cada batida é um círculo na tela; **clique nele**
  para trocar entre **forte** (laranja), normal e **mudo** (riscado). Deixar batidas
  mudas é o melhor exercício de tempo que existe.
- **Subdivisão**: semínimas, colcheias, tercinas ou semicolcheias.
- **Som**: clique seco, madeira ou bip.
- **Treino progressivo**: ele sobe o BPM sozinho (ex.: +5 a cada 4 compassos, até 140).
  Ligue, deixe rodando e repita o trecho — é assim que se ganha velocidade sem vício.

O tempo é agendado no relógio do áudio, não no relógio da tela: não desanda mesmo se
o navegador engasgar.

---

## 5 · Escalas e tons

Escolha a **tônica** (as 12 notas), **o que estudar** (18 opções: maior, menor natural,
harmônica, melódica, os 7 modos gregos, pentatônicas, blues, cromática e os arpejos
maior, menor, 7, m7 e maj7), a **extensão** (1 ou 2 oitavas, só subindo ou subindo e
descendo), o **ritmo** e a **posição no braço**.

Clicando em **Gerar partitura** ele:

1. escreve a **tablatura** (dá para copiar e imprimir) com o **dedilhado** já resolvido —
   um dedo por casa, e a mão desliza quando precisa;
2. desenha o **braço inteiro** com todas as notas daquele tom (a tônica em laranja);
3. **carrega a escala dentro da aula**, então os botões *Tocar com metrônomo* e
   *Estudar nota por nota* funcionam igual a uma música: braço, tablatura, contagem
   e correção pelo microfone.

> Se a escala escolhida não couber até a casa 12, o campo *Posição* avisa — troque
> para 1 oitava ou escolha outra corda para a tônica.

---

## 6 · Tocar junto (a ideia do Moises, aqui dentro)

No alto do painel ficam os **links do Moises** — eles são lidos do arquivo `tex.md`
(um link por linha). Colando um link novo no campo, ele abre e fica salvo no navegador.

Embaixo, um player para um arquivo **do seu computador** (arraste o MP3/WAV ou clique
em *Escolher arquivo*). Nada é enviado para lugar nenhum. Dá para:

- **mudar a velocidade** de 25% a 150% **sem mudar o tom**;
- **transpor** de −6 a +6 semitons **sem mudar a velocidade**;
- **abafar o baixo da gravação**, para você ser o baixo;
- **tirar o que está no centro** da imagem estéreo (normalmente a voz);
- marcar **A** e **B** e repetir aquele trecho sem parar;
- pôr um **metrônomo por cima** da música.

> Sobre a separação de faixas: tirar o baixo e tirar o centro aqui são equalização,
> não separação de verdade. Para a separação real (baixo, voz, bateria em faixas
> separadas), use o Moises pelos links do topo, **baixe a faixa sem o baixo** e abra
> esse arquivo aqui — aí você tem o melhor dos dois: separação boa + velocidade, tom
> e loop A–B.

---

## 7 · Buscar partitura

Digite o nome da música (o nome do artista junto ajuda), escolha o **instrumento** e
clique em buscar. Ele procura no catálogo do **Songsterr** e lista só as músicas que
têm faixa daquele instrumento, mostrando também a **afinação** de cada faixa. Clicando
na faixa, a partitura interativa abre numa aba nova.

Precisa de **internet** e de a página estar rodando pelo `iniciar.bat` com o **Node**
instalado (quem conversa com o Songsterr é o `servidor.js`; o navegador sozinho é
barrado por CORS). Se der errado, ele mostra links de busca no Cifra Club, no
Ultimate Guitar e no próprio Songsterr.

---

## A tela

- **Cartão da nota atual** — corda (nome em português e a letra), casa, dedo da mão
  esquerda, dedo da mão direita e que som deve sair.
- **Braço do baixo** — a bolinha laranja é onde apertar agora (o número dentro dela
  é o dedo). A cinza é a próxima nota. Verde = acertou, vermelho = errou.
- **Tablatura** — as 4 linhas são as cordas (G em cima, E embaixo) e o número é a
  casa. `0` = corda solta. Pode clicar em qualquer coluna para pular pra lá.
- **Velocidade (BPM)** — comece em 50%. Só suba quando acertar tudo. Sério.

---

## Como o baixo é numerado aqui

| Corda | Nome | Ordem | Grossura |
|---|---|---|---|
| E | Mi | 4ª | a mais grossa |
| A | Lá | 3ª | |
| D | Ré | 2ª | |
| G | Sol | 1ª | a mais fina |

**Dedos da mão esquerda:** 1 = indicador · 2 = médio · 3 = anelar · 4 = mindinho.
Regra de ouro: **um dedo por casa**, e aperte logo *atrás* do traste, nunca em cima.

**Mão direita:** alterne sempre indicador e médio (i–m–i–m). O polegar fica apoiado
no captador ou na corda Mi.

---

## Ordem sugerida de estudo

1. Afinador (todo dia, antes de tudo)
2. Exercício 1 · Cordas soltas
3. Exercício 2 · Dedilhado cromático 1-2-3-4
4. With or Without You
5. Exercício 3 · Escala de Sol maior
6. Stand By Me
7. Seven Nation Army
8. Exercício 4 · Blues de 12 compassos
9. Smoke on the Water

15 a 20 minutos por dia rendem mais que 2 horas no sábado.

---

## Adicionar uma música nova

**Jeito fácil:** peça pro Claude — *"adiciona a música X no baixo-aula"* — e ele
escreve a tablatura no `musicas.js`.

**Na mão:** abra `musicas.js` e copie o formato de uma das músicas. Cada nota é:

```js
{ c: 'A', f: 7, d: 2, t: 0, dur: 1 }
//  c   = corda: 'E' (Mi), 'A' (Lá), 'D' (Ré), 'G' (Sol)
//  f   = casa (0 = corda solta)
//  d   = dedo da mão esquerda (0 solta, 1 indicador, 2 médio, 3 anelar, 4 mindinho)
//  t   = em qual batida ela entra (0 = a primeira)
//  dur = quantas batidas ela dura (1 = semínima, 0.5 = colcheia, 2 = mínima)
```

Para não contar tempo na mão, use o atalho `seq()`, que enfileira as notas:

```js
notas: seq(0, [
  ['A', 7, 2, 1.5],   // corda, casa, dedo, duração
  ['A', 7, 2, 0.5],
  ['A', 10, 4, 1],
])
```

E `repetir(notas, vezes, batidasPorCiclo)` repete um trecho.

---

## Se não funcionar

| Problema | Solução |
|---|---|
| Não pede permissão do microfone | Você abriu o `index.html` direto. Use o `iniciar.bat`. |
| Ele não escuta nada | Confira se a **entrada de áudio** selecionada é a certa (pode estar pegando um microfone que não é o que você quer). A barrinha verde no topo mostra se está entrando som. Depois: chegue mais perto, aumente o volume do baixo e toque com força. |
| Bloqueei o microfone sem querer | Clique no cadeado 🔒 ao lado do endereço, libere o microfone e recarregue. |
| Ele erra a nota / fica pulando | Use fone de ouvido (o som do metrônomo e do exemplo pelo alto-falante confunde o microfone). Abafe as cordas que não estão sendo tocadas. |
| Diz que está sempre desafinado | Rode o afinador primeiro. Baixo desafinado = tudo errado. |
| A busca de partitura não acha nada | Ela precisa de internet **e** do Node (o `iniciar.bat` usa Python como plano B, e nesse caso a busca não funciona). Instale o Node e rode de novo. |
| O botão de transpor não faz nada | O pitch shifter usa AudioWorklet — precisa de um navegador atual (Chrome, Edge ou Firefox recentes) e da página em `http://localhost`. |
| Corda Mi grave não é detectada | Note grave é difícil pro microfone de notebook. Toque mais perto do braço (som mais redondo) e um pouco mais forte. |

---

## Arquivos

```
baixo-aula/
├── iniciar.bat     ← dê dois cliques aqui
├── index.html      estrutura da página
├── estilo.css      visual
├── musicas.js      as músicas e exercícios  ← mexa aqui pra adicionar música
├── audio.js        microfone, detecção de nota, sons do metrônomo
├── app.js          a lógica da aula (modos 1, 2 e 3)
├── metronomo.js    o metrônomo sozinho (modo 4)
├── escalas.js      gerador de escalas, arpejos e dedilhado (modo 5)
├── playalong.js    player com velocidade, tom e loop A–B (modo 6)
├── partituras.js   busca de partituras por instrumento (modo 7)
├── tex.md          seus links do Moises, um por linha
└── servidor.js     servidor local + ponte para a busca (Node, sem dependências)
```

As músicas conhecidas estão em **versões curtas e simplificadas, para estudo**.
