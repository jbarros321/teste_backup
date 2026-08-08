# 🏆 Melhores Práticas SankhyaW - Padrões de Excelência

## 🎯 **Práticas Extraídas do SankhyaW 4.8**

Este documento consolida as melhores práticas identificadas na análise do sistema SankhyaW 4.8, fornecendo diretrizes para desenvolvimento de alta qualidade no ecossistema Sankhya.

## 🏗️ **Arquitetura e Design**

### **1. Padrão Singleton Thread-Safe**
```java
// ✅ CORRETO: Singleton thread-safe com double-checked locking
public class SankhyaService {
    private volatile static SankhyaService instance;
    private static final Object lock = new Object();
    
    public static SankhyaService getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new SankhyaService();
                }
            }
        }
        return instance;
    }
    
    private SankhyaService() {
        // Inicialização privada
    }
}

// ❌ INCORRETO: Singleton não thread-safe
public class BadService {
    private static BadService instance;
    
    public static BadService getInstance() {
        if (instance == null) {
            instance = new BadService(); // Race condition!
        }
        return instance;
    }
}
```

### **2. Padrão Factory para Criação de Objetos**
```java
// ✅ CORRETO: Factory pattern para criação de entidades
public class EntityFactory {
    
    public static DynamicVO createCliente(String nome, String cpfCnpj) {
        DynamicVO vo = new DynamicVO();
        vo.setProperty("NOMEPARC", nome);
        vo.setProperty("CGCCPF", cpfCnpj);
        vo.setProperty("ATIVO", "S");
        vo.setProperty("DTALTER", new Date());
        return vo;
    }
    
    public static DynamicVO createProduto(String descricao, BigDecimal preco) {
        DynamicVO vo = new DynamicVO();
        vo.setProperty("DESCRPROD", descricao);
        vo.setProperty("VLRVENDA", preco);
        vo.setProperty("ATIVO", "S");
        vo.setProperty("DTALTER", new Date());
        return vo;
    }
}

// ❌ INCORRETO: Criação direta sem padrão
public class BadEntityCreator {
    public void criarCliente() {
        DynamicVO vo = new DynamicVO();
        // Lógica de criação espalhada
        vo.setProperty("NOMEPARC", "Cliente");
        // ... mais código
    }
}
```

### **3. Padrão Builder para Objetos Complexos**
```java
// ✅ CORRETO: Builder pattern para objetos complexos
public class PedidoBuilder {
    private DynamicVO pedido;
    
    public PedidoBuilder() {
        this.pedido = new DynamicVO();
    }
    
    public PedidoBuilder comCliente(BigDecimal codCliente) {
        pedido.setProperty("CODPARC", codCliente);
        return this;
    }
    
    public PedidoBuilder comData(Date data) {
        pedido.setProperty("DTNEG", data);
        return this;
    }
    
    public PedidoBuilder comObservacao(String observacao) {
        pedido.setProperty("OBSERVACAO", observacao);
        return this;
    }
    
    public DynamicVO build() {
        // Validações finais
        if (pedido.getProperty("CODPARC") == null) {
            throw new IllegalStateException("Cliente é obrigatório");
        }
        
        pedido.setProperty("STATUS", "A");
        pedido.setProperty("TIPMOV", "V");
        
        return pedido;
    }
}

// Uso do builder
DynamicVO pedido = new PedidoBuilder()
    .comCliente(new BigDecimal("123"))
    .comData(new Date())
    .comObservacao("Pedido urgente")
    .build();
```

## 🔧 **Gerenciamento de Recursos**

### **1. Padrão Try-With-Resources**
```java
// ✅ CORRETO: Uso de try-with-resources
public void executarQuery(String sql, Object[] params) throws Exception {
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        // Configurar parâmetros
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        
        // Executar query
        try (ResultSet rs = stmt.executeQuery()) {
            processarResultado(rs);
        }
        
    } catch (SQLException e) {
        throw new Exception("Erro ao executar query", e);
    }
}

// ❌ INCORRETO: Não fechar recursos
public void badQuery(String sql) throws Exception {
    Connection conn = getConnection();
    PreparedStatement stmt = conn.prepareStatement(sql);
    ResultSet rs = stmt.executeQuery();
    
    // Recursos não são fechados automaticamente!
    processarResultado(rs);
}
```

### **2. Connection Pooling**
```java
// ✅ CORRETO: Uso de connection pool
public class ConnectionPoolManager {
    private static final HikariDataSource dataSource;
    
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:XE");
        config.setUsername("sankhya");
        config.setPassword("sankhya");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        dataSource = new HikariDataSource(config);
    }
    
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    public static void closePool() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
```

### **3. Gerenciamento de Transações**
```java
// ✅ CORRETO: Gerenciamento adequado de transações
public class TransactionManager {
    
    public static void executeInTransaction(TransactionCallback callback) throws Exception {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            // Executar callback
            callback.execute(conn);
            
            // Commit se tudo OK
            conn.commit();
            
        } catch (Exception e) {
            // Rollback em caso de erro
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Erro no rollback", rollbackEx);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Erro ao fechar conexão", e);
                }
            }
        }
    }
}

// Uso do transaction manager
TransactionManager.executeInTransaction(conn -> {
    // Operações que precisam ser atômicas
    salvarPedido(conn, pedido);
    salvarItens(conn, itens);
    atualizarEstoque(conn, itens);
});
```

## 🛡️ **Segurança e Validação**

### **1. Validação de Entrada**
```java
// ✅ CORRETO: Validação robusta de entrada
public class InputValidator {
    
    public static void validateCliente(DynamicVO cliente) throws Exception {
        // Validação de campos obrigatórios
        validateRequired(cliente, "NOMEPARC", "Nome é obrigatório");
        validateRequired(cliente, "CGCCPF", "CPF/CNPJ é obrigatório");
        
        // Validação de formato
        String cpfCnpj = cliente.getProperty("CGCCPF");
        if (!isValidCpfCnpj(cpfCnpj)) {
            throw new Exception("CPF/CNPJ inválido");
        }
        
        // Validação de tamanho
        String nome = cliente.getProperty("NOMEPARC");
        if (nome.length() > 100) {
            throw new Exception("Nome muito longo (máximo 100 caracteres)");
        }
        
        // Validação de caracteres especiais
        if (containsInvalidCharacters(nome)) {
            throw new Exception("Nome contém caracteres inválidos");
        }
    }
    
    private static void validateRequired(DynamicVO vo, String field, String message) throws Exception {
        Object value = vo.getProperty(field);
        if (value == null || value.toString().trim().isEmpty()) {
            throw new Exception(message);
        }
    }
    
    private static boolean containsInvalidCharacters(String input) {
        return input.matches(".*[<>\"'&].*");
    }
}
```

### **2. Sanitização de Dados**
```java
// ✅ CORRETO: Sanitização adequada de dados
public class DataSanitizer {
    
    public static String sanitizeString(String input) {
        if (input == null) return null;
        
        // Remover caracteres perigosos
        return input.replaceAll("[<>\"'&]", "")
                   .replaceAll("\\s+", " ") // Múltiplos espaços
                   .trim();
    }
    
    public static String sanitizeSQL(String input) {
        if (input == null) return null;
        
        // Remover caracteres SQL perigosos
        return input.replaceAll("[';\"\\\\]", "")
                   .trim();
    }
    
    public static BigDecimal sanitizeDecimal(String input) {
        if (input == null || input.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            // Remover caracteres não numéricos exceto ponto e vírgula
            String cleaned = input.replaceAll("[^0-9.,]", "");
            cleaned = cleaned.replace(",", ".");
            
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
```

### **3. Controle de Acesso**
```java
// ✅ CORRETO: Controle de acesso robusto
public class AccessControl {
    
    public static void validatePermission(BigDecimal userId, String resource, String action) throws Exception {
        if (!hasPermission(userId, resource, action)) {
            throw new Exception("Usuário não tem permissão para " + action + " em " + resource);
        }
    }
    
    public static boolean hasPermission(BigDecimal userId, String resource, String action) {
        try {
            // Verificar permissão no banco
            String sql = "SELECT COUNT(*) FROM AD_PERMISSOES " +
                        "WHERE CODUSU = ? AND RECURSO = ? AND ACAO = ? AND ATIVO = 'S'";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setBigDecimal(1, userId);
                stmt.setString(2, resource);
                stmt.setString(3, action);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Erro ao verificar permissão", e);
            return false;
        }
    }
}
```

## ⚡ **Performance e Otimização**

### **1. Cache Inteligente**
```java
// ✅ CORRETO: Cache com TTL e invalidação
public class SmartCache {
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long defaultTTL;
    
    public SmartCache(long defaultTTL) {
        this.defaultTTL = defaultTTL;
    }
    
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;
        
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        
        return entry.getValue();
    }
    
    public void put(String key, Object value) {
        put(key, value, defaultTTL);
    }
    
    public void put(String key, Object value, long ttl) {
        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + ttl));
    }
    
    public void invalidate(String key) {
        cache.remove(key);
    }
    
    public void clear() {
        cache.clear();
    }
    
    private static class CacheEntry {
        private final Object value;
        private final long expirationTime;
        
        public CacheEntry(Object value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }
        
        public Object getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}
```

### **2. Lazy Loading**
```java
// ✅ CORRETO: Lazy loading para performance
public class LazyEntityLoader {
    private final Map<String, Object> loadedValues = new ConcurrentHashMap<>();
    private final EntityDAO dao;
    
    public LazyEntityLoader(EntityDAO dao) {
        this.dao = dao;
    }
    
    public Object getFieldValue(String fieldName, Object primaryKey) {
        String cacheKey = fieldName + ":" + primaryKey;
        
        return loadedValues.computeIfAbsent(cacheKey, key -> {
            try {
                DynamicVO vo = dao.findByPrimaryKey(primaryKey);
                return vo.getProperty(fieldName);
            } catch (Exception e) {
                logger.error("Erro ao carregar campo " + fieldName, e);
                return null;
            }
        });
    }
    
    public void invalidateCache(Object primaryKey) {
        loadedValues.entrySet().removeIf(entry -> 
            entry.getKey().endsWith(":" + primaryKey));
    }
}
```

### **3. Batch Processing**
```java
// ✅ CORRETO: Processamento em lote para performance
public class BatchProcessor {
    
    public static void processBatch(List<DynamicVO> items, int batchSize, 
                                   BatchProcessorCallback callback) throws Exception {
        for (int i = 0; i < items.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, items.size());
            List<DynamicVO> batch = items.subList(i, endIndex);
            
            try {
                callback.processBatch(batch);
            } catch (Exception e) {
                logger.error("Erro no lote " + (i / batchSize + 1), e);
                throw e;
            }
        }
    }
    
    public static void saveBatch(List<DynamicVO> items, EntityDAO dao) throws Exception {
        processBatch(items, 100, batch -> {
            for (DynamicVO item : batch) {
                dao.save(item);
            }
        });
    }
}

// Uso do batch processor
List<DynamicVO> produtos = getProdutosParaImportar();
BatchProcessor.saveBatch(produtos, produtoDAO);
```

## 📊 **Monitoramento e Logging**

### **1. Logging Estruturado**
```java
// ✅ CORRETO: Logging estruturado com contexto
public class StructuredLogger {
    private static final Logger logger = LoggerFactory.getLogger(StructuredLogger.class);
    
    public static void logOperation(String operation, String entity, Object id, 
                                   long duration, boolean success) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("operation", operation);
        logData.put("entity", entity);
        logData.put("id", id);
        logData.put("duration", duration);
        logData.put("success", success);
        logData.put("timestamp", System.currentTimeMillis());
        logData.put("thread", Thread.currentThread().getName());
        
        if (success) {
            logger.info("Operation completed: {}", logData);
        } else {
            logger.warn("Operation failed: {}", logData);
        }
    }
    
    public static void logError(String operation, Exception error, Map<String, Object> context) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("operation", operation);
        errorData.put("error", error.getMessage());
        errorData.put("errorType", error.getClass().getSimpleName());
        errorData.put("stackTrace", Arrays.toString(error.getStackTrace()));
        errorData.put("timestamp", System.currentTimeMillis());
        errorData.put("context", context);
        
        logger.error("Operation failed: {}", errorData);
    }
}
```

### **2. Métricas de Performance**
```java
// ✅ CORRETO: Coleta de métricas de performance
public class PerformanceMetrics {
    private static final Map<String, AtomicLong> operationCounts = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> operationTimes = new ConcurrentHashMap<>();
    
    public static void recordOperation(String operation, long duration) {
        operationCounts.computeIfAbsent(operation, k -> new AtomicLong(0)).incrementAndGet();
        operationTimes.computeIfAbsent(operation, k -> new AtomicLong(0)).addAndGet(duration);
    }
    
    public static Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        for (String operation : operationCounts.keySet()) {
            long count = operationCounts.get(operation).get();
            long totalTime = operationTimes.get(operation).get();
            double avgTime = count > 0 ? (double) totalTime / count : 0;
            
            Map<String, Object> operationMetrics = new HashMap<>();
            operationMetrics.put("count", count);
            operationMetrics.put("totalTime", totalTime);
            operationMetrics.put("avgTime", avgTime);
            
            metrics.put(operation, operationMetrics);
        }
        
        return metrics;
    }
    
    public static void resetMetrics() {
        operationCounts.clear();
        operationTimes.clear();
    }
}
```

## 🧪 **Testes e Qualidade**

### **1. Testes Unitários**
```java
// ✅ CORRETO: Testes unitários abrangentes
public class ClienteServiceTest {
    
    @Test
    public void testValidarCliente_Sucesso() throws Exception {
        // Arrange
        DynamicVO cliente = new DynamicVO();
        cliente.setProperty("NOMEPARC", "João Silva");
        cliente.setProperty("CGCCPF", "12345678901");
        
        // Act
        ClienteService.validarCliente(cliente);
        
        // Assert
        // Não deve lançar exceção
    }
    
    @Test
    public void testValidarCliente_NomeObrigatorio() {
        // Arrange
        DynamicVO cliente = new DynamicVO();
        cliente.setProperty("CGCCPF", "12345678901");
        // Nome não definido
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            ClienteService.validarCliente(cliente);
        });
    }
    
    @Test
    public void testValidarCliente_CPFInvalido() {
        // Arrange
        DynamicVO cliente = new DynamicVO();
        cliente.setProperty("NOMEPARC", "João Silva");
        cliente.setProperty("CGCCPF", "123"); // CPF inválido
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            ClienteService.validarCliente(cliente);
        });
    }
}
```

### **2. Testes de Integração**
```java
// ✅ CORRETO: Testes de integração
public class ClienteIntegrationTest {
    
    @Test
    public void testSalvarCliente_Integracao() throws Exception {
        // Arrange
        DynamicVO cliente = new DynamicVO();
        cliente.setProperty("NOMEPARC", "Cliente Teste");
        cliente.setProperty("CGCCPF", "12345678901");
        
        // Act
        EntityDAO dao = getEntityDAO("TGFPAR");
        dao.save(cliente);
        
        // Assert
        DynamicVO clienteSalvo = dao.findByPrimaryKey(cliente.getProperty("CODPARC"));
        assertNotNull(clienteSalvo);
        assertEquals("Cliente Teste", clienteSalvo.getProperty("NOMEPARC"));
    }
}
```

## 🎯 **Resumo das Melhores Práticas**

### **Arquitetura e Design**
- ✅ **Singleton Thread-Safe**: Para classes principais
- ✅ **Factory Pattern**: Para criação de objetos
- ✅ **Builder Pattern**: Para objetos complexos
- ✅ **Dependency Injection**: Para desacoplamento

### **Gerenciamento de Recursos**
- ✅ **Try-With-Resources**: Para fechamento automático
- ✅ **Connection Pooling**: Para reutilização de conexões
- ✅ **Transaction Management**: Para consistência de dados
- ✅ **Resource Cleanup**: Para liberação de memória

### **Segurança e Validação**
- ✅ **Input Validation**: Para validação de entrada
- ✅ **Data Sanitization**: Para limpeza de dados
- ✅ **Access Control**: Para controle de acesso
- ✅ **SQL Injection Prevention**: Para segurança de dados

### **Performance e Otimização**
- ✅ **Smart Caching**: Para otimização de consultas
- ✅ **Lazy Loading**: Para carregamento sob demanda
- ✅ **Batch Processing**: Para processamento em lote
- ✅ **Connection Pooling**: Para reutilização de recursos

### **Monitoramento e Logging**
- ✅ **Structured Logging**: Para logs organizados
- ✅ **Performance Metrics**: Para monitoramento
- ✅ **Error Tracking**: Para rastreamento de erros
- ✅ **Audit Trail**: Para auditoria

### **Testes e Qualidade**
- ✅ **Unit Tests**: Para testes unitários
- ✅ **Integration Tests**: Para testes de integração
- ✅ **Code Coverage**: Para cobertura de código
- ✅ **Quality Gates**: Para qualidade de código

---

*Estas melhores práticas extraídas do SankhyaW 4.8 fornecem diretrizes comprovadas para desenvolvimento de alta qualidade no ecossistema Sankhya, garantindo código robusto, seguro e performático.*
