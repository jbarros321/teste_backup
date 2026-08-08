# 🗄️ Procedures, Triggers e Queries SQL - Guia Completo Sankhya

## 🎯 Visão Geral

Este guia apresenta padrões completos para desenvolvimento de **Procedures**, **Triggers** e **Queries SQL** no ambiente Sankhya, incluindo templates, exemplos práticos e melhores práticas para automação e validação de dados.

## 🔧 Procedures de Botão de Ação

### **Template Base de Procedure**
```sql
CREATE OR REPLACE PROCEDURE SANKHYA.STP_NOME_PROCEDURE (
       P_CODUSU NUMBER,        -- Código do usuário logado
       P_IDSESSAO VARCHAR2,    -- Identificador da execução
       P_QTDLINHAS NUMBER,     -- Quantidade de registros selecionados
       P_MENSAGEM OUT VARCHAR2 -- Mensagem de retorno
) AS
       -- Variáveis locais
       FIELD_NUNOTA     NUMBER;
       FIELD_CODPROD    NUMBER;
       P_COUNT          NUMBER;
       P_USUARIO        NUMBER;
       P_DT_LOG         DATE;
       PARAM_P_STATUS   VARCHAR2(10);
       
BEGIN
    -- Obter parâmetros do formulário
    PARAM_P_STATUS := ACT_TXT_PARAM(P_IDSESSAO, 'P_STATUS');
    
    -- Loop pelos registros selecionados
    FOR I IN 1..P_QTDLINHAS LOOP
        -- Obter valores dos campos
        FIELD_NUNOTA := ACT_INT_FIELD(P_IDSESSAO, I, 'NUNOTA');
        FIELD_CODPROD := ACT_INT_FIELD(P_IDSESSAO, I, 'CODPROD');
        P_USUARIO := STP_GET_CODUSULOGADO;
        P_DT_LOG := SYSDATE;
        
        -- Lógica da procedure
        -- ...
        
    END LOOP;
    
    P_MENSAGEM := 'Operação executada com sucesso!';
    
EXCEPTION
    WHEN OTHERS THEN
        P_MENSAGEM := 'Erro: ' || SQLERRM;
END;
/
```

### **Procedure de Marcação de Entrega**
```sql
CREATE OR REPLACE PROCEDURE SANKHYA.STP_MARCAR_ENTREGU (
       P_CODUSU NUMBER,
       P_IDSESSAO VARCHAR2,
       P_QTDLINHAS NUMBER,
       P_MENSAGEM OUT VARCHAR2
) AS
       FIELD_NUNOTA     NUMBER;
       FIELD_CODPROD    NUMBER;
       P_COUNT          NUMBER;
       P_COUNT2         NUMBER;
       P_USUARIO        NUMBER;
       P_DT_LOG         DATE;
       PARAM_P_STATUS   VARCHAR2(10);
       PARAM_P_MSG_SOLICITANTE VARCHAR2(200);
       P_ANEXO          NUMBER;
       P_ANEXO_VALIDO   NUMBER;
      
BEGIN
    PARAM_P_STATUS := ACT_TXT_PARAM(P_IDSESSAO, 'P_STATUS');
    PARAM_P_MSG_SOLICITANTE := ACT_TXT_PARAM(P_IDSESSAO, 'P_MSG_SOLICITANTE');
    
    FOR I IN 1..P_QTDLINHAS LOOP
        FIELD_NUNOTA := ACT_INT_FIELD(P_IDSESSAO, I, 'NUNOTA');
        FIELD_CODPROD := ACT_INT_FIELD(P_IDSESSAO, I, 'CODPROD');
        P_USUARIO := STP_GET_CODUSULOGADO;
        P_DT_LOG := SYSDATE;
        
        -- Verificar se existe anexo
        SELECT COUNT(*) INTO P_ANEXO FROM TSIATA WHERE CODATA = FIELD_NUNOTA;
        
        IF PARAM_P_STATUS = 'N' THEN
            P_MENSAGEM := 'Operação cancelada!';
            RETURN;
        ELSE
            IF P_ANEXO = 0 THEN
                P_MENSAGEM := 'Adicionar anexo que comprove o recebimento da mercadoria!';
                RETURN;
            ELSE
                IF FIELD_CODPROD IS NULL THEN
                    -- Atualização no cabeçalho
                    SELECT COUNT(*) INTO P_COUNT2 
                    FROM TGFCAB 
                    WHERE NUNOTA = FIELD_NUNOTA AND PENDENTE = 'N';
                    
                    IF P_COUNT2 > 0 THEN
                        P_MENSAGEM := 'Nota fiscal já processada!';
                        RETURN;
                    END IF;
                    
                    -- Marcar como entregue
                    UPDATE TGFCAB 
                    SET PENDENTE = 'N',
                        DTENTSAI = P_DT_LOG,
                        USUENTSAI = P_USUARIO
                    WHERE NUNOTA = FIELD_NUNOTA;
                    
                    -- Log da operação
                    INSERT INTO TSIATA (
                        CODATA, TIPO, DESCRICAO, USUARIO, DATA
                    ) VALUES (
                        FIELD_NUNOTA, 'LOG', 
                        'Mercadoria marcada como entregue - ' || PARAM_P_MSG_SOLICITANTE,
                        P_USUARIO, P_DT_LOG
                    );
                    
                ELSE
                    -- Atualização no item
                    SELECT COUNT(*) INTO P_COUNT 
                    FROM TGFITE 
                    WHERE NUNOTA = FIELD_NUNOTA 
                    AND CODPROD = FIELD_CODPROD 
                    AND PENDENTE = 'N';
                    
                    IF P_COUNT > 0 THEN
                        P_MENSAGEM := 'Item já processado!';
                        RETURN;
                    END IF;
                    
                    -- Marcar item como entregue
                    UPDATE TGFITE 
                    SET PENDENTE = 'N',
                        DTENTSAI = P_DT_LOG,
                        USUENTSAI = P_USUARIO
                    WHERE NUNOTA = FIELD_NUNOTA 
                    AND CODPROD = FIELD_CODPROD;
                    
                    -- Log da operação
                    INSERT INTO TSIATA (
                        CODATA, TIPO, DESCRICAO, USUARIO, DATA
                    ) VALUES (
                        FIELD_NUNOTA, 'LOG', 
                        'Item ' || FIELD_CODPROD || ' marcado como entregue - ' || PARAM_P_MSG_SOLICITANTE,
                        P_USUARIO, P_DT_LOG
                    );
                END IF;
            END IF;
        END IF;
    END LOOP;
    
    P_MENSAGEM := 'Operação executada com sucesso!';
    
EXCEPTION
    WHEN OTHERS THEN
        P_MENSAGEM := 'Erro: ' || SQLERRM;
END;
/
```

### **Procedure de Validação de Estoque**
```sql
CREATE OR REPLACE PROCEDURE SANKHYA.STP_VALIDAR_ESTOQUE (
       P_CODUSU NUMBER,
       P_IDSESSAO VARCHAR2,
       P_QTDLINHAS NUMBER,
       P_MENSAGEM OUT VARCHAR2
) AS
       FIELD_NUNOTA     NUMBER;
       FIELD_CODPROD    NUMBER;
       FIELD_QTDNEG     NUMBER;
       P_ESTOQUE_ATUAL  NUMBER;
       P_ESTOQUE_MIN    NUMBER;
       P_PRODUTO        VARCHAR2(100);
       P_ERRO           VARCHAR2(4000);
       
BEGIN
    P_ERRO := '';
    
    FOR I IN 1..P_QTDLINHAS LOOP
        FIELD_NUNOTA := ACT_INT_FIELD(P_IDSESSAO, I, 'NUNOTA');
        FIELD_CODPROD := ACT_INT_FIELD(P_IDSESSAO, I, 'CODPROD');
        FIELD_QTDNEG := ACT_NUM_FIELD(P_IDSESSAO, I, 'QTDNEG');
        
        -- Obter informações do produto
        SELECT DESCRPROD INTO P_PRODUTO 
        FROM TGFPRO 
        WHERE CODPROD = FIELD_CODPROD;
        
        -- Verificar estoque atual
        SELECT NVL(ESTOQUE, 0) INTO P_ESTOQUE_ATUAL
        FROM TGFEST 
        WHERE CODPROD = FIELD_CODPROD;
        
        -- Verificar estoque mínimo
        SELECT NVL(ESTOQUEMIN, 0) INTO P_ESTOQUE_MIN
        FROM TGFPRO 
        WHERE CODPROD = FIELD_CODPROD;
        
        -- Validar disponibilidade
        IF P_ESTOQUE_ATUAL < FIELD_QTDNEG THEN
            P_ERRO := P_ERRO || 'Produto ' || P_PRODUTO || 
                     ' - Estoque insuficiente (Disponível: ' || P_ESTOQUE_ATUAL || 
                     ', Solicitado: ' || FIELD_QTDNEG || ')' || CHR(10);
        END IF;
        
        -- Verificar estoque mínimo
        IF (P_ESTOQUE_ATUAL - FIELD_QTDNEG) < P_ESTOQUE_MIN THEN
            P_ERRO := P_ERRO || 'Produto ' || P_PRODUTO || 
                     ' - Estoque ficará abaixo do mínimo após a venda' || CHR(10);
        END IF;
    END LOOP;
    
    IF P_ERRO IS NOT NULL THEN
        P_MENSAGEM := 'Validação de estoque falhou:' || CHR(10) || P_ERRO;
    ELSE
        P_MENSAGEM := 'Validação de estoque aprovada!';
    END IF;
    
EXCEPTION
    WHEN OTHERS THEN
        P_MENSAGEM := 'Erro na validação: ' || SQLERRM;
END;
/
```

## 🔄 Triggers de Validação

### **Trigger de Validação de Pedido**
```sql
CREATE OR REPLACE TRIGGER TRG_VALIDAR_PEDIDO
    BEFORE INSERT OR UPDATE ON TGFCAB
    FOR EACH ROW
DECLARE
    P_COUNT NUMBER;
    P_CLIENTE VARCHAR2(100);
BEGIN
    -- Validar se cliente está ativo
    SELECT COUNT(*) INTO P_COUNT
    FROM TGFPAR
    WHERE CODPARC = :NEW.CODPARC
    AND ATIVO = 'S';
    
    IF P_COUNT = 0 THEN
        SELECT NOMEPARC INTO P_CLIENTE
        FROM TGFPAR
        WHERE CODPARC = :NEW.CODPARC;
        
        RAISE_APPLICATION_ERROR(-20001, 
            'Cliente ' || P_CLIENTE || ' está inativo!');
    END IF;
    
    -- Validar data de negociação
    IF :NEW.DTNEG > SYSDATE THEN
        RAISE_APPLICATION_ERROR(-20002, 
            'Data de negociação não pode ser futura!');
    END IF;
    
    -- Validar valor mínimo
    IF :NEW.VLRNOTA < 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 
            'Valor da nota não pode ser negativo!');
    END IF;
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20000, 
            'Erro na validação: ' || SQLERRM);
END;
/
```

### **Trigger de Auditoria**
```sql
CREATE OR REPLACE TRIGGER TRG_AUDITORIA_CAB
    AFTER INSERT OR UPDATE OR DELETE ON TGFCAB
    FOR EACH ROW
DECLARE
    P_OPERACAO VARCHAR2(10);
    P_USUARIO NUMBER;
BEGIN
    P_USUARIO := STP_GET_CODUSULOGADO;
    
    -- Determinar operação
    IF INSERTING THEN
        P_OPERACAO := 'INSERT';
    ELSIF UPDATING THEN
        P_OPERACAO := 'UPDATE';
    ELSIF DELETING THEN
        P_OPERACAO := 'DELETE';
    END IF;
    
    -- Inserir log de auditoria
    INSERT INTO TSIATA (
        CODATA, TIPO, DESCRICAO, USUARIO, DATA, OPERACAO
    ) VALUES (
        NVL(:NEW.NUNOTA, :OLD.NUNOTA),
        'AUDIT',
        'Operação: ' || P_OPERACAO || ' - Nota: ' || NVL(:NEW.NUNOTA, :OLD.NUNOTA),
        P_USUARIO,
        SYSDATE,
        P_OPERACAO
    );
    
EXCEPTION
    WHEN OTHERS THEN
        -- Log de erro sem interromper operação
        INSERT INTO TSIATA (
            CODATA, TIPO, DESCRICAO, USUARIO, DATA
        ) VALUES (
            NVL(:NEW.NUNOTA, :OLD.NUNOTA),
            'ERROR',
            'Erro no trigger de auditoria: ' || SQLERRM,
            P_USUARIO,
            SYSDATE
        );
END;
/
```

## 📊 Queries Complexas

### **Query de Análise de Vendas**
```sql
-- Análise de vendas por período e cliente
SELECT 
    PAR.NOMEPARC AS CLIENTE,
    TO_CHAR(CAB.DTNEG, 'YYYY-MM') AS PERIODO,
    COUNT(CAB.NUNOTA) AS QTD_PEDIDOS,
    SUM(CAB.VLRNOTA) AS VALOR_TOTAL,
    AVG(CAB.VLRNOTA) AS TICKET_MEDIO,
    MAX(CAB.VLRNOTA) AS MAIOR_PEDIDO,
    MIN(CAB.VLRNOTA) AS MENOR_PEDIDO,
    ROUND(
        (SUM(CAB.VLRNOTA) / 
         SUM(SUM(CAB.VLRNOTA)) OVER (PARTITION BY TO_CHAR(CAB.DTNEG, 'YYYY-MM'))
        ) * 100, 2
    ) AS PARTICIPACAO_PERCENTUAL
FROM TGFCAB CAB
INNER JOIN TGFPAR PAR ON CAB.CODPARC = PAR.CODPARC
WHERE CAB.TIPMOV = 'V'
AND CAB.DTNEG >= ADD_MONTHS(SYSDATE, -12)
AND CAB.PENDENTE = 'N'
GROUP BY PAR.NOMEPARC, TO_CHAR(CAB.DTNEG, 'YYYY-MM')
HAVING SUM(CAB.VLRNOTA) > 1000
ORDER BY PERIODO DESC, VALOR_TOTAL DESC;
```

### **Query de Análise de Produtos**
```sql
-- Análise de performance de produtos
SELECT 
    PRO.CODPROD,
    PRO.DESCRPROD AS PRODUTO,
    PRO.UNIDADE,
    NVL(EST.ESTOQUE, 0) AS ESTOQUE_ATUAL,
    PRO.ESTOQUEMIN AS ESTOQUE_MINIMO,
    PRO.ESTOQUEMAX AS ESTOQUE_MAXIMO,
    COUNT(ITE.NUNOTA) AS QTD_VENDAS,
    SUM(ITE.QTDNEG) AS QTD_VENDIDA,
    SUM(ITE.VLRTOT) AS VALOR_VENDIDO,
    AVG(ITE.VLRUNIT) AS PRECO_MEDIO,
    ROUND(
        (SUM(ITE.QTDNEG) / NULLIF(NVL(EST.ESTOQUE, 0), 0)) * 100, 2
    ) AS GIRO_ESTOQUE,
    CASE 
        WHEN NVL(EST.ESTOQUE, 0) <= PRO.ESTOQUEMIN THEN 'CRÍTICO'
        WHEN NVL(EST.ESTOQUE, 0) <= (PRO.ESTOQUEMIN * 1.5) THEN 'BAIXO'
        WHEN NVL(EST.ESTOQUE, 0) >= PRO.ESTOQUEMAX THEN 'ALTO'
        ELSE 'NORMAL'
    END AS STATUS_ESTOQUE
FROM TGFPRO PRO
LEFT JOIN TGFEST EST ON PRO.CODPROD = EST.CODPROD
LEFT JOIN TGFITE ITE ON PRO.CODPROD = ITE.CODPROD
LEFT JOIN TGFCAB CAB ON ITE.NUNOTA = CAB.NUNOTA
WHERE CAB.TIPMOV = 'V'
AND CAB.DTNEG >= ADD_MONTHS(SYSDATE, -6)
AND CAB.PENDENTE = 'N'
GROUP BY PRO.CODPROD, PRO.DESCRPROD, PRO.UNIDADE, 
         EST.ESTOQUE, PRO.ESTOQUEMIN, PRO.ESTOQUEMAX
ORDER BY VALOR_VENDIDO DESC;
```

### **Query de Análise Financeira**
```sql
-- Análise de contas a receber
SELECT 
    PAR.NOMEPARC AS CLIENTE,
    TIT.NUMTITULO AS TITULO,
    TIT.DTVENC AS VENCIMENTO,
    TIT.VLRORIGINAL AS VALOR_ORIGINAL,
    TIT.VLRSALDO AS SALDO_DEVEDOR,
    TIT.DTEMISSAO AS DATA_EMISSAO,
    CASE 
        WHEN TIT.DTVENC < SYSDATE THEN 'VENCIDO'
        WHEN TIT.DTVENC <= (SYSDATE + 7) THEN 'VENCE EM 7 DIAS'
        WHEN TIT.DTVENC <= (SYSDATE + 30) THEN 'VENCE EM 30 DIAS'
        ELSE 'FUTURO'
    END AS STATUS_VENCIMENTO,
    CASE 
        WHEN TIT.DTVENC < SYSDATE THEN 
            ROUND((SYSDATE - TIT.DTVENC) / 30, 1)
        ELSE 0
    END AS MESES_ATRASO,
    ROUND(
        (TIT.VLRSALDO / TIT.VLRORIGINAL) * 100, 2
    ) AS PERCENTUAL_PAGO
FROM TGFCPR TIT
INNER JOIN TGFPAR PAR ON TIT.CODPARC = PAR.CODPARC
WHERE TIT.VLRSALDO > 0
AND TIT.DTEMISSAO >= ADD_MONTHS(SYSDATE, -12)
ORDER BY TIT.DTVENC ASC, TIT.VLRSALDO DESC;
```

## 🔍 Views e Consultas Otimizadas

### **View de Dashboard de Vendas**
```sql
CREATE OR REPLACE VIEW VW_DASHBOARD_VENDAS AS
SELECT 
    TO_CHAR(CAB.DTNEG, 'YYYY-MM') AS PERIODO,
    COUNT(CAB.NUNOTA) AS TOTAL_PEDIDOS,
    SUM(CAB.VLRNOTA) AS FATURAMENTO_TOTAL,
    AVG(CAB.VLRNOTA) AS TICKET_MEDIO,
    COUNT(DISTINCT CAB.CODPARC) AS CLIENTES_ATENDIDOS,
    COUNT(DISTINCT ITE.CODPROD) AS PRODUTOS_VENDIDOS,
    SUM(ITE.QTDNEG) AS QUANTIDADE_TOTAL,
    ROUND(
        (SUM(CAB.VLRNOTA) - LAG(SUM(CAB.VLRNOTA)) OVER (ORDER BY TO_CHAR(CAB.DTNEG, 'YYYY-MM'))) /
        NULLIF(LAG(SUM(CAB.VLRNOTA)) OVER (ORDER BY TO_CHAR(CAB.DTNEG, 'YYYY-MM')), 0) * 100, 2
    ) AS CRESCIMENTO_PERCENTUAL
FROM TGFCAB CAB
INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
WHERE CAB.TIPMOV = 'V'
AND CAB.PENDENTE = 'N'
GROUP BY TO_CHAR(CAB.DTNEG, 'YYYY-MM')
ORDER BY PERIODO DESC;
```

### **View de Análise de Clientes**
```sql
CREATE OR REPLACE VIEW VW_ANALISE_CLIENTES AS
SELECT 
    PAR.CODPARC,
    PAR.NOMEPARC AS CLIENTE,
    PAR.CGC_CPF AS DOCUMENTO,
    PAR.ATIVO,
    COUNT(CAB.NUNOTA) AS TOTAL_PEDIDOS,
    SUM(CAB.VLRNOTA) AS VALOR_TOTAL_COMPRAS,
    AVG(CAB.VLRNOTA) AS TICKET_MEDIO,
    MAX(CAB.DTNEG) AS ULTIMA_COMPRA,
    MIN(CAB.DTNEG) AS PRIMEIRA_COMPRA,
    ROUND(
        (MAX(CAB.DTNEG) - MIN(CAB.DTNEG)) / 30, 1
    ) AS TEMPO_RELACIONAMENTO_MESES,
    CASE 
        WHEN SUM(CAB.VLRNOTA) >= 100000 THEN 'PREMIUM'
        WHEN SUM(CAB.VLRNOTA) >= 50000 THEN 'GOLD'
        WHEN SUM(CAB.VLRNOTA) >= 10000 THEN 'SILVER'
        ELSE 'BRONZE'
    END AS CATEGORIA_CLIENTE
FROM TGFPAR PAR
LEFT JOIN TGFCAB CAB ON PAR.CODPARC = CAB.CODPARC
WHERE CAB.TIPMOV = 'V'
AND CAB.PENDENTE = 'N'
GROUP BY PAR.CODPARC, PAR.NOMEPARC, PAR.CGC_CPF, PAR.ATIVO
ORDER BY VALOR_TOTAL_COMPRAS DESC;
```

## 🛠️ Boas Práticas

### **1. Performance**
- **Índices**: Criar índices em campos frequentemente consultados
- **Hints**: Usar hints Oracle quando necessário
- **Estatísticas**: Manter estatísticas atualizadas
- **Partitioning**: Usar particionamento para tabelas grandes
- **Compressão**: Usar compressão de dados

### **2. Segurança**
- **Validação**: Validar todos os dados de entrada
- **Privilégios**: Usar privilégios mínimos necessários
- **Auditoria**: Implementar logs de auditoria
- **Criptografia**: Criptografar dados sensíveis
- **Backup**: Manter backups regulares

### **3. Manutenibilidade**
- **Nomenclatura**: Usar nomes descritivos
- **Documentação**: Documentar procedures complexas
- **Versionamento**: Controle de versão do código
- **Testes**: Testes automatizados
- **Padrões**: Seguir padrões de codificação

### **4. Tratamento de Erros**
- **Exception Handling**: Tratamento adequado de exceções
- **Logs**: Logs detalhados de erros
- **Rollback**: Transações com rollback
- **Validação**: Validação de dados
- **Monitoramento**: Monitoramento de performance

## 🔍 Troubleshooting

### **Problemas Comuns**
- **Deadlock**: Resolver conflitos de lock
- **Timeout**: Ajustar timeouts de consulta
- **Memory**: Otimizar uso de memória
- **I/O**: Reduzir operações de I/O
- **CPU**: Otimizar uso de CPU

### **Soluções**
- **Plans**: Analisar planos de execução
- **Stats**: Verificar estatísticas de tabelas
- **Indexes**: Revisar índices
- **Queries**: Otimizar consultas
- **Hardware**: Considerar upgrade de hardware

## 🚀 Evolução e Tendências

### **Melhorias Contínuas**
- **Novos Recursos**: Novos recursos Oracle
- **Performance**: Otimizações de performance
- **Segurança**: Melhorias de segurança
- **Automação**: Mais automação
- **Cloud**: Migração para cloud

### **Tendências Futuras**
- **Machine Learning**: Integração com ML
- **Real-time**: Processamento em tempo real
- **Microservices**: Arquitetura de microserviços
- **Containerization**: Containerização
- **DevOps**: Integração DevOps

---

*Este documento foi criado com base na documentação oficial do Sankhya Developer sobre Procedures, Triggers e Queries SQL e melhores práticas de desenvolvimento.*
