url https://analytics2.mitrasheet.com:4435/rest/v0
chave eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBUElGIiwiWC1UZW5hbnRJRCI6InRlbmFudF82MTMwMiJ9.-r3_DlcFYZPeoqPDOJDiGD9e7wE5gKdhpq7CUeaQd2cR4cqofdyYQxkkax451Et6-HKBgd_A1B7I3p3CWf4jTw

modelo get 

curl -X GET "https://analytics2.mitrasheet.com:4435/rest/v0/BP_TECWAY?MES="someValue"&ID_EMPRESA="123"&ID_CONTA_CONTABIL="someValue"&BP_TECWAY="123.123"&page=0&size=200"\
 -H "Content-Type: application/json"\
 -H "Authorization: Bearer [BEARER_TOKEN]"

 modelo post 

 curl -X POST "https://analytics2.mitrasheet.com:4435/rest/v0/BP_TECWAY"\
 -H "Content-Type: application/json"\
 -H "Authorization: Bearer [BEARER_TOKEN]"\
 -d '{ 
 "MES" : "someValue", 
 "ID_EMPRESA" : "123", 
 "ID_CONTA_CONTABIL" : "someValue", 
 "BP_TECWAY" : "123.123"
 }' 