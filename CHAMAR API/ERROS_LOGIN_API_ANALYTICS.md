# 🔴 ERROS IDENTIFICADOS - LOGIN API SANKHYA NO ANALYTICS

## 📋 SITUAÇÃO ATUAL

Você está tentando fazer **login na API do Sankhya** através do Analytics, mas a requisição não está funcionando.

---

## ❌ ERROS IDENTIFICADOS

### 🔴 ERRO CRÍTICO #1: BODY DESABILITADO

**Problema**:
- O toggle **"Habilitar body personalizado"** está **DESLIGADO** (cinza)
- Para fazer **LOGIN**, é **OBRIGATÓRIO** enviar credenciais no BODY da requisição
- Sem o BODY, a API não recebe usuário e senha, então o login falha

**O que está acontecendo**:
```
Sua requisição atual:
POST https://api.sankhya.com.br/login
Headers: Authorization, appkey, accept
Body: ❌ VAZIO (não está sendo enviado)
```

**O que deveria ser**:
```
POST https://api.sankhya.com.br/login
Headers: Authorization, appkey, accept
Body: ✅ { "username": "usuario", "password": "senha" }
```

---

### 🔴 ERRO CRÍTICO #2: HEADER AUTHORIZATION INCORRETO

**Problema**:
- Você está enviando `Authorization: bearer 459fa5bf-6c1a-4f69-a6a9-2ae688b2f3fd`
- **Para LOGIN, você NÃO DEVE enviar token de autorização no header**
- O token é o **RESULTADO** do login, não algo que você envia

**Por quê**:
- O login serve para **OBTER** o token de autenticação
- Se você já tem um token, não precisa fazer login
- Se não tem token, não pode enviar um token no header

**Correção**:
- **Remova o header Authorization** da requisição de login
- Ou use apenas `appkey` (que é para identificar o aplicativo)
- O token virá na **resposta** do login e deve ser salvo em `:VAR_USER`

---

### ⚠️ ERRO POTENCIAL #3: URL PODE ESTAR INCORRETA

**Problema**:
- URL configurada: `https://api.sankhya.com.br/login`
- Pode ser que a URL correta seja diferente

**Possíveis URLs corretas**:
- `https://api.sankhya.com.br/mge/service.sbr?serviceName=MobileLoginSP.login`
- `https://seu-servidor-sankhya:porta/mge/service.sbr?serviceName=MobileLoginSP.login`
- `https://api.sankhya.com.br/api/v1/auth/login`

**Verificar**:
- Consulte a documentação da API do seu Sankhya
- Verifique qual é a URL correta para login

---

### ⚠️ ERRO POTENCIAL #4: FALTA CREDENCIAIS NO BODY

**Problema**:
- Mesmo habilitando o BODY, você precisa **preencher** com usuário e senha
- O formato JSON deve estar correto

**Formato esperado** (geralmente):
```json
{
  "username": "seu_usuario",
  "password": "sua_senha"
}
```

**OU** (pode variar):
```json
{
  "login": "seu_usuario",
  "password": "sua_senha"
}
```

**OU** (formato Sankhya tradicional):
```json
{
  "serviceName": "MobileLoginSP.login",
  "requestBody": {
    "login": "seu_usuario",
    "password": "sua_senha"
  }
}
```

---

## ✅ CORREÇÃO PASSO A PASSO

### PASSO 1: Habilitar o BODY
1. Encontre o toggle **"Habilitar body personalizado"**
2. **LIGUE O TOGGLE** (deve ficar azul/verde)
3. Um campo de texto aparecerá abaixo

### PASSO 2: Remover Header Authorization
1. Na seção **Headers**
2. Encontre o header **"Authorization"** com valor `bearer 459fa5bf-6c1a-4f69-a6a9-2ae688b2f3fd`
3. **Clique no ícone de lixeira** para remover
4. Mantenha apenas:
   - `appkey: e140cf74-9173-4979-ab47-b6b94d451903`
   - `accept: application/json`

### PASSO 3: Preencher o BODY
No campo BODY que apareceu, cole um dos formatos abaixo:

**OPÇÃO A - Formato Simples**:
```json
{
  "username": "seu_usuario_aqui",
  "password": "sua_senha_aqui"
}
```

**OPÇÃO B - Formato Sankhya Service**:
```json
{
  "serviceName": "MobileLoginSP.login",
  "requestBody": {
    "login": "seu_usuario_aqui",
    "password": "sua_senha_aqui"
  }
}
```

**OPÇÃO C - Formato Alternativo**:
```json
{
  "login": "seu_usuario_aqui",
  "password": "sua_senha_aqui"
}
```

**⚠️ IMPORTANTE**: Substitua `seu_usuario_aqui` e `sua_senha_aqui` pelos valores reais!

### PASSO 4: Verificar URL (se necessário)
Se mesmo assim não funcionar, tente estas URLs:

1. **URL Serviço Sankhya**:
   ```
   https://seu-servidor:porta/mge/service.sbr?serviceName=MobileLoginSP.login&outputType=json
   ```

2. **URL API REST**:
   ```
   https://api.sankhya.com.br/api/v1/auth/login
   ```

3. **URL Atual** (manter se for a correta):
   ```
   https://api.sankhya.com.br/login
   ```

### PASSO 5: Verificar Variável de Retorno
- Mantenha `:VAR_USER` na variável de retorno
- Após o login bem-sucedido, o token será armazenado nessa variável
- Use esse token nas próximas chamadas da API

---

## 📊 CONFIGURAÇÃO CORRETA FINAL

### Headers (Correto)
```
Nome: appkey
Valor: e140cf74-9173-4979-ab47-b6b94d451903

Nome: accept
Valor: application/json
```

**❌ REMOVIDO**:
- ~~Authorization: bearer 459fa5bf-6c1a-4f69-a6a9-2ae688b2f3fd~~ (removido)

### Body (Correto)
**✅ Habilitado** (toggle ligado)

```json
{
  "username": "seu_usuario",
  "password": "sua_senha"
}
```

### URL (Verificar se está correta)
```
https://api.sankhya.com.br/login
```
ou
```
https://seu-servidor:porta/mge/service.sbr?serviceName=MobileLoginSP.login&outputType=json
```

### Variável de Retorno
```
:VAR_USER
```

---

## 🎯 RESPOSTA ESPERADA DA API

### Sucesso (HTTP 200)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "user": {
    "id": 123,
    "name": "Nome do Usuário"
  }
}
```

**O token será armazenado em `:VAR_USER`**

### Erro (HTTP 401)
```json
{
  "error": "Invalid credentials",
  "message": "Usuário ou senha inválidos"
}
```

### Erro (HTTP 400)
```json
{
  "error": "Bad Request",
  "message": "Campo 'username' é obrigatório"
}
```

---

## 🔍 COMO TESTAR

### 1. Após Configurar Tudo
1. Clique em **"Testar API"** (botão roxo)
2. Verifique a resposta:
   - **200 OK**: Login funcionou! Token em `:VAR_USER`
   - **401 Unauthorized**: Credenciais inválidas
   - **400 Bad Request**: Body incorreto ou campos faltando

### 2. Se Der Erro 401
- Verifique se usuário e senha estão corretos
- Verifique se a conta está ativa no Sankhya
- Teste as credenciais manualmente no sistema

### 3. Se Der Erro 400
- Verifique se o BODY está habilitado
- Verifique se o JSON está no formato correto
- Verifique se todos os campos obrigatórios estão preenchidos

### 4. Se Der Erro 404
- URL pode estar incorreta
- Tente as URLs alternativas mencionadas acima

---

## 📝 RESUMO DOS ERROS

| # | Erro | Impacto | Prioridade |
|---|------|---------|------------|
| 1 | **Body desabilitado** | ❌ Credenciais não são enviadas | 🔴 CRÍTICO |
| 2 | **Authorization no header** | ❌ API pode rejeitar login | 🔴 CRÍTICO |
| 3 | **URL pode estar incorreta** | ❌ Endpoint não encontrado | ⚠️ MÉDIO |
| 4 | **Formato JSON incorreto** | ❌ API não entende requisição | ⚠️ MÉDIO |

---

## ✅ CHECKLIST DE CORREÇÃO

Antes de testar novamente, verifique:

- [ ] ✅ BODY está **HABILITADO** (toggle ligado)
- [ ] ✅ BODY contém **usuário e senha** em formato JSON
- [ ] ✅ Header **Authorization foi REMOVIDO**
- [ ] ✅ Header **appkey está presente**
- [ ] ✅ Header **accept: application/json está presente**
- [ ] ✅ URL está correta (verificar documentação)
- [ ] ✅ Variável de retorno está configurada (`:VAR_USER`)

---

## 🆘 SE AINDA NÃO FUNCIONAR

### Verificar Documentação da API
1. Consulte a documentação oficial da API Sankhya
2. Verifique qual é o formato exato esperado
3. Verifique qual é a URL correta para login

### Testar com Ferramenta Externa
1. Use Postman ou Insomnia para testar a API
2. Configure exatamente como no Analytics
3. Veja qual é a resposta real da API
4. Compare com o que está configurado no Analytics

### Contatar Suporte
1. Entre em contato com o suporte Sankhya
2. Informe que está tentando fazer login via API
3. Solicite documentação da API de autenticação

---

## 📚 REFERÊNCIAS

Para mais informações sobre configuração de API no Analytics:
- **CONFIGURAR_API_ANALYTICS.txt** - Guia completo de configuração
- **EXEMPLO_JSON_BODY.txt** - Exemplos de JSON para copiar

---

**Última atualização**: 2025-01-02  
**Status**: 🔴 Erros identificados - Aguardando correção



