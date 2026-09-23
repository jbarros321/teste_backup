/* =====================================================================
   05 - MIGRA AS REGRAS DE EXCLUSAO PARA A ESTRUTURA NOVA
   tenant_60677

   DET_DEMONSTRATIVO_ANO_CTACTB_EXC esta VAZIA (0 linhas) enquanto
   DET_DEMONSTRATIVO_CTACTB_EXC tem 35 regras. Os vinculos foram
   migrados para a estrutura por ano, as exclusoes NAO.

   Sem isto, trocar as queries para o caminho novo faz as 35 regras
   sumirem em silencio - as contas excluidas voltam a somar, sem erro
   nenhum na tela. Todas as 35 sao da estrutura 10 (DFC Externo).

   Rodar ANTES de aplicar o 03_patch_queries.py com CAMINHO = "NOVO".
   Esperado: 34 linhas inseridas.
   ===================================================================== */

START TRANSACTION;

INSERT INTO DET_DEMONSTRATIVO_ANO_CTACTB_EXC
       (ID_DET_DEMONSTRATIVO_ANO, PADRAO_CTACTB, SINAL)
VALUES
       (257, '5.1.02.01.000009', 1)   -- EST10 2.1 Contas a receber / 2025,
       (257, '5.1.02.01.000010', 1)   -- EST10 2.1 Contas a receber / 2025,
       (258, '5.1.02.01.000009', 1)   -- EST10 2.1 Contas a receber / 2024,
       (258, '5.1.02.01.000010', 1)   -- EST10 2.1 Contas a receber / 2024,
       (269, '5.9.01.01.000001', 1)   -- EST10 2.7 Obrigações tributárias / 2025,
       (269, '5.9.01.02.000001', 1)   -- EST10 2.7 Obrigações tributárias / 2025,
       (269, '5.9.01.02.000004', 1)   -- EST10 2.7 Obrigações tributárias / 2025,
       (269, '5.9.01.01.000002', 1)   -- EST10 2.7 Obrigações tributárias / 2025,
       (269, '5.9.01.02.000002', 1)   -- EST10 2.7 Obrigações tributárias / 2025,
       (269, '5.9.01.02.000003', 1)   -- EST10 2.7 Obrigações tributárias / 2025,
       (270, '5.9.01.01.000002', 1)   -- EST10 2.7 Obrigações tributárias / 2024,
       (270, '5.9.01.01.000001', 1)   -- EST10 2.7 Obrigações tributárias / 2024,
       (270, '5.9.01.02.000002', 1)   -- EST10 2.7 Obrigações tributárias / 2024,
       (270, '5.9.01.02.000001', 1)   -- EST10 2.7 Obrigações tributárias / 2024,
       (279, '3.2.01.01.000001', -1)   -- EST10 3.3 Aquisição de imobilizado / 2025,
       (279, '5.1.03.01.000004', 1)   -- EST10 3.3 Aquisição de imobilizado / 2025,
       (279, '5.1.03.01.000002', 1)   -- EST10 3.3 Aquisição de imobilizado / 2025,
       (279, '5.1.03.01.000001', 1)   -- EST10 3.3 Aquisição de imobilizado / 2025,
       (279, '5.1.03.01.000007', 1)   -- EST10 3.3 Aquisição de imobilizado / 2025,
       (279, '5.1.03.01.000005', 1)   -- EST10 3.3 Aquisição de imobilizado / 2025,
       (279, '5.1.03.01.000003', 1)   -- EST10 3.3 Aquisição de imobilizado / 2025,
       (280, '3.2.01.01.000001', -1)   -- EST10 3.3 Aquisição de imobilizado / 2024,
       (280, '5.1.03.01.000004', 1)   -- EST10 3.3 Aquisição de imobilizado / 2024,
       (280, '5.1.03.01.000002', 1)   -- EST10 3.3 Aquisição de imobilizado / 2024,
       (280, '5.1.03.01.000001', 1)   -- EST10 3.3 Aquisição de imobilizado / 2024,
       (280, '5.1.03.01.000007', 1)   -- EST10 3.3 Aquisição de imobilizado / 2024,
       (280, '5.1.03.01.000005', 1)   -- EST10 3.3 Aquisição de imobilizado / 2024,
       (280, '5.1.03.01.000003', 1)   -- EST10 3.3 Aquisição de imobilizado / 2024,
       (281, '5.1.03.02.000001', 1)   -- EST10 3.4 Aquisição de intangível / 2025,
       (282, '5.1.03.02.000001', 1)   -- EST10 3.4 Aquisição de intangível / 2024,
       (283, '5.1.04.01.000014', 1)   -- EST10 4.1 Emprestimos e Financiamentos / 2025,
       (284, '5.1.04.01.000014', 1)   -- EST10 4.1 Emprestimos e Financiamentos / 2024,
       (312, '5.1.04.01.000014', 1)   -- EST10 4.1 Emprestimos e Financiamentos / 2023,
       (289, '2.1.01.12.000031', -1)   -- EST10 4.4 Lucros distribuídos / 2025;

/* ---- Conferencia: as duas tabelas tem que bater ---------------------
   Deve voltar zero linhas. Cada linha devolvida e uma regra que ficou
   para tras.                                                           */
SELECT D.ID_ESTR_DEMONSTRATIVO, D.ORDEM, D.NOME_GRUPO, A.ANO, E.PADRAO_CTACTB
  FROM DET_DEMONSTRATIVO_CTACTB_EXC   E
  JOIN DET_DEMONSTRATIVO_REFERENCIA   R ON R.ID = E.ID_DET_DEMONSTRATIVO_REFERENCIA
  JOIN DET_DEMONSTRATIVO_ANO          A ON A.ID = R.ID_DET_DEMONSTRATIVO_ANO
  JOIN DET_DEMONSTRATIVO              D ON D.ID = A.ID_DET_DEMONSTRATIVO
 WHERE NOT EXISTS (
         SELECT 1
           FROM DET_DEMONSTRATIVO_ANO_CTACTB_EXC N
          WHERE N.ID_DET_DEMONSTRATIVO_ANO = A.ID
            AND N.PADRAO_CTACTB = E.PADRAO_CTACTB
       );

/* Se a conferencia voltar vazia: COMMIT;  senao: ROLLBACK; */
