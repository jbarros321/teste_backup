# 🏆 Melhores Práticas de Desenvolvimento Sankhya

## 🎯 Visão Geral

Este documento consolida as melhores práticas para desenvolvimento e personalização no Sankhya, baseado em experiência real, padrões de mercado e análise do código fonte SankhyaW.

## 🏗️ **Arquitetura e Design**

### **1. Princípios SOLID**

#### **Single Responsibility Principle (SRP)**
```java
// ❌ ERRADO - Classe com múltiplas responsabilidades
public class PedidoService {
    public void processarPedido(Pedido pedido) {
        validarPedido(pedido);
        calcularImpostos(pedido);
        atualizarEstoque(pedido);
        enviarEmail(pedido);
        gerarRelatorio(pedido);
    }
}

// ✅ CORRETO - Classes com responsabilidade única
public class PedidoValidator {
    public void validarPedido(Pedido pedido) { /* ... */ }
}

public class CalculadoraImpostos {
    public void calcularImpostos(Pedido pedido) { /* ... */ }
}

public class EstoqueService {
    public void atualizarEstoque(Pedido pedido) { /* ... */ }
}

public class EmailService {
    public void enviarEmail(Pedido pedido) { /* ... */ }
}

public class RelatorioService {
    public void gerarRelatorio(Pedido pedido) { /* ... */ }
}
```

#### **Open/Closed Principle (OCP)**
```java
// ✅ CORRETO - Aberto para extensão, fechado para modificação
public abstract class CalculadoraDesconto {
    public abstract BigDecimal calcularDesconto(Pedido pedido);
}

public class DescontoQuantidade extends CalculadoraDesconto {
    public BigDecimal calcularDesconto(Pedido pedido) {
        if (pedido.getQuantidade() > 100) {
            return pedido.getValor().multiply(new BigDecimal("0.10"));
        }
        return BigDecimal.ZERO;
    }
}

public class DescontoFidelidade extends CalculadoraDesconto {
    public BigDecimal calcularDesconto(Pedido pedido) {
        if (pedido.getCliente().isFidelidade()) {
            return pedido.getValor().multiply(new BigDecimal("0.05"));
        }
        return BigDecimal.ZERO;
    }
}
```

### **2. Padrões de Design**

#### **Factory Pattern**
```java
public class ConnectorFactory {
    public static Connector criarConnector(TipoConnector tipo) {
        switch (tipo) {
            case ORACLE:
                return new OracleConnector();
            case SQLSERVER:
                return new SQLServerConnector();
            case REST_API:
                return new RestAPIConnector();
            default:
                throw new IllegalArgumentException("Tipo não suportado: " + tipo);
        }
    }
}
```

#### **Strategy Pattern**
```java
public interface EstrategiaCalculoImposto {
    BigDecimal calcularImposto(BigDecimal valor);
}

public class CalculoICMS implements EstrategiaCalculoImposto {
    public BigDecimal calcularImposto(BigDecimal valor) {
        return valor.multiply(new BigDecimal("0.18"));
    }
}

public class CalculoIPI implements EstrategiaCalculoImposto {
    public BigDecimal calcularImposto(BigDecimal valor) {
        return valor.multiply(new BigDecimal("0.10"));
    }
}

public class CalculadoraImpostos {
    private EstrategiaCalculoImposto estrategia;
    
    public void setEstrategia(EstrategiaCalculoImposto estrategia) {
        this.estrategia = estrategia;
    }
    
    public BigDecimal calcular(BigDecimal valor) {
        return estrategia.calcularImposto(valor);
    }
}
```

## 💻 **Código Limpo**

### **1. Nomenclatura**

#### **Classes e Interfaces**
```java
// ✅ CORRETO - Nomes descritivos
public class PedidoVendaService { }
public class CalculadoraImpostos { }
public class ValidadorCpfCnpj { }
public interface RepositorioPedido { }

// ❌ ERRADO - Nomes genéricos
public class Service { }
public class Helper { }
public class Util { }
public interface Repo { }
```

#### **Métodos**
```java
// ✅ CORRETO - Verbos que descrevem ação
public void processarPedido(Pedido pedido) { }
public BigDecimal calcularTotal(List<Item> itens) { }
public boolean validarCpf(String cpf) { }
public Pedido buscarPedidoPorId(BigDecimal id) { }

// ❌ ERRADO - Nomes genéricos ou confusos
public void process(Pedido p) { }
public BigDecimal calc(List<Item> i) { }
public boolean check(String s) { }
public Pedido get(BigDecimal id) { }
```

#### **Variáveis**
```java
// ✅ CORRETO - Nomes descritivos
BigDecimal valorTotalPedido;
String nomeCliente;
Date dataVencimento;
List<ItemPedido> itensPedido;

// ❌ ERRADO - Nomes genéricos
BigDecimal total;
String nome;
Date data;
List<Item> items;
```

### **2. Métodos Pequenos e Focados**

```java
// ❌ ERRADO - Método muito longo
public void processarPedido(Pedido pedido) {
    // 50+ linhas de código
    if (pedido != null) {
        // validações...
        // cálculos...
        // persistência...
        // notificações...
        // logs...
    }
}

// ✅ CORRETO - Métodos pequenos e focados
public void processarPedido(Pedido pedido) {
    validarPedido(pedido);
    calcularValores(pedido);
    persistirPedido(pedido);
    notificarInteressados(pedido);
    logarProcessamento(pedido);
}

private void validarPedido(Pedido pedido) {
    if (pedido == null) {
        throw new IllegalArgumentException("Pedido não pode ser nulo");
    }
    validarCliente(pedido.getCliente());
    validarItens(pedido.getItens());
}

private void calcularValores(Pedido pedido) {
    calcularSubtotal(pedido);
    calcularDescontos(pedido);
    calcularImpostos(pedido);
    calcularTotal(pedido);
}
```

### **3. Comentários Efetivos**

```java
// ✅ CORRETO - Comentários que explicam "por que", não "o que"
public class CalculadoraImpostos {
    
    // Taxa de ICMS varia por estado, por isso precisamos consultar a UF do cliente
    private static final Map<String, BigDecimal> TAXAS_ICMS_POR_UF = new HashMap<>();
    
    public BigDecimal calcularICMS(Pedido pedido) {
        String uf = pedido.getCliente().getUf();
        BigDecimal taxa = TAXAS_ICMS_POR_UF.get(uf);
        
        if (taxa == null) {
            // Usar taxa padrão para UFs não mapeadas
            taxa = new BigDecimal("0.18");
        }
        
        return pedido.getValor().multiply(taxa);
    }
}

// ❌ ERRADO - Comentários óbvios
public BigDecimal calcularICMS(Pedido pedido) {
    // Buscar UF do cliente
    String uf = pedido.getCliente().getUf();
    
    // Buscar taxa na tabela
    BigDecimal taxa = TAXAS_ICMS_POR_UF.get(uf);
    
    // Multiplicar valor pela taxa
    return pedido.getValor().multiply(taxa);
}
```

## 🛡️ **Tratamento de Erros**

### **1. Exceções Específicas**

```java
// ✅ CORRETO - Exceções específicas
public class PedidoInvalidoException extends Exception {
    public PedidoInvalidoException(String message) {
        super(message);
    }
}

public class ClienteNaoEncontradoException extends Exception {
    public ClienteNaoEncontradoException(BigDecimal codigoCliente) {
        super("Cliente não encontrado: " + codigoCliente);
    }
}

public class EstoqueInsuficienteException extends Exception {
    public EstoqueInsuficienteException(String produto, BigDecimal solicitado, BigDecimal disponivel) {
        super(String.format("Estoque insuficiente para produto %s: solicitado %s, disponível %s", 
                           produto, solicitado, disponivel));
    }
}

// ❌ ERRADO - Uso genérico de RuntimeException
public void processarPedido(Pedido pedido) {
    if (pedido == null) {
        throw new RuntimeException("Erro");
    }
}
```

### **2. Tratamento Defensivo**

```java
// ✅ CORRETO - Validação defensiva
public class PedidoService {
    
    public void processarPedido(Pedido pedido) throws PedidoInvalidoException {
        validarPedido(pedido);
        // processar...
    }
    
    private void validarPedido(Pedido pedido) throws PedidoInvalidoException {
        if (pedido == null) {
            throw new PedidoInvalidoException("Pedido não pode ser nulo");
        }
        
        if (pedido.getCliente() == null) {
            throw new PedidoInvalidoException("Cliente é obrigatório");
        }
        
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new PedidoInvalidoException("Pedido deve ter pelo menos um item");
        }
        
        for (ItemPedido item : pedido.getItens()) {
            if (item.getQuantidade() == null || item.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
                throw new PedidoInvalidoException("Quantidade do item deve ser positiva");
            }
        }
    }
}
```

## 📊 **Performance**

### **1. Otimização de Consultas**

```java
// ❌ ERRADO - N+1 queries
public List<PedidoDTO> listarPedidos() {
    List<DynamicVO> pedidos = facade.findByDynamicFinder("Pedido.findAll");
    
    List<PedidoDTO> resultado = new ArrayList<>();
    for (DynamicVO pedido : pedidos) {
        PedidoDTO dto = new PedidoDTO();
        dto.setCodigo(pedido.asBigDecimal("NUNOTA"));
        
        // Query adicional para cada pedido (N+1 problem)
        List<DynamicVO> itens = facade.findByDynamicFinder("ItemPedido.findByPedido", 
                                                          pedido.asBigDecimal("NUNOTA"));
        dto.setItens(converterItens(itens));
        
        resultado.add(dto);
    }
    return resultado;
}

// ✅ CORRETO - Query otimizada com JOIN
public List<PedidoDTO> listarPedidos() {
    String sql = """
        SELECT p.NUNOTA, p.DTEMISSAO, p.VLRNOTA,
               i.CODPROD, i.DESCRPROD, i.QTDNEG, i.VLRUNIT
        FROM TGFCAB p
        LEFT JOIN TGFITE i ON p.NUNOTA = i.NUNOTA
        ORDER BY p.DTEMISSAO DESC
        """;
    
    List<DynamicVO> resultado = facade.getQueryExecutor().executeQuery(sql);
    return agruparPedidosComItens(resultado);
}
```

### **2. Cache Inteligente**

```java
// ✅ CORRETO - Cache com TTL
@Component
public class ProdutoService {
    
    @Cacheable(value = "produtos", key = "#id")
    public ProdutoDTO buscarProduto(BigDecimal id) {
        // Consulta ao banco apenas se não estiver no cache
        return produtoRepository.buscarPorId(id);
    }
    
    @CacheEvict(value = "produtos", key = "#produto.id")
    public void atualizarProduto(ProdutoDTO produto) {
        produtoRepository.atualizar(produto);
        // Remove do cache para forçar atualização
    }
    
    @CacheEvict(value = "produtos", allEntries = true)
    public void limparCache() {
        // Limpa todo o cache de produtos
    }
}
```

### **3. Paginação Eficiente**

```java
// ✅ CORRETO - Paginação com OFFSET/FETCH
public Page<PedidoDTO> listarPedidosPaginado(Pageable pageable) {
    String sql = """
        SELECT * FROM (
            SELECT p.*, ROW_NUMBER() OVER (ORDER BY p.DTEMISSAO DESC) as rn
            FROM TGFCAB p
            WHERE p.TIPMOV = 'V'
        ) WHERE rn BETWEEN ? AND ?
        """;
    
    int offset = (int) pageable.getOffset() + 1;
    int limit = offset + pageable.getPageSize() - 1;
    
    List<DynamicVO> pedidos = facade.getQueryExecutor().executeQuery(sql, offset, limit);
    
    // Contar total para paginação
    String countSql = "SELECT COUNT(*) FROM TGFCAB WHERE TIPMOV = 'V'";
    List<Object> totalResult = facade.getQueryExecutor().executeQuery(countSql);
    long total = ((BigDecimal) totalResult.get(0)).longValue();
    
    return new PageImpl<>(converterParaDTO(pedidos), pageable, total);
}
```

## 🔒 **Segurança**

### **1. Validação de Entrada**

```java
// ✅ CORRETO - Validação robusta
public class ValidadorEntrada {
    
    public void validarCpf(String cpf) throws ValidationException {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new ValidationException("CPF é obrigatório");
        }
        
        // Remover formatação
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        
        // Validar tamanho
        if (cpfLimpo.length() != 11) {
            throw new ValidationException("CPF deve ter 11 dígitos");
        }
        
        // Validar dígitos verificadores
        if (!ValidadorCpfCnpj.isValidCpf(cpfLimpo)) {
            throw new ValidationException("CPF inválido");
        }
    }
    
    public void validarEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email é obrigatório");
        }
        
        if (!ValidadorEmail.isValidEmail(email)) {
            throw new ValidationException("Email inválido");
        }
        
        // Verificar domínio suspeito
        if (email.toLowerCase().contains("tempmail") || 
            email.toLowerCase().contains("10minutemail")) {
            throw new ValidationException("Domínio de email temporário não permitido");
        }
    }
}
```

### **2. Sanitização de Dados**

```java
// ✅ CORRETO - Sanitização de entrada
public class SanitizadorDados {
    
    public String sanitizarString(String input) {
        if (input == null) {
            return null;
        }
        
        // Remover caracteres perigosos
        return input.replaceAll("[<>\"'&]", "")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
    
    public String sanitizarSQL(String input) {
        if (input == null) {
            return null;
        }
        
        // Escapar caracteres SQL perigosos
        return input.replace("'", "''")
                   .replace("\\", "\\\\")
                   .replace("%", "\\%")
                   .replace("_", "\\_");
    }
    
    public BigDecimal sanitizarDecimal(String input) {
        if (input == null || input.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            // Remover caracteres não numéricos exceto ponto e vírgula
            String cleanInput = input.replaceAll("[^0-9.,]", "")
                                    .replace(",", ".");
            
            return new BigDecimal(cleanInput);
        } catch (NumberFormatException e) {
            throw new ValidationException("Valor numérico inválido: " + input);
        }
    }
}
```

## 📝 **Logging**

### **1. Logging Estruturado**

```java
// ✅ CORRETO - Logs estruturados
@Component
public class PedidoService {
    
    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);
    
    public void processarPedido(Pedido pedido) {
        logger.info("Iniciando processamento do pedido", 
                   "pedidoId", pedido.getId(),
                   "clienteId", pedido.getCliente().getId(),
                   "valorTotal", pedido.getValorTotal());
        
        try {
            validarPedido(pedido);
            calcularValores(pedido);
            persistirPedido(pedido);
            
            logger.info("Pedido processado com sucesso", 
                       "pedidoId", pedido.getId(),
                       "tempoProcessamento", System.currentTimeMillis() - inicio);
            
        } catch (Exception e) {
            logger.error("Erro ao processar pedido", 
                        "pedidoId", pedido.getId(),
                        "erro", e.getMessage(),
                        "stackTrace", e);
            
            throw new RuntimeException("Erro ao processar pedido", e);
        }
    }
}

// ❌ ERRADO - Logs não estruturados
public void processarPedido(Pedido pedido) {
    System.out.println("Processando pedido: " + pedido.getId());
    
    try {
        // processar...
        System.out.println("Pedido processado");
    } catch (Exception e) {
        System.err.println("Erro: " + e.getMessage());
    }
}
```

### **2. Níveis de Log Apropriados**

```java
// ✅ CORRETO - Uso apropriado dos níveis
public class ExemploLogging {
    private static final Logger logger = LoggerFactory.getLogger(ExemploLogging.class);
    
    public void exemploLogs() {
        // TRACE - Informações muito detalhadas para debug
        logger.trace("Entrando no método processarPedido com parâmetros: {}", parametros);
        
        // DEBUG - Informações para debug
        logger.debug("Validando pedido: {}", pedido.getId());
        
        // INFO - Informações gerais importantes
        logger.info("Pedido {} processado com sucesso", pedido.getId());
        
        // WARN - Situações que podem ser problemáticas
        logger.warn("Cliente {} sem limite de crédito definido", cliente.getId());
        
        // ERROR - Erros que precisam de atenção
        logger.error("Falha ao conectar com sistema externo", exception);
        
        // FATAL - Erros críticos que impedem o funcionamento
        logger.fatal("Sistema de banco de dados indisponível", exception);
    }
}
```

## 🧪 **Testes**

### **1. Testes Unitários**

```java
// ✅ CORRETO - Testes unitários bem estruturados
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {
    
    @Mock
    private PedidoRepository pedidoRepository;
    
    @Mock
    private ClienteRepository clienteRepository;
    
    @InjectMocks
    private PedidoService pedidoService;
    
    @Test
    @DisplayName("Deve processar pedido válido com sucesso")
    void deveProcessarPedidoValido() {
        // Arrange
        Pedido pedido = criarPedidoValido();
        Cliente cliente = criarClienteValido();
        
        when(clienteRepository.buscarPorId(pedido.getClienteId())).thenReturn(cliente);
        when(pedidoRepository.salvar(any(Pedido.class))).thenReturn(pedido);
        
        // Act
        Pedido resultado = pedidoService.processarPedido(pedido);
        
        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(StatusPedido.PROCESSADO);
        verify(pedidoRepository).salvar(pedido);
    }
    
    @Test
    @DisplayName("Deve lançar exceção para pedido com cliente inválido")
    void deveLancarExcecaoParaClienteInvalido() {
        // Arrange
        Pedido pedido = criarPedidoValido();
        
        when(clienteRepository.buscarPorId(pedido.getClienteId())).thenReturn(null);
        
        // Act & Assert
        assertThatThrownBy(() -> pedidoService.processarPedido(pedido))
            .isInstanceOf(ClienteNaoEncontradoException.class)
            .hasMessage("Cliente não encontrado: " + pedido.getClienteId());
    }
    
    private Pedido criarPedidoValido() {
        Pedido pedido = new Pedido();
        pedido.setId(BigDecimal.ONE);
        pedido.setClienteId(BigDecimal.valueOf(123));
        pedido.setValorTotal(new BigDecimal("100.00"));
        return pedido;
    }
    
    private Cliente criarClienteValido() {
        Cliente cliente = new Cliente();
        cliente.setId(BigDecimal.valueOf(123));
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@teste.com");
        return cliente;
    }
}
```

### **2. Testes de Integração**

```java
// ✅ CORRETO - Testes de integração
@SpringBootTest
@Transactional
class PedidoServiceIntegrationTest {
    
    @Autowired
    private PedidoService pedidoService;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    @DisplayName("Deve processar pedido completo com persistência")
    void deveProcessarPedidoCompleto() {
        // Arrange
        Cliente cliente = criarECadastrarCliente();
        Pedido pedido = criarPedidoParaCliente(cliente);
        
        // Act
        Pedido resultado = pedidoService.processarPedido(pedido);
        
        // Assert
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(StatusPedido.PROCESSADO);
        
        // Verificar persistência
        Pedido pedidoPersistido = entityManager.find(Pedido.class, resultado.getId());
        assertThat(pedidoPersistido).isNotNull();
        assertThat(pedidoPersistido.getStatus()).isEqualTo(StatusPedido.PROCESSADO);
    }
}
```

## 🎯 **Padrões Sankhya Específicos**

### **1. Eventos Programados**

```java
// ✅ CORRETO - Evento programado bem estruturado
public class PedidoEventoProgramado implements EventoProgramavelJava {
    
    private static final Logger logger = LoggerFactory.getLogger(PedidoEventoProgramado.class);
    
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO pedido = (DynamicVO) event.getVo();
        
        logger.debug("Validando pedido antes da inserção: {}", pedido.asBigDecimal("NUNOTA"));
        
        // Validações de negócio
        validarCliente(pedido);
        validarItens(pedido);
        validarLimiteCredito(pedido);
        
        // Definir valores padrão
        if (pedido.asDate("DTEMISSAO") == null) {
            pedido.setProperty("DTEMISSAO", new Date());
        }
        
        if (pedido.asString("TIPMOV") == null) {
            pedido.setProperty("TIPMOV", "V");
        }
    }
    
    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO pedido = (DynamicVO) event.getVo();
        
        logger.info("Pedido inserido com sucesso: {}", pedido.asBigDecimal("NUNOTA"));
        
        // Ações pós-inserção
        calcularComissoes(pedido);
        enviarNotificacoes(pedido);
        atualizarIndicadores(pedido);
    }
    
    private void validarCliente(DynamicVO pedido) throws Exception {
        BigDecimal codParc = pedido.asBigDecimal("CODPARC");
        if (codParc == null) {
            throw new Exception("Cliente é obrigatório");
        }
        
        // Verificar se cliente existe e está ativo
        String sql = "SELECT COUNT(*) FROM TGFPAR WHERE CODPARC = ? AND ATIVO = 'S'";
        // implementar validação...
    }
}
```

### **2. Botões de Ação**

```java
// ✅ CORRETO - Botão de ação bem estruturado
public class AprovacaoPedidoAction implements AcaoRotinaJava {
    
    private static final Logger logger = LoggerFactory.getLogger(AprovacaoPedidoAction.class);
    
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        Registro[] linhas = contexto.getLinhas();
        
        if (linhas == null || linhas.length == 0) {
            throw new Exception("Nenhum pedido selecionado para aprovação");
        }
        
        int aprovados = 0;
        int rejeitados = 0;
        
        for (Registro linha : linhas) {
            try {
                BigDecimal nunota = linha.getField("NUNOTA");
                
                logger.info("Aprovando pedido: {}", nunota);
                
                if (podeAprovar(nunota, contexto.getUsuarioLogado())) {
                    aprovarPedido(nunota, contexto.getUsuarioLogado());
                    aprovados++;
                    
                    logger.info("Pedido {} aprovado com sucesso", nunota);
                } else {
                    rejeitarPedido(nunota, "Usuário sem permissão para aprovação");
                    rejeitados++;
                    
                    logger.warn("Pedido {} rejeitado - sem permissão", nunota);
                }
                
            } catch (Exception e) {
                logger.error("Erro ao processar pedido {}: {}", 
                           linha.getField("NUNOTA"), e.getMessage());
                rejeitados++;
            }
        }
        
        // Mensagem de resultado
        String mensagem = String.format("Processamento concluído: %d aprovados, %d rejeitados", 
                                       aprovados, rejeitados);
        contexto.setMensagemRetorno(mensagem);
        
        logger.info("Aprovação em lote concluída: {}", mensagem);
    }
    
    private boolean podeAprovar(BigDecimal nunota, BigDecimal usuario) {
        // Implementar lógica de autorização
        return true; // Simplificado
    }
    
    private void aprovarPedido(BigDecimal nunota, BigDecimal usuario) throws Exception {
        // Implementar lógica de aprovação
    }
}
```

## 🎊 **Conclusão**

As melhores práticas de desenvolvimento Sankhya incluem:

- **✅ Arquitetura Limpa**: Princípios SOLID e padrões de design
- **✅ Código Limpo**: Nomenclatura clara e métodos focados
- **✅ Tratamento de Erros**: Exceções específicas e validação defensiva
- **✅ Performance**: Consultas otimizadas e cache inteligente
- **✅ Segurança**: Validação e sanitização de dados
- **✅ Logging**: Logs estruturados e níveis apropriados
- **✅ Testes**: Cobertura adequada com testes unitários e de integração
- **✅ Padrões Sankhya**: Eventos e botões de ação bem estruturados

### **Benefícios:**
- **Manutenibilidade**: Código mais fácil de manter e evoluir
- **Confiabilidade**: Menos bugs e maior estabilidade
- **Performance**: Aplicações mais rápidas e eficientes
- **Segurança**: Proteção contra vulnerabilidades
- **Produtividade**: Desenvolvimento mais ágil e eficiente

---

*Este documento consolida as melhores práticas essenciais para desenvolvimento de alta qualidade no Sankhya.*
