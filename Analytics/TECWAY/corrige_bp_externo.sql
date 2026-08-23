-- =====================================================================
-- Correcao do Balanco Patrimonial Externo (EST.ID = 1) - tenant_51809
-- Refs 2025-12-01 (ID_REF PL=48 / Mutuos PC=38)
--      2024-12-01 (ID_REF PL=49 / Mutuos PC=39)
--
-- Diagnostico (CODEMP = 999, consolidado):
--   1) A conta 2.1.01.12.000031 (saldo -6.500.000,00 em 2025) esta em
--      "Mutuos a pagar (PC)"; inflava o grupo em 6.500 mil.
--   2) As contas de PL 2.3.02.02.000005 (-9.031.040,76 em 2025) e
--      2.3.04.01.000011 (-911.300,74 em 2025 / -6.500.333,81 em 2024)
--      nao tinham vinculo nenhum no BP Externo; faltavam em
--      "Lucros acumulados" (16.442 mil em 2025, 6.500 mil em 2024).
--
-- Simulado contra o balanco alvo: os 24 grupos batem e
-- ativo = passivo + PL em 2025 (98.777) e 2024 (112.129).
-- =====================================================================

START TRANSACTION;

-- ---------------------------------------------------------------------
-- 1) Reclassifica 2.1.01.12.000031: Mutuos a pagar (PC) -> Lucros acumulados
--    (1 linha por referencia; o SINAL = -1 ja esta correto e nao muda)
-- ---------------------------------------------------------------------
UPDATE DET_DEMONSTRATIVO_CTACTB
   SET ID_DET_DEMONSTRATIVO_REFERENCIA = 48
 WHERE ID = 13790
   AND PADRAO_CTACTB = '2.1.01.12.000031'
   AND ID_DET_DEMONSTRATIVO_REFERENCIA = 38;      -- 2025-12-01

UPDATE DET_DEMONSTRATIVO_CTACTB
   SET ID_DET_DEMONSTRATIVO_REFERENCIA = 49
 WHERE ID = 13822
   AND PADRAO_CTACTB = '2.1.01.12.000031'
   AND ID_DET_DEMONSTRATIVO_REFERENCIA = 39;      -- 2024-12-01 (saldo 0, so consistencia)

-- ---------------------------------------------------------------------
-- 2) Vincula as duas contas de PL orfas ao grupo Lucros acumulados
-- ---------------------------------------------------------------------
INSERT INTO DET_DEMONSTRATIVO_CTACTB (ID_DET_DEMONSTRATIVO_REFERENCIA, PADRAO_CTACTB, SINAL) VALUES
  (48, '2.3.02.02.000005', -1),   -- 2025-12-01
  (48, '2.3.04.01.000011', -1),   -- 2025-12-01
  (49, '2.3.02.02.000005', -1),   -- 2024-12-01 (saldo 0, so consistencia)
  (49, '2.3.04.01.000011', -1);   -- 2024-12-01

-- Esperado: 2 linhas em UPDATE + 4 em INSERT.  Se bater: COMMIT;  senao: ROLLBACK;

-- ---------------------------------------------------------------------
-- 3) VALIDACAO (apos o COMMIT) - nenhuma conta 2.3.* sem vinculo no BP Externo
-- ---------------------------------------------------------------------
SELECT b.CTACTB, SUM(b.VLRLANC) AS SALDO
FROM IMP_BASE_BALANCETE b
WHERE b.CODEMP = 999
  AND b.CTACTB LIKE '2.3%'
  AND b.REFERENCIA < '2026-01-01'
  AND NOT EXISTS (
      SELECT 1
      FROM DET_DEMONSTRATIVO_CTACTB m
      JOIN DET_DEMONSTRATIVO_REFERENCIA r ON r.ID = m.ID_DET_DEMONSTRATIVO_REFERENCIA
      JOIN DET_DEMONSTRATIVO d ON d.ID = r.ID_DET_DEMONSTRATIVO
      WHERE d.ID_ESTR_DEMONSTRATIVO = 1
        AND r.REFERENCIA = '2025-12-01'
        AND m.PADRAO_CTACTB = b.CTACTB)
GROUP BY b.CTACTB
HAVING SUM(b.VLRLANC) <> 0;   -- deve vir vazio
