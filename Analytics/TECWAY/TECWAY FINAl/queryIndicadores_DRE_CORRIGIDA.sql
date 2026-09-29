/* =====================================================================
   INDICADORES - DRE  (era queryDadosDREindicadores.sql)
   Migrado de :VAR_MES para o filtro de PERIODO da tela.
   tenant_61302 | ESTR_DRE_TW.ID = 9 ("DRE Padrao Tecway - Interno")

   Parametros do componente:
       :VAR_DATA_INICIO   data inicial do filtro da tela
       :VAR_DATA_FIM      data final   do filtro da tela
       :VAR_EMPRESA       lista de empresas (FIND_IN_SET), vazio = todas

   NAO use comentario de linha (--): o componente achata a query numa
   unica linha e um -- comentaria todo o resto. So comentario de bloco.

   ---------------------------------------------------------------------
   O QUE MUDOU
   ---------------------------------------------------------------------
   1) O ANO saia de :VAR_MES ('MM/AAAA'); agora sai de :VAR_DATA_FIM.
      Com :VAR_MES vazio, STR_TO_DATE(CONCAT('01/', NULL)) dava NULL, o
      ano virava NULL e as 12 colunas vinham todas em branco. Agora o
      filtro vazio nao zera: sem data fim usa hoje, sem data inicio usa
      01/01 do ano da data fim.

   2) O PERIODO passou a limitar os meses. Antes a query montava sempre
      os 12 meses do ano inteiro, ignorando qualquer recorte. Agora so
      os meses que intersectam [:VAR_DATA_INICIO, :VAR_DATA_FIM] entram;
      os de fora ficam NULL, e a tela mostra celula vazia em vez de um
      numero que o filtro nao pediu.

   3) A NOTA explicativa vinha de `DET_NOTAS.MES = :VAR_MES`. Como a nota
      e de um mes so, agora usa o mes da DATA FIM, que e o fechamento do
      periodo escolhido.

   4) TRIM no codigo da conta. 10 vinculos do CAD_CONTA_DRE_TW estao
      gravados com espaco a esquerda (2 contas, estruturas 1 e 5) e nunca
      casavam com a tabela de valores. Nao afeta as estruturas 7 e 9, mas
      custa nada e evita perda silenciosa de conta.

   ---------------------------------------------------------------------
   LIMITES DA CARGA (conferido em 28/09/2026)
   ---------------------------------------------------------------------
   DRE_TECWAY tem 21.034 linhas, mas:
       - a cobertura de 2026 para no mes 04. Um filtro que va alem de
         30/04/2026 traz MAI a DEZ em branco - e carga, nao query.
       - so existem as empresas 6, 7 e 999. As outras 11 que aparecem no
         IMP_BASE_BALANCETE nao tem linha aqui, e vem tudo zerado.
       - a DRE_TECWAY nao e derivavel do IMP_BASE_BALANCETE: testei mes a
         mes e acumulado no ano, e so 7,5% dos 8.102 casos casam. Sao duas
         cargas independentes, entao os indicadores e o BP nao conferem
         entre si por construcao.
   ===================================================================== */

WITH RECURSIVE PARAMS AS (
    SELECT
        /* aceita 'AAAA-MM-DD' e 'DD/MM/AAAA' */
        CASE WHEN TRIM(COALESCE(:VAR_DATA_INICIO, '')) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
             THEN STR_TO_DATE(TRIM(:VAR_DATA_INICIO), '%d/%m/%Y')
             ELSE DATE(NULLIF(TRIM(COALESCE(:VAR_DATA_INICIO, '')), ''))
        END AS DT_INI_IN,
        CASE WHEN TRIM(COALESCE(:VAR_DATA_FIM, '')) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
             THEN STR_TO_DATE(TRIM(:VAR_DATA_FIM), '%d/%m/%Y')
             ELSE DATE(NULLIF(TRIM(COALESCE(:VAR_DATA_FIM, '')), ''))
        END AS DT_FIM_IN
),
P AS (
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
MESES AS (
    SELECT 1 AS NUM_MES
    UNION ALL
    SELECT NUM_MES + 1 FROM MESES WHERE NUM_MES < 12
),
PERIODOS AS (
    /* so os meses do ano que intersectam o periodo filtrado */
    SELECT
        LPAD(m.NUM_MES, 2, '0')                              AS MES_NUM,
        CONCAT(LPAD(m.NUM_MES, 2, '0'), '/', p.ANO)          AS PERIODO
    FROM MESES m
    CROSS JOIN P p
    WHERE LAST_DAY(MAKEDATE(p.ANO, 1) + INTERVAL (m.NUM_MES - 1) MONTH) >= p.DT_INI
      AND          MAKEDATE(p.ANO, 1) + INTERVAL (m.NUM_MES - 1) MONTH  <= p.DT_FIM
),
VALORES AS (
    SELECT
        per.PERIODO,
        d.ORDEM,
        d.HIERARQUIA,
        d.DESCRICAO,
        n.NOTA,
        /* DRE_TECWAY guarda o valor em milhares: o x1000 confere contra o
           balancete (mediana da razao = 1000,0000 nas contas casadas) */
        SUM(COALESCE(t.DRE_TECWAY, 0)) * 1000 AS VALOR
    FROM DET_DRE_TW d
    CROSS JOIN PERIODOS per
    LEFT JOIN CAD_CONTA_DRE_TW c
           ON c.ID_DET_DRE_TW = d.ID
    LEFT JOIN DRE_TECWAY t
           /* TRIM nos dois lados: 10 vinculos do CAD_CONTA_DRE_TW tem
              espaco sobrando no codigo da conta (estruturas 1 e 5), e
              com igualdade crua eles nunca casam. */
           ON TRIM(t.ID_CONTA_CONTABIL) = TRIM(c.ID_CONTA_CONTABIL)
          AND t.MES = per.PERIODO
          AND ( :VAR_EMPRESA IS NULL OR :VAR_EMPRESA = ''
                OR FIND_IN_SET(t.ID_EMPRESA, :VAR_EMPRESA) )
    LEFT JOIN (
        SELECT ID_DET_DRE_TW, MAX(NOTA) AS NOTA
        FROM DET_NOTAS
        WHERE MES = (SELECT DATE_FORMAT(DT_FIM, '%m/%Y') FROM P)
          AND ( :VAR_EMPRESA IS NULL OR :VAR_EMPRESA = ''
                OR FIND_IN_SET(EMPRESA, :VAR_EMPRESA) )
        GROUP BY ID_DET_DRE_TW
    ) n ON n.ID_DET_DRE_TW = d.ID
    WHERE d.ID_ESTR_DRE_TW = 9
    GROUP BY per.PERIODO, d.ORDEM, d.HIERARQUIA, d.DESCRICAO, n.NOTA
)
SELECT
    ORDEM,
    HIERARQUIA,
    DESCRICAO,
    NOTA,
    MAX(CASE WHEN PERIODO LIKE '01/%' THEN VALOR END) AS VALOR_JAN,
    MAX(CASE WHEN PERIODO LIKE '02/%' THEN VALOR END) AS VALOR_FEV,
    MAX(CASE WHEN PERIODO LIKE '03/%' THEN VALOR END) AS VALOR_MAR,
    MAX(CASE WHEN PERIODO LIKE '04/%' THEN VALOR END) AS VALOR_ABR,
    MAX(CASE WHEN PERIODO LIKE '05/%' THEN VALOR END) AS VALOR_MAI,
    MAX(CASE WHEN PERIODO LIKE '06/%' THEN VALOR END) AS VALOR_JUN,
    MAX(CASE WHEN PERIODO LIKE '07/%' THEN VALOR END) AS VALOR_JUL,
    MAX(CASE WHEN PERIODO LIKE '08/%' THEN VALOR END) AS VALOR_AGO,
    MAX(CASE WHEN PERIODO LIKE '09/%' THEN VALOR END) AS VALOR_SET,
    MAX(CASE WHEN PERIODO LIKE '10/%' THEN VALOR END) AS VALOR_OUT,
    MAX(CASE WHEN PERIODO LIKE '11/%' THEN VALOR END) AS VALOR_NOV,
    MAX(CASE WHEN PERIODO LIKE '12/%' THEN VALOR END) AS VALOR_DEZ,
    /* o periodo que a query realmente usou, para a tela mostrar */
    (SELECT DT_INI FROM P) AS DATA_INICIO_USADA,
    (SELECT DT_FIM FROM P) AS DATA_FIM_USADA
FROM VALORES
GROUP BY ORDEM, HIERARQUIA, DESCRICAO, NOTA
ORDER BY ORDEM;
