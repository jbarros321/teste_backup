/* =====================================================================
   03 - VINCULOS DE FORNECEDOR FALTANDO NO CADASTRO DO BP INTERNO
   tenant_47255

   Achado do item 3 do 00_DIAGNOSTICO.md. NAO e a causa dos 272.289,25
   (isso e a carga parada em 03/2026), mas sao contas de fornecedor com
   movimento no razao que nao chegam na tela, e a lista cresce todo mes.

   Levantado em 22/09/2026 contra IMP_BASE_BALANCETE.
   Contas de fornecedor no razao: 1243
   Nenhuma outra linha do BP usa os prefixos 2.1.01.01 / 2.1.01.02, e nao
   ha regra em DET_DEMONSTRATIVO_CTACTB_EXC sobre eles - conferido.

   Escolha UMA das duas partes:
     PARTE A - por prefixo (recomendada): 2 linhas resolvem para sempre.
     PARTE B - conta a conta: fecha o buraco de hoje, quebra de novo no
               proximo fornecedor cadastrado no Sankhya.
   ===================================================================== */

START TRANSACTION;


/* =====================================================================
   PARTE A - MAPEAMENTO POR PREFIXO  (RECOMENDADA)
   =====================================================================
   A query do BP Interno casa conta com regra por LIKE
   (B.CTACTB LIKE R.PADRAO_CTACTB), entao um padrao com curinga substitui
   a enumeracao. Trocar 1226 linhas exatas por 2 padroes faz a linha de
   Fornecedores absorver sozinha toda conta nova.

   O desempate por especificidade da query (ROW_NUMBER ... ORDER BY
   LENGTH(PADRAO_CTACTB) DESC) continua valendo: se um dia precisarem
   tirar uma conta especifica desses prefixos, basta cadastra-la em
   DET_DEMONSTRATIVO_CTACTB_EXC.

   ESTRUTURA 2 (BP Interno) / DET_DEMONSTRATIVO 42 / ORDEM 2.1.1
     referencia 82 = 2025-12-01, referencia 83 = 2024-12-01
   Esperado: DELETE de 1226 + 1226 linhas, INSERT de 4.
   ------------------------------------------------------------------- */

DELETE FROM DET_DEMONSTRATIVO_CTACTB
 WHERE ID_DET_DEMONSTRATIVO_REFERENCIA IN (82, 83)
   AND (PADRAO_CTACTB LIKE '2.1.01.01.%' OR PADRAO_CTACTB LIKE '2.1.01.02.%');

INSERT INTO DET_DEMONSTRATIVO_CTACTB
       (ID_DET_DEMONSTRATIVO_REFERENCIA, PADRAO_CTACTB, SINAL)
VALUES (82, '2.1.01.01.%', -1),
       (82, '2.1.01.02.%', -1),
       (83, '2.1.01.01.%', -1),
       (83, '2.1.01.02.%', -1);

/* Conferencia: tem que voltar 4 linhas, duas por referencia. */
SELECT ID_DET_DEMONSTRATIVO_REFERENCIA, PADRAO_CTACTB, SINAL
  FROM DET_DEMONSTRATIVO_CTACTB
 WHERE ID_DET_DEMONSTRATIVO_REFERENCIA IN (82, 83)
 ORDER BY 1, 2;


/* ---------------------------------------------------------------------
   Mesma ideia na estrutura da tela migrada (ESTR_DRE_TW 7).
   CUIDADO: CAD_CONTA_DRE_TW nao e casada por LIKE - a query usa
   t.ID_CONTA_CONTABIL = c.ID_CONTA_CONTABIL. Logo o prefixo aqui so
   funciona junto com o patch de query do arquivo 04. Sem o 04, use a
   PARTE B.3.
   Esperado: DELETE de 2058 linhas, INSERT de 2.
   ------------------------------------------------------------------- */

-- DELETE FROM CAD_CONTA_DRE_TW WHERE ID_DET_DRE_TW = 201;
-- INSERT INTO CAD_CONTA_DRE_TW (ID, ID_DET_DRE_TW, ID_CONTA_CONTABIL) VALUES
--   (60021, 201, '2.1.01.01.%'),
--   (60022, 201, '2.1.01.02.%');


/* =====================================================================
   PARTE B - CONTA A CONTA  (alternativa, se a PARTE A for recusada)
   =====================================================================
   NAO rode junto com a PARTE A - ela ja cobre estas contas.
   Comente a PARTE A inteira antes de descomentar esta.

   B.1  ESTRUTURA 2 / referencia 82 (2025-12-01) -- 22 contas
   B.2  ESTRUTURA 2 / referencia 83 (2024-12-01) -- 22 contas
        Impacto no CODEMP 999 com a base de hoje (ate 03/2026): 16.685,00
   B.3  ESTR_DRE_TW 7 / DET_DRE_TW 201 -- 32 contas
        Impacto no CODEMP 999 com a base de hoje (ate 03/2026): 17.089,00

-- B.1
INSERT INTO DET_DEMONSTRATIVO_CTACTB
       (ID_DET_DEMONSTRATIVO_REFERENCIA, PADRAO_CTACTB, SINAL)
VALUES
       (82, '2.1.01.01.000001', -1),
       (82, '2.1.01.01.000002', -1),
       (82, '2.1.01.02.002085', -1),
       (82, '2.1.01.02.002086', -1),
       (82, '2.1.01.02.002105', -1),
       (82, '2.1.01.02.002137', -1),
       (82, '2.1.01.02.002138', -1),
       (82, '2.1.01.02.002139', -1),
       (82, '2.1.01.02.002140', -1),
       (82, '2.1.01.02.002141', -1),
       (82, '2.1.01.02.002142', -1),
       (82, '2.1.01.02.002143', -1),
       (82, '2.1.01.02.002144', -1),
       (82, '2.1.01.02.002145', -1),
       (82, '2.1.01.02.002146', -1),
       (82, '2.1.01.02.002148', -1),
       (82, '2.1.01.02.002158', -1),
       (82, '2.1.01.02.002159', -1),
       (82, '2.1.01.02.002160', -1),
       (82, '2.1.01.02.002161', -1),
       (82, '2.1.01.02.002162', -1),
       (82, '2.1.01.02.002163', -1);

-- B.2
INSERT INTO DET_DEMONSTRATIVO_CTACTB
       (ID_DET_DEMONSTRATIVO_REFERENCIA, PADRAO_CTACTB, SINAL)
VALUES
       (83, '2.1.01.01.000001', -1),
       (83, '2.1.01.01.000002', -1),
       (83, '2.1.01.02.002085', -1),
       (83, '2.1.01.02.002086', -1),
       (83, '2.1.01.02.002105', -1),
       (83, '2.1.01.02.002137', -1),
       (83, '2.1.01.02.002138', -1),
       (83, '2.1.01.02.002139', -1),
       (83, '2.1.01.02.002140', -1),
       (83, '2.1.01.02.002141', -1),
       (83, '2.1.01.02.002142', -1),
       (83, '2.1.01.02.002143', -1),
       (83, '2.1.01.02.002144', -1),
       (83, '2.1.01.02.002145', -1),
       (83, '2.1.01.02.002146', -1),
       (83, '2.1.01.02.002148', -1),
       (83, '2.1.01.02.002158', -1),
       (83, '2.1.01.02.002159', -1),
       (83, '2.1.01.02.002160', -1),
       (83, '2.1.01.02.002161', -1),
       (83, '2.1.01.02.002162', -1),
       (83, '2.1.01.02.002163', -1);

-- B.3
INSERT INTO CAD_CONTA_DRE_TW (ID, ID_DET_DRE_TW, ID_CONTA_CONTABIL)
VALUES
       (60030, 201, '2.1.01.02.002105'),
       (60031, 201, '2.1.01.02.002106'),
       (60032, 201, '2.1.01.02.002107'),
       (60033, 201, '2.1.01.02.002108'),
       (60034, 201, '2.1.01.02.002109'),
       (60035, 201, '2.1.01.02.002110'),
       (60036, 201, '2.1.01.02.002111'),
       (60037, 201, '2.1.01.02.002112'),
       (60038, 201, '2.1.01.02.002113'),
       (60039, 201, '2.1.01.02.002114'),
       (60040, 201, '2.1.01.02.002115'),
       (60041, 201, '2.1.01.02.002116'),
       (60042, 201, '2.1.01.02.002117'),
       (60043, 201, '2.1.01.02.002118'),
       (60044, 201, '2.1.01.02.002119'),
       (60045, 201, '2.1.01.02.002137'),
       (60046, 201, '2.1.01.02.002138'),
       (60047, 201, '2.1.01.02.002139'),
       (60048, 201, '2.1.01.02.002140'),
       (60049, 201, '2.1.01.02.002141'),
       (60050, 201, '2.1.01.02.002142'),
       (60051, 201, '2.1.01.02.002143'),
       (60052, 201, '2.1.01.02.002144'),
       (60053, 201, '2.1.01.02.002145'),
       (60054, 201, '2.1.01.02.002146'),
       (60055, 201, '2.1.01.02.002148'),
       (60056, 201, '2.1.01.02.002158'),
       (60057, 201, '2.1.01.02.002159'),
       (60058, 201, '2.1.01.02.002160'),
       (60059, 201, '2.1.01.02.002161'),
       (60060, 201, '2.1.01.02.002162'),
       (60061, 201, '2.1.01.02.002163');

   ===================================================================== */

/* Confira as contagens. Se bater: COMMIT;  senao: ROLLBACK; */
