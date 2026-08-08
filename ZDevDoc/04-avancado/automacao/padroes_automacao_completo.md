# 🤖 Padrões de Automação e Agendamentos Sankhya - Guia Completo

## 🎯 Visão Geral

Este guia apresenta padrões completos para **automação de processos**, **ações agendadas** e **sistemas de monitoramento** no ambiente Sankhya, incluindo templates, exemplos práticos e melhores práticas para implementação de automações robustas e confiáveis.

## ⏰ Ações Agendadas

### **Template Base de Ação Agendada**
```sql
-- Procedure para ação agendada
CREATE OR REPLACE PROCEDURE SANKHYA.STP_ACAO_AGENDADA AS
    P_COUNT_PROCESSADOS NUMBER := 0;
    P_COUNT_ERROS NUMBER := 0;
    P_DATA_EXECUCAO DATE := SYSDATE;
    
    CURSOR C_REGISTROS IS
        SELECT ID, CAMPO1, CAMPO2
        FROM TABELA
        WHERE STATUS = 'PENDENTE'
        AND DTCRIACAO < SYSDATE - 1;
        
BEGIN
    -- Log de início
    INSERT INTO LOG_EXECUCAO (DT_EXECUCAO, PROCEDURE, STATUS, OBSERVACAO)
    VALUES (P_DATA_EXECUCAO, 'STP_ACAO_AGENDADA', 'INICIADO', 'Processamento iniciado');
    
    FOR R IN C_REGISTROS LOOP
        BEGIN
            -- Processar registro
            UPDATE TABELA 
            SET STATUS = 'PROCESSADO',
                DTATUALIZACAO = SYSDATE,
                USUARIO_ATUALIZACAO = 'SISTEMA'
            WHERE ID = R.ID;
            
            P_COUNT_PROCESSADOS := P_COUNT_PROCESSADOS + 1;
            
            -- Commit a cada 100 registros
            IF MOD(P_COUNT_PROCESSADOS, 100) = 0 THEN
                COMMIT;
            END IF;
            
        EXCEPTION
            WHEN OTHERS THEN
                P_COUNT_ERROS := P_COUNT_ERROS + 1;
                -- Log do erro específico
                INSERT INTO LOG_ERRO_DETALHE (ID_REGISTRO, ERRO, DT_ERRO, PROCEDURE)
                VALUES (R.ID, SQLERRM, SYSDATE, 'STP_ACAO_AGENDADA');
        END;
    END LOOP;
    
    COMMIT;
    
    -- Log final
    UPDATE LOG_EXECUCAO 
    SET STATUS = 'CONCLUIDO',
        OBSERVACAO = 'Processados: ' || P_COUNT_PROCESSADOS || ' | Erros: ' || P_COUNT_ERROS
    WHERE DT_EXECUCAO = P_DATA_EXECUCAO 
    AND PROCEDURE = 'STP_ACAO_AGENDADA';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        -- Log de erro geral
        INSERT INTO LOG_ERRO (DT_ERRO, PROCEDURE, ERRO, STATUS)
        VALUES (SYSDATE, 'STP_ACAO_AGENDADA', SQLERRM, 'ERRO_GERAL');
        COMMIT;
END;
/
```

### **Ação Agendada de Retorno de Pendência**
```sql
CREATE OR REPLACE PROCEDURE SANKHYA.STP_RETORN_PEND AS
    P_NUNOTA   TGFCAB.NUNOTA%TYPE;
    P_PENDENTE TGFCAB.PENDENTE%TYPE; 
    P_DIAS     NUMBER;
    P_COUNT_ATUALIZADOS NUMBER := 0;

    CURSOR RET_PEND IS
        SELECT
            NUNOTA,
            PENDENTE,
            AD_DT_MARC_ENTREG, 
            SYSDATE - AD_DT_MARC_ENTREG AS DIAS
        FROM TGFCAB
        WHERE
            DTNEG > SYSDATE - 60
            AND PENDENTE = 'N'
            AND (AD_VALIDACAO_COMPROV_ENTREGA = 'N' OR AD_VALIDACAO_COMPROV_ENTREGA IS NULL);

BEGIN
    -- Log de início
    INSERT INTO LOG_EXECUCAO (DT_EXECUCAO, PROCEDURE, STATUS, OBSERVACAO)
    VALUES (SYSDATE, 'STP_RETORN_PEND', 'INICIADO', 'Verificação de pendências iniciada');
    
    FOR R IN RET_PEND LOOP
        BEGIN
            P_NUNOTA := R.NUNOTA;
            P_DIAS := R.DIAS;
            
            -- Retornar para pendente se passou mais de 7 dias
            IF P_DIAS > 7 THEN
                UPDATE TGFCAB 
                SET PENDENTE = 'S',
                    AD_DT_MARC_ENTREG = NULL,
                    AD_VALIDACAO_COMPROV_ENTREGA = NULL
                WHERE NUNOTA = P_NUNOTA;
                
                P_COUNT_ATUALIZADOS := P_COUNT_ATUALIZADOS + 1;
                
                -- Log da operação
                INSERT INTO LOG_OPERACAO (
                    DT_OPERACAO, PROCEDURE, OPERACAO, DETALHES
                ) VALUES (
                    SYSDATE, 'STP_RETORN_PEND', 'RETORNO_PENDENCIA',
                    'Nota ' || P_NUNOTA || ' retornada para pendente após ' || P_DIAS || ' dias'
                );
            END IF;
            
        EXCEPTION
            WHEN OTHERS THEN
                -- Log do erro específico
                INSERT INTO LOG_ERRO_DETALHE (
                    ID_REGISTRO, ERRO, DT_ERRO, PROCEDURE
                ) VALUES (
                    P_NUNOTA, SQLERRM, SYSDATE, 'STP_RETORN_PEND'
                );
        END;
    END LOOP;
    
    COMMIT;
    
    -- Log final
    UPDATE LOG_EXECUCAO 
    SET STATUS = 'CONCLUIDO',
        OBSERVACAO = 'Notas retornadas para pendente: ' || P_COUNT_ATUALIZADOS
    WHERE DT_EXECUCAO = SYSDATE 
    AND PROCEDURE = 'STP_RETORN_PEND';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        INSERT INTO LOG_ERRO (DT_ERRO, PROCEDURE, ERRO, STATUS)
        VALUES (SYSDATE, 'STP_RETORN_PEND', SQLERRM, 'ERRO_GERAL');
        COMMIT;
END;
/
```

### **Ação Agendada de Limpeza de Logs**
```sql
CREATE OR REPLACE PROCEDURE SANKHYA.STP_LIMPEZA_LOGS AS
    P_COUNT_REMOVIDOS NUMBER := 0;
    P_DATA_LIMITE DATE := SYSDATE - 90; -- Manter logs por 90 dias
    
BEGIN
    -- Log de início
    INSERT INTO LOG_EXECUCAO (DT_EXECUCAO, PROCEDURE, STATUS, OBSERVACAO)
    VALUES (SYSDATE, 'STP_LIMPEZA_LOGS', 'INICIADO', 'Limpeza de logs iniciada');
    
    -- Remover logs antigos
    DELETE FROM LOG_EXECUCAO 
    WHERE DT_EXECUCAO < P_DATA_LIMITE;
    P_COUNT_REMOVIDOS := P_COUNT_REMOVIDOS + SQL%ROWCOUNT;
    
    DELETE FROM LOG_ERRO 
    WHERE DT_ERRO < P_DATA_LIMITE;
    P_COUNT_REMOVIDOS := P_COUNT_REMOVIDOS + SQL%ROWCOUNT;
    
    DELETE FROM LOG_ERRO_DETALHE 
    WHERE DT_ERRO < P_DATA_LIMITE;
    P_COUNT_REMOVIDOS := P_COUNT_REMOVIDOS + SQL%ROWCOUNT;
    
    DELETE FROM LOG_OPERACAO 
    WHERE DT_OPERACAO < P_DATA_LIMITE;
    P_COUNT_REMOVIDOS := P_COUNT_REMOVIDOS + SQL%ROWCOUNT;
    
    COMMIT;
    
    -- Log final
    UPDATE LOG_EXECUCAO 
    SET STATUS = 'CONCLUIDO',
        OBSERVACAO = 'Registros removidos: ' || P_COUNT_REMOVIDOS
    WHERE DT_EXECUCAO = SYSDATE 
    AND PROCEDURE = 'STP_LIMPEZA_LOGS';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        INSERT INTO LOG_ERRO (DT_ERRO, PROCEDURE, ERRO, STATUS)
        VALUES (SYSDATE, 'STP_LIMPEZA_LOGS', SQLERRM, 'ERRO_GERAL');
        COMMIT;
END;
/
```

## 📧 Envio Automático de Relatórios

### **Procedure de Envio de Relatório por Email**
```sql
CREATE OR REPLACE PROCEDURE SANKHYA.STP_ENVIO_RELATORIO_EMAIL AS
    P_RELATORIO VARCHAR2(4000);
    P_EMAIL_DESTINATARIO VARCHAR2(500);
    P_ASSUNTO VARCHAR2(200);
    P_CORPO_EMAIL VARCHAR2(4000);
    P_DATA_RELATORIO DATE := SYSDATE;
    
    CURSOR C_DESTINATARIOS IS
        SELECT EMAIL, NOME
        FROM USUARIOS_NOTIFICACAO
        WHERE ATIVO = 'S'
        AND TIPO_RELATORIO = 'VENDAS_DIARIAS';
        
BEGIN
    -- Gerar relatório de vendas do dia
    SELECT 
        'RELATÓRIO DE VENDAS - ' || TO_CHAR(P_DATA_RELATORIO, 'DD/MM/YYYY') || CHR(10) ||
        '===============================================' || CHR(10) ||
        'Total de Pedidos: ' || COUNT(*) || CHR(10) ||
        'Valor Total: R$ ' || TO_CHAR(SUM(VLRNOTA), 'FM999G999G999D90') || CHR(10) ||
        'Ticket Médio: R$ ' || TO_CHAR(AVG(VLRNOTA), 'FM999G999G999D90') || CHR(10) ||
        '===============================================' || CHR(10) ||
        'Top 5 Clientes:' || CHR(10) ||
        LISTAGG(
            PAR.NOMEPARC || ' - R$ ' || TO_CHAR(SUM(CAB.VLRNOTA), 'FM999G999G999D90'),
            CHR(10)
        ) WITHIN GROUP (ORDER BY SUM(CAB.VLRNOTA) DESC)
    INTO P_RELATORIO
    FROM TGFCAB CAB
    INNER JOIN TGFPAR PAR ON CAB.CODPARC = PAR.CODPARC
    WHERE CAB.TIPMOV = 'V'
    AND CAB.DTNEG = TRUNC(P_DATA_RELATORIO)
    AND CAB.PENDENTE = 'N'
    GROUP BY PAR.NOMEPARC
    ORDER BY SUM(CAB.VLRNOTA) DESC
    FETCH FIRST 5 ROWS ONLY;
    
    -- Enviar para cada destinatário
    FOR D IN C_DESTINATARIOS LOOP
        BEGIN
            P_EMAIL_DESTINATARIO := D.EMAIL;
            P_ASSUNTO := 'Relatório de Vendas - ' || TO_CHAR(P_DATA_RELATORIO, 'DD/MM/YYYY');
            P_CORPO_EMAIL := 'Olá ' || D.NOME || ',' || CHR(10) || CHR(10) ||
                           P_RELATORIO || CHR(10) || CHR(10) ||
                           'Este é um relatório automático do sistema Sankhya.' || CHR(10) ||
                           'Data de geração: ' || TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS');
            
            -- Simular envio de email (implementar conforme sistema de email)
            INSERT INTO FILA_EMAIL (
                DESTINATARIO, ASSUNTO, CORPO, DT_CRIACAO, STATUS
            ) VALUES (
                P_EMAIL_DESTINATARIO, P_ASSUNTO, P_CORPO_EMAIL, SYSDATE, 'PENDENTE'
            );
            
            -- Log do envio
            INSERT INTO LOG_OPERACAO (
                DT_OPERACAO, PROCEDURE, OPERACAO, DETALHES
            ) VALUES (
                SYSDATE, 'STP_ENVIO_RELATORIO_EMAIL', 'ENVIO_EMAIL',
                'Relatório enviado para: ' || P_EMAIL_DESTINATARIO
            );
            
        EXCEPTION
            WHEN OTHERS THEN
                INSERT INTO LOG_ERRO_DETALHE (
                    ID_REGISTRO, ERRO, DT_ERRO, PROCEDURE
                ) VALUES (
                    P_EMAIL_DESTINATARIO, SQLERRM, SYSDATE, 'STP_ENVIO_RELATORIO_EMAIL'
                );
        END;
    END LOOP;
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        INSERT INTO LOG_ERRO (DT_ERRO, PROCEDURE, ERRO, STATUS)
        VALUES (SYSDATE, 'STP_ENVIO_RELATORIO_EMAIL', SQLERRM, 'ERRO_GERAL');
        COMMIT;
END;
/
```

## ✅ Validações Automáticas

### **Procedure de Validação de Dados**
```sql
CREATE OR REPLACE PROCEDURE SANKHYA.STP_VALIDACAO_DADOS AS
    P_COUNT_VALIDADOS NUMBER := 0;
    P_COUNT_ERROS NUMBER := 0;
    P_RESULTADO_VALIDACAO VARCHAR2(4000);
    
    CURSOR C_PEDIDOS_PENDENTES IS
        SELECT NUNOTA, CODPARC, VLRNOTA, DTNEG
        FROM TGFCAB
        WHERE PENDENTE = 'S'
        AND DTNEG >= SYSDATE - 7;
        
BEGIN
    -- Log de início
    INSERT INTO LOG_EXECUCAO (DT_EXECUCAO, PROCEDURE, STATUS, OBSERVACAO)
    VALUES (SYSDATE, 'STP_VALIDACAO_DADOS', 'INICIADO', 'Validação de dados iniciada');
    
    FOR P IN C_PEDIDOS_PENDENTES LOOP
        BEGIN
            P_RESULTADO_VALIDACAO := '';
            
            -- Validar cliente ativo
            SELECT COUNT(*) INTO P_COUNT_VALIDADOS
            FROM TGFPAR
            WHERE CODPARC = P.CODPARC
            AND ATIVO = 'S';
            
            IF P_COUNT_VALIDADOS = 0 THEN
                P_RESULTADO_VALIDACAO := P_RESULTADO_VALIDACAO || 'Cliente inativo; ';
            END IF;
            
            -- Validar valor mínimo
            IF P.VLRNOTA <= 0 THEN
                P_RESULTADO_VALIDACAO := P_RESULTADO_VALIDACAO || 'Valor inválido; ';
            END IF;
            
            -- Validar data
            IF P.DTNEG > SYSDATE THEN
                P_RESULTADO_VALIDACAO := P_RESULTADO_VALIDACAO || 'Data futura; ';
            END IF;
            
            -- Validar itens
            SELECT COUNT(*) INTO P_COUNT_VALIDADOS
            FROM TGFITE
            WHERE NUNOTA = P.NUNOTA;
            
            IF P_COUNT_VALIDADOS = 0 THEN
                P_RESULTADO_VALIDACAO := P_RESULTADO_VALIDACAO || 'Sem itens; ';
            END IF;
            
            -- Registrar resultado da validação
            IF P_RESULTADO_VALIDACAO IS NOT NULL THEN
                INSERT INTO LOG_VALIDACAO (
                    ID_REGISTRO, TIPO_REGISTRO, RESULTADO, DT_VALIDACAO
                ) VALUES (
                    P.NUNOTA, 'PEDIDO', P_RESULTADO_VALIDACAO, SYSDATE
                );
                
                P_COUNT_ERROS := P_COUNT_ERROS + 1;
            END IF;
            
        EXCEPTION
            WHEN OTHERS THEN
                INSERT INTO LOG_ERRO_DETALHE (
                    ID_REGISTRO, ERRO, DT_ERRO, PROCEDURE
                ) VALUES (
                    P.NUNOTA, SQLERRM, SYSDATE, 'STP_VALIDACAO_DADOS'
                );
        END;
    END LOOP;
    
    COMMIT;
    
    -- Log final
    UPDATE LOG_EXECUCAO 
    SET STATUS = 'CONCLUIDO',
        OBSERVACAO = 'Validações concluídas - Erros encontrados: ' || P_COUNT_ERROS
    WHERE DT_EXECUCAO = SYSDATE 
    AND PROCEDURE = 'STP_VALIDACAO_DADOS';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        INSERT INTO LOG_ERRO (DT_ERRO, PROCEDURE, ERRO, STATUS)
        VALUES (SYSDATE, 'STP_VALIDACAO_DADOS', SQLERRM, 'ERRO_GERAL');
        COMMIT;
END;
/
```

## 🔧 Procedures de Manutenção

### **Procedure de Manutenção de Índices**
```sql
CREATE OR REPLACE PROCEDURE SANKHYA.STP_MANUTENCAO_INDICES AS
    P_COUNT_INDICES NUMBER := 0;
    P_SQL_DDL VARCHAR2(4000);
    
    CURSOR C_INDICES_FRAGMENTADOS IS
        SELECT 
            INDEX_NAME,
            TABLE_NAME,
            ROUND(100 * (1 - (AVG_SPACE / AVG_BLOCKS)), 2) AS FRAGMENTACAO
        FROM (
            SELECT 
                I.INDEX_NAME,
                I.TABLE_NAME,
                AVG(S.BYTES) AS AVG_SPACE,
                AVG(S.BLOCKS) AS AVG_BLOCKS
            FROM USER_INDEXES I
            INNER JOIN USER_SEGMENTS S ON I.INDEX_NAME = S.SEGMENT_NAME
            WHERE I.TABLE_NAME IN ('TGFCAB', 'TGFITE', 'TGFPAR', 'TGFPRO')
            GROUP BY I.INDEX_NAME, I.TABLE_NAME
        )
        WHERE ROUND(100 * (1 - (AVG_SPACE / AVG_BLOCKS)), 2) > 20;
        
BEGIN
    -- Log de início
    INSERT INTO LOG_EXECUCAO (DT_EXECUCAO, PROCEDURE, STATUS, OBSERVACAO)
    VALUES (SYSDATE, 'STP_MANUTENCAO_INDICES', 'INICIADO', 'Manutenção de índices iniciada');
    
    FOR I IN C_INDICES_FRAGMENTADOS LOOP
        BEGIN
            -- Reconstruir índice fragmentado
            P_SQL_DDL := 'ALTER INDEX ' || I.INDEX_NAME || ' REBUILD';
            
            EXECUTE IMMEDIATE P_SQL_DDL;
            
            P_COUNT_INDICES := P_COUNT_INDICES + 1;
            
            -- Log da operação
            INSERT INTO LOG_OPERACAO (
                DT_OPERACAO, PROCEDURE, OPERACAO, DETALHES
            ) VALUES (
                SYSDATE, 'STP_MANUTENCAO_INDICES', 'REBUILD_INDEX',
                'Índice ' || I.INDEX_NAME || ' reconstruído (Fragmentação: ' || I.FRAGMENTACAO || '%)'
            );
            
        EXCEPTION
            WHEN OTHERS THEN
                INSERT INTO LOG_ERRO_DETALHE (
                    ID_REGISTRO, ERRO, DT_ERRO, PROCEDURE
                ) VALUES (
                    I.INDEX_NAME, SQLERRM, SYSDATE, 'STP_MANUTENCAO_INDICES'
                );
        END;
    END LOOP;
    
    COMMIT;
    
    -- Log final
    UPDATE LOG_EXECUCAO 
    SET STATUS = 'CONCLUIDO',
        OBSERVACAO = 'Índices reconstruídos: ' || P_COUNT_INDICES
    WHERE DT_EXECUCAO = SYSDATE 
    AND PROCEDURE = 'STP_MANUTENCAO_INDICES';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        INSERT INTO LOG_ERRO (DT_ERRO, PROCEDURE, ERRO, STATUS)
        VALUES (SYSDATE, 'STP_MANUTENCAO_INDICES', SQLERRM, 'ERRO_GERAL');
        COMMIT;
END;
/
```

### **Procedure de Backup de Dados Críticos**
```sql
CREATE OR REPLACE PROCEDURE SANKHYA.STP_BACKUP_DADOS_CRITICOS AS
    P_COUNT_BACKUPS NUMBER := 0;
    P_DATA_BACKUP DATE := SYSDATE;
    
BEGIN
    -- Log de início
    INSERT INTO LOG_EXECUCAO (DT_EXECUCAO, PROCEDURE, STATUS, OBSERVACAO)
    VALUES (P_DATA_BACKUP, 'STP_BACKUP_DADOS_CRITICOS', 'INICIADO', 'Backup de dados críticos iniciado');
    
    -- Backup de configurações
    INSERT INTO BACKUP_CONFIGURACOES (
        DT_BACKUP, CONFIGURACAO, VALOR, USUARIO
    )
    SELECT P_DATA_BACKUP, PARAMETRO, VALOR, 'SISTEMA'
    FROM PARAMETROS_SISTEMA
    WHERE CRITICO = 'S';
    
    P_COUNT_BACKUPS := P_COUNT_BACKUPS + SQL%ROWCOUNT;
    
    -- Backup de usuários ativos
    INSERT INTO BACKUP_USUARIOS (
        DT_BACKUP, CODUSU, NOME, ATIVO, PERFIL
    )
    SELECT P_DATA_BACKUP, CODUSU, NOME, ATIVO, PERFIL
    FROM TSIUSU
    WHERE ATIVO = 'S';
    
    P_COUNT_BACKUPS := P_COUNT_BACKUPS + SQL%ROWCOUNT;
    
    -- Backup de permissões
    INSERT INTO BACKUP_PERMISSOES (
        DT_BACKUP, CODUSU, RECURSO, PERMISSAO
    )
    SELECT P_DATA_BACKUP, CODUSU, RECURSO, PERMISSAO
    FROM PERMISSOES_USUARIO
    WHERE ATIVO = 'S';
    
    P_COUNT_BACKUPS := P_COUNT_BACKUPS + SQL%ROWCOUNT;
    
    COMMIT;
    
    -- Log final
    UPDATE LOG_EXECUCAO 
    SET STATUS = 'CONCLUIDO',
        OBSERVACAO = 'Registros de backup criados: ' || P_COUNT_BACKUPS
    WHERE DT_EXECUCAO = P_DATA_BACKUP 
    AND PROCEDURE = 'STP_BACKUP_DADOS_CRITICOS';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        INSERT INTO LOG_ERRO (DT_ERRO, PROCEDURE, ERRO, STATUS)
        VALUES (SYSDATE, 'STP_BACKUP_DADOS_CRITICOS', SQLERRM, 'ERRO_GERAL');
        COMMIT;
END;
/
```

## 🔔 Sistema de Notificações

### **Procedure de Notificações Automáticas**
```sql
CREATE OR REPLACE PROCEDURE SANKHYA.STP_NOTIFICACOES_AUTOMATICAS AS
    P_COUNT_NOTIFICACOES NUMBER := 0;
    P_TIPO_NOTIFICACAO VARCHAR2(50);
    P_MENSAGEM VARCHAR2(4000);
    P_DESTINATARIOS VARCHAR2(1000);
    
    CURSOR C_NOTIFICACOES_PENDENTES IS
        SELECT 
            TIPO,
            MENSAGEM,
            LISTAGG(EMAIL, ';') WITHIN GROUP (ORDER BY EMAIL) AS EMAILS
        FROM NOTIFICACOES_PENDENTES NP
        INNER JOIN USUARIOS_NOTIFICACAO UN ON NP.TIPO = UN.TIPO_NOTIFICACAO
        WHERE NP.STATUS = 'PENDENTE'
        AND UN.ATIVO = 'S'
        GROUP BY TIPO, MENSAGEM;
        
BEGIN
    -- Log de início
    INSERT INTO LOG_EXECUCAO (DT_EXECUCAO, PROCEDURE, STATUS, OBSERVACAO)
    VALUES (SYSDATE, 'STP_NOTIFICACOES_AUTOMATICAS', 'INICIADO', 'Processamento de notificações iniciado');
    
    FOR N IN C_NOTIFICACOES_PENDENTES LOOP
        BEGIN
            P_TIPO_NOTIFICACAO := N.TIPO;
            P_MENSAGEM := N.MENSAGEM;
            P_DESTINATARIOS := N.EMAILS;
            
            -- Enviar notificação
            INSERT INTO FILA_NOTIFICACAO (
                TIPO, MENSAGEM, DESTINATARIOS, DT_CRIACAO, STATUS
            ) VALUES (
                P_TIPO_NOTIFICACAO, P_MENSAGEM, P_DESTINATARIOS, SYSDATE, 'ENVIADO'
            );
            
            -- Marcar como processada
            UPDATE NOTIFICACOES_PENDENTES 
            SET STATUS = 'PROCESSADA',
                DT_PROCESSAMENTO = SYSDATE
            WHERE TIPO = P_TIPO_NOTIFICACAO
            AND MENSAGEM = P_MENSAGEM
            AND STATUS = 'PENDENTE';
            
            P_COUNT_NOTIFICACOES := P_COUNT_NOTIFICACOES + 1;
            
            -- Log da operação
            INSERT INTO LOG_OPERACAO (
                DT_OPERACAO, PROCEDURE, OPERACAO, DETALHES
            ) VALUES (
                SYSDATE, 'STP_NOTIFICACOES_AUTOMATICAS', 'ENVIO_NOTIFICACAO',
                'Notificação ' || P_TIPO_NOTIFICACAO || ' enviada para: ' || P_DESTINATARIOS
            );
            
        EXCEPTION
            WHEN OTHERS THEN
                INSERT INTO LOG_ERRO_DETALHE (
                    ID_REGISTRO, ERRO, DT_ERRO, PROCEDURE
                ) VALUES (
                    P_TIPO_NOTIFICACAO, SQLERRM, SYSDATE, 'STP_NOTIFICACOES_AUTOMATICAS'
                );
        END;
    END LOOP;
    
    COMMIT;
    
    -- Log final
    UPDATE LOG_EXECUCAO 
    SET STATUS = 'CONCLUIDO',
        OBSERVACAO = 'Notificações processadas: ' || P_COUNT_NOTIFICACOES
    WHERE DT_EXECUCAO = SYSDATE 
    AND PROCEDURE = 'STP_NOTIFICACOES_AUTOMATICAS';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        INSERT INTO LOG_ERRO (DT_ERRO, PROCEDURE, ERRO, STATUS)
        VALUES (SYSDATE, 'STP_NOTIFICACOES_AUTOMATICAS', SQLERRM, 'ERRO_GERAL');
        COMMIT;
END;
/
```

## 📊 Monitoramento e Logs

### **Estrutura de Tabelas de Log**
```sql
-- Tabela de log de execução
CREATE TABLE LOG_EXECUCAO (
    ID NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    DT_EXECUCAO DATE NOT NULL,
    PROCEDURE VARCHAR2(100) NOT NULL,
    STATUS VARCHAR2(20) NOT NULL,
    OBSERVACAO VARCHAR2(4000),
    DT_CRIACAO DATE DEFAULT SYSDATE
);

-- Tabela de log de erros
CREATE TABLE LOG_ERRO (
    ID NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    DT_ERRO DATE NOT NULL,
    PROCEDURE VARCHAR2(100) NOT NULL,
    ERRO VARCHAR2(4000) NOT NULL,
    STATUS VARCHAR2(20) NOT NULL,
    DT_CRIACAO DATE DEFAULT SYSDATE
);

-- Tabela de log de erros detalhados
CREATE TABLE LOG_ERRO_DETALHE (
    ID NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    ID_REGISTRO VARCHAR2(100),
    ERRO VARCHAR2(4000) NOT NULL,
    DT_ERRO DATE NOT NULL,
    PROCEDURE VARCHAR2(100) NOT NULL,
    DT_CRIACAO DATE DEFAULT SYSDATE
);

-- Tabela de log de operações
CREATE TABLE LOG_OPERACAO (
    ID NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    DT_OPERACAO DATE NOT NULL,
    PROCEDURE VARCHAR2(100) NOT NULL,
    OPERACAO VARCHAR2(100) NOT NULL,
    DETALHES VARCHAR2(4000),
    DT_CRIACAO DATE DEFAULT SYSDATE
);

-- Tabela de log de validação
CREATE TABLE LOG_VALIDACAO (
    ID NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    ID_REGISTRO VARCHAR2(100) NOT NULL,
    TIPO_REGISTRO VARCHAR2(50) NOT NULL,
    RESULTADO VARCHAR2(4000) NOT NULL,
    DT_VALIDACAO DATE NOT NULL,
    DT_CRIACAO DATE DEFAULT SYSDATE
);
```

### **Procedure de Monitoramento de Performance**
```sql
CREATE OR REPLACE PROCEDURE SANKHYA.STP_MONITORAMENTO_PERFORMANCE AS
    P_COUNT_ALERTAS NUMBER := 0;
    P_TAMANHO_DB NUMBER;
    P_ESPACO_LIVRE NUMBER;
    P_PERCENTUAL_USO NUMBER;
    
BEGIN
    -- Log de início
    INSERT INTO LOG_EXECUCAO (DT_EXECUCAO, PROCEDURE, STATUS, OBSERVACAO)
    VALUES (SYSDATE, 'STP_MONITORAMENTO_PERFORMANCE', 'INICIADO', 'Monitoramento de performance iniciado');
    
    -- Verificar espaço em disco
    SELECT 
        ROUND(SUM(BYTES) / 1024 / 1024 / 1024, 2) AS TAMANHO_GB,
        ROUND(SUM(BYTES_FREE) / 1024 / 1024 / 1024, 2) AS ESPACO_LIVRE_GB
    INTO P_TAMANHO_DB, P_ESPACO_LIVRE
    FROM (
        SELECT BYTES, 0 AS BYTES_FREE FROM USER_SEGMENTS
        UNION ALL
        SELECT 0 AS BYTES, BYTES AS BYTES_FREE FROM USER_FREE_SPACE
    );
    
    P_PERCENTUAL_USO := ROUND(((P_TAMANHO_DB - P_ESPACO_LIVRE) / P_TAMANHO_DB) * 100, 2);
    
    -- Alertar se uso > 80%
    IF P_PERCENTUAL_USO > 80 THEN
        INSERT INTO LOG_OPERACAO (
            DT_OPERACAO, PROCEDURE, OPERACAO, DETALHES
        ) VALUES (
            SYSDATE, 'STP_MONITORAMENTO_PERFORMANCE', 'ALERTA_ESPACO',
            'Uso de espaço em disco: ' || P_PERCENTUAL_USO || '% (Crítico)'
        );
        
        P_COUNT_ALERTAS := P_COUNT_ALERTAS + 1;
    END IF;
    
    -- Verificar procedures com erro
    SELECT COUNT(*) INTO P_COUNT_ALERTAS
    FROM LOG_ERRO
    WHERE DT_ERRO >= SYSDATE - 1;
    
    IF P_COUNT_ALERTAS > 10 THEN
        INSERT INTO LOG_OPERACAO (
            DT_OPERACAO, PROCEDURE, OPERACAO, DETALHES
        ) VALUES (
            SYSDATE, 'STP_MONITORAMENTO_PERFORMANCE', 'ALERTA_ERROS',
            'Muitos erros nas últimas 24h: ' || P_COUNT_ALERTAS
        );
    END IF;
    
    COMMIT;
    
    -- Log final
    UPDATE LOG_EXECUCAO 
    SET STATUS = 'CONCLUIDO',
        OBSERVACAO = 'Monitoramento concluído - Alertas: ' || P_COUNT_ALERTAS
    WHERE DT_EXECUCAO = SYSDATE 
    AND PROCEDURE = 'STP_MONITORAMENTO_PERFORMANCE';
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        INSERT INTO LOG_ERRO (DT_ERRO, PROCEDURE, ERRO, STATUS)
        VALUES (SYSDATE, 'STP_MONITORAMENTO_PERFORMANCE', SQLERRM, 'ERRO_GERAL');
        COMMIT;
END;
/
```

## 🛠️ Boas Práticas

### **1. Agendamento**
- **Horários**: Agendar em horários de baixo uso
- **Frequência**: Definir frequência adequada
- **Dependências**: Considerar dependências entre jobs
- **Monitoramento**: Monitorar execução
- **Rollback**: Ter planos de rollback

### **2. Performance**
- **Batch Processing**: Processar em lotes
- **Commits**: Commits regulares
- **Índices**: Manter índices otimizados
- **Estatísticas**: Estatísticas atualizadas
- **Recursos**: Monitorar uso de recursos

### **3. Confiabilidade**
- **Tratamento de Erros**: Tratamento robusto
- **Logs**: Logs detalhados
- **Validações**: Validações adequadas
- **Backup**: Backups regulares
- **Recuperação**: Planos de recuperação

### **4. Segurança**
- **Privilégios**: Privilégios mínimos
- **Auditoria**: Logs de auditoria
- **Criptografia**: Dados sensíveis
- **Acesso**: Controle de acesso
- **Backup**: Backups seguros

## 🔍 Troubleshooting

### **Problemas Comuns**
- **Deadlock**: Resolver conflitos
- **Timeout**: Ajustar timeouts
- **Memory**: Otimizar memória
- **I/O**: Reduzir I/O
- **CPU**: Otimizar CPU

### **Soluções**
- **Logs**: Analisar logs
- **Monitoramento**: Usar ferramentas
- **Otimização**: Otimizar código
- **Recursos**: Ajustar recursos
- **Manutenção**: Manutenção preventiva

## 🚀 Evolução e Tendências

### **Melhorias Contínuas**
- **Automação**: Mais automação
- **Inteligência**: IA/ML
- **Real-time**: Tempo real
- **Cloud**: Migração cloud
- **Microservices**: Microserviços

### **Tendências Futuras**
- **DevOps**: Integração DevOps
- **Containerization**: Containerização
- **Kubernetes**: Orquestração
- **Serverless**: Serverless
- **Edge Computing**: Edge computing

---

*Este documento foi criado com base na documentação oficial do Sankhya Developer sobre Padrões de Automação e melhores práticas de desenvolvimento.*
