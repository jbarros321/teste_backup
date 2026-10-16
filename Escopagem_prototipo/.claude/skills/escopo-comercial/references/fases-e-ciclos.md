# Fases e ciclos de entrega

## Antes de tudo: o modelo de fases é do cliente, não da skill

Se já existe documento anterior deste cliente, **mantenha o modelo de fases dele** e
encaixe o escopo novo nesse formato. Trocar o vocabulário que o cliente já aprovou
custa confiança e não entrega nada. Pergunte ao usuário qual é esse modelo e
reproduza os nomes de fase dele. Dois formatos que aparecem com frequência:

- **Projeto de dezenas de horas, com piloto em campo** — Planejamento ·
  Desenvolvimento · Teste em campo · Refino · Implantação.
- **Projeto de centenas de horas, multi-módulo** — ondas de implantação, cada uma
  com as 7 fases abaixo.

Diga no próprio documento que o modelo foi mantido e por quê ("já é o adotado com o
cliente"). As 7 fases a seguir são o **default para projeto novo**, não um padrão a
ser imposto sobre um histórico que já existe. Quando o modelo do cliente não tem
fase de entrada em produção, a estabilização é absorvida pela fase de implantação
ou de entrega — nunca como fase extra com percentual exposto.

## O ciclo completo (default para projeto novo)

Cada onda do roadmap é um **ciclo de entrega completo**: começa em detalhamento e
termina em produção assistida. Nenhuma onda entrega sem passar pelas 7 fases.

Dentro do documento comercial isso vira duas coisas: o capítulo 7 ganha a tabela
de distribuição por fase, e cada onda ganha sua própria linha de fases com horas
e percentual.

## As 7 fases do ciclo

| # | Fase | O que acontece | Marco de saída |
|---|---|---|---|
| F1 | Detalhamento funcional | Validação das regras com o responsável por área; desenho das telas e dos parâmetros | Documento funcional aprovado |
| F2 | Parametrização | Configuração do padrão da plataforma: cadastros, domínios, fluxos, perfis | Ambiente configurado |
| F3 | Desenvolvimento | Personalizações, módulos novos, relatórios e painéis da onda | Itens entregues em homologação |
| F4 | Integrações | Importadores, exportadores, retorno bancário e remessas da onda | Integração executando ponta a ponta |
| F5 | Migração de dados | Carga dos blocos da onda, deduplicação e conciliação de saldo de abertura | Dados conferidos pelo cliente |
| F6 | Testes e homologação | Teste integrado, roteiro de aceite, correção de apontamentos | Termo de homologação da onda |
| F7 | Treinamento e go-live | Treinamento por perfil, entrada em produção e operação assistida | Onda em produção |

## Distribuição padrão por fase

Use como base. Ajuste quando a onda for atípica — e, se ajustar, diga por quê no
próprio documento.

| Fase | % da onda | Observação |
|---|---|---|
| F1 · Detalhamento funcional | 10% | 12% quando a onda tem regra legal nova |
| F2 · Parametrização | 12% | sobe em ondas de fundação |
| F3 · Desenvolvimento | 35% | núcleo do esforço |
| F4 · Integrações | 10% | 0% em onda sem integração — redistribua em F3 |
| F5 · Migração de dados | 12% | 0% em onda sem carga — redistribua em F3 |
| F6 · Testes e homologação | 13% | nunca abaixo de 10% |
| F7 · Treinamento e go-live | 8% | mínimo de 8 h por onda; absorve a estabilização pós-entrada em produção, sem linha própria |
| **Total** | **100%** | |

Perfis alternativos:

- **Onda de fundação** (muito cadastro, pouca regra): F1 10 · F2 22 · F3 25 · F4 8 · F5 14 · F6 13 · F7 8
- **Onda de desenvolvimento novo** (motor, módulo inédito): F1 12 · F2 6 · F3 48 · F4 6 · F5 6 · F6 14 · F7 8
- **Onda de integração / migração**: F1 10 · F2 6 · F3 22 · F4 24 · F5 18 · F6 12 · F7 8
- **Onda de inteligência** (painéis, prestação de contas): F1 12 · F2 8 · F3 42 · F4 8 · F5 4 · F6 18 · F7 8

Regras de consistência:

1. Os percentuais de cada onda somam exatamente 100%.
2. F6 nunca abaixo de 10% e F7 nunca abaixo de 8 h — são as fases que o cliente
   mais tenta cortar e as que mais custam depois.
3. Fase com 0% é escrita como "não se aplica nesta onda", não é omitida da tabela.
4. A soma das horas de todas as fases de todas as ondas = total da capa.

## Formato no documento (capítulo 7)

Primeiro a tabela de ondas com o total de horas, como em `estrutura.md`. Depois,
a distribuição por fase — uma linha por onda, uma coluna por fase, em horas:

| Onda | F1 | F2 | F3 | F4 | F5 | F6 | F7 | Total |
|---|---|---|---|---|---|---|---|---|
| 1 · Fundação e controle financeiro | 48 | 106 | 120 | 38 | 67 | 62 | 39 | 480 |
| 2 · Orçamento e compliance | 58 | 29 | 230 | 29 | 29 | 67 | 38 | 480 |
| **Total** | | | | | | | | **N** |

E o consolidado por fase, com horas e percentual do projeto inteiro:

| Fase | Horas | % do projeto | Entregável da fase |
|---|---|---|---|
| F1 · Detalhamento funcional | 224 | 10% | Documento funcional aprovado por onda |

Sob a tabela, uma frase de leitura comercial: quanto do esforço total é
desenvolvimento novo versus configuração, e em que mês o cliente começa a
capturar valor (derivado das horas pela premissa de carga).

## Ciclo de aceite

Deixe explícito no capítulo 11, porque é o que sustenta o faturamento por onda:

- A onda é considerada entregue com o **termo de homologação** assinado ao fim de F6.
- Prazo de homologação pelo cliente: 10 dias úteis por onda. Silêncio além disso
  conta como aceite tácito.
- Apontamento dentro do escopo da onda é correção, incluída nas horas de F6.
- Apontamento fora do escopo da onda é aditivo, estimado em horas pelo mesmo
  catálogo de `estimativa-horas.md`.
- As ondas são sequenciais: F7 de uma onda e F1 da seguinte podem se sobrepor, o
  restante não.
