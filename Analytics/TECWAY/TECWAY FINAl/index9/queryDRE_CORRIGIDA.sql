/* =====================================================================
   index9 - DRE (queryDRE)
   tenant_61302 | ESTR_DEMONSTRATIVOS.ID = 3

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
   Resultado = MOVIMENTO das contas 3, 4 e 5 entre data inicio e data
   fim (BETWEEN esta certo aqui: DRE e fluxo). Comparativa: mesmo
   periodo um ano antes.

   O QUE MUDOU EM RELACAO A ORIGINAL
   - As datas passam pelo mesmo parser das outras telas ('DD/MM/AAAA' e
     'AAAA-MM-DD', com ou sem hora). DATE('31/12/2025') devolve NULL no
     MySQL, entao a data crua nao servia.
   - ANO_REFERENCIA com dois formatos (2025 e 20251201):
     `ANO_REFERENCIA <= YEAR(...)` virava `20251201 <= 2025`, FALSO, e a
     linha sumia. Agora o ano e normalizado antes de comparar.
   - ROW_NUMBER particionado so por conta: uma conta que atende duas
     linhas era somada em uma so. Agora a particao inclui a linha, e as
     exclusoes valem por linha. (Conferido em 29/09/2026, CODEMP 999,
     2024 e 2025: nenhuma diferenca de valor com os dados atuais.)

   COLUNAS DEVOLVIDAS
       HIERARQUIA, DESCRICAO, NOTA
       VALOR_ANO_ATUAL, VALOR_ANO_ANTERIOR   valor cheio (a tela divide)
       + diagnostico (DATAS_OK etc.)
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
dados AS (
    SELECT
        CTACTB,
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                          AND (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END) AS VALOR_ATUAL,
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI_ANT FROM D)
                                          AND (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS VALOR_ANTERIOR
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
      AND (CTACTB LIKE '3%' OR CTACTB LIKE '4%' OR CTACTB LIKE '5%')
    GROUP BY CTACTB
),
linhas AS (
    SELECT DET.ID AS ID_DET, DET.ORDEM, DET.NOME_GRUPO, DET.COD_NOTA_EXPLICATIVA, DET.VARIACAO
    FROM ESTR_DEMONSTRATIVOS EST
    INNER JOIN DET_DEMONSTRATIVO DET ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    WHERE EST.ID = 3
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
        D2.VALOR_ATUAL, D2.VALOR_ANTERIOR, R.ORDEM, R.NOME_GRUPO,
        R.COD_NOTA_EXPLICATIVA, R.SINAL,
        ROW_NUMBER() OVER (
            PARTITION BY D2.CTACTB, R.ORDEM, R.NOME_GRUPO
            ORDER BY LENGTH(R.PADRAO_CTACTB) DESC, R.PADRAO_CTACTB DESC
        ) AS rn
    FROM dados D2
    INNER JOIN regras R ON D2.CTACTB LIKE R.PADRAO_CTACTB
    LEFT JOIN excl E ON E.ORDEM = R.ORDEM AND D2.CTACTB LIKE E.PADRAO_CTACTB
    WHERE E.PADRAO_CTACTB IS NULL
),
agg AS (
    SELECT ORDEM, NOME_GRUPO,
           SUM(IFNULL(VALOR_ATUAL, 0)    * SINAL) AS VALOR_ANO_ATUAL,
           SUM(IFNULL(VALOR_ANTERIOR, 0) * SINAL) AS VALOR_ANO_ANTERIOR
    FROM contas_mapeadas WHERE rn = 1
    GROUP BY ORDEM, NOME_GRUPO
)
SELECT
    R.ORDEM                            AS HIERARQUIA,
    R.NOME_GRUPO                       AS DESCRICAO,
    R.COD_NOTA_EXPLICATIVA             AS NOTA,
    IFNULL(A.VALOR_ANO_ATUAL, 0)       AS VALOR_ANO_ATUAL,
    IFNULL(A.VALOR_ANO_ANTERIOR, 0)    AS VALOR_ANO_ANTERIOR,
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
FROM (SELECT DISTINCT ORDEM, NOME_GRUPO, COD_NOTA_EXPLICATIVA FROM regras) R
LEFT JOIN agg A ON A.ORDEM = R.ORDEM AND A.NOME_GRUPO = R.NOME_GRUPO
ORDER BY R.ORDEM;
