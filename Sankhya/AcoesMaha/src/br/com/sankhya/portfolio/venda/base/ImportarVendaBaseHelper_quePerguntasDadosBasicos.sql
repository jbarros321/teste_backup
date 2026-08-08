select  distinct   p.codquest, p.descrperg, p.codperg ,
(select 
    CASE UPPER(r.descrresp)  
    WHEN 'VENDA PADRÃO'  THEN   
        CASE
            WHEN C.AD_TIPOVENDA = 'VP' THEN 
                (SELECT R2.CODRESP FROM TPQRES R2 WHERE R2.CODPERG = P.CODPERG AND UPPER(R2.descrresp)  = 'VENDA PADRÃO' AND ROWNUM = 1)
            ELSE
                (SELECT R2.CODRESP FROM TPQRES R2 WHERE R2.CODPERG = P.CODPERG AND UPPER(R2.descrresp)  = 'VENDA POR USUÁRIO' AND ROWNUM = 1)
            END
    ELSE r.codresp  END

 from  TPQRES r where r.codperg = p.codperg
and ( UPPER(r.descrresp)  = 'VENDA PADRÃO')   
and rownum = 1
 ) as CODRESP_TIPO_VENDA_CONTRATO ,
 (select r.codresp
 from  TPQRES r where r.codperg = p.codperg
and (UPPER(r.descrresp) =  UPPER('Não')  or UPPER(r.descrresp) =  UPPER('Venda na Base'))   
and rownum = 1
 ) as CODRESP ,
 
 C.AD_TIPOVENDA
from TCSQXF qf
inner join TPQQUE q on q.codquest = qf.codquest
inner join TPQPER p on p.CODQUEST = qf.CODQUEST
left join TPQDPD pd on pd.CODPERGDEP = p.CODPERG
INNER JOIN TCSCON c on (C.CODPARC =  :CODPARC )
where qf.codfld = :CODFLD
and q.descrquest = 'DADOS BÁSICOS'
and pd.codpergdep is null
