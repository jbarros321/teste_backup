/* =====================================================================
   04 - QUERY DO BP INTERNO CORRIGIDA  (ESTR_DEMONSTRATIVOS.ID = 2)
   tenant_47255

   O que muda em relacao ao que esta publicado:

   1) DEVOLVE A ULTIMA COMPETENCIA REALMENTE CARREGADA, nas colunas
      REF_MAX_DISPONIVEL e MESES_DEFASAGEM. Sem isso a tela mostra o
      saldo de marco com rotulo de setembro e ninguem percebe - foi
      exatamente o que aconteceu (00_DIAGNOSTICO.md).
   2) Substitui `BP Interno/queryDados.sql`, que estava com dois erros:
      pegava so o MOVIMENTO do mes em vez do SALDO acumulado, e fazia
      INNER JOIN em DET_DEMONSTRATIVO_REFERENCIA sem escolher a
      referencia vigente, contando todo valor em dobro.
   3) Escolhe a referencia vigente com
      MAX(REFERENCIA) <= :VAR_DATA_REF_DRE, igual ao BP Externo.

   Parametros: :VAR_DATA_REF_DRE (data de corte), :VAR_EMPRESA_DRE (CODEMP)
   Colunas devolvidas: ORDEM, NOME_GRUPO, ANO_ATUAL, ANO_ANTERIOR,
                       REF_MAX_DISPONIVEL, MESES_DEFASAGEM
   ===================================================================== */

WITH ref_disponivel AS (
    SELECT MAX(REFERENCIA) AS REF_MAX
      FROM IMP_BASE_BALANCETE
     WHERE CODEMP = :VAR_EMPRESA_DRE
),
base_bal AS (
    SELECT
        CTACTB,
        /* contas patrimoniais: saldo acumulado desde o inicio ate o corte */
        SUM(CASE WHEN REFERENCIA <= LAST_DAY(DATE(:VAR_DATA_REF_DRE))
                 THEN VLRLANC ELSE 0 END) AS soma_ate_atu,
        SUM(CASE WHEN REFERENCIA <= LAST_DAY(DATE_SUB(DATE(:VAR_DATA_REF_DRE), INTERVAL 1 YEAR))
                 THEN VLRLANC ELSE 0 END) AS soma_ate_ant
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
      AND (CTACTB LIKE '1%' OR CTACTB LIKE '2%')
    GROUP BY CTACTB

    UNION ALL

    /* contas de resultado: acumulam apenas o ano corrente */
    SELECT
        CTACTB,
        SUM(CASE WHEN REFERENCIA >= MAKEDATE(YEAR(DATE(:VAR_DATA_REF_DRE)), 1)
                  AND REFERENCIA <= LAST_DAY(DATE(:VAR_DATA_REF_DRE))
                 THEN VLRLANC ELSE 0 END) AS soma_ate_atu,
        SUM(CASE WHEN REFERENCIA >= MAKEDATE(YEAR(DATE_SUB(DATE(:VAR_DATA_REF_DRE), INTERVAL 1 YEAR)), 1)
                  AND REFERENCIA <= LAST_DAY(DATE_SUB(DATE(:VAR_DATA_REF_DRE), INTERVAL 1 YEAR))
                 THEN VLRLANC ELSE 0 END) AS soma_ate_ant
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
      AND (CTACTB LIKE '3%' OR CTACTB LIKE '4%' OR CTACTB LIKE '5%')
    GROUP BY CTACTB
),
regras_contas AS (
    SELECT DISTINCT
           DET.ORDEM, DET.NOME_GRUPO,
           DETCTACTB.PADRAO_CTACTB, DETCTACTB.SINAL
      FROM ESTR_DEMONSTRATIVOS            EST
      JOIN DET_DEMONSTRATIVO              DET       ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
      JOIN DET_DEMONSTRATIVO_REFERENCIA   DETREF    ON DET.ID = DETREF.ID_DET_DEMONSTRATIVO
      JOIN DET_DEMONSTRATIVO_CTACTB       DETCTACTB ON DETREF.ID = DETCTACTB.ID_DET_DEMONSTRATIVO_REFERENCIA
     WHERE EST.ID = 2
       /* referencia vigente: a mais recente que nao passa da data de corte.
          Sem isto cada conta entra uma vez por referencia cadastrada. */
       AND DETREF.REFERENCIA = (
              SELECT MAX(R.REFERENCIA)
                FROM DET_DEMONSTRATIVO_REFERENCIA R
               WHERE R.ID_DET_DEMONSTRATIVO = DET.ID
                 AND R.REFERENCIA <= DATE(:VAR_DATA_REF_DRE)
           )
),
regras_excluidas AS (
    SELECT DISTINCT
           DET.ORDEM, DET.NOME_GRUPO, EXC.PADRAO_CTACTB
      FROM ESTR_DEMONSTRATIVOS            EST
      JOIN DET_DEMONSTRATIVO              DET    ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
      JOIN DET_DEMONSTRATIVO_REFERENCIA   DETREF ON DET.ID = DETREF.ID_DET_DEMONSTRATIVO
      JOIN DET_DEMONSTRATIVO_CTACTB_EXC   EXC    ON DETREF.ID = EXC.ID_DET_DEMONSTRATIVO_REFERENCIA
     WHERE EST.ID = 2
       AND DETREF.REFERENCIA = (
              SELECT MAX(R.REFERENCIA)
                FROM DET_DEMONSTRATIVO_REFERENCIA R
               WHERE R.ID_DET_DEMONSTRATIVO = DET.ID
                 AND R.REFERENCIA <= DATE(:VAR_DATA_REF_DRE)
           )
),
contas_mapeadas AS (
    SELECT B.soma_ate_atu, B.soma_ate_ant,
           R.ORDEM, R.NOME_GRUPO, R.SINAL,
           /* se a conta casar com mais de um padrao da mesma linha,
              vale o mais especifico (o padrao mais longo) */
           ROW_NUMBER() OVER (PARTITION BY B.CTACTB, R.ORDEM, R.NOME_GRUPO
                              ORDER BY LENGTH(R.PADRAO_CTACTB) DESC, R.PADRAO_CTACTB DESC) AS rn
      FROM base_bal B
      JOIN regras_contas R
        ON B.CTACTB LIKE R.PADRAO_CTACTB
      LEFT JOIN regras_excluidas E
        ON B.CTACTB LIKE E.PADRAO_CTACTB
       AND E.ORDEM = R.ORDEM
       AND E.NOME_GRUPO = R.NOME_GRUPO
     WHERE E.PADRAO_CTACTB IS NULL
)
SELECT
    R.ORDEM,
    R.NOME_GRUPO,
    IFNULL(Agg.ANO_ATUAL, 0)    AS ANO_ATUAL,
    IFNULL(Agg.ANO_ANTERIOR, 0) AS ANO_ANTERIOR,
    DATE_FORMAT(D.REF_MAX, '%m/%Y')                              AS REF_MAX_DISPONIVEL,
    GREATEST(TIMESTAMPDIFF(MONTH, D.REF_MAX, DATE(:VAR_DATA_REF_DRE)), 0) AS MESES_DEFASAGEM
FROM (SELECT DISTINCT ORDEM, NOME_GRUPO FROM regras_contas) R
CROSS JOIN ref_disponivel D
LEFT JOIN (
    SELECT ORDEM, NOME_GRUPO,
           SUM(IFNULL(soma_ate_atu, 0) * IFNULL(SINAL, 1)) AS ANO_ATUAL,
           SUM(IFNULL(soma_ate_ant, 0) * IFNULL(SINAL, 1)) AS ANO_ANTERIOR
      FROM contas_mapeadas
     WHERE rn = 1
     GROUP BY ORDEM, NOME_GRUPO
) Agg ON R.ORDEM = Agg.ORDEM AND R.NOME_GRUPO = Agg.NOME_GRUPO
ORDER BY R.ORDEM;
