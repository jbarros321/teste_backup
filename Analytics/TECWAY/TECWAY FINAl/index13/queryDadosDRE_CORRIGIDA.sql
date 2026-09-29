/* =====================================================================
   index10 e index13 - Indicadores (mesma query nas duas telas) - DRE (queryDadosDRE)
   tenant_61302 | DET_DRE_TW.ID_ESTR_DRE_TW = 9 ("DRE da estrutura 9")

   Parametros do componente (os mesmos das telas index1 a index9):
       :VAR_DATA_INICIO   data inicial do filtro
       :VAR_DATA_FIM      data final   do filtro
       :VAR_EMPRESA_DRE   CODEMP

   ATENCAO: nao use comentario de linha (--) nesta query. O componente
   grava tudo numa unica linha, e um -- comentaria todo o resto.

   ---------------------------------------------------------------------
   FONTE: DRE_TECWAY (mantida, por decisao de 29/09/2026)
   ---------------------------------------------------------------------
   A DRE_TECWAY guarda, em cada mes, o ACUMULADO DO ANO ate aquele mes,
   em milhares (x1000 = reais). Em dezembro bate ao centavo com o
   resultado do ano no balancete (CODEMP 999: 2025 = 911.300,74). O
   balancete de 2022-2025 so tem o resultado em dezembro, por isso a DRE
   mes a mes continua nesta tabela. Limitacao: vai ate 04/2026 e so tem
   as empresas 6, 7 e 999.

   MESES: os 12 meses do ano da data fim; so os que tocam o periodo
   filtrado tem valor, os outros vem NULL. Cada coluna e o acumulado de
   janeiro ate aquele mes (e o que a tabela guarda).

   O QUE MUDOU EM RELACAO A ORIGINAL: datas pelo parser das outras telas
   (a original quebrava com 'DD/MM/AAAA HH:MM:SS'); :VAR_EMPRESA (lista)
   virou :VAR_EMPRESA_DRE; vinculos com DISTINCT + TRIM; diagnostico
   (DATAS_OK etc.). Sem SINAL: receita positiva e despesa negativa, que e
   o que a tela espera. As linhas 6.1/6.2 (adicoes/exclusoes) repetem
   contas de outras linhas de proposito.
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
    SELECT TRIM(t.ID_CONTA_CONTABIL) AS CTACTB, M.N,
           SUM(COALESCE(t.DRE_TECWAY, 0)) * 1000 AS V
    FROM DRE_TECWAY t
    INNER JOIN M ON M.ATIVO = 1
                AND t.MES = DATE_FORMAT(M.DE, '%m/%Y')
    WHERE t.ID_EMPRESA = :VAR_EMPRESA_DRE
    GROUP BY TRIM(t.ID_CONTA_CONTABIL), M.N
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
    (SELECT COUNT(*) FROM DRE_TECWAY
      WHERE ID_EMPRESA = :VAR_EMPRESA_DRE)                   AS LANC_EMPRESA,
    (SELECT COUNT(*) FROM DRE_TECWAY t2
      INNER JOIN M ON M.ATIVO = 1 AND t2.MES = DATE_FORMAT(M.DE, '%m/%Y')
      WHERE t2.ID_EMPRESA = :VAR_EMPRESA_DRE)                AS LANC_PERIODO,
    (SELECT DATE_FORMAT(MAX(STR_TO_DATE(CONCAT('01/', MES), '%d/%m/%Y')), '%m/%Y')
       FROM DRE_TECWAY WHERE ID_EMPRESA = :VAR_EMPRESA_DRE)  AS ULTIMO_MES_CARGA,
    CASE WHEN (SELECT DT_INI_IN FROM PARAMS) IS NULL
            OR (SELECT DT_FIM_IN FROM PARAMS) IS NULL
         THEN 0 ELSE 1 END                                   AS DATAS_OK
FROM DET_DRE_TW d
LEFT JOIN LM ON LM.ID = d.ID
WHERE d.ID_ESTR_DRE_TW = 9
GROUP BY d.ID, d.ORDEM, d.HIERARQUIA, d.DESCRICAO
ORDER BY d.ORDEM;
