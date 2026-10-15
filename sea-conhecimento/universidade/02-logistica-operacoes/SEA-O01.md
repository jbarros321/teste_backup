# Curso Integridade de Estoque e Movimentações no Sankhya ERP (SEA-O01)

> Fonte: https://www.searchitect.pro/universidade/SEA-O01  
> Coletado em: 2026-10-14

---

## SEA-O01 — Integridade de Estoque e Movimentações no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 5 horas, com certificado SEA Practitioner — Estoques na trilha Supply Chain e Operações. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP. Aprofunda o trabalho do SEA - Estoques Expert.

### Ementa

- Módulo 1 · Território do Estoques Expert

- Cobertura: TGFEST, TGFCAB, TGFITE, custos e inventário

- Fronteira com WMS no endereçamento e com Compras na entrada

- Quando a divergência é de movimentação e quando é de custo

- Limites: não assume movimentação sem o registro na tabela

- Módulo 2 · Perguntando com evidência de estoque

- Anexando o extrato da movimentação e o saldo apurado

- Informando a TOP que originou o lançamento

- Recortando produto, local e período da divergência

- Separando erro de saldo de erro de custo

- Módulo 3 · Casos de uso do dia a dia

- Divergência de saldo entre o físico e o TGFEST

- Parametrização de TOPs que movimentam estoque

- Condução e fechamento de inventário

- Desenho de endereçamento e preparação para WMS

- Módulo 4 · Entregas, validação e governança

- Lendo o diagnóstico técnico e o plano de troubleshooting

- Aplicando o plano de inventário e a estratégia de supply

- Usando SQL e APIs de estoque com segurança

- Estabelecendo rastreabilidade e governança do saldo

Primeira aula aberta

## Cobertura: TGFEST, TGFCAB, TGFITE, custos e inventário

### Onde o Estoques Expert entra
Você aciona o Estoques Expert quando a pergunta central é integridade de estoque: saldo, origem de movimentação, custo, valuation ou inventário. O terreno estrutural dele inclui TGFEST como saldo de estoque, TGFCAB como cabeçalho da nota ou movimento, TGFITE como itens e TGFCUS como histórico de custos. A ligação documentada entre TGFCAB e TGFITE usa NUNOTA; produto, empresa e local também fazem parte do recorte necessário para entender onde determinado saldo está sendo analisado.
Não trate essas estruturas como quatro versões da mesma informação. TGFCAB e TGFITE representam o documento e seus itens; TGFEST representa saldo. TGFCUS pertence à camada de custos. Essa separação determina o tipo de pergunta que você deve fazer ao expert.

### O que o expert consegue distinguir
Se você mostra uma nota com quantidade correta em TGFITE, mas encontra saldo inesperado em TGFEST, o expert trata isso como uma investigação de movimentação versus estado resultante. A existência do documento não prova, sozinha, que todo o efeito esperado chegou corretamente ao estoque.
Se a quantidade está coerente, mas o valor atribuído ao estoque está divergente, a investigação muda para custos. A documentação operacional usada pela SEA trata custo como valuation do estoque e registra que custo médio pode depender de entradas, saídas, produção, devoluções, ajustes e transferências. A Ajuda Sankhya também documenta atualmente o custo médio pela Média Ponderada Móvel e relaciona divergências a saldo e reprocessamento de custos.
Inventário continua dentro do território do expert porque confronta a realidade física com o estado sistêmico. Mas ele não considera uma contagem física, uma nota ou um custo isolado como prova suficiente de integridade. O inventário precisa preservar contagem, divergência, auditoria e rastreabilidade; a própria base SEA proíbe assumir que inventário físico e sistêmico são automaticamente equivalentes.
Você procura o expert para descobrir se documento, item, saldo e valuation contam a mesma história operacional.

Caso

### quatro estruturas, quatro perguntas
Produto 450, empresa 1, local 101. O mesmo produto aparece em quatro lugares, e cada um responde uma coisa diferente:

- TGFCAB — existiu um documento que deveria movimentar

- TGFITE — o documento previa 12 unidades daquele produto

- TGFEST — o saldo resultante hoje é 74

- TGFCUS — o histórico de custos daquele período

Tratá-las como quatro versões da mesma informação é a origem de metade dos diagnósticos errados de estoque. Documento e itens representam a intenção registrada; TGFEST representa o estado resultante; custos são outra camada inteira.
É por isso que uma nota com quantidade correta em TGFITE convive tranquilamente com saldo inesperado em TGFEST: a existência do documento não prova que o efeito chegou ao estoque.
A pergunta que você faz determina a estrutura que responde. Escolher a estrutura primeiro é o caminho para provar a coisa errada.

Leve para a prática
Classifique sua dúvida como documento e movimentação, saldo, custo ou inventário antes de acionar o Estoques Expert.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
