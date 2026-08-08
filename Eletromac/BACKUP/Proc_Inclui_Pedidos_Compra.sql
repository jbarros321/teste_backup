CREATE OR REPLACE PROCEDURE SANKHYA.Proc_Inclui_Pedidos_Compra
IS
/* ----------------------------------------------------------------------------
-- OBJETO: PROCEDURE PERSONALIZADA
-- AUTOR: LEANDRO MARCOS MOREIRA
-- ATUALIZADO: 30/11/2019 11:41
-- INTUITO: LANÇAR AUTOMATICAMENTE PEDIDOS DE COMPRA COM BASE EM SUGESTÃO DE DASHBOARD.
--          CONSIDERE A PROCEDURE PROC_ANALISE_COMPRAS TENDO EM VISTA A TABELA ANALISE_COMPRAS
*/ ----------------------------------------------------------------------------

   P_COUNT INT;                            P_NUNOTA INT;                           P_NUMNOTA INT := 0;
   P_VLRNOTA FLOAT;                        P_ITENS NUMBER (10);                    P_VLRMINPEDCPA FLOAT;
   P_QTDMAXPEDCPA NUMBER(10);              P_SEQUENCIA INT := 0;                   P_QTDNEG FLOAT;
   I_CODEMP INT;                           I_CODLOCALORIG INT := 1;                I_CODPROD INT;
   I_CONTROLE VARCHAR2(10);                I_CODVOL VARCHAR(5);                    I_CODVEND INT;
   I_ALIQIPI NUMBER;                       I_QTDNEG NUMBER := 0;                   I_QTDENTREGUE NUMBER := 0;
   I_QTDCONFERIDA NUMBER := 0;             I_VLRUNIT NUMBER := 0;                  I_VLRTOT NUMBER;
   I_VLRCUS NUMBER := 0;                   I_BASEIPI NUMBER := 0;                  I_VLRIPI NUMBER;
   I_ATUALESTOQUE INT := 0;                I_USOPROD VARCHAR(2) := 'R';            I_CODCFO INT := 0;
   I_BASEICMS NUMBER := 0;                 I_VLRICMS NUMBER := 0;                  I_VLRDESC NUMBER := 0;
   I_BASESUBSTIT NUMBER := 0;              I_VLRSUBST NUMBER := 0;                 I_PENDENTE VARCHAR(2) := 'S';
   I_RESERVA VARCHAR(2) := 'N';            I_STATUSNOTA VARCHAR(2) := 'A';         I_CODEXEC INT := 0;
   I_FATURAR VARCHAR(2) := 'S';            I_VLRREPRED NUMBER := 0;                I_VLRDESCBONIF NUMBER := 0;
   I_PERCDESC NUMBER := 0;                 I_CODPARC INT;                          I_DESCRPROD VARCHAR2(50);     
   N_CODEMP INT := 0;                      N_DTNEG VARCHAR2(15) := TO_CHAR(SYSDATE, 'DD/MM/YYYY');     N_DTENTSAI VARCHAR2(15) := TO_CHAR(SYSDATE, 'DD/MM/YYYY');
   N_DTMOV VARCHAR2(15) := TO_CHAR(SYSDATE, 'DD/MM/YYYY');      N_HRMOV VARCHAR2(10) := TO_CHAR(SYSDATE, 'HHMMSS');    N_CODEMPNEGOC INT;
   N_CODPARC INT := 0;                     N_RATEADO VARCHAR(2) := 'N';            N_CODVEICULO INT := 0;
   N_CODTIPOPER INT;                       N_DHTIPOPER DATE;                       N_TIPMOV VARCHAR2(2) := 'O';
   N_CODTIPVENDA INT;                      N_DHTIPVENDA DATE;                      N_CODVEND INT;
   N_COMISSAO INT := 0;                    N_CODMOEDA INT := 0;                    N_CODOBSPADRAO INT := 0;
   N_OBSERVACAO VARCHAR2(4000) := 'Citar o número dessa ordem de compra em sua NF-e.' || CHR(10) ||
                                  'Não aceitamos pedidos em desacordo com a ordem de compra.' || CHR(10) ||
                                  'Em caso de faturamento parcial comunicar ao departamento de compras.' || CHR(10) ||
                                  'Guia de substituição tributária deve vir anexa a nota (em caso de tomador ser o forncedor).' || CHR(10) ||
                                  'Enviar os arquivos XML para compras2@eletromac.com.br.' || CHR(10) ||
                                  'Não aceitamos títulos descontados por terceiros.' || CHR(10) ||
                                  'LANCAMENTO REALIZADO VIA PEDIDO AUTOMATICO';
   N_VLRSEG INT := 0;                          N_VLRICMSSEG INT := 0;
   N_VLRDESTAQUE INT := 0;                 N_VLRJURO INT := 0;                     N_VLRVENDOR INT := 0;
   N_VLROUTROS INT := 0;                   N_VLREMB INT := 0;                      N_VLRICMSEMB INT := 0;
   N_VLRDESCSERV INT := 0;                 N_IPIEMB INT := 0;                      N_TIPIPIEMB VARCHAR(2) := 'N';
   N_VLRDESCTOT INT := 0;                  N_VLRDESCTOTITEM INT := 0;              N_VLRFRETE INT := 0;
   N_ICMSFRETE INT := 0;                   N_BASEICMSFRETE INT := 0;               N_TIPFRETE VARCHAR(2):= 'N';
   N_CIF_FOB VARCHAR(2) := 'F';            N_VLRNOTA NUMBER;                       N_CODPARCTRANSP INT := 0;
   N_QTDVOL INT := 0;                      N_PENDENTE VARCHAR(2) := 'S';           N_BASEICMS NUMBER := 0;
   N_VLRICMS NUMBER := 0;                  N_BASEIPI NUMBER;                       N_VLRIPI NUMBER;
   N_ISSRETIDO VARCHAR(2) := 'N';          N_BASEISS NUMBER := 0;                  N_VLRISS NUMBER := 0;
   N_APROVADO VARCHAR(2) := 'N';           N_STATUSNOTA VARCHAR(2) := 'A';         N_CODUSU INT;
   N_IRFRETIDO VARCHAR(2) := 'S';          N_VLRIRF NUMBER := 0;                   N_DTALTER DATE := SYSDATE;
   N_CODPARCDEST INT := 0;                 N_VLRSUBST NUMBER := 0;                 N_BASESUBSTIT NUMBER := 0;
   N_CODPROJ INT := 0;                     N_NUMCONTRATO INT := 0;                 N_BASEINSS NUMBER := 0;
   N_VLRINSS NUMBER := 0;                  N_VLRREPREDTOT NUMBER := 0;             N_PERCDESC NUMBER := 0;
   N_CODPARCREMETENTE INT := 0;            N_CODPARCCONSIGNATARIO INT := 0;        N_CODPARCREDESPACHO INT := 0;
   N_CODNAT INT := 0;                      N_VLRFRETECPL NUMBER := 0;              N_NROREDZ INT := 0;
   N_VLRMOEDA INT := 0;                    N_CODUSUINC INT;                        N_NUTRANSF INT := 0;
   N_CODCID INT := 0;                      N_HRENTSAI VARCHAR2(15) := TO_CHAR(SYSDATE, 'DD/MM/YYYY');    N_AGRUPBOL VARCHAR(2) := 'N';
   N_PRODUETLOC VARCHAR(2) := 'N';
      
    CURSOR cBuscaNota IS
      SELECT CODEMP
         , CODEMPNEGOC
         , CODPARC
         , CODTIPOPER
         , DHTIPOPER
         , AD_CODTIPVENDA
         , DHTIPVENDA
         , AD_COMPRADOR
         , VLRNOTA
         , BASEIPI
         , VLRIPI
         , CODUSU
         , CODUSUINC
         , ITENS
         , AD_VLRMINPEDCPA
         , AD_QTDMAXPEDCPA
     FROM
  (SELECT COM.CODEMP AS CODEMP
        , COM.CODEMP AS CODEMPNEGOC
        , COM.CODPARC
        , 3 AS CODTIPOPER
        , (SELECT MAX(DHALTER) FROM TGFTOP WHERE CODTIPOPER = 3) AS DHTIPOPER
        , PAR.AD_CODTIPVENDA
        , (SELECT MAX(DHALTER) FROM TGFTPV WHERE CODTIPVENDA = PAR.AD_CODTIPVENDA) AS DHTIPVENDA
        , PRO.AD_COMPRADOR
        , ROUND(SUM((COM.SUGESTAO * Obtem_Custo_REP(COM.CODPROD)) * ( CASE WHEN IPI.PERCENTUAL <> NULL AND IPI.PERCENTUAL > 0 THEN IPI.PERCENTUAL / 100 ELSE 0 END)), 2) + SUM(COM.SUGESTAO * Obtem_Custo_REP(COM.CODPROD)) AS VLRNOTA
        , SUM(CASE WHEN PRO.TEMIPICOMPRA = 'S' THEN CASE WHEN IPI.PERCENTUAL IS NOT NULL OR IPI.PERCENTUAL > 0 THEN COM.SUGESTAO * Obtem_Custo_REP(COM.CODPROD) ELSE 0 END ELSE 0 END ) AS BASEIPI
        , ROUND(SUM(CASE WHEN PRO.TEMIPICOMPRA = 'S' THEN CASE WHEN IPI.PERCENTUAL IS NOT NULL OR IPI.PERCENTUAL > 0 THEN (COM.SUGESTAO * Obtem_Custo_REP(COM.CODPROD)) * (IPI.PERCENTUAL / 100) ELSE 0 END ELSE 0 END), 2) AS VLRIPI
        , CASE WHEN PRO.AD_COMPRADOR = 100 THEN 98 ELSE 0 END AS CODUSU
        , CASE WHEN PRO.AD_COMPRADOR = 100 THEN 98 ELSE 0 END AS CODUSUINC
        , COUNT(1) AS ITENS
        , PAR.AD_VLRMINPEDCPA
        , PAR.AD_QTDMAXPEDCPA
      FROM (SELECT COM.CODEMP
                 , COM.CODPROD
                 , COM.CONTROLE
                 , COM.CODPARC
                 , COM.SOLCOMPRA
                 , SUM(COM.SUGESTAO) AS SUGESTAO
              FROM ANALISE_COMPRAS_NOVA COM
          GROUP BY COM.CODEMP
                 , COM.CODPROD
                 , COM.CONTROLE
                 , COM.CODPARC
                 , COM.SOLCOMPRA) COM
INNER JOIN TGFPRO PRO ON PRO.CODPROD = COM.CODPROD
INNER JOIN TGFIPI IPI ON IPI.CODIPI = PRO.CODIPI
INNER JOIN TGFPAR PAR ON PAR.CODPARC = COM.CODPARC
     WHERE PAR.AD_VLRMINPEDCPA IS NOT NULL
         AND PAR.AD_QTDMAXPEDCPA IS NOT NULL
         AND PAR.AD_CODTIPVENDA  IS NOT NULL
       AND PAR.AD_VLRMINPEDCPA > 0
       AND PAR.AD_QTDMAXPEDCPA > 0
       AND PAR.AD_CODTIPVENDA > 0
       AND COM.SUGESTAO > 0
       AND COM.SOLCOMPRA = 'S'
       AND PAR.ATIVO = 'S'
       AND PRO.ATIVO = 'S'
  GROUP BY COM.CODPARC
         , PRO.AD_COMPRADOR
         , PAR.AD_VLRMINPEDCPA
         , PAR.AD_QTDMAXPEDCPA
         , PAR.AD_CODTIPVENDA)
  WHERE VLRNOTA >= AD_VLRMINPEDCPA
    AND ITENS <= AD_QTDMAXPEDCPA
    /*
    AND CODPARC NOT IN (SELECT C.CODPARC 
                          FROM TGFCAB C
                         WHERE C.OBSERVACAO LIKE '%LANCAMENTO REALIZADO VIA PEDIDO AUTOMATICO%'
                           AND C.TIPMOV = 'O'
                           AND C.STATUSNOTA = 'A')
    */
    ORDER BY CODEMP ASC
           , AD_COMPRADOR ASC
           , CODPARC ASC;

    CURSOR cBuscaItens IS
    SELECT COM.CODEMP AS CODEMP
         , COM.CODPROD
         , COM.CONTROLE
         , PRO.CODVOL
         , PRO.AD_COMPRADOR
         , NVL(IPI.PERCENTUAL,0) AS ALIQIPI
         , SUM(COM.SUGESTAO) AS QTDNEG
         , NVL(Obtem_Custo_REP(COM.CODPROD), 0) AS VLRUNIT
         , NVL(SUM(COM.SUGESTAO) * Obtem_Custo_REP(COM.CODPROD), 0) AS VLRTOT
         , NVL(Obtem_Custo_REP(COM.CODPROD), 0) AS VLRCUS
         , CASE WHEN PRO.TEMIPICOMPRA = 'S' AND PRO.TEMIPIVENDA = 'S' THEN CASE WHEN NVL(IPI.PERCENTUAL,0) = 0 THEN 0 ELSE SUM(COM.SUGESTAO * Obtem_Custo_REP(COM.CODPROD)) END ELSE 0 END AS BASEIPI
         , CASE WHEN PRO.TEMIPICOMPRA = 'S' AND PRO.TEMIPIVENDA = 'S' THEN ROUND(SUM(COM.SUGESTAO * Obtem_Custo_REP(COM.CODPROD)) * NVL(IPI.PERCENTUAL / 100,0), 2) ELSE 0 END AS VLRIPI
         , PAR.CODPARC
         , PRO.DESCRPROD
      FROM ANALISE_COMPRAS_NOVA COM
INNER JOIN TGFPRO PRO ON PRO.CODPROD = COM.CODPROD
INNER JOIN TGFIPI IPI ON IPI.CODIPI = PRO.CODIPI
INNER JOIN TGFPAR PAR ON PAR.CODPARC = COM.CODPARC
     WHERE COM.SUGESTAO > 0
        AND COM.CODPARC > 0
        AND PAR.AD_VLRMINPEDCPA IS NOT NULL
        AND PAR.AD_QTDMAXPEDCPA IS NOT NULL
        AND PAR.AD_CODTIPVENDA  IS NOT NULL
        AND PAR.AD_VLRMINPEDCPA > 0
        AND PAR.AD_QTDMAXPEDCPA > 0
        AND PAR.AD_CODTIPVENDA > 0
        AND COM.SOLCOMPRA = 'S'
        AND PAR.ATIVO = 'S'
        AND PRO.ATIVO = 'S'
        /*
        AND PAR.CODPARC NOT IN (SELECT C.CODPARC 
                                  FROM TGFCAB C
                                 WHERE C.OBSERVACAO LIKE '%LANCAMENTO REALIZADO VIA PEDIDO AUTOMATICO%'
                                   AND C.TIPMOV = 'O'
                                   AND C.STATUSNOTA = 'A')
        */
 GROUP BY COM.CODPROD
        , COM.CONTROLE
        , PRO.CODVOL
        , PRO.AD_COMPRADOR
        , IPI.PERCENTUAL
        , PRO.TEMIPICOMPRA
        , PRO.TEMIPIVENDA
        , PAR.CODPARC
        , PRO.DESCRPROD
 ORDER BY COM.CODEMP
        , PRO.AD_COMPRADOR ASC
        , PAR.CODPARC ASC
        , PRO.DESCRPROD ASC;

BEGIN

   OPEN cBuscaNota;
   OPEN cBuscaItens;
   
   LOOP
   
       FETCH cBuscaNota INTO
         N_CODEMP,
         N_CODEMPNEGOC,
         N_CODPARC,
         N_CODTIPOPER,
         N_DHTIPOPER,
         N_CODTIPVENDA,
         N_DHTIPVENDA,
         N_CODVEND,
         N_VLRNOTA,
         N_BASEIPI,
         N_VLRIPI,
         N_CODUSU,
         N_CODUSUINC,         
         P_ITENS,
         P_VLRMINPEDCPA,
         P_QTDMAXPEDCPA;
         
     EXIT WHEN cBuscaNota%NOTFOUND;
   
      SELECT ULTCOD + 1 INTO P_NUNOTA FROM TGFNUM WHERE ARQUIVO = 'TGFCAB';
      
      UPDATE TGFNUM SET ULTCOD = ULTCOD + 1 WHERE ARQUIVO = 'TGFCAB';

   INSERT INTO TGFCAB (NUNOTA, NUMNOTA, CODEMP, DTNEG, DTENTSAI, DTMOV, HRMOV, CODEMPNEGOC, CODPARC, RATEADO,
                       CODVEICULO, CODTIPOPER, DHTIPOPER, TIPMOV, CODTIPVENDA, DHTIPVENDA, CODVEND,
                       COMISSAO, CODMOEDA, CODOBSPADRAO, OBSERVACAO, VLRSEG, VLRICMSSEG, VLRDESTAQUE,
                       VLRJURO, VLRVENDOR, VLROUTROS, VLREMB, VLRICMSEMB, VLRDESCSERV, IPIEMB,
                       TIPIPIEMB, VLRDESCTOT, VLRDESCTOTITEM, VLRFRETE, ICMSFRETE, BASEICMSFRETE,
                       TIPFRETE, CIF_FOB, VLRNOTA, CODPARCTRANSP, QTDVOL, PENDENTE, BASEICMS,
                       VLRICMS, BASEIPI, VLRIPI, ISSRETIDO, BASEISS, VLRISS, APROVADO, STATUSNOTA,
                       CODUSU, IRFRETIDO, VLRIRF, DTALTER, CODPARCDEST, VLRSUBST, BASESUBSTIT,
                       CODPROJ, NUMCONTRATO, BASEINSS, VLRINSS, VLRREPREDTOT, PERCDESC,
                       CODPARCREMETENTE, CODPARCCONSIGNATARIO, CODPARCREDESPACHO, CODNAT,
                       VLRFRETECPL, NROREDZ, VLRMOEDA, CODUSUINC, NUTRANSF, CODCID, HRENTSAI,
                       AGRUPBOL, PRODUETLOC)
               VALUES (P_NUNOTA, P_NUMNOTA, 1, N_DTNEG, N_DTENTSAI, N_DTMOV, N_HRMOV, 1, N_CODPARC, N_RATEADO,
                       N_CODVEICULO, N_CODTIPOPER, N_DHTIPOPER, N_TIPMOV, N_CODTIPVENDA, N_DHTIPVENDA, N_CODVEND,
                       N_COMISSAO, N_CODMOEDA, N_CODOBSPADRAO, N_OBSERVACAO, N_VLRSEG, N_VLRICMSSEG, N_VLRDESTAQUE,
                       N_VLRJURO, N_VLRVENDOR, N_VLROUTROS, N_VLREMB, N_VLRICMSEMB, N_VLRDESCSERV, N_IPIEMB,
                       N_TIPIPIEMB, N_VLRDESCTOT, N_VLRDESCTOTITEM, N_VLRFRETE, N_ICMSFRETE, N_BASEICMSFRETE,
                       N_TIPFRETE, N_CIF_FOB, N_VLRNOTA, N_CODPARCTRANSP, N_QTDVOL, N_PENDENTE, N_BASEICMS,
                       N_VLRICMS, N_BASEIPI, N_VLRIPI, N_ISSRETIDO, N_BASEISS, N_VLRISS, N_APROVADO, N_STATUSNOTA,
                       N_CODUSU, N_IRFRETIDO, N_VLRIRF, N_DTALTER, N_CODPARCDEST, N_VLRSUBST, N_BASESUBSTIT,
                       N_CODPROJ, N_NUMCONTRATO, N_BASEINSS, N_VLRINSS, N_VLRREPREDTOT, N_PERCDESC,
                       N_CODPARCREMETENTE, N_CODPARCCONSIGNATARIO, N_CODPARCREDESPACHO, N_CODNAT,
                       N_VLRFRETECPL, N_NROREDZ, N_VLRMOEDA, N_CODUSUINC, N_NUTRANSF, N_CODCID, N_HRENTSAI,
                       N_AGRUPBOL, N_PRODUETLOC);
                       
                       P_SEQUENCIA := 0;
                       
                       LOOP
                       
                       FETCH cBuscaItens INTO
                         I_CODEMP,
                         I_CODPROD,
                         I_CONTROLE,
                         I_CODVOL,
                         I_CODVEND,
                         I_ALIQIPI,
                         I_QTDNEG,
                         I_VLRUNIT,
                         I_VLRTOT,
                         I_VLRCUS,
                         I_BASEIPI,
                         I_VLRIPI,
                         I_CODPARC,
                         I_DESCRPROD;
                         
     IF I_CODEMP = N_CODEMP AND I_CODVEND = N_CODVEND AND I_CODPARC = N_CODPARC THEN                       
        P_SEQUENCIA := P_SEQUENCIA + 1;
                                
     INSERT INTO TGFITE (NUNOTA, SEQUENCIA, CODEMP, CODLOCALORIG, CODPROD, CONTROLE, CODVOL, CODVEND,
                         ALIQIPI, QTDNEG, QTDENTREGUE, QTDCONFERIDA, VLRUNIT, VLRTOT, VLRCUS, BASEIPI,
                         VLRIPI, ATUALESTOQUE, USOPROD, CODCFO, BASEICMS, VLRICMS, VLRDESC, BASESUBSTIT,
                         VLRSUBST, PENDENTE, RESERVA, STATUSNOTA, CODEXEC, FATURAR, VLRREPRED, VLRDESCBONIF,
                         PERCDESC)
                 VALUES (P_NUNOTA, P_SEQUENCIA, 1, I_CODLOCALORIG, I_CODPROD, I_CONTROLE, I_CODVOL, I_CODVEND,
                         I_ALIQIPI, I_QTDNEG, I_QTDENTREGUE, I_QTDCONFERIDA, I_VLRUNIT, I_VLRTOT, I_VLRCUS, I_BASEIPI,
                         I_VLRIPI, I_ATUALESTOQUE, I_USOPROD, I_CODCFO, I_BASEICMS, I_VLRICMS, I_VLRDESC, I_BASESUBSTIT,
                         I_VLRSUBST, I_PENDENTE, I_RESERVA, I_STATUSNOTA, I_CODEXEC, I_FATURAR, I_VLRREPRED, I_VLRDESCBONIF,
                         I_PERCDESC);
                         
                         P_ITENS := P_ITENS -1;
                         
     END IF;

     EXIT WHEN P_ITENS = 0;                         
     END LOOP;                        
   END LOOP;
   
   CLOSE cBuscaNota;
   CLOSE cBuscaItens;
   
   COMMIT;
   
END;
/
