# Curso Construção de Telas e Runtime Visual no Sankhya ERP (SEA-T05)

> Fonte: https://www.searchitect.pro/universidade/SEA-T05  
> Coletado em: 2026-10-14

---

## SEA-T05 — Construção de Telas e Runtime Visual no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 5 horas, com certificado SEA Practitioner — Screen Builder na trilha Plataforma Técnica e Dados. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP. Aprofunda o trabalho do SEA - Construtor de Telas Expert.

### Ementa

- Módulo 1 · Território do Screen Builder Expert

- Cobertura: metadados XML, componentes visuais e eventos

- Telas mestre-detalhe e o ciclo de vida da tela

- Fronteira com SDK nas rotinas Java e ActionButtons

- Limites: não afirma ordem de eventos sem evidência

- Módulo 2 · Perguntando com evidência de tela

- Anexando o XML da tela e o comportamento observado

- Informando a versão e o ambiente de execução

- Descrevendo a sequência de interação do usuário

- Isolando erro de metadado de erro de rotina

- Módulo 3 · Casos de uso do dia a dia

- Projeto de nova tela e definição de componentes

- Modelagem mestre-detalhe e configuração de eventos

- Desenvolvimento de ActionButtons e rotinas Java

- Diagnóstico de performance e persistência da tela

- Módulo 4 · Entregas, validação e governança

- Revisando o XML proposto antes de aplicar

- Validando transações e persistência em homologação

- Documentando a tela e a decisão arquitetural

- Planejando a evolução técnica do componente

Primeira aula aberta

## Cobertura: metadados XML, componentes visuais e eventos
Você aciona o SEA · Construtor de Telas Expert quando a pergunta nasce na estrutura da tela e no comportamento que ela produz em execução. O território dele inclui metadados, XML, componentes visuais, eventos, ações, integração com rotinas e a relação dessas peças com o Runtime.

### O que o expert cobre
O expert analisa a cadeia completa da tela, sem tratar XML como sinônimo de comportamento:

- metadados que descrevem a estrutura e a configuração da tela

- campos, grades, formulários, abas, containers, painéis e demais componentes suportados

- propriedades e relacionamentos que possam ser comprovados na versão analisada

- Aba Eventos e sua relação com entidades

- Aba Ações e os mecanismos oficialmente disponíveis

- comportamento observado quando o Runtime interpreta essa configuração

A documentação pública não especifica integralmente a estrutura interna de todos os metadados e componentes. Por isso, o expert não inventa uma tag XML, propriedade, ID ou atributo porque seu nome parece plausível. Metadado não documentado precisa ser confrontado com a configuração real do ambiente.

### Eventos fazem parte do território
Na Aba Eventos, a plataforma permite configurar eventos associados a entidades de telas nativas ou adicionais, com execução por Rotina Java ou Rotina de Banco de Dados. A própria configuração permite salvar a ordem dos eventos ativos. Isso é evidência sobre os eventos configurados naquela entidade; não autoriza transformar nomes conceituais de ciclo de vida em eventos oficiais sem confirmação.
O trabalho do expert é separar configuração declarada de execução observada. Se um campo aparece corretamente no XML, mas não assume o valor esperado durante a operação, a investigação não termina no XML. O componente pode depender de evento, regra, relacionamento, rotina ou contexto de execução.
Quando você recebe uma conclusão do expert, leia também o nível de confiança. Uma propriedade confirmada pela documentação e pelo ambiente tem peso diferente de uma hipótese arquitetural ainda sem reprodução em Runtime.

Caso

### o campo certo no XML e vazio na tela
A configuração está correta. O campo não assume o valor esperado durante a operação.
A investigação não termina no XML. O componente pode depender de evento, regra, relacionamento, rotina ou contexto de execução — e nenhum deles aparece na declaração do campo.
O trabalho é separar configuração declarada de execução observada. São duas afirmações distintas, e o XML só sustenta a primeira.
Some a isso um limite estrutural: a documentação pública não especifica integralmente a estrutura interna de todos os metadados e componentes.
Por isso não se inventa tag XML, propriedade, ID ou atributo porque o nome parece plausível. Metadado não documentado precisa ser confrontado com a configuração real do ambiente.
Uma propriedade inventada produz um XML que abre, salva e não faz nada — o pior tipo de erro, porque não avisa.

Leve para a prática
Antes de tratar uma configuração de tela como fato, identifique se ela está comprovada por documentação, metadado do ambiente ou comportamento observado em Runtime.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
