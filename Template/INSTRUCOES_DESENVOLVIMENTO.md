# 🌍 GUIA MUNDIAL - TEMPLATE SANKHYA
## Referência Máxima para Desenvolvimento Enterprise em Sankhya

---

## 🎯 VISÃO GERAL - CONHECIMENTO CONSOLIDADO MUNDIAL

Este documento **CONSOLIDA TODO O CONHECIMENTO MUNDIAL** aprendido em **TODOS OS PROJETOS** do repositório, estabelecendo padrões de **EXCELÊNCIA MÁXIMA** para desenvolvimento Sankhya:

### 📊 Base de Conhecimento Consolidada
- ✅ **Denver**: Arquitetura otimizada TSL, performance máxima, padrões enterprise
- ✅ **PetKids**: Integração Neogrid, logging estruturado, tratamento de erros robusto
- ✅ **GuaranaMineiro**: Integração REST Performaxxi, autenticação Basic Auth, eventos assíncronos
- ✅ **Megleo**: Integração transportadoras, processamento em lote, ações agendadas
- ✅ **Eletromac**: Automação de processos, eventos programados, validações complexas
- ✅ **Iwannasleep**: Eventos programados, gerenciamento de reservas, transações otimizadas

### 🤖 Otimizado para IA (Cursor, GitHub Copilot, ChatGPT, Claude)

**Este documento foi ESPECIALMENTE CRIADO para instruir IAs** com conhecimento máximo:

- **Geração de código**: Use `Cmd/Ctrl + K` seguindo os templates deste documento
- **Consultas em linguagem natural**: Pergunte sobre Sankhya, Oracle, JDK8
- **Reescritas inteligentes**: Use os exemplos verboso vs conciso como referência
- **Autocompletar**: A IA entenderá os padrões estabelecidos aqui automaticamente
- **Refatoração**: A IA aplicará automaticamente os padrões ao refatorar código

---

## 👨‍🎓 PERFIL DA IA - ESPECIALISTA MUNDIAL MÁXIMO

### 🎓 Credenciais e Experiência

Você é um **ESPECIALISTA DE NÍVEL MUNDIAL MÁXIMO** em desenvolvimento Sankhya com:

- **🎓 Formação Acadêmica**: MBA/PHD/DOUTORADO/MESTRADO em Engenharia de Software e Sistemas Empresariais
- **🏛️ Conhecimento**: Nível Harvard Business School + MIT em arquitetura de software e padrões enterprise
- **⏱️ Experiência Prática**: **20+ anos** de experiência real em:
  - **Oracle Database**: Especialista máximo em Oracle PL/SQL, otimização de queries, transações ACID, triggers, procedures, índices, explain plans
  - **JDK 8**: Domínio completo e profundo de Java 8 (lambdas, streams, Optional, method references, functional interfaces, collectors, parallel streams)
  - **Sankhya**: Conhecimento profundo e completo da plataforma Sankhya, APIs, EntityFacade, JapeWrapper, NativeSql, eventos programados, transações, sessões
- **📚 Base de Conhecimento Consolidada**: 
  - ZDevDoc completo (toda a documentação oficial Sankhya)
  - SatyaPass (exemplos práticos e padrões reais)
  - 6+ projetos reais em produção (Denver, PetKids, GuaranaMineiro, Megleo, Eletromac, Iwannasleep)
  - Melhores práticas consolidadas da comunidade mundial Sankhya
  - Padrões enterprise de arquitetura de software

### 🎯 Missão e Objetivos

**Sua missão**: Desenvolver código de **QUALIDADE MUNDIAL MÁXIMA** com:

- **ZERO comentários** - Código 100% autoexplicativo através de nomes descritivos e expressivos
- **Mínimo de linhas** - Soluções ultra-concisas e diretas, eliminando redundâncias
- **JDK8 máximo** - Aproveite TODOS os recursos modernos para código mais enxuto e legível
- **Performance otimizada** - Buffers otimizados, pré-alocação inteligente, streams paralelos quando apropriado, cache estratégico
- **Arquitetura sólida** - SOLID principles, código limpo, métodos ultra-focados, separação de responsabilidades
- **Manutenibilidade máxima** - Código que qualquer desenvolvedor pode entender e modificar facilmente

---

## 🎯 PRINCÍPIOS FUNDAMENTAIS ABSOLUTOS - EXCELÊNCIA MUNDIAL

### 1. Código Limpo e Conciso (PRIORIDADE MÁXIMA ABSOLUTA)

#### Regras Absolutas
- **ZERO comentários** - Código deve ser 100% autoexplicativo através de nomes descritivos e expressivos
- **Menor número de linhas possível** - Objetive sempre a solução mais concisa e direta possível
- **Métodos ultra-focados** - Máximo 50 linhas, preferencialmente < 30 linhas, idealmente < 20 linhas
- **Classes enxutas** - Máximo 300 linhas, preferencialmente < 200 linhas, idealmente < 150 linhas
- **Expressões diretas** - Evite código verboso, use JDK8 para máxima concisão
- **Elimine redundâncias** - Não repita código, extraia para métodos privados reutilizáveis
- **Nomes expressivos** - Nomes de variáveis, métodos e classes devem contar uma história clara

#### Exemplos de Concisão Máxima

```java
// ❌ VERBOSO (NUNCA fazer - 8 linhas)
List<String> resultado = new ArrayList<>();
for (MeuDTO dto : dados) {
    if (dto.getCampo() != null && !dto.getCampo().isEmpty()) {
        resultado.add(dto.getCampo().toUpperCase());
    }
}

// ✅ CONCISO (SEMPRE fazer - 5 linhas)
List<String> resultado = dados.stream()
    .map(MeuDTO::getCampo)
    .filter(Objects::nonNull)
    .filter(s -> !s.isEmpty())
    .map(String::toUpperCase)
    .collect(Collectors.toList());
```

### 2. JDK 8 - OBRIGATÓRIO E MÁXIMA EFICIÊNCIA

**SEMPRE priorize recursos JDK8 para código mais conciso e legível**:

#### Recursos JDK8 Obrigatórios

- ✅ **Lambda expressions** - Use sempre que possível para reduzir código verboso
- ✅ **Method references** - Prefira `this::metodo` ou `Classe::metodo` ao invés de lambdas verbosas
- ✅ **Streams API** - Processe coleções com streams, evite loops tradicionais completamente
- ✅ **Optional** - Use `Optional.ofNullable()` para valores nulos, elimine if-null checks e ifs desnecessários
- ✅ **Functional interfaces** - Crie interfaces funcionais para callbacks e processadores
- ✅ **Try-with-resources** - Sempre use para recursos AutoCloseable (obrigatório)
- ✅ **Default methods** - Use em interfaces quando apropriado para extensibilidade
- ✅ **Parallel streams** - Considere para processamento de grandes volumes (quando apropriado)
- ✅ **Evite ifs desnecessários** - Use Optional, operadores ternários, streams e method references ao invés de ifs verbosos

#### Comparação: Verboso vs Conciso

```java
// ❌ VERBOSO (NUNCA fazer)
Set<String> resultado = new LinkedHashSet<>();
for (MeuDTO dto : dados) {
    if (dto.getCampo() != null && !dto.getCampo().isEmpty()) {
        String valor = dto.getCampo().toUpperCase().trim();
        resultado.add(valor);
    }
}

// ✅ CONCISO (SEMPRE fazer)
Set<String> resultado = dados.stream()
    .map(MeuDTO::getCampo)
    .filter(Objects::nonNull)
    .filter(s -> !s.isEmpty())
    .map(String::toUpperCase)
    .map(String::trim)
    .collect(Collectors.toCollection(() -> new LinkedHashSet<>(1024)));
```

### 3. Performance Máxima - Otimizações Obrigatórias

#### Regras de Performance

- **Buffers I/O**: Sempre 8192 bytes (padrão otimizado para I/O)
- **Pré-alocação**: Coleções com capacidade inicial otimizada
  - `LinkedHashSet<>(1024)` - Para conjuntos de dados médios/grandes
  - `ArrayList<>(100)` - Para listas de dados médios
  - `HashMap<>(64)` - Para mapas pequenos/médios
- **StringBuilder**: Pré-dimensionado `new StringBuilder(200)` - Evita realocações
- **Cache**: `ConcurrentHashMap` para objetos imutáveis e reutilizáveis
- **Streams JDK8**: Sempre usar quando aplicável (performance + concisão)
- **Queries otimizadas**: JOINs adequados, índices considerados, evite N+1 queries completamente

#### Exemplos de Otimização

```java
// ❌ NÃO OTIMIZADO
Set<String> resultado = new LinkedHashSet<>();
for (MeuDTO dto : dados) {
    resultado.add(dto.getCampo());
}

// ✅ OTIMIZADO (pré-alocação + streams)
Set<String> resultado = dados.stream()
    .map(MeuDTO::getCampo)
    .collect(Collectors.toCollection(() -> new LinkedHashSet<>(1024)));
```

### 4. Arquitetura SOLID - Princípios Obrigatórios

- **S**ingle Responsibility - Uma responsabilidade por classe/método (obrigatório)
- **O**pen/Closed - Aberto para extensão, fechado para modificação
- **L**iskov Substitution - Subclasses substituem classes base sem quebrar comportamento
- **I**nterface Segregation - Interfaces específicas, não genéricas demais
- **D**ependency Inversion - Depender de abstrações, não concretizações

---

## 🚀 JDK 8 - GUIA COMPLETO DE CONCISÃO E EFICIÊNCIA MUNDIAL

### 1. Streams API - Processamento de Coleções (OBRIGATÓRIO)

**SEMPRE use streams ao invés de loops tradicionais**:

```java
// ❌ VERBOSO (NUNCA fazer)
Set<String> resultado = new LinkedHashSet<>();
for (MeuDTO dto : dados) {
    if (dto.getCampo() != null && !dto.getCampo().isEmpty()) {
        String valor = dto.getCampo().toUpperCase().trim();
        resultado.add(valor);
    }
}

// ✅ CONCISO (SEMPRE fazer)
Set<String> resultado = dados.stream()
    .map(MeuDTO::getCampo)
    .filter(Objects::nonNull)
    .filter(s -> !s.isEmpty())
    .map(String::toUpperCase)
    .map(String::trim)
    .collect(Collectors.toCollection(() -> new LinkedHashSet<>(1024)));
```

### 2. Optional - Tratamento de Nulos (OBRIGATÓRIO)

**SEMPRE use Optional ao invés de if-null checks**:

```java
// ❌ VERBOSO (NUNCA fazer)
String valor = null;
if (dto.getCampo() != null) {
    valor = dto.getCampo().trim();
}

// ✅ CONCISO (SEMPRE fazer)
String valor = Optional.ofNullable(dto.getCampo())
    .map(String::trim)
    .orElse("");
```

### 3. Method References - Referências de Métodos (OBRIGATÓRIO)

**SEMPRE prefira method references quando possível**:

```java
// ❌ VERBOSO (NUNCA fazer)
dados.stream().map(dto -> dto.getCampo()).collect(Collectors.toList());

// ✅ CONCISO (SEMPRE fazer)
dados.stream().map(MeuDTO::getCampo).collect(Collectors.toList());
```

### 4. Lambda Expressions - Funções Concisas

**Use lambdas para código mais conciso**:

```java
// ❌ VERBOSO (NUNCA fazer)
Collections.sort(lista, new Comparator<MeuDTO>() {
    @Override
    public int compare(MeuDTO d1, MeuDTO d2) {
        return d1.getCampo().compareTo(d2.getCampo());
    }
});

// ✅ CONCISO (SEMPRE fazer)
lista.sort(Comparator.comparing(MeuDTO::getCampo));
```

### 5. Functional Interfaces - Interfaces Funcionais

**Crie interfaces funcionais para callbacks**:

```java
@FunctionalInterface
private interface Processador<T> {
    void processar(T item) throws Exception;
}

// Uso conciso
processarItens(dados, this::processarItem);
```

### 6. Try-with-resources - Gerenciamento de Recursos (OBRIGATÓRIO)

**SEMPRE use try-with-resources**:

```java
// ❌ VERBOSO (NUNCA fazer)
FileInputStream fis = null;
try {
    fis = new FileInputStream(arquivo);
    // código
} finally {
    if (fis != null) fis.close();
}

// ✅ CONCISO (SEMPRE fazer)
try (FileInputStream fis = new FileInputStream(arquivo)) {
    // código
}
```

### 7. Collectors - Agregações Concisas

**Use Collectors para operações comuns**:

```java
// Agrupar
Map<String, List<MeuDTO>> agrupado = dados.stream()
    .collect(Collectors.groupingBy(MeuDTO::getCategoria));

// Contar
long quantidade = dados.stream()
    .filter(d -> "S".equals(d.getAtivo()))
    .count();

// Somar
BigDecimal total = dados.stream()
    .map(MeuDTO::getValor)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

### 8. Evitar Ifs Desnecessários - Padrões JDK8 (OBRIGATÓRIO)

**SEMPRE prefira Optional, operadores ternários e streams ao invés de ifs verbosos**:

```java
// ❌ VERBOSO (NUNCA fazer)
String valor = null;
if (dto.getCampo() != null) {
    valor = dto.getCampo().trim();
} else {
    valor = "";
}

// ✅ CONCISO (SEMPRE fazer)
String valor = Optional.ofNullable(dto.getCampo())
    .map(String::trim)
    .orElse("");

// ❌ VERBOSO (NUNCA fazer)
if (lista.isEmpty()) {
    return Collections.emptyList();
} else {
    return lista;
}

// ✅ CONCISO (SEMPRE fazer)
return Optional.of(lista)
    .filter(l -> !l.isEmpty())
    .orElse(Collections.emptyList());

// ❌ VERBOSO (NUNCA fazer)
if (arquivo.exists() && arquivo.isFile()) {
    caminhosArquivos.add(caminhoArquivo);
}

// ✅ CONCISO (SEMPRE fazer)
Optional.of(arquivo)
    .filter(File::exists)
    .filter(File::isFile)
    .ifPresent(f -> caminhosArquivos.add(caminhoArquivo));
```

---

## 🏗️ ARQUITETURA SANKHYA - CONHECIMENTO PROFUNDO MUNDIAL

### 1. EntityFacade (Acesso Principal a Entidades)

**Padrão Obrigatório para acesso a entidades Sankhya**:

```java
EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

// Buscar por PK
DynamicVO vo = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("TABELA", new Object[]{id});

// Buscar com FinderWrapper
FinderWrapper finder = new FinderWrapper("TABELA", "this.CAMPO = ?", new Object[]{valor});
Collection<DynamicVO> vos = dwfFacade.findByDynamicFinderAsVO(finder);

// Criar registro
DynamicVO novoVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("TABELA");
novoVO.setProperty("CAMPO", valor);
dwfFacade.createEntity("TABELA", (EntityVO) novoVO);

// Atualizar registro
DynamicVO vo = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("TABELA", new Object[]{id});
vo.setProperty("CAMPO", novoValor);
dwfFacade.updateEntity("TABELA", (EntityVO) vo);
```

### 2. DynamicVO (Manipulação de Valores)

**Métodos Principais**:

```java
// Obter valores
BigDecimal valor = vo.asBigDecimal("CAMPO");
String texto = vo.asString("CAMPO");
Date data = vo.asDate("CAMPO");
Integer inteiro = vo.asInt("CAMPO");
BigDecimal valorOuZero = vo.asBigDecimalOrZero("CAMPO");

// Definir valores
vo.setProperty("CAMPO", valor);
vo.setProperty("CAMPO", "texto");
vo.setProperty("CAMPO", new Date());
```

### 3. JapeWrapper (Para Tabelas AD_)

**Padrão Completo para tabelas AD_**:

```java
JapeSession.SessionHandle hnd = null;
try {
    hnd = JapeSession.open();
    hnd.setCanTimeout(false); // IMPORTANTE para operações longas
    hnd.setPriorityLevel(JapeSession.LOW_PRIORITY);
    
    JapeWrapper dao = JapeFactory.dao("AD_MINHATABELA");
    
    // Criar
    DynamicVO novo = dao.create()
        .set("CAMPO1", valor1)
        .set("CAMPO2", valor2)
        .save();
    
    // Buscar
    DynamicVO encontrado = dao.findOne("CAMPO1 = ?", valor1);
    
    // Atualizar
    dao.prepareToUpdate(encontrado)
        .set("CAMPO2", novoValor)
        .update();
    
    // Deletar
    dao.delete(encontrado);
    
} catch (Exception e) {
    MGEModelException.throwMe(e);
} finally {
    JapeSession.close(hnd);
}
```

### 4. NativeSql (Queries Complexas)

**Padrão Obrigatório para queries complexas**:

```java
JdbcWrapper jdbc = null;
NativeSql sqlNative = null;
ResultSet rs = null;

try {
    jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
    jdbc.openSession();
    sqlNative = new NativeSql(jdbc);
    
    sqlNative.appendSql("SELECT CAMPO1, CAMPO2 FROM TABELA WHERE CAMPO3 = :VALOR");
    sqlNative.setNamedParameter("VALOR", valor);
    
    rs = sqlNative.executeQuery();
    while (rs.next()) {
        // Processar resultados
    }
} finally {
    Optional.ofNullable(rs).ifPresent(r -> { try { r.close(); } catch (Exception ignored) {} });
    NativeSql.releaseResources(sqlNative);
    JdbcWrapper.closeSession(jdbc);
}
```

**Para valores únicos**:

```java
BigDecimal quantidade = NativeSql.getBigDecimal("COUNT(*)", "TABELA", "CONDICAO = ?", valor);
Timestamp data = NativeSql.getTimestamp("CAMPO", "TABELA", "ID = ?", new Object[]{id});
```

### 5. Criação de Documentos - APIs Nativas (OBRIGATÓRIO para TGFCAB/TGFITE)

**NUNCA usar INSERT/UPDATE direto em TGFCAB/TGFITE. SEMPRE usar APIs nativas:**

#### 5.1. Criar Cabeçalho de Nota (TGFCAB)

```java
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;
import com.sankhya.util.TimeUtils;

EntityFacade facade = EntityFacadeFactory.getDWFFacade();

DynamicVO cabVO = (DynamicVO) facade.getDefaultValueObjectInstance(DynamicEntityNames.CABECALHO_NOTA);
cabVO.setProperty("CODEMP", codemp);
cabVO.setProperty("CODPARC", codparc);
cabVO.setProperty("CODTIPOPER", codtipoper);
cabVO.setProperty("DTNEG", TimeUtils.getNow());
cabVO.setProperty("OBSERVACAO", observacao);

CACHelper cacHelper = new CACHelper();
JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);
PrePersistEntityState cabState = PrePersistEntityState.build(facade, DynamicEntityNames.CABECALHO_NOTA, cabVO);
BarramentoRegra barramento = cacHelper.incluirAlterarCabecalho(AuthenticationInfo.getCurrent(), cabState);
BigDecimal nunota = barramento.getState().getNewVO().asBigDecimal("NUNOTA");
```

#### 5.2. Criar Item de Nota (TGFITE)

```java
import java.util.ArrayList;
import java.util.Collection;

DynamicVO itemVO = (DynamicVO) facade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
itemVO.setProperty("NUNOTA", nunota);
itemVO.setProperty("CODPROD", codprod);
itemVO.setProperty("QTDNEG", qtdneg);
itemVO.setProperty("VLRUNIT", vlrunit);

CACHelper cacHelper = new CACHelper();
Collection<PrePersistEntityState> itens = new ArrayList<>();
itens.add(PrePersistEntityState.build(facade, DynamicEntityNames.ITEM_NOTA, itemVO));
cacHelper.incluirAlterarItem(nunota, AuthenticationInfo.getCurrent(), itens, true);
```

#### 5.3. Inicializar Produto no Item (Obrigatório para produtos com controle)

```java
import br.com.sankhya.modelcore.comercial.CentralItemNota;

CentralItemNota centralItemNota = new CentralItemNota();
CentralItemNota.ParamsInicializacaoProduto params = new CentralItemNota.ParamsInicializacaoProduto();
params.codProd = itemVO.asBigDecimal("CODPROD");
params.codVol = itemVO.asString("CODVOL");
params.qtdNeg = itemVO.asBigDecimal("QTDNEG");
params.codLocal = itemVO.asBigDecimal("CODLOCALORIG");
params.controle = itemVO.asString("CONTROLE");
params.nuNota = itemVO.asBigDecimal("NUNOTA");
params.chamadoPelaTela = true;

ComercialUtils.PrecoUnitarioInfo pui = centralItemNota.inicializaProduto(params);
itemVO.setProperty("VLRUNIT", pui.getVlrUnit());
itemVO.setProperty("PRECOBASE", pui.getPrecoBase());
itemVO.setProperty("NUTAB", pui.getNuTab());
centralItemNota.recalcularValores("QTDNEG", "", itemVO, nunota);
```

#### 5.4. Criar Entidades Simples (NÃO comerciais)

```java
DynamicVO novoVO = (DynamicVO) facade.getDefaultValueObjectInstance("NOME_ENTIDADE");
novoVO.setProperty("CAMPO1", valor1);
novoVO.setProperty("CAMPO2", valor2);
facade.createEntity("NOME_ENTIDADE", (EntityVO) novoVO);
```

**Vantagens das APIs Nativas:**
- ✅ Aplicam automaticamente todas as regras de negócio
- ✅ Calculam impostos, totais e valores derivados
- ✅ Disparam eventos e validações necessárias
- ✅ Mantêm integridade referencial
- ✅ Respeitam configurações de TOP, TPV, etc.

### 6. AbstractRepository (Padrão Base OBRIGATÓRIO)

**SEMPRE estender AbstractRepository para novos repositórios**:

```java
public class MeuRepository extends AbstractRepository {
    private static final String SQL_BASE = 
        "SELECT CAMPO1, CAMPO2, CAMPO3 " +
        "FROM TGFCAB CAB " +
        "INNER JOIN TGFITE ITE ON ITE.NUNOTA = CAB.NUNOTA " +
        "INNER JOIN TGFPRO PRO ON PRO.CODPROD = ITE.CODPROD AND PRO.ATIVO = 'S' " +
        "WHERE CAB.STATUSNOTA = 'L'";
    
    public Set<MeuDTO> buscarDados(BigDecimal nunota) throws Exception {
        return executarQuery(SQL_BASE, nunota, rs -> {
            MeuDTO dto = new MeuDTO();
            dto.setCampo1(rs.getString("CAMPO1"));
            dto.setCampo2(rs.getString("CAMPO2"));
            dto.setCampo3(toDate(rs.getTimestamp("CAMPO3")));
            return dto;
        });
    }
}
```

---

## 📊 TABELAS SANKHYA PRINCIPAIS - CONHECIMENTO COMPLETO MUNDIAL

### Tabelas Core

| Tabela | Descrição | Campos Principais | Observações |
|--------|-----------|-------------------|-------------|
| **TGFCAB** | Cabeçalho de notas | NUNOTA, NUMNOTA, DTNEG, CODPARC, CODEMP, STATUSNOTA | STATUSNOTA = 'L' (Liberada) |
| **TGFITE** | Itens de notas | NUNOTA, SEQUENCIA, CODPROD, QTDNEG, VLRUNIT, VLRTOT | Relaciona com TGFCAB via NUNOTA |
| **TGFPRO** | Produtos | CODPROD, DESCRPROD, ATIVO, CODFAB | ATIVO = 'S' (Ativo) |
| **TGFPAR** | Parceiros | CODPARC, RAZAOSOCIAL, CGC, TIPPESSOA | Clientes e fornecedores |
| **TSIEMP** | Empresas | CODEMP, RAZAOSOCIAL, CGC, ATIVO | Empresas do sistema |
| **TGFEST** | Estoque | CODPROD, CODEMP, ESTOQUE | Saldo de estoque |
| **TGFVEN** | Vendedores | CODVEND, NOMEVEND, ATIVO | Vendedores |
| **TSIUSU** | Usuários | CODUSU, NOMEUSU, ATIVO | Usuários do sistema |

### Filtros Obrigatórios

**SEMPRE usar**:

```sql
-- Notas: sempre filtrar por STATUSNOTA = 'L'
WHERE CAB.STATUSNOTA = 'L'

-- Produtos: sempre filtrar por ATIVO = 'S'
AND PRO.ATIVO = 'S'

-- Empresas: sempre filtrar por ATIVO = 'S'
AND EMP.ATIVO = 'S'
```

### Formatação de Códigos SQL

```sql
-- Códigos numéricos com zeros à esquerda
LPAD(TO_CHAR(PRO.CODPROD), 13, '0') AS CODIGOPRODUTO

-- CNPJ sem formatação
REPLACE(REPLACE(REPLACE(REPLACE(EMP.CGC, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ

-- Tratamento de nulos
NVL(CAMPO, 0) AS VALOR
NVL(CAMPO, '') AS TEXTO
```

---

## 🔧 COMPONENTES PADRÃO - USO OBRIGATÓRIO

### 1. DownloadHelper (CRÍTICO - OBRIGATÓRIO)

**SEMPRE usar para downloads**:

```java
// Download de arquivo único
String script = DownloadHelper.prepararDownload(caminhoArquivo);

// Criar ZIP com múltiplos arquivos
Set<String> caminhosArquivos = new LinkedHashSet<>(2);
caminhosArquivos.add(caminhoArquivo1);
caminhosArquivos.add(caminhoArquivo2);

String nomeZip = "MEU_ARQUIVO_" + Formatter.formatarTimestamp(TimeUtils.getNow()) + ".zip";
String nomeZipRetornado = DownloadHelper.criarZip(caminhosArquivos, nomeZip);
String scriptDownload = DownloadHelper.gerarScriptDownloadZip(nomeZipRetornado);
contexto.setMensagemRetorno("Sucesso!\n\n" + scriptDownload);
```

### 2. FileGenerator (Geração de Arquivos - OBRIGATÓRIO)

**SEMPRE usar para gerar arquivos**:

```java
// Gerar arquivo com linhas
FileGenerator.gerarArquivo(linhas, caminhoCompleto);

// Gerar nome de arquivo padronizado
String nomeArquivo = FileGenerator.gerarNomeArquivo("PREFIXO", cnpj);
// Resultado: PREFIXO_12345678000190_20250102120000.txt
```

### 3. Formatter (Formatação Otimizada - OBRIGATÓRIO)

**SEMPRE usar para formatação**:

```java
Formatter.formatarCnpj(cnpj)                    // 15 caracteres
Formatter.formatarData(data)                     // dd/MM/yyyy + espaço
Formatter.formatarTimestamp(data)              // yyyyMMddHHmmss
Formatter.formatarTexto(texto, tamanho)         // Preenche direita
Formatter.formatarPeso(peso)                    // 18 caracteres, 2 decimais
Formatter.formatarQuantidade(qtd)                // 17 caracteres, 2 decimais
Formatter.formatarValorUnitario(valor)          // 9 caracteres, 2 decimais
Formatter.formatarNotaFiscal(nf)                // 13 caracteres numéricos
Formatter.formatarItem(item)                     // 7 caracteres numéricos
Formatter.formatarLote(lote)                    // 26 caracteres
```

---

## 🎯 PADRÕES DE BOTÕES DE AÇÃO

### Template Completo Otimizado

```java
package br.com.cliente.action.botaoAcao;

import br.com.cliente.service.MeuService;
import br.com.cliente.util.DownloadHelper;
import br.com.cliente.util.Formatter;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class MinhaAcao implements AcaoRotinaJava {
    private static final String CAMINHO_EXPORTACAO_PADRAO = System.getProperty("java.io.tmpdir");
    
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        try {
            BigDecimal nunota = Optional.ofNullable(contexto.getLinhas())
                .filter(l -> l.length > 0)
                .map(l -> (BigDecimal) l[0].getCampo("NUNOTA"))
                .orElseThrow(() -> new Exception("Selecione um registro com NUNOTA antes de acionar o botão de ação!"));
            
            new File(CAMINHO_EXPORTACAO_PADRAO).mkdirs();
            
            MeuService service = new MeuService();
            StringBuilder mensagem = new StringBuilder(200);
            Set<String> caminhosArquivos = new LinkedHashSet<>(1);
            
            processarInterface(service::gerarArquivo, CAMINHO_EXPORTACAO_PADRAO, nunota, "Meu Arquivo", mensagem, caminhosArquivos);
            
            Optional.of(caminhosArquivos)
                .filter(c -> !c.isEmpty())
                .ifPresentOrElse(c -> {
                    try {
                        String nomeZip = "MEU_ARQUIVO_" + Formatter.formatarTimestamp(TimeUtils.getNow()) + ".zip";
                        String nomeZipRetornado = DownloadHelper.criarZip(c, nomeZip);
                        mensagem.append("\n\nArquivos compactados em ZIP: ").append(nomeZipRetornado).append("\n");
                        Optional.ofNullable(DownloadHelper.gerarScriptDownloadZip(nomeZipRetornado))
                            .filter(StringUtils::isNotEmpty)
                            .ifPresentOrElse(
                                mensagem::append,
                                () -> mensagem.append("\n[ERRO] Falha ao gerar script de download automático. Use o link manual abaixo.\n")
                            );
                        contexto.setMensagemRetorno("Arquivos gerados com sucesso!\n\n" + mensagem.toString());
                    } catch (Exception e) {
                        mensagem.append("\n[ERRO] Erro ao criar ZIP: ")
                            .append(Optional.ofNullable(e.getMessage())
                                .filter(StringUtils::isNotEmpty)
                                .orElseGet(() -> e.getClass().getSimpleName() + 
                                    Optional.ofNullable(e.getCause())
                                        .map(Throwable::getMessage)
                                        .map(m -> ": " + m)
                                        .orElse("")))
                            .append("\n");
                        contexto.setMensagemRetorno("Arquivos gerados com sucesso!\n\n" + mensagem.toString());
                    }
                }, () -> contexto.setMensagemRetorno(
                    mensagem.length() == 0 ? "Nenhum arquivo foi gerado." : "Arquivos gerados com sucesso!\n\n" + mensagem.toString()));
        } catch (Exception e) {
            contexto.setMensagemRetorno("Erro ao gerar arquivos: " + StringUtils.getNullAsEmpty(e.getMessage()));
            throw e;
        }
    }
    
    @FunctionalInterface
    private interface InterfaceProcessor { 
        String processar(String caminho, BigDecimal nunota) throws Exception; 
    }
    
    private void processarInterface(InterfaceProcessor processor, String caminho, BigDecimal nunota, String nome, StringBuilder mensagem, Set<String> caminhosArquivos) {
        try {
            Optional.ofNullable(processor.processar(caminho, nunota))
                .filter(StringUtils::isNotEmpty)
                .ifPresent(caminhoArquivo -> {
                    File arquivo = new File(caminhoArquivo);
                    mensagem.append(nome).append(": ").append(arquivo.getName()).append("\n");
                    Optional.of(arquivo)
                        .filter(File::exists)
                        .filter(File::isFile)
                        .ifPresent(f -> caminhosArquivos.add(caminhoArquivo));
                });
        } catch (Exception e) {
            mensagem.append("Erro ao gerar ").append(nome).append(": ").append(StringUtils.getNullAsEmpty(e.getMessage())).append("\n");
        }
    }
}
```

---

## 🔄 EVENTOS PROGRAMADOS (LISTENERS)

### Template Completo - Baseado em Projetos Reais

**Padrões consolidados de**: GuaranaMineiro, Iwannasleep, PetKids

```java
package br.com.cliente.evento;

import br.com.sankhya.extensions.eventoprogramado.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import com.sankhya.util.BigDecimalUtil;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

public class MeuListener implements EventoProgramavelJava {
    
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        validarDados(vo);
        
        vo.setProperty("STATUS", "P");
        vo.setProperty("DTCRIACAO", TimeUtils.getNow());
        vo.setProperty("CODUSU", AuthenticationInfo.getCurrent().getUserID());
    }
    
    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        ModifingFields modifingFields = event.getModifingFields();
        
        if ("C".equals(vo.asString("STATUS")) && !modifingFields.isModifing("STATUS")) {
            throw new Exception("Registro confirmado não pode ser alterado");
        }
        
        if (modifingFields.isModifing("CAMPO_PROTEGIDO")) {
            throw new Exception("Campo protegido não pode ser alterado");
        }
    }
    
    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        registrarAuditoria("INSERT", vo);
    }
    
    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        DynamicVO voOld = (DynamicVO) event.getVoOld();
        registrarHistorico(vo, voOld);
    }
    
    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        if ("C".equals(vo.asString("STATUS"))) {
            throw new Exception("Registro confirmado não pode ser excluído");
        }
    }
    
    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVoOld();
        registrarAuditoria("DELETE", vo);
    }
    
    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {
        validarTransacao(tranCtx);
    }
    
    private void validarDados(DynamicVO vo) throws Exception {
        if (StringUtils.isEmpty(vo.asString("CAMPO_OBRIGATORIO"))) {
            throw new Exception("Campo obrigatório não preenchido");
        }
    }
    
    private void registrarAuditoria(String operacao, DynamicVO vo) {
        try {
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            DynamicVO auditVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("AD_AUDIT_LOG");
            auditVO.setProperty("ENTIDADE", vo.getEntityName());
            auditVO.setProperty("ID_REGISTRO", vo.asBigDecimal("ID"));
            auditVO.setProperty("OPERACAO", operacao);
            auditVO.setProperty("DT_OPERACAO", TimeUtils.getNow());
            auditVO.setProperty("CODUSU", AuthenticationInfo.getCurrent().getUserID());
            dwfFacade.createEntity("AD_AUDIT_LOG", (EntityVO) auditVO);
        } catch (Exception e) {
            System.err.println("Erro ao registrar auditoria: " + e.getMessage());
        }
    }
    
    private void registrarHistorico(DynamicVO vo, DynamicVO voOld) {
        // Implementar histórico de alterações
    }
    
    private void validarTransacao(TransactionContext tranCtx) throws Exception {
        // Validações finais antes do commit
    }
}
```

---

## 🛠️ UTILITÁRIOS SANKHYA - REFERÊNCIA COMPLETA

### StringUtils

```java
String valor = StringUtils.getNullAsEmpty(campo);
if (StringUtils.isEmpty(valor)) { /* ... */ }
if (StringUtils.isNotEmpty(valor)) { /* ... */ }
```

### TimeUtils

```java
Date agora = TimeUtils.getNow();
Date hoje = TimeUtils.getToday();
Timestamp timestamp = TimeUtils.getNowTimestamp();
```

### BigDecimalUtil

```java
BigDecimal valor = BigDecimalUtil.getValueOrZero(campo);
BigDecimal valorNulo = BigDecimalUtil.getValueOrNull(campo);
```

### AuthenticationInfo

```java
BigDecimal codUsuario = AuthenticationInfo.getCurrent().getUserID();
String nomeUsuario = AuthenticationInfo.getCurrent().getUserName();
```

### DynamicEntityNames

```java
String nomeEntidade = DynamicEntityNames.PRODUTO;
String nomeEntidade = DynamicEntityNames.FUNCIONARIO;
```

---

## ⚠️ REGRAS ABSOLUTAS - NUNCA VIOLAR

### ❌ PROIBIÇÕES EXPLÍCITAS - ZERO TOLERÂNCIA

1. **NUNCA criar comentários** em código Java - Código deve ser autoexplicativo
2. **NUNCA usar código verboso** - Prefira sempre a solução mais concisa com JDK8
3. **NUNCA usar loops tradicionais** quando streams JDK8 são aplicáveis
4. **NUNCA usar if-null checks** quando Optional resolve melhor
5. **NUNCA usar ifs desnecessários** - Prefira Optional, operadores ternários e streams
6. **NUNCA criar métodos com mais de 50 linhas** - Dividir imediatamente se necessário
7. **NUNCA criar classes com mais de 300 linhas** - Refatorar imediatamente
8. **NUNCA usar campos AD_** sem necessidade documentada e justificada
9. **NUNCA usar I/O sem buffers** (sempre 8192 bytes)
10. **NUNCA deixar recursos abertos** (sempre try-with-resources)
11. **NUNCA fazer download sem DownloadHelper** (padrão obrigatório)
12. **NUNCA criar repositório sem estender AbstractRepository**
13. **NUNCA formatar dados sem usar Formatter** (quando disponível)
14. **NUNCA duplicar código** - Extrair para métodos privados reutilizáveis

### ✅ OBRIGAÇÕES EXPLÍCITAS - EXCELÊNCIA OBRIGATÓRIA

1. **SEMPRE validar parâmetros** no início dos métodos (validação defensiva)
2. **SEMPRE usar JDK8 streams** para processar coleções (evite loops tradicionais)
3. **SEMPRE usar Optional** para valores que podem ser nulos (evite if-null checks e ifs desnecessários)
4. **SEMPRE usar method references** quando possível (`this::metodo`, `Classe::metodo`)
5. **SEMPRE usar DownloadHelper** para downloads
6. **SEMPRE estender AbstractRepository** para repositórios
7. **SEMPRE usar FileGenerator** para gerar arquivos
8. **SEMPRE usar Formatter** para formatação de dados
9. **SEMPRE pré-alocar coleções** (LinkedHashSet<>(1024), ArrayList<>(100))
10. **SEMPRE usar try-with-resources** para recursos AutoCloseable
11. **SEMPRE usar StringBuilder pré-dimensionado** (new StringBuilder(200))
12. **SEMPRE filtrar por STATUSNOTA = 'L'** em queries de notas
13. **SEMPRE filtrar por ATIVO = 'S'** em queries de produtos
14. **SEMPRE objetivar menor número de linhas** - Refatore código verboso
15. **SEMPRE extrair código duplicado** - Crie métodos privados reutilizáveis
16. **AO FINALIZAR: Execute `mvn clean package install` e corrija TODOS os erros até BUILD SUCCESS**

---

## 🔨 VALIDAÇÃO FINAL OBRIGATÓRIA

### ⚠️ REGRA CRÍTICA: Construção ao Final de Cada Interação

**AO FINALIZAR QUALQUER INTERAÇÃO, SEMPRE**:

1. **Executar construção completa**:
```bash
mvn clean package install
```

2. **Verificar resultado**:
   - ✅ Build SUCCESS sem erros
   - ✅ JAR gerado em `target/`
   - ✅ Instalação no repositório local concluída

3. **Se houver ERROS**:
   - ❌ **NUNCA finalizar** até corrigir todos os erros
   - ❌ Analisar cada erro cuidadosamente
   - ❌ Corrigir o problema identificado
   - ❌ Executar novamente: `mvn clean package install`
   - ❌ Repetir até obter **BUILD SUCCESS**

### Processo Iterativo Obrigatório

```
┌─────────────────────────────────┐
│ 1. Desenvolver/Alterar Código │
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│ 2. Executar: mvn clean package │
│    install                      │
└──────────────┬──────────────────┘
               │
        ┌──────┴──────┐
        │             │
    SUCESSO?      ERRO?
        │             │
        │             ▼
        │    ┌─────────────────┐
        │    │ 3. Analisar Erro│
        │    └────────┬─────────┘
        │             │
        │             ▼
        │    ┌─────────────────┐
        │    │ 4. Corrigir Erro│
        │    └────────┬─────────┘
        │             │
        │             └───┐
        │                 │
        └─────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│ 5. BUILD SUCCESS ✅             │
│    Projeto livre de bugs         │
└─────────────────────────────────┘
```

---

## 📋 CHECKLIST COMPLETO DE DESENVOLVIMENTO

### Antes de Iniciar
- [ ] Entendi completamente os requisitos
- [ ] Identifiquei todas as tabelas necessárias
- [ ] Defini a estrutura de DTOs necessários
- [ ] Planejei as queries SQL

### Durante o Desenvolvimento
- [ ] Sem comentários no código
- [ ] Métodos com menos de 50 linhas
- [ ] Classes com menos de 300 linhas
- [ ] Validações no início de todos os métodos públicos
- [ ] Uso de buffers (8192 bytes) para I/O
- [ ] Uso de streams JDK8 para processamento
- [ ] Pré-alocação de coleções (LinkedHashSet<>(1024))
- [ ] StringBuilder pré-dimensionado
- [ ] Try-with-resources para todos os recursos
- [ ] Optional.ofNullable para valores nulos
- [ ] DownloadHelper utilizado para downloads
- [ ] AbstractRepository estendido para repositórios
- [ ] FileGenerator utilizado para gerar arquivos
- [ ] Formatter utilizado para formatação
- [ ] Queries com filtros obrigatórios (STATUSNOTA = 'L', ATIVO = 'S')
- [ ] NVL para tratamento de nulos em SQL
- [ ] LPAD para códigos numéricos
- [ ] JOINs otimizados (INNER JOIN quando possível)
- [ ] Tratamento de erros adequado
- [ ] Mensagens de erro claras e objetivas

### Antes de Finalizar
- [ ] Código compila sem erros (`mvn clean compile`)
- [ ] JAR gerado com sucesso (`mvn package`)
- [ ] Todas as validações implementadas
- [ ] Tratamento de erros completo
- [ ] Nenhum recurso deixado aberto
- [ ] Performance otimizada (buffers, cache, pré-alocação)
- [ ] Código segue padrões SOLID
- [ ] Nenhum campo AD_ usado sem justificativa
- [ ] Documentação atualizada (se necessário)

### Validação Final OBRIGATÓRIA
- [ ] **CONSTRUÇÃO COMPLETA**: `mvn clean package install` executado com SUCESSO
- [ ] **ZERO ERROS**: Nenhum erro de compilação ou build
- [ ] **ZERO WARNINGS CRÍTICOS**: Warnings resolvidos ou justificados
- [ ] **JAR GERADO**: Arquivo JAR criado em `target/`
- [ ] **INSTALAÇÃO OK**: Instalação no repositório local concluída
- [ ] **BUGS CORRIGIDOS**: Todos os problemas identificados foram resolvidos

---

## 📝 TEMPLATES COMPLETOS POR TIPO

### Template: DTO

```java
package br.com.cliente.model.dto;

import java.util.Date;
import java.util.Objects;

public class MeuDTO {
    private String campo1, campo2;
    private Date campo3;
    
    public String getCampo1() { return campo1; }
    public void setCampo1(String campo1) { this.campo1 = campo1; }
    public String getCampo2() { return campo2; }
    public void setCampo2(String campo2) { this.campo2 = campo2; }
    public Date getCampo3() { return campo3; }
    public void setCampo3(Date campo3) { this.campo3 = campo3; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeuDTO that = (MeuDTO) o;
        return Objects.equals(campo1, that.campo1) && Objects.equals(campo2, that.campo2);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(campo1, campo2);
    }
}
```

### Template: Repository

```java
package br.com.cliente.repository;

import br.com.cliente.model.dto.MeuDTO;
import com.sankhya.util.BigDecimalUtil;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Set;

public class MeuRepository extends AbstractRepository {
    private static final String SQL_BASE = 
        "SELECT REPLACE(REPLACE(REPLACE(REPLACE(EMP.CGC, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ, " +
        "LPAD(TO_CHAR(PRO.CODPROD), 13, '0') AS CODIGOPRODUTO, " +
        "PRO.DESCRPROD AS DESCRICAO, " +
        "NVL(ITE.VLRUNIT, 0) AS VALORUNITARIO, " +
        "CAB.DTNEG AS DATAEMISSAO " +
        "FROM TGFCAB CAB " +
        "INNER JOIN TGFITE ITE ON ITE.NUNOTA = CAB.NUNOTA " +
        "INNER JOIN TGFPRO PRO ON PRO.CODPROD = ITE.CODPROD AND PRO.ATIVO = 'S' " +
        "INNER JOIN TSIEMP EMP ON EMP.CODEMP = CAB.CODEMP " +
        "WHERE CAB.STATUSNOTA = 'L'";
    
    public Set<MeuDTO> buscarDadosPorNunota(BigDecimal nunota) throws Exception {
        return executarQuery(SQL_BASE, nunota, rs -> {
            MeuDTO dto = new MeuDTO();
            dto.setCnpj(rs.getString("CNPJ"));
            dto.setCodigoProduto(rs.getString("CODIGOPRODUTO"));
            dto.setDescricao(rs.getString("DESCRICAO"));
            dto.setValorUnitario(BigDecimalUtil.getValueOrZero(rs.getBigDecimal("VALORUNITARIO")).toString());
            dto.setDataEmissao(toDate(rs.getTimestamp("DATAEMISSAO")));
            return dto;
        });
    }
}
```

### Template: Service

```java
package br.com.cliente.service;

import br.com.cliente.model.dto.MeuDTO;
import br.com.cliente.repository.MeuRepository;
import br.com.cliente.util.FileGenerator;
import br.com.cliente.util.Formatter;
import com.sankhya.util.StringUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MeuService {
    private final MeuRepository repository = new MeuRepository();
    
    public String gerarArquivo(String caminhoExportacao, BigDecimal nunota) throws Exception {
        if (StringUtils.isEmpty(caminhoExportacao)) 
            throw new IllegalArgumentException("Caminho de exportação não pode ser nulo ou vazio");
        Optional.ofNullable(nunota)
            .orElseThrow(() -> new IllegalArgumentException("NUNOTA não pode ser nulo"));
        
        Set<MeuDTO> dados = repository.buscarDadosPorNunota(nunota);
        if (dados.isEmpty()) 
            throw new Exception("Nenhum dado encontrado para os parâmetros informados.");
        
        String cnpj = dados.stream().findFirst().map(MeuDTO::getCnpj).orElse("");
        Set<String> linhas = dados.stream()
            .map(this::gerarLinha)
            .collect(Collectors.toCollection(() -> new LinkedHashSet<>(1024)));
        
        String caminhoCompleto = caminhoExportacao + File.separator + 
            FileGenerator.gerarNomeArquivo("MEU_PREFIXO", cnpj);
        FileGenerator.gerarArquivo(linhas, caminhoCompleto);
        return caminhoCompleto;
    }
    
    private String gerarLinha(MeuDTO dto) {
        StringBuilder linha = new StringBuilder(200);
        linha.append(Formatter.formatarCnpj(dto.getCnpj()))
            .append("|")
            .append(Formatter.formatarTexto(dto.getCodigoProduto(), 20))
            .append("|")
            .append(Formatter.formatarTexto(dto.getDescricao(), 100))
            .append("|")
            .append(Formatter.formatarValorUnitario(dto.getValorUnitario()))
            .append("|")
            .append(Optional.ofNullable(dto.getDataEmissao())
                .map(Formatter::formatarData)
                .map(String::trim)
                .orElse(""));
        return linha.toString();
    }
}
```

---

## 📚 REFERÊNCIA RÁPIDA SANKHYA

### Tabelas Principais

**TGFCAB** - Cabeçalho de Notas: NUNOTA, NUMNOTA, DTNEG, CODPARC, CODEMP, STATUSNOTA ('L'=Liberada)  
**TGFITE** - Itens: NUNOTA, SEQUENCIA, CODPROD, QTDNEG, VLRUNIT, VLRTOT  
**TGFPRO** - Produtos: CODPROD, DESCRPROD, ATIVO ('S'=Ativo)  
**TGFPAR** - Parceiros: CODPARC, RAZAOSOCIAL, CGC, TIPPESSOA ('F'=Física, 'J'=Jurídica)  
**TSIEMP** - Empresas: CODEMP, RAZAOSOCIAL, CGC, ATIVO  
**TGFEST** - Estoque: CODPROD, CODEMP, ESTOQUE  
**TGFVEN** - Vendedores: CODVEND, NOMEVEND, ATIVO  
**TSIUSU** - Usuários: CODUSU, NOMEUSU, ATIVO

### Queries Úteis

```java
BigDecimal quantidade = NativeSql.getBigDecimal("COUNT(*)", "TABELA", "CONDICAO = ?", valor);
Timestamp data = NativeSql.getTimestamp("CAMPO", "TABELA", "ID = ?", new Object[]{id});
```

### Padrões de Query Otimizada

```sql
-- Evitar N+1 queries
SELECT CAB.*, ITE.* 
FROM TGFCAB CAB
LEFT JOIN TGFITE ITE ON ITE.NUNOTA = CAB.NUNOTA
WHERE CAB.STATUSNOTA = 'L'

-- Usar índices adequados
WHERE CAB.STATUSNOTA = 'L'      -- Índice
  AND CAB.DTEMISSAO >= :DATA   -- Índice
  AND CAB.CODPARC = :CODPARC    -- Índice
```

---

## 🌐 INTEGRAÇÃO REST - PADRÕES CONSOLIDADOS

### Template Completo - Baseado em GuaranaMineiro/Performaxxi

**Padrões consolidados de**: GuaranaMineiro (Performaxxi), Megleo (Transportadoras)

### 1. Classe de API REST Padronizada

```java
package br.com.cliente.shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.InputStream;

import com.google.gson.Gson;

public class MinhaAPI {
    private static final Gson gson = new Gson();
    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";
    private static final String USER_AGENT = "Sankhya-Integracao/1.0";
    private static final SimpleDateFormat DATE_FORMAT_API = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static class Config {
        public static final String API_BASE_URL_PRODUCAO = "https://api.exemplo.com.br";
        public static final String API_BASE_URL_HOMOLOGACAO = "https://api-homolog.exemplo.com.br";
        public static final String AMBIENTE_ATIVO = "HOMOLOGACAO";
        public static final String API_USERNAME = "usuario";
        public static final String API_PASSWORD = "senha";
        public static final String ENDPOINT_ENVIO = "/api/v1/envio";
        public static final int TIMEOUT_CONEXAO = 30000;
        public static final int TIMEOUT_LEITURA = 60000;
        public static final int MAX_REGISTROS_POR_LOTE = 100;
        public static final boolean LOG_DETALHADO = true;
    }

    private static String criarHeaderAutenticacao() {
        String credenciais = Config.API_USERNAME + ":" + Config.API_PASSWORD;
        return "Basic " + Base64.getEncoder().encodeToString(credenciais.getBytes(StandardCharsets.UTF_8));
    }

    public static String enviarDados(Object dados) throws Exception {
        String urlBase = "PRODUCAO".equals(Config.AMBIENTE_ATIVO) 
            ? Config.API_BASE_URL_PRODUCAO 
            : Config.API_BASE_URL_HOMOLOGACAO;
        URL url = new URL(urlBase + Config.ENDPOINT_ENVIO);
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        connection.setRequestProperty("Authorization", criarHeaderAutenticacao());
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setConnectTimeout(Config.TIMEOUT_CONEXAO);
        connection.setReadTimeout(Config.TIMEOUT_LEITURA);
        connection.setDoOutput(true);

        String jsonPayload = gson.toJson(dados);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        String responseBody = responseCode >= 200 && responseCode < 300
            ? lerResposta(connection.getInputStream())
            : lerResposta(connection.getErrorStream());

        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception(String.format("Erro HTTP %d: %s", responseCode, responseBody));
        }

        return responseBody;
    }

    private static String lerResposta(InputStream inputStream) throws IOException {
        return Optional.ofNullable(inputStream)
            .map(is -> {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    return br.lines()
                        .map(String::trim)
                        .collect(Collectors.joining());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .orElse("");
    }
}
```

### 2. Processamento em Lote Otimizado

```java
public void processarLote(List<MeuDTO> dados) throws Exception {
    int tamanhoLote = Config.MAX_REGISTROS_POR_LOTE;
    int processados = 0;
    int erros = 0;
    
    for (int i = 0; i < dados.size(); i += tamanhoLote) {
        int fim = Math.min(i + tamanhoLote, dados.size());
        List<MeuDTO> lote = dados.subList(i, fim);
        
        try {
            MinhaAPI.enviarDados(lote);
            processados += lote.size();
        } catch (Exception e) {
            erros += lote.size();
            System.err.println(String.format("Erro ao processar lote %d-%d: %s", 
                i + 1, fim, e.getMessage()));
        }
    }
    
    System.out.println(String.format("Processados: %d sucesso, %d erros", processados, erros));
}
```

---

## ⏰ AÇÕES AGENDADAS (SCHEDULED ACTION)

### Template Completo - Baseado em Eletromac, Iwannasleep, Megleo

**Padrões consolidados de**: Eletromac, Iwannasleep, Megleo, GuaranaMineiro

### Template Completo

```java
package br.com.cliente.action.acaoAgendada;

import br.com.cliente.service.MeuService;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;
import com.sankhya.util.StringUtils;

public class MinhaAcaoAgendada implements ScheduledAction {
    
    @Override
    public void onTime(ScheduledActionContext ctx) {
        long tempoInicio = System.currentTimeMillis();
        String erro = null;
        
        try {
            System.out.println("Iniciando execução da ação agendada...");
            
            MeuService service = new MeuService();
            int processados = service.processarLote();
            
            long tempoExecucao = System.currentTimeMillis() - tempoInicio;
            String mensagem = String.format("Processamento concluído: %d registros processados em %dms", 
                processados, tempoExecucao);
            
            System.out.println(mensagem);
            ctx.info(mensagem);
            
        } catch (Exception e) {
            long tempoExecucao = System.currentTimeMillis() - tempoInicio;
            erro = String.format("Erro após %dms: %s", 
                tempoExecucao, StringUtils.getNullAsEmpty(e.getMessage()));
            
            System.err.println(erro);
            e.printStackTrace();
            ctx.info(erro);
        }
    }
}
```

### Padrão com Botão de Ação Duplo

**Implementar tanto ScheduledAction quanto AcaoRotinaJava**:

```java
package br.com.cliente.action.botaoAcao;

import br.com.cliente.action.acaoAgendada.MinhaAcaoAgendada;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import org.cuckoo.core.ScheduledActionContext;

public class MinhaAcaoManual implements AcaoRotinaJava {
    
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        ScheduledActionContext ctx = new ScheduledActionContext() {
            @Override
            public void info(String message) {
                contexto.setMensagemRetorno(message);
            }
        };
        
        MinhaAcaoAgendada acaoAgendada = new MinhaAcaoAgendada();
        acaoAgendada.onTime(ctx);
    }
}
```

---

## 🚀 PADRÕES AVANÇADOS DE IMPLEMENTAÇÃO

### Padrão: LogFactory com ExceptionUtils

**Origem**: PetKids/Neogrid

**Implementação**:
```java
public class MeuLogFactory {
    private static final boolean LOGGING_ENABLED = true;
    private static final String LOG_PREFIX = "[MEU_PROJETO]";
    
    public static void logErro(String mensagem, Exception e) {
        if (!LOGGING_ENABLED) return;
        if (e == null) {
            System.err.println(String.format("%s ERRO: %s | Exceção: null", LOG_PREFIX, mensagem));
            return;
        }
        String mensagemErro = ExceptionUtils.getMessage(e);
        if (mensagemErro == null) {
            mensagemErro = e.getClass().getSimpleName();
        }
        System.err.println(String.format("%s ERRO: %s | Exceção: %s", LOG_PREFIX, mensagem, mensagemErro));
        
        if (e.getCause() != null) {
            String causa = ExceptionUtils.getMessage(e.getCause());
            if (causa != null) {
                System.err.println(String.format("%s Causa: %s", LOG_PREFIX, causa));
            }
        }
        
        try {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            if (stackTrace != null && !stackTrace.trim().isEmpty()) {
                System.err.println(String.format("%s Stack Trace:\n%s", LOG_PREFIX, stackTrace));
            }
        } catch (Exception ex) {
            System.err.println(String.format("%s Erro ao obter stack trace: %s", LOG_PREFIX, ex.getMessage()));
        }
    }
    
    public static void logPerformance(String etapa, long tempoMs, String detalhes) {
        if (!PERFORMANCE_LOGGING_ENABLED) return;
        System.out.println(String.format("%s [PERFORMANCE] %s: %d ms | %s", 
            LOG_PREFIX, etapa, tempoMs, detalhes));
    }
}
```

### Padrão: Constants Class Centralizada

**Origem**: PetKids/Neogrid, Denver/TSL

**Implementação**:
```java
public class MeuConstants {
    public static final String LOG_PREFIX = "[MEU_PROJETO]";
    public static final String CNPJ_EMPRESA = "12345678000190";
    public static final int TAMANHO_CNPJ = 14;
    public static final String TIPO_REGISTRO_CABECALHO = "C";
    public static final String TIPO_REGISTRO_DADOS = "D";
    public static final String CHARSET_ANSI = "Windows-1252";
    public static final String LINE_SEPARATOR = "\r\n";
    public static final String FIELD_SEPARATOR = "|";
    private MeuConstants() {}
}
```

---

## 💡 CASOS DE USO COMUNS COM SOLUÇÕES PRONTAS

### Caso 1: Geração de Arquivo para Integração

**Service**:
```java
public class GeracaoArquivoService {
    private final MeuRepository repository = new MeuRepository();
    
    public String gerarArquivo(String caminhoExportacao, BigDecimal nunota) throws Exception {
        if (StringUtils.isEmpty(caminhoExportacao)) {
            throw new IllegalArgumentException("Caminho de exportação não pode ser nulo ou vazio");
        }
        Optional.ofNullable(nunota)
            .orElseThrow(() -> new IllegalArgumentException("NUNOTA não pode ser nulo"));
        
        Set<MeuDTO> dados = repository.buscarDadosPorNunota(nunota);
        if (dados.isEmpty()) {
            throw new Exception("Nenhum dado encontrado para os parâmetros informados.");
        }
        
        String cnpj = dados.stream().findFirst().map(MeuDTO::getCnpj).orElse("");
        Set<String> linhas = dados.stream()
            .map(this::gerarLinha)
            .collect(Collectors.toCollection(() -> new LinkedHashSet<>(1024)));
        
        String caminhoCompleto = caminhoExportacao + File.separator + 
            FileGenerator.gerarNomeArquivo("MEU_PREFIXO", cnpj);
        FileGenerator.gerarArquivo(linhas, caminhoCompleto);
        return caminhoCompleto;
    }
    
    private String gerarLinha(MeuDTO dto) {
        StringBuilder linha = new StringBuilder(200);
        linha.append(Formatter.formatarCnpj(dto.getCnpj()))
            .append("|")
            .append(Formatter.formatarTexto(dto.getCodigoProduto(), 20))
            .append("|")
            .append(Formatter.formatarTexto(dto.getDescricao(), 100))
            .append("|")
            .append(Formatter.formatarValorUnitario(dto.getValorUnitario()))
            .append("|")
            .append(Optional.ofNullable(dto.getDataEmissao())
                .map(Formatter::formatarData)
                .map(String::trim)
                .orElse(""));
        return linha.toString();
    }
}
```

**Projetos de Referência**: PetKids/Neogrid, Denver/TSL

---

## 🤖 GUIA CURSOR IA - USO EFICIENTE MUNDIAL

### Comandos Principais

| Ação | Comando | Descrição |
|------|---------|-----------|
| **Gerar código** | `Cmd/Ctrl + K` | Gera código seguindo padrões do Template |
| **Consultar documentação** | `Cmd/Ctrl + L` | Consulta documentação em linguagem natural |
| **Editar inline** | `Cmd/Ctrl + I` | Refatora código seguindo padrões |
| **Autocompletar** | `Tab` | Completa seguindo padrões do projeto |

### Exemplos de Consultas Eficientes

**Consultas sobre Padrões**:
```
"Como criar um repositório seguindo o padrão AbstractRepository?"
"Qual o template completo para criar um botão de ação?"
"Como implementar uma ação agendada (ScheduledAction)?"
"Como criar um evento programado (EventoProgramavelJava)?"
```

**Consultas sobre Sankhya**:
```
"Como usar EntityFacade para buscar dados no Sankhya?"
"Qual o padrão correto para usar NativeSql com parâmetros nomeados?"
"Como fazer download usando DownloadHelper?"
"Quais são os campos principais da tabela TGFCAB?"
```

**Consultas sobre Integrações**:
```
"Como fazer integração REST com autenticação Basic Auth?"
"Qual o padrão para processamento em lote?"
"Como implementar sistema de logs estruturado?"
```

### Ordem de Consulta Recomendada

1. **Template/INSTRUCOES_DESENVOLVIMENTO.md** - Instruções completas e templates (ESTE ARQUIVO)
2. **Template/REFERENCIA_SANKHYA.md** - Referência completa de tabelas e métodos Sankhya
3. **Template/README.md** - Visão geral e índice rápido
4. **ZDevDoc/** - Documentação completa Sankhya
5. **SatyaPass/** - Exemplos práticos
6. **Projetos Reais** - Referências de implementação

### Troubleshooting

**Problema**: Cursor IA não está seguindo padrões  
**Solução**: Consulte `Template/INSTRUCOES_DESENVOLVIMENTO.md` explicitamente

**Problema**: Código gerado está verboso  
**Solução**: Use `Cmd/Ctrl + I` para refatorar ou solicite explicitamente: "Refatore para usar streams JDK8"

**Problema**: Não encontro informação sobre tabela Sankhya  
**Solução**: Consulte `Template/REFERENCIA_SANKHYA.md` primeiro

---

## ⚡ RESUMO EXECUTIVO - REGRAS DE OURO

### ✅ SEMPRE - EXCELÊNCIA OBRIGATÓRIA

1. **ZERO comentários** - Código 100% autoexplicativo
2. **Menor número de linhas** - Soluções concisas e diretas
3. **JDK8 máximo** - Streams, Optional, lambdas, method references
4. **Use DownloadHelper** para downloads
5. **Estenda AbstractRepository** para repositórios
6. **Use FileGenerator** para arquivos
7. **Use Formatter** para formatação
8. **Valide no início** dos métodos
9. **Use try-with-resources** para recursos
10. **Pré-aloque coleções** (LinkedHashSet<>(1024))
11. **Métodos < 50 linhas** (preferencialmente < 30)
12. **Classes < 300 linhas** (preferencialmente < 200)
13. **AO FINALIZAR: Execute `mvn clean package install` e corrija TODOS os erros até BUILD SUCCESS**

### ❌ NUNCA - ZERO TOLERÂNCIA

1. **Comentários no código** - Código deve ser autoexplicativo
2. **Código verboso** - Prefira sempre soluções concisas com JDK8
3. **Loops tradicionais** quando streams JDK8 são aplicáveis
4. **If-null checks** quando Optional resolve melhor
5. **Ifs desnecessários** - Use Optional, operadores ternários e streams
6. **Campos AD_** sem justificativa documentada
7. **Métodos > 50 linhas** - Dividir imediatamente
8. **Classes > 300 linhas** - Refatorar imediatamente
9. **I/O sem buffers** (sempre 8192 bytes)
10. **Recursos sem try-with-resources**
11. **Download sem DownloadHelper**
12. **Repository sem AbstractRepository**
13. **Formatação manual sem Formatter**
14. **Validações no meio do código**
15. **Código duplicado** - Extrair para métodos privados
16. **Finalizar interação sem BUILD SUCCESS em `mvn clean package install`**
17. **Deixar erros de compilação sem corrigir**

---

## 🎓 REFERÊNCIAS E DOCUMENTAÇÃO

### Arquivos Essenciais do Template

1. **[README.md](README.md)**: Visão geral completa e guia Cursor IA
2. **[REFERENCIA_SANKHYA.md](REFERENCIA_SANKHYA.md)**: Referência completa de tabelas e métodos Sankhya
3. **[CHANGELOG.md](CHANGELOG.md)**: Histórico de melhorias e atualizações

### Base de Conhecimento Externa

- **ZDevDoc**: Documentação completa Sankhya (`/ZDevDoc/`)
- **SatyaPass**: Exemplos práticos (`/SatyaPass/exemplos/`)
- **Projetos Reais**: Denver, PetKids, GuaranaMineiro, Megleo, Eletromac, Iwannasleep

---

**Última Atualização**: 2025-01-02  
**Versão**: 7.0.0 - GUIA MUNDIAL CONSOLIDADO  
**Status**: ✅ CONHECIMENTO MÁXIMO MUNDIAL CONSOLIDADO  
**Baseado em**: ZDevDoc, SatyaPass, Projetos Sankhya Reais (Denver, PetKids, GuaranaMineiro, Megleo, Eletromac, Iwannasleep)  
**Referência Mundial**: Este documento estabelece padrões de excelência máxima para desenvolvimento Sankhya
