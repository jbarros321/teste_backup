# 🪝 Webhooks Sankhya - Guia Completo

## 🎯 Visão Geral

Este documento apresenta um guia completo sobre webhooks no Sankhya, incluindo implementação, configuração, segurança, monitoramento e exemplos práticos para integração em tempo real com sistemas externos.

## 🔗 **Conceitos de Webhooks**

### **1. O que são Webhooks**

Webhooks são uma forma de comunicação em tempo real entre sistemas, onde um sistema (fonte) envia automaticamente dados para outro sistema (destino) quando um evento específico ocorre.

#### **Fluxo Básico de Webhook**
```
Sistema Sankhya → Evento → Webhook → Sistema Externo
     ↓              ↓         ↓           ↓
   Trigger      Notificação  HTTP POST   Processamento
```

### **2. Vantagens dos Webhooks**
- **Tempo Real**: Comunicação instantânea
- **Eficiência**: Não requer polling constante
- **Escalabilidade**: Suporta alto volume de eventos
- **Simplicidade**: Implementação direta via HTTP

## 🛠️ **Implementação de Webhooks**

### **1. Estrutura Básica de Webhook**

#### **Classe Principal de Webhook**
```java
@Component
public class SankhyaWebhookManager {
    
    @Autowired
    private WebhookConfigurationService configService;
    
    @Autowired
    private WebhookDeliveryService deliveryService;
    
    @Autowired
    private WebhookEventLogger eventLogger;
    
    public void processarEvento(WebhookEvent event) {
        try {
            // Obter configurações de webhook para este evento
            List<WebhookConfig> configs = configService.getWebhookConfigs(event.getEventType());
            
            for (WebhookConfig config : configs) {
                if (config.isAtivo() && isEventoAplicavel(event, config)) {
                    // Enviar webhook
                    enviarWebhook(event, config);
                }
            }
            
        } catch (Exception e) {
            eventLogger.logarErro(event, e);
        }
    }
    
    private void enviarWebhook(WebhookEvent event, WebhookConfig config) {
        try {
            // Preparar payload
            String payload = prepararPayload(event, config);
            
            // Enviar webhook
            WebhookResponse response = deliveryService.enviarWebhook(
                config.getUrl(), 
                payload, 
                config.getHeaders(),
                config.getTimeout()
            );
            
            // Log do resultado
            eventLogger.logarEnvio(event, config, response);
            
            // Processar retry se necessário
            if (!response.isSucesso() && config.isRetryAtivo()) {
                agendarRetry(event, config);
            }
            
        } catch (Exception e) {
            eventLogger.logarErro(event, config, e);
            
            if (config.isRetryAtivo()) {
                agendarRetry(event, config);
            }
        }
    }
    
    private String prepararPayload(WebhookEvent event, WebhookConfig config) {
        // Transformar evento em JSON baseado na configuração
        ObjectMapper mapper = new ObjectMapper();
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", event.getEventType());
        payload.put("timestamp", event.getTimestamp());
        payload.put("data", event.getData());
        payload.put("source", "Sankhya");
        
        // Adicionar dados específicos baseados na configuração
        if (config.includeMetadata()) {
            payload.put("metadata", event.getMetadata());
        }
        
        try {
            return mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar payload", e);
        }
    }
}
```

#### **Modelo de Evento Webhook**
```java
@Entity
@Table(name = "AD_WEBHOOK_EVENTS")
public class WebhookEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigDecimal id;
    
    @Column(name = "EVENT_TYPE")
    private String eventType;
    
    @Column(name = "ENTITY_NAME")
    private String entityName;
    
    @Column(name = "ENTITY_ID")
    private BigDecimal entityId;
    
    @Column(name = "ACTION")
    private String action; // INSERT, UPDATE, DELETE
    
    @Column(name = "DATA")
    @Lob
    private String data; // JSON com dados do evento
    
    @Column(name = "METADATA")
    @Lob
    private String metadata; // JSON com metadados adicionais
    
    @Column(name = "TIMESTAMP")
    private Timestamp timestamp;
    
    @Column(name = "STATUS")
    private String status; // PENDING, PROCESSED, FAILED
    
    // Getters e setters
}

public enum WebhookEventType {
    PEDIDO_CRIADO("pedido.criado"),
    PEDIDO_ATUALIZADO("pedido.atualizado"),
    PEDIDO_CANCELADO("pedido.cancelado"),
    CLIENTE_CRIADO("cliente.criado"),
    CLIENTE_ATUALIZADO("cliente.atualizado"),
    PRODUTO_CRIADO("produto.criado"),
    PRODUTO_ATUALIZADO("produto.atualizado"),
    ESTOQUE_ATUALIZADO("estoque.atualizado"),
    FATURA_GERADA("fatura.gerada"),
    PAGAMENTO_RECEBIDO("pagamento.recebido");
    
    private final String value;
    
    WebhookEventType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
```

### **2. Configuração de Webhooks**

#### **Modelo de Configuração**
```java
@Entity
@Table(name = "AD_WEBHOOK_CONFIGS")
public class WebhookConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigDecimal id;
    
    @Column(name = "NOME")
    private String nome;
    
    @Column(name = "URL")
    private String url;
    
    @Column(name = "EVENT_TYPES")
    private String eventTypes; // JSON array com tipos de eventos
    
    @Column(name = "HEADERS")
    @Lob
    private String headers; // JSON com headers HTTP
    
    @Column(name = "SECRET_KEY")
    private String secretKey; // Para validação de assinatura
    
    @Column(name = "TIMEOUT")
    private Integer timeout; // Timeout em segundos
    
    @Column(name = "RETRY_ENABLED")
    private Boolean retryEnabled;
    
    @Column(name = "MAX_RETRIES")
    private Integer maxRetries;
    
    @Column(name = "RETRY_DELAY")
    private Integer retryDelay; // Delay em segundos
    
    @Column(name = "ATIVO")
    private Boolean ativo;
    
    @Column(name = "FILTERS")
    @Lob
    private String filters; // JSON com filtros de eventos
    
    // Getters e setters
}
```

#### **Serviço de Configuração**
```java
@Service
public class WebhookConfigurationService {
    
    @Autowired
    private WebhookConfigRepository configRepository;
    
    public List<WebhookConfig> getWebhookConfigs(String eventType) {
        return configRepository.findByEventTypeAndAtivo(eventType, true);
    }
    
    public WebhookConfig salvarConfiguracao(WebhookConfig config) {
        // Validar URL
        validarUrl(config.getUrl());
        
        // Validar headers
        validarHeaders(config.getHeaders());
        
        // Validar filtros
        validarFiltros(config.getFilters());
        
        return configRepository.save(config);
    }
    
    public void ativarWebhook(BigDecimal configId) {
        WebhookConfig config = configRepository.findById(configId)
            .orElseThrow(() -> new RuntimeException("Configuração não encontrada"));
        
        config.setAtivo(true);
        configRepository.save(config);
    }
    
    public void desativarWebhook(BigDecimal configId) {
        WebhookConfig config = configRepository.findById(configId)
            .orElseThrow(() -> new RuntimeException("Configuração não encontrada"));
        
        config.setAtivo(false);
        configRepository.save(config);
    }
    
    private void validarUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL inválida: " + url);
        }
    }
    
    private void validarHeaders(String headers) {
        if (headers != null && !headers.trim().isEmpty()) {
            try {
                new ObjectMapper().readValue(headers, Map.class);
            } catch (Exception e) {
                throw new IllegalArgumentException("Headers JSON inválido");
            }
        }
    }
    
    private void validarFiltros(String filters) {
        if (filters != null && !filters.trim().isEmpty()) {
            try {
                new ObjectMapper().readValue(filters, Map.class);
            } catch (Exception e) {
                throw new IllegalArgumentException("Filtros JSON inválidos");
            }
        }
    }
}
```

### **3. Serviço de Entrega**

#### **Implementação do Delivery Service**
```java
@Service
public class WebhookDeliveryService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private WebhookRetryService retryService;
    
    public WebhookResponse enviarWebhook(String url, String payload, 
                                       String headersJson, Integer timeout) {
        
        try {
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            if (headersJson != null && !headersJson.trim().isEmpty()) {
                Map<String, String> customHeaders = new ObjectMapper()
                    .readValue(headersJson, new TypeReference<Map<String, String>>() {});
                customHeaders.forEach(headers::set);
            }
            
            // Configurar timeout
            if (timeout != null) {
                HttpComponentsClientHttpRequestFactory factory = 
                    new HttpComponentsClientHttpRequestFactory();
                factory.setConnectTimeout(timeout * 1000);
                factory.setReadTimeout(timeout * 1000);
                
                RestTemplate customRestTemplate = new RestTemplate(factory);
                
                // Enviar requisição
                HttpEntity<String> entity = new HttpEntity<>(payload, headers);
                ResponseEntity<String> response = customRestTemplate.postForEntity(url, entity, String.class);
                
                return new WebhookResponse(true, response.getStatusCodeValue(), 
                                         response.getBody(), null);
            } else {
                // Usar RestTemplate padrão
                HttpEntity<String> entity = new HttpEntity<>(payload, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
                
                return new WebhookResponse(true, response.getStatusCodeValue(), 
                                         response.getBody(), null);
            }
            
        } catch (Exception e) {
            return new WebhookResponse(false, 0, null, e.getMessage());
        }
    }
    
    public WebhookResponse enviarWebhookComAssinatura(String url, String payload, 
                                                     String secretKey, Integer timeout) {
        try {
            // Gerar assinatura HMAC
            String signature = gerarAssinatura(payload, secretKey);
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Sankhya-Signature", signature);
            
            // Enviar requisição
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            return new WebhookResponse(true, response.getStatusCodeValue(), 
                                     response.getBody(), null);
            
        } catch (Exception e) {
            return new WebhookResponse(false, 0, null, e.getMessage());
        }
    }
    
    private String gerarAssinatura(String payload, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] signature = mac.doFinal(payload.getBytes());
            return "sha256=" + Base64.getEncoder().encodeToString(signature);
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar assinatura", e);
        }
    }
}
```

#### **Modelo de Resposta**
```java
public class WebhookResponse {
    private boolean sucesso;
    private int statusCode;
    private String responseBody;
    private String errorMessage;
    private long timestamp;
    
    public WebhookResponse(boolean sucesso, int statusCode, String responseBody, String errorMessage) {
        this.sucesso = sucesso;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.errorMessage = errorMessage;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters e setters
}
```

## 🔄 **Sistema de Retry**

### **1. Implementação de Retry**
```java
@Service
public class WebhookRetryService {
    
    @Autowired
    private WebhookRetryRepository retryRepository;
    
    @Autowired
    private WebhookDeliveryService deliveryService;
    
    @Scheduled(fixedDelay = 60000) // Executar a cada minuto
    public void processarRetries() {
        List<WebhookRetry> retries = retryRepository.findRetriesParaProcessar();
        
        for (WebhookRetry retry : retries) {
            try {
                processarRetry(retry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void processarRetry(WebhookRetry retry) {
        WebhookConfig config = retry.getWebhookConfig();
        
        // Verificar se ainda pode tentar
        if (retry.getTentativas() >= config.getMaxRetries()) {
            marcarComoFalhou(retry);
            return;
        }
        
        // Aguardar delay
        long tempoEspera = calcularTempoEspera(retry);
        if (System.currentTimeMillis() - retry.getUltimaTentativa().getTime() < tempoEspera) {
            return; // Ainda não é hora de tentar novamente
        }
        
        // Tentar novamente
        WebhookResponse response = deliveryService.enviarWebhook(
            config.getUrl(),
            retry.getPayload(),
            config.getHeaders(),
            config.getTimeout()
        );
        
        // Atualizar retry
        retry.setTentativas(retry.getTentativas() + 1);
        retry.setUltimaTentativa(new Timestamp(System.currentTimeMillis()));
        
        if (response.isSucesso()) {
            marcarComoSucesso(retry);
        } else {
            retryRepository.save(retry);
        }
    }
    
    private long calcularTempoEspera(WebhookRetry retry) {
        // Exponential backoff: 1min, 2min, 4min, 8min, 16min...
        return (long) Math.pow(2, retry.getTentativas()) * 60 * 1000;
    }
    
    public void agendarRetry(WebhookEvent event, WebhookConfig config) {
        WebhookRetry retry = new WebhookRetry();
        retry.setWebhookEvent(event);
        retry.setWebhookConfig(config);
        retry.setPayload(event.getData());
        retry.setTentativas(0);
        retry.setUltimaTentativa(new Timestamp(System.currentTimeMillis()));
        retry.setStatus("PENDING");
        
        retryRepository.save(retry);
    }
    
    private void marcarComoSucesso(WebhookRetry retry) {
        retry.setStatus("SUCCESS");
        retry.setProcessadoEm(new Timestamp(System.currentTimeMillis()));
        retryRepository.save(retry);
    }
    
    private void marcarComoFalhou(WebhookRetry retry) {
        retry.setStatus("FAILED");
        retry.setProcessadoEm(new Timestamp(System.currentTimeMillis()));
        retryRepository.save(retry);
    }
}
```

### **2. Modelo de Retry**
```java
@Entity
@Table(name = "AD_WEBHOOK_RETRIES")
public class WebhookRetry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigDecimal id;
    
    @ManyToOne
    @JoinColumn(name = "WEBHOOK_EVENT_ID")
    private WebhookEvent webhookEvent;
    
    @ManyToOne
    @JoinColumn(name = "WEBHOOK_CONFIG_ID")
    private WebhookConfig webhookConfig;
    
    @Column(name = "PAYLOAD")
    @Lob
    private String payload;
    
    @Column(name = "TENTATIVAS")
    private Integer tentativas;
    
    @Column(name = "ULTIMA_TENTATIVA")
    private Timestamp ultimaTentativa;
    
    @Column(name = "STATUS")
    private String status; // PENDING, SUCCESS, FAILED
    
    @Column(name = "PROCESSADO_EM")
    private Timestamp processadoEm;
    
    // Getters e setters
}
```

## 🔐 **Segurança de Webhooks**

### **1. Validação de Assinatura**
```java
@Component
public class WebhookSignatureValidator {
    
    public boolean validarAssinatura(String payload, String signature, String secretKey) {
        try {
            String expectedSignature = gerarAssinatura(payload, secretKey);
            return MessageDigest.isEqual(signature.getBytes(), expectedSignature.getBytes());
        } catch (Exception e) {
            return false;
        }
    }
    
    private String gerarAssinatura(String payload, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] signature = mac.doFinal(payload.getBytes());
            return "sha256=" + Base64.getEncoder().encodeToString(signature);
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar assinatura", e);
        }
    }
}
```

### **2. Rate Limiting**
```java
@Component
public class WebhookRateLimiter {
    
    private final Map<String, RateLimitInfo> rateLimits = new ConcurrentHashMap<>();
    
    public boolean isAllowed(String webhookUrl, int maxRequests, int timeWindowMinutes) {
        String key = webhookUrl;
        RateLimitInfo info = rateLimits.computeIfAbsent(key, k -> new RateLimitInfo());
        
        long now = System.currentTimeMillis();
        long windowStart = now - (timeWindowMinutes * 60 * 1000);
        
        // Limpar requisições antigas
        info.getRequests().removeIf(timestamp -> timestamp < windowStart);
        
        // Verificar limite
        if (info.getRequests().size() >= maxRequests) {
            return false;
        }
        
        // Adicionar nova requisição
        info.getRequests().add(now);
        return true;
    }
    
    private static class RateLimitInfo {
        private final List<Long> requests = new ArrayList<>();
        
        public List<Long> getRequests() {
            return requests;
        }
    }
}
```

## 📊 **Monitoramento e Logging**

### **1. Logger de Eventos**
```java
@Service
public class WebhookEventLogger {
    
    @Autowired
    private WebhookEventLogRepository logRepository;
    
    public void logarEnvio(WebhookEvent event, WebhookConfig config, WebhookResponse response) {
        WebhookEventLog log = new WebhookEventLog();
        log.setWebhookEvent(event);
        log.setWebhookConfig(config);
        log.setSucesso(response.isSucesso());
        log.setStatusCode(response.getStatusCode());
        log.setResponseBody(response.getResponseBody());
        log.setErrorMessage(response.getErrorMessage());
        log.setTimestamp(new Timestamp(System.currentTimeMillis()));
        
        logRepository.save(log);
    }
    
    public void logarErro(WebhookEvent event, WebhookConfig config, Exception e) {
        WebhookEventLog log = new WebhookEventLog();
        log.setWebhookEvent(event);
        log.setWebhookConfig(config);
        log.setSucesso(false);
        log.setStatusCode(0);
        log.setErrorMessage(e.getMessage());
        log.setTimestamp(new Timestamp(System.currentTimeMillis()));
        
        logRepository.save(log);
    }
    
    public void logarErro(WebhookEvent event, Exception e) {
        WebhookEventLog log = new WebhookEventLog();
        log.setWebhookEvent(event);
        log.setSucesso(false);
        log.setStatusCode(0);
        log.setErrorMessage(e.getMessage());
        log.setTimestamp(new Timestamp(System.currentTimeMillis()));
        
        logRepository.save(log);
    }
}
```

### **2. Métricas de Webhook**
```java
@Component
public class WebhookMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public WebhookMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void registrarEnvio(String webhookUrl, boolean sucesso, long tempoExecucao) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("webhook.delivery.time")
                .tag("webhook.url", webhookUrl)
                .tag("success", String.valueOf(sucesso))
                .register(meterRegistry));
        
        Counter.builder("webhook.delivery.count")
                .tag("webhook.url", webhookUrl)
                .tag("success", String.valueOf(sucesso))
                .register(meterRegistry)
                .increment();
    }
    
    public void registrarRetry(String webhookUrl, int tentativas) {
        Counter.builder("webhook.retry.count")
                .tag("webhook.url", webhookUrl)
                .tag("attempt", String.valueOf(tentativas))
                .register(meterRegistry)
                .increment();
    }
}
```

## 🎯 **Exemplos Práticos**

### **1. Webhook para Pedido de Venda**
```java
// Evento programado para disparar webhook quando pedido é criado
public class PedidoWebhookEvent implements EventoProgramavelJava {
    
    @Autowired
    private SankhyaWebhookManager webhookManager;
    
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO pedido = (DynamicVO) event.getVo();
        
        // Criar evento webhook
        WebhookEvent webhookEvent = new WebhookEvent();
        webhookEvent.setEventType("pedido.criado");
        webhookEvent.setEntityName("TGFCAB");
        webhookEvent.setEntityId(pedido.asBigDecimal("NUNOTA"));
        webhookEvent.setAction("INSERT");
        webhookEvent.setTimestamp(new Timestamp(System.currentTimeMillis()));
        webhookEvent.setStatus("PENDING");
        
        // Preparar dados do pedido
        Map<String, Object> data = new HashMap<>();
        data.put("nunota", pedido.asBigDecimal("NUNOTA"));
        data.put("codparc", pedido.asBigDecimal("CODPARC"));
        data.put("dtemissao", pedido.asDate("DTEMISSAO"));
        data.put("vlrnota", pedido.asBigDecimal("VLRNOTA"));
        data.put("tipmov", pedido.asString("TIPMOV"));
        
        // Adicionar itens do pedido
        List<Map<String, Object>> itens = obterItensPedido(pedido.asBigDecimal("NUNOTA"));
        data.put("itens", itens);
        
        // Serializar dados
        String jsonData = new ObjectMapper().writeValueAsString(data);
        webhookEvent.setData(jsonData);
        
        // Processar webhook
        webhookManager.processarEvento(webhookEvent);
    }
    
    private List<Map<String, Object>> obterItensPedido(BigDecimal nunota) {
        // Implementar consulta dos itens do pedido
        List<Map<String, Object>> itens = new ArrayList<>();
        
        String sql = "SELECT CODPROD, DESCRPROD, QTDNEG, VLRTOT FROM TGFITE WHERE NUNOTA = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, nunota);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("codprod", rs.getBigDecimal("CODPROD"));
                item.put("descrprod", rs.getString("DESCRPROD"));
                item.put("qtdneg", rs.getBigDecimal("QTDNEG"));
                item.put("vlrtot", rs.getBigDecimal("VLRTOT"));
                
                itens.add(item);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return itens;
    }
}
```

### **2. Webhook para Atualização de Estoque**
```java
// Evento programado para disparar webhook quando estoque é atualizado
public class EstoqueWebhookEvent implements EventoProgramavelJava {
    
    @Autowired
    private SankhyaWebhookManager webhookManager;
    
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO estoque = (DynamicVO) event.getVo();
        
        // Verificar se houve mudança no saldo
        BigDecimal saldoAnterior = estoque.getProperty("SALDOFISICO_OLD") != null ? 
            (BigDecimal) estoque.getProperty("SALDOFISICO_OLD") : BigDecimal.ZERO;
        BigDecimal saldoAtual = estoque.asBigDecimal("SALDOFISICO");
        
        if (saldoAnterior.compareTo(saldoAtual) != 0) {
            // Criar evento webhook
            WebhookEvent webhookEvent = new WebhookEvent();
            webhookEvent.setEventType("estoque.atualizado");
            webhookEvent.setEntityName("TGFPRO");
            webhookEvent.setEntityId(estoque.asBigDecimal("CODPROD"));
            webhookEvent.setAction("UPDATE");
            webhookEvent.setTimestamp(new Timestamp(System.currentTimeMillis()));
            webhookEvent.setStatus("PENDING");
            
            // Preparar dados do estoque
            Map<String, Object> data = new HashMap<>();
            data.put("codprod", estoque.asBigDecimal("CODPROD"));
            data.put("descrprod", estoque.asString("DESCRPROD"));
            data.put("saldoAnterior", saldoAnterior);
            data.put("saldoAtual", saldoAtual);
            data.put("variacao", saldoAtual.subtract(saldoAnterior));
            data.put("estmin", estoque.asBigDecimal("ESTMIN"));
            data.put("estmax", estoque.asBigDecimal("ESTMAX"));
            
            // Serializar dados
            String jsonData = new ObjectMapper().writeValueAsString(data);
            webhookEvent.setData(jsonData);
            
            // Processar webhook
            webhookManager.processarEvento(webhookEvent);
        }
    }
}
```

### **3. Webhook para Integração com ERP Externo**
```java
// Configuração de webhook para integração com ERP externo
@RestController
@RequestMapping("/api/webhooks")
public class WebhookConfigController {
    
    @Autowired
    private WebhookConfigurationService configService;
    
    @PostMapping("/config")
    public ResponseEntity<WebhookConfig> criarConfiguracao(@RequestBody WebhookConfigDTO dto) {
        try {
            WebhookConfig config = new WebhookConfig();
            config.setNome(dto.getNome());
            config.setUrl(dto.getUrl());
            config.setEventTypes(dto.getEventTypes());
            config.setHeaders(dto.getHeaders());
            config.setSecretKey(dto.getSecretKey());
            config.setTimeout(dto.getTimeout());
            config.setRetryEnabled(dto.getRetryEnabled());
            config.setMaxRetries(dto.getMaxRetries());
            config.setRetryDelay(dto.getRetryDelay());
            config.setAtivo(true);
            config.setFilters(dto.getFilters());
            
            WebhookConfig savedConfig = configService.salvarConfiguracao(config);
            
            return ResponseEntity.ok(savedConfig);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/config/{id}")
    public ResponseEntity<WebhookConfig> obterConfiguracao(@PathVariable BigDecimal id) {
        try {
            WebhookConfig config = configService.getWebhookConfigs("").stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
            
            if (config != null) {
                return ResponseEntity.ok(config);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/config/{id}/toggle")
    public ResponseEntity<Void> toggleConfiguracao(@PathVariable BigDecimal id) {
        try {
            // Implementar lógica de toggle (ativar/desativar)
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
```

## 🎯 **Boas Práticas**

### **1. Configuração**
- **URLs Válidas**: Sempre validar URLs de webhook
- **Timeouts**: Configurar timeouts apropriados
- **Headers**: Usar headers para autenticação
- **Filtros**: Implementar filtros para eventos relevantes

### **2. Segurança**
- **Assinatura HMAC**: Sempre assinar payloads
- **HTTPS**: Usar apenas URLs HTTPS
- **Rate Limiting**: Implementar controle de taxa
- **Validação**: Validar payloads recebidos

### **3. Confiabilidade**
- **Retry Logic**: Implementar retry com backoff exponencial
- **Idempotência**: Garantir idempotência nas operações
- **Dead Letter Queue**: Implementar DLQ para falhas
- **Monitoring**: Monitorar entregas e falhas

### **4. Performance**
- **Async Processing**: Processar webhooks assincronamente
- **Batch Processing**: Agrupar eventos quando possível
- **Connection Pooling**: Usar pool de conexões HTTP
- **Caching**: Cachear configurações frequentemente acessadas

## 🎊 **Conclusão**

Os webhooks no Sankhya devem ser:

- **✅ Confiáveis**: Com retry automático e tratamento de falhas
- **✅ Seguros**: Com assinatura e validação adequadas
- **✅ Performáticos**: Com processamento assíncrono
- **✅ Monitoráveis**: Com logs e métricas detalhadas
- **✅ Flexíveis**: Com configuração dinâmica

### **Casos de Uso Recomendados:**
1. **Notificações em Tempo Real**: Pedidos, pagamentos, entregas
2. **Sincronização de Dados**: Estoque, produtos, clientes
3. **Integração com ERPs**: Sistemas externos
4. **Alertas de Sistema**: Falhas, limites, vencimentos
5. **Automação de Processos**: Workflows automatizados

---

*Este documento fornece um guia completo para implementação de webhooks robustos e confiáveis no Sankhya.*
