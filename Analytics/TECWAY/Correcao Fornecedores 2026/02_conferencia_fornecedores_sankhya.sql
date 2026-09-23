/* =====================================================================
   02 - CONFERENCIA CONTA A CONTA DE FORNECEDORES CONTRA O SANKHYA
   tenant_47255

   Use depois de religar a carga. Exporte o resultado e cole ao lado do
   balancete do Sankhya na mesma data: a soma da coluna SALDO tem que
   bater com o total de Fornecedores de la.

   Parametros:
     :VAR_DATA_REF_DRE  data de corte  (ex. '2026-09-21')
     :VAR_EMPRESA_DRE   CODEMP         (999 = consolidado)

   ATENCAO ao corte por dia: IMP_BASE_BALANCETE guarda uma linha por
   COMPETENCIA (sempre dia 01), nao por lancamento. Nao da para cortar em
   21/09 aqui - o menor grao e o mes fechado. Se o Sankhya foi extraido
   em 21/09 com setembro ainda aberto, compare contra 31/08 ou peca o
   balancete do Sankhya na mesma competencia fechada.
   ===================================================================== */

/* ---- A) Total, para bater de cara com o rodape do Sankhya ----------- */
SELECT
    ROUND(-SUM(VLRLANC), 2) AS TOTAL_FORNECEDORES
FROM IMP_BASE_BALANCETE
WHERE CODEMP = :VAR_EMPRESA_DRE
  AND (CTACTB LIKE '2.1.01.01.%' OR CTACTB LIKE '2.1.01.02.%')
  AND REFERENCIA <= LAST_DAY(DATE(:VAR_DATA_REF_DRE));

/* ---- B) Conta a conta, so o que tem saldo --------------------------- */
SELECT
    CTACTB,
    ROUND(-SUM(VLRLANC), 2) AS SALDO
FROM IMP_BASE_BALANCETE
WHERE CODEMP = :VAR_EMPRESA_DRE
  AND (CTACTB LIKE '2.1.01.01.%' OR CTACTB LIKE '2.1.01.02.%')
  AND REFERENCIA <= LAST_DAY(DATE(:VAR_DATA_REF_DRE))
GROUP BY CTACTB
HAVING ROUND(SUM(VLRLANC), 2) <> 0
ORDER BY CTACTB;

/* ---- C) O que o balancete tem e o cadastro do BP Interno nao ve ----- *
   Se esta consulta voltar linhas com saldo, sao contas de fornecedor
   que existem no razao mas nao entram na tela. E o achado do item 3 do
   00_DIAGNOSTICO.md - corrigido pelo script 03.                        */
SELECT
    b.CTACTB,
    ROUND(-SUM(b.VLRLANC), 2) AS SALDO_ORFAO
FROM IMP_BASE_BALANCETE b
WHERE b.CODEMP = :VAR_EMPRESA_DRE
  AND (b.CTACTB LIKE '2.1.01.01.%' OR b.CTACTB LIKE '2.1.01.02.%')
  AND b.REFERENCIA <= LAST_DAY(DATE(:VAR_DATA_REF_DRE))
  AND NOT EXISTS (
        SELECT 1
          FROM DET_DEMONSTRATIVO            d
          JOIN DET_DEMONSTRATIVO_REFERENCIA r  ON r.ID_DET_DEMONSTRATIVO = d.ID
          JOIN DET_DEMONSTRATIVO_CTACTB     v  ON v.ID_DET_DEMONSTRATIVO_REFERENCIA = r.ID
         WHERE d.ID_ESTR_DEMONSTRATIVO = 2          -- BP Interno
           AND b.CTACTB LIKE v.PADRAO_CTACTB
      )
GROUP BY b.CTACTB
HAVING ROUND(SUM(b.VLRLANC), 2) <> 0
ORDER BY b.CTACTB;
