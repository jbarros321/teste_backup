import { lazy, Suspense } from 'react'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'

import { RequireAuth } from '@/features/auth/RequireAuth'

import { AppLayout } from './layouts/AppLayout'

const HomePage = lazy(() => import('@/pages/HomePage'))
const SongPage = lazy(() => import('@/pages/SongPage'))
const FavoritesPage = lazy(() => import('@/pages/FavoritesPage'))
const RepertoiresPage = lazy(() => import('@/pages/RepertoiresPage'))
const HistoryPage = lazy(() => import('@/pages/HistoryPage'))
const SettingsPage = lazy(() => import('@/pages/SettingsPage'))
const LoginPage = lazy(() => import('@/pages/LoginPage'))
const ChurchOnboardingPage = lazy(() => import('@/pages/ChurchOnboardingPage'))
const NotFoundPage = lazy(() => import('@/pages/NotFoundPage'))

function PageFallback() {
  return (
    <div className="py-16 text-center text-sm text-muted-foreground" role="status">
      Carregando…
    </div>
  )
}

const router = createBrowserRouter([
  { path: '/entrar', element: <LoginPage /> },
  {
    path: '/igreja',
    element: (
      <RequireAuth>
        <ChurchOnboardingPage />
      </RequireAuth>
    ),
  },
  {
    path: '/',
    element: (
      <RequireAuth>
        <AppLayout />
      </RequireAuth>
    ),
    children: [
      { index: true, element: <HomePage /> },
      { path: 'musica/:slug', element: <SongPage /> },
      { path: 'favoritos', element: <FavoritesPage /> },
      { path: 'repertorios', element: <RepertoiresPage /> },
      { path: 'historico', element: <HistoryPage /> },
      { path: 'configuracoes', element: <SettingsPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])

export function AppRouter() {
  return (
    <Suspense fallback={<PageFallback />}>
      <RouterProvider router={router} />
    </Suspense>
  )
}
