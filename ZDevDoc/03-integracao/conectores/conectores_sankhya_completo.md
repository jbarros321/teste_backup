# 🔌 Conectores Sankhya - Guia Completo

## 🎯 Visão Geral

Este documento apresenta um guia completo sobre conectores no Sankhya, incluindo diferentes tipos de conectores, implementações, padrões e exemplos práticos para integração com sistemas externos.

## 🔗 **Tipos de Conectores**

### **1. Conectores de Banco de Dados**

#### **Conector Oracle**
```java
@Component
public class OracleConnector {
    
    @Value("${oracle.url}")
    private String url;
    
    @Value("${oracle.username}")
    private String username;
    
    @Value("${oracle.password}")
    private String password;
    
    private HikariDataSource dataSource;
    
    @PostConstruct
    public void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        dataSource = new HikariDataSource(config);
    }
    
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    public void executeQuery(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            processResultSet(rs);
        }
    }
    
    private void processResultSet(ResultSet rs) throws SQLException {
        while (rs.next()) {
            // Processar resultados
            System.out.println("Resultado: " + rs.getString(1));
        }
    }
}
```

#### **Conector SQL Server**
```java
@Component
public class SQLServerConnector {
    
    @Value("${sqlserver.url}")
    private String url;
    
    @Value("${sqlserver.username}")
    private String username;
    
    @Value("${sqlserver.password}")
    private String password;
    
    private HikariDataSource dataSource;
    
    @PostConstruct
    public void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        config.setMaximumPoolSize(15);
        config.setMinimumIdle(3);
        config.setConnectionTimeout(30000);
        
        dataSource = new HikariDataSource(config);
    }
    
    public List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return results;
    }
}
```

### **2. Conectores de APIs REST**

#### **Conector Genérico para APIs REST**
```java
@Component
public class RestAPIConnector {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public RestAPIConnector() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        
        // Configurar timeout
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        restTemplate.setRequestFactory(factory);
    }
    
    public <T> T get(String url, Class<T> responseType, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        
        try {
            ResponseEntity<T> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, responseType
            );
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public <T> T post(String url, Object requestBody, Class<T> responseType, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar request body", e);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, httpHeaders);
        
        try {
            ResponseEntity<T> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, responseType
            );
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean put(String url, Object requestBody, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar request body", e);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, httpHeaders);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity, String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean delete(String url, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.DELETE, entity, String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
```

#### **Conector Específico para Integração com ERP**
```java
@Component
public class ERPConnector {
    
    @Autowired
    private RestAPIConnector restAPIConnector;
    
    @Value("${erp.api.base-url}")
    private String baseUrl;
    
    @Value("${erp.api.token}")
    private String apiToken;
    
    public List<ProdutoERP> buscarProdutos(String filtro) {
        String url = baseUrl + "/api/produtos?filtro=" + URLEncoder.encode(filtro, StandardCharsets.UTF_8);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiToken);
        headers.put("Accept", "application/json");
        
        try {
            return restAPIConnector.get(url, List.class, headers);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public boolean sincronizarProduto(ProdutoSankhya produtoSankhya) {
        String url = baseUrl + "/api/produtos/sincronizar";
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiToken);
        headers.put("Content-Type", "application/json");
        
        ProdutoERP produtoERP = converterParaERP(produtoSankhya);
        
        return restAPIConnector.post(url, produtoERP, Boolean.class, headers);
    }
    
    public boolean atualizarEstoque(String codigoProduto, BigDecimal quantidade) {
        String url = baseUrl + "/api/estoque/atualizar";
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiToken);
        headers.put("Content-Type", "application/json");
        
        AtualizacaoEstoque atualizacao = new AtualizacaoEstoque();
        atualizacao.setCodigoProduto(codigoProduto);
        atualizacao.setQuantidade(quantidade);
        atualizacao.setDataAtualizacao(new Date());
        
        return restAPIConnector.put(url, atualizacao, headers);
    }
    
    private ProdutoERP converterParaERP(ProdutoSankhya produtoSankhya) {
        ProdutoERP produtoERP = new ProdutoERP();
        produtoERP.setCodigo(produtoSankhya.getCODPROD().toString());
        produtoERP.setNome(produtoSankhya.getDESCRPROD());
        produtoERP.setPreco(produtoSankhya.getVLROFERTA());
        produtoERP.setCategoria(produtoSankhya.getCODGRUPOPROD().toString());
        
        return produtoERP;
    }
}
```

### **3. Conectores de Mensageria**

#### **Conector RabbitMQ**
```java
@Component
public class RabbitMQConnector {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange}")
    private String exchange;
    
    @Value("${rabbitmq.routing-key}")
    private String routingKey;
    
    public void enviarMensagem(Object mensagem) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, mensagem);
            System.out.println("Mensagem enviada com sucesso");
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void enviarMensagemParaFila(String fila, Object mensagem) {
        try {
            rabbitTemplate.convertAndSend(fila, mensagem);
            System.out.println("Mensagem enviada para fila " + fila);
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem para fila: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @RabbitListener(queues = "${rabbitmq.queue.sankhya}")
    public void receberMensagem(String mensagem) {
        try {
            System.out.println("Mensagem recebida: " + mensagem);
            processarMensagem(mensagem);
        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processarMensagem(String mensagem) {
        // Implementar lógica de processamento
        System.out.println("Processando mensagem: " + mensagem);
    }
}
```

#### **Conector Apache Kafka**
```java
@Component
public class KafkaConnector {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topic.sankhya}")
    private String topicSankhya;
    
    public void enviarMensagem(String chave, Object mensagem) {
        try {
            kafkaTemplate.send(topicSankhya, chave, mensagem);
            System.out.println("Mensagem enviada para Kafka");
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem para Kafka: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void enviarMensagemParaTopico(String topico, String chave, Object mensagem) {
        try {
            kafkaTemplate.send(topico, chave, mensagem);
            System.out.println("Mensagem enviada para tópico " + topico);
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @KafkaListener(topics = "${kafka.topic.sankhya}", groupId = "sankhya-group")
    public void receberMensagem(String mensagem) {
        try {
            System.out.println("Mensagem recebida do Kafka: " + mensagem);
            processarMensagemKafka(mensagem);
        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem do Kafka: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processarMensagemKafka(String mensagem) {
        // Implementar lógica de processamento
        System.out.println("Processando mensagem do Kafka: " + mensagem);
    }
}
```

### **4. Conectores de Arquivos**

#### **Conector FTP/SFTP**
```java
@Component
public class FTPConnector {
    
    @Value("${ftp.host}")
    private String host;
    
    @Value("${ftp.port}")
    private int port;
    
    @Value("${ftp.username}")
    private String username;
    
    @Value("${ftp.password}")
    private String password;
    
    @Value("${ftp.directory}")
    private String directory;
    
    public boolean uploadArquivo(String nomeArquivo, byte[] conteudo) {
        try (FTPClient ftpClient = new FTPClient()) {
            ftpClient.connect(host, port);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            ftpClient.changeWorkingDirectory(directory);
            
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(conteudo)) {
                boolean success = ftpClient.storeFile(nomeArquivo, inputStream);
                ftpClient.logout();
                return success;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public byte[] downloadArquivo(String nomeArquivo) {
        try (FTPClient ftpClient = new FTPClient()) {
            ftpClient.connect(host, port);
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            ftpClient.changeWorkingDirectory(directory);
            
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                boolean success = ftpClient.retrieveFile(nomeArquivo, outputStream);
                if (success) {
                    return outputStream.toByteArray();
                }
            }
            
            ftpClient.logout();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public List<String> listarArquivos() {
        List<String> arquivos = new ArrayList<>();
        
        try (FTPClient ftpClient = new FTPClient()) {
            ftpClient.connect(host, port);
            ftpClient.login(username, password);
            ftpClient.changeWorkingDirectory(directory);
            
            FTPFile[] files = ftpClient.listFiles();
            for (FTPFile file : files) {
                if (file.isFile()) {
                    arquivos.add(file.getName());
                }
            }
            
            ftpClient.logout();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return arquivos;
    }
    
    public boolean deletarArquivo(String nomeArquivo) {
        try (FTPClient ftpClient = new FTPClient()) {
            ftpClient.connect(host, port);
            ftpClient.login(username, password);
            ftpClient.changeWorkingDirectory(directory);
            
            boolean success = ftpClient.deleteFile(nomeArquivo);
            ftpClient.logout();
            
            return success;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
```

#### **Conector para Arquivos CSV**
```java
@Component
public class CSVConnector {
    
    public List<Map<String, String>> lerCSV(InputStream inputStream) {
        List<Map<String, String>> dados = new ArrayList<>();
        
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputStream))
                .withSkipLines(1) // Pular cabeçalho
                .build()) {
            
            String[] headers = reader.readNext();
            
            String[] linha;
            while ((linha = reader.readNext()) != null) {
                Map<String, String> registro = new HashMap<>();
                for (int i = 0; i < headers.length && i < linha.length; i++) {
                    registro.put(headers[i], linha[i]);
                }
                dados.add(registro);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return dados;
    }
    
    public byte[] gerarCSV(List<Map<String, Object>> dados, String[] headers) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            
            // Escrever cabeçalho
            csvWriter.writeNext(headers);
            
            // Escrever dados
            for (Map<String, Object> registro : dados) {
                String[] linha = new String[headers.length];
                for (int i = 0; i < headers.length; i++) {
                    Object valor = registro.get(headers[i]);
                    linha[i] = valor != null ? valor.toString() : "";
                }
                csvWriter.writeNext(linha);
            }
            
            csvWriter.flush();
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
```

### **5. Conectores de Email**

#### **Conector SMTP**
```java
@Component
public class EmailConnector {
    
    @Value("${mail.smtp.host}")
    private String smtpHost;
    
    @Value("${mail.smtp.port}")
    private int smtpPort;
    
    @Value("${mail.username}")
    private String username;
    
    @Value("${mail.password}")
    private String password;
    
    public void enviarEmail(String destinatario, String assunto, String corpo) {
        enviarEmail(destinatario, assunto, corpo, null);
    }
    
    public void enviarEmail(String destinatario, String assunto, String corpo, List<AnexoEmail> anexos) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(assunto);
            message.setText(corpo);
            
            // Adicionar anexos se houver
            if (anexos != null && !anexos.isEmpty()) {
                Multipart multipart = new MimeMultipart();
                
                // Adicionar corpo do email
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(corpo);
                multipart.addBodyPart(textPart);
                
                // Adicionar anexos
                for (AnexoEmail anexo : anexos) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.setContent(anexo.getConteudo(), anexo.getTipoMime());
                    attachmentPart.setFileName(anexo.getNomeArquivo());
                    multipart.addBodyPart(attachmentPart);
                }
                
                message.setContent(multipart);
            }
            
            Transport.send(message);
            System.out.println("Email enviado com sucesso para " + destinatario);
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void enviarEmailComTemplate(String destinatario, String assunto, String templateName, Map<String, Object> variaveis) {
        try {
            String corpo = processarTemplate(templateName, variaveis);
            enviarEmail(destinatario, assunto, corpo);
        } catch (Exception e) {
            System.err.println("Erro ao enviar email com template: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String processarTemplate(String templateName, Map<String, Object> variaveis) {
        try {
            String templateContent = carregarTemplate(templateName);
            
            for (Map.Entry<String, Object> entry : variaveis.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                templateContent = templateContent.replace(placeholder, value);
            }
            
            return templateContent;
            
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    private String carregarTemplate(String templateName) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/" + templateName + ".html");
            if (inputStream != null) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
```

## 🎯 **Padrões de Conectores**

### **1. Factory Pattern para Conectores**
```java
public interface Connector {
    void conectar();
    void desconectar();
    boolean isConectado();
}

public class ConnectorFactory {
    
    public static Connector criarConnector(TipoConnector tipo) {
        switch (tipo) {
            case ORACLE:
                return new OracleConnector();
            case SQLSERVER:
                return new SQLServerConnector();
            case REST_API:
                return new RestAPIConnector();
            case RABBITMQ:
                return new RabbitMQConnector();
            case KAFKA:
                return new KafkaConnector();
            case FTP:
                return new FTPConnector();
            case EMAIL:
                return new EmailConnector();
            default:
                throw new IllegalArgumentException("Tipo de conector não suportado: " + tipo);
        }
    }
}

public enum TipoConnector {
    ORACLE, SQLSERVER, REST_API, RABBITMQ, KAFKA, FTP, EMAIL
}
```

### **2. Builder Pattern para Configuração**
```java
public class ConnectorConfigBuilder {
    
    private String host;
    private int port;
    private String username;
    private String password;
    private int timeout;
    private int poolSize;
    private boolean ssl;
    
    public ConnectorConfigBuilder host(String host) {
        this.host = host;
        return this;
    }
    
    public ConnectorConfigBuilder port(int port) {
        this.port = port;
        return this;
    }
    
    public ConnectorConfigBuilder credentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }
    
    public ConnectorConfigBuilder timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }
    
    public ConnectorConfigBuilder poolSize(int poolSize) {
        this.poolSize = poolSize;
        return this;
    }
    
    public ConnectorConfigBuilder ssl(boolean ssl) {
        this.ssl = ssl;
        return this;
    }
    
    public ConnectorConfig build() {
        return new ConnectorConfig(host, port, username, password, timeout, poolSize, ssl);
    }
}

// Uso do builder
ConnectorConfig config = new ConnectorConfigBuilder()
    .host("localhost")
    .port(1521)
    .credentials("usuario", "senha")
    .timeout(30000)
    .poolSize(10)
    .ssl(true)
    .build();
```

### **3. Strategy Pattern para Diferentes Implementações**
```java
public interface DataSyncStrategy {
    void sincronizarDados(DataSource origem, DataSource destino);
}

public class IncrementalSyncStrategy implements DataSyncStrategy {
    @Override
    public void sincronizarDados(DataSource origem, DataSource destino) {
        // Implementar sincronização incremental
        System.out.println("Executando sincronização incremental");
    }
}

public class FullSyncStrategy implements DataSyncStrategy {
    @Override
    public void sincronizarDados(DataSource origem, DataSource destino) {
        // Implementar sincronização completa
        System.out.println("Executando sincronização completa");
    }
}

public class SyncContext {
    private DataSyncStrategy strategy;
    
    public void setStrategy(DataSyncStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void executarSincronizacao(DataSource origem, DataSource destino) {
        strategy.sincronizarDados(origem, destino);
    }
}
```

## 🛡️ **Tratamento de Erros e Retry**

### **1. Retry Pattern**
```java
@Component
public class RetryConnector {
    
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void executarComRetry(Runnable operacao) {
        try {
            operacao.run();
        } catch (Exception e) {
            System.err.println("Tentativa falhou, tentando novamente...");
            throw e;
        }
    }
    
    @Recover
    public void recuperar(Exception ex) {
        System.err.println("Todas as tentativas falharam: " + ex.getMessage());
        // Implementar lógica de recuperação
    }
}
```

### **2. Circuit Breaker Pattern**
```java
@Component
public class CircuitBreakerConnector {
    
    private final CircuitBreaker circuitBreaker;
    
    public CircuitBreakerConnector() {
        this.circuitBreaker = CircuitBreaker.ofDefaults("sankhyaConnector");
    }
    
    public <T> T executarComCircuitBreaker(Supplier<T> operacao) {
        return circuitBreaker.executeSupplier(operacao);
    }
    
    public void executarComCircuitBreaker(Runnable operacao) {
        circuitBreaker.executeRunnable(operacao);
    }
}
```

## 📊 **Monitoramento de Conectores**

### **1. Métricas de Performance**
```java
@Component
public class ConnectorMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public ConnectorMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void registrarExecucao(String connectorName, long tempoExecucao, boolean sucesso) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("connector.execution.time")
                .tag("connector", connectorName)
                .tag("success", String.valueOf(sucesso))
                .register(meterRegistry));
        
        Counter.builder("connector.execution.count")
                .tag("connector", connectorName)
                .tag("success", String.valueOf(sucesso))
                .register(meterRegistry)
                .increment();
    }
    
    public void registrarErro(String connectorName, String tipoErro) {
        Counter.builder("connector.error.count")
                .tag("connector", connectorName)
                .tag("error.type", tipoErro)
                .register(meterRegistry)
                .increment();
    }
}
```

### **2. Health Check**
```java
@Component
public class ConnectorHealthIndicator implements HealthIndicator {
    
    @Autowired
    private List<Connector> connectors;
    
    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean allHealthy = true;
        
        for (Connector connector : connectors) {
            boolean isHealthy = connector.isConectado();
            details.put(connector.getClass().getSimpleName(), isHealthy ? "UP" : "DOWN");
            
            if (!isHealthy) {
                allHealthy = false;
            }
        }
        
        return allHealthy ? Health.up().withDetails(details).build() 
                          : Health.down().withDetails(details).build();
    }
}
```

## 🎯 **Boas Práticas**

### **1. Configuração**
- **Configuração Externa**: Use arquivos de configuração externos
- **Connection Pooling**: Configure pools de conexão adequadamente
- **Timeouts**: Defina timeouts apropriados
- **SSL/TLS**: Use conexões seguras quando possível

### **2. Tratamento de Erros**
- **Retry Logic**: Implemente lógica de retry
- **Circuit Breaker**: Use circuit breakers para falhas
- **Logging**: Registre erros adequadamente
- **Fallback**: Implemente estratégias de fallback

### **3. Performance**
- **Async Processing**: Use processamento assíncrono quando possível
- **Batch Operations**: Agrupe operações em lotes
- **Caching**: Implemente cache quando apropriado
- **Monitoring**: Monitore performance continuamente

### **4. Segurança**
- **Credenciais**: Armazene credenciais de forma segura
- **Encryption**: Criptografe dados sensíveis
- **Access Control**: Implemente controle de acesso
- **Audit**: Registre operações para auditoria

## 🎊 **Conclusão**

Os conectores Sankhya devem ser:

- **✅ Confiáveis**: Com tratamento robusto de erros
- **✅ Performáticos**: Otimizados para alta demanda
- **✅ Seguros**: Com proteção adequada de dados
- **✅ Monitoráveis**: Com métricas e alertas
- **✅ Flexíveis**: Adaptáveis a diferentes cenários

### **Tipos de Conectores Recomendados:**
1. **Banco de Dados**: Oracle, SQL Server, PostgreSQL
2. **APIs REST**: Para integração com sistemas externos
3. **Mensageria**: RabbitMQ, Kafka para comunicação assíncrona
4. **Arquivos**: FTP, SFTP, CSV para troca de dados
5. **Email**: SMTP para notificações

---

*Este documento fornece um guia completo para implementação de conectores robustos e confiáveis no Sankhya.*
