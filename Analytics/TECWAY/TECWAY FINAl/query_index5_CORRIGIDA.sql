/* =====================================================================
   DFC EXTERNO - query da tela index.html ("Relatorio DFC")
   tenant_61302 | ESTR_DEMONSTRATIVOS.ID = 10

   Parametros do componente:
       :VAR_DATA_INICIO   data inicial do filtro da tela
       :VAR_DATA_FIM      data final   do filtro da tela
       :VAR_EMPRESA_DRE   CODEMP

   ATENCAO: nao use comentario de linha (--) nesta query. O componente
   grava tudo numa unica linha, e um -- comentaria todo o resto.

   ---------------------------------------------------------------------
   O QUE FOI PRESERVADO DA LOGICA DA DFC
   ---------------------------------------------------------------------
   Esta tela nao e um balanco nem uma DRE simples; ela tem regras
   proprias, e TODAS continuam valendo, sem alteracao de semantica:

     - DET.VARIACAO = 'Sim'  -> a linha e a VARIACAO de saldo entre os dois
       cortes: acum_anterior - acum_atual.
     - ORDEM '5.1' e '5.2'   -> saldos acumulados (caixa inicial e final),
       nao fluxo do periodo.
     - ORDEM '1.9.1'         -> quando o valor calculado da zero, cai para
       a tabela OUTROS_DFC (lancamento manual).
     - exclusoes             -> aqui elas SUBTRAEM valor da linha
       (ABS(VLRLANC) * SINAL), diferente do BP, onde a conta e apenas
       desconsiderada. A estrutura 10 e a unica com regras de exclusao
       cadastradas: 35 no total.

   ---------------------------------------------------------------------
   O QUE ESTAVA ERRADO
   ---------------------------------------------------------------------

   1) ANO_REFERENCIA COM DOIS FORMATOS NA MESMA COLUNA.
      `ANO_REFERENCIA <= YEAR(:VAR_DATA_FIM)` virava `20251201 <= 2025`,
      que e FALSO. MAX(...) devolvia NULL, o `= NULL` nao casava e a linha
      desaparecia. Na estrutura 10 TODAS as referencias sao AAAAMMDD
      (20231201, 20241201, 20251201), entao a tela vinha VAZIA - nao
      parcialmente, inteira. Aparecia duas vezes na query original
      (base_exclusao e base_valores).

      Correcao: normalizar para ano antes de comparar.

   2) AS DATAS DO FILTRO NAO ERAM INTERPRETADAS.
      A query comparava `REFERENCIA BETWEEN :VAR_DATA_INICIO AND ...` com o
      parametro cru. A plataforma entrega a data como 'DD/MM/AAAA
      HH:MM:SS', que nao compara corretamente com uma coluna DATETIME.
      Agora as duas datas passam por um parser que aceita 'DD/MM/AAAA' e
      'AAAA-MM-DD', com ou sem hora (o LEFT(...,10) corta a hora).

   3) UM CADASTRO SO PARA AS DUAS COLUNAS.
      A coluna do periodo e a comparativa usavam o mesmo ano de cadastro.
      Agora cada uma resolve o seu: a atual no ano de :VAR_DATA_FIM, a
      comparativa no ano anterior. Sem fallback - se o ano daquela coluna
      nao tem conta vinculada, a coluna vem NULL e a tela mostra isso, em
      vez de usar o mapeamento de outro exercicio em silencio.

   4) DIVISAO POR 1000.
      A query dividia tudo por 1000, e a tela nao desfaz - ou seja o
      relatorio saia em milhares. Removido: valores completos.

   ---------------------------------------------------------------------
   SITUACAO DO CADASTRO (conferido em 28/09/2026)
   ---------------------------------------------------------------------
   A estrutura 10 tem os anos 2023, 2024 e 2025 - NAO tem 2026. Com o
   filtro em 2026 a coluna atual vem "sem cadastro" e a comparativa (2025)
   vem preenchida. Com o filtro em 2025, as duas vem completas.

   As 35 regras de exclusao estao distribuidas assim:
       2.1 Contas a receber            2024, 2025
       2.7 Obrigacoes tributarias      2024 (4), 2025 (6)
       3.3 Aquisicao de imobilizado    2024 (7), 2025 (7)
       3.4 Aquisicao de intangivel     2024, 2025
       4.1 Emprestimos e Financiam.    2023, 2024, 2025
       4.4 Lucros distribuidos         2025

   COLUNAS DEVOLVIDAS
       ORDEM, DESCRICAO, VARIACAO
       VALOR_ANO_ATUAL / VALOR_ANO_ANTERIOR   valor, ou NULL se o ano
                                              daquela coluna nao tem conta
       QTD_CONTAS_ATU / _ANT, SEM_CADASTRO_ATU / _ANT
       ANO_ATU, ANO_ANT, DATA_INICIO_USADA, DATA_FIM_USADA
       LANC_EMPRESA, LANC_PERIODO, DATAS_OK
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
    SELECT
        LEAST(DT_INI, DT_FIM)                               AS DT_INI,
        GREATEST(DT_INI, DT_FIM)                            AS DT_FIM,
        YEAR(GREATEST(DT_INI, DT_FIM))                      AS ANO_ATU,
        YEAR(GREATEST(DT_INI, DT_FIM)) - 1                  AS ANO_ANT,
        DATE_SUB(LEAST(DT_INI, DT_FIM),    INTERVAL 1 YEAR) AS DT_INI_ANT,
        DATE_SUB(GREATEST(DT_INI, DT_FIM), INTERVAL 1 YEAR) AS DT_FIM_ANT,
        /* a DFC precisa de dois anos atras: as linhas 5.1 e as de VARIACAO
           comparam saldo de abertura com saldo de fechamento */
        DATE_SUB(GREATEST(DT_INI, DT_FIM), INTERVAL 2 YEAR) AS DT_FIM_ANT2
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
        /* saldos acumulados nos tres cortes */
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END) AS acum_atu,
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM_ANT FROM D)
                 THEN VLRLANC ELSE 0 END) AS acum_ant,
        SUM(CASE WHEN DATE(REFERENCIA) <= (SELECT DT_FIM_ANT2 FROM D)
                 THEN VLRLANC ELSE 0 END) AS acum_ant2
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
    /* a referencia do ano da COLUNA ATUAL, e so dela. O CASE normaliza os
       dois formatos de ANO_REFERENCIA; o LIMIT 1 impede que um ano com
       dois registros dobre os vinculos. */
    SELECT L.ID_DET,
        (SELECT R.ID FROM DET_DEMONSTRATIVO_REFERENCIA R
          WHERE R.ID_DET_DEMONSTRATIVO = L.ID_DET
            AND (CASE WHEN R.ANO_REFERENCIA > 10000
                      THEN FLOOR(R.ANO_REFERENCIA / 10000)
                      ELSE R.ANO_REFERENCIA END) = (SELECT ANO_ATU FROM D)
          ORDER BY R.ANO_REFERENCIA DESC, R.ID DESC LIMIT 1) AS ID_REF
    FROM linhas L
),
ref_ant AS (
    SELECT L.ID_DET,
        (SELECT R.ID FROM DET_DEMONSTRATIVO_REFERENCIA R
          WHERE R.ID_DET_DEMONSTRATIVO = L.ID_DET
            AND (CASE WHEN R.ANO_REFERENCIA > 10000
                      THEN FLOOR(R.ANO_REFERENCIA / 10000)
                      ELSE R.ANO_REFERENCIA END) = (SELECT ANO_ANT FROM D)
          ORDER BY R.ANO_REFERENCIA DESC, R.ID DESC LIMIT 1) AS ID_REF
    FROM linhas L
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
    /* os cinco agregados por linha, para a coluna ATUAL */
    SELECT
        R.ORDEM,
        SUM(IFNULL(B.ano_atu,   0) * R.SINAL) AS ano_v,
        SUM(IFNULL(B.acum_atu,  0) * R.SINAL) AS acum_v,
        SUM(IFNULL(B.acum_ant,  0) * R.SINAL) AS acum_ant_v
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
        SUM(IFNULL(B.acum_ant2, 0) * R.SINAL) AS acum_ant_v
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
    /* lancamento manual da linha 1.9.1 */
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
    /* ano sem cadastro -> NULL naquela coluna. Com cadastro, aplica as
       regras proprias da DFC e subtrai a exclusao. */
    /* A linha 1.9.1 Outros e MANUAL: vem da tabela OUTROS_DFC e nao tem
       conta contabil vinculada por desenho. Por isso ela e isenta da regra
       de "sem cadastro" - senao a trava a apagaria justamente na unica
       linha que nunca teve vinculo. As outras duas linhas sem vinculo em
       2025 (1.2 Baixa de ativo imobilizado e 3.1 Investimentos
       temporarios) continuam marcadas, porque nelas a ausencia de conta e
       falta de cadastro mesmo. */
    CASE WHEN QA.QTD IS NULL AND TRIM(L.ORDEM) <> '1.9.1' THEN NULL ELSE
        (CASE
            WHEN TRIM(L.ORDEM) = '1.9.1' AND IFNULL(VA.ano_v, 0) = 0
                 THEN (SELECT v_atu FROM outros)
            WHEN TRIM(L.ORDEM) = '5.1' THEN VA.acum_ant_v
            WHEN TRIM(L.ORDEM) = '5.2' THEN VA.acum_v
            WHEN L.VARIACAO = 'Sim'    THEN VA.acum_ant_v - VA.acum_v
            ELSE VA.ano_v
        END) - IFNULL(EA.V, 0)
    END AS VALOR_ANO_ATUAL,
    CASE WHEN QB.QTD IS NULL AND TRIM(L.ORDEM) <> '1.9.1' THEN NULL ELSE
        (CASE
            WHEN TRIM(L.ORDEM) = '1.9.1' AND IFNULL(VB.ano_v, 0) = 0
                 THEN (SELECT v_ant FROM outros)
            WHEN TRIM(L.ORDEM) = '5.1' THEN VB.acum_ant_v
            WHEN TRIM(L.ORDEM) = '5.2' THEN VB.acum_v
            WHEN L.VARIACAO = 'Sim'    THEN VB.acum_ant_v - VB.acum_v
            ELSE VB.ano_v
        END) - IFNULL(EB.V, 0)
    END AS VALOR_ANO_ANTERIOR,
    IFNULL(QA.QTD, 0)                          AS QTD_CONTAS_ATU,
    IFNULL(QB.QTD, 0)                          AS QTD_CONTAS_ANT,
    CASE WHEN QA.QTD IS NULL AND TRIM(L.ORDEM) <> '1.9.1' THEN 1 ELSE 0 END AS SEM_CADASTRO_ATU,
    CASE WHEN QB.QTD IS NULL AND TRIM(L.ORDEM) <> '1.9.1' THEN 1 ELSE 0 END AS SEM_CADASTRO_ANT,
    (SELECT ANO_ATU FROM D)                    AS ANO_ATU,
    (SELECT ANO_ANT FROM D)                    AS ANO_ANT,
    (SELECT DT_INI  FROM D)                    AS DATA_INICIO_USADA,
    (SELECT DT_FIM  FROM D)                    AS DATA_FIM_USADA,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE)         AS LANC_EMPRESA,
    (SELECT COUNT(*) FROM IMP_BASE_BALANCETE
      WHERE CODEMP = :VAR_EMPRESA_DRE
        AND DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                 AND (SELECT DT_FIM FROM D)) AS LANC_PERIODO,
    /* 1 = as duas datas chegaram. 0 = a query calcularia sobre a data de
       hoje, e a tela deve PARAR em vez de mostrar numero plausivel. */
    CASE WHEN (SELECT DT_INI_IN FROM PARAMS) IS NULL
            OR (SELECT DT_FIM_IN FROM PARAMS) IS NULL
         THEN 0 ELSE 1 END                     AS DATAS_OK
FROM linhas L
LEFT JOIN qtd_atu QA ON QA.ORDEM = L.ORDEM
LEFT JOIN qtd_ant QB ON QB.ORDEM = L.ORDEM
LEFT JOIN val_atu VA ON VA.ORDEM = L.ORDEM
LEFT JOIN val_ant VB ON VB.ORDEM = L.ORDEM
LEFT JOIN exc_atu EA ON EA.ORDEM = L.ORDEM
LEFT JOIN exc_ant EB ON EB.ORDEM = L.ORDEM
ORDER BY L.ORDEM
LIMIT 500;
