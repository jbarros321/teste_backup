# 🏗️ Conceitos Fundamentais Sankhya

## 🎯 **Visão Geral dos Conceitos Básicos**

Este documento consolida os conceitos fundamentais do sistema Sankhya, baseado na análise completa dos recursos oficiais e melhores práticas identificadas.

## 📚 **Conceitos Principais**

### **1. Dicionário de Dados**
O Dicionário de Dados é o coração do sistema Sankhya, responsável por definir e gerenciar todas as estruturas de dados.

#### **Características Principais**
- **Tabelas Personalizadas**: Criação de novas estruturas de dados
- **Campos Customizados**: Adição de campos em tabelas existentes
- **Relacionamentos**: Definição de relacionamentos entre tabelas
- **Validações**: Regras de validação de dados
- **Índices**: Otimização de performance
- **Triggers**: Ações automáticas em dados

#### **Exemplo Básico**
```sql
-- Criação de tabela personalizada
CREATE TABLE AD_EXEMPLO (
    ID NUMBER(10) NOT NULL,
    NOME VARCHAR2(100) NOT NULL,
    ATIVO CHAR(1) DEFAULT 'S',
    DATA_CRIACAO DATE DEFAULT SYSDATE,
    CONSTRAINT PK_AD_EXEMPLO PRIMARY KEY (ID)
);
```

### **2. Eventos Programados**
Eventos programados permitem executar código automaticamente quando determinadas operações ocorrem no sistema.

#### **Tipos de Eventos**
- **Before Insert**: Antes de inserir um registro
- **After Insert**: Após inserir um registro
- **Before Update**: Antes de atualizar um registro
- **After Update**: Após atualizar um registro
- **Before Delete**: Antes de excluir um registro
- **After Delete**: Após excluir um registro

#### **Exemplo Básico**
```java
public void beforeInsert(PersistenceEvent event) throws Exception {
    DynamicVO vo = (DynamicVO) event.getVo();
    
    // Validação básica
    String nome = vo.getProperty("NOME");
    if (nome == null || nome.trim().isEmpty()) {
        throw new Exception("Nome é obrigatório");
    }
}
```

### **3. Botões de Ação**
Botões de ação permitem executar rotinas personalizadas a partir de qualquer tela do sistema.

#### **Tipos de Rotinas**
- **Rotina Lançador**: Navegação para outras telas
- **Rotina Banco de Dados**: Execução de procedures SQL
- **Rotina JavaScript**: Execução de código client-side
- **Rotina Java**: Execução de código server-side
- **Transação Manual**: Controle manual de transações

#### **Exemplo Básico**
```javascript
function exemploBotaoAcao() {
    try {
        var registro = getCurrentRecord();
        
        if (!registro) {
            throw new Error('Nenhum registro selecionado');
        }
        
        // Executar ação
        executarAcao(registro);
        
        showMessage('Ação executada com sucesso!');
        refreshCurrentScreen();
        
    } catch (error) {
        showError('Erro: ' + error.message);
    }
}
```

### **4. SankhyaJS Framework**
SankhyaJS é o framework para desenvolvimento de componentes HTML5 interativos.

#### **Características Principais**
- **Baseado em AngularJS**: Framework JavaScript robusto
- **Componentes HTML5**: Interface moderna e responsiva
- **Integração com Backend**: Comunicação com APIs Sankhya
- **Validações Client-side**: Validações em tempo real
- **Navegação**: Funções de navegação integradas

#### **Exemplo Básico**
```html
<div class="sankhya-component" ng-controller="ExemploController">
    <div class="component-header">
        <h2>Exemplo SankhyaJS</h2>
    </div>
    
    <div class="component-content">
        <form ng-submit="salvar()">
            <div class="form-group">
                <label for="nome">Nome:</label>
                <input type="text" id="nome" ng-model="dados.nome" required>
            </div>
            
            <div class="form-actions">
                <button type="submit" class="btn-primary">Salvar</button>
            </div>
        </form>
    </div>
</div>

<script>
function ExemploController($scope, $http) {
    $scope.dados = { nome: '' };
    
    $scope.salvar = function() {
        $http.post('/sankhya/api/exemplo', $scope.dados)
            .then(function(response) {
                alert('Dados salvos com sucesso!');
            });
    };
}
</script>
```

### **5. Relatórios Formatados**
Relatórios formatados permitem criar documentos personalizados usando iReport/JasperReports.

#### **Características Principais**
- **Design Visual**: Interface gráfica para criação
- **Consultas SQL**: Integração com banco de dados
- **Parâmetros**: Relatórios dinâmicos
- **Sub-relatórios**: Relatórios complexos
- **Exportação**: Múltiplos formatos (PDF, Excel, HTML)

#### **Exemplo Básico**
```sql
-- Consulta para relatório
SELECT 
    c.NOMECLI,
    p.DESCRPROD,
    i.QTDNEG,
    i.VLRTOT,
    cab.DTNEG
FROM TGFITE i
INNER JOIN TGFCAB cab ON i.NUNOTA = cab.NUNOTA
INNER JOIN TGFPRO p ON i.CODPROD = p.CODPROD
INNER JOIN TGFPAR c ON cab.CODCLI = c.CODCLI
WHERE cab.DTNEG BETWEEN $P{dataInicio} AND $P{dataFim}
ORDER BY cab.DTNEG DESC
```

## 🛠️ **Ferramentas de Desenvolvimento**

### **1. SDK Sankhya**
O SDK Sankhya fornece as ferramentas necessárias para desenvolvimento.

#### **Componentes do SDK**
- **DevKit**: Ambiente de desenvolvimento
- **Bibliotecas Core**: APIs principais
- **Ferramentas de Build**: Compilação e empacotamento
- **Templates**: Modelos de projeto
- **Documentação**: Referência completa

### **2. Generator Sankhya**
O Generator Sankhya automatiza a criação de código padrão.

#### **Funcionalidades**
- **Geração de Código**: Código automático baseado em templates
- **Scaffolding**: Estrutura de projeto
- **Boilerplate**: Código padrão
- **Customização**: Adaptação de templates

### **3. SankhyaUtil**
SankhyaUtil fornece utilitários para desenvolvimento.

#### **Utilitários Disponíveis**
- **Validações**: Funções de validação
- **Formatação**: Formatação de dados
- **Conversões**: Conversão de tipos
- **Cálculos**: Funções matemáticas
- **Strings**: Manipulação de strings
- **Datas**: Manipulação de datas

## 📊 **Tipos de Personalização**

### **1. Personalizações de Dados**
- **Dicionário de Dados**: Estruturas personalizadas
- **Campos Customizados**: Novos campos
- **Validações**: Regras de validação
- **Triggers**: Ações automáticas

### **2. Personalizações de Interface**
- **SankhyaJS**: Componentes HTML5
- **Dashboards**: Painéis de controle
- **Formulários**: Interfaces personalizadas
- **Relatórios**: Documentos customizados

### **3. Personalizações de Processo**
- **Eventos Programados**: Automação de processos
- **Botões de Ação**: Ações personalizadas
- **Workflows**: Fluxos de trabalho
- **Integrações**: Conectividade externa

### **4. Personalizações de Negócio**
- **Regras de Negócio**: Lógica empresarial
- **Validações**: Controles de qualidade
- **Cálculos**: Processamentos específicos
- **Automações**: Processos automatizados

## 🎯 **Melhores Práticas Fundamentais**

### **1. Nomenclatura**
- **Prefixo AD_**: Para tabelas personalizadas
- **Nomes Descritivos**: Claro e objetivo
- **Convenções**: Seguir padrões estabelecidos
- **Documentação**: Sempre documentar

### **2. Validações**
- **Campos Obrigatórios**: Sempre validar
- **Formatos**: Verificar formatos corretos
- **Regras de Negócio**: Aplicar validações
- **Mensagens**: Erros claros e úteis

### **3. Performance**
- **Índices**: Criar índices necessários
- **Consultas**: Otimizar consultas SQL
- **Cache**: Usar cache quando apropriado
- **Monitoramento**: Acompanhar performance

### **4. Segurança**
- **Validações**: Validar todas as entradas
- **Permissões**: Controlar acesso
- **Auditoria**: Registrar alterações
- **Criptografia**: Proteger dados sensíveis

## 📈 **Próximos Passos**

### **Para Iniciantes**
1. **Configurar Ambiente**: Instalar SDK e ferramentas
2. **Primeiro Projeto**: Criar projeto básico
3. **Conceitos Básicos**: Entender fundamentos
4. **Prática**: Implementar exemplos simples

### **Para Desenvolvedores**
1. **Personalizações**: Implementar customizações
2. **Integrações**: Conectar com sistemas externos
3. **Otimização**: Melhorar performance
4. **Avançado**: Técnicas complexas

### **Para Especialistas**
1. **Arquitetura**: Design de soluções enterprise
2. **Performance**: Otimização avançada
3. **Segurança**: Implementação robusta
4. **Inovação**: Soluções criativas

---

*Estes conceitos fundamentais formam a base para todo desenvolvimento Sankhya, fornecendo o conhecimento essencial para criar soluções robustas e eficientes.*
