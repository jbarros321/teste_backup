# Testes do parcelamento de títulos

Dois níveis de teste, porque não dá para validar tudo no mesmo lugar.

## 1. Simulação de cálculo (roda em qualquer máquina, sem banco)

```bash
python simulador_calculo.py
```

Porta a aritmética das **duas** versões (original e corrigida) para Python, com `Decimal` +
`ROUND_HALF_UP` para reproduzir o `ROUND`/`TRUNC` do Oracle. Compara lado a lado o rateio,
os juros, os vencimentos e o parsing de valores. Saída já gerada em `resultado_simulacao.txt`.

**O que ele prova:** os defeitos de cálculo são reais e reproduzíveis, e a versão corrigida fecha
os valores. **O que ele não prova:** sintaxe PL/SQL, `%ROWTYPE`, locks, constraints, transação.

## 2. Bateria no Oracle (rodar em HOMOLOGAÇÃO)

Cria títulos de teste clonando um título real para a faixa `NUFIN >= 900.000.000`, executa os
cenários e dá **`ROLLBACK` de tudo** — nada é commitado.

```sql
-- 1. mock das funções ACT_* da Ação de Botão
@PKG_MOCK_ACT.sql

-- 2. cópia da procedure com as chamadas ACT_ redirecionadas para o mock
--    (gerada por sed a partir de ../STP_PARCELARFINA_SATIS.sql — regerar a cada alteração)
@STP_PARCELARFINA_SATIS_TESTE.sql

-- 3. editar o DEFINE NUFIN_MODELO no topo com um NUFIN a receber qualquer
--    (serve só de molde de colunas; o título não é alterado) e executar
@RODAR_TESTES.sql
```

Saída esperada: `RESULTADO: 31 OK / 0 FALHA(S)` (31 asserções — a declaração da procedure `CHECA` não conta).

### Cenários cobertos

| # | Cenário | Verifica |
|---|---|---|
| 1 | R$ 100,00 em 3x automático | 33,33 / 33,33 / 33,34, soma exata, 1º vencimento na data base |
| 2 | R$ 1.000,00 em 2x, juros P 10% | 550 / 550 |
| 3 | R$ 1.000,00 em 4x manual, juros V R$ 100 | soma 1.100 (juros somado uma vez, não 4×) |
| 4 | Lista fora de ordem `3=300;1=500;2=200` | aceita e ordena |
| 5 | Estado do título original | vira a parcela **1**, não a última |
| 6 | Elegibilidade e validações | `-20104`, `-20108`, `-20113`, `-20114`, `-20101`, `-20115`, `-20116` e **nada gravado** após o erro |
| 7 | Rastreabilidade | campos `AD_*`, registros em `TGFFRE`, desdobramentos distintos |

### Regerar a cópia de teste após alterar a procedure

```bash
sed -e 's/CREATE OR REPLACE PROCEDURE STP_PARCELARFINA_SATIS (/CREATE OR REPLACE PROCEDURE STP_PARCELARFINA_SATIS_TESTE (/' \
    -e 's/\bACT_TXT_PARAM(/PKG_MOCK_ACT.ACT_TXT_PARAM(/g' \
    -e 's/\bACT_INT_PARAM(/PKG_MOCK_ACT.ACT_INT_PARAM(/g' \
    -e 's/\bACT_DEC_PARAM(/PKG_MOCK_ACT.ACT_DEC_PARAM(/g' \
    -e 's/\bACT_DTA_PARAM(/PKG_MOCK_ACT.ACT_DTA_PARAM(/g' \
    -e 's/\bACT_INT_FIELD(/PKG_MOCK_ACT.ACT_INT_FIELD(/g' \
    ../STP_PARCELARFINA_SATIS.sql > STP_PARCELARFINA_SATIS_TESTE.sql
```

### Limpeza

Não é necessária — o script termina em `ROLLBACK`. Se algo for interrompido no meio,
confira e remova a faixa de teste:

```sql
SELECT NUFIN, VLRDESDOB, HISTORICO FROM TGFFIN WHERE NUFIN >= 900000000;
-- DELETE FROM TGFFRE WHERE NUFIN >= 900000000;
-- DELETE FROM TGFFIN WHERE NUFIN >= 900000000;
```

> `PKG_MOCK_ACT` e `STP_PARCELARFINA_SATIS_TESTE` são objetos **de homologação**.
> Não subir para produção — lá vai apenas `../STP_PARCELARFINA_SATIS.sql`.
