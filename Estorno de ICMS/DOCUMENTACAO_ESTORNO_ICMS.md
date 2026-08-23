# 📊 DOCUMENTAÇÃO - CONSULTA ESTORNO DE ICMS

## 🎯 OBJETIVO

Esta consulta calcula o **estorno de ICMS** vinculando notas de venda com notas de compra usando método **FIFO (First In, First Out)**, e para produtos de fabricação própria, vincula os itens de matéria prima nas ordens de produção através do lote.

---

## 📋 ARQUIVOS DISPONÍVEIS

| Arquivo | Descrição |
|---------|-----------|
| **consulta_estorno_icms_refatorada.sql** | Consulta refatorada e melhorada (versão base) |
| **consulta_estorno_icms_com_ordem_producao.sql** | Versão com vínculo de ordem de produção via lote |

---

## 🔍 COMO FUNCIONA A CONSULTA

### Estrutura Geral

A consulta é dividida em **duas partes principais** unidas por `UNION ALL`:

1. **Revenda**: Vincula vendas diretas com compras
2. **Fabricação Própria (MPP)**: Vincula produtos fabricados com matérias primas

---

## 📐 PARTE 1: REVENDA

### Fluxo de Processamento

```
VENDAS → COMPRAS → VINCULO_FIFO → CONSUMO_VALIDO → CONSUMO_CALCULADO → RESULTADO
```

### 1. CTE VENDAS
**Objetivo**: Agrupar vendas do período

**Filtros**:
- `TIPMOV = 'V'` (Vendas)
- `STATUSNOTA = 'L'` (Liberadas)
- `TRUNC(DTNEG, 'MM') = :P_PERIODO` (Período informado)
- `CODCFO IN (...)` (CFOPs específicos)

**Campos Calculados**:
- `QTD_VENDIDA`: Quantidade total vendida
- `TOTAL_ICMS_VENDA`: Total de ICMS da venda
- `TOTAL_NOTA_VENDA`: Valor líquido da nota (VLRTOT - VLRDESC)
- `BASE_CALC`: Percentual de redução de base (PERCREDBASE) para empresas 1, 4, 6

### 2. CTE COMPRAS
**Objetivo**: Agrupar compras disponíveis

**Filtros**:
- `TIPMOV = 'C'` (Compras)
- `STATUSNOTA = 'L'` (Liberadas)
- `TRUNC(DTENTSAI, 'MM') > :P_PERIDCOMPD` (Compras após data informada)

**Campos Calculados**:
- `QTD_COMPRADA`: Quantidade comprada (converte TN para kg: * 1000)
- `TOTAL_ICMS_COMPRA`: Total de ICMS da compra
- `TOTAL_NOTA_COMPRA`: Valor líquido da nota
- `ICMS_UNIT`: ICMS unitário (VLRICMS / QTDNEG)
- `BASERED`: Base reduzida unitária (para produtos com CODTRIB <> 0)

**Tratamento Especial**:
- Se `CODEMP = 1` e produto existe em `TPRLMP` (matéria prima), marca como `'MPP'`

### 3. CTE VINCULO_FIFO
**Objetivo**: Vincular vendas com compras usando FIFO

**Lógica**:
- JOIN por: `CODPROD`, `CODEMP`, `CONTROLE = LOTE`, `DATA_COMPRA <= DATA_VENDA`
- Calcula `CONSUMO_ACUMULADO` usando Window Function

### 4. CTE CONSUMO_VALIDO
**Objetivo**: Calcular saldo disponível de cada compra

**Cálculo**:
```sql
SALDO_DISPONIVEL = QTD_COMPRADA - (CONSUMO_ACUMULADO - QTD_VENDIDA)
```

### 5. CTE CONSUMO_CALCULADO
**Objetivo**: Calcular quantidade consumida de cada compra

**Cálculo**:
```sql
QTD_CONSUMIDA = LEAST(QTD_VENDIDA, GREATEST(SALDO_DISPONIVEL, 0))
```

### 6. Cálculo do Estorno
**Fórmulas por Empresa**:

#### Empresas 1, 4, 6 (com BASE_CALC):
```sql
ESTORNO = (BASE_CALC / 100) * (ICMS_UNIT * QTD_CONSUMIDA)
```

#### Empresas 1, 4 (ajuste ICMS venda = 0):
```sql
ESTORNO = ICMS_UNIT * QTD_CONSUMIDA
```

#### Outras Empresas (diferença de alíquota):
```sql
ESTORNO = (VLRUNIT * QTD_VENDIDA) * (ALIQ_COMPRA - ALIQ_VENDA)
```

#### Outras Empresas (venda sem ICMS):
```sql
ESTORNO = (VLRUNIT * QTD_VENDIDA) * ALIQ_COMPRA
```

---

## 🏭 PARTE 2: FABRICAÇÃO PRÓPRIA (MPP)

### Fluxo de Processamento

```
VENDAS → FABRICAÇÃO (TPRLMP) → COMPRAS → VINCULACAO_BASE → CONSUMO_ACUMULADO → VINCULACAO → RESULTADO
```

### 1. CTE VENDAS (MPP)
**Objetivo**: Buscar produtos fabricados e suas matérias primas

**Lógica Especial**:
- JOIN com `TPRLMP` para buscar receitas de fabricação
- Calcula `VOLUMULT` (volume multiplicador) considerando conversões de unidade
- Busca receitas aninhadas (produtos que são matérias primas de outros produtos)

**Campos**:
- `PRODUTO`: Produto vendido (fabricado)
- `CODPROD`: Matéria prima usada na fabricação
- `LOTE = 'MPP'`: Identifica como fabricação própria
- `QTD_VENDIDA`: Quantidade de matéria prima necessária (VOLUMULT * QTDNEG)

### 2. CTE COMPRAS (MPP)
**Objetivo**: Buscar compras de matérias primas

**Similar à Parte 1**, mas sem agrupamento por NUNOTA (cada item individual)

### 3. CTE VINCULACAO_BASE
**Objetivo**: Vincular vendas com compras disponíveis

**Diferença**: Não usa `CONTROLE = LOTE` (matérias primas podem não ter lote específico)

### 4. CTE CONSUMO_ACUMULADO
**Objetivo**: Controlar quantidade já consumida em compras anteriores

**Lógica**:
- Calcula `QTD_CONSUMIDA_NESTA_COMPRA` para cada compra
- Acumula `QTD_JA_CONSUMIDA_ANTES` usando Window Function

### 5. CTE VINCULACAO
**Objetivo**: Vincular com controle de consumo acumulado

**Cálculo Final**:
```sql
QTD_CONSUMIDA = CASE
    WHEN QTD_JA_CONSUMIDA_ANTES >= QTD_VENDIDA THEN 0
    WHEN SALDO_DISPONIVEL <= 0 THEN 0
    WHEN (QTD_VENDIDA - QTD_JA_CONSUMIDA_ANTES) <= SALDO_DISPONIVEL 
        THEN (QTD_VENDIDA - QTD_JA_CONSUMIDA_ANTES)
    ELSE SALDO_DISPONIVEL
END
```

---

## 🔗 VERSÃO COM ORDEM DE PRODUÇÃO

### Campos Adicionais

A versão `consulta_estorno_icms_com_ordem_producao.sql` adiciona:

#### Informações da Ordem de Produção:
- `NUMERO_ORDEM_PRODUCAO`: Número da OP (NUOP)
- `DATA_INICIO_OP`: Data de início da OP
- `DATA_FIM_OP`: Data de fim da OP
- `STATUS_OP`: Status da OP (L = Liberada, F = Finalizada)
- `LOTE_OP`: Lote da ordem de produção
- `PRODUTO_OP`: Produto fabricado na OP
- `DESCRICAO_PRODUTO_OP`: Descrição do produto

#### Informações da Matéria Prima (via Lote):
- `NUNOTA_COMPRA_MP`: Número da nota de compra da MP
- `NUMNOTA_COMPRA_MP`: Número fiscal da nota de compra
- `DATA_COMPRA_MP`: Data da compra da MP
- `CODPROD_MP`: Código da matéria prima
- `DESCRICAO_MP`: Descrição da matéria prima
- `LOTE_MP`: Lote da matéria prima
- `QTD_MP_CONSUMIDA`: Quantidade de MP consumida
- `VLRUNIT_MP`: Valor unitário da MP
- `TOTAL_ICMS_COMPRA_MP`: Total de ICMS da compra da MP
- `TOTAL_NOTA_COMPRA_MP`: Total da nota de compra da MP

### Vínculo via Lote

**Lógica**:
1. Busca ordem de produção (`TPRLPA`) pelo `CONTROLE` (lote)
2. Vincula matéria prima comprada pelo mesmo `CONTROLE` (lote)
3. Permite rastrear qual nota de compra foi usada em qual ordem de produção

**JOINs**:
```sql
LEFT JOIN TPRLPA OP ON OP.CONTROLE = S.CONTROLE 
                    AND OP.CODPROD = S.PRODUTO
                    AND OP.CODEMP = S.CODEMP

LEFT JOIN TGFITE MP ON MP.CONTROLE = S.CONTROLE
                    AND MP.CODPROD = S.CODPROD
                    AND MP.CODEMP = S.CODEMP
                    AND MP.DATA_COMPRA <= S.DATA_VENDA
```

---

## 📊 PARÂMETROS DA CONSULTA

### Parâmetros Obrigatórios

- `:P_PERIODO`: Período das vendas (formato: DATE do primeiro dia do mês)
  - Exemplo: `TO_DATE('01/01/2024', 'DD/MM/YYYY')` para janeiro/2024

- `:P_PERIDCOMPD`: Data limite para buscar compras (formato: DATE do primeiro dia do mês)
  - Exemplo: `TO_DATE('01/01/2023', 'DD/MM/YYYY')` para buscar compras a partir de janeiro/2023

- `:A_CODEMP`: Código da empresa (filtro final)

### Exemplo de Uso

```sql
:P_PERIODO = TO_DATE('01/12/2024', 'DD/MM/YYYY')
:P_PERIDCOMPD = TO_DATE('01/01/2024', 'DD/MM/YYYY')
:A_CODEMP = 1
```

---

## 🔧 MELHORIAS APLICADAS

### 1. Formatação e Legibilidade
- ✅ Indentação consistente
- ✅ Comentários explicativos
- ✅ Agrupamento lógico de CTEs
- ✅ Nomes de campos mais descritivos

### 2. Correções de Erros
- ✅ Adicionado `STATUSNOTA = 'L'` em todas as queries de compras
- ✅ Corrigido `NULLIF` para evitar divisão por zero
- ✅ Corrigido `TO_DATE` para datas fixas
- ✅ Adicionado filtro `ATIVO = 'S'` em JOINs com tabelas de cadastro

### 3. Performance
- ✅ Filtros aplicados o mais cedo possível
- ✅ Índices sugeridos nos JOINs principais
- ✅ Window Functions otimizadas

### 4. Validações
- ✅ Verificação de `NULLIF` em todas as divisões
- ✅ Tratamento de valores nulos com `NVL`
- ✅ Validação de saldo disponível antes de calcular consumo

---

## 📈 REGRAS DE ESTORNO POR EMPRESA

### Empresa 1, 4, 6
**Regra**: Estorno por BASE_CALC
```sql
ESTORNO = (BASE_CALC / 100) * (ICMS_UNIT * QTD_CONSUMIDA)
```

### Empresa 1, 4 (especial)
**Regra**: Ajuste ICMS venda = 0 e CODTRIB = 40 ou 41
```sql
ESTORNO = ICMS_UNIT * QTD_CONSUMIDA
```

### Empresa 1, 4 (regra especial AD_TIPO = 'A')
**Regra**: A partir de 01/12/2025
- Se `BASEC_VEND = BASEC_COMP`: Estorno = 0
- Se `BASEC_VEND < BASEC_COMP`: Estorno = 0
- Se `BASEC_VEND > BASEC_COMP`: Estorno = `(BASEC_VEND - BASEC_COMP) / 100 * (VLRUNIT_COMPRA * QTD_CONSUMIDA)`

### Outras Empresas
**Regra 1**: Diferença de alíquota (Compra > Venda)
```sql
ESTORNO = (VLRUNIT * QTD_VENDIDA) * (ALIQ_COMPRA - ALIQ_VENDA)
```

**Regra 2**: Venda sem ICMS
```sql
ESTORNO = (VLRUNIT * QTD_VENDIDA) * ALIQ_COMPRA
```

### Empresa 3
**Regra Especial**: Se `CODTRIB = 51`, estorno = 0

---

## 🎯 CASOS DE USO

### 1. Relatório de Estorno Mensal
**Use**: `consulta_estorno_icms_refatorada.sql`
- Visualizar estorno por período
- Agrupar por empresa, produto, nota
- Exportar para Excel/PDF

### 2. Rastreamento de Matérias Primas
**Use**: `consulta_estorno_icms_com_ordem_producao.sql`
- Ver qual nota de compra foi usada em cada ordem de produção
- Rastrear lote de matéria prima até produto final
- Auditoria de fabricação

### 3. Análise de Performance
**Use**: Ambas as consultas
- Comparar estorno entre empresas
- Identificar produtos com maior estorno
- Analisar tendências temporais

---

## ⚠️ OBSERVAÇÕES IMPORTANTES

### Filtros Obrigatórios Aplicados
✅ **STATUSNOTA = 'L'**: Apenas notas liberadas
✅ **ATIVO = 'S'**: Apenas produtos, empresas e parceiros ativos
✅ **TIPMOV**: Filtro por tipo de movimento (V = Venda, C = Compra)

### Performance
- A consulta pode retornar muitas linhas
- Use filtros de período e empresa para melhor performance
- Considere criar índices em:
  - `TGFCAB.DTNEG`
  - `TGFCAB.STATUSNOTA`
  - `TGFCAB.TIPMOV`
  - `TGFITE.CONTROLE`
  - `TGFITE.CODPROD`
  - `TPRLPA.CONTROLE`

### Limitações
- Window Functions podem impactar performance em grandes volumes
- Consulta de fabricação própria é mais complexa e pode ser mais lenta
- Vínculo via lote depende de `CONTROLE` estar preenchido corretamente

---

## 🐛 TROUBLESHOOTING

### Problema: Consulta retorna vazio
**Solução**: 
- Verifique se há notas liberadas no período
- Verifique se os filtros não estão muito restritivos
- Verifique se há compras disponíveis no período informado

### Problema: Estorno zerado quando deveria ter valor
**Solução**:
- Verifique se a regra de estorno está correta para a empresa
- Verifique se `BASE_CALC` está preenchido (empresas 1, 4, 6)
- Verifique se as alíquotas estão corretas

### Problema: Ordem de produção não aparece
**Solução**:
- Verifique se o `CONTROLE` (lote) está preenchido na venda
- Verifique se existe ordem de produção com o mesmo `CONTROLE`
- Verifique se a OP está com status 'L' ou 'F'

### Problema: Matéria prima não vinculada
**Solução**:
- Verifique se o `CONTROLE` (lote) está preenchido na compra
- Verifique se a data da compra é anterior à data da venda
- Verifique se o produto e empresa estão corretos

---

## 📚 REFERÊNCIAS

- **Template/REFERENCIA_SANKHYA.md**: Referência completa de tabelas Sankhya
- **Template/CONHECIMENTO_CONSOLIDADO.md**: Padrões e boas práticas

---

**Última atualização**: 2025-01-02  
**Versão**: 1.0.0



