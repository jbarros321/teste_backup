URL 1 https://analytics2.mitrasheet.com:4435/rest/v0

CHAVE PRIVADA 1 eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBUEkgMSIsIlgtVGVuYW50SUQiOiJ0ZW5hbnRfNDcyNTUifQ.F0JpE1JJ90RZuruOS-zaW8Ipdx1VbzXeMZIlIz9Q6h9VuI3HfMigMHgIe1hQtkXiNIYhRtoYkWQjqRKw_Y6nkA


EXEMPLO DE GET curl -X GET "https://analytics2.mitrasheet.com:4435/rest/v0/IMP_BASE_BALANCETE?AUTO_ID="123"&CODEMP="123"&CTACTB="someValue"&REFERENCIA="1970/02/28"&VLRLANC="123.123"&page=0&size=200"\
 -H "Content-Type: application/json"\
 -H "Authorization: Bearer [BEARER_TOKEN]"


URL 2 https://analytics2.mitrasheet.com:4435/rest/v0

CHAVE PRIVADA 2 eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBUEkyIiwiWC1UZW5hbnRJRCI6InRlbmFudF81MTgwOSJ9.otYgh2QhCf7SQy6xTKka2QZmRUhvA-yKtRRdEWifwxZH9s5PWErDfzuurG46UJirJTBxs2wJ6Yj6_zsaqI_7gQ

EXEMPLO GET curl -X GET "https://analytics2.mitrasheet.com:4435/rest/v0/IMP_BASE_BALANCETE?AUTO_ID="123"&CODEMP="123"&CTACTB="someValue"&REFERENCIA="1970/02/28"&VLRLANC="123.123"&page=0&size=200"\
 -H "Content-Type: application/json"\
 -H "Authorization: Bearer [BEARER_TOKEN]"


 CONSULTA PARA ANALISAR NAS SUAS APIS PARA VER QUAL CONTA CTACTB ESTA DIVERGENTE 


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
    GROUP BY
        CTACTB
