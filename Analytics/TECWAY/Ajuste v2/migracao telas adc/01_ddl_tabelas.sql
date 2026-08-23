/* =====================================================================
   01 - CRIAÇÃO DAS TABELAS NECESSÁRIAS PARA AS TELAS
        DRE / BP / Indicadores / BaseIndicadores
   Destino: tenant_47255 (projeto atual - "Ajuste v2")
   Origem da modelagem: tenant_28263 (projeto "telas adc"),
   lido via REST /rest/v0 em 17/08/2026.

   MySQL 8 (mesma engine das demais tabelas do Analytics).

   BLOCO A - tabelas de ESTRUTURA (cadastro)  -> OBRIGATÓRIO
             carga no 02_dados_estrutura.sql
   BLOCO B - tabelas de VALORES (fatos)       -> OPCIONAL

   Decisão tomada: a base oficial destas telas é o IMP_BASE_BALANCETE deste
   tenant, não a base do projeto de origem. Por isso rode apenas o BLOCO A e
   use as queries 06_query_tela_*_balancete.sql, que leem os valores do
   balancete daqui.

   O BLOCO B só é necessário se um dia optarem por replicar a base da origem
   (aí valem as queries 05_query_tela_*.sql, sem alteração). Detalhes e
   números da verificação no MAPEAMENTO.md.
   ===================================================================== */


/* =====================================================================
   BLOCO A - ESTRUTURA
   7 + 307 + 16.842 + 45 + 22 = 17.223 linhas
   ===================================================================== */

/* ---------------------------------------------------------------------
   ESTR_DRE_TW - cabeçalho da estrutura (qual demonstrativo)
   IDs usados pelas telas:
     1 = DRE Padrão Tecway              -> tela DRE            (queryDRE.sql)
     5 = BP  Padrão Tecway              -> tela BP             (bp_querydados.sql)
     7 = BP  Padrão Tecway - Interno    -> tela Indicadores    (queryDadosBPindicadores.sql)
     9 = DRE Padrão Tecway - Interno    -> tela BaseIndicadores(queryDadosDREindicadores.sql)
   (6, 8 e 10 entram junto por completude do cadastro)
   --------------------------------------------------------------------- */
CREATE TABLE IF NOT EXISTS ESTR_DRE_TW (
    ID                  INT           NOT NULL,
    NOME_DEMONSTRATIVO  VARCHAR(100)  NOT NULL,
    TIPO_DEMONSTRATIVO  VARCHAR(10)   NOT NULL,   -- 'DRE' | 'BP' | 'DFC'
    PRIMARY KEY (ID)
);

/* ---------------------------------------------------------------------
   DET_DRE_TW - linhas de cada demonstrativo
   TIPO      : 'CONTA' (soma as contas ligadas em CAD_CONTA_DRE_TW) ou
               'CALCULO' / 'CALCULO1'..'CALCULO13' (fórmula na coluna CALCULO)
   CALCULO   : fórmula no formato '[2]+[3]-[4]', referenciando ORDEM
   HIERARQUIA: '1.', '1.1.', ... usada para recuo/negrito na tela
   SINAL     : multiplicador (+1/-1)
   RECUO     : recuo extra opcional (tela de indicadores BP)
   --------------------------------------------------------------------- */
CREATE TABLE IF NOT EXISTS DET_DRE_TW (
    ID               INT           NOT NULL,
    ID_ESTR_DRE_TW   INT           NOT NULL,
    ORDEM            INT           NOT NULL,
    DESCRICAO        VARCHAR(255)  NOT NULL,
    TIPO             VARCHAR(20)   NOT NULL,
    CALCULO          VARCHAR(255)  NULL,
    FORMATACAO       VARCHAR(20)   NULL,   -- 'NORMAL' | 'DESTAQUE1' | ...
    HIERARQUIA       VARCHAR(20)   NOT NULL,
    SINAL            INT           NOT NULL DEFAULT 1,
    RECUO            INT           NULL,
    PRIMARY KEY (ID),
    KEY IDX_DET_DRE_TW_ESTR (ID_ESTR_DRE_TW, ORDEM)
);

/* ---------------------------------------------------------------------
   CAD_CONTA_DRE_TW - de/para linha do demonstrativo x conta contábil
   ID_CONTA_CONTABIL usa o mesmo plano de contas de IMP_BASE_BALANCETE.CTACTB
   (validado: 453/453 contas conferem em 12/2024)
   --------------------------------------------------------------------- */
CREATE TABLE IF NOT EXISTS CAD_CONTA_DRE_TW (
    ID                 INT          NOT NULL,
    ID_DET_DRE_TW      INT          NOT NULL,
    ID_CONTA_CONTABIL  VARCHAR(30)  NOT NULL,
    PRIMARY KEY (ID),
    KEY IDX_CAD_CONTA_DRE_TW_DET (ID_DET_DRE_TW),
    KEY IDX_CAD_CONTA_DRE_TW_CTA (ID_CONTA_CONTABIL)
);

/* ---------------------------------------------------------------------
   DET_NOTAS - número da nota explicativa exibido na coluna "Nota"
   das telas de indicadores. MES no formato 'MM/AAAA'; EMPRESA em texto
   (aceita lista, é lida com FIND_IN_SET).
   --------------------------------------------------------------------- */
CREATE TABLE IF NOT EXISTS DET_NOTAS (
    ID              INT           NOT NULL,
    ID_DET_DRE_TW   INT           NOT NULL,
    NOTA            VARCHAR(10)   NOT NULL,
    MES             VARCHAR(7)    NOT NULL,   -- 'MM/AAAA'
    EMPRESA         VARCHAR(50)   NOT NULL,
    DESCRICAO       VARCHAR(255)  NULL,
    PRIMARY KEY (ID),
    KEY IDX_DET_NOTAS_DET (ID_DET_DRE_TW, MES)
);

/* ---------------------------------------------------------------------
   CAD_CONTA_NOTAS_TW - contas que compõem cada nota explicativa.
   Não é usada pelas 4 telas desta importação, mas é dependência do mesmo
   cadastro de notas (a tela de notas usa). Criar junto evita retrabalho.
   --------------------------------------------------------------------- */
CREATE TABLE IF NOT EXISTS CAD_CONTA_NOTAS_TW (
    ID                 INT          NOT NULL,
    ID_DET_NOTAS       INT          NOT NULL,
    ID_CONTA_CONTABIL  VARCHAR(30)  NOT NULL,
    PRIMARY KEY (ID),
    KEY IDX_CAD_CONTA_NOTAS_DET (ID_DET_NOTAS)
);


/* =====================================================================
   BLOCO B - VALORES (fatos)  --  OPCIONAL (ver cabeçalho do arquivo)

   Na origem essas tabelas guardam o lançamento analítico (220.202 linhas em
   DRE_TECWAY e 1.012.398 em BP_TECWAY). As 4 queries só fazem
   SUM(...) GROUP BY (MES, ID_EMPRESA, ID_CONTA_CONTABIL), então aqui elas
   entram JÁ AGREGADAS por essa chave: ~23 mil + ~142 mil linhas
   (52 meses, 01/2022 a 04/2026). As queries continuam funcionando sem
   qualquer alteração.

   Unidade: MILHARES (as queries multiplicam por 1000).
   Semântica:
     DRE_TECWAY = movimento do mês, apenas contas de resultado (3/4/5)
     BP_TECWAY  = saldo do mês, todas as contas (1 a 6); contas de resultado
                  entram com sinal invertido em relação ao razão
   ===================================================================== */

CREATE TABLE IF NOT EXISTS DRE_TECWAY (
    MES                VARCHAR(7)     NOT NULL,   -- 'MM/AAAA'
    ID_EMPRESA         INT            NOT NULL,
    ID_CONTA_CONTABIL  VARCHAR(30)    NOT NULL,
    DRE_TECWAY         DECIMAL(20,6)  NULL,       -- em MILHARES
    PRIMARY KEY (MES, ID_EMPRESA, ID_CONTA_CONTABIL),
    KEY IDX_DRE_TECWAY_CTA (ID_CONTA_CONTABIL, MES)
);

CREATE TABLE IF NOT EXISTS BP_TECWAY (
    MES                VARCHAR(7)     NOT NULL,   -- 'MM/AAAA'
    ID_EMPRESA         INT            NOT NULL,
    ID_CONTA_CONTABIL  VARCHAR(30)    NOT NULL,
    BP_TECWAY          DECIMAL(20,6)  NULL,       -- em MILHARES
    PRIMARY KEY (MES, ID_EMPRESA, ID_CONTA_CONTABIL),
    KEY IDX_BP_TECWAY_CTA (ID_CONTA_CONTABIL, MES)
);

/* ---------------------------------------------------------------------
   Se preferir manter as tabelas idênticas à origem (linha a linha, com
   ID_CR / ID_HISTORICO / ID_NUMERO_DE_LANCAMENTO / NUMLOTE), use o modelo
   abaixo no lugar dos dois CREATEs acima. Nenhuma das 4 queries usa essas
   colunas - é só se houver outro uso previsto.

CREATE TABLE IF NOT EXISTS DRE_TECWAY (
    ID_CR                    INT           NULL,
    ID_EMPRESA               INT           NOT NULL,
    ID_HISTORICO             INT           NULL,
    ID_NUMERO_DE_LANCAMENTO  INT           NULL,
    ID_CONTA_CONTABIL        VARCHAR(30)   NOT NULL,
    DRE_TECWAY               DECIMAL(20,6) NULL,
    MES                      VARCHAR(7)    NOT NULL,
    KEY IDX_DRE_TECWAY (MES, ID_EMPRESA, ID_CONTA_CONTABIL)
);

CREATE TABLE IF NOT EXISTS BP_TECWAY (
    MES                      VARCHAR(7)    NOT NULL,
    ID_CR                    INT           NULL,
    ID_EMPRESA               INT           NOT NULL,
    ID_HISTORICO             INT           NULL,
    ID_NUMERO_DE_LANCAMENTO  INT           NULL,
    BP_TECWAY                DECIMAL(20,6) NULL,
    ID_CONTA_CONTABIL        VARCHAR(30)   NOT NULL,
    NUMLOTE                  INT           NULL,
    KEY IDX_BP_TECWAY (MES, ID_EMPRESA, ID_CONTA_CONTABIL)
);
   --------------------------------------------------------------------- */
