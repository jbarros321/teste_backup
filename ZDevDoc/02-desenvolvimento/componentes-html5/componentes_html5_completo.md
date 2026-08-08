# 🎨 Componentes HTML5 Sankhya - Guia Completo

## 🎯 Visão Geral

Os **Componentes HTML5** no Sankhya permitem criar interfaces web modernas e interativas usando JSP, CSS e JavaScript. Estes componentes são essenciais para dashboards, relatórios e aplicações web personalizadas.

## 🏗️ Estrutura Base

### **Template JSP Padrão**
```jsp
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="snk" uri="/WEB-INF/tld/sankhyaUtil.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
    <title>Componente HTML5</title>
    <link rel="stylesheet" type="text/css" href="${BASE_FOLDER}css/mainCSS.css">
    <snk:load />
</head>
<body>
    <!-- Conteúdo do componente -->
</body>
</html>
```

## 📊 Componentes de Dashboard

### **1. Grid de Cards (2x2)**
```jsp
<style>
body {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(2, 1fr);
    gap: 20px;
    height: 70vh;
    padding: 20px;
}

.card {
    background-color: #f0f0f0;
    border-radius: 10px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    cursor: pointer;
}

.card:hover {
    background-color: #e0e0e0;
}

.card svg {
    width: 50px;
    height: 50px;
    margin-bottom: 10px;
}

.card p {
    font-size: 35px;
    font-weight: bold;
}

.card p2 {
    font-size: 18px;
    font-weight: bold;
}
</style>

<c:forEach items="${dados.rows}" var="row">
    <div class="card" onclick="abrirRequisicoes()">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
        </svg>
        <p>${row.VALOR}</p>
        <p2>${row.DESCRICAO}</p2>
    </div>
</c:forEach>
```

### **2. Gráfico de Barras**
```jsp
<style>
.chart-container {
    width: 100%;
    height: 400px;
    margin: 20px 0;
}

.bar-chart {
    display: flex;
    align-items: end;
    height: 100%;
    gap: 10px;
    padding: 20px;
}

.bar {
    flex: 1;
    background: linear-gradient(to top, #007bff, #0056b3);
    border-radius: 4px 4px 0 0;
    position: relative;
    min-height: 20px;
}

.bar-label {
    position: absolute;
    bottom: -25px;
    left: 50%;
    transform: translateX(-50%);
    font-size: 12px;
    white-space: nowrap;
}

.bar-value {
    position: absolute;
    top: -25px;
    left: 50%;
    transform: translateX(-50%);
    font-size: 12px;
    font-weight: bold;
}
</style>

<div class="chart-container">
    <div class="bar-chart">
        <c:forEach items="${dados.rows}" var="row">
            <div class="bar" style="height: ${row.PERCENTUAL}%">
                <div class="bar-value">${row.VALOR}</div>
                <div class="bar-label">${row.CATEGORIA}</div>
            </div>
        </c:forEach>
    </div>
</div>
```

### **3. Tabela Responsiva**
```jsp
<style>
.table-container {
    width: 100%;
    overflow-x: auto;
    margin: 20px 0;
}

.data-table {
    width: 100%;
    border-collapse: collapse;
    background-color: white;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.data-table th,
.data-table td {
    padding: 12px;
    text-align: left;
    border-bottom: 1px solid #ddd;
}

.data-table th {
    background-color: #f8f9fa;
    font-weight: bold;
    color: #495057;
}

.data-table tr:hover {
    background-color: #f5f5f5;
}

.data-table .number {
    text-align: right;
}

.data-table .currency {
    text-align: right;
    color: #28a745;
    font-weight: bold;
}
</style>

<div class="table-container">
    <table class="data-table">
        <thead>
            <tr>
                <th>Cliente</th>
                <th>Data</th>
                <th class="number">Quantidade</th>
                <th class="currency">Valor</th>
                <th>Status</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${dados.rows}" var="row">
                <tr>
                    <td>${row.CLIENTE}</td>
                    <td><fmt:formatDate value="${row.DATA}" pattern="dd/MM/yyyy"/></td>
                    <td class="number">${row.QUANTIDADE}</td>
                    <td class="currency">R$ <fmt:formatNumber value="${row.VALOR}" pattern="#,##0.00"/></td>
                    <td>
                        <span class="status status-${row.STATUS}">${row.STATUS}</span>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
```

## 🎨 Componentes Interativos

### **1. Filtros Dinâmicos**
```jsp
<style>
.filter-container {
    background-color: #f8f9fa;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
}

.filter-row {
    display: flex;
    gap: 15px;
    align-items: end;
    flex-wrap: wrap;
}

.filter-group {
    display: flex;
    flex-direction: column;
    min-width: 200px;
}

.filter-group label {
    font-weight: bold;
    margin-bottom: 5px;
    color: #495057;
}

.filter-group input,
.filter-group select {
    padding: 8px 12px;
    border: 1px solid #ced4da;
    border-radius: 4px;
    font-size: 14px;
}

.filter-buttons {
    display: flex;
    gap: 10px;
}

.btn {
    padding: 8px 16px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    font-weight: bold;
}

.btn-primary {
    background-color: #007bff;
    color: white;
}

.btn-secondary {
    background-color: #6c757d;
    color: white;
}

.btn:hover {
    opacity: 0.9;
}
</style>

<div class="filter-container">
    <div class="filter-row">
        <div class="filter-group">
            <label for="dataInicio">Data Início:</label>
            <input type="date" id="dataInicio" name="dataInicio" value="${param.dataInicio}">
        </div>
        
        <div class="filter-group">
            <label for="dataFim">Data Fim:</label>
            <input type="date" id="dataFim" name="dataFim" value="${param.dataFim}">
        </div>
        
        <div class="filter-group">
            <label for="cliente">Cliente:</label>
            <select id="cliente" name="cliente">
                <option value="">Todos</option>
                <c:forEach items="${clientes}" var="cliente">
                    <option value="${cliente.CODIGO}" ${param.cliente == cliente.CODIGO ? 'selected' : ''}>
                        ${cliente.NOME}
                    </option>
                </c:forEach>
            </select>
        </div>
        
        <div class="filter-buttons">
            <button class="btn btn-primary" onclick="aplicarFiltros()">Filtrar</button>
            <button class="btn btn-secondary" onclick="limparFiltros()">Limpar</button>
        </div>
    </div>
</div>

<script>
function aplicarFiltros() {
    const dataInicio = document.getElementById('dataInicio').value;
    const dataFim = document.getElementById('dataFim').value;
    const cliente = document.getElementById('cliente').value;
    
    const params = new URLSearchParams();
    if (dataInicio) params.append('dataInicio', dataInicio);
    if (dataFim) params.append('dataFim', dataFim);
    if (cliente) params.append('cliente', cliente);
    
    window.location.href = '?' + params.toString();
}

function limparFiltros() {
    document.getElementById('dataInicio').value = '';
    document.getElementById('dataFim').value = '';
    document.getElementById('cliente').value = '';
    window.location.href = window.location.pathname;
}
</script>
```

### **2. Cards de KPI**
```jsp
<style>
.kpi-container {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin: 20px 0;
}

.kpi-card {
    background: white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    border-left: 4px solid #007bff;
    transition: transform 0.2s ease;
}

.kpi-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.kpi-card.success {
    border-left-color: #28a745;
}

.kpi-card.warning {
    border-left-color: #ffc107;
}

.kpi-card.danger {
    border-left-color: #dc3545;
}

.kpi-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.kpi-title {
    font-size: 14px;
    color: #6c757d;
    font-weight: 500;
    margin: 0;
}

.kpi-icon {
    width: 24px;
    height: 24px;
    opacity: 0.7;
}

.kpi-value {
    font-size: 32px;
    font-weight: bold;
    color: #212529;
    margin: 0 0 8px 0;
}

.kpi-change {
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 4px;
}

.kpi-change.positive {
    color: #28a745;
}

.kpi-change.negative {
    color: #dc3545;
}

.kpi-change.neutral {
    color: #6c757d;
}
</style>

<div class="kpi-container">
    <c:forEach items="${kpis}" var="kpi">
        <div class="kpi-card ${kpi.TIPO}">
            <div class="kpi-header">
                <h3 class="kpi-title">${kpi.TITULO}</h3>
                <svg class="kpi-icon" viewBox="0 0 24 24">
                    <path d="${kpi.ICONE}" fill="currentColor"/>
                </svg>
            </div>
            <p class="kpi-value">${kpi.VALOR}</p>
            <div class="kpi-change ${kpi.MUDANCA_TIPO}">
                <c:if test="${kpi.MUDANCA_TIPO == 'positive'}">
                    <svg width="12" height="12" viewBox="0 0 24 24">
                        <path d="M7 14l5-5 5 5z" fill="currentColor"/>
                    </svg>
                </c:if>
                <c:if test="${kpi.MUDANCA_TIPO == 'negative'}">
                    <svg width="12" height="12" viewBox="0 0 24 24">
                        <path d="M7 10l5 5 5-5z" fill="currentColor"/>
                    </svg>
                </c:if>
                ${kpi.MUDANCA}% vs período anterior
            </div>
        </div>
    </c:forEach>
</div>
```

## 🔧 Funcionalidades JavaScript

### **1. snk:query para Consultas**
```jsp
<snk:query var="dados" sql="
    SELECT 
        PAR.NOMEPARC AS CLIENTE,
        COUNT(CAB.NUNOTA) AS QTD_PEDIDOS,
        SUM(ITE.VLRTOT) AS VALOR_TOTAL
    FROM TGFCAB CAB
    INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
    INNER JOIN TGFPAR PAR ON CAB.CODPARC = PAR.CODPARC
    WHERE CAB.DTNEG >= SYSDATE - 30
    GROUP BY PAR.NOMEPARC
    ORDER BY VALOR_TOTAL DESC
    FETCH FIRST 10 ROWS ONLY
"/>

<c:forEach items="${dados.rows}" var="row">
    <div class="data-item">
        <span class="cliente">${row.CLIENTE}</span>
        <span class="quantidade">${row.QTD_PEDIDOS}</span>
        <span class="valor">R$ <fmt:formatNumber value="${row.VALOR_TOTAL}" pattern="#,##0.00"/></span>
    </div>
</c:forEach>
```

### **2. Navegação com openLevel**
```javascript
function abrirDetalhes(codigo) {
    openLevel('TGFPAR', 'VIEW', {
        CODPARC: codigo
    });
}

function abrirPedidos(cliente) {
    openLevel('TGFCAB', 'LIST', {
        CODPARC: cliente,
        TIPMOV: 'V'
    });
}

function abrirRelatorio(tipo) {
    openLevel('RELATORIO', 'REPORT', {
        TIPO: tipo,
        DATA_INICIO: document.getElementById('dataInicio').value,
        DATA_FIM: document.getElementById('dataFim').value
    });
}
```

### **3. Atualização de Componentes**
```javascript
function atualizarDashboard() {
    refreshDetails();
    showMessage('Dashboard atualizado com sucesso!');
}

function exportarDados(formato) {
    const params = new URLSearchParams();
    params.append('formato', formato);
    params.append('dados', JSON.stringify(obterDadosFiltrados()));
    
    window.open('exportar?' + params.toString(), '_blank');
}

function obterDadosFiltrados() {
    return {
        dataInicio: document.getElementById('dataInicio').value,
        dataFim: document.getElementById('dataFim').value,
        cliente: document.getElementById('cliente').value,
        status: document.getElementById('status').value
    };
}
```

## 🎨 Estilos e CSS

### **1. Tema Padrão**
```css
:root {
    --primary-color: #007bff;
    --secondary-color: #6c757d;
    --success-color: #28a745;
    --warning-color: #ffc107;
    --danger-color: #dc3545;
    --light-color: #f8f9fa;
    --dark-color: #343a40;
    --border-radius: 8px;
    --box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

body {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    background-color: #f5f5f5;
    margin: 0;
    padding: 20px;
    color: #333;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    background: white;
    border-radius: var(--border-radius);
    box-shadow: var(--box-shadow);
    padding: 20px;
}
```

### **2. Componentes Responsivos**
```css
@media (max-width: 768px) {
    .kpi-container {
        grid-template-columns: 1fr;
    }
    
    .filter-row {
        flex-direction: column;
        align-items: stretch;
    }
    
    .filter-group {
        min-width: auto;
    }
    
    .data-table {
        font-size: 12px;
    }
    
    .data-table th,
    .data-table td {
        padding: 8px 4px;
    }
}

@media (max-width: 480px) {
    body {
        padding: 10px;
    }
    
    .container {
        padding: 15px;
    }
    
    .kpi-value {
        font-size: 24px;
    }
}
```

## 🛠️ Boas Práticas

### **1. Performance**
- **Minificação**: Minificar CSS e JavaScript
- **Cache**: Usar cache para dados estáticos
- **Lazy Loading**: Carregar dados sob demanda
- **Otimização**: Otimizar consultas SQL
- **Compressão**: Usar compressão de dados

### **2. Acessibilidade**
- **Semântica**: Usar elementos HTML semânticos
- **Contraste**: Manter contraste adequado
- **Navegação**: Suporte para navegação por teclado
- **Screen Readers**: Compatibilidade com leitores de tela
- **Responsividade**: Design responsivo

### **3. Manutenibilidade**
- **Modularização**: Separar CSS e JavaScript
- **Documentação**: Documentar código complexo
- **Padrões**: Seguir padrões de nomenclatura
- **Versionamento**: Controle de versão
- **Testes**: Testes automatizados

### **4. Segurança**
- **Validação**: Validar dados de entrada
- **Sanitização**: Sanitizar dados antes do uso
- **HTTPS**: Usar HTTPS para comunicação
- **Headers**: Configurar headers de segurança
- **Auditoria**: Registrar operações sensíveis

## 🔍 Troubleshooting

### **Problemas Comuns**
- **Layout quebrado**: Verificar CSS e estrutura HTML
- **JavaScript não funciona**: Verificar console de erros
- **Dados não carregam**: Verificar consultas SQL
- **Performance lenta**: Otimizar consultas e CSS
- **Responsividade**: Testar em diferentes dispositivos

### **Soluções**
- **Debug**: Usar ferramentas de debug do navegador
- **Logs**: Verificar logs do servidor
- **Testes**: Testar em ambiente isolado
- **Documentação**: Consultar documentação oficial
- **Suporte**: Contatar suporte técnico

## 🚀 Evolução e Tendências

### **Melhorias Contínuas**
- **Novos Componentes**: Novos componentes HTML5
- **Melhor Performance**: Otimizações de performance
- **Responsividade**: Melhor suporte mobile
- **Acessibilidade**: Melhorias de acessibilidade
- **Interatividade**: Mais recursos interativos

### **Tendências Futuras**
- **Web Components**: Componentes web nativos
- **PWA**: Progressive Web Apps
- **WebAssembly**: Performance nativa
- **AI/ML**: Integração com IA
- **AR/VR**: Realidade aumentada/virtual

---

*Este documento foi criado com base na documentação oficial do Sankhya Developer sobre Componentes HTML5 e melhores práticas de desenvolvimento.*
