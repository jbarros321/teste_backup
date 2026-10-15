# Curso Governança Contábil e SPED no Sankhya ERP (SEA-N04)

> Fonte: https://www.searchitect.pro/universidade/SEA-N04  
> Coletado em: 2026-10-14

---

## SEA-N04 — Governança Contábil e SPED no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 5 horas, com certificado SEA Practitioner — Contábil na trilha Núcleo ERP. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP. Aprofunda o trabalho do SEA - Contábil Expert.

### Ementa

- Módulo 1 · Território do Contábil Expert

- Cobertura: parametrização contábil, plano de contas, ECD e ECF

- As quatro integrações que alimentam a contabilidade

- Quando a diferença nasce no módulo de origem, não no Contábil

- Limites: não assume contabilização sem o lote gerado

- Módulo 2 · Perguntando com evidência contábil

- Anexando o lote contábil e o lançamento divergente

- Informando o plano de contas e o referencial em uso

- Descrevendo qual módulo originou o movimento

- Recortando o período exato da divergência

- Módulo 3 · Casos de uso do dia a dia

- Diagnóstico de contabilização incorreta na origem

- Validação das integrações Financeiro, Fiscal, Estoque e Folha

- Auditoria de lançamentos e lotes com SQL segura

- Geração e validação de ECD e ECF

- Módulo 4 · Entregas, validação e governança

- Lendo o diagnóstico técnico-contábil e o plano de ação

- Aplicando o checklist Enterprise e o de SPED

- Conferindo a SQL de auditoria antes de rodar em produção

- Fechando o ciclo com evidência para o auditor

Primeira aula aberta

## Cobertura: parametrização contábil, plano de contas, ECD e ECF

### Onde acionar o Contábil Expert
Você aciona o Contábil Expert quando a pergunta está dentro da governança contábil do Sankhya: parametrização da empresa contábil, plano de contas, plano referencial, lançamentos, lotes, saldos e preparação ou diagnóstico de ECD e ECF. O papel dele não é apenas explicar conceito contábil. O expert conecta configuração, movimento registrado e evidência de runtime para dizer o que está confirmado e o que ainda precisa ser provado.
No plano de contas, por exemplo, você pode trazer problemas de estrutura, classificação, conta analítica ou sintética, máscara e relacionamento com conta referencial. O Sankhya mantém o vínculo da conta contábil com a conta referencial no Plano de Contas, e existem validações específicas dessa associação. Uma mensagem real desse terreno é "Existem Contas Contábeis Referenciais que possuem Natureza diferente do Grupo de Conta informado. Conforme manual da ECF isso não é possível. Verifique!"
Quando a investigação exigir banco de dados, estruturas como TCBPLA, TCBCTA, TCBLOT e TCBLAN fazem parte do território de validação do expert. O nome conhecido da tabela, porém, não o autoriza a inventar campos ou relacionamentos: estrutura deve ser confirmada no dicionário do ambiente.

### ECD e ECF não começam no arquivo
O expert trata ECD e ECF como resultado de uma cadeia contábil. Plano, lançamentos, saldos, período e plano referencial precisam sustentar o arquivo. No Sankhya, a própria geração da ECD e da ECF depende de configurações nas Preferências da Empresa de Contabilidade e no Plano de Contas.
Por isso, uma crítica como "Conta Informada deve existir no plano de contas e ser analítica" não deve ser tratada como simples problema de texto do SPED. Ela pode exigir rastrear a conta criticada no PVA e confrontar plano, registros e saldos que originaram o arquivo.
O expert também não declara uma obrigação válida porque o Sankhya conseguiu gerar o arquivo. ECD e ECF só ganham evidência de validação depois da conferência no programa oficial aplicável. A Receita disponibiliza programas próprios para validar essas escriturações.

Caso

### a conta referencial com natureza incompatível
A mensagem é literal: "Existem Contas Contábeis Referenciais que possuem Natureza diferente do Grupo de Conta informado. Conforme manual da ECF isso não é possível. Verifique!"
Ela não é um erro de tela. É a cadeia contábil avisando que o vínculo entre a conta do plano e a conta referencial não sobrevive à regra da obrigação.
A investigação percorre plano de contas, natureza, grupo, conta referencial e a regra da ECF que sustenta a crítica. Estruturas como TCBPLA, TCBCTA, TCBLOT e TCBLAN entram quando a análise desce ao banco — sempre confirmadas no dicionário do ambiente, nunca deduzidas pelo nome.
E vale a separação que fecha o módulo: o Sankhya conseguir gerar o arquivo não torna a obrigação válida. ECD e ECF só ganham evidência de validação depois da conferência no programa oficial aplicável.

Leve para a prática
Antes de pedir ao expert uma explicação para uma divergência de ECD ou ECF, identifique empresa, período, plano de contas e plano referencial envolvidos e separe geração do arquivo de validação no PVA.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
