# Consolidar Itens de Nota - P&D

## 📋 Descrição

Botão de ação para consolidar múltiplos itens de uma nota em um único item de serviço quando a TOP está configurada com `AD_AGRUPATDITENS = 'S'`.

## 🎯 Objetivo

Cria uma nova nota consolidando múltiplos itens da nota de origem em um único item de serviço, preservando todos os dados da nota original e aplicando as regras de negócio do Sankhya através das APIs nativas.

## 🏗️ Estrutura do Projeto

```
P&D/
├── src/
│   └── br/com/pd/action/botaoAcao/
│       └── ConsolidarItensNota.java
├── lib/                          # Dependências Sankhya (criar manualmente)
│   ├── SankhyaW-extensions.jar
│   ├── jape.jar
│   ├── mge-modelcore.jar
│   └── sanutil.jar
├── pom.xml                       # Configuração Maven
├── README.md                     # Este arquivo
└── STP_CONSOLIDAR_ITENS_NOTA.SQL # Stored procedure original (referência)
```

## 📦 Dependências

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
- `sanws.jar` - **OBRIGATÓRIO** - Contém ServiceContext necessário para CACHelper.incluirAlterarCabecalho()
- `sanhttp.jar` - OPCIONAL - Funcionalidades HTTP adicionais

**⚠️ IMPORTANTE**: O JAR `sanws.jar` é **obrigatório** para compilação. 
Sem ele, o projeto não compila devido à dependência de `ServiceContext`.

**Como obter os JARs:**
1. **SDK Sankhya**: Os JARs estão disponíveis no SDK do Sankhya (pasta `api_sankhya/`)
2. **Copiar de outro projeto**: Se houver outro projeto com estes JARs, copie de lá
3. **Extrair do servidor Sankhya**: Os JARs geralmente estão na pasta de instalação do Sankhya

**Onde obter os JARs:**
- Os JARs estão disponíveis no SDK do Sankhya
- Ou copie de outro projeto que já tenha compilado (ex: PetKids, Template)
- Ou extraia do servidor Sankhya (pasta de instalação)

### Dependências Maven

- `commons-io` (2.1) - Utilitários de I/O
- `commons-lang3` (3.1) - Utilitários Apache Commons
- `commons-lang` (2.6) - ExceptionUtils para tratamento de erros

## 🔧 Compilação

### Pré-requisitos

- Java JDK 8
- Maven 3.6+
- JARs Sankhya na pasta `lib/`

### Comandos

```bash
# Compilar o projeto
mvn clean compile

# Gerar JAR
mvn clean package

# O JAR será gerado em:
# target/consolidar-itens-nota-1.0.0.jar
```

## 🚀 Instalação no Sankhya

1. **Copiar JAR**: Copie o JAR gerado para o servidor Sankhya
2. **Upload no Módulo Java**: Faça upload do JAR através do módulo Java do Sankhya
3. **Configurar Botão de Ação**: 
   - No Construtor de Telas ou Dicionário de Dados
   - Tipo: Rotina Java
   - Classe: `br.com.pd.action.botaoAcao.ConsolidarItensNota`
   - Parâmetros:
     - `CODTIPOPER_DEST`: Código da TOP de destino (obrigatório)
     - `SERIENOTA`: Série da nota (opcional)

## 📝 Parâmetros

### Parâmetros do Contexto

- **CODTIPOPER_DEST** (obrigatório): Código da TOP de destino que deve estar configurada com:
  - `AD_AGRUPATDITENS = 'S'`
  - `AD_SERVEMPREITADA` preenchido com o código do produto de serviço

- **SERIENOTA** (opcional): Série da nota a ser utilizada na nova nota

### Campos das Linhas Selecionadas

- **NUNOTA**: Número da nota de origem (obrigatório)
- **CODEMP**: Código da empresa (opcional, usa da nota origem se não informado)
- **CODPARC**: Código do parceiro (opcional, usa da nota origem se não informado)

## ⚙️ Funcionamento

1. **Validação**: Verifica se a TOP de destino está configurada corretamente
2. **Busca Dados**: Obtém todos os dados da nota de origem
3. **Cálculo de Totais**: Calcula totais de mão de obra e material
4. **Criação de Nota**: Cria nova nota usando APIs nativas (CACHelper)
5. **Item Consolidado**: Cria item único com observação da composição
6. **Vínculo**: Cria vínculo TGFVAR entre nota nova e origem
7. **Atualização**: Marca nota origem como não pendente

## 🔍 Características Técnicas

### APIs Nativas Utilizadas

- **CACHelper**: Criação de cabeçalho e itens com regras de negócio
- **PrePersistEntityState**: Preparação de entidades para persistência
- **TipoOperacaoUtils**: Obtenção de dados da TOP
- **CentralItemNota**: Inicialização de produtos com preços e custos
- **EntityFacade**: Operações CRUD nativas

### Benefícios

✅ **Regras de Negócio**: Todas as regras são aplicadas automaticamente  
✅ **Validações**: Validações do sistema são respeitadas  
✅ **Triggers**: Triggers e eventos são disparados corretamente  
✅ **Cálculos**: Cálculos automáticos de preços, custos e impostos  
✅ **Integração**: Integração com outros módulos (financeiro, estoque, etc.)  
✅ **Manutenibilidade**: Código mais fácil de manter e compatível com atualizações  

## 📊 Cálculo de Percentuais

O sistema calcula automaticamente os percentuais de mão de obra e material:

- **Mão de Obra**: Itens com `USOPROD = 'S'`
- **Material**: Outros itens
- **Percentuais**: Calculados com 2 casas decimais
- **Observação**: Incluída no item consolidado

## ⚠️ Validações

- TOP de destino deve existir
- TOP deve ter `AD_AGRUPATDITENS = 'S'`
- TOP deve ter `AD_SERVEMPREITADA` preenchido
- Nota de origem deve existir
- Produto de serviço deve existir

## 🐛 Tratamento de Erros

- Erros são capturados por nota individual
- Processamento continua mesmo com erros em algumas notas
- Mensagem final mostra quantidade de processadas e erros
- Detalhes dos erros são registrados no log

## 📚 Documentação Adicional

- Ver `STP_CONSOLIDAR_ITENS_NOTA.SQL` para referência da lógica original
- Documentação técnica completa em `docs/Documentacao_Tecnica.md`

## 🔄 Versionamento

- **v1.0.0**: Versão inicial usando APIs nativas do Sankhya

## 👥 Autor

P&D - Pesquisa e Desenvolvimento

## 📄 Licença

Uso interno - Personalização Sankhya

