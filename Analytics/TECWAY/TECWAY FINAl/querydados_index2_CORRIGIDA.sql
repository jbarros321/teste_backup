/* =====================================================================
   BP TECWAY - queryDados CORRIGIDA para o filtro de PERIODO
   VERSAO PARA A TELA index2.html - BP INTERNO
   tenant_61302 | ESTR_DEMONSTRATIVOS.ID = 2 (Interno)

   Diferencas em relacao a querydados_CORRIGIDA.sql:
       - EST.ID = 2 (Interno) em vez de 1
       - as colunas de valor saem como ANO_ATUAL / ANO_ANTERIOR, que e como
         a index2.html as procura (a index1.html usa VLRLANC_ATU/_ANT)

   A query original desta tela tinha, alem dos defeitos listados abaixo, um
   a mais: ela NAO usava DET_DEMONSTRATIVO_CTACTB_EXC, ou seja ignorava as
   regras de exclusao de conta por completo. Aqui elas valem.

   Parametros do componente:
       :VAR_DATA_INICIO   data inicial do filtro da tela
       :VAR_DATA_FIM      data final   do filtro da tela
       :VAR_EMPRESA_DRE   CODEMP

   ATENCAO: nao use comentario de linha (--) nesta query. O componente
   costuma gravar tudo numa unica linha, e um -- comentaria todo o resto.
   Aqui so ha comentario de bloco, que sobrevive ao achatamento.

   ---------------------------------------------------------------------
   CADA COLUNA USA O CADASTRO DO SEU PROPRIO ANO
   ---------------------------------------------------------------------
   A tela tem duas colunas: a do periodo filtrado e a comparativa de um
   ano antes. Cada uma resolve o vinculo conta x linha no SEU ano:

       ANO_ATUAL  -> cadastro do ano de :VAR_DATA_FIM
       ANO_ANTERIOR  -> cadastro do ano de :VAR_DATA_FIM menos 1

   Nao ha fallback de um ano para o outro. Se o ano daquela coluna nao tem
   conta vinculada, aquela coluna vem NULL e a tela mostra "sem cadastro";
   a outra coluna continua trazendo valor normalmente.

   Com o cadastro de hoje (28/09/2026) e o filtro em 2026, o BP Externo
   fica assim: coluna de 2026 sem cadastro nas 24 linhas, coluna de 2025
   com as 24 linhas preenchidas. Isso e o retrato correto - o cadastro do
   ano 2026 e que ainda nao foi feito.

   Uma versao anterior desta query caia no ano anterior quando o ano do
   filtro nao tinha vinculo, e usava esse mapeamento nas DUAS colunas, em
   silencio. Foi assim que "Adiantamento de clientes" mostrou valor em
   2026 sem ter conta cadastrada em 2026. Esse fallback foi REMOVIDO.

   A linha sem cadastro continua aparecendo, marcada, em vez de sumir:
   linha que desaparece muda o total do balanco sem explicar por que.

   ---------------------------------------------------------------------
   O QUE ESTAVA ERRADO NA VERSAO ORIGINAL (conferido no tenant_61302)
   ---------------------------------------------------------------------

   1) ANO_REFERENCIA ESTA COM DOIS FORMATOS NA MESMA COLUNA.
      Valores reais: 2024, 2025, 2026 (4 digitos) e 20231201, 20241201,
      20251201, 20261201 (AAAAMMDD). Dos 356 registros, 352 sao AAAAMMDD
      e 4 sao ano puro.

      A comparacao `ANO_REFERENCIA <= YEAR(:VAR_DATA_FIM)` virava
      `20251201 <= 2026`, que e FALSO. Logo MAX(...) devolvia NULL, o
      `DETREF.ANO_REFERENCIA = NULL` nunca casava e a linha desaparecia
      sem deixar rastro: 24 de 24 linhas do BP Externo em ZERO, e 36 de
      37 do Interno. Era este o sintoma de "dados incompletos".

      Correcao: normalizar para ano antes de comparar (o CASE do
      ANO_REF_NORM). Vale padronizar a coluna no cadastro - enquanto os
      dois formatos convivem, qualquer query nova escrita sem essa
      normalizacao volta a zerar a tela, e sem dar erro.

   2) BETWEEN NO BALANCO PATRIMONIAL DAVA MOVIMENTO, NAO SALDO.
      IMP_BASE_BALANCETE guarda MOVIMENTO por competencia. O saldo de uma
      conta patrimonial e o acumulado desde o inicio, nao o do periodo.
      `REFERENCIA BETWEEN :VAR_DATA_INICIO AND :VAR_DATA_FIM` devolvia so
      a movimentacao da janela - numero errado em toda linha de balanco.
      Agora: saldo = acumulado ATE a data fim da coluna. O BETWEEN ficou
      apenas onde a linha e de fluxo (3.3.2).

   3) AS EXCLUSOES PERDERAM A LINHA (ORDEM).
      `regras_excluidas` selecionava so PADRAO_CTACTB, sem ORDEM, e o
      LEFT JOIN casava so pela conta. Uma conta excluida de UMA linha
      passava a ser excluida de TODAS. Sao 35 regras, todas da estrutura
      10 (DFC) hoje, mas o defeito vale para qualquer estrutura.

   4) NADA GARANTIA UMA REFERENCIA SO POR ANO (risco, nao defeito atual).
      Hoje nenhum par (linha, ano normalizado) tem mais de uma referencia,
      entao nao ha duplicacao. Mas a coluna aceita 2025 e 20251201, que
      normalizam para o mesmo ano - e foi assim que Fornecedores
      multiplicou por 12 no tenant anterior. O LIMIT 1 e a trava.

   5) GRUPO SEM CONTA CASADA VOLTAVA NULL POR ACIDENTE.
      `SUM(IFNULL(M.valor_atu,0) * M.SINAL)` dava NULL quando nao havia
      linha casada, porque M.SINAL e NULL - celula em branco sem que
      ninguem soubesse se era zero ou falta de cadastro. Agora os dois
      casos sao distintos: conta cadastrada que somou zero vem 0; coluna
      sem cadastro no ano vem NULL, com a marca correspondente.

   6) ROW_NUMBER SEM NOME_GRUPO E SEM DESEMPATE ESTAVEL.
      A particao era (CTACTB, ORDEM) mas o join usa (ORDEM, NOME_GRUPO),
      e o ORDER BY so tinha LENGTH - empate resolvido ao acaso, resultado
      podendo variar entre execucoes.

   ---------------------------------------------------------------------
   COLUNAS DEVOLVIDAS
   ---------------------------------------------------------------------
       ORDEM, NOME_GRUPO, COD_NOTA_EXPLICATIVA
       ANO_ATUAL / ANO_ANTERIOR    valor, ou NULL se o ano daquela
                                    coluna nao tem conta vinculada
       QTD_CONTAS_ATU / _ANT        contas vinculadas em cada ano
       SEM_CADASTRO_ATU / _ANT      1 = aquele ano nao tem cadastro
       ANO_ATU / ANO_ANT            os anos de cada coluna
       DATA_INICIO_USADA, DATA_FIM_USADA
   ===================================================================== */

WITH PARAMS AS (
    SELECT
        /* aceita 'AAAA-MM-DD' e 'DD/MM/AAAA', que e como a plataforma
           costuma devolver o filtro de data */
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
       data inicio usa 01/01 do ano da data fim. Se vierem invertidas,
       troca - em vez de devolver um periodo negativo. */
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
base_bal AS (
    SELECT
        CTACTB,
        /* SALDO acumulado ate a data fim - e isto que vai nas linhas de
           balanco. Antes era BETWEEN, que dava so o movimento. */
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS saldo_atu,
        /* FLUXO do periodo - so para a linha de resultado 3.3.2 */
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                          AND (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS fluxo_atu,
        /* SALDO DE ABERTURA do periodo - so para a linha 3.3.1 */
        SUM(CASE WHEN DATE(REFERENCIA) < (SELECT DT_INI FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS abertura_atu,
        /* os mesmos tres cortes deslocados um ano, para a coluna
           comparativa */
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS saldo_ant,
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI_ANT FROM D)
                                          AND (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS fluxo_ant,
        SUM(CASE WHEN DATE(REFERENCIA) < (SELECT DT_INI_ANT FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS abertura_ant
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
    GROUP BY CTACTB
),
linhas AS (
    /* a lista de linhas vem da ESTRUTURA, nao dos vinculos. E o que faz a
       linha sem cadastro continuar aparecendo, marcada, em vez de sumir
       da tela e mexer no total sem explicacao. */
    SELECT
        DET.ID   AS ID_DET,
        DET.ORDEM,
        DET.NOME_GRUPO,
        DET.COD_NOTA_EXPLICATIVA
    FROM ESTR_DEMONSTRATIVOS EST
    INNER JOIN DET_DEMONSTRATIVO DET
            ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    WHERE EST.ID = 2
      /* O filtro `AND TRIM(DET.ORDEM) <> '3.3.3'` foi REMOVIDO. Ele vinha da
         query original e excluia a linha 3.3.3 Lucro/Prejuizos acumulados,
         cujo saldo em 31/12/2025 e 52.561.286,48. Sem ela o patrimonio
         liquido ficava incompleto e o balanco nao fechava por 6.842.268,19
         (Ativo 98.776.914,99 contra Passivo+PL 91.934.646,80). */
),
ref_atu AS (
    /* a referencia do ano da COLUNA ATUAL, e so dela.

       O CASE normaliza os dois formatos de ANO_REFERENCIA (2025 e
       20251201) para o ano - sem isso a comparacao com YEAR() e sempre
       falsa nos registros AAAAMMDD e a linha desaparece.

       O LIMIT 1 garante uma referencia so, mesmo que o ano venha a ter
       dois registros (2025 e 20251201), o que dobraria os vinculos. */
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
    /* a mesma coisa para o ano da COLUNA COMPARATIVA */
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
    SELECT DISTINCT L.ORDEM, L.NOME_GRUPO,
           C.PADRAO_CTACTB, C.SINAL
    FROM linhas L
    INNER JOIN ref_atu RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
regras_ant AS (
    SELECT DISTINCT L.ORDEM, L.NOME_GRUPO,
           C.PADRAO_CTACTB, C.SINAL
    FROM linhas L
    INNER JOIN ref_ant RV ON RV.ID_DET = L.ID_DET
    INNER JOIN DET_DEMONSTRATIVO_CTACTB C
            ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
),
exc_atu AS (
    /* a ORDEM voltou: sem ela uma conta excluida de uma linha era
       excluida de todas */
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
    SELECT ORDEM, NOME_GRUPO, COUNT(*) AS QTD
    FROM regras_atu GROUP BY ORDEM, NOME_GRUPO
),
qtd_ant AS (
    SELECT ORDEM, NOME_GRUPO, COUNT(*) AS QTD
    FROM regras_ant GROUP BY ORDEM, NOME_GRUPO
),
map_atu AS (
    SELECT
        R.ORDEM, R.NOME_GRUPO, R.SINAL,
        /* TODAS as linhas usam o SALDO acumulado, inclusive as 3.3.x.

           Antes 3.3.1 usava o saldo de abertura e 3.3.2 o fluxo do periodo,
           herdado da query original. Conferido contra os dados: o balanco do
           BP Interno so fecha com as cinco linhas do PL em saldo -
           5.000.000,00 + 0,00 + 911.300,74 - 37.941.546,46 + 52.561.286,48
           = 20.531.040,76, que e exatamente o PL necessario para igualar
           Ativo 98.776.914,99. Testei as tres leituras em todas as
           combinacoes possiveis e nenhuma outra fecha. */
        B.saldo_atu AS valor,
        ROW_NUMBER() OVER (
            PARTITION BY B.CTACTB, R.ORDEM, R.NOME_GRUPO
            ORDER BY LENGTH(R.PADRAO_CTACTB) DESC, R.PADRAO_CTACTB DESC
        ) AS rn
    FROM base_bal B
    INNER JOIN regras_atu R ON B.CTACTB LIKE R.PADRAO_CTACTB
    LEFT JOIN exc_atu E ON E.ORDEM = R.ORDEM
                       AND B.CTACTB LIKE E.PADRAO_CTACTB
    WHERE E.PADRAO_CTACTB IS NULL
),
map_ant AS (
    SELECT
        R.ORDEM, R.NOME_GRUPO, R.SINAL,
        B.saldo_ant AS valor,
        ROW_NUMBER() OVER (
            PARTITION BY B.CTACTB, R.ORDEM, R.NOME_GRUPO
            ORDER BY LENGTH(R.PADRAO_CTACTB) DESC, R.PADRAO_CTACTB DESC
        ) AS rn
    FROM base_bal B
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
    L.ORDEM,
    L.NOME_GRUPO,
    L.COD_NOTA_EXPLICATIVA,
    /* ano sem cadastro -> NULL naquela coluna, para a tela mostrar
       "sem cadastro". Com cadastro -> o valor, e 0 e zero de verdade,
       nao falta de dado. */
    CASE WHEN QA.QTD IS NULL THEN NULL ELSE IFNULL(TA.V, 0) END AS ANO_ATUAL,
    CASE WHEN QB.QTD IS NULL THEN NULL ELSE IFNULL(TB.V, 0) END AS ANO_ANTERIOR,
    IFNULL(QA.QTD, 0)                             AS QTD_CONTAS_ATU,
    IFNULL(QB.QTD, 0)                             AS QTD_CONTAS_ANT,
    CASE WHEN QA.QTD IS NULL THEN 1 ELSE 0 END    AS SEM_CADASTRO_ATU,
    CASE WHEN QB.QTD IS NULL THEN 1 ELSE 0 END    AS SEM_CADASTRO_ANT,
    (SELECT ANO_ATU FROM D)                       AS ANO_ATU,
    (SELECT ANO_ANT FROM D)                       AS ANO_ANT,
    (SELECT DT_INI  FROM D)                       AS DATA_INICIO_USADA,
    (SELECT DT_FIM  FROM D)                       AS DATA_FIM_USADA,
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
