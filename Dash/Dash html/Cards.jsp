<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored ="false"%>
<!DOCTYPE html>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="snk" uri="/WEB-INF/tld/sankhyaUtil.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pendentes de Faturamento por Coleta</title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.9.1/chart.min.js"></script>
<style>
    body {
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        margin: 0;
        padding: 20px;
        background-color: #f5f5f5;
    }
    
    .container {
        width: 590px;
        height: 542px;
        position: fixed;
        margin: 0 auto;
        background: white;
        border-radius: 8px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        padding: 20px 25px 50px 25px;
        overflow: hidden;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
    }
    
    .header {
        text-align: left;
        margin-bottom: 30px;
    }
    
    .title {
        font-size: 16px;
        font-weight: bold;
        color: #333;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        margin: 0;
    }
    
    .chart-container {
        position: relative;
        height: 400px; /* Reduzindo para dar espaço aos valores */
        background: #fafafa;
        border-radius: 6px;
        padding: 40px 20px 20px 20px; /* Mais padding no topo */
    }

    #billingChart {
        margin-left: -60px;
    }
    
    .stats-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 20px;
        margin-top: 30px;
    }
    
    .stat-card {
        padding: 20px;
        border-radius: 8px;
        text-align: center;
    }
    
    .stat-card.total { background-color: #e3f2fd; }
    .stat-card.max { background-color: #e8f5e8; }
    .stat-card.min { background-color: #ffebee; }
    .stat-card.avg { background-color: #f3e5f5; }
    
    .stat-label {
        font-size: 14px;
        font-weight: 600;
        margin-bottom: 8px;
    }
    
    .stat-value {
        font-size: 18px;
        font-weight: bold;
    }
    
    .total .stat-label { color: #1976d2; }
    .total .stat-value { color: #0d47a1; }
    .max .stat-label { color: #388e3c; }
    .max .stat-value { color: #1b5e20; }
    .min .stat-label { color: #d32f2f; }
    .min .stat-value { color: #b71c1c; }
    .avg .stat-label { color: #7b1fa2; }
    .avg .stat-value { color: #4a148c; }
    
    .note {
        margin-top: 25px;
        padding: 15px;
        background-color: #f8f9fa;
        border-left: 4px solid #1e40af;
        font-size: 13px;
        color: #666;
    }
</style>
 <snk:load/>
</head>

<snk:query var="Pedidos_pend">
SELECT 
    SUM(VLRNOTA) AS VLTNOTS,
    PENDENTE,
    TO_CHAR(AD_DTCOLETAPREVISTA, 'Month/YYYY', 'NLS_DATE_LANGUAGE=PORTUGUESE') AS MES
FROM TGFCAB 
WHERE PENDENTE = 'S' 
  AND STATUSNOTA = 'L'
  AND CODTIPOPER = 1009
  AND AD_DTCOLETAPREVISTA IS NOT NULL
  AND AD_DTCOLETAPREVISTA >= TRUNC(SYSDATE, 'MM')
  AND (SELECT COUNT(*) FROM TSILIB WHERE NUCHAVE = TGFCAB.NUNOTA AND TABELA IN ('TGFITE','TGFCAB') AND DHLIB IS NULL) = 0 
  AND (SELECT COUNT(*) FROM TSILIB WHERE NUCHAVE = TGFCAB.NUNOTA AND TABELA IN ('TGFITE','TGFCAB') AND REPROVADO ='S') = 0 
  AND DTNEG >= '01/07/2025'
GROUP BY 
    PENDENTE,
    TO_CHAR(AD_DTCOLETAPREVISTA, 'Month/YYYY', 'NLS_DATE_LANGUAGE=PORTUGUESE')
ORDER BY 
    MIN(AD_DTCOLETAPREVISTA)
</snk:query>

<body>
    <div class="container">
        <div class="chart-container">
            <canvas id="billingChart"></canvas>
        </div>
    </div>

    <script>
    // Arrays preenchidos com dados da query
    const labels = [];
    const values = [];
    
    <c:forEach items="${Pedidos_pend.rows}" var="row">
        labels.push("${row.MES}");
        values.push(${row.VLTNOTS});
    </c:forEach>

    // Dados do gráfico
    const chartData = {
        labels: labels,
        datasets: [{
            data: values,
            backgroundColor: '#1e40af',
            borderColor: '#1e40af',
            borderWidth: 1
        }]
    };

    // Função para formatar valores em Real
    function formatCurrency(value) {
        return value.toLocaleString('pt-BR', {
            style: 'currency',
            currency: 'BRL'
        });
    }

    // Configuração do gráfico
    const config = {
        type: 'bar',
        data: chartData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            layout: {
                padding: {
                    top: 50,  // Espaço extra no topo para os valores
                    bottom: 10,
                    left: 10,
                    right: 10
                }
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return 'Valor: ' + formatCurrency(context.parsed.y);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    // Adiciona margem extra no topo
                    suggestedMax: function(context) {
                        const max = Math.max(...values);
                        return max * 1.15; // 15% a mais que o valor máximo
                    },
                    ticks: {
                        callback: function(value) {
                            if (value >= 1000000) return (value / 1000000).toFixed(0) + 'M';
                            if (value >= 1000) return (value / 1000).toFixed(0) + 'K';
                            return value;
                        }
                    },
                    grid: { color: '#e0e0e0' }
                },
                x: {
                    ticks: { maxRotation: 45, minRotation: 45 },
                    grid: { color: '#e0e0e0' }
                }
            },
            animation: {
                onComplete: function() {
                    const chart = this;
                    const ctx = chart.ctx;
                    ctx.font = 'bold 11px Arial'; // Font menor
                    ctx.fillStyle = '#333';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'bottom';
                    
                    chart.data.datasets.forEach((dataset, i) => {
                        const meta = chart.getDatasetMeta(i);
                        meta.data.forEach((bar, index) => {
                            const data = dataset.data[index];
                            // Posiciona o texto com mais espaço
                            ctx.fillText(formatCurrency(data), bar.x, bar.y - 8);
                        });
                    });
                }
            }
        }
    };

    // Calcular suggestedMax dinamicamente
    if (values.length > 0) {
        const maxValue = Math.max(...values);
        config.options.scales.y.suggestedMax = maxValue * 1.15;
    }

    // Criar o gráfico
    const ctx = document.getElementById('billingChart').getContext('2d');
    const billingChart = new Chart(ctx, config);

    // Calcular e exibir estatísticas
    function updateStats() {
        const values = chartData.datasets[0].data;
        const total = values.reduce((sum, value) => sum + value, 0);
        const max = Math.max(...values);
        const min = Math.min(...values);
        const avg = total / values.length;

        // Se existirem os elementos, atualiza
        if (document.getElementById('totalValue')) {
            document.getElementById('totalValue').textContent = formatCurrency(total);
            document.getElementById('maxValue').textContent = formatCurrency(max);
            document.getElementById('minValue').textContent = formatCurrency(min);
            document.getElementById('avgValue').textContent = formatCurrency(avg);
        }
    }

    updateStats();
    </script>
</body>
</html>