# Fornecedores do BP Interno não bate com o balancete do Sankhya (2026)

**Reclamação:** em 21/09/2026 o Sankhya mostra **1.707.138,17** em Fornecedores e o
BP Interno mostra **1.434.848,92**. Diferença de **272.289,25**.

**Causa:** a tabela `IMP_BASE_BALANCETE` do tenant_47255 **está com a carga parada em
março/2026**. A tela não percebe isso — ela pede "saldo até 30/09/2026", o SQL faz
`REFERENCIA <= LAST_DAY(:VAR_DATA_REF_DRE)`, e como não existe nada depois de
2026-03-01 o resultado devolvido é o **saldo de 31/03/2026**, rotulado como setembro.

Investigação feita em 22/09/2026 lendo o tenant pela API REST `/rest/v0`.

---

## 1. A conta fecha exatamente

Somando no balancete todas as contas de fornecedor (`2.1.01.01.*` + `2.1.01.02.*`),
CODEMP 999, acumulado desde o início:

| Referência | Movimento do mês | Saldo acumulado |
|---|---:|---:|
| 2025-09 | -452.951,53 | 1.573.760,87 |
| 2025-10 | 52.721,60 | 1.626.482,47 |
| 2025-11 | 100.749,50 | 1.727.231,97 |
| 2025-12 | -239.237,38 | 1.487.994,59 |
| 2026-01 | 9.336,99 | 1.497.331,58 |
| 2026-02 | 41.845,01 | 1.539.176,59 |
| **2026-03** | -104.327,67 | **1.434.848,92** |
| 2026-04 … 2026-09 | — | *não importado* |

**1.434.848,92 é exatamente o número da tela.** Não é erro de cadastro, de sinal, de
vínculo de conta nem da fórmula: é o saldo de 31/03/2026 sendo exibido como se fosse
21/09/2026. Os 272.289,25 de diferença são o movimento de abril a 21/09/2026 que nunca
entrou na base.

Evidência: `evidencias/saldo_fornecedores_por_mes.csv`.

## 2. A carga parou em março — e não é só Fornecedores

Linhas de `IMP_BASE_BALANCETE` por mês (todas as empresas):

```
2025-10  1.055     2026-01    962
2025-11  1.011     2026-02    876
2025-12  1.353     2026-03  1.048
                   2026-04+     0   <-- nada
```

`MAX(REFERENCIA)` na tabela inteira = `2026-03-01`. Carga mensal regular até março e
nada depois: **faltam 6 competências (04/2026 a 09/2026)**. Isso afeta *todas* as telas
que leem o balancete — BP Interno, BP Externo, DFC, Indicadores —, não só a linha de
Fornecedores. Fornecedores foi só onde alguém reparou.

Evidência: `evidencias/carga_balancete_por_mes.csv`.

## 3. Achado secundário: contas novas de fornecedor sem vínculo no cadastro

Independente da carga, há contas de fornecedor com movimento no balancete que **não
estão vinculadas a nenhuma linha** do BP Interno nos cadastros que temos:

| Estrutura | Linha | Contas mapeadas | Contas faltando | Impacto hoje (CODEMP 999) |
|---|---|---:|---:|---:|
| `ESTR_DRE_TW` 7 (tela migrada) | `DET_DRE_TW` 201 – Fornecedores | 2.058 | **32** | 17.089,00 |
| `ESTR_DEMONSTRATIVOS` 2 (BP Interno) | `DET_DEMONSTRATIVO` 42 – Fornecedores (ref. 2025 e 2024) | 1.226 | **22** | 16.685,00 |

São contas criadas depois da última atualização do cadastro:
`2.1.01.02.002085/002086/002105/002106–002119/002137–002148/002158–002163`.

Hoje isso vale ~17 mil, mas cresce todo mês, porque o cadastro lista as 2.058 contas
uma a uma em vez de usar o prefixo. **Não é a causa da diferença de 272 mil** (o número
da tela de produção já inclui essas contas), mas vai virar uma segunda divergência se
não for tratado. Correção em `03_vinculos_fornecedores_faltantes.sql`.

Evidência: `evidencias/contas_fornecedor_sem_vinculo.csv`.

## 4. Achado secundário: `BP Interno/queryDados.sql` está quebrado

O arquivo `Analytics/TECWAY/BP Interno/queryDados.sql` tem dois defeitos graves:

1. `DATE_FORMAT(REFERENCIA,'%Y-%m') = DATE_FORMAT(:VAR_DATA_REF_DRE,'%Y-%m')` — pega
   só o **movimento do mês**, não o saldo acumulado, apesar de o alias se chamar
   `soma_ate_atu`. Para conta patrimonial isso está errado por definição.
2. Faz `INNER JOIN DET_DEMONSTRATIVO_REFERENCIA` **sem escolher a referência vigente**.
   Como cada linha tem 2 referências (2025-12 e 2024-12) com o mesmo conjunto de
   contas, todo valor é **contado em dobro**.

O número de produção não bate com nenhum dos dois efeitos, então essa query não é a
que está publicada — é um rascunho antigo na pasta. Mesmo assim não deve ser usada.
A versão correta está em `04_query_bp_interno_corrigida.sql`.

---

## O que fazer, em ordem

1. **Religar a carga do balancete** (04/2026 a 09/2026). É isto que resolve os
   272.289,25. Rodar `01_verifica_defasagem_balancete.sql` antes e depois.
2. **Conferir contra o Sankhya** com `02_conferencia_fornecedores_sankhya.sql`, que
   devolve conta a conta — se depois da carga ainda sobrar diferença, ela aponta onde.
3. **Aplicar `03_vinculos_fornecedores_faltantes.sql`** para o cadastro parar de perder
   conta nova.
4. **Publicar o aviso de defasagem** (`04_query_bp_interno_corrigida.sql` +
   `05_patch_tela_aviso_defasagem.js`) para a tela nunca mais mostrar saldo de março
   com cara de setembro.

## Como reproduzir

```bash
python3 reproduz_diagnostico.py
```

Lê o tenant pela API REST e reimprime a tabela do item 1 e a contagem do item 2.
