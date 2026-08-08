# TOTVS Protheus - APIs e Integrações

## Formas de Integração

| Método | Descrição |
|:---|:---|
| **APIs REST Customizadas** | Desenvolvidas em AdvPL/TL++ (mais comum) |
| **APIs Públicas TOTVS** | Disponíveis em versões recentes via TDN |
| **TOTVS iPaaS** | Plataforma de integração com conectores prontos |
| **Web Services SOAP** | Legado, ainda utilizado em algumas integrações |

---

## Configuração do REST Server

### appserver.ini
```ini
[REST]
Port=8080
URIs=RESTFUL
Security=1
Public=/api/oauth2

[RESTFUL]
URL=/rest
PrepareIn=ALL
Instances=1,3
```

### Autenticação (OAuth 2.0)
```http
POST http://servidor:8080/rest/api/oauth2/v1/token
Content-Type: application/json

{
  "grant_type": "password",
  "username": "admin",
  "password": "senha123"
}
```

**Resposta:**
```json
{
  "access_token": "eyJhbGciOi...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "dGhpcyBpcyBh..."
}
```

---

## Endpoints Padrão (APIs Públicas)

> [!IMPORTANT]
> Consulte o [TDN](https://tdn.totvs.com) buscando "APIs Públicas Protheus" para a lista completa da sua versão.

### Clientes

| Método | Endpoint | Descrição |
|:---|:---|:---|
| `GET` | `/rest/api/retaguarda/v2/customers` | Listar clientes |
| `GET` | `/rest/api/retaguarda/v2/customers/{id}` | Buscar cliente |
| `POST` | `/rest/api/retaguarda/v2/customers` | Criar cliente |
| `PUT` | `/rest/api/retaguarda/v2/customers/{id}` | Atualizar cliente |
| `DELETE` | `/rest/api/retaguarda/v2/customers/{id}` | Excluir cliente |

**Exemplo — Criar Cliente:**
```http
POST /rest/api/retaguarda/v2/customers
Authorization: Bearer {token}
Content-Type: application/json
```
```json
{
  "customerCode": "000100",
  "storeId": "01",
  "name": "EMPRESA EXEMPLO LTDA",
  "shortName": "EXEMPLO",
  "governmentId": "12345678000190",
  "stateId": "123456789",
  "address": {
    "street": "RUA EXEMPLO",
    "number": "100",
    "city": "SAO PAULO",
    "state": "SP",
    "zipCode": "01001000"
  }
}
```

---

### Pedidos de Venda

| Método | Endpoint | Descrição |
|:---|:---|:---|
| `GET` | `/rest/api/retaguarda/v2/salesOrders` | Listar pedidos |
| `GET` | `/rest/api/retaguarda/v2/salesOrders/{id}` | Buscar pedido |
| `POST` | `/rest/api/retaguarda/v2/salesOrders` | Criar pedido |

**Exemplo — Criar Pedido:**
```json
{
  "orderNumber": "000001",
  "customerCode": "000100",
  "storeId": "01",
  "sellerCode": "000001",
  "paymentCondition": "001",
  "items": [
    {
      "itemCode": "000001",
      "product": "PROD001",
      "quantity": 10,
      "unitPrice": 25.90,
      "tes": "501"
    }
  ]
}
```

---

### Produtos

| Método | Endpoint | Descrição |
|:---|:---|:---|
| `GET` | `/rest/api/retaguarda/v2/products` | Listar produtos |
| `GET` | `/rest/api/retaguarda/v2/products/{id}` | Buscar produto |
| `POST` | `/rest/api/retaguarda/v2/products` | Criar produto |

---

### Financeiro

| Método | Endpoint | Descrição |
|:---|:---|:---|
| `GET` | `/rest/api/retaguarda/v2/accountsReceivable` | Títulos a receber |
| `GET` | `/rest/api/retaguarda/v2/accountsPayable` | Títulos a pagar |
| `POST` | `/rest/api/retaguarda/v2/accountsReceivable` | Incluir título CR |
| `POST` | `/rest/api/retaguarda/v2/accountsPayable` | Incluir título CP |

---

### Estoque

| Método | Endpoint | Descrição |
|:---|:---|:---|
| `GET` | `/rest/api/retaguarda/v2/stockBalances` | Saldos de estoque |
| `POST` | `/rest/api/retaguarda/v2/stockMovements` | Movimentar estoque |

---

## Query Params Comuns

| Parâmetro | Descrição | Exemplo |
|:---|:---|:---|
| `page` | Página | `?page=1` |
| `pageSize` | Itens por página | `?pageSize=20` |
| `order` | Ordenação | `?order=name` |
| `filter` | Filtro | `?filter=state eq 'SP'` |
| `fields` | Campos retornados | `?fields=code,name` |

---

## Criando APIs Customizadas (AdvPL)

### Exemplo básico REST em AdvPL:
```advpl
#INCLUDE "TOTVS.CH"
#INCLUDE "RESTFUL.CH"

WSRESTFUL CustomerAPI DESCRIPTION "API de Clientes"

    WSDATA page AS INTEGER
    WSDATA pageSize AS INTEGER

    WSMETHOD GET    DESCRIPTION "Listar clientes"    WSSYNTAX "/customers"
    WSMETHOD POST   DESCRIPTION "Criar cliente"      WSSYNTAX "/customers"

END WSRESTFUL

WSMETHOD GET WSRECEIVE page, pageSize WSSERVICE CustomerAPI
    Local cQuery := "SELECT A1_COD, A1_NOME FROM SA1010 WHERE D_E_L_E_T_ = ' '"
    // ... lógica de consulta
    Self:SetResponse(cJsonResponse)
RETURN .T.
```

---

## Ferramentas de Teste

| Ferramenta | Uso |
|:---|:---|
| **Postman** | Testar endpoints REST |
| **Insomnia** | Alternativa ao Postman |
| **Swagger/OpenAPI** | Se configurado no ambiente |
| **APSDU** | Validar dados no banco |

> [!TIP]
> Para verificar se o REST está rodando: acesse `http://servidor:8080/rest` no navegador. Deve retornar informações do serviço.

> [!CAUTION]
> Sempre teste integrações em ambiente de **homologação** antes de produção. Erros em APIs podem gerar dados inconsistentes no ERP.
