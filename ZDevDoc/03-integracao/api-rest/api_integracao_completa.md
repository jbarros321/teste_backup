# 🔗 API de Integração Sankhya - Guia Completo

## 🎯 Visão Geral

A **API de Integração Sankhya** é uma ferramenta poderosa que permite a integração da plataforma Sankhya com outros sistemas, facilitando a troca de dados, automação de processos e criação de soluções personalizadas. A API oferece endpoints RESTful para acessar e manipular dados de forma programática.

## 🏗️ Arquitetura da API

### **Componentes Principais**
- **Gateway de Integração**: Ponto de entrada para todas as requisições
- **Camada de Autorização**: Controle de acesso e autenticação
- **Mapeamento de Serviços**: Roteamento e transformação de dados
- **Endpoints RESTful**: Interface padronizada para operações
- **Sistema de Respostas**: Códigos de retorno e tratamento de erros

### **Fluxo de Integração**
```
┌─────────────────────────────────────────────────────────────┐
│                    SISTEMA EXTERNO                        │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Aplicação     │ │   Middleware    │ │   Webhook       │ │
│  │   Cliente       │ │   de Integração │ │   Handler       │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    GATEWAY SANKHYA                        │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Autenticação  │ │   Autorização   │ │   Rate Limiting │ │
│  │   e Tokens      │ │   de Acesso     │ │   e Throttling  │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    MAPEAMENTO DE SERVIÇOS                 │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Roteamento    │ │   Transformação │ │   Validação     │ │
│  │   de Requisições│ │   de Dados      │ │   de Parâmetros │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    SISTEMA SANKHYA                        │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Processamento │ │   Persistência  │ │   Resposta      │ │
│  │   de Negócio    │ │   de Dados      │ │   e Feedback    │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🔐 Autenticação e Autorização

### **1. Geração de Tokens**
```bash
# Exemplo de geração de token via API
curl -X POST "https://api.sankhya.com.br/auth/token" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "seu_usuario",
    "password": "sua_senha",
    "grant_type": "password"
  }'
```

### **2. Uso do Token**
```bash
# Exemplo de uso do token em requisições
curl -X GET "https://api.sankhya.com.br/v1/produtos" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json"
```

### **3. Renovação de Token**
```bash
# Renovação automática de token
curl -X POST "https://api.sankhya.com.br/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refresh_token": "SEU_REFRESH_TOKEN_AQUI"
  }'
```

## 📋 Endpoints Principais

### **1. Autenticação**
- **POST /auth/token**: Obter token de acesso
- **POST /auth/refresh**: Renovar token
- **POST /auth/logout**: Invalidar token

### **2. Produtos**
- **GET /v1/produtos**: Listar produtos
- **POST /v1/produtos**: Criar produto
- **PUT /v1/produtos/{id}**: Atualizar produto
- **DELETE /v1/produtos/{id}**: Excluir produto

### **3. Clientes**
- **GET /v1/clientes**: Listar clientes
- **POST /v1/clientes**: Criar cliente
- **PUT /v1/clientes/{id}**: Atualizar cliente
- **GET /v1/clientes/{id}**: Obter cliente específico

### **4. Pedidos**
- **GET /v1/pedidos**: Listar pedidos
- **POST /v1/pedidos**: Criar pedido
- **PUT /v1/pedidos/{id}**: Atualizar pedido
- **GET /v1/pedidos/{id}**: Obter pedido específico

### **5. Financeiro**
- **GET /v1/contas-receber**: Listar contas a receber
- **GET /v1/contas-pagar**: Listar contas a pagar
- **POST /v1/baixas**: Processar baixas
- **GET /v1/relatorios-financeiros**: Relatórios financeiros

## 🛠️ Exemplos Práticos

### **1. Integração com E-commerce**
```javascript
// Exemplo de integração com e-commerce
class SankhyaEcommerceIntegration {
    constructor(apiUrl, token) {
        this.apiUrl = apiUrl;
        this.token = token;
    }
    
    // Sincronizar produtos
    async sincronizarProdutos(produtos) {
        try {
            for (const produto of produtos) {
                const response = await fetch(`${this.apiUrl}/v1/produtos`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${this.token}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        codigo: produto.sku,
                        descricao: produto.nome,
                        preco: produto.preco,
                        estoque: produto.estoque,
                        categoria: produto.categoria
                    })
                });
                
                if (!response.ok) {
                    throw new Error(`Erro ao criar produto ${produto.sku}: ${response.statusText}`);
                }
            }
            
            console.log('Produtos sincronizados com sucesso');
            
        } catch (error) {
            console.error('Erro na sincronização:', error);
            throw error;
        }
    }
    
    // Sincronizar pedidos
    async sincronizarPedidos(pedidos) {
        try {
            for (const pedido of pedidos) {
                const response = await fetch(`${this.apiUrl}/v1/pedidos`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${this.token}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        numero: pedido.id,
                        cliente: pedido.cliente,
                        itens: pedido.itens,
                        valor: pedido.total,
                        data: pedido.data
                    })
                });
                
                if (!response.ok) {
                    throw new Error(`Erro ao criar pedido ${pedido.id}: ${response.statusText}`);
                }
            }
            
            console.log('Pedidos sincronizados com sucesso');
            
        } catch (error) {
            console.error('Erro na sincronização:', error);
            throw error;
        }
    }
}

// Uso da integração
const integracao = new SankhyaEcommerceIntegration(
    'https://api.sankhya.com.br',
    'seu_token_aqui'
);

// Sincronizar dados
integracao.sincronizarProdutos(produtosEcommerce)
    .then(() => integracao.sincronizarPedidos(pedidosEcommerce))
    .catch(error => console.error('Erro na integração:', error));
```

### **2. Integração com Sistema de CRM**
```python
# Exemplo de integração com CRM em Python
import requests
import json
from datetime import datetime

class SankhyaCRMIntegration:
    def __init__(self, api_url, token):
        self.api_url = api_url
        self.token = token
        self.headers = {
            'Authorization': f'Bearer {token}',
            'Content-Type': 'application/json'
        }
    
    def obter_clientes(self, filtros=None):
        """Obter lista de clientes do Sankhya"""
        try:
            params = {}
            if filtros:
                params.update(filtros)
            
            response = requests.get(
                f'{self.api_url}/v1/clientes',
                headers=self.headers,
                params=params
            )
            
            response.raise_for_status()
            return response.json()
            
        except requests.exceptions.RequestException as e:
            print(f'Erro ao obter clientes: {e}')
            return None
    
    def criar_cliente(self, dados_cliente):
        """Criar novo cliente no Sankhya"""
        try:
            response = requests.post(
                f'{self.api_url}/v1/clientes',
                headers=self.headers,
                json=dados_cliente
            )
            
            response.raise_for_status()
            return response.json()
            
        except requests.exceptions.RequestException as e:
            print(f'Erro ao criar cliente: {e}')
            return None
    
    def atualizar_cliente(self, cliente_id, dados_cliente):
        """Atualizar cliente existente no Sankhya"""
        try:
            response = requests.put(
                f'{self.api_url}/v1/clientes/{cliente_id}',
                headers=self.headers,
                json=dados_cliente
            )
            
            response.raise_for_status()
            return response.json()
            
        except requests.exceptions.RequestException as e:
            print(f'Erro ao atualizar cliente: {e}')
            return None
    
    def sincronizar_crm(self, clientes_crm):
        """Sincronizar clientes do CRM com o Sankhya"""
        try:
            clientes_sankhya = self.obter_clientes()
            clientes_sankhya_ids = {c['id']: c for c in clientes_sankhya.get('data', [])}
            
            for cliente_crm in clientes_crm:
                cliente_id = cliente_crm.get('external_id')
                
                if cliente_id in clientes_sankhya_ids:
                    # Atualizar cliente existente
                    self.atualizar_cliente(cliente_id, {
                        'nome': cliente_crm['nome'],
                        'email': cliente_crm['email'],
                        'telefone': cliente_crm['telefone'],
                        'endereco': cliente_crm['endereco']
                    })
                else:
                    # Criar novo cliente
                    novo_cliente = self.criar_cliente({
                        'nome': cliente_crm['nome'],
                        'email': cliente_crm['email'],
                        'telefone': cliente_crm['telefone'],
                        'endereco': cliente_crm['endereco'],
                        'external_id': cliente_crm['id']
                    })
                    
                    if novo_cliente:
                        cliente_crm['external_id'] = novo_cliente['id']
            
            print('Sincronização de clientes concluída com sucesso')
            
        except Exception as e:
            print(f'Erro na sincronização: {e}')

# Uso da integração
integracao = SankhyaCRMIntegration(
    'https://api.sankhya.com.br',
    'seu_token_aqui'
)

# Sincronizar clientes
clientes_crm = [
    {
        'id': 'crm_001',
        'nome': 'João Silva',
        'email': 'joao@email.com',
        'telefone': '(11) 99999-9999',
        'endereco': 'Rua A, 123'
    },
    # ... mais clientes
]

integracao.sincronizar_crm(clientes_crm)
```

### **3. Integração com Sistema de Estoque**
```java
// Exemplo de integração com sistema de estoque em Java
public class SankhyaEstoqueIntegration {
    
    private final String apiUrl;
    private final String token;
    private final RestTemplate restTemplate;
    
    public SankhyaEstoqueIntegration(String apiUrl, String token) {
        this.apiUrl = apiUrl;
        this.token = token;
        this.restTemplate = new RestTemplate();
        
        // Configurar headers padrão
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + token);
            request.getHeaders().add("Content-Type", "application/json");
            return execution.execute(request, body);
        });
    }
    
    public List<Produto> obterProdutos() {
        try {
            String url = apiUrl + "/v1/produtos";
            ResponseEntity<ProdutoResponse> response = restTemplate.getForEntity(
                url, ProdutoResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody().getData();
            }
            
            return Collections.emptyList();
            
        } catch (Exception e) {
            System.err.println("Erro ao obter produtos: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    public boolean atualizarEstoque(String codigoProduto, int novaQuantidade) {
        try {
            String url = apiUrl + "/v1/produtos/" + codigoProduto + "/estoque";
            
            Map<String, Object> dados = new HashMap<>();
            dados.put("quantidade", novaQuantidade);
            dados.put("dataAtualizacao", LocalDateTime.now());
            
            ResponseEntity<Void> response = restTemplate.exchange(
                url, HttpMethod.PUT, 
                new HttpEntity<>(dados), 
                Void.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            System.err.println("Erro ao atualizar estoque: " + e.getMessage());
            return false;
        }
    }
    
    public void sincronizarEstoque(List<ItemEstoque> itensEstoque) {
        try {
            for (ItemEstoque item : itensEstoque) {
                boolean sucesso = atualizarEstoque(
                    item.getCodigoProduto(), 
                    item.getQuantidade()
                );
                
                if (sucesso) {
                    System.out.println("Estoque atualizado para produto " + 
                        item.getCodigoProduto() + ": " + item.getQuantidade());
                } else {
                    System.err.println("Falha ao atualizar estoque para produto " + 
                        item.getCodigoProduto());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erro na sincronização de estoque: " + e.getMessage());
        }
    }
}

// Classes de dados
class Produto {
    private String codigo;
    private String descricao;
    private BigDecimal preco;
    private int estoque;
    // getters e setters
}

class ItemEstoque {
    private String codigoProduto;
    private int quantidade;
    // getters e setters
}

class ProdutoResponse {
    private List<Produto> data;
    // getters e setters
}
```

## 🔧 Boas Práticas

### **1. Autenticação e Segurança**
- **Tokens**: Usar tokens com expiração adequada
- **HTTPS**: Sempre usar HTTPS para comunicação
- **Rate Limiting**: Respeitar limites de requisições
- **Validação**: Validar todos os dados de entrada
- **Logs**: Registrar todas as operações para auditoria

### **2. Tratamento de Erros**
- **Códigos HTTP**: Usar códigos de status apropriados
- **Mensagens**: Fornecer mensagens de erro claras
- **Retry**: Implementar retry para falhas temporárias
- **Fallback**: Ter estratégias de fallback
- **Monitoramento**: Monitorar erros e performance

### **3. Performance**
- **Cache**: Usar cache para dados frequentes
- **Paginação**: Implementar paginação para grandes volumes
- **Compressão**: Usar compressão de dados
- **Pooling**: Reutilizar conexões HTTP
- **Assíncrono**: Usar processamento assíncrono quando possível

### **4. Monitoramento**
- **Métricas**: Coletar métricas de uso
- **Logs**: Manter logs detalhados
- **Alertas**: Configurar alertas para falhas
- **Dashboard**: Criar dashboards de monitoramento
- **Relatórios**: Gerar relatórios de uso

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

*Este documento foi criado com base na documentação oficial do Sankhya Developer sobre API de Integração e melhores práticas de desenvolvimento.*
