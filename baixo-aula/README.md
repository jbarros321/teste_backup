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

## Os 3 modos

| Modo | O que faz |
|---|---|
| **1 · Aprender** | Sem tempo, sem pressa. Mostra uma nota por vez e **só avança quando você acerta**. Se errar, ele diz o que você tocou e se está grave ou agudo demais. É por aqui que se começa. |
| **2 · Tocar no tempo** | Metrônomo rodando, 4 batidas de contagem e a música inteira. Ele marca em verde cada nota que você pegou no tempo certo e no fim dá uma nota de 0 a 100%. |
| **3 · Afinador** | Afinador cromático. Toque cada corda solta e ele diz se está alta ou baixa, e para que lado girar a tarraxa. **Afine sempre antes de estudar.** |

Atalhos: `Espaço` começa/para · `→` pula a nota · `←` volta uma nota.

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
| Corda Mi grave não é detectada | Note grave é difícil pro microfone de notebook. Toque mais perto do braço (som mais redondo) e um pouco mais forte. |

---

## Arquivos

```
baixo-aula/
├── iniciar.bat     ← dê dois cliques aqui
├── index.html      estrutura da página
├── estilo.css      visual
├── musicas.js      as músicas e exercícios  ← mexa aqui pra adicionar música
├── audio.js        microfone, detecção de nota, metrônomo
├── app.js          a lógica da aula
└── servidor.js     servidor local (Node, sem dependências)
```

As músicas conhecidas estão em **versões curtas e simplificadas, para estudo**.
