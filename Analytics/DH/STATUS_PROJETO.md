# Status do Projeto — Indicadores de Armazenagem (Analytics/DH)

> Arquivo de linha de raciocínio. Atualizar a cada mudança relevante para que qualquer sessão futura
> (humana ou IA) entenda rapidamente o estado do projeto.

**Última atualização:** 14/07/2026

## O que é

Componente HTML único (`index.html`) embarcado na plataforma **Mitra** — dashboard de lançamentos de
armazenagem por parceiro. Não há build, framework ou backend próprio: tudo é HTML/CSS/JS inline, com
Tailwind (CDN), SheetJS/XLSX (CDN) e Chart.js (CDN).

### Integração com a plataforma Mitra (APIs globais injetadas)

- `queryMitra({ query })` — executa SQL (as queries vêm em `window.componentData.*`, ex.: `queryParceiros`, `queryManuais`).
- `setVariableMitra({ name, content })` — define variáveis de bind (`:VAR_PARCEIRO_SELECIONADO`, `:VAR_PERIODO_INICIO/FIM`, `:VAR_ITEM_DEL_ID`).
- `formMitra({ id: 8 })` — abre o formulário de lançamento manual.
- `modalMitra({ id })` — modais (itens ERP = `componentData.modalItensErpId`; PDF = id 13).
- `dbactionMitra(id)` — ação de banco (delete de lançamento manual = `componentData.dbActionDeleteItemId`).
- `updateComponentsMitra({ all: true })` — pede refresh de todos os componentes; a plataforma responde
  disparando o evento customizado `mitra-update-lancamentos-view` no `body`, que chama `fetchAndRenderNotas()`.

### Fluxo de navegação (3 telas no mesmo arquivo)

1. **Parceiros** (`#parceiros-view`) → 2. **Meses/Períodos** (`#meses-view`) → 3. **Notas** (`#notas-view`:
KPIs, gráfico de evolução de saldo com eixo duplo, cards de Critérios Vinculados, cards de Lançamentos
Manuais, Total Projetado e tabela de itens consolidados).

## Histórico de decisões e correções

### 14/07/2026 — Bug: lançamento manual aparecia duplicado após salvar

**Sintoma:** ao salvar um lançamento manual, o card aparecia 2x; ao sair e voltar na tela, aparecia 1x (correto).

**Causa raiz:** condição de corrida em `fetchAndRenderNotas()`. Após salvar, o evento
`mitra-update-lancamentos-view` era disparado mais de uma vez (o form da plataforma + o
`updateComponentsMitra({all:true})` chamado no clique), gerando **duas execuções concorrentes** da função.
Como ela limpava os grids no início e depois ia **anexando** cards com `innerHTML +=` entre vários
`await`, as duas execuções anexavam os mesmos cards → duplicação visual. O dado no banco sempre esteve
correto (por isso ao reabrir mostrava certo).

**Correção aplicada (duas camadas de defesa):**
1. **Token de renderização** (`notasRenderToken`): cada execução captura um token no início; após cada
   `await`, `isStale()` verifica se uma execução mais nova começou e, se sim, aborta sem tocar no DOM.
2. **Atribuição única de HTML**: os cards de critérios/manuais e as linhas da tabela agora são montados
   em strings locais (`projetadosHtml`, `manuaisHtml`, `rowsHtml`) e atribuídos com `innerHTML =` de uma
   vez — mesmo que duas execuções terminem, o resultado substitui em vez de acumular.

### 14/07/2026 — Melhoria visual dos cards de critérios

- Acento lateral colorido por tipo: **azul** = critério projetado, **âmbar** = lançamento manual (classe `.criterio-card.manual`).
- Ícone em "pastilha" ao lado do nome (calculadora = critério, lápis = manual).
- Badge em caixa alta; badge "Manual" agora âmbar (antes cinza).
- Total em destaque (15px, negrito, tabular-nums) com rótulo "TOTAL" discreto e divisor tracejado.
- Hover com elevação (translateY + sombra), nome com clamp de 2 linhas + `title` (tooltip) para nomes longos.
- **Atenção:** a exportação Excel ("Exportar Itens") lê os cards pelo DOM usando as classes
  `.criterio-nome`, `.criterio-row-value` e `.criterio-total-row .value` — essas classes foram preservadas
  no redesign. Se mudar a estrutura dos cards, revisar o handler de `#export-excel-btn`.

### 14/07/2026 — Relatório PDF não mostrava/somava lançamentos manuais

**Arquivo:** `..\Index2.html` (Analytics/Index2.html — "Relatório de Lançamento de Armazenagem", aberto via `modalMitra` a partir da tela principal).

**Causa raiz:** o relatório só consultava `componentData.queryItens` (critérios de cobrança); não existia nenhuma consulta de lançamentos manuais nesse componente — logo eles nunca apareciam na tabela nem entravam no `totalGeral`.

**Correção:** o `initializeComponent` agora também consulta `componentData.queryManuais` (mesmo nome usado nos outros componentes). Os manuais são renderizados na tabela "Critérios de Cobrança" com selo âmbar "Manual" e somados ao "Valor Total Geral".
- A query é **opcional**: se `queryManuais` não estiver configurada no componente da plataforma, o relatório funciona normalmente sem manuais (com `catch` para não derrubar o `Promise.all`).
- Colunas resolvidas **por nome** (tolerante): critério = `NOME_CRITERIO`/`CRITERIO`/`DESCRICAO`; valor = `VALOR_TOTAL_ITEM`/`VALOR_TOTAL`/`VALOR`; opcionais `QUANTIDADE`/`QTD` e `VALOR_UNITARIO`. Fallback posicional: col 1 = critério, última col = valor.
- **Pendência de configuração:** cadastrar a query `queryManuais` no componente do relatório na plataforma Mitra (filtrando pelo mesmo lançamento/período do relatório), senão os manuais continuam ausentes.

### 14/07/2026 — DashDH: queries reescritas + redesign completo do dashboard

**Pasta:** `DashDH/` (dashboard gerencial embarcado na Mitra; queries cadastradas no componente via `componentData.*`).

**Queries (todas as 15 usadas pelo `index.html` foram reescritas):** o conjunto original veio do
Analytics IA do Sankhya e tinha erros graves: `WITHTIPO_PESO_POR_PARCEIRO` (WITH colado → sintaxe),
variável com typo `:VAR_FILTRO_PARCEiro`, subqueries correlacionadas dentro de tabelas derivadas
(ilegal em MySQL), JOIN duplo em `VINCULO_PARCEIRO_TIPO` duplicando somas, `queryProdutos` juntando
entrada/saída por `NUNOTA` (nunca casa) e composição/custo por parceiro usando valor de mercadoria
em vez do modelo de critérios. Modelo novo, comum a todas:
- `VINCULO_CORRETO` (rn=1 por parceiro) → `MOVIMENTOS` (entradas `GESTAO_NOTAS_APROVACAO`+`IMP_ITENS_NOTAS_APRV`;
  saídas `IMP_NOTAS_ARMAZ` com peso/valor negativos) → `SALDOS` com `SUM() OVER (PARTITION BY CODPARC ORDER BY DT_MOV, AUTO_ID)`.
- Pico do período segue o **modelo validado** do componente principal: saldo inicial (antes de
  `:VAR_DT_INICIO`) é **clampado em 0** (`GREATEST(...,0)`) ANTES de acumular os movimentos do período;
  pico = `GREATEST(max(saldo inicial clampado + acumulado do período), saldo inicial clampado, 0)`.
  Na evolução mensal o mesmo clamp é aplicado ao saldo de abertura de cada mês (via `FIRST_VALUE`).
- `queryKpiEstoqueAtual` segue o modelo validado `querySaldoAtual`: só `IMP_NOTAS_ARMAZ`, com
  `ATUALESTOQUE = -1` como entrada e `= 1` como saída, sempre `PESO_ITEM` (ignora tipo de peso), sem clamp.
- `queryKpiSaldo`/`queryKpiSaldoHistorico` = modelo validado `querySaldoHistorico` (`DT_MOV <= :VAR_DT_FIM`, `GREATEST(...,0)`).
- Custo por critério (mesma fórmula do componente principal): Percentual/Valor Declarado → pico de valor × %;
  Entrada/Saida → toneladas movimentadas × valor; senão pico de peso (ton) × valor.
- `queryKpiPicoPeriodo` agora retorna `DATA_PICO` em ISO (`%Y-%m-%d`); o JS formata para pt-BR.
- Os arquivos que não são consumidos pelo `index.html` novo mas estavam cadastrados em componentes
  (`queryKpis.sql`, `queryCustos.sql`, `queryServicos.sql`, `queryParceiros.sql`, `queryAnaliseCriterios.sql`)
  também foram realinhados ao modelo validado, mantendo os nomes de colunas de saída originais.
  `queryLancamentos.sql` não usa o modelo de custo e permaneceu como estava.

**Redesign do `index.html`:** tema claro para apresentação (cards brancos, sem gradientes saturados,
sem roxo). Paleta categórica validada p/ daltonismo (ordem fixa): `#2a78d6, #16a37a, #eda100, #e34948, #e87ba4, #eb6834`.
Novidades: KPI hero "Custo Projetado" (soma de `queryCriterios` no cliente), donuts com total no centro
e legenda com valor + %, barras horizontais para critérios/parceiros, barras empilhadas de critérios
por parceiro, tabela de produtos com busca client-side e linha de totais, escape de HTML nos valores
interpolados, meta no topo (período + hora de atualização). Contrato Mitra preservado
(`componentData.*`, `setVariableMitra`, evento `mitra-update-dashboard-armazenagem-v3`).

**Pendência de configuração:** recolar as queries atualizadas no componente do dashboard na plataforma Mitra.

## Pontos de atenção conhecidos (não corrigidos / a observar)

- As queries SQL de exportação de notas estão hardcoded no JS (handler de `#export-notas-btn`), com nomes de
  tabelas do ERP (`GESTAO_NOTAS_APROVACAO`, `IMP_ITENS_NOTAS_APRV`, `IMP_NOTAS_ARMAZ`, `IMP_PACEIRO` — sic,
  "PACEIRO" é o nome real da tabela).
- Os índices de colunas dos resultados de `queryCriteriosProjetados`, `queryManuais` e
  `queryItensConsolidados` são posicionais (hardcoded) — se a query na plataforma mudar a ordem das
  colunas, a tela quebra silenciosamente.
- Valores vindos do banco são interpolados direto no HTML (sem escape). Risco baixo (dados internos),
  mas nomes com `<`/`"` podem quebrar o layout.

## Possíveis próximos passos

- Filtro/busca nos cards de critérios quando houver muitos.
- Ordenação da tabela de itens por coluna.
- Escapar HTML dos valores interpolados.
