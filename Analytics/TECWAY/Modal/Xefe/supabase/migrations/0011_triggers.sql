-- =====================================================================
-- 0011 — Triggers: updated_at, numeração, busca, progresso, atividade,
--        notificações, auditoria e limites de plano.
-- =====================================================================

-- ---------------------------------------------------------------------
-- updated_at automático
-- ---------------------------------------------------------------------
create or replace function public.tg_set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end $$;

do $$
declare t text;
begin
  foreach t in array array[
    'tenants','profiles','memberships','departments','teams','spaces','folders','lists',
    'projects','tasks','comments','documents','goals','okr_objectives','okr_key_results',
    'dashboards','automations','views','templates','time_entries','ai_conversations'
  ] loop
    execute format(
      'create trigger set_updated_at before update on public.%I
       for each row execute function public.tg_set_updated_at()', t);
  end loop;
end $$;

-- ---------------------------------------------------------------------
-- Tarefa: numeração sequencial, busca, profundidade e conclusão
-- ---------------------------------------------------------------------
create or replace function public.tg_task_before()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_depth int := 0;
  v_parent uuid;
  v_cat public.status_category;
begin
  if tg_op = 'INSERT' and new.seq is null then
    new.seq := public.next_seq(new.tenant_id, 'task');
  end if;

  -- Texto de busca (título + descrição plana)
  new.search_tsv :=
      setweight(to_tsvector('portuguese', coalesce(new.title, '')), 'A')
   || setweight(to_tsvector('portuguese', coalesce(new.description_text, '')), 'B');

  -- Profundidade máxima de 3 níveis
  if new.parent_task_id is not null then
    v_parent := new.parent_task_id;
    while v_parent is not null and v_depth < 5 loop
      select t.parent_task_id into v_parent from public.tasks t where t.id = v_parent;
      v_depth := v_depth + 1;
    end loop;
    if v_depth >= 3 then
      raise exception 'Profundidade máxima de subtarefas (3 níveis) excedida'
        using errcode = 'check_violation';
    end if;
  end if;

  -- completed_at conforme a categoria do status
  if tg_op = 'UPDATE' and new.status_id is distinct from old.status_id then
    select s.category into v_cat from public.task_statuses s where s.id = new.status_id;
    if v_cat = 'done' then
      new.completed_at := coalesce(new.completed_at, now());
      new.progress := 100;
    else
      new.completed_at := null;
      if old.progress = 100 then new.progress := 0; end if;
    end if;
  end if;

  return new;
end $$;

create trigger task_before
  before insert or update on public.tasks
  for each row execute function public.tg_task_before();

-- Regra R19: só conclui a tarefa-pai se as subtarefas estiverem concluídas
create or replace function public.tg_task_require_subtasks()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare v_required boolean; v_open int;
begin
  if new.completed_at is null or old.completed_at is not null then
    return new;
  end if;

  select l.require_subtasks_done into v_required
  from public.lists l where l.id = new.list_id;

  if coalesce(v_required, false) then
    select count(*) into v_open
    from public.tasks t
    where t.parent_task_id = new.id
      and t.deleted_at is null
      and t.completed_at is null;
    if v_open > 0 then
      raise exception 'Existem % subtarefa(s) em aberto nesta tarefa', v_open
        using errcode = 'check_violation';
    end if;
  end if;
  return new;
end $$;

create trigger task_require_subtasks
  before update on public.tasks
  for each row execute function public.tg_task_require_subtasks();

-- ---------------------------------------------------------------------
-- Checklist: percentual de conclusão
-- ---------------------------------------------------------------------
create or replace function public.tg_checklist_progress()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare v_checklist uuid;
begin
  v_checklist := coalesce(new.checklist_id, old.checklist_id);
  update public.checklists c
  set progress = coalesce((
    select round(100.0 * count(*) filter (where i.is_done) / nullif(count(*), 0), 2)
    from public.checklist_items i where i.checklist_id = v_checklist
  ), 0)
  where c.id = v_checklist;
  return coalesce(new, old);
end $$;

create trigger checklist_progress
  after insert or update or delete on public.checklist_items
  for each row execute function public.tg_checklist_progress();

create or replace function public.tg_checklist_item_done()
returns trigger language plpgsql as $$
begin
  if new.is_done and not coalesce(old.is_done, false) then
    new.done_at := now();
    new.done_by := auth.uid();
  elsif not new.is_done then
    new.done_at := null; new.done_by := null;
  end if;
  return new;
end $$;

create trigger checklist_item_done
  before update on public.checklist_items
  for each row execute function public.tg_checklist_item_done();

-- ---------------------------------------------------------------------
-- Projeto: progresso e horas
-- ---------------------------------------------------------------------
create or replace function public.tg_project_progress()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare v_project uuid;
begin
  v_project := coalesce(new.project_id, old.project_id);
  if v_project is null then return coalesce(new, old); end if;

  update public.projects p
  set progress = coalesce((
    select round(
      100.0 * sum(coalesce(t.estimate_minutes, 60)) filter (where t.completed_at is not null)
            / nullif(sum(coalesce(t.estimate_minutes, 60)), 0), 2)
    from public.tasks t
    where t.project_id = v_project and t.deleted_at is null
  ), 0)
  where p.id = v_project;

  return coalesce(new, old);
end $$;

create trigger project_progress
  after insert or update of completed_at, estimate_minutes, project_id or delete
  on public.tasks
  for each row execute function public.tg_project_progress();

-- Horas apontadas somam de volta na tarefa e no projeto
create or replace function public.tg_time_rollup()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare v_task uuid;
begin
  v_task := coalesce(new.task_id, old.task_id);
  if v_task is null then return coalesce(new, old); end if;

  update public.tasks t
  set time_spent_minutes = coalesce((
    select sum(te.duration_minutes) from public.time_entries te
    where te.task_id = v_task and te.duration_minutes is not null
  ), 0)
  where t.id = v_task;

  return coalesce(new, old);
end $$;

create trigger time_rollup
  after insert or update or delete on public.time_entries
  for each row execute function public.tg_time_rollup();

-- ---------------------------------------------------------------------
-- Feed de atividades
-- ---------------------------------------------------------------------
create or replace function public.tg_task_activity()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare v_actor uuid := auth.uid();
begin
  if tg_op = 'INSERT' then
    insert into public.activity_logs (tenant_id, entity, entity_id, actor_id, action, new_value)
    values (new.tenant_id, 'task', new.id, v_actor, 'created', new.title);
    return new;
  end if;

  if new.status_id is distinct from old.status_id then
    insert into public.activity_logs (tenant_id, entity, entity_id, actor_id, action, field, old_value, new_value)
    values (new.tenant_id, 'task', new.id, v_actor, 'status_changed', 'status',
            (select s.name from public.task_statuses s where s.id = old.status_id),
            (select s.name from public.task_statuses s where s.id = new.status_id));
  end if;

  if new.priority is distinct from old.priority then
    insert into public.activity_logs (tenant_id, entity, entity_id, actor_id, action, field, old_value, new_value)
    values (new.tenant_id, 'task', new.id, v_actor, 'updated', 'priority', old.priority::text, new.priority::text);
  end if;

  if new.due_date is distinct from old.due_date then
    insert into public.activity_logs (tenant_id, entity, entity_id, actor_id, action, field, old_value, new_value)
    values (new.tenant_id, 'task', new.id, v_actor, 'updated', 'due_date', old.due_date::text, new.due_date::text);
  end if;

  if new.title is distinct from old.title then
    insert into public.activity_logs (tenant_id, entity, entity_id, actor_id, action, field, old_value, new_value)
    values (new.tenant_id, 'task', new.id, v_actor, 'updated', 'title', old.title, new.title);
  end if;

  return new;
end $$;

create trigger task_activity
  after insert or update on public.tasks
  for each row execute function public.tg_task_activity();

-- ---------------------------------------------------------------------
-- Notificações
-- ---------------------------------------------------------------------
create or replace function public.tg_notify_assignment()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare v_task public.tasks%rowtype;
begin
  select * into v_task from public.tasks where id = new.task_id;
  if new.user_id = auth.uid() then return new; end if;   -- não notifica auto-atribuição

  insert into public.notifications (tenant_id, user_id, type, title, body, entity, entity_id, actor_id, link)
  values (new.tenant_id, new.user_id, 'task.assigned',
          'Você foi atribuído a uma tarefa', v_task.title, 'task', v_task.id, auth.uid(),
          '/tasks/' || v_task.id);
  return new;
end $$;

create trigger notify_assignment
  after insert on public.task_assignees
  for each row execute function public.tg_notify_assignment();

create or replace function public.tg_notify_mention()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  if new.mentioned_user_id is not null and new.mentioned_user_id <> coalesce(auth.uid(), '00000000-0000-0000-0000-000000000000'::uuid) then
    insert into public.notifications (tenant_id, user_id, type, title, body, entity, entity_id, actor_id)
    values (new.tenant_id, new.mentioned_user_id, 'comment.mention',
            'Você foi mencionado', null, new.entity, new.entity_id, auth.uid());
  end if;

  if new.mentioned_team_id is not null then
    insert into public.notifications (tenant_id, user_id, type, title, body, entity, entity_id, actor_id)
    select new.tenant_id, tm.user_id, 'comment.mention', 'Sua equipe foi mencionada',
           null, new.entity, new.entity_id, auth.uid()
    from public.team_members tm
    where tm.team_id = new.mentioned_team_id and tm.user_id <> coalesce(auth.uid(), '00000000-0000-0000-0000-000000000000'::uuid);
  end if;

  return new;
end $$;

create trigger notify_mention
  after insert on public.mentions
  for each row execute function public.tg_notify_mention();

-- Comentário notifica responsáveis e observadores da tarefa
create or replace function public.tg_notify_comment()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  if new.entity <> 'task' then return new; end if;

  insert into public.notifications (tenant_id, user_id, type, title, body, entity, entity_id, actor_id)
  select new.tenant_id, u.user_id, 'comment.created', 'Novo comentário',
         left(new.body_text, 200), 'task', new.entity_id, new.author_id
  from (
    select ta.user_id from public.task_assignees ta where ta.task_id = new.entity_id
    union
    select tw.user_id from public.task_watchers tw where tw.task_id = new.entity_id
  ) u
  where u.user_id <> new.author_id;

  return new;
end $$;

create trigger notify_comment
  after insert on public.comments
  for each row execute function public.tg_notify_comment();

-- ---------------------------------------------------------------------
-- Auditoria genérica (tabelas sensíveis)
-- ---------------------------------------------------------------------
create or replace function public.tg_audit()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare v_tenant uuid;
begin
  begin
    v_tenant := case
      when tg_op = 'DELETE' then (to_jsonb(old) ->> 'tenant_id')::uuid
      else (to_jsonb(new) ->> 'tenant_id')::uuid
    end;
  exception when others then v_tenant := null;
  end;

  insert into public.audit_logs (tenant_id, actor_id, table_name, record_id, operation, old_data, new_data)
  values (
    v_tenant,
    auth.uid(),
    tg_table_name,
    case when tg_op = 'DELETE' then (to_jsonb(old) ->> 'id')::uuid else (to_jsonb(new) ->> 'id')::uuid end,
    tg_op,
    case when tg_op in ('UPDATE','DELETE') then to_jsonb(old) end,
    case when tg_op in ('INSERT','UPDATE') then to_jsonb(new) end
  );
  return coalesce(new, old);
end $$;

do $$
declare t text;
begin
  foreach t in array array[
    'memberships','tenant_domains','screen_access','role_permissions','membership_permissions',
    'ai_settings','ai_tenant_settings','api_keys','integrations','webhooks','plans','subscriptions'
  ] loop
    execute format(
      'create trigger audit_changes after insert or update or delete on public.%I
       for each row execute function public.tg_audit()', t);
  end loop;
end $$;

-- ---------------------------------------------------------------------
-- Limites do plano
-- ---------------------------------------------------------------------
create or replace function public.tg_enforce_plan_limits()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare v_max int; v_count int;
begin
  if tg_table_name = 'memberships' then
    select p.max_users into v_max
    from public.tenants t join public.plans p on p.id = t.plan_id
    where t.id = new.tenant_id;
    if v_max is not null then
      select count(*) into v_count from public.memberships m
      where m.tenant_id = new.tenant_id and m.status = 'active';
      if v_count >= v_max then
        raise exception 'PLAN_LIMIT_EXCEEDED: limite de % usuários atingido', v_max
          using errcode = 'check_violation';
      end if;
    end if;

  elsif tg_table_name = 'projects' then
    select p.max_projects into v_max
    from public.tenants t join public.plans p on p.id = t.plan_id
    where t.id = new.tenant_id;
    if v_max is not null then
      select count(*) into v_count from public.projects pr
      where pr.tenant_id = new.tenant_id and pr.deleted_at is null;
      if v_count >= v_max then
        raise exception 'PLAN_LIMIT_EXCEEDED: limite de % projetos atingido', v_max
          using errcode = 'check_violation';
      end if;
    end if;
  end if;

  return new;
end $$;

create trigger enforce_plan_limits_users
  before insert on public.memberships
  for each row execute function public.tg_enforce_plan_limits();

create trigger enforce_plan_limits_projects
  before insert on public.projects
  for each row execute function public.tg_enforce_plan_limits();

-- ---------------------------------------------------------------------
-- Documentos: busca e versionamento
-- ---------------------------------------------------------------------
create or replace function public.tg_document_before()
returns trigger language plpgsql as $$
begin
  new.search_tsv :=
      setweight(to_tsvector('portuguese', coalesce(new.title, '')), 'A')
   || setweight(to_tsvector('portuguese', coalesce(new.content_text, '')), 'B');
  if tg_op = 'UPDATE' and new.content is distinct from old.content then
    new.version := old.version + 1;
  end if;
  return new;
end $$;

create trigger document_before
  before insert or update on public.documents
  for each row execute function public.tg_document_before();

create or replace function public.tg_document_version()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  if new.version > coalesce(old.version, 0) then
    insert into public.document_versions (tenant_id, document_id, version, content, author_id)
    values (new.tenant_id, new.id, new.version, new.content, auth.uid())
    on conflict (document_id, version) do nothing;
  end if;
  return new;
end $$;

create trigger document_version
  after update on public.documents
  for each row execute function public.tg_document_version();
