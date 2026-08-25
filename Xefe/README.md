# Xefe — Plataforma de Gestão de Trabalho (modelo ClickUp)

Plataforma SaaS multiempresa (multi-tenant) de gestão de projetos, tarefas, processos,
documentos, metas, automações, dashboards e IA — construída sobre **Supabase (PostgreSQL + Auth
+ Storage + Realtime + Edge Functions)** e **React + Vite + TypeScript + Tailwind**.

## Princípios de acesso (decisões desta arquitetura)

| Papel | Quem é | O que faz |
|---|---|---|
| **Platform Admin (super admin)** | Você | Vê e controla **tudo**, em todos os tenants. Único que altera **configuração de IA**, planos, limites, chaves e integrações globais. |
| **Owner / Responsável do domínio** | Dono do domínio de e-mail (ex.: `@tecway.com.br`) | Administra a própria empresa: convida usuários, define **quais telas cada perfil enxerga**, cria equipes, espaços, projetos. **Não** altera IA. |
| **Admin / Gestor / Líder** | Delegados do owner | Escopo operacional conforme matriz de permissões. |
| **Colaborador** | Usuário comum | Só enxerga o que o perfil dele libera. Não configura acessos. |
| **Convidado (guest)** | Externo | Só enxerga recursos explicitamente compartilhados com ele. |

O vínculo usuário → empresa é feito **pelo domínio do e-mail** (`tenant_domains`). Quem entra com
e-mail de domínio verificado é anexado automaticamente ao tenant com o papel padrão daquele domínio.

## Estrutura do repositório

```
Xefe/
├── README.md
├── .env.example
├── docs/                       # Escopo técnico-funcional completo
│   ├── 00-visao-geral.md
│   ├── 01-escopo-funcional.md
│   ├── 02-arquitetura-tecnica.md
│   ├── 03-modelo-de-dados.md
│   ├── 04-seguranca-e-rls.md
│   ├── 05-matriz-de-permissoes.md
│   ├── 06-ia-gemini.md
│   ├── 07-api-e-contratos.md
│   ├── 08-frontend-e-telas.md
│   ├── 09-roadmap-e-fases.md
│   └── 10-operacao-e-observabilidade.md
└── supabase/
    ├── migrations/             # SQL pronto para `supabase db push`
    │   ├── 0001_extensions_e_tipos.sql
    │   ├── 0002_core_tenancy.sql               # empresas, domínios, papéis, telas
    │   ├── 0003_funcoes_de_autorizacao.sql     # funções usadas pelo RLS + entrada por domínio
    │   ├── 0004_hierarquia_e_projetos.sql      # espaços, pastas, listas, projetos, campos
    │   ├── 0005_tarefas.sql                    # tarefas, subtarefas, relações, checklists
    │   ├── 0006_colaboracao_e_arquivos.sql     # comentários, anexos, documentos, logs
    │   ├── 0007_tempo_metas_okr.sql
    │   ├── 0008_dashboards_automacoes_integracoes.sql
    │   ├── 0009_ia_gemini.sql                  # config de IA (só super admin) + consumo
    │   ├── 0010_rls_policies.sql               # ★ o coração da segurança
    │   ├── 0011_triggers.sql                   # auditoria, progresso, notificações, limites
    │   ├── 0012_views_e_rpcs.sql               # dashboards, carga, busca, Gantt
    │   ├── 0013_seed_permissoes_e_telas.sql    # catálogos + bootstrap de empresa
    │   └── 0014_hardening_final.sql            # trava do papel anon + checagem de RLS
    └── functions/              # Edge Functions (Deno)
        ├── _shared/            # CORS, clientes Supabase, cliente Gemini
        ├── gemini-chat/        # chat contextual (contexto lido sob RLS)
        ├── gemini-agent/       # gerar projeto, resumir, analisar risco
        └── invite-accept/      # aceite de convite por token
```

## Subir o ambiente em ~10 minutos

```bash
npm i -g supabase
supabase init && supabase link --project-ref <SEU_PROJECT_REF>
supabase db push                       # aplica todas as migrations em ordem
supabase secrets set GEMINI_API_KEY=...  MODEL_DEFAULT=gemini-2.5-flash
supabase functions deploy gemini-chat gemini-agent invite-accept
```

Depois, promova seu usuário a super admin (uma única vez, via SQL Editor):

```sql
insert into public.platform_admins (user_id, note)
values ((select id from auth.users where email = 'seu@email.com'), 'fundador');
```

## Leitura recomendada na ordem

1. `docs/00-visao-geral.md` — contexto e glossário
2. `docs/04-seguranca-e-rls.md` — modelo de isolamento (o coração do sistema)
3. `supabase/migrations/` — schema executável
4. `docs/06-ia-gemini.md` — IA com chave server-side
