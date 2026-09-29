/* =====================================================================
   index10 - Base de variaveis dos indicadores - DRE (queryDadosDRE)
   tenant_61302 | DET_DRE_TW.ID_ESTR_DRE_TW = 9 ("DRE da estrutura 9")

   Parametros do componente (os mesmos das telas index1 a index9):
       :VAR_DATA_INICIO   data inicial do filtro
       :VAR_DATA_FIM      data final   do filtro
       :VAR_EMPRESA_DRE   CODEMP

   ATENCAO: nao use comentario de linha (--) nesta query. O componente
   grava tudo numa unica linha, e um -- comentaria todo o resto.

   ---------------------------------------------------------------------
   FONTE: BALANCETE (IMP_BASE_BALANCETE), como as telas index1 a index9
   ---------------------------------------------------------------------
   A versao anterior lia DRE_TECWAY. Essa tabela so tem dados ate 04/2026 e so das empresas
   6, 7 e 999.
   Agora os valores saem do balancete, pelo MESMO vinculo de contas que
   ja existia (CAD_CONTA_DRE_TW -> DET_DRE_TW), em valor cheio (a tabela
   antiga guardava em milhares).

   MESES: os 12 meses do ano da data fim. So os meses que tocam o
   periodo filtrado tem valor; os outros vem NULL.

   VALOR DO MES = MOVIMENTO do mes (so a parte do mes dentro do
   periodo). Sem SINAL: no balancete receita ja vem positiva e despesa
   negativa, que e o que a tela espera (ela nao aplica sinal na DRE).
   As linhas 6.1/6.2 (adicoes/exclusoes) repetem contas de outras linhas
   de proposito - a tela usa essas duas so no calculo do imposto.
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
        LEAST(DT_INI, DT_FIM)          AS DT_INI,
        GREATEST(DT_INI, DT_FIM)       AS DT_FIM,
        YEAR(GREATEST(DT_INI, DT_FIM)) AS ANO
    FROM (
        SELECT
            COALESCE(DT_FIM_IN, CURDATE())                              AS DT_FIM,
            COALESCE(DT_INI_IN,
                     MAKEDATE(YEAR(COALESCE(DT_FIM_IN, CURDATE())), 1)) AS DT_INI
        FROM PARAMS
    ) X
),
N12 AS (
    SELECT 1 AS N UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
    UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
),
M AS (
    /* os 12 meses do ano da data fim. ATIVO = 1 so para os meses que
       tocam o periodo filtrado; os outros saem NULL (celula vazia).
       DE/ATE = o pedaco do mes que esta dentro do periodo. */
    SELECT
        N,
        CASE WHEN LAST_DAY(MAKEDATE(ANO, 1) + INTERVAL (N - 1) MONTH) >= DT_INI
              AND MAKEDATE(ANO, 1) + INTERVAL (N - 1) MONTH <= DT_FIM
             THEN 1 ELSE 0 END                                                AS ATIVO,
        GREATEST(MAKEDATE(ANO, 1) + INTERVAL (N - 1) MONTH, DT_INI)          AS DE,
        LEAST(LAST_DAY(MAKEDATE(ANO, 1) + INTERVAL (N - 1) MONTH), DT_FIM)   AS ATE
    FROM N12 CROSS JOIN D
),
vinc AS (
    SELECT DISTINCT d.ID, TRIM(c.ID_CONTA_CONTABIL) AS CONTA
    FROM DET_DRE_TW d
    INNER JOIN CAD_CONTA_DRE_TW c ON c.ID_DET_DRE_TW = d.ID
    WHERE d.ID_ESTR_DRE_TW = 9
),
movs AS (
    SELECT B.CTACTB, M.N, SUM(B.VLRLANC) AS V
    FROM IMP_BASE_BALANCETE B
    INNER JOIN M ON M.ATIVO = 1
                AND DATE(B.REFERENCIA) BETWEEN M.DE AND M.ATE
    WHERE B.CODEMP = :VAR_EMPRESA_DRE
    GROUP BY B.CTACTB, M.N
),
LM AS (
    SELECT v.ID, s.N, SUM(s.V) AS V
    FROM vinc v
    INNER JOIN movs s ON s.CTACTB = v.CONTA
    GROUP BY v.ID, s.N
)
SELECT
    d.ORDEM,
    d.HIERARQUIA,
    d.DESCRICAO,
    CASE WHEN (SELECT ATIVO FROM M WHERE N = 1) = 1
         THEN IFNULL(SUM(CASE WHEN LM.N = 1 THEN LM.V END), 0) END AS VALOR_JAN,
    CASE WHEN (SELECT ATIVO FROM M WHERE N = 2) = 1
         THEN IFNULL(SUM(CASE WHEN LM.N = 2 THEN LM.V END), 0) END AS VALOR_FEV,
    CASE WHEN (SELECT ATIVO FROM M WHERE N = 3) = 1
         THEN IFNULL(SUM(CASE WHEN LM.N = 3 THEN LM.V END), 0) END AS VALOR_MAR,
    CASE WHEN (SELECT ATIVO FROM M WHERE N = 4) = 1
         THEN IFNULL(SUM(CASE WHEN LM.N = 4 THEN LM.V END), 0) END AS VALOR_ABR,
    CASE WHEN (SELECT ATIVO FROM M WHERE N = 5) = 1
         THEN IFNULL(SUM(CASE WHEN LM.N = 5 THEN LM.V END), 0) END AS VALOR_MAI,
    CASE WHEN (SELECT ATIVO FROM M WHERE N = 6) = 1
         THEN IFNULL(SUM(CASE WHEN LM.N = 6 THEN LM.V END), 0) END AS VALOR_JUN,
    CASE WHEN (SELECT ATIVO FROM M WHERE N = 7) = 1
         THEN IFNULL(SUM(CASE WHEN LM.N = 7 THEN LM.V END), 0) END AS VALOR_JUL,
    CASE WHEN (SELECT ATIVO FROM M WHERE N = 8) = 1
         THEN IFNULL(SUM(CASE WHEN LM.N = 8 THEN LM.V END), 0) END AS VALOR_AGO,
    CASE WHEN (SELECT ATIVO FROM M WHERE N = 9) = 1
         THEN IFNULL(SUM(CASE WHEN LM.N = 9 THEN LM.V END), 0) END AS VALOR_SET,
    CASE WHEN (SELECT ATIVO FROM M WHERE N = 10) = 1
         THEN IFNULL(SUM(CASE WHEN LM.N = 10 THEN LM.V END), 0) END AS VALOR_OUT,
    CASE WHEN (SELECT ATIVO FROM M WHERE N = 11) = 1
         THEN IFNULL(SUM(CASE WHEN LM.N = 11 THEN LM.V END), 0) END AS VALOR_NOV,
    CASE WHEN (SELECT ATIVO FROM M WHERE N = 12) = 1
         THEN IFNULL(SUM(CASE WHEN LM.N = 12 THEN LM.V END), 0) END AS VALOR_DEZ,
    (SELECT DT_INI FROM D)                                   AS DATA_INICIO_USADA,
    (SELECT DT_FIM FROM D)                                   AS DATA_FIM_USADA,
    (SELECT ANO    FROM D)                                   AS ANO_ATU,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE)                       AS LANC_EMPRESA,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE
        AND DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                 AND (SELECT DT_FIM FROM D)) AS LANC_PERIODO,
    CASE WHEN (SELECT DT_INI_IN FROM PARAMS) IS NULL
            OR (SELECT DT_FIM_IN FROM PARAMS) IS NULL
         THEN 0 ELSE 1 END                                   AS DATAS_OK
FROM DET_DRE_TW d
LEFT JOIN LM ON LM.ID = d.ID
WHERE d.ID_ESTR_DRE_TW = 9
GROUP BY d.ID, d.ORDEM, d.HIERARQUIA, d.DESCRICAO
ORDER BY d.ORDEM;
