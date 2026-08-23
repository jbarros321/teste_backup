# Importação das telas DRE / BP / Indicadores / BaseIndicadores

Origem: projeto **tenant_28263** (pasta `telas adc`, acesso em `integra.md`)
Destino: projeto **tenant_47255** (pasta `Ajuste v2` — o que estamos ajustando)

Levantamento feito lendo os dois tenants pela API REST `/rest/v0` em 17/08/2026.

---

## 1. As 4 telas e o que cada uma consome

| Tela | HTML (origem) | Query | Estrutura (`ESTR_DRE_TW.ID`) | Base de valor |
|---|---|---|---|---|
| DRE | `dre.html` | `queryDRE.sql` | **1** – DRE Padrão Tecway | `DRE_TECWAY` |
| BP | `bp.html` | `bp_querydados.sql` | **5** – BP Padrão Tecway | `BP_TECWAY` |
| Indicadores (BP) | `indicadores.html` | `queryDadosBPindicadores.sql` | **7** – BP Padrão Tecway ‑ Interno | `BP_TECWAY` + `DET_NOTAS` |
| BaseIndicadores (DRE) | `baseindicadores.html` | `queryDadosDREindicadores.sql` | **9** – DRE Padrão Tecway ‑ Interno | `DRE_TECWAY` + `DET_NOTAS` |

`querybaseindicabp.sql` e `querybaseindicadoresdre.sql` são cópias byte a byte de
`queryDadosBPindicadores.sql` e `queryDadosDREindicadores.sql` — não são queries a mais.

Variáveis usadas pelas 4 queries: **`:VAR_MES`** (texto `'MM/AAAA'`) e **`:VAR_EMPRESA`**
(código ou lista separada por vírgula, lida com `FIND_IN_SET`).
O projeto de destino hoje usa `:VAR_DATA_REF_DRE` e `:VAR_EMPRESA_DRE` — são
outras variáveis, então as duas novas precisam ser criadas lá (item 5 do plano).

---

## 2. Tabelas necessárias

### Precisam ser criadas (nenhuma delas existe hoje no destino)

| Tabela | Papel | Linhas a carregar |
|---|---|---|
| `ESTR_DRE_TW` | cabeçalho da estrutura (qual demonstrativo) | 7 |
| `DET_DRE_TW` | linhas do demonstrativo (ordem, descrição, tipo, fórmula, hierarquia) | 307 |
| `CAD_CONTA_DRE_TW` | de/para linha × conta contábil | 16.842 |
| `DET_NOTAS` | número da nota explicativa por linha/mês/empresa | 45 |
| `CAD_CONTA_NOTAS_TW` | contas de cada nota (dependência do mesmo cadastro) | 22 |

**São só essas cinco.** `DRE_TECWAY` e `BP_TECWAY` **não** precisam ser criadas:
ficou definido que a base oficial destas telas é o `IMP_BASE_BALANCETE` deste
tenant, e as queries `06_query_tela_*_balancete.sql` já leem de lá. O DDL das
duas continua no `01_ddl_tabelas.sql` (bloco B, comentado como opcional) caso
um dia queiram replicar a base da origem.

### Já existem no destino e são reaproveitadas

`IMP_BASE_BALANCETE`, `DET_DEMONSTRATIVO`, `ESTR_DEMONSTRATIVOS`, `OUTROS_DFC`,
`NOTAS_EXPLICATIVAS_*` — usadas pelas telas de DFC/BP/DRE que já estão no ar.
Nenhuma delas é tocada por esta importação.

> Observação de método: o token do destino tem uma *whitelist* de tabelas, então
> pelo REST não dá para provar que uma tabela não existe — o erro é sempre
> "this token doesn't have access to table X". A conclusão de que as 7 acima
> não existem vem de não haver nenhuma referência a elas nas queries/telas já
> publicadas no destino. Vale confirmar no painel antes de rodar o DDL (o
> script usa `CREATE TABLE IF NOT EXISTS`, então é seguro rodar de novo).

---

## 3. De onde vêm os valores — decisão tomada: balancete do destino

Ficou definido usar o `IMP_BASE_BALANCETE` deste tenant como base oficial, em vez
de replicar `DRE_TECWAY`/`BP_TECWAY` da origem. As queries `06_*_balancete.sql`
fazem isso. O que a comparação entre as duas bases mostrou:

- O plano de contas é o mesmo (`CAD_CONTA_DRE_TW.ID_CONTA_CONTABIL` = `IMP_BASE_BALANCETE.CTACTB`)
  e os códigos de empresa também (`ID_EMPRESA` = `CODEMP`).
- Em **12/2024** as duas bases batem exatamente: `DRE_TECWAY × 1000` = balancete
  em **453 de 453** combinações conta × empresa.
- Mas em **06/2024** batem só 210 de 442. O motivo: no destino as contas de
  resultado (3/4/5/6) **só têm movimento em dezembro** — os meses de janeiro a
  novembro estão zerados (fechamento anual). Somando o valor absoluto de todas as
  contas de resultado da empresa 999 no destino: `2025-01` a `2025-11` = 0,00 e
  `2025-12` = 246.803.956,56.
- As contas patrimoniais (1/2), essas sim, têm movimento mês a mês no destino.

**Consequência prática desta escolha:** as telas de BP e de Indicadores ficam
completas (contas patrimoniais têm dado mensal aqui), mas nas telas de **DRE e
BaseIndicadores as colunas de janeiro a novembro sairão zeradas** — só dezembro
terá valor — até que o balancete mensal das contas de resultado seja importado
neste tenant. Isso está avisado no cabeçalho de cada query `06_*`.

As queries `06_*` substituem `DRE_TECWAY`/`BP_TECWAY` por duas CTEs sobre o
balancete, que devolvem exatamente o mesmo formato (`MES` `'MM/AAAA'`,
`ID_EMPRESA`, `ID_CONTA_CONTABIL`, valor em milhares) — o resto de cada query
ficou intacto:

- `BASE_DRE` = movimento do mês (`VLRLANC` do próprio mês) ÷ 1000.
- `BASE_BP` = saldo do mês: contas 1/2 acumulam todo o histórico até o mês;
  contas 3/4/5/6 acumulam só o ano corrente e entram com sinal invertido.

Semântica das tabelas da origem, para referência (levantada comparando com o balancete):

- `DRE_TECWAY` = **movimento do mês**, só contas de resultado (3/4/5). Em milhares.
- `BP_TECWAY` = **saldo do mês**, todas as contas (1 a 6), com as de resultado de
  sinal invertido em relação ao razão. Em milhares.
- Período disponível na origem: **01/2022 a 04/2026** (52 meses) nas duas.

---

## 4. Arquivos entregues nesta pasta

| Arquivo | O que faz |
|---|---|
| `01_ddl_tabelas.sql` | cria as tabelas. **Bloco A (5 tabelas de estrutura) = obrigatório**; bloco B (`DRE_TECWAY`/`BP_TECWAY`) = opcional |
| `02_dados_estrutura.sql` | carga completa das 5 tabelas de cadastro (17.223 linhas, dados reais da origem) — pronto para rodar |
| `06_query_tela_*_balancete.sql` | **as 4 queries a cadastrar**, lendo os valores do `IMP_BASE_BALANCETE` daqui |
| `05_query_tela_*.sql` | as 4 queries originais, sem alteração (só servem se um dia usarem a base da origem) |
| `03_extrair_valores.py` | baixa `DRE_TECWAY`/`BP_TECWAY` da origem, agrega por (MES, EMPRESA, CONTA) e gera o `04_...` |
| `04_dados_valores_*.sql` / `.csv` | 01/2024 a 12/2025 extraído da origem — guardado como comparativo/plano B, **não precisa rodar** |

---

## 5. Plano de execução

1. **Rodar o bloco A do `01_ddl_tabelas.sql`** no destino (5 tabelas de estrutura).
2. **Rodar `02_dados_estrutura.sql`** — já está pronto, com os dados reais.
3. **Criar as variáveis `VAR_MES` e `VAR_EMPRESA`** no projeto de destino
   (`VAR_MES` no formato `MM/AAAA`, ex.: `12/2025`; `VAR_EMPRESA` = `999` para o
   consolidado). Se preferir não criar variável nova, dá para derivar `VAR_MES`
   de `:VAR_DATA_REF_DRE` com `DATE_FORMAT(DATE(:VAR_DATA_REF_DRE), '%m/%Y')`
   dentro das queries — me avise e eu adapto os 4 arquivos `06_*`.
4. **Cadastrar as 4 queries `06_query_tela_*_balancete.sql`** e **colar os 4
   HTMLs** — esta parte é a sua.
5. **Conferência**: rodar a tela de DRE em `12/2024` e comparar com o
   demonstrativo já publicado no destino. É o mês em que as duas bases batem
   100% (453/453 contas), então serve de prova de que a estrutura e o de/para
   de contas ficaram corretos.

## 6. Pendência que sobra desta escolha

Usando o balancete daqui, as telas de **DRE e BaseIndicadores só terão valor em
dezembro** — janeiro a novembro saem zerados, porque neste tenant as contas de
resultado só têm movimento no fechamento anual. BP e Indicadores não sofrem com
isso (contas patrimoniais têm dado mensal).

Para destravar as duas telas mensais existem dois caminhos:

- **importar o balancete mensal das contas de resultado neste tenant** (caminho
  natural, mantém uma base só); ou
- **carregar `DRE_TECWAY` com os dados da origem** — o `04_dados_valores_DRE_TECWAY.sql`
  já está pronto para 01/2024 a 12/2025 — e apontar só a tela de DRE para ele
  (query `05_query_tela_DRE.sql`). Nesse caso essas duas telas mostram os números
  da origem, que divergem em alguns pontos dos das telas de DFC/BP já publicadas
  (ex.: empresa 999, conta `5.1.01.04.000004`, que está zerada aqui e tem valor lá).

Me diga qual dos dois e eu sigo.
