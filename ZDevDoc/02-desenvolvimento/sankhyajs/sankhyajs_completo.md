# 🎨 SankhyaJS (HTML5) - Framework Front-end Completo

## 🎯 Visão Geral

O **SankhyaJS** é o framework front-end oficial da Sankhya, baseado em AngularJS e padrões web modernos. Ele facilita o desenvolvimento de telas e recursos visuais de maneira padronizada e ágil, simplificando a criação, alteração e customização de interfaces.

## 🏗️ Arquitetura SankhyaJS

### **Camadas da Plataforma Sankhya**
```
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA FRONT-END                        │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   SankhyaJS     │ │   AngularJS     │ │   Componentes   │ │
│  │   (HTML5)       │ │   Framework     │ │   Customizados  │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA DE APLICAÇÃO                     │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Controllers   │ │    Services     │ │   Directives    │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA DE DADOS                         │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Jape          │ │   Oracle DB     │ │   APIs          │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🛠️ Ferramentas de Desenvolvimento

### **1. snkCode (Visual Studio Code)**
- **Extensão**: Pesquisar por "snkcode" nas extensões do VS Code
- **Funcionalidades**:
  - Autocomplete inteligente
  - Sugestão de snippets
  - Validação de sintaxe
  - Formatação automática
  - Navegação rápida

### **2. Generator Sankhya**
- **Propósito**: Gerador de código para componentes dynaform
- **Recursos**:
  - Padronização de CRUD
  - Interceptors automáticos
  - Recursos visuais
  - Templates personalizados
  - Integração com dynaform

### **3. Showcase de Componentes**
- **URL**: `https://local:8080/mge/ShowcaseHTML5.xhtml5`
- **Funcionalidades**:
  - Visualização interativa
  - Exemplos de uso
  - Documentação viva
  - Testes de componentes
  - Referência rápida

## 📚 Recursos de Aprendizado

### **Cursos Recomendados**
| Curso | Plataforma | Carga Horária |
|-------|------------|---------------|
| [W3Schools AngularJS](https://www.w3schools.com/angular/angular_ref_directives.asp) | W3Schools | N/A |
| [AngularJS: crie webapps poderosas](https://cursos.alura.com.br/course/angularjs-mvc) | Alura | 16 Horas |
| [AngularJS por Rodrigo Branas](https://www.youtube.com/playlist?list=PLQCmSnNFVYnTD5p2fR4EXmtlR6jQJMbPb) | YouTube | 10 Horas |
| [AngularJS Docs](https://code.angularjs.org/1.5.8/docs/api) | code.angularjs.org | N/A |

### **Certificação Sankhya**
- **Associate Front-End**: Conhecimentos específicos do framework Sankhya
- **Specialist Dashboards**: Especialização em dashboards
- **Specialist Add-ons**: Desenvolvimento de add-ons

## 🏗️ Estrutura de Componentes

### **Template Base JSP**
```jsp
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="snk" uri="/WEB-INF/tld/sankhyaUtil.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
    <title>Componente SankhyaJS</title>
    <link rel="stylesheet" type="text/css" href="${BASE_FOLDER}css/mainCSS.css">
    <snk:load />
</head>
<body>
    <!-- Conteúdo do componente -->
</body>
</html>
```

### **Template com CSS Customizado**
```jsp
<head>
    <title>Dashboard SankhyaJS</title>
    <link rel="stylesheet" type="text/css" href="${BASE_FOLDER}css/mainCSS.css">
    <style>
        /* Estilos customizados */
        .sankhya-component {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.2);
        }
        
        .sankhya-card {
            background: white;
            border-radius: 8px;
            padding: 15px;
            margin: 10px 0;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
    </style>
    <snk:load />
</head>
```

## 🔧 Taglibs Sankhya

### **snk:load**
- **Propósito**: Carrega recursos necessários do SankhyaJS
- **Uso**: Sempre incluir no `<head>` do componente
- **Recursos carregados**:
  - AngularJS framework
  - SankhyaJS core
  - CSS padrão
  - JavaScript utilities

### **snk:query**
- **Propósito**: Executa consultas SQL e disponibiliza resultados
- **Sintaxe**:
```jsp
<snk:query var="nomeVariavel">
    SELECT campo1, campo2, campo3
    FROM tabela
    WHERE condicao = :PARAMETRO
    ORDER BY campo1
</snk:query>
```

### **snk:param**
- **Propósito**: Define parâmetros para consultas
- **Sintaxe**:
```jsp
<snk:param name="PARAMETRO" value="${valor}" />
```

### **snk:format**
- **Propósito**: Formatação de dados
- **Sintaxe**:
```jsp
<snk:format value="${valor}" type="currency" />
<snk:format value="${data}" type="date" pattern="dd/MM/yyyy" />
<snk:format value="${numero}" type="number" pattern="#,##0.00" />
```

## 📊 Componentes Visuais

### **1. Cards Dashboard**
```jsp
<style>
.sankhya-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 30px;
    border-radius: 15px;
    text-align: center;
    box-shadow: 0 4px 15px rgba(0,0,0,0.2);
    transition: transform 0.3s ease;
    cursor: pointer;
}

.sankhya-card:hover {
    transform: translateY(-5px);
}

.sankhya-card .value {
    font-size: 3em;
    font-weight: bold;
    margin: 10px 0;
}

.sankhya-card .label {
    font-size: 1.2em;
    opacity: 0.9;
}
</style>

<div class="sankhya-card" onclick="abrirDetalhes()">
    <div class="label">Total de Vendas</div>
    <div class="value">${totalVendas}</div>
    <div class="change positive">↗ +12.5% vs mês anterior</div>
</div>
```

### **2. Grid de Cards Responsivo**
```jsp
<style>
.sankhya-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    padding: 20px;
}

.sankhya-grid-item {
    background: white;
    border-radius: 10px;
    padding: 25px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    border-left: 4px solid #007bff;
    transition: all 0.3s ease;
}

.sankhya-grid-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}
</style>

<div class="sankhya-grid">
    <div class="sankhya-grid-item">
        <h3>Receita Total</h3>
        <div class="value">R$ ${receitaTotal}</div>
        <div class="subtitle">Este mês</div>
    </div>
    
    <div class="sankhya-grid-item">
        <h3>Pedidos</h3>
        <div class="value">${totalPedidos}</div>
        <div class="subtitle">Processados</div>
    </div>
</div>
```

### **3. Tabelas Interativas**
```jsp
<style>
.sankhya-table {
    width: 100%;
    border-collapse: collapse;
    background: white;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.sankhya-table th {
    background: #f8f9fa;
    padding: 15px;
    text-align: left;
    font-weight: 600;
    color: #495057;
    border-bottom: 2px solid #dee2e6;
}

.sankhya-table td {
    padding: 12px 15px;
    border-bottom: 1px solid #dee2e6;
}

.sankhya-table tr:hover {
    background: #f8f9fa;
}

.sankhya-table .clickable {
    cursor: pointer;
    color: #007bff;
}

.sankhya-table .clickable:hover {
    text-decoration: underline;
}
</style>

<snk:query var="dados">
    SELECT codigo, descricao, valor, status
    FROM tabela
    WHERE condicao = :PARAMETRO
</snk:query>

<table class="sankhya-table">
    <thead>
        <tr>
            <th>Código</th>
            <th>Descrição</th>
            <th>Valor</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${dados.rows}" var="row">
            <tr>
                <td class="clickable" onclick="abrirDetalhes('${row.codigo}')">
                    ${row.codigo}
                </td>
                <td>${row.descricao}</td>
                <td>
                    <snk:format value="${row.valor}" type="currency" />
                </td>
                <td>
                    <span class="status-${row.status}">${row.status}</span>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
```

## 📈 Gráficos e Visualizações

### **1. Gráfico de Pizza com Chart.js**
```jsp
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<style>
.sankhya-chart-container {
    width: 100%;
    height: 400px;
    margin: 20px 0;
    background: white;
    border-radius: 10px;
    padding: 20px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}
</style>

<snk:query var="pizza">
    SELECT categoria, COUNT(*) as quantidade, SUM(valor) as total
    FROM vendas
    WHERE dtvenda BETWEEN :P_PERIODO.INI AND :P_PERIODO.FIN
    GROUP BY categoria
    ORDER BY quantidade DESC
</snk:query>

<div class="sankhya-chart-container">
    <canvas id="sankhyaPieChart"></canvas>
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
    var ctx = document.getElementById('sankhyaPieChart').getContext('2d');
    var labels = [];
    var data = [];
    var colors = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'];

    <c:forEach items="${pizza.rows}" var="row" varStatus="status">
        labels.push("${row.categoria}");
        data.push(${row.quantidade});
    </c:forEach>

    var chartData = {
        labels: labels,
        datasets: [{
            data: data,
            backgroundColor: colors.slice(0, labels.length),
            borderWidth: 2,
            borderColor: '#fff'
        }]
    };

    var myPieChart = new Chart(ctx, {
        type: 'pie',
        data: chartData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            title: {
                display: true,
                text: 'Distribuição por Categoria',
                fontSize: 16,
                fontColor: '#333'
            },
            legend: {
                position: 'bottom',
                labels: {
                    padding: 20,
                    usePointStyle: true
                }
            },
            onClick: function(event, elements) {
                if (elements.length > 0) {
                    var clickedIndex = elements[0].index;
                    var categoria = chartData.labels[clickedIndex];
                    abrirDetalhesCategoria(categoria);
                }
            }
        }
    });

    function abrirDetalhesCategoria(categoria) {
        const params = {'A_CATEGORIA': categoria};
        refreshDetails('lvl_detalhes_categoria', params);
    }
});
</script>
```

### **2. Gráfico de Barras**
```jsp
<div class="sankhya-chart-container">
    <canvas id="sankhyaBarChart"></canvas>
</div>

<script>
var ctx = document.getElementById('sankhyaBarChart').getContext('2d');
var myBarChart = new Chart(ctx, {
    type: 'bar',
    data: {
        labels: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun'],
        datasets: [{
            label: 'Vendas',
            data: [12, 19, 3, 5, 2, 3],
            backgroundColor: 'rgba(54, 162, 235, 0.8)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 2,
            borderRadius: 5
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
            y: {
                beginAtZero: true,
                grid: {
                    color: 'rgba(0,0,0,0.1)'
                }
            },
            x: {
                grid: {
                    display: false
                }
            }
        },
        plugins: {
            legend: {
                display: false
            }
        }
    }
});
</script>
```

### **3. Gráfico de Linha**
```jsp
<div class="sankhya-chart-container">
    <canvas id="sankhyaLineChart"></canvas>
</div>

<script>
var ctx = document.getElementById('sankhyaLineChart').getContext('2d');
var myLineChart = new Chart(ctx, {
    type: 'line',
    data: {
        labels: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun'],
        datasets: [{
            label: 'Receita',
            data: [12, 19, 3, 5, 2, 3],
            borderColor: 'rgb(75, 192, 192)',
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            tension: 0.4,
            fill: true
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
            y: {
                beginAtZero: true
            }
        }
    }
});
</script>
```

## 🔗 Navegação e Interação

### **1. Funções de Navegação Padrão**
```javascript
// Abrir novo nível
function abrirNivel(parametro) {
    var params = { 'A_PARAMETRO': parametro };
    var level = 'lvl_nivel_destino';
    openLevel(level, params);
}

// Atualizar detalhes
function atualizarDetalhes(filtro) {
    const params = { 'A_FILTRO': filtro };
    refreshDetails('svl_detalhes', params);
}

// Navegação com múltiplos parâmetros
function navegarCompleto(codigo, tipo, status) {
    var params = { 
        'A_CODIGO': parseInt(codigo),
        'A_TIPO': tipo,
        'A_STATUS': status
    };
    openLevel('lvl_destino', params);
}

// Navegação com validação
function navegarComValidacao(parametros) {
    try {
        if (!validarParametros(parametros)) {
            mostrarMensagem('Parâmetros inválidos!', 'error');
            return;
        }
        
        var params = {
            'A_CODIGO': parametros.codigo,
            'A_TIPO': parametros.tipo
        };
        
        openLevel('lvl_destino', params);
        
    } catch (error) {
        console.error('Erro na navegação:', error);
        mostrarMensagem('Erro ao navegar: ' + error.message, 'error');
    }
}
```

### **2. Interações com Gráficos**
```javascript
// Click em gráfico de pizza
function onPieChartClick(event, elements) {
    if (elements.length > 0) {
        var clickedIndex = elements[0].index;
        var label = chartData.labels[clickedIndex];
        var value = chartData.datasets[0].data[clickedIndex];
        
        // Navegar para detalhes
        abrirDetalhes(label, value);
    }
}

// Hover em gráfico de barras
function onBarChartHover(event, elements) {
    if (elements.length > 0) {
        var index = elements[0].index;
        var label = chartData.labels[index];
        var value = chartData.datasets[0].data[index];
        
        // Mostrar tooltip customizado
        mostrarTooltip(label, value);
    }
}

// Seleção múltipla em gráfico
function onChartSelection(selectedElements) {
    var selectedData = [];
    
    selectedElements.forEach(function(element) {
        var index = element.index;
        selectedData.push({
            label: chartData.labels[index],
            value: chartData.datasets[0].data[index]
        });
    });
    
    processarSelecao(selectedData);
}
```

### **3. Validação e Tratamento de Erros**
```javascript
function executarAcaoComValidacao(parametros) {
    try {
        // Validar parâmetros
        if (!validarParametros(parametros)) {
            throw new Error('Parâmetros inválidos');
        }
        
        // Executar ação
        var resultado = processarAcao(parametros);
        
        // Sucesso
        mostrarMensagem('Ação executada com sucesso!', 'success');
        return resultado;
        
    } catch (error) {
        console.error('Erro na execução:', error);
        mostrarMensagem('Erro ao executar ação: ' + error.message, 'error');
    }
}

function validarParametros(params) {
    if (!params || !params.A_CODIGO) {
        console.error('Parâmetro obrigatório não informado');
        return false;
    }
    
    if (params.A_CODIGO < 0) {
        console.error('Código deve ser positivo');
        return false;
    }
    
    return true;
}

function mostrarMensagem(texto, tipo) {
    var classe = tipo === 'success' ? 'alert-success' : 'alert-error';
    var icone = tipo === 'success' ? '✓' : '✗';
    
    var mensagem = '<div class="alert ' + classe + '">' +
                   '<span class="alert-icon">' + icone + '</span>' +
                   '<span class="alert-text">' + texto + '</span>' +
                   '</div>';
    
    // Inserir mensagem no DOM
    document.getElementById('mensagens').innerHTML = mensagem;
    
    // Remover após 5 segundos
    setTimeout(function() {
        document.getElementById('mensagens').innerHTML = '';
    }, 5000);
}
```

## 🎨 Estilos e Temas

### **1. Tema Padrão Sankhya**
```css
:root {
    --sankhya-primary: #007bff;
    --sankhya-secondary: #6c757d;
    --sankhya-success: #28a745;
    --sankhya-danger: #dc3545;
    --sankhya-warning: #ffc107;
    --sankhya-info: #17a2b8;
    --sankhya-light: #f8f9fa;
    --sankhya-dark: #343a40;
    
    --sankhya-font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    --sankhya-border-radius: 8px;
    --sankhya-box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.sankhya-component {
    font-family: var(--sankhya-font-family);
    background-color: #f5f5f5;
    margin: 0;
    padding: 0;
}

.sankhya-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}
```

### **2. Componentes Responsivos**
```css
/* Mobile First */
.sankhya-component {
    width: 100%;
    padding: 10px;
}

/* Tablet */
@media (min-width: 768px) {
    .sankhya-component {
        width: 50%;
        padding: 20px;
    }
}

/* Desktop */
@media (min-width: 1024px) {
    .sankhya-component {
        width: 33.333%;
        padding: 30px;
    }
}

/* Large Desktop */
@media (min-width: 1200px) {
    .sankhya-component {
        width: 25%;
        padding: 40px;
    }
}
```

### **3. Animações e Transições**
```css
.sankhya-fade-in {
    animation: sankhyaFadeIn 0.5s ease-in;
}

@keyframes sankhyaFadeIn {
    from { 
        opacity: 0; 
        transform: translateY(20px); 
    }
    to { 
        opacity: 1; 
        transform: translateY(0); 
    }
}

.sankhya-slide-in {
    animation: sankhyaSlideIn 0.3s ease-out;
}

@keyframes sankhyaSlideIn {
    from { 
        transform: translateX(-100%); 
    }
    to { 
        transform: translateX(0); 
    }
}

.sankhya-hover-effect {
    transition: all 0.3s ease;
}

.sankhya-hover-effect:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 15px rgba(0,0,0,0.2);
}
```

## 📱 Responsividade Avançada

### **1. Grid Responsivo Sankhya**
```css
.sankhya-responsive-grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 20px;
}

@media (min-width: 768px) {
    .sankhya-responsive-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (min-width: 1024px) {
    .sankhya-responsive-grid {
        grid-template-columns: repeat(3, 1fr);
    }
}

@media (min-width: 1200px) {
    .sankhya-responsive-grid {
        grid-template-columns: repeat(4, 1fr);
    }
}
```

### **2. Tabela Responsiva Sankhya**
```css
.sankhya-table-responsive {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

@media (max-width: 768px) {
    .sankhya-table-responsive table {
        font-size: 12px;
    }
    
    .sankhya-table-responsive th,
    .sankhya-table-responsive td {
        padding: 8px 5px;
    }
    
    .sankhya-table-responsive .mobile-hide {
        display: none;
    }
}
```

## 🔍 Debugging e Desenvolvimento

### **1. Console do Navegador**
```javascript
// Debug básico
console.log('Debug SankhyaJS:', variavel);
console.table(dados);
console.group('Grupo de Debug');
console.log('Item 1:', item1);
console.log('Item 2:', item2);
console.groupEnd();

// Debug de performance
console.time('Operação Sankhya');
// ... código da operação
console.timeEnd('Operação Sankhya');
```

### **2. Validação de Componentes**
```javascript
function validarComponenteSankhya() {
    var erros = [];
    
    // Verificar se SankhyaJS está carregado
    if (typeof openLevel === 'undefined') {
        erros.push('SankhyaJS não está carregado');
    }
    
    // Verificar se Chart.js está disponível
    if (typeof Chart === 'undefined') {
        erros.push('Chart.js não está carregado');
    }
    
    // Verificar elementos DOM
    var elementos = document.querySelectorAll('.sankhya-component');
    if (elementos.length === 0) {
        erros.push('Nenhum componente Sankhya encontrado');
    }
    
    if (erros.length > 0) {
        console.error('Erros de validação:', erros);
        return false;
    }
    
    console.log('Componente Sankhya validado com sucesso');
    return true;
}
```

### **3. Monitoramento de Performance**
```javascript
function monitorarPerformanceSankhya() {
    // Monitorar tempo de carregamento
    window.addEventListener('load', function() {
        var loadTime = performance.timing.loadEventEnd - performance.timing.navigationStart;
        console.log('Tempo de carregamento SankhyaJS:', loadTime + 'ms');
    });
    
    // Monitorar operações de navegação
    var originalOpenLevel = window.openLevel;
    window.openLevel = function(level, params) {
        console.time('Navegação para ' + level);
        var resultado = originalOpenLevel.call(this, level, params);
        console.timeEnd('Navegação para ' + level);
        return resultado;
    };
}
```

## 🚀 Boas Práticas SankhyaJS

### **1. Estrutura de Arquivos**
- **JSP**: Componentes visuais e lógica de apresentação
- **CSS**: Estilos específicos do componente
- **JavaScript**: Interações e navegação
- **SQL**: Consultas otimizadas e parametrizadas

### **2. Performance**
- Use consultas SQL otimizadas
- Implemente paginação para grandes volumes
- Cache resultados quando possível
- Minimize chamadas desnecessárias
- Use lazy loading para componentes pesados

### **3. Responsividade**
- Use CSS Grid e Flexbox
- Implemente breakpoints para diferentes telas
- Teste em diferentes resoluções
- Considere dispositivos móveis

### **4. Acessibilidade**
- Use semântica HTML adequada
- Implemente navegação por teclado
- Adicione alt text em imagens
- Mantenha contraste adequado
- Use ARIA labels quando necessário

### **5. Manutenibilidade**
- Documente código complexo
- Use nomenclatura consistente
- Modularize componentes
- Teste regularmente
- Mantenha versões atualizadas

## 🆘 Suporte e Comunidade

### **Recursos de Ajuda**
- **Comunidade Sankhya Developer**: Sala HTML5
- **Documentação Técnica**: Recursos de API, conceitos e componentes
- **Showcase**: Visualização interativa dos componentes
- **Portal Sankhya Developer**: Documentação oficial

### **Contatos**
- Portal do Desenvolvedor Sankhya
- Comunidade online
- Suporte técnico oficial
- Universidade Sankhya

---

*Este documento foi criado com base na documentação oficial do SankhyaJS e exemplos práticos encontrados no repositório de conhecimento.*
