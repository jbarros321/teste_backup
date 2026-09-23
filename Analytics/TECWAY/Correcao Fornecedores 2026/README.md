# Correção — Fornecedores do BP Interno (2026)

Divergência reportada em 22/09/2026: BP Interno mostrava **1.434.848,92** em
Fornecedores contra **1.707.138,17** no balancete do Sankhya em 21/09.

**Causa:** `IMP_BASE_BALANCETE` está com a carga parada em **março/2026**. A tela
devolvia o saldo de 31/03 rotulado como setembro, sem nenhum aviso. Os 272.289,25
são o movimento de abril a setembro que nunca foi importado. Bate ao centavo —
ver `00_DIAGNOSTICO.md`.

| Arquivo | O que é |
|---|---|
| `00_DIAGNOSTICO.md` | investigação completa, evidências e ordem de execução |
| `01_verifica_defasagem_balancete.sql` | checa até quando a carga foi feita (rodar antes e depois) |
| `02_conferencia_fornecedores_sankhya.sql` | conferência conta a conta contra o Sankhya |
| `03_vinculos_fornecedores_faltantes.sql` | vincula as contas novas de fornecedor (achado secundário) |
| `04_query_bp_interno_corrigida.sql` | query do BP Interno com aviso de defasagem embutido |
| `05_patch_tela_aviso_defasagem.js` | faixa de aviso em `BP Interno/bp.html` |
| `reproduz_diagnostico.py` | reimprime as evidências lendo o tenant pela API REST |
| `evidencias/` | CSVs gerados na investigação |

## Ordem

1. Religar a carga do balancete (04/2026 a 09/2026) — **é isto que resolve a divergência**.
2. `01` para confirmar que a defasagem zerou.
3. `02` para conferir contra o Sankhya na mesma competência fechada.
4. `03` (parte A) para o cadastro parar de perder fornecedor novo.
5. `04` + `05` para a tela nunca mais mostrar saldo velho sem avisar.

> Nenhum destes scripts foi executado. Os SQL de escrita (`03`) abrem
> `START TRANSACTION` e listam as contagens esperadas — confira antes do `COMMIT`.
