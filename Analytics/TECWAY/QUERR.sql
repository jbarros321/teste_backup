SELECT
  COUNT(*) 
  
  FROM (
  
    SELECT
        CTACTB,
        SUM(
            CASE
                WHEN REFERENCIA <= TIMESTAMP(LAST_DAY(DATE(:VAR_DATA_REF_DRE)))
                    THEN VLRLANC
                ELSE 0
            END
        ) AS soma_ate_atu,
        SUM(
            CASE
                WHEN REFERENCIA <= TIMESTAMP(
                        LAST_DAY(
                            DATE_SUB(DATE(:VAR_DATA_REF_DRE), INTERVAL 1 YEAR)
                        )
                     )
                    THEN VLRLANC
                ELSE 0
            END
        ) AS soma_ate_ant
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
      AND REFERENCIA <= TIMESTAMP(LAST_DAY(DATE(:VAR_DATA_REF_DRE)))


  AND CTACTB LIKE '1%'
    GROUP BY
        CTACTBe
ORDER BY CTACTB ASC
)