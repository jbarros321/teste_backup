-- =====================================================================
-- AJUSTES — DFC EXTERNO (ESTR_DEMONSTRATIVOS.ID = 10)
-- tenant_51809 (API 2) — CODEMP 999 — referencia 01/12/2025
--
-- IMPORTANTE: aqui a QUERY esta correta (query.sql / ajustes/04_queryDFC.sql).
-- Reproduzi o relatorio linha a linha com ela. O problema e SO de cadastro.
-- Nao alterar a query, ao contrario do que foi preciso no BP Interno.
--
-- SINTOMA: o DFC nao fecha contra o proprio caixa.
--     Aumento de caixa informado ......... (11.429)
--     Saldo final - saldo inicial ........  (2.476)   (1.898 - 4.374)
--     Diferenca .......................... 8.953
--
-- Duas causas, que somadas dao a diferenca:
--     1.3 Provisao p/ perdas ....... (1.394) -> 960     dif 2.354
--     4.4 Lucros distribuidos ..... (11.500) -> (5.000) dif 6.500
--                                                       ----------
--                                                          8.854  (+99 da
--     linha 1.9.1, ver observacao no fim do arquivo)
--
-- DEPOIS DOS AJUSTES (simulado sobre os dados reais da API):
--     2025: Oper 56.313 | Inv (16.890) | Fin (41.898) | Aumento (2.476)
--           Saldo final - inicial (2.476)   -> residuo R$ 260
--     2024: Oper 51.986 | Inv (19.657) | Fin (35.260) | Aumento (2.930)
--           Saldo final - inicial (2.932)   -> residuo R$ 1.140
--     Ambos fecham na casa do milhar.
-- =====================================================================
START TRANSACTION;

-- ---------------------------------------------------------------------
-- 1) Linha 1.3 "Provisao para perdas estimadas com clientes"
--
--    A referencia 2025-12-01 (refID 870) aponta a conta PATRIMONIAL
--    1.1.02.06.000001 (a propria PECLD do ativo). Como a linha e um
--    ajuste de resultado (despesa sem caixa), ela tem de apontar a conta
--    de DESPESA, igual fazem 1.4 Perdas (5.1.02.01.000009), 1.5
--    Depreciacao e 1.6 Amortizacao.
--
--    A propria referencia 2024-12-01 (refID 871, vinc. 39604) JA usa a
--    conta certa: 5.1.02.01.000010. So o cadastro de 2025 regrediu.
--
--    Confere com a DRE, grupo 5.3.2 "Provisao para PECLD":
--        5.1.02.01.000010 = (959.596,22) em 2025 e (674.315,19) em 2024
--    Com o ajuste a linha vai de (1.394) para 960 em 2025.
--    Esperado: 1 linha
-- ---------------------------------------------------------------------
UPDATE DET_DEMONSTRATIVO_CTACTB
   SET PADRAO_CTACTB = '5.1.02.01.000010'
 WHERE ID = 31454
   AND PADRAO_CTACTB = '1.1.02.06.000001';

-- ---------------------------------------------------------------------
-- 2) Linha 4.4 "Lucros distribuidos"  — resolve a pendencia 2 do
--    ajustes/05_PENDENCIAS.md ("Preciso que voce indique qual conta
--    registra essa transferencia"). A conta e 2.1.01.12.000031.
--
--    O grupo aponta 2.3.04.01.000009, que variou 11.500.000,00 em 2025
--    num lancamento unico misturando duas coisas:
--        distribuicao de lucros de 2025 ................  5.000.000,00
--        transferencia do lucro de 2024 p/ acumulados ..  6.500.000,00
--
--    A contrapartida da transferencia esta em 2.1.01.12.000031, que tem
--    UM UNICO lancamento em toda a base: (6.500.000,00) em 2025-12-01,
--    e nada em 2024 — por isso a exclusao nao contamina o ano anterior
--    (que ja sai correto em (2.500)).
--    E a mesma conta reclassificada para lucros acumulados no Balanco
--    (ver ajustes/01_ajustes_dados.sql e BP Interno/ajustes_bp_interno.sql).
--
--    Mecanismo identico ao ja usado em 2.1, 2.7, 3.3, 3.4 e 4.1.
--    Com a exclusao: (11.500.000) - (6.500.000) = (5.000.000).
--    Esperado: 1 linha
-- ---------------------------------------------------------------------
INSERT INTO DET_DEMONSTRATIVO_CTACTB_EXC
       (ID_DET_DEMONSTRATIVO_REFERENCIA, PADRAO_CTACTB, SINAL)
VALUES (916, '2.1.01.12.000031', -1);       -- 916 = ORDEM 4.4, ref 2025-12-01

-- Confira 1 + 1 linhas afetadas.  Se bater: COMMIT;  senao: ROLLBACK;


-- =====================================================================
-- OBSERVACAO 1 — linha 1.9.1 "Outros" (nao incluida acima)
--
-- A tabela OUTROS_DFC traz 99.000,00 (2025) e (32.000,00) (2024), e a
-- query devolve esses valores corretamente na linha 1.9.1. Mas o total
-- do relatorio nao os considera: o "Aumento de caixa" exibido e (11.429)
-- e o meu calculo com a linha 1.9.1 somada da (11.330) — exatamente 99
-- de diferenca.
--
-- O fechamento so bate COM a linha 1.9.1 somada (foi assim que cheguei a
-- (2.476)). Ou seja, o total do DFC no index.html esta deixando a 1.9.1
-- de fora do fluxo operacional. Isso e no HTML, nao no banco — por isso
-- nao entra neste script. Vale conferir junto com a troca da query.
--
-- OBSERVACAO 2 — DMPL (ESTR_DEMONSTRATIVOS.ID = 11)
--
-- O grupo 2 "Lucro distribuidos" do DMPL usa a MESMA conta
-- 2.3.04.01.000009 (refID 950 em 2025, vinc. 43371, SINAL -1) e sofre do
-- mesmo problema — e o item 2 do 05_PENDENCIAS.md. O ajuste analogo seria:
--
--     INSERT INTO DET_DEMONSTRATIVO_CTACTB_EXC
--            (ID_DET_DEMONSTRATIVO_REFERENCIA, PADRAO_CTACTB, SINAL)
--     VALUES (950, '2.1.01.12.000031', -1);
--
-- Deixei COMENTADO de proposito: nao validei o DMPL ponta a ponta, porque
-- conforme o item 4 do 05_PENDENCIAS.md a query do DMPL ainda nao produz
-- o formato de matriz do relatorio. Confirme o sinal contra o relatorio
-- antes de aplicar.
-- =====================================================================
