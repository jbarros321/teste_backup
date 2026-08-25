-- =====================================================================
-- 0012 — Views e RPCs de leitura (dashboards, carga, busca, Gantt)
--
-- Toda view usa security_invoker = true: sem isso a view rodaria com os
-- privilégios do dono e furaria o RLS das tabelas de origem.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Tarefa "completa" para as telas de lista/kanban
-- ---------------------------------------------------------------------
create view public.v_tasks_full with (security_invoker = true) as
select
  t.*,
  s.name                                as status_name,
  s.category                            as status_category,
  s.color                               as status_color,
  l.name                                as list_name,
  p.name                                as project_name,
  (t.due_date is not null
     and t.completed_at is null
     and t.due_date < now())            as is_overdue,
  coalesce((
    select jsonb_agg(jsonb_build_object('id', pr.id, 'name', pr.full_name, 'avatar', pr.avatar_url))
    from public.task_assignees ta
    join public.profiles pr on pr.id = ta.user_id
    where ta.task_id = t.id
  ), '[]'::jsonb)                       as assignees,
  coalesce((
    select jsonb_agg(jsonb_build_object('id', tg.id, 'name', tg.name, 'color', tg.color))
    from public.task_tags tt join public.tags tg on tg.id = tt.tag_id
    where tt.task_id = t.id
  ), '[]'::jsonb)                       as tags,
  (select count(*) from public.tasks st where st.parent_task_id = t.id and st.deleted_at is null) as subtask_count,
  (select count(*) from public.comments c where c.entity = 'task' and c.entity_id = t.id and c.deleted_at is null) as comment_count,
  (select count(*) from public.attachments a where a.entity = 'task' and a.entity_id = t.id and a.deleted_at is null) as attachment_count
from public.tasks t
left join public.task_statuses s on s.id = t.status_id
left join public.lists l         on l.id = t.list_id
left join public.projects p      on p.id = t.project_id
where t.deleted_at is null;

-- ---------------------------------------------------------------------
-- Indicadores de projeto
-- ---------------------------------------------------------------------
create view public.v_project_stats with (security_invoker = true) as
select
  p.id                as project_id,
  p.tenant_id,
  p.name,
  p.status,
  p.progress,
  p.budget,
  p.cost_actual,
  p.end_date,
  count(t.id)                                                          as total_tasks,
  count(t.id) filter (where t.completed_at is not null)                as done_tasks,
  count(t.id) filter (where t.completed_at is null
                        and t.due_date < now())                        as overdue_tasks,
  count(t.id) filter (where t.status_id is null)                       as tasks_without_status,
  count(distinct ta.user_id)                                           as people_involved,
  coalesce(sum(t.estimate_minutes), 0)                                 as estimated_minutes,
  coalesce(sum(t.time_spent_minutes), 0)                               as spent_minutes,
  case
    when p.end_date is not null
         and p.end_date - current_date <= 15
         and p.progress < 60 then 'at_risk'
    when coalesce(sum(t.time_spent_minutes), 0) >
         coalesce(sum(t.estimate_minutes), 0) * 1.1
         and coalesce(sum(t.estimate_minutes), 0) > 0 then 'at_risk'
    else 'on_track'
  end                                                                  as risk
from public.projects p
left join public.tasks t          on t.project_id = p.id and t.deleted_at is null
left join public.task_assignees ta on ta.task_id = t.id
where p.deleted_at is null
group by p.id;

-- ---------------------------------------------------------------------
-- Carga de trabalho (capacidade x horas apontadas na semana corrente)
-- ---------------------------------------------------------------------
create view public.v_workload with (security_invoker = true) as
select
  m.tenant_id,
  m.user_id,
  pr.full_name,
  pr.avatar_url,
  pr.weekly_capacity_hours                                          as capacity_hours,
  coalesce(round(sum(te.duration_minutes) / 60.0, 2), 0)            as used_hours,
  coalesce(round(
    100.0 * sum(te.duration_minutes) / 60.0 / nullif(pr.weekly_capacity_hours, 0), 1), 0) as utilization_pct,
  (coalesce(sum(te.duration_minutes), 0) / 60.0 > pr.weekly_capacity_hours) as is_overloaded,
  (select count(*) from public.task_assignees ta
     join public.tasks t on t.id = ta.task_id
    where ta.user_id = m.user_id and t.completed_at is null and t.deleted_at is null) as open_tasks
from public.memberships m
join public.profiles pr on pr.id = m.user_id
left join public.time_entries te
       on te.user_id = m.user_id
      and te.tenant_id = m.tenant_id
      and te.started_at >= date_trunc('week', now())
where m.status = 'active'
group by m.tenant_id, m.user_id, pr.full_name, pr.avatar_url, pr.weekly_capacity_hours;

-- ---------------------------------------------------------------------
-- Contadores do dashboard, em uma chamada só
-- ---------------------------------------------------------------------
create or replace function public.fn_dashboard_counters(
  p_tenant uuid,
  p_from   timestamptz default (now() - interval '30 days'),
  p_to     timestamptz default now()
)
returns jsonb
language sql
stable
security invoker
set search_path = public
as $$
  with t as (
    select *
    from tasks
    where tenant_id = p_tenant
      and deleted_at is null
      and created_at between p_from and p_to
  )
  select jsonb_build_object(
    'total_tasks',      (select count(*) from t),
    'done_tasks',       (select count(*) from t where completed_at is not null),
    'overdue_tasks',    (select count(*) from t where completed_at is null and due_date < now()),
    'unassigned_tasks', (select count(*) from t x
                         where not exists (select 1 from task_assignees ta where ta.task_id = x.id)),
    'by_priority',      coalesce((
                          select jsonb_object_agg(z.priority, z.qtd)
                          from (select priority::text as priority, count(*) as qtd
                                from t group by 1) z), '{}'::jsonb),
    'by_status',        coalesce((
                          select jsonb_object_agg(z.status, z.qtd)
                          from (select coalesce(s.name, 'Sem status') as status, count(*) as qtd
                                from t left join task_statuses s on s.id = t.status_id
                                group by 1) z), '{}'::jsonb),
    'by_assignee',      coalesce((
                          select jsonb_object_agg(z.pessoa, z.qtd)
                          from (select coalesce(pr.full_name, 'Sem responsável') as pessoa, count(*) as qtd
                                from t
                                left join task_assignees ta on ta.task_id = t.id and ta.is_primary
                                left join profiles pr on pr.id = ta.user_id
                                group by 1) z), '{}'::jsonb),
    'hours_logged',     coalesce((
                          select round(sum(te.duration_minutes) / 60.0, 2)
                          from time_entries te
                          where te.tenant_id = p_tenant
                            and te.started_at between p_from and p_to), 0)
  );
$$;

-- ---------------------------------------------------------------------
-- Busca global (respeita RLS porque é security invoker)
-- ---------------------------------------------------------------------
create or replace function public.fn_search(p_tenant uuid, p_query text, p_limit int default 30)
returns table (
  entity public.entity_type,
  id uuid,
  title text,
  snippet text,
  rank real
)
language sql
stable
security invoker
set search_path = public
as $$
  with q as (select websearch_to_tsquery('portuguese', p_query) as tsq)
  select 'task'::public.entity_type, t.id, t.title,
         left(coalesce(t.description_text, ''), 180),
         ts_rank(t.search_tsv, q.tsq)
  from tasks t, q
  where t.tenant_id = p_tenant and t.deleted_at is null and t.search_tsv @@ q.tsq
  union all
  select 'document'::public.entity_type, d.id, d.title,
         left(coalesce(d.content_text, ''), 180),
         ts_rank(d.search_tsv, q.tsq)
  from documents d, q
  where d.tenant_id = p_tenant and d.deleted_at is null and d.search_tsv @@ q.tsq
  union all
  select 'project'::public.entity_type, p.id, p.name,
         left(coalesce(p.description, ''), 180), 0.5::real
  from projects p
  where p.tenant_id = p_tenant and p.deleted_at is null
    and p.name ilike '%' || p_query || '%'
  union all
  select 'comment'::public.entity_type, c.id, left(c.body_text, 60),
         left(c.body_text, 180), 0.3::real
  from comments c
  where c.tenant_id = p_tenant and c.deleted_at is null
    and c.body_text ilike '%' || p_query || '%'
  order by rank desc
  limit p_limit;
$$;

-- ---------------------------------------------------------------------
-- Gantt: caminho crítico por projeto (CPM simplificado sobre depends_on)
-- ---------------------------------------------------------------------
create or replace function public.fn_critical_path(p_project uuid)
returns table (task_id uuid, title text, earliest_start timestamptz, earliest_finish timestamptz, is_critical boolean)
language sql
stable
security invoker
set search_path = public
as $$
  with recursive base as (
    select t.id, t.title, t.start_date, t.due_date,
           coalesce(t.estimate_minutes, 0) as dur
    from tasks t
    where t.project_id = p_project and t.deleted_at is null
  ),
  chain as (
    select b.id, b.title, b.start_date as es,
           b.start_date + make_interval(mins => b.dur) as ef, 1 as depth
    from base b
    where not exists (
      select 1 from task_relations r
      where r.source_task_id = b.id and r.relation in ('depends_on','blocked_by')
    )
    union all
    select b.id, b.title, c.ef,
           c.ef + make_interval(mins => b.dur), c.depth + 1
    from base b
    join task_relations r on r.source_task_id = b.id and r.relation in ('depends_on','blocked_by')
    join chain c on c.id = r.target_task_id
    where c.depth < 50
  )
  select c.id, c.title, min(c.es), max(c.ef),
         max(c.ef) = (select max(ef) from chain) as is_critical
  from chain c
  group by c.id, c.title;
$$;

-- ---------------------------------------------------------------------
-- Reordenação estável no Kanban (média entre vizinhos)
-- ---------------------------------------------------------------------
create or replace function public.fn_move_task(
  p_task uuid, p_list uuid, p_status uuid, p_before uuid, p_after uuid
)
returns numeric
language plpgsql
security invoker
set search_path = public
as $$
declare
  v_before numeric;
  v_after  numeric;
  v_new    numeric;
begin
  select position into v_before from tasks where id = p_before;
  select position into v_after  from tasks where id = p_after;

  v_new := case
    when v_before is null and v_after is null then 1000
    when v_before is null then v_after - 100
    when v_after  is null then v_before + 100
    else (v_before + v_after) / 2
  end;

  update tasks
  set list_id = coalesce(p_list, list_id),
      status_id = coalesce(p_status, status_id),
      position = v_new
  where id = p_task;

  return v_new;
end $$;

-- ---------------------------------------------------------------------
-- Varredura de prazos: gera notificações de "vence em breve" e "atrasada"
-- (chamada por pg_cron a cada 15 minutos)
-- ---------------------------------------------------------------------
create or replace function public.fn_scan_due_tasks()
returns int
language plpgsql
security definer
set search_path = ''
as $$
declare v_count int := 0;
begin
  with alvo as (
    select t.id, t.tenant_id, t.title, ta.user_id,
           case when t.due_date < now() then 'task.overdue' else 'task.due_soon' end as tipo
    from public.tasks t
    join public.task_assignees ta on ta.task_id = t.id
    where t.deleted_at is null
      and t.completed_at is null
      and t.due_date is not null
      and t.due_date < now() + interval '24 hours'
  ), inserido as (
    insert into public.notifications (tenant_id, user_id, type, title, body, entity, entity_id, link)
    select a.tenant_id, a.user_id, a.tipo,
           case when a.tipo = 'task.overdue' then 'Tarefa atrasada' else 'Tarefa vence em breve' end,
           a.title, 'task', a.id, '/tasks/' || a.id
    from alvo a
    where not exists (
      select 1 from public.notifications n
      where n.user_id = a.user_id and n.entity_id = a.id and n.type = a.tipo
        and n.created_at > now() - interval '20 hours'
    )
    returning 1
  )
  select count(*) into v_count from inserido;
  return v_count;
end $$;

-- ---------------------------------------------------------------------
-- Expurgo da lixeira
-- ---------------------------------------------------------------------
create or replace function public.fn_purge_trash(p_days int default 30)
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
  delete from public.tasks       where deleted_at < now() - make_interval(days => p_days);
  delete from public.documents   where deleted_at < now() - make_interval(days => p_days);
  delete from public.attachments where deleted_at < now() - make_interval(days => p_days);
  delete from public.activity_logs where created_at < now() - interval '2 years';
end $$;

grant execute on function
  public.fn_dashboard_counters(uuid, timestamptz, timestamptz),
  public.fn_search(uuid, text, int),
  public.fn_critical_path(uuid),
  public.fn_move_task(uuid, uuid, uuid, uuid, uuid)
to authenticated;

-- Agendamentos (requer a extensão pg_cron habilitada no projeto)
-- select cron.schedule('scan_due_tasks', '*/15 * * * *', $$select public.fn_scan_due_tasks()$$);
-- select cron.schedule('purge_trash',    '0 3 * * *',    $$select public.fn_purge_trash(30)$$);
