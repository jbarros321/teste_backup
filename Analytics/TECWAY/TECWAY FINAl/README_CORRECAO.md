# Correção — estrutura por ano com filtro de período (empresa, data início, data fim)

Tenant **61302**. Conferido contra os dados reais em 28/09/2026 (só leitura, pela API
de `conexao.md`).

## O que fazer

1. **Query do componente** → conteúdo de **`querydados_CORRIGIDA_oneline.sql`**.
   Para o BP Interno, troque `EST.ID = 1` por `EST.ID = 2` (agora é um lugar só).
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

| | linhas com valor <> 0, antes | depois |
|---|---:|---:|
| BP Externo (est 1) | **0 de 24** | 24 de 24 |
| BP Interno (est 2) | **1 de 37** | 35 de 37 |

As 2 linhas do Interno que seguem zeradas são `3.2` *Ajuste de avaliação patrimonial* e
`3.3.2` *Distribuição de lucros*: têm vínculo de conta, mas não têm movimento no período.
Não é falha da query — ver a ressalva sobre `3.3.1`/`3.3.2` no fim deste documento.

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

### 2. Cada coluna usa o cadastro do seu próprio ano — e sem cadastro não mostra valor

A tela tem duas colunas: a do período filtrado e a comparativa de um ano antes. **Cada
uma resolve o vínculo conta × linha no seu próprio ano**, sem fallback de um para o outro:

| coluna | cadastro usado |
|---|---|
| `VLRLANC_ATU` | ano de `:VAR_DATA_FIM` |
| `VLRLANC_ANT` | ano de `:VAR_DATA_FIM` menos 1 |

Se o ano daquela coluna não tem conta vinculada, **aquela coluna vem `NULL`** e a tela
mostra *sem cadastro* — não zero. A outra coluna continua trazendo valor normalmente.

> Uma versão intermediária desta correção caía no ano anterior quando o ano do filtro não
> tinha vínculo, e usava esse mapeamento nas **duas** colunas, em silêncio. Foi assim que
> *Adiantamento de clientes* apareceu com valor em 2026 sem ter conta cadastrada em 2026.
> Esse fallback foi **removido**.

Com o cadastro de hoje, filtro em 2026, CODEMP 999:

| | coluna 2026 | coluna 2025 |
|---|---|---|
| BP Externo | 24 de 24 linhas *sem cadastro* | 24 de 24 com valor |
| BP Interno | 36 de 37 *sem cadastro* (só Fornecedores tem 2026) | 36 de 37 com valor |

Isso é o retrato correto: **o cadastro do ano 2026 é que ainda não foi feito**. Com o
filtro em 2025, as duas colunas do BP Externo vêm completas.

Três decisões de exibição que vêm disso:

- **A linha sem cadastro continua aparecendo**, marcada, em vez de desaparecer. Linha que
  some muda o total do balanço sem explicar por quê.
- **`0` e *sem cadastro* são coisas diferentes** na tela. Conta cadastrada que somou zero
  mostra `0,00`; ano sem cadastro mostra *sem cadastro*.
- **Subtotal que inclui linha sem cadastro vem marcado com `*`**, porque não está
  completo. Somar `NULL` como zero produziria um subtotal que parece fechado e não é.

E a tela avisa no topo quantas linhas estão sem cadastro em cada ano.

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

**Fornecedores do BP Interno**, CODEMP 999, coluna de 2026:

```
query  = 1.707.138,17
Sankhya = 1.707.138,17   -> fecha ao centavo
```

**O BP Externo fecha.** Nos anos em que a coluna está completa, ativo e passivo + PL
batem exatamente (CODEMP 999):

| ano | Ativo | Passivo + PL | diferença |
|---|---:|---:|---:|
| 2025 | 98.776.914,99 | 98.776.914,99 | **0,00** |
| 2024 | 112.129.073,59 | 112.129.073,59 | **0,00** |

> Correção de uma afirmação anterior minha: eu havia reportado que o BP Externo não
> fechava, por 5.493.440,82. Aquilo era artefato do fallback — o cadastro de 2025 aplicado
> ao corte de datas de 2026. Sem o fallback, fecha.

**Casos de borda** (BP Interno, nenhum derruba a tela):

| filtro | corte aplicado | colunas |
|---|---|---|
| 01/01 a 31/12/2026 | idem | 2026 / 2025 |
| 01/01 a 31/03/2026 | idem | 2026 / 2025 |
| 01/01 a 31/12/2025 | idem | 2025 / 2024 |
| datas invertidas | reordenado | — |
| data início vazia | 01/01 do ano da data fim | — |
| data fim vazia | até hoje | — |
| ambas vazias | 01/01 até hoje | — |

As 14 empresas (`CODEMP` 1, 2, 3, 4, 6, 7, 11, 14, 15, 16, 18, 600, 888, 999) retornam
linhas; as menores preenchem menos porque têm menos contas movimentadas.

---

## Pendências que **não** são da query

São de cadastro. A query não pode corrigi-las sem inventar número, então a tela **mostra**
o problema em vez de escondê-lo.

### O ano 2026 precisa dos vínculos de conta

É a pendência principal. Nenhuma das 24 linhas do BP Externo tem conta vinculada em 2026,
e no Interno só Fornecedores tem. Até isso ser feito, a coluna de 2026 vem *sem cadastro*.

### O BP Interno não fecha

Com a coluna completa (2024, CODEMP 999): Ativo 112.129.073,59 contra Passivo + PL
103.685.538,17 — diferença de **8.443.535,42**. Testei se o filtro `DET.ORDEM <> '3.3.3'`
explicava (a linha *Lucro/Prejuízos acumulados*): não explica, e incluí-la passa do ponto
para o outro lado (−17.441.212,65). Ou seja, há falta de cadastro no PL do Interno além do
3.3.3. O Externo, com a mesma query, fecha — então não é a consulta.

Mantive o `<> '3.3.3'` porque ele já estava na sua query e removê-lo é decisão contábil.

### As linhas 3.3.1 e 3.3.2 — confirmar a leitura contábil

A query herdou este mapeamento, e eu o preservei. Mas os nomes das linhas na estrutura
sugerem que ele pode estar trocado:

Coluna de 2025, CODEMP 999 — as três leituras possíveis de cada linha:

| ORDEM | NOME_GRUPO no cadastro | a query usa | valor usado | fluxo do período | saldo |
|---|---|---|---:|---:|---:|
| 3.3.1 | Resultado do exercício | abertura | 20.188.772,57 | −19.277.471,83 | 911.300,74 |
| 3.3.2 | Distribuição de lucros | fluxo | −11.500.000,00 | −11.500.000,00 | −37.941.546,46 |

O 3.3.2 está coerente. O **3.3.1 chama atenção**: uma linha chamada *Resultado do
exercício* normalmente é o **fluxo do período**, não o saldo de abertura — e a diferença
entre as duas leituras é de quase 40 milhões. **Não mexi nisso**: muda número divulgado e
é decisão de quem fecha o balanço. Se confirmarem, é uma linha em cada `CASE` da query
(`map_atu` e `map_ant`).

Testei se isso explicava o Interno não fechar: **não explica.** Com 3.3.1 como abertura a
diferença é 8.443.535,42; como fluxo, 15.631.640,37. Nenhuma das duas fecha, então a falta
de cadastro no PL do Interno é um problema separado.

### Padronizar `ANO_REFERENCIA`

Os dois formatos convivendo são uma armadilha permanente (item 1). Hoje nenhum par
(linha, ano) tem referência repetida, então não há duplicação; mas a coluna aceita `2025`
e `20251201`, que normalizam para o mesmo ano — foi assim que Fornecedores multiplicou
por 12 no tenant anterior. A query tem `LIMIT 1` como trava, e vale corrigir o cadastro.

---

# Telas de indicadores (BP e DRE)

Modelo de dados **diferente** do balanço: `DET_DRE_TW` / `CAD_CONTA_DRE_TW` /
`BP_TECWAY` / `DRE_TECWAY` / `DET_NOTAS`, com `MES` como texto `'MM/AAAA'`. Não passam
por `DET_DEMONSTRATIVO_REFERENCIA`, então o defeito do `ANO_REFERENCIA` não as atinge —
elas tinham outro problema: **ainda estavam no filtro de mês**.

| arquivo | origem | estrutura |
|---|---|---|
| `queryIndicadores_BP_CORRIGIDA.sql` | `telas adc/queryDadosBPindicadores.sql` | `ESTR_DRE_TW.ID = 7` |
| `queryIndicadores_DRE_CORRIGIDA.sql` | `telas adc/queryDadosDREindicadores.sql` | `ESTR_DRE_TW.ID = 9` |

Versões `_oneline.sql` para colar no componente. Parâmetros: `:VAR_DATA_INICIO`,
`:VAR_DATA_FIM`, `:VAR_EMPRESA`.

## O que mudou

1. **O ano saía de `:VAR_MES`**, agora sai de `:VAR_DATA_FIM`. Com `:VAR_MES` vazio,
   `STR_TO_DATE(CONCAT('01/', NULL))` dava `NULL`, o ano virava `NULL` e as 12 colunas
   vinham todas em branco.
2. **O período passou a limitar os meses.** Antes a query montava sempre os 12 meses do
   ano, ignorando qualquer recorte — o filtro de data não tinha efeito nenhum sobre as
   colunas. Agora só entram os meses que intersectam `[data início, data fim]`.
   Mês parcial entra inteiro, porque a origem só tem granularidade mensal.
3. **A nota explicativa** vinha de `DET_NOTAS.MES = :VAR_MES`; agora usa o mês da data
   fim, que é o fechamento do período.
4. **`TRIM` no código da conta.** 10 vínculos do `CAD_CONTA_DRE_TW` estão gravados com
   espaço à esquerda (2 contas, estruturas 1 e 5) e nunca casavam com a tabela de
   valores. Não afeta as estruturas 7 e 9, mas evita perda silenciosa.

O `* 1000` das queries originais está **certo**: conferi contra o balancete e a mediana
da razão nas contas casadas é exatamente `1000,0000`.

## Conferência

DRE Interno (est 9), empresa 999, 01/01/2026 a 30/04/2026 — **30 de 41 linhas com
valor**. A janela de meses fecha em todos os casos testados:

| filtro | meses que entram |
|---|---|
| 01/01 a 31/12/2026 | JAN … DEZ |
| 01/01 a 31/03/2026 | JAN, FEV, MAR |
| 01/03 a 30/06/2026 | MAR, ABR, MAI, JUN |
| 01/02 a 28/02/2026 | FEV |
| 10/02 a 20/03/2026 | FEV, MAR |
| datas invertidas | reordenado |
| início / fim / ambos vazios | não zera — cai no ano da data fim |

## Três limites de carga — não são da query

1. **`BP_TECWAY` está VAZIA.** Zero linhas no tenant. O indicador de BP vai mostrar a
   estrutura com tudo zerado até a carga ser feita, e nenhuma query muda isso. A carga é
   a do `../Ajuste v2/migracao telas adc/04_dados_valores_BP_TECWAY.sql`.
2. **`DRE_TECWAY` só vai até 04/2026.** Um filtro que passe de 30/04/2026 traz MAI a DEZ
   em branco. E só existem as empresas **6, 7 e 999** — as outras 11 do
   `IMP_BASE_BALANCETE` não têm linha aqui e vêm zeradas.
3. **`DRE_TECWAY` não é derivável do `IMP_BASE_BALANCETE`.** Testei mês a mês e acumulado
   no ano: só **7,5%** dos 8.102 casos casam. São duas cargas independentes — então os
   indicadores e o balanço **não conferem entre si por construção**. Se a expectativa é
   que confiram, isso precisa ser decidido no processo de carga, não na consulta.
