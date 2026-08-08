# 📚 Guia de Referência SankhyaJS (HTML5)

## 🎯 Visão Geral

O **SankhyaJS** é um framework front-end baseado em AngularJS e padrões web, desenvolvido pela Sankhya para facilitar a criação de telas e recursos visuais de forma padronizada e ágil.

### 📖 Documentação Oficial
- **Portal Sankhya Developer**: https://developer.sankhya.com.br/docs/sankhya-js
- **Showcase de Componentes**: `https://local:8080/mge/ShowcaseHTML5.xhtml5`

## 🛠️ Ferramentas de Desenvolvimento

### 1. snkCode (Visual Studio Code)
- **Extensão**: Pesquisar por "snkcode" nas extensões do VS Code
- **Funcionalidades**: Autocomplete e sugestão de snippets
- **Benefícios**: Facilita criação de interfaces com o framework

### 2. Generator Sankhya
- **Propósito**: Gerador de código para componentes dynaform
- **Recursos**: Padronização de CRUD, interceptors e recursos visuais
- **Integração**: Trabalha em conjunto com componentes dynaform

## 🏗️ Estrutura de Componentes HTML5

### Template Base JSP
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

## 🔧 Taglibs Sankhya

### snk:load
- **Propósito**: Carrega recursos necessários do SankhyaJS
- **Uso**: Sempre incluir no `<head>` do componente

### snk:query
- **Propósito**: Executa consultas SQL e disponibiliza resultados
- **Sintaxe**:
```jsp
<snk:query var="nomeVariavel">
    SELECT campo1, campo2 FROM tabela WHERE condicao = :PARAMETRO
</snk:query>
```

### Exemplo de Uso:
```jsp
<snk:query var="dados">
    SELECT COUNT(*) as total, SUM(valor) as soma 
    FROM TGFCAB 
    WHERE DTMOV BETWEEN :P_PERIODO.INI AND :P_PERIODO.FIN
</snk:query>

<c:forEach items="${dados.rows}" var="row">
    <p>Total: ${row.total} | Soma: ${row.soma}</p>
</c:forEach>
```

## 🎨 Padrões de Interface

### 1. Cards Dashboard
```jsp
<style>
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
</style>

<div class="card" onclick="abrirNivel()">
    <h3>Título do Card</h3>
    <p>${valor}</p>
</div>
```

### 2. Tabelas com Formatação
```jsp
<table border="0.6">
    <thead>
        <tr>
            <th>Código</th>
            <th>Descrição</th>
            <th>Valor</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${dados.rows}" var="row">
            <tr>
                <td onclick="abrir('${row.codigo}')">${row.codigo}</td>
                <td>${row.descricao}</td>
                <td>
                    <fmt:formatNumber value="${row.valor}" type="number" 
                        maxFractionDigits="2" minFractionDigits="2" />
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
```

## 📊 Integração com Chart.js

### Gráfico de Pizza
```jsp
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<snk:query var="pizza">
    SELECT categoria, COUNT(*) as quantidade 
    FROM tabela 
    GROUP BY categoria
</snk:query>

<canvas id="myPieChart"></canvas>

<script>
document.addEventListener('DOMContentLoaded', function () {
    var ctx = document.getElementById('myPieChart').getContext('2d');
    var labels = [];
    var data = [];

    <c:forEach items="${pizza.rows}" var="row">
        labels.push("${row.categoria}");
        data.push(${row.quantidade});
    </c:forEach>

    var myPieChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56']
            }]
        },
        options: {
            onClick: function(event, elements) {
                if (elements.length > 0) {
                    var clickedIndex = elements[0].index;
                    var categoria = labels[clickedIndex];
                    abrirDetalhes(categoria);
                }
            }
        }
    });
});
</script>
```

## 🔗 Navegação e Interação

### Funções JavaScript Padrão

#### 1. Abrir Novo Nível
```javascript
function abrirNivel() {
    var params = { 'A_PARAMETRO': valor };
    var level = 'lvl_nivel_destino';
    openLevel(level, params);
}
```

#### 2. Atualizar Detalhes
```javascript
function atualizarDetalhes() {
    const params = { 'A_FILTRO': valor };
    refreshDetails('svl_detalhes', params);
}
```

#### 3. Navegação com Parâmetros
```javascript
function navegarComParametros(codigo, tipo) {
    var params = { 
        'A_CODIGO': parseInt(codigo),
        'A_TIPO': tipo
    };
    openLevel('lvl_destino', params);
}
```

## 🎯 Boas Práticas

### 1. Estrutura de Arquivos
- **JSP**: Componentes visuais e lógica de apresentação
- **CSS**: Estilos específicos do componente
- **JavaScript**: Interações e navegação
- **SQL**: Consultas otimizadas e parametrizadas

### 2. Performance
- Use consultas SQL otimizadas
- Implemente paginação para grandes volumes
- Cache resultados quando possível
- Minimize chamadas desnecessárias

### 3. Responsividade
- Use CSS Grid e Flexbox
- Implemente breakpoints para diferentes telas
- Teste em diferentes resoluções

### 4. Acessibilidade
- Use semântica HTML adequada
- Implemente navegação por teclado
- Adicione alt text em imagens
- Mantenha contraste adequado

## 🔍 Debugging e Desenvolvimento

### 1. Console do Navegador
```javascript
console.log('Debug:', variavel);
console.table(dados);
```

### 2. Validação de Parâmetros
```javascript
function validarParametros(params) {
    if (!params || !params.A_CODIGO) {
        console.error('Parâmetro obrigatório não informado');
        return false;
    }
    return true;
}
```

### 3. Tratamento de Erros
```javascript
function executarAcao() {
    try {
        // Lógica da ação
        var resultado = processarDados();
        return resultado;
    } catch (error) {
        console.error('Erro na execução:', error);
        alert('Erro ao processar dados');
    }
}
```

## 📚 Recursos de Aprendizado

### Cursos Recomendados
1. **W3Schools AngularJS**: https://www.w3schools.com/angular/angular_ref_directives.asp
2. **Alura AngularJS**: https://cursos.alura.com.br/course/angularjs-mvc (16 horas)
3. **YouTube - Rodrigo Branas**: https://www.youtube.com/playlist?list=PLQCmSnNFVYnTD5p2fR4EXmtlR6jQJMbPb (10 horas)
4. **AngularJS Docs**: https://code.angularjs.org/1.5.8/docs/api

### Certificação Sankhya
- **Associate Front-End**: Conhecimentos específicos do framework Sankhya
- **Specialist Dashboards**: Especialização em dashboards

## 🆘 Suporte e Comunidade

### Recursos de Ajuda
- **Comunidade Sankhya Developer**: Sala HTML5
- **Documentação Técnica**: Recursos de API, conceitos e componentes
- **Showcase**: Visualização interativa dos componentes

### Contatos
- Portal do Desenvolvedor Sankhya
- Comunidade online
- Suporte técnico oficial

---

*Este guia foi criado com base na documentação oficial do SankhyaJS e exemplos práticos encontrados no repositório de conhecimento.*
