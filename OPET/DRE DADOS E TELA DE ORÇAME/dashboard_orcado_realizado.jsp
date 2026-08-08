<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
    <!DOCTYPE html>
    <%@ page import="java.util.*" %>
        <%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
            <%@ taglib prefix="snk" uri="/WEB-INF/tld/sankhyaUtil.tld" %>
                <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
                    <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
                        <html lang="pt-BR">

                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>OPET | Orçado × Realizado (Forecast)</title>
                            <link rel="preconnect" href="https://fonts.googleapis.com">
                            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                            <link
                                href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&display=swap"
                                rel="stylesheet">
                            <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
                            <script src="https://cdn.jsdelivr.net/npm/xlsx@0.18.5/dist/xlsx.full.min.js"></script>
                            <snk:load />
                            <style>
                                :root {
                                    --bg: #eef1f5;
                                    --surface: #ffffff;
                                    --surface-2: #f5f7fa;
                                    --border: #dbe1e8;
                                    --ink: #2b3a4a;
                                    /* azul chumbo escuro */
                                    --ink-2: #5c6b7a;
                                    /* texto secundário */
                                    --primary: #3a4a5e;
                                    /* azul chumbo */
                                    --primary-d: #2b3a4a;
                                    --ok: #16a34a;
                                    --warn: #d97706;
                                    --danger: #dc2626;
                                }

                                * {
                                    box-sizing: border-box;
                                }

                                html,
                                body {
                                    margin: 0;
                                    padding: 0;
                                    min-height: 100%;
                                    font-family: 'Outfit', 'Segoe UI', sans-serif;
                                    background: var(--bg);
                                    color: var(--ink);
                                }

                                .wrap {
                                    padding: 20px 26px 40px;
                                }

                                .topbar {
                                    display: flex;
                                    flex-wrap: wrap;
                                    align-items: center;
                                    justify-content: space-between;
                                    gap: 14px;
                                    margin-bottom: 18px;
                                }

                                .topbar h1 {
                                    font-size: 1.5rem;
                                    font-weight: 800;
                                    margin: 0;
                                    letter-spacing: -0.5px;
                                    color: var(--primary-d);
                                }

                                .topbar p {
                                    margin: 2px 0 0;
                                    font-size: 0.85rem;
                                    color: var(--ink-2);
                                }

                                .filtros {
                                    display: flex;
                                    flex-wrap: wrap;
                                    gap: 10px;
                                    align-items: flex-end;
                                }

                                .filtro-item {
                                    display: flex;
                                    flex-direction: column;
                                    gap: 3px;
                                }

                                .filtro-item label {
                                    font-size: 0.62rem;
                                    text-transform: uppercase;
                                    letter-spacing: 0.5px;
                                    color: var(--ink-2);
                                    font-weight: 700;
                                }

                                select.filtro {
                                    background: var(--surface);
                                    color: var(--ink);
                                    border: 1px solid var(--border);
                                    border-radius: 8px;
                                    padding: 7px 10px;
                                    font-size: 0.8rem;
                                    font-weight: 600;
                                    min-width: 140px;
                                }

                                select.filtro:focus {
                                    outline: 2px solid var(--primary);
                                }

                                input.filtro {
                                    background: var(--surface);
                                    color: var(--ink);
                                    border: 1px solid var(--border);
                                    border-radius: 8px;
                                    padding: 6px 10px;
                                    font-size: 0.8rem;
                                    font-weight: 600;
                                    min-width: 140px;
                                }

                                input.filtro:focus {
                                    outline: 2px solid var(--primary);
                                }

                                select[multiple].filtro {
                                    min-width: 170px;
                                    padding: 4px;
                                }

                                select[multiple].filtro option {
                                    padding: 3px 6px;
                                }

                                .filtro-item .hint {
                                    font-size: 0.55rem;
                                    color: #9aa7b4;
                                    font-weight: 600;
                                }

                                .btn {
                                    background: var(--primary);
                                    color: #fff;
                                    border: none;
                                    border-radius: 8px;
                                    padding: 8px 16px;
                                    font-size: 0.8rem;
                                    font-weight: 700;
                                    cursor: pointer;
                                }

                                .btn.ghost {
                                    background: var(--surface);
                                    border: 1px solid var(--border);
                                    color: var(--ink);
                                }

                                .btn:hover {
                                    filter: brightness(1.05);
                                }

                                .consumo-hero {
                                    background: linear-gradient(135deg, var(--primary), var(--primary-d));
                                    color: #fff;
                                    border-radius: 18px;
                                    padding: 22px 26px;
                                    margin-bottom: 18px;
                                    box-shadow: 0 8px 22px rgba(43, 58, 74, 0.20);
                                }

                                .consumo-hero .titulo {
                                    font-size: 0.72rem;
                                    text-transform: uppercase;
                                    letter-spacing: 1px;
                                    color: #cfd8e2;
                                    font-weight: 700;
                                }

                                .consumo-hero .pct {
                                    font-size: 2.6rem;
                                    font-weight: 800;
                                    line-height: 1.1;
                                    margin: 4px 0 2px;
                                    color: #fff;
                                }

                                .consumo-hero .pct.ok {
                                    color: #86efac;
                                }

                                .consumo-hero .pct.warn {
                                    color: #fcd34d;
                                }

                                .consumo-hero .pct.danger {
                                    color: #fca5a5;
                                }

                                .consumo-hero .legenda {
                                    font-size: 0.82rem;
                                    color: #e2e8f0;
                                    margin-bottom: 14px;
                                }

                                .bar-track {
                                    position: relative;
                                    height: 26px;
                                    background: rgba(0, 0, 0, 0.22);
                                    border-radius: 14px;
                                    overflow: hidden;
                                    border: 1px solid rgba(255, 255, 255, 0.15);
                                }

                                .bar-fill {
                                    height: 100%;
                                    border-radius: 14px 0 0 14px;
                                    transition: width 0.6s ease;
                                    display: flex;
                                    align-items: center;
                                    justify-content: flex-end;
                                    padding-right: 10px;
                                    font-size: 0.72rem;
                                    font-weight: 800;
                                    color: #fff;
                                    white-space: nowrap;
                                }

                                .bar-limit {
                                    position: absolute;
                                    top: -3px;
                                    bottom: -3px;
                                    width: 2px;
                                    background: repeating-linear-gradient(#fff, #fff 4px, transparent 4px, transparent 8px);
                                }

                                .kpis {
                                    display: grid;
                                    grid-template-columns: repeat(2, 1fr);
                                    gap: 12px;
                                    margin-bottom: 18px;
                                }

                                @media (min-width: 720px) {
                                    .kpis {
                                        grid-template-columns: repeat(4, 1fr);
                                    }
                                }

                                .kpi {
                                    background: var(--surface);
                                    border: 1px solid var(--border);
                                    border-radius: 14px;
                                    padding: 14px 16px;
                                    box-shadow: 0 1px 2px rgba(43, 58, 74, 0.04);
                                }

                                .kpi .k-t {
                                    font-size: 0.64rem;
                                    text-transform: uppercase;
                                    letter-spacing: 0.5px;
                                    color: var(--ink-2);
                                    font-weight: 700;
                                }

                                .kpi .k-v {
                                    font-size: 1.15rem;
                                    font-weight: 800;
                                    margin-top: 4px;
                                    color: var(--primary-d);
                                }

                                .kpi .k-s {
                                    font-size: 0.66rem;
                                    color: var(--ink-2);
                                    margin-top: 3px;
                                }

                                .kpi .k-v.ok {
                                    color: var(--ok);
                                }

                                .kpi .k-v.warn {
                                    color: var(--warn);
                                }

                                .kpi .k-v.danger {
                                    color: var(--danger);
                                }

                                .charts {
                                    display: grid;
                                    grid-template-columns: 1fr;
                                    gap: 14px;
                                    margin-bottom: 18px;
                                }

                                @media (min-width: 980px) {
                                    .charts {
                                        grid-template-columns: 2fr 1fr;
                                    }
                                }

                                .panel {
                                    background: var(--surface);
                                    border: 1px solid var(--border);
                                    border-radius: 16px;
                                    padding: 16px 18px;
                                    box-shadow: 0 1px 2px rgba(43, 58, 74, 0.04);
                                }

                                .panel h3 {
                                    font-size: 0.8rem;
                                    font-weight: 700;
                                    margin: 0 0 12px;
                                    color: var(--ink);
                                }

                                .chart-holder {
                                    position: relative;
                                    height: 280px;
                                }

                                .tabela-panel {
                                    background: var(--surface);
                                    border: 1px solid var(--border);
                                    border-radius: 16px;
                                    padding: 16px 18px;
                                    margin-bottom: 18px;
                                    box-shadow: 0 1px 2px rgba(43, 58, 74, 0.04);
                                }

                                .tabela-head {
                                    display: flex;
                                    justify-content: space-between;
                                    align-items: center;
                                    margin-bottom: 12px;
                                    flex-wrap: wrap;
                                    gap: 10px;
                                }

                                .tabela-scroll {
                                    overflow: auto;
                                    max-height: 62vh;
                                    border-radius: 10px;
                                    border: 1px solid var(--border);
                                }

                                table {
                                    border-collapse: collapse;
                                    width: 100%;
                                    font-size: 0.77rem;
                                }

                                thead {
                                    position: sticky;
                                    top: 0;
                                    z-index: 20;
                                }

                                thead th {
                                    background: var(--primary);
                                    color: #fff;
                                    text-align: right;
                                    font-weight: 700;
                                    padding: 10px 12px;
                                    border-bottom: 2px solid var(--primary-d);
                                    white-space: nowrap;
                                }

                                thead th:first-child,
                                tbody td:first-child {
                                    text-align: left;
                                }

                                tbody td {
                                    padding: 8px 12px;
                                    border-bottom: 1px solid var(--border);
                                    text-align: right;
                                    white-space: nowrap;
                                    color: var(--ink);
                                }

                                tbody tr:nth-child(even) td {
                                    background: var(--surface-2);
                                }

                                tbody tr:hover td {
                                    background: #e8edf3;
                                }

                                tr.row-total td {
                                    background: #dbe6db;
                                    color: #14532d;
                                    font-weight: 700;
                                    border-color: #bbd4bb;
                                }

                                .col-sep {
                                    border-right: 2px solid #c4ccd6 !important;
                                }

                                .badge-tipo {
                                    font-size: 0.6rem;
                                    font-weight: 800;
                                    padding: 2px 8px;
                                    border-radius: 20px;
                                }

                                .badge-R {
                                    background: rgba(22, 163, 74, 0.12);
                                    color: #15803d;
                                }

                                .badge-D {
                                    background: rgba(220, 38, 38, 0.12);
                                    color: #b91c1c;
                                }

                                .mini-bar-wrap {
                                    display: inline-flex;
                                    align-items: center;
                                    gap: 8px;
                                    min-width: 130px;
                                    justify-content: flex-end;
                                }

                                .mini-track {
                                    width: 80px;
                                    height: 8px;
                                    background: #e2e8f0;
                                    border-radius: 6px;
                                    overflow: hidden;
                                }

                                .mini-fill {
                                    height: 100%;
                                    border-radius: 6px;
                                }

                                .val-pos {
                                    color: var(--ok);
                                    font-weight: 700;
                                }

                                .val-neg {
                                    color: var(--danger);
                                    font-weight: 700;
                                }

                                .empty {
                                    text-align: center;
                                    padding: 30px;
                                    color: var(--ink-2);
                                }

                                .ok {
                                    color: var(--ok);
                                }

                                .warn {
                                    color: var(--warn);
                                }

                                .danger {
                                    color: var(--danger);
                                }
                            </style>
                        </head>

                        <%--============================================================ORÇADO vs REALIZADO
                            ORÇADO=PREVREC / PREVDESP (TGFMET: CODMETA 5=oficial, 7=forecast) REALIZADO=COMP_REC /
                            COMP_DESP (financeiro / portal / contabilidade) Agregado por mês / empresa / projeto / CR /
                            natureza / origem. Período: ano de 2026 (realizado até hoje; orçado cobre o ano p/
                            forecast).============================================================--%>
    <snk:query var="dados">
        SELECT
            TO_CHAR(Q.DTREF, 'YYYY-MM')          AS MES_ANO,
            Q.CODEMP,
            E.NOMEFANTASIA                       AS EMPRESA_NOME,
            Q.CODPROJ,
            P.IDENTIFICACAO                      AS PROJETO_NOME,
            Q.CODCENCUS,
            C.DESCRCENCUS                        AS CC_NOME,
            Q.CODNAT,
            N.DESCRNAT                           AS NATUREZA_NOME,
            N.TIPNAT,
            Q.TIPO,
            SUM(NVL(Q.PREVREC, 0))               AS ORCADO_REC,
            SUM(NVL(Q.PREVDESP, 0))              AS ORCADO_DESP,
            SUM(NVL(Q.COMP_REC, 0))              AS REALIZADO_REC,
            SUM(NVL(Q.COMP_DESP, 0))             AS REALIZADO_DESP
        FROM (
            SELECT
                5 AS CODMETA, TRUNC(FIN.DTNEG, 'MM') AS DTREF, FIN.CODEMP, FIN.CODPROJ, FIN.CODCENCUS, FIN.CODNAT,
                FIN.NUFIN AS NROUNICO, 'FINANCEIRO' AS TIPO,
                0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS BAIXA_DESP,
                CASE WHEN FIN.RECDESP = 1 THEN FIN.VLRDESDOB ELSE 0 END AS COMP_REC,
                CASE WHEN FIN.RECDESP = -1 THEN FIN.VLRDESDOB ELSE 0 END AS COMP_DESP,
                0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
            FROM TGFFIN FIN
            WHERE FIN.PROVISAO = 'N'
              AND FIN.CODNAT IN (SELECT DISTINCT N.CODNAT FROM TGFNAT N START WITH N.DESCRNAT IN ('CANCELAMENTO RECEITA SERVICOS MENSALIDADES', 'OUTRAS RECEITAS', 'DOACOES RECEBIDAS', 'LOCACAO DE SALAS', 'RECEBIMENTOS NAO IDENTIFICADOS', 'TAXAS DIVERSAS', 'DESCONTOS CONCEDIDOS', 'JUROS E ENCARGOS PAGOS', 'TAXAS DE SERVICOS BANCARIOS', 'TAXAS OPERADORAS CARTOES E PIX', 'DESCONTOS OBTIDOS', 'JUROS E ENCARGOS RECEBIDOS', 'RENDIMENTO SOBRE APLICACOES FINANCEIRAS', 'RECEBIMENTO PRECATORIOS') CONNECT BY PRIOR N.CODNAT = N.CODNATPAI)
              AND RECDESP <> 0

            UNION ALL

            SELECT
                5 AS CODMETA, TRUNC(CAB.DTNEG, 'MM') AS DTREF, CAB.CODEMP, CAB.CODPROJ, CAB.CODCENCUS, CAB.CODNAT,
                CAB.NUNOTA AS NROUNICO, 'PORTAL' AS TIPO,
                0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS BAIXA_DESP,
                CAB.VLRNOTA AS COMP_REC, 0 AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
            FROM TGFCAB CAB
            WHERE CAB.CODEMP IN (1, 2) AND CAB.CODTIPOPER IN (1128)

            UNION ALL

            SELECT
                5 AS CODMETA, TRUNC(CAB.DTNEG, 'MM') AS DTREF, CAB.CODEMP, CAB.CODPROJ, CAB.CODCENCUS, CAB.CODNAT,
                CAB.NUNOTA AS NROUNICO, 'PORTAL' AS TIPO,
                0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS BAIXA_DESP,
                CAB.VLRNOTA AS COMP_REC, 0 AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
            FROM TGFCAB CAB
            WHERE CAB.CODNAT IN (SELECT DISTINCT N.CODNAT FROM TGFNAT N START WITH N.DESCRNAT IN ('CUSTOS PESSOAL DIVERSOS DOCENTE', 'CUSTOS EDUCACIONAIS', 'DESPESAS ADMINISTRATIVAS', 'DESPESAS DE ALUGUEL', 'DESPESAS DE CONSERVACAO E MANUTENCAO', 'DESPESAS GERAIS', 'DESPESAS TECNOLOGICAS', 'GOVERNANCA CORPORATIVA', 'SERVICOS DE TERCEIROS', 'SERVICOS ASSESSORIA CORPORATIVA', 'PESSOAL DIVERSOS ADMINISTRATIVO', 'DESPESAS COM VENDAS', 'DESPESAS MARKETING E COMERCIAL', 'DESPESAS INDEDUTIVEIS', 'DEDUCOES DA RECEITA', 'DESCONTOS CONCEDIDOS', 'DEVOLUCOES/CANCELAMENTO', 'TRIBUTOS E CONTRIBUICOES S/ RECEITA', 'RECEITA DE BENS E SERVICOS', 'RECEITA BRUTA DE VENDA DE MERCADORIAS', 'RECEITA OPERACIONAL BRUTA SERVICOS', 'CUSTOS DE MERCADORIAS E SERVICOS', 'CUSTO PEDAGOGICO', 'INVESTIMENTOS') CONNECT BY PRIOR N.CODNAT = N.CODNATPAI)

            UNION ALL

            SELECT
                5 AS CODMETA, LAN.REFERENCIA AS DTREF, LAN.CODEMP, LAN.CODPROJ, LAN.CODCENCUS, NAT.CODNAT,
                LAN.NUMDOC AS NROUNICO, 'CONTABILIDADE' AS TIPO,
                0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS BAIXA_DESP,
                0 AS COMP_REC, LAN.VLRLANC AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
            FROM TCBLAN LAN
            LEFT JOIN TGFNAT NAT ON LAN.CODCTACTB = NAT.CODCTACTB
            WHERE NAT.ANALITICA = 'S'
              AND NAT.CODNAT IN (SELECT DISTINCT N.CODNAT FROM TGFNAT N START WITH N.DESCRNAT IN ('REPASSE VENDAS', 'DESPESAS COM PESSOAL ADMINISTRATIVO', 'SALARIOS E ORDENADOS ADMINISTRATIVO', 'ENCARGOS PESSOAL ADMINISTRATIVOS', 'OUTROS RESULTADOS DE INVESTIMENTOS', 'PROVISOES E PERDAS GERAIS', 'RESULTADO DE PARTIC. EM OUTRAS SOCIEDADES', 'DEPRECIACAO E AMORTIZACAO', 'RESULTADO COM DERIVATIVOS DE SWAP - AVJ', 'VARIACAO CAMBIAL PASSIVA', 'VARIACAO MONETARIA PASSIVA', 'RESULTADO COM DERIVATIVOS DE HEDGE - AVJ', 'VARIACAO CAMBIAL ATIVA', 'VARIACAO MONETARIA ATIVA', 'PROVISAO TRIBUTOS S/ LUCRO', 'DESCONTOS CONCEDIDOS', 'TRIBUTOS E CONTRIBUICOES S/ RECEITA', 'RECEITA CLINICA ESCOLA', 'RECEITA SERVICOS MENSALIDADES', 'CUSTO DOCENTE', 'CUSTO PESSOAL DOCENTE', 'CUSTOS ENCARGOS DOCENTE', 'CUSTOS FINANCIAMENTO', 'DESPESAS TRIBUTARIAS', 'PROVISAO DE TRIBUTOS S/ LUCRO') CONNECT BY PRIOR N.CODNAT = N.CODNATPAI)

            UNION ALL

            SELECT
                5 AS CODMETA, TRUNC(CAB.DTNEG, 'MM') AS DTREF, CAB.CODEMP, CAB.CODPROJ, CAB.CODCENCUS, CAB.CODNAT,
                CAB.NUNOTA AS NROUNICO, 'PORTAL' AS TIPO,
                0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS BAIXA_DESP,
                CAB.VLRNOTA AS COMP_REC, 0 AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
            FROM TGFCAB CAB
            WHERE CAB.CODTIPOPER IN (1132, 1133, 1134, 1135, 1137, 1140, 1717, 1714)
              AND CAB.CODNAT IN (SELECT DISTINCT N.CODNAT FROM TGFNAT N START WITH N.DESCRNAT IN ('CUSTO DE MERCADORIA', 'MATERIAL BRINDES E BINIFICAÇÕES', 'MATERIAL DE DEMONSTRAÇÃO') CONNECT BY PRIOR N.CODNAT = N.CODNATPAI)

            UNION ALL

            SELECT
                5 AS CODMETA, MET.DTREF AS DTREF, MET.CODEMP, MET.CODPROJ, MET.CODCENCUS, MET.CODNAT,
                0 AS NROUNICO, 'ORCAMENTO' AS TIPO,
                ABS(MET.PREVDESP) AS PREVDESP, NVL(MET.REALDESP, 0) AS REALDESP, ABS(MET.PREVREC) AS PREVREC, NVL(MET.REALREC, 0) AS REALREC,
                0 AS BAIXA_REC, 0 AS BAIXA_DESP, 0 AS COMP_REC, 0 AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
            FROM TGFMET MET
            WHERE MET.CODMETA = 5

            UNION ALL

            SELECT
                7 AS CODMETA, MET.DTREF AS DTREF, MET.CODEMP, MET.CODPROJ, MET.CODCENCUS, MET.CODNAT,
                0 AS NROUNICO, 'FORECAST' AS TIPO,
                ABS(MET.PREVDESP) AS PREVDESP, NVL(MET.REALDESP, 0) AS REALDESP, ABS(MET.PREVREC) AS PREVREC, NVL(MET.REALREC, 0) AS REALREC,
                0 AS BAIXA_REC, 0 AS BAIXA_DESP, 0 AS COMP_REC, 0 AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
            FROM TGFMET MET
            WHERE MET.CODMETA = 7

        ) Q
        LEFT JOIN TGFNAT N ON N.CODNAT = Q.CODNAT
        LEFT JOIN TCSPRJ P ON P.CODPROJ = Q.CODPROJ
        LEFT JOIN TSIEMP E ON E.CODEMP = Q.CODEMP
        LEFT JOIN TSICUS C ON C.CODCENCUS = Q.CODCENCUS
        WHERE Q.DTREF >= DATE '2026-01-01'
          AND Q.DTREF < DATE '2027-01-01'
        GROUP BY TO_CHAR(Q.DTREF, 'YYYY-MM'), Q.CODEMP, E.NOMEFANTASIA, Q.CODPROJ, P.IDENTIFICACAO, Q.CODCENCUS, C.DESCRCENCUS, Q.CODNAT, N.DESCRNAT, N.TIPNAT, Q.TIPO
        ORDER BY TO_CHAR(Q.DTREF, 'YYYY-MM'), N.DESCRNAT
    </snk:query>
                                            ), N.DESCRNAT </snk:query>

                                            <body>
                                                <div class="wrap">

                                                    <!-- Header + Filtros -->
                                                    <div class="topbar">
                                                        <div>
                                                            <h1>Orçado &times; Realizado &mdash; OPET</h1>
                                                            <p>Orçamento (TGFMET) contra o realizado comprometido, com
                                                                forecast
                                                                de fechamento &mdash; 2026.</p>
                                                        </div>
                                                        <div class="filtros">
                                                            <div class="filtro-item"><label>Data inicial</label><input
                                                                    type="date" id="f-dt-de" class="filtro"
                                                                    min="2026-01-01" max="2026-12-31"></div>
                                                            <div class="filtro-item"><label>Data final</label><input
                                                                    type="date" id="f-dt-ate" class="filtro"
                                                                    min="2026-01-01" max="2026-12-31"></div>
                                                            <div class="filtro-item"><label>&nbsp;</label><button
                                                                    id="btn-carregar" class="btn">Carregar</button>
                                                            </div>
                                                            <div class="filtro-item">
                                                                <label>Empresa</label>
                                                                <select id="f-emp" class="filtro" multiple
                                                                    size="3"></select>
                                                                <span class="hint">Ctrl+clique p/ várias</span>
                                                            </div>
                                                            <div class="filtro-item">
                                                                <label>Centro de Resultado</label>
                                                                <select id="f-cc" class="filtro" multiple
                                                                    size="3"></select>
                                                                <span class="hint">Ctrl+clique p/ várias</span>
                                                            </div>
                                                            <div class="filtro-item"><label>Projeto</label><select
                                                                    id="f-proj" class="filtro"></select></div>
                                                            <div class="filtro-item"><label>Natureza</label><select
                                                                    id="f-nat" class="filtro"></select></div>
                                                            <div class="filtro-item"><label>Origem
                                                                    (realizado)</label><select id="f-orig"
                                                                    class="filtro"></select></div>
                                                            <div class="filtro-item"><label>Cenário orçado</label>
                                                                <select id="f-cenario" class="filtro">
                                                                    <option value="ORCAMENTO" selected>Oficial</option>
                                                                    <option value="FORECAST">Forecast</option>
                                                                    <option value="AMBOS">Ambos (soma)</option>
                                                                </select>
                                                            </div>
                                                            <div class="filtro-item"><label>Visão</label>
                                                                <select id="f-tipo" class="filtro">
                                                                    <option value="D" selected>Despesas</option>
                                                                    <option value="R">Receitas</option>
                                                                    <option value="T">Receitas + Despesas</option>
                                                                </select>
                                                            </div>
                                                            <button id="btn-limpar" class="btn ghost">Limpar</button>
                                                            <button id="btn-excel" class="btn">Exportar Excel</button>
                                                        </div>
                                                    </div>

                                                    <!-- Barra de consumo -->
                                                    <div class="consumo-hero">
                                                        <div class="titulo" id="hero-titulo">Consumo do Orçamento de
                                                            Despesas
                                                        </div>
                                                        <div class="pct" id="hero-pct">0%</div>
                                                        <div class="legenda" id="hero-legenda">&nbsp;</div>
                                                        <div class="bar-track">
                                                            <div class="bar-fill" id="hero-bar"
                                                                style="width:0%; background:#86efac;"></div>
                                                            <div class="bar-limit" style="left:100%;"></div>
                                                        </div>
                                                    </div>

                                                    <!-- KPIs -->
                                                    <div class="kpis" id="kpis"></div>

                                                    <!-- Gráficos -->
                                                    <div class="charts">
                                                        <div class="panel">
                                                            <h3>Orçado &times; Realizado por mês</h3>
                                                            <div class="chart-holder"><canvas id="chartMensal"></canvas>
                                                            </div>
                                                        </div>
                                                        <div class="panel">
                                                            <h3>Distribuição do orçado por natureza</h3>
                                                            <div class="chart-holder"><canvas id="chartPizza"></canvas>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div class="charts">
                                                        <div class="panel">
                                                            <h3>Acumulado &amp; Forecast (projeção de fechamento)</h3>
                                                            <div class="chart-holder"><canvas
                                                                    id="chartForecast"></canvas>
                                                            </div>
                                                        </div>
                                                        <div class="panel">
                                                            <h3>% de consumo por natureza (Top 12)</h3>
                                                            <div class="chart-holder"><canvas
                                                                    id="chartConsumo"></canvas>
                                                            </div>
                                                        </div>
                                                    </div>

                                                    <!-- Resumo Mensal -->
                                                    <div class="tabela-panel">
                                                        <div class="tabela-head">
                                                            <h3 style="margin:0; font-size:0.85rem;">Resumo Mensal
                                                                &mdash;
                                                                Orçado vs Realizado</h3>
                                                            <span id="resumo-info"
                                                                style="font-size:0.72rem; color:var(--ink-2);"></span>
                                                        </div>
                                                        <div class="tabela-scroll">
                                                            <table id="tabela-resumo">
                                                                <thead id="resumo-head"></thead>
                                                                <tbody id="resumo-body"></tbody>
                                                            </table>
                                                        </div>
                                                    </div>

                                                    <!-- Detalhamento: Natureza / Centro de Resultado / Empresa / Projeto -->
                                                    <div class="tabela-panel">
                                                        <div class="tabela-head">
                                                            <h3 style="margin:0; font-size:0.85rem;">Detalhamento por
                                                                Natureza &rsaquo; Centro de Resultado
                                                            </h3>
                                                            <span id="tab-info"
                                                                style="font-size:0.72rem; color:var(--ink-2);"></span>
                                                        </div>
                                                        <div class="tabela-scroll">
                                                            <table id="tabela">
                                                                <thead>
                                                                    <tr>
                                                                        <th>Natureza</th>
                                                                        <th>Empresa</th>
                                                                        <th>Projeto</th>
                                                                        <th>Centro de Resultado</th>
                                                                        <th>Tipo</th>
                                                                        <th>Orçado</th>
                                                                        <th>Realizado</th>
                                                                        <th>Saldo</th>
                                                                        <th>% Consumo</th>
                                                                    </tr>
                                                                </thead>
                                                                <tbody id="tbody"></tbody>
                                                            </table>
                                                        </div>
                                                    </div>
                                                </div>

                                                <%-- Dados serializados para JSON (EL do servidor só é usado aqui) --%>
                                                    <script>
                                                        window.DADOS_FORECAST = [
                                                            <c:forEach items="${dados.rows}" var="r" varStatus="st">
                                                                {
                                                                    "MES_ANO": "${r.MES_ANO}",
                                                                "CODEMP": "${r.CODEMP}",
                                                                "EMPRESA_NOME": "${fn:replace(fn:replace(r.EMPRESA_NOME, '\\', '\\\\'), '"', '\\"')}",
                                                                "CODPROJ": "${r.CODPROJ}",
                                                                "PROJETO_NOME": "${fn:replace(fn:replace(r.PROJETO_NOME, '\\', '\\\\'), '"', '\\"')}",
                                                                "CODCENCUS": "${r.CODCENCUS}",
                                                                "CC_NOME": "${fn:replace(fn:replace(r.CC_NOME, '\\', '\\\\'), '"', '\\"')}",
                                                                "CODNAT": "${r.CODNAT}",
                                                                "NATUREZA_NOME": "${fn:replace(fn:replace(r.NATUREZA_NOME, '\\', '\\\\'), '"', '\\"')}",
                                                                "TIPNAT": "${r.TIPNAT}",
                                                                "TIPO": "${r.TIPO}",
                                                                "ORCADO_REC": "${empty r.ORCADO_REC ? 0 : r.ORCADO_REC}",
                                                                "ORCADO_DESP": "${empty r.ORCADO_DESP ? 0 : r.ORCADO_DESP}",
                                                                "REALIZADO_REC": "${empty r.REALIZADO_REC ? 0 : r.REALIZADO_REC}",
                                                                "REALIZADO_DESP": "${empty r.REALIZADO_DESP ? 0 : r.REALIZADO_DESP}"
            }<c:if test="${!st.last}">,</c:if>
                                                            </c:forEach>
                                                        ];
                                                    </script>

                                                    <%-- Lógica em JavaScript puro: usa CONCATENAÇÃO de string (nunca
                                                        template literals), para não colidir com a sintaxe ${...} do EL
                                                        do JSP. --%>
                                                        <script>
                                                            document.addEventListener('DOMContentLoaded', function () {
                                                                var MESES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
                                                                var HOJE = new Date();

                                                                // Origens que representam ORÇAMENTO (metas) — separadas do realizado
                                                                var ORIG_META = { 'ORCAMENTO': 1, 'FORECAST': 1 };

                                                                // Converte valor vindo do servidor (número ou string com vírgula/ponto) em Number.
                                                                // Trata locale pt-BR ("1.234,56"), en-US ("1,234.56"), simples ("833,15" / "833.15") e "R$".
                                                                function num(x) {
                                                                    if (typeof x === 'number') return isNaN(x) ? 0 : x;
                                                                    if (x == null) return 0;
                                                                    var s = String(x).trim().replace(/\s/g, '').replace(/R\$/gi, '');
                                                                    if (!s) return 0;
                                                                    var hasDot = s.indexOf('.') >= 0, hasComma = s.indexOf(',') >= 0;
                                                                    if (hasDot && hasComma) {
                                                                        // o separador mais à direita é o decimal
                                                                        if (s.lastIndexOf(',') > s.lastIndexOf('.')) s = s.replace(/\./g, '').replace(',', '.');
                                                                        else s = s.replace(/,/g, '');
                                                                    } else if (hasComma) {
                                                                        s = s.replace(/\./g, '').replace(',', '.');
                                                                    }
                                                                    var n = parseFloat(s);
                                                                    return isNaN(n) ? 0 : n;
                                                                }

                                                                function fmtBRL(v) {
                                                                    if (typeof v !== 'number' || isNaN(v)) return 'R$ 0,00';
                                                                    return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
                                                                }
                                                                function fmtCompact(v) {
                                                                    var a = Math.abs(v);
                                                                    if (a >= 1e6) return 'R$ ' + (v / 1e6).toFixed(1).replace('.', ',') + 'M';
                                                                    if (a >= 1e3) return 'R$ ' + (v / 1e3).toFixed(1).replace('.', ',') + 'k';
                                                                    return fmtBRL(v);
                                                                }
                                                                function fmtPct(v) {
                                                                    if (!isFinite(v) || isNaN(v)) return '-';
                                                                    return v.toFixed(1).replace('.', ',') + '%';
                                                                }
                                                                function corConsumo(p) { return p > 100 ? '#dc2626' : (p >= 85 ? '#d97706' : '#16a34a'); }
                                                                function classeConsumo(p) { return p > 100 ? 'danger' : (p >= 85 ? 'warn' : 'ok'); }
                                                                function varClass(v) { return v > 0 ? 'val-pos' : (v < 0 ? 'val-neg' : ''); }

                                                                // ─── Normalização ─────────────────────────────────────────
                                                                var RAW = (window.DADOS_FORECAST || []).map(function (r) {
                                                                    var partes = (r.MES_ANO || '').split('-');
                                                                    var ano = partes[0] || null;
                                                                    var mes = partes[1] ? parseInt(partes[1], 10) : null;
                                                                    return {
                                                                        MES_ANO: r.MES_ANO, ANO: ano, MES: mes,
                                                                        CODEMP: r.CODEMP,
                                                                        EMPRESA_NOME: (r.EMPRESA_NOME && r.EMPRESA_NOME.trim()) ? r.EMPRESA_NOME.trim() : ('Empresa ' + r.CODEMP),
                                                                        CODPROJ: r.CODPROJ,
                                                                        PROJETO_NOME: (r.PROJETO_NOME && r.PROJETO_NOME.trim()) ? r.PROJETO_NOME.trim() : ('Projeto ' + r.CODPROJ),
                                                                        CODCENCUS: r.CODCENCUS,
                                                                        CC_NOME: (r.CC_NOME && r.CC_NOME.trim()) ? r.CC_NOME.trim() : ((r.CODCENCUS !== '' && r.CODCENCUS != null) ? ('CR ' + r.CODCENCUS) : 'Sem CR'),
                                                                        CODNAT: r.CODNAT,
                                                                        NATUREZA_NOME: (r.NATUREZA_NOME && r.NATUREZA_NOME.trim()) ? r.NATUREZA_NOME.trim() : ('Natureza ' + r.CODNAT),
                                                                        TIPNAT: (r.TIPNAT || '').trim(),
                                                                        TIPO: (r.TIPO || '').trim(),
                                                                        orcRec: num(r.ORCADO_REC), orcDesp: num(r.ORCADO_DESP),
                                                                        realRec: num(r.REALIZADO_REC), realDesp: num(r.REALIZADO_DESP)
                                                                    };
                                                                });

                                                                function el(id) { return document.getElementById(id); }
                                                                var fDtDe = el('f-dt-de'), fDtAte = el('f-dt-ate'), fEmp = el('f-emp'), fCc = el('f-cc'),
                                                                    fProj = el('f-proj'), fNat = el('f-nat'), fOrig = el('f-orig'),
                                                                    fCenario = el('f-cenario'), fTipo = el('f-tipo');
                                                                var chMensal, chPizza, chConsumo, chForecast;
                                                                var carregado = false;   // só renderiza depois de preencher a data e clicar em Carregar

                                                                // Converte 'YYYY-MM-DD' (input date) em 'YYYY-MM' para comparar com MES_ANO
                                                                function toMes(dataStr) { return (dataStr && dataStr.length >= 7) ? dataStr.slice(0, 7) : ''; }

                                                                // ─── Utilitários de filtro ────────────────────────────────
                                                                function distinct(campoCod, campoNome) {
                                                                    var m = {};
                                                                    RAW.forEach(function (r) {
                                                                        if (r[campoCod] !== '' && r[campoCod] != null) m[String(r[campoCod])] = r[campoNome];
                                                                    });
                                                                    return Object.keys(m).map(function (k) { return [k, m[k]]; })
                                                                        .sort(function (a, b) { return String(a[1] || '').localeCompare(String(b[1] || '')); });
                                                                }
                                                                function opts(sel, lista, labelTodos) {
                                                                    var html = labelTodos != null ? '<option value="">' + labelTodos + '</option>' : '';
                                                                    lista.forEach(function (par) { html += '<option value="' + par[0] + '">' + (par[1] || par[0]) + '</option>'; });
                                                                    sel.innerHTML = html;
                                                                }
                                                                function selecionados(sel) {
                                                                    var out = [];
                                                                    for (var i = 0; i < sel.options.length; i++) {
                                                                        if (sel.options[i].selected && sel.options[i].value !== '') out.push(sel.options[i].value);
                                                                    }
                                                                    return out;
                                                                }

                                                                function popularFiltros() {
                                                                    // Empresas e Centros de Resultado (multi-seleção, sem "Todos")
                                                                    opts(fEmp, distinct('CODEMP', 'EMPRESA_NOME'), null);
                                                                    opts(fCc, distinct('CODCENCUS', 'CC_NOME'), null);

                                                                    opts(fProj, distinct('CODPROJ', 'PROJETO_NOME'), 'Todos');
                                                                    opts(fNat, distinct('CODNAT', 'NATUREZA_NOME'), 'Todas');

                                                                    // Origem: apenas origens de realizado (metas ficam no filtro Cenário)
                                                                    var origens = {};
                                                                    RAW.forEach(function (r) { if (r.TIPO && !ORIG_META[r.TIPO]) origens[r.TIPO] = 1; });
                                                                    var origHtml = '<option value="">Todas</option>';
                                                                    Object.keys(origens).sort().forEach(function (t) { origHtml += '<option value="' + t + '">' + t + '</option>'; });
                                                                    fOrig.innerHTML = origHtml;
                                                                }

                                                                function filtrar() {
                                                                    var de = toMes(fDtDe.value), ate = toMes(fDtAte.value);
                                                                    var emps = selecionados(fEmp), ccs = selecionados(fCc);
                                                                    var cenario = fCenario.value;
                                                                    return RAW.filter(function (r) {
                                                                        if (de && r.MES_ANO < de) return false;
                                                                        if (ate && r.MES_ANO > ate) return false;
                                                                        if (emps.length && emps.indexOf(String(r.CODEMP)) < 0) return false;
                                                                        if (ccs.length && ccs.indexOf(String(r.CODCENCUS)) < 0) return false;
                                                                        if (fProj.value && String(r.CODPROJ) !== fProj.value) return false;
                                                                        if (fNat.value && String(r.CODNAT) !== fNat.value) return false;
                                                                        // Origem só afeta linhas de realizado
                                                                        if (fOrig.value && !ORIG_META[r.TIPO] && r.TIPO !== fOrig.value) return false;
                                                                        // Cenário controla qual orçamento entra (evita dupla contagem)
                                                                        if (ORIG_META[r.TIPO] && cenario !== 'AMBOS' && r.TIPO !== cenario) return false;
                                                                        return true;
                                                                    });
                                                                }

                                                                // Receita ou despesa é definido pelo TIPO DA NATUREZA (TGFNAT.TIPNAT),
                                                                // não pela coluna que a origem preencheu. Isso alinha o orçado (TGFMET, que
                                                                // separa PREVREC/PREVDESP) com o realizado (Portal joga tudo em COMP_REC,
                                                                // Contabilidade em COMP_DESP): somamos as duas colunas e classificamos por TIPNAT.
                                                                function isReceita(r) { return r.TIPNAT === 'R'; }
                                                                function valores(r) {
                                                                    var orcTot = r.orcRec + r.orcDesp;
                                                                    var realTot = r.realRec + r.realDesp;
                                                                    if (fTipo.value === 'T') return { orc: orcTot, real: realTot };   // tudo junto
                                                                    var rec = isReceita(r);
                                                                    if (fTipo.value === 'R') return rec ? { orc: orcTot, real: realTot } : { orc: 0, real: 0 };
                                                                    return !rec ? { orc: orcTot, real: realTot } : { orc: 0, real: 0 };
                                                                }

                                                                // Ano de referência = maior ano presente no conjunto filtrado
                                                                function anoRef(linhas) {
                                                                    var max = 0;
                                                                    linhas.forEach(function (r) { var a = parseInt(r.ANO, 10); if (a > max) max = a; });
                                                                    return max || HOJE.getFullYear();
                                                                }
                                                                function mesCorte(linhas) {
                                                                    var ref = anoRef(linhas);
                                                                    if (ref < HOJE.getFullYear()) return 12;
                                                                    if (ref > HOJE.getFullYear()) return 0;
                                                                    return HOJE.getMonth() + 1;
                                                                }

                                                                // ─── Agregação por mês (1..12) ────────────────────────────
                                                                function agregaMes(linhas) {
                                                                    var porMes = {};
                                                                    for (var m = 1; m <= 12; m++) porMes[m] = { orc: 0, real: 0, temDado: false };
                                                                    linhas.forEach(function (r) {
                                                                        if (!r.MES) return;
                                                                        var v = valores(r);
                                                                        porMes[r.MES].orc += v.orc;
                                                                        porMes[r.MES].real += v.real;
                                                                        if (v.orc !== 0 || v.real !== 0) porMes[r.MES].temDado = true;
                                                                    });
                                                                    return porMes;
                                                                }

                                                                // Aviso enquanto o período não é carregado
                                                                function mostrarPrompt() {
                                                                    el('hero-titulo').textContent = 'Selecione o período';
                                                                    el('hero-pct').textContent = '—';
                                                                    el('hero-pct').className = 'pct';
                                                                    el('hero-legenda').innerHTML = 'Preencha <b>Data inicial</b> e <b>Data final</b> e clique em <b>Carregar</b> para exibir os dados.';
                                                                    el('hero-bar').style.width = '0%';
                                                                    el('hero-bar').textContent = '';
                                                                    el('kpis').innerHTML = '';
                                                                    el('resumo-body').innerHTML = '<tr><td colspan="8" class="empty">Preencha o período e clique em Carregar.</td></tr>';
                                                                    el('resumo-info').textContent = '';
                                                                    el('tbody').innerHTML = '<tr><td colspan="9" class="empty">Preencha o período e clique em Carregar.</td></tr>';
                                                                    el('tab-info').textContent = '';
                                                                    [chMensal, chPizza, chConsumo, chForecast].forEach(function (c) { if (c) c.destroy(); });
                                                                    chMensal = chPizza = chConsumo = chForecast = null;
                                                                }

                                                                // ─── Render principal ─────────────────────────────────────
                                                                function render() {
                                                                    if (!carregado) { mostrarPrompt(); return; }
                                                                    var linhas = filtrar();
                                                                    var visReceita = fTipo.value === 'R';

                                                                    var totOrc = 0, totReal = 0;
                                                                    linhas.forEach(function (r) { var v = valores(r); totOrc += v.orc; totReal += v.real; });
                                                                    var consumoPct = totOrc !== 0 ? (totReal / totOrc) * 100 : 0;
                                                                    var saldo = totOrc - totReal;

                                                                    el('hero-titulo').textContent = fTipo.value === 'T' ? 'Orçamento — Receitas + Despesas'
                                                                        : (visReceita ? 'Realização do Orçamento de Receitas' : 'Consumo do Orçamento de Despesas');
                                                                    var pctEl = el('hero-pct');
                                                                    pctEl.textContent = fmtPct(consumoPct);
                                                                    pctEl.className = 'pct ' + classeConsumo(consumoPct);
                                                                    el('hero-legenda').innerHTML =
                                                                        (visReceita ? 'Realizado' : 'Comprometido') + ' <b>' + fmtBRL(totReal) + '</b> de <b>' + fmtBRL(totOrc) + '</b> orçados' +
                                                                        ' &bull; Saldo <b style="color:' + (saldo >= 0 ? '#bbf7d0' : '#fecaca') + '">' + fmtBRL(saldo) + '</b>';
                                                                    var bar = el('hero-bar');
                                                                    bar.style.width = Math.min(consumoPct, 100) + '%';
                                                                    bar.style.background = corConsumo(consumoPct);
                                                                    bar.textContent = consumoPct > 8 ? fmtPct(consumoPct) : '';

                                                                    // Forecast
                                                                    var porMes = agregaMes(linhas);
                                                                    var corte = mesCorte(linhas);
                                                                    var realYTD = 0, orcYTD = 0, orcRestante = 0, orcTotal = 0, mesesComReal = 0;
                                                                    for (var m = 1; m <= 12; m++) {
                                                                        orcTotal += porMes[m].orc;
                                                                        if (m <= corte) {
                                                                            realYTD += porMes[m].real;
                                                                            orcYTD += porMes[m].orc;
                                                                            if (porMes[m].real !== 0) mesesComReal++;
                                                                        } else {
                                                                            orcRestante += porMes[m].orc;
                                                                        }
                                                                    }
                                                                    var forecast = realYTD + orcRestante;
                                                                    var runRate = mesesComReal > 0 ? (realYTD / mesesComReal) * 12 : 0;
                                                                    var desvioForecast = forecast - orcTotal;
                                                                    var atingYTD = orcYTD !== 0 ? (realYTD / orcYTD) * 100 : 0;

                                                                    var nNat = {}; linhas.forEach(function (r) { nNat[r.CODNAT] = 1; });

                                                                    el('kpis').innerHTML =
                                                                        kpi('Orçado (Ano)', fmtBRL(orcTotal), linhas.length + ' registros', '') +
                                                                        kpi(visReceita ? 'Realizado (YTD)' : 'Comprometido (YTD)', fmtBRL(realYTD), fmtPct(atingYTD) + ' do orçado YTD', classeConsumo(atingYTD)) +
                                                                        kpi('Saldo orçamentário', fmtBRL(saldo), saldo >= 0 ? 'Dentro do orçado' : 'Estourado', saldo >= 0 ? 'ok' : 'danger') +
                                                                        kpi('Naturezas', String(Object.keys(nNat).length), 'no filtro atual', '') +
                                                                        kpi('Forecast fechamento', fmtBRL(forecast), 'Realizado + orçado restante', '') +
                                                                        kpi('Projeção (run-rate)', fmtBRL(runRate), 'Média realizada anualizada', '') +
                                                                        kpi('Desvio vs Orçado', fmtBRL(desvioForecast),
                                                                            desvioForecast === 0 ? 'Sem desvio' : (desvioForecast > 0 ? 'Acima do orçado' : 'Abaixo do orçado'),
                                                                            visReceita ? (desvioForecast >= 0 ? 'ok' : 'danger') : (desvioForecast > 0 ? 'danger' : 'ok')) +
                                                                        kpi('Meses realizados', String(mesesComReal), 'até ' + (corte >= 1 && corte <= 12 ? MESES[corte - 1] : '-'), '');

                                                                    renderMensal(porMes);
                                                                    renderForecast(porMes, corte);
                                                                    renderPizza(linhas);
                                                                    renderConsumo(linhas);
                                                                    renderResumo(linhas, porMes, corte);
                                                                    renderTabela(linhas);
                                                                }

                                                                function kpi(t, v, s, cls) {
                                                                    return '<div class="kpi"><div class="k-t">' + t + '</div><div class="k-v ' + (cls || '') + '">' + v +
                                                                        '</div><div class="k-s">' + (s || '&nbsp;') + '</div></div>';
                                                                }

                                                                // ─── Gráfico mensal ───────────────────────────────────────
                                                                function renderMensal(porMes) {
                                                                    var labels = [], orc = [], real = [];
                                                                    for (var m = 1; m <= 12; m++) {
                                                                        if (porMes[m].temDado) { labels.push(MESES[m - 1]); orc.push(porMes[m].orc); real.push(porMes[m].real); }
                                                                    }
                                                                    if (chMensal) chMensal.destroy();
                                                                    chMensal = new Chart(el('chartMensal'), {
                                                                        type: 'bar',
                                                                        data: {
                                                                            labels: labels, datasets: [
                                                                                { label: 'Orçado', data: orc, backgroundColor: '#3a4a5e', borderRadius: 5 },
                                                                                { label: 'Realizado', data: real, backgroundColor: '#d97706', borderRadius: 5 }
                                                                            ]
                                                                        },
                                                                        options: baseOpts(true)
                                                                    });
                                                                }

                                                                // ─── Gráfico acumulado + forecast ─────────────────────────
                                                                function renderForecast(porMes, corte) {
                                                                    var labels = [], orcAcum = [], realAcum = [], foreAcum = [];
                                                                    var accOrc = 0, accReal = 0, accFore = 0;
                                                                    for (var m = 1; m <= 12; m++) {
                                                                        labels.push(MESES[m - 1]);
                                                                        accOrc += porMes[m].orc;
                                                                        orcAcum.push(accOrc);
                                                                        if (m <= corte) {
                                                                            accReal += porMes[m].real;
                                                                            realAcum.push(accReal);
                                                                            accFore = accReal;
                                                                            foreAcum.push(accFore);
                                                                        } else {
                                                                            realAcum.push(null);
                                                                            accFore += porMes[m].orc;
                                                                            foreAcum.push(accFore);
                                                                        }
                                                                    }
                                                                    if (chForecast) chForecast.destroy();
                                                                    chForecast = new Chart(el('chartForecast'), {
                                                                        type: 'line',
                                                                        data: {
                                                                            labels: labels, datasets: [
                                                                                { label: 'Orçado acum.', data: orcAcum, borderColor: '#3a4a5e', backgroundColor: 'transparent', tension: 0.25, borderWidth: 2, pointRadius: 2 },
                                                                                { label: 'Realizado acum.', data: realAcum, borderColor: '#16a34a', backgroundColor: 'rgba(22,163,74,0.10)', fill: true, tension: 0.25, borderWidth: 2, pointRadius: 2, spanGaps: false },
                                                                                { label: 'Forecast acum.', data: foreAcum, borderColor: '#d97706', backgroundColor: 'transparent', borderDash: [6, 4], tension: 0.25, borderWidth: 2, pointRadius: 0 }
                                                                            ]
                                                                        },
                                                                        options: baseOpts(true)
                                                                    });
                                                                }

                                                                // ─── Gráfico pizza ────────────────────────────────────────
                                                                function renderPizza(linhas) {
                                                                    var porNat = {};
                                                                    linhas.forEach(function (r) { var v = valores(r); porNat[r.NATUREZA_NOME] = (porNat[r.NATUREZA_NOME] || 0) + v.orc; });
                                                                    var arr = Object.keys(porNat).map(function (k) { return [k, porNat[k]]; })
                                                                        .filter(function (e) { return e[1] > 0; })
                                                                        .sort(function (a, b) { return b[1] - a[1]; });
                                                                    var top = arr.slice(0, 7);
                                                                    var resto = arr.slice(7).reduce(function (s, e) { return s + e[1]; }, 0);
                                                                    if (resto > 0) top.push(['Outras', resto]);
                                                                    var paleta = ['#3a4a5e', '#5b7089', '#8091a8', '#d97706', '#16a34a', '#0891b2', '#dc2626', '#94a3b8'];

                                                                    if (chPizza) chPizza.destroy();
                                                                    chPizza = new Chart(el('chartPizza'), {
                                                                        type: 'doughnut',
                                                                        data: {
                                                                            labels: top.map(function (e) { return e[0]; }),
                                                                            datasets: [{ data: top.map(function (e) { return e[1]; }), backgroundColor: paleta, borderColor: '#ffffff', borderWidth: 2 }]
                                                                        },
                                                                        options: {
                                                                            responsive: true, maintainAspectRatio: false,
                                                                            plugins: {
                                                                                legend: { position: 'bottom', labels: { color: '#3a4a5e', font: { size: 10 }, boxWidth: 12, padding: 8 } },
                                                                                tooltip: { callbacks: { label: function (c) { return c.label + ': ' + fmtBRL(c.parsed); } } }
                                                                            }
                                                                        }
                                                                    });
                                                                }

                                                                // ─── Gráfico % consumo por natureza ───────────────────────
                                                                function renderConsumo(linhas) {
                                                                    var porNat = {};
                                                                    linhas.forEach(function (r) {
                                                                        var v = valores(r);
                                                                        if (!porNat[r.NATUREZA_NOME]) porNat[r.NATUREZA_NOME] = { orc: 0, real: 0 };
                                                                        porNat[r.NATUREZA_NOME].orc += v.orc; porNat[r.NATUREZA_NOME].real += v.real;
                                                                    });
                                                                    var arr = Object.keys(porNat).map(function (k) {
                                                                        return { nome: k, pct: porNat[k].orc > 0 ? (porNat[k].real / porNat[k].orc) * 100 : 0, orc: porNat[k].orc };
                                                                    }).filter(function (e) { return e.orc > 0; })
                                                                        .sort(function (a, b) { return b.pct - a.pct; })
                                                                        .slice(0, 12);

                                                                    if (chConsumo) chConsumo.destroy();
                                                                    chConsumo = new Chart(el('chartConsumo'), {
                                                                        type: 'bar',
                                                                        data: {
                                                                            labels: arr.map(function (a) { return a.nome; }),
                                                                            datasets: [{
                                                                                label: '% consumo', data: arr.map(function (a) { return +a.pct.toFixed(1); }),
                                                                                backgroundColor: arr.map(function (a) { return corConsumo(a.pct); }), borderRadius: 5
                                                                            }]
                                                                        },
                                                                        options: {
                                                                            indexAxis: 'y', responsive: true, maintainAspectRatio: false,
                                                                            plugins: {
                                                                                legend: { display: false },
                                                                                tooltip: { callbacks: { label: function (c) { return 'Consumo: ' + fmtPct(c.parsed.x); } } }
                                                                            },
                                                                            scales: {
                                                                                x: { ticks: { color: '#5c6b7a', callback: function (v) { return v + '%'; } }, grid: { color: '#e5e9ef' } },
                                                                                y: { ticks: { color: '#3a4a5e', font: { size: 10 } }, grid: { display: false } }
                                                                            }
                                                                        }
                                                                    });
                                                                }

                                                                function baseOpts(money) {
                                                                    return {
                                                                        responsive: true, maintainAspectRatio: false,
                                                                        interaction: { mode: 'index', intersect: false },
                                                                        plugins: {
                                                                            legend: { labels: { color: '#3a4a5e', font: { size: 11 }, boxWidth: 14 } },
                                                                            tooltip: {
                                                                                callbacks: {
                                                                                    label: function (c) {
                                                                                        var val = (c.parsed && c.parsed.y != null) ? c.parsed.y : c.parsed;
                                                                                        return c.dataset.label + ': ' + fmtBRL(val);
                                                                                    }
                                                                                }
                                                                            }
                                                                        },
                                                                        scales: {
                                                                            x: { ticks: { color: '#5c6b7a' }, grid: { display: false } },
                                                                            y: { ticks: { color: '#5c6b7a', callback: function (v) { return money ? fmtCompact(v) : v; } }, grid: { color: '#e5e9ef' } }
                                                                        }
                                                                    };
                                                                }

                                                                // ─── Tabela Resumo Mensal ─────────────────────────────────
                                                                function renderResumo(linhas, porMes, corte) {
                                                                    el('resumo-head').innerHTML =
                                                                        '<tr>' +
                                                                        '<th style="text-align:left;">Mês</th>' +
                                                                        '<th>Orçado</th>' +
                                                                        '<th>Realizado</th>' +
                                                                        '<th class="col-sep">% Consumo</th>' +
                                                                        '<th>Saldo</th>' +
                                                                        '<th>Orçado Acum.</th>' +
                                                                        '<th>Realizado Acum.</th>' +
                                                                        '<th class="col-sep">Forecast Acum.</th>' +
                                                                        '</tr>';

                                                                    var anoLbl = anoRef(linhas);
                                                                    var html = '', accOrc = 0, accReal = 0, accFore = 0, totOrc = 0, totReal = 0, algum = false;
                                                                    for (var m = 1; m <= 12; m++) {
                                                                        var d = porMes[m];
                                                                        if (!d.temDado && d.orc === 0) continue;   // mês sem orçado e sem realizado: pula
                                                                        algum = true;
                                                                        accOrc += d.orc; totOrc += d.orc; totReal += (m <= corte ? d.real : 0);
                                                                        var pct = d.orc !== 0 ? (d.real / d.orc) * 100 : 0;
                                                                        var saldo = d.orc - d.real;
                                                                        var realCell, foreCell;
                                                                        if (m <= corte) {
                                                                            accReal += d.real; accFore = accReal;
                                                                            realCell = fmtBRL(d.real);
                                                                            foreCell = fmtBRL(accFore);
                                                                        } else {
                                                                            accFore += d.orc;
                                                                            realCell = '<span style="color:#9aa7b4;">—</span>';
                                                                            foreCell = '<span class="warn">' + fmtBRL(accFore) + '</span>';
                                                                        }
                                                                        html +=
                                                                            '<tr>' +
                                                                            '<td>' + MESES[m - 1] + '/' + String(anoLbl).slice(2) + '</td>' +
                                                                            '<td>' + fmtBRL(d.orc) + '</td>' +
                                                                            '<td>' + realCell + '</td>' +
                                                                            '<td class="col-sep ' + (m <= corte ? classeConsumo(pct) : '') + '">' + (m <= corte ? fmtPct(pct) : '—') + '</td>' +
                                                                            '<td class="' + varClass(saldo) + '">' + (m <= corte ? fmtBRL(saldo) : '—') + '</td>' +
                                                                            '<td>' + fmtBRL(accOrc) + '</td>' +
                                                                            '<td>' + (m <= corte ? fmtBRL(accReal) : '<span style="color:#9aa7b4;">—</span>') + '</td>' +
                                                                            '<td class="col-sep">' + foreCell + '</td>' +
                                                                            '</tr>';
                                                                    }

                                                                    if (!algum) {
                                                                        el('resumo-body').innerHTML = '<tr><td colspan="8" class="empty">Nenhum dado para os filtros selecionados.</td></tr>';
                                                                        el('resumo-info').textContent = '';
                                                                        return;
                                                                    }

                                                                    var pctT = totOrc !== 0 ? (totReal / totOrc) * 100 : 0;
                                                                    html +=
                                                                        '<tr class="row-total">' +
                                                                        '<td>TOTAL</td>' +
                                                                        '<td>' + fmtBRL(totOrc) + '</td>' +
                                                                        '<td>' + fmtBRL(totReal) + '</td>' +
                                                                        '<td class="col-sep">' + fmtPct(pctT) + '</td>' +
                                                                        '<td class="' + varClass(totOrc - totReal) + '">' + fmtBRL(totOrc - totReal) + '</td>' +
                                                                        '<td>' + fmtBRL(accOrc) + '</td>' +
                                                                        '<td>' + fmtBRL(accReal) + '</td>' +
                                                                        '<td class="col-sep">' + fmtBRL(accFore) + '</td>' +
                                                                        '</tr>';

                                                                    el('resumo-body').innerHTML = html;
                                                                    el('resumo-info').textContent = 'Realizado até ' + (corte >= 1 && corte <= 12 ? MESES[corte - 1] : '-') + ' • demais meses projetados pelo orçado';
                                                                }

                                                                // ─── Tabela por natureza ──────────────────────────────────
                                                                function renderTabela(linhas) {
                                                                    // Consolida por Natureza + Empresa + Projeto + Centro de Resultado.
                                                                    // Os filtros da tela (Empresa, CR, Projeto, Natureza...) fatiam este nível.
                                                                    var grupos = {};
                                                                    linhas.forEach(function (r) {
                                                                        var v = valores(r);
                                                                        var k = r.CODNAT + '|' + r.CODEMP + '|' + r.CODPROJ + '|' + r.CODCENCUS;
                                                                        if (!grupos[k]) grupos[k] = {
                                                                            nat: r.NATUREZA_NOME, tipo: r.TIPNAT,
                                                                            emp: r.EMPRESA_NOME, proj: r.PROJETO_NOME, cr: r.CC_NOME,
                                                                            orc: 0, real: 0
                                                                        };
                                                                        grupos[k].orc += v.orc; grupos[k].real += v.real;
                                                                    });
                                                                    var arr = Object.keys(grupos).map(function (k) { return grupos[k]; })
                                                                        .filter(function (n) { return n.orc !== 0 || n.real !== 0; })
                                                                        .sort(function (a, b) {
                                                                            if (a.nat !== b.nat) return String(a.nat).localeCompare(String(b.nat));
                                                                            return b.orc - a.orc;
                                                                        });

                                                                    var tbody = el('tbody');
                                                                    if (!arr.length) {
                                                                        tbody.innerHTML = '<tr><td colspan="9" class="empty">Nenhum dado para os filtros selecionados.</td></tr>';
                                                                        el('tab-info').textContent = '';
                                                                        return;
                                                                    }

                                                                    var html = '', totOrc = 0, totReal = 0;
                                                                    arr.forEach(function (n) {
                                                                        var saldo = n.orc - n.real;
                                                                        var pct = n.orc !== 0 ? (n.real / n.orc) * 100 : 0;
                                                                        var cor = corConsumo(pct);
                                                                        totOrc += n.orc; totReal += n.real;
                                                                        var badge = n.tipo === 'R'
                                                                            ? '<span class="badge-tipo badge-R">RECEITA</span>'
                                                                            : '<span class="badge-tipo badge-D">DESPESA</span>';
                                                                        html +=
                                                                            '<tr>' +
                                                                            '<td>' + n.nat + '</td>' +
                                                                            '<td style="text-align:left;">' + n.emp + '</td>' +
                                                                            '<td style="text-align:left;">' + n.proj + '</td>' +
                                                                            '<td style="text-align:left;">' + n.cr + '</td>' +
                                                                            '<td style="text-align:left;">' + badge + '</td>' +
                                                                            '<td>' + fmtBRL(n.orc) + '</td>' +
                                                                            '<td>' + fmtBRL(n.real) + '</td>' +
                                                                            '<td class="' + (saldo >= 0 ? 'val-pos' : 'val-neg') + '">' + fmtBRL(saldo) + '</td>' +
                                                                            '<td><div class="mini-bar-wrap">' +
                                                                            '<span class="' + classeConsumo(pct) + '" style="font-weight:700;">' + fmtPct(pct) + '</span>' +
                                                                            '<div class="mini-track"><div class="mini-fill" style="width:' + Math.min(pct, 100) + '%; background:' + cor + ';"></div></div>' +
                                                                            '</div></td>' +
                                                                            '</tr>';
                                                                    });

                                                                    var saldoT = totOrc - totReal;
                                                                    var pctT = totOrc !== 0 ? (totReal / totOrc) * 100 : 0;
                                                                    html +=
                                                                        '<tr class="row-total">' +
                                                                        '<td>TOTAL</td>' +
                                                                        '<td style="text-align:left;">&nbsp;</td>' +
                                                                        '<td style="text-align:left;">&nbsp;</td>' +
                                                                        '<td style="text-align:left;">&nbsp;</td>' +
                                                                        '<td style="text-align:left;">&nbsp;</td>' +
                                                                        '<td>' + fmtBRL(totOrc) + '</td>' +
                                                                        '<td>' + fmtBRL(totReal) + '</td>' +
                                                                        '<td class="' + (saldoT >= 0 ? 'val-pos' : 'val-neg') + '">' + fmtBRL(saldoT) + '</td>' +
                                                                        '<td>' + fmtPct(pctT) + '</td>' +
                                                                        '</tr>';

                                                                    tbody.innerHTML = html;
                                                                    el('tab-info').textContent = arr.length + ' linhas (natureza × CR × empresa × projeto)';
                                                                }

                                                                // ─── Exportar Excel ───────────────────────────────────────
                                                                el('btn-excel').addEventListener('click', function () {
                                                                    try {
                                                                        var wb = XLSX.utils.book_new();
                                                                        var linhas = filtrar();

                                                                        // Aba 1: Resumo Mensal
                                                                        var porMes = agregaMes(linhas);
                                                                        var corte = mesCorte(linhas);
                                                                        var accOrc = 0, accReal = 0, accFore = 0;
                                                                        var resumoData = [];
                                                                        for (var m = 1; m <= 12; m++) {
                                                                            var d = porMes[m];
                                                                            if (!d.temDado && d.orc === 0) continue;
                                                                            accOrc += d.orc;
                                                                            if (m <= corte) { accReal += d.real; accFore = accReal; } else { accFore += d.orc; }
                                                                            resumoData.push({
                                                                                'Mês': MESES[m - 1] + '/' + String(anoRef(linhas)),
                                                                                'Orçado': d.orc,
                                                                                'Realizado': m <= corte ? d.real : null,
                                                                                'Consumo %': (m <= corte && d.orc !== 0) ? +((d.real / d.orc) * 100).toFixed(1) : null,
                                                                                'Saldo': m <= corte ? (d.orc - d.real) : null,
                                                                                'Orçado Acum.': accOrc,
                                                                                'Realizado Acum.': m <= corte ? accReal : null,
                                                                                'Forecast Acum.': accFore
                                                                            });
                                                                        }
                                                                        XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(resumoData), 'Resumo Mensal');

                                                                        // Aba 2: Por Natureza
                                                                        var porNat = {};
                                                                        linhas.forEach(function (r) {
                                                                            var v = valores(r), k = r.CODNAT;
                                                                            if (!porNat[k]) porNat[k] = { Natureza: r.NATUREZA_NOME, Tipo: r.TIPNAT, Orcado: 0, Realizado: 0 };
                                                                            porNat[k].Orcado += v.orc; porNat[k].Realizado += v.real;
                                                                        });
                                                                        var natData = Object.keys(porNat).map(function (k) {
                                                                            var n = porNat[k];
                                                                            return {
                                                                                Natureza: n.Natureza, Tipo: n.Tipo === 'R' ? 'Receita' : 'Despesa',
                                                                                Orcado: n.Orcado, Realizado: n.Realizado, Saldo: n.Orcado - n.Realizado,
                                                                                'Consumo %': n.Orcado ? +((n.Realizado / n.Orcado) * 100).toFixed(1) : 0
                                                                            };
                                                                        });
                                                                        XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(natData), 'Por Natureza');

                                                                        // Aba 3: Detalhado
                                                                        var detData = linhas.map(function (r) {
                                                                            var v = valores(r);
                                                                            return {
                                                                                'Mês': r.MES_ANO, 'Origem': r.TIPO, 'Natureza': r.NATUREZA_NOME, 'Cód. Nat.': r.CODNAT,
                                                                                'Empresa': r.EMPRESA_NOME, 'Projeto': r.PROJETO_NOME, 'Centro Resultado': r.CC_NOME,
                                                                                'Orçado': v.orc, 'Realizado': v.real, 'Saldo': v.orc - v.real
                                                                            };
                                                                        });
                                                                        XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(detData), 'Detalhado');

                                                                        XLSX.writeFile(wb, 'OPET_Orcado_x_Realizado.xlsx');
                                                                    } catch (e) { console.error(e); }
                                                                });

                                                                // ─── Eventos ──────────────────────────────────────────────
                                                                // Botão Carregar: valida o período e habilita a renderização
                                                                el('btn-carregar').addEventListener('click', function () {
                                                                    if (!fDtDe.value || !fDtAte.value) {
                                                                        alert('Preencha a Data inicial e a Data final para carregar os dados.');
                                                                        return;
                                                                    }
                                                                    if (fDtDe.value > fDtAte.value) {
                                                                        alert('A Data inicial não pode ser maior que a Data final.');
                                                                        return;
                                                                    }
                                                                    carregado = true;
                                                                    render();
                                                                });

                                                                // Demais filtros só reprocessam depois que os dados foram carregados
                                                                [fEmp, fCc, fProj, fNat, fOrig, fCenario, fTipo].forEach(function (f) {
                                                                    f.addEventListener('change', function () { if (carregado) render(); });
                                                                });
                                                                // Alterar as datas exige clicar em Carregar novamente
                                                                [fDtDe, fDtAte].forEach(function (f) {
                                                                    f.addEventListener('change', function () { carregado = false; mostrarPrompt(); });
                                                                });

                                                                el('btn-limpar').addEventListener('click', function () {
                                                                    fDtDe.value = ''; fDtAte.value = '';
                                                                    for (var e = 0; e < fEmp.options.length; e++) fEmp.options[e].selected = false;
                                                                    for (var c = 0; c < fCc.options.length; c++) fCc.options[c].selected = false;
                                                                    fProj.value = ''; fNat.value = ''; fOrig.value = ''; fCenario.value = 'ORCAMENTO'; fTipo.value = 'D';
                                                                    carregado = false;
                                                                    mostrarPrompt();
                                                                });

                                                                // ─── Init ─────────────────────────────────────────────────
                                                                if (!RAW.length) {
                                                                    el('kpis').innerHTML = '';
                                                                    el('tbody').innerHTML = '<tr><td colspan="9" class="empty">Nenhum registro retornado pela consulta.</td></tr>';
                                                                    el('resumo-body').innerHTML = '<tr><td colspan="8" class="empty">Nenhum registro.</td></tr>';
                                                                    return;
                                                                }
                                                                popularFiltros();
                                                                mostrarPrompt();   // aguarda o preenchimento do período + Carregar
                                                            });
                                                        </script>
                                            </body>

                        </html>