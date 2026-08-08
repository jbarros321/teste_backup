# 🔄 Rotinas de Botões de Ação Sankhya

## 🎯 Visão Geral

Este documento apresenta diferentes tipos de rotinas para botões de ação no Sankhya, extraídas do código fonte SankhyaW 4.8 e implementações reais do sistema.

## 🚀 **Tipos de Rotinas**

### **1. Rotina Lançador (LC)**

#### **Descrição**
Abre ou navega para outras telas do sistema.

#### **Implementação**
```java
package br.com.empresa.rotinas;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

/**
 * Rotina lançador para abrir telas do sistema
 */
public class RotinaLancadorAction implements AcaoRotinaJava {
    
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        // Obter parâmetros
        String resourceId = (String) contexto.getParam("resourceId");
        String parametros = (String) contexto.getParam("parametros");
        
        // Validar parâmetros
        if (resourceId == null || resourceId.trim().isEmpty()) {
            contexto.setMensagemRetorno("Resource ID é obrigatório!");
            return;
        }
        
        // Abrir tela
        abrirTela(resourceId, parametros, contexto);
        
        contexto.setMensagemRetorno("Tela aberta com sucesso!");
    }
    
    private void abrirTela(String resourceId, String parametros, ContextoAcao contexto) throws Exception {
        try {
            // Simular abertura de tela
            System.out.println("Abrindo tela: " + resourceId);
            
            if (parametros != null && !parametros.trim().isEmpty()) {
                System.out.println("Parâmetros: " + parametros);
            }
            
            // Em implementação real, usar o framework de navegação do Sankhya
            // Por exemplo: Navigator.openScreen(resourceId, parametros);
            
        } catch (Exception e) {
            throw new Exception("Erro ao abrir tela " + resourceId + ": " + e.getMessage());
        }
    }
}

/**
 * Exemplos de uso da rotina lançador
 */
class ExemplosRotinaLancador {
    
    /**
     * Abrir tela de pedidos de venda
     */
    public void abrirPedidosVenda(ContextoAcao contexto) throws Exception {
        contexto.setParam("resourceId", "TGFCAB_VENDA");
        contexto.setParam("parametros", "TIPMOV=V");
        
        RotinaLancadorAction lancador = new RotinaLancadorAction();
        lancador.doAction(contexto);
    }
    
    /**
     * Abrir tela de clientes
     */
    public void abrirClientes(ContextoAcao contexto) throws Exception {
        contexto.setParam("resourceId", "TGFPAR");
        contexto.setParam("parametros", "CLIENTE=S");
        
        RotinaLancadorAction lancador = new RotinaLancadorAction();
        lancador.doAction(contexto);
    }
    
    /**
     * Abrir tela de produtos
     */
    public void abrirProdutos(ContextoAcao contexto) throws Exception {
        contexto.setParam("resourceId", "TGFPRO");
        contexto.setParam("parametros", "ATIVO=S");
        
        RotinaLancadorAction lancador = new RotinaLancadorAction();
        lancador.doAction(contexto);
    }
}
```

### **2. Rotina Banco de Dados (SP)**

#### **Descrição**
Executa stored procedures no banco de dados.

#### **Implementação**
```java
package br.com.empresa.rotinas;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.modelcore.actionbutton.StoredProcedureAction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Rotina para execução de stored procedures
 */
public class RotinaBancoDadosAction implements AcaoRotinaJava {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();
        
        if (linhas == null || linhas.length == 0) {
            contexto.setMensagemRetorno("Nenhum registro selecionado!");
            return;
        }
        
        // Obter nome da procedure
        String nomeProcedure = (String) contexto.getParam("nomeProcedure");
        
        if (nomeProcedure == null || nomeProcedure.trim().isEmpty()) {
            contexto.setMensagemRetorno("Nome da procedure é obrigatório!");
            return;
        }
        
        int executadas = 0;
        
        for (Registro linha : linhas) {
            try {
                // Preparar parâmetros
                Map<String, Object> parametros = prepararParametros(linha, contexto);
                
                // Executar procedure
                executarProcedure(nomeProcedure, parametros);
                
                executadas++;
                
            } catch (Exception e) {
                contexto.mostraErro("Erro ao executar procedure para registro: " + e.getMessage());
            }
        }
        
        contexto.setMensagemRetorno("Procedures executadas: " + executadas);
    }
    
    private Map<String, Object> prepararParametros(Registro linha, ContextoAcao contexto) {
        Map<String, Object> parametros = new HashMap<>();
        
        // Parâmetros do registro
        parametros.put("NUNOTA", linha.getField("NUNOTA"));
        parametros.put("CODPARC", linha.getField("CODPARC"));
        parametros.put("CODVEN", linha.getField("CODVEN"));
        parametros.put("VLRNOTA", linha.getField("VLRNOTA"));
        
        // Parâmetros do contexto
        parametros.put("USUARIO", contexto.getUsuarioLogado());
        parametros.put("DATA_ATUAL", new java.util.Date());
        
        return parametros;
    }
    
    private void executarProcedure(String nomeProcedure, Map<String, Object> parametros) throws Exception {
        try {
            // Usar StoredProcedureAction do Sankhya
            StoredProcedureAction procedure = new StoredProcedureAction(nomeProcedure, facade.getJdbcWrapper());
            
            // Executar procedure
            String resultado = procedure.execute();
            
            System.out.println("Procedure " + nomeProcedure + " executada: " + resultado);
            
        } catch (Exception e) {
            throw new Exception("Erro na execução da procedure " + nomeProcedure + ": " + e.getMessage());
        }
    }
}

/**
 * Exemplos de stored procedures
 */
class ExemplosProcedures {
    
    /**
     * Procedure para calcular comissões
     */
    public static final String PROCEDURE_CALCULAR_COMISSOES = """
        CREATE OR REPLACE PROCEDURE CALCULAR_COMISSOES_VENDEDOR(
            p_CODVEN IN NUMBER,
            p_DT_INICIO IN DATE,
            p_DT_FIM IN DATE,
            p_TOTAL_COMISSAO OUT NUMBER
        ) AS
        BEGIN
            SELECT SUM(VLRNOTA * 0.05) -- 5% de comissão
            INTO p_TOTAL_COMISSAO
            FROM TGFCAB
            WHERE CODVEN = p_CODVEN
            AND DTEMISSAO BETWEEN p_DT_INICIO AND p_DT_FIM
            AND STATUSNOTA = 'L';
            
            -- Salvar comissão calculada
            INSERT INTO AD_COMISSOES (CODVEN, DT_INICIO, DT_FIM, VALOR_COMISSAO, DT_CALCULO)
            VALUES (p_CODVEN, p_DT_INICIO, p_DT_FIM, p_TOTAL_COMISSAO, SYSDATE);
            
        EXCEPTION
            WHEN OTHERS THEN
                p_TOTAL_COMISSAO := 0;
                RAISE;
        END;
        """;
    
    /**
     * Procedure para atualizar estoque
     */
    public static final String PROCEDURE_ATUALIZAR_ESTOQUE = """
        CREATE OR REPLACE PROCEDURE ATUALIZAR_ESTOQUE_PRODUTO(
            p_CODPROD IN NUMBER,
            p_QUANTIDADE IN NUMBER,
            p_TIPO_MOVIMENTO IN VARCHAR2, -- 'ENTRADA' ou 'SAIDA'
            p_RETORNO OUT VARCHAR2
        ) AS
            v_SALDO_ATUAL NUMBER;
        BEGIN
            -- Buscar saldo atual
            SELECT SALDOFISICO INTO v_SALDO_ATUAL
            FROM TGFPRO
            WHERE CODPROD = p_CODPROD;
            
            -- Atualizar estoque
            IF p_TIPO_MOVIMENTO = 'ENTRADA' THEN
                UPDATE TGFPRO 
                SET SALDOFISICO = SALDOFISICO + p_QUANTIDADE
                WHERE CODPROD = p_CODPROD;
                
                p_RETORNO := 'Estoque atualizado com entrada';
                
            ELSIF p_TIPO_MOVIMENTO = 'SAIDA' THEN
                IF v_SALDO_ATUAL >= p_QUANTIDADE THEN
                    UPDATE TGFPRO 
                    SET SALDOFISICO = SALDOFISICO - p_QUANTIDADE
                    WHERE CODPROD = p_CODPROD;
                    
                    p_RETORNO := 'Estoque atualizado com saída';
                ELSE
                    p_RETORNO := 'ERRO: Estoque insuficiente';
                END IF;
            END IF;
            
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                p_RETORNO := 'ERRO: Produto não encontrado';
            WHEN OTHERS THEN
                p_RETORNO := 'ERRO: ' || SQLERRM;
        END;
        """;
}
```

### **3. Rotina JavaScript (SC)**

#### **Descrição**
Executa código JavaScript no cliente.

#### **Implementação**
```javascript
// Rotina JavaScript para validação no cliente
function validarPedidoVenda() {
    try {
        // Obter dados do formulário
        var nunota = getValue("NUNOTA");
        var codparc = getValue("CODPARC");
        var vlrnota = getValue("VLRNOTA");
        
        // Validações
        if (!nunota || nunota.trim() === "") {
            showMessage("Número do pedido é obrigatório!");
            return false;
        }
        
        if (!codparc || codparc.trim() === "") {
            showMessage("Cliente é obrigatório!");
            return false;
        }
        
        if (!vlrnota || parseFloat(vlrnota) <= 0) {
            showMessage("Valor do pedido deve ser maior que zero!");
            return false;
        }
        
        // Validação adicional de crédito
        validarCreditoCliente(codparc, vlrnota);
        
        return true;
        
    } catch (error) {
        showMessage("Erro na validação: " + error.message);
        return false;
    }
}

function validarCreditoCliente(codparc, vlrnota) {
    // Fazer chamada AJAX para verificar crédito
    var request = new XMLHttpRequest();
    request.open("POST", "/sankhya/action/validarCredito", false);
    request.setRequestHeader("Content-Type", "application/json");
    
    var data = {
        codparc: codparc,
        vlrnota: vlrnota
    };
    
    request.send(JSON.stringify(data));
    
    if (request.status === 200) {
        var response = JSON.parse(request.responseText);
        
        if (!response.creditoOk) {
            showMessage("Cliente sem crédito suficiente!");
            return false;
        }
    }
    
    return true;
}

function calcularTotalPedido() {
    try {
        var total = 0;
        var itens = getGridData("TGFITE");
        
        for (var i = 0; i < itens.length; i++) {
            var quantidade = parseFloat(itens[i].QTDNEG) || 0;
            var preco = parseFloat(itens[i].VLRNEG) || 0;
            var subtotal = quantidade * preco;
            
            total += subtotal;
        }
        
        // Aplicar desconto se houver
        var desconto = parseFloat(getValue("DESCONTO")) || 0;
        total -= desconto;
        
        // Aplicar impostos
        var icms = total * 0.18; // 18% ICMS
        total += icms;
        
        // Atualizar campo total
        setValue("VLRNOTA", total.toFixed(2));
        
        return total;
        
    } catch (error) {
        showMessage("Erro no cálculo: " + error.message);
        return 0;
    }
}

function formatarCampos() {
    // Formatar CPF/CNPJ
    var cgccpf = getValue("CGCCPF");
    if (cgccpf) {
        var cgccpfLimpo = cgccpf.replace(/\D/g, "");
        
        if (cgccpfLimpo.length === 11) {
            // Formatar CPF
            cgccpf = cgccpfLimpo.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
        } else if (cgccpfLimpo.length === 14) {
            // Formatar CNPJ
            cgccpf = cgccpfLimpo.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
        }
        
        setValue("CGCCPF", cgccpf);
    }
    
    // Formatar telefone
    var telefone = getValue("TELEFONE");
    if (telefone) {
        var telefoneLimpo = telefone.replace(/\D/g, "");
        
        if (telefoneLimpo.length === 10) {
            telefone = telefoneLimpo.replace(/(\d{2})(\d{4})(\d{4})/, "($1) $2-$3");
        } else if (telefoneLimpo.length === 11) {
            telefone = telefoneLimpo.replace(/(\d{2})(\d{5})(\d{4})/, "($1) $2-$3");
        }
        
        setValue("TELEFONE", telefone);
    }
    
    // Formatar CEP
    var cep = getValue("CEPPARC");
    if (cep) {
        var cepLimpo = cep.replace(/\D/g, "");
        if (cepLimpo.length === 8) {
            cep = cepLimpo.replace(/(\d{5})(\d{3})/, "$1-$2");
            setValue("CEPPARC", cep);
        }
    }
}

function buscarEnderecoPorCEP() {
    var cep = getValue("CEPPARC");
    if (!cep || cep.length < 8) {
        return;
    }
    
    var cepLimpo = cep.replace(/\D/g, "");
    
    if (cepLimpo.length === 8) {
        try {
            var request = new XMLHttpRequest();
            request.open("GET", "https://viacep.com.br/ws/" + cepLimpo + "/json/", false);
            request.send();
            
            if (request.status === 200) {
                var response = JSON.parse(request.responseText);
                
                if (!response.erro) {
                    setValue("ENDPARC", response.logradouro);
                    setValue("CIDADEPARC", response.localidade);
                    setValue("UFPARC", response.uf);
                    setValue("BAIRROPARC", response.bairro);
                }
            }
        } catch (error) {
            console.log("Erro ao buscar CEP: " + error.message);
        }
    }
}

// Funções auxiliares
function getValue(campo) {
    // Implementar busca do valor do campo
    return document.getElementById(campo)?.value || "";
}

function setValue(campo, valor) {
    // Implementar definição do valor do campo
    var elemento = document.getElementById(campo);
    if (elemento) {
        elemento.value = valor;
    }
}

function getGridData(gridName) {
    // Implementar busca dos dados da grid
    return [];
}

function showMessage(mensagem) {
    // Implementar exibição de mensagem
    alert(mensagem);
}
```

### **4. Rotina Java (RJ)**

#### **Descrição**
Executa código Java no servidor.

#### **Implementação**
```java
package br.com.empresa.rotinas;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Rotina Java para processamento complexo no servidor
 */
public class RotinaJavaAction implements AcaoRotinaJava {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();
        
        if (linhas == null || linhas.length == 0) {
            contexto.setMensagemRetorno("Nenhum registro selecionado!");
            return;
        }
        
        // Obter tipo de processamento
        String tipoProcessamento = (String) contexto.getParam("tipoProcessamento");
        
        if (tipoProcessamento == null) {
            contexto.setMensagemRetorno("Tipo de processamento é obrigatório!");
            return;
        }
        
        int processados = 0;
        
        for (Registro linha : linhas) {
            try {
                // Processar baseado no tipo
                switch (tipoProcessamento.toUpperCase()) {
                    case "APROVAR":
                        processarAprovacao(linha, contexto);
                        break;
                    case "CANCELAR":
                        processarCancelamento(linha, contexto);
                        break;
                    case "FATURAR":
                        processarFaturamento(linha, contexto);
                        break;
                    case "INTEGRAR":
                        processarIntegracao(linha, contexto);
                        break;
                    default:
                        throw new Exception("Tipo de processamento inválido: " + tipoProcessamento);
                }
                
                processados++;
                
            } catch (Exception e) {
                contexto.mostraErro("Erro ao processar registro: " + e.getMessage());
            }
        }
        
        contexto.setMensagemRetorno("Processamento concluído: " + processados + " registros");
    }
    
    private void processarAprovacao(Registro linha, ContextoAcao contexto) throws Exception {
        BigDecimal nunota = linha.getField("NUNOTA");
        
        // Buscar pedido
        DynamicVO pedido = facade.findEntityByPrimaryKey("TGFCAB", nunota);
        
        if (pedido == null) {
            throw new Exception("Pedido não encontrado: " + nunota);
        }
        
        // Validar status
        if (!"L".equals(pedido.asString("STATUSNOTA"))) {
            throw new Exception("Pedido não está liberado para aprovação");
        }
        
        // Validar crédito
        if (!validarCredito(pedido)) {
            throw new Exception("Cliente sem crédito suficiente");
        }
        
        // Validar estoque
        if (!validarEstoque(nunota)) {
            throw new Exception("Estoque insuficiente");
        }
        
        // Aprovar pedido
        pedido.setProperty("STATUSNOTA", "A");
        pedido.setProperty("DTAPROVACAO", new Date());
        pedido.setProperty("USUAPROVACAO", contexto.getUsuarioLogado());
        
        facade.saveEntity("TGFCAB", pedido);
        
        // Reservar estoque
        reservarEstoque(nunota);
        
        System.out.println("Pedido " + nunota + " aprovado com sucesso");
    }
    
    private void processarCancelamento(Registro linha, ContextoAcao contexto) throws Exception {
        BigDecimal nunota = linha.getField("NUNOTA");
        
        // Buscar pedido
        DynamicVO pedido = facade.findEntityByPrimaryKey("TGFCAB", nunota);
        
        if (pedido == null) {
            throw new Exception("Pedido não encontrado: " + nunota);
        }
        
        // Validar se pode cancelar
        String status = pedido.asString("STATUSNOTA");
        if ("F".equals(status)) {
            throw new Exception("Pedido já foi faturado e não pode ser cancelado");
        }
        
        // Cancelar pedido
        pedido.setProperty("STATUSNOTA", "C");
        pedido.setProperty("DTCANCELAMENTO", new Date());
        pedido.setProperty("USUCANCELAMENTO", contexto.getUsuarioLogado());
        
        facade.saveEntity("TGFCAB", pedido);
        
        // Liberar estoque reservado
        liberarEstoque(nunota);
        
        System.out.println("Pedido " + nunota + " cancelado com sucesso");
    }
    
    private void processarFaturamento(Registro linha, ContextoAcao contexto) throws Exception {
        BigDecimal nunota = linha.getField("NUNOTA");
        
        // Buscar pedido
        DynamicVO pedido = facade.findEntityByPrimaryKey("TGFCAB", nunota);
        
        if (pedido == null) {
            throw new Exception("Pedido não encontrado: " + nunota);
        }
        
        // Validar status
        if (!"A".equals(pedido.asString("STATUSNOTA"))) {
            throw new Exception("Pedido deve estar aprovado para faturamento");
        }
        
        // Gerar nota fiscal
        BigDecimal nunfe = gerarNotaFiscal(pedido);
        
        // Atualizar pedido
        pedido.setProperty("STATUSNOTA", "F");
        pedido.setProperty("DTFATURAMENTO", new Date());
        pedido.setProperty("USUFATURAMENTO", contexto.getUsuarioLogado());
        pedido.setProperty("NUNFE", nunfe);
        
        facade.saveEntity("TGFCAB", pedido);
        
        // Baixar estoque
        baixarEstoque(nunota);
        
        System.out.println("Pedido " + nunota + " faturado com sucesso - NFe: " + nunfe);
    }
    
    private void processarIntegracao(Registro linha, ContextoAcao contexto) throws Exception {
        BigDecimal nunota = linha.getField("NUNOTA");
        
        // Buscar pedido
        DynamicVO pedido = facade.findEntityByPrimaryKey("TGFCAB", nunota);
        
        if (pedido == null) {
            throw new Exception("Pedido não encontrado: " + nunota);
        }
        
        // Preparar dados para integração
        Map<String, Object> dadosIntegracao = prepararDadosIntegracao(pedido);
        
        // Enviar para sistema externo
        boolean sucesso = enviarParaSistemaExterno(dadosIntegracao);
        
        if (sucesso) {
            // Atualizar status de integração
            pedido.setProperty("STATUS_INTEGRACAO", "INTEGRADO");
            pedido.setProperty("DT_INTEGRACAO", new Date());
            
            facade.saveEntity("TGFCAB", pedido);
            
            System.out.println("Pedido " + nunota + " integrado com sucesso");
        } else {
            throw new Exception("Falha na integração do pedido");
        }
    }
    
    private boolean validarCredito(DynamicVO pedido) throws Exception {
        BigDecimal codparc = pedido.asBigDecimal("CODPARC");
        BigDecimal vlrnota = pedido.asBigDecimal("VLRNOTA");
        
        String sql = "SELECT LIMCRED FROM TGFPAR WHERE CODPARC = ?";
        List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql, codparc);
        
        if (!resultado.isEmpty()) {
            BigDecimal limiteCredito = resultado.get(0).asBigDecimal("LIMCRED");
            return vlrnota.compareTo(limiteCredito) <= 0;
        }
        
        return false;
    }
    
    private boolean validarEstoque(BigDecimal nunota) throws Exception {
        String sql = """
            SELECT 
                i.QTDNEG,
                p.SALDOFISICO
            FROM TGFITE i
            JOIN TGFPRO p ON i.CODPROD = p.CODPROD
            WHERE i.NUNOTA = ?
            """;
        
        List<DynamicVO> itens = facade.getQueryExecutor().executeQuery(sql, nunota);
        
        for (DynamicVO item : itens) {
            BigDecimal qtdNecessaria = item.asBigDecimal("QTDNEG");
            BigDecimal saldoDisponivel = item.asBigDecimal("SALDOFISICO");
            
            if (saldoDisponivel.compareTo(qtdNecessaria) < 0) {
                return false;
            }
        }
        
        return true;
    }
    
    private void reservarEstoque(BigDecimal nunota) throws Exception {
        String sql = """
            UPDATE TGFPRO p
            SET SALDOBLOQUEADO = SALDOBLOQUEADO + i.QTDNEG
            FROM TGFITE i
            WHERE i.NUNOTA = ? AND i.CODPROD = p.CODPROD
            """;
        
        facade.getQueryExecutor().executeUpdate(sql, nunota);
    }
    
    private void liberarEstoque(BigDecimal nunota) throws Exception {
        String sql = """
            UPDATE TGFPRO p
            SET SALDOBLOQUEADO = SALDOBLOQUEADO - i.QTDNEG
            FROM TGFITE i
            WHERE i.NUNOTA = ? AND i.CODPROD = p.CODPROD
            """;
        
        facade.getQueryExecutor().executeUpdate(sql, nunota);
    }
    
    private void baixarEstoque(BigDecimal nunota) throws Exception {
        String sql = """
            UPDATE TGFPRO p
            SET SALDOFISICO = SALDOFISICO - i.QTDNEG
            FROM TGFITE i
            WHERE i.NUNOTA = ? AND i.CODPROD = p.CODPROD
            """;
        
        facade.getQueryExecutor().executeUpdate(sql, nunota);
    }
    
    private BigDecimal gerarNotaFiscal(DynamicVO pedido) throws Exception {
        // Simular geração de nota fiscal
        // Em implementação real, integrar com sistema fiscal
        return new BigDecimal("123456789");
    }
    
    private Map<String, Object> prepararDadosIntegracao(DynamicVO pedido) {
        Map<String, Object> dados = new HashMap<>();
        
        dados.put("nunota", pedido.asBigDecimal("NUNOTA"));
        dados.put("codparc", pedido.asBigDecimal("CODPARC"));
        dados.put("vlrnota", pedido.asBigDecimal("VLRNOTA"));
        dados.put("dtemissao", pedido.asDate("DTEMISSAO"));
        
        return dados;
    }
    
    private boolean enviarParaSistemaExterno(Map<String, Object> dados) {
        // Simular envio para sistema externo
        // Em implementação real, usar API ou web service
        System.out.println("Enviando dados para sistema externo: " + dados);
        return true;
    }
}
```

### **5. Transação Manual para Ações (TMA)**

#### **Descrição**
Controle manual de transações para ações complexas.

#### **Implementação**
```java
package br.com.empresa.rotinas;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;

/**
 * Rotina com controle manual de transação
 */
public class TransacaoManualAction implements AcaoRotinaJava {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();
        
        if (linhas == null || linhas.length == 0) {
            contexto.setMensagemRetorno("Nenhum registro selecionado!");
            return;
        }
        
        // Iniciar transação manual
        facade.beginTransaction();
        
        try {
            int processados = 0;
            
            for (Registro linha : linhas) {
                try {
                    // Processar registro
                    processarRegistroComTransacao(linha, contexto);
                    processados++;
                    
                } catch (Exception e) {
                    // Rollback da transação em caso de erro
                    facade.rollbackTransaction();
                    throw new Exception("Erro ao processar registro: " + e.getMessage());
                }
            }
            
            // Commit da transação se tudo correu bem
            facade.commitTransaction();
            
            contexto.setMensagemRetorno("Processamento concluído: " + processados + " registros");
            
        } catch (Exception e) {
            // Rollback em caso de erro
            facade.rollbackTransaction();
            contexto.mostraErro("Erro no processamento: " + e.getMessage());
        }
    }
    
    private void processarRegistroComTransacao(Registro linha, ContextoAcao contexto) throws Exception {
        BigDecimal nunota = linha.getField("NUNOTA");
        
        // Operação 1: Atualizar pedido
        atualizarPedido(nunota);
        
        // Operação 2: Atualizar estoque
        atualizarEstoque(nunota);
        
        // Operação 3: Gerar log
        gerarLogOperacao(nunota, contexto);
        
        // Operação 4: Notificar usuário
        notificarUsuario(nunota, contexto);
        
        System.out.println("Registro " + nunota + " processado com sucesso");
    }
    
    private void atualizarPedido(BigDecimal nunota) throws Exception {
        DynamicVO pedido = facade.findEntityByPrimaryKey("TGFCAB", nunota);
        
        if (pedido != null) {
            pedido.setProperty("STATUSNOTA", "P");
            pedido.setProperty("DT_PROCESSAMENTO", new Date());
            
            facade.saveEntity("TGFCAB", pedido);
        }
    }
    
    private void atualizarEstoque(BigDecimal nunota) throws Exception {
        String sql = """
            UPDATE TGFPRO p
            SET SALDOFISICO = SALDOFISICO - i.QTDNEG
            FROM TGFITE i
            WHERE i.NUNOTA = ? AND i.CODPROD = p.CODPROD
            """;
        
        facade.getQueryExecutor().executeUpdate(sql, nunota);
    }
    
    private void gerarLogOperacao(BigDecimal nunota, ContextoAcao contexto) throws Exception {
        DynamicVO log = facade.createEntity("AD_LOG_OPERACAO");
        log.setProperty("NUNOTA", nunota);
        log.setProperty("OPERACAO", "PROCESSAMENTO_MANUAL");
        log.setProperty("USUARIO", contexto.getUsuarioLogado());
        log.setProperty("DT_OPERACAO", new Date());
        log.setProperty("STATUS", "SUCESSO");
        
        facade.saveEntity("AD_LOG_OPERACAO", log);
    }
    
    private void notificarUsuario(BigDecimal nunota, ContextoAcao contexto) throws Exception {
        // Simular notificação
        System.out.println("Notificando usuário sobre processamento do pedido " + nunota);
    }
}
```

## 🎯 **Boas Práticas das Rotinas**

### **1. Escolha do Tipo de Rotina**
- **Lançador (LC)**: Para navegação e abertura de telas
- **Banco de Dados (SP)**: Para operações complexas no banco
- **JavaScript (SC)**: Para validações e cálculos no cliente
- **Java (RJ)**: Para lógica de negócio complexa no servidor
- **Transação Manual (TMA)**: Para controle preciso de transações

### **2. Performance**
- **Consultas Otimizadas**: SQL eficiente
- **Batch Processing**: Processar em lotes
- **Índices**: Usar índices adequados
- **Transações**: Controlar transações adequadamente

### **3. Tratamento de Erros**
- **Validações**: Validar dados antes de processar
- **Rollback**: Fazer rollback em caso de erro
- **Logs**: Registrar erros e operações
- **Mensagens**: Mensagens claras para o usuário

### **4. Segurança**
- **Autorização**: Verificar permissões
- **Validação**: Validar entrada de dados
- **Auditoria**: Registrar operações
- **Sanitização**: Limpar dados

## 🎊 **Conclusão**

As rotinas de botões de ação demonstram:

- **✅ Flexibilidade**: Diferentes tipos de rotinas
- **✅ Funcionalidade**: Cada tipo tem seu propósito
- **✅ Performance**: Otimizadas para diferentes cenários
- **✅ Segurança**: Tratamento adequado de erros
- **✅ Manutenibilidade**: Código bem estruturado
- **✅ Escalabilidade**: Suporte a crescimento

### **Benefícios:**
- **Versatilidade**: Múltiplas opções de implementação
- **Eficiência**: Otimizadas para cada tipo de operação
- **Confiabilidade**: Tratamento robusto de erros
- **Facilidade**: Fácil de implementar e manter
- **Integração**: Integração completa com Sankhya

---

*Este documento apresenta diferentes tipos de rotinas para botões de ação no Sankhya, fornecendo implementações práticas e funcionais para cada tipo.*
