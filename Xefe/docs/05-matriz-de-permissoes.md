# 05 — Matriz de Permissões e Acesso às Telas

## 1. Hierarquia

```
PLATFORM ADMIN  (você)          fora do tenant, vê e controla tudo
      │
      └── OWNER  (responsável do domínio)   administra a empresa e os acessos
              └── ADMIN                      delegado do owner
                      └── MANAGER            gestor de área
                              └── LEADER     líder de equipe
                                      └── MEMBER      colaborador
                                              └── GUEST  externo, só o compartilhado
```

`role_rank`: owner 60 · admin 50 · manager 40 · leader 30 · member 20 · guest 10.

## 2. Matriz de permissões

Legenda: ✅ concedido · ⬜ negado · 🔒 exclusivo da plataforma

| Permissão | Platform | Owner | Admin | Manager | Leader | Member | Guest |
|---|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| `task.create` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| `task.update` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| `task.delete` | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ |
| `comment.create` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `comment.delete` | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ |
| `attachment.create` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `list.create` / `list.update` | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ |
| `space.create` / `space.delete` | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ |
| `project.create` / `project.update` | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ |
| `project.delete` | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ |
| `document.create` / `document.update` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| `goal.create` / `goal.update` | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ |
| `dashboard.create` / `dashboard.update` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| `automation.create` / `.update` / `.delete` | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ |
| `integration.create` / `.update` | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| `time.view_all` | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ |
| `time.approve` | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ |
| `report.export` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| `user.manage` | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| `role.manage` | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| `screen.manage` | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| `audit.view` | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| `ai.use` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| `ai.view_all` | ✅ | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ |
| **`ai.manage`** | 🔒 | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| `platform.tenants` | 🔒 | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| `platform.plans` | 🔒 | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| `platform.logs` | 🔒 | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |

O owner recebe automaticamente tudo que **não** é `platform_only` — não precisa de linhas em
`role_permissions`. As permissões 🔒 são recusadas por `has_perm()` antes mesmo de olhar o papel.

## 3. Acesso às telas (o que o responsável do domínio configura)

O owner abre **Configurações → Acesso às Telas** e marca, para cada papel, quais telas aparecem.
Isso grava em `screen_access (tenant_id, role, screen_key, can_view, can_edit)`.

Padrão aplicado a uma empresa nova por `fn_seed_screen_access()`:

| Tela | Owner | Admin | Manager | Leader | Member | Guest |
|---|:--:|:--:|:--:|:--:|:--:|:--:|
| Início | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| Tarefas (Lista / Kanban) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Calendário / Gantt / Timeline | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| Projetos | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| Espaços e Listas | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| Documentos | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Metas / OKRs | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| Cronômetro / Timesheet | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| Carga de Trabalho | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| Dashboards / Relatórios | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| Automações | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ |
| Integrações | ✅ | ✅ | ✅ | ⬜ | ⬜ | ⬜ |
| Chat com IA / Assistente | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| Configurações → Empresa | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| Configurações → Usuários | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| Configurações → Perfis / Telas / Domínios / Plano | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| Configurações → Auditoria | ✅ | ✅ | ⬜ | ⬜ | ⬜ | ⬜ |
| **Admin → Empresas / IA / Planos / Consumo / Logs** | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |

As cinco telas `admin.*` têm `platform_only = true`: nenhum owner consegue liberá-las nem para si
mesmo — a policy de `screen_access` bloqueia a gravação.

## 4. Sobrescrita individual

Casos pontuais não exigem criar papel novo:

```sql
-- Liberar exclusão de tarefa para um colaborador específico
insert into membership_permissions (membership_id, permission_key, effect)
values ('<membership>', 'task.delete', 'allow');

-- Revogar exportação de um gestor específico (deny sempre vence)
insert into membership_permissions (membership_id, permission_key, effect)
values ('<membership>', 'report.export', 'deny');
```

Ordem de avaliação em `has_perm()`:

```
1. É platform_admin?                          -> true
2. A permissão é platform_only?               -> false
3. Existe deny individual?                    -> false
4. Existe allow individual?                   -> true
5. É owner?                                   -> true
6. O papel tem a permissão em role_permissions? -> true/false
```

## 5. Convidados

Convidado (`guest`) não enxerga o tenant. Ele só vê o que estiver em `resource_shares`:

```sql
insert into resource_shares (tenant_id, entity, entity_id, user_id, can_edit)
values ('<tenant>', 'project', '<projeto>', '<usuario>', false);
```

Compartilhar um projeto libera automaticamente as tarefas dele (a policy de `tasks` verifica
`has_share('project', project_id)`). É assim que um cliente externo acompanha um projeto sem ver o
resto da empresa.
