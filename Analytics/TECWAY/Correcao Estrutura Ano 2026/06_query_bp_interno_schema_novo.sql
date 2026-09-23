/* =====================================================================
   06 - BP INTERNO NO SCHEMA NOVO  (ESTR_DEMONSTRATIVOS.ID = 2)
   tenant_60677

   Junta as duas correcoes:
     - encadeamento novo   DET_DEMONSTRATIVO -> _ANO -> _ANO_CTACTB
     - aviso de defasagem  (REF_MAX_DISPONIVEL / MESES_DEFASAGEM)

   DET_DEMONSTRATIVO_REFERENCIA sai do caminho: no modelo novo o vinculo
   e por ANO, nao por data. Isso tambem elimina a duplicacao - a linha
   Fornecedores tem 12 referencias em 2026 e 12 em 2027, uma por mes,
   cada uma com uma copia completa dos 1.226 vinculos.

   Parametros: :VAR_DATA_REF_DRE (data de corte), :VAR_EMPRESA_DRE (CODEMP)
   Colunas: ORDEM, NOME_GRUPO, ANO_ATUAL, ANO_ANTERIOR,
            REF_MAX_DISPONIVEL, MESES_DEFASAGEM
   ===================================================================== */

WITH ref_disponivel AS (
    SELECT MAX(REFERENCIA) AS REF_MAX
      FROM IMP_BASE_BALANCETE
     WHERE CODEMP = :VAR_EMPRESA_DRE
),
base_bal AS (
    /* patrimoniais: saldo acumulado desde o inicio ate o corte */
    SELECT CTACTB,
           SUM(CASE WHEN REFERENCIA <= LAST_DAY(DATE(:VAR_DATA_REF_DRE))
                    THEN VLRLANC ELSE 0 END) AS soma_ate_atu,
           SUM(CASE WHEN REFERENCIA <= LAST_DAY(DATE_SUB(DATE(:VAR_DATA_REF_DRE), INTERVAL 1 YEAR))
                    THEN VLRLANC ELSE 0 END) AS soma_ate_ant
      FROM IMP_BASE_BALANCETE
     WHERE CODEMP = :VAR_EMPRESA_DRE
       AND (CTACTB LIKE '1%' OR CTACTB LIKE '2%')
     GROUP BY CTACTB
    UNION ALL
    /* resultado: acumulam so o ano corrente */
    SELECT CTACTB,
           SUM(CASE WHEN REFERENCIA >= MAKEDATE(YEAR(DATE(:VAR_DATA_REF_DRE)), 1)
                     AND REFERENCIA <= LAST_DAY(DATE(:VAR_DATA_REF_DRE))
                    THEN VLRLANC ELSE 0 END),
           SUM(CASE WHEN REFERENCIA >= MAKEDATE(YEAR(DATE_SUB(DATE(:VAR_DATA_REF_DRE), INTERVAL 1 YEAR)), 1)
                     AND REFERENCIA <= LAST_DAY(DATE_SUB(DATE(:VAR_DATA_REF_DRE), INTERVAL 1 YEAR))
                    THEN VLRLANC ELSE 0 END)
      FROM IMP_BASE_BALANCETE
     WHERE CODEMP = :VAR_EMPRESA_DRE
       AND (CTACTB LIKE '3%' OR CTACTB LIKE '4%' OR CTACTB LIKE '5%')
     GROUP BY CTACTB
),
ano_vigente AS (
    /* UM registro de ano por linha do demonstrativo.
       MAX(ANO) <= ano de corte, nunca ANO = YEAR(corte): a estrutura 2 ja
       tem 2027 cadastrado, e em janeiro sem o ano novo a igualdade zeraria
       a tela inteira. */
    SELECT A.ID, A.ID_DET_DEMONSTRATIVO, A.ANO
      FROM DET_DEMONSTRATIVO_ANO A
     WHERE A.ANO = (
             SELECT MAX(A2.ANO)
               FROM DET_DEMONSTRATIVO_ANO A2
              WHERE A2.ID_DET_DEMONSTRATIVO = A.ID_DET_DEMONSTRATIVO
                AND A2.ANO <= YEAR(DATE(:VAR_DATA_REF_DRE))
           )
),
regras_contas AS (
    SELECT DISTINCT DET.ORDEM, DET.NOME_GRUPO, AV.ID AS ID_ANO,
           VINC.PADRAO_CTACTB, VINC.SINAL
      FROM ESTR_DEMONSTRATIVOS          EST
      JOIN DET_DEMONSTRATIVO            DET  ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
      JOIN ano_vigente                  AV   ON AV.ID_DET_DEMONSTRATIVO = DET.ID
      JOIN DET_DEMONSTRATIVO_ANO_CTACTB VINC ON VINC.ID_DET_DEMONSTRATIVO_ANO = AV.ID
     WHERE EST.ID = 2
),
regras_excluidas AS (
    SELECT DISTINCT DET.ORDEM, DET.NOME_GRUPO, EXC.PADRAO_CTACTB
      FROM ESTR_DEMONSTRATIVOS              EST
      JOIN DET_DEMONSTRATIVO                DET ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
      JOIN ano_vigente                      AV  ON AV.ID_DET_DEMONSTRATIVO = DET.ID
      JOIN DET_DEMONSTRATIVO_ANO_CTACTB_EXC EXC ON EXC.ID_DET_DEMONSTRATIVO_ANO = AV.ID
     WHERE EST.ID = 2
),
contas_mapeadas AS (
    SELECT B.soma_ate_atu, B.soma_ate_ant, R.ORDEM, R.NOME_GRUPO, R.SINAL,
           ROW_NUMBER() OVER (PARTITION BY B.CTACTB, R.ORDEM, R.NOME_GRUPO
                              ORDER BY LENGTH(R.PADRAO_CTACTB) DESC,
                                       R.PADRAO_CTACTB DESC) AS rn
      FROM base_bal B
      JOIN regras_contas R   ON B.CTACTB LIKE R.PADRAO_CTACTB
      LEFT JOIN regras_excluidas E
             ON B.CTACTB LIKE E.PADRAO_CTACTB
            AND E.ORDEM = R.ORDEM
            AND E.NOME_GRUPO = R.NOME_GRUPO
     WHERE E.PADRAO_CTACTB IS NULL
)
SELECT R.ORDEM,
       R.NOME_GRUPO,
       IFNULL(Agg.ANO_ATUAL, 0)    AS ANO_ATUAL,
       IFNULL(Agg.ANO_ANTERIOR, 0) AS ANO_ANTERIOR,
       DATE_FORMAT(D.REF_MAX, '%m/%Y') AS REF_MAX_DISPONIVEL,
       GREATEST(TIMESTAMPDIFF(MONTH, D.REF_MAX, DATE(:VAR_DATA_REF_DRE)), 0)
                                       AS MESES_DEFASAGEM
  FROM (SELECT DISTINCT ORDEM, NOME_GRUPO FROM regras_contas) R
  CROSS JOIN ref_disponivel D
  LEFT JOIN (
        SELECT ORDEM, NOME_GRUPO,
               SUM(IFNULL(soma_ate_atu, 0) * IFNULL(SINAL, 1)) AS ANO_ATUAL,
               SUM(IFNULL(soma_ate_ant, 0) * IFNULL(SINAL, 1)) AS ANO_ANTERIOR
          FROM contas_mapeadas
         WHERE rn = 1
         GROUP BY ORDEM, NOME_GRUPO
  ) Agg ON R.ORDEM = Agg.ORDEM AND R.NOME_GRUPO = Agg.NOME_GRUPO
 ORDER BY R.ORDEM;
