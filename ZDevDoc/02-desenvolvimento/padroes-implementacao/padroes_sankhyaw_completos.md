# 🏗️ Padrões de Implementação SankhyaW - Análise Completa

## 🎯 **Análise do Sistema SankhyaW 4.8**

Este documento apresenta os padrões de implementação extraídos da análise completa do sistema SankhyaW 4.8, fornecendo insights valiosos sobre como o Sankhya implementa suas funcionalidades internamente.

## 📚 **Arquitetura Geral do SankhyaW**

### **Estrutura de Módulos**
```
SankhyaW 4.8/
├── JAPE/                    # Framework de Persistência
├── MGE-Modelcore/          # Core do Modelo de Negócio
├── sankhya-js/             # Framework JavaScript/AngularJS
├── MGE-*-Model/            # Módulos de Modelo
├── MGE-*-VC/               # Módulos de View Controller
├── MGE-*-VC-Flex/          # Módulos Flex
├── SankhyaUtil/            # Utilitários
├── SankhyaGwt/             # Framework GWT
└── [Outros Módulos]        # Módulos específicos
```

## 🔧 **Padrões JAPE (Java Persistence)**

### **1. Classe Principal Jape**
```java
// Padrão Singleton com inicialização thread-safe
public class Jape implements JapeMBean {
    private volatile static byte initFlag = 0;
    private volatile static Jape singleton;
    private static final Lock locker = new ReentrantLock();
    
    public static void initialize() {
        if (initFlag != 0) return;
        
        try {
            locker.lock();
            if (initFlag != 0 || singleton != null) return;
            
            // Sequência importante de inicialização
            singleton = new Jape();
            singleton.initializeInternal();
            singleton.loadMetadataProvidersByClassPath();
            
            initFlag = 1;
        } finally {
            locker.unlock();
        }
    }
    
    private void initializeInternal() {
        initBSH();                    // BeanShell
        EntityCacheResizer.startTask();
        SessionManager.startTasks();
        JDBCResourcesMonitor.startTasks();
    }
}
```

### **2. Padrão de Eventos Programados**
```java
// AbstractAction - Base para todas as ações
public abstract class AbstractAction implements ActitonExecutor {
    protected Map<Integer, Map<String, Object>> params;
    protected BigDecimal userId;
    protected int selectedRowsSize;
    protected String rootEntity;
    
    public AbstractAction() {
        params = new HashMap<Integer, Map<String,Object>>();
    }
    
    // Adicionar parâmetros por linha
    public void addRowParam(String name, Object value, int rowIndex) {
        Map<String, Object> map = params.get(rowIndex);
        if(map == null) {
            map = new HashMap<String, Object>();
            params.put(rowIndex, map);
        }
        map.put(name, value);
    }
    
    // Parse de configuração XML
    public void parseSource(Element source, EntityFacade dwfFacade) throws Exception {
        Element paramsElement = source.getChild("params");
        if(paramsElement != null) {
            for(Element paramElem : (List<Element>) paramsElement.getChildren()) {
                String type = XMLUtils.getRequiredAttributeAsString(paramElem, "type");
                Object value = null;
                String paramName = XMLUtils.getRequiredAttributeAsString(paramElem, "paramName");
                
                // Conversão de tipos
                if("I".equals(type)) {
                    value = XMLUtils.getContentAsBigDecimal(paramElem).intValue();
                } else if("F".equals(type)) {
                    value = XMLUtils.getContentAsBigDecimal(paramElem).doubleValue();
                } else if("D".equals(type) || "DH".equals(type)) {
                    value = XMLUtils.getContentAsTimeStamp(paramElem);
                } else {
                    value = XMLUtils.getContentAsString(paramElem);
                }
                
                String seq = paramElem.getAttributeValue("sequence");
                if(seq == null) {
                    addParam(paramName, value);
                } else {
                    addRowParam(paramName, value, Integer.parseInt(seq));
                }
            }
        }
    }
}
```

### **3. Padrão de Agentes**
```java
// Agent - Sistema de agentes para processamento
public class Agent {
    private AgentContext context;
    private AgentDescriptor descriptor;
    private AgentRuntime runtime;
    
    // Execução de agente
    public void execute() throws Exception {
        try {
            // Inicializar contexto
            context = new AgentContext();
            context.setDescriptor(descriptor);
            
            // Executar lógica do agente
            executeAgentLogic();
            
        } catch (Exception e) {
            // Tratamento de erro
            handleAgentError(e);
        }
    }
    
    // Validação de permissões
    public boolean hasPermission(String resource) {
        return Validador.validate(context, resource);
    }
}
```

## 🎨 **Padrões SankhyaJS (AngularJS)**

### **1. Estrutura de Módulos**
```javascript
// Padrão de módulos AngularJS
angular
    .module('snk.commons', [])
    .module('snk.components.actionbutton', ['snk.commons'])
    .module('snk.components.datagrid', ['snk.commons'])
    .module('snk.components.form', ['snk.commons']);
```

### **2. Padrão de Diretivas**
```javascript
// Diretiva de Botão de Ação
angular
    .module('snk.components.actionbutton')
    .directive('skActionButton', ['$q', 'StringUtils', function ($q, StringUtils) {
        return {
            scope: {
                entityName: '@skEntityName',
                resourceId: '@skResourceId',
                accessResourceId: '@skAccessResourceId',
                context: '=skContext',
                zIndex: '<',
                isDisabled: '=?skIsDisabled',
                tooltip: '@?skTooltip',
                tooltipPlacement: '@?skTooltipPlacement',
                ignoreControleAcesso: '=?skIgnoreControleAcesso',
                squareMode: "=?skSquareMode"
            },
            templateUrl: 'components/actionbutton/actionbutton.tpl.html',
            restrict: 'E',
            controller: 'SkActionButtonController',
            controllerAs: 'ctrl',
            link: postLink
        };
        
        function postLink(scope, element, attr, ctrl) {
            // Lógica de link da diretiva
        }
    }]);
```

### **3. Padrão de Controllers**
```javascript
// Controller de Botão de Ação
angular
    .module('snk.components.actionbutton')
    .controller('SkActionButtonController', [
        '$scope', '$q', 'SkI18nService', 'ServiceProxy', 'StringUtils', 
        'SkApplication', 'ObjectUtils', 'PopUpParameter', 'MessageUtils', 
        'MGEParameters', 'ArrayUtils', 'DateUtils', '$cacheFactory', 
        'KeyboardManager', 'AngularUtil',
        function ($scope, $q, SkI18nService, ServiceProxy, StringUtils, 
                 SkApplication, ObjectUtils, PopUpParameter, MessageUtils, 
                 MGEParameters, ArrayUtils, DateUtils, $cacheFactory, 
                 KeyboardManager, AngularUtil) {
            
            var CLIENT_EVENT_CONFIRM_NAME = "br.com.sankhya.actionbutton.clientconfirm";
            var ACTION_BUTTON_CACHE_NAME = "actionButtonCache";
            
            var self = this;
            var _actions;
            var _actionsLoaded = false;
            var _indexSelected = -1;
            var _callbacksById;
            var _popOver;
            
            // Configurações
            var _isOrderActions = MGEParameters.asBoolean('global.ordenar.acoes.personalizadas');
            var _isBindKeys = MGEParameters.asBoolean('global.atalho.acoes.personalizadas');
            var _lastValuesCache = {};
            
            // Inicialização
            function init() {
                if (!ServiceProxy.hasClientEvent(CLIENT_EVENT_CONFIRM_NAME)) {
                    ServiceProxy.addClientEvent(CLIENT_EVENT_CONFIRM_NAME, clientConfirm);
                }
                
                _callbacksById = $cacheFactory.get(ACTION_BUTTON_CACHE_NAME) || 
                                $cacheFactory(ACTION_BUTTON_CACHE_NAME);
                
                // Bind de teclas
                KeyboardManager.bind('ctrl+b', function () {
                    if(_popOver) {
                        _popOver.show();
                        AngularUtil.timeout(function () {
                            var itemElem = angular.element(document.querySelector('.btn-action-focus'));
                            if (itemElem && itemElem[0]) {
                                itemElem[0].focus();
                            }
                            if(_indexSelected == -1) {
                                nextAction();
                            }
                        }, 200);
                    }
                });
                
                // Navegação por teclado
                KeyboardManager.bind('down', function () {
                    if(_popOver.popUpIsOpen() && _actionsLoaded) {
                        nextAction();
                    }
                }, { propagate : true});
                
                KeyboardManager.bind('enter', function () {
                    if(_popOver.popUpIsOpen() && _actionsLoaded && 
                       _indexSelected > -1 && _actions != null && 
                       _indexSelected < _actions.length) {
                        executeAction(_actions[_indexSelected]);
                        _popOver.hide();
                    }
                }, { propagate : true});
            }
            
            // Execução de ação
            function executeAction(action) {
                try {
                    // Validações
                    if (!action) return;
                    
                    // Executar ação
                    var result = action.execute();
                    
                    // Callback de sucesso
                    if (result && result.then) {
                        result.then(function(response) {
                            handleActionSuccess(action, response);
                        }).catch(function(error) {
                            handleActionError(action, error);
                        });
                    } else {
                        handleActionSuccess(action, result);
                    }
                    
                } catch (error) {
                    handleActionError(action, error);
                }
            }
            
            // Inicializar
            init();
        }
    ]);
```

## 🗄️ **Padrões de Persistência**

### **1. EntityDAO Pattern**
```java
// Padrão de acesso a dados
public class EntityDAO {
    private EntityFacade facade;
    private SQLProvider sqlProvider;
    private Map<String, EntityPropertyDescriptor> fieldsByName;
    
    // Buscar entidade por ID
    public DynamicVO findByPrimaryKey(Object primaryKey) throws Exception {
        try {
            // Construir query
            String sql = sqlProvider.getSelectByPrimaryKeySQL();
            
            // Executar query
            PreparedStatement stmt = facade.getConnection().prepareStatement(sql);
            stmt.setObject(1, primaryKey);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return buildVOFromResultSet(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            throw new PersistenceException("Erro ao buscar entidade", e);
        }
    }
    
    // Salvar entidade
    public void save(DynamicVO vo) throws Exception {
        if (vo.isNew()) {
            insert(vo);
        } else {
            update(vo);
        }
    }
    
    // Inserir nova entidade
    private void insert(DynamicVO vo) throws Exception {
        String sql = sqlProvider.getInsertSQL();
        PreparedStatement stmt = facade.getConnection().prepareStatement(sql);
        
        // Preencher parâmetros
        int paramIndex = 1;
        for (EntityPropertyDescriptor field : fieldsByName.values()) {
            Object value = vo.getProperty(field.getName());
            stmt.setObject(paramIndex++, value);
        }
        
        stmt.executeUpdate();
    }
}
```

### **2. Padrão de Transações**
```java
// Gerenciamento de transações
public class TransactionManager {
    private static final ThreadLocal<Transaction> currentTransaction = new ThreadLocal<>();
    
    public static void beginTransaction() throws Exception {
        if (currentTransaction.get() != null) {
            throw new Exception("Transação já iniciada");
        }
        
        Transaction tx = new Transaction();
        tx.begin();
        currentTransaction.set(tx);
    }
    
    public static void commitTransaction() throws Exception {
        Transaction tx = currentTransaction.get();
        if (tx == null) {
            throw new Exception("Nenhuma transação ativa");
        }
        
        try {
            tx.commit();
        } finally {
            currentTransaction.remove();
        }
    }
    
    public static void rollbackTransaction() throws Exception {
        Transaction tx = currentTransaction.get();
        if (tx == null) {
            throw new Exception("Nenhuma transação ativa");
        }
        
        try {
            tx.rollback();
        } finally {
            currentTransaction.remove();
        }
    }
}
```

## 🎯 **Padrões de Validação**

### **1. Validação de Entidades**
```java
// Validador de entidades
public class EntityValidator {
    
    public static void validate(DynamicVO vo, String entityName) throws Exception {
        // Validações obrigatórias
        validateRequiredFields(vo, entityName);
        
        // Validações de formato
        validateFieldFormats(vo, entityName);
        
        // Validações de negócio
        validateBusinessRules(vo, entityName);
    }
    
    private static void validateRequiredFields(DynamicVO vo, String entityName) throws Exception {
        EntityMetaData metadata = MetaDataProvider.getMetaData(entityName);
        
        for (EntityField field : metadata.getFields()) {
            if (field.isRequired() && vo.getProperty(field.getName()) == null) {
                throw new Exception("Campo obrigatório: " + field.getName());
            }
        }
    }
    
    private static void validateFieldFormats(DynamicVO vo, String entityName) throws Exception {
        EntityMetaData metadata = MetaDataProvider.getMetaData(entityName);
        
        for (EntityField field : metadata.getFields()) {
            Object value = vo.getProperty(field.getName());
            if (value != null) {
                validateFieldFormat(field, value);
            }
        }
    }
}
```

### **2. Validação de Parâmetros**
```java
// Validação de parâmetros de entrada
public class ParameterValidator {
    
    public static void validateParameters(Map<String, Object> params, 
                                        Map<String, ParameterDescriptor> expectedParams) throws Exception {
        for (Map.Entry<String, ParameterDescriptor> entry : expectedParams.entrySet()) {
            String paramName = entry.getKey();
            ParameterDescriptor descriptor = entry.getValue();
            
            Object value = params.get(paramName);
            
            // Validar obrigatoriedade
            if (descriptor.isRequired() && value == null) {
                throw new Exception("Parâmetro obrigatório: " + paramName);
            }
            
            // Validar tipo
            if (value != null) {
                validateParameterType(paramName, value, descriptor.getType());
            }
        }
    }
    
    private static void validateParameterType(String paramName, Object value, Class<?> expectedType) throws Exception {
        if (!expectedType.isAssignableFrom(value.getClass())) {
            throw new Exception("Tipo inválido para parâmetro " + paramName + 
                              ". Esperado: " + expectedType.getSimpleName() + 
                              ", Recebido: " + value.getClass().getSimpleName());
        }
    }
}
```

## 🔄 **Padrões de Cache**

### **1. Cache de Entidades**
```java
// Cache de entidades
public class EntityCache {
    private final Map<String, DynamicVO> cache = new ConcurrentHashMap<>();
    private final long maxAge;
    private final Map<String, Long> timestamps = new ConcurrentHashMap<>();
    
    public EntityCache(long maxAge) {
        this.maxAge = maxAge;
    }
    
    public DynamicVO get(String key) {
        Long timestamp = timestamps.get(key);
        if (timestamp != null && System.currentTimeMillis() - timestamp > maxAge) {
            // Cache expirado
            cache.remove(key);
            timestamps.remove(key);
            return null;
        }
        
        return cache.get(key);
    }
    
    public void put(String key, DynamicVO value) {
        cache.put(key, value);
        timestamps.put(key, System.currentTimeMillis());
    }
    
    public void clear() {
        cache.clear();
        timestamps.clear();
    }
}
```

### **2. Cache de Metadados**
```java
// Cache de metadados
public class MetadataCache {
    private static final Map<String, EntityMetaData> metadataCache = new ConcurrentHashMap<>();
    
    public static EntityMetaData getMetadata(String entityName) {
        return metadataCache.computeIfAbsent(entityName, name -> {
            try {
                return MetaDataProvider.getMetaData(name);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao carregar metadados para " + name, e);
            }
        });
    }
    
    public static void clearCache() {
        metadataCache.clear();
    }
}
```

## 🚀 **Padrões de Performance**

### **1. Connection Pooling**
```java
// Pool de conexões
public class ConnectionPool {
    private final BlockingQueue<Connection> availableConnections;
    private final Set<Connection> usedConnections;
    private final int maxConnections;
    
    public ConnectionPool(int maxConnections) {
        this.maxConnections = maxConnections;
        this.availableConnections = new LinkedBlockingQueue<>(maxConnections);
        this.usedConnections = Collections.synchronizedSet(new HashSet<>());
        
        // Inicializar conexões
        initializeConnections();
    }
    
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = availableConnections.poll(30, TimeUnit.SECONDS);
            if (conn == null) {
                throw new SQLException("Timeout ao obter conexão");
            }
            
            usedConnections.add(conn);
            return conn;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrompido ao obter conexão", e);
        }
    }
    
    public void releaseConnection(Connection conn) {
        if (usedConnections.remove(conn)) {
            availableConnections.offer(conn);
        }
    }
}
```

### **2. Lazy Loading**
```java
// Carregamento preguiçoso
public class LazyFieldLoader {
    private final Map<String, Object> loadedValues = new ConcurrentHashMap<>();
    private final EntityDAO dao;
    private final String entityName;
    
    public Object getFieldValue(String fieldName, Object primaryKey) {
        String cacheKey = entityName + ":" + primaryKey + ":" + fieldName;
        
        return loadedValues.computeIfAbsent(cacheKey, key -> {
            try {
                DynamicVO vo = dao.findByPrimaryKey(primaryKey);
                return vo.getProperty(fieldName);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao carregar campo " + fieldName, e);
            }
        });
    }
}
```

## 📊 **Padrões de Monitoramento**

### **1. MBean para Monitoramento**
```java
// MBean para monitoramento
public interface JapeMBean {
    String getVersion();
    int getActiveConnections();
    int getCacheSize();
    long getTotalQueries();
    long getTotalExecutionTime();
    
    void clearCache();
    void resetStatistics();
}

// Implementação
public class Jape implements JapeMBean {
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    @Override
    public long getTotalQueries() {
        return totalQueries.get();
    }
    
    @Override
    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }
    
    @Override
    public void resetStatistics() {
        totalQueries.set(0);
        totalExecutionTime.set(0);
    }
}
```

### **2. Logging Estruturado**
```java
// Logging estruturado
public class SankhyaLogger {
    private static final Logger logger = LoggerFactory.getLogger(SankhyaLogger.class);
    
    public static void logAction(String action, String entity, Object id, long duration) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("action", action);
        logData.put("entity", entity);
        logData.put("id", id);
        logData.put("duration", duration);
        logData.put("timestamp", System.currentTimeMillis());
        
        logger.info("Action executed: {}", logData);
    }
    
    public static void logError(String operation, Exception error) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("operation", operation);
        errorData.put("error", error.getMessage());
        errorData.put("stackTrace", Arrays.toString(error.getStackTrace()));
        errorData.put("timestamp", System.currentTimeMillis());
        
        logger.error("Operation failed: {}", errorData);
    }
}
```

## 🎯 **Padrões de Segurança**

### **1. Validação de Permissões**
```java
// Validador de permissões
public class PermissionValidator {
    
    public static boolean hasPermission(BigDecimal userId, String resource, String action) {
        try {
            // Verificar permissão no banco
            String sql = "SELECT COUNT(*) FROM AD_PERMISSOES WHERE CODUSU = ? AND RECURSO = ? AND ACAO = ?";
            
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            stmt.setBigDecimal(1, userId);
            stmt.setString(2, resource);
            stmt.setString(3, action);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Erro ao verificar permissão", e);
            return false;
        }
    }
    
    public static void validatePermission(BigDecimal userId, String resource, String action) throws Exception {
        if (!hasPermission(userId, resource, action)) {
            throw new Exception("Usuário não tem permissão para " + action + " em " + resource);
        }
    }
}
```

### **2. Sanitização de Dados**
```java
// Sanitizador de dados
public class DataSanitizer {
    
    public static String sanitizeString(String input) {
        if (input == null) return null;
        
        // Remover caracteres perigosos
        return input.replaceAll("[<>\"'&]", "")
                   .trim();
    }
    
    public static String sanitizeSQL(String input) {
        if (input == null) return null;
        
        // Remover caracteres SQL perigosos
        return input.replaceAll("[';\"\\\\]", "")
                   .trim();
    }
    
    public static Object sanitizeParameter(Object value, Class<?> expectedType) {
        if (value == null) return null;
        
        if (expectedType == String.class) {
            return sanitizeString(value.toString());
        }
        
        return value;
    }
}
```

## 📈 **Padrões de Configuração**

### **1. Configuração Centralizada**
```java
// Gerenciador de configurações
public class ConfigurationManager {
    private static final Map<String, Object> config = new ConcurrentHashMap<>();
    private static final Properties properties = new Properties();
    
    static {
        loadConfiguration();
    }
    
    private static void loadConfiguration() {
        try (InputStream is = ConfigurationManager.class.getResourceAsStream("/sankhya.properties")) {
            properties.load(is);
            
            // Carregar configurações do banco
            loadDatabaseConfiguration();
            
        } catch (IOException e) {
            logger.error("Erro ao carregar configurações", e);
        }
    }
    
    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Valor inválido para configuração {}: {}", key, value);
            }
        }
        return defaultValue;
    }
    
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
}
```

## 🎉 **Resumo dos Padrões Identificados**

### **Padrões Arquiteturais**
- ✅ **Singleton Thread-Safe**: Para classes principais
- ✅ **Factory Pattern**: Para criação de objetos
- ✅ **DAO Pattern**: Para acesso a dados
- ✅ **Observer Pattern**: Para eventos
- ✅ **Strategy Pattern**: Para diferentes tipos de ação

### **Padrões de Implementação**
- ✅ **Lazy Loading**: Para performance
- ✅ **Connection Pooling**: Para gerenciamento de conexões
- ✅ **Cache Management**: Para otimização
- ✅ **Transaction Management**: Para consistência
- ✅ **Error Handling**: Para robustez

### **Padrões de Segurança**
- ✅ **Permission Validation**: Para controle de acesso
- ✅ **Data Sanitization**: Para segurança de dados
- ✅ **Input Validation**: Para validação de entrada
- ✅ **Audit Logging**: Para rastreabilidade

### **Padrões de Performance**
- ✅ **Caching**: Para otimização de consultas
- ✅ **Connection Pooling**: Para reutilização de conexões
- ✅ **Lazy Loading**: Para carregamento sob demanda
- ✅ **Batch Processing**: Para processamento em lote

---

*Estes padrões extraídos do SankhyaW 4.8 fornecem uma base sólida para entender como implementar soluções robustas e eficientes no ecossistema Sankhya.*
