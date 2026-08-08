# Escopo de Atuação: Automatização de Estoque e Gestão de Retalhos (ERMAQ)

Este documento detalha as etapas necessárias para a transição do sistema de controle de estoque baseado em unidades fixas (Unidades Alternativas) para o novo modelo de **Medida Dinâmica**, garantindo maior flexibilidade operacional e precisão nos saldos de retalho.

## 1. Objetivo do Projeto
Eliminar a dependência de cadastros fixos na aba "Unidades Alternativas" do Sankhya, permitindo que o cálculo de consumo e sobra de chapas/barras seja feito com base no **valor de cálculo digitado pelo usuário** no momento da nota.

## 2. O que mudou no Processo?
*   **Antes**: Era necessário que cada chapa tivesse um cadastro de unidade alternativa (ex: 1 UN = 6 metros). Se a chapa fosse diferente, o processo parava ou errava.
*   **Agora**: O sistema ignora a unidade alternativa. Ele lê o campo de cálculo preenchido pelo usuário (ex: se o usuário digitar `1.6` no campo da medida, o sistema entende que aquela peça tem 1,6 metros e calcula a sobra automaticamente).

## 3. Planejamento de Tempo (Total 8 Horas)

| Atividade | Descrição | Esforço Est. |
| :--- | :--- | :--- |
| **Implantação Técnica** | Instalação da nova classe Java (`RealizaMovimentosEstoqueSemVOA`) no ambiente de testes/produção do Sankhya. | 1 Hora |
| **Testes de Compra** | Lançamento de notas de compra para validar se as quantidades continuam sendo distribuídas corretamente entre os campos `AD_...`. | 2 Horas |
| **Testes de Retalho CORE** | Testes de venda e movimentação interna com medidas variadas (ex: chapa de 1.6m, venda de 1.0m) para validar se o retalho de sobra (0.6m) é gerado corretamente sem erros. | 3 Horas |
| **Auditoria de Logs** | Verificação das tabelas de log (`AD_DETACAOLOG`) para garantir que o robô está registrando cada passo sem interrupções. | 1 Hora |
| **Ajustes e Formalização** | Revisão final dos saldos e entrega da documentação de uso aos operadores. | 1 Hora |

## 4. Roteiro de Testes (Não Técnico)
Para validar se a entrega está correta, realize os seguintes passos no sistema:

1.  **Lançamento de Nota de Compra**:
    - Escolha um produto que controla retalho.
    - Digite uma medida no campo de cálculo correspondente (ex: no campo de 6000mm, digite `1.8`).
    - Confirme a nota e verifique se o estoque "Inteiro" entrou com a medida correta.

2.  **Processamento de Venda/Uso**:
    - Realize uma venda parcial dessa medida.
    - Execute a ação agendada (Ação de Movimentos de Estoque).
    - **Critério de Sucesso**: O sistema deve dar baixa na peça "Inteira" e gerar um novo item de estoque do tipo "Retalho" com a medida exatamente igual à sobra matemática.

3.  **Verificação de Inventário**:
    - Acesse o portal de estoque e confirme se os saldos de peças inteiras e retalhos batem 100% com a realidade física.

## 5. Resultados Esperados
*   **Agilidade**: Não é mais necessário cadastrar novas unidades para chapas de tamanhos atípicos.
*   **Precisão**: O estoque de retalho passa a ser gerado fielmente ao que foi digitado na nota, sem aproximações.
*   **Autonomia**: O usuário final tem controle total sobre o fator de conversão de cada item.

---
**Documento gerado em:** 01/04/2026  
**Status:** Será refatorado o Java nos pontos de unidade alternativa que já foram mapeados na rotina que gera as requisições. Após essa refatoração, o sistema estará pronto para os testes e homologação final.
