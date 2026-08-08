/* 
  Lógica solicitada para aplicar na queryData baseada na variável :VAR_DRE_EMP 
  Adicione este bloco nas condições WHERE da sua consulta SQL principal.
*/

AND NOT (
    (
        INSTR(',' || :VAR_DRE_EMP || ',', ',1,') > 0 
        OR INSTR(',' || :VAR_DRE_EMP || ',', ',2,') > 0
    )
    AND TO_CHAR(CODNAT) LIKE '2%'
)
AND NOT (
    (
        INSTR(',' || :VAR_DRE_EMP || ',', ',3,') > 0 
        OR INSTR(',' || :VAR_DRE_EMP || ',', ',4,') > 0 
        OR INSTR(',' || :VAR_DRE_EMP || ',', ',5,') > 0 
        OR INSTR(',' || :VAR_DRE_EMP || ',', ',6,') > 0
    )
    AND TO_CHAR(CODNAT) LIKE '3%'
)

/* 
 * OBSERVAÇÕES:
 * 1. Alteramos CODNAT para TO_CHAR(CODNAT) caso na sua base seja numérico.
 *    Se você utilizar o alias de uma tabela, substitua CODNAT por O.CODNAT ou N.CODNAT conforme aplicável na sua query.
 * 2. A técnica de INSTR com vírgulas é usada para evitar que a pesquisa por '1' acidentalmente atinja '11' ou '21' caso hajam mais empresas no futuro (caso a variável venha como lista '1,2,3').
 */
