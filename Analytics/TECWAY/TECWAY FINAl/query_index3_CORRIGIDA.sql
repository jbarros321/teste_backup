/* =====================================================================
   DRE EXTERNO - query da tela index3.html
   tenant_61302 | ESTR_DEMONSTRATIVOS.ID = 3

   Parametros do componente:
       :VAR_DATA_INICIO   data inicial do filtro da tela
       :VAR_DATA_FIM      data final   do filtro da tela
       :VAR_EMPRESA_DRE   CODEMP

   ATENCAO: nao use comentario de linha (--) nesta query. O componente
   costuma gravar tudo numa unica linha, e um -- comentaria todo o resto.
   Aqui so ha comentario de bloco, que sobrevive ao achatamento.

   ---------------------------------------------------------------------
   DRE E FLUXO, NAO SALDO - o BETWEEN aqui esta CERTO
   ---------------------------------------------------------------------
   Diferente do Balanco Patrimonial, a DRE mede o RESULTADO DO PERIODO.
   Entao `REFERENCIA BETWEEN :VAR_DATA_INICIO AND :VAR_DATA_FIM` e o
   comportamento correto e foi mantido. No BP isso era defeito (saldo
   patrimonial e acumulado desde o inicio); aqui nao.

   ---------------------------------------------------------------------
   CADA COLUNA USA O CADASTRO DO SEU PROPRIO ANO
   ---------------------------------------------------------------------
       VALOR_ANO_ATUAL -> cadastro do ano de :VAR_DATA_FIM
       VALOR_ANO_ANTERIOR -> cadastro do ano de :VAR_DATA_FIM menos 1

   Sem fallback entre anos. Se o ano daquela coluna nao tem conta
   vinculada, aquela coluna vem NULL e a tela mostra "sem cadastro"; a
   outra coluna continua trazendo valor normalmente.

   A linha sem cadastro continua aparecendo, marcada, em vez de sumir.

   ---------------------------------------------------------------------
   O QUE ESTAVA ERRADO NA VERSAO ORIGINAL
   ---------------------------------------------------------------------

   1) ANO_REFERENCIA ESTA COM DOIS FORMATOS NA MESMA COLUNA.
      Valores reais no tenant: 2024, 2025, 2026 (4 digitos) e 20231201,
      20241201, 20251201, 20261201 (AAAAMMDD). Dos 356 registros, 352 sao
      AAAAMMDD e 4 sao ano puro.

      A comparacao `ANO_REFERENCIA <= YEAR(:VAR_DATA_FIM)` virava
      `20251201 <= 2026`, que e FALSO. MAX(...) devolvia NULL, o
      `ANO_REFERENCIA = NULL` nunca casava e a linha desaparecia inteira.
      Na estrutura 3 TODAS as referencias sao AAAAMMDD, ou seja a tela
      vinha completamente vazia. Era este o sintoma.

      Correcao: normalizar para ano antes de comparar.

   2) AS EXCLUSOES PERDIAM A LINHA. A CTE regras_excluidas selecionava so
      PADRAO_CTACTB, sem ORDEM, e o LEFT JOIN casava so pela conta. Uma
      conta excluida de UMA linha passava a ser excluida de TODAS.

   3) O ROW_NUMBER PARTICIONAVA SO POR CTACTB. `PARTITION BY D.CTACTB`
      sem ORDEM: quando a mesma conta atende duas linhas do demonstrativo,
      so UMA delas recebia o valor - a outra ficava zerada, escolhida ao
      acaso. E o ORDER BY tinha so LENGTH, sem desempate estavel.

   4) BETWEEN mantido: DRE e fluxo (ver acima).

   ---------------------------------------------------------------------
   COLUNAS DEVOLVIDAS
   ---------------------------------------------------------------------
       HIERARQUIA, DESCRICAO, NOTA
       VALOR_ANO_ATUAL / VALOR_ANO_ANTERIOR   valor, ou NULL se o ano daquela
                                     coluna nao tem conta vinculada
       QTD_CONTAS_ATU / _ANT         contas vinculadas em cada ano
       SEM_CADASTRO_ATU / _ANT       1 = aquele ano nao tem cadastro
       ANO_ATU / ANO_ANT             os anos de cada coluna
       DATA_INICIO_USADA, DATA_FIM_USADA
   ===================================================================== */

WITH PARAMS AS (
    SELECT
        /* aceita 'AAAA-MM-DD' e 'DD/MM/AAAA' */
        /* A plataforma entrega a data como 'DD/MM/AAAA HH:MM:SS'. O parser
           anterior exigia 'DD/MM/AAAA' exato, entao a hora fazia o REGEXP
           falhar; e DATE('01/01/2025 00:00:00') devolve NULL no MySQL,
           porque DATE() nao entende dia/mes/ano. Resultado: a data virava
           NULL, caia no CURDATE() e a tela calculava sobre hoje.

           O LEFT(...,10) corta a hora antes de testar, e as duas grafias
           (DD/MM/AAAA e AAAA-MM-DD) sao tratadas separadamente. */
        CASE
            WHEN LEFT(TRIM(COALESCE(:VAR_DATA_INICIO, '')), 10) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
                 THEN STR_TO_DATE(LEFT(TRIM(:VAR_DATA_INICIO), 10), '%d/%m/%Y')
            WHEN LEFT(TRIM(COALESCE(:VAR_DATA_INICIO, '')), 10) REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}$'
                 THEN DATE(LEFT(TRIM(:VAR_DATA_INICIO), 10))
            ELSE NULL
        END AS DT_INI_IN,
        /* A plataforma entrega a data como 'DD/MM/AAAA HH:MM:SS'. O parser
           anterior exigia 'DD/MM/AAAA' exato, entao a hora fazia o REGEXP
           falhar; e DATE('01/01/2025 00:00:00') devolve NULL no MySQL,
           porque DATE() nao entende dia/mes/ano. Resultado: a data virava
           NULL, caia no CURDATE() e a tela calculava sobre hoje.

           O LEFT(...,10) corta a hora antes de testar, e as duas grafias
           (DD/MM/AAAA e AAAA-MM-DD) sao tratadas separadamente. */
        CASE
            WHEN LEFT(TRIM(COALESCE(:VAR_DATA_FIM, '')), 10) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
                 THEN STR_TO_DATE(LEFT(TRIM(:VAR_DATA_FIM), 10), '%d/%m/%Y')
            WHEN LEFT(TRIM(COALESCE(:VAR_DATA_FIM, '')), 10) REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}$'
                 THEN DATE(LEFT(TRIM(:VAR_DATA_FIM), 10))
            ELSE NULL
        END AS DT_FIM_IN
),
D AS (
    /* filtro vazio nao pode derrubar a tela: sem data fim usa hoje, sem
       data inicio usa 01/01 do ano da data fim. Datas invertidas: troca. */
    SELECT
        LEAST(DT_INI, DT_FIM)                        AS DT_INI,
        GREATEST(DT_INI, DT_FIM)                     AS DT_FIM,
        YEAR(GREATEST(DT_INI, DT_FIM))               AS ANO_ATU,
        YEAR(GREATEST(DT_INI, DT_FIM)) - 1           AS ANO_ANT,
        DATE_SUB(LEAST(DT_INI, DT_FIM),    INTERVAL 1 YEAR) AS DT_INI_ANT,
        DATE_SUB(GREATEST(DT_INI, DT_FIM), INTERVAL 1 YEAR) AS DT_FIM_ANT
    FROM (
        SELECT
            COALESCE(DT_FIM_IN, CURDATE())                            AS DT_FIM,
            COALESCE(DT_INI_IN,
                     MAKEDATE(YEAR(COALESCE(DT_FIM_IN, CURDATE())), 1)) AS DT_INI
        FROM PARAMS
    ) X
),
dados AS (
    /* FLUXO do periodo, e o mesmo periodo um ano antes. Contas de
       resultado apenas (3, 4 e 5). */
    SELECT
        CTACTB,
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                          AND (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END) AS valor_atu,
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI_ANT FROM D)
                                          AND (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS valor_ant
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
      AND (CTACTB LIKE '3%' OR CTACTB LIKE '4%' OR CTACTB LIKE '5%')
    GROUP BY CTACTB
),
linhas AS (
    /* a lista de linhas vem da ESTRUTURA, nao dos vinculos - e o que faz a
       linha sem cadastro continuar aparecendo, marcada, em vez de sumir */
    SELECT
        DET.ID   AS ID_DET,
        DET.ORDEM,
        DET.NOME_GRUPO,
        DET.COD_NOTA_EXPLICATIVA
    FROM ESTR_DEMONSTRATIVOS EST
    INNER JOIN DET_DEMONSTRATIVO DET
            ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    WHERE EST.ID = 3
),
ref_atu AS (
    /* a referencia do ano da COLUNA ATUAL, e so dela.
       O CASE normaliza os dois formatos de ANO_REFERENCIA.
       O LIMIT 1 impede que um ano com dois registros dobre os vinculos. */
    SELECT
        L.ID_DET,
        (SELECT R.ID
           FROM DET_DEMONSTRATIVO_REFERENCIA R
          WHERE R.ID_DET_DEMONSTRATIVO = L.ID_DET
            AND (CASE WHEN R.ANO_REFERENCIA > 10000
                      THEN FLOOR(R.ANO_REFERENCIA / 10000)
                      ELSE R.ANO_REFERENCIA END) = (SELECT ANO_ATU FROM D)
          ORDER BY R.ANO_REFERENCIA DESC, R.ID DESC
          LIMIT 1) AS ID_REF
    FROM linhas L
),
ref_ant AS (
    SELECT
        L.ID_DET,
        (SELECT R.ID
           FROM DET_DEMONSTRATIVO_REFERENCIA R
          WHERE R.ID_DET_DEMONSTRATIVO = L.ID_DET
            AND (CASE WHEN R.ANO_REFERENCIA > 10000
                      THEN FLOOR(R.ANO_REFERENCIA / 10000)
                      ELSE R.ANO_REFERENCIA END) = (SELECT ANO_ANT FROM D)
          ORDER BY R.ANO_REFERENCIA DESC, R.ID DESC
          LIMIT 1) AS ID_REF
    FROM linhas L
),
regras_atu AS (
    SELECT DISTINCT L.ORDEM, L.NOME_GRUPO, C.PADRAO_CTACTB, C.SINAL
    FROM linhas L
    INNER JOIN ref_atu RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
regras_ant AS (
    SELECT DISTINCT L.ORDEM, L.NOME_GRUPO, C.PADRAO_CTACTB, C.SINAL
    FROM linhas L
    INNER JOIN ref_ant RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
exc_atu AS (
    /* com a ORDEM: sem ela, conta excluida de UMA linha era excluida de
       TODAS. A estrutura 10 tem 35 dessas regras. */
    SELECT DISTINCT L.ORDEM, C.PADRAO_CTACTB
    FROM linhas L
    INNER JOIN ref_atu RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB_EXC C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
exc_ant AS (
    SELECT DISTINCT L.ORDEM, C.PADRAO_CTACTB
    FROM linhas L
    INNER JOIN ref_ant RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB_EXC C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
qtd_atu AS (
    SELECT ORDEM, NOME_GRUPO, COUNT(*) AS QTD FROM regras_atu
    GROUP BY ORDEM, NOME_GRUPO
),
qtd_ant AS (
    SELECT ORDEM, NOME_GRUPO, COUNT(*) AS QTD FROM regras_ant
    GROUP BY ORDEM, NOME_GRUPO
),
map_atu AS (
    SELECT
        R.ORDEM, R.NOME_GRUPO, R.SINAL, B.valor_atu AS valor,
        /* a particao inclui ORDEM e NOME_GRUPO: a mesma conta pode entrar
           em linhas diferentes, e o desempate por PADRAO_CTACTB deixa o
           resultado estavel entre execucoes */
        ROW_NUMBER() OVER (
            PARTITION BY B.CTACTB, R.ORDEM, R.NOME_GRUPO
            ORDER BY LENGTH(R.PADRAO_CTACTB) DESC, R.PADRAO_CTACTB DESC
        ) AS rn
    FROM dados B
    INNER JOIN regras_atu R ON B.CTACTB LIKE R.PADRAO_CTACTB
    LEFT JOIN exc_atu E ON E.ORDEM = R.ORDEM
                       AND B.CTACTB LIKE E.PADRAO_CTACTB
    WHERE E.PADRAO_CTACTB IS NULL
),
map_ant AS (
    SELECT
        R.ORDEM, R.NOME_GRUPO, R.SINAL, B.valor_ant AS valor,
        ROW_NUMBER() OVER (
            PARTITION BY B.CTACTB, R.ORDEM, R.NOME_GRUPO
            ORDER BY LENGTH(R.PADRAO_CTACTB) DESC, R.PADRAO_CTACTB DESC
        ) AS rn
    FROM dados B
    INNER JOIN regras_ant R ON B.CTACTB LIKE R.PADRAO_CTACTB
    LEFT JOIN exc_ant E ON E.ORDEM = R.ORDEM
                       AND B.CTACTB LIKE E.PADRAO_CTACTB
    WHERE E.PADRAO_CTACTB IS NULL
),
tot_atu AS (
    SELECT ORDEM, NOME_GRUPO, SUM(valor * SINAL) AS V
    FROM map_atu WHERE rn = 1 GROUP BY ORDEM, NOME_GRUPO
),
tot_ant AS (
    SELECT ORDEM, NOME_GRUPO, SUM(valor * SINAL) AS V
    FROM map_ant WHERE rn = 1 GROUP BY ORDEM, NOME_GRUPO
)
SELECT
    L.ORDEM        AS HIERARQUIA,
    L.NOME_GRUPO   AS DESCRICAO,
    L.COD_NOTA_EXPLICATIVA AS NOTA,
    /* ano sem cadastro -> NULL naquela coluna, para a tela mostrar
       "sem cadastro". Com cadastro -> o valor, e 0 e zero de verdade. */
    CASE WHEN QA.QTD IS NULL THEN NULL ELSE IFNULL(TA.V, 0) END AS VALOR_ANO_ATUAL,
    CASE WHEN QB.QTD IS NULL THEN NULL ELSE IFNULL(TB.V, 0) END AS VALOR_ANO_ANTERIOR,
    IFNULL(QA.QTD, 0)                           AS QTD_CONTAS_ATU,
    IFNULL(QB.QTD, 0)                           AS QTD_CONTAS_ANT,
    CASE WHEN QA.QTD IS NULL THEN 1 ELSE 0 END  AS SEM_CADASTRO_ATU,
    CASE WHEN QB.QTD IS NULL THEN 1 ELSE 0 END  AS SEM_CADASTRO_ANT,
    (SELECT ANO_ATU FROM D)                     AS ANO_ATU,
    (SELECT ANO_ANT FROM D)                     AS ANO_ANT,
    (SELECT DT_INI  FROM D)                     AS DATA_INICIO_USADA,
    (SELECT DT_FIM  FROM D)                     AS DATA_FIM_USADA,
    /* ------------------------------------------------------------------
       Dois contadores para a tela poder dizer POR QUE veio zerado. Sem
       eles, "tudo 0,00" tem tres causas possiveis e nenhuma aparece:

         a) o parametro :VAR_EMPRESA_DRE nao esta declarado no componente,
            entao CODEMP = NULL e o balancete nao casa com nada;
         b) a empresa selecionada nao tem lancamento no balancete;
         c) a empresa tem lancamento, mas nao no periodo filtrado.

       LANC_EMPRESA = 0  -> caso (a) ou (b)
       LANC_EMPRESA > 0 e LANC_PERIODO = 0 -> caso (c)
       ------------------------------------------------------------------ */
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE)                       AS LANC_EMPRESA,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE
        AND DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                 AND (SELECT DT_FIM FROM D)) AS LANC_PERIODO,
    /* ------------------------------------------------------------------
       1 = as duas datas do filtro chegaram e foram interpretadas.
       0 = pelo menos uma nao chegou.

       Isto existe porque o fallback para CURDATE() era SILENCIOSO: com a
       data fim ausente a query passava a calcular o saldo de hoje e o de
       hoje-menos-1-ano, devolvia numeros plausiveis, e nada na tela dizia
       que o filtro tinha sido ignorado. Foi assim que o BP Externo mostrou
       107.338.229,09 (Passivo+PL em 29/09/2025) para quem havia filtrado o
       ano de 2025 inteiro, e por isso a coluna de 2024 nunca aparecia.

       A causa e o parametro nao declarado no componente: a plataforma so
       substitui um :VAR_* que esteja declarado. Com a tela nova, DATAS_OK=0
       interrompe e diz isso, em vez de mostrar numero errado.
       ------------------------------------------------------------------ */
    CASE WHEN (SELECT DT_INI_IN FROM PARAMS) IS NULL
            OR (SELECT DT_FIM_IN FROM PARAMS) IS NULL
         THEN 0 ELSE 1 END                                  AS DATAS_OK
FROM linhas L
LEFT JOIN qtd_atu QA ON QA.ORDEM = L.ORDEM AND QA.NOME_GRUPO = L.NOME_GRUPO
LEFT JOIN qtd_ant QB ON QB.ORDEM = L.ORDEM AND QB.NOME_GRUPO = L.NOME_GRUPO
LEFT JOIN tot_atu TA ON TA.ORDEM = L.ORDEM AND TA.NOME_GRUPO = L.NOME_GRUPO
LEFT JOIN tot_ant TB ON TB.ORDEM = L.ORDEM AND TB.NOME_GRUPO = L.NOME_GRUPO
ORDER BY L.ORDEM
LIMIT 500;
