# 10 — Operação, Observabilidade e Implantação

## 1. Ambientes

| Ambiente | Projeto Supabase | Origem do deploy |
|---|---|---|
| local | `supabase start` (Docker) | máquina do desenvolvedor |
| staging | projeto dedicado | branch `develop` |
| production | projeto dedicado | branch `main` (com aprovação) |

Nenhuma alteração manual de schema em produção. Tudo passa por migration versionada e revisada.

## 2. Implantação inicial

```bash
# 1. CLI e vínculo
npm i -g supabase
supabase login
supabase link --project-ref <SEU_PROJECT_REF>

# 2. Schema
supabase db push

# 3. Segredos (nunca versionados)
supabase secrets set GEMINI_API_KEY=AIza...
supabase secrets set APP_ORIGIN=https://app.seudominio.com

# 4. Edge Functions
supabase functions deploy gemini-chat
supabase functions deploy gemini-agent
supabase functions deploy invite-accept

# 5. Tipos TypeScript para o front
supabase gen types typescript --linked > src/types/database.ts
```

Depois, no SQL Editor, uma única vez:

```sql
-- Promover você a super admin
insert into public.platform_admins (user_id, note)
values ((select id from auth.users where email = 'seu@email.com'), 'fundador');

-- Criar a primeira empresa
select public.fn_bootstrap_tenant(
  'TECWAY', 'tecway', 'tecway.com.br', 'responsavel@tecway.com.br', 'BUSINESS'
);

-- Habilitar IA para essa empresa
update public.ai_tenant_settings
set is_enabled = true, monthly_token_cap = 500000
where tenant_id = '<uuid retornado acima>';
```

## 3. Jobs agendados (pg_cron)

```sql
select cron.schedule('scan_due_tasks', '*/15 * * * *', $$select public.fn_scan_due_tasks()$$);
select cron.schedule('purge_trash',    '0 3 * * *',    $$select public.fn_purge_trash(30)$$);
```

## 4. Backup e recuperação

| Item | Política |
|---|---|
| Banco | PITR do Supabase (7 dias no Pro, 28 no Team) + dump diário para storage externo |
| Storage | replicação para bucket S3 secundário, diária |
| Segredos | cofre externo (1Password/Vault); nunca no repositório |
| Teste de restauração | trimestral, cronometrado, em projeto descartável |

**RPO alvo:** 5 minutos. **RTO alvo:** 2 horas. Um backup que nunca foi restaurado não é backup.

## 5. Observabilidade

| Sinal | Ferramenta | Alerta |
|---|---|---|
| Erros de front | Sentry | taxa de erro acima de 1% das sessões |
| Erros de Edge Function | Supabase Logs + Sentry | 5 falhas em 5 min na mesma função |
| Consultas lentas | `pg_stat_statements` | p95 acima de 500 ms |
| Conexões | painel Supabase | acima de 80% do pool |
| Falha de RLS (403 inesperado) | log estruturado no front | pico súbito indica policy quebrada |
| Consumo de IA | `ai_usage` | empresa acima de 80% da cota mensal |
| Fila de webhooks | `webhook_deliveries` | mais de 100 pendentes por 10 min |
| Automações | `automation_runs` | taxa de falha acima de 5% |

Consultas úteis de plantão:

```sql
-- Consultas mais caras
select calls, round(mean_exec_time::numeric, 2) as ms_medio, left(query, 120)
from pg_stat_statements order by mean_exec_time desc limit 20;

-- Empresas próximas do teto de IA
select t.name, public.ai_quota_left(t.id) as tokens_restantes
from public.tenants t join public.ai_tenant_settings a on a.tenant_id = t.id
where a.is_enabled order by 2 asc limit 20;

-- Tabelas sem RLS (deve retornar zero linhas)
select tablename from pg_tables
where schemaname = 'public' and rowsecurity = false;
```

Essa última consulta merece um teste automatizado: **qualquer tabela nova sem RLS deve quebrar o CI.**

## 6. CI/CD

```yaml
# .github/workflows/ci.yml (esqueleto)
on: [push, pull_request]
jobs:
  qualidade:
    steps:
      - run: npm ci
      - run: npm run typecheck
      - run: npm run lint
      - run: npm run test
      - run: supabase db start && supabase test db     # pgTAP: RLS e regras de negócio
  deploy-staging:
    if: github.ref == 'refs/heads/develop'
    steps:
      - run: supabase db push --linked
      - run: supabase functions deploy --project-ref ${{ secrets.STAGING_REF }}
      - run: npm run build && npx vercel deploy --prebuilt
```

## 7. Testes

| Camada | Ferramenta | Cobertura mínima |
|---|---|---|
| Banco (RLS e triggers) | pgTAP (`supabase test db`) | 100% das tabelas com RLS testado |
| Unitário (front) | Vitest | 70% das funções de domínio |
| Componente | Testing Library | telas críticas: tarefa, kanban, permissões |
| E2E | Playwright | login, criar tarefa, mover no kanban, convidar usuário, chat de IA |
| Carga | k6 | 500 usuários simultâneos, p95 abaixo de 800 ms |

Exemplo de teste de isolamento (pgTAP):

```sql
begin;
select plan(2);

set local role authenticated;
set local request.jwt.claims = '{"sub":"<usuario-do-tenant-A>","role":"authenticated"}';

select is((select count(*) from public.tasks where tenant_id = '<tenant-B>'), 0::bigint,
          'usuário do tenant A não enxerga tarefa do tenant B');

select throws_ok(
  $$update public.ai_settings set model_default = 'hack'$$,
  'usuário comum não altera a configuração de IA');

select * from finish();
rollback;
```

## 8. Runbook de incidentes

| Sintoma | Primeira verificação | Ação |
|---|---|---|
| "Sem permissão" em massa | `select tablename, rowsecurity from pg_tables` e policies alteradas | reverter a última migration de RLS |
| Lentidão geral | `pg_stat_statements` e conexões | matar query travada; revisar índice ausente |
| IA sem responder | logs da function e `ai_usage.error` | validar `GEMINI_API_KEY` e cota do provedor |
| Notificações paradas | `cron.job_run_details` | reagendar `fn_scan_due_tasks` |
| Webhooks acumulando | `webhook_deliveries` pendentes | verificar endpoint do cliente; pausar webhook |
| Upload falhando | policies de `storage.objects` e cota do plano | conferir prefixo `tenant/{id}/` do caminho |

## 9. Custo estimado (ordem de grandeza)

| Item | Faixa mensal |
|---|---|
| Supabase Pro | US$ 25 + uso |
| Hospedagem do front (Vercel/Netlify) | US$ 0 a 20 |
| Gemini (flash, ~2 M tokens) | US$ 5 a 30 |
| Sentry | US$ 0 a 26 |
| **Total inicial** | **US$ 30 a 100** |

O custo cresce com armazenamento, banda e uso de IA — os três já têm medição por tenant
(`usage_counters` e `ai_usage`), então o repasse por plano é calculável desde o primeiro cliente.
