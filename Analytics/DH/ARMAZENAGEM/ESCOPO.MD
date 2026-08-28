ESCOPO TECNICCO FUNCIONAL PARA GERAR PROJETO ARMAZENAGEM DH

Escopo Técnico-Funcional: Projeto Gestão de Armazenagem (VILOG)
1. Visão Geral e Objetivo do Projeto
O sistema Gestão de Armazenagem é uma aplicação web projetada para calcular e gerenciar os custos de armazenagem de produtos para diferentes parceiros comerciais (clientes). O seu principal objetivo é automatizar a apuração dos valores a serem cobrados, com base em contratos, critérios de cobrança e movimentações de estoque (entradas e saídas) importadas do ERP Sankhya.
O fluxo principal consiste em:
1. Sincronizar dados mestres do Sankhya (Parceiros, Produtos, Notas Fiscais).
2. Configurar modelos de contrato e seus respectivos critérios de cobrança.
3. Vincular parceiros a esses modelos de contrato.
4. Processar automaticamente TODAS as movimentações de estoque importadas (entradas e saídas), sem etapa de confirmação manual, para calcular os custos de armazenagem com base nas regras definidas.
5. Disponibilizar dashboards e relatórios de entradas e saídas — por nota e por item — com exportação em PDF/Excel/CSV utilizando o logo da DH.

2. Arquitetura de Dados e Integração
O sistema opera com um modelo de dados híbrido, utilizando um banco de dados interno (MySQL) para suas próprias configurações e transações, e integrando-se diretamente com o banco de dados do Sankhya (Oracle) para obter dados operacionais.
2.1. Integração de Dados Externos (Fonte da Verdade: Sankhya)
A base de todo o sistema são os dados importados do ERP Sankhya. Isso é feito através de rotinas de importação (IMPORTS) que populam tabelas internas (prefixo IMP_).
* IMP_PACEIRO: Traz os dados de parceiros da tabela TGFPAR do Sankhya.
* IMP_EMPRESA: Traz os dados de empresas da TSIEMP.
* IMP_PRODUTOS: Traz o cadastro de produtos da TGFPRO.
* IMP_NOTAS_ARMAZ: Tabela crucial que importa TODAS as movimentações de notas fiscais (entradas e saídas) das tabelas TGFCAB e TGFITE. Esta é a fonte única e primária para todos os cálculos de estoque. Todas as notas importadas entram automaticamente no fluxo de cálculo, sem qualquer etapa de confirmação/aprovação manual.
* IMP_ITENS_NOTAS_ARMAZ: Itens das notas importadas (TGFITE), com produto, quantidade, valor unitário, peso bruto e peso líquido. É a base dos relatórios analíticos por item.
2.2. Modelo de Dados Interno (Tabelas Nativas do Projeto)
Estas são as tabelas que armazenam as regras de negócio, configurações e transações geradas pelo próprio sistema.
* TIPO_ARMAZENAGEM: Representa um “Modelo de Contrato”. Define um conjunto de regras de cobrança. Armazena o nome do contrato e o Tipo de Peso a ser usado nos cálculos (Bruto ou Líquido).
* CRITERIO_PADRAO: Uma tabela mestre com todos os critérios de cobrança possíveis (Ex: “Taxa de Recebimento”, “Seguro sobre o Valor Declarado”, “Armazenagem por Pico”).
* CRITERIO_COBRANCA: Tabela central da lógica de negócio. Vincula um TIPO_ARMAZENAGEM a um CRITERIO_PADRAO e define o valor ou percentual a ser cobrado.
* VINCULO_PARCEIRO_TIPO: Associa um CODPARC (Parceiro) a um ID_TIPO_ARMAZENAGEM (Modelo de Contrato), definindo qual regra de cobrança se aplica a qual parceiro. Também define o Período de Fechamento (Quinzenal, Mensal, etc.).
* (REMOVIDO) GESTAO_NOTAS_APROVACAO e NOTA_ARMAZ_APROVAR_ESPELHO: tabelas do antigo fluxo de confirmação manual de entrada. Não são mais utilizadas — todas as notas de IMP_NOTAS_ARMAZ são consideradas automaticamente.
* LANCAMENTO_ARMAZENAGEM: Cabeçalho dos lançamentos de cobrança gerados, vinculando um parceiro, tipo de armazenagem e data.
* ITEM_LANCAMENTO: Itens de um lançamento, detalhando cada critério cobrado, sua quantidade base, valor unitário e valor total.
* USUARIOS_CUSTOM e PERMISSOES_TELA: Tabelas para gestão de usuários e controle de acesso granular por tela e por tipo de operação (visualizar, incluir, editar, excluir).

2.3. Credenciais de Acesso à API Sankhya (Cliente DH)
Parâmetros de autenticação utilizados pela integração com o ERP Sankhya (Gateway / API de Autenticação).
* Client ID: fe0cdb62-b1d7-4f79-b938-0bd847307fd2
* Client Secret: 9hS6yRK0OLt6NjV3wjoz7W4ob0mJRBel
* Token (header X-Token): abe78d63-7710-430a-b3d9-7ee33d91527d

Uso: as três chaves são enviadas na chamada de autenticação (headers `client_id`, `client_secret` e `X-Token`, acompanhados de `appkey`/usuário conforme o ambiente). O retorno é um Bearer Token de sessão, reutilizado nas chamadas seguintes de leitura das tabelas TGFPAR, TSIEMP, TGFPRO, TGFCAB e TGFITE que alimentam as tabelas IMP_*.

ATENÇÃO — SEGURANÇA: estas credenciais são secretas e este arquivo está versionado em repositório Git. Recomenda-se movê-las para variáveis de ambiente / cofre de segredos (ex.: `SANKHYA_CLIENT_ID`, `SANKHYA_CLIENT_SECRET`, `SANKHYA_TOKEN`) e manter neste documento apenas os nomes das variáveis. Em caso de exposição indevida, solicitar a rotação das chaves junto ao administrador do Sankhya.

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
* (REMOVIDA) Tela: Confirmação de Entrada
    * A etapa de confirmação manual de entrada foi ELIMINADA do sistema. Não existe mais tela, fluxo de 3 passos, botão “Salvar e Transferir”, nem tabelas de pendência/espelho.
    * Nova regra: TODAS as notas importadas em IMP_NOTAS_ARMAZ (entradas e saídas) são consideradas automaticamente e imediatamente no fluxo de cálculo, dashboards e relatórios, sem aprovação prévia.
    * Impacto: os cálculos de saldo, pico, entradas e saídas passam a ler exclusivamente IMP_NOTAS_ARMAZ, filtrando as movimentações por ATUALESTOQUE = 1, CODPARC e período (DTNEG / DATA_ENTREGA). O tipo de movimento (Entrada/Saída) é determinado pelo Tipo de Operação (TOP/CODTIPOPER) da nota importada.
    * Migração: as telas, queries e rotinas que referenciavam GESTAO_NOTAS_APROVACAO, IMP_NOTA_ARMAZ_APROVAR, IMP_ITENS_NOTAS_APRV e NOTA_ARMAZ_APROVAR_ESPELHO devem ser reescritas para IMP_NOTAS_ARMAZ / IMP_ITENS_NOTAS_ARMAZ.
* Tela: Lançamentos por Nota (Lançamentos por Nota)
    * Objetivo: Visualizar e gerenciar os custos de armazenagem já calculados e lançados.
    * Como Funciona: Uma lista (GENERAL_LIST) exibe os lançamentos. O usuário pode criar um novo lançamento manual ou a partir de uma nota fiscal, ver os detalhes de um lançamento existente (abrindo o modal Detalhes do Lancamento), ou excluí-lo.
3.3. Módulo de Relatórios e Exportação
* Tela: Relatório de Entradas e Saídas (Relatório de Movimentações)
    * Objetivo: Consultar e exportar todas as movimentações de entrada e saída do período, considerando TODAS as notas importadas em IMP_NOTAS_ARMAZ (sem etapa de confirmação).
    * Filtros: Parceiro (obrigatório), Período (data início / data fim), Tipo de Movimento (Entrada / Saída / Ambos), Produto (opcional) e Empresa (opcional).
    * Visões disponíveis (abas):
        1. Por Nota (sintético): uma linha por nota fiscal — Número Único (NUNOTA), Número da Nota (NUMNOTA), Série, Data de Negociação/Entrega, Tipo de Movimento (Entrada/Saída), Tipo de Operação (TOP), Parceiro, Qtd. de Itens, Peso Bruto (Kg), Peso Líquido (Kg) e Valor Total (R$).
        2. Por Item (analítico): uma linha por item de nota — NUNOTA, Número da Nota, Data, Tipo de Movimento, Código e Descrição do Produto, Unidade, Quantidade, Valor Unitário, Valor Total, Peso Bruto (Kg) e Peso Líquido (Kg).
    * Totalizadores: rodapé com Total de Entradas, Total de Saídas e Saldo do Período (peso e valor), coerentes com a Lógica de Cálculo da seção 4.
* Funcionalidade: Exportação de Relatórios
    * Botão “Exportar” disponível nas duas visões (Por Nota e Por Item), respeitando exatamente os filtros e a ordenação aplicados na tela.
    * Formatos suportados:
        * PDF: layout paisagem, com cabeçalho institucional contendo o LOGO DA DH, título do relatório, nome/código do parceiro, período filtrado, data-hora da emissão e usuário emissor; rodapé com numeração de páginas (“Página X de Y”) e linha de totais repetida ao final.
        * Excel (XLSX): primeira aba com os dados tabulados (cabeçalho congelado, colunas numéricas formatadas, filtros automáticos) e o LOGO DA DH posicionado no topo da planilha, acima do cabeçalho da tabela.
        * CSV: exportação simples dos dados, sem logo, para integração com outros sistemas.
    * Identidade visual: o logo da DH é o mesmo utilizado no restante do sistema (arquivo de imagem em base64 ou asset estático), aplicado no canto superior esquerdo do cabeçalho, com a razão social e o título do relatório à direita.
    * Nomenclatura dos arquivos: `Relatorio_Movimentacoes_<PorNota|PorItem>_<CODPARC>_<AAAAMMDD_inicio>_<AAAAMMDD_fim>.<ext>`.
    * Permissões: a exportação é controlada por PERMISSOES_TELA, permitindo liberar visualização sem liberar exportação.
    * Queries de apoio: queryExportarNotas.sql (visão Por Nota) e queryItensArmazenados.sql / queryItensConsolidados.sql (visão Por Item), ambas apontando para IMP_NOTAS_ARMAZ e IMP_ITENS_NOTAS_ARMAZ.
3.4. Módulo de Análise (Dashboards)
* Tela: Dashboards (Dashboard Estratégico)
    * Objetivo: Fornecer uma visão gerencial completa dos indicadores de armazenagem.
    * Como Funciona: Uma tela customizada (HTML) com filtros de data, parceiro e tipo. Apresenta múltiplos gráficos e KPIs, como:
        * Custo total e Volume total.
        * Evolução de custos ao longo do tempo.
        * Composição de custo por critério e por tipo de armazenagem.
        * Ranking de parceiros e produtos.
3.5. Módulo de Administração
* Tela: Usuários e Permissões (Gestao de Usuarios Simplificada)
    * Objetivo: Gerenciar usuários e suas permissões de acesso.
    * Como Funciona: Uma tela de CRUD (GENERAL_LIST) para a tabela USUARIOS_CUSTOM. Permite adicionar, editar e excluir usuários. O botão “Gerenciar Permissões” abre um modal (ModalGerenciarPermissoes) que controla a tabela PERMISSOES_TELA, permitindo definir acessos de forma granular para cada tela do sistema.

4. Lógica de Cálculo de Custos (Processo Central)
Este é o coração do sistema e ocorre principalmente através de queries SQL complexas nos dashboards e relatórios.
1. Determinação do Tipo de Peso: Para um dado parceiro, o sistema consulta a tabela VINCULO_PARCEIRO_TIPO e TIPO_ARMAZENAGEM para saber se deve usar o Peso Bruto (PESO_ITEM) ou Líquido (PESO_ITEM_LIQ) das notas.
2. Cálculo de Estoque e Pico:
    * O sistema reconstrói o histórico de movimentações (entradas e saídas) exclusivamente a partir da IMP_NOTAS_ARMAZ dentro do período de filtro, considerando TODAS as notas importadas (sem filtro de aprovação/confirmação).
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
