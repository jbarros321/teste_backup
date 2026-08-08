# 📚 Documentação Técnica - Template para Personalizações Sankhya

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Arquitetura do Projeto](#arquitetura-do-projeto)
3. [Componentes Padrão](#componentes-padrão)
4. [Padrões de Código](#padrões-de-código)
5. [Padrões Sankhya](#padrões-sankhya)
6. [Estrutura de Diretórios](#estrutura-de-diretórios)
7. [Como Usar o Template](#como-usar-o-template)
8. [Exemplos Práticos](#exemplos-práticos)
9. [Melhores Práticas](#melhores-práticas)
10. [Troubleshooting](#troubleshooting)

---

## 🎯 Visão Geral

### Informações do Projeto

- **Projeto**: Template para Personalizações Sankhya
- **Versão**: 6.0.0
- **Status**: ✅ **TEMPLATE CONSOLIDADO**
- **Baseado em**: Conhecimento consolidado de TODOS os projetos do repositório

### Objetivo

Fornecer uma estrutura base completa e otimizada para criação de novas personalizações Sankhya, seguindo as melhores práticas de desenvolvimento Java JDK8, arquitetura SOLID e código limpo.

### Especificações Técnicas

- **Java**: JDK 8
- **Build**: Maven 3.x
- **Arquitetura**: SOLID, código limpo
- **Otimizações**: Buffers, streams, cache, pré-alocação
- **Padrões**: Sem comentários, métodos simples e práticos

### Conhecimento Consolidado

Este Template incorpora **TODO O CONHECIMENTO APRENDIDO** de todos os projetos:

- ✅ **6 projetos reais** analisados em profundidade
- ✅ **48 implementações** de `AcaoRotinaJava` analisadas
- ✅ **7 implementações** de `ScheduledAction` analisadas
- ✅ **21 implementações** de `EventoProgramavelJava` analisadas
- ✅ **12 implementações** de `Repository` analisadas
- ✅ **20+ tabelas Sankhya** documentadas completamente
- ✅ **8 padrões avançados** consolidados de código real
- ✅ **8 casos de uso comuns** com soluções prontas e testadas

---

## 🏗️ Arquitetura do Projeto

### Padrão Arquitetural

O template segue o padrão **MVC (Model-View-Controller)** adaptado para integração Sankhya:

- **Model**: DTOs (`ExemploDTO`)
- **Repository**: Camada de acesso a dados (queries SQL)
- **Service**: Lógica de negócio e orquestração
- **Action**: Camada de apresentação (botão de ação Sankhya)
- **Util**: Utilitários e constantes

### Stack Tecnológico

- **Java JDK 8**
- **Maven 3.x**
- **Sankhya Core** (via JARs locais)
  - `SankhyaW-extensions.jar`
  - `jape.jar`
  - `mge-modelcore.jar`
  - `sanutil.jar`
  - `sanws.jar`
- **Oracle Database** (via JDBC do Sankhya)

### Princípios de Design

1. **SOLID**: Separação de responsabilidades, inversão de dependências
2. **DRY**: Don't Repeat Yourself - código reutilizável
3. **KISS**: Keep It Simple, Stupid - soluções simples e diretas
4. **YAGNI**: You Aren't Gonna Need It - não adicionar complexidade desnecessária

---

## 🔧 Componentes Padrão

### Arquitetura com Interfaces

O Template segue o princípio de **Dependency Inversion** (SOLID), utilizando interfaces sempre que possível:

- ✅ **Repository** - Interface para repositórios de dados
- ✅ **Service** - Interface para serviços de negócio
- ✅ **Interfaces Funcionais** - ResultSetMapper, QueryExecutor, ResultSetExtractor, SqlConfigurator

**Benefícios**:
- Desacoplamento entre camadas
- Facilita testes unitários (mocks)
- Permite múltiplas implementações
- Segue princípios SOLID

### 1. Repository (Interface)

**Localização**: `src/br/com/cliente/repository/Repository.java`

**Responsabilidade**: Define contrato para acesso a dados

**Métodos da Interface**:

```java
public interface Repository {
    <T> Set<T> executarQuery(String sql, BigDecimal nunota, ResultSetMapper<T> mapper) throws Exception;
    
    <T> Set<T> executarQueryComParametros(String sql, ResultSetMapper<T> mapper, SqlConfigurator configurador) throws Exception;
    
    <T> T executarQueryCustomizada(QueryExecutor<T> executor) throws Exception;
    
    <T> T executarQueryUnica(String sql, SqlConfigurator configurador, ResultSetExtractor<T> extractor) throws Exception;
    
    BigDecimal executarQueryValorUnico(String sql, SqlConfigurator configurador) throws Exception;
    
    String executarQueryStringUnica(String sql, SqlConfigurator configurador) throws Exception;
    
    Timestamp executarQueryTimestampUnico(String sql, SqlConfigurator configurador) throws Exception;
}
```

### 2. AbstractRepository (Implementação Base)

**Localização**: `src/br/com/cliente/repository/AbstractRepository.java`

**Responsabilidade**: Classe base abstrata para repositórios com lógica comum de acesso a dados

**Funcionalidades**:
- Gerenciamento automático de conexões JDBC através de `JdbcWrapper`
- Fechamento seguro de recursos em blocos `finally`
- Métodos genéricos com mapeamento funcional usando interfaces funcionais
- Tratamento de exceções padronizado
- Suporte a parâmetros nomeados em queries SQL
- Conversão automática de tipos (Timestamp para Date, formatação de CNPJ)

**Código Fonte Completo**:

```java
package br.com.cliente.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

abstract class AbstractRepository implements Repository {
    
    @Override
    public <T> Set<T> executarQuery(String sql, BigDecimal nunota, ResultSetMapper<T> mapper) throws Exception {
        if (nunota == null) throw new IllegalArgumentException("NUNOTA não pode ser nulo.");
        return executarQueryComParametros(sql, mapper, s -> {
            s.appendSql(" AND CAB.NUNOTA = :NUNOTA");
            s.setNamedParameter("NUNOTA", nunota);
        });
    }
    
    protected <T> T executarQueryCustomizada(QueryExecutor<T> executor) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sqlNative = null;
        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            sqlNative = new NativeSql(jdbc);
            return executor.executar(sqlNative);
        } finally {
            fecharRecurso(sqlNative, NativeSql::releaseResources);
            fecharRecurso(jdbc, j -> JdbcWrapper.closeSession(j));
        }
    }
    
    protected <T> Set<T> executarQueryComParametros(String sql, ResultSetMapper<T> mapper, SqlConfigurator configurador) throws Exception {
        Set<T> conjunto = new LinkedHashSet<>(1024);
        JdbcWrapper jdbc = null;
        NativeSql sqlNative = null;
        ResultSet rs = null;
        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            sqlNative = new NativeSql(jdbc);
            sqlNative.appendSql(sql);
            if (configurador != null) configurador.configurar(sqlNative);
            rs = sqlNative.executeQuery();
            while (rs.next()) conjunto.add(mapper.map(rs));
        } finally {
            Optional.ofNullable(rs).ifPresent(r -> { try { r.close(); } catch (Exception ignored) {} });
            fecharRecurso(sqlNative, NativeSql::releaseResources);
            fecharRecurso(jdbc, j -> JdbcWrapper.closeSession(j));
        }
        return conjunto;
    }
    
    @Override
    public <T> T executarQueryUnica(String sql, SqlConfigurator configurador, ResultSetExtractor<T> extractor) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sqlNative = null;
        ResultSet rs = null;
        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            sqlNative = new NativeSql(jdbc);
            sqlNative.appendSql(sql);
            if (configurador != null) configurador.configurar(sqlNative);
            rs = sqlNative.executeQuery();
            return rs.next() ? extractor.extract(rs) : null;
        } finally {
            Optional.ofNullable(rs).ifPresent(r -> { try { r.close(); } catch (Exception ignored) {} });
            fecharRecurso(sqlNative, NativeSql::releaseResources);
            fecharRecurso(jdbc, j -> JdbcWrapper.closeSession(j));
        }
    }
    
    @Override
    public BigDecimal executarQueryValorUnico(String sql, SqlConfigurator configurador) throws Exception {
        return executarQueryUnica(sql, configurador, rs -> rs.getBigDecimal(1));
    }
    
    @Override
    public String executarQueryStringUnica(String sql, SqlConfigurator configurador) throws Exception {
        return executarQueryUnica(sql, configurador, rs -> rs.getString(1));
    }
    
    @Override
    public Timestamp executarQueryTimestampUnico(String sql, SqlConfigurator configurador) throws Exception {
        return executarQueryUnica(sql, configurador, rs -> rs.getTimestamp(1));
    }
    
    private static <T> void fecharRecurso(T recurso, Consumer<T> closer) {
        try { Optional.ofNullable(recurso).ifPresent(closer); } catch (Exception ignored) {}
    }
    
    protected static Date toDate(Timestamp ts) {
        return Optional.ofNullable(ts).map(t -> new Date(t.getTime())).orElse(null);
    }
    
    protected static String formatarCnpj(String cnpj) {
        return Optional.ofNullable(cnpj).map(c -> c.replaceAll("[^0-9]", "")).orElse("");
    }
    
    protected static String formatarCnpjCompleto(String cnpj) {
        return Optional.ofNullable(cnpj).map(c -> c.replace(".", "").replace("/", "").replace("-", "").replace(" ", "")).orElse("");
    }
}
```

**Métodos da Interface Repository**:

| Método | Descrição | Parâmetros | Retorno |
|--------|-----------|------------|---------|
| `executarQuery(String, BigDecimal, ResultSetMapper)` | Executa query filtrando por NUNOTA | SQL base, NUNOTA, mapper | `Set<T>` |
| `executarQueryComParametros(String, ResultSetMapper, SqlConfigurator)` | Executa query com parâmetros customizados | SQL base, mapper, configurador | `Set<T>` |
| `executarQueryCustomizada(QueryExecutor)` | Executa query totalmente customizada | Executor | `T` |
| `executarQueryUnica(String, SqlConfigurator, ResultSetExtractor)` | Executa query retornando único resultado | SQL, configurador, extractor | `T` |
| `executarQueryValorUnico(String, SqlConfigurator)` | Executa query retornando BigDecimal único | SQL, configurador | `BigDecimal` |
| `executarQueryStringUnica(String, SqlConfigurator)` | Executa query retornando String única | SQL, configurador | `String` |
| `executarQueryTimestampUnico(String, SqlConfigurator)` | Executa query retornando Timestamp único | SQL, configurador | `Timestamp` |

**Métodos Utilitários Protegidos** (herdados por subclasses):

| Método | Descrição | Parâmetros | Retorno |
|--------|-----------|------------|---------|
| `toDate(Timestamp)` | Converte Timestamp para Date | Timestamp | `Date` |
| `formatarCnpj(String)` | Remove formatação do CNPJ | CNPJ formatado | CNPJ apenas números |
| `formatarCnpjCompleto(String)` | Remove toda formatação do CNPJ | CNPJ formatado | CNPJ limpo |

**Características de Performance**:
- ✅ Pré-alocação de `LinkedHashSet` com capacidade inicial de 1024 elementos
- ✅ Uso de `Optional` para tratamento seguro de nulos
- ✅ Fechamento automático de recursos em blocos `finally`
- ✅ Method references para fechamento de recursos (`NativeSql::releaseResources`)
- ✅ Uso de interfaces funcionais para flexibilidade e concisão

**Exemplo de Uso Completo**:

```java
public class MeuRepository extends AbstractRepository {
    
    private static final String SQL_BASE = 
        "SELECT c.NUNOTA, c.NUMNOTA, c.DTNEG, p.DESCRPROD " +
        "FROM TGFCAB c " +
        "INNER JOIN TGFITE i ON i.NUNOTA = c.NUNOTA " +
        "INNER JOIN TGFPRO p ON p.CODPROD = i.CODPROD AND p.ATIVO = 'S' " +
        "WHERE c.STATUSNOTA = 'L'";
    
    public Set<NotaDTO> buscarNotasPorNunota(BigDecimal nunota) throws Exception {
        return executarQuery(SQL_BASE, nunota, this::mapearNotaDTO);
    }
    
    public Set<NotaDTO> buscarNotasPorPeriodo(Timestamp inicio, Timestamp fim) throws Exception {
        return executarQueryComParametros(SQL_BASE, this::mapearNotaDTO, sql -> {
            Optional.ofNullable(inicio).ifPresent(i -> {
                sql.appendSql(" AND c.DTNEG >= :INICIO");
                sql.setNamedParameter("INICIO", i);
            });
            Optional.ofNullable(fim).ifPresent(f -> {
                sql.appendSql(" AND c.DTNEG <= :FIM");
                sql.setNamedParameter("FIM", f);
            });
        });
    }
    
    public BigDecimal contarNotasPorEmpresa(Integer codEmp) throws Exception {
        return executarQueryValorUnico(
            "SELECT COUNT(*) FROM TGFCAB WHERE STATUSNOTA = 'L'",
            sql -> {
                sql.appendSql(" AND CODEMP = :CODEMP");
                sql.setNamedParameter("CODEMP", codEmp);
            }
        );
    }
    
    private NotaDTO mapearNotaDTO(ResultSet rs) throws Exception {
        NotaDTO dto = new NotaDTO();
        dto.setNunota(rs.getBigDecimal("NUNOTA"));
        dto.setNumNota(rs.getInt("NUMNOTA"));
        dto.setDataNegociacao(toDate(rs.getTimestamp("DTNEG")));
        dto.setDescricaoProduto(rs.getString("DESCRPROD"));
        return dto;
    }
}
```

**Fluxo de Execução**:

1. **Abertura de Conexão**: `EntityFacadeFactory.getDWFFacade().getJdbcWrapper()` obtém wrapper JDBC
2. **Abertura de Sessão**: `jdbc.openSession()` abre sessão JDBC
3. **Criação de NativeSql**: `new NativeSql(jdbc)` cria objeto SQL nativo
4. **Configuração de SQL**: `appendSql()` e `setNamedParameter()` configuram query
5. **Execução**: `executeQuery()` executa query e retorna ResultSet
6. **Mapeamento**: `ResultSetMapper.map()` converte cada linha em DTO
7. **Fechamento**: Recursos são fechados automaticamente em `finally`

### 3. Interfaces Funcionais

**Localização**: `src/br/com/cliente/repository/`

**Interfaces Funcionais Disponíveis**:

#### ResultSetMapper<T>
```java
@FunctionalInterface
public interface ResultSetMapper<T> {
    T map(ResultSet rs) throws Exception;
}
```
**Uso**: Mapeia uma linha do ResultSet para um objeto DTO

#### QueryExecutor<T>
```java
@FunctionalInterface
public interface QueryExecutor<T> {
    T executar(NativeSql sqlNative) throws Exception;
}
```
**Uso**: Executa query totalmente customizada

#### ResultSetExtractor<T>
```java
@FunctionalInterface
public interface ResultSetExtractor<T> {
    T extract(ResultSet rs) throws Exception;
}
```
**Uso**: Extrai valor único do ResultSet

#### SqlConfigurator
```java
@FunctionalInterface
public interface SqlConfigurator {
    void configurar(NativeSql sql) throws Exception;
}
```
**Uso**: Configura parâmetros da query SQL

### 4. Service (Interface)

**Localização**: `src/br/com/cliente/service/Service.java`

**Responsabilidade**: Define contrato para serviços de negócio

**Métodos da Interface**:

```java
public interface Service {
    String gerarArquivo(String caminhoExportacao, BigDecimal nunota) throws Exception;
}
```

**Exemplo de Implementação**:

```java
public class ExemploService implements Service {
    private final ExemploRepository repository = new ExemploRepository();
    
    @Override
    public String gerarArquivo(String caminhoExportacao, BigDecimal nunota) throws Exception {
        // implementação
    }
}
```

### 5. DownloadHelper

**Localização**: `src/br/com/cliente/util/DownloadHelper.java`

**Responsabilidade**: Utilitário padronizado para download de arquivos e criação de ZIPs

**Métodos principais**:
- `prepararDownload(String caminhoArquivo)`: Prepara download de arquivo único
- `criarZip(Collection<String> arquivos, String nomeZip)`: Cria ZIP com múltiplos arquivos
- `gerarScriptDownloadZip(String nomeArquivo)`: Gera script HTML/JS para download automático

**Exemplo de Uso**:

```java
List<String> arquivos = Arrays.asList("arquivo1.txt", "arquivo2.txt");
String zipPath = DownloadHelper.criarZip(arquivos, "meu_zip.zip");
DownloadHelper.prepararDownload(zipPath);
```

### 6. FileGenerator

**Localização**: `src/br/com/cliente/util/FileGenerator.java`

**Responsabilidade**: Utilitário para geração de arquivos padronizados

**Funcionalidades**:
- Encoding Windows-1252 (ANSI)
- Quebra de linha CRLF
- Geração de nomes de arquivo padronizados
- Buffer otimizado (8192 bytes)

**Exemplo de Uso**:

```java
FileGenerator generator = new FileGenerator("meu_arquivo.txt");
generator.escreverLinha("Linha 1");
generator.escreverLinha("Linha 2");
generator.fechar();
```

### 7. Formatter

**Localização**: `src/br/com/cliente/util/Formatter.java`

**Responsabilidade**: Formatadores otimizados com cache

**Funcionalidades**:
- Formatação de CNPJ, datas, números
- Cache de espaços em branco
- Operações com arrays de char para performance

**Exemplo de Uso**:

```java
String cnpj = Formatter.formatarCNPJ("12345678000190");
String data = Formatter.formatarData(new Date());
String numero = Formatter.formatarNumero(123.45, 2);
```

---

## 💻 Padrões de Código

### JDK8 Máximo

**SEMPRE usar**:
- ✅ Streams para processamento de coleções
- ✅ Optional para tratamento de valores nulos
- ✅ Lambdas para funções anônimas
- ✅ Method references quando possível

**Exemplo**:

```java
// ✅ CORRETO
Set<String> resultado = dados.stream()
    .map(MeuDTO::getCampo)
    .filter(Objects::nonNull)
    .filter(s -> !s.isEmpty())
    .map(String::toUpperCase)
    .collect(Collectors.toCollection(() -> new LinkedHashSet<>(1024)));

// ❌ ERRADO
Set<String> resultado = new LinkedHashSet<>();
for (MeuDTO dto : dados) {
    if (dto.getCampo() != null && !dto.getCampo().isEmpty()) {
        resultado.add(dto.getCampo().toUpperCase());
    }
}
```

### Validação Defensiva

**SEMPRE validar** parâmetros no início dos métodos públicos:

```java
public void processar(Integer codigo, String nome) {
    if (codigo == null || codigo <= 0) {
        throw new IllegalArgumentException("Código inválido");
    }
    if (nome == null || nome.trim().isEmpty()) {
        throw new IllegalArgumentException("Nome inválido");
    }
    // ... resto do código
}
```

### Tratamento de Erros

**SEMPRE usar** ExceptionUtils para stack traces completos:

```java
try {
    // código
} catch (Exception e) {
    throw new RuntimeException("Erro ao processar: " + ExceptionUtils.getStackTrace(e), e);
}
```

### Performance

**SEMPRE otimizar**:
- Buffers I/O: 8192 bytes
- Pré-alocação: `new LinkedHashSet<>(1024)`, `new ArrayList<>(100)`
- StringBuilder: `new StringBuilder(200)`
- Cache: `ConcurrentHashMap` para objetos imutáveis

---

## 🎯 Padrões Sankhya

### EntityFacade

**SEMPRE usar** EntityFacade para acesso principal a entidades:

```java
EntityFacade facade = EntityFacade.getInstance();
DynamicObject produto = facade.findEntity("Produto", codigo);
```

### NativeSql

**SEMPRE usar** NativeSql com `setNamedParameter` para queries complexas:

```java
NativeSql sql = new NativeSql("SELECT campo FROM tabela WHERE codigo = :codigo");
sql.setNamedParameter("codigo", codigo);
sql.setMaxResults(100);
```

### JapeWrapper

**SEMPRE usar** JapeWrapper para tabelas AD_ (campos adicionais):

```java
JapeWrapper wrapper = JapeWrapper.create("AD_MINHATABELA");
wrapper.set("CAMPO", valor);
wrapper.save();
```

### Filtros Obrigatórios

**SEMPRE filtrar**:
- `STATUSNOTA = 'L'` em queries de notas
- `ATIVO = 'S'` em queries de produtos
- `CODTIPOPER + DHTIPOPER` em JOINs com tipo de operação

**Exemplo**:

```sql
SELECT * FROM TGFCAB 
WHERE STATUSNOTA = 'L' 
  AND CODTIPOPER = :codTipOper 
  AND DHTIPOPER = :dhTipOper
```

---

## 📁 Estrutura de Diretórios

### Estrutura Completa

```
Template/
├── docs/                              # Documentação técnica
│   └── DOCUMENTACAO_TECNICA.md       # Este arquivo
├── README.md                          # Visão geral do projeto
├── CONHECIMENTO_CONSOLIDADO.md        # Consolidação máxima de conhecimento
├── INSTRUCOES_DESENVOLVIMENTO.md      # Tudo sobre desenvolvimento
├── REFERENCIA_SANKHYA.md              # Referência Sankhya
├── CHANGELOG.md                       # Histórico de mudanças
├── pom.xml                            # Configuração Maven
├── lib/                               # JARs do Sankhya (não versionados)
│   └── README.md                      # Instruções sobre JARs
└── src/
    └── br/com/cliente/
        ├── action/
        │   └── botaoAcao/
        │       └── PersonalizacaoSankhya.java
        ├── model/
        │   └── dto/
        │       └── ExemploDTO.java
        ├── repository/
        │   ├── Repository.java                    # ✅ Interface
        │   ├── AbstractRepository.java           # ✅ Implementação base
        │   ├── ExemploRepository.java            # ✅ Implementação concreta
        │   ├── ResultSetMapper.java              # ✅ Interface funcional
        │   ├── QueryExecutor.java                # ✅ Interface funcional
        │   ├── ResultSetExtractor.java           # ✅ Interface funcional
        │   └── SqlConfigurator.java              # ✅ Interface funcional
        ├── service/
        │   ├── Service.java                      # ✅ Interface
        │   └── ExemploService.java               # ✅ Implementação concreta
        └── util/
            ├── DownloadHelper.java
            ├── FileGenerator.java
            ├── FileGeneratorInterface.java       # Interface (referência)
            ├── Formatter.java
            └── FormatterInterface.java           # Interface (referência)
```

### Convenções de Nomenclatura

- **Classes**: PascalCase (`MeuRepository.java`)
- **Métodos**: camelCase (`buscarDados()`)
- **Constantes**: UPPER_SNAKE_CASE (`MAX_TAMANHO`)
- **Pacotes**: lowercase (`br.com.cliente.repository`)

---

## 🚀 Como Usar o Template

### 1. Copiar o Template

```bash
cp -r Template NovoProjeto
cd NovoProjeto
```

### 2. Configurar o Projeto

#### Atualizar pom.xml

```xml
<groupId>br.com.novoprojeto</groupId>
<artifactId>novo-projeto</artifactId>
<name>Novo Projeto</name>
<description>Descrição do novo projeto</description>
```

#### Renomear Pacote

1. Renomear diretório `br/com/cliente` para `br/com/novoprojeto`
2. Atualizar `package` em todos os arquivos Java
3. Atualizar imports

#### Configurar JARs Sankhya

Copiar JARs necessários para `lib/`:
- `SankhyaW-extensions.jar`
- `jape.jar`
- `mge-modelcore.jar`
- `sanutil.jar`
- `sanws.jar`

### 3. Implementar a Lógica

#### Criar DTOs

```java
public class MeuDTO {
    private String campo1;
    private Integer campo2;
    
    // Construtor, getters, setters, equals, hashCode
}
```

#### Criar Repository

```java
public class MeuRepository extends AbstractRepository implements Repository {
    
    public Set<MeuDTO> buscarDados(BigDecimal nunota) throws Exception {
        String sql = "SELECT campo1, campo2 FROM tabela WHERE codigo = :codigo";
        return executarQuery(sql, nunota, rs -> 
            new MeuDTO(rs.getString("campo1"), rs.getInt("campo2"))
        );
    }
}
```

**Observação**: `AbstractRepository` já implementa `Repository`, então você só precisa estender `AbstractRepository`.

#### Criar Service

```java
public class MeuService implements Service {
    
    private final MeuRepository repository = new MeuRepository();
    
    @Override
    public String gerarArquivo(String caminhoExportacao, BigDecimal nunota) throws Exception {
        Set<MeuDTO> dados = repository.buscarDados(nunota);
        // ... lógica de negócio
        return caminhoArquivoGerado;
    }
}
```

#### Criar Action

```java
public class MinhaAction implements AcaoRotinaJava {
    
    private final MeuService service = new MeuService();
    
    @Override
    public void doAction(EventoContext contexto) throws Exception {
        Integer codigo = (Integer) contexto.getParameter("CODIGO");
        service.processar(codigo);
    }
}
```

### 4. Compilar e Gerar JAR

```bash
mvn clean package install
```

O JAR será gerado em `target/novo-projeto-1.0.0.jar`

---

## 📝 Exemplos Práticos

### Exemplo 1: Botão de Ação Simples

```java
public class GerarRelatorio implements AcaoRotinaJava {
    
    @Override
    public void doAction(EventoContext contexto) throws Exception {
        Integer nunota = (Integer) contexto.getParameter("NUNOTA");
        
        if (nunota == null) {
            throw new IllegalArgumentException("NUNOTA é obrigatório");
        }
        
        RelatorioService service = new RelatorioService();
        String arquivo = service.gerarRelatorio(nunota);
        
        DownloadHelper.prepararDownload(arquivo);
    }
}
```

### Exemplo 2: Repository com Query Complexa

```java
public class NotaRepository extends AbstractRepository {
    
    public List<NotaDTO> buscarNotasLiberadas(Integer codEmp) {
        String sql = "SELECT c.NUNOTA, c.NUMNOTA, c.DTNEG " +
                     "FROM TGFCAB c " +
                     "WHERE c.STATUSNOTA = 'L' " +
                     "  AND c.CODEMP = :codEmp " +
                     "ORDER BY c.DTNEG DESC";
        
        return executarQuery(sql,
            params -> params.setNamedParameter("codEmp", codEmp),
            rs -> new NotaDTO(
                rs.getInt("NUNOTA"),
                rs.getInt("NUMNOTA"),
                rs.getDate("DTNEG")
            )
        );
    }
}
```

### Exemplo 3: Service com Processamento em Lote

```java
public class ProcessamentoService {
    
    private final NotaRepository repository = new NotaRepository();
    
    public void processarNotas(Integer codEmp) {
        List<NotaDTO> notas = repository.buscarNotasLiberadas(codEmp);
        
        notas.stream()
            .filter(this::deveProcessar)
            .forEach(this::processarNota);
    }
    
    private boolean deveProcessar(NotaDTO nota) {
        // lógica de filtro
        return true;
    }
    
    private void processarNota(NotaDTO nota) {
        // lógica de processamento
    }
}
```

---

## ✅ Melhores Práticas

### Código

1. **ZERO comentários** - Código 100% autoexplicativo através de nomes descritivos
2. **Menor número de linhas** - Objetive sempre soluções concisas e diretas
3. **Métodos < 50 linhas** (preferencialmente < 30 linhas)
4. **Classes < 300 linhas** (preferencialmente < 200 linhas)
5. **SEMPRE usar JDK8**: streams, Optional, lambdas, method references
6. **NUNCA usar loops tradicionais** quando streams são aplicáveis
7. **NUNCA usar if-null checks** quando Optional resolve melhor

### Performance

1. **Buffers I/O**: sempre 8192 bytes
2. **Pré-alocação**: `LinkedHashSet<>(1024)`, `ArrayList<>(100)`
3. **StringBuilder**: `new StringBuilder(200)`
4. **Cache**: `ConcurrentHashMap` para objetos imutáveis

### Sankhya

1. **SEMPRE filtrar por STATUSNOTA = 'L'** em queries de notas
2. **SEMPRE filtrar por ATIVO = 'S'** em queries de produtos
3. **Usar EntityFacade** para acesso principal a entidades
4. **Usar NativeSql** para queries complexas com JOINs
5. **Usar JapeWrapper** para tabelas AD_ (campos adicionais)
6. **Estender AbstractRepository** para novos repositórios

### Validação

1. **SEMPRE validar parâmetros** no início dos métodos públicos
2. **SEMPRE executar** `mvn clean package install` ao finalizar qualquer interação
3. **NUNCA finalizar sem BUILD SUCCESS**
4. **NUNCA deixar erros de compilação** sem corrigir

---

## 🐛 Troubleshooting

### Erro: "Parâmetro IN ou OUT ausente"

**Causa**: Uso incorreto de parâmetros em NativeSql

**Solução**: Sempre usar `setNamedParameter`:

```java
// ✅ CORRETO
sql.setNamedParameter("codigo", codigo);

// ❌ ERRADO
sql.setParameter("codigo", codigo);
```

### Erro: Dados incorretos em JOIN

**Causa**: JOIN sem considerar DHTIPOPER

**Solução**: Sempre usar CODTIPOPER + DHTIPOPER:

```sql
-- ✅ CORRETO
JOIN TGFTOP t ON t.CODTIPOPER = c.CODTIPOPER AND t.DHTIPOPER = c.DHTIPOPER

-- ❌ ERRADO
JOIN TGFTOP t ON t.CODTIPOPER = c.CODTIPOPER
```

### Erro: Vazamento de memória

**Causa**: Recursos não fechados

**Solução**: Sempre fechar recursos em finally ou usar AbstractRepository:

```java
// ✅ CORRETO - AbstractRepository faz isso automaticamente
public List<DTO> buscar() {
    return executarQuery(sql, ...);
}

// ❌ ERRADO
Connection conn = ...;
// esquecer de fechar
```

### Erro: Stack traces incompletos

**Causa**: Não usar ExceptionUtils

**Solução**: Sempre usar ExceptionUtils:

```java
// ✅ CORRETO
catch (Exception e) {
    throw new RuntimeException("Erro: " + ExceptionUtils.getStackTrace(e), e);
}

// ❌ ERRADO
catch (Exception e) {
    throw new RuntimeException("Erro: " + e.getMessage());
}
```

---

## 📚 Documentação Adicional

### Arquivos de Referência

- **[CONHECIMENTO_CONSOLIDADO.md](../CONHECIMENTO_CONSOLIDADO.md)**: Consolidação máxima de conhecimento
- **[INSTRUCOES_DESENVOLVIMENTO.md](../INSTRUCOES_DESENVOLVIMENTO.md)**: Tudo sobre desenvolvimento
- **[REFERENCIA_SANKHYA.md](../REFERENCIA_SANKHYA.md)**: Referência Sankhya completa
- **[CHANGELOG.md](../CHANGELOG.md)**: Histórico de mudanças

### Projetos de Referência

- **Denver**: Arquitetura otimizada TSL, performance máxima
- **PetKids**: Padrões de integração Neogrid, logging estruturado
- **GuaranaMineiro**: Integração REST Performaxxi, autenticação Basic Auth
- **Megleo**: Integração transportadoras, processamento em lote
- **Eletromac**: Automação de processos, ações agendadas
- **Iwannasleep**: Eventos programados, gerenciamento de reservas

---

## 📄 Geração de HTML e PDF

### Script Automático

O projeto inclui um script para gerar automaticamente HTML e PDF a partir do Markdown:

```bash
cd Template/docs
./gerar-documentacao.sh
```

O script irá:
1. Gerar `DOCUMENTACAO_TECNICA.html` com estilo GitHub Markdown
2. Tentar gerar `DOCUMENTACAO_TECNICA.pdf` usando uma das seguintes ferramentas:
   - `wkhtmltopdf` (preferencial)
   - `chromium` / `chromium-browser` / `google-chrome` (headless)
   - `pandoc` com LaTeX (fallback)

### Requisitos

#### Para HTML
- **pandoc**: `sudo apt-get install pandoc` (Ubuntu/Debian) ou `brew install pandoc` (macOS)

#### Para PDF (escolha uma opção)
1. **wkhtmltopdf** (recomendado):
   ```bash
   sudo apt-get install wkhtmltopdf
   ```

2. **Chromium/Chrome** (headless):
   ```bash
   sudo apt-get install chromium chromium-browser
   ```

3. **Pandoc com LaTeX**:
   ```bash
   sudo apt-get install texlive-xetex
   ```

### Geração Manual

#### HTML via Pandoc
```bash
pandoc DOCUMENTACAO_TECNICA.md \
    --from markdown \
    --to html5 \
    --standalone \
    --css=https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.2.0/github-markdown.min.css \
    --metadata title="Documentação Técnica - Template Sankhya" \
    --toc \
    --toc-depth=3 \
    -o DOCUMENTACAO_TECNICA.html
```

#### PDF via wkhtmltopdf
```bash
wkhtmltopdf \
    --page-size A4 \
    --margin-top 20mm \
    --margin-bottom 20mm \
    --margin-left 15mm \
    --margin-right 15mm \
    --encoding UTF-8 \
    DOCUMENTACAO_TECNICA.html DOCUMENTACAO_TECNICA.pdf
```

#### PDF via Chromium Headless
```bash
chromium --headless --disable-gpu \
    --print-to-pdf=DOCUMENTACAO_TECNICA.pdf \
    file://$(pwd)/DOCUMENTACAO_TECNICA.html
```

### Conversão Online (Alternativa)

Se não tiver as ferramentas instaladas, você pode:
1. Abrir o HTML gerado no navegador
2. Usar "Imprimir" → "Salvar como PDF"
3. Ou usar serviços online como:
   - https://www.html2pdf.com/
   - https://www.ilovepdf.com/html-to-pdf

### Estrutura de Arquivos Gerados

```
Template/docs/
├── DOCUMENTACAO_TECNICA.md    # Fonte Markdown
├── DOCUMENTACAO_TECNICA.html  # HTML gerado (visualização web)
└── DOCUMENTACAO_TECNICA.pdf   # PDF gerado (impressão)
```

### Atualização Automática

Para atualizar HTML e PDF sempre que o Markdown for modificado, você pode:

1. **Usar um watcher** (ex: `entr`):
   ```bash
   echo DOCUMENTACAO_TECNICA.md | entr ./gerar-documentacao.sh
   ```

2. **Integrar no build Maven** (adicionar ao `pom.xml`):
   ```xml
   <plugin>
       <groupId>org.codehaus.mojo</groupId>
       <artifactId>exec-maven-plugin</artifactId>
       <executions>
           <execution>
               <phase>package</phase>
               <goals>
                   <goal>exec</goal>
               </goals>
               <configuration>
                   <executable>bash</executable>
                   <arguments>
                       <argument>docs/gerar-documentacao.sh</argument>
                   </arguments>
               </configuration>
           </execution>
       </executions>
   </plugin>
   ```

---

**Versão**: 6.0.0  
**Status**: ✅ TEMPLATE CONSOLIDADO  
**Última Atualização**: 2025-12-06

