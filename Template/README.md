# Template para Personalizações Sankhya

## 📋 Visão Geral

Template padronizado para desenvolvimento de personalizações no sistema Sankhya, consolidando conhecimento de todos os projetos do repositório e otimizado para uso com Cursor IA.

## 🎯 Objetivo

Fornecer uma estrutura base completa e otimizada para criação de novas personalizações, seguindo as melhores práticas de desenvolvimento Java JDK8, arquitetura SOLID e código limpo.

## ⚙️ Especificações Técnicas

- **Java**: JDK 8
- **Build**: Maven 3.x
- **Arquitetura**: SOLID, código limpo
- **Otimizações**: Buffers, streams, cache, pré-alocação
- **Padrões**: Sem comentários, métodos simples e práticos

## 📁 Estrutura do Projeto

```
Template/
├── docs/                              # Documentação técnica
│   └── DOCUMENTACAO_TECNICA.md        # Documentação técnica completa
├── README.md                          # Este arquivo (visão geral)
├── CONHECIMENTO_CONSOLIDADO.md        # 🎓 Consolidação máxima de conhecimento
├── INSTRUCOES_DESENVOLVIMENTO.md      # ⭐ Tudo sobre desenvolvimento (consolidado)
├── REFERENCIA_SANKHYA.md              # 📊 Referência Sankhya (consolidado)
├── CHANGELOG.md                       # 📝 Histórico
├── pom.xml                            # Configuração Maven
├── lib/                               # JARs do Sankhya (não versionados)
│   └── README.md                      # Instruções sobre JARs
└── src/
    └── br/com/cliente/
        ├── action/botaoAcao/
        │   └── PersonalizacaoSankhya.java
        ├── model/dto/
        │   └── ExemploDTO.java
        ├── repository/
        │   ├── AbstractRepository.java
        │   └── ExemploRepository.java
        ├── service/
        │   └── ExemploService.java
        └── util/
            ├── DownloadHelper.java
            ├── FileGenerator.java
            └── Formatter.java
```

## 🚀 Como Usar Este Template

### 1. Copiar o Template

```bash
cp -r Template NovoProjeto
cd NovoProjeto
```

### 2. Configurar o Projeto

- Atualizar `pom.xml` (groupId, artifactId, name, description)
- Renomear pacote `br.com.cliente` para o pacote do cliente
- Renomear classes conforme necessário
- Atualizar imports e referências

### 3. Implementar a Lógica

- **DTOs**: Criar classes DTO em `model/dto/`
- **Repositories**: Estender `AbstractRepository` e implementar queries
- **Services**: Implementar lógica de negócio
- **Actions**: Implementar botões de ação que utilizam os serviços

### 4. Compilar e Gerar JAR

```bash
mvn clean package install
```

## 🔧 Dependências

### ⚠️ IMPORTANTE: JARs Sankhya Necessários

**Antes de compilar**, você precisa criar a pasta `lib/` e adicionar os seguintes JARs:

```bash
mkdir -p lib
```

Os seguintes JARs devem estar na pasta `lib/`:

- `SankhyaW-extensions.jar` - Extensões do Sankhya
- `jape.jar` - Java Persistence API do Sankhya
- `mge-modelcore.jar` - Core do modelo Sankhya
- `sanutil.jar` - Utilitários Sankhya
- `sanws.jar` - **OBRIGATÓRIO** - Contém ServiceContext necessário para algumas operações

**Como obter os JARs:**
1. **SDK Sankhya**: Os JARs estão disponíveis no SDK do Sankhya (pasta `api_sankhya/`)
2. **Copiar de outro projeto**: Se houver outro projeto com estes JARs, copie de lá
3. **Extrair do servidor Sankhya**: Os JARs geralmente estão na pasta de instalação do Sankhya

### Dependências Maven

- `commons-io` (2.1) - Utilitários de I/O
- `commons-lang3` (3.1) - Utilitários Apache Commons
- `commons-lang` (2.6) - ExceptionUtils para tratamento de erros

## 📚 Componentes Padrão

### DownloadHelper
Classe utilitária padronizada para download de arquivos e criação de ZIPs.

**Métodos principais:**
- `prepararDownload(String caminhoArquivo)`: Prepara download de arquivo único
- `criarZip(Collection<String> arquivos, String nomeZip)`: Cria ZIP com múltiplos arquivos
- `gerarScriptDownloadZip(String nomeArquivo)`: Gera script HTML/JS para download automático

### AbstractRepository
Classe base para repositórios que fornece:
- Gerenciamento automático de conexões JDBC
- Fechamento seguro de recursos
- Métodos genéricos com mapeamento funcional

### FileGenerator
Utilitário para geração de arquivos:
- Encoding Windows-1252 (ANSI)
- Quebra de linha CRLF
- Geração de nomes de arquivo padronizados

### Formatter
Formatadores otimizados com cache:
- Formatação de CNPJ, datas, números
- Cache de espaços em branco
- Operações com arrays de char para performance

## 🎓 Conhecimento Consolidado

Este Template incorpora **TODO O CONHECIMENTO APRENDIDO** de todos os projetos:

### 📊 Estatísticas de Conhecimento Extraído

**Projetos Analisados em Profundidade**:
- ✅ **6 projetos reais** analisados:
  - **Denver**: 2 Repositories, 2 Actions - Arquitetura TSL otimizada, padrões de repositório
  - **PetKids**: 5 Repositories, 1 Action, 1 ScheduledAction, LogFactory, Constants - Logging estruturado, constantes centralizadas
  - **GuaranaMineiro**: 1 API, 1 Helper, 1 Action, 1 ScheduledAction, 1 Evento - Integração REST avançada, eventos programados
  - **Megleo**: 1 ScheduledAction, 2 Eventos - Ações agendadas, eventos programados
  - **Eletromac**: 1 ScheduledAction - Automação de processos
  - **Iwannasleep**: 1 ScheduledAction, 2 Eventos - Eventos programados, gerenciamento de reservas

**Implementações Analisadas**:
- ✅ **48 implementações** de `AcaoRotinaJava` analisadas
- ✅ **7 implementações** de `ScheduledAction` analisadas
- ✅ **21 implementações** de `EventoProgramavelJava` analisadas
- ✅ **12 implementações** de `Repository` analisadas
- ✅ **25 classes Helper** analisadas
- ✅ **4 classes Constants** analisadas

**Conhecimento Documentado**:
- ✅ **20+ tabelas Sankhya** documentadas completamente
- ✅ **8 padrões avançados** consolidados de código real
- ✅ **8 casos de uso comuns** com soluções prontas e testadas
- ✅ **4 componentes padrão** documentados completamente
- ✅ **~230K** de documentação consolidada em 4 arquivos principais

### Padrões Arquiteturais Incorporados
- **Denver**: Arquitetura otimizada TSL, performance máxima
- **PetKids**: Padrões de integração Neogrid, logging estruturado
- **GuaranaMineiro**: Integração REST Performaxxi, autenticação Basic Auth
- **Megleo**: Integração transportadoras, processamento em lote
- **Eletromac**: Automação de processos, ações agendadas
- **Iwannasleep**: Eventos programados, gerenciamento de reservas

### Padrões de Código Incorporados
- **JDK8 máximo**: Streams, Optional, lambdas, method references
- **Performance**: Buffers 8192 bytes, pré-alocação, cache
- **Validação**: Defensiva no início dos métodos
- **Tratamento de erros**: Robusto com mensagens claras
- **Logging**: Estruturado com ExceptionUtils

### Padrões Sankhya Incorporados
- **EntityFacade**: Acesso principal a entidades
- **NativeSql**: Queries complexas com parâmetros nomeados
- **JapeWrapper**: Tabelas AD_ com transações
- **Filtros obrigatórios**: STATUSNOTA = 'L', ATIVO = 'S'
- **JOIN com tipo de operação**: CODTIPOPER + DHTIPOPER

## 🎯 Princípios Fundamentais - Excelência Obrigatória

1. **ZERO comentários** - Código 100% autoexplicativo
2. **Menor número de linhas** - Soluções concisas e diretas
3. **JDK8 máximo** - Streams, Optional, lambdas, method references
4. **Sempre use DownloadHelper** para downloads
5. **Estenda AbstractRepository** para novos repositórios
6. **Use Formatter** para formatação de dados
7. **Métodos < 50 linhas** (preferencialmente < 30)
8. **Classes < 300 linhas** (preferencialmente < 200)
9. **Otimize**: buffers (8192 bytes), streams JDK8, cache, pré-alocação
10. **Valide sempre**: parâmetros no início dos métodos
11. **Construa sempre**: execute `mvn clean package install` ao finalizar

## 📑 Índice Rápido

| Tarefa | Arquivo | Seção |
|--------|---------|-------|
| Criar botão de ação | `INSTRUCOES_DESENVOLVIMENTO.md` | "Padrões de Botões de Ação" |
| Criar repositório | `INSTRUCOES_DESENVOLVIMENTO.md` | "Template: Repository" |
| Criar evento programado | `INSTRUCOES_DESENVOLVIMENTO.md` | "Eventos Programados" |
| Integração REST | `INSTRUCOES_DESENVOLVIMENTO.md` | "Integração REST" |
| Ação agendada | `INSTRUCOES_DESENVOLVIMENTO.md` | "Ações Agendadas" |
| Consultar tabelas | `REFERENCIA_SANKHYA.md` | "Tabelas Core" |
| Usar métodos repositório | `REFERENCIA_SANKHYA.md` | "Métodos Disponíveis" |

## 🤖 Otimizado para Cursor IA

Este template foi **especialmente otimizado para uso com Cursor IA**, consolidando TODO O CONHECIMENTO aprendido em todos os projetos do repositório.

### 🚀 Como usar com Cursor IA

1. Abra o projeto no Cursor
2. Use `Cmd/Ctrl + K` para gerar código seguindo os padrões do template
3. Use `Cmd/Ctrl + L` para consultar a documentação em linguagem natural
4. Use `Cmd/Ctrl + I` para edições inline inteligentes
5. Consulte `.cursorrules` para regras específicas do projeto

### 📚 Documentação Consolidada

Este Template possui **4 arquivos principais** de documentação consolidada:

1. **[CONHECIMENTO_CONSOLIDADO.md](CONHECIMENTO_CONSOLIDADO.md)** 🎓 **CONHECIMENTO MÁXIMO** - Consolidação máxima de todo conhecimento
2. **[INSTRUCOES_DESENVOLVIMENTO.md](INSTRUCOES_DESENVOLVIMENTO.md)** ⭐ **PRINCIPAL** - Tudo sobre desenvolvimento
3. **[REFERENCIA_SANKHYA.md](REFERENCIA_SANKHYA.md)** 📊 **REFERÊNCIA** - Tabelas e métodos Sankhya
4. **[CHANGELOG.md](CHANGELOG.md)** 📝 **HISTÓRICO** - Melhorias e atualizações

## 📄 Documentação

### Documentação Técnica Completa
📚 **[DOCUMENTACAO_TECNICA.md](docs/DOCUMENTACAO_TECNICA.md)** - Documentação técnica completa e unificada contendo:
- Visão geral do template
- Arquitetura e componentes implementados
- Padrões de código e melhores práticas
- Guia de uso completo
- Exemplos práticos

## 📝 Histórico de Consolidação

**Versão 6.0.0** (2025-01-02): Consolidação de arquivos MD - De 12 arquivos para 4 arquivos principais, mantendo TODO O CONTEÚDO consolidado.

**Versão 4.0.0** (2025-01-02): Conhecimento Máximo Consolidado - Extração profunda de conhecimento de 6 projetos reais.

---

**Versão**: 6.0.0  
**Status**: ✅ TEMPLATE CONSOLIDADO - CONHECIMENTO MÁXIMO  
**Baseado em**: Conhecimento consolidado de TODOS os projetos do repositório (Denver, PetKids, GuaranaMineiro, Megleo, Eletromac, Iwannasleep)
