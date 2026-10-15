# Curso Contratos e Faturamento Recorrente no Sankhya ERP (SEA-N05)

> Fonte: https://www.searchitect.pro/universidade/SEA-N05  
> Coletado em: 2026-10-14

---

## SEA-N05 — Contratos e Faturamento Recorrente no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 4 horas, com certificado SEA Practitioner — Contratos na trilha Núcleo ERP. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP. Aprofunda o trabalho do SEA - Contratos Expert.

### Ementa

- Módulo 1 · Território do Contratos Expert

- Cobertura: billing recorrente, provisões, reajustes e renovações

- Fronteira com Financeiro na provisão e com Fiscal na NFS-e

- O papel do scheduler e por que ele explica a maioria das falhas

- Limites: não assume job executado sem o log

- Módulo 2 · Perguntando com evidência contratual

- Anexando a configuração do contrato e a TOP aplicada

- Capturando o log do scheduler na janela do faturamento

- Informando o índice de reajuste e a data-base

- Descrevendo o ciclo esperado versus o gerado

- Módulo 3 · Casos de uso do dia a dia

- Contrato que não fatura: da parametrização ao job

- Validação de provisões financeiras geradas pelo contrato

- Análise de reajuste contratual e renovação automática

- Integração financeira e emissão de NFS-e

- Módulo 4 · Entregas, validação e governança

- Lendo o diagnóstico funcional e o plano de troubleshooting

- Aplicando o checklist de validação do ciclo de billing

- Montando observabilidade sobre o faturamento recorrente

- Medindo o impacto financeiro e fiscal antes de corrigir

Primeira aula aberta

## Cobertura: billing recorrente, provisões, reajustes e renovações
Você aciona o Contratos Expert quando a dúvida está no ciclo contratual que transforma uma obrigação recorrente em faturamento e seus efeitos relacionados. O território dele inclui contrato, vigência, ocorrência, produtos ou serviços, preço, quantidade, periodicidade, referência de faturamento, TOP aplicada ao ciclo, provisões, reajuste, renovação e o vínculo do contrato com os documentos gerados.

### O que o expert cobre no billing recorrente
O expert não trata "contrato cadastrado" como sinônimo de "contrato faturável". Quando analisa billing, separa cadastro, elegibilidade, execução e resultado. A própria operação do Sankhya possui rotinas distintas para Faturamento de Contratos e Agendador para Faturamento de Contratos. No agendador, filtros como Empresa, Parceiro e Data de Reajuste convivem com parâmetros de faturamento, TOP, tipo de título, tipo de negociação, referência e série.
Por isso, você pode procurar o expert para entender por que um ciclo recorrente não produziu o resultado esperado ou por que produziu valor, referência ou documento divergente. Ele vai separar o que está configurado do que realmente aconteceu.

### Provisões, reajustes e renovações
Provisão faz parte do território do expert enquanto efeito originado ou governado pelo contrato. No Cadastro de Contratos, a quantidade de provisões pode ser definida pela configuração contratual ou, quando aplicável, pelo parâmetro QTDMESPROVFATC. A documentação também descreve que o faturamento pode baixar a provisão correspondente e que reajustes exigem atenção às parcelas futuras.
Reajuste e renovação também são eventos contratuais. O expert analisa vigência, ocorrência, preço vigente, referência e efeito esperado, mas não declara que um reajuste ou uma renovação ocorreu apenas porque estava previsto. A operação precisa deixar evidência.
O limite do expert começa quando a pergunta deixa de ser "o que o contrato deveria originar?" e passa a ser exclusivamente financeira, fiscal ou de infraestrutura. Nessa fronteira, ele continua correlacionando a origem contratual, mas não substitui o especialista do domínio de destino.

Caso

### contrato cadastrado, contrato faturável
O contrato 3182 existe, está ativo e não faturou em julho. "Mas está cadastrado", diz o time.
Cadastro e elegibilidade são camadas diferentes, e o próprio Sankhya separa as rotinas: existe o Faturamento de Contratos e existe o Agendador para Faturamento de Contratos. No agendador, filtros como Empresa, Parceiro e Data de Reajuste convivem com parâmetros de faturamento, TOP, tipo de título, tipo de negociação, referência e série.
Ou seja: um contrato pode estar impecável e simplesmente não entrar na seleção daquela execução.
O expert separa quatro estados: cadastro, elegibilidade, execução e resultado. Cada um falha de um jeito e produz o mesmo sintoma na tela.
Reajuste e renovação seguem a mesma lógica: estavam previstos não significa que ocorreram. A operação precisa deixar evidência.

Leve para a prática
Traga ao Contratos Expert problemas em que contrato, recorrência, provisão, reajuste ou renovação sejam a origem do ciclo, e não trate cadastro ou previsão como prova de execução.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
