/* =====================================================================
   07 - QUERIES DOS FILTROS DAS TELAS (mês e empresa)

   As originais do projeto de origem liam de BP_TECWAY, que não existe
   neste tenant - por isso o filtro vinha vazio. Abaixo o equivalente
   lendo de IMP_BASE_BALANCETE, que já existe aqui.

   Original (não funciona no destino):
     SELECT DISTINCT MES AS ID, MES AS DESCR
     FROM BP_TECWAY
     ORDER BY STR_TO_DATE(CONCAT('01/', MES), '%d/%m/%Y') DESC
   ===================================================================== */


/* ---------------------------------------------------------------------
   FILTRO DE MÊS  ->  alimenta a variável VAR_MES (formato 'MM/AAAA')
   Retorna do mais recente para o mais antigo, igual à original.
   O GROUP BY + MAX(REFERENCIA) evita o erro de ONLY_FULL_GROUP_BY que
   um DISTINCT com ORDER BY sobre coluna fora do SELECT provoca.
   No destino hoje isso devolve 52 meses: 12/2021 a 03/2026.
   --------------------------------------------------------------------- */
SELECT
    DATE_FORMAT(REFERENCIA, '%m/%Y') AS ID,
    DATE_FORMAT(REFERENCIA, '%m/%Y') AS DESCR
FROM IMP_BASE_BALANCETE
GROUP BY DATE_FORMAT(REFERENCIA, '%m/%Y')
ORDER BY MAX(REFERENCIA) DESC;


/* ---------------------------------------------------------------------
   Variante: só os meses da empresa selecionada (se o filtro de mês vier
   depois do de empresa na tela).
   --------------------------------------------------------------------- */
-- SELECT
--     DATE_FORMAT(REFERENCIA, '%m/%Y') AS ID,
--     DATE_FORMAT(REFERENCIA, '%m/%Y') AS DESCR
-- FROM IMP_BASE_BALANCETE
-- WHERE :VAR_EMPRESA IS NULL
--    OR :VAR_EMPRESA = ''
--    OR FIND_IN_SET(CODEMP, :VAR_EMPRESA)
-- GROUP BY DATE_FORMAT(REFERENCIA, '%m/%Y')
-- ORDER BY MAX(REFERENCIA) DESC;


/* ---------------------------------------------------------------------
   FILTRO DE EMPRESA  ->  alimenta a variável VAR_EMPRESA
   No destino hoje devolve: 1, 2, 3, 4, 6, 7, 11, 14, 15, 16, 18, 600,
   888 e 999 (999 = consolidado, é o usado nas telas Tecway).
   --------------------------------------------------------------------- */
SELECT
    CODEMP AS ID,
    CODEMP AS DESCR
FROM IMP_BASE_BALANCETE
GROUP BY CODEMP
ORDER BY CODEMP;
