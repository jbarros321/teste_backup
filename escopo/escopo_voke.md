# Escopo do Projeto — Orçamentário Voke (Folha/RH + Compras)

| | |
|---|---|
| **Cliente / Produto** | Voke |
| **Documento-fonte** | `Estimativa-Horas-Orcamentario-Folha-RH (1).doc` |
| **Data base** | 17/07/2026 |
| **Fonte única de dados** | **Sankhya** |
| **Esforço total** | **134h — fechado** |
| **Duração** | 10 semanas |

O projeto tem **duas pontas de orçamento** e uma **camada comum de entrada de dados**, todas sobre a mesma base — o **Sankhya** — e o mesmo motor de Orçado x Realizado x Forecast:

1. **Folha / RH** — orçamento de pessoal.
2. **Compras** — orçamento por Centro de Resultado e Natureza, com o de-para CR ↔ Gerência.
3. **Motor de Orçamento** — carga via planilha e drivers de orçamento, transversal às duas pontas.

## Resumo Executivo — 134h

| Fase | Entrega | Horas | % |
|---|---|---:|---:|
| Fase 0 | Carga inicial, modelagem e infraestrutura | 12h | 9% |
| Fase 1 | Orçamento core — Dashboard + DRE de Folha | 16h | 12% |
| Fase 2 | Planejamento — Vagas, Movimentações, Colaboradores | 12h | 9% |
| Fase 3 | Jornada — Banco de horas, HE, faltas | 10h | 7% |
| Fase 4 | Integrações (mínimo contratual) | 35h | 26% |
| Fase 5 | Agente IA + Testes + Deploy | 9h | 7% |
| Fase 6 | **Compras** — CR, de-para Gerência, cenários | 28h | 21% |
| Fase 7 | **Motor de Orçamento** — upload de planilha + drivers | 12h | 9% |
| | **TOTAL** | **134h** | **100%** |

---

# PARTE I — ORÇAMENTÁRIO DE FOLHA/RH

## 1. Problema

O orçamento de pessoal hoje vive em duas planilhas Excel — `Planejamento_Banco_de_Horas` e `Considerações_Folha` (18 abas no total). Não há versão única da verdade, o Realizado entra manualmente, o Forecast é recalculado à mão e não existe drill por estrutura organizacional.

## 2. Solução

Um sistema único de orçamento de pessoal/folha com **Orçado x Realizado x Desvio x Forecast**, planejamento de headcount e visão de RH. A alimentação acontece em dois tempos: primeiro pela carga das planilhas (o sistema já é útil na Fase 1), depois pelas integrações com o Sankhya, que substituem a carga manual.

### Objetivos
1. Acompanhar Orçado x Realizado x Desvio x Forecast por **Diretoria → Gerência → Centro de Custo → Natureza**.
2. Planejar vagas (headcount e ramp de salários mês a mês).
3. Provisionar movimentações (promoções/progressões) e turnover (custos rescisórios).
4. Monitorar jornada (banco de horas, HE, faltas, atestados) e seu impacto em folha.
5. Servir de base para treinamentos (T&D) como dimensão de orçamento.

## 3. Escopo funcional — Folha/RH

| # | Módulo | Entrega |
|---|---|---|
| 1 | **Dashboard Orçamento (home)** | 4 KPIs (Orçado, Realizado, Desvio %, Forecast do ano), gráfico Orçado x Real x Forecast mensal, top desvios por natureza, quebra por Diretoria, cross-filter bidirecional e drill-down |
| 2 | **Folha / DRE** | Planilha inteligente (Handsontable + HyperFormula) com Orçado x Real x Desvio por rubrica e mês — espelho da aba Resumo, com fórmulas e persistência |
| 3 | **Planejamento de Vagas** | Grid editável: headcount por área, ramp salarial mês a mês, custo acumulado |
| 4 | **Movimentações & Turnover** | Provisões de promoções (performance, base salarial, encargos 70%) e custos rescisórios por diretoria/mês |
| 5 | **Jornada (Banco de Horas)** | Banco de horas, HE, faltas e atestados por colaborador e área, com impacto financeiro estimado |
| 6 | **Colaboradores** | CRUD com hierarquia de liderança (Diretoria / Gerência / LMA) e filtros |
| 7 | **Importação** | Upload de XLSX/CSV com preview e validação |
| 8 | **Agente IA de negócio** | Consultas em linguagem natural sobre os dados (ex.: "qual o desvio de FGTS acumulado?") |

## 4. Detalhamento de horas — Folha/RH (59h)

### Fase 0 — Carga inicial, modelagem e infraestrutura (12h)

| Task | Horas | Detalhes |
|---|---:|---|
| Modelagem do banco (DDL normalizado) | 4h | Dimensões compartilhadas (empresa, CR, gerência, natureza, projeto) + tabelas fato/planejamento de Folha e Compras, FKs, índices |
| Script de carga das planilhas (XLSX/CSV) | 4h | Parser das 2 planilhas (18 abas), normalização, tratamento de horas (hh:mm→min), datas |
| Server Functions base (CRUD + queries) | 2h | SFs SQL por entidade, paginação, filtros |
| Testes de carga e validação de dados | 2h | Conferência de totais, integridade referencial |

### Fase 1 — Orçamento core (16h)

| Task | Horas | Detalhes |
|---|---:|---|
| Dashboard principal (home) | 6h | 4 KPIs, gráfico Orçado x Real x Forecast mensal, top desvios por natureza, quebra por Diretoria |
| Cross-filter + Drill-down | 4h | Drill Diretoria → Gerência → Centro de Custo → Natureza, cross-filter bidirecional |
| Tela Folha / DRE | 4h | Handsontable com Orç x Real x Desvio por rubrica e mês, fórmulas HyperFormula |
| SFs de drill e universais | 2h | SFs por dimensão, SFs Universal para cross-filter |

### Fase 2 — Planejamento (12h)

| Task | Horas | Detalhes |
|---|---:|---|
| Planejamento de Vagas | 4h | Grid editável: headcount por área, ramp salarial mês a mês, custo acumulado |
| Movimentações & Turnover | 4h | Provisões de promoções (encargos 70%) e custos rescisórios por diretoria/mês |
| Colaboradores (cadastro) | 2h | CRUD com hierarquia de liderança, filtros |
| Importação manual | 2h | Upload de XLSX/CSV com preview e validação |

### Fase 3 — Jornada (10h)

| Task | Horas | Detalhes |
|---|---:|---|
| Banco de Horas | 3h | Visão por colaborador e por área, total positivo/negativo |
| Horas Extras | 3h | Ranking, totais por diretoria/LMA, impacto financeiro estimado |
| Faltas e Atestados | 2h | Absenteísmo por área, tendência, alertas |
| Impacto financeiro consolidado | 2h | Custo de HE em folha, projeção de banco de horas |

### Fase 5 — Agente IA + Testes + Deploy (9h)

| Task | Horas | Detalhes |
|---|---:|---|
| Configuração do agente de negócio | 3h | Perfis, tabelas liberadas, SFs permitidas, Business Instructions |
| Business Instructions e curadoria de respostas | 1h | Glossário de negócio (natureza, CR, gerência, cenário), regras de leitura do de-para, bateria de perguntas-padrão validadas |
| Testes funcionais | 3h | Validação das telas, cross-filter, drill, carga, permissões |
| Deploy e ajustes finais | 2h | Build, revisão visual, ajustes de UX |

O acréscimo de 1h em cadastro e 1h em curadoria existe para o agente não responder com confiança um número errado: ele precisa saber ler o de-para CR ↔ Gerência com vigência e distinguir cenário (orçado original x revisado x forecast) antes de somar.

## 5. Integrações — Fase 4 (35h, mínimo contratual)

O **Sankhya é a fonte única do Realizado e dos cadastros** — Folha e Compras leem da mesma base. As demais integrações cobrem domínios que o Sankhya não atende.

| Fonte | Domínio | Horas | Auth | Staging |
|---|---|---:|---|---|
| **Sankhya** | Realizado financeiro/contábil, compras (pedidos e contratos) e cadastros (empresa, CR, natureza, projeto, parceiro) — atende Folha **e** Compras | 12h | Sankhya Gateway — login de serviço + token de sessão | `IMP_FIN_REALIZADO`, `IMP_COMPRAS_PEDIDOS` |
| **Ponto Tel** | Jornada: marcações, saldos, HE, faltas | 8h | A confirmar | `IMP_PONTO_BANCO`, `IMP_PONTO_HE`, `IMP_PONTO_FALTAS` |
| **Gupy** | Vagas e candidatos | 4h | STATIC_KEY | `IMP_VAGAS` → `PLAN_VAGAS` |
| **Mindsight** | Desempenho: ciclos e notas | 4h | Token | `IMP_AVALIACOES` → `PLAN_MOVIMENTACOES` |
| **Moodle** | Treinamentos (T&D) | 4h | Token de webservice | `IMP_TREINAMENTOS` |
| **Power BI** | Datasets e queries DAX | 3h | Azure AD / OAuth2 | `IMP_BI_METRICAS` |
| | **Total** | **35h** | | |

---

# PARTE II — ORÇAMENTÁRIO DE COMPRAS (28h)

## 6. Módulo Compras — Orçamento por Centro de Resultado

### 6.1 Contexto e o problema do Centro de Resultado

O orçamento de Compras é feito **por Centro de Resultado (CR)**. O CR é a chave operacional do negócio: **pedidos e contratos já estão amarrados ao número do CR**, e mudar esse número significaria reparametrizar tudo — pedidos, contratos, integrações, histórico.

O problema é que a **estrutura organizacional se move com frequência**. O time de Compras, por exemplo, sai de uma diretoria e passa a responder a outra. O número do CR continua o mesmo, mas ele deixou de pertencer àquela diretoria — e o acompanhamento por CR puro quebra.

> **Regra do cliente:** *"pensando em sistema é o número do Centro de Resultado mesmo"* — o CR é o identificador imutável. O redirecionamento organizacional é feito por fora, por uma camada de **Gerência**.

### 6.2 A solução: camada de Gerência como de-para

Foi criada internamente uma **nomenclatura de Gerência** que funciona como camada de redirecionamento entre o CR e a estrutura organizacional:

```
Pedido / Contrato  →  CR (número imutável)  →  [DE-PARA]  →  Gerência  →  Diretoria
```

Quando uma área muda de estrutura, **não se mexe no CR**: altera-se apenas o de-para `CR → Gerência`. Nenhum pedido, contrato ou parâmetro precisa ser tocado.

| Requisito | Implicação técnica |
|---|---|
| CR é a chave de origem, imutável | Todo dado transacional grava `cr_id`; a hierarquia nunca é gravada no fato |
| A hierarquia é resolvida por de-para | `MAP_CR_GERENCIA` consultada em tempo de query — não desnormalizada no fato |
| A estrutura muda ao longo do tempo | O de-para tem **vigência**: o mesmo CR pode ser Gerência A em jan–jun e Gerência B em jul–dez |
| Comparar histórico sem distorcer | Duas visões: **estrutura vigente** (reprocessa o histórico pela estrutura de hoje) e **estrutura da época** |
| Um CR pode ser rateado | O de-para suporta percentual de rateio quando um CR se divide entre gerências |
| Hoje isso vive em planilha | A tela de manutenção do de-para substitui a planilha de redirecionamento atual |

### 6.3 Cenários de orçamento

| Cenário | Uso |
|---|---|
| **Orçado original (Budget)** | Versão aprovada no ciclo, congelada — base de comparação do ano |
| **Revisões (Revisado 1, 2, …)** | Reorçamentos ao longo do ano, com data, autor e justificativa |
| **Forecast** | Realizado acumulado + orçado do restante do ano |
| **Realizado** | Vem do **Sankhya**, consolidado por CR + natureza + mês |

Todas as telas aceitam comparação **cenário x cenário** (ex.: Revisado 2 x Orçado original).

### 6.4 Base única: Sankhya

**Folha e Compras não têm bases separadas.** Todos os dados de origem vêm do **Sankhya**, então o sistema segue a base do Sankhya como referência — sem cadastro paralelo e sem de-para de cadastro.

| O que vem do Sankhya | Uso no orçamento |
|---|---|
| **Empresas** | Dimensão Empresa (driver por empresa, consolidação) |
| **Centros de Resultado** | Chave imutável do orçamento de Compras |
| **Naturezas / plano de contas** | Dimensão Natureza — **a mesma para Folha e Compras**, o que torna o consolidado nativo |
| **Projetos** | Dimensão Projeto |
| **Parceiros (fornecedores)** | Detalhe do realizado |
| **Movimentos financeiros / contábeis** | Realizado de Folha e de Compras |
| **Pedidos e contratos de compra** | Base do realizado de Compras |

Consequências práticas:

- O sistema **espelha** os cadastros do Sankhya (carga incremental); não os edita. Cadastro novo nasce no Sankhya.
- A única estrutura mantida **fora** do Sankhya é o **de-para CR ↔ Gerência**, por ser criação interna.
- O consolidado Folha + Compras sai direto, porque as duas pontas usam a mesma natureza e o mesmo CR.

### 6.5 Escopo funcional — Compras

| # | Tela / Módulo | O que entrega |
|---|---|---|
| 1 | **Dashboard Compras** | KPIs Orçado x Realizado x Desvio % x Forecast; gráfico mensal; top desvios por natureza; quebra por Gerência e por CR. Drill **Empresa → Diretoria → Gerência → CR → Projeto → Natureza** e cross-filter |
| 2 | **Orçamento por CR** | Planilha editável CR x Natureza x Mês, totalizadores por gerência/diretoria, fórmulas e persistência por cenário |
| 3 | **De-Para CR ↔ Gerência** | Manutenção do redirecionamento com vigência, histórico, rateio percentual e validações (CR órfão, rateio ≠ 100%, vigências sobrepostas) |
| 4 | **Cenários & Versionamento** | Criar/congelar versão, comparar cenários lado a lado, log de alterações |
| 5 | **CR por Responsável** | Carteira do responsável: seus CRs, orçado, desvios e pendências |
| 6 | **Pedidos & Contratos x Saldo** | Consumo do orçamento por pedido/contrato: realizado x saldo disponível por CR/natureza/mês (versão simplificada — sem empenho/comprometido, ver item 12) |
| 7 | **Consolidado Voke** | Visão única Folha/RH + Compras por natureza e estrutura, usando o mesmo de-para |

### 6.6 Modelagem

```
DIM_EMPRESA            (empresa_id, cnpj, razao_social, ativo)              ← espelho Sankhya
DIM_CR                 (cr_id, numero_cr, descricao, status, ativo)         ← espelho Sankhya
DIM_PROJETO            (projeto_id, codigo, nome, empresa_id, status)       ← espelho Sankhya
DIM_NATUREZA           (natureza_id, codigo, descricao)                     ← espelho Sankhya
DIM_GERENCIA           (gerencia_id, nome, diretoria_id)
DIM_DIRETORIA          (diretoria_id, nome)
DIM_RESPONSAVEL        (responsavel_id, nome, email, perfil)
DIM_CENARIO            (cenario_id, tipo, ano, versao, status, data_congelamento)

MAP_CR_GERENCIA        (cr_id, gerencia_id, data_inicio, data_fim, perc_rateio)  ← de-para (só no sistema)
MAP_CR_RESPONSAVEL     (cr_id, responsavel_id, data_inicio, data_fim)

FATO_ORC_COMPRAS       (cenario_id, empresa_id, cr_id, projeto_id, natureza_id, periodo, valor, origem)
FATO_REAL_COMPRAS      (empresa_id, cr_id, projeto_id, natureza_id, periodo, valor)   ← Sankhya
```

**Regra central:** nenhum fato guarda gerência ou diretoria. A hierarquia é resolvida em tempo de query via `MAP_CR_GERENCIA`, respeitando a vigência do período consultado.

### 6.7 Detalhamento de horas — Fase 6 (28h)

| Task | Horas | Detalhes |
|---|---:|---|
| Modelagem Compras + de-para com vigência | 3h | Fatos, `MAP_CR_GERENCIA` com vigência e rateio, índices |
| Tela De-Para CR ↔ Gerência | 3h | CRUD com vigência, histórico, rateio, validações |
| Motor de resolução de hierarquia | 2h | SFs que resolvem CR → Gerência → Diretoria por período (estrutura vigente / da época) |
| Motor de cenários e versionamento | 4h | Criar, congelar e comparar versões; forecast; log de alterações |
| Planilha de Orçamento por CR | 5h | Handsontable CR x Natureza x Mês, fórmulas, totalizadores, persistência por cenário |
| Dashboard Compras | 5h | KPIs, série mensal, top desvios, quebra por gerência/CR, drill + cross-filter |
| CR por Responsável | 2h | Carteira do responsável, filtros, pendências |
| Pedidos & Contratos x saldo (simplificado) | 2h | Consumo do orçamento por pedido/contrato: realizado x saldo disponível por CR/natureza/mês, a partir dos pedidos do Sankhya |
| Carga das planilhas de Compras + de-para | 2h | Parser, normalização de CR contra a base do Sankhya, validação de cobertura do de-para |
| **Total Fase 6** | **28h** | |

> A integração **Sankhya** já está na Fase 4 (12h) e alimenta o Realizado e os pedidos de Compras — não há hora adicional de conexão nesta fase.

---

# PARTE III — MOTOR DE ORÇAMENTO (12h)

## 7. Carga via planilha e Drivers

Camada **transversal**: vale para Folha/RH e para Compras.

### 7.1 As três formas de orçar

| Forma | Como funciona | Quando se usa |
|---|---|---|
| **Digitação na tela** | Planilha inteligente dentro do sistema, célula a célula, por cenário | Ajuste fino, revisões, forecast |
| **Upload de planilha** | Sobe o XLSX/CSV do orçamento pronto e o sistema carrega direto no cenário | Ciclo orçamentário, coleta com as áreas, quem já trabalha em Excel |
| **Driver (regra)** | O sistema **gera** os valores a partir de uma regra parametrizada | Naturezas dirigidas por meta ou premissa |

As três convivem: o driver gera a base, o upload sobrepõe, a digitação ajusta. Cada célula guarda sua **origem** (digitado / planilha / driver), então dá para reprocessar só o que veio de regra — e **travar** o que foi digitado contra sobrescrita.

### 7.2 Upload de orçamento via planilha

- **Template oficial** para download, com as dimensões corretas (empresa, CR, projeto, natureza, meses).
- **Upload livre** com mapeamento de colunas, salvo como perfil reutilizável.
- **Preview antes de gravar**: linhas válidas, linhas com erro e o delta contra o cenário atual.
- **Validações**: CR inexistente/inativo, CR sem de-para vigente no período, natureza desconhecida, empresa/projeto inválidos, duplicidade, total que não bate.
- **Carga sempre dentro de um cenário**, nunca sobre o orçamento congelado. Modos: substituir, complementar ou somar.
- **Log e rollback**: quem subiu, quando, qual arquivo, quantas linhas — com desfazer.

### 7.3 Drivers de orçamento

Um **driver** é a regra que calcula o valor orçado em vez de alguém digitar. Cada driver tem **nível de aplicação**, **tipo de cálculo** e **vigência**.

**Níveis de aplicação** (do mais amplo ao mais específico):

| Nível | Exemplo |
|---|---|
| **Global** | Reajuste de 5% em tudo para o próximo ano |
| **Empresa** | A empresa X orça viagens com teto próprio |
| **Natureza** | Energia elétrica cresce pelo índice contratado |
| **Centro de Resultado (CR)** | O CR de Compras tem regra própria de material de consumo |
| **Projeto** | O projeto Y tem verba fechada distribuída pelo cronograma |
| **Combinação** | Empresa X + natureza Z + CR 1234 |

**Precedência:** do mais específico para o mais genérico. Driver de `empresa+natureza+CR` vence o de `natureza`, que vence o `global`.

**Tipos de driver incluídos nas 134h:**

| Tipo | Cálculo | Exemplo |
|---|---|---|
| **Valor fixo** | Valor absoluto por mês | R$ 50.000/mês de aluguel |
| **% sobre base** | Percentual sobre outra natureza ou grupo | Encargos = 70% da folha |
| **% de meta de receita** | Percentual sobre a meta de receita do período | Marketing = 2% da receita orçada do mês |

O driver de **% sobre meta de receita** exige o **cadastro de metas de receita** por empresa (e opcionalmente por projeto) mês a mês. Quando a meta muda, as naturezas ligadas a ela **recalculam em cascata**, com o impacto no total exibido antes de aplicar.

### 7.4 Simulação antes de aplicar

Nenhum driver grava direto: **configura → simula → aplica**. A simulação mostra o valor que cada célula assumiria e o delta contra o cenário atual; a aplicação grava sempre dentro de um cenário, preservando o anterior.

### 7.5 Dimensões Empresa e Projeto

Entram como dimensões novas (espelho do Sankhya) e passam a valer em toda a cadeia: modelo, fatos, filtros, drill e dashboards.

```
META_RECEITA   (empresa_id, projeto_id, periodo, valor_meta, cenario_id)
DRIVER         (driver_id, nome, tipo, nivel, empresa_id, cr_id, projeto_id,
                natureza_id, parametros_json, data_inicio, data_fim, prioridade, ativo)
DRIVER_EXECUCAO (execucao_id, driver_id, cenario_id, data, usuario, linhas_afetadas, valor_total)
```

### 7.6 Detalhamento de horas — Fase 7 (12h)

| Task | Horas | Detalhes |
|---|---:|---|
| Dimensões Empresa e Projeto | 3h | Espelho do Sankhya, propagação nos fatos, filtros, drill e dashboards |
| Upload de orçamento via planilha | 4h | Template, mapeamento de colunas, preview com delta, validações, modos de carga, log e rollback |
| Motor de drivers (3 tipos) | 4h | Cadastro, precedência entre níveis, vigência, cálculo, geração em cenário, marcação de origem |
| Metas de receita + driver % de receita | 1h | Cadastro de metas por empresa/projeto/mês e recálculo em cascata |
| **Total Fase 7** | **12h** | |

---

# PARTE IV — EXECUÇÃO

## 8. Cronograma

| Período | Atividade | Horas |
|---|---|---:|
| Semana 1–2 | Fase 0 + Fase 1 — Carga, modelagem e Dashboard core | 28h |
| Semana 3–4 | Fase 2 + Fase 3 — Planejamento + Jornada | 22h |
| Semana 5–6 | Fase 6 — Compras (CR, de-para, cenários) | 28h |
| Semana 7 | Fase 7 — Motor de Orçamento (upload + drivers) | 12h |
| Semana 8–10 | Fase 4 — Integrações (conforme credenciais) | 35h |
| Semana 10 | Fase 5 — Agente IA + Testes + Deploy | 9h |
| | **TOTAL** | **134h** |

A Fase 4 pode ser antecipada e rodar em paralelo assim que cada credencial chegar — o sistema já opera desde a Fase 1 com os dados das planilhas.

## 9. Premissas

- **Folha e Compras compartilham a mesma base: o Sankhya.** Empresa, CR, natureza, projeto e parceiro seguem o cadastro do Sankhya; o sistema espelha, não cria cadastro paralelo.
- O Realizado (Folha e Compras) vem integralmente do Sankhya — não há segunda fonte.
- O de-para CR ↔ Gerência é a única estrutura mantida fora do Sankhya, por ser criação interna.
- **O número do CR não muda.** Toda mudança de estrutura é tratada pelo de-para.
- As planilhas de origem de Folha/RH mantêm a estrutura atual das 18 abas.
- Encargos sobre promoções considerados a 70% (regra atual da planilha).
- Cada integração entra no cronograma após a credencial correspondente ser liberada.
- Drivers nunca gravam direto: passam por simulação e são aplicados dentro de um cenário.
- **As 134h são fechadas.** Escopo além do descrito neste documento entra como aditivo (ver item 12).

## 10. Dependências do cliente

| Item | O que providenciar | Urgência |
|---|---|---|
| **Sankhya** | URL do ambiente, usuário de serviço com token, liberação dos serviços/consultas (financeiro, compras, cadastros) | **Alta** — fonte única de dados |
| **De-para CR ↔ Gerência** | Planilha atual do redirecionamento, com vigências conhecidas | **Alta** — bloqueia a Fase 6 |
| **Responsáveis por CR** | CRs vêm do Sankhya; o cliente informa o responsável de cada CR | Alta |
| **Planilhas de orçamento de Compras** | Base atual por CR e natureza | Alta |
| **Metas de receita** | Metas por empresa/projeto e mês, para o driver de % sobre receita | Alta |
| **Regras de driver** | Lista das naturezas dirigidas por regra e a fórmula de cada uma | Alta |
| Ponto Tel | Confirmação de API no contrato, documentação, token | Média |
| Mindsight | Confirmação de disponibilidade de API, token | Média |
| Power BI | App Registration no Azure AD (tenant/client id + secret) | Média |
| Gupy | API Token do painel | Baixa |
| Moodle | URL + token de webservice | Baixa |

## 11. Fora de escopo

- Folha de pagamento transacional (cálculo/geração de folha) — o sistema é orçamentário.
- Emissão, aprovação ou workflow transacional de pedidos de compra — o sistema **lê** pedidos e contratos do Sankhya, não os origina.
- Gestão de fornecedores, cotações e sourcing.
- Alteração de cadastro (CR, natureza, empresa, projeto) no Sankhya — o sistema é somente leitura sobre esses cadastros.
- Definição das regras de negócio dos drivers — o sistema executa as regras; a política orçamentária é do cliente.
- Orçamento de receita em si: o sistema **consome** a meta de receita como parâmetro de driver, não a orça.
- Integrações não listadas no item 5.
- Migração histórica além das planilhas fornecidas.

## 12. Backlog — o que ficou fora do fechamento

Itens tecnicamente prontos para entrar, mas não incluídos nas 134h. Ficam disponíveis como aditivo ou 2ª fase.

| Item | Horas est. | Por que ficou fora |
|---|---:|---|
| **Pedidos & Contratos — empenho/comprometido** | 3h | A versão simplificada (realizado x saldo) está incluída na Fase 6. Falta o comprometido/empenhado, que depende de mapear os status de pedido no Sankhya |
| **Drivers avançados** | 4h | Índice/inflação, per capita (por headcount) e escalonado por volume — os 3 tipos principais estão incluídos |
| **Tela de rastreio de driver por célula** | 2h | Visão de qual driver venceu em cada célula e travas manuais em lote |
| **Workflow de coleta orçamentária** | 6h | Ciclo de envio/aprovação por responsável de CR, com prazos e status |
| **Importação Compras avançada** | 3h | Perfis múltiplos de mapeamento e reconciliação automática de divergências |
| **Aprofundamento das integrações** | — | As 35h são o mínimo contratual; APIs mais complexas que o previsto podem exigir aditivo |
| | **~18h** | |
