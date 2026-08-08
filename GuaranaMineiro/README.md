# ?? GuaranaMineiro - Sistema de Integração Performaxxi

## ?? Visão Geral

O **GuaranaMineiro** é um sistema de integração completo entre o Sankhya e a plataforma Performaxxi, implementando três módulos principais para automatizar processos logísticos e de comunicação.

## ?? Módulos do Sistema

### 1. ?? Ação Agendada - Comprovantes de Entrega
- **Classe**: `ComprovantesEntrega.java`
- **Funcionalidade**: Consulta automática de comprovantes de entrega
- **Agendamento**: Execução diária para veículos com ordem de carga
- **Documentação**: [docs/acaoAgendada/](./docs/acaoAgendada/)

### 2. ?? Botão de Ação - Integração de Pedidos
- **Classe**: `IntegraPerformaxxi.java`
- **Funcionalidade**: Envio manual de pedidos para otimização de rotas
- **Interface**: Botão de ação com parâmetros configuráveis
- **Documentação**: [docs/botaoAcao/](./docs/botaoAcao/)

### 3. ?? Evento Programado - Notificação de Recebimentos
- **Classe**: `RecebimentoEvento.java`
- **Funcionalidade**: Notificação automática de recebimentos via PIX
- **Trigger**: Baixa de títulos na tabela `TGFFIN`
- **Documentação**: [docs/evento/](./docs/evento/)

## ??? Arquitetura

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

## ?? Instalação e Configuração

### 1. Pré-requisitos
- Sankhya Framework
- Java 8+
- Acesso à API Performaxxi
- Permissões de banco de dados

### 2. Deploy do Módulo
```bash
# Compilar projeto
mvn clean package

# Deploy do JAR
cp target/guaranamineiro-1.0.0.jar [SANKHYA_HOME]/modules/
```

### 3. Configuração da API
- **URL Base**: `https://www.performaxxi.com.br`
- **Autenticação**: Basic Auth
- **Endpoints**:
  - `/API.REST/importacao/pedidos` (Botão de Ação)
  - `/API.REST/comprovantes-entrega` (Ação Agendada)
  - `/API.REST/enviar-mensagem-mobile` (Evento Programado)

### 4. Criação de Tabelas
Importar metadados das tabelas de controle:
- `AD_COMPROVANTES`
- `AD_INTPERFORMAXXILOG`
- `AD_RECEBIMENTOLOG`

## ?? Monitoramento

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

## ?? Troubleshooting

### Problemas Comuns
1. **Erro de Autenticação**: Verificar credenciais da API
2. **Timeout**: Aumentar timeouts de conexão
3. **Dados Inválidos**: Verificar validação de parâmetros
4. **Logs Não Aparecem**: Verificar permissões de banco

### Suporte
- **Documentação**: [docs/](./docs/)
- **Logs**: Consultar tabelas de controle
- **Contato**: Sistema de Integração GuaranaMineiro

## ?? Estrutura do Projeto

```
GuaranaMineiro/
??? README.md
??? src/
?   ??? br/com/performaxxi/action/
?       ??? acaoAgendada/
?       ?   ??? ComprovantesEntrega.java
?       ??? botaoAcao/
?       ?   ??? IntegraPerformaxxi.java
?       ??? evento/
?           ??? RecebimentoEvento.java
??? docs/
?   ??? INDICE_DOCUMENTACAO_COMPLETA.md
?   ??? acaoAgendada/
?   ??? botaoAcao/
?   ??? evento/
??? pom.xml
```

## ?? Métricas e KPIs

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

## ?? Segurança

### Dados Sensíveis
- **Credenciais API**: Armazenadas em configuração
- **Celulares**: Formatados para envio
- **Valores**: Logados apenas para auditoria

### Controle de Acesso
- **Execução Manual**: Apenas usuários autorizados
- **Logs**: Apenas administradores
- **Configuração**: Apenas desenvolvedores

## ?? Suporte e Contato

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

## ?? Documentação Completa

Para documentação detalhada de cada módulo, consulte:

- [?? Índice da Documentação Completa](./docs/INDICE_DOCUMENTACAO_COMPLETA.md)
- [?? Ação Agendada - Comprovantes de Entrega](./docs/acaoAgendada/)
- [?? Botão de Ação - Integração Performaxxi](./docs/botaoAcao/)
- [?? Evento Programado - Recebimento](./docs/evento/)

---

**Versão**: 1.0.0
**Última atualização**: Janeiro 2025
**Autor**: Sistema de Integração GuaranaMineiro
