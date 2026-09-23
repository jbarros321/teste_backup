# Correção — estrutura por ano (`DET_DEMONSTRATIVO_ANO`), tenant_60677

## Comece aqui

**`11_RESOLVIDO.md`** — o que fazer, em um passo.
**`10_queryDados_FINAL.sql`** — a query pronta, para colar no componente.

Conferido contra os dados reais: Fornecedores do BP Interno dá **1.707.138,17**, que é
exatamente o número do Sankhya que abriu a investigação.

---

## Os demais arquivos

| Arquivo | O que é |
|---|---|
| `00_DIAGNOSTICO.md` | o quadro completo e as evidências |
| `07_config_componente.md` | o erro `As configurações do componente...` |
| `08_cadastro_ano_incompleto.md` | cobertura de ano por estrutura |
| `09_ano_vazio.md` | por que `:VAR_ANO_FILTRO` vazio zera a tela |
| `01_descobre_estrutura_nova.sql` | consultas de schema (já rodadas) |
| `02_template_join_corrigido.sql` | o encadeamento novo e as armadilhas |
| `03_patch_queries.py` | reescreve as 38 junções nos 24 arquivos do repositório |
| `04_patch_tela_varAno.js` | trava da tela tolerante a grafia de parâmetro |
| `05_migra_exclusoes.sql` | as 35 exclusões, só se migrar para o caminho `_ANO_CTACTB` |
| `06_query_bp_interno_schema_novo.sql` | variante usando as tabelas `_ANO_CTACTB` |

> Nada foi executado no tenant — só leitura. O `03_patch_queries.py` rodou só em dry-run.
