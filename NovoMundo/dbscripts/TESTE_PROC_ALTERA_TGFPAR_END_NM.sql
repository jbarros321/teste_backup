-- ============================================================================
-- SCRIPT DE TESTE - PROC_ALTERA_TGFPAR_END_NM e PROC_INCLUI_PARCEIRO_UFS_NM
-- ============================================================================
-- Autor: Teste de Validação
-- Data: Dezembro 2025
-- 
-- Este script testa as procedures de alteração de endereço de parceiros
-- 
-- INSTRUÇÕES:
-- - Para valores NULL nos parâmetros, use 0 (zero)
-- - O script converte 0 para NULL automaticamente na procedure
-- - Ajuste as datas (P_DTINI e P_DTFIN) conforme necessário
-- ============================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED;
SET ECHO ON;
SET FEEDBACK ON;
SET LINESIZE 200;
SET PAGESIZE 1000;

-- ============================================================================
-- VARIÁVEIS DE TESTE
-- ============================================================================
-- Ajuste estes valores conforme seus dados de teste
-- IMPORTANTE: Para valores NULL, deixe vazio ou use 0
DEFINE P_CODUSU = 0;
DEFINE P_DTINI = '01/01/2024';
DEFINE P_DTFIN = '31/12/2024';
DEFINE P_CODEMP = 0;
DEFINE P_CODEMPMATRIZ = 0;
DEFINE P_NUNOTA = 0;
DEFINE P_CODTIPOPER = 0;
DEFINE P_APENASDIFAL = 'N';
DEFINE P_SEQTELA = 1;

-- ============================================================================
-- PREPARAÇÃO: VERIFICAÇÃO DE DADOS ANTES DO TESTE
-- ============================================================================
PROMPT ============================================================================
PROMPT 1. VERIFICAÇÃO DE DADOS ANTES DO TESTE
PROMPT ============================================================================

PROMPT 
PROMPT Verificando notas que serão processadas...
SELECT COUNT(*) AS QTD_NOTAS
FROM TGFCAB C
INNER JOIN TGFPAR P ON P.CODPARC = C.CODPARC
INNER JOIN TGFTOP T ON T.CODTIPOPER = C.CODTIPOPER AND T.DHALTER = C.DHTIPOPER
WHERE C.CODPARC < 900000000
  AND (T.ATUALLIVFIS <> 'N' OR T.CODTIPOPER = 1005)
  AND EXISTS (
    SELECT 1
    FROM TGFITE I
    WHERE I.NUNOTA = C.NUNOTA
      AND (I.CODCFO > 5000 OR I.CODCFO IN (1201, 1202, 1410, 1411, 2201, 2202, 2410, 2411))
  )
  AND C.DTENTSAI BETWEEN TO_DATE('&P_DTINI', 'DD/MM/YYYY') AND TO_DATE('&P_DTFIN', 'DD/MM/YYYY')
  AND (TO_NUMBER('&P_CODEMP') = 0 OR C.CODEMP = TO_NUMBER('&P_CODEMP'))
  AND (TO_NUMBER('&P_NUNOTA') = 0 OR C.NUNOTA = TO_NUMBER('&P_NUNOTA'))
  AND (TO_NUMBER('&P_CODTIPOPER') = 0 OR C.CODTIPOPER = TO_NUMBER('&P_CODTIPOPER'))
  AND ((NVL('&P_APENASDIFAL', 'N') = 'S' AND
        (NVL(C.VLRICMSDIFALDEST, 0) <> 0 OR
         NVL(C.VLRICMSDIFALREM, 0) <> 0 OR
         NVL(C.VLRICMSFCP, 0) <> 0)) OR
       (NVL('&P_APENASDIFAL', 'N') = 'N'))
  AND NOT EXISTS (
    SELECT 1
    FROM TSIEMP E
    WHERE E.CGC = P.CGC_CPF
      AND E.CGC * 1 <> 0
  );

PROMPT 
PROMPT Exibindo amostra de notas que serão processadas (primeiras 10)...
SELECT C.NUNOTA,
       C.CODPARC,
       P.NOMEPARC,
       C.DTENTSAI,
       C.CODEMP,
       C.CODTIPOPER,
       C.AD_UF AS UF_NOVA,
       C.AD_CODCID AS COD_CIDADE_NOVA,
       C.AD_INSCESTADNAUF AS IE_NOVA,
       C.AD_CODPARCORIG AS COD_PARC_ORIG
FROM TGFCAB C
INNER JOIN TGFPAR P ON P.CODPARC = C.CODPARC
INNER JOIN TGFTOP T ON T.CODTIPOPER = C.CODTIPOPER AND T.DHALTER = C.DHTIPOPER
WHERE C.CODPARC < 900000000
  AND (T.ATUALLIVFIS <> 'N' OR T.CODTIPOPER = 1005)
  AND EXISTS (
    SELECT 1
    FROM TGFITE I
    WHERE I.NUNOTA = C.NUNOTA
      AND (I.CODCFO > 5000 OR I.CODCFO IN (1201, 1202, 1410, 1411, 2201, 2202, 2410, 2411))
  )
  AND C.DTENTSAI BETWEEN TO_DATE('&P_DTINI', 'DD/MM/YYYY') AND TO_DATE('&P_DTFIN', 'DD/MM/YYYY')
  AND (TO_NUMBER('&P_CODEMP') = 0 OR C.CODEMP = TO_NUMBER('&P_CODEMP'))
  AND (TO_NUMBER('&P_NUNOTA') = 0 OR C.NUNOTA = TO_NUMBER('&P_NUNOTA'))
  AND (TO_NUMBER('&P_CODTIPOPER') = 0 OR C.CODTIPOPER = TO_NUMBER('&P_CODTIPOPER'))
  AND ((NVL('&P_APENASDIFAL', 'N') = 'S' AND
        (NVL(C.VLRICMSDIFALDEST, 0) <> 0 OR
         NVL(C.VLRICMSDIFALREM, 0) <> 0 OR
         NVL(C.VLRICMSFCP, 0) <> 0)) OR
       (NVL('&P_APENASDIFAL', 'N') = 'N'))
  AND NOT EXISTS (
    SELECT 1
    FROM TSIEMP E
    WHERE E.CGC = P.CGC_CPF
      AND E.CGC * 1 <> 0
  )
  AND C.AD_UF IS NOT NULL
  AND ROWNUM <= 10;

-- ============================================================================
-- TESTE DIRETO: PROC_INCLUI_PARCEIRO_UFS_NM
-- ============================================================================
PROMPT 
PROMPT ============================================================================
PROMPT 2. EXECUTANDO TESTE DIRETO DA PROCEDURE PROC_INCLUI_PARCEIRO_UFS_NM
PROMPT ============================================================================
PROMPT 
PROMPT ATENÇÃO: Este teste executa a procedure diretamente com parâmetros fixos
PROMPT Para testar via PROC_ALTERA_TGFPAR_END_NM, use o sistema Sankhya
PROMPT 

DECLARE
  V_MENSAGEM VARCHAR2(4000);
  V_DTINI DATE := TO_DATE('&P_DTINI', 'DD/MM/YYYY');
  V_DTFIN DATE := TO_DATE('&P_DTFIN', 'DD/MM/YYYY');
  V_CODEMP NUMBER;
  V_CODEMPMATRIZ NUMBER;
  V_NUNOTA NUMBER;
  V_CODTIPOPER NUMBER;
  V_APENASDIFAL VARCHAR2(1) := NVL('&P_APENASDIFAL', 'N');
  V_SEQTELA NUMBER := TO_NUMBER('&P_SEQTELA');
BEGIN
  -- Tratar valores NULL (quando 0 significa NULL)
  V_CODEMP := CASE WHEN TO_NUMBER('&P_CODEMP') = 0 THEN NULL ELSE TO_NUMBER('&P_CODEMP') END;
  V_CODEMPMATRIZ := CASE WHEN TO_NUMBER('&P_CODEMPMATRIZ') = 0 THEN NULL ELSE TO_NUMBER('&P_CODEMPMATRIZ') END;
  V_NUNOTA := CASE WHEN TO_NUMBER('&P_NUNOTA') = 0 THEN NULL ELSE TO_NUMBER('&P_NUNOTA') END;
  V_CODTIPOPER := CASE WHEN TO_NUMBER('&P_CODTIPOPER') = 0 THEN NULL ELSE TO_NUMBER('&P_CODTIPOPER') END;
BEGIN
  DBMS_OUTPUT.PUT_LINE('Iniciando execução da procedure PROC_INCLUI_PARCEIRO_UFS_NM...');
  DBMS_OUTPUT.PUT_LINE('Parâmetros:');
  DBMS_OUTPUT.PUT_LINE('  DTINI: ' || TO_CHAR(V_DTINI, 'DD/MM/YYYY'));
  DBMS_OUTPUT.PUT_LINE('  DTFIN: ' || TO_CHAR(V_DTFIN, 'DD/MM/YYYY'));
  DBMS_OUTPUT.PUT_LINE('  CODEMP: ' || NVL(TO_CHAR(V_CODEMP), 'NULL'));
  DBMS_OUTPUT.PUT_LINE('  CODEMPMATRIZ: ' || NVL(TO_CHAR(V_CODEMPMATRIZ), 'NULL'));
  DBMS_OUTPUT.PUT_LINE('  NUNOTA: ' || NVL(TO_CHAR(V_NUNOTA), 'NULL'));
  DBMS_OUTPUT.PUT_LINE('  CODTIPOPER: ' || NVL(TO_CHAR(V_CODTIPOPER), 'NULL'));
  DBMS_OUTPUT.PUT_LINE('  APENASDIFAL: ' || V_APENASDIFAL);
  DBMS_OUTPUT.PUT_LINE('  SEQTELA: ' || TO_CHAR(V_SEQTELA));
  DBMS_OUTPUT.PUT_LINE('');
  
  BEGIN
    PROC_INCLUI_PARCEIRO_UFS_NM(
      P_DTINI => V_DTINI,
      P_DTFIN => V_DTFIN,
      P_CODEMP => V_CODEMP,
      P_CODEMPMATRIZ => V_CODEMPMATRIZ,
      P_NUNOTA => V_NUNOTA,
      P_CODTIPOPER => V_CODTIPOPER,
      P_APENASDIFAL => V_APENASDIFAL,
      P_SEQTELA => V_SEQTELA,
      P_MENSAGEM => V_MENSAGEM
    );
    
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('EXECUÇÃO CONCLUÍDA COM SUCESSO!');
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('Mensagem retornada:');
    DBMS_OUTPUT.PUT_LINE(V_MENSAGEM);
    DBMS_OUTPUT.PUT_LINE('========================================');
    
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('========================================');
      DBMS_OUTPUT.PUT_LINE('ERRO NA EXECUÇÃO!');
      DBMS_OUTPUT.PUT_LINE('========================================');
      DBMS_OUTPUT.PUT_LINE('Código do Erro: ' || SQLCODE);
      DBMS_OUTPUT.PUT_LINE('Mensagem: ' || SQLERRM);
      DBMS_OUTPUT.PUT_LINE('========================================');
      RAISE;
  END;
END;
/

-- ============================================================================
-- VALIDAÇÃO: VERIFICAÇÃO DE RESULTADOS APÓS O TESTE
-- ============================================================================
PROMPT 
PROMPT ============================================================================
PROMPT 3. VALIDAÇÃO DOS RESULTADOS
PROMPT ============================================================================

PROMPT 
PROMPT Verificando parceiros criados (código >= 900000000)...
SELECT COUNT(*) AS QTD_PARCEIROS_CRIADOS
FROM TGFPAR
WHERE CODPARC BETWEEN 900000000 AND 999000000
  AND DTALTER >= TRUNC(SYSDATE);

PROMPT 
PROMPT Exibindo parceiros criados recentemente...
SELECT CODPARC,
       NOMEPARC,
       RAZAOSOCIAL,
       CODCID,
       IDENTINSCESTAD,
       DTALTER,
       CODPARCMATRIZ
FROM TGFPAR
WHERE CODPARC BETWEEN 900000000 AND 999000000
  AND DTALTER >= TRUNC(SYSDATE)
ORDER BY DTALTER DESC;

PROMPT 
PROMPT Verificando registros na tabela AD_TGFPARUF...
SELECT COUNT(*) AS QTD_REGISTROS_AD_TGFPARUF
FROM AD_TGFPARUF
WHERE CODPARC IN (
  SELECT DISTINCT AD_CODPARCORIG
  FROM TGFCAB
  WHERE AD_CODPARCORIG IS NOT NULL
    AND DTENTSAI BETWEEN TO_DATE('&P_DTINI', 'DD/MM/YYYY') AND TO_DATE('&P_DTFIN', 'DD/MM/YYYY')
);

PROMPT 
PROMPT Exibindo registros na tabela AD_TGFPARUF (limitado a 50 registros)...
SELECT /*+ FIRST_ROWS(50) */ CODPARC,
       CODUF,
       INSCESTADNAUF,
       CODPARCVINC
FROM AD_TGFPARUF
WHERE CODPARC IN (
  SELECT /*+ FIRST_ROWS(50) */ DISTINCT AD_CODPARCORIG
  FROM TGFCAB
  WHERE AD_CODPARCORIG IS NOT NULL
    AND DTENTSAI BETWEEN TO_DATE('&P_DTINI', 'DD/MM/YYYY') AND TO_DATE('&P_DTFIN', 'DD/MM/YYYY')
    AND ROWNUM <= 50
)
  AND ROWNUM <= 50
ORDER BY CODPARC;

PROMPT 
PROMPT Verificando notas alteradas (AD_CODPARCORIG preenchido)...
SELECT COUNT(*) AS QTD_NOTAS_ALTERADAS
FROM TGFCAB
WHERE AD_CODPARCORIG IS NOT NULL
  AND DTENTSAI BETWEEN TO_DATE('&P_DTINI', 'DD/MM/YYYY') AND TO_DATE('&P_DTFIN', 'DD/MM/YYYY');

PROMPT 
PROMPT Exibindo notas alteradas (limitado a 50 registros)...
SELECT /*+ FIRST_ROWS(50) */ C.NUNOTA,
       C.CODPARC AS COD_PARC_NOVO,
       C.AD_CODPARCORIG AS COD_PARC_ORIG,
       P_ORIG.NOMEPARC AS NOME_PARC_ORIG,
       P_NOVO.NOMEPARC AS NOME_PARC_NOVO,
       C.DTENTSAI
FROM TGFCAB C
LEFT JOIN TGFPAR P_ORIG ON P_ORIG.CODPARC = C.AD_CODPARCORIG
LEFT JOIN TGFPAR P_NOVO ON P_NOVO.CODPARC = C.CODPARC
WHERE C.AD_CODPARCORIG IS NOT NULL
  AND C.DTENTSAI BETWEEN TO_DATE('&P_DTINI', 'DD/MM/YYYY') AND TO_DATE('&P_DTFIN', 'DD/MM/YYYY')
  AND ROWNUM <= 50
ORDER BY C.DTENTSAI DESC, C.NUNOTA DESC;

PROMPT 
PROMPT Verificando registros na tabela AD_TGFPARENDORIG...
SELECT COUNT(*) AS QTD_REGISTROS_AD_TGFPARENDORIG
FROM AD_TGFPARENDORIG
WHERE SEQUENCIA = &P_SEQTELA;

PROMPT 
PROMPT Exibindo registros na tabela AD_TGFPARENDORIG...
SELECT SEQUENCIA,
       CODPARC,
       UFORIG,
       INSCESTADNAUFORIG,
       CODCIDORIG,
       UFNOVA,
       INSCESTADNAUFNOVA,
       CODCIDNOVA,
       CODPARCNOVO
FROM AD_TGFPARENDORIG
WHERE SEQUENCIA = &P_SEQTELA
ORDER BY CODPARC;

PROMPT 
PROMPT ============================================================================
PROMPT TESTE CONCLUÍDO
PROMPT ============================================================================
PROMPT 
PROMPT IMPORTANTE: Revise os resultados acima para validar se as alterações
PROMPT foram aplicadas corretamente. Se necessário, faça ROLLBACK das alterações.
PROMPT 
