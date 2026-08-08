# TOTVS Protheus - Documentação

ERP **multi-segmento** da TOTVS, desenvolvido em AdvPL/TL++.

- **Banco de Dados**: SQL Server, Oracle ou PostgreSQL
- **Prefixo de tabelas**: 3 caracteres (ex: `SA1`, `SC5`, `SE1`)
- **Prefixo de campos**: 2 chars + `_` (ex: `A1_NOME`, `C5_NUM`)
- **Exclusão lógica**: Campo `D_E_L_E_T_` (` ` = ativo, `*` = excluído)

---

## Documentos

| # | Documento | Descrição |
|:--|:---|:---|
| 01 | [Visão Geral](./01_VISAO_GERAL.md) | Arquitetura, módulos (SIGA*), dicionário de dados |
| 02 | [Tabelas Principais](./02_TABELAS_PRINCIPAIS.md) | SA1, SB1, SC5, SE1, SE2 — campos e relacionamentos |
| 03 | [Views e Consultas SQL](./03_VIEWS_E_CONSULTAS_SQL.md) | Consultas práticas com filtro D_E_L_E_T_ |
| 04 | [APIs e Integrações](./04_APIS_E_INTEGRACOES.md) | REST API, OAuth, endpoints, AdvPL customizado |
| 05 | [Fluxo de Pedidos](./05_FLUXO_PEDIDOS.md) | MATA410 → Liberação → NF → Financeiro |
| 06 | [Financeiro](./06_FINANCEIRO.md) | SIGAFIN: SE1, SE2, SE5, rotinas FINA* |
| 07 | [Clientes](./07_CLIENTES.md) | SA1: cadastro, tipos, crédito, consultas |
| 08 | [Estoque e Compras](./08_ESTOQUE_E_COMPRAS.md) | SIGAEST/SIGACOM: SB2, SC7, movimentações |
