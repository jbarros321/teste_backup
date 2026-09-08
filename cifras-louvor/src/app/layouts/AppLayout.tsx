import { Music2, Heart, ListMusic, History, Settings } from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'

import { UserMenu } from '@/features/auth/UserMenu'
import { ThemeToggle } from '@/features/settings/ThemeToggle'
import { cn } from '@/lib/utils'

const NAV = [
  { to: '/', label: 'Músicas', icon: Music2, end: true },
  { to: '/favoritos', label: 'Favoritos', icon: Heart, end: false },
  { to: '/repertorios', label: 'Repertórios', icon: ListMusic, end: false },
  { to: '/historico', label: 'Histórico', icon: History, end: false },
  { to: '/configuracoes', label: 'Configurações', icon: Settings, end: false },
]

export function AppLayout() {
  return (
    <div className="min-h-dvh bg-background text-foreground">
      <header className="sticky top-0 z-40 border-b border-border bg-background/85 backdrop-blur">
        <div className="mx-auto flex h-14 max-w-6xl items-center gap-3 px-4">
          <NavLink to="/" className="flex items-center gap-2 font-semibold">
            <Music2 className="size-5 text-primary" aria-hidden />
            <span>Cifra Ministério</span>
          </NavLink>
          <div className="ml-auto flex items-center gap-1">
            <UserMenu />
            <ThemeToggle />
          </div>
        </div>
      </header>

      <div className="mx-auto flex max-w-6xl gap-6 px-4 py-6">
        <nav aria-label="Navegação principal" className="hidden w-48 shrink-0 md:block">
          <ul className="sticky top-20 space-y-1">
            {NAV.map(({ to, label, icon: Icon, end }) => (
              <li key={to}>
                <NavLink
                  to={to}
                  end={end}
                  className={({ isActive }) =>
                    cn(
                      'flex items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors',
                      isActive
                        ? 'bg-accent font-medium text-accent-foreground'
                        : 'text-muted-foreground hover:bg-accent/60 hover:text-foreground',
                    )
                  }
                >
                  <Icon className="size-4" aria-hidden />
                  {label}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        <main className="min-w-0 flex-1">
          <Outlet />
        </main>
      </div>

      <nav
        aria-label="Navegação principal"
        className="sticky bottom-0 z-40 border-t border-border bg-background/95 backdrop-blur md:hidden"
      >
        <ul className="flex">
          {NAV.map(({ to, label, icon: Icon, end }) => (
            <li key={to} className="flex-1">
              <NavLink
                to={to}
                end={end}
                className={({ isActive }) =>
                  cn(
                    'flex flex-col items-center gap-1 py-2 text-[11px]',
                    isActive ? 'text-primary' : 'text-muted-foreground',
                  )
                }
              >
                <Icon className="size-5" aria-hidden />
                {label}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
    </div>
  )
}
