# Curso Arquitetura Financeira e Tesouraria no Sankhya ERP (SEA-N02)

> Fonte: https://www.searchitect.pro/universidade/SEA-N02  
> Coletado em: 2026-10-14

---

## SEA-N02 — Arquitetura Financeira e Tesouraria no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 5 horas, com certificado SEA Practitioner — Financeiro na trilha Núcleo ERP. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP. Aprofunda o trabalho do SEA - Financeiro Expert.

### Ementa

- Módulo 1 · Território do Financeiro Expert

- Cobertura: TGFFIN, PIX, CNAB, Open Finance e conciliação

- Fronteira com o Contábil, o Comercial e o Contratos

- Quando o problema é de integração bancária e não de ERP

- Limites: não assume liquidação sem evidência de runtime

- Módulo 2 · Perguntando com evidência financeira

- Anexando o retorno CNAB e o log da liquidação

- Descrevendo o fluxo esperado versus o observado

- Informando layout do banco, versão e ambiente

- Mascarando conta, agência e valores antes de enviar

- Módulo 3 · Casos de uso do dia a dia

- Investigação de falha de liquidação no TGFFIN

- Divergência de conciliação entre extrato e ERP

- Projeto de integração bancária com PIX e Open Finance

- Modelagem de fluxo de caixa e estratégia de tesouraria

- Módulo 4 · Entregas, validação e governança

- Lendo a arquitetura financeira e o plano de integração

- Aplicando o checklist de governança e compliance

- Projetando observabilidade sobre o ciclo financeiro

- Validando a conciliação em janela controlada

Primeira aula aberta

## Cobertura: TGFFIN, PIX, CNAB, Open Finance e conciliação
Você aciona o Financeiro Expert quando a pergunta está no ciclo financeiro e de tesouraria: título, baixa, liquidação, banco, PIX, CNAB, Open Finance, conciliação, fluxo de caixa ou integração financeira. No Sankhya, a TGFFIN está no centro desse território. A própria documentação pública de desenvolvimento confirma TGFFIN como tabela financeira, e a API pública expõe recursos de receitas com filtros de empresa, parceiro, vencimento, data de baixa e status financeiro.

### Onde o Financeiro Expert entra
Você pode chamar o Financeiro Expert para investigar um título identificado por NUFIN, relacionar sua origem financeira com uma nota por NUNOTA, analisar uma baixa ou verificar por que o comportamento financeiro divergiu do esperado. Evidência real de runtime também mostra operações e consultas sobre TGFFIN usando NUFIN e NUNOTA, além de campos financeiros como VLRDESDOB e VLRBAIXA.
No lado bancário, o território do expert continua quando o título atravessa a fronteira do ERP:

- CNAB de remessa e retorno, ocorrências, rejeições e baixa automática

- PIX de cobrança ou pagamento, incluindo integração bancária e conciliação

- Open Finance dentro da arquitetura financeira e bancária

- contas e movimentações bancárias

- matching entre título, baixa, extrato e movimento bancário

- rastreabilidade entre ERP, arquivo, mensagem e retorno

A documentação Sankhya confirma, por exemplo, PIX via CNAB com remessa e retorno e uso de TXID para associação da transação à cobrança.

### O que "conciliação" significa aqui
O expert não trata conciliação como sinônimo de "o título está baixado". Você pode ter informação no ERP e ainda precisar verificar o movimento bancário correspondente. O terreno dele é justamente analisar essa cadeia sem apagar suas diferenças: título financeiro, baixa, evento bancário e conciliação são estados relacionados, mas não equivalentes. Os materiais operacionais do expert definem conciliação como relação entre registros financeiros do ERP e movimentações bancárias, envolvendo títulos, baixas, extratos, diferenças e ajustes.
Quando você procura o expert, espere uma resposta que separa o que o ERP registra, o que a integração transportou e o que a fonte bancária efetivamente confirmou.

Caso

### quatro afirmações sobre o mesmo título
O título NUFIN 55210, de R$ 18.400, vence hoje. Quatro frases circulam sobre ele:

- "o título está baixado" — estado no ERP, verificável em TGFFIN

- "o pagamento foi enviado ao banco" — remessa gerada, que prova envio e não liquidação

- "o banco liquidou" — exige retorno bancário; nenhum campo do ERP sustenta isso sozinho

- "está conciliado" — exige que o movimento bancário tenha sido casado com o registro financeiro

As quatro descrevem estados relacionados e não equivalentes. É perfeitamente possível ter o primeiro sem o terceiro: alguém baixou manualmente e o crédito nunca entrou.
Quem trata os quatro como sinônimos fecha o mês com um caixa que não existe. O expert responde separando o que o ERP registra, o que a integração transportou e o que a fonte bancária efetivamente confirmou.

Leve para a prática
Classifique seu caso como título, baixa, PIX, CNAB, Open Finance, integração bancária ou conciliação antes de acionar o Financeiro Expert.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
