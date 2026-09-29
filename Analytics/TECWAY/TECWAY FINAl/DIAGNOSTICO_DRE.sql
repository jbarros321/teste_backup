/* =====================================================================
   DIAGNOSTICO - por que a DRE vem zerada
   Cole no lugar da query da tela (index3) e veja o resultado, OU rode
   direto num executor de SQL da plataforma.

   Troque EST_ID = 3 por 4 para testar o DRE Interno.

   Devolve UMA linha com contadores. Cada coluna responde uma pergunta,
   na ordem em que a query trabalha. A primeira que vier 0 e o ponto onde
   quebra.

   Comentario de bloco apenas - nada de -- aqui.
   ===================================================================== */

WITH PARAMS AS (
    SELECT
        CASE WHEN TRIM(COALESCE(:VAR_DATA_INICIO, '')) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
             THEN STR_TO_DATE(TRIM(:VAR_DATA_INICIO), '%d/%m/%Y')
             ELSE DATE(NULLIF(TRIM(COALESCE(:VAR_DATA_INICIO, '')), ''))
        END AS DT_INI_IN,
        CASE WHEN TRIM(COALESCE(:VAR_DATA_FIM, '')) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
             THEN STR_TO_DATE(TRIM(:VAR_DATA_FIM), '%d/%m/%Y')
             ELSE DATE(NULLIF(TRIM(COALESCE(:VAR_DATA_FIM, '')), ''))
        END AS DT_FIM_IN
),
D AS (
    SELECT
        LEAST(DT_INI, DT_FIM)                        AS DT_INI,
        GREATEST(DT_INI, DT_FIM)                     AS DT_FIM,
        YEAR(GREATEST(DT_INI, DT_FIM))               AS ANO_ATU,
        YEAR(GREATEST(DT_INI, DT_FIM)) - 1           AS ANO_ANT
    FROM (
        SELECT
            COALESCE(DT_FIM_IN, CURDATE())                              AS DT_FIM,
            COALESCE(DT_INI_IN,
                     MAKEDATE(YEAR(COALESCE(DT_FIM_IN, CURDATE())), 1)) AS DT_INI
        FROM PARAMS
    ) X
)
SELECT
    /* 1. o que a plataforma entregou nos parametros, em texto cru.
          Se vier vazio, o problema e o filtro da tela, nao a query. */
    CONCAT('[', COALESCE(:VAR_DATA_INICIO, '<NULO>'), ']')  AS P1_TXT_DATA_INICIO,
    CONCAT('[', COALESCE(:VAR_DATA_FIM, '<NULO>'), ']')     AS P2_TXT_DATA_FIM,
    CONCAT('[', COALESCE(:VAR_EMPRESA_DRE, '<NULO>'), ']')  AS P3_TXT_EMPRESA,

    /* 2. as datas depois de interpretadas. NULO aqui = formato nao
          reconhecido. */
    (SELECT DT_INI  FROM D)  AS P4_DATA_INICIO_LIDA,
    (SELECT DT_FIM  FROM D)  AS P5_DATA_FIM_LIDA,
    (SELECT ANO_ATU FROM D)  AS P6_ANO_COLUNA_ATUAL,
    (SELECT ANO_ANT FROM D)  AS P7_ANO_COLUNA_ANTERIOR,

    /* 3. a estrutura tem linhas? */
    (SELECT COUNT(*) FROM DET_DEMONSTRATIVO WHERE ID_ESTR_DEMONSTRATIVO = 3)
        AS P8_LINHAS_DA_ESTRUTURA,

    /* 4. quantas dessas linhas tem referencia no ano da coluna atual,
          com a normalizacao dos dois formatos. Se 0, o ano nao existe no
          cadastro. */
    (SELECT COUNT(*)
       FROM DET_DEMONSTRATIVO DET
      WHERE DET.ID_ESTR_DEMONSTRATIVO = 3
        AND EXISTS (SELECT 1 FROM DET_DEMONSTRATIVO_REFERENCIA R
                     WHERE R.ID_DET_DEMONSTRATIVO = DET.ID
                       AND (CASE WHEN R.ANO_REFERENCIA > 10000
                                 THEN FLOOR(R.ANO_REFERENCIA / 10000)
                                 ELSE R.ANO_REFERENCIA END) = (SELECT ANO_ATU FROM D)))
        AS P9_LINHAS_COM_REF_NO_ANO,

    /* 5. quantos vinculos de conta essas referencias carregam */
    (SELECT COUNT(*)
       FROM DET_DEMONSTRATIVO DET
       JOIN DET_DEMONSTRATIVO_REFERENCIA R ON R.ID_DET_DEMONSTRATIVO = DET.ID
       JOIN DET_DEMONSTRATIVO_CTACTB C ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = R.ID
      WHERE DET.ID_ESTR_DEMONSTRATIVO = 3
        AND (CASE WHEN R.ANO_REFERENCIA > 10000
                  THEN FLOOR(R.ANO_REFERENCIA / 10000)
                  ELSE R.ANO_REFERENCIA END) = (SELECT ANO_ATU FROM D))
        AS P10_VINCULOS_DE_CONTA,

    /* 6. o balancete tem linha para a empresa escolhida? (sem filtro de
          conta nem de data ainda) */
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE WHERE CODEMP = :VAR_EMPRESA_DRE)
        AS P11_LINHAS_BALANCETE_DA_EMPRESA,

    /* 7. e contas de resultado (3, 4, 5)? Se P11 > 0 e P12 = 0, o filtro
          de prefixo de conta e o problema. */
    (SELECT COUNT(DISTINCT CTACTB) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE
        AND (CTACTB LIKE '3%' OR CTACTB LIKE '4%' OR CTACTB LIKE '5%'))
        AS P12_CONTAS_RESULTADO,

    /* 8. dentro do periodo filtrado, quanto movimento existe? Se P12 > 0
          e P13 = 0, o corte de datas nao pega nada. */
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE
        AND (CTACTB LIKE '3%' OR CTACTB LIKE '4%' OR CTACTB LIKE '5%')
        AND DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D) AND (SELECT DT_FIM FROM D))
        AS P13_LANCAMENTOS_NO_PERIODO,

    /* 9. AS CONTAS DO CADASTRO CASAM COM AS DO BALANCETE?
          Este e o teste decisivo. Se P10 > 0, P12 > 0 e P14 = 0, o
          cadastro aponta para contas que nao existem no balancete - ou ha
          espaco sobrando no codigo de um dos lados. */
    (SELECT COUNT(*)
       FROM (SELECT DISTINCT C.PADRAO_CTACTB
               FROM DET_DEMONSTRATIVO DET
               JOIN DET_DEMONSTRATIVO_REFERENCIA R ON R.ID_DET_DEMONSTRATIVO = DET.ID
               JOIN DET_DEMONSTRATIVO_CTACTB C ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = R.ID
              WHERE DET.ID_ESTR_DEMONSTRATIVO = 3
                AND (CASE WHEN R.ANO_REFERENCIA > 10000
                          THEN FLOOR(R.ANO_REFERENCIA / 10000)
                          ELSE R.ANO_REFERENCIA END) = (SELECT ANO_ATU FROM D)) P
       JOIN (SELECT DISTINCT CTACTB FROM IMP_BASE_BALANCETE
              WHERE CODEMP = :VAR_EMPRESA_DRE) B
         ON B.CTACTB LIKE P.PADRAO_CTACTB)
        AS P14_CONTAS_QUE_CASAM,

    /* 10. o mesmo teste, mas com TRIM nos dois lados. Se P14 = 0 e
           P15 > 0, o problema e espaco em branco no codigo da conta. */
    (SELECT COUNT(*)
       FROM (SELECT DISTINCT TRIM(C.PADRAO_CTACTB) AS PADRAO_CTACTB
               FROM DET_DEMONSTRATIVO DET
               JOIN DET_DEMONSTRATIVO_REFERENCIA R ON R.ID_DET_DEMONSTRATIVO = DET.ID
               JOIN DET_DEMONSTRATIVO_CTACTB C ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = R.ID
              WHERE DET.ID_ESTR_DEMONSTRATIVO = 3
                AND (CASE WHEN R.ANO_REFERENCIA > 10000
                          THEN FLOOR(R.ANO_REFERENCIA / 10000)
                          ELSE R.ANO_REFERENCIA END) = (SELECT ANO_ATU FROM D)) P
       JOIN (SELECT DISTINCT TRIM(CTACTB) AS CTACTB FROM IMP_BASE_BALANCETE
              WHERE CODEMP = :VAR_EMPRESA_DRE) B
         ON B.CTACTB = P.PADRAO_CTACTB)
        AS P15_CASAM_COM_TRIM,

    /* 11. a soma final que a tela deveria mostrar, sem nenhuma das CTEs
           intermediarias. Se vier numero aqui e a tela mostrar zero, o
           problema esta na tela ou nas CTEs, nao nos dados. */
    (SELECT ROUND(SUM(B.VLR * C.SINAL), 2)
       FROM (SELECT CTACTB,
                    SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                                      AND (SELECT DT_FIM FROM D)
                             THEN VLRLANC ELSE 0 END) AS VLR
               FROM IMP_BASE_BALANCETE
              WHERE CODEMP = :VAR_EMPRESA_DRE
                AND (CTACTB LIKE '3%' OR CTACTB LIKE '4%' OR CTACTB LIKE '5%')
              GROUP BY CTACTB) B
       JOIN (SELECT DISTINCT C2.PADRAO_CTACTB, C2.SINAL
               FROM DET_DEMONSTRATIVO DET
               JOIN DET_DEMONSTRATIVO_REFERENCIA R ON R.ID_DET_DEMONSTRATIVO = DET.ID
               JOIN DET_DEMONSTRATIVO_CTACTB C2 ON C2.ID_DET_DEMONSTRATIVO_REFERENCIA = R.ID
              WHERE DET.ID_ESTR_DEMONSTRATIVO = 3
                AND (CASE WHEN R.ANO_REFERENCIA > 10000
                          THEN FLOOR(R.ANO_REFERENCIA / 10000)
                          ELSE R.ANO_REFERENCIA END) = (SELECT ANO_ATU FROM D)) C
         ON B.CTACTB LIKE C.PADRAO_CTACTB)
        AS P16_SOMA_ESPERADA
;
