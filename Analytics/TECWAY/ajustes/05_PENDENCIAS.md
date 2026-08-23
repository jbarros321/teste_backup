# Pendências — precisam de decisão sua

## 1. Linha 4.1 do DFC "Empréstimos e Financiamentos" — erro no relatório oficial

O relatório de 2025 traz **101.195**. O cálculo correto dá **-20.985**.
A diferença é **122.180** — exatamente o valor que faz o DFC não fechar:

| | Relatório | Correto |
|---|---:|---:|
| Operacionais | 56.313 | 56.313 |
| Investimentos | (16.890) | (16.890) |
| Financiamentos | 80.282 | (41.899) |
| **Aumento de caixa** | **119.704** | **(2.476)** |
| Saldo final − saldo inicial (1.898 − 4.374) | (2.476) | (2.476) |

O relatório afirma "Aumento de caixa 119.704" e, duas linhas abaixo, mostra
caixa indo de 4.374 para 1.898 — uma queda de 2.476. Com o valor calculado
o DFC fecha na casa do milhar.

A variação de empréstimos confere pelo Balanço:
PC 27.141 + PNC 21.773 = 48.914 em 2025, contra 36.623 + 24.467 = 61.090 em
2024 — ou seja, empréstimos **caíram** ~12.176; não houve captação de 101.195.

**Decisão:** confirmar que 101.195 é erro do relatório. Se for, nada a ajustar
no sistema — a query corrigida já produz o número certo.

## 2. Linha 4.4 do DFC / "Lucro distribuídos" do DMPL — falta exclusão

Calculado **-11.500**, alvo **-5.000**. Diferença **6.500**.

A conta `2.3.04.01.000009` varia 11.500 no ano, mas isso mistura duas coisas:
a distribuição de lucros de 5.000 e a transferência do lucro de 2024 (6.500).
Não há exclusão cadastrada para o grupo 4.4.

**Decisão:** cadastrar em `DET_DEMONSTRATIVO_CTACTB_EXC` a conta que
representa a transferência do resultado, para o grupo 4.4 (DFC) e para o
grupo 2 do DMPL — mesmo mecanismo já usado em 2.1, 2.7, 3.3, 3.4 e 4.1.
Preciso que você indique qual conta registra essa transferência.

## 3. Indicadores — a imagem que você mandou contém o bug

A Liquidez Seca aparece como 1,87 (2025) e 1,98 (2024), **idênticas à
Liquidez Corrente**, e a linha "AC − ESTOQUE" mostra 86.086, que é o próprio
AC sem subtrair estoque. Isso é o efeito do `LIKE '1.1.3.%'`.

Valores corretos com a query ajustada:

| | 2025 | 2024 |
|---|---:|---:|
| Liquidez Corrente | 1,87 | 1,98 |
| **Liquidez Seca** | **1,66** | **1,40** |
| Liquidez Geral | 1,10 | 1,10 |
| Solvência Geral | 1,26 | 1,28 |

Estoque é 9.800 (2025) e 27.816 (2024) — ignorá-lo distorce muito 2024.

**Decisão:** confirmar que a seca deve passar a 1,66 / 1,40. Se o relatório
oficial já foi emitido com 1,87 / 1,98, avise antes de publicar a correção.

## 4. DMPL — a query não produz o formato do relatório

`queryDMPL.sql` devolve uma linha por ORDEM. O relatório é uma matriz:
4 colunas (Capital Social, Ajuste de Avaliação, Lucros ou Prejuízos
Acumulados, Total) e linhas alternando saldo e movimento por exercício.

Além do formato, os números não fecham:

| Linha | Calculado 2025 | Alvo |
|---|---:|---:|
| Lucro distribuídos | 11.500 | (5.000) |
| Lucro do exercício | 911 | 911 ✓ |
| Ajuste de Avaliação | 0 | (12) |

O "Ajuste de Avaliação" de (12) não tem conta com esse saldo mapeada no
grupo. Aqui é reescrita da query, não ajuste pontual — me diga se quer que
eu faça, e como a estrutura deve mapear cada coluna.

## 5. Cosmético — nome de grupo com espaço duplo

`DET_DEMONSTRATIVO` ORDEM 2.2.3 está gravado como `'Mútuos a pagar  (PNC)'`,
com dois espaços. O valor sai certo, mas como as queries agrupam por
`NOME_GRUPO`, qualquer join ou comparação por nome trata como grupo distinto.

```sql
UPDATE DET_DEMONSTRATIVO SET NOME_GRUPO = 'Mútuos a pagar (PNC)'
 WHERE ID_ESTR_DEMONSTRATIVO = 1 AND ORDEM = '2.2.3';
```

## 6. Duplicação latente no Balanço (saldo zero hoje)

`1.1.03.03.000006` está ligada a "Mútuo a receber" (ID 3086) e a
"Adiantamentos" (ID 3197) na referência 2024-12-01. Saldo 0,00, então não
afeta nada hoje — mas é dupla contagem esperando lançamento. Não removi
porque não sei qual dos dois grupos é o correto.
