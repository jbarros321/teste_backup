# 01 — Escopo Funcional Detalhado

Cada módulo abaixo lista: **objetivo → funcionalidades → regras de negócio → tabelas → telas**.

---

## M01 — Empresas e Multi-Tenant

**Objetivo:** permitir que várias empresas usem a mesma plataforma com separação total de dados.

**Funcionalidades:** cadastro de empresa (nome, CNPJ, logo, segmento), plano contratado, status,
limites (usuários, armazenamento, projetos, automações, tokens de IA), configurações
personalizadas (fuso, moeda, semana útil, idioma), domínios de e-mail vinculados.

**Regras:**
- R1. Toda tabela operacional carrega `tenant_id NOT NULL`.
- R2. Nenhuma consulta pode retornar linha de outro tenant — garantido por RLS, não pelo app.
- R3. Um domínio de e-mail pertence a **um único** tenant (`unique(domain)`).
- R4. O domínio precisa ser verificado (`verified_at`) antes do auto-join valer.
- R5. Ao atingir um limite do plano, a criação é bloqueada com erro `PLAN_LIMIT_EXCEEDED`.

**Tabelas:** `tenants`, `tenant_domains`, `tenant_settings`, `plans`, `subscriptions`, `usage_counters`.

**Telas:** `admin.tenants`, `settings.company`, `settings.domains`, `settings.plan`.

---

## M02 — Usuários

**Objetivo:** gerir o ciclo de vida das pessoas na plataforma.

**Cadastro:** nome, e-mail, telefone, foto, cargo, departamento, empresa, status, data de cadastro,
último acesso, fuso horário, idioma, capacidade semanal (horas).

**Status:** `active`, `inactive`, `blocked`, `invited`.

**Funcionalidades:** criar, editar, bloquear, excluir (soft delete), convidar por e-mail, reenviar
convite, redefinir senha, alterar foto, alterar permissões, transferir responsabilidades.

**Regras:**
- R6. Signup com e-mail de domínio verificado gera membership automática com o `default_role` do domínio.
- R7. Signup com e-mail fora de domínio conhecido só entra via convite (`invitations`).
- R8. Excluir usuário nunca apaga histórico: `memberships.status = 'inactive'` mais reatribuição de tarefas.
- R9. Um mesmo e-mail pode ser membro de vários tenants (memberships N:N).

**Tabelas:** `profiles`, `memberships`, `invitations`.

**Telas:** `settings.users`, `settings.invites`, `profile.me`.

---

## M03 — Perfis e Permissões

**Perfis padrão:** Proprietário (`owner`), Administrador (`admin`), Gestor (`manager`),
Líder (`leader`), Colaborador (`member`), Convidado (`guest`) — mais o `platform_admin` global.

**Granularidade:** permissões por tenant, espaço, pasta, lista, projeto, tarefa, documento e dashboard.

**Ações:** `view`, `create`, `update`, `delete`, `approve`, `manage_users`, `manage_settings`,
`export`, `manage_automation`, `manage_ai`, `manage_integrations`.

**Regras:**
- R10. `manage_ai` é **exclusiva do platform_admin** — nenhum papel de tenant a recebe.
- R11. O `owner` do domínio configura `screen_access` (quais telas cada papel vê) e os convites.
- R12. Permissão pode ser concedida por papel (`role_permissions`) e sobrescrita por usuário
      (`membership_permissions`, com efeito `allow` ou `deny`; `deny` sempre vence).
- R13. Convidado só acessa recursos presentes em `resource_shares`.

**Tabelas:** `roles`, `permissions`, `role_permissions`, `membership_permissions`, `screens`,
`screen_access`, `resource_shares`.

**Telas:** `settings.roles`, `settings.screens`, `settings.permissions`.

---

## M04 — Equipes e Departamentos

Criar equipe, adicionar usuários, definir gestor e líderes, criar departamentos hierárquicos,
visualizar membros, carga de trabalho e produtividade.

**Regras:**
- R14. Departamento é auto-relacionado (`parent_id`) e permite N níveis.
- R15. Um usuário pode pertencer a várias equipes; a capacidade é do usuário, não da equipe.

**Tabelas:** `departments`, `teams`, `team_members`.

---

## M05 — Projetos

**Cadastro:** nome, descrição, responsável, equipe, data inicial e final, status, prioridade,
orçamento, cliente, tags, cor, ícone.

**Indicadores calculados:** progresso (%), total de tarefas, concluídas, atrasadas, horas
trabalhadas versus estimadas, usuários envolvidos, custo realizado versus orçado, risco.

**Regras:**
- R16. Progresso = tarefas concluídas dividido por tarefas totais, ponderado por `estimate_minutes` quando houver.
- R17. Projeto em risco quando (prazo menor que 15 dias e progresso menor que 60%) ou (horas realizadas acima de 110% do estimado).

**Tabelas:** `projects`, `project_members`, view `v_project_stats`.

---

## M06 — Tarefas e Subtarefas

**Cadastro:** título, descrição (rich text), responsável, participantes, projeto, lista, status,
prioridade, data inicial, data final, horário, tempo estimado, tags, anexos, checklists, campos
personalizados.

**Prioridades:** `urgent`, `high`, `normal`, `low`, `none`.

**Status:** configuráveis por tenant, espaço ou lista, cada um mapeado a uma categoria
(`todo`, `in_progress`, `done`, `canceled`) para permitir métricas comparáveis entre times.

**Regras:**
- R18. Subtarefa é uma tarefa com `parent_task_id`; profundidade máxima de 3 níveis, validada por trigger.
- R19. Concluir tarefa-pai exige subtarefas concluídas quando `lists.require_subtasks_done = true`.
- R20. `position` (numeric) define a ordem no Kanban e na Lista — reordenação por média entre vizinhos.
- R21. Toda alteração de campo relevante gera linha em `activity_logs` via trigger.

**Tabelas:** `task_statuses`, `tasks`, `task_assignees`, `task_watchers`, `task_relations`,
`checklists`, `checklist_items`, `tags`, `task_tags`, `custom_fields`, `custom_field_values`.

---

## M07 — Checklists

Múltiplos checklists por tarefa, com itens ordenáveis, responsável e prazo por item.
Percentual de conclusão calculado automaticamente por trigger no campo `checklists.progress`.

---

## M08 — Comentários e Colaboração

Comentários encadeados (`parent_id`), respostas, menções `@usuario` e `@equipe`, reações, anexos,
links e histórico. Menção gera notificação e, opcionalmente, e-mail.

**Tabelas:** `comments`, `comment_reactions`, `mentions`, `notifications`.

---

## M09 — Anexos e Arquivos

Upload em tarefas, projetos, comentários, documentos e itens de checklist. Formatos: PDF, Office,
imagens, vídeos, compactados. Armazenamento em **Supabase Storage**, bucket privado por tenant,
com URLs assinadas de curta duração.

**Regras:**
- R22. Caminho canônico: `tenant/{tenant_id}/{entity}/{entity_id}/{uuid}-{filename}`.
- R23. Tamanho máximo por arquivo e cota total vêm do plano (`plans.max_storage_mb`).
- R24. Exclusão é lógica por 30 dias (lixeira) antes da remoção física.

**Tabelas:** `attachments` mais policies dedicadas em `storage.objects`.

---

## M10 — Visualizações

| Visão | Recursos |
|---|---|
| **Lista** | Colunas configuráveis, ordenação, filtros, agrupamento, edição inline, seleção em massa |
| **Kanban** | Colunas por status, drag and drop, WIP limit, swimlanes por responsável ou prioridade |
| **Calendário** | Dia, semana e mês; arrastar para alterar datas; criação inline |
| **Gantt** | Linha do tempo, dependências, duração, progresso, caminho crítico, reprogramação automática |
| **Timeline** | Faixas por responsável ou projeto para planejamento macro |
| **Tabela** | Grade densa tipo planilha com campos personalizados |

**Regras:**
- R25. Cada visão é uma linha em `views` com `config jsonb` (filtros, colunas, agrupamento) e escopo
      pessoal ou compartilhado.
- R26. Caminho crítico calculado no servidor pela RPC `fn_critical_path(project_id)`.

---

## M11 — Metas e OKRs

Metas com nome, objetivo, responsável, período, valor inicial, valor alvo, valor atual, indicador e
unidade. OKRs com objetivo mais resultados-chave; cada KR pode ser alimentado manualmente ou
vinculado a uma métrica automática, como percentual de tarefas concluídas.

**Tabelas:** `goals`, `goal_updates`, `okr_objectives`, `okr_key_results`.

---

## M12 — Dashboards

Cards: total de tarefas, concluídas, atrasadas, por responsável, por projeto, por status, horas
trabalhadas, produtividade, progresso e metas. Gráficos: pizza, barras, linha, área, indicadores e
tabelas. Dashboard personalizado com grid arrastável, filtros, compartilhamento e exportação.

**Tabelas:** `dashboards`, `dashboard_widgets` e views de agregação.

---

## M13 — Documentos

Editor colaborativo (rich text), tabelas, imagens, links, comentários, histórico de versões,
compartilhamento com link público opcional e árvore de páginas aninhadas.

**Tabelas:** `documents`, `document_versions`, `document_shares`.

---

## M14 — Automações

Modelo **QUANDO → CONDIÇÃO → AÇÃO**, com execução assíncrona e log de cada disparo.

Gatilhos: `task.created`, `task.status_changed`, `task.assigned`, `task.due_soon`, `task.overdue`,
`comment.created`, `checklist.completed`, `schedule.cron`.

Ações: alterar campo, atribuir responsável, mover de lista, criar tarefa, criar subtarefa, comentar,
enviar notificação, e-mail ou webhook, aplicar template e chamar a IA.

**Tabelas:** `automations`, `automation_runs`.

---

## M15 — Templates

Template de projeto, lista, tarefa, checklist e documento. Aplicar um template gera a estrutura com
datas relativas (`+3d`, `+2w`) calculadas a partir da data de aplicação.

---

## M16 — Tempo, Timesheet e Carga de Trabalho

Cronômetro (start, pause, stop), lançamento manual, aprovação e rejeição de horas, visão diária,
semanal e mensal, e visão de capacidade com detecção de sobrecarga quando o utilizado supera a
capacidade cadastrada.

**Tabelas:** `time_entries`, `timesheets`, `timesheet_approvals`.

---

## M17 — Notificações

Central in-app via Realtime, e-mail, WhatsApp, Teams e Slack. Eventos: nova tarefa, atribuição,
comentário, menção, mudança de status, prazo próximo, atraso, automação executada e aprovação
pendente. Preferências por usuário e por canal.

**Tabelas:** `notifications`, `notification_preferences`, `notification_channels`.

---

## M18 — Atividades e Auditoria

`activity_logs` guarda o feed humano por entidade; `audit_logs` guarda a trilha técnica imutável com
valor anterior e novo, ator, IP e user-agent. Retenção configurável por plano.

---

## M19 — Busca Global e Filtros

Busca full-text (`tsvector` mais `pg_trgm`) sobre tarefas, projetos, documentos, comentários e
usuários, sempre filtrada por RLS. Filtros avançados combináveis com AND/OR aninhado, salváveis
como visão.

---

## M20 — Integrações, API e Webhooks

API REST via PostgREST mais Edge Functions, API Keys por tenant com escopo e rate limit, webhooks de
saída com HMAC-SHA256 e retry exponencial. Integrações previstas: ERP, CRM, WhatsApp, e-mail,
Teams, Slack, Google Calendar, Outlook e Power BI.

---

## M21 — Inteligência Artificial (Gemini)

Criação automática de projetos, fases e tarefas a partir de linguagem natural; resumo de comentários
e projetos; assistente de produtividade (atrasos, riscos, sobrecarga, gargalos, tarefas sem
responsável) e chat contextual.

**Regra crítica R27:** a configuração de IA (chave, modelo, temperatura, prompts, limites de
consumo, habilitar ou desabilitar por tenant) é **editável somente pelo platform_admin**. Usuários
de tenant apenas consomem a IA dentro dos limites definidos.

---

## M22 — Planos, Assinaturas e Administração

Planos FREE, PRO, BUSINESS e ENTERPRISE com controle de usuários, armazenamento, projetos,
automações, tokens de IA e integrações. Painel administrativo global com tenants, usuários, planos,
assinaturas, permissões, logs, integrações e API keys.
