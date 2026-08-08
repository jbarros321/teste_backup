# Pet Kids - Integração Neogrid

## 📋 Visão Geral

Este projeto implementa a integração entre o sistema Sankhya e a plataforma Neogrid, gerando arquivos de exportação no formato especificado pela Neogrid para sistemas de informações gerenciais para distribuidores.

## 🎯 Objetivo

Gerar arquivos de extração de dados no formato Neogrid para os seguintes relatórios:
- **Vendedores** (Layout v5.0)
- **Clientes** (Layout v5.0.4)
- **Produtos** (Layout v5.1)
- **Vendas** (Layout v5.2)
- **Estoque** (Layout v5.0)

## 📊 Especificações Técnicas

### Formato dos Arquivos

- **Formato**: Flat File com separadores PIPE (|)
- **Encoding**: ANSI
- **Quebra de linha**: PC/Windows (CRLF)
- **Critério**: Delimitado para identificar as colunas

### Tipos de Arquivos

#### Arquivos de Cadastro
Os arquivos de cadastro (Vendedores, Clientes e Produtos) devem conter **todos os dados da filial**, independente das indústrias envolvidas nos projetos.

#### Arquivos de Movimentos
Os arquivos de movimentos (Vendas e Estoque) devem ser gerados **um arquivo para cada indústria**.

### Nomenclatura dos Arquivos

Sugestão de formato:
```
MascaraDocumento_CNPJFilial_CNPJIndustria_AAAAMMDDHHMMSS.txt
```

Onde:
- **MascaraDocumento**: Identificação do tipo de documento
  - `RELVEN` - Relatório de Vendedores
  - `RELCLI` - Relatório de Clientes
  - `RELPRO` - Relatório de Produtos
  - `VENDAS` - Relatório de Vendas
  - `RELEST` - Relatório de Estoque
- **CNPJFilial**: CNPJ da filial do distribuidor (14 dígitos)
- **CNPJIndustria**: CNPJ da indústria (14 dígitos) - apenas para Vendas e Estoque
- **AAAAMMDDHHMMSS**: Data e hora da criação do arquivo

### Regras de Dados

#### Dados Numéricos
- Campos não obrigatórios sem dados devem existir separados por PIPE, mas vazios
- Valores decimais devem usar ponto (.) como separador
- Exemplo: R$125,00 → `125.00`
- Valores devem ser representados como positivos (sem sinal "-" ou "+")

#### Dados Alfanuméricos
- **Não utilizar** caracteres acentuados (Ç, Á, Ê, etc.)
- **Não utilizar** caracteres especiais (\, @, #, etc.)
- Campos não obrigatórios sem dados devem existir separados por PIPE, mas vazios

### Frequência de Envio

- **Cadastros**: Podem ser enviados diariamente com dados de inclusão ou atualização
- **Movimentos**: Devem ser transmitidos diariamente
- **Agendamento**: O sistema deve permitir agendamento automático da geração

## 📁 Estrutura do Projeto

```
PetKids/
├── docs/                          # Documentação
│   ├── README.md                  # Este arquivo
│   └── layouts/                   # PDFs dos layouts Neogrid
├── src/
│   ├── br/com/petkids/neogrid/
│   │   ├── action/                # Botões de ação
│   │   ├── service/                # Serviços de geração
│   │   ├── model/                  # Modelos de dados
│   │   └── util/                   # Utilitários
│   └── main/sql/                  # Scripts SQL
├── lib/                           # Dependências locais
└── pom.xml                        # Configuração Maven
```

## 🔧 Componentes Principais

### 1. Geração de Relatórios

Cada tipo de relatório terá uma classe de serviço específica:
- `VendedoresService` - Geração do arquivo de vendedores
- `ClientesService` - Geração do arquivo de clientes
- `ProdutosService` - Geração do arquivo de produtos
- `VendasService` - Geração do arquivo de vendas
- `EstoqueService` - Geração do arquivo de estoque

### 2. Formatação de Dados

- `NeogridFormatter` - Utilitário para formatação de dados conforme padrão Neogrid
- Remoção de acentos e caracteres especiais
- Formatação de números decimais
- Formatação de datas

### 3. Geração de Arquivos

- `FileGenerator` - Classe base para geração de arquivos
- Encoding ANSI
- Separador PIPE
- Quebra de linha Windows

## 📝 Layouts dos Relatórios

### Registro de Cabeçalho (Todos os Relatórios)

Todos os arquivos começam com um registro de cabeçalho contendo:
- Tipo de Registro
- Identificação
- Versão
- Número do Relatório
- Data/Hora de Emissão
- CNPJ do Emissor

### Relatório de Vendedores

**Registro 02 - VENDEDORES** (de uma a N ocorrências)
- Código do Vendedor
- Nome do Vendedor
- CPF/CNPJ
- E-mail
- Telefone
- Status (Ativo/Inativo)

### Relatório de Clientes

**Registro 02 - CLIENTES** (de uma a N ocorrências)
- Código do Cliente
- Razão Social
- Nome Fantasia
- CNPJ/CPF
- Inscrição Estadual
- Endereço completo
- CEP
- Telefone
- E-mail
- Status

### Relatório de Produtos

**Registro 02 - PRODUTOS** (de uma a N ocorrências)
- Código do Produto
- Descrição
- Código de Barras
- Unidade de Medida
- Preço de Venda
- Status

### Relatório de Vendas

**Registro 02 - NOTAS FISCAIS** (de uma a N ocorrências)
- Tipo de Registro
- Tipo de Faturamento
- Número NF
- Série NF
- Tipo NF
- Data de Emissão
- CNPJ/CPF do Cliente
- Código do Cliente
- Código do Vendedor
- Condição de Entrega (tipo de frete)
- Valor Total da NF

**Registro 03 - ITENS** (de uma a N ocorrências)
- Tipo de Registro
- Número NF
- Série NF
- Tipo NF
- Código do Produto
- Quantidade
- Valor Unitário
- Valor Total do Item
- Desconto

### Relatório de Estoque

**Registro 02 - ESTOQUE** (de uma a N ocorrências)
- Código do Produto
- Quantidade em Estoque
- Data de Movimentação
- Tipo de Movimento

## 🚀 Como Usar

### Pré-requisitos

- Java 8+
- Maven 3.6+
- Sankhya Framework
- Dependências Sankhya (JARs locais)

### Configuração das Dependências Sankhya

Este projeto requer JARs do Sankhya que não estão disponíveis no Maven Central. Os arquivos necessários devem estar no diretório `lib/`:

- `SankhyaW-extensions.jar`
- `jape.jar`
- `mge-modelcore.jar`
- `sanutil.jar`

#### Opção 1: Usar JARs Reais (Recomendado)

**Método Automático (Recomendado):**

Execute o script helper que localiza e copia os JARs automaticamente:

```bash
./copy-sankhya-jars.sh
```

**Método Manual:**

Se você sabe onde estão os JARs, copie-os manualmente:

```bash
# Exemplo (ajuste o caminho conforme sua instalação)
cp /caminho/para/sankhya/lib/SankhyaW-extensions.jar lib/
cp /caminho/para/sankhya/lib/jape.jar lib/
cp /caminho/para/sankhya/lib/mge-modelcore.jar lib/
cp /caminho/para/sankhya/lib/sanutil.jar lib/
```

#### Opção 2: Criar Placeholders Temporários

Se você precisa apenas validar a estrutura do projeto sem compilar:

```bash
./create-lib-placeholders.sh
```

⚠️ **Atenção**: Os placeholders permitem que o Maven resolva as dependências, mas a compilação falhará porque as classes Sankhya não estarão disponíveis. Use apenas para validação da estrutura do projeto.

Consulte `lib/README.md` para mais informações.

### Compilação

```bash
mvn clean compile
```

### Build

```bash
mvn clean package
```

### Instalação

1. Copiar o JAR gerado para o diretório de extensões do Sankhya
2. Configurar os parâmetros de geração (filial, indústria, etc.)
3. Executar via botão de ação ou evento programado

## 📌 Observações Importantes

1. **Atualização de Dados**: Informações serão substituídas, sendo considerada somente a última atualização no portal Neogrid
2. **Notas Fiscais**: Para atualizar uma nota já enviada, a mesma deve ser reenviada com a atualização
3. **Cancelamento de NF**: Notas de cancelamento devem referenciar a nota originária
4. **Estoque Zero**: Produtos com estoque zero e sem movimento no dia também devem ser enviados
5. **Intervalo de Datas**: Arquivos de movimentos podem conter dados de um intervalo de datas

## 📄 Documentação

### Documentação Técnica Completa
📚 **[DOCUMENTACAO_TECNICA.md](docs/DOCUMENTACAO_TECNICA.md)** - Documentação técnica completa e unificada contendo:
- Visão geral do projeto
- Especificação completa de campos
- Arquitetura e componentes implementados
- Melhorias implementadas (ExceptionUtils, Constantes, Logging, JavaDoc)
- Utilização de utilitários Sankhya
- Guia de uso completo
- Padrões aplicados (Repository, DTO, Enum, Exception, Validation, Logging)
- Próximos passos e checklist de validação

### Layouts de Referência
Os layouts completos estão disponíveis nos arquivos PDF em `docs/layouts/`:
- `LayoutRelatorioVendedoresDI_Neogrid_v5.0.pdf`
- `LayoutRelatorioClientesDI_Neogrid_v5.0.4.pdf`
- `LayoutRelatorioProdutosDI_Neogrid_v5.1.pdf`
- `LayoutRelatorioVendasDI_Neogrid_v5.2.pdf`
- `LayoutRelatorioEstoqueDI_Neogrid_v5.0.pdf`

## 👥 Informações do Projeto

- **Cliente**: Pet Kids (KELCO PET CARE PRODUTOS ANIMAIS LTDA)
- **CNPJ**: 07.056.359/0001-20
- **Solicitação**: 1558
- **Data de Abertura**: 17/10/2025
- **Responsável Técnico**: A definir
- **Horas Estimadas**: 44 horas

## 📞 Suporte

Para dúvidas ou problemas, consulte a documentação técnica ou entre em contato com a equipe de desenvolvimento.

