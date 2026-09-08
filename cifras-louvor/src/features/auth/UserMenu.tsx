import { LogOut } from 'lucide-react'

import { Button } from '@/components/ui/button'

import { useAuth } from './AuthProvider'

/** Igreja ativa + sair. Em modo demonstração mostra só o aviso. */
export function UserMenu() {
  const { status, memberships, activeChurch, selectChurch, signOut } = useAuth()

  if (status === 'demo') {
    return (
      <span className="rounded-full border border-border px-2.5 py-1 text-xs text-muted-foreground">
        Modo demonstração
      </span>
    )
  }

  if (status !== 'signed_in') return null

  return (
    <div className="flex items-center gap-2">
      {memberships.length > 1 ? (
        <>
          <label className="sr-only" htmlFor="active-church">
            Igreja ativa
          </label>
          <select
            id="active-church"
            className="h-8 max-w-[10rem] rounded-md border border-input bg-transparent px-2 text-sm"
            value={activeChurch?.church.id ?? ''}
            onChange={(event) => selectChurch(event.target.value)}
          >
            {memberships.map(({ church }) => (
              <option key={church.id} value={church.id}>
                {church.name}
              </option>
            ))}
          </select>
        </>
      ) : (
        activeChurch && (
          <span className="hidden max-w-[12rem] truncate text-sm text-muted-foreground sm:inline">
            {activeChurch.church.name}
          </span>
        )
      )}

      <Button variant="ghost" size="icon" aria-label="Sair" title="Sair" onClick={() => void signOut()}>
        <LogOut aria-hidden />
      </Button>
    </div>
  )
}
