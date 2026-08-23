-- ============================================================================
-- Consulta das parcelas geradas por STP_PARCELARFINA_SATIS
-- Troque 500247 pelo NUFIN do titulo de origem.
-- ============================================================================

-- 1) As parcelas (a de origem + as novas), na ordem de vencimento
SELECT F.NUFIN,
       F.DESDOBRAMENTO,
       F.VLRDESDOB,
       F.DTVENC,
       F.DTNEG,
       F.HISTORICO,
       F.AD_PARCELADO,
       F.AD_FINANORIGINAL,
       F.AD_USUDEPARCELAMENTO,
       U.NOMEUSU,
       F.AD_DTOPERPARC
  FROM TGFFIN F
  LEFT JOIN TSIUSU U ON U.CODUSU = F.AD_USUDEPARCELAMENTO
 WHERE F.AD_FINANORIGINAL = 500247
 ORDER BY F.DTVENC, F.NUFIN;

-- 2) Conferencia: a soma das parcelas bate com o esperado?
SELECT COUNT(*)             QTD_PARCELAS,
       SUM(VLRDESDOB)       SOMA,
       MIN(DTVENC)          PRIMEIRO_VENC,
       MAX(DTVENC)          ULTIMO_VENC
  FROM TGFFIN
 WHERE AD_FINANORIGINAL = 500247;

-- 3) Rastreabilidade do acerto em TGFFRE
SELECT R.NUACERTO, R.SEQUENCIA, R.NUFIN, R.NUFINORIG, R.NUNOTA, R.TIPACERTO,
       R.CODUSU, R.DHALTER
  FROM TGFFRE R
 WHERE R.NUFINORIG = 500247
 ORDER BY R.NUACERTO, R.SEQUENCIA;

-- 4) Ultimos parcelamentos feitos (sem saber o NUFIN de origem)
SELECT F.AD_FINANORIGINAL  NUFIN_ORIGEM,
       COUNT(*)            QTD_PARCELAS,
       SUM(F.VLRDESDOB)    SOMA,
       MAX(F.AD_DTOPERPARC) QUANDO,
       MAX(U.NOMEUSU)      QUEM
  FROM TGFFIN F
  LEFT JOIN TSIUSU U ON U.CODUSU = F.AD_USUDEPARCELAMENTO
 WHERE F.AD_PARCELADO = 'S'
   AND F.AD_DTOPERPARC >= TRUNC(SYSDATE)
 GROUP BY F.AD_FINANORIGINAL
 ORDER BY QUANDO DESC;
