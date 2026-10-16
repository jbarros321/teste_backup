# PROPOSTA DE ESCOPO
## Força de Vendas — Mobilidade Comercial
### Módulo de Permissões e Limites de Despesas
Documento comercial — visão de negócio, escopo, fases e entregáveis

| Campo | Conteúdo |
|---|---|
| Cliente | Satis |
| Solução | Força de Vendas — Mobilidade Comercial |
| **Módulo desta fase** | **Permissões e Limites de Despesas** |
| Responsável pela implantação | Neuon Soluções |
| **Escopo elaborado por** | **Jonatan Barros — Tech Lead** |
| Plataforma-base | Aplicação web/mobile própria integrada ao Sankhya (ERP) por API autenticada |
| **Esforço total estimado** | **42 horas**, em 5 fases de entrega |
| Versão do documento | 1.0 — outubro/2026 |

Documento elaborado a partir do escopo "Permissões de Despesas" (16/10/2026) e do levantamento integral do código da aplicação — telas de Simulação de Pedidos, Acompanhamento de Pedidos e RDV, funções de servidor, tabelas e integrações com o Sankhya.

---

## 1. Sumário executivo

A Satis opera sua força de vendas em um aplicativo próprio integrado ao Sankhya. O vendedor monta o pedido no celular, vê na hora o total, o desconto e a comissão, efetiva o pedido direto no ERP e acompanha a situação da nota. O mesmo aplicativo recebe as despesas de viagem, com leitura do comprovante por inteligência artificial e aprovação por gestor e financeiro.

Falta a esse conjunto a política de despesas da empresa. Hoje, quando não há regra cadastrada, a categoria nasce permitida e sem limite: o vendedor lança o tipo de despesa que quiser, no valor que quiser, e o controle acontece depois do gasto, na conferência do gestor.

A Força de Vendas é composta hoje por três rotinas em produção — Simulação de Pedidos, Acompanhamento de Pedidos e RDV. Esta proposta contrata **um módulo dentro dessa solução**: o controle de permissões e limites de despesas, que se encaixa na tela de RDV já em uso. Ele define: quais tipos de despesa cada vendedor pode lançar, quais os limites de valor por lançamento, por dia e por mês, e como o sistema valida, alerta ou bloqueia no momento do envio — garantindo aderência à política da empresa dentro da tela que a equipe já usa.

### Os cinco ganhos centrais

| Ganho | O que muda na prática |
|---|---|
| Política de despesas vira regra do sistema | O vendedor só vê e só lança o que a empresa autoriza, sem depender de conferência posterior |
| Limite conferido no envio | Valor acima do permitido por lançamento, por dia ou por mês é barrado ou alertado antes de entrar no fluxo |
| Saldo visível para quem gasta | O vendedor enxerga quanto ainda pode lançar no dia e no mês, por tipo de despesa |
| Exceção com caminho formal | Gasto fora da regra passa por solicitação e aprovação do gestor, em vez de acordo verbal |
| Consumo acompanhado | O gestor vê o consumo dos limites por vendedor e por tipo de despesa, sem montar planilha |

### Números do desafio atual

| Indicador | Situação hoje |
|---|---|
| Política de despesas parametrizada | Inexistente — sem regra cadastrada, a categoria fica permitida e sem limite |
| Categorias de despesa em uso | 10 — Combustível, Pedágio, Alimentação, Hospedagem, Estacionamento, Táxi, Passagem aérea, Material de apoio, Representação e Outros |
| Limite por vendedor | Não existe matriz por vendedor ou equipe; a exceção por usuário é cadastro avulso |
| Controle de acumulado | Apurado no envio como aviso, sem saldo visível ao vendedor antes do lançamento |
| Fluxo de exceção | Não existe — o caso fora da regra é resolvido na conversa entre vendedor e gestor |
| Rotinas da força de vendas em produção | 3 — Simulação de Pedidos, Acompanhamento de Pedidos e RDV |

---

## 2. O problema que o projeto resolve

| Situação identificada | Risco para o negócio |
|---|---|
| Sem regra cadastrada, a categoria fica permitida e sem limite | Despesa fora da política entra no fluxo e consome tempo de gestor para ser barrada |
| Não há matriz de permissão por vendedor ou equipe | Mesma política para perfis diferentes de viagem e de carteira |
| Limite por dia e por mês sem saldo visível | O vendedor descobre que estourou o limite só no momento do envio |
| Falta comprovante obrigatório por tipo de despesa, configurável pelo gestor | Despesa sem lastro documental chega à aprovação |
| Exceção sem caminho no sistema | Autorização informal, sem registro de quem liberou e por quê |
| Consumo dos limites sem visão consolidada | O gestor monta controle paralelo para saber quem está perto do teto |
| Alteração de permissão sem registro | Não se sabe quem mudou o limite de um vendedor nem quando |

Todos esses pontos têm a mesma raiz: a política de despesas existe na empresa, mas não dentro do sistema. A proposta inverte isso — a política passa a ser cadastro do gestor e trava aplicada no momento do lançamento.

---

## 3. Princípios que orientam a solução

| Princípio | Significado |
|---|---|
| A política da empresa é parâmetro, não código | Tipos de despesa, limites e exceções são cadastro do gestor; mudar a política não exige desenvolvimento |
| Validação preventiva | Permissão e limite são conferidos antes do envio, com resultado permitido, alerta ou bloqueado |
| Exceção é formal | Gasto fora da regra tem solicitação, decisão e registro — não acordo verbal |
| A exceção prevalece sobre a regra geral | O limite específico de um vendedor vale no lugar do limite da categoria, e sua remoção devolve o vendedor à regra geral |
| O servidor é a autoridade | Toda regra conferida na tela é conferida de novo no servidor, que não confia em dado vindo do navegador |
| Rastreabilidade | Alteração de permissão e decisão de exceção ficam gravadas com usuário e data/hora |
| A IA apoia, não decide | A leitura do comprovante preenche e aponta; a decisão permanece do gestor e do financeiro |

---

## 4. Escopo da solução

### 4.1 A solução e o módulo desta fase

A tabela abaixo situa o módulo contratado dentro da Força de Vendas. Os três primeiros blocos são a solução já em operação; os blocos 4 a 11 compõem o módulo de Permissões e Limites de Despesas, objeto desta proposta.

| # | Bloco | O que contempla | Situação |
|---|---|---|---|
| 1 | Simulação de pedidos | Cabeçalho, itens com desconto de antecipação, parcelas, resumo de comissão, efetivação e envio ao Sankhya, pedido mãe/filho | Já entregue |
| 2 | Acompanhamento de pedidos | Lista com filtros, situação no ERP, reenvio manual, assinaturas do cliente e do vendedor, PDF no layout do ERP e compartilhamento | Já entregue |
| 3 | RDV — lançamento e aprovação | Lançamento com comprovante, leitura por IA, pré-validação com bloqueios e avisos, fluxo gestor → financeiro, rateio e histórico | Já entregue |
| 4 | **Cadastro de tipos de despesa** | Nome, ícone, exigência de comprovante, ativação e inativação — a categoria não é excluída, apenas desativada | **Neste escopo** |
| 5 | **Matriz de permissões** | Vendedor ou equipe × tipo de despesa permitido, com aplicação de configuração em lote | **Neste escopo** |
| 6 | **Limites de valor** | Valor máximo por lançamento, por dia e por mês, com periodicidade por categoria e por vendedor | **Neste escopo** |
| 7 | **Regras adicionais** | Comprovante obrigatório, valor por quilômetro rodado e dias da semana permitidos | **Neste escopo** |
| 8 | **Validação no lançamento** | Exibir ao vendedor só o que lhe é permitido, validar o valor contra os limites, mostrar saldo do dia e do mês e exigir comprovante quando configurado | **Neste escopo** |
| 9 | **Fluxo de exceção** | Solicitação pelo vendedor quando o limite é ultrapassado, com aprovação ou recusa do gestor | **Neste escopo** |
| 10 | **Painel de consumo de limites** | Consumo por vendedor e por tipo de despesa | **Neste escopo** |
| 11 | **Histórico de alterações de permissão** | Quem alterou cada permissão e quando | **Neste escopo** |

Os blocos 1 a 3 constam para referência do que a solução entrega hoje e **não carregam horas** neste documento — já estão em produção.

### 4.2 Fora do escopo desta fase

- **Integração das despesas com o financeiro do ERP** (reembolso automático). O aval do financeiro permanece registrando a despesa no aplicativo.
- **Leitura automática do comprovante como regra de decisão.** A leitura por IA já existente permanece como apoio, sem poder de decisão.
- **Cálculo de rota e quilometragem por GPS.** O valor por quilômetro é parâmetro; a medição da rota permanece manual.
- **Cálculo de margem e lucro** no pedido de venda.
- **Alterações nos demais módulos** — Pedidos, Campanhas, Agenda, Ranking e Remuneração.
- **Os pontos mapeados no levantamento do código**, relacionados no Anexo A. Não estão incluídos nestas 42 horas e são estimados à parte quando contratados.

---

## 5. Módulo desta fase — blocos de entrega e horas

Composição do módulo de Permissões e Limites de Despesas dentro da Força de Vendas.

| # | Bloco de entrega | Entrega de negócio | Horas |
|---|---|---|---|
| 1 | Levantamento e desenho da solução | Política de despesas alinhada e protótipo aprovado antes da construção | 3,5 |
| 2 | Estrutura de dados e regras | Base que guarda tipos, permissões e limites, com consulta de saldo e validação | 7 |
| 3 | Tela de Permissões de Despesas | O gestor configura tipos, matriz e limites, aplica em lote e enxerga o histórico | 9,5 |
| 4 | Validação no lançamento | A tela do vendedor passa a respeitar permissão, limite e comprovante obrigatório | 6 |
| 5 | Acompanhamento e exceções | Fluxo de exceção e painel de consumo dos limites | 3,5 |
| 6 | Testes internos | Cenários de limite e de perfil conferidos antes do piloto | 2,5 |
| 7 | Teste em campo | Validação com grupo piloto em uso real | 5 |
| 8 | Refino | Ajustes apontados pelos vendedores e pelo gestor, revalidados | 3,5 |
| 9 | Implantação, treinamento e entrada em produção | Permissões configuradas para toda a equipe, equipe orientada e operação acompanhada na estabilização | 1,5 |
| | **Total** | | **42** |

---

## 6. Aproveitamento da plataforma e esforço de personalização

A coluna "solução atual" indica o que a Força de Vendas já resolve e é aproveitado pelo módulo; a última coluna define o que o módulo constrói.

| Necessidade | Solução atual | Esforço de personalização |
|---|---|---|
| Tela de lançamento de despesa (RDV) | Atende | Aproveitada integralmente; recebe a validação de permissão e limite |
| Fluxo de aprovação gestor → financeiro | Atende | Aproveitado; recebe a decisão de exceção |
| Leitura do comprovante por IA | Atende | Aproveitada sem alteração |
| Cadastro de tipos de despesa | Parcial | Ícone, exigência de comprovante e inativação |
| Permissão por vendedor ou equipe | Não atende | Novo: matriz de permissão com aplicação em lote |
| Limites por lançamento, dia e mês | Não atende | Novo: limite com periodicidade e consulta de saldo disponível |
| Regras adicionais (quilometragem, dias da semana) | Não atende | Novo |
| Fluxo de exceção de limite | Não atende | Novo: solicitação, aprovação e registro |
| Painel de consumo de limites | Não atende | Novo |
| Histórico de alteração de permissão | Não atende | Novo |

### Natureza do esforço

| Natureza | Horas | % | Onde se concentra |
|---|---|---|---|
| Desenho e validação de regra com o gestor | 3,5 | 8% | Política de despesas, limites e protótipo |
| Desenvolvimento novo | 20 | 48% | Estrutura de dados, tela de permissões e fluxo de exceção |
| Adaptação de tela existente | 6 | 14% | Validação na tela de lançamento do vendedor |
| Testes internos e piloto em campo | 7,5 | 18% | Cenários de limite, perfis e uso real |
| Refino, implantação e entrada em produção | 5 | 12% | Ajustes do piloto, liberação para a equipe e estabilização |
| **Total** | **42** | **100%** | |

---

## 7. Roadmap de implantação

O documento de origem organiza a entrega em cinco fases — Planejamento, Desenvolvimento, Teste em campo, Refino e Implantação. Esse modelo é mantido, porque já é o adotado com o cliente, e a entrada em produção é acompanhada dentro da fase de Implantação.

| Fase | Horas | % | Conteúdo | Entregável de negócio |
|---|---|---|---|---|
| 1 · Planejamento | 3,5 | 8% | Alinhamento da política de despesas, desenho das regras de limite e protótipo da tela | Regras de negócio documentadas e protótipo aprovado |
| 2 · Desenvolvimento | 28,5 | 68% | Estrutura de dados e regras, tela de permissões, validação no lançamento, exceções, painel de consumo e testes internos | Módulos entregues em homologação |
| 3 · Teste em campo | 5 | 12% | Piloto com grupo de vendedores, lançamentos reais no celular | Relatório do piloto com ajustes identificados |
| 4 · Refino | 3,5 | 8% | Ajustes de usabilidade apontados pelos vendedores e de regra apontados pelo gestor, com revalidação | Versão refinada aprovada pelo gestor |
| 5 · Implantação e entrada em produção | 1,5 | 4% | Configuração das permissões de toda a equipe, treinamento e acompanhamento da estabilização | Solução liberada para toda a equipe e operação estabilizada |
| **Total** | **42** | **100%** | | |

### Detalhamento por fase

**Fase 1 — Planejamento (3,5 h)**

| Atividade | Horas |
|---|---|
| Reunião de alinhamento da política de despesas (tipos, limites, exceções) | 2 |
| Desenho das regras de limite (por lançamento, por dia, por mês, por vendedor/equipe) | 1 |
| Protótipo da tela de Permissões de Despesas e validação com o gestor | 0,5 |

*Entregáveis: regras de negócio documentadas; protótipo aprovado.*

**Fase 2 — Desenvolvimento (28,5 h)**

*Estrutura de dados e regras — 7 h*

| Atividade | Horas |
|---|---|
| Cadastro de tipos de despesa | 2 |
| Tabela de permissões por vendedor/equipe com limites por lançamento, dia e mês | 2,5 |
| Regras adicionais: comprovante obrigatório, valor por km rodado, dias da semana permitidos | 1 |
| Funções de consulta de saldo disponível e validação do lançamento | 1,5 |

*Tela de Permissões de Despesas — 9,5 h*

| Atividade | Horas |
|---|---|
| Cadastro e manutenção dos tipos de despesa (ativar/inativar, ícone, exige comprovante) | 2,5 |
| Matriz de permissões: vendedor/equipe × tipo de despesa permitido | 3 |
| Configuração de limites: valor máximo por lançamento, por dia e por mês | 2,5 |
| Aplicar configuração em lote (copiar regra de um vendedor para outros ou para a equipe) | 1 |
| Histórico de alterações das permissões (quem alterou e quando) | 0,5 |

*Validação no lançamento de despesas — 6 h*

| Atividade | Horas |
|---|---|
| Exibir ao vendedor somente os tipos de despesa permitidos para ele | 2 |
| Validar o valor contra os limites (bloqueio ou alerta conforme configuração) | 1,5 |
| Mostrar saldo disponível do dia e do mês por tipo de despesa | 1 |
| Exigir comprovante quando o tipo de despesa estiver configurado assim | 1,5 |

*Acompanhamento e exceções — 3,5 h*

| Atividade | Horas |
|---|---|
| Solicitação de exceção pelo vendedor quando o limite for ultrapassado | 2 |
| Aprovação ou recusa da exceção pelo gestor | 1 |
| Painel de consumo dos limites por vendedor e por tipo de despesa | 0,5 |

*Testes internos — 2,5 h*

| Atividade | Horas |
|---|---|
| Roteiro de testes com cenários de limite (dentro, no limite, acima, sem permissão) | 1,5 |
| Testes de acesso por perfil (vendedor × gestor) e correções | 1 |

**Fase 3 — Teste em campo (5 h)**

| Atividade | Horas |
|---|---|
| Preparação do piloto: configuração das permissões dos vendedores participantes | 1,5 |
| Acompanhamento do uso em campo (lançamentos reais, celular, conexão) | 2,5 |
| Coleta de feedback dos vendedores e do gestor | 1 |

**Fase 4 — Refino (3,5 h)**

| Atividade | Horas |
|---|---|
| Ajustes de usabilidade e textos apontados pelos vendedores | 2 |
| Ajustes de regras e limites apontados pelo gestor | 1 |
| Revalidação dos cenários alterados | 0,5 |

**Fase 5 — Implantação e entrada em produção (1,5 h)**

| Atividade | Horas |
|---|---|
| Configuração das permissões para toda a equipe, treinamento e acompanhamento da estabilização | 1,5 |

Esforço total estimado: **42 horas**.

---

## 8. Integrações previstas

Esta fase não cria integração nova. As integrações já existentes na aplicação permanecem em operação e são relacionadas para contexto:

| Integração | Direção | Prioridade |
|---|---|---|
| Cadastros do ERP — vendedores, equipes e centros de resultado | Entrada | Alta |
| Leitura do comprovante por inteligência artificial | Entrada | Média |
| Armazenamento privado de comprovantes com link temporário | Entrada/Saída | Média |

---

## 9. Dados

Não há migração. Os tipos de despesa já existentes na aplicação são aproveitados, e a carga inicial se resume ao cadastro da política.

| Ponto de atenção | Tratamento previsto |
|---|---|
| Categorias já cadastradas na aplicação | Aproveitadas; recebem a configuração de permissão e limite na Fase 5 |
| Exceções por usuário já cadastradas | Revisadas junto do gestor durante o levantamento |
| Lista de itens não permitidos no comprovante | Mantida como está; a unificação do cadastro consta no Anexo A |

---

## 10. Compromissos de qualidade da solução

| Tema | Compromisso |
|---|---|
| Autoridade do servidor | Permissão, limite e acumulado são conferidos no servidor, sem confiar em dado vindo do navegador |
| Rastreabilidade | Alteração de permissão e decisão de exceção gravam usuário, data/hora e justificativa |
| Precisão do acumulado | O cálculo por dia, semana e mês desconsidera despesas reprovadas e é conferido no piloto |
| Coerência de permissão | A tela do vendedor só oferece o tipo de despesa que ele pode lançar |
| Restrição de acesso | A tela de Permissões de Despesas é restrita ao perfil gestor |
| Usabilidade em campo | Telas operáveis no celular, com saldo visível antes do lançamento e tolerância a conexão instável |
| LGPD | Dados pessoais do vendedor e comprovantes com controle de acesso por vínculo e trilha de consulta |

---

## 11. Premissas e responsabilidades

### Responsabilidades da Neuon Soluções

- Desenvolvimento dos blocos descritos no capítulo 5.
- Configuração das permissões de toda a equipe na implantação.
- Treinamento de vendedores e gestores.
- Acompanhamento da entrada em produção e correções do período de estabilização.

### Responsabilidades do cliente

- Fornecimento da política de despesas — tipos, limites e exceções — na etapa de levantamento.
- Definição dos perfis de acesso (vendedor e gestor) e de quem aprova a exceção.
- Disponibilização de um grupo piloto de vendedores para o teste em campo.
- Homologação dos entregáveis ao final de cada fase, no prazo acordado.
- Manutenção dos serviços fora do escopo, incluindo o processo de reembolso.

### Premissas gerais

- O esforço é expresso em **horas** e inclui o acompanhamento da entrada em produção e as correções do período de estabilização.
- Se houver necessidade de prazo em calendário, a conversão é de 160 h/mês por frente de trabalho — as 42 horas equivalem a aproximadamente 0,3 mês com uma frente.
- As fases são sequenciais; o Refino depende do resultado do Teste em campo.
- Ajustes que surgirem no teste em campo e mudem o escopo são estimados à parte, em horas, pelo mesmo critério deste documento.
- Os pontos do Anexo A não estão incluídos nestas 42 horas.

---

## 12. Próximos passos

| # | Passo | Responsável |
|---|---|---|
| 1 | Validação comercial deste escopo | Cliente |
| 2 | Entrega da política de despesas (tipos, limites e exceções) | Cliente |
| 3 | Fechamento da proposta comercial | Neuon + Cliente |
| 4 | Kick-off e Fase 1 — Planejamento | Neuon |
| 5 | Indicação do grupo piloto para o teste em campo | Cliente |

---

## Anexo A — Pontos mapeados no levantamento do código

O levantamento das três rotinas da Força de Vendas — Simulação de Pedidos, Acompanhamento de Pedidos e RDV — identificou os pontos abaixo. **Nenhum deles está incluído nas 42 horas desta proposta**; são relacionados para conhecimento do cliente e estimados à parte, em horas, caso venham a ser contratados.

### RDV

| # | Ponto | Efeito hoje |
|---|---|---|
| A1 | A despesa aprovada entra na fila de integração e é marcada como enviada ao Sankhya, mas nenhum processo consome a fila | O financeiro acredita que o lançamento chegou ao ERP |
| A2 | Não existe natureza nem configuração contábil para o RDV | Sem isso a despesa não tem como ser lançada no financeiro do ERP |
| A3 | O rateio entre centros de resultado é fixo em 50/50 e o parâmetro de configuração não é lido | A apuração por centro não reflete o acordo real |
| A4 | O vendedor comum só encontra a si mesmo na lista de vendedor relacionado | Na prática ele não consegue ratear |
| A5 | O gestor pode aprovar a própria despesa | Falha de segregação de função |
| A6 | O financeiro não é notificado quando o gestor aprova | A despesa fica parada sem que ninguém saiba |
| A7 | Gestor ligado apenas pela hierarquia do ERP decide, mas não recebe a notificação | Despesa sem acompanhamento |
| A8 | Despesa reprovada não pode ser corrigida e reenviada | O vendedor relança do zero e o histórico da correção se perde |
| A9 | Não existe aprovação em lote nem status de pagamento | Conferência e reembolso acompanhados fora do sistema |
| A10 | O hash do arquivo e a leitura da IA são produzidos no navegador e aceitos pelo servidor | A checagem de comprovante duplicado pode ser contornada |
| A11 | O perfil Financeiro tem o menu "Minhas despesas" sem permissão de lançar | A tela retorna erro de permissão |
| A12 | A lista de itens não permitidos usa separadores diferentes entre a tela do vendedor e a do gestor | Termo cadastrado pode não ser aplicado |

### Pedidos

| # | Ponto | Efeito hoje |
|---|---|---|
| A13 | O preço mínimo é calculado e não aplicado; margem não existe | Venda abaixo do mínimo entra no pedido |
| A14 | A atualização da situação no ERP, prevista a cada 5 minutos, não tem agendamento | Só o administrador atualiza; o vendedor vê informação defasada |
| A15 | As parcelas são sempre divididas em partes iguais, sem usar o percentual de cada parcela do ERP | Parcelamento divergente do cadastrado no Sankhya |
| A16 | A comissão usa o vendedor logado, não o dono do pedido | O valor muda conforme quem abre a tela |
| A17 | Pedido efetivado não pode ser cancelado nem reaberto e não tem histórico | Correção só dentro do ERP, sem rastro no aplicativo |
| A18 | O gestor vê os botões Continuar, Excluir e Enviar nos pedidos da equipe, e o sistema recusa a ação | Chamado de suporte evitável |
| A19 | O PDF traz campos fixos ou em branco e usa dados de empresa de parâmetro, não a empresa do pedido | Documento ao cliente com informação incorreta |
| A20 | A falha de envio ao ERP não grava status próprio, apenas texto | Pedido com erro não aparece em filtro |
| A21 | A fila de transferência e de brinde não tem processador | A solicitação é registrada e nunca chega ao Sankhya |
| A22 | A base de demonstração tem pedidos com número de ERP fictício, e sua remoção não apaga as parcelas | Dado de teste convivendo com dado real |

---

### Aprovação

_______________________________________
Responsável pela empresa

_______________________________________
Responsável pelo desenvolvimento

---

*Neuon Soluções — Escopo comercial · versão 1.0 · outubro/2026 · elaborado por Jonatan Barros, Tech Lead*
