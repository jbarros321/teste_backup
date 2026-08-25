-- =====================================================================
-- 0010 — Row Level Security
--
-- Modelo:
--   • platform_admin  -> vê e escreve tudo, em qualquer tenant
--   • membro pleno    -> vê o tenant inteiro, escreve conforme permissão
--   • convidado       -> só vê o que está em resource_shares
--   • ninguém         -> nada de outro tenant, em nenhuma hipótese
--
-- Todas as funções são chamadas dentro de (select ...) para virarem
-- InitPlan: uma avaliação por consulta, não por linha.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1) Policies padrão para tabelas operacionais com tenant_id
-- ---------------------------------------------------------------------
do $$
declare
  r record;
begin
  for r in
    select * from (values
      ('departments','team'),        ('teams','team'),          ('team_members','team'),
      ('spaces','space'),            ('folders','space'),       ('lists','list'),
      ('projects','project'),        ('project_members','project'),
      ('task_statuses','space'),     ('tags','tag'),
      ('custom_fields','settings'),  ('custom_field_values','task'),
      ('views','view'),              ('templates','template'),
      ('task_assignees','task'),     ('task_watchers','task'),  ('task_tags','task'),
      ('task_relations','task'),     ('checklists','task'),     ('checklist_items','task'),
      ('comment_reactions','comment'), ('mentions','comment'),
      ('attachments','attachment'),
      ('document_versions','document'),
      ('goals','goal'),              ('goal_updates','goal'),
      ('okr_objectives','goal'),     ('okr_key_results','goal'),
      ('dashboards','dashboard'),    ('dashboard_widgets','dashboard'),
      ('automations','automation'),
      ('notification_channels','settings'),
      ('integrations','integration'),('webhooks','integration'),
      ('resource_shares','settings')
    ) as t(tbl, module)
  loop
    execute format('alter table public.%I enable row level security', r.tbl);

    execute format($f$
      create policy %I on public.%I
        for select to authenticated
        using (
          (select public.is_platform_admin())
          or tenant_id in (select public.user_tenant_ids_full())
        )
    $f$, r.tbl || '_sel', r.tbl);

    execute format($f$
      create policy %I on public.%I
        for insert to authenticated
        with check (
          (select public.is_platform_admin())
          or (tenant_id in (select public.user_tenant_ids_full())
              and public.has_perm(tenant_id, %L))
        )
    $f$, r.tbl || '_ins', r.tbl, r.module || '.create');

    execute format($f$
      create policy %I on public.%I
        for update to authenticated
        using (
          (select public.is_platform_admin())
          or (tenant_id in (select public.user_tenant_ids_full())
              and public.has_perm(tenant_id, %L))
        )
        with check (
          (select public.is_platform_admin())
          or tenant_id in (select public.user_tenant_ids_full())
        )
    $f$, r.tbl || '_upd', r.tbl, r.module || '.update');

    execute format($f$
      create policy %I on public.%I
        for delete to authenticated
        using (
          (select public.is_platform_admin())
          or (tenant_id in (select public.user_tenant_ids_full())
              and public.has_perm(tenant_id, %L))
        )
    $f$, r.tbl || '_del', r.tbl, r.module || '.delete');
  end loop;
end $$;

-- ---------------------------------------------------------------------
-- 2) Plataforma: só o super admin
-- ---------------------------------------------------------------------
alter table public.platform_admins enable row level security;
create policy platform_admins_sel on public.platform_admins
  for select to authenticated
  using ((select public.is_platform_admin()) or user_id = (select auth.uid()));
create policy platform_admins_all on public.platform_admins
  for all to authenticated
  using ((select public.is_platform_admin()))
  with check ((select public.is_platform_admin()));

alter table public.plans enable row level security;
create policy plans_sel on public.plans for select to authenticated using (true);
create policy plans_write on public.plans for all to authenticated
  using ((select public.is_platform_admin()))
  with check ((select public.is_platform_admin()));

-- ---------------------------------------------------------------------
-- 3) Empresas, domínios, assinaturas e consumo
-- ---------------------------------------------------------------------
alter table public.tenants enable row level security;
create policy tenants_sel on public.tenants
  for select to authenticated
  using ((select public.is_platform_admin()) or id in (select public.user_tenant_ids()));
create policy tenants_ins on public.tenants
  for insert to authenticated
  with check ((select public.is_platform_admin()));      -- criação de empresa é ato do super admin
create policy tenants_upd on public.tenants
  for update to authenticated
  using ((select public.is_platform_admin()) or public.has_min_role(id, 'owner'))
  with check ((select public.is_platform_admin()) or public.has_min_role(id, 'owner'));
create policy tenants_del on public.tenants
  for delete to authenticated
  using ((select public.is_platform_admin()));

alter table public.tenant_domains enable row level security;
create policy tenant_domains_sel on public.tenant_domains
  for select to authenticated
  using ((select public.is_platform_admin()) or tenant_id in (select public.user_tenant_ids_full()));
-- Vincular/alterar domínio muda quem entra automaticamente na empresa:
-- restrito ao super admin e ao responsável do domínio (owner).
create policy tenant_domains_write on public.tenant_domains
  for all to authenticated
  using ((select public.is_platform_admin()) or public.has_min_role(tenant_id, 'owner'))
  with check ((select public.is_platform_admin()) or public.has_min_role(tenant_id, 'owner'));

alter table public.subscriptions enable row level security;
create policy subscriptions_sel on public.subscriptions
  for select to authenticated
  using ((select public.is_platform_admin()) or public.has_min_role(tenant_id, 'admin'));
create policy subscriptions_write on public.subscriptions
  for all to authenticated
  using ((select public.is_platform_admin())) with check ((select public.is_platform_admin()));

alter table public.usage_counters enable row level security;
create policy usage_counters_sel on public.usage_counters
  for select to authenticated
  using ((select public.is_platform_admin()) or public.has_min_role(tenant_id, 'admin'));

-- ---------------------------------------------------------------------
-- 4) Perfis, memberships e convites
-- ---------------------------------------------------------------------
alter table public.profiles enable row level security;
-- Vê o próprio perfil e o de quem divide algum tenant.
create policy profiles_sel on public.profiles
  for select to authenticated
  using (
    (select public.is_platform_admin())
    or id = (select auth.uid())
    or exists (
      select 1 from public.memberships m
      where m.user_id = public.profiles.id
        and m.tenant_id in (select public.user_tenant_ids())
    )
  );
create policy profiles_upd_self on public.profiles
  for update to authenticated
  using ((select public.is_platform_admin()) or id = (select auth.uid()))
  with check ((select public.is_platform_admin()) or id = (select auth.uid()));

alter table public.memberships enable row level security;
create policy memberships_sel on public.memberships
  for select to authenticated
  using (
    (select public.is_platform_admin())
    or user_id = (select auth.uid())
    or tenant_id in (select public.user_tenant_ids_full())
  );
-- Gerir pessoas exige manage_users (owner/admin por padrão).
create policy memberships_ins on public.memberships
  for insert to authenticated
  with check ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'user.manage'));
create policy memberships_upd on public.memberships
  for update to authenticated
  using ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'user.manage'))
  with check ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'user.manage'));
create policy memberships_del on public.memberships
  for delete to authenticated
  using ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'user.manage'));

alter table public.membership_permissions enable row level security;
create policy membership_permissions_all on public.membership_permissions
  for all to authenticated
  using (
    (select public.is_platform_admin())
    or exists (select 1 from public.memberships m
               where m.id = membership_id and public.has_perm(m.tenant_id, 'user.manage'))
  )
  with check (
    (select public.is_platform_admin())
    or exists (select 1 from public.memberships m
               where m.id = membership_id and public.has_perm(m.tenant_id, 'user.manage'))
  );

alter table public.invitations enable row level security;
create policy invitations_sel on public.invitations
  for select to authenticated
  using (
    (select public.is_platform_admin())
    or public.has_perm(tenant_id, 'user.manage')
    or lower(email) = lower((select auth.jwt() ->> 'email'))
  );
create policy invitations_write on public.invitations
  for all to authenticated
  using ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'user.manage'))
  with check ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'user.manage'));

-- ---------------------------------------------------------------------
-- 5) Papéis, permissões e telas
-- ---------------------------------------------------------------------
alter table public.roles enable row level security;
create policy roles_sel on public.roles
  for select to authenticated
  using ((select public.is_platform_admin()) or tenant_id is null
         or tenant_id in (select public.user_tenant_ids_full()));
create policy roles_write on public.roles
  for all to authenticated
  using ((select public.is_platform_admin())
         or (tenant_id is not null and public.has_perm(tenant_id, 'role.manage')))
  with check ((select public.is_platform_admin())
         or (tenant_id is not null and public.has_perm(tenant_id, 'role.manage')));

alter table public.permissions enable row level security;
create policy permissions_sel on public.permissions for select to authenticated using (true);
create policy permissions_write on public.permissions for all to authenticated
  using ((select public.is_platform_admin())) with check ((select public.is_platform_admin()));

alter table public.role_permissions enable row level security;
create policy role_permissions_sel on public.role_permissions
  for select to authenticated
  using ((select public.is_platform_admin()) or tenant_id is null
         or tenant_id in (select public.user_tenant_ids_full()));
create policy role_permissions_write on public.role_permissions
  for all to authenticated
  using ((select public.is_platform_admin())
         or (tenant_id is not null and public.has_perm(tenant_id, 'role.manage')))
  with check ((select public.is_platform_admin())
         or (tenant_id is not null and public.has_perm(tenant_id, 'role.manage')));

alter table public.screens enable row level security;
create policy screens_sel on public.screens for select to authenticated using (true);
create policy screens_write on public.screens for all to authenticated
  using ((select public.is_platform_admin())) with check ((select public.is_platform_admin()));

-- É AQUI que o responsável do domínio configura o que cada perfil enxerga.
alter table public.screen_access enable row level security;
create policy screen_access_sel on public.screen_access
  for select to authenticated
  using ((select public.is_platform_admin()) or tenant_id in (select public.user_tenant_ids()));
create policy screen_access_write on public.screen_access
  for all to authenticated
  using ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'screen.manage'))
  with check (
    ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'screen.manage'))
    -- nenhum owner consegue liberar tela exclusiva da plataforma
    and (
      (select public.is_platform_admin())
      or not exists (select 1 from public.screens s where s.key = screen_key and s.platform_only)
    )
  );

-- ---------------------------------------------------------------------
-- 6) Conteúdo com acesso de convidado (share explícito)
-- ---------------------------------------------------------------------
alter table public.tasks enable row level security;
create policy tasks_sel on public.tasks
  for select to authenticated
  using (
    (select public.is_platform_admin())
    or tenant_id in (select public.user_tenant_ids_full())
    or public.has_share('task', id)
    or public.has_share('list', list_id)
    or (project_id is not null and public.has_share('project', project_id))
  );
create policy tasks_ins on public.tasks
  for insert to authenticated
  with check (
    (select public.is_platform_admin())
    or (tenant_id in (select public.user_tenant_ids_full()) and public.has_perm(tenant_id, 'task.create'))
  );
create policy tasks_upd on public.tasks
  for update to authenticated
  using (
    (select public.is_platform_admin())
    or (tenant_id in (select public.user_tenant_ids_full()) and public.has_perm(tenant_id, 'task.update'))
    or exists (select 1 from public.task_assignees ta
               where ta.task_id = public.tasks.id and ta.user_id = (select auth.uid()))
  )
  with check (
    (select public.is_platform_admin())
    or tenant_id in (select public.user_tenant_ids_full())
    or public.has_share('task', id)
  );
create policy tasks_del on public.tasks
  for delete to authenticated
  using (
    (select public.is_platform_admin())
    or (tenant_id in (select public.user_tenant_ids_full()) and public.has_perm(tenant_id, 'task.delete'))
  );

alter table public.documents enable row level security;
create policy documents_sel on public.documents
  for select to authenticated
  using (
    (select public.is_platform_admin())
    or tenant_id in (select public.user_tenant_ids_full())
    or public.has_share('document', id)
  );
create policy documents_ins on public.documents
  for insert to authenticated
  with check ((select public.is_platform_admin())
    or (tenant_id in (select public.user_tenant_ids_full()) and public.has_perm(tenant_id, 'document.create')));
create policy documents_upd on public.documents
  for update to authenticated
  using ((select public.is_platform_admin())
    or (tenant_id in (select public.user_tenant_ids_full()) and public.has_perm(tenant_id, 'document.update'))
    or exists (select 1 from public.resource_shares rs
               where rs.entity = 'document' and rs.entity_id = public.documents.id
                 and rs.user_id = (select auth.uid()) and rs.can_edit))
  with check ((select public.is_platform_admin()) or tenant_id in (select public.user_tenant_ids_full())
    or public.has_share('document', id));
create policy documents_del on public.documents
  for delete to authenticated
  using ((select public.is_platform_admin())
    or (tenant_id in (select public.user_tenant_ids_full()) and public.has_perm(tenant_id, 'document.delete')));

alter table public.comments enable row level security;
create policy comments_sel on public.comments
  for select to authenticated
  using (
    (select public.is_platform_admin())
    or tenant_id in (select public.user_tenant_ids_full())
    or public.has_share(entity, entity_id)
  );
create policy comments_ins on public.comments
  for insert to authenticated
  with check (
    author_id = (select auth.uid())
    and (
      tenant_id in (select public.user_tenant_ids_full())
      or public.has_share(entity, entity_id)
    )
  );
-- Comentário é editável/apagável apenas pelo autor (ou por quem administra o tenant).
create policy comments_upd on public.comments
  for update to authenticated
  using ((select public.is_platform_admin()) or author_id = (select auth.uid()))
  with check ((select public.is_platform_admin()) or author_id = (select auth.uid()));
create policy comments_del on public.comments
  for delete to authenticated
  using ((select public.is_platform_admin()) or author_id = (select auth.uid())
         or public.has_perm(tenant_id, 'comment.delete'));

-- ---------------------------------------------------------------------
-- 7) Tempo: cada um lança o seu; gestor aprova
-- ---------------------------------------------------------------------
alter table public.time_entries enable row level security;
create policy time_entries_sel on public.time_entries
  for select to authenticated
  using (
    (select public.is_platform_admin())
    or user_id = (select auth.uid())
    or (tenant_id in (select public.user_tenant_ids_full()) and public.has_perm(tenant_id, 'time.view_all'))
  );
create policy time_entries_ins on public.time_entries
  for insert to authenticated
  with check (user_id = (select auth.uid()) and tenant_id in (select public.user_tenant_ids_full()));
create policy time_entries_upd on public.time_entries
  for update to authenticated
  using (
    (select public.is_platform_admin())
    or (user_id = (select auth.uid()) and approved_at is null)
    or public.has_perm(tenant_id, 'time.approve')
  )
  with check (tenant_id in (select public.user_tenant_ids_full()));
create policy time_entries_del on public.time_entries
  for delete to authenticated
  using ((select public.is_platform_admin())
         or (user_id = (select auth.uid()) and approved_at is null)
         or public.has_perm(tenant_id, 'time.approve'));

alter table public.timesheets enable row level security;
create policy timesheets_sel on public.timesheets
  for select to authenticated
  using ((select public.is_platform_admin()) or user_id = (select auth.uid())
         or public.has_perm(tenant_id, 'time.approve'));
create policy timesheets_write on public.timesheets
  for all to authenticated
  using ((select public.is_platform_admin()) or user_id = (select auth.uid())
         or public.has_perm(tenant_id, 'time.approve'))
  with check (tenant_id in (select public.user_tenant_ids_full()));

-- ---------------------------------------------------------------------
-- 8) Notificações: estritamente pessoais
-- ---------------------------------------------------------------------
alter table public.notifications enable row level security;
create policy notifications_sel on public.notifications
  for select to authenticated using (user_id = (select auth.uid()));
create policy notifications_upd on public.notifications
  for update to authenticated
  using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
create policy notifications_del on public.notifications
  for delete to authenticated using (user_id = (select auth.uid()));
-- INSERT só via trigger/Edge Function (service_role), nunca pelo cliente.

alter table public.notification_preferences enable row level security;
create policy notification_prefs_all on public.notification_preferences
  for all to authenticated
  using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));

-- ---------------------------------------------------------------------
-- 9) Logs: leitura conforme papel, escrita só pelo sistema
-- ---------------------------------------------------------------------
alter table public.activity_logs enable row level security;
create policy activity_logs_sel on public.activity_logs
  for select to authenticated
  using ((select public.is_platform_admin()) or tenant_id in (select public.user_tenant_ids_full()));

alter table public.audit_logs enable row level security;
create policy audit_logs_sel on public.audit_logs
  for select to authenticated
  using ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'audit.view'));

alter table public.automation_runs enable row level security;
create policy automation_runs_sel on public.automation_runs
  for select to authenticated
  using ((select public.is_platform_admin()) or tenant_id in (select public.user_tenant_ids_full()));

alter table public.webhook_deliveries enable row level security;
create policy webhook_deliveries_sel on public.webhook_deliveries
  for select to authenticated
  using ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'integration.update'));

alter table public.tenant_sequences enable row level security;  -- sem policy: só service_role

-- ---------------------------------------------------------------------
-- 10) API keys: nunca expõem o hash a não-administradores
-- ---------------------------------------------------------------------
alter table public.api_keys enable row level security;
create policy api_keys_sel on public.api_keys
  for select to authenticated
  using ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'integration.update'));
create policy api_keys_write on public.api_keys
  for all to authenticated
  using ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'integration.update'))
  with check ((select public.is_platform_admin()) or public.has_perm(tenant_id, 'integration.update'));

-- ---------------------------------------------------------------------
-- 11) IA — CONFIGURAÇÃO É EXCLUSIVA DO PLATFORM ADMIN (regra R27)
-- ---------------------------------------------------------------------
alter table public.ai_settings enable row level security;
create policy ai_settings_admin_only on public.ai_settings
  for all to authenticated
  using ((select public.is_platform_admin()))
  with check ((select public.is_platform_admin()));

alter table public.ai_tenant_settings enable row level security;
-- O tenant PODE LER (para a UI saber se a IA está ligada e quanto sobrou de cota),
-- mas SÓ o platform admin escreve.
create policy ai_tenant_settings_sel on public.ai_tenant_settings
  for select to authenticated
  using ((select public.is_platform_admin()) or tenant_id in (select public.user_tenant_ids_full()));
create policy ai_tenant_settings_write on public.ai_tenant_settings
  for all to authenticated
  using ((select public.is_platform_admin()))
  with check ((select public.is_platform_admin()));

alter table public.ai_prompts enable row level security;
create policy ai_prompts_admin_only on public.ai_prompts
  for all to authenticated
  using ((select public.is_platform_admin()))
  with check ((select public.is_platform_admin()));

-- Consumo: cada usuário vê as próprias conversas; administradores do tenant veem todas.
alter table public.ai_conversations enable row level security;
create policy ai_conversations_sel on public.ai_conversations
  for select to authenticated
  using ((select public.is_platform_admin()) or user_id = (select auth.uid())
         or public.has_perm(tenant_id, 'ai.view_all'));
create policy ai_conversations_write on public.ai_conversations
  for all to authenticated
  using ((select public.is_platform_admin()) or user_id = (select auth.uid()))
  with check (user_id = (select auth.uid()) and tenant_id in (select public.user_tenant_ids_full()));

alter table public.ai_messages enable row level security;
create policy ai_messages_sel on public.ai_messages
  for select to authenticated
  using (
    (select public.is_platform_admin())
    or exists (select 1 from public.ai_conversations c
               where c.id = conversation_id and c.user_id = (select auth.uid()))
    or public.has_perm(tenant_id, 'ai.view_all')
  );
-- Escrita de mensagens é sempre pela Edge Function (service_role).

alter table public.ai_usage enable row level security;
create policy ai_usage_sel on public.ai_usage
  for select to authenticated
  using ((select public.is_platform_admin())
         or (tenant_id in (select public.user_tenant_ids_full()) and public.has_min_role(tenant_id, 'admin')));

alter table public.ai_actions enable row level security;
create policy ai_actions_sel on public.ai_actions
  for select to authenticated
  using ((select public.is_platform_admin()) or requested_by = (select auth.uid())
         or public.has_perm(tenant_id, 'ai.view_all'));
create policy ai_actions_upd on public.ai_actions
  for update to authenticated
  using (
    (select public.is_platform_admin())
    or (tenant_id in (select public.user_tenant_ids_full()) and public.has_perm(tenant_id, 'task.create'))
  )
  with check (tenant_id in (select public.user_tenant_ids_full()));

-- ---------------------------------------------------------------------
-- 12) Storage: um bucket privado, isolado por prefixo tenant/{tenant_id}/
-- ---------------------------------------------------------------------
insert into storage.buckets (id, name, public, file_size_limit)
values ('attachments', 'attachments', false, 52428800)
on conflict (id) do nothing;

create policy "attachments_read" on storage.objects
  for select to authenticated
  using (
    bucket_id = 'attachments'
    and (
      (select public.is_platform_admin())
      or (storage.foldername(name))[2]::uuid in (select public.user_tenant_ids_full())
    )
  );

create policy "attachments_write" on storage.objects
  for insert to authenticated
  with check (
    bucket_id = 'attachments'
    and (storage.foldername(name))[1] = 'tenant'
    and (storage.foldername(name))[2]::uuid in (select public.user_tenant_ids_full())
  );

create policy "attachments_delete" on storage.objects
  for delete to authenticated
  using (
    bucket_id = 'attachments'
    and (
      (select public.is_platform_admin())
      or ((storage.foldername(name))[2]::uuid in (select public.user_tenant_ids_full())
          and owner = (select auth.uid()))
    )
  );

-- A trava final contra o papel `anon` fica em 0014, depois que todos os
-- objetos existirem (revoke só atinge o que já foi criado).
