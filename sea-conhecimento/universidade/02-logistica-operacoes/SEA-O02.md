# Curso Procurement do Pedido à Entrada Fiscal no Sankhya ERP (SEA-O02)

> Fonte: https://www.searchitect.pro/universidade/SEA-O02  
> Coletado em: 2026-10-14

---

## SEA-O02 — Procurement do Pedido à Entrada Fiscal no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 5 horas, com certificado SEA Practitioner — Compras na trilha Supply Chain e Operações. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP. Aprofunda o trabalho do SEA - Compras Expert.

### Ementa

- Módulo 1 · Território do Compras Expert

- Cobertura: cotação a pedido, workflows e aprovações

- O efeito cascata de uma compra: estoque, financeiro e fiscal

- Fronteira com o Fiscal na entrada de XML

- Limites: não valida integração só pelo HTTP 200

- Módulo 2 · Perguntando com evidência de compras

- Anexando o pedido, a TOP de compra e o XML de entrada

- Descrevendo o fluxo de aprovação configurado

- Informando o retorno da integração REST ou do portal

- Isolando o ponto exato onde o fluxo parou

- Módulo 3 · Casos de uso do dia a dia

- Pedido de compra que não gera financeiro ou fiscal

- Validação de parametrização de TOP de compra

- Investigação de XML de entrada rejeitado

- Integração com portal de compras via REST e Gateway

- Módulo 4 · Entregas, validação e governança

- Lendo o diagnóstico completo e a análise de impacto

- Aplicando o checklist de validação e homologação

- Desenhando a arquitetura de Procurement

- Usando SQL de diagnóstico e SDK Java com segurança

Primeira aula aberta

## Cobertura: cotação a pedido, workflows e aprovações
Você aciona o Compras Expert quando a dúvida está dentro do ciclo operacional de Procurement no Sankhya: solicitação, cotação, fornecedor, aprovação, pedido de compra, recebimento e preparação da entrada. O trabalho dele não é olhar apenas a tela onde o sintoma apareceu. O expert segue o documento e as dependências que determinam seu comportamento.

### Onde começa o território do expert
Na operação de compras, uma solicitação pode alimentar a cotação e a cotação pode resultar em pedido, mas você não deve tratar essa sequência como universal. Parametrização, permissões, workflow e TOP podem mudar o caminho. A própria documentação Sankhya mostra dependências de produto, grupo de produto e TOP para geração de solicitação de compra.
Quando existe um pedido, estruturas que o expert conhece e pode usar na investigação incluem:

- TGFCAB, para o cabeçalho do documento

- TGFITE, para os itens

- TGFTOP, para o Tipo de Operação

- TGFPAR, quando fornecedor ou parceiro faz parte da análise

Na TOP, não olho somente o código. TGFCAB.CODTIPOPER identifica o tipo de operação e TGFCAB.DHTIPOPER permite relacionar o documento à versão histórica correspondente em TGFTOP.DHALTER. Isso evita analisar uma configuração atual e presumir que ela era a configuração válida quando o documento foi processado.

### Workflow e aprovação não são sinônimos de pedido liberado
Se uma cotação não vira pedido ou um pedido fica bloqueado, o expert não conclui imediatamente que existe defeito no workflow. O terreno dele inclui verificar o encadeamento operacional: usuário, permissões, aprovação, TOP, parâmetros e evidência do estado real do documento.
Você também precisa reconhecer a fronteira do expert. Se a causa sair de Procurement e entrar exclusivamente em tributação especializada, infraestrutura, autenticação ou contrato técnico de uma API, ele preserva o contexto da compra e escala a camada especializada. Não transforma uma hipótese de compras em conclusão de outro domínio.
O padrão do expert é simples: configuração esperada não supera runtime observado. Um fluxo desenhado para aprovar automaticamente continua sendo apenas comportamento esperado até que a execução daquele documento confirme o que aconteceu.

Caso

### a solicitação que nunca virou pedido
Solicitação criada, cotação registrada, pedido inexistente. O time conclui que "o workflow está quebrado".
A sequência solicitação → cotação → pedido é comum e não é universal: parametrização, permissões, workflow e TOP podem mudar o caminho. A própria documentação mostra dependências de produto, grupo de produto e TOP para a geração de solicitação de compra.
Antes de acusar o workflow, o encadeamento operacional precisa ser verificado: usuário, permissões, aprovação, TOP, parâmetros e o estado real do documento.
E existe o limite que organiza a investigação inteira: configuração esperada não supera runtime observado. Um fluxo desenhado para aprovar automaticamente continua sendo comportamento esperado até que a execução daquele documento confirme o que aconteceu.
Fluxograma na parede e execução no banco são duas afirmações independentes.

Leve para a prática
Ao acionar o expert sobre uma compra parada, identifique o documento, a etapa atual, a TOP usada e o ponto do fluxo em que o comportamento deixou de corresponder ao esperado.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
