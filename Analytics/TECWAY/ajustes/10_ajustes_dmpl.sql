-- =====================================================================
-- DMPL Externo (EST.ID = 11) - vinculos e sinal
-- Roda junto com a query 09_queryDMPL.sql.
-- =====================================================================
START TRANSACTION;

-- 1) Lucros distribuidos deve sair NEGATIVO no relatorio (reducao do PL).
--    Hoje a conta esta com SINAL = +1 e o valor sai positivo.
--    Esperado: 1 linha por referencia cadastrada.
UPDATE DET_DEMONSTRATIVO_CTACTB C
  JOIN DET_DEMONSTRATIVO_REFERENCIA R ON R.ID = C.ID_DET_DEMONSTRATIVO_REFERENCIA
  JOIN DET_DEMONSTRATIVO D ON D.ID = R.ID_DET_DEMONSTRATIVO
   SET C.SINAL = -1
 WHERE D.ID_ESTR_DEMONSTRATIVO = 11 AND D.ORDEM = '2'
   AND C.PADRAO_CTACTB = '2.3.04.01.000009' AND C.SINAL = 1;

-- 2) Tres contas de PL nao estao no grupo 5 (Lucros ou Prejuizos Acumulados).
--    Sao as mesmas ja corrigidas no Balanco Patrimonial.
--    Com elas: 2023 = 15.632 e 2024 = 19.632, ambos exatos.
INSERT INTO DET_DEMONSTRATIVO_CTACTB (ID_DET_DEMONSTRATIVO_REFERENCIA, PADRAO_CTACTB, SINAL)
SELECT R.ID, X.CTA, -1
FROM DET_DEMONSTRATIVO_REFERENCIA R
JOIN DET_DEMONSTRATIVO D ON D.ID = R.ID_DET_DEMONSTRATIVO
JOIN (SELECT '2.3.02.02.000005' AS CTA
      UNION ALL SELECT '2.3.04.01.000011'
      UNION ALL SELECT '2.1.01.12.000031') X
WHERE D.ID_ESTR_DEMONSTRATIVO = 11 AND D.ORDEM = '5'
  AND NOT EXISTS (SELECT 1 FROM DET_DEMONSTRATIVO_CTACTB C2
                  WHERE C2.ID_DET_DEMONSTRATIVO_REFERENCIA = R.ID AND C2.PADRAO_CTACTB = X.CTA);

-- Confira as contagens.  Se bater: COMMIT;  senao: ROLLBACK;
