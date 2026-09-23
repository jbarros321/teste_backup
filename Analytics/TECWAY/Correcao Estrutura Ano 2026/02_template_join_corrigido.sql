/* =====================================================================
   02 - O ENCADEAMENTO CORRIGIDO
   Schema confirmado pelo mapa de FK de 23/09/2026.
   ===================================================================== */

/* ---------------------------------------------------------------------
   ANTES - o que esta publicado e quebra
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


/* =====================================================================
   OPCAO 1 - CAMINHO LEGADO  (menor mudanca)
   DET_DEMONSTRATIVO -> _ANO -> _REFERENCIA -> _CTACTB

   Use se o script 01/A mostrar que DET_DEMONSTRATIVO_CTACTB continua com
   os ~18 mil vinculos e DET_DEMONSTRATIVO_ANO_CTACTB esta vazia.
   E o que o 03_patch_queries.py aplica com CAMINHO = "LEGADO".
   ===================================================================== */
FROM ESTR_DEMONSTRATIVOS EST
JOIN DET_DEMONSTRATIVO            DET    ON EST.ID    = DET.ID_ESTR_DEMONSTRATIVO
JOIN DET_DEMONSTRATIVO_ANO        DETANO ON DET.ID    = DETANO.ID_DET_DEMONSTRATIVO
JOIN DET_DEMONSTRATIVO_REFERENCIA DETREF ON DETANO.ID = DETREF.ID_DET_DEMONSTRATIVO_ANO
JOIN DET_DEMONSTRATIVO_CTACTB     VINC   ON DETREF.ID = VINC.ID_DET_DEMONSTRATIVO_REFERENCIA
WHERE EST.ID = 2
  /* ano vigente: o mais recente que nao passa do ano de corte */
  AND DETANO.ANO = (
         SELECT MAX(A.ANO)
           FROM DET_DEMONSTRATIVO_ANO A
          WHERE A.ID_DET_DEMONSTRATIVO = DET.ID
            AND A.ANO <= YEAR(DATE(:VAR_DATA_REF_DRE))
      )
  /* referencia vigente DENTRO do ano vigente */
  AND DETREF.REFERENCIA = (
         SELECT MAX(R.REFERENCIA)
           FROM DET_DEMONSTRATIVO_REFERENCIA R
          WHERE R.ID_DET_DEMONSTRATIVO_ANO = DETANO.ID     -- FK nova
            AND R.REFERENCIA <= DATE(:VAR_DATA_REF_DRE)
      )


/* =====================================================================
   OPCAO 2 - CAMINHO NOVO  (o que a estrutura nova parece querer)
   DET_DEMONSTRATIVO -> _ANO -> _ANO_CTACTB

   As tabelas DET_DEMONSTRATIVO_ANO_CTACTB / _EXC penduram direto em
   DET_DEMONSTRATIVO_ANO e passam por cima de _REFERENCIA. Some a data e
   sobra so o ano - que e justamente o ponto da mudanca.

   Use se o script 01/A mostrar os vinculos nas tabelas _ANO_CTACTB.
   E o que o 03_patch_queries.py aplica com CAMINHO = "NOVO".
   ===================================================================== */
-- FROM ESTR_DEMONSTRATIVOS EST
-- JOIN DET_DEMONSTRATIVO           DET    ON EST.ID    = DET.ID_ESTR_DEMONSTRATIVO
-- JOIN DET_DEMONSTRATIVO_ANO       DETANO ON DET.ID    = DETANO.ID_DET_DEMONSTRATIVO
-- JOIN DET_DEMONSTRATIVO_ANO_CTACTB VINC  ON DETANO.ID = VINC.ID_DET_DEMONSTRATIVO_ANO
-- WHERE EST.ID = 2
--   AND DETANO.ANO = (
--          SELECT MAX(A.ANO)
--            FROM DET_DEMONSTRATIVO_ANO A
--           WHERE A.ID_DET_DEMONSTRATIVO = DET.ID
--             AND A.ANO <= YEAR(DATE(:VAR_DATA_REF_DRE))
--       )
--
-- /* as exclusoes tambem mudam de tabela */
-- LEFT JOIN DET_DEMONSTRATIVO_ANO_CTACTB_EXC EXC
--        ON EXC.ID_DET_DEMONSTRATIVO_ANO = DETANO.ID
--       AND B.CTACTB LIKE EXC.PADRAO_CTACTB


/* =====================================================================
   TRES ARMADILHAS
   =====================================================================

   1) MULTIPLICACAO POR ANO. Sem o filtro de ano vigente, cada conta entra
      uma vez por ano cadastrado em DET_DEMONSTRATIVO_ANO. Com 2024, 2025
      e 2026, Fornecedores triplica - e nao da erro nenhum, so um numero
      errado. E o mesmo defeito que BP Interno/queryDados.sql ja tinha
      (ver Correcao Fornecedores 2026/00_DIAGNOSTICO.md, item 4), agora
      com uma dimensao a mais para errar.

   2) MAX(ANO) <= ano de corte, NUNCA `ANO = YEAR(:VAR_DATA_REF_DRE)`.
      Com a igualdade, janeiro sem 2027 cadastrado zera a tela inteira.
      Com o MAX, o cadastro de 2026 continua valendo ate alguem criar o
      novo.

   3) CAMINHO ERRADO ABRE ZERADO, NAO DA ERRO. Se os vinculos estiverem
      em _ANO_CTACTB e a query continuar lendo _CTACTB, a tela abre sem
      erro nenhum e com tudo zero. Por isso o script 01/A vem antes de
      qualquer patch - e por isso vale conferir Fornecedores contra o
      Sankhya depois (Correcao Fornecedores 2026/02_*.sql).
   ===================================================================== */
