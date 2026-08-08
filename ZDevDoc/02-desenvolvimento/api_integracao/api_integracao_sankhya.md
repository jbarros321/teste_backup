# 🔗 API de Integração Sankhya - Guia Completo

## 🎯 Visão Geral

Este documento apresenta um guia completo sobre APIs de integração no Sankhya, incluindo endpoints, autenticação, exemplos práticos e melhores práticas para integração com sistemas externos.

## 📡 **Endpoints Principais**

### **1. Autenticação**

#### **Login**
```http
POST /api/login
Content-Type: application/json

{
    "usuario": "seu_usuario",
    "senha": "sua_senha",
    "empresa": "codigo_empresa"
}
```

**Resposta:**
```json
{
    "success": true,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "user": {
        "id": 123,
        "nome": "João Silva",
        "email": "joao@empresa.com"
    }
}
```

#### **Refresh Token**
```http
POST /api/refresh
Authorization: Bearer {token}
```

### **2. Produtos**

#### **Listar Produtos**
```http
GET /api/produtos?page=1&limit=100&filtro=nome
Authorization: Bearer {token}
```

#### **Buscar Produto por ID**
```http
GET /api/produtos/{id}
Authorization: Bearer {token}
```

#### **Criar Produto**
```http
POST /api/produtos
Authorization: Bearer {token}
Content-Type: application/json

{
    "codprod": "PROD001",
    "descrprod": "Produto Exemplo",
    "unidade": "UN",
    "vlroferta": 100.00,
    "estmin": 10,
    "estmax": 1000
}
```

#### **Atualizar Produto**
```http
PUT /api/produtos/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
    "descrprod": "Produto Atualizado",
    "vlroferta": 120.00
}
```

### **3. Clientes**

#### **Listar Clientes**
```http
GET /api/clientes?page=1&limit=100
Authorization: Bearer {token}
```

#### **Buscar Cliente por CPF/CNPJ**
```http
GET /api/clientes/buscar?documento=12345678901
Authorization: Bearer {token}
```

#### **Criar Cliente**
```http
POST /api/clientes
Authorization: Bearer {token}
Content-Type: application/json

{
    "nome": "Cliente Exemplo",
    "cgccpf": "12345678901",
    "email": "cliente@exemplo.com",
    "telefone": "(11) 99999-9999",
    "endereco": {
        "logradouro": "Rua Exemplo, 123",
        "cidade": "São Paulo",
        "uf": "SP",
        "cep": "01234-567"
    }
}
```

### **4. Pedidos**

#### **Listar Pedidos**
```http
GET /api/pedidos?dataInicio=2024-01-01&dataFim=2024-12-31
Authorization: Bearer {token}
```

#### **Criar Pedido**
```http
POST /api/pedidos
Authorization: Bearer {token}
Content-Type: application/json

{
    "codparc": 123,
    "dtemissao": "2024-01-15",
    "observacoes": "Pedido via API",
    "itens": [
        {
            "codprod": "PROD001",
            "qtdneg": 2,
            "vlrunit": 50.00
        }
    ]
}
```

#### **Atualizar Status do Pedido**
```http
PUT /api/pedidos/{id}/status
Authorization: Bearer {token}
Content-Type: application/json

{
    "status": "FATURADO"
}
```

### **5. Estoque**

#### **Consultar Saldo**
```http
GET /api/estoque/{codprod}
Authorization: Bearer {token}
```

#### **Atualizar Estoque**
```http
PUT /api/estoque/{codprod}
Authorization: Bearer {token}
Content-Type: application/json

{
    "saldo": 100,
    "motivo": "ENTRADA_MANUAL"
}
```

## 🔐 **Autenticação e Segurança**

### **1. Implementação de Autenticação**

```java
@RestController
@RequestMapping("/api")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.autenticar(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(false, "Credenciais inválidas"));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String token) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            AuthResponse response = authService.refreshToken(cleanToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(false, "Token inválido"));
        }
    }
}
```

### **2. Middleware de Autenticação**

```java
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtService jwtService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        if (isPublicEndpoint(request.getRequestURI())) {
            return true;
        }
        
        String token = extractToken(request);
        if (token == null || !jwtService.validarToken(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Token inválido ou ausente");
            return false;
        }
        
        // Adicionar informações do usuário ao contexto
        UserInfo userInfo = jwtService.extrairUserInfo(token);
        SecurityContextHolder.getContext().setAuthentication(
            new JWTAuthenticationToken(userInfo)
        );
        
        return true;
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private boolean isPublicEndpoint(String uri) {
        return uri.equals("/api/login") || uri.equals("/api/refresh");
    }
}
```

## 📊 **Implementação de Endpoints**

### **1. Controller de Produtos**

```java
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    
    @Autowired
    private ProdutoService produtoService;
    
    @GetMapping
    public ResponseEntity<Page<ProdutoDTO>> listarProdutos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String filtro) {
        
        try {
            Page<ProdutoDTO> produtos = produtoService.listarProdutos(page, limit, filtro);
            return ResponseEntity.ok(produtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDTO> buscarProduto(@PathVariable BigDecimal id) {
        try {
            ProdutoDTO produto = produtoService.buscarPorId(id);
            if (produto != null) {
                return ResponseEntity.ok(produto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<ProdutoDTO> criarProduto(@Valid @RequestBody ProdutoDTO produto) {
        try {
            ProdutoDTO produtoCriado = produtoService.criarProduto(produto);
            return ResponseEntity.status(HttpStatus.CREATED).body(produtoCriado);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoDTO> atualizarProduto(
            @PathVariable BigDecimal id, 
            @Valid @RequestBody ProdutoDTO produto) {
        
        try {
            ProdutoDTO produtoAtualizado = produtoService.atualizarProduto(id, produto);
            if (produtoAtualizado != null) {
                return ResponseEntity.ok(produtoAtualizado);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable BigDecimal id) {
        try {
            boolean deletado = produtoService.deletarProduto(id);
            if (deletado) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
```

### **2. Service de Produtos**

```java
@Service
@Transactional
public class ProdutoService {
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private ProdutoMapper produtoMapper;
    
    public Page<ProdutoDTO> listarProdutos(int page, int limit, String filtro) {
        try {
            Pageable pageable = PageRequest.of(page, limit);
            Page<DynamicVO> produtos;
            
            if (filtro != null && !filtro.trim().isEmpty()) {
                produtos = produtoRepository.buscarComFiltro(filtro, pageable);
            } else {
                produtos = produtoRepository.listarTodos(pageable);
            }
            
            return produtos.map(produtoMapper::toDTO);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao listar produtos", e);
        }
    }
    
    public ProdutoDTO buscarPorId(BigDecimal id) {
        try {
            DynamicVO produto = produtoRepository.buscarPorId(id);
            if (produto != null) {
                return produtoMapper.toDTO(produto);
            }
            return null;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar produto", e);
        }
    }
    
    public ProdutoDTO criarProduto(ProdutoDTO produtoDTO) {
        try {
            // Validar dados
            validarProduto(produtoDTO);
            
            // Verificar se já existe
            if (produtoRepository.existePorCodigo(produtoDTO.getCodprod())) {
                throw new ValidationException("Produto já existe com este código");
            }
            
            // Converter para VO
            DynamicVO produtoVO = produtoMapper.toVO(produtoDTO);
            
            // Salvar
            DynamicVO produtoSalvo = produtoRepository.salvar(produtoVO);
            
            return produtoMapper.toDTO(produtoSalvo);
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao criar produto", e);
        }
    }
    
    public ProdutoDTO atualizarProduto(BigDecimal id, ProdutoDTO produtoDTO) {
        try {
            // Buscar produto existente
            DynamicVO produtoExistente = produtoRepository.buscarPorId(id);
            if (produtoExistente == null) {
                return null;
            }
            
            // Validar dados
            validarProduto(produtoDTO);
            
            // Atualizar campos
            produtoExistente.setProperty("DESCRPROD", produtoDTO.getDescrprod());
            produtoExistente.setProperty("UNIDADE", produtoDTO.getUnidade());
            produtoExistente.setProperty("VLROFERTA", produtoDTO.getVlroferta());
            produtoExistente.setProperty("ESTMIN", produtoDTO.getEstmin());
            produtoExistente.setProperty("ESTMAX", produtoDTO.getEstmax());
            
            // Salvar
            DynamicVO produtoAtualizado = produtoRepository.salvar(produtoExistente);
            
            return produtoMapper.toDTO(produtoAtualizado);
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao atualizar produto", e);
        }
    }
    
    public boolean deletarProduto(BigDecimal id) {
        try {
            DynamicVO produto = produtoRepository.buscarPorId(id);
            if (produto == null) {
                return false;
            }
            
            produtoRepository.deletar(id);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao deletar produto", e);
        }
    }
    
    private void validarProduto(ProdutoDTO produto) {
        if (produto.getCodprod() == null || produto.getCodprod().trim().isEmpty()) {
            throw new ValidationException("Código do produto é obrigatório");
        }
        
        if (produto.getDescrprod() == null || produto.getDescrprod().trim().isEmpty()) {
            throw new ValidationException("Descrição do produto é obrigatória");
        }
        
        if (produto.getVlroferta() != null && produto.getVlroferta().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Valor do produto deve ser positivo");
        }
    }
}
```

### **3. Repository de Produtos**

```java
@Repository
public class ProdutoRepository {
    
    @Autowired
    private EntityFacadeFactory entityFacadeFactory;
    
    public Page<DynamicVO> listarTodos(Pageable pageable) {
        try {
            EntityFacade facade = entityFacadeFactory.getEntityFacade();
            
            String sql = "SELECT * FROM TGFPRO ORDER BY CODPROD";
            
            QueryExecutor executor = facade.getQueryExecutor();
            List<DynamicVO> produtos = executor.executeQuery(sql);
            
            // Implementar paginação manual
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), produtos.size());
            
            List<DynamicVO> pageContent = produtos.subList(start, end);
            
            return new PageImpl<>(pageContent, pageable, produtos.size());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao listar produtos", e);
        }
    }
    
    public Page<DynamicVO> buscarComFiltro(String filtro, Pageable pageable) {
        try {
            EntityFacade facade = entityFacadeFactory.getEntityFacade();
            
            String sql = "SELECT * FROM TGFPRO WHERE UPPER(DESCRPROD) LIKE UPPER(?) ORDER BY CODPROD";
            
            QueryExecutor executor = facade.getQueryExecutor();
            List<DynamicVO> produtos = executor.executeQuery(sql, "%" + filtro + "%");
            
            // Implementar paginação manual
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), produtos.size());
            
            List<DynamicVO> pageContent = produtos.subList(start, end);
            
            return new PageImpl<>(pageContent, pageable, produtos.size());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar produtos", e);
        }
    }
    
    public DynamicVO buscarPorId(BigDecimal id) {
        try {
            EntityFacade facade = entityFacadeFactory.getEntityFacade();
            
            String sql = "SELECT * FROM TGFPRO WHERE CODPROD = ?";
            
            QueryExecutor executor = facade.getQueryExecutor();
            List<DynamicVO> produtos = executor.executeQuery(sql, id);
            
            return produtos.isEmpty() ? null : produtos.get(0);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar produto", e);
        }
    }
    
    public boolean existePorCodigo(String codigo) {
        try {
            EntityFacade facade = entityFacadeFactory.getEntityFacade();
            
            String sql = "SELECT COUNT(*) FROM TGFPRO WHERE CODPROD = ?";
            
            QueryExecutor executor = facade.getQueryExecutor();
            List<Object> resultado = executor.executeQuery(sql, codigo);
            
            return resultado != null && !resultado.isEmpty() && 
                   ((BigDecimal) resultado.get(0)).intValue() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public DynamicVO salvar(DynamicVO produto) {
        try {
            EntityFacade facade = entityFacadeFactory.getEntityFacade();
            
            // Se tem ID, é atualização
            if (produto.getProperty("CODPROD") != null) {
                return facade.updateEntity("TGFPRO", produto);
            } else {
                return facade.createEntity("TGFPRO", produto);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao salvar produto", e);
        }
    }
    
    public void deletar(BigDecimal id) {
        try {
            EntityFacade facade = entityFacadeFactory.getEntityFacade();
            
            DynamicVO produto = facade.findEntityByPrimaryKey("TGFPRO", id);
            if (produto != null) {
                facade.removeEntity("TGFPRO", produto);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao deletar produto", e);
        }
    }
}
```

## 🎯 **DTOs e Mappers**

### **1. DTO de Produto**

```java
public class ProdutoDTO {
    private BigDecimal codprod;
    private String descrprod;
    private String unidade;
    private BigDecimal vlroferta;
    private BigDecimal estmin;
    private BigDecimal estmax;
    private String ativo;
    private BigDecimal saldofisico;
    private Date dtcadastro;
    
    // Getters e setters
}
```

### **2. Mapper de Produto**

```java
@Component
public class ProdutoMapper {
    
    public ProdutoDTO toDTO(DynamicVO vo) {
        if (vo == null) {
            return null;
        }
        
        ProdutoDTO dto = new ProdutoDTO();
        dto.setCodprod(vo.asBigDecimal("CODPROD"));
        dto.setDescrprod(vo.asString("DESCRPROD"));
        dto.setUnidade(vo.asString("UNIDADE"));
        dto.setVlroferta(vo.asBigDecimal("VLROFERTA"));
        dto.setEstmin(vo.asBigDecimal("ESTMIN"));
        dto.setEstmax(vo.asBigDecimal("ESTMAX"));
        dto.setAtivo(vo.asString("ATIVO"));
        dto.setSaldofisico(vo.asBigDecimal("SALDOFISICO"));
        dto.setDtcadastro(vo.asDate("DTCADASTRO"));
        
        return dto;
    }
    
    public DynamicVO toVO(ProdutoDTO dto) {
        if (dto == null) {
            return null;
        }
        
        DynamicVO vo = new DynamicVO();
        vo.setProperty("CODPROD", dto.getCodprod());
        vo.setProperty("DESCRPROD", dto.getDescrprod());
        vo.setProperty("UNIDADE", dto.getUnidade());
        vo.setProperty("VLROFERTA", dto.getVlroferta());
        vo.setProperty("ESTMIN", dto.getEstmin());
        vo.setProperty("ESTMAX", dto.getEstmax());
        vo.setProperty("ATIVO", dto.getAtivo() != null ? dto.getAtivo() : "S");
        vo.setProperty("SALDOFISICO", dto.getSaldofisico());
        vo.setProperty("DTCADASTRO", dto.getDtcadastro() != null ? dto.getDtcadastro() : new Date());
        
        return vo;
    }
}
```

## 📊 **Paginação e Filtros**

### **1. Implementação de Paginação**

```java
public class PaginationUtils {
    
    public static <T> Page<T> criarPagina(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        
        if (start >= items.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, items.size());
        }
        
        List<T> pageContent = items.subList(start, end);
        return new PageImpl<>(pageContent, pageable, items.size());
    }
    
    public static String construirSqlComPaginacao(String sqlBase, Pageable pageable) {
        StringBuilder sql = new StringBuilder(sqlBase);
        
        // Adicionar ORDER BY se não existir
        if (!sqlBase.toUpperCase().contains("ORDER BY")) {
            sql.append(" ORDER BY 1");
        }
        
        // Adicionar paginação
        sql.append(" OFFSET ").append(pageable.getOffset())
           .append(" ROWS FETCH NEXT ").append(pageable.getPageSize()).append(" ROWS ONLY");
        
        return sql.toString();
    }
}
```

### **2. Filtros Avançados**

```java
@Component
public class FiltroService {
    
    public String construirFiltroSQL(Map<String, Object> filtros, String tabela) {
        StringBuilder where = new StringBuilder();
        List<Object> parametros = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : filtros.entrySet()) {
            String campo = entry.getKey();
            Object valor = entry.getValue();
            
            if (valor != null && !valor.toString().trim().isEmpty()) {
                if (where.length() > 0) {
                    where.append(" AND ");
                }
                
                // Construir condição baseada no tipo de campo
                if (campo.endsWith("_LIKE")) {
                    where.append(campo.replace("_LIKE", "")).append(" LIKE ?");
                    parametros.add("%" + valor + "%");
                } else if (campo.endsWith("_DATE_FROM")) {
                    where.append(campo.replace("_DATE_FROM", "")).append(" >= ?");
                    parametros.add(valor);
                } else if (campo.endsWith("_DATE_TO")) {
                    where.append(campo.replace("_DATE_TO", "")).append(" <= ?");
                    parametros.add(valor);
                } else {
                    where.append(campo).append(" = ?");
                    parametros.add(valor);
                }
            }
        }
        
        return where.length() > 0 ? " WHERE " + where.toString() : "";
    }
}
```

## 🛡️ **Tratamento de Erros**

### **1. Exception Handler Global**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
        logger.warn("Erro de validação: {}", e.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            e.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException e) {
        logger.warn("Erro de autorização: {}", e.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "UNAUTHORIZED",
            "Token inválido ou expirado",
            HttpStatus.UNAUTHORIZED.value()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        logger.error("Erro interno: ", e);
        
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "Erro interno do servidor",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### **2. Modelo de Erro**

```java
public class ErrorResponse {
    private String code;
    private String message;
    private int status;
    private long timestamp;
    
    public ErrorResponse(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters e setters
}
```

## 📊 **Monitoramento e Logs**

### **1. Log de Requisições**

```java
@Component
public class RequestLoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("{} {} - Status: {} - Duration: {}ms - User: {}", 
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                httpResponse.getStatus(),
                duration,
                getCurrentUser()
            );
        }
    }
    
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }
}
```

### **2. Métricas de API**

```java
@Component
public class APIMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public APIMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void registrarRequisicao(String endpoint, String method, int statusCode, long duration) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("api.request.duration")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", String.valueOf(statusCode))
                .register(meterRegistry));
        
        Counter.builder("api.request.count")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", String.valueOf(statusCode))
                .register(meterRegistry)
                .increment();
    }
}
```

## 🎯 **Boas Práticas**

### **1. Design da API**
- **RESTful**: Seguir convenções REST
- **Versionamento**: Usar versionamento de API
- **Documentação**: Documentar todos os endpoints
- **Consistência**: Manter padrões consistentes

### **2. Segurança**
- **HTTPS**: Sempre usar HTTPS em produção
- **Autenticação**: Implementar autenticação robusta
- **Autorização**: Controlar acesso aos recursos
- **Rate Limiting**: Implementar limite de requisições

### **3. Performance**
- **Paginação**: Sempre paginar listas grandes
- **Cache**: Implementar cache quando apropriado
- **Índices**: Otimizar consultas com índices
- **Compressão**: Usar compressão de resposta

### **4. Monitoramento**
- **Logs**: Registrar todas as operações importantes
- **Métricas**: Coletar métricas de performance
- **Alertas**: Configurar alertas para falhas
- **Health Checks**: Implementar health checks

## 🎊 **Conclusão**

A API de integração Sankhya deve ser:

- **✅ RESTful**: Seguindo padrões REST
- **✅ Segura**: Com autenticação e autorização
- **✅ Performática**: Com paginação e cache
- **✅ Monitorada**: Com logs e métricas
- **✅ Documentada**: Com documentação clara

### **Endpoints Recomendados:**
1. **Autenticação**: Login, refresh token
2. **Produtos**: CRUD completo
3. **Clientes**: CRUD completo
4. **Pedidos**: Criação e consulta
5. **Estoque**: Consulta e atualização

---

*Este documento fornece um guia completo para implementação de APIs robustas e seguras no Sankhya.*
