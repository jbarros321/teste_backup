# Estrutura do escopo comercial — 12 capítulos

Padrão extraído de `Escopo_Comercial_Baluarte.docx`. Mantenha a ordem. Capítulo
sem insumo: escreva o que há e marque "a confirmar no detalhamento", nunca omita
silenciosamente os capítulos 4.2, 7 e 11.

## Capa

```
PROPOSTA DE ESCOPO
<Nome do sistema / solução>
Documento comercial — visão de negócio, escopo, fases e entregáveis
```

Quadro de identificação (tabela de duas colunas):

| Campo | Conteúdo |
|---|---|
| Cliente | Razão social |
| CNPJ | |
| Responsável pela implantação | Neuon Soluções |
| **Escopo elaborado por** | **Nome — papel** (obrigatório; ver regra 1 da SKILL) |
| Plataforma-base | Sankhya (ERP) + módulos de personalização |
| **Esforço total estimado** | **N horas**, em M ondas de entrega |
| Versão do documento | 1.0 — mês/ano |

Abaixo do quadro, uma linha dizendo de onde veio a análise ("Documento elaborado
a partir da análise integral de ... 5 planilhas (32 abas), 3 relatórios fiscais e
748 arquivos eletrônicos").

## 1. Sumário executivo

Três parágrafos: o que o cliente faz, por que o modelo atual chegou ao limite, o
que a solução propõe. Depois dois quadros:

- **Os cinco ganhos centrais** — `Ganho | O que muda na prática`. O ganho é efeito
  de negócio, não funcionalidade.
- **Números do desafio atual** — `Indicador | Situação hoje`. Só números que saem
  do insumo do cliente (volumes, divergências, retrabalho).

## 2. O problema que o projeto resolve

Tabela `Situação identificada | Risco para o negócio`. Uma linha por achado da
análise. Fecha com um parágrafo de raiz comum ("a planilha registra o resultado,
mas não impõe a regra").

## 3. Princípios que orientam a solução

Tabela `Princípio | Significado`. Só princípios declarados pelo cliente ou
derivados de regra de negócio dura — nunca preferência técnica do implantador.

## 4. Escopo da solução

- **4.1 Blocos incluídos** — `# | Bloco | O que contempla`.
- **4.2 Fora do escopo desta fase** — lista corrida, cada item com a fronteira
  explícita ("o sistema consome o resultado da folha, não calcula"). Obrigatório.

## 5. Módulos da solução

Tabela `Módulo | Entrega de negócio | Horas estimadas`. Códigos M01..Mnn, com
nota de quais módulos são transversais. A coluna de horas é a mesma do capítulo 6
e precisa somar o total da capa.

## 6. Aproveitamento da plataforma e esforço de personalização

Duas tabelas:

1. `Necessidade | Plataforma padrão | Esforço de personalização` — a coluna do
   meio usa só: **Atende · Parcial · Insuficiente · Não atende**.
2. `Natureza | Horas | % | Onde se concentra` — aproveitamento do padrão,
   adaptação de recursos existentes, desenvolvimento novo. Horas antes do
   percentual; o percentual é derivado, não o contrário.

## 7. Roadmap de implantação

Tabela `Onda | Horas | Conteúdo | Entregável de negócio`. Uma onda por linha,
sequenciadas para tirar a equipe do processo manual antes dos módulos de
inteligência. Cada entregável é verificável e escrito em uma frase de efeito
("a equipe para de digitar pagamento em planilha").

Depois da tabela de ondas, inclua a **distribuição por fase** (as 7 fases do
ciclo de entrega, em horas e percentual) e o consolidado por fase — formato e
percentuais em `fases-e-ciclos.md`.

Fecha com: **esforço total estimado de N horas**; se o usuário pediu prazo,
acrescente a derivação ("a N h/mês por frente, equivale a ~M meses") com a
premissa de carga visível.

## 8. Integrações previstas

Tabela `Integração | Direção | Prioridade`. Direção: Entrada/Saída. Prioridade:
Alta/Média/Baixa. Diga explicitamente quando a integração é por exportação em
lote sem dependência de API de terceiro — isso remove risco de cronograma.

## 9. Migração de dados

Parágrafo com o número de blocos de carga e a lista. Depois tabela
`Ponto de atenção | Tratamento previsto`, fechando com a frase de que são pontos
já endereçados, não riscos abertos.

## 10. Compromissos de qualidade da solução

Tabela `Tema | Compromisso`. Temas recorrentes: auditoria, guarda documental,
precisão numérica, isolamento, desempenho, exportação, disponibilidade, LGPD,
usabilidade. Compromisso verificável (número, prazo, formato), não adjetivo.

## 11. Premissas e responsabilidades

Três listas: responsabilidades da Neuon, responsabilidades do cliente, premissas
gerais. Nas premissas, incluir sempre: base de horas e carga considerada, que as
ondas são sequenciais e que alteração de escopo dentro da onda vira aditivo.

## 12. Próximos passos

Tabela `# | Passo | Responsável`. Tipicamente: validação comercial do escopo
(cliente) → fechamento de proposta e contrato → kick-off e detalhamento da Onda 1
→ início da implantação.

## Rodapé

`Neuon Soluções — Escopo comercial · versão X · mês/ano · elaborado por <Nome>`

## Tom

- Terceira pessoa, afirmativo, sem futuro vago ("o sistema alerta", não "poderá alertar").
- Zero vocabulário de implementação: sem tabela, campo, trigger, SQL, JSP, snk.
- Citação do cliente entre aspas quando sustenta um princípio — com autor e papel.
- Valores em R$ com separador brasileiro; horas como número inteiro.
