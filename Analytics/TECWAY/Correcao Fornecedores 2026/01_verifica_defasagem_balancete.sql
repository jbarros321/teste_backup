/* =====================================================================
   01 - VERIFICA A DEFASAGEM DA CARGA DO BALANCETE
   tenant_47255 - tabela IMP_BASE_BALANCETE

   Rodar ANTES e DEPOIS de religar a carga.
   Em 22/09/2026 este script devolvia MAX(REFERENCIA) = 2026-03-01,
   ou seja 6 competencias em atraso.
   ===================================================================== */

/* ---- A) Panorama: ate quando cada empresa tem balancete ------------- */
SELECT
    CODEMP,
    MIN(REFERENCIA)                                        AS PRIMEIRA_REF,
    MAX(REFERENCIA)                                        AS ULTIMA_REF,
    TIMESTAMPDIFF(MONTH, MAX(REFERENCIA), CURDATE())       AS MESES_EM_ATRASO,
    COUNT(*)                                               AS LINHAS
FROM IMP_BASE_BALANCETE
GROUP BY CODEMP
ORDER BY MESES_EM_ATRASO DESC, CODEMP;

/* ---- B) Quais competencias do ano corrente estao faltando ----------- */
WITH RECURSIVE MESES AS (
    SELECT MAKEDATE(YEAR(CURDATE()), 1) AS REF
    UNION ALL
    SELECT DATE_ADD(REF, INTERVAL 1 MONTH) FROM MESES
     WHERE REF < DATE_FORMAT(CURDATE(), '%Y-%m-01')
)
SELECT
    m.REF                                                  AS COMPETENCIA,
    COUNT(b.AUTO_ID)                                       AS LINHAS_CARREGADAS,
    CASE WHEN COUNT(b.AUTO_ID) = 0 THEN 'FALTANDO' ELSE 'OK' END AS SITUACAO
FROM MESES m
LEFT JOIN IMP_BASE_BALANCETE b
       ON DATE_FORMAT(b.REFERENCIA, '%Y-%m') = DATE_FORMAT(m.REF, '%Y-%m')
GROUP BY m.REF
ORDER BY m.REF;

/* ---- C) A linha de Fornecedores mes a mes --------------------------- *
   Reproduz a tabela do item 1 do 00_DIAGNOSTICO.md.
   Fornecedores = 2.1.01.01.* + 2.1.01.02.* (nenhuma outra linha do BP
   usa esses dois prefixos - conferido em 22/09/2026).
   Troque :VAR_EMPRESA_DRE por 999 para o consolidado.                   */
SELECT
    DATE_FORMAT(b.REFERENCIA, '%Y-%m')                     AS COMPETENCIA,
    ROUND(-SUM(b.VLRLANC), 2)                              AS MOVIMENTO_MES,
    ROUND(-SUM(SUM(b.VLRLANC)) OVER (ORDER BY DATE_FORMAT(b.REFERENCIA, '%Y-%m')), 2)
                                                           AS SALDO_ACUMULADO
FROM IMP_BASE_BALANCETE b
WHERE b.CODEMP = :VAR_EMPRESA_DRE
  AND (b.CTACTB LIKE '2.1.01.01.%' OR b.CTACTB LIKE '2.1.01.02.%')
GROUP BY DATE_FORMAT(b.REFERENCIA, '%Y-%m')
ORDER BY COMPETENCIA;
