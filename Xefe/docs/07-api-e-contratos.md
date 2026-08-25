# 07 — API e Contratos

## 1. Duas portas

| Porta | Base | Autenticação | Uso |
|---|---|---|---|
| **PostgREST** | `https://<ref>.supabase.co/rest/v1` | JWT do usuário (`anon` key + Bearer) | CRUD do app |
| **Edge Functions** | `https://<ref>.supabase.co/functions/v1` | JWT do usuário | IA, convites, integrações |

Não existe camada REST intermediária escrita à mão. O CRUD é gerado pelo PostgREST sobre o schema,
e o RLS é o controlador de acesso. Menos código, menos lugar para errar.

## 2. CRUD via supabase-js

```ts
// Listar tarefas do Kanban de uma lista
const { data } = await supabase
  .from('v_tasks_full')
  .select('*')
  .eq('list_id', listId)
  .order('position')

// Criar tarefa
const { data } = await supabase.from('tasks').insert({
  tenant_id, list_id: listId, title: 'Criar tela de indicadores',
  priority: 'high', due_date: '2026-09-10T18:00:00Z'
}).select().single()

// Mover card no Kanban (posição estável, uma linha alterada)
await supabase.rpc('fn_move_task', {
  p_task: taskId, p_list: listId, p_status: statusId,
  p_before: prevId, p_after: nextId
})

// Busca global
const { data } = await supabase.rpc('fn_search', { p_tenant: tenantId, p_query: 'fluxo de caixa' })
```

## 3. API REST equivalente (integrações externas)

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/rest/v1/tasks?select=*&tenant_id=eq.{id}` | listar tarefas |
| `GET` | `/rest/v1/tasks?id=eq.{id}` | obter tarefa |
| `POST` | `/rest/v1/tasks` | criar |
| `PATCH` | `/rest/v1/tasks?id=eq.{id}` | atualizar |
| `DELETE` | `/rest/v1/tasks?id=eq.{id}` | excluir |

Mesmo padrão para `projects`, `lists`, `spaces`, `comments`, `documents`, `goals`, `time_entries`,
`memberships`, `dashboards`. Filtros, ordenação, paginação (`Range`) e `select` aninhado são os do
PostgREST:

```
GET /rest/v1/tasks?select=id,title,task_assignees(user_id,profiles(full_name))&status_id=eq.{id}
```

## 4. RPCs de negócio

| RPC | Assinatura | Retorno |
|---|---|---|
| `me()` | — | perfil, memberships, papel, telas e permissões |
| `fn_dashboard_counters` | `(p_tenant, p_from, p_to)` | jsonb com KPIs |
| `fn_search` | `(p_tenant, p_query, p_limit)` | resultados multi-entidade |
| `fn_critical_path` | `(p_project)` | tarefas do caminho crítico |
| `fn_move_task` | `(p_task, p_list, p_status, p_before, p_after)` | nova posição |
| `ai_is_available` | `(p_tenant)` | boolean |
| `ai_quota_left` | `(p_tenant)` | tokens restantes |
| `fn_bootstrap_tenant` | `(nome, slug, domínio, e-mail do owner, plano)` | uuid do tenant (super admin) |

## 5. Edge Functions

### `POST /functions/v1/gemini-chat`
```json
{ "tenant_id": "uuid", "conversation_id": "uuid|null", "message": "texto",
  "context": { "project_id": "uuid" } }
```
```json
{ "conversation_id": "uuid", "answer": "texto",
  "usage": { "input": 1820, "output": 340, "latency_ms": 1450 } }
```

### `POST /functions/v1/gemini-agent`
```json
{ "tenant_id": "uuid", "action": "generate_project|summarize|risk_scan|apply",
  "prompt": "texto", "entity": "task", "entity_id": "uuid", "ai_action_id": "uuid" }
```

### `POST /functions/v1/invite-accept`
```json
{ "token": "..." }  ->  { "tenant_id": "uuid", "role": "member" }
```

## 6. API Keys para sistemas externos

Criadas em `api_keys` com escopo e rate limit. A chave em claro é exibida **uma única vez**; o banco
guarda apenas `key_hash` (SHA-256) e `key_prefix`.

```http
POST /functions/v1/api-gateway/tasks
X-Xefe-Key: xf_live_8f2c...
```

O gateway valida o hash, checa `scopes`, aplica rate limit e executa a operação com o tenant da
chave. Chave revogada (`revoked_at`) ou expirada é recusada com `401`.

## 7. Webhooks de saída

Eventos: `task.created`, `task.updated`, `task.completed`, `task.assigned`, `project.created`,
`project.completed`, `comment.created`, `goal.updated`, `automation.executed`.

```http
POST https://cliente.com/webhook
X-Xefe-Event: task.completed
X-Xefe-Signature: sha256=<hmac(secret, body)>
X-Xefe-Delivery: <uuid>
```

```json
{
  "event": "task.completed",
  "tenant_id": "uuid",
  "occurred_at": "2026-08-25T14:22:10Z",
  "data": { "id": "uuid", "seq": 128, "title": "Criar tela de indicadores",
            "project_id": "uuid", "completed_at": "2026-08-25T14:22:09Z" }
}
```

Retry exponencial (1 min, 5 min, 25 min, 2 h, 10 h), até 5 tentativas, registrado em
`webhook_deliveries`. O consumidor deve validar o HMAC e tratar entregas duplicadas por
`X-Xefe-Delivery` — a entrega é *at least once*.

## 8. Realtime

```ts
supabase.channel(`tenant:${tenantId}:tasks`)
  .on('postgres_changes',
      { event: '*', schema: 'public', table: 'tasks', filter: `tenant_id=eq.${tenantId}` },
      payload => queryClient.invalidateQueries(['tasks']))
  .subscribe()
```

O Realtime respeita RLS: o cliente só recebe as linhas que poderia ler por `select`.

## 9. Erros

| HTTP | Situação | Ação no front |
|---|---|---|
| `401` | JWT ausente/expirado | refresh e retry; se falhar, logout |
| `403` | RLS recusou | mensagem "sem permissão"; revisar papel |
| `409` | conflito de unicidade | mostrar o registro existente |
| `422` | violação de check/trigger | exibir a mensagem do banco (ex.: `PLAN_LIMIT_EXCEEDED`) |
| `429` | rate limit | backoff exponencial |
| `502` | falha na chamada ao Gemini | oferecer nova tentativa |

Erros de regra de negócio chegam com a mensagem levantada pelo trigger, o que permite tratá-los por
prefixo (`PLAN_LIMIT_EXCEEDED:`) sem inventar um catálogo paralelo de códigos.
