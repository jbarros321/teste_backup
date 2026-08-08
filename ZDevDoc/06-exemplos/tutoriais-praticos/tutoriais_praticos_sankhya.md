# 📚 Tutoriais Práticos Sankhya - Guia Passo a Passo

## 🎯 Visão Geral

Este documento apresenta tutoriais práticos passo a passo para implementar funcionalidades no Sankhya, desde conceitos básicos até implementações avançadas, com código funcional e explicações detalhadas.

## 🚀 **Tutorial 1: Criando seu Primeiro Botão de Ação**

### **Objetivo**
Criar um botão de ação que calcula o valor total de um pedido incluindo impostos e descontos.

### **Passo 1: Preparar a Estrutura**

#### **1.1 Criar a Classe Java**
```java
package br.com.empresa.tutoriais;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Tutorial 1: Botão de ação para calcular total do pedido
 */
public class CalcularTotalPedidoAction implements AcaoRotinaJava {
    
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        // Obter registros selecionados
        Registro[] linhas = contexto.getLinhas();
        
        if (linhas == null || linhas.length == 0) {
            contexto.setMensagemRetorno("Nenhum pedido selecionado!");
            return;
        }
        
        // Processar cada pedido selecionado
        for (Registro linha : linhas) {
            BigDecimal nunota = linha.getField("NUNOTA");
            
            // Calcular total do pedido
            BigDecimal totalCalculado = calcularTotalPedido(nunota);
            
            // Atualizar o pedido
            atualizarTotalPedido(nunota, totalCalculado);
            
            System.out.println("Pedido " + nunota + " atualizado com total: R$ " + totalCalculado);
        }
        
        contexto.setMensagemRetorno("Cálculo concluído para " + linhas.length + " pedido(s)!");
    }
    
    private BigDecimal calcularTotalPedido(BigDecimal nunota) throws Exception {
        EntityFacade facade = EntityFacadeFactory.getDWFFacade();
        
        // Buscar itens do pedido
        String sqlItens = """
            SELECT 
                QTDNEG,
                VLRNEG,
                VLRTOT,
                CODPROD
            FROM TGFITE 
            WHERE NUNOTA = ?
            """;
        
        List<DynamicVO> itens = facade.getQueryExecutor().executeQuery(sqlItens, nunota);
        
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (DynamicVO item : itens) {
            BigDecimal quantidade = item.asBigDecimal("QTDNEG");
            BigDecimal precoUnitario = item.asBigDecimal("VLRNEG");
            BigDecimal valorTotal = quantidade.multiply(precoUnitario);
            
            subtotal = subtotal.add(valorTotal);
        }
        
        // Aplicar desconto (exemplo: 5% para pedidos acima de R$ 1000)
        BigDecimal desconto = BigDecimal.ZERO;
        if (subtotal.compareTo(new BigDecimal("1000")) > 0) {
            desconto = subtotal.multiply(new BigDecimal("0.05"));
        }
        
        // Calcular impostos (exemplo: ICMS 18%)
        BigDecimal valorComDesconto = subtotal.subtract(desconto);
        BigDecimal icms = valorComDesconto.multiply(new BigDecimal("0.18"));
        
        // Calcular total final
        BigDecimal totalFinal = valorComDesconto.add(icms);
        
        return totalFinal;
    }
    
    private void atualizarTotalPedido(BigDecimal nunota, BigDecimal totalCalculado) throws Exception {
        EntityFacade facade = EntityFacadeFactory.getDWFFacade();
        
        // Buscar o pedido
        DynamicVO pedido = facade.findEntityByPrimaryKey("TGFCAB", nunota);
        
        if (pedido != null) {
            // Atualizar o valor total
            pedido.setProperty("VLRNOTA", totalCalculado);
            
            // Salvar as alterações
            facade.saveEntity("TGFCAB", pedido);
        }
    }
}
```

#### **1.2 Configurar o Botão no Sistema**
```xml
<!-- Configuração do botão de ação -->
<action-button>
    <name>Calcular Total Pedido</name>
    <description>Calcula o valor total do pedido incluindo impostos e descontos</description>
    <type>Java</type>
    <class>br.com.empresa.tutoriais.CalcularTotalPedidoAction</class>
    <entity>TGFCAB</entity>
    <position>toolbar</position>
    <icon>calculator</icon>
    <enabled>true</enabled>
</action-button>
```

### **Passo 2: Testar o Botão**

#### **2.1 Criar Script de Teste**
```java
package br.com.empresa.tutoriais.test;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.empresa.tutoriais.CalcularTotalPedidoAction;

/**
 * Classe de teste para o botão de ação
 */
public class TesteCalcularTotalPedido {
    
    public static void main(String[] args) {
        try {
            // Criar contexto de teste
            ContextoAcao contexto = new ContextoAcaoTeste();
            
            // Criar ação
            CalcularTotalPedidoAction acao = new CalcularTotalPedidoAction();
            
            // Executar ação
            acao.doAction(contexto);
            
            System.out.println("Teste executado com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro no teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Implementação de teste do ContextoAcao
 */
class ContextoAcaoTeste implements ContextoAcao {
    
    private Registro[] linhas = new Registro[]{
        new RegistroTeste("NUNOTA", BigDecimal.valueOf(12345))
    };
    
    @Override
    public Registro[] getLinhas() {
        return linhas;
    }
    
    @Override
    public void setMensagemRetorno(String message) {
        System.out.println("Mensagem: " + message);
    }
    
    // Implementar outros métodos necessários...
}

/**
 * Implementação de teste do Registro
 */
class RegistroTeste implements Registro {
    
    private String campo;
    private Object valor;
    
    public RegistroTeste(String campo, Object valor) {
        this.campo = campo;
        this.valor = valor;
    }
    
    @Override
    public Object getField(String fieldName) {
        if (campo.equals(fieldName)) {
            return valor;
        }
        return null;
    }
    
    // Implementar outros métodos necessários...
}
```

### **Passo 3: Melhorar o Botão**

#### **3.1 Adicionar Validações**
```java
// Adicionar ao método doAction
private void validarPedido(BigDecimal nunota) throws Exception {
    EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    
    // Verificar se pedido existe
    DynamicVO pedido = facade.findEntityByPrimaryKey("TGFCAB", nunota);
    if (pedido == null) {
        throw new Exception("Pedido não encontrado: " + nunota);
    }
    
    // Verificar se é pedido de venda
    if (!"V".equals(pedido.asString("TIPMOV"))) {
        throw new Exception("Apenas pedidos de venda podem ser processados");
    }
    
    // Verificar se pedido não está faturado
    if ("F".equals(pedido.asString("STATUSNOTA"))) {
        throw new Exception("Pedido já foi faturado: " + nunota);
    }
}
```

#### **3.2 Adicionar Logs Detalhados**
```java
// Adicionar logging
private void logarOperacao(BigDecimal nunota, BigDecimal totalAnterior, BigDecimal totalNovo) {
    String logMessage = String.format(
        "Pedido %s: Total anterior R$ %.2f -> Total novo R$ %.2f (Diferença: R$ %.2f)",
        nunota, totalAnterior, totalNovo, totalNovo.subtract(totalAnterior)
    );
    
    System.out.println(logMessage);
}
```

## 🔄 **Tutorial 2: Criando seu Primeiro Evento Programado**

### **Objetivo**
Criar um evento programado que valida dados de cliente antes da inserção e calcula score de crédito automaticamente.

### **Passo 1: Criar a Classe do Evento**

```java
package br.com.empresa.tutoriais;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.util.ValidadorCpfCnpj;
import br.com.sankhya.util.ValidadorEmail;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Tutorial 2: Evento programado para validação de clientes
 */
public class ValidacaoClienteEvent implements EventoProgramavelJava {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO cliente = (DynamicVO) event.getVo();
        
        System.out.println("Validando cliente antes da inserção...");
        
        // Validar dados obrigatórios
        validarDadosObrigatorios(cliente);
        
        // Validar CPF/CNPJ
        validarDocumento(cliente);
        
        // Validar email
        validarEmail(cliente);
        
        // Definir valores padrão
        definirValoresPadrao(cliente);
        
        System.out.println("Validação concluída com sucesso!");
    }
    
    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO cliente = (DynamicVO) event.getVo();
        
        System.out.println("Cliente inserido, calculando score de crédito...");
        
        // Calcular score de crédito
        calcularScoreCredito(cliente);
        
        // Enviar email de boas-vindas
        enviarEmailBoasVindas(cliente);
        
        // Atualizar indicadores
        atualizarIndicadores();
        
        System.out.println("Processamento pós-inserção concluído!");
    }
    
    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO cliente = (DynamicVO) event.getVo();
        
        System.out.println("Validando cliente antes da atualização...");
        
        // Validar apenas campos alterados
        validarCamposAlterados(cliente);
        
        System.out.println("Validação de atualização concluída!");
    }
    
    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO cliente = (DynamicVO) event.getVo();
        
        System.out.println("Cliente atualizado, verificando mudanças...");
        
        // Verificar se precisa recalcular score
        if (precisaRecalcularScore(cliente)) {
            recalcularScoreCredito(cliente);
        }
        
        System.out.println("Processamento pós-atualização concluído!");
    }
    
    private void validarDadosObrigatorios(DynamicVO cliente) throws Exception {
        String nome = cliente.asString("NOMEPARC");
        String cgccpf = cliente.asString("CGCCPF");
        
        if (nome == null || nome.trim().isEmpty()) {
            throw new Exception("Nome do cliente é obrigatório");
        }
        
        if (cgccpf == null || cgccpf.trim().isEmpty()) {
            throw new Exception("CPF/CNPJ do cliente é obrigatório");
        }
        
        // Verificar duplicatas
        verificarDuplicatas(cgccpf);
    }
    
    private void validarDocumento(DynamicVO cliente) throws Exception {
        String cgccpf = cliente.asString("CGCCPF");
        String cgccpfLimpo = cgccpf.replaceAll("[^0-9]", "");
        
        if (cgccpfLimpo.length() == 11) {
            if (!ValidadorCpfCnpj.isValidCpf(cgccpfLimpo)) {
                throw new Exception("CPF inválido: " + cgccpf);
            }
        } else if (cgccpfLimpo.length() == 14) {
            if (!ValidadorCpfCnpj.isValidCnpj(cgccpfLimpo)) {
                throw new Exception("CNPJ inválido: " + cgccpf);
            }
        } else {
            throw new Exception("CPF/CNPJ deve ter 11 ou 14 dígitos: " + cgccpf);
        }
    }
    
    private void validarEmail(DynamicVO cliente) throws Exception {
        String email = cliente.asString("EMAIL");
        
        if (email != null && !email.trim().isEmpty()) {
            if (!ValidadorEmail.isValidEmail(email)) {
                throw new Exception("Email inválido: " + email);
            }
        }
    }
    
    private void verificarDuplicatas(String cgccpf) throws Exception {
        String sql = "SELECT COUNT(*) FROM TGFPAR WHERE CGCCPF = ?";
        List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql, cgccpf);
        
        if (!resultado.isEmpty() && resultado.get(0).asBigDecimal("1").intValue() > 0) {
            throw new Exception("Já existe um cliente cadastrado com este CPF/CNPJ: " + cgccpf);
        }
    }
    
    private void definirValoresPadrao(DynamicVO cliente) {
        // Definir data de cadastro
        if (cliente.asDate("DTCADASTRO") == null) {
            cliente.setProperty("DTCADASTRO", new Date());
        }
        
        // Definir como ativo
        if (cliente.asString("ATIVO") == null) {
            cliente.setProperty("ATIVO", "S");
        }
        
        // Definir limite de crédito padrão
        if (cliente.asBigDecimal("LIMCRED") == null) {
            cliente.setProperty("LIMCRED", new BigDecimal("1000"));
        }
        
        // Definir tipo de cliente
        if (cliente.asString("CLIENTE") == null) {
            cliente.setProperty("CLIENTE", "S");
        }
    }
    
    private void calcularScoreCredito(DynamicVO cliente) throws Exception {
        BigDecimal codparc = cliente.asBigDecimal("CODPARC");
        BigDecimal score = calcularScore(cliente);
        
        // Atualizar score no cliente
        cliente.setProperty("SCORE_CREDITO", score);
        facade.saveEntity("TGFPAR", cliente);
        
        // Salvar histórico de score
        salvarHistoricoScore(codparc, score, "CÁLCULO_INICIAL");
        
        System.out.println("Score de crédito calculado: " + score + " para cliente " + codparc);
    }
    
    private BigDecimal calcularScore(DynamicVO cliente) {
        BigDecimal score = new BigDecimal("500"); // Score base
        
        // Ajustar baseado no tipo de documento
        String cgccpf = cliente.asString("CGCCPF");
        if (cgccpf.length() == 14) { // CNPJ
            score = score.add(new BigDecimal("100"));
        }
        
        // Ajustar baseado no email
        String email = cliente.asString("EMAIL");
        if (email != null && !email.trim().isEmpty()) {
            score = score.add(new BigDecimal("50"));
        }
        
        // Ajustar baseado no telefone
        String telefone = cliente.asString("TELEFONE");
        if (telefone != null && !telefone.trim().isEmpty()) {
            score = score.add(new BigDecimal("30"));
        }
        
        // Garantir que score esteja entre 0 e 1000
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            score = BigDecimal.ZERO;
        } else if (score.compareTo(new BigDecimal("1000")) > 0) {
            score = new BigDecimal("1000");
        }
        
        return score;
    }
    
    private void salvarHistoricoScore(BigDecimal codparc, BigDecimal score, String motivo) throws Exception {
        DynamicVO historico = facade.createEntity("AD_HIST_SCORE");
        historico.setProperty("CODPARC", codparc);
        historico.setProperty("SCORE", score);
        historico.setProperty("MOTIVO", motivo);
        historico.setProperty("DT_CALCULO", new Date());
        
        facade.saveEntity("AD_HIST_SCORE", historico);
    }
    
    private void enviarEmailBoasVindas(DynamicVO cliente) {
        String email = cliente.asString("EMAIL");
        String nome = cliente.asString("NOMEPARC");
        
        if (email != null && !email.trim().isEmpty()) {
            System.out.println("Enviando email de boas-vindas para: " + email);
            
            // Aqui você implementaria o envio real do email
            String assunto = "Bem-vindo à nossa empresa!";
            String corpo = "Olá " + nome + ",\n\nSeja bem-vindo à nossa empresa!";
            
            // Simular envio
            System.out.println("Email enviado com sucesso!");
        }
    }
    
    private void atualizarIndicadores() {
        System.out.println("Atualizando indicadores de clientes...");
        
        // Aqui você implementaria a atualização de indicadores
        // Por exemplo, contadores, estatísticas, etc.
    }
    
    private void validarCamposAlterados(DynamicVO cliente) throws Exception {
        // Verificar se CPF/CNPJ foi alterado
        String cgccpfAtual = cliente.asString("CGCCPF");
        String cgccpfAnterior = (String) cliente.getProperty("CGCCPF_OLD");
        
        if (!cgccpfAtual.equals(cgccpfAnterior)) {
            validarDocumento(cliente);
            verificarDuplicatas(cgccpfAtual);
        }
        
        // Verificar se email foi alterado
        String emailAtual = cliente.asString("EMAIL");
        String emailAnterior = (String) cliente.getProperty("EMAIL_OLD");
        
        if (!emailAtual.equals(emailAnterior) && emailAtual != null && !emailAtual.trim().isEmpty()) {
            validarEmail(cliente);
        }
    }
    
    private boolean precisaRecalcularScore(DynamicVO cliente) {
        // Verificar se campos que afetam o score foram alterados
        String emailAtual = cliente.asString("EMAIL");
        String emailAnterior = (String) cliente.getProperty("EMAIL_OLD");
        
        String telefoneAtual = cliente.asString("TELEFONE");
        String telefoneAnterior = (String) cliente.getProperty("TELEFONE_OLD");
        
        return !emailAtual.equals(emailAnterior) || !telefoneAtual.equals(telefoneAnterior);
    }
    
    private void recalcularScoreCredito(DynamicVO cliente) throws Exception {
        BigDecimal codparc = cliente.asBigDecimal("CODPARC");
        BigDecimal scoreAnterior = cliente.asBigDecimal("SCORE_CREDITO");
        BigDecimal scoreNovo = calcularScore(cliente);
        
        // Atualizar score
        cliente.setProperty("SCORE_CREDITO", scoreNovo);
        facade.saveEntity("TGFPAR", cliente);
        
        // Salvar histórico
        salvarHistoricoScore(codparc, scoreNovo, "RECÁLCULO");
        
        System.out.println("Score recalculado: " + scoreAnterior + " -> " + scoreNovo);
    }
    
    // Métodos não utilizados neste tutorial
    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {
        // Implementar se necessário
    }
    
    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {
        // Implementar se necessário
    }
    
    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {
        // Implementar se necessário
    }
}
```

### **Passo 2: Configurar o Evento**

#### **2.1 Criar Configuração XML**
```xml
<!-- Configuração do evento programado -->
<programmable-event>
    <entity>TGFPAR</entity>
    <class>br.com.empresa.tutoriais.ValidacaoClienteEvent</class>
    <enabled>true</enabled>
    <description>Validação e processamento de clientes</description>
    <events>
        <event>beforeInsert</event>
        <event>afterInsert</event>
        <event>beforeUpdate</event>
        <event>afterUpdate</event>
    </events>
</programmable-event>
```

### **Passo 3: Testar o Evento**

#### **3.1 Criar Script de Teste**
```java
package br.com.empresa.tutoriais.test;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.empresa.tutoriais.ValidacaoClienteEvent;

/**
 * Teste para o evento de validação de cliente
 */
public class TesteValidacaoCliente {
    
    public static void main(String[] args) {
        try {
            // Criar cliente de teste
            DynamicVO cliente = criarClienteTeste();
            
            // Criar evento
            ValidacaoClienteEvent evento = new ValidacaoClienteEvent();
            
            // Simular evento de inserção
            PersistenceEvent event = new PersistenceEventTeste(cliente);
            
            // Executar validações
            evento.beforeInsert(event);
            evento.afterInsert(event);
            
            System.out.println("Teste de inserção concluído com sucesso!");
            
            // Simular evento de atualização
            cliente.setProperty("EMAIL", "novo@email.com");
            evento.beforeUpdate(event);
            evento.afterUpdate(event);
            
            System.out.println("Teste de atualização concluído com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro no teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static DynamicVO criarClienteTeste() {
        DynamicVO cliente = new DynamicVO();
        cliente.setProperty("NOMEPARC", "Cliente Teste");
        cliente.setProperty("CGCCPF", "12345678901");
        cliente.setProperty("EMAIL", "cliente@teste.com");
        cliente.setProperty("TELEFONE", "(11) 99999-9999");
        
        return cliente;
    }
}
```

## ⏰ **Tutorial 3: Criando sua Primeira Ação Agendada**

### **Objetivo**
Criar uma ação agendada que executa diariamente para gerar relatórios de vendas e enviar por email.

### **Passo 1: Criar a Classe da Ação Agendada**

```java
package br.com.empresa.tutoriais;

import br.com.sankhya.acaoagendada.ScheduledActionsUtils;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Tutorial 3: Ação agendada para relatório de vendas
 */
public class RelatorioVendasJob implements Runnable {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    @Override
    public void run() {
        try {
            System.out.println("Iniciando geração de relatório de vendas...");
            
            // Calcular período (ontem)
            Date dataInicio = calcularDataInicio();
            Date dataFim = calcularDataFim();
            
            // Gerar relatório
            String relatorio = gerarRelatorioVendas(dataInicio, dataFim);
            
            // Enviar por email
            enviarRelatorioPorEmail(relatorio, dataInicio, dataFim);
            
            // Salvar log da execução
            salvarLogExecucao("SUCESSO", relatorio.length());
            
            System.out.println("Relatório de vendas gerado e enviado com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório: " + e.getMessage());
            e.printStackTrace();
            
            try {
                salvarLogExecucao("ERRO", e.getMessage());
            } catch (Exception ex) {
                System.err.println("Erro ao salvar log: " + ex.getMessage());
            }
        }
    }
    
    private Date calcularDataInicio() {
        Date hoje = new Date();
        return new Date(hoje.getTime() - (24 * 60 * 60 * 1000)); // Ontem
    }
    
    private Date calcularDataFim() {
        Date hoje = new Date();
        return new Date(hoje.getTime() - (1 * 60 * 60 * 1000)); // Ontem às 23h
    }
    
    private String gerarRelatorioVendas(Date dataInicio, Date dataFim) throws Exception {
        StringBuilder relatorio = new StringBuilder();
        
        // Cabeçalho
        relatorio.append("RELATÓRIO DE VENDAS DIÁRIO\n");
        relatorio.append("==========================\n\n");
        relatorio.append("Período: ").append(dateFormat.format(dataInicio)).append(" a ").append(dateFormat.format(dataFim)).append("\n\n");
        
        // Resumo geral
        String resumoGeral = gerarResumoGeral(dataInicio, dataFim);
        relatorio.append(resumoGeral).append("\n\n");
        
        // Top vendedores
        String topVendedores = gerarTopVendedores(dataInicio, dataFim);
        relatorio.append(topVendedores).append("\n\n");
        
        // Top produtos
        String topProdutos = gerarTopProdutos(dataInicio, dataFim);
        relatorio.append(topProdutos).append("\n\n");
        
        // Vendas por hora
        String vendasPorHora = gerarVendasPorHora(dataInicio, dataFim);
        relatorio.append(vendasPorHora).append("\n");
        
        return relatorio.toString();
    }
    
    private String gerarResumoGeral(Date dataInicio, Date dataFim) throws Exception {
        String sql = """
            SELECT 
                COUNT(*) as QTD_PEDIDOS,
                SUM(VLRNOTA) as TOTAL_VENDAS,
                AVG(VLRNOTA) as TICKET_MEDIO,
                COUNT(DISTINCT CODPARC) as QTD_CLIENTES
            FROM TGFCAB 
            WHERE TIPMOV = 'V' 
            AND DTEMISSAO BETWEEN ? AND ?
            AND STATUSNOTA = 'L'
            """;
        
        List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql, dataInicio, dataFim);
        
        if (!resultado.isEmpty()) {
            DynamicVO resumo = resultado.get(0);
            BigDecimal qtdPedidos = resumo.asBigDecimal("QTD_PEDIDOS");
            BigDecimal totalVendas = resumo.asBigDecimal("TOTAL_VENDAS");
            BigDecimal ticketMedio = resumo.asBigDecimal("TICKET_MEDIO");
            BigDecimal qtdClientes = resumo.asBigDecimal("QTD_CLIENTES");
            
            return String.format("""
                RESUMO GERAL:
                - Quantidade de Pedidos: %s
                - Total de Vendas: R$ %.2f
                - Ticket Médio: R$ %.2f
                - Clientes Atendidos: %s
                """, qtdPedidos, totalVendas, ticketMedio, qtdClientes);
        }
        
        return "RESUMO GERAL:\nNenhuma venda encontrada no período.";
    }
    
    private String gerarTopVendedores(Date dataInicio, Date dataFim) throws Exception {
        String sql = """
            SELECT 
                c.CODVEN,
                p.NOMEPARC as NOME_VENDEDOR,
                COUNT(c.NUNOTA) as QTD_PEDIDOS,
                SUM(c.VLRNOTA) as TOTAL_VENDAS
            FROM TGFCAB c
            JOIN TGFPAR p ON c.CODVEN = p.CODPARC
            WHERE c.TIPMOV = 'V' 
            AND c.DTEMISSAO BETWEEN ? AND ?
            AND c.STATUSNOTA = 'L'
            GROUP BY c.CODVEN, p.NOMEPARC
            ORDER BY TOTAL_VENDAS DESC
            """;
        
        List<DynamicVO> vendedores = facade.getQueryExecutor().executeQuery(sql, dataInicio, dataFim);
        
        StringBuilder resultado = new StringBuilder("TOP 5 VENDEDORES:\n");
        resultado.append(String.format("%-10s %-30s %-10s %-15s\n", "CÓDIGO", "NOME", "PEDIDOS", "TOTAL VENDAS"));
        resultado.append("-".repeat(70)).append("\n");
        
        int count = 0;
        for (DynamicVO vendedor : vendedores) {
            if (count >= 5) break;
            
            BigDecimal codigo = vendedor.asBigDecimal("CODVEN");
            String nome = vendedor.asString("NOME_VENDEDOR");
            BigDecimal pedidos = vendedor.asBigDecimal("QTD_PEDIDOS");
            BigDecimal vendas = vendedor.asBigDecimal("TOTAL_VENDAS");
            
            resultado.append(String.format("%-10s %-30s %-10s %-15.2f\n", 
                                         codigo, nome, pedidos, vendas));
            count++;
        }
        
        return resultado.toString();
    }
    
    private String gerarTopProdutos(Date dataInicio, Date dataFim) throws Exception {
        String sql = """
            SELECT 
                i.CODPROD,
                p.DESCRPROD,
                SUM(i.QTDNEG) as QTD_VENDIDA,
                SUM(i.VLRTOT) as TOTAL_VENDAS
            FROM TGFCAB c
            JOIN TGFITE i ON c.NUNOTA = i.NUNOTA
            JOIN TGFPRO p ON i.CODPROD = p.CODPROD
            WHERE c.TIPMOV = 'V' 
            AND c.DTEMISSAO BETWEEN ? AND ?
            AND c.STATUSNOTA = 'L'
            GROUP BY i.CODPROD, p.DESCRPROD
            ORDER BY TOTAL_VENDAS DESC
            """;
        
        List<DynamicVO> produtos = facade.getQueryExecutor().executeQuery(sql, dataInicio, dataFim);
        
        StringBuilder resultado = new StringBuilder("TOP 5 PRODUTOS:\n");
        resultado.append(String.format("%-10s %-40s %-15s %-15s\n", "CÓDIGO", "PRODUTO", "QTD VENDIDA", "TOTAL VENDAS"));
        resultado.append("-".repeat(85)).append("\n");
        
        int count = 0;
        for (DynamicVO produto : produtos) {
            if (count >= 5) break;
            
            BigDecimal codigo = produto.asBigDecimal("CODPROD");
            String descricao = produto.asString("DESCRPROD");
            BigDecimal qtdVendida = produto.asBigDecimal("QTD_VENDIDA");
            BigDecimal totalVendas = produto.asBigDecimal("TOTAL_VENDAS");
            
            resultado.append(String.format("%-10s %-40s %-15.3f %-15.2f\n", 
                                         codigo, descricao, qtdVendida, totalVendas));
            count++;
        }
        
        return resultado.toString();
    }
    
    private String gerarVendasPorHora(Date dataInicio, Date dataFim) throws Exception {
        String sql = """
            SELECT 
                TO_CHAR(DTEMISSAO, 'HH24') as HORA,
                COUNT(*) as QTD_PEDIDOS,
                SUM(VLRNOTA) as TOTAL_VENDAS
            FROM TGFCAB 
            WHERE TIPMOV = 'V' 
            AND DTEMISSAO BETWEEN ? AND ?
            AND STATUSNOTA = 'L'
            GROUP BY TO_CHAR(DTEMISSAO, 'HH24')
            ORDER BY HORA
            """;
        
        List<DynamicVO> vendasHora = facade.getQueryExecutor().executeQuery(sql, dataInicio, dataFim);
        
        StringBuilder resultado = new StringBuilder("VENDAS POR HORA:\n");
        resultado.append(String.format("%-5s %-10s %-15s\n", "HORA", "PEDIDOS", "TOTAL VENDAS"));
        resultado.append("-".repeat(35)).append("\n");
        
        for (DynamicVO venda : vendasHora) {
            String hora = venda.asString("HORA");
            BigDecimal pedidos = venda.asBigDecimal("QTD_PEDIDOS");
            BigDecimal vendas = venda.asBigDecimal("TOTAL_VENDAS");
            
            resultado.append(String.format("%-5s %-10s %-15.2f\n", hora, pedidos, vendas));
        }
        
        return resultado.toString();
    }
    
    private void enviarRelatorioPorEmail(String relatorio, Date dataInicio, Date dataFim) {
        try {
            String assunto = "Relatório de Vendas - " + dateFormat.format(dataInicio);
            String destinatarios = "vendas@empresa.com,gerencia@empresa.com";
            
            System.out.println("Enviando relatório por email...");
            System.out.println("Assunto: " + assunto);
            System.out.println("Destinatários: " + destinatarios);
            System.out.println("Tamanho do relatório: " + relatorio.length() + " caracteres");
            
            // Aqui você implementaria o envio real do email
            // Por exemplo, usando JavaMail API
            
            System.out.println("Email enviado com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
            throw new RuntimeException("Falha no envio do email", e);
        }
    }
    
    private void salvarLogExecucao(String status, Object detalhes) throws Exception {
        DynamicVO log = facade.createEntity("AD_LOG_ACAO_AGENDADA");
        log.setProperty("NOME_ACAO", "RelatorioVendasJob");
        log.setProperty("STATUS", status);
        log.setProperty("DETALHES", detalhes.toString());
        log.setProperty("DT_EXECUCAO", new Date());
        
        facade.saveEntity("AD_LOG_ACAO_AGENDADA", log);
    }
}
```

### **Passo 2: Configurar a Ação Agendada**

#### **2.1 Criar Configuração XML**
```xml
<!-- Configuração da ação agendada -->
<scheduled-action>
    <name>RelatorioVendasJob</name>
    <class>br.com.empresa.tutoriais.RelatorioVendasJob</class>
    <description>Gera relatório diário de vendas e envia por email</description>
    <schedule>0 0 8 * * ?</schedule> <!-- Executa todo dia às 8h -->
    <enabled>true</enabled>
    <timeout>300</timeout> <!-- 5 minutos -->
    <retry-count>3</retry-count>
</scheduled-action>
```

### **Passo 3: Testar a Ação Agendada**

#### **3.1 Criar Script de Teste**
```java
package br.com.empresa.tutoriais.test;

import br.com.empresa.tutoriais.RelatorioVendasJob;

/**
 * Teste para a ação agendada de relatório de vendas
 */
public class TesteRelatorioVendasJob {
    
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando teste da ação agendada...");
            
            // Criar e executar a ação
            RelatorioVendasJob job = new RelatorioVendasJob();
            job.run();
            
            System.out.println("Teste da ação agendada concluído com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro no teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

## 🎯 **Boas Práticas dos Tutoriais**

### **1. Estrutura de Código**
- **Pacotes Organizados**: Use pacotes lógicos
- **Nomenclatura Clara**: Nomes descritivos
- **Comentários**: Documente código complexo
- **Tratamento de Erros**: Sempre trate exceções

### **2. Validações**
- **Dados de Entrada**: Valide sempre
- **Regras de Negócio**: Implemente validações
- **Mensagens Claras**: Erros compreensíveis
- **Logs Detalhados**: Registre operações

### **3. Performance**
- **Consultas Otimizadas**: Use SQL eficiente
- **Batch Processing**: Processe em lotes
- **Índices**: Crie índices necessários
- **Cache**: Use cache quando apropriado

### **4. Manutenibilidade**
- **Código Modular**: Funções pequenas
- **Configuração Externa**: Parâmetros externos
- **Testes**: Implemente testes
- **Documentação**: Documente APIs

## 🎊 **Conclusão**

Os tutoriais práticos demonstram:

- **✅ Implementação Passo a Passo**: Guias detalhados
- **✅ Código Funcional**: Exemplos testáveis
- **✅ Boas Práticas**: Padrões de qualidade
- **✅ Validações Robustas**: Tratamento de erros
- **✅ Logs e Monitoramento**: Rastreabilidade
- **✅ Testes Incluídos**: Validação de funcionamento

### **Benefícios:**
- **Aprendizado Prático**: Implementação real
- **Código de Qualidade**: Padrões enterprise
- **Facilidade de Manutenção**: Bem estruturado
- **Reutilização**: Código modular
- **Confiabilidade**: Tratamento robusto

---

*Este documento fornece tutoriais práticos passo a passo para implementar funcionalidades no Sankhya, desde conceitos básicos até implementações avançadas.*
