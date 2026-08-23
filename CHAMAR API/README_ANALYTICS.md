# 📋 GUIA COMPLETO - API Analytics para TGFMET

## 🎯 O QUE VOCÊ PRECISA FAZER

Configurar uma chamada de API REST no Analytics do Sankhya para inserir dados na tabela **TGFMET**.

---

## ✅ CHECKLIST RÁPIDO

- [ ] Mudar tipo de chamada de **GET** para **POST**
- [ ] Colar URL completa do endpoint Sankhya
- [ ] Habilitar "Body personalizado" e colar JSON
- [ ] Configurar View e ativar Link Dinâmico
- [ ] Mapear variáveis da View no JSON
- [ ] Testar com botão "Testar API"

---

## 📝 CONFIGURAÇÃO PASSO A PASSO

### 1️⃣ TIPO DE CHAMADA
```
MUDAR DE: GET
PARA:     POST ✅
```

### 2️⃣ URL
```
https://api.sankhya.co/mge/service.sbr?serviceName=CRUDServiceProvider.saveRecord&outputType=json
```

**OU** (se usar servidor próprio):
```
https://seu-servidor:porta/mge/service.sbr?serviceName=CRUDServiceProvider.saveRecord&outputType=json
```

### 3️⃣ HEADERS
✅ **Já está configurado:**
- `Content-Type: application/json`

⚠️ **Adicionar se precisar autenticação:**
- `Authorization: Bearer SEU_TOKEN`

### 4️⃣ BODY
✅ **Habilitar:** "Habilitar body personalizado" (ligar toggle)

✅ **JSON para colar:**
```json
{
  "serviceName": "CRUDServiceProvider.saveRecord",
  "requestBody": {
    "dataSet": {
      "rootEntity": "TGFMET",
      "includePresentationFields": "S",
      "dataRow": {
        "CODMET": ":CODMET",
        "DESCRICAO": ":DESCRICAO",
        "VALOR": ":VALOR",
        "ATIVO": "S"
      }
    }
  }
}
```

### 5️⃣ VARIÁVEIS
1. Configure sua **View** primeiro
2. Ative o **"Link Dinâmico"** na View
3. Use `:NOME_DA_COLUNA` no JSON do Body
4. Exemplo: se View tem coluna "CODIGO", use `:CODIGO`

### 6️⃣ VARIÁVEL DE RETORNO
✅ Deixar como está: `:VAR_SANKHYA_SESSION`

---

## 📚 ARQUIVOS DISPONÍVEIS

| Arquivo | Descrição |
|---------|-----------|
| **CONFIGURAR_API_ANALYTICS.txt** | Guia completo detalhado |
| **GUIA_VISUAL_CAMPOS.txt** | Guia visual mostrando cada campo |
| **EXEMPLO_JSON_BODY.txt** | Exemplos de JSON prontos para copiar |
| **README_ANALYTICS.md** | Este arquivo (resumo) |

---

## 🔧 EXEMPLO PRÁTICO

### Sua View retorna:
| CODIGO | DESCRICAO | VALOR |
|--------|-----------|-------|
| 001    | Método A  | 100.50|
| 002    | Método B  | 200.00|

### No JSON do Body, use:
```json
{
  "CODMET": ":CODIGO",
  "DESCRICAO": ":DESCRICAO",
  "VALOR": ":VALOR"
}
```

### Resultado:
- Serão feitas **2 chamadas de API**
- Cada linha será inserida na TGFMET
- Registro 1: CODMET=001, DESCRICAO="Método A", VALOR=100.50
- Registro 2: CODMET=002, DESCRICAO="Método B", VALOR=200.00

---

## ⚠️ IMPORTANTE

### Campos Obrigatórios da TGFMET:
- ✅ **CODMET** (deve ser único)
- ✅ **DESCRICAO** (não pode ser vazio)

### Observação:
> "Será realizada uma chamada na API para cada linha retornada na View"

Isso significa:
- 10 linhas na View = 10 chamadas de API
- Cada linha inserida separadamente
- Se uma der erro, as outras continuam

---

## 🐛 SOLUÇÃO DE PROBLEMAS

| Erro | Solução |
|------|---------|
| **401 Unauthorized** | Verificar token de autenticação |
| **404 Not Found** | Verificar URL e servidor |
| **400 Bad Request** | Verificar formato JSON e nomes dos campos |
| **500 Internal Server Error** | Verificar logs do servidor Sankhya |

---

## ✅ TESTE FINAL

1. Clique em **"Testar API"** (botão roxo)
2. Verifique mensagem de sucesso
3. Confira se registro foi inserido na TGFMET
4. Se der erro, verifique os logs

---

## 📖 DOCUMENTAÇÃO COMPLETA

Para mais detalhes, consulte:
- **CONFIGURAR_API_ANALYTICS.txt** - Guia completo
- **GUIA_VISUAL_CAMPOS.txt** - Guia visual
- **EXEMPLO_JSON_BODY.txt** - Exemplos de JSON

---

**Pronto! Agora você tem tudo que precisa para configurar a API no Analytics! 🚀**




