-- =====================================================================
-- AJUSTES DE CADASTRO — BP INTERNO (ESTR_DEMONSTRATIVOS.ID = 2)
-- tenant_51809 (API 2) — CODEMP 999 — referencias 2025-12-01 e 2024-12-01
--
-- Sem estes ajustes o BP Interno NAO fecha, mesmo com a query corrigida:
--     2025: ativo - (passivo+PL) =  9.942.341,50
--     2024: ativo - (passivo+PL) =  6.500.333,81
-- As 2 contas abaixo existem no BP Externo (que fecha) e faltam no Interno.
-- Depois destes ajustes a diferenca vai a 0,00 nos dois anos e o ativo
-- do Interno bate com o do Externo (98.776.914,99 / 112.129.073,59).
--
-- IDs de DET_DEMONSTRATIVO_REFERENCIA usados (EST 2):
--     3.3.1 Resultado do exercicio        -> 118 (2025)  119 (2024)
--     3.3.3 Lucro/Prejuizos acumulados    -> 122 (2025)  123 (2024)
--     2.1.7 Mutuo a pagar (PC)            ->  96 (2025)   97 (2024)
-- =====================================================================
START TRANSACTION;

-- ---------------------------------------------------------------------
-- 1) Conta do RESULTADO DO EXERCICIO nao esta vinculada no Interno.
--    2.3.04.01.000011: saldo -911.300,74 (2025) e -6.500.333,81 (2024)
--    — e exatamente o lucro do exercicio da DRE (911 mil / 6.500 mil).
--    No Externo ela esta em 3.1.2 (vinc. 47359 e 47361). No Interno o
--    grupo 3.3.1 so tem 2.3.04.01.000004, zerada em 2025.
--    Esperado: 2 linhas
-- ---------------------------------------------------------------------
INSERT INTO DET_DEMONSTRATIVO_CTACTB
       (ID_DET_DEMONSTRATIVO_REFERENCIA, PADRAO_CTACTB, SINAL)
VALUES (118, '2.3.04.01.000011', -1),
       (119, '2.3.04.01.000011', -1);

-- ---------------------------------------------------------------------
-- 2) Conta de lucros acumulados nao vinculada no Interno.
--    2.3.02.02.000005: saldo -9.031.040,76 (2025), 0,00 (2024).
--    No Externo esta em 3.1.2 (vinc. 47358 e 47360).
--    Esperado: 2 linhas
-- ---------------------------------------------------------------------
INSERT INTO DET_DEMONSTRATIVO_CTACTB
       (ID_DET_DEMONSTRATIVO_REFERENCIA, PADRAO_CTACTB, SINAL)
VALUES (122, '2.3.02.02.000005', -1),
       (123, '2.3.02.02.000005', -1);

-- ---------------------------------------------------------------------
-- 3) Reclassificacao: 2.1.01.12.000031 (-6.500.000,00 em 2025) esta em
--    2.1.7 "Mutuo a pagar (PC)" no Interno, mas e conta de lucros
--    acumulados — no Externo ja foi reclassificada para 3.1.2 (vinc.
--    13790/13822) e o mesmo ajuste consta em ajustes/01_ajustes_dados.sql
--    ("a mesma conta ja reclassificada no Balanco"). So o Interno ficou
--    para tras. NAO altera o fechamento (passivo e PL sao o mesmo lado),
--    mas hoje infla o passivo circulante em 6,5 MM e reduz o PL em 6,5 MM.
--    Esperado: 1 linha
-- ---------------------------------------------------------------------
UPDATE DET_DEMONSTRATIVO_CTACTB
   SET ID_DET_DEMONSTRATIVO_REFERENCIA = 122      -- 3.3.3 Lucro/Prejuizos acumulados (2025)
 WHERE ID = 20335
   AND PADRAO_CTACTB = '2.1.01.12.000031';

--    Espelho em 2024 (saldo 0,00 hoje; entra por consistencia com o Externo).
--    Esperado: 1 linha
INSERT INTO DET_DEMONSTRATIVO_CTACTB
       (ID_DET_DEMONSTRATIVO_REFERENCIA, PADRAO_CTACTB, SINAL)
VALUES (123, '2.1.01.12.000031', -1);

-- ---------------------------------------------------------------------
-- 4) Cosmetico — nome de grupo com espaco duplo (ja apontado em
--    ajustes/05_PENDENCIAS.md item 5). As queries agrupam por NOME_GRUPO.
--    Esperado: 1 linha
-- ---------------------------------------------------------------------
UPDATE DET_DEMONSTRATIVO SET NOME_GRUPO = 'Mútuos a pagar (PNC)'
 WHERE ID_ESTR_DEMONSTRATIVO = 1 AND ORDEM = '2.2.3';

-- Confira 2 + 2 + 1 + 1 + 1 linhas afetadas.  Se bater: COMMIT;  senao: ROLLBACK;
