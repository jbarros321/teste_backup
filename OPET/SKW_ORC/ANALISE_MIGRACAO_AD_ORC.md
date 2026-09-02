# Migração do dashboard para AD_AGREGORC / AD_ANALIORC

Análise das duas tabelas personalizadas e a migração das consultas do
`Final2 (6).jsp`. Schema e números lidos direto do Sankhya pelo API Gateway
(OAuth 2.0 `client_credentials`), não por suposição.

**Status: aplicado no JSP e validado contra o banco.**

---

## 1. Estrutura real das tabelas

Ambas carregadas em **02/09/2026**, cobrindo **2025 e 2026**.

### AD_AGREGORC — 32.699 linhas

Uma linha por **mês × empresa × centro de resultado × projeto × natureza**, com
as três medidas já pivotadas em colunas.

| Coluna | Tipo | Observação |
|---|---|---|
| `SEQ` | NUMBER(10) | PK |
| `ANO` / `MES` | NUMBER(10) | |
| `DTREF` | DATE | 1º dia do mês de competência |
| `CODEMP` / `CODCENCUS` / `CODPROJ` | NUMBER(10) | |
| `CODNAT` | **NUMBER(10)** | casa direto com `TGFNAT.CODNAT`, sem conversão |
| `NAT_N1` / `NAT_N2` / `NAT_N3` | VARCHAR2(100) | **códigos** da hierarquia DRE, não descrições |
| `VLR_ORCADO` / `VLR_REALIZADO` / `VLR_FORECAST` | FLOAT | |
| `DTCARGA` | DATE | |

> Não há colunas de descrição (empresa, projeto, CR, natureza) — elas continuam
> vindo dos joins com `TSIEMP`, `TCSPRJ`, `TSICUS`, `TGFNAT`.

### AD_ANALIORC — 610.833 linhas

Um lançamento por linha. É a base que, somada, gera o agregado.

| Coluna | Tipo | Observação |
|---|---|---|
| `SEQ` | NUMBER(10) | PK |
| `MEDIDA` | VARCHAR2(100) | `REALIZADO` \| `ORCADO` \| `FORECAST` |
| `ORIGEM` | VARCHAR2(100) | ver tabela abaixo |
| `ANO` / `MES` / `DTREF` | | competência |
| `ANO_ORIGEM` / `MES_ORIGEM` | NUMBER(10) | competência original do lançamento |
| `DTLANC` | DATE | data do lançamento (nula nas metas) |
| `CODEMP` / `CODCENCUS` / `CODPROJ` / `CODPARC` | NUMBER(10) | |
| `CODNAT` | NUMBER(10) | |
| `NAT_N1` / `NAT_N2` / `NAT_N3` | VARCHAR2(100) | |
| `VLR` | FLOAT | **com sinal**: receita +, despesa − |
| `DOCUMENTO` | VARCHAR2(100) | nulo nas metas |
| `HISTORICO` | **CLOB** | nulo nas metas — não pode entrar em `GROUP BY` |
| `TIPLANC` | VARCHAR2(10) | `R` \| `D` \| null |
| `AJUSTADO` | VARCHAR2(10) | |
| `CHAVE` | VARCHAR2(100) | rastreabilidade (`CTB:…`, `NF:…`, `DIR:…`, `FIN:…`) |
| `DTCARGA` | DATE | |

**Distribuição por MEDIDA / ORIGEM:**

| MEDIDA | ORIGEM | linhas |
|---|---|---:|
| REALIZADO | CONTABILIDADE | 489.808 |
| REALIZADO | CUSTOMIZACAO (dir. autorais, comissões) | 86.421 |
| REALIZADO | PORTAL | 17.601 |
| REALIZADO | FINANCEIRO | 4.461 |
| REALIZADO | MANUAL | 22 |
| REALIZADO | CMV | 21 |
| ORCADO | SANKHYA_META | 7.618 |
| FORECAST | SANKHYA_META | 4.881 |

---

## 2. Validações rodadas contra o banco

1. **Agregado = analítico, exato.** Comparação `FULL OUTER JOIN` de todas as
   chaves (ano, mês, empresa, CR, projeto, natureza) para 2025 e 2026, nas três
   medidas: **0 chaves divergentes**.

2. **Todas as naturezas existem na TGFNAT** (0 órfãs), mas **20 estão com
   `TIPNAT` nulo** — 17 delas em INVESTIMENTOS, 2 despesas e 1 receita
   (`1010406 RECEBIMENTOS NAO IDENTIFICADOS`). Daí o fallback
   `NVL(N.TIPNAT, CASE WHEN NAT_N1 = '1000000' THEN 'R' ELSE 'D' END)`,
   que acerta os 20 casos.

3. **Convenção de sinal.** Receita positiva, despesa negativa, nas três medidas.
   Consolidado de 2026-01:

   | NAT_N1 | Descrição | ORÇADO | REALIZADO | FORECAST |
   |---|---|---:|---:|---:|
   | 1000000 | RECEITA LIQUIDA | 2.225.552,00 | 13.128.743,86 | 2.153.949,30 |
   | 2000000 | CUSTO EDITORAS | −55.500,00 | −5.204.951,79 | 0,00 |
   | 3000000 | CUSTOS ENSINOS | −621.174,00 | −608.592,62 | −576.072,00 |
   | 4000000 | DESPESAS | −3.988.882,00 | −3.251.233,85 | −1.772.358,59 |
   | 5000000 | DEPRECIAÇÃO E RES. FINANCEIRO | −200.386,00 | −255.840,90 | −107.887,34 |
   | 6000000 | PROVISÃO TRIBUTOS S/ LUCRO | 0,00 | −34.250,55 | 0,00 |
   | 7000000 | INVESTIMENTOS | −1.038.203,00 | −335.976,21 | −340.076,50 |

4. **`ABS()` por lançamento estava errado.** A tela trabalha com magnitude
   positiva, mas aplicar `ABS()` linha a linha **descarta os estornos**:

   | 2026, realizado | valor |
   |---|---:|
   | `SUM(ABS(VLR))` | 224.587.173,24 |
   | soma com sinal normalizado | 219.629.358,40 |
   | **diferença** | **4.957.814,84** (2,3%) |

   São 2.215 lançamentos positivos em naturezas de despesa (estornos/créditos) e
   3 negativos em receita. A regra correta — a mesma que o `_sinalCase` do modelo
   antigo aplicava — é **normalizar o sinal pelo `TIPNAT` e somar algebricamente**:
   `receita = +VLR`, `despesa = −VLR`. É o que os três SQLs fazem agora.

5. **Fechamento ponta a ponta** (2026, após a migração):

   | Medida | Valor |
   |---|---:|
   | Orçamento | 114.134.130,00 |
   | Forecast | 68.235.585,47 |
   | Realizado (via `snk:query` / AD_AGREGORC) | 212.539.601,34 |
   | Realizado (via `_buildSqlRealizado` / AD_ANALIORC) | **212.539.601,34** |

   Os dois caminhos batem centavo a centavo — trocar o realizado ao clicar em
   *Carregar* não altera nenhum total, só o quebra por origem.

   Detalhamento do nó `1010200` (2026-01, empresa 1): 2.886 lançamentos somando
   **4.132.541,63**, idêntico ao valor da célula no agregado.

---

## 3. O que mudou no `Final2 (6).jsp`

Backup do arquivo original em `Final2 (6).jsp.bak`.

| # | Local | Antes | Agora |
|---|---|---|---|
| A | `<snk:query var="dados">` | UNION ALL de TGFFIN + TGFCAB×3 + TCBLAN + direitos autorais + TGFCOM + TGFMET×2 (~270 linhas) | `AD_AGREGORC` (48 linhas) |
| B | `_buildSqlRealizado()` | dezenas de blocos montados a partir de `DRE_REGRAS` | `AD_ANALIORC` agregada, com `ORIGEM` |
| C | `_buildSqlTitulos()` | os mesmos blocos, um lançamento por linha | `AD_ANALIORC` detalhada |
| D | `_carregarRealizado()` / `abrirModalTitulos()` | sondavam `ALL_TAB_COLUMNS` via `_carregarCapacidades()` | não precisam mais |
| E | `_avisosDoNo()` | avisava sobre naturezas fora da matriz DRE | só avisa se o nó não tem natureza |
| F | `popularFiltros()` | preenchia os filtros uma vez, na carga | extraído para `atualizarItensFiltros()` + `popularOrigens()`, refeitos após o *Carregar* |
| G | `window.DRE_REGRAS` / `DRE_CFG` e 18 funções | matriz origem×sinal por natureza e o sondador de dicionário | **removidos** (ver seção 4) |
| H | `min`/`max` dos inputs de data | fixos em `2026-01-01` / `2026-12-31` | derivados do período que a consulta trouxe |

Contrato preservado: `window.DADOS_FORECAST` continua com as mesmas 17 colunas
(`MES_ANO … REALIZADO_DESP`), `_carregarRealizado` continua lendo 14 colunas
posicionais e `_renderTitulosReais` as mesmas 15. Nenhuma outra parte do
JavaScript precisou mudar.

**Correção de brinde (F):** o filtro *Origem (realizado)* estava quebrado — as
opções vinham da consulta antiga (`FINANCEIRO`, `PORTAL`, `CONTABILIDADE`…) mas
os dados carregados usavam outros rótulos (`Movimentacao Financeira`,
`Portal Compras`…), então qualquer seleção zerava a tela. Agora as opções são
regeradas a partir do que foi realmente carregado.

### Detalhe de implementação

- `DTREF` guarda sempre o dia 1 do mês, então o período da tela é comparado por
  mês (`_periodoMesAD`), não por dia — senão um filtro de 01/01 a 31/01 não
  pegaria nada em 01/02.
- `HISTORICO` é CLOB: no detalhamento sai como
  `NVL(TO_CHAR(SUBSTR(L.HISTORICO,1,400)),'-')`.
- O detalhamento resolve as naturezas do nó clicado com `_natsDoNo(natId).ids`
  (a árvore da `TGFNAT`), que já existia.

---

## 4. Código morto removido

A origem e o sinal de cada lançamento agora vêm prontos da `AD_ANALIORC`, então
toda a máquina que os inferia deixou de ter chamadas. O que saiu foi levantado
por **grafo de alcançabilidade** (comentários desconsiderados, HTML incluído por
causa de `onclick=`), não por leitura:

- `window.DRE_REGRAS` e `window.DRE_CFG` — a matriz origem×sinal por natureza e
  seus parâmetros (92 linhas, o `<script>` inteiro)
- `_blocosFonte()` (233 linhas), `_regrasTodas()`, `_regrasDoNo()`,
  `_sinalCase()`, `_todosIds()`, `_cfg()`
- o sondador do dicionário do banco: `_carregarCapacidades()`,
  `_avisosCapacidades()`, `_conferirEsquema()`, `_ESQUEMA_USADO`, `_CAND`,
  `_cap`, `_faltantes`, `_tem()`, `_colDe()`, `_topOk()`, `_exprValorNota()`
- junto foram os blocos de tributos (`TGFDIN`), comissões (`TGFCOM`) e direitos
  autorais (`AD_DIRAUTPROD`), que hoje chegam como `ORIGEM = 'CUSTOMIZACAO'`

Depois da remoção o grafo acusa **0 declarações inalcançáveis** e `node --check`
passa. Conferi também, linha a linha contra o backup, que tudo que sumiu é
matriz DRE, SQL antigo ou uma dessas funções.

**3.459 → 2.775 linhas** (−684, −20%); o arquivo caiu de 326 KB para 253 KB.

---

## 5. Escopo de período

Mantido em **2026 em diante** (`WHERE A.ANO >= 2026`). As tabelas também têm
2025 carregado; liberar é só afrouxar esse filtro — mas com uma ressalva medida:

| | linhas no `snk:query` | JSON embutido na página |
|---|---:|---:|
| só 2026 | 17.823 | ~2,9 MB de metas + 6,2 MB de realizado |
| 2025 + 2026 | 33.481 | ~2,9 MB de metas + **9,9 MB** de realizado |

O `min`/`max` dos inputs de data não é mais fixo: sai do período que a consulta
trouxe, então acompanha sozinho a mudança do filtro de ano.

> Se um dia 2025 for liberado, vale tirar o `REALIZADO` do `<snk:query>`: ele é
> descartado no primeiro clique em *Carregar*
> (`RAW = metas.concat(novas)`), então são ~10 MB trafegados à toa. O
> `atualizarItensFiltros()` já está pronto para esse cenário — ele reabastece as
> listas de empresa/CR/projeto/natureza depois que o realizado chega.

---

## 6. Outras oportunidades

- `DTCARGA` permite mostrar na tela quando foi a última carga da Mitra.
- `NAT_N1/N2/N3` dão a hierarquia DRE pronta em 3 níveis; a `<snk:query
  var="arvoreNat">` sobre a `TGFNAT` (313 linhas) pode ser trocada por elas se
  quiser eliminar da árvore as naturezas sem movimento.

---

## 7. Riscos e cuidados

- **Não formatar o arquivo.** O *format on save* do VS Code quebra literais de
  texto dentro do `<snk:query>` em várias linhas e zera o realizado. Os SQLs
  novos praticamente não têm literais longos, o que reduz bastante o risco — mas
  a regra continua valendo.
- O `snk:query` devolve **17.823 linhas** embutidas como JSON na página (mesma
  ordem de grandeza da consulta antiga). Se virar problema de tamanho, ver a
  ressalva da seção 5.
- `EMPRESAS_EXCLUIDAS` no JS e o antigo `NOT IN (999,103,777,…)` ficaram
  inócuos: as tabelas só têm as empresas 1 a 5.

---

## 8. Como reproduzir as validações

O acesso ao banco foi feito pelo API Gateway
([doc](https://developer.sankhya.com.br/reference/post_authenticate)):

1. `POST https://api.sankhya.com.br/authenticate`
   com header `X-Token` e corpo form-urlencoded
   `client_id` + `client_secret` + `grant_type=client_credentials`
   → devolve `access_token` (validade de 300s).
2. `POST https://api.sankhya.com.br/gateway/v1/mge/service.sbr?serviceName=DbExplorerSP.executeQuery&outputType=json`
   com `Authorization: Bearer <token>` e corpo
   `{"serviceName":"DbExplorerSP.executeQuery","requestBody":{"sql":"…"}}`.

As credenciais estão em `api_dados.md`. O `DbExplorerSP` trunca o retorno em
**5.000 linhas** — por isso as conferências de totais são feitas com `SUM()`
dentro do próprio SQL, e não somando no cliente.
