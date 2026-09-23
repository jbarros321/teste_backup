/* =====================================================================
   10 - QUERY DEFINITIVA DO BP  (substitui o queri1.sql)
   tenant_60677 - ESTR_DEMONSTRATIVOS.ID = 1 (Externo)
                  para o Interno, troque os dois "EST.ID = 1" por 2.

   Resolve de uma vez os quatro problemas:

   1) ANO VAZIO. :VAR_ANO_FILTRO esta sem valor, e comparacao com NULL
      nunca da verdadeiro - a query voltava zero linhas e a tela dizia
      "Ano invalido". Agora os dois filtros sao resolvidos no topo:
          ANO = :VAR_ANO_FILTRO, senao o ano do :VAR_MES_FILTRO
                ('07/2026' -> 2026), senao o ano corrente
          MES = :VAR_MES_FILTRO se for MM/AAAA E do mesmo ANO;
                'Todos', vazio ou ano discordante -> ano inteiro (31/12)
      A tela funciona com o filtro de ano vazio, com o de mes vazio, e
      com os dois preenchidos - inclusive se discordarem.

   2) ANO SEM VINCULO. `DA.ANO = :VAR_ANO_FILTRO` fazia a linha sumir ou
      zerar. Situacao real em 23/09/2026:
          EST 1  BP Externo   2026: 24 linhas, 24 refs, ZERO vinculos
          EST 2  BP Interno   2026: so 1 linha das 38 (Fornecedores)
          EST 3/4/10/11                   2026: nem cadastro de ano existe
      Agora a query escolhe o ano mais recente ATE o ano do filtro QUE
      TENHA VINCULO. O BP Externo cai em 2025 e mostra numero; o
      Fornecedores do Interno fica em 2026, que e onde estao os dados.

   3) DUPLICACAO. A linha 2.1.1 Fornecedores tem 12 referencias em 2026 e
      12 em 2027, uma por mes, cada uma com copia completa dos vinculos.
      O MAX(REFERENCIA) por ano colapsa isso - sem ele, Fornecedores x12.

   4) GRUPO SEM CONTA VOLTAVA NULL. `SUM(IFNULL(M.valor,0) * M.SINAL)`
      da NULL quando nao ha linha casada (0 * NULL = NULL). Agora 0.

   Parametros: :VAR_ANO_FILTRO (pode vir vazio), :VAR_MES_FILTRO
               ('MM/AAAA' ou 'Todos'), :VAR_EMPRESA_DRE
   ===================================================================== */

WITH PARAMS AS (
    SELECT
        /* o filtro de ano, se tiver valor (hoje esta vazio) */
        NULLIF(TRIM(COALESCE(:VAR_ANO_FILTRO, '')), '')            AS ANO_TXT,
        /* o filtro de mes so vale se vier como MM/AAAA. 'Todos',
           vazio ou qualquer outra coisa cai para NULL = ano inteiro. */
        CASE WHEN TRIM(COALESCE(:VAR_MES_FILTRO, '')) REGEXP '^[0-9]{2}/[0-9]{4}$'
             THEN TRIM(:VAR_MES_FILTRO)
        END                                                        AS MES_TXT
),
ANO_RESOLVIDO AS (
    SELECT
        P.MES_TXT,
        /* prioridade: filtro de ano > ano do mes selecionado > ano corrente.
           O filtro de ano manda porque e ele que define o exercicio; o mes
           so entra quando o ano esta vazio - que e a situacao de hoje. */
        CAST(COALESCE(P.ANO_TXT, RIGHT(P.MES_TXT, 4), YEAR(CURDATE())) AS UNSIGNED) AS ANO
    FROM PARAMS P
),
MES_RESOLVIDO AS (
    SELECT
        A.ANO,
        /* se os dois filtros discordarem (ano 2026 + mes 07/2025), o mes e
           ignorado e vale o ano inteiro - em vez de devolver um corte que
           contradiz o ano escolhido. */
        CASE WHEN A.MES_TXT IS NOT NULL
              AND CAST(RIGHT(A.MES_TXT, 4) AS UNSIGNED) = A.ANO
             THEN A.MES_TXT
        END AS MES
    FROM ANO_RESOLVIDO A
),
DATAS AS (
    SELECT
        M.ANO,
        M.MES,
        /* mes escolhido -> ultimo dia dele; sem mes -> 31/12 do ano */
        CASE WHEN M.MES IS NULL
             THEN STR_TO_DATE(CONCAT('31/12/', M.ANO), '%d/%m/%Y')
             ELSE LAST_DAY(STR_TO_DATE(CONCAT('01/', M.MES), '%d/%m/%Y'))
        END AS DATA_FILTRO,
        /* mesmo corte um ano antes, para a coluna comparativa */
        CASE WHEN M.MES IS NULL
             THEN STR_TO_DATE(CONCAT('31/12/', M.ANO - 1), '%d/%m/%Y')
             ELSE LAST_DAY(DATE_SUB(STR_TO_DATE(CONCAT('01/', M.MES), '%d/%m/%Y'), INTERVAL 1 YEAR))
        END AS DATA_FILTRO_ANT
    FROM MES_RESOLVIDO M
),
base_bal AS (
    SELECT
        CTACTB,
        SUM(CASE WHEN REFERENCIA <= (SELECT DATA_FILTRO FROM DATAS)
                 THEN VLRLANC ELSE 0 END) AS total_atu,
        SUM(CASE WHEN YEAR(REFERENCIA) = (SELECT ANO FROM DATAS)
                  AND REFERENCIA <= (SELECT DATA_FILTRO FROM DATAS)
                 THEN VLRLANC ELSE 0 END) AS ytd_atu,
        SUM(CASE WHEN REFERENCIA <= (SELECT DATA_FILTRO_ANT FROM DATAS)
                 THEN VLRLANC ELSE 0 END) AS total_ant,
        SUM(CASE WHEN YEAR(REFERENCIA) = (SELECT ANO FROM DATAS) - 1
                  AND REFERENCIA <= (SELECT DATA_FILTRO_ANT FROM DATAS)
                 THEN VLRLANC ELSE 0 END) AS ytd_ant,
        SUM(CASE WHEN REFERENCIA < MAKEDATE((SELECT ANO FROM DATAS), 1)
                 THEN VLRLANC ELSE 0 END) AS saldo_final_ano_anterior,
        SUM(CASE WHEN REFERENCIA < MAKEDATE((SELECT ANO FROM DATAS) - 1, 1)
                 THEN VLRLANC ELSE 0 END) AS saldo_final_ano_retrasado
    FROM IMP_BASE_BALANCETE
    WHERE CODEMP = :VAR_EMPRESA_DRE
    GROUP BY CTACTB
),
ano_vigente AS (
    /* UM registro de ano por linha: o mais recente, ate o ano escolhido,
       QUE TENHA VINCULO DE CONTA.

       O EXISTS nao e detalhe: o ano 2026 do BP Externo existe para as 24
       linhas, com 24 referencias, e ZERO vinculos - o cadastro de ano foi
       criado e ninguem vinculou as contas. Sem o EXISTS a query escolhe
       esse 2026 vazio e o BP Externo inteiro zera. Com ele, cai no
       cadastro de 2025 (2.044 vinculos) e mostra numero de verdade. */
    SELECT A.ID, A.ID_DET_DEMONSTRATIVO
    FROM DET_DEMONSTRATIVO_ANO A
    WHERE A.ANO = (
        SELECT MAX(A2.ANO)
        FROM DET_DEMONSTRATIVO_ANO A2
        WHERE A2.ID_DET_DEMONSTRATIVO = A.ID_DET_DEMONSTRATIVO
          AND A2.ANO <= (SELECT ANO FROM DATAS)
          AND EXISTS (
                SELECT 1
                FROM DET_DEMONSTRATIVO_REFERENCIA R2
                JOIN DET_DEMONSTRATIVO_CTACTB     C2
                  ON C2.ID_DET_DEMONSTRATIVO_REFERENCIA = R2.ID
                WHERE R2.ID_DET_DEMONSTRATIVO_ANO = A2.ID
              )
    )
),
ref_vigente AS (
    /* UMA referencia por ano vigente: a mais recente ate a data de corte,
       com fallback para a mais antiga quando todas sao posteriores.
       E o que impede Fornecedores de ser contado 12 vezes. */
    SELECT V.ID AS ID_ANO,
           COALESCE(
               (SELECT MAX(R.REFERENCIA) FROM DET_DEMONSTRATIVO_REFERENCIA R
                 WHERE R.ID_DET_DEMONSTRATIVO_ANO = V.ID
                   AND R.REFERENCIA <= (SELECT DATA_FILTRO FROM DATAS)),
               (SELECT MIN(R.REFERENCIA) FROM DET_DEMONSTRATIVO_REFERENCIA R
                 WHERE R.ID_DET_DEMONSTRATIVO_ANO = V.ID)
           ) AS REFERENCIA
    FROM ano_vigente V
),
regras_contas AS (
    SELECT DISTINCT
        DET.ORDEM, DET.NOME_GRUPO, DET.COD_NOTA_EXPLICATIVA,
        C.PADRAO_CTACTB, C.SINAL
    FROM ESTR_DEMONSTRATIVOS          EST
    JOIN DET_DEMONSTRATIVO            DET ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    JOIN ano_vigente                  V   ON V.ID_DET_DEMONSTRATIVO = DET.ID
    JOIN ref_vigente                  RV  ON RV.ID_ANO = V.ID
    JOIN DET_DEMONSTRATIVO_REFERENCIA R   ON R.ID_DET_DEMONSTRATIVO_ANO = V.ID
                                         AND R.REFERENCIA = RV.REFERENCIA
    JOIN DET_DEMONSTRATIVO_CTACTB     C   ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = R.ID
    WHERE EST.ID = 1
),
regras_excluidas AS (
    SELECT DISTINCT DET.ORDEM, C.PADRAO_CTACTB
    FROM ESTR_DEMONSTRATIVOS              EST
    JOIN DET_DEMONSTRATIVO                DET ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
    JOIN ano_vigente                      V   ON V.ID_DET_DEMONSTRATIVO = DET.ID
    JOIN ref_vigente                      RV  ON RV.ID_ANO = V.ID
    JOIN DET_DEMONSTRATIVO_REFERENCIA     R   ON R.ID_DET_DEMONSTRATIVO_ANO = V.ID
                                             AND R.REFERENCIA = RV.REFERENCIA
    JOIN DET_DEMONSTRATIVO_CTACTB_EXC     C   ON C.ID_DET_DEMONSTRATIVO_REFERENCIA = R.ID
    WHERE EST.ID = 1
),
contas_mapeadas AS (
    SELECT
        R.ORDEM, R.NOME_GRUPO, R.COD_NOTA_EXPLICATIVA, R.SINAL,
        CASE WHEN TRIM(R.ORDEM) = '3.3.1' THEN B.saldo_final_ano_anterior
             WHEN TRIM(R.ORDEM) = '3.3.2' THEN B.ytd_atu
             ELSE B.total_atu END AS valor_atu,
        CASE WHEN TRIM(R.ORDEM) = '3.3.1' THEN B.saldo_final_ano_retrasado
             WHEN TRIM(R.ORDEM) = '3.3.2' THEN B.ytd_ant
             ELSE B.total_ant END AS valor_ant,
        ROW_NUMBER() OVER (
            PARTITION BY B.CTACTB, R.ORDEM, R.NOME_GRUPO
            ORDER BY LENGTH(R.PADRAO_CTACTB) DESC, R.PADRAO_CTACTB DESC
        ) AS rn
    FROM base_bal B
    JOIN regras_contas R      ON B.CTACTB LIKE R.PADRAO_CTACTB
    LEFT JOIN regras_excluidas E ON E.ORDEM = R.ORDEM
                                AND B.CTACTB LIKE E.PADRAO_CTACTB
    WHERE E.PADRAO_CTACTB IS NULL
)
SELECT
    R.ORDEM,
    R.NOME_GRUPO,
    R.COD_NOTA_EXPLICATIVA,
    /* IFNULL por fora: grupo sem conta casada vale 0, nao NULL */
    IFNULL(SUM(M.valor_atu * M.SINAL), 0) AS VLRLANC_ATU,
    IFNULL(SUM(M.valor_ant * M.SINAL), 0) AS VLRLANC_ANT,
    /* o ano e a data que a query realmente usou - para a tela mostrar */
    (SELECT ANO         FROM DATAS) AS ANO_USADO,
    (SELECT MES         FROM DATAS) AS MES_USADO,
    (SELECT DATA_FILTRO FROM DATAS) AS DATA_CORTE
FROM (SELECT DISTINCT ORDEM, NOME_GRUPO, COD_NOTA_EXPLICATIVA FROM regras_contas) R
LEFT JOIN contas_mapeadas M
       ON M.ORDEM = R.ORDEM
      AND M.NOME_GRUPO = R.NOME_GRUPO
      AND M.rn = 1
GROUP BY R.ORDEM, R.NOME_GRUPO, R.COD_NOTA_EXPLICATIVA
ORDER BY R.ORDEM
LIMIT 500;
