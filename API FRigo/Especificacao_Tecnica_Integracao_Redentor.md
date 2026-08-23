# 📄 Especificação Técnica de Integração — JHS Alimentos x AIS APIS (Frigorífico Redentor)

> **Documento oficial de integração** para comunicação via API entre o sistema AIS APIS (Redentor) e o ERP Sankhya (JHS Alimentos).

---

## 🔑 1. Credenciais e Servidor

### A. Credenciais de Servidor e Usuário (ERP Sankhya)

| Parâmetro | Valor |
|-----------|-------|
| **Servidor Base** | `http://jhsalimentosfw2.fortiddns.com:8280` |
| **Ambiente** | Teste / Homologação |
| **Usuário API** | `REDENTOR.FRIGO` |
| **Senha API** | `CristoR$denT0R@@` |

### B. Credenciais OAuth 2.0 (API Gateway Sankhya Oficial)

#### Produção:
* **Client ID:** `2bd99733-569c-40e9-b8bc-0a95d36729d8`
* **Client Secret:** `26evyVhtqJheajGiu48zCsb237onDTLE`

#### Sandbox (Teste):
* **Client ID Sandbox:** `85846fbb-7797-4326-af46-32d7f758e67b`
* **Client Secret Sandbox:** `BBsYPKNw3DSIASizZQFjSXMnIGYga4fx`

---

## 🔐 2. Autenticação

### Opção A — OAuth 2.0 (API Gateway Oficial Sankhya)

Para gerar o `access_token` (Bearer Token) via API Gateway:

```http
POST https://api.sankhya.com.br/authenticate
Content-Type: application/x-www-form-urlencoded

client_id=85846fbb-7797-4326-af46-32d7f758e67b
client_secret=BBsYPKNw3DSIASizZQFjSXMnIGYga4fx
grant_type=client_credentials
```

### Opção B — Login MobileLoginSP (Sessão ERP)

```http
POST http://jhsalimentosfw2.fortiddns.com:8280/mge/service.sbr?serviceName=MobileLoginSP.login&outputType=json
Content-Type: application/json

{
  "serviceName": "MobileLoginSP.login",
  "requestBody": {
    "NOMUSU": { "$": "REDENTOR.FRIGO" },
    "INTERNO": { "$": "CristoR$denT0R@@" },
    "KEEPCONNECTED": { "$": "S" }
  }
}
```

---

## 📡 3. Endpoints de Envio de Dados (Redentor → JHS Webhook)

As chamadas de envio de dados de produção devem ser realizadas para o endpoint do Webhook JHS.

### Endpoint Base do Webhook
```http
POST http://jhsalimentosfw2.fortiddns.com:8280/mge/service.sbr?service=RedentoWebhook&acao={ACAO}&outputType=json
Content-Type: application/json
X-Redentor-Token: TOKEN_SECRETO_REDENTOR
```

---

### Ações Disponíveis (`acao`):

#### 1. `acao=PesagemAbate` — Dados de Abate de Animais
```json
{
  "cnpj_industria": "99999999999999",
  "data_abate": "2026-07-22 10:00:00",
  "SIF": "999",
  "cnpj_produtor": "11111111111111",
  "lote_abate": 101,
  "sequencia_abate": 1,
  "banda": "D",
  "codigo_especie": "1",
  "codigo_raca": "01",
  "codigo_destino": "1",
  "codigo_tipificacao": "A",
  "codigo_qualidade": "1",
  "codigo_idade": "2",
  "peso_liquido_carcaca": 245.50,
  "data_producao": "2026-07-22",
  "serial_etiqueta_tr": "ETQ10001",
  "peso_liquido_tr": 120.00,
  "codigo_produto_tr": 501,
  "serial_etiqueta_dt": "ETQ10002",
  "peso_liquido_dt": 125.50,
  "codigo_produto_dt": 502,
  "habilitacoes": "LE,CN"
}
```

#### 2. `acao=PesagemConferencia` — Peso Frio / Câmara
```json
{
  "data_abate": "2026-07-22 10:00:00",
  "sequencia_abate": 1,
  "banda": "D",
  "serial_etiqueta": "ETQ10001",
  "peso_liquido": 118.50,
  "data_pesagem": "2026-07-22 14:00:00"
}
```

#### 3. `acao=ConsumoMP` — Consumo de Matéria-Prima (Entrada na Desossa)
```json
{
  "numero_op": "OP2026001",
  "serial_etiqueta": "ETQ10001",
  "codigo_produto": 501,
  "banda": "D",
  "quarto": "TR",
  "quantidade": 1,
  "peso_liquido": 118.50,
  "data_consumo": "2026-07-22 15:00:00"
}
```

#### 4. `acao=ItemProduzido` — Cortes / Embalagens Produzidos
```json
{
  "numero_op": "OP2026001",
  "sif": "999",
  "rastreabilidade": "RAST20260722-01",
  "serial_etiqueta": "PROD90001",
  "codigo_produto": 1050,
  "pecas": 4,
  "quantidade": 1,
  "peso_bruto": 25.80,
  "tara": 0.30,
  "peso_liquido": 25.50,
  "data_producao": "2026-07-22 16:00:00",
  "data_validade": "2026-09-22",
  "data_abate": "2026-07-22",
  "data_desossa": "2026-07-22",
  "data_movimentacao": "2026-07-22 16:30:00"
}
```

---

## 📤 4. Resposta Padrão do Webhook

```json
{
  "status": "OK",
  "mensagem": "Dados recebidos e gravados com sucesso. ID: RED_1784662400000"
}
```

Em caso de inconsistência (ex: código de produto não cadastrado no De-Para):
```json
{
  "status": "ERRO",
  "mensagem": "Codigo Redentor=1050 nao possui mapeamento ativo em AD_REDENTOR_DEPARA."
}
```
