<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
    <!DOCTYPE html>
    <%@ page import="java.util.*" %>
        <%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
            <%@ taglib prefix="snk" uri="/WEB-INF/tld/sankhyaUtil.tld" %>
                <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
                    <html lang="pt-BR">

                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>OPET | Acompanhamento de Orçado vs Realizado</title>
                        <link rel="preconnect" href="https://fonts.googleapis.com">
                        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                        <link
                            href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&display=swap"
                            rel="stylesheet">
                        <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
                        <script src="https://cdn.jsdelivr.net/npm/xlsx@0.18.5/dist/xlsx.full.min.js"></script>
                        <snk:load />
                        <style>
                            * {
                                box-sizing: border-box;
                            }

                            html,
                            body {
                                margin: 0;
                                padding: 0;
                                min-height: 100%;
                                font-family: 'Outfit', 'Segoe UI', sans-serif;
                                background: #0f172a;
                                color: #e2e8f0;
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
                                color: #fff;
                            }

                            .topbar p {
                                margin: 2px 0 0;
                                font-size: 0.85rem;
                                color: #94a3b8;
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
                                color: #64748b;
                                font-weight: 700;
                            }

                            select.filtro {
                                background: #1e293b;
                                color: #e2e8f0;
                                border: 1px solid #334155;
                                border-radius: 8px;
                                padding: 7px 10px;
                                font-size: 0.8rem;
                                font-weight: 600;
                                min-width: 150px;
                            }

                            select.filtro:focus {
                                outline: 2px solid #6366f1;
                            }

                            .btn {
                                background: linear-gradient(135deg, #6366f1, #8b5cf6);
                                color: #fff;
                                border: none;
                                border-radius: 8px;
                                padding: 8px 16px;
                                font-size: 0.8rem;
                                font-weight: 700;
                                cursor: pointer;
                            }

                            .btn.ghost {
                                background: #1e293b;
                                border: 1px solid #334155;
                            }

                            .btn:hover {
                                filter: brightness(1.1);
                            }

                            .kpis {
                                display: grid;
                                grid-template-columns: repeat(2, 1fr);
                                gap: 12px;
                                margin-bottom: 18px;
                            }

                            @media (min-width: 720px) {
                                .kpis {
                                    grid-template-columns: repeat(3, 1fr);
                                }
                            }

                            @media (min-width: 1100px) {
                                .kpis {
                                    grid-template-columns: repeat(6, 1fr);
                                }
                            }

                            .kpi {
                                background: #1e293b;
                                border: 1px solid #334155;
                                border-radius: 14px;
                                padding: 14px 16px;
                            }

                            .kpi .k-t {
                                font-size: 0.64rem;
                                text-transform: uppercase;
                                letter-spacing: 0.5px;
                                color: #94a3b8;
                                font-weight: 700;
                            }

                            .kpi .k-v {
                                font-size: 1.1rem;
                                font-weight: 800;
                                margin-top: 4px;
                                color: #fff;
                            }

                            .kpi .k-s {
                                font-size: 0.66rem;
                                color: #64748b;
                                margin-top: 3px;
                            }

                            .kpi .k-v.ok {
                                color: #34d399;
                            }

                            .kpi .k-v.warn {
                                color: #fbbf24;
                            }

                            .kpi .k-v.danger {
                                color: #f87171;
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
                                background: #1e293b;
                                border: 1px solid #334155;
                                border-radius: 16px;
                                padding: 16px 18px;
                            }

                            .panel h3 {
                                font-size: 0.8rem;
                                font-weight: 700;
                                margin: 0 0 12px;
                                color: #e2e8f0;
                            }

                            .chart-holder {
                                position: relative;
                                height: 280px;
                            }

                            .tabela-panel {
                                background: #1e293b;
                                border: 1px solid #334155;
                                border-radius: 16px;
                                padding: 16px 18px;
                                margin-bottom: 18px;
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
                                max-height: 60vh;
                                border-radius: 10px;
                            }

                            /* Tabelas - cabeçalhos agrupados */
                            .th-group {
                                background: #312e81;
                                color: #c7d2fe;
                                text-align: center;
                                font-weight: 700;
                                font-size: 0.7rem;
                                padding: 8px 10px;
                                border-right: 2px solid #1e293b;
                                white-space: nowrap;
                            }

                            .th-group-total {
                                background: #14532d;
                                color: #bbf7d0;
                            }

                            .th-sub {
                                background: #1e293b;
                                color: #94a3b8;
                                text-align: right;
                                font-weight: 600;
                                font-size: 0.68rem;
                                padding: 6px 8px;
                                border-right: 1px solid #334155;
                                white-space: nowrap;
                            }

                            .th-sub-total {
                                background: #064e3b;
                                color: #6ee7b7;
                            }

                            .th-desc {
                                background: #312e81;
                                color: #c7d2fe;
                                font-weight: 700;
                                font-size: 0.72rem;
                                padding: 8px 12px;
                                position: sticky;
                                left: 0;
                                z-index: 25;
                                min-width: 260px;
                            }

                            .th-desc-sub {
                                background: #1e293b;
                                color: transparent;
                                padding: 6px 12px;
                                position: sticky;
                                left: 0;
                                z-index: 25;
                                font-size: 0.68rem;
                            }

                            table {
                                border-collapse: collapse;
                                width: 100%;
                                font-size: 0.76rem;
                            }

                            thead {
                                position: sticky;
                                top: 0;
                                z-index: 30;
                            }

                            tbody td {
                                padding: 7px 10px;
                                border-bottom: 1px solid #263449;
                                text-align: right;
                                white-space: nowrap;
                                color: #e2e8f0;
                            }

                            td.td-desc {
                                position: sticky;
                                left: 0;
                                z-index: 10;
                                text-align: left !important;
                                background: #1e293b;
                            }

                            tbody tr:hover td {
                                background: #263449;
                            }

                            tbody tr:hover td.td-desc {
                                background: #263449;
                            }

                            .col-sep {
                                border-right: 2px solid #475569 !important;
                            }

                            tr.row-tipo td {
                                background: #1e3a5f;
                                font-weight: 700;
                                cursor: pointer;
                            }

                            tr.row-tipo td.td-desc {
                                background: #1e3a5f;
                            }

                            tr.row-nat td {
                                background: #1e293b;
                                font-weight: 600;
                                cursor: pointer;
                            }

                            tr.row-nat td.td-desc {
                                background: #1e293b;
                            }

                            tr.row-total td {
                                background: #14532d;
                                color: #bbf7d0;
                                font-weight: 700;
                                border-color: #166534;
                            }

                            tr.row-total td.td-desc {
                                background: #14532d;
                            }

                            .badge-tipo {
                                font-size: 0.6rem;
                                font-weight: 800;
                                padding: 2px 8px;
                                border-radius: 20px;
                            }

                            .badge-R {
                                background: rgba(16, 185, 129, 0.15);
                                color: #34d399;
                            }

                            .badge-D {
                                background: rgba(239, 68, 68, 0.15);
                                color: #f87171;
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
                                background: #0b1220;
                                border-radius: 6px;
                                overflow: hidden;
                            }

                            .mini-fill {
                                height: 100%;
                                border-radius: 6px;
                            }

                            .val-pos {
                                color: #34d399;
                                font-weight: 700;
                            }

                            .val-neg {
                                color: #f87171;
                                font-weight: 700;
                            }

                            tr.row-total .val-pos {
                                color: #6ee7b7;
                            }

                            tr.row-total .val-neg {
                                color: #fca5a5;
                            }

                            .empty {
                                text-align: center;
                                padding: 30px;
                                color: #64748b;
                            }

                            .ok {
                                color: #34d399;
                            }

                            .warn {
                                color: #fbbf24;
                            }

                            .danger {
                                color: #f87171;
                            }

                            .chevron {
                                transition: transform 0.2s ease-in-out;
                                display: inline-block;
                            }

                            tr.collapsed .chevron {
                                transform: rotate(-90deg);
                            }

                            tr.hidden {
                                display: none;
                            }
                        </style>
                    </head>

                    <%--============================================================DASHBOARD ORÇADO vs REALIZADO —
                        DADOS COMPLETOS Query: dados.sql como subconsulta, SEM agregação. Retorna TODAS as colunas
                        (PREVREC, PREVDESP, REALREC, REALDESP, COMP_REC, COMP_DESP, BAIXA_REC, BAIXA_DESP,
                        BAIXA_*_ANO_ANT). Agregação e filtros feitos 100% em
                        JavaScript.============================================================--%>
                        <snk:query var="dados">
                            SELECT
                            Q.CODMETA,
                            TO_CHAR(Q.DTREF, 'YYYY-MM') AS MES_ANO,
                            Q.CODEMP,
                            Q.CODPROJ,
                            P.IDENTIFICACAO AS PROJETO_NOME,
                            Q.CODCENCUS,
                            Q.CODNAT,
                            N.DESCRNAT AS NATUREZA_NOME,
                            N.TIPNAT,
                            Q.NROUNICO,
                            Q.TIPO,
                            NVL(Q.PREVDESP, 0) AS PREVDESP,
                            NVL(Q.REALDESP, 0) AS REALDESP,
                            NVL(Q.PREVREC, 0) AS PREVREC,
                            NVL(Q.REALREC, 0) AS REALREC,
                            NVL(Q.BAIXA_REC, 0) AS BAIXA_REC,
                            NVL(Q.BAIXA_DESP, 0) AS BAIXA_DESP,
                            NVL(Q.COMP_REC, 0) AS COMP_REC,
                            NVL(Q.COMP_DESP, 0) AS COMP_DESP,
                            NVL(Q.BAIXA_REC_ANO_ANT, 0) AS BAIXA_REC_ANO_ANT,
                            NVL(Q.BAIXA_DESP_ANO_ANT, 0) AS BAIXA_DESP_ANO_ANT
                            FROM (

                            -- ============================================================
                            -- 1. BAIXAS FINANCEIRAS - RECEITA E DESPESA (TGFFIN)
                            -- ============================================================
                            SELECT
                            5 AS CODMETA, TRUNC(FIN.DTNEG, 'MM') AS DTREF, FIN.CODEMP, FIN.CODPROJ, FIN.CODCENCUS,
                            FIN.CODNAT,
                            FIN.NUFIN AS NROUNICO, 'FINANCEIRO' AS TIPO,
                            0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS BAIXA_DESP,
                            CASE WHEN FIN.RECDESP = 1 THEN FIN.VLRDESDOB ELSE 0 END AS COMP_REC,
                            CASE WHEN FIN.RECDESP = -1 THEN FIN.VLRDESDOB ELSE 0 END AS COMP_DESP,
                            0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
                            FROM TGFFIN FIN
                            WHERE FIN.PROVISAO = 'N'
                            AND FIN.CODNAT IN (SELECT DISTINCT N.CODNAT FROM TGFNAT N
                            START WITH N.DESCRNAT IN (
                            'CANCELAMENTO RECEITA SERVICOS MENSALIDADES','OUTRAS RECEITAS','DOACOES RECEBIDAS','LOCACAO
                            DE SALAS',
                            'RECEBIMENTOS NAO IDENTIFICADOS','TAXAS DIVERSAS','DESCONTOS CONCEDIDOS','JUROS E ENCARGOS
                            PAGOS',
                            'TAXAS DE SERVICOS BANCARIOS','TAXAS OPERADORAS CARTOES E PIX','DESCONTOS OBTIDOS','JUROS E
                            ENCARGOS RECEBIDOS',
                            'RENDIMENTO SOBRE APLICACOES FINANCEIRAS','RECEBIMENTO PRECATORIOS')
                            CONNECT BY PRIOR N.CODNAT = N.CODNATPAI)
                            AND RECDESP <> 0

                                UNION ALL

                                -- ============================================================
                                -- 4. PORTAL - NF ENTRADA/SAIDA (TGFCAB - CODTIPOPER 1128)
                                -- ============================================================
                                SELECT
                                5 AS CODMETA, TRUNC(CAB.DTNEG, 'MM') AS DTREF, CAB.CODEMP, CAB.CODPROJ, CAB.CODCENCUS,
                                CAB.CODNAT,
                                CAB.NUNOTA AS NROUNICO, 'PORTAL' AS TIPO,
                                0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS
                                BAIXA_DESP,
                                CAB.VLRNOTA AS COMP_REC, 0 AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
                                FROM TGFCAB CAB
                                WHERE CAB.CODEMP IN (1, 2) AND CAB.CODTIPOPER IN (1128)

                                UNION ALL

                                SELECT
                                5 AS CODMETA, TRUNC(CAB.DTNEG, 'MM') AS DTREF, CAB.CODEMP, CAB.CODPROJ, CAB.CODCENCUS,
                                CAB.CODNAT,
                                CAB.NUNOTA AS NROUNICO, 'PORTAL' AS TIPO,
                                0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS
                                BAIXA_DESP,
                                CAB.VLRNOTA AS COMP_REC, 0 AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
                                FROM TGFCAB CAB
                                WHERE CAB.CODNAT IN (SELECT DISTINCT N.CODNAT FROM TGFNAT N
                                START WITH N.DESCRNAT IN (
                                'CUSTOS PESSOAL DIVERSOS DOCENTE','CUSTOS EDUCACIONAIS','DESPESAS
                                ADMINISTRATIVAS','DESPESAS DE ALUGUEL',
                                'DESPESAS DE CONSERVACAO E MANUTENCAO','DESPESAS GERAIS','DESPESAS
                                TECNOLOGICAS','GOVERNANCA CORPORATIVA',
                                'SERVICOS DE TERCEIROS','SERVICOS ASSESSORIA CORPORATIVA','PESSOAL DIVERSOS
                                ADMINISTRATIVO','DESPESAS COM VENDAS',
                                'DESPESAS MARKETING E COMERCIAL','DESPESAS INDEDUTIVEIS','DEDUCOES DA
                                RECEITA','DESCONTOS CONCEDIDOS',
                                'DEVOLUCOES/CANCELAMENTO','TRIBUTOS E CONTRIBUICOES S/ RECEITA','RECEITA DE BENS E
                                SERVICOS',
                                'RECEITA BRUTA DE VENDA DE MERCADORIAS','RECEITA OPERACIONAL BRUTA SERVICOS','CUSTOS DE
                                MERCADORIAS E SERVICOS',
                                'CUSTO PEDAGOGICO','INVESTIMENTOS')
                                CONNECT BY PRIOR N.CODNAT = N.CODNATPAI)

                                UNION ALL

                                -- ============================================================
                                -- 5. CONTABILIDADE - LANCAMENTOS
                                -- ============================================================
                                SELECT
                                5 AS CODMETA, LAN.REFERENCIA AS DTREF, LAN.CODEMP, LAN.CODPROJ, LAN.CODCENCUS,
                                NAT.CODNAT,
                                LAN.NUMDOC AS NROUNICO, 'CONTABILIDADE' AS TIPO,
                                0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS
                                BAIXA_DESP,
                                0 AS COMP_REC, LAN.VLRLANC AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
                                FROM TCBLAN LAN
                                LEFT JOIN TGFNAT NAT ON LAN.CODCTACTB = NAT.CODCTACTB
                                WHERE NAT.ANALITICA = 'S'
                                AND NAT.CODNAT IN (SELECT DISTINCT N.CODNAT FROM TGFNAT N
                                START WITH N.DESCRNAT IN (
                                'REPASSE VENDAS','DESPESAS COM PESSOAL ADMINISTRATIVO','SALARIOS E ORDENADOS
                                ADMINISTRATIVO',
                                'ENCARGOS PESSOAL ADMINISTRATIVOS','OUTROS RESULTADOS DE INVESTIMENTOS','PROVISOES E
                                PERDAS GERAIS',
                                'RESULTADO DE PARTIC. EM OUTRAS SOCIEDADES','DEPRECIACAO E AMORTIZACAO','RESULTADO COM
                                DERIVATIVOS DE SWAP - AVJ',
                                'VARIACAO CAMBIAL PASSIVA','VARIACAO MONETARIA PASSIVA','RESULTADO COM DERIVATIVOS DE
                                HEDGE - AVJ',
                                'VARIACAO CAMBIAL ATIVA','VARIACAO MONETARIA ATIVA','PROVISAO TRIBUTOS S/
                                LUCRO','DESCONTOS CONCEDIDOS',
                                'TRIBUTOS E CONTRIBUICOES S/ RECEITA','RECEITA CLINICA ESCOLA','RECEITA SERVICOS
                                MENSALIDADES','CUSTO DOCENTE',
                                'CUSTO PESSOAL DOCENTE','CUSTOS ENCARGOS DOCENTE','CUSTOS FINANCIAMENTO','DESPESAS
                                TRIBUTARIAS',
                                'PROVISAO DE TRIBUTOS S/ LUCRO')
                                CONNECT BY PRIOR N.CODNAT = N.CODNATPAI)

                                UNION ALL

                                -- ============================================================
                                -- 6. PORTAL - OUTRAS OPERACOES (TGFCAB)
                                -- ============================================================
                                SELECT
                                5 AS CODMETA, TRUNC(CAB.DTNEG, 'MM') AS DTREF, CAB.CODEMP, CAB.CODPROJ, CAB.CODCENCUS,
                                CAB.CODNAT,
                                CAB.NUNOTA AS NROUNICO, 'PORTAL' AS TIPO,
                                0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS
                                BAIXA_DESP,
                                CAB.VLRNOTA AS COMP_REC, 0 AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
                                FROM TGFCAB CAB
                                WHERE CAB.CODTIPOPER IN (1132, 1133, 1134, 1135, 1137, 1140, 1717, 1714)
                                AND CAB.CODNAT IN (SELECT DISTINCT N.CODNAT FROM TGFNAT N
                                START WITH N.DESCRNAT IN (
                                'CUSTO DE MERCADORIA','MATERIAL BRINDES E BINIFICAÇÕES','MATERIAL DE DEMONSTRAÇÃO')
                                CONNECT BY PRIOR N.CODNAT = N.CODNATPAI)

                                ) Q
                                LEFT JOIN TGFNAT N ON N.CODNAT = Q.CODNAT
                                LEFT JOIN TCSPRJ P ON P.CODPROJ = Q.CODPROJ
                                WHERE Q.DTREF >= DATE '2026-01-01'
                                AND Q.DTREF <= TRUNC(SYSDATE) ORDER BY Q.DTREF, Q.TIPO, N.DESCRNAT </snk:query>

                                    <body>
                                        <div class="wrap">

                                            <!-- Header + Filtros -->
                                            <div class="topbar">
                                                <div>
                                                    <h1>Acompanhamento Orçado vs Realizado &mdash; OPET</h1>
                                                    <p>Previsões, realizações, baixas financeiras e competência por
                                                        natureza, projeto e empresa.</p>
                                                </div>
                                                <div class="filtros">
                                                    <div class="filtro-item"><label>Ano</label><select id="f-ano"
                                                            class="filtro"></select></div>
                                                    <div class="filtro-item"><label>Empresa</label><select id="f-emp"
                                                            class="filtro"></select></div>
                                                    <div class="filtro-item"><label>Projeto</label><select id="f-proj"
                                                            class="filtro"></select></div>
                                                    <div class="filtro-item"><label>Natureza</label><select id="f-nat"
                                                            class="filtro"></select></div>
                                                    <div class="filtro-item"><label>Origem</label><select id="f-tipo"
                                                            class="filtro"></select></div>
                                                    <div class="filtro-item"><label>Regime</label>
                                                        <select id="f-regime" class="filtro">
                                                            <option value="COMP" selected>Competência</option>
                                                            <option value="BAIXA">Caixa (Baixas)</option>
                                                            <option value="TUDO">Todos</option>
                                                        </select>
                                                    </div>
                                                    <button id="btn-limpar" class="btn ghost">Limpar</button>
                                                    <button id="btn-excel" class="btn">Exportar Excel</button>
                                                </div>
                                            </div>

                                            <!-- KPIs -->
                                            <div class="kpis" id="kpis"></div>

                                            <!-- Gráficos -->
                                            <div class="charts">
                                                <div class="panel">
                                                    <h3>Orçado × Realizado por mês</h3>
                                                    <div class="chart-holder"><canvas id="chartMensal"></canvas></div>
                                                </div>
                                                <div class="panel">
                                                    <h3>Distribuição do orçado por natureza</h3>
                                                    <div class="chart-holder"><canvas id="chartPizza"></canvas></div>
                                                </div>
                                            </div>
                                            <div class="charts" style="grid-template-columns:1fr;">
                                                <div class="panel">
                                                    <h3>% de consumo por natureza (Top 12)</h3>
                                                    <div class="chart-holder" style="height:320px;"><canvas
                                                            id="chartConsumo"></canvas></div>
                                                </div>
                                            </div>

                                            <!-- Resumo Mensal -->
                                            <div class="tabela-panel">
                                                <div class="tabela-head">
                                                    <h3 style="margin:0; font-size:0.85rem;">Resumo Mensal</h3>
                                                    <span id="resumo-info"
                                                        style="font-size:0.72rem; color:#64748b;"></span>
                                                </div>
                                                <div class="tabela-scroll">
                                                    <table id="tabela-resumo">
                                                        <thead id="resumo-head"></thead>
                                                        <tbody id="resumo-body"></tbody>
                                                    </table>
                                                </div>
                                            </div>

                                            <!-- Detalhamento Hierárquico -->
                                            <div class="tabela-panel">
                                                <div class="tabela-head">
                                                    <h3 style="margin:0; font-size:0.85rem;">Detalhamento por Origem
                                                        &rsaquo; Natureza &rsaquo; Empresa / Projeto / Centro de Custo
                                                    </h3>
                                                    <span id="detalhe-info"
                                                        style="font-size:0.72rem; color:#64748b;"></span>
                                                </div>
                                                <div class="tabela-scroll" style="max-height:70vh;">
                                                    <table id="tabela-detalhe">
                                                        <thead id="detalhe-head"></thead>
                                                        <tbody id="detalhe-body"></tbody>
                                                    </table>
                                                </div>
                                            </div>

                                            <!-- Tabela por Natureza (legado, manter para compatibilidade) -->
                                            <div class="tabela-panel">
                                                <div class="tabela-head">
                                                    <h3 style="margin:0; font-size:0.85rem;">Consolidado por Natureza
                                                    </h3>
                                                    <span id="tab-info"
                                                        style="font-size:0.72rem; color:#64748b;"></span>
                                                </div>
                                                <div class="tabela-scroll">
                                                    <table id="tabela">
                                                        <thead>
                                                            <tr>
                                                                <th style="text-align:left;">Natureza</th>
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

                                        <%-- Dados serializados para JSON --%>
                                            <script>
                                                window.DADOS_RAW = [
                                                    <c:forEach items="${dados.rows}" var="r" varStatus="st">
                                                        {
                                                            "MES_ANO": "${r.MES_ANO}",
                                                        "CODEMP": "${r.CODEMP}",
                                                        "CODPROJ": "${r.CODPROJ}",
                                                        "PROJETO_NOME": "${r.PROJETO_NOME}",
                                                        "CODCENCUS": "${r.CODCENCUS}",
                                                        "CODNAT": "${r.CODNAT}",
                                                        "NATUREZA_NOME": "${r.NATUREZA_NOME}",
                                                        "TIPNAT": "${r.TIPNAT}",
                                                        "NROUNICO": "${r.NROUNICO}",
                                                        "TIPO": "${r.TIPO}",
                                                        "PREVDESP": ${empty r.PREVDESP ? 0 : r.PREVDESP},
                                                        "REALDESP": ${empty r.REALDESP ? 0 : r.REALDESP},
                                                        "PREVREC": ${empty r.PREVREC ? 0 : r.PREVREC},
                                                        "REALREC": ${empty r.REALREC ? 0 : r.REALREC},
                                                        "BAIXA_REC": ${empty r.BAIXA_REC ? 0 : r.BAIXA_REC},
                                                        "BAIXA_DESP": ${empty r.BAIXA_DESP ? 0 : r.BAIXA_DESP},
                                                        "COMP_REC": ${empty r.COMP_REC ? 0 : r.COMP_REC},
                                                        "COMP_DESP": ${empty r.COMP_DESP ? 0 : r.COMP_DESP},
                                                        "BAIXA_REC_ANO_ANT": ${empty r.BAIXA_REC_ANO_ANT ? 0 : r.BAIXA_REC_ANO_ANT},
                                                        "BAIXA_DESP_ANO_ANT": ${empty r.BAIXA_DESP_ANO_ANT ? 0 : r.BAIXA_DESP_ANO_ANT}
            }<c:if test="${!st.last}">,</c:if>
                                                    </c:forEach>
                                                ];
                                            </script>

                                            <%-- Lógica JS puro: usa CONCATENAÇÃO de string (nunca template literals)
                                                para não colidir com a sintaxe ${...} do EL do JSP. --%>
                                                <script>
                                                    document.addEventListener('DOMContentLoaded', function () {
                                                        var MESES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

                                                        /* ─── Formatação ─────────────────────────────────────────── */
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
                                                        function corConsumo(p) { return p > 100 ? '#f87171' : (p >= 85 ? '#fbbf24' : '#34d399'); }
                                                        function classeConsumo(p) { return p > 100 ? 'danger' : (p >= 85 ? 'warn' : 'ok'); }
                                                        function varClass(v) { return v > 0 ? 'val-pos' : (v < 0 ? 'val-neg' : ''); }

                                                        function novoAcc() {
                                                            return { orcRec: 0, orcDesp: 0, realRec: 0, realDesp: 0, anoAntRec: 0, anoAntDesp: 0 };
                                                        }
                                                        function somaAcc(a, v) {
                                                            a.orcRec += v.orcRec; a.orcDesp += v.orcDesp;
                                                            a.realRec += v.realRec; a.realDesp += v.realDesp;
                                                            a.anoAntRec += v.anoAntRec; a.anoAntDesp += v.anoAntDesp;
                                                        }

                                                        /* ─── Normalização dos dados brutos ──────────────────────── */
                                                        var RAW = (window.DADOS_RAW || []).map(function (r) {
                                                            var partes = (r.MES_ANO || '').split('-');
                                                            var ano = partes[0] || null;
                                                            var mes = partes[1] ? parseInt(partes[1], 10) : null;
                                                            return {
                                                                MES_ANO: r.MES_ANO, ANO: ano, MES: mes,
                                                                MES_KEY: r.MES_ANO,
                                                                CODEMP: r.CODEMP, EMPRESA_NOME: 'Empresa ' + r.CODEMP,
                                                                CODPROJ: r.CODPROJ,
                                                                PROJETO_NOME: (r.PROJETO_NOME && r.PROJETO_NOME.trim()) ? r.PROJETO_NOME.trim() : ('Projeto ' + r.CODPROJ),
                                                                CODCENCUS: r.CODCENCUS,
                                                                CODNAT: r.CODNAT,
                                                                NATUREZA_NOME: (r.NATUREZA_NOME && r.NATUREZA_NOME.trim()) ? r.NATUREZA_NOME.trim() : ('Natureza ' + r.CODNAT),
                                                                TIPNAT: (r.TIPNAT || '').trim(),
                                                                NROUNICO: r.NROUNICO,
                                                                TIPO: (r.TIPO || '').trim(),
                                                                PREVREC: +r.PREVREC || 0, PREVDESP: +r.PREVDESP || 0,
                                                                REALREC: +r.REALREC || 0, REALDESP: +r.REALDESP || 0,
                                                                BAIXA_REC: +r.BAIXA_REC || 0, BAIXA_DESP: +r.BAIXA_DESP || 0,
                                                                COMP_REC: +r.COMP_REC || 0, COMP_DESP: +r.COMP_DESP || 0,
                                                                BAIXA_REC_ANO_ANT: +r.BAIXA_REC_ANO_ANT || 0,
                                                                BAIXA_DESP_ANO_ANT: +r.BAIXA_DESP_ANO_ANT || 0
                                                            };
                                                        });

                                                        function el(id) { return document.getElementById(id); }
                                                        var fAno = el('f-ano'), fEmp = el('f-emp'), fProj = el('f-proj');
                                                        var fNat = el('f-nat'), fTipo = el('f-tipo'), fRegime = el('f-regime');
                                                        var chMensal, chPizza, chConsumo;
                                                        var expandidos = {};

                                                        /* ─── Extração de valores por regime ─────────────────────── */
                                                        function getValores(r) {
                                                            var regime = fRegime.value;
                                                            var realRec = 0, realDesp = 0;
                                                            if (regime === 'COMP') {
                                                                realRec = r.REALREC + r.COMP_REC;
                                                                realDesp = r.REALDESP + r.COMP_DESP;
                                                            } else if (regime === 'BAIXA') {
                                                                realRec = r.BAIXA_REC;
                                                                realDesp = r.BAIXA_DESP;
                                                            } else {
                                                                realRec = r.REALREC + r.COMP_REC + r.BAIXA_REC;
                                                                realDesp = r.REALDESP + r.COMP_DESP + r.BAIXA_DESP;
                                                            }
                                                            return {
                                                                orcRec: r.PREVREC, orcDesp: r.PREVDESP,
                                                                realRec: realRec, realDesp: realDesp,
                                                                anoAntRec: r.BAIXA_REC_ANO_ANT, anoAntDesp: r.BAIXA_DESP_ANO_ANT
                                                            };
                                                        }

                                                        /* ─── Filtros ────────────────────────────────────────────── */
                                                        function distinct(campoCod, campoNome) {
                                                            var m = {};
                                                            RAW.forEach(function (r) {
                                                                if (r[campoCod] !== '' && r[campoCod] != null) m[String(r[campoCod])] = r[campoNome];
                                                            });
                                                            return Object.keys(m).map(function (k) { return [k, m[k]]; })
                                                                .sort(function (a, b) { return String(a[1] || '').localeCompare(String(b[1] || '')); });
                                                        }
                                                        function opts(sel, lista, labelTodos) {
                                                            var html = '<option value="">' + labelTodos + '</option>';
                                                            lista.forEach(function (par) { html += '<option value="' + par[0] + '">' + (par[1] || par[0]) + '</option>'; });
                                                            sel.innerHTML = html;
                                                        }
                                                        function popularFiltros() {
                                                            var anos = [];
                                                            RAW.forEach(function (r) { if (r.ANO && anos.indexOf(r.ANO) < 0) anos.push(r.ANO); });
                                                            anos.sort(function (a, b) { return b - a; });
                                                            var htmlAno = '<option value="">Todos</option>';
                                                            anos.forEach(function (a) { htmlAno += '<option value="' + a + '">' + a + '</option>'; });
                                                            fAno.innerHTML = htmlAno;
                                                            var anoAtual = String(new Date().getFullYear());
                                                            if (anos.indexOf(anoAtual) >= 0) fAno.value = anoAtual;

                                                            opts(fEmp, distinct('CODEMP', 'EMPRESA_NOME'), 'Todas');
                                                            opts(fProj, distinct('CODPROJ', 'PROJETO_NOME'), 'Todos');
                                                            opts(fNat, distinct('CODNAT', 'NATUREZA_NOME'), 'Todas');

                                                            var tipos = {};
                                                            RAW.forEach(function (r) { if (r.TIPO) tipos[r.TIPO] = 1; });
                                                            var tipoHtml = '<option value="">Todas</option>';
                                                            Object.keys(tipos).sort().forEach(function (t) {
                                                                tipoHtml += '<option value="' + t + '">' + t + '</option>';
                                                            });
                                                            fTipo.innerHTML = tipoHtml;
                                                        }

                                                        function filtrar() {
                                                            return RAW.filter(function (r) {
                                                                if (fAno.value && r.ANO !== fAno.value) return false;
                                                                if (fEmp.value && String(r.CODEMP) !== fEmp.value) return false;
                                                                if (fProj.value && String(r.CODPROJ) !== fProj.value) return false;
                                                                if (fNat.value && String(r.CODNAT) !== fNat.value) return false;
                                                                if (fTipo.value && r.TIPO !== fTipo.value) return false;
                                                                return true;
                                                            });
                                                        }

                                                        /* ─── KPIs ───────────────────────────────────────────────── */
                                                        function renderKpis(linhas) {
                                                            var total = novoAcc();
                                                            linhas.forEach(function (r) { somaAcc(total, getValores(r)); });

                                                            var atingRec = total.orcRec !== 0 ? (total.realRec / total.orcRec) * 100 : NaN;
                                                            var atingDesp = total.orcDesp !== 0 ? (total.realDesp / total.orcDesp) * 100 : NaN;
                                                            var resultOrc = total.orcRec - total.orcDesp;
                                                            var resultReal = total.realRec - total.realDesp;
                                                            var varResult = resultReal - resultOrc;

                                                            var card = function (t, v, s, cls) {
                                                                return '<div class="kpi"><div class="k-t">' + t + '</div><div class="k-v ' + (cls || '') + '">' + v +
                                                                    '</div><div class="k-s">' + (s || '&nbsp;') + '</div></div>';
                                                            };

                                                            el('kpis').innerHTML =
                                                                card('Receita Orçada', fmtBRL(total.orcRec), linhas.length + ' registros', '') +
                                                                card('Receita Realizada', fmtBRL(total.realRec), 'Atingimento: ' + fmtPct(atingRec), total.realRec >= total.orcRec ? 'ok' : '') +
                                                                card('Despesa Orçada', fmtBRL(total.orcDesp), '', '') +
                                                                card('Despesa Realizada', fmtBRL(total.realDesp), 'Consumo: ' + fmtPct(atingDesp), total.realDesp > total.orcDesp ? 'danger' : '') +
                                                                card('Resultado', fmtBRL(resultReal), 'Orçado: ' + fmtBRL(resultOrc), resultReal >= 0 ? 'ok' : 'danger') +
                                                                card('Variação', fmtBRL(varResult), 'Baixas ano ant.: ' + fmtBRL(total.anoAntRec - total.anoAntDesp), varResult >= 0 ? 'ok' : 'danger');
                                                        }

                                                        /* ─── Gráfico mensal ─────────────────────────────────────── */
                                                        function renderMensal(linhas) {
                                                            var porMes = {};
                                                            linhas.forEach(function (r) {
                                                                if (!r.MES) return;
                                                                var v = getValores(r);
                                                                if (!porMes[r.MES]) porMes[r.MES] = novoAcc();
                                                                somaAcc(porMes[r.MES], v);
                                                            });
                                                            var labels = [], orcR = [], realR = [], orcD = [], realD = [];
                                                            for (var m = 1; m <= 12; m++) {
                                                                if (porMes[m]) {
                                                                    labels.push(MESES[m - 1]);
                                                                    orcR.push(porMes[m].orcRec); realR.push(porMes[m].realRec);
                                                                    orcD.push(porMes[m].orcDesp); realD.push(porMes[m].realDesp);
                                                                }
                                                            }
                                                            if (chMensal) chMensal.destroy();
                                                            chMensal = new Chart(el('chartMensal'), {
                                                                type: 'bar',
                                                                data: {
                                                                    labels: labels, datasets: [
                                                                        { label: 'Receita Orçada', data: orcR, backgroundColor: '#6366f1', borderRadius: 5 },
                                                                        { label: 'Receita Realizada', data: realR, backgroundColor: '#34d399', borderRadius: 5 },
                                                                        { label: 'Despesa Orçada', data: orcD, backgroundColor: '#f59e0b', borderRadius: 5 },
                                                                        { label: 'Despesa Realizada', data: realD, backgroundColor: '#f87171', borderRadius: 5 }
                                                                    ]
                                                                },
                                                                options: baseOpts(true)
                                                            });
                                                        }

                                                        /* ─── Gráfico pizza ──────────────────────────────────────── */
                                                        function renderPizza(linhas) {
                                                            var porNat = {};
                                                            linhas.forEach(function (r) {
                                                                var v = getValores(r);
                                                                var totalOrc = v.orcRec + v.orcDesp;
                                                                porNat[r.NATUREZA_NOME] = (porNat[r.NATUREZA_NOME] || 0) + Math.abs(totalOrc);
                                                            });
                                                            var arr = Object.keys(porNat).map(function (k) { return [k, porNat[k]]; })
                                                                .filter(function (e) { return e[1] > 0; })
                                                                .sort(function (a, b) { return b[1] - a[1]; });
                                                            var top = arr.slice(0, 7);
                                                            var resto = arr.slice(7).reduce(function (s, e) { return s + e[1]; }, 0);
                                                            if (resto > 0) top.push(['Outras', resto]);
                                                            var paleta = ['#6366f1', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#06b6d4', '#f43f5e', '#64748b'];

                                                            if (chPizza) chPizza.destroy();
                                                            chPizza = new Chart(el('chartPizza'), {
                                                                type: 'doughnut',
                                                                data: {
                                                                    labels: top.map(function (e) { return e[0]; }),
                                                                    datasets: [{ data: top.map(function (e) { return e[1]; }), backgroundColor: paleta, borderColor: '#1e293b', borderWidth: 2 }]
                                                                },
                                                                options: {
                                                                    responsive: true, maintainAspectRatio: false,
                                                                    plugins: {
                                                                        legend: { position: 'bottom', labels: { color: '#cbd5e1', font: { size: 10 }, boxWidth: 12, padding: 8 } },
                                                                        tooltip: { callbacks: { label: function (c) { return c.label + ': ' + fmtBRL(c.parsed); } } }
                                                                    }
                                                                }
                                                            });
                                                        }

                                                        /* ─── Gráfico % consumo por natureza ─────────────────────── */
                                                        function renderConsumo(linhas) {
                                                            var porNat = {};
                                                            linhas.forEach(function (r) {
                                                                var v = getValores(r);
                                                                if (!porNat[r.NATUREZA_NOME]) porNat[r.NATUREZA_NOME] = { orc: 0, real: 0 };
                                                                porNat[r.NATUREZA_NOME].orc += (v.orcRec + v.orcDesp);
                                                                porNat[r.NATUREZA_NOME].real += (v.realRec + v.realDesp);
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
                                                                        x: { ticks: { color: '#94a3b8', callback: function (v) { return v + '%'; } }, grid: { color: '#263449' } },
                                                                        y: { ticks: { color: '#cbd5e1', font: { size: 10 } }, grid: { display: false } }
                                                                    }
                                                                }
                                                            });
                                                        }

                                                        function baseOpts(money) {
                                                            return {
                                                                responsive: true, maintainAspectRatio: false,
                                                                plugins: {
                                                                    legend: { labels: { color: '#cbd5e1', font: { size: 11 }, boxWidth: 14 } },
                                                                    tooltip: { callbacks: { label: function (c) { return c.dataset.label + ': ' + fmtBRL(c.parsed.y); } } }
                                                                },
                                                                scales: {
                                                                    x: { ticks: { color: '#94a3b8' }, grid: { display: false } },
                                                                    y: { ticks: { color: '#94a3b8', callback: function (v) { return money ? fmtCompact(v) : v; } }, grid: { color: '#263449' } }
                                                                }
                                                            };
                                                        }

                                                        /* ─── Tabela: Resumo Mensal ──────────────────────────────── */
                                                        function renderResumo(linhas) {
                                                            var porMes = {};
                                                            linhas.forEach(function (r) {
                                                                if (!r.MES_KEY) return;
                                                                if (!porMes[r.MES_KEY]) porMes[r.MES_KEY] = novoAcc();
                                                                somaAcc(porMes[r.MES_KEY], getValores(r));
                                                            });
                                                            var meses = Object.keys(porMes).sort();

                                                            el('resumo-head').innerHTML =
                                                                '<tr>' +
                                                                '<th class="th-desc" style="min-width:120px;">Mês</th>' +
                                                                '<th class="th-group" colspan="3">Receitas</th>' +
                                                                '<th class="th-group" colspan="3">Despesas</th>' +
                                                                '<th class="th-group th-group-total" colspan="3">Resultado</th>' +
                                                                '</tr>' +
                                                                '<tr>' +
                                                                '<th class="th-desc-sub">.</th>' +
                                                                '<th class="th-sub">Orçado</th>' +
                                                                '<th class="th-sub">Realizado</th>' +
                                                                '<th class="th-sub col-sep">Ating. %</th>' +
                                                                '<th class="th-sub">Orçado</th>' +
                                                                '<th class="th-sub">Realizado</th>' +
                                                                '<th class="th-sub col-sep">Consumo %</th>' +
                                                                '<th class="th-sub th-sub-total">Orçado</th>' +
                                                                '<th class="th-sub th-sub-total">Realizado</th>' +
                                                                '<th class="th-sub th-sub-total">Var (R$)</th>' +
                                                                '</tr>';

                                                            var total = novoAcc();
                                                            var html = '';

                                                            meses.forEach(function (key) {
                                                                var d = porMes[key];
                                                                somaAcc(total, d);
                                                                var parts = key.split('-');
                                                                var label = MESES[parseInt(parts[1], 10) - 1] + '/' + parts[0].slice(2);
                                                                var resOrc = d.orcRec - d.orcDesp;
                                                                var resReal = d.realRec - d.realDesp;
                                                                var varRes = resReal - resOrc;
                                                                html +=
                                                                    '<tr>' +
                                                                    '<td class="td-desc font-semibold">' + label + '</td>' +
                                                                    '<td>' + fmtBRL(d.orcRec) + '</td>' +
                                                                    '<td>' + fmtBRL(d.realRec) + '</td>' +
                                                                    '<td class="col-sep">' + fmtPct(d.orcRec !== 0 ? d.realRec / d.orcRec * 100 : NaN) + '</td>' +
                                                                    '<td>' + fmtBRL(d.orcDesp) + '</td>' +
                                                                    '<td>' + fmtBRL(d.realDesp) + '</td>' +
                                                                    '<td class="col-sep">' + fmtPct(d.orcDesp !== 0 ? d.realDesp / d.orcDesp * 100 : NaN) + '</td>' +
                                                                    '<td>' + fmtBRL(resOrc) + '</td>' +
                                                                    '<td class="' + varClass(resReal) + '">' + fmtBRL(resReal) + '</td>' +
                                                                    '<td class="' + varClass(varRes) + '">' + fmtBRL(varRes) + '</td>' +
                                                                    '</tr>';
                                                            });

                                                            if (!meses.length) {
                                                                el('resumo-body').innerHTML = '<tr><td colspan="10" class="empty">Nenhum dado para os filtros selecionados.</td></tr>';
                                                                el('resumo-info').textContent = '';
                                                                return;
                                                            }

                                                            var resOrcT = total.orcRec - total.orcDesp;
                                                            var resRealT = total.realRec - total.realDesp;
                                                            html +=
                                                                '<tr class="row-total">' +
                                                                '<td class="td-desc">TOTAL</td>' +
                                                                '<td>' + fmtBRL(total.orcRec) + '</td>' +
                                                                '<td>' + fmtBRL(total.realRec) + '</td>' +
                                                                '<td class="col-sep">' + fmtPct(total.orcRec !== 0 ? total.realRec / total.orcRec * 100 : NaN) + '</td>' +
                                                                '<td>' + fmtBRL(total.orcDesp) + '</td>' +
                                                                '<td>' + fmtBRL(total.realDesp) + '</td>' +
                                                                '<td class="col-sep">' + fmtPct(total.orcDesp !== 0 ? total.realDesp / total.orcDesp * 100 : NaN) + '</td>' +
                                                                '<td>' + fmtBRL(resOrcT) + '</td>' +
                                                                '<td class="' + varClass(resRealT) + '">' + fmtBRL(resRealT) + '</td>' +
                                                                '<td class="' + varClass(resRealT - resOrcT) + '">' + fmtBRL(resRealT - resOrcT) + '</td>' +
                                                                '</tr>';

                                                            el('resumo-body').innerHTML = html;
                                                            el('resumo-info').textContent = meses.length + ' meses';
                                                        }

                                                        /* ─── Tabela: Detalhamento hierárquico ───────────────────── */
                                                        function renderDetalhe(linhas) {
                                                            var meses = [];
                                                            var mesMap = {};
                                                            linhas.forEach(function (r) {
                                                                if (r.MES_KEY && !mesMap[r.MES_KEY]) { mesMap[r.MES_KEY] = 1; meses.push(r.MES_KEY); }
                                                            });
                                                            meses.sort();

                                                            /* Cabeçalho */
                                                            var h1 = '<tr><th class="th-desc" rowspan="1">Origem / Natureza / Detalhe</th>';
                                                            var h2 = '<tr><th class="th-desc-sub">.</th>';
                                                            meses.forEach(function (key) {
                                                                var parts = key.split('-');
                                                                var lbl = MESES[parseInt(parts[1], 10) - 1] + '/' + parts[0].slice(2);
                                                                h1 += '<th class="th-group" colspan="5">' + lbl + '</th>';
                                                                h2 +=
                                                                    '<th class="th-sub">Orç. Rec</th>' +
                                                                    '<th class="th-sub">Real. Rec</th>' +
                                                                    '<th class="th-sub">Orç. Desp</th>' +
                                                                    '<th class="th-sub">Real. Desp</th>' +
                                                                    '<th class="th-sub col-sep">Var (R$)</th>';
                                                            });
                                                            h1 += '<th class="th-group th-group-total" colspan="5">Acumulado</th></tr>';
                                                            h2 +=
                                                                '<th class="th-sub th-sub-total">Orç. Rec</th>' +
                                                                '<th class="th-sub th-sub-total">Real. Rec</th>' +
                                                                '<th class="th-sub th-sub-total">Orç. Desp</th>' +
                                                                '<th class="th-sub th-sub-total">Real. Desp</th>' +
                                                                '<th class="th-sub th-sub-total">Var (R$)</th></tr>';
                                                            el('detalhe-head').innerHTML = h1 + h2;

                                                            if (!linhas.length) {
                                                                el('detalhe-body').innerHTML = '<tr><td colspan="100%" class="empty">Nenhum dado para os filtros selecionados.</td></tr>';
                                                                el('detalhe-info').textContent = '';
                                                                return;
                                                            }

                                                            /* Montar árvore TIPO -> NAT -> detalhe */
                                                            var arvore = {};
                                                            linhas.forEach(function (r) {
                                                                var v = getValores(r);
                                                                var tipo = r.TIPO || '(sem origem)';
                                                                var nat = (r.CODNAT !== null && r.CODNAT !== '') ? String(r.CODNAT) : '(sem natureza)';
                                                                var det = 'Emp ' + (r.CODEMP || '-') + ' \u2022 Proj ' + (r.CODPROJ || '-') + ' \u2022 CC ' + (r.CODCENCUS || '-');

                                                                if (!arvore[tipo]) arvore[tipo] = { total: novoAcc(), meses: {}, nats: {} };
                                                                var nTipo = arvore[tipo];
                                                                if (!nTipo.nats[nat]) nTipo.nats[nat] = { nome: r.NATUREZA_NOME, total: novoAcc(), meses: {}, dets: {} };
                                                                var nNat = nTipo.nats[nat];
                                                                if (!nNat.dets[det]) nNat.dets[det] = { total: novoAcc(), meses: {} };
                                                                var nDet = nNat.dets[det];

                                                                [nTipo, nNat, nDet].forEach(function (node) {
                                                                    somaAcc(node.total, v);
                                                                    if (r.MES_KEY) {
                                                                        if (!node.meses[r.MES_KEY]) node.meses[r.MES_KEY] = novoAcc();
                                                                        somaAcc(node.meses[r.MES_KEY], v);
                                                                    }
                                                                });
                                                            });

                                                            var celulas = function (node) {
                                                                var h = '';
                                                                meses.forEach(function (key) {
                                                                    var d = node.meses[key] || novoAcc();
                                                                    var variacao = (d.realRec - d.realDesp) - (d.orcRec - d.orcDesp);
                                                                    h +=
                                                                        '<td>' + fmtBRL(d.orcRec) + '</td>' +
                                                                        '<td>' + fmtBRL(d.realRec) + '</td>' +
                                                                        '<td>' + fmtBRL(d.orcDesp) + '</td>' +
                                                                        '<td>' + fmtBRL(d.realDesp) + '</td>' +
                                                                        '<td class="col-sep ' + varClass(variacao) + '">' + fmtBRL(variacao) + '</td>';
                                                                });
                                                                var t = node.total;
                                                                var varT = (t.realRec - t.realDesp) - (t.orcRec - t.orcDesp);
                                                                h +=
                                                                    '<td>' + fmtBRL(t.orcRec) + '</td>' +
                                                                    '<td>' + fmtBRL(t.realRec) + '</td>' +
                                                                    '<td>' + fmtBRL(t.orcDesp) + '</td>' +
                                                                    '<td>' + fmtBRL(t.realDesp) + '</td>' +
                                                                    '<td class="' + varClass(varT) + '">' + fmtBRL(varT) + '</td>';
                                                                return h;
                                                            };

                                                            var body = '';
                                                            var totalGeral = novoAcc();
                                                            var totalGeralMeses = {};

                                                            Object.keys(arvore).sort().forEach(function (tipo) {
                                                                var nTipo = arvore[tipo];
                                                                var idTipo = 'T|' + tipo;
                                                                var abertoTipo = expandidos[idTipo] === true;

                                                                somaAcc(totalGeral, nTipo.total);
                                                                meses.forEach(function (key) {
                                                                    var md = nTipo.meses[key];
                                                                    if (md) {
                                                                        if (!totalGeralMeses[key]) totalGeralMeses[key] = novoAcc();
                                                                        somaAcc(totalGeralMeses[key], md);
                                                                    }
                                                                });

                                                                body +=
                                                                    '<tr class="row-tipo ' + (abertoTipo ? '' : 'collapsed') + '" data-node-id="' + idTipo + '">' +
                                                                    '<td class="td-desc"><span class="chevron mr-1">&#9662;</span>' + tipo + '</td>' +
                                                                    celulas(nTipo) +
                                                                    '</tr>';

                                                                Object.keys(nTipo.nats).sort().forEach(function (nat) {
                                                                    var nNat = nTipo.nats[nat];
                                                                    var idNat = 'N|' + tipo + '|' + nat;
                                                                    var abertoNat = expandidos[idNat] === true;

                                                                    body +=
                                                                        '<tr class="row-nat ' + (abertoNat ? '' : 'collapsed') + ' ' + (abertoTipo ? '' : 'hidden') + '" ' +
                                                                        'data-node-id="' + idNat + '" data-parent-id="' + idTipo + '">' +
                                                                        '<td class="td-desc" style="padding-left: 28px;"><span class="chevron mr-1">&#9662;</span>' + (nNat.nome || 'Natureza ' + nat) + '</td>' +
                                                                        celulas(nNat) +
                                                                        '</tr>';

                                                                    Object.keys(nNat.dets).sort().forEach(function (det) {
                                                                        body +=
                                                                            '<tr class="' + (abertoTipo && abertoNat ? '' : 'hidden') + '" data-parent-id="' + idNat + '">' +
                                                                            '<td class="td-desc text-slate-400" style="padding-left: 52px;">' + det + '</td>' +
                                                                            celulas(nNat.dets[det]) +
                                                                            '</tr>';
                                                                    });
                                                                });
                                                            });

                                                            var varTG = (totalGeral.realRec - totalGeral.realDesp) - (totalGeral.orcRec - totalGeral.orcDesp);
                                                            body +=
                                                                '<tr class="row-total">' +
                                                                '<td class="td-desc">TOTAL GERAL</td>' +
                                                                '<td>' + fmtBRL(totalGeral.orcRec) + '</td>' +
                                                                '<td>' + fmtBRL(totalGeral.realRec) + '</td>' +
                                                                '<td>' + fmtBRL(totalGeral.orcDesp) + '</td>' +
                                                                '<td>' + fmtBRL(totalGeral.realDesp) + '</td>' +
                                                                '<td class="' + varClass(varTG) + '">' + fmtBRL(varTG) + '</td>';

                                                            meses.forEach(function (key) {
                                                                var d = totalGeralMeses[key] || novoAcc();
                                                                var variacao = (d.realRec - d.realDesp) - (d.orcRec - d.orcDesp);
                                                                body +=
                                                                    '<td>' + fmtBRL(d.orcRec) + '</td>' +
                                                                    '<td>' + fmtBRL(d.realRec) + '</td>' +
                                                                    '<td>' + fmtBRL(d.orcDesp) + '</td>' +
                                                                    '<td>' + fmtBRL(d.realDesp) + '</td>' +
                                                                    '<td class="col-sep ' + varClass(variacao) + '">' + fmtBRL(variacao) + '</td>';
                                                            });
                                                            body += '</tr>';

                                                            el('detalhe-body').innerHTML = body;
                                                            el('detalhe-info').textContent = Object.keys(arvore).length + ' origens \u2022 ' + meses.length + ' meses';
                                                        }

                                                        /* ─── Expandir / recolher ────────────────────────────────── */
                                                        el('detalhe-body').addEventListener('click', function (event) {
                                                            var target = event.target.closest('tr[data-node-id]');
                                                            if (!target) return;
                                                            var nodeId = target.getAttribute('data-node-id');

                                                            target.classList.toggle('collapsed');
                                                            var isCollapsed = target.classList.contains('collapsed');
                                                            expandidos[nodeId] = !isCollapsed;

                                                            var children = document.querySelectorAll('tr[data-parent-id="' + nodeId + '"]');
                                                            for (var i = 0; i < children.length; i++) {
                                                                var child = children[i];
                                                                child.classList.toggle('hidden', isCollapsed);
                                                                if (isCollapsed) {
                                                                    var childId = child.getAttribute('data-node-id');
                                                                    if (childId) {
                                                                        expandidos[childId] = false;
                                                                        child.classList.add('collapsed');
                                                                        var grandchildren = document.querySelectorAll('tr[data-parent-id="' + childId + '"]');
                                                                        for (var j = 0; j < grandchildren.length; j++) {
                                                                            grandchildren[j].classList.add('hidden');
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });

                                                        /* ─── Tabela consolidada por natureza ────────────────────── */
                                                        function renderTabela(linhas) {
                                                            var porNat = {};
                                                            linhas.forEach(function (r) {
                                                                var v = getValores(r), k = r.CODNAT;
                                                                if (!porNat[k]) porNat[k] = { nome: r.NATUREZA_NOME, tipo: r.TIPNAT, orcRec: 0, orcDesp: 0, realRec: 0, realDesp: 0 };
                                                                porNat[k].orcRec += v.orcRec; porNat[k].orcDesp += v.orcDesp;
                                                                porNat[k].realRec += v.realRec; porNat[k].realDesp += v.realDesp;
                                                            });
                                                            var arr = Object.keys(porNat).map(function (k) {
                                                                var n = porNat[k];
                                                                var orc = n.orcRec + n.orcDesp;
                                                                var real = n.realRec + n.realDesp;
                                                                return { nome: n.nome, tipo: n.tipo, orc: orc, real: real };
                                                            }).filter(function (n) { return n.orc !== 0 || n.real !== 0; })
                                                                .sort(function (a, b) { return b.orc - a.orc; });

                                                            var tbody = el('tbody');
                                                            if (!arr.length) {
                                                                tbody.innerHTML = '<tr><td colspan="6" class="empty">Nenhum dado para os filtros selecionados.</td></tr>';
                                                                el('tab-info').textContent = '';
                                                                return;
                                                            }

                                                            var html = '';
                                                            arr.forEach(function (n) {
                                                                var saldo = n.orc - n.real;
                                                                var pct = n.orc !== 0 ? (n.real / n.orc) * 100 : 0;
                                                                var cor = corConsumo(pct);
                                                                var badge = n.tipo === 'R'
                                                                    ? '<span class="badge-tipo badge-R">RECEITA</span>'
                                                                    : '<span class="badge-tipo badge-D">DESPESA</span>';
                                                                html +=
                                                                    '<tr>' +
                                                                    '<td style="text-align:left;">' + n.nome + '</td>' +
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
                                                            tbody.innerHTML = html;
                                                            el('tab-info').textContent = arr.length + ' naturezas';
                                                        }

                                                        /* ─── Render principal ───────────────────────────────────── */
                                                        function render() {
                                                            var linhas = filtrar();
                                                            renderKpis(linhas);
                                                            renderMensal(linhas);
                                                            renderPizza(linhas);
                                                            renderConsumo(linhas);
                                                            renderResumo(linhas);
                                                            renderDetalhe(linhas);
                                                            renderTabela(linhas);
                                                        }

                                                        /* ─── Exportar Excel ─────────────────────────────────────── */
                                                        el('btn-excel').addEventListener('click', function () {
                                                            try {
                                                                var wb = XLSX.utils.book_new();
                                                                var linhas = filtrar();

                                                                /* Aba Resumo Mensal */
                                                                var porMes = {};
                                                                linhas.forEach(function (r) {
                                                                    if (!r.MES_KEY) return;
                                                                    if (!porMes[r.MES_KEY]) porMes[r.MES_KEY] = novoAcc();
                                                                    somaAcc(porMes[r.MES_KEY], getValores(r));
                                                                });
                                                                var resumoData = Object.keys(porMes).sort().map(function (key) {
                                                                    var d = porMes[key];
                                                                    return {
                                                                        'Mês': key,
                                                                        'Receita Orçada': d.orcRec, 'Receita Realizada': d.realRec,
                                                                        'Ating. %': d.orcRec ? +((d.realRec / d.orcRec) * 100).toFixed(1) : 0,
                                                                        'Despesa Orçada': d.orcDesp, 'Despesa Realizada': d.realDesp,
                                                                        'Consumo %': d.orcDesp ? +((d.realDesp / d.orcDesp) * 100).toFixed(1) : 0,
                                                                        'Resultado Orçado': d.orcRec - d.orcDesp,
                                                                        'Resultado Realizado': d.realRec - d.realDesp,
                                                                        'Variação': (d.realRec - d.realDesp) - (d.orcRec - d.orcDesp)
                                                                    };
                                                                });
                                                                XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(resumoData), 'Resumo Mensal');

                                                                /* Aba Detalhamento */
                                                                var detData = linhas.map(function (r) {
                                                                    var v = getValores(r);
                                                                    return {
                                                                        'Mês': r.MES_ANO, 'Origem': r.TIPO, 'Natureza': r.NATUREZA_NOME,
                                                                        'Cód. Natureza': r.CODNAT, 'Empresa': r.CODEMP, 'Projeto': r.PROJETO_NOME,
                                                                        'Centro Custo': r.CODCENCUS,
                                                                        'Orç. Rec': v.orcRec, 'Real. Rec': v.realRec,
                                                                        'Orç. Desp': v.orcDesp, 'Real. Desp': v.realDesp,
                                                                        'Variação': (v.realRec - v.realDesp) - (v.orcRec - v.orcDesp)
                                                                    };
                                                                });
                                                                XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(detData), 'Detalhamento');

                                                                /* Aba Por Natureza */
                                                                var porNat = {};
                                                                linhas.forEach(function (r) {
                                                                    var v = getValores(r), k = r.CODNAT;
                                                                    if (!porNat[k]) porNat[k] = { Natureza: r.NATUREZA_NOME, Tipo: r.TIPNAT, Orcado: 0, Realizado: 0 };
                                                                    porNat[k].Orcado += (v.orcRec + v.orcDesp);
                                                                    porNat[k].Realizado += (v.realRec + v.realDesp);
                                                                });
                                                                var natData = Object.keys(porNat).map(function (k) {
                                                                    var n = porNat[k];
                                                                    return {
                                                                        Natureza: n.Natureza, Tipo: n.Tipo === 'R' ? 'Receita' : 'Despesa',
                                                                        Orçado: n.Orcado, Realizado: n.Realizado, Saldo: n.Orcado - n.Realizado,
                                                                        'Consumo %': n.Orcado ? +((n.Realizado / n.Orcado) * 100).toFixed(1) : 0
                                                                    };
                                                                });
                                                                XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(natData), 'Por Natureza');

                                                                XLSX.writeFile(wb, 'OPET_Orcado_vs_Realizado.xlsx');
                                                            } catch (e) { console.error(e); }
                                                        });

                                                        /* ─── Eventos ────────────────────────────────────────────── */
                                                        [fAno, fEmp, fProj, fNat, fTipo, fRegime].forEach(function (f) {
                                                            f.addEventListener('change', render);
                                                        });
                                                        el('btn-limpar').addEventListener('click', function () {
                                                            fAno.value = ''; fEmp.value = ''; fProj.value = '';
                                                            fNat.value = ''; fTipo.value = ''; fRegime.value = 'COMP';
                                                            render();
                                                        });

                                                        /* ─── Init ───────────────────────────────────────────────── */
                                                        if (!RAW.length) {
                                                            el('kpis').innerHTML = '';
                                                            el('tbody').innerHTML = '<tr><td colspan="6" class="empty">Nenhum registro retornado pela consulta.</td></tr>';
                                                            el('resumo-body').innerHTML = '<tr><td colspan="10" class="empty">Nenhum registro.</td></tr>';
                                                            el('detalhe-body').innerHTML = '<tr><td colspan="100%" class="empty">Nenhum registro.</td></tr>';
                                                            return;
                                                        }
                                                        popularFiltros();
                                                        render();
                                                    });
                                                </script>
                                    </body>

                    </html>