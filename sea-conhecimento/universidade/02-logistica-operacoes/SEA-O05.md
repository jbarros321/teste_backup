# Curso Gestão de Frotas e Manutenção Veicular no Sankhya ERP (SEA-O05)

> Fonte: https://www.searchitect.pro/universidade/SEA-O05  
> Coletado em: 2026-10-14

---

## SEA-O05 — Gestão de Frotas e Manutenção Veicular no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 4 horas, com certificado SEA Practitioner — Frotas na trilha Supply Chain e Operações. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP. Aprofunda o trabalho do SEA - Frotas Expert.

### Ementa

- Módulo 1 · Território do Frotas Expert

- Cobertura: veículos, OS, pneus, abastecimento e telemetria

- Fronteira com Manutenção nos ativos e com TMS na viagem

- Quando o dado vem do rastreador e quando vem do ERP

- Limites: não infere hodômetro sem a leitura registrada

- Módulo 2 · Perguntando com evidência de frota

- Anexando a OS, o plano de manutenção e o histórico do veículo

- Informando a fonte da telemetria e a frequência de coleta

- Descrevendo o ciclo de abastecimento praticado

- Recortando veículo, motorista e período

- Módulo 3 · Casos de uso do dia a dia

- Implantação da gestão de frotas e modelagem operacional

- Manutenção preventiva, corretiva e controle de pneus

- Gestão de abastecimentos e consumo por KM

- Motoristas, documentos, multas e integração com rastreadores

- Módulo 4 · Entregas, validação e governança

- Lendo o diagnóstico e a arquitetura de solução

- Aplicando o plano de implantação e sustentação

- Revisando a SQL e o modelo analítico entregues

- Estabelecendo observabilidade e governança da frota

Primeira aula aberta

## Cobertura: veículos, OS, pneus, abastecimento e telemetria
O SEA · Frotas Expert atua sobre a operação de frota como uma cadeia conectada. Você pode acioná-lo para entender o cadastro e a governança de veículos, ordens de serviço, manutenção veicular, abastecimentos, pneus, hodômetro, horímetro, documentos, multas, rastreadores, GPS, telemetria, custos e os efeitos desses eventos sobre a disponibilidade da frota. O escopo também alcança integrações, SQL e BI, observabilidade e sustentação quando o problema continua sendo a operação da frota.

### Veículo e manutenção veicular
O veículo é o ativo central. A partir dele, o expert acompanha eventos que alteram sua condição operacional ou produzem evidência de utilização. Na Gestão de Frotas do Sankhya, o subprocesso 11.00.605 — Ordens de Serviço usa a rotina Gestão de Frotas, Rotinas, Ordem de Serviço. Uma OS pode ser lançada manualmente ou originada pelo processo de manutenção preventiva; nela entram máquina, empresa, plano de manutenção, quilometragem ou hora atual e os produtos empregados no serviço.
Isso permite ao expert analisar uma dúvida como "por que esta preventiva não ocorreu?" dentro da cadeia veículo, KM ou horímetro, plano e OS, sem assumir que a manutenção aconteceu apenas porque havia um plano cadastrado.

### Pneus, combustível e telemetria
Pneus também pertencem ao território. O Sankhya possui operação para montagem, desmontagem, rodízio, recape, descarte, medição de sulco e aferição de pressão. A movimentação mantém histórico associado a data e hora, KM e usuário.
No abastecimento, o expert trata o evento como parte da governança de combustível e da quilometragem. No rastreamento, trata configuração, integração, posições recebidas e evidência de processamento. A integração oficial com rastreadores pode registrar latitude e longitude em TCFPOSICAOVEI e, quando o log está habilitado, registrar processamento em TMSLOGINTEGRACAO.
A regra operacional é simples: cadastro mostra contexto; evento mostra atividade; runtime mostra o que realmente aconteceu. O expert não transforma tela preenchida, integração configurada ou plano existente em confirmação de execução.

Caso

### o veículo que aparece em cinco registros
Placa ABC-1D23, mês de julho. O mesmo veículo produz eventos de naturezas distintas:

- OS — uma corretiva aberta em 08/07

- plano — preventiva prevista para 84.000 km

- abastecimento — quatro registros, 620 litros

- hodômetro — leituras vindas de abastecimento, OS, portaria e checklist

- telemetria — posições e KM do rastreador

Cada fonte responde uma pergunta e nenhuma responde todas. O KM que aparece na OS e o KM do histórico podem divergir legitimamente, porque foram capturados em momentos e por origens diferentes.
O histórico de hodômetro é consultável pela rotina Histórico KM Veículo e centralizado em `TMSVEIHKM`, com origem registrada.
Saber de onde veio cada número é o que separa uma divergência real de duas medições corretas em instantes diferentes.

Leve para a prática
Classifique sua demanda pelo evento de frota envolvido — veículo, OS, pneu, abastecimento ou telemetria — e procure o Frotas Expert quando a decisão depender da governança operacional desse veículo.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
