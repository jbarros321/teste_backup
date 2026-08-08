# 🛤️ Jornadas de Negócio Sankhya

## 🎯 Visão Geral

Este documento mapeia as principais jornadas de negócio no Sankhya, fornecendo passo a passo detalhado para implementação e configuração de processos essenciais. Cada jornada inclui configurações, validações e boas práticas.

## 💼 **JORNADA DE VENDAS**

### **📋 Visão Geral da Jornada**
```
Cliente → Pedido → Aprovação → Faturamento → Entrega → Cobrança
```

### **1. Cadastro de Cliente**

#### **Configurações Necessárias**
```sql
-- Parâmetros para cadastro de clientes
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PAR.CONTROLAR.LIMITE.CREDITO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PAR.VALIDAR.CPF.CNPJ';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PAR.OBRIGAR.ENDERECO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PAR.CONTROLAR.BLOQUEIO';
```

#### **Validações Automáticas**
```java
// Evento programado para validação de cliente
public class ValidacaoClienteEvent implements EventoProgramavelJava {
    
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO cliente = (DynamicVO) event.getVo();
        
        // Validar CPF/CNPJ
        validarDocumento(cliente.asString("CGCCPF"));
        
        // Validar endereço
        if (cliente.getProperty("ENDE") == null) {
            throw new Exception("Endereço é obrigatório");
        }
        
        // Validar limite de crédito
        BigDecimal limite = cliente.asBigDecimal("LIMCRED");
        if (limite == null || limite.compareTo(BigDecimal.ZERO) <= 0) {
            cliente.setProperty("LIMCRED", new BigDecimal("1000"));
        }
    }
    
    private void validarDocumento(String documento) throws Exception {
        // Implementar validação de CPF/CNPJ
        if (documento == null || documento.trim().isEmpty()) {
            throw new Exception("CPF/CNPJ é obrigatório");
        }
    }
}
```

### **2. Criação de Pedido**

#### **Configurações de Pedido**
```sql
-- Parâmetros para pedidos de venda
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VEN.VALIDAR.ESTOQUE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VEN.CONTROLAR.LIMITE.CREDITO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VEN.USAR.COMISSAO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VEN.CONTROLAR.PRECO.MINIMO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VEN.USAR.DESCONTO';
```

#### **Validações de Negócio**
```java
// Evento para validação de pedido
public class ValidacaoPedidoEvent implements EventoProgramavelJava {
    
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO pedido = (DynamicVO) event.getVo();
        
        // Validar cliente
        validarCliente(pedido.asBigDecimal("CODPARC"));
        
        // Validar produtos
        validarProdutos(pedido.asBigDecimal("NUNOTA"));
        
        // Validar limite de crédito
        validarLimiteCredito(pedido);
        
        // Calcular comissões
        calcularComissoes(pedido);
    }
    
    private void validarLimiteCredito(DynamicVO pedido) throws Exception {
        BigDecimal codParc = pedido.asBigDecimal("CODPARC");
        BigDecimal valorTotal = pedido.asBigDecimal("VLRNOTA");
        
        // Consultar limite de crédito do cliente
        String sql = "SELECT LIMCRED, SALDOCRED FROM TGFPAR WHERE CODPARC = ?";
        // Implementar validação...
    }
}
```

### **3. Aprovação de Pedido**

#### **Workflow de Aprovação**
```sql
-- Configuração de aprovação
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VEN.APROVACAO.OBRIGATORIA';
UPDATE TSIPAR SET TEXTO = '1000' WHERE CHAVE = 'VEN.VALOR.LIMITE.APROVACAO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VEN.APROVACAO.MULTIPLOS.NIVEIS';
```

#### **Botão de Ação para Aprovação**
```java
public class AprovacaoPedidoAction implements AcaoRotinaJava {
    
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();
        
        for (Registro linha : linhas) {
            BigDecimal nunota = linha.getField("NUNOTA");
            
            // Verificar se pode aprovar
            if (podeAprovar(nunota, contexto.getUsuarioLogado())) {
                aprovarPedido(nunota);
                contexto.setMensagemRetorno("Pedido aprovado com sucesso!");
            } else {
                throw new Exception("Usuário não tem permissão para aprovar este pedido");
            }
        }
    }
    
    private boolean podeAprovar(BigDecimal nunota, BigDecimal usuario) {
        // Verificar permissões de aprovação
        // Implementar lógica de aprovação...
        return true;
    }
}
```

### **4. Faturamento**

#### **Configurações de Faturamento**
```sql
-- Parâmetros para faturamento
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FAT.VALIDAR.ESTOQUE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FAT.GERAR.CONTAS.RECEBER';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FAT.CONTROLAR.SEQUENCIA.NF';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FAT.VALIDAR.IMPOSTOS';
```

#### **Processo de Faturamento**
```java
// Ação para faturamento
public class FaturamentoAction implements AcaoRotinaJava {
    
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();
        
        for (Registro linha : linhas) {
            BigDecimal nunota = linha.getField("NUNOTA");
            
            // Validar pedido
            validarPedidoParaFaturamento(nunota);
            
            // Faturar
            faturarPedido(nunota);
            
            // Gerar contas a receber
            gerarContasReceber(nunota);
            
            // Atualizar estoque
            atualizarEstoque(nunota);
        }
        
        contexto.setMensagemRetorno("Pedidos faturados com sucesso!");
    }
}
```

### **5. Entrega**

#### **Controle de Entrega**
```sql
-- Configurações de entrega
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'ENT.CONTROLAR.ENTREGA';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'ENT.VALIDAR.ENDERECO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'ENT.CONTROLAR.PRAZO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'ENT.GERAR.ROMANEIO';
```

## 🛒 **JORNADA DE COMPRAS**

### **📋 Visão Geral da Jornada**
```
Fornecedor → Pedido → Recebimento → Conferência → Aprovação → Pagamento
```

### **1. Cadastro de Fornecedor**

#### **Configurações de Fornecedor**
```sql
-- Parâmetros para fornecedores
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FOR.VALIDAR.CNPJ';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FOR.CONTROLAR.APROVACAO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FOR.VALIDAR.INSCRICAO.ESTADUAL';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FOR.CONTROLAR.BLOQUEIO';
```

### **2. Pedido de Compra**

#### **Validações de Compra**
```java
public class ValidacaoCompraEvent implements EventoProgramavelJava {
    
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO compra = (DynamicVO) event.getVo();
        
        // Validar fornecedor
        validarFornecedor(compra.asBigDecimal("CODPARC"));
        
        // Validar produtos
        validarProdutosCompra(compra.asBigDecimal("NUNOTA"));
        
        // Validar preços
        validarPrecosCompra(compra);
        
        // Verificar orçamento
        verificarOrcamento(compra);
    }
}
```

### **3. Recebimento**

#### **Controle de Recebimento**
```sql
-- Configurações de recebimento
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'REC.CONTROLAR.QUALIDADE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'REC.VALIDAR.QUANTIDADE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'REC.CONTROLAR.PRAZO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'REC.GERAR.ENTRADA.ESTOQUE';
```

## 📦 **JORNADA DE ESTOQUE**

### **📋 Visão Geral da Jornada**
```
Entrada → Controle → Movimentação → Inventário → Saída → Relatórios
```

### **1. Controle de Entrada**

#### **Configurações de Estoque**
```sql
-- Parâmetros de estoque
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'EST.CONTROLAR.ESTOQUE.NEGATIVO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'EST.USAR.CONTROLE.LOTE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'EST.USAR.VALIDADE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'EST.CONTROLAR.ROTATIVIDADE';
```

### **2. Movimentações**

#### **Controle de Movimentação**
```java
public class ControleEstoqueEvent implements EventoProgramavelJava {
    
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO movimento = (DynamicVO) event.getVo();
        
        // Atualizar saldo
        atualizarSaldoEstoque(movimento);
        
        // Verificar estoque mínimo
        verificarEstoqueMinimo(movimento);
        
        // Calcular rotatividade
        calcularRotatividade(movimento);
        
        // Gerar alertas
        gerarAlertasEstoque(movimento);
    }
}
```

## 💰 **JORNADA FINANCEIRA**

### **📋 Visão Geral da Jornada**
```
Contas a Pagar → Contas a Receber → Fluxo de Caixa → Conciliação → Relatórios
```

### **1. Contas a Pagar**

#### **Configurações Financeiras**
```sql
-- Parâmetros financeiros
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FIN.CONTROLAR.VENCIMENTO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FIN.USAR.CENTRO.CUSTO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FIN.CONTROLAR.CHEQUES';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FIN.USAR.CONCILIACAO.BANCARIA';
```

### **2. Contas a Receber**

#### **Controle de Recebimento**
```java
public class ContasReceberEvent implements EventoProgramavelJava {
    
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO conta = (DynamicVO) event.getVo();
        
        // Validar vencimento
        validarVencimento(conta.asDate("DTVENC"));
        
        // Calcular juros e multa
        calcularJurosMulta(conta);
        
        // Verificar limite de crédito
        verificarLimiteCredito(conta.asBigDecimal("CODPARC"));
    }
}
```

## 🏭 **JORNADA DE PRODUÇÃO**

### **📋 Visão Geral da Jornada**
```
Planejamento → Ordem → Produção → Controle → Finalização → Análise
```

### **1. Ordem de Produção**

#### **Configurações de Produção**
```sql
-- Parâmetros de produção
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PROD.USAR.ORDEM.PRODUCAO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PROD.CONTROLAR.RECEPCAO.MATERIA';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PROD.USAR.CONTROLE.QUALIDADE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PROD.CONTROLAR.CUSTOS.PRODUCAO';
```

## 📊 **RELATÓRIOS E INDICADORES**

### **1. Dashboards por Jornada**

#### **Dashboard de Vendas**
```xml
<!-- Configuração de dashboard para vendas -->
<gadget type="chart" title="Vendas por Período">
    <query>
        SELECT 
            TO_CHAR(DTEMISSAO, 'MM/YYYY') AS PERIODO,
            SUM(VLRNOTA) AS VALOR_TOTAL
        FROM TGFCAB 
        WHERE TIPMOV = 'V'
        AND DTEMISSAO >= ADD_MONTHS(SYSDATE, -12)
        GROUP BY TO_CHAR(DTEMISSAO, 'MM/YYYY')
        ORDER BY PERIODO
    </query>
</gadget>
```

#### **Dashboard de Estoque**
```xml
<!-- Configuração de dashboard para estoque -->
<gadget type="table" title="Produtos com Estoque Baixo">
    <query>
        SELECT 
            CODPROD,
            DESCRPROD,
            SALDOFISICO,
            ESTMIN
        FROM TGFPRO 
        WHERE SALDOFISICO <= ESTMIN
        ORDER BY (ESTMIN - SALDOFISICO) DESC
    </query>
</gadget>
```

## 🎯 **AUTOMAÇÃO DE JORNADAS**

### **1. Ações Agendadas**

#### **Job de Controle de Vendas**
```java
public class ControleVendasJob implements Runnable {
    
    public void run() {
        try {
            // Verificar pedidos em atraso
            verificarPedidosAtraso();
            
            // Verificar estoque baixo
            verificarEstoqueBaixo();
            
            // Gerar relatórios automáticos
            gerarRelatoriosAutomaticos();
            
            // Enviar alertas
            enviarAlertas();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### **2. Eventos Automáticos**

#### **Notificação de Vencimento**
```java
public class NotificacaoVencimentoEvent implements EventoProgramavelJava {
    
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO titulo = (DynamicVO) event.getVo();
        
        // Verificar se vence em 5 dias
        Date vencimento = titulo.asDate("DTVENC");
        long diasParaVencimento = (vencimento.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
        
        if (diasParaVencimento <= 5) {
            enviarNotificacaoVencimento(titulo);
        }
    }
}
```

## 🎊 **Benefícios das Jornadas Mapeadas**

### **✅ Eficiência:**
- **Processos Padronizados**: Fluxos bem definidos
- **Automação**: Redução de trabalho manual
- **Validações**: Prevenção de erros

### **✅ Controle:**
- **Visibilidade**: Acompanhamento completo
- **Auditoria**: Rastreabilidade total
- **Compliance**: Conformidade com regulamentações

### **✅ Performance:**
- **Otimização**: Processos otimizados
- **Relatórios**: Indicadores em tempo real
- **Alertas**: Notificações proativas

---

*Este documento mapeia as principais jornadas de negócio no Sankhya, fornecendo configurações, validações e automações para otimizar os processos empresariais.*
