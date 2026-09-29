# O que fazer — passo a passo

## 1. Criar o parâmetro que falta

No componente, criar **um** parâmetro novo:

| Nome | Valor |
|---|---|
| `varDataInicio` | `:VAR_DATA_INICIO` |

Sem ele a tela não abre — ela vai dizer `Faltou: varDataInicio`.

Os outros três já existem e **não precisam mexer**:

| Nome | Valor |
|---|---|
| `queryDados` | *(a query — passo 2)* |
| `varEmpresa` | `:VAR_EMPRESA_DRE` |
| `varMes` | `:VAR_DATA_FIM` ← apesar do nome, é a data fim. Funciona assim. |

---

## 2. Colar a query

Cada tela tem a sua. Abra o arquivo, copie **tudo** (é uma linha só) e cole no parâmetro
`queryDados` da tela correspondente.

| Tela | Arquivo para colar | Estrutura |
|---|---|---|
| **`index1.html`** — BP Externo | `querydados_CORRIGIDA_oneline.sql` | `EST.ID = 1` |
| **`index2.html`** — BP Interno | `querydados_index2_CORRIGIDA_oneline.sql` | `EST.ID = 2` |

> As duas já vêm com o `EST.ID` certo e com os nomes de coluna que cada tela procura.
> Não precisa editar nada dentro delas.

Se quiser o BP Interno na `index1.html` em vez da `index2.html`, use o
`querydados_CORRIGIDA_oneline.sql` trocando `EST.ID = 1` por `EST.ID = 2` — é um lugar só.

---

## 3. Publicar a tela

| Arquivo | Onde |
|---|---|
| `index1.html` | na tela do BP Externo |

O original está guardado em `index1.html.bak`, caso precise voltar.

---

## 4. (Opcional) Telas de indicadores

Só se for mexer nelas agora. Elas consomem **quatro** queries ao mesmo tempo:

| Parâmetro | Arquivo |
|---|---|
| `queryDadosBP` | `queryIndicadores_BP_CORRIGIDA_oneline.sql` |
| `queryDadosDRE` | `queryIndicadores_DRE_CORRIGIDA_oneline.sql` |

Vão em `telas adc/indicadores.html` e `telas adc/baseindicadores.html`.

Aviso: o indicador de BP vai mostrar tudo zerado porque a tabela `BP_TECWAY` está **vazia**
no tenant — é carga pendente, não a query. O de DRE só tem dado até **04/2026** e só para
as empresas **6, 7 e 999**.

---

# O que esperar depois de colar

## Com o filtro em 2026, a coluna de 2026 vem "sem cadastro"

Isso é o correto. **Nenhuma das 24 linhas do BP Externo tem conta contábil vinculada no ano
2026**, e no Interno só Fornecedores tem. A coluna de 2025 vem preenchida normalmente.

Cada coluna usa o cadastro do seu próprio ano, sem misturar. Foi isso que corrigiu o
"Adiantamento de clientes" aparecendo com valor em 2026 sem ter conta cadastrada em 2026.

Para a coluna de 2026 trazer valor, **as contas precisam ser vinculadas ao ano 2026 no
cadastro da estrutura**. Não há nada na consulta que resolva isso.

## Na tela, três coisas passaram a ser diferentes

- `0,00` significa **zero de verdade** (conta cadastrada, saldo zero).
- *sem cadastro* significa **aquele ano não tem conta vinculada** naquela linha.
- Subtotal com `*` está **incompleto** — inclui linha sem cadastro, então não é número
  fechado.

E no topo aparece um aviso dizendo quantas linhas estão sem cadastro em cada ano.

## Confere

- Fornecedores do BP Interno, coluna 2026, CODEMP 999 = **1.707.138,17** — igual ao
  balancete do Sankhya, ao centavo.
- O **BP Externo fecha**: em 2025, Ativo = Passivo + PL = 98.776.914,99 (diferença 0,00).
  Em 2024, 112.129.073,59 nos dois lados.

---

# Pendências (não são da query)

1. **Vincular as contas no ano 2026** — é a principal. Até então, coluna de 2026 vazia.
2. **O BP Interno não fecha**: em 2024, Ativo 112.129.073,59 contra Passivo + PL
   103.685.538,17, diferença de 8.443.535,42. Testei se era o filtro `<> '3.3.3'` ou a
   leitura da linha 3.3.1: não é nenhum dos dois. Falta cadastro no PL do Interno.
   O Externo, com a mesma query, fecha.
3. **`ANO_REFERENCIA` está com dois formatos** na mesma coluna (`2025` e `20251201`). A
   query trata os dois, mas vale padronizar — qualquer consulta nova escrita sem esse
   cuidado volta a zerar a tela, e sem dar erro.
4. **A linha 3.3.1 do Interno** chama-se *Resultado do exercício* mas é calculada como
   saldo de abertura. Como fluxo do período o número muda em ~40 milhões. É decisão de
   quem fecha o balanço; não mexi.

---

# Um ponto em aberto na `index2.html`

A `index2.html` faz `parseFloat(valor) || 0`, então **"sem cadastro" apareceria nela como
`0,00`** — exatamente a confusão que a correção evita na `index1.html`.

A query dela já está certa (manda `NULL`), mas o JavaScript da tela ainda precisa do mesmo
tratamento que a `index1.html` recebeu: distinguir nulo de zero, marcar subtotal incompleto
e mostrar o aviso. Enquanto isso não for feito, na `index2.html` leia `0,00` com cuidado —
pode ser falta de cadastro, não saldo zero.

Se quiser, faço essa correção na `index2.html` também.

---

Detalhamento técnico completo, com as evidências e os números: **`README_CORRECAO.md`**.
