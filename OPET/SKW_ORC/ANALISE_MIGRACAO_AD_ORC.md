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

4. **As 4 regras do consolidado.** São elas que fazem o número bater com o DRE
   gerencial, e a tela agora segue todas:

   **1 — Tudo é SOMA, nunca subtração.** O sinal já vem aplicado na carga:
   receita positiva, custo e despesa negativos. Lucro bruto é `receita + custo`.
   Não usar `ABS()` nem inverter sinal — quem inverte dobra o erro.

   **2 — Período é `ANO`/`MES`, nunca `DTLANC`.** `ANO`/`MES` são o período
   *gerencial*, já com as remessas de competência (`AJUSTADO='S'`,
   `ANO_ORIGEM`/`MES_ORIGEM`). `DTLANC` é a data real do documento e existe só
   para auditoria; agregar por ela reintroduz o que o ajuste resolve.

   **3 — Grupo do DRE é `NAT_N1`, não o `TIPNAT` da natureza.** Dentro de
   `NAT_N1 = '1000000'` (RECEITA LIQUIDA) as deduções — ISS, PIS, COFINS,
   descontos, cancelamentos — têm `TIPNAT='D'`, mas pertencem à receita e já
   entram somadas com sinal negativo. Classificar por `TIPNAT` tirava essas
   linhas da receita e jogava em despesa.

   | `NAT_N1` | Grupo |
   |---|---|
   | 1000000 | RECEITA LIQUIDA |
   | 2000000 | CUSTO EDITORAS |
   | 3000000 | CUSTOS ENSINOS |
   | 4000000 | DESPESAS |
   | 5000000 | DEPRECIAÇÃO E RESULTADO FINANCEIRO |
   | 6000000 | PROVISÃO DE TRIBUTOS S/ LUCRO |
   | 7000000 | INVESTIMENTOS (fora do resultado) |

   **4 — Orçado × realizado só se compara até empresa × natureza.** O orçado vem
   sempre com CR e projeto preenchidos (`R$ 0` sem projeto e `R$ 0` sem CR); o
   realizado tem **R$ 34,7 mi sem projeto** e **R$ 14,7 mi sem CR** (receita e
   CMV, que não têm essa dimensão). Em 2026, **72 das 130** células
   empresa × `NAT_N3` têm realizado sem nenhum orçado. A tela avisa quando os
   filtros de projeto ou CR estão ativos, mas não bloqueia: abrir só o realizado
   nesse nível é legítimo.

5. **Fechamento ponta a ponta.** O DRE montado a partir do que a tela recebe é
   idêntico ao que sai direto da `AD_AGREGORC` (2026):

   | Linha | Orçado | Realizado | Forecast |
   |---|---:|---:|---:|
   | RECEITA LIQUIDA | 34.303.349,00 | 76.505.346,81 | 30.695.793,30 |
   | CUSTO EDITORAS | −3.907.410,00 | −31.814.541,04 | 0,00 |
   | CUSTOS ENSINOS | −12.446.896,00 | −8.330.211,27 | −11.508.238,00 |
   | **= LUCRO BRUTO** | **17.949.043,00** | **36.360.594,50** | **19.187.555,30** |
   | DESPESAS | −52.025.069,00 | −30.890.603,26 | −22.049.087,94 |
   | **= EBITDA** | **−34.076.026,00** | **5.469.991,24** | **−2.861.532,64** |
   | DEPRECIAÇÃO E RESULT. FINANCEIRO | −2.471.025,00 | −1.934.020,64 | −1.146.581,03 |
   | **= LAIR** | **−36.547.051,00** | **3.535.970,60** | **−4.008.113,67** |
   | PROVISÃO DE TRIBUTOS S/ LUCRO | 0,00 | −961.577,99 | 0,00 |
   | **= RESULTADO LIQUIDO** | **−36.547.051,00** | **2.574.392,61** | **−4.008.113,67** |
   | INVESTIMENTOS (fora do resultado) | −9.056.429,00 | −3.319.042,45 | −1.092.499,50 |

   O realizado do `_buildSqlRealizado` (analítico, usado no *Carregar*) bate com
   o agregado nos **7 grupos, com diferença zero**. O detalhamento do nó 1010200
   (2026-01, empresa 1) soma 4.132.541,63 em 2.886 lançamentos — igual à célula.

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
| I | cabeçalho | não indicava a idade dos dados | badge `<snk:query var="carga">` com a data da carga da Mitra |

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

---

## 6. Conferência com a Mitra — a carga do Sankhya está atrasada

Comparando `AD_AGREGORC` (Sankhya) com `CONS_AGREGADO` (API Mitra) para 2026,
mês a mês:

| Mês | linhas API | linhas Sankhya | realizado API | realizado Sankhya | diferença |
|---|---:|---:|---:|---:|---:|
| jan–jul | 14.086 | 14.086 | — | — | **0,00** |
| **ago** | 1.477 | 1.288 | −4.024.467,81 | −1.339.382,86 | **2.685.084,95** |
| **set** | 538 | 498 | −295.012,16 | −281.339,59 | **13.672,57** |
| out–dez | 1.157 | 1.157 | — | — | 0,00 |
| **total** | **17.258** | **17.029** | **−744.649,84** | **1.954.107,68** | **2.698.757,52** |

O mesmo aparece no analítico: em agosto faltam **1.119 lançamentos** e em
setembro **7** — 1.126 no total, exatamente os R$ 2.698.757,52.

**Causa:** a carga é que está defasada, não a consulta.

| | timestamp |
|---|---|
| `AD_AGREGORC.DTCARGA` / `AD_ANALIORC.DTCARGA` (máx.) | 02/09/2026 **00:00** |
| `CARGA_EM` na API Mitra (máx.) | 02/09/2026 **15:33** |

De janeiro a julho os dois lados batem centavo a centavo, o que confirma que o
mapeamento das consultas está correto. A divergência está só nos meses ainda em
movimento (agosto/setembro), que a Mitra reprocessou depois que as tabelas do
Sankhya foram carregadas.

### O que exatamente falta em agosto

Comparando lançamento a lançamento pela coluna `CHAVE` (identificador de origem
da Mitra): **1.122 lançamentos na API que não existem no Sankhya**, somando
−2.683.121,96. Deles, **1.119 são de `ORIGEM = 'CONTABILIDADE'` com
`DATA_LANC = 01/08/2026`** — é a **contabilização da folha de agosto**, um lote
único que a Mitra processou depois da carga:

| CODNAT | Natureza | lanç. | valor |
|---|---|---:|---:|
| 3010101 | SALARIOS E ORDENADOS DOCENTE | 21 | −1.167.366,52 |
| 4010101 | SALARIOS E ORDENADOS ADMINISTRATIVO | 70 | −492.235,87 |
| 3010207 | INSS DOCENTE | 84 | −324.080,10 |
| 4010207 | INSS ADMINISTRATIVO | 291 | −136.888,19 |
| 3010206 | FGTS DOCENTE | 56 | −133.560,42 |
| 3010202 | 13º SALARIO DOCENTE | 18 | −116.753,18 |
| 4010206 | FGTS ADMINISTRATIVO | 196 | −80.019,90 |
| 4010201 | FERIAS ADMINISTRATIVO | 153 | −61.522,28 |
| … | (mais 19 naturezas de folha) | 233 | −170.695,50 |
| | **total (27 naturezas)** | **1.122** | **−2.683.121,96** |

Há também **3 lançamentos que existem no Sankhya e a Mitra já removeu** no
reprocessamento (`FIN:179381:0`, `NF:198628:1`, `NF:198628:2`) — por isso a
diferença do agregado (2.685.084,95) é ligeiramente maior que a soma acima.

**Ação:** reprocessar a carga Mitra → Sankhya das duas tabelas. Não há nada a
corrigir no JSP.

### Badge da data de carga (implementado)

Para que uma defasagem de carga não volte a passar por erro de cálculo, o
cabeçalho agora mostra quando os dados foram carregados. A consulta pega a carga
**mais antiga** das duas tabelas, que é a que limita o dado:

```sql
SELECT TO_CHAR(MIN(DT), 'DD/MM/YYYY HH24:MI') AS QUANDO,
       FLOOR((SYSDATE - MIN(DT)) * 24) AS HORAS
FROM (SELECT MAX(DTCARGA) AS DT FROM AD_AGREGORC
      UNION ALL SELECT MAX(DTCARGA) FROM AD_ANALIORC)
```

`marcarIdadeDaCarga()` colore o badge pela idade: **verde** abaixo de 12 h,
**âmbar** até 24 h, **vermelho** acima — e a partir de 12 h acrescenta um
`title` avisando que os meses ainda em movimento podem estar atrás da Mitra.
No estado atual a consulta devolve `02/09/2026 00:00` e `13` horas, ou seja, o
badge já sobe em âmbar.

## 7. Outras oportunidades

- `NAT_N1/N2/N3` dão a hierarquia DRE pronta em 3 níveis; a `<snk:query
  var="arvoreNat">` sobre a `TGFNAT` (313 linhas) pode ser trocada por elas se
  quiser eliminar da árvore as naturezas sem movimento.

---

## 8. Riscos e cuidados

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

## 9. Como reproduzir as validações

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
