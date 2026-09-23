# BP Interno — o que muda e o que ficou aberto

Query pronta: **`12_queryDados_BP_INTERNO_FINAL.sql`**. Devolve as mesmas colunas de
antes (`ORDEM`, `NOME_GRUPO`, `ANO_ATUAL`, `ANO_ANTERIOR`) mais `ANO_USADO`,
`MES_USADO` e `DATA_CORTE` para a tela poder mostrar o que foi usado.

## Sete defeitos na query antiga

| # | Na query antiga | Efeito |
|---|---|---|
| 1 | `DET.ID = DETREF.ID_DET_DEMONSTRATIVO` | a coluna não existe mais → `Unknown column` |
| 2 | `:VAR_DATA_REF_DRE` | os filtros agora são ano + mês |
| 3 | `DETREF.REFERENCIA = DATE(:VAR_DATA_REF_DRE)` | igualdade exata de data. As referências são gravadas sempre no dia 01, então `2026-09-21` nunca casava → tela zerada |
| 4 | `B.CTACTB = DETCTACTB.PADRAO_CTACTB` | igualdade em vez de `LIKE`: qualquer padrão com `%` é ignorado em silêncio |
| 5 | `IFNULL(DETCTACTB.SINAL, 0)` | multiplicar por zero apaga o vínculo; a convenção das outras telas é `IFNULL(SINAL, 1)` |
| 6 | não usava `DET_DEMONSTRATIVO_CTACTB_EXC` | exclusões ignoradas |
| 7 | — | ano escolhido podia não ter vínculo nenhum (caso do 2026 do BP Externo) |

O comportamento de a coluna anterior usar o cadastro **do ano anterior** foi mantido —
era o que o `CASE WHEN DETREF.REFERENCIA = DATE_SUB(...)` fazia.

## Conferido contra os dados (CODEMP 999, ano 2026, mês 09/2026)

As 38 linhas devolvem valor nas duas colunas. Fornecedores:

| | cadastro | valor |
|---|---|---:|
| ANO_ATUAL (30/09/2026) | 2026 | **1.707.138,17** |
| ANO_ANTERIOR (30/09/2025) | 2025 | 1.573.760,87 |

`1.707.138,17` é o número do Sankhya que abriu a investigação.

## O que NÃO fecha — e não é a query

Ativo × Passivo+PL em 30/09/2026:

```
ativo         109.090.546,01
passivo + PL  103.790.208,07
diferença       5.300.337,94
```

A causa são **18 contas patrimoniais com saldo que não estão vinculadas a nenhuma linha
do BP Interno**. A soma delas é **-5.300.337,94** — bate ao centavo com o desequilíbrio.

As maiores:

| Conta | Saldo em 30/09/2026 |
|---|---:|
| `2.1.01.06.000034` | -5.550.000,00 |
| `2.1.02.05.000001` | -618.865,74 |
| `2.1.01.06.000035` | 550.000,00 |
| `2.1.02.01.000007` | -302.504,61 |
| `2.1.02.05.000002` | 275.556,45 |
| `2.1.02.05.000007` | -146.065,04 |
| `1.1.03.01.000014` | 140.788,66 |

Lista completa em `evidencias_contas_sem_vinculo.csv`, com uma coluna por tela.

O BP Externo tem o mesmo problema, um pouco pior: **35 contas**, somando -5.963.886,32.

É a mesma classe de problema da investigação anterior (`../Correcao Fornecedores 2026/`):
conta criada no Sankhya sem ninguém vincular no cadastro do demonstrativo. A query nova
não inventa esse vínculo — ela só deixa de escondê-lo, porque agora as linhas aparecem.

## Consulta para reencontrar essas contas quando quiser

```sql
SELECT B.CTACTB, ROUND(SUM(B.VLRLANC), 2) AS SALDO
  FROM IMP_BASE_BALANCETE B
 WHERE B.CODEMP = :VAR_EMPRESA_DRE
   AND (B.CTACTB LIKE '1.%' OR B.CTACTB LIKE '2.%')
   AND B.REFERENCIA <= :DATA_CORTE
   AND NOT EXISTS (
         SELECT 1
           FROM DET_DEMONSTRATIVO            D
           JOIN DET_DEMONSTRATIVO_ANO        A ON A.ID_DET_DEMONSTRATIVO = D.ID
           JOIN DET_DEMONSTRATIVO_REFERENCIA R ON R.ID_DET_DEMONSTRATIVO_ANO = A.ID
           JOIN DET_DEMONSTRATIVO_CTACTB     C ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = R.ID
          WHERE D.ID_ESTR_DEMONSTRATIVO = 2          -- 1 para o BP Externo
            AND B.CTACTB LIKE C.PADRAO_CTACTB
       )
 GROUP BY B.CTACTB
HAVING ROUND(SUM(B.VLRLANC), 2) <> 0
 ORDER BY ABS(SUM(B.VLRLANC)) DESC;
```
