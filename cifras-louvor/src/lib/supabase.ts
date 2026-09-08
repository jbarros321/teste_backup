/**
 * Cliente Supabase.
 *
 * Só a anon key entra aqui — a service_role ignora o RLS e daria a qualquer
 * visitante acesso ao conteúdo de todas as igrejas.
 *
 * Sem `.env.local`, o app entra em MODO DEMONSTRAÇÃO: o acervo de exemplo
 * continua funcionando e nada tenta ir para a rede.
 */
import { createClient, type SupabaseClient } from '@supabase/supabase-js'

const url = import.meta.env.VITE_SUPABASE_URL
const anonKey = import.meta.env.VITE_SUPABASE_ANON_KEY

export const isSupabaseConfigured = Boolean(url && anonKey && !url.includes('SEU-PROJETO'))

export const supabase: SupabaseClient | null = isSupabaseConfigured
  ? createClient(url as string, anonKey as string, {
      auth: { persistSession: true, autoRefreshToken: true, detectSessionInUrl: true },
    })
  : null

/** Use nos serviços: falha alto e claro em vez de estourar com `null`. */
export function requireSupabase(): SupabaseClient {
  if (!supabase) {
    throw new Error(
      'Supabase não está configurado. Copie .env.example para .env.local e preencha as chaves.',
    )
  }
  return supabase
}
