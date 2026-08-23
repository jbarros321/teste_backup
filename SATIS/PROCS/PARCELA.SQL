CREATE OR REPLACE PROCEDURE STP_PARCELARFINA_SATIS (
       P_CODUSU      NUMBER,
       P_IDSESSAO    VARCHAR2,
       P_QTDLINHAS   NUMBER,
       P_MENSAGEM    OUT VARCHAR2
) AS
       PARAM_P_NROPARCELAS NUMBER;
       PARAM_P_VLRAUTOMATIC VARCHAR2(4000);
       PARAM_P_DESCRPARCELA VARCHAR2(4000);
       PARAM_P_BASEVENCIMEN DATE;
       PARAM_P_RANGE NUMBER;
       PARAM_P_JUROS VARCHAR2(4000);
       PARAM_P_FORMAJUROS VARCHAR2(4000);
       PARAM_P_TAXAJUROS FLOAT;
       PARAM_P_DTNEG VARCHAR2(4000);

       FIELD_NUFIN           NUMBER;
       P_USUARIO             VARCHAR2(4000);

       P_LINHA               VARCHAR2(4000);
       P_PARCELA             NUMBER;
       P_VALOR_RAW           VARCHAR2(4000);
       P_VALOR               NUMBER;
       P_NVNUFIN             NUMBER;
       P_VENCNOVO            DATE;
       P_VALOR_TOTAL         NUMBER;
       P_NEG                 DATE;
       P_NEGOCI              DATE;
       P_BAIXA               DATE;
       P_RECDESP             NUMBER;
       P_NUACERTO            NUMBER;

       TYPE T_NUMS IS TABLE OF NUMBER;
       P_PARCELAS   T_NUMS := T_NUMS();

       TYPE T_PARCELAS IS TABLE OF VARCHAR2(4000);
       P_LISTA  T_PARCELAS;

       V_TOTAL_PARCELAS NUMBER := 0;
       V_VALOR_TITULO   NUMBER := 0;

BEGIN
       PARAM_P_NROPARCELAS := ACT_INT_PARAM(P_IDSESSAO, 'P_NROPARCELAS');
       PARAM_P_VLRAUTOMATIC := ACT_TXT_PARAM(P_IDSESSAO, 'P_VLRAUTOMATIC');
       PARAM_P_DESCRPARCELA := ACT_TXT_PARAM(P_IDSESSAO, 'P_DESCRPARCELA');
       PARAM_P_BASEVENCIMEN := ACT_DTA_PARAM(P_IDSESSAO, 'P_BASEVENCIMEN');
       PARAM_P_RANGE := ACT_INT_PARAM(P_IDSESSAO, 'P_RANGE');
       PARAM_P_JUROS := ACT_TXT_PARAM(P_IDSESSAO, 'PARAM_P_JUROS');
       PARAM_P_FORMAJUROS := ACT_TXT_PARAM(P_IDSESSAO, 'P_FORMAJUROS');
       PARAM_P_TAXAJUROS := ACT_DEC_PARAM(P_IDSESSAO, 'P_TAXAJUROS');
       PARAM_P_DTNEG := ACT_TXT_PARAM(P_IDSESSAO, 'P_DTNEG');

       PARAM_P_DTNEG := REPLACE(PARAM_P_DTNEG, '''', '');

       SELECT NOMEUSU INTO P_USUARIO FROM TSIUSU WHERE CODUSU = P_CODUSU;

       SELECT MAX(NUACERTO) INTO P_NUACERTO FROM TGFFRE;

        IF PARAM_P_VLRAUTOMATIC = 'S' THEN
            
            FOR IDX IN 1 .. P_QTDLINHAS LOOP
            
                FIELD_NUFIN := ACT_INT_FIELD(P_IDSESSAO, IDX, 'NUFIN');

                SELECT DHBAIXA,RECDESP INTO P_BAIXA, P_RECDESP FROM TGFFIN WHERE NUFIN = FIELD_NUFIN;


                IF P_BAIXA IS NOT NULL OR P_RECDESP <> 1 THEN

                                        RAISE_APPLICATION_ERROR(-20009,'Somente Titulos de Receitas não baixados podem ser parcelados!');

                END IF;

  IF P_BAIXA IS NULL AND P_RECDESP = 1 THEN 
        
                    -- Busca o valor total
                    SELECT VLRDESDOB,DTNEG
                      INTO P_VALOR_TOTAL,P_NEG
                      FROM TGFFIN
                     WHERE NUFIN = FIELD_NUFIN;
    
                    SELECT CASE 
                             WHEN NVL(PARAM_P_JUROS,'N') = 'S' THEN 
                                  P_VALOR_TOTAL +
    
                                  CASE 
                                      WHEN PARAM_P_FORMAJUROS = 'P' 
                                          THEN P_VALOR_TOTAL * (PARAM_P_TAXAJUROS / 100)
    
                                      WHEN PARAM_P_FORMAJUROS = 'V' 
                                          THEN PARAM_P_TAXAJUROS
                                  END
                             ELSE 
                                  P_VALOR_TOTAL
                           END
                    INTO P_VALOR_TOTAL
                    FROM DUAL;
    
    
               SELECT CASE 
                 WHEN PARAM_P_DTNEG = 'NO' THEN P_NEG
                 WHEN PARAM_P_DTNEG = 'H'  THEN TRUNC(SYSDATE)
                 ELSE P_NEG   -- evita ficar NULL
                       END
                INTO P_NEGOCI
                FROM DUAL;
     
            RAISE_APPLICATION_ERROR(-20009,'Somente Titulos de Receitas não baixados podem ser parcelados!'||P_NEGOCI);
                    -- Divide igualmente
                    P_VALOR := ROUND(P_VALOR_TOTAL / PARAM_P_NROPARCELAS, 2);
            
                    -- Percorre cada parcela
                    FOR PARC IN 1 .. PARAM_P_NROPARCELAS LOOP
                        
                        -- Calcula vencimento conforme range
                        SELECT PARAM_P_BASEVENCIMEN + (PARC - 1) * NVL(PARAM_P_RANGE, 30)
                          INTO P_VENCNOVO
                          FROM DUAL;
            
                        IF PARC = 1 THEN
                            -- Atualiza parcela 1 no próprio título
                            UPDATE TGFFIN
                               SET VLRDESDOB       = P_VALOR,
                                   HISTORICO       = 'PARCELA '||PARC||'/'||PARAM_P_NROPARCELAS,
                                   AD_PARCELADO    = 'S',
                                   AD_FINANORIGINAL = FIELD_NUFIN,
                                   DTVENC          = P_VENCNOVO,
                                   DTNEG           = P_NEGOCI,
                                   DESDOBRAMENTO   = PARC,
                                   AD_USUDEPARCELAMENTO = P_USUARIO,
                                   AD_DTOPERPARC = SYSDATE
    
                             WHERE NUFIN = FIELD_NUFIN;

                         INSERT INTO TGFFRE (CODUSU,DHALTER,NUACERTO,NUFIN,NUFINORIG,NUNOTA,SEQUENCIA,TIPACERTO ) 
                         VALUES ( STP_GET_CODUSULOGADO,SYSDATE,P_NUACERTO+1,P_MAXSEQ_DESP+1,NULL,NULL,1,'A' ); COMMIT;
        
                    ELSE
                        -- Gera novo NUFIN para a próxima parcela
                         SELECT MAX(NUFIN)+1 INTO P_NVNUFIN FROM TGFFIN;
        
                        INSERT INTO TGFFIN
                        (
                            NUFIN, CODEMP, NUMNOTA, SERIENOTA, DTNEG, DESDOBRAMENTO, DHMOV,
                            DTVENCINIC, DTVENC, CODPARC, CODTIPOPER, DHTIPOPER, CODBCO,
                            CODCTABCOINT, HISTORICO, CODNAT, CODCENCUS, CODPROJ, CODVEND,
                            CODMOEDA, CODTIPTIT, VLRDESDOB, VLRVENDOR, VLRIRF, VLRISS,
                            DESPCART, ISSRETIDO, VLRDESC, VLRMULTA, VLRINSS, TIPMULTA,
                            VLRJURO, TIPJURO, BASEICMS, ALIQICMS, CODTIPOPERBAIXA,
                            DHTIPOPERBAIXA, VLRBAIXA, AUTORIZADO, RECDESP, PROVISAO,
                            ORIGEM, TIPMARCCHEQ, NUNOTA, RATEADO, DTENTSAI, VLRPROV,
                            IRFRETIDO, INSSRETIDO, CARTAODESC, DTALTER, NUMCONTRATO,
                            ORDEMCARGA, CODVEICULO, CODUSU, SEQUENCIA,
                            AD_FINANORIGINAL, AD_PARCELADO,AD_USUDEPARCELAMENTO,AD_DTOPERPARC
                        )
                        SELECT 
                            P_NVNUFIN, CODEMP, NUMNOTA, SERIENOTA, P_NEGOCI, PARC,
                            DHMOV, DTVENCINIC, P_VENCNOVO, CODPARC, CODTIPOPER,
                            DHTIPOPER, CODBCO, CODCTABCOINT,
                            'PARCELA '||PARC||'/'||PARAM_P_NROPARCELAS,
                            CODNAT, CODCENCUS, CODPROJ, CODVEND, CODMOEDA,
                            CODTIPTIT, P_VALOR, VLRVENDOR, VLRIRF, VLRISS,
                            DESPCART, ISSRETIDO, VLRDESC, VLRMULTA, VLRINSS,
                            TIPMULTA, VLRJURO, TIPJURO, BASEICMS, ALIQICMS,
                            CODTIPOPERBAIXA, DHTIPOPERBAIXA, VLRBAIXA, AUTORIZADO,
                            RECDESP, PROVISAO, ORIGEM, TIPMARCCHEQ, NUNOTA, RATEADO,
                            DTENTSAI, VLRPROV, IRFRETIDO, INSSRETIDO, CARTAODESC,
                            DTALTER, NUMCONTRATO, ORDEMCARGA, CODVEICULO, CODUSU,
                            SEQUENCIA,
                            FIELD_NUFIN, 'S',P_USUARIO,SYSDATE
                        FROM TGFFIN
                        WHERE NUFIN = FIELD_NUFIN;
        
                    END IF;
        
                END LOOP;
        
                END IF;

            END LOOP;
        

        END IF;


       IF PARAM_P_VLRAUTOMATIC = 'N' THEN

             SELECT REGEXP_SUBSTR(PARAM_P_DESCRPARCELA, '[^;]+', 1, LEVEL)
             BULK COLLECT INTO P_LISTA
             FROM DUAL
             CONNECT BY REGEXP_SUBSTR(PARAM_P_DESCRPARCELA, '[^;]+', 1, LEVEL) IS NOT NULL;

             IF P_LISTA.COUNT <> PARAM_P_NROPARCELAS THEN
                RAISE_APPLICATION_ERROR(-20001,
                   'A QUANTIDADE DE PARCELAS INFORMADA (' || P_LISTA.COUNT ||
                   ') É DIFERENTE DA QUANTIDADE ESPERADA (' || PARAM_P_NROPARCELAS || ').');
             END IF;

             ---------------------------------------------------------------------
             -- CAPTURA VALORES E PARCELAS + VALIDAÇÕES BÁSICAS
             ---------------------------------------------------------------------
             P_PARCELAS.DELETE;
             V_TOTAL_PARCELAS := 0;

             FOR IDX IN 1 .. P_LISTA.COUNT LOOP
                 P_PARCELA := REGEXP_SUBSTR(P_LISTA(IDX), '^[^=]+');
                 P_VALOR_RAW := REGEXP_SUBSTR(P_LISTA(IDX), '[^=]+$', 1, 1);
                 P_VALOR_RAW := REPLACE(REPLACE(P_VALOR_RAW, '.', ''), ',', '.');
                 P_VALOR := TO_NUMBER(P_VALOR_RAW);

                 -- IMPEDIR PARCELA = 0
                 IF P_PARCELA = 0 THEN
                    RAISE_APPLICATION_ERROR(-20004, 'A PARCELA NÃO PODE SER ZERO.');
                 END IF;

                 -- IMPEDIR PARCELA NEGATIVA
                 IF P_PARCELA < 0 THEN
                    RAISE_APPLICATION_ERROR(-20005, 'A PARCELA NÃO PODE SER NEGATIVA.');
                 END IF;

                 -- PARCELA MAIOR QUE O LIMITE INFORMADO
                 IF P_PARCELA > PARAM_P_NROPARCELAS THEN
                    RAISE_APPLICATION_ERROR(-20006,
                     'A PARCELA ' || P_PARCELA ||
                     ' É MAIOR QUE O NÚMERO MÁXIMO INFORMADO (' || PARAM_P_NROPARCELAS || ').');
                 END IF;

                 P_PARCELAS.EXTEND;
                 P_PARCELAS(P_PARCELAS.COUNT) := P_PARCELA;

                 V_TOTAL_PARCELAS := V_TOTAL_PARCELAS + P_VALOR;
             END LOOP;

             -- COLETA VALOR DO TÍTULO

             SELECT SUM(VLRDESDOB)
             INTO V_VALOR_TITULO
             FROM (
                   SELECT ACT_INT_FIELD(P_IDSESSAO, ROWNUM, 'NUFIN') NU
                   FROM DUAL CONNECT BY LEVEL <= P_QTDLINHAS
                  ) A
             JOIN TGFFIN F ON F.NUFIN = A.NU;

             IF V_TOTAL_PARCELAS <> V_VALOR_TITULO THEN
                RAISE_APPLICATION_ERROR(-20007,
                   'A SOMA DAS PARCELAS (' || V_TOTAL_PARCELAS ||
                   ') É DIFERENTE DO VALOR TOTAL DO TÍTULO (' || V_VALOR_TITULO || ').');
             END IF;

             -- VERIFICAR PARCELAS DUPLICADAS

             FOR I IN 1 .. P_PARCELAS.COUNT LOOP
                FOR J IN I+1 .. P_PARCELAS.COUNT LOOP
                    IF P_PARCELAS(I) = P_PARCELAS(J) THEN
                       RAISE_APPLICATION_ERROR(-20008,
                         'EXISTE PARCELA DUPLICADA: ' || P_PARCELAS(I));
                    END IF;
                END LOOP;
             END LOOP;

             -- VALIDAR SEQUÊNCIA (PARCELAS DEVEM SER 1,2,3...)

             FOR I IN 1 .. P_PARCELAS.COUNT LOOP
                IF P_PARCELAS(I) <> I THEN
                    RAISE_APPLICATION_ERROR(-20009,
                       'AS PARCELAS DEVEM SER SEQUENCIAIS (1..N). ERRO NA PARCELA '||P_PARCELAS(I));
                END IF;
             END LOOP;

             -- EXECUÇÃO NORMAL

             FOR IDX IN 1 .. P_LISTA.COUNT LOOP
                FOR I IN 1..P_QTDLINHAS LOOP

                     FIELD_NUFIN := ACT_INT_FIELD(P_IDSESSAO, I, 'NUFIN');

                    SELECT DHBAIXA,RECDESP INTO P_BAIXA, P_RECDESP FROM TGFFIN WHERE NUFIN = FIELD_NUFIN;

                IF P_BAIXA IS NOT NULL OR P_RECDESP <> 1 THEN

                                        RAISE_APPLICATION_ERROR(-20009,'Somente Titulos de Receitas não baixados podem ser parcelados!');

                END IF;

                IF P_BAIXA IS NULL AND P_RECDESP = 1 THEN 

                     P_LINHA   := P_LISTA(IDX);
                     P_PARCELA := REGEXP_SUBSTR(P_LINHA, '^[^=]+');
                     P_VALOR_RAW := REGEXP_SUBSTR(P_LISTA(IDX), '[^=]+$', 1, 1);
                     P_VALOR_RAW := REPLACE(REPLACE(P_VALOR_RAW, '.', ''), ',', '.');
                     P_VALOR := TO_NUMBER(P_VALOR_RAW);

                     SELECT MAX(NUFIN)+1 INTO P_NVNUFIN FROM TGFFIN;

                     SELECT CASE 
                              WHEN NVL(PARAM_P_JUROS,'N') = 'S' THEN 
                                   P_VALOR +
                                   CASE 
                                       WHEN PARAM_P_FORMAJUROS = 'P' 
                                           THEN P_VALOR * (PARAM_P_TAXAJUROS / 100)
                                       WHEN PARAM_P_FORMAJUROS = 'V' 
                                           THEN PARAM_P_TAXAJUROS
                                   END
                              ELSE 
                                   P_VALOR
                            END
                     INTO P_VALOR
                     FROM DUAL;

                     SELECT CASE WHEN PARAM_P_DTNEG = 'NO' THEN P_NEG WHEN PARAM_P_DTNEG = 'H' THEN TRUNC(SYSDATE) END INTO P_NEGOCI FROM DUAL;

                     SELECT PARAM_P_BASEVENCIMEN + P_PARCELA*NVL(PARAM_P_RANGE,30)
                     INTO P_VENCNOVO FROM DUAL;

                     UPDATE TGFFIN
                        SET VLRDESDOB = P_VALOR,
                            HISTORICO = 'PARCELA '||1||'/'||PARAM_P_NROPARCELAS,AD_PARCELADO ='S',AD_FINANORIGINAL = FIELD_NUFIN,
                            DTVENC    = P_VENCNOVO,
                            DTNEG     = P_NEGOCI,
                            DESDOBRAMENTO = P_PARCELA,
                            AD_USUDEPARCELAMENTO = P_USUARIO,
                            AD_DTOPERPARC = SYSDATE

                      WHERE NUFIN = FIELD_NUFIN;

                     IF P_PARCELA > 1 THEN
                        INSERT INTO TGFFIN
                        (NUFIN, CODEMP, NUMNOTA, SERIENOTA, DTNEG, DESDOBRAMENTO, DHMOV,
                         DTVENCINIC, DTVENC, CODPARC, CODTIPOPER, DHTIPOPER, CODBCO,
                         CODCTABCOINT, HISTORICO, CODNAT, CODCENCUS, CODPROJ, CODVEND,
                         CODMOEDA, CODTIPTIT, VLRDESDOB, VLRVENDOR, VLRIRF, VLRISS,
                         DESPCART, ISSRETIDO, VLRDESC, VLRMULTA, VLRINSS, TIPMULTA,
                         VLRJURO, TIPJURO, BASEICMS, ALIQICMS, CODTIPOPERBAIXA,
                         DHTIPOPERBAIXA, VLRBAIXA, AUTORIZADO, RECDESP, PROVISAO,
                         ORIGEM, TIPMARCCHEQ, NUNOTA, RATEADO, DTENTSAI, VLRPROV,
                         IRFRETIDO, INSSRETIDO, CARTAODESC, DTALTER, NUMCONTRATO,
                         ORDEMCARGA, CODVEICULO, CODUSU, SEQUENCIA, VLRDESCEMBUT,
                         VLRJUROEMBUT, VLRMULTAEMBUT, VLRMOEDA, VLRMOEDABAIXA,
                         VLRMULTANEGOC, VLRJURONEGOC, VLRMULTALIB, VLRJUROLIB,
                         NRODOCTEF, TPAGNFCE, BLOQVAR, VLRHONOR, DTPRAZO,
                         TIMBLOQUEADA, TIMTXADMGERALU,AD_FINANORIGINAL,AD_PARCELADO,AD_USUDEPARCELAMENTO,AD_DTOPERPARC)


                        (SELECT P_NVNUFIN, CODEMP, NUMNOTA, SERIENOTA, P_NEGOCI, P_PARCELA,
                                DHMOV, DTVENCINIC, P_VENCNOVO, CODPARC, CODTIPOPER,
                                DHTIPOPER, CODBCO, CODCTABCOINT,
                                'PARCELA '||P_PARCELA||'/'||PARAM_P_NROPARCELAS,
                                CODNAT, CODCENCUS, CODPROJ, CODVEND, CODMOEDA,
                                CODTIPTIT, P_VALOR, VLRVENDOR, VLRIRF, VLRISS,
                                DESPCART, ISSRETIDO, VLRDESC, VLRMULTA, VLRINSS,
                                TIPMULTA, VLRJURO, TIPJURO, BASEICMS, ALIQICMS,
                                CODTIPOPERBAIXA, DHTIPOPERBAIXA, VLRBAIXA, AUTORIZADO,
                                RECDESP, PROVISAO, ORIGEM, TIPMARCCHEQ, NUNOTA, RATEADO,
                                DTENTSAI, VLRPROV, IRFRETIDO, INSSRETIDO, CARTAODESC,
                                DTALTER, NUMCONTRATO, ORDEMCARGA, CODVEICULO, CODUSU,
                                SEQUENCIA, VLRDESCEMBUT, VLRJUROEMBUT, VLRMULTAEMBUT,
                                VLRMOEDA, VLRMOEDABAIXA, VLRMULTANEGOC, VLRJURONEGOC,
                                VLRMULTALIB, VLRJUROLIB, NRODOCTEF, TPAGNFCE, BLOQVAR,
                                VLRHONOR, DTPRAZO, TIMBLOQUEADA, TIMTXADMGERALU,FIELD_NUFIN,'S',P_USUARIO,SYSDATE
                        FROM TGFFIN
                        WHERE NUFIN = FIELD_NUFIN);

                        COMMIT;
                     END IF;

                END IF;

                END LOOP;
             END LOOP;

             P_MENSAGEM := 'PARCELAMENTO EXECUTADO COM SUCESSO!';

       END IF;

END;
/