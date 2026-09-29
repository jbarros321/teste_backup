/* =====================================================================
   index9 - Imagens das notas (varNotaImg)
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
   Imagens das notas da empresa com REFERENCIA ate a data fim.

   O QUE MUDOU EM RELACAO A ORIGINAL
   - As datas passam pelo mesmo parser das outras telas ('DD/MM/AAAA' e
     'AAAA-MM-DD', com ou sem hora). DATE('31/12/2025') devolve NULL no
     MySQL, entao a data crua nao servia.
   - :VAR_DATA_REF_DRE (data unica) foi trocada pela data fim do filtro,
     ja interpretada. Comparar REFERENCIA com o texto cru
     '31/12/2025 00:00:00' e comparacao de texto, nao de data.

   COLUNAS DEVOLVIDAS
       ID_NOTAS_EXPLICATIVAS_DET, URL_IMAGEM
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
)
SELECT NTE_DET.ID AS ID_NOTAS_EXPLICATIVAS_DET, NTE_IMG.URL_IMAGEM
FROM NOTAS_EXPLICATIVAS_CAB NTE
INNER JOIN NOTAS_EXPLICATIVAS_DET NTE_DET
        ON NTE.ID = NTE_DET.ID_NOTAS_EXPLICATIVAS_CAB
INNER JOIN NOTAS_EXPLICATIVAS_IMAGENS NTE_IMG
        ON NTE_DET.ID = NTE_IMG.ID_NOTAS_EXPLICATIVAS_DET
WHERE NTE.CODEMP = :VAR_EMPRESA_DRE
  AND DATE(NTE.REFERENCIA) <= (SELECT DT_FIM FROM D)
ORDER BY NTE_IMG.DT_CRIACAO;
