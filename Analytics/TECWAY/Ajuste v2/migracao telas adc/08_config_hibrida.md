# Correção das linhas de DRE zeradas (configuração híbrida)

## O sintoma

Na tela de indicadores, as linhas `BP (...)` vieram corretas mês a mês, e as
linhas `DRE (...)` — Receita bruta, Receita líquida, CSP, Lucro bruto, Lucro
operacional, Lucro líquido — saíram `0,00` em todos os meses, com valor só na
última coluna (dezembro). O EBITDA, que depende do lucro operacional, veio
errado pelo mesmo motivo.

## A causa

Não é erro de carga. Neste tenant o `IMP_BASE_BALANCETE` só tem movimento das
contas de resultado (3/4/5/6) em dezembro — é o fechamento anual. Somando o
valor absoluto dessas contas para a empresa 999: `2025-01` a `2025-11` = 0,00 e
`2025-12` = 246.803.956,56. As contas patrimoniais (1/2), essas sim, têm
movimento mensal — por isso o BP funcionou.

## A correção: BP do balancete daqui + DRE da base de origem

O `DRE_TECWAY` da origem tem o movimento mês a mês e já foi extraído
(`04_dados_valores_DRE_TECWAY.sql`, período 01/2022 a 04/2026). É uma tabela
pequena — ~23 mil linhas agregadas — bem diferente do `BP_TECWAY`, que continua
desnecessário.

### Passos

1. **Criar só a tabela `DRE_TECWAY`** — é o primeiro `CREATE TABLE` do bloco B
   do `01_ddl_tabelas.sql`. `BP_TECWAY` continua não sendo criada.
2. **Rodar `04_dados_valores_DRE_TECWAY.sql`.**
3. **Trocar as queries de DRE** (as de BP continuam iguais):

| Tela | Propriedade | Query a usar |
|---|---|---|
| Indicadores / BaseIndicadores | `queryDadosBP` | `06_query_tela_INDICADORES_balancete.sql` *(não muda)* |
| Indicadores / BaseIndicadores | `queryDadosDRE` | **`05_query_tela_BASEINDICADORES.sql`** *(original, lê `DRE_TECWAY`)* |
| DRE | `queryDRE` | **`05_query_tela_DRE.sql`** *(original, lê `DRE_TECWAY`)* |
| BP | `queryDados` | `06_query_tela_BP_balancete.sql` *(não muda)* |

Ou seja: as queries `06_*` de BP ficam; as `06_*` de DRE são substituídas pelas
`05_*` correspondentes.

## O que muda nos números

Os valores de DRE passam a ser os do projeto de origem. Em **12/2024** as duas
bases batem 100% (453/453 contas), então dezembro não muda. Nos demais meses não
há com o que comparar — hoje está zerado. Existem contas em que as duas bases
divergem (ex.: empresa 999, conta `5.1.01.04.000004`, zerada aqui e com valor
lá), então some uma diferença possível entre estas telas e as de DFC/BP já
publicadas, que continuam lendo o balancete daqui.

## A alternativa, se preferir base única

Importar o balancete mensal das contas de resultado neste tenant. Aí as queries
`06_*` de DRE voltam a servir, `DRE_TECWAY` não precisa existir e todas as telas
passam a ler da mesma base. É o caminho mais limpo a longo prazo, mas depende de
uma nova carga de balancete — não dá para resolver só com script.

## Cobertura de período

- `DRE_TECWAY` carregado: **01/2022 a 04/2026** (52 meses).
- Filtro de mês (`07_queries_filtros.sql`, lê o balancete): **12/2021 a 03/2026**.

Ou seja, todo mês que o filtro oferece tem DRE correspondente, exceto `12/2021`,
que não existe na origem e sairá zerado.
