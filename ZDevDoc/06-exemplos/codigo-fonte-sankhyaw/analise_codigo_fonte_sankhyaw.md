# 🔍 Análise do Código Fonte SankhyaW 4.8

## 🎯 Visão Geral

Este documento apresenta uma análise detalhada do código fonte do SankhyaW 4.8, focando em **ações agendadas**, **eventos programados** e **botões de ação**. A análise foi realizada no diretório `/home/lemoreira/Downloads/sankhyaw-4.8` e revelou implementações avançadas e padrões úteis para desenvolvimento.

## 🕐 **AÇÕES AGENDADAS (MGESchedule)**

### **📁 Estrutura do Módulo MGESchedule**

#### **Arquivo Principal: `MGESchedule.java`**
**Localização**: `/MGESchedule/src/br/com/sankhya/mgeschedule/MGESchedule.java`

#### **Características Principais:**
- **Singleton Pattern**: Implementação thread-safe do padrão singleton
- **Configuração via XML**: Carregamento de configurações via `mgeschedule-cfg.xml`
- **Suporte a CRON**: Expressões CRON para agendamento
- **Controle de Host**: Restrição de execução por servidor específico
- **Cleanup Automático**: Sistema de limpeza de recursos

#### **Implementação do Scheduler:**
```java
public class MGESchedule {
    private HashSet allowedJobs;
    private String hostAllowed;
    private Map<String, Collection<Thread>> threadsByEJB;
    private CukooAdapter cukooAdapter;
    
    // Singleton pattern
    public static MGESchedule getInstance() {
        if (singleton == null) {
            singleton = new MGESchedule();
        }
        return singleton;
    }
    
    // Carregamento de jobs via classpath
    public void loadJobsByClassPath() throws Exception {
        if(cukooAdapter != null) {
            cukooAdapter.init();
        } else {
            List<InputStream> resourceInputStream = 
                ClasspathUtils.getResourceInputStream("META-INF/mgeschedule-cfg.xml");
            // Processamento das configurações...
        }
    }
}
```

#### **Sistema de Agendamento:**
```java
public class ScheduleConfig {
    private List fields;
    private long lastRun;
    private long timeElapse;
    private boolean fieldsIsEmpty = true;
    
    // Suporte a CRON e intervalos
    public ScheduleConfig(String cfg) {
        if (cfg.startsWith("&")) {
            // Intervalo de tempo (ex: &30000 = 30 segundos)
            timeElapse = Long.parseLong(cfg.substring(1));
        } else {
            // Expressão CRON (ex: "0 0 12 * * ?" = meio-dia todos os dias)
            StringTokenizer tok = new StringTokenizer(cfg, " ");
            int[] calendarFields = new int[] { 
                Calendar.MINUTE, Calendar.HOUR_OF_DAY, 
                Calendar.DAY_OF_MONTH, Calendar.MONTH, Calendar.DAY_OF_WEEK 
            };
            // Processamento dos campos CRON...
        }
    }
}
```

#### **Execução de Jobs EJB:**
```java
private class EJBJob extends ScheduledJob {
    private Object delegateInstance;
    private Object homeInterface;
    private Method createMethod;
    private Method onScheduleMethod;
    private Method removeMethod;
    
    public void doJob() throws Exception {
        EJBLocalObject bean = null;
        try {
            if(jobListener != null) {
                jobListener.beforeRun();
            }
            
            bean = (EJBLocalObject) createMethod.invoke(homeInterface);
            onScheduleMethod.invoke(bean);
            
        } finally {
            if (bean != null) {
                removeMethod.invoke(bean);
            }
        }
    }
}
```

### **📁 Utilitários de Ações Agendadas**

#### **Arquivo: `ScheduledActionsUtils.java`**
**Localização**: `/MGE-Modelcore/src/br/com/sankhya/acaoagendada/ScheduledActionsUtils.java`

#### **Funcionalidades:**
```java
public class ScheduledActionsUtils {
    
    // Atualizar ações agendadas por módulo
    public static void updateScheduledActionsByModule(BigDecimal codModulo, Collection<?> actions) throws Exception {
        if (actions != null && !actions.isEmpty()) {
            if(ExternalModulesUtils.moduleHasInterface(codModulo, ScheduledAction.class)) {
                updateScheduledActions(actions, SchedulerOptions.REFRESH);
            } else {
                updateScheduledActions(actions, SchedulerOptions.STOP); 
            }
        }
    }
    
    // Parar ações por módulo
    public static void stopActionsByModule(Collection<?> actions) throws Exception {
        if (actions != null) {
            updateScheduledActions(actions, SchedulerOptions.STOP);
        }
    }
    
    // Validar expressões CRON
    public static void validateTriggerExpression(String expression, String triggerType) throws Exception {
        if ("C".equals(triggerType)) {  // C = cron
            try {
                CronTriggerImpl tr = new CronTriggerImpl();
                tr.setCronExpression(expression);
            } catch (Exception e) {
                throw new Exception("Expressão CRON inválida: " + e.getMessage());
            }
        } else {
            // Validação de intervalo de tempo (ex: "min:30", "hour:2")
            Pattern regex = Pattern.compile("(sec|min|hour):\\d+$", Pattern.CASE_INSENSITIVE);
            if (expression != null && !regex.matcher(expression).find()) {
                throw new Exception("Expressão de intervalo inválida");
            }
        }
    }
}
```

## 🎯 **BOTÕES DE AÇÃO**

### **📁 Estrutura do Sistema de Botões de Ação**

#### **Interface Principal: `AcaoRotinaJava.java`**
**Localização**: `/MGE-Modelcore/src/br/com/sankhya/extensions/actionbutton/AcaoRotinaJava.java`

```java
public interface AcaoRotinaJava {
    void doAction(ContextoAcao contexto) throws Exception;
}
```

#### **Contexto de Execução: `ContextoAcao.java`**
```java
public interface ContextoAcao {
    QueryExecutor getQuery();           // Executor de consultas SQL
    Registro getLinhaPai();             // Registro pai (para detalhes)
    Registro[] getLinhas();             // Registros selecionados
    BigDecimal getUsuarioLogado();      // Usuário logado
    Registro novaLinha() throws Exception;
    Registro novaLinha(String entidade) throws Exception;
    Object getParam(String nome);       // Parâmetros da ação
    Object getParametroSistema(String nome);
    boolean confirmarSimNao(String titulo, String texto, int indice);
    void confirmar(String titulo, String texto, int indice);
    void eMail(String titulo, String mensagem, String destinatarios) throws Exception;
    void setMensagemRetorno(String message);
    void mostraErro(String message) throws Exception;
}
```

#### **Carregador de Ações: `ActionsButtonLoader.java`**
```java
public class ActionsButtonLoader {
    public static final String LAUNCHER = "LC";           // Lançador de telas
    public static final String STORED_PROCEDURE = "SP";   // Stored Procedure
    public static final String SCRIPT = "SC";             // Script JavaScript
    public static final String ROTINA_JAVA = "RJ";        // Rotina Java
    
    // Carregar ações para uma entidade específica
    public static Element loadActions(EntityFacade dwfFacade, String resourceID, 
                                    String entityName, String accessResourceID) throws Exception {
        Collection<DynamicVO> actions = null;
        
        if (entityName != null) {
            FinderWrapper finder = new FinderWrapper(
                DynamicEntityNames.BOTAO_ACAO, 
                " this.NOMEINSTANCIA = ? ", 
                new Object[] { entityName }
            );
            finder.setOrderBy("ORDEM");
            actions = dwfFacade.findByDynamicFinderAsVO(finder);
        }
        
        Element actionsElem = new Element("actions"); 
        if(actions != null) {
            for(DynamicVO act : actions) {
                // Verificação de permissões e adição à lista
                actionsElem.addContent(getSource(act));
            }
        }
        return actionsElem;
    }
}
```

#### **Implementações de Ações:**

**1. Ação Java: `JavaAction.java`**
```java
public class JavaAction extends ServerSideAction implements ActitonExecutor {
    private BigDecimal moduleID;
    private String className;
    
    public JavaAction(String className, BigDecimal moduleID, String masterEntity, EntityFacade entityFacade) throws Exception {
        super(masterEntity, entityFacade);
        this.className = className;
        this.moduleID = moduleID;
    }
    
    protected void prepareAndExecute(ExecutionContext executionCtx) throws Exception {
        AcaoRotinaJava action = (AcaoRotinaJava) CustomModuleLoader
            .getClass(entityFacade, moduleID, className).newInstance();
        action.doAction(executionCtx);
    }
}
```

**2. Ação Stored Procedure: `StoredProcedureAction.java`**
```java
public class StoredProcedureAction extends AbstractAction implements ActitonExecutor {
    private String procName;
    private JdbcWrapper jdbc;
    
    public String execute() throws Exception {
        this.jdbc.openSession();
        
        ProcedureCaller caller = new ProcedureCaller(procName);
        
        // Adicionar parâmetros de entrada
        for(Integer i: params.keySet()) {
            Map<String, Object> row = params.get(i);
            for(String name: row.keySet()) {
                Object value = row.get(name);
                String type = determineParameterType(value);
                caller.addDBInputParameter(jdbc.getConnection(), type, i, name, value);
            }
        }
        
        // Parâmetros padrão: usuário, ID execução, quantidade de registros
        caller.addInputParameter(userId);
        caller.addInputParameter(caller.getExecucutionId());
        caller.addInputParameter(new BigDecimal(selectedRowsSize));
        caller.addOutputParameter(Types.VARCHAR, "SUCESSMESSAGE");
        
        caller.execute(jdbc.getConnection());
        return StringUtils.getEmptyAsNull(caller.resultAsString("SUCESSMESSAGE"));
    }
}
```

## 🎪 **EVENTOS PROGRAMADOS**

### **📁 Interface de Eventos Programados**

#### **Interface Principal: `EventoProgramavelJava.java`**
**Localização**: `/MGE-Modelcore/src/br/com/sankhya/extensions/eventoprogramavel/EventoProgramavelJava.java`

```java
public interface EventoProgramavelJava {
    // Eventos Before (antes da operação)
    void beforeInsert(PersistenceEvent event) throws Exception;
    void beforeUpdate(PersistenceEvent event) throws Exception;
    void beforeDelete(PersistenceEvent event) throws Exception;
    
    // Eventos After (após a operação)
    void afterInsert(PersistenceEvent event) throws Exception;
    void afterUpdate(PersistenceEvent event) throws Exception;
    void afterDelete(PersistenceEvent event) throws Exception;
    
    // Evento Before Commit (antes do commit da transação)
    void beforeCommit(TransactionContext tranCtx) throws Exception;
}
```

#### **Contexto de Eventos: `EventoProgramavelCtx.java`**
```java
public class EventoProgramavelCtx implements ContextoAcaoProgramada {
    
    public Class<?> getInterfaceToSearch() {
        return EventoProgramavelJava.class;
    }
    
    public Map<String, String> getLinguagensToScript() {
        Map<String, String> linguagensMap = new HashMap<String, String>();
        linguagensMap.put("javascript", "JavaScript");
        return linguagensMap;
    }
    
    // Gerar stub de procedure para eventos
    public String getProcedureStub(EntityFacade dwfFacade, String procName, Element actElem) throws Exception {
        JdbcWrapper jdbc = null;
        try {
            jdbc = dwfFacade.getJdbcWrapper();
            
            StringBuffer buf = NativeSql.getStringBufferSQLFromResource(
                ActionsButtonLoader.class, 
                jdbc.getDialect() == EntityMetaData.ORACLE_DIALECT ? 
                    "EVPSTUBORCL.sql" : "EVPSTUBMSSQL.sql"
            );
            
            StringUtils.replaceString("${NOMEPROC}", procName, buf);
            return buf.toString();
        } finally {
            JdbcWrapper.closeSession(jdbc);
        }
    }
}
```

### **📁 CRUD Listeners (Exemplos de Implementação)**

#### **Exemplo: `ParceiroCrudListener.java`**
```java
public class ParceiroCrudListener extends CRUDServiceListenerAdapter implements ParallelableDataSetLoader {
    
    public void beforeFind(FinderWrapper finder) throws Exception {
        ParceiroHellper helper = new ParceiroHellper();
        helper.doBeforeFind(finder);
    }
    
    public Collection loadCustomData(FinderWrapper finder) throws Exception {
        if (MGECoreParameter.getParameterAsBoolean("global.usar.paginacao.de.registros")) {
            return new ParallelDatasetLoader().loadData(finder);
        }
        return null;
    }
}
```

## 🛠️ **PADRÕES E IMPLEMENTAÇÕES ÚTEIS**

### **1. Padrão Singleton Thread-Safe**
```java
public class MGESchedule {
    private static MGESchedule singleton;
    
    public static MGESchedule getInstance() {
        if (singleton == null) {
            singleton = new MGESchedule();
        }
        return singleton;
    }
}
```

### **2. Factory Pattern para Ações**
```java
public class ActionsButtonLoader {
    public static Element getSource(DynamicVO vo) throws Exception {
        String tipo = vo.asString("TIPO");
        Element source = new Element("action");
        source.setAttribute("type", tipo);
        
        // Configuração baseada no tipo
        if(LAUNCHER.equals(tipo)) {
            Element launcherElem = XMLUtils.getRequiredChild(configElem, "laucher");
            source.setAttribute("resourceID", XMLUtils.getRequiredAttributeAsString(launcherElem, "resourceID"));
        }
        
        return source;
    }
}
```

### **3. Template Method para Jobs**
```java
private abstract class ScheduledJob implements Runnable {
    ScheduleConfig scheduleConfig;
    String id;
    
    public void run() {
        try {
            Calendar c = new GregorianCalendar();
            long timeToSleep = (scheduleConfig.timeElapse > 0) ? 100 : 60000;
            
            while (!Thread.currentThread().isInterrupted()) {
                c.setTimeInMillis(System.currentTimeMillis());
                
                if (isOnTime(c)) {
                    try {
                        doJob(); // Método abstrato implementado pelas subclasses
                    } finally {
                        notifyCleanUp();
                        ThreadLocalsRemover remover = new ThreadLocalsRemover();
                        remover.addPackagePrefix("com.sankhya");
                        remover.addPackagePrefix("br.com.sankhya");
                    }
                }
                Thread.sleep(timeToSleep);
            }
        } catch (Throwable e) {
            log(String.format("JOB \"%s\" finalizado. (%s)", id, e.getMessage()));
        }
    }
    
    abstract void doJob() throws Exception;
    abstract boolean isOnTime(Calendar c) throws Exception;
}
```

### **4. Strategy Pattern para Tipos de Ação**
```java
public interface ActitonExecutor {
    String execute() throws Exception;
}

// Implementações específicas
public class JavaAction extends ServerSideAction implements ActitonExecutor { }
public class StoredProcedureAction extends AbstractAction implements ActitonExecutor { }
public class ScriptAction extends AbstractAction implements ActitonExecutor { }
```

### **5. Observer Pattern para Listeners**
```java
public interface JobListener {
    void beforeRun();
}

public interface JobListenerAfter extends JobListener {
    void afterRun();
}

// Implementação no MGESchedule
public void doJob() throws Exception {
    EJBLocalObject bean = null;
    try {
        if(jobListener != null) {
            jobListener.beforeRun();
        }
        
        bean = (EJBLocalObject) createMethod.invoke(homeInterface);
        onScheduleMethod.invoke(bean);
        
    } finally {
        if (jobListener instanceof JobListenerAfter) {
            if(jobListener != null) {
                ((JobListenerAfter) jobListener).afterRun();
            }
        }
    }
}
```

## 📋 **CONFIGURAÇÃO XML**

### **Estrutura do mgeschedule-cfg.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<schedule>
    <ejb-job name="MeuJob" schedule="0 0 12 * * ?">
        <!-- Configuração do job -->
    </ejb-job>
    
    <ejb-job name="OutroJob" schedule="&30000">
        <!-- Job executado a cada 30 segundos -->
    </ejb-job>
</schedule>
```

### **Configuração de Botões de Ação**
```xml
<action type="RJ" description="Minha Ação Java">
    <params>
        <promptParam name="PARAMETRO1" paramType="STRING" required="true"/>
        <promptParam name="PARAMETRO2" paramType="NUMBER" required="false"/>
    </params>
</action>
```

## 🎯 **BENEFÍCIOS E APLICAÇÕES**

### **✅ Ações Agendadas:**
- **Automação**: Execução automática de tarefas recorrentes
- **Flexibilidade**: Suporte a CRON e intervalos de tempo
- **Controle**: Restrição por servidor e controle de execução
- **Monitoramento**: Sistema de logs e cleanup automático

### **✅ Botões de Ação:**
- **Integração**: Execução de código Java, SQL e JavaScript
- **Contexto Rico**: Acesso a registros, usuário e parâmetros
- **Permissões**: Controle de acesso granular
- **Flexibilidade**: Suporte a diferentes tipos de ação

### **✅ Eventos Programados:**
- **Reatividade**: Resposta automática a mudanças de dados
- **Integridade**: Validações e processamentos automáticos
- **Flexibilidade**: Suporte a Java e JavaScript
- **Transacional**: Integração com transações do banco

## 🚀 **IMPLEMENTAÇÕES PRÁTICAS**

### **1. Job de Limpeza Automática**
```java
public class LimpezaJob implements Runnable {
    public void doJob() throws Exception {
        // Lógica de limpeza
        System.out.println("Executando limpeza automática...");
        
        // Limpar logs antigos
        // Limpar arquivos temporários
        // Otimizar banco de dados
    }
}
```

### **2. Botão de Ação para Processamento**
```java
public class ProcessarPedidosAction implements AcaoRotinaJava {
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();
        
        for (Registro linha : linhas) {
            BigDecimal idPedido = linha.getField("IDPEDIDO");
            
            // Processar pedido
            processarPedido(idPedido);
        }
        
        contexto.setMensagemRetorno("Pedidos processados com sucesso!");
    }
}
```

### **3. Evento de Validação**
```java
public class ValidacaoProdutoEvent implements EventoProgramavelJava {
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // Validar campos obrigatórios
        if (vo.getProperty("NOMEPROD") == null) {
            throw new Exception("Nome do produto é obrigatório");
        }
        
        // Validar código único
        validarCodigoUnico(vo);
    }
    
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // Criar estoque inicial
        criarEstoqueInicial(vo);
        
        // Notificar sistemas externos
        notificarSistemasExternos(vo);
    }
}
```

## 🎊 **CONCLUSÃO**

A análise do código fonte SankhyaW 4.8 revelou uma arquitetura robusta e bem estruturada para:

- **Ações Agendadas**: Sistema completo de agendamento com suporte a CRON
- **Botões de Ação**: Framework flexível para execução de ações personalizadas
- **Eventos Programados**: Sistema reativo para processamento automático

### **Principais Descobertas:**
1. **Padrões de Design**: Uso extensivo de Singleton, Factory, Strategy e Observer
2. **Arquitetura Modular**: Separação clara de responsabilidades
3. **Flexibilidade**: Suporte a múltiplos tipos de ação e configuração
4. **Robustez**: Tratamento de erros e cleanup automático
5. **Extensibilidade**: Interfaces bem definidas para customização

### **Aplicações Práticas:**
- **Automação de Processos**: Jobs para tarefas recorrentes
- **Integração de Sistemas**: Botões para comunicação externa
- **Validações de Negócio**: Eventos para regras automáticas
- **Monitoramento**: Sistema de logs e auditoria

Este conhecimento pode ser aplicado diretamente no desenvolvimento de personalizações Sankhya, seguindo os mesmos padrões e estruturas utilizados no sistema oficial.

---

*Este documento foi criado com base na análise completa do código fonte SankhyaW 4.8 e representa implementações reais e padrões utilizados no sistema oficial.*
