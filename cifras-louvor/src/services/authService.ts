/**
 * Porta de entrada para autenticação. Nenhum componente fala com o Supabase
 * direto — é isso que permite trocar o backend sem reescrever a interface.
 */
import type { Session, User } from '@supabase/supabase-js'

import { requireSupabase, supabase } from '@/lib/supabase'

export interface Credentials {
  email: string
  password: string
}

export const authService = {
  async getSession(): Promise<Session | null> {
    if (!supabase) return null
    const { data } = await supabase.auth.getSession()
    return data.session
  },

  onAuthStateChange(callback: (session: Session | null) => void): () => void {
    if (!supabase) return () => {}
    const { data } = supabase.auth.onAuthStateChange((_event, session) => callback(session))
    return () => data.subscription.unsubscribe()
  },

  async signIn({ email, password }: Credentials): Promise<User> {
    const { data, error } = await requireSupabase().auth.signInWithPassword({ email, password })
    if (error) throw new Error(translateAuthError(error.message))
    return data.user
  },

  async signUp({ email, password }: Credentials, name: string): Promise<User | null> {
    const { data, error } = await requireSupabase().auth.signUp({
      email,
      password,
      options: { data: { name } },
    })
    if (error) throw new Error(translateAuthError(error.message))
    return data.user
  },

  async signInWithGoogle(): Promise<void> {
    const { error } = await requireSupabase().auth.signInWithOAuth({
      provider: 'google',
      options: { redirectTo: `${window.location.origin}/` },
    })
    if (error) throw new Error(translateAuthError(error.message))
  },

  async resetPassword(email: string): Promise<void> {
    const { error } = await requireSupabase().auth.resetPasswordForEmail(email, {
      redirectTo: `${window.location.origin}/nova-senha`,
    })
    if (error) throw new Error(translateAuthError(error.message))
  },

  async signOut(): Promise<void> {
    if (!supabase) return
    await supabase.auth.signOut()
  },
}

/** As mensagens do Supabase vêm em inglês; o músico não tem que lidar com isso. */
function translateAuthError(message: string): string {
  const table: Array<[RegExp, string]> = [
    [/invalid login credentials/i, 'E-mail ou senha incorretos.'],
    [/email not confirmed/i, 'Confirme seu e-mail antes de entrar.'],
    [/user already registered/i, 'Já existe uma conta com este e-mail.'],
    [/password should be at least (\d+)/i, 'A senha precisa ter pelo menos 6 caracteres.'],
    [/unable to validate email/i, 'E-mail inválido.'],
    [/rate limit|too many requests/i, 'Muitas tentativas. Aguarde um instante e tente de novo.'],
    [/provider is not enabled/i, 'Este método de login não está habilitado no projeto Supabase.'],
  ]

  for (const [pattern, translated] of table) {
    if (pattern.test(message)) return translated
  }
  return message
}
