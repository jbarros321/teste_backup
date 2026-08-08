# 🗄️ Jape - Persistência de Dados Sankhya

## 🎯 Visão Geral

O **Jape** é o framework de persistência de dados da plataforma Sankhya, responsável por gerenciar a comunicação entre a aplicação e o banco de dados Oracle. É uma camada de abstração que simplifica operações de CRUD (Create, Read, Update, Delete) e oferece recursos avançados de mapeamento objeto-relacional.

## 🏗️ Arquitetura do Jape

### **Componentes Principais**
- **EntityManager**: Gerenciador de entidades
- **QueryBuilder**: Construtor de consultas
- **TransactionManager**: Gerenciador de transações
- **CacheManager**: Gerenciador de cache
- **ConnectionPool**: Pool de conexões

### **Camadas de Abstração**
```
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA DE APLICAÇÃO                     │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Controllers   │ │    Services     │ │   Repositories  │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA JAPE                              │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │ EntityManager   │ │  QueryBuilder   │ │TransactionMgr   │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA DE DADOS                         │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Oracle DB     │ │   Connection    │ │     Cache       │ │
│  │                 │ │     Pool        │ │                 │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🔧 Funcionalidades Principais

### **1. EntityManager**
- Gerenciamento de entidades
- Operações CRUD
- Controle de ciclo de vida
- Persistência automática
- Detecção de mudanças

### **2. QueryBuilder**
- Construção de consultas SQL
- Consultas tipadas
- Consultas nativas
- Agregações
- Joins complexos

### **3. TransactionManager**
- Controle de transações
- Rollback automático
- Isolamento de transações
- Transações aninhadas
- Gerenciamento de locks

### **4. CacheManager**
- Cache de primeiro nível
- Cache de segundo nível
- Cache de consultas
- Invalidação inteligente
- Performance otimizada

## 📊 Operações CRUD

### **Create (Inserir)**
```java
// Exemplo de inserção
EntityManager em = JapeSession.getEntityManager();
TGFCAB cab = new TGFCAB();
cab.setNunota(12345);
cab.setNumnota("000001");
cab.setDtmov(new Date());
em.persist(cab);
em.commit();
```

### **Read (Consultar)**
```java
// Exemplo de consulta
EntityManager em = JapeSession.getEntityManager();
QueryBuilder qb = em.getQueryBuilder();
List<TGFCAB> cabs = qb.select(TGFCAB.class)
    .where("dtmov >= ?", dataInicio)
    .and("dtmov <= ?", dataFim)
    .and("statusnota = ?", "L")
    .getResultList();
```

### **Update (Atualizar)**
```java
// Exemplo de atualização
EntityManager em = JapeSession.getEntityManager();
TGFCAB cab = em.find(TGFCAB.class, nunota);
if (cab != null) {
    cab.setStatusnota("A");
    em.merge(cab);
    em.commit();
}
```

### **Delete (Excluir)**
```java
// Exemplo de exclusão
EntityManager em = JapeSession.getEntityManager();
TGFCAB cab = em.find(TGFCAB.class, nunota);
if (cab != null) {
    em.remove(cab);
    em.commit();
}
```

## 🔍 QueryBuilder Avançado

### **Consultas Simples**
```java
QueryBuilder qb = em.getQueryBuilder();
List<TGFCAB> cabs = qb.select(TGFCAB.class)
    .where("codemp = ?", codEmpresa)
    .orderBy("dtmov DESC")
    .getResultList();
```

### **Consultas com Joins**
```java
List<Object[]> results = qb.select("c.nunota, c.numnota, p.razaosocial")
    .from(TGFCAB.class, "c")
    .join(TGFPAR.class, "p", "c.codparc = p.codparc")
    .where("c.dtmov >= ?", dataInicio)
    .getResultList();
```

### **Consultas com Agregações**
```java
Object result = qb.select("SUM(c.vlrnota), COUNT(c.nunota)")
    .from(TGFCAB.class, "c")
    .where("c.dtmov >= ?", dataInicio)
    .and("c.dtmov <= ?", dataFim)
    .getSingleResult();
```

### **Consultas Nativas**
```java
List<Object[]> results = qb.nativeQuery(
    "SELECT c.nunota, c.numnota, p.razaosocial " +
    "FROM tgfcab c " +
    "INNER JOIN tgfpar p ON c.codparc = p.codparc " +
    "WHERE c.dtmov >= ? AND c.dtmov <= ?",
    dataInicio, dataFim
).getResultList();
```

## 🔄 Gerenciamento de Transações

### **Transações Automáticas**
```java
@EntityManager
public class MeuService {
    
    public void processarPedido(Integer nunota) {
        EntityManager em = JapeSession.getEntityManager();
        
        try {
            // Operações dentro da transação
            TGFCAB cab = em.find(TGFCAB.class, nunota);
            cab.setStatusnota("A");
            
            // Commit automático ao final do método
        } catch (Exception e) {
            // Rollback automático em caso de erro
            throw e;
        }
    }
}
```

### **Transações Manuais**
```java
public void processarLote(List<Integer> nunotas) {
    EntityManager em = JapeSession.getEntityManager();
    
    try {
        em.beginTransaction();
        
        for (Integer nunota : nunotas) {
            TGFCAB cab = em.find(TGFCAB.class, nunota);
            cab.setStatusnota("A");
            em.merge(cab);
        }
        
        em.commit();
    } catch (Exception e) {
        em.rollback();
        throw e;
    }
}
```

### **Transações Aninhadas**
```java
@Transactional
public void processarComTransacaoAninhada() {
    EntityManager em = JapeSession.getEntityManager();
    
    // Transação principal
    processarCabecalho();
    
    // Transação aninhada
    em.beginTransaction();
    try {
        processarItens();
        em.commit();
    } catch (Exception e) {
        em.rollback();
        throw e;
    }
    
    // Continua transação principal
    processarFinanceiro();
}
```

## 💾 Cache e Performance

### **Cache de Primeiro Nível**
```java
EntityManager em = JapeSession.getEntityManager();

// Primeira consulta - vai ao banco
TGFCAB cab1 = em.find(TGFCAB.class, 12345);

// Segunda consulta - vem do cache
TGFCAB cab2 = em.find(TGFCAB.class, 12345);
```

### **Cache de Consultas**
```java
QueryBuilder qb = em.getQueryBuilder();
List<TGFCAB> cabs = qb.select(TGFCAB.class)
    .where("statusnota = ?", "L")
    .cacheable(true) // Habilita cache da consulta
    .getResultList();
```

### **Otimização de Consultas**
```java
// Consulta otimizada com fetch join
List<TGFCAB> cabs = qb.select(TGFCAB.class)
    .fetch("itens") // Carrega itens junto
    .where("dtmov >= ?", dataInicio)
    .getResultList();
```

## 🔗 Relacionamentos

### **Relacionamento 1:N**
```java
// Entidade TGFCAB
@Entity
public class TGFCAB {
    @OneToMany(mappedBy = "nunota", cascade = CascadeType.ALL)
    private List<TGFITE> itens;
    
    // getters e setters
}

// Entidade TGFITE
@Entity
public class TGFITE {
    @ManyToOne
    @JoinColumn(name = "nunota")
    private TGFCAB cabecalho;
    
    // getters e setters
}
```

### **Relacionamento N:1**
```java
// Entidade TGFITE
@Entity
public class TGFITE {
    @ManyToOne
    @JoinColumn(name = "codprod")
    private TGFPRO produto;
    
    // getters e setters
}
```

### **Relacionamento N:N**
```java
// Entidade com relacionamento N:N
@Entity
public class MinhaEntidade {
    @ManyToMany
    @JoinTable(
        name = "TABELA_INTERMEDIARIA",
        joinColumns = @JoinColumn(name = "ID_ENTIDADE1"),
        inverseJoinColumns = @JoinColumn(name = "ID_ENTIDADE2")
    )
    private List<OutraEntidade> relacionamentos;
}
```

## 🎯 Casos de Uso Avançados

### **1. Consultas Dinâmicas**
```java
public List<TGFCAB> buscarComFiltros(Map<String, Object> filtros) {
    QueryBuilder qb = em.getQueryBuilder();
    qb.select(TGFCAB.class);
    
    if (filtros.containsKey("dataInicio")) {
        qb.where("dtmov >= ?", filtros.get("dataInicio"));
    }
    
    if (filtros.containsKey("dataFim")) {
        qb.and("dtmov <= ?", filtros.get("dataFim"));
    }
    
    if (filtros.containsKey("status")) {
        qb.and("statusnota = ?", filtros.get("status"));
    }
    
    return qb.getResultList();
}
```

### **2. Operações em Lote**
```java
public void atualizarStatusLote(List<Integer> nunotas, String status) {
    EntityManager em = JapeSession.getEntityManager();
    
    try {
        em.beginTransaction();
        
        for (Integer nunota : nunotas) {
            TGFCAB cab = em.find(TGFCAB.class, nunota);
            if (cab != null) {
                cab.setStatusnota(status);
                em.merge(cab);
            }
        }
        
        em.commit();
    } catch (Exception e) {
        em.rollback();
        throw e;
    }
}
```

### **3. Consultas com Paginação**
```java
public List<TGFCAB> buscarComPaginação(int pagina, int tamanho) {
    QueryBuilder qb = em.getQueryBuilder();
    return qb.select(TGFCAB.class)
        .orderBy("dtmov DESC")
        .setFirstResult(pagina * tamanho)
        .setMaxResults(tamanho)
        .getResultList();
}
```

### **4. Consultas com Subconsultas**
```java
public List<TGFCAB> buscarComSubconsulta() {
    QueryBuilder qb = em.getQueryBuilder();
    return qb.select(TGFCAB.class)
        .where("nunota IN (SELECT nunota FROM tgfite WHERE codprod = ?)", codProduto)
        .getResultList();
}
```

## 🔒 Segurança e Controle de Acesso

### **Controle de Permissões**
```java
@EntityManager
public class ServicoSeguro {
    
    public void operacaoRestrita(Integer nunota) {
        // Verificar permissões antes da operação
        if (!temPermissao("ALTERAR_NOTA")) {
            throw new SecurityException("Sem permissão para alterar nota");
        }
        
        EntityManager em = JapeSession.getEntityManager();
        TGFCAB cab = em.find(TGFCAB.class, nunota);
        // ... operações
    }
}
```

### **Auditoria Automática**
```java
@Entity
@Audited
public class TGFCAB {
    @Id
    private Integer nunota;
    
    @AuditField
    private String statusnota;
    
    @AuditField
    private BigDecimal vlrnota;
    
    // outros campos
}
```

## 📈 Performance e Otimização

### **1. Lazy Loading**
```java
@Entity
public class TGFCAB {
    @OneToMany(mappedBy = "nunota", fetch = FetchType.LAZY)
    private List<TGFITE> itens;
}
```

### **2. Eager Loading**
```java
@Entity
public class TGFCAB {
    @OneToMany(mappedBy = "nunota", fetch = FetchType.EAGER)
    private List<TGFITE> itens;
}
```

### **3. Consultas Otimizadas**
```java
// Evitar N+1 queries
List<TGFCAB> cabs = qb.select(TGFCAB.class)
    .fetch("itens") // Carrega itens junto
    .where("dtmov >= ?", dataInicio)
    .getResultList();
```

### **4. Índices Estratégicos**
```java
@Entity
@Index(name = "IDX_TGFCAB_DTMOV", columns = {"dtmov"})
@Index(name = "IDX_TGFCAB_STATUS", columns = {"statusnota"})
public class TGFCAB {
    // campos
}
```

## 🛠️ Boas Práticas

### **1. Gerenciamento de Conexões**
- Sempre fechar EntityManager
- Usar try-with-resources
- Evitar vazamentos de conexão
- Monitorar pool de conexões

### **2. Transações**
- Manter transações curtas
- Evitar transações longas
- Usar rollback em caso de erro
- Considerar transações aninhadas

### **3. Performance**
- Usar índices adequados
- Evitar N+1 queries
- Usar cache quando apropriado
- Monitorar performance

### **4. Segurança**
- Validar entrada de dados
- Usar prepared statements
- Controlar permissões
- Auditar operações

## 🔍 Troubleshooting

### **Problemas Comuns**
- Vazamentos de conexão
- Transações longas
- Queries lentas
- Problemas de cache
- Erros de mapeamento

### **Soluções**
- Análise de logs
- Monitoramento de performance
- Revisão de queries
- Otimização de índices
- Ajuste de cache

## 🚀 Evolução e Tendências

### **Melhorias Contínuas**
- Novos recursos de cache
- Otimizações de performance
- Melhor suporte a transações
- Integração com cloud
- Ferramentas de monitoramento

### **Tendências Futuras**
- Microserviços
- Containers
- Cloud native
- Inteligência artificial
- Automação

---

*Este documento foi criado com base na documentação oficial do Sankhya Developer sobre o Jape (Persistência de dados).*
