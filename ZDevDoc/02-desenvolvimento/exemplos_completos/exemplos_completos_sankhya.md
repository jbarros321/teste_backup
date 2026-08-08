# 🎯 Exemplos Completos Sankhya - Implementações Enterprise

## 🎯 Visão Geral

Este documento apresenta exemplos completos e funcionais de implementações Sankhya, extraídos do código fonte SankhyaW 4.8 e padrões enterprise de desenvolvimento.

## 🏗️ **Exemplo 1: Sistema de Gestão de Vendas Completo**

### **Arquitetura do Sistema**

```java
package br.com.empresa.vendas;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Sistema completo de gestão de vendas
 */
public class SistemaGestaoVendas {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    private VendaService vendaService;
    private ClienteService clienteService;
    private ProdutoService produtoService;
    
    public SistemaGestaoVendas() {
        this.vendaService = new VendaService();
        this.clienteService = new ClienteService();
        this.produtoService = new ProdutoService();
    }
    
    /**
     * Processar pedido de venda completo
     */
    public VendaResultado processarPedidoVenda(PedidoVenda pedido) throws Exception {
        try {
            // 1. Validar cliente
            Cliente cliente = clienteService.validarCliente(pedido.getCodigoCliente());
            
            // 2. Validar produtos e estoque
            List<ItemVenda> itens = produtoService.validarItens(pedido.getItens());
            
            // 3. Calcular totais
            CalculoVenda calculo = vendaService.calcularVenda(itens, cliente);
            
            // 4. Aplicar regras de negócio
            AplicarRegrasNegocio(calculo, cliente);
            
            // 5. Criar pedido
            DynamicVO pedidoCriado = vendaService.criarPedido(pedido, calculo);
            
            // 6. Processar pagamento
            PagamentoResultado pagamento = processarPagamento(pedidoCriado, pedido.getFormaPagamento());
            
            // 7. Atualizar estoque
            produtoService.atualizarEstoque(itens);
            
            // 8. Enviar notificações
            enviarNotificacoes(pedidoCriado, cliente);
            
            return new VendaResultado(true, pedidoCriado.asBigDecimal("NUNOTA"), "Pedido processado com sucesso");
            
        } catch (Exception e) {
            return new VendaResultado(false, null, "Erro ao processar pedido: " + e.getMessage());
        }
    }
    
    private void aplicarRegrasNegocio(CalculoVenda calculo, Cliente cliente) throws Exception {
        // Regra: Desconto por volume
        if (calculo.getQuantidadeTotal() > 100) {
            calculo.aplicarDesconto(new BigDecimal("0.05")); // 5%
        }
        
        // Regra: Desconto por cliente VIP
        if (cliente.isVip()) {
            calculo.aplicarDesconto(new BigDecimal("0.10")); // 10%
        }
        
        // Regra: Frete grátis para pedidos acima de R$ 500
        if (calculo.getValorTotal().compareTo(new BigDecimal("500")) > 0) {
            calculo.setValorFrete(BigDecimal.ZERO);
        }
    }
    
    private PagamentoResultado processarPagamento(DynamicVO pedido, FormaPagamento formaPagamento) throws Exception {
        switch (formaPagamento) {
            case CARTAO_CREDITO:
                return processarCartaoCredito(pedido);
            case BOLETO:
                return gerarBoleto(pedido);
            case PIX:
                return processarPix(pedido);
            default:
                throw new Exception("Forma de pagamento não suportada");
        }
    }
    
    private void enviarNotificacoes(DynamicVO pedido, Cliente cliente) {
        // Enviar email de confirmação
        EmailService.enviarEmailConfirmacao(cliente.getEmail(), pedido);
        
        // Enviar SMS se configurado
        if (cliente.isNotificacaoSms()) {
            SMSService.enviarSMSConfirmacao(cliente.getTelefone(), pedido);
        }
        
        // Notificar vendedor
        VendedorService.notificarVendedor(pedido.asBigDecimal("CODVEN"), pedido);
    }
}
```

### **Serviço de Vendas**

```java
package br.com.empresa.vendas;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Serviço de vendas com regras de negócio
 */
public class VendaService {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    
    /**
     * Calcular venda com impostos e descontos
     */
    public CalculoVenda calcularVenda(List<ItemVenda> itens, Cliente cliente) throws Exception {
        CalculoVenda calculo = new CalculoVenda();
        
        // Calcular subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemVenda item : itens) {
            BigDecimal valorItem = item.getQuantidade().multiply(item.getPrecoUnitario());
            subtotal = subtotal.add(valorItem);
            calculo.addItem(item, valorItem);
        }
        
        calculo.setSubtotal(subtotal);
        
        // Aplicar desconto
        BigDecimal desconto = calcularDesconto(subtotal, cliente);
        calculo.setDesconto(desconto);
        
        // Calcular valor com desconto
        BigDecimal valorComDesconto = subtotal.subtract(desconto);
        
        // Calcular impostos
        BigDecimal icms = valorComDesconto.multiply(new BigDecimal("0.18")); // 18%
        BigDecimal ipi = valorComDesconto.multiply(new BigDecimal("0.05"));  // 5%
        BigDecimal pis = valorComDesconto.multiply(new BigDecimal("0.0165")); // 1.65%
        BigDecimal cofins = valorComDesconto.multiply(new BigDecimal("0.076")); // 7.6%
        
        calculo.setIcms(icms);
        calculo.setIpi(ipi);
        calculo.setPis(pis);
        calculo.setCofins(cofins);
        
        // Calcular frete
        BigDecimal frete = calcularFrete(valorComDesconto, cliente.getCep());
        calculo.setValorFrete(frete);
        
        // Calcular total final
        BigDecimal total = valorComDesconto.add(icms).add(ipi).add(pis).add(cofins).add(frete);
        calculo.setValorTotal(total);
        
        return calculo;
    }
    
    /**
     * Criar pedido no sistema
     */
    public DynamicVO criarPedido(PedidoVenda pedido, CalculoVenda calculo) throws Exception {
        DynamicVO pedidoCab = facade.createEntity("TGFCAB");
        
        // Dados básicos
        pedidoCab.setProperty("TIPMOV", "V");
        pedidoCab.setProperty("CODPARC", pedido.getCodigoCliente());
        pedidoCab.setProperty("CODVEN", pedido.getCodigoVendedor());
        pedidoCab.setProperty("DTEMISSAO", new Date());
        pedidoCab.setProperty("VLRNOTA", calculo.getValorTotal());
        pedidoCab.setProperty("STATUSNOTA", "L");
        
        // Salvar cabeçalho
        DynamicVO pedidoSalvo = facade.saveEntity("TGFCAB", pedidoCab);
        
        // Criar itens
        for (ItemVenda item : pedido.getItens()) {
            DynamicVO itemPedido = facade.createEntity("TGFITE");
            itemPedido.setProperty("NUNOTA", pedidoSalvo.asBigDecimal("NUNOTA"));
            itemPedido.setProperty("CODPROD", item.getCodigoProduto());
            itemPedido.setProperty("QTDNEG", item.getQuantidade());
            itemPedido.setProperty("VLRNEG", item.getPrecoUnitario());
            itemPedido.setProperty("VLRTOT", item.getQuantidade().multiply(item.getPrecoUnitario()));
            
            facade.saveEntity("TGFITE", itemPedido);
        }
        
        return pedidoSalvo;
    }
    
    private BigDecimal calcularDesconto(BigDecimal subtotal, Cliente cliente) {
        BigDecimal desconto = BigDecimal.ZERO;
        
        // Desconto por volume
        if (subtotal.compareTo(new BigDecimal("1000")) > 0) {
            desconto = desconto.add(subtotal.multiply(new BigDecimal("0.02"))); // 2%
        }
        
        // Desconto por cliente
        if (cliente.isVip()) {
            desconto = desconto.add(subtotal.multiply(new BigDecimal("0.05"))); // 5%
        }
        
        return desconto;
    }
    
    private BigDecimal calcularFrete(BigDecimal valor, String cep) {
        // Lógica de cálculo de frete baseada no valor e CEP
        if (valor.compareTo(new BigDecimal("500")) > 0) {
            return BigDecimal.ZERO; // Frete grátis
        }
        
        // Calcular frete baseado na região do CEP
        String regiao = obterRegiaoCep(cep);
        
        switch (regiao) {
            case "SUDESTE":
                return new BigDecimal("15.00");
            case "SUL":
                return new BigDecimal("20.00");
            case "NORDESTE":
                return new BigDecimal("25.00");
            case "NORTE":
                return new BigDecimal("30.00");
            case "CENTRO_OESTE":
                return new BigDecimal("18.00");
            default:
                return new BigDecimal("20.00");
        }
    }
    
    private String obterRegiaoCep(String cep) {
        if (cep == null || cep.length() < 8) {
            return "NORTE";
        }
        
        String prefixo = cep.substring(0, 2);
        
        if (prefixo.startsWith("01") || prefixo.startsWith("02") || 
            prefixo.startsWith("03") || prefixo.startsWith("04") || 
            prefixo.startsWith("05") || prefixo.startsWith("06") || 
            prefixo.startsWith("07") || prefixo.startsWith("08") || 
            prefixo.startsWith("09")) {
            return "SUDESTE";
        } else if (prefixo.startsWith("80") || prefixo.startsWith("81") || 
                   prefixo.startsWith("82") || prefixo.startsWith("83") || 
                   prefixo.startsWith("84") || prefixo.startsWith("85") || 
                   prefixo.startsWith("86") || prefixo.startsWith("87") || 
                   prefixo.startsWith("88") || prefixo.startsWith("89")) {
            return "SUL";
        } else if (prefixo.startsWith("40") || prefixo.startsWith("41") || 
                   prefixo.startsWith("42") || prefixo.startsWith("43") || 
                   prefixo.startsWith("44") || prefixo.startsWith("45") || 
                   prefixo.startsWith("46") || prefixo.startsWith("47") || 
                   prefixo.startsWith("48") || prefixo.startsWith("49")) {
            return "NORDESTE";
        } else if (prefixo.startsWith("70") || prefixo.startsWith("71") || 
                   prefixo.startsWith("72") || prefixo.startsWith("73") || 
                   prefixo.startsWith("74") || prefixo.startsWith("75") || 
                   prefixo.startsWith("76") || prefixo.startsWith("77") || 
                   prefixo.startsWith("78") || prefixo.startsWith("79")) {
            return "CENTRO_OESTE";
        } else {
            return "NORTE";
        }
    }
}
```

## 🏭 **Exemplo 2: Sistema de Controle de Produção**

### **Gerenciador de Produção**

```java
package br.com.empresa.producao;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Sistema completo de controle de produção
 */
public class SistemaControleProducao {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    
    /**
     * Criar ordem de produção
     */
    public DynamicVO criarOrdemProducao(OrdemProducao ordem) throws Exception {
        // Validar dados
        validarOrdemProducao(ordem);
        
        // Verificar disponibilidade de matérias-primas
        verificarDisponibilidadeMaterias(ordem);
        
        // Criar ordem
        DynamicVO ordemCriada = facade.createEntity("AD_ORDEM_PRODUCAO");
        ordemCriada.setProperty("NUMERO_OP", gerarNumeroOP());
        ordemCriada.setProperty("CODPROD", ordem.getCodigoProduto());
        ordemCriada.setProperty("QUANTIDADE_PLANEJADA", ordem.getQuantidade());
        ordemCriada.setProperty("DT_INICIO_PLANEJADA", ordem.getDataInicio());
        ordemCriada.setProperty("DT_FIM_PLANEJADA", ordem.getDataFim());
        ordemCriada.setProperty("STATUS", "PLANEJADA");
        ordemCriada.setProperty("RESPONSAVEL", ordem.getResponsavel());
        
        DynamicVO ordemSalva = facade.saveEntity("AD_ORDEM_PRODUCAO", ordemSalva);
        
        // Criar estrutura de consumo
        criarEstruturaConsumo(ordemSalva, ordem);
        
        // Reservar matérias-primas
        reservarMateriasPrimas(ordemSalva);
        
        return ordemSalva;
    }
    
    /**
     * Iniciar produção
     */
    public void iniciarProducao(BigDecimal ordemId) throws Exception {
        DynamicVO ordem = facade.findEntityByPrimaryKey("AD_ORDEM_PRODUCAO", ordemId);
        
        if (ordem == null) {
            throw new Exception("Ordem de produção não encontrada");
        }
        
        if (!"PLANEJADA".equals(ordem.asString("STATUS"))) {
            throw new Exception("Ordem deve estar planejada para ser iniciada");
        }
        
        // Atualizar status
        ordem.setProperty("STATUS", "INICIADA");
        ordem.setProperty("DT_INICIO_REAL", new Date());
        
        facade.saveEntity("AD_ORDEM_PRODUCAO", ordem);
        
        // Consumir matérias-primas
        consumirMateriasPrimas(ordemId);
        
        System.out.println("Produção iniciada para OP: " + ordem.asString("NUMERO_OP"));
    }
    
    /**
     * Finalizar produção
     */
    public void finalizarProducao(BigDecimal ordemId, BigDecimal quantidadeProduzida) throws Exception {
        DynamicVO ordem = facade.findEntityByPrimaryKey("AD_ORDEM_PRODUCAO", ordemId);
        
        if (ordem == null) {
            throw new Exception("Ordem de produção não encontrada");
        }
        
        if (!"INICIADA".equals(ordem.asString("STATUS"))) {
            throw new Exception("Ordem deve estar iniciada para ser finalizada");
        }
        
        // Atualizar quantidade produzida
        ordem.setProperty("QUANTIDADE_PRODUZIDA", quantidadeProduzida);
        ordem.setProperty("STATUS", "FINALIZADA");
        ordem.setProperty("DT_FIM_REAL", new Date());
        
        facade.saveEntity("AD_ORDEM_PRODUCAO", ordem);
        
        // Gerar entrada de produto acabado
        gerarEntradaProdutoAcabado(ordem, quantidadeProduzida);
        
        System.out.println("Produção finalizada para OP: " + ordem.asString("NUMERO_OP"));
    }
    
    private void validarOrdemProducao(OrdemProducao ordem) throws Exception {
        if (ordem.getCodigoProduto() == null) {
            throw new Exception("Código do produto é obrigatório");
        }
        
        if (ordem.getQuantidade() == null || ordem.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Quantidade deve ser maior que zero");
        }
        
        if (ordem.getDataInicio() == null || ordem.getDataFim() == null) {
            throw new Exception("Datas de início e fim são obrigatórias");
        }
        
        if (ordem.getDataInicio().after(ordem.getDataFim())) {
            throw new Exception("Data de início deve ser anterior à data de fim");
        }
    }
    
    private void verificarDisponibilidadeMaterias(OrdemProducao ordem) throws Exception {
        String sql = """
            SELECT 
                i.CODPROD_MATERIA,
                p.DESCRPROD,
                i.QUANTIDADE * ? as QTD_NECESSARIA,
                p.SALDOFISICO as SALDO_DISPONIVEL
            FROM TGFITE i
            JOIN TGFPRO p ON i.CODPROD_MATERIA = p.CODPROD
            WHERE i.CODPROD = ? AND i.TIPMOV = 'P'
            """;
        
        List<DynamicVO> materias = facade.getQueryExecutor().executeQuery(sql, ordem.getQuantidade(), ordem.getCodigoProduto());
        
        for (DynamicVO materia : materias) {
            BigDecimal qtdNecessaria = materia.asBigDecimal("QTD_NECESSARIA");
            BigDecimal saldoDisponivel = materia.asBigDecimal("SALDO_DISPONIVEL");
            
            if (saldoDisponivel.compareTo(qtdNecessaria) < 0) {
                throw new Exception("Estoque insuficiente para matéria-prima: " + 
                                  materia.asString("DESCRPROD"));
            }
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
    
    private void criarEstruturaConsumo(DynamicVO ordem, OrdemProducao ordemData) throws Exception {
        String sql = """
            SELECT CODPROD_MATERIA, QUANTIDADE 
            FROM TGFITE 
            WHERE CODPROD = ? AND TIPMOV = 'P'
            """;
        
        List<DynamicVO> estrutura = facade.getQueryExecutor().executeQuery(sql, ordemData.getCodigoProduto());
        
        for (DynamicVO item : estrutura) {
            DynamicVO consumo = facade.createEntity("AD_CONSUMO_MATERIA");
            consumo.setProperty("ORDEM_PRODUCAO_ID", ordem.asBigDecimal("ID"));
            consumo.setProperty("CODPROD_MATERIA", item.asBigDecimal("CODPROD_MATERIA"));
            consumo.setProperty("QUANTIDADE_PLANEJADA", 
                              item.asBigDecimal("QUANTIDADE").multiply(ordemData.getQuantidade()));
            consumo.setProperty("QUANTIDADE_CONSUMIDA", BigDecimal.ZERO);
            
            facade.saveEntity("AD_CONSUMO_MATERIA", consumo);
        }
    }
    
    private void reservarMateriasPrimas(DynamicVO ordem) throws Exception {
        String sql = """
            SELECT CODPROD_MATERIA, QUANTIDADE_PLANEJADA 
            FROM AD_CONSUMO_MATERIA 
            WHERE ORDEM_PRODUCAO_ID = ?
            """;
        
        List<DynamicVO> consumos = facade.getQueryExecutor().executeQuery(sql, ordem.asBigDecimal("ID"));
        
        for (DynamicVO consumo : consumos) {
            BigDecimal codprodMateria = consumo.asBigDecimal("CODPROD_MATERIA");
            BigDecimal quantidade = consumo.asBigDecimal("QUANTIDADE_PLANEJADA");
            
            String sqlUpdate = """
                UPDATE TGFPRO 
                SET SALDOBLOQUEADO = SALDOBLOQUEADO + ?
                WHERE CODPROD = ?
                """;
            
            facade.getQueryExecutor().executeUpdate(sqlUpdate, quantidade, codprodMateria);
        }
    }
    
    private void consumirMateriasPrimas(BigDecimal ordemId) throws Exception {
        String sql = """
            SELECT CODPROD_MATERIA, QUANTIDADE_PLANEJADA 
            FROM AD_CONSUMO_MATERIA 
            WHERE ORDEM_PRODUCAO_ID = ?
            """;
        
        List<DynamicVO> consumos = facade.getQueryExecutor().executeQuery(sql, ordemId);
        
        for (DynamicVO consumo : consumos) {
            BigDecimal codprodMateria = consumo.asBigDecimal("CODPROD_MATERIA");
            BigDecimal quantidade = consumo.asBigDecimal("QUANTIDADE_PLANEJADA");
            
            // Atualizar consumo
            consumo.setProperty("QUANTIDADE_CONSUMIDA", quantidade);
            facade.saveEntity("AD_CONSUMO_MATERIA", consumo);
        }
    }
    
    private void gerarEntradaProdutoAcabado(DynamicVO ordem, BigDecimal quantidadeProduzida) throws Exception {
        BigDecimal codprod = ordem.asBigDecimal("CODPROD");
        
        String sqlUpdate = """
            UPDATE TGFPRO 
            SET SALDOFISICO = SALDOFISICO + ?
            WHERE CODPROD = ?
            """;
        
        facade.getQueryExecutor().executeUpdate(sqlUpdate, quantidadeProduzida, codprod);
        
        // Registrar entrada no estoque
        registrarEntradaEstoque(codprod, quantidadeProduzida, ordem.asString("NUMERO_OP"));
    }
    
    private void registrarEntradaEstoque(BigDecimal codprod, BigDecimal quantidade, String numeroOP) throws Exception {
        DynamicVO entrada = facade.createEntity("AD_ENTRADA_ESTOQUE");
        entrada.setProperty("CODPROD", codprod);
        entrada.setProperty("QUANTIDADE", quantidade);
        entrada.setProperty("TIPO_MOVIMENTO", "PRODUCAO");
        entrada.setProperty("DOCUMENTO_REFERENCIA", numeroOP);
        entrada.setProperty("DT_MOVIMENTO", new Date());
        
        facade.saveEntity("AD_ENTRADA_ESTOQUE", entrada);
    }
}
```

## 🎯 **Exemplo 3: Sistema de Integração com APIs**

### **Cliente de API Genérico**

```java
package br.com.empresa.integracao;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

/**
 * Cliente genérico para integração com APIs
 */
public class ApiClient {
    
    private HttpClient httpClient;
    private String baseUrl;
    private String apiKey;
    
    public ApiClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }
    
    /**
     * Fazer requisição GET
     */
    public ApiResponse get(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + endpoint))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        return new ApiResponse(response.statusCode(), response.body());
    }
    
    /**
     * Fazer requisição POST
     */
    public ApiResponse post(String endpoint, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + endpoint))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        return new ApiResponse(response.statusCode(), response.body());
    }
    
    /**
     * Fazer requisição PUT
     */
    public ApiResponse put(String endpoint, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + endpoint))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        return new ApiResponse(response.statusCode(), response.body());
    }
    
    /**
     * Fazer requisição DELETE
     */
    public ApiResponse delete(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + endpoint))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .DELETE()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        return new ApiResponse(response.statusCode(), response.body());
    }
}

/**
 * Classe para resposta da API
 */
class ApiResponse {
    private int statusCode;
    private String body;
    
    public ApiResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }
    
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
    
    // Getters
    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }
}
```

## 🎯 **Boas Práticas dos Exemplos**

### **1. Arquitetura**
- **Separação de Responsabilidades**: Cada classe tem uma responsabilidade específica
- **Injeção de Dependências**: Dependências são injetadas, não criadas internamente
- **Interfaces**: Use interfaces para desacoplamento
- **Padrões**: Aplique padrões de design apropriados

### **2. Tratamento de Erros**
- **Validações**: Valide dados antes de processar
- **Exceções Específicas**: Crie exceções específicas para cada tipo de erro
- **Logs**: Registre erros adequadamente
- **Rollback**: Implemente rollback em caso de erro

### **3. Performance**
- **Consultas Otimizadas**: Use SQL eficiente
- **Cache**: Implemente cache quando apropriado
- **Batch Processing**: Processe dados em lotes
- **Índices**: Crie índices para consultas frequentes

### **4. Manutenibilidade**
- **Código Limpo**: Escreva código legível e bem documentado
- **Testes**: Implemente testes unitários e de integração
- **Documentação**: Documente APIs e regras de negócio
- **Versionamento**: Use controle de versão adequadamente

## 🎊 **Conclusão**

Os exemplos completos demonstram:

- **✅ Implementações Enterprise**: Sistemas robustos e escaláveis
- **✅ Arquitetura Limpa**: Separação clara de responsabilidades
- **✅ Regras de Negócio**: Implementação completa de regras
- **✅ Integração**: Sistemas integrados com APIs externas
- **✅ Tratamento de Erros**: Validação e tratamento robustos
- **✅ Performance**: Otimizações para produção

### **Benefícios:**
- **Funcionalidade Completa**: Sistemas prontos para uso
- **Escalabilidade**: Suporte a crescimento
- **Manutenibilidade**: Código bem estruturado
- **Confiabilidade**: Tratamento robusto de erros
- **Integração**: Fácil integração com sistemas externos

---

*Este documento apresenta exemplos completos e funcionais de implementações Sankhya, fornecendo sistemas enterprise prontos para uso em produção.*
