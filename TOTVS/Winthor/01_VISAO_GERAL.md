# TOTVS Winthor - Visão Geral

## Sobre o Sistema

O **TOTVS Winthor** (anteriormente conhecido como **PC Sistemas**) é um ERP voltado para o segmento de **distribuição atacadista e varejo**. É amplamente utilizado por empresas distribuidoras de alimentos, bebidas, materiais de construção, higiene e limpeza, entre outros.

## Arquitetura

| Componente | Tecnologia |
|:---|:---|
| **Banco de Dados** | Oracle Database |
| **Linguagem** | PL/SQL, Java |
| **Interface** | Desktop (Delphi) e Web |
| **Servidor de Aplicação** | WTA (Winthor Anywhere) |
| **Integração** | API REST via TOTVS Developers |

## Módulos Principais

| Módulo | Descrição |
|:---|:---|
| **Comercial** | Gestão de vendas, pedidos, faturamento, comissões |
| **Logística** | Controle de estoque, WMS, carregamento, rotas |
| **Financeiro** | Contas a pagar, contas a receber, caixa e bancos |
| **Compras** | Pedidos de compra, cotações, recebimento |
| **Fiscal** | Emissão de NF-e, SPED, obrigações acessórias |
| **Contábil** | Contabilidade geral, centros de custo |
| **RH** | Folha de pagamento, ponto eletrônico |

## Nomenclatura

As tabelas do Winthor seguem o padrão de nomenclatura herdado da **PC Sistemas**, onde praticamente todas as tabelas iniciam com o prefixo **`PC`**:

- `PCPRODUT` → **PC** + **PRODUT**o
- `PCCLIENT` → **PC** + **CLIENT**e
- `PCPEDC` → **PC** + **PED**ido **C**abeçalho

## Rotinas

As funcionalidades do sistema são organizadas por **números de rotina**. Exemplos:

| Rotina | Descrição |
|:---|:---|
| **132** | Parâmetros da Presidência (configuração geral) |
| **316** | Digitar Pedido de Venda |
| **336** | Alterar / Liberar Pedido de Venda |
| **530** | Permissões de Acesso |
| **560** | Atualização de Banco de Dados |
| **1402** | Faturar Pedido (Telemarketing) |
| **1432** | Faturar Pedido de Venda |
| **2500** | Criação/Alteração de Tabelas, Campos e Views |
| **2521** | Integração NEOGRID (EDI) |

## Ambiente

- **Base de Dados**: Oracle (obrigatório)
- **Encoding**: Geralmente WE8MSWIN1252 ou AL32UTF8
- **Chaves Primárias**: Cada tabela possui seu identificador próprio (ex: `CODCLI`, `CODPROD`, `NUMNOTA`)
- **Registros**: O sistema mantém registros históricos, raramente apaga dados fisicamente
- **Parametrização**: Amplamente configurável via tabela `PCCONSUM` (rotina 132)

---

> [!TIP]
> Para descobrir qual tabela uma funcionalidade utiliza, consulte a **Rotina 2500** que lista todas as tabelas, campos e views do sistema.

> [!IMPORTANT]
> A TOTVS desencoraja o acesso direto ao banco de dados para integrações. Utilize preferencialmente as APIs REST disponíveis no portal [TOTVS Developers](https://api.totvs.com.br/).
