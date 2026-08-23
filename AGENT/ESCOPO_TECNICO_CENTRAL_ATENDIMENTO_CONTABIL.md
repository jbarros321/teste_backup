# ESCOPO TÉCNICO-FUNCIONAL
## Agente Sankhya Mitra — "Central de Atendimento Contábil"

**Versão:** 1.0
**Data:** 18/08/2026
**Plataforma-alvo:** Sankhya Om / Mitra (Agentes de IA)
**Documento base:** `AGENT/ESCOPO.MD`

---

## SUMÁRIO

1. Visão geral e objetivo
2. Escopo (incluído / não incluído)
3. Atores, papéis e permissões
4. Arquitetura da solução
5. Modelo de dados (tabelas customizadas)
6. Taxonomia: categorias, subcategorias e campos exigidos
7. Máquina de estados (status) e regras de transição
8. Priorização automática e SLA
9. Roteamento e distribuição de chamados
10. Ferramentas (tools) do agente — contratos de entrada/saída
11. Instruções do agente (system prompt operacional)
12. Fluxos conversacionais detalhados
13. Interfaces (telas) e componentes
14. Anexos
15. Regras de segurança e guardrails
16. Notificações e integrações
17. Analytics e indicadores
18. Critérios de aceite
19. Plano de entrega em fases
20. Riscos, premissas e pendências de decisão

---

## 1. VISÃO GERAL E OBJETIVO

Criar um agente de atendimento interno, chamado **"Central de Atendimento Contábil"**, que funcione como ponto único de entrada para solicitações dirigidas à área Contábil/Fiscal dentro do Sankhya.

**Problema atual:** solicitações chegam por canais dispersos (WhatsApp, e-mail, ligação, corredor), sem padronização, sem rastreabilidade e frequentemente incompletas — obrigando a equipe contábil a gastar tempo em idas e vindas apenas para levantar dados básicos (empresa, período, número do documento, motivo).

**Objetivo da solução:**
- Permitir que **qualquer usuário do Sankhya**, sem conhecimento contábil ou técnico, abra uma solicitação em poucos passos.
- Fazer com que **o agente execute o trabalho de interpretação e coleta**, e não o usuário preenchendo formulário.
- Entregar à equipe contábil solicitações **completas, classificadas, priorizadas e rastreáveis**.
- Gerar base de dados estruturada para análise gerencial do atendimento.

**Princípio de design central:** o usuário não deve perceber que está preenchendo um formulário. A coleta de dados acontece por conversa guiada, com perguntas mínimas e nunca repetidas.

---

## 2. ESCOPO

### 2.1 Incluído

| # | Item |
|---|---|
| 1 | Tela inicial com 8 cards de categoria ("Como podemos ajudar?") |
| 2 | Agente conversacional de triagem, coleta e classificação |
| 3 | Identificação automática de categoria/subcategoria/entidades a partir de texto livre |
| 4 | Confirmação de resumo antes do registro |
| 5 | Registro de chamado com identificador único (`#CTB-000000`) |
| 6 | Modelo de dados de chamados, histórico, detalhes dinâmicos e anexos |
| 7 | Tela de histórico de chamados do usuário e detalhamento |
| 8 | Máquina de estados com 7 status e regras de transição |
| 9 | Classificação automática de prioridade (4 níveis) e cálculo de SLA |
| 10 | Roteamento por categoria/subcategoria/empresa para responsável ou fila |
| 11 | Ferramentas de consulta somente-leitura ao Sankhya (pedido, NF, período, TOP, parceiro, empresa) |
| 12 | Upload e vinculação de anexos |
| 13 | Guardrails de segurança (proibição de execução sem autorização/confirmação) |
| 14 | Painel analítico com os indicadores da seção 17 |

### 2.2 Não incluído (fora do escopo desta entrega)

- Execução automática de ações transacionais no Sankhya (liberar pedido, alterar pedido, excluir pedido, liberar período contábil, criar conta contábil/natureza). Nesta fase o agente **apenas registra e encaminha**. Ver seção 19 (Fase 4) para habilitação futura controlada.
- Portal externo / acesso de usuários fora do Sankhya.
- Integração com ferramenta externa de ITSM (Jira, ServiceNow, Zendesk).
- Aprovação por workflow multinível fora do Sankhya.
- Atendimento a áreas fora de Contábil/Fiscal.

---

## 3. ATORES, PAPÉIS E PERMISSÕES

| Papel | Descrição | Permissões |
|---|---|---|
| **Solicitante** | Qualquer usuário do Sankhya | Abrir chamado; consultar apenas os próprios chamados; responder solicitações de informação; anexar arquivos; cancelar chamado próprio enquanto em `NOVO` |
| **Atendente Contábil** | Analista da equipe contábil/fiscal | Visualizar chamados da(s) sua(s) fila(s); assumir; alterar status; solicitar informação; registrar solução; anexar; reclassificar categoria/prioridade |
| **Gestor Contábil** | Coordenação/gerência | Tudo do Atendente + reatribuir responsável; alterar prioridade e SLA; cancelar qualquer chamado; acesso total ao painel analítico |
| **Aprovador** | Responsável por autorização (ex.: liberação de período) | Aprovar/reprovar chamados em `AGUARDANDO APROVAÇÃO` |
| **Administrador** | TI / Analista de sistema | Manutenção de categorias, subcategorias, campos exigidos, roteamento, SLA e configuração do agente |

**Regra de visibilidade:** o Solicitante enxerga somente chamados onde `CODUSUSOLIC = usuário logado` OU onde é o `CODUSURESP` informado. Gestor e Administrador enxergam tudo. A restrição deve ser aplicada **no nível da ferramenta de consulta**, nunca apenas na camada de prompt.

---

## 4. ARQUITETURA DA SOLUÇÃO

```
┌──────────────────────────────────────────────────────────┐
│  Camada de Apresentação (Sankhya Om)                     │
│  • Tela inicial de cards (HTML5 Component / Dashboard)   │
│  • Painel de conversa do agente Mitra                    │
│  • Tela de Histórico de Chamados (grid + detalhe)        │
│  • Painel Analítico (BI / Dashboard Sankhya)             │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────┐
│  Agente Mitra "Central de Atendimento Contábil"          │
│  • Instruções (system prompt) — seção 11                 │
│  • Base de conhecimento (taxonomia + FAQ contábil)       │
│  • Tools registradas — seção 10                          │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────┐
│  Camada de Serviços (ServiceProvider / Regras Java)      │
│  • Validação de campos obrigatórios por subcategoria     │
│  • Geração de numeração sequencial CTB-######            │
│  • Classificação de prioridade e cálculo de SLA          │
│  • Roteamento e atribuição                               │
│  • Máquina de estados / trilha de auditoria              │
│  • Envio de notificações                                 │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────┐
│  Persistência                                            │
│  • Tabelas AD_CTB* (chamados, histórico, detalhes, anexos)│
│  • Entidades Sankhya padrão (somente leitura pelo agente):│
│    TGFCAB, TGFITE, TGFPAR, TGFTOP, TGFNAT, TSIEMP,       │
│    TSIUSU, plano de contas e centros de resultado        │
└──────────────────────────────────────────────────────────┘
```

**Decisões arquiteturais:**
1. **O agente não escreve direto em tabela.** Toda gravação passa por ferramenta que invoca serviço com validação — garante integridade mesmo se o modelo "alucinar" um payload.
2. **Toda consulta ao Sankhya é somente-leitura e parametrizada.** Nenhuma tool aceita SQL livre gerado pelo modelo.
3. **A taxonomia é dado, não prompt.** Categorias, subcategorias e campos exigidos ficam em tabela e são lidos pela tool `listar_taxonomia`, permitindo manutenção sem reescrever o agente.

---

## 5. MODELO DE DADOS

> Nomenclatura sugerida; ajustar ao padrão de nomes vigente do ambiente.

### 5.1 `AD_CTBCHAM` — Chamado (cabeçalho)

| Campo | Tipo | Obrig. | Descrição |
|---|---|---|---|
| `NUCHAMADO` | INT (PK, identity) | S | Chave interna sequencial |
| `NUMCHAMADO` | VARCHAR(14) | S | Identificador exibido: `CTB-000125` (único) |
| `DTABERTURA` | DATETIME | S | Data/hora de abertura |
| `CODUSUSOLIC` | INT | S | Usuário solicitante (FK TSIUSU) |
| `CODEMP` | INT | S | Empresa referente à solicitação (FK TSIEMP) |
| `CODCATEG` | INT | S | Categoria (FK AD_CTBCAT) |
| `CODSUBCAT` | INT | S | Subcategoria (FK AD_CTBSUB) |
| `ASSUNTO` | VARCHAR(150) | S | Título gerado pelo agente (resumo em uma linha) |
| `DESCRICAO` | VARCHAR(4000) | S | Descrição consolidada da solicitação |
| `STATUS` | VARCHAR(25) | S | Ver seção 7 |
| `PRIORIDADE` | VARCHAR(10) | S | URGENTE / ALTA / NORMAL / BAIXA |
| `PRIORIDADESUG` | VARCHAR(10) | N | Prioridade sugerida pelo agente (para auditoria de acerto) |
| `CODUSURESP` | INT | N | Responsável atual |
| `CODFILA` | INT | N | Fila/grupo de atendimento |
| `DTPREVISTA` | DATETIME | N | Prazo (SLA) calculado |
| `DTPRIMRESP` | DATETIME | N | Data/hora da primeira resposta |
| `DTCONCLUSAO` | DATETIME | N | Data/hora de conclusão |
| `SOLUCAO` | VARCHAR(4000) | N | Solução aplicada |
| `TEMPOATEND` | DECIMAL(10,2) | N | Horas úteis do atendimento (calculado) |
| `ORIGEM` | CHAR(1) | S | `C` = card, `L` = texto livre |
| `DTALTER` | DATETIME | S | Última atualização |
| `CANCMOTIVO` | VARCHAR(500) | N | Motivo de cancelamento |

Índices: `NUMCHAMADO` (único), (`CODUSUSOLIC`,`DTABERTURA`), (`STATUS`,`CODUSURESP`), (`CODCATEG`,`DTABERTURA`).

### 5.2 `AD_CTBDET` — Detalhes dinâmicos (chave/valor)

Armazena os campos específicos coletados por subcategoria (ex.: `NUMPEDIDO`, `MUNICIPIO`, `ALIQUOTA`) sem exigir alteração de estrutura a cada nova subcategoria.

| Campo | Tipo | Descrição |
|---|---|---|
| `NUCHAMADO` | INT (PK) | FK `AD_CTBCHAM` |
| `SEQUENCIA` | INT (PK) | Ordem |
| `CAMPO` | VARCHAR(40) | Código do campo (ex.: `NUMPEDIDO`) |
| `ROTULO` | VARCHAR(80) | Rótulo exibido ("Número do pedido") |
| `VALOR` | VARCHAR(500) | Valor informado |
| `TIPO` | CHAR(1) | `T` texto, `N` número, `D` data, `L` lista, `B` booleano |

### 5.3 `AD_CTBHIST` — Histórico / interações

| Campo | Tipo | Descrição |
|---|---|---|
| `NUCHAMADO` | INT (PK) | FK |
| `SEQUENCIA` | INT (PK) | Ordem cronológica |
| `DHEVENTO` | DATETIME | Data/hora |
| `CODUSU` | INT | Autor (usuário ou usuário-sistema do agente) |
| `TIPO` | VARCHAR(20) | `ABERTURA`, `COMENTARIO`, `MUDSTATUS`, `REATRIBUICAO`, `ANEXO`, `SOLICINFO`, `RESPINFO`, `APROVACAO`, `CONCLUSAO`, `CANCELAMENTO`, `RECLASSIF` |
| `DESCRICAO` | VARCHAR(2000) | Texto do evento |
| `VALORANT` | VARCHAR(200) | Valor anterior (quando aplicável) |
| `VALORNOVO` | VARCHAR(200) | Valor novo |
| `VISIVELSOLIC` | CHAR(1) | `S`/`N` — permite notas internas |

### 5.4 `AD_CTBANX` — Anexos

| Campo | Tipo | Descrição |
|---|---|---|
| `NUCHAMADO` / `SEQUENCIA` | INT (PK) | |
| `NOMEARQ` | VARCHAR(255) | Nome original |
| `CHAVEARQ` | VARCHAR(200) | Chave no repositório de arquivos do Sankhya |
| `TAMANHO` | INT | Bytes |
| `MIMETYPE` | VARCHAR(80) | |
| `CODUSU` / `DHUPLOAD` | INT / DATETIME | |

### 5.5 Tabelas de configuração

| Tabela | Conteúdo |
|---|---|
| `AD_CTBCAT` | Categorias: código, nome, descrição, ícone, ordem, ativo |
| `AD_CTBSUB` | Subcategorias: categoria, nome, ativo, fila padrão, prioridade padrão, SLA em horas, exige aprovação (`S`/`N`) |
| `AD_CTBCAMPO` | Campos por subcategoria: código, rótulo, tipo, obrigatório (`S`/`N`/`C`=condicional), ordem, domínio de valores, texto de ajuda |
| `AD_CTBROT` | Roteamento: categoria/subcategoria/empresa → fila ou responsável |
| `AD_CTBSLA` | SLA por prioridade e/ou subcategoria, em horas úteis; calendário de expediente |

**Regra:** os campos exigidos das seções 5 a 12 do documento base devem ser **carregados como registros em `AD_CTBCAMPO`**, não codificados no prompt. O agente consulta a taxonomia em tempo de execução.

---

## 6. TAXONOMIA: CATEGORIAS, SUBCATEGORIAS E CAMPOS

### 6.1 Categoria 1 — IMPOSTOS E FISCAL
*"Parametrizações, ISS, pendências e dúvidas fiscais."*

| Subcategoria | Campos coletados |
|---|---|
| Parametrização de impostos | Empresa*, Imposto*, Operação*, Tipo de documento/NF, Número da NF ou NU, Parceiro, Situação atual*, Resultado esperado*, Motivo da solicitação* |
| Cadastro/alteração de ISS para outros municípios | Empresa*, Município*, UF*, Serviço*, Código do serviço, Alíquota, Existe retenção (S/N), Exemplo de NF |
| Consulta de pendências fiscais | Empresa*, Período*, Tipo de pendência, Obrigação/tributo, Descrição da situação* |
| Dúvidas fiscais | Empresa*, Descrição da dúvida*, Documento relacionado |
| Outros assuntos fiscais | Empresa*, Descrição livre* |

### 6.2 Categoria 2 — CADASTROS E PARAMETRIZAÇÕES
*"TOP, Natureza, Conta Contábil, Centro de Custo e acessos."*

| Subcategoria | Campos coletados |
|---|---|
| Configuração de TOP | Empresa*, Código da TOP, Descrição*, Operação*, Problema atual*, Comportamento esperado*, Documento de exemplo |
| Cadastro/alteração de Natureza | Empresa*, Tipo de cadastro*, Descrição*, Finalidade*, Natureza desejada, Exemplo da operação |
| Cadastro/alteração de Conta Contábil | Empresa*, Tipo de cadastro*, Descrição*, Finalidade*, Conta desejada, Exemplo da operação |
| Inclusão/alteração de Centro de Custo | Empresa*, Centro de Custo*, Inclusão ou alteração*, Descrição*, Finalidade*, Responsável* |
| Liberação de acesso para usuários | Usuário*, Empresa*, Sistema/módulo*, Acesso solicitado*, Motivo* |
| Outros cadastros/parametrizações | Empresa*, Descrição livre* |

### 6.3 Categoria 3 — PEDIDOS
*"Liberação, alteração ou exclusão de pedidos."*

Campos comuns a toda solicitação de pedido: **Empresa\*, Número do pedido\*, Parceiro\*, Motivo da solicitação\***.

| Subcategoria | Campos adicionais |
|---|---|
| Liberar pedido | — |
| Alterar pedido | Campo a ser alterado*, Informação atual*, Nova informação*, Motivo da alteração* |
| Excluir pedido | Motivo (**obrigatório**)*; verificar documentos/operações vinculadas quando a informação estiver disponível |
| Outra solicitação | Descrição livre* |

**Regra crítica (item 7 do documento base):** o agente **nunca** informa que um pedido foi liberado, alterado ou excluído sem confirmação efetiva retornada pela ferramenta/sistema. Sem permissão → registra e encaminha.

### 6.4 Categoria 4 — CONTABILIDADE
*"Demonstrações, fechamento, período contábil e dúvidas contábeis."*

| Subcategoria | Campos coletados |
|---|---|
| Solicitação de demonstrações contábeis | Empresa*, Período*, Demonstração desejada* (Balanço Patrimonial, DRE, Balancete, DFC, Razão, Outros), Finalidade* |
| Dúvidas sobre fechamento | Empresa*, Período*, Processo/conta envolvida*, Descrição da dúvida*, Valor envolvido, Documento/lançamento relacionado |
| Liberação de período contábil | Empresa*, Período*, Motivo da liberação*, Tipo de lançamento a ser realizado*, Usuário responsável pelo lançamento*, Justificativa* (**obrigatória**) |
| Dúvidas/orientações contábeis | Empresa*, Descrição da dúvida* |
| Classificação contábil | Empresa*, Operação/documento*, Dúvida de classificação* |
| Lançamentos contábeis | Empresa*, Período*, Descrição do lançamento*, Valor, Documento de origem |
| Provisões | Empresa*, Período*, Tipo de provisão*, Valor, Critério/base* |
| Rateios | Empresa*, Período*, Regra de rateio*, Centros envolvidos*, Finalidade* |
| Outras questões contábeis | Empresa*, Descrição livre* |

Em "Dúvidas sobre fechamento", o agente **deve tentar orientar** quando houver informação suficiente na base de conhecimento; se não concluir, encaminha à equipe contábil.
Em "Liberação de período contábil", o agente **não libera** sem autorização e ferramenta específica — sempre registra com `EXIGEAPROVACAO = S`.

### 6.5 Categoria 5 — RELATÓRIOS GERENCIAIS
*"Relatórios, consultas e informações gerenciais."*

Subcategorias: DRE Gerencial; Relatório de faturamento; Relatório de despesas; Relatório por Centro de Resultado; Relatório por empresa; Indicadores; Informações para Diretoria; Outros relatórios.

Campos: Empresa(s)*, Período*, Tipo de relatório*, Informações/dimensões necessárias*, Centro de Resultado (quando aplicável), Projeto (quando aplicável), Natureza (quando aplicável), Finalidade*.

Quando possível, o agente utiliza dados já existentes no Sankhya para atender diretamente à solicitação (consulta somente-leitura), antes de abrir chamado.

### 6.6 Categoria 6 — ESTOQUE E IMOBILIZADO
*"Estoque, custos, produtos, patrimônio e demais solicitações."*

> **Observação:** o documento base lista este card (item 2, card 6) mas não traz a seção detalhada correspondente (as seções 10 e 11 estão ausentes). A estrutura abaixo é **proposta** e requer validação da área contábil.

| Subcategoria (proposta) | Campos propostos |
|---|---|
| Divergência de estoque | Empresa*, Produto*, Local/depósito, Período*, Quantidade sistema*, Quantidade física*, Descrição* |
| Custo de produto | Empresa*, Produto*, Período*, Custo apresentado*, Custo esperado, Descrição* |
| Cadastro/alteração de produto (visão contábil) | Empresa*, Produto*, Alteração desejada*, Motivo* |
| Movimentação de imobilizado | Empresa*, Bem/patrimônio*, Tipo de movimentação* (aquisição, baixa, transferência, reavaliação), Data*, Valor, Motivo* |
| Depreciação | Empresa*, Bem/patrimônio, Período*, Dúvida/ocorrência* |
| Inventário | Empresa*, Período*, Local, Descrição* |
| Outras solicitações de estoque/imobilizado | Empresa*, Descrição livre* |

### 6.7 Categoria 7 — CONCILIAÇÕES E DIVERGÊNCIAS
*"Diferenças, saldos e conciliações contábeis."*

> Igualmente **proposta** — validar com a área.

| Subcategoria (proposta) | Campos propostos |
|---|---|
| Conciliação bancária | Empresa*, Conta bancária*, Período*, Valor divergente*, Descrição* |
| Conciliação de contas a pagar/receber | Empresa*, Parceiro, Período*, Título/documento, Valor divergente*, Descrição* |
| Divergência de saldo contábil | Empresa*, Conta contábil*, Período*, Saldo apresentado*, Saldo esperado*, Descrição* |
| Divergência fiscal x contábil | Empresa*, Período*, Tributo/obrigação*, Valores comparados*, Descrição* |
| Outras conciliações | Empresa*, Descrição livre* |

Em todas as subcategorias desta categoria, o agente deve **incentivar ativamente o anexo** da evidência da divergência (relatório, extrato, print).

### 6.8 Categoria 8 — OUTROS
*"Demais dúvidas e solicitações."*

Campo livre. O agente deve: (1) interpretar a solicitação; (2) tentar enquadrar em uma das 7 categorias existentes e propor ao usuário; (3) se não for possível, classificar como `OUTROS`; (4) fazer as perguntas necessárias; (5) registrar o chamado.

---

## 7. MÁQUINA DE ESTADOS

### 7.1 Status

| Status | Significado |
|---|---|
| `NOVO` | Registrado, ainda sem responsável assumido |
| `EM ANÁLISE` | Responsável assumiu e está avaliando |
| `AGUARDANDO INFORMAÇÃO` | Pendente de retorno do solicitante (relógio de SLA pausado) |
| `EM ATENDIMENTO` | Execução em andamento |
| `AGUARDANDO APROVAÇÃO` | Pendente de autorização de aprovador |
| `CONCLUÍDO` | Encerrado com solução registrada |
| `CANCELADO` | Encerrado sem execução |

### 7.2 Transições permitidas

```
NOVO ──────────────► EM ANÁLISE, CANCELADO
EM ANÁLISE ────────► EM ATENDIMENTO, AGUARDANDO INFORMAÇÃO,
                     AGUARDANDO APROVAÇÃO, CONCLUÍDO, CANCELADO
AGUARDANDO INFO ───► EM ANÁLISE, EM ATENDIMENTO, CANCELADO
EM ATENDIMENTO ────► AGUARDANDO INFORMAÇÃO, AGUARDANDO APROVAÇÃO,
                     CONCLUÍDO, CANCELADO
AGUARDANDO APROV. ─► EM ATENDIMENTO, CONCLUÍDO, CANCELADO
CONCLUÍDO ─────────► (reabertura → EM ATENDIMENTO, até 5 dias úteis)
CANCELADO ─────────► (terminal)
```

### 7.3 Regras
- `CONCLUÍDO` exige `SOLUCAO` preenchida.
- `CANCELADO` exige `CANCMOTIVO` preenchida.
- `AGUARDANDO INFORMAÇÃO` **pausa** a contagem de SLA; ao retornar, retoma.
- Toda transição grava registro em `AD_CTBHIST` com valor anterior e novo.
- Subcategorias com `EXIGEAPROVACAO = S` (ex.: liberação de período contábil, exclusão de pedido) **não podem** ir direto para `CONCLUÍDO` sem passar por `AGUARDANDO APROVAÇÃO`.
- Solicitante pode cancelar somente enquanto `NOVO`.

---

## 8. PRIORIZAÇÃO AUTOMÁTICA E SLA

### 8.1 Matriz de prioridade

| Prioridade | Critérios de enquadramento |
|---|---|
| **URGENTE** | Impacto em fechamento; obrigação fiscal com prazo; pagamento; faturamento; informação para Diretoria; bloqueio operacional crítico |
| **ALTA** | Impacto operacional relevante (processo parado para um setor, mas com contorno) |
| **NORMAL** | Solicitação operacional comum |
| **BAIXA** | Dúvidas, consultas ou melhorias sem impacto imediato |

### 8.2 Mecânica
- O agente **sugere** a prioridade (`PRIORIDADESUG`) com base nos critérios acima e no conteúdo da solicitação.
- A prioridade final (`PRIORIDADE`) pode ser ajustada conforme regras internas — por regra padrão da subcategoria (`AD_CTBSUB.PRIORIDADEPADRAO`) ou manualmente por Atendente/Gestor.
- Divergências entre `PRIORIDADESUG` e `PRIORIDADE` alimentam indicador de acurácia da triagem.

### 8.3 SLA sugerido (parametrizável em `AD_CTBSLA`, em horas úteis)

| Prioridade | 1ª resposta | Solução |
|---|---|---|
| URGENTE | 1h | 4h |
| ALTA | 4h | 16h (2 dias) |
| NORMAL | 8h (1 dia) | 40h (5 dias) |
| BAIXA | 16h (2 dias) | 80h (10 dias) |

Calendário de expediente configurável (padrão: seg–sex, 08:00–18:00, exceto feriados). `DTPREVISTA` calculada na abertura e recalculada em mudança de prioridade.

---

## 9. ROTEAMENTO E DISTRIBUIÇÃO

1. Ao registrar, o serviço consulta `AD_CTBROT` na ordem de especificidade: **subcategoria + empresa** → **subcategoria** → **categoria + empresa** → **categoria** → **fila padrão**.
2. Resultado define `CODFILA` e, quando houver responsável fixo, `CODUSURESP`.
3. Sem responsável definido, o chamado permanece `NOVO` na fila até ser assumido.
4. Reatribuição manual sempre permitida a Gestor, com registro em histórico.
5. Chamados `URGENTE` disparam notificação imediata à fila (seção 16).

---

## 10. FERRAMENTAS (TOOLS) DO AGENTE

> Todas as tools de consulta são **somente-leitura** e recebem parâmetros tipados. Nenhuma tool aceita SQL livre. Toda tool valida se o usuário do contexto tem permissão sobre a empresa/dado solicitado.

### 10.1 `listar_taxonomia`
Retorna categorias, subcategorias ativas e os campos exigidos de cada uma.
- **Entrada:** `codCategoria` (opcional), `codSubcategoria` (opcional)
- **Saída:** lista de `{codigo, nome, descricao, icone, subcategorias:[{codigo, nome, campos:[{codigo, rotulo, tipo, obrigatorio, dominio, ajuda}]}]}`
- **Uso:** consultada antes de iniciar a coleta, para saber exatamente o que perguntar.

### 10.2 `criar_chamado`
- **Entrada:**
```json
{
  "codCategoria": 3,
  "codSubcategoria": 302,
  "codEmpresa": 1,
  "assunto": "Alteração de condição de pagamento do pedido 156946",
  "descricao": "texto consolidado",
  "prioridadeSugerida": "NORMAL",
  "origem": "C",
  "detalhes": [
    {"campo":"NUMPEDIDO","rotulo":"Número do pedido","valor":"156946","tipo":"N"},
    {"campo":"CAMPOALTER","rotulo":"Campo a ser alterado","valor":"Condição de pagamento","tipo":"T"},
    {"campo":"VALORATUAL","rotulo":"Informação atual","valor":"30 dias","tipo":"T"},
    {"campo":"VALORNOVO","rotulo":"Nova informação","valor":"45 dias","tipo":"T"},
    {"campo":"MOTIVO","rotulo":"Motivo","valor":"Ajuste comercial","tipo":"T"}
  ],
  "anexos": ["chave-arquivo-1"]
}
```
- **Saída:** `{numChamado:"CTB-000125", nuChamado:125, status:"NOVO", prioridade:"NORMAL", dtAbertura, dtPrevista, responsavel, fila}`
- **Erros:** `CAMPO_OBRIGATORIO_AUSENTE` (lista os campos faltantes), `EMPRESA_SEM_PERMISSAO`, `SUBCATEGORIA_INATIVA`.
- **Regra:** só pode ser chamada **após confirmação explícita do usuário** (seção 12.4).

### 10.3 `listar_chamados`
- **Entrada:** `status` (opcional), `codCategoria` (opcional), `dataInicial`/`dataFinal` (opcional), `limite` (padrão 20)
- **Saída:** número, data de abertura, categoria, assunto, status, responsável, última atualização.
- **Regra de segurança:** filtra automaticamente pelos chamados do usuário do contexto, salvo perfil Gestor/Admin.

### 10.4 `detalhar_chamado`
- **Entrada:** `numChamado`
- **Saída:** cabeçalho completo + detalhes + histórico visível + lista de anexos.
- Nega acesso a chamado de terceiro para perfil Solicitante.

### 10.5 `adicionar_interacao`
- **Entrada:** `numChamado`, `tipo` (`COMENTARIO` | `RESPINFO`), `texto`, `anexos` (opcional)
- **Saída:** confirmação + novo status, quando a interação disparar transição (ex.: `RESPINFO` move de `AGUARDANDO INFORMAÇÃO` para `EM ANÁLISE`).

### 10.6 `cancelar_chamado`
- **Entrada:** `numChamado`, `motivo` (obrigatório)
- Permitido ao solicitante somente em `NOVO`.

### 10.7 `anexar_arquivo`
- **Entrada:** `numChamado` (ou identificador de rascunho), `nomeArquivo`, `conteudo`/`chaveUpload`
- **Saída:** confirmação e sequência do anexo.
- Validação de extensão e tamanho (seção 14).

### 10.8 Consultas de contexto Sankhya (somente leitura)

| Tool | Entrada | Saída |
|---|---|---|
| `consultar_empresas_usuario` | — | empresas às quais o usuário tem acesso |
| `consultar_pedido` | `codEmpresa`, `numPedido` | número, parceiro, data, valor, situação/status de liberação, TOP, existência de documentos vinculados |
| `consultar_nota` | `codEmpresa`, `numNota` ou `nuNota` | dados fiscais básicos, parceiro, TOP, situação |
| `consultar_parceiro` | `nome` ou `codParc` ou `CNPJ/CPF` | código, razão social, CNPJ/CPF, cidade/UF |
| `consultar_top` | `codTop` ou `descricao` | código, descrição, características principais |
| `consultar_periodo_contabil` | `codEmpresa`, `periodo` | situação do período (aberto/fechado), data de fechamento |
| `consultar_produto` | `codProd` ou `descricao` | código, descrição, unidade, grupo |
| `consultar_conta_contabil` | `codigo` ou `descricao` | conta, descrição, situação |
| `consultar_centro_resultado` | `codigo` ou `descricao` | centro, descrição, situação |

**Uso obrigatório:** ao capturar um número de pedido/NF, o agente **valida via tool** antes de confirmar o resumo. Se a consulta não retornar o registro, informa ao usuário e pede conferência — nunca assume existência.

---

## 11. INSTRUÇÕES DO AGENTE (system prompt operacional)

> Texto para configuração do agente no Mitra. Ajustar apenas o necessário ao formato da plataforma.

```
IDENTIDADE
Você é a "Central de Atendimento Contábil", assistente de atendimento interno da
área Contábil/Fiscal dentro do Sankhya. Seu papel é acolher a solicitação de
qualquer colaborador, entender o que ele precisa, coletar apenas as informações
necessárias, confirmar e registrar o chamado.

PÚBLICO
Colaboradores de qualquer área, sem conhecimento contábil ou técnico. Nunca
pressuponha domínio de termos contábeis; explique quando precisar usá-los.

LINGUAGEM
Português do Brasil. Tom profissional, cordial, objetivo e simples. Evite
respostas longas e jargão técnico desnecessário. Faça perguntas de forma
organizada, preferencialmente uma ou poucas por vez, com opções quando existirem.

FLUXO PADRÃO
1. O usuário chega por um card de categoria ou por texto livre.
2. Identifique a subcategoria — pergunte "O que você precisa fazer?" com as
   opções da categoria escolhida, ou deduza do texto livre e confirme.
3. Consulte a taxonomia (listar_taxonomia) para saber quais campos coletar.
4. Pergunte SOMENTE o que ainda falta. Nunca peça algo que o usuário já informou
   e nunca peça algo que você consiga obter por consulta ao sistema.
5. Valide identificadores (pedido, nota, período, parceiro) usando as tools de
   consulta antes de confirmar.
6. Sugira prioridade conforme a matriz.
7. Apresente um RESUMO estruturado e peça confirmação, oferecendo as opções
   "Confirmar" e "Alterar informações".
8. Só após a confirmação explícita, chame criar_chamado.
9. Informe número, categoria, assunto, status, data de abertura e prazo estimado.

IDENTIFICAÇÃO AUTOMÁTICA
Se o usuário escrever livremente (ex.: "Preciso liberar o pedido 156946"),
extraia categoria, subcategoria e entidades (número de pedido, NF, empresa,
período, parceiro, município, conta) e pergunte apenas o que faltar.

REGRAS INEGOCIÁVEIS
- Nunca invente informações, dados do Sankhya, números de documento, saldos,
  parametrizações ou resultados de consulta.
- Nunca afirme que executou uma ação sem confirmação efetiva retornada pela
  ferramenta. Isso inclui liberar, alterar ou excluir pedido, e liberar período
  contábil.
- Nunca exclua ou altere dados, parametrizações, contas contábeis ou naturezas
  sem autorização e ferramenta específica.
- Se não tiver permissão ou ferramenta para executar, responda exatamente com o
  sentido de: "Não tenho permissão para realizar essa alteração diretamente. Vou
  registrar sua solicitação com todas as informações necessárias para que a
  equipe responsável possa avaliar." — e prossiga com o registro.
- Se uma consulta falhar ou não retornar dados, diga isso claramente; não
  preencha lacunas por suposição.
- Não exponha dados de chamados de outros usuários.

ANEXOS
Incentive anexos sempre que ajudarem a análise: print do erro, NF, pedido,
relatório, documento contábil, evidência da divergência. Em conciliações e
divergências, peça a evidência ativamente.

OBRIGATORIEDADES ESPECÍFICAS
- Exclusão de pedido: motivo é obrigatório; verifique documentos ou operações
  vinculadas quando essa informação estiver disponível.
- Liberação de período contábil: justificativa é obrigatória, além de empresa,
  período, motivo, tipo de lançamento e usuário responsável. Registrar sempre
  para aprovação; nunca liberar.

DÚVIDAS
Em dúvidas de fechamento, classificação ou orientação contábil, tente orientar
quando houver informação suficiente na base de conhecimento. Se não conseguir
concluir com segurança, registre e encaminhe à equipe contábil.

RELATÓRIOS
Em Relatórios Gerenciais, quando os dados solicitados estiverem disponíveis por
consulta, apresente-os ao usuário antes de abrir chamado, e pergunte se ainda
deseja registrar a solicitação.

ENCERRAMENTO
Após registrar, ofereça: consultar o histórico de chamados ou abrir outra
solicitação.
```

---

## 12. FLUXOS CONVERSACIONAIS

### 12.1 Entrada por card

1. Usuário clica em **PEDIDOS**.
2. Agente: *"O que você precisa fazer?"* → opções: Liberar pedido / Alterar pedido / Excluir pedido / Outra solicitação relacionada a pedidos.
3. Usuário: **Alterar pedido**.
4. Agente coleta em sequência (pulando o já sabido): Empresa → Número do pedido → (valida via `consultar_pedido`; se retornar parceiro, **não pergunta o parceiro**) → Campo a ser alterado → Informação atual (pré-preenche com o valor retornado, pedindo confirmação) → Nova informação → Motivo.
5. Resumo → confirmação → `criar_chamado` → número do chamado.

### 12.2 Entrada por texto livre (identificação automática)

Usuário: *"Preciso liberar o pedido 156946."*

Agente identifica:
- Categoria: **Pedidos**
- Subcategoria: **Liberação de pedido**
- Número do pedido: **156946**

Chama `consultar_pedido`. Se localizado, obtém empresa e parceiro e pergunta apenas o **motivo da solicitação**. Se não localizado, informa e pede conferência do número.

### 12.3 Enquadramento a partir de "Outros"

Usuário descreve livremente. Agente interpreta, propõe enquadramento (*"Isso parece uma solicitação de Conciliações e Divergências. Posso seguir por aí?"*), coleta os campos daquela subcategoria. Não sendo possível enquadrar, classifica como `OUTROS` e coleta descrição + empresa.

### 12.4 Confirmação antes do registro (obrigatória)

```
Confira sua solicitação:

Categoria: Pedidos
Tipo: Alteração de pedido
Empresa: Empresa X
Pedido: 156946
Alteração: Condição de pagamento
De: 30 dias
Para: 45 dias
Motivo: Ajuste comercial

Está correto?
[Confirmar]  [Alterar informações]
```

- "Alterar informações" → agente pergunta qual item corrigir e reapresenta o resumo.
- `criar_chamado` **só** após "Confirmar".

### 12.5 Retorno do registro

```
Chamado CTB-000125 aberto com sucesso.

Número: CTB-000125
Categoria: Pedidos — Alteração de pedido
Assunto: Alteração de condição de pagamento do pedido 156946
Status: NOVO
Abertura: 18/08/2026 14:32
Prazo estimado: 25/08/2026 14:32
```

### 12.6 Fluxo sem permissão de execução

Quando o usuário pede execução direta e o agente não possui ferramenta/permissão:
> "Não tenho permissão para realizar essa alteração diretamente. Vou registrar sua solicitação com todas as informações necessárias para que a equipe responsável possa avaliar."

Segue imediatamente para a coleta e o registro — sem interromper o atendimento.

### 12.7 Consulta ao histórico

Usuário pede "meus chamados" ou clica em **Histórico de chamados** → `listar_chamados` → grid; ao selecionar um → `detalhar_chamado` → cabeçalho, dados coletados, histórico e anexos; ações disponíveis: comentar, anexar, responder solicitação de informação, cancelar (se `NOVO`).

---

## 13. INTERFACES (TELAS) E COMPONENTES

### 13.1 Tela inicial

- **Título:** "Como podemos ajudar?"
- **Subtítulo:** "Selecione o assunto da sua solicitação:"
- **Grid de 8 cards** (sugestão: 4 colunas em desktop, 2 em tablet, 1 em mobile), cada um com ícone, nome e descrição:

| # | Card | Descrição | Ícone sugerido |
|---|---|---|---|
| 1 | IMPOSTOS E FISCAL | Parametrizações, ISS, pendências e dúvidas fiscais. | documento com percentual |
| 2 | CADASTROS E PARAMETRIZAÇÕES | TOP, Natureza, Conta Contábil, Centro de Custo e acessos. | engrenagem/ajustes |
| 3 | PEDIDOS | Liberação, alteração ou exclusão de pedidos. | carrinho / lista de pedido |
| 4 | CONTABILIDADE | Demonstrações, fechamento, período contábil e dúvidas contábeis. | livro-razão / balança |
| 5 | RELATÓRIOS GERENCIAIS | Relatórios, consultas e informações gerenciais. | gráfico de barras |
| 6 | ESTOQUE E IMOBILIZADO | Estoque, custos, produtos, patrimônio, e demais solicitações. | caixa / prédio |
| 7 | CONCILIAÇÕES E DIVERGÊNCIAS | Diferenças, saldos e conciliações contábeis. | duas setas / comparação |
| 8 | OUTROS | Demais dúvidas e solicitações. | balão de conversa |

- Todo card é clicável (área inteira, não apenas o texto).
- Campo de texto livre abaixo dos cards: *"Ou descreva o que você precisa..."* — aciona o fluxo de identificação automática.
- Visual limpo, corporativo e moderno, aderente ao padrão visual do Sankhya (tipografia, cores e espaçamento do design system vigente).
- Estados: hover, foco por teclado, ativo. Navegação por `Tab` e acionamento por `Enter`/`Espaço`.

### 13.2 Barra superior

- Item **"Histórico de chamados"** sempre visível.
- Identificação do usuário e empresa de contexto.

### 13.3 Tela de Histórico

**Grid (colunas):** Número · Data de abertura · Categoria · Assunto · Status (com badge colorido) · Responsável · Última atualização.
**Filtros:** status, categoria, período, texto.
**Ordenação padrão:** última atualização, decrescente.
**Ação:** clique na linha abre o detalhe.

### 13.4 Tela de Detalhe do Chamado

- Bloco de cabeçalho: número, status, prioridade, categoria/subcategoria, empresa, solicitante, responsável, abertura, prazo, conclusão.
- Bloco "Informações da solicitação": lista rótulo/valor a partir de `AD_CTBDET`.
- **Linha do tempo** do histórico (apenas eventos com `VISIVELSOLIC = S` para o solicitante).
- Lista de anexos com download.
- Caixa de comentário + botão de anexar.
- Botão "Cancelar chamado" (condicionado ao status).

### 13.5 Painel de conversa

- Área de mensagens, opções em botões quando houver lista fechada, indicador de digitação, botão de anexo, botão "Recomeçar atendimento".
- Bloco de resumo renderizado como card com os botões **Confirmar** e **Alterar informações**.

### 13.6 Badges de status (sugestão de cor)

`NOVO` azul · `EM ANÁLISE` roxo · `AGUARDANDO INFORMAÇÃO` âmbar · `EM ATENDIMENTO` ciano · `AGUARDANDO APROVAÇÃO` laranja · `CONCLUÍDO` verde · `CANCELADO` cinza.
Cor nunca é o único indicador — sempre acompanhada do rótulo textual.

---

## 14. ANEXOS

- Formatos aceitos: `pdf, png, jpg, jpeg, xlsx, xls, csv, docx, txt, xml, zip`.
- Tamanho máximo por arquivo: **10 MB**; máximo de **10 arquivos** por chamado.
- Anexos podem ser enviados durante a conversa (antes do registro, vinculados ao rascunho) ou depois, pela tela de detalhe.
- O agente sugere anexo proativamente nos casos: erro de sistema (print), divergência (evidência/relatório), solicitação sobre NF ou pedido (documento), dúvida sobre lançamento (comprovante).
- Armazenamento no repositório de arquivos do Sankhya; a tabela guarda apenas a chave.

---

## 15. REGRAS DE SEGURANÇA E GUARDRAILS

### 15.1 Proibições absolutas do agente

O agente **nunca** deve:
1. Inventar informações.
2. Inventar dados do Sankhya.
3. Informar que executou uma ação sem confirmação do sistema.
4. Excluir dados sem autorização.
5. Alterar parametrizações sem autorização.
6. Liberar período contábil sem autorização.
7. Liberar pedido sem autorização.
8. Alterar pedido sem autorização.
9. Criar contas contábeis sem autorização.
10. Criar naturezas sem autorização.

### 15.2 Controles técnicos que sustentam as proibições

> As regras acima **não podem depender apenas do prompt**. Cada uma tem contraparte técnica:

| Regra | Controle técnico |
|---|---|
| Não executar ação transacional | Nenhuma tool de escrita em entidade transacional está registrada no agente nesta fase |
| Não afirmar execução sem confirmação | A mensagem de sucesso é montada **a partir do retorno da tool**; sem retorno, não há mensagem de sucesso |
| Não expor dado de terceiro | Filtro por usuário aplicado dentro da tool/serviço, não no prompt |
| Não consultar empresa sem acesso | Toda tool valida `codEmpresa` contra as empresas do usuário |
| Não gravar chamado inválido | Serviço valida campos obrigatórios da subcategoria e rejeita o payload |
| Auditoria | Toda chamada de tool com escrita gera registro em `AD_CTBHIST` com autor, data e payload resumido |

### 15.3 LGPD e dados sensíveis
- Não solicitar CPF, dados bancários pessoais ou senhas em texto livre.
- Anexos ficam sujeitos às mesmas regras de acesso do chamado.
- Log de acesso a chamados mantido por, no mínimo, 12 meses.

---

## 16. NOTIFICAÇÕES E INTEGRAÇÕES

| Evento | Destinatário | Canal |
|---|---|---|
| Chamado aberto | Fila/responsável | Notificação Sankhya + e-mail |
| Chamado aberto com prioridade URGENTE | Fila + gestor | Notificação imediata + e-mail |
| Status alterado | Solicitante | Notificação Sankhya |
| `AGUARDANDO INFORMAÇÃO` | Solicitante | Notificação + e-mail |
| Resposta do solicitante | Responsável | Notificação |
| `AGUARDANDO APROVAÇÃO` | Aprovador | Notificação + e-mail |
| SLA a vencer (80% do prazo) | Responsável | Notificação |
| SLA vencido | Responsável + gestor | Notificação + e-mail |
| Chamado concluído | Solicitante | Notificação + e-mail com a solução |

Rotina agendada (a cada 30 min) avalia SLA a vencer/vencido.

---

## 17. ANALYTICS E INDICADORES

Painel analítico com:

1. Quantidade de chamados (total e no período)
2. Chamados por categoria
3. Chamados por empresa
4. Chamados por usuário solicitante
5. Chamados por responsável
6. Chamados por prioridade
7. Tempo médio de atendimento
8. Chamados atrasados (SLA vencido)
9. Chamados concluídos
10. Principais dúvidas (agrupamento por subcategoria e por termos recorrentes)
11. Principais causas de divergências
12. Volume de chamados por mês (série temporal)

**Indicadores complementares sugeridos:**
13. Taxa de resolução na primeira interação
14. Tempo médio de primeira resposta
15. Aderência ao SLA (%)
16. Acurácia da classificação automática (`PRIORIDADESUG` vs `PRIORIDADE`; categoria sugerida vs categoria final após reclassificação)
17. Taxa de abandono da conversa antes da confirmação
18. Chamados reabertos

Filtros globais do painel: período, empresa, categoria, prioridade, responsável, status.

---

## 18. CRITÉRIOS DE ACEITE

| # | Critério | Como validar |
|---|---|---|
| CA-01 | Tela inicial exibe título, subtítulo e os 8 cards com nome, descrição e ícone corretos | Inspeção visual + comparação com seção 13.1 |
| CA-02 | Todo card é clicável e inicia o atendimento da categoria correspondente | Clicar nos 8 cards |
| CA-03 | Ao selecionar categoria, o agente pergunta a necessidade específica e **não** apresenta formulário completo | Teste em todas as 8 categorias |
| CA-04 | O agente coleta apenas os campos da subcategoria e nunca repete pergunta já respondida | Roteiro de conversa com informações antecipadas |
| CA-05 | Texto livre "Preciso liberar o pedido 156946" resulta em categoria Pedidos, subcategoria Liberação, pedido 156946 preenchido, faltando apenas o motivo | Teste direto |
| CA-06 | Número de pedido/NF informado é validado por consulta antes da confirmação | Testar com número existente e inexistente |
| CA-07 | Resumo é apresentado antes do registro, com "Confirmar" e "Alterar informações" | Teste em 3 categorias |
| CA-08 | Nenhum chamado é criado sem confirmação explícita | Abandonar o fluxo antes de confirmar e verificar a base |
| CA-09 | "Alterar informações" permite corrigir e reapresenta o resumo | Teste |
| CA-10 | Registro gera `CTB-######` único e sequencial, exibido com número, categoria, assunto, status, data e prazo | 20 aberturas simultâneas sem colisão |
| CA-11 | Exclusão de pedido sem motivo é bloqueada | Tentar concluir sem motivo |
| CA-12 | Liberação de período contábil sem justificativa é bloqueada e nunca é executada pelo agente | Teste |
| CA-13 | Solicitação de execução sem permissão retorna a mensagem padrão e registra o chamado | Teste |
| CA-14 | O agente nunca afirma execução sem retorno de ferramenta | Teste com tool simulando falha |
| CA-15 | Histórico lista os campos previstos e permite abrir o detalhe | Teste |
| CA-16 | Solicitante não acessa chamado de outro usuário nem por número direto | Teste com número de terceiro |
| CA-17 | Todos os 7 status e as transições da seção 7.2 funcionam; transições inválidas são rejeitadas | Matriz de transições |
| CA-18 | Prioridade é sugerida conforme a matriz e pode ser ajustada | 8 casos-teste (2 por nível) |
| CA-19 | SLA é calculado em horas úteis e pausado em `AGUARDANDO INFORMAÇÃO` | Teste com virada de expediente |
| CA-20 | Anexos até 10 MB são aceitos, vinculados e recuperáveis | Teste com formatos permitidos e bloqueados |
| CA-21 | Histórico do chamado registra todos os eventos da seção 5.3 | Ciclo completo de um chamado |
| CA-22 | Painel apresenta os 12 indicadores obrigatórios com filtros funcionais | Conferência contra consulta SQL |
| CA-23 | Respostas em pt-BR, tom profissional/cordial/objetivo, sem excesso técnico e sem prolixidade | Revisão de 20 diálogos pela área contábil |
| CA-24 | Categoria "Outros" interpreta, tenta enquadrar e registra corretamente | 5 solicitações ambíguas |

---

## 19. PLANO DE ENTREGA EM FASES

### Fase 1 — Fundação (estrutura e registro)
- Modelo de dados `AD_CTB*` e carga da taxonomia (categorias, subcategorias, campos).
- Serviço de criação de chamado com validação, numeração, prioridade e SLA.
- Tools: `listar_taxonomia`, `criar_chamado`, `listar_chamados`, `detalhar_chamado`.
- Agente configurado com o prompt da seção 11, cobrindo as categorias 1 a 5 e 8.
- Tela inicial com os 8 cards e painel de conversa.
**Entregável:** abertura e consulta de chamados ponta a ponta.

### Fase 2 — Contexto Sankhya e histórico completo
- Tools de consulta somente-leitura (pedido, NF, parceiro, TOP, período, produto, conta, centro).
- Identificação automática a partir de texto livre.
- Tela de histórico e detalhe; interações e anexos.
- Categorias 6 e 7 (após validação da área — ver seção 20).
**Entregável:** agente que pré-valida dados e não repete perguntas.

### Fase 3 — Operação da equipe contábil
- Fila de atendimento, roteamento, atribuição, máquina de estados completa, aprovação.
- Notificações e monitoramento de SLA.
- Painel analítico com os indicadores da seção 17.
**Entregável:** ciclo de atendimento gerenciado.

### Fase 4 — Automação assistida (opcional, sujeita a autorização formal)
- Habilitação controlada de ações executáveis (ex.: liberação de pedido) mediante:
  perfil autorizado + confirmação dupla + registro de auditoria + retorno efetivo do sistema antes de qualquer mensagem de sucesso.
- Sugestão automática de solução com base no histórico de chamados semelhantes.
**Entregável:** redução de trabalho manual em casos de baixo risco.

---

## 20. RISCOS, PREMISSAS E PENDÊNCIAS DE DECISÃO

### 20.1 Lacunas identificadas no documento base

| # | Lacuna | Encaminhamento |
|---|---|---|
| L-01 | **Seções 10 e 11 ausentes.** Os cards 6 (Estoque e Imobilizado) e 7 (Conciliações e Divergências) aparecem na tela inicial, mas o documento não detalha suas subcategorias e campos. | Estrutura **proposta** nas seções 6.6 e 6.7 deste escopo — **requer validação da área contábil antes da Fase 2**. |
| L-02 | Não há definição de quem atende cada categoria. | Preencher `AD_CTBROT` com a área; sem isso, tudo cai em fila única. |
| L-03 | SLA não definido no documento base. | Valores da seção 8.3 são **sugestão**; validar com a coordenação contábil. |
| L-04 | Não há definição sobre pesquisa de satisfação no encerramento. | Decidir se entra na Fase 3. |
| L-05 | Não há política de reabertura definida. | Proposta: 5 dias úteis após conclusão. Validar. |
| L-06 | Comportamento fora do horário de expediente não especificado. | Proposta: registrar normalmente; SLA conta a partir do próximo horário útil. |

### 20.2 Premissas
- O usuário já está autenticado no Sankhya; o agente herda a identidade e as permissões de empresa do contexto.
- Existe repositório de arquivos disponível para anexos.
- A plataforma Mitra suporta: tools parametrizadas, renderização de opções em botões e upload de arquivo na conversa. *(Confirmar o suporte a botões; caso não haja, as opções são apresentadas como lista numerada e o usuário responde pelo número.)*

### 20.3 Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Agente afirmar execução não realizada | Alto — perda de confiança | Não registrar tools de escrita transacional na Fase 1; mensagem de sucesso derivada exclusivamente do retorno da tool |
| Classificação incorreta de categoria | Médio | Confirmação do enquadramento com o usuário + reclassificação pelo atendente + indicador de acurácia |
| Excesso de perguntas afastando o usuário | Alto — abandono | Campos obrigatórios enxutos; uso de consulta para preencher o que já existe; indicador de abandono monitorado |
| Vazamento de chamado entre usuários | Alto | Filtro de visibilidade na camada de serviço, com teste de aceite dedicado (CA-16) |
| Volume alto em fila única | Médio | Roteamento por categoria desde a Fase 3 |
| Taxonomia engessada no prompt | Médio | Taxonomia em tabela, lida por tool em tempo de execução |

---

## ANEXO A — RESUMO EXECUTIVO PARA CONFIGURAÇÃO NO MITRA

**Nome do agente:** Central de Atendimento Contábil
**Objetivo:** central de atendimento interna da área Contábil — o usuário escolhe uma categoria (ou descreve livremente), o agente entende, pergunta só o necessário, confirma e registra o chamado.
**Instruções:** seção 11 deste documento.
**Tools:** `listar_taxonomia`, `criar_chamado`, `listar_chamados`, `detalhar_chamado`, `adicionar_interacao`, `cancelar_chamado`, `anexar_arquivo`, `consultar_empresas_usuario`, `consultar_pedido`, `consultar_nota`, `consultar_parceiro`, `consultar_top`, `consultar_periodo_contabil`, `consultar_produto`, `consultar_conta_contabil`, `consultar_centro_resultado`.
**Base de conhecimento:** taxonomia das 8 categorias, FAQ contábil/fiscal interno, política de prioridade e SLA.
**Tela inicial:** "Como podemos ajudar?" + "Selecione o assunto da sua solicitação:" + 8 cards + campo de texto livre + acesso ao histórico.
**Regra de ouro:** nunca inventar dado, nunca afirmar execução sem confirmação do sistema, nunca executar ação sem autorização — quando não puder, registrar e encaminhar.

---
*Fim do documento.*
