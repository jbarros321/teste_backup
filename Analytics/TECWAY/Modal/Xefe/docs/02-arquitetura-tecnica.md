# 02 — Arquitetura Técnica

## 1. Decisão de arquitetura

A plataforma é **Supabase-first**: o PostgreSQL é a fonte de verdade e também o motor de segurança
(RLS). O cliente conversa diretamente com o banco via PostgREST e Realtime; toda lógica que exige
segredo (chave do Gemini, webhooks assinados, e-mail, billing) roda em **Edge Functions** com
`service_role`.

Consequência prática: não existe backend intermediário obrigatório na Fase 1. Isso reduz custo,
latência e superfície de bug, e concentra a segurança em um único lugar auditável — as policies.

```
┌──────────────────────────────────────────────────────────────────┐
│  CLIENTES                                                        │
│  Web (React + Vite + TS + Tailwind)   •   Mobile (React Native)  │
└───────────────┬──────────────────────────────────┬───────────────┘
                │ supabase-js (JWT do usuário)     │
                ▼                                  ▼
┌──────────────────────────────┐   ┌──────────────────────────────┐
│  SUPABASE API GATEWAY        │   │  EDGE FUNCTIONS (Deno)       │
│  • PostgREST (CRUD + RPC)    │   │  • gemini-chat               │
│  • Realtime (WS)             │   │  • gemini-agent              │
│  • Storage (S3 compatível)   │   │  • invite-accept             │
│  • GoTrue (Auth/JWT)         │   │  • webhooks-dispatch         │
└──────────────┬───────────────┘   │  • automations-runner        │
               │                   │  • notifications-fanout      │
               │                   └───────────┬──────────────────┘
               ▼                               │ service_role
┌──────────────────────────────────────────────▼──────────────────┐
│  POSTGRESQL 15                                                  │
│  • Schema multi-tenant (tenant_id em toda tabela operacional)   │
│  • RLS em 100% das tabelas + funções SECURITY DEFINER           │
│  • Triggers (auditoria, progresso, notificações, automações)    │
│  • pg_cron (jobs), pgmq/queue (fila), pg_net (HTTP saída)       │
│  • Views e RPCs de agregação para dashboards                    │
└──────────────┬──────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────┐
│  EXTERNOS: Gemini API • SMTP/Resend • Slack • Teams • WhatsApp   │
└──────────────────────────────────────────────────────────────────┘
```

## 2. Stack

| Camada | Tecnologia | Justificativa |
|---|---|---|
| Front | React 18 + Vite + TypeScript | Build rápido, SPA com rotas protegidas |
| UI | Tailwind CSS + shadcn/ui + Radix | Consistência e acessibilidade sem reinventar componentes |
| Estado servidor | TanStack Query | Cache, invalidação e otimismo no CRUD |
| Estado local | Zustand | Store leve para UI (filtros, painéis, tema) |
| Formulários | React Hook Form + Zod | Validação compartilhada com o backend |
| Drag and drop | dnd-kit | Kanban, reordenação de listas e widgets |
| Gantt | gantt-task-react ou implementação própria em SVG | Dependências e caminho crítico |
| Gráficos | Recharts | Dashboards |
| Editor | TipTap (ProseMirror) | Documentos e descrição de tarefas |
| Datas | date-fns + date-fns-tz | Fuso por tenant e por usuário |
| Backend | Supabase (PostgreSQL, Auth, Storage, Realtime, Edge Functions/Deno) | Segurança no banco, menos código de servidor |
| IA | Google Gemini API | Chat, geração de projetos, resumos e análise de risco |
| Observabilidade | Supabase Logs + Sentry | Erros de front e de function |
| CI/CD | GitHub Actions | Lint, typecheck, testes, `supabase db push`, deploy |

## 3. Fluxo de autenticação e entrada por domínio

```
1. Usuário faz signup/login (e-mail e senha, magic link ou Google OAuth).
2. GoTrue cria a linha em auth.users.
3. Trigger on_auth_user_created:
   a. cria public.profiles espelhando id, e-mail, nome e avatar;
   b. extrai o domínio do e-mail;
   c. procura tenant_domains verificado com esse domínio;
      - achou  -> cria membership com o default_role do domínio (status active);
      - não achou -> procura invitations pendente para o e-mail;
          - achou -> cria membership com o papel do convite e marca o convite como aceito;
          - não achou -> usuário fica sem tenant (tela "aguardando convite").
4. O front chama a RPC me() e recebe: perfil, memberships, papel, permissões e telas liberadas.
5. O tenant ativo é guardado no cliente e enviado em cada query como filtro; o RLS revalida.
```

Importante: o tenant ativo **nunca** é fonte de autorização — é apenas conveniência de UI. A
autorização real vem sempre de `memberships` lida dentro das policies.

## 4. Módulos do front-end

```
src/
├── app/                # bootstrap, providers, router, guards
├── modules/
│   ├── auth/           # login, signup, convite, recuperação
│   ├── workspace/      # spaces, folders, lists
│   ├── projects/
│   ├── tasks/          # lista, kanban, calendário, gantt, timeline, detalhe
│   ├── docs/
│   ├── goals/
│   ├── time/           # cronômetro, timesheet, carga
│   ├── dashboards/
│   ├── automations/
│   ├── ai/             # chat, agente, sugestões
│   ├── notifications/
│   ├── settings/       # empresa, usuários, papéis, telas, domínios
│   └── admin/          # somente platform_admin: tenants, planos, IA, logs
├── components/         # design system
├── lib/                # supabase client, guards, permissions, formatters
└── types/              # tipos gerados por `supabase gen types typescript`
```

## 5. Guardas de rota e de tela

```ts
// lib/permissions.ts
export const can = (perm: Permission) => session.permissions.includes(perm);
export const seesScreen = (key: ScreenKey) => session.screens.includes(key);
export const isPlatformAdmin = () => session.isPlatformAdmin;

// app/router.tsx
<Route path="/admin/ia" element={
  <Guard when={isPlatformAdmin()} fallback={<Forbidden />}>
    <AiSettingsPage />
  </Guard>
} />
```

O guard de UI é apenas cosmético. A mesma regra é reforçada por RLS: mesmo que alguém force a rota
ou chame a API na mão, o banco recusa.

## 6. Realtime

- Canal por tenant e entidade: `tenant:{id}:tasks`, `tenant:{id}:notifications`.
- Realtime respeita RLS: o cliente só recebe as linhas que poderia ler via select.
- Usos: movimentação de card no Kanban, novos comentários, notificações e presença.

## 7. Jobs e assíncrono

| Job | Frequência | Implementação |
|---|---|---|
| Prazo próximo e atraso | a cada 15 min | `pg_cron` chamando `fn_scan_due_tasks()` |
| Runner de automações | contínuo | fila em tabela mais Edge Function acionada por `pg_net` |
| Dispatch de webhooks | contínuo | fila `webhook_deliveries` com retry exponencial |
| Recalcular agregados de dashboard | a cada 10 min | refresh de views materializadas |
| Expurgo da lixeira | diário | `fn_purge_trash(30)` |

## 8. Performance

- Índice em `tenant_id` em toda tabela operacional, sempre como primeira coluna de índices compostos.
- Índices compostos para as consultas quentes: `(tenant_id, list_id, position)`,
  `(tenant_id, status_id)`, `(tenant_id, due_date)`, `(tenant_id, assignee_id, due_date)`.
- Funções usadas em policies marcadas `stable` e chamadas como `(select fn())` para virar InitPlan
  (avaliada uma vez por query, não por linha).
- Paginação por keyset (`position`, `created_at`) em vez de `offset` em listas grandes.
- Views materializadas para os cards de dashboard mais pesados.
- `EXPLAIN ANALYZE` obrigatório em toda query nova que ultrapasse 50 ms no dataset de carga.

## 9. Ambientes

| Ambiente | Projeto Supabase | Uso |
|---|---|---|
| local | `supabase start` (Docker) | desenvolvimento e testes |
| staging | projeto dedicado | homologação e QA |
| production | projeto dedicado | clientes |

Migrations versionadas em `supabase/migrations`, aplicadas por CI. Nenhuma alteração manual em
produção pelo painel: tudo passa por migration revisada.
