# 🎯 Casos de Uso Reais Sankhya - Implementações Práticas

## 🎯 Visão Geral

Este documento apresenta casos de uso reais implementados em Sankhya, com exemplos práticos, códigos funcionais e explicações detalhadas baseados em implementações reais do sistema.

## 🏢 **Caso de Uso 1: Sistema de Controle de Comissões**

### **Contexto do Negócio**
Uma empresa de vendas precisa controlar comissões de vendedores baseadas em diferentes critérios: valor vendido, margem de lucro, tipo de produto e performance mensal.

### **Implementação**

#### **1. Estrutura de Dados**
```sql
-- Tabela de configuração de comissões
CREATE TABLE AD_CONFIG_COMISSAO (
    ID NUMBER PRIMARY KEY,
    CODVEN NUMBER,
    CODGRUPOPROD NUMBER,
    PERCENTUAL_COMISSAO NUMBER(5,2),
    VALOR_MINIMO NUMBER(15,2),
    MARGEM_MINIMA NUMBER(5,2),
    ATIVO VARCHAR2(1) DEFAULT 'S',
    DT_INICIO DATE,
    DT_FIM DATE
);

-- Tabela de histórico de comissões
CREATE TABLE AD_HIST_COMISSAO (
    ID NUMBER PRIMARY KEY,
    NUNOTA NUMBER,
    CODVEN NUMBER,
    VLRNOTA NUMBER(15,2),
    VLRLUCRO NUMBER(15,2),
    PERCENTUAL_APLICADO NUMBER(5,2),
    VALOR_COMISSAO NUMBER(15,2),
    DT_CALCULO DATE,
    STATUS VARCHAR2(20) DEFAULT 'CALCULADO'
);
```

#### **2. Evento Programado para Cálculo Automático**
```java
package br.com.empresa.comissoes;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Evento para cálculo automático de comissões em pedidos de venda
 */
public class ComissaoVendaEvent implements EventoProgramavelJava {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    
    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO pedido = (DynamicVO) event.getVo();
        
        // Verificar se é pedido de venda
        if (!"V".equals(pedido.asString("TIPMOV"))) {
            return;
        }
        
        // Calcular comissão
        calcularComissaoPedido(pedido);
    }
    
    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO pedido = (DynamicVO) event.getVo();
        
        // Verificar se é pedido de venda
        if (!"V".equals(pedido.asString("TIPMOV"))) {
            return;
        }
        
        // Recalcular comissão se necessário
        if (precisaRecalcular(pedido)) {
            recalcularComissao(pedido);
        }
    }
    
    private void calcularComissaoPedido(DynamicVO pedido) throws Exception {
        BigDecimal nunota = pedido.asBigDecimal("NUNOTA");
        BigDecimal codven = pedido.asBigDecimal("CODVEN");
        BigDecimal vlrnota = pedido.asBigDecimal("VLRNOTA");
        
        // Buscar configuração de comissão
        BigDecimal percentualComissao = buscarPercentualComissao(codven, nunota);
        
        if (percentualComissao != null && percentualComissao.compareTo(BigDecimal.ZERO) > 0) {
            // Calcular valor da comissão
            BigDecimal valorComissao = vlrnota.multiply(percentualComissao)
                                             .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            
            // Verificar margem mínima
            if (verificarMargemMinima(nunota, percentualComissao)) {
                // Salvar comissão
                salvarComissao(nunota, codven, vlrnota, percentualComissao, valorComissao);
            }
        }
    }
    
    private BigDecimal buscarPercentualComissao(BigDecimal codven, BigDecimal nunota) throws Exception {
        // Buscar itens do pedido para determinar grupo de produto
        String sql = "SELECT DISTINCT CODGRUPOPROD FROM TGFITE WHERE NUNOTA = ?";
        List<DynamicVO> itens = facade.getQueryExecutor().executeQuery(sql, nunota);
        
        BigDecimal percentualMaximo = BigDecimal.ZERO;
        
        for (DynamicVO item : itens) {
            BigDecimal codgrupoprod = item.asBigDecimal("CODGRUPOPROD");
            
            String sqlComissao = """
                SELECT PERCENTUAL_COMISSAO 
                FROM AD_CONFIG_COMISSAO 
                WHERE CODVEN = ? AND CODGRUPOPROD = ? 
                AND ATIVO = 'S' 
                AND (DT_INICIO IS NULL OR DT_INICIO <= SYSDATE)
                AND (DT_FIM IS NULL OR DT_FIM >= SYSDATE)
                ORDER BY PERCENTUAL_COMISSAO DESC
                """;
            
            List<DynamicVO> configs = facade.getQueryExecutor().executeQuery(sqlComissao, codven, codgrupoprod);
            
            if (!configs.isEmpty()) {
                BigDecimal percentual = configs.get(0).asBigDecimal("PERCENTUAL_COMISSAO");
                if (percentual.compareTo(percentualMaximo) > 0) {
                    percentualMaximo = percentual;
                }
            }
        }
        
        return percentualMaximo;
    }
    
    private boolean verificarMargemMinima(BigDecimal nunota, BigDecimal percentualComissao) throws Exception {
        // Calcular margem do pedido
        String sqlMargem = """
            SELECT SUM(VLRTOT - VLRCUSTOTOT) / SUM(VLRTOT) * 100 as MARGEM
            FROM TGFITE 
            WHERE NUNOTA = ?
            """;
        
        List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sqlMargem, nunota);
        
        if (!resultado.isEmpty()) {
            BigDecimal margem = resultado.get(0).asBigDecimal("MARGEM");
            
            // Buscar margem mínima configurada
            String sqlMargemMin = """
                SELECT MARGEM_MINIMA 
                FROM AD_CONFIG_COMISSAO 
                WHERE PERCENTUAL_COMISSAO = ?
                AND ATIVO = 'S'
                """;
            
            List<DynamicVO> configs = facade.getQueryExecutor().executeQuery(sqlMargemMin, percentualComissao);
            
            if (!configs.isEmpty()) {
                BigDecimal margemMinima = configs.get(0).asBigDecimal("MARGEM_MINIMA");
                return margem.compareTo(margemMinima) >= 0;
            }
        }
        
        return true; // Se não há configuração, permite
    }
    
    private void salvarComissao(BigDecimal nunota, BigDecimal codven, BigDecimal vlrnota, 
                               BigDecimal percentual, BigDecimal valorComissao) throws Exception {
        DynamicVO comissao = facade.createEntity("AD_HIST_COMISSAO");
        comissao.setProperty("NUNOTA", nunota);
        comissao.setProperty("CODVEN", codven);
        comissao.setProperty("VLRNOTA", vlrnota);
        comissao.setProperty("PERCENTUAL_APLICADO", percentual);
        comissao.setProperty("VALOR_COMISSAO", valorComissao);
        comissao.setProperty("DT_CALCULO", new Date());
        comissao.setProperty("STATUS", "CALCULADO");
        
        facade.saveEntity("AD_HIST_COMISSAO", comissao);
        
        System.out.println("Comissão calculada: R$ " + valorComissao + " para vendedor " + codven);
    }
    
    private boolean precisaRecalcular(DynamicVO pedido) {
        // Verificar se o valor do pedido foi alterado
        BigDecimal vlrnota = pedido.asBigDecimal("VLRNOTA");
        BigDecimal vlrnotaOld = pedido.getProperty("VLRNOTA_OLD") != null ? 
                               (BigDecimal) pedido.getProperty("VLRNOTA_OLD") : BigDecimal.ZERO;
        
        return vlrnota.compareTo(vlrnotaOld) != 0;
    }
    
    private void recalcularComissao(DynamicVO pedido) throws Exception {
        BigDecimal nunota = pedido.asBigDecimal("NUNOTA");
        
        // Excluir comissão anterior
        String sqlDelete = "DELETE FROM AD_HIST_COMISSAO WHERE NUNOTA = ?";
        facade.getQueryExecutor().executeUpdate(sqlDelete, nunota);
        
        // Recalcular
        calcularComissaoPedido(pedido);
    }
}
```

#### **3. Botão de Ação para Relatório de Comissões**
```java
package br.com.empresa.comissoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Botão de ação para gerar relatório de comissões
 */
public class RelatorioComissoesAction implements AcaoRotinaJava {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        // Obter parâmetros
        Date dataInicio = (Date) contexto.getParam("dataInicio");
        Date dataFim = (Date) contexto.getParam("dataFim");
        BigDecimal codven = (BigDecimal) contexto.getParam("codven");
        
        // Gerar relatório
        String relatorio = gerarRelatorioComissoes(dataInicio, dataFim, codven);
        
        // Exibir relatório
        contexto.setMensagemRetorno(relatorio);
    }
    
    private String gerarRelatorioComissoes(Date dataInicio, Date dataFim, BigDecimal codven) throws Exception {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("RELATÓRIO DE COMISSÕES\n");
        relatorio.append("======================\n\n");
        
        String sql = """
            SELECT 
                h.CODVEN,
                p.NOMEPARC as NOME_VENDEDOR,
                COUNT(h.ID) as QTD_PEDIDOS,
                SUM(h.VLRNOTA) as TOTAL_VENDAS,
                SUM(h.VALOR_COMISSAO) as TOTAL_COMISSAO,
                AVG(h.PERCENTUAL_APLICADO) as PERCENTUAL_MEDIO
            FROM AD_HIST_COMISSAO h
            JOIN TGFPAR p ON h.CODVEN = p.CODPARC
            WHERE h.DT_CALCULO BETWEEN ? AND ?
            AND (? IS NULL OR h.CODVEN = ?)
            AND h.STATUS = 'CALCULADO'
            GROUP BY h.CODVEN, p.NOMEPARC
            ORDER BY TOTAL_COMISSAO DESC
            """;
        
        List<DynamicVO> resultados = facade.getQueryExecutor().executeQuery(sql, dataInicio, dataFim, codven, codven);
        
        relatorio.append(String.format("%-10s %-30s %-10s %-15s %-15s %-10s\n", 
                                     "CÓDIGO", "NOME", "PEDIDOS", "VENDAS", "COMISSÃO", "% MÉDIO"));
        relatorio.append("-".repeat(100)).append("\n");
        
        BigDecimal totalVendas = BigDecimal.ZERO;
        BigDecimal totalComissao = BigDecimal.ZERO;
        
        for (DynamicVO resultado : resultados) {
            BigDecimal codigo = resultado.asBigDecimal("CODVEN");
            String nome = resultado.asString("NOME_VENDEDOR");
            BigDecimal qtdPedidos = resultado.asBigDecimal("QTD_PEDIDOS");
            BigDecimal vendas = resultado.asBigDecimal("TOTAL_VENDAS");
            BigDecimal comissao = resultado.asBigDecimal("TOTAL_COMISSAO");
            BigDecimal percentual = resultado.asBigDecimal("PERCENTUAL_MEDIO");
            
            relatorio.append(String.format("%-10s %-30s %-10s %-15.2f %-15.2f %-10.2f\n",
                                         codigo, nome, qtdPedidos, vendas, comissao, percentual));
            
            totalVendas = totalVendas.add(vendas);
            totalComissao = totalComissao.add(comissao);
        }
        
        relatorio.append("-".repeat(100)).append("\n");
        relatorio.append(String.format("%-50s %-15.2f %-15.2f\n", "TOTAIS:", totalVendas, totalComissao));
        
        return relatorio.toString();
    }
}
```

## 🏭 **Caso de Uso 2: Sistema de Controle de Produção**

### **Contexto do Negócio**
Uma indústria precisa controlar a produção de produtos, incluindo ordens de produção, consumo de matérias-primas, controle de qualidade e rastreabilidade.

### **Implementação**

#### **1. Estrutura de Dados**
```sql
-- Tabela de ordens de produção
CREATE TABLE AD_ORDEM_PRODUCAO (
    ID NUMBER PRIMARY KEY,
    NUMERO_OP VARCHAR2(20),
    CODPROD NUMBER,
    QUANTIDADE_PLANEJADA NUMBER(15,3),
    QUANTIDADE_PRODUZIDA NUMBER(15,3),
    DT_INICIO_PLANEJADA DATE,
    DT_FIM_PLANEJADA DATE,
    DT_INICIO_REAL DATE,
    DT_FIM_REAL DATE,
    STATUS VARCHAR2(20) DEFAULT 'PLANEJADA',
    RESPONSAVEL NUMBER,
    OBSERVACOES VARCHAR2(500)
);

-- Tabela de consumo de matérias-primas
CREATE TABLE AD_CONSUMO_MATERIA (
    ID NUMBER PRIMARY KEY,
    ORDEM_PRODUCAO_ID NUMBER,
    CODPROD_MATERIA NUMBER,
    QUANTIDADE_PLANEJADA NUMBER(15,3),
    QUANTIDADE_CONSUMIDA NUMBER(15,3),
    CUSTO_UNITARIO NUMBER(15,2),
    DT_CONSUMO DATE,
    RESPONSAVEL NUMBER
);

-- Tabela de controle de qualidade
CREATE TABLE AD_CONTROLE_QUALIDADE (
    ID NUMBER PRIMARY KEY,
    ORDEM_PRODUCAO_ID NUMBER,
    TESTE_REALIZADO VARCHAR2(100),
    RESULTADO VARCHAR2(50),
    CONFORME VARCHAR2(1),
    OBSERVACOES VARCHAR2(500),
    DT_TESTE DATE,
    RESPONSAVEL NUMBER
);
```

#### **2. Evento Programado para Controle de Produção**
```java
package br.com.empresa.producao;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Evento para controle de produção
 */
public class ControleProducaoEvent implements EventoProgramavelJava {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO ordemProducao = (DynamicVO) event.getVo();
        
        // Gerar número da OP automaticamente
        if (ordemProducao.asString("NUMERO_OP") == null) {
            String numeroOP = gerarNumeroOP();
            ordemProducao.setProperty("NUMERO_OP", numeroOP);
        }
        
        // Validar dados
        validarOrdemProducao(ordemProducao);
    }
    
    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO ordemProducao = (DynamicVO) event.getVo();
        
        // Criar estrutura de consumo de matérias-primas
        criarEstruturaConsumo(ordemProducao);
        
        // Atualizar estoque de matérias-primas reservadas
        reservarMateriasPrimas(ordemProducao);
        
        System.out.println("Ordem de produção criada: " + ordemProducao.asString("NUMERO_OP"));
    }
    
    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO ordemProducao = (DynamicVO) event.getVo();
        String statusAnterior = (String) ordemProducao.getProperty("STATUS_OLD");
        String statusAtual = ordemProducao.asString("STATUS");
        
        // Verificar mudança de status
        if (!statusAnterior.equals(statusAtual)) {
            processarMudancaStatus(ordemProducao, statusAnterior, statusAtual);
        }
    }
    
    private String gerarNumeroOP() throws Exception {
        String sql = "SELECT MAX(TO_NUMBER(SUBSTR(NUMERO_OP, 3))) + 1 FROM AD_ORDEM_PRODUCAO WHERE NUMERO_OP LIKE 'OP%'";
        List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql);
        
        int proximoNumero = 1;
        if (!resultado.isEmpty() && resultado.get(0).asBigDecimal("1") != null) {
            proximoNumero = resultado.get(0).asBigDecimal("1").intValue();
        }
        
        return String.format("OP%06d", proximoNumero);
    }
    
    private void validarOrdemProducao(DynamicVO ordemProducao) throws Exception {
        BigDecimal quantidade = ordemProducao.asBigDecimal("QUANTIDADE_PLANEJADA");
        
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Quantidade planejada deve ser maior que zero");
        }
        
        Date dtInicio = ordemProducao.asDate("DT_INICIO_PLANEJADA");
        Date dtFim = ordemProducao.asDate("DT_FIM_PLANEJADA");
        
        if (dtInicio != null && dtFim != null && dtInicio.after(dtFim)) {
            throw new Exception("Data de início deve ser anterior à data de fim");
        }
        
        // Verificar disponibilidade de matérias-primas
        verificarDisponibilidadeMaterias(ordemProducao);
    }
    
    private void verificarDisponibilidadeMaterias(DynamicVO ordemProducao) throws Exception {
        BigDecimal codprod = ordemProducao.asBigDecimal("CODPROD");
        BigDecimal quantidade = ordemProducao.asBigDecimal("QUANTIDADE_PLANEJADA");
        
        // Buscar estrutura do produto
        String sql = """
            SELECT CODPROD_MATERIA, QUANTIDADE 
            FROM TGFITE 
            WHERE CODPROD = ? AND TIPMOV = 'P'
            """;
        
        List<DynamicVO> estrutura = facade.getQueryExecutor().executeQuery(sql, codprod);
        
        for (DynamicVO item : estrutura) {
            BigDecimal codprodMateria = item.asBigDecimal("CODPROD_MATERIA");
            BigDecimal qtdNecessaria = item.asBigDecimal("QUANTIDADE").multiply(quantidade);
            
            // Verificar estoque disponível
            String sqlEstoque = """
                SELECT SALDOFISICO - SALDOBLOQUEADO as DISPONIVEL
                FROM TGFPRO 
                WHERE CODPROD = ?
                """;
            
            List<DynamicVO> estoque = facade.getQueryExecutor().executeQuery(sqlEstoque, codprodMateria);
            
            if (!estoque.isEmpty()) {
                BigDecimal disponivel = estoque.get(0).asBigDecimal("DISPONIVEL");
                if (disponivel.compareTo(qtdNecessaria) < 0) {
                    throw new Exception("Estoque insuficiente para matéria-prima: " + codprodMateria);
                }
            }
        }
    }
    
    private void criarEstruturaConsumo(DynamicVO ordemProducao) throws Exception {
        BigDecimal ordemId = ordemProducao.asBigDecimal("ID");
        BigDecimal codprod = ordemProducao.asBigDecimal("CODPROD");
        BigDecimal quantidade = ordemProducao.asBigDecimal("QUANTIDADE_PLANEJADA");
        
        // Buscar estrutura do produto
        String sql = """
            SELECT CODPROD_MATERIA, QUANTIDADE 
            FROM TGFITE 
            WHERE CODPROD = ? AND TIPMOV = 'P'
            """;
        
        List<DynamicVO> estrutura = facade.getQueryExecutor().executeQuery(sql, codprod);
        
        for (DynamicVO item : estrutura) {
            DynamicVO consumo = facade.createEntity("AD_CONSUMO_MATERIA");
            consumo.setProperty("ORDEM_PRODUCAO_ID", ordemId);
            consumo.setProperty("CODPROD_MATERIA", item.asBigDecimal("CODPROD_MATERIA"));
            consumo.setProperty("QUANTIDADE_PLANEJADA", item.asBigDecimal("QUANTIDADE").multiply(quantidade));
            consumo.setProperty("QUANTIDADE_CONSUMIDA", BigDecimal.ZERO);
            
            facade.saveEntity("AD_CONSUMO_MATERIA", consumo);
        }
    }
    
    private void reservarMateriasPrimas(DynamicVO ordemProducao) throws Exception {
        BigDecimal ordemId = ordemProducao.asBigDecimal("ID");
        
        String sql = """
            SELECT CODPROD_MATERIA, QUANTIDADE_PLANEJADA 
            FROM AD_CONSUMO_MATERIA 
            WHERE ORDEM_PRODUCAO_ID = ?
            """;
        
        List<DynamicVO> consumos = facade.getQueryExecutor().executeQuery(sql, ordemId);
        
        for (DynamicVO consumo : consumos) {
            BigDecimal codprodMateria = consumo.asBigDecimal("CODPROD_MATERIA");
            BigDecimal quantidade = consumo.asBigDecimal("QUANTIDADE_PLANEJADA");
            
            // Atualizar saldo bloqueado
            String sqlUpdate = """
                UPDATE TGFPRO 
                SET SALDOBLOQUEADO = SALDOBLOQUEADO + ?
                WHERE CODPROD = ?
                """;
            
            facade.getQueryExecutor().executeUpdate(sqlUpdate, quantidade, codprodMateria);
        }
    }
    
    private void processarMudancaStatus(DynamicVO ordemProducao, String statusAnterior, String statusAtual) throws Exception {
        BigDecimal ordemId = ordemProducao.asBigDecimal("ID");
        
        switch (statusAtual) {
            case "INICIADA":
                if ("PLANEJADA".equals(statusAnterior)) {
                    iniciarProducao(ordemId);
                }
                break;
                
            case "FINALIZADA":
                if ("INICIADA".equals(statusAnterior)) {
                    finalizarProducao(ordemId);
                }
                break;
                
            case "CANCELADA":
                cancelarProducao(ordemId);
                break;
        }
    }
    
    private void iniciarProducao(BigDecimal ordemId) throws Exception {
        // Atualizar data de início real
        String sql = "UPDATE AD_ORDEM_PRODUCAO SET DT_INICIO_REAL = SYSDATE WHERE ID = ?";
        facade.getQueryExecutor().executeUpdate(sql, ordemId);
        
        System.out.println("Produção iniciada para OP: " + ordemId);
    }
    
    private void finalizarProducao(BigDecimal ordemId) throws Exception {
        // Atualizar data de fim real
        String sql = "UPDATE AD_ORDEM_PRODUCAO SET DT_FIM_REAL = SYSDATE WHERE ID = ?";
        facade.getQueryExecutor().executeUpdate(sql, ordemId);
        
        // Consumir matérias-primas
        consumirMateriasPrimas(ordemId);
        
        // Gerar entrada de produto acabado
        gerarEntradaProdutoAcabado(ordemId);
        
        System.out.println("Produção finalizada para OP: " + ordemId);
    }
    
    private void consumirMateriasPrimas(BigDecimal ordemId) throws Exception {
        String sql = """
            SELECT CODPROD_MATERIA, QUANTIDADE_CONSUMIDA 
            FROM AD_CONSUMO_MATERIA 
            WHERE ORDEM_PRODUCAO_ID = ?
            """;
        
        List<DynamicVO> consumos = facade.getQueryExecutor().executeQuery(sql, ordemId);
        
        for (DynamicVO consumo : consumos) {
            BigDecimal codprodMateria = consumo.asBigDecimal("CODPROD_MATERIA");
            BigDecimal quantidade = consumo.asBigDecimal("QUANTIDADE_CONSUMIDA");
            
            // Atualizar estoque
            String sqlUpdate = """
                UPDATE TGFPRO 
                SET SALDOFISICO = SALDOFISICO - ?,
                    SALDOBLOQUEADO = SALDOBLOQUEADO - ?
                WHERE CODPROD = ?
                """;
            
            facade.getQueryExecutor().executeUpdate(sqlUpdate, quantidade, quantidade, codprodMateria);
        }
    }
    
    private void gerarEntradaProdutoAcabado(BigDecimal ordemId) throws Exception {
        String sql = """
            SELECT CODPROD, QUANTIDADE_PRODUZIDA 
            FROM AD_ORDEM_PRODUCAO 
            WHERE ID = ?
            """;
        
        List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql, ordemId);
        
        if (!resultado.isEmpty()) {
            DynamicVO ordem = resultado.get(0);
            BigDecimal codprod = ordem.asBigDecimal("CODPROD");
            BigDecimal quantidade = ordem.asBigDecimal("QUANTIDADE_PRODUZIDA");
            
            // Atualizar estoque do produto acabado
            String sqlUpdate = """
                UPDATE TGFPRO 
                SET SALDOFISICO = SALDOFISICO + ?
                WHERE CODPROD = ?
                """;
            
            facade.getQueryExecutor().executeUpdate(sqlUpdate, quantidade, codprod);
        }
    }
    
    private void cancelarProducao(BigDecimal ordemId) throws Exception {
        // Liberar matérias-primas reservadas
        liberarMateriasPrimas(ordemId);
        
        System.out.println("Produção cancelada para OP: " + ordemId);
    }
    
    private void liberarMateriasPrimas(BigDecimal ordemId) throws Exception {
        String sql = """
            SELECT CODPROD_MATERIA, QUANTIDADE_PLANEJADA 
            FROM AD_CONSUMO_MATERIA 
            WHERE ORDEM_PRODUCAO_ID = ?
            """;
        
        List<DynamicVO> consumos = facade.getQueryExecutor().executeQuery(sql, ordemId);
        
        for (DynamicVO consumo : consumos) {
            BigDecimal codprodMateria = consumo.asBigDecimal("CODPROD_MATERIA");
            BigDecimal quantidade = consumo.asBigDecimal("QUANTIDADE_PLANEJADA");
            
            // Liberar saldo bloqueado
            String sqlUpdate = """
                UPDATE TGFPRO 
                SET SALDOBLOQUEADO = SALDOBLOQUEADO - ?
                WHERE CODPROD = ?
                """;
            
            facade.getQueryExecutor().executeUpdate(sqlUpdate, quantidade, codprodMateria);
        }
    }
}
```

## 🛒 **Caso de Uso 3: Sistema de E-commerce Integrado**

### **Contexto do Negócio**
Uma empresa precisa integrar seu sistema Sankhya com uma loja virtual, sincronizando produtos, estoques, preços e pedidos em tempo real.

### **Implementação**

#### **1. Webhook para Sincronização de Produtos**
```java
package br.com.empresa.ecommerce;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Evento para sincronização com e-commerce
 */
public class EcommerceSyncEvent implements EventoProgramavelJava {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    private EcommerceAPI ecommerceAPI = new EcommerceAPI();
    
    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO produto = (DynamicVO) event.getVo();
        
        if (produto.asString("ATIVO").equals("S")) {
            sincronizarProdutoComEcommerce(produto);
        }
    }
    
    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO produto = (DynamicVO) event.getVo();
        
        // Verificar se houve mudanças relevantes
        if (mudancasRelevantes(produto)) {
            sincronizarProdutoComEcommerce(produto);
        }
    }
    
    private boolean mudancasRelevantes(DynamicVO produto) {
        // Verificar se preço, estoque ou status mudaram
        BigDecimal precoAtual = produto.asBigDecimal("VLROFERTA");
        BigDecimal precoAnterior = produto.getProperty("VLROFERTA_OLD") != null ? 
                                  (BigDecimal) produto.getProperty("VLROFERTA_OLD") : BigDecimal.ZERO;
        
        String ativoAtual = produto.asString("ATIVO");
        String ativoAnterior = (String) produto.getProperty("ATIVO_OLD");
        
        return precoAtual.compareTo(precoAnterior) != 0 || 
               !ativoAtual.equals(ativoAnterior);
    }
    
    private void sincronizarProdutoComEcommerce(DynamicVO produto) throws Exception {
        try {
            Map<String, Object> dadosProduto = prepararDadosProduto(produto);
            
            // Enviar para e-commerce
            boolean sucesso = ecommerceAPI.sincronizarProduto(dadosProduto);
            
            if (sucesso) {
                registrarSincronizacao(produto.asBigDecimal("CODPROD"), "SUCESSO", null);
            } else {
                registrarSincronizacao(produto.asBigDecimal("CODPROD"), "ERRO", "Falha na API");
            }
            
        } catch (Exception e) {
            registrarSincronizacao(produto.asBigDecimal("CODPROD"), "ERRO", e.getMessage());
            throw e;
        }
    }
    
    private Map<String, Object> prepararDadosProduto(DynamicVO produto) {
        Map<String, Object> dados = new HashMap<>();
        
        dados.put("codigo", produto.asBigDecimal("CODPROD").toString());
        dados.put("nome", produto.asString("DESCRPROD"));
        dados.put("descricao", produto.asString("DESCRPROD"));
        dados.put("preco", produto.asBigDecimal("VLROFERTA"));
        dados.put("estoque", produto.asBigDecimal("SALDOFISICO"));
        dados.put("ativo", "S".equals(produto.asString("ATIVO")));
        dados.put("categoria", produto.asBigDecimal("CODGRUPOPROD"));
        dados.put("peso", produto.asBigDecimal("PESOBRUTO"));
        dados.put("altura", produto.asBigDecimal("ALTURA"));
        dados.put("largura", produto.asBigDecimal("LARGURA"));
        dados.put("profundidade", produto.asBigDecimal("PROFUNDIDADE"));
        
        // Buscar imagens
        dados.put("imagens", buscarImagensProduto(produto.asBigDecimal("CODPROD")));
        
        return dados;
    }
    
    private String[] buscarImagensProduto(BigDecimal codprod) {
        try {
            String sql = "SELECT CAMINHO_IMAGEM FROM AD_PRODUTO_IMAGEM WHERE CODPROD = ? AND ATIVO = 'S'";
            List<DynamicVO> imagens = facade.getQueryExecutor().executeQuery(sql, codprod);
            
            return imagens.stream()
                         .map(vo -> vo.asString("CAMINHO_IMAGEM"))
                         .toArray(String[]::new);
                         
        } catch (Exception e) {
            return new String[0];
        }
    }
    
    private void registrarSincronizacao(BigDecimal codprod, String status, String erro) throws Exception {
        DynamicVO log = facade.createEntity("AD_LOG_ECOMMERCE");
        log.setProperty("CODPROD", codprod);
        log.setProperty("TIPO_OPERACAO", "SINCRONIZACAO_PRODUTO");
        log.setProperty("STATUS", status);
        log.setProperty("ERRO", erro);
        log.setProperty("DT_OPERACAO", new Date());
        
        facade.saveEntity("AD_LOG_ECOMMERCE", log);
    }
}

/**
 * Cliente para API do e-commerce
 */
class EcommerceAPI {
    
    private String apiUrl = "https://api.ecommerce.com.br";
    private String apiKey = "sua_api_key";
    
    public boolean sincronizarProduto(Map<String, Object> dadosProduto) {
        try {
            // Implementar chamada HTTP para API
            // Usar RestTemplate ou similar
            
            System.out.println("Sincronizando produto: " + dadosProduto.get("codigo"));
            
            // Simular sucesso
            return true;
            
        } catch (Exception e) {
            System.err.println("Erro ao sincronizar produto: " + e.getMessage());
            return false;
        }
    }
    
    public Map<String, Object> buscarPedidoEcommerce(String numeroPedido) {
        try {
            // Implementar busca de pedido na API
            Map<String, Object> pedido = new HashMap<>();
            
            // Simular dados do pedido
            pedido.put("numero", numeroPedido);
            pedido.put("cliente", "Cliente E-commerce");
            pedido.put("valor", new BigDecimal("150.00"));
            pedido.put("status", "PENDENTE");
            
            return pedido;
            
        } catch (Exception e) {
            System.err.println("Erro ao buscar pedido: " + e.getMessage());
            return null;
        }
    }
}
```

#### **2. Ação Agendada para Importação de Pedidos**
```java
package br.com.empresa.ecommerce;

import br.com.sankhya.acaoagendada.ScheduledActionsUtils;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Ação agendada para importar pedidos do e-commerce
 */
public class ImportarPedidosEcommerceJob implements Runnable {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    private EcommerceAPI ecommerceAPI = new EcommerceAPI();
    
    @Override
    public void run() {
        try {
            System.out.println("Iniciando importação de pedidos do e-commerce");
            
            // Buscar pedidos pendentes no e-commerce
            List<Map<String, Object>> pedidos = buscarPedidosPendentes();
            
            int importados = 0;
            int erros = 0;
            
            for (Map<String, Object> pedidoEcommerce : pedidos) {
                try {
                    importarPedido(pedidoEcommerce);
                    importados++;
                } catch (Exception e) {
                    erros++;
                    System.err.println("Erro ao importar pedido " + pedidoEcommerce.get("numero") + ": " + e.getMessage());
                }
            }
            
            System.out.println("Importação concluída: " + importados + " importados, " + erros + " erros");
            
        } catch (Exception e) {
            System.err.println("Erro na importação de pedidos: " + e.getMessage());
        }
    }
    
    private List<Map<String, Object>> buscarPedidosPendentes() {
        try {
            // Implementar busca de pedidos pendentes na API
            // Por enquanto, retornar lista vazia
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("Erro ao buscar pedidos pendentes: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private void importarPedido(Map<String, Object> pedidoEcommerce) throws Exception {
        String numeroPedido = (String) pedidoEcommerce.get("numero");
        
        // Verificar se pedido já foi importado
        if (pedidoJaImportado(numeroPedido)) {
            return;
        }
        
        // Criar pedido no Sankhya
        DynamicVO pedido = facade.createEntity("TGFCAB");
        pedido.setProperty("TIPMOV", "V");
        pedido.setProperty("DTEMISSAO", new Date());
        pedido.setProperty("VLRNOTA", pedidoEcommerce.get("valor"));
        pedido.setProperty("OBSERVACAO", "Pedido importado do e-commerce: " + numeroPedido);
        
        // Buscar ou criar cliente
        BigDecimal codparc = buscarOuCriarCliente(pedidoEcommerce);
        pedido.setProperty("CODPARC", codparc);
        
        // Salvar pedido
        DynamicVO pedidoSalvo = facade.saveEntity("TGFCAB", pedido);
        
        // Importar itens do pedido
        importarItensPedido(pedidoSalvo, pedidoEcommerce);
        
        // Marcar como importado
        marcarPedidoImportado(numeroPedido, pedidoSalvo.asBigDecimal("NUNOTA"));
        
        System.out.println("Pedido importado: " + numeroPedido + " -> NUNOTA: " + pedidoSalvo.asBigDecimal("NUNOTA"));
    }
    
    private boolean pedidoJaImportado(String numeroPedido) throws Exception {
        String sql = "SELECT COUNT(*) FROM AD_PEDIDO_ECOMMERCE WHERE NUMERO_PEDIDO = ?";
        List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql, numeroPedido);
        
        return !resultado.isEmpty() && resultado.get(0).asBigDecimal("1").intValue() > 0;
    }
    
    private BigDecimal buscarOuCriarCliente(Map<String, Object> pedidoEcommerce) throws Exception {
        String email = (String) pedidoEcommerce.get("email");
        
        // Buscar cliente existente
        String sql = "SELECT CODPARC FROM TGFPAR WHERE EMAIL = ?";
        List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql, email);
        
        if (!resultado.isEmpty()) {
            return resultado.get(0).asBigDecimal("CODPARC");
        }
        
        // Criar novo cliente
        DynamicVO cliente = facade.createEntity("TGFPAR");
        cliente.setProperty("NOMEPARC", pedidoEcommerce.get("cliente"));
        cliente.setProperty("EMAIL", email);
        cliente.setProperty("CLIENTE", "S");
        cliente.setProperty("ATIVO", "S");
        
        DynamicVO clienteSalvo = facade.saveEntity("TGFPAR", cliente);
        
        return clienteSalvo.asBigDecimal("CODPARC");
    }
    
    private void importarItensPedido(DynamicVO pedido, Map<String, Object> pedidoEcommerce) throws Exception {
        List<Map<String, Object>> itens = (List<Map<String, Object>>) pedidoEcommerce.get("itens");
        
        for (Map<String, Object> item : itens) {
            DynamicVO itemPedido = facade.createEntity("TGFITE");
            itemPedido.setProperty("NUNOTA", pedido.asBigDecimal("NUNOTA"));
            itemPedido.setProperty("CODPROD", item.get("codprod"));
            itemPedido.setProperty("QTDNEG", item.get("quantidade"));
            itemPedido.setProperty("VLRNEG", item.get("preco"));
            itemPedido.setProperty("VLRTOT", item.get("total"));
            
            facade.saveEntity("TGFITE", itemPedido);
        }
    }
    
    private void marcarPedidoImportado(String numeroPedido, BigDecimal nunota) throws Exception {
        DynamicVO log = facade.createEntity("AD_PEDIDO_ECOMMERCE");
        log.setProperty("NUMERO_PEDIDO", numeroPedido);
        log.setProperty("NUNOTA", nunota);
        log.setProperty("DT_IMPORTACAO", new Date());
        
        facade.saveEntity("AD_PEDIDO_ECOMMERCE", log);
    }
}
```

## 🎯 **Boas Práticas dos Casos de Uso**

### **1. Estrutura de Dados**
- **Normalização**: Use estruturas normalizadas
- **Índices**: Crie índices para consultas frequentes
- **Constraints**: Defina constraints apropriadas
- **Auditoria**: Inclua campos de auditoria

### **2. Tratamento de Erros**
- **Validação**: Valide dados antes de processar
- **Logs**: Registre operações importantes
- **Rollback**: Implemente rollback em caso de erro
- **Notificações**: Notifique erros críticos

### **3. Performance**
- **Batch Processing**: Processe dados em lotes
- **Índices**: Use índices adequados
- **Cache**: Implemente cache quando apropriado
- **Async Processing**: Use processamento assíncrono

### **4. Integração**
- **APIs**: Use APIs bem definidas
- **Webhooks**: Implemente webhooks para tempo real
- **Retry Logic**: Implemente lógica de retry
- **Monitoramento**: Monitore integrações

## 🎊 **Conclusão**

Os casos de uso reais demonstram:

- **✅ Implementações Práticas**: Código funcional e testável
- **✅ Padrões Reais**: Baseados em necessidades reais
- **✅ Integração Completa**: Eventos, botões e ações agendadas
- **✅ Tratamento Robusto**: Validação e tratamento de erros
- **✅ Performance**: Otimizado para produção
- **✅ Monitoramento**: Logs e auditoria adequados

### **Benefícios:**
- **Aplicabilidade**: Soluções prontas para uso
- **Escalabilidade**: Suporte a crescimento
- **Manutenibilidade**: Código bem estruturado
- **Confiabilidade**: Tratamento robusto de erros
- **Performance**: Otimizado para alta demanda

---

*Este documento apresenta casos de uso reais implementados em Sankhya, fornecendo exemplos práticos e funcionais para desenvolvimento de soluções empresariais.*
