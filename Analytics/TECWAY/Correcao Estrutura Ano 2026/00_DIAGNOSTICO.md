# Telas quebradas depois da estrutura por ano — tenant_60677

Dois erros independentes, mais dois problemas de dado que ninguém tinha visto.

| # | Erro / problema | Natureza |
|---|---|---|
| 1 | `Unknown column 'R.ID_DET_DEMONSTRATIVO' in 'where clause'` | SQL — 38 junções em 24 arquivos |
| 2 | `...(queryDados, varAno, varMes, varEmpresa) não foram carregadas da plataforma` | config — `varMes` vazio e `varAno` inexistente (ver `07_config_componente.md`) |
| 3 | **35 regras de exclusão não migradas** | dado — sumiriam em silêncio |
| 4 | **Vínculos duplicados 2,8× no caminho legado** | dado — Fornecedores multiplicaria por 12 |
| 5 | **2026 só cadastrado no BP Externo** | dado — as outras 5 telas abrem vazias (ver `08_cadastro_ano_incompleto.md`) |

Investigado em 23/09/2026 lendo o **tenant_60677** pela API (`API.MD`). Era por isso
que o tenant_47255 mostrava o schema antigo: a estrutura nova está em outro tenant.

---

## 1. Erro 1 — o encadeamento

### 1.1 O schema novo, confirmado pela API

```
ESTR_DEMONSTRATIVOS                                                    6 linhas
  └─ DET_DEMONSTRATIVO            (ID_ESTR_DEMONSTRATIVO)            154
       └─ DET_DEMONSTRATIVO_ANO   (ID, ID_DET_DEMONSTRATIVO, ANO)    357
            ├─ DET_DEMONSTRATIVO_ANO_CTACTB      (ID_DET_DEMONSTRATIVO_ANO)  20.444   << NOVO
            ├─ DET_DEMONSTRATIVO_ANO_CTACTB_EXC  (ID_DET_DEMONSTRATIVO_ANO)       0   << VAZIA
            └─ DET_DEMONSTRATIVO_REFERENCIA      (ID, ID_DET_DEMONSTRATIVO_ANO, REFERENCIA)  379
                 ├─ DET_DEMONSTRATIVO_CTACTB     (ID_DET_DEMONSTRATIVO_REFERENCIA)  57.723
                 └─ DET_DEMONSTRATIVO_CTACTB_EXC (ID_DET_DEMONSTRATIVO_REFERENCIA)      35
```

`DET_DEMONSTRATIVO_ANO.ANO` é `int` — não é FK para `CAD_ANO`.
`DET_DEMONSTRATIVO_REFERENCIA` tem 3 colunas e **perdeu `ID_DET_DEMONSTRATIVO`** — é
exatamente o que o erro dizia.

### 1.2 São 38 junções em 24 arquivos, em 4 formatos

"Os indicadores não carregam" é o primeiro sintoma; DRE, DFC, DMPL e os dois BP quebram
igual. Os formatos: join direto, join com `ON` invertido, subconsulta correlata, e join
no sentido inverso (da referência para a linha). O `03_patch_queries.py` cobre os
quatro e roda sem deixar aviso pendente.

### 1.3 Qual dos dois caminhos usar — resolvido

Comparei o conteúdo dos dois:

| | linhas na tabela | pares distintos (linha, ano, conta, sinal) |
|---|---:|---:|
| `_ANO_CTACTB` (novo) | 20.444 | 20.444 |
| `_REFERENCIA` → `_CTACTB` (legado) | 57.723 | 20.444 |

**Conteúdo idêntico** — zero diferença em 326 chaves (linha, ano). Mas o legado carrega
os mesmos 20.444 vínculos em 57.723 linhas.

**Decisão: ficar no LEGADO por enquanto.** É o que o `queri1.sql` (BP Externo, já em
produção) adotou, e ele mantém as 35 regras de exclusão funcionando — ele já colapsa as
12 referências com `MAX(REFERENCIA)`, então a duplicação não o afeta. O caminho NOVO é
mais limpo e vale como consolidação depois, mas exige rodar o `05_migra_exclusoes.sql`
antes. O `03_patch_queries.py` está com `CAMINHO = "LEGADO"`; trocar é uma linha.

### 1.4 Problema 4 — a duplicação, e por que ela é perigosa

De 357 pares (linha, ano), 355 têm 1 referência. Dois têm **12**:

```
EST2  2.1.1  Fornecedores  ano 2026  ->  refs 2026-01-01 .. 2026-12-01  (12)
EST2  2.1.1  Fornecedores  ano 2027  ->  refs 2027-01-01 .. 2027-12-01  (12)
```

Cada uma dessas 12 referências carrega uma cópia completa dos ~1.226 vínculos de
Fornecedores. Uma query que passe por `_REFERENCIA` sem escolher **uma** referência
multiplica Fornecedores por 12 — e não dá erro nenhum, só um número errado.

É a mesma armadilha do `BP Interno/queryDados.sql`
(`Correcao Fornecedores 2026/00_DIAGNOSTICO.md`, item 4), agora com uma dimensão a mais.
O caminho novo elimina isso de raiz: o vínculo é por ano, não por data.

### 1.5 Problema 3 — as 35 exclusões não migradas

`DET_DEMONSTRATIVO_ANO_CTACTB_EXC` está **vazia**; `DET_DEMONSTRATIVO_CTACTB_EXC` tem
**35** regras. Os vínculos foram migrados, as exclusões não.

Trocar as queries para o caminho novo **sem migrar isso** faz as 35 regras sumirem em
silêncio: as contas excluídas voltam a somar, sem erro na tela. Todas as 35 são da
estrutura 10 (DFC Externo) — o BP não é afetado, mas o DFC sim:

| Linha | Anos | Contas excluídas |
|---|---|---:|
| 2.1 Contas a receber | 2024, 2025 | 2 |
| 2.7 Obrigações tributárias | 2024 (4), 2025 (6) | 10 |
| 3.3 Aquisição de imobilizado | 2024, 2025 | 7 cada |
| 3.4 Aquisição de intangível | 2024, 2025 | 1 cada |
| 4.1 Empréstimos e Financiamentos | 2023, 2024, 2025 | 1 cada |
| 4.4 Lucros distribuídos | 2025 | 1 |

`05_migra_exclusoes.sql` insere as 34 linhas distintas e traz a consulta de conferência.

### 1.6 A regra do ano vigente

Use `MAX(ANO) <= YEAR(:VAR_DATA_REF_DRE)`, **nunca** `ANO = YEAR(...)`.

A estrutura 2 já tem **2027** cadastrado (anos: 2024, 2025, 2026, 2027). Com a
igualdade, virar o ano sem o cadastro novo zera a tela inteira; com o `MAX`, o cadastro
do ano anterior continua valendo. E sem filtro nenhum, cada conta entra uma vez por ano
— 4 anos na estrutura 2, ou seja Fornecedores × 4.

---

## 2. Erro 2 — `varAno` no BP Externo

Não é SQL. A trava de inicialização passou a exigir `window.componentData.varAno`:

```js
if (window.componentData && window.componentData.queryDados
    && window.componentData.varAno && window.componentData.varMes
    && window.componentData.varEmpresa) { ... }
else if (Date.now() - startTime > maxWaitTime) { /* 5s -> erro */ }
```

`varAno` **não existe na configuração do componente na plataforma**, então a condição
nunca fecha, vencem os 5 segundos e a tela morre antes de rodar qualquer query.

`grep -rn "varAno"` na pasta TECWAY não encontra nada — o HTML com `varAno` só existe
publicado na plataforma, não está versionado aqui. Vale baixar as versões publicadas
antes de continuar.

Correção em `04_patch_tela_varAno.js`. O passo obrigatório é criar o parâmetro `varAno`
na configuração do componente; o patch faz a tela dizer *qual* parâmetro faltou em vez
de morrer em branco, e resolve o ano com fallback para o ano da data de referência.

---

## Ordem de execução

1. `05_migra_exclusoes.sql` — **antes de qualquer patch de query.**
2. `python3 03_patch_queries.py` → revisar o diff.
3. `python3 03_patch_queries.py --aplicar` → grava, com `.bak`.
4. Acrescentar o filtro de ano vigente onde o script avisar (ele não insere sozinho) —
   modelo em `02_template_join_corrigido.sql`, pronto em `06_query_bp_interno_schema_novo.sql`.
5. Criar `varAno` no componente e aplicar `04_patch_tela_varAno.js`.
6. Conferir Fornecedores com `../Correcao Fornecedores 2026/02_conferencia_fornecedores_sankhya.sql`.
   Se multiplicar, faltou o filtro do passo 4.
