-- ============================================================================
-- PROCEDURE: STP_PRECO_DINAMICO
-- ============================================================================
-- OBJETIVO PRINCIPAL:
--   Esta procedure calcula o preço dinâmico de um produto ou serviço considerando
--   múltiplos fatores de negócio que impactam o valor final. É utilizada para
--   calcular preços de venda que consideram margens, prazos de pagamento e
--   cargas tributárias de forma automatizada.
--
-- CONTEXTO DE USO:
--   Esta procedure é tipicamente chamada durante a digitação de itens em notas
--   fiscais ou orçamentos, quando o sistema precisa calcular automaticamente o
--   preço de venda baseado em regras de negócio configuráveis.
--
-- FATORES CONSIDERADOS NO CÁLCULO:
--   1. Preço base da tabela de preços (TGFTAB/TGFITE)
--   2. Valor do cargo cadastrado (se o produto usa preço por cargo)
--   3. Margem de lucro bruta configurada no cabeçalho (AD_MARGLUCPROD/AD_MARGLUCSERV)
--   4. Fator de prazo de pagamento (acréscimo de 2% a cada 30 dias)
--   5. Carga tributária do produto (AD_CARGATRIBUTARIA) - APENAS para produtos (USOPROD <> 'S')
--
-- FÓRMULA DE CÁLCULO:
--   Para PRODUTOS (USOPROD <> 'S'):
--     Preço Final = Preço Base / (1 - (Margem Total / 100))
--     Onde: Margem Total = Margem Produto + Carga Tributária
--   
--   Para SERVIÇOS com cargo (USOPROD = 'S' e AD_PRECOCARGO = 'S'):
--     Preço Final = Preço Base × FATORMARGLUCSER
--     IMPORTANTE: Carga tributária NÃO é aplicada para serviços
--   
--   Para SERVIÇOS sem cargo (USOPROD = 'S' e AD_PRECOCARGO <> 'S'):
--     Preço Final = Preço Base / (1 - (Margem Serviço / 100))
--     IMPORTANTE: Carga tributária NÃO é aplicada para serviços
--
--   Incremento Prazo = 2% a cada 30 dias acima do mínimo configurado (não aplicado ao preço)
--
-- FLUXO DE EXECUÇÃO:
--   1. Obtém dados do formulário através da sessão (P_IDSESSAO)
--   2. Busca preço base na tabela de preços vigente
--   3. Verifica se produto usa preço por cargo e ajusta se necessário
--   4. Obtém margens de lucro do cabeçalho da nota
--   5. Calcula fator de ajuste por prazo de pagamento
--   6. Aplica todas as margens e retorna preço final em formato JSON
--
-- DEPENDÊNCIAS:
--   - Tabela TGFTAB: Tabelas de preços
--   - Tabela TGFPRO: Cadastro de produtos
--   - Tabela TGFCAB: Cabeçalho de notas (margens)
--   - Tabela TGFPPG: Condições de pagamento (prazo)
--   - Tabela TSIPAR: Parâmetros do sistema (MINDIASCODPRAZO, FATORCUSINDERET, FATORMARGLUCSER)
--   - Tabela AD_CARGOS: Cargos e seus valores (personalização)
--   - Tabela AD_CARGOSUNALT: Unidades alternativas dos cargos (personalização)
--   - Procedure STP_OBTEM_PRECO2: Obtém preço da tabela de preços
--   - Funções ACT_DEC_FIELD e ACT_TXT_FIELD: Acessam campos do formulário
--
-- PARÂMETROS:
--   P_IDSESSAO (IN VARCHAR2)
--     * ID da sessão do usuário no sistema Sankhya
--     * Utilizado para acessar valores dos campos do formulário atual
--     * Obtido através de funções ACT_DEC_FIELD e ACT_TXT_FIELD
--
--   P_RESULT (OUT VARCHAR2)
--     * JSON com o resultado do cálculo
--     * Formato: {"PRECO":"valor", "FATOR_PRAZO":"valor"}
--     * Exemplo: {"PRECO":"150.50", "FATOR_PRAZO":"1.06"}
--
-- RETORNO JSON:
--   {
--     "PRECO": "150.50",        -- Preço final calculado (2 casas decimais)
--     "FATOR_PRAZO": "1.06"     -- Fator de ajuste por prazo (1.06 = +6%)
--   }
--
-- EXEMPLO DE USO:
--   DECLARE
--     V_RESULT VARCHAR2(4000);
--   BEGIN
--     STP_PRECO_DINAMICO('SESSAO123', V_RESULT);
--     DBMS_OUTPUT.PUT_LINE(V_RESULT);
--     -- Saída: {"PRECO":"150.50", "FATOR_PRAZO":"1.06"}
--   END;
--
-- OBSERVAÇÕES IMPORTANTES:
--   * A procedure diferencia produtos de serviços para aplicar margens corretas
--   * Produtos usam AD_MARGLUCPROD, serviços usam AD_MARGLUCSERV
--   * Carga tributária (AD_CARGATRIBUTARIA) só se aplica para PRODUTOS (USOPROD <> 'S')
--   * Para SERVIÇOS (USOPROD = 'S'), a carga tributária NÃO é aplicada no cálculo
--   * O cálculo de prazo considera dias acima do mínimo configurado (MINDIASCODPRAZO)
--   * O fator de prazo é calculado mas NÃO é aplicado ao preço final (apenas informativo)
--   * Se não houver margem configurada, assume 0% (sem acréscimo)
--   * O preço é arredondado para 2 casas decimais
-- ============================================================================

CREATE OR REPLACE PROCEDURE STP_PRECO_DINAMICO (
    P_IDSESSAO IN  VARCHAR2,
    P_RESULT   OUT VARCHAR2
) AS
    V_CODPROD               NUMBER := NVL(ACT_DEC_FIELD(P_IDSESSAO, 0, 'CODPROD'), 0);
    V_CODVOL                VARCHAR2(10) := ACT_TXT_FIELD(P_IDSESSAO, 0, 'CODVOL');
    V_CONTROLE              VARCHAR2(60) := ACT_TXT_FIELD(P_IDSESSAO, 0, 'CONTROLE');
    V_CODTABPRECO           NUMBER := NVL(ACT_DEC_FIELD(P_IDSESSAO, 0, 'CODTAB'), 0);
    V_CODTIPVENDA           NUMBER := NVL(ACT_DEC_FIELD(P_IDSESSAO, 0, 'CODTIPVENDA'), 0);
    V_NUNOTA                NUMBER := NVL(ACT_DEC_FIELD(P_IDSESSAO, 0, 'NUNOTA'), 0);
    V_CODLOCAL              NUMBER := NVL(ACT_DEC_FIELD(P_IDSESSAO, 0, 'CODLOCAL'), 0);
    V_CODEMP                NUMBER := NVL(ACT_DEC_FIELD(P_IDSESSAO, 0, 'CODEMP'), 0);
    V_SEQUENCIA             NUMBER := NVL(ACT_DEC_FIELD(P_IDSESSAO, 0, 'SEQUENCIA'), 0);
    V_NUTAB                 NUMBER := 0;
    V_PRECO_CARGO           NUMBER := 0;
    V_PRECO_BASE            NUMBER := 0;
    V_PRAZO_MAX             NUMBER := 0;
    V_FATOR_PRAZO           NUMBER := 1;
    V_DIAS_CONSID           NUMBER := 0;
    V_QTD_INC_PRAZO         NUMBER := 0;
    V_INC_PRAZO_PCT         NUMBER := 0;
    V_MARGLUCPROD           NUMBER := 0;
    V_MARGLUCSERV           NUMBER := 0;
    V_MARGEM_TOTAL          NUMBER := 0;
    V_FLAG_PRECO            VARCHAR2(1) := 'N';
    V_USOPROD               VARCHAR2(1) := 'P';
    V_CARGATRIBUTARIA       NUMBER := 0;
    V_NUCARGO               NUMBER := 0;
    V_NUCARGO_FORM          NUMBER := NVL(ACT_DEC_FIELD(P_IDSESSAO, 0, 'AD_NUCARGO'), 0);
    V_MIN_DIAS_COD_PRAZO    NUMBER := 2;
    V_VLRTABELA             NUMBER := 0;
    V_FATOR_CUSTOS_INDIRETOS NUMBER := 0.8285;
    V_FATOR_MARGEM_LUCRO_SERV NUMBER := 2.00;
BEGIN
    BEGIN
        IF V_CODTABPRECO > 0 THEN
            SELECT MAX(NUTAB)
              INTO V_NUTAB
              FROM TGFTAB
             WHERE CODTAB = V_CODTABPRECO
               AND DTVIGOR <= TRUNC(SYSDATE);
        ELSE
            SELECT MAX(NUTAB)
              INTO V_NUTAB
              FROM TGFTAB
             WHERE DTVIGOR <= TRUNC(SYSDATE);
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            BEGIN
                IF V_CODTABPRECO > 0 THEN
                    SELECT NVL(MAX(NUTAB), 1)
                      INTO V_NUTAB
                      FROM TGFTAB
                     WHERE CODTAB = V_CODTABPRECO;
                ELSE
                    SELECT NVL(MAX(NUTAB), 1)
                      INTO V_NUTAB
                      FROM TGFTAB;
                END IF;
            EXCEPTION
                WHEN OTHERS THEN
                    V_NUTAB := 1;
            END;
    END;


    BEGIN
        SELECT NVL(AD_PRECOCARGO, 'N'),
               NVL(USOPROD, 'P'),
               NVL(AD_CARGATRIBUTARIA, 0)
          INTO V_FLAG_PRECO,
               V_USOPROD,
               V_CARGATRIBUTARIA
          FROM TGFPRO
         WHERE CODPROD = V_CODPROD;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            V_FLAG_PRECO := 'N';
            V_USOPROD := 'P';
            V_CARGATRIBUTARIA := 0;
    END;
    
    IF V_FLAG_PRECO = 'S' AND V_USOPROD = 'S' THEN
        DECLARE
            V_NUCARGO_OBTIDO NUMBER;
        BEGIN
            -- Obter NUCARGO do formulário
            IF V_NUCARGO_FORM > 0 THEN
                V_NUCARGO_OBTIDO := V_NUCARGO_FORM;
            ELSIF V_SEQUENCIA > 0 THEN
                V_NUCARGO_OBTIDO := NVL(ACT_DEC_FIELD(P_IDSESSAO, V_SEQUENCIA, 'AD_NUCARGO'), 0);
            ELSE
                V_NUCARGO_OBTIDO := NVL(ACT_DEC_FIELD(P_IDSESSAO, 0, 'AD_NUCARGO'), 0);
            END IF;
            
            -- Se não encontrou no formulário e tem NUNOTA, buscar no TGFITE
            IF V_NUCARGO_OBTIDO = 0 AND V_NUNOTA > 0 THEN
                BEGIN
                    IF V_SEQUENCIA > 0 THEN
                        SELECT NVL(ITE.AD_NUCARGO, 0)
                          INTO V_NUCARGO_OBTIDO
                          FROM TGFITE ITE
                         WHERE ITE.NUNOTA = V_NUNOTA
                           AND ITE.SEQUENCIA = V_SEQUENCIA
                           AND ITE.CODPROD = V_CODPROD
                           AND ROWNUM = 1;
                    ELSE
                        SELECT NVL(ITE.AD_NUCARGO, 0)
                          INTO V_NUCARGO_OBTIDO
                          FROM (
                              SELECT ITE.AD_NUCARGO
                                FROM TGFITE ITE
                               WHERE ITE.NUNOTA = V_NUNOTA
                                 AND ITE.CODPROD = V_CODPROD
                               ORDER BY ITE.SEQUENCIA DESC
                          ) ITE
                         WHERE ROWNUM = 1;
                    END IF;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        V_NUCARGO_OBTIDO := 0;
                END;
            END IF;
            
            -- Se ainda não encontrou, usar padrão 1
            IF V_NUCARGO_OBTIDO = 0 THEN
                V_NUCARGO_OBTIDO := 1;
            END IF;
            
            -- Calcular preço base do cargo
            BEGIN
                IF V_CODVOL = 'HR' THEN
                    SELECT AD.VALOR
                      INTO V_PRECO_BASE
                      FROM AD_CARGOS AD
                     WHERE AD.NUCARGO = V_NUCARGO_OBTIDO;
                ELSE
                    SELECT CASE 
                             WHEN ALT.DIVMULT = 'M' THEN AD.VALOR * ALT.QTDHORAS 
                             ELSE AD.VALOR / NVL(ALT.QTDHORAS, 1) 
                           END
                      INTO V_PRECO_BASE
                      FROM AD_CARGOS AD
                      INNER JOIN AD_CARGOSUNALT ALT ON AD.NUCARGO = ALT.NUCARGO
                     WHERE AD.NUCARGO = V_NUCARGO_OBTIDO
                       AND ALT.CODVOLALT = V_CODVOL;
                END IF;
                
                V_NUCARGO := V_NUCARGO_OBTIDO;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    -- Se não encontrou o cargo, usar preço da tabela de preços
                    STP_OBTEM_PRECO2(V_NUTAB, V_CODPROD, TRUNC(SYSDATE), V_PRECO_BASE);
            END;
        END;
    ELSE
        STP_OBTEM_PRECO2(V_NUTAB, V_CODPROD, TRUNC(SYSDATE), V_PRECO_BASE);
        
        BEGIN
            V_VLRTABELA := OBTEMCUSTO(
                P_CODPROD => V_CODPROD,
                P_POREMP => CASE WHEN V_CODEMP > 0 THEN 'S' ELSE 'N' END,
                P_CODEMP => V_CODEMP,
                P_PORLOCAL => CASE WHEN V_CODLOCAL > 0 THEN 'S' ELSE 'N' END,
                P_CODLOCAL => V_CODLOCAL,
                P_PORCONTROLE => CASE WHEN V_CONTROLE IS NOT NULL AND TRIM(V_CONTROLE) != '' THEN 'S' ELSE 'N' END,
                P_CONTROLE => V_CONTROLE,
                P_DATA => TRUNC(SYSDATE),
                P_TIPO => 0
            );
        EXCEPTION
            WHEN OTHERS THEN
                V_VLRTABELA := 0;
        END;
        
        IF V_VLRTABELA > 0 THEN
            V_PRECO_BASE := V_VLRTABELA;
        ELSIF V_PRECO_BASE = 0 THEN
            NULL;
        END IF;
    END IF;

    IF V_NUNOTA > 0 THEN
        BEGIN
            SELECT NVL(CAB.AD_MARGLUCPROD, 0),
                   NVL(CAB.AD_MARGLUCSERV, 0)
              INTO V_MARGLUCPROD,
                   V_MARGLUCSERV
              FROM TGFCAB CAB
             WHERE CAB.NUNOTA = V_NUNOTA;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                V_MARGLUCPROD := 0;
                V_MARGLUCSERV := 0;
        END;
    ELSE
        V_MARGLUCPROD := NVL(ACT_DEC_FIELD(P_IDSESSAO, 0, 'AD_MARGLUCPROD'), 0);
        V_MARGLUCSERV := NVL(ACT_DEC_FIELD(P_IDSESSAO, 0, 'AD_MARGLUCSERV'), 0);
    END IF;
    
    BEGIN
        SELECT NUMDEC
          INTO V_FATOR_CUSTOS_INDIRETOS
          FROM TSIPAR
         WHERE CHAVE = 'FATORCUSINDERET';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            V_FATOR_CUSTOS_INDIRETOS := 0.8285;
    END;
    
    IF V_PRECO_BASE > 0 AND V_FLAG_PRECO <> 'S' AND V_USOPROD <> 'S' THEN
        V_PRECO_BASE := V_PRECO_BASE * V_FATOR_CUSTOS_INDIRETOS;
    END IF;

    SELECT NVL(MAX(P.PRAZO), 0)
      INTO V_PRAZO_MAX
      FROM TGFPPG P
     WHERE P.CODTIPVENDA = V_CODTIPVENDA;

    BEGIN
        SELECT INTEIRO
          INTO V_MIN_DIAS_COD_PRAZO
          FROM TSIPAR
         WHERE CHAVE = 'MINDIASCODPRAZO';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            V_MIN_DIAS_COD_PRAZO := 2;
    END;
    
    BEGIN
        SELECT NUMDEC
          INTO V_FATOR_MARGEM_LUCRO_SERV
          FROM TSIPAR
         WHERE CHAVE = 'FATORMARGLUCSER';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            V_FATOR_MARGEM_LUCRO_SERV := 2.00;
    END;

    IF V_PRAZO_MAX >= V_MIN_DIAS_COD_PRAZO THEN
        V_DIAS_CONSID := V_PRAZO_MAX - (V_MIN_DIAS_COD_PRAZO - 1);
        V_QTD_INC_PRAZO := CEIL(V_DIAS_CONSID / 30);
        V_FATOR_PRAZO := 1 + V_QTD_INC_PRAZO * 0.02;
        V_INC_PRAZO_PCT := V_QTD_INC_PRAZO * 2;
    END IF;

    IF V_USOPROD = 'S' THEN
        IF V_FLAG_PRECO = 'S' AND V_PRECO_BASE > 0 THEN
            -- Serviço com preço por cargo: aplicar margem de serviço configurada
            IF V_MARGLUCSERV IS NOT NULL AND V_MARGLUCSERV > 0 THEN
                V_MARGEM_TOTAL := V_MARGLUCSERV;
                IF V_MARGEM_TOTAL > 0 THEN
                    V_PRECO_BASE := V_PRECO_BASE / (1 - (V_MARGEM_TOTAL / 100));
                END IF;
            ELSE
                IF V_FATOR_MARGEM_LUCRO_SERV > 0 THEN
                    V_PRECO_BASE := V_PRECO_BASE * V_FATOR_MARGEM_LUCRO_SERV;
                    V_MARGEM_TOTAL := 0; -- Não é margem percentual, é multiplicação direta
                ELSE
                    V_MARGEM_TOTAL := 0;
                END IF;
            END IF;
            
            -- IMPORTANTE: Carga tributária NÃO se aplica para serviços (USOPROD = 'S')
            -- A carga tributária (AD_CARGATRIBUTARIA) só se aplica para produtos (USOPROD <> 'S')
        ELSE
            -- Serviço sem preço por cargo: aplicar margem de serviço se configurada
            V_MARGEM_TOTAL := V_MARGLUCSERV;
            
            IF V_MARGEM_TOTAL > 0 THEN
                V_PRECO_BASE := V_PRECO_BASE / (1 - (V_MARGEM_TOTAL / 100));
            END IF;
        END IF;
    ELSE
        V_MARGEM_TOTAL := V_MARGLUCPROD + V_CARGATRIBUTARIA;

        IF V_MARGEM_TOTAL > 0 THEN
            V_PRECO_BASE := V_PRECO_BASE / (1 - (V_MARGEM_TOTAL / 100));
        END IF;
    END IF;

    V_PRECO_BASE := ROUND(V_PRECO_BASE, 2);

    P_RESULT := '{' ||
                '"PRECO":"' || TO_CHAR(V_PRECO_BASE, 'FM9999990.000000', 'NLS_NUMERIC_CHARACTERS = ''.,''') || '",' ||
                '"FATOR_PRAZO":"' || TO_CHAR(V_FATOR_PRAZO, 'FM9990.000000', 'NLS_NUMERIC_CHARACTERS = ''.,''') || '"}';
END;
/
