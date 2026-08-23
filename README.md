# 🏢 Personalizações Sankhya

## 🤖 Otimizado para Cursor IA

Este repositório foi **especialmente otimizado para uso com Cursor IA**, aproveitando todas as funcionalidades avançadas de IA do editor:

- ✅ **Arquivo `.cursorrules`** configurado com regras específicas para todo o repositório
- ✅ **Template base** (`Template/`) otimizado para novas personalizações
- ✅ **Documentação estruturada** para consultas em linguagem natural
- ✅ **Exemplos práticos** em SatyaPass e projetos reais
- ✅ **Base de conhecimento** completa em ZDevDoc

**Como usar com Cursor IA**:
1. Abra o repositório no Cursor
2. Use `Cmd/Ctrl + K` para gerar código seguindo os padrões do Template
3. Use `Cmd/Ctrl + L` para consultar documentação em linguagem natural
4. Use `Cmd/Ctrl + I` para edições inline inteligentes
5. Consulte `Template/CURSOR_IA_GUIA.md` para guia completo

---

Repositório centralizado para personalizações e integrações do sistema Sankhya, atendendo múltiplas empresas com soluções customizadas.

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Empresas Atendidas](#-empresas-atendidas)
- [Como Contribuir](#-como-contribuir)
- [Padrões de Desenvolvimento](#-padrões-de-desenvolvimento)
- [Deploy e Instalação](#-deploy-e-instalação)
- [Documentação](#-documentação)

## 🎯 Objetivo do Repositório

Este repositório é o **centro de conhecimento e desenvolvimento** para personalizações Sankhya, otimizado para uso exclusivo com **Cursor IA**.

### Objetivos Principais

1. **Template Base** (`Template/`)
   - Fornecer estrutura padronizada para novas personalizações
   - Consolidar melhores práticas e padrões estabelecidos
   - Documentação completa para desenvolvimento com IA
   - Templates prontos para uso imediato

2. **Base de Conhecimento** (`ZDevDoc/`)
   - Documentação completa Sankhya consolidada
   - Guias de desenvolvimento e técnicas avançadas
   - Referência para consultas em linguagem natural

3. **Exemplos Práticos** (`SatyaPass/`)
   - 248 exemplos de código funcionais
   - Padrões testados e validados
   - Referência para implementações reais

4. **Projetos Reais**
   - Implementações de produção
   - Padrões validados em uso real
   - Referência para novas personalizações

### Princípios Fundamentais

- ✅ **Manutenibilidade** - Código limpo, sem comentários, autoexplicativo
- ✅ **Escalabilidade** - Arquitetura preparada para crescimento
- ✅ **Compatibilidade** - Funcionamento em diferentes versões do Sankhya
- ✅ **Rastreabilidade** - Logs detalhados e auditoria completa
- ✅ **Excelência** - ZERO comentários, menor número de linhas, JDK8 máximo
- ✅ **Otimização IA** - Estrutura preparada para Cursor IA

## 🎯 Visão Geral

Este repositório contém personalizações desenvolvidas para o sistema Sankhya, organizadas por empresa e funcionalidade. Cada personalização é desenvolvida seguindo padrões estabelecidos para garantir qualidade máxima.

## 📁 Estrutura do Projeto

```
personalizacoes/
├── README.md                           # Este arquivo
├── .gitignore                          # Configuração do Git
├── .cursorrules                        # ⭐ Regras Cursor IA (CRÍTICO)
├── .cursorignore                       # Arquivos ignorados pelo Cursor
├── Template/                           # ⭐ Template base otimizado
│   ├── INSTRUCOES_DESENVOLVIMENTO.md  # ⭐ Instruções completas e templates
│   ├── CONHECIMENTO_CONSOLIDADO.md    # 🎓 Consolidação máxima de conhecimento
│   ├── REFERENCIA_SANKHYA.md          # 📊 Referência Sankhya completa
│   ├── INDICE_CONHECIMENTO_SANKHYA.md # 📑 Índice rápido de conhecimento
│   ├── CURSOR_IA_GUIA.md              # 🤖 Guia específico Cursor IA
│   ├── CHANGELOG.md                   # 📝 Histórico de mudanças
│   ├── README.md                      # 📖 Visão geral do template
│   ├── pom.xml                        # Configuração Maven
│   ├── lib/                           # JARs Sankhya (não versionados)
│   └── src/br/com/cliente/           # Código fonte exemplo
│       ├── action/botaoAcao/         # Botão de ação exemplo
│       ├── model/dto/                # DTOs exemplo
│       ├── repository/                # Repositórios (AbstractRepository)
│       ├── service/                   # Serviços exemplo
│       └── util/                      # Utilitários padrão
│           ├── Constants.java        # Constantes centralizadas
│           ├── DownloadHelper.java   # Helper para downloads
│           ├── FileGenerator.java    # Geração de arquivos
│           ├── Formatter.java        # Formatação de dados
│           ├── MessageHelper.java   # Mensagens formatadas
│           └── Validator.java        # Validações comuns
├── ZDevDoc/                            # 📚 Base de conhecimento Sankhya completa
│   ├── INDICE_PRINCIPAL.md            # Índice principal
│   ├── 01-fundamentos/                # Conceitos básicos
│   ├── 02-desenvolvimento/            # Guias de desenvolvimento
│   ├── 03-integracao/                 # Integrações
│   ├── 04-avancado/                   # Técnicas avançadas
│   └── ...
├── SatyaPass/                          # 💡 Exemplos práticos funcionais
│   ├── exemplos/                      # 248 exemplos de código
│   └── INDICE_EXEMPLOS.md             # Índice de exemplos
├── GuaranaMineiro/                     # Personalizações para Guarana Mineiro
│   ├── br.com.performaxxi.btnacao/     # Integração PERFORMAXXI - Pedidos
│   │   ├── src/                        # Código fonte Java
│   │   ├── docs/                       # Documentação técnica
│   │   ├── pom.xml                     # Configuração Maven
│   │   └── target/                     # Arquivos compilados (ignorados)
│   └── br.com.performaxxi.evento.recebimento/ # Integração PERFORMAXXI - Recebimentos
│       ├── src/                        # Código fonte Java
│       ├── docs/                       # Documentação técnica
│       ├── pom.xml                     # Configuração Maven
│       └── target/                     # Arquivos compilados (ignorados)
├── Eletromac/                          # Personalizações para Eletromac
│   └── src/br/com/sankhya/botaoacao/   # Automação de processos
│       ├── AutomatizacaoProcessos.java # Processo principal
│       └── AutomatizacaoProcessosHelper.java # Helper classes
├── Sankhya/                            # Personalizações genéricas Sankhya
│   ├── BotoesAcaoSankhya/              # Botões de ação genéricos
│   ├── AcoesMaha/                      # Integração com sistema Maha
│   ├── AutoCmpExtension/               # Extensão de automação
│   ├── ExtensaoCelula-Model/           # Extensão de célula (modelo)
│   ├── ExtensaoCelula-VC/              # Extensão de célula (view controller)
│   ├── ExtensaoCriterioHolding-Model/  # Extensão de critério holding
│   ├── ExtensaoCriterioHolding-VC/     # Extensão de critério holding (VC)
│   ├── ExtensaoExportadorNovidadesRlzJob/ # Exportador de novidades
│   ├── MigracaoProcesso/               # Migração de processos
│   ├── processos/                      # Processos diversos
│   └── ProcessosModelo/                # Modelos de processos
├── Iwannasleep/                        # Personalizações para Iwannasleep
│   └── src/br/com/sankhya/botaoacao/   # Automação de processos
│       ├── AutomatizacaoProcessos.java # Processo principal
│       ├── AutomatizacaoProcessosHelper.java # Helper classes
│       └── evento/                     # Eventos programáveis
│           ├── ReservaEstoqueExpiradaEventoProgramavel.java
│           └── ReservaEstoqueMatrizEventoProgramavel.java
├── Credpar/                            # Personalizações para Credpar
│   ├── CredParApp/                     # Aplicativo móvel
│   ├── CredParApp-Model-Tiny/          # Modelo de dados
│   └── CredParApp-VC/                  # View Controller
├── Megleo/                             # Personalizações para Megleo
│   └── src/br/com/sankhya/action/      # Sistema de integração de transportadoras
│       ├── acaoAgendada/               # Ações agendadas
│       ├── botaoacao/                  # Botões de ação
│       ├── evento/                     # Eventos do sistema
│       ├── funcoes/                    # Funções auxiliares
│       ├── RegraNegocio/               # Regras de negócio
│       └── Utilitarios/                # Utilitários
├── Denver/                              # Personalizações para Denver
│   ├── src/br/com/denver/tsl/          # Integração TSL
│   ├── docs/                           # Documentação técnica
│   └── pom.xml                         # Configuração Maven
├── PetKids/                             # Personalizações para PetKids
│   ├── src/br/com/petkids/neogrid/     # Integração Neogrid
│   ├── docs/                           # Documentação
│   └── pom.xml                         # Configuração Maven
├── NovoMundo/                           # Personalizações para NovoMundo
│   ├── datadictionary/                 # Metadados das tabelas
│   ├── dbscripts/                      # Scripts SQL
│   ├── src/me/handz/importacao/        # Código-fonte Java (EJBs)
│   ├── web/                            # Interfaces HTML5/Flex
│   └── extension.xml                   # Descritor da extensão
├── Monteccer/                           # Personalizações para Monteccer
│   ├── src/br/com/monteccer/           # Integração Monteccer
│   └── pom.xml                         # Configuração Maven
├── Emfal/                               # Personalizações para Emfal
│   ├── src/br/com/emfal/serasa/        # Integração Serasa Experian
│   ├── docs/                           # Documentação
│   └── pom.xml                         # Configuração Maven
├── CVSBeneficios/                       # Personalizações para CVS Benefícios
│   ├── src/br/com/cvs/                 # Código fonte
│   ├── docs/                           # Documentação
│   └── pom.xml                         # Configuração Maven
├── Brassol/                             # Personalizações para Brassol
│   ├── src/br/                         # Código fonte
│   ├── docs/                           # Documentação completa
│   └── pom.xml                         # Configuração Maven
├── SERPA/                               # Personalizações para SERPA
│   ├── src/br/com/serpa/               # Integração WMS TSL
│   ├── docs/                           # Documentação
│   └── pom.xml                         # Configuração Maven
├── P&D/                                 # Pesquisa e Desenvolvimento
│   ├── src/br/com/pd/action/           # Botões de ação
│   ├── docs/                           # Documentação
│   └── pom.xml                         # Configuração Maven
├── SankhyaJX/                           # Biblioteca JavaScript
│   ├── jx.js                           # Versão desenvolvimento
│   ├── jx.min.js                       # Versão produção
│   └── README.md                       # Documentação
└── [FuturasEmpresas]/                  # Futuras empresas
```

## 🏢 Empresas Atendidas

### 🥤 Guarana Mineiro
**Status**: ✅ Ativo  
**Versão**: 1.0.0  
**Cliente**: Guarana Mineiro  
**Tecnologias**: Java 8, Sankhya Extensions, REST API, Basic Auth, Gson JSON

#### 📦 **Integração PERFORMAXXI - Sistema Unificado** (`integracao-performaxxi-1.0.0.jar`)

Sistema de integração unificada entre Sankhya e PERFORMAXXI, contendo três funcionalidades principais para automatizar processos logísticos e de comunicação.

##### 1. **Botão de Ação (botaoAcao)** - `IntegraPerformaxxi.java`

**Classe Principal**: `br.com.performaxxi.action.botaoAcao.IntegraPerformaxxi`

**Funcionalidades Detalhadas**:
- **Integração Manual de Pedidos**: Envio de pedidos selecionados para otimização de rotas no PERFORMAXXI
- **Query Dinâmica**: Construção de SQL baseada em parâmetros configuráveis (período, filtros, status)
- **Filtro de Enviados**: Opção para incluir ou excluir pedidos já enviados anteriormente
- **Processamento em Lote**: Suporte a múltiplos pedidos por execução
- **Validação Robusta**: Validação de parâmetros e dados antes do envio
- **Tratamento de Erros**: Tratamento específico para erros de autenticação (401)
- **Logs Detalhados**: Registro completo em `AD_INTPERFORMAXXILOG` com:
  - Número de lote único por execução
  - Status (SUCESSO, ERRO, PARCIAL)
  - Quantidade de sucessos e erros
  - Tempo de execução em milissegundos
  - Parâmetros utilizados em JSON
  - Mensagens de erro detalhadas

**Classes Auxiliares**:
- `PerformaxxiIntegracaoHelper`: Helper com métodos para:
  - Extração de parâmetros do contexto
  - Execução de query dinâmica com filtros
  - Filtro de pedidos já enviados
  - Conversão de dados para formato API
  - Registro de logs estruturados
- `PerformaxxiAPI`: Classe de comunicação com API:
  - Autenticação Basic Auth (Base64)
  - Timeout configurável (conexão: 30s, leitura: 60s)
  - Serialização JSON com Gson
  - Tratamento de respostas HTTP
  - Geração de Correlation ID para rastreabilidade

**Parâmetros Suportados**:
- `DATA_INICIAL`: Data inicial do período (obrigatório)
- `DATA_FINAL`: Data final do período (obrigatório)
- `CODPARC`: Código do parceiro/cliente (opcional)
- `CODEMP`: Código da empresa (opcional)
- `STATUSNOTA`: Status da nota (padrão: 'L' - Liberada)
- `INCLUIR_ENVIADOS`: Incluir pedidos já enviados (padrão: false)

**Métricas de Performance**:
- ✅ **Evidência de Funcionamento**: Integração executada em 239ms
- ✅ **Evidência de Logs**: 84 registros de auditoria com rastreabilidade completa
- ✅ **Taxa de Sucesso**: Monitoramento via tabela `AD_INTPERFORMAXXILOG`

##### 2. **Evento de Recebimento (evento)** - `RecebimentoEvento.java`

**Classe Principal**: `br.com.performaxxi.action.evento.RecebimentoEvento`

**Funcionalidades Detalhadas**:
- **Trigger Automático**: Disparado em `afterInsert` e `afterUpdate` da tabela `TGFFIN`
- **Validação de Campos Modificados**: Processa apenas quando campos específicos são alterados:
  - `CODTIPOPERBAIXA`: Código do tipo de operação de baixa
  - `DHTIPOPERBAIXA`: Data/hora do tipo de operação de baixa
  - `DHBAIXA`: Data/hora da baixa
  - `VLRBAIXA`: Valor da baixa
- **Filtro de Receitas**: Processa apenas receitas (`RECDESP = 1`) com baixa válida
- **Notificação Mobile**: Envio de mensagem para motoristas via API PERFORMAXXI
- **Logging Assíncrono**: Logging em thread separada para não bloquear transação
- **Correlation ID**: Rastreabilidade completa de eventos

**Fluxo de Processamento**:
1. Validação de campos modificados (`ModifingFields`)
2. Verificação se é receita com baixa válida
3. Busca de dados adicionais (ordem de carga, nome do cliente)
4. Criação de DTO de recebimento
5. Conversão para JSON e envio para API
6. Registro de log independente (assíncrono)

**Tabela de Log**: `AD_RECEBIMENTOLOG`
- Campos: NUFIN, CODPARC, VLRBAIXA, DHBAIXA, STATUS, MENSAGEM, CORRELATION_ID, DHEXECUCAO

##### 3. **Ação Agendada (acaoAgendada)** - `ComprovantesEntrega.java`

**Classe Principal**: `br.com.performaxxi.action.acaoAgendada.ComprovantesEntrega`

**Funcionalidades Detalhadas**:
- **Consulta Automática**: Execução programada para buscar comprovantes de entrega
- **Filtro por Data**: Busca veículos com ordem de carga do dia atual (`DTNEG = TRUNC(SYSDATE)`)
- **Processamento por Veículo**: Consulta individual de comprovantes por veículo
- **Salvamento em Tabela**: Comprovantes salvos em `AD_COMPROVANTES`
- **Notificação de Erros**: Envio de email em caso de falha na execução
- **Métricas de Performance**: Registro de tempo de execução e quantidade processada

**Query de Busca**:
```sql
SELECT CAB.DTNEG AS "data", VEI.PLACA AS "idRastreador"
FROM TGFCAB CAB
INNER JOIN TGFORD ORD ON CAB.ORDEMCARGA = ORD.ORDEMCARGA
INNER JOIN TGFVEI VEI ON VEI.CODVEICULO = ORD.CODVEICULO
WHERE CAB.ORDEMCARGA IS NOT NULL
  AND CAB.ORDEMCARGA != 0
  AND CAB.DTNEG = TRUNC(SYSDATE)
```

**Tabela de Armazenamento**: `AD_COMPROVANTES`
- Campos: ID_COMPROVANTE, ID_RASTREADOR, DATA, CLASSE, CONJUNTO, DADOS_JSON, DHEXECUCAO

**Configuração de Agendamento**:
- Frequência: Diária (configurável)
- Horário: Configurável via ScheduledAction do Sankhya
- Timeout: Configurável (padrão: sem timeout)

**Arquitetura Unificada**:
```
GuaranaMineiro/
├── src/br/com/performaxxi/action/
│   ├── botaoAcao/         # Classes do Botão de Ação
│   ├── evento/            # Classes do Evento Recebimento
│   └── acaoAgendada/      # Classes da Ação Agendada
├── docs/                  # Documentação centralizada
│   ├── botaoAcao/         # Documentação do Botão de Ação
│   ├── evento/            # Documentação do Evento Recebimento
│   └── acaoAgendada/      # Documentação da Ação Agendada
└── target/integracao-performaxxi-1.0.0.jar  # JAR único final
```

**Documentação**: 
- [Documentação Centralizada](GuaranaMineiro/docs/README.md)
- [Botão de Ação](GuaranaMineiro/docs/botaoAcao/)
- [Evento Recebimento](GuaranaMineiro/docs/evento/)
- [Ação Agendada](GuaranaMineiro/docs/acaoAgendada/)

### ⚡ Eletromac
**Status**: ✅ Ativo  
**Versão**: 1.0.0  
**Cliente**: Eletromac  
**Tecnologias**: Java 8, ScheduledAction, CACHelper, CentralItemNota, ConfirmacaoNotaHelper

#### 📦 **Automação de Processos - Transferência Automática de Estoque**

Sistema completo de automação para transferência automática de estoque entre empresas (empresa 1 → empresa 6) com geração automática de lotes de notas e confirmação.

**Classe Principal**: `br.com.sankhya.botaoacao.AutomatizacaoProcessos`

**Funcionalidades Detalhadas**:

1. **Processo Principal** (`AutomatizacaoProcessos.java`):
   - Implementa `AcaoRotinaJava` (botão de ação) e `ScheduledAction` (ação agendada)
   - Execução com prioridade baixa (`LOW_PRIORITY`)
   - Sem timeout configurado (`setCanTimeout(false)`)
   - Logging detalhado de cada etapa do processo

2. **Fluxo de Processamento** (`AutomatizacaoProcessosHelper.java`):
   - **Etapa 1 - Notas Pendentes**: `notasPendentes()` - Busca notas pendentes de transferência
   - **Etapa 2 - Geração de Lote**: `gerarLoteNotas()` - Gera lote quando há notas pendentes
   - **Etapa 3 - Transferência**: `gerarTransferencia()` - Cria notas de transferência usando CACHelper
   - **Etapa 4 - Confirmação**: `confirmarNotas()` - Confirma notas automaticamente
   - **Etapa 5 - Lote NFe**: Geração automática de lote NFe após confirmação

3. **Integração com APIs Nativas**:
   - **CACHelper**: Criação de cabeçalho e itens de nota
   - **CentralItemNota**: Inicialização de produtos com preços e custos
   - **ConfirmacaoNotaHelper**: Confirmação automática de notas
   - **BarramentoRegra**: Aplicação de regras de negócio
   - **ServicosNFeHelper2**: Geração de lote NFe

4. **Controle de Estoque**:
   - Verificação de estoque negativo antes da transferência
   - Controle de lote para produtos com rastreabilidade
   - Validação de disponibilidade por empresa

5. **Sistema de Notificações**:
   - Notificação por email em caso de falha
   - Sistema de fila de mensagens (`TMDFMG`)
   - Logging estruturado de erros

**Arquitetura**:
```
Eletromac/
├── src/br/com/sankhya/botaoacao/
│   ├── AutomatizacaoProcessos.java      # Classe principal (Action + ScheduledAction)
│   └── AutomatizacaoProcessosHelper.java # Helper com lógica de negócio
├── SQL Scripts/                          # Scripts SQL diversos
│   ├── PROC_ANALISE_COMPRAS_NOVA.sql
│   ├── PROC_LIMCREDITO.sql
│   ├── TRG_INS_UPD_TGFCAB_REGRA.SQL
│   └── ...
└── Relatórios/                           # Relatórios JasperReports
    ├── COMISSAO DE VENDEDORES/
    ├── ORCAMENTO DE COMPRA/
    └── PEDIDO DE COMPRA/
```

**Métodos Principais do Helper**:
- `notasPendentes()`: Busca notas pendentes usando NativeSql
- `existeNotas()`: Verifica se há notas para processar
- `gerarLoteNotas()`: Gera lote usando APIs nativas
- `gerarTransferencia()`: Cria notas de transferência
- `confirmarNotas()`: Confirma notas automaticamente

**Configuração de Contexto**:
- Autenticação: Usuário SUP com permissões administrativas
- Propriedades de sessão: CentralCompraVenda habilitada
- Timeout: Desabilitado para processamento longo

**Relatórios Adicionais**:
- Comissão de Vendedores (PDF, Excel, Word)
- Orçamento de Compra (PDF, Excel, Word)
- Pedido de Compra (PDF, Excel, Word)

### 🏢 Sankhya (Personalizações Genéricas)
**Status**: ✅ Ativo  
**Versão**: Múltiplas versões  
**Cliente**: Sankhya (Personalizações genéricas reutilizáveis)  
**Tecnologias**: Java 8, Sankhya Extensions, EJB, Processos Sankhya, REST API

#### 📦 **Componentes Principais**

##### 1. **BotoesAcaoSankhya** - Botões de Ação Reutilizáveis
**Localização**: `Sankhya/BotoesAcaoSankhya/`

**Classes Principais**:
- **`IncluirOS.java`**: Criação de ordens de serviço via API
- **`SalvarApontamento.java`**: Sistema de apontamentos de serviços
- **`EncaminharFila.java`**: Gerenciamento de filas de atendimento
- **`TirarFila.java`**: Remoção de itens de filas
- **`OrdenacaoCursosUniversidade.java`**: Ordenação de cursos universitários
- **`OrdenacaoCursosUniversidadeBatch.java`**: Processamento em lote de cursos

**Funcionalidades**:
- Integração com módulo de serviços do Sankhya
- Gerenciamento de filas e atendimentos
- Processamento em lote de operações
- APIs para criação de documentos

##### 2. **AcoesMaha** - Integração Sistema Maha
**Localização**: `Sankhya/AcoesMaha/`

**Classes Principais**:
- **`RequestAPI.java`**: Integração com sistema Maha via REST
- **`ImportarVendaBase.java`**: Importação de vendas base do Maha

**Funcionalidades**:
- Sistema de diagnóstico e questionários
- Integração REST com sistema Maha
- Importação de dados de vendas
- Processamento de respostas de APIs

**Arquivos SQL**: 23 arquivos SQL com procedures e triggers relacionados

##### 3. **Processos Sankhya** - Processos de Manutenção e Gestão
**Localização**: `Sankhya/processos/`

**Subprojetos**:
- **ProcessoManutencaoSnk**: Processos de manutenção (versão Snk)
- **ProcessoManutencaoSnkBack**: Processos de manutenção (versão Back)

**Classes Principais**:
- **`FormularioSolicitacaoManutencao.java`**: Formulário de solicitação de manutenção
- **`EventoOSManutencaoSk.java`**: Eventos de ordem de serviço de manutenção
- **`EventoItemOSManutencaoSk.java`**: Eventos de itens de OS de manutenção
- **`BloqueioTesteEntradaAberto.java`**: Bloqueio de teste de entrada aberto
- **`BloqueioEnvioSoftwareOSManutencaoSk.java`**: Bloqueio de envio de software
- **`BloqueioAlteracaoOSManutencaoSk.java`**: Bloqueio de alteração de OS
- **`BloqueioExclusaoApontamentoManutencaoSk.java`**: Bloqueio de exclusão de apontamento
- **`BloqueioEnvioServiceDeskManutencaoSK.java`**: Bloqueio de envio para Service Desk
- **`RotinaTeste.java`**: Rotinas de teste (implementa `AcaoRotinaJava` e `EventoProcessoJava`)
- **`RemoverInstanciaProcesso.java`**: Remoção de instâncias de processos
- **`PopularUCP.java`**: Popular UCP (Unidade de Controle de Processo)
- **`PopularUCP2.java`**: Versão alternativa de Popular UCP
- **`PopularUCPTarefa.java`**: Popular UCP para tarefas
- **`MigrarInstancias.java`**: Migração de instâncias de processos
- **`EnviaSinal.java`**: Envio de sinais para processos
- **`AtualizarVersaoProcesso.java`**: Atualização de versão de processos
- **`AtualizaExpressaoCandidato.java`**: Atualização de expressões candidatas
- **`AtribuirDesatribuirTarefa.java`**: Atribuição/desatribuição de tarefas
- **`RecriaExpressaoTWFELE.java`**: Recriação de expressões TWFELE

**Funcionalidades**:
- Gestão completa de processos de manutenção
- Sistema de bloqueios e validações
- Eventos programados para automação
- Migração e atualização de processos
- Gestão de tarefas e atribuições

##### 4. **MigracaoProcesso** - Ferramentas de Migração
**Localização**: `Sankhya/MigracaoProcesso/`

**Classes Principais**:
- **`MigrarInstancias.java`**: Migração de instâncias de processos
- **`ImportarAtividadesProcesso.java`**: Importação de atividades de processos
- **`ImportarAtividadesInstancia.java`**: Importação de atividades de instâncias

**Funcionalidades**:
- Migração de processos entre ambientes
- Importação de atividades e instâncias
- Validação de integridade de dados
- Backup e restauração de processos

##### 5. **Extensões de Célula** - Funcionalidades Avançadas para Células
**Localização**: `Sankhya/ExtensaoCelula-Model/` e `Sankhya/ExtensaoCelula-VC/`

**Componentes**:
- **Model**: Modelo de dados com 9 arquivos SQL e 5 XMLs
- **VC**: View Controller com 2 arquivos XML
- **VC-Flex**: Interface Flex com 5 arquivos MXML

**Funcionalidades**:
- Extensão de células em telas Sankhya
- Validações customizadas
- Comportamentos específicos por célula

##### 6. **Extensões de Critério Holding** - Rateio e Desconto
**Localização**: `Sankhya/ExtensaoCriterioHolding-Model/` e `Sankhya/ExtensaoCriterioHolding-VC/`

**Componentes**:
- **Model**: 14 arquivos XML, 9 arquivos Java, 2 arquivos SQL
- **VC**: 2 arquivos XML
- **VC-Flex**: 10 arquivos MXML

**Funcionalidades**:
- Critérios de rateio customizados
- Regras de desconto específicas
- Cálculos avançados de holding

##### 7. **AutoCmpExtension** - Extensão de Automação
**Localização**: `Sankhya/AutoCmpExtension/`

**Componentes**:
- 5 arquivos XML
- 2 arquivos SQL
- 1 arquivo Java

**Funcionalidades**:
- Automação de processos
- Extensões de componentes

##### 8. **ExtensaoExportadorNovidadesRlzJob** - Exportador de Novidades
**Localização**: `Sankhya/ExtensaoExportadorNovidadesRlzJob/`

**Componentes**:
- 7 arquivos XML
- 1 arquivo Java

**Funcionalidades**:
- Exportação de novidades
- Job agendado para exportação
- Integração com sistemas externos

##### 9. **ProcessosModelo** - Templates de Processos
**Localização**: `Sankhya/ProcessosModelo/`

**Componentes**:
- 11 arquivos Java

**Funcionalidades**:
- Templates reutilizáveis de processos
- Padrões de implementação
- Exemplos de uso

**Estatísticas do Projeto Sankhya**:
- **Total de Classes Java**: 86+ arquivos
- **Total de Arquivos SQL**: 23+ arquivos
- **Total de Arquivos XML**: 30+ arquivos
- **Total de Arquivos MXML**: 15+ arquivos
- **Implementações de Interfaces Sankhya**: 20+ classes

### 😴 Iwannasleep
**Status**: ✅ Ativo  
**Versão**: 1.0.0  
**Cliente**: Iwannasleep  
**Tecnologias**: Java 8, ScheduledAction, EventoProgramavelJava, CACHelper, CentralItemNota

#### 📦 **Automação de Processos - Gerenciamento de Reservas de Estoque**

Sistema completo de automação para gerenciamento de reservas de estoque com controle de expiração, geração de documentos matriz e confirmação automática.

**Classe Principal**: `br.com.sankhya.botaoacao.AutomatizacaoProcessos`

**Funcionalidades Detalhadas**:

1. **Processo Principal** (`AutomatizacaoProcessos.java`):
   - Implementa `AcaoRotinaJava` e `ScheduledAction`
   - Execução com prioridade baixa e sem timeout
   - Três processos principais:
     - `gerarDocumentosMatriz()`: Geração de documentos para matriz
     - `confirmarReservasEstoque()`: Confirmação de reservas válidas
     - `cancelarReservasEstoqueAntigas()`: Cancelamento de reservas expiradas

2. **Helper de Automação** (`AutomatizacaoProcessosHelper.java`):
   - **Geração de Documentos Matriz**: Criação de documentos usando CACHelper
   - **Confirmação de Reservas**: Validação e confirmação de reservas válidas
   - **Cancelamento de Reservas**: Limpeza automática de reservas expiradas
   - **Coleta de Erros**: Sistema de coleta de mensagens de erro
   - **Notificação de Falhas**: Envio de email com hash MD5 para evitar duplicatas

3. **Eventos Programáveis**:
   - **ReservaEstoqueExpiradaEventoProgramavel.java**:
     - Disparado em eventos de reserva de estoque
     - Validação de data de expiração
     - Cancelamento automático de reservas expiradas
   - **ReservaEstoqueMatrizEventoProgramavel.java**:
     - Gerenciamento de reservas por matriz
     - Validação de regras específicas por matriz
     - Processamento automático de reservas

**Arquitetura**:
```
Iwannasleep/
├── src/br/com/sankhya/botaoacao/
│   ├── AutomatizacaoProcessos.java              # Classe principal
│   ├── AutomatizacaoProcessosHelper.java        # Helper com lógica
│   └── evento/
│       ├── ReservaEstoqueExpiradaEventoProgramavel.java
│       └── ReservaEstoqueMatrizEventoProgramavel.java
```

**Métodos Principais**:
- `gerarDocumentosMatriz()`: Gera documentos para matriz usando CACHelper
- `confirmarReservasEstoque()`: Confirma reservas válidas
- `cancelarReservasEstoqueAntigas()`: Cancela reservas expiradas
- `getMensagensErro()`: Retorna lista de erros coletados
- `notificarFalhaViaEmail()`: Envia email com hash MD5 para evitar duplicatas

**Sistema de Notificações**:
- Hash MD5 da mensagem para evitar emails duplicados
- Verificação de mensagem já enviada antes de notificar
- Lista de destinatários configurável
- Sistema de fila de mensagens (`TMDFMG`)

**Controle de Expiração**:
- Validação de data de expiração de reservas
- Cancelamento automático de reservas antigas
- Logging de operações de cancelamento

### 💳 Credpar
**Status**: ✅ Ativo  
**Versão**: Múltiplas versões  
**Cliente**: Credpar  
**Tecnologias**: Java 8, EJB 3.0, TinyEJB, View Controller, HTML5, JavaScript, Flex

#### 📦 **Sistema Completo de Recarga de Celular e Gestão de Créditos**

Sistema completo de gestão de recargas de celular com aplicativo móvel, integração com operadoras e controle de créditos e limites.

##### 1. **CredParApp** - Aplicativo Móvel
**Localização**: `Credpar/CredParApp/`

**Componentes**:
- **189 arquivos de assets**:
  - 81 arquivos PNG (ícones e imagens)
  - 36 arquivos JavaScript
  - 25 arquivos HTML
  - Outros arquivos de recursos

**Funcionalidades**:
- Interface web completa para aplicativo móvel
- Gestão de recargas de celular
- Consulta de extratos e histórico
- Sistema de autenticação e segurança
- Interface responsiva para dispositivos móveis

**Tecnologias**:
- HTML5 para estrutura
- JavaScript para interatividade
- CSS para estilização
- Build.xml para compilação

##### 2. **CredParApp-Model-Tiny** - Modelo de Dados
**Localização**: `Credpar/CredParApp-Model-Tiny/`

**Componentes**:
- **6 arquivos Java** (EJBs):
  - Entidades do modelo de dados
  - Session Beans para acesso a dados
- **9 arquivos XML**: Configurações EJB e mapeamentos
- **8 arquivos SQL**: Scripts de criação de tabelas e procedures
- **TinyEJB**: Framework leve para EJBs

**Entidades Principais**:
- **`OperadoraCelular`**: Gestão de operadoras de telefonia
  - Cadastro de operadoras
  - Configurações de integração
  - Parâmetros de API
- **`HistoricoRecargaCel`**: Histórico completo de recargas
  - Registro de todas as recargas realizadas
  - Status de processamento
  - Dados de transação
- **`ProdutoOperadora`**: Produtos disponíveis por operadora
  - Catálogo de produtos
  - Preços e comissões
  - Disponibilidade
- **`OndeComprar`**: Localização de pontos de venda
  - Geolocalização
  - Informações de contato
  - Disponibilidade de produtos

**Funcionalidades**:
- Modelo de dados completo com relacionamentos
- Persistência com TinyEJB
- Procedures SQL para operações complexas
- Integração com banco de dados Oracle

##### 3. **CredParApp-VC** - View Controller
**Localização**: `Credpar/CredParApp-VC/`

**Componentes**:
- **1 arquivo Java**: View Controller principal
- **1 arquivo XML**: Configuração do módulo

**Funcionalidades**:
- **Busca de Clientes**: Pesquisa avançada de clientes
- **Extratos**: Geração de extratos detalhados
- **Gestão de Lojistas**: Cadastro e gestão de lojistas
- **Controle de Limites**: Monitoramento de limites utilizados
- **Sistema de Segmentação**: Segmentação de clientes
- **Relatórios**: Geração de relatórios diversos

**Integrações**:
- Integração com CredParApp-Model para acesso a dados
- Comunicação com APIs de operadoras
- Sistema de notificações

##### 4. **Web Services** - APIs REST
**Localização**: `Credpar/CredParApp-Model-Tiny/ws-credparapp/`

**Componentes**:
- **4 arquivos XML**: Configurações de web services
- **1 arquivo SQL**: Procedures para web services
- **1 arquivo EXT**: Extensão de configuração

**Funcionalidades**:
- APIs REST para integração externa
- Endpoints para recarga de celular
- Consulta de saldo e histórico
- Gestão de créditos e limites

**Arquitetura Completa**:
```
Credpar/
├── CredParApp/                    # Aplicativo móvel (189 arquivos)
│   ├── assets/                    # Recursos (PNG, JS, HTML)
│   └── build.xml                  # Build do aplicativo
├── CredParApp-Model-Tiny/         # Modelo de dados
│   ├── ejbsrc/                    # EJBs (6 arquivos Java)
│   ├── src/                       # SQL e XML (17 arquivos)
│   ├── ws-credparapp/             # Web Services (6 arquivos)
│   ├── tinyejb-xdoclet-build.xml # Build TinyEJB
│   └── xdoclet-build.xml          # Build XDoclet
└── CredParApp-VC/                 # View Controller
    ├── src/                       # Código Java
    └── META-INF/                  # Configurações
```

**Funcionalidades Principais**:
- ✅ **Recarga de Celular**: Sistema completo de recarga
- ✅ **Gestão de Operadoras**: Cadastro e configuração
- ✅ **Histórico Completo**: Registro de todas as transações
- ✅ **Controle de Créditos**: Limites e créditos por lojista
- ✅ **Extratos Detalhados**: Relatórios completos
- ✅ **Aplicativo Móvel**: Interface web responsiva
- ✅ **APIs REST**: Integração com sistemas externos
- ✅ **Sistema de Jobs**: Processamento automático
- ✅ **Geolocalização**: Pontos de venda mapeados

### 🚚 Megleo
**Status**: ✅ Ativo  
**Versão**: 1.0.0  
**Cliente**: Megleo  
**Tecnologias**: Java 8, REST API, JSON, ScheduledAction, EventoProgramavelJava

#### 📦 **Sistema de Integração de Transportadoras**

Solução completa para gestão de logística com integração via API REST para inserção automática de pedidos, busca de transportadoras e envio de notas confirmadas.

**Classes Principais**:

1. **InserirPedidoMegleo.java** (`br.com.sankhya.action.botaoacao`):
   - **Funcionalidade**: Botão de ação para inserção manual de pedidos
   - **Processamento**: 
     - Extração de dados do pedido (NUNOTA)
     - Cálculo de volumes e dimensões
     - Validação de CEP origem/destino
     - Envio para API de transportadora
     - Registro de log em `AD_LOGMEG`
   - **Parâmetros**: NUNOTA (obrigatório)

2. **EnviaPedido.java** (`br.com.sankhya.action.funcoes`):
   - **Funcionalidade**: Classe auxiliar para envio de pedidos
   - **Características**:
     - Construção de JSON com dados do pedido
     - Cálculo de volumes (peso, comprimento, altura, largura)
     - Validação de CEP origem/destino
     - Autenticação por token
     - Tratamento de respostas HTTP
   - **Campos Processados**:
     - CEP origem e destino
     - Peso global e por item
     - Dimensões (comprimento, altura, largura)
     - Valor do pedido
     - CNPJ do destinatário
     - Quantidade de volumes

3. **EnviaNotasConfirmadas.java** (`br.com.sankhya.action.acaoAgendada`):
   - **Funcionalidade**: Ação agendada para envio automático de notas confirmadas
   - **Processamento**:
     - Busca notas confirmadas não enviadas
     - Filtro por data e status
     - Envio em lote para API
     - Marcação de notas como enviadas
     - Logging de operações

4. **BuscaTransportadoraPedido.java** (`br.com.sankhya.action.botaoacao.BuscaTransportadora`):
   - **Funcionalidade**: Busca automática de transportadora para pedido
   - **Processamento**:
     - Consulta API de transportadoras
     - Comparação de cotações
     - Seleção automática da melhor opção
     - Atualização de dados do pedido

5. **RegistraLOG.java** (`br.com.sankhya.action.funcoes`):
   - **Funcionalidade**: Sistema de logging centralizado
   - **Tabela**: `AD_LOGMEG`
   - **Campos**: TITULO, CONTEUDO, DHEXECUCAO, NUNOTA
   - **Métodos**:
     - `insereRegistro(String titulo, String conteudo)`
     - `insereRegistro(String titulo, String conteudo, BigDecimal nunota)`
     - `insereRegistroTransacaoAutomatica(...)`

**Eventos Programáveis**:
- **alteracaoProduto.java**: Disparado em alterações de produto
- **alteraParceiroTransportador.java**: Disparado em alterações de parceiro transportador

**Regras de Negócio**:
- **EnviaNotaConfirmar.java**: Regra para envio automático ao confirmar nota

**Arquitetura**:
```
Megleo/
├── src/br/com/sankhya/action/
│   ├── acaoAgendada/
│   │   └── EnviaNotasConfirmadas.java
│   ├── botaoacao/
│   │   ├── InserirPedidoMegleo.java
│   │   └── BuscaTransportadora/
│   │       └── BuscaTransportadoraPedido.java
│   ├── evento/
│   │   ├── alteracaoProduto.java
│   │   └── alteraParceiroTransportador.java
│   ├── funcoes/
│   │   ├── EnviaPedido.java
│   │   ├── RegistraLOG.java
│   │   └── AlteracaoSKU.java
│   ├── RegraNegocio/
│   │   └── EnviaNotaConfirmar.java
│   └── Utilitarios/
│       └── MensagemUtils.java
```

**Integrações**:
- **APIs de Transportadoras**: Comunicação HTTP/REST com JSON
- **Autenticação**: Token-based authentication
- **Validação de CEP**: Integração com serviços de CEP
- **Cálculo de Volumes**: Algoritmo automático de cálculo

**Arquitetura**:
```
Megleo/
├── src/br/com/sankhya/action/
│   ├── acaoAgendada/           # Ações agendadas
│   │   └── EnviaNotasConfirmadas.java
│   ├── botaoacao/              # Botões de ação
│   │   ├── InserirPedidoMegleo.java
│   │   └── BuscaTransportadora/
│   ├── evento/                 # Eventos do sistema
│   │   ├── alteracaoProduto.java
│   │   └── alteraParceiroTransportador.java
│   ├── funcoes/                # Funções auxiliares
│   │   ├── EnviaPedido.java
│   │   ├── RegistraLOG.java
│   │   └── AlteracaoSKU.java
│   ├── RegraNegocio/           # Regras de negócio
│   │   └── EnviaNotaConfirmar.java
│   └── Utilitarios/            # Utilitários
│       └── MensagemUtils.java
```

**Integrações**:
- **APIs de Transportadoras**: Comunicação HTTP/REST com JSON
- **Sistema de Agendamento**: Ações programadas via Sankhya
- **Autenticação por Token**: Sistema seguro de autenticação
- **Sistema de Logs**: Auditoria completa de operações
- **Tratamento de Erros**: Gestão robusta de falhas de integração

**Tecnologias**:
- Java 8+ com Sankhya Jape
- JSON para comunicação com APIs
- HTTP/REST para integração
- Oracle JDBC para banco de dados
- Scheduled Actions para agendamento

### 🏭 Denver
**Status**: ✅ Ativo  
**Versão**: 1.1.0  
**Cliente**: Denver  
**Tecnologias**: Java 8, Repository Pattern, DTO Pattern, Performance Optimization

#### 📦 **Integração TSL - Total Service Logística**

Sistema completo de integração entre Sankhya e Total Service Logística (TSL), gerando arquivos de exportação conforme manual de interfaces padrões WMS versão 1.2.

**Classe Principal**: `br.com.denver.tsl.action.botaoAcao.GerarArquivoTSL`

**Funcionalidades Principais**:

1. **REC_IN - Recebimento de Mercadorias Integrado**:
   - **Tamanho da linha**: 619 caracteres
   - **Campos principais**: CNPJ, Nota Fiscal, Item NF, Número Palete, Código Produto, Identificador Caixa, Peso Caixa, Data Produção, Data Vencimento, Lote, Info Complementar, Valor Unitário
   - **Query**: Busca recebimentos por `CODPARC` e `LOTE` (não mais por `NUNOTA`)
   - **Suporte a múltiplos lotes**: Gera arquivo separado para cada lote

2. **PED_IN - Expedição de Mercadorias**:
   - **Tamanho da linha**: 232 caracteres
   - **Campos principais**: CNPJ, Ordem Frete, Número Pedido, Item Pedido, Código Produto, Número Palete, Quantidade, Peso, Data Fabricação (DE/ATÉ), Lote, CNPJ Cliente
   - **Query**: Busca expedições por `NUNOTA` e `STATUSNOTA = 'L'`

**Arquitetura Detalhada**:

1. **Action Layer** (`GerarArquivoTSL.java`):
   - Captura de `NUNOTA` e `CODPARC` do contexto
   - Validação de parâmetros obrigatórios
   - Chamada do serviço principal
   - Geração de ZIP com todos os arquivos
   - Download automático via `DownloadHelper`

2. **Service Layer** (`TSLService.java`):
   - Orquestração da geração de arquivos
   - Coordenação entre repositórios e formatadores
   - Geração de nomes de arquivos padronizados
   - Agrupamento por lote (REC_IN)

3. **Repository Layer**:
   - **AbstractTSLRepository**: Repositório base com métodos genéricos
   - **RecebimentoRepository**: Query específica para recebimentos
     - JOIN com TGFITE para obter LOTE
     - Filtro por CODPARC e LOTE
     - Agrupamento por LOTE
   - **ExpedicaoRepository**: Query específica para expedições
     - JOIN com TGFCAB, TGFITE, TGFPRO
     - Filtro por STATUSNOTA = 'L'
     - Filtro por produtos com `AD_INTEGTOTALLOGISTICA = 'S'`

4. **DTO Layer**:
   - **RecebimentoDTO**: Dados de recebimentos com todos os campos necessários
   - **ExpedicaoDTO**: Dados de expedições com todos os campos necessários

5. **Util Layer**:
   - **TSLFormatter**: Formatação de campos conforme especificação TSL
     - Formatação posicional (campos fixos)
     - Preenchimento com espaços em branco
     - Formatação de números com vírgula como separador decimal
     - Formatação de datas (dd/MM/yyyy)
   - **FileGenerator**: Geração de arquivos com encoding Windows-1252
   - **TSLConstants**: Constantes centralizadas (tamanhos de campos, formatos)
   - **DownloadHelper**: Criação de ZIP e download automático

**Otimizações de Performance**:
- **Cache de espaços em branco**: `ConcurrentHashMap` para reutilização
- **ThreadLocal**: Para formatadores de data (evita sincronização)
- **Pré-alocação**: Listas com capacidade inicial de 1024
- **Buffer de 32KB**: Para escrita de arquivos
- **Operações otimizadas**: Arrays de char diretos (`getChars()`, `Arrays.fill()`)
- **StringBuilder pré-dimensionado**: Capacidade inicial de 200 caracteres

**Campos Adicionais Necessários** (TGFPRO):
- `AD_INTEGTOTALLOGISTICA` (CHAR(1)): Flag de integração ('S' para exportar)
- `AD_NUMEROPALETE` (VARCHAR2(26)): Número do palete (opcional)
- `AD_IDENTIFICADORCAIXA` (VARCHAR2(31)): Identificador da caixa (opcional, apenas REC_IN)

**Nomenclatura dos Arquivos**:
- Formato: `{INTERFACE}_{CNPJ}_{TIMESTAMP}.txt`
- Exemplo: `REC_IN_12345678000190_20250101120000.txt`

**Documentação**: [Denver/README.md](Denver/README.md) | [Documentação Técnica](Denver/docs/DOCUMENTACAO_TECNICA.md)

### 🐾 PetKids
**Status**: ✅ Ativo  
**Versão**: 1.0.0  
**Cliente**: Pet Kids (KELCO PET CARE PRODUTOS ANIMAIS LTDA)  
**CNPJ**: 07.056.359/0001-20  
**Tecnologias**: Java 8, ScheduledAction, Repository Pattern, Exception Handling, Logging Estruturado

#### 📦 **Integração Neogrid**

Sistema completo de integração entre Sankhya e plataforma Neogrid, gerando arquivos de exportação no formato especificado para sistemas de informações gerenciais para distribuidores.

**Classe Principal**: `br.com.petkids.neogrid.action.botaoAcao.GerarArquivoNeogrid`

**Funcionalidades Principais**:

1. **Botão de Ação e Rotina Agendada**:
   - Implementa `AcaoRotinaJava` e `ScheduledAction`
   - Execução manual via botão ou automática via agendamento
   - Parâmetros configuráveis via contexto ou `MGECoreParameter`

2. **Serviços Específicos** (`br.com.petkids.neogrid.service.impl`):
   - **VendedoresService**: Geração de relatório de vendedores (Layout v5.0)
   - **ClientesService**: Geração de relatório de clientes (Layout v5.0.4)
   - **ProdutosService**: Geração de relatório de produtos (Layout v5.1)
   - **VendasService**: Geração de relatório de vendas (Layout v5.2) - Notas fiscais e itens
   - **EstoqueService**: Geração de relatório de estoque (Layout v5.0)

3. **Repositórios** (`br.com.petkids.neogrid.repository`):
   - **AbstractNeogridRepository**: Repositório base com métodos genéricos
   - **VendedoresRepository**: Query específica para vendedores
   - **ClientesRepository**: Query específica para clientes
   - **ProdutosRepository**: Query específica para produtos
   - **VendasRepository**: Query específica para vendas (notas e itens)
   - **EstoqueRepository**: Query específica para estoque

4. **DTOs** (`br.com.petkids.neogrid.model.dto`):
   - **VendedorDTO**: Dados de vendedores
   - **ClienteDTO**: Dados de clientes
   - **ProdutoDTO**: Dados de produtos
   - **VendaDTO**: Dados de notas fiscais
   - **ItemVendaDTO**: Dados de itens de notas
   - **EstoqueDTO**: Dados de movimentações de estoque

5. **Enums** (`br.com.petkids.neogrid.model.enums`):
   - **TipoRelatorio**: Enum para tipos de relatórios
   - **StatusVendedor**: Enum para status de vendedores
   - **TipoFaturamento**: Enum para tipos de faturamento
   - **TipoFrete**: Enum para tipos de frete
   - **TipoNF**: Enum para tipos de nota fiscal

6. **Utilitários** (`br.com.petkids.neogrid.util`):
   - **NeogridFormatter**: Formatação de dados conforme padrão Neogrid
     - Remoção de acentos e caracteres especiais
     - Formatação de números decimais (ponto como separador)
     - Formatação de datas usando `TimeUtils`
     - Formatação de CNPJ/CPF (apenas números)
   - **FileGenerator**: Geração de arquivos com encoding ANSI
   - **NeogridLogFactory**: Sistema de logging estruturado com ExceptionUtils
   - **NeogridConstants**: Constantes centralizadas
   - **DownloadHelper**: Criação de ZIP e download automático

7. **Validação** (`br.com.petkids.neogrid.validation`):
   - **NeogridValidator**: Validações centralizadas de dados de entrada

8. **Exception Handling** (`br.com.petkids.neogrid.exception`):
   - **NeogridException**: Exceção base
   - **NeogridFileException**: Exceção para erros de arquivo
   - **NeogridRepositoryException**: Exceção para erros de repositório
   - **NeogridServiceException**: Exceção para erros de serviço
   - **NeogridValidationException**: Exceção para erros de validação

**Características Técnicas**:
- **Formato**: Flat File com separadores PIPE (|)
- **Encoding**: ANSI (Windows-1252)
- **Quebra de linha**: PC/Windows (CRLF)
- **Nomenclatura**: `{MascaraDocumento}_{CNPJFilial}_{CNPJIndustria}_{AAAAMMDDHHMMSS}.txt`
- **Máscaras**: RELVEN, RELCLI, RELPRO, VENDAS, RELEST

**Arquitetura**:
```
PetKids/
├── src/br/com/petkids/neogrid/
│   ├── action/botaoAcao/
│   │   └── GerarArquivoNeogrid.java
│   ├── service/
│   │   ├── NeogridService.java
│   │   └── impl/
│   │       ├── AbstractNeogridService.java
│   │       ├── VendedoresService.java
│   │       ├── ClientesService.java
│   │       ├── ProdutosService.java
│   │       ├── VendasService.java
│   │       └── EstoqueService.java
│   ├── repository/
│   │   ├── AbstractNeogridRepository.java
│   │   ├── VendedoresRepository.java
│   │   ├── ClientesRepository.java
│   │   ├── ProdutosRepository.java
│   │   ├── VendasRepository.java
│   │   └── EstoqueRepository.java
│   ├── model/
│   │   ├── dto/ (6 DTOs)
│   │   └── enums/ (5 Enums)
│   ├── util/ (5 utilitários)
│   ├── validation/
│   │   └── NeogridValidator.java
│   └── exception/ (5 exceções)
└── docs/
    └── layouts/ (5 PDFs de layouts)
```

**Padrões Aplicados**:
- Repository Pattern com AbstractRepository
- Service Layer com AbstractService
- DTO Pattern com equals/hashCode
- Exception Handling hierárquico
- Logging estruturado com ExceptionUtils
- Constants Class centralizada
- Validation centralizada

**Documentação**: [PetKids/README.md](PetKids/README.md) | [Documentação Técnica](PetKids/docs/DOCUMENTACAO_TECNICA.md)

### 🏨 NovoMundo
**Status**: ✅ Ativo  
**Versão**: 1.01  
**Cliente**: Hotel Mega Modas  
**Tecnologias**: Java 8+, EJB 3.0, Oracle PL/SQL, Sankhya Core 3.17+, HTML5/Flex, Jackson JSON

#### 📦 **Extensão de Importação de Arquivos e NFS-e**

Extensão completa para importação de arquivos e processamento de NFS-e (Notas Fiscais de Serviços Eletrônicas) do Hotel Mega Modas, suportando formatos nacional e regional.

**Componentes Principais**:

1. **Sistema de Importação de Arquivos** (EJBs):
   - **ImportacaoArquivoSP**: Session Bean para importação de arquivos
   - **ProcessarArquivosDiretorioJob**: Job para processamento automático de arquivos em diretório
   - **Tabelas**: `TIMPORTCONF` (configuração), `TIMPORTLOG` (log de importações)

2. **Tela Adicional AD_XMLNFSEHMMC**:
   - **Tabela Principal**: `AD_XMLNFSEHMMC` (XMLs importados)
     - Campos: SEQUENCIA (PK), DHREGISTRO, CODUSU, OBSERVACAO, XML (CLOB)
   - **Tabela Filha**: `AD_XMLNFSEHMM` (Notas extraídas)
     - Chave composta: NUMERO, NUMERORPS, SEQUENCIA
     - Campos: Dados da nota, prestador, tomador, STATUS, NUNOTA, CODPARC, CODEMP

3. **Procedures Principais** (`dbscripts/`):
   - **PROC_IMP_XMLNFSEHMM_MARTINS**: 
     - Importa XMLs de NFS-e
     - Suporta formato nacional e regional
     - Detecta automaticamente o formato
     - Valida XML antes do processamento
     - Permite reprocessamento forçado
   - **PROC_GERA_NFSE_PORTAIS_MARTINS**:
     - Gera notas fiscais no portal
     - Cria TGFCAB, TGFITE, TGFIMN, TGFFIN
     - Aplica regras de negócio do Sankhya
   - **PROC_IMP_XMLNFSEHMM_IM_MARTINS**:
     - Wrapper para importação automática
     - Integração com sistema de importação (TIMPORTCONF)
     - Processamento via jobs

**Arquitetura Detalhada**:
```
NovoMundo/
├── datadictionary/
│   └── metadata.xml                    # Metadados das tabelas
├── dbscripts/
│   ├── oracle.sql                      # DDL Oracle
│   ├── mssqlserver.sql                 # DDL SQL Server
│   ├── PROC_IMP_XMLNFSEHMM_MARTINS.sql # Importação XMLs
│   ├── PROC_IMP_XMLNFSEHMM_IM_MARTINS.sql # Wrapper automático
│   └── PROC_GERA_NFSE_PORTAIS_MARTINS.sql # Geração de notas
├── src/me/handz/importacao/
│   ├── model/services/
│   │   ├── ImportacaoArquivoSP.java
│   │   ├── ImportacaoArquivoSPBean.java
│   │   ├── ImportacaoArquivoSPHome.java
│   │   ├── ImportacaoArquivoSPSession.java
│   │   ├── ProcessarArquivosDiretorioJobBean.java
│   │   ├── ProcessarArquivosDiretorioJobLocal.java
│   │   ├── ProcessarArquivosDiretorioJobLocalHome.java
│   │   └── ProcessarArquivosDiretorioJobSession.java
│   └── META-INF/                       # Configurações EJB
├── web/
│   ├── html5/                          # Interface HTML5
│   └── flex/                           # Interface Flex
└── extension.xml                       # Descritor da extensão
```

**Formatos de XML Suportados**:

1. **Formato Nacional**:
   - Estrutura: `GerarNfseResponse` → `GerarNfseResposta` → `ListaNfse`
   - Detecção: Presença de `GERARNFSERESPONSE` ou `TOMADORSERVICO`
   - Campos: Estrutura padrão nacional

2. **Formato Regional (Goiânia)**:
   - Estrutura: `GerarNfseResposta` → `ListaNfse`
   - Detecção: Ausência dos marcadores do formato nacional
   - Campos: Estrutura específica de Goiânia

**Fluxos de Processamento**:

1. **Importação Manual**:
   ```
   Usuário → AD_XMLNFSEHMMC (Upload XML)
            → PROC_IMP_XMLNFSEHMM_MARTINS
            → AD_XMLNFSEHMM (Notas extraídas)
   ```

2. **Importação Automática**:
   ```
   Sistema de Importação → TIMPORTCONF (Config)
                         → Arquivo XML no diretório
                         → PROC_IMP_XMLNFSEHMM_IM_MARTINS
                         → PROC_IMP_XMLNFSEHMM_MARTINS
                         → AD_XMLNFSEHMM
   ```

3. **Geração de Notas**:
   ```
   AD_XMLNFSEHMM → PROC_GERA_NFSE_PORTAIS_MARTINS
                 → TGFCAB, TGFITE, TGFIMN, TGFFIN
                 → Notas geradas no sistema
   ```

**Configurações Necessárias**:
- **Parâmetro de Relatório 3**: Configurado em Centro > Telas Adicionais
- **TIMPORTCONF**: Para importação automática
  - IDTELA: Identificador único
  - STPFINAL: `PROC_IMP_XMLNFSEHMM_IM_MARTINS`
  - CAMPOARQUIVO: `XML`

**Tecnologias Utilizadas**:
- **Java 8+**: Linguagem principal
- **EJB 3.0**: Enterprise Java Beans para serviços
- **Oracle PL/SQL**: Procedures e funções
- **Sankhya Core 3.17+**: Framework Sankhya
- **HTML5/Flex**: Interfaces web
- **Jackson 2.13.3**: Processamento JSON
- **Apache Commons IO 2.11.0**: Utilitários de I/O

**Documentação**: [NovoMundo/docs/](NovoMundo/docs/) | [Resumo Estrutura](NovoMundo/docs/RESUMO_ESTRUTURA_PROJETO.md) | [Documentação Técnica](NovoMundo/docs/DOCUMENTACAO_TECNICA.md)

### 🏢 Monteccer
**Status**: ✅ Ativo  
**Versão**: 1.0.0  
**Cliente**: Monteccer  
**Tecnologias**: Java 8+, Sankhya Extensions, JasperReports, Reflection, ZIP, PDF

#### 📦 **Sistema de Integração Monteccer**

Sistema completo de integração entre Sankhya e Monteccer para geração de DANFEs (Documentos Auxiliares de Nota Fiscal Eletrônica) em PDF com suporte a processamento em lote.

**Classe Principal**: `br.com.monteccer.action.botaoAcao.IntegraMonteccer`

**Funcionalidades Detalhadas**:

1. **Geração de DANFEs** (`DanfeHelper.java`):
   - **Identificação de Tipo**: Detecta automaticamente se é NFe ou NFS-e
   - **Geração de PDF**: Usa classes específicas do Sankhya:
     - NFe: `br.com.sankhya.modelcore.comercial.nfe.ImpressaoNotaHelper`
     - NFS-e: `br.com.sankhya.modelcore.comercial.nfse.ImpressaoNotaNFSeHelper`
   - **Reflection**: Usa reflection para chamar métodos dinamicamente
   - **Geração de ZIP**: Agrupa múltiplos PDFs em um único ZIP
   - **Nomenclatura**: `DANFEs_{TIMESTAMP}.zip`

2. **Processamento por Parâmetros**:
   - **TIPO**: Tipo de movimento (ENTRADA, SAIDA, TODOS)
   - **DATA_INICIAL**: Data inicial do período
   - **DATA_FINAL**: Data final do período
   - **TIPO_MOVIMENTO**: Tipo específico de movimento

3. **Query Dinâmica**:
   - Construção de SQL baseada em parâmetros
   - Filtros por tipo, datas, status
   - Validação de notas válidas

**Arquitetura Detalhada**:
```
Monteccer/
├── src/br/com/monteccer/
│   ├── action/botaoAcao/
│   │   └── IntegraMonteccer.java        # Botão de ação principal
│   ├── helper/
│   │   └── DanfeHelper.java            # Helper para geração de DANFEs
│   └── util/
│       └── DownloadHelper.java         # Helper para download
├── src/main/resources/
│   └── META-INF/
│       ├── MANIFEST.MF                 # Manifest do JAR
│       └── mgemodule-cfg.xml           # Configuração do módulo
└── pom.xml                              # Configuração Maven
```

**Métodos Principais do DanfeHelper**:
- `gerarDanfeZip(List<BigDecimal> notas)`: Método principal de geração
- `gerarZipDanfe(List<BigDecimal> notas)`: Gera ZIP com PDFs
- `processarNotaParaZip(BigDecimal nunota, ZipOutputStream zipOut)`: Processa nota individual
- `gerarPdfDanfe(BigDecimal nunota)`: Gera PDF do DANFE
- `identificarTipoNota(BigDecimal nunota)`: Identifica tipo (NFe ou NFS-e)
- `gerarPdfDanfeNFSeAlternativo(BigDecimal nunota)`: Método alternativo para NFS-e
- `obterClasseImpressao(TipoNota tipoNota)`: Retorna classe de impressão correta

**Características Técnicas**:
- **Reflection**: Uso de reflection para chamar métodos dinamicamente
- **JasperReports**: Geração de PDFs usando JasperReports
- **ZIP**: Criação de arquivos ZIP com múltiplos PDFs
- **Download Automático**: Download via `DownloadHelper`
- **Tratamento de Erros**: Isolamento de erros por nota (continua processamento)

**Tecnologias Utilizadas**:
- **Java 8+**: Linguagem principal
- **Sankhya Extensions**: Framework Sankhya
- **JasperReports**: Geração de relatórios PDF
- **Reflection**: Chamada dinâmica de métodos
- **ZIP**: Criação de arquivos ZIP
- **JUnit 4.13.2**: Testes unitários
- **Mockito 3.12.4**: Mocking para testes
- **PowerMock 2.0.9**: Mocking de classes finais

**Configuração do Módulo** (`mgemodule-cfg.xml`):
- Nome: `integracao-monteccer`
- Versão: 1.0.0
- Tipo: java
- Classe principal: `br.com.monteccer.action.botaoAcao.IntegraMonteccer`

### 🔍 Emfal
**Status**: ✅ Ativo  
**Versão**: 1.0.0  
**Cliente**: Emfal  
**Tecnologias**: Java 8, SankhyaW Extensions, OAuth2, REST API, Serasa Experian API

#### 📦 **Integração Serasa Experian**

Sistema completo de integração com API Serasa Experian para consultas de score, pendências, alertas e monitoramento contínuo.

**Classe Principal**: `br.com.emfal.action.botaoAcao.ConsultaSerasa`

**Funcionalidades Detalhadas**:

1. **Tipos de Consulta Suportados**:
   - **Score PF/PJ**: Análise de crédito e score de pessoas físicas e jurídicas
   - **Pendências Financeiras**: Verificação de pendências financeiras
   - **Alertas de Documentos**: Alertas sobre documentos (CPF/CNPJ)
   - **Informações Cadastrais**: Dados cadastrais completos
   - **Renda Presumida**: Análise de renda e faturamento
   - **Ações Judiciais**: Acompanhamento de processos judiciais

2. **Componentes Principais**:
   - **SerasaAPIService**: Serviço principal de comunicação com API
   - **SerasaHelper**: Helper com métodos auxiliares
   - **SerasaResponseMapper**: Mapeamento de respostas da API
   - **SerasaConfig**: Configurações da API (URLs, credenciais)
   - **SerasaConstants**: Constantes centralizadas
   - **SerasaUtils**: Utilitários diversos

3. **Sistema de Autenticação**:
   - **OAuth2**: Autenticação via OAuth2
   - **Client ID/Secret**: Credenciais configuráveis
   - **Token Management**: Gerenciamento automático de tokens
   - **Refresh Token**: Renovação automática de tokens

4. **Monitoramento Contínuo**:
   - Sistema de monitoramento automático
   - Agendamento configurável
   - Alertas automáticos
   - Logging de todas as consultas

**Arquitetura Detalhada**:
```
Emfal/
├── src/br/com/emfal/
│   ├── action/botaoAcao/
│   │   └── ConsultaSerasa.java          # Botão de ação principal
│   └── serasa/
│       ├── service/
│       │   └── SerasaAPIService.java   # Serviço de API
│       ├── mapper/
│       │   └── SerasaResponseMapper.java # Mapeamento de respostas
│       ├── config/
│       │   └── SerasaConfig.java        # Configurações
│       ├── constants/
│       │   └── SerasaConstants.java     # Constantes
│       ├── util/
│       │   └── SerasaUtils.java         # Utilitários
│       └── SerasaHelper.java           # Helper principal
├── src/main/sql/
│   └── 01_criar_tabelas_serasa.sql     # Scripts SQL
└── docs/
    └── README.md                        # Documentação
```

**Tecnologias Utilizadas**:
- **Java 8**: Linguagem principal
- **SankhyaW Extensions**: Framework Sankhya
- **OAuth2**: Autenticação
- **REST API**: Comunicação com Serasa Experian
- **JSON**: Processamento de dados
- **HTTP Client**: Cliente HTTP para requisições

**Documentação**: [Emfal/docs/README.md](Emfal/docs/README.md)

### 💼 CVSBeneficios
**Status**: 🔄 Em Desenvolvimento  
**Versão**: Em desenvolvimento  
**Cliente**: CVS Benefícios  
**Tecnologias**: Java 8, SankhyaW Extensions, Tesseract OCR, Processamento de Imagens

#### 📦 **Personalização CVS Benefícios - OCR e Processamento de Imagens**

Sistema de processamento de documentos e imagens com OCR (Optical Character Recognition) para extração automática de dados de documentos.

**Status Atual**:
- ✅ **Fase**: Desenvolvimento inicial concluído
- 🔄 **Desenvolvimento**: Em andamento
- ⏳ **Testes**: Pendente
- ⏳ **Produção**: Pendente

**Componentes Principais**:

##### 1. **Processamento OCR**
**Localização**: `CVSBeneficios/src/br/`

**Classes Java**:
- **3 arquivos Java**: Classes de processamento OCR
- Integração com Tesseract OCR
- Processamento de imagens
- Extração de texto de documentos

**Funcionalidades**:
- Leitura de documentos escaneados
- Extração de texto usando OCR
- Processamento de múltiplos formatos de imagem
- Validação de dados extraídos

##### 2. **Tesseract OCR**
**Localização**: `CVSBeneficios/tessdata/`

**Arquivos de Treinamento**:
- **`eng.traineddata`**: Modelo de reconhecimento para inglês
- **`por.traineddata`**: Modelo de reconhecimento para português

**Funcionalidades**:
- Reconhecimento de caracteres em inglês
- Reconhecimento de caracteres em português
- Suporte a múltiplos idiomas
- Alta precisão de reconhecimento

##### 3. **Recursos de Imagens**
**Localização**: `CVSBeneficios/img/`

**Arquivos**:
- **2 arquivos JPG**: Imagens de exemplo/teste
- **1 arquivo JPEG**: Imagem de exemplo/teste
- **1 arquivo PNG**: Imagem de exemplo/teste

**Uso**:
- Imagens de teste para validação do OCR
- Exemplos de documentos processados
- Documentação visual

##### 4. **Resultados de OCR**
**Localização**: `CVSBeneficios/ocr_complete_results.txt`

**Conteúdo**:
- Resultados completos de processamento OCR
- Dados extraídos de documentos
- Métricas de precisão
- Logs de processamento

**Arquitetura**:
```
CVSBeneficios/
├── src/br/                        # Código fonte Java (3 arquivos)
├── tessdata/                      # Modelos OCR Tesseract
│   ├── eng.traineddata           # Inglês
│   └── por.traineddata           # Português
├── img/                           # Imagens de teste (4 arquivos)
├── ocr_complete_results.txt       # Resultados de OCR
├── docs/                          # Documentação
│   └── README.md
└── pom.xml                        # Configuração Maven
```

**Tecnologias Utilizadas**:
- **Java 8**: Linguagem principal
- **SankhyaW Extensions**: Framework Sankhya
- **Tesseract OCR**: Biblioteca de reconhecimento óptico de caracteres
- **Processamento de Imagens**: Manipulação e análise de imagens
- **Maven**: Gerenciamento de dependências

**Casos de Uso**:
- Extração de dados de documentos escaneados
- Processamento de formulários
- Digitalização de documentos antigos
- Automação de entrada de dados
- Validação de documentos

**Próximos Passos**:
- ✅ Estrutura básica criada
- 🔄 Integração com Sankhya em desenvolvimento
- ⏳ Testes de precisão OCR
- ⏳ Validação de dados extraídos
- ⏳ Interface de usuário para upload de documentos

**Documentação**: [CVSBeneficios/docs/README.md](CVSBeneficios/docs/README.md)

### 🏭 Brassol
**Status**: ✅ Ativo  
**Cliente**: Brassol  
**Tecnologias**: Java 8, Sankhya Extensions, Triggers

#### 📦 **Personalização Brassol**

Personalizações específicas para o sistema Brassol no Sankhya, incluindo alteração em lote de CFOP em itens de nota.

**Classe Principal**: `br.com.brassol.action.botaoAcao.AlteraCFOP`

**Funcionalidades Detalhadas**:

1. **Alteração de CFOP em Lote** (`AlteraCFOP.java`):
   - **Funcionalidade**: Botão de ação para alterar CFOP de múltiplos itens de nota em lote
   - **Processamento**: 
     - Validação de linhas selecionadas
     - Validação de parâmetro CODCFO obrigatório
     - Desabilitação temporária de trigger `TRG_UPT_TGFITE` para performance
     - Alteração em lote de CFOP em todos os itens selecionados
     - Reabilitação de trigger após processamento
     - Tratamento de erros por linha individual
     - Mensagem de retorno com quantidade de registros alterados
   - **Parâmetros**: 
     - `CODCFO` (obrigatório): Código do CFOP a ser aplicado
   - **Campos Processados**: 
     - `CODCFO`: Código Fiscal de Operações e Prestações

**Características Técnicas**:
- Desabilitação temporária de trigger para evitar processamento desnecessário
- Processamento em lote otimizado
- Tratamento de erros isolado por linha
- Logging detalhado de início e fim de processamento

**Arquitetura**:
```
Brassol/
├── src/br/com/brassol/action/botaoAcao/
│   └── AlteraCFOP.java                    # Botão de ação principal
├── docs/                                  # Documentação completa
│   ├── DOCUMENTACAO_FUNCIONAL_BRASSOL.md
│   ├── DOCUMENTACAO_FUNCIONAL_BRASSOL.html
│   ├── RELATORIO_ATENDIMENTO_TECNICO.md
│   └── RELATORIO_ATENDIMENTO_TECNICO.html
└── pom.xml                                # Configuração Maven
```

**Documentação Disponível**:
- Relatório de Atendimento Técnico (MD, HTML, PDF, DOCX)
- Documentação Funcional (MD, HTML, PDF, DOCX)

**Documentação**: [Brassol/docs/](Brassol/docs/)

### 🚛 SERPA
**Status**: 🔄 Em Desenvolvimento  
**Cliente**: SERPA  
**Tecnologias**: Java 8, Sankhya Extensions, File Processing

#### 📦 **Integração WMS TSL - Serpa**

Personalização para integração entre Sankhya e WMS TSL da Serpa, permitindo exportação e importação de arquivos TXT conforme padrão estabelecido no documento "Interfaces Padrões WMS - TSL V1.2".

**Classes Principais**:

1. **ExportarArquivoTXT.java** (`br.com.serpa.action.botaoAcao`):
   - **Funcionalidade**: Botão de ação para exportar dados do Sankhya para arquivo TXT
   - **Processamento**: 
     - Extração de dados conforme padrão WMS TSL V1.2
     - Formatação de campos conforme especificação
     - Geração de arquivo TXT formatado
     - Download automático do arquivo gerado

2. **ImportarArquivoTXT.java** (`br.com.serpa.action.botaoAcao`):
   - **Funcionalidade**: Botão de ação para importar dados de arquivo TXT para o Sankhya
   - **Processamento**: 
     - Leitura de arquivo TXT formatado
     - Validação de formato conforme padrão WMS TSL V1.2
     - Processamento de dados
     - Inserção/atualização no Sankhya

3. **SerpaTXTHelper.java** (`br.com.serpa.shared`):
   - **Funcionalidade**: Classe auxiliar compartilhada para processamento de arquivos TXT
   - **Características**:
     - Formatação de campos conforme padrão TSL
     - Validação de dados
     - Conversão de formatos
     - Tratamento de erros

**Funcionalidades Principais**:
- **Exportar TXT**: Exporta dados do Sankhya para arquivo TXT formatado conforme padrão WMS TSL
- **Importar TXT**: Importa dados de arquivo TXT formatado para o Sankhya
- **Validação**: Validação de formato e dados conforme especificação TSL V1.2
- **Processamento em Lote**: Suporte a processamento de múltiplos registros

**Arquitetura**:
```
SERPA/
├── src/br/com/serpa/
│   ├── action/botaoAcao/
│   │   ├── ExportarArquivoTXT.java    # Exportação de dados
│   │   └── ImportarArquivoTXT.java    # Importação de dados
│   └── shared/
│       └── SerpaTXTHelper.java        # Helper compartilhado
├── docs/
│   └── README.md                       # Documentação
└── pom.xml                             # Configuração Maven
```

**Status**: Estrutura básica criada, pendente ajustes conforme documento WMS TSL V1.2

**Documentação**: [SERPA/docs/README.md](SERPA/docs/README.md)

### 🔬 P&D
**Status**: ✅ Ativo  
**Versão**: 2.0.0  
**Cliente**: Pesquisa e Desenvolvimento  
**Tecnologias**: Java 8, CACHelper, PrePersistEntityState, TipoOperacaoUtils, CentralItemNota, EntityFacade

#### 📦 **Consolidar Itens de Nota**

Botão de ação avançado para consolidar múltiplos itens de uma nota em um único item de serviço quando a TOP está configurada com `AD_AGRUPATDITENS = 'S'`.

**Classe Principal**: `br.com.pd.action.botaoAcao.ConsolidarItensNota`

**Funcionalidades Detalhadas**:

1. **Processo de Consolidação**:
   - **Validação de TOP**: Verifica se TOP de destino está configurada corretamente
     - `AD_AGRUPATDITENS = 'S'`
     - `AD_SERVEMPREITADA` preenchido
   - **Busca de Dados**: Obtém todos os dados da nota de origem
   - **Cálculo de Totais**: Calcula totais de mão de obra e material
   - **Criação de Nota**: Cria nova nota usando APIs nativas (CACHelper)
   - **Item Consolidado**: Cria item único com observação da composição
   - **Vínculo**: Cria vínculo TGFVAR entre nota nova e origem
   - **Atualização**: Marca nota origem como não pendente

2. **Cálculo de Percentuais**:
   - **Mão de Obra**: Itens com `USOPROD = 'S'`
   - **Material**: Outros itens
   - **Percentuais**: Calculados com 2 casas decimais
   - **Observação**: Incluída no item consolidado com composição detalhada

3. **APIs Nativas Utilizadas**:
   - **CACHelper**: Criação de cabeçalho e itens com regras de negócio
   - **PrePersistEntityState**: Preparação de entidades para persistência
   - **TipoOperacaoUtils**: Obtenção de dados da TOP
   - **CentralItemNota**: Inicialização de produtos com preços e custos
   - **EntityFacade**: Operações CRUD nativas
   - **BarramentoRegra**: Aplicação de regras de negócio

**Arquitetura**:
```
P&D/
├── src/br/com/pd/action/botaoAcao/
│   └── ConsolidarItensNota.java        # Classe principal
├── docs/                                 # Documentação
├── STP_CONSOLIDAR_ITENS_NOTA.SQL        # Procedure original (referência)
├── STP_MARGEM_LUCRO_MINIMA.SQL          # Procedure adicional
├── STP_PRECO_DINAMICO.SQL               # Procedure adicional
└── TRG_INC_UPD_TGFITE_REGRA_PD.SQL      # Trigger adicional
```

**Parâmetros**:
- **CODTIPOPER_DEST** (obrigatório): Código da TOP de destino
- **SERIENOTA** (opcional): Série da nota a ser utilizada

**Campos das Linhas Selecionadas**:
- **NUNOTA**: Número da nota de origem (obrigatório)
- **CODEMP**: Código da empresa (opcional)
- **CODPARC**: Código do parceiro (opcional)

**Validações Implementadas**:
- TOP de destino deve existir
- TOP deve ter `AD_AGRUPATDITENS = 'S'`
- TOP deve ter `AD_SERVEMPREITADA` preenchido
- Nota de origem deve existir
- Produto de serviço deve existir

**Tratamento de Erros**:
- Erros são capturados por nota individual
- Processamento continua mesmo com erros em algumas notas
- Mensagem final mostra quantidade de processadas e erros
- Detalhes dos erros são registrados no log

**Benefícios**:
- ✅ **Regras de Negócio**: Todas as regras são aplicadas automaticamente
- ✅ **Validações**: Validações do sistema são respeitadas
- ✅ **Triggers**: Triggers e eventos são disparados corretamente
- ✅ **Cálculos**: Cálculos automáticos de preços, custos e impostos
- ✅ **Integração**: Integração com outros módulos (financeiro, estoque, etc.)
- ✅ **Manutenibilidade**: Código mais fácil de manter e compatível com atualizações

**Documentação**: [P&D/README.md](P&D/README.md)

### 📚 SankhyaJX
**Status**: ✅ Ativo  
**Descrição**:

#### 📦 **Biblioteca JavaScript para Sankhya**
Coleção de métodos estáticos para facilitar a manipulação de requisições HTTP, manipulação de dados de banco de dados, interação com páginas web e gerenciamento de parâmetros e cookies em aplicações web.

**Funcionalidades Principais**:
- **Banco de Dados**: `consultar()`, `salvar()`, `novoSalvar()`, `deletar()`
- **Manipulação de Página**: `acionarBotao()`, `removerFrame()`, `novaGuia()`, `abrirPagina()`, `fecharPagina()`
- **Retorno de Valores**: `getUrl()`, `getCookie()`, `getArquivo()`, `getParametro()`
- **Chamada de Serviço**: `chamarServico()`

**Instalação**:
```html
<script src="https://cdn.jsdelivr.net/gh/wansleynery/SankhyaJX@main/jx.js"></script>
```

**Documentação**: [SankhyaJX/README.md](SankhyaJX/README.md)

### 📊 Sermavil
**Status**: ✅ Ativo  
**Versão**: 1.0.0  
**Cliente**: Sermavil  
**Tecnologias**: Java 8, JasperReports, iReport, Oracle SQL

#### 📦 **Relatório de Comprovante de Checklist Operacional**

Sistema de geração de relatórios personalizados usando JasperReports/iReport para criação de comprovantes de checklist operacional.

**Classe Principal**: Template JasperReports (`Comprovante_Checklist_Operacional_ORACLE.jrxml`)

**Funcionalidades Detalhadas**:

1. **Geração de Comprovante**:
   - Template JasperReports completo
   - Integração com banco de dados Oracle
   - Formatação profissional com cores da empresa
   - Layout otimizado para impressão

2. **Características do Relatório**:
   - **Cabeçalho**: Informações da empresa e do checklist
   - **Detalhes**: Itens do checklist com status
   - **Rodapé**: Assinaturas e observações
   - **Formatação**: Cores oficiais da empresa Sermavil

3. **Dados Processados**:
   - Informações do checklist operacional
   - Status de cada item verificado
   - Datas e horários de execução
   - Responsáveis pela execução

**Arquitetura**:
```
Sermavil/
├── docs/
│   └── DOCUMENTACAO_COMPLETA_RELATORIO_CHECKLIST_SERMAVIL.md
└── reports/
    └── Comprovante_Checklist_Operacional_ORACLE.jrxml
```

**Tecnologias Utilizadas**:
- **JasperReports**: Geração de relatórios
- **iReport**: Designer de templates
- **Oracle SQL**: Queries para dados
- **Java 8**: Integração com Sankhya

**Características Técnicas**:
- Template otimizado para Oracle
- Suporte a múltiplos formatos de saída (PDF, Excel, HTML)
- Formatação condicional
- Cálculos e totais automáticos

**Documentação**: [Sermavil/docs/DOCUMENTACAO_COMPLETA_RELATORIO_CHECKLIST_SERMAVIL.md](Sermavil/docs/DOCUMENTACAO_COMPLETA_RELATORIO_CHECKLIST_SERMAVIL.md)

---

### 🔄 **Contribuições Cruzadas entre Projetos**

As personalizações da **Eletromac**, **Sankhya**, **Iwannasleep**, **Credpar** e **Megleo** contribuíram significativamente para melhorar o projeto **GuaranaMineiro**:

#### **Padrões Aplicados do Eletromac:**
- ✅ Uso correto de `NativeSql` com `setNamedParameter`
- ✅ Gerenciamento adequado de `JdbcWrapper` com `openSession/closeSession`
- ✅ Tratamento robusto de exceções
- ✅ Estrutura de logs organizados

#### **Padrões Aplicados do Sankhya:**
- ✅ Uso de `EntityFacadeFactory.getDWFFacade().getJdbcWrapper()`
- ✅ Padrão de `NativeSql.releaseResources()` em blocos finally
- ✅ Construção de SQL dinâmico com parâmetros nomeados
- ✅ Tratamento adequado de `ResultSet`

#### **Padrões Aplicados do Iwannasleep:**
- ✅ Sistema de eventos programáveis para automação
- ✅ Gerenciamento de reservas de estoque com controle de expiração
- ✅ Estrutura modular para processos de automação
- ✅ Integração com sistema de notificações

#### **Padrões Aplicados do Credpar:**
- ✅ Arquitetura de aplicativo móvel com View Controller
- ✅ Sistema de integração com APIs externas (operadoras)
- ✅ Modelo de dados robusto com entidades relacionais
- ✅ Sistema de jobs para processamento automático
- ✅ Gestão de limites e créditos com auditoria completa

#### **Padrões Aplicados do Megleo:**
- ✅ Sistema de integração com APIs de transportadoras via HTTP/REST
- ✅ Gestão de volumes e cargas com cálculo automático
- ✅ Ações agendadas para processamento em lote
- ✅ Sistema de logging centralizado com auditoria completa
- ✅ Validação de CEP e integração com serviços externos
- ✅ Tratamento robusto de JSON para comunicação com APIs

#### **Resultado no GuaranaMineiro:**
- 🚀 **Eliminação definitiva** do erro `"Parâmetro IN ou OUT ausente do índice:: 1"`
- 🚀 **Compatibilidade total** com padrões Sankhya estabelecidos
- 🚀 **Gerenciamento robusto** de recursos de banco de dados
- 🚀 **Alinhamento** com melhores práticas das outras personalizações

*Novas empresas serão adicionadas conforme necessário*

## 🤝 Como Contribuir

### 🚀 Processo Recomendado com Cursor IA

1. **Use Template/** como base:
   ```bash
   cp -r Template NovoProjeto
   cd NovoProjeto
   ```

2. **Configure no Cursor IA**:
   - O Cursor lerá automaticamente `.cursorrules`
   - Consulte `Template/CURSOR_IA_GUIA.md` para uso eficiente

3. **Desenvolva com IA**:
   - Use `Cmd/Ctrl + K` para gerar código seguindo templates
   - Use `Cmd/Ctrl + L` para consultar documentação
   - Use `Cmd/Ctrl + I` para refatorar código

4. **Siga Padrões**:
   - ZERO comentários
   - Menor número de linhas possível
   - JDK8 máximo (streams, Optional, lambdas)
   - Use componentes padrão do Template

### 📝 Padrões de Nomenclatura

1. **Diretórios de Empresa**: `NomeEmpresa` (sem espaços, PascalCase)
2. **Projetos**: `br.com.empresa.funcionalidade` (padrão Java)
3. **Commits**: Seguir [Conventional Commits](https://www.conventionalcommits.org/)
   - `feat:` - Nova funcionalidade
   - `fix:` - Correção de bug
   - `docs:` - Documentação
   - `chore:` - Tarefas de manutenção

### 🔧 Processo de Desenvolvimento

1. **Copiar Template** para novo projeto
2. **Configurar** pom.xml e pacotes
3. **Desenvolver** seguindo padrões do Template
4. **Documentar** todas as funcionalidades
5. **Testar** em ambiente de desenvolvimento
6. **Validar**: `mvn clean package install` (BUILD SUCCESS obrigatório)
7. **Criar Pull Request** com descrição detalhada

### 📋 Checklist para Novas Personalizações

- [ ] Baseado em Template/ (estrutura padronizada)
- [ ] Código sem comentários (100% autoexplicativo)
- [ ] Métodos < 50 linhas (preferencialmente < 30)
- [ ] Classes < 300 linhas (preferencialmente < 200)
- [ ] JDK8 máximo (streams, Optional, lambdas)
- [ ] Documentação técnica criada
- [ ] Logs de auditoria implementados
- [ ] Configurações externalizadas
- [ ] Tratamento de erros robusto
- [ ] `mvn clean package install` com BUILD SUCCESS

## 🛠️ Padrões de Desenvolvimento

### ⭐ Template Base - Use Sempre

**Para novas personalizações, SEMPRE use `Template/` como base**:
- Estrutura padronizada e otimizada
- Componentes padrão prontos:
  - **Constants**: Constantes centralizadas (charset, separadores, tamanhos)
  - **DownloadHelper**: Criação de ZIP e download automático
  - **FileGenerator**: Geração de arquivos com encoding correto
  - **Formatter**: Formatação de CNPJ, CPF, datas, números
  - **MessageHelper**: Mensagens formatadas para actions (erro, sucesso, info)
  - **Validator**: Validações comuns (CNPJ, CPF, diretórios, campos obrigatórios)
- **AbstractRepository**: Repositório base com métodos genéricos
- **Repository Pattern**: Interface e implementações prontas
- **Service Pattern**: Interface e implementações exemplo
- **DTO Pattern**: Exemplo com equals/hashCode
- Documentação completa para IA
- Templates prontos para uso

**Componentes Padrão Disponíveis**:

1. **Constants** (`br.com.cliente.util.Constants`):
   - `CHARSET_ANSI`: Windows-1252
   - `LINE_SEPARATOR`: CRLF (\r\n)
   - `FIELD_SEPARATOR`: PIPE (|)
   - `TAMANHO_CNPJ`: 14
   - `TAMANHO_CPF`: 11
   - `BUFFER_SIZE`: 8192

2. **MessageHelper** (`br.com.cliente.util.MessageHelper`):
   - `mostrarErro()`: Mensagem de erro formatada em vermelho
   - `mostrarSucesso()`: Mensagem de sucesso formatada em verde
   - `mostrarInfo()`: Mensagem informativa formatada em azul

3. **Validator** (`br.com.cliente.util.Validator`):
   - `validarCnpjCpf()`: Validação de CNPJ ou CPF
   - `validarCnpj()`: Validação específica de CNPJ
   - `validarCpf()`: Validação específica de CPF
   - `validarECriarDiretorio()`: Valida e cria diretório se necessário
   - `validarNaoVazio()`: Validação de campos obrigatórios

4. **DownloadHelper** (`br.com.cliente.util.DownloadHelper`):
   - Criação de arquivos ZIP
   - Download automático via contexto Sankhya
   - Suporte a múltiplos arquivos

5. **FileGenerator** (`br.com.cliente.util.FileGenerator`):
   - Geração de arquivos com encoding configurável
   - Buffer otimizado (8192 bytes)
   - Suporte a quebra de linha Windows/Unix

6. **Formatter** (`br.com.cliente.util.Formatter`):
   - Formatação de CNPJ/CPF
   - Formatação de datas
   - Formatação de números decimais
   - Remoção de acentos e caracteres especiais

**Consulte**: `Template/README.md` e `Template/INSTRUCOES_DESENVOLVIMENTO.md`

### ☕ Java/Sankhya

- **Java 8** - Compatibilidade obrigatória com versões do Sankhya
- **Maven** - Gerenciamento de dependências
- **Shaded JAR** - Para evitar conflitos de dependências
- **APIs Sankhya** - Uso direto das APIs oficiais do Sankhya
- **Logging** - Usar tabelas de log customizadas
- **ZERO comentários** - Código 100% autoexplicativo
- **JDK8 máximo** - Streams, Optional, lambdas, method references

### 🔧 **Padrões de Acesso a Banco de Dados (Lições Aprendidas)**

#### ✅ **Padrão Recomendado (Baseado em Eletromac/Sankhya):**
```java
// Usar NativeSql diretamente com EntityFacade
JdbcWrapper jdbc = null;
NativeSql nativeSql = null;
try {
    jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
    jdbc.openSession();
    nativeSql = new NativeSql(jdbc);
    nativeSql.appendSql("SELECT * FROM TABLE WHERE campo = :PARAM");
    nativeSql.setNamedParameter("PARAM", value);
    ResultSet rs = nativeSql.executeQuery();
    // ... processar resultados
} finally {
    NativeSql.releaseResources(nativeSql);
    JdbcWrapper.closeSession(jdbc);
}
```

#### ❌ **Padrão Problemático (Evitar):**
```java
// QueryExecutor com placeholders posicionais pode causar problemas
query.setParam("P1", value1);
query.setParam("P2", value2);
query.nativeSelect(sqlWithQuestionMarks); // Pode gerar erro "Parâmetro IN ou OUT ausente"
```

#### 🎯 **Benefícios do Padrão Recomendado:**
- ✅ **Compatibilidade total** com Oracle JDBC
- ✅ **Gerenciamento adequado** de recursos
- ✅ **Parâmetros nomeados** mais legíveis e seguros
- ✅ **Tratamento robusto** de exceções
- ✅ **Alinhamento** com padrões Sankhya estabelecidos

### 📊 Banco de Dados

- **Tabelas de Log**: Prefixo `AD_` para auditoria
- **Campos Padrão**: `ID_LOG`, `DATA_CRIACAO`, `CORRELATION_ID`
- **Índices**: Criar para campos de consulta frequente
- **DDL**: Documentar para Oracle e PostgreSQL

### 🔐 Segurança

- **Credenciais**: Nunca hardcoded, usar `application.properties`
- **Logs**: Não registrar dados sensíveis
- **Validação**: Validar todos os inputs externos
- **Auditoria**: Registrar todas as operações críticas

## 🚀 Deploy e Instalação

### 📦 Compilação

```bash
# Navegar para o projeto específico
cd GuaranaMineiro/br.com.performaxxi.btnacao/

# Compilar o projeto
mvn clean package

# O JAR shaded será gerado em:
# target/btnacao-1.0.0-shaded.jar
```

### 📤 Instalação no Sankhya

1. **Upload** do JAR shaded no módulo Java
2. **Configurar** botão de ação
3. **Criar tabelas** de log (usar DDL da documentação)
4. **Configurar** parâmetros em `application.properties`
5. **Testar** em ambiente de desenvolvimento

### ⚙️ Configuração

Cada personalização deve ter seu arquivo de configuração:

```properties
# application.properties
performaxxi.api.url=https://api.performaxxi.com.br
performaxxi.api.username=usuario
performaxxi.api.password=senha
performaxxi.api.timeout=30000
```

## 📚 Documentação

### 🎯 Documentação Principal para IA

**Para desenvolvimento com Cursor IA, consulte nesta ordem**:

1. **Template/** ⭐ **BASE DE CONHECIMENTO**
   - `Template/INSTRUCOES_DESENVOLVIMENTO.md` - Instruções completas e templates
   - `Template/CURSOR_IA_GUIA.md` - Guia específico Cursor IA
   - `Template/INDICE_CONHECIMENTO_SANKHYA.md` - Índice rápido de conhecimento
   - `Template/REFERENCIA_SANKHYA.md` - Referência completa de tabelas e métodos
   - `Template/README.md` - Visão geral do template

2. **ZDevDoc/** 📚 **DOCUMENTAÇÃO COMPLETA SANKHYA**
   - `ZDevDoc/INDICE_PRINCIPAL.md` - Índice principal
   - `ZDevDoc/02-desenvolvimento/` - Guias de desenvolvimento
   - `ZDevDoc/04-avancado/` - Técnicas avançadas

3. **SatyaPass/** 💡 **EXEMPLOS PRÁTICOS**
   - `SatyaPass/exemplos/` - 248 exemplos de código funcionais
   - `SatyaPass/INDICE_EXEMPLOS.md` - Índice de exemplos

4. **Projetos Reais** 🏢 **REFERÊNCIAS DE IMPLEMENTAÇÃO**
   - `Denver/` - Arquitetura otimizada
   - `PetKids/` - Padrões de integração
   - `GuaranaMineiro/` - Integração REST
   - `Megleo/` - Integração transportadoras

### 📖 Estrutura de Documentação por Projeto

Cada projeto deve conter:

```
[NomeProjeto]/
├── docs/
│   ├── integracao-[nome].md           # Visão geral da integração
│   ├── processo-logs-[nome].md       # Documentação do sistema de logs
│   ├── implantacao-[nome].md         # Guia de instalação
│   ├── Metadados_[TABELA].zip        # Metadados exportados do Sankhya (DDL)
│   └── verificacao-plano-trabalho.md # Relatório de implementação
├── README.md                          # Documentação do projeto
└── src/                               # Código fonte
```

> **📋 Nota sobre DDL**: Utilize os arquivos `Metadados_*.zip` exportados diretamente do Sankhya para criar as tabelas. Estes arquivos contêm a estrutura oficial e são mais confiáveis que scripts manuais.

### 🔍 Tipos de Documentação

- **Técnica**: Arquitetura, APIs, banco de dados
- **Funcional**: Fluxos de negócio, casos de uso
- **Operacional**: Instalação, configuração, troubleshooting
- **Auditoria**: Logs, monitoramento, métricas
- **IA**: Instruções específicas para Cursor IA (Template/)

## 🆘 Suporte

### 📞 Contatos

- **Desenvolvimento**: leandromarcosmoreira@gmail.com
- **Suporte Técnico**: WhatsApp 34 99120-4642
- **Documentação**: Disponível dentro de cada projeto na pasta `docs/`

### 🐛 Reportar Problemas

1. Verificar se o problema já foi reportado
2. Criar issue com template preenchido
3. Incluir logs e informações do ambiente
4. Anexar screenshots se necessário

## 📄 Licença

Este projeto é proprietário e confidencial. Uso restrito às empresas contratantes.

---

---

## 🤖 Uso com Cursor IA

### Comandos Úteis

| Ação | Comando | Descrição |
|------|---------|-----------|
| Gerar código | `Cmd/Ctrl + K` | Gera código seguindo padrões do Template |
| Consultar | `Cmd/Ctrl + L` | Consulta documentação em linguagem natural |
| Editar inline | `Cmd/Ctrl + I` | Refatora código seguindo padrões |
| Autocompletar | `Tab` | Completa seguindo padrões do projeto |

### Exemplos de Consultas Eficientes

**Consultas sobre Padrões**:
```
"Como criar um repositório seguindo o padrão AbstractRepository?"
"Qual o template completo para criar um botão de ação?"
"Como implementar uma ação agendada (ScheduledAction)?"
"Como criar um evento programado (EventoProgramavelJava)?"
```

**Consultas sobre Sankhya**:
```
"Como usar EntityFacade para buscar dados no Sankhya?"
"Qual o padrão correto para usar NativeSql com parâmetros nomeados?"
"Como fazer download usando DownloadHelper?"
"Como formatar CNPJ usando Formatter?"
```

**Consultas sobre Integrações**:
```
"Como fazer integração REST com autenticação Basic Auth?"
"Qual o padrão para processamento em lote?"
"Como implementar sistema de logs estruturado?"
```

**Consultas sobre Otimização**:
```
"Como refatorar este código para usar streams JDK8?"
"Como reduzir o número de linhas deste método?"
"Como otimizar esta query SQL?"
```

### Documentação para IA - Ordem de Consulta Recomendada

1. **Template/** ⭐ **BASE DE CONHECIMENTO PRINCIPAL**
   - `Template/INSTRUCOES_DESENVOLVIMENTO.md` - Instruções completas e templates
   - `Template/CURSOR_IA_GUIA.md` - Guia específico Cursor IA
   - `Template/INDICE_CONHECIMENTO_SANKHYA.md` - Índice rápido de conhecimento
   - `Template/REFERENCIA_SANKHYA.md` - Referência completa de tabelas e métodos
   - `Template/README.md` - Visão geral e índice rápido

2. **ZDevDoc/** 📚 **DOCUMENTAÇÃO COMPLETA SANKHYA**
   - `ZDevDoc/INDICE_PRINCIPAL.md` - Índice principal
   - `ZDevDoc/02-desenvolvimento/` - Guias de desenvolvimento
   - `ZDevDoc/04-avancado/` - Técnicas avançadas

3. **SatyaPass/** 💡 **EXEMPLOS PRÁTICOS**
   - `SatyaPass/exemplos/` - 248 exemplos de código funcionais
   - `SatyaPass/INDICE_EXEMPLOS.md` - Índice de exemplos

4. **Projetos Reais** 🏢 **REFERÊNCIAS DE IMPLEMENTAÇÃO**
   - `Denver/` - Arquitetura otimizada TSL
   - `PetKids/` - Padrões de integração Neogrid
   - `GuaranaMineiro/` - Integração REST Performaxxi
   - `Megleo/` - Integração transportadoras
   - `Eletromac/` - Automação de processos

---

## 🎓 Conhecimento Consolidado do Repositório

### 📊 Estatísticas do Repositório

**Total de Projetos**: 17+ projetos ativos
- ✅ **13 projetos em produção**:
  - GuaranaMineiro (Integração Performaxxi)
  - Eletromac (Automação de processos)
  - Sankhya (Personalizações genéricas - 9 subprojetos)
  - Iwannasleep (Gerenciamento de reservas)
  - Credpar (Sistema de recarga - 3 módulos)
  - Megleo (Integração transportadoras)
  - Denver (Integração TSL)
  - PetKids (Integração Neogrid)
  - NovoMundo (Importação NFS-e)
  - Monteccer (Geração DANFEs)
  - Emfal (Integração Serasa)
  - Brassol (Personalizações específicas)
  - P&D (Consolidação de itens)
- 🔄 **2 projetos em desenvolvimento**:
  - CVSBeneficios (OCR e processamento de imagens)
  - SERPA (Integração WMS TSL)
- 📚 **3 projetos de conhecimento**:
  - Template (Base de conhecimento e templates)
  - SatyaPass (248 exemplos práticos)
  - ZDevDoc (Documentação completa Sankhya)
- 🔧 **1 biblioteca JavaScript**:
  - SankhyaJX (Biblioteca JavaScript para Sankhya)
- 📊 **1 projeto adicional**:
  - Sermavil (Relatórios JasperReports)

**Total de Implementações** (baseado em análise completa do código):
- ✅ **65+ implementações** de `AcaoRotinaJava` (Botões de Ação)
  - GuaranaMineiro: 1 (IntegraPerformaxxi)
  - Eletromac: 1 (AutomatizacaoProcessos)
  - Iwannasleep: 1 (AutomatizacaoProcessos)
  - Megleo: 2 (InserirPedidoMegleo, BuscaTransportadoraPedido)
  - Denver: 2 (GerarArquivoTSL, ImportarItensTXT)
  - PetKids: 1 (GerarArquivoNeogrid)
  - Monteccer: 1 (IntegraMonteccer)
  - Emfal: 1 (ConsultaSerasa)
  - SERPA: 2 (ExportarArquivoTXT, ImportarArquivoTXT)
  - P&D: 1 (ConsolidarItensNota)
  - Sankhya/BotoesAcaoSankhya: 8+ (IncluirOS, SalvarApontamento, EncaminharFila, TirarFila, OrdenacaoCursosUniversidade, etc.)
  - Sankhya/AcoesMaha: 2+ (ImportarVendaBase, RequestAPI)
  - Sankhya/processos: 10+ (RotinaTeste, RemoverInstanciaProcesso, PopularUCP, MigrarInstancias, EnviaSinal, etc.)
  - Sankhya/MigracaoProcesso: 3 (MigrarInstancias, ImportarAtividadesProcesso, ImportarAtividadesInstancia)
  - Template: 1 (PersonalizacaoSankhya)
- ✅ **7+ implementações** de `ScheduledAction` (Ações Agendadas)
  - GuaranaMineiro: 1 (ComprovantesEntrega)
  - Eletromac: 1 (AutomatizacaoProcessos)
  - Iwannasleep: 1 (AutomatizacaoProcessos)
  - Megleo: 1 (EnviaNotasConfirmadas)
  - PetKids: 1 (GerarArquivoNeogrid)
- ✅ **21+ implementações** de `EventoProgramavelJava` (Eventos Programados)
  - GuaranaMineiro: 1 (RecebimentoEvento)
  - Iwannasleep: 2 (ReservaEstoqueExpiradaEventoProgramavel, ReservaEstoqueMatrizEventoProgramavel)
  - Megleo: 2 (alteracaoProduto, alteraParceiroTransportador)
  - Sankhya/processos: 10+ (FormularioSolicitacaoManutencao, EventoOSManutencaoSk, EventoItemOSManutencaoSk, BloqueioTesteEntradaAberto, BloqueioEnvioSoftwareOSManutencaoSk, BloqueioAlteracaoOSManutencaoSk, BloqueioExclusaoApontamentoManutencaoSk, BloqueioEnvioServiceDeskManutencaoSK, CopiaAnexosFinanceiro)
- ✅ **12+ implementações** de `Repository` (Repositórios)
  - Denver: 3 (AbstractTSLRepository, RecebimentoRepository, ExpedicaoRepository)
  - PetKids: 6 (AbstractNeogridRepository, VendedoresRepository, ClientesRepository, ProdutosRepository, VendasRepository, EstoqueRepository)
  - Template: 1 (AbstractRepository)
- ✅ **30+ classes Helper** (Classes Auxiliares)
  - GuaranaMineiro: 2 (PerformaxxiIntegracaoHelper, PerformaxxiAPI)
  - Eletromac: 1 (AutomatizacaoProcessosHelper)
  - Iwannasleep: 1 (AutomatizacaoProcessosHelper)
  - Megleo: 3 (EnviaPedido, RegistraLOG, AlteracaoSKU)
  - Denver: 3 (TSLFormatter, FileGenerator, DownloadHelper)
  - PetKids: 5 (NeogridFormatter, FileGenerator, NeogridLogFactory, NeogridConstants, DownloadHelper)
  - Monteccer: 2 (DanfeHelper, DownloadHelper)
  - Emfal: 6 (SerasaAPIService, SerasaHelper, SerasaResponseMapper, SerasaConfig, SerasaConstants, SerasaUtils)
  - SERPA: 1 (SerpaTXTHelper)
  - Template: 3 (DownloadHelper, FileGenerator, Formatter)
- ✅ **5+ classes Constants** (Constantes Centralizadas)
  - Denver: 1 (TSLConstants)
  - PetKids: 1 (NeogridConstants)
  - Emfal: 1 (SerasaConstants)
  - Template: 1 (Constants) - Base para todos os projetos
- ✅ **6+ classes de Validação** (Validações Centralizadas)
  - PetKids: 1 (NeogridValidator)
  - Template: 1 (Validator) - Validações comuns (CNPJ, CPF, diretórios)
- ✅ **3+ classes MessageHelper** (Mensagens Formatadas)
  - Template: 1 (MessageHelper) - Mensagens formatadas para actions

**Tipos de Integrações**:
- 🔄 **Integrações REST**: GuaranaMineiro (Performaxxi), Megleo (Transportadoras), Emfal (Serasa Experian)
- 📄 **Integrações com Arquivos**: Denver (TSL), PetKids (Neogrid), SERPA (WMS TSL)
- 📋 **Processamento de XML**: NovoMundo (NFS-e)
- 📱 **Aplicativos Móveis**: Credpar (CredParApp)
- 🔧 **Automações**: Eletromac, Iwannasleep
- 📊 **Relatórios**: Eletromac (Comissões, Orçamentos, Pedidos)
- 🔬 **P&D**: P&D (Consolidação de Itens, Preço Dinâmico)

### Padrões Arquiteturais Identificados

**Estrutura MVC Adaptada para Sankhya**:
- **Action Layer**: Botões de ação (`AcaoRotinaJava`) e ações agendadas (`ScheduledAction`)
- **Service Layer**: Lógica de negócio isolada, orquestração de repositórios
- **Repository Layer**: Acesso a dados com `AbstractRepository` como base padrão
- **DTO Layer**: Transferência de dados com equals/hashCode obrigatórios
- **Util Layer**: Componentes padrão (DownloadHelper, Formatter, FileGenerator)

### Padrões de Integração Identificados

1. **Integração REST** (GuaranaMineiro/Performaxxi)
   - Autenticação Basic Auth (Base64)
   - Timeout configurável (conexão: 30s, leitura: 60s)
   - Gson para serialização JSON
   - Tratamento robusto de erros HTTP
   - Logging detalhado em tabelas AD_

2. **Integração com Arquivos** (PetKids/Neogrid, Denver/TSL)
   - Encoding Windows-1252 (ANSI)
   - Quebra de linha CRLF (\r\n)
   - Layout posicional fixo ou com separadores (PIPE)
   - Geração padronizada com FileGenerator

3. **Ações Agendadas** (Eletromac, Iwannasleep, Megleo)
   - Implementação de `ScheduledAction`
   - Processamento em lote otimizado
   - Sistema de logs estruturado
   - Notificações por email em caso de erro

4. **Eventos Programados** (GuaranaMineiro, Iwannasleep)
   - Implementação de `EventoProgramavelJava`
   - Triggers baseados em campos específicos
   - Validações antes/depois de operações
   - Auditoria completa de alterações

### Padrões de Performance Consolidados

- **Buffers I/O**: Sempre 8192 bytes (padrão otimizado)
- **Pré-alocação**: LinkedHashSet<>(1024), ArrayList<>(100)
- **StringBuilder**: Pré-dimensionado (new StringBuilder(200))
- **Cache**: ConcurrentHashMap para objetos imutáveis
- **Pattern pré-compilado**: Para regex (evita recompilação)
- **ThreadLocal**: Para SimpleDateFormat (evita sincronização)

### Padrões de Queries SQL Consolidados

- **Filtros Obrigatórios**: STATUSNOTA = 'L' (notas), ATIVO = 'S' (produtos/parceiros/empresas)
- **JOIN com Tipo de Operação**: Sempre usar CODTIPOPER + DHTIPOPER
- **Evitar N+1 queries**: Usar JOINs adequados ao invés de loops
- **Parâmetros nomeados**: Usar `:PARAM` ao invés de `?` quando possível
- **Tratamento de nulos**: NVL para valores padrão

### Padrões de Logging Consolidados

- **Logging estruturado**: Prefixo padronizado, formato consistente
- **ExceptionUtils**: Para obter mensagens e stack traces completos
- **Tabelas de log**: Prefixo `AD_` para auditoria
- **Campos padrão**: ID_LOG, DATA_CRIACAO, CORRELATION_ID
- **Níveis de log**: INFO, ERRO, PERFORMANCE

---

**Última atualização**: 02/01/2025  
**Versão**: 8.1.0 - Documentação Aprimorada com Detalhes de Componentes Padrão  
**Mantenedor**: Leandro Marcos Moreira

**Melhorias nesta versão**:
- ✅ Detalhamento completo dos componentes padrão do Template (Constants, MessageHelper, Validator, DownloadHelper, FileGenerator, Formatter)
- ✅ Informações detalhadas sobre Brassol/AlteraCFOP
- ✅ Melhorias na seção SERPA com detalhes técnicos
- ✅ Estrutura completa do Template documentada
- ✅ Referências precisas aos componentes disponíveis

### 📈 **Histórico de Versões**

- **v8.1.0** (02/01/2025): Documentação aprimorada com detalhes completos dos componentes padrão do Template (Constants, MessageHelper, Validator, DownloadHelper, FileGenerator, Formatter), informações detalhadas sobre Brassol/AlteraCFOP, melhorias na seção SERPA, estrutura completa do Template documentada
- **v8.0.0** (02/01/2025): Documentação máxima detalhada de todos os projetos (17+ projetos), estatísticas precisas baseadas em análise completa do código (65+ AcaoRotinaJava, 7+ ScheduledAction, 21+ EventoProgramavelJava), detalhamento completo de projetos Sankhya genéricos (9 subprojetos), Credpar (3 módulos), CVSBeneficios (OCR), Sermavil (JasperReports)
- **v7.0.0** (02/01/2025): Consolidação completa de todos os projetos do repositório (15+ projetos), adicionados Denver, PetKids, NovoMundo, Monteccer, Emfal, CVSBeneficios, Brassol, SERPA, P&D, SankhyaJX
- **v6.0.0** (02/01/2025): Revisão completa com conhecimento consolidado de todos os projetos, aprimoramento do Template como base de conhecimento máximo
- **v5.0.0** (02/01/2025): Otimização completa para Cursor IA, Template base consolidado, documentação estruturada
- **v4.0.0** (10/01/2025): Adicionado projeto Megleo com sistema de integração de transportadoras
- **v3.0.0** (10/01/2025): Adicionados projetos Iwannasleep e Credpar
- **v2.0.0** (10/01/2025): Adicionados projetos Eletromac e Sankhya
- **v1.0.0** (20/12/2024): Versão inicial com projeto GuaranaMineiro
# DadosEstudoIA
