# ☕ Módulo Java Sankhya - Guia Completo

## 🎯 Visão Geral

Este documento apresenta um guia completo sobre desenvolvimento de módulos Java no Sankhya, incluindo arquitetura, implementação, padrões e exemplos práticos baseados na análise do código fonte SankhyaW 4.8.

## 🏗️ **Arquitetura do Módulo Java**

### **1. Estrutura de um Módulo Java**

```
MeuModulo/
├── src/
│   └── br/
│       └── com/
│           └── empresa/
│               └── modulojava/
│                   ├── entities/          # Entidades do módulo
│                   ├── services/          # Serviços de negócio
│                   ├── controllers/       # Controladores
│                   ├── repositories/      # Repositórios de dados
│                   ├── utils/            # Utilitários
│                   └── config/           # Configurações
├── lib/                                  # Bibliotecas externas
├── resources/                            # Recursos do módulo
│   ├── META-INF/
│   │   └── mgemodule-cfg.xml            # Configuração do módulo
│   └── sql/                             # Scripts SQL
└── build.xml                            # Script de build
```

### **2. Configuração do Módulo**

#### **mgemodule-cfg.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<module>
    <name>MeuModuloJava</name>
    <version>1.0.0</version>
    <description>Módulo Java para funcionalidades customizadas</description>
    
    <dependencies>
        <dependency>
            <name>MGE-Modelcore</name>
            <version>4.8.0</version>
        </dependency>
        <dependency>
            <name>SankhyaUtil</name>
            <version>4.8.0</version>
        </dependency>
    </dependencies>
    
    <java-modules>
        <module>
            <name>MeuModuloJava</name>
            <package>br.com.empresa.modulojava</package>
            <class-path>lib/</class-path>
        </module>
    </java-modules>
    
    <data-dictionary>
        <table name="AD_MY_TABLE">
            <column name="ID" type="NUMBER" primary-key="true"/>
            <column name="NOME" type="VARCHAR2" length="100"/>
            <column name="DESCRICAO" type="VARCHAR2" length="500"/>
            <column name="ATIVO" type="VARCHAR2" length="1" default="S"/>
        </table>
    </data-dictionary>
</module>
```

## 🔧 **Implementação de Serviços**

### **1. Serviço Base**

```java
package br.com.empresa.modulojava.services;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.MGECoreParameter;

/**
 * Serviço base para operações comuns do módulo
 */
public abstract class BaseModuleService {
    
    protected EntityFacade facade;
    
    public BaseModuleService() {
        this.facade = EntityFacadeFactory.getDWFFacade();
    }
    
    /**
     * Obtém parâmetro do sistema
     */
    protected String getParameter(String paramName) {
        try {
            return MGECoreParameter.getParameter(paramName);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Obtém parâmetro do sistema com valor padrão
     */
    protected String getParameter(String paramName, String defaultValue) {
        String value = getParameter(paramName);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Valida se o módulo está ativo
     */
    protected boolean isModuleActive() {
        String active = getParameter("MODULO.MEU_MODULO.ATIVO", "N");
        return "S".equals(active);
    }
    
    /**
     * Log de operações
     */
    protected void logOperation(String operation, String details) {
        System.out.println(String.format("[MeuModulo] %s - %s", operation, details));
    }
}
```

### **2. Serviço de Entidade**

```java
package br.com.empresa.modulojava.services;

import java.math.BigDecimal;
import java.util.List;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.sql.NativeSql;

/**
 * Serviço para operações com entidades customizadas
 */
public class EntidadeCustomizadaService extends BaseModuleService {
    
    private static final String ENTITY_NAME = "AD_MY_TABLE";
    
    /**
     * Criar nova entidade
     */
    public DynamicVO criarEntidade(String nome, String descricao) throws Exception {
        if (!isModuleActive()) {
            throw new Exception("Módulo não está ativo");
        }
        
        logOperation("CRIAR_ENTIDADE", String.format("Nome: %s", nome));
        
        DynamicVO entity = facade.createEntity(ENTITY_NAME);
        entity.setProperty("NOME", nome);
        entity.setProperty("DESCRICAO", descricao);
        entity.setProperty("ATIVO", "S");
        
        DynamicVO savedEntity = facade.saveEntity(ENTITY_NAME, entity);
        
        logOperation("ENTIDADE_CRIADA", String.format("ID: %s", savedEntity.asBigDecimal("ID")));
        
        return savedEntity;
    }
    
    /**
     * Buscar entidade por ID
     */
    public DynamicVO buscarPorId(BigDecimal id) throws Exception {
        return facade.findEntityByPrimaryKey(ENTITY_NAME, id);
    }
    
    /**
     * Buscar entidades ativas
     */
    public List<DynamicVO> buscarAtivas() throws Exception {
        FinderWrapper finder = new FinderWrapper(ENTITY_NAME);
        finder.setDynamicFinder("EntidadeCustomizada.findAtivas");
        
        return facade.findByDynamicFinder(finder);
    }
    
    /**
     * Atualizar entidade
     */
    public DynamicVO atualizarEntidade(BigDecimal id, String nome, String descricao) throws Exception {
        DynamicVO entity = buscarPorId(id);
        if (entity == null) {
            throw new Exception("Entidade não encontrada: " + id);
        }
        
        entity.setProperty("NOME", nome);
        entity.setProperty("DESCRICAO", descricao);
        
        return facade.saveEntity(ENTITY_NAME, entity);
    }
    
    /**
     * Desativar entidade
     */
    public void desativarEntidade(BigDecimal id) throws Exception {
        DynamicVO entity = buscarPorId(id);
        if (entity == null) {
            throw new Exception("Entidade não encontrada: " + id);
        }
        
        entity.setProperty("ATIVO", "N");
        facade.saveEntity(ENTITY_NAME, entity);
        
        logOperation("ENTIDADE_DESATIVADA", String.format("ID: %s", id));
    }
    
    /**
     * Buscar por nome (usando SQL nativo)
     */
    public List<DynamicVO> buscarPorNome(String nome) throws Exception {
        NativeSql sql = new NativeSql(facade.getQueryExecutor());
        sql.appendSql("SELECT * FROM AD_MY_TABLE WHERE UPPER(NOME) LIKE UPPER(?) AND ATIVO = 'S'");
        sql.addParameter(nome);
        
        return sql.executeQuery();
    }
}
```

### **3. Serviço de Negócio**

```java
package br.com.empresa.modulojava.services;

import java.math.BigDecimal;
import java.util.List;

import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Serviço de regras de negócio específicas
 */
public class RegraNegocioService extends BaseModuleService {
    
    private EntidadeCustomizadaService entidadeService;
    
    public RegraNegocioService() {
        super();
        this.entidadeService = new EntidadeCustomizadaService();
    }
    
    /**
     * Processar lote de entidades
     */
    public void processarLote(List<BigDecimal> ids) throws Exception {
        logOperation("PROCESSAR_LOTE", String.format("Processando %d entidades", ids.size()));
        
        int processadas = 0;
        int erros = 0;
        
        for (BigDecimal id : ids) {
            try {
                processarEntidade(id);
                processadas++;
            } catch (Exception e) {
                erros++;
                logOperation("ERRO_PROCESSAMENTO", 
                           String.format("ID: %s, Erro: %s", id, e.getMessage()));
            }
        }
        
        logOperation("LOTE_PROCESSADO", 
                   String.format("Processadas: %d, Erros: %d", processadas, erros));
    }
    
    /**
     * Processar entidade individual
     */
    private void processarEntidade(BigDecimal id) throws Exception {
        DynamicVO entity = entidadeService.buscarPorId(id);
        
        if (entity == null) {
            throw new Exception("Entidade não encontrada: " + id);
        }
        
        // Aplicar regras de negócio
        aplicarRegras(entity);
        
        // Validar dados
        validarDados(entity);
        
        // Salvar alterações
        facade.saveEntity("AD_MY_TABLE", entity);
        
        logOperation("ENTIDADE_PROCESSADA", String.format("ID: %s", id));
    }
    
    /**
     * Aplicar regras de negócio
     */
    private void aplicarRegras(DynamicVO entity) {
        String nome = entity.asString("NOME");
        
        // Regra: Nome deve estar em maiúsculo
        if (nome != null && !nome.equals(nome.toUpperCase())) {
            entity.setProperty("NOME", nome.toUpperCase());
        }
        
        // Regra: Descrição não pode ser vazia
        String descricao = entity.asString("DESCRICAO");
        if (descricao == null || descricao.trim().isEmpty()) {
            entity.setProperty("DESCRICAO", "Descrição padrão");
        }
    }
    
    /**
     * Validar dados da entidade
     */
    private void validarDados(DynamicVO entity) throws Exception {
        String nome = entity.asString("NOME");
        
        if (nome == null || nome.trim().isEmpty()) {
            throw new Exception("Nome é obrigatório");
        }
        
        if (nome.length() > 100) {
            throw new Exception("Nome deve ter no máximo 100 caracteres");
        }
        
        // Verificar duplicatas
        List<DynamicVO> duplicatas = entidadeService.buscarPorNome(nome);
        BigDecimal idAtual = entity.asBigDecimal("ID");
        
        for (DynamicVO duplicata : duplicatas) {
            if (!duplicata.asBigDecimal("ID").equals(idAtual)) {
                throw new Exception("Já existe uma entidade com este nome");
            }
        }
    }
}
```

## 🎮 **Controladores e APIs**

### **1. Controller REST**

```java
package br.com.empresa.modulojava.controllers;

import java.math.BigDecimal;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.com.empresa.modulojava.services.EntidadeCustomizadaService;
import br.com.empresa.modulojava.services.RegraNegocioService;

/**
 * Controller REST para operações do módulo
 */
@Path("/meu-modulo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MeuModuloController {
    
    private EntidadeCustomizadaService entidadeService;
    private RegraNegocioService regraNegocioService;
    
    public MeuModuloController() {
        this.entidadeService = new EntidadeCustomizadaService();
        this.regraNegocioService = new RegraNegocioService();
    }
    
    /**
     * Criar nova entidade
     */
    @POST
    @Path("/entidades")
    public Response criarEntidade(@QueryParam("nome") String nome,
                                 @QueryParam("descricao") String descricao) {
        try {
            DynamicVO entity = entidadeService.criarEntidade(nome, descricao);
            
            return Response.status(Response.Status.CREATED)
                          .entity(new EntidadeResponse(entity))
                          .build();
                          
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(new ErrorResponse(e.getMessage()))
                          .build();
        }
    }
    
    /**
     * Buscar entidade por ID
     */
    @GET
    @Path("/entidades/{id}")
    public Response buscarEntidade(@PathParam("id") BigDecimal id) {
        try {
            DynamicVO entity = entidadeService.buscarPorId(id);
            
            if (entity == null) {
                return Response.status(Response.Status.NOT_FOUND)
                              .entity(new ErrorResponse("Entidade não encontrada"))
                              .build();
            }
            
            return Response.ok(new EntidadeResponse(entity)).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(new ErrorResponse(e.getMessage()))
                          .build();
        }
    }
    
    /**
     * Listar entidades ativas
     */
    @GET
    @Path("/entidades")
    public Response listarEntidades() {
        try {
            List<DynamicVO> entities = entidadeService.buscarAtivas();
            
            List<EntidadeResponse> response = entities.stream()
                                                    .map(EntidadeResponse::new)
                                                    .collect(Collectors.toList());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(new ErrorResponse(e.getMessage()))
                          .build();
        }
    }
    
    /**
     * Atualizar entidade
     */
    @PUT
    @Path("/entidades/{id}")
    public Response atualizarEntidade(@PathParam("id") BigDecimal id,
                                     @QueryParam("nome") String nome,
                                     @QueryParam("descricao") String descricao) {
        try {
            DynamicVO entity = entidadeService.atualizarEntidade(id, nome, descricao);
            
            return Response.ok(new EntidadeResponse(entity)).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(new ErrorResponse(e.getMessage()))
                          .build();
        }
    }
    
    /**
     * Desativar entidade
     */
    @DELETE
    @Path("/entidades/{id}")
    public Response desativarEntidade(@PathParam("id") BigDecimal id) {
        try {
            entidadeService.desativarEntidade(id);
            
            return Response.ok(new SuccessResponse("Entidade desativada com sucesso")).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(new ErrorResponse(e.getMessage()))
                          .build();
        }
    }
    
    /**
     * Processar lote de entidades
     */
    @POST
    @Path("/processar-lote")
    public Response processarLote(List<BigDecimal> ids) {
        try {
            regraNegocioService.processarLote(ids);
            
            return Response.ok(new SuccessResponse("Lote processado com sucesso")).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(new ErrorResponse(e.getMessage()))
                          .build();
        }
    }
}
```

### **2. Classes de Resposta**

```java
package br.com.empresa.modulojava.controllers;

import br.com.sankhya.jape.vo.DynamicVO;
import java.math.BigDecimal;

/**
 * Classe de resposta para entidades
 */
public class EntidadeResponse {
    private BigDecimal id;
    private String nome;
    private String descricao;
    private String ativo;
    
    public EntidadeResponse(DynamicVO vo) {
        this.id = vo.asBigDecimal("ID");
        this.nome = vo.asString("NOME");
        this.descricao = vo.asString("DESCRICAO");
        this.ativo = vo.asString("ATIVO");
    }
    
    // Getters e setters
}

/**
 * Classe de resposta de sucesso
 */
public class SuccessResponse {
    private String message;
    private boolean success;
    
    public SuccessResponse(String message) {
        this.message = message;
        this.success = true;
    }
    
    // Getters e setters
}

/**
 * Classe de resposta de erro
 */
public class ErrorResponse {
    private String message;
    private boolean success;
    
    public ErrorResponse(String message) {
        this.message = message;
        this.success = false;
    }
    
    // Getters e setters
}
```

## 🔄 **Integração com Eventos Programados**

### **1. Evento Programado para o Módulo**

```java
package br.com.empresa.modulojava.events;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Evento programado para entidades do módulo
 */
public class EntidadeCustomizadaEvent implements EventoProgramavelJava {
    
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO entity = (DynamicVO) event.getVo();
        
        // Validar dados antes da inserção
        validarDados(entity);
        
        // Definir valores padrão
        if (entity.asString("ATIVO") == null) {
            entity.setProperty("ATIVO", "S");
        }
        
        System.out.println("Inserindo nova entidade: " + entity.asString("NOME"));
    }
    
    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO entity = (DynamicVO) event.getVo();
        
        // Ações pós-inserção
        notificarInsercao(entity);
        atualizarIndicadores(entity);
        
        System.out.println("Entidade inserida com sucesso: " + entity.asBigDecimal("ID"));
    }
    
    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO entity = (DynamicVO) event.getVo();
        
        // Validar dados antes da atualização
        validarDados(entity);
        
        // Registrar alterações
        registrarAlteracoes(entity);
        
        System.out.println("Atualizando entidade: " + entity.asBigDecimal("ID"));
    }
    
    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO entity = (DynamicVO) event.getVo();
        
        // Ações pós-atualização
        notificarAtualizacao(entity);
        
        System.out.println("Entidade atualizada com sucesso: " + entity.asBigDecimal("ID"));
    }
    
    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {
        DynamicVO entity = (DynamicVO) event.getVo();
        
        // Verificar se pode ser deletada
        if (!podeDeletar(entity)) {
            throw new Exception("Entidade não pode ser deletada");
        }
        
        System.out.println("Deletando entidade: " + entity.asBigDecimal("ID"));
    }
    
    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {
        DynamicVO entity = (DynamicVO) event.getVo();
        
        // Ações pós-deleção
        notificarDelecao(entity);
        
        System.out.println("Entidade deletada com sucesso: " + entity.asBigDecimal("ID"));
    }
    
    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {
        // Ações antes do commit da transação
        System.out.println("Commit da transação iniciado");
    }
    
    private void validarDados(DynamicVO entity) throws Exception {
        String nome = entity.asString("NOME");
        
        if (nome == null || nome.trim().isEmpty()) {
            throw new Exception("Nome é obrigatório");
        }
        
        if (nome.length() > 100) {
            throw new Exception("Nome deve ter no máximo 100 caracteres");
        }
    }
    
    private void notificarInsercao(DynamicVO entity) {
        // Implementar notificação
    }
    
    private void notificarAtualizacao(DynamicVO entity) {
        // Implementar notificação
    }
    
    private void notificarDelecao(DynamicVO entity) {
        // Implementar notificação
    }
    
    private void atualizarIndicadores(DynamicVO entity) {
        // Implementar atualização de indicadores
    }
    
    private void registrarAlteracoes(DynamicVO entity) {
        // Implementar registro de alterações
    }
    
    private boolean podeDeletar(DynamicVO entity) {
        // Implementar lógica de verificação
        return true;
    }
}
```

## 🔧 **Utilitários e Helpers**

### **1. Utilitário de Validação**

```java
package br.com.empresa.modulojava.utils;

import br.com.sankhya.util.ValidadorCpfCnpj;
import br.com.sankhya.util.ValidadorEmail;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Utilitário para validações específicas do módulo
 */
public class ValidacaoUtils {
    
    private static final Pattern PATTERN_TELEFONE = Pattern.compile("^\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}$");
    
    /**
     * Validar CPF/CNPJ
     */
    public static boolean validarCpfCnpj(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            return false;
        }
        
        String documentoLimpo = documento.replaceAll("[^0-9]", "");
        
        if (documentoLimpo.length() == 11) {
            return ValidadorCpfCnpj.isValidCpf(documentoLimpo);
        } else if (documentoLimpo.length() == 14) {
            return ValidadorCpfCnpj.isValidCnpj(documentoLimpo);
        }
        
        return false;
    }
    
    /**
     * Validar email
     */
    public static boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return ValidadorEmail.isValidEmail(email);
    }
    
    /**
     * Validar telefone
     */
    public static boolean validarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return false;
        }
        
        return PATTERN_TELEFONE.matcher(telefone).matches();
    }
    
    /**
     * Validar valor monetário
     */
    public static boolean validarValorMonetario(BigDecimal valor) {
        if (valor == null) {
            return false;
        }
        
        return valor.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    /**
     * Validar porcentagem
     */
    public static boolean validarPorcentagem(BigDecimal porcentagem) {
        if (porcentagem == null) {
            return false;
        }
        
        return porcentagem.compareTo(BigDecimal.ZERO) >= 0 && 
               porcentagem.compareTo(new BigDecimal("100")) <= 0;
    }
    
    /**
     * Formatar CPF
     */
    public static String formatarCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return null;
        }
        
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        
        if (cpfLimpo.length() == 11) {
            return cpfLimpo.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        }
        
        return cpf;
    }
    
    /**
     * Formatar CNPJ
     */
    public static String formatarCnpj(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return null;
        }
        
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
        
        if (cnpjLimpo.length() == 14) {
            return cnpjLimpo.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
        }
        
        return cnpj;
    }
    
    /**
     * Formatar telefone
     */
    public static String formatarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return null;
        }
        
        String telefoneLimpo = telefone.replaceAll("[^0-9]", "");
        
        if (telefoneLimpo.length() == 10) {
            return telefoneLimpo.replaceAll("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
        } else if (telefoneLimpo.length() == 11) {
            return telefoneLimpo.replaceAll("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
        }
        
        return telefone;
    }
}
```

### **2. Utilitário de Configuração**

```java
package br.com.empresa.modulojava.utils;

import br.com.sankhya.modelcore.util.MGECoreParameter;

/**
 * Utilitário para configurações do módulo
 */
public class ConfiguracaoUtils {
    
    private static final String PREFIXO_PARAMETRO = "MODULO.MEU_MODULO.";
    
    /**
     * Obter parâmetro do módulo
     */
    public static String obterParametro(String nome) {
        try {
            return MGECoreParameter.getParameter(PREFIXO_PARAMETRO + nome);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Obter parâmetro com valor padrão
     */
    public static String obterParametro(String nome, String valorPadrao) {
        String valor = obterParametro(nome);
        return valor != null ? valor : valorPadrao;
    }
    
    /**
     * Verificar se módulo está ativo
     */
    public static boolean isModuloAtivo() {
        String ativo = obterParametro("ATIVO", "N");
        return "S".equals(ativo);
    }
    
    /**
     * Obter timeout de operações
     */
    public static int obterTimeout() {
        String timeout = obterParametro("TIMEOUT", "30");
        try {
            return Integer.parseInt(timeout);
        } catch (NumberFormatException e) {
            return 30;
        }
    }
    
    /**
     * Obter tamanho máximo de lote
     */
    public static int obterTamanhoMaximoLote() {
        String tamanho = obterParametro("TAMANHO_MAX_LOTE", "1000");
        try {
            return Integer.parseInt(tamanho);
        } catch (NumberFormatException e) {
            return 1000;
        }
    }
    
    /**
     * Verificar se deve logar operações
     */
    public static boolean deveLogarOperacoes() {
        String logar = obterParametro("LOGAR_OPERACOES", "S");
        return "S".equals(logar);
    }
    
    /**
     * Obter URL de callback
     */
    public static String obterUrlCallback() {
        return obterParametro("URL_CALLBACK");
    }
    
    /**
     * Verificar se deve enviar notificações
     */
    public static boolean deveEnviarNotificacoes() {
        String notificar = obterParametro("ENVIAR_NOTIFICACOES", "S");
        return "S".equals(notificar);
    }
}
```

## 📊 **Monitoramento e Logs**

### **1. Logger Customizado**

```java
package br.com.empresa.modulojava.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger customizado para o módulo
 */
public class ModuleLogger {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String MODULE_NAME = "MeuModuloJava";
    
    /**
     * Log de informação
     */
    public static void info(String message) {
        if (ConfiguracaoUtils.deveLogarOperacoes()) {
            log("INFO", message);
        }
    }
    
    /**
     * Log de aviso
     */
    public static void warn(String message) {
        log("WARN", message);
    }
    
    /**
     * Log de erro
     */
    public static void error(String message, Throwable throwable) {
        log("ERROR", message + " - " + throwable.getMessage());
        throwable.printStackTrace();
    }
    
    /**
     * Log de debug
     */
    public static void debug(String message) {
        if (ConfiguracaoUtils.deveLogarOperacoes()) {
            log("DEBUG", message);
        }
    }
    
    /**
     * Log de operação
     */
    public static void operation(String operation, String details) {
        info(String.format("%s - %s", operation, details));
    }
    
    /**
     * Log de performance
     */
    public static void performance(String operation, long duration) {
        info(String.format("PERFORMANCE - %s executado em %dms", operation, duration));
    }
    
    private static void log(String level, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format("[%s] %s [%s] %s", 
                                         timestamp, level, MODULE_NAME, message);
        
        System.out.println(logMessage);
    }
}
```

## 🎯 **Exemplos de Uso**

### **1. Exemplo de Implementação Completa**

```java
package br.com.empresa.modulojava.examples;

import br.com.empresa.modulojava.services.EntidadeCustomizadaService;
import br.com.empresa.modulojava.services.RegraNegocioService;
import br.com.empresa.modulojava.utils.ConfiguracaoUtils;
import br.com.empresa.modulojava.utils.ModuleLogger;
import br.com.empresa.modulojava.utils.ValidacaoUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Exemplo de uso do módulo Java
 */
public class ExemploUsoModulo {
    
    public static void main(String[] args) {
        try {
            // Verificar se módulo está ativo
            if (!ConfiguracaoUtils.isModuloAtivo()) {
                ModuleLogger.warn("Módulo não está ativo");
                return;
            }
            
            ModuleLogger.info("Iniciando exemplo de uso do módulo");
            
            // Criar serviço
            EntidadeCustomizadaService service = new EntidadeCustomizadaService();
            
            // Criar entidades de exemplo
            criarEntidadesExemplo(service);
            
            // Buscar e processar entidades
            processarEntidades(service);
            
            ModuleLogger.info("Exemplo concluído com sucesso");
            
        } catch (Exception e) {
            ModuleLogger.error("Erro no exemplo", e);
        }
    }
    
    private static void criarEntidadesExemplo(EntidadeCustomizadaService service) throws Exception {
        ModuleLogger.info("Criando entidades de exemplo");
        
        // Criar entidade 1
        DynamicVO entidade1 = service.criarEntidade("ENTIDADE_EXEMPLO_1", "Descrição da entidade 1");
        ModuleLogger.info("Entidade 1 criada com ID: " + entidade1.asBigDecimal("ID"));
        
        // Criar entidade 2
        DynamicVO entidade2 = service.criarEntidade("ENTIDADE_EXEMPLO_2", "Descrição da entidade 2");
        ModuleLogger.info("Entidade 2 criada com ID: " + entidade2.asBigDecimal("ID"));
        
        // Criar entidade 3
        DynamicVO entidade3 = service.criarEntidade("ENTIDADE_EXEMPLO_3", "Descrição da entidade 3");
        ModuleLogger.info("Entidade 3 criada com ID: " + entidade3.asBigDecimal("ID"));
    }
    
    private static void processarEntidades(EntidadeCustomizadaService service) throws Exception {
        ModuleLogger.info("Processando entidades");
        
        // Buscar entidades ativas
        List<DynamicVO> entidades = service.buscarAtivas();
        ModuleLogger.info("Encontradas " + entidades.size() + " entidades ativas");
        
        // Extrair IDs para processamento em lote
        List<BigDecimal> ids = entidades.stream()
                                       .map(vo -> vo.asBigDecimal("ID"))
                                       .collect(Collectors.toList());
        
        // Processar em lote
        RegraNegocioService regraService = new RegraNegocioService();
        regraService.processarLote(ids);
        
        ModuleLogger.info("Processamento em lote concluído");
    }
}
```

## 🎯 **Boas Práticas**

### **1. Estrutura e Organização**
- **Pacotes Lógicos**: Organize classes por funcionalidade
- **Separação de Responsabilidades**: Separe serviços, controladores e utilitários
- **Configuração Externa**: Use arquivos de configuração
- **Documentação**: Documente todas as classes e métodos públicos

### **2. Tratamento de Erros**
- **Exceções Específicas**: Crie exceções específicas para o módulo
- **Validação Robusta**: Valide todos os dados de entrada
- **Logs Detalhados**: Registre operações importantes
- **Rollback**: Implemente rollback em caso de erro

### **3. Performance**
- **Connection Pooling**: Use pool de conexões
- **Cache**: Implemente cache quando apropriado
- **Batch Processing**: Processe dados em lotes
- **Índices**: Crie índices para consultas frequentes

### **4. Segurança**
- **Validação de Entrada**: Valide todos os dados
- **Autorização**: Verifique permissões
- **Auditoria**: Registre operações sensíveis
- **Criptografia**: Criptografe dados sensíveis

## 🎊 **Conclusão**

O desenvolvimento de módulos Java no Sankhya deve seguir:

- **✅ Arquitetura Limpa**: Estrutura bem organizada
- **✅ Serviços Bem Definidos**: Separação clara de responsabilidades
- **✅ Integração Completa**: Eventos programados e APIs
- **✅ Utilitários Reutilizáveis**: Código modular e reutilizável
- **✅ Monitoramento**: Logs e métricas adequadas
- **✅ Configuração Flexível**: Parâmetros externos
- **✅ Tratamento de Erros**: Validação e tratamento robustos

### **Benefícios:**
- **Modularidade**: Código organizado e reutilizável
- **Manutenibilidade**: Fácil manutenção e evolução
- **Integração**: Integração completa com Sankhya
- **Performance**: Otimizado para alta performance
- **Escalabilidade**: Suporte a crescimento

---

*Este documento fornece um guia completo para desenvolvimento de módulos Java robustos e eficientes no Sankhya.*
