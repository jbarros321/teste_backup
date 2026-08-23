# Estimativa de Horas — Orçamentário de Folha/RH (Voke)

> Conversão fiel do documento `Estimativa-Horas-Orcamentario-Folha-RH (1).doc`
> Data do documento: **17/07/2026**

## Resumo Executivo

| Bloco | Horas | % |
|---|---:|---:|
| Desenvolvimento do Sistema (Fases 0–3) | 60h | 56% |
| Integrações e Consolidação (Fase 4) | 38h | 35% |
| Agente IA + Testes + Deploy (Fase 5) | 10h | 9% |
| **TOTAL** | **108h** | **100%** |

## Escopo de Desenvolvimento

Substituir as planilhas de orçamento (`Planejamento_Banco_de_Horas` e `Considerações_Folha`) por um sistema único de orçamento de pessoal/folha, com **Orçado x Realizado x Forecast**, planejamento de headcount e visão de RH — alimentado primeiro pela carga das planilhas e, depois, pelas integrações.

### Objetivos do sistema
- Acompanhar Orçado x Realizado x Desvio x Forecast por Diretoria, Gerência, Centro de Custo e Natureza.
- Planejar vagas (headcount e ramp de salários mês a mês).
- Provisionar movimentações (promoções/progressões) e turnover (custos rescisórios).
- Monitorar jornada (banco de horas, horas extras, faltas, atestados) e seu impacto em folha.
- Base para treinamentos (T&D) como dimensão de orçamento.

## Entregas por Tela

| Tela / Módulo | O que entrega |
|---|---|
| **Dashboard Orçamento (home)** | KPIs (Orçado, Realizado, Desvio %, Forecast do ano), gráfico Orçado x Real x Forecast mensal, top desvios por natureza e quebra por Diretoria. Cross-filter e drill: Diretoria → Gerência → Centro de Custo → Natureza. |
| **Folha / DRE** | Planilha inteligente (tipo Excel) com Orçado x Real x Desvio por rubrica de folha e mês — espelho da aba Resumo, com fórmulas e persistência. |
| **Planejamento de Vagas** | Headcount por área, ramp de salários mês a mês e custo acumulado. Grid editável. |
| **Movimentações & Turnover** | Provisões de promoções (performance, base salarial, encargos) e custos rescisórios por diretoria/mês. |
| **Jornada (Banco de Horas)** | Banco de horas, horas extras, faltas e atestados por colaborador e área, com impacto financeiro estimado. |
| **Colaboradores** | Cadastro e consulta com hierarquia de liderança (Diretoria / Gerência / LMA) e filtros. |
| **Importação** | Upload dos XLSX/CSV com preview e validação. Cada integração substitui a carga manual quando as credenciais chegarem. |
| **Agente IA de negócio** | Consultas em linguagem natural sobre os dados de orçamento (ex.: "qual o desvio de FGTS acumulado?"). |

## Fases de Entrega

| Fase | Entrega | Depende de |
|---|---|---|
| **Fase 0 — Carga inicial** | Import das planilhas para as tabelas; dashboard e telas funcionando com dados reais dos XLSX | Somente das planilhas (já temos) |
| **Fase 1 — Orçamento core** | Dashboard Orçado x Real x Forecast + DRE de folha + drill/cross-filter | Fase 0 |
| **Fase 2 — Planejamento** | Vagas, Movimentações e Turnover (planilhas editáveis) | Fase 1 |
| **Fase 3 — Jornada** | Telas de banco de horas / HE / faltas + impacto financeiro | Fase 0 |
| **Fase 4 — Integrações** | Substituir carga manual por SAP, Ponto Tel, Gupy, Mindsight, Moodle e Power BI | Credenciais do cliente |
| **Fase 5 — Agente IA** | Chat de negócio sobre os dados de orçamento | Fases 1–3 |

## Detalhamento Técnico de Horas

### Bloco 1 — Desenvolvimento do Sistema (60h)

#### Fase 0 — Carga Inicial e Infraestrutura (12h)

| Task | Horas | Detalhes |
|---|---:|---|
| Modelagem do banco (DDL normalizado) | 3h | 4 dimensões + 8 tabelas fato/planejamento, FKs, índices |
| Script de carga das planilhas (XLSX/CSV) | 4h | Parser das 2 planilhas (18 abas), normalização, tratamento de horas (hh:mm→min), datas |
| Server Functions base (CRUD + queries) | 3h | SFs SQL para cada entidade, paginação, filtros |
| Testes de carga e validação de dados | 2h | Conferência de totais, integridade referencial |

#### Fase 1 — Orçamento Core (18h)

| Task | Horas | Detalhes |
|---|---:|---|
| Dashboard principal (home) | 6h | 4 KPIs (Orçado, Realizado, Desvio %, Forecast), gráfico Orçado x Real x Forecast mensal, top desvios por natureza, quebra por Diretoria |
| Cross-filter + Drill-down | 4h | Drill: Diretoria → Gerência → Centro de Custo → Natureza. Cross-filter bidirecional em todos os gráficos |
| Tela Folha / DRE | 5h | Handsontable com Orç x Real x Desvio por rubrica e mês (espelho aba Resumo), fórmulas HyperFormula |
| SFs de drill e universais | 3h | SFs por dimensão com todos os params, SFs Universal para cross-filter |

#### Fase 2 — Planejamento (16h)

| Task | Horas | Detalhes |
|---|---:|---|
| Planejamento de Vagas | 5h | Grid editável: headcount por área, ramp salarial mês a mês, custo acumulado |
| Movimentações & Turnover | 5h | Provisões de promoções (performance, base salarial, encargos 70%), custos rescisórios por diretoria/mês |
| Colaboradores (cadastro) | 3h | CRUD com hierarquia de liderança, filtros por diretoria/gerência/LMA |
| Importação manual | 3h | Upload de XLSX/CSV com preview, mapeamento de colunas, validação |

#### Fase 3 — Jornada (14h)

| Task | Horas | Detalhes |
|---|---:|---|
| Banco de Horas | 4h | Visão por colaborador e por área, total positivo/negativo, comportamento |
| Horas Extras | 3h | Ranking, totais por diretoria/LMA, impacto financeiro estimado |
| Faltas e Atestados | 3h | Absenteísmo por área, tendência, alertas |
| Impacto financeiro consolidado | 4h | Cálculo de custo de HE em folha, projeção de banco de horas, dashboard jornada |

### Bloco 2 — Integrações e Consolidação de Dados (38h)

> Mínimo contratual: **35h**. Estimativa real: **38h**.

#### Integração SAP — Realizado Contábil/Compras (10h)

| Task | Horas | Detalhes |
|---|---:|---|
| Análise da versão/gateway + configuração de conexão | 2h | Identificar ECC/S4HANA/B1, configurar authType (OAuth/Session) |
| Criação da integração custom + teste | 2h | blueprintId, authenticationConfig, testIntegration |
| SFs INTEGRATION para FI (contábil) e MM (compras) | 3h | Endpoints de lançamentos por CC/natureza/período |
| Data Loader incremental + consolidação com dados locais | 3h | IMP_FIN_REALIZADO, merge com FATO_ORCAMENTO, validação de totais |

#### Integração Ponto Tel — Jornada (8h)

| Task | Horas | Detalhes |
|---|---:|---|
| Análise da API + configuração de conexão | 2h | Confirmar endpoints, auth, limites |
| SFs INTEGRATION (marcações, saldos, HE, faltas) | 3h | 4–5 endpoints diferentes |
| Data Loader + consolidação | 3h | IMP_PONTO_BANCO, IMP_PONTO_HE, IMP_PONTO_FALTAS, merge com tabelas locais |

#### Integração Gupy — Vagas (5h)

| Task | Horas | Detalhes |
|---|---:|---|
| Configuração de conexão (STATIC_KEY) | 1h | Token direto, auth simples |
| SFs INTEGRATION (/jobs, /candidates) | 2h | Endpoints de vagas e status |
| Data Loader + consolidação | 2h | IMP_VAGAS, merge com PLAN_VAGAS |

#### Integração Mindsight — Desempenho (5h)

| Task | Horas | Detalhes |
|---|---:|---|
| Análise da API + configuração | 1.5h | Confirmar disponibilidade, auth |
| SFs INTEGRATION (ciclos, notas) | 1.5h | Endpoints de avaliação |
| Data Loader + consolidação | 2h | IMP_AVALIACOES, merge com PLAN_MOVIMENTACOES |

#### Integração Moodle — Treinamentos (5h)

| Task | Horas | Detalhes |
|---|---:|---|
| Configuração de conexão (token webservice) | 1h | URL + token |
| SFs INTEGRATION (courses, completions) | 2h | core_course_*, core_completion_* |
| Data Loader + consolidação | 2h | IMP_TREINAMENTOS, tela de T&D |

#### Integração Power BI — Dashboards (5h)

| Task | Horas | Detalhes |
|---|---:|---|
| App Registration Azure AD + OAuth2 | 2h | tenant_id, client_id, client_secret, permissões |
| SFs INTEGRATION (datasets, executeQueries DAX) | 2h | Metadados + queries |
| Consolidação de métricas | 1h | IMP_BI_METRICAS |

### Bloco 3 — Agente IA + Testes + Deploy (10h)

| Task | Horas | Detalhes |
|---|---:|---|
| Configuração do agente de negócio | 3h | Perfis, tabelas liberadas, SFs permitidas, Business Instructions |
| Testes funcionais completos | 4h | Validação de todas as telas, cross-filter, drill, carga, permissões |
| Deploy e ajustes finais | 3h | Build, revisão visual, ajustes de UX |

## Cronograma Sugerido

| Período | Atividade | Horas |
|---|---|---:|
| Semana 1–2 | Fase 0 + Fase 1 (Carga + Dashboard core) | 30h |
| Semana 3–4 | Fase 2 + Fase 3 (Planejamento + Jornada) | 30h |
| Semana 5–7 | Fase 4 (Integrações — conforme credenciais) | 38h |
| Semana 8 | Fase 5 (Agente IA + Testes + Deploy) | 10h |

> As integrações (Fase 4) podem ser iniciadas em paralelo assim que as credenciais estiverem disponíveis. O sistema funciona desde a Fase 1 com dados das planilhas.

## Dependências Externas (responsabilidade do cliente)

| Integração | O que providenciar | Urgência |
|---|---|---|
| SAP | Versão do SAP, URL gateway, credenciais FI/MM | **Alta** (coração do orçamento) |
| Ponto Tel | Confirmação de API no contrato, doc, token | Média |
| Gupy | API Token do painel Gupy | Baixa (auth simples) |
| Mindsight | Confirmação de API, token | Média |
| Moodle | URL + token de webservice | Baixa (auth simples) |
| Power BI | App Registration Azure AD | Média |
