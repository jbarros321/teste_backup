/* =====================================================================
   index9 - Tabelas das notas (queryNotasTab)
   tenant_61302

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
   Mantida a logica original: cada grupo da nota tem sua propria data
   de referencia (REFERENCIA_GRUPO); contas de resultado somam o ano do
   grupo ate essa data, contas patrimoniais o saldo ate essa data. O
   filtro so define quais notas entram (REFERENCIA ate a data fim) e
   quais anos vao em cada coluna (ano da data fim e o anterior).

   O QUE MUDOU EM RELACAO A ORIGINAL
   - As datas passam pelo mesmo parser das outras telas ('DD/MM/AAAA' e
     'AAAA-MM-DD', com ou sem hora). DATE('31/12/2025') devolve NULL no
     MySQL, entao a data crua nao servia.
   - :VAR_DATA_REF_DRE (data unica) foi trocada pela data fim do filtro,
     ja interpretada. Comparar REFERENCIA com o texto cru
     '31/12/2025 00:00:00' e comparacao de texto, nao de data.

   COLUNAS DEVOLVIDAS
       ID, REFERENCIA, CODEMP, ID_GRU, NOTA, DESCRICAO
       VALOR_ANO_ATUAL, VALOR_ANO_ANTERIOR   em MILHARES (como a original)
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
CONTAS_GRUPO AS ( SELECT NTE.ID, NTE.REFERENCIA, NTE.CODEMP, NTE_DET.ID AS ID_GRU, NTE_DET.NOTA, NTE_DET_GRU.ID AS ID_GRUPO, NTE_DET_GRU.DESCRICAO, NTE_CTB_REF.ID AS ID_NOTAS_EXPLICATIVAS_GRUPOS_REFER, NTE_CTB_REF.REFERENCIA AS REFERENCIA_GRUPO, NTE_CTB.PADRAO_CTACTB, NTE_CTB.SINAL FROM NOTAS_EXPLICATIVAS_CAB NTE INNER JOIN NOTAS_EXPLICATIVAS_DET NTE_DET ON NTE.ID = NTE_DET.ID_NOTAS_EXPLICATIVAS_CAB INNER JOIN NOTAS_EXPLICATIVAS_GRUPOS NTE_DET_GRU ON NTE_DET.ID = NTE_DET_GRU.ID_NOTAS_EXPLICATIVAS_DET INNER JOIN NOTAS_EXPLICATIVAS_GRUPOS_REFER NTE_CTB_REF ON NTE_DET_GRU.ID = NTE_CTB_REF.ID_NOTAS_EXPLICATIVAS_GRUPOS INNER JOIN NOTAS_EXPLICATIVAS_GRUPOS_CONTAS NTE_CTB ON NTE_CTB_REF.ID = NTE_CTB.ID_NOTAS_EXPLICATIVAS_GRUPOS_REFER WHERE NTE.CODEMP = :VAR_EMPRESA_DRE AND DATE(NTE.REFERENCIA) <= (SELECT DT_FIM FROM D) ), BALANCETE AS ( SELECT CODEMP, CTACTB, REFERENCIA, SUM(VLRLANC) AS VLRLANC FROM IMP_BASE_BALANCETE WHERE CODEMP = :VAR_EMPRESA_DRE GROUP BY CODEMP, CTACTB, REFERENCIA ), BASE AS ( SELECT CG.ID, CG.REFERENCIA, CG.CODEMP, CG.ID_GRU, CG.NOTA, CG.ID_GRUPO, CG.DESCRICAO, CG.REFERENCIA_GRUPO, YEAR(CG.REFERENCIA_GRUPO) AS ANO_GRUPO, ( COALESCE(SUM(BAL.VLRLANC), 0) * CG.SINAL ) / 1000 AS VALOR FROM CONTAS_GRUPO CG LEFT JOIN BALANCETE BAL ON BAL.CODEMP = CG.CODEMP AND BAL.CTACTB = CG.PADRAO_CTACTB AND ( ( ( CG.PADRAO_CTACTB LIKE '3%' OR CG.PADRAO_CTACTB LIKE '4%' OR CG.PADRAO_CTACTB LIKE '5%' ) AND YEAR(BAL.REFERENCIA) = YEAR(CG.REFERENCIA_GRUPO) AND BAL.REFERENCIA <= CG.REFERENCIA_GRUPO ) OR ( CG.PADRAO_CTACTB NOT LIKE '3%' AND CG.PADRAO_CTACTB NOT LIKE '4%' AND CG.PADRAO_CTACTB NOT LIKE '5%' AND BAL.REFERENCIA <= CG.REFERENCIA_GRUPO ) ) GROUP BY CG.ID, CG.REFERENCIA, CG.CODEMP, CG.ID_GRU, CG.NOTA, CG.ID_GRUPO, CG.DESCRICAO, CG.REFERENCIA_GRUPO, CG.SINAL ) SELECT BASE.ID, BASE.REFERENCIA, BASE.CODEMP, BASE.ID_GRU, BASE.NOTA, BASE.DESCRICAO, SUM( CASE WHEN BASE.ANO_GRUPO = (SELECT ANO_ATU FROM D) THEN BASE.VALOR ELSE 0 END ) AS VALOR_ANO_ATUAL, SUM( CASE WHEN BASE.ANO_GRUPO = (SELECT ANO_ANT FROM D) THEN BASE.VALOR ELSE 0 END ) AS VALOR_ANO_ANTERIOR FROM BASE GROUP BY BASE.ID, BASE.REFERENCIA, BASE.CODEMP, BASE.ID_GRU, BASE.NOTA, BASE.DESCRICAO ORDER BY CAST( SUBSTRING_INDEX( CONCAT(BASE.NOTA, '.0.0.0'), '.', 1 ) AS UNSIGNED ), CAST( SUBSTRING_INDEX( SUBSTRING_INDEX( CONCAT(BASE.NOTA, '.0.0.0'), '.', 2 ), '.', -1 ) AS UNSIGNED ), CAST( SUBSTRING_INDEX( SUBSTRING_INDEX( CONCAT(BASE.NOTA, '.0.0.0'), '.', 3 ), '.', -1 ) AS UNSIGNED ), CAST( SUBSTRING_INDEX( SUBSTRING_INDEX( CONCAT(BASE.NOTA, '.0.0.0'), '.', 4 ), '.', -1 ) AS UNSIGNED ), BASE.ID_GRUPO;
