import { Check, Copy } from 'lucide-react'
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'

import { Alert } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { useAuth } from '@/features/auth/AuthProvider'
import {
  FONT_SIZE_MAX,
  FONT_SIZE_MIN,
  FONT_SIZE_STEP,
  useSettingsStore,
  type AccidentalPreference,
  type ThemePreference,
} from '@/features/settings/useSettingsStore'
import { churchService } from '@/services/churchService'
import type { ChurchRole } from '@/types/database'

const THEMES: Array<{ value: ThemePreference; label: string }> = [
  { value: 'system', label: 'Do sistema' },
  { value: 'light', label: 'Claro' },
  { value: 'dark', label: 'Escuro' },
]

const ACCIDENTALS: Array<{ value: AccidentalPreference; label: string; hint: string }> = [
  { value: 'auto', label: 'Automático', hint: 'Segue o tom: em Mi usa F#, em Mib usa Ab' },
  { value: 'sharp', label: 'Sustenidos (#)', hint: 'Sempre C#, D#, F#…' },
  { value: 'flat', label: 'Bemóis (b)', hint: 'Sempre Db, Eb, Gb…' },
]

const ROLE_LABELS: Record<ChurchRole, string> = {
  admin: 'Administrador',
  leader: 'Líder',
  musician: 'Músico',
  viewer: 'Espectador',
}

export default function SettingsPage() {
  const settings = useSettingsStore()
  const { activeChurch } = useAuth()

  return (
    <section className="space-y-5 pb-16">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">Configurações</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          As preferências ficam salvas neste dispositivo.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Aparência</CardTitle>
          <CardDescription>Como a cifra é exibida na tela.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-5">
          <div className="space-y-2">
            <Label htmlFor="theme">Tema</Label>
            <div className="flex flex-wrap gap-2" id="theme">
              {THEMES.map(({ value, label }) => (
                <Button
                  key={value}
                  type="button"
                  variant={settings.theme === value ? 'default' : 'outline'}
                  size="sm"
                  aria-pressed={settings.theme === value}
                  onClick={() => settings.setTheme(value)}
                >
                  {label}
                </Button>
              ))}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="font-size">
              Tamanho da letra: {Math.round(settings.fontSize * 100)}%
            </Label>
            <input
              id="font-size"
              type="range"
              className="w-full accent-[var(--primary)]"
              min={FONT_SIZE_MIN}
              max={FONT_SIZE_MAX}
              step={FONT_SIZE_STEP}
              value={settings.fontSize}
              onChange={(event) => settings.setFontSize(Number(event.target.value))}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="accidentals">Nomenclatura dos acordes</Label>
            <div className="flex flex-wrap gap-2" id="accidentals">
              {ACCIDENTALS.map(({ value, label, hint }) => (
                <Button
                  key={value}
                  type="button"
                  variant={settings.accidentals === value ? 'default' : 'outline'}
                  size="sm"
                  title={hint}
                  aria-pressed={settings.accidentals === value}
                  onClick={() => settings.setAccidentals(value)}
                >
                  {label}
                </Button>
              ))}
            </div>
            <p className="text-xs text-muted-foreground">
              {ACCIDENTALS.find((item) => item.value === settings.accidentals)?.hint}
            </p>
          </div>
        </CardContent>
      </Card>

      {activeChurch && <ChurchCard churchId={activeChurch.church.id} />}
    </section>
  )
}

function ChurchCard({ churchId }: { churchId: string }) {
  const { activeChurch } = useAuth()
  const [copied, setCopied] = useState(false)

  const { data: members } = useQuery({
    queryKey: ['members', churchId],
    queryFn: () => churchService.listMembers(churchId),
  })

  const church = activeChurch!.church
  const isAdmin = activeChurch!.role === 'admin' || activeChurch!.role === 'leader'

  return (
    <Card>
      <CardHeader>
        <CardTitle>{church.name}</CardTitle>
        <CardDescription>
          Seu papel: {ROLE_LABELS[activeChurch!.role]}
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-5">
        {isAdmin && (
          <div className="space-y-2">
            <Label>Código de convite</Label>
            <div className="flex items-center gap-2">
              <code className="rounded-md border border-border bg-muted px-3 py-1.5 font-mono text-sm tracking-widest">
                {church.invite_code}
              </code>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => {
                  void navigator.clipboard.writeText(church.invite_code).then(() => {
                    setCopied(true)
                    setTimeout(() => setCopied(false), 2000)
                  })
                }}
              >
                {copied ? <Check aria-hidden /> : <Copy aria-hidden />}
                {copied ? 'Copiado' : 'Copiar'}
              </Button>
            </div>
            <p className="text-xs text-muted-foreground">
              Quem tem este código entra na igreja como músico. Compartilhe só com a equipe.
            </p>
          </div>
        )}

        <div className="space-y-2">
          <Label>Equipe</Label>
          {members === undefined ? (
            <p className="text-sm text-muted-foreground">Carregando…</p>
          ) : members.length === 0 ? (
            <Alert>Nenhum membro listado.</Alert>
          ) : (
            <ul className="divide-y divide-border rounded-md border border-border">
              {members.map((member) => (
                <li key={member.id} className="flex items-center justify-between gap-3 px-3 py-2">
                  <span className="min-w-0">
                    <span className="block truncate text-sm font-medium">
                      {member.profile?.name ?? member.profile?.email ?? 'Membro'}
                    </span>
                    {member.profile?.name && (
                      <span className="block truncate text-xs text-muted-foreground">
                        {member.profile.email}
                      </span>
                    )}
                  </span>
                  <span className="shrink-0 text-xs text-muted-foreground">
                    {ROLE_LABELS[member.role]}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
