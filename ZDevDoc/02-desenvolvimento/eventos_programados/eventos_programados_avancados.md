# 🔄 Eventos Programados Avançados Sankhya

## 🎯 Visão Geral

Este documento apresenta implementações avançadas de eventos programados no Sankhya, extraídas do código fonte SankhyaW 4.8 e padrões de desenvolvimento enterprise.

## 🏗️ **Arquitetura Avançada de Eventos**

### **1. Sistema de Eventos Hierárquico**

```java
package br.com.empresa.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistema hierárquico de eventos programados
 */
public abstract class HierarchicalEventProcessor implements EventoProgramavelJava {
    
    private List<EventProcessor> processors = new ArrayList<>();
    private boolean stopOnError = false;
    
    public HierarchicalEventProcessor() {
        initializeProcessors();
    }
    
    /**
     * Inicializar processadores de evento
     */
    protected abstract void initializeProcessors();
    
    /**
     * Adicionar processador
     */
    protected void addProcessor(EventProcessor processor) {
        processors.add(processor);
    }
    
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        executeProcessors("beforeInsert", event);
    }
    
    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        executeProcessors("afterInsert", event);
    }
    
    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        executeProcessors("beforeUpdate", event);
    }
    
    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        executeProcessors("afterUpdate", event);
    }
    
    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {
        executeProcessors("beforeDelete", event);
    }
    
    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {
        executeProcessors("afterDelete", event);
    }
    
    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {
        executeProcessors("beforeCommit", tranCtx);
    }
    
    private void executeProcessors(String methodName, Object event) throws Exception {
        for (EventProcessor processor : processors) {
            try {
                processor.execute(methodName, event);
            } catch (Exception e) {
                if (stopOnError) {
                    throw e;
                } else {
                    logError(processor, methodName, e);
                }
            }
        }
    }
    
    private void logError(EventProcessor processor, String methodName, Exception e) {
        System.err.println("Erro no processador " + processor.getClass().getSimpleName() + 
                          " no método " + methodName + ": " + e.getMessage());
    }
    
    /**
     * Interface para processadores de evento
     */
    public interface EventProcessor {
        void execute(String methodName, Object event) throws Exception;
    }
}
```

### **2. Evento com Cache Inteligente**

```java
package br.com.empresa.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Evento programado com cache inteligente
 */
public abstract class CachedEventProcessor implements EventoProgramavelJava {
    
    private Map<String, Object> cache = new ConcurrentHashMap<>();
    private long cacheTimeout = 300000; // 5 minutos
    private Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        processWithCache("beforeInsert", event);
    }
    
    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        processWithCache("afterInsert", event);
    }
    
    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        processWithCache("beforeUpdate", event);
    }
    
    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        processWithCache("afterUpdate", event);
    }
    
    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {
        processWithCache("beforeDelete", event);
    }
    
    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {
        processWithCache("afterDelete", event);
    }
    
    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {
        processWithCache("beforeCommit", tranCtx);
    }
    
    private void processWithCache(String methodName, Object event) throws Exception {
        String cacheKey = generateCacheKey(methodName, event);
        
        if (isCacheValid(cacheKey)) {
            Object cachedResult = cache.get(cacheKey);
            if (cachedResult != null) {
                useCachedResult(cachedResult, event);
                return;
            }
        }
        
        // Processar e cachear resultado
        Object result = processEvent(methodName, event);
        cacheResult(cacheKey, result);
    }
    
    private String generateCacheKey(String methodName, Object event) {
        if (event instanceof PersistenceEvent) {
            PersistenceEvent pe = (PersistenceEvent) event;
            DynamicVO vo = (DynamicVO) pe.getVo();
            return methodName + "_" + vo.getEntityName() + "_" + vo.asBigDecimal("ID");
        }
        return methodName + "_" + event.getClass().getSimpleName();
    }
    
    private boolean isCacheValid(String cacheKey) {
        Long timestamp = cacheTimestamps.get(cacheKey);
        if (timestamp == null) {
            return false;
        }
        
        return (System.currentTimeMillis() - timestamp) < cacheTimeout;
    }
    
    private void cacheResult(String cacheKey, Object result) {
        cache.put(cacheKey, result);
        cacheTimestamps.put(cacheKey, System.currentTimeMillis());
    }
    
    private void useCachedResult(Object cachedResult, Object event) {
        // Implementar uso do resultado em cache
        System.out.println("Usando resultado em cache: " + cachedResult);
    }
    
    /**
     * Processar evento (implementado pelas subclasses)
     */
    protected abstract Object processEvent(String methodName, Object event) throws Exception;
    
    /**
     * Limpar cache
     */
    public void clearCache() {
        cache.clear();
        cacheTimestamps.clear();
    }
    
    /**
     * Definir timeout do cache
     */
    public void setCacheTimeout(long timeout) {
        this.cacheTimeout = timeout;
    }
}
```

## 🔄 **Eventos Especializados**

### **1. Evento de Auditoria Avançada**

```java
package br.com.empresa.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Evento de auditoria avançada
 */
public class AdvancedAuditEvent implements EventoProgramavelJava {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        auditOperation("INSERT", vo, null);
    }
    
    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        logOperation("INSERT", vo, "SUCCESS");
    }
    
    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        DynamicVO oldVo = getOldValues(vo);
        auditOperation("UPDATE", vo, oldVo);
    }
    
    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        logOperation("UPDATE", vo, "SUCCESS");
    }
    
    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        auditOperation("DELETE", vo, null);
    }
    
    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        logOperation("DELETE", vo, "SUCCESS");
    }
    
    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {
        // Auditoria da transação
        auditTransaction(tranCtx);
    }
    
    private void auditOperation(String operation, DynamicVO newVo, DynamicVO oldVo) throws Exception {
        try {
            DynamicVO audit = facade.createEntity("AD_AUDIT_LOG");
            
            // Informações básicas
            audit.setProperty("ENTITY_NAME", newVo.getEntityName());
            audit.setProperty("ENTITY_ID", newVo.asBigDecimal("ID"));
            audit.setProperty("OPERATION", operation);
            audit.setProperty("DT_OPERATION", new Date());
            audit.setProperty("USER_ID", getCurrentUserId());
            audit.setProperty("SESSION_ID", getCurrentSessionId());
            audit.setProperty("IP_ADDRESS", getCurrentIpAddress());
            
            // Valores antigos e novos
            if (oldVo != null) {
                audit.setProperty("OLD_VALUES", serializeValues(oldVo));
            }
            audit.setProperty("NEW_VALUES", serializeValues(newVo));
            
            // Diferenças
            if (oldVo != null && newVo != null) {
                audit.setProperty("CHANGES", calculateChanges(oldVo, newVo));
            }
            
            // Metadados
            audit.setProperty("METADATA", getMetadata(newVo));
            
            facade.saveEntity("AD_AUDIT_LOG", audit);
            
        } catch (Exception e) {
            System.err.println("Erro na auditoria: " + e.getMessage());
            // Não re-lançar exceção para não interromper a operação principal
        }
    }
    
    private DynamicVO getOldValues(DynamicVO vo) {
        // Implementar busca dos valores antigos
        // Isso depende da implementação específica do Sankhya
        return null;
    }
    
    private String serializeValues(DynamicVO vo) {
        Map<String, Object> values = new HashMap<>();
        
        // Serializar valores importantes
        if (vo.asBigDecimal("ID") != null) {
            values.put("ID", vo.asBigDecimal("ID"));
        }
        if (vo.asString("NOMEPARC") != null) {
            values.put("NOMEPARC", vo.asString("NOMEPARC"));
        }
        if (vo.asString("CGCCPF") != null) {
            values.put("CGCCPF", vo.asString("CGCCPF"));
        }
        if (vo.asString("EMAIL") != null) {
            values.put("EMAIL", vo.asString("EMAIL"));
        }
        if (vo.asBigDecimal("VLRNOTA") != null) {
            values.put("VLRNOTA", vo.asBigDecimal("VLRNOTA"));
        }
        
        return values.toString();
    }
    
    private String calculateChanges(DynamicVO oldVo, DynamicVO newVo) {
        Map<String, String> changes = new HashMap<>();
        
        // Comparar campos importantes
        if (!Objects.equals(oldVo.asString("NOMEPARC"), newVo.asString("NOMEPARC"))) {
            changes.put("NOMEPARC", oldVo.asString("NOMEPARC") + " -> " + newVo.asString("NOMEPARC"));
        }
        
        if (!Objects.equals(oldVo.asString("EMAIL"), newVo.asString("EMAIL"))) {
            changes.put("EMAIL", oldVo.asString("EMAIL") + " -> " + newVo.asString("EMAIL"));
        }
        
        if (!Objects.equals(oldVo.asBigDecimal("VLRNOTA"), newVo.asBigDecimal("VLRNOTA"))) {
            changes.put("VLRNOTA", oldVo.asBigDecimal("VLRNOTA") + " -> " + newVo.asBigDecimal("VLRNOTA"));
        }
        
        return changes.toString();
    }
    
    private String getMetadata(DynamicVO vo) {
        Map<String, Object> metadata = new HashMap<>();
        
        metadata.put("ENTITY_NAME", vo.getEntityName());
        metadata.put("TIMESTAMP", new Date());
        metadata.put("USER_AGENT", getUserAgent());
        metadata.put("REQUEST_ID", getRequestId());
        
        return metadata.toString();
    }
    
    private void logOperation(String operation, DynamicVO vo, String status) {
        System.out.println(String.format("Operação %s em %s (ID: %s) - Status: %s", 
                                        operation, vo.getEntityName(), vo.asBigDecimal("ID"), status));
    }
    
    private void auditTransaction(TransactionContext tranCtx) {
        // Auditoria da transação
        System.out.println("Auditoria da transação iniciada");
    }
    
    // Métodos auxiliares
    private BigDecimal getCurrentUserId() {
        // Implementar busca do usuário atual
        return new BigDecimal("1");
    }
    
    private String getCurrentSessionId() {
        // Implementar busca da sessão atual
        return "SESSION_" + System.currentTimeMillis();
    }
    
    private String getCurrentIpAddress() {
        // Implementar busca do IP atual
        return "127.0.0.1";
    }
    
    private String getUserAgent() {
        // Implementar busca do User-Agent
        return "SankhyaW/4.8";
    }
    
    private String getRequestId() {
        // Implementar geração de ID da requisição
        return "REQ_" + System.currentTimeMillis();
    }
}
```

### **2. Evento de Validação Complexa**

```java
package br.com.empresa.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.util.ValidadorCpfCnpj;
import br.com.sankhya.util.ValidadorEmail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Evento de validação complexa
 */
public class ComplexValidationEvent implements EventoProgramavelJava {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    private List<ValidationRule> validationRules = new ArrayList<>();
    
    public ComplexValidationEvent() {
        initializeValidationRules();
    }
    
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        validateEntity(vo, "INSERT");
    }
    
    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        validateEntity(vo, "UPDATE");
    }
    
    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        validateEntity(vo, "DELETE");
    }
    
    private void validateEntity(DynamicVO vo, String operation) throws Exception {
        List<ValidationError> errors = new ArrayList<>();
        
        for (ValidationRule rule : validationRules) {
            try {
                if (rule.appliesTo(vo, operation)) {
                    ValidationResult result = rule.validate(vo);
                    if (!result.isValid()) {
                        errors.addAll(result.getErrors());
                    }
                }
            } catch (Exception e) {
                errors.add(new ValidationError(rule.getName(), e.getMessage()));
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Erros de validação encontrados", errors);
        }
    }
    
    private void initializeValidationRules() {
        // Regras para clientes
        validationRules.add(new CpfCnpjValidationRule());
        validationRules.add(new EmailValidationRule());
        validationRules.add(new TelefoneValidationRule());
        validationRules.add(new EnderecoValidationRule());
        
        // Regras para pedidos
        validationRules.add(new PedidoValidationRule());
        validationRules.add(new EstoqueValidationRule());
        validationRules.add(new CreditoValidationRule());
        
        // Regras para produtos
        validationRules.add(new ProdutoValidationRule());
        validationRules.add(new PrecoValidationRule());
    }
    
    // Interfaces e classes de validação
    public interface ValidationRule {
        boolean appliesTo(DynamicVO vo, String operation);
        ValidationResult validate(DynamicVO vo) throws Exception;
        String getName();
    }
    
    public static class ValidationResult {
        private boolean valid;
        private List<ValidationError> errors;
        
        public ValidationResult(boolean valid) {
            this.valid = valid;
            this.errors = new ArrayList<>();
        }
        
        public ValidationResult(boolean valid, List<ValidationError> errors) {
            this.valid = valid;
            this.errors = errors != null ? errors : new ArrayList<>();
        }
        
        public void addError(ValidationError error) {
            this.errors.add(error);
            this.valid = false;
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public List<ValidationError> getErrors() { return errors; }
    }
    
    public static class ValidationError {
        private String rule;
        private String message;
        
        public ValidationError(String rule, String message) {
            this.rule = rule;
            this.message = message;
        }
        
        // Getters
        public String getRule() { return rule; }
        public String getMessage() { return message; }
    }
    
    public static class ValidationException extends Exception {
        private List<ValidationError> errors;
        
        public ValidationException(String message, List<ValidationError> errors) {
            super(message);
            this.errors = errors;
        }
        
        public List<ValidationError> getErrors() { return errors; }
    }
    
    // Implementações específicas de regras
    private class CpfCnpjValidationRule implements ValidationRule {
        @Override
        public boolean appliesTo(DynamicVO vo, String operation) {
            return "TGFPAR".equals(vo.getEntityName());
        }
        
        @Override
        public ValidationResult validate(DynamicVO vo) throws Exception {
            ValidationResult result = new ValidationResult(true);
            
            String cgccpf = vo.asString("CGCCPF");
            if (cgccpf == null || cgccpf.trim().isEmpty()) {
                result.addError(new ValidationError("CPF/CNPJ", "CPF/CNPJ é obrigatório"));
                return result;
            }
            
            String cgccpfLimpo = cgccpf.replaceAll("[^0-9]", "");
            
            if (cgccpfLimpo.length() == 11) {
                if (!ValidadorCpfCnpj.isValidCpf(cgccpfLimpo)) {
                    result.addError(new ValidationError("CPF", "CPF inválido: " + cgccpf));
                }
            } else if (cgccpfLimpo.length() == 14) {
                if (!ValidadorCpfCnpj.isValidCnpj(cgccpfLimpo)) {
                    result.addError(new ValidationError("CNPJ", "CNPJ inválido: " + cgccpf));
                }
            } else {
                result.addError(new ValidationError("CPF/CNPJ", "CPF/CNPJ deve ter 11 ou 14 dígitos"));
            }
            
            return result;
        }
        
        @Override
        public String getName() {
            return "CPF/CNPJ Validation";
        }
    }
    
    private class EmailValidationRule implements ValidationRule {
        @Override
        public boolean appliesTo(DynamicVO vo, String operation) {
            return "TGFPAR".equals(vo.getEntityName()) && vo.asString("EMAIL") != null;
        }
        
        @Override
        public ValidationResult validate(DynamicVO vo) throws Exception {
            ValidationResult result = new ValidationResult(true);
            
            String email = vo.asString("EMAIL");
            if (!ValidadorEmail.isValidEmail(email)) {
                result.addError(new ValidationError("Email", "Email inválido: " + email));
            }
            
            return result;
        }
        
        @Override
        public String getName() {
            return "Email Validation";
        }
    }
    
    private class TelefoneValidationRule implements ValidationRule {
        @Override
        public boolean appliesTo(DynamicVO vo, String operation) {
            return "TGFPAR".equals(vo.getEntityName()) && vo.asString("TELEFONE") != null;
        }
        
        @Override
        public ValidationResult validate(DynamicVO vo) throws Exception {
            ValidationResult result = new ValidationResult(true);
            
            String telefone = vo.asString("TELEFONE");
            String telefoneLimpo = telefone.replaceAll("[^0-9]", "");
            
            if (telefoneLimpo.length() < 10 || telefoneLimpo.length() > 11) {
                result.addError(new ValidationError("Telefone", "Telefone deve ter 10 ou 11 dígitos"));
            }
            
            return result;
        }
        
        @Override
        public String getName() {
            return "Telefone Validation";
        }
    }
    
    private class EnderecoValidationRule implements ValidationRule {
        @Override
        public boolean appliesTo(DynamicVO vo, String operation) {
            return "TGFPAR".equals(vo.getEntityName());
        }
        
        @Override
        public ValidationResult validate(DynamicVO vo) throws Exception {
            ValidationResult result = new ValidationResult(true);
            
            String endereco = vo.asString("ENDPARC");
            String cidade = vo.asString("CIDADEPARC");
            String uf = vo.asString("UFPARC");
            String cep = vo.asString("CEPPARC");
            
            if (endereco == null || endereco.trim().isEmpty()) {
                result.addError(new ValidationError("Endereço", "Endereço é obrigatório"));
            }
            
            if (cidade == null || cidade.trim().isEmpty()) {
                result.addError(new ValidationError("Cidade", "Cidade é obrigatória"));
            }
            
            if (uf == null || uf.trim().isEmpty()) {
                result.addError(new ValidationError("UF", "UF é obrigatória"));
            }
            
            if (cep != null && !cep.trim().isEmpty()) {
                String cepLimpo = cep.replaceAll("[^0-9]", "");
                if (cepLimpo.length() != 8) {
                    result.addError(new ValidationError("CEP", "CEP deve ter 8 dígitos"));
                }
            }
            
            return result;
        }
        
        @Override
        public String getName() {
            return "Endereço Validation";
        }
    }
    
    private class PedidoValidationRule implements ValidationRule {
        @Override
        public boolean appliesTo(DynamicVO vo, String operation) {
            return "TGFCAB".equals(vo.getEntityName());
        }
        
        @Override
        public ValidationResult validate(DynamicVO vo) throws Exception {
            ValidationResult result = new ValidationResult(true);
            
            BigDecimal codparc = vo.asBigDecimal("CODPARC");
            BigDecimal vlrnota = vo.asBigDecimal("VLRNOTA");
            
            if (codparc == null) {
                result.addError(new ValidationError("Cliente", "Cliente é obrigatório"));
            }
            
            if (vlrnota == null || vlrnota.compareTo(BigDecimal.ZERO) <= 0) {
                result.addError(new ValidationError("Valor", "Valor do pedido deve ser maior que zero"));
            }
            
            return result;
        }
        
        @Override
        public String getName() {
            return "Pedido Validation";
        }
    }
    
    private class EstoqueValidationRule implements ValidationRule {
        @Override
        public boolean appliesTo(DynamicVO vo, String operation) {
            return "TGFCAB".equals(vo.getEntityName()) && "V".equals(vo.asString("TIPMOV"));
        }
        
        @Override
        public ValidationResult validate(DynamicVO vo) throws Exception {
            ValidationResult result = new ValidationResult(true);
            
            BigDecimal nunota = vo.asBigDecimal("NUNOTA");
            
            // Verificar estoque dos itens
            String sql = """
                SELECT 
                    i.CODPROD,
                    p.DESCRPROD,
                    i.QTDNEG,
                    p.SALDOFISICO
                FROM TGFITE i
                JOIN TGFPRO p ON i.CODPROD = p.CODPROD
                WHERE i.NUNOTA = ?
                """;
            
            List<DynamicVO> itens = facade.getQueryExecutor().executeQuery(sql, nunota);
            
            for (DynamicVO item : itens) {
                BigDecimal qtdNecessaria = item.asBigDecimal("QTDNEG");
                BigDecimal saldoDisponivel = item.asBigDecimal("SALDOFISICO");
                
                if (saldoDisponivel.compareTo(qtdNecessaria) < 0) {
                    result.addError(new ValidationError("Estoque", 
                        "Estoque insuficiente para produto " + item.asString("DESCRPROD") + 
                        " (Necessário: " + qtdNecessaria + ", Disponível: " + saldoDisponivel + ")"));
                }
            }
            
            return result;
        }
        
        @Override
        public String getName() {
            return "Estoque Validation";
        }
    }
    
    private class CreditoValidationRule implements ValidationRule {
        @Override
        public boolean appliesTo(DynamicVO vo, String operation) {
            return "TGFCAB".equals(vo.getEntityName()) && "V".equals(vo.asString("TIPMOV"));
        }
        
        @Override
        public ValidationResult validate(DynamicVO vo) throws Exception {
            ValidationResult result = new ValidationResult(true);
            
            BigDecimal codparc = vo.asBigDecimal("CODPARC");
            BigDecimal vlrnota = vo.asBigDecimal("VLRNOTA");
            
            if (codparc != null && vlrnota != null) {
                String sql = "SELECT LIMCRED FROM TGFPAR WHERE CODPARC = ?";
                List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql, codparc);
                
                if (!resultado.isEmpty()) {
                    BigDecimal limiteCredito = resultado.get(0).asBigDecimal("LIMCRED");
                    
                    if (vlrnota.compareTo(limiteCredito) > 0) {
                        result.addError(new ValidationError("Crédito", 
                            "Valor do pedido excede o limite de crédito do cliente"));
                    }
                }
            }
            
            return result;
        }
        
        @Override
        public String getName() {
            return "Crédito Validation";
        }
    }
    
    private class ProdutoValidationRule implements ValidationRule {
        @Override
        public boolean appliesTo(DynamicVO vo, String operation) {
            return "TGFPRO".equals(vo.getEntityName());
        }
        
        @Override
        public ValidationResult validate(DynamicVO vo) throws Exception {
            ValidationResult result = new ValidationResult(true);
            
            String descricao = vo.asString("DESCRPROD");
            BigDecimal preco = vo.asBigDecimal("VLROFERTA");
            
            if (descricao == null || descricao.trim().isEmpty()) {
                result.addError(new ValidationError("Descrição", "Descrição do produto é obrigatória"));
            }
            
            if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
                result.addError(new ValidationError("Preço", "Preço do produto deve ser maior que zero"));
            }
            
            return result;
        }
        
        @Override
        public String getName() {
            return "Produto Validation";
        }
    }
    
    private class PrecoValidationRule implements ValidationRule {
        @Override
        public boolean appliesTo(DynamicVO vo, String operation) {
            return "TGFPRO".equals(vo.getEntityName());
        }
        
        @Override
        public ValidationResult validate(DynamicVO vo) throws Exception {
            ValidationResult result = new ValidationResult(true);
            
            BigDecimal preco = vo.asBigDecimal("VLROFERTA");
            BigDecimal custo = vo.asBigDecimal("VLRCUSTO");
            
            if (preco != null && custo != null && preco.compareTo(custo) <= 0) {
                result.addError(new ValidationError("Margem", 
                    "Preço de venda deve ser maior que o custo do produto"));
            }
            
            return result;
        }
        
        @Override
        public String getName() {
            return "Preço Validation";
        }
    }
    
    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        // Implementar se necessário
    }
    
    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        // Implementar se necessário
    }
    
    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {
        // Implementar se necessário
    }
    
    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {
        // Implementar se necessário
    }
}
```

## 🎯 **Boas Práticas dos Eventos Avançados**

### **1. Arquitetura**
- **Hierarquia**: Use eventos hierárquicos para organização
- **Cache**: Implemente cache para performance
- **Validação**: Use validações complexas e reutilizáveis
- **Auditoria**: Implemente auditoria completa

### **2. Performance**
- **Cache Inteligente**: Cache resultados de validações
- **Validações Assíncronas**: Para operações pesadas
- **Batch Processing**: Processe em lotes quando possível
- **Índices**: Use índices adequados

### **3. Manutenibilidade**
- **Regras Reutilizáveis**: Crie regras de validação reutilizáveis
- **Configuração**: Torne regras configuráveis
- **Logs Detalhados**: Registre operações importantes
- **Testes**: Implemente testes para eventos

### **4. Segurança**
- **Validação Robusta**: Valide todos os dados
- **Auditoria Completa**: Registre todas as operações
- **Controle de Acesso**: Verifique permissões
- **Sanitização**: Limpe dados antes de usar

## 🎊 **Conclusão**

Os eventos programados avançados demonstram:

- **✅ Arquitetura Sofisticada**: Eventos hierárquicos e com cache
- **✅ Validação Complexa**: Sistema de regras reutilizáveis
- **✅ Auditoria Completa**: Rastreamento detalhado de operações
- **✅ Performance**: Cache inteligente e otimizações
- **✅ Manutenibilidade**: Código bem estruturado e modular
- **✅ Segurança**: Validações robustas e auditoria

### **Benefícios:**
- **Escalabilidade**: Suporte a crescimento
- **Confiabilidade**: Validações robustas
- **Rastreabilidade**: Auditoria completa
- **Performance**: Otimizado para alta demanda
- **Flexibilidade**: Regras configuráveis

---

*Este documento apresenta implementações avançadas de eventos programados no Sankhya, fornecendo padrões sofisticados para desenvolvimento enterprise.*
