# 🔗 Endpoints da API Sankhya - Guia Completo

## 🎯 Visão Geral

A API Sankhya oferece mais de **100 endpoints** para integração com diferentes módulos do sistema. Cada endpoint possui exemplos de código em múltiplas linguagens de programação.

## 📋 Categorias de Endpoints

### **1. Autenticação**
- **POST /login**: Autenticação de usuário
- **POST /refresh**: Renovação de token
- **POST /logout**: Logout de usuário

### **2. Produtos**
- **GET /icmsprodutos**: Listar produtos com ICMS
- **GET /icmsgrupoprodutos**: Listar grupos de produtos com ICMS
- **POST /produto**: Criar produto
- **GET /produto**: Obter produto
- **GET /produtoempresa**: Obter produto por empresa

### **3. Clientes e Parceiros**
- **GET /icmsgrupoparceiros**: Listar grupos de parceiros com ICMS
- **POST /contatocliente**: Criar contato de cliente
- **GET /contatocliente**: Obter contato de cliente
- **GET /creditocliente**: Obter crédito de cliente
- **POST /cliente**: Criar cliente
- **GET /cliente**: Obter cliente
- **PUT /cliente**: Atualizar cliente

### **4. Pedidos**
- **POST /pedidos**: Criar pedido
- **GET /pedidos**: Obter pedidos
- **POST /incaltitempedido**: Incluir/alterar item de pedido
- **POST /excaltitempedido**: Excluir item de pedido
- **GET /statuspedidos**: Obter status de pedidos
- **POST /cancelamentopedidos**: Cancelar pedidos
- **POST /faturamentopedidos**: Faturar pedidos

### **5. Financeiro**
- **GET /financeiros**: Obter títulos financeiros
- **POST /financeiroreceitainclusao**: Incluir receita
- **POST /financeiroreceitaatualizar**: Atualizar receita
- **POST /financeiroreceitasbaixa**: Dar baixa em receita
- **POST /financeirodespesainclusao**: Incluir despesa
- **POST /financeirodespesaatualizar**: Atualizar despesa
- **POST /financeirodespesabaixa**: Dar baixa em despesa

## 🛠️ Exemplos de Código por Endpoint

### **1. Autenticação - POST /login**

#### **JavaScript (Node.js)**
```javascript
const axios = require('axios');

async function login(username, password) {
    try {
        const response = await axios.post('https://api.sankhya.com.br/login', {
            username: username,
            password: password
        });
        
        return response.data.token;
    } catch (error) {
        console.error('Erro no login:', error.response.data);
        throw error;
    }
}

// Uso
login('usuario', 'senha')
    .then(token => console.log('Token:', token))
    .catch(error => console.error('Erro:', error));
```

#### **Python**
```python
import requests
import json

def login(username, password):
    try:
        response = requests.post('https://api.sankhya.com.br/login', 
            json={
                'username': username,
                'password': password
            }
        )
        
        response.raise_for_status()
        return response.json()['token']
        
    except requests.exceptions.RequestException as e:
        print(f'Erro no login: {e}')
        raise

# Uso
token = login('usuario', 'senha')
print(f'Token: {token}')
```

#### **Java**
```java
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class SankhyaLogin {
    
    public String login(String username, String password) {
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.sankhya.com.br/login", 
                entity, 
                Map.class
            );
            
            return (String) response.getBody().get("token");
            
        } catch (Exception e) {
            System.err.println("Erro no login: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
```

### **2. Produtos - GET /produto**

#### **JavaScript (Node.js)**
```javascript
async function obterProduto(codigo, token) {
    try {
        const response = await axios.get(`https://api.sankhya.com.br/produto`, {
            headers: {
                'Authorization': `Bearer ${token}`
            },
            params: {
                codigo: codigo
            }
        });
        
        return response.data;
    } catch (error) {
        console.error('Erro ao obter produto:', error.response.data);
        throw error;
    }
}

// Uso
obterProduto('PROD001', token)
    .then(produto => console.log('Produto:', produto))
    .catch(error => console.error('Erro:', error));
```

#### **Python**
```python
def obter_produto(codigo, token):
    try:
        headers = {
            'Authorization': f'Bearer {token}'
        }
        
        params = {
            'codigo': codigo
        }
        
        response = requests.get('https://api.sankhya.com.br/produto',
            headers=headers,
            params=params
        )
        
        response.raise_for_status()
        return response.json()
        
    except requests.exceptions.RequestException as e:
        print(f'Erro ao obter produto: {e}')
        raise

# Uso
produto = obter_produto('PROD001', token)
print(f'Produto: {produto}')
```

#### **Java**
```java
public Produto obterProduto(String codigo, String token) {
    RestTemplate restTemplate = new RestTemplate();
    
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    
    HttpEntity<String> entity = new HttpEntity<>(headers);
    
    try {
        ResponseEntity<Produto> response = restTemplate.exchange(
            "https://api.sankhya.com.br/produto?codigo=" + codigo,
            HttpMethod.GET,
            entity,
            Produto.class
        );
        
        return response.getBody();
        
    } catch (Exception e) {
        System.err.println("Erro ao obter produto: " + e.getMessage());
        throw new RuntimeException(e);
    }
}
```

### **3. Clientes - POST /cliente**

#### **JavaScript (Node.js)**
```javascript
async function criarCliente(dadosCliente, token) {
    try {
        const response = await axios.post('https://api.sankhya.com.br/cliente', dadosCliente, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        
        return response.data;
    } catch (error) {
        console.error('Erro ao criar cliente:', error.response.data);
        throw error;
    }
}

// Uso
const cliente = {
    nome: 'João Silva',
    email: 'joao@email.com',
    telefone: '(11) 99999-9999',
    endereco: {
        logradouro: 'Rua A',
        numero: '123',
        cidade: 'São Paulo',
        uf: 'SP',
        cep: '01234-567'
    }
};

criarCliente(cliente, token)
    .then(resultado => console.log('Cliente criado:', resultado))
    .catch(error => console.error('Erro:', error));
```

#### **Python**
```python
def criar_cliente(dados_cliente, token):
    try:
        headers = {
            'Authorization': f'Bearer {token}',
            'Content-Type': 'application/json'
        }
        
        response = requests.post('https://api.sankhya.com.br/cliente',
            headers=headers,
            json=dados_cliente
        )
        
        response.raise_for_status()
        return response.json()
        
    except requests.exceptions.RequestException as e:
        print(f'Erro ao criar cliente: {e}')
        raise

# Uso
cliente = {
    'nome': 'João Silva',
    'email': 'joao@email.com',
    'telefone': '(11) 99999-9999',
    'endereco': {
        'logradouro': 'Rua A',
        'numero': '123',
        'cidade': 'São Paulo',
        'uf': 'SP',
        'cep': '01234-567'
    }
}

resultado = criar_cliente(cliente, token)
print(f'Cliente criado: {resultado}')
```

#### **Java**
```java
public Cliente criarCliente(Cliente cliente, String token) {
    RestTemplate restTemplate = new RestTemplate();
    
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    
    HttpEntity<Cliente> entity = new HttpEntity<>(cliente, headers);
    
    try {
        ResponseEntity<Cliente> response = restTemplate.postForEntity(
            "https://api.sankhya.com.br/cliente",
            entity,
            Cliente.class
        );
        
        return response.getBody();
        
    } catch (Exception e) {
        System.err.println("Erro ao criar cliente: " + e.getMessage());
        throw new RuntimeException(e);
    }
}
```

### **4. Pedidos - POST /pedidos**

#### **JavaScript (Node.js)**
```javascript
async function criarPedido(dadosPedido, token) {
    try {
        const response = await axios.post('https://api.sankhya.com.br/pedidos', dadosPedido, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        
        return response.data;
    } catch (error) {
        console.error('Erro ao criar pedido:', error.response.data);
        throw error;
    }
}

// Uso
const pedido = {
    cliente: 'CLI001',
    vendedor: 'VEN001',
    itens: [
        {
            produto: 'PROD001',
            quantidade: 2,
            preco: 100.00
        },
        {
            produto: 'PROD002',
            quantidade: 1,
            preco: 50.00
        }
    ],
    observacoes: 'Pedido urgente'
};

criarPedido(pedido, token)
    .then(resultado => console.log('Pedido criado:', resultado))
    .catch(error => console.error('Erro:', error));
```

#### **Python**
```python
def criar_pedido(dados_pedido, token):
    try:
        headers = {
            'Authorization': f'Bearer {token}',
            'Content-Type': 'application/json'
        }
        
        response = requests.post('https://api.sankhya.com.br/pedidos',
            headers=headers,
            json=dados_pedido
        )
        
        response.raise_for_status()
        return response.json()
        
    except requests.exceptions.RequestException as e:
        print(f'Erro ao criar pedido: {e}')
        raise

# Uso
pedido = {
    'cliente': 'CLI001',
    'vendedor': 'VEN001',
    'itens': [
        {
            'produto': 'PROD001',
            'quantidade': 2,
            'preco': 100.00
        },
        {
            'produto': 'PROD002',
            'quantidade': 1,
            'preco': 50.00
        }
    ],
    'observacoes': 'Pedido urgente'
}

resultado = criar_pedido(pedido, token)
print(f'Pedido criado: {resultado}')
```

#### **Java**
```java
public Pedido criarPedido(Pedido pedido, String token) {
    RestTemplate restTemplate = new RestTemplate();
    
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    
    HttpEntity<Pedido> entity = new HttpEntity<>(pedido, headers);
    
    try {
        ResponseEntity<Pedido> response = restTemplate.postForEntity(
            "https://api.sankhya.com.br/pedidos",
            entity,
            Pedido.class
        );
        
        return response.getBody();
        
    } catch (Exception e) {
        System.err.println("Erro ao criar pedido: " + e.getMessage());
        throw new RuntimeException(e);
    }
}
```

### **5. Financeiro - GET /financeiros**

#### **JavaScript (Node.js)**
```javascript
async function obterFinanceiros(filtros, token) {
    try {
        const response = await axios.get('https://api.sankhya.com.br/financeiros', {
            headers: {
                'Authorization': `Bearer ${token}`
            },
            params: filtros
        });
        
        return response.data;
    } catch (error) {
        console.error('Erro ao obter financeiros:', error.response.data);
        throw error;
    }
}

// Uso
const filtros = {
    dataInicio: '2024-01-01',
    dataFim: '2024-12-31',
    status: 'pendente'
};

obterFinanceiros(filtros, token)
    .then(financeiros => console.log('Financeiros:', financeiros))
    .catch(error => console.error('Erro:', error));
```

#### **Python**
```python
def obter_financeiros(filtros, token):
    try:
        headers = {
            'Authorization': f'Bearer {token}'
        }
        
        response = requests.get('https://api.sankhya.com.br/financeiros',
            headers=headers,
            params=filtros
        )
        
        response.raise_for_status()
        return response.json()
        
    except requests.exceptions.RequestException as e:
        print(f'Erro ao obter financeiros: {e}')
        raise

# Uso
filtros = {
    'dataInicio': '2024-01-01',
    'dataFim': '2024-12-31',
    'status': 'pendente'
}

financeiros = obter_financeiros(filtros, token)
print(f'Financeiros: {financeiros}')
```

#### **Java**
```java
public List<Financeiro> obterFinanceiros(Map<String, String> filtros, String token) {
    RestTemplate restTemplate = new RestTemplate();
    
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    
    HttpEntity<String> entity = new HttpEntity<>(headers);
    
    try {
        StringBuilder url = new StringBuilder("https://api.sankhya.com.br/financeiros?");
        for (Map.Entry<String, String> entry : filtros.entrySet()) {
            url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        
        ResponseEntity<FinanceiroResponse> response = restTemplate.exchange(
            url.toString(),
            HttpMethod.GET,
            entity,
            FinanceiroResponse.class
        );
        
        return response.getBody().getData();
        
    } catch (Exception e) {
        System.err.println("Erro ao obter financeiros: " + e.getMessage());
        throw new RuntimeException(e);
    }
}
```

## 🔧 Códigos de Retorno

### **Códigos de Sucesso**
- **200 OK**: Requisição bem-sucedida
- **201 Created**: Recurso criado com sucesso
- **202 Accepted**: Requisição aceita para processamento
- **204 No Content**: Requisição bem-sucedida sem conteúdo

### **Códigos de Erro do Cliente**
- **400 Bad Request**: Requisição inválida
- **401 Unauthorized**: Não autorizado
- **403 Forbidden**: Acesso negado
- **404 Not Found**: Recurso não encontrado
- **409 Conflict**: Conflito de dados
- **422 Unprocessable Entity**: Dados inválidos

### **Códigos de Erro do Servidor**
- **500 Internal Server Error**: Erro interno do servidor
- **502 Bad Gateway**: Erro de gateway
- **503 Service Unavailable**: Serviço indisponível
- **504 Gateway Timeout**: Timeout de gateway

## 🛠️ Boas Práticas

### **1. Autenticação**
- **Tokens**: Usar tokens com expiração adequada
- **Renovação**: Renovar tokens automaticamente
- **Segurança**: Nunca expor tokens em logs
- **HTTPS**: Sempre usar HTTPS para comunicação
- **Rate Limiting**: Respeitar limites de requisições

### **2. Tratamento de Erros**
- **Códigos**: Verificar códigos de status HTTP
- **Retry**: Implementar retry para falhas temporárias
- **Logs**: Registrar erros para auditoria
- **Fallback**: Ter estratégias de fallback
- **Monitoramento**: Monitorar erros e performance

### **3. Performance**
- **Cache**: Usar cache para dados frequentes
- **Paginação**: Implementar paginação para grandes volumes
- **Compressão**: Usar compressão de dados
- **Pooling**: Reutilizar conexões HTTP
- **Assíncrono**: Usar processamento assíncrono quando possível

### **4. Segurança**
- **Validação**: Validar todos os dados de entrada
- **Sanitização**: Sanitizar dados antes do envio
- **Criptografia**: Usar criptografia para dados sensíveis
- **Auditoria**: Registrar todas as operações
- **Backup**: Manter backups regulares

## 🔍 Troubleshooting

### **Problemas Comuns**
- **Token expirado**: Renovar token automaticamente
- **Rate limit**: Implementar backoff exponencial
- **Timeout**: Ajustar timeouts de conexão
- **Dados inválidos**: Validar dados antes do envio
- **Conectividade**: Verificar conectividade de rede

### **Soluções**
- **Logs**: Analisar logs de erro
- **Debug**: Usar ferramentas de debug
- **Testes**: Testar em ambiente isolado
- **Documentação**: Consultar documentação oficial
- **Suporte**: Contatar suporte técnico

## 🚀 Evolução e Tendências

### **Melhorias Contínuas**
- **Novos Endpoints**: Novos endpoints para funcionalidades
- **Melhor Performance**: Otimizações de performance
- **Webhooks**: Suporte para webhooks
- **GraphQL**: Suporte para GraphQL
- **SDKs**: SDKs para diferentes linguagens

### **Tendências Futuras**
- **Real-time**: APIs em tempo real
- **Microserviços**: Arquitetura de microserviços
- **Cloud**: Execução em cloud
- **IA**: Integração com inteligência artificial
- **Blockchain**: Integração com blockchain

---

*Este documento foi criado com base na documentação oficial do Sankhya Developer sobre Endpoints da API e melhores práticas de desenvolvimento.*
