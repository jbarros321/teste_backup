# Validação do Cálculo do Preço Unitário: 1.286,10

## Contexto
- **Produto**: 104 - EMPREITADA 702
- **Cargo**: 3 - ELETRICISTA INDUSTRIAL Sênior (SR)
- **Unidade**: DI (DIAS - DIÁRIAS)
- **Quantidade**: 5
- **AD_MARGLUCSERV**: NULL
- **Valor Unitário Final**: 1.286,10

## Dados do Cadastro de Cargos

### Cargo Base (HR - Horas)
- **NUCARGO**: 3
- **Valor (HR)**: 71,45 (obtido de `AD_CARGOS.VALOR`)

### Unidade Alternativa (DI - Dias)
- **CODVOLALT**: DI
- **DIVMULT**: 'M' (Multiplica)
- **QTDHORAS**: 9,0
- **Valor da unidade**: 643,05 (calculado automaticamente)

## Cálculo Passo a Passo

### Passo 1: Obter Preço Base do Cargo (HR)
```sql
SELECT VALOR INTO P_PRECO_CARGO
FROM AD_CARGOS
WHERE NUCARGO = 3;
```
**Resultado**: `P_PRECO_CARGO = 71,45`

### Passo 2: Calcular Preço Alternativo (DI)
Como a unidade do item é `DI` (não é `HR`), a trigger busca a unidade alternativa:

```sql
SELECT CODVOLALT, DIVMULT, QTDHORAS
INTO P_CODVOLALT_CARGO, P_DIVMULT_CARGO, P_QTDHORAS_CARGO
FROM AD_CARGOSUNALT
WHERE NUCARGO = 3 AND CODVOLALT = 'DI';
```

**Resultado**:
- `P_CODVOLALT_CARGO = 'DI'`
- `P_DIVMULT_CARGO = 'M'` (Multiplica)
- `P_QTDHORAS_CARGO = 9,0`

Como `DIVMULT = 'M'` (Multiplica), o cálculo é:
```sql
P_PRECO_ALTERNATIVO := P_PRECO_CARGO * P_QTDHORAS_CARGO;
P_PRECO_ALTERNATIVO := 71,45 * 9,0;
P_PRECO_ALTERNATIVO := 643,05;
```

### Passo 3: Definir Preço Base
Como `:NEW.CODVOL = 'DI'` (não é 'HR'), o preço base é o preço alternativo:
```sql
P_PRECO_BASE := P_PRECO_ALTERNATIVO;
P_PRECO_BASE := 643,05;
```

### Passo 4: Obter Fator de Margem de Lucro para Serviços
A trigger busca o parâmetro `FATORMARGLUCSER`:
```sql
SELECT NUMDEC INTO P_FATOR_MARGEM_LUCRO_SERV
FROM TSIPAR
WHERE CHAVE = 'FATORMARGLUCSER';
```

**Resultado**: `P_FATOR_MARGEM_LUCRO_SERV = 2,00` (padrão se não encontrado)

### Passo 5: Aplicar Margem de Lucro
Como `P_USOPROD = 'S'` (Serviço) e `P_PRECOCARGO = 'S'` (usa preço por cargo):

```sql
IF NVL(P_MARGLUCSERV, 0) > 0 THEN
    -- Aplicar margem percentual
    P_PRECO_BASE := P_PRECO_BASE / (1 - (P_MARGEM_TOTAL / 100));
ELSE
    -- Quando AD_MARGLUCSERV está NULL ou 0, usar FATORMARGLUCSER como multiplicador
    P_PRECO_BASE := P_PRECO_BASE * P_FATOR_MARGEM_LUCRO_SERV;
END IF;
```

Como `AD_MARGLUCSERV = NULL` (ou seja, `NVL(P_MARGLUCSERV, 0) = 0`), a trigger usa o **fator multiplicador**:

```sql
P_PRECO_BASE := P_PRECO_BASE * P_FATOR_MARGEM_LUCRO_SERV;
P_PRECO_BASE := 643,05 * 2,00;
P_PRECO_BASE := 1.286,10;
```

### Passo 6: Arredondar e Definir Valor Unitário
```sql
P_PRECO_FINAL := ROUND(P_PRECO_BASE, 2);
P_PRECO_FINAL := ROUND(1.286,10, 2);
P_PRECO_FINAL := 1.286,10;

:NEW.VLRUNIT := P_PRECO_FINAL;
:NEW.VLRUNIT := 1.286,10;
```

## Resumo da Fórmula

```
Valor Unitário = (Valor Cargo HR × QTDHORAS) × FATORMARGLUCSER
Valor Unitário = (71,45 × 9,0) × 2,00
Valor Unitário = 643,05 × 2,00
Valor Unitário = 1.286,10
```

## Observações Importantes

1. **Quando `AD_MARGLUCSERV = NULL` ou `0`**:
   - A trigger usa `FATORMARGLUCSER` como **multiplicador direto**
   - Não aplica fórmula de margem percentual

2. **Quando `AD_MARGLUCSERV > 0`**:
   - A trigger aplica fórmula de margem percentual: `Preço / (1 - (Margem / 100))`
   - Exemplo: Se `AD_MARGLUCSERV = 50`, então `Preço = 643,05 / (1 - 0,50) = 1.286,10`

3. **Carga Tributária**:
   - **NÃO se aplica** para serviços (`USOPROD = 'S'`)
   - Só se aplica para produtos (`USOPROD <> 'S'`)

4. **Fator de Prazo**:
   - O fator de prazo (2% a cada 30 dias) **não está sendo aplicado** no cálculo do preço unitário
   - Ele é calculado mas não usado na fórmula final (linhas 335-343)

## Validação

✅ **Cálculo confirmado**: O valor unitário de **1.286,10** está correto quando:
- Cargo 3 (HR) = 71,45
- Unidade alternativa DI com QTDHORAS = 9,0
- DIVMULT = 'M' (Multiplica)
- AD_MARGLUCSERV = NULL
- FATORMARGLUCSER = 2,00

