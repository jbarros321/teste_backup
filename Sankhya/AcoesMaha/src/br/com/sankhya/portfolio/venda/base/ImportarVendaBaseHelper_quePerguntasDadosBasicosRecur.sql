select distinct p.descrperg, pd.CODPERGDEP, pd.CODPERGMESTRE  , (select r.codresp from  TPQRES r where r.codperg = pd.codpergdep
and (UPPER(r.descrresp) like  UPPER('Não')  or UPPER(r.descrresp) like  UPPER('Venda na Base') or UPPER(r.descrresp) like ('0')) 
and rownum = 1
 ) as CODRESP,
 pm.codquest
 from TPQDPD pd
 inner join TPQPER p on (p.CODPERG = pd.CODPERGDEP)
 inner join TPQPER pm on (pm.CODPERG = pd.codpergmestre)
where pd.codpergmestre = :CODPERGMESTRE

