# 🚀 GUIA COMPLETO - API REST CUSTOM SANKHYA (TGFMET)

## 📋 VISÃO GERAL RÁPIDA

Criar uma **API REST custom** no Sankhya seguindo o padrão moderno:
- ✅ Endpoint `/v1/...`
- ✅ JSON puro (sem XML)
- ✅ Sem `service.sbr`
- ✅ OAuth 2.0 (mesmo token que você já usa)
- ✅ Action Java para lógica de negócio

**Resultado Final**:
```
POST https://api.sankhya.com.br/v1/tgfmet
```

---

## 🎯 PASSO A PASSO COMPLETO

### 1️⃣ CRIAR O SERVIÇO REST

#### 📍 Caminho no Sankhya
```
Configurações → Integrações → Serviços REST
```

#### ➕ Novo Serviço

Preencha assim:

| Campo | Valor |
|-------|-------|
| **Nome** | API TGFMET |
| **Identificador** | `tgfmet` |
| **Versão** | `v1` |
| **Ativo** | ✅ (marcado) |
| **Tipo** | `Action` |
| **Método HTTP** | `POST` |
| **Autenticação** | `OAuth 2.0` |

#### 📌 IMPORTANTE
- O **Identificador** (`tgfmet`) vira parte da URL
- Endpoint gerado: `https://api.sankhya.com.br/v1/tgfmet`

---

### 2️⃣ CRIAR A ACTION (JAVA)

#### 📍 Caminho
```
Configurações → Integrações → Ações
```

#### ➕ Nova Ação

| Campo | Valor |
|-------|-------|
| **Nome** | ACT_API_TGFMET |
| **Identificador** | `actApiTgfmet` |
| **Tipo** | `Java` |
| **Ativo** | ✅ (marcado) |

#### 📝 Código Java

Copie o código do arquivo **`ActApiTgfmet.java`** e cole na Action.

**Localização do arquivo**: `Analytics/API_REST_GOL_TGFMET/ActApiTgfmet.java`

**O que o código faz**:
- ✅ Recebe JSON no body
- ✅ Valida campos obrigatórios (CODMET, DESCRICAO)
- ✅ Verifica se CODMET já existe (evita duplicação)
- ✅ Insere registro na tabela TGFMET
- ✅ Retorna JSON com status e dados inseridos

---

### 3️⃣ VINCULAR SERVIÇO REST À ACTION

#### 📍 Volte no Serviço REST criado

1. Abra o serviço **"API TGFMET"**
2. Vá na aba **"Ações"**
3. Selecione a action **"ACT_API_TGFMET"**
4. **Salve**

---

### 4️⃣ CONFIGURAR PERMISSÕES (MUITO IMPORTANTE!)

#### 📍 Caminho
```
Configurações → Segurança → Perfis de Acesso
```

#### 🔐 Configurar Perfil

1. Selecione o **perfil do usuário** que vai usar o token OAuth
2. Vá na aba **"Serviços REST"**
3. Marque a permissão:
   - ✅ **tgfmet (POST)**
4. **Salve**

#### ⚠️ SEM ISSO → ERRO 401 ou 403
Se não configurar as permissões, a API retornará erro de autorização mesmo com token válido.

---

### 5️⃣ TESTAR A API

#### 🧪 Teste via CURL

```bash
curl --request POST \
  --url https://api.sankhya.com.br/v1/tgfmet \
  --header 'Authorization: Bearer SEU_TOKEN_OAUTH' \
  --header 'Content-Type: application/json' \
  --data '{
    "CODMET": 999,
    "DESCRICAO": "Teste de inserção via API REST",
    "VALOR": 100.50,
    "ATIVO": "S"
  }'
```

#### ✅ Resposta Esperada (Sucesso)

```json
{
  "status": "OK",
  "mensagem": "Registro inserido com sucesso na TGFMET",
  "CODMET": 999,
  "DESCRICAO": "Teste de inserção via API REST",
  "VALOR": 100.50,
  "ATIVO": "S"
}
```

#### ❌ Resposta Esperada (Erro - Código Duplicado)

```json
{
  "status": "ERRO",
  "mensagem": "Código CODMET 999 já existe na tabela TGFMET",
  "codigo": 999
}
```

#### ❌ Resposta Esperada (Erro - Campo Obrigatório)

```json
{
  "error": "Campo obrigatório 'CODMET' não informado"
}
```

---

## 📊 FORMATO DO JSON (REQUEST)

### Campos Obrigatórios
- **CODMET**: Número (BigDecimal) - Código do método (deve ser único)
- **DESCRICAO**: String - Descrição do método (não pode ser vazio)

### Campos Opcionais
- **VALOR**: Número (BigDecimal) - Valor do método (padrão: 0)
- **ATIVO**: String - Status ativo/inativo (padrão: "S")
  - Valores aceitos: "S" ou "N"

### Exemplo Completo

```json
{
  "CODMET": 123,
  "DESCRICAO": "Método de teste",
  "VALOR": 150.75,
  "ATIVO": "S"
}
```

### Exemplo Mínimo

```json
{
  "CODMET": 456,
  "DESCRICAO": "Método simples"
}
```

---

## 🔧 USAR NO ANALYTICS

### Configuração no Analytics

#### 1. Tipo de Chamada
```
POST
```

#### 2. URL
```
https://api.sankhya.com.br/v1/tgfmet
```

#### 3. Headers
```
Authorization: Bearer :VAR_TOKEN
Content-Type: application/json
```

**Nota**: `:VAR_TOKEN` deve conter o token OAuth obtido no login.

#### 4. Body (Habilitado)
```json
{
  "CODMET": :CODMET,
  "DESCRICAO": ":DESCRICAO",
  "VALOR": :VALOR,
  "ATIVO": "S"
}
```

**Nota**: Use variáveis da View (`:CODMET`, `:DESCRICAO`, etc.)

#### 5. Variável de Retorno
```
:VAR_RESPOSTA_TGFMET
```

---

## 🎯 VANTAGENS DESSE MODELO

✅ **JSON simples** - Sem XML, fácil de trabalhar
✅ **Mesmo token OAuth** - Usa o mesmo token que outras APIs
✅ **Sem service.sbr** - Endpoint direto `/v1/...`
✅ **Controle total** - Lógica de negócio na Action Java
✅ **Escalável** - Fácil adicionar validações e regras
✅ **Padrão atual** - Segue padrão moderno Sankhya

---

## ⚠️ VALIDAÇÕES IMPLEMENTADAS

### Validações Automáticas

1. ✅ **CODMET obrigatório** - Deve ser informado
2. ✅ **DESCRICAO obrigatória** - Deve ser informada e não vazia
3. ✅ **CODMET único** - Verifica se já existe antes de inserir
4. ✅ **ATIVO válido** - Aceita apenas "S" ou "N" (padrão: "S")
5. ✅ **VALOR numérico** - Valida formato numérico

### Tratamento de Erros

- **Campo obrigatório ausente**: Retorna erro 400 com mensagem
- **Código duplicado**: Retorna JSON com status "ERRO" e mensagem
- **Erro de banco**: Retorna erro 500 com detalhes

---

## 🔍 EXEMPLOS DE USO

### Exemplo 1: Inserir Registro Simples

**Request**:
```json
{
  "CODMET": 100,
  "DESCRICAO": "Método de Vendas Online"
}
```

**Response**:
```json
{
  "status": "OK",
  "mensagem": "Registro inserido com sucesso na TGFMET",
  "CODMET": 100,
  "DESCRICAO": "Método de Vendas Online",
  "VALOR": 0,
  "ATIVO": "S"
}
```

### Exemplo 2: Inserir Registro Completo

**Request**:
```json
{
  "CODMET": 200,
  "DESCRICAO": "Método Premium",
  "VALOR": 500.00,
  "ATIVO": "S"
}
```

**Response**:
```json
{
  "status": "OK",
  "mensagem": "Registro inserido com sucesso na TGFMET",
  "CODMET": 200,
  "DESCRICAO": "Método Premium",
  "VALOR": 500.00,
  "ATIVO": "S"
}
```

### Exemplo 3: Tentar Inserir Código Duplicado

**Request**:
```json
{
  "CODMET": 100,
  "DESCRICAO": "Tentativa de duplicar"
}
```

**Response**:
```json
{
  "status": "ERRO",
  "mensagem": "Código CODMET 100 já existe na tabela TGFMET",
  "codigo": 100
}
```

---

## 🐛 TROUBLESHOOTING

### Erro 401 (Unauthorized)
**Causa**: Token inválido ou ausente
**Solução**: 
- Verifique se o token OAuth está correto
- Verifique se o token não expirou
- Faça login novamente para obter novo token

### Erro 403 (Forbidden)
**Causa**: Permissão não configurada
**Solução**: 
- Vá em Configurações → Segurança → Perfis de Acesso
- Marque a permissão "tgfmet (POST)" no perfil do usuário

### Erro 404 (Not Found)
**Causa**: Endpoint não encontrado
**Solução**: 
- Verifique se o Serviço REST está ativo
- Verifique se o identificador está correto (`tgfmet`)
- Verifique se a versão está correta (`v1`)

### Erro 400 (Bad Request)
**Causa**: JSON inválido ou campo obrigatório ausente
**Solução**: 
- Verifique se o JSON está bem formatado
- Verifique se CODMET e DESCRICAO estão presentes
- Verifique se os tipos de dados estão corretos

### Erro 500 (Internal Server Error)
**Causa**: Erro na Action Java
**Solução**: 
- Verifique os logs do Sankhya
- Verifique se a tabela TGFMET existe
- Verifique se há erros de sintaxe no código Java

---

## 📚 ARQUIVOS DISPONÍVEIS

| Arquivo | Descrição |
|---------|-----------|
| **ActApiTgfmet.java** | Código Java da Action (copiar e colar) |
| **GUIA_COMPLETO_API_REST.md** | Este arquivo (guia completo) |
| **EXEMPLO_USO_ANALYTICS.md** | Exemplos específicos para Analytics |

---

## ✅ CHECKLIST FINAL

Antes de usar no Analytics, verifique:

- [ ] ✅ Serviço REST criado e ativo
- [ ] ✅ Action criada e ativa
- [ ] ✅ Serviço REST vinculado à Action
- [ ] ✅ Permissões configuradas no perfil
- [ ] ✅ Teste via CURL funcionando
- [ ] ✅ Token OAuth válido
- [ ] ✅ URL correta no Analytics

---

**Última atualização**: 2025-01-02  
**Versão**: 1.0.0



