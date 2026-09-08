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
outra, e desfaz tudo no fim.

- **Aprovado:** devolve uma linha com `resultado = APROVADO`.
- **Reprovado:** aborta com erro em vermelho dizendo o que vazou. Nesse caso
  **não suba para produção** e me avise.

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

O app **esconde** o botão "Entrar com Google" enquanto o provedor estiver
desligado, para não oferecer um caminho que não funciona. Para habilitar:

1. No **Google Cloud Console** → *APIs e Serviços* → *Credenciais* → *Criar
   credenciais* → *ID do cliente OAuth* → tipo **Aplicativo da Web**.
2. Em *URIs de redirecionamento autorizados*, cole:
   `https://sdobcmjsnsbummbyhmqj.supabase.co/auth/v1/callback`
3. No Supabase, **Authentication → Sign In / Providers → Google**: habilite e
   cole o *Client ID* e o *Client Secret*.
4. Em **Authentication → URL Configuration**, adicione `http://localhost:5173`
   em *Redirect URLs*.

O botão aparece sozinho assim que o provedor ficar ligado — o app consulta os
provedores habilitados ao abrir a tela de login.

## 7. Desligar a confirmação de e-mail (importante no início)

Em **Authentication → Sign In / Providers → Email**, desligue **Confirm email**
e salve.

Isto não é só conveniência. Com a confirmação ligada, o Supabase usa o serviço
de e-mail embutido, que envia **poucas mensagens por hora e só para endereços da
equipe do projeto**. Na segunda tentativa de cadastro você bate no limite e
recebe `email rate limit exceeded` — um erro que parece problema de senha e não
é.

Com *Confirm email* desligado, o cadastro já devolve a sessão e você entra
direto.

Antes de colocar no ar, religue a confirmação **e** configure um SMTP próprio em
**Project Settings → Authentication → SMTP Settings** (Resend, Brevo, SendGrid…).
Sem SMTP próprio, o e-mail de confirmação não chega para os músicos da igreja.

## Primeiro acesso

1. Crie sua conta em `/entrar`.
2. O app leva você para `/igreja`: crie a igreja (você entra como administrador).
3. Os outros músicos criam a conta e entram com o **código de convite** da igreja.

O código fica em `churches.invite_code`. A tela de administração para vê-lo e
trocá-lo chega no M4.
