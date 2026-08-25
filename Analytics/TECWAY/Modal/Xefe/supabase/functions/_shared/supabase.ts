import { createClient, SupabaseClient } from "https://esm.sh/@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY")!;
const SERVICE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

/**
 * Cliente que herda o JWT do usuário: TODAS as consultas passam por RLS.
 * Use este para ler dados de negócio — é a garantia de que a IA nunca
 * enxerga nada além do que o próprio usuário enxergaria.
 */
export function userClient(req: Request): SupabaseClient {
  const authorization = req.headers.get("Authorization") ?? "";
  return createClient(SUPABASE_URL, ANON_KEY, {
    global: { headers: { Authorization: authorization } },
    auth: { persistSession: false },
  });
}

/**
 * Cliente privilegiado: ignora RLS. Use SOMENTE para gravar telemetria,
 * mensagens de IA e notificações — nunca para ler dados de negócio.
 */
export function adminClient(): SupabaseClient {
  return createClient(SUPABASE_URL, SERVICE_KEY, { auth: { persistSession: false } });
}

export async function requireUser(req: Request) {
  const supabase = userClient(req);
  const { data, error } = await supabase.auth.getUser();
  if (error || !data.user) throw new Response("Não autenticado", { status: 401 });
  return { supabase, user: data.user };
}

/** Confirma que o usuário é membro ativo do tenant informado. */
export async function requireMembership(
  supabase: SupabaseClient,
  tenantId: string,
  userId: string,
) {
  const { data, error } = await supabase
    .from("memberships")
    .select("id, role, status")
    .eq("tenant_id", tenantId)
    .eq("user_id", userId)
    .maybeSingle();

  if (error) throw new Error(error.message);
  if (!data || data.status !== "active") {
    throw new Response("Sem acesso a esta empresa", { status: 403 });
  }
  return data;
}
