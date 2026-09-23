/* =====================================================================
   12 - BP INTERNO - QUERY DEFINITIVA   (ESTR_DEMONSTRATIVOS.ID = 2)
   tenant_60677

   Substitui a query antiga do BP Interno. Devolve as mesmas 4 colunas:
   ORDEM, NOME_GRUPO, ANO_ATUAL, ANO_ANTERIOR (mais 3 de diagnostico).

   Mantem o comportamento da original: cada coluna usa o cadastro de
   contas DO SEU PROPRIO ANO - ANO_ATUAL com o cadastro do ano escolhido,
   ANO_ANTERIOR com o do ano anterior.

   O QUE FOI CORRIGIDO
   -------------------
   1) `DET.ID = DETREF.ID_DET_DEMONSTRATIVO` - a coluna nao existe mais.
      DET_DEMONSTRATIVO_REFERENCIA agora pendura em DET_DEMONSTRATIVO_ANO.
      E a causa do "Unknown column 'R.ID_DET_DEMONSTRATIVO'".

   2) `:VAR_DATA_REF_DRE` trocado por :VAR_ANO_FILTRO + :VAR_MES_FILTRO.
      Os dois podem vir vazios: o ano sai do filtro de ano, senao do mes
      ('07/2026' -> 2026), senao do ano corrente.

   3) `DETREF.REFERENCIA = DATE(:VAR_DATA_REF_DRE)` - igualdade exata de
      data. As referencias sao gravadas sempre no dia 01, entao uma data
      como 2026-09-21 nunca casava e a tela vinha zerada. Agora usa a
      referencia mais recente ate a data de corte.

   4) `B.CTACTB = DETCTACTB.PADRAO_CTACTB` - igualdade em vez de LIKE.
      Funciona so porque hoje o cadastro lista conta a conta; no dia em
      que alguem usar um padrao com %, ele e ignorado em silencio. Agora
      e LIKE, com desempate pelo padrao mais especifico.

   5) `IFNULL(DETCTACTB.SINAL, 0)` - multiplicar por 0 zera o vinculo.
      A convencao das outras telas e IFNULL(SINAL, 1).

   6) Passou a aplicar DET_DEMONSTRATIVO_CTACTB_EXC, que a original
      ignorava por completo.

   7) O ano escolhido precisa TER VINCULO. O ano 2026 do BP Externo tem
      24 referencias e zero vinculos; sem essa checagem a tela zera.

   Parametros: :VAR_ANO_FILTRO, :VAR_MES_FILTRO, :VAR_EMPRESA_DRE
   ===================================================================== */

WITH PARAMS AS (
    SELECT
        NULLIF(TRIM(COALESCE(:VAR_ANO_FILTRO, '')), '') AS ANO_TXT,
        CASE WHEN TRIM(COALESCE(:VAR_MES_FILTRO, '')) REGEXP '^[0-9]{2}/[0-9]{4}$'
             THEN TRIM(:VAR_MES_FILTRO)
        END                                             AS MES_TXT
),
ANO_RESOLVIDO AS (
    SELECT
        P.MES_TXT,
        CAST(COALESCE(P.ANO_TXT, RIGHT(P.MES_TXT, 4), YEAR(CURDATE())) AS UNSIGNED) AS ANO
    FROM PARAMS P
),
DATAS AS (
    SELECT
        A.ANO,
        /* mes de outro ano e ignorado: vale o ano inteiro */
        CASE WHEN A.MES_TXT IS NOT NULL
              AND CAST(RIGHT(A.MES_TXT, 4) AS UNSIGNED) = A.ANO
             THEN A.MES_TXT
        END AS MES
    FROM ANO_RESOLVIDO A
),
CORTES AS (
    SELECT
        D.ANO,
        D.MES,
        CASE WHEN D.MES IS NULL
             THEN STR_TO_DATE(CONCAT('31/12/', D.ANO), '%d/%m/%Y')
             ELSE LAST_DAY(STR_TO_DATE(CONCAT('01/', D.MES), '%d/%m/%Y'))
        END AS DATA_ATU,
        CASE WHEN D.MES IS NULL
             THEN STR_TO_DATE(CONCAT('31/12/', D.ANO - 1), '%d/%m/%Y')
             ELSE LAST_DAY(DATE_SUB(STR_TO_DATE(CONCAT('01/', D.MES), '%d/%m/%Y'), INTERVAL 1 YEAR))
        END AS DATA_ANT
    FROM DATAS D
),
/* as duas colunas do relatorio, cada uma com seu ano e sua data */
ALVOS AS (
    SELECT 'ATU' AS COLUNA, C.ANO     AS ANO_ALVO, C.DATA_ATU AS DATA_CORTE FROM CORTES C
    UNION ALL
    SELECT 'ANT',           C.ANO - 1,             C.DATA_ANT              FROM CORTES C
),
base_bal AS (
    SELECT
        CTACTB,
        SUM(CASE WHEN REFERENCIA <= (SELECT DATA_ATU FROM CORTES)
                 THEN VLRLANC ELSE 0 END) AS soma_ate_atu,
        SUM(CASE WHEN REFERENCIA <= (SELECT DATA_ANT FROM CORTES)
                 THEN VLRLANC ELSE 0 END) AS soma_ate_ant
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
    GROUP BY CTACTB
),
/* ano vigente de cada linha, por coluna: o mais recente ate o ano alvo
   QUE TENHA VINCULO DE CONTA */
ano_max AS (
    SELECT
        AL.COLUNA,
        AL.DATA_CORTE,
        A.ID_DET_DEMONSTRATIVO,
        MAX(A.ANO) AS ANO
    FROM ALVOS AL
    JOIN DET_DEMONSTRATIVO_ANO A
      ON A.ANO <= AL.ANO_ALVO
    WHERE EXISTS (
        SELECT 1
        FROM DET_DEMONSTRATIVO_REFERENCIA R2
        JOIN DET_DEMONSTRATIVO_CTACTB     C2
          ON C2.ID_DET_DEMONSTRATIVO_REFERENCIA = R2.ID
        WHERE R2.ID_DET_DEMONSTRATIVO_ANO = A.ID
    )
    GROUP BY AL.COLUNA, AL.DATA_CORTE, A.ID_DET_DEMONSTRATIVO
),
ano_vigente AS (
    SELECT M.COLUNA, M.DATA_CORTE, M.ID_DET_DEMONSTRATIVO, A.ID AS ID_ANO, A.ANO
    FROM ano_max M
    JOIN DET_DEMONSTRATIVO_ANO A
      ON A.ID_DET_DEMONSTRATIVO = M.ID_DET_DEMONSTRATIVO
     AND A.ANO = M.ANO
),
/* UMA referencia por ano vigente - e o que impede Fornecedores, que tem
   12 referencias mensais em 2026, de ser contado 12 vezes */
ref_vigente AS (
    SELECT
        V.COLUNA, V.ID_DET_DEMONSTRATIVO, V.ID_ANO, V.ANO,
        COALESCE(
            (SELECT MAX(R.REFERENCIA) FROM DET_DEMONSTRATIVO_REFERENCIA R
              WHERE R.ID_DET_DEMONSTRATIVO_ANO = V.ID_ANO
                AND R.REFERENCIA <= V.DATA_CORTE),
            (SELECT MIN(R.REFERENCIA) FROM DET_DEMONSTRATIVO_REFERENCIA R
              WHERE R.ID_DET_DEMONSTRATIVO_ANO = V.ID_ANO)
        ) AS REFERENCIA
    FROM ano_vigente V
),
regras_contas AS (
    SELECT DISTINCT
        RV.COLUNA, DET.ORDEM, DET.NOME_GRUPO, RV.ANO,
        C.PADRAO_CTACTB, C.SINAL
    FROM ESTR_DEMONSTRATIVOS          EST
    JOIN DET_DEMONSTRATIVO            DET ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    JOIN ref_vigente                  RV  ON RV.ID_DET_DEMONSTRATIVO = DET.ID
    JOIN DET_DEMONSTRATIVO_REFERENCIA R   ON R.ID_DET_DEMONSTRATIVO_ANO = RV.ID_ANO
                                         AND R.REFERENCIA = RV.REFERENCIA
    JOIN DET_DEMONSTRATIVO_CTACTB     C   ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = R.ID
    WHERE EST.ID = 2
),
regras_excluidas AS (
    SELECT DISTINCT
        RV.COLUNA, DET.ORDEM, DET.NOME_GRUPO, C.PADRAO_CTACTB
    FROM ESTR_DEMONSTRATIVOS              EST
    JOIN DET_DEMONSTRATIVO                DET ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    JOIN ref_vigente                      RV  ON RV.ID_DET_DEMONSTRATIVO = DET.ID
    JOIN DET_DEMONSTRATIVO_REFERENCIA     R   ON R.ID_DET_DEMONSTRATIVO_ANO = RV.ID_ANO
                                             AND R.REFERENCIA = RV.REFERENCIA
    JOIN DET_DEMONSTRATIVO_CTACTB_EXC     C   ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = R.ID
    WHERE EST.ID = 2
),
contas_mapeadas AS (
    SELECT
        R.COLUNA, R.ORDEM, R.NOME_GRUPO,
        IFNULL(R.SINAL, 1) AS SINAL,
        CASE WHEN R.COLUNA = 'ATU' THEN B.soma_ate_atu ELSE B.soma_ate_ant END AS VALOR,
        ROW_NUMBER() OVER (
            PARTITION BY R.COLUNA, B.CTACTB, R.ORDEM, R.NOME_GRUPO
            ORDER BY LENGTH(R.PADRAO_CTACTB) DESC, R.PADRAO_CTACTB DESC
        ) AS rn
    FROM base_bal B
    JOIN regras_contas R
      ON B.CTACTB LIKE R.PADRAO_CTACTB
    LEFT JOIN regras_excluidas E
      ON E.COLUNA = R.COLUNA
     AND E.ORDEM = R.ORDEM
     AND E.NOME_GRUPO = R.NOME_GRUPO
     AND B.CTACTB LIKE E.PADRAO_CTACTB
    WHERE E.PADRAO_CTACTB IS NULL
)
SELECT
    G.ORDEM,
    G.NOME_GRUPO,
    IFNULL(SUM(CASE WHEN M.COLUNA = 'ATU' THEN M.VALOR * M.SINAL END), 0) AS ANO_ATUAL,
    IFNULL(SUM(CASE WHEN M.COLUNA = 'ANT' THEN M.VALOR * M.SINAL END), 0) AS ANO_ANTERIOR,
    (SELECT ANO      FROM CORTES) AS ANO_USADO,
    (SELECT MES      FROM CORTES) AS MES_USADO,
    (SELECT DATA_ATU FROM CORTES) AS DATA_CORTE
FROM (SELECT DISTINCT ORDEM, NOME_GRUPO FROM regras_contas) G
LEFT JOIN contas_mapeadas M
       ON M.ORDEM = G.ORDEM
      AND M.NOME_GRUPO = G.NOME_GRUPO
      AND M.rn = 1
GROUP BY G.ORDEM, G.NOME_GRUPO
ORDER BY G.ORDEM;
