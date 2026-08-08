# Escopo Tecno-Funcional — Sistema de Gestão de Projetos Culturais Incentivados

**Cliente:** Baluarte Agência de Projetos Culturais Ltda — CNPJ 07.560.676/0001-89
**Implantação:** Neuon Soluções
**Base de análise:** acervo `Baluarte-20260808T021306Z-1-001` (5 planilhas de gestão / 32 abas, 3 PDFs, 748 XMLs)
**Versão:** 1.0 — agosto/2026
**Documento complementar:** [Mapa do Negócio Baluarte](https://claude.ai/code/artifact/e2e5a479-7567-4ac9-89a1-4d5a58658aaf)

---

## Índice

1. [Objetivo e problema a resolver](#1-objetivo-e-problema-a-resolver)
2. [Princípios arquiteturais inegociáveis](#2-princípios-arquiteturais-inegociáveis)
3. [Escopo e não-escopo](#3-escopo-e-não-escopo)
4. [Mapa de módulos](#4-mapa-de-módulos)
5. [M01 — Fundação: cadastros e domínios](#m01--fundação-cadastros-e-domínios)
6. [M02 — Projetos e habilitações em leis](#m02--projetos-e-habilitações-em-leis)
7. [M03 — Orçamento](#m03--orçamento)
8. [M04 — Captação e patrocínio](#m04--captação-e-patrocínio)
9. [M05 — Fornecedores e contratos](#m05--fornecedores-e-contratos)
10. [M06 — Plano de ação (contas a pagar do projeto)](#m06--plano-de-ação-contas-a-pagar-do-projeto)
11. [M07 — Tesouraria e conciliação bancária](#m07--tesouraria-e-conciliação-bancária)
12. [M08 — Aplicações financeiras e rendimentos](#m08--aplicações-financeiras-e-rendimentos)
13. [M09 — Motor de compliance e tetos](#m09--motor-de-compliance-e-tetos)
14. [M10 — Documentos fiscais e validação](#m10--documentos-fiscais-e-validação)
15. [M11 — Prestação de contas](#m11--prestação-de-contas)
16. [M12 — Metas físicas e escopo do projeto](#m12--metas-físicas-e-escopo-do-projeto)
17. [M13 — Pessoas, folha, eSocial e rateio](#m13--pessoas-folha-esocial-e-rateio)
18. [M14 — Institucional](#m14--institucional)
19. [M15 — Painéis e BI](#m15--painéis-e-bi)
20. [M16 — Workflow, alçadas e segurança](#m16--workflow-alçadas-e-segurança)
21. [Modelo de dados](#21-modelo-de-dados)
22. [Catálogo de regras parametrizáveis](#22-catálogo-de-regras-parametrizáveis)
23. [Matriz de perfis e permissões](#23-matriz-de-perfis-e-permissões)
24. [Integrações](#24-integrações)
25. [Migração de dados](#25-migração-de-dados)
26. [Requisitos não-funcionais](#26-requisitos-não-funcionais)
27. [Mapeamento sobre Sankhya: padrão × personalização](#27-mapeamento-sobre-sankhya-padrão--personalização)
28. [Roadmap de implantação](#28-roadmap-de-implantação)
29. [Definições adotadas para o protótipo](#29-definições-adotadas-para-o-protótipo)

---

## 1. Objetivo e problema a resolver

### 1.1 O que a Baluarte faz

A Baluarte capta recursos de empresas via leis de incentivo à cultura, executa o orçamento aprovado do projeto e presta contas ao poder público. Atua em três papéis simultâneos:

- **Proponente** — o projeto é dela (Rio Memórias Ano VI, EEA PRONAC 232029)
- **Gestora** — o proponente é terceiro e ela administra a execução (Copa Studio, Associação Rio Memórias, Atrom Produções)
- **Fornecedora** — emite NF contra o projeto que administra, dentro de rubricas aprovadas

### 1.2 O problema

Toda a gestão vive em planilhas Excel, uma pasta de trabalho por combinação **projeto × lei**. Consequências observadas nos arquivos:

| Problema | Evidência no acervo |
|---|---|
| Retrabalho de digitação | O mesmo gasto é reescrito em 5 lugares: orçamento de execução, orçamento aprovado da lei, plano de ação, cronograma de desembolso e controle da conta |
| Rateio multi-lei sem trava | Colunas `ISS / LIC / LICC` preenchidas à mão, sem validação de que a soma bate com o aprovado de cada lei |
| Conciliação divergente | Três séries paralelas de rendimento no ICMS que não fecham: R$ 1.039.741,65 (Baluarte) × R$ 1.023.352,21 (contabilidade) |
| Tetos sem bloqueio | Limites de 5%, 10%, 20% e 30% em células soltas; o estouro só aparece depois e vira devolução ao órgão |
| Fórmulas quebradas | `#REF!` em abas ativas de memória de cálculo e remuneração |
| Exceções fora do padrão | "LICC 2025 está sendo operado em planilha avulsa" |
| Conhecimento tácito | Decisões críticas em campo de observação livre (nota substituída, código de serviço inadequado, valor a devolver no PRONAC seguinte) |
| Rastreabilidade por convenção | Ligação entre camadas feita por número de rubrica e nome de fornecedor digitados, não por chave |

### 1.3 Objetivo da solução

Substituir o conjunto de planilhas por um sistema transacional único onde:

1. O gasto é **lançado uma vez** e se propaga para todas as visões (execução, lei, plano de ação, cronograma, extrato, prestação de contas).
2. Os **tetos legais são preventivos**, não corretivos — o sistema impede ou exige aprovação antes do estouro.
3. O **rateio entre leis** é uma operação controlada com validação de saldo por lei.
4. A **conciliação bancária** é automática por retorno bancário, não digitada.
5. A **prestação de contas** é gerada, não montada.
6. A folha CLT e os autônomos (eSocial) são **alocados a rubricas** com trilha auditável.
7. O **institucional permanece apartado dos projetos**, com uma única ponte auditável: a NF da Baluarte contra o projeto.

---

## 2. Princípios arquiteturais inegociáveis

Estes princípios derivam diretamente de regras de negócio declaradas pela cliente e devem restringir qualquer decisão de modelagem.

### P1 — Segregação institucional × projeto

> *"Os dados não podem ser misturados pois isso não representará como fazemos a gestão. O institucional é completamente apartado dos projetos."* — Paula Sued, sócia e diretora

- O projeto **não é centro de custo da Baluarte**. É uma entidade financeira autônoma, com conta bancária exclusiva, orçamento próprio, tetos próprios e prestação de contas própria, que apenas **contrata** a Baluarte.
- Nenhum relatório padrão pode somar caixa institucional com caixa de projeto.
- A única ponte permitida é o documento fiscal emitido pela Baluarte contra o projeto (ou a folha rateada), que é **receita institucional** e **despesa de rubrica** ao mesmo tempo — e sempre sujeito a teto.
- Implementação: contextos contábeis/financeiros separados por empresa lógica, com bloqueio de lançamento cruzado sem documento de ponte.

### P2 — O orçamento de execução é a fonte da verdade

Existe **um único orçamento real** por projeto/ano, na linguagem da equipe (Oficinas, Processo Seletivo, Circulação, Comunicação, Custos Administrativos). Os orçamentos das leis são **projeções** dele.

- Lançar na visão de execução e ratear para as leis. Nunca o inverso.
- Cada linha de execução carrega um rateio `{lei → valor}` cuja soma deve igualar o total da linha.

### P3 — Multi-lei é a regra, não a exceção

Um projeto pode estar habilitado em N leis simultâneas. O EEA roda em três (LIC R$ 1.319.987,98 + ISS R$ 390.761,04 + LICC R$ 499.998 = R$ 2.210.747,02 captados).

- Toda entidade financeira do projeto tem dimensão obrigatória `habilitação` (projeto + lei).
- Cada lei tem seu próprio plano de rubricas, formulário, tetos, calendário de repasses e regra de devolução.

### P4 — Versionamento imutável do orçamento

As versões convivem lado a lado, nunca sobrescritas: "Aprovação inicial", "Readequação 01 – aprovada 30/06", "Readequação 02 – aprovada 31/10/25", "Readequação 03 – aprovada 06/03/26", "Readequação em aprovação".

- Cada versão guarda qtd, unidade, valor unitário, total **e justificativa textual por rubrica alterada**.
- Uma e apenas uma versão é `VIGENTE`; a execução sempre confronta com a vigente.
- Versões anteriores são consultáveis e comparáveis (diff por rubrica).

### P5 — Compliance preventivo

Todo lançamento passa pelo motor de regras **antes** de ser efetivado. O resultado é `PERMITIDO`, `ALERTA` ou `BLOQUEADO`, sempre com a regra citada e o valor de folga.

### P6 — Rastreabilidade por chave, não por texto

Rubrica, fornecedor, documento e lançamento bancário se ligam por identificador. Campo de observação livre existe, mas nunca é o único lugar onde uma decisão mora.

### P7 — Multi-proponente

O sistema atende projetos cujo proponente **não é a Baluarte**. Proponente é atributo da habilitação, não do sistema.

---

## 3. Escopo e não-escopo

### 3.1 Em escopo

| # | Bloco | Descrição |
|---|---|---|
| 1 | Gestão de projetos culturais | Cadastro, edições, habilitações em leis, vigências, metas |
| 2 | Orçamento multi-versão e multi-lei | Aprovado, readequações, execução, rateio |
| 3 | Captação | Prospecção, termos de compromisso, direcionamento, repasse, contrapartidas |
| 4 | Contas a pagar do projeto | Plano de ação, parcelas, aprovação, pagamento |
| 5 | Tesouraria de projeto | Contas vinculadas, conciliação, tarifas, devoluções |
| 6 | Aplicações e rendimentos | Séries por conta e veículo, autorização de uso, devolução |
| 7 | Compliance | Motor de tetos, remanejamento, alertas |
| 8 | Documentos fiscais | Recebimento, validação, guarda, substituição, impostos vinculados |
| 9 | Prestação de contas | Numeração, dossiê, exportação por lei |
| 10 | Pessoas | Alocação de folha e autônomos a rubricas, consumo de eSocial |
| 11 | Institucional | Faturamento, contratos de gestão, contas a receber/pagar, NF de serviço |
| 12 | Painéis | Saúde de projeto, tetos, fluxo de caixa, produtividade |
| 13 | Workflow e alçadas | Maker–checker, perfis, trilha de auditoria |

### 3.2 Fora de escopo (nesta fase)

- Processamento de folha de pagamento (cálculo). O sistema **consome** o resultado da folha e a transmissão eSocial feitas pelo sistema de RH/contabilidade atual.
- Escrituração contábil fiscal completa (SPED, ECD, ECF) — permanece na contabilidade terceirizada.
- Emissão de NFS-e pela prefeitura (o sistema prepara os dados e registra a nota emitida).
- Portal público de inscrição de alunos/participantes.
- Gestão de conteúdo/acervo cultural.

---

## 4. Mapa de módulos

```
┌─────────────────────────────────────────────────────────────────────────┐
│ M16 · Workflow, alçadas e segurança          (transversal)              │
│ M09 · Motor de compliance e tetos            (transversal)              │
└─────────────────────────────────────────────────────────────────────────┘

  INSTITUCIONAL                    │        PROJETOS
  ─────────────────────────────────┼──────────────────────────────────────
                                   │
  M14 · Institucional              │   M02 · Projetos e habilitações
   ├ Contratos de gestão           │    ├ Edições / anos
   ├ Faturamento e NF de serviço   │    ├ Habilitação por lei
   ├ Contas a receber              │    └ Conta bancária vinculada
   ├ Contas a pagar da empresa     │
   └ Cursos livres                 │   M03 · Orçamento
                                   │    ├ Aprovado (versões)
  M13 · Pessoas                    │    ├ Execução (fonte da verdade)
   ├ Colaboradores e vínculos      │    └ Rateio multi-lei
   ├ Consumo eSocial (XML)         │
   ├ Rateio folha → rubrica  ──────┼──▶ M04 · Captação
   └ Autônomos / RPA               │    ├ Termos de compromisso
                                   │    ├ Direcionamento e repasse
                                   │    └ Contrapartidas
       ┌───────── PONTE ───────────┤
       │ NF Baluarte → projeto     │   M05 · Fornecedores e contratos
       │ (sujeita a teto)          │
       └───────────────────────────┤   M06 · Plano de ação (AP projeto)
                                   │    └ Parcelas, status, aprovação
  M15 · Painéis e BI               │
                                   │   M07 · Tesouraria e conciliação
  M01 · Fundação                   │   M08 · Aplicações e rendimentos
   ├ Leis e planos de rubricas     │   M10 · Documentos fiscais
   ├ Domínios controlados          │   M11 · Prestação de contas
   ├ Órgãos e plataformas          │   M12 · Metas físicas
   └ Tabelas de referência (FGV)   │
```

---

## M01 — Fundação: cadastros e domínios

### Objetivo
Parametrizar o comportamento de cada lei de incentivo sem código, de modo que uma lei nova entre por cadastro.

### Entidades

#### `LEI_INCENTIVO`
| Campo | Tipo | Obs |
|---|---|---|
| `id` | PK | |
| `sigla` | texto | LIC, ISS, ICMS, LICC, PROMAC |
| `nome_completo` | texto | Lei 8.313/91, Lei do ISS/RJ… |
| `esfera` | enum | FEDERAL, ESTADUAL, MUNICIPAL |
| `uf` / `municipio` | texto | |
| `orgao_gestor` | FK ÓRGÃO | MinC, SMC/CCPC, SECEC… |
| `plataforma` | FK PLATAFORMA | SALIC, Desenvolve Cultura, Profilm… |
| `mascara_identificador` | texto | `WEC nnn/nn/aaaa`, `SEI-nnnnnn/nnnnnn/aaaa`, PRONAC numérico |
| `estrutura_orcamento` | enum | Ver abaixo |
| `forma_devolucao` | enum | GRU, DARM, GUIA_ESTADUAL, OUTRO |
| `exige_referencia_preco` | bool | LICC exige (Tabela FGV/2022) |
| `permite_uso_rendimento` | enum | NAO, SIM_COM_AUTORIZACAO, SIM |
| `ativo` | bool | |

**`estrutura_orcamento`** — define a hierarquia de rubricas e o formulário:

| Valor | Hierarquia | Lei |
|---|---|---|
| `ETAPA_RUBRICA` | Etapa (1 Pré-produção, 2 Produção, 3 Pós, 4 Comunicação) → rubrica `n.n` | ISS/RJ |
| `GRUPO_DESPESA` | Grupo fixo (1 Pessoal … 6 Impostos) → item `n.n` | ICMS/RJ |
| `PRODUTO_ETAPA_UF` | Produto → Etapa → UF → item nº | LIC/Rouanet |
| `GRUPO_BENEFICIARIO` | Grupo de despesa (lista fechada) + beneficiário + referência | LICC/ES |

#### `PLANO_RUBRICA` e `RUBRICA_MODELO`
Plano de contas orçamentário por lei. Cada `RUBRICA_MODELO` tem código hierárquico, descrição, grupo/etapa, unidade padrão, e flags:
- `conta_como_administrativo` (bool)
- `conta_como_divulgacao` (bool)
- `conta_como_captacao` (bool)
- `conta_como_remuneracao_proponente` (bool)
- `atividade` (enum MEIO / FIM) — usado no controle ISS

#### `TABELA_REFERENCIA_PRECO`
Para leis que exigem justificativa de valor. Ex.: "Item 153 da Tabela de Mão de Obra da FGV/2022", "Item 67 da Tabela de Serviços da FGV/2022", "Art. 19, §1º, IN 001/2023". Campos: fonte, item, descrição, valor de referência, vigência.

#### Domínios controlados (extraídos das abas `Dados`)

| Domínio | Valores |
|---|---|
| `TIPO_DOCUMENTO` | NOTA FISCAL · RPA · RECIBO · GUIA DE RECOLHIMENTO · FATURA · BOLETO · DANFE · CUPOM FISCAL · TARIFA · REPASSE · APÓLICE |
| `TIPO_TRANSACAO` | PIX · TED/DOC · REPASSE · DEVOLUÇÃO · TAXA · CRÉDITO · CHEQUE · SAQUE/DINHEIRO · TRANSFERÊNCIA BANCÁRIA |
| `STATUS_PARCELA` | Enviar dados · Dados enviados · RPA enviado · Pagar · Pago · Cadastrar · Cadastrado · Comprov enviado |
| `STATUS_PLATAFORMA` | Alterar · Em aprovação · Subir no sistema · Inserido · Conferido |
| `TIPO_DESPESA_ICMS` | 1. Pessoal · 2. Estrutura · 3. Logística · 4. Divulgação/Mídia/Comunicação · 5. Despesas Administrativas · 6. Impostos, Tarifas, Seguros |
| `UNIDADE_MEDIDA` | mês · dia · hora · semana · diária · cachê · serviço · projeto · período · unidade · verba · licença · unitário |
| `FAIXA_PATROCINIO` | Patrocinador Master · Copatrocínio · Apoio |
| `MARCACAO_SALIC` | Sim · Não · Ajustar |

#### `TEMPLATE_TEXTO`
Catálogo de textos padronizados com lacunas, extraído da aba `Dados` da planilha LIC:

- **Texto obrigatório da NF** por lei/projeto — ex.: *"nº + rubrica para o projeto Rio Memórias - Ano VII, SEI-180001/000233/2025 e nº 66153. Governo do Estado do Rio de Janeiro / Secretaria de Estado de Cultura e Economia Criativa. Lei de incentivo à Cultura."*
- **Justificativas de base de pagamento**: reembolso simples, contracheque, FGTS/INSS/IR, RPA + guia, nota fiscal com retenção, passagem aérea (agência e cartão), mídia de internet.

Cada template tem variáveis (`{{valor}}`, `{{data_pagamento}}`, `{{data_reembolso}}`, `{{passageiro}}`, `{{trecho}}`) preenchidas automaticamente no momento da geração.

### Telas

| Código | Tela | Função |
|---|---|---|
| T01.1 | Cadastro de leis de incentivo | Parametriza estrutura, tetos padrão, devolução, plataforma |
| T01.2 | Plano de rubricas por lei | Árvore de rubricas-modelo com flags de classificação |
| T01.3 | Tabelas de referência de preço | Importa e versiona tabelas FGV e similares |
| T01.4 | Domínios do sistema | Manutenção de listas controladas |
| T01.5 | Templates de texto | Editor com variáveis e pré-visualização |
| T01.6 | Órgãos e plataformas | Contatos, prazos, canais |

---

## M02 — Projetos e habilitações em leis

### Objetivo
Representar o projeto como programa plurianual e cada habilitação em lei como entidade financeira autônoma.

### Modelo

```
PROJETO (Rio Memórias, Estúdio Escola de Animação)
  └── EDICAO (Ano VI, Ano VII, Ano XII, Ano XIII)
        └── HABILITACAO (uma por lei)
              ├── CONTA_BANCARIA_VINCULADA (1..N: movimento, captação)
              ├── ORCAMENTO_APROVADO (versões)
              ├── TERMO_COMPROMISSO (N patrocinadores)
              ├── REPASSE (parcelas recebidas)
              └── PRESTACAO_CONTAS
```

### Entidade `HABILITACAO`

| Campo | Exemplo |
|---|---|
| `projeto_edicao` | Estúdio Escola de Animação — Ano XIII |
| `lei` | ISS |
| `identificador` | WEC 456/01/2023 |
| `proponente` | Copa Studio — CNPJ 09.551.826/0001-13 |
| `papel_baluarte` | GESTORA |
| `area_cultural` | Audiovisual |
| `linha_acao` | Exposições, Festivais e Mostras |
| `valor_aprovado` | 1.678.000,00 |
| `valor_captado` | 390.761,04 |
| `percentual_captado` | 23% (calculado) |
| `vigencia_inicio` / `vigencia_fim` | |
| `outra_fonte_recurso` | "Lei Federal de Incentivo à Cultura. R$ 1.319.987,98" |
| `situacao` | EM_CAPTACAO · EM_EXECUCAO · EM_PRESTACAO · ENCERRADA |
| `saldo_edicao_anterior` | 36.534,02 (transportado) |

### Regras

- **R02.1** — Uma habilitação exige ao menos uma conta bancária vinculada antes de qualquer lançamento.
- **R02.2** — O `percentual_captado` é recalculado a cada repasse e dispara alerta de necessidade de readequação quando cai abaixo do parâmetro (sugestão: 90%).
- **R02.3** — Saldo e pendência de edição anterior são transportados explicitamente com documento de origem. Caso real: *"A Baluarte precisa devolver este valor negativo já que optamos por não realizar o festival. O valor devolvido deverá ser pago no Pronac seguinte."*
- **R02.4** — Encerramento de habilitação só é possível com prestação de contas aprovada e saldo zerado (ou devolução registrada).

### Telas

| Código | Tela | Função |
|---|---|---|
| T02.1 | Árvore de projetos | Projeto → edições → habilitações, com semáforo de saúde |
| T02.2 | Ficha da habilitação | Todos os dados legais, contas, situação, links para orçamento e prestação |
| T02.3 | Painel do projeto | Visão consolidada multi-lei: captado, executado, saldo, tetos, metas |
| T02.4 | Contas bancárias vinculadas | Banco, agência, conta, tipo (movimento/captação), saldo, data de abertura |

---

## M03 — Orçamento

O módulo mais crítico. Três camadas conectadas.

### 3.1 Orçamento aprovado (por lei, versionado)

Reproduz o formulário oficial de cada lei.

**Entidade `ORCAMENTO_VERSAO`**

| Campo | Obs |
|---|---|
| `habilitacao` | FK |
| `tipo` | INICIAL · READEQUACAO · ADAPTACAO_AO_CAPTADO |
| `sequencia` | 01, 02, 03 |
| `situacao` | RASCUNHO · EM_APROVACAO · APROVADA · VIGENTE · SUPERADA · REJEITADA |
| `data_protocolo` / `data_aprovacao` | |
| `documento_protocolo` | Anexo |

**Entidade `ORCAMENTO_LINHA`**

| Campo | Obs |
|---|---|
| `versao` | FK |
| `rubrica` | Código hierárquico + descrição |
| `etapa` / `grupo` / `produto` / `uf` | Conforme estrutura da lei |
| `qtd` · `unidade` · `qtd_unidades` · `valor_unitario` | |
| `valor_total` | Calculado: qtd × qtd_unidades × valor_unitário |
| `justificativa` | Texto — **obrigatório** quando o valor difere da versão anterior |
| `referencia_preco` | FK tabela de referência — obrigatório se a lei exigir |
| `beneficiario` | CONTRATADO · PROPONENTE (LICC) |
| `classificacao_lei` | Código de classificação próprio da lei, vindo de lista parametrizável em `CLASSIFICACAO_RUBRICA`. O protótipo já nasce com os marcadores `RN` e `RD` usados no SALIC/Rouanet |

### Telas

| Código | Tela | Função |
|---|---|---|
| T03.1 | **Editor de orçamento aprovado** | Grade hierárquica com subtotais por etapa/grupo; validação de tetos em tempo real; campo de justificativa obrigatório em linha alterada |
| T03.2 | **Comparador de versões** | Colunas lado a lado (como na planilha) — versão A × B × C, destacando alteradas, zeradas e incluídas, com justificativa. Exportável no layout oficial da lei |
| T03.3 | Fluxo de readequação | Submissão, acompanhamento na plataforma, anexo do deferimento |

### 3.2 Orçamento de execução (fonte da verdade)

**Entidade `EXECUCAO_LINHA`** — o orçamento real, na linguagem da equipe.

| Campo | Exemplo |
|---|---|
| `projeto_edicao` | EEA 2025 |
| `bloco` | REALIZAÇÃO · PRODUÇÃO · COMUNICAÇÃO · CUSTOS ADMINISTRATIVOS · PRÉ-PRODUÇÃO ANO SEGUINTE |
| `agrupador` | Oficinas – sala de aula · Processo seletivo · Pós-produção · Circulação · ES Aula Inaugural |
| `descricao` | Locação notebooks (alunes + estag + rede) |
| `unidade` · `qtd` · `ocorrencia` · `valor_unitario` | unidade · 17 · 7 · 954,88 |
| `valor_total` | 113.631,00 |
| `observacao` | Texto livre com a racional |

**Entidade `EXECUCAO_RATEIO`** — o coração do multi-lei.

| Campo | |
|---|---|
| `execucao_linha` | FK |
| `habilitacao` | FK (a lei que paga) |
| `valor` | |
| `rubrica_lei` | FK — a rubrica correspondente naquela lei |

### Regras

- **R03.1** — `Σ EXECUCAO_RATEIO.valor = EXECUCAO_LINHA.valor_total`. Bloqueante.
- **R03.2** — `Σ rateios por habilitação ≤ valor captado da habilitação`. Alerta ao ultrapassar o planejado, bloqueio ao ultrapassar o captado + rendimento autorizado.
- **R03.3** — Toda linha de execução com rateio deve apontar rubrica existente na versão **vigente** do orçamento aprovado daquela lei.
- **R03.4** — Alteração de rateio gera log com usuário, data, valores antes/depois e motivo.

### Telas

| Código | Tela | Função |
|---|---|---|
| T03.4 | **Orçamento de execução** | Grade por bloco/agrupador com colunas de rateio por lei; totalizadores por lei no rodapé confrontando com o captado |
| T03.5 | **Assistente de rateio** | Ao lançar uma despesa, sugere distribuição por lei considerando saldo de rubrica, tetos e histórico |
| T03.6 | **Matriz de cobertura** | Linha de execução × lei, mostrando o que está descoberto (sem fonte) |

### 3.3 Cronograma de desembolso (visão de acompanhamento)

Não é uma entidade nova — é a **visão consolidada** por rubrica de cada habilitação, gerada dos dados de M03 + M06 + M07. Substitui as abas `Crono Desembolso *`.

**Colunas (idênticas às da planilha, agora calculadas):**

| Coluna | Origem |
|---|---|
| ITEM · DESCRIÇÃO | Rubrica da versão vigente |
| APROVADO | `ORCAMENTO_LINHA.valor_total` (versão vigente) |
| EXECUÇÃO (planejado/negociado) | Σ parcelas do plano de ação |
| EXECUTADO | Σ pagamentos liquidados |
| FALTA EXECUTAR | Execução − Executado |
| REMANEJAMENTO | Folga legal restante = `Aprovado × %remanejamento − |Execução − Aprovado|` |
| OBSERVAÇÕES / FORNECEDOR | Alocações de fornecedor |
| Por repasse: PLANEJADO × EXECUTADO | Cruzamento parcela × repasse |
| SOBRA EM CADA PARCELA | Repasse recebido − executado no período |

**Bloco Resumo Financeiro** (rodapé): patrocinadores e valores, rendimentos utilizáveis e não utilizáveis, verba total, custo total, sobra, limites de alteração, checagem de tetos por entidade (ex.: `COPA STUDIO planejado × alocado × limite ISS × outros gastos × saldo`).

| Código | Tela | Função |
|---|---|---|
| T03.7 | **Cronograma de desembolso** | Visão-espelho da planilha, somente leitura, com drill-down até o lançamento bancário; exportação em Excel no layout atual para uso externo |

---

## M04 — Captação e patrocínio

### Objetivo
Controlar o funil de captação e o ciclo **compromisso → direcionamento → repasse**, que hoje vive na aba `Desembolso_CI`.

### Entidades

#### `PATROCINADOR`
Razão social, CNPJ, contatos, histórico de projetos apoiados.

#### `TERMO_COMPROMISSO`
| Campo | Exemplo |
|---|---|
| `habilitacao` | EEA ISS 2025 |
| `patrocinador` | ONS |
| `faixa` | Copatrocínio |
| `valor_comprometido` | 93.121,67 |
| `percentual_captacao` | 6% (calculado) |
| `data_assinatura` · `data_publicacao` | |
| `custo_publicacao` | Rubrica "Publicação dos termos de compromisso" |
| `vigencia` | Relevante para validade de nota substituída |

#### `DIRECIONAMENTO`
A empresa direciona o crédito fiscal. Campos: termo, valor, data, comprovante, situação.

#### `REPASSE`
O fundo/órgão transfere para a conta do projeto. Campos: habilitação, número do repasse (1º, 2º, 3º), valor planejado, valor recebido, data, conta de destino, lançamento bancário vinculado.

#### `CONTRAPARTIDA`
Obrigação devida ao patrocinador (vídeo institucional, logo animada, cotas de ingresso). Campos: patrocinador, descrição, prazo, situação, rubrica de custo, evidência de entrega.

### Regras

- **R04.1** — Direcionamento e repasse são etapas distintas com datas próprias; o painel mostra quem direcionou e ainda não teve repasse (evidência real: Nasajon direcionado em 20/03/2025 sem repasse).
- **R04.2** — `Σ termos de compromisso = valor da solicitação` apresentado no formulário da lei.
- **R04.3** — Saldo restante para depósito e métrica mensal calculados automaticamente (aba `Desembolso_CI` linhas 21–30).
- **R04.4** — Repasse recebido a maior gera devolução com rastreio. Caso real: *"Devolução de valor enviado a mais do repasse dia 27/11. Os repasses foram nos valores de R$ 132.133,93 e R$ 16.687,25. Total de R$ 148.821,18."*
- **R04.5** — O consumo do teto de captação (remuneração do captador) é calculado sobre o valor efetivamente captado, não sobre o comprometido.

### Telas

| Código | Tela | Função |
|---|---|---|
| T04.1 | Funil de captação | Prospect → negociação → termo assinado → direcionado → repassado |
| T04.2 | **Mapa de desembolso do patrocinador** | Matriz patrocinador × mês (FEV/MAR, MAR/ABR…) com valor, direcionamento, data, repasse, data — espelho da `Desembolso_CI` |
| T04.3 | Termos de compromisso | Cadastro, anexo, publicação, vigência |
| T04.4 | Contrapartidas | Checklist por patrocinador com prazo e evidência |

---

## M05 — Fornecedores e contratos

### Objetivo
Substituir a tabela de ~120 fornecedores mantida em aba de Excel (com o aviso "sempre insira novas linhas utilizando a opção Inserir Linha… pois isso pode impedir a replicação automática das fórmulas").

### Entidade `FORNECEDOR`

| Campo | Obs |
|---|---|
| `razao_social` · `nome_fantasia` | |
| `documento` | CNPJ ou CPF |
| `natureza` | PJ · MEI · ME · PF (RPA) · CLT · ESTAGIÁRIO |
| `inscricao_municipal` · `inscricao_estadual` | |
| `regime` | Simples Nacional · Lucro Presumido · Lucro Real · Isento |
| `codigo_servico_habitual` | Para validação da NF |
| `contatos` | E-mail, telefone, responsável |
| `dados_bancarios` | Banco, agência, conta, tipo de chave PIX, chave |
| `situacao_cadastral` | Ativo · Bloqueado · Em regularização |

### Regras

- **R05.1** — Alteração de natureza jurídica (MEI → ME) gera novo CNPJ e exige vínculo entre cadastros + declaração assinada. Caso real: *"Fornecedor alterou de MEI para ME, CNPJ alterado e contrato com termo aditivo. Terá em sua documentação uma declaração assinada atestando que se trata da mesma pessoa anteriormente contratada."*
- **R05.2** — Fornecedor MEI é sinalizado para emitir pelo sistema nacional de NFS-e.
- **R05.3** — Duplicidade por CNPJ é bloqueada (a planilha tem "MOBILE COMUNICACAO LTDA" duas vezes com o mesmo CNPJ).
- **R05.4** — Alerta quando o mesmo fornecedor pessoa física ultrapassa limite de faturamento MEI no acumulado do ano.

### `CONTRATO`
Fornecedor, habilitação(ões), rubrica(s), objeto, valor, parcelas previstas, vigência, aditivos, anexo assinado.

### Telas

| Código | Tela | Função |
|---|---|---|
| T05.1 | Cadastro de fornecedores | Com validação de CNPJ/CPF e consulta de situação |
| T05.2 | Contratos | Vinculação a rubricas e geração automática das parcelas no plano de ação |
| T05.3 | Painel do fornecedor | Tudo que ele recebeu, por projeto, lei e rubrica |

---

## M06 — Plano de ação (contas a pagar do projeto)

### Objetivo
Substituir as abas `Plano de ação *`. É a tela onde a equipe trabalha diariamente.

### Entidade `PARCELA`

| Campo | Exemplo |
|---|---|
| `habilitacao` | EEA ISS |
| `rubrica` | 2.18 Locação de computadores |
| `execucao_linha` | Locação notebooks |
| `fornecedor` | GoPlug Locações |
| `contrato` | FK opcional |
| `valor` | 16.233,00 |
| `mes_referencia` | out-nov-dez |
| `previsao_pagamento` | 20/11/2025 |
| `status` | Comprov Enviado |
| `data_status` | 01/12/2025 |
| `tipo_documento` | NOTA FISCAL |
| `numero_documento` | NF 10719 |
| `data_documento` | 25/11/2025 |
| `data_pagamento` | |
| `id_plataforma` | 121192 |
| `parcela_pai` | Para linha de imposto vinculada (`IMP NF 10719`) |
| `justificativa_base_pagamento` | Gerada do template |
| `observacao` | |

### Regras

- **R06.1** — Uma rubrica comporta **N fornecedores** e cada fornecedor **N parcelas**. A soma das parcelas por rubrica alimenta a coluna EXECUÇÃO do cronograma.
- **R06.2** — Toda parcela consome saldo da rubrica na versão vigente. Ao criar, o motor de compliance (M09) valida.
- **R06.3** — **Imposto vinculado**: parcelas do tipo `IMP` são filhas de uma parcela-mãe (nota), com data de pagamento própria e posterior. Devem aparecer no cronograma como linha separada mas somar à mesma rubrica. Evidência: `NF 1949` + `IMP 1949`, `NF 2130` + `IMP 2130`.
- **R06.4** — **Folha como parcela**: pagamentos de folha alocados a rubrica geram até 4 parcelas por competência (LÍQUIDO, IR, INSS, FGTS), com previsão e execução independentes. Evidência: aba `Rem Balu LIC`.
- **R06.5** — Mudança de status registra usuário e timestamp; o status `Pago` exige lançamento bancário conciliado.
- **R06.6** — Solicitação de pagamento pode entrar por integração (hoje "via Trello") e cai na fila como rascunho.

### Telas

| Código | Tela | Função |
|---|---|---|
| T06.1 | **Plano de ação** | Grade agrupada por etapa → rubrica → fornecedor → parcela, com filtros por status, período e lei. Edição em linha |
| T06.2 | **Fila de pagamentos** | Parcelas com previsão na semana, agrupadas por conta bancária, prontas para remessa |
| T06.3 | Solicitação de pagamento | Formulário para o solicitante (ou integração), com anexo do documento |
| T06.4 | **Aprovação** | Fila do coordenador/gerente com visão de impacto no teto antes de aprovar |
| T06.5 | Programação de parcelas | Geração em série a partir do contrato (12 meses, valores fixos ou variáveis) |

---

## M07 — Tesouraria e conciliação bancária

### Objetivo
Substituir a aba `CONTROLE CC` — o extrato conciliado linha a linha.

### Entidade `LANCAMENTO_BANCARIO`

| Campo | Origem hoje |
|---|---|
| `conta_vinculada` | Banco do Brasil AG 1251-3 CC 45157-6 |
| `data` | DATA |
| `tipo_transacao` | PIX, TED/DOC, REPASSE, DEVOLUÇÃO, TAXA, CRÉDITO |
| `numero_transacao` | Número Pgto (ex.: 90501, 120502) |
| `favorecido` | RAZÃO SOCIAL + CNPJ/CPF |
| `valor` · `natureza` | VALOR · C/D |
| `saldo` | Calculado |
| `parcela_vinculada` | FK — a conciliação |
| `numero_comprovacao` | Sequencial da prestação de contas |
| `observacao` | "ERRO TEMPO EXCEDIDO", "CPF ou CNPJ inválidos" |

### Regras

- **R07.1** — Cada conta vinculada tem extrato próprio e saldo próprio. Nunca há saldo consolidado entre projetos.
- **R07.2** — **Conciliação**: importação de retorno bancário (OFX/CNAB) com sugestão automática de vínculo parcela ↔ lançamento por valor + data + favorecido. Conciliação manual assistida para o restante.
- **R07.3** — **Devolução por erro** gera par de lançamentos (crédito de estorno + débito de reenvio) mantendo a rastreabilidade do motivo. Padrão observado no extrato real.
- **R07.4** — **Tarifas bancárias** são lançamentos sem rubrica ou com rubrica específica (`6.3 Taxas bancárias` no ICMS; `Taxas bancárias` nos custos administrativos).
- **R07.5** — O **número de comprovação** é sequencial por habilitação, atribuído automaticamente ao conciliar um pagamento, e nunca reutilizado.
- **R07.6** — Repasses recebidos entram como crédito e são vinculados ao `REPASSE` de M04.

### Telas

| Código | Tela | Função |
|---|---|---|
| T07.1 | **Extrato da conta vinculada** | Espelho da `CONTROLE CC`, com todas as colunas atuais + drill-down |
| T07.2 | **Conciliação bancária** | Duas colunas: extrato importado × parcelas em aberto, com match automático e ações em lote |
| T07.3 | Remessa de pagamentos | Geração de arquivo de pagamento (CNAB/PIX em lote) a partir da fila aprovada |
| T07.4 | Posição de caixa por projeto | Saldo por conta, previsto × realizado, próximos vencimentos |

---

## M08 — Aplicações financeiras e rendimentos

### Objetivo
Substituir a aba `CONTROLE APLICACAO` e resolver a divergência de conciliação hoje existente.

### Entidade `RENDIMENTO`

| Campo | Obs |
|---|---|
| `conta_vinculada` | Movimento ou Captação |
| `veiculo` | Invest Fácil · CDB · Poupança |
| `competencia` | Mês/ano |
| `valor` | Pode ser negativo (IR sobre resgate) |
| `saldo_acumulado` | Calculado |
| `fonte` | BALUARTE · CONTABILIDADE · BANCO |
| `situacao_uso` | NAO_SOLICITADO · SOLICITADO · AUTORIZADO · NAO_UTILIZAVEL |
| `data_autorizacao` · `documento_autorizacao` | |
| `valor_devolvido` · `documento_devolucao` | DARM / GRU |

### Regras

- **R08.1** — Rendimento **não é receita livre**. A regra difere por lei e vem do cadastro `LEI_INCENTIVO.permite_uso_rendimento`:
  - **LIC** — `NAO` — não pode usar por conta do teto do projeto
  - **ISS** — `SIM_COM_AUTORIZACAO` — precisa de autorização prévia da CCPC
  - **ICMS** — `SIM_COM_AUTORIZACAO`
  - **LICC** — `SIM_COM_AUTORIZACAO` (parâmetro inicial mais restritivo; alterável no cadastro da lei sem desenvolvimento)
- **R08.2** — Rendimento **autorizado** vira orçamento adicional e deve ser alocado a rubrica específica, com registro. Caso real: Rio Memórias VI, R$ 8.395,21 autorizados e aplicados na rubrica 2.26 Oficineiro.
- **R08.3** — Rendimento gerado **após** a autorização é `NAO_UTILIZAVEL` e deve ser devolvido. Caso real: *"não poderemos usar o que render adiante, a não ser que seja solicitado um novo uso. Sobras decorrentes de novos rendimentos deverão ser recolhidas ao fundo."*
- **R08.4** — **Conciliação tripla**: o sistema mantém as três séries (Baluarte, contabilidade, extrato bancário) e exibe a divergência por competência, exigindo justificativa ou ajuste. Hoje a divergência é de R$ 16.389,44 no ICMS e ninguém a resolve.
- **R08.5** — Devolução por DARM/GRU gera lançamento bancário e documento anexado.

### Telas

| Código | Tela | Função |
|---|---|---|
| T08.1 | **Controle de aplicação** | Série mensal por conta e veículo, com saldo acumulado |
| T08.2 | **Conciliação de rendimentos** | Três colunas (Baluarte × contabilidade × banco) com destaque de divergência |
| T08.3 | Solicitação de uso de rendimento | Fluxo até a autorização do órgão, com anexo |
| T08.4 | Devoluções | Cálculo do valor a devolver, geração da guia, baixa |

---

## M09 — Motor de compliance e tetos

### Objetivo
Transformar os limites hoje calculados em células soltas em **validação preventiva** com bloqueio ou alçada.

### Arquitetura

Um motor de regras parametrizável por lei. Cada regra é uma linha de cadastro, não código:

```
REGRA_TETO
  ├── lei
  ├── tipo_teto            (AGENCIAMENTO, ADMINISTRATIVO, DIVULGACAO,
  │                         REMUNERACAO_PROPONENTE, REMUNERACAO_FORNECEDOR,
  │                         CAPTACAO, REMANEJAMENTO_RUBRICA, REMANEJAMENTO_TOTAL)
  ├── base_calculo         (VALOR_APROVADO, VALOR_CAPTADO, VALOR_TOTAL_PROJETO,
  │                         VALOR_RUBRICA)
  ├── percentual           (5, 10, 20, 30)
  ├── valor_absoluto_max   (ex.: R$ 50.000 para captação no ICMS)
  ├── considera_folha      (bool — na LIC o teto do proponente NÃO conta folha)
  ├── acao                 (ALERTAR, BLOQUEAR, EXIGIR_APROVACAO)
  └── vigencia
```

### Tetos identificados no acervo

| Teto | Base | Lei | Evidência |
|---|---|---|---|
| Agenciamento / captação | 5% do orçamento específico | ISS | Nome da rubrica: *"Agenciamento - Máximo 5% do orçamento específico Lei do ISS"* |
| Captação | 10% e ≤ R$ 50.000 | ICMS | Instruções da SECEC |
| Custos administrativos | 10% do total | ISS, ICMS | *"custos adm com máximo de 10%"* + marcação `> limite de 10% ATENÇÃO` |
| Divulgação/mídia | 20% do total | ICMS | Instruções da SECEC; célula "Limite custos divulgação" R$ 629.110,80 |
| Remuneração do proponente | 20% | ISS | Bloco CONTROLE ISS: `teto proponente 125.480,98 · 0,20` |
| Remuneração de fornecedores | 30% | ISS | Bloco CONTROLE ISS: `teto fornecedores 188.221,47 · 0,30` |
| Remuneração do proponente | valor absoluto | LIC | `EXECUÇÃO 240.179,53 × TETO 300.681,22` — **"o limite é baseado em NFs, não conta folha de pagamento"** |
| Remanejamento por rubrica | 20% | ISS, ICMS | Coluna REMANEJAMENTO em todos os cronogramas |
| Remanejamento do total | 20% | ISS | *"alteração de até 20% do valor de uma rubrica aprovada ou do valor total do orçamento aprovado"* |

### Regra de remanejamento

> *"O produtor cultural/proponente estará dispensado do requerimento de readequação orçamentária para o projeto cultural e da correspondente justificativa, podendo realizá-la diretamente, sem prévia autorização da SMC, quando consistir em alteração de até 20% do valor de uma rubrica aprovada ou do valor total do orçamento aprovado."*

Implementação:

```
folga_remanejamento(rubrica) =
    valor_aprovado × %remanejamento − |valor_executado − valor_aprovado|

SE  valor_executado ≤ valor_aprovado × (1 + %remanejamento)
    → PERMITIDO, sem readequação
SENÃO
    → EXIGE readequação orçamentária formal com justificativa
```

### Controle "atividade meio × atividade fim" (ISS)

O ISS exige separar despesas de atividade-meio (gestão) das de atividade-fim (realização cultural), com tetos distintos por entidade. Evidência: `CONTROLE ISS — copa 82.570,17 · baluarte 115.719,28 · fim 295.662,95 · atividade meio 29.704,17`.

- Cada rubrica-modelo carrega o atributo `atividade` (MEIO/FIM).
- Painel por entidade: planejado × alocado × diferença × limite × outros gastos previstos × saldo (espelho do bloco da planilha).

### Comportamento

| Situação | Ação |
|---|---|
| Consumo < 80% do teto | Livre |
| 80% ≤ consumo < 100% | Alerta visual no lançamento e no painel |
| Consumo ≥ 100% | Bloqueio, liberável apenas por alçada de diretoria com justificativa |
| Estouro já ocorrido | Gera **obrigação de devolução** rastreada até a liquidação |

### Telas

| Código | Tela | Função |
|---|---|---|
| T09.1 | **Painel de tetos** | Por habilitação: cada teto com barra de consumo, valor limite, consumido, folga |
| T09.2 | Cadastro de regras | Parametrização por lei, sem código |
| T09.3 | Simulador de impacto | "Se eu pagar X nesta rubrica, o que acontece com os tetos?" |
| T09.4 | Obrigações de devolução | Estouros identificados, valor, prazo, situação, guia gerada |

---

## M10 — Documentos fiscais e validação

### Objetivo
Estruturar o que hoje mora em observação livre e depende de conferência humana.

### Entidade `DOCUMENTO_FISCAL`

| Campo | |
|---|---|
| `tipo` | NF, RPA, Recibo, Guia, Fatura, Boleto, DANFE, Apólice |
| `numero` · `serie` · `data_emissao` · `competencia` | |
| `emitente` (fornecedor) · `tomador` | |
| `valor_bruto` · `retencoes` (ISS, IR, INSS, PIS, COFINS, CSLL) · `valor_liquido` | |
| `codigo_servico` | Validado contra o esperado da rubrica |
| `discriminacao` | Texto da nota |
| `arquivo` | PDF e XML |
| `situacao` | RECEBIDO · EM_CONFERENCIA · APROVADO · REJEITADO · SUBSTITUIDO · CANCELADO |
| `documento_substituto` | FK — para o caso de substituição |
| `motivo_substituicao` | |
| `declaracao_anexa` | Quando necessário |

### Regras

- **R10.1** — **Texto obrigatório da nota**: o sistema gera o texto que o fornecedor deve usar, a partir do template da lei + dados do projeto + rubrica. Exemplo real gerado hoje à mão:
  > *"nº + rubrica para o projeto Rio Memórias - Ano VII, SEI-180001/000233/2025 e nº 66153. Governo do Estado do Rio de Janeiro / Secretaria de Estado de Cultura e Economia Criativa. Lei de incentivo à Cultura. Valor: R$ 00,00. Inserir dados bancários no corpo da nota (preferencialmente chave PIX; para MEI é permitido chave PF ou PJ). Emitir com código de serviço de acordo com a descrição da nota (MEI deverá usar o sistema nacional de emissão). OBS Contabilidade: inserir sempre nome completo e número do CRC do profissional."*
- **R10.2** — **Validação de código de serviço**: comparar o código da nota com o esperado para a rubrica; divergência gera alerta. Casos reais que hoje viram observação: *"o código não é o ideal pois trata de indústria cinematográfica e essa é uma rubrica de produção executiva de uma exposição"*; *"achei o código da NF esquisito (pesquisa de mercado); vale alertar a Associação"*.
- **R10.3** — **Substituição de nota**: registrar motivo, vigência do termo de compromisso no momento da substituição, e declaração assinada quando houver mudança de CNPJ do fornecedor.
- **R10.4** — **Retenções geram parcela filha** (guia de recolhimento) com vencimento próprio.
- **R10.5** — Importação automática de **XML de NFS-e tomada** (padrão ABRASF/Ginfes, já presente no acervo) para pré-preencher o documento: número, data, competência, prestador, valores, retenções, código de serviço, município da prestação.
- **R10.6** — **Justificativa de base de pagamento** gerada do template conforme o tipo (reembolso, folha, FGTS/INSS/IR, RPA, NF com retenção, passagem aérea, mídia de internet).

### Telas

| Código | Tela | Função |
|---|---|---|
| T10.1 | **Recepção de documentos** | Upload/importação XML, OCR opcional, fila de conferência |
| T10.2 | Conferência | Checklist por lei: texto correto, código de serviço, dados bancários, CRC do contador |
| T10.3 | Gerador de instrução ao fornecedor | Produz o texto obrigatório e envia por e-mail |
| T10.4 | Substituições | Trilha de nota substituída com documentação de suporte |

---

## M11 — Prestação de contas

### Objetivo
Gerar o dossiê em vez de montá-lo. Hoje isso é a coluna `Nº` do CONTROLE CC mais uma pasta de PDFs.

### Entidades

- `PRESTACAO_CONTAS` — habilitação, período, situação (EM_MONTAGEM, PROTOCOLADA, EM_DILIGENCIA, APROVADA, APROVADA_COM_RESSALVA, REJEITADA), protocolo, data.
- `ITEM_COMPROVACAO` — número sequencial, lançamento bancário, documento fiscal, comprovante de pagamento, justificativa, rubrica, situação.
- `DILIGENCIA` — solicitação do órgão, prazo, resposta, anexo.

### Regras

- **R11.1** — Numeração sequencial automática por habilitação, sem lacuna e sem reuso.
- **R11.2** — Item só é elegível se tiver documento fiscal aprovado + comprovante de pagamento conciliado + rubrica válida.
- **R11.3** — Relatório físico-financeiro cruza execução financeira com metas atingidas (M12).
- **R11.4** — Exportação no layout de cada órgão (planilha e PDF), e por lote para upload na plataforma.
- **R11.5** — Fechamento exige: saldo zerado ou devolução registrada, todas as contrapartidas entregues, todas as metas justificadas.

### Telas

| Código | Tela | Função |
|---|---|---|
| T11.1 | **Montagem da prestação** | Lista dos itens elegíveis com checklist de pendências por item |
| T11.2 | Dossiê | Geração do PDF consolidado com capa, índice e comprovantes na ordem |
| T11.3 | Diligências | Controle de prazo e resposta |
| T11.4 | Exportador por órgão | Layouts de SALIC, CCPC, Desenvolve Cultura |

---

## M12 — Metas físicas e escopo do projeto

### Objetivo
A prestação de contas não é só financeira. Hoje as metas vivem soltas no rodapé do cronograma.

### Entidade `META`

| Campo | Exemplo |
|---|---|
| `habilitacao` / `projeto_edicao` | Rio Memórias VI |
| `descricao` | Oficinas · Passeios · Livros · Exposição · Museu Virtual |
| `quantidade_aprovada` | 45 · 20 · 1.000 · 1 · 1 |
| `quantidade_executada` | 48 · 11 · 1.000 · 0 · 0 |
| `saldo` | Calculado |
| `justificativa` | "mais oficinas", "tirar a exposição" |
| `evidencia` | Lista de presença, foto, relatório |

Também: público estimado × atingido (ICMS prevê 48.000 pessoas), turmas, alunos, meses de aula.

### Regras

- **R12.1** — Alteração de meta é decisão registrada com data e origem (reunião), e **dispara sugestão de readequação orçamentária**. Caso real: reunião 15/01/2025 — "aumentar mais 30 oficinas, aumentar mais 10 passeios, tirar exposição", que gerou a Readequação em aprovação com remanejamento entre rubricas.
- **R12.2** — Rubrica ligada a meta excluída deve ser zerada com justificativa. Caso real: *"Valor zerado pela exclusão da ação exposição vinculada à rubrica."*

### Telas

| Código | Tela | Função |
|---|---|---|
| T12.1 | Painel de metas | Aprovado × executado × saldo, com semáforo |
| T12.2 | Registro de realização | Lançamento de oficina/passeio realizado com evidência |
| T12.3 | Relatório físico-financeiro | Cruzamento meta × rubrica × valor executado |

---

## M13 — Pessoas, folha, eSocial e rateio

### Objetivo
Trazer para o sistema a dimensão hoje invisível nas planilhas e presente nos 745 XMLs: a folha real da Baluarte e sua alocação aos projetos.

### 13.1 O que os XMLs revelam

| Categoria eSocial | Vínculo | Eventos no acervo |
|---|---|---|
| `101` | Empregado — geral (CLT) | 348 |
| `901` | Estagiário | 85 |
| `701` | Contribuinte individual — diretor / autônomo | 56 |
| `723` | Trabalhador autônomo (RPA) | 42 |

**Volume:** 35–38 pessoas por mês (abr/mai/jun 2026).

**Eventos presentes** (fluxo completo, não apenas totalizadores):

| Evento | Descrição | Qtd |
|---|---|---|
| `S-1200` / `evtRemun` | Remuneração do trabalhador | 109 |
| `S-1210` / `evtPgtos` | Pagamentos de rendimentos | 126 |
| `S-1010` / `evtTabRubrica` | Tabela de rubricas da folha | 3 |
| `S-2190/2200` / `evtAdmissao` | Admissão | 2 |
| `S-2206` / `evtAltContratual` | Alteração contratual | 29 |
| `S-2230` / `evtAfastTemp` | Afastamento temporário | 48 |
| `S-2299` / `evtDeslig` | Desligamento | 1 |
| `S-2300/2306` / `evtTSVInicio`, `evtTSVAltContr` | Trabalhador sem vínculo (autônomo/estagiário) | 14 |
| `S-2240` / `evtExpRisco` | Exposição a agentes nocivos (SST) | 24 |
| `S-5001` / `evtBasesTrab` | Bases das contribuições por trabalhador | 111 |
| `S-5003` / `evtBasesFGTS` | Bases de FGTS por trabalhador | 111 |
| `S-5002` / `evtIrrfBenef` | IRRF por beneficiário | 128 |
| `S-5011` / `evtCS`, `evtFGTS` | Consolidado por contribuinte | 12 |
| `S-5012` / `evtIrrf` | IRRF consolidado | 6 |
| `S-1299` / `evtFechaEvPer`, `evtReabreEvPer` | Fechamento e reabertura de período | 9 |
| `evtExclusao` | Exclusão de evento | 9 |

### 13.2 Por que isso importa para os projetos

Pessoas da folha CLT da Baluarte são **alocadas a rubricas de projeto**. Evidência direta no cronograma da LIC:

- Rubrica 10 — *"Folha de pagamento Mariane Freitas (líquido + IR) Fevereiro a Dezembro 2025"* — R$ 40.628,70
- Rubrica 15 — *"Folha de pagamento Mariana Rodrigues (líquido + IR) Fevereiro a Agosto 2025"* — R$ 75.313,67
- Rubrica 36 — *"Folha de Pagamento Andrezza Soares (líquido + IR) Fevereiro 2025 a Janeiro 2026"* — R$ 65.480,64
- Rubrica 76 — *"Folha de Pagamento Luiza Ferraz (líquido + IR)"* — R$ 52.850,54
- Rubricas 84 e 85 — Recolhimentos FGTS e INSS, por pessoa

Cada competência gera **até quatro lançamentos separados** no projeto — líquido, IR, INSS e FGTS — com datas de previsão e execução distintas (líquido no dia 1º, tributos no dia 20). Hoje isso é uma matriz manual de 48 colunas por pessoa na aba `Rem Balu LIC`.

Além disso, existe a distinção **pagamento direto pelo projeto × reembolso à Baluarte**: a Baluarte adianta a folha e o projeto reembolsa. As duas formas aparecem no resumo mensal da aba (`Pagamento direto folha` × `Reembolso Baluarte`).

### 13.3 Entidades

#### `COLABORADOR`
Pessoa física, CPF, matrícula, categoria eSocial, cargo, data de admissão/desligamento, vínculo (CLT, estagiário, autônomo, contribuinte individual).

#### `FOLHA_COMPETENCIA`
Competência, colaborador, líquido, IR, INSS, FGTS, outros descontos, data prevista e data efetiva de cada componente. **Importado** do eSocial/sistema de folha.

#### `RATEIO_FOLHA`
| Campo | |
|---|---|
| `folha_competencia` | FK |
| `habilitacao` | FK (a lei que custeia) |
| `rubrica` | FK |
| `percentual` ou `valor` | |
| `componente` | LIQUIDO · IR · INSS · FGTS |
| `forma` | PAGAMENTO_DIRETO · REEMBOLSO_BALUARTE |
| `conta_teto` | bool — **na LIC, folha NÃO conta para o teto de remuneração do proponente** |

### 13.4 Regras

- **R13.1** — Importação do XML eSocial cria/atualiza `COLABORADOR` e `FOLHA_COMPETENCIA` automaticamente. Chave: CPF + competência + tipo de evento.
- **R13.2** — Um colaborador pode ser rateado entre **N habilitações** e entre projeto e institucional. A soma dos percentuais de rateio por competência deve ser 100%.
- **R13.3** — O rateio gera parcelas em M06 (uma por componente), que consomem saldo da rubrica e passam pelo motor de compliance.
- **R13.4** — **Regra crítica de teto**: na LIC, a remuneração do proponente é calculada apenas sobre notas fiscais; folha não entra. O sistema precisa dessa flag por lei, senão o cálculo do teto fica errado. Evidência: *"O limite é baseado em NFs, não conta folha de pagamento"* — R$ 240.179,53 de NF contra teto de R$ 300.681,22, com R$ 325.584,15 adicionais de folha fora do teto.
- **R13.5** — Reembolso à Baluarte é lançamento institucional (contas a receber) **e** despesa de rubrica no projeto, com documento de ponte.
- **R13.6** — Autônomos (categorias 701/723) geram **RPA** com retenções de INSS e IR, cujo recolhimento vira parcela filha (guia).
- **R13.7** — Alerta de afastamento/desligamento que impacte rubrica em execução (a rubrica ficará com saldo não executado e pode exigir readequação). Caso real: Mariana Rodrigues, prevista fev–dez, executada fev–ago.

### 13.5 Telas

| Código | Tela | Função |
|---|---|---|
| T13.1 | **Importador eSocial** | Upload de lote de XMLs, parsing, log de eventos por tipo, conciliação com colaboradores |
| T13.2 | Colaboradores | Cadastro com vínculo, categoria e histórico de eventos |
| T13.3 | **Matriz de rateio de folha** | Colaborador × competência × habilitação × rubrica × componente. Substitui a `Rem Balu LIC` |
| T13.4 | Resumo mensal de remuneração | Pagamento direto folha · Reembolso Baluarte · NF produção · NF captação — previsto × executado (espelho do rodapé da aba) |
| T13.5 | Painel de teto de remuneração | Por lei, separando o que conta e o que não conta |

---

## M14 — Institucional

### Objetivo
O caixa da própria empresa, **apartado dos projetos** (P1).

### 14.1 Receitas identificadas nos Livros Fiscais

| Natureza | Exemplo real (abr–jun/2026) |
|---|---|
| Consultoria de planejamento de projeto | *"Consultoria de planejamento de PROJETO a ser apresentado no âmbito da Lei Rouanet"* — R$ 25.000 |
| Gestão de programa de editais corporativos | *"Apoio à gestão do Programa de Editais Transformando Energia em Cultura e na implementação do NAP Cultura (Núcleo de Acompanhamento de Projetos). Assinatura do contrato, parcela 1/5"* — R$ 176.000 |
| Consultoria de projetos incentivados | *"Prestação de serviços de gestão e monitoramento de projetos culturais da contratante"* — R$ 39.200 |
| Cursos livres | *"Comercialização de Cursos Livres, Estúdio Escola de Animação (março/26)"* — R$ 1.103,04 |
| Remuneração dentro de projeto (NF contra o projeto) | Produção 30%, Captação 10%, LICC 15% |

**Faturamento:** abr/2026 R$ 778.445,93 · mai R$ 406.556,05 · jun R$ 557.539,89. ISS 5%.

### 14.2 Modelo de remuneração sobre projetos

Extraído da aba `EXECUÇÃO 2025`:

| Linha | % | Base | Valor |
|---|---|---|---|
| Baluarte — produção | 30% | 1.710.749,02 | 504.654,01 |
| Baluarte — captação | 10% | 1.443.099,56 | 111.134,92 |
| Baluarte — LICC | 15% | 499.998,00 | 74.999,70 |
| Baluarte — comunicação | — | — | 23.593,53 |
| Copa Studio (parceiro) | 10% | 390.761,04 | 39.076,00 |
| Atrom — produção LICC | 5% | 499.998,00 | 24.999,90 |
| Captador externo | 10% | 174.527,79 | 17.452,78 |

### 14.3 Entidades

- `CONTRATO_GESTAO` — cliente, objeto, modelo de remuneração (% sobre captação, % sobre produção, valor fixo, parcelas), vigência, projetos vinculados.
- `FATURAMENTO` — geração de NF de serviço a partir do contrato ou do avanço do projeto.
- `CONTA_RECEBER` / `CONTA_PAGAR` institucional.
- `NFSE_TOMADA` — importação dos XMLs `UneNFSe_TomadosAbrasf` para contas a pagar e apuração de ISS retido.

### 14.4 Regras

- **R14.1** — Nenhum relatório padrão soma caixa institucional com caixa de projeto.
- **R14.2** — A NF emitida contra um projeto gera automaticamente: receita institucional + parcela no plano de ação do projeto + consumo de teto de remuneração.
- **R14.3** — O reconhecimento de receita de honorário sobre captação acompanha o **repasse efetivo**, não o termo de compromisso.
- **R14.4** — Importação do XML de NFS-e tomada alimenta contas a pagar e o livro de serviços tomados.

### 14.5 Telas

| Código | Tela | Função |
|---|---|---|
| T14.1 | Contratos de gestão | Modelo de remuneração e projetos vinculados |
| T14.2 | Faturamento | Geração de NF de serviço, com o cálculo do % sobre a base |
| T14.3 | Contas a receber / a pagar institucionais | Fluxo próprio, isolado dos projetos |
| T14.4 | **Ponte projeto ↔ institucional** | Relatório auditável de tudo que a Baluarte recebeu de cada projeto, por natureza |
| T14.5 | Importação de NFS-e tomadas | Upload/consulta do XML ABRASF |

---

## M15 — Painéis e BI

| Código | Painel | Conteúdo |
|---|---|---|
| T15.1 | **Saúde do projeto** | Por habilitação: captado × aprovado (% captação), executado × captado, saldo, dias até o fim da vigência, metas, pendências de prestação |
| T15.2 | **Consumo de tetos** | Barras por tipo de teto, por lei, com folga em R$ e % |
| T15.3 | **Fluxo de caixa por conta vinculada** | Repasses previstos, parcelas a pagar, saldo projetado, alerta de insuficiência |
| T15.4 | **Funil de captação** | Prospect → termo → direcionado → repassado, por patrocinador e por projeto |
| T15.5 | **Produtividade da equipe** | Parcelas por status, tempo médio em cada etapa, gargalos (Alterar → Em aprovação → Inserido → Conferido) |
| T15.6 | **Rentabilidade institucional** | Receita por contrato/projeto × custo de equipe alocada |
| T15.7 | **Riscos e devoluções** | Estouros de teto, rendimento não utilizável, sobras a devolver, diligências abertas |
| T15.8 | **Calendário regulatório** | Vencimentos de prestação, prazos de diligência, vigências a expirar |

---

## M16 — Workflow, alçadas e segurança

### Objetivo
Reproduzir como controle de sistema a segregação de funções que hoje é convenção.

### Fluxos

**Fluxo do pagamento**
```
Enviar dados → Dados enviados / RPA enviado → Pagar → Pago → Cadastrado → Comprov enviado
```

**Fluxo de aprovação na plataforma do órgão** (padrão observado no ICMS / Desenvolve Cultura)

| Status | Significa | Quem executa |
|---|---|---|
| Alterar | Precisa de ajuste no documento | Analista |
| Em aprovação | Conferido pela coordenação, enviado à gerência | Coordenadora |
| Subir no sistema | Liberado pela gerência, pode subir | Analista |
| Inserido | Inserido na plataforma do órgão | Estagiário |
| Conferido | Conferido pela coordenação na plataforma | Coordenadora |

### Regras

- **R16.1** — **Maker–checker obrigatório**: quem cria ou executa não pode aprovar. Duas conferências: antes de subir e depois de inserido.
- **R16.2** — Alçada por valor e por tipo de exceção (estouro de teto exige diretoria).
- **R16.3** — Trilha de auditoria imutável em todas as entidades financeiras: quem, quando, valor antes/depois, motivo.
- **R16.4** — Perfil de acesso por projeto: usuário só vê os projetos aos quais está alocado.
- **R16.5** — Dados de terceiros (proponentes como Copa Studio, Associação Rio Memórias, Atrom) só visíveis a quem tem vínculo com o projeto.
- **R16.6** — Notificações: parcela vencendo, teto em 80%, diligência com prazo, repasse não recebido, meta em risco.

---

## 21. Modelo de dados

### 21.1 Diagrama textual

```
PROJETO ─┬─ EDICAO ─┬─ HABILITACAO ─┬─ CONTA_VINCULADA ─── LANCAMENTO_BANCARIO
         │          │               │                              │
         │          │               ├─ ORCAMENTO_VERSAO ── ORCAMENTO_LINHA
         │          │               │                              │
         │          │               ├─ TERMO_COMPROMISSO ─┬─ DIRECIONAMENTO
         │          │               │                     └─ REPASSE ────┐
         │          │               │                                    │
         │          │               ├─ RENDIMENTO                        │
         │          │               ├─ REGRA_TETO (herda de LEI)         │
         │          │               └─ PRESTACAO_CONTAS ── ITEM_COMPROVACAO
         │          │                                              │
         │          ├─ EXECUCAO_LINHA ── EXECUCAO_RATEIO ──────────┤
         │          └─ META                                        │
         │                                                         │
         └─ CONTRAPARTIDA                                          │
                                                                   │
FORNECEDOR ─┬─ CONTRATO ─── PARCELA ───────────────────────────────┘
            └─ DOCUMENTO_FISCAL ── RETENCAO ── PARCELA (filha: guia)

COLABORADOR ── FOLHA_COMPETENCIA ── RATEIO_FOLHA ── PARCELA

LEI_INCENTIVO ─┬─ PLANO_RUBRICA ── RUBRICA_MODELO
               ├─ REGRA_TETO
               ├─ TEMPLATE_TEXTO
               └─ TABELA_REFERENCIA_PRECO

--- INSTITUCIONAL (contexto separado) ---
CLIENTE ── CONTRATO_GESTAO ── FATURAMENTO ── CONTA_RECEBER
FORNECEDOR ── NFSE_TOMADA ── CONTA_PAGAR
```

### 21.2 Chaves e dimensões obrigatórias

Toda entidade financeira do lado projeto carrega, obrigatoriamente:

| Dimensão | Por quê |
|---|---|
| `habilitacao_id` | Isola o dinheiro por projeto **e** por lei |
| `rubrica_id` | Base de todo controle orçamentário e da prestação |
| `execucao_linha_id` | Liga ao orçamento real (opcional para lançamentos administrativos) |
| `conta_vinculada_id` | Determina o extrato e o saldo |
| `competencia` | Base do controle mensal e do rateio de folha |

---

## 22. Catálogo de regras parametrizáveis

| ID | Regra | Tipo | Parâmetro |
|---|---|---|---|
| R-TETO-01 | Agenciamento/captação ≤ % do orçamento específico | Bloqueio | 5% (ISS) |
| R-TETO-02 | Captação ≤ % e ≤ valor absoluto | Bloqueio | 10% e R$ 50.000 (ICMS) |
| R-TETO-03 | Custos administrativos ≤ % do total | Bloqueio | 10% |
| R-TETO-04 | Divulgação/mídia ≤ % do total | Bloqueio | 20% (ICMS) |
| R-TETO-05 | Remuneração do proponente ≤ % ou valor | Bloqueio | 20% (ISS) / absoluto (LIC) |
| R-TETO-06 | Remuneração de fornecedores ≤ % | Alerta | 30% (ISS) |
| R-TETO-07 | Folha conta para o teto de remuneração? | Flag | Não, na LIC |
| R-REM-01 | Remanejamento por rubrica sem readequação | Alerta | 20% |
| R-REM-02 | Remanejamento do total sem readequação | Alerta | 20% |
| R-ORC-01 | Σ rateio = valor da linha de execução | Bloqueio | — |
| R-ORC-02 | Σ execução por lei ≤ captado + rendimento autorizado | Bloqueio | — |
| R-ORC-03 | Alteração de linha exige justificativa | Bloqueio | Quando difere da versão anterior |
| R-ORC-04 | Valor exige referência de preço | Bloqueio | LICC |
| R-REND-01 | Uso de rendimento | Bloqueio | NÃO (LIC) / AUTORIZAÇÃO (ISS) |
| R-REND-02 | Rendimento posterior à autorização é não utilizável | Automático | — |
| R-DOC-01 | Código de serviço compatível com a rubrica | Alerta | — |
| R-DOC-02 | Texto obrigatório da nota conferido | Bloqueio na aprovação | Template por lei |
| R-DOC-03 | MEI usa sistema nacional de NFS-e | Alerta | — |
| R-PAG-01 | Parcela paga exige conciliação bancária | Bloqueio | — |
| R-PAG-02 | Retenção gera parcela filha (guia) | Automático | — |
| R-PC-01 | Item de comprovação exige documento + comprovante + rubrica | Bloqueio | — |
| R-PC-02 | Numeração sequencial sem lacuna | Automático | Por habilitação |
| R-WF-01 | Maker–checker | Bloqueio | Criador ≠ aprovador |
| R-CAP-01 | Receita de honorário sobre captação reconhecida no repasse | Automático | — |
| R-SEG-01 | Não somar caixa institucional com caixa de projeto | Bloqueio | — |

---

## 23. Matriz de perfis e permissões

| Perfil | Cria | Aprova | Consulta | Observações |
|---|---|---|---|---|
| **Diretoria** (sócia/diretora) | Contratos, orçamento | Estouro de teto, readequação, prestação | Tudo | Assina como representante legal |
| **Gerência** | — | Pagamento, subida à plataforma | Todos os projetos | Libera para o sistema do órgão |
| **Coordenação** | Orçamento, plano de ação, readequação | Documento, conferência na plataforma | Projetos alocados | Duplo papel de conferência |
| **Analista** | Parcela, documento, correção | — | Projetos alocados | Executa e corrige |
| **Estagiário** | — | — | Projetos alocados | Insere na plataforma do órgão |
| **Financeiro** | Lançamento bancário, conciliação | — | Financeiro de todos | Não altera orçamento |
| **Contabilidade** (externo) | — | — | Somente leitura fiscal | Acesso restrito |
| **Proponente terceiro** | — | — | Somente seu projeto | Portal restrito (fase 2) |

---

## 24. Integrações

| # | Integração | Direção | Prioridade | Observação |
|---|---|---|---|---|
| I1 | **eSocial (XML)** | Entrada | Alta | Já disponível: eventos S-1200, S-1210, S-2200, S-2299, S-5001, S-5002, S-5003, S-5011, S-5012. Parsing e conciliação por CPF+competência |
| I2 | **NFS-e tomadas (ABRASF/Ginfes XML)** | Entrada | Alta | Já disponível no acervo |
| I3 | **Retorno bancário (OFX/CNAB 240)** | Entrada | Alta | Banco do Brasil, Bradesco, Banestes, BTG |
| I4 | **Remessa de pagamentos (CNAB / PIX em lote)** | Saída | Alta | A partir da fila aprovada |
| I5 | **Emissão de NFS-e** | Saída | Média | Prefeitura do Rio (institucional) |
| I6 | **SALIC / Rouanet** | Saída | Média | Exportação em lote no layout do órgão + registro do identificador retornado pela plataforma no campo `id_plataforma` |
| I7 | **Desenvolve Cultura (SECEC)** | Saída | Média | Idem, com o fluxo de status da plataforma (Alterar → Em aprovação → Subir → Inserido → Conferido) controlado no sistema |
| I8 | **CCPC / Profilm** | Saída | Média | Idem |
| I9 | **Trello** | Entrada | Média | Webhook de card novo cria solicitação de pagamento em rascunho. A tela T06.3 é o canal nativo; o Trello permanece como porta de entrada alternativa |
| I10 | **Consulta CNPJ/CPF** | Entrada | Baixa | Validação de cadastro e situação |
| I11 | **Excel** | Saída | Alta | Exportação no layout atual das planilhas, para uso externo e transição |

---

## 25. Migração de dados

### 25.1 Escopo da carga inicial

| Onda | Conteúdo | Origem |
|---|---|---|
| 1 | Leis, planos de rubrica, domínios, templates | Abas `Dados` + formulários das leis |
| 2 | Fornecedores (~120) | Aba `Dados` das planilhas ICMS e LIC |
| 3 | Projetos, edições, habilitações, contas vinculadas | Cabeçalhos dos cronogramas |
| 4 | Orçamentos aprovados, **todas as versões** | Abas `APROV ISS`, `ORC_Aprov`, `ORC LIC`, `ORC ISS`, `ORC LICC` |
| 5 | Orçamento de execução e rateios | Abas `EXECUÇÃO 2024/2025` |
| 6 | Patrocinadores, termos, direcionamentos, repasses | Aba `Desembolso_CI` + resumos financeiros |
| 7 | Plano de ação (parcelas históricas) | Abas `Plano de ação *` |
| 8 | Extratos e conciliações | Abas `CONTROLE CC` |
| 9 | Rendimentos | Abas `CONTROLE APLICACAO` |
| 10 | Colaboradores e histórico de folha | XMLs eSocial + aba `Rem Balu LIC` |
| 11 | Metas | Rodapés dos cronogramas |

### 25.2 Cuidados

- **Fórmulas quebradas**: há `#REF!` nas abas `Memória de Calculo_Valido` e `Rem Balu LIC`. Esses valores **não são migrados**; o saldo correspondente entra por ajuste de abertura documentado.
- **Divergências de rendimento**: as três séries do ICMS não fecham (R$ 1.039.741,65 × R$ 1.023.352,21). A série oficial na migração é a do **extrato bancário**; as demais entram como séries de conferência em M08, e a diferença é registrada como divergência aberta na tela T08.2.
- **Nomes de fornecedor inconsistentes**: a mesma pessoa aparece como "Cau", "Cau 4o", "Claudio Santos", "50.652.249 CLAUDIO SANTOS DINIZ DA SILVA". Deduplicação assistida por CNPJ/CPF, com tela de fusão de cadastros e manutenção dos apelidos como `nome_fantasia`.
- **Abas ocultas** (`Memória de Calculo_Valido`, `Remuneração Baluarte (2)`, `ORC_Execução`) contêm histórico de 2018 e cálculos legados. **Ficam fora da migração**; os arquivos originais são guardados como anexo documental do projeto.
- **Nomenclatura de arquivo**: o nome `Crono desembolso_ISS_231.266,23.xlsx` diverge do total real da aba (R$ 531.266,23). A migração usa sempre o **valor apurado na aba**, nunca o nome do arquivo.

---

## 26. Requisitos não-funcionais

| # | Requisito | Detalhe |
|---|---|---|
| NF1 | **Auditoria total** | Log imutável de toda alteração em entidade financeira: usuário, timestamp, antes/depois, motivo. Retenção mínima de 10 anos (prazo de guarda de prestação de contas) |
| NF2 | **Guarda documental** | Armazenamento de PDF e XML com integridade verificável (hash), organizado por projeto/lei/rubrica |
| NF3 | **Precisão numérica** | Todos os valores monetários em decimal com 2 casas; arredondamento explícito e consistente. As planilhas têm resíduos de centavo (`-0,00`, `-0,01`) que não podem se propagar |
| NF4 | **Multiempresa / multi-proponente** | Isolamento lógico por proponente |
| NF5 | **Desempenho** | Cronograma de desembolso com ~700 linhas e drill-down deve abrir em menos de 3s |
| NF6 | **Exportação** | Todo painel exportável em Excel e PDF; layouts oficiais por órgão |
| NF7 | **Disponibilidade** | Uso em horário comercial; janela de manutenção fora do fechamento mensal (dias 1–10 e 20–25 são críticos por causa do calendário de folha e tributos) |
| NF8 | **LGPD** | Dados de pessoas físicas (CPF, dados bancários, folha) com controle de acesso, minimização e trilha de consulta |
| NF9 | **Usabilidade em grade** | A equipe vem do Excel: as telas principais precisam de edição em linha, cópia/cola, filtros e atalhos de teclado |
| NF10 | **Idempotência de importação** | Reimportar o mesmo XML ou retorno bancário não duplica registros |

---

## 27. Mapeamento sobre Sankhya: padrão × personalização

Implantação sobre Sankhya. A coluna "padrão" indica o que o produto já resolve; a coluna "personalização" define o que o protótipo constrói.

| Necessidade | Sankhya padrão | Personalização necessária |
|---|---|---|
| Cadastro de parceiros (fornecedores, patrocinadores) | ✅ Parceiro | Campos de natureza MEI/RPA, código de serviço habitual |
| Contas a pagar / a receber | ✅ Financeiro | Dimensão obrigatória `habilitação` + `rubrica` |
| Contas bancárias e conciliação | ✅ Tesouraria | Conta vinculada por projeto, bloqueio de saldo consolidado |
| Centro de custo / projeto | ✅ CenCus / Projeto | **Insuficiente** — precisa de entidade Habilitação com orçamento versionado e tetos |
| Orçamento | ⚠️ Parcial | **Novo módulo**: versionamento com justificativa por linha, estrutura variável por lei, rateio multi-lei |
| Motor de tetos | ❌ | **Novo** — regras parametrizáveis com bloqueio no lançamento |
| Plano de ação | ⚠️ | Adaptação do contas a pagar com status próprios e parcela filha de imposto |
| Documentos fiscais | ✅ Nota / XML | Validação de código de serviço vs rubrica; texto obrigatório; substituição |
| Folha e eSocial | ⚠️ | Importação de XML e **matriz de rateio folha → rubrica** (novo) |
| Prestação de contas | ❌ | **Novo módulo** — numeração, dossiê, exportação por órgão |
| Metas físicas | ❌ | **Novo** |
| Workflow e alçadas | ✅ Workflow Sankhya | Configuração dos fluxos maker–checker |
| BI | ✅ | Modelagem dos painéis |

**Resumo:** aproximadamente 45% aproveitável do padrão (financeiro, parceiros, fiscal, workflow, BI), 25% adaptação e 30% desenvolvimento novo — concentrado em Orçamento multi-lei, Motor de tetos, Prestação de contas e Rateio de folha.

---

## 28. Roadmap de implantação

### Onda 1 — Fundação e controle financeiro do projeto *(estimativa: 3 meses)*
Objetivo: sair da planilha para o sistema no dia a dia.
- M01 Fundação · M02 Projetos e habilitações · M05 Fornecedores
- M06 Plano de ação · M07 Tesouraria e conciliação
- Migração das ondas 1–3 e 7–8
- **Entregável:** a equipe para de digitar pagamento em planilha

### Onda 2 — Orçamento e compliance *(3 meses)*
Objetivo: o controle deixa de ser corretivo.
- M03 Orçamento (aprovado versionado + execução + rateio)
- M09 Motor de compliance e tetos
- Cronograma de desembolso gerado (T03.7)
- Migração das ondas 4–5
- **Entregável:** estouro de teto vira bloqueio, não devolução

### Onda 3 — Captação, rendimentos e documentos *(2 meses)*
- M04 Captação · M08 Aplicações e rendimentos · M10 Documentos fiscais
- Integrações I2 (NFS-e tomada) e I3/I4 (bancário)
- Migração das ondas 6 e 9
- **Entregável:** conciliação de rendimento resolvida; fim da divergência

### Onda 4 — Pessoas e institucional *(2 meses)*
- M13 Pessoas, eSocial e rateio · M14 Institucional
- Integração I1 (eSocial)
- Migração da onda 10
- **Entregável:** a matriz de 48 colunas some

### Onda 5 — Prestação de contas e inteligência *(2 meses)*
- M11 Prestação de contas · M12 Metas · M15 Painéis
- Exportadores por órgão
- **Entregável:** dossiê gerado, não montado

### Onda 6 — Órgãos e portal do proponente *(2 meses)*
- I6/I7/I8: exportadores em lote no layout de cada órgão e controle do status na plataforma
- Portal de consulta para proponentes terceiros (Copa Studio, Associação Rio Memórias, Atrom), somente leitura do próprio projeto
- **Entregável:** o proponente terceiro consulta sozinho, sem pedir planilha

---

## 29. Definições adotadas para o protótipo

Pontos que o acervo não explicita e que o protótipo resolve por parametrização — todos alteráveis em cadastro, sem desenvolvimento.

| # | Ponto | Definição adotada |
|---|---|---|
| D1 | Marcadores `RN` / `RD` nas rubricas do SALIC | Modelados como `CLASSIFICACAO_RUBRICA`, lista parametrizável por lei. O protótipo carrega os dois códigos e os exibe no cronograma da LIC; o significado é atributo de cadastro, não regra de sistema |
| D2 | `ID Profilm` no plano de ação do ISS | Tratado como `id_plataforma` — identificador genérico do documento na plataforma do órgão, com rótulo configurável por lei |
| D3 | Regra de rendimento na LICC | `SIM_COM_AUTORIZACAO`, o parâmetro mais restritivo. Ajustável no cadastro da lei |
| D4 | Fronteira com o institucional | O sistema cobre faturamento, contas a receber e contas a pagar institucionais (M14). O **cálculo** da folha permanece externo; o sistema consome o resultado via XML eSocial |
| D5 | Canal de solicitação de pagamento | A tela T06.3 é o canal nativo. O Trello continua funcionando como porta de entrada via webhook, criando rascunho |
| D6 | Conciliação bancária | Por retorno bancário (OFX/CNAB 240) com match automático, e conciliação manual assistida como alternativa. Bancos do protótipo: Banco do Brasil, Bradesco, Banestes e BTG |
| D7 | Integração com SALIC, CCPC e Desenvolve Cultura | Exportação em lote no layout de cada órgão + controle do status da plataforma dentro do sistema. Sem dependência de API |
| D8 | Alocação de folha a rubricas | **Percentual fixo por colaborador e competência**, definido na matriz T13.3, com possibilidade de sobrescrita manual por lançamento. A soma dos percentuais por competência precisa fechar 100% |
| D9 | Dimensionamento | Base do acervo: 5 habilitações ativas, 4 leis, 3 proponentes terceiros, ~120 fornecedores, ~38 pessoas na folha. O protótipo é construído para 5× esse volume |
| D10 | Série oficial de rendimento | Extrato bancário. As séries de Baluarte e contabilidade entram como conferência, com a divergência visível em T08.2 |
| D11 | Histórico legado (abas ocultas, 2018) | Fora da migração. Arquivos originais guardados como anexo documental |
| D12 | Acesso de proponentes terceiros | Portal somente leitura do próprio projeto, na Onda 6, com isolamento por vínculo (R16.5) |

---

*Documento elaborado a partir da leitura integral do acervo: `Deploy.pdf`, `Planilha.pdf`, `Livros Fiscais Baluarte Agencia.pdf`, as 32 abas das 5 planilhas de gestão orçamentária e os 748 XMLs (745 eventos eSocial + 3 NFS-e tomadas ABRASF). Todas as citações, valores e regras reproduzem conteúdo dos arquivos.*
