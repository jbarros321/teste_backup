# 04 — Segurança e Row Level Security

Este é o documento mais importante do projeto. Se o RLS estiver certo, um bug de front-end vira
inconveniente; se estiver errado, vira vazamento entre empresas.

## 1. Princípio

> **A autorização mora no banco.** O front-end esconde botões; o PostgreSQL recusa linhas.

Toda tabela tem RLS habilitado. O cliente web usa a chave `anon` e o JWT do usuário — jamais a
`service_role`. A `service_role` existe apenas dentro das Edge Functions, para telemetria,
notificações e integrações.

## 2. Os quatro sujeitos

| Sujeito | Como é identificado | Alcance |
|---|---|---|
| **Platform admin** | linha em `platform_admins` | tudo, em todos os tenants |
| **Membro pleno** | `memberships` ativo com papel ≠ `guest` | o tenant inteiro, conforme permissão |
| **Convidado** | `memberships` ativo com papel `guest` | somente o que está em `resource_shares` |
| **Anônimo** | sem JWT | nada (`revoke all ... from anon`) |

## 3. Funções de apoio

Todas são `SECURITY DEFINER` (para não recursionar no RLS da própria `memberships`) e `STABLE`
(para o planner promovê-las a InitPlan — uma avaliação por consulta, não por linha).

| Função | Uso |
|---|---|
| `is_platform_admin()` | super admin? |
| `user_tenant_ids()` | todos os tenants do usuário, incluindo os de convidado |
| `user_tenant_ids_full()` | apenas os tenants com acesso pleno (exclui `guest`) |
| `my_role(tenant)` | papel no tenant |
| `has_min_role(tenant, papel)` | comparação por `role_rank` |
| `has_perm(tenant, 'task.update')` | permissão efetiva (deny > allow > papel) |
| `can_see_screen(tenant, 'admin.ai')` | tela liberada para o papel |
| `has_share(entity, id)` | recurso compartilhado com o usuário ou com equipe dele |

### Por que sempre `(select fn())`

```sql
-- ERRADO: a função é avaliada uma vez POR LINHA
using (tenant_id in (select public.user_tenant_ids()) and public.is_platform_admin())

-- CERTO: vira InitPlan, avaliada uma vez por consulta
using ((select public.is_platform_admin()) or tenant_id in (select public.user_tenant_ids_full()))
```

Em uma lista de 50 mil tarefas essa diferença é de milissegundos para segundos.

## 4. Padrão aplicado às tabelas de conteúdo

```sql
-- SELECT: admin global, ou membro pleno do tenant
using (
  (select public.is_platform_admin())
  or tenant_id in (select public.user_tenant_ids_full())
)

-- INSERT / UPDATE / DELETE: membro pleno + permissão do módulo
with check (
  (select public.is_platform_admin())
  or (tenant_id in (select public.user_tenant_ids_full())
      and public.has_perm(tenant_id, 'task.create'))
)
```

Gerado em laço por `0010_rls_policies.sql` para 35 tabelas — o mesmo padrão em todas, sem margem
para esquecer uma.

## 5. Exceções deliberadas

**Tarefas** ganham uma cláusula extra de leitura para convidados:

```sql
or public.has_share('task', id)
or public.has_share('list', list_id)
or (project_id is not null and public.has_share('project', project_id))
```

E um responsável pode atualizar a própria tarefa mesmo sem `task.update` global.

**Comentários** só são editáveis pelo autor. Nem o owner edita comentário alheio — apagar, sim
(com `comment.delete`), reescrever, não.

**Apontamento de horas**: cada um lança o seu; quem tem `time.view_all` vê o dos outros; quem tem
`time.approve` aprova. Depois de aprovado, o próprio autor não altera mais.

**Notificações** são estritamente pessoais (`user_id = auth.uid()`) e só o sistema insere.

**`tenant_domains`** só é alterado pelo super admin ou pelo `owner`. Quem controla o domínio
controla quem entra automaticamente na empresa — é uma escrita de segurança, não de configuração.

## 6. A trava da IA (regra R27)

```sql
alter table public.ai_settings enable row level security;
create policy ai_settings_admin_only on public.ai_settings
  for all to authenticated
  using ((select public.is_platform_admin()))
  with check ((select public.is_platform_admin()));
```

Três camadas garantem que só você mexe na IA:

1. **RLS**: `ai_settings` e `ai_prompts` são invisíveis e ingravável para qualquer não-super-admin.
   `ai_tenant_settings` é legível pelo tenant (para a UI mostrar cota) mas gravável só por você.
2. **Catálogo de permissões**: `ai.manage` está marcada `platform_only = true`, e `has_perm()`
   retorna `false` para qualquer permissão assim — inclusive para o `owner`, que tem "tudo".
3. **Tela**: `admin.ai` tem `platform_only = true`, e a policy de `screen_access` impede que um
   owner libere para si uma tela marcada como exclusiva da plataforma.

A chave da API não está em nenhuma dessas tabelas: fica em Supabase Secrets e é lida apenas dentro
da Edge Function. O banco guarda só o *nome* do segredo (`api_key_ref`).

## 7. Storage

Bucket privado `attachments`, caminho `tenant/{tenant_id}/{entity}/{entity_id}/{uuid}-{arquivo}`.
A policy extrai o `tenant_id` do próprio caminho:

```sql
(storage.foldername(name))[2]::uuid in (select public.user_tenant_ids_full())
```

Download sempre por URL assinada de curta duração (60 s), gerada no cliente autenticado.

## 8. Autenticação

- JWT de acesso com validade curta (1 h) e refresh token rotativo — padrão do Supabase Auth.
- Senha com política mínima de 10 caracteres; MFA (TOTP) disponível e obrigatório para `owner`,
  `admin` e `platform_admin`.
- Login social (Google) opcional por tenant.
- Rate limiting no gateway do Supabase mais limite por API key (`api_keys.rate_limit_per_min`).
- Sessões revogáveis pelo owner (`auth.admin.signOut`) via Edge Function administrativa.

## 9. Checklist de teste de segurança

Rode antes de cada release. Cada item deve **falhar** a operação.

```
[ ] Usuário do tenant A faz select em tarefa do tenant B                    -> 0 linhas
[ ] Usuário comum faz update em ai_settings                                 -> erro RLS
[ ] Usuário comum faz select em ai_settings                                 -> 0 linhas
[ ] Owner tenta inserir screen_access para 'admin.ai'                       -> erro RLS
[ ] Owner tenta conceder ai.manage a um papel                               -> has_perm() = false
[ ] Convidado lista tarefas do tenant                                       -> só as compartilhadas
[ ] Convidado tenta criar tarefa                                            -> erro RLS
[ ] Membro tenta apagar tarefa sem task.delete                              -> erro RLS
[ ] Membro edita comentário de outro                                        -> erro RLS
[ ] Usuário altera hora já aprovada                                         -> erro RLS
[ ] Cliente tenta baixar arquivo de outro tenant no Storage                 -> 403
[ ] anon faz select em qualquer tabela                                      -> permission denied
[ ] JWT expirado em qualquer endpoint                                       -> 401
```

Automatize com `supabase test db` (pgTAP) — cada linha vira um teste que roda no CI.

## 10. Conformidade (LGPD)

- Base legal por finalidade registrada em `tenants.settings`.
- Direito de acesso: RPC `fn_export_user_data(user_id)` — a implementar na Fase 5 — gera JSON completo do titular.
- Direito de exclusão: anonimização do `profiles` mantendo os registros operacionais
  (autoria vira "Usuário removido"), preservando integridade histórica.
- Retenção de logs configurável por plano; `audit_logs` nunca é apagado antes de 2 anos.
- Criptografia em repouso (disco do Supabase) e em trânsito (TLS 1.3).
