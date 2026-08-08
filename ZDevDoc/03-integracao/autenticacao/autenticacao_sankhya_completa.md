# 🔐 Autenticação Sankhya - Guia Completo

## 🎯 Visão Geral

Este documento apresenta um guia completo sobre autenticação no Sankhya, incluindo métodos de autenticação, segurança, tokens, e integração com sistemas externos.

## 🔑 **Tipos de Autenticação**

### **1. Autenticação por Usuário e Senha**

#### **Implementação Básica**
```java
// Classe para autenticação básica
public class AutenticacaoBasica {
    
    public boolean autenticar(String usuario, String senha) {
        try {
            // Hash da senha
            String senhaHash = Crypter.hashPassword(senha);
            
            // Consultar usuário no banco
            String sql = "SELECT CODPARC, SENHA, ATIVO FROM TGFPAR WHERE EMAIL = ? AND ATIVO = 'S'";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, usuario);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String senhaBanco = rs.getString("SENHA");
                    boolean ativo = "S".equals(rs.getString("ATIVO"));
                    
                    if (ativo && Crypter.verifyPassword(senha, senhaBanco)) {
                        return true;
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
}
```

#### **Validações de Segurança**
```java
public class ValidacaoSeguranca {
    
    public void validarTentativasLogin(String usuario, String ip) throws Exception {
        // Verificar tentativas de login
        String sql = "SELECT COUNT(*) FROM TSILOG " +
                    "WHERE USUARIO = ? AND IP = ? AND DTLOG >= ? AND ACAO = 'LOGIN_FALHA'";
        
        Date limiteTempo = new Date(System.currentTimeMillis() - (30 * 60 * 1000)); // 30 minutos
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario);
            stmt.setString(2, ip);
            stmt.setTimestamp(3, new Timestamp(limiteTempo.getTime()));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) >= 5) {
                throw new Exception("Muitas tentativas de login. Acesso bloqueado por 30 minutos.");
            }
        }
    }
    
    public void registrarTentativaLogin(String usuario, String ip, boolean sucesso) {
        String acao = sucesso ? "LOGIN_SUCESSO" : "LOGIN_FALHA";
        
        String sql = "INSERT INTO TSILOG (USUARIO, IP, ACAO, DTLOG) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario);
            stmt.setString(2, ip);
            stmt.setString(3, acao);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### **2. Autenticação por Token JWT**

#### **Geração de Tokens**
```java
public class JWTAuthentication {
    
    private static final String SECRET_KEY = "sankhya_secret_key_2024";
    private static final int TOKEN_VALIDITY = 24 * 60 * 60; // 24 horas
    
    public String gerarToken(BigDecimal userId, String usuario, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("usuario", usuario);
        claims.put("roles", roles);
        claims.put("issuedAt", System.currentTimeMillis());
        claims.put("expiresAt", System.currentTimeMillis() + (TOKEN_VALIDITY * 1000));
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (TOKEN_VALIDITY * 1000)))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }
    
    public boolean validarToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public Claims extrairClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}
```

#### **Middleware de Autenticação**
```java
@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String token = extrairToken(request);
        
        if (token != null && jwtAuthentication.validarToken(token)) {
            Claims claims = jwtAuthentication.extrairClaims(token);
            String usuario = claims.getSubject();
            BigDecimal userId = claims.get("userId", BigDecimal.class);
            
            // Configurar contexto de segurança
            SecurityContextHolder.getContext().setAuthentication(
                new JWTAuthenticationToken(userId, usuario, null)
            );
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extrairToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### **3. Autenticação OAuth 2.0**

#### **Implementação OAuth 2.0**
```java
@Configuration
@EnableAuthorizationServer
public class OAuth2Config extends AuthorizationServerConfigurerAdapter {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("sankhya-client")
                .secret(passwordEncoder().encode("sankhya-secret"))
                .authorizedGrantTypes("authorization_code", "refresh_token", "password")
                .scopes("read", "write")
                .redirectUris("http://localhost:8080/callback")
                .accessTokenValiditySeconds(3600)
                .refreshTokenValiditySeconds(7200);
    }
    
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                .tokenStore(tokenStore())
                .accessTokenConverter(accessTokenConverter());
    }
    
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }
    
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("sankhya_oauth_secret");
        return converter;
    }
}
```

### **4. Autenticação por Certificado Digital**

#### **Validação de Certificado**
```java
public class CertificadoDigitalAuth {
    
    public boolean validarCertificado(X509Certificate certificado) {
        try {
            // Verificar validade do certificado
            certificado.checkValidity();
            
            // Verificar se o certificado está revogado
            if (verificarRevogacao(certificado)) {
                return false;
            }
            
            // Verificar cadeia de certificação
            if (!verificarCadeiaCertificacao(certificado)) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean verificarRevogacao(X509Certificate certificado) {
        try {
            // Implementar verificação de CRL ou OCSP
            // Consultar lista de certificados revogados
            return false; // Simplificado para exemplo
        } catch (Exception e) {
            return true; // Em caso de erro, considerar revogado
        }
    }
    
    private boolean verificarCadeiaCertificacao(X509Certificate certificado) {
        try {
            // Implementar verificação da cadeia de certificação
            // Verificar se o certificado é confiável
            return true; // Simplificado para exemplo
        } catch (Exception e) {
            return false;
        }
    }
}
```

## 🛡️ **Segurança e Autorização**

### **1. Controle de Acesso Baseado em Roles (RBAC)**

#### **Definição de Roles**
```java
@Entity
@Table(name = "AD_ROLES")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigDecimal id;
    
    @Column(name = "NOME")
    private String nome;
    
    @Column(name = "DESCRICAO")
    private String descricao;
    
    @Column(name = "ATIVO")
    private String ativo;
    
    @ManyToMany(mappedBy = "roles")
    private Set<Usuario> usuarios;
    
    @ManyToMany
    @JoinTable(
        name = "AD_ROLE_PERMISSIONS",
        joinColumns = @JoinColumn(name = "ROLE_ID"),
        inverseJoinColumns = @JoinColumn(name = "PERMISSION_ID")
    )
    private Set<Permission> permissions;
}
```

#### **Sistema de Permissões**
```java
@Entity
@Table(name = "AD_PERMISSIONS")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigDecimal id;
    
    @Column(name = "RECURSO")
    private String recurso;
    
    @Column(name = "ACAO")
    private String acao;
    
    @Column(name = "DESCRICAO")
    private String descricao;
    
    // Getters e setters
}

// Serviço de autorização
@Service
public class AuthorizationService {
    
    public boolean hasPermission(BigDecimal userId, String recurso, String acao) {
        String sql = "SELECT COUNT(*) FROM AD_USER_ROLES ur " +
                    "JOIN AD_ROLE_PERMISSIONS rp ON ur.ROLE_ID = rp.ROLE_ID " +
                    "JOIN AD_PERMISSIONS p ON rp.PERMISSION_ID = p.ID " +
                    "WHERE ur.USER_ID = ? AND p.RECURSO = ? AND p.ACAO = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, userId);
            stmt.setString(2, recurso);
            stmt.setString(3, acao);
            
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean hasRole(BigDecimal userId, String roleName) {
        String sql = "SELECT COUNT(*) FROM AD_USER_ROLES ur " +
                    "JOIN AD_ROLES r ON ur.ROLE_ID = r.ID " +
                    "WHERE ur.USER_ID = ? AND r.NOME = ? AND r.ATIVO = 'S'";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, userId);
            stmt.setString(2, roleName);
            
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
```

### **2. Controle de Sessão**

#### **Gerenciamento de Sessão**
```java
@Service
public class SessionManager {
    
    private static final int SESSION_TIMEOUT = 30 * 60; // 30 minutos
    private Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    
    public String criarSessao(BigDecimal userId, String usuario) {
        String sessionId = UUID.randomUUID().toString();
        
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setUserId(userId);
        sessionInfo.setUsuario(usuario);
        sessionInfo.setCreatedAt(System.currentTimeMillis());
        sessionInfo.setLastAccess(System.currentTimeMillis());
        
        activeSessions.put(sessionId, sessionInfo);
        
        // Agendar limpeza da sessão
        scheduleSessionCleanup(sessionId);
        
        return sessionId;
    }
    
    public boolean validarSessao(String sessionId) {
        SessionInfo session = activeSessions.get(sessionId);
        
        if (session == null) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        if ((now - session.getLastAccess()) > (SESSION_TIMEOUT * 1000)) {
            removerSessao(sessionId);
            return false;
        }
        
        session.setLastAccess(now);
        return true;
    }
    
    public void removerSessao(String sessionId) {
        activeSessions.remove(sessionId);
    }
    
    public void renovarSessao(String sessionId) {
        SessionInfo session = activeSessions.get(sessionId);
        if (session != null) {
            session.setLastAccess(System.currentTimeMillis());
        }
    }
    
    private void scheduleSessionCleanup(String sessionId) {
        // Implementar limpeza automática de sessões expiradas
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!validarSessao(sessionId)) {
                    removerSessao(sessionId);
                }
            }
        }, SESSION_TIMEOUT * 1000);
    }
}
```

## 🔗 **Integração com Sistemas Externos**

### **1. SSO (Single Sign-On)**

#### **Integração SAML**
```java
@Component
public class SAMLIntegration {
    
    public String gerarSAMLRequest(String targetUrl) {
        try {
            // Criar requisição SAML
            AuthnRequest authnRequest = new AuthnRequestBuilder()
                    .buildObject();
            
            authnRequest.setID(UUID.randomUUID().toString());
            authnRequest.setIssueInstant(Instant.now());
            authnRequest.setDestination(targetUrl);
            
            // Assinar requisição
            Signature signature = buildSignature();
            authnRequest.setSignature(signature);
            
            // Codificar para base64
            String samlRequest = Base64.getEncoder().encodeToString(
                XMLObjectSupport.marshall(authnRequest).getBytes()
            );
            
            return samlRequest;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean validarSAMLResponse(String samlResponse) {
        try {
            // Decodificar resposta SAML
            byte[] decodedResponse = Base64.getDecoder().decode(samlResponse);
            
            // Parse da resposta
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(decodedResponse));
            
            // Validar assinatura
            if (!validarAssinaturaSAML(document)) {
                return false;
            }
            
            // Extrair informações do usuário
            String usuario = extrairUsuarioSAML(document);
            if (usuario != null) {
                // Criar sessão local
                criarSessaoLocal(usuario);
                return true;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
}
```

### **2. LDAP Integration**

#### **Autenticação LDAP**
```java
@Service
public class LDAPAuthenticationService {
    
    @Value("${ldap.url}")
    private String ldapUrl;
    
    @Value("${ldap.base.dn}")
    private String baseDn;
    
    public boolean autenticarLDAP(String usuario, String senha) {
        DirContext context = null;
        
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUrl);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, "cn=" + usuario + "," + baseDn);
            env.put(Context.SECURITY_CREDENTIALS, senha);
            
            context = new InitialDirContext(env);
            return true;
            
        } catch (AuthenticationException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public UsuarioInfo obterInfoUsuarioLDAP(String usuario) {
        DirContext context = null;
        
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUrl);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, "cn=" + usuario + "," + baseDn);
            
            context = new InitialDirContext(env);
            
            // Buscar informações do usuário
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            
            String filter = "(cn=" + usuario + ")";
            NamingEnumeration<SearchResult> results = context.search(baseDn, filter, searchControls);
            
            if (results.hasMore()) {
                SearchResult result = results.next();
                Attributes attributes = result.getAttributes();
                
                UsuarioInfo usuarioInfo = new UsuarioInfo();
                usuarioInfo.setUsuario(usuario);
                usuarioInfo.setNome(getAttributeValue(attributes, "displayName"));
                usuarioInfo.setEmail(getAttributeValue(attributes, "mail"));
                usuarioInfo.setTelefone(getAttributeValue(attributes, "telephoneNumber"));
                
                return usuarioInfo;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }
    
    private String getAttributeValue(Attributes attributes, String attributeName) {
        try {
            Attribute attribute = attributes.get(attributeName);
            if (attribute != null && attribute.size() > 0) {
                return attribute.get(0).toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
```

## 📊 **Monitoramento e Auditoria**

### **1. Log de Autenticação**
```java
@Component
public class AuthenticationAuditLogger {
    
    public void logarTentativaLogin(String usuario, String ip, boolean sucesso, String detalhes) {
        String sql = "INSERT INTO AD_AUTH_LOG (USUARIO, IP, SUCESSO, DETALHES, DT_LOG) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario);
            stmt.setString(2, ip);
            stmt.setString(3, sucesso ? "S" : "N");
            stmt.setString(4, detalhes);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            
            stmt.executeUpdate();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void logarAcessoRecurso(BigDecimal userId, String recurso, String acao, String ip) {
        String sql = "INSERT INTO AD_ACCESS_LOG (USER_ID, RECURSO, ACAO, IP, DT_ACCESS) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, userId);
            stmt.setString(2, recurso);
            stmt.setString(3, acao);
            stmt.setString(4, ip);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            
            stmt.executeUpdate();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### **2. Alertas de Segurança**
```java
@Service
public class SecurityAlertService {
    
    public void verificarAnomalias(String usuario, String ip) {
        // Verificar login de IPs suspeitos
        if (isIPSuspeito(ip)) {
            enviarAlertaSeguranca("Login de IP suspeito", usuario, ip);
        }
        
        // Verificar múltiplos logins simultâneos
        if (hasMultiplosLoginsSimultaneos(usuario)) {
            enviarAlertaSeguranca("Múltiplos logins simultâneos", usuario, ip);
        }
        
        // Verificar horário incomum
        if (isHorarioIncomum()) {
            enviarAlertaSeguranca("Login em horário incomum", usuario, ip);
        }
    }
    
    private boolean isIPSuspeito(String ip) {
        // Implementar lógica para identificar IPs suspeitos
        // Verificar blacklists, geolocalização, etc.
        return false; // Simplificado
    }
    
    private boolean hasMultiplosLoginsSimultaneos(String usuario) {
        String sql = "SELECT COUNT(*) FROM AD_ACTIVE_SESSIONS WHERE USUARIO = ? AND DT_ULTIMO_ACESSO > ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis() - (5 * 60 * 1000))); // 5 minutos
            
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 3;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean isHorarioIncomum() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        // Considerar incomum se for entre 22h e 6h
        return hour >= 22 || hour <= 6;
    }
    
    private void enviarAlertaSeguranca(String tipo, String usuario, String ip) {
        // Implementar envio de alertas (email, SMS, etc.)
        System.out.println("ALERTA DE SEGURANÇA: " + tipo + " - Usuário: " + usuario + " - IP: " + ip);
    }
}
```

## 🎯 **Boas Práticas**

### **1. Segurança**
- **Senhas Fortes**: Implementar políticas de senha
- **Criptografia**: Sempre criptografar dados sensíveis
- **HTTPS**: Usar sempre HTTPS em produção
- **Rate Limiting**: Implementar limite de tentativas
- **Auditoria**: Logar todas as tentativas de acesso

### **2. Performance**
- **Cache de Sessões**: Cachear informações de sessão
- **Tokens JWT**: Usar tokens stateless quando possível
- **Connection Pooling**: Usar pool de conexões
- **Índices**: Criar índices para consultas de autenticação

### **3. Manutenibilidade**
- **Configuração Externa**: Configurar parâmetros externamente
- **Logging Estruturado**: Usar logs estruturados
- **Testes**: Implementar testes de autenticação
- **Documentação**: Documentar APIs de autenticação

## 🎊 **Conclusão**

A autenticação no Sankhya deve ser:

- **✅ Segura**: Múltiplas camadas de segurança
- **✅ Flexível**: Suporte a diferentes métodos
- **✅ Auditável**: Logs completos de acesso
- **✅ Performática**: Otimizada para alta demanda
- **✅ Integrável**: Compatível com sistemas externos

### **Implementações Recomendadas:**
1. **JWT** para APIs REST
2. **OAuth 2.0** para integração externa
3. **LDAP/SAML** para SSO empresarial
4. **RBAC** para controle de acesso granular
5. **Auditoria** para compliance e segurança

---

*Este documento fornece um guia completo para implementação de autenticação segura e robusta no Sankhya.*
