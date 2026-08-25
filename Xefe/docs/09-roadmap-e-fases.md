# 09 — Roadmap e Fases

Estimativas para um time de **2 desenvolvedores full-stack + 1 designer meio-período**.
Ajuste proporcionalmente.

## Fase 0 — Fundação (1 semana)

Projeto Supabase criado, migrations `0001`–`0013` aplicadas, super admin promovido, primeiro tenant
criado por `fn_bootstrap_tenant`, projeto React iniciado com autenticação e `rpc('me')` funcionando.

**Pronto quando:** dois usuários de tenants diferentes fazem login e nenhum vê dado do outro —
comprovado pelo checklist da seção 9 de `04-seguranca-e-rls.md`.

## Fase 1 — MVP (6 a 8 semanas)

Login e convites · multiempresa por domínio · usuários e papéis · **tela de acesso às telas** ·
equipes e departamentos · espaços, pastas e listas · projetos · tarefas e subtarefas · checklists ·
comentários com menção · anexos · visão Lista · visão Kanban · notificações in-app · busca global.

**Pronto quando:** uma equipe real substitui a planilha atual por esta ferramenta.

## Fase 2 — Gestão avançada (4 a 6 semanas)

Calendário · Gantt com dependências e caminho crítico · Timeline · dashboards com widgets ·
relatórios com exportação · cronômetro · timesheet com aprovação · carga de trabalho · documentos
colaborativos · campos personalizados · templates.

**Pronto quando:** um gestor consegue planejar, acompanhar e prestar contas sem sair da plataforma.

## Fase 3 — Automação e integração (3 a 4 semanas)

Editor visual de automações · runner assíncrono com log · webhooks assinados com retry · API keys
com escopo e rate limit · integrações com e-mail, Slack, Teams, WhatsApp e Google Calendar.

**Pronto quando:** uma automação de produção roda por 7 dias sem intervenção e com log auditável.

## Fase 4 — Inteligência Artificial (3 a 4 semanas)

Painel de configuração de IA (exclusivo do super admin) · chat contextual · geração de projeto com
aprovação humana · resumos · assistente de risco · cotas e telemetria de consumo.

**Pronto quando:** a IA responde sobre dados reais respeitando RLS e o consumo por empresa é
visível e limitado.

## Fase 5 — SaaS comercial (4 a 6 semanas)

Planos e assinaturas · billing com gateway · controle de limites em tempo real · onboarding
self-service · área administrativa completa · impersonação com auditoria · relatórios de receita.

**Pronto quando:** uma empresa nova assina, paga e usa sem intervenção manual.

## Fase 6 — Mobile (6 a 8 semanas)

App React Native: tarefas, criação rápida, comentários, notificações push, apontamento de horas,
dashboard resumido e aprovações.

---

## Ordem de implementação dentro da Fase 1

Sequência que evita retrabalho — cada item usa o anterior:

```
1.  migrations 0001–0003        base + funções de autorização
2.  auth + rpc me()             sessão com papel, telas e permissões
3.  migrations 0004–0005        hierarquia + tarefas
4.  CRUD de espaços/listas      valida RLS de escrita na prática
5.  CRUD de tarefas             o coração do produto
6.  visão Lista                 mais simples que Kanban, mesma query
7.  visão Kanban                reaproveita a query da Lista + fn_move_task
8.  detalhe da tarefa           subtarefas, checklists, comentários, anexos
9.  usuários e convites         cresce o time de teste
10. tela de acesso às telas     o owner passa a se autoconfigurar
11. notificações + Realtime     fecha o ciclo de colaboração
12. busca global                depende de tudo acima já existir
```

## Riscos e mitigação

| Risco | Impacto | Mitigação |
|---|---|---|
| Policy de RLS mal escrita vaza dados entre tenants | Crítico | Suíte pgTAP obrigatória no CI, com um teste por tabela |
| Gantt e editor de documentos consomem mais tempo que o previsto | Alto | São os primeiros candidatos a biblioteca pronta; não construa do zero na Fase 2 |
| Custo de IA sem controle | Médio | Cota por tenant desde o primeiro dia, `flash` como padrão, telemetria obrigatória |
| Consulta lenta com volume real | Médio | Dataset de carga com 500 mil tarefas antes do lançamento; `EXPLAIN ANALYZE` em toda query nova |
| Escopo do MVP crescer | Alto | A Fase 1 está fechada acima; qualquer adição empurra data, não entra de graça |
