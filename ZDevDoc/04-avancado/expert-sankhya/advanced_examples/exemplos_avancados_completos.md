# 🚀 Exemplos Avançados Sankhya - Casos de Uso Reais

## 🎯 **Exemplos Práticos de Personalizações Avançadas**

Este documento apresenta exemplos práticos e casos de uso reais de personalizações Sankhya, baseados em cenários empresariais complexos e soluções inovadoras.

## 🏗️ **Caso de Uso 1: Sistema de Gestão de Vendas Inteligente**

### **Contexto do Negócio**
Empresa de varejo com múltiplas lojas, necessitando de um sistema inteligente para gestão de vendas, estoque e relacionamento com clientes.

### **Solução Implementada**
```java
// Sistema de Gestão de Vendas Inteligente
package br.com.sankhya.personalizacao.vendas;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

/**
 * Sistema inteligente de gestão de vendas
 * Implementa regras de negócio complexas e automações
 */
public class SistemaGestaoVendasInteligente {
    
    private SistemaRecomendacaoProdutos sistemaRecomendacao;
    private SistemaAnaliseComportamento sistemaAnalise;
    private SistemaNotificacaoInteligente sistemaNotificacao;
    
    public SistemaGestaoVendasInteligente() {
        this.sistemaRecomendacao = new SistemaRecomendacaoProdutos();
        this.sistemaAnalise = new SistemaAnaliseComportamento();
        this.sistemaNotificacao = new SistemaNotificacaoInteligente();
    }
    
    /**
     * Evento: Antes de inserir pedido
     * Aplica validações inteligentes e sugestões
     */
    public void beforeInsertPedido(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // 1. Análise de comportamento do cliente
        BigDecimal clienteId = vo.getProperty("CODCLI");
        AnaliseComportamento analise = sistemaAnalise.analisarCliente(clienteId);
        
        // 2. Aplicar regras baseadas no comportamento
        aplicarRegrasComportamento(vo, analise);
        
        // 3. Sugerir produtos complementares
        List<BigDecimal> produtosSugeridos = sistemaRecomendacao.gerarRecomendacoes(clienteId, 5);
        vo.setProperty("AD_PRODUTOS_SUGERIDOS", produtosSugeridos.toString());
        
        // 4. Calcular desconto inteligente
        BigDecimal desconto = calcularDescontoInteligente(vo, analise);
        vo.setProperty("VLRDESC", desconto);
        
        // 5. Definir prioridade de entrega
        String prioridade = definirPrioridadeEntrega(analise);
        vo.setProperty("AD_PRIORIDADE_ENTREGA", prioridade);
    }
    
    /**
     * Evento: Após inserir pedido
     * Executa ações pós-venda inteligentes
     */
    public void afterInsertPedido(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // 1. Atualizar perfil do cliente
        BigDecimal clienteId = vo.getProperty("CODCLI");
        sistemaAnalise.atualizarPerfilCliente(clienteId, vo);
        
        // 2. Enviar notificações personalizadas
        sistemaNotificacao.enviarConfirmacaoPersonalizada(vo);
        
        // 3. Atualizar estoque inteligente
        atualizarEstoqueInteligente(vo);
        
        // 4. Agendar follow-up
        agendarFollowUp(vo);
        
        // 5. Registrar métricas de vendas
        registrarMetricasVendas(vo);
    }
    
    private void aplicarRegrasComportamento(DynamicVO vo, AnaliseComportamento analise) throws Exception {
        // Regra 1: Cliente frequente - desconto automático
        if (analise.isClienteFrequente()) {
            BigDecimal desconto = vo.getProperty("VLRDESC");
            if (desconto == null) desconto = BigDecimal.ZERO;
            desconto = desconto.add(new BigDecimal("5.00")); // R$ 5,00 adicional
            vo.setProperty("VLRDESC", desconto);
        }
        
        // Regra 2: Cliente VIP - frete grátis
        if (analise.isClienteVIP()) {
            vo.setProperty("AD_FRETE_GRATIS", "S");
        }
        
        // Regra 3: Cliente com histórico de cancelamento - validação extra
        if (analise.temHistoricoCancelamento()) {
            vo.setProperty("AD_REQUER_APROVACAO", "S");
        }
    }
    
    private BigDecimal calcularDescontoInteligente(DynamicVO vo, AnaliseComportamento analise) {
        BigDecimal valorTotal = vo.getProperty("VLRNOTA");
        BigDecimal desconto = BigDecimal.ZERO;
        
        // Desconto por volume
        if (valorTotal.compareTo(new BigDecimal("1000.00")) > 0) {
            desconto = desconto.add(valorTotal.multiply(new BigDecimal("0.03"))); // 3%
        }
        
        // Desconto por fidelidade
        if (analise.getTempoCliente() > 12) { // Mais de 1 ano
            desconto = desconto.add(valorTotal.multiply(new BigDecimal("0.02"))); // 2%
        }
        
        // Desconto por frequência
        if (analise.getPedidosUltimos6Meses() > 10) {
            desconto = desconto.add(valorTotal.multiply(new BigDecimal("0.01"))); // 1%
        }
        
        return desconto;
    }
    
    private String definirPrioridadeEntrega(AnaliseComportamento analise) {
        if (analise.isClienteVIP()) {
            return "ALTA";
        } else if (analise.isClienteFrequente()) {
            return "MEDIA";
        } else {
            return "NORMAL";
        }
    }
    
    private void atualizarEstoqueInteligente(DynamicVO vo) throws Exception {
        // Implementar atualização inteligente de estoque
        // Considerar sazonalidade, tendências, etc.
    }
    
    private void agendarFollowUp(DynamicVO vo) throws Exception {
        // Agendar follow-up baseado no perfil do cliente
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7); // 7 dias após a compra
        
        // Criar tarefa de follow-up
        // Implementar criação de tarefa
    }
    
    private void registrarMetricasVendas(DynamicVO vo) throws Exception {
        // Registrar métricas para análise posterior
        // Implementar registro de métricas
    }
}

// Classe para análise de comportamento
class AnaliseComportamento {
    private boolean clienteFrequente;
    private boolean clienteVIP;
    private boolean temHistoricoCancelamento;
    private int tempoCliente; // em meses
    private int pedidosUltimos6Meses;
    
    // Getters e setters
    public boolean isClienteFrequente() { return clienteFrequente; }
    public void setClienteFrequente(boolean clienteFrequente) { this.clienteFrequente = clienteFrequente; }
    
    public boolean isClienteVIP() { return clienteVIP; }
    public void setClienteVIP(boolean clienteVIP) { this.clienteVIP = clienteVIP; }
    
    public boolean temHistoricoCancelamento() { return temHistoricoCancelamento; }
    public void setTemHistoricoCancelamento(boolean temHistoricoCancelamento) { this.temHistoricoCancelamento = temHistoricoCancelamento; }
    
    public int getTempoCliente() { return tempoCliente; }
    public void setTempoCliente(int tempoCliente) { this.tempoCliente = tempoCliente; }
    
    public int getPedidosUltimos6Meses() { return pedidosUltimos6Meses; }
    public void setPedidosUltimos6Meses(int pedidosUltimos6Meses) { this.pedidosUltimos6Meses = pedidosUltimos6Meses; }
}
```

## 🏗️ **Caso de Uso 2: Sistema de Controle de Qualidade Automatizado**

### **Contexto do Negócio**
Indústria alimentícia com necessidade de controle rigoroso de qualidade, rastreabilidade e conformidade com normas sanitárias.

### **Solução Implementada**
```java
// Sistema de Controle de Qualidade Automatizado
package br.com.sankhya.personalizacao.qualidade;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Sistema automatizado de controle de qualidade
 * Implementa validações rigorosas e rastreabilidade completa
 */
public class SistemaControleQualidadeAutomatizado {
    
    private SistemaRastreabilidade sistemaRastreabilidade;
    private SistemaValidacaoNormas sistemaValidacao;
    private SistemaAlertasQualidade sistemaAlertas;
    
    public SistemaControleQualidadeAutomatizado() {
        this.sistemaRastreabilidade = new SistemaRastreabilidade();
        this.sistemaValidacao = new SistemaValidacaoNormas();
        this.sistemaAlertas = new SistemaAlertasQualidade();
    }
    
    /**
     * Evento: Antes de inserir produto
     * Validações rigorosas de qualidade
     */
    public void beforeInsertProduto(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // 1. Validar conformidade com normas sanitárias
        validarConformidadeSanitaria(vo);
        
        // 2. Verificar rastreabilidade de ingredientes
        validarRastreabilidadeIngredientes(vo);
        
        // 3. Validar prazo de validade
        validarPrazoValidade(vo);
        
        // 4. Verificar certificações
        validarCertificacoes(vo);
        
        // 5. Calcular score de qualidade
        BigDecimal scoreQualidade = calcularScoreQualidade(vo);
        vo.setProperty("AD_SCORE_QUALIDADE", scoreQualidade);
    }
    
    /**
     * Evento: Após inserir produto
     * Ações pós-inserção para controle de qualidade
     */
    public void afterInsertProduto(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // 1. Criar registro de rastreabilidade
        sistemaRastreabilidade.criarRegistroRastreabilidade(vo);
        
        // 2. Agendar inspeções de qualidade
        agendarInspecoesQualidade(vo);
        
        // 3. Configurar alertas de validade
        configurarAlertasValidade(vo);
        
        // 4. Registrar no sistema de certificação
        registrarSistemaCertificacao(vo);
        
        // 5. Notificar equipe de qualidade
        sistemaAlertas.notificarEquipeQualidade(vo);
    }
    
    private void validarConformidadeSanitaria(DynamicVO vo) throws Exception {
        String categoria = vo.getProperty("AD_CATEGORIA");
        
        // Validações específicas por categoria
        switch (categoria) {
            case "ALIMENTOS_PERECIVEIS":
                validarAlimentosPereciveis(vo);
                break;
            case "ALIMENTOS_NAO_PERECIVEIS":
                validarAlimentosNaoPereciveis(vo);
                break;
            case "BEBIDAS":
                validarBebidas(vo);
                break;
            default:
                throw new Exception("Categoria de produto não reconhecida: " + categoria);
        }
    }
    
    private void validarAlimentosPereciveis(DynamicVO vo) throws Exception {
        // Validar temperatura de armazenamento
        BigDecimal temperatura = vo.getProperty("AD_TEMPERATURA_ARMAZENAMENTO");
        if (temperatura == null || temperatura.compareTo(new BigDecimal("4")) > 0) {
            throw new Exception("Alimentos perecíveis devem ser armazenados abaixo de 4°C");
        }
        
        // Validar prazo de validade
        Date dataValidade = vo.getProperty("AD_DATA_VALIDADE");
        if (dataValidade == null) {
            throw new Exception("Alimentos perecíveis devem ter data de validade definida");
        }
        
        // Validar lote
        String lote = vo.getProperty("AD_LOTE");
        if (lote == null || lote.trim().isEmpty()) {
            throw new Exception("Alimentos perecíveis devem ter lote definido");
        }
    }
    
    private void validarRastreabilidadeIngredientes(DynamicVO vo) throws Exception {
        String ingredientes = vo.getProperty("AD_INGREDIENTES");
        
        if (ingredientes != null && !ingredientes.trim().isEmpty()) {
            // Verificar se todos os ingredientes são rastreáveis
            String[] listaIngredientes = ingredientes.split(",");
            
            for (String ingrediente : listaIngredientes) {
                if (!sistemaRastreabilidade.isIngredienteRastreavel(ingrediente.trim())) {
                    throw new Exception("Ingrediente não rastreável: " + ingrediente);
                }
            }
        }
    }
    
    private void validarPrazoValidade(DynamicVO vo) throws Exception {
        Date dataValidade = vo.getProperty("AD_DATA_VALIDADE");
        Date dataFabricacao = vo.getProperty("AD_DATA_FABRICACAO");
        
        if (dataValidade != null && dataFabricacao != null) {
            long diasValidade = (dataValidade.getTime() - dataFabricacao.getTime()) / (1000 * 60 * 60 * 24);
            
            // Validar prazo mínimo de validade
            if (diasValidade < 7) {
                throw new Exception("Prazo de validade deve ser de pelo menos 7 dias");
            }
        }
    }
    
    private void validarCertificacoes(DynamicVO vo) throws Exception {
        String certificacoes = vo.getProperty("AD_CERTIFICACOES");
        
        if (certificacoes != null && !certificacoes.trim().isEmpty()) {
            String[] listaCertificacoes = certificacoes.split(",");
            
            for (String certificacao : listaCertificacoes) {
                if (!sistemaValidacao.isCertificacaoValida(certificacao.trim())) {
                    throw new Exception("Certificação inválida: " + certificacao);
                }
            }
        }
    }
    
    private BigDecimal calcularScoreQualidade(DynamicVO vo) {
        BigDecimal score = new BigDecimal("100"); // Score inicial
        
        // Reduzir score por problemas identificados
        String categoria = vo.getProperty("AD_CATEGORIA");
        if ("ALIMENTOS_PERECIVEIS".equals(categoria)) {
            score = score.subtract(new BigDecimal("10")); // Maior risco
        }
        
        // Verificar certificações
        String certificacoes = vo.getProperty("AD_CERTIFICACOES");
        if (certificacoes != null && certificacoes.contains("ISO22000")) {
            score = score.add(new BigDecimal("5")); // Bonus por certificação
        }
        
        return score;
    }
    
    private void agendarInspecoesQualidade(DynamicVO vo) throws Exception {
        // Agendar inspeções baseadas no tipo de produto
        String categoria = vo.getProperty("AD_CATEGORIA");
        
        if ("ALIMENTOS_PERECIVEIS".equals(categoria)) {
            // Inspeção diária
            agendarInspecao(vo, 1);
        } else if ("ALIMENTOS_NAO_PERECIVEIS".equals(categoria)) {
            // Inspeção semanal
            agendarInspecao(vo, 7);
        }
    }
    
    private void agendarInspecao(DynamicVO vo, int dias) throws Exception {
        // Implementar agendamento de inspeção
    }
    
    private void configurarAlertasValidade(DynamicVO vo) throws Exception {
        Date dataValidade = vo.getProperty("AD_DATA_VALIDADE");
        
        if (dataValidade != null) {
            // Configurar alerta 30 dias antes do vencimento
            Date dataAlerta = new Date(dataValidade.getTime() - (30L * 24 * 60 * 60 * 1000));
            sistemaAlertas.configurarAlertaValidade(vo, dataAlerta);
        }
    }
    
    private void registrarSistemaCertificacao(DynamicVO vo) throws Exception {
        // Registrar produto no sistema de certificação
        // Implementar registro
    }
}
```

## 🏗️ **Caso de Uso 3: Dashboard Executivo em Tempo Real**

### **Contexto do Negócio**
Empresa de grande porte necessitando de dashboard executivo com métricas em tempo real, alertas inteligentes e análises preditivas.

### **Solução Implementada**
```html
<!-- Dashboard Executivo em Tempo Real -->
<div class="dashboard-executivo-tempo-real" ng-controller="DashboardExecutivoController">
    <div class="dashboard-header">
        <h1>Dashboard Executivo - Tempo Real</h1>
        <div class="status-indicators">
            <span class="status-indicator online" ng-show="status.online">
                <i class="icon-wifi"></i> Online
            </span>
            <span class="status-indicator offline" ng-show="!status.online">
                <i class="icon-wifi-off"></i> Offline
            </span>
            <span class="last-update">
                Última atualização: {{ultimaAtualizacao | date:'dd/MM/yyyy HH:mm:ss'}}
            </span>
        </div>
    </div>
    
    <div class="dashboard-grid">
        <!-- KPIs Principais -->
        <div class="kpi-section">
            <div class="kpi-card" ng-repeat="kpi in kpis" ng-class="{'alert': kpi.alerta}">
                <div class="kpi-header">
                    <h3>{{kpi.titulo}}</h3>
                    <div class="kpi-trend" ng-class="kpi.tendencia">
                        <i class="icon" ng-class="kpi.tendencia === 'up' ? 'icon-trending-up' : 'icon-trending-down'"></i>
                        {{kpi.variacao}}%
                    </div>
                </div>
                <div class="kpi-value" ng-class="{'critical': kpi.critico}">
                    {{kpi.valor | currency:'R$ '}}
                </div>
                <div class="kpi-chart">
                    <canvas id="kpi-chart-{{$index}}" width="200" height="100"></canvas>
                </div>
                <div class="kpi-alert" ng-show="kpi.alerta">
                    <i class="icon-warning"></i>
                    {{kpi.mensagemAlerta}}
                </div>
            </div>
        </div>
        
        <!-- Gráfico Principal -->
        <div class="main-chart-section">
            <div class="chart-header">
                <h3>Evolução de Vendas - Tempo Real</h3>
                <div class="chart-controls">
                    <select ng-model="filtros.periodo" ng-change="atualizarGrafico()">
                        <option value="hoje">Hoje</option>
                        <option value="semana">Esta Semana</option>
                        <option value="mes">Este Mês</option>
                        <option value="trimestre">Este Trimestre</option>
                    </select>
                    <button ng-click="exportarGrafico()" class="btn-export">
                        <i class="icon-download"></i> Exportar
                    </button>
                </div>
            </div>
            <div class="chart-content">
                <canvas id="mainChart" width="800" height="400"></canvas>
            </div>
        </div>
        
        <!-- Alertas Inteligentes -->
        <div class="alertas-section">
            <h3>Alertas Inteligentes</h3>
            <div class="alertas-list">
                <div class="alerta-item" ng-repeat="alerta in alertas" ng-class="alerta.prioridade">
                    <div class="alerta-icon">
                        <i class="icon" ng-class="getIconAlerta(alerta.tipo)"></i>
                    </div>
                    <div class="alerta-content">
                        <h4>{{alerta.titulo}}</h4>
                        <p>{{alerta.descricao}}</p>
                        <span class="alerta-time">{{alerta.timestamp | date:'HH:mm:ss'}}</span>
                    </div>
                    <div class="alerta-actions">
                        <button ng-click="resolverAlerta(alerta)" class="btn-resolve">
                            <i class="icon-check"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Tabela de Dados em Tempo Real -->
        <div class="data-table-section">
            <h3>Vendas em Tempo Real</h3>
            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Cliente</th>
                            <th>Produto</th>
                            <th>Quantidade</th>
                            <th>Valor</th>
                            <th>Status</th>
                            <th>Timestamp</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr ng-repeat="venda in vendasTempoReal" ng-class="{'new-row': venda.novo}">
                            <td>{{venda.cliente}}</td>
                            <td>{{venda.produto}}</td>
                            <td>{{venda.quantidade}}</td>
                            <td>{{venda.valor | currency:'R$ '}}</td>
                            <td>
                                <span class="status-badge" ng-class="venda.status">
                                    {{venda.status}}
                                </span>
                            </td>
                            <td>{{venda.timestamp | date:'HH:mm:ss'}}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
// Controller para Dashboard Executivo em Tempo Real
function DashboardExecutivoController($scope, $http, $interval, $timeout) {
    $scope.kpis = [];
    $scope.alertas = [];
    $scope.vendasTempoReal = [];
    $scope.status = { online: true };
    $scope.ultimaAtualizacao = new Date();
    $scope.filtros = { periodo: 'hoje' };
    $scope.chart = null;
    
    // Inicializar dashboard
    $scope.init = function() {
        $scope.carregarKPIs();
        $scope.carregarAlertas();
        $scope.carregarVendasTempoReal();
        $scope.configurarAtualizacaoTempoReal();
        $scope.configurarWebSocket();
    };
    
    // Carregar KPIs
    $scope.carregarKPIs = function() {
        $http.get('/sankhya/dashboard/kpis-tempo-real')
            .then(function(response) {
                $scope.kpis = response.data;
                $scope.criarGraficosKPIs();
                $scope.verificarAlertasKPIs();
            });
    };
    
    // Carregar alertas
    $scope.carregarAlertas = function() {
        $http.get('/sankhya/dashboard/alertas')
            .then(function(response) {
                $scope.alertas = response.data;
            });
    };
    
    // Carregar vendas em tempo real
    $scope.carregarVendasTempoReal = function() {
        $http.get('/sankhya/dashboard/vendas-tempo-real')
            .then(function(response) {
                $scope.vendasTempoReal = response.data;
                $scope.criarGraficoPrincipal();
            });
    };
    
    // Configurar atualização em tempo real
    $scope.configurarAtualizacaoTempoReal = function() {
        // Atualizar KPIs a cada 30 segundos
        $interval(function() {
            $scope.carregarKPIs();
        }, 30000);
        
        // Atualizar vendas a cada 10 segundos
        $interval(function() {
            $scope.carregarVendasTempoReal();
        }, 10000);
        
        // Atualizar alertas a cada 60 segundos
        $interval(function() {
            $scope.carregarAlertas();
        }, 60000);
    };
    
    // Configurar WebSocket para atualizações em tempo real
    $scope.configurarWebSocket = function() {
        var ws = new WebSocket('ws://localhost:8080/sankhya/websocket/dashboard');
        
        ws.onopen = function() {
            $scope.status.online = true;
            $scope.$apply();
        };
        
        ws.onclose = function() {
            $scope.status.online = false;
            $scope.$apply();
        };
        
        ws.onmessage = function(event) {
            var data = JSON.parse(event.data);
            
            switch(data.tipo) {
                case 'KPI_UPDATE':
                    $scope.atualizarKPI(data.kpi);
                    break;
                case 'NOVA_VENDA':
                    $scope.adicionarNovaVenda(data.venda);
                    break;
                case 'ALERTA':
                    $scope.adicionarAlerta(data.alerta);
                    break;
            }
            
            $scope.$apply();
        };
    };
    
    // Atualizar KPI específico
    $scope.atualizarKPI = function(kpiData) {
        for (var i = 0; i < $scope.kpis.length; i++) {
            if ($scope.kpis[i].id === kpiData.id) {
                $scope.kpis[i] = kpiData;
                break;
            }
        }
    };
    
    // Adicionar nova venda
    $scope.adicionarNovaVenda = function(venda) {
        venda.novo = true;
        $scope.vendasTempoReal.unshift(venda);
        
        // Manter apenas as últimas 100 vendas
        if ($scope.vendasTempoReal.length > 100) {
            $scope.vendasTempoReal = $scope.vendasTempoReal.slice(0, 100);
        }
        
        // Remover flag de novo após 5 segundos
        $timeout(function() {
            venda.novo = false;
        }, 5000);
    };
    
    // Adicionar alerta
    $scope.adicionarAlerta = function(alerta) {
        $scope.alertas.unshift(alerta);
        
        // Manter apenas os últimos 50 alertas
        if ($scope.alertas.length > 50) {
            $scope.alertas = $scope.alertas.slice(0, 50);
        }
    };
    
    // Verificar alertas nos KPIs
    $scope.verificarAlertasKPIs = function() {
        for (var i = 0; i < $scope.kpis.length; i++) {
            var kpi = $scope.kpis[i];
            
            // Verificar se KPI está em nível crítico
            if (kpi.valor < kpi.valorMinimo) {
                kpi.critico = true;
                kpi.alerta = true;
                kpi.mensagemAlerta = "Valor abaixo do mínimo esperado";
            } else if (kpi.valor > kpi.valorMaximo) {
                kpi.critico = true;
                kpi.alerta = true;
                kpi.mensagemAlerta = "Valor acima do máximo esperado";
            } else {
                kpi.critico = false;
                kpi.alerta = false;
            }
        }
    };
    
    // Criar gráficos dos KPIs
    $scope.criarGraficosKPIs = function() {
        $timeout(function() {
            $scope.kpis.forEach(function(kpi, index) {
                var ctx = document.getElementById('kpi-chart-' + index);
                if (ctx) {
                    new Chart(ctx, {
                        type: 'line',
                        data: {
                            labels: kpi.labels,
                            datasets: [{
                                data: kpi.values,
                                borderColor: kpi.cor,
                                backgroundColor: kpi.cor + '20',
                                tension: 0.4,
                                fill: true
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            plugins: {
                                legend: { display: false }
                            },
                            scales: {
                                x: { display: false },
                                y: { display: false }
                            },
                            elements: {
                                point: { radius: 0 }
                            }
                        }
                    });
                }
            });
        }, 100);
    };
    
    // Criar gráfico principal
    $scope.criarGraficoPrincipal = function() {
        var ctx = document.getElementById('mainChart');
        if (ctx) {
            if ($scope.chart) {
                $scope.chart.destroy();
            }
            
            $scope.chart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: $scope.vendasTempoReal.map(v => v.timestamp),
                    datasets: [{
                        label: 'Vendas',
                        data: $scope.vendasTempoReal.map(v => v.valor),
                        borderColor: 'rgb(75, 192, 192)',
                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                        tension: 0.4,
                        fill: true
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: true },
                        tooltip: {
                            mode: 'index',
                            intersect: false
                        }
                    },
                    scales: {
                        x: {
                            display: true,
                            title: { display: true, text: 'Tempo' }
                        },
                        y: {
                            display: true,
                            title: { display: true, text: 'Valor (R$)' }
                        }
                    },
                    animation: {
                        duration: 0 // Desabilitar animação para tempo real
                    }
                }
            });
        }
    };
    
    // Obter ícone do alerta
    $scope.getIconAlerta = function(tipo) {
        switch(tipo) {
            case 'CRITICO': return 'icon-alert-triangle';
            case 'AVISO': return 'icon-alert-circle';
            case 'INFO': return 'icon-info';
            default: return 'icon-bell';
        }
    };
    
    // Resolver alerta
    $scope.resolverAlerta = function(alerta) {
        $http.post('/sankhya/dashboard/resolver-alerta', { id: alerta.id })
            .then(function(response) {
                // Remover alerta da lista
                var index = $scope.alertas.indexOf(alerta);
                if (index > -1) {
                    $scope.alertas.splice(index, 1);
                }
            });
    };
    
    // Exportar gráfico
    $scope.exportarGrafico = function() {
        var canvas = document.getElementById('mainChart');
        var url = canvas.toDataURL('image/png');
        var link = document.createElement('a');
        link.download = 'dashboard-executivo-' + new Date().toISOString().split('T')[0] + '.png';
        link.href = url;
        link.click();
    };
    
    // Inicializar quando o controller carregar
    $scope.init();
}
</script>
```

## 📊 **Métricas dos Exemplos Avançados**

### **Casos de Uso Implementados**
- **Sistema de Gestão de Vendas Inteligente**: Automação e análise de comportamento
- **Sistema de Controle de Qualidade**: Validações rigorosas e rastreabilidade
- **Dashboard Executivo em Tempo Real**: Métricas e alertas inteligentes

### **Tecnologias Utilizadas**
- **Java**: Eventos programados e validações
- **SankhyaJS**: Componentes HTML5 interativos
- **WebSocket**: Comunicação em tempo real
- **Chart.js**: Gráficos dinâmicos
- **AngularJS**: Framework frontend

### **Benefícios Alcançados**
- **Automação**: Processos automatizados
- **Inteligência**: Análise e recomendações
- **Tempo Real**: Atualizações instantâneas
- **Qualidade**: Validações rigorosas
- **Eficiência**: Otimização de processos

---

*Estes exemplos representam casos de uso reais e complexos de personalizações Sankhya, demonstrando a aplicação prática de conhecimentos avançados em cenários empresariais.*
