# ✅ CORREÇÕES APLICADAS - TABELA TPRLPA

## 🔴 PROBLEMA IDENTIFICADO

A consulta estava usando **campos incorretos** da tabela `TPRLPA`:

### ❌ Campos Usados (INCORRETOS)
- `NUOP` - Não existe
- `DTINICIO` - Não existe
- `DTFIM` - Não existe
- `STATUS` - Não existe
- `CONTROLE` - Não existe
- `CODPROD` - Não existe
- `CODEMP` - Não existe

---

## ✅ CORREÇÃO APLICADA

### Campos Corretos da TPRLPA

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `IDPROC` | Número | ID do processo de produção |
| `CODPRODPA` | Número | Código do produto para produção |
| `CONTROLEPA` | String | **Controle/Lote da ordem de produção** |
| `TAMLOTEPAD` | Número | Tamanho do lote padrão |
| `MULTIDEAL` | Número | Multiplicador deal |
| `QTDPRODMIN` | Número | Quantidade produto mínimo |
| `UNTEMPOATRAVESS` | String | Unidade tempo atravessamento |
| `TEMPOATRAVESS` | Número | Tempo atravessamento |
| `BASCALCDTVAL` | Número | Base cálculo data validade |
| `TIPOGERASERIE` | String | Tipo geração série |
| `MASCSERIE` | String | Máscara série |
| `TIPOTEMPO` | String | Tipo tempo |
| `TEMPOFIXO` | Número | Tempo fixo |
| `IDFORMULA` | Número | ID da fórmula |
| `CODLOCDEST` | Número | Código local destino |
| `CODUSUALT` | Número | Código usuário alteração |
| `DHALTER` | Timestamp | **Data/hora alteração** |
| `DHCAD` | Timestamp | **Data/hora cadastro** |
| `CODUSUCAD` | Número | Código usuário cadastro |

---

## 🔧 VÍNCULOS CORRIGIDOS

### Vínculo Ordem de Produção

**Antes (INCORRETO)**:
```sql
LEFT JOIN TPRLPA OP ON OP.CONTROLE = S.CONTROLE 
                    AND OP.CODPROD = S.PRODUTO
                    AND OP.CODEMP = S.CODEMP
```

**Depois (CORRETO)**:
```sql
LEFT JOIN (
    SELECT DISTINCT
        OP.IDPROC,
        OP.CODPRODPA,
        OP.CONTROLEPA,
        OP.TAMLOTEPAD,
        OP.MULTIDEAL,
        OP.QTDPRODMIN,
        OP.DHALTER,
        OP.DHCAD,
        PRO_OP.DESCRPROD AS DESCRICAO_PRODUTO_OP
    FROM TPRLPA OP
    INNER JOIN TGFPRO PRO_OP ON PRO_OP.CODPROD = OP.CODPRODPA AND PRO_OP.ATIVO = 'S'
    WHERE OP.CONTROLEPA IS NOT NULL
      AND NVL(OP.CONTROLEPA, ' ') <> ' '
) OP ON OP.CONTROLEPA = S.CONTROLE
    AND OP.CODPRODPA = S.PRODUTO
```

### Vínculo Matéria Prima

**Antes (INCORRETO)**:
```sql
MP ON MP.CONTROLE = S.CONTROLE
    AND MP.CODPROD = S.CODPROD
    AND MP.CODEMP = S.CODEMP
```

**Depois (CORRETO)**:
```sql
MP ON MP.CONTROLE = OP.CONTROLEPA  -- ✅ Vincular MP com lote da OP
    AND MP.CODPROD = S.CODPROD
    AND MP.CODEMP = S.CODEMP
```

**Explicação**:
- A matéria prima (MP) deve vincular com o **lote da ordem de produção** (`OP.CONTROLEPA`)
- Isso permite rastrear qual nota de compra da MP foi usada em qual OP

---

## 📊 CAMPOS RETORNADOS (AJUSTADOS)

### Informações da Ordem de Produção

| Campo | Descrição |
|-------|-----------|
| `ID_PROCESSO_PRODUCAO` | ID do processo (IDPROC) |
| `PRODUTO_ORDEM_PRODUCAO` | Produto da OP (CODPRODPA) |
| `LOTE_ORDEM_PRODUCAO` | **Lote da OP (CONTROLEPA)** - usado para vínculo |
| `TAMANHO_LOTE_PADRAO` | Tamanho lote padrão |
| `MULTIPLICADOR_DEAL` | Multiplicador deal |
| `QTDE_PRODUTO_MINIMA` | Quantidade produto mínimo |
| `DATA_HORA_ALTERACAO_OP` | Data/hora última alteração |
| `DATA_HORA_CADASTRO_OP` | Data/hora cadastro |
| `DESCRICAO_PRODUTO_OP` | Descrição do produto fabricado |

### Informações da Matéria Prima

| Campo | Descrição |
|-------|-----------|
| `NUNOTA_COMPRA_MP` | Número da nota de compra da MP |
| `NUMNOTA_COMPRA_MP` | Número fiscal da nota de compra |
| `DATA_COMPRA_MP` | Data da compra da MP |
| `CODPROD_MP` | Código da matéria prima |
| `DESCRICAO_MP` | Descrição da matéria prima |
| `LOTE_MP` | Lote da matéria prima (CONTROLE) |
| `QTD_MP_CONSUMIDA` | Quantidade de MP consumida |
| `VLRUNIT_MP` | Valor unitário da MP |
| `TOTAL_ICMS_COMPRA_MP` | Total de ICMS da compra da MP |
| `TOTAL_NOTA_COMPRA_MP` | Total da nota de compra da MP |

---

## 🔗 LÓGICA DE VÍNCULO

### Fluxo de Rastreamento

```
VENDA (TGFITE)
    ↓ CONTROLE (lote)
ORDEM DE PRODUÇÃO (TPRLPA)
    ↓ CONTROLEPA (lote)
MATÉRIA PRIMA COMPRADA (TGFITE)
    ↓ CONTROLE (lote)
NOTA DE COMPRA (TGFCAB)
```

### Como Funciona

1. **Venda tem lote** (`TGFITE.CONTROLE`)
2. **Ordem de produção tem mesmo lote** (`TPRLPA.CONTROLEPA`)
3. **Matéria prima comprada tem mesmo lote** (`TGFITE.CONTROLE`)
4. **Resultado**: Rastreamento completo: Venda → OP → MP → Nota Compra

---

## ✅ VALIDAÇÕES ADICIONADAS

### Filtros Aplicados

1. **TPRLPA**:
   - ✅ `CONTROLEPA IS NOT NULL` - Apenas OPs com lote
   - ✅ `NVL(CONTROLEPA, ' ') <> ' '` - Lote não vazio

2. **Matéria Prima**:
   - ✅ `CONTROLE IS NOT NULL` - Apenas MPs com lote
   - ✅ `NVL(CONTROLE, ' ') <> ' '` - Lote não vazio

3. **Produtos**:
   - ✅ `ATIVO = 'S'` - Apenas produtos ativos

---

## 🎯 BENEFÍCIOS

### Rastreabilidade Completa
✅ Permite rastrear: **Venda → OP → MP → Nota Compra**

### Vínculo Correto
✅ Usa campos corretos da tabela TPRLPA
✅ Vincula via lote (`CONTROLEPA`)
✅ Permite buscar qual nota de compra foi usada

### Informações Detalhadas
✅ Informações completas da OP
✅ Informações completas da MP
✅ Rastreamento via lote

---

## ⚠️ OBSERVAÇÕES IMPORTANTES

### Condições para Vínculo
- ✅ A venda deve ter `CONTROLE` (lote) preenchido
- ✅ Deve existir OP com `CONTROLEPA` igual ao lote da venda
- ✅ A MP deve ter `CONTROLE` igual ao `CONTROLEPA` da OP
- ✅ A data da compra da MP deve ser anterior à data da venda

### Se não vincular
- Se `CONTROLEPA` estiver vazio na OP → Não vincula
- Se `CONTROLE` estiver vazio na MP → Não vincula
- Se não houver OP com mesmo lote → Não vincula

---

## 📝 RESUMO DAS MUDANÇAS

| Antes | Depois |
|-------|--------|
| ❌ Campos inexistentes | ✅ Campos corretos da TPRLPA |
| ❌ Vínculo incorreto | ✅ Vínculo via `CONTROLEPA` |
| ❌ MP vinculada com venda | ✅ MP vinculada com OP via lote |
| ❌ Sem validações | ✅ Validações de lote não vazio |

---

**Última atualização**: 2025-01-02  
**Status**: ✅ Campos corrigidos - Vínculo ajustado



