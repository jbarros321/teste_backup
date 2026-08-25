/**
 * Cliente do Google Gemini.
 *
 * A chave vive em Supabase Secrets (GEMINI_API_KEY) e é lida somente aqui,
 * dentro da Edge Function. Ela nunca vai para o banco nem para o navegador.
 *
 *   supabase secrets set GEMINI_API_KEY=...
 */
import { SupabaseClient } from "https://esm.sh/@supabase/supabase-js@2";

const GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta";

export type GeminiPart = { text: string };
export type GeminiContent = { role: "user" | "model"; parts: GeminiPart[] };

export type AiConfig = {
  model: string;
  temperature: number;
  maxOutputTokens: number;
  topP: number;
  systemPrompt: string;
};

export type GeminiResult = {
  text: string;
  inputTokens: number;
  outputTokens: number;
  latencyMs: number;
};

/** Lê a configuração global + override do tenant (tabelas escritas só pelo super admin). */
export async function loadAiConfig(
  admin: SupabaseClient,
  tenantId: string,
  promptKey = "chat_default",
  advanced = false,
): Promise<AiConfig> {
  const [{ data: global }, { data: tenant }, { data: prompt }] = await Promise.all([
    admin.from("ai_settings").select("*").eq("id", true).single(),
    admin.from("ai_tenant_settings").select("*").eq("tenant_id", tenantId).maybeSingle(),
    admin.from("ai_prompts").select("content")
      .eq("key", promptKey).eq("is_active", true)
      .order("version", { ascending: false }).limit(1).maybeSingle(),
  ]);

  if (!global?.is_enabled) throw new Error("IA desabilitada na plataforma");
  if (!tenant?.is_enabled) throw new Error("IA não habilitada para esta empresa");

  return {
    model: tenant.model_override ?? (advanced ? global.model_advanced : global.model_default),
    temperature: Number(global.temperature),
    maxOutputTokens: global.max_output_tokens,
    topP: Number(global.top_p),
    systemPrompt: prompt?.content ?? "Você é um assistente de gestão de trabalho.",
  };
}

export async function generate(
  contents: GeminiContent[],
  cfg: AiConfig,
  responseMimeType?: "application/json",
): Promise<GeminiResult> {
  const apiKey = Deno.env.get("GEMINI_API_KEY");
  if (!apiKey) throw new Error("GEMINI_API_KEY não configurada");

  const started = Date.now();
  const res = await fetch(
    `${GEMINI_BASE}/models/${cfg.model}:generateContent`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json", "x-goog-api-key": apiKey },
      body: JSON.stringify({
        contents,
        systemInstruction: { parts: [{ text: cfg.systemPrompt }] },
        generationConfig: {
          temperature: cfg.temperature,
          maxOutputTokens: cfg.maxOutputTokens,
          topP: cfg.topP,
          ...(responseMimeType ? { responseMimeType } : {}),
        },
      }),
    },
  );

  if (!res.ok) {
    throw new Error(`Gemini ${res.status}: ${await res.text()}`);
  }

  const body = await res.json();
  const text: string =
    body?.candidates?.[0]?.content?.parts?.map((p: GeminiPart) => p.text ?? "").join("") ?? "";

  return {
    text,
    inputTokens: body?.usageMetadata?.promptTokenCount ?? 0,
    outputTokens: body?.usageMetadata?.candidatesTokenCount ?? 0,
    latencyMs: Date.now() - started,
  };
}

/** Registra consumo para cota e faturamento. Sempre chame, inclusive em erro. */
export async function recordUsage(
  admin: SupabaseClient,
  row: {
    tenant_id: string;
    user_id: string;
    feature: string;
    model: string;
    input_tokens: number;
    output_tokens: number;
    success: boolean;
    error?: string;
  },
) {
  await admin.from("ai_usage").insert(row);
}
