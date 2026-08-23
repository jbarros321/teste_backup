SELECT 
    S.NUNOTA,
    S.NUNOTAVENDA,
    S.NUNOTA_VENDA,
    S.NUNOTA_COMPRA,
    S.CODEMP,
    EMP.NOMEFANTASIA,
    S.PRODUTO AS PRODUTO_VENDA,
    S.CODPROD AS PRODUTO_COMPRA,
    S.TOTAL_ICMS_COMPRA,
    S.TOTAL_NOTA_COMPRA,
    S.TOTAL_ICMS_VENDA,
    S.TOTAL_NOTA_VENDA,
    S.ICMS_UNIT,
    PRO.DESCRPROD,
    NC.AD_TIPO,
    
    CASE 
        WHEN S.CODEMP IN (1,4) 
         AND NC.AD_TIPO = 'A' 
         AND TRUNC(S.DATA_VENDA, 'MM') >= TO_DATE('01/12/2025', 'DD/MM/YYYY')
         AND S.BASEC_VEND = S.BASEC_COMP 
        THEN 0 
        
        WHEN S.CODEMP IN (1,4) 
         AND NC.AD_TIPO = 'A' 
         AND TRUNC(S.DATA_VENDA, 'MM') >= TO_DATE('01/12/2025', 'DD/MM/YYYY')
         AND S.BASEC_VEND <> S.BASEC_COMP 
         AND S.BASEC_VEND < S.BASEC_COMP 
        THEN 0
        
        WHEN S.CODEMP IN (1,4) 
         AND NC.AD_TIPO = 'A' 
         AND TRUNC(S.DATA_VENDA, 'MM') >= TO_DATE('01/12/2025', 'DD/MM/YYYY')
         AND S.BASEC_VEND <> S.BASEC_COMP 
         AND S.BASEC_VEND > S.BASEC_COMP 
        THEN SUM((S.BASEC_VEND - S.BASEC_COMP) / 100 * (ROUND((S.TOTAL_NOTA_COMPRA / S.QTD_COMPRADA), 2) * S.QTD_CONSUMIDA))
        
        ELSE SUM(S.VALOR_ESTORNO_FINANCEIRO) 
    END AS ESTORNO,
    
    S.DESCRICAO_ESTORNO,
    S.QTD_CONSUMIDA,
    PRO.NCM,
    NC.CODNCM,
    NCC.CODNCM AS NCMVENDA,
    S.BASEC_COMP,
    S.BASEC_VEND,
    S.BASE_CALC,
    S.DATA_COMPRA,
    S.DATA_VENDA,
    
    OP.IDPROC AS ID_PROCESSO_PRODUCAO,
    OP.CODPRODPA AS PRODUTO_ORDEM_PRODUCAO,
    OP.CONTROLEPA AS LOTE_ORDEM_PRODUCAO,
    OP.TAMLOTEPAD AS TAMANHO_LOTE_PADRAO,
    OP.MULTIDEAL AS MULTIPLICADOR_DEAL,
    OP.QTDPRODMIN AS QTDE_PRODUTO_MINIMA,
    OP.DHALTER AS DATA_HORA_ALTERACAO_OP,
    OP.DHCAD AS DATA_HORA_CADASTRO_OP,
    OP.DESCRICAO_PRODUTO_OP,
    
    MP.NUNOTA AS NUNOTA_COMPRA_MP,
    MP.NUMNOTA AS NUMNOTA_COMPRA_MP,
    MP.DATA_COMPRA AS DATA_COMPRA_MP,
    MP.CODPROD AS CODPROD_MP,
    PRO_MP.DESCRPROD AS DESCRICAO_MP,
    MP.CONTROLE AS LOTE_MP,
    MP.QTD_CONSUMIDA AS QTD_MP_CONSUMIDA,
    MP.VLRUNIT AS VLRUNIT_MP,
    MP.TOTAL_ICMS_COMPRA AS TOTAL_ICMS_COMPRA_MP,
    MP.TOTAL_NOTA_COMPRA AS TOTAL_NOTA_COMPRA_MP

FROM (
    SELECT 
        NUNOTA,
        NUNOTAVENDA, 
        CODPROD AS PRODUTO,
        CODPROD,
        LOTE,
        CONTROLE,
        CODEMP,
        NUNOTA_VENDA,
        DATA_VENDA,
        NUNOTA_COMPRA,
        DATA_COMPRA,
        QTD_VENDIDA,
        QTD_COMPRADA,
        QTD_CONSUMIDA,
        TOTAL_ICMS_COMPRA,
        TOTAL_NOTA_COMPRA,
        TOTAL_NOTA_VENDA,
        VLRUNIT,
        TOTAL_ICMS_VENDA,
        ICMS_UNIT,
        ICMS_VENDA_UNIT,
        DESCONT,
        DESC_UNIT,
        BASE_CALC,
        BASERED_CALC,
        BASEC_COMP,
        BASEC_VEND,
        CASE 
            WHEN CODEMP = 3 AND CODTRIB = 51 THEN 0 
            ELSE VALOR_ESTORNO_FINANCEIRO 
        END AS VALOR_ESTORNO_FINANCEIRO,
        ICMS_RETOR,
        SALDO_FINAL,
        CASE 
            WHEN LOTE = 'MPP' THEN 'Fabricação Propria' 
            ELSE 'Revenda' 
        END AS MODELO,
        DESCRICAO_ESTORNO
    FROM (
        WITH VENDAS AS (
            SELECT 
                CAB.NUNOTA,
                ITE.CODTRIB,
                ITE.CODPROD,
                ITE.CONTROLE AS LOTE,
                CAB.CODEMP,
                CAB.NUMNOTA AS NUNOTA_VENDA,
                CAB.DTNEG AS DATA_VENDA,
                SUM(ITE.QTDNEG) AS QTD_VENDIDA,
                SUM(ITE.VLRICMS) AS TOTAL_ICMS_VENDA,
                SUM(ITE.VLRTOT) - SUM(ITE.VLRDESC) AS TOTAL_NOTA_VENDA,
                ITE.VLRUNIT,
                SUM(ITE.VLRDESC) / NULLIF(SUM(ITE.QTDNEG), 0) AS DESCONT,
                SUM(ITE.VLRICMS) / NULLIF(SUM(ITE.QTDNEG), 0) AS ICMS_VENDA_UNIT,
                SUM(ITE.VLRTOT) - SUM(ITE.VLRDESC) AS TT_LIQ_VENDA,
                CASE 
                    WHEN CAB.CODEMP IN (1, 4, 6) THEN NVL(DIN.PERCREDBASE, 0)
                    ELSE 0 
                END AS BASE_CALC
            FROM TGFITE ITE
            INNER JOIN TGFCAB CAB ON CAB.NUNOTA = ITE.NUNOTA
            LEFT JOIN TGFDIN DIN ON DIN.NUNOTA = ITE.NUNOTA 
                                 AND DIN.SEQUENCIA = ITE.SEQUENCIA 
                                 AND DIN.CODINC = 1
            WHERE CAB.TIPMOV = 'V'
              AND CAB.STATUSNOTA = 'L'
              AND ITE.CODCFO IN (5101, 5102, 5116, 5117, 5152, 5910, 5911, 5927, 5949, 
                                 6101, 6102, 6108, 6116, 6117, 6910, 6911, 6949)
              AND TRUNC(CAB.DTNEG, 'MM') = :P_PERIODO
            GROUP BY
                CAB.NUNOTA,
                ITE.CODTRIB,
                ITE.CODPROD,
                ITE.CONTROLE,
                CAB.CODEMP,
                CAB.NUMNOTA,
                CAB.DTNEG,
                ITE.VLRUNIT,
                DIN.PERCREDBASE
        ),
        
        COMPRAS AS (
            SELECT
                ITEC.NUNOTA,
                ITEC.CODPROD,
                CASE 
                    WHEN CABC.CODEMP = 1 
                     AND EXISTS (SELECT 1 FROM TPRLMP WHERE CODPRODPA = ITEC.CODPROD)
                    THEN 'MPP'
                    ELSE ITEC.CONTROLE
                END AS CONTROLE,
                CABC.CODEMP,
                CABC.NUMNOTA AS NUNOTA_COMPRA,
                CABC.DTNEG AS DATA_COMPRA,
                SUM(CASE 
                    WHEN ITEC.CODVOL = 'TN' THEN ITEC.QTDNEG * 1000 
                    ELSE ITEC.QTDNEG 
                END) AS QTD_COMPRADA,
                SUM(ITEC.VLRICMS) AS TOTAL_ICMS_COMPRA,
                SUM(ITEC.VLRTOT) - SUM(ITEC.VLRDESC) AS TOTAL_NOTA_COMPRA,
                ITEC.VLRUNIT,
                SUM(ITEC.VLRICMS) / NULLIF(SUM(ITEC.QTDNEG), 0) AS ICMS_UNIT,
                SUM(ITEC.VLRDESC) / NULLIF(SUM(ITEC.VLRTOT), 0) AS DESC_UNIT,
                CASE 
                    WHEN ITEC.CODTRIB <> 0 
                    THEN SUM(DIN2.BASERED) / NULLIF(SUM(CASE 
                        WHEN ITEC.CODVOL = 'TN' THEN ITEC.QTDNEG * 1000 
                        ELSE ITEC.QTDNEG 
                    END), 0)
                    ELSE 0 
                END AS BASERED
            FROM TGFITE ITEC
            INNER JOIN TGFCAB CABC ON CABC.NUNOTA = ITEC.NUNOTA
            LEFT JOIN TGFDIN DIN2 ON DIN2.NUNOTA = ITEC.NUNOTA 
                                   AND DIN2.SEQUENCIA = ITEC.SEQUENCIA 
                                   AND DIN2.CODINC = 1
            WHERE CABC.TIPMOV = 'C'
              AND CABC.STATUSNOTA = 'L'
              AND TRUNC(CABC.DTENTSAI, 'MM') > :P_PERIDCOMPD
            GROUP BY
                ITEC.NUNOTA,
                ITEC.CODPROD,
                ITEC.CONTROLE,
                CABC.CODEMP,
                CABC.NUMNOTA,
                CABC.DTNEG,
                ITEC.VLRUNIT,
                ITEC.CODTRIB
        ),
        
        VINCULO_FIFO AS (
            SELECT
                V.*,
                C.NUNOTA AS NUNOTAVENDA,
                C.CONTROLE,
                C.NUNOTA_COMPRA,
                C.DATA_COMPRA,
                C.QTD_COMPRADA,
                C.TOTAL_ICMS_COMPRA,
                C.TOTAL_NOTA_COMPRA,
                C.ICMS_UNIT,
                C.DESC_UNIT,
                C.BASERED,
                SUM(V.QTD_VENDIDA) OVER (
                    PARTITION BY C.CODPROD, C.CONTROLE, C.NUNOTA_COMPRA
                    ORDER BY V.DATA_VENDA, V.NUNOTA_VENDA
                ) AS CONSUMO_ACUMULADO
            FROM VENDAS V
            INNER JOIN COMPRAS C ON C.CODPROD = V.CODPROD
                                 AND C.CODEMP = V.CODEMP
                                 AND C.CONTROLE = V.LOTE
                                 AND C.DATA_COMPRA <= V.DATA_VENDA
        ),
        
        CONSUMO_VALIDO AS (
            SELECT
                NUNOTA,
                NUNOTAVENDA,
                CODTRIB,
                CODPROD,
                LOTE,
                CONTROLE,
                CODEMP,
                NUNOTA_VENDA,
                DATA_VENDA,
                QTD_VENDIDA,
                NUNOTA_COMPRA,
                DATA_COMPRA,
                QTD_COMPRADA,
                TOTAL_ICMS_VENDA,
                TOTAL_NOTA_VENDA,
                TT_LIQ_VENDA,
                VLRUNIT,
                DESCONT,
                ICMS_VENDA_UNIT,
                BASE_CALC,
                TOTAL_ICMS_COMPRA,
                TOTAL_NOTA_COMPRA,
                ICMS_UNIT,
                DESC_UNIT,
                BASERED,
                CONSUMO_ACUMULADO,
                QTD_COMPRADA - (CONSUMO_ACUMULADO - QTD_VENDIDA) AS SALDO_DISPONIVEL
            FROM VINCULO_FIFO
        ),
        
        CONSUMO_CALCULADO AS (
            SELECT
                NUNOTA,
                NUNOTAVENDA, 
                CODTRIB,
                CODPROD,
                LOTE,
                CONTROLE,
                CODEMP,
                NUNOTA_VENDA,
                DATA_VENDA,
                NUNOTA_COMPRA,
                DATA_COMPRA,
                QTD_VENDIDA,
                QTD_COMPRADA,
                TOTAL_ICMS_COMPRA,
                TOTAL_NOTA_COMPRA,
                TOTAL_NOTA_VENDA,
                TOTAL_ICMS_VENDA,
                TT_LIQ_VENDA,
                VLRUNIT,
                ICMS_UNIT,
                ICMS_VENDA_UNIT,
                DESCONT,
                DESC_UNIT,
                BASE_CALC,
                BASERED,
                SALDO_DISPONIVEL,
                LEAST(QTD_VENDIDA, GREATEST(SALDO_DISPONIVEL, 0)) AS QTD_CONSUMIDA
            FROM CONSUMO_VALIDO
        )
        
        SELECT
            NUNOTA,
            NUNOTAVENDA, 
            CODTRIB,
            CODPROD,
            LOTE,
            CONTROLE,
            CODEMP,
            NUNOTA_VENDA,
            DATA_VENDA,
            NUNOTA_COMPRA,
            DATA_COMPRA,
            QTD_VENDIDA,
            QTD_COMPRADA,
            QTD_CONSUMIDA,
            TOTAL_ICMS_COMPRA,
            TOTAL_NOTA_COMPRA,
            TOTAL_NOTA_VENDA,
            VLRUNIT,
            TT_LIQ_VENDA,
            TOTAL_ICMS_VENDA,
            ICMS_UNIT,
            ICMS_VENDA_UNIT,
            DESCONT,
            DESC_UNIT,
            BASE_CALC,
            BASERED * QTD_CONSUMIDA AS BASERED_CALC,
            ROUND(TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0) * 100, 2) AS BASEC_COMP,
            ROUND(TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0) * 100, 2) AS BASEC_VEND,
            CASE 
                WHEN CODEMP IN (1, 4, 6) AND BASE_CALC > 0
                THEN ((BASE_CALC / 100)) * ((ROUND(ICMS_UNIT, 2) * QTD_CONSUMIDA))
                
                WHEN CODEMP IN (1, 4) 
                 AND ROUND((TOTAL_NOTA_VENDA * (ROUND(TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0), 2)) - TOTAL_ICMS_VENDA), 0) = 0 
                 AND (CODTRIB = 40 OR CODTRIB = 41)
                THEN ((ROUND(ICMS_UNIT, 2) * QTD_CONSUMIDA))
                
                WHEN CODEMP NOT IN (1, 4, 6) 
                 AND ROUND((TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0)) * 100, 2) > 
                     ROUND((TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0)) * 100, 2)
                THEN (VLRUNIT * QTD_VENDIDA) * ROUND((TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0)) - 
                                                      (TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0)), 5)
                
                WHEN CODEMP NOT IN (1, 4, 6) 
                 AND ROUND(TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0) * 100, 2) = 0
                THEN (VLRUNIT * QTD_VENDIDA) * ROUND((TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0)) * 100, 2)
                
                ELSE 0
            END AS VALOR_ESTORNO_FINANCEIRO,
            ICMS_UNIT * QTD_CONSUMIDA AS ICMS_RETOR,
            QTD_COMPRADA - SUM(QTD_CONSUMIDA) OVER (
                PARTITION BY CODPROD, CONTROLE, NUNOTA_COMPRA
                ORDER BY DATA_VENDA, NUNOTA_VENDA
            ) AS SALDO_FINAL,
            CASE 
                WHEN CODEMP IN (1, 4, 6) AND BASE_CALC > 0
                THEN 'EMPRESA ' || CODEMP || ' - Estorno por BASE_CALC' ||
                     ' - Formula: (BASE_CALC / 100) * (ICMS_UNIT * QTD_CONSUMIDA)' ||
                     ' - Valores: BASE_CALC=' || TO_CHAR(BASE_CALC, 'FM9999990D99') ||
                     ', ICMS_UNIT=' || TO_CHAR(ROUND(ICMS_UNIT, 2), 'FM9999990D99') ||
                     ', QTD_CONSUMIDA=' || TO_CHAR(QTD_CONSUMIDA, 'FM9999990D999999')
                
                WHEN CODEMP IN (1, 4)
                 AND ROUND((TOTAL_NOTA_VENDA * ROUND(TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0), 2) - TOTAL_ICMS_VENDA), 0) = 0
                THEN 'EMPRESA ' || CODEMP || ' - Ajuste ICMS Venda (resultado zero)' ||
                     ' - Formula: (TOTAL_NOTA_VENDA * ALIQ_VENDA) - TOTAL_ICMS_VENDA' ||
                     ' - Valores: TOTAL_NOTA_VENDA=' || TO_CHAR(TOTAL_NOTA_VENDA, 'FM9999990D99') ||
                     ', ALIQ_VENDA=' || TO_CHAR(ROUND(TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0), 2), 'FM9999990D99') ||
                     ', TOTAL_ICMS_VENDA=' || TO_CHAR(TOTAL_ICMS_VENDA, 'FM9999990D99')
                
                WHEN CODEMP NOT IN (1, 4, 6)
                 AND ROUND((TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0)) * 100, 2) >
                     ROUND((TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0)) * 100, 2)
                THEN 'EMPRESA ' || CODEMP || ' - Diferenca de carga ICMS (Compra > Venda)' ||
                     ' - Formula: (VLRUNIT * QTD_VENDIDA) * (ALIQ_COMPRA - ALIQ_VENDA)' ||
                     ' - Valores: VLRUNIT=' || TO_CHAR(VLRUNIT, 'FM9999990D999999') ||
                     ', QTD_VENDIDA=' || TO_CHAR(QTD_VENDIDA, 'FM9999990D999999') ||
                     ', ALIQ_COMPRA=' || TO_CHAR(ROUND(TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0), 5), 'FM9999990D999999') ||
                     ', ALIQ_VENDA=' || TO_CHAR(ROUND(TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0), 5), 'FM9999990D999999')
                
                WHEN CODEMP NOT IN (1, 4, 6)
                 AND ROUND(TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0) * 100, 2) = 0
                THEN 'EMPRESA ' || CODEMP || ' - Venda sem ICMS' ||
                     ' - Formula: (VLRUNIT * QTD_VENDIDA) * ALIQ_COMPRA' ||
                     ' - Valores: VLRUNIT=' || TO_CHAR(VLRUNIT, 'FM9999990D999999') ||
                     ', QTD_VENDIDA=' || TO_CHAR(QTD_VENDIDA, 'FM9999990D999999') ||
                     ', ALIQ_COMPRA=' || TO_CHAR(ROUND(TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0), 5), 'FM9999990D999999')
                
                ELSE 'SEM ESTORNO - Nenhuma regra aplicavel'
            END AS DESCRICAO_ESTORNO
        FROM CONSUMO_CALCULADO
        WHERE QTD_CONSUMIDA > 0
    )
    
    UNION ALL
    
    SELECT
        NUNOTA,
        NUNOTAVENDA, 
        PRODUTO,
        CODPROD,
        LOTE,
        CONTROLE,
        CODEMP,
        NUNOTA_VENDA,
        DATA_VENDA,
        NUNOTA_COMPRA,
        DATA_COMPRA,
        QTD_VENDIDA,
        QTD_COMPRADA,
        QTD_CONSUMIDA,
        TOTAL_ICMS_COMPRA,
        TOTAL_NOTA_COMPRA,
        TOTAL_NOTA_VENDA,
        VLRUNIT,
        TOTAL_ICMS_VENDA,
        ICMS_UNIT,
        ICMS_VENDA_UNIT,
        DESCONT,
        DESC_UNIT,
        BASE_CALC,
        0 AS BASERED_CALC,
        BASEC_COMP,
        BASEC_VEND,
        VALOR_ESTORNO_FINANCEIRO,
        ICMS_RETOR,
        SALDO_FINAL,
        CASE 
            WHEN LOTE = 'MPP' THEN 'Fabricação Propria' 
            ELSE 'Revenda' 
        END AS MODELO,
        DESCRICAO_ESTORNO
    FROM (
        WITH VENDAS AS (
            SELECT 
                ITE.CODTRIB,
                CAB.NUNOTA,
                ITE.CODPROD AS PRODUTO,
                FAB.CODPRODMP AS CODPROD,
                'MPP' AS LOTE,
                CAB.CODEMP,
                CAB.NUMNOTA AS NUNOTA_VENDA,
                CAB.DTNEG AS DATA_VENDA,
                SUM((FAB.VOLUMULT) * ITE.QTDNEG) AS QTD_VENDIDA,
                ITE.VLRICMS AS TOTAL_ICMS_VENDA,
                ITE.VLRTOT - ITE.VLRDESC AS TOTAL_NOTA_VENDA,
                ITE.VLRUNIT,
                CASE 
                    WHEN SUM(ITE.QTDNEG) <> 0 THEN SUM(ITE.VLRDESC) / SUM(ITE.QTDNEG)
                    ELSE 0 
                END AS DESCONT,
                CASE 
                    WHEN SUM(ITE.QTDNEG) <> 0 THEN SUM(ITE.VLRICMS) / SUM(ITE.QTDNEG)
                    ELSE 0 
                END AS ICMS_VENDA_UNIT,
                CASE 
                    WHEN CAB.CODEMP IN (1) THEN NVL(DIN.PERCREDBASE, 0)
                    ELSE 0
                END AS BASE_CALC
            FROM TGFITE ITE
            INNER JOIN TGFCAB CAB ON CAB.NUNOTA = ITE.NUNOTA
            LEFT JOIN TGFDIN DIN ON DIN.NUNOTA = ITE.NUNOTA 
                                 AND DIN.SEQUENCIA = ITE.SEQUENCIA 
                                 AND DIN.CODINC = 1
            LEFT JOIN (
                SELECT 
                    SS.CODPRODPA,
                    SS.CODPRODMP,
                    SS.DESCRPROD,
                    SS.QTDPORLOTE,
                    CASE 
                        WHEN SS.CODVOL = 'KG' THEN SS.QTDPORLOTE * NVL(QTDPORLOTE2, 1)
                        ELSE SS.QTDPORLOTE 
                    END AS VOLUMULT,
                    SS.CODVOL
                FROM (
                    SELECT 
                        LMP.CODPRODPA,
                        LMPI.CODPRODMP,
                        PRO.DESCRPROD,
                        CASE 
                            WHEN LMPI.TIPOQTD = 'V' THEN LMPI.QTDMISTURA
                            ELSE LMPI.QTDMISTURA
                        END AS QTDPORLOTE,
                        LMPI.CODVOL
                    FROM TPRLMP LMP
                    INNER JOIN TPRLMP LMPI ON LMPI.CODPRODPA = LMP.CODPRODMP
                    INNER JOIN TGFPRO PRO ON PRO.CODPROD = LMPI.CODPRODMP
                    INNER JOIN TPREFX EFX ON EFX.IDEFX = LMP.IDEFX
                    INNER JOIN TPREFX EFXI ON EFXI.IDEFX = LMPI.IDEFX
                    WHERE EFX.IDPROC = (SELECT MAX(IDPROC) FROM TPRLPA WHERE CODPRODPA = LMP.CODPRODPA)
                      AND EFXI.IDPROC = (SELECT MAX(IDPROC) FROM TPRLPA WHERE CODPRODPA = LMPI.CODPRODPA)
                      AND NVL(LMP.CONTROLEPA, ' ') = ' '
                      AND LMP.TIPOUSOMP = 'N'
                    UNION ALL
                    SELECT 
                        LMP.CODPRODPA,
                        LMP.CODPRODMP,
                        PRO.DESCRPROD,
                        CASE 
                            WHEN LMP.TIPOQTD = 'V' THEN LMP.QTDMISTURA
                            ELSE LMP.QTDMISTURA
                        END,
                        LMP.CODVOL
                    FROM TPRLMP LMP
                    INNER JOIN TGFPRO PRO ON PRO.CODPROD = LMP.CODPRODMP
                    INNER JOIN TPREFX EFX ON EFX.IDEFX = LMP.IDEFX
                    WHERE EFX.IDPROC = (SELECT MAX(IDPROC) FROM TPRLPA WHERE CODPRODPA = LMP.CODPRODPA)
                      AND NVL(LMP.CONTROLEPA, ' ') = ' '
                      AND LMP.TIPOUSOMP = 'N'
                ) SS
                LEFT JOIN (
                    SELECT 
                        CODPRODPA AS CODPRODPA2,
                        CODPRODMP AS CODPRODMP2,
                        CASE 
                            WHEN TIPOQTD = 'V' THEN QTDMISTURA
                            ELSE QTDMISTURA
                        END AS QTDPORLOTE2
                    FROM TPRLMP
                ) PI ON PI.CODPRODPA2 = SS.CODPRODPA
            ) FAB ON FAB.CODPRODPA = ITE.CODPROD
            WHERE CAB.TIPMOV = 'V'
              AND CAB.STATUSNOTA = 'L'
              AND TRUNC(CAB.DTNEG, 'MM') = :P_PERIODO
              AND ITE.CODCFO IN (5101, 5102, 5116, 5117, 5152, 5910, 5911, 5927, 5949, 
                                 6101, 6102, 6108, 6116, 6117, 6910, 6911, 6949)
            GROUP BY 
                CAB.NUNOTA,
                ITE.CODPROD,
                FAB.CODPRODMP,
                CAB.CODEMP,
                CAB.NUMNOTA,
                CAB.DTNEG,
                ITE.VLRUNIT,
                DIN.PERCREDBASE,
                ITE.VLRICMS,
                ITE.VLRTOT,
                ITE.VLRDESC,
                ITE.CODTRIB
        ),
        
        COMPRAS AS (
            SELECT 
                CAB.NUNOTA,
                ITE.CONTROLE,
                ITE.CODPROD,
                CAB.CODEMP,
                CAB.NUMNOTA AS NUNOTA_COMPRA,
                CAB.DTNEG AS DATA_COMPRA,
                CASE 
                    WHEN ITE.CODVOL = 'TN' THEN ITE.QTDNEG * 1000 
                    ELSE ITE.QTDNEG 
                END AS QTD_COMPRADA,
                ITE.VLRICMS AS TOTAL_ICMS_COMPRA,
                ITE.VLRTOT - ITE.VLRDESC AS TOTAL_NOTA_COMPRA,
                ITE.VLRUNIT,
                ITE.VLRDESC AS DESC_UNIT,
                ITE.VLRICMS / NULLIF(ITE.QTDNEG, 0) AS ICMS_UNIT,
                CASE 
                    WHEN ITE.CODTRIB <> 0 
                    THEN DIN.BASERED / NULLIF(CASE 
                        WHEN ITE.CODVOL = 'TN' THEN ITE.QTDNEG * 1000 
                        ELSE ITE.QTDNEG 
                    END, 0)
                    ELSE 0 
                END AS BASERED
            FROM TGFITE ITE
            INNER JOIN TGFCAB CAB ON CAB.NUNOTA = ITE.NUNOTA
            LEFT JOIN TGFDIN DIN ON DIN.NUNOTA = ITE.NUNOTA 
                                 AND DIN.SEQUENCIA = ITE.SEQUENCIA 
                                 AND DIN.CODINC = 1
            WHERE CAB.TIPMOV = 'C'
              AND CAB.STATUSNOTA = 'L'
              AND TRUNC(CAB.DTENTSAI, 'MM') > :P_PERIDCOMPD
        ),
        
        VINCULACAO_BASE AS (
            SELECT
                V.*, 
                C.NUNOTA AS NUNOTAVENDA, 
                C.CONTROLE,
                C.NUNOTA_COMPRA,
                C.DATA_COMPRA,
                C.QTD_COMPRADA,
                C.TOTAL_ICMS_COMPRA,
                C.TOTAL_NOTA_COMPRA,
                C.VLRUNIT AS VLRUNIT_COMPRA,
                C.ICMS_UNIT,
                C.DESC_UNIT,
                C.BASERED,
                (C.QTD_COMPRADA - SUM(V.QTD_VENDIDA) OVER (
                    PARTITION BY V.CODPROD, C.CONTROLE, C.NUNOTA_COMPRA
                    ORDER BY V.DATA_VENDA, V.NUNOTA_VENDA
                    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                )) AS SALDO_DISPONIVEL
            FROM VENDAS V
            INNER JOIN COMPRAS C ON C.CODPROD = V.CODPROD
                                 AND C.CODEMP = V.CODEMP
                                 AND C.DATA_COMPRA <= V.DATA_VENDA
        ),
        
        CONSUMO_ACUMULADO AS (
            SELECT 
                CODTRIB,
                NUNOTA,
                NUNOTAVENDA,
                PRODUTO,
                CODPROD,
                CODEMP,
                NUNOTA_VENDA,
                DATA_VENDA,
                QTD_VENDIDA,
                NUNOTA_COMPRA,
                DATA_COMPRA,
                CONTROLE,
                SALDO_DISPONIVEL,
                CASE
                    WHEN SALDO_DISPONIVEL <= 0 THEN 0
                    WHEN QTD_VENDIDA <= SALDO_DISPONIVEL THEN QTD_VENDIDA
                    ELSE SALDO_DISPONIVEL
                END AS QTD_CONSUMIDA_NESTA_COMPRA,
                SUM(CASE
                    WHEN SALDO_DISPONIVEL <= 0 THEN 0
                    WHEN QTD_VENDIDA <= SALDO_DISPONIVEL THEN QTD_VENDIDA
                    ELSE SALDO_DISPONIVEL
                END) OVER (
                    PARTITION BY CODPROD, NUNOTA_VENDA 
                    ORDER BY DATA_COMPRA, NUNOTA_COMPRA
                    ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING
                ) AS QTD_JA_CONSUMIDA_ANTES
            FROM VINCULACAO_BASE
            WHERE SALDO_DISPONIVEL > 0
        ),
        
        VINCULACAO AS (
            SELECT
                VB.*,
                CA.QTD_JA_CONSUMIDA_ANTES
            FROM VINCULACAO_BASE VB
            INNER JOIN CONSUMO_ACUMULADO CA ON CA.CODPROD = VB.CODPROD
                                            AND CA.NUNOTA_VENDA = VB.NUNOTA_VENDA
                                            AND CA.NUNOTA_COMPRA = VB.NUNOTA_COMPRA
                                            AND CA.CONTROLE = VB.CONTROLE
        )
        
        SELECT
            NUNOTA,
            NUNOTAVENDA,
            PRODUTO,
            CODPROD,
            LOTE,
            CONTROLE,
            CODEMP,
            NUNOTA_VENDA,
            DATA_VENDA,
            NUNOTA_COMPRA,
            DATA_COMPRA,
            QTD_VENDIDA,
            QTD_COMPRADA,
            CASE
                WHEN NVL(QTD_JA_CONSUMIDA_ANTES, 0) >= QTD_VENDIDA THEN 0
                WHEN SALDO_DISPONIVEL <= 0 THEN 0
                WHEN (QTD_VENDIDA - NVL(QTD_JA_CONSUMIDA_ANTES, 0)) <= SALDO_DISPONIVEL 
                THEN (QTD_VENDIDA - NVL(QTD_JA_CONSUMIDA_ANTES, 0))
                ELSE SALDO_DISPONIVEL
            END AS QTD_CONSUMIDA,
            TOTAL_ICMS_COMPRA,
            TOTAL_NOTA_COMPRA,
            VLRUNIT_COMPRA,
            ICMS_UNIT,
            DESC_UNIT,
            BASERED,
            TOTAL_NOTA_VENDA,
            TOTAL_ICMS_VENDA,
            VLRUNIT,
            DESCONT,
            ICMS_VENDA_UNIT,
            BASE_CALC,
            ROUND(TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0) * 100, 2) AS BASEC_COMP,
            ROUND(TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0) * 100, 2) AS BASEC_VEND,
            CASE 
                WHEN CODEMP IN (1, 4) AND BASE_CALC > 0 
                THEN ROUND(ICMS_UNIT, 2) * (
                    CASE
                        WHEN NVL(QTD_JA_CONSUMIDA_ANTES, 0) >= QTD_VENDIDA THEN 0
                        WHEN SALDO_DISPONIVEL <= 0 THEN 0
                        WHEN (QTD_VENDIDA - NVL(QTD_JA_CONSUMIDA_ANTES, 0)) <= SALDO_DISPONIVEL 
                        THEN (QTD_VENDIDA - NVL(QTD_JA_CONSUMIDA_ANTES, 0))
                        ELSE SALDO_DISPONIVEL
                    END
                ) * ((BASE_CALC / 100))
                
                WHEN CODEMP IN (1, 4) 
                 AND ROUND((TOTAL_NOTA_VENDA * (ROUND(TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0), 2)) - TOTAL_ICMS_VENDA), 0) = 0 
                 AND (CODTRIB = 40 OR CODTRIB = 41)
                THEN ROUND(ICMS_UNIT, 2) * (
                    CASE
                        WHEN NVL(QTD_JA_CONSUMIDA_ANTES, 0) >= QTD_VENDIDA THEN 0
                        WHEN SALDO_DISPONIVEL <= 0 THEN 0
                        WHEN (QTD_VENDIDA - NVL(QTD_JA_CONSUMIDA_ANTES, 0)) <= SALDO_DISPONIVEL 
                        THEN (QTD_VENDIDA - NVL(QTD_JA_CONSUMIDA_ANTES, 0))
                        ELSE SALDO_DISPONIVEL
                    END
                )
                
                WHEN CODEMP NOT IN (1, 4, 6) 
                 AND ROUND((TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0)) * 100, 2) > 
                     ROUND((TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0)) * 100, 2)
                THEN (VLRUNIT * QTD_VENDIDA) * ROUND((TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0)) - 
                                                      (TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0)), 5)
                
                WHEN CODEMP NOT IN (1, 4, 6) 
                 AND ROUND(TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0) * 100, 2) = 0
                THEN (VLRUNIT * QTD_VENDIDA) * ROUND((TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0)) * 100, 2)
                
                ELSE 0
            END AS VALOR_ESTORNO_FINANCEIRO,
            ICMS_UNIT * CASE
                WHEN NVL(QTD_JA_CONSUMIDA_ANTES, 0) >= QTD_VENDIDA THEN 0
                WHEN SALDO_DISPONIVEL <= 0 THEN 0
                WHEN (QTD_VENDIDA - NVL(QTD_JA_CONSUMIDA_ANTES, 0)) <= SALDO_DISPONIVEL 
                THEN (QTD_VENDIDA - NVL(QTD_JA_CONSUMIDA_ANTES, 0))
                ELSE SALDO_DISPONIVEL
            END AS ICMS_RETOR,
            SALDO_DISPONIVEL AS SALDO_FINAL,
            CASE 
                WHEN CODEMP IN (1, 4, 6) AND BASE_CALC > 0
                THEN 'EMPRESA ' || CODEMP || ' - Estorno por BASE_CALC' ||
                     ' - Formula: (BASE_CALC / 100) * (ICMS_UNIT * QTD_CONSUMIDA)' ||
                     ' - Valores: BASE_CALC=' || TO_CHAR(BASE_CALC, 'FM9999990D99') ||
                     ', ICMS_UNIT=' || TO_CHAR(ROUND(ICMS_UNIT, 2), 'FM9999990D99') ||
                     ', QTD_CONSUMIDA=' || TO_CHAR(QTD_CONSUMIDA, 'FM9999990D999999')
                
                WHEN CODEMP NOT IN (1, 4, 6)
                 AND ROUND((TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0)) * 100, 2) >
                     ROUND((TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0)) * 100, 2)
                THEN 'EMPRESA ' || CODEMP || ' - Diferenca de carga ICMS (Compra > Venda)' ||
                     ' - Formula: (VLRUNIT * QTD_VENDIDA) * (ALIQ_COMPRA - ALIQ_VENDA)' ||
                     ' - Valores: VLRUNIT=' || TO_CHAR(VLRUNIT, 'FM9999990D999999') ||
                     ', QTD_VENDIDA=' || TO_CHAR(QTD_VENDIDA, 'FM9999990D999999') ||
                     ', ALIQ_COMPRA=' || TO_CHAR(ROUND(TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0), 5), 'FM9999990D999999') ||
                     ', ALIQ_VENDA=' || TO_CHAR(ROUND(TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0), 5), 'FM9999990D999999')
                
                WHEN CODEMP NOT IN (1, 4, 6)
                 AND ROUND(TOTAL_ICMS_VENDA / NULLIF(TOTAL_NOTA_VENDA, 0) * 100, 2) = 0
                THEN 'EMPRESA ' || CODEMP || ' - Venda sem ICMS' ||
                     ' - Formula: (VLRUNIT * QTD_VENDIDA) * ALIQ_COMPRA' ||
                     ' - Valores: VLRUNIT=' || TO_CHAR(VLRUNIT, 'FM9999990D999999') ||
                     ', QTD_VENDIDA=' || TO_CHAR(QTD_VENDIDA, 'FM9999990D999999') ||
                     ', ALIQ_COMPRA=' || TO_CHAR(ROUND(TOTAL_ICMS_COMPRA / NULLIF(TOTAL_NOTA_COMPRA, 0), 5), 'FM9999990D999999')
                
                ELSE 'SEM ESTORNO - Nenhuma regra aplicavel'
            END AS DESCRICAO_ESTORNO
        FROM VINCULACAO
        WHERE SALDO_DISPONIVEL > 0
          AND NVL(QTD_JA_CONSUMIDA_ANTES, 0) < QTD_VENDIDA
    )
    WHERE QTD_CONSUMIDA = QTD_VENDIDA
) S

INNER JOIN TSIEMP EMP ON S.CODEMP = EMP.CODEMP AND EMP.ATIVO = 'S'
INNER JOIN TGFPRO PRO ON S.CODPROD = PRO.CODPROD AND PRO.ATIVO = 'S'
INNER JOIN TGFNCM NC ON PRO.NCM = NC.CODNCM
INNER JOIN TGFPRO PROC ON S.PRODUTO = PROC.CODPROD AND PROC.ATIVO = 'S'
INNER JOIN TGFNCM NCC ON PROC.NCM = NCC.CODNCM

LEFT JOIN (
    SELECT DISTINCT
        OP.IDPROC,
        OP.CODPRODPA,
        OP.CONTROLEPA,
        OP.TAMLOTEPAD,
        OP.MULTIDEAL,
        OP.QTDPRODMIN,
        OP.DHALTER,
        OP.DHCAD,
        PRO_OP.DESCRPROD AS DESCRICAO_PRODUTO_OP
    FROM TPRLPA OP
    INNER JOIN TGFPRO PRO_OP ON PRO_OP.CODPROD = OP.CODPRODPA AND PRO_OP.ATIVO = 'S'
    WHERE OP.CONTROLEPA IS NOT NULL
      AND NVL(OP.CONTROLEPA, ' ') <> ' '
) OP ON OP.CONTROLEPA = S.CONTROLE
    AND OP.CODPRODPA = S.PRODUTO

LEFT JOIN (
    SELECT
        ITE.NUNOTA,
        CAB.NUMNOTA,
        CAB.DTNEG AS DATA_COMPRA,
        ITE.CODPROD,
        ITE.CONTROLE,
        ITE.QTDNEG AS QTD_CONSUMIDA,
        ITE.VLRUNIT,
        ITE.VLRICMS AS TOTAL_ICMS_COMPRA,
        ITE.VLRTOT - ITE.VLRDESC AS TOTAL_NOTA_COMPRA,
        CAB.CODEMP
    FROM TGFITE ITE
    INNER JOIN TGFCAB CAB ON CAB.NUNOTA = ITE.NUNOTA
    WHERE CAB.TIPMOV = 'C'
      AND CAB.STATUSNOTA = 'L'
      AND ITE.CONTROLE IS NOT NULL
      AND NVL(ITE.CONTROLE, ' ') <> ' '
) MP ON MP.CONTROLE = NVL(OP.CONTROLEPA, S.CONTROLE)
    AND MP.CODPROD = S.CODPROD
    AND MP.CODEMP = S.CODEMP
    AND MP.DATA_COMPRA <= S.DATA_VENDA

LEFT JOIN TGFPRO PRO_MP ON PRO_MP.CODPROD = MP.CODPROD AND PRO_MP.ATIVO = 'S'

WHERE S.CODEMP = :A_CODEMP

GROUP BY 
    S.NUNOTA,
    S.NUNOTAVENDA,
    S.CODEMP,
    EMP.NOMEFANTASIA,
    S.PRODUTO,
    S.CODPROD,
    S.NUNOTA_VENDA,
    S.NUNOTA_COMPRA,
    S.TOTAL_ICMS_COMPRA,
    S.TOTAL_NOTA_COMPRA,
    S.TOTAL_ICMS_VENDA,
    S.TOTAL_NOTA_VENDA,
    S.ICMS_UNIT,
    PRO.DESCRPROD,
    S.DESCRICAO_ESTORNO,
    S.QTD_CONSUMIDA,
    PRO.NCM,
    NC.CODNCM,
    NCC.CODNCM,
    S.BASEC_COMP,
    S.BASEC_VEND,
    S.BASE_CALC,
    S.DATA_COMPRA,
    NC.AD_TIPO,
    S.DATA_VENDA,
    OP.IDPROC,
    OP.CODPRODPA,
    OP.CONTROLEPA,
    OP.TAMLOTEPAD,
    OP.MULTIDEAL,
    OP.QTDPRODMIN,
    OP.DHALTER,
    OP.DHCAD,
    OP.DESCRICAO_PRODUTO_OP,
    MP.NUNOTA,
    MP.NUMNOTA,
    MP.DATA_COMPRA,
    MP.CODPROD,
    PRO_MP.DESCRPROD,
    MP.CONTROLE,
    MP.QTD_CONSUMIDA,
    MP.VLRUNIT,
    MP.TOTAL_ICMS_COMPRA,
    MP.TOTAL_NOTA_COMPRA

ORDER BY 
    S.DATA_VENDA,
    S.NUNOTA_VENDA,
    S.CODPROD,
    OP.IDPROC
