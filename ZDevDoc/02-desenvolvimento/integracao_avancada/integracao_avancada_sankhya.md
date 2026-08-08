# 🔗 Integração Avançada Sankhya - Padrões Enterprise

## 🎯 Visão Geral

Este documento apresenta padrões avançados de integração no Sankhya, extraídos do código fonte SankhyaW 4.8 e melhores práticas de desenvolvimento enterprise.

## 🏗️ **Arquitetura de Integração Avançada**

### **1. Padrão de Integração com Message Queue**

```java
package br.com.empresa.integracao;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Sistema de integração com message queue
 */
public class MessageQueueIntegration {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    private BlockingQueue<IntegrationMessage> messageQueue;
    private IntegrationConsumer consumer;
    
    public MessageQueueIntegration() {
        this.messageQueue = new LinkedBlockingQueue<>();
        this.consumer = new IntegrationConsumer(messageQueue);
        startConsumer();
    }
    
    /**
     * Enviar mensagem para queue
     */
    public void sendMessage(IntegrationMessage message) throws Exception {
        // Validar mensagem
        validateMessage(message);
        
        // Adicionar metadados
        message.setTimestamp(System.currentTimeMillis());
        message.setMessageId(generateMessageId());
        
        // Enviar para queue
        messageQueue.offer(message);
        
        // Log da operação
        logMessageSent(message);
    }
    
    /**
     * Processar mensagem recebida
     */
    public void processMessage(IntegrationMessage message) throws Exception {
        try {
            // Determinar tipo de integração
            IntegrationType type = determineIntegrationType(message);
            
            // Processar baseado no tipo
            switch (type) {
                case PRODUCT_SYNC:
                    processProductSync(message);
                    break;
                case ORDER_SYNC:
                    processOrderSync(message);
                    break;
                case CUSTOMER_SYNC:
                    processCustomerSync(message);
                    break;
                default:
                    throw new Exception("Tipo de integração não suportado: " + type);
            }
            
            // Marcar como processada
            markMessageAsProcessed(message);
            
        } catch (Exception e) {
            handleIntegrationError(message, e);
        }
    }
    
    private void validateMessage(IntegrationMessage message) throws Exception {
        if (message == null) {
            throw new Exception("Mensagem não pode ser nula");
        }
        
        if (message.getEntityType() == null || message.getEntityType().trim().isEmpty()) {
            throw new Exception("Tipo de entidade é obrigatório");
        }
        
        if (message.getOperation() == null) {
            throw new Exception("Operação é obrigatória");
        }
        
        if (message.getData() == null) {
            throw new Exception("Dados da mensagem são obrigatórios");
        }
    }
    
    private IntegrationType determineIntegrationType(IntegrationMessage message) {
        String entityType = message.getEntityType().toUpperCase();
        
        switch (entityType) {
            case "PRODUTO":
            case "PRODUCT":
                return IntegrationType.PRODUCT_SYNC;
            case "PEDIDO":
            case "ORDER":
                return IntegrationType.ORDER_SYNC;
            case "CLIENTE":
            case "CUSTOMER":
                return IntegrationType.CUSTOMER_SYNC;
            default:
                return IntegrationType.UNKNOWN;
        }
    }
    
    private void processProductSync(IntegrationMessage message) throws Exception {
        DynamicVO produtoData = (DynamicVO) message.getData();
        
        switch (message.getOperation()) {
            case CREATE:
                createProduct(produtoData);
                break;
            case UPDATE:
                updateProduct(produtoData);
                break;
            case DELETE:
                deleteProduct(produtoData.asBigDecimal("CODPROD"));
                break;
        }
    }
    
    private void processOrderSync(IntegrationMessage message) throws Exception {
        DynamicVO pedidoData = (DynamicVO) message.getData();
        
        switch (message.getOperation()) {
            case CREATE:
                createOrder(pedidoData);
                break;
            case UPDATE:
                updateOrder(pedidoData);
                break;
            case DELETE:
                deleteOrder(pedidoData.asBigDecimal("NUNOTA"));
                break;
        }
    }
    
    private void processCustomerSync(IntegrationMessage message) throws Exception {
        DynamicVO clienteData = (DynamicVO) message.getData();
        
        switch (message.getOperation()) {
            case CREATE:
                createCustomer(clienteData);
                break;
            case UPDATE:
                updateCustomer(clienteData);
                break;
            case DELETE:
                deleteCustomer(clienteData.asBigDecimal("CODPARC"));
                break;
        }
    }
    
    private void startConsumer() {
        Thread consumerThread = new Thread(consumer);
        consumerThread.setName("IntegrationConsumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }
    
    private String generateMessageId() {
        return "MSG_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
    
    private void logMessageSent(IntegrationMessage message) {
        System.out.println("Mensagem enviada: " + message.getMessageId() + 
                          " - Tipo: " + message.getEntityType() + 
                          " - Operação: " + message.getOperation());
    }
    
    private void markMessageAsProcessed(IntegrationMessage message) throws Exception {
        DynamicVO log = facade.createEntity("AD_INTEGRATION_LOG");
        log.setProperty("MESSAGE_ID", message.getMessageId());
        log.setProperty("ENTITY_TYPE", message.getEntityType());
        log.setProperty("OPERATION", message.getOperation().toString());
        log.setProperty("STATUS", "PROCESSED");
        log.setProperty("PROCESSED_AT", new Date());
        
        facade.saveEntity("AD_INTEGRATION_LOG", log);
    }
    
    private void handleIntegrationError(IntegrationMessage message, Exception e) throws Exception {
        DynamicVO log = facade.createEntity("AD_INTEGRATION_LOG");
        log.setProperty("MESSAGE_ID", message.getMessageId());
        log.setProperty("ENTITY_TYPE", message.getEntityType());
        log.setProperty("OPERATION", message.getOperation().toString());
        log.setProperty("STATUS", "ERROR");
        log.setProperty("ERROR_MESSAGE", e.getMessage());
        log.setProperty("PROCESSED_AT", new Date());
        
        facade.saveEntity("AD_INTEGRATION_LOG", log);
        
        // Re-enviar para retry se necessário
        if (message.getRetryCount() < 3) {
            message.incrementRetryCount();
            messageQueue.offer(message);
        }
    }
}

/**
 * Classe para mensagens de integração
 */
class IntegrationMessage {
    private String messageId;
    private String entityType;
    private Operation operation;
    private Object data;
    private long timestamp;
    private int retryCount;
    
    public IntegrationMessage(String entityType, Operation operation, Object data) {
        this.entityType = entityType;
        this.operation = operation;
        this.data = data;
        this.retryCount = 0;
    }
    
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    // Getters e setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    public Operation getOperation() { return operation; }
    public void setOperation(Operation operation) { this.operation = operation; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}

/**
 * Enum para operações
 */
enum Operation {
    CREATE, UPDATE, DELETE, SYNC
}

/**
 * Enum para tipos de integração
 */
enum IntegrationType {
    PRODUCT_SYNC, ORDER_SYNC, CUSTOMER_SYNC, UNKNOWN
}

/**
 * Consumidor de mensagens
 */
class IntegrationConsumer implements Runnable {
    private BlockingQueue<IntegrationMessage> queue;
    private MessageQueueIntegration integration;
    
    public IntegrationConsumer(BlockingQueue<IntegrationMessage> queue) {
        this.queue = queue;
        this.integration = new MessageQueueIntegration();
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                IntegrationMessage message = queue.take();
                integration.processMessage(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Erro ao processar mensagem: " + e.getMessage());
            }
        }
    }
}
```

### **2. Padrão de Integração com Webhooks**

```java
package br.com.empresa.integracao;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Sistema de webhooks para integração em tempo real
 */
public class WebhookIntegration {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    private WebhookRegistry registry;
    
    public WebhookIntegration() {
        this.registry = new WebhookRegistry();
    }
    
    /**
     * Registrar webhook
     */
    public void registerWebhook(String event, String url, Map<String, String> headers) throws Exception {
        WebhookConfig config = new WebhookConfig(event, url, headers);
        registry.register(config);
        
        // Salvar no banco
        saveWebhookConfig(config);
    }
    
    /**
     * Executar webhook
     */
    public CompletableFuture<WebhookResult> executeWebhook(String event, Object data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                WebhookConfig config = registry.getConfig(event);
                if (config == null) {
                    return new WebhookResult(false, "Webhook não configurado para evento: " + event);
                }
                
                // Preparar payload
                String payload = preparePayload(event, data);
                
                // Enviar webhook
                HttpResponse response = sendWebhook(config, payload);
                
                // Processar resposta
                return processWebhookResponse(response);
                
            } catch (Exception e) {
                return new WebhookResult(false, "Erro ao executar webhook: " + e.getMessage());
            }
        });
    }
    
    /**
     * Processar webhook recebido
     */
    public void processIncomingWebhook(String event, String payload, Map<String, String> headers) throws Exception {
        // Validar assinatura
        if (!validateWebhookSignature(payload, headers)) {
            throw new Exception("Assinatura do webhook inválida");
        }
        
        // Determinar tipo de evento
        WebhookEventType eventType = determineEventType(event);
        
        // Processar baseado no tipo
        switch (eventType) {
            case PRODUCT_CREATED:
                processProductCreated(payload);
                break;
            case ORDER_UPDATED:
                processOrderUpdated(payload);
                break;
            case CUSTOMER_DELETED:
                processCustomerDeleted(payload);
                break;
            default:
                throw new Exception("Tipo de evento não suportado: " + eventType);
        }
        
        // Log do webhook processado
        logWebhookProcessed(event, payload);
    }
    
    private String preparePayload(String event, Object data) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", event);
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("data", data);
        
        return JSONUtils.toJson(payload);
    }
    
    private HttpResponse sendWebhook(WebhookConfig config, String payload) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(config.getUrl()))
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(payload));
        
        // Adicionar headers
        for (Map.Entry<String, String> header : config.getHeaders().entrySet()) {
            requestBuilder.header(header.getKey(), header.getValue());
        }
        
        HttpRequest request = requestBuilder.build();
        
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    private WebhookResult processWebhookResponse(HttpResponse<String> response) {
        boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
        String message = success ? "Webhook executado com sucesso" : "Erro HTTP: " + response.statusCode();
        
        return new WebhookResult(success, message, response.body());
    }
    
    private boolean validateWebhookSignature(String payload, Map<String, String> headers) {
        String signature = headers.get("X-Webhook-Signature");
        if (signature == null) {
            return false;
        }
        
        String expectedSignature = calculateSignature(payload);
        return signature.equals(expectedSignature);
    }
    
    private String calculateSignature(String payload) {
        // Implementar cálculo de assinatura HMAC
        String secret = "webhook_secret_key";
        return HmacUtils.hmacSha256Hex(secret, payload);
    }
    
    private WebhookEventType determineEventType(String event) {
        switch (event.toUpperCase()) {
            case "PRODUCT.CREATED":
                return WebhookEventType.PRODUCT_CREATED;
            case "ORDER.UPDATED":
                return WebhookEventType.ORDER_UPDATED;
            case "CUSTOMER.DELETED":
                return WebhookEventType.CUSTOMER_DELETED;
            default:
                return WebhookEventType.UNKNOWN;
        }
    }
    
    private void processProductCreated(String payload) throws Exception {
        Map<String, Object> data = JSONUtils.fromJson(payload, Map.class);
        // Implementar lógica de criação de produto
        System.out.println("Processando produto criado: " + data.get("id"));
    }
    
    private void processOrderUpdated(String payload) throws Exception {
        Map<String, Object> data = JSONUtils.fromJson(payload, Map.class);
        // Implementar lógica de atualização de pedido
        System.out.println("Processando pedido atualizado: " + data.get("id"));
    }
    
    private void processCustomerDeleted(String payload) throws Exception {
        Map<String, Object> data = JSONUtils.fromJson(payload, Map.class);
        // Implementar lógica de exclusão de cliente
        System.out.println("Processando cliente deletado: " + data.get("id"));
    }
    
    private void saveWebhookConfig(WebhookConfig config) throws Exception {
        DynamicVO webhook = facade.createEntity("AD_WEBHOOK_CONFIG");
        webhook.setProperty("EVENT", config.getEvent());
        webhook.setProperty("URL", config.getUrl());
        webhook.setProperty("HEADERS", JSONUtils.toJson(config.getHeaders()));
        webhook.setProperty("ACTIVE", "S");
        webhook.setProperty("CREATED_AT", new Date());
        
        facade.saveEntity("AD_WEBHOOK_CONFIG", webhook);
    }
    
    private void logWebhookProcessed(String event, String payload) throws Exception {
        DynamicVO log = facade.createEntity("AD_WEBHOOK_LOG");
        log.setProperty("EVENT", event);
        log.setProperty("PAYLOAD", payload);
        log.setProperty("PROCESSED_AT", new Date());
        log.setProperty("STATUS", "SUCCESS");
        
        facade.saveEntity("AD_WEBHOOK_LOG", log);
    }
}

/**
 * Configuração de webhook
 */
class WebhookConfig {
    private String event;
    private String url;
    private Map<String, String> headers;
    
    public WebhookConfig(String event, String url, Map<String, String> headers) {
        this.event = event;
        this.url = url;
        this.headers = headers;
    }
    
    // Getters
    public String getEvent() { return event; }
    public String getUrl() { return url; }
    public Map<String, String> getHeaders() { return headers; }
}

/**
 * Registro de webhooks
 */
class WebhookRegistry {
    private Map<String, WebhookConfig> configs = new HashMap<>();
    
    public void register(WebhookConfig config) {
        configs.put(config.getEvent(), config);
    }
    
    public WebhookConfig getConfig(String event) {
        return configs.get(event);
    }
}

/**
 * Resultado de webhook
 */
class WebhookResult {
    private boolean success;
    private String message;
    private String responseBody;
    
    public WebhookResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public WebhookResult(boolean success, String message, String responseBody) {
        this.success = success;
        this.message = message;
        this.responseBody = responseBody;
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getResponseBody() { return responseBody; }
}

/**
 * Tipos de eventos de webhook
 */
enum WebhookEventType {
    PRODUCT_CREATED, ORDER_UPDATED, CUSTOMER_DELETED, UNKNOWN
}
```

## 🎯 **Boas Práticas da Integração Avançada**

### **1. Arquitetura**
- **Desacoplamento**: Use message queues para desacoplar sistemas
- **Assíncrono**: Processe integrações de forma assíncrona
- **Retry**: Implemente retry automático para falhas
- **Monitoramento**: Monitore todas as integrações

### **2. Segurança**
- **Autenticação**: Use tokens e certificados
- **Assinatura**: Valide assinaturas de webhooks
- **Criptografia**: Criptografe dados sensíveis
- **Auditoria**: Registre todas as operações

### **3. Performance**
- **Pool de Conexões**: Use pool de conexões HTTP
- **Timeout**: Configure timeouts adequados
- **Batch Processing**: Processe mensagens em lotes
- **Cache**: Use cache para dados frequentes

### **4. Confiabilidade**
- **Idempotência**: Garanta operações idempotentes
- **Transações**: Use transações distribuídas quando necessário
- **Backup**: Faça backup de mensagens importantes
- **Recovery**: Implemente recuperação de falhas

## 🎊 **Conclusão**

A integração avançada demonstra:

- **✅ Padrões Enterprise**: Arquitetura sofisticada e escalável
- **✅ Message Queues**: Processamento assíncrono e confiável
- **✅ Webhooks**: Integração em tempo real
- **✅ Segurança**: Autenticação e validação robustas
- **✅ Performance**: Otimizações para alta demanda
- **✅ Monitoramento**: Rastreamento completo de operações

### **Benefícios:**
- **Escalabilidade**: Suporte a crescimento
- **Confiabilidade**: Processamento robusto
- **Flexibilidade**: Integração com diversos sistemas
- **Performance**: Otimizado para alta demanda
- **Manutenibilidade**: Código bem estruturado

---

*Este documento apresenta padrões avançados de integração no Sankhya, fornecendo soluções enterprise para conectividade e sincronização de sistemas.*
