# Integração TSL - Denver

## 📋 Visão Geral

Personalização para integração entre o sistema Sankhya e o **Total Service Logística (TSL)**, gerando arquivos de exportação conforme manual de interfaces padrões WMS versão 1.2.

## 🎯 Objetivo

Exportar arquivos TXT do Sankhya na estrutura proposta no manual de integração da Total Service, contemplando a movimentação dos produtos e campos essenciais como produto, peso líquido, peso bruto, data de fabricação e data de validade.

## 📦 Interfaces Implementadas

### REC_IN - Recebimento de Mercadorias Integrado
- **Tamanho da linha**: 619 caracteres
- **Campos**: CNPJ, Nota Fiscal, Item NF, Número Palete, Código Produto, Identificador Caixa, Peso Caixa, Data Produção, Data Vencimento, Lote, Info Complementar, Valor Unitário

### PED_IN - Expedição de Mercadorias
- **Tamanho da linha**: 232 caracteres
- **Campos**: CNPJ, Ordem Frete, Número Pedido, Item Pedido, Código Produto, Número Palete, Quantidade, Peso, Data Fabricação (DE/ATÉ), Lote, CNPJ Cliente

## ⚙️ Especificações Técnicas

- **Formato**: Arquivo texto posicional (campos fixos)
- **Encoding**: ANSI (Windows-1252)
- **Quebra de linha**: PC/Windows (CRLF)
- **Separador de campos**: Espaço em branco ao final de cada campo

## 📁 Estrutura do Projeto

```
Denver/
├── Docs/                                  # Documentos de requisitos
├── docs/
│   └── DOCUMENTACAO_TECNICA.md           # Documentação técnica completa
├── lib/                                   # JARs do Sankhya
├── pom.xml
├── README.md                              # Este arquivo
├── REFATORACAO_PERFORMANCE.md            # Otimizações aplicadas
├── REVISAO_QUERIES.md                    # Revisão das queries
└── src/
    └── br/com/denver/tsl/
        ├── action/botaoAcao/
        │   └── GerarArquivoTSL.java      # Botão de ação
        ├── model/dto/
        │   ├── RecebimentoDTO.java       # DTO recebimento
        │   └── ExpedicaoDTO.java         # DTO expedição
        ├── repository/
        │   ├── AbstractTSLRepository.java
        │   ├── RecebimentoRepository.java
        │   └── ExpedicaoRepository.java
        ├── service/
        │   └── TSLService.java           # Serviço principal
        └── util/
            ├── FileGenerator.java
            ├── TSLConstants.java
            └── TSLFormatter.java
```

## 🚀 Instalação

### 1. Compilar o Projeto

```bash
cd Denver
mvn clean package
```

### 2. Instalar JAR no Sankhya

Copiar o JAR gerado (`target/integracao-tsl-1.0.0.jar`) para o diretório de extensões do Sankhya.

### 3. Criar Campos Adicionais na Tabela TGFPRO

Antes de usar o sistema, é necessário criar os campos adicionais na tabela `TGFPRO`:

```sql
-- Campo obrigatório: Flag de integração
ALTER TABLE TGFPRO ADD AD_INTEGTOTALLOGISTICA CHAR(1);
COMMENT ON COLUMN TGFPRO.AD_INTEGTOTALLOGISTICA IS 'Flag para integração TSL: S=Exporta, N/NULL=Não exporta';

-- Campo opcional: Número do palete
ALTER TABLE TGFPRO ADD AD_NUMEROPALETE VARCHAR2(26);
COMMENT ON COLUMN TGFPRO.AD_NUMEROPALETE IS 'Número identificador do palete para exportação TSL';

-- Campo opcional: Identificador da caixa (apenas REC_IN)
ALTER TABLE TGFPRO ADD AD_IDENTIFICADORCAIXA VARCHAR2(31);
COMMENT ON COLUMN TGFPRO.AD_IDENTIFICADORCAIXA IS 'Identificador da caixa para exportação TSL REC_IN';
```

**⚠️ Importante**: 
- Marcar produtos para exportação: `UPDATE TGFPRO SET AD_INTEGTOTALLOGISTICA = 'S' WHERE CODPROD = [código]`
- Produtos sem `AD_INTEGTOTALLOGISTICA = 'S'` não serão exportados

### 4. Configurar Botão de Ação

No Sankhya, configurar um botão de ação:

- **Classe**: `br.com.denver.tsl.action.botaoAcao.GerarArquivoTSL`
- **Método**: `doAction`
- **Parâmetros do Contexto** (obtidos automaticamente da linha selecionada):
  - `NUNOTA`: Número da nota fiscal (obrigatório)
  - `CODPARC`: Código do parceiro/fornecedor (obrigatório para recebimentos)

**Observações**:
- Tipo de interface: fixo em "TODOS" (gera REC_IN e PED_IN)
- Caminho de exportação: fixo no diretório temporário do sistema
- LOTE é obtido automaticamente da tabela `TGFITE` usando `NUNOTA`
- Sistema suporta múltiplos lotes por nota (gera arquivo separado para cada lote)
- Apenas produtos com `AD_INTEGTOTALLOGISTICA = 'S'` serão exportados

## 📖 Uso

### Via Botão de Ação

1. Acessar a tela onde o botão foi configurado (ex: tela de notas fiscais)
2. Selecionar uma linha com `NUNOTA` e `CODPARC` preenchidos
3. Clicar no botão "Gerar Arquivo TSL"
4. Aguardar a geração dos arquivos
5. Verificar mensagem de sucesso com localização dos arquivos e link para download do ZIP

### Nomenclatura dos Arquivos

Formato: `{INTERFACE}_{CNPJ}_{TIMESTAMP}.txt`

**Exemplo:**
- `REC_IN_12345678000190_20250101120000.txt`
- `PED_IN_12345678000190_20250101120000.txt`

## 🔧 Dependências

- **Java JDK 8**
- **Maven 3.x**
- **Sankhya JARs** (localizados em `lib/`):
  - `SankhyaW-extensions.jar`
  - `jape.jar`
  - `mge-modelcore.jar`
  - `sanutil.jar`

## ⚡ Otimizações de Performance

O projeto foi desenvolvido com foco em **alta performance e eficiência máxima**:

### Cache e Memória
- ✅ Cache de espaços em branco (`ConcurrentHashMap`)
- ✅ Cache de arrays de caracteres
- ✅ ThreadLocal para formatadores de data
- ✅ Pré-alocação de listas (capacidade inicial: 1024)

### Manipulação de Strings
- ✅ Arrays de char diretos para operações
- ✅ StringBuilder pré-dimensionado
- ✅ Operações otimizadas (`getChars()`, `Arrays.fill()`)

### I/O Otimizado
- ✅ Buffer de 32KB para escrita
- ✅ Encoding Windows-1252
- ✅ Escrita direta de arrays

### Banco de Dados
- ✅ Queries otimizadas com JOINs adequados
- ✅ Native SQL do Sankhya
- ✅ Gestão adequada de recursos

### Recursos JDK8
- ✅ Lambda expressions
- ✅ Method references
- ✅ Functional interfaces
- ✅ ThreadLocal com lambdas

**📊 Resultado**: ~518 linhas de código otimizado, máxima performance para JDK8.

## 📚 Documentação

- **[Documentação Técnica Completa](docs/DOCUMENTACAO_TECNICA.md)**: Documentação detalhada do projeto
- **[Documentação de Campos SELECT e TXT](docs/DOCUMENTACAO_CAMPOS_SELECT_TXT.md)**: Documentação detalhada dos campos do SELECT e estrutura do arquivo TXT gerado
- **[Refatoração de Performance](REFATORACAO_PERFORMANCE.md)**: Detalhes das otimizações
- **[Revisão de Queries](REVISAO_QUERIES.md)**: Análise das queries SQL
- **[Revisão Completa](REVISAO_COMPLETA.md)**: Revisão completa do projeto e código
- **[Revisão Completa do Projeto](REVISAO_COMPLETA_PROJETO.md)**: Revisão completa e validação final

## ⚠️ Observações Importantes

### Requisitos de Configuração

- ✅ **Campos Adicionais Obrigatórios**: É necessário criar os seguintes campos adicionais na tabela `TGFPRO`:
  - `AD_INTEGTOTALLOGISTICA` (CHAR(1)): 'S' para exportar, 'N' ou NULL para não exportar
  - `AD_NUMEROPALETE` (VARCHAR2): Número do palete (opcional)
  - `AD_IDENTIFICADORCAIXA` (VARCHAR2): Identificador da caixa (opcional, apenas REC_IN)

### Funcionalidades

- ✅ Os arquivos são gerados no formato posicional conforme especificação TSL
- ✅ Campos numéricos utilizam vírgula como separador decimal
- ✅ Datas são formatadas como `dd/MM/yyyy`
- ✅ Campos vazios são preenchidos com espaços em branco
- ✅ Sistema otimizado para processar grandes volumes de dados
- ✅ Apenas produtos ativos são processados (`PRO.ATIVO = 'S'`)
- ✅ Apenas produtos marcados para integração são exportados (`AD_INTEGTOTALLOGISTICA = 'S'`)
- ✅ Apenas notas liberadas são processadas (`STATUSNOTA = 'L'`)

### Tratamento de Valores Nulos

- ✅ **Código do Produto**: Se `REFERENCIA` for NULL, usa `CODPROD` com zeros à esquerda (13 dígitos)
- ✅ **Peso**: Se `PESOLIQ` for NULL, usa `0`
- ✅ **Valor Unitário**: Se `VLRUNIT` for NULL, usa `0`
- ✅ **CNPJ Cliente**: Se `CGC_CPF` for NULL, preenche com espaços

## 📋 Estatísticas do Projeto

- **Total de linhas**: 518
- **Arquivos Java**: 10
- **Interfaces implementadas**: 2 (REC_IN, PED_IN)
- **Build**: ✅ Sucesso
- **Performance**: ⚡ Máxima para JDK8

## 🔗 Referências

- Manual TSL: Interfaces Padrões WMS - TSL V1.2
- Documento de Requisitos: Documento - Plano de requisitos (Denver)
- Documentação Sankhya: Documentação oficial

---

**Versão**: 1.1.0  
**Status**: ✅ CONCLUÍDO  
**Última Atualização**: 2025-12-02

### Mudanças na Versão 1.1.0

- ✅ Recebimentos agora são buscados por `CODPARC` e `LOTE` (não mais por `NUNOTA`)
- ✅ LOTE é obtido automaticamente da tabela `TGFITE` usando `NUNOTA`
- ✅ Suporte a múltiplos lotes por nota (gera arquivo separado para cada lote)
- ✅ Uso extensivo de SankhyaUtils (StringUtils, BigDecimalUtil, TimeUtils)
- ✅ Otimização de código com redução de IFs desnecessários
- ✅ Query de recebimento atualizada com `INNER JOIN` em `TGFEST` e filtros por `CODPARC` e `LOTE`
