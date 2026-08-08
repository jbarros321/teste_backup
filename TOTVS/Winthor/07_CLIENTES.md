# TOTVS Winthor - Gestão de Clientes

## Tabela Principal: PCCLIENT

### Cadastro (Rotina 302)

A rotina **302** é a principal para cadastro e manutenção de clientes no Winthor.

### Campos Essenciais

| Campo | Descrição | Obrigatório |
|:---|:---|:---|
| `CODCLI` | Código do cliente (auto-incremento) | Auto |
| `CLIENTE` | Razão social | ✅ |
| `FANTASIA` | Nome fantasia | ✅ |
| `CGCENT` | CNPJ ou CPF | ✅ |
| `IEENT` | Inscrição estadual | Depende |
| `ENDERENT` | Endereço de entrega | ✅ |
| `MUNICENT` | Município | ✅ |
| `ESTENT` | UF | ✅ |
| `CEPENT` | CEP | ✅ |
| `TELENT` | Telefone | Não |
| `EMAIL` | E-mail | Não |
| `CODCOB` | Tipo de cobrança padrão | ✅ |
| `CODPLPAG` | Plano de pagamento padrão | ✅ |
| `CODPRACA` | Praça | ✅ |
| `CODATIV` | Ramo de atividade | ✅ |
| `CODUSUR` | RCA vinculado | ✅ |

### Campos Financeiros

| Campo | Descrição |
|:---|:---|
| `LIMCRED` | Limite de crédito |
| `SALDODEV` | Saldo devedor atual |
| `BLOQUEIO` | Bloqueado (S/N) |
| `BLOQFINANC` | Bloqueio financeiro |
| `DTULTCOMP` | Data da última compra |
| `VLULTCOMP` | Valor da última compra |
| `CODREDE` | Código da rede |

### Classificações

| Campo | Descrição |
|:---|:---|
| `CODPRACA` | Praça / Região |
| `CODATIV` | Ramo de atividade |
| `CODREDE` | Rede / Grupo econômico |
| `CODCLASSE` | Classificação (A, B, C) |

---

## Consultas SQL para Clientes

### Listar Clientes com Dados Completos
```sql
SELECT 
    c.CODCLI, c.CLIENTE, c.FANTASIA, c.CGCENT,
    c.ENDERENT, c.MUNICENT, c.ESTENT, c.CEPENT,
    c.TELENT, c.EMAIL,
    c.LIMCRED, c.BLOQUEIO,
    u.NOME AS VENDEDOR
FROM PCCLIENT c
LEFT JOIN PCUSUARI u ON c.CODUSUR = u.CODUSUR
ORDER BY c.CLIENTE;
```

### Clientes Bloqueados
```sql
SELECT CODCLI, CLIENTE, FANTASIA, BLOQUEIO,
    LIMCRED, SALDODEV
FROM PCCLIENT
WHERE BLOQUEIO = 'S' OR BLOQFINANC = 'S'
ORDER BY CLIENTE;
```

### Clientes Inativos (sem compra há X dias)
```sql
SELECT CODCLI, CLIENTE, FANTASIA, DTULTCOMP,
    TRUNC(SYSDATE) - TRUNC(DTULTCOMP) AS DIAS_SEM_COMPRA
FROM PCCLIENT
WHERE DTULTCOMP < TRUNC(SYSDATE) - 90
  AND BLOQUEIO = 'N'
ORDER BY DTULTCOMP;
```

### Ranking de Clientes por Faturamento
```sql
SELECT 
    c.CODCLI, c.CLIENTE,
    COUNT(DISTINCT n.NUMNOTA) AS QTD_NFS,
    SUM(n.VLTOTAL) AS TOTAL_COMPRAS,
    MAX(n.DTSAIDA) AS ULTIMA_COMPRA
FROM PCCLIENT c
JOIN PCNFSAID n ON c.CODCLI = n.CODCLI
WHERE n.DTSAIDA >= ADD_MONTHS(TRUNC(SYSDATE), -12)
  AND n.CONDVENDA IN (1,2,3,7,9,14)
GROUP BY c.CODCLI, c.CLIENTE
ORDER BY TOTAL_COMPRAS DESC;
```

### Análise de Crédito
```sql
SELECT 
    c.CODCLI, c.CLIENTE,
    c.LIMCRED,
    NVL(SUM(CASE WHEN NVL(p.VPAGO,'N')='N' THEN p.VALOR - NVL(p.VLPAGO,0) ELSE 0 END), 0) AS SALDO_DEVEDOR,
    c.LIMCRED - NVL(SUM(CASE WHEN NVL(p.VPAGO,'N')='N' THEN p.VALOR - NVL(p.VLPAGO,0) ELSE 0 END), 0) AS CREDITO_DISPONIVEL
FROM PCCLIENT c
LEFT JOIN PCPREST p ON c.CODCLI = p.CODCLI
WHERE c.BLOQUEIO = 'N'
GROUP BY c.CODCLI, c.CLIENTE, c.LIMCRED
HAVING c.LIMCRED > 0
ORDER BY CREDITO_DISPONIVEL;
```

---

## Rotinas Relacionadas

| Rotina | Descrição |
|:---|:---|
| **302** | Cadastro de clientes |
| **303** | Relatório de clientes |
| **501** | Contas a receber por cliente |
| **336** | Pedidos do cliente |
| **522** | Histórico financeiro |

> [!TIP]
> O campo `CODREDE` permite agrupar filiais de um mesmo cliente/rede para análise consolidada de faturamento e crédito.
