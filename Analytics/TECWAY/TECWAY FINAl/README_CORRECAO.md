# Correção — estrutura por ano com filtro de período (empresa, data início, data fim)

Tenant **61302**. Conferido contra os dados reais em 28/09/2026 (só leitura, pela API
de `conexao.md`).

## O que fazer

1. **Query do componente** → conteúdo de **`querydados_CORRIGIDA_oneline.sql`**.
   Para o BP Interno, troque os três `EST.ID = 1` por `EST.ID = 2`.
   A versão comentada e legível é `querydados_CORRIGIDA.sql` — mesma query.
2. **Criar o parâmetro da data inicial** no componente: `varDataInicio` = `:VAR_DATA_INICIO`.
   É o único que falta hoje (ver abaixo).
3. **Tela** → `index1.html`. O original está preservado em `index1.html.bak`.

### Parâmetros do componente

| Parâmetro | Valor | Situação |
|---|---|---|
| `queryDados` | conteúdo do `querydados_CORRIGIDA_oneline.sql` | substituir |
| `varEmpresa` | `:VAR_EMPRESA_DRE` | já existe |
| `varDataFim` | `:VAR_DATA_FIM` | hoje está registrado como **`varMes`** |
| `varDataInicio` | `:VAR_DATA_INICIO` | **não existe — criar** |

A tela aceita `varMes` como apelido de `varDataFim`, então ela funciona antes e depois
de renomear o parâmetro. Não precisa de virada combinada.

---

## Por que a tela vinha incompleta

### 1. `ANO_REFERENCIA` está com dois formatos na mesma coluna — é a causa principal

`DET_DEMONSTRATIVO_REFERENCIA.ANO_REFERENCIA` é um inteiro, e o tenant tem:

```
20231201, 20241201, 20251201, 20261201   (AAAAMMDD)  -> 352 registros
2024, 2025, 2026                         (ano puro)  ->   4 registros
```

A query comparava `ANO_REFERENCIA <= YEAR(:VAR_DATA_FIM)`, isto é `20251201 <= 2026`,
que é **falso**. Logo `MAX(...)` devolvia `NULL`, o `DETREF.ANO_REFERENCIA = NULL` nunca
casava, e a linha **desaparecia inteira**:

| | linhas com valor, antes | depois |
|---|---:|---:|
| BP Externo (est 1) | **0 de 24** | 24 de 24 |
| BP Interno (est 2) | **1 de 37** | 37 de 37 |

A única linha que sobrevivia era Fornecedores do Interno — justamente a que tem o ano
gravado como `2026`, de 4 dígitos.

As 4 exceções de 4 dígitos são todas a linha Fornecedores:

```
est 1  2.1.1 Fornecedores  2025  -> 1.226 vínculos
est 1  2.1.1 Fornecedores  2024  -> 1.226 vínculos
est 1  2.1.1 Fornecedores  2026  ->     0 vínculos
est 2  2.1.1 Fornecedores  2026  -> 2.163 vínculos
```

A correção normaliza antes de comparar, aceitando os dois formatos:

```sql
CASE WHEN R.ANO_REFERENCIA > 10000 THEN FLOOR(R.ANO_REFERENCIA / 10000)
     ELSE R.ANO_REFERENCIA END
```

> Vale padronizar a coluna no cadastro. Enquanto os dois formatos convivem, qualquer
> query nova escrita sem essa normalização volta a zerar a tela — e sem dar erro.

### 2. O ano 2026 está cadastrado e vazio

As 23 referências `20261201` do BP Externo têm **zero vínculos de conta**, e a
referência `2026` do BP Externo também. Normalizar o formato sem olhar vínculo faz a
query escolher 2026 e zerar tudo de novo, agora em silêncio. A correção exige vínculo:

```sql
AND EXISTS (SELECT 1 FROM DET_DEMONSTRATIVO_CTACTB C
             WHERE C.ID_DET_DEMONSTRATIVO_REFERENCIA = R.ID)
```

Com isso o BP Externo cai em 2025 e mostra número. **É cadastro de 2025 valendo para o
exercício de 2026** — funciona, mas o certo é vincular as contas no ano 2026.

### 3. `BETWEEN` no balanço dava movimento, não saldo

`IMP_BASE_BALANCETE` guarda **movimento por competência**. O saldo de uma conta
patrimonial é o acumulado desde o início, não o do período. A query usava

```sql
SUM(CASE WHEN REFERENCIA BETWEEN :VAR_DATA_INICIO AND :VAR_DATA_FIM THEN VLRLANC END)
```

o que devolvia apenas a movimentação da janela — número errado em **toda** linha de
balanço, e que ficaria errado sem parecer errado. Agora:

| corte | usado em |
|---|---|
| `<= :VAR_DATA_FIM` | saldo — todas as linhas de balanço |
| `BETWEEN :VAR_DATA_INICIO AND :VAR_DATA_FIM` | fluxo — linha `3.3.2` |
| `< :VAR_DATA_INICIO` | saldo de abertura — linha `3.3.1` |

e os mesmos três cortes deslocados um ano para a coluna comparativa.

### 4. As exclusões perdiam a linha

`regras_excluidas` selecionava só `PADRAO_CTACTB`, sem `ORDEM`, e o `LEFT JOIN` casava
apenas pela conta. Uma conta excluída de **uma** linha passava a ser excluída de
**todas**. São 35 regras (todas na estrutura 10 / DFC hoje), mas o defeito valia para
qualquer estrutura. A `ORDEM` voltou à CTE e ao JOIN.

### 5. Detalhes que davam número instável ou célula em branco

- `SUM(IFNULL(M.valor_atu,0) * M.SINAL)` dava `NULL` quando o grupo não tinha conta
  casada, porque `M.SINAL` é `NULL` — célula em branco em vez de zero. O `IFNULL` passou
  para fora do `SUM`.
- O `ROW_NUMBER` particionava por `(CTACTB, ORDEM)` mas o join usa `(ORDEM, NOME_GRUPO)`,
  e o `ORDER BY` só tinha `LENGTH` — empate resolvido ao acaso, resultado podendo variar
  entre execuções. Corrigido nos dois pontos.
- Nenhum `PADRAO_CTACTB` do tenant tem curinga (`%` ou `_`) nos 3.305 padrões: o `LIKE`
  é igualdade exata. Mantive `LIKE` para não mudar o comportamento caso passem a usar.

### 6. A tela morria em branco, e não por causa da query

A trava lia `componentData.varMes` pelo nome exato. Nome de propriedade em JavaScript
diferencia maiúscula de minúscula, então um parâmetro registrado como `Varmes` ou
`VarDataInicio` fazia a condição nunca fechar: venciam os 5 segundos e a tela morria sem
dizer o que faltou. Além disso ela disparava uma **segunda** consulta
(`SELECT :VAR_MES_FILTRO`) só para descobrir a data de referência, e morria quando essa
voltava vazia.

Agora: os nomes são resolvidos ignorando caixa e pontuação e aceitando apelidos; a
mensagem diz **qual** parâmetro faltou e quais a plataforma expôs; e a segunda consulta
deixou de existir — a query devolve `DATA_INICIO_USADA` e `DATA_FIM_USADA`.

### 7. A tela mostrava milhares

Havia um `/ 1000` com arredondamento para inteiro em cada valor. Como o pedido é dado
completo e exato, saiu: agora mostra o valor cheio com centavos.

---

## Conferência contra os dados reais

CODEMP 999, período 01/01/2026 a 31/12/2026:

```
Fornecedores do BP Interno = 1.707.138,17
balancete do Sankhya       = 1.707.138,17   -> fecha ao centavo
```

Casos de borda, todos no BP Interno (nenhum zera a tela):

| filtro | corte aplicado | Fornecedores |
|---|---|---:|
| 01/01/2026 a 31/12/2026 | 01/01/2026 a 31/12/2026 | 1.707.138,17 |
| 01/01/2026 a 31/03/2026 | idem | 1.434.717,57 |
| 01/01/2025 a 31/12/2025 | idem | 1.487.994,59 |
| datas invertidas | reordenado para 01/01 a 31/12/2026 | 1.707.138,17 |
| data início vazia | 01/01/2026 a 31/12/2026 | 1.707.138,17 |
| data fim vazia | 01/01/2026 a 28/09/2026 | 1.707.138,17 |
| ambas vazias | 01/01/2026 a 28/09/2026 | 1.707.138,17 |

As 14 empresas (`CODEMP` 1, 2, 3, 4, 6, 7, 11, 14, 15, 16, 18, 600, 888, 999) retornam
linhas com valor; nenhuma vem toda zerada. As empresas menores preenchem menos linhas
porque têm menos contas movimentadas, não por falha da query.

---

## Pendências que **não** são da query

São de cadastro. A query não pode corrigi-las sem inventar número, então a tela passou a
**mostrar** a diferença em vez de escondê-la.

### O balanço não fecha

CODEMP 999, 31/12/2026:

| | Ativo | Passivo + PL | diferença |
|---|---:|---:|---:|
| BP Externo | 108.490.546,01 | 102.997.105,19 | 5.493.440,82 |
| BP Interno | 108.490.546,01 | 85.766.592,88 | 22.723.953,13 |

Duas causas visíveis:

1. **`DET.ORDEM <> '3.3.3'`** — a query exclui a linha 3.3.3 *Lucro/Prejuízos
   acumulados*, cujo saldo em 31/12/2026 é **50.335.860,57**. Mantive o filtro porque
   ele já estava na sua query e removê-lo é decisão contábil, não técnica. Só que
   incluí-lo também não fecha (daria 136.102.453,45 contra um ativo de 108.490.546,01),
   então há mais de um ajuste pendente no cadastro do PL.
2. **Fornecedores do Externo usa o cadastro de 2025** e dá 1.524.500 contra 1.707.138 do
   Interno — os mesmos **182.638** de contas que só existem no cadastro de 2026, já
   descritos em `../Correcao Fornecedores 2026/`.

### As linhas 3.3.1 e 3.3.2 — confirmar a leitura contábil

A query herdou este mapeamento, e eu o preservei. Mas os nomes das linhas na estrutura
sugerem que ele pode estar trocado:

| ORDEM | NOME_GRUPO no cadastro | o que a query usa | valor | se fosse fluxo do período |
|---|---|---|---:|---:|
| 3.3.1 | Resultado do exercício | saldo de abertura | 911.300,74 | **5.029.301,08** |
| 3.3.2 | Distribuição de lucros | fluxo do período | 0,00 | — (saldo: −37.941.546,46) |

Uma linha chamada *Resultado do exercício* normalmente é o **fluxo do período**, não o
saldo de abertura — e *Distribuição de lucros* deu exatamente zero no período, o que
também chama atenção. **Não mexi nisso**: muda número divulgado e é decisão de quem
fecha o balanço. Se confirmarem, é uma linha de cada `CASE` na query.

### Padronizar `ANO_REFERENCIA`

Os dois formatos convivendo são uma armadilha permanente (item 1). Hoje nenhum par
(linha, ano) tem referência repetida, então não há duplicação; mas a coluna aceita `2025`
e `20251201`, que normalizam para o mesmo ano — foi assim que Fornecedores multiplicou
por 12 no tenant anterior. A query tem `LIMIT 1` como trava, e vale corrigir o cadastro.
