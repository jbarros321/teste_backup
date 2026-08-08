# 🎯 Padrões de Botões de Ação Sankhya

## 🎯 Visão Geral

Este documento apresenta padrões de desenvolvimento para botões de ação no Sankhya, extraídos do código fonte SankhyaW 4.8 e melhores práticas de desenvolvimento.

## 🏗️ **Padrões Arquiteturais**

### **1. Padrão Base para Botões de Ação**

```java
package br.com.empresa.padroes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;

import java.math.BigDecimal;

/**
 * Padrão base para botões de ação
 */
public abstract class BaseActionButton implements AcaoRotinaJava {
    
    protected EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    
    @Override
    public final void doAction(ContextoAcao contexto) throws Exception {
        try {
            // Validações iniciais
            validarContexto(contexto);
            
            // Processar ação
            processarAcao(contexto);
            
            // Log de sucesso
            logarSucesso(contexto);
            
        } catch (Exception e) {
            // Log de erro
            logarErro(contexto, e);
            
            // Re-lançar exceção
            throw e;
        }
    }
    
    /**
     * Validar contexto da ação
     */
    protected void validarContexto(ContextoAcao contexto) throws Exception {
        if (contexto == null) {
            throw new Exception("Contexto da ação é obrigatório");
        }
        
        Registro[] linhas = contexto.getLinhas();
        if (linhas == null || linhas.length == 0) {
            throw new Exception("Nenhum registro selecionado");
        }
    }
    
    /**
     * Processar a ação específica (implementado pelas subclasses)
     */
    protected abstract void processarAcao(ContextoAcao contexto) throws Exception;
    
    /**
     * Log de sucesso
     */
    protected void logarSucesso(ContextoAcao contexto) {
        System.out.println("Ação executada com sucesso: " + this.getClass().getSimpleName());
    }
    
    /**
     * Log de erro
     */
    protected void logarErro(ContextoAcao contexto, Exception e) {
        System.err.println("Erro na ação " + this.getClass().getSimpleName() + ": " + e.getMessage());
        e.printStackTrace();
    }
}
```

### **2. Padrão para Ações com Validação**

```java
package br.com.empresa.padroes;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Padrão para ações com validação de dados
 */
public abstract class ValidatedActionButton extends BaseActionButton {
    
    @Override
    protected void processarAcao(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();
        
        int processados = 0;
        int erros = 0;
        
        for (Registro linha : linhas) {
            try {
                // Validar registro
                validarRegistro(linha);
                
                // Processar registro
                processarRegistro(linha, contexto);
                
                processados++;
                
            } catch (Exception e) {
                erros++;
                contexto.mostraErro("Erro ao processar registro: " + e.getMessage());
            }
        }
        
        // Mensagem de resultado
        contexto.setMensagemRetorno(
            String.format("Processamento concluído: %d processados, %d erros", processados, erros)
        );
    }
    
    /**
     * Validar registro individual
     */
    protected abstract void validarRegistro(Registro linha) throws Exception;
    
    /**
     * Processar registro individual
     */
    protected abstract void processarRegistro(Registro linha, ContextoAcao contexto) throws Exception;
}
```

### **3. Padrão para Ações com Transação**

```java
package br.com.empresa.padroes;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;

/**
 * Padrão para ações que requerem controle de transação
 */
public abstract class TransactionalActionButton extends BaseActionButton {
    
    @Override
    protected void processarAcao(ContextoAcao contexto) throws Exception {
        // Iniciar transação
        facade.beginTransaction();
        
        try {
            // Processar ação
            processarAcaoComTransacao(contexto);
            
            // Commit da transação
            facade.commitTransaction();
            
        } catch (Exception e) {
            // Rollback da transação
            facade.rollbackTransaction();
            
            // Re-lançar exceção
            throw e;
        }
    }
    
    /**
     * Processar ação dentro de transação
     */
    protected abstract void processarAcaoComTransacao(ContextoAcao contexto) throws Exception;
}
```

## 🔄 **Padrões de Processamento**

### **1. Padrão Batch Processing**

```java
package br.com.empresa.padroes;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Padrão para processamento em lotes
 */
public abstract class BatchProcessingActionButton extends BaseActionButton {
    
    private static final int DEFAULT_BATCH_SIZE = 100;
    
    @Override
    protected void processarAcao(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();
        
        // Dividir em lotes
        List<List<Registro>> lotes = dividirEmLotes(linhas, getBatchSize());
        
        int totalProcessados = 0;
        int totalErros = 0;
        
        for (int i = 0; i < lotes.size(); i++) {
            List<Registro> lote = lotes.get(i);
            
            try {
                System.out.println("Processando lote " + (i + 1) + " de " + lotes.size());
                
                int processados = processarLote(lote, contexto);
                totalProcessados += processados;
                
            } catch (Exception e) {
                totalErros += lote.size();
                contexto.mostraErro("Erro no lote " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        contexto.setMensagemRetorno(
            String.format("Processamento em lotes concluído: %d processados, %d erros", 
                         totalProcessados, totalErros)
        );
    }
    
    /**
     * Dividir registros em lotes
     */
    private List<List<Registro>> dividirEmLotes(Registro[] linhas, int tamanhoLote) {
        List<List<Registro>> lotes = new ArrayList<>();
        
        for (int i = 0; i < linhas.length; i += tamanhoLote) {
            List<Registro> lote = new ArrayList<>();
            
            for (int j = i; j < Math.min(i + tamanhoLote, linhas.length); j++) {
                lote.add(linhas[j]);
            }
            
            lotes.add(lote);
        }
        
        return lotes;
    }
    
    /**
     * Processar lote de registros
     */
    protected abstract int processarLote(List<Registro> lote, ContextoAcao contexto) throws Exception;
    
    /**
     * Obter tamanho do lote
     */
    protected int getBatchSize() {
        return DEFAULT_BATCH_SIZE;
    }
}
```

### **2. Padrão Chain of Responsibility**

```java
package br.com.empresa.padroes;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.util.ArrayList;
import java.util.List;

/**
 * Padrão Chain of Responsibility para validações
 */
public abstract class ChainValidationActionButton extends BaseActionButton {
    
    private List<ValidationHandler> validationChain = new ArrayList<>();
    
    public ChainValidationActionButton() {
        inicializarChain();
    }
    
    @Override
    protected void processarAcao(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();
        
        for (Registro linha : linhas) {
            // Executar chain de validações
            for (ValidationHandler handler : validationChain) {
                handler.validate(linha, contexto);
            }
            
            // Processar após validações
            processarRegistroValidado(linha, contexto);
        }
    }
    
    /**
     * Inicializar chain de validações
     */
    protected abstract void inicializarChain();
    
    /**
     * Adicionar handler de validação
     */
    protected void addValidationHandler(ValidationHandler handler) {
        validationChain.add(handler);
    }
    
    /**
     * Processar registro após validações
     */
    protected abstract void processarRegistroValidado(Registro linha, ContextoAcao contexto) throws Exception;
    
    /**
     * Interface para handlers de validação
     */
    public interface ValidationHandler {
        void validate(Registro linha, ContextoAcao contexto) throws Exception;
    }
}
```

## 📊 **Padrões de Relatórios**

### **1. Padrão Template Method para Relatórios**

```java
package br.com.empresa.padroes;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Padrão Template Method para geração de relatórios
 */
public abstract class ReportActionButton extends BaseActionButton {
    
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    @Override
    protected void processarAcao(ContextoAcao contexto) throws Exception {
        // Obter parâmetros
        Date dataInicio = obterDataInicio(contexto);
        Date dataFim = obterDataFim(contexto);
        
        // Validar parâmetros
        validarParametros(dataInicio, dataFim);
        
        // Gerar relatório
        String relatorio = gerarRelatorio(dataInicio, dataFim);
        
        // Processar relatório
        processarRelatorio(relatorio, contexto);
    }
    
    /**
     * Obter data de início
     */
    protected Date obterDataInicio(ContextoAcao contexto) {
        return (Date) contexto.getParam("dataInicio");
    }
    
    /**
     * Obter data de fim
     */
    protected Date obterDataFim(ContextoAcao contexto) {
        return (Date) contexto.getParam("dataFim");
    }
    
    /**
     * Validar parâmetros
     */
    protected void validarParametros(Date dataInicio, Date dataFim) throws Exception {
        if (dataInicio == null || dataFim == null) {
            throw new Exception("Data de início e fim são obrigatórias");
        }
        
        if (dataInicio.after(dataFim)) {
            throw new Exception("Data de início deve ser anterior à data de fim");
        }
    }
    
    /**
     * Gerar relatório (implementado pelas subclasses)
     */
    protected abstract String gerarRelatorio(Date dataInicio, Date dataFim) throws Exception;
    
    /**
     * Processar relatório gerado
     */
    protected void processarRelatorio(String relatorio, ContextoAcao contexto) throws Exception {
        // Exibir relatório
        contexto.setMensagemRetorno(relatorio);
        
        // Salvar relatório
        salvarRelatorio(relatorio);
    }
    
    /**
     * Salvar relatório
     */
    protected abstract void salvarRelatorio(String relatorio) throws Exception;
}
```

## 🔧 **Padrões de Configuração**

### **1. Padrão Strategy para Configurações**

```java
package br.com.empresa.padroes;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

/**
 * Padrão Strategy para diferentes tipos de processamento
 */
public abstract class StrategyActionButton extends BaseActionButton {
    
    private ProcessingStrategy strategy;
    
    @Override
    protected void processarAcao(ContextoAcao contexto) throws Exception {
        // Determinar estratégia baseada no contexto
        strategy = determinarEstrategia(contexto);
        
        // Executar estratégia
        strategy.process(contexto);
    }
    
    /**
     * Determinar estratégia de processamento
     */
    protected abstract ProcessingStrategy determinarEstrategia(ContextoAcao contexto);
    
    /**
     * Interface para estratégias de processamento
     */
    public interface ProcessingStrategy {
        void process(ContextoAcao contexto) throws Exception;
    }
}
```

### **2. Padrão Builder para Configurações Complexas**

```java
package br.com.empresa.padroes;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Padrão Builder para configurações de ação
 */
public abstract class ConfigurableActionButton extends BaseActionButton {
    
    protected ActionConfiguration config;
    
    @Override
    protected void processarAcao(ContextoAcao contexto) throws Exception {
        // Construir configuração
        config = buildConfiguration(contexto);
        
        // Validar configuração
        validarConfiguracao(config);
        
        // Executar ação com configuração
        executarComConfiguracao(contexto, config);
    }
    
    /**
     * Construir configuração
     */
    protected abstract ActionConfiguration buildConfiguration(ContextoAcao contexto);
    
    /**
     * Validar configuração
     */
    protected void validarConfiguracao(ActionConfiguration config) throws Exception {
        if (config == null) {
            throw new Exception("Configuração é obrigatória");
        }
        
        config.validate();
    }
    
    /**
     * Executar ação com configuração
     */
    protected abstract void executarComConfiguracao(ContextoAcao contexto, ActionConfiguration config) throws Exception;
    
    /**
     * Classe para configuração da ação
     */
    public static class ActionConfiguration {
        private Map<String, Object> parameters = new HashMap<>();
        private Date dataInicio;
        private Date dataFim;
        private boolean validarDados = true;
        private boolean salvarLog = true;
        private int tamanhoLote = 100;
        
        public void addParameter(String key, Object value) {
            parameters.put(key, value);
        }
        
        public Object getParameter(String key) {
            return parameters.get(key);
        }
        
        public void validate() throws Exception {
            if (dataInicio != null && dataFim != null && dataInicio.after(dataFim)) {
                throw new Exception("Data de início deve ser anterior à data de fim");
            }
            
            if (tamanhoLote <= 0) {
                throw new Exception("Tamanho do lote deve ser maior que zero");
            }
        }
        
        // Getters e setters
        public Date getDataInicio() { return dataInicio; }
        public void setDataInicio(Date dataInicio) { this.dataInicio = dataInicio; }
        
        public Date getDataFim() { return dataFim; }
        public void setDataFim(Date dataFim) { this.dataFim = dataFim; }
        
        public boolean isValidarDados() { return validarDados; }
        public void setValidarDados(boolean validarDados) { this.validarDados = validarDados; }
        
        public boolean isSalvarLog() { return salvarLog; }
        public void setSalvarLog(boolean salvarLog) { this.salvarLog = salvarLog; }
        
        public int getTamanhoLote() { return tamanhoLote; }
        public void setTamanhoLote(int tamanhoLote) { this.tamanhoLote = tamanhoLote; }
    }
}
```

## 🎯 **Padrões de Logging e Monitoramento**

### **1. Padrão Observer para Logging**

```java
package br.com.empresa.padroes;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;

import java.util.ArrayList;
import java.util.List;

/**
 * Padrão Observer para logging de ações
 */
public abstract class ObservableActionButton extends BaseActionButton {
    
    private List<ActionObserver> observers = new ArrayList<>();
    
    @Override
    protected void processarAcao(ContextoAcao contexto) throws Exception {
        // Notificar início
        notificarInicio(contexto);
        
        try {
            // Processar ação
            processarAcaoObservada(contexto);
            
            // Notificar sucesso
            notificarSucesso(contexto);
            
        } catch (Exception e) {
            // Notificar erro
            notificarErro(contexto, e);
            throw e;
        }
    }
    
    /**
     * Adicionar observador
     */
    public void addObserver(ActionObserver observer) {
        observers.add(observer);
    }
    
    /**
     * Remover observador
     */
    public void removeObserver(ActionObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Notificar início da ação
     */
    protected void notificarInicio(ContextoAcao contexto) {
        for (ActionObserver observer : observers) {
            observer.onActionStart(contexto);
        }
    }
    
    /**
     * Notificar sucesso da ação
     */
    protected void notificarSucesso(ContextoAcao contexto) {
        for (ActionObserver observer : observers) {
            observer.onActionSuccess(contexto);
        }
    }
    
    /**
     * Notificar erro da ação
     */
    protected void notificarErro(ContextoAcao contexto, Exception e) {
        for (ActionObserver observer : observers) {
            observer.onActionError(contexto, e);
        }
    }
    
    /**
     * Processar ação observada (implementado pelas subclasses)
     */
    protected abstract void processarAcaoObservada(ContextoAcao contexto) throws Exception;
    
    /**
     * Interface para observadores
     */
    public interface ActionObserver {
        void onActionStart(ContextoAcao contexto);
        void onActionSuccess(ContextoAcao contexto);
        void onActionError(ContextoAcao contexto, Exception e);
    }
}
```

## 🎯 **Exemplo de Implementação com Padrões**

### **Botão de Ação usando Múltiplos Padrões**

```java
package br.com.empresa.exemplos;

import br.com.empresa.padroes.*;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.math.BigDecimal;
import java.util.List;

/**
 * Exemplo de botão de ação usando múltiplos padrões
 */
public class ProcessamentoVendasAction extends ObservableActionButton 
                                      implements ChainValidationActionButton {
    
    private List<ValidationHandler> validationChain = new ArrayList<>();
    
    public ProcessamentoVendasAction() {
        inicializarChain();
        inicializarObservadores();
    }
    
    @Override
    protected void inicializarChain() {
        addValidationHandler(new StatusValidationHandler());
        addValidationHandler(new ValorValidationHandler());
        addValidationHandler(new ClienteValidationHandler());
    }
    
    private void inicializarObservadores() {
        addObserver(new LoggingObserver());
        addObserver(new MetricsObserver());
    }
    
    @Override
    protected void processarAcaoObservada(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();
        
        for (Registro linha : linhas) {
            // Executar chain de validações
            for (ValidationHandler handler : validationChain) {
                handler.validate(linha, contexto);
            }
            
            // Processar após validações
            processarVenda(linha, contexto);
        }
    }
    
    private void processarVenda(Registro linha, ContextoAcao contexto) throws Exception {
        BigDecimal nunota = linha.getField("NUNOTA");
        
        // Lógica de processamento da venda
        System.out.println("Processando venda: " + nunota);
    }
    
    // Handlers de validação
    private class StatusValidationHandler implements ValidationHandler {
        @Override
        public void validate(Registro linha, ContextoAcao contexto) throws Exception {
            String status = linha.getField("STATUSNOTA");
            if (!"L".equals(status)) {
                throw new Exception("Venda deve estar liberada para processamento");
            }
        }
    }
    
    private class ValorValidationHandler implements ValidationHandler {
        @Override
        public void validate(Registro linha, ContextoAcao contexto) throws Exception {
            BigDecimal valor = linha.getField("VLRNOTA");
            if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
                throw new Exception("Valor da venda deve ser maior que zero");
            }
        }
    }
    
    private class ClienteValidationHandler implements ValidationHandler {
        @Override
        public void validate(Registro linha, ContextoAcao contexto) throws Exception {
            BigDecimal codparc = linha.getField("CODPARC");
            if (codparc == null) {
                throw new Exception("Cliente é obrigatório");
            }
        }
    }
    
    // Observadores
    private class LoggingObserver implements ActionObserver {
        @Override
        public void onActionStart(ContextoAcao contexto) {
            System.out.println("Iniciando processamento de vendas");
        }
        
        @Override
        public void onActionSuccess(ContextoAcao contexto) {
            System.out.println("Processamento de vendas concluído com sucesso");
        }
        
        @Override
        public void onActionError(ContextoAcao contexto, Exception e) {
            System.err.println("Erro no processamento de vendas: " + e.getMessage());
        }
    }
    
    private class MetricsObserver implements ActionObserver {
        @Override
        public void onActionStart(ContextoAcao contexto) {
            // Registrar métricas de início
        }
        
        @Override
        public void onActionSuccess(ContextoAcao contexto) {
            // Registrar métricas de sucesso
        }
        
        @Override
        public void onActionError(ContextoAcao contexto, Exception e) {
            // Registrar métricas de erro
        }
    }
}
```

## 🎯 **Boas Práticas dos Padrões**

### **1. Escolha do Padrão**
- **BaseActionButton**: Para ações simples
- **ValidatedActionButton**: Para ações com validação
- **TransactionalActionButton**: Para ações com transação
- **BatchProcessingActionButton**: Para processamento em lotes
- **ReportActionButton**: Para geração de relatórios

### **2. Combinação de Padrões**
- **Chain + Observer**: Validação + Monitoramento
- **Strategy + Builder**: Flexibilidade + Configuração
- **Template + Factory**: Estrutura + Criação

### **3. Manutenibilidade**
- **Código Reutilizável**: Padrões bem definidos
- **Testabilidade**: Fácil de testar
- **Extensibilidade**: Fácil de estender
- **Documentação**: Bem documentado

## 🎊 **Conclusão**

Os padrões de botões de ação demonstram:

- **✅ Arquitetura Limpa**: Padrões bem definidos
- **✅ Reutilização**: Código reutilizável
- **✅ Manutenibilidade**: Fácil manutenção
- **✅ Testabilidade**: Fácil de testar
- **✅ Extensibilidade**: Fácil de estender
- **✅ Flexibilidade**: Adaptável a diferentes cenários

### **Benefícios:**
- **Qualidade**: Código de alta qualidade
- **Produtividade**: Desenvolvimento mais rápido
- **Consistência**: Padrões consistentes
- **Escalabilidade**: Suporte a crescimento
- **Confiabilidade**: Código confiável

---

*Este documento apresenta padrões de desenvolvimento para botões de ação no Sankhya, fornecendo estruturas reutilizáveis e boas práticas de desenvolvimento.*
