# Parcelamento Manual de Títulos — `STP_PARCELARFINA_SATIS`

Procedure de **Ação de Botão** do Sankhya que divide um título a receber (`TGFFIN`) em N parcelas,
com valores rateados automaticamente ou informados manualmente, aplicando juros opcionais em
percentual ou valor fixo.

- **Arquivo original (com defeitos):** `PARCELA.SQL`
- **Arquivo corrigido:** `STP_PARCELARFINA_SATIS.sql`
- **Tabelas afetadas:** `TGFFIN` (UPDATE + INSERT), `TGFFRE` (INSERT — rastreabilidade do acerto)
- **Campos AD_ exigidos em `TGFFIN`:** `AD_PARCELADO`, `AD_FINANORIGINAL`, `AD_USUDEPARCELAMENTO`, `AD_DTOPERPARC`

---

## 1. Assinatura

```sql
STP_PARCELARFINA_SATIS (
    P_CODUSU     NUMBER,       -- usuário logado (Sankhya injeta)
    P_IDSESSAO   VARCHAR2,     -- sessão da ação (Sankhya injeta)
    P_QTDLINHAS  NUMBER,       -- nº de linhas selecionadas na grade
    P_MENSAGEM   OUT VARCHAR2  -- retorno exibido ao usuário
)
```

---

## 2. Parâmetros da Ação de Botão

São os **9 parâmetros originais**, com os mesmos nomes. São eles que fazem o Sankhya abrir o
popup de preenchimento antes de executar a procedure.

| Nome cadastrado na Ação | Lido por | Valores | Descrição |
|---|---|---|---|
| `P_VLRAUTOMATIC` | `ACT_TXT_PARAM` | `S` / `N` | `S` = rateia o valor igualmente. `N` = valores informados em `P_DESCRPARCELA`. |
| `P_NROPARCELAS` | `ACT_INT_PARAM` | 2..360 | Quantidade de parcelas. |
| `P_DESCRPARCELA` | `ACT_TXT_PARAM` | `1=1000,00;2=500,00` | Lista `numero=valor` separada por `;`. Usada só no modo `N`. Aceita fora de ordem. |
| `P_BASEVENCIMEN` | `ACT_DTA_PARAM` | data | Vencimento da **1ª** parcela. |
| `P_RANGE` | `ACT_INT_PARAM` | dias > 0 | Intervalo entre parcelas. Assume 30 se vier nulo. |
| `PARAM_P_JUROS` | `ACT_TXT_PARAM` | `S` / `N` | Aplica juros sobre o total. Assume `N` se vier nulo. |
| `P_FORMAJUROS` | `ACT_TXT_PARAM` | `P` / `V` | `P` = percentual sobre o total. `V` = valor fixo somado ao total. |
| `P_TAXAJUROS` | `ACT_DEC_PARAM` | > 0 | Percentual (se `P`) ou valor em R$ (se `V`). |
| `P_DTNEG` | `ACT_TXT_PARAM` | `NO` / `H` | `NO` = mantém a data de negociação original. `H` = usa a data de hoje. |

> **Atenção ao nome `PARAM_P_JUROS`.** Todos os outros parâmetros usam o prefixo `P_`; só esse
> é lido com o prefixo `PARAM_P_`. O literal foi mantido igual ao original — se na Ação o
> parâmetro estiver cadastrado como `P_JUROS`, é só trocar o literal nessa linha da procedure,
> senão os juros nunca serão aplicados (o valor chega `NULL` e cai no padrão `N`).

Todos os valores de opção são normalizados com `UPPER` + `TRIM` + remoção de aspas, então
`'s'`, `` 'S' `` e `S` funcionam igual.

---

## 3. Fluxo de execução

Tudo em **uma única procedure**, sem subprogramas aninhados — mesmo formato do original.

```
1.  Lê os 9 parâmetros e normaliza (UPPER/TRIM/aspas, defaults de RANGE e JUROS)
2.  Valida os parâmetros           -> aborta antes de tocar em qualquer título
3.  Se modo = N: faz o parse e valida a lista de parcelas (uma vez só)
4.  Resolve NOMEUSU, próximo NUFIN e próximo NUACERTO (uma única vez)
5.  Para cada linha selecionada:
    5.1  ACT_INT_FIELD -> NUFIN
    5.2  SELECT * ... FOR UPDATE NOWAIT + 7 validações de elegibilidade
    5.3  Calcula juros sobre o total do título (uma vez)
    5.4  Monta os valores em V_VALOR / V_BASE (rateio ou lista manual)
    5.5  Grava: parcela 1 = UPDATE no título original
                parcelas 2..N = INSERT de cópias (TGFFIN%ROWTYPE)
                + INSERT em TGFFRE para cada parcela
6.  P_MENSAGEM := resumo
```

**Sem `COMMIT`.** A transação é do Sankhya: ou o parcelamento inteiro grava, ou nada grava.

---

## 4. Blocos internos

### 4.1 Leitura e normalização dos parâmetros

Chamadas diretas a `ACT_*_PARAM`, como no original. Depois:

```sql
PARAM_P_VLRAUTOMATIC := NVL(UPPER(TRIM(REPLACE(PARAM_P_VLRAUTOMATIC, '''', ''))), 'X');
PARAM_P_RANGE        := NVL(PARAM_P_RANGE, 30);
PARAM_P_TAXAJUROS    := NVL(PARAM_P_TAXAJUROS, 0);
```

O `'X'` como padrão de `P_VLRAUTOMATIC` e `P_FORMAJUROS` é proposital: um parâmetro em branco
cai numa mensagem de erro clara em vez de passar despercebido.

### 4.2 Validação dos parâmetros

Roda **antes** de qualquer escrita: linhas selecionadas ≥ 1; modo `S`/`N` informado; parcelas
entre 2 e 360; data base preenchida; intervalo > 0; coerência de juros (`P`/`V`, taxa > 0,
percentual ≤ 100); lista obrigatória no modo manual; **modo manual = 1 título por execução**.

### 4.3 Parse da lista de parcelas (modo `N`)

Split de `P_DESCRPARCELA` por `;` via `CONNECT BY`, depois por `=`. Carrega
`V_ENTRADA(numero_da_parcela)`, o que resolve ordem e duplicidade de uma vez:

- número não inteiro, `< 1` ou `> P_NROPARCELAS` → `-20114`
- `V_ENTRADA.EXISTS(n)` → parcela duplicada → `-20114`
- quantidade de itens ≠ `P_NROPARCELAS` → `-20112`
- buraco na sequência 1..N → `-20114`
- valor ≤ 0 ou não numérico → `-20115`

**Conversão do valor:** remove `R$`, espaço, tab e espaço não-quebrável; se houver vírgula
trata como pt-BR (`1.234,56`), senão como decimal com ponto (`1234.56`); valida por regex antes
do `TO_NUMBER`. É isso que evita `"1500.00"` virar `150000`.

### 4.4 Elegibilidade do título

`SELECT * INTO V_ORIG FROM TGFFIN WHERE NUFIN = V_NUFIN FOR UPDATE NOWAIT` — bloqueio
pessimista, com `ORA-00054` mapeado para `-20110`. Rejeita: inexistente, `DHBAIXA` preenchida,
`RECDESP <> 1`, `VLRBAIXA > 0`, `VLRDESDOB <= 0`, `AD_PARCELADO = 'S'`, `RATEADO = 'S'`.

### 4.5 Cálculo dos valores

`V_BASE(i)` é a parcela **sem** juros (usada para ratear retenções); `V_VALOR(i)` é o que vai
para `VLRDESDOB`.

Juros calculados **uma única vez sobre o total do título**, nos dois modos:

```sql
IF PARAM_P_JUROS = 'S' AND PARAM_P_FORMAJUROS = 'P' THEN V_JUROS := ROUND(V_TOTALTIT * (PARAM_P_TAXAJUROS/100), 2);
ELSIF PARAM_P_JUROS = 'S' AND PARAM_P_FORMAJUROS = 'V' THEN V_JUROS := ROUND(PARAM_P_TAXAJUROS, 2);
ELSE V_JUROS := 0; END IF;
```

- **Modo `S`:** parcelas 1..N-1 = `TRUNC(total/N, 2)`; a última recebe `total - acumulado`.
- **Modo `N`:** valida `soma informada = valor do título` (tolerância R$ 0,005), depois
  redistribui `V_ENTRADA(i) * total_com_juros / soma`, com o resíduo na última parcela.

Nos dois casos a soma fecha **exatamente** no centavo.

### 4.6 Gravação

- **Parcela 1** → `UPDATE` no `NUFIN` original (mantém o `DESDOBRAMENTO` que já tinha).
- **Parcelas 2..N** → `V_NOVO := V_ORIG; ... INSERT INTO TGFFIN VALUES V_NOVO`, sem lista de
  colunas. `DESDOBRAMENTO` = maior desdobramento existente na mesma `NUNOTA` + k, para não
  colidir com parcelas já existentes da nota.
- **Vencimento:** `TRUNC(P_BASEVENCIMEN) + (i-1) * P_RANGE` — parcela 1 vence **na** data base.
- **Retenções** (`VLRIRF`, `VLRISS`, `VLRINSS`, `VLRDESC`, `VLRVENDOR`, `VLRPROV`, `VLRHONOR`)
  rateadas proporcionalmente por `V_BASE(i) / VLRDESDOB` original.
- **Zerados** nas novas parcelas: juros, multa, e todos os campos de baixa
  (`VLRBAIXA`, `DHBAIXA`, `CODTIPOPERBAIXA`, `DHTIPOPERBAIXA`, `VLRMOEDABAIXA`, embutidos,
  negociados e liberados).
- **`TGFFRE`**: um `NUACERTO` por execução, `SEQUENCIA` incremental, `NUFIN` da parcela gerada
  e `NUFINORIG` do título de origem, em **ambos** os modos.

### 4.7 Tratamento de erro

```sql
EXCEPTION
   WHEN OTHERS THEN
        IF SQLCODE BETWEEN -20999 AND -20000 THEN RAISE; END IF;
        RAISE_APPLICATION_ERROR(-20199, 'FALHA AO PARCELAR O TITULO ' || ... || ': ' || SQLERRM);
```

Erros de negócio sobem com a mensagem original; qualquer ORA inesperado vira `-20199`
identificando o `NUFIN` em que parou.

---

## 5. Códigos de erro

| Código | Situação |
|---|---|
| `-20101` | Parâmetro inválido ou ausente |
| `-20102` | Nenhum título selecionado |
| `-20103` | Título não encontrado / `NUFIN` nulo |
| `-20104` | Título já baixado |
| `-20105` | Título não é receita (`RECDESP <> 1`) |
| `-20106` | Título com baixa parcial |
| `-20107` | Valor do título zerado, negativo ou insuficiente para N parcelas |
| `-20108` | Título já parcelado anteriormente |
| `-20109` | Título com rateio |
| `-20110` | Título bloqueado por outro usuário |
| `-20111` | Lista de parcelas ausente ou fora do formato |
| `-20112` | Quantidade de parcelas divergente |
| `-20113` | Soma das parcelas ≠ valor do título |
| `-20114` | Número de parcela inválido, duplicado ou faltante |
| `-20115` | Valor de parcela inválido ou ≤ 0 |
| `-20116` | Configuração de juros inválida |
| `-20117` | Mais de um título selecionado no modo manual |
| `-20199` | Erro inesperado (ORA original no texto) |

---

## 6. Defeitos encontrados na versão original

### 6.1 Bloqueantes

| # | Local | Problema |
|---|---|---|
| 1 | linha 136 | `P_MAXSEQ_DESP` **nunca declarada** → `PLS-00201`. A procedure **não compila**. |
| 2 | linha 108 | `RAISE_APPLICATION_ERROR` de depuração deixado no meio do fluxo, disparando **sempre** com a mensagem "Somente Titulos de Receitas..."`||P_NEGOCI`. O modo automático (`S`) **nunca chegava a gravar nada**. |
| 3 | linha 48 | `ACT_TXT_PARAM(P_IDSESSAO, 'PARAM_P_JUROS')` — nome errado (os demais usam prefixo `P_`). Resultado: `PARAM_P_JUROS` sempre `NULL` e, por causa do `NVL(...,'N')`, **os juros nunca eram aplicados**. |
| 4 | linhas 272-324 | No modo manual, o `UPDATE TGFFIN` está **fora** do `IF P_PARCELA > 1`. Ele roda em **toda** iteração, então o título original é sobrescrito N vezes e termina com o valor e o vencimento da **última** parcela, mas com `HISTORICO` fixo em `'PARCELA 1/N'` (o `||1||` está hard-coded). |
| 5 | linha 310 | `P_NEG` **nunca é carregada** no ramo manual (só é lida no ramo automático, linha 77). Com `P_DTNEG = 'NO'`, `DTNEG` das parcelas fica `NULL`. |

### 6.2 Integridade financeira

| # | Problema |
|---|---|
| 6 | **Centavos perdidos.** `ROUND(total/N, 2)` aplicado a todas as parcelas: R$ 100,00 em 3× gera 33,33 × 3 = R$ 99,99. Falta jogar a diferença na última parcela. |
| 7 | **Juros por valor multiplicado por N.** No modo manual (linhas 300-302) o valor fixo de juros é somado a **cada** parcela. Juros de R$ 50 em 10 parcelas viram R$ 500. No modo automático o mesmo juro é somado uma única vez — comportamentos divergentes. |
| 8 | **`CASE` sem `ELSE`** nos cálculos de juros (linhas 86-92, 298-303) e de `DTNEG` (linha 310). Se `P_FORMAJUROS` vier diferente de `P`/`V`, o resultado é `NULL` e `VLRDESDOB` fica `NULL`. |
| 9 | **Vencimento inconsistente.** Automático usa `base + (PARC-1)*range` (1ª parcela na data base); manual usa `base + PARCELA*range` (1ª parcela em base+30). |
| 10 | **Encargos duplicados.** A cópia leva `VLRDESC`, `VLRIRF`, `VLRISS`, `VLRINSS`, `VLRMULTA`, `VLRJURO` **integrais** para cada parcela — retenções e descontos multiplicados por N. |
| 11 | **Campos de baixa copiados** (`VLRBAIXA`, `CODTIPOPERBAIXA`, `DHTIPOPERBAIXA`) em vez de zerados. |
| 12 | **`DESDOBRAMENTO` reiniciado em 1..N** sem considerar os desdobramentos já existentes da mesma `NUNOTA` — colide com parcelas da nota. |
| 13 | **`RATEADO='S'` é copiado** para as novas parcelas, mas nenhum rateio é gerado em `TGFRAT`. |

### 6.3 Concorrência e transação

| # | Problema |
|---|---|
| 14 | **`COMMIT` dentro da procedure** (linhas 136 e 363). Se a parcela 5 falhar, as parcelas 1-4 já estão gravadas e o título fica parcelado pela metade, sem rollback. Também quebra a transação gerenciada pelo Sankhya. |
| 15 | **`SELECT MAX(NUFIN)+1` dentro do loop**, sem lock. Dois usuários simultâneos geram o mesmo `NUFIN` → `ORA-00001`, ou pior, gravação cruzada. |
| 16 | **Sem lock no título**. Dois usuários podem parcelar o mesmo `NUFIN` ao mesmo tempo. |
| 17 | **`SELECT NVL(MAX(NUACERTO)) ...` sem `NVL`** (linha 57): com `TGFFRE` vazia, `P_NUACERTO+1` é `NULL`. |

### 6.4 Validação e usabilidade

| # | Problema |
|---|---|
| 18 | Sem checagem de `PARAM_P_NROPARCELAS` nulo ou zero → `ORA-01476` (divisão por zero) no modo automático. |
| 19 | Sem checagem de reparcelamento (`AD_PARCELADO='S'`), de baixa parcial (`VLRBAIXA>0`) nem de `PARAM_P_BASEVENCIMEN` nula. |
| 20 | **Parsing de valor frágil**: `REPLACE(REPLACE(x,'.',''),',','.')` transforma `"1500.00"` em `150000`. Erro de 100× silencioso. |
| 21 | `P_PARCELA NUMBER := REGEXP_SUBSTR(...)` — conversão implícita. Texto não numérico gera `ORA-06502` cru, sem mensagem ao usuário. |
| 22 | Validação `IF P_PARCELAS(I) <> I` obriga o usuário a digitar as parcelas **em ordem**; `2=100;1=200` é rejeitado mesmo estando correto. |
| 23 | `ACT_INT_FIELD(P_IDSESSAO, ROWNUM, 'NUFIN')` dentro de `CONNECT BY LEVEL` (linha 239) — `ROWNUM` em consulta hierárquica não é garantido; deveria ser `LEVEL`, e chamar função PL/SQL ali é frágil. |
| 24 | Com vários títulos selecionados no modo manual, a soma das parcelas é comparada com o **total de todos os títulos**, mas a mesma lista é aplicada a **cada** título. |
| 25 | `P_MENSAGEM` só é preenchida no ramo `'N'`. No modo automático o usuário recebe retorno vazio. |
| 26 | Se `P_VLRAUTOMATIC` não for `S` nem `N`, **nada acontece** e nenhuma mensagem é exibida — parece sucesso. |
| 27 | Nenhum `EXCEPTION` handler: erros ORA chegam crus ao usuário. |
| 28 | `SELECT ... INTO` sem tratamento de `NO_DATA_FOUND` (usuário e título). |
| 29 | Comparações `= 'S'` sem `UPPER`/`TRIM`; o `REPLACE(P_DTNEG,'''','')` da linha 53 é gambiarra para aspas vindas do parâmetro. |
| 30 | As duas listas de `INSERT` (60+ colunas cada) são **diferentes entre si** — o ramo automático não copia `VLRDESCEMBUT`, `VLRJUROEMBUT`, `VLRMULTAEMBUT`, `VLRMOEDA`, `DTPRAZO` etc. Além de duplicação, qualquer coluna nova do Sankhya quebra a rotina. |

---

## 7. O que mudou na versão corrigida

- **Sem `COMMIT`** — atomicidade garantida pela transação do Sankhya.
- **`INSERT INTO TGFFIN VALUES V_NOVO`** a partir de `TGFFIN%ROWTYPE`: as duas listas de 60 colunas
  desapareceram; atualizações de versão do Sankhya que adicionem colunas não quebram mais a rotina.
- **`FOR UPDATE NOWAIT`** no título + `NUFIN` sequenciado uma única vez fora do loop.
- **Fechamento exato ao centavo** — resíduo sempre na última parcela, nos dois modos.
- **Juros calculados uma única vez sobre o total**, nos dois modos, com `ELSE 0` em todos os `CASE`.
- **Vencimento unificado**: parcela 1 vence na data base, nos dois modos.
- **Todas as validações antes da primeira escrita**, com mensagens de negócio e códigos `-201xx` estáveis.
- **Retenções rateadas proporcionalmente**; juros, multa e campos de baixa zerados nas novas parcelas.
- **`DESDOBRAMENTO` calculado** a partir do maior desdobramento existente na nota.
- **`TGFFRE` gravado nos dois modos**, com `NUFIN`/`NUFINORIG` corretos.
- **Parcelas fora de ordem aceitas** no modo manual; buracos e duplicidades detectados.
- Mensagens sem acentuação, para evitar problema de *charset* entre cliente e banco.

**O que NÃO mudou, de propósito:** a assinatura, os 9 parâmetros e seus nomes literais
(inclusive `'PARAM_P_JUROS'`), e o formato de procedure única e plana — sem funções ou
procedures aninhadas, sem package auxiliar. Nenhum parâmetro novo foi criado, então a Ação
de Botão existente continua valendo sem alteração.

---

## 8. Pendências antes de subir para produção

1. **Geração de `NUFIN`** — a rotina usa `MAX(NUFIN)+1` (como o original). Se a base usa uma tabela
   de sequência do Sankhya para `TGFFIN.NUFIN`, o valor gerado aqui **não é reservado lá** e pode
   colidir com um lançamento feito pela tela. Confirmar o mecanismo da instalação e, se for o caso,
   trocar `SELECT NVL(MAX(NUFIN),0)+1` pela sequência oficial. *É o único ponto que herdei sem correção
   por depender do ambiente.*
2. **`TGFFRE`** — as colunas e o `TIPACERTO = 'A'` vieram do código original e não foram validados
   contra o dicionário. Se a semântica não for essa, basta remover os dois `INSERT INTO TGFFRE`.
3. **`INSERT ... VALUES record`** falha se `TGFFIN` tiver coluna virtual ou identity na instalação.
   Testar em homologação; se ocorrer, a alternativa é voltar à lista explícita de colunas.
4. **Títulos com rateio** (`RATEADO='S'`) estão **bloqueados**. Para liberá-los é preciso replicar
   as linhas de rateio proporcionalmente — não implementado.
5. **Nome do parâmetro de juros** — confirmar na Ação se é `PARAM_P_JUROS` ou `P_JUROS` e alinhar
   o literal na procedure. Enquanto não bater, o parâmetro chega `NULL` e os juros não são aplicados.
6. **Dias úteis e feriados** — o vencimento é sempre data base + N dias, sem desviar de fim de
   semana ou feriado. Se a regra exigir, é preciso um parâmetro novo na Ação.
7. **Boletos emitidos** — não há checagem de título com boleto/cobrança registrada. Avaliar se deve
   bloquear o parcelamento nesse caso.
8. **Estorno** — não existe rotina para desfazer um parcelamento. `AD_FINANORIGINAL` guarda a origem,
   o que torna o estorno viável, mas ele precisa ser escrito.

---

## 9. Testes

Implementados em `TESTE/` — ver `TESTE/README.md`.

- **`TESTE/simulador_calculo.py`** — porta a aritmética das duas versões para Python
  (`Decimal` + `ROUND_HALF_UP`, reproduzindo `ROUND`/`TRUNC` do Oracle) e compara lado a lado.
  Roda sem banco. Saída em `TESTE/resultado_simulacao.txt`.
- **`TESTE/PKG_MOCK_ACT.sql` + `TESTE/STP_PARCELARFINA_SATIS_TESTE.sql` + `TESTE/RODAR_TESTES.sql`**
  — 31 asserções contra o Oracle de homologação, clonando títulos para a faixa
  `NUFIN >= 900.000.000` e terminando em `ROLLBACK`.

### Matriz de cenários


| Cenário | Entrada | Esperado |
|---|---|---|
| Rateio com dízima | R$ 100,00 · 3 parcelas · automático | 33,33 / 33,33 / 33,34 — soma 100,00 |
| Juros percentual | R$ 1.000,00 · 2 parcelas · `P` 10% | 550,00 / 550,00 — soma 1.100,00 |
| Juros valor fixo | R$ 1.000,00 · 4 parcelas · `V` 100 | soma 1.100,00 (**não** 1.400,00) |
| Manual fora de ordem | `3=300,00;1=500,00;2=200,00` sobre R$ 1.000,00 | aceito, vencimentos 1→base, 2→base+30, 3→base+60 |
| Manual com buraco | `1=500,00;3=500,00` com N=3 | erro `-20114` |
| Manual soma errada | `1=400,00;2=400,00` sobre R$ 1.000,00 | erro `-20113` |
| Valor formato US | `1=1500.00` | aceito como 1.500,00 |
| Título baixado | qualquer | erro `-20104`, nada gravado |
| Reparcelamento | título já com `AD_PARCELADO='S'` | erro `-20108` |
| Falha no meio | forçar erro na parcela 3 | **nenhuma** parcela gravada |
| Concorrência | dois usuários no mesmo `NUFIN` | segundo recebe `-20110` |
