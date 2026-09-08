import { AppProviders } from '@/app/AppProviders'
import { AppRouter } from '@/app/router'
import { AuthProvider } from '@/features/auth/AuthProvider'
import { useAppliedTheme } from '@/features/settings/useAppliedTheme'

export default function App() {
  useAppliedTheme()

  return (
    <AppProviders>
      <AuthProvider>
        <AppRouter />
      </AuthProvider>
    </AppProviders>
  )
}
