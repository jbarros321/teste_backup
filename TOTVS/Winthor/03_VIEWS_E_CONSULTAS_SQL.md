# TOTVS Winthor - Views e Consultas SQL

## Como Descobrir Views no Winthor

1. **Rotina 2500**: Aba "Criação/Alteração de Tabelas e Campos" → "Views"
2. **Dicionário Oracle**: Consultar `DBA_VIEWS` ou `ALL_VIEWS`

```sql
-- Listar todas as views do schema Winthor
SELECT VIEW_NAME FROM ALL_VIEWS WHERE OWNER = 'WINT' ORDER BY VIEW_NAME;
```

---

## Consultas SQL Práticas

### 1. Vendas dos Últimos 30 Dias

```sql
SELECT 
    n.NUMNOTA,
    n.DTSAIDA,
    c.CODCLI,
    c.CLIENTE,
    c.FANTASIA,
    n.VLTOTAL,
    u.NOME AS VENDEDOR
FROM PCNFSAID n
JOIN PCCLIENT c ON n.CODCLI = c.CODCLI
JOIN PCUSUARI u ON n.CODUSUR = u.CODUSUR
WHERE n.DTSAIDA >= TRUNC(SYSDATE) - 30
  AND n.CONDVENDA IN (1, 2, 3, 7, 9, 14)
ORDER BY n.DTSAIDA DESC;
```

### 2. Pedidos Pendentes e Bloqueados

```sql
SELECT 
    p.NUMPED,
    p.DATA,
    c.CLIENTE,
    p.VLTOTAL,
    p.POSICAO,
    DECODE(p.POSICAO, 'P','Pendente', 'B','Bloqueado', 'L','Liberado', p.POSICAO) AS STATUS
FROM PCPEDC p
JOIN PCCLIENT c ON p.CODCLI = c.CODCLI
WHERE p.POSICAO IN ('P', 'B')
ORDER BY p.DATA DESC;
```

### 3. Itens de um Pedido Específico

```sql
SELECT 
    i.NUMPED,
    i.NUMSEQ,
    pr.CODPROD,
    pr.DESCRICAO,
    i.QT,
    i.PVENDA,
    i.VLSUBTOTAL
FROM PCPEDI i
JOIN PCPRODUT pr ON i.CODPROD = pr.CODPROD
WHERE i.NUMPED = :NUMPED
ORDER BY i.NUMSEQ;
```

### 4. Contas a Receber em Aberto

```sql
SELECT 
    p.NUMNOTA,
    p.PREST,
    c.CLIENTE,
    p.DTVENC,
    p.VALOR,
    NVL(p.VLPAGO, 0) AS VLPAGO,
    p.VALOR - NVL(p.VLPAGO, 0) AS SALDO,
    TRUNC(SYSDATE) - TRUNC(p.DTVENC) AS DIAS_ATRASO
FROM PCPREST p
JOIN PCCLIENT c ON p.CODCLI = c.CODCLI
WHERE NVL(p.VPAGO, 'N') = 'N'
  AND p.VALOR - NVL(p.VLPAGO, 0) > 0
ORDER BY p.DTVENC;
```

### 5. Contas a Pagar Pendentes

```sql
SELECT 
    l.NUMTRANSACAO,
    f.FORNECEDOR,
    l.NUMNOTA,
    l.DTVENC,
    l.VALOR,
    TRUNC(SYSDATE) - TRUNC(l.DTVENC) AS DIAS_ATRASO
FROM PCLANC l
JOIN PCFORNEC f ON l.CODFORNEC = f.CODFORNEC
WHERE l.DTPAGTO IS NULL
ORDER BY l.DTVENC;
```

### 6. Posição de Estoque

```sql
SELECT 
    e.CODPROD,
    pr.DESCRICAO,
    e.CODFILIAL,
    e.QTESTGER AS ESTOQUE,
    e.QTRESERV AS RESERVADO,
    e.QTESTGER - e.QTRESERV AS DISPONIVEL,
    e.CUSTOMED
FROM PCEST e
JOIN PCPRODUT pr ON e.CODPROD = pr.CODPROD
WHERE e.CODFILIAL = :CODFILIAL
  AND e.QTESTGER > 0
ORDER BY pr.DESCRICAO;
```

### 7. Faturamento por RCA (Vendedor)

```sql
SELECT 
    u.CODUSUR,
    u.NOME AS VENDEDOR,
    COUNT(DISTINCT n.NUMNOTA) AS QTD_NOTAS,
    SUM(n.VLTOTAL) AS TOTAL_FATURADO
FROM PCNFSAID n
JOIN PCUSUARI u ON n.CODUSUR = u.CODUSUR
WHERE n.DTSAIDA BETWEEN TO_DATE('01/01/2026','DD/MM/YYYY') AND TO_DATE('31/12/2026','DD/MM/YYYY')
  AND n.CONDVENDA IN (1, 2, 3, 7, 9, 14)
GROUP BY u.CODUSUR, u.NOME
ORDER BY TOTAL_FATURADO DESC;
```

### 8. Top 10 Produtos Mais Vendidos

```sql
SELECT * FROM (
    SELECT 
        m.CODPROD,
        pr.DESCRICAO,
        SUM(m.QT) AS QTD_VENDIDA,
        SUM(m.QT * m.PUNIT) AS VALOR_TOTAL
    FROM PCMOV m
    JOIN PCPRODUT pr ON m.CODPROD = pr.CODPROD
    WHERE m.TIPOMOV = 'S'
      AND m.DTMOV >= TRUNC(SYSDATE) - 30
    GROUP BY m.CODPROD, pr.DESCRICAO
    ORDER BY QTD_VENDIDA DESC
) WHERE ROWNUM <= 10;
```

---

## Views Úteis (Exemplos para Criação)

### View de Pedidos Completa

```sql
CREATE OR REPLACE VIEW VW_PEDIDOS_COMPLETOS AS
SELECT 
    p.NUMPED,
    p.DATA,
    p.POSICAO,
    c.CODCLI,
    c.CLIENTE,
    u.NOME AS VENDEDOR,
    i.CODPROD,
    pr.DESCRICAO AS PRODUTO,
    i.QT,
    i.PVENDA,
    i.VLSUBTOTAL,
    p.VLTOTAL AS TOTAL_PEDIDO
FROM PCPEDC p
JOIN PCCLIENT c ON p.CODCLI = c.CODCLI
JOIN PCUSUARI u ON p.CODUSUR = u.CODUSUR
JOIN PCPEDI i ON p.NUMPED = i.NUMPED
JOIN PCPRODUT pr ON i.CODPROD = pr.CODPROD;
```

### View de Inadimplência

```sql
CREATE OR REPLACE VIEW VW_INADIMPLENCIA AS
SELECT 
    c.CODCLI,
    c.CLIENTE,
    c.FANTASIA,
    COUNT(*) AS QTD_TITULOS,
    SUM(p.VALOR - NVL(p.VLPAGO, 0)) AS SALDO_DEVEDOR,
    MIN(p.DTVENC) AS VENC_MAIS_ANTIGO,
    MAX(TRUNC(SYSDATE) - TRUNC(p.DTVENC)) AS MAX_DIAS_ATRASO
FROM PCPREST p
JOIN PCCLIENT c ON p.CODCLI = c.CODCLI
WHERE NVL(p.VPAGO, 'N') = 'N'
  AND p.VALOR - NVL(p.VLPAGO, 0) > 0
  AND p.DTVENC < TRUNC(SYSDATE)
GROUP BY c.CODCLI, c.CLIENTE, c.FANTASIA;
```

### View de Estoque Disponível

```sql
CREATE OR REPLACE VIEW VW_ESTOQUE_DISPONIVEL AS
SELECT 
    e.CODPROD,
    pr.DESCRICAO,
    pr.EMBALAGEM,
    pr.UNIDADE,
    e.CODFILIAL,
    e.QTESTGER AS ESTOQUE_TOTAL,
    e.QTRESERV AS RESERVADO,
    e.QTBLOQUEADA AS BLOQUEADO,
    e.QTESTGER - NVL(e.QTRESERV, 0) - NVL(e.QTBLOQUEADA, 0) AS DISPONIVEL,
    e.CUSTOMED
FROM PCEST e
JOIN PCPRODUT pr ON e.CODPROD = pr.CODPROD
WHERE e.QTESTGER > 0;
```

---

## Dicas para Consultas SQL no Oracle (Winthor)

| Dica | Descrição |
|:---|:---|
| **Datas** | Use `TRUNC(data)` para remover hora/minuto |
| **Null** | Sempre use `NVL(campo, valor_padrao)` para campos que podem ser nulos |
| **Status** | Filtre campos de status/posição para excluir cancelados |
| **Performance** | Evite `SELECT *`, liste só as colunas necessárias |
| **Formatação** | Use Oracle SQL Developer com `Ctrl+F7` para formatar |
| **Condição de Venda** | `CONDVENDA IN (1,2,3,7,9,14)` = vendas normais |

> [!CAUTION]
> Nunca execute `UPDATE` ou `DELETE` direto no banco de produção sem autorização. Utilize sempre uma base de homologação para testes.
