CREATE OR REPLACE PROCEDURE STP_PARCELARFINA_SATIS (
       P_CODUSU      NUMBER,
       P_IDSESSAO    VARCHAR2,
       P_QTDLINHAS   NUMBER,
       P_MENSAGEM    OUT VARCHAR2
) AS

       C_MAX_PARCELAS       CONSTANT PLS_INTEGER := 360;
       C_RANGE_PADRAO       CONSTANT PLS_INTEGER := 30;
       C_TOLERANCIA         CONSTANT NUMBER      := 0.005;
       C_GERA_ACERTO        CONSTANT VARCHAR2(1) := 'S';

       TYPE T_PARCELA IS RECORD (
            NUMERO      PLS_INTEGER,
            VALORBASE   NUMBER,
            VALOR       NUMBER,
            VENCIMENTO  DATE
       );
       TYPE T_PLANO IS TABLE OF T_PARCELA INDEX BY PLS_INTEGER;

       V_NROPARCELAS        PLS_INTEGER;
       V_VLRAUTOMATICO      VARCHAR2(10);
       V_DESCRPARCELA       VARCHAR2(4000);
       V_BASEVENCIMENTO     DATE;
       V_RANGE              PLS_INTEGER;
       V_APLICAJUROS        VARCHAR2(10);
       V_FORMAJUROS         VARCHAR2(10);
       V_TAXAJUROS          NUMBER;
       V_ORIGEMDTNEG        VARCHAR2(10);
       V_TIPOINTERVALO      VARCHAR2(10);
       V_AJUSTADIAUTIL      VARCHAR2(10);
       V_PERMITEVENCPASS    VARCHAR2(10);

       V_USUARIO            VARCHAR2(100);
       V_PLANO              T_PLANO;
       V_PROXNUFIN          NUMBER;
       V_NUACERTO           NUMBER;
       V_SEQACERTO          PLS_INTEGER := 0;
       V_TITULOSPROC        PLS_INTEGER := 0;
       V_NUFIN              NUMBER;
       V_ORIGEM             TGFFIN%ROWTYPE;

       E_TITULO_EM_USO      EXCEPTION;
       PRAGMA EXCEPTION_INIT(E_TITULO_EM_USO, -54);

       -------------------------------------------------------------------
       -- LEITURA DEFENSIVA DOS PARAMETROS DA ACAO
       -------------------------------------------------------------------
       FUNCTION FN_PARAM_TXT(P_NOME VARCHAR2, P_PADRAO VARCHAR2 DEFAULT NULL) RETURN VARCHAR2 IS
              V_RET VARCHAR2(4000);
       BEGIN
              V_RET := TRIM(ACT_TXT_PARAM(P_IDSESSAO, P_NOME));
              RETURN NVL(V_RET, P_PADRAO);
       EXCEPTION
              WHEN OTHERS THEN RETURN P_PADRAO;
       END FN_PARAM_TXT;

       FUNCTION FN_PARAM_OPC(P_NOME VARCHAR2, P_PADRAO VARCHAR2 DEFAULT NULL) RETURN VARCHAR2 IS
              V_RET VARCHAR2(4000);
       BEGIN
              V_RET := UPPER(TRIM(REPLACE(REPLACE(ACT_TXT_PARAM(P_IDSESSAO, P_NOME), CHR(39), ''), CHR(34), '')));
              RETURN NVL(V_RET, P_PADRAO);
       EXCEPTION
              WHEN OTHERS THEN RETURN P_PADRAO;
       END FN_PARAM_OPC;

       FUNCTION FN_PARAM_INT(P_NOME VARCHAR2, P_PADRAO PLS_INTEGER DEFAULT NULL) RETURN PLS_INTEGER IS
       BEGIN
              RETURN NVL(ACT_INT_PARAM(P_IDSESSAO, P_NOME), P_PADRAO);
       EXCEPTION
              WHEN OTHERS THEN RETURN P_PADRAO;
       END FN_PARAM_INT;

       FUNCTION FN_PARAM_DEC(P_NOME VARCHAR2, P_PADRAO NUMBER DEFAULT NULL) RETURN NUMBER IS
       BEGIN
              RETURN NVL(ACT_DEC_PARAM(P_IDSESSAO, P_NOME), P_PADRAO);
       EXCEPTION
              WHEN OTHERS THEN RETURN P_PADRAO;
       END FN_PARAM_DEC;

       FUNCTION FN_PARAM_DTA(P_NOME VARCHAR2, P_PADRAO DATE DEFAULT NULL) RETURN DATE IS
       BEGIN
              RETURN NVL(ACT_DTA_PARAM(P_IDSESSAO, P_NOME), P_PADRAO);
       EXCEPTION
              WHEN OTHERS THEN RETURN P_PADRAO;
       END FN_PARAM_DTA;

       -------------------------------------------------------------------
       -- CONVERSAO E CALCULOS AUXILIARES
       -------------------------------------------------------------------
       FUNCTION FN_NUM_BR(P_TXT VARCHAR2) RETURN NUMBER IS
              V_TXT VARCHAR2(4000) := TRIM(REPLACE(P_TXT, ' ', ''));
       BEGIN
              IF V_TXT IS NULL THEN
                 RAISE_APPLICATION_ERROR(-20115, 'VALOR DE PARCELA VAZIO NA LISTA INFORMADA.');
              END IF;

              V_TXT := REPLACE(REPLACE(REPLACE(V_TXT, 'R$', ''), CHR(9), ''), CHR(160), '');

              IF INSTR(V_TXT, ',') > 0 THEN
                 V_TXT := REPLACE(REPLACE(V_TXT, '.', ''), ',', '.');
              END IF;

              IF NOT REGEXP_LIKE(V_TXT, '^[0-9]+(\.[0-9]{1,6})?$') THEN
                 RAISE_APPLICATION_ERROR(-20115, 'VALOR DE PARCELA INVALIDO: "' || P_TXT || '". USE O FORMATO 1234,56');
              END IF;

              RETURN TO_NUMBER(V_TXT);
       END FN_NUM_BR;

       FUNCTION FN_JUROS(P_BASE NUMBER) RETURN NUMBER IS
       BEGIN
              IF V_APLICAJUROS <> 'S' THEN
                 RETURN 0;
              END IF;

              RETURN CASE V_FORMAJUROS
                        WHEN 'P' THEN ROUND(P_BASE * (V_TAXAJUROS / 100), 2)
                        WHEN 'V' THEN ROUND(V_TAXAJUROS, 2)
                        ELSE 0
                     END;
       END FN_JUROS;

       FUNCTION FN_VENCIMENTO(P_PARCELA PLS_INTEGER) RETURN DATE IS
              V_DT DATE;
       BEGIN
              IF V_TIPOINTERVALO = 'M' THEN
                 V_DT := ADD_MONTHS(TRUNC(V_BASEVENCIMENTO), P_PARCELA - 1);
              ELSE
                 V_DT := TRUNC(V_BASEVENCIMENTO) + ((P_PARCELA - 1) * V_RANGE);
              END IF;

              IF V_AJUSTADIAUTIL = 'S' THEN
                 WHILE (TRUNC(V_DT) - TRUNC(V_DT, 'IW')) IN (5, 6) LOOP
                       V_DT := V_DT + 1;
                 END LOOP;
              END IF;

              RETURN V_DT;
       END FN_VENCIMENTO;

       FUNCTION FN_DTNEG(P_DTNEGORIGINAL DATE) RETURN DATE IS
       BEGIN
              RETURN CASE V_ORIGEMDTNEG
                        WHEN 'H' THEN TRUNC(SYSDATE)
                        ELSE NVL(TRUNC(P_DTNEGORIGINAL), TRUNC(SYSDATE))
                     END;
       END FN_DTNEG;

       -------------------------------------------------------------------
       -- VALIDACOES
       -------------------------------------------------------------------
       PROCEDURE PR_VALIDA_PARAMETROS IS
       BEGIN
              IF NVL(P_QTDLINHAS, 0) < 1 THEN
                 RAISE_APPLICATION_ERROR(-20102, 'SELECIONE AO MENOS UM TITULO PARA PARCELAR.');
              END IF;

              IF V_VLRAUTOMATICO NOT IN ('S', 'N') THEN
                 RAISE_APPLICATION_ERROR(-20101, 'PARAMETRO VALOR AUTOMATICO NAO INFORMADO. INFORME S PARA RATEIO AUTOMATICO OU N PARA VALORES MANUAIS.');
              END IF;

              IF NVL(V_NROPARCELAS, 0) < 2 THEN
                 RAISE_APPLICATION_ERROR(-20101, 'O NUMERO DE PARCELAS DEVE SER MAIOR OU IGUAL A 2.');
              END IF;

              IF V_NROPARCELAS > C_MAX_PARCELAS THEN
                 RAISE_APPLICATION_ERROR(-20101, 'O NUMERO DE PARCELAS EXCEDE O LIMITE DE ' || C_MAX_PARCELAS || '.');
              END IF;

              IF V_BASEVENCIMENTO IS NULL THEN
                 RAISE_APPLICATION_ERROR(-20101, 'INFORME A DATA BASE DE VENCIMENTO DA PRIMEIRA PARCELA.');
              END IF;

              IF TRUNC(V_BASEVENCIMENTO) < TRUNC(SYSDATE) AND V_PERMITEVENCPASS <> 'S' THEN
                 RAISE_APPLICATION_ERROR(-20101, 'A DATA BASE DE VENCIMENTO ' || TO_CHAR(V_BASEVENCIMENTO, 'DD/MM/YYYY') || ' E ANTERIOR A HOJE.');
              END IF;

              IF V_TIPOINTERVALO = 'D' AND NVL(V_RANGE, 0) < 1 THEN
                 RAISE_APPLICATION_ERROR(-20101, 'O INTERVALO ENTRE PARCELAS DEVE SER MAIOR QUE ZERO.');
              END IF;

              IF V_APLICAJUROS = 'S' THEN
                 IF V_FORMAJUROS NOT IN ('P', 'V') THEN
                    RAISE_APPLICATION_ERROR(-20116, 'INFORME A FORMA DE JUROS: P PARA PERCENTUAL OU V PARA VALOR.');
                 END IF;
                 IF NVL(V_TAXAJUROS, 0) <= 0 THEN
                    RAISE_APPLICATION_ERROR(-20116, 'A TAXA OU VALOR DE JUROS DEVE SER MAIOR QUE ZERO.');
                 END IF;
                 IF V_FORMAJUROS = 'P' AND V_TAXAJUROS > 100 THEN
                    RAISE_APPLICATION_ERROR(-20116, 'PERCENTUAL DE JUROS ACIMA DE 100. REVISE O VALOR INFORMADO.');
                 END IF;
              END IF;

              IF V_VLRAUTOMATICO = 'N' THEN
                 IF V_DESCRPARCELA IS NULL THEN
                    RAISE_APPLICATION_ERROR(-20111, 'INFORME A LISTA DE PARCELAS NO FORMATO 1=1000,00;2=500,00');
                 END IF;
                 IF P_QTDLINHAS > 1 THEN
                    RAISE_APPLICATION_ERROR(-20117, 'O PARCELAMENTO COM VALORES MANUAIS ACEITA APENAS UM TITULO POR EXECUCAO. FORAM SELECIONADOS ' || P_QTDLINHAS || '.');
                 END IF;
              END IF;
       END PR_VALIDA_PARAMETROS;

       FUNCTION FN_CARREGA_TITULO(P_NUFIN NUMBER) RETURN TGFFIN%ROWTYPE IS
              V_FIN TGFFIN%ROWTYPE;
       BEGIN
              IF P_NUFIN IS NULL THEN
                 RAISE_APPLICATION_ERROR(-20103, 'NUFIN NAO IDENTIFICADO NA LINHA SELECIONADA.');
              END IF;

              BEGIN
                 SELECT * INTO V_FIN FROM TGFFIN WHERE NUFIN = P_NUFIN FOR UPDATE NOWAIT;
              EXCEPTION
                 WHEN NO_DATA_FOUND THEN
                      RAISE_APPLICATION_ERROR(-20103, 'TITULO NUFIN ' || P_NUFIN || ' NAO ENCONTRADO.');
                 WHEN E_TITULO_EM_USO THEN
                      RAISE_APPLICATION_ERROR(-20110, 'O TITULO NUFIN ' || P_NUFIN || ' ESTA SENDO ALTERADO POR OUTRO USUARIO. TENTE NOVAMENTE.');
              END;

              IF V_FIN.DHBAIXA IS NOT NULL THEN
                 RAISE_APPLICATION_ERROR(-20104, 'O TITULO NUFIN ' || P_NUFIN || ' JA ESTA BAIXADO E NAO PODE SER PARCELADO.');
              END IF;

              IF NVL(V_FIN.RECDESP, 0) <> 1 THEN
                 RAISE_APPLICATION_ERROR(-20105, 'O TITULO NUFIN ' || P_NUFIN || ' NAO E UM TITULO A RECEBER. SOMENTE RECEITAS PODEM SER PARCELADAS.');
              END IF;

              IF NVL(V_FIN.VLRBAIXA, 0) > 0 THEN
                 RAISE_APPLICATION_ERROR(-20106, 'O TITULO NUFIN ' || P_NUFIN || ' POSSUI BAIXA PARCIAL DE ' || TO_CHAR(V_FIN.VLRBAIXA, 'FM999G999G990D00') || ' E NAO PODE SER PARCELADO.');
              END IF;

              IF NVL(V_FIN.VLRDESDOB, 0) <= 0 THEN
                 RAISE_APPLICATION_ERROR(-20107, 'O TITULO NUFIN ' || P_NUFIN || ' POSSUI VALOR ZERADO OU NEGATIVO.');
              END IF;

              IF NVL(V_FIN.AD_PARCELADO, 'N') = 'S' THEN
                 RAISE_APPLICATION_ERROR(-20108, 'O TITULO NUFIN ' || P_NUFIN || ' JA FOI PARCELADO EM ' || TO_CHAR(V_FIN.AD_DTOPERPARC, 'DD/MM/YYYY') || '.');
              END IF;

              IF NVL(V_FIN.RATEADO, 'N') = 'S' THEN
                 RAISE_APPLICATION_ERROR(-20109, 'O TITULO NUFIN ' || P_NUFIN || ' POSSUI RATEIO E NAO PODE SER PARCELADO POR ESTA ROTINA.');
              END IF;

              RETURN V_FIN;
       END FN_CARREGA_TITULO;

       -------------------------------------------------------------------
       -- MONTAGEM DO PLANO DE PARCELAS
       -------------------------------------------------------------------
       PROCEDURE PR_PLANO_AUTOMATICO(P_VLRTITULO NUMBER) IS
              V_TOTAL     NUMBER;
              V_PARC      NUMBER;
              V_PARCBASE  NUMBER;
              V_ACUM      NUMBER := 0;
              V_ACUMBASE  NUMBER := 0;
       BEGIN
              V_PLANO.DELETE;

              V_TOTAL    := ROUND(P_VLRTITULO + FN_JUROS(P_VLRTITULO), 2);
              V_PARC     := TRUNC(V_TOTAL / V_NROPARCELAS, 2);
              V_PARCBASE := TRUNC(P_VLRTITULO / V_NROPARCELAS, 2);

              IF V_PARC <= 0 THEN
                 RAISE_APPLICATION_ERROR(-20107, 'O VALOR DO TITULO E INSUFICIENTE PARA ' || V_NROPARCELAS || ' PARCELAS.');
              END IF;

              FOR I IN 1 .. V_NROPARCELAS LOOP
                  V_PLANO(I).NUMERO     := I;
                  V_PLANO(I).VENCIMENTO := FN_VENCIMENTO(I);

                  IF I < V_NROPARCELAS THEN
                     V_PLANO(I).VALOR     := V_PARC;
                     V_PLANO(I).VALORBASE := V_PARCBASE;
                     V_ACUM               := V_ACUM + V_PARC;
                     V_ACUMBASE           := V_ACUMBASE + V_PARCBASE;
                  ELSE
                     V_PLANO(I).VALOR     := ROUND(V_TOTAL - V_ACUM, 2);
                     V_PLANO(I).VALORBASE := ROUND(P_VLRTITULO - V_ACUMBASE, 2);
                  END IF;
              END LOOP;
       END PR_PLANO_AUTOMATICO;

       PROCEDURE PR_PLANO_MANUAL(P_VLRTITULO NUMBER) IS
              TYPE T_ENTRADA IS TABLE OF NUMBER INDEX BY PLS_INTEGER;
              V_ENTRADA  T_ENTRADA;
              V_ITEM     VARCHAR2(4000);
              V_NUMTXT   VARCHAR2(4000);
              V_VLRTXT   VARCHAR2(4000);
              V_NUM      PLS_INTEGER;
              V_SOMA     NUMBER := 0;
              V_TOTAL    NUMBER;
              V_ACUM     NUMBER := 0;
              V_QTD      PLS_INTEGER := 0;
       BEGIN
              V_PLANO.DELETE;

              FOR R IN (SELECT TRIM(REGEXP_SUBSTR(V_DESCRPARCELA, '[^;]+', 1, LEVEL)) ITEM
                          FROM DUAL
                       CONNECT BY REGEXP_SUBSTR(V_DESCRPARCELA, '[^;]+', 1, LEVEL) IS NOT NULL) LOOP

                  V_ITEM := R.ITEM;
                  CONTINUE WHEN V_ITEM IS NULL;

                  IF INSTR(V_ITEM, '=') = 0 THEN
                     RAISE_APPLICATION_ERROR(-20111, 'ITEM ' || V_ITEM || ' FORA DO FORMATO ESPERADO. USE NUMERO=VALOR, EX: 1=1000,00;2=500,00');
                  END IF;

                  V_NUMTXT := TRIM(SUBSTR(V_ITEM, 1, INSTR(V_ITEM, '=') - 1));
                  V_VLRTXT := TRIM(SUBSTR(V_ITEM, INSTR(V_ITEM, '=') + 1));

                  IF NOT REGEXP_LIKE(V_NUMTXT, '^[0-9]+$') THEN
                     RAISE_APPLICATION_ERROR(-20114, 'NUMERO DE PARCELA INVALIDO: ' || V_NUMTXT);
                  END IF;

                  V_NUM := TO_NUMBER(V_NUMTXT);

                  IF V_NUM < 1 THEN
                     RAISE_APPLICATION_ERROR(-20114, 'A PARCELA NAO PODE SER ZERO OU NEGATIVA.');
                  END IF;

                  IF V_NUM > V_NROPARCELAS THEN
                     RAISE_APPLICATION_ERROR(-20114, 'A PARCELA ' || V_NUM || ' E MAIOR QUE O NUMERO DE PARCELAS INFORMADO (' || V_NROPARCELAS || ').');
                  END IF;

                  IF V_ENTRADA.EXISTS(V_NUM) THEN
                     RAISE_APPLICATION_ERROR(-20114, 'A PARCELA ' || V_NUM || ' FOI INFORMADA MAIS DE UMA VEZ.');
                  END IF;

                  V_ENTRADA(V_NUM) := FN_NUM_BR(V_VLRTXT);

                  IF V_ENTRADA(V_NUM) <= 0 THEN
                     RAISE_APPLICATION_ERROR(-20115, 'O VALOR DA PARCELA ' || V_NUM || ' DEVE SER MAIOR QUE ZERO.');
                  END IF;

                  V_SOMA := V_SOMA + V_ENTRADA(V_NUM);
                  V_QTD  := V_QTD + 1;
              END LOOP;

              IF V_QTD <> V_NROPARCELAS THEN
                 RAISE_APPLICATION_ERROR(-20112, 'A QUANTIDADE DE PARCELAS INFORMADA (' || V_QTD || ') E DIFERENTE DA ESPERADA (' || V_NROPARCELAS || ').');
              END IF;

              FOR I IN 1 .. V_NROPARCELAS LOOP
                  IF NOT V_ENTRADA.EXISTS(I) THEN
                     RAISE_APPLICATION_ERROR(-20114, 'AS PARCELAS DEVEM SER SEQUENCIAIS DE 1 ATE ' || V_NROPARCELAS || '. FALTOU A PARCELA ' || I || '.');
                  END IF;
              END LOOP;

              IF ABS(ROUND(V_SOMA, 2) - ROUND(P_VLRTITULO, 2)) > C_TOLERANCIA THEN
                 RAISE_APPLICATION_ERROR(-20113, 'A SOMA DAS PARCELAS (' || TO_CHAR(V_SOMA, 'FM999G999G990D00') || ') E DIFERENTE DO VALOR DO TITULO (' || TO_CHAR(P_VLRTITULO, 'FM999G999G990D00') || ').');
              END IF;

              V_TOTAL := ROUND(V_SOMA + FN_JUROS(V_SOMA), 2);

              FOR I IN 1 .. V_NROPARCELAS LOOP
                  V_PLANO(I).NUMERO     := I;
                  V_PLANO(I).VALORBASE  := V_ENTRADA(I);
                  V_PLANO(I).VENCIMENTO := FN_VENCIMENTO(I);

                  IF I < V_NROPARCELAS THEN
                     V_PLANO(I).VALOR := ROUND(V_ENTRADA(I) * V_TOTAL / V_SOMA, 2);
                     V_ACUM           := V_ACUM + V_PLANO(I).VALOR;
                  ELSE
                     V_PLANO(I).VALOR := ROUND(V_TOTAL - V_ACUM, 2);
                  END IF;
              END LOOP;
       END PR_PLANO_MANUAL;

       -------------------------------------------------------------------
       -- GRAVACAO
       -------------------------------------------------------------------
       PROCEDURE PR_REGISTRA_ACERTO(P_NUFINNOVO NUMBER, P_NUFINORIG NUMBER, P_NUNOTA NUMBER) IS
       BEGIN
              IF C_GERA_ACERTO <> 'S' THEN
                 RETURN;
              END IF;

              V_SEQACERTO := V_SEQACERTO + 1;

              INSERT INTO TGFFRE (NUACERTO, SEQUENCIA, NUFIN, NUFINORIG, NUNOTA, TIPACERTO, CODUSU, DHALTER)
              VALUES (V_NUACERTO, V_SEQACERTO, P_NUFINNOVO, P_NUFINORIG, P_NUNOTA, 'A', P_CODUSU, SYSDATE);
       END PR_REGISTRA_ACERTO;

       PROCEDURE PR_APLICA_PLANO(P_ORIG TGFFIN%ROWTYPE, P_DTNEG DATE) IS
              V_NOVO      TGFFIN%ROWTYPE;
              V_BASEDESD  NUMBER;
              V_FATOR     NUMBER;
              V_HIST      VARCHAR2(4000);
              V_VLRPARC   NUMBER;
              V_VENCPARC  DATE;
       BEGIN
              SELECT GREATEST(NVL(MAX(F.DESDOBRAMENTO), 0), NVL(P_ORIG.DESDOBRAMENTO, 0))
                INTO V_BASEDESD
                FROM TGFFIN F
               WHERE P_ORIG.NUNOTA IS NOT NULL
                 AND F.NUNOTA = P_ORIG.NUNOTA;

              FOR I IN 1 .. V_PLANO.COUNT LOOP

                  V_VLRPARC  := V_PLANO(I).VALOR;
                  V_VENCPARC := V_PLANO(I).VENCIMENTO;

                  V_FATOR := CASE WHEN NVL(P_ORIG.VLRDESDOB, 0) = 0 THEN 0
                                  ELSE V_PLANO(I).VALORBASE / P_ORIG.VLRDESDOB
                             END;

                  V_HIST := SUBSTR('PARCELA ' || I || '/' || V_NROPARCELAS || ' - ' || P_ORIG.HISTORICO, 1, 100);

                  IF I = 1 THEN
                     UPDATE TGFFIN
                        SET VLRDESDOB            = V_VLRPARC,
                            HISTORICO            = V_HIST,
                            DTVENC               = V_VENCPARC,
                            DTVENCINIC           = V_VENCPARC,
                            DTNEG                = P_DTNEG,
                            DESDOBRAMENTO        = NVL(P_ORIG.DESDOBRAMENTO, 1),
                            VLRIRF               = ROUND(NVL(P_ORIG.VLRIRF, 0)    * V_FATOR, 2),
                            VLRISS               = ROUND(NVL(P_ORIG.VLRISS, 0)    * V_FATOR, 2),
                            VLRINSS              = ROUND(NVL(P_ORIG.VLRINSS, 0)   * V_FATOR, 2),
                            VLRDESC              = ROUND(NVL(P_ORIG.VLRDESC, 0)   * V_FATOR, 2),
                            VLRVENDOR            = ROUND(NVL(P_ORIG.VLRVENDOR, 0) * V_FATOR, 2),
                            VLRPROV              = ROUND(NVL(P_ORIG.VLRPROV, 0)   * V_FATOR, 2),
                            VLRHONOR             = ROUND(NVL(P_ORIG.VLRHONOR, 0)  * V_FATOR, 2),
                            VLRJURO              = 0,
                            VLRMULTA             = 0,
                            DTALTER              = SYSDATE,
                            AD_PARCELADO         = 'S',
                            AD_FINANORIGINAL     = P_ORIG.NUFIN,
                            AD_USUDEPARCELAMENTO = V_USUARIO,
                            AD_DTOPERPARC        = SYSDATE
                      WHERE NUFIN = P_ORIG.NUFIN;

                     PR_REGISTRA_ACERTO(P_ORIG.NUFIN, P_ORIG.NUFIN, P_ORIG.NUNOTA);
                  ELSE
                     V_NOVO := P_ORIG;

                     V_NOVO.NUFIN                := V_PROXNUFIN;
                     V_PROXNUFIN                 := V_PROXNUFIN + 1;

                     V_NOVO.DESDOBRAMENTO        := V_BASEDESD + I - 1;
                     V_NOVO.VLRDESDOB            := V_VLRPARC;
                     V_NOVO.DTVENC               := V_VENCPARC;
                     V_NOVO.DTVENCINIC           := V_VENCPARC;
                     V_NOVO.DTNEG                := P_DTNEG;
                     V_NOVO.HISTORICO            := V_HIST;

                     V_NOVO.VLRIRF               := ROUND(NVL(P_ORIG.VLRIRF, 0)    * V_FATOR, 2);
                     V_NOVO.VLRISS               := ROUND(NVL(P_ORIG.VLRISS, 0)    * V_FATOR, 2);
                     V_NOVO.VLRINSS              := ROUND(NVL(P_ORIG.VLRINSS, 0)   * V_FATOR, 2);
                     V_NOVO.VLRDESC              := ROUND(NVL(P_ORIG.VLRDESC, 0)   * V_FATOR, 2);
                     V_NOVO.VLRVENDOR            := ROUND(NVL(P_ORIG.VLRVENDOR, 0) * V_FATOR, 2);
                     V_NOVO.VLRPROV              := ROUND(NVL(P_ORIG.VLRPROV, 0)   * V_FATOR, 2);
                     V_NOVO.VLRHONOR             := ROUND(NVL(P_ORIG.VLRHONOR, 0)  * V_FATOR, 2);

                     V_NOVO.VLRJURO              := 0;
                     V_NOVO.VLRMULTA             := 0;
                     V_NOVO.VLRBAIXA             := 0;
                     V_NOVO.DHBAIXA              := NULL;
                     V_NOVO.CODTIPOPERBAIXA      := NULL;
                     V_NOVO.DHTIPOPERBAIXA       := NULL;
                     V_NOVO.VLRMOEDABAIXA        := 0;
                     V_NOVO.VLRDESCEMBUT         := 0;
                     V_NOVO.VLRJUROEMBUT         := 0;
                     V_NOVO.VLRMULTAEMBUT        := 0;
                     V_NOVO.VLRMULTANEGOC        := 0;
                     V_NOVO.VLRJURONEGOC         := 0;
                     V_NOVO.VLRMULTALIB          := 0;
                     V_NOVO.VLRJUROLIB           := 0;

                     V_NOVO.DTALTER              := SYSDATE;
                     V_NOVO.CODUSU               := P_CODUSU;
                     V_NOVO.AD_PARCELADO         := 'S';
                     V_NOVO.AD_FINANORIGINAL     := P_ORIG.NUFIN;
                     V_NOVO.AD_USUDEPARCELAMENTO := V_USUARIO;
                     V_NOVO.AD_DTOPERPARC        := SYSDATE;

                     INSERT INTO TGFFIN VALUES V_NOVO;

                     PR_REGISTRA_ACERTO(V_NOVO.NUFIN, P_ORIG.NUFIN, P_ORIG.NUNOTA);
                  END IF;
              END LOOP;
       END PR_APLICA_PLANO;

BEGIN

       V_NROPARCELAS     := FN_PARAM_INT('P_NROPARCELAS');
       V_VLRAUTOMATICO   := FN_PARAM_OPC('P_VLRAUTOMATIC', 'X');
       V_DESCRPARCELA    := FN_PARAM_TXT('P_DESCRPARCELA');
       V_BASEVENCIMENTO  := FN_PARAM_DTA('P_BASEVENCIMEN');
       V_RANGE           := FN_PARAM_INT('P_RANGE', C_RANGE_PADRAO);
       V_APLICAJUROS     := FN_PARAM_OPC('P_JUROS', 'N');
       V_FORMAJUROS      := FN_PARAM_OPC('P_FORMAJUROS', 'X');
       V_TAXAJUROS       := FN_PARAM_DEC('P_TAXAJUROS', 0);
       V_ORIGEMDTNEG     := FN_PARAM_OPC('P_DTNEG', 'NO');
       V_TIPOINTERVALO   := FN_PARAM_OPC('P_TIPOINTERVALO', 'D');
       V_AJUSTADIAUTIL   := FN_PARAM_OPC('P_AJUSTADIAUTIL', 'N');
       V_PERMITEVENCPASS := FN_PARAM_OPC('P_PERMITEVENCPASS', 'N');

       V_RANGE := NVL(V_RANGE, C_RANGE_PADRAO);

       PR_VALIDA_PARAMETROS;

       BEGIN
          SELECT NOMEUSU INTO V_USUARIO FROM TSIUSU WHERE CODUSU = P_CODUSU;
       EXCEPTION
          WHEN NO_DATA_FOUND THEN V_USUARIO := 'CODUSU ' || P_CODUSU;
       END;

       SELECT NVL(MAX(NUFIN), 0) + 1    INTO V_PROXNUFIN FROM TGFFIN;
       SELECT NVL(MAX(NUACERTO), 0) + 1 INTO V_NUACERTO  FROM TGFFRE;

       FOR IDX IN 1 .. P_QTDLINHAS LOOP

           V_NUFIN  := ACT_INT_FIELD(P_IDSESSAO, IDX, 'NUFIN');
           V_ORIGEM := FN_CARREGA_TITULO(V_NUFIN);

           IF V_VLRAUTOMATICO = 'S' THEN
              PR_PLANO_AUTOMATICO(V_ORIGEM.VLRDESDOB);
           ELSE
              PR_PLANO_MANUAL(V_ORIGEM.VLRDESDOB);
           END IF;

           PR_APLICA_PLANO(V_ORIGEM, FN_DTNEG(V_ORIGEM.DTNEG));

           V_TITULOSPROC := V_TITULOSPROC + 1;
       END LOOP;

       P_MENSAGEM := 'PARCELAMENTO CONCLUIDO: ' || V_TITULOSPROC || ' TITULO(S) DIVIDIDO(S) EM ' || V_NROPARCELAS || ' PARCELAS.';

EXCEPTION
       WHEN OTHERS THEN
            IF SQLCODE BETWEEN -20999 AND -20000 THEN
               RAISE;
            END IF;
            RAISE_APPLICATION_ERROR(-20199, 'FALHA AO PARCELAR O TITULO ' || NVL(TO_CHAR(V_NUFIN), '?') || ': ' || SQLERRM);
END STP_PARCELARFINA_SATIS;
/
