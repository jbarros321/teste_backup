# 00 — Visão Geral

## 1. Produto

**Xefe** é uma plataforma de gestão de trabalho no modelo ClickUp: centraliza projetos, tarefas,
processos, documentos, metas, tempo, automações, indicadores e colaboração em um único ambiente
SaaS multiempresa.

## 2. Objetivos

- Centralizar a gestão de projetos e atividades da empresa.
- Organizar equipes, responsáveis e carga de trabalho.
- Controlar tarefas, prazos e dependências.
- Oferecer múltiplas visualizações do mesmo dado (Lista, Kanban, Calendário, Gantt, Timeline).
- Centralizar documentos e conhecimento.
- Automatizar processos repetitivos (quando → condição → ação).
- Gerar dashboards, indicadores e relatórios.
- Registrar histórico e auditoria completos.
- Integrar com sistemas externos (ERP, CRM, Teams, Slack, WhatsApp, Power BI).
- Usar IA (Google Gemini) como copiloto de produtividade.

## 3. Glossário

| Termo | Definição |
|---|---|
| **Tenant / Workspace** | Empresa. Unidade máxima de isolamento de dados. |
| **Domínio** | Domínio de e-mail vinculado a um tenant (`@empresa.com`). Define entrada automática. |
| **Responsável do domínio** | Usuário `owner` — administra a empresa e os acessos às telas. |
| **Área / Departamento** | Agrupador organizacional dentro do tenant. |
| **Space (Espaço)** | Grande divisão de trabalho (ex.: "Projetos Estratégicos"). |
| **Folder (Pasta)** | Agrupador de listas dentro de um espaço. |
| **List (Lista)** | Container de tarefas. Pode ou não estar dentro de uma pasta. |
| **Project (Projeto)** | Entidade de gestão com orçamento, cliente, datas e indicadores. |
| **Task / Subtask** | Unidade de trabalho. Subtarefa = tarefa com `parent_task_id`. |
| **Screen (Tela)** | Chave de UI (`tasks.kanban`, `admin.ia`) cujo acesso é configurável por papel. |
| **Platform Admin** | Super administrador da plataforma (fora do modelo de tenant). |

## 4. Hierarquia funcional

```
EMPRESA (tenant)
└── ÁREA / DEPARTAMENTO
    └── ESPAÇO (space)
        └── PASTA (folder)
            └── LISTA (list)
                └── TAREFA (task)
                    ├── SUBTAREFA (task.parent_task_id)
                    └── CHECKLIST → ITENS
```

Exemplo real:

```
TECWAY
└── Financeiro
    └── Projetos Estratégicos
        └── Projeto Fluxo de Caixa
            └── Desenvolvimento
                └── Criar tela de indicadores
                    ├── Criar layout
                    ├── Desenvolver API
                    └── Validar dados
```

## 5. Modelo de acesso em uma frase

> **O super admin controla tudo (inclusive IA). O responsável do domínio controla quem entra e
> quais telas cada perfil vê. O usuário comum apenas usa o que lhe foi liberado. O convidado só
> enxerga o que foi explicitamente compartilhado.**

## 6. Não-objetivos da Fase 1

- Aplicativo mobile nativo (Fase 6 — React Native).
- Editor colaborativo com CRDT em tempo real (Fase 2 usa versionamento otimista).
- Billing com gateway de pagamento (Fase 5).
