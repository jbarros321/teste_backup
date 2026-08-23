# 📊 ANALYTICS - Consultas e Relatórios

Esta pasta contém consultas SQL e documentação para análises e relatórios no Analytics do Sankhya.

---

## 📁 ESTRUTURA DA PASTA

```
Analytics/
├── README.md                                    # Este arquivo
├── GUIA_RAPIDO.md                               # Guia rápido de uso
├── DOCUMENTACAO_CONSULTA_GERENTE_ONLINE.md      # Documentação gerente online
├── DOCUMENTACAO_GOL_GERENTE_ONLINE.md          # Documentação GOL (Gerente Online)
├── consulta_gerente_online.sql                  # Consulta detalhada
├── consulta_gerente_online_resumida.sql        # Consulta resumida
├── consulta_GOL_gerente_online.sql             # Consulta GOL detalhada
├── consulta_GOL_gerente_online_resumida.sql    # Consulta GOL resumida
└── consulta_GOL_hierarquia_equipe.sql          # Consulta hierarquia GOL
```

---

## 🎯 CONSULTAS DISPONÍVEIS

### 1. Consulta Gerente Online (Geral)
**Descrição**: Análise completa de gerentes de vendas e suas equipes.

**Arquivos**:
- `consulta_gerente_online.sql` - Versão detalhada (uma linha por nota)
- `consulta_gerente_online_resumida.sql` - Versão resumida (uma linha por gerente)

**Documentação**: `DOCUMENTACAO_CONSULTA_GERENTE_ONLINE.md`

**Guia Rápido**: `GUIA_RAPIDO.md`

---

### 2. Consulta GOL (Gerente Online) - Módulo Sankhya
**Descrição**: Consultas específicas para o módulo **GOL (Gerente Online)** do Sankhya, incluindo hierarquia completa: Gerente GOL → Supervisor → Vendedor.

**Arquivos**:
- `consulta_GOL_gerente_online.sql` - Versão detalhada com hierarquia completa
- `consulta_GOL_gerente_online_resumida.sql` - Versão resumida por gerente GOL
- `consulta_GOL_hierarquia_equipe.sql` - Hierarquia completa com métricas por nível

**Documentação**: `DOCUMENTACAO_GOL_GERENTE_ONLINE.md`

**Características Especiais**:
- ✅ Inclui informações de **Supervisor** (campo AD_SUPERVISOR)
- ✅ Hierarquia completa: Gerente GOL → Supervisor → Vendedor
- ✅ Métricas calculadas por cada nível da hierarquia
- ✅ Ideal para gestão de equipes de vendas online

---

## 🚀 COMO USAR

### Passo 1: Escolha a Consulta
- **Detalhada**: Para análises específicas e relatórios detalhados
- **Resumida**: Para dashboards e visão executiva

### Passo 2: Copie o SQL
Abra o arquivo `.sql` e copie todo o conteúdo.

### Passo 3: Cole no Analytics
1. Acesse o Analytics do Sankhya
2. Vá em **Consultas** → **Nova Consulta SQL**
3. Cole o SQL copiado
4. Configure os parâmetros obrigatórios:
   - `DATA_INICIO`: Data inicial
   - `DATA_FIM`: Data final

### Passo 4: Execute e Visualize
1. Clique em **Executar**
2. Visualize os resultados
3. Crie gráficos/dashboards conforme necessário

---

## 📚 DOCUMENTAÇÃO

| Arquivo | Descrição |
|---------|-----------|
| **README.md** | Visão geral da pasta (este arquivo) |
| **GUIA_RAPIDO.md** | Guia rápido de uso (3 passos) |
| **DOCUMENTACAO_CONSULTA_GERENTE_ONLINE.md** | Documentação gerente online (geral) |
| **DOCUMENTACAO_GOL_GERENTE_ONLINE.md** | Documentação GOL (Gerente Online Sankhya) |

---

## 🔧 REQUISITOS

### Tabelas Utilizadas
- `TGFCAB` - Cabeçalho de Notas
- `TGFVEN` - Vendedores
- `TGFPAR` - Parceiros/Clientes
- `TSIEMP` - Empresas
- `TGFTOP` - Tipos de Operação

### Permissões Necessárias
- Acesso de leitura às tabelas acima
- Permissão para executar consultas SQL no Analytics

---

## 📊 EXEMPLOS DE USO

### Dashboard de Performance
Use `consulta_gerente_online_resumida.sql` para criar:
- Ranking de gerentes por valor vendido
- Comparação de equipes
- Gráficos de evolução temporal

### Relatório Detalhado
Use `consulta_gerente_online.sql` para criar:
- Relatório de vendas por gerente
- Análise de vendedores individuais
- Rastreamento de clientes

---

## ⚠️ OBSERVAÇÕES IMPORTANTES

### Filtros Aplicados
✅ Apenas notas liberadas (`STATUSNOTA = 'L'`)
✅ Apenas vendedores ativos (`ATIVO = 'S'`)
✅ Apenas clientes ativos (`ATIVO = 'S'`)
✅ Apenas empresas ativas (`ATIVO = 'S'`)

### Performance
- Consulta detalhada pode retornar muitas linhas
- Use filtros de período e gerente para melhor performance
- Prefira consulta resumida para dashboards

### Parâmetros
- `DATA_INICIO` e `DATA_FIM` são obrigatórios
- Parâmetros opcionais podem ser `NULL` para não filtrar

---

## 🆘 SUPORTE

### Problemas Comuns
Consulte a seção **TROUBLESHOOTING** em:
- `DOCUMENTACAO_CONSULTA_GERENTE_ONLINE.md` - Para consultas gerais
- `DOCUMENTACAO_GOL_GERENTE_ONLINE.md` - Para consultas GOL específicas

### Documentação Sankhya
- `Template/REFERENCIA_SANKHYA.md` - Referência de tabelas
- `Template/CONHECIMENTO_CONSOLIDADO.md` - Padrões e boas práticas

---

## 📝 PRÓXIMAS CONSULTAS

Esta pasta será expandida com novas consultas conforme necessário:
- [ ] Consulta de produtos mais vendidos
- [ ] Consulta de clientes por região
- [ ] Consulta de performance de vendedores
- [ ] Consulta de estoque e movimentações

---

**Última atualização**: 2025-01-02  
**Versão**: 1.0.0
