   Projeto Reforma Tributária do Consumo –
              Adequações NFS-e
                 Nota Técnica Nº 004 – Versão 1.1




                            19 de agosto de 2025




Secretaria-Executiva do Comitê Gestor da Nota Fiscal de Serviço Eletrônica
                    de Padrão Nacional (SE/CGNFS-e)
                                                                         Sumário

1.     Introdução ................................................................................................................................................. 3
2.     Layout NFS-e: Novos Grupos no Contexto da EC nº 132/2023.................................................................. 4
     2.1       Novos Grupos na DPS da NFS-e........................................................................................................ 6
       2.1.1        Grupo de Informações Relativas ao Destinatário ........................................................................ 7
       2.1.2        (REMOVIDO)Grupo de Informações Relativas ao Adquirente ................................................... 8
       2.1.2        Grupo de Informações de Operações Relacionadas a Bens Imóveis, Exceto Obras .................... 9
       2.1.3        Grupo de Informações Relativas ao Serviço Prestado para IBS e CBS ....................................... 10
     2.2       Novos Grupos na NFS-e .................................................................................................................. 12
       2.2.1        Grupo de Informações Comuns Relativas ao IBS e à CBS .......................................................... 12
       2.2.2        Grupo de Informações de Valores Brutos Relativos ao IBS e à CBS ........................................... 12
       2.2.3        Grupos Totalizadores ................................................................................................................. 13
       2.2.3.1          Grupo de Informações Relativas às Totalizações do IBS ........................................................ 14
       2.2.3.2          Grupo de Informações Relativas às Totalizações da CBS ....................................................... 15




                                                                                                                                                                   2
   1. Introdução

       Este documento contempla a quarta versão dos novos agrupamentos e campos opcionais do
layout da Nota Fiscal de Serviço eletrônica – NFS-e padrão nacional relacionados à tributação do
Imposto sobre Bens e Serviços – IBS e da Contribuição sobre Bens e Serviços – CBS incidentes nas
operações de serviços, em atendimento às alterações previstas na Emenda Constitucional nº 132 de,
20 de dezembro de 2023, que deu ensejo à Reforma Tributária do Consumo – RTC. Importante
observar que esta nota técnica substitui as versões anteriores.

       Os novos agrupamentos de campos foram inseridos a partir do layout atual da NFS-e, presente
no documento “AnexoIV-LeiautesRN_ADN-SNNFSe_V1.00.02-Produção.xlsx”, aba “LEIAUTE
NFS-e ADN” que consta na sessão de documentação técnica no Portal da NFS-e:
https://www.gov.br/nfse/pt-br/biblioteca/documentacao-tecnica.


       Importante esclarecer que o conjunto de campos apresentados neste documento é uma quarta
versão, resultado de estudos técnicos realizados tomando como base o texto da Lei Complementar –
LC nº 214, de 16 de janeiro de 2025, e sua divulgação objetiva dar transparência aos Estados, aos
Municípios, às empresas prestadoras de serviço e de Tecnologia da Informação – TI e contribuintes
para que possam se familiarizar com o novo padrão que deverá vigorar a partir de janeiro de 2026.
Os estudos técnicos permanecem e novas versões deverão ser publicadas nas próximas semanas com
atualizações do layout proposto.

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
       Junto a esta nota técnica, foram também publicados dois anexos na seção de documentação
técnica do portal da NFS-e (https://www.gov.br/nfse/pt-br/biblioteca/documentacao-tecnica):



       •   AnexoVI-LeiautesRN_RTC_IBSCBS-V1.01.01.xlsx
             Composto por três tabelas, este anexo possui o layout da NFS-e com os novos grupos
             referentes ao IBS e à CBS (“Leiaute DPS_NFS-e - RT”) e as primeiras regras de
             negócio do grupo “IBSCBS” da DPS (“RN DPS - RTC_IBSCBS”) e da NFS-e (“RN
             NFS-e - RTC_IBSCBS”);

       •   AnexoVII-IndOp_IBSCBS_V1.00.00-.xlsx
             Composto por uma tabela com os códigos indicadores da operação que serão
             referenciados no campo “cIndOp” da DPS. A tabela foi baseada no art. 11 da Lei
             Complementar – LC Nº 214, de 16 de janeiro de 2025.

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

                                                                                                4
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


                                                                                      5
                                  Por exemplo:
                                  o 14: Campo de tamanho fixo com 14 caracteres ou números;
                                  o 1-150: Campo de tamanho variável, podendo possuir de 1 a 150 caracteres ou
                                     números;
                                  o 1-3V2: Campo de tamanho variável, podendo possuir de 1 a 3 números mais duas
                                     casas decimais.


                                  Nas tags G ou CG (Group ou Choice Group), como são apenas informações de
                                  agrupamento de campos, não há um tamanho relacionado.


                           •      DESCRIÇÃO
                                  Descrição sucinta do que representa cada campo e como deve ser preenchido.



                      2.1 Novos Grupos na DPS da NFS-e

                      Na DPS, foi criado o grupo IBSCBS (caminho NFSe/infNFSe/DPS/infDPS/). Neste grupo
             serão dispostos todos os subgrupos e informações relativas aos novos tributos: IBS e CBS; e que
             deverão ser informados pelo contribuinte na emissão. Importante observar que os campos/tags/células
             listados abaixo em laranja se referem a campos que foram modificados em relação à versão anterior,
             publicada na Nota Técnica SE/CGNFS-e nº 003, de 04 de julho de 2025.


                                                CAMPO       ELE   TIPO   OCOR.   TAM.                            DESCRIÇÃO
           CAMINHO NO XML
                                                                                             Grupo de informações declaradas pelo emitente
NFSe/infNFSe/DPS/infDPS/                        IBSCBS      G      -      1-1     -
                                                                                                        referentes ao IBS e à CBS
                                                                                              Indicador da finalidade da emissão de NFS-e

NFSe/infNFSe/DPS/infDPS/IBSCBS/                  finNFSe     E     N      1-1     1                         0 = NFS-e regular
                                                                                                          1 = NFS-e de crédito
                                                                                                           2 = NFS-e de débito
                                                                                              Indica operação de uso ou consumo pessoal.
NFSe/infNFSe/DPS/infDPS/IBSCBS/                  indFinal    E     N      1-1     1
                                                                                                                    0=Não
                                                                                                                     1=Sim
                                                                                        Código indicador da operação de fornecimento, conforme tabela
NFSe/infNFSe/DPS/infDPS/IBSCBS/                  cIndOp      E     N      1-1     6
                                                                                                        “código indicador de operação”
                                                                                          Tipo de Operação com Entes Governanementais ou outros
                                                                                                         serviços sobre bens imóveis:

                                                                                                 1 – Fornecimento com pagamento posterior;
NFSe/infNFSe/DPS/infDPS/IBSCBS/                  tpOper      E     N      0-1     1
                                                                                        2 - Recebimento do pagamento com fornecimento já realizado;
                                                                                                3 – Fornecimento com pagamento já realizado;
                                                                                         4 – Recebimento do pagamento com fornecimento posterior;
                                                                                        5 – Fornecimento e recebimento do pagamento concomitantes.
NFSe/infNFSe/DPS/infDPS/IBSCBS/              gRefNFSe       G      -      0-1     -                     Grupo de NFS-e referenciadas.

NFSe/infNFSe/DPS/infDPS/IBSCBS/gRefNFSe/        refNFSe      E     C     1-99     50                    Chave da NFS-e referenciada.
                                                                                                         Tipo de ente governamental
NFSe/infNFSe/DPS/infDPS/IBSCBS/              tpEnteGov       E     N      0-1     1
                                                                                             Para administração pública direta e suas autarquias e
                                                                                                                 fundações:


                                                                                                                                      6
                                                                                                                           1 = União
                                                                                                                          2 = Estado
                                                                                                                      3 = Distrito Federal
                                                                                                                         4 = Município
                                                                                                                           9 = Outro
                                                                                                Se tpEnteGov informado for igual a 9 = outro, este campo deve
NFSe/infNFSe/DPS/infDPS/IBSCBS/              xTpEnteGov            E   C         0-1    100     informar qual a descrição do ente. Exemplo: "Comitê Gestor do
                                                                                                                             IBS".
                                                                                                           A respeito do Destinatário dos serviços:

                                                                                                0 – o destinatário é o próprio tomador/adquirente identificado
                                                                                                      na NFS-e (tomador=adquirente=destinatário);
                                                                                                  1 – o destinatário não é o próprio adquirente, podendo ser
                                                                                                    outra pessoa, física ou jurídica (ou equiparada), ou um
                                                                                                     estabelecimento diferente do indicado como tomador
                                                                                                           (tomador=adquirente≠destinatário);
                                              indPessoas
NFSe/infNFSe/DPS/infDPS/IBSCBS/                                    E   N         1-1     1
                                                indDest
                                                                                                  2 - O adquirente e o destinatário são a mesma pessoa, mas
                                                                                                  diferente do tomador de serviços; (tomador ≠ adquirente =
                                                                                                                         destinatário)
                                                                                                 3 - O tomador e o destinatário são a mesma pessoa, diferente
                                                                                                     do adquirente; (tomador = destinatário ≠ adquirente)
                                                                                                    4 - O tomador, o adquirente e o destinatário são pessoas
                                                                                                distintas que devem ser identificadas. (tomador ≠ adquirente ≠
                                                                                                                         destinatário)




                     Algumas observações importantes acerca dos campos acima:
                          1. O campo “cIndOp” se refere à tabela de indicador da operação publicada no ANEXO
                              AnexoVII-IndOp_IBSCBS_V1.00.00-.xlsx;
                          2. As notas de ajuste, de crédito e de débito, estão em processo de estudos e atualizações
                              serão publicadas nas próximas notas técnicas. Portanto, a menção a essas notas de ajuste
                              na descrição do campo “finNFSe” tem, por enquanto, o único objetivo de esclarecer a
                              necessidade da criação desse campo.


                        2.1.1 Grupo de Informações Relativas ao Destinatário

                                                           CAMPO           ELE   TIPO   OCOR.     TAM.                      DESCRIÇÃO
                 CAMINHO NO XML
 NFSe/infNFSe/DPS/infDPS/IBSCBS/                            dest           G       -     0-1        -    Grupo de informações relativas ao Destinatário

                                                                                                            Número da inscrição no Cadastro Nacional de
 NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                       CNPJ           CE      N     1-1       14
                                                                                                          Pessoa Jurídica (CNPJ) do destinatário de serviço
                                                                                                            Número da inscrição no Cadastro Nacional de
 NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                       CPF            CE      N     1-1       11
                                                                                                            Pessoa Física (CPF) do destinatário do serviço
                                                                                                          Número de identificação fiscal fornecido por órgão
 NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                        NIF           CE      C     1-1       40
                                                                                                               de administração tributária no exterior
                                                                                                                 Motivo para não informação do NIF:

 NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                      cNaoNIF         CE      N     1-1        1            0 - Não informado na nota de origem;
                                                                                                                        1 - Dispensado do NIF;
                                                                                                                       2 - Não exigência do NIF;

 NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                      xNome            E      C     1-1       150        Nome / Nome Empresarial do destinatário

                                                                                                          Grupo de informações do endereço do destinatário
 NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                       end            G       -     0-1        -
                                                                                                                            do serviço.

 NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/                  endNac          CG      -     1-1        -        Grupo de informações do endereço nacional.
                                                                                                          Código do município do endereço do destinatário do
 NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/endNac/            cMun            E      N     1-1        7                          serviço.
                                                                                                                           (Tabela do IBGE)
                                                                                                          Código numérico do Endereçamento Postal nacional
 NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/endNac/            CEP             E      C     1-1        8                           (CEP)
                                                                                                                do endereço do destinatário do serviço.


                                                                                                                                             7
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/            endExt      CG      -     1-1     -        Grupo de informações do endereço no exterior.
                                                                                               Código do país do endereço do destinatário do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/endExt/      cPais       E     C      1-1     2                           serviço.
                                                                                                           (Tabela de Países ISO)
                                                                                             Código alfanumérico do Endereçamento Postal no
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/endExt/    cEndPost      E     C      1-1    1-11
                                                                                                    exterior do destinatário do serviço.
                                                                                               Nome da cidade no exterior do destinatário do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/endExt/    xCidade       E     C      1-1    1-60
                                                                                                                  serviço.
                                                                                            Estado, província ou região da cidade no exterior do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/endExt/   xEstProvReg    E     C      1-1    1-60
                                                                                                          destinatário do serviço.
                                                                                      1-        Tipo e nome do logradouro do endereço do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/             xLgr        E     C      1-1
                                                                                     255                  destinatário do serviço.
                                                                                            Número no logradouro do endereço do destinatário
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/              nro        E     C      1-1    1-60
                                                                                                                 do serviço.
                                                                                      1-       Complemento do endereço do destinatário do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/             xCpl        E     C      0-1
                                                                                     156                          serviço.

NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/end/            xBairro      E     C      1-1    1-60      Bairro do endereço do destinatário do serviço.
                                                                                                      Número do telefone do destinatário.
                                                                                                  (Preencher com o Código DDD + número do
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                 fone        E     N      0-1    6-20     telefone. Nas operações com exterior é permitido
                                                                                             informar o código do país + código da localidade +
                                                                                                             número do telefone)
NFSe/infNFSe/DPS/infDPS/IBSCBS/dest/                 email       E     C      0-1    1-80                  E-mail do destinatário.




                       2.1.2 (REMOVIDO) Grupo de Informações Relativas ao Adquirente

                     Para o padrão da NFS-e nacional, o Tomador de Serviços (conceito da Lei Complementar nº
                 116, de 31 de julho de 2003) e o Adquirente (conceito da Lei Complementar nº 214, de 16 de
                 janeiro de 2025) são identificados como a mesma pessoa, pois é quem:

                     a) se obriga ao pagamento ou outra forma de contraprestação pelo fornecimento; ou;

                     b) nos casos de pagamento ou de qualquer outra forma de contraprestação por conta e ordem
                     ou em nome de terceiros, aquele por conta de quem ou em nome de quem decorre a obrigação
                     de pagamento ou de qualquer outra forma de contraprestação pelo fornecimento do bem ou
                     do serviço.

                    Dessa forma, o grupo “adq” em “NFSe/infNFSe/DPS/infDPS/IBSCBS/” foi suprimido do
                 layout nesta Nota Técnica, uma vez que o Adquirente já está sendo identificado no grupo “toma”
                 em “NFSe/infNFSe/DPS/infDPS/”.
                    O Tomador/Adquirente pode ser também o próprio destinatário. O Destinatário é a pessoa a
                 quem é fornecido o bem ou o serviço.

                                                   CAMPO        ELE   TIPO   OCOR.   TAM.                      DESCRIÇÃO
                CAMINHO NO XML
NFSe/infNFSe/DPS/infDPS/IBSCBS/                      adq        G      -      0-1     -     Grupo de informações relativas ao Adquirente

                                                                                            Número da inscrição no Cadastro Nacional de Pessoa
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                 CNPJ        CE     N      1-1     14
                                                                                                 Jurídica (CNPJ) do adquirente de serviço
                                                                                             Número da inscrição no Cadastro de Pessoa Física
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                  CPF        CE     N      1-1     11
                                                                                                      (CPF) do adquirente do serviço
                                                                                            Número de identificação fiscal fornecido por órgão de
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                  NIF        CE     C      1-1     40
                                                                                                   administração tributária no exterior
                                                                                                    Motivo para não informação do NIF:

NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                cNaoNIF      CE     N      1-1     1            0 - Não informado na nota de origem;
                                                                                                          1 - Dispensado do NIF;
                                                                                                         2 - Não exigência do NIF;
                                                                                              Número do Cadastro de Atividade Econômica da
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                 CAEPF        E     N      0-1     14
                                                                                              Pessoa Física (CAEPF) do adquirente do serviço.


                                                                                                                                 8
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                   xNome         E        C       1-1     150           Nome / Nome Empresarial do adquirente

                                                                                                      Grupo de informações do endereço do adquirente do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                    end          G        -       0-1      -
                                                                                                                           serviço.

NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/               endNac        CG       -       1-1      -           Grupo de informações do endereço nacional.
                                                                                                      Código do município do endereço do adquirente do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/endNac/         cMun         E        N       1-1      7                            serviço.
                                                                                                                       (Tabela do IBGE)
                                                                                                      Código numérico do Endereçamento Postal nacional
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/endNac/         CEP          E        C       1-1      8                             (CEP)
                                                                                                             do endereço do adquirente do serviço.
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/               endExt        CG       -       1-1      -         Grupo de informações do endereço no exterior.
                                                                                                         Código do país do endereço do adquirente do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/endExt/         cPais        E        C       1-1      2                             serviço.
                                                                                                                     (Tabela de Países ISO)
                                                                                                       Código alfanumérico do Endereçamento Postal no
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/endExt/       cEndPost       E        C       1-1     1-11
                                                                                                               exterior do adquirente do serviço.
                                                                                                         Nome da cidade no exterior do adquirente do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/endExt/       xCidade        E        C       1-1     1-60
                                                                                                                            serviço.
                                                                                                      Estado, província ou região da cidade no exterior do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/endExt/      xEstProvReg     E        C       1-1     1-60
                                                                                                                     adquirente do serviço.
                                                                                              1-          Tipo e nome do logradouro do endereço do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/                xLgr         E        C       1-1
                                                                                             255                     adquirente do serviço.
                                                                                                      Número no logradouro do endereço do adquirente do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/                 nro         E        C       1-1     1-60
                                                                                                                            serviço.
                                                                                              1-
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/                xCpl         E        C       0-1              Complemento do endereço do adquirente do serviço.
                                                                                             156

NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/end/               xBairro       E        C       1-1     1-60        Bairro do endereço do adquirente do serviço.
                                                                                                          Número do telefone do adquirente do serviço.
                                                                                                           (Preencher com o Código DDD + número do
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                    fone         E        N       0-1     6-20       telefone. Nas operações com exterior é permitido
                                                                                                       informar o código do país + código da localidade +
                                                                                                                       número do telefone)
NFSe/infNFSe/DPS/infDPS/IBSCBS/adq/                    email        E        C       0-1     1-80                    E-mail do adquirente.




                       2.1.2 Grupo de Informações de Operações Relacionadas a Bens Imóveis, Exceto
                             Obras

                                                       CAMPO         ELE     TIPO    OCOR.    TAM.                        DESCRIÇÃO
                CAMINHO NO XML
                                                                                                             Grupo de informações de operações
NFSe/infNFSe/DPS/infDPS/IBSCBS/                        imovel           G        -    0-1         -
                                                                                                         relacionadas a bens imóveis, exceto obras.
                                                                                                                     Inscrição imobiliária fiscal
                                                                                                              (código fornecido pela prefeitura para a
NFSe/infNFSe/DPS/infDPS/IBSCBS/imovel/               inscImobFisc       E        C    0-1     1-30
                                                                                                        identificação da obra ou para fins de recolhimento
                                                                                                                              do IPTU)

NFSe/infNFSe/DPS/infDPS/IBSCBS/imovel/                   cCIB           CE       C    1-1         8       Código do Cadastro Imobiliário Brasileiro - CIB

NFSe/infNFSe/DPS/infDPS/IBSCBS/imovel/                   end         CG          -    1-1         -       Grupo de informações do endereço do imóvel.

                                                                                                          Código de Endereçamento Postal numérico do
NFSe/infNFSe/DPS/infDPS/IBSCBS/imovel/end/               CEP            CE       C    1-1         8
                                                                                                                  endereço nacional do imóvel.
                                                                                                        Grupo de informações descritivas do endereço do
NFSe/infNFSe/DPS/infDPS/IBSCBS/imovel/end/              endExt       CG          -    1-1         -
                                                                                                                       imóvel no exterior.
                                                                                                        Código de Endereçamento Postal alfanumérico do
NFSe/infNFSe/DPS/infDPS/IBSCBS/imovel/end/endExt/      cEndPost         E        C    1-1     1-11
                                                                                                                endereço do imóvel no exterior.

NFSe/infNFSe/DPS/infDPS/IBSCBS/imovel/end/endExt/      xCidade          E        C    1-1     1-60         Nome da cidade no exterior, local do imóvel.

                                                                                                        Estado, província ou região da cidade no exterior,
NFSe/infNFSe/DPS/infDPS/IBSCBS/imovel/end/endExt/    xEstProvReg        E        C    1-1     1-60
                                                                                                                         local do imóvel.
                                                                                               1-
NFSe/infNFSe/DPS/infDPS/IBSCBS/imovel/end/               xLgr           E        C    1-1              Tipo e nome do logradouro do endereço do imóvel.
                                                                                              255

NFSe/infNFSe/DPS/infDPS/IBSCBS/imovel/end/               nro            E        C    1-1     1-60        Número no logradouro do endereço do imóvel.

                                                                                               1-
NFSe/infNFSe/DPS/infDPS/IBSCBS/imovel/end/               xCpl           E        C    0-1                     Complemento do endereço do imóvel.
                                                                                              156



                                                                                                                                           9
NFSe/infNFSe/DPS/infDPS/IBSCBS/imovel/end/               xBairro        E    C      1-1     1-60               Bairro do endereço do imóvel.




                    Observação importante acerca dos campos acima:
                        1. O grupo “obra” em “NFSe/infNFSe/DPS/infDPS/serv” deverá ser utilizado quando se
                               tratar de um serviço de obra.

                       2.1.3 Grupo de Informações Relativas ao Serviço Prestado para IBS e CBS



                                                                                   OCO
                                                     CAMPO         ELE      TIPO          TAM.                        DESCRIÇÃO
    CAMINHO NO XML                                                                  R.
NFSe/infNFSe/DPS/infDPS/                                                                            Grupo de informações relativas aos valores do
                                                     valores       G         -     1-1     -
IBSCBS/                                                                                                      serviço prestado para IBS e CBS
                                                                                                        Grupo de informações relativas a valores
                                                                                                       incluídos neste documento e recebidos por
NFSe/infNFSe/DPS/infDPS/                                                                           motivo de estarem relacionadas a operações de
                                                   gReeRepRes      G         -     0-1     -
IBSCBS/valores/                                                                                        terceiros, objeto de reembolso, repasse ou
                                                                                                     ressarcimento pelo recebedor, já tributados e
                                                                                                                    aqui referenciados
                                                                                                     Grupo relativo aos documentos referenciados
NFSe/infNFSe/DPS/infDPS/                                                            1-
                                                                                                            nos casos de reembolso, repasse e
IBSCBS/valores/gReeRepRe                           documentos      G         -     100     -
                                                                                                    ressarcimento que serão considerados na base
s/                                                                                  0
                                                                                                          de cálculo do ISSQN, do IBS e da CBS.
NFSe/infNFSe/DPS/infDPS/                                                                              Grupo de informações de documentos fiscais
IBSCBS/valores/gReeRepRe                           dFeNacional     CG        -     1-1     -          eletrônicos que se encontram no repositório
s/documentos/                                                                                                            nacional.
                                                                                                   Documento fiscal a que se refere a chaveDfe que seja
                                                                                                        um dos documentos do Repositório Nacional:
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                                                                         1 = NFS-e
BS/valores/gReeRepRes/docume                       tipoChaveDFe     E        N     1-1     1
                                                                                                                          2 = NF-e
ntos/dFeNacional/
                                                                                                                          3 = CT-e
                                                                                                                          9 = Outro
                                                                                                     Descrição da DF-e a que se refere a chaveDfe que
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                                                      seja um dos documentos do Repositório Nacional.
BS/valores/gReeRepRes/docume                      xTipoChaveDFe     E        C     0-1    255
                                                                                                   Deve ser preenchido apenas quando tipoChaveDFe =
ntos/dFeNacional/
                                                                                                                         9 (Outro).
NFSe/infNFSe/DPS/infDPS/IBSC                                                                       Chave do Documento Fiscal eletrônico do repositório
BS/valores/gReeRepRes/docume                         chaveDFe       E        C     1-1    1-50      nacional referenciado para os casos de operações já
ntos/dFeNacional/                                                                                                        tributadas.
NFSe/infNFSe/DPS/infDPS/                                                                              Grupo de informações de documento fiscais,
IBSCBS/valores/gReeRepRe                          docFiscalOutro   CG        -     1-1     -         eletrônicos ou não, que não se encontram no
s/documentos/                                                                                                      repositório nacional.
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                                                   Código do município emissor do documento fiscal que
BS/valores/gReeRepRes/docume                       cMunDocFiscal    E        N     1-1     7
                                                                                                          não se encontra no repositório nacional
ntos/docFiscalOutro/
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                                           1-      Número do documento fiscal que não se encontra no
BS/valores/gReeRepRes/docume                        nDocFiscal      E        C     1-1
                                                                                          255                    repositório nacional
ntos/docFiscalOutro/
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                                           1-
BS/valores/gReeRepRes/docume                        xDocFiscal      E        C     1-1                        Descrição do documento fiscal
                                                                                          255
ntos/docFiscalOutro/
NFSe/infNFSe/DPS/infDPS/
IBSCBS/valores/gReeRepRe                            docOutro       CG        -     1-1     -       Grupo de informações de documento não fiscal.
s/documentos/
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                                           1-
BS/valores/gReeRepRes/docume                           nDoc         E        C     1-1                      Número do documento não fiscal.
                                                                                          255
ntos/docOutro/
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                                           1-
BS/valores/gReeRepRes/docume                           xDoc         E        C     1-1                     Descrição do documento não fiscal.
                                                                                          255
ntos/docOutro/
NFSe/infNFSe/DPS/infDPS/
                                                                                                       Grupo de informações do fornecedor do
IBSCBS/valores/gReeRepRe                              fornec       G         -     0-1     -
                                                                                                              documento referenciado
s/documentos/
NFSe/infNFSe/DPS/infDPS/IBSC
BS/valores/gReeRepRes/docume                           CNPJ        CE        N     1-1     14       Número da inscrição federal (CNPJ) do fornecedor.
ntos/fornec/
NFSe/infNFSe/DPS/infDPS/IBSC
BS/valores/gReeRepRes/docume                           CPF         CE        N     1-1     11        Número da inscrição federal (CPF) do fornecedor.
ntos/fornec/


                                                                                                                                     10
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                           Este elemento só deverá ser preenchido para
BS/valores/gReeRepRes/docume         NIF         CE   C   1-1    40
                                                                              fornecedores não residentes no Brasil.
ntos/fornec/
                                                                               Motivo para não informação do NIF:
NFSe/infNFSe/DPS/infDPS/IBSC
BS/valores/gReeRepRes/docume       cNaoNIF       CE   N   1-1    1            0 - Não informado na nota de origem;
ntos/fornec/                                                                         1 - Dispensado do NIF;
                                                                                    2 - Não exigência do NIF;
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                 1-
BS/valores/gReeRepRes/docume        xNome        E    C   1-1                  Nome / Razão Social do fornecedor.
                                                                150
ntos/fornec/
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                            Data da emissão do documento dedutível.
BS/valores/gReeRepRes/docume       dtEmiDoc      E    D   1-1    -
                                                                                 Ano, mês e dia (AAAA-MM-DD)
ntos/
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                          Data da competência do documento dedutível.
BS/valores/gReeRepRes/docume      dtCompDoc      E    D   1-1    -
                                                                                 Ano, mês e dia (AAAA-MM-DD)
ntos/
                                                                       Tipo de valor incluído neste documento, recebido por
                                                                          motivo de estarem relacionadas a operações de
                                                                            terceiros, objeto de reembolso, repasse ou
                                                                        ressarcimento pelo recebedor, já tributados e aqui
                                                                                            referenciados

                                                                        01 = Repasse de remuneração por intermediação de
                                                                                     imóveis a demais corretores
                                                                                            envolvidos na operação
                                                                          02 = Repasse de valores a fornecedor relativo a
                                                                                   fornecimento intermediado por
NFSe/infNFSe/DPS/infDPS/IBSC                                                                  agência de turismo
BS/valores/gReeRepRes/docume     tpReeRepRes     E    N   1-1    2         03 = Reembolso ou ressarcimento recebido por
ntos/                                                                                  agência de propaganda e
                                                                                 publicidade por valores pagos relativos a
                                                                              serviços de produção externa por conta
                                                                                              e ordem de terceiro
                                                                           04 = Reembolso ou ressarcimento recebido por
                                                                                       agência de propaganda e
                                                                                 publicidade por valores pagos relativos a
                                                                                      serviços de mídia por conta
                                                                                              e ordem de terceiro
                                                                             99 = Outros reembolsos ou ressarcimentos
                                                                              recebidos por valores pagos relativos a
                                                                                 operações por conta e ordem de terceiro
                                                                        Descrição do reembolso ou ressarcimento quando a
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                 0-     opção é "99 – Outros reembolsos ou ressarcimentos
BS/valores/gReeRepRes/docume     xTpReeRepRes    E    C   0-1
                                                                150    recebidos por valores pagos relativos a operações por
ntos/
                                                                                      conta e ordem de terceiro".
                                                                            Valor monetário (total ou parcial, conforme
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                 1-    documento informado) utilizado para não inclusão na
BS/valores/gReeRepRes/docume     vlrReeRepRes    E    N   1-1
                                                                15V2    base de cálculo do ISS e do IBS e da CBS da NFS-e
ntos/
                                                                                    que está sendo emitida (R$).
NFSe/infNFSe/DPS/infDPS/                                               Grupo de informações relacionados aos tributos
                                     trib        G    -   1-1    -
IBSCBS/valores/                                                                               IBS e CBS
NFSe/infNFSe/DPS/infDPS/                                                Grupo de informações relacionadas ao IBS e à
                                   gIBSCBS       G    -   1-1    -
IBSCBS/valores/trib/                                                                              CBS
NFSe/infNFSe/DPS/infDPS/IBSC                                                      Código de Situação Tributária do
                                     CST         E    N   1-1    3
BS/valores/trib/gIBSCBS/                                                                     IBS e da CBS
NFSe/infNFSe/DPS/infDPS/IBSC                                                     Código de Classificação Tributária
                                  cClassTrib     E    N   1-1    6
BS/valores/trib/gIBSCBS/                                                                    do IBS e da CBS
NFSe/infNFSe/DPS/infDPS/IBSC                                             Código e classificação do crédito presumido: IBS e
                                  cCredPres      E    N   0-1    2
BS/valores/trib/gIBSCBS/                                                                          CBS.
NFSe/infNFSe/DPS/infDPS/
IBSCBSSEL/valores/trib/gIB       gTribRegular    G    -   0-1    -      Grupo de informações da Tributação Regular
SCBS/
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                                 Código de Situação Tributária do
BSSEL/valores/trib/gIBSCBS/gTr     CSTReg        E    N   1-1    3
                                                                                IBS e da CBS de tributação regular
ibRegular/
NFSe/infNFSe/DPS/infDPS/IBSC
                                                                               Código da Classificação Tributária do
BSSEL/valores/trib/gIBSCBS/gTr   cClassTribReg   E    N   1-1    6
                                                                                IBS e da CBS de tributação regular
ibRegular/
NFSe/infNFSe/DPS/infDPS/
                                                                            Grupo de informações relacionadas ao
IBSCBS/valores/trib/gIBSC            gDif        G    -   0-1    -
                                                                                 diferimento para IBS e CBS
BS/
NFSe/infNFSe/DPS/infDPS/IBSC                                     1-
                                    pDifUF       E    N   1-1             Percentual de diferimento para o IBS estadual.
BS/valores/trib/gIBSCBS/gDif/                                   3V2
NFSe/infNFSe/DPS/infDPS/IBSC                                     1-
                                   pDifMun       E    N   1-1            Percentual de diferimento para o IBS municipal.
BS/valores/trib/gIBSCBS/gDif/                                   3V2
NFSe/infNFSe/DPS/infDPS/IBSC                                     1-
                                   pDifCBS       E    N   1-1                 Percentual de diferimento para a CBS.
BS/valores/trib/gIBSCBS/gDif/                                   3V2




                                                                                                          11
                       2.2 Novos Grupos na NFS-e

                                Nos novos grupos que constam na NFS-e, fora dos limites da DPS, os campos aqui
                       apresentados são os que deverão ser parametrizados pelos Estados, pelos Municípios e pela
                       União ou aqueles que a própria plataforma, dadas as suas regras e cálculos, fornecerá de forma
                       automatizada a partir dos dados emitidos pelos contribuintes na DPS, listados no tópico
                       anterior.
                                De forma análoga ao novo grupo criado na DPS, aqui também foi criado o grupo IBSCBS
                       (caminho NFSe/infNFSe/). Neste grupo serão dispostos todos os subgrupos e informações relativas
                       aos novos tributos: IBS e CBS. Importante observar que os campos/tags/células listados abaixo
                       em amarelo se referem a campos que foram modificados em relação à versão anterior,
                       publicada na Nota Técnica SE/CGNFS-e nº 002, de 28 de fevereiro de 2025.



                                      CAMPO           ELE         TIPO           OCOR.          TAM.                            DESCRIÇÃO
      CAMINHO NO XML
                                                                                                               Grupo de informações geradas pelo sistema
 NFSe/infNFSe/                        IBSCBS           G              -            1-1               -
                                                                                                                      referentes ao IBS e à CBS




                       2.2.1 Grupo de Informações Comuns Relativas ao IBS e à CBS

     CAMINHO NO XML                    CAMPO          ELE        TIPO     OCOR.     TAM.                                    DESCRIÇÃO
                                                                                              Grupo de informações geradas pelo sistema referentes ao
NFSe/infNFSe/                         IBSCBS          G           -        1-1           -
                                                                                              IBS e à CBS
                                                                                              Código IBGE da localidade de incidência do IBS/CBS (local da
NFSe/infNFSe/IBSCBS/               cLocalidadeIncid    E          N        1-1           7
                                                                                              operação).
NFSe/infNFSe/IBSCBS/               xLocalidadeIncid    E          C        1-1          600   Nome da localidade de incidência do IBS/CBS.
                                                                                              Descrição do código indicador da operação de fornecimento,
NFSe/infNFSe/IBSCBS/                   xIndOp          E          C        1-1          500
                                                                                              conforme tabela “código indicador de operação”
NFSe/infNFSe/IBSCBS/                    xCST           E          C        1-1          600   Descrição do Código de Situação Tributária do IBS/CBS.

NFSe/infNFSe/IBSCBS/                 xCClassTrib       E          C        1-1          600   Descrição do Código de Classificação Tributária do IBS/CBS

NFSe/infNFSe/IBSCBS/                  pRedutor         E          C        1-1      1-2V2     Percentual de redução de aliquota em compra governamental.



                       2.2.2 Grupo de Informações de Valores Brutos Relativos ao IBS e à CBS


          CAMINHO NO XML                    CAMPO          ELE     TIPO          OCOR.         TAM.                            DESCRIÇÃO
                                                                                                           Grupo de valores brutos referentes ao IBS e à
 NFSe/infNFSe/IBSCBS/                       valores         G         -           1-1            -
                                                                                                           CBS
                                                                                                           Valor da base de cálculo (BC) do IBS/CBS antes das
                                                                                                                  reduções para cálculo do tributo bruto.

                                                                                                         vBC = vServ - descIncond – vCalcReeRepRes – vISSQN –
 NFSe/infNFSe/IBSCBS/valores/                   vBC         E         N           1-1         1-15V2
                                                                                                                       vPIS - vCOFINS (até 2026)

                                                                                                                                   ou




                                                                                                                                              12
                                                                                       vBC = vServ - descIncond – vCalcReeRepRes – vISSQN
                                                                                                            (até 2032)

                                                                                          Valor monetário (R$) total relativo ao fornecimento
                                                                                        próprio de bens materiais ou relacionados a operações
                                                                                             de terceiros, objeto de reembolso, repasse ou
 NFSe/infNFSe/IBSCBS/valores/        vCalcReeRepRes        E   N      0-1    1-15V2
                                                                                           ressarcimento pelo recebedor, já tributados e aqui
                                                                                         referenciados e que não integram da base de cálculo
                                                                                                   (BC) do ISSQN, do IBS e da CBS.
                                                                                        Grupo de Informações relativas aos valores do
 NFSe/infNFSe/IBSCBS/valores/              uf
                                                                                        IBS Estadual
                                                                                        Alíquota da UF para IBS da localidade de incidência
 NFSe/infNFSe/IBSCBS/valores/uf/        pIBSUF             E   N      1-1     1-2V2     parametrizada
                                                                                        no sistema.
 NFSe/infNFSe/IBSCBS/valores/uf/       pRedAliqUF          E   N      1-1     1-3V2     Percentual de redução de alíquota estadual.
                                                                                      pAliqEfetUF = pIBSUF x (1 - pRedAliqUF) x (1 - pRedutor)

 NFSe/infNFSe/IBSCBS/valores/uf/       pAliqEfetUF         E   N      1-1     1-2V2          Se pRedAliqUF não for informado na DPS, então
                                                                                                    pAliqEfetUF é a própria pIBSUF.

                                                                                        Grupo de Informações relativas aos valores do
 NFSe/infNFSe/IBSCBS/valores/             mun
                                                                                        IBS Municipal
                                                                                        Alíquota da UF para IBS da localidade de incidência
 NFSe/infNFSe/IBSCBS/valores/mun/       pIBSMun            E   N      1-1     1-2V2     parametrizada
                                                                                        no sistema.
 NFSe/infNFSe/IBSCBS/valores/mun/     pRedAliqMun          E   N      1-1     1-3V2     Percentual de redução de alíquota municipal.
                                                                                        pAliqEfetMun = pIBSMun x (1 - pRedAliqMun) x (1 -
                                                                                                            pRedutor)
 NFSe/infNFSe/IBSCBS/valores/mun/     pAliqEfetMun         E   N      1-1     1-2V2
                                                                                             Se pRedAliqMun não for informado na DPS, então
                                                                                                   pAliqEfetMun é a própria pIBSMun.

                                                                                        Grupo de Informações relativas aos valores da
 NFSe/infNFSe/IBSCBS/valores/             fed
                                                                                        CBS
                                                                                        Alíquota da UF para IBS da localidade de incidência
 NFSe/infNFSe/IBSCBS/valores/fed/        pCBS              E   N      1-1     1-2V2     parametrizada
                                                                                        no sistema.
 NFSe/infNFSe/IBSCBS/valores/fed/     pRedAliqCBS          E   N      1-1     1-3V2     Percentual da redução de alíquota.
                                                                                          pAliqEfetCBS = pCBS x (1 - pRedAliqCBS) x (1 -
                                                                                                            pRedutor)
 NFSe/infNFSe/IBSCBS/valores/fed/     pAliqEfetCBS         E   N      1-1     1-2V2
                                                                                             Se pRedAliqCBS não for informado na DPS, então
                                                                                                     pAliqEfetCBS é a própria pCBS.




                         2.2.3 Grupos Totalizadores

            CAMINHO NO XML                       CAMPO         ELE   TIPO   OCOR.     TAM.                         DESCRIÇÃO

NFSe/infNFSe/IBSCBS/                             totCIBS       G      -      1-1         -                   Grupo de Totalizadores
                                                                                                     Valor Total da NF considerando os impostos
                                                                                                                 por fora: IBS e CBS.
                                                                                                      O IBS e a CBS são por fora, por isso seus
                                                                                                       valores devem ser adicionados ao valor
                                                                                                                      total da NF.
NFSe/infNFSe/IBSCBS/totCIBS/                      vTotNF        E     N      1-1      1-15V2
                                                                                                              vTotNF = vLiq (em 2026)

                                                                                                      vTotNF = vLiq + vCBS + vIBSTot (a partir
                                                                                                                     de 2027)

                                                                                                      Grupo de informações de tributação
NFSe/infNFSe/IBSCBS/totCIBS/                 gTribRegular      G      -      0-1         -                          regular




                                                                                                                              13
                                                                                                                  Alíquota efetiva de tributação regular do
NFSe/infNFSe/IBSCBS/totCIBS/gTribRegular/        pAliqEfeRegIBSUF    E         N       1-1         1-2V2                         IBS estadual

                                                                                                                Valor da tributação regular do IBS estadual.
NFSe/infNFSe/IBSCBS/totCIBS/gTribRegular/          vTribRegIBSUF     E         N       1-1      1-15V2
                                                                                                                 vTribRegIBSUF = vBC x pAliqEfeRegIBSUF

                                                                                                                  Alíquota efetiva de tributação regular do
NFSe/infNFSe/IBSCBS/totCIBS/gTribRegular/        pAliqEfeRegIBSMun   E         N       1-1         1-2V2                        IBS municipal

                                                                                                                     Valor da tributação regular do IBS
                                                                                                                                  municipal.
NFSe/infNFSe/IBSCBS/totCIBS/gTribRegular/         vTribRegIBSMun     E         N       1-1      1-15V2
                                                                                                                          vTribRegIBSMun = vBC x
                                                                                                                             pAliqEfeRegIBSMun

                                                                                                                  Alíquota efetiva de tributação regular da
NFSe/infNFSe/IBSCBS/totCIBS/gTribRegular/         pAliqEfeRegCBS     E         N       1-1         1-2V2                             CBS

                                                                                                                     Valor da tributação regular da CBS.
NFSe/infNFSe/IBSCBS/totCIBS/gTribRegular/          vTribRegCBS       E         N       1-1      1-15V2
                                                                                                                   vTribRegCBS = vBC x pAliqEfeRegCBS

                                                                                                                 Grupo de informações da composição
                                                                                                                 do valor do IBS e da CBS em compras
NFSe/infNFSe/IBSCBS/totCIBS/                     gTribCompraGov      G         -       0-1           -
                                                                                                                            governamentais


                                                                                                                 Alíquota do IBS de competência do Estado
NFSe/infNFSe/IBSCBS/totCIBS/gTribCompraGov            pIBSUF         E         N       1-1         1-2V2


                                                                                                                  Valor do Tributo do IBS da UF calculado
NFSe/infNFSe/IBSCBS/totCIBS/gTribCompraGov            vIBSUF         E         N       1-1      1-15V2

                                                                                                                     Alíquota do IBS de competência do
NFSe/infNFSe/IBSCBS/totCIBS/gTribCompraGov           pIBSMun         E         N       1-1         1-2V2                          Município

                                                                                                                    Valor do Tributo do IBS do Município
NFSe/infNFSe/IBSCBS/totCIBS/gTribCompraGov           vIBSMun         E         N       1-1      1-15V2                            calculado


                                                                                                                                 Alíquota da CBS
NFSe/infNFSe/IBSCBS/totCIBS/gTribCompraGov             pCBS          E         N       1-1         1-2V2


                                                                                                                      Valor do Tributo da CBS calculado
NFSe/infNFSe/IBSCBS/totCIBS/gTribCompraGov             vCBS          E         N       1-1      1-15V2




                                 2.2.3.1 Grupo de Informações Relativas às Totalizações do IBS

             CAMINHO NO XML                            CAMPO         ELE     TIPO   OCOR.    TAM.                                DESCRIÇÃO

NFSe/infNFSe/IBSCBS/totCIBS/                             gIBS            G     -      1-1      -           Grupo de totalizadores referentes ao IBS
                                                                                                           Valor total do IBS.
                                                                                               1-
NFSe/infNFSe/IBSCBS/totCIBS/gIBS/                       vIBSTot          E     N      1-1
                                                                                              15V2
                                                                                                           vIBSTot = vIBSUF + vIBSMun
                                                                                                           Grupo de valores referentes ao crédito
NFSe/infNFSe/IBSCBS/totCIBS/gIBS/                    gIBSCredPres        G     -      0-1      -
                                                                                                           presumido para IBS

                                                                                               1-
NFSe/infNFSe/IBSCBS/totCIBS/gIBS/gIBSCredPres/        pCredPresIBS       E     N      1-1                  Alíquota do crédito presumido para o IBS
                                                                                              2V2

                                                                                                           Valor do Crédito Presumido para o IBS
                                                                                               1-
NFSe/infNFSe/IBSCBS/totCIBS/gIBS/gIBSCredPres/        vCredPresIBS       E     N      1-1
                                                                                              15V2
                                                                                                           vCredPresIBS = vBC x pCredPresIBS


NFSe/infNFSe/IBSCBS/totCIBS/gIBS/                      gIBSUFTot         G     -      1-1      -           Grupo de valores referentes ao IBS Estadual


                                                                                                           Total do Diferimento do IBS estadual.
                                                                                               1-
NFSe/infNFSe/IBSCBS/totCIBS/gIBS/gIBSUFTot/              vDifUF          E     N      1-1
                                                                                              15V2
                                                                                                           vDifUF = vIBSUF x pDifUF
                                                                                                           Total valor do IBS estadual.
                                                                                               1-
NFSe/infNFSe/IBSCBS/totCIBS/gIBS/gIBSUFTot/              vIBSUF          E     N      1-1
                                                                                              15V2
                                                                                                           vIBSUF = vBC x (pIBSUF ou pAliqEfetUF)




                                                                                                                                             14
NFSe/infNFSe/IBSCBS/totCIBS/gIBS/                gIBSMunTot     G       -          1-1           -           Grupo de valores referentes ao IBS Municipal


                                                                                                             Total do Diferimento do IBS municipal.
                                                                                                 1-
NFSe/infNFSe/IBSCBS/totCIBS/gIBS/gIBSMunTot/       vDifMun      E       N          1-1
                                                                                                15V2
                                                                                                             vDifMun = vIBSMun x pDifMun

                                                                                                             Total valor do IBS municipal.
                                                                                                 1-
NFSe/infNFSe/IBSCBS/totCIBS/gIBS/gIBSMunTot/      vIBSMun       E       N          1-1
                                                                                                15V2
                                                                                                             vIBSMun = vBC x (pIBSMun ou pAliqEfetMun)




                               2.2.3.2 Grupo de Informações Relativas às Totalizações da CBS

              CAMINHO NO XML                       CAMPO        ELE         TIPO         OCOR.         TAM.                          DESCRIÇÃO

NFSe/infNFSe/IBSCBS/totCIBS/                        gCBS            G        -            1-1            -          Grupo de valores referentes à CBS

                                                                                                                    Grupo de valores referentes ao crédito
NFSe/infNFSe/IBSCBS/totCIBS/gCBS/                gCBSCredPres       G        -            0-1            -
                                                                                                                    presumido para CBS

NFSe/infNFSe/IBSCBS/totCIBS/gCBS/gCBSCredPres/   pCredPresCBS       E        N            1-1          1-2V2        Alíquota do crédito presumido para a CBS

                                                                                                                    Valor do Crédito Presumido da CBS.
                                                                                                        1-
NFSe/infNFSe/IBSCBS/totCIBS/gCBS/gCBSCredPres/   vCredPresCBS       E        N            1-1
                                                                                                       15V2
                                                                                                                    vCredPresCBS = vBC x pCredPresCBS
                                                                                                                    Total do Diferimento CBS.
                                                                                                        1-
NFSe/infNFSe/IBSCBS/totCIBS/gCBS/                  vDifCBS          E        N            1-1
                                                                                                       15V2
                                                                                                                    vDifCBS = vCBS x pDifCBS

                                                                                                                    Total valor da CBS da União.
                                                                                                        1-
NFSe/infNFSe/IBSCBS/totCIBS/gCBS/                    vCBS           E        N            1-1
                                                                                                       15V2
                                                                                                                    vCBS = vBC x (pCBS ou pAliqEfetCBS)




                                                        SAMUEL KRUGER
                                           Auditor-Fiscal da Receita Federal do Brasil
                      Secretário-Executivo da Secretaria-Executiva do Comitê Gestor da Nota Fiscal de Serviço
                                             Eletrônica de Padrão Nacional (SE/CGNFS-e)




                                                                                                                                             15
