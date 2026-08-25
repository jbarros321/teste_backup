# 06 — Inteligência Artificial (Google Gemini)

## 1. Regra de governança

> **Só o platform admin configura a IA.** Chave, modelo, temperatura, prompts, cotas e
> habilitação por empresa. Os tenants apenas consomem, dentro do limite que você definir.

Isso é imposto em três camadas independentes (RLS, catálogo de permissões e catálogo de telas) —
detalhadas em `04-seguranca-e-rls.md`, seção 6.

## 2. Onde fica a chave

```bash
supabase secrets set GEMINI_API_KEY=AIza...
supabase secrets set APP_ORIGIN=https://app.seudominio.com
```

A chave **nunca**:
- entra no banco (a tabela guarda só `api_key_ref = 'GEMINI_API_KEY'`, o nome do segredo);
- vai para o bundle do front;
- aparece em log ou em resposta de erro.

Ela é lida em `Deno.env.get("GEMINI_API_KEY")` dentro da Edge Function, que é o único ponto do
sistema com acesso a ela.

## 3. Arquitetura da chamada

```
Navegador                Edge Function                    Postgres            Gemini
    │                          │                              │                  │
    │ POST /gemini-chat        │                              │                  │
    │ (JWT do usuário) ───────>│                              │                  │
    │                          │ auth.getUser()               │                  │
    │                          │ membership? ────────────────>│ (sob RLS)        │
    │                          │ ai_is_available(tenant)? ───>│                  │
    │                          │ contexto de negócio ────────>│ (sob RLS!)       │
    │                          │                              │                  │
    │                          │ generateContent ─────────────────────────────-─>│
    │                          │<──────────────────────────────────────────────-─│
    │                          │ grava ai_messages + ai_usage>│ (service_role)   │
    │<──── resposta ───────────│                              │                  │
```

**O detalhe que importa:** o contexto entregue ao Gemini é lido com o **cliente do próprio
usuário**, ou seja, atravessando o RLS. A IA nunca enxerga uma linha que aquele usuário não
enxergaria na tela. Usar `service_role` para montar contexto seria o caminho mais curto — e
transformaria o chat em um vazamento de dados entre tenants.

## 4. Tabelas

| Tabela | Quem escreve | Quem lê |
|---|---|---|
| `ai_settings` (singleton) | platform admin | platform admin |
| `ai_tenant_settings` | platform admin | platform admin + tenant (só leitura) |
| `ai_prompts` | platform admin | platform admin (Edge Function via service_role) |
| `ai_conversations` | usuário | dono da conversa + `ai.view_all` |
| `ai_messages` | Edge Function | dono da conversa + `ai.view_all` |
| `ai_usage` | Edge Function | platform admin + admin do tenant |
| `ai_actions` | Edge Function / usuário | solicitante + `ai.view_all` |

## 5. Modelos e parâmetros

Configuráveis em `ai_settings` — trocar de modelo é um `update`, não um deploy:

| Campo | Padrão | Uso |
|---|---|---|
| `model_default` | `gemini-2.5-flash` | chat, resumos, classificação — barato e rápido |
| `model_advanced` | `gemini-2.5-pro` | geração de projeto e análise de risco |
| `temperature` | `0.30` | respostas previsíveis; suba só para brainstorming |
| `max_output_tokens` | `4096` | |
| `top_p` | `0.95` | |
| `monthly_token_cap` | 5.000.000 | teto global da plataforma |

Por empresa (`ai_tenant_settings`): `is_enabled`, `model_override`, `monthly_token_cap`,
`allow_agent_write`.

## 6. Cota

```sql
select public.ai_is_available('<tenant>');  -- plano permite? habilitado? sobrou cota?
select public.ai_quota_left('<tenant>');    -- tokens restantes no mês
```

A Edge Function consulta antes de cada chamada e grava o consumo real depois — inclusive quando dá
erro, para que uma falha em loop não fique invisível.

## 7. Funcionalidades

### 7.1 Chat contextual — `gemini-chat`

```http
POST /functions/v1/gemini-chat
Authorization: Bearer <jwt>

{ "tenant_id": "...", "conversation_id": null, "message": "Quais tarefas estão atrasadas?" }
```

Recebe automaticamente como contexto: indicadores do período, tarefas atrasadas, projetos com
risco calculado e carga da equipe. Perguntas que funcionam de imediato:

- "Quais tarefas estão atrasadas?"
- "Qual projeto tem maior risco e por quê?"
- "Resuma as atividades da equipe esta semana."
- "Quem está sobrecarregado?"
- "Que tarefas estão sem responsável?"

### 7.2 Geração de projeto — `gemini-agent`, `action: "generate_project"`

```json
{ "tenant_id": "...", "action": "generate_project",
  "prompt": "Crie um projeto para implantação de um CRM em 90 dias" }
```

Retorna uma **proposta** em `ai_actions` com status `proposed` e um preview:

```json
{
  "project": { "name": "Implantação de CRM", "duration_days": 90 },
  "phases": [
    { "name": "Discovery", "tasks": [
      { "title": "Mapear processos comerciais", "estimate_minutes": 960,
        "start_offset_days": 0, "duration_days": 5, "priority": "high",
        "subtasks": [...], "checklist": [...] }
    ]}
  ]
}
```

Nada é gravado ainda. O usuário revisa na tela e confirma:

```json
{ "tenant_id": "...", "action": "apply", "ai_action_id": "..." }
```

A gravação usa o cliente do usuário — se ele não tiver `project.create`, o RLS recusa. **A IA nunca
tem mais poder do que quem a acionou.**

### 7.3 Resumo — `action: "summarize"`

Resume comentários de uma tarefa, atividade de um projeto ou um documento. Máximo de 8 linhas, com
decisões, pendências, riscos e próximos passos.

### 7.4 Assistente de produtividade — `action: "risk_scan"`

Devolve JSON com achados classificados por severidade: tarefas atrasadas, projetos em risco,
pessoas sobrecarregadas, gargalos e atividades sem responsável. Pode rodar por `pg_cron` semanal e
virar notificação para os gestores.

## 8. Prompts

Versionados em `ai_prompts (key, version, content, is_active)`. Trocar um prompt é um `insert` com
`version + 1`, não um deploy. Chaves iniciais: `chat_default`, `project_generator`, `summarizer`,
`risk_analyst`.

Todos carregam a mesma instrução central: **usar apenas o contexto fornecido e nunca inventar
dados**. Quando a informação não está no contexto, a resposta correta é dizer que não encontrou.

## 9. Custos e monitoramento

`ai_usage` registra por chamada: tenant, usuário, feature, modelo, tokens de entrada e saída,
sucesso e erro. A tela `admin.usage` mostra consumo por empresa, por feature e por dia, com projeção
de fechamento do mês.

Boas práticas já aplicadas no código:
- histórico limitado às últimas 20 mensagens da conversa;
- contexto recortado (25 projetos, 25 atrasos, 50 pessoas);
- `flash` como padrão, `pro` só onde a qualidade paga a diferença;
- `responseMimeType: application/json` nas ações estruturadas, evitando reprocessar texto solto.

## 10. Limites conhecidos

- Sem streaming na versão atual (resposta chega inteira). Para streaming, troque
  `:generateContent` por `:streamGenerateContent?alt=sse` e repasse o `ReadableStream`.
- Sem embeddings/RAG na Fase 4. Quando a base de documentos crescer, adicione `pgvector` e uma
  tabela `document_chunks` com busca por similaridade antes de montar o contexto.
- O contexto é montado por consultas fixas. Function calling do Gemini (deixar o modelo escolher a
  consulta) é evolução natural — e exige uma lista branca de funções, nunca SQL livre.
