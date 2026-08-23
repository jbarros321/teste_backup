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

| Parâmetro | Tipo | Obrigatório | Valores | Descrição |
|---|---|---|---|---|
| `P_VLRAUTOMATIC` | Texto/Opção | Sim | `S` / `N` | `S` = rateia o valor igualmente entre as parcelas. `N` = valores informados manualmente em `P_DESCRPARCELA`. |
| `P_NROPARCELAS` | Inteiro | Sim | 2..360 | Quantidade de parcelas. |
| `P_DESCRPARCELA` | Texto | Só quando `P_VLRAUTOMATIC = 'N'` | `1=1000,00;2=500,00` | Lista `numero=valor` separada por `;`. Aceita fora de ordem. |
| `P_BASEVENCIMEN` | Data | Sim | — | Vencimento da **1ª** parcela. |
| `P_RANGE` | Inteiro | Não (padrão 30) | > 0 | Intervalo entre parcelas (dias, ou meses se `P_TIPOINTERVALO='M'`). |
| `P_JUROS` | Texto/Opção | Não (padrão `N`) | `S` / `N` | Aplica juros sobre o total. |
| `P_FORMAJUROS` | Texto/Opção | Só quando `P_JUROS='S'` | `P` / `V` | `P` = percentual sobre o total. `V` = valor fixo somado ao total. |
| `P_TAXAJUROS` | Decimal | Só quando `P_JUROS='S'` | > 0 | Percentual (se `P`) ou valor em R$ (se `V`). |
| `P_DTNEG` | Texto/Opção | Não (padrão `NO`) | `NO` / `H` | `NO` = mantém a data de negociação original. `H` = usa a data de hoje. |
| `P_TIPOINTERVALO` | Texto/Opção | Não (padrão `D`) | `D` / `M` | **Novo.** `D` = intervalo em dias. `M` = mensal por `ADD_MONTHS` (mantém o dia do mês). |
| `P_AJUSTADIAUTIL` | Texto/Opção | Não (padrão `N`) | `S` / `N` | **Novo.** Empurra vencimentos que caem em sábado/domingo para a segunda-feira. |
| `P_PERMITEVENCPASS` | Texto/Opção | Não (padrão `N`) | `S` / `N` | **Novo.** Libera data base de vencimento retroativa. |

> Os 3 parâmetros novos são **opcionais de verdade**: a leitura é feita por wrappers que devolvem
> o valor padrão caso o campo não exista na tela do botão. A procedure funciona sem alterar a Ação.

---

## 3. Fluxo de execução

```
1. Lê e normaliza todos os parâmetros (FN_PARAM_*)
2. PR_VALIDA_PARAMETROS            -> aborta antes de tocar em qualquer título
3. Resolve NOMEUSU, próximo NUFIN e próximo NUACERTO (uma única vez)
4. Para cada linha selecionada:
   4.1 ACT_INT_FIELD -> NUFIN
   4.2 FN_CARREGA_TITULO           -> lock + 7 validações de elegibilidade
   4.3 PR_PLANO_AUTOMATICO | PR_PLANO_MANUAL  -> monta o plano em memória
   4.4 PR_APLICA_PLANO             -> parcela 1 = UPDATE no título original
                                      parcelas 2..N = INSERT de cópias
                                      + PR_REGISTRA_ACERTO em TGFFRE
5. P_MENSAGEM := resumo
```

**Sem `COMMIT`.** A transação é do Sankhya: ou o parcelamento inteiro grava, ou nada grava.

---

## 4. Funções e procedures internas

### 4.1 Leitura de parâmetros

| Rotina | Retorno | O que faz |
|---|---|---|
| `FN_PARAM_TXT(nome, padrao)` | `VARCHAR2` | `ACT_TXT_PARAM` com `TRIM`. Preserva maiúsculas/minúsculas — usado na lista de parcelas. |
| `FN_PARAM_OPC(nome, padrao)` | `VARCHAR2` | Idem, mas `UPPER` + remove aspas simples/duplas. Usado nos campos de opção (`S`/`N`, `P`/`V`, `NO`/`H`). |
| `FN_PARAM_INT(nome, padrao)` | `PLS_INTEGER` | `ACT_INT_PARAM` com fallback. |
| `FN_PARAM_DEC(nome, padrao)` | `NUMBER` | `ACT_DEC_PARAM` com fallback. |
| `FN_PARAM_DTA(nome, padrao)` | `DATE` | `ACT_DTA_PARAM` com fallback. |

Todas capturam exceção e devolvem o padrão — é isso que torna os parâmetros novos opcionais.

### 4.2 Conversão e cálculo

| Rotina | Retorno | O que faz |
|---|---|---|
| `FN_NUM_BR(txt)` | `NUMBER` | Converte valor digitado. **Detecta o formato**: se tem vírgula trata como pt-BR (`1.234,56`), senão como decimal com ponto (`1234.56`). Remove `R$`, tab e espaço não-quebrável. Valida por regex e lança `-20115` com o texto ofensor. |
| `FN_JUROS(base)` | `NUMBER` | `0` se `P_JUROS <> 'S'`. `P` → `base * taxa/100`. `V` → `taxa`. **Sempre `ELSE 0`** — nunca devolve `NULL`. |
| `FN_VENCIMENTO(parcela)` | `DATE` | `base + (parcela-1) * range` ou `ADD_MONTHS(base, parcela-1)`. Parcela 1 vence **na** data base. Ajuste de fim de semana via `TRUNC(dt) - TRUNC(dt,'IW')` (independente de NLS). |
| `FN_DTNEG(dtneg_original)` | `DATE` | `H` → hoje; qualquer outro → data original, com fallback para hoje se nula. |

### 4.3 Validação

| Rotina | O que faz |
|---|---|
| `PR_VALIDA_PARAMETROS` | Roda **antes** de qualquer escrita: linhas selecionadas ≥ 1; modo `S`/`N` informado; parcelas entre 2 e 360; data base preenchida e não retroativa; intervalo > 0; coerência de juros; lista obrigatória no modo manual; **modo manual = 1 título por execução**. |
| `FN_CARREGA_TITULO(nufin)` | `SELECT * ... FOR UPDATE NOWAIT` (bloqueio pessimista) e retorna o `TGFFIN%ROWTYPE`. Rejeita: inexistente, `DHBAIXA` preenchida, `RECDESP <> 1`, `VLRBAIXA > 0` (baixa parcial), `VLRDESDOB <= 0`, `AD_PARCELADO = 'S'` (reparcelamento), `RATEADO = 'S'`. |

### 4.4 Montagem do plano

O plano é uma coleção em memória `V_PLANO(i) = {NUMERO, VALORBASE, VALOR, VENCIMENTO}`.
`VALORBASE` é a parcela **sem** juros (usada para ratear retenções); `VALOR` é o que vai para `VLRDESDOB`.

| Rotina | O que faz |
|---|---|
| `PR_PLANO_AUTOMATICO(vlr)` | `total = vlr + FN_JUROS(vlr)`; parcelas 1..N-1 = `TRUNC(total/N, 2)`; **a última recebe `total - soma_das_anteriores`**, garantindo fechamento exato ao centavo. |
| `PR_PLANO_MANUAL(vlr)` | Faz o *split* de `P_DESCRPARCELA` por `;` e por `=`, carrega numa *associative array* indexada pelo número da parcela. Valida: formato, número inteiro ≥ 1, número ≤ N, duplicidade, quantidade = N, **ausência de buracos (1..N)**, valor > 0, soma = valor do título (tolerância R$ 0,005). Depois aplica juros **uma vez sobre a soma** e redistribui proporcionalmente, com o resíduo na última parcela. |

### 4.5 Gravação

| Rotina | O que faz |
|---|---|
| `PR_APLICA_PLANO(orig, dtneg)` | Parcela 1 → `UPDATE` no `NUFIN` original. Parcelas 2..N → cópia do registro via `V_NOVO := P_ORIG; ... INSERT INTO TGFFIN VALUES V_NOVO`, sem lista de colunas. Calcula `DESDOBRAMENTO` a partir do `MAX(DESDOBRAMENTO)` da mesma `NUNOTA` para não colidir com desdobramentos existentes. Rateia proporcionalmente `VLRIRF`, `VLRISS`, `VLRINSS`, `VLRDESC`, `VLRVENDOR`, `VLRPROV`, `VLRHONOR`; zera juros, multa e todos os campos de baixa. |
| `PR_REGISTRA_ACERTO(novo, orig, nunota)` | Insere em `TGFFRE` um `NUACERTO` por execução, com `SEQUENCIA` incremental, `NUFIN` da parcela gerada e `NUFINORIG` do título de origem. Desligável pela constante `C_GERA_ACERTO`. |

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
- **Vencimento unificado**: parcela 1 vence na data base, nos dois modos. Opção mensal e ajuste de fim de semana.
- **Todas as validações antes da primeira escrita**, com mensagens de negócio e códigos `-201xx` estáveis.
- **Retenções rateadas proporcionalmente**; juros, multa e campos de baixa zerados nas novas parcelas.
- **`DESDOBRAMENTO` calculado** a partir do maior desdobramento existente na nota.
- **`TGFFRE` gravado nos dois modos**, com `NUFIN`/`NUFINORIG` corretos.
- **Parcelas fora de ordem aceitas** no modo manual; buracos e duplicidades detectados.
- Mensagens sem acentuação, para evitar problema de *charset* entre cliente e banco.

---

## 8. Pendências antes de subir para produção

1. **Geração de `NUFIN`** — a rotina usa `MAX(NUFIN)+1` (como o original). Se a base usa uma tabela
   de sequência do Sankhya para `TGFFIN.NUFIN`, o valor gerado aqui **não é reservado lá** e pode
   colidir com um lançamento feito pela tela. Confirmar o mecanismo da instalação e, se for o caso,
   trocar `SELECT NVL(MAX(NUFIN),0)+1` pela sequência oficial. *É o único ponto que herdei sem correção
   por depender do ambiente.*
2. **`TGFFRE`** — as colunas e o `TIPACERTO = 'A'` vieram do código original e não foram validados
   contra o dicionário. Se a semântica não for essa, basta trocar `C_GERA_ACERTO` para `'N'`.
3. **`INSERT ... VALUES record`** falha se `TGFFIN` tiver coluna virtual ou identity na instalação.
   Testar em homologação; se ocorrer, a alternativa é voltar à lista explícita de colunas.
4. **Títulos com rateio** (`RATEADO='S'`) estão **bloqueados**. Para liberá-los é preciso replicar
   as linhas de rateio proporcionalmente — não implementado.
5. **Feriados** — o ajuste de dia útil só considera sábado e domingo. Integrar com a tabela de
   feriados do Sankhya se a regra exigir.
6. **Boletos emitidos** — não há checagem de título com boleto/cobrança registrada. Avaliar se deve
   bloquear o parcelamento nesse caso.
7. **Estorno** — não existe rotina para desfazer um parcelamento. `AD_FINANORIGINAL` guarda a origem,
   o que torna o estorno viável, mas ele precisa ser escrito.

---

## 9. Roteiro de teste sugerido

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
