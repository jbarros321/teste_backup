/* =====================================================================
   index9 - DFC (queryDFC)
   tenant_61302 | ESTR_DEMONSTRATIVOS.ID = 10

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
   Mesma query da index5 (DFC Externo), ja conferida: filtro 2025 ->
   aumento de caixa 4.025 (2025) e (2.933) (2024). So o SELECT final
   muda, para devolver em MILHARES como a index9 espera.

   O QUE MUDOU EM RELACAO A ORIGINAL
   - As datas passam pelo mesmo parser das outras telas ('DD/MM/AAAA' e
     'AAAA-MM-DD', com ou sem hora). DATE('31/12/2025') devolve NULL no
     MySQL, entao a data crua nao servia.
   - ANO_REFERENCIA com dois formatos (2025 e 20251201):
     `ANO_REFERENCIA <= YEAR(...)` virava `20251201 <= 2025`, FALSO, e a
     linha sumia. Agora o ano e normalizado antes de comparar.
   - Caixa inicial (5.1) e linhas de variacao passam a usar o saldo na
     vespera da data inicio (identico no ano cheio).
   - Linha 1.9.1 Outros sem lancamento manual nao vira NULL.

   COLUNAS DEVOLVIDAS
       ORDEM, DESCRICAO, VARIACAO
       VALOR_ANO_ATUAL, VALOR_ANO_ANTERIOR   em MILHARES (como a original)
       + diagnostico
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
base_bal AS (
    SELECT
        CTACTB,
        /* fluxo do periodo e do mesmo periodo um ano antes */
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                          AND (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END) AS ano_atu,
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI_ANT FROM D)
                                          AND (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS ano_ant,
        /* saldos de FECHAMENTO: acumulado ate a data fim de cada coluna */
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END) AS acum_atu,
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS acum_ant,
        /* saldos de ABERTURA: acumulado ate a vespera da data inicio de
           cada coluna. Antes a abertura era "data fim menos 1 ano", o que so
           acerta quando o filtro e o ano inteiro: filtrando 01/03 a 31/07,
           o caixa inicial saia em 31/07 do ano anterior e a DFC nao fechava
           (caixa inicial + fluxo do periodo <> caixa final). Com < DT_INI a
           abertura, o fluxo (BETWEEN) e o fechamento (<= DT_FIM) cobrem o
           mesmo balancete sem buraco nem sobreposicao. */
        SUM(CASE WHEN DATE(REFERENCIA) < (SELECT DT_INI FROM D)
                 THEN VLRLANC ELSE 0 END) AS abert_atu,
        SUM(CASE WHEN DATE(REFERENCIA) < (SELECT DT_INI_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS abert_ant
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
    GROUP BY CTACTB
),
linhas AS (
    /* a lista de linhas vem da ESTRUTURA: linha sem cadastro continua
       aparecendo, marcada, em vez de sumir e mudar o total sem explicacao */
    SELECT DET.ID AS ID_DET, DET.ORDEM, DET.NOME_GRUPO, DET.VARIACAO
    FROM ESTR_DEMONSTRATIVOS EST
    INNER JOIN DET_DEMONSTRATIVO DET
            ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    WHERE EST.ID = 10
),
ref_atu AS (
    /* UM cadastro para as DUAS colunas: o do ano mais recente que seja
       <= ano da data fim (regra da query original). Filtrando 2025, a
       coluna 2024 tambem usa o cadastro de 2025 - e assim que a DFC
       publicada foi montada: em 2024 a 4.3 Mutuos fica 786 mil (com as
       contas 2.1.01.05.000001 e 2.1.01.12.000005..09, que so existem no
       cadastro de 2025) e o aumento de caixa (2.933) mil. Com o cadastro
       proprio de 2024 dava (2.910), divergindo do publicado.

       O CASE normaliza os dois formatos de ANO_REFERENCIA (2025 e
       20251201). O ORDER BY usa o ano JA normalizado: ordenando o valor
       cru, 20251201 venceria 2026. O LIMIT 1 impede que um ano com dois
       registros dobre os vinculos. */
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
ref_ant AS (
    /* a coluna comparativa usa o MESMO cadastro da atual */
    SELECT ID_DET, ID_REF FROM ref_atu
),
regras_atu AS (
    SELECT DISTINCT L.ORDEM, C.PADRAO_CTACTB, C.SINAL
    FROM linhas L
    INNER JOIN ref_atu RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
regras_ant AS (
    SELECT DISTINCT L.ORDEM, C.PADRAO_CTACTB, C.SINAL
    FROM linhas L
    INNER JOIN ref_ant RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
qtd_atu AS (
    SELECT ORDEM, COUNT(*) AS QTD FROM regras_atu GROUP BY ORDEM
),
qtd_ant AS (
    SELECT ORDEM, COUNT(*) AS QTD FROM regras_ant GROUP BY ORDEM
),
val_atu AS (
    /* fluxo, fechamento e abertura por linha, para a coluna ATUAL */
    SELECT
        R.ORDEM,
        SUM(IFNULL(B.ano_atu,   0) * R.SINAL) AS ano_v,
        SUM(IFNULL(B.acum_atu,  0) * R.SINAL) AS acum_v,
        SUM(IFNULL(B.abert_atu, 0) * R.SINAL) AS acum_ant_v
    FROM regras_atu R
    LEFT JOIN base_bal B ON B.CTACTB = R.PADRAO_CTACTB
    GROUP BY R.ORDEM
),
val_ant AS (
    /* os mesmos agregados deslocados um ano, para a coluna COMPARATIVA */
    SELECT
        R.ORDEM,
        SUM(IFNULL(B.ano_ant,   0) * R.SINAL) AS ano_v,
        SUM(IFNULL(B.acum_ant,  0) * R.SINAL) AS acum_v,
        SUM(IFNULL(B.abert_ant, 0) * R.SINAL) AS acum_ant_v
    FROM regras_ant R
    LEFT JOIN base_bal B ON B.CTACTB = R.PADRAO_CTACTB
    GROUP BY R.ORDEM
),
exc_atu AS (
    /* exclusoes da DFC: SUBTRAEM valor da linha (ABS * SINAL), no ano da
       coluna atual. A ORDEM entra no agrupamento, entao uma conta excluida
       de uma linha nao afeta as outras. */
    SELECT L.ORDEM,
           SUM(CASE WHEN DATE(B.REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                               AND (SELECT DT_FIM FROM D)
                    THEN ABS(IFNULL(B.VLRLANC, 0)) * EXC.SINAL ELSE 0 END) AS V
    FROM linhas L
    INNER JOIN ref_atu RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB_EXC EXC
            ON EXC.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
    LEFT JOIN IMP_BASE_BALANCETE B
           ON B.CTACTB = EXC.PADRAO_CTACTB
          AND B.CODEMP = :VAR_EMPRESA_DRE
    GROUP BY L.ORDEM
),
exc_ant AS (
    SELECT L.ORDEM,
           SUM(CASE WHEN DATE(B.REFERENCIA) BETWEEN (SELECT DT_INI_ANT FROM D)
                                               AND (SELECT DT_FIM_ANT FROM D)
                    THEN ABS(IFNULL(B.VLRLANC, 0)) * EXC.SINAL ELSE 0 END) AS V
    FROM linhas L
    INNER JOIN ref_ant RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB_EXC EXC
            ON EXC.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
    LEFT JOIN IMP_BASE_BALANCETE B
           ON B.CTACTB = EXC.PADRAO_CTACTB
          AND B.CODEMP = :VAR_EMPRESA_DRE
    GROUP BY L.ORDEM
),
outros AS (
    /* lancamento manual da linha 1.9.1. Sem linha em OUTROS_DFC o SUM
       devolve NULL, e a 1.9.1 sairia como "sem cadastro" - por isso o
       IFNULL(...,0) no SELECT final. */
    SELECT
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                          AND (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END) AS v_atu,
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI_ANT FROM D)
                                          AND (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS v_ant
    FROM OUTROS_DFC
    WHERE CODEMP = :VAR_EMPRESA_DRE
)
SELECT
    L.ORDEM,
    L.NOME_GRUPO AS DESCRICAO,
    L.VARIACAO,
    (CASE WHEN QA.QTD IS NULL AND TRIM(L.ORDEM) <> '1.9.1' THEN NULL ELSE
        (CASE
            WHEN TRIM(L.ORDEM) = '1.9.1' AND IFNULL(VA.ano_v, 0) = 0
                 THEN IFNULL((SELECT v_atu FROM outros), 0)
            WHEN TRIM(L.ORDEM) = '5.1' THEN VA.acum_ant_v
            WHEN TRIM(L.ORDEM) = '5.2' THEN VA.acum_v
            WHEN L.VARIACAO = 'Sim'    THEN VA.acum_ant_v - VA.acum_v
            ELSE VA.ano_v
        END) - IFNULL(EA.V, 0)
    END) / 1000 AS VALOR_ANO_ATUAL,
    (CASE WHEN QB.QTD IS NULL AND TRIM(L.ORDEM) <> '1.9.1' THEN NULL ELSE
        (CASE
            WHEN TRIM(L.ORDEM) = '1.9.1' AND IFNULL(VB.ano_v, 0) = 0
                 THEN IFNULL((SELECT v_ant FROM outros), 0)
            WHEN TRIM(L.ORDEM) = '5.1' THEN VB.acum_ant_v
            WHEN TRIM(L.ORDEM) = '5.2' THEN VB.acum_v
            WHEN L.VARIACAO = 'Sim'    THEN VB.acum_ant_v - VB.acum_v
            ELSE VB.ano_v
        END) - IFNULL(EB.V, 0)
    END) / 1000 AS VALOR_ANO_ANTERIOR,
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
FROM linhas L
LEFT JOIN qtd_atu QA ON QA.ORDEM = L.ORDEM
LEFT JOIN qtd_ant QB ON QB.ORDEM = L.ORDEM
LEFT JOIN val_atu VA ON VA.ORDEM = L.ORDEM
LEFT JOIN val_ant VB ON VB.ORDEM = L.ORDEM
LEFT JOIN exc_atu EA ON EA.ORDEM = L.ORDEM
LEFT JOIN exc_ant EB ON EB.ORDEM = L.ORDEM
ORDER BY L.ORDEM
LIMIT 500;
