# Curso Transporte, Frete e Torre de Controle no Sankhya ERP (SEA-O08)

> Fonte: https://www.searchitect.pro/universidade/SEA-O08  
> Coletado em: 2026-10-14

---

## SEA-O08 — Transporte, Frete e Torre de Controle no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 5 horas, com certificado SEA Practitioner — TMS na trilha Supply Chain e Operações. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP. Aprofunda o trabalho do SEA - TMS Expert.

### Ementa

- Módulo 1 · Território do TMS Expert

- Cobertura: cotação, contratos de frete, viagens e CIOT

- Documentos: CT-e, MDF-e e o piso mínimo da ANTT

- Fronteira com Fiscal na emissão e com Logística na carga

- Limites: não calcula frete sem a tabela vigente

- Módulo 2 · Perguntando com evidência de transporte

- Anexando a tabela de frete e o contrato da transportadora

- Informando o retorno do EDI e o status do documento

- Descrevendo a rota, a carga e a modalidade contratada

- Isolando divergência de cálculo de divergência de emissão

- Módulo 3 · Casos de uso do dia a dia

- Configuração de tabelas, cotação e contratos de frete

- Emissão de CT-e e MDF-e e configuração de CIOT

- EDI com transportadoras: DOCCOB, CONEMB e OCOREN

- Torre de controle, tracking e tratamento de ocorrências

- Módulo 4 · Entregas, validação e governança

- Lendo o diagnóstico operacional e de integrações

- Aplicando o plano de implantação e a arquitetura TMS

- Revisando SQL comentada e o modelo analítico

- Conferindo a matriz de riscos e o plano de testes

Primeira aula aberta

## Cobertura: cotação, contratos de frete, viagens e CIOT
Você aciona o TMS Expert quando a dúvida está dentro da cadeia de contratação e execução do transporte: cotação, tabela e contrato de frete, transportadora, pedido de transporte, ordem de carga, viagem, afretamento e CIOT. O expert não trata uma tela isoladamente como prova de que o transporte aconteceu. Ele lê a sequência operacional e separa cada estágio.

### Da cotação à viagem
Na Cotação de Frete - TMS Transportador, a operação pode simular o valor usando origem, destino, peso, volume e valor da mercadoria. A própria rotina pode gerar um pedido de transporte com base na cotação. Isso não transforma a cotação em contratação nem prova que a viagem foi executada.
Quando você trouxer um contrato de transportadora, o expert vai confrontar as condições comerciais com a tabela efetivamente usada pelo TMS. Vigência, transportadora, modalidade, faixas, rota ou região e regra de cálculo precisam permanecer coerentes. Contrato assinado não prova que a tabela está correta, assim como uma tabela cadastrada não prova que aquele frete foi contratado.
A rotina Viagem entra depois na cadeia operacional. No TMS documentado pela Sankhya, ela concentra vínculos como pedido, ordem de carregamento, veículo e motorista e participa da emissão de CT-e e MDF-e e do cálculo de despesas como pedágio e adiantamentos. Uma viagem cadastrada, porém, continua sendo um estágio: não comprova entrega.

### Onde entra o CIOT
CIOT não é sinônimo de CT-e, MDF-e ou contrato. É o Código Identificador da Operação de Transporte registrado perante a ANTT. Desde 24 de maio de 2026, a ANTT informa a universalização da obrigatoriedade para operações de transporte rodoviário remunerado de cargas, observadas as regras e exceções vigentes. No Sankhya Log TMS, as adequações de 2026 acrescentaram à Viagem dados como Distância, Produto Principal, Tipo Operação CIOT, Cálculo do Frete Mínimo, Quantidade de Eixos e Retorno Vazio.
Por isso, quando a pergunta envolver CIOT, o expert valida também versão e SOP TMS, categoria e contexto da operação, dados da viagem e regra ANTT vigente. Não assume regularidade apenas porque existe viagem ou valor de frete informado.

Caso

### cotação, tabela, contrato e viagem
Quatro objetos que o time chama de "o frete":

- cotação — o cálculo feito para aquela operação

- tabela — a regra que o TMS usou para calcular

- contrato — a condição comercial negociada com a transportadora

- viagem — a execução real, com veículo, motorista e documentos

Contrato assinado não prova que a tabela está correta, assim como uma tabela cadastrada não prova que aquele frete foi contratado.
A divergência mais comum do domínio nasce aí: a cotação calculou certo segundo a tabela, e a tabela não representa o contrato vigente. Ninguém errou de propósito; a tabela ficou para trás.
E quando a pergunta envolve CIOT, entram ainda versão e SOP TMS, categoria e contexto da operação, dados da viagem e a regra ANTT vigente — regularidade não se presume porque existe viagem ou valor informado.

Leve para a prática
Antes de pedir ao expert uma validação de contratação, identifique separadamente a cotação, a tabela vigente, a transportadora contratada, o pedido ou viagem e, quando aplicável, a evidência do CIOT.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
