---
name: escopo-comercial
description: Monta um escopo comercial de projeto (proposta de escopo, visão de negócio, módulos, roadmap e esforço) no padrão Neuon Soluções. Use quando o usuário pedir "escopo comercial", "proposta de escopo", "documento comercial", "escopagem" de um cliente, ou quiser transformar um escopo técnico/levantamento em documento de negócio. Sempre pergunta quem está elaborando o escopo e estima esforço em HORAS.
---

# Escopo comercial — padrão Neuon

Produz o documento comercial que vai ao cliente: linguagem de negócio, sem SQL,
sem nome de tabela, sem jargão de implementação. O leitor é o decisor que assina.

Referência canônica: `Escopo_Comercial_Baluarte.docx` (12 capítulos) e o escopo
técnico `ESCOPO_TECNICO_FUNCIONAL_BALUARTE.md`, ambos em `Escopagem_prototipo/`.

## Duas regras que nunca são dispensadas

1. **Autoria é obrigatória.** Antes de escrever uma linha, pergunte o nome de quem
   está elaborando o escopo (e o cargo/papel, se houver). Vai no quadro de capa como
   "Escopo elaborado por" e na assinatura do rodapé. Não invente, não use o nome do
   cliente, não use "Neuon Soluções" como autor, não deduza do git. Se o usuário não
   responder, pergunte de novo — este é o único bloqueio real da skill.
2. **Esforço sempre em horas.** Toda estimativa — por módulo, por onda, total — é
   expressa em **quantidade de horas**. Nunca em meses, semanas, sprints, pontos,
   percentuais de esforço isolados ou faixas de preço. Se o material de origem fala
   em meses (o Baluarte fala), converta para horas e diga a premissa de conversão.
   Prazo em meses só pode aparecer como informação derivada, ao lado das horas, e
   apenas se o usuário pedir.

## Processo

### 1. Levantar o insumo

Leia o que existir: escopo técnico, ata de reunião, planilhas do cliente, e-mails,
transcrição. Não escreva o comercial a partir de suposição — cada número do
documento precisa de origem. Se o insumo for fino, liste o que falta e siga com
premissas explicitamente marcadas.

### 2. Entrevista mínima

Pergunte em **uma** rodada (use AskUserQuestion quando houver opções fechadas):

- **Quem está elaborando o escopo** (nome e papel) — obrigatório.
- Cliente: razão social e CNPJ.
- Plataforma-base (default: Sankhya ERP + módulos de personalização).
- Perfil de horas: valor/hora ou faixa, se o documento deve mostrar investimento.
  Se o usuário não quiser preço, o documento fica só em horas.
- Carga semanal considerada para derivar prazo (default: 160 h/mês por frente).

O resto você infere do insumo e marca como premissa.

### 3. Escrever os 12 capítulos

Siga `references/estrutura.md` — ordem, função e tom de cada capítulo, com o
quadro de capa já ajustado para autoria e horas.

### 4. Estimar

Siga `references/estimativa-horas.md`: catálogo bottom-up por tipo de item,
overheads (GP, homologação, treinamento, migração) e o formato das tabelas de
horas por módulo e por onda. Toda onda fecha com subtotal de horas; o documento
fecha com total de horas.

### 5. Distribuir em fases

Siga `references/fases-e-ciclos.md`. Cada onda é um ciclo de entrega completo com
as 7 fases (detalhamento, parametrização, desenvolvimento, integrações, migração,
testes/homologação, treinamento/go-live), e cada fase recebe horas e percentual.
A soma das fases de todas as ondas tem que fechar com o total da capa.

### 6. Entregar

- Arquivo Markdown em `<cliente>/Escopo_Comercial_<Cliente>.md` (ou na pasta que o
  usuário indicar).
- Se o usuário pedir `.docx`, converta com pandoc
  (`pandoc arq.md -o arq.docx`); se pandoc não existir, entregue o `.md` e avise.
- Ao final, informe no chat: autor registrado, total de horas e o que ficou como
  premissa em aberto.

## Erros a evitar

- Escopo sem capítulo "Fora do escopo" — é ele que evita discussão de aditivo.
- Ganho escrito como funcionalidade ("tela de orçamento") em vez de efeito no
  negócio ("estouro de teto vira bloqueio, não devolução de recurso").
- Número sem origem. Todo indicador do capítulo 1 sai do insumo do cliente.
- Estimativa em um bloco único. Horas precisam ser rastreáveis até o item e até a fase.
- Onda sem fase de testes ou sem treinamento dimensionado.
- Tabelas de horas que não fecham entre os capítulos 5, 6 e 7.
- Promessa de integração por API de terceiro sem confirmação de que a API existe.
