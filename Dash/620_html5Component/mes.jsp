<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored ="false"%>
<!DOCTYPE html>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="snk" uri="/WEB-INF/tld/sankhyaUtil.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8" />
  <title>Acumulado Safra Atual x Safra Anterior — Pizza</title>
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/chartjs-plugin-datalabels"></script>
  <style>
    :root {
      --bg:#f7f7f8;
      --card:#ffffff;
      --border:#e6e6e6;
      --text:#222;
      --muted:#666;
      --radius:12px;
    }
    html, body {
      margin: 0;
      padding: 0;
      height: 100%;
      width: 100%;
      background: var(--bg);
      overflow: hidden;
      font-family: system-ui,-apple-system,Segoe UI,Roboto,Ubuntu,"Helvetica Neue",Arial;
    }
    .fixed-container {
      width: 590px;
      height: 542px;
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: var(--bg);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      box-shadow: 0 6px 20px rgba(0,0,0,.06);
      display: flex;
      flex-direction: column;
      padding: 8px;
      box-sizing: border-box;
    }
    h2 {
      font-size: 16px;
      font-weight: 700;
      margin: 0 0 6px;
      text-align: center;
    }
    .container {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8px;
      overflow: hidden;
    }
    .cards {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 6px;
      height: 150px;
    }
    .card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      display: flex;
      flex-direction: column;
      font-size: 11px;
      overflow: hidden;
    }
    .card .header {
      font-weight: 600;
      font-size: 11px;
      background: #fafafa;
      padding: 4px 6px;
      border-bottom: 1px solid var(--border);
    }
    .wrap {
      padding: 4px;
      flex: 1;
    }
    table {
      width: 100%;
      border-collapse: collapse;
    }
    th, td {
      padding: 4px;
      font-size: 11px;
      text-align: left;
    }
    th { color: var(--muted); font-weight: 600; }
    .charts {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 6px;
      height: 280px;
    }
    .chart-card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 6px;
      display: flex;
      flex-direction: column;
    }
    .chart-title {
      margin: 0 0 4px;
      font-size: 12px;
      font-weight: 600;
      text-align: center;
    }
    .chart-card canvas {
      max-height: 200px;
    }
    .legend {
      display: flex;
      gap: 6px;
      justify-content: center;
      margin-top: 4px;
      font-size: 10px;
      flex-wrap: wrap;
    }
    .dot {
      display: inline-block;
      width: 10px;
      height: 10px;
      border-radius: 3px;
      margin-right: 4px;
      vertical-align: middle;
    }
    .c-realizado { background:#2CA02C; }
    .c-previsto  { background:#FF7F0E; }
    .c-faltante  { background:#FFD700; }
    .c-anterior  { background:#1F77B4; }


    .info {
    margin: 4px 0 0;
    font-size: 10px;
    text-align: center;
    color: var(--muted);
    }
  </style>
  <snk:load/>
</head>
<body>

    <!-- QUERY GRÁFICO -->
  <snk:query var="grafico" dataSource="MGEDS">
SELECT
    SUM(QTDPREV) AS QTDPREV,
    SUM(QTDREAL) AS QTDREAL,
    SUM(VLR_PREV) AS VLR_PREV,
    SUM(VLR_REAL) AS VLR_REAL,
    CASE WHEN SUM(QTDPREV) = 0 THEN 0 ELSE SUM(QTDREAL) * 100 / NULLIF(SUM(QTDPREV), 0) END AS PERC,
    CASE WHEN SUM(VLR_PREV) = 0 THEN 0 ELSE NVL(SUM(VLR_REAL) * 100 / NULLIF(SUM(VLR_PREV), 0), 0) END AS PERC_VLR,
    'Safra Atual' AS PERIOD,
    'GREEN' AS COLOR
FROM
(
    SELECT
        DTREF,
        CODMETA,
        CODVEND,
        APELIDO,
        CODGER,
        CODPARC,
        PARCEIRO,
        MARCA,
        CODGRUPOPROD,
        SUM(QTDPREV) AS QTDPREV,
        SUM(QTDREAL) AS QTDREAL,
        SUM(QTDPREV * PRECOLT) AS VLR_PREV,
        SUM(NVL(VLRREAL, 0)) AS VLR_REAL
    FROM
    (
        SELECT MET.CODMETA,
               MET.DTREF,
               NVL(MET.CODVEND, 0) AS CODVEND,
               NVL(VEN.APELIDO, 0) AS APELIDO,
               NVL(VEN.CODGER, 0) AS CODGER,
               NVL(MET.CODPARC, 0) AS CODPARC,
               NVL(PAR.RAZAOSOCIAL, 0) AS PARCEIRO,
               NVL(MET.MARCA, 0) AS MARCA,
               NVL(VGF.CODGRUPOPROD, 0) AS CODGRUPOPROD,
               NVL(VGF.CODCENCUS, 0) AS CODCENCUS,
               NVL(MET.QTDPREV, 0) AS QTDPREV,
               SUM(NVL(VGF.QTD, 0)) AS QTDREAL,
               NVL(PRC.VLRVENDALT, 0) AS PRECOLT,
               SUM(NVL(VGF.VLR, 0)) AS VLRREAL
        FROM TGFMET MET
        LEFT JOIN VGF_VENDAS_SATIS VGF 
            ON MET.DTREF = TRUNC(VGF.DTMOV, 'MM')
            AND MET.CODVEND = VGF.CODVEND
            AND MET.CODPARC = VGF.CODPARC
            AND MET.MARCA = VGF.MARCA
            AND VGF.BONIFICACAO = 'N'
        LEFT JOIN AD_PRECOMARCA PRC 
            ON MET.MARCA = PRC.MARCA 
            AND PRC.CODMETA = MET.CODMETA 
            AND PRC.DTREF = (
                SELECT MAX(DTREF) 
                FROM AD_PRECOMARCA 
                WHERE CODMETA = MET.CODMETA 
                  AND DTREF <= MET.DTREF 
                  AND MARCA = MET.MARCA
            )
        LEFT JOIN TGFPAR PAR ON MET.CODPARC = PAR.CODPARC
        LEFT JOIN TGFVEN VEN ON MET.CODVEND = VEN.CODVEND
        GROUP BY MET.CODMETA, MET.DTREF, NVL(MET.CODVEND, 0), NVL(VEN.APELIDO, 0),
                 NVL(VEN.CODGER, 0), NVL(MET.CODPARC, 0), NVL(PAR.RAZAOSOCIAL, 0),
                 NVL(MET.MARCA, 0), NVL(VGF.CODGRUPOPROD, 0), NVL(VGF.CODCENCUS, 0),
                 NVL(MET.QTDPREV, 0), NVL(PRC.VLRVENDALT, 0)
    )
    WHERE CODMETA = 4
      AND TRUNC(DTREF, 'MM') = TRUNC(SYSDATE, 'MM')
      AND (QTDPREV <> 0 OR QTDREAL <> 0 OR VLRREAL <> 0)
    GROUP BY DTREF, CODMETA, CODVEND, APELIDO, CODGER, CODPARC, PARCEIRO, MARCA, CODGRUPOPROD
)
UNION ALL
SELECT
    SUM(QTDPREV) AS QTDPREV,
    SUM(QTDREAL) AS QTDREAL,
    SUM(VLR_PREV) AS VLR_PREV,  -- valor previsto da safra anterior agora incluso
    SUM(VLR_REAL) AS VLR_REAL,
    CASE WHEN SUM(QTDPREV) = 0 THEN 0 ELSE SUM(QTDREAL) * 100 / NULLIF(SUM(QTDPREV), 0) END AS PERC,
    CASE WHEN SUM(VLR_PREV) = 0 THEN 0 ELSE NVL(SUM(VLR_REAL) * 100 / NULLIF(SUM(VLR_PREV), 0), 0) END AS PERC_VLR,
    'Safra Anterior' AS PERIOD,
    'RED' AS COLOR
FROM
(
    SELECT
        DTREF,
        CODMETA,
        CODVEND,
        APELIDO,
        CODGER,
        CODPARC,
        PARCEIRO,
        MARCA,
        CODGRUPOPROD,
        SUM(QTDPREV) AS QTDPREV,
        SUM(QTDREAL) AS QTDREAL,
        SUM(QTDPREV * PRECOLT) AS VLR_PREV,  -- cálculo do previsto
        SUM(NVL(VLRREAL, 0)) AS VLR_REAL
    FROM
    (
        SELECT MET.CODMETA,
               MET.DTREF,
               NVL(MET.CODVEND, 0) AS CODVEND,
               NVL(VEN.APELIDO, 0) AS APELIDO,
               NVL(VEN.CODGER, 0) AS CODGER,
               NVL(MET.CODPARC, 0) AS CODPARC,
               NVL(PAR.RAZAOSOCIAL, 0) AS PARCEIRO,
               NVL(MET.MARCA, 0) AS MARCA,
               NVL(VGF.CODGRUPOPROD, 0) AS CODGRUPOPROD,
               NVL(VGF.CODCENCUS, 0) AS CODCENCUS,
               NVL(MET.QTDPREV, 0) AS QTDPREV,
               SUM(NVL(VGF.QTD, 0)) AS QTDREAL,
               NVL(PRC.VLRVENDALT, 0) AS PRECOLT,
               SUM(NVL(VGF.VLR, 0)) AS VLRREAL
        FROM TGFMET MET
        LEFT JOIN VGF_VENDAS_SATIS VGF 
            ON MET.DTREF = TRUNC(VGF.DTMOV, 'MM')
            AND MET.CODVEND = VGF.CODVEND
            AND MET.CODPARC = VGF.CODPARC
            AND MET.MARCA = VGF.MARCA
            AND VGF.BONIFICACAO = 'N'
        LEFT JOIN AD_PRECOMARCA PRC 
            ON MET.MARCA = PRC.MARCA 
            AND PRC.CODMETA = MET.CODMETA 
            AND PRC.DTREF = (
                SELECT MAX(DTREF) 
                FROM AD_PRECOMARCA 
                WHERE CODMETA = MET.CODMETA 
                  AND DTREF <= MET.DTREF 
                  AND MARCA = MET.MARCA
            )
        LEFT JOIN TGFPAR PAR ON MET.CODPARC = PAR.CODPARC
        LEFT JOIN TGFVEN VEN ON MET.CODVEND = VEN.CODVEND
        GROUP BY MET.CODMETA, MET.DTREF, NVL(MET.CODVEND, 0), NVL(VEN.APELIDO, 0),
                 NVL(VEN.CODGER, 0), NVL(MET.CODPARC, 0), NVL(PAR.RAZAOSOCIAL, 0),
                 NVL(MET.MARCA, 0), NVL(VGF.CODGRUPOPROD, 0), NVL(VGF.CODCENCUS, 0),
                 NVL(MET.QTDPREV, 0), NVL(PRC.VLRVENDALT, 0)
    )
    WHERE CODMETA = 4
    AND DTREF >= TO_DATE('01/07/' || TO_CHAR(EXTRACT(YEAR FROM SYSDATE)-1), 'DD/MM/YYYY')
    AND DTREF <= LAST_DAY(ADD_MONTHS(TRUNC(SYSDATE,'MM'), -12))
    GROUP BY DTREF, CODMETA, CODVEND, APELIDO, CODGER, CODPARC, PARCEIRO, MARCA, CODGRUPOPROD
)
ORDER BY PERIOD DESC

  </snk:query>

  <!-- QUERY TABELA -->
  <snk:query var="tabela" dataSource="MGEDS">
    WITH Dados AS (
    SELECT
        MET.DTREF,
        MET.CODMETA,
        NVL(MET.CODVEND, 0) AS CODVEND,
        NVL(VEN.APELIDO, 0) AS APELIDO,
        NVL(VEN.CODGER, 0) AS CODGER,
        NVL(MET.CODPARC, 0) AS CODPARC,
        NVL(PAR.RAZAOSOCIAL, 0) AS PARCEIRO,
        NVL(MET.MARCA, 0) AS MARCA,
        NVL(VGF.CODGRUPOPROD, 0) AS CODGRUPOPROD,
        NVL(VGF.CODCENCUS, 0) AS CODCENCUS,
        NVL(MET.QTDPREV, 0) AS QTDPREV,
        SUM(NVL(VGF.QTD, 0)) AS QTDREAL,
        NVL(PRC.VLRVENDALT, 0) AS PRECOLT,
        SUM(NVL(VGF.VLR, 0)) AS VLRREAL
    FROM TGFMET MET
    LEFT JOIN VGF_VENDAS_SATIS VGF ON MET.DTREF = TRUNC(VGF.DTMOV, 'MM') 
        AND MET.CODVEND = VGF.CODVEND 
        AND MET.CODPARC = VGF.CODPARC 
        AND MET.MARCA = VGF.MARCA 
        AND VGF.BONIFICACAO = 'N'
    LEFT JOIN AD_PRECOMARCA PRC ON (MET.MARCA = PRC.MARCA 
        AND PRC.CODMETA = MET.CODMETA 
        AND PRC.DTREF = (SELECT MAX(DTREF) 
                         FROM AD_PRECOMARCA 
                         WHERE CODMETA = MET.CODMETA 
                           AND DTREF <= MET.DTREF 
                           AND MARCA = MET.MARCA))
    LEFT JOIN TGFPAR PAR ON MET.CODPARC = PAR.CODPARC
    LEFT JOIN TGFVEN VEN ON MET.CODVEND = VEN.CODVEND
    GROUP BY MET.CODMETA, MET.DTREF, NVL(MET.CODVEND, 0), NVL(VEN.APELIDO, 0), 
             NVL(VEN.CODGER, 0), NVL(MET.CODPARC, 0), NVL(PAR.RAZAOSOCIAL, 0), 
             NVL(MET.MARCA, 0), NVL(VGF.CODGRUPOPROD, 0), NVL(VGF.CODCENCUS, 0), 
             NVL(MET.QTDPREV, 0), NVL(PRC.VLRVENDALT, 0)
),
PERIOD_ATUAL AS (
    SELECT
        SUM(QTDPREV) AS QTDPREV_ATUAL,
        SUM(QTDREAL) AS QTDREAL_ATUAL,
        SUM(QTDPREV * PRECOLT) AS VLR_PREV_ATUAL,
        SUM(VLRREAL) AS VLR_REAL_ATUAL
    FROM Dados
    WHERE 
        CODMETA = 4
        AND TRUNC(DTREF, 'MM') = TRUNC(SYSDATE, 'MM')
        AND (QTDPREV <> 0 OR QTDREAL <> 0 OR VLRREAL <> 0)
),
PERIOD_ANTERIOR AS (
    SELECT
        SUM(QTDPREV) AS QTDPREV_ANTERIOR,
        SUM(QTDREAL) AS QTDREAL_ANTERIOR,
        SUM(QTDPREV * PRECOLT) AS VLR_PREV_ANTERIOR,
        SUM(VLRREAL) AS VLR_REAL_ANTERIOR
    FROM Dados
    WHERE 
        CODMETA = 4
        AND DTREF >= TO_DATE('01/07/' || TO_CHAR(EXTRACT(YEAR FROM SYSDATE)-1), 'DD/MM/YYYY')
        AND DTREF <= LAST_DAY(ADD_MONTHS(TRUNC(SYSDATE,'MM'), -12))
)
SELECT
    'R$ ' || TO_CHAR(PERIOD_ATUAL.VLR_PREV_ATUAL, 'FM999G999G999G990D00') AS VLR_PREV_SAFRA_ATUAL,
    'R$ ' || TO_CHAR(PERIOD_ATUAL.VLR_REAL_ATUAL, 'FM999G999G999G990D00') AS VLR_REAL_SAFRA_ATUAL,
    'R$ ' || TO_CHAR(PERIOD_ANTERIOR.VLR_PREV_ANTERIOR, 'FM999G999G999G990D00') AS VLR_PREV_SAFRA_ANTERIOR,
    'R$ ' || TO_CHAR(PERIOD_ANTERIOR.VLR_REAL_ANTERIOR, 'FM999G999G999G990D00') AS VLR_REAL_SAFRA_ANTERIOR,
    'Safra Atual' AS PERIODO_ATUAL,
    'Safra Anterior' AS PERIODO_ANTERIOR,
    CASE WHEN PERIOD_ATUAL.VLR_PREV_ATUAL IS NOT NULL THEN 'GREEN' ELSE 'RED' END AS COLOR,
    'R$ ' || TO_CHAR(PERIOD_ATUAL.VLR_PREV_ATUAL - PERIOD_ATUAL.VLR_REAL_ATUAL, 'FM999G999G999G990D00') AS VLR_FALTA_BATER
FROM PERIOD_ATUAL
CROSS JOIN PERIOD_ANTERIOR
ORDER BY PERIODO_ATUAL DESC, PERIODO_ANTERIOR DESC


  </snk:query>
  <div class="fixed-container">
    <div class="container">
      <h2>ACUMULADO SAFRA ATUAL X SAFRA ANTERIOR</h2>

      <div class="cards">
        <div class="card">
          <div class="header">Safra &bull; Previsto &bull; Realizado</div>
          <div class="wrap">
            <table>
              <thead>
                <tr>
                  <th>Safra</th>
                  <th>Previsto</th>
                  <th>Realizado</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="row" items="${tabela.rows}">
                  <tr>
                    <td>${row.PERIODO_ATUAL}</td>
                    <td>${row.VLR_PREV_SAFRA_ATUAL}</td>
                    <td>${row.VLR_REAL_SAFRA_ATUAL}</td>
                  </tr>
                  <tr>
                    <td>${row.PERIODO_ANTERIOR}</td>
                    <td>${row.VLR_PREV_SAFRA_ANTERIOR}</td>
                    <td>${row.VLR_REAL_SAFRA_ANTERIOR}</td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>

        <div class="card">
          <div class="header">Safra &bull; Faltante</div>
          <div class="wrap">
            <table>
              <thead>
                <tr>
                  <th>Safra</th>
                  <th>Faltante</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="row" items="${tabela.rows}">
                  <tr>
                    <td>${row.PERIODO_ATUAL}</td>
                    <td>${row.VLR_FALTA_BATER}</td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="charts">
        <div class="chart-card">
          <p class="chart-title">Safra Atual</p>
          <canvas id="chartSafraAtual"></canvas>
          <div class="legend">
            <span><i class="dot c-previsto"></i>Previsto</span>
            <span><i class="dot c-realizado"></i>Realizado</span>
            <span><i class="dot c-faltante"></i>Faltante</span>
          </div>
          <p class="info">Período analisado: início da safra até a data atual</p>
        </div>

        <div class="chart-card">
          <p class="chart-title">Safra Anterior</p>
          <canvas id="chartSafraAnterior"></canvas>
          <div class="legend">
            <span><i class="dot c-previsto"></i>Previsto</span>
            <span><i class="dot c-anterior"></i>Realizado</span>
            <span><i class="dot c-faltante"></i>Faltante</span>
          </div>
           <p class="info">Período analisado: início da safra anterior até o fim do mês vigente</p>
        </div>
      </div>
    </div>
  </div>

<script>
  let previstoAtual = 0, realizadoAtual = 0, faltanteAtual = 0;
  let previstoAnterior = 0, realizadoAnterior = 0, faltanteAnterior = 0;

  <c:forEach var="row" items="${grafico.rows}">
    <c:if test="${row.PERIOD eq 'Safra Atual'}">
      previstoAtual  = ${row.VLR_PREV};
      realizadoAtual = ${row.VLR_REAL};
      faltanteAtual  = previstoAtual - realizadoAtual;
    </c:if>
    <c:if test="${row.PERIOD eq 'Safra Anterior'}">
      previstoAnterior  = ${row.VLR_PREV};
      realizadoAnterior = ${row.VLR_REAL};
      faltanteAnterior  = previstoAnterior - realizadoAnterior;
    </c:if>
  </c:forEach>

  new Chart(document.getElementById('chartSafraAtual'), {
    type: 'pie',
    data: {
      labels: ['Previsto', 'Realizado', 'Faltante'],
      datasets: [{
        data: [previstoAtual, realizadoAtual, faltanteAtual],
        backgroundColor: ['#FF7F0E', '#2CA02C', '#FFD700'],
        borderColor: '#fff',
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        datalabels: {
          color: '#000',
          font: { size: 10, weight: 'bold' },
          formatter: (value, ctx) => {
            const sum = ctx.chart.data.datasets[0].data.reduce((a,b) => a+b,0);
            return ((value/sum)*100).toFixed(1) + '%';
          }
        }
      }
    },
    plugins: [ChartDataLabels]
  });

  new Chart(document.getElementById('chartSafraAnterior'), {
    type: 'pie',
    data: {
      labels: ['Previsto', 'Realizado', 'Faltante'],
      datasets: [{
        data: [previstoAnterior, realizadoAnterior, faltanteAnterior],
        backgroundColor: ['#FF7F0E', '#1F77B4', '#FFD700'],
        borderColor: '#fff',
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        datalabels: {
          color: '#000',
          font: { size: 10, weight: 'bold' },
          formatter: (value, ctx) => {
            const sum = ctx.chart.data.datasets[0].data.reduce((a,b) => a+b,0);
            return ((value/sum)*100).toFixed(1) + '%';
          }
        }
      }
    },
    plugins: [ChartDataLabels]
  });
</script>
</body>
</html>
