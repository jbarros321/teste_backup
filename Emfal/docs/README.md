# EMFAL - INTEGRACAO SERASA EXPERIAN

## **Objetivo:** Integracao completa com API Serasa Experian para consultas de score, pendencias, alertas e monitoramento continuo
## **Data:** 03/01/2025
## **Versao:** 1.0.0

---

## **VISAO GERAL**

### **O que e esta Integracao**
Esta integracao fornece uma solucao completa para consultas na API Serasa Experian, incluindo:
- Consulta de score PF/PJ
- Consulta de pendencias financeiras
- Consulta de alertas de documentos
- Consulta de informacoes cadastrais
- Consulta de renda presumida
- Consulta de acoes judiciais
- Sistema de monitoramento continuo
- Sistema de alertas automaticos
- Auditoria completa de consultas

### **Quando Usar**
- Consultas de credito e analise de risco
- Monitoramento continuo de clientes
- Analise de pendencias financeiras
- Verificacao de documentos
- Analise de renda e faturamento
- Acompanhamento de acoes judiciais

---

## **ESTRUTURA DO PROJETO**

```
Emfal/
??? docs/                    # Documentacao
?   ??? README.md           # Este arquivo
??? lib/                    # Dependencias
?   ??? SankhyaW-extensions.jar
??? src/                    # Codigo fonte
?   ??? br/com/emfal/serasa/
?       ??? action/         # Acoes principais
?       ??? helper/         # Classes auxiliares
?       ??? client/         # Cliente HTTP
?       ??? auth/           # Gerenciador de autenticacao
?       ??? config/         # Configuracoes
?       ??? logger/         # Sistema de logs
?       ??? SerasaDataMapper.java
?   ??? main/sql/           # Scripts SQL
??? target/                 # Arquivos compilados
??? pom.xml                 # Configuracao Maven
```

---

## **COMO USAR A INTEGRACAO**

### **Passo 1: Configurar Credenciais**
1. Obter credenciais da API Serasa Experian
2. Configurar Client ID e Client Secret
3. Definir URL da API e parametros

### **Passo 2: Executar Scripts SQL**
```sql
-- Executar script de criacao das tabelas
@src/main/sql/01_criar_tabelas_serasa.sql
```

### **Passo 3: Configurar Sistema**
1. Acessar tela de configuracao Serasa
2. Inserir credenciais da API
3. Configurar parametros de consulta
4. Ativar monitoramento continuo

### **Passo 4: Usar a Integracao**
1. Selecionar registros na tela desejada
2. Executar acao "Consulta Serasa Completa"
3. Aguardar processamento
4. Verificar resultados nos campos atualizados

### **Passo 5: Monitoramento**
1. Acessar dashboard de monitoramento
2. Verificar logs de consultas
3. Configurar alertas automaticos
4. Acompanhar estatisticas de uso

---

## **CONFIGURACOES IMPORTANTES**

### **Dependencias**
- **SankhyaW-extensions.jar** - Biblioteca principal do Sankhya
- **Java 8** - Versao compativel com Sankhya
- **Maven** - Gerenciamento de dependencias
- **API Serasa Experian** - Credenciais de acesso

### **Estrutura de Pacote**
```
br.com.emfal.serasa
```
- `br.com.emfal` - Pacote do cliente Emfal
- `serasa` - Integracao com Serasa Experian
- `action` - Acoes principais
- `helper` - Classes auxiliares
- `client` - Cliente HTTP
- `auth` - Autenticacao
- `config` - Configuracoes
- `logger` - Sistema de logs

### **Configuracao Maven**
- **Source/Target:** Java 8
- **Encoding:** ISO-8859-1
- **Packaging:** JAR
- **Dependencies:** System scope para SankhyaW
- **Artifact:** emfal-serasa-integracao

---

## **EXEMPLO DE USO**

### **Criar Projeto "Emfal Serasa"**
```bash
# 1. Copiar template (ja feito)
# cp -r Template Emfal
# cd Emfal

# 2. Ajustar pom.xml (ja feito)
# - groupId: br.com.emfal
# - artifactId: emfal-serasa-integracao
# - name: Emfal - Integracao Serasa Experian

# 3. Renomear classe (ja feito, agora com estrutura de pacotes)
# mv src/br/com/cliente/action/botaoAcao/PersonalizacaoSankhya.java \
#    src/br/com/emfal/serasa/action/ConsultaSerasaCompletaAction.java

# 4. Ajustar pacote na classe (ja feito)
# package br.com.emfal.serasa.action;

# 5. Executar scripts SQL (proximo passo)
# @src/main/sql/01_criar_tabelas_serasa.sql

# 6. Compilar e empacotar (proximo passo)
# mvn clean compile
# mvn package
```

---

## **BOAS PRATICAS**

### **Desenvolvimento**
- Sempre validar registros selecionados
- Implementar tratamento de erros
- Usar logs para debug
- Testar em ambiente de desenvolvimento

### **Codigo**
- Manter codigo limpo e documentado
- Seguir padroes Java
- Usar nomes descritivos
- Comentar logica complexa

### **Documentacao**
- Manter README atualizado
- Documentar funcionalidades
- Criar relatorios de atendimento
- Versionar adequadamente

---

## **TROUBLESHOOTING**

### **Problemas Comuns**

#### **Erro de Compilacao**
- Verificar se SankhyaW-extensions.jar esta na pasta lib/
- Confirmar versao do Java (deve ser 8)
- Verificar sintaxe do codigo
- Verificar problemas de encoding (caracteres especiais)

#### **Erro de Dependencia**
- Verificar se o JAR esta no classpath
- Confirmar configuracao do Maven
- Verificar se o arquivo existe

#### **Problemas no Sankhya**
- Verificar se a classe implementa AcaoRotinaJava
- Confirmar se o metodo doAction esta correto
- Testar com registros simples primeiro

---

## **CONTATO E SUPORTE**

**Desenvolvedor:** Assistente de IA
**Template Version:** 1.0.0
**Data de Criacao:** 03/01/2025
**Compatibilidade:** Sankhya + Java 8 + Maven

---

## **CHANGELOG**

### **v1.0.0 (03/01/2025)**
- Criacao da integracao Serasa Experian
- Estrutura completa para Cenario 2
- Implementacao de todas as classes e funcionalidades
- Documentacao completa e scripts SQL
