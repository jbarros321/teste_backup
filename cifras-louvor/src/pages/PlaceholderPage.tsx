import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export function PlaceholderPage({
  title,
  description,
  milestone,
}: {
  title: string
  description: string
  milestone: string
}) {
  return (
    <section className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
        <p className="mt-1 text-sm text-muted-foreground">{description}</p>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>Em construção</CardTitle>
          <CardDescription>Esta tela chega no {milestone}.</CardDescription>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          O scaffold (M0) já está de pé: tema claro/escuro/palco, rotas, estado global e testes.
        </CardContent>
      </Card>
    </section>
  )
}
