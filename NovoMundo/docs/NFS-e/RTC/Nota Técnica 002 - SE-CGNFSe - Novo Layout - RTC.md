   Projeto Reforma Tributária do Consumo –
              Adequações NFS-e
                 Nota Técnica Nº 002 – Versão 1.0




                           28 de fevereiro de 2025



Secretaria-Executiva do Comitê Gestor da Nota Fiscal de Serviço Eletrônica
                    de Padrão Nacional (SE/CGNFS-e)
                                                                         Sumário

1.     Introdução ................................................................................................................................................. 3
2.     Layout NFS-e: Novos Grupos no Contexto da EC nº 132/2023.................................................................. 4
     2.1       Novos Grupos na DPS da NFS-e........................................................................................................ 6
       2.1.1        Grupo de Informações Relativas ao Destinatário ........................................................................ 6
       2.1.2        Grupo de Informações Relativas ao Adquirente .......................................................................... 7
       2.1.3        Grupo de Informações Relativas ao Serviço Prestado para IBS e CBS ......................................... 8
       2.1.4        Grupo de Informações Relativas aos Valores do Serviço Prestado para IBS e CBS ...................... 8
       2.1.4.1          Grupo de Informações Específicas Relativas ao IBS Estadual .................................................. 9
       2.1.4.2          Grupo de Informações Específicas Relativas ao IBS Municipal................................................ 9
       2.1.4.3          Grupo de Informações Específicas Relativas à CBS ............................................................... 10
     2.2       Novos Grupos na NFS-e .................................................................................................................. 10
       2.2.1        Grupo de Informações Comuns Relativas ao IBS e à CBS .......................................................... 11
       2.2.2        Grupo de Informações de Valores Brutos Relativos ao IBS e à CBS ........................................... 11
       2.2.3        Grupos Totalizadores ................................................................................................................. 12
       2.2.3.1          Grupo de Informações Relativas às Totalizações do IBS ........................................................ 12
       2.2.3.2          Grupo de Informações Relativas às Totalizações da CBS ....................................................... 12




                                                                                                                                                                   2
   1. Introdução

       Este documento contempla a segunda versão dos novos agrupamentos e campos opcionais do
layout da Nota Fiscal de Serviço eletrônica – NFS-e padrão nacional relacionados à tributação do
Imposto sobre Bens e Serviços – IBS e da Contribuição sobre Bens e Serviços – CBS incidentes nas
operações de serviços, em atendimento às alterações previstas na Emenda Constitucional nº 132 de,
20 de dezembro de 2023, que deu ensejo à Reforma Tributária do Consumo – RTC.

       Os novos agrupamentos de campos foram inseridos a partir do layout atual da NFS-e, presente
no documento “AnexoIV-LeiautesRN_ADN-SNNFSe_V1.00.02-Produção.xlsx”, aba “LEIAUTE
NFS-e ADN” que consta na sessão de documentação técnica no Portal da NFS-e:
https://www.gov.br/nfse/pt-br/biblioteca/documentacao-tecnica.

       Importante esclarecer que o conjunto de campos apresentados neste documento é uma segunda
versão, resultado de estudos técnicos realizados tomando como base o texto da Lei Complementar –
LC nº 214, de 16 de janeiro de 2025, e sua divulgação objetiva dar transparência aos Municípios, às
empresas prestadoras de serviço e de Tecnologia da Informação – TI e contribuintes para que possam
se familiarizar com o novo padrão que deverá vigorar a partir de janeiro de 2026. Os estudos técnicos
permanecem e novas versões deverão ser publicadas nos próximos meses com atualizações do layout
proposto.

       Para melhor compreensão dos agrupamentos que serão apresentados no próximo tópico desta
Nota Técnica, a seguir é apresentada a modelagem do processo de emissão da NFS-e. O emissor
(prestador de serviços) preenche a Declaração de Prestação de Serviço (DPS) que será enviada à
“Sefin Nacional”, responsável pela validação das informações, cálculo dos tributos e autorização da
NFS-e. Caso as informações atendam aos requisitos, será gerada a NFS-e em formato XML com o
destaque dos tributos devidos e que poderá ser acessada pelo emissor e demais envolvidos na
operação. É importante observar que a DPS é assinada e encapsulada no interior da nota gerada e que,
dessa forma, há dois grupos de informações apresentados neste documento: um relativo à DPS com
campos que serão informados pelo contribuinte e outro que, a partir dessas informações, terá campos
calculados pela plataforma.




                                                                                                   3
2. Layout NFS-e: Novos Grupos no Contexto da EC nº 132/2023

          Para melhor compreensão das informações disponibilizadas nos layouts dispostos
   neste tópico, segue um breve glossário.
      •   CAMINHO NO XML
          O XML (Extensible Markup Language) é uma linguagem de marcação utilizada para
          estruturar e armazenar dados em um formato legível por máquina que obedece aos
          layouts dispostos neste documento e que possui regras a serem seguidas, em uma
          sequência pré-definida e ordenada. Esta coluna informa o path, ou caminho, ordenado
          em que as informações devem ser dispostas na formatação do XML.


      •   CAMPO
          Nome do campo/informação que deve constar no XML.


      •   ELE
          Elemento ou valor do campo que deve ser informado. Pode ser dos tipos:
              o ID: Campo identificador do documento;
              o E: Element (Elemento), atributo deve ser informado no campo;
              o G: Group (Grupo), tag que identifica um grupo de informações que será
                 formado por elements (E), choice elements (CE) ou outros grupos;
              o CG: Choice Group (Grupo de Escolha), tag que identifica um grupo que deverá
                 ser informado a depender da escolha do emitente;
              o CE: Choice Element (Elemento de Escolha), elemento de uma lista pré-
                 determinada, a ser preenchida pelo emitente.



                                                                                           4
•   TIPO
    Tipo do campo que deve ser informado para a tag específica. Pode ser dos tipos:
       o N: Numérico;
       o C: Caractere;
       o D: Data.


    Nas tags G ou CG (Group ou Choice Group), como são apenas informações de
    agrupamento de campos, não há um tipo relacionado.


•   OCOR.
    Quantidade de ocorrências possíveis para o campo/tag.
    Por exemplo:
       o 0-1: A informação não é obrigatória e, caso seja informada, só será possível de
           ser informada uma única vez no documento;
       o 1-1: Informação obrigatória e que deverá ser informada apenas uma vez no
           documento;
       o 1-60: Informação obrigatória que deverá ser informada até, no máximo, 60
           vezes no documento.
•   TAM.
    Tamanho da informação para o campo/tag.
    Por exemplo:
    o 14: Campo de tamanho fixo com 14 caracteres ou números;
    o 1-150: Campo de tamanho variável, podendo possuir de 1 a 150 caracteres ou
       números;
    o 1-3V2: Campo de tamanho variável, podendo possuir de 1 a 3 números mais duas
       casas decimais.


    Nas tags G ou CG (Group ou Choice Group), como são apenas informações de
    agrupamento de campos, não há um tamanho relacionado.


•   DESCRIÇÃO
    Descrição sucinta do que representa cada campo e como deve ser preenchido.




                                                                                      5
                      2.1 Novos Grupos na DPS da NFS-e

                     Na DPS, foi criado o grupo IBSCBS (caminho NFSe/infNFSe/DPS/infDPS/). Neste grupo
            serão dispostos todos os subgrupos e informações relativas aos novos tributos: IBS e CBS; e que
            deverão ser informados pelo contribuinte na emissão. Importante observar que os campos/tags
            listados abaixo em vermelho se referem a campos que foram modificados em relação à versão
            anterior, publicada na Nota Técnica SE/CGNFS-e nº 001, de 01 de agosto de 2024.


                                       CAMPO      ELE     TIPO    OCOR.      TAM.                             DESCRIÇÃO
     CAMINHO NO XML
                                                                                    Grupo de informações declaradas pelo emitente referentes ao
NFSe/infNFSe/DPS/infDPS/               IBSCBS     G         -      1-1        -
                                                                                                           IBS e à CBS




                          2.1.1    Grupo de Informações Relativas ao Destinatário

                                                        CAMPO        ELE     TIPO   OCOR.   TAM.                      DESCRIÇÃO
                CAMINHO NO XML
NFSe/infNFSe/DPS/infDPS/IBSCBS/                          dest            G    -      0-1     -     Grupo de informações relativas ao Destinatário

                                                                                                   Número da inscrição no Cadastro Nacional de
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                     CNPJ        CE       N      1-1     14
                                                                                                   Pessoa Jurídica (CNPJ) do destinatário de serviço
                                                                                                   Número da inscrição no Cadastro Nacional de
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                     CPF         CE       N      1-1     11
                                                                                                   Pessoa Física (CPF) do destinatário do serviço
                                                                                                   Número de identificação fiscal fornecido por órgão
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                      NIF        CE       C      1-1     40
                                                                                                   de administração tributária no exterior
                                                                                                   Motivo para não informação do NIF:

NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                    cNaoNIF      CE       N      1-1     1     0 - Não informado na nota de origem;
                                                                                                   1 - Dispensado do NIF;
                                                                                                   2 - Não exigência do NIF;
                                                                                                   Número do Cadastro de Atividade Econômica da
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                    CAEPF            E    N      0-1     14
                                                                                                   Pessoa Física (CAEPF) do destinatário do serviço.

NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                    xNome            E    C      1-1    150    Nome / Nome Empresarial do destinatário

                                                                                                   Grupo de informações do endereço do destinatário
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                     end             G    -      0-1     -
                                                                                                   do serviço.

NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/                endNac       CG       -      1-1     -     Grupo de informações do endereço nacional.

                                                                                                   Código do município do endereço do destinatário do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/endNac/          cMun            E    N      1-1     7     serviço.
                                                                                                    (Tabela do IBGE)

                                                                                                   Código numérico do Endereçamento Postal nacional
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/endNac/          CEP             E    C      1-1     8     (CEP)
                                                                                                    do endereço do destinatário do serviço.
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/                endExt       CG       -      1-1     -     Grupo de informações do endereço no exterior.
                                                                                                   Código do país do endereço do destinatário do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/endExt/          cPais           E    C      1-1     2     serviço.
                                                                                                    (Tabela de Países ISO)
                                                                                                   Código alfanumérico do Endereçamento Postal no
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/endExt/       cEndPost           E    C      1-1    1-11
                                                                                                   exterior do destinatário do serviço.
                                                                                                   Nome da cidade no exterior do destinatário do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/endExt/         xCidade          E    C      1-1    1-60
                                                                                                   serviço.
                                                                                                   Estado, província ou região da cidade no exterior do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/endExt/    xEstProvReg           E    C      1-1    1-60
                                                                                                   destinatário do serviço.
                                                                                             1-    Tipo e nome do logradouro do endereço do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/                 xLgr            E    C      1-1
                                                                                            255    destinatário do serviço.
                                                                                                   Número no logradouro do endereço do destinatário
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/                  nro            E    C      1-1    1-60
                                                                                                   do serviço.
                                                                                             1-    Complemento do endereço do destinatário do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/                 xCpl            E    C      0-1
                                                                                            156    serviço.

NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/                xBairro          E    C      1-1    1-60   Bairro do endereço do destinatário do serviço.



                                                                                                                                        6
                                                                                           Número do telefone do destinatário.
                                                                                            (Preencher com o Código DDD + número do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                 fone       E     N      0-1    6-20   telefone. Nas operações com exterior é permitido
                                                                                           informar o código do país + código da localidade +
                                                                                           número do telefone)
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                email       E     C      0-1    1-80   E-mail do destinatário.




                          2.1.2    Grupo de Informações Relativas ao Adquirente

                                                  CAMPO        ELE   TIPO   OCOR.   TAM.                      DESCRIÇÃO
                CAMINHO NO XML
NFSe/infNFSe/DPS/infDPS/IBSCBS/                     adq        G      -      0-1     -     Grupo de informações relativas ao Adquirente

                                                                                           Número da inscrição no Cadastro Nacional de Pessoa
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                 CNPJ       CE     N      1-1     14
                                                                                           Jurídica (CNPJ) do adquirente de serviço
                                                                                           Número da inscrição no Cadastro de Pessoa Física
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                 CPF        CE     N      1-1     11
                                                                                           (CPF) do adquirente do serviço
                                                                                           Número de identificação fiscal fornecido por órgão de
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                 NIF        CE     C      1-1     40
                                                                                           administração tributária no exterior
                                                                                           Motivo para não informação do NIF:

NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/               cNaoNIF      CE     N      1-1     1     0 - Não informado na nota de origem;
                                                                                           1 - Dispensado do NIF;
                                                                                           2 - Não exigência do NIF;
                                                                                           Número do Cadastro de Atividade Econômica da
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                CAEPF        E     N      0-1     14
                                                                                           Pessoa Física (CAEPF) do adquirente do serviço.

NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                xNome        E     C      1-1    150    Nome / Nome Empresarial do adquirente

                                                                                           Grupo de informações do endereço do adquirente do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                 end        G      -      0-1     -
                                                                                           serviço.

NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/            endNac      CG     -      1-1     -     Grupo de informações do endereço nacional.
                                                                                           Código do município do endereço do adquirente do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/endNac/      cMun        E     N      1-1     7     serviço.
                                                                                            (Tabela do IBGE)
                                                                                           Código numérico do Endereçamento Postal nacional
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/endNac/      CEP         E     C      1-1     8     (CEP)
                                                                                            do endereço do adquirente do serviço.
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/            endExt      CG     -      1-1     -     Grupo de informações do endereço no exterior.
                                                                                           Código do país do endereço do adquirente do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/endExt/      cPais       E     C      1-1     2     serviço.
                                                                                            (Tabela de Países ISO)
                                                                                           Código alfanumérico do Endereçamento Postal no
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/endExt/    cEndPost      E     C      1-1    1-11
                                                                                           exterior do adquirente do serviço.
                                                                                           Nome da cidade no exterior do adquirente do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/endExt/    xCidade       E     C      1-1    1-60
                                                                                           serviço.
                                                                                           Estado, província ou região da cidade no exterior do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/endExt/   xEstProvReg    E     C      1-1    1-60
                                                                                           adquirente do serviço.
                                                                                     1-    Tipo e nome do logradouro do endereço do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/             xLgr        E     C      1-1
                                                                                    255    adquirente do serviço.
                                                                                           Número no logradouro do endereço do adquirente do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/              nro        E     C      1-1    1-60
                                                                                           serviço.
                                                                                     1-
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/             xCpl        E     C      0-1           Complemento do endereço do adquirente do serviço.
                                                                                    156

NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/            xBairro      E     C      1-1    1-60   Bairro do endereço do adquirente do serviço.
                                                                                           Número do telefone do adquirente do serviço.
                                                                                            (Preencher com o Código DDD + número do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                 fone        E     N      0-1    6-20   telefone. Nas operações com exterior é permitido
                                                                                           informar o código do país + código da localidade +
                                                                                           número do telefone)
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                 email       E     C      0-1    1-80   E-mail do adquirente.




                                                                                                                                7
                          2.1.3     Grupo de Informações Relativas ao Serviço Prestado para IBS e CBS

                                                           CAMPO             ELE     TIPO    OCOR.    TAM.                           DESCRIÇÃO
                 CAMINHO NO XML
                                                                                                               Grupo de informações relativas ao serviço
NFSe/infNFSe/DPS/infDPS/IBSCBS/                              serv             G          G    1-1         -
                                                                                                               prestado para IBS/CBS.
                                                                                                               Modo de prestação do serviço:
NFSe/infNFSe/DPS/infDPS/IBSCBS/serv/                    modoPrestServ         E          C    1-1         1
                                                                                                               1 - Presencial;
                                                                                                               2 - Não presencial;

NFSe/infNFSe/DPS/infDPS/IBSCBS/serv/                    clocalPrestServ      CE          N    1-1         7    Código da localidade da prestação do serviço.
                                                                                                               Código do país onde ocorreu a prestação do
NFSe/infNFSe/DPS/infDPS/IBSCBS/serv/                    cPaisPrestServ       CE          C    1-1         2    serviço.
                                                                                                                (Tabela de Países ISO)
                                                                                                               Cadastro de imóveis. Obrigatório para construção
NFSe/infNFSe/DPS/infDPS/IBSCBS/serv/                         cCIB             E          C    0-1         8
                                                                                                               civil.
                                                                                                               Grupo de informações de compras
NFSe/infNFSe/DPS/infDPS/IBSCBS/serv/                    gCompraGov            G          -    0-1         -    governamentais relacionadas aos tributos
                                                                                                               IBS / CBS



                                                                                                               Indicador de compra governamental
NFSe/infNFSe/DPS/infDPS/IBSCBS/serv/gCompraGov/          indCompGov           E          N    0-1         1    0 - Não;
                                                                                                               1 - Sim;




                          2.1.4     Grupo de Informações Relativas aos Valores do Serviço Prestado para IBS e
                                    CBS

                                                                              CAMPO           ELE    TIPO     OCOR.   TAM.                DESCRIÇÃO
                         CAMINHO NO XML
                                                                                                                              Grupo de informações relativas
NFSe/infNFSe/DPS/infDPS/IBSCBS/                                               valores          G      -        1-1      -     aos valores do serviço prestado
                                                                                                                              para IBS e CBS
                                                                                                                              Grupo de informações
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/                                           trib         G      -        1-1      -     relacionados aos tributos IBS e
                                                                                                                              CBS
                                                                                                                              Grupo de informações
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/                                 gIBSCBS           G      -        1-1      -
                                                                                                                              relacionadas ao IBS e à CBS

                                                                                                                              Código de Situação Tributária do
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/                         cstIBSCBS         E      N        1-1      3
                                                                                                                              IBS e da CBS

                                                                                                                              Código de Classificação Tributária
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/                      cClassTribIBSCBS     E      N        1-1      6
                                                                                                                              do IBS e da CBS

                                                                                                                              Grupo de Informações do
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/                       gIBSCredPres        G      -        1-1      -     Crédito Presumido
                                                                                                                              referente ao IBS
                                                                                                                              Código de Classificação do Crédito
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSCredPres/          cCredPresIBS        E      N        1-1      2
                                                                                                                              Presumido do IBS.
                                                                                                                        1-    Percentual do Crédito Presumido do
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSCredPres/          pCredPresIBS        E      N        1-1
                                                                                                                       2V2    IBS.




                                                                                                                                                  8
                               2.1.4.1 Grupo de Informações Específicas Relativas ao IBS Estadual

                                                                          CAMPO           ELE        TIPO OCOR.       TAM.                DESCRIÇÃO
                           CAMINHO NO XML
                                                                                                                                 Grupo de informações
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/                      gIBSUF            G         -       1-1      -         relacionados ao IBS para o
                                                                                                                                 Estado
                                                                                                                                 Grupo de informações
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSUF/                 gDif            G         -       0-1      -         relacionadas ao diferimento
                                                                                                                                 para o Estado
                                                                                                                       1-        Percentual de diferimento
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSUF/gDif/           pDifUF           E         N       1-1
                                                                                                                      3V2        estadual.
                                                                                                                                 Grupo de informações
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSUF/              gDevTrib           G         -       0-1      -         relacionados à devolução
                                                                                                                                 tributária para o Estado
                                                                                                                       1-        Devolução personalizada do
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSUF/gDevol/       vDevTribUF         E         N       1-1
                                                                                                                      15V2       IBS estadual.
                                                                                                                                 Grupo de informações
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSUF/               gDeson            G         -       0-1      -         relacionados à desoneração
                                                                                                                                 para o Estado
                                                                                                                                 Código de Situação tributária
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSUF/gDeson/       cstUFDeson         E         N       1-1      3         estadual de Desoneração do
                                                                                                                                 IBS.
                                                                                                                                 Código de Classificação
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSUF/gDeson/    cClassTribUFDeson     E         N       1-1      6         estadual de Desoneração do
                                                                                                                                 IBS.

                                                                                                                       1-        Valor da alíquota estadual de
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSUF/gDeson/      pAliqUFDeson        E         N       1-1
                                                                                                                      2V2        Desoneração do IBS.




                               2.1.4.2 Grupo de Informações Específicas Relativas ao IBS Municipal

                                                                             CAMPO              ELE    TIPO    OCOR.    TAM.               DESCRIÇÃO
                            CAMINHO NO XML
                                                                                                                                    Grupo de informações
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/                        gIBSMun             G         -     1-1          -      relacionados ao IBS para
                                                                                                                                    o Município
                                                                                                                                    Grupo de informações
                                                                                                                                    relacionadas ao
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSMun/                  gDif              G         -     0-1          -
                                                                                                                                    diferimento para o
                                                                                                                                    Município
                                                                                                                            1-      Percentual de diferimento
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSMun/gDif/            pDifMun             E        N     1-1
                                                                                                                           3V2      municipal.
                                                                                                                                    Grupo de informações
                                                                                                                                    relacionadas à devolução
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSMun/                gDevTrib            G         -     0-1          -
                                                                                                                                    tributária para o
                                                                                                                                    Município
                                                                                                                            1-      Devolução personalizada do
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSMun/gDevol/       vDevTribMun            E        N     1-1
                                                                                                                           15V2     IBS municipal.

                                                                                                                                    Grupo de informações
                                                                                                                                    relacionadas à
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSMun/                 gDeson             G         -     0-1          -
                                                                                                                                    desoneração para o
                                                                                                                                    Município
                                                                                                                                    Código de Situação
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSMun/gDeson/       cstMunDeson            E        N     1-1         3       tributária municipal de
                                                                                                                                    desoneração do IBS.
                                                                                                                                    Código de Classificação
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSMun/gDeson/    cClassTribMunDeson        E        N     1-1         6       municipal de desoneração
                                                                                                                                    do IBS.

                                                                                                                            1-      Valor da alíquota municipal
NFSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gIBSMun/gDeson/      pAliqMunDeson           E        N     1-1
                                                                                                                           2V2      de desoneração do IBS.




                                                                                                                                                9
                                 2.1.4.3 Grupo de Informações Específicas Relativas à CBS

                         CAMINHO NO XML                                          CAMPO           ELE   TIPO   OCOR.   TAM.              DESCRIÇÃO
                                                                                                                             Grupo de informações
FSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/                               gCBS            G     -      1-1     -
                                                                                                                             relacionadas à CBS
                                                                                                                             Grupo de informações
FSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gCBS/                   gCBSCredPres           G     -      0-1     -     relacionadas ao crédito
                                                                                                                             presumido para a CBS

                                                                                                                             Código de Classificação do
Se/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gCBS/gCBSCredPres/       cCredPresCBS           E     N      1-1     2
                                                                                                                             Crédito Presumido da CBS.

                                                                                                                       1-    Percentual do Crédito Presumido
Se/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gCBS/gCBSCredPres/       pCredPresCBS           E     N      1-1
                                                                                                                      2V2    CBS.

                                                                                                                             Grupo de informações
FSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gCBS/                          gDif            G     -      0-1     -     relacionadas ao diferimento
                                                                                                                             para a CBS
                                                                                                                       1-    Percentual de diferimento da
Se/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gCBS/gDif/                     pDifCBS          E     N      1-1
                                                                                                                      3V2    CBS.
                                                                                                                             Grupo de informações
FSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gCBS/                        gDevTrib          G     -      0-1     -     relacionadas à devolução
                                                                                                                             tributária para a CBS
                                                                                                                       1-
Se/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gCBS/gDevol/                 vDevTribCBS        E     N      1-1           Devolução personalizada da CBS.
                                                                                                                      15V2

                                                                                                                             Grupo de informações da
FSe/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gCBS/                         gDeson           G     -      0-1     -
                                                                                                                             desoneração para a CBS

                                                                                                                             Código de Situação tributária de
Se/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gCBS/gDeson/                 cstCBSDeson        E     N      1-1     3
                                                                                                                             Desoneração da CBS.
                                                                                                                             Código de Classificação
Se/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gCBS/gDeson/           cClassTribCBSDeson       E     N      1-1     6     Tributária de Desoneração da
                                                                                                                             CBS.
                                                                                                                       1-    Valor da alíquota Desoneração
Se/infNFSe/DPS/infDPS/IBSCBS/valores/trib/gIBSCBS/gCBS/gDeson/             pAliqCBSDeson          E     N      1-1
                                                                                                                      2V2    da CBS.




                       2.2 Novos Grupos na NFS-e

                                Nos novos grupos que constam na NFS-e, fora dos limites da DPS, os campos aqui
                       apresentados são os que deverão ser parametrizados pelos Estados, pelos Municípios e pela
                       União ou aqueles que a própria plataforma, dadas as suas regras e cálculos, fornecerá de forma
                       automatizada a partir dos dados emitidos pelos contribuintes na DPS, listados no tópico
                       anterior.
                                De forma análoga ao novo grupo criado na DPS, aqui também foi criado o grupo IBSCBS
               (caminho NFSe/infNFSe/). Neste grupo serão dispostos todos os subgrupos e informações relativas aos novos
               tributos: IBS e CBS. Importante observar que os campos/tags listados abaixo em vermelho se referem a
               campos que foram modificados em relação à versão anterior, publicada na Nota Técnica SE/CGNFS-
               e nº 001, de 01 de agosto de 2024.



     CAMINHO NO XML                      CAMPO            ELE    TIPO   OCOR.      TAM.                               DESCRIÇÃO
                                                                                             Grupo de informações geradas pelo sistema referentes ao
FSe/infNFSe/                             IBSCBS            G      -      1-1         -
                                                                                             IBS e à CBS


                                                                                                                                          10
                             2.2.1   Grupo de Informações Comuns Relativas ao IBS e à CBS

     CAMINHO NO XML                      CAMPO          ELE   TIPO   OCOR.   TAM.                                DESCRIÇÃO
                                                                                     Grupo de informações geradas pelo sistema referentes ao
FSe/infNFSe/                             IBSCBS         G      -      1-1      -
                                                                                     IBS e à CBS
Se/infNFSe/IBSCBS/                   xLocalidadeIncid    E     C      1-1    600     Nome da localidade de incidência do IBS/CBS.

Se/infNFSe/IBSCBS/                     xCSTIBSCBS        E     C      1-1    600     Descrição do Código de Situação Tributária do IBS/CBS.

Se/infNFSe/IBSCBS/                   xClassTribIBSCBS    E     C      1-1    600     Descrição do Código de Classificação Tributária do IBS/CBS

FSe/infNFSe/IBSCBS/                     compGov         G      -      0-1      -     Grupo referente a compras governamentais.



                                                                                     Tipo do ente da compra governamental:

                                                                                     1 - União;
Se/infNFSe/IBSCBS/compGov/            tpCompraGov        E     C      1-1      1
                                                                                     2 - Estados;
                                                                                     3 - Distrito Federal;
                                                                                     4 - Municípios;



Se/infNFSe/IBSCBS/compGov/              pRedutor         E     C      1-1    1-2V2   Percentual de redução de alíquota em compra governamental.



                             2.2.2   Grupo de Informações de Valores Brutos Relativos ao IBS e à CBS

     CAMINHO NO XML                      CAMPO          ELE   TIPO   OCOR.   TAM.                                DESCRIÇÃO

FSe/infNFSe/IBSCBS/                      valores        G      -      1-1      -     Grupo de valores brutos referentes ao IBS e à CBS
                                                                              1-     Valor da base de cálculo (BC) do IBS/CBS antes das reduções para
Se/infNFSe/IBSCBS/valores/                 vBC           E     N      1-1
                                                                             15V2    cálculo do tributo bruto.
FSe/infNFSe/IBSCBS/valores/                uf                                        Grupo de Informações relativas aos valores do IBS Estadual
                                                                                     Alíquota da UF para IBS da localidade de incidência parametrizada
Se/infNFSe/IBSCBS/valores/uf/            pIBSUF          E     N      1-1    1-2V2
                                                                                     no sistema.
Se/infNFSe/IBSCBS/valores/uf/          pRedAliqUF        E     N      1-1    1-3V2   Percentual de redução de alíquota estadual.
                                                                                     pAliqEfetUF = pIBSUF x pRedAliqUF
Se/infNFSe/IBSCBS/valores/uf/          pAliqEfetUF       E     N      1-1    1-2V2
                                                                                     Se pRedAliqUF não for informado na DPS, então pAliqEfetUF é a
                                                                                     própria pIBSUF.
                                                                                     Valor do tributo bruto da Operação para IBS Estadual.
                                                                              1-
Se/infNFSe/IBSCBS/valores/uf/           vTribOpUF        E     N      1-1
                                                                             15V2
                                                                                     vTribOpUF = vBC x pIBSUF.
FSe/infNFSe/IBSCBS/valores/               mun                                        Grupo de Informações relativas aos valores do IBS Municipal
                                                                                     Alíquota da UF para IBS da localidade de incidência parametrizada
Se/infNFSe/IBSCBS/valores/mun/          pIBSMun          E     N      1-1    1-2V2
                                                                                     no sistema.
Se/infNFSe/IBSCBS/valores/mun/         pRedAliqMun       E     N      1-1    1-3V2   Percentual de redução de alíquota estadual.
                                                                                     pAliqEfetMun = pIBSMun x pRedAliqMun
Se/infNFSe/IBSCBS/valores/mun/         pAliqEfetMun      E     N      1-1    1-2V2
                                                                                     Se pRedAliqMun não for informado na DPS, então pAliqEfetMun é a
                                                                                     própria pIBSMun.
                                                                                     Valor do tributo bruto da Operação para o IBS Municipal.
                                                                              1-
Se/infNFSe/IBSCBS/valores/mun/         vTribOpMun        E     N      1-1
                                                                             15V2
                                                                                     vTribOpMun = vBC x pIBSMun.
FSe/infNFSe/IBSCBS/valores/                fed                                       Grupo de Informações relativas aos valores da CBS
                                                                                     Alíquota da UF para IBS da localidade de incidência parametrizada
Se/infNFSe/IBSCBS/valores/fed/            pCBS           E     N      1-1    1-2V2
                                                                                     no sistema.
Se/infNFSe/IBSCBS/valores/fed/         pRedAliqCBS       E     N      1-1    1-3V2   Percentual da redução de alíquota.
                                                                                     pAliqEfetCBS = pCBS x pRedAliqCBS
Se/infNFSe/IBSCBS/valores/fed/         pAliqEfetCBS      E     N      1-1    1-2V2
                                                                                     Se pRedAliqCBS não for informado na DPS, então pAliqEfetCBS é a
                                                                                     própria pCBS.
                                                                                     Valor do tributo bruto da Operação para a CBS.
                                                                              1-
Se/infNFSe/IBSCBS/valores/fed/         vTribOpCBS        E     N      1-1
                                                                             15V2
                                                                                     vTribOpCBS = vBC x pCBS.




                                                                                                                                      11
                             2.2.3   Grupos Totalizadores

     CAMINHO NO XML                     CAMPO                   ELE      TIPO     OCOR.    TAM.                                   DESCRIÇÃO

FSe/infNFSe/IBSCBS/                     totCIBS                 G         -        1-1       -       Grupo de Totalizadores
                                                                                                     Valor Total da NF considerando os impostos por fora IBS e CBS.
                                                                                                     O IBS e a CBS são por fora, por isso seus valores devem ser
                                                                                             1-
Se/infNFSe/IBSCBS/totCIBS/              vTotNF                   E        N        1-1               adicionados ao valor total da NF.
                                                                                            15V2
                                                                                                     vTotNF = VLiq + vCBS + vIBSTot




                                  2.2.3.1 Grupo de Informações Relativas às Totalizações do IBS

           CAMINHO NO XML                             CAMPO                ELE    TIPO     OCOR.      TAM.                               DESCRIÇÃO

FSe/infNFSe/IBSCBS/totCIBS/                            gIBS                   G     -       1-1         -    Grupo de totalizadores referentes ao IBS
                                                                                                             Valor do Crédito Presumido.
                                                                                                       1-
Se/infNFSe/IBSCBS/totCIBS/gIBS/                     vCredPresIBS              E     N       1-1
                                                                                                      15V2
                                                                                                             vCredPresIBS = vBC x pCredPresIBS

FSe/infNFSe/IBSCBS/totCIBS/gIBS/                    gIBSUFTot                 G     -       1-1         -    Grupo de valores referentes ao IBS Estadual

                                                                                                             Total do Diferimento estadual.
                                                                                                       1-
Se/infNFSe/IBSCBS/totCIBS/gIBS/gIBSUFTot/              vDifUF                 E     N       1-1
                                                                                                      15V2
                                                                                                             vDifUF = (vBC x pDifUF)
                                                                                                             Total valor desonerado estadual.
                                                                                                       1-
Se/infNFSe/IBSCBS/totCIBS/gIBS/gIBSUFTot/             vDesonUF                E     N       1-1
                                                                                                      15V2
                                                                                                             vDesonUF = (vBC x pAliqUF) - (vBC x pAliqEfetUF)
                                                                                                             Total valor do IBS estadual.
                                                                                                       1-
Se/infNFSe/IBSCBS/totCIBS/gIBS/gIBSUFTot/              vIBSUF                 E     N       1-1
                                                                                                      15V2   vIBSUF = vBC x (pIBSUF ou pAliqEfetUF) - (vCredPresIBS
                                                                                                             + vDifUF + vDevTribUF)

FSe/infNFSe/IBSCBS/totCIBS/gIBS/                    gIBSMunTot                G     -       1-1         -    Grupo de valores referentes ao IBS Municipal

                                                                                                             Total do Diferimento municipal.
                                                                                                       1-
Se/infNFSe/IBSCBS/totCIBS/gIBS/gIBSMunTot/            vDifMun                 E     N       1-1
                                                                                                      15V2
                                                                                                             vDifMun = (vBC x pDifMun)
                                                                                                             Total valor desonerado municipal.
                                                                                                       1-
Se/infNFSe/IBSCBS/totCIBS/gIBS/gIBSMunTot/          vDesonMun                 E     N       1-1
                                                                                                      15V2
                                                                                                             vDesonMun = (vBC x pAliqMun) - (vBC x pAliqEfetMun)
                                                                                                             Total valor do IBS municipal.
                                                                                                       1-
Se/infNFSe/IBSCBS/totCIBS/gIBS/gIBSMunTot/            vIBSMun                 E     N       1-1
                                                                                                      15V2   vIBSMun = vBC x (pIBSMun ou pAliqEfetMun) -
                                                                                                             (vCredPresIBS + vDifMun + vDevTribMun)
                                                                                                             Total Valor do IBS Total.
                                                                                                       1-
Se/infNFSe/IBSCBS/totCIBSSel/gIBS/                    vIBSTot                 E     N       1-1
                                                                                                      15V2
                                                                                                             vIBSTot = vIBSUF + vIBSMun




                                  2.2.3.2 Grupo de Informações Relativas às Totalizações da CBS

      CAMINHO NO XML                        CAMPO                ELE      TIPO     OCOR.     TAM.                                 DESCRIÇÃO

FSe/infNFSe/IBSCBS/totCIBSSel/               gCBS                    G        -     1-1          -    Grupo de totalizadores referentes à CBS

                                                                                              1-      Valor do Crédito Presumido da CBS.
Se/infNFSe/IBSCBS/totCIBS/gCBS/        vCredPresCBS                  E        N     1-1
                                                                                             15V2
                                                                                                      vCredPresCBS = vBC x pCredPresCBS
                                                                                                      Total do Diferimento CBS.
                                                                                              1-
Se/infNFSe/IBSCBS/totCIBS/gCBS/             vDifCBS                  E        N     1-1
                                                                                             15V2
                                                                                                      vDifCBS = (vBC x pDifCBS)
                                                                                                      Total do valor desonerado CBS.
                                                                                              1-
Se/infNFSe/IBSCBS/totCIBS/gCBS/         vDesonCBS                    E        N     1-1
                                                                                             15V2
                                                                                                      vDesonCBS = (vBC x pAliqCBS) - (vBC x pAliqEfetCBS)
                                                                                                      Total valor da CBS da União.
                                                                                              1-
Se/infNFSe/IBSCBS/totCIBS/gCBS/              vCBS                    E        N     1-1
                                                                                             15V2     vCBS = vBC x (pCBS ou pAliqEfetCBS) - (vCredPresCBS + vDifCBS
                                                                                                      vDevTribCBS)




                                                                                                                                                     12
                                  SAMUEL KRUGER
                     Auditor-Fiscal da Receita Federal do Brasil
Secretário-Executivo da Secretaria-Executiva do Comitê Gestor da Nota Fiscal de Serviço
                       Eletrônica de Padrão Nacional (SE/CGNFS-e)




                                                                                          13
