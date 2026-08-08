CREATE OR REPLACE PROCEDURE "PROC_ALTERA_TGFPAR_END_NM"(P_CODUSU    NUMBER, -- Código do usuário logado
                                                        P_IDSESSAO  VARCHAR2, -- Identificador da execução. Serve para buscar informações dos parâmetros/campos da execução.
                                                        P_QTDLINHAS NUMBER, -- Informa a quantidade de registros selecionados no momento da execução.
                                                        P_MENSAGEM  OUT VARCHAR2 -- Caso seja passada uma mensagem aqui, ela será exibida como uma informação ao usuário.
                                                        ) AS
  PARAM_DTINI        DATE;
  PARAM_DTFIN        DATE;
  PARAM_CODEMP       VARCHAR2(4000);
  PARAM_CODEMPMATRIZ VARCHAR2(4000);
  PARAM_NUNOTA       VARCHAR2(4000);
  PARAM_CODTIPOPER   VARCHAR2(4000);
  PARAM_APENASDIFAL  VARCHAR2(4000);
  FIELD_SEQUENCIA    NUMBER;
BEGIN

  -- Os valores informados pelo formulário de parâmetros, podem ser obtidos com as funções:
  --     ACT_INT_PARAM
  --     ACT_DEC_PARAM
  --     ACT_TXT_PARAM
  --     ACT_DTA_PARAM
  -- Estas funções recebem 2 argumentos:
  --     ID DA SESSÃO - Identificador da execução (Obtido através de P_IDSESSAO))
  --     NOME DO PARAMETRO - Determina qual parametro deve se deseja obter.

  PARAM_DTINI        := ACT_DTA_PARAM(P_IDSESSAO, 'DTINI');
  PARAM_DTFIN        := ACT_DTA_PARAM(P_IDSESSAO, 'DTFIN');
  PARAM_CODEMP       := ACT_TXT_PARAM(P_IDSESSAO, 'CODEMP');
  PARAM_CODEMPMATRIZ := ACT_TXT_PARAM(P_IDSESSAO, 'CODEMPMATRIZ');
  PARAM_NUNOTA       := ACT_TXT_PARAM(P_IDSESSAO, 'NUNOTA');
  PARAM_CODTIPOPER   := ACT_TXT_PARAM(P_IDSESSAO, 'CODTIPOPER');
  PARAM_APENASDIFAL  := ACT_TXT_PARAM(P_IDSESSAO, 'APENASDIFAL');

  FOR I IN 1 .. P_QTDLINHAS -- Este loop permite obter o valor de campos dos registros envolvidos na execução.
  LOOP
    -- A variável "I" representa o registro corrente.
    -- Para obter o valor dos campos utilize uma das seguintes funções:
    --     ACT_INT_FIELD (Retorna o valor de um campo tipo NUMÉRICO INTEIRO))
    --     ACT_DEC_FIELD (Retorna o valor de um campo tipo NUMÉRICO DECIMAL))
    --     ACT_TXT_FIELD (Retorna o valor de um campo tipo TEXTO),
    --     ACT_DTA_FIELD (Retorna o valor de um campo tipo DATA)
    -- Estas funções recebem 3 argumentos:
    --     ID DA SESSÃO - Identificador da execução (Obtido através do parâmetro P_IDSESSAO))
    --     NÚMERO DA LINHA - Relativo a qual linha selecionada.
    --     NOME DO CAMPO - Determina qual campo deve ser obtido.
    FIELD_SEQUENCIA := ACT_INT_FIELD(P_IDSESSAO, I, 'SEQUENCIA');
  
    PROC_INCLUI_PARCEIRO_UFS_NM(PARAM_DTINI,
                                PARAM_DTFIN,
                                PARAM_CODEMP,
                                PARAM_CODEMPMATRIZ,
                                PARAM_NUNOTA,
                                PARAM_CODTIPOPER,
                                PARAM_APENASDIFAL,
                                FIELD_SEQUENCIA,
                                P_MENSAGEM);
  
  END LOOP;

  -- <ESCREVA SEU CÓDIGO DE FINALIZAÇÃO AQUI> --

END;

/