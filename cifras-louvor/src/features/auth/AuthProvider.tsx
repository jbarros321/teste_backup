/**
 * Sessão do usuário e igreja ativa.
 *
 * Sem Supabase configurado, o app roda em modo demonstração: `status` fica
 * como `demo` e as telas usam o acervo de exemplo, sem tentar rede.
 */
import type { Session, User } from '@supabase/supabase-js'
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'

import { isSupabaseConfigured } from '@/lib/supabase'
import { authService } from '@/services/authService'
import { churchService, type Membership } from '@/services/churchService'

export type AuthStatus = 'loading' | 'demo' | 'signed_out' | 'signed_in'

interface AuthContextValue {
  status: AuthStatus
  user: User | null
  memberships: Membership[]
  /** Igreja em que o usuário está trabalhando agora. */
  activeChurch: Membership | null
  selectChurch: (churchId: string) => void
  refreshMemberships: () => Promise<void>
  signOut: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)
const ACTIVE_CHURCH_KEY = 'cifra-ministerio:igreja-ativa'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<AuthStatus>(isSupabaseConfigured ? 'loading' : 'demo')
  const [session, setSession] = useState<Session | null>(null)
  const [memberships, setMemberships] = useState<Membership[]>([])
  const [activeChurchId, setActiveChurchId] = useState<string | null>(() =>
    localStorage.getItem(ACTIVE_CHURCH_KEY),
  )

  const loadMemberships = useCallback(async () => {
    try {
      setMemberships(await churchService.listMine())
    } catch {
      // Falha de rede não pode derrubar a sessão; as telas mostram o vazio.
      setMemberships([])
    }
  }, [])

  useEffect(() => {
    if (!isSupabaseConfigured) return

    let active = true

    void authService.getSession().then((initial) => {
      if (!active) return
      setSession(initial)
      setStatus(initial ? 'signed_in' : 'signed_out')
    })

    const unsubscribe = authService.onAuthStateChange((next) => {
      if (!active) return
      setSession(next)
      setStatus(next ? 'signed_in' : 'signed_out')
    })

    return () => {
      active = false
      unsubscribe()
    }
  }, [])

  useEffect(() => {
    if (status === 'signed_in') void loadMemberships()
    else setMemberships([])
  }, [status, loadMemberships])

  const selectChurch = useCallback((churchId: string) => {
    localStorage.setItem(ACTIVE_CHURCH_KEY, churchId)
    setActiveChurchId(churchId)
  }, [])

  const activeChurch = useMemo(() => {
    if (memberships.length === 0) return null
    return memberships.find((item) => item.church.id === activeChurchId) ?? memberships[0]!
  }, [memberships, activeChurchId])

  const value = useMemo<AuthContextValue>(
    () => ({
      status,
      user: session?.user ?? null,
      memberships,
      activeChurch,
      selectChurch,
      refreshMemberships: loadMemberships,
      signOut: async () => {
        await authService.signOut()
        localStorage.removeItem(ACTIVE_CHURCH_KEY)
      },
    }),
    [status, session, memberships, activeChurch, selectChurch, loadMemberships],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth precisa estar dentro de <AuthProvider>')
  return context
}
