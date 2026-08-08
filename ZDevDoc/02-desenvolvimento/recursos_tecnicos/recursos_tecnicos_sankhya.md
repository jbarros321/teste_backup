# 🔧 Recursos Técnicos Sankhya - Ferramentas e Utilitários

## 🎯 Visão Geral

Este documento apresenta recursos técnicos avançados do Sankhya, extraídos do código fonte SankhyaW 4.8 e ferramentas de desenvolvimento enterprise.

## 🛠️ **Utilitários Técnicos**

### **1. Gerenciador de Configurações**

```java
package br.com.empresa.recursos;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador de configurações do sistema
 */
public class ConfiguracaoManager {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    private Map<String, Object> cache = new ConcurrentHashMap<>();
    private long cacheTimeout = 300000; // 5 minutos
    private Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    
    /**
     * Obter configuração
     */
    public String obterConfiguracao(String chave) throws Exception {
        return obterConfiguracao(chave, String.class);
    }
    
    /**
     * Obter configuração com tipo específico
     */
    @SuppressWarnings("unchecked")
    public <T> T obterConfiguracao(String chave, Class<T> tipo) throws Exception {
        // Verificar cache
        if (isCacheValid(chave)) {
            Object valor = cache.get(chave);
            if (valor != null) {
                return (T) valor;
            }
        }
        
        // Buscar no banco
        String sql = "SELECT VALOR FROM AD_CONFIG_SISTEMA WHERE CHAVE = ? AND ATIVO = 'S'";
        List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql, chave);
        
        if (!resultado.isEmpty()) {
            String valorStr = resultado.get(0).asString("VALOR");
            T valor = converterValor(valorStr, tipo);
            
            // Cachear resultado
            cache.put(chave, valor);
            cacheTimestamps.put(chave, System.currentTimeMillis());
            
            return valor;
        }
        
        return null;
    }
    
    /**
     * Definir configuração
     */
    public void definirConfiguracao(String chave, Object valor) throws Exception {
        String sql = """
            MERGE INTO AD_CONFIG_SISTEMA 
            USING (SELECT ? as CHAVE, ? as VALOR FROM DUAL) src
            ON (AD_CONFIG_SISTEMA.CHAVE = src.CHAVE)
            WHEN MATCHED THEN 
                UPDATE SET VALOR = src.VALOR, DT_ALTERACAO = SYSDATE
            WHEN NOT MATCHED THEN 
                INSERT (CHAVE, VALOR, DT_CADASTRO) 
                VALUES (src.CHAVE, src.VALOR, SYSDATE)
            """;
        
        facade.getQueryExecutor().executeUpdate(sql, chave, valor.toString());
        
        // Atualizar cache
        cache.put(chave, valor);
        cacheTimestamps.put(chave, System.currentTimeMillis());
    }
    
    /**
     * Obter configurações por categoria
     */
    public Map<String, String> obterConfiguracoesPorCategoria(String categoria) throws Exception {
        String sql = "SELECT CHAVE, VALOR FROM AD_CONFIG_SISTEMA WHERE CHAVE LIKE ? AND ATIVO = 'S'";
        List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql, categoria + "%");
        
        Map<String, String> configuracoes = new HashMap<>();
        for (DynamicVO row : resultado) {
            configuracoes.put(row.asString("CHAVE"), row.asString("VALOR"));
        }
        
        return configuracoes;
    }
    
    private boolean isCacheValid(String chave) {
        Long timestamp = cacheTimestamps.get(chave);
        if (timestamp == null) {
            return false;
        }
        
        return (System.currentTimeMillis() - timestamp) < cacheTimeout;
    }
    
    @SuppressWarnings("unchecked")
    private <T> T converterValor(String valorStr, Class<T> tipo) {
        if (valorStr == null) {
            return null;
        }
        
        if (tipo == String.class) {
            return (T) valorStr;
        } else if (tipo == Integer.class) {
            return (T) Integer.valueOf(valorStr);
        } else if (tipo == Boolean.class) {
            return (T) Boolean.valueOf(valorStr);
        } else if (tipo == Long.class) {
            return (T) Long.valueOf(valorStr);
        } else if (tipo == Double.class) {
            return (T) Double.valueOf(valorStr);
        } else if (tipo == BigDecimal.class) {
            return (T) new BigDecimal(valorStr);
        }
        
        return (T) valorStr;
    }
    
    /**
     * Limpar cache
     */
    public void limparCache() {
        cache.clear();
        cacheTimestamps.clear();
    }
}
```

### **2. Gerenciador de Cache**

```java
package br.com.empresa.recursos;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Sistema de cache avançado
 */
public class CacheManager {
    
    private Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public CacheManager() {
        // Iniciar limpeza automática
        scheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * Armazenar no cache
     */
    public void put(String key, Object value) {
        put(key, value, 300); // 5 minutos por padrão
    }
    
    /**
     * Armazenar no cache com TTL específico
     */
    public void put(String key, Object value, long ttlSeconds) {
        long expirationTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        cache.put(key, new CacheEntry(value, expirationTime));
    }
    
    /**
     * Obter do cache
     */
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        
        return entry.getValue();
    }
    
    /**
     * Verificar se existe no cache
     */
    public boolean contains(String key) {
        return get(key) != null;
    }
    
    /**
     * Remover do cache
     */
    public void remove(String key) {
        cache.remove(key);
    }
    
    /**
     * Limpar todo o cache
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * Obter estatísticas do cache
     */
    public CacheStatistics getStatistics() {
        int totalEntries = cache.size();
        int expiredEntries = 0;
        
        for (CacheEntry entry : cache.values()) {
            if (entry.isExpired()) {
                expiredEntries++;
            }
        }
        
        return new CacheStatistics(totalEntries, expiredEntries, totalEntries - expiredEntries);
    }
    
    private void cleanupExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Entrada do cache
     */
    private static class CacheEntry {
        private Object value;
        private long expirationTime;
        
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
    
    /**
     * Estatísticas do cache
     */
    public static class CacheStatistics {
        private int totalEntries;
        private int expiredEntries;
        private int validEntries;
        
        public CacheStatistics(int totalEntries, int expiredEntries, int validEntries) {
            this.totalEntries = totalEntries;
            this.expiredEntries = expiredEntries;
            this.validEntries = validEntries;
        }
        
        // Getters
        public int getTotalEntries() { return totalEntries; }
        public int getExpiredEntries() { return expiredEntries; }
        public int getValidEntries() { return validEntries; }
    }
}
```

### **3. Gerenciador de Logs**

```java
package br.com.empresa.recursos;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Sistema de logging avançado
 */
public class LogManager {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    private BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();
    private LogWriter writer;
    
    public LogManager() {
        this.writer = new LogWriter(logQueue);
        startWriter();
    }
    
    /**
     * Log de informação
     */
    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }
    
    /**
     * Log de aviso
     */
    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }
    
    /**
     * Log de erro
     */
    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }
    
    /**
     * Log de debug
     */
    public void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }
    
    /**
     * Log estruturado
     */
    public void log(LogLevel level, String message, Throwable throwable) {
        LogEntry entry = new LogEntry(
            level,
            message,
            throwable,
            Thread.currentThread().getName(),
            System.currentTimeMillis()
        );
        
        logQueue.offer(entry);
    }
    
    /**
     * Log de operação de negócio
     */
    public void logBusinessOperation(String operation, String entity, BigDecimal entityId, String details) {
        String message = String.format("Operação: %s, Entidade: %s, ID: %s, Detalhes: %s",
                                     operation, entity, entityId, details);
        log(LogLevel.INFO, message, null);
    }
    
    /**
     * Log de performance
     */
    public void logPerformance(String operation, long durationMs) {
        String message = String.format("Operação: %s, Duração: %dms", operation, durationMs);
        log(LogLevel.INFO, message, null);
    }
    
    private void startWriter() {
        Thread writerThread = new Thread(writer);
        writerThread.setName("LogWriter");
        writerThread.setDaemon(true);
        writerThread.start();
    }
    
    /**
     * Escritor de logs
     */
    private class LogWriter implements Runnable {
        private BlockingQueue<LogEntry> queue;
        
        public LogWriter(BlockingQueue<LogEntry> queue) {
            this.queue = queue;
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    LogEntry entry = queue.take();
                    writeLog(entry);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Erro ao escrever log: " + e.getMessage());
                }
            }
        }
        
        private void writeLog(LogEntry entry) throws Exception {
            // Escrever no console
            System.out.println(entry.toString());
            
            // Escrever no banco
            DynamicVO log = facade.createEntity("AD_SYSTEM_LOG");
            log.setProperty("LOG_LEVEL", entry.getLevel().toString());
            log.setProperty("MESSAGE", entry.getMessage());
            log.setProperty("THREAD_NAME", entry.getThreadName());
            log.setProperty("TIMESTAMP", new Date(entry.getTimestamp()));
            
            if (entry.getThrowable() != null) {
                log.setProperty("STACK_TRACE", getStackTrace(entry.getThrowable()));
            }
            
            facade.saveEntity("AD_SYSTEM_LOG", log);
        }
        
        private String getStackTrace(Throwable throwable) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        }
    }
    
    /**
     * Entrada de log
     */
    private static class LogEntry {
        private LogLevel level;
        private String message;
        private Throwable throwable;
        private String threadName;
        private long timestamp;
        
        public LogEntry(LogLevel level, String message, Throwable throwable, String threadName, long timestamp) {
            this.level = level;
            this.message = message;
            this.throwable = throwable;
            this.threadName = threadName;
            this.timestamp = timestamp;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] [%s] %s - %s", 
                               new Date(timestamp), level, threadName, message);
        }
        
        // Getters
        public LogLevel getLevel() { return level; }
        public String getMessage() { return message; }
        public Throwable getThrowable() { return throwable; }
        public String getThreadName() { return threadName; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Níveis de log
     */
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
}
```

### **4. Gerenciador de Validações**

```java
package br.com.empresa.recursos;

import br.com.sankhya.util.ValidadorCpfCnpj;
import br.com.sankhya.util.ValidadorEmail;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Sistema de validações avançado
 */
public class ValidacaoManager {
    
    /**
     * Validar CPF
     */
    public boolean validarCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }
        
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        
        if (cpfLimpo.length() != 11) {
            return false;
        }
        
        // Verificar se todos os dígitos são iguais
        if (cpfLimpo.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        return ValidadorCpfCnpj.isValidCpf(cpfLimpo);
    }
    
    /**
     * Validar CNPJ
     */
    public boolean validarCnpj(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return false;
        }
        
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
        
        if (cnpjLimpo.length() != 14) {
            return false;
        }
        
        return ValidadorCpfCnpj.isValidCnpj(cnpjLimpo);
    }
    
    /**
     * Validar email
     */
    public boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return ValidadorEmail.isValidEmail(email);
    }
    
    /**
     * Validar telefone
     */
    public boolean validarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return false;
        }
        
        String telefoneLimpo = telefone.replaceAll("[^0-9]", "");
        
        return telefoneLimpo.length() >= 10 && telefoneLimpo.length() <= 11;
    }
    
    /**
     * Validar CEP
     */
    public boolean validarCep(String cep) {
        if (cep == null || cep.trim().isEmpty()) {
            return false;
        }
        
        String cepLimpo = cep.replaceAll("[^0-9]", "");
        
        return cepLimpo.length() == 8;
    }
    
    /**
     * Validar senha forte
     */
    public boolean validarSenhaForte(String senha) {
        if (senha == null || senha.length() < 8) {
            return false;
        }
        
        boolean temMaiuscula = Pattern.compile("[A-Z]").matcher(senha).find();
        boolean temMinuscula = Pattern.compile("[a-z]").matcher(senha).find();
        boolean temNumero = Pattern.compile("[0-9]").matcher(senha).find();
        boolean temEspecial = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(senha).find();
        
        return temMaiuscula && temMinuscula && temNumero && temEspecial;
    }
    
    /**
     * Validar URL
     */
    public boolean validarUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validar data
     */
    public boolean validarData(Date data) {
        return data != null && !data.after(new Date());
    }
    
    /**
     * Validar valor monetário
     */
    public boolean validarValorMonetario(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    /**
     * Validar entidade completa
     */
    public List<ValidationError> validarEntidade(DynamicVO entidade, String tipoEntidade) {
        List<ValidationError> errors = new ArrayList<>();
        
        switch (tipoEntidade.toUpperCase()) {
            case "CLIENTE":
                errors.addAll(validarCliente(entidade));
                break;
            case "PRODUTO":
                errors.addAll(validarProduto(entidade));
                break;
            case "PEDIDO":
                errors.addAll(validarPedido(entidade));
                break;
            default:
                errors.add(new ValidationError("TIPO", "Tipo de entidade não suportado"));
        }
        
        return errors;
    }
    
    private List<ValidationError> validarCliente(DynamicVO cliente) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Validar nome
        String nome = cliente.asString("NOMEPARC");
        if (nome == null || nome.trim().isEmpty()) {
            errors.add(new ValidationError("NOMEPARC", "Nome é obrigatório"));
        }
        
        // Validar CPF/CNPJ
        String cgccpf = cliente.asString("CGCCPF");
        if (cgccpf != null && !cgccpf.trim().isEmpty()) {
            String cgccpfLimpo = cgccpf.replaceAll("[^0-9]", "");
            if (cgccpfLimpo.length() == 11) {
                if (!validarCpf(cgccpf)) {
                    errors.add(new ValidationError("CGCCPF", "CPF inválido"));
                }
            } else if (cgccpfLimpo.length() == 14) {
                if (!validarCnpj(cgccpf)) {
                    errors.add(new ValidationError("CGCCPF", "CNPJ inválido"));
                }
            } else {
                errors.add(new ValidationError("CGCCPF", "CPF/CNPJ deve ter 11 ou 14 dígitos"));
            }
        }
        
        // Validar email
        String email = cliente.asString("EMAIL");
        if (email != null && !email.trim().isEmpty()) {
            if (!validarEmail(email)) {
                errors.add(new ValidationError("EMAIL", "Email inválido"));
            }
        }
        
        // Validar telefone
        String telefone = cliente.asString("TELEFONE");
        if (telefone != null && !telefone.trim().isEmpty()) {
            if (!validarTelefone(telefone)) {
                errors.add(new ValidationError("TELEFONE", "Telefone inválido"));
            }
        }
        
        return errors;
    }
    
    private List<ValidationError> validarProduto(DynamicVO produto) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Validar descrição
        String descricao = produto.asString("DESCRPROD");
        if (descricao == null || descricao.trim().isEmpty()) {
            errors.add(new ValidationError("DESCRPROD", "Descrição é obrigatória"));
        }
        
        // Validar preço
        BigDecimal preco = produto.asBigDecimal("VLROFERTA");
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(new ValidationError("VLROFERTA", "Preço deve ser maior que zero"));
        }
        
        return errors;
    }
    
    private List<ValidationError> validarPedido(DynamicVO pedido) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Validar cliente
        BigDecimal codparc = pedido.asBigDecimal("CODPARC");
        if (codparc == null) {
            errors.add(new ValidationError("CODPARC", "Cliente é obrigatório"));
        }
        
        // Validar valor
        BigDecimal valor = pedido.asBigDecimal("VLRNOTA");
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(new ValidationError("VLRNOTA", "Valor deve ser maior que zero"));
        }
        
        return errors;
    }
    
    /**
     * Classe para erro de validação
     */
    public static class ValidationError {
        private String field;
        private String message;
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        // Getters
        public String getField() { return field; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            return field + ": " + message;
        }
    }
}
```

## 🎯 **Boas Práticas dos Recursos Técnicos**

### **1. Configuração**
- **Cache**: Use cache para configurações frequentes
- **Validação**: Valide configurações ao carregar
- **Fallback**: Tenha valores padrão para configurações
- **Segurança**: Proteja configurações sensíveis

### **2. Cache**
- **TTL**: Use TTL apropriado para cada tipo de dado
- **Limpeza**: Implemente limpeza automática de cache
- **Estatísticas**: Monitore estatísticas de cache
- **Memória**: Controle uso de memória

### **3. Logs**
- **Níveis**: Use níveis de log apropriados
- **Assíncrono**: Processe logs de forma assíncrona
- **Estruturado**: Use logs estruturados
- **Retenção**: Implemente política de retenção

### **4. Validação**
- **Completa**: Valide todos os campos obrigatórios
- **Formato**: Valide formatos de dados
- **Negócio**: Implemente validações de negócio
- **Performance**: Otimize validações frequentes

## 🎊 **Conclusão**

Os recursos técnicos demonstram:

- **✅ Configuração Dinâmica**: Sistema flexível de configurações
- **✅ Cache Inteligente**: Cache com TTL e limpeza automática
- **✅ Logging Avançado**: Sistema de logs estruturado
- **✅ Validação Robusta**: Validações completas e performáticas
- **✅ Utilitários Úteis**: Ferramentas para desenvolvimento
- **✅ Performance**: Otimizado para produção

### **Benefícios:**
- **Flexibilidade**: Configurações dinâmicas
- **Performance**: Cache e otimizações
- **Confiabilidade**: Logs e validações robustas
- **Manutenibilidade**: Código bem estruturado
- **Escalabilidade**: Suporte a crescimento

---

*Este documento apresenta recursos técnicos avançados do Sankhya, fornecendo ferramentas e utilitários essenciais para desenvolvimento enterprise.*
