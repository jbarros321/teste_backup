/**
 * gemini-chat — Chat contextual com a IA.
 *
 * Fluxo:
 *   1. autentica o usuário (JWT) e confirma membership no tenant;
 *   2. verifica se a IA está habilitada e se há cota (RPC ai_is_available);
 *   3. monta o CONTEXTO lendo o banco com o cliente do PRÓPRIO usuário,
 *      ou seja, sob RLS — a IA nunca vê o que o usuário não veria;
 *   4. chama o Gemini com a chave do servidor;
 *   5. grava mensagens e consumo com o cliente admin.
 *
 * POST { tenant_id, conversation_id?, message, context? }
 */
import { corsHeaders, json, fail } from "../_shared/cors.ts";
import { userClient, adminClient } from "../_shared/supabase.ts";
import { loadAiConfig, generate, recordUsage, GeminiContent } from "../_shared/gemini.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return fail("Método não permitido", 405);

  const supabase = userClient(req);
  const admin = adminClient();

  const { data: auth } = await supabase.auth.getUser();
  if (!auth?.user) return fail("Não autenticado", 401);
  const userId = auth.user.id;

  let payload: { tenant_id?: string; conversation_id?: string; message?: string; context?: Record<string, string> };
  try {
    payload = await req.json();
  } catch {
    return fail("JSON inválido");
  }

  const { tenant_id, conversation_id, message, context } = payload;
  if (!tenant_id || !message?.trim()) return fail("tenant_id e message são obrigatórios");

  // --- membership (sob RLS) ---
  const { data: membership } = await supabase
    .from("memberships")
    .select("id, role, status")
    .eq("tenant_id", tenant_id)
    .eq("user_id", userId)
    .maybeSingle();
  if (!membership || membership.status !== "active") return fail("Sem acesso a esta empresa", 403);

  // --- IA habilitada e com cota? ---
  const { data: available } = await supabase.rpc("ai_is_available", { p_tenant: tenant_id });
  if (!available) return fail("IA indisponível para esta empresa (desabilitada ou sem cota)", 403, "AI_UNAVAILABLE");

  let cfg;
  try {
    cfg = await loadAiConfig(admin, tenant_id, "chat_default");
  } catch (e) {
    return fail((e as Error).message, 403, "AI_DISABLED");
  }

  // --- conversa ---
  let convId = conversation_id;
  if (!convId) {
    const { data: conv, error } = await supabase
      .from("ai_conversations")
      .insert({
        tenant_id,
        user_id: userId,
        title: message.slice(0, 60),
        context: context ?? {},
      })
      .select("id")
      .single();
    if (error) return fail(error.message, 400);
    convId = conv.id;
  }

  // --- histórico ---
  const { data: history } = await supabase
    .from("ai_messages")
    .select("role, content")
    .eq("conversation_id", convId)
    .order("created_at", { ascending: true })
    .limit(20);

  // --- contexto de negócio, lido SOB RLS ---
  const businessContext = await buildContext(supabase, tenant_id, context);

  const contents: GeminiContent[] = [
    { role: "user", parts: [{ text: `CONTEXTO ATUAL DA EMPRESA (dados reais):\n${businessContext}` }] },
    { role: "model", parts: [{ text: "Contexto recebido. Pode perguntar." }] },
    ...(history ?? [])
      .filter((m) => m.role === "user" || m.role === "model")
      .map((m) => ({ role: m.role as "user" | "model", parts: [{ text: m.content }] })),
    { role: "user", parts: [{ text: message }] },
  ];

  try {
    const result = await generate(contents, cfg);

    await admin.from("ai_messages").insert([
      { tenant_id, conversation_id: convId, role: "user", content: message },
      {
        tenant_id,
        conversation_id: convId,
        role: "model",
        content: result.text,
        model: cfg.model,
        input_tokens: result.inputTokens,
        output_tokens: result.outputTokens,
        latency_ms: result.latencyMs,
      },
    ]);

    await recordUsage(admin, {
      tenant_id,
      user_id: userId,
      feature: "chat",
      model: cfg.model,
      input_tokens: result.inputTokens,
      output_tokens: result.outputTokens,
      success: true,
    });

    return json({
      conversation_id: convId,
      answer: result.text,
      usage: { input: result.inputTokens, output: result.outputTokens, latency_ms: result.latencyMs },
    });
  } catch (e) {
    await recordUsage(admin, {
      tenant_id,
      user_id: userId,
      feature: "chat",
      model: cfg.model,
      input_tokens: 0,
      output_tokens: 0,
      success: false,
      error: (e as Error).message,
    });
    return fail("Falha ao consultar a IA: " + (e as Error).message, 502);
  }
});

/**
 * Monta um resumo compacto do estado da empresa.
 * Usa o cliente do usuário de propósito: o RLS recorta o que pode ser lido.
 */
async function buildContext(
  supabase: ReturnType<typeof userClient>,
  tenantId: string,
  ctx?: Record<string, string>,
): Promise<string> {
  const [counters, overdue, projects, workload] = await Promise.all([
    supabase.rpc("fn_dashboard_counters", { p_tenant: tenantId }),
    supabase
      .from("v_tasks_full")
      .select("seq, title, due_date, status_name, assignees")
      .eq("tenant_id", tenantId)
      .eq("is_overdue", true)
      .order("due_date", { ascending: true })
      .limit(25),
    supabase
      .from("v_project_stats")
      .select("name, status, progress, total_tasks, done_tasks, overdue_tasks, risk, end_date")
      .eq("tenant_id", tenantId)
      .limit(25),
    supabase
      .from("v_workload")
      .select("full_name, capacity_hours, used_hours, utilization_pct, is_overloaded, open_tasks")
      .eq("tenant_id", tenantId)
      .limit(50),
  ]);

  const blocks = [
    `INDICADORES: ${JSON.stringify(counters.data ?? {})}`,
    `TAREFAS ATRASADAS (${overdue.data?.length ?? 0}): ${JSON.stringify(overdue.data ?? [])}`,
    `PROJETOS: ${JSON.stringify(projects.data ?? [])}`,
    `CARGA DA EQUIPE: ${JSON.stringify(workload.data ?? [])}`,
  ];

  if (ctx?.task_id) {
    const { data: task } = await supabase
      .from("v_tasks_full")
      .select("*")
      .eq("id", ctx.task_id)
      .maybeSingle();
    if (task) blocks.push(`TAREFA EM FOCO: ${JSON.stringify(task)}`);
  }

  return blocks.join("\n\n");
}
