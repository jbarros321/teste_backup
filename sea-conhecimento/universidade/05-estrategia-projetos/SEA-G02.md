# Curso Diagnóstico Multidomínio e RCA no Sankhya ERP (SEA-G02)

> Fonte: https://www.searchitect.pro/universidade/SEA-G02  
> Coletado em: 2026-10-14

---

## SEA-G02 — Diagnóstico Multidomínio e RCA no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 5 horas, com certificado SEA Practitioner — Diagnóstico Horizontal na trilha Orquestração e Governança. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP.

### Ementa

- Módulo 1 · Território do Agent Generalist

- Cobertura horizontal: 20 domínios correlacionados

- Quando o horizontal supera o especialista e vice-versa

- Correlação entre módulos como método de investigação

- Limites: profundidade menor que a do expert de domínio

- Módulo 2 · Montando o caso multidomínio

- Reunindo evidência de módulos diferentes no mesmo pedido

- Descrevendo a cadeia de eventos em ordem cronológica

- Identificando o módulo de origem versus os afetados

- Anexando logs, SQL e runtime de forma correlacionável

- Módulo 3 · Casos de uso do dia a dia

- Diagnóstico de problema que atravessa vários módulos

- Investigação de falha operacional sem origem óbvia

- Condução de RCA estruturada

- Recomendação do especialista certo para o passo seguinte

- Módulo 4 · Entregas, validação e governança

- Lendo o diagnóstico multidisciplinar e o plano de investigação

- Aplicando o checklist operacional e de governança

- Validando a correlação proposta com evidência

- Escalando para o expert de domínio na hora certa

Primeira aula aberta

## Cobertura horizontal: 20 domínios correlacionados
Você procura o Agent Generalist quando o problema não cabe com segurança em uma única caixa. A função dele é atuar como inteligência horizontal do ecossistema Sankhya, relacionando processo funcional, dados, integração, runtime, segurança e arquitetura antes de concluir onde está a primeira divergência.

### As camadas que formam a cobertura do Generalist
A arquitetura do expert é organizada em 20 camadas de conhecimento, numeradas de 00 a 19. Elas cobrem:

- contexto e governança do próprio Generalist

- arquitetura horizontal e mapa do ecossistema

- fontes, evidências e anti-alucinação

- APIs, Gateway e autenticação

- integrações e middleware

- sincronização, idempotência e recovery

- estruturas, entidades e tabelas

- TOPs e runtime operacional

- Comercial, Compras e ciclo documental

- Estoque, WMS, Produção e Inventário

- Financeiro e Tesouraria

- Fiscal, XML e documentos eletrônicos

- SDK, JAPE e extensibilidade

- banco de dados, SQL, BI e performance

- runtime, logs e observabilidade

- troubleshooting, RCA e War Room

- segurança, LGPD e governança de dados

- código real e casos multidomínio

- arquitetura enterprise

Essa organização permite ao Generalist atravessar fronteiras sem tratar cada módulo como sistema isolado.

### O que horizontal significa na prática
Considere uma nota confirmada que não gerou financeiro. O expert pode relacionar NUNOTA, documento em TGFCAB, itens em TGFITE, CODTIPOPER, versão histórica da TOP, possível NUFIN em TGFFIN, configuração, logs e runtime. TGFCAB e TGFITE são estruturas oficialmente documentadas para operações comerciais.
Se o mesmo documento veio por API, o expert amplia a análise para autenticação, request, response, timeout e estado final. Se existe XML fiscal, acrescenta autorização e protocolo. Se há customização, inclui SDK e runtime.
Você não precisa decidir antecipadamente qual desses territórios contém a causa. Esse é justamente o valor da cobertura horizontal.
O expert não considera sucesso de tela, HTTP ou configuração estática como prova suficiente do resultado de negócio. O log oficial do sistema é uma das evidências utilizadas para investigar comportamento fora do padrão.

Caso

### uma pergunta, cinco camadas
"O pedido foi confirmado e não gerou financeiro."
A cobertura horizontal permite relacionar, no mesmo raciocínio: NUNOTA, documento em TGFCAB, itens em TGFITE, CODTIPOPER, versão histórica da TOP, possível NUFIN em TGFFIN, configuração, logs e runtime.
Nenhuma dessas camadas responde sozinha. Juntas, elas localizam a primeira divergência.
É essa organização — 20 camadas de conhecimento correlacionadas — que permite atravessar fronteiras sem tratar cada módulo como sistema isolado.
E existe um limite que atravessa todas elas: sucesso de tela, HTTP ou configuração estática não é prova do resultado de negócio.
O log oficial do sistema é uma das evidências usadas para investigar comportamento fora do padrão — e é frequentemente a única que distingue "deveria ter acontecido" de "aconteceu".

Leve para a prática
Traga ao Generalist problemas que atravessam processos, dados ou runtimes e preserve os identificadores que permitem ligar um domínio ao outro.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
