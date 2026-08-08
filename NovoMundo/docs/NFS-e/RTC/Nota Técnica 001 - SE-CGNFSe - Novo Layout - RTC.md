   Projeto Reforma Tributária do Consumo –
              Adequações NFS-e
                 Nota Técnica Nº 001 – Versão 1.0




                            01 de agosto de 2024



Secretaria-Executiva do Comitê Gestor da Nota Fiscal de Serviço Eletrônica
                    de Padrão Nacional (SE/CGNFS-e)
                                                                         Sumário

1.     Introdução ................................................................................................................................................. 3
2.     Layout NFS-e: Novos Grupos no Contexto da EC nº 132/2023.................................................................. 4
     2.1      Novos Grupos na DPS da NFS-e ......................................................................................................... 6
       2.1.1          Grupo de Informações Relativas ao Destinatário ...................................................................... 6
       2.1.2          Grupo de Informações Relativas ao Adquirente ........................................................................ 7
       2.1.3          Grupo de Informações Relativas ao Serviço Prestado para IBS, CBS e IS .................................. 7
       2.1.4          Grupo de Informações Relativas aos Valores do Serviço Prestado para IBS, CBS e IS ............... 8
       2.1.4.1        Grupo de Informações Específicas Relativas ao IBS Estadual .................................................... 8
       2.1.4.2        Grupo de Informações Específicas Relativas ao IBS Municipal .................................................. 8
       2.1.4.3        Grupo de Informações Específicas Relativas à CBS.................................................................... 9
     2.2      Novos Grupos na NFS-e ..................................................................................................................... 9
       2.2.1          Grupo de Informações Comuns Relativas ao IBS e à CBS .......................................................... 9
       2.2.2          Grupo de Informações de Valores Brutos Relativos ao IBS e à CBS ......................................... 10
       2.2.3          Grupos Totalizadores ............................................................................................................... 10
       2.2.3.1        Grupo de Informações Relativas às Totalizações do Imposto Seletivo .................................... 10
       2.2.3.2        Grupo de Informações Relativas às Totalizações do IBS .......................................................... 11
       2.2.3.3        Grupo de Informações Relativas às Totalizações da CBS ......................................................... 11




                                                                                                                                                                   2
   1. Introdução

       Este documento contempla a primeira versão dos novos agrupamentos e campos opcionais do
layout da Nota Fiscal de Serviço eletrônica – NFS-e padrão nacional relacionados à tributação do
Imposto sobre Bens e Serviços – IBS, da Contribuição sobre Bens e Serviços – CBS e do Imposto
Seletivo – IS incidentes nas operações de serviços, em atendimento às alterações previstas na Reforma
Tributária do Consumo, conforme a Emenda Constitucional – EC nº 132, de 20 de dezembro de 2023.

       Os novos agrupamentos de campos foram inseridos a partir do layout atual da NFS-e, presente
no documento “AnexoIV-LeiautesRN_ADN-SNNFSe_V1.00.02-Produção.xlsx”, aba “LEIAUTE
NFS-e ADN” que consta na sessão de documentação técnica no Portal da NFS-e:
https://www.gov.br/nfse/pt-br/biblioteca/documentacao-tecnica.

       Importante esclarecer que o conjunto de campos apresentados neste documento é uma
primeira versão, resultado de estudos técnicos realizados tomando como base o texto do Projeto de
Lei Complementar – PLP nº 68, de 25 de abril de 2024, ainda em tramitação no Congresso Nacional
e sua divulgação objetiva dar transparência aos Municípios, empresas prestadoras de serviço e de
Tecnologia da Informação – TI e contribuintes para que possam se familiarizar com o novo padrão
que deverá vigorar a partir de janeiro de 2026. Os estudos técnicos permanecem e novas versões
deverão ser publicadas nos próximos meses com atualizações do layout proposto.

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

              Na    DPS,     foi     criado    o     grupo     IBSCBSSEL        (caminho
      NFSe/infNFSe/DPS/infDPS/). Neste grupo serão dispostos todos os subgrupos e
      informações relativas aos novos tributos: IBS, CBS e Imposto Seletivo; e que deverão
      ser informados pelo contribuinte na emissão.




   2.1.1   Grupo de Informações Relativas ao Destinatário




                                                                                        6
2.1.2   Grupo de Informações Relativas ao Adquirente




2.1.3   Grupo de Informações Relativas ao Serviço Prestado para IBS, CBS e IS




                                                                                7
2.1.4   Grupo de Informações Relativas aos Valores do Serviço Prestado para IBS,
        CBS e IS




   2.1.4.1 Grupo de Informações Específicas Relativas ao IBS Estadual




   2.1.4.2 Grupo de Informações Específicas Relativas ao IBS Municipal




                                                                              8
        2.1.4.3 Grupo de Informações Específicas Relativas à CBS




2.2 Novos Grupos na NFS-e

       Nos novos grupos que constam na NFS-e, fora dos limites da DPS, os campos aqui
apresentados são os que deverão ser parametrizados pelos Estados, pelos Municípios e pela
União ou aqueles que a própria plataforma, dadas as suas regras e cálculos, fornecerá de forma
automatizada a partir dos dados emitidos pelos contribuintes na DPS, listados no tópico
anterior.
       De forma análoga ao novo grupo criado na DPS, aqui também foi criado o grupo
IBSCBSSEL (caminho NFSe/infNFSe/). Neste grupo serão dispostos todos os subgrupos e
informações relativas aos novos tributos: IBS, CBS e Imposto Seletivo.




    2.2.1   Grupo de Informações Comuns Relativas ao IBS e à CBS




                                                                                            9
2.2.2   Grupo de Informações de Valores Brutos Relativos ao IBS e à CBS




2.2.3   Grupos Totalizadores




   2.2.3.1 Grupo de Informações Relativas às Totalizações do Imposto Seletivo




                                                                                10
       2.2.3.2 Grupo de Informações Relativas às Totalizações do IBS




       2.2.3.3 Grupo de Informações Relativas às Totalizações da CBS




                                  SAMUEL KRUGER
                     Auditor-Fiscal da Receita Federal do Brasil
Secretário-Executivo da Secretaria-Executiva do Comitê Gestor da Nota Fiscal de Serviço
                       Eletrônica de Padrão Nacional (SE/CGNFS-e)




                                                                                          11
