# ⚡ GUIA RÁPIDO - CONSULTA GERENTE ONLINE

## 🎯 ESCOLHA SUA CONSULTA

### 📊 Consulta Detalhada
**Arquivo**: `consulta_gerente_online.sql`
- ✅ Uma linha por nota/pedido
- ✅ Informações completas de cada venda
- ✅ Ideal para: relatórios detalhados, análises específicas

### 📈 Consulta Resumida
**Arquivo**: `consulta_gerente_online_resumida.sql`
- ✅ Uma linha por gerente
- ✅ Totais e métricas agregadas
- ✅ Ideal para: dashboards, rankings, visão executiva

---

## 🚀 COMO USAR (3 PASSOS)

### 1️⃣ Copie o SQL
Abra o arquivo `.sql` escolhido e copie todo o conteúdo.

### 2️⃣ Cole no Analytics
1. Vá em **Consultas** → **Nova Consulta SQL**
2. Cole o SQL copiado
3. Configure os parâmetros:

```
DATA_INICIO = 01/01/2024
DATA_FIM = 31/01/2024
P_CODEMP = NULL (ou código específico)
P_CODGERENTE = NULL (ou código específico)
```

### 3️⃣ Execute e Visualize
1. Clique em **Executar**
2. Visualize os resultados
3. Crie gráficos/dashboards conforme necessário

---

## 📋 PARÂMETROS

| Parâmetro | Obrigatório | Descrição | Exemplo |
|-----------|-------------|-----------|---------|
| `DATA_INICIO` | ✅ Sim | Data inicial | `01/01/2024` |
| `DATA_FIM` | ✅ Sim | Data final | `31/01/2024` |
| `P_CODEMP` | ❌ Não | Filtrar por empresa | `1` ou `NULL` |
| `P_CODGERENTE` | ❌ Não | Filtrar por gerente | `123` ou `NULL` |
| `P_CODVENDEDOR` | ❌ Não | Filtrar por vendedor | `456` ou `NULL` |
| `P_TIPMOV` | ❌ Não | Tipo movimento | `'V'`, `'P'` ou `'T'` |

---

## 💡 EXEMPLOS PRÁTICOS

### Exemplo 1: Todos os Gerentes em Janeiro/2024
```
DATA_INICIO = 01/01/2024
DATA_FIM = 31/01/2024
P_CODEMP = NULL
P_CODGERENTE = NULL
P_TIPMOV = 'T'
```
**Resultado**: Ranking de todos os gerentes com totais do mês

### Exemplo 2: Gerente Específico - Detalhado
```
DATA_INICIO = 01/01/2024
DATA_FIM = 31/01/2024
P_CODEMP = NULL
P_CODGERENTE = 123
P_TIPMOV = 'V'
```
**Resultado**: Todas as vendas detalhadas do gerente 123

### Exemplo 3: Vendedor Específico
```
DATA_INICIO = 01/01/2024
DATA_FIM = 31/01/2024
P_CODEMP = NULL
P_CODGERENTE = NULL
P_CODVENDEDOR = 456
P_TIPMOV = 'T'
```
**Resultado**: Todas as notas do vendedor 456 com informações do gerente

---

## 📊 CAMPOS PRINCIPAIS RETORNADOS

### Consulta Detalhada
- ✅ Código e nome do gerente
- ✅ Código e nome do vendedor
- ✅ Código e nome do cliente
- ✅ Dados da nota (número, data, valor)
- ✅ Métricas calculadas (totais por mês)

### Consulta Resumida
- ✅ Código e nome do gerente
- ✅ Total de vendedores na equipe
- ✅ Total de notas e valor total
- ✅ Valor médio por nota
- ✅ Totais por tipo (vendas/pedidos)

---

## ⚠️ IMPORTANTE

✅ **Sempre use**: Filtros de período (DATA_INICIO e DATA_FIM)
✅ **Performance**: Use consulta resumida para dashboards
✅ **Detalhamento**: Use consulta detalhada para análises específicas

---

## 📖 DOCUMENTAÇÃO COMPLETA

Para mais detalhes, consulte:
- **DOCUMENTACAO_CONSULTA_GERENTE_ONLINE.md** - Documentação completa
- **README.md** - Visão geral da pasta Analytics

---

**Pronto para usar! 🚀**



