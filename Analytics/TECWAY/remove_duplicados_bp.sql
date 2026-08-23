-- =====================================================================
-- Remove vinculos DUPLICADOS no Balanco Patrimonial Externo (EST.ID = 1)
-- tenant_51809
--
-- As contas 1.2.02.02.000002 / 000010 / 000012 / 000013 estao ligadas
-- SIMULTANEAMENTE a "1.2.4 Imobilizado" e a "1.2.2 Investimentos" na
-- referencia 2025-12-01 -> dupla contagem no ativo.
-- Na referencia 2024-12-01 elas estao apenas em Investimentos, que e o
-- vinculo correto; o de Imobilizado e o duplicado.
--
-- Efeito: o KPI que monta a coluna do ano anterior com a estrutura de
-- 2025 deixa de inflar o Imobilizado em 8.497 mil
-- (10.082 -> 1.585) e o total do ativo 2024 volta de 120.626 p/ 112.129.
-- A coluna de 2025 nao muda: as 4 contas tem saldo 0,00 em 2025.
-- =====================================================================

START TRANSACTION;

CREATE TABLE DET_DEMONSTRATIVO_CTACTB_BKP_DUP AS
SELECT * FROM DET_DEMONSTRATIVO_CTACTB WHERE ID IN (30604, 30606, 30608, 30610);

DELETE FROM DET_DEMONSTRATIVO_CTACTB
WHERE ID IN (30604, 30606, 30608, 30610)
  AND ID_DET_DEMONSTRATIVO_REFERENCIA = 2          -- Imobilizado @2025-12-01
  AND PADRAO_CTACTB IN ('1.2.02.02.000002','1.2.02.02.000010',
                        '1.2.02.02.000012','1.2.02.02.000013');

-- Esperado: 4 linhas.  Se bater: COMMIT;  senao: ROLLBACK;

-- ---------------------------------------------------------------------
-- VALIDACAO: nenhuma conta em mais de um grupo na mesma referencia
-- ---------------------------------------------------------------------
SELECT r.REFERENCIA, m.PADRAO_CTACTB, COUNT(*) AS QTD_GRUPOS
FROM DET_DEMONSTRATIVO_CTACTB m
JOIN DET_DEMONSTRATIVO_REFERENCIA r ON r.ID = m.ID_DET_DEMONSTRATIVO_REFERENCIA
JOIN DET_DEMONSTRATIVO d ON d.ID = r.ID_DET_DEMONSTRATIVO
WHERE d.ID_ESTR_DEMONSTRATIVO = 1
GROUP BY r.REFERENCIA, m.PADRAO_CTACTB
HAVING COUNT(*) > 1;   -- deve sobrar so 1.1.03.03.000006 @2024-12-01 (saldo 0)
