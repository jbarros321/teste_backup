CREATE OR REPLACE PROCEDURE STP_PARCELARFINA_SATIS_TESTE (
       P_CODUSU      NUMBER,
       P_IDSESSAO    VARCHAR2,
       P_QTDLINHAS   NUMBER,
       P_MENSAGEM    OUT VARCHAR2
) AS
       PARAM_P_NROPARCELAS  NUMBER;
       PARAM_P_VLRAUTOMATIC VARCHAR2(4000);
       PARAM_P_DESCRPARCELA VARCHAR2(4000);
       PARAM_P_BASEVENCIMEN DATE;
       PARAM_P_RANGE        NUMBER;
       PARAM_P_JUROS        VARCHAR2(4000);
       PARAM_P_FORMAJUROS   VARCHAR2(4000);
       PARAM_P_TAXAJUROS    FLOAT;
       PARAM_P_DTNEG        VARCHAR2(4000);

       TYPE T_VALORES IS TABLE OF NUMBER INDEX BY PLS_INTEGER;
       V_ENTRADA   T_VALORES;
       V_VALOR     T_VALORES;
       V_BASE      T_VALORES;

       V_USUARIO       NUMBER;
       V_NUFIN         NUMBER;
       V_ORIG          TGFFIN%ROWTYPE;
       V_NOVO          TGFFIN%ROWTYPE;
       V_PROXNUFIN     NUMBER;
       V_NUACERTO      NUMBER;
       V_SEQACERTO     NUMBER := 0;
       V_BASEDESD      NUMBER;
       V_TOTALTIT      NUMBER;
       V_TOTALGERAL    NUMBER;
       V_JUROS         NUMBER;
       V_VLRPARC       NUMBER;
       V_VLRBASE       NUMBER;
       V_ACUM          NUMBER;
       V_ACUMBASE      NUMBER;
       V_FATOR         NUMBER;
       V_VENC          DATE;
       V_DTNEG         DATE;
       V_HIST          VARCHAR2(200);
       V_ITEM          VARCHAR2(4000);
       V_NUMTXT        VARCHAR2(4000);
       V_VLRTXT        VARCHAR2(4000);
       V_NUM           PLS_INTEGER;
       V_QTDINF        NUMBER := 0;
       V_SOMAINF       NUMBER := 0;
       V_TITULOS       NUMBER := 0;

       E_TITULO_EM_USO EXCEPTION;
       PRAGMA EXCEPTION_INIT(E_TITULO_EM_USO, -54);

BEGIN

       PARAM_P_NROPARCELAS  := PKG_MOCK_ACT.ACT_INT_PARAM(P_IDSESSAO, 'P_NROPARCELAS');
       PARAM_P_VLRAUTOMATIC := PKG_MOCK_ACT.ACT_TXT_PARAM(P_IDSESSAO, 'P_VLRAUTOMATIC');
       PARAM_P_DESCRPARCELA := PKG_MOCK_ACT.ACT_TXT_PARAM(P_IDSESSAO, 'P_DESCRPARCELA');
       PARAM_P_BASEVENCIMEN := PKG_MOCK_ACT.ACT_DTA_PARAM(P_IDSESSAO, 'P_BASEVENCIMEN');
       PARAM_P_RANGE        := PKG_MOCK_ACT.ACT_INT_PARAM(P_IDSESSAO, 'P_RANGE');
       PARAM_P_JUROS        := PKG_MOCK_ACT.ACT_TXT_PARAM(P_IDSESSAO, 'PARAM_P_JUROS');
       PARAM_P_FORMAJUROS   := PKG_MOCK_ACT.ACT_TXT_PARAM(P_IDSESSAO, 'P_FORMAJUROS');
       PARAM_P_TAXAJUROS    := PKG_MOCK_ACT.ACT_DEC_PARAM(P_IDSESSAO, 'P_TAXAJUROS');
       PARAM_P_DTNEG        := PKG_MOCK_ACT.ACT_TXT_PARAM(P_IDSESSAO, 'P_DTNEG');

       PARAM_P_VLRAUTOMATIC := NVL(UPPER(TRIM(REPLACE(PARAM_P_VLRAUTOMATIC, '''', ''))), 'X');
       PARAM_P_JUROS        := NVL(UPPER(TRIM(REPLACE(PARAM_P_JUROS,        '''', ''))), 'N');
       PARAM_P_FORMAJUROS   := NVL(UPPER(TRIM(REPLACE(PARAM_P_FORMAJUROS,   '''', ''))), 'X');
       PARAM_P_DTNEG        := NVL(UPPER(TRIM(REPLACE(PARAM_P_DTNEG,        '''', ''))), 'NO');
       PARAM_P_DESCRPARCELA := TRIM(PARAM_P_DESCRPARCELA);
       PARAM_P_RANGE        := NVL(PARAM_P_RANGE, 30);
       PARAM_P_TAXAJUROS    := NVL(PARAM_P_TAXAJUROS, 0);

       IF NVL(P_QTDLINHAS, 0) < 1 THEN
          RAISE_APPLICATION_ERROR(-20102, 'SELECIONE AO MENOS UM TITULO PARA PARCELAR.');
       END IF;

       IF PARAM_P_VLRAUTOMATIC NOT IN ('S', 'N') THEN
          RAISE_APPLICATION_ERROR(-20101, 'INFORME O PARAMETRO DE VALOR AUTOMATICO: S PARA RATEIO AUTOMATICO OU N PARA VALORES MANUAIS.');
       END IF;

       IF NVL(PARAM_P_NROPARCELAS, 0) < 2 THEN
          RAISE_APPLICATION_ERROR(-20101, 'O NUMERO DE PARCELAS DEVE SER MAIOR OU IGUAL A 2.');
       END IF;

       IF PARAM_P_NROPARCELAS > 360 THEN
          RAISE_APPLICATION_ERROR(-20101, 'O NUMERO DE PARCELAS EXCEDE O LIMITE DE 360.');
       END IF;

       IF PARAM_P_BASEVENCIMEN IS NULL THEN
          RAISE_APPLICATION_ERROR(-20101, 'INFORME A DATA BASE DE VENCIMENTO DA PRIMEIRA PARCELA.');
       END IF;

       IF PARAM_P_RANGE < 1 THEN
          RAISE_APPLICATION_ERROR(-20101, 'O INTERVALO ENTRE PARCELAS DEVE SER MAIOR QUE ZERO.');
       END IF;

       IF PARAM_P_JUROS = 'S' THEN
          IF PARAM_P_FORMAJUROS NOT IN ('P', 'V') THEN
             RAISE_APPLICATION_ERROR(-20116, 'INFORME A FORMA DE JUROS: P PARA PERCENTUAL OU V PARA VALOR.');
          END IF;
          IF PARAM_P_TAXAJUROS <= 0 THEN
             RAISE_APPLICATION_ERROR(-20116, 'A TAXA OU VALOR DE JUROS DEVE SER MAIOR QUE ZERO.');
          END IF;
          IF PARAM_P_FORMAJUROS = 'P' AND PARAM_P_TAXAJUROS > 100 THEN
             RAISE_APPLICATION_ERROR(-20116, 'PERCENTUAL DE JUROS ACIMA DE 100. REVISE O VALOR INFORMADO.');
          END IF;
       END IF;

       IF PARAM_P_VLRAUTOMATIC = 'N' THEN

          IF PARAM_P_DESCRPARCELA IS NULL THEN
             RAISE_APPLICATION_ERROR(-20111, 'INFORME A LISTA DE PARCELAS NO FORMATO 1=1000,00;2=500,00');
          END IF;

          IF P_QTDLINHAS > 1 THEN
             RAISE_APPLICATION_ERROR(-20117, 'O PARCELAMENTO COM VALORES MANUAIS ACEITA APENAS UM TITULO POR EXECUCAO. FORAM SELECIONADOS ' || P_QTDLINHAS || '.');
          END IF;

          FOR R IN (SELECT TRIM(REGEXP_SUBSTR(PARAM_P_DESCRPARCELA, '[^;]+', 1, LEVEL)) ITEM
                      FROM DUAL
                   CONNECT BY REGEXP_SUBSTR(PARAM_P_DESCRPARCELA, '[^;]+', 1, LEVEL) IS NOT NULL) LOOP

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

              IF V_NUM > PARAM_P_NROPARCELAS THEN
                 RAISE_APPLICATION_ERROR(-20114, 'A PARCELA ' || V_NUM || ' E MAIOR QUE O NUMERO DE PARCELAS INFORMADO (' || PARAM_P_NROPARCELAS || ').');
              END IF;

              IF V_ENTRADA.EXISTS(V_NUM) THEN
                 RAISE_APPLICATION_ERROR(-20114, 'A PARCELA ' || V_NUM || ' FOI INFORMADA MAIS DE UMA VEZ.');
              END IF;

              V_VLRTXT := REPLACE(REPLACE(REPLACE(REPLACE(V_VLRTXT, ' ', ''), 'R$', ''), CHR(9), ''), CHR(160), '');

              IF INSTR(V_VLRTXT, ',') > 0 THEN
                 V_VLRTXT := REPLACE(REPLACE(V_VLRTXT, '.', ''), ',', '.');
              END IF;

              IF NOT REGEXP_LIKE(V_VLRTXT, '^[0-9]+(\.[0-9]{1,6})?$') THEN
                 RAISE_APPLICATION_ERROR(-20115, 'VALOR DE PARCELA INVALIDO: ' || V_ITEM || '. USE O FORMATO 1234,56');
              END IF;

              V_ENTRADA(V_NUM) := TO_NUMBER(V_VLRTXT);

              IF V_ENTRADA(V_NUM) <= 0 THEN
                 RAISE_APPLICATION_ERROR(-20115, 'O VALOR DA PARCELA ' || V_NUM || ' DEVE SER MAIOR QUE ZERO.');
              END IF;

              V_SOMAINF := V_SOMAINF + V_ENTRADA(V_NUM);
              V_QTDINF  := V_QTDINF + 1;
           END LOOP;

           IF V_QTDINF <> PARAM_P_NROPARCELAS THEN
              RAISE_APPLICATION_ERROR(-20112, 'A QUANTIDADE DE PARCELAS INFORMADA (' || V_QTDINF || ') E DIFERENTE DA ESPERADA (' || PARAM_P_NROPARCELAS || ').');
           END IF;

           FOR I IN 1 .. PARAM_P_NROPARCELAS LOOP
               IF NOT V_ENTRADA.EXISTS(I) THEN
                  RAISE_APPLICATION_ERROR(-20114, 'AS PARCELAS DEVEM SER SEQUENCIAIS DE 1 ATE ' || PARAM_P_NROPARCELAS || '. FALTOU A PARCELA ' || I || '.');
               END IF;
           END LOOP;
       END IF;

       V_USUARIO := P_CODUSU;

       SELECT NVL(MAX(NUFIN), 0) + 1    INTO V_PROXNUFIN FROM TGFFIN;
       SELECT NVL(MAX(NUACERTO), 0) + 1 INTO V_NUACERTO  FROM TGFFRE;

       FOR IDX IN 1 .. P_QTDLINHAS LOOP

           V_NUFIN := PKG_MOCK_ACT.ACT_INT_FIELD(P_IDSESSAO, IDX, 'NUFIN');

           IF V_NUFIN IS NULL THEN
              RAISE_APPLICATION_ERROR(-20103, 'NUFIN NAO IDENTIFICADO NA LINHA SELECIONADA.');
           END IF;

           BEGIN
              SELECT * INTO V_ORIG FROM TGFFIN WHERE NUFIN = V_NUFIN FOR UPDATE NOWAIT;
           EXCEPTION
              WHEN NO_DATA_FOUND THEN
                   RAISE_APPLICATION_ERROR(-20103, 'TITULO NUFIN ' || V_NUFIN || ' NAO ENCONTRADO.');
              WHEN E_TITULO_EM_USO THEN
                   RAISE_APPLICATION_ERROR(-20110, 'O TITULO NUFIN ' || V_NUFIN || ' ESTA SENDO ALTERADO POR OUTRO USUARIO. TENTE NOVAMENTE.');
           END;

           IF V_ORIG.DHBAIXA IS NOT NULL THEN
              RAISE_APPLICATION_ERROR(-20104, 'O TITULO NUFIN ' || V_NUFIN || ' JA ESTA BAIXADO E NAO PODE SER PARCELADO.');
           END IF;

           IF NVL(V_ORIG.RECDESP, 0) <> 1 THEN
              RAISE_APPLICATION_ERROR(-20105, 'O TITULO NUFIN ' || V_NUFIN || ' NAO E UM TITULO A RECEBER. SOMENTE RECEITAS PODEM SER PARCELADAS.');
           END IF;

           IF NVL(V_ORIG.VLRBAIXA, 0) > 0 THEN
              RAISE_APPLICATION_ERROR(-20106, 'O TITULO NUFIN ' || V_NUFIN || ' POSSUI BAIXA PARCIAL E NAO PODE SER PARCELADO.');
           END IF;

           IF NVL(V_ORIG.VLRDESDOB, 0) <= 0 THEN
              RAISE_APPLICATION_ERROR(-20107, 'O TITULO NUFIN ' || V_NUFIN || ' POSSUI VALOR ZERADO OU NEGATIVO.');
           END IF;

           IF NVL(V_ORIG.AD_PARCELADO, 'N') = 'S' THEN
              RAISE_APPLICATION_ERROR(-20108, 'O TITULO NUFIN ' || V_NUFIN || ' JA FOI PARCELADO EM ' || TO_CHAR(V_ORIG.AD_DTOPERPARC, 'DD/MM/YYYY HH24:MI') || '.');
           END IF;

           IF NVL(V_ORIG.RATEADO, 'N') = 'S' THEN
              RAISE_APPLICATION_ERROR(-20109, 'O TITULO NUFIN ' || V_NUFIN || ' POSSUI RATEIO E NAO PODE SER PARCELADO POR ESTA ROTINA.');
           END IF;

           V_TOTALTIT := V_ORIG.VLRDESDOB;

           IF PARAM_P_JUROS = 'S' AND PARAM_P_FORMAJUROS = 'P' THEN
              V_JUROS := ROUND(V_TOTALTIT * (PARAM_P_TAXAJUROS / 100), 2);
           ELSIF PARAM_P_JUROS = 'S' AND PARAM_P_FORMAJUROS = 'V' THEN
              V_JUROS := ROUND(PARAM_P_TAXAJUROS, 2);
           ELSE
              V_JUROS := 0;
           END IF;

           V_TOTALGERAL := ROUND(V_TOTALTIT + V_JUROS, 2);

           V_VALOR.DELETE;
           V_BASE.DELETE;
           V_ACUM     := 0;
           V_ACUMBASE := 0;

           IF PARAM_P_VLRAUTOMATIC = 'S' THEN

              V_VLRPARC := TRUNC(V_TOTALGERAL / PARAM_P_NROPARCELAS, 2);
              V_VLRBASE := TRUNC(V_TOTALTIT   / PARAM_P_NROPARCELAS, 2);

              IF V_VLRPARC <= 0 THEN
                 RAISE_APPLICATION_ERROR(-20107, 'O VALOR DO TITULO NUFIN ' || V_NUFIN || ' E INSUFICIENTE PARA ' || PARAM_P_NROPARCELAS || ' PARCELAS.');
              END IF;

              FOR I IN 1 .. PARAM_P_NROPARCELAS LOOP
                  IF I < PARAM_P_NROPARCELAS THEN
                     V_VALOR(I) := V_VLRPARC;
                     V_BASE(I)  := V_VLRBASE;
                     V_ACUM     := V_ACUM + V_VLRPARC;
                     V_ACUMBASE := V_ACUMBASE + V_VLRBASE;
                  ELSE
                     V_VALOR(I) := ROUND(V_TOTALGERAL - V_ACUM, 2);
                     V_BASE(I)  := ROUND(V_TOTALTIT - V_ACUMBASE, 2);
                  END IF;
              END LOOP;

           ELSE

              IF ABS(ROUND(V_SOMAINF, 2) - ROUND(V_TOTALTIT, 2)) > 0.005 THEN
                 RAISE_APPLICATION_ERROR(-20113, 'A SOMA DAS PARCELAS (' || TO_CHAR(V_SOMAINF, 'FM999G999G990D00') || ') E DIFERENTE DO VALOR DO TITULO (' || TO_CHAR(V_TOTALTIT, 'FM999G999G990D00') || ').');
              END IF;

              FOR I IN 1 .. PARAM_P_NROPARCELAS LOOP
                  V_BASE(I) := V_ENTRADA(I);
                  IF I < PARAM_P_NROPARCELAS THEN
                     V_VALOR(I) := ROUND(V_ENTRADA(I) * V_TOTALGERAL / V_SOMAINF, 2);
                     V_ACUM     := V_ACUM + V_VALOR(I);
                  ELSE
                     V_VALOR(I) := ROUND(V_TOTALGERAL - V_ACUM, 2);
                  END IF;
              END LOOP;

           END IF;

           SELECT GREATEST(NVL(MAX(DESDOBRAMENTO), 0), NVL(V_ORIG.DESDOBRAMENTO, 0))
             INTO V_BASEDESD
             FROM TGFFIN
            WHERE V_ORIG.NUNOTA IS NOT NULL
              AND NUNOTA = V_ORIG.NUNOTA;

           IF PARAM_P_DTNEG = 'H' THEN
              V_DTNEG := TRUNC(SYSDATE);
           ELSE
              V_DTNEG := NVL(TRUNC(V_ORIG.DTNEG), TRUNC(SYSDATE));
           END IF;

           FOR I IN 1 .. PARAM_P_NROPARCELAS LOOP

               V_VENC    := TRUNC(PARAM_P_BASEVENCIMEN) + ((I - 1) * PARAM_P_RANGE);
               V_VLRPARC := V_VALOR(I);
               V_HIST    := SUBSTR('PARCELA ' || I || '/' || PARAM_P_NROPARCELAS || ' - ' || V_ORIG.HISTORICO, 1, 100);

               IF NVL(V_ORIG.VLRDESDOB, 0) = 0 THEN
                  V_FATOR := 0;
               ELSE
                  V_FATOR := V_BASE(I) / V_ORIG.VLRDESDOB;
               END IF;

               V_SEQACERTO := V_SEQACERTO + 1;

               IF I = 1 THEN

                  UPDATE TGFFIN
                     SET VLRDESDOB            = V_VLRPARC,
                         HISTORICO            = V_HIST,
                         DTVENC               = V_VENC,
                         DTVENCINIC           = V_VENC,
                         DTNEG                = V_DTNEG,
                         VLRIRF               = ROUND(NVL(V_ORIG.VLRIRF, 0)    * V_FATOR, 2),
                         VLRISS               = ROUND(NVL(V_ORIG.VLRISS, 0)    * V_FATOR, 2),
                         VLRINSS              = ROUND(NVL(V_ORIG.VLRINSS, 0)   * V_FATOR, 2),
                         VLRDESC              = ROUND(NVL(V_ORIG.VLRDESC, 0)   * V_FATOR, 2),
                         VLRVENDOR            = ROUND(NVL(V_ORIG.VLRVENDOR, 0) * V_FATOR, 2),
                         VLRPROV              = ROUND(NVL(V_ORIG.VLRPROV, 0)   * V_FATOR, 2),
                         VLRHONOR             = ROUND(NVL(V_ORIG.VLRHONOR, 0)  * V_FATOR, 2),
                         VLRJURO              = 0,
                         VLRMULTA             = 0,
                         DTALTER              = SYSDATE,
                         AD_PARCELADO         = 'S',
                         AD_FINANORIGINAL     = V_ORIG.NUFIN,
                         AD_USUDEPARCELAMENTO = V_USUARIO,
                         AD_DTOPERPARC        = SYSDATE
                   WHERE NUFIN = V_ORIG.NUFIN;

                  INSERT INTO TGFFRE (NUACERTO, SEQUENCIA, NUFIN, NUFINORIG, NUNOTA, TIPACERTO, CODUSU, DHALTER)
                  VALUES (V_NUACERTO, V_SEQACERTO, V_ORIG.NUFIN, V_ORIG.NUFIN, V_ORIG.NUNOTA, 'A', P_CODUSU, SYSDATE);

               ELSE

                  V_NOVO := V_ORIG;

                  V_NOVO.NUFIN                := V_PROXNUFIN;
                  V_PROXNUFIN                 := V_PROXNUFIN + 1;

                  V_NOVO.DESDOBRAMENTO        := V_BASEDESD + I - 1;
                  V_NOVO.VLRDESDOB            := V_VLRPARC;
                  V_NOVO.DTVENC               := V_VENC;
                  V_NOVO.DTVENCINIC           := V_VENC;
                  V_NOVO.DTNEG                := V_DTNEG;
                  V_NOVO.HISTORICO            := V_HIST;

                  V_NOVO.VLRIRF               := ROUND(NVL(V_ORIG.VLRIRF, 0)    * V_FATOR, 2);
                  V_NOVO.VLRISS               := ROUND(NVL(V_ORIG.VLRISS, 0)    * V_FATOR, 2);
                  V_NOVO.VLRINSS              := ROUND(NVL(V_ORIG.VLRINSS, 0)   * V_FATOR, 2);
                  V_NOVO.VLRDESC              := ROUND(NVL(V_ORIG.VLRDESC, 0)   * V_FATOR, 2);
                  V_NOVO.VLRVENDOR            := ROUND(NVL(V_ORIG.VLRVENDOR, 0) * V_FATOR, 2);
                  V_NOVO.VLRPROV              := ROUND(NVL(V_ORIG.VLRPROV, 0)   * V_FATOR, 2);
                  V_NOVO.VLRHONOR             := ROUND(NVL(V_ORIG.VLRHONOR, 0)  * V_FATOR, 2);

                  V_NOVO.VLRJURO              := 0;
                  V_NOVO.VLRMULTA             := 0;
                  V_NOVO.VLRBAIXA             := 0;

                  -- DHBAIXA / CODTIPOPERBAIXA / DHTIPOPERBAIXA sao herdados do titulo original.
                  -- O original e obrigatoriamente NAO baixado (validacoes -20104 e -20106), entao
                  -- ja estao no estado "sem baixa". Nao force NULL: CODTIPOPERBAIXA e NOT NULL
                  -- em TGFFIN e o INSERT estoura com ORA-01400.
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
                  V_NOVO.AD_FINANORIGINAL     := V_ORIG.NUFIN;
                  V_NOVO.AD_USUDEPARCELAMENTO := V_USUARIO;
                  V_NOVO.AD_DTOPERPARC        := SYSDATE;

                  INSERT INTO TGFFIN VALUES V_NOVO;

                  INSERT INTO TGFFRE (NUACERTO, SEQUENCIA, NUFIN, NUFINORIG, NUNOTA, TIPACERTO, CODUSU, DHALTER)
                  VALUES (V_NUACERTO, V_SEQACERTO, V_NOVO.NUFIN, V_ORIG.NUFIN, V_ORIG.NUNOTA, 'A', P_CODUSU, SYSDATE);

               END IF;

           END LOOP;

           V_TITULOS := V_TITULOS + 1;

       END LOOP;

       P_MENSAGEM := 'PARCELAMENTO CONCLUIDO: ' || V_TITULOS || ' TITULO(S) DIVIDIDO(S) EM ' || PARAM_P_NROPARCELAS || ' PARCELAS.';

EXCEPTION
       WHEN OTHERS THEN
            IF SQLCODE BETWEEN -20999 AND -20000 THEN
               RAISE;
            END IF;
            RAISE_APPLICATION_ERROR(-20199, 'FALHA AO PARCELAR O TITULO ' || NVL(TO_CHAR(V_NUFIN), '?') || ': ' || SQLERRM ||
                                       ' [' || SUBSTR(REPLACE(DBMS_UTILITY.FORMAT_ERROR_BACKTRACE, CHR(10), ' '), 1, 400) || ']');
END;
/
