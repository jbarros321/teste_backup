# 📊 REFERÊNCIA SANKHYA - TABELAS E MÉTODOS

## 🎯 VISÃO GERAL

Este documento **CONSOLIDA** conhecimento completo sobre:
- ✅ Tabelas principais do Sankhya (TGFCAB, TGFITE, TGFPRO, etc.)
- ✅ Métodos de repositório (AbstractRepository)
- ✅ Queries SQL padrão e otimizadas
- ✅ Utilitários Sankhya

**Conhecimento consolidado de**: Todos os projetos do repositório (Denver, PetKids, GuaranaMineiro, Megleo, Eletromac, Iwannasleep)

---

## 📚 ÍNDICE RÁPIDO DE NAVEGAÇÃO

### Tabelas Core
- [TGFCAB](#tgfcab---cabeçalho-de-notas-fiscais) - Cabeçalho de Notas Fiscais
- [TGFITE](#tgfite---itens-de-notas-fiscais) - Itens de Notas Fiscais
- [TGFPRO](#tgfpro---produtos) - Produtos
- [TGFPAR](#tgfpar---parceiros-clientes-e-fornecedores) - Parceiros (Clientes e Fornecedores)
- [TSIEMP](#tsiemp---empresas) - Empresas
- [TGFEST](#tgfest---estoque) - Estoque
- [TGFVEN](#tgfven---vendedores) - Vendedores
- [TSIUSU](#tsiusu---usuários) - Usuários

### Tabelas Financeiras
- [TGFFIN](#tgffin---financeiro-títulos-a-receberpagar) - Financeiro (Títulos a Receber/Pagar)
- [TGFTOP](#tgftop---tipos-de-operação) - Tipos de Operação
- [TGFTIT](#tgftit---tipos-de-título) - Tipos de Título
- [TGFTPV](#tgftpv---tipos-de-venda) - Tipos de Venda
- [TGFPPG](#tgfppg---prazos-de-pagamento) - Prazos de Pagamento

### Tabelas de Logística
- [TGFORD](#tgford---ordens-de-carga) - Ordens de Carga
- [TGFVEI](#tgfvei---veículos) - Veículos

### Tabelas de Checklist
- [TCFCHKOPER](#tcfchkoper---checklist-operacional) - Checklist Operacional
- [TCFMODCHECKLIST](#tcfmodchecklist---modelos-de-checklist) - Modelos de Checklist

### Métodos e Componentes
- [Métodos de Repositório](#-métodos-de-repositório---guia-completo) - AbstractRepository
- [Padrões de Query](#-padrões-avançados-de-queries) - Queries SQL otimizadas
- [Filtros Obrigatórios](#-filtros-obrigatórios---resumo) - Resumo de filtros

---

## 📋 TABELAS CORE - CONHECIMENTO PROFUNDO

### TGFCAB - Cabeçalho de Notas Fiscais

**Descrição**: Tabela principal que armazena o cabeçalho de todas as notas fiscais (entrada e saída).

**Chave Primária**: `NUNOTA` (Número Único da Nota)

**Campos Principais**:
- `NUNOTA`: Número único da nota (PK)
- `NUMNOTA`: Número da nota fiscal
- `SERIENOTA`: Série da nota fiscal
- `DTNEG`: Data de negociação
- `DTFATUR`: Data de faturamento
- `CODPARC`: Código do parceiro (cliente/fornecedor)
- `CODEMP`: Código da empresa
- `CODVEND`: Código do vendedor
- `TIPMOV`: Tipo de movimento ('C' = Compra, 'V' = Venda, 'T' = Transferência)
- `STATUSNOTA`: Status da nota ('L' = Liberada, 'P' = Pendente, 'C' = Cancelada)
- `CODTIPOPER`: Código do tipo de operação
- `ORDEMCARGA`: Ordem de carga
- `CIF_FOB`: Tipo de frete ('C' = CIF, 'F' = FOB)
- `CODTIPVENDA`: Código do tipo de venda
- `PENDENTE`: Pendência ('S' = Sim, 'N' = Não)

**Filtros Obrigatórios**:
```sql
WHERE CAB.STATUSNOTA = 'L'  -- SEMPRE filtrar apenas notas liberadas
```

**Relacionamentos**:
- `TGFITE` (1:N) via `NUNOTA`
- `TGFPAR` (N:1) via `CODPARC`
- `TSIEMP` (N:1) via `CODEMP`
- `TGFVEN` (N:1) via `CODVEND`
- `TGFTOP` (N:1) via `CODTIPOPER`

**Padrões de Query**:
```sql
SELECT CAB.NUNOTA, CAB.NUMNOTA, CAB.DTNEG, CAB.CODPARC, CAB.CODEMP
FROM TGFCAB CAB
WHERE CAB.STATUSNOTA = 'L'
  AND CAB.TIPMOV = 'V'  -- Vendas
  AND CAB.DTNEG >= :DATA_INI
  AND CAB.DTNEG <= :DATA_FIM
```

---

### TGFITE - Itens de Notas Fiscais

**Descrição**: Tabela que armazena os itens de cada nota fiscal.

**Chave Primária Composta**: `NUNOTA` + `SEQUENCIA`

**Campos Principais**:
- `NUNOTA`: Número único da nota (FK para TGFCAB)
- `SEQUENCIA`: Sequência do item
- `CODPROD`: Código do produto (FK para TGFPRO)
- `QTDNEG`: Quantidade negociada (pode ser negativa)
- `VLRUNIT`: Valor unitário
- `VLRTOT`: Valor total
- `VLRDESC`: Valor de desconto
- `VLRIPI`: Valor de IPI
- `VLRICMS`: Valor de ICMS
- `VLRSUBST`: Valor de substituição tributária
- `CONTROLE`: Controle de lote/série
- `CODLOCALORIG`: Código do local de origem
- `CODLOCALDEST`: Código do local de destino

**Relacionamentos**:
- `TGFCAB` (N:1) via `NUNOTA`
- `TGFPRO` (N:1) via `CODPROD`
- `TGFEST` (N:1) via `CODPROD` + `CONTROLE` + `CODLOCALORIG`

**Padrões de Query**:
```sql
SELECT ITE.NUNOTA, ITE.SEQUENCIA, ITE.CODPROD, ITE.QTDNEG, ITE.VLRUNIT, ITE.VLRTOT
FROM TGFITE ITE
INNER JOIN TGFCAB CAB ON CAB.NUNOTA = ITE.NUNOTA
WHERE CAB.STATUSNOTA = 'L'
  AND ITE.QTDNEG > 0
```

---

### TGFPRO - Produtos

**Descrição**: Tabela que armazena o cadastro de produtos e serviços.

**Chave Primária**: `CODPROD` (Código do Produto)

**Campos Principais**:
- `CODPROD`: Código do produto (PK)
- `DESCRPROD`: Descrição do produto
- `REFERENCIA`: Referência/código de barras/EAN
- `ATIVO`: Flag de ativo ('S' = Ativo, 'N' = Inativo)
- `CODFAB`: Código do fabricante
- `UNIDADE`: Unidade de medida
- `CODVOL`: Código de volume
- `QTDEMB`: Quantidade de embalagem
- `PESOLIQ`: Peso líquido
- `PESOBRUTO`: Peso bruto
- `COMPLDESC`: Descrição complementar
- `USOPROD`: Uso do produto ('S' = Serviço, 'P' ou outro = Produto)

**Filtros Obrigatórios**:
```sql
AND PRO.ATIVO = 'S'  -- SEMPRE filtrar apenas produtos ativos
```

**Padrões de Query**:
```sql
SELECT PRO.CODPROD, PRO.DESCRPROD, PRO.REFERENCIA, PRO.ATIVO
FROM TGFPRO PRO
WHERE PRO.ATIVO = 'S'
  AND PRO.USOPROD <> 'S'  -- Apenas produtos, não serviços
```

**Formatação de Código**:
```sql
-- Priorizar REFERENCIA, fallback para CODPROD com zeros à esquerda
NVL(PRO.REFERENCIA, LPAD(TO_CHAR(PRO.CODPROD), 13, '0')) AS CODIGOPRODUTO
```

---

### TGFPAR - Parceiros (Clientes e Fornecedores)

**Descrição**: Tabela que armazena o cadastro de clientes, fornecedores e outros parceiros.

**Chave Primária**: `CODPARC` (Código do Parceiro)

**Campos Principais**:
- `CODPARC`: Código do parceiro (PK)
- `RAZAOSOCIAL`: Razão social
- `NOMEFANTASIA`: Nome fantasia
- `CGC_CPF`: CNPJ ou CPF (sem formatação no banco)
- `TIPPESSOA`: Tipo de pessoa ('J' = Jurídica, 'F' = Física)
- `ATIVO`: Flag de ativo ('S' = Ativo, 'N' = Inativo)
- `CODCID`: Código da cidade
- `CEP`: CEP
- `CODPARC`: Código do parceiro

**Filtros Obrigatórios**:
```sql
AND PAR.ATIVO = 'S'  -- SEMPRE filtrar apenas parceiros ativos
```

**Formatação de CNPJ/CPF**:
```sql
-- Remover formatação
REPLACE(REPLACE(REPLACE(REPLACE(PAR.CGC_CPF, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ
```

---

### TSIEMP - Empresas

**Descrição**: Tabela que armazena o cadastro de empresas do sistema.

**Chave Primária**: `CODEMP` (Código da Empresa)

**Campos Principais**:
- `CODEMP`: Código da empresa (PK)
- `RAZAOSOCIAL`: Razão social
- `NOMEFANTASIA`: Nome fantasia
- `CGC`: CNPJ da empresa
- `ATIVO`: Flag de ativo ('S' = Ativo, 'N' = Inativo)
- `CODPARC`: Código do parceiro (relacionamento com TGFPAR)

**Filtros Obrigatórios**:
```sql
AND EMP.ATIVO = 'S'  -- SEMPRE filtrar apenas empresas ativas
```

**Formatação de CNPJ**:
```sql
REPLACE(REPLACE(REPLACE(REPLACE(EMP.CGC, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ
```

---

### TGFEST - Estoque

**Descrição**: Tabela que armazena informações de estoque por produto, empresa, lote e local.

**Chave Primária Composta**: `CODPROD` + `CODEMP` + `CONTROLE` + `CODLOCAL`

**Campos Principais**:
- `CODPROD`: Código do produto (FK para TGFPRO)
- `CODEMP`: Código da empresa (FK para TSIEMP)
- `CONTROLE`: Controle de lote/série
- `CODLOCAL`: Código do local
- `ESTOQUE`: Quantidade em estoque
- `DTFABRICACAO`: Data de fabricação
- `DTVAL`: Data de validade
- `ATIVO`: Flag de ativo ('S' = Ativo, 'N' = Inativo)
- `CODPARC`: Código do parceiro/fornecedor

**Filtros Obrigatórios**:
```sql
AND EST.ATIVO = 'S'  -- SEMPRE filtrar apenas estoque ativo
```

**Relacionamentos**:
- `TGFPRO` (N:1) via `CODPROD`
- `TSIEMP` (N:1) via `CODEMP`
- `TGFITE` (N:1) via `CODPROD` + `CONTROLE` + `CODLOCAL`

**Padrões de Query**:
```sql
SELECT EST.CODPROD, EST.CODEMP, EST.CONTROLE, EST.ESTOQUE, EST.DTFABRICACAO, EST.DTVAL
FROM TGFEST EST
INNER JOIN TGFPRO PRO ON PRO.CODPROD = EST.CODPROD AND PRO.ATIVO = 'S'
WHERE EST.ATIVO = 'S'
  AND EST.CODEMP = :CODEMP
  AND EST.CONTROLE = :LOTE
```

---

### TGFVEN - Vendedores

**Descrição**: Tabela que armazena o cadastro de vendedores.

**Chave Primária**: `CODVEND` (Código do Vendedor)

**Campos Principais**:
- `CODVEND`: Código do vendedor (PK)
- `NOMEVEND`: Nome do vendedor
- `ATIVO`: Flag de ativo ('S' = Ativo, 'N' = Inativo)

**Filtros Obrigatórios**:
```sql
AND VEN.ATIVO = 'S'  -- SEMPRE filtrar apenas vendedores ativos
```

---

### TSIUSU - Usuários

**Descrição**: Tabela que armazena o cadastro de usuários do sistema.

**Chave Primária**: `CODUSU` (Código do Usuário)

**Campos Principais**:
- `CODUSU`: Código do usuário (PK)
- `NOMEUSU`: Nome do usuário
- `ATIVO`: Flag de ativo ('S' = Ativo, 'N' = Inativo)

**Filtros Obrigatórios**:
```sql
AND USU.ATIVO = 'S'  -- SEMPRE filtrar apenas usuários ativos
```

---

### TGFCAB - Campos Adicionais Importantes

**Campos de Controle e Validação**:
- `DTMOV`: Data de movimento (usada em análises temporais)
- `DTENTSAI`: Data de entrada/saída (validação em compras)
- `CODEMPNEGOC`: Código da empresa de negócio (transferências)
- `DUPLICADO`: Flag de duplicação ('S' = Sim, 'N' = Não)
- `OBSERVACAO`: Observações da nota
- `PESO`: Peso total da nota
- `VLRNOTA`: Valor total da nota
- `VLRICMSDIFALDEST`: Valor ICMS diferido alíquota destino
- `VLRSUBST`: Valor substituição tributária
- `CODTIPOPER`: Código tipo operação (relaciona com TGFTOP)
- `DHTIPOPER`: Data/hora alteração tipo operação (para JOIN com TGFTOP)
- `CODTIPVENDA`: Código tipo venda (relaciona com TGFTPV)
- `CODVEICULO`: Código do veículo (relaciona com TGFVEI)

**Padrões de Validação por CODTIPOPER**:
```sql
-- Transferências internas (TOP 18, 134): mesma empresa origem e destino
-- Transferência local (TOP 154): empresas específicas permitidas
-- Transferência NF-E (TOP 622): empresas específicas permitidas
-- Entrada estoque (TOP 113): validar empresa do usuário
-- Pedidos (TOP 1): validar pendências e duplicados
```

---

### TGFITE - Campos Adicionais Importantes

**Campos de Controle**:
- `QTDENTREGUE`: Quantidade entregue (usado em pedidos)
- `PENDENTE`: Flag de pendência ('S' = Sim, 'N' = Não)
- `CODEMP`: Código da empresa (pode diferir do cabeçalho)
- `CODLOCALORIG`: Local de origem (relaciona com estoque)
- `CODLOCALDEST`: Local de destino (relaciona com estoque)
- `CODVOL`: Código de volume/unidade alternativa

**Validações Importantes**:
```sql
-- Desconto negativo: validar conforme CODTIPOPER
-- Quantidade entregue: não pode ser maior que QTDNEG
-- Sequência negativa: usado em ajustes e devoluções
-- Controle de lote: obrigatório para produtos com TIPCONTEST = 'S'
```

---

## 💰 TABELAS FINANCEIRAS

### TGFFIN - Financeiro (Títulos a Receber/Pagar)

**Descrição**: Tabela que armazena títulos financeiros (contas a receber e a pagar).

**Chave Primária**: `NUFIN` (Número Único Financeiro)

**Campos Principais**:
- `NUFIN`: Número único financeiro (PK)
- `NUNOTA`: Número único da nota (FK para TGFCAB)
- `CODPARC`: Código do parceiro (FK para TGFPAR)
- `CODEMP`: Código da empresa (FK para TSIEMP)
- `CODVEND`: Código do vendedor (FK para TGFVEN)
- `DTNEG`: Data de negociação
- `DTVENC`: Data de vencimento
- `DHBAIXA`: Data/hora de baixa (NULL = não baixado)
- `VLRDESDOB`: Valor desdobrado (valor do título)
- `RECDESP`: Receita/Despesa (1 = Receita, -1 = Despesa)
- `PROVISAO`: Flag de provisão ('S' = Sim, 'N' = Não)
- `CODTIPTIT`: Código tipo título (FK para TGFTIT)
- `ORIGEM`: Origem ('E' = Entrada, 'S' = Saída)
- `HISTORICO`: Histórico do título
- `NUMNOTA`: Número da nota fiscal
- `NUMCONTRATO`: Número do contrato

**Filtros Obrigatórios**:
```sql
WHERE FIN.RECDESP = 1  -- Receitas (ou -1 para despesas)
  AND FIN.PROVISAO = 'N'  -- Excluir provisões
  AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99)  -- Excluir tipos específicos
```

**Relacionamentos**:
- `TGFCAB` (N:1) via `NUNOTA`
- `TGFPAR` (N:1) via `CODPARC`
- `TSIEMP` (N:1) via `CODEMP`
- `TGFVEN` (N:1) via `CODVEND`
- `TGFTIT` (N:1) via `CODTIPTIT`

**Padrões de Query**:
```sql
-- Títulos em aberto (não baixados)
SELECT FIN.NUFIN, FIN.DTVENC, FIN.VLRDESDOB, PAR.RAZAOSOCIAL
FROM TGFFIN FIN
INNER JOIN TGFPAR PAR ON PAR.CODPARC = FIN.CODPARC AND PAR.ATIVO = 'S'
WHERE FIN.RECDESP = 1
  AND FIN.PROVISAO = 'N'
  AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99)
  AND FIN.DHBAIXA IS NULL
  AND FIN.DTVENC < TRUNC(SYSDATE)  -- Vencidos
```

---

### TGFTOP - Tipos de Operação

**Descrição**: Tabela que armazena os tipos de operação fiscal.

**Chave Primária Composta**: `CODTIPOPER` + `DHALTER`

**Campos Principais**:
- `CODTIPOPER`: Código do tipo de operação (PK parcial)
- `DHALTER`: Data/hora de alteração (PK parcial)
- `DESCROPER`: Descrição da operação
- `TIPMOV`: Tipo de movimento ('C' = Compra, 'V' = Venda, 'T' = Transferência, 'O' = Pedido, 'E' = Devolução)
- `GRUPO`: Grupo da operação ('COMPRA', 'VENDA', etc.)
- `ATIVO`: Flag de ativo ('S' = Ativo, 'N' = Inativo)

**Padrão de JOIN**:
```sql
-- SEMPRE usar CODTIPOPER + DHTIPOPER para JOIN correto
INNER JOIN TGFTOP TOP ON TOP.CODTIPOPER = CAB.CODTIPOPER 
    AND TOP.DHALTER = CAB.DHTIPOPER
```

---

### TGFTPV - Tipos de Venda

**Descrição**: Tabela que armazena tipos de venda (condições de pagamento).

**Chave Primária**: `CODTIPVENDA`

**Campos Principais**:
- `CODTIPVENDA`: Código do tipo de venda (PK)
- `DESCRICAO`: Descrição do tipo de venda

**Relacionamentos**:
- `TGFPPG` (1:N) via `CODTIPVENDA` - Prazos de pagamento

---

### TGFPPG - Prazos de Pagamento

**Descrição**: Tabela que armazena os prazos de pagamento associados aos tipos de venda.

**Chave Primária**: `CODTIPVENDA` + `PRAZO`

**Campos Principais**:
- `CODTIPVENDA`: Código do tipo de venda (FK para TGFTPV)
- `PRAZO`: Prazo em dias

**Padrões de Query**:
```sql
-- Obter prazo máximo de um tipo de venda
SELECT NVL(MAX(P.PRAZO), 0) AS PRAZO_MAX
FROM TGFPPG P
WHERE P.CODTIPVENDA = :CODTIPVENDA
```

---

### TGFTIT - Tipos de Título

**Descrição**: Tabela que armazena tipos de títulos financeiros.

**Chave Primária**: `CODTIPTIT`

**Campos Principais**:
- `CODTIPTIT`: Código do tipo de título (PK)
- `DESCRICAO`: Descrição do tipo
- `ATIVO`: Flag de ativo ('S' = Ativo, 'N' = Inativo)

**Filtros Obrigatórios**:
```sql
AND TIT.ATIVO = 'S'  -- SEMPRE filtrar apenas tipos ativos
```

---

## 🚚 TABELAS DE LOGÍSTICA

### TGFORD - Ordens de Carga

**Descrição**: Tabela que armazena ordens de carga para logística e expedição.

**Chave Primária**: `ORDEMCARGA` + `CODEMP`

**Campos Principais**:
- `ORDEMCARGA`: Número da ordem de carga (PK parcial)
- `CODEMP`: Código da empresa (PK parcial)
- `CODPARCMOTORISTA`: Código do parceiro motorista (FK para TGFPAR)
- `CODPARCTRANSP`: Código do parceiro transportadora (FK para TGFPAR)
- `DTFATUR`: Data de faturamento
- `DTENTREGA`: Data de entrega

**Relacionamentos**:
- `TGFCAB` (1:N) via `ORDEMCARGA` + `CODEMP`
- `TGFPAR` (N:1) via `CODPARCMOTORISTA` (motorista)
- `TGFPAR` (N:1) via `CODPARCTRANSP` (transportadora)
- `TSIEMP` (N:1) via `CODEMP`

**Padrões de Query**:
```sql
SELECT ORD.ORDEMCARGA, ORD.CODEMP,
       MOT.RAZAOSOCIAL AS MOTORISTA,
       TRANSP.RAZAOSOCIAL AS TRANSPORTADORA
FROM TGFORD ORD
LEFT JOIN TGFPAR MOT ON MOT.CODPARC = ORD.CODPARCMOTORISTA AND MOT.ATIVO = 'S'
LEFT JOIN TGFPAR TRANSP ON TRANSP.CODPARC = ORD.CODPARCTRANSP AND TRANSP.ATIVO = 'S'
WHERE ORD.ORDEMCARGA = :ORDEMCARGA
  AND ORD.CODEMP = :CODEMP
```

---

### TGFVEI - Veículos

**Descrição**: Tabela que armazena cadastro de veículos da frota.

**Chave Primária**: `CODVEICULO`

**Campos Principais**:
- `CODVEICULO`: Código do veículo (PK)
- `PLACA`: Placa do veículo
- `MARCAMODELO`: Marca e modelo do veículo
- `NROFROTA`: Número da frota

**Relacionamentos**:
- `TGFCAB` (1:N) via `CODVEICULO`
- `TCFCHKOPER` (1:N) via `CODVEICULO` (checklists operacionais)

**Padrões de Query**:
```sql
SELECT VEI.CODVEICULO, VEI.PLACA, VEI.MARCAMODELO, VEI.NROFROTA
FROM TGFVEI VEI
WHERE VEI.CODVEICULO = :CODVEICULO
```

---

## ✅ TABELAS DE CHECKLIST

### TCFCHKOPER - Checklist Operacional

**Descrição**: Tabela principal que armazena checklists operacionais realizados.

**Chave Primária**: `NUCHECK`

**Campos Principais**:
- `NUCHECK`: Número único do checklist (PK)
- `CODMOD`: Código do modelo do checklist (FK para TCFMODCHECKLIST)
- `CODEMP`: Código da empresa (FK para TSIEMP)
- `CODUSU`: Código do usuário responsável (FK para TSIUSU)
- `DHINI`: Data e hora de início (Timestamp)
- `STATUS`: Status do checklist ('E' = Encerrado)
- `CODCENCUS`: Código do centro de resultado (FK para TSICUS)
- `CODVEICULO`: Código do veículo (FK para TGFVEI)
- `HORIMETRO`: Horas de funcionamento
- `KM`: Quilometragem
- `ASSINATURA`: Assinatura digital (BLOB)

**Relacionamentos**:
- `TCFMODCHECKLIST` (N:1) via `CODMOD`
- `TSIEMP` (N:1) via `CODEMP`
- `TSIUSU` (N:1) via `CODUSU`
- `TSICUS` (N:1) via `CODCENCUS`
- `TGFVEI` (N:1) via `CODVEICULO`

---

### TCFMODCHECKLIST - Modelos de Checklist

**Descrição**: Tabela que armazena modelos de checklist disponíveis.

**Chave Primária**: `CODMOD`

**Campos Principais**:
- `CODMOD`: Código do modelo (PK)
- `DESCRICAO`: Descrição do modelo
- `ATIVO`: Flag de ativação ('S' = Ativo, 'N' = Inativo)

**Relacionamentos**:
- `TCFCHKOPER` (1:N) via `CODMOD`
- `TCFITECHECKLIST` (1:N) via `CODMOD`

---

### TCFITECHECKLIST - Itens dos Modelos de Checklist

**Descrição**: Tabela que armazena os itens de cada modelo de checklist.

**Chave Primária Composta**: `CODMOD` + `SEQUENCIA`

**Campos Principais**:
- `CODMOD`: Código do modelo (FK para TCFMODCHECKLIST)
- `SEQUENCIA`: Sequência do item (PK parcial)
- `DESCRICAO`: Descrição do item
- `ATIVO`: Flag de ativação ('S' = Ativo, 'N' = Inativo)

**Relacionamentos**:
- `TCFMODCHECKLIST` (N:1) via `CODMOD`

---

## 🗺️ TABELAS AUXILIARES GEOGRÁFICAS

### TSICID - Cidades

**Descrição**: Tabela que armazena cadastro de cidades.

**Chave Primária**: `CODCID`

**Campos Principais**:
- `CODCID`: Código da cidade (PK)
- `NOMECID`: Nome da cidade
- `UF`: Código da UF (FK para TSIUFS)

**Relacionamentos**:
- `TGFPAR` (1:N) via `CODCID`
- `TSIUFS` (N:1) via `UF`

---

### TSIUFS - Unidades Federativas

**Descrição**: Tabela que armazena estados brasileiros.

**Chave Primária**: `CODUF`

**Campos Principais**:
- `CODUF`: Código da UF (PK)
- `UF`: Sigla da UF (ex: 'SP', 'RJ')
- `DESCRICAO`: Descrição do estado

**Relacionamentos**:
- `TSICID` (1:N) via `CODUF`

---

### TSIEND - Endereços

**Descrição**: Tabela que armazena tipos de endereços/logradouros.

**Chave Primária**: `CODEND`

**Campos Principais**:
- `CODEND`: Código do endereço (PK)
- `NOMEEND`: Nome do logradouro

**Relacionamentos**:
- `TGFPAR` (1:N) via `CODEND`

---

### TSIBAI - Bairros

**Descrição**: Tabela que armazena cadastro de bairros.

**Chave Primária**: `CODBAI`

**Campos Principais**:
- `CODBAI`: Código do bairro (PK)
- `NOMEBAI`: Nome do bairro

**Relacionamentos**:
- `TGFPAR` (1:N) via `CODBAI`

---

## 📞 TABELAS DE CONTATOS E COMUNICAÇÃO

### TGFCTT - Contatos

**Descrição**: Tabela que armazena contatos dos parceiros.

**Chave Primária Composta**: `CODPARC` + `CODCONTATO`

**Campos Principais**:
- `CODPARC`: Código do parceiro (FK para TGFPAR)
- `CODCONTATO`: Código do contato (PK parcial)
- `NOMECONTATO`: Nome do contato

**Relacionamentos**:
- `TGFPAR` (N:1) via `CODPARC`

**Padrões de Query**:
```sql
-- Buscar primeiro contato de um parceiro
SELECT NOMECONTATO 
FROM TGFCTT CTT 
WHERE CTT.CODPARC = :CODPARC 
  AND CTT.CODCONTATO = 1
```

---

## 📊 TABELAS DE PREÇOS E TABELAS AUXILIARES

### TGFTAB - Tabelas de Preços

**Descrição**: Tabela que armazena tabelas de preços e suas versões.

**Chave Primária**: `NUTAB`

**Campos Principais**:
- `NUTAB`: Número único da tabela (PK)
- `CODTAB`: Código da tabela de preços
- `DTVIGOR`: Data de vigência

**Padrões de Query**:
```sql
-- Obter tabela de preços vigente
SELECT MAX(NUTAB) AS NUTAB_VIGENTE
FROM TGFTAB
WHERE CODTAB = :CODTAB
  AND DTVIGOR <= TRUNC(SYSDATE)
```

---

### TGFEXC - Exceções (Tabela 04)

**Descrição**: Tabela que armazena exceções e bloqueios (usada como tabela 04).

**Chave Primária**: `NUTAB` + `TIPO` + `CODPROD`

**Campos Principais**:
- `NUTAB`: Número da tabela (ex: 919 para tabela 04)
- `TIPO`: Tipo de exceção ('V' = Venda, 'C' = Compra)
- `CODPROD`: Código do produto (FK para TGFPRO)

**Padrões de Query**:
```sql
-- Verificar se produto está bloqueado na tabela 04
SELECT CODPROD
FROM TGFEXC EXC
WHERE EXC.NUTAB = 919
  AND EXC.TIPO = 'V'
  AND EXC.CODPROD = :CODPROD
```

---

## 🔗 RELACIONAMENTOS E JOINS

### Padrão de JOIN Completo para Notas

```sql
SELECT 
    CAB.NUNOTA, CAB.NUMNOTA, CAB.DTNEG,
    PAR.RAZAOSOCIAL, PAR.CGC_CPF,
    EMP.RAZAOSOCIAL AS EMPRESA, EMP.CGC AS CNPJ_EMPRESA,
    ITE.SEQUENCIA, ITE.QTDNEG, ITE.VLRUNIT, ITE.VLRTOT,
    PRO.DESCRPROD, PRO.REFERENCIA,
    EST.DTFABRICACAO, EST.DTVAL, EST.CONTROLE AS LOTE
FROM TGFCAB CAB
INNER JOIN TGFITE ITE ON ITE.NUNOTA = CAB.NUNOTA
INNER JOIN TGFPRO PRO ON PRO.CODPROD = ITE.CODPROD AND PRO.ATIVO = 'S'
INNER JOIN TGFPAR PAR ON PAR.CODPARC = CAB.CODPARC AND PAR.ATIVO = 'S'
INNER JOIN TSIEMP EMP ON EMP.CODEMP = CAB.CODEMP AND EMP.ATIVO = 'S'
LEFT JOIN TGFEST EST ON EST.CODPROD = ITE.CODPROD 
    AND EST.CODEMP = CAB.CODEMP 
    AND ((EST.CONTROLE IS NULL AND ITE.CONTROLE IS NULL) OR EST.CONTROLE = ITE.CONTROLE)
    AND EST.CODLOCAL = ITE.CODLOCALORIG
    AND EST.ATIVO = 'S'
WHERE CAB.STATUSNOTA = 'L'
  AND CAB.TIPMOV = 'V'  -- Vendas
```

### Padrão de JOIN para Compras com Estoque

```sql
SELECT 
    CAB.NUNOTA, CAB.NUMNOTA,
    ITE.SEQUENCIA, ITE.CODPROD, ITE.QTDNEG,
    PRO.DESCRPROD,
    EST.ESTOQUE, EST.DTFABRICACAO, EST.DTVAL
FROM TGFCAB CAB
INNER JOIN TGFITE ITE ON ITE.NUNOTA = CAB.NUNOTA
INNER JOIN TGFPRO PRO ON PRO.CODPROD = ITE.CODPROD AND PRO.ATIVO = 'S'
INNER JOIN TGFEST EST ON EST.CODPROD = ITE.CODPROD 
    AND EST.CODEMP = CAB.CODEMP 
    AND EST.CONTROLE = ITE.CONTROLE
    AND EST.CODLOCAL = ITE.CODLOCALORIG
    AND EST.ATIVO = 'S'
WHERE CAB.STATUSNOTA = 'L'
  AND CAB.TIPMOV = 'C'  -- Compras
```

---

## 🎯 FILTROS OBRIGATÓRIOS - RESUMO

```sql
-- Notas: SEMPRE filtrar por STATUSNOTA = 'L'
WHERE CAB.STATUSNOTA = 'L'

-- Produtos: SEMPRE filtrar por ATIVO = 'S'
AND PRO.ATIVO = 'S'

-- Empresas: SEMPRE filtrar por ATIVO = 'S'
AND EMP.ATIVO = 'S'

-- Parceiros: SEMPRE filtrar por ATIVO = 'S'
AND PAR.ATIVO = 'S'

-- Estoque: SEMPRE filtrar por ATIVO = 'S'
AND EST.ATIVO = 'S'

-- Vendedores: SEMPRE filtrar por ATIVO = 'S'
AND VEN.ATIVO = 'S'

-- Usuários: SEMPRE filtrar por ATIVO = 'S'
AND USU.ATIVO = 'S'
```

---

## 🔧 FORMATAÇÃO SQL - PADRÕES

### Código de Produto
```sql
-- Priorizar REFERENCIA, fallback para CODPROD com zeros à esquerda (13 dígitos)
NVL(PRO.REFERENCIA, LPAD(TO_CHAR(PRO.CODPROD), 13, '0')) AS CODIGOPRODUTO
```

### CNPJ/CPF
```sql
-- Remover toda formatação
REPLACE(REPLACE(REPLACE(REPLACE(PAR.CGC_CPF, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ
```

### Valores Numéricos
```sql
-- Tratamento de nulos com fallback para zero
NVL(ITE.VLRUNIT, 0) AS VALORUNITARIO
NVL(PRO.PESOLIQ, 0) AS PESO

-- Valores absolutos (para quantidades)
ABS(ITE.QTDNEG) AS QUANTIDADE
```

### Datas
```sql
-- Data de emissão: priorizar DTFATUR, fallback para DTNEG
NVL(CAB.DTFATUR, CAB.DTNEG) AS DTEMISSAO

-- Data de produção: usar campo do estoque
EST.DTFABRICACAO AS DATAPRODUCAO
```

---

## 🔍 PADRÕES AVANÇADOS DE QUERIES

### Análise Temporal com Períodos Mensais

**Padrão**: Agrupar dados por períodos mensais usando `ADD_MONTHS` e `TRUNC`:

```sql
-- Análise de vendas por mês (últimos 12 meses)
SELECT 
    SUM(CASE WHEN CAB.DTMOV BETWEEN TRUNC(ADD_MONTHS(SYSDATE, -12)) AND TRUNC(ADD_MONTHS(SYSDATE - 1, -11)) THEN ITE.QTDNEG ELSE 0 END) AS MES_12,
    SUM(CASE WHEN CAB.DTMOV BETWEEN TRUNC(ADD_MONTHS(SYSDATE, -11)) AND TRUNC(ADD_MONTHS(SYSDATE - 1, -10)) THEN ITE.QTDNEG ELSE 0 END) AS MES_11,
    -- ... continuar para outros meses
FROM TGFITE ITE
INNER JOIN TGFCAB CAB ON CAB.NUNOTA = ITE.NUNOTA
WHERE CAB.STATUSNOTA = 'L'
  AND CAB.TIPMOV = 'V'
```

### Subquery para Buscar Dados de Compras Relacionadas

**Padrão**: Buscar informações de compras relacionadas a uma venda:

```sql
-- Buscar data de produção de compra relacionada
SELECT ITE_COMPRA.AD_DATAPRODUCAO
FROM TGFITE ITE_COMPRA
INNER JOIN TGFCAB CAB_COMPRA ON CAB_COMPRA.NUNOTA = ITE_COMPRA.NUNOTA
WHERE CAB_COMPRA.TIPMOV = 'C'
  AND CAB_COMPRA.STATUSNOTA = 'L'
  AND CAB_COMPRA.CODEMP = CAB.CODEMP
  AND ITE_COMPRA.CODPROD = ITE.CODPROD
  AND ((ITE_COMPRA.CONTROLE IS NULL AND ITE.CONTROLE IS NULL) 
       OR ITE_COMPRA.CONTROLE = ITE.CONTROLE)
  AND ROWNUM = 1
```

### JOIN com Tipo de Operação (Sempre usar DHTIPOPER)

**Padrão**: JOIN correto com TGFTOP sempre requer ambos os campos:

```sql
-- CORRETO: Usar CODTIPOPER + DHTIPOPER
INNER JOIN TGFTOP TOP ON TOP.CODTIPOPER = CAB.CODTIPOPER 
    AND TOP.DHALTER = CAB.DHTIPOPER

-- ERRADO: Apenas CODTIPOPER (pode retornar versão incorreta)
INNER JOIN TGFTOP TOP ON TOP.CODTIPOPER = CAB.CODTIPOPER
```

### Análise de Crédito com Média Ponderada

**Padrão**: Calcular média ponderada de valores por período:

```sql
-- Média ponderada de valores desdobrados (últimos 6 meses)
SELECT 
    ROUND(SNK_DIVIDIR(
        (CASE WHEN VLRDESDOB_01 = 0 THEN 0 ELSE VLRDESDOB_01 * 6 END +
         CASE WHEN VLRDESDOB_02 = 0 THEN 0 ELSE VLRDESDOB_02 * 5 END +
         CASE WHEN VLRDESDOB_03 = 0 THEN 0 ELSE VLRDESDOB_03 * 4 END +
         CASE WHEN VLRDESDOB_04 = 0 THEN 0 ELSE VLRDESDOB_04 * 3 END +
         CASE WHEN VLRDESDOB_05 = 0 THEN 0 ELSE VLRDESDOB_05 * 2 END +
         CASE WHEN VLRDESDOB_06 = 0 THEN 0 ELSE VLRDESDOB_06 * 1 END),
        (CASE WHEN VLRDESDOB_01 = 0 THEN 0 ELSE 6 END +
         CASE WHEN VLRDESDOB_02 = 0 THEN 0 ELSE 5 END +
         CASE WHEN VLRDESDOB_03 = 0 THEN 0 ELSE 4 END +
         CASE WHEN VLRDESDOB_04 = 0 THEN 0 ELSE 3 END +
         CASE WHEN VLRDESDOB_05 = 0 THEN 0 ELSE 2 END +
         CASE WHEN VLRDESDOB_06 = 0 THEN 0 ELSE 1 END)
    ), 2) AS MEDIA_PONDERADA
FROM VW_ANALISE_CREDITO
```

### Query com Múltiplos JOINs para Logística

**Padrão**: JOIN completo para dados de expedição com ordens de carga:

```sql
SELECT 
    CAB.NUNOTA, CAB.NUMNOTA, CAB.DTNEG,
    PAR.RAZAOSOCIAL AS CLIENTE,
    EN.NOMEEND || ', ' || PAR.NUMEND AS ENDERECO,
    CID.NOMECID AS CIDADE,
    UFS.UF AS UF,
    BAI.NOMEBAI AS BAIRRO,
    ORD.ORDEMCARGA,
    MOT.RAZAOSOCIAL AS MOTORISTA,
    TRANSP.RAZAOSOCIAL AS TRANSPORTADORA,
    VEI.PLACA, VEI.MARCAMODELO
FROM TGFCAB CAB
JOIN TGFPAR PAR ON CAB.CODPARC = PAR.CODPARC AND PAR.ATIVO = 'S'
JOIN TSICID CID ON PAR.CODCID = CID.CODCID
JOIN TSIUFS UFS ON CID.UF = UFS.CODUF
JOIN TSIEND EN ON PAR.CODEND = EN.CODEND
JOIN TSIBAI BAI ON PAR.CODBAI = BAI.CODBAI
JOIN TGFTOP TOP ON CAB.CODTIPOPER = TOP.CODTIPOPER AND CAB.DHTIPOPER = TOP.DHALTER
JOIN TGFORD ORD ON ORD.ORDEMCARGA = CAB.ORDEMCARGA AND ORD.CODEMP = CAB.CODEMP
LEFT JOIN TGFPAR MOT ON MOT.CODPARC = ORD.CODPARCMOTORISTA AND MOT.ATIVO = 'S'
LEFT JOIN TGFPAR TRANSP ON TRANSP.CODPARC = ORD.CODPARCTRANSP AND TRANSP.ATIVO = 'S'
LEFT JOIN TGFVEI VEI ON VEI.CODVEICULO = CAB.CODVEICULO
WHERE CAB.STATUSNOTA = 'L'
  AND CAB.DTNEG BETWEEN :PERIODO_INI AND :PERIODO_FIN
```

### Validação de Estoque com Controle de Lote

**Padrão**: JOIN com estoque considerando controle de lote (pode ser NULL):

```sql
LEFT JOIN TGFEST EST ON EST.CODPROD = ITE.CODPROD 
    AND EST.CODEMP = CAB.CODEMP 
    AND ((EST.CONTROLE IS NULL AND ITE.CONTROLE IS NULL) 
         OR EST.CONTROLE = ITE.CONTROLE)
    AND EST.CODLOCAL = ITE.CODLOCALORIG
    AND EST.ATIVO = 'S'
```

### Cálculo de Quantidade com Funções Customizadas

**Padrão**: Usar funções do Sankhya para conversão de unidades:

```sql
-- Calcular volume usando função FC_QTDALT_HL
SELECT SUM(FC_QTDALT_HL(ITE.CODPROD, ITE.QTDNEG, 'HL')) AS VOLUME
FROM TGFITE ITE
WHERE ITE.NUNOTA = :NUNOTA

-- Calcular quantidade em unidade alternativa
SELECT SUM(FC_QTDALT_HL(ITE.CODPROD, ITE.QTDNEG, ITE.CODVOL)) AS QUANTIDADE_ALT
FROM TGFITE ITE
WHERE ITE.NUNOTA = :NUNOTA
```

### Filtros Dinâmicos com Parâmetros Opcionais

**Padrão**: Construir WHERE dinâmico com parâmetros opcionais:

```sql
WHERE CAB.STATUSNOTA = 'L'
  AND (CAB.CODEMP IN :P_CODEMP OR :P_CODEMP IS NULL)
  AND (CAB.CODVEND IN :P_CODVEND OR :P_CODVEND IS NULL)
  AND (CAB.TIPMOV = :P_TIPO OR :P_TIPO = 'T')
  AND (CAB.PENDENTE = :P_PENDENTE OR :P_PENDENTE = 'T')
  AND (CAB.CODPARC = :P_CODPARC OR :P_CODPARC IS NULL)
  AND (CAB.ORDEMCARGA = :P_ORDEMCARGA OR :P_ORDEMCARGA IS NULL)
  AND (CAB.CODVEICULO = :P_CODVEICULO OR :P_CODVEICULO IS NULL)
```

### Análise de Atraso de Pagamento

**Padrão**: Calcular dias de atraso considerando baixa e vencimento:

```sql
SELECT 
    CASE 
        WHEN FIN.DHBAIXA IS NOT NULL THEN FIN.DHBAIXA - FIN.DTVENC
        WHEN FIN.DHBAIXA IS NULL AND FIN.DTVENC < TRUNC(SYSDATE) THEN TRUNC(SYSDATE) - FIN.DTVENC
        ELSE 0 
    END AS DIAS_ATRASO
FROM TGFFIN FIN
WHERE FIN.RECDESP = 1
  AND FIN.PROVISAO = 'N'
  AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99)
  AND FIN.DTNEG >= (SYSDATE - 90)
```

---

## 💡 EXEMPLOS PRÁTICOS DE USO

### Exemplo 1: Buscar Vendas por Período

```java
public Set<VendaDTO> buscarVendasPorPeriodo(Timestamp periodoIni, Timestamp periodoFin) throws Exception {
    String sql = "SELECT CAB.NUNOTA, CAB.NUMNOTA, CAB.DTNEG, PAR.RAZAOSOCIAL " +
                 "FROM TGFCAB CAB " +
                 "INNER JOIN TGFPAR PAR ON PAR.CODPARC = CAB.CODPARC AND PAR.ATIVO = 'S' " +
                 "WHERE CAB.STATUSNOTA = 'L' AND CAB.TIPMOV = 'V'";
    
    return executarQueryComParametros(sql, this::mapearVenda, s -> {
        if (periodoIni != null) {
            s.appendSql(" AND CAB.DTNEG >= :PERIODO_INI");
            s.setNamedParameter("PERIODO_INI", periodoIni);
        }
        if (periodoFin != null) {
            s.appendSql(" AND CAB.DTNEG <= :PERIODO_FIN");
            s.setNamedParameter("PERIODO_FIN", periodoFin);
        }
    });
}
```

### Exemplo 2: Buscar Itens com Produtos e Estoque

```java
public Set<ItemDTO> buscarItensComEstoque(BigDecimal nunota) throws Exception {
    String sql = "SELECT ITE.SEQUENCIA, ITE.CODPROD, ITE.QTDNEG, " +
                 "PRO.DESCRPROD, PRO.REFERENCIA, " +
                 "EST.DTFABRICACAO, EST.DTVAL, EST.CONTROLE AS LOTE " +
                 "FROM TGFITE ITE " +
                 "INNER JOIN TGFPRO PRO ON PRO.CODPROD = ITE.CODPROD AND PRO.ATIVO = 'S' " +
                 "LEFT JOIN TGFEST EST ON EST.CODPROD = ITE.CODPROD " +
                 "  AND EST.CODEMP = (SELECT CODEMP FROM TGFCAB WHERE NUNOTA = :NUNOTA) " +
                 "  AND ((EST.CONTROLE IS NULL AND ITE.CONTROLE IS NULL) OR EST.CONTROLE = ITE.CONTROLE) " +
                 "  AND EST.ATIVO = 'S' " +
                 "WHERE ITE.NUNOTA = :NUNOTA";
    
    return executarQueryComParametros(sql, this::mapearItem, s -> {
        s.setNamedParameter("NUNOTA", nunota);
    });
}
```

### Exemplo 3: Contar Produtos Ativos por Empresa

```java
public BigDecimal contarProdutosAtivos(BigDecimal codemp) throws Exception {
    return executarQueryValorUnico(
        "SELECT COUNT(*) FROM TGFPRO PRO WHERE PRO.ATIVO = 'S'",
        s -> {
            if (codemp != null) {
                s.appendSql(" AND EXISTS (SELECT 1 FROM TGFCAB CAB WHERE CAB.CODEMP = :CODEMP " +
                           "AND EXISTS (SELECT 1 FROM TGFITE ITE WHERE ITE.NUNOTA = CAB.NUNOTA " +
                           "AND ITE.CODPROD = PRO.CODPROD))");
                s.setNamedParameter("CODEMP", codemp);
            }
        }
    );
}
```

---

## ⚠️ BOAS PRÁTICAS

1. **SEMPRE** usar filtros obrigatórios (`STATUSNOTA = 'L'`, `ATIVO = 'S'`)
2. **SEMPRE** usar `INNER JOIN` quando o relacionamento é obrigatório
3. **SEMPRE** usar `LEFT JOIN` quando o relacionamento é opcional
4. **SEMPRE** tratar valores nulos com `NVL()` ou `Optional`
5. **SEMPRE** formatar CNPJ removendo caracteres especiais
6. **SEMPRE** priorizar `REFERENCIA` sobre `CODPROD` para código de produto
7. **SEMPRE** usar `ABS()` para quantidades quando necessário
8. **SEMPRE** validar parâmetros antes de executar queries
9. **SEMPRE** usar `LinkedHashSet<>(1024)` para coleções grandes
10. **SEMPRE** fechar recursos em blocos `finally`

---

---

## 🔧 MÉTODOS DE REPOSITÓRIO - GUIA COMPLETO

### 🎯 VISÃO GERAL

Esta seção descreve todos os métodos disponíveis em `AbstractRepository` e como utilizá-los de forma eficiente e correta.

---

## 📋 INTERFACES FUNCIONAIS

### ResultSetMapper<T>
```java
protected interface ResultSetMapper<T> { 
    T map(ResultSet rs) throws Exception; 
}
```
**Uso**: Mapear uma linha do ResultSet para um objeto DTO.

### QueryExecutor<T>
```java
protected interface QueryExecutor<T> { 
    T executar(NativeSql sqlNative) throws Exception; 
}
```
**Uso**: Executar queries totalmente customizadas com controle total.

### ResultSetExtractor<T>
```java
protected interface ResultSetExtractor<T> { 
    T extract(ResultSet rs) throws Exception; 
}
```
**Uso**: Extrair um valor único do ResultSet.

### SqlConfigurator
```java
protected interface SqlConfigurator { 
    void configurar(NativeSql sql) throws Exception; 
}
```
**Uso**: Configurar parâmetros SQL dinamicamente.

---

## 🚀 MÉTODOS DISPONÍVEIS

### 1. executarQuery() - Query Simples por NUNOTA

**Assinatura**:
```java
protected <T> Set<T> executarQuery(String sql, BigDecimal nunota, ResultSetMapper<T> mapper) throws Exception
```

**Descrição**: Executa uma query que retorna múltiplos registros, filtrando por NUNOTA.

**Parâmetros**:
- `sql`: SQL base (deve conter `WHERE CAB.STATUSNOTA = 'L'`)
- `nunota`: Número único da nota (obrigatório)
- `mapper`: Função para mapear ResultSet para DTO

**Retorno**: `Set<T>` com os resultados

**Exemplo**:
```java
public Set<ExemploDTO> buscarDadosPorNunota(BigDecimal nunota) throws Exception {
    String sql = "SELECT PRO.CODPROD, PRO.DESCRPROD " +
                 "FROM TGFCAB CAB " +
                 "INNER JOIN TGFITE ITE ON ITE.NUNOTA = CAB.NUNOTA " +
                 "INNER JOIN TGFPRO PRO ON PRO.CODPROD = ITE.CODPROD AND PRO.ATIVO = 'S' " +
                 "WHERE CAB.STATUSNOTA = 'L'";
    
    return executarQuery(sql, nunota, rs -> {
        ExemploDTO dto = new ExemploDTO();
        dto.setCodigoProduto(rs.getString("CODPROD"));
        dto.setDescricao(rs.getString("DESCRPROD"));
        return dto;
    });
}
```

**Quando Usar**: Quando você precisa buscar dados relacionados a uma nota específica (NUNOTA).

---

### 2. executarQueryComParametros() - Query com Parâmetros Dinâmicos

**Assinatura**:
```java
protected <T> Set<T> executarQueryComParametros(String sql, ResultSetMapper<T> mapper, SqlConfigurator configurador) throws Exception
```

**Descrição**: Executa uma query com múltiplos parâmetros opcionais configuráveis.

**Parâmetros**:
- `sql`: SQL base
- `mapper`: Função para mapear ResultSet para DTO
- `configurador`: Função para configurar parâmetros SQL (pode ser null)

**Retorno**: `Set<T>` com os resultados

**Exemplo 1 - Com Período**:
```java
public Set<VendaDTO> buscarVendasPorPeriodo(Timestamp periodoIni, Timestamp periodoFin) throws Exception {
    String sql = "SELECT CAB.NUNOTA, CAB.NUMNOTA, CAB.DTNEG " +
                 "FROM TGFCAB CAB " +
                 "WHERE CAB.STATUSNOTA = 'L' AND CAB.TIPMOV = 'V'";
    
    return executarQueryComParametros(sql, this::mapearVenda, sql -> {
        if (periodoIni != null) {
            sql.appendSql(" AND CAB.DTNEG >= :PERIODO_INI");
            sql.setNamedParameter("PERIODO_INI", periodoIni);
        }
        if (periodoFin != null) {
            sql.appendSql(" AND CAB.DTNEG <= :PERIODO_FIN");
            sql.setNamedParameter("PERIODO_FIN", periodoFin);
        }
    });
}
```

**Exemplo 2 - Com Múltiplos Filtros**:
```java
public Set<ProdutoDTO> buscarProdutos(BigDecimal codemp, String descricao) throws Exception {
    String sql = "SELECT PRO.CODPROD, PRO.DESCRPROD " +
                 "FROM TGFPRO PRO " +
                 "WHERE PRO.ATIVO = 'S'";
    
    return executarQueryComParametros(sql, this::mapearProduto, sql -> {
        if (codemp != null) {
            sql.appendSql(" AND EXISTS (SELECT 1 FROM TGFCAB CAB WHERE CAB.CODEMP = :CODEMP " +
                        "AND EXISTS (SELECT 1 FROM TGFITE ITE WHERE ITE.NUNOTA = CAB.NUNOTA " +
                        "AND ITE.CODPROD = PRO.CODPROD))");
            sql.setNamedParameter("CODEMP", codemp);
        }
        if (descricao != null && !descricao.trim().isEmpty()) {
            sql.appendSql(" AND UPPER(PRO.DESCRPROD) LIKE UPPER(:DESCRICAO)");
            sql.setNamedParameter("DESCRICAO", "%" + descricao + "%");
        }
    });
}
```

**Quando Usar**: Quando você precisa de filtros opcionais ou múltiplos parâmetros.

---

### 3. executarQueryCustomizada() - Query Totalmente Customizada

**Assinatura**:
```java
protected <T> T executarQueryCustomizada(QueryExecutor<T> executor) throws Exception
```

**Descrição**: Executa uma query totalmente customizada com controle completo sobre o NativeSql.

**Parâmetros**:
- `executor`: Função que recebe NativeSql e retorna o resultado

**Retorno**: `T` (qualquer tipo)

**Exemplo - Query Complexa com Subqueries**:
```java
public Map<BigDecimal, List<ItemDTO>> buscarItensAgrupadosPorNota(Set<BigDecimal> nunotas) throws Exception {
    return executarQueryCustomizada(sqlNative -> {
        Map<BigDecimal, List<ItemDTO>> resultado = new HashMap<>();
        
        StringBuilder inClause = new StringBuilder();
        for (BigDecimal nunota : nunotas) {
            if (inClause.length() > 0) inClause.append(",");
            inClause.append(nunota);
        }
        
        sqlNative.appendSql("SELECT ITE.NUNOTA, ITE.SEQUENCIA, ITE.CODPROD, ITE.QTDNEG " +
                          "FROM TGFITE ITE " +
                          "WHERE ITE.NUNOTA IN (" + inClause.toString() + ") " +
                          "ORDER BY ITE.NUNOTA, ITE.SEQUENCIA");
        
        try (ResultSet rs = sqlNative.executeQuery()) {
            while (rs.next()) {
                BigDecimal nunota = rs.getBigDecimal("NUNOTA");
                resultado.computeIfAbsent(nunota, k -> new ArrayList<>())
                    .add(mapearItem(rs));
            }
        }
        return resultado;
    });
}
```

**Quando Usar**: Quando você precisa de controle total sobre a query (subqueries complexas, agregações, etc.).

---

### 4. executarQueryValorUnico() - Retornar BigDecimal

**Assinatura**:
```java
protected BigDecimal executarQueryValorUnico(String sql, SqlConfigurator configurador) throws Exception
```

**Descrição**: Executa uma query que retorna um único valor BigDecimal (COUNT, SUM, MAX, etc.).

**Parâmetros**:
- `sql`: SQL que retorna um único valor numérico
- `configurador`: Função para configurar parâmetros SQL (pode ser null)

**Retorno**: `BigDecimal` ou `null` se não houver resultado

**Exemplo 1 - Contar Registros**:
```java
public BigDecimal contarNotasPorPeriodo(Timestamp periodoIni, Timestamp periodoFin) throws Exception {
    return executarQueryValorUnico(
        "SELECT COUNT(*) FROM TGFCAB CAB WHERE CAB.STATUSNOTA = 'L'",
        sql -> {
            if (periodoIni != null) {
                sql.appendSql(" AND CAB.DTNEG >= :PERIODO_INI");
                sql.setNamedParameter("PERIODO_INI", periodoIni);
            }
            if (periodoFin != null) {
                sql.appendSql(" AND CAB.DTNEG <= :PERIODO_FIN");
                sql.setNamedParameter("PERIODO_FIN", periodoFin);
            }
        }
    );
}
```

**Exemplo 2 - Somar Valores**:
```java
public BigDecimal somarValorTotalPorNota(BigDecimal nunota) throws Exception {
    return executarQueryValorUnico(
        "SELECT SUM(ITE.VLRTOT) FROM TGFITE ITE WHERE ITE.NUNOTA = :NUNOTA",
        sql -> sql.setNamedParameter("NUNOTA", nunota)
    );
}
```

**Quando Usar**: Quando você precisa de um único valor numérico (contagem, soma, máximo, etc.).

---

### 5. executarQueryStringUnica() - Retornar String

**Assinatura**:
```java
protected String executarQueryStringUnica(String sql, SqlConfigurator configurador) throws Exception
```

**Descrição**: Executa uma query que retorna um único valor String.

**Parâmetros**:
- `sql`: SQL que retorna um único valor texto
- `configurador`: Função para configurar parâmetros SQL (pode ser null)

**Retorno**: `String` ou `null` se não houver resultado

**Exemplo**:
```java
public String buscarPrimeiroCnpjIndustria(Timestamp periodoIni, Timestamp periodoFin) throws Exception {
    return executarQueryStringUnica(
        "SELECT REPLACE(REPLACE(REPLACE(REPLACE(PAR.CGC_CPF, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ " +
        "FROM TGFPAR PAR " +
        "INNER JOIN TGFCAB CAB_COMPRA ON PAR.CODPARC = CAB_COMPRA.CODPARC AND CAB_COMPRA.TIPMOV = 'C' " +
        "WHERE PAR.ATIVO = 'S' " +
        "AND CAB_COMPRA.STATUSNOTA = 'L' " +
        "AND ROWNUM = 1",
        sql -> {
            if (periodoIni != null) {
                sql.appendSql(" AND CAB_COMPRA.DTNEG >= :PERIODO_INI");
                sql.setNamedParameter("PERIODO_INI", periodoIni);
            }
            if (periodoFin != null) {
                sql.appendSql(" AND CAB_COMPRA.DTNEG <= :PERIODO_FIN");
                sql.setNamedParameter("PERIODO_FIN", periodoFin);
            }
        }
    );
}
```

**Quando Usar**: Quando você precisa de um único valor texto.

---

### 6. executarQueryTimestampUnico() - Retornar Timestamp

**Assinatura**:
```java
protected Timestamp executarQueryTimestampUnico(String sql, SqlConfigurator configurador) throws Exception
```

**Descrição**: Executa uma query que retorna um único valor Timestamp.

**Parâmetros**:
- `sql`: SQL que retorna um único valor data/hora
- `configurador`: Função para configurar parâmetros SQL (pode ser null)

**Retorno**: `Timestamp` ou `null` se não houver resultado

**Exemplo**:
```java
public Timestamp buscarUltimaDataMovimento(BigDecimal codemp) throws Exception {
    return executarQueryTimestampUnico(
        "SELECT MAX(CAB.DTNEG) FROM TGFCAB CAB WHERE CAB.STATUSNOTA = 'L'",
        sql -> {
            if (codemp != null) {
                sql.appendSql(" AND CAB.CODEMP = :CODEMP");
                sql.setNamedParameter("CODEMP", codemp);
            }
        }
    );
}
```

**Quando Usar**: Quando você precisa de um único valor data/hora.

---

## 🎯 MÉTODOS UTILITÁRIOS

### toDate()
```java
protected static Date toDate(Timestamp ts)
```
**Descrição**: Converte Timestamp para Date usando Optional.

**Exemplo**:
```java
dto.setDataEmissao(toDate(rs.getTimestamp("DATAEMISSAO")));
```

### formatarCnpj()
```java
protected static String formatarCnpj(String cnpj)
```
**Descrição**: Remove todos os caracteres não numéricos do CNPJ.

**Exemplo**:
```java
String cnpjLimpo = formatarCnpj(rs.getString("CGC"));
```

### formatarCnpjCompleto()
```java
protected static String formatarCnpjCompleto(String cnpj)
```
**Descrição**: Remove formatação completa (pontos, barras, hífens, espaços).

**Exemplo**:
```java
String cnpjLimpo = formatarCnpjCompleto(rs.getString("CGC_CPF"));
```

---

## 💡 PADRÕES DE USO AVANÇADOS

### Padrão 1: Extrair Mapper para Method Reference

**Antes**:
```java
return executarQuery(sql, nunota, rs -> {
    ExemploDTO dto = new ExemploDTO();
    dto.setCnpj(rs.getString("CNPJ"));
    dto.setCodigoProduto(rs.getString("CODIGOPRODUTO"));
    return dto;
});
```

**Depois**:
```java
return executarQuery(sql, nunota, this::mapearDTO);

private ExemploDTO mapearDTO(ResultSet rs) throws Exception {
    ExemploDTO dto = new ExemploDTO();
    dto.setCnpj(rs.getString("CNPJ"));
    dto.setCodigoProduto(rs.getString("CODIGOPRODUTO"));
    return dto;
}
```

### Padrão 2: Usar Optional para Parâmetros Opcionais

**Antes**:
```java
if (periodoIni != null) {
    sql.appendSql(" AND CAB.DTNEG >= :PERIODO_INI");
    sql.setNamedParameter("PERIODO_INI", periodoIni);
}
```

**Depois**:
```java
Optional.ofNullable(periodoIni).ifPresent(p -> {
    sql.appendSql(" AND CAB.DTNEG >= :PERIODO_INI");
    sql.setNamedParameter("PERIODO_INI", p);
});
```

### Padrão 3: Query com Subquery para Filtros Complexos

```java
public Set<ProdutoDTO> buscarProdutosComVendas(BigDecimal codemp, Timestamp dataIni) throws Exception {
    String sql = "SELECT DISTINCT PRO.CODPROD, PRO.DESCRPROD " +
                 "FROM TGFPRO PRO " +
                 "WHERE PRO.ATIVO = 'S' " +
                 "AND EXISTS (SELECT 1 FROM TGFITE ITE " +
                 "            INNER JOIN TGFCAB CAB ON CAB.NUNOTA = ITE.NUNOTA " +
                 "            WHERE ITE.CODPROD = PRO.CODPROD " +
                 "            AND CAB.STATUSNOTA = 'L' " +
                 "            AND CAB.TIPMOV = 'V'";
    
    return executarQueryComParametros(sql, this::mapearProduto, sql -> {
        if (codemp != null) {
            sql.appendSql(" AND CAB.CODEMP = :CODEMP");
            sql.setNamedParameter("CODEMP", codemp);
        }
        if (dataIni != null) {
            sql.appendSql(" AND CAB.DTNEG >= :DATA_INI");
            sql.setNamedParameter("DATA_INI", dataIni);
        }
        sql.appendSql(")");
    });
}
```

---

## ⚠️ BOAS PRÁTICAS

1. **SEMPRE** usar method references quando o mapper é reutilizado
2. **SEMPRE** validar parâmetros obrigatórios antes de executar queries
3. **SEMPRE** usar `Optional` para parâmetros opcionais
4. **SEMPRE** incluir filtros obrigatórios no SQL base (`STATUSNOTA = 'L'`, `ATIVO = 'S'`)
5. **SEMPRE** usar `LinkedHashSet<>(1024)` para coleções grandes
6. **SEMPRE** tratar valores nulos com `NVL()` no SQL ou `Optional` no Java
7. **SEMPRE** usar `executarQueryCustomizada()` para queries muito complexas
8. **SEMPRE** documentar queries complexas com comentários inline (se necessário)
9. **SEMPRE** usar parâmetros nomeados (`:NOME`) ao invés de posicionais (`?`)
10. **SEMPRE** fechar recursos adequadamente (os métodos já fazem isso automaticamente)

---

## 📚 EXEMPLOS COMPLETOS

Veja `Template/src/br/com/cliente/repository/ExemploRepository.java` para exemplos práticos de uso de todos os métodos.

---

## 📖 REFERÊNCIAS ADICIONAIS

- **[INSTRUCOES_DESENVOLVIMENTO.md](INSTRUCOES_DESENVOLVIMENTO.md)** - Guia completo de desenvolvimento
- **[README.md](README.md)** - Visão geral do Template
- **Projetos Reais**: Denver, PetKids, GuaranaMineiro, Megleo, Eletromac, Iwannasleep

---

**Última Atualização**: 2025-01-02  
**Versão**: 1.0.0 - CONSOLIDADO  
**Status**: ✅ REFERÊNCIA COMPLETA CONSOLIDADA

