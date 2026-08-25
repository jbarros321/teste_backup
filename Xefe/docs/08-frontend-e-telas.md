# 08 — Front-end e Telas

## 1. Estrutura de navegação

```
┌──────────────────────────────────────────────────────────────────────┐
│ [Logo] Empresa ▾    🔍 Buscar...           🔔  🤖 IA   👤 Perfil     │
├──────────┬───────────────────────────────────────────────────────────┤
│ Início   │                                                           │
│ Tarefas  │   Conteúdo da tela ativa                                  │
│ Projetos │   (a lista lateral só mostra o que screen_access liberou) │
│ Espaços  │                                                           │
│ Docs     │                                                           │
│ Metas    │                                                           │
│ Tempo    │                                                           │
│ Dash     │                                                           │
│ Automaç. │                                                           │
│ IA       │                                                           │
│ ─────    │                                                           │
│ Config.  │                                                           │
│ Admin ★  │  ★ visível apenas para o platform admin                   │
└──────────┴───────────────────────────────────────────────────────────┘
```

## 2. Telas e critérios de aceite

### Autenticação
| Tela | Critérios |
|---|---|
| Login | e-mail/senha, magic link, Google; erro claro; "esqueci a senha" |
| Cadastro | e-mail de domínio verificado entra direto na empresa; fora disso, exige convite |
| Convite | aceita por token, valida e-mail correspondente, expira em 7 dias |
| Sem empresa | tela "aguardando convite" com contato do responsável do domínio |

### Trabalho
| Tela | Critérios |
|---|---|
| Início | minhas tarefas de hoje, atrasadas, menções, atividade recente |
| Tarefas — Lista | colunas configuráveis, agrupamento, edição inline, seleção em massa, paginação por keyset |
| Tarefas — Kanban | drag & drop com atualização otimista, WIP limit, swimlanes, contagem por coluna |
| Tarefas — Calendário | dia/semana/mês, arrastar altera datas, criar clicando no dia |
| Tarefas — Gantt | dependências, arrastar barra, caminho crítico destacado, zoom dia/semana/mês |
| Tarefas — Timeline | faixas por responsável ou projeto |
| Detalhe da tarefa | painel lateral ou página; descrição rich text, subtarefas, checklists, comentários, anexos, tempo, relações, campos personalizados, histórico |
| Projetos | cards com progresso, risco, orçamento × realizado, responsável |
| Espaços/Listas | árvore com drag & drop, criação inline, cores e ícones |

### Apoio
| Tela | Critérios |
|---|---|
| Documentos | editor TipTap, árvore de páginas, versões, comentários, compartilhamento |
| Metas / OKRs | barra de progresso, atualização de valor com histórico, status calculado |
| Cronômetro | start/pause/stop flutuante, um único timer ativo, vínculo com tarefa |
| Timesheet | grade dia × projeto, submissão, aprovação, rejeição com motivo |
| Carga de trabalho | barras capacidade × utilizado, sobrecarga em vermelho, redistribuição por drag |
| Dashboards | grid arrastável, widgets configuráveis, filtros globais, exportação |
| Relatórios | filtros, prévia, exportação PDF/Excel/CSV |
| Automações | editor visual quando → condição → ação, teste a seco, log de execuções |
| IA — Chat | histórico de conversas, contexto atual, indicador de cota |
| IA — Assistente | cards de achados (atrasos, riscos, sobrecarga) com ação direta |

### Configurações (responsável do domínio)
| Tela | Critérios |
|---|---|
| Empresa | nome, CNPJ, logo, fuso, moeda, semana útil |
| Usuários | lista com papel e status, convidar, reenviar, bloquear, transferir responsabilidades |
| Perfis e Permissões | matriz papel × permissão, sobrescrita por usuário |
| **Acesso às Telas** | matriz papel × tela com toggles; telas `platform_only` aparecem travadas |
| Domínios | adicionar domínio, verificar por DNS TXT, definir papel padrão, ligar/desligar auto-join |
| Plano e Consumo | plano atual, uso de usuários/projetos/armazenamento/tokens de IA |
| Auditoria | trilha filtrável por usuário, tabela, período |

### Admin (somente platform admin)
| Tela | Critérios |
|---|---|
| Empresas | criar via `fn_bootstrap_tenant`, suspender, entrar como (impersonar com log) |
| **Configuração de IA** | modelo, temperatura, tokens, prompts versionados, habilitar por empresa e cota |
| Planos | CRUD de planos e limites |
| Consumo | tokens de IA e armazenamento por empresa, com projeção mensal |
| Logs | auditoria global e erros de function |

## 3. Guardas

```ts
// A UI esconde; o banco recusa. As duas coisas, sempre.
const { data: session } = useSession()          // vem de rpc('me')

const canSeeScreen = (key: ScreenKey) =>
  session.isPlatformAdmin || session.screens.includes(key)

const can = (perm: Permission) =>
  session.isPlatformAdmin || session.permissions.includes(perm)

// Menu lateral montado a partir do que o banco liberou
const menu = SCREENS.filter(s => canSeeScreen(s.key))
```

Nunca escreva `if (role === 'admin')` no front. O papel muda; a permissão é o contrato.

## 4. Padrões de UX

**Atualização otimista** no Kanban e nos checkboxes: aplica localmente, envia, e reverte com toast
se o servidor recusar. Arrastar um card e esperar 400 ms para ele assentar é a diferença entre uma
ferramenta que se usa o dia inteiro e uma que se abandona.

**Paginação por keyset** nas listas grandes (`position`/`created_at` como cursor). `offset` degrada
linearmente e a lista de tarefas de uma empresa ativa passa de 100 mil linhas.

**Estados vazios úteis**: toda tela vazia oferece a ação seguinte (criar, importar, aplicar
template), nunca só um ícone cinza.

**Atalhos**: `T` nova tarefa, `/` busca, `C` comentar, `Esc` fechar painel, `Ctrl+K` command palette.

**Acessibilidade**: contraste AA, foco visível, navegação por teclado no Kanban (mover card com
setas mais `Shift`), `aria-live` para toasts.

**Tema**: claro e escuro seguindo `prefers-color-scheme`, com escolha explícita persistida.

## 5. Responsividade

| Faixa | Comportamento |
|---|---|
| ≥ 1280 px | layout completo com painel lateral de detalhe |
| 1024–1279 px | menu colapsado em ícones; detalhe vira modal |
| 768–1023 px | Kanban com scroll horizontal; Gantt em modo compacto |
| < 768 px | navegação inferior; Lista e Detalhe apenas; Gantt e Timeline indisponíveis (aviso explícito) |

## 6. Performance no cliente

- `@tanstack/react-virtual` nas listas acima de 100 itens.
- Code splitting por módulo (Gantt e editor de documentos são os maiores; carregam sob demanda).
- `staleTime` de 30 s nas queries de listagem, invalidação por Realtime.
- Debounce de 300 ms na busca; `abortSignal` cancelando a requisição anterior.
- Orçamento: primeira renderização útil abaixo de 2 s em 3G rápido; bundle inicial abaixo de 250 KB
  comprimido.
