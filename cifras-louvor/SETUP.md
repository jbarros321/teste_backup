# Configurar o Supabase

Leva uns 10 minutos. Enquanto não fizer isso, o app roda em **modo demonstração**
com o acervo de exemplo — nada quebra.

## 1. Criar o projeto

1. Entre em <https://supabase.com/dashboard> e clique em **New project**.
2. Nome: `cifra-ministerio`. Escolha a região mais próxima (South America).
3. Guarde a senha do banco que ele gerar.

## 2. Rodar as migrations

No dashboard, abra **SQL Editor → New query** e rode os arquivos **nesta ordem**,
um de cada vez:

1. `supabase/migrations/0001_schema.sql` — tabelas e índices
2. `supabase/migrations/0002_rls.sql` — Row Level Security
3. `supabase/migrations/0003_functions.sql` — gatilhos, busca e RPCs

Cada um deve terminar com "Success. No rows returned".

## 3. Conferir o isolamento entre igrejas

Ainda no SQL Editor, rode `supabase/tests/rls_isolation.sql`.

Ele cria duas igrejas de mentira, verifica que uma não enxerga nem escreve na
outra, e desfaz tudo no fim. Precisa terminar com a mensagem
`OK: isolamento entre igrejas confirmado.` — se abortar com "VAZAMENTO", **não
suba para produção** e me avise.

## 4. Pegar as chaves

**Project Settings → API**:

- `Project URL` → `VITE_SUPABASE_URL`
- `anon public` → `VITE_SUPABASE_ANON_KEY`

> A chave `service_role` **nunca** entra no frontend: ela ignora o RLS e daria
> acesso ao conteúdo de todas as igrejas. Ela só vale dentro de Edge Functions.

## 5. Ligar no app

```bash
cp .env.example .env.local
# edite .env.local e cole as duas chaves
npm run dev
```

`.env.local` já está no `.gitignore`.

## 6. Login com Google (opcional)

**Authentication → Providers → Google**: habilite e cole o Client ID e o Secret
do Google Cloud Console. Nas **URL Configuration**, adicione
`http://localhost:5173` em *Redirect URLs*.

Sem isso, o botão "Entrar com Google" avisa que o provedor não está habilitado —
o login por e-mail e senha continua funcionando.

## 7. Durante o desenvolvimento

Em **Authentication → Providers → Email**, desligue *Confirm email* para não
precisar confirmar cada conta de teste. Religue antes de colocar no ar.

## Primeiro acesso

1. Crie sua conta em `/entrar`.
2. O app leva você para `/igreja`: crie a igreja (você entra como administrador).
3. Os outros músicos criam a conta e entram com o **código de convite** da igreja.

O código fica em `churches.invite_code`. A tela de administração para vê-lo e
trocá-lo chega no M4.
