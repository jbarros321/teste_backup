/* =====================================================================
   DMPL - Mutacoes do Patrimonio Liquido - query da tela index8.html
   tenant_61302 | ESTR_DEMONSTRATIVOS.ID = 11

   Parametros do componente:
       :VAR_DATA_INICIO   data inicial do filtro da tela
       :VAR_DATA_FIM      data final   do filtro da tela
       :VAR_EMPRESA_DRE   CODEMP

   ATENCAO: nao use comentario de linha (--) nesta query. O componente
   grava tudo numa unica linha, e um -- comentaria todo o resto.

   ---------------------------------------------------------------------
   REGRA DA DMPL (mantida da versao original)
   ---------------------------------------------------------------------
   Linhas da estrutura 11:
       1 Capital social          -> SALDO
       2 Lucros distribuidos     -> MOVIMENTO do periodo
       3 Lucro do exercicio      -> MOVIMENTO do periodo
       4 Ajuste de avaliacao     -> MOVIMENTO do periodo
       5 Lucros acumulados       -> SALDO
   A tela parte do saldo inicial (ANO_BASE das linhas 1 e 5) e soma os
   movimentos do periodo anterior e do atual.

   ---------------------------------------------------------------------
   O QUE ESTAVA ERRADO (query_index8.sql)
   ---------------------------------------------------------------------
   1) A TELA VINHA VAZIA. Na estrutura 11 TODAS as referencias estao como
      AAAAMMDD (20231201, 20241201, 20251201). O
      `ANO_REFERENCIA <= YEAR(:VAR_DATA_FIM)` virava `20251201 <= 2025`,
      falso, e nenhuma linha voltava. Agora o ano e normalizado.
   2) As datas iam cruas; a plataforma entrega 'DD/MM/AAAA HH:MM:SS'.
      Agora passam pelo mesmo parser das outras telas.
   3) O saldo inicial era "data fim menos 2 anos", o que so acerta com o
      filtro no ano cheio. Agora e o saldo na vespera da data inicio do
      periodo comparativo, e o saldo intermediario e o fechamento desse
      periodo. Com filtro de ano cheio o resultado e identico. Com periodo
      parcial, o bloco atual parte do saldo real na vespera da data
      inicio (ABERTURA_ATU), porque entre o fim do comparativo e o inicio
      do atual ha meses que nenhum dos dois blocos cobre.
   4) Divisao por 1000 no SQL: agora a query devolve o valor cheio e a
      tela converte para milhares, como index5/6/7.
   5) A tela precisava de uma segunda consulta (SELECT :VAR_MES) para
      descobrir o ano. Agora vem ANO_ATU, e as datas de cada saldo.

   CADASTRO: todas as colunas usam o cadastro do ano mais recente que seja
   <= ano da data fim (mesma regra da index5). Na estrutura 11 os anos
   2023, 2024 e 2025 tem cadastro identico.

   COLUNAS DEVOLVIDAS
       ORDEM, NOME_GRUPO
       ANO_ATUAL, ANO_ANT, ANO_BASE     (valor cheio; NULL = sem cadastro)
       ABERTURA_ATU, DATA_ABERTURA_ATU  saldo na vespera da data inicio -
                                        a tela usa quando o periodo nao
                                        encosta no comparativo
       SEM_CADASTRO
       ANO_ATU
       DATA_SALDO_BASE, DATA_SALDO_ANT, DATA_SALDO_ATU   datas de cada saldo
                                         (para o rotulo "Saldos em ...")
       DATA_INICIO_USADA, DATA_FIM_USADA
       LANC_EMPRESA, LANC_PERIODO, DATAS_OK
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
    L.NOME_GRUPO,
    /* linhas 1 e 5 sao SALDO; 2, 3 e 4 sao MOVIMENTO. Sem cadastro, NULL. */
    CASE WHEN V.QTD IS NULL THEN NULL
         WHEN TRIM(L.ORDEM) IN ('1','5') THEN V.saldo_atu
         ELSE V.mov_atu END                                  AS ANO_ATUAL,
    CASE WHEN V.QTD IS NULL THEN NULL
         WHEN TRIM(L.ORDEM) IN ('1','5') THEN V.saldo_ant
         ELSE V.mov_ant END                                  AS ANO_ANT,
    CASE WHEN V.QTD IS NULL THEN NULL
         WHEN TRIM(L.ORDEM) IN ('1','5') THEN V.saldo_base
         ELSE 0 END                                          AS ANO_BASE,
    CASE WHEN V.QTD IS NULL THEN NULL
         WHEN TRIM(L.ORDEM) IN ('1','5') THEN V.saldo_abert
         ELSE 0 END                                          AS ABERTURA_ATU,
    CASE WHEN V.QTD IS NULL THEN 1 ELSE 0 END                AS SEM_CADASTRO,
    (SELECT ANO_ATU FROM D)                                  AS ANO_ATU,
    (SELECT DATE_SUB(DT_INI_ANT, INTERVAL 1 DAY) FROM D)     AS DATA_SALDO_BASE,
    (SELECT DT_FIM_ANT FROM D)                               AS DATA_SALDO_ANT,
    (SELECT DATE_SUB(DT_INI, INTERVAL 1 DAY) FROM D)         AS DATA_ABERTURA_ATU,
    (SELECT DT_FIM     FROM D)                               AS DATA_SALDO_ATU,
    (SELECT DT_INI     FROM D)                               AS DATA_INICIO_USADA,
    (SELECT DT_FIM     FROM D)                               AS DATA_FIM_USADA,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE)                       AS LANC_EMPRESA,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE
        AND DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                 AND (SELECT DT_FIM FROM D)) AS LANC_PERIODO,
    /* 1 = as duas datas chegaram. 0 = a query calcularia sobre a data de
       hoje, e a tela deve PARAR em vez de mostrar numero plausivel. */
    CASE WHEN (SELECT DT_INI_IN FROM PARAMS) IS NULL
            OR (SELECT DT_FIM_IN FROM PARAMS) IS NULL
         THEN 0 ELSE 1 END                                   AS DATAS_OK
FROM linhas L
LEFT JOIN val V ON V.ORDEM = L.ORDEM
ORDER BY L.ORDEM;
