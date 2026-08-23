import { createClient } from '@supabase/supabase-js';

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL as string | undefined;
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY as string | undefined;

/**
 * Indica se as credenciais do Supabase foram configuradas no arquivo .env.
 * Enquanto for `false`, o app roda em "modo offline" (localStorage) sem quebrar.
 */
export const isSupabaseConfigured = Boolean(supabaseUrl && supabaseAnonKey);

if (!isSupabaseConfigured) {
  // Aviso amigável no console em vez de derrubar a aplicação.
  console.warn(
    '[UIFlux] Supabase não configurado. Preencha VITE_SUPABASE_URL e ' +
      'VITE_SUPABASE_ANON_KEY no arquivo .env para habilitar login real e banco externo. ' +
      'Rodando em modo offline (localStorage) por enquanto.'
  );
}

/**
 * Cliente único do Supabase para toda a aplicação.
 * Se as chaves não estiverem configuradas, criamos um cliente com placeholders
 * apenas para não quebrar os imports — nenhuma chamada real deve ser feita nesse estado.
 */
export const supabase = createClient(
  supabaseUrl ?? 'https://placeholder.supabase.co',
  supabaseAnonKey ?? 'placeholder-anon-key',
  {
    auth: {
      persistSession: true,
      autoRefreshToken: true,
      detectSessionInUrl: true,
    },
  }
);
