/* =====================================================================
   index9 - Indicadores (queryIndicadores)
   tenant_61302 | ESTR_DEMONSTRATIVOS.ID = 1

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
   Mesmo motor do queryBP (saldo ate a data fim, exclusoes, conta mais
   especifica), entao os indicadores batem com o balanco do relatorio.

   O QUE MUDOU EM RELACAO A ORIGINAL
   - As datas passam pelo mesmo parser das outras telas ('DD/MM/AAAA' e
     'AAAA-MM-DD', com ou sem hora). DATE('31/12/2025') devolve NULL no
     MySQL, entao a data crua nao servia.
   - ANO_REFERENCIA com dois formatos (2025 e 20251201):
     `ANO_REFERENCIA <= YEAR(...)` virava `20251201 <= 2025`, FALSO, e a
     linha sumia. Agora o ano e normalizado antes de comparar.
   - A original juntava conta = padrao por igualdade e ignorava as
     exclusoes; o queryBP usa LIKE e exclusoes. Agora os dois usam o
     mesmo calculo.

   COLUNAS DEVOLVIDAS
       AC, PC, ESTOQUE, RLP, PNC, ATIVO_TOTAL (_ATU/_ANT)  valor cheio
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
    /* filtro vazio nao derruba a tela: sem data fim usa hoje, sem data
       inicio usa 01/01 do ano da data fim; datas invertidas sao trocadas.
       DATAS_OK = 0 avisa quando isso acontece. */
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
base_bal AS (
    SELECT
        CTACTB,
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END) AS saldo_atu,
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                          AND (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END) AS fluxo_atu,
        SUM(CASE WHEN DATE(REFERENCIA) <  (SELECT DT_INI FROM D)
                 THEN VLRLANC ELSE 0 END) AS abert_atu,
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS saldo_ant,
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI_ANT FROM D)
                                          AND (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS fluxo_ant,
        SUM(CASE WHEN DATE(REFERENCIA) <  (SELECT DT_INI_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS abert_ant
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
    GROUP BY CTACTB
),
linhas AS (
    SELECT DET.ID AS ID_DET, DET.ORDEM, DET.NOME_GRUPO, DET.COD_NOTA_EXPLICATIVA, DET.VARIACAO
    FROM ESTR_DEMONSTRATIVOS EST
    INNER JOIN DET_DEMONSTRATIVO DET ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    WHERE EST.ID = 1
      AND TRIM(DET.ORDEM) <> '3.3.3'
),
ref AS (
    /* cadastro do ano mais recente <= ano da data fim, para as DUAS
       colunas (regra da query original, a mesma da index5). O CASE
       normaliza os dois formatos de ANO_REFERENCIA (2025 e 20251201) e o
       ORDER BY usa o ano ja normalizado - ordenando o valor cru, 20251201
       venceria 2026. O LIMIT 1 impede que um ano com dois registros dobre
       os vinculos. */
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
    SELECT DISTINCT L.ORDEM, L.NOME_GRUPO, L.COD_NOTA_EXPLICATIVA,
           C.PADRAO_CTACTB, C.SINAL
    FROM linhas L
    INNER JOIN ref RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
excl AS (
    SELECT DISTINCT L.ORDEM, E.PADRAO_CTACTB
    FROM linhas L
    INNER JOIN ref RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB_EXC E
            ON E.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
contas_mapeadas AS (
    SELECT
        R.ORDEM, R.NOME_GRUPO, R.SINAL,
        CASE WHEN TRIM(R.ORDEM) = '3.3.1' THEN B.abert_atu
             WHEN TRIM(R.ORDEM) = '3.3.2' THEN B.fluxo_atu
             WHEN R.ORDEM LIKE '1.%' OR R.ORDEM LIKE '2.%' OR R.ORDEM LIKE '3.%'
                  THEN B.saldo_atu
             ELSE B.fluxo_atu END AS valor_atu,
        CASE WHEN TRIM(R.ORDEM) = '3.3.1' THEN B.abert_ant
             WHEN TRIM(R.ORDEM) = '3.3.2' THEN B.fluxo_ant
             WHEN R.ORDEM LIKE '1.%' OR R.ORDEM LIKE '2.%' OR R.ORDEM LIKE '3.%'
                  THEN B.saldo_ant
             ELSE B.fluxo_ant END AS valor_ant,
        ROW_NUMBER() OVER (
            PARTITION BY B.CTACTB, R.ORDEM, R.NOME_GRUPO
            ORDER BY LENGTH(R.PADRAO_CTACTB) DESC, R.PADRAO_CTACTB DESC
        ) AS rn
    FROM base_bal B
    INNER JOIN regras R ON B.CTACTB LIKE R.PADRAO_CTACTB
    LEFT JOIN excl E ON E.ORDEM = R.ORDEM AND B.CTACTB LIKE E.PADRAO_CTACTB
    WHERE E.PADRAO_CTACTB IS NULL
),
vals AS (
    SELECT R.ORDEM,
           SUM(IFNULL(M.valor_atu, 0) * M.SINAL) AS V_ATU,
           SUM(IFNULL(M.valor_ant, 0) * M.SINAL) AS V_ANT
    FROM (SELECT DISTINCT ORDEM, NOME_GRUPO FROM regras) R
    LEFT JOIN contas_mapeadas M
           ON M.ORDEM = R.ORDEM AND M.NOME_GRUPO = R.NOME_GRUPO AND M.rn = 1
    GROUP BY R.ORDEM
)
SELECT
    IFNULL(SUM(CASE WHEN ORDEM LIKE '1.1.%'       THEN V_ATU END), 0) AS AC_ATU,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '2.1.%'       THEN V_ATU END), 0) AS PC_ATU,
    IFNULL(SUM(CASE WHEN TRIM(ORDEM) = '1.1.3'    THEN V_ATU END), 0) AS ESTOQUE_ATU,
    IFNULL(SUM(CASE WHEN TRIM(ORDEM) = '1.2.1'    THEN V_ATU END), 0) AS RLP_ATU,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '2.2.%'       THEN V_ATU END), 0) AS PNC_ATU,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '1.%'         THEN V_ATU END), 0) AS ATIVO_TOTAL_ATU,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '1.1.%'       THEN V_ANT END), 0) AS AC_ANT,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '2.1.%'       THEN V_ANT END), 0) AS PC_ANT,
    IFNULL(SUM(CASE WHEN TRIM(ORDEM) = '1.1.3'    THEN V_ANT END), 0) AS ESTOQUE_ANT,
    IFNULL(SUM(CASE WHEN TRIM(ORDEM) = '1.2.1'    THEN V_ANT END), 0) AS RLP_ANT,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '2.2.%'       THEN V_ANT END), 0) AS PNC_ANT,
    IFNULL(SUM(CASE WHEN ORDEM LIKE '1.%'         THEN V_ANT END), 0) AS ATIVO_TOTAL_ANT,
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
FROM vals;
