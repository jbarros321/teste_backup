select  pd.CODPERGMESTRE, res.CODRESP, per.CODQUEST
from TPQDPD pd
left join TPQDPD pdm on pdm.CODPERGDEP = pd.CODPERGMESTRE
inner join TPQRES res on res.CODPERG = pd.CODPERGMESTRE
inner join TPQPER per on per.CODPERG = res.CODPERG
where pd.CODPERGDEP =  :CODPERGDEP
and UPPER(res.DESCRRESP) = 'SIM'
and pdm.CODPERGDEP is null