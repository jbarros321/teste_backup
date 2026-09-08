import { Link } from 'react-router-dom'

import { buttonVariants } from '@/components/ui/button'

export default function NotFoundPage() {
  return (
    <section className="py-16 text-center">
      <p className="text-sm font-medium text-primary">404</p>
      <h1 className="mt-2 text-2xl font-semibold tracking-tight">Página não encontrada</h1>
      <p className="mt-2 text-sm text-muted-foreground">
        O link pode estar errado ou a música foi removida.
      </p>
      <Link to="/" className={`${buttonVariants()} mt-6`}>
        Voltar para as músicas
      </Link>
    </section>
  )
}
