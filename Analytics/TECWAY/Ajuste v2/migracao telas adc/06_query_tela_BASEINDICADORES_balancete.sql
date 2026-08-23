/* =====================================================================
   06 - QUERY DA TELA BASEINDICADORES (estrutura ESTR_DRE_TW.ID = 9)
   Versao adaptada para o projeto de destino (tenant_47255).

   Diferenca para a query original (queryDadosDREindicadores.sql):
   as tabelas DRE_TECWAY / BP_TECWAY nao existem aqui - os valores saem de
   IMP_BASE_BALANCETE atraves das CTEs BASE_DRE / BASE_BP abaixo, que
   devolvem exatamente o mesmo formato (MES 'MM/AAAA', ID_EMPRESA,
   ID_CONTA_CONTABIL, valor em MILHARES). O resto da query e identico.

   BASE_DRE = movimento do mes (VLRLANC do proprio mes).r
   BASE_BP  = saldo do mes: contas 1/2 acumulam todo o historico ate o mes;
              contas 3/4/5/6 acumulam apenas o ano corrente e entram com
              sinal invertido (mesma convencao da BP_TECWAY de origem).

   ATENCAO: neste tenant as contas de resultado (3/4/5/6) so tem movimento
   em dezembro. Nas telas de DRE/BaseIndicadores as colunas de janeiro a
   novembro sairao zeradas ate que o balancete mensal dessas contas seja
   importado aqui. Ver MAPEAMENTO.md, item 6.
   ===================================================================== */

WITH RECURSIVE PARAMS AS (     SELECT         :VAR_MES AS MES_REFERENCIA,         YEAR(STR_TO_DATE(CONCAT('01/', :VAR_MES), '%d/%m/%Y')) AS ANO_REFERENCIA ),  MESES AS (     SELECT 1 AS NUM_MES     UNION ALL     SELECT NUM_MES + 1     FROM MESES     WHERE NUM_MES < 12 ),  PERIODOS AS (     SELECT         LPAD(m.NUM_MES, 2, '0') AS MES_NUM,         CONCAT(LPAD(m.NUM_MES, 2, '0'), '/', p.ANO_REFERENCIA) AS PERIODO     FROM MESES m     CROSS JOIN PARAMS p ), BASE_DRE AS (
    SELECT
        DATE_FORMAT(b.REFERENCIA, '%m/%Y') AS MES,
        b.CODEMP                           AS ID_EMPRESA,
        b.CTACTB                           AS ID_CONTA_CONTABIL,
        SUM(b.VLRLANC) / 1000              AS DRE_TECWAY
    FROM IMP_BASE_BALANCETE b
    GROUP BY
        DATE_FORMAT(b.REFERENCIA, '%m/%Y'),
        b.CODEMP,
        b.CTACTB
),  VALORES AS (     SELECT         per.PERIODO,         d.ORDEM,         d.HIERARQUIA,         d.descricao,         n.NOTA,         SUM(COALESCE(t.DRE_TECWAY, 0)) * 1000 AS VALOR     FROM DET_DRE_TW d     CROSS JOIN PERIODOS per     LEFT JOIN CAD_CONTA_DRE_TW c         ON c.ID_DET_DRE_TW = d.ID     LEFT JOIN BASE_DRE t         ON t.ID_CONTA_CONTABIL = c.ID_CONTA_CONTABIL        AND t.MES = per.PERIODO        AND (             :VAR_EMPRESA IS NULL             OR :VAR_EMPRESA = ''             OR FIND_IN_SET(t.ID_EMPRESA, :VAR_EMPRESA)        )     LEFT JOIN (         SELECT             ID_DET_DRE_TW,             MAX(NOTA) AS NOTA         FROM DET_NOTAS         WHERE MES = :VAR_MES           AND (                 :VAR_EMPRESA IS NULL                 OR :VAR_EMPRESA = ''                 OR FIND_IN_SET(EMPRESA, :VAR_EMPRESA)           )         GROUP BY ID_DET_DRE_TW     ) n         ON n.ID_DET_DRE_TW = d.ID     WHERE d.ID_ESTR_DRE_TW = 9     GROUP BY         per.PERIODO,         d.ORDEM,         d.HIERARQUIA,         d.descricao,         n.NOTA )  SELECT     ORDEM,     HIERARQUIA,     descricao AS DESCRICAO,     NOTA,     MAX(CASE WHEN PERIODO LIKE '01/%' THEN VALOR END) AS VALOR_JAN,     MAX(CASE WHEN PERIODO LIKE '02/%' THEN VALOR END) AS VALOR_FEV,     MAX(CASE WHEN PERIODO LIKE '03/%' THEN VALOR END) AS VALOR_MAR,     MAX(CASE WHEN PERIODO LIKE '04/%' THEN VALOR END) AS VALOR_ABR,     MAX(CASE WHEN PERIODO LIKE '05/%' THEN VALOR END) AS VALOR_MAI,     MAX(CASE WHEN PERIODO LIKE '06/%' THEN VALOR END) AS VALOR_JUN,     MAX(CASE WHEN PERIODO LIKE '07/%' THEN VALOR END) AS VALOR_JUL,     MAX(CASE WHEN PERIODO LIKE '08/%' THEN VALOR END) AS VALOR_AGO,     MAX(CASE WHEN PERIODO LIKE '09/%' THEN VALOR END) AS VALOR_SET,     MAX(CASE WHEN PERIODO LIKE '10/%' THEN VALOR END) AS VALOR_OUT,     MAX(CASE WHEN PERIODO LIKE '11/%' THEN VALOR END) AS VALOR_NOV,     MAX(CASE WHEN PERIODO LIKE '12/%' THEN VALOR END) AS VALOR_DEZ FROM VALORES GROUP BY     ORDEM,     HIERARQUIA,     descricao,     NOTA ORDER BY ORDEM
