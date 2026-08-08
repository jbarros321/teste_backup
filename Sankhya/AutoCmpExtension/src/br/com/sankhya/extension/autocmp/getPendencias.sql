SELECT 
     ITE.NUMOS, 
     ITE.NUMITEM,
     (SELECT NUFAP FROM TCSOSE O WHERE O.NUMOS = ITE.NUMOS) AS NUFAP,
     ITE.SOLUCAO, 
     ITE.DHENTRADA, 
     NVL( ( 
      SELECT  
         'true'  
      FROM  
         TCSOSE OSE 
      WHERE  
        NVL (OSE.TIPO, 'X') <> 'P' AND NVL (OSE.NUMCONTRATO, 0) > 0 
        AND OSE.NUMOS = ITE.NUMOS  
        AND NOT EXISTS 
        (            
          SELECT  1 
             FROM TCSITE I 
            WHERE     
                  I.NUMOS = OSE.NUMOS 
                  AND I.CODSERV  = 50604 --Implementacao
        ) 
    ), 'false') AS CORRECAO,
    (SELECT 
        CASE 
            WHEN CODGRUPOPROD = 1006 THEN 'false'
            ELSE 'true'
        END
     FROM 
            TGFPRO PRO
       WHERE 
           PRO.CODPROD = ITE.CODPROD 
    )AS ISSANKHYA,
    ITE.PRIORIDADE,
    ITE.CODUSUREM,
    (SELECT NOMEUSU FROM TSIUSU U WHERE U.CODUSU = ITE.CODUSUREM ) AS NOMEREMETENTE,
	(SELECT 
		   max(CEL.SLACK)
		FROM 
			AD_INDCELPROD CEL 
		INNER JOIN AD_MEMBCELULA MEM ON CEL.CODCELPROD = MEM.CODCELPROD
		WHERE 
			MEM.CODUSU = ITE.CODUSUREM
	) AS SLACK
    FROM 
        TCSITE ITE 
    WHERE  
        CODUSU = 1721 --Fila de Compilacao
        AND HRFINAL IS NULL
	ORDER BY NVL(ITE.PRIORIDADE, 10), ITE.DHENTRADA