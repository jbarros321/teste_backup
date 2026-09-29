/* =====================================================================
   DRA - Demonstracao do Resultado Abrangente - query da tela index7.html
   tenant_61302

   Parametros do componente:
       :VAR_DATA_INICIO   data inicial do filtro da tela
       :VAR_DATA_FIM      data final   do filtro da tela
       :VAR_EMPRESA_DRE   CODEMP

   ATENCAO: nao use comentario de linha (--) nesta query. O componente
   grava tudo numa unica linha, e um -- comentaria todo o resto.

   ---------------------------------------------------------------------
   REGRA (mantida da versao original)
   ---------------------------------------------------------------------
   Lucro liquido do exercicio = soma do MOVIMENTO das contas de
   resultado (3, 4 e 5) no periodo. E fluxo, entao o BETWEEN entre data
   inicio e data fim esta CERTO aqui (como nas DRE). A coluna comparativa
   e o mesmo periodo deslocado um ano. Nao depende de cadastro de
   estrutura, entao nao existe "sem cadastro" nesta tela.

   Conferido em 29/09/2026, CODEMP 999: 2025 = 911.300,74 e
   2024 = 6.500.333,81 - identico a linha "Lucro do exercicio" da DFC.

   ---------------------------------------------------------------------
   O QUE ESTAVA ERRADO (querydados_index7.sql)
   ---------------------------------------------------------------------
   1) As datas iam cruas. A plataforma entrega 'DD/MM/AAAA HH:MM:SS', que
      nao compara com a coluna DATETIME. Agora passam pelo mesmo parser
      das outras telas (DD/MM/AAAA e AAAA-MM-DD, com ou sem hora).
   2) A tela precisava de uma segunda consulta (SELECT :VAR_MES FROM DUAL)
      so para descobrir o ano, e morria quando ela vinha vazia. A query
      agora devolve ANO_ATU e ANO_ANT.
   3) Divisao por 1000 no SQL: agora a query devolve o valor cheio e a
      tela converte para milhares, como a index5 e a index6.
   4) Sem diagnostico: "0" na tela podia ser empresa sem balancete,
      parametro nao declarado ou periodo vazio. Agora vem LANC_EMPRESA,
      LANC_PERIODO e DATAS_OK, como nas outras telas.

   COLUNAS DEVOLVIDAS (uma linha)
       VALOR_ATUAL, VALOR_ANTERIOR
       ANO_ATU, ANO_ANT, DATA_INICIO_USADA, DATA_FIM_USADA
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
    /* filtro vazio nao derruba a tela: sem data fim usa hoje, sem data
       inicio usa 01/01 do ano da data fim; datas invertidas sao trocadas.
       DATAS_OK = 0 avisa a tela quando isso acontece. */
    SELECT
        LEAST(DT_INI, DT_FIM)                               AS DT_INI,
        GREATEST(DT_INI, DT_FIM)                            AS DT_FIM,
        YEAR(GREATEST(DT_INI, DT_FIM))                      AS ANO_ATU,
        YEAR(GREATEST(DT_INI, DT_FIM)) - 1                  AS ANO_ANT,
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
res AS (
    SELECT
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                          AND (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END) AS V_ATU,
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI_ANT FROM D)
                                          AND (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS V_ANT
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
      AND (CTACTB LIKE '3%' OR CTACTB LIKE '4%' OR CTACTB LIKE '5%')
)
SELECT
    IFNULL((SELECT V_ATU FROM res), 0)                       AS VALOR_ATUAL,
    IFNULL((SELECT V_ANT FROM res), 0)                       AS VALOR_ANTERIOR,
    (SELECT ANO_ATU FROM D)                                  AS ANO_ATU,
    (SELECT ANO_ANT FROM D)                                  AS ANO_ANT,
    (SELECT DT_INI  FROM D)                                  AS DATA_INICIO_USADA,
    (SELECT DT_FIM  FROM D)                                  AS DATA_FIM_USADA,
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
FROM DUAL;
