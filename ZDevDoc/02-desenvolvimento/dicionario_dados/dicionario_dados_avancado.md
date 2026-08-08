# 📊 Dicionário de Dados Avançado Sankhya

## 🎯 Visão Geral

Este documento apresenta implementações avançadas do Dicionário de Dados no Sankhya, extraídas do código fonte SankhyaW 4.8 e padrões de desenvolvimento enterprise.

## 🏗️ **Arquitetura do Dicionário de Dados**

### **1. Estrutura Base do Dicionário**

```java
package br.com.empresa.dicionario;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Classe base para operações com Dicionário de Dados
 */
public class DicionarioDadosBase {
    
    protected EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    protected DataDictionaryUtils dictionaryUtils = new DataDictionaryUtils();
    
    /**
     * Criar nova entidade no dicionário
     */
    public DynamicVO criarEntidade(String nomeEntidade) throws Exception {
        return facade.createEntity(nomeEntidade);
    }
    
    /**
     * Buscar entidade por chave primária
     */
    public DynamicVO buscarEntidade(String nomeEntidade, BigDecimal id) throws Exception {
        return facade.findEntityByPrimaryKey(nomeEntidade, id);
    }
    
    /**
     * Salvar entidade
     */
    public DynamicVO salvarEntidade(String nomeEntidade, DynamicVO entidade) throws Exception {
        return facade.saveEntity(nomeEntidade, entidade);
    }
    
    /**
     * Deletar entidade
     */
    public void deletarEntidade(String nomeEntidade, BigDecimal id) throws Exception {
        DynamicVO entidade = buscarEntidade(nomeEntidade, id);
        if (entidade != null) {
            facade.deleteEntity(nomeEntidade, entidade);
        }
    }
    
    /**
     * Obter metadados da entidade
     */
    public InstanceInformationMetadata obterMetadados(String nomeEntidade) throws Exception {
        return dictionaryUtils.getInstanceInformation(nomeEntidade);
    }
}
```

### **2. Gerenciador de Metadados**

```java
package br.com.empresa.dicionario;

import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils.InstanceInformationMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Gerenciador de metadados do dicionário de dados
 */
public class MetadadosManager {
    
    private DataDictionaryUtils dictionaryUtils = new DataDictionaryUtils();
    private Map<String, InstanceInformationMetadata> cache = new HashMap<>();
    
    /**
     * Obter metadados da entidade
     */
    public InstanceInformationMetadata obterMetadados(String nomeEntidade) throws Exception {
        // Verificar cache primeiro
        if (cache.containsKey(nomeEntidade)) {
            return cache.get(nomeEntidade);
        }
        
        // Buscar metadados
        InstanceInformationMetadata metadados = dictionaryUtils.getInstanceInformation(nomeEntidade);
        
        // Cachear resultado
        cache.put(nomeEntidade, metadados);
        
        return metadados;
    }
    
    /**
     * Obter informações do campo
     */
    public FieldMetadata obterInfoCampo(String nomeEntidade, String nomeCampo) throws Exception {
        InstanceInformationMetadata metadados = obterMetadados(nomeEntidade);
        
        // Buscar informações do campo
        for (FieldMetadata campo : metadados.getFields()) {
            if (campo.getName().equals(nomeCampo)) {
                return campo;
            }
        }
        
        return null;
    }
    
    /**
     * Validar estrutura da entidade
     */
    public boolean validarEstrutura(String nomeEntidade, Map<String, Object> dados) throws Exception {
        InstanceInformationMetadata metadados = obterMetadados(nomeEntidade);
        
        for (FieldMetadata campo : metadados.getFields()) {
            if (campo.isRequired() && !dados.containsKey(campo.getName())) {
                return false;
            }
            
            Object valor = dados.get(campo.getName());
            if (valor != null && !validarTipoCampo(campo, valor)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validar tipo do campo
     */
    private boolean validarTipoCampo(FieldMetadata campo, Object valor) {
        switch (campo.getType()) {
            case "NUMBER":
                return valor instanceof Number;
            case "VARCHAR2":
            case "CHAR":
                return valor instanceof String;
            case "DATE":
                return valor instanceof java.util.Date;
            case "CLOB":
                return valor instanceof String;
            case "BLOB":
                return valor instanceof byte[];
            default:
                return true;
        }
    }
    
    /**
     * Limpar cache
     */
    public void limparCache() {
        cache.clear();
    }
    
    /**
     * Classe para metadados de campo
     */
    public static class FieldMetadata {
        private String name;
        private String type;
        private int length;
        private int precision;
        private int scale;
        private boolean required;
        private String defaultValue;
        
        // Getters e setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }
        
        public int getPrecision() { return precision; }
        public void setPrecision(int precision) { this.precision = precision; }
        
        public int getScale() { return scale; }
        public void setScale(int scale) { this.scale = scale; }
        
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    }
}
```

## 🔧 **Implementações Específicas**

### **1. Gerenciador de Tabelas Customizadas**

```java
package br.com.empresa.dicionario;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Gerenciador de tabelas customizadas
 */
public class TabelasCustomizadasManager extends DicionarioDadosBase {
    
    /**
     * Criar tabela customizada
     */
    public void criarTabelaCustomizada(String nomeTabela, String descricao) throws Exception {
        String sql = String.format("""
            CREATE TABLE %s (
                ID NUMBER PRIMARY KEY,
                NOME VARCHAR2(100),
                DESCRICAO VARCHAR2(500),
                ATIVO VARCHAR2(1) DEFAULT 'S',
                DT_CADASTRO DATE DEFAULT SYSDATE,
                DT_ALTERACAO DATE,
                USUARIO_CADASTRO NUMBER,
                USUARIO_ALTERACAO NUMBER
            )
            """, nomeTabela);
        
        facade.getQueryExecutor().executeUpdate(sql);
        
        // Criar índices
        criarIndicesTabela(nomeTabela);
        
        // Criar trigger de auditoria
        criarTriggerAuditoria(nomeTabela);
        
        System.out.println("Tabela " + nomeTabela + " criada com sucesso");
    }
    
    /**
     * Criar índices para a tabela
     */
    private void criarIndicesTabela(String nomeTabela) throws Exception {
        String sql = String.format("""
            CREATE INDEX IDX_%s_NOME ON %s (NOME)
            """, nomeTabela, nomeTabela);
        
        facade.getQueryExecutor().executeUpdate(sql);
        
        sql = String.format("""
            CREATE INDEX IDX_%s_ATIVO ON %s (ATIVO)
            """, nomeTabela, nomeTabela);
        
        facade.getQueryExecutor().executeUpdate(sql);
    }
    
    /**
     * Criar trigger de auditoria
     */
    private void criarTriggerAuditoria(String nomeTabela) throws Exception {
        String sql = String.format("""
            CREATE OR REPLACE TRIGGER TRG_%s_AUDIT
            BEFORE UPDATE ON %s
            FOR EACH ROW
            BEGIN
                :NEW.DT_ALTERACAO := SYSDATE;
                :NEW.USUARIO_ALTERACAO := USER;
            END;
            """, nomeTabela, nomeTabela);
        
        facade.getQueryExecutor().executeUpdate(sql);
    }
    
    /**
     * Inserir registro na tabela customizada
     */
    public DynamicVO inserirRegistro(String nomeTabela, String nome, String descricao) throws Exception {
        DynamicVO registro = criarEntidade(nomeTabela);
        
        registro.setProperty("NOME", nome);
        registro.setProperty("DESCRICAO", descricao);
        registro.setProperty("ATIVO", "S");
        registro.setProperty("USUARIO_CADASTRO", getUsuarioAtual());
        
        return salvarEntidade(nomeTabela, registro);
    }
    
    /**
     * Buscar registros ativos
     */
    public List<DynamicVO> buscarRegistrosAtivos(String nomeTabela) throws Exception {
        String sql = String.format("SELECT * FROM %s WHERE ATIVO = 'S' ORDER BY NOME", nomeTabela);
        return facade.getQueryExecutor().executeQuery(sql);
    }
    
    /**
     * Atualizar registro
     */
    public DynamicVO atualizarRegistro(String nomeTabela, BigDecimal id, String nome, String descricao) throws Exception {
        DynamicVO registro = buscarEntidade(nomeTabela, id);
        
        if (registro != null) {
            registro.setProperty("NOME", nome);
            registro.setProperty("DESCRICAO", descricao);
            registro.setProperty("USUARIO_ALTERACAO", getUsuarioAtual());
            
            return salvarEntidade(nomeTabela, registro);
        }
        
        return null;
    }
    
    /**
     * Desativar registro
     */
    public void desativarRegistro(String nomeTabela, BigDecimal id) throws Exception {
        DynamicVO registro = buscarEntidade(nomeTabela, id);
        
        if (registro != null) {
            registro.setProperty("ATIVO", "N");
            registro.setProperty("USUARIO_ALTERACAO", getUsuarioAtual());
            
            salvarEntidade(nomeTabela, registro);
        }
    }
    
    /**
     * Obter usuário atual
     */
    private BigDecimal getUsuarioAtual() {
        // Implementar busca do usuário atual
        return new BigDecimal("1");
    }
}
```

### **2. Gerenciador de Relacionamentos**

```java
package br.com.empresa.dicionario;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Gerenciador de relacionamentos entre entidades
 */
public class RelacionamentosManager extends DicionarioDadosBase {
    
    /**
     * Criar relacionamento entre entidades
     */
    public void criarRelacionamento(String entidadePai, String entidadeFilho, 
                                   String campoChave, String tipoRelacionamento) throws Exception {
        String sql = String.format("""
            ALTER TABLE %s 
            ADD CONSTRAINT FK_%s_%s 
            FOREIGN KEY (%s) 
            REFERENCES %s (ID)
            """, entidadeFilho, entidadeFilho, entidadePai, campoChave, entidadePai);
        
        facade.getQueryExecutor().executeUpdate(sql);
        
        // Criar índice para performance
        String sqlIndex = String.format("""
            CREATE INDEX IDX_%s_%s ON %s (%s)
            """, entidadeFilho, campoChave, entidadeFilho, campoChave);
        
        facade.getQueryExecutor().executeUpdate(sqlIndex);
        
        System.out.println("Relacionamento criado entre " + entidadePai + " e " + entidadeFilho);
    }
    
    /**
     * Buscar registros relacionados
     */
    public List<DynamicVO> buscarRegistrosRelacionados(String entidadeFilho, String campoChave, 
                                                      BigDecimal idPai) throws Exception {
        String sql = String.format("SELECT * FROM %s WHERE %s = ?", entidadeFilho, campoChave);
        return facade.getQueryExecutor().executeQuery(sql, idPai);
    }
    
    /**
     * Contar registros relacionados
     */
    public int contarRegistrosRelacionados(String entidadeFilho, String campoChave, 
                                         BigDecimal idPai) throws Exception {
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", entidadeFilho, campoChave);
        List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql, idPai);
        
        if (!resultado.isEmpty()) {
            return resultado.get(0).asBigDecimal("1").intValue();
        }
        
        return 0;
    }
    
    /**
     * Validar integridade referencial
     */
    public boolean validarIntegridadeReferencial(String entidadePai, String entidadeFilho, 
                                                String campoChave, BigDecimal idPai) throws Exception {
        // Verificar se o registro pai existe
        DynamicVO registroPai = buscarEntidade(entidadePai, idPai);
        if (registroPai == null) {
            return false;
        }
        
        // Verificar se há registros filhos órfãos
        String sql = String.format("""
            SELECT COUNT(*) FROM %s f 
            WHERE f.%s = ? 
            AND NOT EXISTS (SELECT 1 FROM %s p WHERE p.ID = f.%s)
            """, entidadeFilho, campoChave, entidadePai, campoChave);
        
        List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql, idPai);
        
        if (!resultado.isEmpty()) {
            return resultado.get(0).asBigDecimal("1").intValue() == 0;
        }
        
        return true;
    }
    
    /**
     * Limpar registros órfãos
     */
    public int limparRegistrosOrfaos(String entidadeFilho, String campoChave, String entidadePai) throws Exception {
        String sql = String.format("""
            DELETE FROM %s 
            WHERE %s IN (
                SELECT f.%s FROM %s f 
                WHERE NOT EXISTS (
                    SELECT 1 FROM %s p WHERE p.ID = f.%s
                )
            )
            """, entidadeFilho, campoChave, campoChave, entidadeFilho, entidadePai, campoChave);
        
        int registrosRemovidos = facade.getQueryExecutor().executeUpdate(sql);
        
        System.out.println(registrosRemovidos + " registros órfãos removidos de " + entidadeFilho);
        
        return registrosRemovidos;
    }
}
```

### **3. Gerenciador de Campos Calculados**

```java
package br.com.empresa.dicionario;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Gerenciador de campos calculados
 */
public class CamposCalculadosManager extends DicionarioDadosBase {
    
    /**
     * Adicionar campo calculado
     */
    public void adicionarCampoCalculado(String nomeTabela, String nomeCampo, 
                                       String expressao, String tipoDados) throws Exception {
        String sql = String.format("""
            ALTER TABLE %s ADD %s %s
            """, nomeTabela, nomeCampo, tipoDados);
        
        facade.getQueryExecutor().executeUpdate(sql);
        
        // Criar trigger para calcular o campo
        criarTriggerCampoCalculado(nomeTabela, nomeCampo, expressao);
        
        System.out.println("Campo calculado " + nomeCampo + " adicionado à tabela " + nomeTabela);
    }
    
    /**
     * Criar trigger para campo calculado
     */
    private void criarTriggerCampoCalculado(String nomeTabela, String nomeCampo, String expressao) throws Exception {
        String sql = String.format("""
            CREATE OR REPLACE TRIGGER TRG_%s_%s_CALC
            BEFORE INSERT OR UPDATE ON %s
            FOR EACH ROW
            BEGIN
                :NEW.%s := %s;
            END;
            """, nomeTabela, nomeCampo, nomeTabela, nomeCampo, expressao);
        
        facade.getQueryExecutor().executeUpdate(sql);
    }
    
    /**
     * Recalcular campos calculados
     */
    public int recalcularCamposCalculados(String nomeTabela, String nomeCampo, String expressao) throws Exception {
        String sql = String.format("""
            UPDATE %s SET %s = %s
            """, nomeTabela, nomeCampo, expressao);
        
        int registrosAtualizados = facade.getQueryExecutor().executeUpdate(sql);
        
        System.out.println(registrosAtualizados + " registros atualizados na tabela " + nomeTabela);
        
        return registrosAtualizados;
    }
    
    /**
     * Validar campos calculados
     */
    public List<ValidationResult> validarCamposCalculados(String nomeTabela, String nomeCampo, String expressao) throws Exception {
        List<ValidationResult> resultados = new ArrayList<>();
        
        String sql = String.format("""
            SELECT ID, %s as VALOR_CALCULADO, %s as VALOR_ATUAL
            FROM %s 
            WHERE %s != %s
            """, expressao, nomeCampo, nomeTabela, expressao, nomeCampo);
        
        List<DynamicVO> registros = facade.getQueryExecutor().executeQuery(sql);
        
        for (DynamicVO registro : registros) {
            BigDecimal id = registro.asBigDecimal("ID");
            BigDecimal valorCalculado = registro.asBigDecimal("VALOR_CALCULADO");
            BigDecimal valorAtual = registro.asBigDecimal("VALOR_ATUAL");
            
            ValidationResult resultado = new ValidationResult(id, valorAtual, valorCalculado);
            resultados.add(resultado);
        }
        
        return resultados;
    }
    
    /**
     * Classe para resultado de validação
     */
    public static class ValidationResult {
        private BigDecimal id;
        private BigDecimal valorAtual;
        private BigDecimal valorCalculado;
        
        public ValidationResult(BigDecimal id, BigDecimal valorAtual, BigDecimal valorCalculado) {
            this.id = id;
            this.valorAtual = valorAtual;
            this.valorCalculado = valorCalculado;
        }
        
        public boolean isValid() {
            return valorAtual.compareTo(valorCalculado) == 0;
        }
        
        // Getters
        public BigDecimal getId() { return id; }
        public BigDecimal getValorAtual() { return valorAtual; }
        public BigDecimal getValorCalculado() { return valorCalculado; }
    }
}
```

## 📊 **Exemplos de Implementação**

### **1. Sistema de Configurações**

```java
package br.com.empresa.dicionario.exemplos;

import br.com.empresa.dicionario.TabelasCustomizadasManager;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Exemplo de sistema de configurações usando dicionário de dados
 */
public class SistemaConfiguracoes {
    
    private TabelasCustomizadasManager tabelaManager = new TabelasCustomizadasManager();
    private static final String TABELA_CONFIG = "AD_CONFIG_SISTEMA";
    
    /**
     * Inicializar sistema de configurações
     */
    public void inicializarSistema() throws Exception {
        // Criar tabela de configurações se não existir
        criarTabelaConfiguracoes();
        
        // Inserir configurações padrão
        inserirConfiguracoesPadrao();
    }
    
    /**
     * Criar tabela de configurações
     */
    private void criarTabelaConfiguracoes() throws Exception {
        String sql = """
            CREATE TABLE IF NOT EXISTS AD_CONFIG_SISTEMA (
                ID NUMBER PRIMARY KEY,
                CHAVE VARCHAR2(100) UNIQUE NOT NULL,
                VALOR VARCHAR2(500),
                DESCRICAO VARCHAR2(500),
                TIPO VARCHAR2(20) DEFAULT 'STRING',
                ATIVO VARCHAR2(1) DEFAULT 'S',
                DT_CADASTRO DATE DEFAULT SYSDATE,
                DT_ALTERACAO DATE,
                USUARIO_CADASTRO NUMBER,
                USUARIO_ALTERACAO NUMBER
            )
            """;
        
        tabelaManager.facade.getQueryExecutor().executeUpdate(sql);
    }
    
    /**
     * Inserir configurações padrão
     */
    private void inserirConfiguracoesPadrao() throws Exception {
        inserirConfiguracao("SISTEMA.NOME", "Sistema Empresarial", "Nome do sistema", "STRING");
        inserirConfiguracao("SISTEMA.VERSAO", "1.0.0", "Versão do sistema", "STRING");
        inserirConfiguracao("SISTEMA.TIMEOUT", "300", "Timeout em segundos", "NUMBER");
        inserirConfiguracao("SISTEMA.LOG_ATIVO", "S", "Se o log está ativo", "BOOLEAN");
        inserirConfiguracao("EMAIL.SERVIDOR", "smtp.empresa.com", "Servidor de email", "STRING");
        inserirConfiguracao("EMAIL.PORTA", "587", "Porta do servidor de email", "NUMBER");
        inserirConfiguracao("EMAIL.USUARIO", "sistema@empresa.com", "Usuário do email", "STRING");
    }
    
    /**
     * Inserir configuração
     */
    public DynamicVO inserirConfiguracao(String chave, String valor, String descricao, String tipo) throws Exception {
        return tabelaManager.inserirRegistro(TABELA_CONFIG, chave, descricao + " | Valor: " + valor);
    }
    
    /**
     * Obter configuração
     */
    public String obterConfiguracao(String chave) throws Exception {
        String sql = "SELECT VALOR FROM " + TABELA_CONFIG + " WHERE CHAVE = ? AND ATIVO = 'S'";
        List<DynamicVO> resultado = tabelaManager.facade.getQueryExecutor().executeQuery(sql, chave);
        
        if (!resultado.isEmpty()) {
            return resultado.get(0).asString("VALOR");
        }
        
        return null;
    }
    
    /**
     * Definir configuração
     */
    public void definirConfiguracao(String chave, String valor) throws Exception {
        String sql = "UPDATE " + TABELA_CONFIG + " SET VALOR = ?, DT_ALTERACAO = SYSDATE WHERE CHAVE = ?";
        tabelaManager.facade.getQueryExecutor().executeUpdate(sql, valor, chave);
    }
    
    /**
     * Listar todas as configurações
     */
    public List<DynamicVO> listarConfiguracoes() throws Exception {
        String sql = "SELECT * FROM " + TABELA_CONFIG + " WHERE ATIVO = 'S' ORDER BY CHAVE";
        return tabelaManager.facade.getQueryExecutor().executeQuery(sql);
    }
}
```

### **2. Sistema de Auditoria**

```java
package br.com.empresa.dicionario.exemplos;

import br.com.empresa.dicionario.TabelasCustomizadasManager;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Exemplo de sistema de auditoria usando dicionário de dados
 */
public class SistemaAuditoria {
    
    private TabelasCustomizadasManager tabelaManager = new TabelasCustomizadasManager();
    private static final String TABELA_AUDIT = "AD_AUDIT_LOG";
    
    /**
     * Inicializar sistema de auditoria
     */
    public void inicializarSistema() throws Exception {
        criarTabelaAuditoria();
    }
    
    /**
     * Criar tabela de auditoria
     */
    private void criarTabelaAuditoria() throws Exception {
        String sql = """
            CREATE TABLE IF NOT EXISTS AD_AUDIT_LOG (
                ID NUMBER PRIMARY KEY,
                ENTIDADE VARCHAR2(50) NOT NULL,
                ENTIDADE_ID NUMBER NOT NULL,
                OPERACAO VARCHAR2(20) NOT NULL,
                USUARIO_ID NUMBER NOT NULL,
                DT_OPERACAO DATE DEFAULT SYSDATE,
                VALORES_ANTIGOS CLOB,
                VALORES_NOVOS CLOB,
                CAMPO_ALTERADO VARCHAR2(100),
                VALOR_ANTIGO VARCHAR2(500),
                VALOR_NOVO VARCHAR2(500),
                IP_ADDRESS VARCHAR2(50),
                SESSION_ID VARCHAR2(100)
            )
            """;
        
        tabelaManager.facade.getQueryExecutor().executeUpdate(sql);
        
        // Criar índices para performance
        String sqlIndex1 = "CREATE INDEX IDX_AUDIT_ENTIDADE ON " + TABELA_AUDIT + " (ENTIDADE, ENTIDADE_ID)";
        String sqlIndex2 = "CREATE INDEX IDX_AUDIT_USUARIO ON " + TABELA_AUDIT + " (USUARIO_ID)";
        String sqlIndex3 = "CREATE INDEX IDX_AUDIT_DATA ON " + TABELA_AUDIT + " (DT_OPERACAO)";
        
        tabelaManager.facade.getQueryExecutor().executeUpdate(sqlIndex1);
        tabelaManager.facade.getQueryExecutor().executeUpdate(sqlIndex2);
        tabelaManager.facade.getQueryExecutor().executeUpdate(sqlIndex3);
    }
    
    /**
     * Registrar operação de auditoria
     */
    public void registrarOperacao(String entidade, BigDecimal entidadeId, String operacao,
                                 BigDecimal usuarioId, String valoresAntigos, String valoresNovos,
                                 String campoAlterado, String valorAntigo, String valorNovo,
                                 String ipAddress, String sessionId) throws Exception {
        
        DynamicVO audit = tabelaManager.criarEntidade(TABELA_AUDIT);
        
        audit.setProperty("ENTIDADE", entidade);
        audit.setProperty("ENTIDADE_ID", entidadeId);
        audit.setProperty("OPERACAO", operacao);
        audit.setProperty("USUARIO_ID", usuarioId);
        audit.setProperty("VALORES_ANTIGOS", valoresAntigos);
        audit.setProperty("VALORES_NOVOS", valoresNovos);
        audit.setProperty("CAMPO_ALTERADO", campoAlterado);
        audit.setProperty("VALOR_ANTIGO", valorAntigo);
        audit.setProperty("VALOR_NOVO", valorNovo);
        audit.setProperty("IP_ADDRESS", ipAddress);
        audit.setProperty("SESSION_ID", sessionId);
        
        tabelaManager.salvarEntidade(TABELA_AUDIT, audit);
    }
    
    /**
     * Buscar histórico de alterações
     */
    public List<DynamicVO> buscarHistoricoAlteracoes(String entidade, BigDecimal entidadeId) throws Exception {
        String sql = """
            SELECT * FROM AD_AUDIT_LOG 
            WHERE ENTIDADE = ? AND ENTIDADE_ID = ? 
            ORDER BY DT_OPERACAO DESC
            """;
        
        return tabelaManager.facade.getQueryExecutor().executeQuery(sql, entidade, entidadeId);
    }
    
    /**
     * Buscar operações por usuário
     */
    public List<DynamicVO> buscarOperacoesPorUsuario(BigDecimal usuarioId, Date dataInicio, Date dataFim) throws Exception {
        String sql = """
            SELECT * FROM AD_AUDIT_LOG 
            WHERE USUARIO_ID = ? 
            AND DT_OPERACAO BETWEEN ? AND ?
            ORDER BY DT_OPERACAO DESC
            """;
        
        return tabelaManager.facade.getQueryExecutor().executeQuery(sql, usuarioId, dataInicio, dataFim);
    }
    
    /**
     * Gerar relatório de auditoria
     */
    public String gerarRelatorioAuditoria(Date dataInicio, Date dataFim) throws Exception {
        String sql = """
            SELECT 
                ENTIDADE,
                OPERACAO,
                COUNT(*) as QTD_OPERACOES,
                COUNT(DISTINCT USUARIO_ID) as QTD_USUARIOS
            FROM AD_AUDIT_LOG 
            WHERE DT_OPERACAO BETWEEN ? AND ?
            GROUP BY ENTIDADE, OPERACAO
            ORDER BY ENTIDADE, OPERACAO
            """;
        
        List<DynamicVO> resultado = tabelaManager.facade.getQueryExecutor().executeQuery(sql, dataInicio, dataFim);
        
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("RELATÓRIO DE AUDITORIA\n");
        relatorio.append("======================\n\n");
        relatorio.append(String.format("Período: %s a %s\n\n", dataInicio, dataFim));
        
        relatorio.append(String.format("%-20s %-15s %-15s %-15s\n", 
                                     "ENTIDADE", "OPERACAO", "QTD_OPERACOES", "QTD_USUARIOS"));
        relatorio.append("-".repeat(70)).append("\n");
        
        for (DynamicVO linha : resultado) {
            String entidade = linha.asString("ENTIDADE");
            String operacao = linha.asString("OPERACAO");
            BigDecimal qtdOperacoes = linha.asBigDecimal("QTD_OPERACOES");
            BigDecimal qtdUsuarios = linha.asBigDecimal("QTD_USUARIOS");
            
            relatorio.append(String.format("%-20s %-15s %-15s %-15s\n",
                                         entidade, operacao, qtdOperacoes, qtdUsuarios));
        }
        
        return relatorio.toString();
    }
}
```

## 🎯 **Boas Práticas do Dicionário de Dados**

### **1. Estrutura e Organização**
- **Nomenclatura Consistente**: Use padrões de nomenclatura
- **Documentação**: Documente todas as tabelas e campos
- **Versionamento**: Controle de versão das estruturas
- **Backup**: Faça backup das estruturas

### **2. Performance**
- **Índices Adequados**: Crie índices para consultas frequentes
- **Particionamento**: Use particionamento para tabelas grandes
- **Estatísticas**: Mantenha estatísticas atualizadas
- **Monitoramento**: Monitore performance das consultas

### **3. Segurança**
- **Controle de Acesso**: Implemente controle de acesso
- **Auditoria**: Registre todas as alterações
- **Validação**: Valide dados antes de inserir
- **Sanitização**: Limpe dados de entrada

### **4. Manutenibilidade**
- **Padrões**: Use padrões consistentes
- **Modularidade**: Organize em módulos
- **Testes**: Implemente testes para estruturas
- **Documentação**: Mantenha documentação atualizada

## 🎊 **Conclusão**

O Dicionário de Dados Avançado demonstra:

- **✅ Arquitetura Robusta**: Estruturas bem organizadas
- **✅ Funcionalidades Avançadas**: Metadados, relacionamentos e campos calculados
- **✅ Performance**: Índices e otimizações adequadas
- **✅ Segurança**: Auditoria e controle de acesso
- **✅ Manutenibilidade**: Código bem estruturado e documentado
- **✅ Escalabilidade**: Suporte a crescimento

### **Benefícios:**
- **Flexibilidade**: Estruturas adaptáveis
- **Performance**: Otimizado para consultas
- **Segurança**: Controle e auditoria completos
- **Manutenibilidade**: Fácil de manter e evoluir
- **Confiabilidade**: Estruturas robustas e testadas

---

*Este documento apresenta implementações avançadas do Dicionário de Dados no Sankhya, fornecendo padrões enterprise para desenvolvimento de estruturas de dados robustas e escaláveis.*
