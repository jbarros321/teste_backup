import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'

import { useAuth } from './AuthProvider'

/**
 * Portão das rotas internas.
 *
 * Em modo demonstração (sem Supabase) deixa passar: o acervo de exemplo é
 * público e é o que permite conferir a interface antes de ligar o backend.
 */
export function RequireAuth({ children }: { children: ReactNode }) {
  const { status, memberships } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return (
      <div className="grid min-h-dvh place-items-center text-sm text-muted-foreground" role="status">
        Carregando…
      </div>
    )
  }

  if (status === 'signed_out') {
    return <Navigate to="/entrar" replace state={{ from: location.pathname }} />
  }

  if (status === 'signed_in' && memberships.length === 0 && location.pathname !== '/igreja') {
    return <Navigate to="/igreja" replace />
  }

  return <>{children}</>
}
