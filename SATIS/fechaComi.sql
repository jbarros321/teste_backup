CREATE OR REPLACE PROCEDURE          DB_ATUALIZA_RESUMO_FECH (
           P_CODUSU NUMBER,        -- Código do usuário logado
           P_IDSESSAO VARCHAR2,    -- Identificador da execução. Serve para buscar informações dos parâmetros/campos da execução.
           P_QTDLINHAS NUMBER,     -- Informa a quantidade de registros selecionados no momento da execução.
           P_MENSAGEM OUT VARCHAR2 -- Caso seja passada uma mensagem aqui, ela será exibida como uma informação ao usuário.
    ) AS
           FIELD_NUFECH          NUMBER;
           FIELD_SEQUE           NUMBER;
           P_DTINICIO            DATE;
           P_DTFIM               DATE;
           P_COUNT               INT;
           P_MAXSEQ              INT;
           P_MAXSEQ2             INT;
           P_MAXSEQ3             INT;
           P_MAXSEQ4             INT;
           P_COUNT2              INT;
           P_TIPO                VARCHAR2(1000);
           P_CODPARC             INT;
           P_VALOR               NUMBER;
           P_DESLIGADO           VARCHAR2(1000);
           P_REMFIXA             NUMBER;
           P_VLRTOT              NUMBER;
           P_TIPO2               VARCHAR2(1000);
           P_CODPARC2            INT;
           P_VALOR2              NUMBER;
           P_DESLIGADO2          VARCHAR2(1000);
           P_REMFIXA2            NUMBER;
           P_VLRTOT2             NUMBER;
           P_EXTRA               NUMBER;
           P_EXTRA2              NUMBER;
           P_VEND                INT;
           P_VEND2               INT;
           P_ADTFIXO             NUMBER;
           P_ADTFIXO2            NUMBER;

          CURSOR VEND_COMI IS                     
          SELECT 
            V.CODVEND,'V', V.CODPARC, ROUND(SUM(R.VLRCOM),6), CASE WHEN V.AD_DESLIGADO = 'S' THEN 'SIM' ELSE 'NÃO' END AS DESLIGADO, NVL(VR.REMFIXA,0), NVL(VR.REMFIXA,0) + ROUND(SUM(R.VLRCOM),6),NVL(VR.EXTRAS,0) AS EXTRA, NVL(VR.ADIANTCOMISSAO,0)AS ADIANT_FIXO     
             FROM AD_DBFECHCOMNOTAS R
             INNER JOIN TGFVEN V ON R.CODVEND = V.CODVEND
             LEFT JOIN AD_VENDREGIAO VR ON VR.CODVEND = V.CODVEND AND VR.ATIVO = 'S'
          WHERE 
              NUFECH = FIELD_NUFECH
              AND R.VLRCOM <> 0 --| Incluido esta condição para que seja levando em consideração os valores das devoluções que entram como negativo
          GROUP BY V.CODPARC, V.CODVEND, V.AD_DESLIGADO, VR.REMFIXA,VR.EXTRAS,VR.ADIANTCOMISSAO;



          CURSOR VEND_SCOMI IS  
          SELECT 
               V.CODVEND,'V', V.CODPARC, 0 AS VLRCOM, CASE WHEN V.AD_DESLIGADO = 'S' THEN 'SIM' ELSE 'NÃO' END AS DESLIGADO, NVL(VR.REMFIXA,0),NVL(VR.REMFIXA,0),NVL(VR.EXTRAS,0) AS EXTRA, NVL(VR.ADIANTCOMISSAO,0)AS ADIANT_FIXO   
            FROM TGFVEN V
               INNER JOIN AD_VENDREGIAO VR ON VR.CODVEND = V.CODVEND  AND VR.ATIVO = 'S'
            WHERE 
                VR.REMFIXA <> 0 
                AND V.ATIVO = 'S'
                AND EXISTS (SELECT 1 FROM AD_DBFECHCOMFIN F WHERE F.CODVEND = V.CODVEND AND F.NUFECH = FIELD_NUFECH AND VALOR = 0);



    BEGIN
    
        EXECUTE IMMEDIATE 'ALTER TRIGGER TRG_UPT_TGFFIN DISABLE';
        EXECUTE IMMEDIATE 'ALTER TRIGGER TRG_AD_DBFECHCOMFIN_DLT DISABLE';
        EXECUTE IMMEDIATE 'ALTER TRIGGER TRG_AD_DBFECHCOMNOTAS_DLT DISABLE';
        EXECUTE IMMEDIATE 'ALTER TRIGGER TRG_INC_UPD_TGFPAR_SATIS DISABLE';
        EXECUTE IMMEDIATE 'ALTER TRIGGER TRG_AD_TGFINVENCI_SATIS DISABLE';




    
           FOR I IN 1..P_QTDLINHAS -- Este loop permite obter o valor de campos dos registros envolvidos na execução.
           LOOP                    -- A variável "I" representa o registro corrente.
               FIELD_NUFECH := Act_Int_Field(P_IDSESSAO, I, 'NUFECH');
    
               SELECT  DTINICIO, DTFIM INTO P_DTINICIO, P_DTFIM
               FROM AD_DBFECHCOM
               WHERE NUFECH = FIELD_NUFECH;
    
    
                --SE JÁ EXISTIR PEDIDO DE COMPRA O FECHAMENTO NÃO SERÁ ATUALIZADO
               SELECT COUNT(*) INTO P_COUNT
                 FROM AD_DBFECHCOMFIN
               WHERE NUFECH = FIELD_NUFECH
                 AND NUNOTA IS NOT NULL;

                SELECT COUNT(*) INTO P_COUNT2 FROM AD_DBFECHCOMFIN WHERE NUFECH = FIELD_NUFECH; 
    
                 IF P_COUNT > 0 THEN 
                 P_MENSAGEM := 'Este fechamento já possui Pedido gerado não pode ser feito novamente!';
                 RETURN;
                 END IF;


               --INICIA A ATUALIZAÇÃO DO FECHAMENTO
               
               --ESTORNA QUALQUER VINCULO DE FINANCEIRO/NOTA COM O FECHAMENTO QUE ESTÁ SENDO ATUALIZADO (TGFFIN.AD_NUFECH = NULL)
               UPDATE TGFFIN SET AD_NUFECH = NULL
               WHERE AD_NUFECH = FIELD_NUFECH;
    
            /*
              --LIMPA A TABELA DE "Pedidos de Comissão dos Vendedores" PARA ATUALIZAR O FECHAMENTO
               DELETE FROM AD_DBFECHCOMFIN
               WHERE NUFECH = FIELD_NUFECH;
               COMMIT;
            */
               --LIMPA A TABELA DE "Notas que geram Comissão" PARA ATUALIZAR O FECHAMENTO
               DELETE FROM AD_DBFECHCOMNOTAS
               WHERE NUFECH = FIELD_NUFECH;
               COMMIT;
    
    
            --VENDEDORES ATIVOS (NÃO DESLIGADOS)
            --FINANCEIRO NORMAL/NÃO RENEGOCIADO
            INSERT INTO AD_DBFECHCOMNOTAS (NUFECH, SEQUENCIA, NUNOTA, CODEMP, NUMNOTA, DTNEG, CODPARC, NUFIN, NUFINREN, DTVENCIMENTO, DHBAIXA, VLRBAIXA, VLRCOM, CODVEND, TIPO, DESCICMS, DESCCOMISSAO, DESCFIN, VLRDESDOB, VLRBAIXAORIG, VLRJURO, VLRMULTA, TIPOFIN, PERCCOMVEND, VLRTAXA)
             (
             SELECT 
                 FIELD_NUFECH , 
                 ROWNUM, 
                 CAB.NUNOTA, 
                 CAB.CODEMP, 
                 CAB.NUMNOTA, 
                 CAB.DTNEG, 
                 CAB.CODPARC, 
                 FIN.NUFIN, 
                 NULL AS NUFINREN,
                 FIN.DTVENC, 
                 FIN.DHBAIXA, 
                ROUND(
                    ((FIN.VLRBAIXA - FIN.VLRJURO - FIN.VLRMULTA)
                    - ((CAB.VLRICMS / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA)) * FIN.VLRDESDOB)
                    - NVL((CASE WHEN CCM.AD_DIMINUIBASE = 'S' THEN 0 ELSE ((CAB.VLRNOTA - CAB.VLRICMS) * (SELECT SUM(PERCCOM) FROM TGFCCM WHERE NUNOTA = CAB.NUNOTA AND AD_DIMINUIBASE = 'S')/100) / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA) * FIN.VLRDESDOB END),0)
                    ) 
                ,6) * CASE WHEN CAB.TIPMOV = 'D' THEN -1 ELSE 1 END AS VLRBAIXA, 
                ROUND(
                    ((FIN.VLRBAIXA - FIN.VLRJURO - FIN.VLRMULTA)
                    - ((CAB.VLRICMS / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA)) * FIN.VLRDESDOB)
                    - NVL((CASE WHEN CCM.AD_DIMINUIBASE = 'S' THEN 0 ELSE ((CAB.VLRNOTA - CAB.VLRICMS) * (SELECT SUM(PERCCOM) FROM TGFCCM WHERE NUNOTA = CAB.NUNOTA AND AD_DIMINUIBASE = 'S')/100) / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA) * FIN.VLRDESDOB END),0)
                    ) * CCM.PERCCOM / 100
                ,6) * CASE WHEN CAB.TIPMOV = 'D' THEN -1 ELSE 1 END AS VLRCOM, 
                 CCM.CODVEND, 
                 CAB.TIPMOV,
                 ((CAB.VLRICMS / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA)) * FIN.VLRDESDOB) AS DESCICMS, 
                NVL((CASE WHEN CCM.AD_DIMINUIBASE = 'S' THEN 0 ELSE ((CAB.VLRNOTA - CAB.VLRICMS) * (SELECT SUM(PERCCOM) FROM TGFCCM WHERE NUNOTA = CAB.NUNOTA AND AD_DIMINUIBASE = 'S')/100) / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA) * FIN.VLRDESDOB END),0) AS DESCCOMISSAO,
                FIN.VLRDESC AS DESCFIN,
                FIN.VLRDESDOB,
                FIN.VLRBAIXA,
                FIN.VLRJURO,
                FIN.VLRMULTA,
                'VEND. ATIVO - FIN. NORMAL' AS TIPOFIN,
                CCM.PERCCOM AS PERCCOMVEND,
                FIN.CARTAODESC
            FROM 
                TGFCAB CAB, TGFPAR PAR, TGFFIN FIN, TSIEMP EMP, TGFTOP TPO, TGFVEN VEN, TGFTPV TPV, TGFCCM CCM
            WHERE 
                CAB.CODPARC = PAR.CODPARC 
                AND CAB.NUNOTA = FIN.NUNOTA
                AND CAB.CODEMP = EMP.CODEMP
                AND CAB.CODTIPOPER = TPO.CODTIPOPER
                AND CAB.DHTIPOPER = TPO.DHALTER
                AND CAB.CODTIPVENDA = TPV.CODTIPVENDA
                AND CAB.DHTIPVENDA = TPV.DHALTER
                AND CAB.NUNOTA = CCM.NUNOTA
                AND CCM.CODVEND = VEN.CODVEND
                AND TPO.ATUALCOM = 'C'
                AND NVL(VEN.AD_DESLIGADO,'N') = 'N'
                AND FIN.NURENEG IS NULL
                AND FIN.DHBAIXA >= P_DTINICIO
                AND FIN.DHBAIXA <= P_DTFIM
                --AND FIN.AD_NUFECH IS NULL
                AND NOT EXISTS (SELECT 1 FROM AD_DBFECHCOMNOTAS F WHERE F.NUFIN = FIN.NUFIN AND F.CODVEND = CCM.CODVEND)
               );
    
    
            SELECT MAX(SEQUENCIA) INTO P_MAXSEQ
                FROM AD_DBFECHCOMNOTAS
                WHERE NUFECH = FIELD_NUFECH;
    
    
    
            --VENDEDORES ATIVOS (NÃO DESLIGADOS)
            --FINANCEIRO RENEGOCIADO
            INSERT INTO AD_DBFECHCOMNOTAS (NUFECH, SEQUENCIA, NUNOTA, CODEMP, NUMNOTA, DTNEG, CODPARC, NUFIN, NUFINREN, DTVENCIMENTO, DHBAIXA, VLRBAIXA, VLRCOM, CODVEND, TIPO, DESCICMS, DESCCOMISSAO, DESCFIN, VLRDESDOB, VLRBAIXAORIG, VLRJURO, VLRMULTA, TIPOFIN, PERCCOMVEND, VLRTAXA)
            (
            SELECT 
                FIELD_NUFECH , 
                ROWNUM + P_MAXSEQ, 
                CAB.NUNOTA, 
                CAB.CODEMP, 
                CAB.NUMNOTA, 
                CAB.DTNEG, 
                CAB.CODPARC, 
                FIN.NUFIN, 
                V.NUFINNOVO AS NUFINREN,
                FIN.DTVENC, 
                FIN.DHBAIXA, 
                (V.VLRPROP - V.VLRICMS) * CASE WHEN CAB.TIPMOV = 'D' THEN -1 ELSE 1 END AS VLRBAIXA, 
                ROUND(
                (V.VLRPROP - V.VLRICMS - V.VLRDESCCOM)
                * CCM.PERCCOM / 100,6)
                * CASE WHEN CAB.TIPMOV = 'D' THEN -1 ELSE 1 END AS VLRCOM, 
                CCM.CODVEND, 
                CAB.TIPMOV,
                V.VLRICMS AS DESCICMS, 
                V.VLRDESCCOM AS DESCCOMISSAO, 
                V.VLRDESCPROP AS DESCFIN,
                FIN.VLRDESDOB,
                FIN.VLRBAIXA,
                FIN.VLRJURO,
                FIN.VLRMULTA,
                'VEND. ATIVO - FIN. RENEGOCIADO' AS TIPOFIN,
                CCM.PERCCOM AS PERCCOMVEND,
                V.CARTAODESC
            FROM 
                TGFCAB CAB, TGFPAR PAR, TGFFIN FIN, TSIEMP EMP, TGFTOP TPO, TGFVEN VEN, TGFTPV TPV, TGFCCM CCM, VRENEG V
            WHERE 
                CAB.CODPARC = PAR.CODPARC 
                AND CAB.NUNOTA = V.NUNOTA
                AND CAB.CODEMP = EMP.CODEMP
                AND CAB.CODTIPOPER = TPO.CODTIPOPER
                AND CAB.DHTIPOPER = TPO.DHALTER
                AND CAB.CODTIPVENDA = TPV.CODTIPVENDA
                AND CAB.DHTIPVENDA = TPV.DHALTER
                AND CAB.NUNOTA = CCM.NUNOTA
                AND CCM.CODVEND = VEN.CODVEND
                AND FIN.NUFIN = V.NUFINNOVO
                AND TPO.ATUALCOM = 'C'
                AND NVL(VEN.AD_DESLIGADO,'N') = 'N'
                AND FIN.NURENEG IS NOT NULL
                AND FIN.DHBAIXA >= P_DTINICIO
                AND FIN.DHBAIXA <= P_DTFIM
                --AND FIN.AD_NUFECH IS NULL
                AND NOT EXISTS (SELECT 1 FROM AD_DBFECHCOMNOTAS F WHERE F.NUFIN = FIN.NUFIN AND F.CODVEND = CCM.CODVEND)
               );
               
               
               
                
               SELECT MAX(SEQUENCIA) INTO P_MAXSEQ2
                 FROM AD_DBFECHCOMNOTAS
               WHERE NUFECH = FIELD_NUFECH;
    
            --VENDEDORES DESLIGADOS
            --FINANCEIRO NORMAL/NÃO RENEGOCIADO
            INSERT INTO AD_DBFECHCOMNOTAS (NUFECH, SEQUENCIA, NUNOTA, CODEMP, NUMNOTA, DTNEG, CODPARC, NUFIN, NUFINREN, DTVENCIMENTO, DHBAIXA, VLRBAIXA, VLRCOM, CODVEND, TIPO, DESCICMS, DESCCOMISSAO, DESCFIN, VLRDESDOB, VLRBAIXAORIG, VLRJURO, VLRMULTA, TIPOFIN, PERCCOMVEND, VLRTAXA)
           (
            SELECT 
                FIELD_NUFECH
                , ROWNUM+ P_MAXSEQ2
                , CAB.NUNOTA
                , CAB.CODEMP
                , CAB.NUMNOTA
                , CAB.DTNEG
                , CAB.CODPARC
                , FIN.NUFIN
                , NULL AS NUFINREN
                , FIN.DTVENC
                , FIN.DHBAIXA
                ,ROUND(  --VLR BAIXA - BASE DE CALCULO
                        (
                            FIN.VLRDESDOB
                            - ((CAB.VLRICMS / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA)) * FIN.VLRDESDOB)
                            - NVL((CASE WHEN CCM.AD_DIMINUIBASE = 'S' THEN 0 ELSE ((CAB.VLRNOTA - CAB.VLRICMS) * (SELECT SUM(PERCCOM) FROM TGFCCM WHERE NUNOTA = CAB.NUNOTA AND AD_DIMINUIBASE = 'S')/100) / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA) * FIN.VLRDESDOB END),0)
                        ) 
                        ,6)
                        * CASE WHEN CAB.TIPMOV = 'D' THEN -1 ELSE 1 END AS VLRBAIXA, 
                ROUND(  --VLRCOM
                        (
                            FIN.VLRDESDOB
                            - ((CAB.VLRICMS / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA)) * FIN.VLRDESDOB)
                            - NVL((CASE WHEN CCM.AD_DIMINUIBASE = 'S' THEN 0 ELSE ((CAB.VLRNOTA - CAB.VLRICMS) * (SELECT SUM(PERCCOM) FROM TGFCCM WHERE NUNOTA = CAB.NUNOTA AND AD_DIMINUIBASE = 'S')/100) / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA) * FIN.VLRDESDOB END),0)
                        ) 
                        * CCM.PERCCOM / 100,6)
                        * CASE WHEN CAB.TIPMOV = 'D' THEN -1 ELSE 1 END AS VLRCOM, 
                CCM.CODVEND, 
                CAB.TIPMOV,
                ((CAB.VLRICMS / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA)) * FIN.VLRDESDOB) AS DESCICMS, 
                NVL((CASE WHEN CCM.AD_DIMINUIBASE = 'S' THEN 0 ELSE ((CAB.VLRNOTA - CAB.VLRICMS) * (SELECT SUM(PERCCOM) FROM TGFCCM WHERE NUNOTA = CAB.NUNOTA AND AD_DIMINUIBASE = 'S')/100) / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA) * FIN.VLRDESDOB END),0) AS DESCCOMISSAO,
                FIN.VLRDESC AS DESCFIN,
                FIN.VLRDESDOB,
                FIN.VLRBAIXA,
                FIN.VLRJURO,
                FIN.VLRMULTA,
                'VEND. DESLIGADO - FIN. NORMAL' AS TIPOFIN,
                CCM.PERCCOM AS PERCCOMVEND,
                FIN.CARTAODESC
            FROM
                TGFCAB CAB, TGFPAR PAR, TGFFIN FIN, TSIEMP EMP, TGFTOP TPO, TGFVEN VEN, TGFTPV TPV, TGFCCM CCM
            WHERE
                CAB.CODPARC = PAR.CODPARC 
                AND CAB.NUNOTA = FIN.NUNOTA
                AND CAB.CODEMP = EMP.CODEMP
                AND CAB.CODTIPOPER = TPO.CODTIPOPER
                AND CAB.DHTIPOPER = TPO.DHALTER
                AND CAB.CODTIPVENDA = TPV.CODTIPVENDA
                AND CAB.DHTIPVENDA = TPV.DHALTER
                AND CAB.NUNOTA = CCM.NUNOTA
                AND CCM.CODVEND = VEN.CODVEND
                AND TPO.ATUALCOM = 'C'
                AND VEN.AD_DESLIGADO = 'S'
                AND CAB.STATUSNOTA = 'L'
                --AND FIN.DTVENC >= P_DTINICIO
                AND NOT EXISTS (SELECT 1 FROM AD_DBFECHCOMNOTAS WHERE NUFIN = FIN.NUFIN AND CODVEND = CCM.CODVEND)
                --AND FIN.DHBAIXA IS NULL
                --AND FIN.AD_NUFECH IS NULL
                AND FIN.NURENEG IS NULL
               );
    
    
               SELECT MAX(SEQUENCIA) INTO P_MAXSEQ3
                 FROM AD_DBFECHCOMNOTAS
               WHERE NUFECH = FIELD_NUFECH;
               
            --VENDEDORES DESLIGADOS
            --FINANCEIRO RENEGOCIADO
            INSERT INTO AD_DBFECHCOMNOTAS (NUFECH, SEQUENCIA, NUNOTA, CODEMP, NUMNOTA, DTNEG, CODPARC, NUFIN, NUFINREN, DTVENCIMENTO, DHBAIXA, VLRBAIXA, VLRCOM, CODVEND, TIPO, DESCICMS, DESCCOMISSAO, DESCFIN, VLRDESDOB, VLRBAIXAORIG, VLRJURO, VLRMULTA, TIPOFIN, PERCCOMVEND, VLRTAXA)
            (
            SELECT 
                FIELD_NUFECH , 
                ROWNUM + P_MAXSEQ3, 
                CAB.NUNOTA, 
                CAB.CODEMP, 
                CAB.NUMNOTA, 
                CAB.DTNEG, 
                CAB.CODPARC, 
                FIN.NUFIN, 
                V.NUFINNOVO AS NUFINREN,
                FIN.DTVENC, 
                FIN.DHBAIXA, 
                ROUND(
                    (V.VLRPROP - V.VLRICMS - V.VLRDESCCOM)
                ,6)
                * CASE WHEN CAB.TIPMOV = 'D' THEN -1 ELSE 1 END AS VLRBAIXA, 
                ROUND(
                (V.VLRPROP - V.VLRICMS - V.VLRDESCCOM)
                * CCM.PERCCOM / 100,6)
                * CASE WHEN CAB.TIPMOV = 'D' THEN -1 ELSE 1 END AS VLRCOM, 
                CCM.CODVEND, 
                CAB.TIPMOV,
                V.VLRICMS AS DESCICMS, 
                V.VLRDESCCOM AS DESCCOMISSAO, 
                V.VLRDESCPROP AS DESCFIN,
                FIN.VLRDESDOB,
                FIN.VLRBAIXA,
                FIN.VLRJURO,
                FIN.VLRMULTA,
                'VEND. DESLIGADO - FIN. RENEGOCIADO' AS TIPOFIN,
                CCM.PERCCOM AS PERCCOMVEND,
                V.CARTAODESC
            FROM 
                TGFCAB CAB, TGFPAR PAR, TGFFIN FIN, TSIEMP EMP, TGFTOP TPO, TGFVEN VEN, TGFTPV TPV, TGFCCM CCM, VRENEG V
            WHERE 
                CAB.CODPARC = PAR.CODPARC 
                AND CAB.NUNOTA = V.NUNOTA
                AND CAB.CODEMP = EMP.CODEMP
                AND CAB.CODTIPOPER = TPO.CODTIPOPER
                AND CAB.DHTIPOPER = TPO.DHALTER
                AND CAB.CODTIPVENDA = TPV.CODTIPVENDA
                AND CAB.DHTIPVENDA = TPV.DHALTER
                AND CAB.NUNOTA = CCM.NUNOTA
                AND CCM.CODVEND = VEN.CODVEND
                AND FIN.NUFIN = V.NUFINNOVO
                AND TPO.ATUALCOM = 'C'
                AND NVL(VEN.AD_DESLIGADO,'N') = 'S'
                AND FIN.NURENEG IS NOT NULL
                --AND FIN.DHBAIXA >= P_DTINICIO
                --AND FIN.AD_NUFECH IS NULL
                AND NOT EXISTS (SELECT 1 FROM AD_DBFECHCOMNOTAS F WHERE F.NUFIN = FIN.NUFIN AND F.CODVEND = CCM.CODVEND)
               );
               
    
    
    
    
    
               --Retirando estre trecho, pois segundo o cliente, lançamentos com a TOP 1303 não devem entrar na rotina de comissão.   
               
    /*
               --COMISSÕES DOS VENDEDORES REFERENTE AOS FINANCEIROS IMPORTADOS -- TRECHO ACRESCENTADO 11/04/22 - TALES ALVES(SANKHYA) 
    
               SELECT NVL(MAX(SEQUENCIA),0) INTO P_MAXSEQ3
                 FROM AD_DBFECHCOMNOTAS
               WHERE NUFECH = FIELD_NUFECH;
    
    
               INSERT INTO AD_DBFECHCOMNOTAS (NUFECH, SEQUENCIA, NUNOTA, CODEMP, NUMNOTA, DTNEG, CODPARC, NUFIN, DTVENCIMENTO, DHBAIXA, VLRBAIXA, VLRCOM, CODVEND, TIPO, DESCICMS, DESCCOMISSAO, DESCFIN)
             ( SELECT FIELD_NUFECH, ROWNUM + P_MAXSEQ3, FIN.NUNOTA, FIN.CODEMP, FIN.NUMNOTA, FIN.DTNEG, FIN.CODPARC, FIN.NUFIN, FIN.DTVENC, FIN.DHBAIXA, 
               (FIN.VLRBAIXA - FIN.VLRJURO - FIN.VLRMULTA) , (COM.PERCCOM *  (FIN.VLRBAIXA - FIN.VLRJURO - FIN.VLRMULTA)/100) , COM.CODVEND, 'F', 0, 0, FIN.VLRDESC
               FROM TGFFIN FIN
               JOIN TGFPAR PAR ON PAR.CODPARC = FIN.CODPARC
               JOIN AD_COMISSIONADOS COM ON COM.CODPARC = PAR.CODPARC
               WHERE FIN.CODTIPOPER = 1303
               AND FIN.NURENEG IS NULL
               AND FIN.RECDESP = 1
               AND NVL(COM.RECCOMANT,'N') = 'S'
               AND FIN.DHBAIXA >= P_DTINICIO
               AND FIN.DHBAIXA <= P_DTFIM
               AND FIN.AD_NUFECH IS NULL);
    
    
               SELECT NVL(MAX(SEQUENCIA),0) INTO P_MAXSEQ4
                 FROM AD_DBFECHCOMNOTAS
               WHERE NUFECH = FIELD_NUFECH;
    
    
               --COMISSÕES DOS VENDEDORES REFERENTE AOS FINANCEIROS IMPORTADOS RENEGOCIADOS -- TRECHO ACRESCENTADO 26/04/22 - TALES ALVES(SANKHYA) 
               INSERT INTO AD_DBFECHCOMNOTAS (NUFECH, SEQUENCIA, NUNOTA, CODEMP, NUMNOTA, DTNEG, CODPARC, NUFIN, DTVENCIMENTO, DHBAIXA, VLRBAIXA, VLRCOM, CODVEND, TIPO, DESCICMS, DESCCOMISSAO, DESCFIN)
             ( SELECT FIELD_NUFECH, ROWNUM + P_MAXSEQ4, FIN.NUNOTA, FIN.CODEMP, FIN.NUMNOTA, FIN.DTNEG, FIN.CODPARC, FIN.NUFIN, FIN.DTVENC, FIN.DHBAIXA, 
               (FIN.VLRBAIXA - FIN.VLRJURO - FIN.VLRMULTA) , (COM.PERCCOM *  (FIN.VLRBAIXA - FIN.VLRJURO - FIN.VLRMULTA)/100) , COM.CODVEND, 'F', 0, 0, FIN.VLRDESC
               FROM TGFFIN FIN
               JOIN TGFPAR PAR ON PAR.CODPARC = FIN.CODPARC
               JOIN AD_COMISSIONADOS COM ON COM.CODPARC = PAR.CODPARC
               JOIN VRENEG V ON V.NUFINNOVO = FIN.NUFIN
               WHERE FIN.CODTIPOPER = 1303
               AND FIN.NURENEG IS NOT NULL
               AND FIN.RECDESP = 1
               AND NVL(COM.RECCOMANT,'N') = 'S'
               AND FIN.DHBAIXA >= P_DTINICIO
               AND FIN.DHBAIXA <= P_DTFIM
               AND FIN.AD_NUFECH IS NULL);                    
    
            */  
            
            /* parte nova COMISSÃO FUTURA 
             Ajuste por Thiago Bonatti 05/06/2023
            */
    /*        
            DELETE FROM AD_DBFECHCOMNOTASA
            WHERE NUFECH = FIELD_NUFECH;
            
            -- comissões de vendedore NÃO demitidos (em aberto)
            INSERT INTO AD_DBFECHCOMNOTASA (NUFECH, SEQUENCIA, NUNOTA, CODEMP, NUMNOTA, DTNEG, CODPARC, NUFIN, DTVENCIMENTO, DHBAIXA, VLRBAIXA, VLRCOM, CODVEND, TIPO, DESCICMS, DESCCOMISSAO, DESCFIN)
           (SELECT FIELD_NUFECH , ROWNUM, CAB.NUNOTA, CAB.CODEMP, CAB.NUMNOTA, CAB.DTNEG, CAB.CODPARC, FIN.NUFIN, FIN.DTVENC, FIN.DHBAIXA, 
             FIN.VLRDESDOB * CASE WHEN CAB.TIPMOV = 'D' THEN -1 ELSE 1 END , 
            ROUND(
            (FIN.VLRDESDOB
            - ((CAB.VLRICMS / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA)) * FIN.VLRDESDOB)
            - NVL((CASE WHEN CCM.AD_DIMINUIBASE = 'S' THEN 0 ELSE ((CAB.VLRNOTA - CAB.VLRICMS) * (SELECT SUM(PERCCOM) FROM TGFCCM WHERE NUNOTA = CAB.NUNOTA AND AD_DIMINUIBASE = 'S')/100) / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA) * FIN.VLRDESDOB END),0)
            ) * CCM.PERCCOM / 100,6)
             * CASE WHEN CAB.TIPMOV = 'D' THEN -1 ELSE 1 END , 
             CCM.CODVEND, CAB.TIPMOV,
             ((CAB.VLRICMS / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA)) * FIN.VLRDESDOB), 
            NVL((CASE WHEN CCM.AD_DIMINUIBASE = 'S' THEN 0 ELSE ((CAB.VLRNOTA - CAB.VLRICMS) * (SELECT SUM(PERCCOM) FROM TGFCCM WHERE NUNOTA = CAB.NUNOTA AND AD_DIMINUIBASE = 'S')/100) / (SELECT SUM(VLRDESDOB) FROM TGFFIN WHERE NUNOTA = CAB.NUNOTA) * FIN.VLRDESDOB END),0),
            FIN.VLRDESC
              FROM TGFCAB CAB, TGFPAR PAR, TGFFIN FIN, TSIEMP EMP, TGFTOP TPO, TGFVEN VEN, TGFTPV TPV, TGFCCM CCM
            WHERE CAB.CODPARC = PAR.CODPARC 
               AND CAB.NUNOTA = FIN.NUNOTA
               AND CAB.CODEMP = EMP.CODEMP
               AND CAB.CODTIPOPER = TPO.CODTIPOPER
               AND CAB.DHTIPOPER = TPO.DHALTER
               AND CAB.CODTIPVENDA = TPV.CODTIPVENDA
               AND CAB.DHTIPVENDA = TPV.DHALTER
               AND CAB.NUNOTA = CCM.NUNOTA
               AND CCM.CODVEND = VEN.CODVEND
               AND TPO.ATUALCOM = 'C'
               AND NVL(VEN.AD_DESLIGADO,'N') = 'N'
               AND CAB.STATUSNOTA = 'L'
               AND FIN.DTVENC >= P_DTINICIO
               AND NOT EXISTS (SELECT 1 FROM AD_DBFECHCOMNOTAS WHERE NUFIN = FIN.NUFIN AND CODVEND = CCM.CODVEND)
               --AND FIN.DHBAIXA IS NULL
               AND FIN.AD_NUFECH IS NULL);        
    */
            /* FIM parte nova COMISSÃO FUTURA 
            */
            
            
                 --GERA/ATUALIZA OS REGISTROS DE PEDIDO DE COMPRA NA ABA "Fechamento Comissão"
                IF P_COUNT2 = 0 THEN 
            
                     INSERT INTO AD_DBFECHCOMFIN (NUFECH, SEQUENCIA, TIPO, CODPARC, VALOR, CODVEND, DESLIGADO, REMFIXA,  VLRTOT,EXTRA,ADIANTFIXO)
                      (
                      SELECT 
                          FIELD_NUFECH, V.CODVEND, 'V', V.CODPARC, ROUND(SUM(R.VLRCOM),6), V.CODVEND, CASE WHEN V.AD_DESLIGADO = 'S' THEN 'SIM' ELSE 'NÃO' END AS DESLIGADO, NVL(VR.REMFIXA,0), NVL(VR.REMFIXA,0) + ROUND(SUM(R.VLRCOM),6),NVL(VR.EXTRAS,0) AS EXTRA, NVL(VR.ADIANTCOMISSAO,0)AS ADIANT_FIXO
                      FROM AD_DBFECHCOMNOTAS R
                         INNER JOIN TGFVEN V ON R.CODVEND = V.CODVEND
                         LEFT JOIN AD_VENDREGIAO VR ON VR.CODVEND = V.CODVEND  AND VR.ATIVO = 'S'--AND CODCADASTRO = (SELECT MAX(CODCADASTRO) FROM AD_VENDREGIAO WHERE CODVEND = R.CODVEND) 
                      WHERE 
                          NUFECH = FIELD_NUFECH
                          AND R.VLRCOM <> 0 --| Incluido esta condição para que seja levando em consideração os valores das devoluções que entram como negativo
                         
                      GROUP BY V.CODPARC, V.CODVEND, V.AD_DESLIGADO, VR.REMFIXA,VR.EXTRAS,VR.ADIANTCOMISSAO
                      );
                      COMMIT;
     
                      --COMPLEMENTA A GERAÇÃO DOS REGISTROS DE PEDIDO DE COMPRA COM OS VENDEDORES QUE TEM REMUNERAÇÃO FIXA E NÃO TEM COMISSÃO
                     INSERT INTO AD_DBFECHCOMFIN (NUFECH, SEQUENCIA, TIPO, CODPARC, VALOR, CODVEND, DESLIGADO, REMFIXA,  VLRTOT,EXTRA,ADIANTFIXO)
                      (
                      SELECT 
                          FIELD_NUFECH, V.CODVEND, 'V', V.CODPARC, 0 AS VLRCOM, V.CODVEND, CASE WHEN V.AD_DESLIGADO = 'S' THEN 'SIM' ELSE 'NÃO' END AS DESLIGADO, NVL(VR.REMFIXA,0), NVL(VR.REMFIXA,0),NVL(VR.EXTRAS,0) AS EXTRA, NVL(VR.ADIANTCOMISSAO,0)AS ADIANT_FIXO
                      FROM TGFVEN V
                         INNER JOIN AD_VENDREGIAO VR ON VR.CODVEND = V.CODVEND AND VR.ATIVO = 'S'
                     WHERE 
                          (VR.REMFIXA <> 0 OR ADIANTCOMISSAO <> 0) 
                          AND V.ATIVO = 'S'
                          AND NOT EXISTS (SELECT 1 FROM AD_DBFECHCOMFIN F WHERE F.CODVEND = V.CODVEND AND F.NUFECH = FIELD_NUFECH)
                          
                      );
                      COMMIT;
                END IF;

                -- ATUALIZA OS REGISTROS DE PEDIDO DE COMPRA NA ABA "Fechamento Comissão"
                IF P_COUNT2 > 0 THEN 

                    OPEN VEND_COMI;
                         LOOP
                         FETCH VEND_COMI INTO P_VEND,P_TIPO,P_CODPARC,P_VALOR,P_DESLIGADO,P_REMFIXA,P_VLRTOT,P_EXTRA,P_ADTFIXO ;

                        UPDATE AD_DBFECHCOMFIN SET TIPO = P_TIPO , CODPARC = P_CODPARC , VALOR = P_VALOR , DESLIGADO = P_DESLIGADO ,REMFIXA = P_REMFIXA ,VLRTOT = P_VLRTOT, EXTRA = P_EXTRA,ADIANTFIXO = P_ADTFIXO 
                        WHERE CODVEND = P_VEND
                        AND NUFECH = FIELD_NUFECH
                        AND SEQUENCIA = P_VEND;
                        COMMIT;

                     EXIT WHEN VEND_COMI%NOTFOUND;
                     END LOOP;
                     CLOSE VEND_COMI;   

                    OPEN VEND_SCOMI;
                         LOOP
                         FETCH VEND_SCOMI INTO P_VEND2,P_TIPO2,P_CODPARC2,P_VALOR2,P_DESLIGADO2,P_REMFIXA2,P_VLRTOT2,P_EXTRA2, P_ADTFIXO2;

                          UPDATE AD_DBFECHCOMFIN SET TIPO = P_TIPO2, CODPARC = P_CODPARC2, VALOR = P_VALOR2, DESLIGADO = P_DESLIGADO2, REMFIXA = P_REMFIXA2, VLRTOT = P_VLRTOT2, EXTRA = P_EXTRA2, ADIANTFIXO = P_ADTFIXO2
                           WHERE CODVEND = P_VEND2
                           AND NUFECH = FIELD_NUFECH 
                           AND SEQUENCIA = P_VEND2;
                          COMMIT;

                    EXIT WHEN VEND_SCOMI%NOTFOUND;
                    END LOOP;
                    CLOSE VEND_SCOMI;

                END IF;

                 
                 --INATIVA O VENDEDOR DESLIGADO E ATIVA O PARCEIRO DO VENDEDOR DESLIGADO
                 --COM O VENDEDOR DESLIGADO, O MESMO NÃO SERÁ INSERIDO COMO COMISSIONADO EM NOVAS NOTAS FISCAIS
                 --COM O PARCEIRO ATIVO, SERÁ POSSIVEL GERAR O PEDIDO DE COMPRA DESSE VENDEDOR DESLIGADO
                 UPDATE TGFVEN SET ATIVO = 'N' WHERE CODVEND IN (SELECT CODVEND FROM AD_DBFECHCOMFIN WHERE DESLIGADO = 'SIM' AND NUFECH = FIELD_NUFECH);
                 UPDATE TGFPAR SET ATIVO = 'S' WHERE CODPARC IN (SELECT CODPARC FROM AD_DBFECHCOMFIN WHERE DESLIGADO = 'SIM' AND NUFECH = FIELD_NUFECH);
    
    
                --ATUALIZA OS FINANCEIROS COM O NRO ÚNICO DO FECHAMENTO DE COMISSÃO
                UPDATE TGFFIN 
                SET AD_NUFECH = FIELD_NUFECH
                WHERE NUFIN IN (SELECT DISTINCT NUFIN FROM AD_DBFECHCOMNOTAS WHERE NUFECH = FIELD_NUFECH AND DHBAIXA IS NOT NULL);
    
    
    
           END LOOP;
    
        EXECUTE IMMEDIATE 'ALTER TRIGGER TRG_UPT_TGFFIN ENABLE';
        EXECUTE IMMEDIATE 'ALTER TRIGGER TRG_AD_DBFECHCOMFIN_DLT ENABLE';
        EXECUTE IMMEDIATE 'ALTER TRIGGER TRG_AD_DBFECHCOMNOTAS_DLT ENABLE';
        EXECUTE IMMEDIATE 'ALTER TRIGGER TRG_INC_UPD_TGFPAR_SATIS ENABLE';
        EXECUTE IMMEDIATE 'ALTER TRIGGER TRG_AD_TGFINVENCI_SATIS ENABLE';


    
    P_MENSAGEM := 'Resumo Atualizado! <br>As notas baixadas no periodo selecionado e/ou relacionadas a vendedores desligados foram identificadas e serão utilziadas como base para o fechamento deste comissionamento.';
    
    END;
/