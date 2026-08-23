# 🚀 API REST CUSTOM SANKHYA - TGFMET

## 📋 VISÃO GERAL

API REST custom no padrão moderno Sankhya para inserir dados na tabela **TGFMET**.

**Características**:
- ✅ Endpoint `/v1/tgfmet`
- ✅ JSON puro (sem XML)
- ✅ OAuth 2.0
- ✅ Action Java com validações

---

## 📁 ARQUIVOS DISPONÍVEIS

| Arquivo | Descrição |
|---------|-----------|
| **ActApiTgfmet.java** | Código Java da Action (copiar e colar) |
| **GUIA_COMPLETO_API_REST.md** | Guia passo a passo completo |
| **EXEMPLO_USO_ANALYTICS.md** | Exemplos específicos para Analytics |
| **README.md** | Este arquivo (visão geral) |

---

## 🚀 INÍCIO RÁPIDO

### 1. Criar Serviço REST
```
Configurações → Integrações → Serviços REST
```
- Nome: `API TGFMET`
- Identificador: `tgfmet`
- Versão: `v1`
- Tipo: `Action`
- Método: `POST`
- Autenticação: `OAuth 2.0`

### 2. Criar Action
```
Configurações → Integrações → Ações
```
- Nome: `ACT_API_TGFMET`
- Identificador: `actApiTgfmet`
- Tipo: `Java`
- Cole o código de `ActApiTgfmet.java`

### 3. Vincular
- No Serviço REST, aba "Ações"
- Selecione `ACT_API_TGFMET`
- Salve

### 4. Permissões
```
Configurações → Segurança → Perfis de Acesso
```
- Marque `tgfmet (POST)` no perfil

### 5. Testar
```bash
curl -X POST https://api.sankhya.com.br/v1/tgfmet \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"CODMET": 999, "DESCRICAO": "Teste"}'
```

---

## 📚 DOCUMENTAÇÃO COMPLETA

Consulte:
- **GUIA_COMPLETO_API_REST.md** - Passo a passo detalhado
- **EXEMPLO_USO_ANALYTICS.md** - Como usar no Analytics

---

**Última atualização**: 2025-01-02



