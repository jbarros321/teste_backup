/* =====================================================================
   index9 - DMPL (queryDMPL)
   tenant_61302 | ESTR_DEMONSTRATIVOS.ID = 11

   Parametros do componente (os mesmos das telas index1 a index8):
       :VAR_DATA_INICIO   data inicial do filtro
       :VAR_DATA_FIM      data final   do filtro
       :VAR_EMPRESA_DRE   CODEMP
   A versao original usava uma data so (:VAR_DATA_REF_DRE / varMes) e o
   ANO cheio dela. Agora o periodo e o do filtro; com o filtro de
   01/01 a 31/12 o resultado e o mesmo da versao original.

   ATENCAO: nao use comentario de linha (--) nesta query. O componente
   grava tudo numa unica linha, e um -- comentaria todo o resto.

   REGRA
   Mesma logica da index8: linhas 1 e 5 = SALDO, 2/3/4 = MOVIMENTO.
   ANO_BASE (linhas 1 e 5) = saldo na vespera da data inicio do periodo
   comparativo; ANO_ANT e ANO_ATUAL = fechamento de cada periodo. A tela
   encadeia saldo base + movimentos, o que so fecha quando os dois
   periodos se encostam - ou seja, com o filtro no ano cheio, que e o uso
   deste relatorio ("exercicios findos em ...").

   O QUE MUDOU EM RELACAO A ORIGINAL
   - As datas passam pelo mesmo parser das outras telas ('DD/MM/AAAA' e
     'AAAA-MM-DD', com ou sem hora). DATE('31/12/2025') devolve NULL no
     MySQL, entao a data crua nao servia.
   - A tela vinha sem DMPL da query: na estrutura 11 TODAS as referencias
     sao AAAAMMDD e o `<= YEAR(...)` nunca casava.

   COLUNAS DEVOLVIDAS
       ORDEM, ANO_ATUAL, ANO_ANT, ANO_BASE   em MILHARES (como a original)
       + diagnostico
   ===================================================================== */

WITH PARAMS AS (
    SELECT
        /* A plataforma entrega 'DD/MM/AAAA HH:MM:SS'. O LEFT(...,10) corta
           a hora antes de testar, e as duas grafias sao tratadas
           separadamente - DATE() nao entende dia/mes/ano. */
        CASE
            WHEN LEFT(TRIM(COALESCE(:VAR_DATA_INICIO, '')), 10) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
                 THEN STR_TO_DATE(LEFT(TRIM(:VAR_DATA_INICIO), 10), '%d/%m/%Y')
            WHEN LEFT(TRIM(COALESCE(:VAR_DATA_INICIO, '')), 10) REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}$'
                 THEN DATE(LEFT(TRIM(:VAR_DATA_INICIO), 10))
            ELSE NULL
        END AS DT_INI_IN,
        CASE
            WHEN LEFT(TRIM(COALESCE(:VAR_DATA_FIM, '')), 10) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
                 THEN STR_TO_DATE(LEFT(TRIM(:VAR_DATA_FIM), 10), '%d/%m/%Y')
            WHEN LEFT(TRIM(COALESCE(:VAR_DATA_FIM, '')), 10) REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}$'
                 THEN DATE(LEFT(TRIM(:VAR_DATA_FIM), 10))
            ELSE NULL
        END AS DT_FIM_IN
),
D AS (
    SELECT
        LEAST(DT_INI, DT_FIM)                               AS DT_INI,
        GREATEST(DT_INI, DT_FIM)                            AS DT_FIM,
        YEAR(GREATEST(DT_INI, DT_FIM))                      AS ANO_ATU,
        DATE_SUB(LEAST(DT_INI, DT_FIM),    INTERVAL 1 YEAR) AS DT_INI_ANT,
        DATE_SUB(GREATEST(DT_INI, DT_FIM), INTERVAL 1 YEAR) AS DT_FIM_ANT
    FROM (
        SELECT
            COALESCE(DT_FIM_IN, CURDATE())                              AS DT_FIM,
            COALESCE(DT_INI_IN,
                     MAKEDATE(YEAR(COALESCE(DT_FIM_IN, CURDATE())), 1)) AS DT_INI
        FROM PARAMS
    ) X
),
base_bal AS (
    SELECT
        CTACTB,
        /* movimento do periodo atual e do comparativo */
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                          AND (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END) AS mov_atu,
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI_ANT FROM D)
                                          AND (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS mov_ant,
        /* saldos: fechamento do atual, fechamento do comparativo e
           abertura do comparativo (vespera da data inicio - 1 ano). Assim
           abertura + movimentos = fechamento, sem buraco nem sobreposicao,
           em qualquer periodo filtrado. */
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END) AS saldo_atu,
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS saldo_ant,
        SUM(CASE WHEN DATE(REFERENCIA) <  (SELECT DT_INI_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS saldo_base,
        /* abertura do periodo ATUAL (vespera da data inicio). No ano cheio
           e igual a saldo_ant; num periodo parcial (ex.: 01/03 a 31/07) ha
           um intervalo entre o fim do comparativo e o inicio do atual, e a
           tela precisa partir deste saldo em vez de encadear. */
        SUM(CASE WHEN DATE(REFERENCIA) <  (SELECT DT_INI FROM D)
                 THEN VLRLANC ELSE 0 END) AS saldo_abert
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
    GROUP BY CTACTB
),
linhas AS (
    SELECT DET.ID AS ID_DET, DET.ORDEM, DET.NOME_GRUPO
    FROM ESTR_DEMONSTRATIVOS EST
    INNER JOIN DET_DEMONSTRATIVO DET
            ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    WHERE EST.ID = 11
),
ref AS (
    /* cadastro do ano mais recente <= ano da data fim. O CASE normaliza
       os dois formatos de ANO_REFERENCIA, e o ORDER BY usa o ano ja
       normalizado (ordenando o valor cru, 20251201 venceria 2026). */
    SELECT L.ID_DET,
        (SELECT R.ID FROM DET_DEMONSTRATIVO_REFERENCIA R
          WHERE R.ID_DET_DEMONSTRATIVO = L.ID_DET
            AND (CASE WHEN R.ANO_REFERENCIA > 10000
                      THEN FLOOR(R.ANO_REFERENCIA / 10000)
                      ELSE R.ANO_REFERENCIA END) <= (SELECT ANO_ATU FROM D)
          ORDER BY (CASE WHEN R.ANO_REFERENCIA > 10000
                         THEN FLOOR(R.ANO_REFERENCIA / 10000)
                         ELSE R.ANO_REFERENCIA END) DESC,
                   R.ANO_REFERENCIA DESC, R.ID DESC LIMIT 1) AS ID_REF
    FROM linhas L
),
regras AS (
    SELECT DISTINCT L.ORDEM, C.PADRAO_CTACTB, C.SINAL
    FROM linhas L
    INNER JOIN ref RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
val AS (
    SELECT
        R.ORDEM,
        COUNT(*)                                   AS QTD,
        SUM(IFNULL(B.mov_atu,    0) * R.SINAL)     AS mov_atu,
        SUM(IFNULL(B.mov_ant,    0) * R.SINAL)     AS mov_ant,
        SUM(IFNULL(B.saldo_atu,  0) * R.SINAL)     AS saldo_atu,
        SUM(IFNULL(B.saldo_ant,  0) * R.SINAL)     AS saldo_ant,
        SUM(IFNULL(B.saldo_base, 0) * R.SINAL)     AS saldo_base,
        SUM(IFNULL(B.saldo_abert, 0) * R.SINAL)    AS saldo_abert
    FROM regras R
    LEFT JOIN base_bal B ON B.CTACTB = R.PADRAO_CTACTB
    GROUP BY R.ORDEM
)
SELECT
    L.ORDEM,
    (CASE WHEN V.QTD IS NULL THEN NULL
          WHEN TRIM(L.ORDEM) IN ('1','5') THEN V.saldo_atu
          ELSE V.mov_atu END) / 1000                          AS ANO_ATUAL,
    (CASE WHEN V.QTD IS NULL THEN NULL
          WHEN TRIM(L.ORDEM) IN ('1','5') THEN V.saldo_ant
          ELSE V.mov_ant END) / 1000                          AS ANO_ANT,
    (CASE WHEN V.QTD IS NULL THEN NULL
          WHEN TRIM(L.ORDEM) IN ('1','5') THEN V.saldo_base
          ELSE 0 END) / 1000                                  AS ANO_BASE,
    (SELECT DT_INI  FROM D)                                  AS DATA_INICIO_USADA,
    (SELECT DT_FIM  FROM D)                                  AS DATA_FIM_USADA,
    (SELECT ANO_ATU FROM D)                                  AS ANO_ATU,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE)                       AS LANC_EMPRESA,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE
        AND DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                 AND (SELECT DT_FIM FROM D)) AS LANC_PERIODO,
    CASE WHEN (SELECT DT_INI_IN FROM PARAMS) IS NULL
            OR (SELECT DT_FIM_IN FROM PARAMS) IS NULL
         THEN 0 ELSE 1 END                                   AS DATAS_OK
FROM linhas L
LEFT JOIN val V ON V.ORDEM = L.ORDEM
ORDER BY L.ORDEM;
