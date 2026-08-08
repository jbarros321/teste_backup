# TOTVS Winthor - APIs e Integrações

## Portal TOTVS Developers

A documentação oficial das APIs REST do Winthor está disponível em:
- **URL**: [https://api.totvs.com.br/](https://api.totvs.com.br/)
- Filtre por produto **Winthor** para ver os endpoints disponíveis

---

## Autenticação

### OAuth 2.0 (Padrão)

```http
POST /api/oauth/v1/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=SEU_CLIENT_ID
&client_secret=SEU_CLIENT_SECRET
```

**Resposta:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### Uso do Token

```http
GET /api/winthor/v1/clientes
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
Content-Type: application/json
```

---

## Endpoints Principais

> [!IMPORTANT]
> A disponibilidade dos endpoints depende da versão do Winthor e das configurações do ambiente. Consulte o portal TOTVS Developers para a lista atualizada.

### Clientes

| Método | Endpoint | Descrição |
|:---|:---|:---|
| `GET` | `/api/winthor/v1/clientes` | Listar clientes |
| `GET` | `/api/winthor/v1/clientes/{codcli}` | Consultar cliente por código |
| `POST` | `/api/winthor/v1/clientes` | Criar novo cliente |
| `PUT` | `/api/winthor/v1/clientes/{codcli}` | Atualizar cliente |

**Exemplo — Consultar Cliente:**
```http
GET /api/winthor/v1/clientes/12345
Authorization: Bearer {token}
```

**Resposta:**
```json
{
  "codcli": 12345,
  "cliente": "EMPRESA EXEMPLO LTDA",
  "fantasia": "EXEMPLO",
  "cgcent": "12.345.678/0001-90",
  "enderent": "RUA EXEMPLO, 100",
  "municent": "SAO PAULO",
  "estent": "SP",
  "limcred": 50000.00,
  "bloqueio": "N"
}
```

---

### Produtos

| Método | Endpoint | Descrição |
|:---|:---|:---|
| `GET` | `/api/winthor/v1/produtos` | Listar produtos |
| `GET` | `/api/winthor/v1/produtos/{codprod}` | Consultar produto |
| `POST` | `/api/winthor/v1/produtos` | Criar produto |
| `PUT` | `/api/winthor/v1/produtos/{codprod}` | Atualizar produto |

---

### Pedidos de Venda

| Método | Endpoint | Descrição |
|:---|:---|:---|
| `GET` | `/api/winthor/v1/pedidos` | Listar pedidos |
| `GET` | `/api/winthor/v1/pedidos/{numped}` | Consultar pedido |
| `POST` | `/api/winthor/v1/pedidos` | Criar pedido de venda |
| `PUT` | `/api/winthor/v1/pedidos/{numped}` | Atualizar pedido |

**Exemplo — Criar Pedido:**
```http
POST /api/winthor/v1/pedidos
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "codcli": 12345,
  "codusur": 10,
  "codfilial": "1",
  "codplpag": 1,
  "codcob": "BK",
  "itens": [
    {
      "codprod": 500,
      "qt": 10,
      "pvenda": 25.90
    },
    {
      "codprod": 501,
      "qt": 5,
      "pvenda": 18.50
    }
  ]
}
```

---

### Notas Fiscais

| Método | Endpoint | Descrição |
|:---|:---|:---|
| `GET` | `/api/winthor/v1/notasfiscais` | Listar notas fiscais |
| `GET` | `/api/winthor/v1/notasfiscais/{numnota}` | Consultar nota fiscal |

---

### Financeiro

| Método | Endpoint | Descrição |
|:---|:---|:---|
| `GET` | `/api/winthor/v1/contasreceber` | Listar contas a receber |
| `GET` | `/api/winthor/v1/contaspagar` | Listar contas a pagar |
| `POST` | `/api/winthor/v1/contaspagar` | Incluir conta a pagar |

---

### Estoque

| Método | Endpoint | Descrição |
|:---|:---|:---|
| `GET` | `/api/winthor/v1/estoque` | Consultar posição de estoque |
| `GET` | `/api/winthor/v1/estoque/{codprod}` | Estoque de produto específico |

---

## Parâmetros de Consulta (Query Params)

A maioria dos endpoints `GET` aceita parâmetros para filtrar resultados:

| Parâmetro | Tipo | Descrição |
|:---|:---|:---|
| `page` | int | Número da página |
| `pageSize` | int | Quantidade por página |
| `order` | string | Campo de ordenação |
| `fields` | string | Campos a retornar (separados por vírgula) |
| `filter` | string | Filtros adicionais |

**Exemplo:**
```http
GET /api/winthor/v1/clientes?page=1&pageSize=50&order=cliente&filter=estent eq 'SP'
```

---

## Integração via WTA (Winthor Anywhere)

O **WTA** é o servidor de aplicação web do Winthor que expõe serviços REST para integrações. Configurações importantes:

1. **URL Base**: `http://{servidor}:{porta}/api`
2. **Porta padrão**: 8180
3. **Configuração**: Feita através do painel administrativo do WTA

### Habilitando APIs no WTA

1. Acesse o painel do WTA: `http://servidor:8180/admin`
2. Navegue até **Serviços**
3. Habilite os serviços necessários
4. Configure permissões de acesso

---

## Integração EDI (NEOGRID)

Para integração via EDI com redes varejistas:

| Rotina | Descrição |
|:---|:---|
| **2521** | Importação/Exportação de pedidos via NEOGRID |
| **2523** | Configuração de layouts EDI |

---

## Webhooks e Eventos

O Winthor pode ser configurado para enviar notificações em eventos:

- Pedido criado/alterado
- Nota fiscal emitida
- Pagamento recebido
- Estoque atualizado

> [!TIP]
> Para integrações com e-commerce, verifique os conectores disponíveis no **TOTVS iPaaS** que oferece integrações pré-configuradas.

> [!CAUTION]
> A TOTVS **não homologa** acesso direto ao banco de dados para integrações. Manipulações diretas podem comprometer a estabilidade e impedir suporte técnico.
