# Curso Integração OT/IT e Chão de Fábrica no Sankhya ERP (SEA-O10)

> Fonte: https://www.searchitect.pro/universidade/SEA-O10  
> Coletado em: 2026-10-14

---

## SEA-O10 — Integração OT/IT e Chão de Fábrica no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 5 horas, com certificado SEA Practitioner — Automação Industrial na trilha Supply Chain e Operações. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP. Aprofunda o trabalho do SEA - Automação Industrial.

### Ementa

- Módulo 1 · Território do Automação Industrial

- Cobertura: MES, SCADA, PLC, IIoT, OPC-UA e MQTT

- A fronteira OT/IT e por que ela concentra os riscos

- Quando o problema é de rede industrial e não de ERP

- Limites: não assume integração concluída sem runtime

- Módulo 2 · Perguntando com evidência industrial

- Anexando o payload do evento e o log do middleware

- Informando o protocolo, a topologia e o edge em uso

- Descrevendo a frequência e o volume de eventos

- Isolando perda de mensagem de erro de processamento

- Módulo 3 · Casos de uso do dia a dia

- Arquitetura de integração entre PLC, SCADA, MES e ERP

- Falhas de apontamento automático de produção

- Indicadores de OEE e rastreabilidade industrial

- Segurança OT sob a IEC 62443

- Módulo 4 · Entregas, validação e governança

- Lendo a arquitetura OT/IT proposta

- Aplicando a estratégia de observabilidade e runtime

- Validando os eventos industriais em ambiente controlado

- Estabelecendo governança e analytics industriais

Primeira aula aberta

## Cobertura: MES, SCADA, PLC, IIoT, OPC-UA e MQTT
Você aciona o expert de Automação Industrial quando o problema atravessa o chão de fábrica e precisa ser interpretado como uma cadeia industrial, não como um registro isolado no ERP. O território dele cobre PLC, SCADA, MES, IIoT, edge, OPC-UA, MQTT e a integração desses componentes com o Sankhya ERP. A cadeia que ele usa para raciocinar percorre sensor, evento, PLC, SCADA, MES, ERP, runtime, auditoria e indicador.

### Onde o expert entra
O expert analisa o significado e o percurso do evento industrial. Se um PLC sinaliza fim de ciclo, por exemplo, ele não transforma automaticamente esse sinal em produção realizada. Verifica qual camada recebeu o evento, qual sistema acrescentou contexto produtivo e onde deveria ocorrer o efeito empresarial.
Você pode procurar o expert para questões como:

- PLC executou, mas o SCADA não refletiu o estado esperado

- SCADA recebeu a tag, mas o MES não processou o evento

- MES processou uma operação, mas o Sankhya não apresenta o apontamento esperado

- um dispositivo IIoT publica telemetria por MQTT, mas há dúvida sobre duplicidade, sequência ou perda de mensagens

- um cliente OPC-UA expõe valores, mas você precisa separar comunicação técnica de evidência produtiva

OPC-UA e MQTT pertencem ao território do expert como mecanismos de interoperabilidade e transporte. Eles não substituem o significado operacional do evento. A documentação-base do expert trata explicitamente MQTT, OPC-UA, filas, retry, dead letter, idempotência, middleware e edge como partes da integração industrial.

### Onde o território do expert termina
O expert não inventa integração nativa entre Sankhya e PLC, SCADA ou MES. Também não inventa tag, tópico MQTT, NodeId OPC-UA, payload, endpoint, tabela ou campo.
No lado Sankhya, o expert separa comportamento funcional de contrato técnico. A documentação oficial de Apontamento de Produção, por exemplo, informa explicitamente que essa funcionalidade não contempla regras e integrações com MES. Quando uma integração precisar usar APIs Sankhya, ele valida o contrato no Developer; a autenticação atual documentada usa OAuth 2.0 Client Credentials e X-Token.
Se sua pergunta for puramente fiscal, financeira ou comercial, escale para o expert correspondente. Se o problema envolver o caminho entre máquina, evento, execução industrial e ERP, permaneça no expert de Automação Industrial.

Caso

### fim de ciclo no PLC não é produção no ERP
O PLC sinalizou fim de ciclo. Quantas peças foram produzidas?
O sinal diz que um ciclo terminou. Ele não carrega ordem de produção, produto, lote, qualidade nem perda — esse contexto é acrescentado depois, por outra camada.
A cadeia de raciocínio percorre sensor, evento, PLC, SCADA, MES, ERP, runtime, auditoria e indicador. Em cada salto, algo é acrescentado ou perdido.
Por isso o expert verifica qual camada recebeu o evento, qual sistema acrescentou contexto produtivo e onde deveria ocorrer o efeito empresarial.
OPC-UA e MQTT pertencem a esse território como mecanismos de interoperabilidade e transporte — e não substituem o significado operacional do evento.
Transportar bem um sinal sem contexto entrega ao ERP um número que ninguém sabe interpretar.

Leve para a prática
Antes de acionar o expert, identifique em qual elo entre sensor, PLC, SCADA, MES e ERP o comportamento esperado deixou de acontecer.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
