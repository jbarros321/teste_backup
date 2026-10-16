# PROPOSTA DE ESCOPO
## CRM Comercial Satis
Documento comercial — visão de negócio, escopo, fases e entregáveis

| Campo | Conteúdo |
|---|---|
| Cliente | Satis |
| Responsável pela implantação | Neuon Soluções |
| **Escopo elaborado por** | **Jonatan Barros — Tech Lead** |
| Plataforma-base | Aplicação web responsiva integrada ao Sankhya (ERP) por API autenticada (OAuth / DbExplorer) |
| **Esforço total estimado** | **46 horas**, em 3 fases de entrega |
| Versão do documento | 1.0 — outubro/2026 |

Documento elaborado a partir do Escopo Técnico — CRM Satis (versão 1.0, 16/10/2026).

---

## 1. Sumário executivo

A Satis tem os números de venda no Sankhya e a rotina comercial fora dele. O faturamento, a meta e o estoque estão no ERP; o funil de oportunidades, a agenda de visitas, o acompanhamento de quem parou de comprar e a prospecção por região vivem na memória de cada vendedor e em controles paralelos.

A proposta entrega um CRM comercial que reúne as duas coisas em um só lugar: os dados de venda do ERP, atualizados automaticamente ao longo do dia, e a rotina comercial que hoje não tem sistema — funil, atividades, oportunidades sugeridas pelo próprio histórico de compra, mapa de prospecção e consulta aos dados em linguagem natural.

Cada usuário vê apenas o que lhe cabe: o vendedor a própria carteira, o gerente a equipe ligada a ele no ERP e a diretoria a empresa inteira. O alcance é calculado no servidor em toda consulta, não na tela.

### Os cinco ganhos centrais

| Ganho | O que muda na prática |
|---|---|
| Número de venda sem planilha | Faturamento, ticket médio, mix, devolução e meta chegam prontos do ERP, atualizados a cada 30 minutos |
| Rotina comercial dentro do sistema | Funil, oportunidades e agenda de visitas deixam de ser controle pessoal de cada vendedor |
| O sistema aponta a próxima venda | Recompra atrasada, venda cruzada, queda de compra, distância da meta e pedido parado viram oportunidade com um clique |
| Prospecção com endereço | Empresas ativas do agro a até 100 km de clientes atuais, que ainda não compram, aparecem no mapa com contato |
| Pergunta em português | A diretoria consulta vendas, metas, clientes, estoque e funil em linguagem natural, dentro do próprio alcance de dados |

### Números do desafio atual

| Indicador | Situação hoje |
|---|---|
| Fonte dos números de venda | Sankhya, consultado caso a caso; não há visão consolidada por vendedor, marca, estado e grupo |
| Funil de vendas | Não existe sistema — o acompanhamento é individual |
| Agenda comercial | Fora do ERP |
| Meta acompanhada | Meta 4 do ERP, conferida manualmente contra o realizado |
| Prospecção | Sem base estruturada de empresas do agro por proximidade da carteira |
| Visibilidade por papel | Sem separação sistêmica entre vendedor, gerente e diretoria |

---

## 2. O problema que o projeto resolve

| Situação identificada | Risco para o negócio |
|---|---|
| Número de venda consultado caso a caso no ERP | Decisão comercial tomada com informação defasada e conferência consumindo tempo de gestão |
| Funil e agenda fora de qualquer sistema | Oportunidade esquecida, visita não realizada e previsão de fechamento inexistente |
| Cliente que parou de comprar só é notado tarde | Perda de carteira sem que ninguém seja acionado a tempo |
| Meta conferida manualmente | Vendedor sem meta ou sem venda no período passa despercebido |
| Sem base de prospecção por região | O vendedor prospecta por conhecimento próprio, sem cobertura sistemática do entorno da carteira |
| Estoque e pedidos pendentes consultados separadamente | Pedido parado por falta de estoque descoberto depois da promessa ao cliente |
| Sem separação de alcance por papel | Informação comercial circula sem controle de quem vê o quê |

Todos esses pontos têm a mesma raiz: o ERP registra o que já aconteceu, e não há sistema para o que está acontecendo. O CRM proposto cobre essa lacuna sem duplicar o ERP — o dado de venda continua nascendo no Sankhya e é copiado para o CRM; a rotina comercial nasce no CRM.

---

## 3. Princípios que orientam a solução

| Princípio | Significado |
|---|---|
| O ERP é a fonte do dado de venda | Faturamento, meta, estoque e pedido pendente são copiados do Sankhya; o CRM não recalcula o que o ERP já apura |
| A rotina comercial nasce no CRM | Funil, oportunidade, atividade e nota são dados próprios, sem depender de campo livre no ERP |
| Alcance calculado no servidor | O que cada usuário vê é determinado pelo usuário logado em toda consulta e gravação; a tela nunca informa quem é o usuário |
| Carga idempotente | Rodar a sincronização duas vezes não duplica dado |
| A IA respeita o mesmo alcance | O assistente consulta somente as funções liberadas e responde dentro da carteira de quem pergunta |
| Nada de número sem lastro | Indicador sem base homologada não é exibido — é o caso de margem e lucro |

---

## 4. Escopo da solução

### 4.1 Blocos incluídos no escopo

| # | Bloco | O que contempla |
|---|---|---|
| 1 | Integração e sincronização com o ERP | Autenticação, consultas ao Sankhya, cargas em janelas mensais e trimestrais, agendas automática rápida e diária, fila acionada pela tela e registro de cada execução |
| 2 | Níveis de acesso e segurança | Papéis Diretor, Gerente e Vendedor, alcance por carteira, liberação no primeiro acesso e proteção dos parâmetros de consulta |
| 3 | Dashboard | Visão comercial do período: faturamento líquido, volume, ticket médio, clientes, mix e devoluções, com rankings e filtro cruzado |
| 4 | Análises | Seis indicadores com variação e minigráfico, comparação com ano e período anteriores, atingimento de meta, evolução mensal, rankings e detalhamento por qualquer dimensão |
| 5 | Metas | Meta 4 do ERP em valor e quantidade, por vendedor, cliente e marca, com realizado, atingimento e destaque de quem está sem meta ou sem venda |
| 6 | Funil, oportunidades e agenda | Kanban com arrastar e soltar, valor ponderado pela probabilidade, alerta de oportunidade parada, histórico de etapas e agenda de visitas, ligações e tarefas |
| 7 | Oportunidades automáticas | Recompra, venda cruzada, queda de compra, distância da meta e pedido parado, cada sugestão virando oportunidade com um clique |
| 8 | Clientes | Lista com situação de compra (ativo, em risco, sem compra) e ficha completa do cliente |
| 9 | Estoque e pedidos | Cobertura em dias por período escolhido, faixas de cobertura clicáveis e pedidos a faturar com estoque suficiente ou insuficiente |
| 10 | Mapa por região e prospecção | Clientes posicionados por cidade e coloridos pela situação de compra, oportunidades por região e empresas ativas do agro a até 100 km que ainda não são clientes |
| 11 | Converse com os dados | Assistente de IA em linguagem natural sobre vendas, metas, clientes, estoque e funil, com histórico de conversas |
| 12 | Administração e aparência | Papel de cada usuário, vínculo com o vendedor do ERP, situação das sincronizações, tema claro/escuro e paleta por usuário |

### 4.2 Fora do escopo desta fase

- **Margem, lucro e comissão.** O custo gravado no ERP ainda não teve a base de rateio homologada pelo cliente; exibir margem sobre base não homologada produziria decisão errada.
- **Filtro por safra.** O ERP não traz campo de safra. Foi substituído por período com início e fim; atalhos de safra dependem da definição dos meses pelo cliente.
- **Produtor rural pessoa física como prospect.** A base aberta da Receita Federal traz apenas pessoa jurídica.
- **Atualização automática da base de prospects.** A Receita publica mensalmente; a carga é executada sob demanda.
- **Coordenada exata do cliente.** O posicionamento no mapa é pelo centro do município, porque o ERP não tem coordenada nem CEP preenchidos.
- **Integrações com sistemas além do Sankhya.**
- **Comércio de máquinas agrícolas** entre as atividades econômicas consideradas na prospecção.

---

## 5. Módulos da solução

| Módulo | Entrega de negócio | Horas |
|---|---|---|
| M01 · Levantamento e arquitetura | Regras de negócio alinhadas e modelo de dados definido antes da construção | 2 |
| M02 · Integração com o Sankhya | Os números do ERP passam a chegar ao CRM por conexão autenticada | 4 |
| M03 · Modelo de dados e cargas | Carga de vendas, metas, cadastros e estoque com regravação que não duplica dado | 5 |
| M04 · Sincronização automática | O dado se mantém atualizado sozinho, com atualização sob demanda pela tela | 3,5 |
| M05 · Níveis de acesso e segurança | Cada papel vê exatamente a sua carteira, por decisão do servidor | 3,5 |
| M06 · Dashboard e Análises | A gestão comercial passa a ter a visão do período em uma tela, com detalhamento | 5 |
| M07 · Metas | Meta e realizado lado a lado, com atingimento e ausências em destaque | 2,5 |
| M08 · Funil, oportunidades e agenda | A rotina comercial entra no sistema, com previsão de fechamento e histórico | 3,5 |
| M09 · Oportunidades automáticas | O próprio histórico de compra aponta a próxima venda | 2,5 |
| M10 · Clientes e ficha do cliente | O vendedor chega à visita sabendo a situação completa do cliente | 2,5 |
| M11 · Estoque e pedidos | Cobertura e pedido parado visíveis antes da promessa ao cliente | 2,5 |
| M12 · Mapa por região e prospects | A prospecção passa a cobrir o entorno da carteira de forma sistemática | 3,5 |
| M13 · Assistente de IA | A consulta aos dados deixa de exigir conhecimento de relatório | 2,5 |
| M14 · Identidade visual e navegação | Uso confortável no computador, tablet e celular, em tema claro ou escuro | 1 |
| M15 · Testes, homologação e entrada em produção | Os números do CRM conferem com o ERP, validados por papel, e a operação entra acompanhada | 2,5 |
| **Total** | | **46** |

---

## 6. Aproveitamento da plataforma e esforço de personalização

| Necessidade | Plataforma padrão | Esforço de personalização |
|---|---|---|
| Dados de venda, meta, cliente, produto e estoque | Atende | Cópia para o CRM em janelas de carga, por limite de linhas do serviço de consultas do ERP |
| Autenticação de usuário | Atende | Vínculo do e-mail com o cadastro de vendedor e liberação no primeiro acesso |
| Alcance por carteira e hierarquia | Parcial | Cálculo do alcance no servidor a partir do vendedor e da hierarquia do ERP |
| Agendamento de rotinas | Atende | Agendas rápida e diária, mais fila acionada pela tela |
| Indicadores e rankings | Não atende | Novo: dashboard, análises com filtro cruzado e detalhamento por dimensão |
| Funil, oportunidades e agenda | Não atende | Novo: estrutura própria do CRM com histórico de etapas |
| Oportunidades automáticas | Não atende | Novo: cinco regras calculadas sobre o histórico de compra e a meta |
| Mapa e prospecção | Não atende | Novo: municípios do IBGE, proximidade de até 100 km e base aberta de CNPJ do agro |
| Assistente em linguagem natural | Parcial | Perfil de acesso próprio do assistente e instruções de negócio |
| Aparência e responsividade | Atende | Temas, paletas e layout |

### Distribuição do esforço

| Natureza | Horas | % | Onde se concentra |
|---|---|---|---|
| Levantamento e arquitetura | 2 | 4% | Regras de negócio e modelo de dados |
| Aproveitamento do padrão | 12,5 | 27% | Integração com o ERP, cargas e agendamento |
| Adaptação de recursos existentes | 9,5 | 21% | Alcance por carteira, assistente de IA, aparência e homologação |
| Desenvolvimento novo | 22 | 48% | Dashboard e análises, funil, oportunidades automáticas, mapa e prospecção |
| **Total** | **46** | **100%** | |

---

## 7. Roadmap de implantação

O documento técnico de origem organiza a entrega em três fases. Esse modelo é mantido — já é o adotado com o cliente — e a entrada em produção é acompanhada dentro da fase de entrega.

| Fase | Horas | % | Conteúdo | Entregável de negócio |
|---|---|---|---|---|
| 1 · Fundação de dados e segurança | 18 | 39% | Levantamento e arquitetura, integração com o Sankhya, modelo de dados e cargas, sincronização automática, níveis de acesso | Os números do ERP chegam ao CRM atualizados e cada papel vê só a sua carteira |
| 2 · Telas e funcionalidades | 22 | 48% | Dashboard e análises, metas, funil e agenda, oportunidades automáticas, clientes, estoque e pedidos, mapa e prospecção | A equipe comercial opera a rotina no sistema, com indicadores e funil |
| 3 · Assistente de IA, identidade visual e entrega | 6 | 13% | Assistente "Converse com os dados", temas e navegação, testes, homologação e entrada em produção acompanhada | Consulta em linguagem natural, números conferidos com o ERP e operação estabilizada |
| **Total** | **46** | **100%** | | |

### Detalhamento por fase

**Fase 1 — Fundação de dados e segurança (18 h)**

| Item | Atividade | Descrição | Horas |
|---|---|---|---|
| 1.1 | Levantamento e arquitetura | Alinhamento das regras de negócio, modelo de dados misto e desenho da solução | 2 |
| 1.2 | Integração com o Sankhya | Autenticação, consultas ao serviço do ERP e mapeamento das visões de venda, meta e preço por marca | 4 |
| 1.3 | Modelo de dados e cargas | Estruturas do ERP e do CRM, cargas em janelas mensais e trimestrais, regravação que não duplica dado | 5 |
| 1.4 | Sincronização automática | Motor de sincronização, agendas rápida e diária, fila acionada pela tela e registro das execuções | 3,5 |
| 1.5 | Níveis de acesso e segurança | Papéis, alcance por carteira, liberação no primeiro acesso e proteção dos parâmetros de consulta | 3,5 |

**Fase 2 — Telas e funcionalidades (22 h)**

| Item | Atividade | Descrição | Horas |
|---|---|---|---|
| 2.1 | Dashboard e Análises | Indicadores, filtros cruzados, detalhamento, evolução mensal, rankings e painel de sugestões | 5 |
| 2.2 | Metas | Meta 4 em valor e quantidade, metas × realizado e atingimento | 2,5 |
| 2.3 | Funil, oportunidades e agenda | Kanban, cadastro de oportunidade, histórico de etapas e agenda de atividades | 3,5 |
| 2.4 | Oportunidades automáticas | Recompra, venda cruzada, queda de compra, distância da meta e pedido parado | 2,5 |
| 2.5 | Clientes e ficha do cliente | Lista com situação de compra e ficha completa | 2,5 |
| 2.6 | Estoque e pedidos | Cobertura por período, faixas de cobertura, pedidos a faturar e filtros múltiplos | 2,5 |
| 2.7 | Mapa por região e prospects | Mapa do Brasil, clientes sem compra, oportunidades por região e carga das empresas do agro a até 100 km | 3,5 |

**Fase 3 — Assistente de IA, identidade visual e entrega (6 h)**

| Item | Atividade | Descrição | Horas |
|---|---|---|---|
| 3.1 | Assistente de IA | Chat "Converse com os dados", perfil de acesso do assistente e instruções de negócio | 2,5 |
| 3.2 | Identidade visual e navegação | Temas claro e escuro, paletas, menu lateral e layout responsivo | 1 |
| 3.3 | Testes, homologação e entrada em produção | Conferência dos números com o ERP, testes por papel de usuário, liberação dos acessos e acompanhamento da estabilização | 2,5 |

Esforço total estimado: **46 horas**.

---

## 8. Integrações previstas

| Integração | Direção | Prioridade |
|---|---|---|
| Vendas e devoluções por item | Entrada | Alta |
| Metas do ERP, com preço de referência por marca | Entrada | Alta |
| Pedidos pendentes de faturamento | Entrada | Alta |
| Cadastros — clientes, vendedores, produtos e estoque | Entrada | Alta |
| Autenticação do usuário (e-mail e senha, Google ou Microsoft) | Entrada | Alta |
| Municípios e coordenadas do IBGE | Entrada | Média |
| Base aberta de CNPJ da Receita Federal (empresas ativas do agro) | Entrada | Média |
| Assistente de IA sobre os dados do CRM | Entrada/Saída | Média |

Todas as integrações de venda usam a conexão autenticada do próprio Sankhya. As bases do IBGE e da Receita Federal são públicas e carregadas sob demanda — não há dependência de API de terceiro com risco de cronograma.

---

## 9. Migração e carga de dados

Não há digitação de histórico. A carga inicial é executada pelo próprio motor de sincronização, nas janelas definidas.

| Ponto de atenção | Tratamento previsto |
|---|---|
| Limite de 5.000 linhas por consulta ao ERP | Vendas carregadas mês a mês e metas por trimestre; a carga recusa a janela se o limite for atingido, em vez de trazer dado incompleto |
| Visão de vendas do ERP sem chave estável | A janela de datas é apagada e regravada a cada carga, o que torna a execução repetível sem duplicar dado |
| Limite de execuções por minuto da plataforma | As telas agrupam consultas, a busca aguarda a digitação terminar e não há atualização automática de tela |
| Clientes sem coordenada e sem CEP no ERP | Posicionamento pelo centro do município, com vínculo entre a cidade do ERP e o município do IBGE |
| Base da Receita Federal publicada mensalmente | Carga sob demanda, com registro da data da base utilizada |
| Custo sem base de rateio homologada | Margem e lucro não são exibidos até a homologação |

Esses pontos foram identificados na análise prévia e já estão endereçados no plano — não são riscos abertos.

---

## 10. Compromissos de qualidade da solução

| Tema | Compromisso |
|---|---|
| Alcance de dados | Calculado no servidor a partir do usuário logado, em toda consulta e gravação; a tela nunca informa quem é o usuário |
| Senha | A aplicação não guarda senha de usuário — a autenticação é da plataforma |
| Proteção da consulta | Valores digitados nas telas são enviados ao banco como parâmetros, sem montar comando a partir do texto do usuário |
| Credenciais do ERP | Guardadas na integração, fora do código e das telas |
| Carga repetível | Reexecutar uma sincronização não duplica dado, e cada execução registra início, fim, volume e erro |
| Fidelidade ao ERP | Os números do CRM são conferidos contra o ERP na homologação, por papel de usuário |
| Transparência de regra | Faturamento líquido, tratamento de devolução e bonificação e critério de meta declarados no documento e na tela |
| Desempenho | Listagens paginadas, consultas agrupadas por tela e busca com espera após a digitação |
| Alcance do assistente de IA | O assistente responde apenas dentro da carteira de quem pergunta, pelas funções liberadas no perfil dele |
| Usabilidade | Interface em português, responsiva, com tema claro e escuro |

---

## 11. Premissas e responsabilidades

### Responsabilidades da Neuon Soluções

- Construção da integração com o Sankhya e do motor de sincronização.
- Desenvolvimento dos módulos descritos no capítulo 5.
- Configuração dos papéis e do alcance por carteira.
- Carga das bases do IBGE e da Receita Federal para a prospecção.
- Conferência dos números com o ERP na homologação e acompanhamento da entrada em produção.

### Responsabilidades do cliente

- Liberação dos acessos ao Sankhya e ao serviço de consultas do ERP.
- Validação das regras de faturamento líquido, devolução, bonificação e critério de meta.
- Definição dos meses de safra, caso os atalhos de safra sejam desejados em fase futura.
- Homologação da base de rateio de custo, caso margem e lucro entrem em escopo futuro.
- Indicação dos usuários e dos papéis, e liberação dos acessos que não coincidem com o cadastro de vendedor.
- Homologação dos entregáveis ao final de cada fase, no prazo acordado.

### Premissas gerais

- O esforço é expresso em **horas** e inclui a liberação dos acessos, o acompanhamento da entrada em produção e as correções do período de estabilização.
- Se houver necessidade de prazo em calendário, a conversão é de 160 h/mês por frente de trabalho — as 46 horas equivalem a aproximadamente 0,3 mês com uma frente.
- O ERP permanece a fonte do dado de venda; o CRM não recalcula apuração do ERP.
- As fases são sequenciais; a Fase 2 depende da fundação de dados da Fase 1.
- Alteração de escopo dentro de uma fase é estimada à parte, em horas, pelo mesmo critério deste documento.
- Margem, lucro e comissão permanecem fora do escopo enquanto a base de rateio de custo não for homologada.

---

## 12. Próximos passos

| # | Passo | Responsável |
|---|---|---|
| 1 | Validação comercial deste escopo | Cliente |
| 2 | Liberação dos acessos ao Sankhya e validação das regras de faturamento e meta | Cliente |
| 3 | Fechamento da proposta comercial | Neuon + Cliente |
| 4 | Kick-off e Fase 1 — Fundação de dados e segurança | Neuon |
| 5 | Indicação dos usuários e dos papéis de acesso | Cliente |

---

### Aprovação

_______________________________________
Responsável pela empresa

_______________________________________
Responsável pelo desenvolvimento

---

*Neuon Soluções — Escopo comercial · versão 1.0 · outubro/2026 · elaborado por Jonatan Barros, Tech Lead*
