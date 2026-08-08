# Documentação Unificada Completa - GuaranaMineiro

## Visão Geral do Projeto

O projeto **GuaranaMineiro** é um sistema de integração completo entre o Sankhya e a plataforma Performaxxi, implementando três módulos principais para automatizar processos logísticos e de comunicação.

### Objetivo Principal
- **Automatizar** a troca de informações entre Sankhya e Performaxxi
- **Melhorar a eficiencia** operacional e a rastreabilidade de entregas e recebimentos
- **Fornecer ferramentas** para monitoramento e solução de problemas

## Módulos do Sistema

### 1. Ação Agendada - Comprovantes de Entrega
- **Classe**: `ComprovantesEntrega.java`
- **Funcionalidade**: Consulta automática de comprovantes de entrega via API Performaxxi
- **Agendamento**: Execução diária para buscar comprovantes de veículos com ordem de carga
- **Documentação**: [DOCUMENTACAO_FUNCIONAL.md](./acaoAgendada/DOCUMENTACAO_FUNCIONAL.md)

**Características Técnicas**:
- Implementa `ScheduledAction` do Sankhya
- Consulta veículos com ordem de carga do dia atual
- Salva comprovantes na tabela `AD_COMPROVANTES`
- Sistema de logs e notificação por email em caso de erro

### 2. Botão de Ação - Integração de Pedidos
- **Classe**: `IntegraPerformaxxi.java`
- **Funcionalidade**: Envio manual de pedidos para otimização de rotas
- **Interface**: Botao de acao no Sankhya com parametros configuraveis
- **Documentação**: [DOCUMENTACAO_FUNCIONAL.md](./botaoAcao/DOCUMENTACAO_FUNCIONAL.md)

**Características Técnicas**:
- Implementa `AcaoRotinaJava` do Sankhya
- Query SQL dinamica baseada em parametros
- Sistema robusto de validacao e tratamento de erros
- Logs detalhados na tabela `AD_INTPERFORMAXXILOG`

### 3. Evento Programado - Notificação de Recebimentos
- **Classe**: `RecebimentoEvento.java`
- **Funcionalidade**: Notificacao automatica de recebimentos via PIX
- **Trigger**: Baixa de títulos na tabela `TGFFIN`
- **Documentação**: [DOCUMENTACAO_FUNCIONAL.md](./evento/DOCUMENTACAO_FUNCIONAL.md)

**Características Técnicas**:
- Implementa `EventoProgramavelJava` do Sankhya
- Monitora campos específicos: `CODTIPOPERBAIXA`, `DHTIPOPERBAIXA`, `DHBAIXA`, `VLRBAIXA`
- Envio de mensagens mobile via API Performaxxi
- Logs na tabela `AD_RECEBIMENTOLOG`

## Arquitetura do Sistema

### Componentes Compartilhados
- **PerformaxxiAPI**: Classe utilitária para comunicação com a API
- **Sistema de Logs**: Logging unificado em tabelas específicas
- **Tratamento de Erros**: Sistema robusto de tratamento e recuperação

### Fluxo de Dados
```
Sankhya ? PerformaxxiAPI ? Performaxxi ? Notificação/Processamento
```

### Tabelas de Controle
- `AD_COMPROVANTES`: Comprovantes de entrega
- `AD_INTPERFORMAXXILOG`: Logs de integração de pedidos
- `AD_RECEBIMENTOLOG`: Logs de recebimentos

## Guias de Implementação

### 1. Configuração Inicial
1. **Instalar Módulo Java**: Deploy do JAR no Sankhya
2. **Criar Tabelas**: Importar metadados das tabelas de controle
3. **Configurar API**: Definir credenciais da Performaxxi
4. **Configurar Agendamento**: Definir horários de execução

### 2. Pré-requisitos
- Sankhya Framework
- Java 8+
- Acesso a API Performaxxi
- Permissoes de banco de dados

### 3. Deploy do Módulo
```bash
# Compilar projeto
mvn clean package

# Deploy do JAR
cp target/guaranamineiro-1.0.0.jar [SANKHYA_HOME]/modules/
```

### 4. Configuração da API Performaxxi
- **URL Base Produção**: `https://www.performaxxi.com.br`
- **URL Base Homologação**: `https://www.rotaonline.com.br`
- **Ambiente Ativo**: HOMOLOGACAO (configurável)
- **Autenticação**: Basic Auth
- **Endpoints Implementados**:
  - `/API.REST/importacao/pedidos` (Botão de Ação - Envio de Pedidos)
  - `/API.REST/entrega/comprovantes/veiculo` (Ação Agendada - Consulta de Comprovantes)
  - `/API.REST/rota/enviomensagemrota` (Evento Programado - Mensagens de Recebimento)

### 5. Criacao de Tabelas
Importar metadados das tabelas de controle:
- `AD_COMPROVANTES`
- `AD_INTPERFORMAXXILOG`
- `AD_RECEBIMENTOLOG`

### 6. Testes Unitários
- **TesteComprovantesEntrega**: Testa consulta de comprovantes com placas reais
- **TesteIntegraPerformaxxi**: Testa integração de pedidos com dados simulados
- **TesteRecebimentoEvento**: Testa processamento de eventos de recebimento
- **Ambiente**: SEMPRE usa URL de HOMOLOGACAO para testes
- **Taxa de Sucesso**: 100% em todos os testes unitários
- **Encoding**: Compatível com ISO-8859-1 (sem caracteres especiais)

### 7. Monitoramento e Logs
- **Logs de Execução**: Todas as operações são registradas
- **Correlation ID**: Rastreamento de execuções
- **Métricas**: Performance e taxa de sucesso
- **Alertas**: Notificações por email em caso de erro

## Monitoramento

### Logs de Execução
Todas as operações são registradas em tabelas específicas:
- **Ação Agendada**: `AD_COMPROVANTES`
- **Botão de Ação**: `AD_INTPERFORMAXXILOG`
- **Evento Programado**: `AD_RECEBIMENTOLOG`

### Consultas de Diagnóstico
```sql
-- Verificar logs de execução
SELECT * FROM AD_INTPERFORMAXXILOG
WHERE DATA_INICIO >= SYSDATE - 1
ORDER BY DATA_INICIO DESC;

-- Verificar comprovantes processados
SELECT * FROM AD_COMPROVANTES
WHERE DHEXECUCAO >= SYSDATE - 1
ORDER BY DHEXECUCAO DESC;

-- Verificar recebimentos notificados
SELECT * FROM AD_RECEBIMENTOLOG
WHERE DHEXECUCAO >= SYSDATE - 1
ORDER BY DHEXECUCAO DESC;
```

## Troubleshooting

### Problemas Comuns
1. **Erro de Autenticação**: Verificar credenciais da API
2. **Timeout**: Aumentar timeouts de conexão
3. **Dados Inválidos**: Verificar validação de parâmetros
4. **Logs Não Aparecem**: Verificar permissões de banco

### Suporte
- **Documentação**: [docs/](./)
- **Logs**: Consultar tabelas de controle
- **Contato**: Sistema de Integração GuaranaMineiro

## Estrutura de Arquivos

```
GuaranaMineiro/
??? README.md
??? src/
?   ??? br/com/performaxxi/
?       ??? action/
?       ?   ??? acaoAgendada/
?       ?   ?   ??? ComprovantesEntrega.java
?       ?   ??? botaoAcao/
?       ?   ?   ??? IntegraPerformaxxi.java
?       ?   ??? evento/
?       ?       ??? RecebimentoEvento.java
?       ??? shared/
?       ?   ??? PerformaxxiAPI.java
?       ?   ??? PerformaxxiIntegracaoHelper.java
?       ??? test/
?           ??? TesteComprovantesEntrega.java
?           ??? TesteIntegraPerformaxxi.java
?           ??? TesteRecebimentoEvento.java
??? docs/
?   ??? DOCUMENTACAO_FUNCIONAL_COMPLETA.md (este arquivo)
?   ??? acaoAgendada/
?   ?   ??? DOCUMENTACAO_FUNCIONAL.md
?   ??? botaoAcao/
?   ?   ??? DOCUMENTACAO_FUNCIONAL.md
?   ??? evento/
?       ??? DOCUMENTACAO_FUNCIONAL.md
??? pom.xml
```

## Métricas e KPIs

### Ação Agendada
- Número de veículos processados por dia
- Tempo de execução médio
- Taxa de sucesso na consulta de comprovantes

### Botão de Ação
- Número de pedidos enviados por execução
- Tempo de processamento por lote
- Taxa de sucesso na integração

### Evento Programado
- Número de recebimentos notificados por dia
- Tempo de resposta da API
- Taxa de entrega das mensagens

## Segurança

### Dados Sensíveis
- **Credenciais API**: Armazenadas em configuração
- **Celulares**: Formatados para envio
- **Valores**: Logados apenas para auditoria

### Controle de Acesso
- **Execução Manual**: Apenas usuários autorizados
- **Logs**: Apenas administradores
- **Configuração**: Apenas desenvolvedores

## Compatibilidade e Encoding

### Encoding de Caracteres
- **Padrão**: ISO-8859-1 (Latin-1)
- **Caracteres Especiais**: Removidos para compatibilidade
- **Emojis**: Substituídos por texto ASCII
- **Símbolos Unicode**: Convertidos para caracteres seguros

### Caracteres Substituídos
- `?` ? `OK` (sucesso)
- `?` ? `ERRO` (falha)
- `??` ? `SUCESSO` (todos os testes passaram)
- `??` ? `ATENCAO` (alguns testes falharam)

### Testes de Compatibilidade
- **Compilação**: Sem erros de encoding
- **Execução**: Funcionamento normal em ambientes ISO-8859-1
- **Logs**: Mensagens legíveis em qualquer sistema

## Suporte e Contato

### Equipe de Desenvolvimento
- **Responsável**: Sistema de Integração GuaranaMineiro
- **Especialidade**: Integrações Sankhya e APIs externas

### Documentação Técnica
- **Repositório**: GitHub - personalizacoes
- **Versão**: 1.0.0
- **Última Atualização**: Janeiro 2025

### Escalação de Problemas
1. Consultar documentação específica do módulo
2. Verificar logs de execução
3. Testar conectividade com API
4. Contatar suporte técnico

---

## Documentação Detalhada por Módulo

### [Ação Agendada - Comprovantes de Entrega](./acaoAgendada/DOCUMENTACAO_FUNCIONAL.md)
- **Implementação**: `ComprovantesEntrega.java`
- **Funcionalidade**: Consulta automática de comprovantes
- **Agendamento**: Execução diária
- **Tabelas**: `AD_COMPROVANTES`

### [Botão de Ação - Integração Performaxxi](./botaoAcao/DOCUMENTACAO_FUNCIONAL.md)
- **Implementação**: `IntegraPerformaxxi.java`
- **Funcionalidade**: Envio manual de pedidos
- **Interface**: Botão de ação configurável
- **Tabelas**: `AD_INTPERFORMAXXILOG`

### [Evento Programado - Recebimento](./evento/DOCUMENTACAO_FUNCIONAL.md)
- **Implementação**: `RecebimentoEvento.java`
- **Funcionalidade**: Notificação automática de recebimentos
- **Trigger**: Baixa de títulos
- **Tabelas**: `AD_RECEBIMENTOLOG`

---

**Versão**: 1.0.0
**Última atualização**: Janeiro 2025
**Autor**: Sistema de Integração GuaranaMineiro
