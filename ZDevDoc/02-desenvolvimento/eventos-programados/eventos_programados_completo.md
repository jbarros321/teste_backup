# 🎯 Eventos Programados Sankhya - Guia Completo

## 🎯 Visão Geral

Os **Eventos Programados** são uma funcionalidade avançada da plataforma Sankhya que permite a execução de rotinas de processamento disparadas por operações realizadas em entidades do sistema. Funcionam de forma análoga a um **listener de entidade**, executando código automaticamente quando determinadas condições são atendidas.

## 🏗️ Arquitetura dos Eventos Programados

### **Componentes Principais**
- **Event Listeners**: Ouvintes de eventos de entidades
- **Trigger System**: Sistema de disparo automático
- **Event Handlers**: Manipuladores de eventos
- **Condition Engine**: Motor de condições
- **Execution Engine**: Motor de execução

### **Fluxo de Execução**
```
┌─────────────────────────────────────────────────────────────┐
│                    OPERAÇÃO DO USUÁRIO                    │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Inserção      │ │   Atualização   │ │   Exclusão      │ │
│  │   de Dados      │ │   de Dados      │ │   de Dados      │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    SISTEMA DE EVENTOS                     │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Detecção      │ │   Validação     │ │   Disparo       │ │
│  │   de Evento     │ │   de Condições  │ │   de Rotina     │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    EXECUÇÃO DA ROTINA                     │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Before        │ │   After         │ │   Error         │ │
│  │   Event         │ │   Event         │ │   Handler       │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    RESULTADO E FEEDBACK                   │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Sucesso       │ │   Erro          │ │   Log           │ │
│  │   da Operação   │ │   da Operação   │ │   de Auditoria  │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🔧 Tipos de Eventos

### **1. Eventos de Entidade**
- **Before Insert**: Antes da inserção
- **After Insert**: Após a inserção
- **Before Update**: Antes da atualização
- **After Update**: Após a atualização
- **Before Delete**: Antes da exclusão
- **After Delete**: Após a exclusão

### **2. Eventos de Campo**
- **Field Change**: Mudança de valor de campo
- **Field Validation**: Validação de campo
- **Field Calculation**: Cálculo de campo

### **3. Eventos de Sistema**
- **Login**: Login de usuário
- **Logout**: Logout de usuário
- **Session Start**: Início de sessão
- **Session End**: Fim de sessão

## 📋 Configuração de Eventos Programados

### **1. Configuração Básica**
```xml
<!-- Exemplo de configuração de evento programado -->
<event-programmed>
    <name>ValidarTarefa</name>
    <description>Validar tarefa ao ser inserida</description>
    <entity>TADTAR</entity>
    <event-type>AFTER_INSERT</event-type>
    <condition>
        <field name="STATUS" operator="=" value="A"/>
    </condition>
    <routine>
        <type>database-procedure</type>
        <name>STP_VALIDAR_TAREFA_EVENTO</name>
        <parameters>
            <parameter name="P_CODTAREFA" source="new" field="CODTAREFA"/>
            <parameter name="P_CODUSU" source="user" field="CODUSU"/>
        </parameters>
    </routine>
    <active>true</active>
</event-programmed>
```

### **2. Configuração com Múltiplas Condições**
```xml
<event-programmed>
    <name>ProcessarTarefaCompleta</name>
    <description>Processar tarefa quando todas as etapas estiverem concluídas</description>
    <entity>TADETA</entity>
    <event-type>AFTER_UPDATE</event-type>
    <condition>
        <and>
            <field name="STATUS" operator="=" value="C"/>
            <field name="CODTAREFA" operator="IN" value="SELECT CODTAREFA FROM TADTAR WHERE STATUS = 'A'"/>
        </and>
    </condition>
    <routine>
        <type>java-class</type>
        <class>br.com.empresa.sankhya.event.ProcessarTarefaCompleta</class>
        <method>executar</method>
        <parameters>
            <parameter name="codTarefa" source="new" field="CODTAREFA"/>
        </parameters>
    </routine>
</event-programmed>
```

### **3. Configuração de Evento com Validação**
```xml
<event-programmed>
    <name>ValidarAntesInserir</name>
    <description>Validar dados antes de inserir tarefa</description>
    <entity>TADTAR</entity>
    <event-type>BEFORE_INSERT</event-type>
    <condition>
        <field name="DESCRTAR" operator="IS NOT NULL"/>
    </condition>
    <routine>
        <type>database-procedure</type>
        <name>STP_VALIDAR_TAREFA_BEFORE_INSERT</name>
        <parameters>
            <parameter name="P_DESCRTAR" source="new" field="DESCRTAR"/>
            <parameter name="P_DTPRAZO" source="new" field="DTPRAZO"/>
        </parameters>
        <on-error>ABORT_TRANSACTION</on-error>
    </routine>
</event-programmed>
```

## 💾 Procedures para Eventos Programados

### **1. Evento After Insert**
```sql
CREATE OR REPLACE PROCEDURE STP_VALIDAR_TAREFA_EVENTO (
    P_CODTAREFA NUMBER,
    P_CODUSU NUMBER,
    P_MENSAGEM OUT VARCHAR2
) AS
    P_COUNT_ETAPAS NUMBER;
    P_STATUS_TAREFA VARCHAR2(1);
BEGIN
    -- Obter status da tarefa
    SELECT STATUS INTO P_STATUS_TAREFA
    FROM TADTAR
    WHERE CODTAREFA = P_CODTAREFA;
    
    -- Verificar se é tarefa ativa
    IF P_STATUS_TAREFA = 'A' THEN
        -- Contar etapas da tarefa
        SELECT COUNT(*) INTO P_COUNT_ETAPAS
        FROM TADETA
        WHERE CODTAREFA = P_CODTAREFA;
        
        -- Se não tem etapas, criar etapa padrão
        IF P_COUNT_ETAPAS = 0 THEN
            INSERT INTO TADETA (
                CODTAREFA, CODETAPA, DESCRICAO, STATUS, DTPRAZO
            ) VALUES (
                P_CODTAREFA, 1, 'Etapa Inicial', 'P', 
                SYSDATE + 7
            );
            
            P_MENSAGEM := 'Etapa inicial criada automaticamente';
        ELSE
            P_MENSAGEM := 'Tarefa criada com ' || P_COUNT_ETAPAS || ' etapas';
        END IF;
        
        -- Log da operação
        INSERT INTO LOG_EVENTOS (
            CODTAREFA, EVENTO, DTEVENTO, USUARIO, MENSAGEM
        ) VALUES (
            P_CODTAREFA, 'AFTER_INSERT', SYSDATE, P_CODUSU, P_MENSAGEM
        );
        
    END IF;
    
EXCEPTION
    WHEN OTHERS THEN
        P_MENSAGEM := 'Erro no evento: ' || SQLERRM;
        -- Log do erro
        INSERT INTO LOG_ERROS (
            CODTAREFA, EVENTO, DTERRO, USUARIO, ERRO
        ) VALUES (
            P_CODTAREFA, 'AFTER_INSERT', SYSDATE, P_CODUSU, SQLERRM
        );
END;
```

### **2. Evento Before Update**
```sql
CREATE OR REPLACE PROCEDURE STP_VALIDAR_TAREFA_BEFORE_UPDATE (
    P_CODTAREFA NUMBER,
    P_STATUS_OLD VARCHAR2,
    P_STATUS_NEW VARCHAR2,
    P_CODUSU NUMBER,
    P_MENSAGEM OUT VARCHAR2
) AS
    P_COUNT_ETAPAS_PENDENTES NUMBER;
BEGIN
    -- Validar transição de status
    IF P_STATUS_OLD = 'F' AND P_STATUS_NEW != 'F' THEN
        P_MENSAGEM := 'Tarefa finalizada não pode ter status alterado!';
        RAISE_APPLICATION_ERROR(-20001, P_MENSAGEM);
    END IF;
    
    -- Se está sendo finalizada, verificar etapas
    IF P_STATUS_NEW = 'F' THEN
        SELECT COUNT(*) INTO P_COUNT_ETAPAS_PENDENTES
        FROM TADETA
        WHERE CODTAREFA = P_CODTAREFA
        AND STATUS = 'P';
        
        IF P_COUNT_ETAPAS_PENDENTES > 0 THEN
            P_MENSAGEM := 'Não é possível finalizar tarefa com etapas pendentes!';
            RAISE_APPLICATION_ERROR(-20002, P_MENSAGEM);
        END IF;
    END IF;
    
    -- Se está sendo validada, verificar se pode ser validada
    IF P_STATUS_NEW = 'V' AND P_STATUS_OLD != 'V' THEN
        -- Verificar se todas as etapas estão concluídas
        SELECT COUNT(*) INTO P_COUNT_ETAPAS_PENDENTES
        FROM TADETA
        WHERE CODTAREFA = P_CODTAREFA
        AND STATUS = 'P';
        
        IF P_COUNT_ETAPAS_PENDENTES > 0 THEN
            P_MENSAGEM := 'Não é possível validar tarefa com etapas pendentes!';
            RAISE_APPLICATION_ERROR(-20003, P_MENSAGEM);
        END IF;
    END IF;
    
    P_MENSAGEM := 'Validação concluída com sucesso';
    
EXCEPTION
    WHEN OTHERS THEN
        -- Log do erro
        INSERT INTO LOG_ERROS (
            CODTAREFA, EVENTO, DTERRO, USUARIO, ERRO
        ) VALUES (
            P_CODTAREFA, 'BEFORE_UPDATE', SYSDATE, P_CODUSU, SQLERRM
        );
        RAISE;
END;
```

### **3. Evento After Update**
```sql
CREATE OR REPLACE PROCEDURE STP_PROCESSAR_TAREFA_AFTER_UPDATE (
    P_CODTAREFA NUMBER,
    P_STATUS_OLD VARCHAR2,
    P_STATUS_NEW VARCHAR2,
    P_CODUSU NUMBER,
    P_MENSAGEM OUT VARCHAR2
) AS
    P_COUNT_ETAPAS_TOTAL NUMBER;
    P_COUNT_ETAPAS_CONCLUIDAS NUMBER;
    P_PERCENTUAL_CONCLUSAO NUMBER;
BEGIN
    -- Se foi finalizada, processar finalização
    IF P_STATUS_NEW = 'F' AND P_STATUS_OLD != 'F' THEN
        -- Finalizar todas as etapas pendentes
        UPDATE TADETA
        SET STATUS = 'C',
            DTFINALIZACAO = SYSDATE,
            USUFINALIZACAO = P_CODUSU
        WHERE CODTAREFA = P_CODTAREFA
        AND STATUS = 'P';
        
        -- Atualizar data de finalização da tarefa
        UPDATE TADTAR
        SET DTFINALIZACAO = SYSDATE,
            USUFINALIZACAO = P_CODUSU
        WHERE CODTAREFA = P_CODTAREFA;
        
        P_MENSAGEM := 'Tarefa finalizada com sucesso';
        
    -- Se foi validada, calcular percentual de conclusão
    ELSIF P_STATUS_NEW = 'V' AND P_STATUS_OLD != 'V' THEN
        -- Contar etapas
        SELECT COUNT(*) INTO P_COUNT_ETAPAS_TOTAL
        FROM TADETA
        WHERE CODTAREFA = P_CODTAREFA;
        
        SELECT COUNT(*) INTO P_COUNT_ETAPAS_CONCLUIDAS
        FROM TADETA
        WHERE CODTAREFA = P_CODTAREFA
        AND STATUS = 'C';
        
        -- Calcular percentual
        IF P_COUNT_ETAPAS_TOTAL > 0 THEN
            P_PERCENTUAL_CONCLUSAO := (P_COUNT_ETAPAS_CONCLUIDAS * 100) / P_COUNT_ETAPAS_TOTAL;
        ELSE
            P_PERCENTUAL_CONCLUSAO := 0;
        END IF;
        
        -- Atualizar percentual na tarefa
        UPDATE TADTAR
        SET PERCENTUAL_CONCLUSAO = P_PERCENTUAL_CONCLUSAO
        WHERE CODTAREFA = P_CODTAREFA;
        
        P_MENSAGEM := 'Percentual de conclusão calculado: ' || P_PERCENTUAL_CONCLUSAO || '%';
    END IF;
    
    -- Log da operação
    INSERT INTO LOG_EVENTOS (
        CODTAREFA, EVENTO, DTEVENTO, USUARIO, MENSAGEM, STATUS_OLD, STATUS_NEW
    ) VALUES (
        P_CODTAREFA, 'AFTER_UPDATE', SYSDATE, P_CODUSU, P_MENSAGEM, P_STATUS_OLD, P_STATUS_NEW
    );
    
EXCEPTION
    WHEN OTHERS THEN
        P_MENSAGEM := 'Erro no evento: ' || SQLERRM;
        -- Log do erro
        INSERT INTO LOG_ERROS (
            CODTAREFA, EVENTO, DTERRO, USUARIO, ERRO
        ) VALUES (
            P_CODTAREFA, 'AFTER_UPDATE', SYSDATE, P_CODUSU, SQLERRM
        );
END;
```

## ☕ Classes Java para Eventos Programados

### **1. Classe Base para Eventos**
```java
package br.com.empresa.sankhya.event;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.dwfdata.vo.TgfcabVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.DynamicEntityManager;

public abstract class BaseEvent {
    
    protected DynamicEntityManager dem;
    
    public BaseEvent() {
        this.dem = EntityFacadeFactory.getDWFFacade().getDynamicEntityManager();
    }
    
    /**
     * Método principal executado pelo evento
     */
    public abstract String executar(DynamicVO oldVO, DynamicVO newVO, String eventType);
    
    /**
     * Validar condições do evento
     */
    protected boolean validarCondicoes(DynamicVO oldVO, DynamicVO newVO, String eventType) {
        // Implementar validações específicas
        return true;
    }
    
    /**
     * Log da operação
     */
    protected void logOperacao(String entidade, String evento, String mensagem, String usuario) {
        try {
            DynamicVO log = dem.create("LOG_EVENTOS");
            log.setProperty("ENTIDADE", entidade);
            log.setProperty("EVENTO", evento);
            log.setProperty("DTEVENTO", new java.util.Date());
            log.setProperty("USUARIO", usuario);
            log.setProperty("MENSAGEM", mensagem);
            dem.insert(log);
        } catch (Exception e) {
            System.err.println("Erro ao registrar log: " + e.getMessage());
        }
    }
    
    /**
     * Log de erro
     */
    protected void logErro(String entidade, String evento, String erro, String usuario) {
        try {
            DynamicVO log = dem.create("LOG_ERROS");
            log.setProperty("ENTIDADE", entidade);
            log.setProperty("EVENTO", evento);
            log.setProperty("DTERRO", new java.util.Date());
            log.setProperty("USUARIO", usuario);
            log.setProperty("ERRO", erro);
            dem.insert(log);
        } catch (Exception e) {
            System.err.println("Erro ao registrar log de erro: " + e.getMessage());
        }
    }
}
```

### **2. Evento de Validação de Tarefa**
```java
package br.com.empresa.sankhya.event;

import br.com.sankhya.jape.vo.DynamicVO;
import java.util.List;

public class ValidarTarefaEvent extends BaseEvent {
    
    @Override
    public String executar(DynamicVO oldVO, DynamicVO newVO, String eventType) {
        try {
            if (!validarCondicoes(oldVO, newVO, eventType)) {
                return "Condições não atendidas";
            }
            
            String codTarefa = newVO.asString("CODTAREFA");
            String status = newVO.asString("STATUS");
            
            switch (eventType) {
                case "AFTER_INSERT":
                    return processarAfterInsert(codTarefa, status);
                case "BEFORE_UPDATE":
                    return processarBeforeUpdate(oldVO, newVO);
                case "AFTER_UPDATE":
                    return processarAfterUpdate(oldVO, newVO);
                default:
                    return "Tipo de evento não suportado: " + eventType;
            }
            
        } catch (Exception e) {
            logErro("TADTAR", eventType, e.getMessage(), getCurrentUser());
            return "Erro ao executar evento: " + e.getMessage();
        }
    }
    
    private String processarAfterInsert(String codTarefa, String status) {
        try {
            if ("A".equals(status)) {
                // Verificar se tem etapas
                List<DynamicVO> etapas = dem.findByDynamicFinder("TADETA", 
                    "CODTAREFA = ?", Integer.parseInt(codTarefa));
                
                if (etapas.isEmpty()) {
                    // Criar etapa inicial
                    DynamicVO etapa = dem.create("TADETA");
                    etapa.setProperty("CODTAREFA", Integer.parseInt(codTarefa));
                    etapa.setProperty("CODETAPA", 1);
                    etapa.setProperty("DESCRICAO", "Etapa Inicial");
                    etapa.setProperty("STATUS", "P");
                    etapa.setProperty("DTPRAZO", new java.util.Date());
                    
                    dem.insert(etapa);
                    
                    logOperacao("TADTAR", "AFTER_INSERT", 
                        "Etapa inicial criada automaticamente", getCurrentUser());
                    
                    return "Etapa inicial criada automaticamente";
                }
            }
            
            return "Tarefa processada com sucesso";
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar after insert", e);
        }
    }
    
    private String processarBeforeUpdate(DynamicVO oldVO, DynamicVO newVO) {
        try {
            String statusOld = oldVO.asString("STATUS");
            String statusNew = newVO.asString("STATUS");
            
            // Validar transição de status
            if ("F".equals(statusOld) && !"F".equals(statusNew)) {
                throw new RuntimeException("Tarefa finalizada não pode ter status alterado!");
            }
            
            // Se está sendo finalizada, verificar etapas
            if ("F".equals(statusNew) && !"F".equals(statusOld)) {
                List<DynamicVO> etapasPendentes = dem.findByDynamicFinder("TADETA", 
                    "CODTAREFA = ? AND STATUS = 'P'", 
                    Integer.parseInt(newVO.asString("CODTAREFA")));
                
                if (!etapasPendentes.isEmpty()) {
                    throw new RuntimeException("Não é possível finalizar tarefa com etapas pendentes!");
                }
            }
            
            return "Validação concluída com sucesso";
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar before update", e);
        }
    }
    
    private String processarAfterUpdate(DynamicVO oldVO, DynamicVO newVO) {
        try {
            String codTarefa = newVO.asString("CODTAREFA");
            String statusOld = oldVO.asString("STATUS");
            String statusNew = newVO.asString("STATUS");
            
            // Se foi finalizada, processar finalização
            if ("F".equals(statusNew) && !"F".equals(statusOld)) {
                // Finalizar todas as etapas pendentes
                List<DynamicVO> etapasPendentes = dem.findByDynamicFinder("TADETA", 
                    "CODTAREFA = ? AND STATUS = 'P'", Integer.parseInt(codTarefa));
                
                for (DynamicVO etapa : etapasPendentes) {
                    etapa.setProperty("STATUS", "C");
                    etapa.setProperty("DTFINALIZACAO", new java.util.Date());
                    etapa.setProperty("USUFINALIZACAO", getCurrentUser());
                    dem.update(etapa);
                }
                
                // Atualizar data de finalização da tarefa
                newVO.setProperty("DTFINALIZACAO", new java.util.Date());
                newVO.setProperty("USUFINALIZACAO", getCurrentUser());
                dem.update(newVO);
                
                logOperacao("TADTAR", "AFTER_UPDATE", 
                    "Tarefa finalizada com sucesso", getCurrentUser());
                
                return "Tarefa finalizada com sucesso";
            }
            
            return "Tarefa processada com sucesso";
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar after update", e);
        }
    }
    
    private String getCurrentUser() {
        // Implementar lógica para obter usuário atual
        return "USUARIO_ATUAL";
    }
}
```

### **3. Evento de Processamento de Etapa**
```java
package br.com.empresa.sankhya.event;

import br.com.sankhya.jape.vo.DynamicVO;
import java.util.List;

public class ProcessarEtapaEvent extends BaseEvent {
    
    @Override
    public String executar(DynamicVO oldVO, DynamicVO newVO, String eventType) {
        try {
            if (!validarCondicoes(oldVO, newVO, eventType)) {
                return "Condições não atendidas";
            }
            
            String codTarefa = newVO.asString("CODTAREFA");
            String codEtapa = newVO.asString("CODETAPA");
            String status = newVO.asString("STATUS");
            
            switch (eventType) {
                case "AFTER_UPDATE":
                    return processarAfterUpdate(codTarefa, codEtapa, status);
                default:
                    return "Tipo de evento não suportado: " + eventType;
            }
            
        } catch (Exception e) {
            logErro("TADETA", eventType, e.getMessage(), getCurrentUser());
            return "Erro ao executar evento: " + e.getMessage();
        }
    }
    
    private String processarAfterUpdate(String codTarefa, String codEtapa, String status) {
        try {
            if ("C".equals(status)) {
                // Verificar se todas as etapas estão concluídas
                List<DynamicVO> etapasPendentes = dem.findByDynamicFinder("TADETA", 
                    "CODTAREFA = ? AND STATUS = 'P'", Integer.parseInt(codTarefa));
                
                if (etapasPendentes.isEmpty()) {
                    // Todas as etapas concluídas, atualizar tarefa
                    DynamicVO tarefa = dem.findByPrimaryKey("TADTAR", 
                        new Object[]{Integer.parseInt(codTarefa)});
                    
                    if (tarefa != null) {
                        String statusTarefa = tarefa.asString("STATUS");
                        
                        if ("A".equals(statusTarefa)) {
                            // Calcular percentual de conclusão
                            List<DynamicVO> todasEtapas = dem.findByDynamicFinder("TADETA", 
                                "CODTAREFA = ?", Integer.parseInt(codTarefa));
                            
                            List<DynamicVO> etapasConcluidas = dem.findByDynamicFinder("TADETA", 
                                "CODTAREFA = ? AND STATUS = 'C'", Integer.parseInt(codTarefa));
                            
                            double percentual = 0;
                            if (!todasEtapas.isEmpty()) {
                                percentual = (etapasConcluidas.size() * 100.0) / todasEtapas.size();
                            }
                            
                            // Atualizar tarefa
                            tarefa.setProperty("PERCENTUAL_CONCLUSAO", percentual);
                            
                            // Se 100% concluído, marcar como validada
                            if (percentual >= 100) {
                                tarefa.setProperty("STATUS", "V");
                                tarefa.setProperty("DTVALIDACAO", new java.util.Date());
                                tarefa.setProperty("USUVALIDACAO", getCurrentUser());
                            }
                            
                            dem.update(tarefa);
                            
                            logOperacao("TADETA", "AFTER_UPDATE", 
                                "Tarefa atualizada - " + percentual + "% concluída", getCurrentUser());
                            
                            return "Tarefa atualizada - " + percentual + "% concluída";
                        }
                    }
                }
            }
            
            return "Etapa processada com sucesso";
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar after update", e);
        }
    }
    
    private String getCurrentUser() {
        // Implementar lógica para obter usuário atual
        return "USUARIO_ATUAL";
    }
}
```

## 🎯 Casos de Uso Avançados

### **1. Evento com Validação de Regras de Negócio**
```sql
CREATE OR REPLACE PROCEDURE STP_VALIDAR_REGRAS_NEGOCIO (
    P_ENTIDADE VARCHAR2,
    P_EVENTO VARCHAR2,
    P_CODREGISTRO NUMBER,
    P_CODUSU NUMBER,
    P_MENSAGEM OUT VARCHAR2
) AS
    P_COUNT_REGRAS NUMBER;
    P_REGRA_ATIVA VARCHAR2(1);
    P_CONDICAO VARCHAR2(4000);
    P_ACAO VARCHAR2(4000);
BEGIN
    -- Buscar regras ativas para a entidade e evento
    FOR regra IN (
        SELECT CODREGRA, CONDICAO, ACAO, ATIVO
        FROM AD_REGRAS_NEGOCIO
        WHERE ENTIDADE = P_ENTIDADE
        AND EVENTO = P_EVENTO
        AND ATIVO = 'S'
        ORDER BY ORDEM
    ) LOOP
        -- Executar condição
        EXECUTE IMMEDIATE regra.CONDICAO INTO P_COUNT_REGRAS;
        
        -- Se condição atendida, executar ação
        IF P_COUNT_REGRAS > 0 THEN
            EXECUTE IMMEDIATE regra.ACAO;
            
            -- Log da regra executada
            INSERT INTO LOG_REGRAS_EXECUTADAS (
                CODREGRA, ENTIDADE, EVENTO, CODREGISTRO, 
                DTEXECUCAO, USUARIO
            ) VALUES (
                regra.CODREGRA, P_ENTIDADE, P_EVENTO, P_CODREGISTRO,
                SYSDATE, P_CODUSU
            );
        END IF;
    END LOOP;
    
    P_MENSAGEM := 'Regras de negócio validadas com sucesso';
    
EXCEPTION
    WHEN OTHERS THEN
        P_MENSAGEM := 'Erro ao validar regras: ' || SQLERRM;
        -- Log do erro
        INSERT INTO LOG_ERROS (
            ENTIDADE, EVENTO, DTERRO, USUARIO, ERRO
        ) VALUES (
            P_ENTIDADE, P_EVENTO, SYSDATE, P_CODUSU, SQLERRM
        );
END;
```

### **2. Evento com Integração Externa**
```java
package br.com.empresa.sankhya.event;

import br.com.sankhya.jape.vo.DynamicVO;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

public class IntegracaoExternaEvent extends BaseEvent {
    
    private static final String URL_API = "https://api.externa.com.br/webhook";
    private static final String API_KEY = "sua-api-key-aqui";
    
    @Override
    public String executar(DynamicVO oldVO, DynamicVO newVO, String eventType) {
        try {
            if (!validarCondicoes(oldVO, newVO, eventType)) {
                return "Condições não atendidas";
            }
            
            // Preparar dados para envio
            String dados = prepararDados(newVO, eventType);
            
            // Enviar para API externa
            String resposta = enviarParaAPI(dados);
            
            // Processar resposta
            processarResposta(resposta, newVO);
            
            logOperacao("INTEGRACAO", eventType, 
                "Dados enviados com sucesso", getCurrentUser());
            
            return "Integração executada com sucesso";
            
        } catch (Exception e) {
            logErro("INTEGRACAO", eventType, e.getMessage(), getCurrentUser());
            return "Erro na integração: " + e.getMessage();
        }
    }
    
    private String prepararDados(DynamicVO vo, String eventType) {
        // Preparar JSON com dados do registro
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"evento\":\"").append(eventType).append("\",");
        json.append("\"entidade\":\"").append(vo.getEntityName()).append("\",");
        json.append("\"dados\":{");
        
        // Adicionar campos do registro
        String[] campos = vo.getPropertyNames();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(campos[i]).append("\":\"")
                .append(vo.asString(campos[i])).append("\"");
        }
        
        json.append("}");
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
            throw new RuntimeException("Erro na API: " + response.statusCode() + 
                " - " + response.body());
        }
        
        return response.body();
    }
    
    private void processarResposta(String resposta, DynamicVO vo) {
        // Processar resposta da API externa
        // Implementar lógica específica conforme necessário
        
        // Exemplo: atualizar campo com resposta
        if (resposta.contains("\"status\":\"success\"")) {
            vo.setProperty("STATUS_INTEGRACAO", "S");
            vo.setProperty("DT_INTEGRACAO", new java.util.Date());
            dem.update(vo);
        }
    }
    
    private String getCurrentUser() {
        // Implementar lógica para obter usuário atual
        return "USUARIO_ATUAL";
    }
}
```

### **3. Evento com Notificação**
```sql
CREATE OR REPLACE PROCEDURE STP_NOTIFICAR_MUDANCA_STATUS (
    P_CODTAREFA NUMBER,
    P_STATUS_OLD VARCHAR2,
    P_STATUS_NEW VARCHAR2,
    P_CODUSU NUMBER,
    P_MENSAGEM OUT VARCHAR2
) AS
    P_EMAIL_DESTINATARIO VARCHAR2(255);
    P_NOME_TAREFA VARCHAR2(255);
    P_ASSUNTO VARCHAR2(255);
    P_CORPO_EMAIL CLOB;
BEGIN
    -- Obter dados da tarefa
    SELECT DESCRTAR INTO P_NOME_TAREFA
    FROM TADTAR
    WHERE CODTAREFA = P_CODTAREFA;
    
    -- Obter email do usuário
    SELECT EMAIL INTO P_EMAIL_DESTINATARIO
    FROM TSIUSU
    WHERE CODUSU = P_CODUSU;
    
    -- Preparar email
    P_ASSUNTO := 'Mudança de Status - Tarefa ' || P_CODTAREFA;
    P_CORPO_EMAIL := 'A tarefa "' || P_NOME_TAREFA || '" teve seu status alterado de ' ||
                     P_STATUS_OLD || ' para ' || P_STATUS_NEW || ' em ' ||
                     TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS');
    
    -- Enviar email
    INSERT INTO FILA_EMAIL (
        EMAIL_DESTINATARIO, ASSUNTO, CORPO, DTENVIO, STATUS
    ) VALUES (
        P_EMAIL_DESTINATARIO, P_ASSUNTO, P_CORPO_EMAIL, SYSDATE, 'P'
    );
    
    -- Log da notificação
    INSERT INTO LOG_NOTIFICACOES (
        CODTAREFA, TIPO, DESTINATARIO, ASSUNTO, DTENVIO, STATUS
    ) VALUES (
        P_CODTAREFA, 'EMAIL', P_EMAIL_DESTINATARIO, P_ASSUNTO, SYSDATE, 'ENVIADO'
    );
    
    P_MENSAGEM := 'Notificação enviada com sucesso';
    
EXCEPTION
    WHEN OTHERS THEN
        P_MENSAGEM := 'Erro ao enviar notificação: ' || SQLERRM;
        -- Log do erro
        INSERT INTO LOG_ERROS (
            CODTAREFA, EVENTO, DTERRO, USUARIO, ERRO
        ) VALUES (
            P_CODTAREFA, 'NOTIFICACAO', SYSDATE, P_CODUSU, SQLERRM
        );
END;
```

## 🛠️ Boas Práticas

### **1. Design de Eventos**
- **Granularidade**: Eventos específicos e bem definidos
- **Performance**: Evitar eventos que executam operações pesadas
- **Dependências**: Minimizar dependências entre eventos
- **Idempotência**: Eventos devem ser idempotentes
- **Rollback**: Implementar rollback em caso de erro

### **2. Tratamento de Erros**
- **Logs Detalhados**: Registrar todos os erros
- **Recuperação**: Implementar mecanismos de recuperação
- **Notificações**: Notificar administradores sobre erros críticos
- **Monitoramento**: Monitorar execução dos eventos
- **Alertas**: Configurar alertas para falhas

### **3. Performance**
- **Índices**: Usar índices adequados nas consultas
- **Transações**: Manter transações curtas
- **Cache**: Usar cache para dados frequentes
- **Otimização**: Otimizar consultas SQL
- **Monitoramento**: Monitorar performance dos eventos

### **4. Segurança**
- **Validação**: Validar todos os dados de entrada
- **Autorização**: Verificar permissões do usuário
- **Auditoria**: Registrar todas as operações
- **Criptografia**: Criptografar dados sensíveis
- **Backup**: Manter backups regulares

## 🔍 Troubleshooting

### **Problemas Comuns**
- **Evento não dispara**: Verificar condições e configuração
- **Performance lenta**: Otimizar consultas e índices
- **Erros de validação**: Revisar regras de negócio
- **Deadlocks**: Revisar ordem de locks
- **Timeout**: Otimizar operações longas

### **Soluções**
- **Logs**: Analisar logs de execução
- **Debug**: Usar ferramentas de debug
- **Testes**: Testar em ambiente isolado
- **Documentação**: Consultar documentação oficial
- **Suporte**: Contatar suporte técnico

## 🚀 Evolução e Tendências

### **Melhorias Contínuas**
- **Novos Tipos**: Novos tipos de eventos
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

*Este documento foi criado com base na documentação oficial do Sankhya Developer sobre Eventos Programados e melhores práticas de desenvolvimento.*
