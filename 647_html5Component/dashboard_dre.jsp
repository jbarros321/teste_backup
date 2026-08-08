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
                        <title>Dashboard DRE Corporativo</title>
                        <link rel="preconnect" href="https://fonts.googleapis.com">
                        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                        <link
                            href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&display=swap"
                            rel="stylesheet">
                        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
                            rel="stylesheet">
                        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css"
                            rel="stylesheet">
                        <script
                            src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js"></script>
                        <script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>
                        <snk:load />
                        <style>
                            body {
                                background-color: #f3f4f6;
                                font-family: 'Outfit', sans-serif;
                                color: #1f2937;
                                margin: 0;
                                padding: 0;
                            }

                            /* Vibrant Header */
                            .header-section {
                                background: linear-gradient(135deg, #4f46e5, #7c3aed, #ec4899);
                                color: white;
                                padding: 22px 30px;
                                margin-bottom: 25px;
                                box-shadow: 0 10px 25px rgba(79, 70, 229, 0.3);
                                border-bottom-left-radius: 15px;
                                border-bottom-right-radius: 15px;
                            }

                            .header-section h2 {
                                font-size: 1.6rem;
                                font-weight: 700;
                                margin-bottom: 4px;
                                letter-spacing: -0.5px;
                            }

                            .header-section p {
                                font-size: 0.95rem;
                                opacity: 0.9;
                                margin: 0;
                                font-weight: 400;
                            }

                            .card-container {
                                padding: 0 25px 25px 25px;
                            }

                            .card {
                                border: none;
                                border-radius: 16px;
                                box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
                                background: #ffffff;
                            }

                            .table-wrapper {
                                border-radius: 12px;
                                overflow: auto;
                                max-height: 70vh;
                            }

                            .dre-table {
                                margin-bottom: 0;
                                font-size: 0.85rem;
                                white-space: nowrap;
                                border-collapse: separate;
                                border-spacing: 0;
                                width: 100%;
                            }

                            /* Colorful Table Headers */
                            .dre-table th {
                                background-color: #f8fafc;
                                color: #334155;
                                font-weight: 700;
                                text-align: right;
                                border-bottom: 3px solid #cbd5e1;
                                padding: 12px 14px;
                                position: sticky;
                                top: 0;
                                z-index: 10;
                            }

                            .dre-table th:first-child {
                                text-align: left;
                                position: sticky;
                                left: 0;
                                z-index: 12;
                                background-color: #f1f5f9;
                                min-width: 280px;
                                border-right: 2px solid #e2e8f0;
                            }

                            .dre-table td {
                                vertical-align: middle;
                                text-align: right;
                                border-bottom: 1px solid #e2e8f0;
                                padding: 10px 14px;
                            }

                            .dre-table td:first-child {
                                text-align: left;
                                position: sticky;
                                left: 0;
                                z-index: 5;
                                font-weight: 600;
                                background-color: #fff;
                                border-right: 2px solid #e2e8f0;
                            }

                            .dre-table .th-total {
                                background-color: #f59e0b !important;
                                color: #fff !important;
                                border-bottom-color: #b45309 !important;
                            }

                            .td-total {
                                font-weight: 800 !important;
                                background-color: #fef3c7 !important;
                                color: #b45309 !important;
                            }

                            /* Row Styles based on Grau */
                            .grau-1 td {
                                background-color: #1e293b !important;
                                color: #f8fafc !important;
                                font-weight: 700;
                                border-bottom-color: #334155;
                            }

                            .grau-1:hover td {
                                background-color: #334155 !important;
                            }

                            .grau-2 td {
                                background-color: #f1f5f9 !important;
                                font-weight: 700;
                                color: #0f172a;
                            }

                            .grau-2:hover td {
                                background-color: #e2e8f0 !important;
                            }

                            .grau-3 td {
                                background-color: #ffffff !important;
                                font-weight: 500;
                                color: #334155;
                            }

                            .grau-3:hover td {
                                background-color: #f8fafc !important;
                            }

                            .grau-null td {
                                background-color: #ecfdf5 !important;
                                font-weight: 700;
                                color: #047857;
                            }

                            /* Variance Color Classes */
                            .var-pos {
                                color: #10b981;
                                font-weight: 700;
                            }

                            .var-neg {
                                color: #ef4444;
                                font-weight: 700;
                            }

                            .grau-1 .var-pos {
                                color: #34d399;
                            }

                            .grau-1 .var-neg {
                                color: #f87171;
                            }

                            .row-click {
                                cursor: pointer;
                                transition: background-color 0.15s ease;
                            }

                            .badge-safra {
                                background: linear-gradient(135deg, #f59e0b, #d97706);
                                font-size: 0.9rem;
                                padding: 6px 16px;
                                border-radius: 20px;
                                font-weight: 600;
                                box-shadow: 0 4px 10px rgba(245, 158, 11, 0.3);
                            }

                            .modal-xxl {
                                max-width: 96%;
                            }

                            .modal-header-custom {
                                background: linear-gradient(135deg, #4f46e5, #ec4899);
                                color: white;
                                border: none;
                            }

                            .modal-header-dark {
                                background: linear-gradient(135deg, #0f172a, #1e293b);
                                color: white;
                                border: none;
                            }

                            /* Column toggling classes */
                            .hide-prev .col-prev {
                                display: none !important;
                            }

                            .hide-real .col-real {
                                display: none !important;
                            }

                            .hide-var .col-var {
                                display: none !important;
                            }

                            .hide-m1 .col-m1 {
                                display: none !important;
                            }

                            .hide-m2 .col-m2 {
                                display: none !important;
                            }

                            .hide-m3 .col-m3 {
                                display: none !important;
                            }

                            .hide-m4 .col-m4 {
                                display: none !important;
                            }

                            .hide-m5 .col-m5 {
                                display: none !important;
                            }

                            .hide-m6 .col-m6 {
                                display: none !important;
                            }

                            .hide-m7 .col-m7 {
                                display: none !important;
                            }

                            .hide-m8 .col-m8 {
                                display: none !important;
                            }

                            .hide-m9 .col-m9 {
                                display: none !important;
                            }

                            .hide-m10 .col-m10 {
                                display: none !important;
                            }

                            .hide-m11 .col-m11 {
                                display: none !important;
                            }

                            .hide-m12 .col-m12 {
                                display: none !important;
                            }

                            /* Utilities for PDF */
                            .exporting-pdf {
                                max-height: none !important;
                                overflow: visible !important;
                            }
                        </style>
                    </head>

                    <snk:query var="safras">
                        SELECT CODPROJ, IDENTIFICACAO FROM TCSPRJ WHERE AD_INICIOSAFRA IS NOT NULL ORDER BY CODPROJ
                    </snk:query>

                    <c:set var="safraEfetiva" value="${param.safra}" />
                    <c:if test="${empty safraEfetiva and not empty safras.rows}">
                        <c:set var="safraEfetiva" value="${safras.rows[0].CODPROJ}" />
                    </c:if>

                    <c:if test="${not empty safraEfetiva}">
                        <snk:query var="dreDados">
                            WITH METAS AS (
                            SELECT CODCTACTB, TO_CHAR(DTREF,'MM') AS MES, SUM(PREVISTO) AS PREVISTO
                            FROM TCBMET
                            WHERE DTREF BETWEEN (SELECT AD_INICIOSAFRA FROM TCSPRJ WHERE CODPROJ = ${safraEfetiva})
                            AND (SELECT AD_FINALSAFRA FROM TCSPRJ WHERE CODPROJ = ${safraEfetiva})
                            GROUP BY CODCTACTB, TO_CHAR(DTREF,'MM')
                            ),
                            LANCTOS AS (
                            SELECT CODCTACTB, TO_CHAR(REFERENCIA,'MM') AS MES,
                            ABS(SUM(CASE WHEN TIPLANC = 'D' THEN -VLRLANC WHEN TIPLANC = 'R' THEN VLRLANC END)) AS
                            REALIZADO
                            FROM TCBLAN
                            WHERE REFERENCIA BETWEEN (SELECT AD_INICIOSAFRA FROM TCSPRJ WHERE CODPROJ = ${safraEfetiva})
                            AND (SELECT AD_FINALSAFRA FROM TCSPRJ WHERE CODPROJ = ${safraEfetiva})
                            GROUP BY CODCTACTB, TO_CHAR(REFERENCIA,'MM')
                            ),
                            PREV_POR_CODNAT AS (
                            SELECT CTB.CODNAT, M.MES, SUM(M.PREVISTO) AS PREVISTO
                            FROM AD_CTCONTABIL CTB JOIN METAS M ON M.CODCTACTB = CTB.CODCTB
                            GROUP BY CTB.CODNAT, M.MES
                            ),
                            REAL_POR_CODNAT AS (
                            SELECT CTB.CODNAT, L.MES, ABS(SUM(L.REALIZADO)) AS REALIZADO
                            FROM AD_CTCONTABIL CTB JOIN LANCTOS L ON L.CODCTACTB = CTB.CODCTB
                            GROUP BY CTB.CODNAT, L.MES
                            ),
                            FILHOS AS (
                            SELECT CTA.CODNAT, CTA.DESCRICAO, CTA.GRAU, CTA.CODNATPAI,
                            NVL(MAX(CASE WHEN P.MES='01' THEN P.PREVISTO END),0) AS JAN_PREV,
                            ABS(NVL(MAX(CASE WHEN R.MES='01' THEN R.REALIZADO END),0)) AS JAN_REAL,
                            NVL(MAX(CASE WHEN P.MES='02' THEN P.PREVISTO END),0) AS FEV_PREV,
                            ABS(NVL(MAX(CASE WHEN R.MES='02' THEN R.REALIZADO END),0)) AS FEV_REAL,
                            NVL(MAX(CASE WHEN P.MES='03' THEN P.PREVISTO END),0) AS MAR_PREV,
                            ABS(NVL(MAX(CASE WHEN R.MES='03' THEN R.REALIZADO END),0)) AS MAR_REAL,
                            NVL(MAX(CASE WHEN P.MES='04' THEN P.PREVISTO END),0) AS ABR_PREV,
                            ABS(NVL(MAX(CASE WHEN R.MES='04' THEN R.REALIZADO END),0)) AS ABR_REAL,
                            NVL(MAX(CASE WHEN P.MES='05' THEN P.PREVISTO END),0) AS MAI_PREV,
                            ABS(NVL(MAX(CASE WHEN R.MES='05' THEN R.REALIZADO END),0)) AS MAI_REAL,
                            NVL(MAX(CASE WHEN P.MES='06' THEN P.PREVISTO END),0) AS JUN_PREV,
                            ABS(NVL(MAX(CASE WHEN R.MES='06' THEN R.REALIZADO END),0)) AS JUN_REAL,
                            NVL(MAX(CASE WHEN P.MES='07' THEN P.PREVISTO END),0) AS JUL_PREV,
                            ABS(NVL(MAX(CASE WHEN R.MES='07' THEN R.REALIZADO END),0)) AS JUL_REAL,
                            NVL(MAX(CASE WHEN P.MES='08' THEN P.PREVISTO END),0) AS AGO_PREV,
                            ABS(NVL(MAX(CASE WHEN R.MES='08' THEN R.REALIZADO END),0)) AS AGO_REAL,
                            NVL(MAX(CASE WHEN P.MES='09' THEN P.PREVISTO END),0) AS SETE_PREV,
                            ABS(NVL(MAX(CASE WHEN R.MES='09' THEN R.REALIZADO END),0)) AS SETE_REAL,
                            NVL(MAX(CASE WHEN P.MES='10' THEN P.PREVISTO END),0) AS OUT_PREV,
                            ABS(NVL(MAX(CASE WHEN R.MES='10' THEN R.REALIZADO END),0)) AS OUT_REAL,
                            NVL(MAX(CASE WHEN P.MES='11' THEN P.PREVISTO END),0) AS NOV_PREV,
                            ABS(NVL(MAX(CASE WHEN R.MES='11' THEN R.REALIZADO END),0)) AS NOV_REAL,
                            NVL(MAX(CASE WHEN P.MES='12' THEN P.PREVISTO END),0) AS DEZ_PREV,
                            ABS(NVL(MAX(CASE WHEN R.MES='12' THEN R.REALIZADO END),0)) AS DEZ_REAL
                            FROM AD_DRETOTAL CTA
                            LEFT JOIN PREV_POR_CODNAT P ON P.CODNAT = CTA.CODNAT
                            LEFT JOIN REAL_POR_CODNAT R ON R.CODNAT = CTA.CODNAT
                            WHERE NVL(CTA.USAFORMCALC, 'N') = 'N'
                            GROUP BY CTA.CODNAT, CTA.DESCRICAO, CTA.GRAU, CTA.CODNATPAI
                            )
                            SELECT CODNAT, DESCRICAO, GRAU, CODNATPAI,
                            JAN_PREV, JAN_REAL, FEV_PREV, FEV_REAL, MAR_PREV, MAR_REAL,
                            ABR_PREV, ABR_REAL, MAI_PREV, MAI_REAL, JUN_PREV, JUN_REAL,
                            JUL_PREV, JUL_REAL, AGO_PREV, AGO_REAL, SETE_PREV, SETE_REAL,
                            OUT_PREV, OUT_REAL, NOV_PREV, NOV_REAL, DEZ_PREV, DEZ_REAL,
                            (JAN_PREV+FEV_PREV+MAR_PREV+ABR_PREV+MAI_PREV+JUN_PREV+JUL_PREV+AGO_PREV+SETE_PREV+OUT_PREV+NOV_PREV+DEZ_PREV)
                            AS TOTAL_PREV,
                            (JAN_REAL+FEV_REAL+MAR_REAL+ABR_REAL+MAI_REAL+JUN_REAL+JUL_REAL+AGO_REAL+SETE_REAL+OUT_REAL+NOV_REAL+DEZ_REAL)
                            AS TOTAL_REAL
                            FROM FILHOS
                            UNION ALL
                            SELECT M.CODNAT,
                            (SELECT DESCRICAO FROM AD_DRETOTAL WHERE CODNAT = M.CODNAT) AS DESCRICAO,
                            (SELECT GRAU FROM AD_DRETOTAL WHERE CODNAT = M.CODNAT) AS GRAU,
                            (SELECT CODNATPAI FROM AD_DRETOTAL WHERE CODNAT = M.CODNAT) AS CODNATPAI,
                            NVL(SUM(CASE WHEN TO_CHAR(M.REFERENCIA,'MM')='01' THEN M.VALOR_META END),0) AS JAN_PREV,
                            ABS(NVL(SUM(CASE WHEN TO_CHAR(L.REFERENCIA,'MM')='01' THEN L.VALOR END),0)) AS JAN_REAL,
                            NVL(SUM(CASE WHEN TO_CHAR(M.REFERENCIA,'MM')='02' THEN M.VALOR_META END),0) AS FEV_PREV,
                            ABS(NVL(SUM(CASE WHEN TO_CHAR(L.REFERENCIA,'MM')='02' THEN L.VALOR END),0)) AS FEV_REAL,
                            NVL(SUM(CASE WHEN TO_CHAR(M.REFERENCIA,'MM')='03' THEN M.VALOR_META END),0) AS MAR_PREV,
                            ABS(NVL(SUM(CASE WHEN TO_CHAR(L.REFERENCIA,'MM')='03' THEN L.VALOR END),0)) AS MAR_REAL,
                            NVL(SUM(CASE WHEN TO_CHAR(M.REFERENCIA,'MM')='04' THEN M.VALOR_META END),0) AS ABR_PREV,
                            ABS(NVL(SUM(CASE WHEN TO_CHAR(L.REFERENCIA,'MM')='04' THEN L.VALOR END),0)) AS ABR_REAL,
                            NVL(SUM(CASE WHEN TO_CHAR(M.REFERENCIA,'MM')='05' THEN M.VALOR_META END),0) AS MAI_PREV,
                            ABS(NVL(SUM(CASE WHEN TO_CHAR(L.REFERENCIA,'MM')='05' THEN L.VALOR END),0)) AS MAI_REAL,
                            NVL(SUM(CASE WHEN TO_CHAR(M.REFERENCIA,'MM')='06' THEN M.VALOR_META END),0) AS JUN_PREV,
                            ABS(NVL(SUM(CASE WHEN TO_CHAR(L.REFERENCIA,'MM')='06' THEN L.VALOR END),0)) AS JUN_REAL,
                            NVL(SUM(CASE WHEN TO_CHAR(M.REFERENCIA,'MM')='07' THEN M.VALOR_META END),0) AS JUL_PREV,
                            ABS(NVL(SUM(CASE WHEN TO_CHAR(L.REFERENCIA,'MM')='07' THEN L.VALOR END),0)) AS JUL_REAL,
                            NVL(SUM(CASE WHEN TO_CHAR(M.REFERENCIA,'MM')='08' THEN M.VALOR_META END),0) AS AGO_PREV,
                            ABS(NVL(SUM(CASE WHEN TO_CHAR(L.REFERENCIA,'MM')='08' THEN L.VALOR END),0)) AS AGO_REAL,
                            NVL(SUM(CASE WHEN TO_CHAR(M.REFERENCIA,'MM')='09' THEN M.VALOR_META END),0) AS SETE_PREV,
                            ABS(NVL(SUM(CASE WHEN TO_CHAR(L.REFERENCIA,'MM')='09' THEN L.VALOR END),0)) AS SETE_REAL,
                            NVL(SUM(CASE WHEN TO_CHAR(M.REFERENCIA,'MM')='10' THEN M.VALOR_META END),0) AS OUT_PREV,
                            ABS(NVL(SUM(CASE WHEN TO_CHAR(L.REFERENCIA,'MM')='10' THEN L.VALOR END),0)) AS OUT_REAL,
                            NVL(SUM(CASE WHEN TO_CHAR(M.REFERENCIA,'MM')='11' THEN M.VALOR_META END),0) AS NOV_PREV,
                            ABS(NVL(SUM(CASE WHEN TO_CHAR(L.REFERENCIA,'MM')='11' THEN L.VALOR END),0)) AS NOV_REAL,
                            NVL(SUM(CASE WHEN TO_CHAR(M.REFERENCIA,'MM')='12' THEN M.VALOR_META END),0) AS DEZ_PREV,
                            ABS(NVL(SUM(CASE WHEN TO_CHAR(L.REFERENCIA,'MM')='12' THEN L.VALOR END),0)) AS DEZ_REAL,
                            NVL(SUM(M.VALOR_META),0) AS TOTAL_PREV,
                            NVL(SUM(L.VALOR),0) AS TOTAL_REAL
                            FROM AD_ADMETAFORMDRETOTAL M
                            LEFT JOIN AD_RESULTFORMDRETOTAL L ON L.CODNAT = M.CODNAT AND L.REFERENCIA = M.REFERENCIA
                            WHERE M.REFERENCIA BETWEEN (SELECT AD_INICIOSAFRA FROM TCSPRJ WHERE CODPROJ =
                            ${safraEfetiva})
                            AND (SELECT AD_FINALSAFRA FROM TCSPRJ WHERE CODPROJ = ${safraEfetiva})
                            GROUP BY M.CODNAT
                            ORDER BY CODNAT
                        </snk:query>
                    </c:if>

                    <body id="dreBody">
                        <div class="header-section d-flex flex-wrap justify-content-between align-items-center">
                            <div>
                                <h2><i class="bi bi-pie-chart-fill me-2" style="color: #fbcfe8;"></i>Dashboard DRE
                                    Corporativo</h2>
                                <p><i class="bi bi-bar-chart-steps me-1"></i>Acompanhamento consolidado de Metas,
                                    Realizados e Variação por Safra</p>
                            </div>
                            <div class="d-flex align-items-center mt-3 mt-md-0 gap-3">

                                <!-- Botão Exportar PDF -->
                                <button class="btn btn-warning shadow-sm fw-bold border-0" style="color: #9a3412;"
                                    onclick="exportarParaPDF()">
                                    <i class="bi bi-file-earmark-pdf-fill me-1"></i> Exportar PDF
                                </button>

                                <button class="btn btn-success shadow-sm fw-bold border-0" style="color: #fff;"
                                    onclick="exportarParaExcel('mainDreTable', 'DRE_Safra_' + safraAtual)">
                                    <i class="bi bi-file-earmark-excel-fill me-1"></i> Exportar Excel
                                </button>

                                <!-- Configuracao de Colunas -->
                                <div class="dropdown">
                                    <button class="btn btn-light shadow-sm fw-semibold" type="button"
                                        data-bs-toggle="dropdown" data-bs-auto-close="outside">
                                        <i class="bi bi-layout-three-columns me-1"></i> Colunas
                                    </button>
                                    <div class="dropdown-menu p-3 shadow-lg border-0"
                                        style="min-width: 260px; border-radius: 12px;">
                                        <h6 class="dropdown-header px-0 text-dark fw-bold">Tipos de Valores</h6>
                                        <div class="form-check form-switch mb-1">
                                            <input class="form-check-input" type="checkbox" id="chkPrev" checked
                                                onchange="toggleCols()">
                                            <label class="form-check-label" for="chkPrev">Mostrar Previsto</label>
                                        </div>
                                        <div class="form-check form-switch mb-1">
                                            <input class="form-check-input" type="checkbox" id="chkReal" checked
                                                onchange="toggleCols()">
                                            <label class="form-check-label" for="chkReal">Mostrar Realizado</label>
                                        </div>
                                        <div class="form-check form-switch mb-2">
                                            <input class="form-check-input" type="checkbox" id="chkVar" checked
                                                onchange="toggleCols()">
                                            <label class="form-check-label" for="chkVar">Mostrar Variação</label>
                                        </div>

                                        <h6 class="dropdown-header px-0 text-dark fw-bold border-top pt-2 mt-2">Meses
                                            Exibidos</h6>
                                        <div class="row g-2">
                                            <div class="col-4">
                                                <div class="form-check"><input class="form-check-input chk-month"
                                                        type="checkbox" value="1" id="chkM1" checked
                                                        onchange="toggleCols()"><label class="form-check-label"
                                                        for="chkM1">Jan</label></div>
                                            </div>
                                            <div class="col-4">
                                                <div class="form-check"><input class="form-check-input chk-month"
                                                        type="checkbox" value="2" id="chkM2" checked
                                                        onchange="toggleCols()"><label class="form-check-label"
                                                        for="chkM2">Fev</label></div>
                                            </div>
                                            <div class="col-4">
                                                <div class="form-check"><input class="form-check-input chk-month"
                                                        type="checkbox" value="3" id="chkM3" checked
                                                        onchange="toggleCols()"><label class="form-check-label"
                                                        for="chkM3">Mar</label></div>
                                            </div>
                                            <div class="col-4">
                                                <div class="form-check"><input class="form-check-input chk-month"
                                                        type="checkbox" value="4" id="chkM4" checked
                                                        onchange="toggleCols()"><label class="form-check-label"
                                                        for="chkM4">Abr</label></div>
                                            </div>
                                            <div class="col-4">
                                                <div class="form-check"><input class="form-check-input chk-month"
                                                        type="checkbox" value="5" id="chkM5" checked
                                                        onchange="toggleCols()"><label class="form-check-label"
                                                        for="chkM5">Mai</label></div>
                                            </div>
                                            <div class="col-4">
                                                <div class="form-check"><input class="form-check-input chk-month"
                                                        type="checkbox" value="6" id="chkM6" checked
                                                        onchange="toggleCols()"><label class="form-check-label"
                                                        for="chkM6">Jun</label></div>
                                            </div>
                                            <div class="col-4">
                                                <div class="form-check"><input class="form-check-input chk-month"
                                                        type="checkbox" value="7" id="chkM7" checked
                                                        onchange="toggleCols()"><label class="form-check-label"
                                                        for="chkM7">Jul</label></div>
                                            </div>
                                            <div class="col-4">
                                                <div class="form-check"><input class="form-check-input chk-month"
                                                        type="checkbox" value="8" id="chkM8" checked
                                                        onchange="toggleCols()"><label class="form-check-label"
                                                        for="chkM8">Ago</label></div>
                                            </div>
                                            <div class="col-4">
                                                <div class="form-check"><input class="form-check-input chk-month"
                                                        type="checkbox" value="9" id="chkM9" checked
                                                        onchange="toggleCols()"><label class="form-check-label"
                                                        for="chkM9">Set</label></div>
                                            </div>
                                            <div class="col-4">
                                                <div class="form-check"><input class="form-check-input chk-month"
                                                        type="checkbox" value="10" id="chkM10" checked
                                                        onchange="toggleCols()"><label class="form-check-label"
                                                        for="chkM10">Out</label></div>
                                            </div>
                                            <div class="col-4">
                                                <div class="form-check"><input class="form-check-input chk-month"
                                                        type="checkbox" value="11" id="chkM11" checked
                                                        onchange="toggleCols()"><label class="form-check-label"
                                                        for="chkM11">Nov</label></div>
                                            </div>
                                            <div class="col-4">
                                                <div class="form-check"><input class="form-check-input chk-month"
                                                        type="checkbox" value="12" id="chkM12" checked
                                                        onchange="toggleCols()"><label class="form-check-label"
                                                        for="chkM12">Dez</label></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Selecao de Safra -->
                                <div class="d-flex align-items-center">
                                    <label for="safraSelect" class="me-2 fw-semibold text-light"
                                        style="white-space:nowrap;">Safra:</label>
                                    <select id="safraSelect" class="form-select shadow-sm fw-semibold"
                                        style="min-width: 200px; color: #4f46e5;" onchange="trocarSafra(this.value)">
                                        <c:forEach items="${safras.rows}" var="s">
                                            <option value="${s.CODPROJ}" <c:if test="${s.CODPROJ == safraEfetiva}">
                                                selected</c:if>>${s.CODPROJ} - ${s.IDENTIFICACAO}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="card-container">
                            <div class="card p-4">
                                <div class="d-flex justify-content-between align-items-center mb-3">
                                    <h5 class="fw-bold m-0" style="color: #3730a3;"><i
                                            class="bi bi-table me-2"></i>Visão Consolidada DRE</h5>
                                    <c:if test="${not empty safraEfetiva}">
                                        <span class="badge badge-safra text-white">Safra Ativa: ${safraEfetiva}</span>
                                    </c:if>
                                </div>

                                <c:choose>
                                    <c:when test="${empty safraEfetiva}">
                                        <div class="text-center py-5">
                                            <i class="bi bi-inbox d-block" style="font-size: 3rem; color: #94a3b8;"></i>
                                            <p class="fw-semibold mt-2" style="color: #64748b;">Nenhuma safra encontrada
                                                no sistema.</p>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="table-wrapper" id="pdfTableWrapper">
                                            <table class="dre-table" id="mainDreTable">
                                                <thead>
                                                    <tr>
                                                        <th>Natureza / Descrição</th>

                                                        <th class="col-m1 col-prev">Jan Prev</th>
                                                        <th class="col-m1 col-real">Jan Real</th>
                                                        <th class="col-m1 col-var">Jan Var</th>
                                                        <th class="col-m2 col-prev">Fev Prev</th>
                                                        <th class="col-m2 col-real">Fev Real</th>
                                                        <th class="col-m2 col-var">Fev Var</th>
                                                        <th class="col-m3 col-prev">Mar Prev</th>
                                                        <th class="col-m3 col-real">Mar Real</th>
                                                        <th class="col-m3 col-var">Mar Var</th>
                                                        <th class="col-m4 col-prev">Abr Prev</th>
                                                        <th class="col-m4 col-real">Abr Real</th>
                                                        <th class="col-m4 col-var">Abr Var</th>
                                                        <th class="col-m5 col-prev">Mai Prev</th>
                                                        <th class="col-m5 col-real">Mai Real</th>
                                                        <th class="col-m5 col-var">Mai Var</th>
                                                        <th class="col-m6 col-prev">Jun Prev</th>
                                                        <th class="col-m6 col-real">Jun Real</th>
                                                        <th class="col-m6 col-var">Jun Var</th>
                                                        <th class="col-m7 col-prev">Jul Prev</th>
                                                        <th class="col-m7 col-real">Jul Real</th>
                                                        <th class="col-m7 col-var">Jul Var</th>
                                                        <th class="col-m8 col-prev">Ago Prev</th>
                                                        <th class="col-m8 col-real">Ago Real</th>
                                                        <th class="col-m8 col-var">Ago Var</th>
                                                        <th class="col-m9 col-prev">Set Prev</th>
                                                        <th class="col-m9 col-real">Set Real</th>
                                                        <th class="col-m9 col-var">Set Var</th>
                                                        <th class="col-m10 col-prev">Out Prev</th>
                                                        <th class="col-m10 col-real">Out Real</th>
                                                        <th class="col-m10 col-var">Out Var</th>
                                                        <th class="col-m11 col-prev">Nov Prev</th>
                                                        <th class="col-m11 col-real">Nov Real</th>
                                                        <th class="col-m11 col-var">Nov Var</th>
                                                        <th class="col-m12 col-prev">Dez Prev</th>
                                                        <th class="col-m12 col-real">Dez Real</th>
                                                        <th class="col-m12 col-var">Dez Var</th>

                                                        <th class="th-total col-total col-prev">Total Prev</th>
                                                        <th class="th-total col-total col-real">Total Real</th>
                                                        <th class="th-total col-total col-var">Total Var</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach items="${dreDados.rows}" var="row">
                                                        <c:set var="grauClass" value="" />
                                                        <c:choose>
                                                            <c:when test="${row.GRAU == 1}">
                                                                <c:set var="grauClass" value="grau-1" />
                                                            </c:when>
                                                            <c:when test="${row.GRAU == 2}">
                                                                <c:set var="grauClass" value="grau-2" />
                                                            </c:when>
                                                            <c:when test="${row.GRAU == 3}">
                                                                <c:set var="grauClass" value="grau-3" />
                                                            </c:when>
                                                            <c:when test="${empty row.GRAU}">
                                                                <c:set var="grauClass" value="grau-null" />
                                                            </c:when>
                                                        </c:choose>

                                                        <tr class="row-click ${grauClass}"
                                                            onclick="abrirSintetico('${row.CODNAT}', '${row.DESCRICAO}')">
                                                            <td>
                                                                <div style="font-size: 0.95rem;">
                                                                    <strong>${row.DESCRICAO}</strong></div>
                                                                <small style="opacity:0.7;">Natureza:
                                                                    ${row.CODNAT}</small>
                                                            </td>

                                                            <!-- Mes 1 -->
                                                            <c:set var="v1" value="${row.JAN_REAL - row.JAN_PREV}" />
                                                            <td class="col-m1 col-prev">
                                                                <fmt:formatNumber value="${row.JAN_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="col-m1 col-real">
                                                                <fmt:formatNumber value="${row.JAN_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="col-m1 col-var ${v1 >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${v1}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>

                                                            <!-- Mes 2 -->
                                                            <c:set var="v2" value="${row.FEV_REAL - row.FEV_PREV}" />
                                                            <td class="col-m2 col-prev">
                                                                <fmt:formatNumber value="${row.FEV_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="col-m2 col-real">
                                                                <fmt:formatNumber value="${row.FEV_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="col-m2 col-var ${v2 >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${v2}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>

                                                            <!-- Mes 3 -->
                                                            <c:set var="v3" value="${row.MAR_REAL - row.MAR_PREV}" />
                                                            <td class="col-m3 col-prev">
                                                                <fmt:formatNumber value="${row.MAR_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="col-m3 col-real">
                                                                <fmt:formatNumber value="${row.MAR_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="col-m3 col-var ${v3 >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${v3}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>

                                                            <!-- Mes 4 -->
                                                            <c:set var="v4" value="${row.ABR_REAL - row.ABR_PREV}" />
                                                            <td class="col-m4 col-prev">
                                                                <fmt:formatNumber value="${row.ABR_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="col-m4 col-real">
                                                                <fmt:formatNumber value="${row.ABR_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="col-m4 col-var ${v4 >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${v4}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>

                                                            <!-- Mes 5 -->
                                                            <c:set var="v5" value="${row.MAI_REAL - row.MAI_PREV}" />
                                                            <td class="col-m5 col-prev">
                                                                <fmt:formatNumber value="${row.MAI_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="col-m5 col-real">
                                                                <fmt:formatNumber value="${row.MAI_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="col-m5 col-var ${v5 >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${v5}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>

                                                            <!-- Mes 6 -->
                                                            <c:set var="v6" value="${row.JUN_REAL - row.JUN_PREV}" />
                                                            <td class="col-m6 col-prev">
                                                                <fmt:formatNumber value="${row.JUN_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="col-m6 col-real">
                                                                <fmt:formatNumber value="${row.JUN_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="col-m6 col-var ${v6 >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${v6}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>

                                                            <!-- Mes 7 -->
                                                            <c:set var="v7" value="${row.JUL_REAL - row.JUL_PREV}" />
                                                            <td class="col-m7 col-prev">
                                                                <fmt:formatNumber value="${row.JUL_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="col-m7 col-real">
                                                                <fmt:formatNumber value="${row.JUL_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="col-m7 col-var ${v7 >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${v7}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>

                                                            <!-- Mes 8 -->
                                                            <c:set var="v8" value="${row.AGO_REAL - row.AGO_PREV}" />
                                                            <td class="col-m8 col-prev">
                                                                <fmt:formatNumber value="${row.AGO_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="col-m8 col-real">
                                                                <fmt:formatNumber value="${row.AGO_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="col-m8 col-var ${v8 >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${v8}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>

                                                            <!-- Mes 9 -->
                                                            <c:set var="v9" value="${row.SETE_REAL - row.SETE_PREV}" />
                                                            <td class="col-m9 col-prev">
                                                                <fmt:formatNumber value="${row.SETE_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="col-m9 col-real">
                                                                <fmt:formatNumber value="${row.SETE_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="col-m9 col-var ${v9 >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${v9}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>

                                                            <!-- Mes 10 -->
                                                            <c:set var="v10" value="${row.OUT_REAL - row.OUT_PREV}" />
                                                            <td class="col-m10 col-prev">
                                                                <fmt:formatNumber value="${row.OUT_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="col-m10 col-real">
                                                                <fmt:formatNumber value="${row.OUT_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="col-m10 col-var ${v10 >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${v10}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>

                                                            <!-- Mes 11 -->
                                                            <c:set var="v11" value="${row.NOV_REAL - row.NOV_PREV}" />
                                                            <td class="col-m11 col-prev">
                                                                <fmt:formatNumber value="${row.NOV_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="col-m11 col-real">
                                                                <fmt:formatNumber value="${row.NOV_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="col-m11 col-var ${v11 >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${v11}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>

                                                            <!-- Mes 12 -->
                                                            <c:set var="v12" value="${row.DEZ_REAL - row.DEZ_PREV}" />
                                                            <td class="col-m12 col-prev">
                                                                <fmt:formatNumber value="${row.DEZ_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="col-m12 col-real">
                                                                <fmt:formatNumber value="${row.DEZ_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="col-m12 col-var ${v12 >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${v12}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>

                                                            <!-- Total -->
                                                            <c:set var="vT"
                                                                value="${row.TOTAL_REAL - row.TOTAL_PREV}" />
                                                            <td class="td-total col-total col-prev">
                                                                <fmt:formatNumber value="${row.TOTAL_PREV}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td class="td-total col-total col-real">
                                                                <fmt:formatNumber value="${row.TOTAL_REAL}"
                                                                    type="currency" currencySymbol="R$ " />
                                                            </td>
                                                            <td
                                                                class="td-total col-total col-var ${vT >= 0 ? 'var-pos' : 'var-neg'}">
                                                                <fmt:formatNumber value="${vT}" type="currency"
                                                                    currencySymbol="R$ " />
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Modal Sintetico -->
                        <div class="modal fade" id="modalSintetico" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-xxl modal-dialog-centered modal-dialog-scrollable">
                                <div class="modal-content border-0 shadow-lg" style="border-radius: 16px;">
                                    <div class="modal-header modal-header-custom">
                                        <h5 class="modal-title fw-bold"><i class="bi bi-diagram-3-fill me-2"></i>Visão
                                            Sintética da Conta</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"
                                            aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body" style="background: #f8fafc; padding: 25px;">
                                        <p class="text-muted mb-3" id="subtitleSintetico" style="font-size: 1.1rem;">
                                        </p>
                                        <div id="sinteticoLoading" class="text-center py-5">
                                            <div class="spinner-border text-primary" role="status"></div>
                                            <p class="mt-2 text-muted fw-semibold">Carregando plano de contas...</p>


                                        </div>
                                        <div class="table-wrapper" id="sinteticoTableWrapper" style="display:none;">
                                            <table class="dre-table" id="tableSintetico">
                                                <thead>
                                                    <tr>
                                                        <th>Cód. Conta</th>
                                                        <th>Descrição da Conta</th>
                                                        <th class="col-m1 col-prev">Jan Prev</th>
                                                        <th class="col-m1 col-real">Jan Real</th>
                                                        <th class="col-m1 col-var">Jan Var</th>
                                                        <th class="col-m2 col-prev">Fev Prev</th>
                                                        <th class="col-m2 col-real">Fev Real</th>
                                                        <th class="col-m2 col-var">Fev Var</th>
                                                        <th class="col-m3 col-prev">Mar Prev</th>
                                                        <th class="col-m3 col-real">Mar Real</th>
                                                        <th class="col-m3 col-var">Mar Var</th>
                                                        <th class="col-m4 col-prev">Abr Prev</th>
                                                        <th class="col-m4 col-real">Abr Real</th>
                                                        <th class="col-m4 col-var">Abr Var</th>
                                                        <th class="col-m5 col-prev">Mai Prev</th>
                                                        <th class="col-m5 col-real">Mai Real</th>
                                                        <th class="col-m5 col-var">Mai Var</th>
                                                        <th class="col-m6 col-prev">Jun Prev</th>
                                                        <th class="col-m6 col-real">Jun Real</th>
                                                        <th class="col-m6 col-var">Jun Var</th>
                                                        <th class="col-m7 col-prev">Jul Prev</th>
                                                        <th class="col-m7 col-real">Jul Real</th>
                                                        <th class="col-m7 col-var">Jul Var</th>
                                                        <th class="col-m8 col-prev">Ago Prev</th>
                                                        <th class="col-m8 col-real">Ago Real</th>
                                                        <th class="col-m8 col-var">Ago Var</th>
                                                        <th class="col-m9 col-prev">Set Prev</th>
                                                        <th class="col-m9 col-real">Set Real</th>
                                                        <th class="col-m9 col-var">Set Var</th>
                                                        <th class="col-m10 col-prev">Out Prev</th>
                                                        <th class="col-m10 col-real">Out Real</th>
                                                        <th class="col-m10 col-var">Out Var</th>
                                                        <th class="col-m11 col-prev">Nov Prev</th>
                                                        <th class="col-m11 col-real">Nov Real</th>
                                                        <th class="col-m11 col-var">Nov Var</th>
                                                        <th class="col-m12 col-prev">Dez Prev</th>
                                                        <th class="col-m12 col-real">Dez Real</th>
                                                        <th class="col-m12 col-var">Dez Var</th>
                                                        <th class="th-total col-total col-prev">Total Prev</th>
                                                        <th class="th-total col-total col-real">Total Real</th>
                                                        <th class="th-total col-total col-var">Total Var</th>
                                                    </tr>
                                                </thead>
                                                <tbody></tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Analitico -->
                        <div class="modal fade" id="modalAnalitico" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
                                <div class="modal-content border-0 shadow-lg" style="border-radius: 16px;">
                                    <div class="modal-header modal-header-dark">
                                        <h5 class="modal-title fw-bold"><i class="bi bi-journal-text me-2"
                                                style="color:#38bdf8;"></i>Visão Analítica - Lançamentos</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"
                                            aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body" style="background: #f1f5f9; padding: 25px;">
                                        <p class="text-muted mb-3" id="subtitleAnalitico" style="font-size: 1.1rem;">
                                        </p>
                                        <div id="analiticoLoading" class="text-center py-5">
                                            <div class="spinner-border text-primary" role="status"></div>
                                            <p class="mt-2 text-muted fw-semibold">Buscando lançamentos...</p>
                                        </div>
                                        <div class="table-wrapper" id="analiticoTableWrapper" style="display:none;">
                                            <table class="dre-table" id="tableAnalitico">
                                                <thead>
                                                    <tr>
                                                        <th>Cód. Emp.</th>
                                                        <th>Mês Referência</th>
                                                        <th>Nº Lote</th>
                                                        <th>Nº Lanç.</th>
                                                        <th>Cód. Conta</th>
                                                        <th>Descrição Conta</th>
                                                        <th>Data Mov.</th>
                                                        <th>Valor (R$)</th>
                                                        <th>Histórico</th>
                                                        <th>Nº Doc.</th>
                                                    </tr>
                                                </thead>
                                                <tbody></tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <script
                            src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                        <script>


                            function exportarParaExcel(tableId, nomeArquivo) {
                                var table = document.getElementById(tableId);
                                if (!table) return;

                                var wb = XLSX.utils.book_new();
                                var wsData = [];

                                // Cabeçalhos — pega apenas colunas visíveis
                                var headers = [];
                                table.querySelectorAll('thead tr th').forEach(function (th) {
                                    if (th.offsetParent !== null) headers.push(th.innerText.trim());
                                });
                                wsData.push(headers);

                                // Linhas — apenas colunas visíveis
                                table.querySelectorAll('tbody tr').forEach(function (tr) {
                                    var row = [];
                                    tr.querySelectorAll('td').forEach(function (td) {
                                        if (td.offsetParent !== null) {
                                            // Limpa símbolo de moeda para número
                                            var txt = td.innerText.trim()
                                                .replace('R$\u00a0', '').replace('R$ ', '')
                                                .replace(/\./g, '').replace(',', '.');
                                            var num = parseFloat(txt);
                                            row.push(isNaN(num) ? td.innerText.trim() : num);
                                        }
                                    });
                                    wsData.push(row);
                                });

                                var ws = XLSX.utils.aoa_to_sheet(wsData);

                                // Largura automática das colunas
                                var colWidths = wsData[0].map(function (_, ci) {
                                    return {
                                        wch: Math.max.apply(null, wsData.map(function (r) {
                                            return r[ci] ? String(r[ci]).length : 10;
                                        })) + 2
                                    };
                                });
                                ws['!cols'] = colWidths;

                                XLSX.utils.book_append_sheet(wb, ws, 'DRE');
                                XLSX.writeFile(wb, nomeArquivo + '.xlsx');
                            }


                            var safraAtual = '${safraEfetiva}';

                            function trocarSafra(valor) {
                                var urlParams = new URLSearchParams(window.location.search);
                                urlParams.set('safra', encodeURIComponent(valor));
                                window.location.search = urlParams.toString();
                            }

                            function exportarParaPDF() {
                                var element = document.getElementById('pdfTableWrapper');
                                var originalHeight = element.style.maxHeight;

                                // Remove scroll restrictions before printing
                                element.classList.add('exporting-pdf');
                                element.style.maxHeight = 'none';

                                var opt = {
                                    margin: 0.2,
                                    filename: 'DRE_Safra_' + safraAtual + '.pdf',
                                    image: { type: 'jpeg', quality: 0.98 },
                                    html2canvas: { scale: 2, useCORS: true, logging: false },
                                    jsPDF: { unit: 'in', format: 'a3', orientation: 'landscape' }
                                };

                                html2pdf().set(opt).from(element).save().then(function () {
                                    // Restore scroll restrictions
                                    element.classList.remove('exporting-pdf');
                                    element.style.maxHeight = originalHeight;
                                });
                            }

                            function toggleCols() {
                                var body = document.getElementById('dreBody');
                                if (!document.getElementById('chkPrev').checked) body.classList.add('hide-prev'); else body.classList.remove('hide-prev');
                                if (!document.getElementById('chkReal').checked) body.classList.add('hide-real'); else body.classList.remove('hide-real');
                                if (!document.getElementById('chkVar').checked) body.classList.add('hide-var'); else body.classList.remove('hide-var');

                                for (var i = 1; i <= 12; i++) {
                                    if (!document.getElementById('chkM' + i).checked) body.classList.add('hide-m' + i);
                                    else body.classList.remove('hide-m' + i);
                                }
                            }

                            function fmtMoney(v) {
                                if (v === null || v === undefined || isNaN(v)) return 'R$ 0,00';
                                return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v);
                            }

                            function fmtDate(v) {
                                if (!v) return '';
                                var s = String(v);

                                // Formato DD/MM/YYYY HH:MM:SS ou DD/MM/YYYY
                                if (/^\d{2}\/\d{2}\/\d{4}/.test(s)) return s.split(' ')[0];

                                // Timestamp numérico (milissegundos)
                                if (/^\d{10,}$/.test(s)) {
                                    return new Date(parseInt(s)).toLocaleDateString('pt-BR');
                                }

                                // Formato ISO YYYY-MM-DD ou YYYY-MM-DDTHH:MM:SS
                                if (/^\d{4}-\d{2}-\d{2}/.test(s)) {
                                    var parts = s.substring(0, 10).split('-');
                                    return parts[2] + '/' + parts[1] + '/' + parts[0];
                                }

                                // Tenta converter diretamente
                                try {
                                    var d = new Date(v);
                                    if (!isNaN(d.getTime())) return d.toLocaleDateString('pt-BR');
                                } catch (e) { }

                                return s;
                            }

                            function callSnkService(sql, callback) {
                                var xhr = new XMLHttpRequest();
                                var url = '/mge/service.sbr?serviceName=DbExplorerSP.executeQuery&outputType=json';
                                xhr.open('POST', url, true);
                                xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
                                xhr.withCredentials = true;
                                xhr.onreadystatechange = function () {
                                    if (xhr.readyState === 4) {
                                        try {
                                            var data = JSON.parse(xhr.responseText);
                                            if (data.status === '1' && data.responseBody && data.responseBody.rows) {
                                                callback(data.responseBody.rows);
                                            } else {
                                                console.error('Sankhya API error:', data.statusMessage || 'desconhecido');
                                                callback([]);
                                            }
                                        } catch (e) {
                                            console.error('Parse error:', e);
                                            callback([]);
                                        }
                                    }
                                };
                                xhr.send(JSON.stringify({ serviceName: "DbExplorerSP.executeQuery", requestBody: { sql: sql } }));
                            }

                            function abrirSintetico(codNat, descNat) {
                                if (descNat) descNat = descNat.replace(/'/g, "\\'");
                                document.getElementById('subtitleSintetico').innerHTML = 'Natureza Selecionada: <strong>' + descNat + '</strong> (Cód: ' + codNat + ')';
                                document.getElementById('sinteticoLoading').style.display = 'block';
                                document.getElementById('sinteticoTableWrapper').style.display = 'none';
                                new bootstrap.Modal(document.getElementById('modalSintetico')).show();

                                var sql = "WITH METAS AS (" +
                                    "SELECT CODCTACTB, TO_CHAR(DTREF,'MM') AS MES, NVL(SUM(PREVISTO),0) AS PREVISTO, NVL(SUM(REALIZADO),0) AS REALIZADO " +
                                    "FROM TCBMET GROUP BY CODCTACTB, TO_CHAR(DTREF,'MM')" +
                                    "), AGGR_CONTA AS (" +
                                    "SELECT C.CODNAT, C.CODCTB," +
                                    "NVL(SUM(CASE WHEN M.MES='01' THEN M.PREVISTO END),0) AS JAN_PREV, NVL(SUM(CASE WHEN M.MES='01' THEN M.REALIZADO END),0) AS JAN_REAL," +
                                    "NVL(SUM(CASE WHEN M.MES='02' THEN M.PREVISTO END),0) AS FEV_PREV, NVL(SUM(CASE WHEN M.MES='02' THEN M.REALIZADO END),0) AS FEV_REAL," +
                                    "NVL(SUM(CASE WHEN M.MES='03' THEN M.PREVISTO END),0) AS MAR_PREV, NVL(SUM(CASE WHEN M.MES='03' THEN M.REALIZADO END),0) AS MAR_REAL," +
                                    "NVL(SUM(CASE WHEN M.MES='04' THEN M.PREVISTO END),0) AS ABR_PREV, NVL(SUM(CASE WHEN M.MES='04' THEN M.REALIZADO END),0) AS ABR_REAL," +
                                    "NVL(SUM(CASE WHEN M.MES='05' THEN M.PREVISTO END),0) AS MAI_PREV, NVL(SUM(CASE WHEN M.MES='05' THEN M.REALIZADO END),0) AS MAI_REAL," +
                                    "NVL(SUM(CASE WHEN M.MES='06' THEN M.PREVISTO END),0) AS JUN_PREV, NVL(SUM(CASE WHEN M.MES='06' THEN M.REALIZADO END),0) AS JUN_REAL," +
                                    "NVL(SUM(CASE WHEN M.MES='07' THEN M.PREVISTO END),0) AS JUL_PREV, NVL(SUM(CASE WHEN M.MES='07' THEN M.REALIZADO END),0) AS JUL_REAL," +
                                    "NVL(SUM(CASE WHEN M.MES='08' THEN M.PREVISTO END),0) AS AGO_PREV, NVL(SUM(CASE WHEN M.MES='08' THEN M.REALIZADO END),0) AS AGO_REAL," +
                                    "NVL(SUM(CASE WHEN M.MES='09' THEN M.PREVISTO END),0) AS SETE_PREV, NVL(SUM(CASE WHEN M.MES='09' THEN M.REALIZADO END),0) AS SETE_REAL," +
                                    "NVL(SUM(CASE WHEN M.MES='10' THEN M.PREVISTO END),0) AS OUT_PREV, NVL(SUM(CASE WHEN M.MES='10' THEN M.REALIZADO END),0) AS OUT_REAL," +
                                    "NVL(SUM(CASE WHEN M.MES='11' THEN M.PREVISTO END),0) AS NOV_PREV, NVL(SUM(CASE WHEN M.MES='11' THEN M.REALIZADO END),0) AS NOV_REAL," +
                                    "NVL(SUM(CASE WHEN M.MES='12' THEN M.PREVISTO END),0) AS DEZ_PREV, NVL(SUM(CASE WHEN M.MES='12' THEN M.REALIZADO END),0) AS DEZ_REAL " +
                                    "FROM AD_CTCONTABIL C LEFT JOIN METAS M ON M.CODCTACTB = C.CODCTB GROUP BY C.CODNAT, C.CODCTB" +
                                    ") SELECT C.CODNAT, COALESCE(P.DESCRCTA, TO_CHAR(C.CODCTB)) AS DESCRICAO, D.GRAU, D.CODNATPAI," +
                                    "C.CODCTB AS CONTA_CONTABIL, TO_CHAR(C.CODCTB) AS NOME_CONTA," +
                                    "NVL(A.JAN_PREV,0), NVL(A.JAN_REAL,0), NVL(A.FEV_PREV,0), NVL(A.FEV_REAL,0)," +
                                    "NVL(A.MAR_PREV,0), NVL(A.MAR_REAL,0), NVL(A.ABR_PREV,0), NVL(A.ABR_REAL,0)," +
                                    "NVL(A.MAI_PREV,0), NVL(A.MAI_REAL,0), NVL(A.JUN_PREV,0), NVL(A.JUN_REAL,0)," +
                                    "NVL(A.JUL_PREV,0), NVL(A.JUL_REAL,0), NVL(A.AGO_PREV,0), NVL(A.AGO_REAL,0)," +
                                    "NVL(A.SETE_PREV,0), NVL(A.SETE_REAL,0), NVL(A.OUT_PREV,0), NVL(A.OUT_REAL,0)," +
                                    "NVL(A.NOV_PREV,0), NVL(A.NOV_REAL,0), NVL(A.DEZ_PREV,0), NVL(A.DEZ_REAL,0)," +
                                    "(NVL(A.JAN_PREV,0)+NVL(A.FEV_PREV,0)+NVL(A.MAR_PREV,0)+NVL(A.ABR_PREV,0)+NVL(A.MAI_PREV,0)+NVL(A.JUN_PREV,0)+" +
                                    "NVL(A.JUL_PREV,0)+NVL(A.AGO_PREV,0)+NVL(A.SETE_PREV,0)+NVL(A.OUT_PREV,0)+NVL(A.NOV_PREV,0)+NVL(A.DEZ_PREV,0)) AS TOTAL_PREV," +
                                    "(NVL(A.JAN_REAL,0)+NVL(A.FEV_REAL,0)+NVL(A.MAR_REAL,0)+NVL(A.ABR_REAL,0)+NVL(A.MAI_REAL,0)+NVL(A.JUN_REAL,0)+" +
                                    "NVL(A.JUL_REAL,0)+NVL(A.AGO_REAL,0)+NVL(A.SETE_REAL,0)+NVL(A.OUT_REAL,0)+NVL(A.NOV_REAL,0)+NVL(A.DEZ_REAL,0)) AS TOTAL_REAL " +
                                    "FROM AD_CTCONTABIL C INNER JOIN AD_DRETOTAL D ON D.CODNAT = C.CODNAT AND NVL(D.ATIVO,'S') = 'S' " +
                                    "LEFT JOIN AGGR_CONTA A ON A.CODNAT = C.CODNAT AND A.CODCTB = C.CODCTB " +
                                    "LEFT JOIN TCBPLA P ON P.CODCTACTB = C.CODCTB " +
                                    "WHERE TRIM(TO_CHAR(C.CODNAT)) = TRIM(TO_CHAR(" + codNat + ")) ORDER BY C.CODCTB";

                                callSnkService(sql, function (rows) {
                                    var tbody = document.querySelector('#tableSintetico tbody');
                                    tbody.innerHTML = '';
                                    for (var k = 0; k < rows.length; k++) {
                                        var row = rows[k];
                                        var tr = document.createElement('tr');
                                        tr.className = 'row-click';
                                        tr.setAttribute('onclick', "abrirAnalitico('" + row[4] + "','" + (row[1] || '').replace(/'/g, "\\'") + "')");

                                        var h = '<td><strong style="font-size:0.95rem;">' + (row[4] || '') + '</strong></td><td>' + (row[1] || '') + '</td>';

                                        // Loop for 12 months
                                        var mIdx = 1;
                                        for (var i = 6; i <= 28; i += 2) {
                                            var p = row[i], r = row[i + 1], v = r - p;
                                            var cV = (v >= 0) ? 'var-pos' : 'var-neg';
                                            h += '<td class="col-m' + mIdx + ' col-prev">' + fmtMoney(p) + '</td>';
                                            h += '<td class="col-m' + mIdx + ' col-real">' + fmtMoney(r) + '</td>';
                                            h += '<td class="col-m' + mIdx + ' col-var ' + cV + '">' + fmtMoney(v) + '</td>';
                                            mIdx++;
                                        }

                                        // Totals
                                        var tP = row[30], tR = row[31], tV = tR - tP;
                                        var cTv = (tV >= 0) ? 'var-pos' : 'var-neg';
                                        h += '<td class="td-total col-total col-prev">' + fmtMoney(tP) + '</td>';
                                        h += '<td class="td-total col-total col-real">' + fmtMoney(tR) + '</td>';
                                        h += '<td class="td-total col-total col-var ' + cTv + '">' + fmtMoney(tV) + '</td>';

                                        tr.innerHTML = h;
                                        tbody.appendChild(tr);
                                    }
                                    document.getElementById('sinteticoLoading').style.display = 'none';
                                    document.getElementById('sinteticoTableWrapper').style.display = 'block';
                                });
                            }

                            function abrirAnalitico(codCtaCtb, descConta) {
                                if (descConta) descConta = descConta.replace(/'/g, "\\'");
                                document.getElementById('subtitleAnalitico').innerHTML = 'Conta Contábil Selecionada: <strong>' + descConta + '</strong> (Cód: ' + codCtaCtb + ')';
                                document.getElementById('analiticoLoading').style.display = 'block';
                                document.getElementById('analiticoTableWrapper').style.display = 'none';
                                new bootstrap.Modal(document.getElementById('modalAnalitico')).show();

                                var sql = "SELECT LAN.CODEMP, LAN.REFERENCIA, LAN.NUMLOTE, LAN.NUMLANC, LAN.CODCTACTB, PLA.DESCRCTA, LAN.DTMOV," +
                                    "CASE WHEN LAN.TIPLANC = 'D' THEN -LAN.VLRLANC WHEN LAN.TIPLANC = 'R' THEN LAN.VLRLANC END AS VLRLANC," +
                                    "LAN.COMPLHIST, LAN.NUMDOC " +
                                    "FROM TCBLAN LAN LEFT JOIN TCBPLA PLA ON LAN.CODCTACTB = PLA.CODCTACTB " +
                                    "WHERE TRIM(TO_CHAR(LAN.CODCTACTB)) = TRIM(TO_CHAR(" + codCtaCtb + ")) " +
                                    "AND LAN.REFERENCIA BETWEEN (SELECT AD_INICIOSAFRA FROM TCSPRJ WHERE CODPROJ = " + safraAtual + ") " +
                                    "AND (SELECT AD_FINALSAFRA FROM TCSPRJ WHERE CODPROJ = " + safraAtual + ") " +
                                    "ORDER BY LAN.REFERENCIA, LAN.NUMLOTE, LAN.NUMLANC";

                                callSnkService(sql, function (rows) {
                                    var tbody = document.querySelector('#tableAnalitico tbody');
                                    tbody.innerHTML = '';
                                    for (var k = 0; k < rows.length; k++) {
                                        var row = rows[k];
                                        var valor = row[7];
                                        var clsV = (valor < 0) ? 'color:#ef4444;font-weight:700;' : 'color:#10b981;font-weight:700;';
                                        var tr = document.createElement('tr');
                                        tr.innerHTML =
                                            '<td>' + (row[0] || '') + '</td>' +
                                            '<td>' + fmtDate(row[1]) + '</td>' +
                                            '<td>' + (row[2] || '') + '</td>' +
                                            '<td>' + (row[3] || '') + '</td>' +
                                            '<td><strong style="font-size:0.95rem;">' + (row[4] || '') + '</strong></td>' +
                                            '<td>' + (row[5] || '') + '</td>' +
                                            '<td>' + fmtDate(row[6]) + '</td>' +
                                            '<td style="' + clsV + '">' + fmtMoney(valor) + '</td>' +
                                            '<td style="max-width:300px;overflow:hidden;text-overflow:ellipsis;" title="' + (row[8] || '') + '">' + (row[8] || '') + '</td>' +
                                            '<td>' + (row[9] || '-') + '</td>';
                                        tbody.appendChild(tr);
                                    }
                                    document.getElementById('analiticoLoading').style.display = 'none';
                                    document.getElementById('analiticoTableWrapper').style.display = 'block';
                                });
                            }
                        </script>
                    </body>

                    </html>