# SatyaPass - Base de Conhecimento

## 📚 Visão Geral

Este repositório contém toda a base de conhecimento do projeto **SatyaPass**, incluindo exemplos de código, documentação técnica, metadados e materiais de estudo relacionados ao desenvolvimento de personalizações no Sankhya.

## 📁 Estrutura do Projeto

```
SatyaPass/
├── docs/                    # Documentação em PDF
├── exemplos/                # Códigos-fonte e exemplos práticos
├── metadados/               # Metadados de tabelas customizadas
└── zip_originais/          # Arquivos ZIP originais (backup)
```

## 📖 Documentação

### Documentos Disponíveis

- **`Documentacao_Modulo_5.pdf`** - Documentação completa do Módulo 5
- **`projeto_2_documento.pdf`** - Documentação do Projeto 2

## 💻 Exemplos de Código

### Módulos de Abastecimento

#### Abastecimento Avulso
- **`abastecimento_01/`** - Exemplo inicial de abastecimento
  - Geração de requisição de abastecimento
  - Repository pattern para acesso a dados
  - Service layer para regras de negócio
  - Listener para eventos do Sankhya
  - Botão de ação para gerar requisição

- **`abastecimento_02/`** - Evolução do exemplo 01
  - Melhorias na estrutura de código
  - Tratamento de erros aprimorado

- **`abastecimento_confirmacao/`** - Confirmação de abastecimento
  - Processo de confirmação de requisição
  - Sistema de logs integrado
  - Botão para deletar requisição

- **`abastecimento_excluir/`** - Exclusão de abastecimento
  - Lógica de exclusão de requisições
  - Validações antes da exclusão

- **`abastecimento_fim/`** - Finalização do processo
  - Processo completo de abastecimento
  - Integração com múltiplos componentes

- **`abastecimento_item/`** - Gestão de itens
  - Inclusão de itens na requisição
  - Validações de itens

- **`abastecimento_log/`** - Sistema de logs
  - Logging de operações
  - Auditoria de ações

### Módulos de Logística

#### Logística SatyaPass (Versões 7-12)
- **`logistica-satyapass-7/`** - Versão inicial
- **`logistica-satyapass-8/`** - Evolução 1
- **`logistica-satyapass-9/`** - Evolução 2
- **`logistica-satyapass-10/`** - Evolução 3
- **`logistica-satyapass-11/`** - Evolução 4
- **`logistica-satyapass-12/** - Versão mais completa
  - Sistema completo de logística
  - Integração com múltiplas funcionalidades
  - Estrutura modular e escalável

### Módulo 5 - Integração ValeCard

#### Aulas Práticas
- **`modulo5_aula6/`** - Aula 6 - Integração básica
- **`modulo5_aulas7/`** - Aula 7 - Evolução
- **`modulo5_aula_8/`** - Aula 8 - Avanços
- **`modulo5_aula_11/`** - Aula 11 - Repository pattern
- **`modulo5_aula12/`** - Aula 12 - Validações
- **`modulo5_aula13/`** - Aula 13 - Processamento
- **`modulo5_aula16-validacao/`** - Aula 16 - Sistema de validação
- **`modulo5_aula17-transacao/`** - Aula 17 - Transações

**Características dos Módulos 5:**
- Integração com ValeCard
- Processamento de arquivos Excel
- Validações de dados
- Sistema transacional
- Repository pattern
- Service layer

### Módulo 3 - Rotina de Log

- **`modulo_3_integracao_rotina_de_log/`** - Integração de rotina de log
- **`rotinadelog/`** - Rotina de log completa
  - Sistema de logging centralizado
  - Auditoria de operações
  - Tratamento de erros

### Módulo 2 - Código Fonte Completo

- **`codigo_fonte_completo_modulo_2/`** - Código completo do módulo 2
  - Estrutura base de projeto
  - Padrões de código
  - Boas práticas

### Acesso a Dados

- **`acessodados/`** - Exemplos de acesso a dados
  - Repository pattern
  - Queries SQL
  - EntityFacade examples

## 🗄️ Metadados

### Tabelas Customizadas

- **`Metadados_AD_ABTAVU/`** - Metadados da tabela AD_ABTAVU (Abastecimento Avulso)
  - Estrutura da tabela
  - Campos e tipos
  - Relacionamentos

- **`Metadados_AD_IMPCAB/`** - Metadados da tabela AD_IMPCAB (Importação de Cabeçalho)
  - Estrutura da tabela
  - Campos e tipos
  - Relacionamentos

## 📊 Estatísticas

- **Total de arquivos Java:** 238
- **Exemplos de código:** 26 projetos
- **Documentos PDF:** 2
- **Metadados:** 2 tabelas
- **Arquivos ZIP originais:** 26

## 🎯 Padrões e Boas Práticas Identificados

### Arquitetura

1. **Repository Pattern**
   - Separação de acesso a dados
   - Reutilização de código
   - Facilita testes

2. **Service Layer**
   - Regras de negócio centralizadas
   - Validações
   - Orquestração de operações

3. **Listener Pattern**
   - Eventos do Sankhya
   - Validações automáticas
   - Integrações

### Estrutura de Código

```
src/
├── br/com/satyapass/
│   ├── [modulo]/
│   │   ├── Main.java                    # Classe principal
│   │   ├── repository/                  # Acesso a dados
│   │   ├── service/                     # Regras de negócio
│   │   ├── listener/                    # Eventos
│   │   └── view/                        # Botões de ação
│   └── ...
```

### Componentes Sankhya Utilizados

- **EntityFacade** - Acesso a entidades
- **DynamicVO** - Valores dinâmicos
- **CACHelper** - Helper de Centrais
- **BarramentoRegra** - Regras de negócio
- **AuthenticationInfo** - Informações de autenticação
- **TipoOperacaoUtils** - Utilitários de operação

## 🔧 Como Usar

### 1. Explorar Exemplos

Navegue pelos exemplos na pasta `exemplos/` para entender diferentes padrões e implementações.

### 2. Consultar Documentação

Os documentos PDF na pasta `docs/` contêm informações detalhadas sobre os módulos.

### 3. Verificar Metadados

Os metadados na pasta `metadados/` mostram a estrutura das tabelas customizadas utilizadas.

### 4. Estudar Evolução

Os exemplos de logística (versões 7-12) mostram a evolução de um projeto, desde a versão inicial até uma implementação completa.

## 📝 Notas Importantes

- Todos os arquivos ZIP originais foram preservados em `zip_originais/`
- Os exemplos foram extraídos e organizados por funcionalidade
- Alguns projetos contêm arquivos de configuração do IntelliJ IDEA (`.idea/`)
- Arquivos `.DS_Store` e `__MACOSX/` são do macOS e podem ser ignorados

## 🚀 Próximos Passos

1. Analisar os exemplos de código para entender padrões
2. Consultar a documentação para contexto teórico
3. Verificar metadados para entender estrutura de dados
4. Adaptar exemplos para necessidades específicas

---

**Base de Conhecimento SatyaPass** - Extraída e organizada em `2025`

