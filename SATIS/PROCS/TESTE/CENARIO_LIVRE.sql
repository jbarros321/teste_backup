-- =====================================================================
-- CENARIO LIVRE - preencha os parametros na mao e dispare um caso unico
--
-- Pre-requisitos:
--    @PKG_MOCK_ACT.sql
--    @STP_PARCELARFINA_SATIS_TESTE.sql
--
-- Termina em ROLLBACK. Para gravar de verdade, troque na linha do final.
-- =====================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED
SET DEFINE ON
SET FEEDBACK OFF

-- ------------------------------------------------------------------
-- AQUI: o titulo e o usuario
-- ------------------------------------------------------------------
DEFINE NUFIN_TESTE = 0
DEFINE CODUSU      = 0

DECLARE
       V_MSG   VARCHAR2(4000);
       V_ANTES NUMBER;
BEGIN
       PKG_MOCK_ACT.RESET;

       -- ============================================================
       -- AQUI: os parametros que na tela do botao o usuario preenche
       -- ============================================================
       PKG_MOCK_ACT.SET_PARAM('P_VLRAUTOMATIC',   'N');            -- S = rateio automatico | N = valores manuais
       PKG_MOCK_ACT.SET_PARAM('P_NROPARCELAS',    '3');
       PKG_MOCK_ACT.SET_PARAM('P_DESCRPARCELA',   '1=500,00;2=300,00;3=200,00');  -- so no modo N
       PKG_MOCK_ACT.SET_PARAM('P_BASEVENCIMEN',   TO_CHAR(TRUNC(SYSDATE) + 30, 'DD/MM/YYYY'));
       PKG_MOCK_ACT.SET_PARAM('P_RANGE',          '30');
       PKG_MOCK_ACT.SET_PARAM('PARAM_P_JUROS',          'N');            -- S = aplica juros
       PKG_MOCK_ACT.SET_PARAM('P_FORMAJUROS',     NULL);           -- P = percentual | V = valor
       PKG_MOCK_ACT.SET_PARAM('P_TAXAJUROS',      NULL);
       PKG_MOCK_ACT.SET_PARAM('P_DTNEG',          'NO');           -- NO = mantem a original | H = hoje


       -- linha selecionada na grade (indice 1 = primeira linha)
       PKG_MOCK_ACT.SET_FIELD(1, 'NUFIN', '&NUFIN_TESTE');
       -- para varias linhas no modo automatico:
       -- PKG_MOCK_ACT.SET_FIELD(2, 'NUFIN', '99999');
       -- ... e passar P_QTDLINHAS = 2 na chamada abaixo
       -- ============================================================

       SELECT VLRDESDOB INTO V_ANTES FROM TGFFIN WHERE NUFIN = &NUFIN_TESTE;

       DBMS_OUTPUT.PUT_LINE('ANTES  -> NUFIN &NUFIN_TESTE  VLRDESDOB = ' ||
                            TO_CHAR(V_ANTES, 'FM999G999G990D00'));
       DBMS_OUTPUT.PUT_LINE(RPAD('-', 78, '-'));

       STP_PARCELARFINA_SATIS_TESTE(&CODUSU, 'TESTE', 1, V_MSG);

       DBMS_OUTPUT.PUT_LINE('RETORNO -> ' || V_MSG);
       DBMS_OUTPUT.PUT_LINE(RPAD('-', 78, '-'));
       DBMS_OUTPUT.PUT_LINE(RPAD('NUFIN', 12) || RPAD('DESD', 6) || RPAD('VENCIMENTO', 13) ||
                            RPAD('DTNEG', 13) || LPAD('VALOR', 14) || '  HISTORICO');

       FOR R IN (SELECT NUFIN, DESDOBRAMENTO, DTVENC, DTNEG, VLRDESDOB, HISTORICO
                   FROM TGFFIN
                  WHERE AD_FINANORIGINAL = &NUFIN_TESTE
                  ORDER BY DTVENC, NUFIN) LOOP
           DBMS_OUTPUT.PUT_LINE(RPAD(R.NUFIN, 12) || RPAD(R.DESDOBRAMENTO, 6) ||
                                RPAD(TO_CHAR(R.DTVENC, 'DD/MM/YYYY'), 13) ||
                                RPAD(TO_CHAR(R.DTNEG,  'DD/MM/YYYY'), 13) ||
                                LPAD(TO_CHAR(R.VLRDESDOB, 'FM999G999G990D00'), 14) || '  ' ||
                                R.HISTORICO);
       END LOOP;

       DBMS_OUTPUT.PUT_LINE(RPAD('-', 78, '-'));

       FOR R IN (SELECT COUNT(*) QTD, SUM(VLRDESDOB) TOT
                   FROM TGFFIN WHERE AD_FINANORIGINAL = &NUFIN_TESTE) LOOP
           DBMS_OUTPUT.PUT_LINE(R.QTD || ' parcela(s)   soma = ' ||
                                TO_CHAR(R.TOT, 'FM999G999G990D00') ||
                                '   diferenca vs original = ' ||
                                TO_CHAR(NVL(R.TOT, 0) - V_ANTES, 'FM999G999G990D00'));
       END LOOP;

       FOR R IN (SELECT NUACERTO, SEQUENCIA, NUFIN, NUFINORIG, TIPACERTO
                   FROM TGFFRE WHERE NUFINORIG = &NUFIN_TESTE ORDER BY SEQUENCIA) LOOP
           DBMS_OUTPUT.PUT_LINE('TGFFRE -> acerto ' || R.NUACERTO || ' seq ' || R.SEQUENCIA ||
                                '  NUFIN ' || R.NUFIN || '  origem ' || R.NUFINORIG ||
                                '  tipo ' || R.TIPACERTO);
       END LOOP;

       ROLLBACK;                      -- <<< troque por COMMIT; se quiser gravar
       DBMS_OUTPUT.PUT_LINE(CHR(10) || 'ROLLBACK executado - nada foi gravado.');

EXCEPTION
       WHEN OTHERS THEN
            ROLLBACK;
            DBMS_OUTPUT.PUT_LINE(CHR(10) || 'ERRO: ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE(DBMS_UTILITY.FORMAT_ERROR_BACKTRACE);
END;
/

SET FEEDBACK ON
