# TOTVS Protheus - Visão Geral

## Sobre o Sistema

O **TOTVS Protheus** é o principal ERP da TOTVS, desenvolvido em linguagem **AdvPL/TL++**. Atende múltiplos segmentos (indústria, serviços, varejo, agro, saúde) e possui mais de 50 módulos integrados.

## Arquitetura

| Componente | Tecnologia |
|:---|:---|
| **Banco de Dados** | SQL Server, Oracle, PostgreSQL |
| **Linguagem** | AdvPL / TL++ |
| **Servidor de Aplicação** | TOTVS Application Server (AppServer) |
| **Interface** | SmartClient (Desktop) e Portal Web |
| **API** | REST (via AppServer) |
| **Dicionário de Dados** | Tabelas SX (SX2, SX3, SIX, SX6) |

## Módulos Principais (Siglas)

| Sigla | Módulo | Descrição |
|:---|:---|:---|
| `SIGAFAT` | Faturamento | Pedidos de venda, NFs de saída |
| `SIGAFIN` | Financeiro | Contas a receber/pagar, fluxo de caixa |
| `SIGACOM` | Compras | Pedidos de compra, cotações |
| `SIGAEST` | Estoque/Custos | Controle de estoque e custos |
| `SIGACTB` | Contabilidade | Lançamentos contábeis, balancetes |
| `SIGAFIS` | Livros Fiscais | SPED, obrigações fiscais |
| `SIGAGPE` | Gestão de Pessoal | Folha, ponto, benefícios |
| `SIGAPCP` | Planejamento/Produção | Ordens de produção |
| `SIGAQLT` | Qualidade | Controle de qualidade |
| `SIGACRM` | CRM | Gestão de relacionamento |
| `SIGAATF` | Ativo Fixo | Controle patrimonial |

## Nomenclatura de Tabelas

As tabelas usam **3 caracteres** como identificador (sigla):

- **Primeira letra**: Família (S = Sistema padrão)
- **Segunda e terceira**: Identificação do módulo/funcionalidade

Exemplos: `SA1` (Clientes), `SB1` (Produtos), `SC5` (Pedidos de Venda)

## Conceitos Fundamentais

### Dicionário de Dados (Tabelas SX)

| Tabela | Função |
|:---|:---|
| `SX2` | Cadastro de tabelas (define quais tabelas existem) |
| `SX3` | Campos das tabelas (define os campos de cada tabela) |
| `SIX` | Índices das tabelas |
| `SX6` | Parâmetros do sistema (prefixo `MV_`) |
| `SX7` | Gatilhos (triggers internas) |

### R_E_C_N_O_ e D_E_L_E_T_

| Campo | Descrição |
|:---|:---|
| `R_E_C_N_O_` | Chave primária interna (auto-incremento) |
| `D_E_L_E_T_` | Exclusão lógica: espaço = ativo, `*` = excluído |

> [!CAUTION]
> Sempre filtre `WHERE D_E_L_E_T_ <> '*'` (ou `= ' '`) em consultas SQL. Sem isso, registros excluídos serão incluídos nos resultados.

### Sufixo de Empresa/Filial

As tabelas físicas no banco recebem sufixo numérico:
- `SA1010` = Tabela SA1, Empresa 01, Filial 0
- `SE1990` = Tabela SE1, Empresa 99, Filial 0

### Campos de Filial

Toda tabela possui o campo `FILIAL` (ex: `A1_FILIAL`, `C5_FILIAL`) para controle multi-filial.

## Ferramentas

| Ferramenta | Função |
|:---|:---|
| **APSDU** | Visualizar e editar tabelas diretamente |
| **Configurador (SIGACFG)** | Gerenciar dicionário de dados, parâmetros |
| **TDS (IDE)** | Desenvolvimento em AdvPL/TL++ |
| **SmartClient** | Interface desktop do usuário |

## Tecla F1

Dentro do Protheus, posicione o cursor em qualquer campo e pressione **F1** para ver informações técnicas: nome da tabela, nome do campo, tipo, tamanho.

---

> [!TIP]
> Documentação oficial: [TDN - TOTVS Developer Network](https://tdn.totvs.com)
