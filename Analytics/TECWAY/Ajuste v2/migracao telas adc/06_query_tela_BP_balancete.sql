/* =====================================================================
   06 - QUERY DA TELA BP (estrutura ESTR_DRE_TW.ID = 5)
   Versao adaptada para o projeto de destino (tenant_47255).

   Diferenca para a query original (bp_querydados.sql):
   as tabelas DRE_TECWAY / BP_TECWAY nao existem aqui - os valores saem de
   IMP_BASE_BALANCETE atraves das CTEs BASE_DRE / BASE_BP abaixo, que
   devolvem exatamente o mesmo formato (MES 'MM/AAAA', ID_EMPRESA,
   ID_CONTA_CONTABIL, valor em MILHARES). O resto da query e identico.

   BASE_DRE = movimento do mes (VLRLANC do proprio mes).
   BASE_BP  = saldo do mes: contas 1/2 acumulam todo o historico ate o mes;
              contas 3/4/5/6 acumulam apenas o ano corrente e entram com
              sinal invertido (mesma convencao da BP_TECWAY de origem).

   ATENCAO: neste tenant as contas de resultado (3/4/5/6) so tem movimento
   em dezembro. Nas telas de DRE/BaseIndicadores as colunas de janeiro a
   novembro sairao zeradas ate que o balancete mensal dessas contas seja
   importado aqui. Ver MAPEAMENTO.md, item 6.
   ===================================================================== */

/* =========================   Demonstrativo mês a mês   - Parte da mesma base do query25.sql   - Gera 12 colunas (MES_12 = mais antigo, MES_01 = :VAR_MES)   ========================= */WITH RECURSIVE PARAMS AS (    SELECT        STR_TO_DATE(CONCAT('01/', :VAR_MES), '%d/%m/%Y') AS MES_REFERENCIA,        :VAR_EMPRESA AS EMPRESA),MESES AS (    SELECT        DATE_FORMAT(MES_REFERENCIA, '%m/%Y') AS MES_LABEL,        1 AS IDX    FROM PARAMS    UNION ALL    SELECT        DATE_FORMAT(            DATE_SUB(STR_TO_DATE(CONCAT('01/', MES_LABEL), '%d/%m/%Y'), INTERVAL 1 MONTH),            '%m/%Y'        ) AS MES_LABEL,        IDX + 1 AS IDX    FROM MESES    WHERE IDX < 12), BASE_BP AS (
    SELECT
        per.MES_LABEL  AS MES,
        b.CODEMP      AS ID_EMPRESA,
        b.CTACTB      AS ID_CONTA_CONTABIL,
        SUM(
            CASE WHEN LEFT(b.CTACTB, 1) IN ('1', '2')
                 THEN  b.VLRLANC
                 ELSE -b.VLRLANC
            END
        ) / 1000      AS BP_TECWAY
    FROM MESES per
    JOIN IMP_BASE_BALANCETE b
      ON DATE(b.REFERENCIA) <= LAST_DAY(STR_TO_DATE(CONCAT('01/', per.MES_LABEL), '%d/%m/%Y'))
     AND (
            LEFT(b.CTACTB, 1) IN ('1', '2')
            OR YEAR(b.REFERENCIA) = YEAR(STR_TO_DATE(CONCAT('01/', per.MES_LABEL), '%d/%m/%Y'))
         )
    GROUP BY
        per.MES_LABEL,
        b.CODEMP,
        b.CTACTB
),CALCULOS AS (    /* =========================       Valores por mês (contas base)       ========================= */    SELECT        m.MES_LABEL AS MES,        d.ID,        d.ORDEM,        d.DESCRICAO,        d.TIPO,        d.FORMATACAO,        d.HIERARQUIA,        d.ORDEM AS ORDEM_ORIGEM,        CASE            WHEN d.TIPO = 'CONTA'                THEN COALESCE(SUM(t.BP_TECWAY) * 1000, 0)            ELSE 0        END AS VALOR    FROM MESES m        CROSS JOIN PARAMS p        JOIN DET_DRE_TW d        LEFT JOIN CAD_CONTA_DRE_TW c            ON c.ID_DET_DRE_TW = d.ID        LEFT JOIN BASE_BP t            ON t.ID_CONTA_CONTABIL = c.ID_CONTA_CONTABIL           AND t.MES = m.MES_LABEL           AND (               p.EMPRESA IS NULL               OR p.EMPRESA = ''               OR FIND_IN_SET(t.ID_EMPRESA, p.EMPRESA)           )        LEFT JOIN ESTR_DRE_TW f            ON d.ID_ESTR_DRE_TW = f.ID    WHERE d.TIPO = 'CONTA'      AND f.TIPO_DEMONSTRATIVO = 'BP'      AND f.ID = 5    GROUP BY        m.MES_LABEL,        d.ID,        d.ORDEM,        d.DESCRICAO,        d.TIPO,        d.FORMATACAO,        d.HIERARQUIA    UNION ALL    /* =========================       Cálculos derivados por mês       ========================= */    SELECT        c.MES,        d.ID,        d.ORDEM,        d.DESCRICAO,        d.TIPO,        d.FORMATACAO,        d.HIERARQUIA,        c.ORDEM AS ORDEM_ORIGEM,        c.VALOR *        CASE            WHEN LOCATE(                     CONCAT('-', '[', c.ORDEM, ']'),                     REPLACE(d.CALCULO, ' ', '')                 ) > 0                THEN -1            ELSE 1        END AS VALOR    FROM DET_DRE_TW d        LEFT JOIN ESTR_DRE_TW f            ON d.ID_ESTR_DRE_TW = f.ID        JOIN CALCULOS c            ON FIND_IN_SET(                c.ORDEM,                REPLACE(                    REPLACE(                        REPLACE(                            REPLACE(                                REPLACE(d.CALCULO, ' ', ''),                                '[',                                ''                            ),                            ']',                            ''                        ),                        '+',                        ','                    ),                    '-',                    ','                )            )    WHERE d.TIPO = 'CALCULO'      AND f.ID = 5),RESULTADOS AS (    SELECT        MES,        ORDEM,        DESCRICAO,        FORMATACAO,        HIERARQUIA,        SUM(VALOR) AS VALOR    FROM CALCULOS    GROUP BY        MES,        ORDEM,        DESCRICAO,        FORMATACAO,        HIERARQUIA),AGREGADOS AS (    SELECT        MES,        MAX(CASE WHEN ORDEM = 1  THEN VALOR END) AS V1,        MAX(CASE WHEN ORDEM = 13 THEN VALOR END) AS V13,        MAX(CASE WHEN ORDEM = 17 THEN VALOR END) AS V17,        MAX(CASE WHEN ORDEM = 27 THEN VALOR END) AS V27,        MAX(CASE WHEN ORDEM = 28 THEN VALOR END) AS V28,        MAX(CASE WHEN ORDEM = 42 THEN VALOR END) AS V42    FROM RESULTADOS    GROUP BY MES),RESULTADOS_FINAIS AS (    SELECT * FROM RESULTADOS    UNION ALL    SELECT MES, 1001, 'LIQUIDEZ CORRENTE', NULL, '14.', COALESCE(V1, 0) / NULLIF(V28, 0) FROM AGREGADOS    UNION ALL    SELECT MES, 1002, 'LIQUIDEZ SECA', NULL, '15.', (COALESCE(V1, 0) - COALESCE(V13, 0)) / NULLIF(V28, 0) FROM AGREGADOS    UNION ALL    SELECT MES, 1003, 'LIQUIDEZ GERAL', NULL, '16.', (COALESCE(V1, 0) + COALESCE(V17, 0)) / NULLIF(COALESCE(V28, 0) + COALESCE(V42, 0), 0) FROM AGREGADOS    UNION ALL    SELECT MES, 1004, 'SOLVÊNCIA GERAL', NULL, '17.', COALESCE(V27, 0) / NULLIF(COALESCE(V28, 0) + COALESCE(V42, 0), 0) FROM AGREGADOS
)
SELECT    ORDEM,    DESCRICAO,    FORMATACAO,    HIERARQUIA,    /* MES_12 = mais antigo, MES_01 = :VAR_MES */    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 12) THEN VALOR END) AS MES_12,    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 11) THEN VALOR END) AS MES_11,    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 10) THEN VALOR END) AS MES_10,    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 9)  THEN VALOR END) AS MES_09,    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 8)  THEN VALOR END) AS MES_08,    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 7)  THEN VALOR END) AS MES_07,    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 6)  THEN VALOR END) AS MES_06,    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 5)  THEN VALOR END) AS MES_05,    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 4)  THEN VALOR END) AS MES_04,    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 3)  THEN VALOR END) AS MES_03,    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 2)  THEN VALOR END) AS MES_02,    MAX(CASE WHEN MES = (SELECT MES_LABEL FROM MESES WHERE IDX = 1)  THEN VALOR END) AS MES_01
FROM RESULTADOS_FINAIS
GROUP BY    ORDEM,    DESCRICAO,    FORMATACAO,    HIERARQUIA
ORDER BY ORDEM
