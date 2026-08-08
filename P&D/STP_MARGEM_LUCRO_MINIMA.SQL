-- ============================================================================
-- PROCEDURE: STP_MARGEM_LUCRO_MINIMA
-- ============================================================================
-- OBJETIVO PRINCIPAL:
--   Esta procedure valida se a margem de lucro bruta informada no cabeçalho
--   da nota fiscal (TGFCAB) atende ao valor mínimo configurado no sistema.
--   É utilizada como regra de negócio para garantir que as notas fiscais
--   respeitem uma margem mínima de lucro estabelecida pela empresa.
--
-- CONTEXTO DE USO:
--   Esta procedure é tipicamente chamada por triggers ou processos que validam
--   a nota fiscal antes de sua finalização. Ela verifica a margem de lucro
--   baseada no tipo de itens da nota:
--   * Se a nota contém itens de SERVIÇO (USOPROD = 'S'), valida AD_MARGLUCSERV
--   * Se a nota contém itens de PRODUTO/MATERIAL (USOPROD <> 'S'), valida AD_MARGLUCPROD
--   * Se contém ambos os tipos, valida ambas as margens
--
-- FLUXO DE EXECUÇÃO:
--   1. Inicializa os parâmetros de retorno com valores padrão (assume sucesso)
--   2. Busca os valores mínimos configurados nos parâmetros MARGLUCROSERV e MARGLUCROPROD (TSIPAR)
--   3. Verifica os itens da nota para determinar se há serviços e/ou produtos
--   4. Obtém as margens de lucro do cabeçalho (AD_MARGLUCSERV e/ou AD_MARGLUCPROD)
--   5. Compara cada margem com seu respectivo mínimo e retorna sucesso ou bloqueio
--
-- DEPENDÊNCIAS:
--   - Tabela TSIPAR: Parâmetros do sistema (deve conter MARGLUCROSERV e MARGLUCROPROD)
--   - Tabela TGFCAB: Cabeçalho de notas fiscais (deve conter AD_MARGLUCSERV e/ou AD_MARGLUCPROD)
--   - Tabela TGFITE: Itens de notas fiscais (para verificar tipos de itens)
--   - Tabela TGFPRO: Cadastro de produtos (para verificar USOPROD)
--
-- PARÂMETROS DE ENTRADA:
--   P_NUNOTA (IN INT)
--     * Número único da nota/negociação que será verificada
--     * Este é o identificador principal da nota na tabela TGFCAB
--     * Deve corresponder a uma nota existente no sistema
--
-- PARÂMETROS DE SAÍDA:
--   P_SUCESSO (OUT VARCHAR2)
--     * Retorna 'S' quando a margem atende ao mínimo configurado
--     * Retorna 'N' quando a margem está abaixo do mínimo
--     * Sempre inicializado como 'S' (otimismo: assume sucesso até provar o contrário)
--
--   P_MENSAGEM (OUT VARCHAR2)
--     * Mensagem descritiva explicando o resultado da validação
--     * NULL quando a validação passa (P_SUCESSO = 'S')
--     * Contém detalhes do bloqueio quando a validação falha (P_SUCESSO = 'N')
--     * Formato da mensagem de erro:
--       "MARGEM LUCRO BRUTA: [valor] INFERIOR AO MÍNIMO CONFIGURADO: [parâmetro] VALOR: [mínimo]"
--
--   P_CODUSULIB (OUT NUMBER)
--     * Código do usuário com permissão para liberar o bloqueio
--     * Reservado para futura implementação de sistema de liberação
--     * Atualmente sempre retorna 0 (não implementado)
--
-- TRATAMENTO DE ERROS:
--   - Se o parâmetro MARGLUCROSERV não existir em TSIPAR (quando há itens de serviço):
--     * Lança exceção com código -20101
--     * Mensagem orienta a criação do parâmetro em Preferências > Novo Parâmetro
--     * Tipo do parâmetro deve ser: Inteiro
--
--   - Se o parâmetro MARGLUCROPROD não existir em TSIPAR (quando há itens de produto):
--     * Lança exceção com código -20101
--     * Mensagem orienta a criação do parâmetro em Preferências > Novo Parâmetro
--     * Tipo do parâmetro deve ser: Inteiro
--
--   - Se a nota (P_NUNOTA) não existir em TGFCAB:
--     * A procedure retorna sem validar (assume sucesso)
--
-- EXEMPLO DE USO:
--   DECLARE
--     V_SUCESSO VARCHAR2(1);
--     V_MSG VARCHAR2(4000);
--     V_CODUSU NUMBER;
--   BEGIN
--     STP_MARGEM_LUCRO_MINIMA(12345, V_SUCESSO, V_MSG, V_CODUSU);
--     IF V_SUCESSO = 'N' THEN
--       DBMS_OUTPUT.PUT_LINE('Bloqueio: ' || V_MSG);
--     END IF;
--   END;
--
-- OBSERVAÇÕES IMPORTANTES:
--   * Esta procedure valida a margem baseada no tipo de itens da nota:
--     - Itens de SERVIÇO (USOPROD = 'S') -> valida AD_MARGLUCSERV contra MARGLUCROSERV
--     - Itens de PRODUTO/MATERIAL (USOPROD <> 'S') -> valida AD_MARGLUCPROD contra MARGLUCROPROD
--   * Se a nota tiver ambos os tipos, ambas as margens são validadas com seus respectivos mínimos
--   * Os valores dos parâmetros MARGLUCROSERV e MARGLUCROPROD devem ser números inteiros
--   * A comparação é estrita: margem < mínimo = bloqueio
--   * Margem igual ao mínimo = permite (não bloqueia)
--   * Cada tipo de item tem seu próprio parâmetro de margem mínima
-- ============================================================================
CREATE OR REPLACE PROCEDURE STP_MARGEM_LUCRO_MINIMA(
    P_NUNOTA       IN  INT,      -- Número único da nota a ser validada
    P_SUCESSO      OUT VARCHAR2, -- 'S' = válido, 'N' = bloqueado
    P_MENSAGEM     OUT VARCHAR2, -- Mensagem explicativa (NULL se válido)
    P_CODUSULIB    OUT NUMBER    -- Código do usuário liberador (futuro)
) AS
    -- ========================================================================
    -- DECLARAÇÃO DE VARIÁVEIS LOCAIS
    -- ========================================================================
    
    -- Nome do parâmetro no sistema que contém o valor mínimo de margem para SERVIÇOS
    -- Este parâmetro deve estar cadastrado em TSIPAR com tipo "Inteiro"
    -- Exemplo: Se MARGLUCROSERV = 10, significa margem mínima de 10% para serviços
    P_PARAMETRO_SERV     VARCHAR2(15) := 'MARGLUCROSERV';
    
    -- Nome do parâmetro no sistema que contém o valor mínimo de margem para PRODUTOS
    -- Este parâmetro deve estar cadastrado em TSIPAR com tipo "Inteiro"
    -- Exemplo: Se MARGLUCROPROD = 8, significa margem mínima de 8% para produtos
    P_PARAMETRO_PROD     VARCHAR2(15) := 'MARGLUCROPROD';
    
    -- Valor mínimo de margem de lucro para SERVIÇOS configurado no parâmetro do sistema
    -- Este valor é obtido da tabela TSIPAR (Parâmetros do Sistema)
    -- Unidade: Percentual (ex: 10 = 10% de margem mínima para serviços)
    P_VALORPARAM_SERV    NUMBER;
    
    -- Valor mínimo de margem de lucro para PRODUTOS configurado no parâmetro do sistema
    -- Este valor é obtido da tabela TSIPAR (Parâmetros do Sistema)
    -- Unidade: Percentual (ex: 8 = 8% de margem mínima para produtos)
    P_VALORPARAM_PROD    NUMBER;
    
    -- Margem de lucro de serviços informada no cabeçalho da nota fiscal
    -- Este valor vem do campo AD_MARGLUCSERV da tabela TGFCAB
    -- Unidade: Percentual (ex: 8 = 8% de margem)
    -- IMPORTANTE: Este campo é uma personalização (AD_ = Adicional)
    P_MARGLUCSERV   NUMBER;
    
    -- Margem de lucro de produtos informada no cabeçalho da nota fiscal
    -- Este valor vem do campo AD_MARGLUCPROD da tabela TGFCAB
    -- Unidade: Percentual (ex: 8 = 8% de margem)
    -- IMPORTANTE: Este campo é uma personalização (AD_ = Adicional)
    P_MARGLUCPROD   NUMBER;
    
    -- Flag que indica se a nota contém itens de serviço
    -- TRUE = Tem itens com USOPROD = 'S', FALSE = Não tem
    P_TEM_SERVICO   BOOLEAN := FALSE;
    
    -- Flag que indica se a nota contém itens de produto/material
    -- TRUE = Tem itens com USOPROD <> 'S', FALSE = Não tem
    P_TEM_PRODUTO   BOOLEAN := FALSE;
    
    -- Variáveis temporárias para contagem (usadas para evitar TRUE/FALSE em SQL)
    V_COUNT_SERV    NUMBER;
    V_COUNT_PROD    NUMBER;
    
BEGIN
    -- ========================================================================
    -- INICIALIZAÇÃO DOS PARÂMETROS DE RETORNO
    -- ========================================================================
    -- Estratégia: Assume sucesso até que uma validação prove o contrário
    -- Isso permite que a procedure retorne rapidamente em caso de sucesso
    -- sem necessidade de processamento adicional
    
    P_SUCESSO   := 'S';   -- Assume que a validação passará
    P_MENSAGEM  := NULL;   -- Sem mensagem = sem problema
    P_CODUSULIB := 0;      -- Sem liberação necessária (campo reservado)

    -- ========================================================================
    -- BLOCO 1: RECUPERA OS VALORES MÍNIMOS CONFIGURADOS NOS PARÂMETROS DO SISTEMA
    -- ========================================================================
    -- Objetivo: Buscar na tabela TSIPAR os valores mínimos de margem configurados
    -- para serviços e produtos separadamente
    -- 
    -- Tabela TSIPAR:
    --   - Armazena parâmetros de configuração do sistema Sankhya
    --   - Campo CHAVE: Nome do parâmetro (ex: 'MARGLUCROSERV', 'MARGLUCROPROD')
    --   - Campo INTEIRO: Valor numérico do parâmetro
    --
    -- IMPORTANTE: Os parâmetros serão buscados apenas quando necessário:
    --   * MARGLUCROSERV: apenas se a nota tiver itens de serviço
    --   * MARGLUCROPROD: apenas se a nota tiver itens de produto
    --   Se o parâmetro necessário não existir, um erro será lançado.

    -- ========================================================================
    -- BLOCO 2: VERIFICA TIPOS DE ITENS DA NOTA (SERVIÇO OU PRODUTO)
    -- ========================================================================
    -- Objetivo: Identificar se a nota contém itens de serviço e/ou produto
    -- para determinar qual(is) margem(ns) deve(m) ser validada(s)
    --
    -- Lógica:
    --   * Se a nota tem itens com USOPROD = 'S' -> valida AD_MARGLUCSERV
    --   * Se a nota tem itens com USOPROD <> 'S' -> valida AD_MARGLUCPROD
    --   * Se tem ambos, valida ambas as margens
    --
    -- Tabela TGFITE:
    --   - Itens de notas fiscais/negociações
    --   - Campo NUNOTA: Número único da nota (chave estrangeira)
    --   - Campo CODPROD: Código do produto (chave estrangeira)
    --
    -- Tabela TGFPRO:
    --   - Cadastro de produtos
    --   - Campo USOPROD: Tipo de uso ('S' = Serviço, outros = Produto/Material)
    -- Verifica se há itens de serviço na nota
    -- COUNT(*) sempre retorna um número (0 ou mais), nunca NO_DATA_FOUND
    -- Oracle não suporta TRUE/FALSE em expressões SQL, então usamos variável NUMBER
    SELECT COUNT(*)
      INTO V_COUNT_SERV
      FROM TGFITE ITE
      INNER JOIN TGFPRO PRO ON PRO.CODPROD = ITE.CODPROD
     WHERE ITE.NUNOTA = P_NUNOTA
       AND NVL(PRO.USOPROD, 'P') = 'S';
    
    -- Converte o resultado numérico para BOOLEAN
    P_TEM_SERVICO := (V_COUNT_SERV > 0);

    -- Verifica se há itens de produto/material na nota
    SELECT COUNT(*)
      INTO V_COUNT_PROD
      FROM TGFITE ITE
      INNER JOIN TGFPRO PRO ON PRO.CODPROD = ITE.CODPROD
     WHERE ITE.NUNOTA = P_NUNOTA
       AND NVL(PRO.USOPROD, 'P') <> 'S';
    
    -- Converte o resultado numérico para BOOLEAN
    P_TEM_PRODUTO := (V_COUNT_PROD > 0);

    -- Se a nota não tiver itens de serviço nem de produto, não há o que validar
    IF NOT P_TEM_SERVICO AND NOT P_TEM_PRODUTO THEN
        RETURN;  -- Retorna sem validar (nota sem itens ou apenas itens inválidos)
    END IF;

    -- ========================================================================
    -- BLOCO 2: RECUPERA OS VALORES MÍNIMOS DOS PARÂMETROS (CONFORME NECESSÁRIO)
    -- ========================================================================
    -- Busca os parâmetros apenas se a nota tiver os respectivos tipos de itens
    
    -- Busca parâmetro de margem mínima para SERVIÇOS (se necessário)
    IF P_TEM_SERVICO THEN
        BEGIN
            SELECT INTEIRO
              INTO P_VALORPARAM_SERV
              FROM TSIPAR
             WHERE CHAVE = P_PARAMETRO_SERV;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(
                    -20101,
                    'O Parâmetro MARGLUCROSERV não foi encontrado! ' ||
                    'Crie o Parâmetro MARGLUCROSERV Tipo: Inteiro em Preferências > Novo Parâmetro.'
                );
        END;
    END IF;

    -- Busca parâmetro de margem mínima para PRODUTOS (se necessário)
    IF P_TEM_PRODUTO THEN
        BEGIN
            SELECT INTEIRO
              INTO P_VALORPARAM_PROD
              FROM TSIPAR
             WHERE CHAVE = P_PARAMETRO_PROD;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(
                    -20101,
                    'O Parâmetro MARGLUCROPROD não foi encontrado! ' ||
                    'Crie o Parâmetro MARGLUCROPROD Tipo: Inteiro em Preferências > Novo Parâmetro.'
                );
        END;
    END IF;

    -- ========================================================================
    -- BLOCO 3: OBTÉM AS MARGENS DE LUCRO DO CABEÇALHO DA NOTA
    -- ========================================================================
    -- Objetivo: Buscar os valores das margens de lucro informadas no
    -- cabeçalho da nota fiscal
    --
    -- Tabela TGFCAB:
    --   - Cabeçalho de notas fiscais/negociações
    --   - Campo NUNOTA: Número único da nota (chave primária)
    --   - Campo AD_MARGLUCSERV: Margem de lucro de serviços (personalização)
    --   - Campo AD_MARGLUCPROD: Margem de lucro de produtos (personalização)
    --
    -- IMPORTANTE: 
    --   * Estes campos são personalizações (prefixo AD_)
    --   * Se a nota não existir, Oracle lançará NO_DATA_FOUND
    BEGIN
        SELECT NVL(CAB.AD_MARGLUCSERV, 0),  -- Margem de lucro de serviços
               NVL(CAB.AD_MARGLUCPROD, 0)   -- Margem de lucro de produtos
          INTO P_MARGLUCSERV,
               P_MARGLUCPROD
          FROM TGFCAB CAB  -- Tabela de cabeçalho de notas
         WHERE CAB.NUNOTA = P_NUNOTA;  -- Filtra pela nota informada
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            -- Se a nota não existir, não há como validar
            -- Retorna sem erro (assume que a validação passará)
            RETURN;
    END;

    -- ========================================================================
    -- BLOCO 4: COMPARA AS MARGENS DA NOTA COM O MÍNIMO PERMITIDO
    -- ========================================================================
    -- Objetivo: Verificar se as margens informadas atendem ao mínimo configurado
    --
    -- Lógica de comparação:
    --   * Se margem < mínimo: BLOQUEIA (P_SUCESSO = 'N')
    --   * Se margem >= mínimo: PERMITE (P_SUCESSO = 'S', já inicializado)
    --
    -- Exemplo:
    --   * Mínimo configurado: 10%
    --   * Margem de serviços da nota: 8%
    --   * Resultado: 8 < 10 = BLOQUEIO
    --
    -- Valida margem de SERVIÇOS se a nota tiver itens de serviço
    -- Compara com o parâmetro específico para serviços (MARGLUCROSERV)
    IF P_TEM_SERVICO AND P_MARGLUCSERV < P_VALORPARAM_SERV THEN
        -- ====================================================================
        -- VALIDAÇÃO FALHOU: Margem de serviços abaixo do mínimo
        -- ====================================================================
        P_SUCESSO  := 'N';  -- Indica que a validação falhou
        
        -- Monta mensagem descritiva com os valores envolvidos
        -- Formato: "MARGEM LUCRO BRUTA SERVIÇOS: [valor] INFERIOR AO MÍNIMO CONFIGURADO: [parâmetro] VALOR: [mínimo]"
        P_MENSAGEM := 'MARGEM LUCRO BRUTA SERVIÇOS: ' || P_MARGLUCSERV || CHR(10) ||
                      'INFERIOR AO MÍNIMO CONFIGURADO: ' || P_PARAMETRO_SERV || ' VALOR: ' || P_VALORPARAM_SERV;
    END IF;

    -- Valida margem de PRODUTOS se a nota tiver itens de produto/material
    -- Compara com o parâmetro específico para produtos (MARGLUCROPROD)
    IF P_TEM_PRODUTO AND P_MARGLUCPROD < P_VALORPARAM_PROD THEN
        -- ====================================================================
        -- VALIDAÇÃO FALHOU: Margem de produtos abaixo do mínimo
        -- ====================================================================
        P_SUCESSO  := 'N';  -- Indica que a validação falhou
        
        -- Se já havia mensagem de erro (serviços), adiciona informação de produtos
        -- Caso contrário, cria nova mensagem
        IF P_MENSAGEM IS NOT NULL THEN
            P_MENSAGEM := P_MENSAGEM || CHR(10) || CHR(10) ||
                          'MARGEM LUCRO BRUTA PRODUTOS: ' || P_MARGLUCPROD || CHR(10) ||
                          'INFERIOR AO MÍNIMO CONFIGURADO: ' || P_PARAMETRO_PROD || ' VALOR: ' || P_VALORPARAM_PROD;
        ELSE
            P_MENSAGEM := 'MARGEM LUCRO BRUTA PRODUTOS: ' || P_MARGLUCPROD || CHR(10) ||
                          'INFERIOR AO MÍNIMO CONFIGURADO: ' || P_PARAMETRO_PROD || ' VALOR: ' || P_VALORPARAM_PROD;
        END IF;
    END IF;
    -- Se as margens forem >= ao mínimo, P_SUCESSO já está como 'S' e P_MENSAGEM como NULL
    -- Não é necessário fazer nada adicional

END;
/