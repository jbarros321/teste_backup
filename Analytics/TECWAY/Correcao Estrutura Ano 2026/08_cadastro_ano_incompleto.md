# O que realmente derruba os indicadores: cadastro de ano incompleto em 2026

Cobertura real de `DET_DEMONSTRATIVO_ANO` no tenant_60677 (23/09/2026) — quantas linhas
de cada demonstrativo têm registro de ano:

| EST | Demonstrativo | Linhas | 2023 | 2024 | 2025 | **2026** | 2027 |
|---|---|---:|---:|---:|---:|---:|---:|
| 1 | Balanço Patrimonial Externo | 24 | — | 24 | 24 | **24** | — |
| 2 | Balanço Patrimonial Interno | 38 | — | 38 | 38 | **1** | 1 |
| 3 | DRE Externo | 10 | — | 10 | 10 | **0** | — |
| 4 | DRE Interno | 47 | — | 47 | 47 | **0** | — |
| 10 | DFC Externo | 30 | 18 | 30 | 30 | **0** | — |
| 11 | DMPL Externo | 5 | 5 | 5 | 5 | **0** | — |

**Só o BP Externo tem 2026 completo.** É por isso que ele é a única tela que anda.

## O efeito do `DA.ANO = :VAR_ANO_FILTRO`

O `queri1.sql` filtra o ano por igualdade:

```sql
WHERE EST.ID = 1 AND DA.ANO = :VAR_ANO_FILTRO AND R.REFERENCIA = (...)
```

Com `:VAR_ANO_FILTRO = 2026`, replicando esse filtro nas outras telas:

- **BP Interno** devolve **1 linha de 38**. A única com 2026 cadastrado é justamente
  `2.1.1 Fornecedores` — as outras 37 somem. A tela não dá erro: ela abre quase vazia.
- **DRE Externo, DRE Interno, DFC e DMPL** devolvem **zero linhas**. Tela em branco.

Isso é independente do erro de join e do `varAno`. Mesmo com os dois corrigidos, essas
telas continuam vazias em 2026 enquanto o cadastro não existir.

## Duas saídas

### A) Cadastrar 2026 nas 5 estruturas (o certo)

Replicar o cadastro de 2025 para 2026 em `DET_DEMONSTRATIVO_ANO` e nos vínculos. É o
que fecha de verdade — as telas passam a ter cadastro próprio do exercício.

Nesse caso vale conferir por que o BP Interno tem exatamente 1 linha em 2026 e 1 em
2027: parece uma migração que rodou só para a linha de Fornecedores e parou. As 12
referências mensais em cada um desses dois anos (contra 1 referência nos demais
355 pares linha×ano) reforçam que esse registro foi criado por outro caminho.

### B) Trocar a igualdade por `MAX(ANO) <= ano de corte` (a rede de proteção)

```sql
AND DA.ANO = (
      SELECT MAX(A2.ANO)
        FROM DET_DEMONSTRATIVO_ANO A2
       WHERE A2.ID_DET_DEMONSTRATIVO = DET.ID
         AND A2.ANO <= :VAR_ANO_FILTRO
    )
```

Com isso, quem não tem 2026 continua usando o cadastro de 2025 em vez de sumir. É o que
está em `02_template_join_corrigido.sql` e em `06_query_bp_interno_schema_novo.sql`.

**As duas não são alternativas — faça as duas.** (A) resolve o exercício atual; (B)
impede que a mesma coisa aconteça toda virada de ano, inclusive em 2027, que já está
cadastrado só para aquela mesma linha solta do BP Interno.
