# Curso Fundamentos do Ecossistema Cognitivo no Sankhya ERP (SEA-F01)

> Fonte: https://www.searchitect.pro/universidade/SEA-F01  
> Coletado em: 2026-10-14

---

## SEA-F01 — Fundamentos do Ecossistema Cognitivo no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 6 horas, com certificado SEA Practitioner — Fundamentos de Supervisão de IA. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP.

### Ementa

- Módulo 1 · O que o SEA é — e o que ele não é

- Anatomia do ecossistema: gateway, orquestrador e 26 especialistas

- Iniciativa independente: escopo, isenções e responsabilidade de quem aplica

- Onde o SEA acelera o trabalho e onde ele atrapalha

- Lendo uma resposta: diagnóstico, evidência e recomendação são coisas distintas

- Módulo 2 · O Agent Core como porta de entrada

- Descrever o problema antes de escolher o especialista

- Classificação de domínio, subdomínio e complexidade

- Roteamento para o expert principal e os complementares

- Interpretando a justificativa técnica do encaminhamento

- Módulo 3 · Engenharia de contexto e evidências

- O princípio “não invento, não assumo”: por que o expert cobra evidência

- O que anexar: XML, log de runtime, retorno de SQL, versão e parametrização

- LGPD na prática: anonimizar antes de colar dados de cliente no prompt

- A escala de nível de evidência e o que cada estrela significa

- Módulo 4 · Validando antes de aplicar

- Checklist de validação: da resposta ao ambiente de homologação

- Por que nenhuma resposta substitui teste em ambiente controlado

- Reconhecendo os limites que o expert declara sobre si mesmo

- Devolvendo o aprendizado para a Comunidade SEA

Primeira aula aberta

## Anatomia do ecossistema: gateway, orquestrador e 26 especialistas
Você não opera o SEA como uma coleção de respostas isoladas. Você entra por uma camada transversal que recebe a demanda, preserva contexto, identifica os domínios envolvidos, verifica evidências e decide até onde consegue responder horizontalmente antes de recomendar aprofundamento especializado. Essa camada é o SEA Generalist: inteligência horizontal especializada no ecossistema Sankhya.

### Gateway, orquestração e especialização
Pense em três responsabilidades diferentes.
O gateway representa a entrada: sua demanda precisa chegar com objetivo e contexto suficientes para ser compreendida.
O orquestrador classifica a demanda, identifica dependências e decide qual conhecimento precisa participar.
Os especialistas aprofundam domínios quando a análise horizontal deixa de ser suficiente.
O Generalist atua principalmente na segunda responsabilidade e na análise transversal. Uma pergunta como "o pedido foi confirmado, mas não gerou financeiro" não pertence automaticamente a um único domínio. O expert pode precisar correlacionar documento comercial, TOP, versão histórica da TOP, runtime e financeiro. O documento comercial possui uma identidade como NUNOTA; o financeiro possui sua própria identidade, NUFIN. A ausência de NUFIN é evidência muito mais forte de que o título não foi gerado do que simplesmente "não apareceu na tela".

### Onde o Generalist para
A IA cobre horizontalmente processos funcionais, estruturas de dados, APIs, integrações, SDK, banco, estoque, financeiro, fiscal, segurança, runtime, troubleshooting e arquitetura. A função dela é correlacionar essas camadas, encontrar a primeira divergência entre estado esperado e observado e orientar a validação.
Quando a demanda exige profundidade vertical — por exemplo, desenho tributário formal, investigação profunda de WMS ou revisão especializada de uma customização Java — o Generalist preserva o diagnóstico transversal e encaminha o aprofundamento. Não descarte o que veio antes: o encaminhamento deve levar contexto, evidências e hipóteses já construídas.

Caso

### o pedido que confirmou e não gerou financeiro
Chega o relato: "faturei a NUNOTA 84521 ontem às 14h32 e o financeiro não apareceu".
O Generalist não abre o financeiro primeiro. Ele estabelece a ordem dos fatos:

- o documento existe? NUNOTA 84521 localizada — fato

- qual operação governou? CODTIPOPER 1102, com DHTIPOPER de 12/03 — fato

- a configuração vigente naquela data mandava gerar financeiro? sim — fato

- existe NUFIN vinculado? nenhum — fato

- logo, a falha está entre a confirmação e a materialização do título — hipótese provável, ainda sem runtime

Quatro fatos verificáveis sustentam uma hipótese classificada, e o próximo passo fica óbvio: o log da transação daquele horário. Sem esse encadeamento, a conversa começaria em "será que é a TOP?" e terminaria numa alteração de parametrização sem evidência de que a TOP tivesse qualquer participação.

Leve para a prática
Descreva primeiro o resultado de negócio esperado e deixe o SEA identificar quais especialidades precisam participar.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
