import { Music2 } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Navigate } from 'react-router-dom'

import { Alert } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useAuth } from '@/features/auth/AuthProvider'
import { isSupabaseConfigured } from '@/lib/supabase'
import { authService } from '@/services/authService'

type Mode = 'signin' | 'signup' | 'recover'

const TITLES: Record<Mode, string> = {
  signin: 'Entrar',
  signup: 'Criar conta',
  recover: 'Recuperar senha',
}

export default function LoginPage() {
  const { status } = useAuth()
  const [mode, setMode] = useState<Mode>('signin')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  if (status === 'signed_in') return <Navigate to="/" replace />

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setNotice(null)
    setBusy(true)

    try {
      if (mode === 'signin') {
        await authService.signIn({ email, password })
      } else if (mode === 'signup') {
        await authService.signUp({ email, password }, name.trim())
        setNotice('Conta criada. Se o projeto exigir confirmação, verifique seu e-mail.')
      } else {
        await authService.resetPassword(email)
        setNotice('Enviamos um link de recuperação para o seu e-mail.')
      }
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Não foi possível concluir.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="mx-auto flex min-h-dvh max-w-md flex-col justify-center gap-6 px-4 py-10">
      <div className="text-center">
        <span className="mx-auto flex size-12 items-center justify-center rounded-xl bg-primary text-primary-foreground">
          <Music2 className="size-6" aria-hidden />
        </span>
        <h1 className="mt-4 text-2xl font-semibold tracking-tight">Cifra Ministério</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Cifras, tom, metrônomo e repertório para o seu ministério de louvor.
        </p>
      </div>

      {!isSupabaseConfigured && (
        <Alert>
          O Supabase ainda não foi configurado. Copie <code>.env.example</code> para{' '}
          <code>.env.local</code>, preencha as chaves e reinicie o servidor. Até lá o app funciona em
          modo demonstração, com o acervo de exemplo.
        </Alert>
      )}

      <Card>
        <CardContent className="p-5">
          <form className="space-y-4" onSubmit={handleSubmit}>
            <h2 className="text-lg font-semibold">{TITLES[mode]}</h2>

            {mode === 'signup' && (
              <div className="space-y-1.5">
                <Label htmlFor="name">Seu nome</Label>
                <Input
                  id="name"
                  autoComplete="name"
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                  required
                />
              </div>
            )}

            <div className="space-y-1.5">
              <Label htmlFor="email">E-mail</Label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                required
              />
            </div>

            {mode !== 'recover' && (
              <div className="space-y-1.5">
                <Label htmlFor="password">Senha</Label>
                <Input
                  id="password"
                  type="password"
                  autoComplete={mode === 'signup' ? 'new-password' : 'current-password'}
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  minLength={6}
                  required
                />
              </div>
            )}

            {error && <Alert variant="destructive">{error}</Alert>}
            {notice && <Alert>{notice}</Alert>}

            <Button type="submit" className="w-full" disabled={busy || !isSupabaseConfigured}>
              {busy ? 'Aguarde…' : TITLES[mode]}
            </Button>

            <Button
              type="button"
              variant="outline"
              className="w-full"
              disabled={busy || !isSupabaseConfigured}
              onClick={() => {
                setError(null)
                authService.signInWithGoogle().catch((cause: unknown) => {
                  setError(cause instanceof Error ? cause.message : 'Falha ao entrar com Google.')
                })
              }}
            >
              Entrar com Google
            </Button>
          </form>

          <div className="mt-4 flex flex-wrap justify-between gap-2 text-sm">
            {mode !== 'signin' && (
              <button type="button" className="text-primary hover:underline" onClick={() => setMode('signin')}>
                Já tenho conta
              </button>
            )}
            {mode !== 'signup' && (
              <button type="button" className="text-primary hover:underline" onClick={() => setMode('signup')}>
                Criar conta
              </button>
            )}
            {mode !== 'recover' && (
              <button type="button" className="text-primary hover:underline" onClick={() => setMode('recover')}>
                Esqueci a senha
              </button>
            )}
          </div>
        </CardContent>
      </Card>
    </main>
  )
}
