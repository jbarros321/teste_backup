/* =====================================================================
   01 - SCHEMA NOVO: o que ja sabemos e a unica coisa que falta
   Rodar na plataforma (o REST tem whitelist de tabela e nao serve).
   ===================================================================== */

/* =====================================================================
   JA CONFIRMADO pelo mapa de FK de 23/09/2026
   =====================================================================

   ESTR_DEMONSTRATIVOS
     └─ DET_DEMONSTRATIVO                 (ID_ESTR_DEMONSTRATIVO)      6 colunas
          └─ DET_DEMONSTRATIVO_ANO        (ID_DET_DEMONSTRATIVO)       3 colunas
               ├─ DET_DEMONSTRATIVO_ANO_CTACTB      (ID_DET_DEMONSTRATIVO_ANO)  4 col   << NOVO
               ├─ DET_DEMONSTRATIVO_ANO_CTACTB_EXC  (ID_DET_DEMONSTRATIVO_ANO)  4 col   << NOVO
               └─ DET_DEMONSTRATIVO_REFERENCIA      (ID_DET_DEMONSTRATIVO_ANO)  3 col
                    ├─ DET_DEMONSTRATIVO_CTACTB     (ID_DET_DEMONSTRATIVO_REFERENCIA)  4 col
                    └─ DET_DEMONSTRATIVO_CTACTB_EXC (ID_DET_DEMONSTRATIVO_REFERENCIA)  4 col

   DET_DEMONSTRATIVO_REFERENCIA tem 3 colunas: ID, ID_DET_DEMONSTRATIVO_ANO,
   REFERENCIA. Perdeu ID_DET_DEMONSTRATIVO - e exatamente o que o erro
   "Unknown column 'R.ID_DET_DEMONSTRATIVO' in 'where clause'" dizia.

   Existem DOIS caminhos ate o vinculo conta x linha:
     CAMINHO LEGADO : ..._ANO -> ..._REFERENCIA -> ..._CTACTB
     CAMINHO NOVO   : ..._ANO -> ..._ANO_CTACTB
   ===================================================================== */

/* =====================================================================
   FALTA DECIDIR - rode A e B e me mande a saida
   ===================================================================== */

/* ---- A) QUAL DOS DOIS CAMINHOS TEM OS VINCULOS? --------------------- *
   E a pergunta que decide a correcao inteira. Se os vinculos ja foram
   migrados para as tabelas _ANO_CTACTB, corrigir so o encadeamento
   antigo faz a tela abrir zerada em vez de dar erro - que e pior.       */
SELECT 'DET_DEMONSTRATIVO_ANO_CTACTB'      AS TABELA, COUNT(*) AS LINHAS
  FROM DET_DEMONSTRATIVO_ANO_CTACTB
UNION ALL
SELECT 'DET_DEMONSTRATIVO_ANO_CTACTB_EXC', COUNT(*) FROM DET_DEMONSTRATIVO_ANO_CTACTB_EXC
UNION ALL
SELECT 'DET_DEMONSTRATIVO_CTACTB',         COUNT(*) FROM DET_DEMONSTRATIVO_CTACTB
UNION ALL
SELECT 'DET_DEMONSTRATIVO_CTACTB_EXC',     COUNT(*) FROM DET_DEMONSTRATIVO_CTACTB_EXC
UNION ALL
SELECT 'DET_DEMONSTRATIVO_REFERENCIA',     COUNT(*) FROM DET_DEMONSTRATIVO_REFERENCIA
UNION ALL
SELECT 'DET_DEMONSTRATIVO_ANO',            COUNT(*) FROM DET_DEMONSTRATIVO_ANO;

/* ---- B) AS 3 COLUNAS DE DET_DEMONSTRATIVO_ANO E AS 2 DE CAD_ANO ----- *
   Sao ID, ID_DET_DEMONSTRATIVO e... `ANO` (int) ou `ID_CAD_ANO` (FK)?
   Nao aparece FK para CAD_ANO no mapa, entao apostei em `ANO` - mas a
   FK pode simplesmente nao estar declarada.                            */
SELECT TABLE_NAME, ORDINAL_POSITION, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_NAME IN ('DET_DEMONSTRATIVO_ANO', 'CAD_ANO',
                      'DET_DEMONSTRATIVO_ANO_CTACTB', 'DET_DEMONSTRATIVO_REFERENCIA')
 ORDER BY TABLE_NAME, ORDINAL_POSITION;

/* ---- C) Que anos estao cadastrados, e para quais estruturas --------- *
   Confirma se o BP Interno (EST 2) ja tem 2026 cadastrado. Se nao tiver,
   o filtro de ano vigente (MAX(ANO) <= ano de corte) e o que segura a
   tela funcionando com o cadastro de 2025.
   Se a coluna for ID_CAD_ANO em vez de ANO, troque na linha marcada.    */
SELECT E.ID                AS ID_ESTRUTURA,
       E.NOME_DEMONSTRATIVO,
       A.ANO,                                   -- <<< ou: C.ANO, via JOIN CAD_ANO C ON C.ID = A.ID_CAD_ANO
       COUNT(DISTINCT D.ID) AS LINHAS_DO_DEMONSTRATIVO
  FROM ESTR_DEMONSTRATIVOS    E
  JOIN DET_DEMONSTRATIVO      D ON D.ID_ESTR_DEMONSTRATIVO = E.ID
  JOIN DET_DEMONSTRATIVO_ANO  A ON A.ID_DET_DEMONSTRATIVO  = D.ID
 GROUP BY E.ID, E.NOME_DEMONSTRATIVO, A.ANO
 ORDER BY E.ID, A.ANO;
