CREATE OR REPLACE PROCEDURE SANKHYA.Proc_Saldo_Estoque
IS
/*====================================================================================
   OBJETO:  PROCEDURE PERSONALIZADA
   AUTOR:   LEANDRO MARCOS MOREIRA (leandromarcosmoreira@gmail.com) 34 99120-4642
   INTUITO: OBTENÇÃO DE SALDO DE ESTOQUE HISTÓRICO.
   ÚLTIMA ATUALIZAÇÃO: 27/07/2018 AS 12:00
   ==================================================================================*/

   P_DTNEG TGFCAB.DTNEG%TYPE;
   P_CODEMP TGFCAB.CODEMP%TYPE;
   P_CODLOCAL TGFITE.CODLOCALORIG%TYPE;
   P_CODPROD TGFITE.CODPROD%TYPE;
   P_CONTROLE TGFITE.CONTROLE%TYPE;
   P_SALDO SALDO_ESTOQUE.SALDO%TYPE;
   
   CURSOR SaldoEstoque IS
    SELECT CAST (I.CODEMP AS FLOAT) AS CODEMP
       , CAST (I.CODPROD AS FLOAT) AS CODPROD
       , CAST (I.CODLOCALORIG AS FLOAT) AS CODLOCAL
       , I.CONTROLE
       , C.DTNEG
       , ROUND (
            SUM (
               CASE
                  WHEN P.USOPROD = 'S' THEN 0
                  ELSE ( (I.QTDNEG) * I.ATUALESTOQUE)
               END),
            P.DECQTD)
            AS SALDO
    FROM TGFCAB C
       , TGFITE I
       , TGFPRO P
       , TGFEMP EMP
   WHERE I.NUNOTA = C.NUNOTA
     AND I.RESERVA = 'N'
     AND I.CODEMP > 0
     AND EMP.CODEMP = I.CODEMP
     AND I.CODPROD = P.CODPROD
     AND I.ATUALESTOQUE <> 0
     AND P.USOPROD <> 'S'
GROUP BY I.CODEMP
       , I.CODPROD
       , I.CODLOCALORIG
       , I.CONTROLE
       , P.DECQTD
       , C.DTNEG
ORDER BY CODEMP
       , CODPROD
       , CODLOCAL
       , CONTROLE
       , DTNEG;
   /*
     SELECT I.CODEMP
          , I.CODLOCALORIG AS CODLOCAL
          , I.CODPROD
          , I.CONTROLE
          , C.DTNEG
          , SUM (I.QTDNEG * I.ATUALESTOQUE) OVER (PARTITION BY I.CODPROD, I.CONTROLE, I.CODEMP, I.CODLOCALORIG, C.DTNEG) AS ESTOQUE
       FROM TGFCAB C
          , TGFITE I
          , TGFPRO P
      WHERE C.NUNOTA = I.NUNOTA
        AND I.CODPROD = P.CODPROD
        AND I.RESERVA = 'N'
        AND I.ATUALESTOQUE <> 0
        AND P.USOPROD <> 'S'
   ORDER BY I.CODEMP
          , I.CODLOCALORIG
          , I.CODPROD
          , I.CONTROLE
          , C.DTNEG;
  */

BEGIN

  OPEN SaldoEstoque;

  LOOP

    FETCH SaldoEstoque 
     INTO P_CODEMP
        , P_CODPROD
        , P_CODLOCAL
        , P_CONTROLE
        , P_DTNEG
        , P_SALDO;

      UPDATE SALDO_ESTOQUE SAL 
         SET SAL.SALDO    = P_SALDO 
       WHERE SAL.CODEMP   = P_CODEMP
         AND SAL.CODLOCAL = P_CODLOCAL
         AND SAL.CODPROD  = P_CODPROD 
         AND SAL.CONTROLE = P_CONTROLE
         AND SAL.DTNEG    = P_DTNEG;

        IF SQL%ROWCOUNT = 0 THEN
          INSERT INTO SALDO_ESTOQUE SAL (SAL.CODEMP, SAL.CODLOCAL, SAL.CODPROD, SAL.CONTROLE, SAL.DTNEG, SAL.SALDO)
              VALUES(P_CODEMP, P_CODLOCAL, P_CODPROD, P_CONTROLE, P_DTNEG, P_SALDO);
        END IF;

   EXIT WHEN SaldoEstoque%NOTFOUND;
   END LOOP;

CLOSE SaldoEstoque;
COMMIT;
END;
/
