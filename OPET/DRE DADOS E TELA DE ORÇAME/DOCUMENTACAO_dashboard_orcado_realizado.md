# Dashboard Orçado × Realizado — OPET

Tela (JSP para o Sankhya) de acompanhamento do **orçamento (meta)** contra o
**realizado**, com análise de **forecast** de fechamento, filtros e exportação
para Excel.

> **Versão de referência (correta / homologada):** `dashboard_orcado_realizado_FINAL.jsp`
> Toda esta documentação descreve essa versão.

---

## 1. Arquivos da pasta

| Arquivo | Papel |
|---|---|
| `dashboard_orcado_realizado_FINAL.jsp` | **Versão final e correta.** É a que traz os valores e deve ser publicada no Sankhya. |
| `dashboard_orcado_realizado.jsp` | Versão de trabalho/rascunho (mesma base). |
| `dashboard_orcado_realizado - Copia.jsp` | Cópia de segurança de uma variação anterior (não usar). |
| `dash_orcado_realizado.html` | Protótipo estático original (Tailwind, tema claro). |
| `dados.sql` | Consulta base do **realizado** (referência histórica). |
| `REALIZADOS.SQL` | Realizado standalone (18 colunas) — espelha os ramos de realizado da FINAL: hierarquias de natureza (`START WITH ... CONNECT BY`), Direitos Autorais e Comissões. |
| `Orcamentos.sql` | Orçamento + realizado agregado (16 colunas) — metas TGFMET 5/7, baixas, competência e ano anterior, com as mesmas hierarquias de natureza + Direitos Autorais e Comissões. |

---

## 2. Conceito: Orçado × Realizado

| Medida | Origem | Colunas |
|---|---|---|
| **Orçado** | `TGFMET` (metas) | `PREVREC` (receita) / `PREVDESP` (despesa) |
| **Realizado** | Financeiro / Portal / Contabilidade | `COMP_REC` / `COMP_DESP` |

- **Cenário de orçamento** (na tela): `CODMETA = 5` → **Oficial**; `CODMETA = 7` → **Forecast**.
  Os dois são trazidos como origens separadas para permitir escolher qual entra
  (e **evitar dupla contagem** do orçamento).
- Os valores de `PREVREC`/`PREVDESP` são trazidos com `ABS(...)`.

---

## 3. A consulta (`<snk:query>`)

A query monta uma subconsulta `Q` com **8 ramos** unidos por `UNION ALL` e
agrega no `SELECT` externo por mês/empresa/projeto/CR/natureza/origem.

**Ramos de REALIZADO** (preenchem `COMP_REC`/`COMP_DESP`):
1. `TGFFIN` — baixas financeiras (receita/despesa por `RECDESP`), naturezas financeiras.
2. `TGFCAB` — Portal, NF entrada/saída (`CODTIPOPER 1128`, empresas 1 e 2).
3. `TGFCAB` — Portal, naturezas de despesa/receita (hierarquia de `DESCRNAT`). **← é aqui que entra a maioria das despesas, ex.: natureza 4030508.**
4. `TCBLAN` — Contabilidade (`ANALITICA='S'`, hierarquia de naturezas).
5. `TGFCAB` — Portal, outras operações (`CODTIPOPER` 1132‑1140, 1714, 1717).
6. **Direitos autorais** — base de comissão (`TGFCAB`/`TGFITE`/`TGFFIN` via `CODTIPOPER 1131` e `1714/1718`) × percentual de `AD_DIRAUTPROD`. Natureza fixa **2010300**, origem **`DIREITOS AUTORAIS`**, valor (`COMPARC`) em **`COMP_DESP`** (despesa).
7. **Comissões** — `TGFCOM`/`TGFVEN`/`TGFCCM` (representantes, `VEN.TIPVEND = 'R'`). Natureza fixa **2010401**, origem **`COMISSAO`**, valor (`VLRCOM`) em **`COMP_DESP`** (despesa). `DTREF = TRUNC(CAB.DTFATUR,'MM')`.

**Ramos de ORÇADO** (preenchem `PREVREC`/`PREVDESP`):
6. `TGFMET` com `CODMETA = 5` → origem **ORCAMENTO** (oficial).
7. `TGFMET` com `CODMETA = 7` → origem **FORECAST**.

**Joins de apoio no SELECT externo:**
- `TGFNAT N` → `DESCRNAT` (nome da natureza) e `TIPNAT` (Receita/Despesa).
- `TCSPRJ P` → `IDENTIFICACAO` (nome do projeto).
- `TSIEMP E` → `NOMEFANTASIA` (nome da empresa).
- `TSICUS C` → `DESCRCENCUS` (nome do centro de resultado) e `CODUSURESP` (responsável).
- `TSIUSU U` → `NOMEUSU` (nome do responsável, via `U.CODUSU = C.CODUSURESP`).

**Período (filtro fixo na query):**
```sql
WHERE Q.DTREF >= DATE '2026-01-01'
  AND Q.DTREF <  DATE '2027-01-01'
```
Traz o ano de **2026** inteiro. O realizado naturalmente só existe até o mês
corrente; o orçado cobre o ano todo — necessário para o **forecast** projetar os
meses futuros.

---

## 4. Filtros da tela

| Filtro | Tipo | Observação |
|---|---|---|
| **Data inicial / Data final** | data | A tela **só carrega após preencher e clicar em "Carregar"**. |
| **Empresa** | múltipla seleção | Mostra o nome (`NOMEFANTASIA`). Ctrl+clique para várias. |
| **Centro de Resultado** | múltipla seleção | Mostra o nome (`DESCRCENCUS`). |
| **Projeto** | múltipla seleção | Ctrl+clique. |
| **Natureza** | múltipla seleção | Ctrl+clique. |
| **Responsável** | seleção única | Responsável do centro de resultado (`TSICUS.CODUSURESP` → `TSIUSU.NOMEUSU`). |
| **Origem (realizado)** | seleção única | Filtra só as linhas de realizado (Financeiro/Portal/Contabilidade/Direitos Autorais). |
| **Cenário orçado** | Oficial / Forecast / Ambos | Controla qual meta entra (evita dupla contagem). |
| **Visão** | Despesas / Receitas / Receitas+Despesas | Define o balde exibido. |
| **Limpar** / **Exportar Excel** | botões | |
| **Expandir tudo / Recolher tudo** | botões | Controlam a árvore do detalhamento hierárquico. |

---

## 5. Componentes visuais

- **Barras de consumo (hero):** dois cards lado a lado, sempre mostrando ambos (independente da Visão):
  - **Despesas** — consumo; passar de 100% é **ruim** (vermelho); saldo = orçado − realizado.
  - **Receitas** — realização; **quanto maior, melhor** (≥100% verde, <85% vermelho); superávit = realizado − orçado. Assim receita acima da meta aparece **positiva/verde** (antes ficava vermelha).
- **KPIs:** Orçado (Ano), Realizado/Comprometido (YTD), Saldo orçamentário, Naturezas, **Forecast fechamento**, **Projeção (run-rate)**, **Desvio vs Orçado**, Meses realizados.
- **Gráficos (Chart.js):**
  - `chartMensal` — Orçado × Realizado por mês (barras).
  - `chartPizza` — Distribuição do orçado por natureza (rosca).
  - `chartForecast` — Acumulado & Forecast (linhas: orçado acum., realizado acum., forecast tracejado).
  - `chartConsumo` — % de consumo por natureza, Top 12 (barras horizontais).
- **Tabelas:** Resumo Mensal (orçado/realizado/% consumo/saldo/acumulados) e **Detalhamento hierárquico**: a **Natureza** é o grupo (linha com subtotal e chevron, some a repetição); ao clicar, expande os **detalhes** (Empresa · Projeto · CR · Responsável). Botões Expandir/Recolher tudo e linha de TOTAL GERAL.
- **Exportar Excel (XLSX):** abas `Resumo Mensal`, `Por Natureza`, `Detalhado`.

---

## 6. Regras de cálculo (JavaScript)

### Classificação Receita × Despesa
Feita pelo **`TIPNAT` da natureza** (TGFNAT), **não** pela coluna que a origem
preencheu. Isso é essencial porque o realizado do **Portal cai sempre em
`COMP_REC`** (mesmo sendo despesa) e o da **Contabilidade em `COMP_DESP`**.
A função `valores()` soma as duas colunas e classifica pelo `TIPNAT`:

```
orcTot  = orcRec + orcDesp
realTot = realRec + realDesp
Visão 'T' (ambas) → mostra tudo
Visão 'R'         → só se TIPNAT = 'R'
Visão 'D'         → só se TIPNAT ≠ 'R'
```

### Forecast
- `mesCorte` = mês atual (para o ano corrente); anos passados = 12; futuros = 0.
- **Forecast de fechamento** = realizado acumulado até o mês corte **+** orçado dos meses restantes.
- **Projeção (run-rate)** = (realizado YTD ÷ meses com realizado) × 12.
- **Desvio vs Orçado** = Forecast − Orçado total.

### Parser numérico `num()`
Converte valores vindos do servidor aceitando **vírgula ou ponto** decimal
(`"833,15"`, `"1.234,56"`, `"833.15"`, `"R$ ..."`). Os campos numéricos são
emitidos entre aspas no JSON justamente para que o parser trate o locale.

### Escape do JSON
Os nomes (empresa/projeto/CR/natureza) são escapados com `fn:replace` para que
aspas/contrabarra em qualquer registro não quebrem o `window.DADOS_FORECAST`.

---

## 7. ⚠️ Cuidado crítico — Formatação automática quebra o SQL

**Não rode "Format Document" / não deixe o *format on save* atuar neste `.jsp`.**

O formatador (Prettier/HTML do VS Code) **quebra os literais de texto do SQL em
várias linhas**, por exemplo transformando:

```sql
'DESPESAS ADMINISTRATIVAS'
```
em
```sql
'DESPESAS
ADMINISTRATIVAS'
```

Isso muda o texto no Oracle, a natureza deixa de casar na hierarquia de
`DESCRNAT`, e o **realizado do Portal desaparece** (valores zerados).
Sintoma típico: **aparece só a meta, o realizado fica zerado.**

**Como evitar:**
- Desligar *Format On Save* para arquivos `.jsp`, ou
- Adicionar o arquivo ao `.prettierignore`, ou
- Manter os literais das listas `IN (...)` sempre em **uma única linha**.

Se acontecer de novo, o conserto é rejuntar cada literal `'...'` que ficou
partido entre linhas.

---

## 8. Publicação / testes

1. Publicar/republicar o `dashboard_orcado_realizado_FINAL.jsp` no Sankhya
   (o arquivo em disco local **não** é o que o servidor executa; se houver cache
   de JSP, forçar a recompilação).
2. Preencher **Data inicial** e **Data final** e clicar em **Carregar**.
3. Validar (ex.: natureza 4030508 — LICENÇAS DE SOFTWARE, jan/2026):
   - Orçado (Cenário Oficial) ≈ R$ 91.476,00
   - Realizado ≈ R$ 1.493,33 (833,15 + 660,18)

---

## 9. Observações de dados

- O **orçado** e o **realizado** de uma mesma natureza podem estar em
  **Projeto/CR diferentes** (ex.: orçado no CR TI, realizado em TI e Financeiro).
  Por isso ficam em **linhas separadas** no detalhamento; os KPIs, o Resumo
  Mensal e a linha **TOTAL** consolidam ambos.
- Origem e Cenário são dimensões distintas: **Origem** filtra o realizado;
  **Cenário** escolhe a meta (Oficial/Forecast) — não use "Origem" para separar
  meta de realizado.
