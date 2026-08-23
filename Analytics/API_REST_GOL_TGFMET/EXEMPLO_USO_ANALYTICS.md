# 📊 EXEMPLO DE USO - API REST TGFMET NO ANALYTICS

## 🎯 CONFIGURAÇÃO COMPLETA NO ANALYTICS

### Passo 1: Configurar a Chamada de API

#### Tipo de Chamada
```
POST
```

#### URL
```
https://api.sankhya.com.br/v1/tgfmet
```

#### Headers

**Header 1**:
- **Nome**: `Authorization`
- **Valor**: `Bearer :VAR_TOKEN`

**Header 2**:
- **Nome**: `Content-Type`
- **Valor**: `application/json`

**⚠️ IMPORTANTE**: 
- `:VAR_TOKEN` deve conter o token OAuth obtido no login
- Se ainda não tem token, primeiro faça login na API

---

### Passo 2: Habilitar Body Personalizado

1. **Ligue o toggle** "Habilitar body personalizado"
2. Cole o JSON abaixo no campo Body:

```json
{
  "CODMET": :CODMET,
  "DESCRICAO": ":DESCRICAO",
  "VALOR": :VALOR,
  "ATIVO": "S"
}
```

**Nota**: 
- `:CODMET`, `:DESCRICAO`, `:VALOR` são variáveis da sua View
- O Analytics substitui automaticamente pelos valores reais

---

### Passo 3: Configurar Variáveis

#### Variável de Retorno
```
:VAR_RESPOSTA_TGFMET
```

#### Variáveis da View (exemplo)

Se sua View tem colunas:
- `CODIGO_METODO` → use `:CODIGO_METODO` no JSON
- `DESCR_METODO` → use `:DESCR_METODO` no JSON
- `VALOR_METODO` → use `:VALOR_METODO` no JSON

**JSON ajustado**:
```json
{
  "CODMET": :CODIGO_METODO,
  "DESCRICAO": ":DESCR_METODO",
  "VALOR": :VALOR_METODO,
  "ATIVO": "S"
}
```

---

## 📋 EXEMPLO PRÁTICO COMPLETO

### Cenário: Inserir dados da View na TGFMET

#### Sua View retorna:
| CODIGO | DESCRICAO | VALOR |
|--------|-----------|-------|
| 100    | Método A  | 50.00 |
| 200    | Método B  | 75.50 |

#### Configuração no Analytics:

**Tipo**: POST

**URL**: `https://api.sankhya.com.br/v1/tgfmet`

**Headers**:
```
Authorization: Bearer :VAR_TOKEN
Content-Type: application/json
```

**Body** (habilitado):
```json
{
  "CODMET": :CODIGO,
  "DESCRICAO": ":DESCRICAO",
  "VALOR": :VALOR,
  "ATIVO": "S"
}
```

**Variável de Retorno**: `:VAR_RESPOSTA_TGFMET`

#### Resultado:

Para cada linha da View, será feita uma chamada:

**Linha 1**:
- Request: `{ "CODMET": 100, "DESCRICAO": "Método A", "VALOR": 50.00, "ATIVO": "S" }`
- Response: `{ "status": "OK", "mensagem": "Registro inserido com sucesso na TGFMET", ... }`

**Linha 2**:
- Request: `{ "CODMET": 200, "DESCRICAO": "Método B", "VALOR": 75.50, "ATIVO": "S" }`
- Response: `{ "status": "OK", "mensagem": "Registro inserido com sucesso na TGFMET", ... }`

---

## 🔄 FLUXO COMPLETO COM LOGIN

### Passo 1: Fazer Login (Obter Token)

**Tipo**: POST

**URL**: `https://api.sankhya.com.br/login`

**Headers**:
```
Content-Type: application/json
```

**Body** (habilitado):
```json
{
  "username": "seu_usuario",
  "password": "sua_senha"
}
```

**Variável de Retorno**: `:VAR_TOKEN`

**⚠️ IMPORTANTE**: 
- Faça o login primeiro
- O token será armazenado em `:VAR_TOKEN`
- Use esse token nas próximas chamadas

---

### Passo 2: Inserir na TGFMET

**Tipo**: POST

**URL**: `https://api.sankhya.com.br/v1/tgfmet`

**Headers**:
```
Authorization: Bearer :VAR_TOKEN
Content-Type: application/json
```

**Body** (habilitado):
```json
{
  "CODMET": :CODMET,
  "DESCRICAO": ":DESCRICAO",
  "VALOR": :VALOR,
  "ATIVO": "S"
}
```

**Variável de Retorno**: `:VAR_RESPOSTA_TGFMET`

---

## 📊 EXEMPLO COM MÚLTIPLAS LINHAS

### Observação Importante

> "Será realizada uma chamada na API para cada linha retornada na View"

Isso significa:
- **10 linhas na View** = **10 chamadas de API**
- Cada linha será inserida separadamente
- Se uma der erro, as outras continuam sendo processadas

### Exemplo:

**View com 3 linhas**:
```
CODIGO | DESCRICAO      | VALOR
-------|----------------|-------
100    | Método A       | 50.00
200    | Método B       | 75.50
300    | Método C       | 100.00
```

**Resultado**:
- ✅ 3 chamadas de API serão feitas
- ✅ 3 registros serão inseridos na TGFMET
- ✅ Cada resposta será armazenada em `:VAR_RESPOSTA_TGFMET` (sobrescrevendo a anterior)

**⚠️ DICA**: Se precisar armazenar todas as respostas, considere criar uma tabela de log ou usar outra estratégia.

---

## ✅ VALIDAÇÕES NO ANALYTICS

### Antes de Executar

1. ✅ Verifique se o token OAuth está válido (`:VAR_TOKEN`)
2. ✅ Verifique se a View retorna os campos necessários
3. ✅ Verifique se os tipos de dados estão corretos:
   - CODMET: número
   - DESCRICAO: texto
   - VALOR: número (opcional)

### Após Executar

1. ✅ Verifique a variável `:VAR_RESPOSTA_TGFMET`
2. ✅ Procure por `"status": "OK"` na resposta
3. ✅ Se houver erro, verifique a mensagem retornada

---

## 🐛 TRATAMENTO DE ERROS

### Erro: Token Inválido

**Sintoma**: Resposta 401 Unauthorized

**Solução**:
1. Faça login novamente
2. Atualize `:VAR_TOKEN` com o novo token
3. Tente novamente

### Erro: Permissão Negada

**Sintoma**: Resposta 403 Forbidden

**Solução**:
1. Verifique se o perfil tem permissão "tgfmet (POST)"
2. Vá em Configurações → Segurança → Perfis de Acesso
3. Marque a permissão e salve

### Erro: Código Duplicado

**Sintoma**: Resposta com `"status": "ERRO"`

**Solução**:
1. Verifique se o CODMET já existe na TGFMET
2. Use um código diferente
3. Ou trate o erro na sua lógica

---

## 📝 RESUMO RÁPIDO

### Configuração Mínima

1. **Tipo**: POST
2. **URL**: `https://api.sankhya.com.br/v1/tgfmet`
3. **Headers**: 
   - `Authorization: Bearer :VAR_TOKEN`
   - `Content-Type: application/json`
4. **Body** (habilitado):
   ```json
   {
     "CODMET": :CODMET,
     "DESCRICAO": ":DESCRICAO",
     "VALOR": :VALOR,
     "ATIVO": "S"
   }
   ```
5. **Variável de Retorno**: `:VAR_RESPOSTA_TGFMET`

---

**Pronto para usar! 🚀**



