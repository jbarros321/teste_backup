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

  /**
   * Cria a conta. `needsEmailConfirmation` diz se o projeto exige confirmação:
   * nesse caso o Supabase devolve o usuário mas nenhuma sessão, e o login só
   * funciona depois que a pessoa clicar no link do e-mail.
   */
  async signUp(
    { email, password }: Credentials,
    name: string,
  ): Promise<{ user: User | null; needsEmailConfirmation: boolean }> {
    const { data, error } = await requireSupabase().auth.signUp({
      email,
      password,
      options: { data: { name } },
    })
    if (error) throw new Error(translateAuthError(error.message))
    return { user: data.user, needsEmailConfirmation: data.session === null }
  },

  /**
   * Quais métodos de login estão habilitados no projeto.
   *
   * Evita o botão morto: sem o provedor ligado no dashboard, o Google devolve
   * um erro genérico que não ajuda ninguém.
   */
  async getEnabledProviders(): Promise<{ google: boolean; email: boolean }> {
    const url = import.meta.env.VITE_SUPABASE_URL
    const anonKey = import.meta.env.VITE_SUPABASE_ANON_KEY
    if (!url || !anonKey) return { google: false, email: false }

    try {
      const response = await fetch(`${url}/auth/v1/settings`, { headers: { apikey: anonKey } })
      if (!response.ok) return { google: false, email: true }
      const settings = (await response.json()) as { external?: Record<string, boolean> }
      return {
        google: settings.external?.google === true,
        email: settings.external?.email !== false,
      }
    } catch {
      // Sem rede não dá para saber; assumimos o caminho que sempre existe.
      return { google: false, email: true }
    }
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
export function translateAuthError(message: string): string {
  // O limite de e-mails é o erro mais confuso da lista: nada está errado com a
  // senha, o projeto é que não consegue mandar o e-mail de confirmação.
  if (/email rate limit exceeded/i.test(message)) {
    return 'O Supabase atingiu o limite de e-mails (o serviço embutido envia poucos por hora). Desligue "Confirm email" em Authentication → Providers → Email, ou configure um SMTP próprio.'
  }

  // A política mínima varia por projeto; use o número que o servidor mandou.
  const length = /password should be at least (\d+)/i.exec(message)
  if (length) return `A senha precisa ter pelo menos ${length[1]} caracteres.`

  if (/weak password|known to be weak|pwned|compromised/i.test(message)) {
    return 'Esta senha é fraca ou apareceu em vazamentos conhecidos. Escolha outra.'
  }

  const table: Array<[RegExp, string]> = [
    [/invalid login credentials/i, 'E-mail ou senha incorretos.'],
    [/email not confirmed/i, 'Confirme seu e-mail antes de entrar — o link está na sua caixa de entrada.'],
    [/user already registered|already been registered/i, 'Já existe uma conta com este e-mail.'],
    [/unable to validate email|invalid email/i, 'E-mail inválido.'],
    [/signups not allowed|signup is disabled/i, 'O cadastro está desabilitado neste projeto Supabase.'],
    [/provider is not enabled|unsupported provider/i, 'Este método de login não está habilitado no projeto Supabase.'],
    [/rate limit|too many requests/i, 'Muitas tentativas. Aguarde um minuto e tente de novo.'],
    [/failed to fetch|networkerror/i, 'Sem conexão com o Supabase. Verifique a internet e as chaves em .env.local.'],
  ]

  for (const [pattern, translated] of table) {
    if (pattern.test(message)) return translated
  }
  return message
}
