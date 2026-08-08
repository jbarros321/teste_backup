CREATE OR REPLACE PROCEDURE "STP_EVT_AUDITORIA_PARAMARZ" (
       P_TIPOEVENTO INT,
       P_IDSESSAO VARCHAR2,
       P_CODUSU INT
) AS
       BEFORE_INSERT INT;
       AFTER_INSERT  INT;
       BEFORE_DELETE INT;
       AFTER_DELETE  INT;
       BEFORE_UPDATE INT;
       AFTER_UPDATE  INT;
       BEFORE_COMMIT INT;

       -- Variáveis de controle
       V_SEQ         NUMBER;
       V_ID          NUMBER;
       V_DESCRALT    VARCHAR2(4000);
       V_AUDITALTER  VARCHAR2(4000);

       -- Valores NOVOS (vindos do EVP_GET_CAMPO)
       V_NEW_DESCR           VARCHAR2(4000);
       V_NEW_CODPARC         NUMBER;
       V_NEW_ARMAZENAGEM     NUMBER;
       V_NEW_DESCPALETIZADA  NUMBER;
       V_NEW_CARGPALETIZADA  NUMBER;
       V_NEW_PICKING         NUMBER;
       V_NEW_RECFRIO         NUMBER;
       V_NEW_HREXTRA         NUMBER;
       V_NEW_ISSQN           NUMBER;
       V_NEW_SEGURO          NUMBER;
       V_NEW_OUTROS          NUMBER;

       -- Valores ANTIGOS (buscados via SELECT na tabela)
       V_OLD_DESCR           VARCHAR2(4000);
       V_OLD_CODPARC         NUMBER;
       V_OLD_ARMAZENAGEM     NUMBER;
       V_OLD_DESCPALETIZADA  NUMBER;
       V_OLD_CARGPALETIZADA  NUMBER;
       V_OLD_PICKING         NUMBER;
       V_OLD_RECFRIO         NUMBER;
       V_OLD_HREXTRA         NUMBER;
       V_OLD_ISSQN           NUMBER;
       V_OLD_SEGURO          NUMBER;
       V_OLD_OUTROS          NUMBER;

BEGIN
       BEFORE_INSERT := 0;
       AFTER_INSERT  := 1;
       BEFORE_DELETE := 2;
       AFTER_DELETE  := 3;
       BEFORE_UPDATE := 4;
       AFTER_UPDATE  := 5;
       BEFORE_COMMIT := 10;

       -- ===================== BEFORE INSERT =====================
       IF P_TIPOEVENTO = BEFORE_INSERT THEN

            V_ID := EVP_GET_CAMPO_INT(P_IDSESSAO, 'ID');

            -- Preenche campos de controle
            EVP_SET_CAMPO_DTA(P_IDSESSAO, 'DTINS', SYSDATE);
            EVP_SET_CAMPO_INT(P_IDSESSAO, 'USUINC', P_CODUSU);

            -- Gera sequência
            SELECT NVL(MAX(SEQUENCIA), 0) + 1 INTO V_SEQ FROM AD_HISTALTERARMAZ;

            -- Insere registro de auditoria
            INSERT INTO AD_HISTALTERARMAZ (SEQUENCIA, ID, DHALTER, DESCRALT, USUALTER, AUDITALTER)
            VALUES (V_SEQ, V_ID, SYSDATE, 'Registro Criado', P_CODUSU,
                    'Inserção do registro ID: ' || V_ID);

       END IF;

       -- ===================== BEFORE UPDATE =====================
       IF P_TIPOEVENTO = BEFORE_UPDATE THEN

            V_ID := EVP_GET_CAMPO_INT(P_IDSESSAO, 'ID');

            -- Busca valores NOVOS via sessão do evento
            V_NEW_DESCR          := EVP_GET_CAMPO_TEXTO(P_IDSESSAO, 'DESCR');
            V_NEW_CODPARC        := EVP_GET_CAMPO_INT(P_IDSESSAO, 'CODPARC');
            V_NEW_ARMAZENAGEM    := EVP_GET_CAMPO_DEC(P_IDSESSAO, 'ARMAZENAGEM');
            V_NEW_DESCPALETIZADA := EVP_GET_CAMPO_DEC(P_IDSESSAO, 'DESCPALETIZADA');
            V_NEW_CARGPALETIZADA := EVP_GET_CAMPO_DEC(P_IDSESSAO, 'CARGPALETIZADA');
            V_NEW_PICKING        := EVP_GET_CAMPO_DEC(P_IDSESSAO, 'PICKING');
            V_NEW_RECFRIO        := EVP_GET_CAMPO_DEC(P_IDSESSAO, 'RECFRIO');
            V_NEW_HREXTRA        := EVP_GET_CAMPO_DEC(P_IDSESSAO, 'HREXTRA');
            V_NEW_ISSQN          := EVP_GET_CAMPO_DEC(P_IDSESSAO, 'ISSQN');
            V_NEW_SEGURO         := EVP_GET_CAMPO_DEC(P_IDSESSAO, 'SEGURO');
            V_NEW_OUTROS         := EVP_GET_CAMPO_DEC(P_IDSESSAO, 'OUTROS');

            -- Busca valores ANTIGOS direto na tabela
            SELECT DESCR, CODPARC, ARMAZENAGEM, DESCPALETIZADA, CARGPALETIZADA,
                   PICKING, RECFRIO, HREXTRA, ISSQN, SEGURO, OUTROS
              INTO V_OLD_DESCR, V_OLD_CODPARC, V_OLD_ARMAZENAGEM, V_OLD_DESCPALETIZADA, V_OLD_CARGPALETIZADA,
                   V_OLD_PICKING, V_OLD_RECFRIO, V_OLD_HREXTRA, V_OLD_ISSQN, V_OLD_SEGURO, V_OLD_OUTROS
              FROM AD_PARAMARZ
             WHERE ID = V_ID;

            -- Inicia comparações
            V_DESCRALT   := '';
            V_AUDITALTER := '';

            -- DESCR (texto)
            IF NVL(V_OLD_DESCR, '##') <> NVL(V_NEW_DESCR, '##') THEN
                V_DESCRALT   := V_DESCRALT || 'Descrição, ';
                V_AUDITALTER := V_AUDITALTER || 'Descrição: [' || V_OLD_DESCR || '] -> [' || V_NEW_DESCR || ']' || CHR(10);
            END IF;

            -- CODPARC
            IF NVL(V_OLD_CODPARC, -1) <> NVL(V_NEW_CODPARC, -1) THEN
                V_DESCRALT   := V_DESCRALT || 'Parceiro, ';
                V_AUDITALTER := V_AUDITALTER || 'Parceiro: [' || V_OLD_CODPARC || '] -> [' || V_NEW_CODPARC || ']' || CHR(10);
            END IF;

            -- ARMAZENAGEM
            IF NVL(V_OLD_ARMAZENAGEM, -1) <> NVL(V_NEW_ARMAZENAGEM, -1) THEN
                V_DESCRALT   := V_DESCRALT || 'Armazenagem, ';
                V_AUDITALTER := V_AUDITALTER || 'Armazenagem: [' || V_OLD_ARMAZENAGEM || '] -> [' || V_NEW_ARMAZENAGEM || ']' || CHR(10);
            END IF;

            -- DESCPALETIZADA
            IF NVL(V_OLD_DESCPALETIZADA, -1) <> NVL(V_NEW_DESCPALETIZADA, -1) THEN
                V_DESCRALT   := V_DESCRALT || 'Descarga Paletizada, ';
                V_AUDITALTER := V_AUDITALTER || 'Descarga Paletizada: [' || V_OLD_DESCPALETIZADA || '] -> [' || V_NEW_DESCPALETIZADA || ']' || CHR(10);
            END IF;

            -- CARGPALETIZADA
            IF NVL(V_OLD_CARGPALETIZADA, -1) <> NVL(V_NEW_CARGPALETIZADA, -1) THEN
                V_DESCRALT   := V_DESCRALT || 'Carga Paletizada, ';
                V_AUDITALTER := V_AUDITALTER || 'Carga Paletizada: [' || V_OLD_CARGPALETIZADA || '] -> [' || V_NEW_CARGPALETIZADA || ']' || CHR(10);
            END IF;

            -- PICKING
            IF NVL(V_OLD_PICKING, -1) <> NVL(V_NEW_PICKING, -1) THEN
                V_DESCRALT   := V_DESCRALT || 'Picking, ';
                V_AUDITALTER := V_AUDITALTER || 'Picking: [' || V_OLD_PICKING || '] -> [' || V_NEW_PICKING || ']' || CHR(10);
            END IF;

            -- RECFRIO
            IF NVL(V_OLD_RECFRIO, -1) <> NVL(V_NEW_RECFRIO, -1) THEN
                V_DESCRALT   := V_DESCRALT || 'Recuperação de Frio, ';
                V_AUDITALTER := V_AUDITALTER || 'Recuperação de Frio: [' || V_OLD_RECFRIO || '] -> [' || V_NEW_RECFRIO || ']' || CHR(10);
            END IF;

            -- HREXTRA
            IF NVL(V_OLD_HREXTRA, -1) <> NVL(V_NEW_HREXTRA, -1) THEN
                V_DESCRALT   := V_DESCRALT || 'Hora Extra, ';
                V_AUDITALTER := V_AUDITALTER || 'Hora Extra: [' || V_OLD_HREXTRA || '] -> [' || V_NEW_HREXTRA || ']' || CHR(10);
            END IF;

            -- ISSQN
            IF NVL(V_OLD_ISSQN, -1) <> NVL(V_NEW_ISSQN, -1) THEN
                V_DESCRALT   := V_DESCRALT || 'ISSQN, ';
                V_AUDITALTER := V_AUDITALTER || 'ISSQN: [' || V_OLD_ISSQN || '] -> [' || V_NEW_ISSQN || ']' || CHR(10);
            END IF;

            -- SEGURO
            IF NVL(V_OLD_SEGURO, -1) <> NVL(V_NEW_SEGURO, -1) THEN
                V_DESCRALT   := V_DESCRALT || 'Seguro, ';
                V_AUDITALTER := V_AUDITALTER || 'Seguro: [' || V_OLD_SEGURO || '] -> [' || V_NEW_SEGURO || ']' || CHR(10);
            END IF;

            -- OUTROS
            IF NVL(V_OLD_OUTROS, -1) <> NVL(V_NEW_OUTROS, -1) THEN
                V_DESCRALT   := V_DESCRALT || 'Outros Serviços, ';
                V_AUDITALTER := V_AUDITALTER || 'Outros Serviços: [' || V_OLD_OUTROS || '] -> [' || V_NEW_OUTROS || ']' || CHR(10);
            END IF;

            -- Só insere se houve alteração real
            IF V_DESCRALT IS NOT NULL THEN
                V_DESCRALT := 'Campos alterados: ' || RTRIM(V_DESCRALT, ', ');

                SELECT NVL(MAX(SEQUENCIA), 0) + 1 INTO V_SEQ FROM AD_HISTALTERARMAZ;

                INSERT INTO AD_HISTALTERARMAZ (SEQUENCIA, ID, DHALTER, DESCRALT, USUALTER, AUDITALTER)
                VALUES (V_SEQ, V_ID, SYSDATE, V_DESCRALT, P_CODUSU, V_AUDITALTER);
            END IF;

       END IF;

END;
/
