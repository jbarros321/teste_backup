<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8" isELIgnored="false"%>
<!DOCTYPE html>
<%@ page import="java.util.*" %> <%@ taglib
uri="http://java.sun.com/jstl/core_rt" prefix="c" %> <%@ taglib prefix="snk"
uri="/WEB-INF/tld/sankhyaUtil.tld" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>
<html lang="pt-BR">
  <head>
    <meta charset="UTF-8" />
    <title>Dashboard Financeiro - CICRANO</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js"></script>
    <style>
      :root {
        --bg: #f7f7f8;
        --card: #ffffff;
        --border: #e6e6e6;
        --text: #222;
        --muted: #666;
        --radius: 12px;
      }

      html,
      body {
        margin: 0;
        padding: 0;
        height: 100%;
        width: 100%;
        background: var(--bg);
        overflow: hidden;
        font-family: system-ui, -apple-system, Segoe UI, Roboto, Ubuntu,
          "Helvetica Neue", Arial;
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
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
        display: flex;
        flex-direction: column;
        padding: 8px;
        box-sizing: border-box;
      }

      h1 {
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
        height: 120px;
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

      .chart-container {
        background: var(--card);
        border: 1px solid var(--border);
        border-radius: var(--radius);
        padding: 8px;
        display: flex;
        flex-direction: column;
        height: 300px;
      }

      .chart-title {
        margin: 0 0 4px;
        font-size: 12px;
        font-weight: 600;
        text-align: center;
      }

      .chart-container canvas {
        max-height: 250px;
      }

      .info {
        margin: 4px 0 0;
        font-size: 10px;
        text-align: center;
        color: var(--muted);
      }

      .receita {
        color: #10b981;
        font-weight: bold;
      }
      .despesa {
        color: #ef4444;
        font-weight: bold;
      }
      .saldo {
        color: #667eea;
        font-weight: bold;
      }
    </style>
    <snk:load />
  </head>
  <body>
    <!-- QUERY FLUXO DE CAIXA -->
    <snk:query var="fluxoCaixa">
      SELECT TRUNC(FIN.DHBAIXA) AS DATA, SUM(CASE WHEN FIN.RECDESP = 1 THEN
      FIN.VLRDESDOB ELSE 0 END) AS RECEITAS, SUM(CASE WHEN FIN.RECDESP = -1 THEN
      FIN.VLRDESDOB ELSE 0 END) AS DESPESAS FROM TGFFIN FIN WHERE FIN.DHBAIXA IS
      NOT NULL AND FIN.PROVISAO = 'N' AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99)
      AND TRUNC(FIN.DHBAIXA, 'MM') = TRUNC(SYSDATE, 'MM') GROUP BY
      TRUNC(FIN.DHBAIXA) ORDER BY TRUNC(FIN.DHBAIXA)
    </snk:query>

    <!-- QUERY PROVISÃO RECEITA -->
    <snk:query var="provisaoReceita">
      SELECT TRUNC(FIN.DTVENC) AS DATA, SUM(FIN.VLRDESDOB) AS VALOR FROM TGFFIN
      FIN WHERE FIN.RECDESP = 1 AND FIN.PROVISAO = 'S' AND FIN.DHBAIXA IS NULL
      AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99) AND TRUNC(FIN.DTVENC, 'MM') =
      TRUNC(SYSDATE, 'MM') GROUP BY TRUNC(FIN.DTVENC) ORDER BY TRUNC(FIN.DTVENC)
    </snk:query>

    <!-- QUERY PROVISÃO DESPESA -->
    <snk:query var="provisaoDespesa">
      SELECT TRUNC(FIN.DTVENC) AS DATA, SUM(FIN.VLRDESDOB) AS VALOR FROM TGFFIN
      FIN WHERE FIN.RECDESP = -1 AND FIN.PROVISAO = 'S' AND FIN.DHBAIXA IS NULL
      AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99) AND TRUNC(FIN.DTVENC, 'MM') =
      TRUNC(SYSDATE, 'MM') GROUP BY TRUNC(FIN.DTVENC) ORDER BY TRUNC(FIN.DTVENC)
    </snk:query>

    <!-- QUERY TOTAIS -->
    <snk:query var="totais">
      SELECT SUM(CASE WHEN FIN.RECDESP = 1 AND FIN.PROVISAO = 'N' THEN
      FIN.VLRDESDOB ELSE 0 END) AS TOTAL_RECEITAS_REAL, SUM(CASE WHEN
      FIN.RECDESP = -1 AND FIN.PROVISAO = 'N' THEN FIN.VLRDESDOB ELSE 0 END) AS
      TOTAL_DESPESAS_REAL, SUM(CASE WHEN FIN.RECDESP = 1 AND FIN.PROVISAO = 'S'
      AND FIN.DHBAIXA IS NULL THEN FIN.VLRDESDOB ELSE 0 END) AS
      TOTAL_RECEITAS_PROV, SUM(CASE WHEN FIN.RECDESP = -1 AND FIN.PROVISAO = 'S'
      AND FIN.DHBAIXA IS NULL THEN FIN.VLRDESDOB ELSE 0 END) AS
      TOTAL_DESPESAS_PROV FROM TGFFIN FIN WHERE TRUNC(FIN.DHBAIXA, 'MM') =
      TRUNC(SYSDATE, 'MM') AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99) AND
      ((FIN.PROVISAO = 'N' AND FIN.DHBAIXA IS NOT NULL) OR (FIN.PROVISAO = 'S'
      AND FIN.DHBAIXA IS NULL))
    </snk:query>

    <div class="fixed-container">
      <div class="container">
        <h1>📊 DASHBOARD FINANCEIRO - CICRANO</h1>

        <!-- CARDS COM TOTAIS -->
        <div class="cards">
          <c:forEach var="row" items="${totais.rows}">
            <div class="card">
              <div class="header">Receitas Real</div>
              <div class="wrap">
                <div class="receita">
                  R$
                  <fmt:formatNumber
                    value="${row.TOTAL_RECEITAS_REAL}"
                    minFractionDigits="2"
                    maxFractionDigits="2"
                  />
                </div>
              </div>
            </div>

            <div class="card">
              <div class="header">Despesas Real</div>
              <div class="wrap">
                <div class="despesa">
                  R$
                  <fmt:formatNumber
                    value="${row.TOTAL_DESPESAS_REAL}"
                    minFractionDigits="2"
                    maxFractionDigits="2"
                  />
                </div>
              </div>
            </div>

            <div class="card">
              <div class="header">Saldo Real</div>
              <div class="wrap">
                <div class="saldo">
                  R$
                  <fmt:formatNumber
                    value="${row.TOTAL_RECEITAS_REAL - row.TOTAL_DESPESAS_REAL}"
                    minFractionDigits="2"
                    maxFractionDigits="2"
                  />
                </div>
              </div>
            </div>

            <div class="card">
              <div class="header">Provisão Receita</div>
              <div class="wrap">
                <div class="receita">
                  R$
                  <fmt:formatNumber
                    value="${row.TOTAL_RECEITAS_PROV}"
                    minFractionDigits="2"
                    maxFractionDigits="2"
                  />
                </div>
              </div>
            </div>

            <div class="card">
              <div class="header">Provisão Despesa</div>
              <div class="wrap">
                <div class="despesa">
                  R$
                  <fmt:formatNumber
                    value="${row.TOTAL_DESPESAS_PROV}"
                    minFractionDigits="2"
                    maxFractionDigits="2"
                  />
                </div>
              </div>
            </div>

            <div class="card">
              <div class="header">Período</div>
              <div class="wrap">
                <div style="font-size: 10px; color: var(--muted)">
                  Mês Atual
                </div>
              </div>
            </div>
          </c:forEach>
        </div>

        <!-- GRÁFICOS -->
        <div class="chart-container">
          <p class="chart-title">Fluxo de Caixa Real - Mês Atual</p>
          <canvas id="chartFluxo"></canvas>
        </div>

        <div class="chart-container">
          <p class="chart-title">Provisões - Mês Atual</p>
          <canvas id="chartProv"></canvas>
        </div>

        <p class="info">Dados do mês atual - Atualizado automaticamente</p>
      </div>
    </div>

    <script>
      // Dados Fluxo de Caixa
      const labelsFluxo = [];
      const receitasFluxo = [];
      const despesasFluxo = [];

      <c:forEach var="row" items="${fluxoCaixa.rows}">
        labelsFluxo.push("<fmt:formatDate value='${row.DATA}' pattern='dd/MM' />");
        receitasFluxo.push(<fmt:formatNumber value="${row.RECEITAS}" groupingUsed="false" />);
        despesasFluxo.push(<fmt:formatNumber value="${row.DESPESAS}" groupingUsed="false" />);
      </c:forEach>

      // Gráfico Fluxo de Caixa
      if (labelsFluxo.length > 0) {
        new Chart(document.getElementById('chartFluxo'), {
          type: 'line',
          data: {
            labels: labelsFluxo,
            datasets: [{
              label: 'Receitas',
              data: receitasFluxo,
              borderColor: '#10b981',
              backgroundColor: 'rgba(16, 185, 129, 0.1)',
              tension: 0.4
            }, {
              label: 'Despesas',
              data: despesasFluxo,
              borderColor: '#ef4444',
              backgroundColor: 'rgba(239, 68, 68, 0.1)',
              tension: 0.4
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { display: true, position: 'top' }
            },
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  callback: function(value) {
                    return 'R$ ' + value.toLocaleString('pt-BR');
                  }
                }
              }
            }
          }
        });
      }

      // Dados Provisões - CORREÇÃO: Usar objeto para mapear dados
      const labelsProv = [];
      const receitasProv = [];
      const despesasProv = [];
      const provDataMap = {};

      <c:forEach var="row" items="${provisaoReceita.rows}">
        provDataMap["<fmt:formatDate value='${row.DATA}' pattern='yyyy-MM-dd' />"] = {
          receita: <fmt:formatNumber value="${row.VALOR}" groupingUsed="false" />,
          despesa: 0
        };
      </c:forEach>

      <c:forEach var="row" items="${provisaoDespesa.rows}">
        var dataKey = "<fmt:formatDate value='${row.DATA}' pattern='yyyy-MM-dd' />";
        if (!provDataMap[dataKey]) {
          provDataMap[dataKey] = {
            receita: 0,
            despesa: <fmt:formatNumber value="${row.VALOR}" groupingUsed="false" />
          };
        } else {
          provDataMap[dataKey].despesa = <fmt:formatNumber value="${row.VALOR}" groupingUsed="false" />;
        }
      </c:forEach>

      // Converter mapa para arrays ordenados
      Object.keys(provDataMap).sort().forEach(function(dataKey) {
        var partes = dataKey.split('-');
        var label = partes[2] + '/' + partes[1];
        labelsProv.push(label);
        receitasProv.push(provDataMap[dataKey].receita);
        despesasProv.push(provDataMap[dataKey].despesa);
      });

      // Gráfico Provisões
      if (labelsProv.length > 0) {
        new Chart(document.getElementById('chartProv'), {
          type: 'bar',
          data: {
            labels: labelsProv,
            datasets: [{
              label: 'Provisão Receita',
              data: receitasProv,
              backgroundColor: 'rgba(16, 185, 129, 0.6)',
              borderColor: '#10b981',
              borderWidth: 1
            }, {
              label: 'Provisão Despesa',
              data: despesasProv,
              backgroundColor: 'rgba(239, 68, 68, 0.6)',
              borderColor: '#ef4444',
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { display: true, position: 'top' }
            },
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  callback: function(value) {
                    return 'R$ ' + value.toLocaleString('pt-BR');
                  }
                }
              }
            }
          }
        });
      }
    </script>
  </body>
</html>
