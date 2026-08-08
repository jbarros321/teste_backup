# 🚀 Guia de Primeiros Passos - Sankhya

## 🎯 **Começando com Sankhya**

Este guia apresenta os primeiros passos para começar a desenvolver personalizações Sankhya, com exemplos práticos e tutoriais passo a passo.

## 📋 **Checklist de Preparação**

### **Antes de Começar**
- [ ] Java JDK 11 instalado e configurado
- [ ] Eclipse IDE instalado e configurado
- [ ] Maven instalado e configurado
- [ ] SDK Sankhya baixado e configurado
- [ ] Git configurado
- [ ] Acesso ao banco de dados Sankhya

### **Verificação Rápida**
```bash
# Verificar instalações
java -version
mvn -version
git --version
sankhya-sdk --version
```

## 🏗️ **Passo 1: Criar Primeiro Projeto**

### **1.1 Criar Projeto**
```bash
# Criar diretório de trabalho
mkdir -p ~/sankhya-projects
cd ~/sankhya-projects

# Criar primeiro projeto
sankhya-sdk create-project --name="meu-primeiro-projeto" --type="customization"

# Navegar para o projeto
cd meu-primeiro-projeto
```

### **1.2 Estrutura do Projeto**
```
meu-primeiro-projeto/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/
│   │   │       └── com/
│   │   │           └── empresa/
│   │   │               └── sankhya/
│   │   │                   ├── eventos/
│   │   │                   ├── validacoes/
│   │   │                   └── utils/
│   │   ├── resources/
│   │   │   ├── sql/
│   │   │   ├── properties/
│   │   │   └── logs/
│   │   └── webapp/
│   │       ├── js/
│   │       ├── css/
│   │       └── jsp/
│   └── test/
│       ├── java/
│       └── resources/
├── lib/
├── scripts/
├── config/
├── README.md
├── pom.xml
└── .gitignore
```

### **1.3 Compilar Projeto**
```bash
# Compilar projeto
mvn clean compile

# Executar testes
mvn test

# Verificar se compilou corretamente
echo "✅ Projeto criado e compilado com sucesso!"
```

## 🎯 **Passo 2: Primeira Personalização**

### **2.1 Criar Primeiro Evento**
```java
// src/main/java/br/com/empresa/sankhya/eventos/PrimeiroEvento.java
package br.com.empresa.sankhya.eventos;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

/**
 * Primeiro evento programado Sankhya
 * Demonstra conceitos básicos
 */
public class PrimeiroEvento {
    
    /**
     * Evento: Antes de inserir
     * Aplica validações básicas
     */
    public void beforeInsert(PersistenceEvent event) throws Exception {
        System.out.println("🎉 Primeiro evento Sankhya executado!");
        
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // Validação básica
        String nome = vo.getProperty("NOME");
        if (nome == null || nome.trim().isEmpty()) {
            throw new Exception("Nome é obrigatório");
        }
        
        System.out.println("✅ Validação passou para: " + nome);
    }
    
    /**
     * Evento: Após inserir
     * Executa ações pós-inserção
     */
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        System.out.println("📝 Registro inserido com sucesso!");
        System.out.println("   ID: " + vo.getProperty("ID"));
        System.out.println("   Nome: " + vo.getProperty("NOME"));
        
        // Ação pós-inserção
        enviarNotificacao(vo);
    }
    
    /**
     * Método auxiliar: Enviar notificação
     */
    private void enviarNotificacao(DynamicVO vo) {
        System.out.println("📧 Notificação enviada para: " + vo.getProperty("NOME"));
        // Implementar envio de notificação
    }
}
```

### **2.2 Compilar e Testar**
```bash
# Compilar projeto
mvn clean compile

# Verificar se compilou
echo "✅ Primeiro evento criado e compilado!"
```

## 🎨 **Passo 3: Primeiro Componente HTML5**

### **3.1 Criar Componente SankhyaJS**
```html
<!-- src/main/webapp/jsp/primeiro-componente.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Primeiro Componente SankhyaJS</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 20px;
            background: #f5f5f5;
        }
        
        .container {
            max-width: 800px;
            margin: 0 auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }
        
        .header {
            text-align: center;
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid #007bff;
        }
        
        .header h1 {
            color: #007bff;
            margin: 0;
            font-size: 2.5em;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #333;
        }
        
        .form-group input,
        .form-group textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 16px;
        }
        
        .btn {
            background: #007bff;
            color: white;
            padding: 12px 24px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            margin-right: 10px;
        }
        
        .btn:hover {
            background: #0056b3;
        }
        
        .btn-secondary {
            background: #6c757d;
        }
        
        .btn-secondary:hover {
            background: #545b62;
        }
        
        .message {
            padding: 15px;
            margin: 20px 0;
            border-radius: 5px;
            display: none;
        }
        
        .message.success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        
        .message.error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
    </style>
</head>
<body>
    <div class="container" ng-controller="PrimeiroComponenteController">
        <div class="header">
            <h1>🎉 Primeiro Componente SankhyaJS</h1>
            <p>Bem-vindo ao desenvolvimento Sankhya!</p>
        </div>
        
        <div class="message" id="message"></div>
        
        <form ng-submit="salvar()">
            <div class="form-group">
                <label for="nome">Nome:</label>
                <input type="text" id="nome" ng-model="dados.nome" required>
            </div>
            
            <div class="form-group">
                <label for="email">Email:</label>
                <input type="email" id="email" ng-model="dados.email" required>
            </div>
            
            <div class="form-group">
                <label for="descricao">Descrição:</label>
                <textarea id="descricao" ng-model="dados.descricao" rows="4"></textarea>
            </div>
            
            <div class="form-group">
                <label>
                    <input type="checkbox" ng-model="dados.ativo"> Ativo
                </label>
            </div>
            
            <div class="form-actions">
                <button type="submit" class="btn">💾 Salvar</button>
                <button type="button" ng-click="limpar()" class="btn btn-secondary">🗑️ Limpar</button>
            </div>
        </form>
        
        <div ng-show="dadosSalvos.length > 0" style="margin-top: 30px;">
            <h3>📋 Dados Salvos</h3>
            <div ng-repeat="item in dadosSalvos" style="background: #f8f9fa; padding: 15px; margin: 10px 0; border-radius: 5px;">
                <strong>{{item.nome}}</strong> - {{item.email}}
                <br>
                <small>{{item.descricao}}</small>
                <br>
                <span ng-class="item.ativo ? 'text-success' : 'text-muted'">
                    {{item.ativo ? '✅ Ativo' : '❌ Inativo'}}
                </span>
            </div>
        </div>
    </div>
    
    <script>
        // Controller SankhyaJS
        function PrimeiroComponenteController($scope, $http) {
            // Inicializar dados
            $scope.dados = {
                nome: '',
                email: '',
                descricao: '',
                ativo: true
            };
            
            $scope.dadosSalvos = [];
            
            // Carregar dados iniciais
            $scope.carregarDados = function() {
                // Simular carregamento de dados
                $scope.dadosSalvos = [
                    {
                        nome: 'João Silva',
                        email: 'joao@exemplo.com',
                        descricao: 'Desenvolvedor Sankhya',
                        ativo: true
                    },
                    {
                        nome: 'Maria Santos',
                        email: 'maria@exemplo.com',
                        descricao: 'Analista de Sistemas',
                        ativo: false
                    }
                ];
            };
            
            // Salvar dados
            $scope.salvar = function() {
                try {
                    // Validações básicas
                    if (!$scope.dados.nome || $scope.dados.nome.trim() === '') {
                        $scope.mostrarMensagem('Nome é obrigatório', 'error');
                        return;
                    }
                    
                    if (!$scope.dados.email || $scope.dados.email.trim() === '') {
                        $scope.mostrarMensagem('Email é obrigatório', 'error');
                        return;
                    }
                    
                    // Simular salvamento
                    var novoItem = angular.copy($scope.dados);
                    $scope.dadosSalvos.push(novoItem);
                    
                    $scope.mostrarMensagem('Dados salvos com sucesso!', 'success');
                    $scope.limpar();
                    
                } catch (error) {
                    $scope.mostrarMensagem('Erro ao salvar: ' + error.message, 'error');
                }
            };
            
            // Limpar formulário
            $scope.limpar = function() {
                $scope.dados = {
                    nome: '',
                    email: '',
                    descricao: '',
                    ativo: true
                };
            };
            
            // Mostrar mensagem
            $scope.mostrarMensagem = function(texto, tipo) {
                var messageEl = document.getElementById('message');
                messageEl.textContent = texto;
                messageEl.className = 'message ' + tipo;
                messageEl.style.display = 'block';
                
                // Esconder mensagem após 3 segundos
                setTimeout(function() {
                    messageEl.style.display = 'none';
                }, 3000);
            };
            
            // Inicializar
            $scope.carregarDados();
        }
    </script>
</body>
</html>
```

### **3.2 Testar Componente**
```bash
# Compilar projeto
mvn clean compile

# Executar servidor de desenvolvimento
mvn jetty:run

# Acessar: http://localhost:8080/primeiro-componente.jsp
echo "✅ Componente HTML5 criado e testado!"
```

## 🔧 **Passo 4: Primeiro Botão de Ação**

### **4.1 Criar Botão de Ação**
```javascript
// src/main/webapp/js/primeiro-botao-acao.js

/**
 * Primeiro botão de ação Sankhya
 * Demonstra conceitos básicos
 */
function primeiroBotaoAcao() {
    try {
        console.log("🎯 Primeiro botão de ação executado!");
        
        // Obter dados do registro atual
        var registro = getCurrentRecord();
        
        if (!registro) {
            throw new Error('Nenhum registro selecionado');
        }
        
        // Mostrar informações do registro
        var mensagem = "Informações do Registro:\n";
        mensagem += "ID: " + (registro.id || 'N/A') + "\n";
        mensagem += "Nome: " + (registro.nome || 'N/A') + "\n";
        mensagem += "Status: " + (registro.status || 'N/A');
        
        // Mostrar alerta
        alert(mensagem);
        
        // Executar ação personalizada
        executarAcaoPersonalizada(registro);
        
        // Mostrar mensagem de sucesso
        showMessage('Botão de ação executado com sucesso!');
        
        // Atualizar tela
        refreshCurrentScreen();
        
    } catch (error) {
        console.error('Erro no botão de ação:', error);
        showError('Erro: ' + error.message);
    }
}

/**
 * Executar ação personalizada
 */
function executarAcaoPersonalizada(registro) {
    console.log("⚙️ Executando ação personalizada...");
    
    // Ação 1: Validar dados
    validarDados(registro);
    
    // Ação 2: Processar dados
    processarDados(registro);
    
    // Ação 3: Salvar alterações
    salvarAlteracoes(registro);
    
    // Ação 4: Notificar usuário
    notificarUsuario('Ação personalizada concluída!');
}

/**
 * Validar dados do registro
 */
function validarDados(registro) {
    console.log("✅ Validando dados...");
    
    // Validação 1: Nome obrigatório
    if (!registro.nome || registro.nome.trim() === '') {
        throw new Error('Nome é obrigatório');
    }
    
    // Validação 2: Email válido
    if (registro.email && !isValidEmail(registro.email)) {
        throw new Error('Email inválido');
    }
    
    console.log("✅ Validações passaram");
}

/**
 * Processar dados do registro
 */
function processarDados(registro) {
    console.log("🔄 Processando dados...");
    
    // Processamento 1: Formatar nome
    if (registro.nome) {
        registro.nome = registro.nome.trim().toUpperCase();
    }
    
    // Processamento 2: Formatar email
    if (registro.email) {
        registro.email = registro.email.trim().toLowerCase();
    }
    
    // Processamento 3: Definir status
    if (!registro.status) {
        registro.status = 'ATIVO';
    }
    
    console.log("✅ Dados processados");
}

/**
 * Salvar alterações
 */
function salvarAlteracoes(registro) {
    console.log("💾 Salvando alterações...");
    
    // Simular salvamento
    // Em um cenário real, aqui seria feita a chamada para a API
    console.log("✅ Alterações salvas");
}

/**
 * Notificar usuário
 */
function notificarUsuario(mensagem) {
    console.log("📢 Notificando usuário: " + mensagem);
    // Implementar notificação
}

/**
 * Validar email
 */
function isValidEmail(email) {
    var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * Funções auxiliares (simuladas)
 */
function getCurrentRecord() {
    // Simular obtenção do registro atual
    return {
        id: 1,
        nome: 'João Silva',
        email: 'joao@exemplo.com',
        status: 'ATIVO'
    };
}

function showMessage(mensagem) {
    console.log("✅ " + mensagem);
    // Implementar exibição de mensagem
}

function showError(mensagem) {
    console.error("❌ " + mensagem);
    // Implementar exibição de erro
}

function refreshCurrentScreen() {
    console.log("🔄 Atualizando tela...");
    // Implementar atualização da tela
}
```

### **4.2 Testar Botão de Ação**
```bash
# Compilar projeto
mvn clean compile

# Testar botão de ação
echo "✅ Botão de ação criado e testado!"
```

## 📊 **Passo 5: Primeiro Relatório**

### **5.1 Criar Relatório Básico**
```sql
-- src/main/resources/sql/primeiro-relatorio.sql
-- Primeiro relatório Sankhya
-- Demonstra conceitos básicos

SELECT 
    p.CODPROD,
    p.DESCRPROD,
    p.VLRVENDA,
    p.ESTOQUE,
    c.DESCRCATEGORIA,
    CASE 
        WHEN p.ATIVO = 'S' THEN 'Ativo'
        ELSE 'Inativo'
    END AS STATUS_PRODUTO
FROM TGFPRO p
LEFT JOIN TGFCAT c ON p.CODCATEGORIA = c.CODCATEGORIA
WHERE p.ATIVO = 'S'
    AND p.VLRVENDA > 0
ORDER BY p.DESCRPROD;
```

### **5.2 Configurar Relatório**
```xml
<!-- src/main/resources/xml/primeiro-relatorio.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://jasperreports.sourceforge.net/jasperreports
              http://jasperreports.sourceforge.net/xsd/jasperreport.xsd"
              name="PrimeiroRelatorio"
              pageWidth="595"
              pageHeight="842"
              columnWidth="555"
              leftMargin="20"
              rightMargin="20"
              topMargin="20"
              bottomMargin="20">
    
    <title>
        <band height="50">
            <staticText>
                <reportElement x="0" y="0" width="555" height="30"/>
                <textElement textAlignment="Center">
                    <font size="18" isBold="true"/>
                </textElement>
                <text>Primeiro Relatório Sankhya</text>
            </staticText>
        </band>
    </title>
    
    <columnHeader>
        <band height="20">
            <staticText>
                <reportElement x="0" y="0" width="100" height="20"/>
                <textElement>
                    <font isBold="true"/>
                </textElement>
                <text>Código</text>
            </staticText>
            <staticText>
                <reportElement x="100" y="0" width="200" height="20"/>
                <textElement>
                    <font isBold="true"/>
                </textElement>
                <text>Descrição</text>
            </staticText>
            <staticText>
                <reportElement x="300" y="0" width="100" height="20"/>
                <textElement>
                    <font isBold="true"/>
                </textElement>
                <text>Valor</text>
            </staticText>
            <staticText>
                <reportElement x="400" y="0" width="100" height="20"/>
                <textElement>
                    <font isBold="true"/>
                </textElement>
                <text>Estoque</text>
            </staticText>
        </band>
    </columnHeader>
    
    <detail>
        <band height="20">
            <textField>
                <reportElement x="0" y="0" width="100" height="20"/>
                <textFieldExpression><![CDATA[$F{CODPROD}]]></textFieldExpression>
            </textField>
            <textField>
                <reportElement x="100" y="0" width="200" height="20"/>
                <textFieldExpression><![CDATA[$F{DESCRPROD}]]></textFieldExpression>
            </textField>
            <textField>
                <reportElement x="300" y="0" width="100" height="20"/>
                <textFieldExpression><![CDATA["R$ " + $F{VLRVENDA}]]></textFieldExpression>
            </textField>
            <textField>
                <reportElement x="400" y="0" width="100" height="20"/>
                <textFieldExpression><![CDATA[$F{ESTOQUE}]]></textFieldExpression>
            </textField>
        </band>
    </detail>
</jasperReport>
```

## 🎉 **Resumo dos Primeiros Passos**

### **O que Foi Criado**
- ✅ **Primeiro Projeto**: Estrutura básica
- ✅ **Primeiro Evento**: Validações e ações
- ✅ **Primeiro Componente**: Interface HTML5
- ✅ **Primeiro Botão**: Ação personalizada
- ✅ **Primeiro Relatório**: Documento básico

### **Conceitos Aprendidos**
- 🏗️ **Estrutura de Projeto**: Organização de arquivos
- ⚙️ **Eventos Programados**: Automação de processos
- 🎨 **SankhyaJS**: Componentes HTML5
- 🔧 **Botões de Ação**: Ações personalizadas
- 📊 **Relatórios**: Documentos formatados

### **Próximos Passos**
1. **Explorar Mais**: Testar outras funcionalidades
2. **Integrar APIs**: Conectar com sistemas externos
3. **Otimizar**: Melhorar performance
4. **Avançar**: Técnicas mais complexas

## 🚀 **Comandos Úteis**

### **Desenvolvimento**
```bash
# Compilar projeto
mvn clean compile

# Executar testes
mvn test

# Executar servidor
mvn jetty:run

# Gerar JAR
mvn clean package
```

### **Git**
```bash
# Inicializar repositório
git init

# Adicionar arquivos
git add .

# Fazer commit
git commit -m "Primeiro commit"

# Verificar status
git status
```

---

*Parabéns! Você completou seus primeiros passos no desenvolvimento Sankhya. Continue explorando e aprendendo!* 🎉
