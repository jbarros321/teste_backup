/**
 * invite-accept — Aceite de convite por token, para quem JÁ tem conta.
 * (Quem se cadastra pela primeira vez é vinculado pelo trigger handle_new_user.)
 *
 * POST { token }
 */
import { corsHeaders, json, fail } from "../_shared/cors.ts";
import { userClient, adminClient } from "../_shared/supabase.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return fail("Método não permitido", 405);

  const supabase = userClient(req);
  const admin = adminClient();

  const { data: auth } = await supabase.auth.getUser();
  if (!auth?.user) return fail("Não autenticado", 401);

  const { token } = await req.json().catch(() => ({ token: null }));
  if (!token) return fail("token é obrigatório");

  const email = (auth.user.email ?? "").toLowerCase();

  // Lookup pelo service_role: o convidado ainda não tem acesso à linha via RLS.
  const { data: invite } = await admin
    .from("invitations")
    .select("*")
    .eq("token", token)
    .is("accepted_at", null)
    .is("revoked_at", null)
    .gt("expires_at", new Date().toISOString())
    .maybeSingle();

  if (!invite) return fail("Convite inválido, expirado ou já utilizado", 404);
  if (invite.email.toLowerCase() !== email) {
    return fail("Este convite foi emitido para outro e-mail", 403);
  }

  const { error } = await admin.from("memberships").insert({
    tenant_id: invite.tenant_id,
    user_id: auth.user.id,
    role: invite.role,
    status: "active",
    invited_by: invite.invited_by,
  });

  // Conflito de unicidade = já era membro; tratamos como sucesso idempotente.
  if (error && !error.message.includes("duplicate key")) {
    return fail(error.message, 400);
  }

  await admin.from("invitations").update({ accepted_at: new Date().toISOString() }).eq("id", invite.id);

  return json({ tenant_id: invite.tenant_id, role: invite.role });
});
