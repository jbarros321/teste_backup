# DOCUMENTACAO FUNCIONAL - PERSONALIZACAO BRASSOL

## **Cliente:** Brassol
## **Data:** 03/10/2025
## **Objetivo:** Alteracao Automatica de CFOP

---

## **VISAO GERAL DA SOLUCAO**

### **Objetivo da Personalizacao**
Implementar um botao de acao no sistema Sankhya que permita alterar o CFOP (Codigo Fiscal de Operacoes e Prestacoes) de registros selecionados de forma automatica, contornando as validacoes da trigger do banco de dados.

### **Problema Resolvido**
- **Situacao Anterior:** Alteracoes de CFOP eram bloqueadas pela trigger `TRG_UPT_TGFITE`
- **Solucao Implementada:** Botao de acao que desativa temporariamente a trigger, realiza as alteracoes e reativa a trigger automaticamente

---

## **FUNCIONALIDADES IMPLEMENTADAS**

### **1. Botao de Acao "AlteraCFOP"**
- **Localizacao:** Tela de itens de nota fiscal
- **Acesso:** Atraves do menu de acoes do Sankhya
- **Parametro:** CODCFO (CFOP de destino)

### **2. Processo de Alteracao**
1. **Selecao de Registros:** Usuario seleciona os itens desejados
2. **Configuracao:** Informa o CFOP de destino atraves do parametro
3. **Execucao:** Sistema processa automaticamente:
   - Desativa trigger do banco
   - Altera CFOP de todos os registros selecionados
   - Reativa trigger do banco
   - Exibe resultado da operacao

### **3. Controle de Trigger**
- **Desativacao Automatica:** `TRG_UPT_TGFITE DISABLE`
- **Processamento:** Alteracoes realizadas sem bloqueios
- **Reativacao Automatica:** `TRG_UPT_TGFITE ENABLE`
- **Seguranca:** Garantia de reativacao mesmo em caso de erro

---

## **FLUXO DE USO**

### **Passo 1: Selecao**
- Usuario acessa a tela de itens de nota fiscal
- Seleciona os registros que deseja alterar
- Clica no botao de acao "AlteraCFOP"

### **Passo 2: Configuracao**
- Sistema solicita o parametro CODCFO
- Usuario informa o CFOP de destino (ex: 1411, 1910, etc.)
- Confirma a operacao

### **Passo 3: Processamento**
- Sistema valida os dados informados
- Desativa automaticamente a trigger do banco
- Processa todas as alteracoes de CFOP
- Reativa automaticamente a trigger
- Exibe mensagem de sucesso com quantidade de registros alterados

### **Passo 4: Resultado**
- Usuario recebe confirmacao da operacao
- Registros sao atualizados no banco de dados
- Sistema retorna ao estado normal

---

## **CONFIGURACOES NECESSARIAS**

### **1. Parametro do Botao**
- **Nome:** CODCFO
- **Tipo:** Numero inteiro
- **Descricao:** CFOP de destino para alteracao
- **Obrigatorio:** Sim

### **2. Permissoes**
- Usuario deve ter permissao para executar botoes de acao
- Usuario deve ter permissao para alterar registros de itens
- Usuario deve ter acesso a tela de itens de nota fiscal

### **3. Dependencias**
- Sistema Sankhya funcionando
- Banco de dados acessivel
- Trigger TRG_UPT_TGFITE ativa no banco

---

## **VALIDACOES IMPLEMENTADAS**

### **1. Validacao de Entrada**
- Verifica se ha registros selecionados
- Valida se o parametro CODCFO foi informado
- Confirma se o CFOP de destino e valido

### **2. Validacao de Processo**
- Controle de transacao automatico
- Tratamento de erros por linha
- Logs detalhados para auditoria

### **3. Validacao de Saida**
- Confirmacao de alteracoes realizadas
- Contagem de registros processados
- Verificacao de integridade dos dados

---

## **RESULTADOS ESPERADOS**

### **Beneficios para o Usuario**
- **Agilidade:** Alteracao em lote de CFOPs
- **Seguranca:** Controle automatico de trigger
- **Simplicidade:** Interface intuitiva
- **Confiabilidade:** Processamento sem erros

### **Beneficios para o Sistema**
- **Integridade:** Dados sempre consistentes
- **Performance:** Operacoes otimizadas
- **Auditoria:** Logs completos de operacoes
- **Manutenibilidade:** Codigo limpo e documentado

---

## **CONSIDERACOES IMPORTANTES**

### **Limitacoes**
- Funciona apenas com registros de itens de nota fiscal
- Requer permissoes adequadas do usuario
- Dependente da trigger TRG_UPT_TGFITE estar ativa

### **Seguranca**
- Controle automatico de trigger garante integridade
- Validacoes impedem operacoes invalidas
- Logs permitem auditoria completa

### **Performance**
- Processamento otimizado para grandes volumes
- Controle de transacao eficiente
- Tempo de execucao minimo

---

## **SUPORTE E MANUTENCAO**

### **Manutencao Preventiva**
- Verificacao periodica da trigger do banco
- Monitoramento de logs de execucao
- Atualizacoes conforme evolucao do sistema

### **Suporte Tecnico**
- Documentacao completa disponivel
- Codigo fonte versionado
- Logs detalhados para diagnostico

---

## **STATUS DA IMPLEMENTACAO**

**PERSONALIZACAO CONCLUIDA COM SUCESSO**

- Funcionalidade implementada e testada
- Documentacao entregue
- Treinamento realizado
- Sistema em producao
- Cliente satisfeito

---

**Documento elaborado em:** 03/10/2025
**Desenvolvedor:** Leandro Marcos Moreira
**Cliente:** Brassol
**Contato:** Emerson da Brassol
**Versao:** 1.0
