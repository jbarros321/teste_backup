# 📊 DOCUMENTAÇÃO - CONSULTA GERENTE ONLINE

## 🎯 OBJETIVO

Esta consulta SQL foi desenvolvida para fornecer uma visão completa e detalhada dos dados relacionados aos **gerentes de vendas** e suas equipes no sistema Sankhya, permitindo análises de performance, acompanhamento de vendas e gestão de equipes.

---

## 📋 ARQUIVOS DISPONÍVEIS

| Arquivo | Descrição |
|---------|-----------|
| **consulta_gerente_online.sql** | Consulta detalhada com todas as informações por nota/vendedor |
| **consulta_gerente_online_resumida.sql** | Consulta resumida com totais e métricas agregadas por gerente |

---

## 🔍 CONSULTA DETALHADA (`consulta_gerente_online.sql`)

### Descrição
Retorna **uma linha para cada nota fiscal/pedido**, incluindo informações completas do gerente, vendedor, cliente e documento.

### Campos Retornados

#### Informações do Gerente
- `CODIGO_GERENTE`: Código único do gerente (CODVEND do gerente)
- `NOME_GERENTE`: Apelido/nome do gerente
- `NOME_COMPLETO_GERENTE`: Nome completo do gerente
- `STATUS_GERENTE`: Status ativo/inativo do gerente

#### Informações do Vendedor
- `CODIGO_VENDEDOR`: Código único do vendedor
- `NOME_VENDEDOR`: Apelido/nome do vendedor
- `NOME_COMPLETO_VENDEDOR`: Nome completo do vendedor
- `STATUS_VENDEDOR`: Status ativo/inativo do vendedor
- `CODIGO_SUPERVISOR`: Código do supervisor (campo AD_SUPERVISOR)
- `NOME_SUPERVISOR`: Nome do supervisor

#### Informações do Documento
- `NUNOTA`: Número único da nota
- `NUMNOTA`: Número da nota fiscal
- `DATA_NEGOCIACAO`: Data da negociação (DTNEG)
- `DATA_ENTREGA`: Data de entrega (DTENTSAI)
- `VALOR_NOTA`: Valor total da nota
- `PESO_TOTAL`: Peso total da nota
- `TIPO_DOCUMENTO`: Tipo (Venda, Pedido, Compra, etc.)
- `STATUS_DESCRICAO`: Status legível da nota

#### Informações do Cliente
- `CODIGO_CLIENTE`: Código do cliente
- `NOME_CLIENTE`: Razão social do cliente
- `CNPJ_CPF_CLIENTE`: CNPJ ou CPF do cliente

#### Informações da Empresa
- `CODIGO_EMPRESA`: Código da empresa
- `NOME_EMPRESA`: Razão social da empresa

#### Informações da Operação
- `CODTIPOPER`: Código do tipo de operação
- `DESCRICAO_OPERACAO`: Descrição do tipo de operação
- `TIPO_MOVIMENTO`: Tipo de movimento (V, P, C, E, T)

#### Métricas Calculadas (Window Functions)
- `TOTAL_VENDEDORES_EQUIPE`: Total de vendedores na equipe do gerente
- `TOTAL_NOTAS_MES_GERENTE`: Total de notas do gerente no mês
- `VALOR_TOTAL_MES_GERENTE`: Valor total do gerente no mês
- `TOTAL_NOTAS_MES_VENDEDOR`: Total de notas do vendedor no mês
- `VALOR_TOTAL_MES_VENDEDOR`: Valor total do vendedor no mês

---

## 📊 CONSULTA RESUMIDA (`consulta_gerente_online_resumida.sql`)

### Descrição
Retorna **uma linha por gerente** com totais e métricas agregadas, ideal para dashboards e relatórios executivos.

### Campos Retornados

#### Informações do Gerente
- `CODIGO_GERENTE`: Código único do gerente
- `NOME_GERENTE`: Apelido/nome do gerente
- `NOME_COMPLETO_GERENTE`: Nome completo do gerente

#### Métricas da Equipe
- `TOTAL_VENDEDORES_EQUIPE`: Quantidade de vendedores na equipe
- `TOTAL_NOTAS_PERIODO`: Total de notas no período
- `VALOR_TOTAL_PERIODO`: Valor total vendido no período
- `VALOR_MEDIO_NOTA`: Valor médio por nota
- `PRIMEIRA_VENDA`: Data da primeira venda do período
- `ULTIMA_VENDA`: Data da última venda do período

#### Métricas por Tipo
- `TOTAL_VENDAS`: Quantidade de vendas (TIPMOV = 'V')
- `TOTAL_PEDIDOS`: Quantidade de pedidos (TIPMOV = 'P')
- `VALOR_VENDAS`: Valor total de vendas
- `VALOR_PEDIDOS`: Valor total de pedidos

#### Métricas de Cobertura
- `TOTAL_CLIENTES_ATENDIDOS`: Quantidade de clientes únicos atendidos
- `TOTAL_EMPRESAS`: Quantidade de empresas envolvidas

---

## 🔧 PARÂMETROS DA CONSULTA

### Parâmetros Obrigatórios
- `:DATA_INICIO`: Data inicial do período (formato: DD/MM/YYYY ou DATE)
- `:DATA_FIM`: Data final do período (formato: DD/MM/YYYY ou DATE)

### Parâmetros Opcionais
- `:P_CODEMP`: Filtrar por código de empresa específico (NULL = todas)
- `:P_CODGERENTE`: Filtrar por código de gerente específico (NULL = todos)
- `:P_CODVENDEDOR`: Filtrar por código de vendedor específico (NULL = todos)
- `:P_TIPMOV`: Filtrar por tipo de movimento ('V' = Venda, 'P' = Pedido, 'T' = Todos)

---

## 📐 ESTRUTURA DAS TABELAS UTILIZADAS

### TGFCAB (Cabeçalho de Nota)
- **Chave Primária**: `NUNOTA`
- **Campos Utilizados**: NUNOTA, NUMNOTA, DTNEG, DTENTSAI, VLRNOTA, PESO, CODPARC, CODEMP, CODVEND, CODTIPOPER, DHTIPOPER, STATUSNOTA, TIPMOV
- **Filtro Obrigatório**: `STATUSNOTA = 'L'` (apenas notas liberadas)

### TGFVEN (Vendedores)
- **Chave Primária**: `CODVEND`
- **Campos Utilizados**: CODVEND, APELIDO, NOMEVEND, ATIVO, CODGER, AD_SUPERVISOR
- **Filtro Obrigatório**: `ATIVO = 'S'` (apenas vendedores ativos)
- **Relacionamento**: 
  - `VEN.CODGER` → `GER.CODVEND` (gerente do vendedor)
  - `VEN.AD_SUPERVISOR` → `SUP.CODVEND` (supervisor do vendedor)

### TGFPAR (Parceiros/Clientes)
- **Chave Primária**: `CODPARC`
- **Campos Utilizados**: CODPARC, RAZAOSOCIAL, CGC_CPF, ATIVO
- **Filtro Obrigatório**: `ATIVO = 'S'` (apenas clientes ativos)

### TSIEMP (Empresas)
- **Chave Primária**: `CODEMP`
- **Campos Utilizados**: CODEMP, RAZAOSOCIAL, ATIVO
- **Filtro Obrigatório**: `ATIVO = 'S'` (apenas empresas ativas)

### TGFTOP (Tipos de Operação)
- **Chave Primária Composta**: `CODTIPOPER` + `DHALTER`
- **Campos Utilizados**: CODTIPOPER, DHALTER, DESCROPER, TIPMOV
- **JOIN Obrigatório**: Usar `CODTIPOPER` + `DHTIPOPER` (nunca apenas CODTIPOPER)

---

## 🔗 RELACIONAMENTOS (JOINs)

### JOIN Principal
```sql
TGFCAB → TGFVEN (via CODVEND)
TGFVEN → TGFVEN GER (via CODGER) -- Gerente
TGFVEN → TGFVEN SUP (via AD_SUPERVISOR) -- Supervisor
TGFCAB → TGFPAR (via CODPARC)
TGFCAB → TSIEMP (via CODEMP)
TGFCAB → TGFTOP (via CODTIPOPER + DHTIPOPER)
```

### Tipos de JOIN
- **INNER JOIN**: Garante que apenas registros com relacionamento válido sejam retornados
- **LEFT JOIN**: Para supervisor (pode não existir)

---

## 📊 FUNCIONALIDADES AVANÇADAS

### Window Functions (Funções de Janela)
A consulta detalhada utiliza **Window Functions** para calcular métricas sem agrupar os dados:

```sql
COUNT(DISTINCT VEN.CODVEND) OVER (PARTITION BY GER.CODVEND)
```
- Calcula total de vendedores por gerente mantendo todas as linhas

```sql
SUM(CAB.VLRNOTA) OVER (PARTITION BY GER.CODVEND, TRUNC(CAB.DTNEG, 'MM'))
```
- Calcula valor total do gerente no mês mantendo todas as linhas

### CASE WHEN para Classificação
```sql
CASE 
    WHEN CAB.TIPMOV = 'V' THEN 'Venda'
    WHEN CAB.TIPMOV = 'P' THEN 'Pedido'
    ...
END AS TIPO_DOCUMENTO
```
- Converte códigos em descrições legíveis

---

## 🎯 CASOS DE USO

### 1. Dashboard de Performance de Gerentes
**Use**: `consulta_gerente_online_resumida.sql`
- Visualizar ranking de gerentes por valor vendido
- Comparar equipes
- Identificar gerentes com melhor performance

### 2. Análise Detalhada de Vendas
**Use**: `consulta_gerente_online.sql`
- Ver todas as notas de um gerente específico
- Analisar comportamento de vendedores individuais
- Rastrear vendas por cliente

### 3. Relatório de Equipe
**Use**: `consulta_gerente_online.sql` com filtro por gerente
- Listar todos os vendedores de uma equipe
- Ver vendas de cada vendedor
- Identificar vendedores com melhor desempenho

### 4. Análise Temporal
**Use**: Ambas as consultas com filtro de período
- Comparar períodos
- Identificar tendências
- Acompanhar evolução de vendas

---

## ⚙️ CONFIGURAÇÃO NO ANALYTICS

### Passo 1: Criar Nova Consulta
1. No Analytics, vá em **Consultas** → **Nova Consulta**
2. Selecione **SQL** como tipo de consulta
3. Cole o conteúdo do arquivo `consulta_gerente_online.sql` ou `consulta_gerente_online_resumida.sql`

### Passo 2: Configurar Parâmetros
1. Defina os parâmetros obrigatórios:
   - `DATA_INICIO`: Data inicial
   - `DATA_FIM`: Data final
2. Configure parâmetros opcionais conforme necessário

### Passo 3: Testar Consulta
1. Execute a consulta com parâmetros de teste
2. Verifique se os resultados estão corretos
3. Ajuste filtros se necessário

### Passo 4: Criar Visualizações
1. Use a consulta resumida para gráficos e dashboards
2. Use a consulta detalhada para tabelas e relatórios
3. Configure filtros interativos

---

## 🔍 EXEMPLOS DE USO

### Exemplo 1: Performance de Todos os Gerentes no Mês
```sql
-- Usar consulta_resumida.sql
-- Parâmetros:
DATA_INICIO = '01/01/2024'
DATA_FIM = '31/01/2024'
P_CODEMP = NULL (todas as empresas)
P_CODGERENTE = NULL (todos os gerentes)
P_TIPMOV = 'T' (todos os tipos)
```

### Exemplo 2: Vendas Detalhadas de um Gerente Específico
```sql
-- Usar consulta_detalhada.sql
-- Parâmetros:
DATA_INICIO = '01/01/2024'
DATA_FIM = '31/01/2024'
P_CODEMP = NULL
P_CODGERENTE = 123 (código do gerente)
P_TIPMOV = 'V' (apenas vendas)
```

### Exemplo 3: Análise de um Vendedor Específico
```sql
-- Usar consulta_detalhada.sql
-- Parâmetros:
DATA_INICIO = '01/01/2024'
DATA_FIM = '31/01/2024'
P_CODEMP = NULL
P_CODGERENTE = NULL
P_CODVENDEDOR = 456 (código do vendedor)
P_TIPMOV = 'T'
```

---

## ⚠️ OBSERVAÇÕES IMPORTANTES

### Filtros Obrigatórios Aplicados
✅ **STATUSNOTA = 'L'**: Apenas notas liberadas
✅ **ATIVO = 'S'**: Apenas vendedores, clientes e empresas ativos
✅ **JOIN com DHTIPOPER**: Garante dados corretos de tipo de operação

### Performance
- A consulta detalhada pode retornar muitas linhas
- Use filtros de período e gerente para melhor performance
- Considere criar índices em:
  - `TGFCAB.DTNEG`
  - `TGFCAB.CODVEND`
  - `TGFVEN.CODGER`

### Limitações
- Window Functions podem impactar performance em grandes volumes
- Se necessário, use a consulta resumida e depois detalhe por gerente

---

## 🐛 TROUBLESHOOTING

### Problema: Consulta retorna vazio
**Solução**: 
- Verifique se há notas liberadas no período
- Verifique se os vendedores têm gerente cadastrado (CODGER)
- Verifique se os filtros não estão muito restritivos

### Problema: Gerente não aparece
**Solução**:
- Verifique se o gerente está ativo (ATIVO = 'S')
- Verifique se há vendedores com CODGER preenchido
- Verifique se há notas no período

### Problema: Performance lenta
**Solução**:
- Use a consulta resumida ao invés da detalhada
- Adicione filtros mais específicos (gerente, empresa)
- Reduza o período de análise
- Verifique índices no banco de dados

---

## 📚 REFERÊNCIAS

- **Template/REFERENCIA_SANKHYA.md**: Referência completa de tabelas Sankhya
- **Template/CONHECIMENTO_CONSOLIDADO.md**: Padrões e boas práticas
- **GuaranaMineiro/src/main/resources/sql/query.sql**: Exemplo de consulta similar

---

**Última atualização**: 2025-01-02  
**Versão**: 1.0.0



