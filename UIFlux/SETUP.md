# UIFlux — Guia de Configuração

## 1. Instalar dependências

Foi adicionada a dependência `@supabase/supabase-js`. Rode:

```bash
npm install
```

## 2. Criar o projeto no Supabase

1. Acesse https://supabase.com e crie um projeto (guarde a senha do banco).
2. No painel do projeto, abra **SQL Editor**.
3. Cole todo o conteúdo de [`supabase/schema.sql`](./supabase/schema.sql) e clique em **Run**.
   - Isso cria as tabelas `companies`, `profiles`, `projects`, `documents`, as políticas
     de segurança (RLS) multi-empresa e o gatilho que cria empresa + perfil ao cadastrar.
4. Vá em **Authentication > Providers** e habilite **Email** (Email/Password).
   - Para testar mais rápido, você pode desativar "Confirm email" em
     Authentication > Providers > Email (aí o cadastro já entra direto, sem confirmar o email).

## 3. Configurar as chaves

1. No painel: **Project Settings > API**.
2. Copie a **Project URL** e a chave **anon public**.
3. Na raiz do projeto, copie `.env.example` para `.env` e preencha:

```env
VITE_SUPABASE_URL=https://SEU-PROJETO.supabase.co
VITE_SUPABASE_ANON_KEY=sua-chave-anon-publica
```

## 4. Rodar

```bash
npm run dev
```

- **Cadastro**: em "Criar conta da empresa", o primeiro usuário vira `admin` da empresa.
- **Login**: email + senha reais, validados pelo Supabase.
- Sem `.env` configurado, o app roda em **modo demonstração** (dados locais, qualquer login entra).

## Exportar PDF

No editor, use **Imprimir / PDF**. O relatório POP formatado (diagrama, etapas, matriz RACI,
checklists) é gerado em folha A4 branca — corrigido via CSS `@media print` em `src/index.css`.

---

## Status / Roadmap

| Item | Status |
|------|--------|
| Correção da exportação para PDF | ✅ Feito |
| Login real (Supabase Auth) | ✅ Feito |
| Schema multi-empresa + RLS | ✅ Feito (rodar o SQL) |
| **Fase 2:** salvar projetos no banco (hoje ainda em localStorage) | ⏳ Próximo |
| **Fase 2:** histórico de documentos POP gerados | ⏳ Próximo |
| Papéis dentro da empresa (admin/gestor/membro) na UI | ⏳ Próximo |
