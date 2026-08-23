# 📊 DOCUMENTAÇÃO - CONSULTA GOL (GERENTE ONLINE) SANKHYA

## 🎯 OBJETIVO

Esta documentação descreve as consultas SQL desenvolvidas especificamente para o **módulo GOL (Gerente Online)** do Sankhya, que é o sistema de gestão de equipes de vendas online.

O GOL permite gerenciar hierarquias de vendas com:
- **Gerentes Online (GOL)**: Gerentes que coordenam equipes de vendas
- **Supervisores**: Supervisores que gerenciam grupos de vendedores
- **Vendedores**: Vendedores que realizam as vendas

---

## 📋 ARQUIVOS DISPONÍVEIS

| Arquivo | Descrição |
|---------|-----------|
| **consulta_GOL_gerente_online.sql** | Consulta detalhada com todas as informações por nota/vendedor/supervisor/gerente GOL |
| **consulta_GOL_gerente_online_resumida.sql** | Consulta resumida com totais e métricas agregadas por gerente GOL |
| **consulta_GOL_hierarquia_equipe.sql** | Consulta da hierarquia completa (Gerente → Supervisor → Vendedor) com métricas |

---

## 🏗️ ESTRUTURA DO MÓDULO GOL

### Hierarquia de Vendas
```
GERENTE ONLINE (GOL)
    └── SUPERVISOR
            └── VENDEDOR
                    └── CLIENTE
```

### Relacionamentos no Banco de Dados
- **TGFVEN.CODGER**: Código do gerente (relaciona vendedor com gerente GOL)
- **TGFVEN.AD_SUPERVISOR**: Código do supervisor (campo adicional que relaciona vendedor com supervisor)

---

## 🔍 CONSULTA DETALHADA (`consulta_GOL_gerente_online.sql`)

### Descrição
Retorna **uma linha para cada nota fiscal/pedido**, incluindo informações completas da hierarquia GOL: Gerente Online → Supervisor → Vendedor → Cliente.

### Campos Retornados

#### Informações do Gerente Online (GOL)
- `CODIGO_GERENTE_GOL`: Código único do gerente online
- `NOME_GERENTE_GOL`: Apelido/nome do gerente online
- `NOME_COMPLETO_GERENTE_GOL`: Nome completo do gerente online
- `STATUS_GERENTE_GOL`: Status ativo/inativo do gerente

#### Informações do Supervisor
- `CODIGO_SUPERVISOR`: Código único do supervisor
- `NOME_SUPERVISOR`: Apelido/nome do supervisor
- `NOME_COMPLETO_SUPERVISOR`: Nome completo do supervisor
- `STATUS_SUPERVISOR`: Status ativo/inativo do supervisor

#### Informações do Vendedor
- `CODIGO_VENDEDOR`: Código único do vendedor
- `NOME_VENDEDOR`: Apelido/nome do vendedor
- `NOME_COMPLETO_VENDEDOR`: Nome completo do vendedor
- `STATUS_VENDEDOR`: Status ativo/inativo do vendedor
- `CODIGO_SUPERVISOR_VENDEDOR`: Código do supervisor do vendedor (campo AD_SUPERVISOR)

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
- `TELEFONE_CLIENTE`: Telefone do cliente
- `EMAIL_CLIENTE`: Email do cliente

#### Informações da Empresa
- `CODIGO_EMPRESA`: Código da empresa
- `NOME_EMPRESA`: Razão social da empresa

#### Informações da Operação
- `CODTIPOPER`: Código do tipo de operação
- `DESCRICAO_OPERACAO`: Descrição do tipo de operação
- `TIPO_MOVIMENTO`: Tipo de movimento (V, P, C, E, T)

#### Métricas Calculadas (Window Functions)
- `TOTAL_VENDEDORES_EQUIPE_GOL`: Total de vendedores na equipe do gerente GOL
- `TOTAL_NOTAS_MES_GERENTE_GOL`: Total de notas do gerente GOL no mês
- `VALOR_TOTAL_MES_GERENTE_GOL`: Valor total do gerente GOL no mês
- `TOTAL_NOTAS_MES_SUPERVISOR`: Total de notas do supervisor no mês
- `VALOR_TOTAL_MES_SUPERVISOR`: Valor total do supervisor no mês
- `TOTAL_NOTAS_MES_VENDEDOR`: Total de notas do vendedor no mês
- `VALOR_TOTAL_MES_VENDEDOR`: Valor total do vendedor no mês
- `VALOR_MEDIO_NOTA_GERENTE_GOL`: Valor médio por nota do gerente GOL no mês
- `VALOR_MEDIO_NOTA_VENDEDOR`: Valor médio por nota do vendedor no mês

---

## 📊 CONSULTA RESUMIDA (`consulta_GOL_gerente_online_resumida.sql`)

### Descrição
Retorna **uma linha por gerente GOL** com totais e métricas agregadas, ideal para dashboards e relatórios executivos.

### Campos Retornados

#### Informações do Gerente GOL
- `CODIGO_GERENTE_GOL`: Código único do gerente online
- `NOME_GERENTE_GOL`: Apelido/nome do gerente online
- `NOME_COMPLETO_GERENTE_GOL`: Nome completo do gerente online
- `STATUS_GERENTE_GOL`: Status ativo/inativo do gerente

#### Métricas da Equipe GOL
- `TOTAL_SUPERVISORES_EQUIPE`: Quantidade de supervisores na equipe
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
- `TOTAL_VENDEDORES_ATIVOS`: Quantidade de vendedores ativos na equipe

---

## 🏢 CONSULTA HIERARQUIA (`consulta_GOL_hierarquia_equipe.sql`)

### Descrição
Retorna **uma linha por combinação de Gerente GOL → Supervisor → Vendedor** com métricas agregadas, mostrando a estrutura completa da hierarquia de vendas.

### Campos Retornados

#### Hierarquia Completa
- `CODIGO_GERENTE_GOL` / `NOME_GERENTE_GOL`: Gerente Online
- `CODIGO_SUPERVISOR` / `NOME_SUPERVISOR`: Supervisor
- `CODIGO_VENDEDOR` / `NOME_VENDEDOR`: Vendedor
- `STATUS_VENDEDOR`: Status do vendedor

#### Métricas por Nível
- `TOTAL_NOTAS_PERIODO`: Total de notas
- `VALOR_TOTAL_PERIODO`: Valor total
- `VALOR_MEDIO_NOTA`: Valor médio por nota
- `PRIMEIRA_VENDA` / `ULTIMA_VENDA`: Datas extremas
- `TOTAL_VENDAS` / `TOTAL_PEDIDOS`: Quantidades por tipo
- `VALOR_VENDAS` / `VALOR_PEDIDOS`: Valores por tipo
- `TOTAL_CLIENTES_ATENDIDOS`: Clientes únicos

---

## 🔧 PARÂMETROS DAS CONSULTAS

### Parâmetros Obrigatórios
- `:DATA_INICIO`: Data inicial do período (formato: DD/MM/YYYY ou DATE)
- `:DATA_FIM`: Data final do período (formato: DD/MM/YYYY ou DATE)

### Parâmetros Opcionais
- `:P_CODEMP`: Filtrar por código de empresa específico (NULL = todas)
- `:P_CODGERENTE_GOL`: Filtrar por código de gerente GOL específico (NULL = todos)
- `:P_CODSUPERVISOR`: Filtrar por código de supervisor específico (NULL = todos)
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
- **Relacionamentos GOL**: 
  - `VEN.CODGER` → `GER.CODVEND` (gerente GOL do vendedor)
  - `VEN.AD_SUPERVISOR` → `SUP.CODVEND` (supervisor do vendedor)

### TGFPAR (Parceiros/Clientes)
- **Chave Primária**: `CODPARC`
- **Campos Utilizados**: CODPARC, RAZAOSOCIAL, CGC_CPF, TELEFONE, EMAIL, ATIVO
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

## 🔗 RELACIONAMENTOS (JOINs) GOL

### JOIN Principal
```sql
TGFCAB → TGFVEN (via CODVEND) -- Vendedor
TGFVEN → TGFVEN GER (via CODGER) -- Gerente GOL
TGFVEN → TGFVEN SUP (via AD_SUPERVISOR) -- Supervisor
TGFCAB → TGFPAR (via CODPARC) -- Cliente
TGFCAB → TSIEMP (via CODEMP) -- Empresa
TGFCAB → TGFTOP (via CODTIPOPER + DHTIPOPER) -- Tipo Operação
```

### Tipos de JOIN
- **INNER JOIN**: Garante que apenas registros com relacionamento válido sejam retornados
- **LEFT JOIN**: Para supervisor (pode não existir - nem todo vendedor tem supervisor)

---

## 🎯 CASOS DE USO GOL

### 1. Dashboard de Performance de Gerentes GOL
**Use**: `consulta_GOL_gerente_online_resumida.sql`
- Visualizar ranking de gerentes GOL por valor vendido
- Comparar equipes de diferentes gerentes GOL
- Identificar gerentes GOL com melhor performance

### 2. Análise Detalhada de Vendas GOL
**Use**: `consulta_GOL_gerente_online.sql`
- Ver todas as notas de um gerente GOL específico
- Analisar comportamento de supervisores e vendedores
- Rastrear vendas por cliente na hierarquia GOL

### 3. Relatório de Hierarquia Completa
**Use**: `consulta_GOL_hierarquia_equipe.sql`
- Visualizar estrutura completa: Gerente GOL → Supervisor → Vendedor
- Ver performance de cada nível da hierarquia
- Identificar gargalos na cadeia de vendas

### 4. Análise de Equipe por Supervisor
**Use**: `consulta_GOL_hierarquia_equipe.sql` com filtro por supervisor
- Ver todos os vendedores de um supervisor
- Comparar performance de supervisores
- Identificar supervisores com melhor desempenho

### 5. Análise Temporal GOL
**Use**: Todas as consultas com filtro de período
- Comparar períodos
- Identificar tendências
- Acompanhar evolução de vendas por gerente GOL

---

## ⚙️ CONFIGURAÇÃO NO ANALYTICS

### Passo 1: Criar Nova Consulta
1. No Analytics, vá em **Consultas** → **Nova Consulta**
2. Selecione **SQL** como tipo de consulta
3. Cole o conteúdo do arquivo `.sql` escolhido

### Passo 2: Configurar Parâmetros
1. Defina os parâmetros obrigatórios:
   - `DATA_INICIO`: Data inicial
   - `DATA_FIM`: Data final
2. Configure parâmetros opcionais conforme necessário:
   - `P_CODGERENTE_GOL`: Para filtrar por gerente GOL específico
   - `P_CODSUPERVISOR`: Para filtrar por supervisor específico
   - `P_CODVENDEDOR`: Para filtrar por vendedor específico

### Passo 3: Testar Consulta
1. Execute a consulta com parâmetros de teste
2. Verifique se os resultados estão corretos
3. Ajuste filtros se necessário

### Passo 4: Criar Visualizações
1. Use a consulta resumida para gráficos e dashboards
2. Use a consulta detalhada para tabelas e relatórios
3. Use a consulta hierarquia para visualizações em árvore
4. Configure filtros interativos

---

## 🔍 EXEMPLOS DE USO

### Exemplo 1: Performance de Todos os Gerentes GOL no Mês
```sql
-- Usar consulta_GOL_gerente_online_resumida.sql
-- Parâmetros:
DATA_INICIO = '01/01/2024'
DATA_FIM = '31/01/2024'
P_CODEMP = NULL (todas as empresas)
P_CODGERENTE_GOL = NULL (todos os gerentes GOL)
P_TIPMOV = 'T' (todos os tipos)
```
**Resultado**: Ranking de todos os gerentes GOL com totais do mês

### Exemplo 2: Vendas Detalhadas de um Gerente GOL Específico
```sql
-- Usar consulta_GOL_gerente_online.sql
-- Parâmetros:
DATA_INICIO = '01/01/2024'
DATA_FIM = '31/01/2024'
P_CODEMP = NULL
P_CODGERENTE_GOL = 123 (código do gerente GOL)
P_TIPMOV = 'V' (apenas vendas)
```
**Resultado**: Todas as vendas detalhadas do gerente GOL 123 com hierarquia completa

### Exemplo 3: Hierarquia Completa de uma Equipe
```sql
-- Usar consulta_GOL_hierarquia_equipe.sql
-- Parâmetros:
DATA_INICIO = '01/01/2024'
DATA_FIM = '31/01/2024'
P_CODEMP = NULL
P_CODGERENTE_GOL = 123 (código do gerente GOL)
P_TIPMOV = 'T'
```
**Resultado**: Estrutura completa Gerente GOL → Supervisor → Vendedor com métricas

### Exemplo 4: Análise de um Supervisor Específico
```sql
-- Usar consulta_GOL_hierarquia_equipe.sql
-- Parâmetros:
DATA_INICIO = '01/01/2024'
DATA_FIM = '31/01/2024'
P_CODEMP = NULL
P_CODGERENTE_GOL = NULL
P_CODSUPERVISOR = 456 (código do supervisor)
P_TIPMOV = 'T'
```
**Resultado**: Todos os vendedores do supervisor 456 com métricas

---

## ⚠️ OBSERVAÇÕES IMPORTANTES

### Filtros Obrigatórios Aplicados
✅ **STATUSNOTA = 'L'**: Apenas notas liberadas
✅ **ATIVO = 'S'**: Apenas vendedores, clientes e empresas ativos
✅ **JOIN com DHTIPOPER**: Garante dados corretos de tipo de operação

### Campos Adicionais (AD_)
⚠️ **AD_SUPERVISOR**: Campo adicional na tabela TGFVEN
- Nem todos os vendedores podem ter supervisor cadastrado
- Use LEFT JOIN para incluir vendedores sem supervisor
- Verifique se o campo existe no banco de dados do cliente

### Performance
- A consulta detalhada pode retornar muitas linhas
- Use filtros de período e gerente GOL para melhor performance
- Considere criar índices em:
  - `TGFCAB.DTNEG`
  - `TGFCAB.CODVEND`
  - `TGFVEN.CODGER`
  - `TGFVEN.AD_SUPERVISOR`

### Limitações
- Window Functions podem impactar performance em grandes volumes
- Se necessário, use a consulta resumida e depois detalhe por gerente GOL
- Campos AD_ podem não existir em todas as instalações Sankhya

---

## 🐛 TROUBLESHOOTING

### Problema: Consulta retorna vazio
**Solução**: 
- Verifique se há notas liberadas no período
- Verifique se os vendedores têm gerente GOL cadastrado (CODGER)
- Verifique se os filtros não estão muito restritivos
- Verifique se o campo AD_SUPERVISOR existe no banco

### Problema: Gerente GOL não aparece
**Solução**:
- Verifique se o gerente está ativo (ATIVO = 'S')
- Verifique se há vendedores com CODGER preenchido
- Verifique se há notas no período

### Problema: Supervisor não aparece
**Solução**:
- Verifique se o campo AD_SUPERVISOR está preenchido nos vendedores
- Verifique se o supervisor está ativo (ATIVO = 'S')
- Lembre-se que nem todo vendedor tem supervisor (LEFT JOIN)

### Problema: Performance lenta
**Solução**:
- Use a consulta resumida ao invés da detalhada
- Adicione filtros mais específicos (gerente GOL, empresa)
- Reduza o período de análise
- Verifique índices no banco de dados

### Problema: Campo AD_SUPERVISOR não existe
**Solução**:
- Verifique se o campo foi criado no banco de dados
- Se não existir, remova as referências ao supervisor ou crie o campo
- Consulte a documentação Sankhya sobre campos adicionais

---

## 📚 REFERÊNCIAS

- **Template/REFERENCIA_SANKHYA.md**: Referência completa de tabelas Sankhya
- **Template/CONHECIMENTO_CONSOLIDADO.md**: Padrões e boas práticas
- **GuaranaMineiro/src/main/resources/sql/query.sql**: Exemplo de consulta similar com AD_SUPERVISOR
- **PetKids/docs/VALIDACAO_CAMPOS.md**: Exemplo de uso de CODGER e AD_SUPERVISOR

---

## 🔄 DIFERENÇAS ENTRE CONSULTAS

| Consulta | Foco | Linhas Retornadas | Uso Ideal |
|----------|------|-------------------|-----------|
| **Detalhada** | Uma linha por nota | Muitas | Relatórios detalhados, análises específicas |
| **Resumida** | Uma linha por gerente GOL | Poucas | Dashboards, rankings executivos |
| **Hierarquia** | Uma linha por combinação GOL→Supervisor→Vendedor | Médias | Visualização de estrutura, análise de equipes |

---

**Última atualização**: 2025-01-02  
**Versão**: 1.0.0 - GOL (Gerente Online) Sankhya



