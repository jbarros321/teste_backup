PROPOSTA DE ESCOPO

# BPO Financeiro XPTO

Documento comercial — visão de negócio, escopo, fases e entregáveis

| Campo | Conteúdo |
|---|---|
| Cliente | XPTO Serviços Administrativos Ltda. |
| Responsável pela implantação | Neuon Soluções |
| **Escopo elaborado por** | **Jonatan Barros — Tech Lead** |
| Plataforma-base | Sankhya (ERP) + módulos de personalização |
| **Esforço total estimado** | **60 horas**, em 1 onda de entrega |
| Versão do documento | 1.0 — outubro/2026 |

Documento elaborado a partir da reunião de levantamento com a área administrativa da XPTO, da análise das 4 planilhas de controle hoje em uso (11 abas) e do inventário das 3 carteiras atendidas em outubro de 2026.

## 1. Sumário executivo

A XPTO presta serviços administrativos e está estruturando uma frente de BPO financeiro: assumir, para empresas clientes, a rotina de contas a pagar — recebimento de documentos, conferência, aprovação, programação de pagamento e prestação de contas. Hoje três carteiras já são atendidas nesse modelo, operadas em planilhas separadas por cliente.

O modelo em planilha chegou ao limite no ponto em que a XPTO deixou de operar o próprio dinheiro e passou a operar o dinheiro de terceiros. Cada carteira tem sua planilha, sua nomenclatura e seu histórico, e a segregação entre uma carteira e outra depende de quem abre o arquivo. Não existe registro de quem aprovou um pagamento, o mesmo documento de fornecedor pode ser lançado duas vezes sem que nada impeça, e a prestação de contas é remontada manualmente a cada mês, em formato diferente para cada cliente atendido. O risco deixou de ser retrabalho e passou a ser responsabilidade sobre recurso de terceiro.

Esta fase implanta o núcleo da operação de BPO sobre a plataforma Sankhya: a carteira do cliente atendido passa a ser uma entidade do sistema, com acesso segregado, o título de fornecedor entra uma única vez e com duplicidade bloqueada na origem, o pagamento acima de faixa definida não é liberado sem aprovação registrada, e a posição de cada carteira é extraída em formato único. O escopo cobre uma carteira-piloto em produção assistida; as demais entram por cadastro, sem novo desenvolvimento.

**Os cinco ganhos centrais**

| Ganho | O que muda na prática |
|---|---|
| Carteira como entidade, não como arquivo | A separação entre o dinheiro de um cliente e de outro é imposta pelo sistema, não pela disciplina de quem abre a planilha |
| Duplicidade vira bloqueio | O segundo lançamento do mesmo documento do mesmo fornecedor é recusado no ato, em vez de ser descoberto na conferência do mês |
| Aprovação com autor e data | Todo pagamento acima da faixa tem quem aprovou registrado; a XPTO responde ao cliente atendido com evidência, não com memória |
| Operador vê só o que opera | O acesso acompanha a carteira atribuída, e a saída de um colaborador não exige refazer controle de arquivo |
| Prestação de contas padronizada | A posição da carteira sai no mesmo formato para todos os clientes atendidos, em vez de ser remontada mês a mês |

**Números do desafio atual**

| Indicador | Situação hoje |
|---|---|
| Carteiras atendidas | 3, em 4 planilhas distintas (11 abas) |
| Títulos processados por mês | aproximadamente 420, somadas as três carteiras |
| Lançamentos em duplicidade identificados no último trimestre | 7, todos encontrados na conferência posterior ao pagamento |
| Registro de aprovação de pagamento | inexistente; a autorização circula por mensagem e e-mail |
| Tempo de montagem da prestação de contas | 2 dias úteis por carteira, ao fim de cada mês |
| Formatos de prestação de contas em uso | 3 — um por cliente atendido |

## 2. O problema que o projeto resolve

| Situação identificada | Risco para o negócio |
|---|---|
| Cada carteira atendida é uma planilha própria, com nomenclatura própria | Erro de atribuição entre carteiras; pagamento feito com o recurso do cliente errado |
| Nada impede o mesmo documento de fornecedor de ser lançado duas vezes | Pagamento em duplicidade com recurso de terceiro, descoberto só na conferência posterior |
| A autorização de pagamento circula por mensagem e e-mail | A XPTO não consegue demonstrar ao cliente atendido quem autorizou cada saída |
| O acesso ao controle é o acesso ao arquivo | Qualquer operador alcança qualquer carteira; a segregação depende de confiança, não de regra |
| A prestação de contas é remontada manualmente por carteira | Dois dias úteis por carteira por mês, e o formato muda conforme quem monta |
| O histórico vive na planilha e no e-mail, em paralelo | Na contestação de um lançamento, a reconstituição do que aconteceu depende de busca manual |

A raiz é comum: a planilha registra o resultado da operação, mas não impõe a regra. Ela aceita o lançamento duplicado, aceita o pagamento sem aprovação e aceita ser aberta por quem não deveria — e por isso a confiabilidade da operação de BPO depende hoje da atenção individual de cada operador, não do processo.

## 3. Princípios que orientam a solução

| Princípio | Significado |
|---|---|
| O recurso de terceiro não se mistura | Toda movimentação pertence a uma carteira identificada, e nenhum lançamento existe sem essa identificação |
| A regra impede, não avisa | Duplicidade e pagamento fora de alçada são recusados no momento do lançamento, não apontados depois |
| Quem autorizou fica registrado | Autorização sem autor e data registrados não é autorização |
| Acesso acompanha a atribuição | O operador alcança a carteira que opera, e nada além dela |
| Nova carteira entra por cadastro | Assumir um novo cliente atendido é operação administrativa, não projeto de sistema |

## 4. Escopo da solução

### 4.1 Blocos incluídos

| # | Bloco | O que contempla |
|---|---|---|
| 1 | Carteira de clientes atendidos | Cadastro da carteira, identificação obrigatória da carteira em toda movimentação financeira, isolamento da informação por carteira |
| 2 | Contas a pagar da operação | Rotina de entrada do título de fornecedor, conferência, programação de pagamento e baixa, com bloqueio de duplicidade por fornecedor, documento e carteira |
| 3 | Alçada de aprovação | Faixas de valor por carteira, aprovação registrada com autor e data, impedimento de liberação acima da faixa sem aprovação |
| 4 | Acesso segregado | Perfis de operador de carteira e de supervisão da operação, com a matriz de permissão correspondente |
| 5 | Prestação de contas | Extração da posição da carteira — títulos em aberto, programados e pagos no período — em formato único para todos os clientes atendidos |

### 4.2 Fora do escopo desta fase

- **Conciliação bancária automática** — a baixa do pagamento é registrada pelo operador; o sistema não recebe nem interpreta retorno bancário nesta fase.
- **Remessa eletrônica de pagamentos (CNAB ou PIX em lote)** — a execução do pagamento permanece no canal bancário que a XPTO já usa; o sistema programa e registra, não transmite.
- **Contas a receber e faturamento das carteiras atendidas** — esta fase cobre a saída, não a entrada de recursos do cliente atendido.
- **Portal de acesso do cliente atendido** — a prestação de contas é entregue pela XPTO ao cliente; o cliente atendido não acessa o sistema nesta fase.
- **Apuração e entrega de obrigações fiscais e contábeis das carteiras** — o sistema registra o pagamento do tributo como título, não calcula nem transmite a obrigação.
- **Folha de pagamento das empresas atendidas** — o sistema consome o resultado da folha como título a pagar, não o calcula.
- **Captura automática de documento de fornecedor por leitura de arquivo eletrônico** — a entrada do título é feita pelo operador nesta fase.
- **Painel de inteligência de caixa e projeção** — a entrega desta fase é a posição do período, não a projeção.

Cada item acima é escopo de fase posterior, estimado em horas no momento em que for contratado.

## 5. Módulos da solução

| Módulo | Entrega de negócio | Horas estimadas |
|---|---|---|
| M01 · Carteira de clientes atendidos | Cada empresa atendida é uma carteira isolada no sistema, criada por cadastro | 12,5 |
| M02 · Contas a pagar da operação | O título entra uma única vez; a duplicidade é bloqueio, não conferência posterior | 19,5 |
| M03 · Alçada de aprovação | Pagamento acima da faixa não é liberado sem aprovação registrada com autor e data | 14,0 |
| M04 · Acesso segregado | O operador alcança apenas a carteira que opera | 7,0 |
| M05 · Prestação de contas | A posição da carteira é extraída em formato único, em minutos | 7,0 |
| **Total** | | **60,0** |

M01 e M04 são transversais: a identificação da carteira e a segregação de acesso sustentam todos os demais módulos e por isso são implantados primeiro.

## 6. Aproveitamento da plataforma e esforço de personalização

| Necessidade | Plataforma padrão | Esforço de personalização |
|---|---|---|
| Cadastro de fornecedores e rotina de contas a pagar | Atende | Configuração da rotina para o modelo de BPO |
| Programação e baixa de pagamento | Atende | Configuração, sem desenvolvimento |
| Perfis de acesso e matriz de permissão | Atende | Configuração dos dois perfis da operação |
| Identificação da carteira do cliente atendido em toda movimentação | Parcial | Extensão do cadastro financeiro e obrigatoriedade da informação |
| Isolamento da informação entre carteiras | Parcial | Regra de restrição por carteira atribuída ao operador |
| Bloqueio de duplicidade por fornecedor, documento e carteira | Insuficiente | Regra de validação própria na entrada do título |
| Aprovação por faixa de valor com autor e data registrados | Parcial | Fluxo de alçada configurado por carteira |
| Prestação de contas em formato único por carteira | Parcial | Extração padronizada sobre a informação da rotina |

| Natureza | Horas | % | Onde se concentra |
|---|---|---|---|
| Aproveitamento do padrão | 26,0 | 43,3% | Contas a pagar, cadastro de fornecedores, programação de pagamento, perfis de acesso |
| Adaptação de recursos existentes | 20,5 | 34,2% | Identificação da carteira, alçada de aprovação, extração da prestação de contas |
| Desenvolvimento novo | 13,5 | 22,5% | Bloqueio de duplicidade e isolamento da informação por carteira |
| **Total** | **60,0** | **100%** | |

Dois terços do esforço se apoiam no que a plataforma já faz. O desenvolvimento novo se concentra exatamente nos dois pontos que a planilha não consegue impor: impedir o lançamento duplicado e isolar o recurso de cada cliente atendido.

## 7. Roadmap de implantação

| Onda | Horas | Conteúdo | Entregável de negócio |
|---|---|---|---|
| 1 · Operação de BPO em produção | 60,0 | M01 · M02 · M03 · M04 · M05, sobre uma carteira-piloto | A equipe para de operar o dinheiro do cliente em planilha |
| **Total** | **60,0** | | |

A implantação é uma onda única por decisão de escopo: os cinco módulos formam o menor conjunto que coloca uma carteira em produção com segurança. Dividir a entrega deixaria a carteira-piloto operando parte em sistema e parte em planilha — o pior dos dois modelos.

**Distribuição por fase**

| Onda | F1 | F2 | F3 | F4 | F5 | F6 | F7 | Total |
|---|---|---|---|---|---|---|---|---|
| 1 · Operação de BPO em produção | 6,0 | 12,0 | 23,0 | — | 3,0 | 8,0 | 8,0 | 60,0 |
| **Total** | **6,0** | **12,0** | **23,0** | **—** | **3,0** | **8,0** | **8,0** | **60,0** |

**Consolidado por fase**

| Fase | Horas | % do projeto | Entregável da fase |
|---|---|---|---|
| F1 · Detalhamento funcional | 6,0 | 10,0% | Regras de duplicidade, faixas de alçada e formato da prestação de contas aprovados |
| F2 · Parametrização | 12,0 | 20,0% | Ambiente configurado: carteira-piloto, fornecedores, rotina de contas a pagar e perfis |
| F3 · Desenvolvimento | 23,0 | 38,3% | Bloqueio de duplicidade, isolamento por carteira, alçada e extração entregues em homologação |
| F4 · Integrações | — | 0,0% | Não se aplica nesta onda: conciliação bancária e remessa eletrônica estão fora do escopo desta fase |
| F5 · Migração de dados | 3,0 | 5,0% | Fornecedores e títulos em aberto da carteira-piloto conferidos pela XPTO |
| F6 · Testes e homologação | 8,0 | 13,3% | Termo de homologação da onda assinado |
| F7 · Treinamento e go-live | 8,0 | 13,4% | Operador e supervisor treinados e carteira-piloto em produção assistida |
| **Total** | **60,0** | **100,0%** | |

A parametrização pesa mais que o usual porque a fase é de fundação: boa parte da operação é configuração do que a plataforma já faz, e não desenvolvimento. As horas de integração foram realocadas em desenvolvimento, onde está o esforço real desta onda. O esforço total estimado é de **60 horas**, e a captura de valor começa na entrada em produção da carteira-piloto, ao fim da onda.

## 8. Integrações previstas

| Integração | Direção | Prioridade |
|---|---|---|
| Carga inicial de fornecedores e títulos em aberto da carteira-piloto | Entrada | Alta |
| Extração da prestação de contas da carteira em arquivo | Saída | Alta |

As duas integrações desta fase são feitas por arquivo, em lote, sem dependência de serviço de terceiro: a carga inicial é executada uma vez, e a extração da prestação de contas é gerada pelo próprio sistema. Nenhuma data desta onda depende de disponibilização de interface por banco ou por fornecedor de software, o que remove essa classe de risco do cronograma.

## 9. Migração de dados

A migração desta fase cobre **dois blocos de carga**, ambos restritos à carteira-piloto:

1. Cadastro de fornecedores em uso na carteira-piloto.
2. Títulos em aberto na data de corte, com o saldo a pagar conferido contra a planilha de origem.

| Ponto de atenção | Tratamento previsto |
|---|---|
| O mesmo fornecedor aparece grafado de formas diferentes entre planilhas | Unificação por inscrição do fornecedor antes da carga, com a lista de unificações submetida à XPTO |
| Fornecedor sem inscrição informada na planilha de origem | Carga em lista de pendência, para completar antes da entrada em produção |
| Títulos já pagos presentes na planilha de origem | Excluídos da carga; a migração leva apenas o que está em aberto na data de corte |
| Divergência entre o saldo da planilha e o saldo migrado | Conciliação do saldo de abertura com a XPTO, com aprovação formal antes do go-live |
| Lançamentos em duplicidade existentes na planilha de origem | Identificados e apresentados à XPTO para decisão antes da carga; a regra de bloqueio impede a reentrada |

São pontos já endereçados no escopo e no esforço desta onda, não riscos em aberto.

## 10. Compromissos de qualidade da solução

| Tema | Compromisso |
|---|---|
| Auditoria | Entrada, alteração, aprovação e baixa de título registram autor, data e hora, consultáveis pela supervisão sem solicitação à Neuon |
| Isolamento | Nenhuma consulta, extração ou relatório apresenta informação de carteira não atribuída ao usuário conectado |
| Precisão numérica | Valores em duas casas decimais, sem arredondamento intermediário entre o lançamento e a prestação de contas |
| Bloqueio de duplicidade | A combinação de fornecedor, número de documento e carteira é única; a segunda tentativa é recusada no ato, com a indicação do lançamento original |
| Guarda documental | O documento anexado ao título permanece acessível a partir do próprio título, sem depender de arquivo em rede |
| Desempenho | A extração da prestação de contas de uma carteira com até 500 títulos no período responde em até 30 segundos |
| Exportação | A prestação de contas é exportável em planilha e em PDF, no mesmo formato para todas as carteiras |
| LGPD | A informação de fornecedores e de carteiras é acessível apenas pelos perfis autorizados, e o acesso fica registrado |
| Usabilidade | A entrada de um título pelo operador é concluída em uma única tela, sem navegação entre rotinas |

## 11. Premissas e responsabilidades

**Responsabilidades da Neuon**

- Detalhamento funcional das regras de duplicidade, das faixas de alçada e do formato da prestação de contas, com aprovação formal da XPTO.
- Configuração do ambiente, desenvolvimento dos itens da onda e execução dos dois blocos de carga.
- Roteiro de aceite, condução da homologação e correção dos apontamentos dentro do escopo da onda.
- Treinamento dos perfis de operador de carteira e de supervisão, e operação assistida na entrada em produção.
- Documentação de entrega: manual da operação e registro da parametrização aplicada.

**Responsabilidades da XPTO**

- Indicação de um responsável pela operação de BPO, com disponibilidade para o detalhamento funcional e para a homologação.
- Definição das faixas de valor da alçada e dos aprovadores por carteira, antes do início da parametrização.
- Entrega das planilhas de origem da carteira-piloto na data de corte combinada, e complemento das inscrições de fornecedores pendentes.
- Conferência e aprovação formal do saldo de abertura migrado antes da entrada em produção.
- Disponibilização do ambiente Sankhya e dos acessos necessários à equipe de implantação.
- Homologação dentro do prazo de 10 dias úteis.

**Premissas gerais**

- O esforço desta proposta é expresso em horas: **60 horas** para a onda única, estimadas item a item sobre o escopo do capítulo 4.1.
- A premissa de carga considerada para derivação de prazo, caso venha a ser solicitada, é de 160 horas por mês por frente de trabalho.
- O escopo desta fase cobre **uma carteira-piloto** em produção. As demais carteiras entram por cadastro, sem desenvolvimento adicional, e a operação de cadastro é executada pela própria XPTO após o treinamento.
- A onda é considerada entregue com o **termo de homologação** assinado ao fim da fase de testes e homologação. O prazo de homologação pela XPTO é de 10 dias úteis; silêncio além desse prazo conta como aceite tácito.
- Apontamento dentro do escopo da onda é correção e está incluído nas horas previstas. Apontamento fora do escopo da onda é tratado como aditivo, estimado em horas pelo mesmo critério desta proposta.
- Alteração de escopo solicitada após a aprovação do detalhamento funcional é tratada como aditivo, com estimativa em horas apresentada antes da execução.
- A execução do pagamento permanece no canal bancário atual da XPTO; o sistema programa, registra e comprova, e não transmite pagamento nesta fase.
- Os itens do capítulo 4.2 não estão incluídos nas 60 horas e são estimados à parte quando contratados.
- A plataforma Sankhya está licenciada e em operação na XPTO; esta proposta cobre implantação e personalização, não licenciamento.

## 12. Próximos passos

| # | Passo | Responsável |
|---|---|---|
| 1 | Validação comercial deste escopo | XPTO |
| 2 | Fechamento de proposta e contrato | XPTO · Neuon Soluções |
| 3 | Indicação do responsável pela operação e definição das faixas de alçada | XPTO |
| 4 | Kick-off e detalhamento funcional da onda | Neuon Soluções |
| 5 | Início da implantação | Neuon Soluções |

---

Neuon Soluções — Escopo comercial · versão 1.0 · outubro/2026 · elaborado por Jonatan Barros
