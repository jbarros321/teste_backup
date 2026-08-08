# 🎯 Assistente de Melhores Práticas Sankhya

## 🎯 Visão Geral

Este documento consolida as melhores práticas para configuração e uso do Sankhya, baseado nas orientações oficiais e experiência da comunidade. O objetivo é fornecer configurações de alto padrão para otimizar a gestão e performance do sistema.

## 🏗️ **Configurações de Alto Padrão**

### **1. Configurações de Sistema**

#### **Parâmetros de Performance**
```sql
-- Configurações recomendadas para performance
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.USAR.PAGINACAO.DE.REGISTROS';
UPDATE TSIPAR SET TEXTO = '1000' WHERE CHAVE = 'GLOBAL.TAMANHO.PAGINA.PADRAO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.USAR.CACHE.DE.CONSULTAS';
UPDATE TSIPAR SET TEXTO = '300' WHERE CHAVE = 'GLOBAL.TIMEOUT.CONSULTAS.SEGUNDOS';
```

#### **Configurações de Segurança**
```sql
-- Configurações de segurança recomendadas
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.LOGAR.TENTATIVAS.LOGIN';
UPDATE TSIPAR SET TEXTO = '5' WHERE CHAVE = 'GLOBAL.MAXIMO.TENTATIVAS.LOGIN';
UPDATE TSIPAR SET TEXTO = '30' WHERE CHAVE = 'GLOBAL.TEMPO.BLOQUEIO.LOGIN.MINUTOS';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.OBRIGAR.SENHA.FORTE';
UPDATE TSIPAR SET TEXTO = '90' WHERE CHAVE = 'GLOBAL.DIAS.VALIDADE.SENHA';
```

#### **Configurações de Backup**
```sql
-- Configurações de backup automático
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.BACKUP.AUTOMATICO';
UPDATE TSIPAR SET TEXTO = '02:00' WHERE CHAVE = 'GLOBAL.HORARIO.BACKUP';
UPDATE TSIPAR SET TEXTO = '7' WHERE CHAVE = 'GLOBAL.DIAS.RETER.BACKUP';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.COMPRIMIR.BACKUP';
```

### **2. Configurações por Módulo**

#### **Módulo Financeiro**
```sql
-- Configurações financeiras recomendadas
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FIN.USAR.CENTRO.CUSTO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FIN.USAR.CONTA.CORRENTE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FIN.VALIDAR.LIMITE.CREDITO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FIN.CONTROLAR.CHEQUES';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'FIN.USAR.CONCILIACAO.BANCARIA';
```

#### **Módulo de Vendas**
```sql
-- Configurações de vendas recomendadas
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VEN.USAR.COMISSAO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VEN.VALIDAR.ESTOQUE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VEN.CONTROLAR.LIMITE.CREDITO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VEN.USAR.DESCONTO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VEN.CONTROLAR.PRECO.MINIMO';
```

#### **Módulo de Estoque**
```sql
-- Configurações de estoque recomendadas
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'EST.USAR.CONTROLE.LOTE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'EST.USAR.VALIDADE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'EST.CONTROLAR.ESTOQUE.NEGATIVO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'EST.USAR.ENDERECO.ARMAZEM';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'EST.CONTROLAR.ROTATIVIDADE';
```

## 📋 **Templates de Configuração por Segmento**

### **1. Template para Indústria**

#### **Configurações Específicas**
```sql
-- Configurações para indústria
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'IND.USAR.ORDEM.PRODUCAO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'IND.CONTROLAR.RECEPCAO.MATERIA';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'IND.USAR.CONTROLE.QUALIDADE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'IND.CONTROLAR.CUSTOS.PRODUCAO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'IND.USAR.PLANEJAMENTO.NECESSIDADES';
```

#### **Parâmetros de Produção**
```sql
-- Parâmetros específicos de produção
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PROD.USAR.ROTEIRO.PRODUCAO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PROD.CONTROLAR.TEMPO.SETUP';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PROD.USAR.CONTROLE.EFICIENCIA';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PROD.CONTROLAR.DESPERDICIO';
```

### **2. Template para Varejo**

#### **Configurações Específicas**
```sql
-- Configurações para varejo
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VAR.USAR.PDV';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VAR.CONTROLAR.CAIXA';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VAR.USAR.PROMOCOES';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VAR.CONTROLAR.ESTOQUE.MINIMO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'VAR.USAR.CONTROLE.PERDAS';
```

#### **Parâmetros de Vendas**
```sql
-- Parâmetros específicos de varejo
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PDV.USAR.IMPRESSORA.FISCAL';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PDV.CONTROLAR.CUPOM.NAO.FISCAL';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PDV.USAR.MULTIPLOS.CAIXAS';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'PDV.CONTROLAR.TROCO.AUTOMATICO';
```

### **3. Template para Serviços**

#### **Configurações Específicas**
```sql
-- Configurações para serviços
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'SERV.USAR.CONTROLE.HORAS';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'SERV.CONTROLAR.PROJETOS';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'SERV.USAR.CONTROLE.CUSTOS';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'SERV.CONTROLAR.APROVACAO.ORCAMENTO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'SERV.USAR.CONTROLE.QUALIDADE';
```

## 🔧 **Otimizações de Performance**

### **1. Configurações de Banco de Dados**

#### **Índices Recomendados**
```sql
-- Índices para otimização de performance
CREATE INDEX IDX_TGFCAB_DT_EMISSAO ON TGFCAB(DTEMISSAO);
CREATE INDEX IDX_TGFCAB_CODPARC ON TGFCAB(CODPARC);
CREATE INDEX IDX_TGFITE_CODPROD ON TGFITE(CODPROD);
CREATE INDEX IDX_TGFITE_NUNOTA ON TGFITE(NUNOTA);
CREATE INDEX IDX_TGFPRO_CODPROD ON TGFPRO(CODPROD);

-- Índices compostos para consultas complexas
CREATE INDEX IDX_TGFCAB_COMPOSTO ON TGFCAB(CODPARC, DTEMISSAO, TIPMOV);
CREATE INDEX IDX_TGFITE_COMPOSTO ON TGFITE(NUNOTA, CODPROD, SEQUENCIA);
```

#### **Configurações de Memória**
```sql
-- Configurações de memória para Oracle
-- (Aplicar no arquivo de configuração do Oracle)
-- SGA_TARGET = 2G
-- PGA_AGGREGATE_TARGET = 1G
-- SHARED_POOL_SIZE = 512M
-- DB_CACHE_SIZE = 1G
```

### **2. Configurações de Aplicação**

#### **Cache de Consultas**
```sql
-- Configurações de cache
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.USAR.CACHE.CONSULTAS';
UPDATE TSIPAR SET TEXTO = '10000' WHERE CHAVE = 'GLOBAL.TAMANHO.CACHE.CONSULTAS';
UPDATE TSIPAR SET TEXTO = '3600' WHERE CHAVE = 'GLOBAL.TEMPO.VALIDADE.CACHE.SEGUNDOS';
```

#### **Paginação de Registros**
```sql
-- Configurações de paginação
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.USAR.PAGINACAO.DE.REGISTROS';
UPDATE TSIPAR SET TEXTO = '500' WHERE CHAVE = 'GLOBAL.TAMANHO.PAGINA.PADRAO';
UPDATE TSIPAR SET TEXTO = '1000' WHERE CHAVE = 'GLOBAL.TAMANHO.PAGINA.MAXIMO';
```

## 🛡️ **Configurações de Segurança**

### **1. Controle de Acesso**

#### **Políticas de Senha**
```sql
-- Configurações de senha
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.OBRIGAR.SENHA.FORTE';
UPDATE TSIPAR SET TEXTO = '8' WHERE CHAVE = 'GLOBAL.TAMANHO.MINIMO.SENHA';
UPDATE TSIPAR SET TEXTO = '90' WHERE CHAVE = 'GLOBAL.DIAS.VALIDADE.SENHA';
UPDATE TSIPAR SET TEXTO = '5' WHERE CHAVE = 'GLOBAL.HISTORICO.SENHAS';
```

#### **Controle de Sessão**
```sql
-- Configurações de sessão
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.CONTROLAR.SESSAO';
UPDATE TSIPAR SET TEXTO = '480' WHERE CHAVE = 'GLOBAL.TEMPO.MAXIMO.SESSAO.MINUTOS';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.BLOQUEAR.MULTIPLAS.SESSOES';
```

### **2. Auditoria e Logs**

#### **Configurações de Log**
```sql
-- Configurações de auditoria
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.LOGAR.ACESSOS';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.LOGAR.ALTERACOES';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.LOGAR.TENTATIVAS.LOGIN';
UPDATE TSIPAR SET TEXTO = '365' WHERE CHAVE = 'GLOBAL.DIAS.RETER.LOGS';
```

## 📊 **Monitoramento e Manutenção**

### **1. Configurações de Monitoramento**

#### **Alertas de Sistema**
```sql
-- Configurações de alertas
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.ALERTAR.ESTOQUE.MINIMO';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.ALERTAR.VENCIMENTO.CONTAS';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.ALERTAR.BACKUP.FALHA';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.ALERTAR.PERFORMANCE.BAIXA';
```

#### **Relatórios Automáticos**
```sql
-- Configurações de relatórios
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.RELATORIO.DIARIO.VENDAS';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.RELATORIO.SEMANAL.ESTOQUE';
UPDATE TSIPAR SET TEXTO = 'S' WHERE CHAVE = 'GLOBAL.RELATORIO.MENSAL.FINANCEIRO';
```

### **2. Manutenção Preventiva**

#### **Scripts de Manutenção**
```sql
-- Script de limpeza de logs antigos
DELETE FROM TSILOG 
WHERE DTLOG < ADD_MONTHS(SYSDATE, -12);

-- Script de otimização de índices
ANALYZE TABLE TGFCAB COMPUTE STATISTICS;
ANALYZE TABLE TGFITE COMPUTE STATISTICS;
ANALYZE TABLE TGFPRO COMPUTE STATISTICS;

-- Script de limpeza de sessões inativas
DELETE FROM TSISESSAO 
WHERE DTULTIMOACESSO < SYSDATE - 1;
```

## 🎯 **Checklist de Implementação**

### **Fase 1: Configurações Básicas**
- [ ] Configurar parâmetros de performance
- [ ] Definir configurações de segurança
- [ ] Configurar backup automático
- [ ] Aplicar configurações por módulo

### **Fase 2: Otimizações**
- [ ] Criar índices recomendados
- [ ] Configurar cache de consultas
- [ ] Implementar paginação
- [ ] Configurar monitoramento

### **Fase 3: Segurança**
- [ ] Implementar políticas de senha
- [ ] Configurar controle de sessão
- [ ] Ativar auditoria e logs
- [ ] Configurar alertas de sistema

### **Fase 4: Monitoramento**
- [ ] Configurar alertas automáticos
- [ ] Implementar relatórios automáticos
- [ ] Configurar manutenção preventiva
- [ ] Validar configurações

## 🎊 **Benefícios Esperados**

### **✅ Performance:**
- **Consultas mais rápidas**: Índices otimizados
- **Menor uso de recursos**: Cache e paginação
- **Melhor responsividade**: Configurações otimizadas

### **✅ Segurança:**
- **Controle de acesso**: Políticas rigorosas
- **Auditoria completa**: Logs detalhados
- **Proteção de dados**: Backup automático

### **✅ Manutenibilidade:**
- **Monitoramento proativo**: Alertas automáticos
- **Manutenção preventiva**: Scripts automatizados
- **Relatórios automáticos**: Visibilidade completa

### **✅ Confiabilidade:**
- **Backup automático**: Proteção contra perda
- **Recuperação rápida**: Processos otimizados
- **Alta disponibilidade**: Configurações robustas

---

*Este documento foi criado com base nas melhores práticas oficiais da Sankhya e experiência da comunidade para fornecer configurações de alto padrão.*
