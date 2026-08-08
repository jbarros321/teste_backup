-- ============================================================================
-- TRIGGER: TRG_INC_UPD_TGFITE_REGRA_PD
-- ============================================================================
-- TABELA   : TGFITE (Itens de Notas Fiscais/Negociações)
-- EVENTOS  : BEFORE INSERT OR UPDATE (FOR EACH ROW)
--
-- OBJETIVO PRINCIPAL:
--   Esta trigger implementa regras de validação e cálculo automático de preços
--   para produtos que utilizam preço por cargo. Ela garante a integridade dos
--   dados e calcula automaticamente o preço final considerando margens, prazos
--   e cargas tributárias.
--
-- FUNCIONALIDADES:
--   (1) Valida se produtos com preço por cargo têm cargo informado
--   (2) Garante que unidades informadas sejam compatíveis com o cargo
--   (3) Converte automaticamente quantidades quando necessário (ex: DI -> HR)
--   (4) Calcula preço final considerando:
--       - Preço base do cargo (ou unidade alternativa)
--       - Para SERVIÇOS: Fator de margem de lucro (FATORMARGLUCSER, padrão: 2,00)
--       - Para PRODUTOS: Margem de lucro (AD_MARGLUCPROD do cabeçalho)
--       - Fator de prazo de pagamento (2% a cada 30 dias)
--       - Carga tributária do produto (APENAS para produtos, não para serviços)
--   (5) Bloqueia inclusão/alteração se configurações estiverem incompletas
--   (6) Ajusta quantidade automaticamente para itens com unidade DI
--
-- CONTEXTO DE USO:
--   Esta trigger é utilizada quando:
--   * Produtos têm flag AD_PRECOCARGO = 'S' (usam preço por cargo)
--   * O TOP (Tipo de Operação) usa "Preço de Venda" (USARPRECOCUSTO = 'N')
--   * É necessário calcular preços dinamicamente baseado em regras de negócio
--
-- REGRAS DE VALIDAÇÃO:
--   * Campo AD_NUCARGO é obrigatório para produtos com preço por cargo
--   * Unidade do item deve ser HR (padrão) ou unidade alternativa cadastrada
--   * Cargo deve ter preço cadastrado em AD_CARGOS
--   * Se unidade alternativa, deve ter DIVMULT e QTDHORAS configurados
--
-- CÁLCULO DE PREÇO:
--   Esta trigger calcula o preço EXATAMENTE como a procedure STP_PRECO_DINAMICO:
--   
--   Para SERVIÇOS com cargo (USOPROD = 'S' e AD_PRECOCARGO = 'S'):
--     Preço Final = Preço Base × FATORMARGLUCSER
--     IMPORTANTE: FATORMARGLUCSER é aplicado SEMPRE (independente de margem de serviço)
--     IMPORTANTE: Carga tributária NÃO é aplicada para serviços
--   
--   Para SERVIÇOS sem cargo (USOPROD = 'S' e AD_PRECOCARGO <> 'S'):
--     Preço Final = Preço Base / (1 - (AD_MARGLUCSERV / 100))
--     IMPORTANTE: Carga tributária NÃO é aplicada para serviços
--   
--   Para PRODUTOS (USOPROD <> 'S'):
--     Preço Final = Preço Base / (1 - (Margem Total / 100))
--     Onde: Margem Total = Margem Produto + Carga Tributária
--   
--   Onde:
--     Preço Base = Preço do cargo (HR) ou preço alternativo (outras unidades)
--     FATORMARGLUCSER = Fator configurável (padrão: 2,00) obtido do parâmetro do sistema
--     Carga Tributária = Só aplicada para PRODUTOS (USOPROD <> 'S')
--
-- OBSERVAÇÕES IMPORTANTES:
--   * Trigger BEFORE: Modifica valores antes de inserir/atualizar
--   * Usa Fpodevalidar('TGFITE') para sincronização de dados
--   * Bloqueia operação com RAISE_APPLICATION_ERROR se validação falhar
--   * Ajusta automaticamente :NEW.VLRUNIT e :NEW.VLRTOT
--   * IMPORTANTE: Recalcula preço quando:
--     - INSERT: se AD_NUCARGO estiver preenchido
--     - UPDATE: se AD_NUCARGO foi alterado (OLD.AD_NUCARGO <> NEW.AD_NUCARGO)
-- ============================================================================
CREATE OR REPLACE TRIGGER "SANKHYA"."TRG_INC_UPD_TGFITE_REGRA_PD" 
FOR INSERT OR UPDATE ON TGFITE
COMPOUND TRIGGER

    TYPE T_NUNOTAS IS TABLE OF TGFCAB.NUNOTA%TYPE INDEX BY PLS_INTEGER;
    G_NUNOTAS T_NUNOTAS;
    G_COUNT PLS_INTEGER := 0;

    TYPE T_PROCESSED IS TABLE OF BOOLEAN INDEX BY PLS_INTEGER;
    G_PROCESSED T_PROCESSED;

    BEFORE EACH ROW IS
        P_PRECOCARGO          TGFPRO.AD_PRECOCARGO%TYPE := 'N';
        P_USARPRECOCUSTO      TGFTOP.USARPRECOCUSTO%TYPE;
        P_UN_PRODUTO          TGFPRO.CODVOL%TYPE;
        P_CODVOLALT_CARGO     AD_CARGOSUNALT.CODVOLALT%TYPE;
        P_DIVMULT_CARGO       AD_CARGOSUNALT.DIVMULT%TYPE;
        P_QTDHORAS_CARGO      AD_CARGOSUNALT.QTDHORAS%TYPE;
        P_PRECO_CARGO         AD_CARGOS.VALOR%TYPE := 0;
        P_PRECO_ALTERNATIVO   NUMBER := NULL;
        P_MARGLUCPROD         TGFCAB.AD_MARGLUCPROD%TYPE;
        P_MARGLUCSERV         TGFCAB.AD_MARGLUCSERV%TYPE;
        P_USOPROD             TGFPRO.USOPROD%TYPE := 'P';
        P_CARGATRIBUTARIA     NUMBER := 0;
        P_CODTIPVENDA         TGFCAB.CODTIPVENDA%TYPE;
        P_CUSTO_BASE          NUMBER := 0;
        P_PRECO_BASE          NUMBER := 0;
        P_VLRTABELA           NUMBER := 0;
        P_NUTAB               NUMBER := 0;
        P_CODTABPRECO         NUMBER := 0;
        P_FATOR_CUSTOS_INDIRETOS NUMBER := 0.8285;
        P_CODEMP              TGFCAB.CODEMP%TYPE;
        P_CODLOCAL            NUMBER;
        P_PRAZO_MAX           NUMBER := 0;
        P_FATOR_PRAZO         NUMBER := 1;
        P_DIAS_CONSID         NUMBER := 0;
        P_QTD_INC_PRAZO       NUMBER := 0;
        P_INC_PRAZO_PCT       NUMBER := 0;
        P_MIN_DIAS_COD_PRAZO  NUMBER := 2;
        P_FATOR_MARGEM_LUCRO_SERV NUMBER := 2.00;
        P_MARGEM_TOTAL        NUMBER := 0;
        P_PRECO_FINAL         NUMBER := 0;
        P_CARGO_ALTERADO      BOOLEAN := FALSE;
        P_DIASTRAB            TGFCAB.AD_DIASTRAB%TYPE;
        P_DEVE_RECALCULAR     BOOLEAN := FALSE;
        P_VLRUNIT_ORIGINAL    NUMBER := 0;
        ERRMSG                VARCHAR2(4000);
        ERROR                 EXCEPTION;
        P_VALIDAR             BOOLEAN;
    BEGIN
        P_VALIDAR := Fpodevalidar('TGFITE');
        
        -- Captura valor original do VLRUNIT antes de qualquer cálculo
        IF UPDATING THEN
            P_VLRUNIT_ORIGINAL := NVL(:OLD.VLRUNIT, 0);
        ELSE
            P_VLRUNIT_ORIGINAL := NVL(:NEW.VLRUNIT, 0);
        END IF;
        
        -- Verifica se deve recalcular o preço:
        -- - INSERT: recalcula se AD_NUCARGO estiver preenchido
        -- - UPDATE: recalcula se AD_NUCARGO foi alterado
        IF UPDATING THEN
            -- Verifica se AD_NUCARGO foi alterado
            IF NVL(:OLD.AD_NUCARGO, 0) <> NVL(:NEW.AD_NUCARGO, 0) THEN
                P_DEVE_RECALCULAR := TRUE;
            ELSE
                P_DEVE_RECALCULAR := FALSE;
            END IF;
        ELSE
            -- INSERT: recalcula se AD_NUCARGO estiver preenchido
            IF :NEW.AD_NUCARGO IS NOT NULL THEN
                P_DEVE_RECALCULAR := TRUE;
            ELSE
                P_DEVE_RECALCULAR := FALSE;
            END IF;
        END IF;

        BEGIN
      SELECT NVL(AD_PRECOCARGO, 'N'),
             NVL(USOPROD, 'P'),
             NVL(AD_CARGATRIBUTARIA, 0)
        INTO P_PRECOCARGO,
             P_USOPROD,
             P_CARGATRIBUTARIA
        FROM TGFPRO
       WHERE CODPROD = :NEW.CODPROD;
        EXCEPTION 
      WHEN NO_DATA_FOUND THEN
        P_PRECOCARGO := 'N';
        P_USOPROD := 'P';
        P_CARGATRIBUTARIA := 0;
        END;

        IF NVL(P_PRECOCARGO, 'N') = 'S' THEN

            -- Validações sempre devem ser executadas
            IF :NEW.AD_NUCARGO IS NULL THEN
                ERRMSG := 'O campo Nro. Cargo é obrigatório para produtos com preço por cargo.';
                RAISE ERROR;
            END IF;
            
            -- Recalcula preço se:
            -- - INSERT: AD_NUCARGO estiver preenchido
            -- - UPDATE: AD_NUCARGO foi alterado
            IF P_DEVE_RECALCULAR THEN

            BEGIN
      SELECT VALOR
            INTO P_PRECO_CARGO
            FROM AD_CARGOS
       WHERE NUCARGO = :NEW.AD_NUCARGO;
            EXCEPTION
      WHEN NO_DATA_FOUND THEN
              ERRMSG := 'Cargo ' || :NEW.AD_NUCARGO || ' não possui valor cadastrado em AD_CARGOS.';
              RAISE ERROR;
            END;
            
            -- DEBUG: Para verificar o preço do cargo, execute:
            -- SELECT VALOR FROM AD_CARGOS WHERE NUCARGO = :NEW.AD_NUCARGO;

            BEGIN
      SELECT CODVOLALT,
                 DIVMULT,
                 QTDHORAS
            INTO P_CODVOLALT_CARGO,
                 P_DIVMULT_CARGO,
                 P_QTDHORAS_CARGO
            FROM AD_CARGOSUNALT
       WHERE NUCARGO = :NEW.AD_NUCARGO
             AND CODVOLALT = :NEW.CODVOL;
            EXCEPTION
      WHEN NO_DATA_FOUND THEN
              P_CODVOLALT_CARGO := NULL;
              P_DIVMULT_CARGO := NULL;
              P_QTDHORAS_CARGO := NULL;
            END;

            IF P_CODVOLALT_CARGO IS NOT NULL THEN
      IF P_DIVMULT_CARGO = 'M' THEN
              IF P_QTDHORAS_CARGO IS NOT NULL THEN
                  P_PRECO_ALTERNATIVO := P_PRECO_CARGO * P_QTDHORAS_CARGO;
              ELSE
                  ERRMSG := 'O cargo ' || :NEW.AD_NUCARGO ||
                            ' não possui QTDHORAS configurado para a unidade alternativa ' || :NEW.CODVOL || '.';
                  RAISE ERROR;
              END IF;
      ELSE
              IF P_QTDHORAS_CARGO IS NOT NULL AND P_QTDHORAS_CARGO > 0 THEN
                  P_PRECO_ALTERNATIVO := P_PRECO_CARGO / P_QTDHORAS_CARGO;
              ELSE
                  ERRMSG := 'O cargo ' || :NEW.AD_NUCARGO ||
                            ' não possui QTDHORAS válido (deve ser maior que zero) para a unidade alternativa ' || :NEW.CODVOL || '.';
                  RAISE ERROR;
              END IF;
      END IF;
  ELSIF :NEW.CODVOL <> 'HR' THEN
      ERRMSG := 'Para utilizar a unidade ' || :NEW.CODVOL ||
                    ' é necessário cadastrar a unidade alternativa do tela de Cadastro de Cargos.';
      RAISE ERROR;
  END IF;

            BEGIN
    SELECT TOP.USARPRECOCUSTO 
      INTO P_USARPRECOCUSTO
      FROM TGFTOP TOP
      INNER JOIN TGFCAB CAB ON TOP.CODTIPOPER = CAB.CODTIPOPER
                                AND TOP.DHALTER = CAB.DHTIPOPER
    WHERE CAB.NUNOTA = :NEW.NUNOTA;
            EXCEPTION 
      WHEN NO_DATA_FOUND THEN
            ERRMSG := 'Tipo de operação não encontrado para a nota ' || :NEW.NUNOTA || '.';
            RAISE ERROR;
            END;

            BEGIN
    SELECT NVL(CAB.AD_MARGLUCPROD, 0),
               NVL(CAB.AD_MARGLUCSERV, 0),
               CAB.CODTIPVENDA,
               CAB.AD_DIASTRAB
      INTO P_MARGLUCPROD,
               P_MARGLUCSERV,
               P_CODTIPVENDA,
               P_DIASTRAB
      FROM TGFCAB CAB
     WHERE CAB.NUNOTA = :NEW.NUNOTA;
            EXCEPTION
    WHEN NO_DATA_FOUND THEN
            P_MARGLUCPROD := 0;
            P_MARGLUCSERV := 0;
            P_CODTIPVENDA := NULL;
            P_DIASTRAB := NULL;
            END;

            IF :NEW.QTDNEG <> P_DIASTRAB AND :NEW.CODVOL = 'DI' THEN
    :NEW.QTDNEG := P_DIASTRAB;
  END IF;

            IF P_USARPRECOCUSTO = 'N' THEN

    IF :NEW.AD_NUCARGO IS NULL THEN
      ERRMSG := 'Para o tipo de operação configurado, o campo: AD_NUCARGO é obrigatório.';
      RAISE ERROR;
    END IF;

    P_UN_PRODUTO := :NEW.CODVOL;

    IF P_UN_PRODUTO <> 'HR' THEN
      IF P_CODVOLALT_CARGO IS NULL OR P_CODVOLALT_CARGO = '' THEN
            ERRMSG := 'A unidade do produto (' || P_UN_PRODUTO ||
                      ') não corresponde à unidade padrão do cargo (HR)' ||
                      ' e não há unidade alternativa cadastrada no cargo.';
            RAISE ERROR;
      ELSIF P_UN_PRODUTO <> P_CODVOLALT_CARGO THEN
            ERRMSG := 'A unidade do produto (' || P_UN_PRODUTO ||
                      ') não corresponde à unidade padrão do cargo (HR)' ||
                      ' nem à unidade alternativa (' || P_CODVOLALT_CARGO || ').';
            RAISE ERROR;
      ELSIF P_DIVMULT_CARGO IS NULL OR P_QTDHORAS_CARGO IS NULL THEN
            ERRMSG := 'O cargo ' || :NEW.AD_NUCARGO ||
                      ' não possui configuração de conversão (Multiplica/Divide ou Quantidade).';
            RAISE ERROR;
      END IF;
    END IF;

  END IF;

            -- Define preço base do cargo (alinhado com procedure)
            IF :NEW.CODVOL = 'HR' THEN
      P_PRECO_BASE := P_PRECO_CARGO;
  ELSE
      P_PRECO_BASE := P_PRECO_ALTERNATIVO;
  END IF;

            IF NVL(P_PRECO_BASE, 0) = 0 THEN
      ERRMSG := 'Não foi possível determinar o preço para o cargo ' || :NEW.AD_NUCARGO ||
                    ' na unidade ' || :NEW.CODVOL || '. Verifique as configurações do cargo.';
      RAISE ERROR;
  END IF;

            IF P_CODTIPVENDA IS NOT NULL THEN
      SELECT NVL(MAX(P.PRAZO), 0)
            INTO P_PRAZO_MAX
            FROM TGFPPG P
       WHERE P.CODTIPVENDA = P_CODTIPVENDA;
  END IF;

            BEGIN
      SELECT INTEIRO
            INTO P_MIN_DIAS_COD_PRAZO
            FROM TSIPAR
       WHERE CHAVE = 'MINDIASCODPRAZO';
            EXCEPTION
      WHEN NO_DATA_FOUND THEN
              P_MIN_DIAS_COD_PRAZO := 2;
            END;

            BEGIN
      SELECT NUMDEC
            INTO P_FATOR_MARGEM_LUCRO_SERV
            FROM TSIPAR
       WHERE CHAVE = 'FATORMARGLUCSER';
            EXCEPTION
      WHEN NO_DATA_FOUND THEN
              P_FATOR_MARGEM_LUCRO_SERV := 2.00;
            END;

            IF P_PRAZO_MAX >= P_MIN_DIAS_COD_PRAZO THEN
      P_DIAS_CONSID := P_PRAZO_MAX - (P_MIN_DIAS_COD_PRAZO - 1);
      P_QTD_INC_PRAZO := CEIL(P_DIAS_CONSID / 30);
      P_FATOR_PRAZO := 1 + P_QTD_INC_PRAZO * 0.02;
      P_INC_PRAZO_PCT := P_QTD_INC_PRAZO * 2;
  ELSE
      P_FATOR_PRAZO := 1;
      P_INC_PRAZO_PCT := 0;
  END IF;
            
            IF P_USOPROD = 'S' THEN
      IF P_PRECOCARGO = 'S' AND P_PRECO_BASE > 0 THEN
              -- Serviço com preço por cargo: aplicar margem de serviço configurada
              -- Se houver margem de serviço configurada (AD_MARGLUCSERV), aplicar margem percentual
              -- Caso contrário (NULL ou 0), usar FATORMARGLUCSER como multiplicador direto
              -- IMPORTANTE: Usar exatamente a mesma lógica da procedure STP_PRECO_DINAMICO
              IF NVL(P_MARGLUCSERV, 0) > 0 THEN
                  P_MARGEM_TOTAL := P_MARGLUCSERV;
                  -- Aplicar margem percentual
                  P_PRECO_BASE := P_PRECO_BASE / (1 - (P_MARGEM_TOTAL / 100));
              ELSE
                  -- Quando AD_MARGLUCSERV está NULL ou 0, usar FATORMARGLUCSER como multiplicador
                  -- Exemplo: Preço base DI (643,05) * FATORMARGLUCSER (2,00) = 1.286,10
                  P_PRECO_BASE := P_PRECO_BASE * P_FATOR_MARGEM_LUCRO_SERV;
              END IF;

              -- CORRIGIDO: Carga tributária NÃO se aplica para serviços (USOPROD = 'S')
              -- A carga tributária (AD_CARGATRIBUTARIA) só se aplica para produtos (USOPROD <> 'S')
      ELSE
              -- CORRIGIDO: Para serviços sem cargo, não incluir carga tributária na margem total
              P_MARGEM_TOTAL := P_MARGLUCSERV;

              IF P_MARGEM_TOTAL > 0 THEN
                  P_PRECO_BASE := P_PRECO_BASE / (1 - (P_MARGEM_TOTAL / 100));
              END IF;
      END IF;
  ELSE
      P_MARGEM_TOTAL := P_MARGLUCPROD + P_CARGATRIBUTARIA;

      IF P_MARGEM_TOTAL > 0 THEN
              P_PRECO_BASE := P_PRECO_BASE / (1 - (P_MARGEM_TOTAL / 100));
      END IF;
  END IF;

        P_PRECO_FINAL := ROUND(P_PRECO_BASE, 2);
        
        :NEW.VLRUNIT := P_PRECO_FINAL;

        -- Cálculo de VLRTOT conforme fórmula da planilha
        IF P_USOPROD = 'S' THEN
            -- SERVIÇOS: VLRTOT = ((VLRUNIT * QTDNEG) - VLRDESC) + VALOR_TGFDIN
            DECLARE
                V_SUBTOTAL NUMBER;
                V_VALOR_TGFDIN NUMBER := 0;
            BEGIN
                --V_SUBTOTAL := (P_PRECO_FINAL * NVL(:NEW.QTDNEG, 1)) - NVL(:NEW.VLRDESC, 0);
                V_SUBTOTAL := (P_PRECO_FINAL * NVL(:NEW.QTDNEG, 1));

                BEGIN
                    SELECT NVL(SUM(D.VALOR), 0)
                      INTO V_VALOR_TGFDIN
                      FROM TGFDIN D
                     WHERE D.NUNOTA = :NEW.NUNOTA
                       AND D.SEQUENCIA = :NEW.SEQUENCIA;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        V_VALOR_TGFDIN := 0;
                END;

                --:NEW.VLRTOT := ROUND(V_SUBTOTAL + V_VALOR_TGFDIN, 2);
                :NEW.VLRTOT := ROUND(V_SUBTOTAL, 2);

            END;
        ELSE
            -- PRODUTOS: VLRTOT = ((VLRUNIT * QTDNEG) - VLRDESC) * (1 + AD_CARGATRIBUTARIA / 100)
            :NEW.VLRTOT := ROUND(
                ((P_PRECO_FINAL * NVL(:NEW.QTDNEG, 1)) - NVL(:NEW.VLRDESC, 0)) * 
                (1 + NVL(P_CARGATRIBUTARIA, 0) / 100), 
                2
            );
        END IF;
            END IF; -- Fim do IF P_DEVE_RECALCULAR para produtos com preço por cargo
        END IF;

        IF NVL(P_PRECOCARGO, 'N') <> 'S' AND NVL(P_USOPROD, 'P') <> 'S' THEN
            
            -- Só recalcula preço se AD_NUCARGO foi alterado (ou é INSERT)
            IF P_DEVE_RECALCULAR THEN

            BEGIN
                SELECT NVL(CAB.AD_MARGLUCPROD, 0),
                       CAB.CODTIPVENDA,
                       CAB.CODEMP
                  INTO P_MARGLUCPROD,
                       P_CODTIPVENDA,
                       P_CODEMP
                  FROM TGFCAB CAB
                 WHERE CAB.NUNOTA = :NEW.NUNOTA;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    P_MARGLUCPROD := 0;
                    P_CODTIPVENDA := NULL;
                    P_CODEMP := 1;
            END;
            
            -- CODTAB não está disponível diretamente em TGFCAB na trigger
            -- Usa 0 como padrão (o código já trata esse caso)
            P_CODTABPRECO := 0;

            P_CODLOCAL := NVL(:NEW.CODLOCALORIG, 0);

            BEGIN
                SELECT NUMDEC
                  INTO P_FATOR_CUSTOS_INDIRETOS
                  FROM TSIPAR
                 WHERE CHAVE = 'FATORCUSINDERET';
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    P_FATOR_CUSTOS_INDIRETOS := 0.8285;
            END;

            -- CORRIGIDO: Alinhado com a procedure - Primeiro tenta obter preço via STP_OBTEM_PRECO2
            BEGIN
                IF P_CODTABPRECO > 0 THEN
                    SELECT MAX(NUTAB)
                      INTO P_NUTAB
                      FROM TGFTAB
                     WHERE CODTAB = P_CODTABPRECO
                       AND DTVIGOR <= TRUNC(SYSDATE);
                ELSE
                    SELECT MAX(NUTAB)
                      INTO P_NUTAB
                      FROM TGFTAB
                     WHERE DTVIGOR <= TRUNC(SYSDATE);
                END IF;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    BEGIN
                        IF P_CODTABPRECO > 0 THEN
                            SELECT NVL(MAX(NUTAB), 1)
                              INTO P_NUTAB
                              FROM TGFTAB
                             WHERE CODTAB = P_CODTABPRECO;
                        ELSE
                            SELECT NVL(MAX(NUTAB), 1)
                              INTO P_NUTAB
                              FROM TGFTAB;
                        END IF;
                    EXCEPTION
                        WHEN OTHERS THEN
                            P_NUTAB := 1;
                    END;
            END;

            STP_OBTEM_PRECO2(P_NUTAB, :NEW.CODPROD, TRUNC(SYSDATE), P_PRECO_BASE);

            -- Depois tenta obter custo via OBTEMCUSTO (se obtiver, sobrescreve o preço base)
            BEGIN
                P_VLRTABELA := OBTEMCUSTO(
                    P_CODPROD => :NEW.CODPROD,
                    P_POREMP => CASE WHEN P_CODEMP > 0 THEN 'S' ELSE 'N' END,
                    P_CODEMP => P_CODEMP,
                    P_PORLOCAL => CASE WHEN P_CODLOCAL > 0 THEN 'S' ELSE 'N' END,
                    P_CODLOCAL => P_CODLOCAL,
                    P_PORCONTROLE => CASE WHEN :NEW.CONTROLE IS NOT NULL AND TRIM(:NEW.CONTROLE) != '' THEN 'S' ELSE 'N' END,
                    P_CONTROLE => :NEW.CONTROLE,
                    P_DATA => TRUNC(SYSDATE),
                    P_TIPO => 0
                );
            EXCEPTION
                WHEN OTHERS THEN
                    P_VLRTABELA := 0;
            END;

            -- CORRIGIDO: Se obteve valor via OBTEMCUSTO, sobrescreve o preço base (alinhado com procedure)
            IF P_VLRTABELA > 0 THEN
                P_PRECO_BASE := P_VLRTABELA;
            ELSIF P_PRECO_BASE = 0 THEN
                NULL;
            END IF;

            -- CORRIGIDO: Aplica fator de custos indiretos sobre qualquer preço base final (alinhado com procedure)
            IF P_PRECO_BASE > 0 THEN
                P_PRECO_BASE := P_PRECO_BASE * P_FATOR_CUSTOS_INDIRETOS;

                P_MARGEM_TOTAL := P_MARGLUCPROD + P_CARGATRIBUTARIA;

                IF P_MARGEM_TOTAL > 0 THEN
                    P_PRECO_FINAL := P_PRECO_BASE / (1 - (P_MARGEM_TOTAL / 100));
                ELSE
                    P_PRECO_FINAL := P_PRECO_BASE;
                END IF;

                P_PRECO_FINAL := ROUND(P_PRECO_FINAL, 2);
                :NEW.VLRUNIT := P_PRECO_FINAL;

                -- Cálculo de VLRTOT conforme fórmula da planilha
                -- PRODUTOS: VLRTOT = ((VLRUNIT * QTDNEG) - VLRDESC) * (1 + AD_CARGATRIBUTARIA / 100)
                :NEW.VLRTOT := ROUND(
                    ((P_PRECO_FINAL * NVL(:NEW.QTDNEG, 1)) - NVL(:NEW.VLRDESC, 0)) * 
                    (1 + NVL(P_CARGATRIBUTARIA, 0) / 100), 
                    2
                );
            ELSE
                -- Se não conseguiu obter preço base, mas o produto já tem VLRUNIT,
                -- aplica a carga tributária no VLRTOT mesmo assim
                IF NVL(:NEW.VLRUNIT, 0) > 0 THEN
                    -- Cálculo de VLRTOT conforme fórmula da planilha
                    -- PRODUTOS: VLRTOT = ((VLRUNIT * QTDNEG) - VLRDESC) * (1 + AD_CARGATRIBUTARIA / 100)
                    :NEW.VLRTOT := ROUND(
                        ((:NEW.VLRUNIT * NVL(:NEW.QTDNEG, 1)) - NVL(:NEW.VLRDESC, 0)) * 
                        (1 + NVL(P_CARGATRIBUTARIA, 0) / 100), 
                        2
                    );
                END IF;
            END IF;
            END IF; -- Fim do IF P_DEVE_RECALCULAR para produtos sem preço por cargo
        END IF;

        IF :NEW.NUNOTA IS NOT NULL AND NOT G_PROCESSED.EXISTS(:NEW.NUNOTA) THEN
            G_COUNT := G_COUNT + 1;
            G_NUNOTAS(G_COUNT) := :NEW.NUNOTA;
            G_PROCESSED(:NEW.NUNOTA) := TRUE;
        END IF;

    EXCEPTION
  WHEN OTHERS THEN
     IF (P_VALIDAR) THEN
       IF SQLCODE <> 1 THEN
         ERRMSG := ERRMSG || '  ' || SQLERRM;
       END IF;
       RAISE_APPLICATION_ERROR(-20101, ERRMSG);       
     END IF;
    END BEFORE EACH ROW;

    AFTER STATEMENT IS
        V_NUNOTA         TGFCAB.NUNOTA%TYPE;
        V_TOTAL_ITENS    NUMBER := 0;
        V_VLRNOTA_ATUAL  TGFCAB.VLRNOTA%TYPE;
    BEGIN
        FOR I IN 1 .. G_COUNT LOOP
            V_NUNOTA := G_NUNOTAS(I);

            BEGIN
                SELECT CAB.VLRNOTA
                  INTO V_VLRNOTA_ATUAL
                  FROM TGFCAB CAB
                 WHERE CAB.NUNOTA = V_NUNOTA
                   FOR UPDATE;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    CONTINUE;
            END;

            BEGIN
                SELECT NVL(SUM(ITE.VLRTOT), 0)
                  INTO V_TOTAL_ITENS
                  FROM TGFITE ITE
                 WHERE ITE.NUNOTA = V_NUNOTA;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    V_TOTAL_ITENS := 0;
            END;

            -- Atualiza VLRNOTA com a soma direta dos itens (conforme fórmula da planilha)
            -- Sem incremento de prazo, pois a planilha não considera esse ajuste
            IF ABS(V_VLRNOTA_ATUAL - V_TOTAL_ITENS) > 0.01 THEN
                UPDATE TGFCAB
                   SET VLRNOTA = ROUND(V_TOTAL_ITENS, 2)
                 WHERE NUNOTA = V_NUNOTA;
            END IF;
        END LOOP;

        G_NUNOTAS.DELETE;
        G_PROCESSED.DELETE;
        G_COUNT := 0;
    EXCEPTION
        WHEN OTHERS THEN
            G_NUNOTAS.DELETE;
            G_PROCESSED.DELETE;
            G_COUNT := 0;
            NULL;
    END AFTER STATEMENT;

END;

/