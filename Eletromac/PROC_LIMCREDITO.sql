CREATE OR REPLACE PROCEDURE SANKHYA.PROC_LIMCREDITO
IS
/*==================================================================================
   OBJETO:  PROCEDURE PERSONALIZADA
   AUTOR:   LEANDRO MARCOS MOREIRA (leandromarcosmoreira@gmail.com) 34 99120-4642
   INTUITO: Atualiza Limite de credito com base no movimento e atraso de pagamento
   ÚLTIMA ATUALIZAÇÃO: 14/11/2019 AS 17:00
   =================================================================================
*/
   P_CODPARC TGFPAR.CODPARC%TYPE;
   P_ATRASO VARCHAR2(2);
   P_LIMCRED FLOAT(126);

   CURSOR AnaliseCredito IS
   SELECT VW.CODPARC
        , CASE WHEN ROUND(MAX(VW.VLRDESDOB_TOT), 0) > 50000 THEN 50000 
               WHEN ROUND(MAX(VW.VLRDESDOB_TOT), 0) < 500 THEN 500
               ELSE ROUND(MAX(VW.VLRDESDOB_TOT), 0) END AS LIMCRED
        , (SELECT CASE WHEN TRUNC(SNK_DIVIDIR(SUM(CASE WHEN FIN.DHBAIXA IS NOT NULL THEN FIN.DHBAIXA - FIN.DTVENC
                                                       WHEN FIN.DHBAIXA IS NULL AND FIN.DTVENC < TRUNC (SYSDATE) THEN TRUNC (SYSDATE) - FIN.DTVENC 
                                                       ELSE 0 
                                                   END), COUNT(1))) >= 7 THEN 'S' 
                       ELSE 'N'
                   END AS ATRASO
             FROM TGFFIN FIN
            WHERE FIN.RECDESP = 1
              AND FIN.PROVISAO = 'N'
              AND FIN.CODTIPTIT NOT IN (0,18,27,99)
              AND FIN.DTNEG >= (SYSDATE - 90)
              AND FIN.CODPARC = VW.CODPARC) AS ATRASO
    FROM VW_ANALISE_CREDITO VW
   WHERE VW.CODPARC IN (SELECT PAR.CODPARC 
                          FROM TGFPAR PAR 
                         WHERE PAR.ATIVO = 'S'
                           AND PAR.BLOQUEAR = 'N'
                           AND PAR.LIMCRED > 500
                           AND PAR.GRUPOAUTOR LIKE '%B%'
                           AND PAR.CODTIPPARC NOT IN (40000, 50100, 50200))
GROUP BY VW.CODPARC
ORDER BY LIMCRED DESC;

BEGIN
  OPEN AnaliseCredito;
  LOOP
  FETCH AnaliseCredito INTO
        P_CODPARC
      , P_LIMCRED
      , P_ATRASO;

   -- ATUALIZA LIMITE DE CREDITO
   UPDATE TGFPAR UP
      SET UP.LIMCRED = CASE WHEN P_ATRASO = 'N' THEN ROUND(P_LIMCRED * 1.30, 0)
                            ELSE ROUND(P_LIMCRED, 0)
                        END
    WHERE UP.CODPARC = P_CODPARC;

  EXIT WHEN AnaliseCredito%NOTFOUND;
  END LOOP;
  CLOSE AnaliseCredito;
END;
/
