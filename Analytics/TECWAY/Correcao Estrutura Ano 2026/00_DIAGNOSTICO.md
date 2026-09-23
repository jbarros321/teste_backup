# Indicadores não carregam depois da tabela de intermediação por ano

**Erro:** `Unknown column 'R.ID_DET_DEMONSTRATIVO' in 'where clause'`

---

## 1. Onde exatamente estoura

O alias `R` só existe num lugar nessas queries — a subconsulta correlata que escolhe a
referência vigente de cada linha do demonstrativo:

```sql
AND DETREF.REFERENCIA = (
      SELECT MAX(R.REFERENCIA)
        FROM DET_DEMONSTRATIVO_REFERENCIA R      -- <<< o FROM do alias R
       WHERE R.ID_DET_DEMONSTRATIVO = DET.ID     -- <<< a linha que estoura
         AND R.REFERENCIA <= DATE(:VAR_DATA_REF_DRE)
   )
```

O MySQL diz `in 'where clause'`, não `in 'on clause'`. Isso aponta para **esta**
subconsulta, não para o `JOIN ... ON DET.ID = DETREF.ID_DET_DEMONSTRATIVO` que aparece
logo acima nas mesmas queries.

## 2. O que a base diz hoje

Li o tenant_47255 pela API REST em 22/09/2026:

```
DET_DEMONSTRATIVO_REFERENCIA  ->  {"ID":2, "ID_DET_DEMONSTRATIVO":11, "REFERENCIA":"2025-12-01"}
```

**A coluna `ID_DET_DEMONSTRATIVO` continua existindo em `DET_DEMONSTRATIVO_REFERENCIA`.**

Ou seja: se o `FROM` da subconsulta ainda fosse `DET_DEMONSTRATIVO_REFERENCIA`, o
`WHERE R.ID_DET_DEMONSTRATIVO` resolveria e não haveria erro.

## 3. Conclusão

O `FROM` da subconsulta foi trocado para a **nova tabela de ano**, mas o `WHERE` dela
continuou apontando para a coluna antiga:

```sql
SELECT MAX(R.REFERENCIA)
  FROM NOVA_TABELA_ANO R                      -- trocado
 WHERE R.ID_DET_DEMONSTRATIVO = DET.ID        -- NÃO trocado  <<< erro
```

É um find/replace de nome de tabela que não desceu até o corpo da subconsulta. A nova
tabela de intermediação não tem `ID_DET_DEMONSTRATIVO` (e provavelmente nem
`REFERENCIA` — deve ter `ANO`), então o `MAX(R.REFERENCIA)` tende a estourar logo em
seguida também, assim que o primeiro erro for resolvido.

> Observação: a API me mostra o schema antigo. Ou a alteração ainda não foi aplicada no
> banco que eu consigo ler, ou o REST serve uma projeção fixa. De qualquer forma o
> raciocínio acima não depende disso — ele vem do texto do erro.

## 4. O que eu preciso para fechar a correção

A API tem whitelist de tabelas: ela responde *"this token doesn't have access to table
X"* tanto para tabela inexistente quanto para tabela sem permissão. Testei os nomes
prováveis (`DET_DEMONSTRATIVO_ANO`, `DEMONSTRATIVO_ANO`, `ESTR_DEMONSTRATIVOS_ANO`,
`DET_DEMONSTRATIVO_EXERCICIO`, `ANO_DEMONSTRATIVO`, `DET_ANO`…) e todos devolvem a
mesma mensagem — **não dá para descobrir o nome nem as colunas por ali.**

Rode `01_descobre_estrutura_nova.sql` na plataforma e me mande a saída, ou me diga:

1. O **nome** da nova tabela.
2. As **colunas** dela (esperado algo como `ID`, `ID_DET_DEMONSTRATIVO`, `ANO`).
3. Como `DET_DEMONSTRATIVO_CTACTB` se liga agora — continua em
   `ID_DET_DEMONSTRATIVO_REFERENCIA`, ou passou a apontar para a nova tabela?

Com isso o `02_patch_queries.py` reescreve os arquivos de uma vez.

## 5. Isso não é um erro só — são 20 arquivos

O mesmo encadeamento aparece em toda a pasta TECWAY. Por isso "os indicadores não
carregam" é só o primeiro sintoma: DRE, DFC, DMPL e os dois BP quebram igual.

### Padrão B — subconsulta correlata (dá o erro `in 'where clause'` que você viu)

| Arquivo | Ocorrências |
|---|---:|
| `.claude/dados.sql` | 2 |
| `ajustes/02_queryDRE.sql` | 1 |
| `ajustes/01_ajustes_dados.sql` | 1 |
| `ajustes/10_ajustes_dmpl.sql` | 2 |
| `Correcao Fornecedores 2026/04_query_bp_interno_corrigida.sql` | 2 |

### Padrão A — join direto (vai dar `in 'on clause'`)

| Arquivo | Ocorrências |
|---|---:|
| `.claude/dados.sql` | 3 |
| `querybp.sql` | 1 |
| `BP Interno/queryDados.sql` | 1 |
| `BP Interno/queryDados_oneline.sql` | 1 |
| `BP Interno/DFC EXTERNO/query.sql` | 1 |
| `DFC Externo/query.sql` | 3 |
| `Demost EXTER/queryDFC.sql` | 1 |
| `ajustes/01_ajustes_dados.sql` | 1 |
| `ajustes/03_queryIndicadores.sql` | 2 |
| `ajustes/04_queryDFC.sql` | 3 |
| `ajustes/06_queryDMPL_SEM_ALTERACAO.sql` | 1 |
| `ajustes/09_queryDMPL.sql` | 2 |
| `ajustes/10_ajustes_dmpl.sql` | 3 |
| `ajustes/11_query2.sql` | 3 |
| `ajustes/query1.sql` | 1 |
| `ajustes/query2.sql` | 1 |
| `datas/queryDFC.sql` | 1 |
| `datas/queryDMPL.sql` | 1 |
| `datas/queryIndicadores.sql` | 1 |
| `Correcao Fornecedores 2026/04_query_bp_interno_corrigida.sql` | 4 |

Total: **36 pontos de junção** em 20 arquivos.

## 6. A forma final do encadeamento

Ver `03_template_join_corrigido.sql`. Em resumo, de:

```
DET_DEMONSTRATIVO -> DET_DEMONSTRATIVO_REFERENCIA -> DET_DEMONSTRATIVO_CTACTB
```

para:

```
DET_DEMONSTRATIVO -> NOVA_TABELA_ANO -> DET_DEMONSTRATIVO_REFERENCIA -> DET_DEMONSTRATIVO_CTACTB
```

e a subconsulta de referência vigente passa a filtrar o ano na tabela nova e a data na
tabela de referência.
