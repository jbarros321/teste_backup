-- MySQL 8.0+ (usa CTE). Para MySQL 5.7, ver variante com subquery no rodape.

WITH base_bal AS (
    SELECT
        CTACTB,
        SUM(
            CASE
                WHEN REFERENCIA < DATE_ADD(LAST_DAY(DATE(:VAR_DATA_REF_DRE)), INTERVAL 1 DAY)
                    THEN VLRLANC
                ELSE 0
            END
        ) AS soma_ate_atu,
        SUM(
            CASE
                WHEN REFERENCIA < DATE_ADD(
                        LAST_DAY(DATE_SUB(DATE(:VAR_DATA_REF_DRE), INTERVAL 1 YEAR)),
                        INTERVAL 1 DAY
                     )
                    THEN VLRLANC
                ELSE 0
            END
        ) AS soma_ate_ant
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
      AND REFERENCIA < DATE_ADD(LAST_DAY(DATE(:VAR_DATA_REF_DRE)), INTERVAL 1 DAY)
    GROUP BY
        CTACTB
)

SELECT
    DET.ORDEM,
    DET.NOME_GRUPO,
    DET.COD_NOTA_EXPLICATIVA,
    SUM(
        CASE
            WHEN DETREF.REFERENCIA = DATE(:VAR_DATA_REF_DRE)
                THEN IFNULL(B.soma_ate_atu, 0) * IFNULL(DETCTACTB.SINAL, 0)
            ELSE 0
        END
    ) AS VLRLANC_ATU,
    SUM(
        CASE
            WHEN DETREF.REFERENCIA = DATE_SUB(DATE(:VAR_DATA_REF_DRE), INTERVAL 1 YEAR)
                THEN IFNULL(B.soma_ate_ant, 0) * IFNULL(DETCTACTB.SINAL, 0)
            ELSE 0
        END
    ) AS VLRLANC_ANT
FROM ESTR_DEMONSTRATIVOS EST
    INNER JOIN DET_DEMONSTRATIVO DET
        ON DET.ID_ESTR_DEMONSTRATIVO = EST.ID
    INNER JOIN DET_DEMONSTRATIVO_REFERENCIA DETREF
        ON DETREF.ID_DET_DEMONSTRATIVO = DET.ID
    LEFT JOIN DET_DEMONSTRATIVO_CTACTB DETCTACTB
        ON DETCTACTB.ID_DET_DEMONSTRATIVO_REFERENCIA = DETREF.ID
    LEFT JOIN base_bal B
        ON B.CTACTB = DETCTACTB.PADRAO_CTACTB
WHERE EST.ID = 1
  AND DETREF.REFERENCIA IN (
        DATE(:VAR_DATA_REF_DRE),
        DATE_SUB(DATE(:VAR_DATA_REF_DRE), INTERVAL 1 YEAR)
      )
GROUP BY
    DET.ORDEM,
    DET.NOME_GRUPO,
    DET.COD_NOTA_EXPLICATIVA
ORDER BY
    DET.ORDEM;


/* ---------------------------------------------------------------------------
   Variante MySQL 5.7 (sem CTE) - troque o bloco "LEFT JOIN base_bal B" por:

    LEFT JOIN (
        SELECT
            CTACTB,
            SUM(CASE WHEN REFERENCIA < DATE_ADD(LAST_DAY(DATE(:VAR_DATA_REF_DRE)), INTERVAL 1 DAY)
                     THEN VLRLANC ELSE 0 END) AS soma_ate_atu,
            SUM(CASE WHEN REFERENCIA < DATE_ADD(
                                LAST_DAY(DATE_SUB(DATE(:VAR_DATA_REF_DRE), INTERVAL 1 YEAR)),
                                INTERVAL 1 DAY)
                     THEN VLRLANC ELSE 0 END) AS soma_ate_ant
        FROM IMP_BASE_BALANCETE
        WHERE CODEMP = :VAR_EMPRESA_DRE
          AND REFERENCIA < DATE_ADD(LAST_DAY(DATE(:VAR_DATA_REF_DRE)), INTERVAL 1 DAY)
        GROUP BY CTACTB
    ) B ON B.CTACTB = DETCTACTB.PADRAO_CTACTB

   (nesse caso o parametro :VAR_DATA_REF_DRE aparece mais vezes - se o driver
   usar "?" posicional, respeite a ordem dos placeholders.)
--------------------------------------------------------------------------- */
