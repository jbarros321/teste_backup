-- =====================================================================
-- Bateria de testes de STP_PARCELARFINA_SATIS_TESTE
--
-- Pre-requisitos (executar nesta ordem, em HOMOLOGACAO):
--    @PKG_MOCK_ACT.sql
--    @STP_PARCELARFINA_SATIS_TESTE.sql
--    @RODAR_TESTES.sql
--
-- O script clona um titulo real existente para faixas de NUFIN altas,
-- executa cada cenario e da ROLLBACK ao final. NADA e commitado.
-- Informe em NUFIN_MODELO um titulo a receber qualquer, so para servir
-- de molde de colunas (ele nao e alterado).
-- =====================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED
SET DEFINE ON
SET FEEDBACK OFF

DEFINE NUFIN_MODELO = 1

DECLARE
       C_OFFSET     CONSTANT NUMBER := 900000000;
       V_MODELO     TGFFIN%ROWTYPE;
       V_PROXTESTE  NUMBER;
       V_BASE       DATE := TRUNC(SYSDATE) + 30;
       V_OK         PLS_INTEGER := 0;
       V_FALHA      PLS_INTEGER := 0;

       PROCEDURE ECHO(P_TXT VARCHAR2) IS
       BEGIN
              DBMS_OUTPUT.PUT_LINE(P_TXT);
       END ECHO;

       PROCEDURE CHECA(P_DESC VARCHAR2, P_COND BOOLEAN, P_DETALHE VARCHAR2 DEFAULT NULL) IS
       BEGIN
              IF NVL(P_COND, FALSE) THEN
                 V_OK := V_OK + 1;
                 ECHO('      [OK]    ' || P_DESC);
              ELSE
                 V_FALHA := V_FALHA + 1;
                 ECHO('      [FALHA] ' || P_DESC || CASE WHEN P_DETALHE IS NOT NULL
                                                        THEN '  -> ' || P_DETALHE END);
              END IF;
       END CHECA;

       FUNCTION CRIA_TITULO(P_VALOR NUMBER, P_BAIXADO VARCHAR2 DEFAULT 'N',
                            P_PARCELADO VARCHAR2 DEFAULT 'N') RETURN NUMBER IS
              V_NOVO TGFFIN%ROWTYPE := V_MODELO;
       BEGIN
              V_PROXTESTE := V_PROXTESTE + 1;

              V_NOVO.NUFIN            := V_PROXTESTE;
              V_NOVO.VLRDESDOB        := P_VALOR;
              V_NOVO.RECDESP          := 1;
              V_NOVO.RATEADO          := 'N';
              V_NOVO.NUNOTA           := NULL;
              V_NOVO.DESDOBRAMENTO    := 1;
              V_NOVO.DTNEG            := TRUNC(SYSDATE) - 10;
              V_NOVO.DTVENC           := TRUNC(SYSDATE) + 5;
              V_NOVO.DTVENCINIC       := TRUNC(SYSDATE) + 5;
              V_NOVO.HISTORICO        := 'TITULO DE TESTE';
              V_NOVO.VLRBAIXA         := 0;
              V_NOVO.DHBAIXA          := CASE WHEN P_BAIXADO = 'S' THEN SYSDATE END;
              V_NOVO.CODTIPOPERBAIXA  := NULL;
              V_NOVO.DHTIPOPERBAIXA   := NULL;
              V_NOVO.VLRIRF           := 0;
              V_NOVO.VLRISS           := 0;
              V_NOVO.VLRINSS          := 0;
              V_NOVO.VLRDESC          := 0;
              V_NOVO.VLRJURO          := 0;
              V_NOVO.VLRMULTA         := 0;
              V_NOVO.AD_PARCELADO     := P_PARCELADO;
              V_NOVO.AD_FINANORIGINAL := NULL;
              V_NOVO.AD_DTOPERPARC    := CASE WHEN P_PARCELADO = 'S' THEN SYSDATE END;

              INSERT INTO TGFFIN VALUES V_NOVO;
              RETURN V_PROXTESTE;
       END CRIA_TITULO;

       PROCEDURE PREPARA(P_NUFIN NUMBER, P_MODO VARCHAR2, P_PARCELAS VARCHAR2,
                         P_LISTA VARCHAR2 DEFAULT NULL, P_JUROS VARCHAR2 DEFAULT 'N',
                         P_FORMA VARCHAR2 DEFAULT NULL, P_TAXA VARCHAR2 DEFAULT NULL,
                         P_RANGE VARCHAR2 DEFAULT '30') IS
       BEGIN
              PKG_MOCK_ACT.RESET;
              PKG_MOCK_ACT.SET_PARAM('P_VLRAUTOMATIC', P_MODO);
              PKG_MOCK_ACT.SET_PARAM('P_NROPARCELAS',  P_PARCELAS);
              PKG_MOCK_ACT.SET_PARAM('P_DESCRPARCELA', P_LISTA);
              PKG_MOCK_ACT.SET_PARAM('P_BASEVENCIMEN', TO_CHAR(V_BASE, 'DD/MM/YYYY'));
              PKG_MOCK_ACT.SET_PARAM('P_RANGE',        P_RANGE);
              PKG_MOCK_ACT.SET_PARAM('PARAM_P_JUROS',        P_JUROS);
              PKG_MOCK_ACT.SET_PARAM('P_FORMAJUROS',   P_FORMA);
              PKG_MOCK_ACT.SET_PARAM('P_TAXAJUROS',    P_TAXA);
              PKG_MOCK_ACT.SET_PARAM('P_DTNEG',        'NO');
              PKG_MOCK_ACT.SET_FIELD(1, 'NUFIN', TO_CHAR(P_NUFIN));
       END PREPARA;

       FUNCTION SOMA_PARCELAS(P_NUFIN NUMBER) RETURN NUMBER IS
              V NUMBER;
       BEGIN
              SELECT NVL(SUM(VLRDESDOB), 0) INTO V
                FROM TGFFIN WHERE AD_FINANORIGINAL = P_NUFIN;
              RETURN V;
       END SOMA_PARCELAS;

       FUNCTION QTD_PARCELAS(P_NUFIN NUMBER) RETURN NUMBER IS
              V NUMBER;
       BEGIN
              SELECT COUNT(*) INTO V FROM TGFFIN WHERE AD_FINANORIGINAL = P_NUFIN;
              RETURN V;
       END QTD_PARCELAS;

       FUNCTION VLR_PARCELA(P_NUFIN NUMBER, P_ORDEM NUMBER) RETURN NUMBER IS
              V NUMBER;
       BEGIN
              SELECT VLRDESDOB INTO V
                FROM (SELECT VLRDESDOB, ROW_NUMBER() OVER (ORDER BY DTVENC, NUFIN) RN
                        FROM TGFFIN WHERE AD_FINANORIGINAL = P_NUFIN)
               WHERE RN = P_ORDEM;
              RETURN V;
       EXCEPTION
              WHEN NO_DATA_FOUND THEN RETURN NULL;
       END VLR_PARCELA;

       FUNCTION VENC_PARCELA(P_NUFIN NUMBER, P_ORDEM NUMBER) RETURN DATE IS
              V DATE;
       BEGIN
              SELECT DTVENC INTO V
                FROM (SELECT DTVENC, ROW_NUMBER() OVER (ORDER BY DTVENC, NUFIN) RN
                        FROM TGFFIN WHERE AD_FINANORIGINAL = P_NUFIN)
               WHERE RN = P_ORDEM;
              RETURN V;
       EXCEPTION
              WHEN NO_DATA_FOUND THEN RETURN NULL;
       END VENC_PARCELA;

       FUNCTION ERRO_ESPERADO(P_CODIGO NUMBER, P_NUFIN NUMBER) RETURN VARCHAR2 IS
              V_MSG VARCHAR2(4000);
       BEGIN
              STP_PARCELARFINA_SATIS_TESTE(1, 'TESTE', 1, V_MSG);
              RETURN 'NENHUM ERRO';
       EXCEPTION
              WHEN OTHERS THEN
                   RETURN CASE WHEN SQLCODE = P_CODIGO THEN 'OK'
                               ELSE 'ORA' || SQLCODE || ' ' || SUBSTR(SQLERRM, 1, 120) END;
       END ERRO_ESPERADO;

BEGIN
       SELECT * INTO V_MODELO FROM TGFFIN WHERE NUFIN = &NUFIN_MODELO;
       SELECT GREATEST(NVL(MAX(NUFIN), 0), C_OFFSET) INTO V_PROXTESTE FROM TGFFIN;

       ECHO('======================================================================');
       ECHO(' BATERIA DE TESTES - STP_PARCELARFINA_SATIS');
       ECHO(' Molde: NUFIN &NUFIN_MODELO   Faixa de teste a partir de ' || (V_PROXTESTE + 1));
       ECHO(' Data base de vencimento: ' || TO_CHAR(V_BASE, 'DD/MM/YYYY'));
       ECHO('======================================================================');

       -------------------------------------------------------------------
       DECLARE
              V_NUFIN NUMBER;
              V_MSG   VARCHAR2(4000);
       BEGIN
              ECHO(CHR(10) || '  CENARIO 1 - Rateio com dizima: R$ 100,00 em 3x');
              SAVEPOINT S1;
              V_NUFIN := CRIA_TITULO(100);
              PREPARA(V_NUFIN, 'S', '3');
              STP_PARCELARFINA_SATIS_TESTE(1, 'TESTE', 1, V_MSG);

              CHECA('gerou 3 parcelas', QTD_PARCELAS(V_NUFIN) = 3, 'qtd=' || QTD_PARCELAS(V_NUFIN));
              CHECA('soma fecha em 100,00', SOMA_PARCELAS(V_NUFIN) = 100,
                    'soma=' || SOMA_PARCELAS(V_NUFIN));
              CHECA('parcela 1 = 33,33', VLR_PARCELA(V_NUFIN, 1) = 33.33, 'v=' || VLR_PARCELA(V_NUFIN, 1));
              CHECA('parcela 3 = 33,34', VLR_PARCELA(V_NUFIN, 3) = 33.34, 'v=' || VLR_PARCELA(V_NUFIN, 3));
              CHECA('1o vencimento na data base', VENC_PARCELA(V_NUFIN, 1) = V_BASE,
                    TO_CHAR(VENC_PARCELA(V_NUFIN, 1), 'DD/MM/YYYY'));
              CHECA('3o vencimento em base+60', VENC_PARCELA(V_NUFIN, 3) = V_BASE + 60);
              CHECA('mensagem preenchida', V_MSG IS NOT NULL, V_MSG);
              ROLLBACK TO S1;
       END;

       -------------------------------------------------------------------
       DECLARE
              V_NUFIN NUMBER;
              V_MSG   VARCHAR2(4000);
       BEGIN
              ECHO(CHR(10) || '  CENARIO 2 - Juros percentual: R$ 1.000,00 em 2x, P 10%');
              SAVEPOINT S2;
              V_NUFIN := CRIA_TITULO(1000);
              PREPARA(V_NUFIN, 'S', '2', NULL, 'S', 'P', '10');
              STP_PARCELARFINA_SATIS_TESTE(1, 'TESTE', 1, V_MSG);

              CHECA('soma = 1.100,00', SOMA_PARCELAS(V_NUFIN) = 1100, 'soma=' || SOMA_PARCELAS(V_NUFIN));
              CHECA('parcela 1 = 550,00', VLR_PARCELA(V_NUFIN, 1) = 550);
              CHECA('parcela 2 = 550,00', VLR_PARCELA(V_NUFIN, 2) = 550);
              ROLLBACK TO S2;
       END;

       -------------------------------------------------------------------
       DECLARE
              V_NUFIN NUMBER;
              V_MSG   VARCHAR2(4000);
       BEGIN
              ECHO(CHR(10) || '  CENARIO 3 - Juros valor fixo: R$ 1.000,00 em 4x, V R$ 100,00');
              SAVEPOINT S3;
              V_NUFIN := CRIA_TITULO(1000);
              PREPARA(V_NUFIN, 'N', '4', '1=250,00;2=250,00;3=250,00;4=250,00', 'S', 'V', '100');
              STP_PARCELARFINA_SATIS_TESTE(1, 'TESTE', 1, V_MSG);

              CHECA('soma = 1.100,00 (juros somado UMA vez)', SOMA_PARCELAS(V_NUFIN) = 1100,
                    'soma=' || SOMA_PARCELAS(V_NUFIN));
              CHECA('parcela 1 = 275,00', VLR_PARCELA(V_NUFIN, 1) = 275);
              ROLLBACK TO S3;
       END;

       -------------------------------------------------------------------
       DECLARE
              V_NUFIN NUMBER;
              V_MSG   VARCHAR2(4000);
       BEGIN
              ECHO(CHR(10) || '  CENARIO 4 - Manual fora de ordem: 3=300;1=500;2=200');
              SAVEPOINT S4;
              V_NUFIN := CRIA_TITULO(1000);
              PREPARA(V_NUFIN, 'N', '3', '3=300,00;1=500,00;2=200,00');
              STP_PARCELARFINA_SATIS_TESTE(1, 'TESTE', 1, V_MSG);

              CHECA('aceitou fora de ordem', QTD_PARCELAS(V_NUFIN) = 3);
              CHECA('parcela 1 = 500,00', VLR_PARCELA(V_NUFIN, 1) = 500, 'v=' || VLR_PARCELA(V_NUFIN, 1));
              CHECA('parcela 2 = 200,00', VLR_PARCELA(V_NUFIN, 2) = 200, 'v=' || VLR_PARCELA(V_NUFIN, 2));
              CHECA('parcela 3 = 300,00', VLR_PARCELA(V_NUFIN, 3) = 300, 'v=' || VLR_PARCELA(V_NUFIN, 3));
              CHECA('soma = 1.000,00', SOMA_PARCELAS(V_NUFIN) = 1000);
              ROLLBACK TO S4;
       END;

       -------------------------------------------------------------------
       DECLARE
              V_NUFIN NUMBER;
              V_VLR   NUMBER;
              V_HIST  VARCHAR2(200);
              V_MSG   VARCHAR2(4000);
       BEGIN
              ECHO(CHR(10) || '  CENARIO 5 - Titulo original vira a parcela 1 (nao a ultima)');
              SAVEPOINT S5;
              V_NUFIN := CRIA_TITULO(1000);
              PREPARA(V_NUFIN, 'N', '3', '1=500,00;2=300,00;3=200,00');
              STP_PARCELARFINA_SATIS_TESTE(1, 'TESTE', 1, V_MSG);

              SELECT VLRDESDOB, HISTORICO INTO V_VLR, V_HIST FROM TGFFIN WHERE NUFIN = V_NUFIN;
              CHECA('titulo original = 500,00', V_VLR = 500, 'v=' || V_VLR);
              CHECA('historico da parcela 1', V_HIST LIKE 'PARCELA 1/3%', V_HIST);
              CHECA('soma total = 1.000,00', SOMA_PARCELAS(V_NUFIN) = 1000,
                    'soma=' || SOMA_PARCELAS(V_NUFIN));
              ROLLBACK TO S5;
       END;

       -------------------------------------------------------------------
       DECLARE
              V_NUFIN NUMBER;
              V_RES   VARCHAR2(4000);
       BEGIN
              ECHO(CHR(10) || '  CENARIO 6 - Elegibilidade e validacoes (nada pode ser gravado)');

              SAVEPOINT S6A;
              V_NUFIN := CRIA_TITULO(1000, 'S');
              PREPARA(V_NUFIN, 'S', '3');
              V_RES := ERRO_ESPERADO(-20104, V_NUFIN);
              CHECA('titulo baixado -> -20104', V_RES = 'OK', V_RES);
              ROLLBACK TO S6A;

              SAVEPOINT S6B;
              V_NUFIN := CRIA_TITULO(1000, 'N', 'S');
              PREPARA(V_NUFIN, 'S', '3');
              V_RES := ERRO_ESPERADO(-20108, V_NUFIN);
              CHECA('ja parcelado -> -20108', V_RES = 'OK', V_RES);
              ROLLBACK TO S6B;

              SAVEPOINT S6C;
              V_NUFIN := CRIA_TITULO(1000);
              PREPARA(V_NUFIN, 'N', '2', '1=400,00;2=400,00');
              V_RES := ERRO_ESPERADO(-20113, V_NUFIN);
              CHECA('soma divergente -> -20113', V_RES = 'OK', V_RES);
              CHECA('nada gravado apos o erro', QTD_PARCELAS(V_NUFIN) = 0,
                    'qtd=' || QTD_PARCELAS(V_NUFIN));
              ROLLBACK TO S6C;

              SAVEPOINT S6D;
              V_NUFIN := CRIA_TITULO(1000);
              PREPARA(V_NUFIN, 'N', '2', '1=500,00;1=500,00');
              V_RES := ERRO_ESPERADO(-20114, V_NUFIN);
              CHECA('parcela duplicada -> -20114', V_RES = 'OK', V_RES);
              ROLLBACK TO S6D;

              SAVEPOINT S6E;
              V_NUFIN := CRIA_TITULO(1000);
              PREPARA(V_NUFIN, 'S', '0');
              V_RES := ERRO_ESPERADO(-20101, V_NUFIN);
              CHECA('zero parcelas -> -20101 (sem ORA-01476)', V_RES = 'OK', V_RES);
              ROLLBACK TO S6E;

              SAVEPOINT S6F;
              V_NUFIN := CRIA_TITULO(1000);
              PREPARA(V_NUFIN, 'N', '2', '1=xxx;2=500,00');
              V_RES := ERRO_ESPERADO(-20115, V_NUFIN);
              CHECA('valor nao numerico -> -20115 (sem ORA-06502)', V_RES = 'OK', V_RES);
              ROLLBACK TO S6F;

              SAVEPOINT S6G;
              V_NUFIN := CRIA_TITULO(1000);
              PREPARA(V_NUFIN, 'S', '3', NULL, 'S', 'X', '10');
              V_RES := ERRO_ESPERADO(-20116, V_NUFIN);
              CHECA('forma de juros invalida -> -20116 (sem VLRDESDOB nulo)', V_RES = 'OK', V_RES);
              ROLLBACK TO S6G;
       END;

       -------------------------------------------------------------------
       DECLARE
              V_NUFIN NUMBER;
              V_MSG   VARCHAR2(4000);
              V_QTD   NUMBER;
       BEGIN
              ECHO(CHR(10) || '  CENARIO 7 - Rastreabilidade');
              SAVEPOINT S7;
              V_NUFIN := CRIA_TITULO(900);
              PREPARA(V_NUFIN, 'S', '3');
              STP_PARCELARFINA_SATIS_TESTE(1, 'TESTE', 1, V_MSG);

              SELECT COUNT(*) INTO V_QTD FROM TGFFIN
               WHERE AD_FINANORIGINAL = V_NUFIN AND AD_PARCELADO = 'S'
                 AND AD_USUDEPARCELAMENTO IS NOT NULL AND AD_DTOPERPARC IS NOT NULL;
              CHECA('3 parcelas com campos AD_ preenchidos', V_QTD = 3, 'qtd=' || V_QTD);

              SELECT COUNT(*) INTO V_QTD FROM TGFFRE WHERE NUFINORIG = V_NUFIN;
              CHECA('3 registros em TGFFRE', V_QTD = 3, 'qtd=' || V_QTD);

              SELECT COUNT(DISTINCT DESDOBRAMENTO) INTO V_QTD FROM TGFFIN
               WHERE AD_FINANORIGINAL = V_NUFIN;
              CHECA('desdobramentos distintos', V_QTD = 3, 'qtd=' || V_QTD);
              ROLLBACK TO S7;
       END;

       -------------------------------------------------------------------
       ECHO(CHR(10) || '======================================================================');
       ECHO(' RESULTADO: ' || V_OK || ' OK / ' || V_FALHA || ' FALHA(S)');
       ECHO('======================================================================');

       ROLLBACK;

EXCEPTION
       WHEN OTHERS THEN
            ROLLBACK;
            ECHO(CHR(10) || ' ERRO NA BATERIA: ' || SQLERRM);
            ECHO(DBMS_UTILITY.FORMAT_ERROR_BACKTRACE);
            RAISE;
END;
/

ROLLBACK;
SET FEEDBACK ON
