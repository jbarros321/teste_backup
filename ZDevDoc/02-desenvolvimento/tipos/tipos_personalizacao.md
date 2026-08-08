# 🛠️ Tipos de Personalização/Customização Sankhya

## 📋 Visão Geral

A plataforma Sankhya oferece **14 tipos principais** de personalização/customização, organizados em diferentes camadas da arquitetura. Cada tipo atende a necessidades específicas de desenvolvimento e integração.

## 🏗️ Arquitetura em Camadas

A imagem abaixo mostra onde cada tipo de personalização é inserido nas diferentes camadas da plataforma Sankhya:

```
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA DE APRESENTAÇÃO                   │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │ Telas Adicionais│ │   Dashboards    │ │ Relatórios      │ │
│  │                 │ │                 │ │ Personalizados  │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA DE LÓGICA                        │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │ Botões de Ação  │ │ Ações Agendadas │ │ Eventos         │ │
│  │                 │ │                 │ │ Programados     │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA DE DADOS                         │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │ Gerenciador de  │ │ Consolidador de │ │ Regras de       │ │
│  │ Objetos         │ │ Dados           │ │ Negócio         │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA DE INTEGRAÇÃO                    │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │ API de          │ │ EDI             │ │ Tarefas de      │ │
│  │ Integração      │ │                 │ │ Serviços        │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA DE EXTENSÃO                      │
│  ┌─────────────────┐ ┌─────────────────┐                   │
│  │ Add-ons         │ │ Metas           │                   │
│  │                 │ │ Gerenciais      │                   │
│  └─────────────────┘ └─────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
```

## 📱 1. Telas Adicionais

### **Descrição**
O construtor de telas adicionais permite a criação de novas telas do tipo formulário, que podem apresentar quantidades variáveis de campos e níveis diferentes de complexidade.

### **Uso Indicado**
- Navegação, coleta, análise, inclusão e deleção de registros
- Criação de relatórios
- Cubos de decisão
- Dashboards
- Regras de validação
- Recursos personalizados

### **Recursos de Formação**
- **Plataforma de Personalização (Construtor de Telas)** - Curso - Universidade Sankhya

### **Documentação de Referência**
- Construtor de Telas - Central de Ajuda
- Dicionário de Dados - Central de Ajuda
- Como Identificar Telas Personalizadas - Central de Ajuda

## 🔘 2. Botão de Ação

### **Descrição**
Botões de ação permitem a execução de rotinas de processamento mediante o acionamento manual de um botão. Análogo à inclusão de um item.

### **Tipos de Rotinas**
- **Lançadores**: Lista de tarefas a serem realizadas
- **Rotinas de Banco de Dados**: Procedures
- **Rotinas Java**: Criação de classes para execuções de ações

### **Recursos de Formação**
- **Sankhya Dev Talks - Desenvolvendo Com Módulo Java** - Webinário - YouTube
- **Q & A Sankhya Dev Talks - Módulo Java** - Post na Comunidade

### **Documentação de Referência**
- Botão de ação - Portal Sankhya Developer
- Módulo Java - Central de Ajuda
- Dicionário de Dados - Central de Ajuda
- Construtor de Telas - Central de Ajuda

## ⏰ 3. Ação Agendada

### **Descrição**
Ações agendadas permitem a execução de forma automática e periódica de rotinas de processamento, em horários, datas ou intervalos fixos, com efeito análogo a um job.

### **Características**
- Execução automática
- Períodos configuráveis
- Horários fixos
- Intervalos regulares
- Efeito similar a jobs

### **Recursos de Formação**
- **Sankhya Dev Talks - Desenvolvendo Com Módulo Java** - Webinário - YouTube
- **Q & A Sankhya Dev Talks - Módulo Java** - Post na Comunidade

### **Documentação de Referência**
- Ações Agendadas - Central de Ajuda
- Módulo Java - Central de Ajuda
- Dicionário de Dados - Central de Ajuda
- Construtor de Telas - Central de Ajuda

## 🎯 4. Eventos Programados

### **Descrição**
Eventos programados permitem a execução de rotinas de processamento, que são disparados por operações realizadas em entidades do sistema, de forma análoga a um listener de entidade.

### **Características**
- Disparados por operações em entidades
- Listener de entidade
- Processamento automático
- Integração com fluxo de dados

### **Recursos de Formação**
- **Sankhya Dev Talks - Desenvolvendo Com Módulo Java** - Webinário - YouTube
- **Q & A Sankhya Dev Talks - Módulo Java** - Post na Comunidade

### **Documentação de Referência**
- Módulo Java - Central de Ajuda
- Dicionário de Dados - Central de Ajuda
- Construtor de Telas - Central de Ajuda

## 🔗 5. API de Integração

### **Descrição**
A API de integração fornece meios de integração da plataforma Sankhya com outros sistemas, para troca de dados. Ela também pode ser usada para automatizar buscas e cadastros em vários módulos da plataforma.

### **Funcionalidades**
- Integração com outros sistemas
- Troca de dados
- Automação de buscas
- Cadastros automatizados
- Múltiplos módulos

### **Documentação de Referência**
- API de Serviços (Integração) - Portal Sankhya Developer
- Mapeando Serviços - Portal Sankhya Developer

## 🔌 6. Add-ons

### **Descrição**
Add-ons permitem, através de um único instalador, a inserção de vários recursos de diferenciação, facilitando a inclusão de novas funcionalidades e possibilitando a criação de novas APIs na plataforma Sankhya.

### **Características**
- Instalador único
- Múltiplos recursos
- Novas funcionalidades
- Criação de APIs
- Facilidade de instalação

### **Recursos de Formação**
- **Certified Associate Sankhya Developer - Java Web** - Trilha de Certificação - Universidade Sankhya
- **Certified Associate Sankhya Developer - Banco de Dados** - Trilha de Certificação - Universidade Sankhya

### **Documentação de Referência**
- Add-on studio

## 🗄️ 7. Gerenciador de Objetos

### **Descrição**
O gerenciador de objetos permite a execução, através de uma ferramenta desenvolvida para o banco de dados, de melhorias nos processos de criação, gerenciamento e manipulação dos elementos das telas.

### **Funcionalidades**
- Criação de regras de negócio em tabelas
- Consulta e alteração de objetos
- Recursos de telemetria
- Avaliação de performance
- Gerenciamento de elementos

### **Recursos de Formação**
- **Certified Associate Sankhya Developer - Banco de Dados** - Trilha de Certificação - Universidade Sankhya

### **Documentação de Referência**
- Gerenciador de Objetos - Central de Ajuda

## 📊 8. Construtor de Dashboards

### **Descrição**
O construtor de dashboards permite a criação de painéis com visualizações dinâmicas de métricas e indicadores, facilitando a compreensão das informações gerenciadas pela plataforma Sankhya.

### **Características**
- Visualizações dinâmicas
- Métricas e indicadores
- Facilita compreensão
- Informações centralizadas
- Interface intuitiva

### **Recursos de Formação**
- **Construtor de Dashboard** - Curso - Universidade Sankhya
- **Dashboards: Configurações Básicas** - Curso - Universidade Sankhya
- **Construindo um Dashboard** - Curso - Universidade Sankhya
- **Dashboard: Configurações Avançadas e dicas** - Curso - Universidade Sankhya
- **O que são 'Parâmetros' no Construtor de Componentes de BI** - Curso - Universidade Sankhya

### **Documentação de Referência**
- Construtor de Dashboards - Central de Ajuda
- Construtor de Componentes de BI - Central de Ajuda

## 📄 9. Relatórios Personalizados

### **Descrição**
O formatador de relatórios permite a criação de relatórios personalizados, que contém informações detalhadas e centralizadas sobre a empresa, de maneira textual ou gráfica.

### **Funcionalidades**
- Informações detalhadas
- Centralizadas
- Textual ou gráfica
- Agrupamento
- Comparação
- Operações numéricas
- Cruzamento de dados

### **Recursos de Formação**
- **Formatador de Relatório iReport** - Curso - Universidade Sankhya

### **Documentação de Referência**
- Formatador de Relatórios - Central de Ajuda
- iReport Plugin - Central de Ajuda
- iReport Download - Central de Downloads Sankhya

## 📡 10. EDI - Intercâmbio Eletrônico de Dados

### **Descrição**
Os EDIs permitem o intercâmbio eletrônico de dados, que consiste em uma integração de informações entre organizações, através da troca de arquivos, visando minimizar erros e agilizar o processamento de informações.

### **Aplicações**
- Pagamento eletrônico
- Cobrança eletrônica
- Cobrança registrada
- Cobrança sem registros
- Processos similares

### **Benefícios**
- Minimiza erros
- Agiliza processamento
- Integração entre organizações
- Troca de arquivos
- Automação de processos

### **Documentação de Referência**
- EDI Bancário - Central de Ajuda

## 📈 11. Consolidador de Dados

### **Descrição**
O consolidador de dados permite a criação de tabelas para consolidação de informações, usadas como base de criação dos dashboards, para melhorar a visualização dos dados requisitados nas operações da plataforma.

### **Funcionalidades**
- Criação de tabelas
- Consolidação de informações
- Base para dashboards
- Melhora visualização
- Otimiza operações

### **Documentação de Referência**
- Consolidador de Dados - Central de Ajuda

## 🔄 12. Tarefas de Serviços

### **Descrição**
Tarefas de serviços permitem que usuários da plataforma representem, gerenciem e automatizem fluxos de trabalho de departamentos ou de áreas de um negócio, além de integrar processos junto a outros softwares.

### **Funcionalidades**
- Representar fluxos de trabalho
- Gerenciar processos
- Automatizar operações
- Integrar com outros softwares
- Departamentos e áreas

### **Recursos de Formação**
- **Tarefas de Serviço do Sankhya Flow e como configurá-las** - Curso - Universidade Sankhya

### **Documentação de Referência**
- Tarefas de Serviço com Web Service Interno - Central de Ajuda
- Tarefas de Serviço com Web Service Externo - Central de Ajuda

## ⚖️ 13. Regras de Negócio

### **Descrição**
Regras de negócio permitem a inserção de requisitos específicos aplicados aos portais de notas. As regras são usadas para liberação de operações feitas no portal, sejam elas para confirmação, alteração ou inclusão.

### **Aplicações**
- Portais de notas
- Liberação de operações
- Confirmação
- Alteração
- Inclusão
- Requisitos específicos

### **Documentação de Referência**
- Regras de Negócio - Central de Ajuda

## 🎯 14. Metas Gerenciais

### **Descrição**
Tela que permite a criação de metas gerenciais, para inclusão e configuração de regras para unidades gerenciais, que impactam diretamente na apuração dos resultados.

### **Características**
- Criação de metas gerenciais
- Configuração de regras
- Unidades gerenciais
- Apuração de resultados
- Estruturas hierárquicas
- Composição de metas
- Metas filhas
- Dependências entre regras

### **Documentação de Referência**
- Metas Gerenciais - Central de Ajuda

## 🎓 Recursos de Formação Disponíveis

### **Universidade Sankhya**
- Plataforma de Personalização (Construtor de Telas)
- Construtor de Dashboard
- Dashboards: Configurações Básicas
- Construindo um Dashboard
- Dashboard: Configurações Avançadas e dicas
- O que são 'Parâmetros' no Construtor de Componentes de BI
- Formatador de Relatório iReport
- Tarefas de Serviço do Sankhya Flow e como configurá-las
- Certified Associate Sankhya Developer - Java Web
- Certified Associate Sankhya Developer - Banco de Dados

### **YouTube**
- Sankhya Dev Talks - Desenvolvendo Com Módulo Java

### **Comunidade Sankhya Developer**
- Q & A Sankhya Dev Talks - Módulo Java

## 🔗 Recursos de Apoio

### **Central de Ajuda**
- Construtor de Telas
- Dicionário de Dados
- Como Identificar Telas Personalizadas
- Módulo Java
- Ações Agendadas
- Gerenciador de Objetos
- Construtor de Dashboards
- Construtor de Componentes de BI
- Formatador de Relatórios
- iReport Plugin
- EDI Bancário
- Consolidador de Dados
- Tarefas de Serviço com Web Service Interno
- Tarefas de Serviço com Web Service Externo
- Regras de Negócio
- Metas Gerenciais

### **Portal Sankhya Developer**
- Botão de ação
- API de Serviços (Integração)
- Mapeando Serviços
- Add-on studio

### **Central de Downloads Sankhya**
- iReport Download

## 🆘 Suporte e Comunidade

Para tirar dúvidas e compartilhar informações, use a **sala Personalização** da comunidade Sankhya Developer.

---

*Este documento foi criado com base na documentação oficial do Sankhya Developer sobre tipos de personalização/customização.*
