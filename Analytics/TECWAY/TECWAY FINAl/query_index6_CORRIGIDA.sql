/* =====================================================================
   INDICADORES FINANCEIROS (BP EXTERNO) - query da tela index6.html
   tenant_61302 | ESTR_DEMONSTRATIVOS.ID = 1

   Parametros do componente:
       :VAR_DATA_INICIO   data inicial do filtro da tela
       :VAR_DATA_FIM      data final   do filtro da tela
       :VAR_EMPRESA_DRE   CODEMP

   ATENCAO: nao use comentario de linha (--) nesta query. O componente
   grava tudo numa unica linha, e um -- comentaria todo o resto.

   ---------------------------------------------------------------------
   MESMO MOTOR DA index1 (BP Externo)
   ---------------------------------------------------------------------
   Do WITH ate tot_ant e a query da index1, sem alteracao. Os indicadores
   de liquidez e solvencia sao calculados sobre as MESMAS linhas do
   balanco que a index1 mostra, entao os numeros das duas telas batem.

   ---------------------------------------------------------------------
   O QUE ESTAVA ERRADO NA VERSAO ORIGINAL (querdados_index6.sql)
   ---------------------------------------------------------------------
   1) BETWEEN :VAR_DATA_INICIO AND :VAR_DATA_FIM - dava o MOVIMENTO do
      periodo, nao o SALDO. Indicador de balanco e sobre saldo acumulado
      ate a data fim: filtrando julho, a liquidez era calculada so sobre
      o que se movimentou em julho.
   2) As datas iam cruas; a plataforma entrega 'DD/MM/AAAA HH:MM:SS', que
      nao compara com a coluna. Agora passam pelo mesmo parser das outras.
   3) ANO_REFERENCIA com dois formatos (2025 e 20251201): o
      `ANO_REFERENCIA <= YEAR(...)` virava `20251201 <= 2025`, falso, e a
      linha sumia do indicador sem aviso.
   4) Um cadastro so para as duas colunas; agora cada coluna usa o do seu
      ano, sem fallback.
   5) Exclusoes (DET_DEMONSTRATIVO_CTACTB_EXC) eram ignoradas.

   ---------------------------------------------------------------------
   COLUNAS DEVOLVIDAS (uma linha)
   ---------------------------------------------------------------------
       AC, PC, ESTOQUE, RLP, PNC, ATIVO_TOTAL  (_ATU e _ANT)
       SEM_CADASTRO_ATU / _ANT   quantas linhas do BP (grupos 1 e 2) nao
                                 tem conta vinculada naquele ano - com
                                 isso > 0 o indicador esta incompleto
       ANO_ATU, ANO_ANT, DATA_INICIO_USADA, DATA_FIM_USADA
       LANC_EMPRESA, LANC_PERIODO, DATAS_OK
   ===================================================================== */

WITH PARAMS AS (
    SELECT
        /* aceita 'AAAA-MM-DD' e 'DD/MM/AAAA', que e como a plataforma
           costuma devolver o filtro de data */
        /* A plataforma entrega a data como 'DD/MM/AAAA HH:MM:SS'. O parser
           anterior exigia 'DD/MM/AAAA' exato, entao a hora fazia o REGEXP
           falhar; e DATE('01/01/2025 00:00:00') devolve NULL no MySQL,
           porque DATE() nao entende dia/mes/ano. Resultado: a data virava
           NULL, caia no CURDATE() e a tela calculava sobre hoje.

           O LEFT(...,10) corta a hora antes de testar, e as duas grafias
           (DD/MM/AAAA e AAAA-MM-DD) sao tratadas separadamente. */
        CASE
            WHEN LEFT(TRIM(COALESCE(:VAR_DATA_INICIO, '')), 10) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
                 THEN STR_TO_DATE(LEFT(TRIM(:VAR_DATA_INICIO), 10), '%d/%m/%Y')
            WHEN LEFT(TRIM(COALESCE(:VAR_DATA_INICIO, '')), 10) REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}$'
                 THEN DATE(LEFT(TRIM(:VAR_DATA_INICIO), 10))
            ELSE NULL
        END AS DT_INI_IN,
        /* A plataforma entrega a data como 'DD/MM/AAAA HH:MM:SS'. O parser
           anterior exigia 'DD/MM/AAAA' exato, entao a hora fazia o REGEXP
           falhar; e DATE('01/01/2025 00:00:00') devolve NULL no MySQL,
           porque DATE() nao entende dia/mes/ano. Resultado: a data virava
           NULL, caia no CURDATE() e a tela calculava sobre hoje.

           O LEFT(...,10) corta a hora antes de testar, e as duas grafias
           (DD/MM/AAAA e AAAA-MM-DD) sao tratadas separadamente. */
        CASE
            WHEN LEFT(TRIM(COALESCE(:VAR_DATA_FIM, '')), 10) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
                 THEN STR_TO_DATE(LEFT(TRIM(:VAR_DATA_FIM), 10), '%d/%m/%Y')
            WHEN LEFT(TRIM(COALESCE(:VAR_DATA_FIM, '')), 10) REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}$'
                 THEN DATE(LEFT(TRIM(:VAR_DATA_FIM), 10))
            ELSE NULL
        END AS DT_FIM_IN
),
D AS (
    /* filtro vazio nao pode derrubar a tela: sem data fim usa hoje, sem
       data inicio usa 01/01 do ano da data fim. Se vierem invertidas,
       troca - em vez de devolver um periodo negativo. */
    SELECT
        LEAST(DT_INI, DT_FIM)                        AS DT_INI,
        GREATEST(DT_INI, DT_FIM)                     AS DT_FIM,
        YEAR(GREATEST(DT_INI, DT_FIM))               AS ANO_ATU,
        YEAR(GREATEST(DT_INI, DT_FIM)) - 1           AS ANO_ANT,
        DATE_SUB(LEAST(DT_INI, DT_FIM),    INTERVAL 1 YEAR) AS DT_INI_ANT,
        DATE_SUB(GREATEST(DT_INI, DT_FIM), INTERVAL 1 YEAR) AS DT_FIM_ANT
    FROM (
        SELECT
            COALESCE(DT_FIM_IN, CURDATE())                            AS DT_FIM,
            COALESCE(DT_INI_IN,
                     MAKEDATE(YEAR(COALESCE(DT_FIM_IN, CURDATE())), 1)) AS DT_INI
        FROM PARAMS
    ) X
),
base_bal AS (
    SELECT
        CTACTB,
        /* SALDO acumulado ate a data fim - e isto que vai nas linhas de
           balanco. Antes era BETWEEN, que dava so o movimento. */
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS saldo_atu,
        /* FLUXO do periodo - so para a linha de resultado 3.3.2 */
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                          AND (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS fluxo_atu,
        /* SALDO DE ABERTURA do periodo - so para a linha 3.3.1 */
        SUM(CASE WHEN DATE(REFERENCIA) < (SELECT DT_INI FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS abertura_atu,
        /* os mesmos tres cortes deslocados um ano, para a coluna
           comparativa */
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS saldo_ant,
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI_ANT FROM D)
                                          AND (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS fluxo_ant,
        SUM(CASE WHEN DATE(REFERENCIA) < (SELECT DT_INI_ANT FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS abertura_ant
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
    GROUP BY CTACTB
),
linhas AS (
    /* a lista de linhas vem da ESTRUTURA, nao dos vinculos. E o que faz a
       linha sem cadastro continuar aparecendo, marcada, em vez de sumir
       da tela e mexer no total sem explicacao. */
    SELECT
        DET.ID   AS ID_DET,
        DET.ORDEM,
        DET.NOME_GRUPO,
        DET.COD_NOTA_EXPLICATIVA
    FROM ESTR_DEMONSTRATIVOS EST
    INNER JOIN DET_DEMONSTRATIVO DET
            ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    WHERE EST.ID = 1
      AND TRIM(DET.ORDEM) <> '3.3.3'
),
ref_atu AS (
    /* a referencia do ano da COLUNA ATUAL, e so dela.

       O CASE normaliza os dois formatos de ANO_REFERENCIA (2025 e
       20251201) para o ano - sem isso a comparacao com YEAR() e sempre
       falsa nos registros AAAAMMDD e a linha desaparece.

       O LIMIT 1 garante uma referencia so, mesmo que o ano venha a ter
       dois registros (2025 e 20251201), o que dobraria os vinculos. */
    SELECT
        L.ID_DET,
        (SELECT R.ID
           FROM DET_DEMONSTRATIVO_REFERENCIA R
          WHERE R.ID_DET_DEMONSTRATIVO = L.ID_DET
            AND (CASE WHEN R.ANO_REFERENCIA > 10000
                      THEN FLOOR(R.ANO_REFERENCIA / 10000)
                      ELSE R.ANO_REFERENCIA END) = (SELECT ANO_ATU FROM D)
          ORDER BY R.ANO_REFERENCIA DESC, R.ID DESC
          LIMIT 1) AS ID_REF
    FROM linhas L
),
ref_ant AS (
    /* a mesma coisa para o ano da COLUNA COMPARATIVA */
    SELECT
        L.ID_DET,
        (SELECT R.ID
           FROM DET_DEMONSTRATIVO_REFERENCIA R
          WHERE R.ID_DET_DEMONSTRATIVO = L.ID_DET
            AND (CASE WHEN R.ANO_REFERENCIA > 10000
                      THEN FLOOR(R.ANO_REFERENCIA / 10000)
                      ELSE R.ANO_REFERENCIA END) = (SELECT ANO_ANT FROM D)
          ORDER BY R.ANO_REFERENCIA DESC, R.ID DESC
          LIMIT 1) AS ID_REF
    FROM linhas L
),
regras_atu AS (
    SELECT DISTINCT L.ORDEM, L.NOME_GRUPO,
           C.PADRAO_CTACTB, C.SINAL
    FROM linhas L
    INNER JOIN ref_atu RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
regras_ant AS (
    SELECT DISTINCT L.ORDEM, L.NOME_GRUPO,
           C.PADRAO_CTACTB, C.SINAL
    FROM linhas L
    INNER JOIN ref_ant RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
exc_atu AS (
    /* a ORDEM voltou: sem ela uma conta excluida de uma linha era
       excluida de todas */
    SELECT DISTINCT L.ORDEM, C.PADRAO_CTACTB
    FROM linhas L
    INNER JOIN ref_atu RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB_EXC C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
exc_ant AS (
    SELECT DISTINCT L.ORDEM, C.PADRAO_CTACTB
    FROM linhas L
    INNER JOIN ref_ant RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB_EXC C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
qtd_atu AS (
    SELECT ORDEM, NOME_GRUPO, COUNT(*) AS QTD
    FROM regras_atu GROUP BY ORDEM, NOME_GRUPO
),
qtd_ant AS (
    SELECT ORDEM, NOME_GRUPO, COUNT(*) AS QTD
    FROM regras_ant GROUP BY ORDEM, NOME_GRUPO
),
map_atu AS (
    SELECT
        R.ORDEM, R.NOME_GRUPO, R.SINAL,
        /* 3.3.1 e 3.3.2 sao linhas de RESULTADO, nao de saldo:
           3.3.1 = saldo de abertura do periodo
           3.3.2 = fluxo do periodo
           qualquer outra linha = saldo acumulado ate a data fim */
        CASE WHEN TRIM(R.ORDEM) = '3.3.1' THEN B.abertura_atu
             WHEN TRIM(R.ORDEM) = '3.3.2' THEN B.fluxo_atu
             ELSE B.saldo_atu
        END AS valor,
        ROW_NUMBER() OVER (
            PARTITION BY B.CTACTB, R.ORDEM, R.NOME_GRUPO
            ORDER BY LENGTH(R.PADRAO_CTACTB) DESC, R.PADRAO_CTACTB DESC
        ) AS rn
    FROM base_bal B
    INNER JOIN regras_atu R ON B.CTACTB LIKE R.PADRAO_CTACTB
    LEFT JOIN exc_atu E ON E.ORDEM = R.ORDEM
                       AND B.CTACTB LIKE E.PADRAO_CTACTB
    WHERE E.PADRAO_CTACTB IS NULL
),
map_ant AS (
    SELECT
        R.ORDEM, R.NOME_GRUPO, R.SINAL,
        CASE WHEN TRIM(R.ORDEM) = '3.3.1' THEN B.abertura_ant
             WHEN TRIM(R.ORDEM) = '3.3.2' THEN B.fluxo_ant
             ELSE B.saldo_ant
        END AS valor,
        ROW_NUMBER() OVER (
            PARTITION BY B.CTACTB, R.ORDEM, R.NOME_GRUPO
            ORDER BY LENGTH(R.PADRAO_CTACTB) DESC, R.PADRAO_CTACTB DESC
        ) AS rn
    FROM base_bal B
    INNER JOIN regras_ant R ON B.CTACTB LIKE R.PADRAO_CTACTB
    LEFT JOIN exc_ant E ON E.ORDEM = R.ORDEM
                       AND B.CTACTB LIKE E.PADRAO_CTACTB
    WHERE E.PADRAO_CTACTB IS NULL
),
tot_atu AS (
    SELECT ORDEM, NOME_GRUPO, SUM(valor * SINAL) AS V
    FROM map_atu WHERE rn = 1 GROUP BY ORDEM, NOME_GRUPO
),
tot_ant AS (
    SELECT ORDEM, NOME_GRUPO, SUM(valor * SINAL) AS V
    FROM map_ant WHERE rn = 1 GROUP BY ORDEM, NOME_GRUPO
)
,
vals AS (
    /* valor de cada linha do balanco. Linha sem cadastro no ano entra
       como NULL (e nao soma nada), e e contada em SEM_CADASTRO_*. */
    SELECT
        L.ORDEM,
        CASE WHEN QA.QTD IS NULL THEN NULL ELSE IFNULL(TA.V, 0) END AS V_ATU,
        CASE WHEN QB.QTD IS NULL THEN NULL ELSE IFNULL(TB.V, 0) END AS V_ANT
    FROM linhas L
    LEFT JOIN qtd_atu QA ON QA.ORDEM = L.ORDEM AND QA.NOME_GRUPO = L.NOME_GRUPO
    LEFT JOIN qtd_ant QB ON QB.ORDEM = L.ORDEM AND QB.NOME_GRUPO = L.NOME_GRUPO
    LEFT JOIN tot_atu TA ON TA.ORDEM = L.ORDEM AND TA.NOME_GRUPO = L.NOME_GRUPO
    LEFT JOIN tot_ant TB ON TB.ORDEM = L.ORDEM AND TB.NOME_GRUPO = L.NOME_GRUPO
)
SELECT
    IFNULL(SUM(CASE WHEN ORDEM LIKE '1.1.%'   THEN V_ATU END), 0) AS AC_ATU,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '2.1.%'   THEN V_ATU END), 0) AS PC_ATU,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '1.1.3%'  THEN V_ATU END), 0) AS ESTOQUE_ATU,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '1.2.1%'  THEN V_ATU END), 0) AS RLP_ATU,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '2.2.%'   THEN V_ATU END), 0) AS PNC_ATU,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '1.%'     THEN V_ATU END), 0) AS ATIVO_TOTAL_ATU,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '1.1.%'   THEN V_ANT END), 0) AS AC_ANT,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '2.1.%'   THEN V_ANT END), 0) AS PC_ANT,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '1.1.3%'  THEN V_ANT END), 0) AS ESTOQUE_ANT,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '1.2.1%'  THEN V_ANT END), 0) AS RLP_ANT,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '2.2.%'   THEN V_ANT END), 0) AS PNC_ANT,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '1.%'     THEN V_ANT END), 0) AS ATIVO_TOTAL_ANT,
    SUM(CASE WHEN (ORDEM LIKE '1.%' OR ORDEM LIKE '2.%') AND V_ATU IS NULL
             THEN 1 ELSE 0 END)                                  AS SEM_CADASTRO_ATU,
    SUM(CASE WHEN (ORDEM LIKE '1.%' OR ORDEM LIKE '2.%') AND V_ANT IS NULL
             THEN 1 ELSE 0 END)                                  AS SEM_CADASTRO_ANT,
    (SELECT ANO_ATU FROM D)                                      AS ANO_ATU,
    (SELECT ANO_ANT FROM D)                                      AS ANO_ANT,
    (SELECT DT_INI  FROM D)                                      AS DATA_INICIO_USADA,
    (SELECT DT_FIM  FROM D)                                      AS DATA_FIM_USADA,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE)                           AS LANC_EMPRESA,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE
        AND DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                 AND (SELECT DT_FIM FROM D))     AS LANC_PERIODO,
    /* 1 = as duas datas chegaram. 0 = a query calcularia sobre a data de
       hoje, e a tela deve PARAR em vez de mostrar numero plausivel. */
    CASE WHEN (SELECT DT_INI_IN FROM PARAMS) IS NULL
            OR (SELECT DT_FIM_IN FROM PARAMS) IS NULL
         THEN 0 ELSE 1 END                                       AS DATAS_OK
FROM vals;
