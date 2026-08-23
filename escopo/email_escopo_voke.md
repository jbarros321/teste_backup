# E-mail — Formalização do escopo (Orçamentário Voke)

**Para:** Camila; Thais; Dieiny
**Assunto:** Escopo fechado — Orçamentário Voke (Folha/RH + Compras) — 134h
**Anexo:** `Escopo-Orcamentario-Voke-134h.docx`

---

Oi Camila, Thais e Dieiny,

Segue em anexo o escopo consolidado do Orçamentário da Voke, já com a ponta de Compras incluída. O documento fecha em **134 horas**, distribuídas em 8 fases, com 10 semanas de cronograma.

## O que o projeto entrega

São duas frentes de orçamento sobre uma base comum:

- **Folha / RH** — substitui as planilhas `Planejamento_Banco_de_Horas` e `Considerações_Folha` por um sistema com Orçado x Realizado x Desvio x Forecast, planejamento de vagas, movimentações/turnover e jornada.
- **Compras** — orçamento por Centro de Resultado e Natureza, com cenários versionados (orçado original, revisões e forecast).
- **Motor de Orçamento** — camada transversal que permite subir o orçamento por planilha ou gerá-lo por drivers (regra por natureza, empresa, CR, projeto, global ou % sobre meta de receita).

## Distribuição das horas

| Fase | Entrega | Horas |
|---|---|---:|
| 0 | Carga inicial, modelagem e infraestrutura | 12h |
| 1 | Orçamento core — Dashboard + DRE de Folha | 16h |
| 2 | Planejamento — Vagas, Movimentações, Colaboradores | 12h |
| 3 | Jornada — Banco de horas, HE, faltas | 10h |
| 4 | Integrações (mínimo contratual) | 35h |
| 5 | Agente IA + Testes + Deploy | 9h |
| 6 | Compras — CR, de-para Gerência, cenários | 28h |
| 7 | Motor de Orçamento — upload de planilha + drivers | 12h |
| | **Total** | **134h** |

## Dois pontos que vale destacar

**1. O Sankhya é a fonte única de dados.** Folha e Compras leem da mesma base — empresa, centro de resultado, natureza, projeto e parceiro vêm do cadastro do Sankhya, e o sistema espelha esses cadastros em vez de criar cadastro paralelo. Isso simplificou a arquitetura e é o que torna o consolidado das duas pontas nativo, sem de-para de cadastro.

A única exceção é o **de-para Centro de Resultado ↔ Gerência**, que fica dentro do sistema. Ele existe porque a estrutura organizacional da Voke se movimenta com frequência, mas o número do CR não pode mudar — há pedidos e contratos amarrados a ele. A camada de Gerência permite redirecionar um CR para outra diretoria sem tocar em nenhum parâmetro transacional. O documento detalha o funcionamento no item 6.

**2. Integrações estão no piso do contrato.** As 35h da Fase 4 são o mínimo contratual e representam 26% do projeto. Elas dependem de credenciais e confirmações do cliente — o caso mais sensível é o **Ponto Tel (8h)**, que já constava no escopo original como pendente de "confirmação de API no contrato". Se a API não estiver contratada, essas horas não viram entrega e a tela de Jornada segue operando por carga manual de planilha.

Vale a mesma observação para Mindsight e Power BI, em grau menor. O sistema opera desde a Fase 1 com os dados das planilhas, então nenhuma pendência de credencial trava o projeto — mas trava a substituição da carga manual.

## Dependências do cliente

Os itens abaixo são bloqueantes e valem entrar no acompanhamento de vocês desde já:

| Item | Bloqueia |
|---|---|
| Acesso Sankhya (URL, usuário de serviço, liberação das consultas) | Todo o Realizado |
| Planilha do de-para CR ↔ Gerência, com vigências | Fase 6 (Compras) |
| Responsável de cada CR | Fase 6 |
| Metas de receita por empresa/mês | Driver de % sobre receita |
| Regras de driver (quais naturezas e qual fórmula) | Fase 7 |
| Confirmação de API do Ponto Tel | 8h da Fase 4 |

## Backlog

Para fechar nas 134h, cerca de **18h** ficaram de fora e estão listadas no item 12 do documento — entre elas o empenho/comprometido de pedidos, drivers avançados (índice, per capita, escalonado) e workflow de coleta orçamentária. Registrei tudo com o motivo de cada exclusão, para servir de base caso vire aditivo ou segunda fase.

**Dieiny**, no aspecto comercial: as 134h são fechadas para o escopo descrito. As 18h do backlog e qualquer aprofundamento de integração além do previsto entram como aditivo.

Fico à disposição para revisar o documento com vocês antes de enviar ao cliente. Se identificarem algo fora do combinado com a Voke, me avisem que eu ajusto.

Abraço,

[nome] — Tech Lead
Neuon Soluções
