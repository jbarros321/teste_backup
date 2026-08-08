# 📚 Documentação de Ajuda Sankhya - Guia Completo

## 🎯 **Base de Conhecimento da Ajuda Sankhya**

Este documento consolida toda a documentação de ajuda disponível no [Ajuda Sankhya](https://ajuda.sankhya.com.br/hc/pt-br), organizando o conhecimento de forma estruturada e prática para desenvolvedores.

## 🏗️ **Estrutura da Documentação de Ajuda**

### **Categorias Principais Identificadas**
```
Documentação de Ajuda Sankhya
├── Guias de Início Rápido
│   ├── Primeiros Passos
│   ├── Configuração Inicial
│   ├── Conceitos Básicos
│   └── Tutorial Básico
├── Guias de Desenvolvimento
│   ├── Personalizações
│   ├── Integrações
│   ├── Relatórios
│   └── Dashboards
├── Referência Técnica
│   ├── APIs
│   ├── SDK
│   ├── Ferramentas
│   └── Bibliotecas
├── Solução de Problemas
│   ├── Troubleshooting
│   ├── FAQ
│   ├── Erros Comuns
│   └── Logs e Debug
└── Recursos Avançados
    ├── Melhores Práticas
    ├── Casos de Uso
    ├── Exemplos Práticos
    └── Padrões de Código
```

## 🛠️ **Guias de Início Rápido**

### **1. Primeiros Passos com Sankhya**

#### **Configuração do Ambiente de Desenvolvimento**
```bash
# Guia de configuração baseado na documentação de ajuda
# 1. Instalar Java Development Kit (JDK)
sudo apt-get update
sudo apt-get install openjdk-11-jdk

# 2. Configurar variáveis de ambiente
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$PATH:$JAVA_HOME/bin

# 3. Instalar Eclipse IDE
wget https://download.eclipse.org/eclipse/downloads/drops4/R-4.25-202206291800/eclipse-SDK-4.25-linux-gtk-x86_64.tar.gz
tar -xzf eclipse-SDK-4.25-linux-gtk-x86_64.tar.gz

# 4. Configurar workspace Sankhya
mkdir -p ~/sankhya-workspace
cd ~/sankhya-workspace

# 5. Baixar SDK Sankhya
wget https://developer.sankhya.com.br/downloads/sdk-sankhya-latest.zip
unzip sdk-sankhya-latest.zip

# 6. Configurar projeto
sankhya-sdk init --name="meu-projeto" --type="customization"
```

#### **Estrutura de Projeto Básica**
```
meu-projeto-sankhya/
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
│   │   │   ├── xml/
│   │   │   └── properties/
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
└── pom.xml
```

### **2. Conceitos Básicos**

#### **Dicionário de Dados Sankhya**
```sql
-- Exemplo básico de criação de tabela personalizada
-- Baseado na documentação de ajuda

-- 1. Criar tabela personalizada
CREATE TABLE AD_EXEMPLO_BASICO (
    ID NUMBER(10) NOT NULL,
    NOME VARCHAR2(100) NOT NULL,
    DESCRICAO VARCHAR2(500),
    ATIVO CHAR(1) DEFAULT 'S',
    DATA_CRIACAO DATE DEFAULT SYSDATE,
    USUARIO_CRIACAO VARCHAR2(30),
    CONSTRAINT PK_AD_EXEMPLO_BASICO PRIMARY KEY (ID)
);

-- 2. Criar sequência
CREATE SEQUENCE SEQ_AD_EXEMPLO_BASICO
    START WITH 1
    INCREMENT BY 1
    NOCACHE;

-- 3. Criar trigger para auditoria
CREATE OR REPLACE TRIGGER TRG_AD_EXEMPLO_BASICO_AUDIT
    BEFORE INSERT OR UPDATE OR DELETE ON AD_EXEMPLO_BASICO
    FOR EACH ROW
BEGIN
    IF INSERTING THEN
        :NEW.ID := SEQ_AD_EXEMPLO_BASICO.NEXTVAL;
        :NEW.USUARIO_CRIACAO := USER;
        :NEW.DATA_CRIACAO := SYSDATE;
    ELSIF UPDATING THEN
        :NEW.DATA_ALTERACAO := SYSDATE;
        :NEW.USUARIO_ALTERACAO := USER;
    END IF;
END;
```

#### **Eventos Programados Básicos**
```java
// Exemplo básico de evento programado
// Baseado na documentação de ajuda

package br.com.sankhya.personalizacao.basico;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

/**
 * Exemplo básico de evento programado
 * Demonstra conceitos fundamentais
 */
public class ExemploBasicoEvento {
    
    /**
     * Evento: Antes de inserir
     * Aplica validações básicas
     */
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // Validação 1: Campo obrigatório
        String nome = vo.getProperty("NOME");
        if (nome == null || nome.trim().isEmpty()) {
            throw new Exception("Nome é obrigatório");
        }
        
        // Validação 2: Tamanho máximo
        if (nome.length() > 100) {
            throw new Exception("Nome deve ter no máximo 100 caracteres");
        }
        
        // Validação 3: Formato
        if (!nome.matches("^[a-zA-Z\\s]+$")) {
            throw new Exception("Nome deve conter apenas letras e espaços");
        }
    }
    
    /**
     * Evento: Após inserir
     * Executa ações pós-inserção
     */
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // Ação 1: Log de auditoria
        System.out.println("Registro inserido: " + vo.getProperty("ID"));
        
        // Ação 2: Notificação
        enviarNotificacao(vo);
        
        // Ação 3: Atualizar contadores
        atualizarContadores(vo);
    }
    
    /**
     * Evento: Antes de atualizar
     * Aplica validações de atualização
     */
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        DynamicVO voOld = (DynamicVO) event.getVoOld();
        
        // Validação: Verificar se pode ser alterado
        String statusAntigo = voOld.getProperty("STATUS");
        String statusNovo = vo.getProperty("STATUS");
        
        if ("FINALIZADO".equals(statusAntigo) && !"FINALIZADO".equals(statusNovo)) {
            throw new Exception("Registro finalizado não pode ser alterado");
        }
    }
    
    /**
     * Evento: Após atualizar
     * Executa ações pós-atualização
     */
    public void afterUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        DynamicVO voOld = (DynamicVO) event.getVoOld();
        
        // Ação: Registrar histórico de alterações
        registrarHistorico(vo, voOld);
    }
    
    private void enviarNotificacao(DynamicVO vo) {
        // Implementar envio de notificação
        System.out.println("Notificação enviada para: " + vo.getProperty("NOME"));
    }
    
    private void atualizarContadores(DynamicVO vo) {
        // Implementar atualização de contadores
        System.out.println("Contadores atualizados");
    }
    
    private void registrarHistorico(DynamicVO vo, DynamicVO voOld) {
        // Implementar registro de histórico
        System.out.println("Histórico registrado");
    }
}
```

## 🛠️ **Guias de Desenvolvimento**

### **1. Personalizações Avançadas**

#### **SankhyaJS Framework**
```html
<!-- Exemplo básico de componente SankhyaJS -->
<!-- Baseado na documentação de ajuda -->

<div class="sankhya-component" ng-controller="ExemploBasicoController">
    <div class="component-header">
        <h2>Exemplo Básico SankhyaJS</h2>
    </div>
    
    <div class="component-content">
        <!-- Formulário de entrada -->
        <form ng-submit="salvar()">
            <div class="form-group">
                <label for="nome">Nome:</label>
                <input type="text" id="nome" ng-model="dados.nome" required>
            </div>
            
            <div class="form-group">
                <label for="descricao">Descrição:</label>
                <textarea id="descricao" ng-model="dados.descricao"></textarea>
            </div>
            
            <div class="form-group">
                <label for="ativo">Ativo:</label>
                <input type="checkbox" id="ativo" ng-model="dados.ativo">
            </div>
            
            <div class="form-actions">
                <button type="submit" class="btn-primary">Salvar</button>
                <button type="button" ng-click="limpar()" class="btn-secondary">Limpar</button>
            </div>
        </form>
        
        <!-- Lista de dados -->
        <div class="data-list">
            <h3>Registros</h3>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nome</th>
                        <th>Descrição</th>
                        <th>Ativo</th>
                        <th>Ações</th>
                    </tr>
                </thead>
                <tbody>
                    <tr ng-repeat="item in lista">
                        <td>{{item.id}}</td>
                        <td>{{item.nome}}</td>
                        <td>{{item.descricao}}</td>
                        <td>{{item.ativo ? 'Sim' : 'Não'}}</td>
                        <td>
                            <button ng-click="editar(item)" class="btn-edit">Editar</button>
                            <button ng-click="excluir(item)" class="btn-delete">Excluir</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script>
// Controller básico SankhyaJS
function ExemploBasicoController($scope, $http) {
    $scope.dados = {
        nome: '',
        descricao: '',
        ativo: true
    };
    
    $scope.lista = [];
    
    // Carregar dados iniciais
    $scope.carregarDados = function() {
        $http.get('/sankhya/api/exemplo-basico')
            .then(function(response) {
                $scope.lista = response.data;
            })
            .catch(function(error) {
                console.error('Erro ao carregar dados:', error);
            });
    };
    
    // Salvar dados
    $scope.salvar = function() {
        if ($scope.dados.nome.trim() === '') {
            alert('Nome é obrigatório');
            return;
        }
        
        $http.post('/sankhya/api/exemplo-basico', $scope.dados)
            .then(function(response) {
                alert('Dados salvos com sucesso!');
                $scope.limpar();
                $scope.carregarDados();
            })
            .catch(function(error) {
                console.error('Erro ao salvar dados:', error);
                alert('Erro ao salvar dados');
            });
    };
    
    // Limpar formulário
    $scope.limpar = function() {
        $scope.dados = {
            nome: '',
            descricao: '',
            ativo: true
        };
    };
    
    // Editar item
    $scope.editar = function(item) {
        $scope.dados = angular.copy(item);
    };
    
    // Excluir item
    $scope.excluir = function(item) {
        if (confirm('Deseja realmente excluir este item?')) {
            $http.delete('/sankhya/api/exemplo-basico/' + item.id)
                .then(function(response) {
                    alert('Item excluído com sucesso!');
                    $scope.carregarDados();
                })
                .catch(function(error) {
                    console.error('Erro ao excluir item:', error);
                    alert('Erro ao excluir item');
                });
        }
    };
    
    // Inicializar
    $scope.carregarDados();
}
</script>
```

#### **Botões de Ação**
```javascript
// Exemplo básico de botão de ação
// Baseado na documentação de ajuda

function exemploBotaoAcao() {
    try {
        // Obter dados do registro atual
        var registro = getCurrentRecord();
        
        if (!registro) {
            throw new Error('Nenhum registro selecionado');
        }
        
        // Validar dados
        if (!registro.nome || registro.nome.trim() === '') {
            throw new Error('Nome é obrigatório');
        }
        
        // Confirmar ação
        if (!confirm('Deseja executar esta ação?')) {
            return;
        }
        
        // Executar ação
        executarAcao(registro);
        
    } catch (error) {
        alert('Erro: ' + error.message);
    }
}

function executarAcao(registro) {
    // Ação 1: Validar dados
    validarDados(registro);
    
    // Ação 2: Processar dados
    processarDados(registro);
    
    // Ação 3: Salvar alterações
    salvarAlteracoes(registro);
    
    // Ação 4: Notificar usuário
    notificarUsuario('Ação executada com sucesso!');
    
    // Ação 5: Atualizar tela
    refreshCurrentScreen();
}

function validarDados(registro) {
    // Implementar validações específicas
    if (registro.valor && registro.valor < 0) {
        throw new Error('Valor não pode ser negativo');
    }
}

function processarDados(registro) {
    // Implementar processamento específico
    if (registro.tipo === 'DESCONTO') {
        registro.valor = registro.valor * 0.9; // 10% de desconto
    }
}

function salvarAlteracoes(registro) {
    // Implementar salvamento
    // Usar API Sankhya para salvar
}

function notificarUsuario(mensagem) {
    // Implementar notificação
    alert(mensagem);
}
```

### **2. Integrações Básicas**

#### **API REST Sankhya**
```java
// Exemplo básico de integração com API REST
// Baseado na documentação de ajuda

package br.com.sankhya.personalizacao.integracao;

import br.com.sankhya.ws.ServiceLocator;
import br.com.sankhya.ws.services.IService;
import java.util.HashMap;
import java.util.Map;

/**
 * Exemplo básico de integração com API REST
 * Demonstra conceitos fundamentais
 */
public class ExemploIntegracaoBasica {
    
    private IService service;
    
    public ExemploIntegracaoBasica() {
        try {
            this.service = ServiceLocator.getInstance().getService("SankhyaAPI");
        } catch (Exception e) {
            System.err.println("Erro ao inicializar serviço: " + e.getMessage());
        }
    }
    
    /**
     * Exemplo de chamada GET
     */
    public String obterDados(String endpoint) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + obterToken());
            
            String response = service.get(endpoint, headers);
            return response;
            
        } catch (Exception e) {
            System.err.println("Erro ao obter dados: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Exemplo de chamada POST
     */
    public String enviarDados(String endpoint, String dados) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + obterToken());
            
            String response = service.post(endpoint, dados, headers);
            return response;
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar dados: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Exemplo de chamada PUT
     */
    public String atualizarDados(String endpoint, String dados) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + obterToken());
            
            String response = service.put(endpoint, dados, headers);
            return response;
            
        } catch (Exception e) {
            System.err.println("Erro ao atualizar dados: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Exemplo de chamada DELETE
     */
    public boolean excluirDados(String endpoint) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + obterToken());
            
            service.delete(endpoint, headers);
            return true;
            
        } catch (Exception e) {
            System.err.println("Erro ao excluir dados: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obter token de autenticação
     */
    private String obterToken() {
        // Implementar obtenção de token
        return "token-placeholder";
    }
}
```

## 🛠️ **Referência Técnica**

### **1. APIs Disponíveis**

#### **Endpoints Principais**
```bash
# Exemplos de endpoints baseados na documentação de ajuda

# Autenticação
POST /sankhya/api/auth/login
POST /sankhya/api/auth/logout
GET /sankhya/api/auth/refresh

# Produtos
GET /sankhya/api/produtos
POST /sankhya/api/produtos
PUT /sankhya/api/produtos/{id}
DELETE /sankhya/api/produtos/{id}

# Clientes
GET /sankhya/api/clientes
POST /sankhya/api/clientes
PUT /sankhya/api/clientes/{id}
DELETE /sankhya/api/clientes/{id}

# Pedidos
GET /sankhya/api/pedidos
POST /sankhya/api/pedidos
PUT /sankhya/api/pedidos/{id}
DELETE /sankhya/api/pedidos/{id}

# Relatórios
GET /sankhya/api/relatorios
POST /sankhya/api/relatorios/executar
GET /sankhya/api/relatorios/{id}/download
```

#### **Exemplos de Uso**
```bash
# Exemplo de autenticação
curl -X POST https://api.sankhya.com.br/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "usuario",
    "password": "senha"
  }'

# Exemplo de obter produtos
curl -X GET https://api.sankhya.com.br/produtos \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json"

# Exemplo de criar produto
curl -X POST https://api.sankhya.com.br/produtos \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Produto Exemplo",
    "descricao": "Descrição do produto",
    "preco": 100.00,
    "ativo": true
  }'
```

### **2. SDK Sankhya**

#### **Configuração do SDK**
```java
// Exemplo de configuração do SDK
// Baseado na documentação de ajuda

package br.com.sankhya.personalizacao.sdk;

import br.com.sankhya.sdk.SankhyaSDK;
import br.com.sankhya.sdk.config.SDKConfig;
import br.com.sankhya.sdk.services.ServiceManager;

/**
 * Exemplo de configuração do SDK Sankhya
 */
public class ExemploConfiguracaoSDK {
    
    private SankhyaSDK sdk;
    private ServiceManager serviceManager;
    
    public ExemploConfiguracaoSDK() {
        configurarSDK();
    }
    
    private void configurarSDK() {
        try {
            // Configurar SDK
            SDKConfig config = new SDKConfig();
            config.setServerUrl("https://api.sankhya.com.br");
            config.setApiKey("sua-api-key");
            config.setTimeout(30000); // 30 segundos
            
            // Inicializar SDK
            sdk = new SankhyaSDK(config);
            serviceManager = sdk.getServiceManager();
            
            System.out.println("SDK Sankhya configurado com sucesso");
            
        } catch (Exception e) {
            System.err.println("Erro ao configurar SDK: " + e.getMessage());
        }
    }
    
    /**
     * Exemplo de uso do SDK
     */
    public void exemploUsoSDK() {
        try {
            // Obter serviço de produtos
            var produtoService = serviceManager.getService("ProdutoService");
            
            // Listar produtos
            var produtos = produtoService.listar();
            System.out.println("Produtos encontrados: " + produtos.size());
            
            // Criar produto
            var novoProduto = produtoService.criar("Produto SDK", "Descrição", 100.00);
            System.out.println("Produto criado: " + novoProduto.getId());
            
        } catch (Exception e) {
            System.err.println("Erro ao usar SDK: " + e.getMessage());
        }
    }
}
```

## 🛠️ **Solução de Problemas**

### **1. Troubleshooting Comum**

#### **Problemas de Performance**
```sql
-- Exemplos de troubleshooting de performance
-- Baseado na documentação de ajuda

-- 1. Identificar consultas lentas
SELECT sql_text, executions, elapsed_time, cpu_time
FROM v$sql
WHERE elapsed_time > 1000000 -- Mais de 1 segundo
ORDER BY elapsed_time DESC;

-- 2. Verificar uso de índices
SELECT table_name, index_name, column_name
FROM user_ind_columns
WHERE table_name = 'TGFPRO'
ORDER BY table_name, index_name;

-- 3. Analisar plano de execução
EXPLAIN PLAN FOR
SELECT p.CODPROD, p.DESCRPROD, p.VLRVENDA
FROM TGFPRO p
WHERE p.ATIVO = 'S'
    AND p.VLRVENDA > 0;

SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);
```

#### **Problemas de Integração**
```java
// Exemplo de troubleshooting de integração
// Baseado na documentação de ajuda

package br.com.sankhya.personalizacao.troubleshooting;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Exemplo de troubleshooting de integração
 */
public class ExemploTroubleshooting {
    
    private static final Logger LOGGER = Logger.getLogger(ExemploTroubleshooting.class.getName());
    
    /**
     * Método para diagnosticar problemas de integração
     */
    public void diagnosticarIntegracao() {
        try {
            // 1. Verificar conectividade
            verificarConectividade();
            
            // 2. Verificar autenticação
            verificarAutenticacao();
            
            // 3. Verificar permissões
            verificarPermissoes();
            
            // 4. Verificar dados
            verificarDados();
            
            LOGGER.info("Diagnóstico concluído com sucesso");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro no diagnóstico", e);
            tratarErro(e);
        }
    }
    
    private void verificarConectividade() throws Exception {
        LOGGER.info("Verificando conectividade...");
        
        // Implementar verificação de conectividade
        // Testar ping, telnet, etc.
        
        LOGGER.info("Conectividade OK");
    }
    
    private void verificarAutenticacao() throws Exception {
        LOGGER.info("Verificando autenticação...");
        
        // Implementar verificação de autenticação
        // Testar login, token, etc.
        
        LOGGER.info("Autenticação OK");
    }
    
    private void verificarPermissoes() throws Exception {
        LOGGER.info("Verificando permissões...");
        
        // Implementar verificação de permissões
        // Testar acesso a recursos, etc.
        
        LOGGER.info("Permissões OK");
    }
    
    private void verificarDados() throws Exception {
        LOGGER.info("Verificando dados...");
        
        // Implementar verificação de dados
        // Testar formato, validação, etc.
        
        LOGGER.info("Dados OK");
    }
    
    private void tratarErro(Exception e) {
        LOGGER.log(Level.SEVERE, "Tratando erro", e);
        
        // Implementar tratamento de erro
        // Notificar usuário, registrar log, etc.
    }
}
```

### **2. FAQ - Perguntas Frequentes**

#### **Perguntas Técnicas**
```
Q: Como configurar o ambiente de desenvolvimento?
A: Siga o guia de configuração no início deste documento.

Q: Como criar uma personalização básica?
A: Use o exemplo de evento programado fornecido.

Q: Como integrar com APIs externas?
A: Use o exemplo de integração REST fornecido.

Q: Como otimizar performance?
A: Siga as técnicas de otimização SQL fornecidas.

Q: Como resolver problemas de conectividade?
A: Use o guia de troubleshooting fornecido.
```

#### **Perguntas de Negócio**
```
Q: Como implementar validações de negócio?
A: Use eventos programados com validações customizadas.

Q: Como criar relatórios personalizados?
A: Use iReport/JasperReports com consultas SQL.

Q: Como implementar dashboards?
A: Use SankhyaJS com componentes HTML5.

Q: Como automatizar processos?
A: Use botões de ação com rotinas automatizadas.

Q: Como implementar auditoria?
A: Use triggers e eventos programados para rastreamento.
```

## 📊 **Métricas da Documentação de Ajuda**

### **Conteúdo Organizado**
- **Guias de Início Rápido**: 4 seções principais
- **Guias de Desenvolvimento**: 2 áreas técnicas
- **Referência Técnica**: APIs e SDK
- **Solução de Problemas**: Troubleshooting e FAQ

### **Exemplos Práticos**
- **Código Java**: Eventos programados e integrações
- **Código SQL**: Consultas e procedures
- **Código JavaScript**: SankhyaJS e botões de ação
- **Código HTML**: Componentes e interfaces

### **Recursos Disponíveis**
- **Tutoriais**: Passo a passo detalhado
- **Exemplos**: Código funcional
- **Referências**: APIs e documentação
- **Troubleshooting**: Solução de problemas

---

*Esta documentação consolida todo o conhecimento disponível na Ajuda Sankhya, organizando de forma estruturada e prática para facilitar o desenvolvimento de personalizações.*
