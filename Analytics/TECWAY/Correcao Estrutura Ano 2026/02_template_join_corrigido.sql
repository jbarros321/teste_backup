/* =====================================================================
   02 - O ENCADEAMENTO CORRIGIDO  (template)
   Troque NOVA_TABELA_ANO / ID_NOVA_TABELA_ANO pelos nomes reais.
   ===================================================================== */

/* ---------------------------------------------------------------------
   ANTES  (o que esta publicado e quebra)
   ------------------------------------------------------------------- */
-- FROM ESTR_DEMONSTRATIVOS EST
-- JOIN DET_DEMONSTRATIVO            DET    ON EST.ID = DET.ID_ESTR_DEMONSTRATIVO
-- JOIN DET_DEMONSTRATIVO_REFERENCIA DETREF ON DET.ID = DETREF.ID_DET_DEMONSTRATIVO
-- JOIN DET_DEMONSTRATIVO_CTACTB     VINC   ON DETREF.ID = VINC.ID_DET_DEMONSTRATIVO_REFERENCIA
-- WHERE EST.ID = 2
--   AND DETREF.REFERENCIA = (
--          SELECT MAX(R.REFERENCIA)
--            FROM DET_DEMONSTRATIVO_REFERENCIA R
--           WHERE R.ID_DET_DEMONSTRATIVO = DET.ID           -- <<< estoura
--             AND R.REFERENCIA <= DATE(:VAR_DATA_REF_DRE)
--       )

/* ---------------------------------------------------------------------
   DEPOIS  (com a tabela de ano no meio)

   A linha do demonstrativo passa a ter um registro por ano, e cada ano
   tem suas referencias. A subconsulta de "referencia vigente" agora tem
   que atravessar as duas tabelas: filtra o ANO na tabela nova e a DATA
   na tabela de referencia.
   ------------------------------------------------------------------- */
FROM ESTR_DEMONSTRATIVOS EST
JOIN DET_DEMONSTRATIVO            DET    ON EST.ID    = DET.ID_ESTR_DEMONSTRATIVO
JOIN NOVA_TABELA_ANO              DETANO ON DET.ID    = DETANO.ID_DET_DEMONSTRATIVO
JOIN DET_DEMONSTRATIVO_REFERENCIA DETREF ON DETANO.ID = DETREF.ID_NOVA_TABELA_ANO
JOIN DET_DEMONSTRATIVO_CTACTB     VINC   ON DETREF.ID = VINC.ID_DET_DEMONSTRATIVO_REFERENCIA
WHERE EST.ID = 2
  /* ano vigente: o mais recente que nao passa do ano da data de corte */
  AND DETANO.ANO = (
         SELECT MAX(A.ANO)
           FROM NOVA_TABELA_ANO A
          WHERE A.ID_DET_DEMONSTRATIVO = DET.ID
            AND A.ANO <= YEAR(DATE(:VAR_DATA_REF_DRE))
      )
  /* referencia vigente DENTRO do ano vigente */
  AND DETREF.REFERENCIA = (
         SELECT MAX(R.REFERENCIA)
           FROM DET_DEMONSTRATIVO_REFERENCIA R
          WHERE R.ID_NOVA_TABELA_ANO = DETANO.ID      -- FK nova, nao ID_DET_DEMONSTRATIVO
            AND R.REFERENCIA <= DATE(:VAR_DATA_REF_DRE)
      )

/* ---------------------------------------------------------------------
   VARIANTE - se a tabela de ano SUBSTITUIU a de referencia
   (ou seja, DET_DEMONSTRATIVO_CTACTB agora aponta para a tabela de ano e
   nao existe mais data, so ANO):
   ------------------------------------------------------------------- */
-- FROM ESTR_DEMONSTRATIVOS EST
-- JOIN DET_DEMONSTRATIVO        DET    ON EST.ID    = DET.ID_ESTR_DEMONSTRATIVO
-- JOIN NOVA_TABELA_ANO          DETANO ON DET.ID    = DETANO.ID_DET_DEMONSTRATIVO
-- JOIN DET_DEMONSTRATIVO_CTACTB VINC   ON DETANO.ID = VINC.ID_NOVA_TABELA_ANO
-- WHERE EST.ID = 2
--   AND DETANO.ANO = (
--          SELECT MAX(A.ANO)
--            FROM NOVA_TABELA_ANO A
--           WHERE A.ID_DET_DEMONSTRATIVO = DET.ID
--             AND A.ANO <= YEAR(DATE(:VAR_DATA_REF_DRE))
--       )

/* =====================================================================
   ATENCAO - duas armadilhas que este template ja evita
   =====================================================================

   1) SEM o filtro de ano vigente, cada conta entra UMA VEZ POR ANO
      cadastrado. Com 2024, 2025 e 2026 na tabela nova, Fornecedores
      triplica. Foi exatamente o defeito de `BP Interno/queryDados.sql`,
      que fazia INNER JOIN em DET_DEMONSTRATIVO_REFERENCIA sem escolher a
      referencia vigente e contava tudo em dobro
      (ver Correcao Fornecedores 2026/00_DIAGNOSTICO.md, item 4).
      Com a tabela de ano no meio o risco de multiplicacao aumenta.

   2) O "ano vigente" tem que ser MAX(ANO) <= ano de corte, nao
      `ANO = YEAR(:VAR_DATA_REF_DRE)`. Se ninguem cadastrar 2027 em
      janeiro, a igualdade zera a tela inteira; o MAX mantem o cadastro
      de 2026 valendo ate alguem cadastrar o novo.
   ===================================================================== */
