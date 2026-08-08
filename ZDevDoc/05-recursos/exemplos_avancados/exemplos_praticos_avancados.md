# 🚀 Exemplos Práticos Avançados - Sankhya

## 🎯 **Visão Geral dos Exemplos Avançados**

Esta seção contém exemplos práticos e avançados baseados no conhecimento extraído de todo o ecossistema Sankhya, incluindo [Sankhya Developer](https://developer.sankhya.com.br/), [Ajuda Sankhya](https://ajuda.sankhya.com.br/), [Comunidade Sankhya](https://comunidade.sankhya.com.br/), [Place Sankhya](https://place.sankhya.com.br/) e [Universidade Sankhya](https://universidade.sankhya.com.br/).

## 🏗️ **Arquitetura de Solução Enterprise**

### **1. Sistema de E-commerce Completo**

#### **Arquitetura da Solução**
```javascript
// Baseado nos recursos do Sankhya Developer e Place Sankhya
class EcommerceEnterpriseSolution {
    constructor(config) {
        this.config = config;
        this.apiClient = new SankhyaAPIClient(config.api);
        this.cacheManager = new CacheManager(config.cache);
        this.eventBus = new EventBus();
        this.monitoring = new MonitoringSystem();
        this.analytics = new AnalyticsEngine();
    }

    async initialize() {
        try {
            // 1. Configurar autenticação
            await this.setupAuthentication();
            
            // 2. Inicializar cache
            await this.cacheManager.initialize();
            
            // 3. Configurar eventos
            await this.setupEventHandlers();
            
            // 4. Inicializar monitoramento
            await this.monitoring.start();
            
            // 5. Configurar analytics
            await this.analytics.initialize();
            
            console.log('Sistema E-commerce Enterprise inicializado com sucesso');
        } catch (error) {
            console.error('Erro na inicialização:', error);
            throw error;
        }
    }

    async setupAuthentication() {
        // Baseado na documentação da API de Integração
        const authConfig = {
            baseURL: this.config.api.baseURL,
            username: this.config.api.username,
            password: this.config.api.password,
            grantType: 'password',
            scope: 'read write admin'
        };
        
        await this.apiClient.authenticate(authConfig);
    }

    async setupEventHandlers() {
        // Baseado nos recursos de Eventos Programados
        this.eventBus.on('order.created', this.handleOrderCreated.bind(this));
        this.eventBus.on('payment.processed', this.handlePaymentProcessed.bind(this));
        this.eventBus.on('inventory.updated', this.handleInventoryUpdated.bind(this));
        this.eventBus.on('customer.registered', this.handleCustomerRegistered.bind(this));
    }

    async handleOrderCreated(orderData) {
        try {
            // 1. Validar dados do pedido
            await this.validateOrderData(orderData);
            
            // 2. Verificar estoque
            await this.checkInventory(orderData.items);
            
            // 3. Calcular impostos
            const taxes = await this.calculateTaxes(orderData);
            
            // 4. Aplicar descontos
            const discounts = await this.applyDiscounts(orderData);
            
            // 5. Criar pedido no Sankhya
            const sankhyaOrder = await this.createSankhyaOrder({
                ...orderData,
                taxes,
                discounts
            });
            
            // 6. Enviar confirmação
            await this.sendOrderConfirmation(sankhyaOrder);
            
            // 7. Atualizar analytics
            await this.analytics.trackEvent('order.created', sankhyaOrder);
            
        } catch (error) {
            console.error('Erro ao processar pedido:', error);
            await this.handleOrderError(orderData, error);
        }
    }

    async createSankhyaOrder(orderData) {
        // Baseado nos endpoints da API de Integração
        const orderPayload = {
            cliente_id: orderData.customer.id,
            data_pedido: new Date().toISOString().split('T')[0],
            observacoes: `Pedido do e-commerce: ${orderData.id}`,
            desconto_percentual: orderData.discounts.percentage,
            desconto_valor: orderData.discounts.amount,
            itens: orderData.items.map(item => ({
                produto_id: item.product.id,
                quantidade: item.quantity,
                preco_unitario: item.price,
                desconto_percentual: item.discount.percentage,
                desconto_valor: item.discount.amount
            }))
        };

        return await this.apiClient.makeRequest('POST', '/pedidos', orderPayload);
    }
}
```

#### **Sistema de Sincronização em Tempo Real**
```python
# Baseado nos recursos da Comunidade Sankhya e Universidade Sankhya
import asyncio
import websockets
import json
from typing import Dict, List, Optional

class RealTimeSyncSystem:
    def __init__(self, api_client, config):
        self.api_client = api_client
        self.config = config
        self.websocket = None
        self.sync_queues = {
            'products': asyncio.Queue(),
            'customers': asyncio.Queue(),
            'orders': asyncio.Queue(),
            'inventory': asyncio.Queue()
        }
        self.sync_workers = {}
        self.monitoring = SyncMonitoring()

    async def start(self):
        """Iniciar sistema de sincronização em tempo real"""
        try:
            # 1. Conectar WebSocket
            await self.connect_websocket()
            
            # 2. Iniciar workers de sincronização
            await self.start_sync_workers()
            
            # 3. Iniciar monitoramento
            await self.monitoring.start()
            
            # 4. Configurar heartbeat
            await self.setup_heartbeat()
            
            print('Sistema de sincronização em tempo real iniciado')
            
        except Exception as e:
            print(f'Erro ao iniciar sincronização: {e}')
            raise

    async def connect_websocket(self):
        """Conectar ao WebSocket do Sankhya"""
        websocket_url = f"wss://{self.config.websocket.host}/ws/sync"
        
        self.websocket = await websockets.connect(
            websocket_url,
            extra_headers={
                'Authorization': f'Bearer {self.api_client.token}'
            }
        )
        
        # Configurar handlers de mensagem
        asyncio.create_task(self.handle_websocket_messages())

    async def handle_websocket_messages(self):
        """Processar mensagens do WebSocket"""
        async for message in self.websocket:
            try:
                data = json.loads(message)
                await self.process_sync_message(data)
            except Exception as e:
                print(f'Erro ao processar mensagem: {e}')

    async def process_sync_message(self, data: Dict):
        """Processar mensagem de sincronização"""
        message_type = data.get('type')
        entity_type = data.get('entity_type')
        entity_data = data.get('data')
        
        if message_type == 'sync' and entity_type in self.sync_queues:
            await self.sync_queues[entity_type].put(entity_data)
            await self.monitoring.record_sync_event(entity_type, 'received')

    async def start_sync_workers(self):
        """Iniciar workers de sincronização"""
        for entity_type, queue in self.sync_queues.items():
            worker = asyncio.create_task(
                self.sync_worker(entity_type, queue)
            )
            self.sync_workers[entity_type] = worker

    async def sync_worker(self, entity_type: str, queue: asyncio.Queue):
        """Worker de sincronização para tipo de entidade"""
        while True:
            try:
                # Aguardar dados para sincronizar
                data = await queue.get()
                
                # Processar sincronização
                await self.process_entity_sync(entity_type, data)
                
                # Marcar tarefa como concluída
                queue.task_done()
                
            except Exception as e:
                print(f'Erro no worker {entity_type}: {e}')
                await self.monitoring.record_sync_error(entity_type, str(e))

    async def process_entity_sync(self, entity_type: str, data: Dict):
        """Processar sincronização de entidade específica"""
        try:
            if entity_type == 'products':
                await self.sync_product(data)
            elif entity_type == 'customers':
                await self.sync_customer(data)
            elif entity_type == 'orders':
                await self.sync_order(data)
            elif entity_type == 'inventory':
                await self.sync_inventory(data)
            
            await self.monitoring.record_sync_event(entity_type, 'processed')
            
        except Exception as e:
            print(f'Erro ao sincronizar {entity_type}: {e}')
            raise

    async def sync_product(self, product_data: Dict):
        """Sincronizar produto"""
        # Verificar se produto existe
        existing_product = await self.find_product_by_sku(product_data['sku'])
        
        if existing_product:
            # Atualizar produto existente
            await self.api_client.make_request(
                'PUT', 
                f'/produto/{existing_product["id"]}', 
                product_data
            )
        else:
            # Criar novo produto
            await self.api_client.make_request('POST', '/produto', product_data)
```

### **2. Sistema de Business Intelligence Avançado**

#### **Dashboard Executivo em Tempo Real**
```javascript
// Baseado nos recursos de Business Intelligence da Universidade Sankhya
class ExecutiveDashboard {
    constructor(apiClient, config) {
        this.api = apiClient;
        this.config = config;
        this.charts = {};
        this.dataCache = new Map();
        this.refreshInterval = 30000; // 30 segundos
        this.websocket = null;
    }

    async initialize() {
        try {
            // 1. Configurar layout responsivo
            await this.setupResponsiveLayout();
            
            // 2. Inicializar gráficos
            await this.initializeCharts();
            
            // 3. Conectar WebSocket para dados em tempo real
            await this.connectWebSocket();
            
            // 4. Configurar atualizações automáticas
            this.setupAutoRefresh();
            
            // 5. Carregar dados iniciais
            await this.loadInitialData();
            
            console.log('Dashboard Executivo inicializado');
        } catch (error) {
            console.error('Erro ao inicializar dashboard:', error);
        }
    }

    async setupResponsiveLayout() {
        // Baseado nos recursos de SankhyaJS
        const layout = {
            desktop: {
                columns: 4,
                widgets: [
                    { id: 'sales-overview', size: 'large', position: { x: 0, y: 0 } },
                    { id: 'revenue-chart', size: 'medium', position: { x: 2, y: 0 } },
                    { id: 'top-products', size: 'medium', position: { x: 0, y: 1 } },
                    { id: 'customer-metrics', size: 'medium', position: { x: 2, y: 1 } },
                    { id: 'inventory-alerts', size: 'small', position: { x: 0, y: 2 } },
                    { id: 'performance-metrics', size: 'small', position: { x: 1, y: 2 } }
                ]
            },
            tablet: {
                columns: 2,
                widgets: [
                    { id: 'sales-overview', size: 'large', position: { x: 0, y: 0 } },
                    { id: 'revenue-chart', size: 'medium', position: { x: 0, y: 1 } },
                    { id: 'top-products', size: 'medium', position: { x: 1, y: 1 } }
                ]
            },
            mobile: {
                columns: 1,
                widgets: [
                    { id: 'sales-overview', size: 'large', position: { x: 0, y: 0 } },
                    { id: 'revenue-chart', size: 'large', position: { x: 0, y: 1 } }
                ]
            }
        };

        this.layout = layout;
        await this.applyLayout();
    }

    async initializeCharts() {
        // Gráfico de vendas com SankhyaJS
        this.charts.salesOverview = new SankhyaChart('sales-overview', {
            type: 'line',
            data: await this.getSalesData(),
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: {
                    duration: 1000,
                    easing: 'easeInOutQuart'
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                scales: {
                    x: {
                        display: true,
                        title: {
                            display: true,
                            text: 'Período'
                        }
                    },
                    y: {
                        display: true,
                        title: {
                            display: true,
                            text: 'Valor (R$)'
                        }
                    }
                }
            }
        });

        // Gráfico de receita
        this.charts.revenueChart = new SankhyaChart('revenue-chart', {
            type: 'doughnut',
            data: await this.getRevenueData(),
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });

        // Gráfico de produtos mais vendidos
        this.charts.topProducts = new SankhyaChart('top-products', {
            type: 'bar',
            data: await this.getTopProductsData(),
            options: {
                responsive: true,
                maintainAspectRatio: false,
                indexAxis: 'y',
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });
    }

    async connectWebSocket() {
        // Baseado nos recursos de WebSocket da API
        const wsUrl = `wss://${this.config.websocket.host}/ws/dashboard`;
        
        this.websocket = new WebSocket(wsUrl);
        
        this.websocket.onopen = () => {
            console.log('WebSocket conectado');
            this.websocket.send(JSON.stringify({
                type: 'subscribe',
                channels: ['sales', 'revenue', 'inventory', 'customers']
            }));
        };
        
        this.websocket.onmessage = (event) => {
            const data = JSON.parse(event.data);
            this.handleRealtimeData(data);
        };
        
        this.websocket.onerror = (error) => {
            console.error('Erro no WebSocket:', error);
        };
    }

    async handleRealtimeData(data) {
        // Processar dados em tempo real
        switch (data.type) {
            case 'sales_update':
                await this.updateSalesChart(data.data);
                break;
            case 'revenue_update':
                await this.updateRevenueChart(data.data);
                break;
            case 'inventory_alert':
                await this.showInventoryAlert(data.data);
                break;
            case 'customer_activity':
                await this.updateCustomerMetrics(data.data);
                break;
        }
    }

    async getSalesData() {
        // Baseado nos endpoints da API de Integração
        const response = await this.api.makeRequest('GET', '/relatorios/vendas', {
            periodo: '30_dias',
            agrupamento: 'diario',
            formato: 'chart'
        });
        
        return {
            labels: response.data.labels,
            datasets: [{
                label: 'Vendas',
                data: response.data.values,
                borderColor: 'rgb(75, 192, 192)',
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                tension: 0.1
            }]
        };
    }

    async getRevenueData() {
        const response = await this.api.makeRequest('GET', '/relatorios/receita', {
            periodo: 'mes_atual',
            agrupamento: 'categoria'
        });
        
        return {
            labels: response.data.categories,
            datasets: [{
                data: response.data.values,
                backgroundColor: [
                    '#FF6384',
                    '#36A2EB',
                    '#FFCE56',
                    '#4BC0C0',
                    '#9966FF'
                ]
            }]
        };
    }
}
```

### **3. Sistema de Automação Inteligente**

#### **Workflow de Aprovação Automática**
```python
# Baseado nos recursos de Automação de Processos da Ajuda Sankhya
import asyncio
from datetime import datetime, timedelta
from typing import Dict, List, Optional
from enum import Enum

class ApprovalStatus(Enum):
    PENDING = "pending"
    APPROVED = "approved"
    REJECTED = "rejected"
    EXPIRED = "expired"

class ApprovalWorkflow:
    def __init__(self, api_client, config):
        self.api = api_client
        self.config = config
        self.workflows = {}
        self.approval_rules = {}
        self.notification_service = NotificationService()
        self.audit_logger = AuditLogger()

    async def initialize(self):
        """Inicializar sistema de workflows"""
        try:
            # 1. Carregar workflows configurados
            await self.load_workflows()
            
            # 2. Carregar regras de aprovação
            await self.load_approval_rules()
            
            # 3. Configurar event handlers
            await self.setup_event_handlers()
            
            # 4. Iniciar processamento de workflows
            await self.start_workflow_processor()
            
            print('Sistema de workflows inicializado')
            
        except Exception as e:
            print(f'Erro ao inicializar workflows: {e}')
            raise

    async def load_workflows(self):
        """Carregar workflows do banco de dados"""
        # Baseado nos recursos de Workflows da Ajuda Sankhya
        workflows_data = await self.api.make_request('GET', '/workflows')
        
        for workflow_data in workflows_data['data']:
            workflow = WorkflowDefinition(
                id=workflow_data['id'],
                name=workflow_data['name'],
                description=workflow_data['description'],
                steps=workflow_data['steps'],
                conditions=workflow_data['conditions'],
                actions=workflow_data['actions']
            )
            self.workflows[workflow.id] = workflow

    async def load_approval_rules(self):
        """Carregar regras de aprovação"""
        rules_data = await self.api.make_request('GET', '/approval-rules')
        
        for rule_data in rules_data['data']:
            rule = ApprovalRule(
                id=rule_data['id'],
                name=rule_data['name'],
                conditions=rule_data['conditions'],
                approvers=rule_data['approvers'],
                timeout=rule_data['timeout'],
                escalation=rule_data['escalation']
            )
            self.approval_rules[rule.id] = rule

    async def setup_event_handlers(self):
        """Configurar event handlers"""
        # Baseado nos recursos de Eventos Programados
        event_handlers = {
            'order.created': self.handle_order_created,
            'payment.processed': self.handle_payment_processed,
            'inventory.low': self.handle_inventory_low,
            'customer.registered': self.handle_customer_registered,
            'approval.required': self.handle_approval_required,
            'approval.completed': self.handle_approval_completed
        }
        
        for event_type, handler in event_handlers.items():
            await self.api.subscribe_event(event_type, handler)

    async def handle_order_created(self, order_data: Dict):
        """Processar criação de pedido"""
        try:
            # 1. Verificar se precisa de aprovação
            requires_approval = await self.check_approval_required(order_data)
            
            if requires_approval:
                # 2. Iniciar processo de aprovação
                approval_id = await self.start_approval_process(order_data)
                
                # 3. Enviar notificações
                await self.send_approval_notifications(approval_id)
                
                # 4. Registrar no log de auditoria
                await self.audit_logger.log_event('approval.started', {
                    'order_id': order_data['id'],
                    'approval_id': approval_id,
                    'timestamp': datetime.now()
                })
            else:
                # Aprovação automática
                await self.auto_approve_order(order_data)
                
        except Exception as e:
            print(f'Erro ao processar pedido: {e}')
            await self.handle_workflow_error('order.created', order_data, e)

    async def check_approval_required(self, order_data: Dict) -> bool:
        """Verificar se pedido precisa de aprovação"""
        # Baseado nas regras de aprovação configuradas
        for rule in self.approval_rules.values():
            if await self.evaluate_rule_conditions(rule, order_data):
                return True
        return False

    async def evaluate_rule_conditions(self, rule: ApprovalRule, data: Dict) -> bool:
        """Avaliar condições da regra"""
        for condition in rule.conditions:
            if not await self.evaluate_condition(condition, data):
                return False
        return True

    async def evaluate_condition(self, condition: Dict, data: Dict) -> bool:
        """Avaliar condição específica"""
        field = condition['field']
        operator = condition['operator']
        value = condition['value']
        
        data_value = self.get_nested_value(data, field)
        
        if operator == 'equals':
            return data_value == value
        elif operator == 'greater_than':
            return data_value > value
        elif operator == 'less_than':
            return data_value < value
        elif operator == 'contains':
            return value in str(data_value)
        elif operator == 'in':
            return data_value in value
        
        return False

    async def start_approval_process(self, order_data: Dict) -> str:
        """Iniciar processo de aprovação"""
        # 1. Criar registro de aprovação
        approval_data = {
            'order_id': order_data['id'],
            'status': ApprovalStatus.PENDING.value,
            'created_at': datetime.now().isoformat(),
            'expires_at': (datetime.now() + timedelta(hours=24)).isoformat(),
            'approvers': await self.get_approvers(order_data),
            'current_step': 0,
            'workflow_id': await self.get_workflow_for_order(order_data)
        }
        
        response = await self.api.make_request('POST', '/approvals', approval_data)
        return response['id']

    async def get_approvers(self, order_data: Dict) -> List[Dict]:
        """Obter lista de aprovadores"""
        # Baseado nas regras de aprovação
        approvers = []
        
        for rule in self.approval_rules.values():
            if await self.evaluate_rule_conditions(rule, order_data):
                approvers.extend(rule.approvers)
        
        # Remover duplicatas
        unique_approvers = []
        seen = set()
        for approver in approvers:
            if approver['id'] not in seen:
                unique_approvers.append(approver)
                seen.add(approver['id'])
        
        return unique_approvers

    async def send_approval_notifications(self, approval_id: str):
        """Enviar notificações de aprovação"""
        approval = await self.get_approval(approval_id)
        
        for approver in approval['approvers']:
            notification = {
                'type': 'approval_required',
                'recipient': approver['email'],
                'subject': f'Aprovação necessária - Pedido {approval["order_id"]}',
                'data': {
                    'approval_id': approval_id,
                    'order_id': approval['order_id'],
                    'approver_name': approver['name'],
                    'approval_url': f"{self.config.approval_url}/{approval_id}"
                }
            }
            
            await self.notification_service.send(notification)

    async def handle_approval_completed(self, approval_data: Dict):
        """Processar aprovação concluída"""
        try:
            approval_id = approval_data['approval_id']
            status = approval_data['status']
            
            # 1. Atualizar status da aprovação
            await self.update_approval_status(approval_id, status)
            
            # 2. Processar resultado da aprovação
            if status == ApprovalStatus.APPROVED.value:
                await self.process_approved_order(approval_data)
            elif status == ApprovalStatus.REJECTED.value:
                await self.process_rejected_order(approval_data)
            
            # 3. Enviar notificações
            await self.send_approval_result_notifications(approval_data)
            
            # 4. Registrar no log de auditoria
            await self.audit_logger.log_event('approval.completed', approval_data)
            
        except Exception as e:
            print(f'Erro ao processar aprovação: {e}')
            await self.handle_workflow_error('approval.completed', approval_data, e)

    async def process_approved_order(self, approval_data: Dict):
        """Processar pedido aprovado"""
        order_id = approval_data['order_id']
        
        # 1. Atualizar status do pedido
        await self.api.make_request('PUT', f'/pedidos/{order_id}', {
            'status': 'APROVADO',
            'approved_at': datetime.now().isoformat(),
            'approved_by': approval_data['approver_id']
        })
        
        # 2. Processar faturamento
        await self.process_order_billing(order_id)
        
        # 3. Atualizar estoque
        await self.update_inventory(order_id)
        
        # 4. Enviar confirmação
        await self.send_order_confirmation(order_id)

    async def process_rejected_order(self, approval_data: Dict):
        """Processar pedido rejeitado"""
        order_id = approval_data['order_id']
        
        # 1. Atualizar status do pedido
        await self.api.make_request('PUT', f'/pedidos/{order_id}', {
            'status': 'REJEITADO',
            'rejected_at': datetime.now().isoformat(),
            'rejected_by': approval_data['approver_id'],
            'rejection_reason': approval_data['reason']
        })
        
        # 2. Notificar cliente
        await self.notify_customer_rejection(order_id, approval_data['reason'])
        
        # 3. Liberar estoque reservado
        await self.release_reserved_inventory(order_id)
```

### **4. Sistema de Monitoramento e Alertas**

#### **Monitor de Performance em Tempo Real**
```javascript
// Baseado nos recursos de Monitoramento da Comunidade Sankhya
class PerformanceMonitor {
    constructor(apiClient, config) {
        this.api = apiClient;
        this.config = config;
        this.metrics = new Map();
        this.alerts = new Map();
        this.thresholds = config.thresholds;
        this.alertService = new AlertService();
        this.dashboard = new MonitoringDashboard();
    }

    async initialize() {
        try {
            // 1. Configurar métricas
            await this.setupMetrics();
            
            // 2. Configurar alertas
            await this.setupAlerts();
            
            // 3. Iniciar coleta de métricas
            await this.startMetricsCollection();
            
            // 4. Iniciar dashboard
            await this.dashboard.initialize();
            
            console.log('Monitor de Performance inicializado');
        } catch (error) {
            console.error('Erro ao inicializar monitor:', error);
        }
    }

    async setupMetrics() {
        // Baseado nos recursos de métricas da API
        const metricsConfig = [
            {
                name: 'api_response_time',
                type: 'gauge',
                description: 'Tempo de resposta da API',
                unit: 'ms',
                collection_interval: 5000
            },
            {
                name: 'api_requests_per_second',
                type: 'counter',
                description: 'Requisições por segundo',
                unit: 'req/s',
                collection_interval: 1000
            },
            {
                name: 'database_connections',
                type: 'gauge',
                description: 'Conexões ativas do banco',
                unit: 'connections',
                collection_interval: 10000
            },
            {
                name: 'memory_usage',
                type: 'gauge',
                description: 'Uso de memória',
                unit: 'MB',
                collection_interval: 5000
            },
            {
                name: 'cpu_usage',
                type: 'gauge',
                description: 'Uso de CPU',
                unit: '%',
                collection_interval: 5000
            }
        ];

        for (const metricConfig of metricsConfig) {
            const metric = new Metric(metricConfig);
            this.metrics.set(metric.name, metric);
        }
    }

    async setupAlerts() {
        // Baseado nos recursos de alertas da Ajuda Sankhya
        const alertsConfig = [
            {
                name: 'high_response_time',
                metric: 'api_response_time',
                condition: 'greater_than',
                threshold: 2000,
                severity: 'warning',
                message: 'Tempo de resposta da API acima do normal'
            },
            {
                name: 'critical_response_time',
                metric: 'api_response_time',
                condition: 'greater_than',
                threshold: 5000,
                severity: 'critical',
                message: 'Tempo de resposta da API crítico'
            },
            {
                name: 'high_memory_usage',
                metric: 'memory_usage',
                condition: 'greater_than',
                threshold: 80,
                severity: 'warning',
                message: 'Uso de memória alto'
            },
            {
                name: 'database_connection_limit',
                metric: 'database_connections',
                condition: 'greater_than',
                threshold: 90,
                severity: 'critical',
                message: 'Limite de conexões do banco quase atingido'
            }
        ];

        for (const alertConfig of alertsConfig) {
            const alert = new Alert(alertConfig);
            this.alerts.set(alert.name, alert);
        }
    }

    async startMetricsCollection() {
        // Iniciar coleta de métricas
        for (const [name, metric] of this.metrics) {
            setInterval(async () => {
                try {
                    const value = await this.collectMetricValue(metric);
                    await this.updateMetric(metric, value);
                    await this.checkAlerts(metric, value);
                } catch (error) {
                    console.error(`Erro ao coletar métrica ${name}:`, error);
                }
            }, metric.collection_interval);
        }
    }

    async collectMetricValue(metric) {
        // Coletar valor da métrica baseado no tipo
        switch (metric.name) {
            case 'api_response_time':
                return await this.measureApiResponseTime();
            case 'api_requests_per_second':
                return await this.countApiRequests();
            case 'database_connections':
                return await this.getDatabaseConnections();
            case 'memory_usage':
                return await this.getMemoryUsage();
            case 'cpu_usage':
                return await this.getCpuUsage();
            default:
                return 0;
        }
    }

    async measureApiResponseTime() {
        const start = Date.now();
        try {
            await this.api.makeRequest('GET', '/health');
            return Date.now() - start;
        } catch (error) {
            return -1; // Indica erro
        }
    }

    async checkAlerts(metric, value) {
        // Verificar alertas para a métrica
        for (const [alertName, alert] of this.alerts) {
            if (alert.metric === metric.name) {
                const shouldTrigger = this.evaluateAlertCondition(alert, value);
                
                if (shouldTrigger) {
                    await this.triggerAlert(alert, value);
                }
            }
        }
    }

    evaluateAlertCondition(alert, value) {
        // Avaliar condição do alerta
        switch (alert.condition) {
            case 'greater_than':
                return value > alert.threshold;
            case 'less_than':
                return value < alert.threshold;
            case 'equals':
                return value === alert.threshold;
            default:
                return false;
        }
    }

    async triggerAlert(alert, value) {
        // Disparar alerta
        const alertData = {
            name: alert.name,
            metric: alert.metric,
            value: value,
            threshold: alert.threshold,
            severity: alert.severity,
            message: alert.message,
            timestamp: new Date().toISOString()
        };

        // 1. Registrar alerta
        await this.recordAlert(alertData);
        
        // 2. Enviar notificação
        await this.alertService.sendAlert(alertData);
        
        // 3. Atualizar dashboard
        await this.dashboard.updateAlert(alertData);
        
        // 4. Executar ações automáticas
        await this.executeAlertActions(alert, alertData);
    }

    async executeAlertActions(alert, alertData) {
        // Executar ações automáticas baseadas no alerta
        switch (alert.name) {
            case 'critical_response_time':
                // Escalar para equipe de suporte
                await this.escalateToSupport(alertData);
                break;
            case 'database_connection_limit':
                // Tentar liberar conexões
                await this.releaseDatabaseConnections();
                break;
            case 'high_memory_usage':
                // Limpar cache
                await this.clearCache();
                break;
        }
    }
}
```

## 📊 **Métricas de Conhecimento Aplicado**

### **Fontes Utilizadas**
- **Sankhya Developer**: [https://developer.sankhya.com.br/](https://developer.sankhya.com.br/)
- **Ajuda Sankhya**: [https://ajuda.sankhya.com.br/](https://ajuda.sankhya.com.br/)
- **Comunidade Sankhya**: [https://comunidade.sankhya.com.br/](https://comunidade.sankhya.com.br/)
- **Place Sankhya**: [https://place.sankhya.com.br/](https://place.sankhya.com.br/)
- **Universidade Sankhya**: [https://universidade.sankhya.com.br/](https://universidade.sankhya.com.br/)

### **Tecnologias Aplicadas**
- **SankhyaJS**: Framework JavaScript para componentes HTML5
- **API de Integração**: Endpoints REST para conectividade
- **Eventos Programados**: Triggers e validações automáticas
- **Botões de Ação**: Automação de processos
- **Workflows**: Processos de aprovação e automação
- **Business Intelligence**: Dashboards e relatórios
- **Monitoramento**: Métricas e alertas em tempo real

### **Padrões Implementados**
- **Arquitetura Enterprise**: Soluções escaláveis e robustas
- **Microservices**: Serviços independentes e especializados
- **Event-Driven**: Arquitetura baseada em eventos
- **Real-time**: Sincronização e atualizações em tempo real
- **Monitoring**: Observabilidade e alertas proativos
- **Security**: Segurança e auditoria

---

*Estes exemplos foram criados com base no conhecimento extraído de todo o ecossistema Sankhya e representam aplicações práticas e avançadas da plataforma.*
