/* =====================================================================
   index12 - DRE mes a mes (DRE Padrao Tecway) - queryDRE
   tenant_61302 | DET_DRE_TW.ID_ESTR_DRE_TW = 1

   Parametros do componente (os mesmos das telas index1 a index11):
       :VAR_DATA_INICIO   data inicial do filtro
       :VAR_DATA_FIM      data final   do filtro
       :VAR_EMPRESA_DRE   CODEMP

   ATENCAO: nao use comentario de linha (--) nesta query. O componente
   grava tudo numa unica linha, e um -- comentaria todo o resto.

   ---------------------------------------------------------------------
   FONTE: DRE_TECWAY (mantida, por decisao de 29/09/2026)
   ---------------------------------------------------------------------
   A DRE_TECWAY guarda, em cada mes, o ACUMULADO DO ANO ate aquele mes
   (em milhares; x1000 = reais). Em dezembro bate ao centavo com o
   resultado do ano no balancete (CODEMP 999: 2025 = 911.300,74;
   2024 = 6.500.333,81). O balancete de 2022-2025 so tem o resultado em
   dezembro, por isso a DRE mes a mes continua nesta tabela.
   Limitacao: a DRE_TECWAY vai ate 04/2026 e so tem as empresas 6, 7 e
   999. A query devolve ULTIMO_MES_CARGA e a tela avisa.

   ---------------------------------------------------------------------
   O QUE MUDOU (a logica de calculo e de impostos CALCULO4..13 nao mudou)
   ---------------------------------------------------------------------
   1) DATAS. Saia de :VAR_MES ('MM/AAAA'); agora sai do filtro: ano da
      data fim, e so os meses que tocam o periodo (os outros ficam
      NULL). Como a tabela e acumulada no ano, cada coluna continua sendo
      o acumulado de janeiro ate aquele mes.
   2) EMPRESA. :VAR_EMPRESA virou :VAR_EMPRESA_DRE, como nas outras telas.
   3) VINCULO. ' 3.1.01.01.000003' (Servico de manutencao automotiva)
      esta gravado 4 vezes com espaco na frente e nunca casava: a linha
      saia sem essa conta. Agora TRIM + DISTINCT. So a linha de detalhe
      muda; os totais (Receita bruta etc.) ja tinham a conta.
   4) As formulas das etapas de imposto eram buscadas por ORDEM sem
      filtrar a estrutura; hoje nao ha colisao (ORDEM 68-78 so existe na
      estrutura 1), mas qualquer estrutura nova com essas ORDENS trocaria
      a formula em silencio. Agora filtra ID_ESTR_DRE_TW = 1.
   5) Diagnostico: DATA_INICIO_USADA, DATA_FIM_USADA, ANO_ATU,
      LANC_EMPRESA, LANC_PERIODO, ULTIMO_MES_CARGA, DATAS_OK.
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
    /* os 12 meses do ano da data fim; so entram os que tocam o periodo
       filtrado (os outros saem NULL = celula vazia) */
    SELECT
        LPAD(m.NUM_MES, 2, '0')                         AS MES_NUM,
        CONCAT(LPAD(m.NUM_MES, 2, '0'), '/', p.ANO)     AS PERIODO,
        :VAR_EMPRESA_DRE                                AS EMPRESA
    FROM MESES m
    CROSS JOIN D p
    WHERE LAST_DAY(MAKEDATE(p.ANO, 1) + INTERVAL (m.NUM_MES - 1) MONTH) >= p.DT_INI
      AND          MAKEDATE(p.ANO, 1) + INTERVAL (m.NUM_MES - 1) MONTH  <= p.DT_FIM
),
VINC AS (
    /* DISTINCT + TRIM: o vinculo ' 3.1.01.01.000003' (Servico de
       manutencao automotiva) esta gravado 4 vezes e com espaco na frente;
       sem TRIM nunca casava, com TRIM sem DISTINCT contaria 4 vezes */
    SELECT DISTINCT c.ID_DET_DRE_TW, TRIM(c.ID_CONTA_CONTABIL) AS CONTA
    FROM CAD_CONTA_DRE_TW c
    INNER JOIN DET_DRE_TW dd ON dd.ID = c.ID_DET_DRE_TW
    WHERE dd.ID_ESTR_DRE_TW = 1
), CALCULOS AS ( /* ======================= Período: cada mês (JAN-DEZ) Apenas valor atual ======================== */ SELECT per.PERIODO AS PERIODO, d.ID, d.ORDEM, d.DESCRICAO, d.TIPO, d.FORMATACAO, d.HIERARQUIA, d.ORDEM AS ORDEM_ORIGEM, CASE WHEN d.TIPO = 'CONTA' THEN COALESCE(SUM(t.DRE_TECWAY) * 1000, 0) ELSE 0 END * IFNULL(d.SINAL, 1) AS VALOR, 1 AS SINAL FROM DET_DRE_TW d CROSS JOIN PERIODOS per LEFT JOIN VINC c ON c.ID_DET_DRE_TW = d.ID LEFT JOIN DRE_TECWAY t ON TRIM(t.ID_CONTA_CONTABIL) = c.CONTA AND t.MES = per.PERIODO AND t.ID_EMPRESA = per.EMPRESA LEFT JOIN ESTR_DRE_TW g ON d.ID_ESTR_DRE_TW = g.ID WHERE d.TIPO IN ('CONTA') AND g.TIPO_DEMONSTRATIVO = 'DRE' AND g.ID = 1 GROUP BY per.PERIODO, d.ID, d.ORDEM, d.DESCRICAO, d.TIPO, d.FORMATACAO, d.HIERARQUIA, d.SINAL UNION ALL /* ======================= Cálculos Dinâmicos (exceto 8 a 13) ======================== */ SELECT c.PERIODO, d.ID, d.ORDEM, d.DESCRICAO, d.TIPO, d.FORMATACAO, d.HIERARQUIA, c.ORDEM AS ORDEM_ORIGEM, c.VALOR * CASE WHEN d.TIPO = 'CALCULO1' AND LOCATE( CONCAT('-', '[', c.ORDEM, ']'), REPLACE(d.CALCULO, ' ', '') ) > 0 THEN -1 ELSE 1 END AS VALOR, 1 AS SINAL FROM DET_DRE_TW d JOIN CALCULOS c ON FIND_IN_SET( c.ORDEM, REPLACE( REPLACE( REPLACE( REPLACE( REPLACE(d.CALCULO, ' ', ''), '[', '' ), ']', '' ), '+', ',' ), '-', ',' ) ) LEFT JOIN ESTR_DRE_TW g ON d.ID_ESTR_DRE_TW = g.ID WHERE d.TIPO LIKE 'CALCULO%' AND d.TIPO NOT IN ( 'CALCULO8', 'CALCULO9', 'CALCULO10', 'CALCULO11', 'CALCULO12', 'CALCULO13' ) AND g.TIPO_DEMONSTRATIVO = 'DRE' AND g.ID = 1 ), CALCULOS_INTERMEDIARIOS AS ( SELECT PERIODO, ORDEM, DESCRICAO, TIPO, FORMATACAO, HIERARQUIA, SUM(VALOR) AS VALOR_BASE FROM CALCULOS GROUP BY PERIODO, ORDEM, DESCRICAO, TIPO, FORMATACAO, HIERARQUIA UNION ALL /* Placeholders para cálculos posteriores */ SELECT per.PERIODO, d.ORDEM, d.DESCRICAO, d.TIPO, d.FORMATACAO, d.HIERARQUIA, 0 AS VALOR_BASE FROM DET_DRE_TW d CROSS JOIN PERIODOS per WHERE d.ID_ESTR_DRE_TW = 1 AND d.TIPO IN ( 'CALCULO8', 'CALCULO9', 'CALCULO10', 'CALCULO11', 'CALCULO12', 'CALCULO13' ) ), VALORES_FINAIS AS ( SELECT x.PERIODO, x.ORDEM, x.DESCRICAO, x.TIPO, x.FORMATACAO, x.HIERARQUIA, CASE WHEN x.TIPO = 'CALCULO4' THEN ABS(x.VALOR_BASE) WHEN x.TIPO = 'CALCULO5' THEN CASE WHEN x.VALOR_BASE > 0 THEN x.VALOR_BASE * 0.15 ELSE 0 END WHEN x.TIPO = 'CALCULO6' THEN CASE WHEN x.VALOR_BASE > 240000 THEN (x.VALOR_BASE - 240000) * 0.10 ELSE 0 END WHEN x.TIPO = 'CALCULO7' THEN CASE WHEN x.VALOR_BASE > 0 THEN x.VALOR_BASE * 0.09 ELSE 0 END WHEN x.TIPO IN ( 'CALCULO8', 'CALCULO9', 'CALCULO10', 'CALCULO11', 'CALCULO12', 'CALCULO13' ) THEN 0 ELSE x.VALOR_BASE END AS VALOR_FINAL FROM CALCULOS_INTERMEDIARIOS x ), CALCULOS_N1 AS ( SELECT vf.PERIODO, vf.ORDEM, vf.DESCRICAO, vf.TIPO, vf.FORMATACAO, vf.HIERARQUIA, COALESCE(( SELECT SUM( v_base.VALOR_FINAL * CASE WHEN LOCATE( CONCAT('-', '[', v_base.ORDEM, ']'), REPLACE(def.CALCULO, ' ', '') ) > 0 THEN -1 ELSE 1 END ) FROM VALORES_FINAIS v_base CROSS JOIN ( SELECT CALCULO FROM DET_DRE_TW WHERE ORDEM = vf.ORDEM AND ID_ESTR_DRE_TW = 1 LIMIT 1 ) def WHERE v_base.PERIODO = vf.PERIODO AND FIND_IN_SET( CAST(v_base.ORDEM AS CHAR), REPLACE( REPLACE( REPLACE( REPLACE( REPLACE(def.CALCULO, ' ', ''), '[', '' ), ']', '' ), '+', ',' ), '-', ',' ) ) > 0 ), 0) AS VALOR_FINAL FROM VALORES_FINAIS vf WHERE vf.TIPO IN ('CALCULO8', 'CALCULO9') ), VALORES_COM_N1 AS ( SELECT * FROM VALORES_FINAIS WHERE TIPO NOT IN ( 'CALCULO8', 'CALCULO9', 'CALCULO10', 'CALCULO11', 'CALCULO12', 'CALCULO13' ) UNION ALL SELECT * FROM CALCULOS_N1 ), CALCULOS_N2 AS ( SELECT vf.PERIODO, vf.ORDEM, vf.DESCRICAO, vf.TIPO, vf.FORMATACAO, vf.HIERARQUIA, COALESCE(( SELECT SUM( v_n1.VALOR_FINAL * CASE WHEN LOCATE( CONCAT('-', '[', v_n1.ORDEM, ']'), REPLACE(def.CALCULO, ' ', '') ) > 0 THEN -1 ELSE 1 END ) FROM VALORES_COM_N1 v_n1 CROSS JOIN ( SELECT CALCULO FROM DET_DRE_TW WHERE ORDEM = vf.ORDEM AND ID_ESTR_DRE_TW = 1 LIMIT 1 ) def WHERE v_n1.PERIODO = vf.PERIODO AND FIND_IN_SET( CAST(v_n1.ORDEM AS CHAR), REPLACE( REPLACE( REPLACE( REPLACE( REPLACE(def.CALCULO, ' ', ''), '[', '' ), ']', '' ), '+', ',' ), '-', ',' ) ) > 0 ), 0) AS VALOR_FINAL FROM VALORES_FINAIS vf WHERE vf.TIPO = 'CALCULO10' ), VALORES_COM_N2 AS ( SELECT * FROM VALORES_COM_N1 UNION ALL SELECT * FROM CALCULOS_N2 ), CALCULOS_N3 AS ( SELECT vf.PERIODO, vf.ORDEM, vf.DESCRICAO, vf.TIPO, vf.FORMATACAO, vf.HIERARQUIA, ABS( COALESCE(( SELECT SUM( v_n2.VALOR_FINAL * CASE WHEN LOCATE( CONCAT('-', '[', v_n2.ORDEM, ']'), REPLACE(def.CALCULO, ' ', '') ) > 0 THEN -1 ELSE 1 END ) FROM VALORES_COM_N2 v_n2 CROSS JOIN ( SELECT CALCULO FROM DET_DRE_TW WHERE ORDEM = vf.ORDEM AND ID_ESTR_DRE_TW = 1 LIMIT 1 ) def WHERE v_n2.PERIODO = vf.PERIODO AND FIND_IN_SET( CAST(v_n2.ORDEM AS CHAR), REPLACE( REPLACE( REPLACE( REPLACE( REPLACE(def.CALCULO, ' ', ''), '[', '' ), ']', '' ), '+', ',' ), '-', ',' ) ) > 0 ), 0) ) AS VALOR_FINAL FROM VALORES_FINAIS vf WHERE vf.TIPO = 'CALCULO11' ), VALORES_COM_N3 AS ( SELECT * FROM VALORES_COM_N2 UNION ALL SELECT * FROM CALCULOS_N3 ), CALCULOS_N4 AS ( SELECT vf.PERIODO, vf.ORDEM, vf.DESCRICAO, vf.TIPO, vf.FORMATACAO, vf.HIERARQUIA, COALESCE(( SELECT SUM( v_n3.VALOR_FINAL * CASE WHEN LOCATE( CONCAT('-', '[', v_n3.ORDEM, ']'), REPLACE(def.CALCULO, ' ', '') ) > 0 THEN -1 ELSE 1 END ) FROM VALORES_COM_N3 v_n3 CROSS JOIN ( SELECT CALCULO FROM DET_DRE_TW WHERE ORDEM = vf.ORDEM AND ID_ESTR_DRE_TW = 1 LIMIT 1 ) def WHERE v_n3.PERIODO = vf.PERIODO AND FIND_IN_SET( CAST(v_n3.ORDEM AS CHAR), REPLACE( REPLACE( REPLACE( REPLACE( REPLACE(def.CALCULO, ' ', ''), '[', '' ), ']', '' ), '+', ',' ), '-', ',' ) ) > 0 ), 0) AS VALOR_FINAL FROM VALORES_FINAIS vf WHERE vf.TIPO = 'CALCULO12' ), VALORES_COM_N4 AS ( SELECT * FROM VALORES_COM_N3 UNION ALL SELECT * FROM CALCULOS_N4 ), CALCULOS_N5 AS ( SELECT vf.PERIODO, vf.ORDEM, vf.DESCRICAO, vf.TIPO, vf.FORMATACAO, vf.HIERARQUIA, COALESCE(( SELECT SUM( v_n4.VALOR_FINAL * CASE WHEN LOCATE( CONCAT('-', '[', v_n4.ORDEM, ']'), REPLACE(def.CALCULO, ' ', '') ) > 0 THEN -1 ELSE 1 END ) FROM VALORES_COM_N4 v_n4 CROSS JOIN ( SELECT CALCULO FROM DET_DRE_TW WHERE ORDEM = vf.ORDEM AND ID_ESTR_DRE_TW = 1 LIMIT 1 ) def WHERE v_n4.PERIODO = vf.PERIODO AND FIND_IN_SET( CAST(v_n4.ORDEM AS CHAR), REPLACE( REPLACE( REPLACE( REPLACE( REPLACE(def.CALCULO, ' ', ''), '[', '' ), ']', '' ), '+', ',' ), '-', ',' ) ) > 0 ), 0) AS VALOR_FINAL FROM VALORES_FINAIS vf WHERE vf.TIPO = 'CALCULO13' ), RES_FIN AS ( SELECT PERIODO, ORDEM, DESCRICAO, FORMATACAO, HIERARQUIA, VALOR_FINAL FROM VALORES_COM_N4 UNION ALL SELECT PERIODO, ORDEM, DESCRICAO, FORMATACAO, HIERARQUIA, VALOR_FINAL FROM CALCULOS_N5 ) SELECT ORDEM, DESCRICAO, FORMATACAO, HIERARQUIA, MAX(CASE WHEN PERIODO LIKE '01/%' THEN VALOR_FINAL END) AS VALOR_JAN, MAX(CASE WHEN PERIODO LIKE '02/%' THEN VALOR_FINAL END) AS VALOR_FEV, MAX(CASE WHEN PERIODO LIKE '03/%' THEN VALOR_FINAL END) AS VALOR_MAR, MAX(CASE WHEN PERIODO LIKE '04/%' THEN VALOR_FINAL END) AS VALOR_ABR, MAX(CASE WHEN PERIODO LIKE '05/%' THEN VALOR_FINAL END) AS VALOR_MAI, MAX(CASE WHEN PERIODO LIKE '06/%' THEN VALOR_FINAL END) AS VALOR_JUN, MAX(CASE WHEN PERIODO LIKE '07/%' THEN VALOR_FINAL END) AS VALOR_JUL, MAX(CASE WHEN PERIODO LIKE '08/%' THEN VALOR_FINAL END) AS VALOR_AGO, MAX(CASE WHEN PERIODO LIKE '09/%' THEN VALOR_FINAL END) AS VALOR_SET, MAX(CASE WHEN PERIODO LIKE '10/%' THEN VALOR_FINAL END) AS VALOR_OUT, MAX(CASE WHEN PERIODO LIKE '11/%' THEN VALOR_FINAL END) AS VALOR_NOV, MAX(CASE WHEN PERIODO LIKE '12/%' THEN VALOR_FINAL END) AS VALOR_DEZ,
    (SELECT DT_INI FROM D)                                   AS DATA_INICIO_USADA,
    (SELECT DT_FIM FROM D)                                   AS DATA_FIM_USADA,
    (SELECT ANO    FROM D)                                   AS ANO_ATU,
    (SELECT COUNT(*) FROM DRE_TECWAY
      WHERE ID_EMPRESA = :VAR_EMPRESA_DRE)                   AS LANC_EMPRESA,
    (SELECT COUNT(*) FROM DRE_TECWAY t2
      INNER JOIN PERIODOS p2 ON p2.PERIODO = t2.MES
      WHERE t2.ID_EMPRESA = :VAR_EMPRESA_DRE)                AS LANC_PERIODO,
    (SELECT DATE_FORMAT(MAX(STR_TO_DATE(CONCAT('01/', MES), '%d/%m/%Y')), '%m/%Y')
       FROM DRE_TECWAY WHERE ID_EMPRESA = :VAR_EMPRESA_DRE)  AS ULTIMO_MES_CARGA,
    CASE WHEN (SELECT DT_INI_IN FROM PARAMS) IS NULL
            OR (SELECT DT_FIM_IN FROM PARAMS) IS NULL
         THEN 0 ELSE 1 END                                   AS DATAS_OK
FROM RES_FIN WHERE ORDEM NOT BETWEEN 52 AND 68 GROUP BY ORDEM, DESCRICAO, FORMATACAO, HIERARQUIA ORDER BY ORDEM
