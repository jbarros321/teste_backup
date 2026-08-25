/**
 * gemini-agent — Ações estruturadas da IA.
 *
 * action = "generate_project"  -> gera projeto + fases + tarefas a partir de texto
 * action = "summarize"         -> resume comentários/projeto/documento
 * action = "risk_scan"         -> lista atrasos, riscos, sobrecarga e gargalos
 *
 * A IA NUNCA grava direto. Ela devolve uma PROPOSTA gravada em ai_actions
 * com status 'proposed'; a criação real só acontece quando um humano aprova
 * chamando { action: "apply", ai_action_id }.
 *
 * POST { tenant_id, action, prompt?, entity?, entity_id?, ai_action_id? }
 */
import { corsHeaders, json, fail } from "../_shared/cors.ts";
import { userClient, adminClient } from "../_shared/supabase.ts";
import { loadAiConfig, generate, recordUsage } from "../_shared/gemini.ts";

const PROJECT_SCHEMA = `
{
  "project": { "name": string, "description": string, "start_offset_days": number, "duration_days": number },
  "phases": [
    {
      "name": string,
      "tasks": [
        {
          "title": string,
          "description": string,
          "estimate_minutes": number,
          "start_offset_days": number,
          "duration_days": number,
          "priority": "urgent"|"high"|"normal"|"low",
          "suggested_role": string,
          "subtasks": [ { "title": string, "estimate_minutes": number } ],
          "checklist": [ string ]
        }
      ]
    }
  ]
}`;

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return fail("Método não permitido", 405);

  const supabase = userClient(req);
  const admin = adminClient();

  const { data: auth } = await supabase.auth.getUser();
  if (!auth?.user) return fail("Não autenticado", 401);
  const userId = auth.user.id;

  const body = await req.json().catch(() => null);
  if (!body?.tenant_id || !body?.action) return fail("tenant_id e action são obrigatórios");
  const tenantId: string = body.tenant_id;

  const { data: membership } = await supabase
    .from("memberships").select("status").eq("tenant_id", tenantId).eq("user_id", userId).maybeSingle();
  if (!membership || membership.status !== "active") return fail("Sem acesso a esta empresa", 403);

  // ------------------------------------------------------------------
  // APLICAR uma proposta já aprovada por um humano
  // ------------------------------------------------------------------
  if (body.action === "apply") {
    return await applyAction(supabase, admin, tenantId, userId, body.ai_action_id);
  }

  const { data: available } = await supabase.rpc("ai_is_available", { p_tenant: tenantId });
  if (!available) return fail("IA indisponível para esta empresa", 403, "AI_UNAVAILABLE");

  const promptKey = body.action === "generate_project" ? "project_generator"
                  : body.action === "risk_scan"        ? "risk_analyst"
                  : "summarizer";

  const cfg = await loadAiConfig(admin, tenantId, promptKey, body.action === "generate_project");

  // ------------------------------------------------------------------
  // Contexto por tipo de ação (sempre lido sob RLS)
  // ------------------------------------------------------------------
  let userText = "";

  if (body.action === "generate_project") {
    userText = `Pedido: ${body.prompt}\n\nResponda no schema JSON:\n${PROJECT_SCHEMA}`;
  } else if (body.action === "summarize") {
    const { data: comments } = await supabase
      .from("comments")
      .select("body_text, created_at")
      .eq("entity", body.entity ?? "task")
      .eq("entity_id", body.entity_id)
      .order("created_at", { ascending: true })
      .limit(200);
    userText = `Resuma a discussão abaixo:\n${(comments ?? []).map((c) => `- ${c.body_text}`).join("\n")}`;
  } else if (body.action === "risk_scan") {
    const [{ data: projects }, { data: overdue }, { data: workload }, { data: openTasks }] = await Promise.all([
      supabase.from("v_project_stats").select("*").eq("tenant_id", tenantId).limit(50),
      supabase.from("v_tasks_full").select("seq,title,due_date,status_name,assignees")
        .eq("tenant_id", tenantId).eq("is_overdue", true).limit(100),
      supabase.from("v_workload").select("*").eq("tenant_id", tenantId).limit(100),
      supabase.from("v_tasks_full").select("seq,title,due_date,assignees")
        .eq("tenant_id", tenantId).is("completed_at", null).limit(300),
    ]);
    // "sem responsável" é filtrado aqui: comparar jsonb com '[]' via PostgREST é frágil.
    const unassigned = (openTasks ?? []).filter((t) => (t.assignees?.length ?? 0) === 0).slice(0, 50);
    userText = `PROJETOS: ${JSON.stringify(projects)}\nATRASADAS: ${JSON.stringify(overdue)}\n` +
               `CARGA: ${JSON.stringify(workload)}\nSEM RESPONSÁVEL: ${JSON.stringify(unassigned)}`;
  } else {
    return fail("Ação desconhecida");
  }

  const wantsJson = body.action !== "summarize";

  try {
    const result = await generate(
      [{ role: "user", parts: [{ text: userText }] }],
      cfg,
      wantsJson ? "application/json" : undefined,
    );

    await recordUsage(admin, {
      tenant_id: tenantId, user_id: userId, feature: body.action, model: cfg.model,
      input_tokens: result.inputTokens, output_tokens: result.outputTokens, success: true,
    });

    if (body.action !== "generate_project") {
      return json({ result: wantsJson ? safeJson(result.text) : result.text });
    }

    // Proposta pendente de aprovação humana
    const payload = safeJson(result.text);
    if (!payload) return fail("A IA devolveu um JSON inválido", 502);

    const { data: action, error } = await supabase
      .from("ai_actions")
      .insert({
        tenant_id: tenantId,
        requested_by: userId,
        action_type: "create_project",
        payload,
        status: "proposed",
      })
      .select("id, payload, status")
      .single();
    if (error) return fail(error.message, 400);

    return json({ ai_action: action, preview: payload });
  } catch (e) {
    await recordUsage(admin, {
      tenant_id: tenantId, user_id: userId, feature: body.action, model: cfg.model,
      input_tokens: 0, output_tokens: 0, success: false, error: (e as Error).message,
    });
    return fail("Falha na IA: " + (e as Error).message, 502);
  }
});

function safeJson(text: string) {
  try {
    return JSON.parse(text.replace(/^```json\s*/i, "").replace(/```$/, "").trim());
  } catch {
    return null;
  }
}

/**
 * Grava de fato o que a IA propôs — usando o cliente DO USUÁRIO,
 * para que o RLS valide permissão de criação tarefa a tarefa.
 */
async function applyAction(
  supabase: ReturnType<typeof userClient>,
  admin: ReturnType<typeof adminClient>,
  tenantId: string,
  userId: string,
  aiActionId: string,
) {
  if (!aiActionId) return fail("ai_action_id é obrigatório");

  const { data: action } = await supabase
    .from("ai_actions").select("*").eq("id", aiActionId).maybeSingle();
  if (!action) return fail("Proposta não encontrada", 404);
  if (action.status === "applied") return fail("Proposta já aplicada", 409);

  const p = action.payload;

  const { data: space } = await supabase
    .from("spaces").select("id").eq("tenant_id", tenantId).order("created_at").limit(1).maybeSingle();

  const { data: project, error: projErr } = await supabase
    .from("projects")
    .insert({
      tenant_id: tenantId,
      space_id: space?.id ?? null,
      name: p.project.name,
      description: p.project.description,
      owner_id: userId,
      start_date: offsetDate(p.project.start_offset_days ?? 0),
      end_date: offsetDate((p.project.start_offset_days ?? 0) + (p.project.duration_days ?? 30)),
      status: "planning",
      created_by: userId,
    })
    .select("id")
    .single();
  if (projErr) return fail("RLS recusou a criação do projeto: " + projErr.message, 403);

  const { data: defaultStatus } = await supabase
    .from("task_statuses").select("id")
    .eq("tenant_id", tenantId).eq("is_default", true).limit(1).maybeSingle();

  let created = 0;
  for (const phase of p.phases ?? []) {
    const { data: list } = await supabase
      .from("lists")
      .insert({
        tenant_id: tenantId,
        space_id: space?.id,
        project_id: project.id,
        name: phase.name,
        created_by: userId,
      })
      .select("id")
      .single();
    if (!list) continue;

    for (const t of phase.tasks ?? []) {
      const { data: task } = await supabase
        .from("tasks")
        .insert({
          tenant_id: tenantId,
          list_id: list.id,
          project_id: project.id,
          title: t.title,
          description_text: t.description,
          status_id: defaultStatus?.id ?? null,
          priority: t.priority ?? "normal",
          start_date: offsetDate(t.start_offset_days ?? 0),
          due_date: offsetDate((t.start_offset_days ?? 0) + (t.duration_days ?? 1)),
          estimate_minutes: t.estimate_minutes ?? null,
          created_by: userId,
        })
        .select("id")
        .single();
      if (!task) continue;
      created++;

      for (const st of t.subtasks ?? []) {
        await supabase.from("tasks").insert({
          tenant_id: tenantId,
          list_id: list.id,
          project_id: project.id,
          parent_task_id: task.id,
          title: st.title,
          status_id: defaultStatus?.id ?? null,
          estimate_minutes: st.estimate_minutes ?? null,
          created_by: userId,
        });
        created++;
      }

      if (t.checklist?.length) {
        const { data: cl } = await supabase
          .from("checklists")
          .insert({ tenant_id: tenantId, task_id: task.id, name: "Checklist", created_by: userId })
          .select("id").single();
        if (cl) {
          await supabase.from("checklist_items").insert(
            t.checklist.map((c: string, i: number) => ({
              tenant_id: tenantId, checklist_id: cl.id, content: c, position: (i + 1) * 100,
            })),
          );
        }
      }
    }
  }

  await admin.from("ai_actions").update({
    status: "applied",
    applied_entity: "project",
    applied_entity_id: project.id,
    reviewed_by: userId,
    reviewed_at: new Date().toISOString(),
  }).eq("id", aiActionId);

  return json({ project_id: project.id, tasks_created: created });
}

function offsetDate(days: number): string {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return d.toISOString().slice(0, 10);
}
