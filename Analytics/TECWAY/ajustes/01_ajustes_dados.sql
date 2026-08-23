-- =====================================================================
-- AJUSTES DE DADOS - tenant_51809 (API 2) - base CODEMP 999
-- Validado contra os relatorios oficiais de 31/12/2025.
-- Rode na ordem. Cada bloco tem a contagem esperada de linhas.
-- =====================================================================
START TRANSACTION;

-- ---------------------------------------------------------------------
-- 1) DRE Externo: SINAL invertido em Despesas gerais e administrativas
--    Na referencia 2024 estas contas estao com +1; em 2025 vieram -1.
--    Com a correcao o grupo 2.2 vai de -41.216 para -40.844 (alvo).
--    Esperado: 2 linhas
-- ---------------------------------------------------------------------
UPDATE DET_DEMONSTRATIVO_CTACTB SET SINAL = 1
 WHERE ID IN (23207, 23209)
   AND PADRAO_CTACTB IN ('5.1.02.01.000011','5.1.02.01.000012')
   AND SINAL = -1;

-- ---------------------------------------------------------------------
-- 2) DFC: contas de investimento classificadas como imobilizado
--    1.2.02.02.000002 e 1.2.02.02.000012 estao em "3.3 Aquisicao de
--    imobilizado" e pertencem a "3.2 Aquisicao para investimento".
--    E o mesmo par de contas que ja estava errado no Balanco.
--    Com a correcao: 3.2 = 8.497 e 3.3 = -25.277 (ambos batem o alvo).
--    Esperado: 2 linhas   (ID_REF de 3.2 na referencia 2025-12-01)
-- ---------------------------------------------------------------------
UPDATE DET_DEMONSTRATIVO_CTACTB
   SET ID_DET_DEMONSTRATIVO_REFERENCIA = (
        SELECT R.ID FROM DET_DEMONSTRATIVO_REFERENCIA R
        JOIN DET_DEMONSTRATIVO D ON D.ID = R.ID_DET_DEMONSTRATIVO
        WHERE D.ID_ESTR_DEMONSTRATIVO = 10 AND D.ORDEM = '3.2'
          AND R.REFERENCIA = '2025-12-01')
 WHERE ID IN (37852, 37856)
   AND PADRAO_CTACTB IN ('1.2.02.02.000002','1.2.02.02.000012');

-- ---------------------------------------------------------------------
-- 3) DFC: conta de lucros acumulados indevida em "4.3 Emprestimos - Mutuos"
--    2.1.01.12.000031 e a mesma conta ja reclassificada no Balanco.
--    Com a remocao: 4.3 = 3.833 (alvo).
--    Esperado: 1 linha
-- ---------------------------------------------------------------------
DELETE FROM DET_DEMONSTRATIVO_CTACTB
 WHERE ID = 43087 AND PADRAO_CTACTB = '2.1.01.12.000031';

-- Confira 2 + 2 + 1 linhas afetadas.  Se bater: COMMIT;  senao: ROLLBACK;
