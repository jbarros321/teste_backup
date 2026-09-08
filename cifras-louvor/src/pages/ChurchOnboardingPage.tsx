import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'

import { Alert } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useAuth } from '@/features/auth/AuthProvider'
import { churchService } from '@/services/churchService'

/**
 * Primeira coisa depois do cadastro: o usuário cria a igreja dele ou entra
 * numa existente. Sem igreja não há conteúdo — todo dado do sistema pertence
 * a uma.
 */
export default function ChurchOnboardingPage() {
  const { refreshMemberships, selectChurch } = useAuth()
  const navigate = useNavigate()

  const [churchName, setChurchName] = useState('')
  const [inviteCode, setInviteCode] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function run(action: () => Promise<{ id: string }>) {
    setError(null)
    setBusy(true)
    try {
      const church = await action()
      await refreshMemberships()
      selectChurch(church.id)
      navigate('/', { replace: true })
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Não foi possível concluir.')
    } finally {
      setBusy(false)
    }
  }

  const handleCreate = (event: FormEvent) => {
    event.preventDefault()
    void run(() => churchService.create(churchName.trim()))
  }

  const handleJoin = (event: FormEvent) => {
    event.preventDefault()
    void run(() => churchService.joinByInviteCode(inviteCode.trim()))
  }

  return (
    <main className="mx-auto flex min-h-dvh max-w-lg flex-col justify-center gap-5 px-4 py-10">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">Sua igreja</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          As músicas e os repertórios pertencem à igreja. Crie a sua ou entre em uma existente.
        </p>
      </div>

      {error && <Alert variant="destructive">{error}</Alert>}

      <Card>
        <CardHeader>
          <CardTitle>Criar uma igreja</CardTitle>
          <CardDescription>Você entra como administrador e convida o restante da equipe.</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="flex flex-col gap-3 sm:flex-row" onSubmit={handleCreate}>
            <div className="flex-1 space-y-1.5">
              <Label htmlFor="church-name" className="sr-only">
                Nome da igreja
              </Label>
              <Input
                id="church-name"
                placeholder="Ex.: Igreja Batista Central"
                value={churchName}
                onChange={(event) => setChurchName(event.target.value)}
                required
              />
            </div>
            <Button type="submit" disabled={busy || churchName.trim().length < 3}>
              Criar
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Entrar com um convite</CardTitle>
          <CardDescription>Peça o código de convite a quem administra a igreja.</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="flex flex-col gap-3 sm:flex-row" onSubmit={handleJoin}>
            <div className="flex-1 space-y-1.5">
              <Label htmlFor="invite-code" className="sr-only">
                Código de convite
              </Label>
              <Input
                id="invite-code"
                placeholder="Ex.: 4F2A9C1B"
                value={inviteCode}
                onChange={(event) => setInviteCode(event.target.value.toUpperCase())}
                required
              />
            </div>
            <Button type="submit" variant="secondary" disabled={busy || inviteCode.trim().length < 4}>
              Entrar
            </Button>
          </form>
        </CardContent>
      </Card>
    </main>
  )
}
