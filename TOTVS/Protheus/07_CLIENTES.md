# TOTVS Protheus - Gestão de Clientes

## Tabela Principal: SA1

### Cadastro (MATA030)

A rotina **MATA030** é a principal para cadastro de clientes no Protheus, localizada no módulo **SIGAFAT** (Faturamento).

### Campos Essenciais

| Campo | Descrição | Obrigatório |
|:---|:---|:---|
| `A1_FILIAL` | Filial | ✅ |
| `A1_COD` | Código do cliente | ✅ |
| `A1_LOJA` | Loja (multi-endereço) | ✅ |
| `A1_NOME` | Razão social | ✅ |
| `A1_NREDUZ` | Nome reduzido/fantasia | ✅ |
| `A1_PESSOA` | Tipo pessoa (F/J) | ✅ |
| `A1_CGC` | CNPJ ou CPF | ✅ |
| `A1_INSCR` | Inscrição estadual | Depende |
| `A1_END` | Endereço | ✅ |
| `A1_MUN` | Município | ✅ |
| `A1_EST` | UF | ✅ |
| `A1_CEP` | CEP | ✅ |
| `A1_COD_MUN` | Código IBGE do município | ✅ |
| `A1_TEL` | Telefone | Não |
| `A1_EMAIL` | E-mail | Não |

### Campos Comerciais

| Campo | Descrição |
|:---|:---|
| `A1_VEND` | Vendedor vinculado |
| `A1_COND` | Condição de pagamento padrão |
| `A1_TABELA` | Tabela de preços |
| `A1_TIPO` | Tipo (F=Consumidor, L=Revendedor, R=Rural, S=Solidário, X=Exportação) |
| `A1_RISCO` | Classificação de risco (A, B, C, D, E) |

### Campos Financeiros

| Campo | Descrição |
|:---|:---|
| `A1_LC` | Limite de crédito |
| `A1_SALDUP` | Saldo de duplicatas |
| `A1_MPTS` | Maior título em aberto |
| `A1_MSBLQL` | Bloqueado (1=Sim, 2=Não) |
| `A1_ULTCOM` | Data última compra |
| `A1_PRICOM` | Data primeira compra |

### Chave Única
`A1_FILIAL` + `A1_COD` + `A1_LOJA`

> [!NOTE]
> O conceito de **Loja** (`A1_LOJA`) permite cadastrar múltiplos endereços para o mesmo cliente. Exemplo: `A1_COD = '000100'` + `A1_LOJA = '01'` (matriz) e `A1_LOJA = '02'` (filial).

---

## Consultas SQL para Clientes

### Listar Clientes Ativos
```sql
SELECT A1_COD, A1_LOJA, A1_NOME, A1_NREDUZ, A1_CGC,
    A1_MUN, A1_EST, A1_TEL, A1_EMAIL,
    A1_LC AS LIMITE_CREDITO, A1_RISCO
FROM SA1010
WHERE D_E_L_E_T_ = ' '
  AND A1_MSBLQL <> '1'
ORDER BY A1_NOME;
```

### Clientes Bloqueados
```sql
SELECT A1_COD, A1_LOJA, A1_NOME, A1_CGC, A1_LC, A1_SALDUP
FROM SA1010
WHERE D_E_L_E_T_ = ' '
  AND A1_MSBLQL = '1'
ORDER BY A1_NOME;
```

### Clientes Inativos (sem compra em 90+ dias)
```sql
SELECT A1_COD, A1_NOME, A1_ULTCOM,
    DATEDIFF(DAY, CAST(A1_ULTCOM AS DATE), GETDATE()) AS DIAS_INATIVO
FROM SA1010
WHERE D_E_L_E_T_ = ' '
  AND A1_MSBLQL <> '1'
  AND A1_ULTCOM <> ' '
  AND A1_ULTCOM < FORMAT(DATEADD(DAY, -90, GETDATE()), 'yyyyMMdd')
ORDER BY A1_ULTCOM;
```

### Ranking por Faturamento
```sql
SELECT 
    SA1.A1_COD, SA1.A1_NOME,
    COUNT(DISTINCT SC5.C5_NUM) AS QTD_PEDIDOS,
    SUM(SC6.C6_VALOR) AS TOTAL_FATURADO
FROM SA1010 SA1
INNER JOIN SC5010 SC5 ON SA1.A1_COD = SC5.C5_CLIENTE AND SC5.D_E_L_E_T_ = ' '
INNER JOIN SC6010 SC6 ON SC5.C5_NUM = SC6.C6_NUM AND SC6.D_E_L_E_T_ = ' '
WHERE SA1.D_E_L_E_T_ = ' '
  AND SC5.C5_EMISSAO >= FORMAT(DATEADD(YEAR, -1, GETDATE()), 'yyyyMMdd')
GROUP BY SA1.A1_COD, SA1.A1_NOME
ORDER BY TOTAL_FATURADO DESC;
```

### Análise de Crédito
```sql
SELECT 
    SA1.A1_COD, SA1.A1_NOME, SA1.A1_LC AS LIMITE,
    ISNULL(SUM(SE1.E1_SALDO), 0) AS UTILIZADO,
    SA1.A1_LC - ISNULL(SUM(SE1.E1_SALDO), 0) AS DISPONIVEL
FROM SA1010 SA1
LEFT JOIN SE1010 SE1 ON SA1.A1_COD = SE1.E1_CLIENTE 
    AND SE1.D_E_L_E_T_ = ' ' AND SE1.E1_SALDO > 0
WHERE SA1.D_E_L_E_T_ = ' '
  AND SA1.A1_LC > 0
GROUP BY SA1.A1_COD, SA1.A1_NOME, SA1.A1_LC
ORDER BY DISPONIVEL;
```

---

## Rotinas Relacionadas

| Rotina | Código | Descrição |
|:---|:---|:---|
| Cadastro de Clientes | MATA030 | Incluir/alterar clientes |
| Pedido de Venda | MATA410 | Pedidos do cliente |
| Contas a Receber | FINA040 | Títulos do cliente |
| Análise de Crédito | FINA100 | Posição de crédito |

---

## Tipos de Cliente (A1_TIPO)

| Código | Tipo | Tributação |
|:---|:---|:---|
| `F` | Consumidor Final | ICMS + IPI na base |
| `L` | Revendedor | ICMS normal |
| `R` | Produtor Rural | IE obrigatória |
| `S` | Solidário | Substituição tributária |
| `X` | Exportação | Sem ICMS/IPI |

> [!IMPORTANT]
> O tipo do cliente impacta diretamente o cálculo de impostos no faturamento. Certifique-se de que o `A1_TIPO` está correto antes de emitir notas fiscais.
