# 🔌 Guia de Acesso à API Sankhya — Terminal e Postman

> Guia prático de autenticação e chamadas para a integração **Frigorífico Redentor x JHS Alimentos**.
> Complementa o `Especificacao_Tecnica_Integracao_Redentor.md`.

> ⚠️ **Este arquivo contém secrets em texto puro.** Antes de versionar ou compartilhar, considere
> substituir os valores por placeholders e mover os reais para variáveis de ambiente / cofre de senhas.

---

## 🌐 1. Ambientes — atenção à URL

O ponto que mais gera erro: **produção e sandbox são gateways diferentes**, com hosts distintos.
Não basta trocar o `client_id`.

| Ambiente | Host do Gateway |
|----------|-----------------|
| **Sandbox / Homologação** | `https://api.sandbox.sankhya.com.br` |
| **Produção** | `https://api.sankhya.com.br` |

Usar as credenciais de sandbox contra o host de produção retorna:

```json
{ "error": "invalid_client", "error_description": "Invalid client or Invalid client credentials" }
```

Esse erro **não** significa credencial errada — significa host errado.

---

## 🔑 2. Credenciais

### Sandbox (validadas e funcionando)

| Parâmetro | Valor |
|-----------|-------|
| **X-Token** | `fe421ec8-458b-4e98-94e6-197dde5bb6d2` |
| **Client ID** | `e1b5528f-6e6f-4b9c-ae86-b1d2395db3a4` |
| **Client Secret** | `jmKczws11EfNbzVKkZKDFftN3UMZLsSW` |

> As credenciais sandbox listadas no `Especificacao_Tecnica_Integracao_Redentor.md`
> (`85846fbb-7797-...`) estão **desatualizadas** — são rejeitadas com `X-Token não é válido`.
> Use as da tabela acima.

### Servidor ERP (homologação)

| Parâmetro | Valor |
|-----------|-------|
| **Servidor Base** | `http://jhsalimentosfw2.fortiddns.com:8280` |
| **Usuário API** | `REDENTOR.FRIGO` |
| **Senha API** | `CristoR$denT0R@@` |

---

## 🔐 3. Autenticação OAuth 2.0 — obter o `access_token`

### Terminal (curl)

```bash
curl -s -X POST https://api.sandbox.sankhya.com.br/authenticate \
  -H "X-Token: fe421ec8-458b-4e98-94e6-197dde5bb6d2" \
  -d "grant_type=client_credentials" \
  -d "client_id=e1b5528f-6e6f-4b9c-ae86-b1d2395db3a4" \
  -d "client_secret=jmKczws11EfNbzVKkZKDFftN3UMZLsSW"
```

Extraindo só o token para uma variável de shell (requer `jq`):

```bash
export SANKHYA_TOKEN=$(curl -s -X POST https://api.sandbox.sankhya.com.br/authenticate \
  -H "X-Token: fe421ec8-458b-4e98-94e6-197dde5bb6d2" \
  -d "grant_type=client_credentials" \
  -d "client_id=e1b5528f-6e6f-4b9c-ae86-b1d2395db3a4" \
  -d "client_secret=jmKczws11EfNbzVKkZKDFftN3UMZLsSW" | jq -r .access_token)

echo $SANKHYA_TOKEN
```

### Postman

**Method:** `POST`
**URL:** `https://api.sandbox.sankhya.com.br/authenticate`

**Aba Headers:**

| Key | Value |
|-----|-------|
| `X-Token` | `fe421ec8-458b-4e98-94e6-197dde5bb6d2` |

> Não adicione `Content-Type` manualmente. No modo urlencoded o Postman já envia o correto —
> um header duplicado quebra a request.

**Aba Body → selecione `x-www-form-urlencoded`** (não é `raw`, não é `form-data`):

| ✓ | Key | Value |
|---|-----|-------|
| ✓ | `grant_type` | `client_credentials` |
| ✓ | `client_id` | `e1b5528f-6e6f-4b9c-ae86-b1d2395db3a4` |
| ✓ | `client_secret` | `jmKczws11EfNbzVKkZKDFftN3UMZLsSW` |

Confira que os **três checkboxes estão marcados** — desmarcado, o Postman não envia o campo.

**Automatizar o token** — aba `Scripts → Post-response`:

```javascript
pm.environment.set("token", pm.response.json().access_token);
```

Nas demais requests use o header `Authorization: Bearer {{token}}`.

### Resposta esperada

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "expires_in": 300,
  "refresh_expires_in": 0,
  "token_type": "Bearer",
  "not-before-policy": 0,
  "scope": ""
}
```

⏱️ **O token expira em 300 segundos (5 minutos).** Regere a cada chamada ou implemente cache com renovação.

Para confirmar o ambiente, decodifique o JWT em [jwt.io](https://jwt.io) e verifique o campo
`"ambiente"`: `hml` = homologação, `prd` = produção.

---

## 📡 4. Webhook Redentor — envio de dados

O que muda entre as operações é apenas o parâmetro **`acao=`** na URL e o **JSON do body**.

### Estrutura comum

**URL:**
```
http://jhsalimentosfw2.fortiddns.com:8280/mge/service.sbr?service=RedentoWebhook&acao={ACAO}&outputType=json
```

**Headers:**

| Key | Value |
|-----|-------|
| `Content-Type` | `application/json` |
| `X-Redentor-Token` | `TOKEN_SECRETO_REDENTOR` |

**Postman:** Body → `raw` → `JSON`

### Ação 1 — `acao=PesagemAbate`

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

Equivalente em curl:

```bash
curl -s -X POST \
  "http://jhsalimentosfw2.fortiddns.com:8280/mge/service.sbr?service=RedentoWebhook&acao=PesagemAbate&outputType=json" \
  -H "Content-Type: application/json" \
  -H "X-Redentor-Token: TOKEN_SECRETO_REDENTOR" \
  -d @pesagem_abate.json
```

> `-d @arquivo.json` lê o corpo de um arquivo — mais prático que colar JSON grande no terminal.

### Ação 2 — `acao=PesagemConferencia`

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

### Ação 3 — `acao=ConsumoMP`

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

### Ação 4 — `acao=ItemProduzido`

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

### Resposta do Webhook

Sucesso:
```json
{ "status": "OK", "mensagem": "Dados recebidos e gravados com sucesso. ID: RED_1784662400000" }
```

Erro de mapeamento:
```json
{ "status": "ERRO", "mensagem": "Codigo Redentor=1050 nao possui mapeamento ativo em AD_REDENTOR_DEPARA." }
```

---

## 🧭 5. Ordem recomendada de teste

1. **Autenticar** no gateway sandbox → confirma credenciais e conectividade.
2. **Webhook** com `acao=PesagemAbate` e um registro de teste.

---

## 🛠️ 6. Troubleshooting

| Erro | Causa | Solução |
|------|-------|---------|
| `Missing form parameter: grant_type` | Body não chegou como formulário | Body → `x-www-form-urlencoded` (não `form-data`, não `Params`, não `raw/Text`) |
| `invalid_client` / `Invalid client credentials` | Host errado ou credencial de outro ambiente | Use `api.sandbox.sankhya.com.br` para sandbox |
| `X-Token não é válido` | X-Token não pertence ao par client_id/secret | X-Token e credenciais devem ser da **mesma** integração e ambiente |
| `unauthorized_client` | Cliente sem permissão para `client_credentials` | Solicitar liberação do grant à Sankhya |
| Timeout no `:8280` | DDNS / firewall | Liberação de IP na rede da JHS |
| `401` nas chamadas após autenticar | Token expirado (5 min) | Regerar o `access_token` |

### Diagnóstico no Postman

Com a request aberta, clique no ícone **`</>`** na barra lateral direita (`Ctrl/Cmd + Alt + C`)
e escolha **cURL**. Isso mostra exatamente o que está sendo enviado:

- `--data` / `--data-urlencode` → body urlencoded ✅
- `-F` / `--form` → está em `form-data` ❌
- Parâmetros na URL (`?grant_type=...`) → estão na aba `Params` ❌
- Nenhum `-d` → body vazio ou checkboxes desmarcados ❌

---

## ✅ 7. Checklist rápido

- [ ] URL com host **sandbox** (`api.sandbox.sankhya.com.br`)
- [ ] Header `X-Token` presente
- [ ] Body em `x-www-form-urlencoded`, três campos marcados
- [ ] Sem `Content-Type` duplicado nos headers
- [ ] Token renovado (validade de 5 minutos)
