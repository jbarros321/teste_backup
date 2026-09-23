# `:VAR_ANO_FILTRO` existe mas está vazia

## Por que isso zera a tela sem dar erro

Em SQL, qualquer comparação com `NULL` resulta em `NULL` — nunca em verdadeiro. Com o
ano vazio, a query do `queri1.sql` morre em três pontos ao mesmo tempo:

```sql
WHERE ... AND DA.ANO = :VAR_ANO_FILTRO              -- DA.ANO = NULL  -> nunca casa
CONCAT('31/12/', :VAR_ANO_FILTRO)                   -- '31/12/' + NULL -> NULL
MAKEDATE(:VAR_ANO_FILTRO, 1)                        -- MAKEDATE(NULL,1) -> NULL
YEAR(REFERENCIA) = (:VAR_ANO_FILTRO - 1)            -- NULL - 1 -> NULL
```

`regras_contas` volta vazia, e com ela o relatório inteiro. **Zero linhas, zero erro.**

## A correção

Dar valor à variável: ligar `:VAR_ANO_FILTRO` ao filtro de ano do painel, ou definir um
valor padrão nela. Ela precisa devolver **só o ano, como número**: `2026`.

Não aponte para o filtro de mês — `07/2026` quebra os quatro usos acima.

## Rede de proteção na query (recomendado)

Enquanto o filtro pode vir vazio, vale a query não depender dele. Trocar todo uso de
`:VAR_ANO_FILTRO` por um ano resolvido uma única vez, no topo:

```sql
WITH PARAMS AS (
    SELECT CAST(
             COALESCE(NULLIF(TRIM(:VAR_ANO_FILTRO), ''), YEAR(CURDATE()))
           AS UNSIGNED) AS ANO
),
DATA_FILTRO_CTE AS (
    SELECT
        CASE WHEN :VAR_MES_FILTRO IS NULL OR :VAR_MES_FILTRO = 'Todos'
             THEN LAST_DAY(STR_TO_DATE(CONCAT('31/12/', (SELECT ANO FROM PARAMS)), '%d/%m/%Y'))
             ELSE LAST_DAY(STR_TO_DATE(CONCAT('01/', :VAR_MES_FILTRO), '%d/%m/%Y'))
        END AS DATA_FILTRO,
        CASE WHEN :VAR_MES_FILTRO IS NULL OR :VAR_MES_FILTRO = 'Todos'
             THEN LAST_DAY(STR_TO_DATE(CONCAT('31/12/', (SELECT ANO FROM PARAMS) - 1), '%d/%m/%Y'))
             ELSE LAST_DAY(DATE_SUB(STR_TO_DATE(CONCAT('01/', :VAR_MES_FILTRO), '%d/%m/%Y'), INTERVAL 1 YEAR))
        END AS DATA_FILTRO_ANT
)
-- ... e no resto da query, usar (SELECT ANO FROM PARAMS) em vez de :VAR_ANO_FILTRO
```

Com isso, filtro vazio cai no ano corrente em vez de devolver tela branca.

## Não confundir as duas camadas

Dois lugares diferentes, com nomes parecidos, e cada um quebra de um jeito:

| Camada | O que é | Se estiver errado |
|---|---|---|
| `varAno` (parâmetro do componente) | guarda o **texto** `:VAR_ANO_FILTRO` | a tela nem abre: `Erro Crítico: As configurações...` |
| `:VAR_ANO_FILTRO` (variável da plataforma) | guarda o **valor** `2026` | a tela abre e fica **vazia**, sem erro |

O parâmetro já está criado. Agora falta o valor da variável.
