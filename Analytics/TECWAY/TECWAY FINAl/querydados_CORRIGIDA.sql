/* =====================================================================
   BP TECWAY - queryDados CORRIGIDA para o filtro de PERIODO
   tenant_61302 | ESTR_DEMONSTRATIVOS.ID = 1 (Externo)
                  para o BP Interno, troque os tres "EST.ID = 1" por 2.

   Parametros do componente:
       :VAR_DATA_INICIO   data inicial do filtro da tela
       :VAR_DATA_FIM      data final   do filtro da tela
       :VAR_EMPRESA_DRE   CODEMP

   REGRA: o VINCULO conta x linha continua sendo por ANO; as DATAS do
   filtro definem o corte do balancete. O ano usado e o de :VAR_DATA_FIM.

   ATENCAO: nao use comentario de linha (--) nesta query. O componente
   costuma gravar tudo numa unica linha, e um -- comentaria todo o resto.
   Aqui so ha comentario de bloco, que sobrevive ao achatamento.

   ---------------------------------------------------------------------
   O QUE ESTAVA ERRADO NA VERSAO ANTERIOR (conferido no tenant_61302 em
   28/09/2026, CODEMP 999, periodo 01/01/2026 a 31/12/2026)
   ---------------------------------------------------------------------

   1) ANO_REFERENCIA ESTA COM DOIS FORMATOS NA MESMA COLUNA.
      Valores reais: 2024, 2025, 2026 (4 digitos) e 20231201, 20241201,
      20251201, 20261201 (AAAAMMDD). Dos 356 registros, 352 sao AAAAMMDD
      e 4 sao ano puro.

      A comparacao `ANO_REFERENCIA <= YEAR(:VAR_DATA_FIM)` virava
      `20251201 <= 2026`, que e FALSO. Logo MAX(...) devolvia NULL, o
      `DETREF.ANO_REFERENCIA = NULL` nunca casava e a linha desaparecia:

          BP Externo  -> 24 de 24 linhas em ZERO
          BP Interno  -> 36 de 37 linhas em ZERO

      Sobrevivia so Fornecedores do Interno, a unica linha cujo ano esta
      gravado como 2026. Era este o sintoma de "dados incompletos".

      Correcao: normalizar para ano antes de comparar, aceitando os dois
      formatos (ANO_REF_NORM abaixo).

   2) O ANO 2026 ESTA CADASTRADO E VAZIO.
      As 23 referencias 20261201 do BP Externo tem ZERO vinculos de conta,
      e a referencia 2026 do BP Externo (Fornecedores) tambem. Escolher o
      ano mais recente sem olhar vinculo zera a tela de novo, agora em
      silencio. O EXISTS abaixo faz cair em 2025, que tem os vinculos.

   3) BETWEEN NO BALANCO PATRIMONIAL DAVA MOVIMENTO, NAO SALDO.
      IMP_BASE_BALANCETE guarda MOVIMENTO por competencia. O saldo de uma
      conta patrimonial e o acumulado desde o inicio, nao o do periodo.
      `REFERENCIA BETWEEN :VAR_DATA_INICIO AND :VAR_DATA_FIM` devolvia so
      a movimentacao da janela - numero errado em toda linha de balanco.
      Agora: saldo = acumulado ATE :VAR_DATA_FIM. O BETWEEN ficou apenas
      onde a linha e de fluxo (resultado do exercicio).

   4) AS EXCLUSOES PERDERAM A LINHA (ORDEM).
      `regras_excluidas` selecionava so PADRAO_CTACTB, sem ORDEM, e o
      LEFT JOIN casava so pela conta. Uma conta excluida de UMA linha
      passava a ser excluida de TODAS. Sao 35 regras, todas da estrutura
      10 (DFC) hoje, mas o defeito vale para qualquer estrutura. A ORDEM
      voltou para a CTE e para o JOIN.

   5) NADA GARANTIA UMA REFERENCIA SO POR ANO (risco, nao defeito atual).
      Conferi: hoje nenhum par (linha, ano normalizado) tem mais de uma
      referencia, entao a query nao esta duplicando nada neste momento.
      Mas nada no modelo impede - a coluna aceita 2025 e 20251201, que
      normalizam para o mesmo ano - e foi exatamente assim que a linha
      Fornecedores multiplicou por 12 no tenant anterior. O LIMIT 1 na
      ref_vigente e a trava para isso nao voltar em silencio.

   6) GRUPO SEM CONTA CASADA VOLTAVA NULL.
      `SUM(IFNULL(M.valor_atu,0) * M.SINAL)` da NULL quando nao ha linha
      casada, porque M.SINAL e NULL. O IFNULL passou para fora do SUM.

   7) ROW_NUMBER SEM NOME_GRUPO E SEM DESEMPATE ESTAVEL.
      A particao era (CTACTB, ORDEM) mas o join usa (ORDEM, NOME_GRUPO),
      e o ORDER BY so tinha LENGTH - empate resolvido ao acaso, resultado
      podendo variar entre execucoes. Corrigido nos dois pontos.

   ---------------------------------------------------------------------
   RESULTADO DA CORRECAO (CODEMP 999, 01/01/2026 a 31/12/2026)
   ---------------------------------------------------------------------
       BP Externo : 24 de 24 linhas com valor (era 0 de 24)
       BP Interno : 37 de 37 linhas com valor (era 1 de 37)
       Fornecedores do BP Interno = 1.707.138,17, que e exatamente o
       numero do balancete do Sankhya ja usado como referencia.

   PENDENCIAS DE CADASTRO (nao sao da query - ver README_CORRECAO.md):
       - o balanco nao fecha: Ativo 108.490.546,01 contra Passivo+PL
         102.997.105,19 no Externo (diferenca 5.493.440,82)
       - `DET.ORDEM <> '3.3.3'` exclui Lucros/Prejuizos acumulados
       - o ano 2026 precisa dos vinculos de conta
   ===================================================================== */

WITH PARAMS AS (
    SELECT
        /* aceita 'AAAA-MM-DD' e 'DD/MM/AAAA', que e como a plataforma
           costuma devolver o filtro de data */
        CASE WHEN TRIM(COALESCE(:VAR_DATA_INICIO, '')) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
             THEN STR_TO_DATE(TRIM(:VAR_DATA_INICIO), '%d/%m/%Y')
             ELSE DATE(NULLIF(TRIM(COALESCE(:VAR_DATA_INICIO, '')), ''))
        END AS DT_INI_IN,
        CASE WHEN TRIM(COALESCE(:VAR_DATA_FIM, '')) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
             THEN STR_TO_DATE(TRIM(:VAR_DATA_FIM), '%d/%m/%Y')
             ELSE DATE(NULLIF(TRIM(COALESCE(:VAR_DATA_FIM, '')), ''))
        END AS DT_FIM_IN
),
D AS (
    /* filtro vazio nao pode zerar a tela: sem data fim usa hoje, sem
       data inicio usa 01/01 do ano da data fim. Se vierem invertidas,
       troca - em vez de devolver um periodo negativo. */
    SELECT
        LEAST(DT_INI, DT_FIM)                        AS DT_INI,
        GREATEST(DT_INI, DT_FIM)                     AS DT_FIM,
        YEAR(GREATEST(DT_INI, DT_FIM))               AS ANO_FIM,
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
        /* FLUXO do periodo - so para as linhas de resultado */
        SUM(CASE WHEN DATE(REFERENCIA) BETWEEN (SELECT DT_INI FROM D)
                                          AND (SELECT DT_FIM FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS fluxo_atu,
        /* SALDO DE ABERTURA do periodo */
        SUM(CASE WHEN DATE(REFERENCIA) < (SELECT DT_INI FROM D)
                 THEN VLRLANC ELSE 0 END)                        AS abertura_atu,
        /* os mesmos tres cortes deslocados um ano, para a coluna
           comparativa da tela */
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
ref_vigente AS (
    /* UMA referencia por linha do demonstrativo: a do ano mais recente
       ate o ano da data fim QUE TENHA VINCULO DE CONTA.

       O CASE normaliza os dois formatos de ANO_REFERENCIA (2025 e
       20251201) para o ano - sem isso a comparacao com YEAR() e sempre
       falsa nos registros AAAAMMDD e a linha desaparece.

       O EXISTS evita cair no ano cadastrado e vazio (2026 hoje).

       O LIMIT 1 garante uma referencia so, mesmo quando o ano tem dois
       registros (2025 e 20251201), o que evitaria dobrar os vinculos. */
    SELECT
        DET.ID AS ID_DET,
        (SELECT R.ID
           FROM DET_DEMONSTRATIVO_REFERENCIA R
          WHERE R.ID_DET_DEMONSTRATIVO = DET.ID
            AND (CASE WHEN R.ANO_REFERENCIA > 10000
                      THEN FLOOR(R.ANO_REFERENCIA / 10000)
                      ELSE R.ANO_REFERENCIA END) <= (SELECT ANO_FIM FROM D)
            AND EXISTS (SELECT 1
                          FROM DET_DEMONSTRATIVO_CTACTB C
                         WHERE C.ID_DET_DEMONSTRATIVO_REFERENCIA = R.ID)
          ORDER BY (CASE WHEN R.ANO_REFERENCIA > 10000
                         THEN FLOOR(R.ANO_REFERENCIA / 10000)
                         ELSE R.ANO_REFERENCIA END) DESC,
                   R.ANO_REFERENCIA DESC,
                   R.ID DESC
          LIMIT 1) AS ID_REF
    FROM DET_DEMONSTRATIVO DET
),
regras_contas AS (
    SELECT DISTINCT
        DET.ORDEM,
        DET.NOME_GRUPO,
        DET.COD_NOTA_EXPLICATIVA,
        DETCTACTB.PADRAO_CTACTB,
        DETCTACTB.SINAL
    FROM ESTR_DEMONSTRATIVOS EST
    INNER JOIN DET_DEMONSTRATIVO DET
            ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    INNER JOIN ref_vigente RV
            ON RV.ID_DET = DET.ID
    INNER JOIN DET_DEMONSTRATIVO_CTACTB DETCTACTB
            ON DETCTACTB.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
    WHERE EST.ID = 1
      AND TRIM(DET.ORDEM) <> '3.3.3'
),
regras_excluidas AS (
    /* a ORDEM voltou: sem ela uma conta excluida de uma linha era
       excluida de todas */
    SELECT DISTINCT
        DET.ORDEM,
        DETCTACTB_EXC.PADRAO_CTACTB
    FROM ESTR_DEMONSTRATIVOS EST
    INNER JOIN DET_DEMONSTRATIVO DET
            ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    INNER JOIN ref_vigente RV
            ON RV.ID_DET = DET.ID
    INNER JOIN DET_DEMONSTRATIVO_CTACTB_EXC DETCTACTB_EXC
            ON DETCTACTB_EXC.ID_DET_DEMONSTRATIVO_REFERENCIA = RV.ID_REF
    WHERE EST.ID = 1
),
contas_mapeadas AS (
    SELECT
        R.ORDEM,
        R.NOME_GRUPO,
        R.COD_NOTA_EXPLICATIVA,
        R.SINAL,
        /* 3.3.1 e 3.3.2 sao linhas de RESULTADO, nao de saldo:
           3.3.1 = saldo de abertura do periodo
           3.3.2 = fluxo do periodo
           qualquer outra linha = saldo acumulado ate a data fim */
        CASE WHEN TRIM(R.ORDEM) = '3.3.1' THEN B.abertura_atu
             WHEN TRIM(R.ORDEM) = '3.3.2' THEN B.fluxo_atu
             ELSE B.saldo_atu
        END AS valor_atu,
        CASE WHEN TRIM(R.ORDEM) = '3.3.1' THEN B.abertura_ant
             WHEN TRIM(R.ORDEM) = '3.3.2' THEN B.fluxo_ant
             ELSE B.saldo_ant
        END AS valor_ant,
        ROW_NUMBER() OVER (
            PARTITION BY B.CTACTB, R.ORDEM, R.NOME_GRUPO
            ORDER BY LENGTH(R.PADRAO_CTACTB) DESC, R.PADRAO_CTACTB DESC
        ) AS rn
    FROM base_bal B
    INNER JOIN regras_contas R
            ON B.CTACTB LIKE R.PADRAO_CTACTB
    LEFT JOIN regras_excluidas E
           ON E.ORDEM = R.ORDEM
          AND B.CTACTB LIKE E.PADRAO_CTACTB
    WHERE E.PADRAO_CTACTB IS NULL
)
SELECT
    R.ORDEM,
    R.NOME_GRUPO,
    R.COD_NOTA_EXPLICATIVA,
    /* IFNULL por fora do SUM: grupo sem conta casada vale 0, nao NULL */
    IFNULL(SUM(M.valor_atu * M.SINAL), 0) AS VLRLANC_ATU,
    IFNULL(SUM(M.valor_ant * M.SINAL), 0) AS VLRLANC_ANT,
    /* o periodo que a query realmente usou, para a tela mostrar no titulo */
    (SELECT DT_INI FROM D) AS DATA_INICIO_USADA,
    (SELECT DT_FIM FROM D) AS DATA_FIM_USADA,
    (SELECT ANO_FIM FROM D) AS ANO_USADO
FROM (SELECT DISTINCT ORDEM, NOME_GRUPO, COD_NOTA_EXPLICATIVA FROM regras_contas) R
LEFT JOIN contas_mapeadas M
       ON M.ORDEM = R.ORDEM
      AND M.NOME_GRUPO = R.NOME_GRUPO
      AND M.rn = 1
GROUP BY R.ORDEM, R.NOME_GRUPO, R.COD_NOTA_EXPLICATIVA
ORDER BY R.ORDEM
LIMIT 500;
