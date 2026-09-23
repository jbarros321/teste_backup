# Escopo Funcional — AllStrategy

Sistema de Gestão Orçamentária e Planejamento Corporativo

| | |
|---|---|
| **Produto** | AllStrategy |
| **Plataforma de construção** | Mitra (MitraLab) |
| **Origens de dados** | **Sankhya** (ERP — financeiro, contábil, faturamento, compras) · **Senior** (folha, RH, ponto) |
| **Destino de publicação** | Sankhya (meta/orçamento e verba) |
| **Data base** | 23/09/2026 |
| **Esforço total** | **1.420h** — 3 ciclos |
| **Duração** | 11 meses (Ciclo 1 operante no mês 5) |

---

## Sumário

**Parte I — Produto** · 1 Problema · 2 Visão · 3 Glossário · 4 Mapa de módulos
**Parte II — Escopo funcional** · 5 a 27, um capítulo por módulo — inclui **27 Playground analítico**
**Parte III — Fundação técnica** · 28 Modelo de dados · 29 Regras transversais · 30 Integrações e multi-origem · 31 Requisitos não funcionais · 32 Construção na Mitra
**Parte IV — Execução** · 33 Fases e horas · 34 Cronograma · 35 Premissas · 36 Dependências · 37 Fora de escopo · 38 Backlog · 39 Critérios de aceite

---

# PARTE I — PRODUTO

## 1. Problema

O orçamento corporativo vive hoje em planilhas. Isso produz cinco falhas que o AllStrategy existe para eliminar:

| Falha | Como se manifesta |
|---|---|
| **Não há versão única da verdade** | Cada área tem sua planilha; o consolidado é mais um arquivo que ninguém sabe se está atualizado |
| **O Realizado entra à mão** | Alguém exporta o ERP, cola no Excel e concilia — com atraso de semanas e erro silencioso de sinal, competência e granularidade |
| **Não existe rastro** | Ninguém sabe quem mudou qual célula, quando, por quê, nem qual era o valor antes |
| **A coleta não tem ciclo** | Enviar, cobrar, aprovar e consolidar o orçamento das áreas é trabalho manual de e-mail e telefone |
| **O orçamento não governa a execução** | Aprovado o orçamento, nada impede a área de gastar acima dele: a verba não bloqueia requisição nem pedido |

## 2. Visão do produto

Um sistema onde o orçamento **nasce, é aprovado, vira parâmetro do ERP, é confrontado com o realizado e se reprojeta** — em ciclo fechado, versionado e auditável.

```
PREMISSAS ─→ DRIVERS ─→ CAPTAÇÃO ─→ WORKFLOW ─→ ORÇAMENTO APROVADO
                                                        │
                                          [publicação no ERP: meta e verba]
                                                        │
   SANKHYA ──┐                                          │
             ├──→ REALIZADO ──→ CONFRONTO ──→ DESVIO + JUSTIFICATIVA
   SENIOR  ──┘                       │                  │
                                     └──→ FORECAST ←────┘
                                              │
                            DRE / BALANÇO / FLUXO DE CAIXA PROJETADOS
                                              │
                                        CONSOLIDAÇÃO
                                              │
                          ┌───────────────────┴───────────────────┐
                          │   PLAYGROUND ANALÍTICO (self-service)  │
                          │  estudos, dashboards e análises que o  │
                          │  usuário constrói sobre o mesmo modelo │
                          └────────────────────────────────────────┘
```

Cinco princípios de projeto, que valem como regra em todos os módulos:

1. **O fato nunca guarda hierarquia.** Empresa, CR, projeto e natureza são gravados como chave; diretoria, gerência, grupo de DRE e pacote são resolvidos em tempo de consulta, respeitando vigência. Reorganização de estrutura não reescreve histórico.
2. **Toda medida é soma, nunca subtração.** O sinal vem na carga (receita positiva, despesa negativa). Nenhum relatório usa `ABS()` nem inverte sinal por linha.
3. **Nada sobrescreve versão congelada.** Toda gravação acontece dentro de um cenário aberto. Congelar é irreversível; a correção nasce como nova revisão.
4. **Toda célula sabe de onde veio.** Digitada, importada de planilha, gerada por driver, replicada de outro cenário ou rateada — a origem é gravada junto com o valor.
5. **Simular antes de aplicar.** Driver, rateio, carga e reprojeção mostram o delta antes de gravar.
6. **O modelo é aberto ao usuário.** Tudo que o sistema calcula está disponível no Playground para o usuário montar a própria análise, sob as mesmas regras de negócio e de segurança.

## 2.1 Além do AllStrategy — o que diferencia

O AllStrategy de mercado resolve o ciclo orçamentário. Este projeto entrega o ciclo **e mais quatro coisas** que a ferramenta de prateleira não dá:

| # | Diferencial | O que significa na prática |
|---|---|---|
| 1 | **Multi-origem nativo** | Financeiro, contábil, faturamento e compras vêm do **Sankhya**; folha, headcount, ponto e movimentações vêm do **Senior**. O orçamento de pessoal deixa de ser planilha: o realizado de folha entra automático, no mesmo grão e no mesmo DRE das demais naturezas |
| 2 | **Playground analítico** | O usuário de negócio constrói seus próprios estudos, dashboards e análises sobre o modelo do orçamento, sem depender de TI e sem exportar para o Excel. A ferramenta de prateleira entrega relatórios fechados; aqui o modelo é o produto |
| 3 | **Ciclo fechado com o ERP** | O orçamento aprovado volta ao Sankhya como meta e como **verba que bloqueia requisição e pedido**. O orçamento governa a execução, não apenas a reporta |
| 4 | **Rastreabilidade até o documento** | Qualquer número — de um KPI do dashboard a uma célula de um estudo do Playground — desce por drill até o lançamento e o documento de origem no ERP, com a chave de rastreabilidade |

Consequência de projeto: o Playground **não é um BI paralelo**. Ele lê o mesmo modelo semântico que alimenta o DRE oficial, com as mesmas regras de sinal, competência, de-para e segurança por dimensão — de modo que o estudo do usuário e o relatório oficial nunca divergem.

## 3. Glossário

| Termo | Definição operativa no sistema |
|---|---|
| **Ciclo orçamentário** | Exercício sendo orçado, com calendário próprio de abertura, coleta, aprovação e congelamento |
| **Cenário / Versão** | Corte nomeado e versionado do orçamento: Budget (original), Revisado 1..n, Forecast, Simulação. É a chave de todo fato orçado |
| **Medida** | `ORCADO` · `REALIZADO` · `FORECAST` · `COMPROMETIDO` |
| **Dimensão** | Eixo de análise: Empresa, Centro de Resultado, Projeto, Natureza, Gerência, Diretoria, Pacote, Responsável, Moeda |
| **Natureza** | Conta gerencial. Hierárquica (N1→N2→N3→analítica); o N1 define o grupo do DRE |
| **Centro de Resultado (CR)** | Chave operacional imutável do orçamento. A estrutura organizacional é resolvida por de-para, nunca alterando o CR |
| **Pacote** | Agrupamento matricial de naturezas com dono corporativo (ex.: Viagens, Telecom, Facilities) |
| **Premissa** | Parâmetro de negócio datado (inflação, câmbio, headcount-alvo, meta de receita) |
| **Driver** | Regra que calcula o valor orçado em vez de alguém digitar |
| **Verba** | Saldo orçado disponível em uma célula (empresa × CR × natureza × período), líquido de realizado e comprometido |
| **Comprometido** | Valor já empenhado por requisição/pedido aprovado, ainda não realizado |
| **Competência gerencial** | Período ao qual o valor pertence para análise (`ANO`/`MES`), que pode diferir da data do lançamento |
| **Remessa de competência** | Reclassificação de um lançamento para outro período gerencial, com rastro do período de origem |
| **Origem do dado** | Sistema de onde o registro veio: `SANKHYA`, `SENIOR`, `PLANILHA`, `DRIVER`, `RATEIO`, `MANUAL` |
| **Modelo semântico** | Camada de dimensões, medidas e regras publicada para consumo — usada igualmente pelos relatórios oficiais e pelo Playground |
| **Estudo** | Análise construída pelo usuário no Playground: conjunto de consultas, visualizações e comentários, versionado e compartilhável |

## 4. Mapa de módulos

| # | Bloco | Módulos |
|---|---|---|
| **A** | Fundação | 5 Cadastros e dimensões · 6 Estrutura de DRE · 7 Estruturas organizacionais e de-para |
| **B** | Planejamento | 8 Ciclos e cenários · 9 Premissas · 10 Drivers · 11 Captação · 12 Receita · 13 Pessoal · 14 Custeio e OBZ · 15 Matricial · 16 Investimentos · 17 Rateios |
| **C** | Governança | 18 Workflow de aprovação · 19 Publicação no ERP · 20 Controle de verba |
| **D** | Controle | 21 Realizado e conciliação · 22 Desvios e justificativas · 23 Forecast |
| **E** | Resultado | 24 Demonstrativos projetados · 25 Consolidação · 26 Relatórios e análise |
| **F** | Self-service | **27 Playground analítico** — modelo semântico, construtor de estudos, dashboards do usuário, biblioteca e governança |
| **G** | Transversal | Segurança · Auditoria · Notificação · Agendamento · Administração |

---

# PARTE II — ESCOPO FUNCIONAL

## 5. Cadastros e dimensões

**Objetivo:** dar ao orçamento os mesmos eixos do ERP, sem cadastro paralelo.

### 5.1 Dimensões espelhadas dos sistemas de origem (somente leitura)

| Dimensão | Conteúdo | Origem | Chave |
|---|---|---|---|
| Empresa | Razão social, CNPJ, moeda funcional, situação | Sankhya (mestre) | `CODEMP` |
| Centro de Resultado | Código, descrição, responsável, situação | Sankhya (mestre) | `CODCENCUS` |
| Projeto | Código, nome, empresa, vigência, situação | Sankhya | `CODPROJ` |
| Natureza | Código, descrição, tipo (R/D), pai, analítica S/N | Sankhya | `CODNAT` |
| Parceiro | Fornecedor/cliente — detalhe do realizado | Sankhya | `CODPARC` |
| Usuário | Nome, e-mail, situação — base de responsáveis e aprovadores | Sankhya | `CODUSU` |
| Colaborador | Matrícula, nome, admissão, situação | **Senior** | matrícula |
| Cargo / Função | Código, descrição, faixa salarial | **Senior** | código |
| Estrutura de RH | Filial, departamento, centro de custo da folha | **Senior** | código |
| Rubrica de folha | Código, descrição, tipo (provento/desconto/encargo) | **Senior** | código |

**Regras:**
- Carga **incremental e agendada**; o sistema nunca cria nem edita cadastro no ERP. Cadastro novo nasce no ERP e aparece no ciclo seguinte de sincronização.
- Cadastro **inativado no ERP não é apagado** no orçamento: fica marcado como inativo, continua visível no histórico e é bloqueado para novo lançamento.
- Toda sincronização registra: quando rodou, quantos registros entraram/mudaram, e o que falhou.
- Tela de **saúde dos cadastros**: naturezas sem tipo definido, CRs sem responsável, projetos vencidos, natureza analítica sem pai — cada uma com ação de correção.

### 5.2 Dimensões próprias do AllStrategy

Mantidas dentro do sistema por não existirem no ERP:

| Dimensão | Para quê |
|---|---|
| **Diretoria / Gerência** | Camada organizacional de leitura, resolvida por de-para sobre o CR |
| **Pacote** | Eixo do orçamento matricial |
| **Grupo de DRE** | Blocos do demonstrativo gerencial |
| **Responsável orçamentário** | Dono da célula para coleta, aprovação e justificativa |
| **Cenário** | Versão do orçamento |
| **Moeda e taxa** | Multi-moeda e conversão |

### 5.3 Vigência

Toda dimensão própria e todo de-para carregam `data_inicio` / `data_fim`. As consultas sempre informam a data de referência, e o sistema oferece duas visões:

- **Estrutura vigente** — reprocessa todo o histórico pela estrutura de hoje (comparabilidade).
- **Estrutura da época** — cada período lido pela estrutura que valia nele (fidelidade ao que foi aprovado).

A visão é escolhida pelo usuário e aparece rotulada em todo relatório exportado.

### 5.4 Multi-origem — Sankhya e Senior

O sistema lê de dois sistemas de origem, e isso exige regra explícita de quem manda em quê.

| Domínio | Sistema mestre | O que entra |
|---|---|---|
| Estrutura financeira (empresa, CR, projeto, natureza, parceiro) | **Sankhya** | Dimensões do orçamento inteiro |
| Realizado financeiro, contábil, faturamento e compras | **Sankhya** | Lançamentos analíticos |
| Pessoas (colaborador, cargo, estrutura de RH) | **Senior** | Quadro atual e base do orçamento de pessoal |
| Folha realizada (rubricas, encargos, provisões) | **Senior** | Realizado de pessoal por CR e rubrica |
| Jornada (ponto, banco de horas, HE, faltas) | **Senior** | Impacto financeiro e absenteísmo |
| Movimentações (admissão, desligamento, promoção, afastamento) | **Senior** | Realizado de headcount e turnover |

**Regras de convivência:**

- **Um mestre por domínio.** Nenhuma dimensão tem dois donos. Onde há sobreposição (CR financeiro × centro de custo da folha), existe um **de-para explícito**, com vigência, e não uma tentativa de casar códigos por coincidência.
- **De-para Centro de Custo Senior ↔ CR Sankhya**, com percentual de rateio quando um centro de custo da folha se distribui entre CRs — mesma mecânica do de-para CR ↔ Gerência (item 7), com as mesmas validações.
- **De-para Rubrica Senior ↔ Natureza Sankhya:** cada rubrica de folha é mapeada para a natureza gerencial correspondente, com vigência. Rubrica sem mapeamento cai em uma natureza de "folha não classificada" e aparece em relatório de pendência — nunca é descartada silenciosamente.
- **Sem dupla contagem.** Quando o custo de folha também chega pelo lançamento contábil do Sankhya, apenas **uma** origem é considerada realizado, definida por natureza em parâmetro; a outra fica disponível para conferência, marcada e fora da totalização.
- **Conciliação Senior × Sankhya × AllStrategy:** tela que confronta o total da folha do período nos três lados, apontando a diferença por CR e por rubrica.
- **Frescor independente por origem:** cada sistema tem seu próprio badge de carga; o dashboard sinaliza quando uma das origens está defasada, identificando qual.
- **Falha isolada:** a indisponibilidade de uma origem não impede a carga da outra; o período fica marcado como parcial e é reprocessado quando a origem voltar.

## 6. Estrutura de DRE gerencial

**Objetivo:** ordenar naturezas em um demonstrativo com subtotais e indicadores.

- **Árvore de naturezas** em até 4 níveis (N1→N2→N3→analítica), montada por recursão a partir do cadastro do ERP, com **ordenação semântica** configurável (Receita → Custo → Despesa → Depreciação → Provisão → Investimento) e não alfabética.
- **Grupos de DRE:** cada N1 é vinculado a um grupo, e os grupos compõem os **subtotais calculados** — Receita Líquida, Lucro Bruto, EBITDA, LAIR, Resultado Líquido — além de linhas fora do resultado (Investimentos).
- **Regra do grupo, não do tipo:** a classificação Receita/Despesa de uma linha vem do **grupo de DRE (N1)**, não do tipo da natureza. Deduções de receita (impostos sobre venda, descontos, cancelamentos) são naturezas de despesa que pertencem ao bloco de receita e entram somadas negativas.
- **Fallback de tipo:** natureza sem tipo definido assume o tipo do seu grupo, com alerta na tela de saúde dos cadastros.
- **Linhas calculadas** configuráveis: subtotais, indicadores percentuais (margem bruta, % de despesa sobre receita), AV (análise vertical sobre a receita líquida) e AH (análise horizontal contra período anterior).
- **Múltiplas visões de DRE** (gerencial, societário, por projeto), cada uma com seu mapeamento de grupos — sem duplicar os fatos.
- **Filtro de estrutura:** possibilidade de restringir o demonstrativo exclusivamente às naturezas vinculadas à estrutura contábil/financeira, deixando fora naturezas operacionais e de uso interno.

## 7. Estruturas organizacionais e de-para

**Objetivo:** permitir que a organização mude sem tocar na chave transacional.

O CR é a chave imutável — pedidos, contratos e lançamentos estão amarrados a ele. Quando uma área muda de diretoria, **o CR não muda**: altera-se o de-para.

```
Lançamento → CR (imutável) → [DE-PARA vigente] → Gerência → Diretoria
```

**Funcionalidades:**
- CRUD do de-para **CR ↔ Gerência** com vigência, histórico e **percentual de rateio** (um CR pode se dividir entre gerências).
- CRUD do de-para **CR ↔ Responsável** com vigência — base do workflow de coleta e da carteira do responsável.
- CRUD do de-para **Natureza ↔ Pacote** com vigência — base do matricial.
- CRUD dos de-paras de multi-origem (item 5.4): **Centro de Custo Senior ↔ CR Sankhya** com rateio e **Rubrica Senior ↔ Natureza**, ambos com vigência e relatório de itens não mapeados.
- **Validações bloqueantes:** CR órfão (sem gerência vigente no período do ciclo), rateio somando diferente de 100%, vigências sobrepostas para a mesma chave, gerência sem diretoria.
- **Simulação de reorganização:** aplicar um de-para novo em modo de teste e ver o impacto nos totais por diretoria antes de efetivar.
- Importação do de-para por planilha, com preview e validação.

## 8. Ciclos orçamentários e cenários

**Objetivo:** dar ao orçamento o conceito de versão, com estados e travas.

### 8.1 Ciclo

Um ciclo é o exercício sendo orçado. Tem: ano, empresas participantes, calendário (abertura da coleta, prazo por etapa, data-limite de aprovação, congelamento), responsável do ciclo e status.

### 8.2 Tipos de cenário

| Tipo | Uso | Editável |
|---|---|---|
| **Budget (Orçado original)** | Versão aprovada e congelada do ciclo — base de comparação do ano inteiro | Não, após congelar |
| **Revisado 1..n** | Reorçamento no meio do ano, com data, autor e justificativa obrigatória | Sim, até congelar |
| **Forecast** | Realizado acumulado + projeção do restante do ano | Sim |
| **Simulação / What-if** | Cenário de trabalho; não entra em relatório oficial nem publica no ERP | Sim |

### 8.3 Ciclo de vida do cenário

```
RASCUNHO → EM COLETA → EM APROVAÇÃO → APROVADO → CONGELADO → PUBLICADO
                 ↑             │
                 └── DEVOLVIDO ┘
```

**Regras:**
- Gravação só é aceita em cenário `RASCUNHO` ou `EM COLETA`, e apenas nas células que o usuário tem permissão de editar.
- **Congelar é irreversível.** Grava data, usuário e um hash dos totais por empresa, como prova de integridade.
- **Cópia de cenário:** criar o Revisado 2 a partir do Revisado 1, ou o Budget do ano seguinte a partir do Realizado do ano corrente, com fator de ajuste opcional por natureza, grupo ou global.
- **Comparação cenário × cenário** em qualquer tela: duas colunas de valor, delta absoluto e delta %.
- **Log de alterações por célula:** valor anterior, valor novo, usuário, data/hora, origem e cenário — consultável e exportável.
- **Cenário padrão de leitura** por perfil de usuário, para que os relatórios abram sempre na versão certa.

## 9. Premissas

**Objetivo:** centralizar os parâmetros de negócio que alimentam os cálculos, em vez de deixá-los escondidos em fórmulas.

- Cadastro de premissas por **tipo** (índice, percentual, valor, quantidade), **escopo** (global, empresa, projeto, CR, natureza) e **vigência mensal** — inflação, reajuste salarial, encargos, câmbio, meta de receita, headcount-alvo, preço médio.
- Cada premissa tem versão por cenário: o Revisado 2 pode ter câmbio diferente do Budget.
- **Recálculo em cascata:** alterar uma premissa mostra quantas células e qual valor total serão afetados, e só recalcula após confirmação.
- **Rastreabilidade inversa:** dada uma célula, ver quais premissas a influenciaram.
- Cadastro de **metas de receita** por empresa/projeto/mês, usado como base dos drivers percentuais.

## 10. Drivers de orçamento

**Objetivo:** gerar o orçamento por regra onde a digitação não agrega.

### 10.1 Níveis de aplicação

Global · Empresa · Natureza · Centro de Resultado · Projeto · Pacote · Combinação.

**Precedência:** do mais específico para o mais genérico. Um driver de `empresa+natureza+CR` vence o de `natureza`, que vence o `global`. A tela mostra, para cada célula, **qual driver venceu**.

### 10.2 Tipos de driver

| Tipo | Cálculo |
|---|---|
| **Valor fixo** | Valor absoluto por mês, com curva de distribuição (linear, sazonal, por dias úteis, manual) |
| **% sobre base** | Percentual sobre outra natureza ou grupo (ex.: encargos = % da folha) |
| **% sobre meta** | Percentual sobre a meta de receita do período (ex.: marketing = 2% da receita orçada) |
| **Histórico + ajuste** | Realizado do ano anterior × fator (inflação, crescimento), com opção de excluir outliers |
| **Per capita** | Valor unitário × headcount orçado da área no mês |
| **Por volume** | Valor unitário × quantidade projetada, com faixas escalonadas |
| **Índice** | Base × índice econômico cadastrado como premissa, mês a mês |

### 10.3 Operação

- **Configura → simula → aplica.** A simulação exibe valor proposto, valor atual e delta por célula, com o total do impacto.
- A aplicação grava sempre **dentro de um cenário**, marcando a origem `DRIVER` e o driver de origem em cada célula.
- **Trava de célula:** célula digitada pode ser travada contra sobrescrita por driver. O reprocessamento respeita as travas e informa quantas células pulou.
- **Reprocessamento seletivo:** rodar de novo só o que veio de regra, preservando digitação e importação.
- Histórico de execuções — driver, cenário, data, usuário, linhas afetadas, valor total — com **desfazer** da última execução.

## 11. Captação — as três formas de orçar

As três convivem: o driver gera a base, a planilha sobrepõe, a digitação ajusta.

### 11.1 Grade de digitação

Planilha inteligente dentro do sistema, o coração da coleta:

- Layout **dimensão × 12 meses**, com colunas de total do ano, realizado do ano anterior e orçado do cenário de comparação lado a lado.
- Edição célula a célula com **fórmulas** (soma, percentual, referência a outra célula), copiar/colar de e para Excel, preenchimento em série e distribuição de um total anual pelos meses segundo uma curva.
- **Cor por origem** da célula (digitada, driver, planilha, rateio, replicada) e ícone de célula travada.
- Totalizadores automáticos por natureza, CR, gerência, diretoria e empresa, recalculados ao vivo.
- **Comentário por célula** e anexo por linha — a justificativa do número fica junto do número.
- Salvamento incremental, indicação de alterações não salvas e **bloqueio otimista** (aviso quando outro usuário alterou a mesma célula).
- **Filtros e visões salvas** por usuário.

### 11.2 Importação por planilha

- **Template oficial** para download, já com as dimensões e os meses do ciclo.
- **Upload livre** com mapeamento de colunas, salvo como perfil reutilizável.
- **Preview antes de gravar:** linhas válidas, linhas com erro (com o motivo em cada uma) e o **delta contra o cenário atual**.
- **Validações:** dimensão inexistente ou inativa, CR sem de-para vigente no período, natureza sintética recebendo valor, período fora do ciclo, duplicidade de chave, total que não fecha, moeda divergente.
- **Modos de carga:** substituir, complementar (só o que não existe) ou somar.
- **Log e rollback:** quem subiu, quando, qual arquivo, quantas linhas — com desfazer da carga inteira.

### 11.3 Replicação

Copiar de outro cenário, de outro ano, de outro CR ou de outra empresa, com fator de ajuste e seleção de naturezas.

## 12. Orçamento de Receita

- Projeção por **empresa × projeto × natureza de receita × mês**, e opcionalmente por produto/serviço, cliente, canal ou região, quando a dimensão existir no ERP.
- Duas formas de orçar: **valor direto** ou **quantidade × preço médio**, com premissas de preço e volume mês a mês.
- **Curvas de sazonalidade** nomeadas e reutilizáveis, aplicáveis a um total anual.
- **Deduções de receita** calculadas por driver percentual (impostos, descontos, cancelamentos), respeitando a regra de que vivem dentro do bloco de receita.
- A receita líquida resultante alimenta automaticamente os drivers de "% sobre meta".
- Comparação com o realizado do ano anterior e com o pipeline comercial, quando disponível.

## 13. Orçamento de Pessoal

- **Quadro atual:** carga dos colaboradores ativos com centro de custo, cargo, salário e encargos, direto do **Senior**, convertido para CR pelo de-para do item 5.4.
- **Planejamento de vagas:** headcount por área e mês, com data prevista de admissão, salário previsto e ramp de custo mês a mês.
- **Movimentações:** promoções, méritos e reajustes por data, com base salarial e encargos aplicados por premissa.
- **Turnover:** desligamentos previstos e provisão de custo rescisório.
- **Encargos e benefícios** calculados por premissa percentual ou valor per capita, por empresa e por faixa.
- **Provisões** de férias e 13º distribuídas pelos meses segundo a regra parametrizada.
- Resultado consolidado em naturezas de folha, entrando no DRE como qualquer outra despesa.
- **Realizado de folha automático:** rubricas do Senior mapeadas para natureza, agregadas por CR e mês, entrando no DRE e no confronto orçado × realizado como qualquer outra despesa — sem digitação e sem planilha.
- **Jornada:** banco de horas, horas extras, faltas e afastamentos vindos do ponto (Senior), com impacto financeiro estimado e absenteísmo por área, alimentando a reprojeção do custo de pessoal.
- **Movimentações realizadas** (admissões, desligamentos, promoções) confrontadas com as planejadas, mês a mês.
- Visão de **headcount orçado × realizado** por mês e área, que também alimenta os drivers per capita.

## 14. Orçamento de Custeio e Base Zero (OBZ)

- Orçamento de despesas por **CR × natureza × mês**, com as três formas de captação do item 11.
- **Modo Base Zero:** a célula não parte do histórico; exige **memória de cálculo** — itens com descrição, quantidade, valor unitário, frequência e mês de incidência, cuja soma forma o valor da célula.
- **Limite/teto** por CR, natureza ou pacote, definido pelo corporativo antes da coleta; a grade sinaliza estouro e o workflow pode bloquear o envio.
- **Comparativo com o histórico** ao lado da célula (realizado dos 2 últimos anos e média mensal), mesmo em OBZ, como referência.
- Classificação de despesa **fixa / variável / discricionária**, usada nos cortes e nas simulações.

## 15. Orçamento Matricial

**Objetivo:** dar dupla responsabilidade sobre o gasto — quem consome e quem é dono do pacote.

- **Matriz Pacote × Entidade** (CR, gerência ou empresa), com o de-para Natureza ↔ Pacote como base.
- Dois donos por célula: o **dono da entidade** (consome) e o **dono do pacote** (normatiza). Ambos entram no workflow.
- **Metas de redução** por pacote, distribuídas entre entidades.
- **Comparação entre pares:** ranking de entidades no mesmo pacote, com valor absoluto e valor relativo a um direcionador (por colaborador, por m², por unidade produzida).
- Acompanhamento mensal do pacote: orçado, realizado, desvio e ranking de entidades fora da meta.

## 16. Investimentos (CAPEX)

- Cadastro de **projetos de investimento** com valor total, justificativa, classificação, prioridade e cronograma de desembolso mês a mês.
- **Alçada de aprovação** por faixa de valor, independente do fluxo de custeio.
- **Depreciação projetada:** a partir da data de entrada em operação e da vida útil, o sistema gera automaticamente a despesa de depreciação nos períodos seguintes, que entra no DRE projetado.
- Acompanhamento **orçado × realizado** do investimento, por projeto e por mês.
- O investimento aparece **fora do resultado** no DRE e **dentro** do fluxo de caixa projetado.

## 17. Rateios e alocação

**Objetivo:** distribuir custos de áreas de apoio sobre as áreas de negócio.

- **Regras de rateio** cadastradas com origem (CR/natureza/pacote), destino (lista de CRs ou critério), base de rateio e vigência.
- **Bases de rateio:** percentual fixo, headcount, receita, área, quantidade de um direcionador cadastrado, ou realizado de outra natureza.
- **Rateio em cascata** (o rateio de A para B entra na base do rateio de B para C), com detecção e bloqueio de referência circular.
- Aplicável a **orçado, realizado ou ambos**, sempre gerando linha nova marcada com origem `RATEIO` e chave da regra — o valor original permanece intacto.
- **Simulação antes de aplicar**, com o antes/depois por CR.
- Relatórios com alternador **antes do rateio / depois do rateio**.

## 18. Workflow de aprovação

**Objetivo:** transformar a coleta em processo com dono, prazo e estado.

- **Desenho de fluxo** por ciclo: etapas em sequência ou paralelo, com papel responsável por etapa (responsável do CR → gerente → diretor → controladoria → diretoria).
- **Alçada por valor:** faixas que determinam até onde o orçamento precisa subir.
- **Estados por célula ou por bloco:** pendente, enviado, em análise, aprovado, devolvido.
- **Devolução com motivo obrigatório**, que volta a célula para edição e notifica o responsável.
- **Aprovação em lote** com filtro (tudo de um CR, tudo abaixo de um valor, tudo sem desvio).
- **Delegação** temporária de aprovação com vigência (férias, ausência).
- **Painel do ciclo:** quem já enviou, quem está atrasado, onde está travado, quanto falta em valor e em número de CRs — com cobrança por e-mail em um clique.
- **Prazos e lembretes automáticos:** aviso antes do prazo, alerta no vencimento, escalonamento para o superior.
- Histórico completo e inalterável: quem aprovou o quê, quando, com qual comentário.

## 19. Publicação no ERP

**Objetivo:** fazer o orçamento aprovado virar parâmetro operante, não um relatório.

- Publicação do cenário congelado como **meta/orçamento no ERP**, no grão empresa × CR × projeto × natureza × mês, com o tipo de cenário identificado no destino.
- **Marcação de origem** no registro publicado, para distinguir o que veio do AllStrategy do que foi digitado direto no ERP.
- **Controle de reenvio:** cada linha sabe se já foi publicada; republicar atualiza em vez de duplicar.
- **Tela de auditoria de publicação** — "o que aprovei × o que entrou no ERP": confronto linha a linha por versão de orçamento, apontando linhas faltantes, sobrando e divergentes em valor, com ação de reprocessar as divergências.
- **Despublicação** de uma versão substituída, com registro.
- Execução manual ou agendada, com log de cada remessa (quantas linhas, tempo, erros e o retorno do ERP).

## 20. Controle de verba

**Objetivo:** o orçamento governando a execução.

- **Saldo de verba** calculado por célula (empresa × CR × natureza × período): `orçado − realizado − comprometido`.
- **Consumo por comprometimento:** requisição ou pedido aprovado reserva verba; cancelamento devolve; realização converte comprometido em realizado.
- **Política por natureza ou CR:** apenas informar, alertar, exigir aprovação de exceção, ou bloquear.
- **Tolerância** configurável (percentual ou valor) e **acumulação**: verba mensal, trimestral ou anual; com ou sem transporte de saldo não usado para o mês seguinte.
- **Remanejamento de verba** entre células, com origem, destino, motivo e alçada própria — e trilha completa.
- **Aprovação de exceção** (estouro autorizado) com justificativa e prazo.
- **Consulta de saldo** disponível ao usuário no momento da requisição, com o extrato de consumo da célula.

## 21. Realizado e conciliação

**Objetivo:** ter o realizado certo, no período certo, com o sinal certo — automaticamente.

- **Carga agendada** do realizado a partir das duas origens, no grão de lançamento: empresa, CR, projeto, natureza, parceiro, documento, histórico, data, valor com sinal, **sistema de origem** (`SANKHYA` / `SENIOR`) e **módulo de origem** (contabilidade, financeiro, faturamento, compras, folha, ponto, customizações).
- **Camada agregada** derivada da analítica, para desempenho de consulta, com garantia — validada a cada carga — de que a soma da analítica reproduz o agregado.
- **Competência gerencial:** todo lançamento tem o período gerencial (`ANO`/`MES`) além da data do documento. Lançamentos remetidos para outro período guardam o período de origem e o motivo, e aparecem marcados no drill.
- **Chave de rastreabilidade** por lançamento, apontando o registro de origem no ERP — é o que permite o drill até o documento e a conferência ERP × AllStrategy.
- **Badge de frescor da carga** em toda tela, **por origem**: quando foi a última carga bem-sucedida de cada sistema, com sinalização por idade (verde/âmbar/vermelho) e identificação de qual origem está defasada.
- **Tela de conciliação origem × AllStrategy:** totais por empresa/período em cada sistema de origem e no AllStrategy, diferença, e a lista dos lançamentos que existem de um lado e não do outro — incluindo o confronto de folha Senior × Sankhya × AllStrategy do item 5.4.
- **Regra antidupla contagem:** por natureza, apenas uma origem é considerada realizado; a concorrente fica visível e marcada, fora da totalização.
- **Lançamentos de ajuste gerencial** manuais, com natureza, motivo e aprovação — sempre segregados e identificáveis nos relatórios.
- **Tratamento de granularidade:** quando o realizado chega sem CR ou sem projeto (típico de receita e CMV), o sistema **não inventa** rateio: mantém a chave incompleta, compara com o orçado no menor grão comum (empresa × natureza) e **avisa na tela** que aquela comparação tem granularidade reduzida — sem bloquear a análise.

## 22. Análise de desvios e justificativas

- Cálculo de desvio **absoluto e percentual**, no mês, no acumulado do ano (YTD) e no fechamento projetado.
- **Semáforo** por faixa de tolerância, configurável por natureza e por grupo, com a leitura correta de sinal: despesa acima do orçado é ruim, receita acima é boa.
- **Justificativa obrigatória** acima de um limite (percentual e/ou valor), com categoria (volume, preço, prazo, evento não previsto, erro de orçamento), texto e anexo.
- **Plano de ação** vinculado ao desvio: o que será feito, responsável, prazo e status.
- **Painel de pendências de justificativa** por responsável, com cobrança automática no fechamento mensal.
- **Drill completo** do desvio: grupo → natureza → CR → lançamento → documento de origem.
- **Top desvios** do período por natureza, CR e responsável.

## 23. Forecast e reprojeção

- **Forecast automático:** realizado acumulado até o mês de corte + orçado dos meses restantes, recalculado a cada carga.
- **Projeção run-rate** como segunda leitura: (realizado acumulado ÷ meses com realizado) × meses do ano.
- **Reprojeção manual:** o responsável ajusta os meses futuros sobre a base automática, com justificativa; os meses já realizados ficam bloqueados.
- **Congelamento mensal do forecast**, gerando série histórica — e a **análise de acurácia**: o quanto cada forecast acertou o fechamento, por responsável e por natureza.
- Comparação **Budget × Revisado × Forecast × Realizado** na mesma tela.
- Alerta automático quando o forecast projeta estouro do orçado anual.

## 24. Demonstrativos projetados

### 24.1 DRE gerencial
Orçado × Realizado × Desvio × Forecast, por mês e acumulado; drill por toda a hierarquia de natureza; AV/AH; comparação com ano anterior; comparação entre cenários; quebra por qualquer dimensão.

### 24.2 Fluxo de caixa projetado
Conversão do regime de competência para caixa por **prazo médio parametrizado** (recebimento por natureza/cliente, pagamento por natureza/fornecedor); entrada de investimentos e de itens não-DRE (empréstimos, aportes, impostos diferidos); saldo inicial, movimento e saldo final por mês; alerta de saldo negativo projetado.

### 24.3 Balanço projetado
Projeção das principais contas patrimoniais a partir do DRE e do fluxo (clientes, estoques, fornecedores, imobilizado com depreciação, patrimônio líquido), com fechamento ativo = passivo e apontamento da diferença quando não fecha.

### 24.4 Indicadores
EBITDA, margens, liquidez, endividamento, ROI, e indicadores customizados definidos por fórmula sobre linhas do DRE.

## 25. Consolidação

- **Consolidação multi-empresa** por grupo societário, com estrutura de participação e percentual.
- **Multi-moeda:** moeda funcional por empresa, moeda de apresentação por relatório, tabela de câmbio por mês (orçado e realizado), e escolha do critério de conversão (taxa de fechamento, taxa média, taxa histórica).
- **Eliminações intercompany:** marcação de naturezas e parceiros intragrupo, relatório de conferência dos dois lados e eliminação automática no consolidado.
- **Consolidação por qualquer eixo** — grupo, diretoria, projeto, pacote — e não apenas por empresa.
- Consolidado sempre reconciliável: da linha consolidada até o lançamento de origem.

## 26. Relatórios, dashboards e análise

- **Dashboard executivo:** KPIs de Orçado, Realizado, Desvio % e Forecast do ano; série mensal Orçado × Realizado × Forecast; top desvios; quebra por diretoria, CR e natureza; barras de execução com a leitura correta de sinal para receita e despesa.
- **Cross-filter bidirecional** e **drill-down** por toda a cadeia Empresa → Diretoria → Gerência → CR → Projeto → Natureza → Lançamento → Documento.
- **Filtros globais** persistentes: período, cenário, empresa, CR, projeto, natureza (múltipla), responsável, origem, moeda, visão de estrutura.
- **Relatórios padrão:** DRE, fluxo, balanço, execução por CR, execução por pacote, carteira do responsável, pendências do ciclo, auditoria de publicação, acurácia de forecast.
- **Construtor de relatórios** para o usuário montar visão própria com as dimensões e medidas disponíveis, salvar e compartilhar.
- **Exportação** XLSX (abas de resumo, por natureza e detalhado), CSV e PDF, mantendo os filtros aplicados e carimbando cenário, visão de estrutura e data/hora da extração.
- **Envio agendado** de relatório por e-mail.
- **Agente de consulta em linguagem natural** sobre os dados do orçamento, com glossário de negócio curado e regras de leitura (cenário, sinal, competência, de-para), respondendo apenas pelo que o perfil do usuário pode ver.

### 26.1 Segurança e auditoria (transversal)

- **Perfis** com permissão por módulo e por ação (ver, editar, enviar, aprovar, publicar, administrar).
- **Segurança de dados por dimensão:** o usuário vê e edita apenas as empresas, CRs, projetos e naturezas atribuídos a ele — regra aplicada na consulta, não na tela.
- **Segurança por cenário e por estado:** editar só cenário aberto; ver cenário congelado; simulação visível só ao dono.
- **Trilha de auditoria** de toda alteração de valor, de cadastro, de permissão e de parâmetro, inalterável e exportável.
- **Log de acesso e de extração** de dados.
- **Credenciais de integração** armazenadas cifradas e nunca exibidas em tela, em arquivo de configuração versionado ou em log.

### 26.2 Administração (transversal)

Parâmetros gerais, calendário do ciclo, **fechamento de período** (trava o mês contra alteração de realizado e de forecast), gestão de agendamentos e monitoramento das cargas, central de notificações (e-mail e in-app, com preferências por usuário) e painel de saúde do sistema (última carga por fonte, filas, erros, tempo de resposta).

## 27. Playground analítico

**Objetivo:** dar ao usuário de negócio a capacidade de construir seus próprios estudos, dashboards e análises sobre os dados do orçamento e do realizado — sem TI, sem exportar para o Excel e sem criar uma segunda verdade.

Este é o módulo que leva o produto além de uma ferramenta orçamentária convencional. A regra que o governa: **o Playground consome o mesmo modelo semântico dos relatórios oficiais**. Não existe extração paralela, nem base própria, nem regra de cálculo duplicada.

### 27.1 Modelo semântico publicado

A camada que o usuário enxerga — e a única que ele enxerga.

- **Dimensões publicadas** com nome de negócio (não nome de coluna): Empresa, Diretoria, Gerência, Centro de Resultado, Projeto, Natureza (com toda a hierarquia), Pacote, Parceiro, Responsável, Cenário, Origem, Colaborador, Cargo, Rubrica, Período (ano, trimestre, mês, YTD).
- **Medidas publicadas** já com a regra embutida: Orçado, Realizado, Forecast, Comprometido, Verba disponível, Desvio (absoluto e %), Desvio YTD, AV, AH, Headcount, Custo médio por colaborador, e todos os subtotais do DRE (Receita Líquida, Lucro Bruto, EBITDA, LAIR, Resultado Líquido).
- **Medidas calculadas pelo usuário:** fórmula sobre medidas existentes, com validação de sintaxe, prévia do resultado e possibilidade de promover a medida para a biblioteca corporativa após curadoria.
- **Descrição de negócio em cada item** (o que é, como é calculado, de onde vem, qual a pegadinha) — visível ao passar o mouse. É o glossário do item 3 embutido na ferramenta.
- **Herança total das regras:** sinal, competência gerencial, de-para com vigência, visão de estrutura (vigente/da época), antidupla contagem e menor grão comum valem no Playground exatamente como no DRE oficial.

### 27.2 Construtor de estudos

- **Montagem por arrastar e soltar:** o usuário escolhe dimensões (linhas/colunas), medidas, filtros e período, sem escrever consulta.
- **Tabela dinâmica** com expansão hierárquica, subtotais, ordenação, top N, ocultar zerados e formatação condicional.
- **Visualizações:** tabela, linha, barra, barra empilhada, combinado (barra + linha), pizza, cascata (waterfall, para ponte de resultado), dispersão, mapa de calor, medidor de KPI, indicador com variação e sparkline.
- **Cálculos de análise** aplicáveis a qualquer medida: variação período a período, acumulado, média móvel, participação no total, ranking, YTD/YTG, comparação com ano anterior, projeção linear.
- **Drill-down e drill-through:** descer a hierarquia e, no fim, chegar ao lançamento e ao documento de origem no ERP — o mesmo drill dos relatórios oficiais.
- **Cross-filter** entre visualizações do mesmo estudo.
- **Parâmetros de estudo:** o autor define filtros que o leitor pode trocar (período, empresa, cenário) sem precisar editar o estudo.
- **Modo comparação:** qualquer estudo aceita comparar cenários, anos ou estruturas lado a lado.
- **Anotações:** comentário fixado em um ponto do gráfico ou em uma célula, com autor e data — a análise fica junto do número.

### 27.3 Dashboards do usuário

- **Tela livre** com múltiplos blocos (visualizações, KPIs, texto, imagem), redimensionáveis e posicionáveis em grade.
- **Filtros globais do dashboard**, aplicáveis a todos os blocos ou a blocos selecionados.
- **Blocos de fonte mista** no mesmo dashboard: orçamento, folha (Senior), compras (Sankhya) e indicadores calculados.
- **Atualização** ao abrir, por agendamento ou sob demanda, com indicação de quando o dado foi lido e de qual origem.
- **Alertas:** condição sobre uma medida (ex.: desvio acima de X%) que dispara notificação por e-mail ou in-app, com periodicidade configurável.
- **Modo apresentação** (tela cheia, rotação automática de painéis) para reunião e para TV de área.

### 27.4 Biblioteca, compartilhamento e reuso

- **Espaços:** pessoal (rascunhos do usuário), da área (visível ao time) e corporativo (curado pela controladoria).
- **Compartilhamento** por usuário, papel ou área, com permissão de ver / duplicar / editar.
- **Publicação com curadoria:** um estudo só sobe para o espaço corporativo após revisão da controladoria, que o marca como **oficial**. Estudo não oficial carrega selo visível de "análise do usuário", para nunca ser confundido com número oficial.
- **Duplicar e adaptar:** partir de um estudo existente é o caminho normal de criação.
- **Versionamento** do estudo, com histórico e restauração.
- **Catálogo** com busca por nome, dimensão usada, autor e área; e indicação de estudos mais usados.
- **Estudos-modelo** entregues prontos como ponto de partida: execução por CR, ponte de resultado (waterfall) orçado → realizado, evolução de headcount e custo médio, ranking de desvios, análise de pacote, sazonalidade de receita, aging de justificativas.

### 27.5 Exportação e distribuição

- Exportação de qualquer estudo em XLSX (com os dados por trás da visualização), CSV, PDF e imagem, sempre carimbada com filtros, cenário, visão de estrutura, origem e data/hora.
- **Envio agendado** por e-mail, com destinatários e periodicidade.
- **Link de leitura** para usuários com permissão, respeitando a segurança por dimensão de quem abre — nunca a de quem criou.

### 27.6 Consulta em linguagem natural

- Pergunta em português sobre o modelo semântico, com a resposta acompanhada de **como o número foi obtido** (dimensões, filtros, medida e período usados) e do estudo correspondente, que o usuário pode abrir e ajustar.
- Respeita integralmente a segurança por dimensão e o cenário de leitura do usuário.
- Glossário de negócio curado e bateria de perguntas-padrão validadas, para o agente não responder com confiança um número errado.
- Quando a pergunta é ambígua ou o dado tem granularidade reduzida, o agente **diz isso** em vez de escolher sozinho.

### 27.7 Governança e segurança do Playground

| Risco | Como o módulo trata |
|---|---|
| **Segunda verdade** | O Playground não tem base própria; lê o modelo semântico oficial, com as mesmas regras. Estudo do usuário carrega selo; só o curado é oficial |
| **Vazamento de dado** | A segurança por dimensão é aplicada na consulta. Um estudo compartilhado mostra a cada leitor apenas o que ele pode ver — inclusive na exportação e no envio agendado |
| **Consulta pesada derrubando o sistema** | Limite de linhas e de tempo por consulta, fila separada da operação, cache por estudo e aviso ao usuário quando a consulta é cara, com sugestão de reduzir o grão |
| **Proliferação de estudos** | Catálogo com uso, marcação de obsoleto, arquivamento automático de estudo sem acesso por período configurável |
| **Número errado circulando** | Toda exportação carimbada; todo estudo rastreável até o lançamento; log de quem extraiu o quê e quando |

---

# PARTE III — FUNDAÇÃO TÉCNICA

## 28. Modelo de dados

```
-- DIMENSÕES ESPELHADAS DO ERP (somente leitura)
DIM_EMPRESA        (empresa_id, codigo_erp, cnpj, razao_social, moeda_funcional, ativo)
DIM_CR             (cr_id, codigo_erp, descricao, ativo)
DIM_PROJETO        (projeto_id, codigo_erp, nome, empresa_id, dt_inicio, dt_fim, ativo)
DIM_NATUREZA       (natureza_id, codigo_erp, descricao, natureza_pai_id, nivel,
                    analitica, tipo, ativo)
DIM_PARCEIRO       (parceiro_id, codigo_erp, nome, tipo, intercompany, ativo)
DIM_USUARIO        (usuario_id, codigo_erp, nome, email, ativo)

-- DIMENSÕES PRÓPRIAS
DIM_DIRETORIA      (diretoria_id, nome, ativo)
DIM_GERENCIA       (gerencia_id, nome, diretoria_id, ativo)
DIM_PACOTE         (pacote_id, nome, dono_usuario_id, ativo)
DIM_GRUPO_DRE      (grupo_id, codigo, nome, ordem, tipo_resultado, entra_no_resultado)
DIM_CICLO          (ciclo_id, ano, nome, status, dt_abertura, dt_limite_coleta,
                    dt_limite_aprovacao, responsavel_id)
DIM_CENARIO        (cenario_id, ciclo_id, tipo, nome, versao, status,
                    dt_congelamento, usuario_congelamento, hash_totais, justificativa)
DIM_MOEDA          (moeda_id, codigo, nome)
TAXA_CAMBIO        (moeda_id, periodo, cenario_id, taxa_fechamento, taxa_media)

-- DE-PARAS COM VIGÊNCIA (a hierarquia nunca é gravada no fato)
MAP_CR_GERENCIA    (cr_id, gerencia_id, dt_inicio, dt_fim, perc_rateio)
MAP_CR_RESPONSAVEL (cr_id, usuario_id, dt_inicio, dt_fim)
MAP_NAT_PACOTE     (natureza_id, pacote_id, dt_inicio, dt_fim)
MAP_NAT_GRUPO      (natureza_id, grupo_id, visao_dre, dt_inicio, dt_fim)
MAP_CCUSTO_CR      (ccusto_senior, cr_id, perc_rateio, dt_inicio, dt_fim)
MAP_RUBRICA_NAT    (rubrica_senior, natureza_id, tipo_rubrica, dt_inicio, dt_fim)
PARAM_ORIGEM_REAL  (natureza_id, sistema_origem_oficial, dt_inicio, dt_fim)

-- DIMENSÕES DE PESSOAS (origem Senior)
DIM_COLABORADOR    (colaborador_id, matricula, nome, cargo_id, ccusto_senior,
                    dt_admissao, dt_desligamento, situacao)
DIM_CARGO          (cargo_id, codigo, descricao, faixa_min, faixa_max)
DIM_RUBRICA        (rubrica_id, codigo, descricao, tipo, incide_encargo)
FATO_FOLHA         (fato_id, colaborador_id, rubrica_id, ccusto_senior, cr_id,
                    natureza_id, periodo, valor, origem, dt_carga)
FATO_JORNADA       (fato_id, colaborador_id, periodo, banco_horas, horas_extras,
                    faltas, afastamentos, valor_estimado, dt_carga)

-- PLANEJAMENTO
PREMISSA           (premissa_id, nome, tipo, escopo, empresa_id, cr_id, projeto_id,
                    natureza_id, cenario_id, periodo, valor, dt_inicio, dt_fim)
META_RECEITA       (empresa_id, projeto_id, cenario_id, periodo, valor_meta)
DRIVER             (driver_id, nome, tipo, nivel, empresa_id, cr_id, projeto_id,
                    natureza_id, pacote_id, parametros_json, prioridade,
                    dt_inicio, dt_fim, ativo)
DRIVER_EXECUCAO    (execucao_id, driver_id, cenario_id, dt, usuario_id,
                    linhas_afetadas, valor_total, estornada)
REGRA_RATEIO       (regra_id, nome, origem_json, destino_json, base_rateio,
                    ordem_cascata, dt_inicio, dt_fim, ativo)
MEMORIA_CALCULO    (memoria_id, fato_orc_id, descricao, quantidade, valor_unitario,
                    frequencia, meses_incidencia)

-- PESSOAL
PLAN_HEADCOUNT     (plan_id, cenario_id, cr_id, cargo, qtd, periodo, salario_previsto,
                    dt_admissao_prevista, status)
PLAN_MOVIMENTACAO  (mov_id, cenario_id, colaborador_id, tipo, periodo,
                    valor_base, perc_encargos)

-- INVESTIMENTOS
PROJ_INVESTIMENTO  (inv_id, cenario_id, nome, empresa_id, cr_id, valor_total,
                    classificacao, prioridade, status, dt_entrada_operacao,
                    vida_util_meses)
INV_CRONOGRAMA     (inv_id, periodo, valor_desembolso)

-- FATOS
FATO_ORCADO        (fato_id, cenario_id, empresa_id, cr_id, projeto_id, natureza_id,
                    pacote_id, periodo, moeda_id, valor, origem, driver_id,
                    travado, usuario_id, dt_alteracao)
FATO_REALIZADO     (fato_id, empresa_id, cr_id, projeto_id, natureza_id, parceiro_id,
                    periodo, periodo_origem, ajustado, dt_lancamento, moeda_id, valor,
                    origem, documento, historico, chave_origem, dt_carga)
FATO_AGREGADO      (empresa_id, cr_id, projeto_id, natureza_id, periodo,
                    vlr_orcado, vlr_realizado, vlr_forecast, vlr_comprometido, dt_carga)
FATO_COMPROMETIDO  (fato_id, empresa_id, cr_id, projeto_id, natureza_id, periodo,
                    documento, valor, status, dt_lancamento)

-- GOVERNANÇA
WF_ETAPA           (etapa_id, ciclo_id, ordem, nome, papel_id, alcada_valor, paralela)
WF_INSTANCIA       (instancia_id, cenario_id, cr_id, etapa_atual_id, status)
WF_MOVIMENTO       (mov_id, instancia_id, etapa_id, usuario_id, acao, dt, comentario)
JUSTIFICATIVA      (just_id, cenario_id, empresa_id, cr_id, natureza_id, periodo,
                    categoria, texto, usuario_id, dt, anexo)
PLANO_ACAO         (acao_id, just_id, descricao, responsavel_id, prazo, status)
REMANEJAMENTO      (rem_id, cenario_id, origem_json, destino_json, valor,
                    motivo, usuario_id, dt, status)
PUBLICACAO_ERP     (pub_id, cenario_id, dt, usuario_id, linhas_enviadas,
                    linhas_erro, status)
PUBLICACAO_LINHA   (pub_id, fato_id, chave_erp, status, mensagem)

-- PLAYGROUND
SEM_DIMENSAO       (dim_id, nome_negocio, entidade, campo, hierarquia, descricao,
                    visivel, ordem)
SEM_MEDIDA         (medida_id, nome_negocio, expressao, formato, agregacao,
                    descricao, oficial, visivel)
ESTUDO             (estudo_id, nome, espaco, autor_id, definicao_json, versao,
                    oficial, dt_criacao, dt_alteracao, arquivado)
ESTUDO_VERSAO      (versao_id, estudo_id, definicao_json, autor_id, dt, comentario)
ESTUDO_COMPART     (estudo_id, principal_tipo, principal_id, permissao)
DASHBOARD          (dash_id, nome, espaco, autor_id, layout_json, versao, oficial)
DASH_BLOCO         (bloco_id, dash_id, estudo_id, tipo_visual, posicao_json,
                    filtros_json)
ALERTA             (alerta_id, estudo_id, condicao_json, periodicidade,
                    destinatarios_json, ativo)
ANOTACAO           (anot_id, estudo_id, ancora_json, texto, autor_id, dt)
LOG_CONSULTA       (cons_id, estudo_id, usuario_id, dt, linhas, tempo_ms, cache_hit)

-- TRANSVERSAL
LOG_CELULA         (log_id, fato_id, cenario_id, valor_anterior, valor_novo,
                    origem, usuario_id, dt)
AUDITORIA          (audit_id, entidade, chave, acao, antes_json, depois_json,
                    usuario_id, dt)
CARGA_LOG          (carga_id, fonte, dt_inicio, dt_fim, status, registros, mensagem)
IMPORTACAO         (imp_id, cenario_id, arquivo, perfil_id, modo, linhas_ok,
                    linhas_erro, usuario_id, dt, estornada)
```

**Regras estruturais do modelo:**

1. Nenhum fato grava gerência, diretoria, pacote ou grupo de DRE — todos resolvidos por de-para em tempo de consulta, com a data de referência do período.
2. `FATO_AGREGADO` é derivado, nunca fonte: reconstruível a partir dos fatos analíticos, e essa identidade é validada a cada carga.
3. `FATO_ORCADO` sempre carrega `cenario_id`. Não existe valor orçado fora de cenário.
4. Natureza sintética nunca recebe valor: o fato só aceita natureza analítica.
5. `periodo` é sempre o período gerencial (ano/mês). `dt_lancamento` existe para auditoria e drill, nunca para agrupar.
6. Todo fato carrega o **sistema de origem**. `PARAM_ORIGEM_REAL` define, por natureza, qual origem é a oficial do realizado — a concorrente fica gravada e marcada, fora da totalização.
7. `FATO_FOLHA` guarda o centro de custo do Senior **e** o CR resolvido pelo de-para, para que a conciliação dos dois lados seja possível sem recalcular.
8. O Playground não tem tabela de fato própria: `ESTUDO` guarda apenas a **definição** da análise, nunca o resultado. O dado sempre vem do modelo semântico no momento da consulta.

## 29. Regras transversais

| Tema | Regra |
|---|---|
| **Sinal** | Definido na carga: receita positiva, despesa negativa. Toda totalização é soma. Proibido `ABS()` ou inversão por linha |
| **Classificação R/D** | Vem do **grupo de DRE (N1)**, não do tipo da natureza. Fallback: natureza sem tipo herda o tipo do grupo |
| **Período** | `ano`/`mes` gerencial em toda análise; data do lançamento só em auditoria e drill |
| **Remessa de competência** | Lançamento reclassificado guarda o período de origem e é marcado como ajustado; o drill mostra o motivo |
| **Granularidade** | Orçado × realizado só se compara no menor grão comum. Quando o realizado não tem CR ou projeto, a tela avisa e compara no nível superior — sem bloquear e sem ratear por conta própria |
| **Arredondamento** | Valores em 2 casas; totais somam os valores arredondados (não arredondam a soma), para o total sempre bater com o detalhe |
| **Moeda** | O fato grava na moeda de origem; a conversão acontece na leitura, pela taxa do critério escolhido |
| **Fechamento** | Período fechado rejeita gravação de realizado, forecast e ajuste — inclusive por carga automática |
| **Célula vazia** | Distinta de zero: vazia = não orçado; zero = orçado como zero. A distinção aparece na grade e nos relatórios |
| **Concorrência** | Bloqueio otimista por célula: gravação sobre valor alterado por terceiro é rejeitada com aviso e comparação |

## 30. Integrações e multi-origem

| Sistema | Fonte | Direção | Conteúdo | Frequência |
|---|---|---|---|---|
| **Sankhya** | Cadastros | Entrada | Empresa, CR, projeto, natureza, parceiro, usuário | Diária |
| **Sankhya** | Contabilidade | Entrada | Lançamentos analíticos por natureza | Diária |
| **Sankhya** | Financeiro | Entrada | Baixas de título, por receita/despesa | Diária |
| **Sankhya** | Faturamento | Entrada | Notas e itens, para receita e CMV | Diária |
| **Sankhya** | Compras | Entrada | Requisições, pedidos e contratos — comprometido | Intradiária |
| **Sankhya** | Metas/orçamento | **Saída** | Publicação do cenário aprovado | Sob demanda |
| **Sankhya** | Consulta de verba | **Saída** | Saldo disponível para bloqueio na requisição | Tempo real |
| **Senior** | Cadastro de pessoas | Entrada | Colaborador, cargo, estrutura de RH, rubricas | Diária |
| **Senior** | Folha | Entrada | Realizado de folha por colaborador, rubrica e centro de custo | Mensal (e fechamentos parciais) |
| **Senior** | Encargos e provisões | Entrada | FGTS, INSS, férias, 13º — base do custo total | Mensal |
| **Senior** | Ponto / jornada | Entrada | Banco de horas, HE, faltas, afastamentos | Diária ou semanal |
| **Senior** | Movimentações | Entrada | Admissões, desligamentos, promoções, transferências | Diária |
| — | Planilhas | Entrada | Carga de orçamento, de-paras, premissas, histórico | Sob demanda |
| — | E-mail / notificação | Saída | Cobrança de ciclo, alertas do Playground, relatórios agendados | Contínua |

**Requisitos de toda integração:** autenticação por credencial de serviço armazenada cifrada; execução idempotente (reprocessar não duplica); janela de reprocessamento por período; log com contagem e erro por registro; alerta quando a carga falha ou não roda.

**Requisitos específicos de multi-origem:**

- Cada origem tem **agendamento, log e badge de frescor próprios**; a falha de uma não bloqueia a outra.
- **Reprocessamento por origem e por período**, sem afetar o que veio do outro sistema.
- O mecanismo de acesso ao Senior (API, base de réplica ou extração agendada) é definido na Fase de integração conforme o que o ambiente do cliente disponibiliza — o modelo funcional acima não muda em função disso.
- Mudança de estrutura no Senior (centro de custo novo, rubrica nova) gera **pendência de mapeamento**, não erro silencioso: a carga entra, o valor cai em "não classificado" e a controladoria é notificada.

## 31. Requisitos não funcionais

| Tema | Requisito |
|---|---|
| **Volume** | Dimensionado para 1M+ lançamentos analíticos por ano e 500k células orçadas por cenário |
| **Desempenho** | Dashboard e DRE abrem em até 5s; a grade de digitação responde à edição em até 300ms; a carga diária conclui em janela noturna |
| **Concorrência** | Coleta simultânea de dezenas de responsáveis sem perda de gravação |
| **Disponibilidade** | Disponível no horário comercial estendido; a carga não bloqueia consulta |
| **Rastreabilidade** | Toda alteração de valor auditável; todo número de relatório chega ao documento de origem |
| **Segurança** | Perfil por módulo e por dimensão; credenciais cifradas; log de acesso e de extração |
| **Usabilidade** | Operável por usuário de negócio sem treinamento técnico; grade com comportamento de planilha |
| **Exportação** | Todo relatório exportável carimbado com filtros, cenário, visão de estrutura, origem e data/hora |
| **Playground — isolamento** | Consulta de estudo roda em fila separada da operação: análise pesada não degrada coleta, carga nem publicação |
| **Playground — limites** | Teto de linhas e de tempo por consulta, com aviso ao usuário e sugestão de reduzir o grão antes de abortar |
| **Playground — cache** | Resultado de estudo cacheado por definição + filtros + perfil, invalidado a cada carga da origem envolvida |
| **Multi-origem** | Carga de uma origem não bloqueia a outra; período com origem faltante é marcado como parcial e reprocessado |

## 32. Construção na Mitra — implicações de plataforma

O sistema é construído na Mitra (MitraLab), com as telas embarcáveis no ERP. Isso impõe alguns pontos de projeto que valem como requisito:

| Ponto | Implicação |
|---|---|
| **Consultas cadastradas na plataforma** | Toda leitura passa por query nomeada com parâmetros de bind; nenhuma SQL é montada por concatenação de valor do usuário |
| **Variáveis de filtro** | Os filtros globais (item 26) são variáveis de plataforma; filtros de múltipla seleção chegam como lista e precisam de tratamento explícito |
| **Refresh de componente** | O evento de atualização pode disparar mais de uma vez após gravação em formulário — as telas de digitação precisam ser idempotentes no refresh e protegidas contra execução concorrente |
| **Embarque no ERP** | As telas rodam embarcadas com repasse do usuário logado; a identidade do usuário do ERP é a base da segurança por dimensão, não um login paralelo |
| **Camada agregada** | Dado o custo de consulta analítica sobre volume alto, o agregado é obrigatório para dashboard e DRE, com a analítica reservada ao drill |
| **Credenciais** | Tokens e segredos de integração — Sankhya e Senior — ficam em cofre/parametrização cifrada, nunca em documento, arquivo de projeto ou tela |
| **Playground sobre consulta nomeada** | O construtor de estudos gera a consulta a partir do modelo semântico e a executa com parâmetros de bind; o usuário nunca escreve nem influencia SQL diretamente |
| **Modelo semântico como contrato** | Dimensões e medidas publicadas são a única superfície exposta ao Playground — mudança de estrutura interna não quebra estudo do usuário |

---

# PARTE IV — EXECUÇÃO

## 33. Fases e horas

### Ciclo 1 — Orçamento operante ponta a ponta (620h)

| Fase | Entrega | Horas |
|---|---|---:|
| F0 | **Fundação** — modelo de dados, dimensões espelhadas, sincronização, segurança por perfil e por dimensão, parâmetros, auditoria base | 90h |
| F1 | **Estrutura e cenários** — árvore de naturezas, grupos e visões de DRE, de-paras com vigência, ciclos, cenários, congelamento, cópia e comparação | 70h |
| F2 | **Captação** — grade de digitação com fórmulas, origem por célula, travas e comentários; importação por planilha com preview, validação, modos e rollback; replicação | 80h |
| F3 | **Premissas e drivers** — premissas com vigência e cenário, metas de receita, motor de drivers com precedência, simulação, aplicação e desfazer | 60h |
| F8 | **Realizado** — cargas do ERP (contábil, financeiro, faturamento), camada agregada, competência gerencial e remessas, chave de rastreabilidade, badge de frescor, conciliação, ajustes gerenciais | 90h |
| F9 | **Forecast** — automático, run-rate, reprojeção manual, congelamento mensal, acurácia | 40h |
| F12 | **DRE gerencial** — demonstrativo completo com drill, AV/AH, comparação de cenários e de anos, quebra por dimensão | 80h |
| F14a | **Dashboard e exportação** — KPIs, séries, top desvios, cross-filter, drill até o documento, exportação XLSX | 40h |
| F17a | **Modelo semântico publicado** — dimensões e medidas de negócio, descrições, medidas calculadas, herança das regras e da segurança. Base comum dos relatórios oficiais e do Playground | 30h |
| F16a | **Carga inicial, testes e go-live** — migração do histórico, validação de totais, homologação, treinamento e implantação | 40h |
| | **Subtotal Ciclo 1** | **620h** |

### Ciclo 2 — Senior, planejamento especializado e governança (380h)

| Fase | Entrega | Horas |
|---|---|---:|
| F8b | **Integração Senior** — cadastro de pessoas, folha realizada por rubrica, encargos e provisões, ponto e jornada, movimentações; de-paras Centro de Custo ↔ CR e Rubrica ↔ Natureza; regra antidupla contagem e conciliação Senior × Sankhya × AllStrategy | 80h |
| F4 | **Receita e Pessoal** — receita por quantidade × preço, sazonalidade e deduções; quadro atual, vagas, movimentações, turnover, encargos e provisões | 90h |
| F5 | **Custeio, OBZ e Matricial** — memória de cálculo, tetos, classificação de despesa; matriz pacote × entidade, donos duplos, metas de redução, comparação entre pares | 90h |
| F6 | **Investimentos e Rateios** — CAPEX com cronograma, alçada e depreciação projetada; regras de rateio, bases, cascata, simulação e visão antes/depois | 60h |
| F7 | **Workflow de aprovação** — desenho de fluxo, alçadas, estados, devolução, lote, delegação, painel do ciclo, prazos e lembretes | 60h |
| | **Subtotal Ciclo 2** | **380h** |

### Ciclo 3 — Controle da execução, consolidação e Playground (420h)

| Fase | Entrega | Horas |
|---|---|---:|
| F10 | **Publicação no ERP e controle de verba** — publicação, auditoria "aprovado × publicado", saldo, comprometido, políticas, tolerância, remanejamento, exceção | 50h |
| F11 | **Desvios e justificativas** — semáforo, justificativa obrigatória, categorias, planos de ação, painel de pendências | 40h |
| F13 | **Consolidação** — multi-empresa, multi-moeda, eliminações intercompany, consolidação por qualquer eixo | 50h |
| F12b | **Fluxo de caixa e Balanço projetados** — conversão competência→caixa, itens não-DRE, saldo projetado, contas patrimoniais, indicadores | 50h |
| F17b | **Playground analítico** — construtor de estudos (60h), dashboards do usuário com alertas e modo apresentação (40h), biblioteca, compartilhamento, versionamento e governança (30h), exportação e envio agendado (20h), consulta em linguagem natural sobre o modelo semântico (20h) | 170h |
| F14b | **Estudos-modelo e curadoria** — biblioteca inicial de estudos oficiais prontos, glossário curado, bateria de perguntas validadas, treinamento dos usuários no Playground | 30h |
| F15 | **Notificações, agendador e saúde** — central de notificação, gestão de agendamentos, painel de saúde, log de acesso e extração | 30h |
| | **Subtotal Ciclo 3** | **420h** |

### Total

| Ciclo | Entrega | Horas | % |
|---|---|---:|---:|
| 1 | Orçamento operante ponta a ponta (Sankhya) | 620h | 44% |
| 2 | Senior, planejamento especializado e governança | 380h | 27% |
| 3 | Controle da execução, consolidação e Playground | 420h | 29% |
| | **TOTAL** | **1.420h** | **100%** |

## 34. Cronograma

| Mês | Atividade | Horas |
|---|---|---:|
| 1–2 | F0 + F1 — Fundação, estrutura e cenários | 160h |
| 3 | F2 + F3 — Captação, premissas e drivers | 140h |
| 4 | F8 + F9 — Realizado (Sankhya) e forecast | 130h |
| 5 | F12 + F14a + F17a + F16a — DRE, dashboard, modelo semântico e **go-live do Ciclo 1** | 190h |
| 6 | F8b + F4 (início) — **Integração Senior**, Receita e Pessoal | 140h |
| 7 | F4 (fim) + F5 — Pessoal, Custeio/OBZ e Matricial | 120h |
| 8 | F6 + F7 — CAPEX, rateios e workflow | 120h |
| 9 | F10 + F11 + F13 — Verba, desvios e consolidação | 140h |
| 10 | F12b + F15 + F17b (início) — Fluxo, balanço, notificações e **Playground** | 140h |
| 11 | F17b (fim) + F14b — Playground, estudos-modelo e curadoria | 140h |
| | **TOTAL** | **1.420h** |

**Três entregas com valor próprio, em datas distintas:**

- **Mês 5 — Ciclo 1 em produção.** Substitui a planilha: orçamento captado, realizado do Sankhya automático, DRE, forecast e dashboard.
- **Mês 7 — orçamento de pessoal integrado.** Com o Senior conectado, a folha deixa de ser digitada e passa a entrar no mesmo DRE.
- **Mês 11 — Playground liberado.** As áreas passam a construir as próprias análises sobre o modelo oficial.

O Playground fica no fim por dependência real: ele só vale a pena sobre um modelo semântico estável e alimentado pelas duas origens. Antecipá-lo entregaria uma ferramenta de análise sem dado completo para analisar.

## 35. Premissas

- **Os sistemas de origem são a fonte dos cadastros e do realizado.** Sankhya é mestre da estrutura financeira; Senior é mestre de pessoas e folha. O AllStrategy espelha, não cria cadastro paralelo e não edita cadastro em nenhum dos dois.
- **Um mestre por domínio.** Onde há sobreposição entre Sankhya e Senior, vale o de-para explícito com vigência, e a natureza tem uma única origem oficial de realizado.
- **O Playground não cria base própria.** Ele lê o modelo semântico oficial; estudo do usuário não vira fonte de número oficial sem curadoria.
- A única estrutura mantida fora do ERP é a organizacional própria (diretoria, gerência, pacote) e seus de-paras.
- **A chave do CR não muda.** Toda reorganização é tratada por de-para com vigência.
- O sinal do valor é definido na carga; nenhuma camada posterior inverte sinal.
- A classificação Receita/Despesa segue o **grupo de DRE**, não o tipo da natureza.
- Drivers, rateios e cargas **nunca gravam direto**: passam por simulação e são aplicados dentro de um cenário.
- Cenário congelado é imutável; correção nasce como nova revisão.
- O histórico migrado é o que o cliente fornecer; não há reconstrução de períodos sem dado de origem.
- As integrações de saída (publicação e consulta de verba) dependem dos serviços correspondentes estarem liberados no ERP.
- Ambiente de homologação com massa representativa disponível desde a Fase 0.

## 36. Dependências do cliente

| Item | O que providenciar | Bloqueia | Urgência |
|---|---|---|---|
| **Acesso ao Sankhya** | URL, usuário de serviço, credenciais e liberação dos serviços de leitura e gravação | Tudo | **Crítica** |
| **Acesso ao Senior** | Forma de acesso disponível (API, base de réplica ou extração agendada), credenciais e liberação dos módulos de folha, ponto e cadastro de pessoas | F8b, orçamento de pessoal | **Crítica** |
| **De-paras do Senior** | Centro de custo da folha ↔ CR, e rubrica ↔ natureza gerencial, com vigências | F8b | **Crítica** |
| **Definição da origem oficial por natureza** | Onde folha chega pelos dois sistemas, qual origem vale como realizado | F8b | Alta |
| **Curadoria do Playground** | Quem valida e publica estudo como oficial, e qual a política de espaços e compartilhamento | F17b | Média |
| **Plano de contas gerencial** | Estrutura de naturezas com a hierarquia e o grupo de DRE de cada N1 | F1 | **Crítica** |
| **Definição das origens do realizado** | Quais módulos do ERP compõem o realizado de cada natureza | F8 | **Crítica** |
| **Estrutura organizacional** | Diretorias, gerências e o de-para CR ↔ Gerência, com vigências conhecidas | F1 | **Crítica** |
| **Responsáveis por CR** | Quem responde por cada CR, com vigência | F2, F7 | Alta |
| **Planilhas atuais de orçamento** | Base do ciclo corrente, para carga inicial e validação | F2, F16 | Alta |
| **Regras de driver** | Quais naturezas são dirigidas por regra e a fórmula de cada uma | F3 | Alta |
| **Premissas e metas de receita** | Inflação, reajuste, encargos, câmbio, metas por empresa/mês | F3 | Alta |
| **Regras de competência** | Critérios de remessa de período e de ajuste gerencial | F8 | Alta |
| **Desenho do fluxo de aprovação** | Etapas, papéis, alçadas e prazos | F7 | Alta |
| **Política de verba** | Quais naturezas bloqueiam, tolerância, acumulação e alçada de exceção | F10 | Média |
| **Estrutura societária** | Participações, moedas e naturezas intercompany | F13 | Média |
| **Prazos médios** | Recebimento e pagamento por natureza, para o fluxo de caixa | F12b | Média |
| **Homologadores** | Usuários de negócio disponíveis para validar cada fase | Todas | Alta |

## 37. Fora de escopo

- **Execução transacional:** o sistema não emite nota, não paga título, não gera folha, não origina pedido de compra. Ele orça, controla e lê.
- **Contabilidade societária e fiscal:** apuração, obrigações acessórias, SPED e conciliação contábil permanecem no ERP.
- **Alteração de cadastro no Sankhya ou no Senior** (empresa, CR, projeto, natureza, parceiro, colaborador, rubrica) — leitura apenas.
- **Cálculo de folha:** o sistema lê o resultado da folha do Senior e projeta custo; não calcula folha, não gera evento, não substitui o RH.
- **Gestão de RH transacional** (recrutamento, avaliação, treinamento, e-social) — fora do produto.
- **Definição da política orçamentária:** o sistema executa as regras de driver, rateio, alçada e verba; definir quais são elas é do cliente.
- **Orçamento comercial detalhado por oportunidade** (CRM/pipeline): consumido como dado, não gerido aqui.
- **Planejamento de produção, MRP e capacidade fabril.**
- **BI corporativo de propósito geral:** o Playground analisa o modelo do orçamento e do realizado carregado pelo sistema. Ele **não** é uma ferramenta de BI aberta a qualquer fonte de dado da empresa: conectar bases externas ao Playground está fora do escopo.
- **Migração de histórico além do que for fornecido em base ou planilha.**
- **Integrações não listadas no item 30.**
- **Aplicativo móvel nativo** — as telas são responsivas, mas não há app.

## 38. Backlog — fora do fechamento

Itens tecnicamente prontos para entrar, disponíveis como aditivo ou fase seguinte.

| Item | Horas est. | Por que ficou fora |
|---|---:|---|
| Aprovação por app móvel dedicado | 40h | As telas responsivas atendem o fluxo; o app é conveniência |
| Orçamento plurianual (3–5 anos) e plano estratégico | 60h | O ciclo anual cobre a necessidade imediata |
| Simulação Monte Carlo e análise de sensibilidade | 40h | Depende de maturidade no uso dos cenários |
| Projeção de forecast por aprendizado de máquina | 60h | Exige série histórica consolidada — reavaliar após 2 ciclos completos |
| Publicação do modelo semântico para BI externo | 30h | O Playground atende o consumo interno; expor o modelo para fora é passo seguinte |
| Fontes externas no Playground (planilha e base de terceiros do usuário) | 50h | Abre a porta para a segunda verdade; só entra com política de governança definida |
| Narrativa automática de análise (comentário gerado sobre o estudo) | 30h | Depende de maturidade do Playground em uso real |
| Integração com outros sistemas de RH além do Senior | — | Avaliar caso a caso; o modelo multi-origem já está pronto para receber |
| Orçamento de impostos detalhado por regime | 50h | Hoje entra como natureza e driver percentual |
| Gestão de contratos com projeção automática no orçamento | 40h | O contrato entra hoje como valor orçado manual |
| Portal externo para fornecedores informarem reajuste | 30h | Não faz parte do ciclo interno |
| Versionamento da estrutura de DRE com comparação entre versões | 20h | A visão múltipla de DRE cobre o caso principal |
| | **~370h** | |

## 39. Critérios de aceite

O Ciclo 1 é aceito quando, com dados reais:

1. Os cadastros do ERP aparecem no sistema e a sincronização diária roda sem intervenção.
2. Um ciclo é aberto, coletado por pelo menos três responsáveis simultâneos e congelado, com log de célula completo.
3. A soma dos lançamentos analíticos reproduz exatamente o agregado, validado por confronto chave a chave.
4. O total do realizado por empresa e período bate com o ERP, e cada divergência é explicada pela tela de conciliação — não por ajuste manual.
5. O DRE fecha: a soma dos grupos reproduz os subtotais, e cada linha desce por drill até o documento de origem no ERP.
6. Um driver é configurado, simulado e aplicado, respeitando travas e precedência, e é desfeito sem resíduo.
7. Uma planilha é importada, o preview mostra o delta correto, a carga grava e o rollback devolve o cenário ao estado anterior.
8. O forecast recalcula a cada carga e a comparação Budget × Revisado × Forecast × Realizado abre na mesma tela.
9. Um usuário com perfil restrito vê apenas suas dimensões, em toda tela e em toda exportação.
10. O dashboard abre em até 5 segundos sobre o volume real de produção.
11. O modelo semântico está publicado, com dimensões e medidas nomeadas em linguagem de negócio e descrição em cada item.

**Ciclo 2 — integração Senior:**

12. A folha do período carregada do Senior, convertida por de-para, bate com o fechamento do RH por centro de custo e por rubrica.
13. Rubrica ou centro de custo novo no Senior gera pendência de mapeamento visível, sem perder valor e sem quebrar a carga.
14. Nenhuma natureza soma duas vezes: o confronto Senior × Sankhya × AllStrategy fecha, com a origem concorrente identificada e fora do total.
15. A indisponibilidade de uma origem não impede a carga da outra, e o período parcial é sinalizado e reprocessado.

**Ciclo 3 — Playground:**

16. Um usuário de negócio, sem apoio de TI, constrói um estudo com dimensão, medida, filtro e visualização, e o salva.
17. O mesmo número aparece igual no estudo do usuário e no DRE oficial, para o mesmo recorte — e desce por drill até o documento de origem.
18. Um estudo compartilhado mostra a cada leitor apenas os dados que seu perfil permite, inclusive na exportação e no envio agendado.
19. Uma consulta pesada é limitada e avisada, sem degradar coleta, carga ou publicação em andamento.
20. Um estudo é publicado como oficial somente após curadoria, e o não curado exibe o selo de análise do usuário.

Cada ciclo é homologado antes do encerramento da fase correspondente.
