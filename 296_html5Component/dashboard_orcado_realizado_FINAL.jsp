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
                                    gap: 12px 14px;
                                    align-items: flex-end;
                                    flex: 1 1 100%;
                                    background: var(--surface);
                                    border: 1px solid var(--border);
                                    border-radius: 12px;
                                    padding: 14px 16px;
                                    box-shadow: 0 1px 2px rgba(43, 58, 74, 0.04);
                                }

                                .filtro-item {
                                    display: flex;
                                    flex-direction: column;
                                    gap: 4px;
                                    flex: 0 1 auto;
                                }

                                .filtros .btn {
                                    align-self: flex-end;
                                    height: 34px;
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

                                /* ── Multi-Select Dropdown ──────────────── */
                                .ms-wrap {
                                    position: relative;
                                    min-width: 180px;
                                }

                                .ms-trigger {
                                    display: flex;
                                    align-items: center;
                                    justify-content: space-between;
                                    gap: 6px;
                                    background: var(--surface);
                                    color: var(--ink);
                                    border: 1px solid var(--border);
                                    border-radius: 8px;
                                    padding: 7px 10px;
                                    font-size: 0.8rem;
                                    font-weight: 600;
                                    cursor: pointer;
                                    user-select: none;
                                    transition: border-color 0.15s, box-shadow 0.15s;
                                    min-height: 34px;
                                    white-space: nowrap;
                                    overflow: hidden;
                                    text-overflow: ellipsis;
                                }

                                .ms-trigger:hover {
                                    border-color: var(--primary);
                                }

                                .ms-trigger.open {
                                    border-color: var(--primary);
                                    box-shadow: 0 0 0 2px rgba(58, 74, 94, 0.15);
                                }

                                .ms-trigger .ms-arrow {
                                    flex-shrink: 0;
                                    font-size: 0.55rem;
                                    color: var(--ink-2);
                                    transition: transform 0.2s ease;
                                }

                                .ms-trigger.open .ms-arrow {
                                    transform: rotate(180deg);
                                }

                                .ms-trigger .ms-badge {
                                    background: var(--primary);
                                    color: #fff;
                                    font-size: 0.6rem;
                                    font-weight: 800;
                                    padding: 1px 7px;
                                    border-radius: 10px;
                                    flex-shrink: 0;
                                    min-width: 18px;
                                    text-align: center;
                                }

                                .ms-dropdown {
                                    display: none;
                                    position: absolute;
                                    top: calc(100% + 4px);
                                    left: 0;
                                    min-width: 100%;
                                    width: max-content;
                                    max-width: 340px;
                                    background: var(--surface);
                                    border: 1px solid var(--border);
                                    border-radius: 10px;
                                    box-shadow: 0 8px 24px rgba(43, 58, 74, 0.14);
                                    z-index: 1000;
                                    overflow: hidden;
                                    animation: msSlideIn 0.15s ease;
                                }

                                @keyframes msSlideIn {
                                    from { opacity: 0; transform: translateY(-6px); }
                                    to   { opacity: 1; transform: translateY(0); }
                                }

                                .ms-dropdown.show {
                                    display: block;
                                }

                                .ms-search-box {
                                    padding: 8px 10px;
                                    border-bottom: 1px solid var(--border);
                                }

                                .ms-search-box input {
                                    width: 100%;
                                    border: 1px solid var(--border);
                                    border-radius: 6px;
                                    padding: 6px 10px;
                                    font-size: 0.78rem;
                                    font-family: inherit;
                                    color: var(--ink);
                                    background: var(--surface-2);
                                    outline: none;
                                    transition: border-color 0.15s;
                                }

                                .ms-search-box input:focus {
                                    border-color: var(--primary);
                                }

                                .ms-search-box input::placeholder {
                                    color: #9aa7b4;
                                }

                                .ms-actions {
                                    display: flex;
                                    gap: 4px;
                                    padding: 6px 10px;
                                    border-bottom: 1px solid var(--border);
                                    background: var(--surface-2);
                                }

                                .ms-actions button {
                                    flex: 1;
                                    border: none;
                                    background: transparent;
                                    color: var(--primary);
                                    font-size: 0.68rem;
                                    font-weight: 700;
                                    font-family: inherit;
                                    padding: 4px 6px;
                                    border-radius: 5px;
                                    cursor: pointer;
                                    transition: background 0.12s;
                                }

                                .ms-actions button:hover {
                                    background: rgba(58, 74, 94, 0.08);
                                }

                                .ms-list {
                                    max-height: 220px;
                                    overflow-y: auto;
                                    padding: 4px 0;
                                }

                                .ms-list::-webkit-scrollbar {
                                    width: 5px;
                                }

                                .ms-list::-webkit-scrollbar-thumb {
                                    background: #c4ccd6;
                                    border-radius: 4px;
                                }

                                .ms-option {
                                    display: flex;
                                    align-items: center;
                                    gap: 8px;
                                    padding: 6px 12px;
                                    font-size: 0.76rem;
                                    color: var(--ink);
                                    cursor: pointer;
                                    transition: background 0.1s;
                                    user-select: none;
                                }

                                .ms-option:hover {
                                    background: var(--surface-2);
                                }

                                .ms-option.hidden {
                                    display: none;
                                }

                                .ms-checkbox {
                                    flex-shrink: 0;
                                    width: 16px;
                                    height: 16px;
                                    border: 2px solid var(--border);
                                    border-radius: 4px;
                                    display: flex;
                                    align-items: center;
                                    justify-content: center;
                                    transition: all 0.12s;
                                    font-size: 0.6rem;
                                    color: transparent;
                                }

                                .ms-option.selected .ms-checkbox {
                                    background: var(--primary);
                                    border-color: var(--primary);
                                    color: #fff;
                                }

                                .ms-option-label {
                                    overflow: hidden;
                                    text-overflow: ellipsis;
                                    white-space: nowrap;
                                }

                                .ms-empty {
                                    padding: 14px 12px;
                                    text-align: center;
                                    font-size: 0.74rem;
                                    color: #9aa7b4;
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

                                /* Hierarquia DRE — nós da árvore */
                                tbody tr.row-node td {
                                    cursor: pointer;
                                    font-weight: 700;
                                }

                                tbody tr.row-node.lvl-1 td { background: #d0d9e6 !important; font-size: 0.82rem; }
                                tbody tr.row-node.lvl-2 td { background: #dce4ee !important; }
                                tbody tr.row-node.lvl-3 td { background: #e6ebf2 !important; }
                                tbody tr.row-node.lvl-4 td { background: #eef1f6 !important; font-weight: 600; }
                                tbody tr.row-node.lvl-5 td,
                                tbody tr.row-node.lvl-deep td { background: #f3f5f8 !important; font-weight: 600; }

                                tbody tr.row-node:hover td { background: #cdd6e3 !important; }

                                /* Folhas analíticas */
                                tbody tr.row-leaf td {
                                    cursor: pointer;
                                    font-weight: 600;
                                    background: #f5f7fa !important;
                                }
                                tbody tr.row-leaf:hover td { background: #e8edf3 !important; }

                                /* Detalhes (Empresa · Projeto · CR) */
                                tbody tr.row-det td {
                                    background: var(--surface) !important;
                                }

                                tbody tr.row-det td.det {
                                    text-align: left;
                                    color: var(--ink-2);
                                    white-space: normal;
                                }

                                tr.hidden {
                                    display: none;
                                }

                                .chevron {
                                    display: inline-block;
                                    transition: transform 0.15s ease;
                                    color: var(--ink-2);
                                    font-size: 0.7rem;
                                }

                                tr.row-node:not(.collapsed) > td .chevron,
                                tr.row-leaf:not(.collapsed) > td .chevron {
                                    transform: rotate(90deg);
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

                                /* ── Modal Titulos ──────────────── */
                                .modal-backdrop {
                                    position: fixed;
                                    top: 0;
                                    left: 0;
                                    width: 100vw;
                                    height: 100vh;
                                    background: rgba(15, 23, 42, 0.55);
                                    backdrop-filter: blur(3px);
                                    z-index: 9999;
                                    display: flex;
                                    align-items: center;
                                    justify-content: center;
                                    animation: modalFadeIn 0.15s ease-out;
                                }

                                .modal-box {
                                    background: #ffffff;
                                    width: 92%;
                                    max-width: 1050px;
                                    max-height: 85vh;
                                    border-radius: 10px;
                                    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25);
                                    display: flex;
                                    flex-direction: column;
                                    overflow: hidden;
                                    animation: modalSlideUp 0.15s ease-out;
                                }

                                .modal-header {
                                    padding: 12px 20px;
                                    background: #1e3a5f;
                                    color: #fff;
                                    display: flex;
                                    align-items: center;
                                    justify-content: space-between;
                                    font-weight: 700;
                                    font-size: 0.85rem;
                                }

                                .modal-body {
                                    padding: 0;
                                    max-height: 60vh;
                                    overflow-y: auto;
                                }

                                .modal-footer {
                                    padding: 10px 20px;
                                    background: #f8fafc;
                                    border-top: 1px solid #e2e8f0;
                                    display: flex;
                                    justify-content: flex-end;
                                }

                                @keyframes modalFadeIn { from { opacity: 0; } to { opacity: 1; } }
                                @keyframes modalSlideUp { from { transform: translateY(12px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
                            </style>
                        </head>

                        <%--============================================================ORÇADO vs REALIZADO
                            ORÇADO=PREVREC / PREVDESP (TGFMET: CODMETA 5=oficial, 7=forecast) REALIZADO=COMP_REC /
                            COMP_DESP (financeiro / portal / contabilidade) Agregado por mês / empresa / projeto / CR /
                            natureza / origem. Período: ano de 2026 (realizado até hoje; orçado cobre o ano p/
                            forecast).============================================================--%>
                            <snk:query var="dados">
                                SELECT
                                TO_CHAR(Q.DTREF, 'YYYY-MM') AS MES_ANO,
                                Q.CODEMP,
                                E.NOMEFANTASIA AS EMPRESA_NOME,
                                Q.CODPROJ,
                                P.IDENTIFICACAO AS PROJETO_NOME,
                                Q.CODCENCUS,
                                C.DESCRCENCUS AS CC_NOME,
                                C.CODUSURESP AS CODRESP,
                                U.NOMEUSU AS RESP_NOME,
                                Q.CODNAT,
                                N.DESCRNAT AS NATUREZA_NOME,
                                N.TIPNAT,
                                Q.TIPO,
                                SUM(NVL(Q.PREVREC, 0)) AS ORCADO_REC,
                                SUM(NVL(Q.PREVDESP, 0)) AS ORCADO_DESP,
                                SUM(NVL(Q.COMP_REC, 0)) AS REALIZADO_REC,
                                SUM(NVL(Q.COMP_DESP, 0)) AS REALIZADO_DESP
                                FROM (
                                SELECT
                                5 AS CODMETA, TRUNC(FIN.DTNEG, 'MM') AS DTREF, FIN.CODEMP, FIN.CODPROJ, FIN.CODCENCUS,
                                FIN.CODNAT,
                                FIN.NUFIN AS NROUNICO, 'FINANCEIRO' AS TIPO,
                                0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS
                                BAIXA_DESP,
                                CASE WHEN FIN.RECDESP = 1 THEN FIN.VLRDESDOB ELSE 0 END AS COMP_REC,
                                CASE WHEN FIN.RECDESP = -1 THEN FIN.VLRDESDOB ELSE 0 END AS COMP_DESP,
                                0 AS BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
                                FROM TGFFIN FIN
                                WHERE FIN.PROVISAO = 'N'
                                AND FIN.CODNAT IN (SELECT DISTINCT N.CODNAT FROM TGFNAT N START WITH N.DESCRNAT IN
                                ('CANCELAMENTO RECEITA SERVICOS MENSALIDADES', 'OUTRAS RECEITAS', 'DOACOES RECEBIDAS',
                                'LOCACAO DE SALAS', 'RECEBIMENTOS NAO IDENTIFICADOS', 'TAXAS DIVERSAS', 'DESCONTOS
                                CONCEDIDOS', 'JUROS E ENCARGOS PAGOS', 'TAXAS DE SERVICOS BANCARIOS', 'TAXAS OPERADORAS
                                CARTOES E PIX', 'DESCONTOS OBTIDOS', 'JUROS E ENCARGOS RECEBIDOS', 'RENDIMENTO SOBRE
                                APLICACOES FINANCEIRAS', 'RECEBIMENTO PRECATORIOS') CONNECT BY PRIOR N.CODNAT =
                                N.CODNATPAI)
                                AND RECDESP <> 0

                                    UNION ALL

                                    SELECT
                                    5 AS CODMETA, TRUNC(CAB.DTNEG, 'MM') AS DTREF, CAB.CODEMP, CAB.CODPROJ,
                                    CAB.CODCENCUS, CAB.CODNAT,
                                    CAB.NUNOTA AS NROUNICO, 'PORTAL' AS TIPO,
                                    0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS
                                    BAIXA_DESP,
                                    CAB.VLRNOTA AS COMP_REC, 0 AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS
                                    BAIXA_DESP_ANO_ANT
                                    FROM TGFCAB CAB
                                    WHERE CAB.CODEMP IN (1, 2) AND CAB.CODTIPOPER IN (1128)

                                    UNION ALL

                                    SELECT
                                    5 AS CODMETA, TRUNC(CAB.DTNEG, 'MM') AS DTREF, CAB.CODEMP, CAB.CODPROJ,
                                    CAB.CODCENCUS, CAB.CODNAT,
                                    CAB.NUNOTA AS NROUNICO, 'PORTAL' AS TIPO,
                                    0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS
                                    BAIXA_DESP,
                                    CASE WHEN CAB.TIPMOV = 'D' THEN -CAB.VLRNOTA ELSE CAB.VLRNOTA END AS COMP_REC, 0 AS
                                    COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS
                                    BAIXA_DESP_ANO_ANT
                                    FROM TGFCAB CAB
                                    WHERE CAB.CODNAT IN (SELECT DISTINCT N.CODNAT FROM TGFNAT N START WITH N.DESCRNAT IN
                                    ('CUSTOS PESSOAL DIVERSOS DOCENTE', 'CUSTOS EDUCACIONAIS', 'DESPESAS
                                    ADMINISTRATIVAS', 'DESPESAS DE ALUGUEL', 'DESPESAS DE CONSERVACAO E MANUTENCAO',
                                    'DESPESAS GERAIS', 'DESPESAS TECNOLOGICAS', 'GOVERNANCA CORPORATIVA', 'SERVICOS DE
                                    TERCEIROS', 'SERVICOS ASSESSORIA CORPORATIVA', 'PESSOAL DIVERSOS ADMINISTRATIVO',
                                    'DESPESAS COM VENDAS', 'DESPESAS MARKETING E COMERCIAL', 'DESPESAS INDEDUTIVEIS',
                                    'DEDUCOES DA RECEITA', 'DESCONTOS CONCEDIDOS', 'DEVOLUCOES/CANCELAMENTO', 'TRIBUTOS
                                    E CONTRIBUICOES S/ RECEITA', 'RECEITA DE BENS E SERVICOS', 'RECEITA BRUTA DE VENDA
                                    DE MERCADORIAS', 'RECEITA OPERACIONAL BRUTA SERVICOS', 'CUSTOS DE MERCADORIAS E
                                    SERVICOS', 'CUSTO PEDAGOGICO', 'INVESTIMENTOS') CONNECT BY PRIOR N.CODNAT =
                                    N.CODNATPAI) AND TIPMOV IN ('V','C','D')

                                    UNION ALL

                                    SELECT
                                    5 AS CODMETA, LAN.REFERENCIA AS DTREF, LAN.CODEMP, LAN.CODPROJ, LAN.CODCENCUS,
                                    NAT.CODNAT,
                                    LAN.NUMDOC AS NROUNICO, 'CONTABILIDADE' AS TIPO,
                                    0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS
                                    BAIXA_DESP,
                                    0 AS COMP_REC, LAN.VLRLANC AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS
                                    BAIXA_DESP_ANO_ANT
                                    FROM TCBLAN LAN
                                    LEFT JOIN TGFNAT NAT ON LAN.CODCTACTB = NAT.CODCTACTB
                                    WHERE NAT.ANALITICA = 'S'
                                    AND NAT.CODNAT IN (SELECT DISTINCT N.CODNAT FROM TGFNAT N START WITH N.DESCRNAT IN
                                    ('REPASSE VENDAS', 'DESPESAS COM PESSOAL ADMINISTRATIVO', 'SALARIOS E ORDENADOS
                                    ADMINISTRATIVO', 'ENCARGOS PESSOAL ADMINISTRATIVOS', 'OUTROS RESULTADOS DE
                                    INVESTIMENTOS', 'PROVISOES E PERDAS GERAIS', 'RESULTADO DE PARTIC. EM OUTRAS
                                    SOCIEDADES', 'DEPRECIACAO E AMORTIZACAO', 'RESULTADO COM DERIVATIVOS DE SWAP - AVJ',
                                    'VARIACAO CAMBIAL PASSIVA', 'VARIACAO MONETARIA PASSIVA', 'RESULTADO COM DERIVATIVOS
                                    DE HEDGE - AVJ', 'VARIACAO CAMBIAL ATIVA', 'VARIACAO MONETARIA ATIVA', 'PROVISAO
                                    TRIBUTOS S/ LUCRO', 'DESCONTOS CONCEDIDOS', 'TRIBUTOS E CONTRIBUICOES S/ RECEITA',
                                    'RECEITA CLINICA ESCOLA', 'RECEITA SERVICOS MENSALIDADES', 'CUSTO DOCENTE', 'CUSTO
                                    PESSOAL DOCENTE', 'CUSTOS ENCARGOS DOCENTE', 'CUSTOS FINANCIAMENTO', 'DESPESAS
                                    TRIBUTARIAS', 'PROVISAO DE TRIBUTOS S/ LUCRO') CONNECT BY PRIOR N.CODNAT =
                                    N.CODNATPAI)

                                    UNION ALL

                                    SELECT
                                    5 AS CODMETA, TRUNC(CAB.DTNEG, 'MM') AS DTREF, CAB.CODEMP, CAB.CODPROJ,
                                    CAB.CODCENCUS, CAB.CODNAT,
                                    CAB.NUNOTA AS NROUNICO, 'PORTAL' AS TIPO,
                                    0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS
                                    BAIXA_DESP,
                                    CAB.VLRNOTA AS COMP_REC, 0 AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS
                                    BAIXA_DESP_ANO_ANT
                                    FROM TGFCAB CAB
                                    WHERE CAB.CODTIPOPER IN (1132, 1133, 1134, 1135, 1137, 1140, 1717, 1714)
                                    AND CAB.CODNAT IN (SELECT DISTINCT N.CODNAT FROM TGFNAT N START WITH N.DESCRNAT IN
                                    ('CUSTO DE MERCADORIA', 'MATERIAL BRINDES E BINIFICAÇÕES', 'MATERIAL DE
                                    DEMONSTRAÇÃO') CONNECT BY PRIOR N.CODNAT = N.CODNATPAI)

                                    UNION ALL

                                    -- ============================================================
                                    -- DIREITOS AUTORAIS - REALIZADO (base de comissão x % direito autoral) => COMP_DESP
                                    -- Natureza fixa 2010300. Valor = COMPARC.
                                    -- ============================================================
                                    SELECT
                                    5 AS CODMETA, DA.REFERENCIA AS DTREF, DA.CODEMP, DA.CODPROJ, DA.CODCENCUS,
                                    DA.CODNAT,
                                    0 AS NROUNICO, 'DIREITOS AUTORAIS' AS TIPO,
                                    0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0 AS
                                    BAIXA_DESP,
                                    0 AS COMP_REC, DA.COMPARC AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS
                                    BAIXA_DESP_ANO_ANT
                                    FROM (
                                    SELECT
                                    TRUNC(FIN.DHBAIXA, 'MM') AS REFERENCIA,
                                    EMP.CODEMP,
                                    2010300 AS CODNAT,
                                    CAB.CODCENCUS,
                                    CAB.CODPROJ,
                                    ROUND(SUM(BASE.VLRBASECOM * (DIR.PERCENTUAL / 100)), 2) AS COMPARC
                                    FROM (
                                    SELECT
                                    CAB.NUNOTA, ITE.CODPROD, CAB2.CODTIPOPER, CAB2.DHTIPOPER,
                                    SUM((ITE.VLRUNIT * ITE.QTDNEG) * (FIN.VLRDESDOB / CAB.VLRNOTA)) AS VLRBASECOM
                                    FROM TGFCAB CAB
                                    INNER JOIN (
                                    SELECT CONT.IDCONTAINERS, CONT.NUNOTA, C.CODTIPOPER, C.DHTIPOPER
                                    FROM TGFCONT CONT
                                    INNER JOIN TGFCAB C ON C.NUNOTA = CONT.NUNOTA
                                    WHERE C.CODTIPOPER = 1131
                                    ) CAB2 ON CAB2.NUNOTA = CAB.NUNOTA
                                    INNER JOIN TGFITE ITE ON ITE.NUNOTA = CAB2.IDCONTAINERS
                                    INNER JOIN (
                                    SELECT NUNOTA, SUM(VLRDESDOB) AS VLRDESDOB
                                    FROM TGFFIN
                                    WHERE DHBAIXA BETWEEN '01/01/2000' AND '01/01/2990'
                                    GROUP BY NUNOTA
                                    ) FIN ON FIN.NUNOTA = CAB.NUNOTA
                                    WHERE CAB.STATUSNOTA = 'L' AND CAB.TIPMOV IN ('V', 'D') AND ITE.USOPROD <> 'D'
                                        GROUP BY CAB.NUNOTA, ITE.CODPROD, CAB2.CODTIPOPER, CAB2.DHTIPOPER

                                        UNION ALL

                                        SELECT
                                        CAB.NUNOTA, ITE.CODPROD, CAB.CODTIPOPER, CAB.DHTIPOPER,
                                        SUM((ITE.VLRUNIT * ITE.QTDNEG) * (FIN.VLRDESDOB / CAB.VLRNOTA)) AS VLRBASECOM
                                        FROM TGFCAB CAB
                                        INNER JOIN TGFITE ITE ON ITE.NUNOTA = CAB.NUNOTA
                                        INNER JOIN (
                                        SELECT NUNOTA, SUM(VLRDESDOB) AS VLRDESDOB
                                        FROM TGFFIN
                                        WHERE DHBAIXA BETWEEN '01/01/2000' AND '01/01/2990'
                                        GROUP BY NUNOTA
                                        ) FIN ON FIN.NUNOTA = CAB.NUNOTA
                                        WHERE CAB.STATUSNOTA = 'L' AND CAB.TIPMOV IN ('V', 'D') AND ITE.USOPROD <> 'D'
                                            AND CAB.CODTIPOPER IN (1714, 1718)
                                            GROUP BY CAB.NUNOTA, ITE.CODPROD, CAB.CODTIPOPER, CAB.DHTIPOPER
                                            ) BASE
                                            INNER JOIN TGFCAB CAB ON CAB.NUNOTA = BASE.NUNOTA
                                            INNER JOIN TGFPAR PAR ON PAR.CODPARC = CAB.CODPARC
                                            INNER JOIN TSIEMP EMP ON EMP.CODEMP = CAB.CODEMP
                                            INNER JOIN TGFPRO PRO ON PRO.CODPROD = BASE.CODPROD
                                            INNER JOIN AD_DIRAUTPROD DIR ON DIR.CODPROD = PRO.CODPROD
                                            INNER JOIN TGFVEN AUT ON AUT.CODVEND = DIR.CODVEND
                                            INNER JOIN TGFTOP TPO ON TPO.CODTIPOPER = BASE.CODTIPOPER AND TPO.DHALTER =
                                            BASE.DHTIPOPER
                                            INNER JOIN TGFFIN FIN ON FIN.NUNOTA = CAB.NUNOTA AND FIN.DHBAIXA BETWEEN
                                            '01/01/2000' AND '01/01/2990'
                                            WHERE NVL(PAR.AD_NAOCALCDIRAUT, 'N') = 'N' AND DIR.PERCENTUAL IS NOT NULL
                                            GROUP BY TRUNC(FIN.DHBAIXA, 'MM'), EMP.CODEMP, CAB.CODCENCUS, CAB.CODPROJ
                                            ) DA

                                            UNION ALL

                                            -- ============================================================
                                            -- COMISSÕES - REALIZADO (TGFCOM) => COMP_DESP. Natureza fixa 2010401.
                                            -- ============================================================
                                            SELECT
                                            5 AS CODMETA, CM.DTFAT AS DTREF, CM.CODEMP, CM.CODPROJ, CM.CODCENCUS,
                                            CM.CODNAT,
                                            0 AS NROUNICO, 'COMISSAO' AS TIPO,
                                            0 AS PREVDESP, 0 AS REALDESP, 0 AS PREVREC, 0 AS REALREC, 0 AS BAIXA_REC, 0
                                            AS BAIXA_DESP,
                                            0 AS COMP_REC, CM.VLRCOMI AS COMP_DESP, 0 AS BAIXA_REC_ANO_ANT, 0 AS
                                            BAIXA_DESP_ANO_ANT
                                            FROM (
                                            SELECT DISTINCT
                                            CAB.CODEMP, CAB.CODPROJ, CAB.CODCENCUS,
                                            2010401 AS CODNAT,
                                            TRUNC(CAB.DTFATUR, 'MM') AS DTFAT,
                                            COM.VLRCOM AS VLRCOMI
                                            FROM TGFCOM COM, TGFVEN VEN, TGFFIN FIN, TGFCAB CAB, TGFPAR PAR, TSIEMP EMP,
                                            TGFCCM CCM
                                            WHERE COM.CODVEND = VEN.CODVEND
                                            AND FIN.NUNOTA = CAB.NUNOTA
                                            AND CAB.NUNOTA = COM.NUNOTAORIG
                                            AND CAB.CODPARC = PAR.CODPARC
                                            AND CAB.CODEMP = EMP.CODEMP
                                            AND CCM.NUNOTA = CAB.NUNOTA
                                            AND CCM.CODVEND = VEN.CODVEND
                                            AND VEN.TIPVEND = 'R'
                                            ) CM

                                            UNION ALL

                                            SELECT
                                            5 AS CODMETA, MET.DTREF AS DTREF, MET.CODEMP, MET.CODPROJ, MET.CODCENCUS,
                                            MET.CODNAT,
                                            0 AS NROUNICO, 'ORCAMENTO' AS TIPO,
                                            ABS(MET.PREVDESP) AS PREVDESP, NVL(MET.REALDESP, 0) AS REALDESP,
                                            ABS(MET.PREVREC) AS PREVREC, NVL(MET.REALREC, 0) AS REALREC,
                                            0 AS BAIXA_REC, 0 AS BAIXA_DESP, 0 AS COMP_REC, 0 AS COMP_DESP, 0 AS
                                            BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
                                            FROM TGFMET MET
                                            WHERE MET.CODMETA = 5

                                            UNION ALL

                                            SELECT
                                            7 AS CODMETA, MET.DTREF AS DTREF, MET.CODEMP, MET.CODPROJ, MET.CODCENCUS,
                                            MET.CODNAT,
                                            0 AS NROUNICO, 'FORECAST' AS TIPO,
                                            ABS(MET.PREVDESP) AS PREVDESP, NVL(MET.REALDESP, 0) AS REALDESP,
                                            ABS(MET.PREVREC) AS PREVREC, NVL(MET.REALREC, 0) AS REALREC,
                                            0 AS BAIXA_REC, 0 AS BAIXA_DESP, 0 AS COMP_REC, 0 AS COMP_DESP, 0 AS
                                            BAIXA_REC_ANO_ANT, 0 AS BAIXA_DESP_ANO_ANT
                                            FROM TGFMET MET
                                            WHERE MET.CODMETA = 7

                                            ) Q
                                            LEFT JOIN TGFNAT N ON N.CODNAT = Q.CODNAT
                                            LEFT JOIN TCSPRJ P ON P.CODPROJ = Q.CODPROJ
                                            LEFT JOIN TSIEMP E ON E.CODEMP = Q.CODEMP
                                            LEFT JOIN TSICUS C ON C.CODCENCUS = Q.CODCENCUS
                                            LEFT JOIN TSIUSU U ON U.CODUSU = C.CODUSURESP
                                            WHERE Q.DTREF >= DATE '2026-01-01'
                                            AND Q.DTREF < DATE '2027-01-01'
                                            AND (Q.CODEMP IS NULL OR Q.CODEMP NOT IN (999, 103, 777, 666, 555, 108, 109))
                                            GROUP BY TO_CHAR(Q.DTREF, 'YYYY-MM' ),
                                                Q.CODEMP, E.NOMEFANTASIA, Q.CODPROJ, P.IDENTIFICACAO, Q.CODCENCUS,
                                                C.DESCRCENCUS, C.CODUSURESP, U.NOMEUSU, Q.CODNAT, N.DESCRNAT, N.TIPNAT,
                                                Q.TIPO ORDER BY TO_CHAR(Q.DTREF, 'YYYY-MM' ), N.DESCRNAT </snk:query>

                                <%-- Hierarquia completa de naturezas para montar a árvore DRE --%>
                                <snk:query var="arvoreNat">
                                    SELECT N.CODNAT, N.DESCRNAT, N.CODNATPAI, N.TIPNAT, N.ANALITICA,
                                           LEVEL AS NIVEL
                                    FROM   TGFNAT N
                                    WHERE  SUBSTR(TO_CHAR(N.CODNAT), 1, 1) NOT IN ('8','9')
                                    START WITH (N.CODNATPAI = -999999999 OR N.CODNATPAI IS NULL OR N.CODNATPAI = 0)
                                    CONNECT BY NOCYCLE PRIOR N.CODNAT = N.CODNATPAI
                                           AND N.CODNAT <> N.CODNATPAI
                                    ORDER SIBLINGS BY
                                      CASE
                                        WHEN UPPER(N.DESCRNAT) LIKE '%RECEITA%' THEN 1
                                        WHEN UPPER(N.DESCRNAT) LIKE '%CUSTO%'   THEN 2
                                        WHEN UPPER(N.DESCRNAT) LIKE '%DESPESA%' THEN 3
                                        WHEN UPPER(N.DESCRNAT) LIKE '%DEPRECIA%' THEN 4
                                        WHEN UPPER(N.DESCRNAT) LIKE '%PROVIS%'  THEN 5
                                        WHEN UPPER(N.DESCRNAT) LIKE '%INVESTIMENTO%' THEN 6
                                        ELSE 7
                                      END, N.CODNAT
                                </snk:query>

                                                <body>
                                                    <div class="wrap">

                                                        <!-- Header + Filtros -->
                                                        <div class="topbar">
                                                            <div>
                                                                <h1>Orçado &times; Realizado &mdash; OPET</h1>
                                                                <p>Orçamento (TGFMET) contra o realizado comprometido,
                                                                    com
                                                                    forecast
                                                                    de fechamento &mdash; 2026.</p>
                                                            </div>
                                                            <div class="filtros">
                                                                <div class="filtro-item"><label>Data
                                                                        inicial</label><input type="date" id="f-dt-de"
                                                                        class="filtro" min="2026-01-01"
                                                                        max="2026-12-31"></div>
                                                                <div class="filtro-item"><label>Data final</label><input
                                                                        type="date" id="f-dt-ate" class="filtro"
                                                                        min="2026-01-01" max="2026-12-31"></div>
                                                                <div class="filtro-item"><label>&nbsp;</label><button
                                                                        id="btn-carregar" class="btn">Carregar</button>
                                                                </div>
                                                                <div class="filtro-item">
                                                                    <label>Empresa</label>
                                                                    <div id="f-emp" class="ms-wrap"></div>
                                                                </div>
                                                                <div class="filtro-item">
                                                                    <label>Centro de Resultado</label>
                                                                    <div id="f-cc" class="ms-wrap"></div>
                                                                </div>
                                                                <div class="filtro-item">
                                                                    <label>Projeto</label>
                                                                    <div id="f-proj" class="ms-wrap"></div>
                                                                </div>
                                                                <div class="filtro-item">
                                                                    <label>Natureza</label>
                                                                    <div id="f-nat" class="ms-wrap"></div>
                                                                </div>
                                                                <div class="filtro-item">
                                                                    <label>Responsável</label><select id="f-resp"
                                                                        class="filtro"></select>
                                                                </div>
                                                                <div class="filtro-item"><label>Origem
                                                                        (realizado)</label><select id="f-orig"
                                                                        class="filtro"></select></div>
                                                                <div class="filtro-item"><label>Cenário orçado</label>
                                                                    <select id="f-cenario" class="filtro">
                                                                        <option value="ORCAMENTO" selected>Oficial
                                                                        </option>
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
                                                                <button id="btn-limpar"
                                                                    class="btn ghost">Limpar</button>
                                                                <button id="btn-excel" class="btn">Exportar
                                                                    Excel</button>
                                                            </div>
                                                        </div>

                                                        <!-- Barras de consumo: Despesa e Receita -->
                                                        <div class="charts" style="grid-template-columns:1fr 1fr;">
                                                            <div class="consumo-hero">
                                                                <div class="titulo">Consumo do Orçamento de Despesas
                                                                </div>
                                                                <div class="pct" id="hero-d-pct">0%</div>
                                                                <div class="legenda" id="hero-d-legenda">&nbsp;</div>
                                                                <div class="bar-track">
                                                                    <div class="bar-fill" id="hero-d-bar"
                                                                        style="width:0%; background:#86efac;"></div>
                                                                    <div class="bar-limit" style="left:100%;"></div>
                                                                </div>
                                                            </div>
                                                            <div class="consumo-hero">
                                                                <div class="titulo">Realização do Orçamento de Receitas
                                                                </div>
                                                                <div class="pct" id="hero-r-pct">0%</div>
                                                                <div class="legenda" id="hero-r-legenda">&nbsp;</div>
                                                                <div class="bar-track">
                                                                    <div class="bar-fill" id="hero-r-bar"
                                                                        style="width:0%; background:#86efac;"></div>
                                                                    <div class="bar-limit" style="left:100%;"></div>
                                                                </div>
                                                            </div>
                                                        </div>

                                                        <!-- KPIs -->
                                                        <div class="kpis" id="kpis"></div>

                                                        <!-- Gráficos -->
                                                        <div class="charts">
                                                            <div class="panel">
                                                                <h3>Orçado &times; Realizado por mês</h3>
                                                                <div class="chart-holder"><canvas
                                                                        id="chartMensal"></canvas>
                                                                </div>
                                                            </div>
                                                            <div class="panel">
                                                                <h3>Distribuição do orçado por natureza</h3>
                                                                <div class="chart-holder"><canvas
                                                                        id="chartPizza"></canvas>
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <div class="charts">
                                                            <div class="panel">
                                                                <h3>Acumulado &amp; Forecast (projeção de fechamento)
                                                                </h3>
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
                                                                <h3 style="margin:0; font-size:0.85rem;">Demonstrativo de Resultados &mdash; DRE Hierárquico</h3>
                                                                <div
                                                                    style="display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
                                                                    <button id="btn-expandir" class="btn ghost"
                                                                        style="padding:5px 10px;">Expandir tudo</button>
                                                                    <button id="btn-recolher" class="btn ghost"
                                                                        style="padding:5px 10px;">Recolher tudo</button>
                                                                    <span id="tab-info"
                                                                        style="font-size:0.72rem; color:var(--ink-2);"></span>
                                                                </div>
                                                            </div>
                                                            <div class="tabela-scroll">
                                                                <table id="tabela">
                                                                    <thead>
                                                                        <tr>
                                                                            <th>Natureza / Detalhe</th>
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

                                                    <!-- Modal Detalhes dos Títulos -->
                                                    <div id="modal-titulos-container" class="modal-backdrop" style="display:none;">
                                                        <div class="modal-box" style="max-width:1200px;">
                                                            <div class="modal-header">
                                                                <span id="modal-titulos-title">Detalhes dos Títulos</span>
                                                                <button type="button" id="modal-titulos-close" style="background:none; border:none; color:#fff; font-size:1.4rem; cursor:pointer; font-weight:700;">&times;</button>
                                                            </div>
                                                            <!-- Barra de busca + exportar -->
                                                            <div id="modal-toolbar" style="padding:10px 16px; background:#f8fafc; border-bottom:1px solid #e2e8f0; display:flex; align-items:center; gap:10px;">
                                                                <input type="text" id="modal-titulos-search" placeholder="Pesquisar..." style="flex:1; padding:6px 12px; border:1px solid #cbd5e1; border-radius:6px; font-size:0.8rem; outline:none;" />
                                                                <button type="button" id="modal-titulos-export" style="background:#1e3a5f; color:#fff; border:none; border-radius:6px; padding:6px 14px; font-size:0.78rem; font-weight:700; cursor:pointer;">&#128196; Exportar XLSX</button>
                                                            </div>
                                                            <!-- Tabela única de lançamentos -->
                                                            <div class="modal-body" id="modal-layer-titulos">
                                                                <table id="modal-titulos-table" style="width:100%; border-collapse:collapse; font-size:0.75rem;">
                                                                    <thead>
                                                                        <tr style="background:#1e3a5f; color:#fff; position:sticky; top:0; z-index:10;">
                                                                            <th style="padding:9px 10px; text-align:left;">Origem</th>
                                                                            <th style="padding:9px 10px; text-align:left;">Nº Doc</th>
                                                                            <th style="padding:9px 10px; text-align:left;">Data</th>
                                                                            <th style="padding:9px 10px; text-align:right;">Valor (R$)</th>
                                                                            <th style="padding:9px 10px; text-align:left;">Empresa</th>
                                                                            <th style="padding:9px 10px; text-align:left;">Projeto</th>
                                                                            <th style="padding:9px 10px; text-align:left;">Centro de Resultado</th>
                                                                            <th style="padding:9px 10px; text-align:left;">Natureza</th>
                                                                        </tr>
                                                                    </thead>
                                                                    <tbody id="modal-titulos-tbody"></tbody>
                                                                </table>
                                                            </div>
                                                            <div class="modal-footer" style="justify-content:space-between; align-items:center;">
                                                                <span id="modal-footer-info" style="font-size:0.72rem; color:#5c6b7a;"></span>
                                                                <button type="button" class="btn" id="modal-titulos-btn-close">Fechar</button>
                                                            </div>
                                                        </div>
                                                    </div>

                                                    <%-- Dados serializados para JSON (EL do servidor só é usado aqui)
                                                        --%>
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
                                                                    "CODRESP": "${r.CODRESP}",
                                                                    "RESP_NOME": "${fn:replace(fn:replace(r.RESP_NOME, '\\', '\\\\'), '"', '\\"')}",
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

                                                        <%-- Árvore de naturezas para o DRE hierárquico --%>
                                                        <script>
                                                            window.ARVORE_NAT = [
                                                                <c:forEach items="${arvoreNat.rows}" var="n" varStatus="st">
                                                                    {
                                                                        "CODNAT": "${n.CODNAT}",
                                                                    "DESCRNAT": "${fn:replace(fn:replace(n.DESCRNAT, '\\', '\\\\'), '"', '\\"')}",
                                                                    "CODNATPAI": "${n.CODNATPAI}",
                                                                    "TIPNAT": "${n.TIPNAT}",
                                                                    "ANALITICA": "${n.ANALITICA}",
                                                                    "NIVEL": "${n.NIVEL}"
                                                                    }<c:if test="${!st.last}">,</c:if>
                                                                </c:forEach>
                                                            ];
                                                        </script>

                                                        <%-- Lógica em JavaScript puro: usa CONCATENAÇÃO de string
                                                            (nunca template literals), para não colidir com a sintaxe
                                                            ${...} do EL do JSP. --%>
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
                                                                    // Receita: quanto MAIOR o realizado, melhor (>=100% verde, <85% vermelho)
                                                                    function corReceita(p) { return p >= 100 ? '#16a34a' : (p >= 85 ? '#d97706' : '#dc2626'); }
                                                                    function classeReceita(p) { return p >= 100 ? 'ok' : (p >= 85 ? 'warn' : 'danger'); }
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
                                                                            CODRESP: r.CODRESP,
                                                                            RESP_NOME: (r.RESP_NOME && r.RESP_NOME.trim()) ? r.RESP_NOME.trim() : ((r.CODRESP !== '' && r.CODRESP != null) ? ('Usuário ' + r.CODRESP) : 'Sem responsável'),
                                                                            CODNAT: r.CODNAT,
                                                                            NATUREZA_NOME: (r.NATUREZA_NOME && r.NATUREZA_NOME.trim()) ? r.NATUREZA_NOME.trim() : ('Natureza ' + r.CODNAT),
                                                                            TIPNAT: (r.TIPNAT || '').trim(),
                                                                            TIPO: (r.TIPO || '').trim(),
                                                                            orcRec: num(r.ORCADO_REC), orcDesp: num(r.ORCADO_DESP),
                                                                            realRec: num(r.REALIZADO_REC), realDesp: num(r.REALIZADO_DESP)
                                                                        };
                                                                    });

                                                                    function el(id) { return document.getElementById(id); }
                                                                    var fDtDe = el('f-dt-de'), fDtAte = el('f-dt-ate'),
                                                                        fOrig = el('f-orig'),
                                                                        fResp = el('f-resp'),
                                                                        fCenario = el('f-cenario'), fTipo = el('f-tipo');
                                                                    var msEmp, msCc, msProj, msNat; // MultiSelect instances
                                                                    var chMensal, chPizza, chConsumo, chForecast;
                                                                    var carregado = false;   // só renderiza depois de preencher a data e clicar em Carregar

                                                                    // Converte 'YYYY-MM-DD' (input date) em 'YYYY-MM' para comparar com MES_ANO
                                                                    function toMes(dataStr) { return (dataStr && dataStr.length >= 7) ? dataStr.slice(0, 7) : ''; }

                                                                    // ─── MultiSelect Component ───────────────────────────────
                                                                    function MultiSelect(container, placeholder, onChange) {
                                                                        var self = this;
                                                                        self.container = container;
                                                                        self.placeholder = placeholder || 'Selecione...';
                                                                        self.onChange = onChange || function () {};
                                                                        self.items = [];
                                                                        self.selected = {};
                                                                        self._open = false;

                                                                        // Build DOM
                                                                        var trigger = document.createElement('div');
                                                                        trigger.className = 'ms-trigger';
                                                                        trigger.innerHTML = '<span class="ms-label">' + self.placeholder + '</span>' +
                                                                            '<span class="ms-badge" style="display:none;">0</span>' +
                                                                            '<span class="ms-arrow">▼</span>';
                                                                        self.triggerEl = trigger;
                                                                        self.labelEl = trigger.querySelector('.ms-label');
                                                                        self.badgeEl = trigger.querySelector('.ms-badge');

                                                                        var dropdown = document.createElement('div');
                                                                        dropdown.className = 'ms-dropdown';
                                                                        dropdown.innerHTML =
                                                                            '<div class="ms-search-box"><input type="text" placeholder="Pesquisar..." /></div>' +
                                                                            '<div class="ms-actions">' +
                                                                            '<button type="button" class="ms-btn-all">Selecionar tudo</button>' +
                                                                            '<button type="button" class="ms-btn-none">Limpar</button>' +
                                                                            '</div>' +
                                                                            '<div class="ms-list"></div>';
                                                                        self.dropdownEl = dropdown;
                                                                        self.searchInput = dropdown.querySelector('.ms-search-box input');
                                                                        self.listEl = dropdown.querySelector('.ms-list');

                                                                        container.appendChild(trigger);
                                                                        container.appendChild(dropdown);

                                                                        // Events
                                                                        trigger.addEventListener('click', function (ev) {
                                                                            ev.stopPropagation();
                                                                            self.toggle();
                                                                        });

                                                                        self.searchInput.addEventListener('input', function () {
                                                                            self._filter(this.value);
                                                                        });
                                                                        self.searchInput.addEventListener('click', function (ev) { ev.stopPropagation(); });

                                                                        dropdown.querySelector('.ms-btn-all').addEventListener('click', function (ev) {
                                                                            ev.stopPropagation();
                                                                            self.selectAll();
                                                                        });
                                                                        dropdown.querySelector('.ms-btn-none').addEventListener('click', function (ev) {
                                                                            ev.stopPropagation();
                                                                            self.clearAll();
                                                                        });

                                                                        self.listEl.addEventListener('click', function (ev) {
                                                                            ev.stopPropagation();
                                                                            var opt = ev.target;
                                                                            while (opt && !opt.classList.contains('ms-option')) opt = opt.parentNode;
                                                                            if (!opt) return;
                                                                            var val = opt.getAttribute('data-value');
                                                                            if (self.selected[val]) {
                                                                                delete self.selected[val];
                                                                                opt.classList.remove('selected');
                                                                            } else {
                                                                                self.selected[val] = true;
                                                                                opt.classList.add('selected');
                                                                            }
                                                                            self._updateTrigger();
                                                                            self.onChange();
                                                                        });

                                                                        // Close on outside click
                                                                        document.addEventListener('click', function () {
                                                                            if (self._open) self.close();
                                                                        });
                                                                        dropdown.addEventListener('click', function (ev) { ev.stopPropagation(); });
                                                                    }

                                                                    MultiSelect.prototype.setItems = function (items, keepSelection) {
                                                                        // items = [[value, label], ...]
                                                                        this.items = items;
                                                                        var newSelected = {};
                                                                        if (keepSelection && this.selected) {
                                                                            var validKeys = {};
                                                                            items.forEach(function (it) { validKeys[String(it[0])] = true; });
                                                                            var prevSel = this.selected;
                                                                            Object.keys(prevSel).forEach(function (k) {
                                                                                if (validKeys[k]) newSelected[k] = true;
                                                                            });
                                                                        }
                                                                        this.selected = newSelected;
                                                                        var html = '';
                                                                        if (!items.length) {
                                                                            html = '<div class="ms-empty">Nenhuma opção disponível</div>';
                                                                        } else {
                                                                            var self = this;
                                                                            items.forEach(function (item) {
                                                                                var val = String(item[0]);
                                                                                var isSel = !!self.selected[val];
                                                                                html += '<div class="ms-option' + (isSel ? ' selected' : '') + '" data-value="' + val + '" data-search="' + (item[1] || item[0]).toLowerCase() + '">' +
                                                                                    '<span class="ms-checkbox">✔</span>' +
                                                                                    '<span class="ms-option-label">' + (item[1] || item[0]) + '</span>' +
                                                                                    '</div>';
                                                                            });
                                                                        }
                                                                        this.listEl.innerHTML = html;
                                                                        this._updateTrigger();
                                                                    };

                                                                    MultiSelect.prototype.toggle = function () {
                                                                        if (this._open) this.close(); else this.open();
                                                                    };

                                                                    MultiSelect.prototype.open = function () {
                                                                        var openDds = document.querySelectorAll('.ms-dropdown.show');
                                                                        for (var d = 0; d < openDds.length; d++) {
                                                                            openDds[d].classList.remove('show');
                                                                            if (openDds[d].parentNode && openDds[d].parentNode.querySelector('.ms-trigger')) {
                                                                                openDds[d].parentNode.querySelector('.ms-trigger').classList.remove('open');
                                                                            }
                                                                        }
                                                                        this._open = true;
                                                                        this.dropdownEl.classList.add('show');
                                                                        this.triggerEl.classList.add('open');
                                                                        this.searchInput.value = '';
                                                                        this._filter('');
                                                                        this.searchInput.focus();
                                                                    };

                                                                    MultiSelect.prototype.close = function () {
                                                                        this._open = false;
                                                                        this.dropdownEl.classList.remove('show');
                                                                        this.triggerEl.classList.remove('open');
                                                                    };

                                                                    MultiSelect.prototype.selectAll = function () {
                                                                        var self = this;
                                                                        // Only select visible items (not filtered out)
                                                                        var opts = this.listEl.querySelectorAll('.ms-option:not(.hidden)');
                                                                        for (var i = 0; i < opts.length; i++) {
                                                                            self.selected[opts[i].getAttribute('data-value')] = true;
                                                                            opts[i].classList.add('selected');
                                                                        }
                                                                        this._updateTrigger();
                                                                        this.onChange();
                                                                    };

                                                                    MultiSelect.prototype.clearAll = function () {
                                                                        this.selected = {};
                                                                        var opts = this.listEl.querySelectorAll('.ms-option');
                                                                        for (var i = 0; i < opts.length; i++) opts[i].classList.remove('selected');
                                                                        this._updateTrigger();
                                                                        this.onChange();
                                                                    };

                                                                    MultiSelect.prototype.getSelected = function () {
                                                                        return Object.keys(this.selected);
                                                                    };

                                                                    MultiSelect.prototype._filter = function (term) {
                                                                        var t = (term || '').toLowerCase();
                                                                        var opts = this.listEl.querySelectorAll('.ms-option');
                                                                        var anyVisible = false;
                                                                        for (var i = 0; i < opts.length; i++) {
                                                                            var match = !t || opts[i].getAttribute('data-search').indexOf(t) >= 0;
                                                                            if (match) { opts[i].classList.remove('hidden'); anyVisible = true; }
                                                                            else opts[i].classList.add('hidden');
                                                                        }
                                                                        // Show/hide empty msg
                                                                        var emptyEl = this.listEl.querySelector('.ms-empty');
                                                                        if (!anyVisible && !emptyEl) {
                                                                            var d = document.createElement('div');
                                                                            d.className = 'ms-empty';
                                                                            d.textContent = 'Nenhum resultado';
                                                                            this.listEl.appendChild(d);
                                                                        } else if (anyVisible && emptyEl) {
                                                                            emptyEl.remove();
                                                                        }
                                                                    };

                                                                    MultiSelect.prototype._updateTrigger = function () {
                                                                        var count = Object.keys(this.selected).length;
                                                                        if (count === 0) {
                                                                            this.labelEl.textContent = this.placeholder;
                                                                            this.badgeEl.style.display = 'none';
                                                                        } else if (count === this.items.length && this.items.length > 0) {
                                                                            this.labelEl.textContent = 'Todos';
                                                                            this.badgeEl.textContent = count;
                                                                            this.badgeEl.style.display = '';
                                                                        } else {
                                                                            // Show first selected label + count
                                                                            var first = '';
                                                                            for (var i = 0; i < this.items.length; i++) {
                                                                                if (this.selected[this.items[i][0]]) { first = this.items[i][1] || this.items[i][0]; break; }
                                                                            }
                                                                            this.labelEl.textContent = count === 1 ? first : first + ' (+' + (count - 1) + ')';
                                                                            this.badgeEl.textContent = count;
                                                                            this.badgeEl.style.display = '';
                                                                        }
                                                                    };

                                                                    // ─── Utilitários de filtro ────────────────────────────────
                                                                    var EMPRESAS_EXCLUIDAS = ['999', '103', '777', '666', '555', '108', '109'];

                                                                    function distinct(campoCod, campoNome, filtroFn) {
                                                                        var m = {};
                                                                        RAW.forEach(function (r) {
                                                                            if (EMPRESAS_EXCLUIDAS.indexOf(String(r.CODEMP)) >= 0) return;
                                                                            if (filtroFn && !filtroFn(r)) return;
                                                                            if (r[campoCod] !== '' && r[campoCod] != null) {
                                                                                if (campoCod === 'CODEMP' && EMPRESAS_EXCLUIDAS.indexOf(String(r[campoCod])) >= 0) return;
                                                                                m[String(r[campoCod])] = r[campoNome];
                                                                            }
                                                                        });
                                                                        return Object.keys(m).map(function (k) { return [k, m[k]]; })
                                                                            .sort(function (a, b) { return String(a[1] || '').localeCompare(String(b[1] || '')); });
                                                                    }
                                                                    function opts(sel, lista, labelTodos) {
                                                                        var html = labelTodos != null ? '<option value="">' + labelTodos + '</option>' : '';
                                                                        lista.forEach(function (par) { html += '<option value="' + par[0] + '">' + (par[1] || par[0]) + '</option>'; });
                                                                        sel.innerHTML = html;
                                                                    }

                                                                    function atualizarFiltrosCascata() {
                                                                        var selectedEmps = msEmp ? msEmp.getSelected() : [];
                                                                        var empFilter = function (r) {
                                                                            if (!selectedEmps.length) return true;
                                                                            return selectedEmps.indexOf(String(r.CODEMP)) >= 0;
                                                                        };

                                                                        if (msCc) {
                                                                            msCc.setItems(distinct('CODCENCUS', 'CC_NOME', empFilter), true);
                                                                        }
                                                                        if (msProj) {
                                                                            msProj.setItems(distinct('CODPROJ', 'PROJETO_NOME', empFilter), true);
                                                                        }
                                                                        if (fResp) {
                                                                            var curVal = fResp.value;
                                                                            opts(fResp, distinct('CODRESP', 'RESP_NOME', empFilter), 'Todos');
                                                                            fResp.value = curVal;
                                                                        }
                                                                    }

                                                                    function popularFiltros() {
                                                                        var onChangeMs = function () { if (carregado) render(); };

                                                                        el('f-emp').innerHTML = '';
                                                                        msEmp = new MultiSelect(el('f-emp'), 'Selecione...', function () {
                                                                            atualizarFiltrosCascata();
                                                                            if (carregado) render();
                                                                        });
                                                                        msEmp.setItems(distinct('CODEMP', 'EMPRESA_NOME'));

                                                                        el('f-cc').innerHTML = '';
                                                                        msCc = new MultiSelect(el('f-cc'), 'Selecione...', onChangeMs);
                                                                        msCc.setItems(distinct('CODCENCUS', 'CC_NOME'));

                                                                        el('f-proj').innerHTML = '';
                                                                        msProj = new MultiSelect(el('f-proj'), 'Selecione...', onChangeMs);
                                                                        msProj.setItems(distinct('CODPROJ', 'PROJETO_NOME'));

                                                                        el('f-nat').innerHTML = '';
                                                                        msNat = new MultiSelect(el('f-nat'), 'Selecione...', onChangeMs);
                                                                        msNat.setItems(distinct('CODNAT', 'NATUREZA_NOME'));

                                                                        opts(fResp, distinct('CODRESP', 'RESP_NOME'), 'Todos');

                                                                        // Origem: apenas origens de realizado (metas ficam no filtro Cenário)
                                                                        var origens = {};
                                                                        RAW.forEach(function (r) { if (r.TIPO && !ORIG_META[r.TIPO]) origens[r.TIPO] = 1; });
                                                                        var origHtml = '<option value="">Todas</option>';
                                                                        Object.keys(origens).sort().forEach(function (t) { origHtml += '<option value="' + t + '">' + t + '</option>'; });
                                                                        fOrig.innerHTML = origHtml;
                                                                    }

                                                                    function filtrar() {
                                                                        var de = toMes(fDtDe.value), ate = toMes(fDtAte.value);
                                                                        var emps = msEmp ? msEmp.getSelected() : [];
                                                                        var ccs = msCc ? msCc.getSelected() : [];
                                                                        var projs = msProj ? msProj.getSelected() : [];
                                                                        var nats = msNat ? msNat.getSelected() : [];
                                                                        var cenario = fCenario.value;
                                                                        return RAW.filter(function (r) {
                                                                            if (EMPRESAS_EXCLUIDAS.indexOf(String(r.CODEMP)) >= 0) return false;
                                                                            if (de && r.MES_ANO < de) return false;
                                                                            if (ate && r.MES_ANO > ate) return false;
                                                                            if (emps.length && emps.indexOf(String(r.CODEMP)) < 0) return false;
                                                                            if (ccs.length && ccs.indexOf(String(r.CODCENCUS)) < 0) return false;
                                                                            if (projs.length && projs.indexOf(String(r.CODPROJ)) < 0) return false;
                                                                            if (nats.length && nats.indexOf(String(r.CODNAT)) < 0) return false;
                                                                            if (fResp.value && String(r.CODRESP) !== fResp.value) return false;
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
                                                                        ['hero-d', 'hero-r'].forEach(function (h) {
                                                                            el(h + '-pct').textContent = '—';
                                                                            el(h + '-pct').className = 'pct';
                                                                            el(h + '-bar').style.width = '0%';
                                                                            el(h + '-bar').textContent = '';
                                                                        });
                                                                        el('hero-d-legenda').innerHTML = 'Preencha <b>Data inicial</b> e <b>Data final</b> e clique em <b>Carregar</b>.';
                                                                        el('hero-r-legenda').innerHTML = '&nbsp;';
                                                                        el('kpis').innerHTML = '';
                                                                        el('resumo-body').innerHTML = '<tr><td colspan="8" class="empty">Preencha o período e clique em Carregar.</td></tr>';
                                                                        el('resumo-info').textContent = '';
                                                                        el('tbody').innerHTML = '<tr><td colspan="5" class="empty">Preencha o período e clique em Carregar.</td></tr>';
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
                                                                        var saldo = totOrc - totReal;

                                                                        // ── Dois cards de consumo: Despesa e Receita (independentes da Visão) ──
                                                                        var dOrc = 0, dReal = 0, rOrc = 0, rReal = 0;
                                                                        linhas.forEach(function (r) {
                                                                            var orcTot = r.orcRec + r.orcDesp, realTot = r.realRec + r.realDesp;
                                                                            if (isReceita(r)) { rOrc += orcTot; rReal += realTot; }
                                                                            else { dOrc += orcTot; dReal += realTot; }
                                                                        });
                                                                        // Despesa: consumo — passar de 100% é ruim (vermelho); saldo = sobra do orçado
                                                                        var dPct = dOrc !== 0 ? (dReal / dOrc) * 100 : 0;
                                                                        var dSaldo = dOrc - dReal;
                                                                        el('hero-d-pct').textContent = fmtPct(dPct);
                                                                        el('hero-d-pct').className = 'pct ' + classeConsumo(dPct);
                                                                        el('hero-d-legenda').innerHTML =
                                                                            'Comprometido <b>' + fmtBRL(dReal) + '</b> de <b>' + fmtBRL(dOrc) + '</b> orçados' +
                                                                            ' &bull; Saldo <b style="color:' + (dSaldo >= 0 ? '#bbf7d0' : '#fecaca') + '">' + fmtBRL(dSaldo) + '</b>';
                                                                        var dBar = el('hero-d-bar');
                                                                        dBar.style.width = Math.min(dPct, 100) + '%';
                                                                        dBar.style.background = corConsumo(dPct);
                                                                        dBar.textContent = dPct > 8 ? fmtPct(dPct) : '';
                                                                        // Receita: realização — passar de 100% é BOM (verde); superávit = realizado - orçado
                                                                        var rPct = rOrc !== 0 ? (rReal / rOrc) * 100 : 0;
                                                                        var rSuper = rReal - rOrc;
                                                                        el('hero-r-pct').textContent = fmtPct(rPct);
                                                                        el('hero-r-pct').className = 'pct ' + classeReceita(rPct);
                                                                        el('hero-r-legenda').innerHTML =
                                                                            'Realizado <b>' + fmtBRL(rReal) + '</b> de <b>' + fmtBRL(rOrc) + '</b> orçados' +
                                                                            ' &bull; ' + (rSuper >= 0 ? 'Superávit' : 'Défice') + ' <b style="color:' + (rSuper >= 0 ? '#bbf7d0' : '#fecaca') + '">' + fmtBRL(rSuper) + '</b>';
                                                                        var rBar = el('hero-r-bar');
                                                                        rBar.style.width = Math.min(rPct, 100) + '%';
                                                                        rBar.style.background = corReceita(rPct);
                                                                        rBar.textContent = rPct > 8 ? fmtPct(rPct) : '';

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
                                                                            kpi(visReceita ? 'Realizado (YTD)' : 'Comprometido (YTD)', fmtBRL(realYTD), fmtPct(atingYTD) + ' do orçado YTD', visReceita ? classeReceita(atingYTD) : classeConsumo(atingYTD)) +
                                                                            kpi(visReceita ? 'Superávit de receita' : 'Saldo orçamentário', fmtBRL(visReceita ? (totReal - totOrc) : saldo), (visReceita ? (totReal - totOrc) : saldo) >= 0 ? (visReceita ? 'Acima da meta' : 'Dentro do orçado') : (visReceita ? 'Abaixo da meta' : 'Estourado'), (visReceita ? (totReal - totOrc) : saldo) >= 0 ? 'ok' : 'danger') +
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
                                                                        if (typeof Chart === 'undefined') return;
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
                                                                        if (typeof Chart === 'undefined') return;
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
                                                                        if (typeof Chart === 'undefined') return;
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
                                                                        if (typeof Chart === 'undefined') return;
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

                                                                    // ─── Tabela DRE hierárquica ───────────────────────────────
                                                                    function renderTabela(linhas) {
                                                                        var arvore = window.ARVORE_NAT || [];
                                                                        var tbody = el('tbody');

                                                                        // 1. Construir mapa de nós
                                                                        var natMap = {};
                                                                        var rootNodes = [];
                                                                        arvore.forEach(function (n) {
                                                                            var id = String(n.CODNAT);
                                                                            natMap[id] = {
                                                                                id: id,
                                                                                name: (n.DESCRNAT || '').trim(),
                                                                                parentId: String(n.CODNATPAI),
                                                                                tipnat: (n.TIPNAT || '').trim(),
                                                                                isAnalitica: n.ANALITICA === 'S',
                                                                                nivel: parseInt(n.NIVEL, 10) || 1,
                                                                                children: [],
                                                                                orc: 0,
                                                                                real: 0,
                                                                                detalhes: {}
                                                                            };
                                                                        });

                                                                        // 2. Montar árvore (vincular filhos aos pais)
                                                                        Object.keys(natMap).forEach(function (id) {
                                                                            var node = natMap[id];
                                                                            var parent = natMap[node.parentId];
                                                                            if (parent) {
                                                                                parent.children.push(node);
                                                                            } else {
                                                                                rootNodes.push(node);
                                                                            }
                                                                        });

                                                                        // 3. Distribuir dados filtrados nos nós analíticos
                                                                        linhas.forEach(function (r) {
                                                                            var v = valores(r);
                                                                            var kn = String(r.CODNAT);
                                                                            var node = natMap[kn];
                                                                            if (!node) return;
                                                                            node.orc += v.orc;
                                                                            node.real += v.real;
                                                                            // Agregar detalhes por Empresa·Projeto·CR·Resp
                                                                            var kf = r.CODEMP + '|' + r.CODPROJ + '|' + r.CODCENCUS + '|' + r.CODRESP;
                                                                            if (!node.detalhes[kf]) node.detalhes[kf] = {
                                                                                emp: r.EMPRESA_NOME, proj: r.PROJETO_NOME, cr: r.CC_NOME, resp: r.RESP_NOME,
                                                                                codemp: r.CODEMP, codproj: r.CODPROJ, codcencus: r.CODCENCUS,
                                                                                orc: 0, real: 0
                                                                            };
                                                                            node.detalhes[kf].orc += v.orc;
                                                                            node.detalhes[kf].real += v.real;
                                                                        });

                                                                        // 4. Rollup: propagar valores das folhas para os pais
                                                                        function rollup(node) {
                                                                            node.children.forEach(function (child) {
                                                                                rollup(child);
                                                                                node.orc += child.orc;
                                                                                node.real += child.real;
                                                                            });
                                                                        }
                                                                        rootNodes.forEach(rollup);

                                                                        // 5. Filtrar nós sem dados (recursivo)
                                                                        function hasData(node) {
                                                                            if (node.orc !== 0 || node.real !== 0) return true;
                                                                            return node.children.some(hasData);
                                                                        }

                                                                        // 6. Renderizar
                                                                        var html = '', totOrc = 0, totReal = 0, nodeCount = 0;

                                                                        function renderNode(node, parentId) {
                                                                            if (!hasData(node)) return;
                                                                            nodeCount++;
                                                                            var hasKids = node.children.some(hasData);
                                                                            var hasDetalhes = false;
                                                                            var detArr = [];
                                                                            if (node.isAnalitica) {
                                                                                detArr = Object.keys(node.detalhes).map(function (k) { return node.detalhes[k]; })
                                                                                    .filter(function (f) { return f.orc !== 0 || f.real !== 0; })
                                                                                    .sort(function (a, b) { return b.orc - a.orc; });
                                                                                hasDetalhes = detArr.length > 0;
                                                                            }
                                                                            var expandable = hasKids || hasDetalhes;
                                                                            var nivel = node.nivel;
                                                                            var indent = (nivel - 1) * 20 + 8;
                                                                            var saldo = node.orc - node.real;
                                                                            var pct = node.orc !== 0 ? (node.real / node.orc) * 100 : 0;
                                                                            var isRec = node.tipnat === 'R' || (node.name && node.name.toUpperCase().indexOf('RECEITA') >= 0);
                                                                            var lvlClass = nivel <= 5 ? 'lvl-' + nivel : 'lvl-deep';
                                                                            var rowClass = node.isAnalitica ? 'row-leaf' : 'row-node';
                                                                            var pAttr = parentId ? ' data-parent="' + parentId + '"' : '';
                                                                            var hiddenCls = parentId ? ' hidden' : '';

                                                                            html +=
                                                                                '<tr class="' + rowClass + ' ' + lvlClass + ' collapsed' + hiddenCls + '" data-node="' + node.id + '"' + pAttr + '>' +
                                                                                '<td style="padding-left:' + indent + 'px;">' +
                                                                                (expandable ? '<span class="chevron">&#9656;</span> ' : '<span style="display:inline-block;width:14px;"></span> ') +
                                                                                node.name +
                                                                                '</td>' +
                                                                                '<td>' + fmtBRL(node.orc) + '</td>' +
                                                                                '<td>' + fmtBRL(node.real) + '</td>' +
                                                                                '<td class="' + (saldo >= 0 ? 'val-pos' : 'val-neg') + '">' + fmtBRL(saldo) + '</td>' +
                                                                                '<td><span class="' + (isRec ? classeReceita(pct) : classeConsumo(pct)) + '" style="font-weight:700;">' + fmtPct(pct) + '</span></td>' +
                                                                                '</tr>';

                                                                            // Detalhes da folha analítica
                                                                            if (hasDetalhes) {
                                                                                detArr.forEach(function (f) {
                                                                                    var s2 = f.orc - f.real;
                                                                                    var p2 = f.orc !== 0 ? (f.real / f.orc) * 100 : 0;
                                                                                    var det = f.emp + ' \u00b7 ' + f.proj + ' \u00b7 ' + f.cr + ' \u00b7 Resp: ' + (f.resp || '\u2014');
                                                                                    html +=
                                                                                        '<tr class="row-det hidden" data-parent="' + node.id + '"' +
                                                                                        ' data-codemp="' + (f.codemp != null ? f.codemp : '') + '"' +
                                                                                        ' data-codproj="' + (f.codproj != null ? f.codproj : '') + '"' +
                                                                                        ' data-codcencus="' + (f.codcencus != null ? f.codcencus : '') + '">' +
                                                                                        '<td class="det" style="padding-left:' + (indent + 20) + 'px;">' + det + '</td>' +
                                                                                        '<td>' + fmtBRL(f.orc) + '</td>' +
                                                                                        '<td>' + fmtBRL(f.real) + '</td>' +
                                                                                        '<td class="' + (s2 >= 0 ? 'val-pos' : 'val-neg') + '">' + fmtBRL(s2) + '</td>' +
                                                                                        '<td><span class="' + (isRec ? classeReceita(p2) : classeConsumo(p2)) + '">' + fmtPct(p2) + '</span></td>' +
                                                                                        '</tr>';
                                                                                });
                                                                            }

                                                                            // Filhos (nós intermediários e sub-naturezas)
                                                                            node.children.forEach(function (child) {
                                                                                renderNode(child, node.id);
                                                                            });
                                                                        }

                                                                        rootNodes.forEach(function (root) {
                                                                            renderNode(root, null);
                                                                            totOrc += root.orc;
                                                                            totReal += root.real;
                                                                        });

                                                                        if (!nodeCount) {
                                                                            tbody.innerHTML = '<tr><td colspan="5" class="empty">Nenhum dado para os filtros selecionados.</td></tr>';
                                                                            el('tab-info').textContent = '';
                                                                            return;
                                                                        }

                                                                        // Total geral
                                                                        var saldoT = totOrc - totReal;
                                                                        var pctT = totOrc !== 0 ? (totReal / totOrc) * 100 : 0;
                                                                        html +=
                                                                            '<tr class="row-total">' +
                                                                            '<td>TOTAL GERAL</td>' +
                                                                            '<td>' + fmtBRL(totOrc) + '</td>' +
                                                                            '<td>' + fmtBRL(totReal) + '</td>' +
                                                                            '<td class="' + (saldoT >= 0 ? 'val-pos' : 'val-neg') + '">' + fmtBRL(saldoT) + '</td>' +
                                                                            '<td>' + fmtPct(pctT) + '</td>' +
                                                                                '</tr>';

                                                                        tbody.innerHTML = html;

                                                                        // Mostrar nós raiz (nível 1) visíveis por padrão
                                                                        var raizes = document.querySelectorAll('#tbody tr[data-node]:not([data-parent])');
                                                                        for (var ri = 0; ri < raizes.length; ri++) {
                                                                            raizes[ri].className = raizes[ri].className.replace(/\s*hidden/g, '');
                                                                        }

                                                                        el('tab-info').textContent = nodeCount + ' naturezas na árvore (clique para expandir/recolher)';
                                                                    }

                                                                    // ─── Modal Detalhes dos Títulos ────────────────────────────
                                                                    var _modalNatId = null, _modalNatNome = null;

                                                                    async function abrirModalTitulos(natId, natNome, codemp, codproj, codcencus) {
                                                                        _modalNatId = natId;
                                                                        _modalNatNome = natNome;

                                                                        // Título do modal
                                                                        var titleEl = el('modal-titulos-title');
                                                                        if (titleEl) titleEl.textContent = 'Lançamentos — ' + (natNome || ('Natureza ' + natId));

                                                                        // Abre o modal mostrando loading
                                                                        var tbody = el('modal-titulos-tbody');
                                                                        if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="padding:30px; text-align:center; color:#5c6b7a; font-size:0.85rem;">⏳ Buscando lançamentos no Sankhya...</td></tr>';
                                                                        el('modal-footer-info').textContent = '';
                                                                        var searchEl = el('modal-titulos-search');
                                                                        if (searchEl) searchEl.value = '';
                                                                        el('modal-titulos-container').style.display = 'flex';

                                                                        var fnQuery = window.queryMitra || (parent && parent.queryMitra);
                                                                        var dtIni = fDtDe ? fDtDe.value : '';
                                                                        var dtFim = fDtAte ? fDtAte.value : '';

                                                                        if (fnQuery && dtIni && dtFim) {
                                                                            try {
                                                                                var sql = _buildSqlTitulos(natId, dtIni, dtFim, codemp, codproj, codcencus);
                                                                                var res = await fnQuery(sql);
                                                                                if (res && res.data && res.data.length > 0) {
                                                                                    _renderTitulosReais(res.columns || res.headers || [], res.data);
                                                                                } else {
                                                                                    _renderFallback(natId, codemp, codproj, codcencus);
                                                                                }
                                                                            } catch (err) {
                                                                                console.warn('queryMitra erro:', err);
                                                                                _renderFallback(natId, codemp, codproj, codcencus);
                                                                            }
                                                                        } else {
                                                                            _renderFallback(natId, codemp, codproj, codcencus);
                                                                        }

                                                                        // Busca em tempo real
                                                                        var searchInput = el('modal-titulos-search');
                                                                        if (searchInput) {
                                                                            searchInput.oninput = function () {
                                                                                var term = this.value.toLowerCase();
                                                                                var trs = el('modal-titulos-tbody').querySelectorAll('tr.modal-tit-row');
                                                                                for (var i = 0; i < trs.length; i++) {
                                                                                    trs[i].style.display = (!term || (trs[i].textContent || '').toLowerCase().indexOf(term) >= 0) ? '' : 'none';
                                                                                }
                                                                            };
                                                                        }
                                                                    }

                                                                    // ─── SQL para buscar lançamentos individuais via queryMitra (baseado no ORCAMENTARIO.SQL) ───
                                                                    function _buildSqlTitulos(natId, dtIni, dtFim, codemp, codproj, codcencus) {
                                                                        var natF = 'T.CODNAT IN (SELECT DISTINCT N2.CODNAT FROM TGFNAT N2 START WITH N2.CODNAT=' + natId + ' CONNECT BY PRIOR N2.CODNAT = N2.CODNATPAI)';
                                                                        var dtBaixa = "TRUNC(T.DHBAIXA,'MM') BETWEEN TRUNC(TO_DATE('" + dtIni + "','YYYY-MM-DD'),'MM') AND TRUNC(TO_DATE('" + dtFim + "','YYYY-MM-DD'),'MM')";
                                                                        var dtNeg   = "TRUNC(T.DTNEG,'MM') BETWEEN TRUNC(TO_DATE('" + dtIni + "','YYYY-MM-DD'),'MM') AND TRUNC(TO_DATE('" + dtFim + "','YYYY-MM-DD'),'MM')";
                                                                        var dtRef   = "TRUNC(T.REFERENCIA,'MM') BETWEEN TRUNC(TO_DATE('" + dtIni + "','YYYY-MM-DD'),'MM') AND TRUNC(TO_DATE('" + dtFim + "','YYYY-MM-DD'),'MM')";

                                                                        var fEmp = (codemp != null && String(codemp) !== '' && String(codemp) !== 'null') ? ' AND T.CODEMP=' + codemp : ' AND (T.CODEMP IS NULL OR T.CODEMP NOT IN (999,103,777,666,555,108,109))';
                                                                        var fPrj = (codproj != null && String(codproj) !== '' && String(codproj) !== 'null') ? ' AND T.CODPROJ=' + codproj : '';
                                                                        var fCR  = (codcencus != null && String(codcencus) !== '' && String(codcencus) !== 'null') ? ' AND T.CODCENCUS=' + codcencus : '';

                                                                        var joins = ' LEFT JOIN TSIEMP E ON E.CODEMP=T.CODEMP LEFT JOIN TCSPRJ P ON P.CODPROJ=T.CODPROJ LEFT JOIN TSICUS C ON C.CODCENCUS=T.CODCENCUS LEFT JOIN TGFNAT N ON N.CODNAT=T.CODNAT';
                                                                        var cols  = " NVL(E.NOMEFANTASIA,TO_CHAR(T.CODEMP)) AS EMPRESA, NVL(P.IDENTIFICACAO,'\u2014') AS PROJETO, NVL(C.DESCRCENCUS,'\u2014') AS CR, N.DESCRNAT AS NATUREZA";

                                                                        return (
                                                                            // BLOCO C – Baixas Financeiras (TGFFIN)
                                                                            "SELECT 'C-Fin.Baixa' AS ORIGEM, " +
                                                                            "CASE WHEN T.NUMNOTA IS NOT NULL AND T.NUMNOTA <> 0 THEN TO_CHAR(T.NUMNOTA) ELSE TO_CHAR(T.NUFIN) END AS NR_DOC, " +
                                                                            "TO_CHAR(T.DHBAIXA,'DD/MM/YYYY') AS DATA_MOV, " +
                                                                            "CASE WHEN T.RECDESP=1 THEN T.VLRBAIXA ELSE -T.VLRBAIXA END AS VALOR, " + cols +
                                                                            " FROM TGFFIN T" + joins +
                                                                            " WHERE T.PROVISAO='N' AND T.DHBAIXA IS NOT NULL AND T.CODEMP NOT IN (3,4,5,999,103,777,666,555,108,109)" +
                                                                            " AND " + natF + " AND " + dtBaixa + fEmp + fPrj + fCR +

                                                                            " UNION ALL " +
                                                                            // BLOCO D – Competência Financeira (TGFFIN)
                                                                            "SELECT 'D-Fin.Competência' AS ORIGEM, " +
                                                                            "CASE WHEN T.NUMNOTA IS NOT NULL AND T.NUMNOTA <> 0 THEN TO_CHAR(T.NUMNOTA) ELSE TO_CHAR(T.NUFIN) END AS NR_DOC, " +
                                                                            "TO_CHAR(T.DTNEG,'DD/MM/YYYY') AS DATA_MOV, " +
                                                                            "CASE WHEN T.RECDESP=1 THEN T.VLRDESDOB ELSE -T.VLRDESDOB END AS VALOR, " + cols +
                                                                            " FROM TGFFIN T" + joins +
                                                                            " WHERE T.PROVISAO='N' AND T.CODEMP NOT IN (3,4,5,999,103,777,666,555,108,109)" +
                                                                            " AND " + natF + " AND " + dtNeg + fEmp + fPrj + fCR +

                                                                            " UNION ALL " +
                                                                            // BLOCO F – Portal Receita (TGFCAB)
                                                                            "SELECT 'F-Portal Receita' AS ORIGEM, " +
                                                                            "NVL(TO_CHAR(T.NUMNOTA), TO_CHAR(T.NUNOTA)) AS NR_DOC, " +
                                                                            "TO_CHAR(T.DTNEG,'DD/MM/YYYY') AS DATA_MOV, " +
                                                                            "T.VLRNOTA AS VALOR, " + cols +
                                                                            " FROM TGFCAB T" + joins +
                                                                            " WHERE T.CODTIPOPER IN (1128) AND T.CODEMP IN (1,2)" +
                                                                            " AND " + natF + " AND " + dtNeg + fEmp + fPrj + fCR +

                                                                            " UNION ALL " +
                                                                            // BLOCO H – Portal Mercadoria (TGFCAB)
                                                                            "SELECT 'H-Portal Merc.' AS ORIGEM, " +
                                                                            "NVL(TO_CHAR(T.NUMNOTA), TO_CHAR(T.NUNOTA)) AS NR_DOC, " +
                                                                            "TO_CHAR(T.DTNEG,'DD/MM/YYYY') AS DATA_MOV, " +
                                                                            "T.VLRNOTA AS VALOR, " + cols +
                                                                            " FROM TGFCAB T" + joins +
                                                                            " WHERE T.CODTIPOPER IN (1132,1133,1134,1135,1137,1140,1717,1714)" +
                                                                            " AND (T.CODEMP NOT IN (3,4,5,999,103,777,666,555,108,109) OR T.CODPARC<>8)" +
                                                                            " AND " + natF + " AND " + dtNeg + fEmp + fPrj + fCR +

                                                                            " UNION ALL " +
                                                                            // BLOCO I – Portal Geral (TGFCAB)
                                                                            "SELECT 'I-Portal Geral' AS ORIGEM, " +
                                                                            "NVL(TO_CHAR(T.NUMNOTA), TO_CHAR(T.NUNOTA)) AS NR_DOC, " +
                                                                            "TO_CHAR(T.DTNEG,'DD/MM/YYYY') AS DATA_MOV, " +
                                                                            "CASE WHEN T.TIPMOV='D' THEN -T.VLRNOTA ELSE T.VLRNOTA END AS VALOR, " + cols +
                                                                            " FROM TGFCAB T" + joins +
                                                                            " WHERE T.TIPMOV IN ('V','C','D')" +
                                                                            " AND T.CODTIPOPER NOT IN (1128,1132,1133,1134,1135,1137,1140,1714,1717)" +
                                                                            " AND (T.CODEMP NOT IN (3,4,5,999,103,777,666,555,108,109) OR T.CODPARC<>8)" +
                                                                            " AND " + natF + " AND " + dtNeg + fEmp + fPrj + fCR +

                                                                            " UNION ALL " +
                                                                            // BLOCO G – Contabilidade (TCBLAN) - NUMDOC é o número do documento contábil
                                                                            "SELECT 'G-Contábil' AS ORIGEM, " +
                                                                            "NVL(TO_CHAR(T.NUMDOC), TO_CHAR(T.NULANC)) AS NR_DOC, " +
                                                                            "TO_CHAR(T.REFERENCIA,'DD/MM/YYYY') AS DATA_MOV, " +
                                                                            "T.VLRLANC AS VALOR, " +
                                                                            "NVL(E.NOMEFANTASIA,TO_CHAR(T.CODEMP)) AS EMPRESA, NVL(P.IDENTIFICACAO,'\u2014') AS PROJETO, NVL(C.DESCRCENCUS,'\u2014') AS CR, N.DESCRNAT AS NATUREZA " +
                                                                            " FROM TCBLAN T" +
                                                                            " LEFT JOIN TGFNAT N ON N.CODCTACTB=T.CODCTACTB" +
                                                                            " LEFT JOIN TSIEMP E ON E.CODEMP=T.CODEMP" +
                                                                            " LEFT JOIN TCSPRJ P ON P.CODPROJ=T.CODPROJ" +
                                                                            " LEFT JOIN TSICUS C ON C.CODCENCUS=T.CODCENCUS" +
                                                                            " WHERE N.ANALITICA='S'" +
                                                                            " AND N.CODNAT IN (SELECT DISTINCT N2.CODNAT FROM TGFNAT N2 START WITH N2.CODNAT=" + natId + " CONNECT BY PRIOR N2.CODNAT = N2.CODNATPAI)" +
                                                                            " AND " + dtRef +
                                                                            fEmp + fPrj + fCR +

                                                                            " ORDER BY DATA_MOV DESC, NR_DOC DESC"
                                                                        );
                                                                    }

                                                                    // Fallback: filtra dados DADOS_FORECAST por natureza + empresa/projeto/CR
                                                                    function _renderFallback(natId, codemp, codproj, codcencus) {
                                                                        var linhas = filtrar();
                                                                        var filtradas = linhas.filter(function (r) {
                                                                            var matchNat = String(r.CODNAT) === String(natId);
                                                                            var matchEmp = (codemp == null || String(codemp) === '' || String(r.CODEMP) === String(codemp));
                                                                            var matchPrj = (codproj == null || String(codproj) === '' || String(r.CODPROJ) === String(codproj));
                                                                            var matchCR  = (codcencus == null || String(codcencus) === '' || String(r.CODCENCUS) === String(codcencus));
                                                                            return matchNat && matchEmp && matchPrj && matchCR;
                                                                        });
                                                                        var tbody = el('modal-titulos-tbody');
                                                                        if (!tbody) return;
                                                                        if (!filtradas.length) {
                                                                            tbody.innerHTML = '<tr><td colspan="8" style="padding:20px; text-align:center; color:#94a3b8;">Nenhum lançamento encontrado. Verifique os filtros aplicados.</td></tr>';
                                                                            el('modal-footer-info').textContent = '';
                                                                            return;
                                                                        }
                                                                        var html = '';
                                                                        filtradas.forEach(function (r) {
                                                                            var v = valores(r);
                                                                            var val = v.real !== 0 ? v.real : v.orc;
                                                                            html += '<tr class="modal-tit-row" style="border-bottom:1px solid #e2e8f0;">' +
                                                                                '<td style="padding:7px 10px;"><span class="badge-tipo badge-D">' + (r.TIPO || '—') + '</span></td>' +
                                                                                '<td style="padding:7px 10px; color:#9aa7b4;">—</td>' +
                                                                                '<td style="padding:7px 10px;">' + (r.MES_ANO || '—') + '</td>' +
                                                                                '<td style="padding:7px 10px; text-align:right; font-weight:600;">' + fmtBRL(val) + '</td>' +
                                                                                '<td style="padding:7px 10px;">' + (r.EMPRESA_NOME || '—') + '</td>' +
                                                                                '<td style="padding:7px 10px;">' + (r.PROJETO_NOME || '—') + '</td>' +
                                                                                '<td style="padding:7px 10px;">' + (r.CC_NOME || '—') + '</td>' +
                                                                                '<td style="padding:7px 10px;">' + (r.NATUREZA_NOME || '—') + '</td>' +
                                                                                '</tr>';
                                                                        });
                                                                        tbody.innerHTML = html;
                                                                        el('modal-footer-info').textContent = filtradas.length + ' registros (dados agregados — queryMitra indisponível)';
                                                                    }



                                                                    // Renderiza camada de títulos com dados reais do queryMitra
                                                                    function _renderTitulosReais(columns, data, natNome) {
                                                                        var tbody = el('modal-titulos-tbody');
                                                                        if (!tbody) return;

                                                                        if (!data || !data.length) {
                                                                            tbody.innerHTML = '<tr><td colspan="8" style="padding:20px; text-align:center; color:#94a3b8;">Nenhum título encontrado para esta natureza no período.</td></tr>';
                                                                            el('modal-footer-info').textContent = '';
                                                                            return;
                                                                        }

                                                                        // columns: ['ORIGEM','NR_DOC','DATA_MOV','VALOR','EMPRESA','PROJETO','CENTRO_RESULTADO','NATUREZA']
                                                                        var html = '';
                                                                        data.forEach(function (row) {
                                                                            var origem = row[0] || '—';
                                                                            var nrDoc  = row[1] || '—';
                                                                            var data_  = row[2] || '—';
                                                                            var valor  = parseFloat(row[3]) || 0;
                                                                            var emp    = row[4] || '—';
                                                                            var proj   = row[5] || '—';
                                                                            var cr     = row[6] || '—';
                                                                            var nat    = row[7] || '—';
                                                                            html += '<tr class="modal-tit-row" style="border-bottom:1px solid #e2e8f0;">' +
                                                                                '<td style="padding:7px 10px;"><span class="badge-tipo badge-D">' + origem + '</span></td>' +
                                                                                '<td style="padding:7px 10px; font-weight:600;">' + nrDoc + '</td>' +
                                                                                '<td style="padding:7px 10px;">' + data_ + '</td>' +
                                                                                '<td style="padding:7px 10px; text-align:right; font-weight:600;">' + fmtBRL(valor) + '</td>' +
                                                                                '<td style="padding:7px 10px;">' + emp + '</td>' +
                                                                                '<td style="padding:7px 10px;">' + proj + '</td>' +
                                                                                '<td style="padding:7px 10px;">' + cr + '</td>' +
                                                                                '<td style="padding:7px 10px;">' + nat + '</td>' +
                                                                                '</tr>';
                                                                        });

                                                                        tbody.innerHTML = html;
                                                                        el('modal-footer-info').textContent = data.length + ' títulos encontrados';

                                                                        // Busca na camada de títulos
                                                                        var searchInput = el('modal-titulos-search');
                                                                        if (searchInput) {
                                                                            searchInput.value = '';
                                                                            searchInput.oninput = function () {
                                                                                var term = this.value.toLowerCase();
                                                                                var trs = tbody.querySelectorAll('tr.modal-tit-row');
                                                                                for (var i = 0; i < trs.length; i++) {
                                                                                    trs[i].style.display = (!term || (trs[i].textContent || '').toLowerCase().indexOf(term) >= 0) ? '' : 'none';
                                                                                }
                                                                            };
                                                                        }
                                                                    }

                                                                    function fecharModalHtml() {
                                                                        var container = el('modal-titulos-container');
                                                                        if (container) container.style.display = 'none';
                                                                        // Resetar estado
                                                                        _modalNatId = null; _modalNatNome = null;
                                                                        var si = el('modal-titulos-search'); if (si) si.value = '';
                                                                    }



                                                                    // Botão Exportar XLSX do modal
                                                                    if (el('modal-titulos-export')) {
                                                                        el('modal-titulos-export').addEventListener('click', function () {
                                                                            try {
                                                                                if (typeof XLSX === 'undefined') { alert('Biblioteca XLSX não carregada.'); return; }
                                                                                // Exportar a tabela visível
                                                                                var layerTitulos = el('modal-layer-titulos');
                                                                                var isLayerTitulos = layerTitulos && layerTitulos.style.display !== 'none';
                                                                                var tableId = isLayerTitulos ? 'modal-titulos-table' : 'modal-resumo-table';
                                                                                var tbl = el(tableId);
                                                                                if (!tbl) return;
                                                                                var wb = XLSX.utils.book_new();
                                                                                var ws = XLSX.utils.table_to_sheet(tbl);
                                                                                XLSX.utils.book_append_sheet(wb, ws, isLayerTitulos ? 'Lançamentos' : 'Resumo');
                                                                                XLSX.writeFile(wb, 'OPET_Titulos_' + (_modalNatNome || 'Natureza').replace(/[^a-zA-Z0-9]/g, '_') + '.xlsx');
                                                                            } catch(e) { console.error(e); }
                                                                        });
                                                                    }

                                                                    // Fechar modal HTML
                                                                    if (el('modal-titulos-close')) el('modal-titulos-close').addEventListener('click', fecharModalHtml);
                                                                    if (el('modal-titulos-btn-close')) el('modal-titulos-btn-close').addEventListener('click', fecharModalHtml);
                                                                    if (el('modal-titulos-container')) {
                                                                        el('modal-titulos-container').addEventListener('click', function (ev) {
                                                                            if (ev.target === this) fecharModalHtml();
                                                                        });
                                                                    }

                                                                    // Expandir/recolher nós da árvore e clique para abrir modal
                                                                    el('tbody').addEventListener('click', function (ev) {
                                                                        var tr = ev.target;
                                                                        while (tr && tr.tagName !== 'TR') tr = tr.parentNode;
                                                                        if (!tr) return;

                                                                        var isChevron = ev.target.classList.contains('chevron') || (ev.target.parentNode && ev.target.parentNode.classList && ev.target.parentNode.classList.contains('chevron'));
                                                                        var isLeaf = tr.className.indexOf('row-leaf') >= 0;
                                                                        var isDet = tr.className.indexOf('row-det') >= 0;

                                                                        // Se for linha analítica ou detalhe e NÃO clicou na setinha, abre o modal
                                                                        if ((isLeaf || isDet) && !isChevron) {
                                                                            var natId = tr.getAttribute('data-node') || tr.getAttribute('data-parent');
                                                                            var codemp = isDet ? tr.getAttribute('data-codemp') : null;
                                                                            var codproj = isDet ? tr.getAttribute('data-codproj') : null;
                                                                            var codcencus = isDet ? tr.getAttribute('data-codcencus') : null;
                                                                            var td = tr.querySelector('td');
                                                                            var natNome = td ? td.textContent.trim() : '';
                                                                            if (natId) {
                                                                                abrirModalTitulos(natId, natNome);
                                                                                return;
                                                                            }
                                                                        }

                                                                        var nodeId = tr.getAttribute('data-node');
                                                                        if (!nodeId) return;

                                                                        var colapsar = tr.className.indexOf('collapsed') < 0;
                                                                        if (colapsar) {
                                                                            if (tr.className.indexOf('collapsed') < 0) tr.className += ' collapsed';
                                                                            ocultarDescendentes(nodeId);
                                                                        } else {
                                                                            tr.className = tr.className.replace(/\s*collapsed/g, '');
                                                                            var filhos = document.querySelectorAll('tr[data-parent="' + nodeId + '"]');
                                                                            for (var i = 0; i < filhos.length; i++) {
                                                                                filhos[i].className = filhos[i].className.replace(/\s*hidden/g, '');
                                                                            }
                                                                        }
                                                                    });

                                                                    function ocultarDescendentes(parentId) {
                                                                        var filhos = document.querySelectorAll('tr[data-parent="' + parentId + '"]');
                                                                        for (var i = 0; i < filhos.length; i++) {
                                                                            if (filhos[i].className.indexOf('hidden') < 0) filhos[i].className += ' hidden';
                                                                            if (filhos[i].className.indexOf('collapsed') < 0) filhos[i].className += ' collapsed';
                                                                            var childId = filhos[i].getAttribute('data-node');
                                                                            if (childId) ocultarDescendentes(childId);
                                                                        }
                                                                    }

                                                                    function expandirTodos(expandir) {
                                                                        var todos = document.querySelectorAll('#tbody tr.row-node, #tbody tr.row-leaf');
                                                                        for (var i = 0; i < todos.length; i++) {
                                                                            if (expandir) {
                                                                                todos[i].className = todos[i].className.replace(/\s*collapsed/g, '').replace(/\s*hidden/g, '');
                                                                            } else {
                                                                                if (todos[i].className.indexOf('collapsed') < 0) todos[i].className += ' collapsed';
                                                                                if (todos[i].getAttribute('data-parent') && todos[i].className.indexOf('hidden') < 0) todos[i].className += ' hidden';
                                                                            }
                                                                        }
                                                                        var dets = document.querySelectorAll('#tbody tr.row-det');
                                                                        for (var j = 0; j < dets.length; j++) {
                                                                            if (expandir) dets[j].className = dets[j].className.replace(/\s*hidden/g, '');
                                                                            else if (dets[j].className.indexOf('hidden') < 0) dets[j].className += ' hidden';
                                                                        }
                                                                    }

                                                                    // ─── Exportar Excel ───────────────────────────────────────
                                                                    el('btn-excel').addEventListener('click', function () {
                                                                        try {
                                                                            if (typeof XLSX === 'undefined') {
                                                                                alert('A biblioteca de exportação para Excel (XLSX) não pôde ser carregada.');
                                                                                return;
                                                                            }
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
                                                                    [fResp, fOrig, fCenario, fTipo].forEach(function (f) {
                                                                        f.addEventListener('change', function () { if (carregado) render(); });
                                                                    });
                                                                    // Alterar as datas exige clicar em Carregar novamente
                                                                    [fDtDe, fDtAte].forEach(function (f) {
                                                                        f.addEventListener('change', function () { carregado = false; mostrarPrompt(); });
                                                                    });

                                                                    el('btn-expandir').addEventListener('click', function () { expandirTodos(true); });
                                                                    el('btn-recolher').addEventListener('click', function () { expandirTodos(false); });

                                                                    el('btn-limpar').addEventListener('click', function () {
                                                                        fDtDe.value = ''; fDtAte.value = '';
                                                                        if (msEmp) msEmp.clearAll();
                                                                        atualizarFiltrosCascata();
                                                                        if (msCc) msCc.clearAll();
                                                                        if (msProj) msProj.clearAll();
                                                                        if (msNat) msNat.clearAll();
                                                                        fResp.value = ''; fOrig.value = ''; fCenario.value = 'ORCAMENTO'; fTipo.value = 'D';
                                                                        carregado = false;
                                                                        mostrarPrompt();
                                                                    });

                                                                    // ─── Init ─────────────────────────────────────────────────
                                                                    if (!RAW.length) {
                                                                        el('kpis').innerHTML = '';
                                                                        el('tbody').innerHTML = '<tr><td colspan="5" class="empty">Nenhum registro retornado pela consulta.</td></tr>';
                                                                        el('resumo-body').innerHTML = '<tr><td colspan="8" class="empty">Nenhum registro.</td></tr>';
                                                                        return;
                                                                    }
                                                                    popularFiltros();
                                                                    mostrarPrompt();   // aguarda o preenchimento do período + Carregar
                                                                });
                                                            </script>
                                                </body>

                        </html>