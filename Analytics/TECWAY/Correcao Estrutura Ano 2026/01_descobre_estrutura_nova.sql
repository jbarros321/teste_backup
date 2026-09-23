/* =====================================================================
   01 - DESCOBRE O NOME E AS COLUNAS DA NOVA TABELA DE ANO
   Rodar na plataforma (queryMitra tem SQL livre; o REST tem whitelist de
   tabela e nao serve para isto).

   Mande a saida de A, B e C.
   ===================================================================== */

/* ---- A) Todas as tabelas da familia dos demonstrativos -------------- */
SELECT TABLE_NAME,
       TABLE_ROWS,
       CREATE_TIME,
       UPDATE_TIME
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE()
   AND (TABLE_NAME LIKE '%DEMONSTRATIV%'
     OR TABLE_NAME LIKE '%ANO%'
     OR TABLE_NAME LIKE '%EXERCICIO%'
     OR TABLE_NAME LIKE '%REFERENCIA%')
 ORDER BY CREATE_TIME DESC;          -- a tabela nova aparece no topo

/* ---- B) As colunas de cada uma delas -------------------------------- */
SELECT TABLE_NAME,
       ORDINAL_POSITION,
       COLUMN_NAME,
       COLUMN_TYPE,
       IS_NULLABLE,
       COLUMN_KEY
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND (TABLE_NAME LIKE '%DEMONSTRATIV%'
     OR TABLE_NAME LIKE '%ANO%'
     OR TABLE_NAME LIKE '%EXERCICIO%'
     OR TABLE_NAME LIKE '%REFERENCIA%')
 ORDER BY TABLE_NAME, ORDINAL_POSITION;

/* ---- C) Como as tabelas se ligam (o encadeamento real) -------------- *
   E o que resolve a duvida principal: DET_DEMONSTRATIVO_CTACTB continua
   pendurada em DET_DEMONSTRATIVO_REFERENCIA, ou passou a apontar para a
   tabela de ano?                                                        */
SELECT TABLE_NAME            AS TABELA_FILHA,
       COLUMN_NAME           AS COLUNA_FK,
       REFERENCED_TABLE_NAME AS TABELA_PAI,
       REFERENCED_COLUMN_NAME AS COLUNA_PAI,
       CONSTRAINT_NAME
  FROM information_schema.KEY_COLUMN_USAGE
 WHERE TABLE_SCHEMA = DATABASE()
   AND REFERENCED_TABLE_NAME IS NOT NULL
   AND (TABLE_NAME LIKE '%DEMONSTRATIV%' OR REFERENCED_TABLE_NAME LIKE '%DEMONSTRATIV%')
 ORDER BY TABELA_PAI, TABELA_FILHA;

/* ---- D) Se A/B/C nao rodarem (information_schema bloqueado) --------- *
   Rode isto trocando NOVA_TABELA_ANO pelo nome real e me mande a saida:

       SHOW CREATE TABLE NOVA_TABELA_ANO;
       SHOW CREATE TABLE DET_DEMONSTRATIVO_REFERENCIA;
       SHOW CREATE TABLE DET_DEMONSTRATIVO_CTACTB;
       SELECT * FROM NOVA_TABELA_ANO LIMIT 10;
                                                                         */
