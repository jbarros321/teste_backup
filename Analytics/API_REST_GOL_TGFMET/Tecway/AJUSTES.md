# Registro de Ajustes — Tecway (index.html)

## 15/07/2026 — Espaço para assinatura na Página 3 (Balanço Patrimonial)

**Problema:** na página 3, o texto "As notas explicativas são parte integrante das demonstrações financeiras.", exibido logo após os dados de Ativo e Passivo, ficava colado na linha de assinatura da contadora Jeise Hellen Pires Ferreira (espaçamento de apenas `mt-4`), sem deixar espaço para a assinatura manuscrita.

**Correção (1ª tentativa):** no rodapé da página 3 (`div.page.bp-report-page > footer`), o bloco de assinaturas teve o espaçamento superior alterado de `mt-4` para `mt-16`. O texto ainda permanecia visualmente sobre o campo de assinatura.

**Correção (definitiva):** o parágrafo das notas explicativas foi removido do rodapé e movido para dentro do conteúdo principal (`#bp-report-content`), logo após o bloco de totais (`#totais-container`), com `mt-8`. O rodapé da página 3 agora contém apenas as linhas de assinatura, deixando todo o espaço acima da linha da Jeise livre para a assinatura manuscrita.

**Arquivo alterado:** `index.html` (somente a página 3 do Balanço Patrimonial).

**Observação:** as demais páginas (DRE, DFC, etc.) usam o mesmo padrão de rodapé com `mt-4`; caso o mesmo problema apareça nelas na impressão, aplicar o mesmo ajuste. Os arquivos `indexfinal.html` e `indexBKP.html` não foram alterados.

## 15/07/2026 — Ordem das imagens das notas 26.1 e 26.2 (página 16)

**Problema:** na página das notas explicativas que exibe as imagens (tabelas de demandas judiciais e de crédito Pis/Cofins), as imagens apareciam trocadas: a imagem da nota 26.2 saía após a 26.1 e vice-versa.

**Correção:** a inversão foi aplicada nos dois caminhos de vínculo das imagens:
1. **Remapeamento por posição:** a lista coletada por `collectNotaImagesInQueryOrder` (ordem `DT_CRIACAO` da `varNotaImg`) passou a ser invertida com `.reverse()` antes da distribuição em `NOTAS_IMG_POSICOES = ["26.1", "26.2"]`.
2. **Vínculo por ID (fallback):** quando o remapeamento por posição não ativa, as imagens resolvidas por `ID_NOTAS_EXPLICATIVAS_DET`/`ID_CAB` das notas 26.1 e 26.2 são trocadas explicitamente entre si (`imageRowsTroca`), já que o cadastro grava o vínculo invertido.

**Correção definitiva (identificação por título):** descobriu-se que a numeração das notas muda conforme o mês de referência (25.1/25.2 num mês, 26.1/26.2 em outro), então o código que amarrava a troca aos números fixos "26.1"/"26.2" não ativava em outros meses. A identificação das duas notas passou a ser feita pelo **título** (regex `demanda|cobranc` para Demandas judiciais de cobrança e `pis|cofins` para Recuperação de crédito de Pis/Cofins, com normalização de acentos), e a numeração de exibição é resolvida dinamicamente a partir da nota encontrada. A troca vale para os dois caminhos (remapeamento por posição e vínculo por ID), em qualquer numeração.

**Dedup de imagens:** `collectNotaImagesInQueryOrder` passou a deduplicar por URL — as duas queries de imagem podem retornar as mesmas linhas, o que dobrava a lista e embaralhava a distribuição por posição (as duas imagens acabavam empilhadas na nota de Pis/Cofins). Com a lista limpa e invertida, a tabela de contratos sai após "Demandas judiciais de cobrança" e a tabela Pis/Cofins após "Recuperação de crédito de Pis/Cofins".

**Importante:** o `indexfinal.html` era uma cópia idêntica ao `index.html` antigo (sem nenhum dos ajustes). Ele passou a ser sincronizado com o `index.html` a cada ajuste — os dois arquivos estão idênticos e atualizados. O `indexBKP.html` permanece intocado como backup.

## Histórico anterior (09–10/07/2026)

- Ajustes de espaçamento do rodapé/notas no relatório DEM.
- Bloco de assinaturas replicado para todas as páginas do relatório (antes existia apenas na primeira e na última).
- Correção do botão de exportação para Excel no módulo de Lançamentos (Analytics).
