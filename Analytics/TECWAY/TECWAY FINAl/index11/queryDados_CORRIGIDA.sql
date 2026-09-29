/* =====================================================================
   index11 - Balanco mes a mes (BP Padrao Tecway) + indicadores
   tenant_61302 | DET_DRE_TW.ID_ESTR_DRE_TW = 5 ("BP Padrao Tecway")

   Parametros do componente (os mesmos das telas index1 a index10):
       :VAR_DATA_INICIO   data inicial do filtro
       :VAR_DATA_FIM      data final   do filtro
       :VAR_EMPRESA_DRE   CODEMP

   ATENCAO: nao use comentario de linha (--) nesta query. O componente
   grava tudo numa unica linha, e um -- comentaria todo o resto.

   ---------------------------------------------------------------------
   REGRA (mantida da versao original)
   ---------------------------------------------------------------------
   12 colunas: MES_01 = mes da data fim, MES_12 = 11 meses antes.
   Contas 1 e 2 -> SALDO acumulado ate o fim do mes.
   Contas 3, 4, 5 -> resultado do ano ate o fim do mes, com sinal
   invertido (entra no PL). Linhas TIPO = 'CALCULO' somam/subtraem as
   linhas citadas em [ORDEM] no campo CALCULO (recursivo).

   ---------------------------------------------------------------------
   O QUE MUDOU
   ---------------------------------------------------------------------
   1) DATAS. Saia de :VAR_MES ('MM/AAAA'); agora sai do filtro. O mes
      mais recente e o da data fim, e o saldo desse mes e cortado na
      propria data fim. Meses ANTERIORES a data inicio vem NULL (celula
      vazia). Datas pelo mesmo parser das outras telas.
   2) EMPRESA. Era :VAR_EMPRESA com FIND_IN_SET (vazio = todas), e o
      filtro so era aplicado DEPOIS de somar o balancete de todas as
      empresas nos 12 meses. Agora e CODEMP = :VAR_EMPRESA_DRE, aplicado
      na leitura do balancete, como nas outras telas.
   3) VINCULO DUPLICADO. A conta 1.1.01.01.000001 esta vinculada 2 vezes
      em Disponibilidades e era somada 2 vezes. Agora DISTINCT.
      Conferido (CODEMP 999, 31/12/2025): Disponibilidades = 1.897.772,38
      = saldo real de caixa (igual ao caixa final da DFC, index5).
      Sem TRIM de proposito: o unico vinculo com espaco nesta estrutura
      e ' 3.1.01.01.000004', uma conta de RECEITA cadastrada dentro de
      Disponibilidades - com TRIM ela passaria a tirar 558 mil do caixa.
      O vinculo esta errado e deve ser removido do cadastro.
   4) INDICADORES COM SINAL TROCADO. No balancete o passivo e credor
      (negativo); a divisao AC / PC dava liquidez NEGATIVA (-1,92 em
      12/2025). Agora o passivo entra com o sinal invertido.
   5) ORDEM 13 REPETIDA no cadastro ("Estoque" e uma linha "Realizavel a
      longo prazo" sem contas). O MAX(...) podia escolher a linha vazia;
      agora soma as duas (a vazia vale 0).

   PENDENCIA DE CADASTRO (nao e da query): em 31/12/2025, CODEMP 999, o
   ativo (98,4 mi) nao fecha com passivo + PL (80,8 mi): ha 17,6 mi em
   contas de passivo/PL fora da estrutura 5 (2.3.02.02 reservas 9,0 mi;
   2.1.01.12 7,9 mi; 2.3.04.01 0,9 mi ...).

   COLUNAS DEVOLVIDAS
       ORDEM, DESCRICAO, FORMATACAO, HIERARQUIA, MES_12 .. MES_01
       MES_REF (MM/AAAA da data fim), DATA_INICIO_USADA, DATA_FIM_USADA,
       LANC_EMPRESA, LANC_PERIODO, DATAS_OK
   ===================================================================== */

WITH RECURSIVE PARAMS AS (
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
        LEAST(DT_INI, DT_FIM)    AS DT_INI,
        GREATEST(DT_INI, DT_FIM) AS DT_FIM
    FROM (
        SELECT
            COALESCE(DT_FIM_IN, CURDATE())                              AS DT_FIM,
            COALESCE(DT_INI_IN,
                     MAKEDATE(YEAR(COALESCE(DT_FIM_IN, CURDATE())), 1)) AS DT_INI
        FROM PARAMS
    ) X
),
N12 AS (
    SELECT 1 AS IDX UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
    UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
),
MESES AS (
    /* IDX 1 = mes da data fim. CORTE = fim do mes, ou a data fim no mes
       mais recente. ATIVO = 0 para meses inteiros antes da data inicio. */
    SELECT
        N.IDX,
        DATE_FORMAT(DATE_SUB(DATE_FORMAT(D.DT_FIM, '%Y-%m-01'), INTERVAL (N.IDX - 1) MONTH), '%m/%Y') AS MES_LABEL,
        LEAST(LAST_DAY(DATE_SUB(DATE_FORMAT(D.DT_FIM, '%Y-%m-01'), INTERVAL (N.IDX - 1) MONTH)),
              D.DT_FIM)                                                                        AS CORTE,
        CASE WHEN LAST_DAY(DATE_SUB(DATE_FORMAT(D.DT_FIM, '%Y-%m-01'), INTERVAL (N.IDX - 1) MONTH))
                  >= D.DT_INI THEN 1 ELSE 0 END                                                AS ATIVO
    FROM N12 N CROSS JOIN D
),
VINC AS (
    /* DISTINCT: vinculo cadastrado 2 vezes nao soma 2 vezes */
    SELECT DISTINCT c.ID_DET_DRE_TW, c.ID_CONTA_CONTABIL
    FROM CAD_CONTA_DRE_TW c
),
BASE_BP AS (
    SELECT
        per.MES_LABEL AS MES,
        b.CTACTB      AS ID_CONTA_CONTABIL,
        SUM(CASE WHEN LEFT(b.CTACTB, 1) IN ('1', '2')
                 THEN  b.VLRLANC
                 ELSE -b.VLRLANC END) AS VALOR
    FROM MESES per
    INNER JOIN IMP_BASE_BALANCETE b
            ON DATE(b.REFERENCIA) <= per.CORTE
           AND ( LEFT(b.CTACTB, 1) IN ('1', '2')
                 OR YEAR(b.REFERENCIA) = YEAR(per.CORTE) )
    WHERE per.ATIVO = 1
      AND b.CODEMP = :VAR_EMPRESA_DRE
    GROUP BY per.MES_LABEL, b.CTACTB
),
CALCULOS AS (
    /* valores por mes das linhas CONTA */
    SELECT
        m.MES_LABEL AS MES,
        d.ID,
        d.ORDEM,
        d.DESCRICAO,
        d.TIPO,
        d.FORMATACAO,
        d.HIERARQUIA,
        d.ORDEM AS ORDEM_ORIGEM,
        COALESCE(SUM(t.VALOR), 0) AS VALOR
    FROM MESES m
    INNER JOIN DET_DRE_TW d ON d.ID_ESTR_DRE_TW = 5 AND d.TIPO = 'CONTA'
    LEFT JOIN VINC c ON c.ID_DET_DRE_TW = d.ID
    LEFT JOIN BASE_BP t
           ON t.ID_CONTA_CONTABIL = c.ID_CONTA_CONTABIL
          AND t.MES = m.MES_LABEL
    WHERE m.ATIVO = 1
    GROUP BY m.MES_LABEL, d.ID, d.ORDEM, d.DESCRICAO, d.TIPO, d.FORMATACAO, d.HIERARQUIA

    UNION ALL

    /* linhas CALCULO: soma/subtrai as linhas citadas em [ORDEM] */
    SELECT
        c.MES,
        d.ID,
        d.ORDEM,
        d.DESCRICAO,
        d.TIPO,
        d.FORMATACAO,
        d.HIERARQUIA,
        c.ORDEM AS ORDEM_ORIGEM,
        c.VALOR *
        CASE WHEN LOCATE(CONCAT('-', '[', c.ORDEM, ']'), REPLACE(d.CALCULO, ' ', '')) > 0
             THEN -1 ELSE 1 END AS VALOR
    FROM DET_DRE_TW d
    INNER JOIN CALCULOS c
            ON FIND_IN_SET(
                   c.ORDEM,
                   REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                       d.CALCULO, ' ', ''), '[', ''), ']', ''), '+', ','), '-', ',')
               )
    WHERE d.TIPO = 'CALCULO'
      AND d.ID_ESTR_DRE_TW = 5
),
RESULTADOS AS (
    SELECT MES, ORDEM, DESCRICAO, FORMATACAO, HIERARQUIA, SUM(VALOR) AS VALOR
    FROM CALCULOS
    GROUP BY MES, ORDEM, DESCRICAO, FORMATACAO, HIERARQUIA
),
AGREGADOS AS (
    /* SUM e nao MAX: a ORDEM 13 esta repetida no cadastro */
    SELECT
        MES,
        SUM(CASE WHEN ORDEM = 1  THEN VALOR END) AS V1,
        SUM(CASE WHEN ORDEM = 13 THEN VALOR END) AS V13,
        SUM(CASE WHEN ORDEM = 17 THEN VALOR END) AS V17,
        SUM(CASE WHEN ORDEM = 27 THEN VALOR END) AS V27,
        SUM(CASE WHEN ORDEM = 28 THEN VALOR END) AS V28,
        SUM(CASE WHEN ORDEM = 42 THEN VALOR END) AS V42
    FROM RESULTADOS
    GROUP BY MES
),
RESULTADOS_FINAIS AS (
    SELECT * FROM RESULTADOS
    /* passivo e credor (negativo) no balancete: entra com sinal
       invertido, senao a liquidez sai negativa */
    UNION ALL
    SELECT MES, 1001, 'LIQUIDEZ CORRENTE', NULL, '14.',
           COALESCE(V1, 0) / NULLIF(-COALESCE(V28, 0), 0) FROM AGREGADOS
    UNION ALL
    SELECT MES, 1002, 'LIQUIDEZ SECA', NULL, '15.',
           (COALESCE(V1, 0) - COALESCE(V13, 0)) / NULLIF(-COALESCE(V28, 0), 0) FROM AGREGADOS
    UNION ALL
    SELECT MES, 1003, 'LIQUIDEZ GERAL', NULL, '16.',
           (COALESCE(V1, 0) + COALESCE(V17, 0))
           / NULLIF(-(COALESCE(V28, 0) + COALESCE(V42, 0)), 0) FROM AGREGADOS
    UNION ALL
    SELECT MES, 1004, 'SOLVÊNCIA GERAL', NULL, '17.',
           COALESCE(V27, 0)
           / NULLIF(-(COALESCE(V28, 0) + COALESCE(V42, 0)), 0) FROM AGREGADOS
)
SELECT
    ORDEM,
    DESCRICAO,
    FORMATACAO,
    HIERARQUIA,
    /* MES_12 = mais antigo, MES_01 = mes da data fim */
    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 12)  THEN VALOR END) AS MES_12,
    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 11)  THEN VALOR END) AS MES_11,
    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 10)  THEN VALOR END) AS MES_10,
    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 9)  THEN VALOR END) AS MES_09,
    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 8)  THEN VALOR END) AS MES_08,
    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 7)  THEN VALOR END) AS MES_07,
    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 6)  THEN VALOR END) AS MES_06,
    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 5)  THEN VALOR END) AS MES_05,
    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 4)  THEN VALOR END) AS MES_04,
    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 3)  THEN VALOR END) AS MES_03,
    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 2)  THEN VALOR END) AS MES_02,
    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 1)  THEN VALOR END) AS MES_01,
    (SELECT DATE_FORMAT(DT_FIM, '%m/%Y') FROM D)             AS MES_REF,
    (SELECT DT_INI FROM D)                                   AS DATA_INICIO_USADA,
    (SELECT DT_FIM FROM D)                                   AS DATA_FIM_USADA,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE)                       AS LANC_EMPRESA,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE
        AND DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                 AND (SELECT DT_FIM FROM D)) AS LANC_PERIODO,
    CASE WHEN (SELECT DT_INI_IN FROM PARAMS) IS NULL
            OR (SELECT DT_FIM_IN FROM PARAMS) IS NULL
         THEN 0 ELSE 1 END                                   AS DATAS_OK
FROM RESULTADOS_FINAIS
GROUP BY ORDEM, DESCRICAO, FORMATACAO, HIERARQUIA
ORDER BY ORDEM;
