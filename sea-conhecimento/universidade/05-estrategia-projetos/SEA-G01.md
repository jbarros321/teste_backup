# Curso Orquestração de Demandas com o Agent Core no Sankhya ERP (SEA-G01)

> Fonte: https://www.searchitect.pro/universidade/SEA-G01  
> Coletado em: 2026-10-14

---

## SEA-G01 — Orquestração de Demandas com o Agent Core no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 4 horas, com certificado SEA Practitioner — Orquestração na trilha Orquestração e Governança. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP.

### Ementa

- Módulo 1 · Território do Agent Core

- O papel de gateway: ele roteia, não resolve

- Classificação de solicitação, domínio e subdomínio

- Quando o Core encaminha para mais de um especialista

- Limites: não executa o trabalho do expert de destino

- Módulo 2 · Descrevendo a demanda para triagem

- Sintoma, contexto e impacto: os três blocos do pedido

- Eliminando ambiguidade antes de pedir encaminhamento

- Classificando a complexidade da própria demanda

- O que fazer quando a triagem devolve o expert errado

- Módulo 3 · Engenharia de prompt aplicada

- Lendo o prompt que o Core gera para o especialista

- Ajustando o prompt antes de levá-lo adiante

- Context engineering: o que preservar entre experts

- Encadeando especialistas em problemas multidomínio

- Módulo 4 · Entregas e uso institucional

- Interpretando a justificativa técnica do roteamento

- Usando o Core para onboarding de usuários novos

- Medindo acerto de triagem ao longo do tempo

- Quando pular o Core e ir direto ao especialista

Primeira aula aberta

## O papel de gateway: ele roteia, não resolve
O Agent Core ocupa a entrada do ecossistema SEA. Quando você chega com "minha nota confirmou, mas não gerou financeiro", a função do Core não é investigar a TGFFIN, explicar a TOP ou propor uma correção. A função dele é descobrir quem deve tratar o problema e preservar informação suficiente para que a demanda chegue ao destino certo.

### O gateway decide o destino
Pense no Core como uma camada de orquestração. Ele recebe uma intenção de alto nível, identifica o território envolvido e seleciona o especialista adequado. A separação é deliberada: o Core coordena; os experts aprofundam verticalmente.
Uma demanda pode mencionar objetos reais do Sankhya sem que isso autorize o Core a resolvê-la. Exemplos:

- ter o NUNOTA 84521 com a nota confirmada e nenhum NUFIN encontrado contém sinais de Comercial, TOP e Financeiro

- uma NF-e rejeitada com XML e retorno do autorizador aponta para Fiscal e documentos eletrônicos

- uma chamada que retorna timeout sem indicar se o documento foi criado combina Integrações com runtime e exige preservar payload, resposta e identificadores disponíveis

O Core usa esses sinais para classificar e encaminhar. Ele não transforma o reconhecimento do assunto em competência para executar a análise especializada.

### O que você deve entregar ao gateway
Dê ao Core elementos que melhorem a classificação: objetivo, módulo aparente, objeto afetado e evidências disponíveis. Identificadores como NUNOTA, NUFIN, CODTIPOPER, mensagens de erro, XML, endpoint ou stacktrace podem ser sinais decisivos quando realmente fizerem parte do caso.
Antes de anexar evidências, remova credenciais, tokens, secrets, dados bancários e dados pessoais desnecessários. O Core precisa de contexto suficiente para rotear, não de exposição indiscriminada de dados.
Leia a entrega dele pelo que ela é: classificação, destino e justificativa, não solução técnica. Se ele identifica Financeiro como destino de uma ausência de título, isso não prova que houve falha na geração financeira. Essa conclusão pertence à investigação especializada.

Caso

### o Core que quase respondeu
Chega: "minha nota confirmou, mas não gerou financeiro".
A tentação do gateway é começar a investigar — abrir a TGFFIN, discutir a TOP, propor uma correção. Fazer isso seria assumir o trabalho de quem tem profundidade para fazê-lo melhor.
A função do Core é outra: descobrir quem deve tratar o problema e preservar informação suficiente para que a demanda chegue ao destino certo.
Na prática, ele transforma o relato em: documento identificado, efeito esperado, efeito observado, momento, ambiente — e conclui que o caso atravessa comercial, TOP, financeiro e runtime.
Isso não é meia resposta. É o que impede a demanda de chegar ao expert financeiro sem contexto e voltar pedindo informação que já existia.
Um gateway que resolve vira gargalo. Um gateway que roteia bem multiplica a capacidade de todo o resto.

Leve para a prática
Descreva sua próxima demanda com objetivo, objeto afetado e evidências disponíveis e use o Core para escolher o especialista, não para obter dele a solução vertical.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
