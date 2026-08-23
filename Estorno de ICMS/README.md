# 📊 ESTORNO DE ICMS

Esta pasta contém consultas SQL para cálculo de **estorno de ICMS**, vinculando notas de venda com notas de compra usando método **FIFO** e incluindo vínculo com ordens de produção via lote.

---

## 📁 ARQUIVOS DISPONÍVEIS

| Arquivo | Descrição |
|---------|-----------|
| **consulta_estorno_icms_refatorada.sql** | Consulta refatorada e melhorada (versão base) |
| **consulta_estorno_icms_com_ordem_producao.sql** | Versão com vínculo de ordem de produção via lote |
| **DOCUMENTACAO_ESTORNO_ICMS.md** | Documentação completa explicando como funciona |
| **README.md** | Este arquivo (visão geral) |

---

## 🎯 FUNCIONALIDADES

### ✅ Cálculo de Estorno de ICMS
- Vincula vendas com compras usando método **FIFO**
- Calcula estorno conforme regras específicas por empresa
- Trata produtos de **revenda** e **fabricação própria**

### ✅ Vínculo com Ordens de Produção
- Vincula matérias primas com ordens de produção via **lote**
- Permite rastrear qual nota de compra foi usada em cada OP
- Mostra informações completas da OP e da MP

---

## 🚀 COMO USAR

### Parâmetros Obrigatórios

```sql
:P_PERIODO = TO_DATE('01/12/2024', 'DD/MM/YYYY')  -- Período das vendas
:P_PERIDCOMPD = TO_DATE('01/01/2024', 'DD/MM/YYYY')  -- Data limite compras
:A_CODEMP = 1  -- Código da empresa
```

### Exemplo de Execução

1. Abra a consulta SQL no Analytics ou SQL Developer
2. Configure os parâmetros
3. Execute a consulta
4. Visualize os resultados

---

## 📊 DIFERENÇAS ENTRE VERSÕES

| Versão | Características |
|--------|----------------|
| **Refatorada** | Versão base melhorada, sem vínculo de OP |
| **Com Ordem de Produção** | Inclui vínculo de OP e MP via lote |

---

## 📚 DOCUMENTAÇÃO COMPLETA

Consulte **DOCUMENTACAO_ESTORNO_ICMS.md** para:
- Explicação detalhada de como funciona
- Estrutura das CTEs
- Regras de cálculo por empresa
- Troubleshooting

---

**Última atualização**: 2025-01-02



