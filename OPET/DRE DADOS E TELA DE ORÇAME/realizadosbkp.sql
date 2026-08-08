SELECT 
    CODMETA,
    DTREF,
    CODEMP,
    CODPROJ,
    CODCENCUS,
    CODNAT,
    NROUNICO,
    TIPO,
    PREVDESP,
    REALDESP,
    PREVREC,
    REALREC,
    BAIXA_REC,
    BAIXA_DESP,
    COMP_REC,
    COMP_DESP,
    BAIXA_REC_ANO_ANT,
    BAIXA_DESP_ANO_ANT

FROM (

    -- ============================================================
    -- 1. BAIXAS FINANCEIRAS - RECEITA E DESPESA (TGFFIN - DHBAIXA)
    -- ============================================================

    SELECT
        5                           AS CODMETA,
        TRUNC(FIN.DTNEG, 'MM')      AS DTREF,
        FIN.CODEMP,
        FIN.CODPROJ,
        FIN.CODCENCUS,
        FIN.CODNAT,
        FIN.NUFIN                   AS NROUNICO,
        'FINANCEIRO'                AS TIPO,
        0 AS PREVDESP,
        0 AS REALDESP,
        0 AS PREVREC,
        0 AS REALREC,
        0 AS BAIXA_REC,
        0 AS BAIXA_DESP,
        CASE WHEN FIN.RECDESP =  1 THEN FIN.VLRDESDOB ELSE 0 END AS COMP_REC,
        CASE WHEN FIN.RECDESP = -1 THEN FIN.VLRDESDOB ELSE 0 END AS COMP_DESP,
        0 AS BAIXA_REC_ANO_ANT,
        0 AS BAIXA_DESP_ANO_ANT
    FROM TGFFIN FIN
    WHERE FIN.PROVISAO = 'N'
      AND FIN.CODNAT NOT LIKE '401%'
      AND RECDESP <> 0

    UNION ALL

    -- ==========================================================

    -- ============================================================
    -- 4. PORTAL - NF ENTRADA/SAÍDA (TGFCAB - CODTIPOPER 1128)
    -- ============================================================
    SELECT
        5                               AS CODMETA,
        TRUNC(CAB.DTNEG, 'MM')          AS DTREF,
        CAB.CODEMP,
        CAB.CODPROJ,
        CAB.CODCENCUS,
        CAB.CODNAT,
        CAB.NUNOTA                      AS NROUNICO,
        'PORTAL'                        AS TIPO,
        0 AS PREVDESP,
        0 AS REALDESP,
        0 AS PREVREC,
        0 AS REALREC,
        0 AS BAIXA_REC,
        0 AS BAIXA_DESP,
        CAB.VLRNOTA                     AS COMP_REC,
        0 AS COMP_DESP,
        0 AS BAIXA_REC_ANO_ANT,
        0 AS BAIXA_DESP_ANO_ANT
    FROM TGFCAB CAB
    WHERE CAB.CODEMP IN (1, 2)
      AND CAB.CODTIPOPER IN (1128)

    UNION ALL

    -- ============================================================
    -- 5. CONTABILIDADE - LANÇAMENTOS 
    -- ============================================================
    SELECT
        5                               AS CODMETA,
        LAN.REFERENCIA                  AS DTREF,
        LAN.CODEMP,
        LAN.CODPROJ,
        LAN.CODCENCUS,
        NAT.CODNAT,
        LAN.NUMDOC                      AS NROUNICO,
        'CONTABILIDADE'                 AS TIPO,
        0 AS PREVDESP,
        0 AS REALDESP,
        0 AS PREVREC,
        0 AS REALREC,
        0 AS BAIXA_REC,
        0 AS BAIXA_DESP,
        0 AS COMP_REC,
        LAN.VLRLANC                     AS COMP_DESP,
        0 AS BAIXA_REC_ANO_ANT,
        0 AS BAIXA_DESP_ANO_ANT
    FROM TCBLAN LAN
    LEFT JOIN TGFNAT NAT ON LAN.CODCTACTB = NAT.CODCTACTB
    WHERE NAT.ANALITICA  = 'S'
      AND NAT.CODNAT LIKE '401%'

    UNION ALL

    -- ============================================================
    -- 6. PORTAL - OUTRAS OPERAÇÕES (TGFCAB - CODTIPOPER 1132~1137)
    -- ============================================================
    SELECT
        5                               AS CODMETA,
        TRUNC(CAB.DTNEG, 'MM')          AS DTREF,
        CAB.CODEMP,
        CAB.CODPROJ,
        CAB.CODCENCUS,
        CAB.CODNAT,
        CAB.NUNOTA                      AS NROUNICO,
        'PORTAL'                        AS TIPO,
        0 AS PREVDESP,
        0 AS REALDESP,
        0 AS PREVREC,
        0 AS REALREC,
        0 AS BAIXA_REC,
        0 AS BAIXA_DESP,
        CAB.VLRNOTA                     AS COMP_REC,
        0 AS COMP_DESP,
        0 AS BAIXA_REC_ANO_ANT,
        0 AS BAIXA_DESP_ANO_ANT
    FROM TGFCAB CAB
    WHERE CAB.CODTIPOPER IN (1132, 1133, 1134, 1135, 1137)

)
ORDER BY TIPO, DTREF, CODNAT, NROUNICO