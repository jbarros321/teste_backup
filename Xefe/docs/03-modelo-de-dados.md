# 03 — Modelo de Dados

Schema executável em `supabase/migrations/`. Este documento explica as decisões.

## 1. Regra de ouro

> Toda tabela operacional tem `tenant_id uuid NOT NULL` e um índice que começa por ele.

Isso vale mesmo quando o `tenant_id` seria dedutível por join (ex.: `task_assignees`). A
desnormalização é deliberada: permite que a policy de RLS filtre sem join e que o índice sirva
diretamente à consulta. Um join a mais dentro de uma policy é um join executado por linha.

## 2. Grupos de tabelas

### Plataforma (fora do tenant)
`platform_admins`, `plans`, `permissions`, `screens`, `ai_settings`, `ai_prompts`

### Empresa
`tenants`, `tenant_domains`, `subscriptions`, `usage_counters`, `ai_tenant_settings`

### Pessoas e acesso
`profiles`, `memberships`, `membership_permissions`, `invitations`, `roles`, `role_permissions`,
`screen_access`, `resource_shares`, `departments`, `teams`, `team_members`

### Estrutura de trabalho
`spaces`, `folders`, `lists`, `projects`, `project_members`, `task_statuses`, `views`, `templates`,
`tags`, `custom_fields`, `custom_field_values`

### Execução
`tasks`, `task_assignees`, `task_watchers`, `task_tags`, `task_relations`, `checklists`,
`checklist_items`, `tenant_sequences`

### Colaboração
`comments`, `comment_reactions`, `mentions`, `attachments`, `documents`, `document_versions`

### Tempo e resultado
`time_entries`, `timesheets`, `goals`, `goal_updates`, `okr_objectives`, `okr_key_results`

### Plataforma operacional
`dashboards`, `dashboard_widgets`, `automations`, `automation_runs`, `notifications`,
`notification_preferences`, `notification_channels`, `integrations`, `api_keys`, `webhooks`,
`webhook_deliveries`, `activity_logs`, `audit_logs`

### IA
`ai_settings`, `ai_tenant_settings`, `ai_prompts`, `ai_conversations`, `ai_messages`, `ai_usage`,
`ai_actions`

## 3. Relações centrais

```
tenants 1─┬─N tenant_domains       (domínio de e-mail -> entrada automática)
          ├─N memberships ─1 profiles ─1 auth.users
          ├─N departments ─N teams ─N team_members
          ├─N spaces ─N folders ─N lists ─N tasks
          │                              └─N tasks (parent_task_id = subtarefa)
          ├─N projects ─N lists / tasks
          └─N ...

tasks 1─┬─N task_assignees
        ├─N task_watchers
        ├─N task_tags ─1 tags
        ├─N checklists ─N checklist_items
        ├─N comments        (entity='task')
        ├─N attachments     (entity='task')
        └─N task_relations  (blocks / depends_on / ...)
```

## 4. Decisões que merecem explicação

**Subtarefa não é tabela separada.** É `tasks.parent_task_id`. Um trigger limita a 3 níveis. Assim
toda funcionalidade de tarefa (comentário, anexo, tempo, checklist) vale automaticamente para
subtarefa, sem duplicar código nem tabela.

**Status é dado, não enum.** `task_statuses` permite fluxos diferentes por espaço/lista, e a coluna
`category` (`todo/in_progress/done/canceled`) mantém as métricas comparáveis entre times que
nomearam seus status de formas diferentes.

**`position numeric` em vez de `int`.** Reordenar no Kanban vira uma única linha alterada
(média entre vizinhos: `(1000 + 1100) / 2 = 1050`), sem reindexar a coluna inteira.

**Polimorfismo por (`entity`, `entity_id`).** `comments`, `attachments`, `mentions`,
`custom_field_values` e `resource_shares` apontam para qualquer entidade. Sem FK, mas com o `enum
entity_type` restringindo os valores e índice composto `(tenant_id, entity, entity_id)`. A
alternativa (uma tabela de comentários por entidade) multiplicaria o schema por seis.

**`seq` por tenant.** Cada tarefa recebe um número visível (`TASK-128`) por empresa, gerado em
`tenant_sequences`. UUID é bom para chave, ruim para conversa humana.

**Exclusão é lógica.** `deleted_at` em tasks, projects, documents, attachments; expurgo físico após
30 dias por `fn_purge_trash`. Recuperar é operação de rotina; perder dado não é.

**`duration_minutes` é coluna gerada.** Calculada pelo banco em `time_entries`, nunca pelo cliente.
Cliente não é fonte de verdade para número que vira fatura.

**Dois logs distintos.** `activity_logs` é o feed que o usuário lê ("Maria mudou o status");
`audit_logs` é a trilha técnica com `old_data`/`new_data` em jsonb, para investigação e compliance.
Misturar os dois torna o feed ilegível e a auditoria incompleta.

## 5. Índices que sustentam as telas

| Tela | Índice |
|---|---|
| Kanban / Lista | `tasks (tenant_id, list_id, position) where deleted_at is null` |
| Filtro por status | `tasks (tenant_id, status_id) where deleted_at is null` |
| Prazos e atrasos | `tasks (tenant_id, due_date) where deleted_at is null` |
| Minhas tarefas | `task_assignees (tenant_id, user_id)` |
| Busca global | `tasks using gin (search_tsv)` e `documents using gin (search_tsv)` |
| Timesheet | `time_entries (tenant_id, user_id, started_at desc)` |
| Notificações | `notifications (user_id, read_at, created_at desc)` |
| Feed de atividade | `activity_logs (tenant_id, entity, entity_id, created_at desc)` |

## 6. Índices únicos que codificam regra de negócio

```sql
-- Um único responsável principal por tarefa
create unique index task_one_primary_assignee on task_assignees (task_id) where is_primary;

-- Um único cronômetro rodando por usuário
create unique index one_running_timer_per_user on time_entries (user_id) where ended_at is null;

-- Um domínio de e-mail pertence a uma única empresa
alter table tenant_domains add constraint tenant_domains_domain_key unique (domain);
```

Regra que o banco garante não precisa ser lembrada pelo desenvolvedor da próxima tela.
