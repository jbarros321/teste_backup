# Resolvido — o que fazer

## Um passo

**Substituir a query do componente pelo conteúdo de `10_queryDados_FINAL.sql`.**

É o `queri1.sql` corrigido. Não precisa mexer no filtro de ano, nem preencher
`:VAR_ANO_FILTRO`, nem cadastrar 2026. A query passa a funcionar com o que existe hoje.

Para o BP Interno, trocar os dois `EST.ID = 1` por `EST.ID = 2`.

## O que estava errado, e o que a query nova faz

| Problema | Antes | Agora |
|---|---|---|
| `:VAR_ANO_FILTRO` vazio | `DA.ANO = NULL` → zero linhas → *"Ano inválido"* | ano sai do filtro de ano; se vazio, do mês (`07/2026` → 2026); se os dois vazios, ano corrente |
| Ano 2026 do BP Externo tem **24 referências e ZERO vínculos** | escolhia esse ano vazio → BP Externo **inteiro zerado** | escolhe o ano mais recente **que tenha vínculo** → cai em 2025 e mostra número |
| Fornecedores tem **12 referências** em 2026 (uma por mês), cada uma com cópia completa dos vínculos | sem `MAX(REFERENCIA)`, valor × 12 | uma referência só |
| Grupo sem conta casada | `0 * NULL` = `NULL` → célula em branco | `0` |
| Filtros de mês e ano discordando | corte contradizia o ano | mês de outro ano é ignorado, vale o ano inteiro |

## Conferido contra os dados reais

Simulei a query sobre o tenant_60677. Fornecedores do BP Interno, CODEMP 999:

| varAno | varMes | corte | resultado |
|---|---|---|---:|
| *(vazio)* | `07/2026` | 31/07/2026 | **1.707.138,17** |
| `2026` | `Todos` | 31/12/2026 | 1.707.138,17 |
| *(vazio)* | *(vazio)* | 31/12/2026 | 1.707.138,17 |
| `2026` | `07/2025` | 31/12/2026 | 1.707.138,17 |
| `2026` | `03/2026` | 31/03/2026 | 1.434.717,57 |
| `2025` | `Todos` | 31/12/2025 | 1.487.994,59 |

**1.707.138,17 é exatamente o número do Sankhya** que abriu esta investigação. Fecha ao
centavo, e o balancete agora tem todas as competências de 2026 (01 a 12).

## Duas coisas para o cadastro, quando der

Não travam nada, mas os números ficam melhores:

1. **Vincular as contas no ano 2026 do BP Externo.** Hoje ele cai em 2025 e mostra
   Fornecedores = **1.524.499,90** contra **1.707.138,17** do Interno. A diferença de
   **182.638,27** são 940 contas que só existem no cadastro de 2026 (17 delas com
   saldo). É o mesmo problema de conta nova sem vínculo do
   `../Correcao Fornecedores 2026/`.

2. **Cadastrar 2026 nas outras linhas.** O BP Interno tem 2026 só em Fornecedores (1 de
   38); DRE, DFC e DMPL não têm 2026 nenhum. Com a query nova eles usam 2025 e
   funcionam — mas é o cadastro de 2025 valendo para o exercício de 2026.

## Sobre as 12 referências mensais

A linha Fornecedores do BP Interno tem 12 referências em 2026 e 12 em 2027, uma por mês,
contra 1 referência nos outros 355 pares linha×ano. São 25.956 vínculos onde deveriam
ser 2.163 — a mesma lista repetida 12 vezes. A query nova ignora isso, mas vale limpar:
qualquer query que passe por `DET_DEMONSTRATIVO_REFERENCIA` sem escolher uma referência
multiplica Fornecedores por 12.
