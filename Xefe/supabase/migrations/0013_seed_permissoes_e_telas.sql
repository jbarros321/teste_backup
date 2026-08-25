-- =====================================================================
-- 0013 — Seed: planos, catálogo de permissões, telas, permissões por
--        papel, prompts de IA e função de bootstrap de empresa.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Planos
-- ---------------------------------------------------------------------
insert into public.plans (code, name, price_month, max_users, max_projects, max_storage_mb,
                          max_automations, max_ai_tokens_month, allow_integrations, allow_ai)
values
  ('FREE',       'Free',        0,      5,   3,   1024,    5,        0, false, false),
  ('PRO',        'Pro',       149,     25,  50,  20480,   50,   500000, true,  true),
  ('BUSINESS',   'Business',  499,    100, 500, 102400,  500,  2000000, true,  true),
  ('ENTERPRISE', 'Enterprise', 0,     null, null, null,  null, 10000000, true,  true)
on conflict (code) do nothing;

-- ---------------------------------------------------------------------
-- Catálogo de permissões
-- ---------------------------------------------------------------------
insert into public.permissions (key, module, description, platform_only)
select m.module || '.' || a.action,
       m.module,
       'Permite ' || a.action || ' em ' || m.module,
       false
from (values
  ('team'),('space'),('list'),('project'),('task'),('tag'),('view'),('template'),
  ('comment'),('attachment'),('document'),('goal'),('dashboard'),('automation'),
  ('integration'),('settings')
) as m(module)
cross join (values ('create'),('update'),('delete')) as a(action)
on conflict (key) do nothing;

insert into public.permissions (key, module, description, platform_only) values
  ('user.manage',        'users',   'Convidar, editar, bloquear e remover usuários', false),
  ('role.manage',        'roles',   'Criar papéis e alterar permissões',             false),
  ('screen.manage',      'screens', 'Definir quais telas cada perfil enxerga',       false),
  ('audit.view',         'audit',   'Consultar a trilha de auditoria',               false),
  ('report.export',      'reports', 'Exportar relatórios em PDF, Excel e CSV',       false),
  ('time.view_all',      'time',    'Ver apontamentos de todos os usuários',         false),
  ('time.approve',       'time',    'Aprovar ou rejeitar horas',                     false),
  ('ai.use',             'ai',      'Usar o assistente de IA',                       false),
  ('ai.view_all',        'ai',      'Ver conversas de IA de toda a empresa',         false),
  -- ---- exclusivas da plataforma (R27) ----
  ('ai.manage',          'ai',      'Alterar a configuração de IA (chave, modelo, cotas)', true),
  ('platform.tenants',   'platform','Criar e administrar empresas',                  true),
  ('platform.plans',     'platform','Administrar planos e assinaturas',              true),
  ('platform.logs',      'platform','Ver logs globais da plataforma',                true)
on conflict (key) do nothing;

-- ---------------------------------------------------------------------
-- Permissões por papel (padrão global: tenant_id nulo)
-- ---------------------------------------------------------------------
-- ADMIN: tudo que não é platform_only
insert into public.role_permissions (role_key, permission_key, tenant_id)
select 'admin', p.key, null from public.permissions p where not p.platform_only
on conflict do nothing;

-- MANAGER: conteúdo completo, aprova horas, vê IA da empresa, sem gestão de usuários/papéis
insert into public.role_permissions (role_key, permission_key, tenant_id)
select 'manager', p.key, null
from public.permissions p
where not p.platform_only
  and p.key not in ('user.manage','role.manage','screen.manage','audit.view','integration.delete')
on conflict do nothing;

-- LEADER: cria e edita conteúdo, apaga apenas tarefas e comentários
insert into public.role_permissions (role_key, permission_key, tenant_id)
select 'leader', p.key, null
from public.permissions p
where p.key in (
  'task.create','task.update','task.delete',
  'list.create','list.update',
  'project.create','project.update',
  'comment.create','comment.update','comment.delete',
  'attachment.create','attachment.update','attachment.delete',
  'document.create','document.update',
  'tag.create','view.create','view.update','template.create',
  'goal.create','goal.update','dashboard.create','dashboard.update',
  'time.view_all','time.approve','report.export','ai.use'
)
on conflict do nothing;

-- MEMBER: opera o próprio trabalho
insert into public.role_permissions (role_key, permission_key, tenant_id)
select 'member', p.key, null
from public.permissions p
where p.key in (
  'task.create','task.update',
  'comment.create','comment.update',
  'attachment.create','attachment.update',
  'document.create','document.update',
  'view.create','view.update',
  'tag.create','dashboard.create','dashboard.update',
  'report.export','ai.use'
)
on conflict do nothing;

-- GUEST: apenas comenta no que foi compartilhado com ele
insert into public.role_permissions (role_key, permission_key, tenant_id)
select 'guest', p.key, null
from public.permissions p
where p.key in ('comment.create','attachment.create')
on conflict do nothing;

-- OWNER não precisa de linhas: has_perm() concede tudo que não é platform_only.

-- ---------------------------------------------------------------------
-- Catálogo de telas
-- ---------------------------------------------------------------------
insert into public.screens (key, module, name, path, platform_only, order_index) values
  ('home.overview',      'home',        'Início',                  '/',                     false, 10),
  ('tasks.list',         'tasks',       'Tarefas (Lista)',         '/tasks',                false, 20),
  ('tasks.board',        'tasks',       'Tarefas (Kanban)',        '/tasks/board',          false, 21),
  ('tasks.calendar',     'tasks',       'Calendário',              '/tasks/calendar',       false, 22),
  ('tasks.gantt',        'tasks',       'Gantt',                   '/tasks/gantt',          false, 23),
  ('tasks.timeline',     'tasks',       'Timeline',                '/tasks/timeline',       false, 24),
  ('projects.list',      'projects',    'Projetos',                '/projects',             false, 30),
  ('spaces.manage',      'workspace',   'Espaços e Listas',        '/spaces',               false, 35),
  ('docs.list',          'docs',        'Documentos',              '/docs',                 false, 40),
  ('goals.list',         'goals',       'Metas',                   '/goals',                false, 50),
  ('okr.list',           'goals',       'OKRs',                    '/okrs',                 false, 51),
  ('time.timer',         'time',        'Cronômetro',              '/time',                 false, 60),
  ('time.timesheet',     'time',        'Timesheet',               '/time/timesheet',       false, 61),
  ('time.workload',      'time',        'Carga de Trabalho',       '/time/workload',        false, 62),
  ('dashboards.custom',  'dashboards',  'Dashboards',              '/dashboards',           false, 70),
  ('reports.list',       'reports',     'Relatórios',              '/reports',              false, 75),
  ('automations.list',   'automations', 'Automações',              '/automations',          false, 80),
  ('integrations.list',  'integrations','Integrações',             '/integrations',         false, 85),
  ('ai.chat',            'ai',          'Chat com IA',             '/ai',                   false, 90),
  ('ai.assistant',       'ai',          'Assistente de Produtividade', '/ai/assistant',     false, 91),
  ('settings.company',   'settings',    'Dados da Empresa',        '/settings/company',     false, 100),
  ('settings.users',     'settings',    'Usuários e Convites',     '/settings/users',       false, 101),
  ('settings.roles',     'settings',    'Perfis e Permissões',     '/settings/roles',       false, 102),
  ('settings.screens',   'settings',    'Acesso às Telas',         '/settings/screens',     false, 103),
  ('settings.domains',   'settings',    'Domínios de E-mail',      '/settings/domains',     false, 104),
  ('settings.plan',      'settings',    'Plano e Consumo',         '/settings/plan',        false, 105),
  ('settings.audit',     'settings',    'Auditoria',               '/settings/audit',       false, 106),
  -- ---- exclusivas do super admin ----
  ('admin.tenants',      'admin',       'Empresas (Plataforma)',   '/admin/tenants',        true,  200),
  ('admin.ai',           'admin',       'Configuração de IA',      '/admin/ia',             true,  201),
  ('admin.plans',        'admin',       'Planos e Assinaturas',    '/admin/plans',          true,  202),
  ('admin.usage',        'admin',       'Consumo e Cotas',         '/admin/usage',          true,  203),
  ('admin.logs',         'admin',       'Logs da Plataforma',      '/admin/logs',           true,  204)
on conflict (key) do nothing;

-- ---------------------------------------------------------------------
-- Liberação padrão de telas para um tenant novo.
-- O responsável do domínio ajusta depois em /settings/screens.
-- ---------------------------------------------------------------------
create or replace function public.fn_seed_screen_access(p_tenant uuid)
returns void
language sql
security definer
set search_path = ''
as $$
  insert into public.screen_access (tenant_id, role, screen_key, can_view, can_edit)
  select p_tenant, r.role, s.key,
         case
           when s.platform_only then false
           when r.role = 'guest' then s.key in ('tasks.list','tasks.board','docs.list')
           when r.role = 'member' then s.module not in ('settings','admin','integrations','automations')
           when r.role = 'leader' then s.module not in ('settings','admin')
                                     or s.key in ('settings.company')
           when r.role = 'manager' then s.module <> 'admin'
                                      and s.key not in ('settings.roles','settings.screens','settings.domains','settings.plan')
           else s.module <> 'admin'                    -- owner e admin veem tudo do tenant
         end,
         r.role in ('owner','admin')
  from public.screens s
  cross join (select unnest(enum_range(null::public.member_role)) as role) r
  on conflict (tenant_id, role, screen_key) do nothing;
$$;

-- ---------------------------------------------------------------------
-- Status padrão de tarefa para um tenant novo
-- ---------------------------------------------------------------------
create or replace function public.fn_seed_task_statuses(p_tenant uuid)
returns void
language sql
security definer
set search_path = ''
as $$
  insert into public.task_statuses (tenant_id, name, category, color, position, is_default)
  values
    (p_tenant, 'A Fazer',            'todo',        '#94A3B8', 1, true),
    (p_tenant, 'Em Análise',         'in_progress', '#3B82F6', 2, false),
    (p_tenant, 'Em Desenvolvimento', 'in_progress', '#8B5CF6', 3, false),
    (p_tenant, 'Em Aprovação',       'in_progress', '#F59E0B', 4, false),
    (p_tenant, 'Concluído',          'done',        '#10B981', 5, false),
    (p_tenant, 'Cancelado',          'canceled',    '#EF4444', 6, false);
$$;

-- ---------------------------------------------------------------------
-- Bootstrap de empresa: cria tenant, domínio, owner, telas e status.
-- Executável apenas pelo super admin.
-- ---------------------------------------------------------------------
create or replace function public.fn_bootstrap_tenant(
  p_name        text,
  p_slug        text,
  p_domain      text,
  p_owner_email text,
  p_plan_code   text default 'PRO'
)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_tenant uuid;
  v_owner  uuid;
  v_plan   uuid;
begin
  if not public.is_platform_admin() then
    raise exception 'Apenas o administrador da plataforma pode criar empresas'
      using errcode = 'insufficient_privilege';
  end if;

  select id into v_plan from public.plans where code = p_plan_code;

  insert into public.tenants (name, slug, plan_id, status, created_by)
  values (p_name, p_slug, v_plan, 'active', auth.uid())
  returning id into v_tenant;

  select id into v_owner from auth.users where lower(email) = lower(p_owner_email);

  insert into public.tenant_domains (tenant_id, domain, owner_user_id, default_role, auto_join, verified_at)
  values (v_tenant, lower(p_domain), v_owner, 'member', true, now());

  if v_owner is not null then
    insert into public.memberships (tenant_id, user_id, role, status)
    values (v_tenant, v_owner, 'owner', 'active')
    on conflict (tenant_id, user_id) do update set role = 'owner';
  else
    insert into public.invitations (tenant_id, email, role, invited_by)
    values (v_tenant, lower(p_owner_email), 'owner', auth.uid());
  end if;

  insert into public.ai_tenant_settings (tenant_id, is_enabled) values (v_tenant, false)
  on conflict (tenant_id) do nothing;

  perform public.fn_seed_screen_access(v_tenant);
  perform public.fn_seed_task_statuses(v_tenant);

  insert into public.spaces (tenant_id, name, description, created_by)
  values (v_tenant, 'Geral', 'Espaço inicial criado automaticamente', auth.uid());

  return v_tenant;
end $$;

grant execute on function public.fn_bootstrap_tenant(text, text, text, text, text) to authenticated;

-- ---------------------------------------------------------------------
-- Prompts de IA (curadoria do super admin)
-- ---------------------------------------------------------------------
insert into public.ai_prompts (key, version, content) values
('chat_default', 1,
'Você é o Xefe, assistente de gestão de trabalho. Responda em português do Brasil, de forma direta e objetiva.
Você recebe contexto real da empresa do usuário (tarefas, projetos, prazos, responsáveis). Use somente esse
contexto para responder — nunca invente dados. Se a informação não estiver no contexto, diga que não encontrou.
Ao sugerir ações, proponha-as de forma estruturada para que o usuário confirme antes de qualquer gravação.'),

('project_generator', 1,
'Você é um gerente de projetos experiente. A partir da descrição do usuário, gere uma estrutura de projeto
completa em JSON, com fases, tarefas, subtarefas, estimativas em minutos e datas relativas ao início.
Responda EXCLUSIVAMENTE com JSON válido no schema informado, sem texto antes ou depois.'),

('summarizer', 1,
'Resuma o conteúdo em português do Brasil, em no máximo 8 linhas. Destaque: decisões tomadas, pendências,
riscos e próximos passos. Não repita o texto original nem acrescente informação inexistente.'),

('risk_analyst', 1,
'Analise os dados de projetos e tarefas fornecidos e identifique: tarefas atrasadas, projetos em risco,
usuários sobrecarregados, gargalos e atividades sem responsável. Responda em JSON com uma lista de achados,
cada um com: tipo, severidade (alta/media/baixa), entidade, descrição e ação recomendada.')
on conflict (key, version) do nothing;
