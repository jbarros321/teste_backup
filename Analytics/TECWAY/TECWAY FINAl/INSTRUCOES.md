# O que fazer — passo a passo

## 1. Declarar os parâmetros no componente

Eu disse antes que não precisava criar nenhum. **Estava errado** — e é a causa mais provável
da DRE vir zerada. Confundi duas coisas: o **JavaScript** da tela não precisa mais desses
parâmetros (removi a segunda consulta que os usava), mas a **plataforma** precisa deles
declarados para saber o que substituir no texto da query. Um `:VAR_*` não declarado não é
substituído, e o `WHERE` não casa com nada.

Declare nas **quatro** telas:

| Nome | Valor | Situação hoje |
|---|---|---|
| `varEmpresa` | `:VAR_EMPRESA_DRE` | existe na index1 e index2; **falta na index3 e index4** |
| `varDataInicio` | `:VAR_DATA_INICIO` | conferir nas quatro |
| `varDataFim` | `:VAR_DATA_FIM` | hoje está como `varMes` nas quatro |

Era exatamente essa a diferença entre o BP (que trouxe dados) e a DRE (que zerou): as telas
de BP já tinham o `varEmpresa` declarado; as de DRE nunca tiveram — só `query`, `varMes` e,
na index4, `jdbcId`.

As travas das telas exigem **só a query**, então nada morre em branco por parâmetro faltando.
Mas sem eles declarados a query recebe `NULL` e zera — agora com aviso explícito na tela.

---

## 2. Colar a query — uma por tela

Abra o arquivo, copie **tudo** (é uma linha só) e cole no parâmetro da query da tela.

| Tela | Cole este arquivo | Demonstrativo | Estrutura |
|---|---|---|---|
| `index1.html` | `querydados_CORRIGIDA_oneline.sql` | BP Externo | `EST.ID = 1` |
| `index2.html` | `querydados_index2_CORRIGIDA_oneline.sql` | BP Interno | `EST.ID = 2` |
| `index3.html` | `query_index3_CORRIGIDA_oneline.sql` | DRE Externo | `EST.ID = 3` |
| `index4.html` | `query_index4_CORRIGIDA_oneline.sql` | DRE Interno | `EST.ID = 4` |

Cada uma já vem com o `EST.ID` certo e com os nomes de coluna que aquela tela procura
(`VLRLANC_ATU/ANT`, `ANO_ATUAL/ANTERIOR`, `VALOR_ANO_ATUAL/ANTERIOR`,
`VALOR_ATUAL/ANTERIOR`). **Não edite nada dentro delas.**

## 3. Publicar as telas

`index1.html`, `index2.html`, `index3.html`, `index4.html` — todas corrigidas.
Os originais estão em `.bak` ao lado de cada uma.

## 4. (Opcional) Telas de indicadores

`telas adc/indicadores.html` e `baseindicadores.html` consomem quatro queries:

| Parâmetro | Arquivo |
|---|---|
| `queryDadosBP` | `queryIndicadores_BP_CORRIGIDA_oneline.sql` |
| `queryDadosDRE` | `queryIndicadores_DRE_CORRIGIDA_oneline.sql` |

O indicador de BP vai mostrar tudo zerado porque a tabela `BP_TECWAY` está **vazia** no
tenant — carga pendente, não a query. O de DRE só tem dado até **04/2026** e só para as
empresas **6, 7 e 999**.

---

# O que esperar depois de colar

## Com o filtro em 2026, a coluna de 2026 vem "sem cadastro"

Isso é o correto, e vale para as **quatro** telas. Conferi no tenant:

| Estrutura | linhas | com cadastro em 2026 | em 2025 |
|---|---:|---|---|
| BP Externo | 24 | nenhuma | todas |
| BP Interno | 37 | só Fornecedores | todas menos 1 |
| DRE Externo | 10 | nenhuma | todas |
| DRE Interno | 47 | nenhuma | todas |

**Cada coluna usa o cadastro do seu próprio ano**, sem misturar. Foi isso que corrigiu o
"Adiantamento de clientes" aparecendo com valor em 2026 sem ter conta cadastrada em 2026.

Para a coluna de 2026 trazer valor, **as contas precisam ser vinculadas ao ano 2026 no
cadastro da estrutura**. Nenhuma consulta resolve isso.

Com o filtro em **2025**, as duas colunas vêm completas nas quatro telas.

## Três coisas passaram a ser diferentes na tela

- `0,00` significa **zero de verdade** (conta cadastrada, saldo zero).
- *sem cadastro* significa **aquele ano não tem conta vinculada** naquela linha.
- Subtotal com `*` está **incompleto** — inclui linha sem cadastro.

E aparece um aviso dizendo quantas linhas estão sem cadastro em cada ano.

## Confere

- Fornecedores do BP Interno, coluna 2026, CODEMP 999 = **1.707.138,17** — igual ao
  balancete do Sankhya, ao centavo.
- O **BP Externo fecha**: 2025 → Ativo = Passivo + PL = 98.776.914,99 (diferença 0,00);
  2024 → 112.129.073,59 nos dois lados.
- DRE Externo 2025, receita operacional líquida = 97.726.703,03.
- Casos de borda testados nas quatro: trimestre, mês isolado, meio de mês, datas
  invertidas, data início vazia, data fim vazia, ambas vazias. Nenhum derruba a tela.

---

# O que foi corrigido em cada query

O defeito principal é o mesmo nas quatro: **`ANO_REFERENCIA` tem dois formatos na mesma
coluna** (`2025` e `20251201`). A comparação `ANO_REFERENCIA <= YEAR(:VAR_DATA_FIM)` virava
`20251201 <= 2026`, que é falso, o `MAX(...)` devolvia `NULL` e a linha desaparecia inteira.
Nas estruturas 3 e 4 **todas** as referências são `AAAAMMDD`, então essas duas telas vinham
completamente vazias.

Além disso, por tela:

| Tela | Também corrigido |
|---|---|
| `index1` / BP Externo | `BETWEEN` dava movimento em vez de saldo; exclusões perdiam a `ORDEM`; `IFNULL` dentro do `SUM`; `ROW_NUMBER` sem desempate estável |
| `index2` / BP Interno | tudo do index1, **mais**: a query não usava `DET_DEMONSTRATIVO_CTACTB_EXC` — ignorava toda regra de exclusão |
| `index3` / DRE Externo | exclusões sem `ORDEM`; `ROW_NUMBER` particionado só por `CTACTB`, então conta que atende duas linhas só somava em uma, escolhida ao acaso |
| `index4` / DRE Interno | **nenhuma exclusão de conta existia**; e sem `ROW_NUMBER`, conta que casa com mais de um padrão da mesma linha era somada uma vez por padrão |

> Nas DRE (index3 e index4) o `BETWEEN` foi **mantido**: DRE é resultado do período, então
> ali ele está certo. No BP era defeito, porque saldo patrimonial é acumulado.

As telas `index1` e `index3` também mostravam **milhares** (dividiam por 1000 e
arredondavam). Agora mostram o valor cheio com centavos.

---

# Pendências (não são da query)

1. **Vincular as contas no ano 2026** — é a principal. Até então, coluna de 2026 vazia nas
   quatro telas.
2. **O BP Interno não fecha**: em 2024, Ativo 112.129.073,59 contra Passivo + PL
   103.685.538,17, diferença de 8.443.535,42. Testei se era o filtro `<> '3.3.3'` ou a
   leitura da linha 3.3.1 — não é nenhum dos dois. Falta cadastro no PL do Interno. O
   Externo, com a mesma query, fecha.
3. **`ANO_REFERENCIA` com dois formatos** — as queries tratam, mas vale padronizar no
   cadastro. Qualquer consulta nova escrita sem esse cuidado volta a zerar a tela, sem erro.
4. **A linha 3.3.1 do Interno** chama-se *Resultado do exercício* mas é calculada como saldo
   de abertura. Como fluxo do período o número muda em ~40 milhões. É decisão de quem fecha
   o balanço; não mexi.

---

Detalhamento técnico, com evidências e números: **`README_CORRECAO.md`**.

---

# Duas coisas que apareceram no uso

## "Filtrei julho no BP e mostra o ano todo"

Duas causas, e nenhuma é defeito:

**1. Balanço é saldo, não movimento.** A coluna do BP é o **saldo acumulado até a data
fim**, não a movimentação dentro do período. Filtrar julho num balanço significa "saldo em
31/07" — que inclui tudo desde o início. É assim que balanço patrimonial funciona. A data
início só é usada nas linhas de resultado (`3.3.1` e `3.3.2`), que são fluxo.

**2. O balancete só tem carga até 07/2026.** Conferi no tenant:

```
2026-01   972 linhas      2026-08    4 linhas (valor zero)
2026-02   901             2026-09    4
...                       2026-10    4
2026-07   907             2026-11    4
                          2026-12    4
```

Agosto a dezembro têm 4 linhas cada, todas com valor zero. Então o saldo em 31/07 é igual
ao de 31/12 — não porque o filtro ignora a data, mas porque não houve movimento depois.

**O filtro responde, sim.** Fornecedores do BP Interno, CODEMP 999:

| corte | valor |
|---|---:|
| 31/03/2026 | 1.434.717,57 |
| 31/07/2026 | 1.707.138,17 |
| 31/12/2026 | 1.707.138,17 |

Março difere de julho. Julho e dezembro são iguais porque a carga para em julho.

## "A DRE vem toda zerada"

As queries agora devolvem dois contadores (`LANC_EMPRESA` e `LANC_PERIODO`) e as quatro
telas **dizem a causa na própria tela** em vez de mostrar zero sem explicação:

| Situação | O que a tela diz |
|---|---|
| `LANC_EMPRESA = 0` | "Nenhum lançamento encontrado para a empresa selecionada" — e aponta o `varEmpresa` não declarado |
| `LANC_PERIODO = 0` e `LANC_EMPRESA > 0` | "A empresa tem balancete, mas nada no período filtrado" |
| cadastro do ano faltando | "sem cadastro" por linha, com contagem |

Duas causas conhecidas para a DRE zerada:

1. **`index3.html` e `index4.html` nunca tiveram o parâmetro `varEmpresa`.** Só tinham
   `query`, `varMes` (e `jdbcId` na index4). Sem `varEmpresa` declarado, a plataforma não
   substitui `:VAR_EMPRESA_DRE`, o `CODEMP` fica nulo e o balancete não casa com nada.
   As telas de BP funcionavam porque lá esse parâmetro já existia.
2. **Cinco empresas não têm lançamento de resultado nenhum**: `CODEMP` 3, 14, 15, 600 e
   888 têm zero linhas em contas 3/4/5 em 2025. Se a seleção for uma delas, zera de verdade.
