ESCOPO TECNICCO FUNCIONAL PARA GERAR PROJETO ARMAZENAGEM DH

Escopo Técnico-Funcional: Projeto Gestão de Armazenagem (VILOG)
1. Visão Geral e Objetivo do Projeto
O sistema Gestão de Armazenagem é uma aplicação web projetada para calcular e gerenciar os custos de armazenagem de produtos para diferentes parceiros comerciais (clientes). O seu principal objetivo é automatizar a apuração dos valores a serem cobrados, com base em contratos, critérios de cobrança e movimentações de estoque (entradas e saídas) importadas do ERP Sankhya.
O fluxo principal consiste em:
1. Sincronizar dados mestres do Sankhya (Parceiros, Produtos, Notas Fiscais).
2. Configurar modelos de contrato e seus respectivos critérios de cobrança.
3. Vincular parceiros a esses modelos de contrato.
4. Processar as movimentações de estoque para calcular os custos de armazenagem com base nas regras definidas.
5. Disponibilizar dashboards e relatórios para análise gerencial e operacional.

2. Arquitetura de Dados e Integração
O sistema opera com um modelo de dados híbrido, utilizando um banco de dados interno (MySQL) para suas próprias configurações e transações, e integrando-se diretamente com o banco de dados do Sankhya (Oracle) para obter dados operacionais.
2.1. Integração de Dados Externos (Fonte da Verdade: Sankhya)
A base de todo o sistema são os dados importados do ERP Sankhya. Isso é feito através de rotinas de importação (IMPORTS) que populam tabelas internas (prefixo IMP_).
* IMP_PACEIRO: Traz os dados de parceiros da tabela TGFPAR do Sankhya.
* IMP_EMPRESA: Traz os dados de empresas da TSIEMP.
* IMP_PRODUTOS: Traz o cadastro de produtos da TGFPRO.
* IMP_NOTAS_ARMAZ: Tabela crucial que importa todas as movimentações de notas fiscais (entradas e saídas) das tabelas TGFCAB e TGFITE. Esta é a fonte primária para todos os cálculos de estoque.
* IMP_NOTA_ARMAZ_APROVAR: Importa as notas de venda que precisam de uma confirmação de entrada manual antes de serem consideradas no cálculo.
2.2. Modelo de Dados Interno (Tabelas Nativas do Projeto)
Estas são as tabelas que armazenam as regras de negócio, configurações e transações geradas pelo próprio sistema.
* TIPO_ARMAZENAGEM: Representa um “Modelo de Contrato”. Define um conjunto de regras de cobrança. Armazena o nome do contrato e o Tipo de Peso a ser usado nos cálculos (Bruto ou Líquido).
* CRITERIO_PADRAO: Uma tabela mestre com todos os critérios de cobrança possíveis (Ex: “Taxa de Recebimento”, “Seguro sobre o Valor Declarado”, “Armazenagem por Pico”).
* CRITERIO_COBRANCA: Tabela central da lógica de negócio. Vincula um TIPO_ARMAZENAGEM a um CRITERIO_PADRAO e define o valor ou percentual a ser cobrado.
* VINCULO_PARCEIRO_TIPO: Associa um CODPARC (Parceiro) a um ID_TIPO_ARMAZENAGEM (Modelo de Contrato), definindo qual regra de cobrança se aplica a qual parceiro. Também define o Período de Fechamento (Quinzenal, Mensal, etc.).
* GESTAO_NOTAS_APROVACAO: Tabela operacional onde as notas de entrada ficam “pendentes”, aguardando a confirmação manual do usuário na tela “Confirmação de Entrada”.
* NOTA_ARMAZ_APROVAR_ESPELHO: Tabela “espelho” que armazena um histórico das notas que já foram confirmadas e transferidas, garantindo que não apareçam novamente para confirmação.
* LANCAMENTO_ARMAZENAGEM: Cabeçalho dos lançamentos de cobrança gerados, vinculando um parceiro, tipo de armazenagem e data.
* ITEM_LANCAMENTO: Itens de um lançamento, detalhando cada critério cobrado, sua quantidade base, valor unitário e valor total.
* USUARIOS_CUSTOM e PERMISSOES_TELA: Tabelas para gestão de usuários e controle de acesso granular por tela e por tipo de operação (visualizar, incluir, editar, excluir).

3. Módulos e Funcionalidades (Telas)
O sistema é organizado em módulos acessíveis através de um menu principal.
3.1. Módulo de Cadastros
* Tela: Modelo de Contratos (Modelo de Contratos)
    * Objetivo: Gerenciar os TIPO_ARMAZENAGEM.
    * Como Funciona: Uma lista (PRETTY_LIST) exibe os contratos existentes. O usuário pode adicionar um novo contrato (que abre o modal Modal Novo Tipo Armazenagem), visualizar detalhes, configurar critérios ou excluir.
    * Processo-Chave: Ao criar um novo tipo, o sistema automaticamente insere todos os CRITERIO_PADRAO ativos na tabela CRITERIO_COBRANCA com valor zerado, preparando o contrato para configuração.
* Tela: Gestão de Critérios (Gestão de Critérios)
    * Objetivo: Gerenciar a tabela mestre CRITERIO_PADRAO.
    * Como Funciona: Uma tela de CRUD (GENERAL_LIST) que permite criar, editar e excluir os critérios que estarão disponíveis para serem usados nos contratos.
* Tela: Vínculo de Parceiros (Vínculo de Parceiros)
    * Objetivo: Gerenciar a tabela VINCULO_PARCEIRO_TIPO.
    * Como Funciona: Uma tabela (GENERAL_LIST) permite associar cada parceiro a um “Modelo de Contrato” e definir seu “Período de Fechamento”. Esta tela é fundamental para que o sistema saiba quais regras aplicar para cada cliente.
3.2. Módulo Operacional
* Tela: Confirmação de Entrada (Confirmação de Entrada)
    * Objetivo: Processo manual para que o usuário confirme a entrada física das mercadorias no armazém.
    * Como Funciona: É uma tela customizada (HTML) com um fluxo de 3 passos:
        1. Seleção de Parceiro: O usuário escolhe um parceiro.
        2. Seleção de Período: O sistema lista os períodos (quinzenas/meses) que possuem notas pendentes na tabela GESTAO_NOTAS_APROVACAO.
        3. Confirmação: Uma tabela exibe as notas do período. O usuário marca as notas que entraram e clica em “Salvar e Transferir”.
    * Processo-Chave: Ao “Salvar e Transferir”, o sistema move as notas confirmadas da GESTAO_NOTAS_APROVACAO para a NOTA_ARMAZ_APROVAR_ESPELHO. Isso “limpa” o período, garantindo que as notas não apareçam novamente. Esta é a lógica que precisava de ajuste: a lista de períodos não deveria mostrar meses onde todas as notas já foram movidas para a tabela espelho.
* Tela: Lançamentos por Nota (Lançamentos por Nota)
    * Objetivo: Visualizar e gerenciar os custos de armazenagem já calculados e lançados.
    * Como Funciona: Uma lista (GENERAL_LIST) exibe os lançamentos. O usuário pode criar um novo lançamento manual ou a partir de uma nota fiscal, ver os detalhes de um lançamento existente (abrindo o modal Detalhes do Lancamento), ou excluí-lo.
3.3. Módulo de Análise (Dashboards)
* Tela: Dashboards (Dashboard Estratégico)
    * Objetivo: Fornecer uma visão gerencial completa dos indicadores de armazenagem.
    * Como Funciona: Uma tela customizada (HTML) com filtros de data, parceiro e tipo. Apresenta múltiplos gráficos e KPIs, como:
        * Custo total e Volume total.
        * Evolução de custos ao longo do tempo.
        * Composição de custo por critério e por tipo de armazenagem.
        * Ranking de parceiros e produtos.
3.4. Módulo de Administração
* Tela: Usuários e Permissões (Gestao de Usuarios Simplificada)
    * Objetivo: Gerenciar usuários e suas permissões de acesso.
    * Como Funciona: Uma tela de CRUD (GENERAL_LIST) para a tabela USUARIOS_CUSTOM. Permite adicionar, editar e excluir usuários. O botão “Gerenciar Permissões” abre um modal (ModalGerenciarPermissoes) que controla a tabela PERMISSOES_TELA, permitindo definir acessos de forma granular para cada tela do sistema.

4. Lógica de Cálculo de Custos (Processo Central)
Este é o coração do sistema e ocorre principalmente através de queries SQL complexas nos dashboards e relatórios.
1. Determinação do Tipo de Peso: Para um dado parceiro, o sistema consulta a tabela VINCULO_PARCEIRO_TIPO e TIPO_ARMAZENAGEM para saber se deve usar o Peso Bruto (PESO_ITEM) ou Líquido (PESO_ITEM_LIQ) das notas.
2. Cálculo de Estoque e Pico:
    * O sistema reconstrói o histórico de movimentações (entradas e saídas) da IMP_NOTAS_ARMAZ e GESTAO_NOTAS_APROVACAO dentro do período de filtro.
    * Ele calcula o saldo inicial (estoque antes do período começar).
    * Ele calcula o saldo acumulado dia a dia dentro do período.
    * O Pico de Estoque é o maior valor encontrado entre o saldo inicial e os saldos acumulados diários.
3. Aplicação dos Critérios:
    * Para cada CRITERIO_COBRANCA vinculado ao parceiro, o sistema aplica uma regra:
        * Se TIPO_CALCULO for “Pico”: Usa o Pico de Estoque (em toneladas) e multiplica pelo VALOR do critério.
        * Se TIPO_CALCULO for “Entrada”: Soma todo o peso de entrada no período (em toneladas) e multiplica pelo VALOR.
        * Se TIPO_CALCULO for “Saida”: Soma todo o peso de saída no período (em toneladas) e multiplica pelo VALOR.
        * Se TIPO_CALCULO for “Valor Declarado” (Seguro): Usa o Pico de Valor do estoque e aplica o PERCENTUAL do critério.
4. Totalização: O custo total do período é a soma dos valores calculados para cada critério aplicável, mais quaisquer lançamentos manuais.
