# 🔘 Botões de Ação Sankhya - Guia Completo

## 🎯 Visão Geral

Os **Botões de Ação** são uma ferramenta essencial de customização da plataforma Sankhya-Om, permitindo a execução de tarefas específicas de maneira simples e rápida. Eles podem ser configurados no **Construtor de Telas** e no **Dicionário de Dados** através da aba "Ações".

## 🏗️ Arquitetura dos Botões de Ação

### **Componentes Principais**
- **Interface de Configuração**: Construtor de Telas e Dicionário de Dados
- **Tipos de Rotinas**: 4 tipos principais de execução
- **Sistema de Parâmetros**: Passagem de dados entre telas
- **Controle de Transações**: Gerenciamento de operações de banco
- **Validações**: Controle de permissões e regras de negócio

### **Fluxo de Execução**
```
┌─────────────────────────────────────────────────────────────┐
│                    INTERFACE DO USUÁRIO                   │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Botão de      │ │   Parâmetros    │ │   Validações    │ │
│  │   Ação          │ │   de Entrada    │ │   de Segurança  │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    SISTEMA DE ROTINAS                     │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Lançador      │ │   Banco de      │ │   Javascript    │ │
│  │   (Launcher)    │ │   Dados         │ │   (Script)      │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Java          │ │   Transação     │ │   Validações    │ │
│  │   (Class)       │ │   Manual        │ │   de Negócio    │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    EXECUÇÃO E RESULTADO                   │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Processamento │ │   Atualização   │ │   Feedback      │ │
│  │   da Rotina     │ │   da Interface  │ │   ao Usuário    │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🛠️ Tipos de Rotinas

### **1. Rotina Lançador (Launcher)**
- **Propósito**: Lançar telas do sistema
- **Uso**: Navegação entre telas
- **Configuração**: Definição de tela de destino e parâmetros
- **Exemplo**: Abrir tela de pedidos com filtro específico

### **2. Rotina Banco de Dados (Stored Procedure)**
- **Propósito**: Executar procedures SQL
- **Uso**: Operações complexas de banco de dados
- **Configuração**: Nome da procedure e parâmetros
- **Exemplo**: Atualizar status de pedidos em lote

### **3. Rotina Javascript (Script)**
- **Propósito**: Executar código JavaScript
- **Uso**: Validações e operações no front-end
- **Configuração**: Código JavaScript inline
- **Exemplo**: Validação de campos antes do envio

### **4. Rotina Java (Class)**
- **Propósito**: Executar classes Java
- **Uso**: Lógica de negócio complexa
- **Configuração**: Nome da classe e método
- **Exemplo**: Integração com sistemas externos

## 📋 Exemplo Didático: Sistema de Tarefas

### **Estrutura de Tabelas**

#### **Tabela Tarefa (TADTAR)**
```sql
CREATE TABLE TADTAR (
    CODTAREFA NUMBER(10) PRIMARY KEY,  -- PK auto numerado
    DESCRTAR VARCHAR2(255),            -- Descrição da tarefa
    NUNOTA NUMBER(10),                 -- Campo importado da TGFCAB
    STATUS VARCHAR2(1) DEFAULT 'A',    -- Status da tarefa
    DTCRIACAO DATE DEFAULT SYSDATE     -- Data de criação
);
```

#### **Tabela Etapa (TADETA)**
```sql
CREATE TABLE TADETA (
    CODTAREFA NUMBER(10),              -- FK para TADTAR
    CODETAPA NUMBER(10),               -- PK auto numerado
    DESCRICAO VARCHAR2(255),           -- Descrição da etapa
    STATUS VARCHAR2(1) DEFAULT 'P',    -- Status: P=Pendente, C=Concluída
    DTPRAZO DATE,                      -- Data prazo
    PRIMARY KEY (CODTAREFA, CODETAPA)
);
```

#### **Tabela Participante (TADPTA)**
```sql
CREATE TABLE TADPTA (
    CODTAREFA NUMBER(10),              -- FK para TADTAR
    CODPARC NUMBER(10),                -- FK para TGFPAR
    TIPO VARCHAR2(1),                  -- T: Titular, P: Participante
    PRIMARY KEY (CODTAREFA, CODPARC)
);
```

## 🔧 Configuração de Botões de Ação

### **1. Botão "Criar Tarefa" - Rotina Lançador**

#### **Configuração**
- **Tipo**: Lançador
- **Tela**: TADTAR (Tarefa)
- **Parâmetros**: Nenhum
- **Condições**: Sempre visível

#### **Funcionalidade**
```javascript
// Código JavaScript para o botão
function criarTarefa() {
    try {
        // Abrir tela de criação de tarefa
        openLevel('TADTAR', 'INSERT');
        
        // Mostrar mensagem de sucesso
        showMessage('Tela de criação de tarefa aberta');
        
    } catch (error) {
        showError('Erro ao abrir tela: ' + error.message);
    }
}
```

### **2. Botão "Finalizar Tarefa" - Rotina Banco de Dados**

#### **Configuração**
- **Tipo**: Banco de Dados
- **Procedure**: STP_FINALIZAR_TAREFA
- **Parâmetros**: CODTAREFA (do registro atual)
- **Condições**: STATUS = 'A' (tarefa ativa)

#### **Procedure SQL**
```sql
CREATE OR REPLACE PROCEDURE STP_FINALIZAR_TAREFA (
    P_CODTAREFA NUMBER,
    P_MENSAGEM OUT VARCHAR2
) AS
    P_COUNT_ETAPAS_PENDENTES NUMBER;
BEGIN
    -- Verificar se há etapas pendentes
    SELECT COUNT(*) INTO P_COUNT_ETAPAS_PENDENTES
    FROM TADETA
    WHERE CODTAREFA = P_CODTAREFA
    AND STATUS = 'P';
    
    IF P_COUNT_ETAPAS_PENDENTES > 0 THEN
        P_MENSAGEM := 'Não é possível finalizar tarefa com etapas pendentes!';
        RAISE_APPLICATION_ERROR(-20001, P_MENSAGEM);
    END IF;
    
    -- Finalizar tarefa
    UPDATE TADTAR
    SET STATUS = 'F',
        DTFINALIZACAO = SYSDATE
    WHERE CODTAREFA = P_CODTAREFA;
    
    -- Finalizar todas as etapas pendentes
    UPDATE TADETA
    SET STATUS = 'C',
        DTFINALIZACAO = SYSDATE
    WHERE CODTAREFA = P_CODTAREFA
    AND STATUS = 'P';
    
    P_MENSAGEM := 'Tarefa finalizada com sucesso!';
    
EXCEPTION
    WHEN OTHERS THEN
        P_MENSAGEM := 'Erro ao finalizar tarefa: ' || SQLERRM;
        RAISE;
END;
```

### **3. Botão "Validar Campos" - Rotina Javascript**

#### **Configuração**
- **Tipo**: Javascript
- **Código**: Validação de campos obrigatórios
- **Condições**: Sempre visível

#### **Código JavaScript**
```javascript
function validarCampos() {
    try {
        var registro = getCurrentRecord();
        
        if (!registro) {
            throw new Error('Nenhum registro selecionado');
        }
        
        // Validar descrição
        var descricao = registro.getProperty('DESCRTAR');
        if (!descricao || descricao.trim() === '') {
            throw new Error('Descrição da tarefa é obrigatória');
        }
        
        // Validar data de prazo
        var dataPrazo = registro.getProperty('DTPRAZO');
        if (!dataPrazo) {
            throw new Error('Data de prazo é obrigatória');
        }
        
        // Validar se data não é passada
        var hoje = new Date();
        var prazo = new Date(dataPrazo);
        if (prazo < hoje) {
            throw new Error('Data de prazo não pode ser no passado');
        }
        
        // Mostrar mensagem de sucesso
        showMessage('Validação concluída com sucesso!');
        
        // Atualizar tela
        refreshCurrentScreen();
        
    } catch (error) {
        showError('Erro na validação: ' + error.message);
    }
}
```

### **4. Botão "Integrar com Sistema Externo" - Rotina Java**

#### **Configuração**
- **Tipo**: Java
- **Classe**: br.com.empresa.sankhya.action.IntegracaoExternaAction
- **Método**: executar
- **Parâmetros**: CODTAREFA (do registro atual)

#### **Classe Java**
```java
package br.com.empresa.sankhya.action;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.DynamicEntityManager;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

public class IntegracaoExternaAction {
    
    private static final String URL_API = "https://api.externa.com.br/tarefas";
    private static final String API_KEY = "sua-api-key-aqui";
    
    public String executar(DynamicVO tarefa) throws Exception {
        try {
            // Validar dados da tarefa
            validarTarefa(tarefa);
            
            // Preparar dados para envio
            String dados = prepararDados(tarefa);
            
            // Enviar para API externa
            String resposta = enviarParaAPI(dados);
            
            // Processar resposta
            processarResposta(resposta, tarefa);
            
            return "Integração executada com sucesso!";
            
        } catch (Exception e) {
            throw new Exception("Erro na integração: " + e.getMessage());
        }
    }
    
    private void validarTarefa(DynamicVO tarefa) throws Exception {
        String descricao = tarefa.asString("DESCRTAR");
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new Exception("Descrição da tarefa é obrigatória");
        }
        
        String status = tarefa.asString("STATUS");
        if (!"A".equals(status)) {
            throw new Exception("Apenas tarefas ativas podem ser integradas");
        }
    }
    
    private String prepararDados(DynamicVO tarefa) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(tarefa.asString("CODTAREFA")).append(",");
        json.append("\"descricao\":\"").append(tarefa.asString("DESCRTAR")).append("\",");
        json.append("\"status\":\"").append(tarefa.asString("STATUS")).append("\",");
        json.append("\"dataCriacao\":\"").append(tarefa.asString("DTCRIACAO")).append("\"");
        json.append("}");
        
        return json.toString();
    }
    
    private String enviarParaAPI(String dados) throws Exception {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL_API))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + API_KEY)
            .POST(HttpRequest.BodyPublishers.ofString(dados))
            .build();
        
        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new Exception("Erro na API: " + response.statusCode() + 
                " - " + response.body());
        }
        
        return response.body();
    }
    
    private void processarResposta(String resposta, DynamicVO tarefa) throws Exception {
        // Processar resposta da API externa
        if (resposta.contains("\"status\":\"success\"")) {
            // Atualizar status de integração
            DynamicEntityManager dem = EntityFacadeFactory.getDWFFacade().getDynamicEntityManager();
            tarefa.setProperty("STATUS_INTEGRACAO", "S");
            tarefa.setProperty("DT_INTEGRACAO", new java.util.Date());
            dem.update(tarefa);
        } else {
            throw new Exception("Falha na integração: " + resposta);
        }
    }
}
```

## 🔄 Transação Manual para Ações

### **Configuração de Transação Manual**

#### **Botão "Processar em Lote" - Com Transação Manual**
```java
package br.com.empresa.sankhya.action;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.DynamicEntityManager;
import java.util.List;

public class ProcessarLoteAction {
    
    public String executar(List<DynamicVO> tarefas) throws Exception {
        DynamicEntityManager dem = EntityFacadeFactory.getDWFFacade().getDynamicEntityManager();
        
        try {
            // Iniciar transação manual
            dem.beginTransaction();
            
            int sucessos = 0;
            int erros = 0;
            
            for (DynamicVO tarefa : tarefas) {
                try {
                    // Processar cada tarefa
                    processarTarefa(tarefa);
                    sucessos++;
                    
                } catch (Exception e) {
                    erros++;
                    System.err.println("Erro ao processar tarefa " + 
                        tarefa.asString("CODTAREFA") + ": " + e.getMessage());
                }
            }
            
            // Commit da transação
            dem.commitTransaction();
            
            return "Processamento concluído: " + sucessos + " sucessos, " + erros + " erros";
            
        } catch (Exception e) {
            // Rollback em caso de erro
            dem.rollbackTransaction();
            throw new Exception("Erro no processamento em lote: " + e.getMessage());
        }
    }
    
    private void processarTarefa(DynamicVO tarefa) throws Exception {
        // Lógica de processamento da tarefa
        String status = tarefa.asString("STATUS");
        
        if ("A".equals(status)) {
            // Atualizar status para processada
            tarefa.setProperty("STATUS", "P");
            tarefa.setProperty("DTPROCESSAMENTO", new java.util.Date());
            
            // Salvar alterações
            DynamicEntityManager dem = EntityFacadeFactory.getDWFFacade().getDynamicEntityManager();
            dem.update(tarefa);
        }
    }
}
```

## 🎯 Casos de Uso Avançados

### **1. Botão com Validação Condicional**

#### **Configuração**
- **Tipo**: Javascript
- **Condições**: STATUS = 'A' AND DTPRAZO >= SYSDATE
- **Código**: Validação e processamento

#### **Código JavaScript**
```javascript
function processarTarefaCondicional() {
    try {
        var registro = getCurrentRecord();
        
        if (!registro) {
            throw new Error('Nenhum registro selecionado');
        }
        
        // Validar condições
        var status = registro.getProperty('STATUS');
        var dataPrazo = registro.getProperty('DTPRAZO');
        
        if (status !== 'A') {
            throw new Error('Apenas tarefas ativas podem ser processadas');
        }
        
        if (new Date(dataPrazo) < new Date()) {
            throw new Error('Tarefa vencida não pode ser processada');
        }
        
        // Processar tarefa
        processarTarefa(registro);
        
        // Mostrar mensagem de sucesso
        showMessage('Tarefa processada com sucesso!');
        
        // Atualizar tela
        refreshCurrentScreen();
        
    } catch (error) {
        showError('Erro no processamento: ' + error.message);
    }
}

function processarTarefa(registro) {
    // Lógica de processamento
    registro.setProperty('STATUS', 'P');
    registro.setProperty('DTPROCESSAMENTO', new Date());
    
    // Salvar alterações
    saveRecord(registro);
}
```

### **2. Botão com Integração de Múltiplos Sistemas**

#### **Classe Java**
```java
package br.com.empresa.sankhya.action;

import br.com.sankhya.jape.vo.DynamicVO;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IntegracaoMultiplaAction {
    
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    
    public String executar(DynamicVO tarefa) throws Exception {
        try {
            // Executar integrações em paralelo
            CompletableFuture<String> sistema1 = CompletableFuture.supplyAsync(() -> {
                try {
                    return integrarSistema1(tarefa);
                } catch (Exception e) {
                    return "Erro Sistema 1: " + e.getMessage();
                }
            }, executor);
            
            CompletableFuture<String> sistema2 = CompletableFuture.supplyAsync(() -> {
                try {
                    return integrarSistema2(tarefa);
                } catch (Exception e) {
                    return "Erro Sistema 2: " + e.getMessage();
                }
            }, executor);
            
            CompletableFuture<String> sistema3 = CompletableFuture.supplyAsync(() -> {
                try {
                    return integrarSistema3(tarefa);
                } catch (Exception e) {
                    return "Erro Sistema 3: " + e.getMessage();
                }
            }, executor);
            
            // Aguardar todas as integrações
            CompletableFuture.allOf(sistema1, sistema2, sistema3).join();
            
            // Coletar resultados
            String resultado1 = sistema1.get();
            String resultado2 = sistema2.get();
            String resultado3 = sistema3.get();
            
            // Processar resultados
            processarResultados(resultado1, resultado2, resultado3, tarefa);
            
            return "Integrações executadas com sucesso!";
            
        } catch (Exception e) {
            throw new Exception("Erro nas integrações: " + e.getMessage());
        }
    }
    
    private String integrarSistema1(DynamicVO tarefa) throws Exception {
        // Implementar integração com sistema 1
        Thread.sleep(1000); // Simular processamento
        return "Sistema 1: OK";
    }
    
    private String integrarSistema2(DynamicVO tarefa) throws Exception {
        // Implementar integração com sistema 2
        Thread.sleep(1500); // Simular processamento
        return "Sistema 2: OK";
    }
    
    private String integrarSistema3(DynamicVO tarefa) throws Exception {
        // Implementar integração com sistema 3
        Thread.sleep(800); // Simular processamento
        return "Sistema 3: OK";
    }
    
    private void processarResultados(String resultado1, String resultado2, 
                                   String resultado3, DynamicVO tarefa) {
        // Processar resultados das integrações
        StringBuilder log = new StringBuilder();
        log.append("Integrações executadas:\n");
        log.append("- ").append(resultado1).append("\n");
        log.append("- ").append(resultado2).append("\n");
        log.append("- ").append(resultado3).append("\n");
        
        // Atualizar tarefa com log
        tarefa.setProperty("LOG_INTEGRACOES", log.toString());
        tarefa.setProperty("DT_INTEGRACOES", new java.util.Date());
        
        // Salvar alterações
        DynamicEntityManager dem = EntityFacadeFactory.getDWFFacade().getDynamicEntityManager();
        dem.update(tarefa);
    }
}
```

### **3. Botão com Notificação por Email**

#### **Procedure SQL**
```sql
CREATE OR REPLACE PROCEDURE STP_NOTIFICAR_TAREFA (
    P_CODTAREFA NUMBER,
    P_TIPO_NOTIFICACAO VARCHAR2,
    P_MENSAGEM OUT VARCHAR2
) AS
    P_EMAIL_DESTINATARIO VARCHAR2(255);
    P_NOME_TAREFA VARCHAR2(255);
    P_ASSUNTO VARCHAR2(255);
    P_CORPO_EMAIL CLOB;
    P_COUNT_PARTICIPANTES NUMBER;
BEGIN
    -- Obter dados da tarefa
    SELECT DESCRTAR INTO P_NOME_TAREFA
    FROM TADTAR
    WHERE CODTAREFA = P_CODTAREFA;
    
    -- Contar participantes
    SELECT COUNT(*) INTO P_COUNT_PARTICIPANTES
    FROM TADPTA
    WHERE CODTAREFA = P_CODTAREFA;
    
    -- Preparar email baseado no tipo
    CASE P_TIPO_NOTIFICACAO
        WHEN 'CRIACAO' THEN
            P_ASSUNTO := 'Nova Tarefa Criada - ' || P_CODTAREFA;
            P_CORPO_EMAIL := 'Uma nova tarefa foi criada: "' || P_NOME_TAREFA || 
                           '" com ' || P_COUNT_PARTICIPANTES || ' participante(s).';
        WHEN 'FINALIZACAO' THEN
            P_ASSUNTO := 'Tarefa Finalizada - ' || P_CODTAREFA;
            P_CORPO_EMAIL := 'A tarefa "' || P_NOME_TAREFA || 
                           '" foi finalizada com sucesso.';
        WHEN 'VENCIMENTO' THEN
            P_ASSUNTO := 'Tarefa Próxima do Vencimento - ' || P_CODTAREFA;
            P_CORPO_EMAIL := 'A tarefa "' || P_NOME_TAREFA || 
                           '" está próxima do vencimento.';
        ELSE
            P_ASSUNTO := 'Notificação de Tarefa - ' || P_CODTAREFA;
            P_CORPO_EMAIL := 'Notificação sobre a tarefa "' || P_NOME_TAREFA || '".';
    END CASE;
    
    -- Enviar email para todos os participantes
    FOR participante IN (
        SELECT p.CODPARC, par.EMAIL
        FROM TADPTA p
        INNER JOIN TGFPAR par ON p.CODPARC = par.CODPARC
        WHERE p.CODTAREFA = P_CODTAREFA
        AND par.EMAIL IS NOT NULL
    ) LOOP
        INSERT INTO FILA_EMAIL (
            EMAIL_DESTINATARIO, ASSUNTO, CORPO, DTENVIO, STATUS
        ) VALUES (
            participante.EMAIL, P_ASSUNTO, P_CORPO_EMAIL, SYSDATE, 'P'
        );
    END LOOP;
    
    -- Log da notificação
    INSERT INTO LOG_NOTIFICACOES (
        CODTAREFA, TIPO, ASSUNTO, DTENVIO, STATUS
    ) VALUES (
        P_CODTAREFA, P_TIPO_NOTIFICACAO, P_ASSUNTO, SYSDATE, 'ENVIADO'
    );
    
    P_MENSAGEM := 'Notificações enviadas com sucesso';
    
EXCEPTION
    WHEN OTHERS THEN
        P_MENSAGEM := 'Erro ao enviar notificações: ' || SQLERRM;
        -- Log do erro
        INSERT INTO LOG_ERROS (
            CODTAREFA, EVENTO, DTERRO, ERRO
        ) VALUES (
            P_CODTAREFA, 'NOTIFICACAO', SYSDATE, SQLERRM
        );
END;
```

## 🛠️ Boas Práticas

### **1. Design de Botões**
- **Nomenclatura**: Nomes claros e descritivos
- **Agrupamento**: Agrupar botões relacionados
- **Posicionamento**: Posicionar botões de forma lógica
- **Ícones**: Usar ícones apropriados
- **Cores**: Usar cores consistentes

### **2. Tratamento de Erros**
- **Validações**: Validar dados antes do processamento
- **Mensagens**: Mensagens de erro claras e úteis
- **Logs**: Registrar erros para auditoria
- **Recuperação**: Implementar mecanismos de recuperação
- **Rollback**: Usar transações para rollback

### **3. Performance**
- **Índices**: Usar índices adequados nas consultas
- **Transações**: Manter transações curtas
- **Cache**: Usar cache para dados frequentes
- **Otimização**: Otimizar consultas SQL
- **Monitoramento**: Monitorar performance dos botões

### **4. Segurança**
- **Validação**: Validar todos os dados de entrada
- **Autorização**: Verificar permissões do usuário
- **Auditoria**: Registrar todas as operações
- **Criptografia**: Criptografar dados sensíveis
- **Backup**: Manter backups regulares

## 🔍 Troubleshooting

### **Problemas Comuns**
- **Botão não aparece**: Verificar condições e permissões
- **Erro de execução**: Verificar logs e validações
- **Performance lenta**: Otimizar consultas e transações
- **Dados incorretos**: Verificar mapeamento de parâmetros
- **Timeout**: Otimizar operações longas

### **Soluções**
- **Logs**: Analisar logs de execução
- **Debug**: Usar ferramentas de debug
- **Testes**: Testar em ambiente isolado
- **Documentação**: Consultar documentação oficial
- **Suporte**: Contatar suporte técnico

## 🚀 Evolução e Tendências

### **Melhorias Contínuas**
- **Novos Tipos**: Novos tipos de rotinas
- **Melhor Performance**: Otimizações de performance
- **Integração**: Melhor integração com sistemas externos
- **Monitoramento**: Ferramentas de monitoramento avançadas
- **Documentação**: Documentação mais detalhada

### **Tendências Futuras**
- **Event Streaming**: Processamento de eventos em tempo real
- **Microserviços**: Arquitetura de microserviços
- **Cloud**: Execução em cloud
- **IA**: Integração com inteligência artificial
- **Blockchain**: Integração com blockchain

---

*Este documento foi criado com base na documentação oficial do Sankhya Developer sobre Botões de Ação e melhores práticas de desenvolvimento.*
