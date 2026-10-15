# Curso Desenvolvimento SDK e Runtime Java no Sankhya ERP (SEA-T02)

> Fonte: https://www.searchitect.pro/universidade/SEA-T02  
> Coletado em: 2026-10-14

---

## SEA-T02 — Desenvolvimento SDK e Runtime Java no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 6 horas, com certificado SEA Practitioner — SDK na trilha Plataforma Técnica e Dados. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP. Aprofunda o trabalho do SEA - SDK Expert.

### Ementa

- Módulo 1 · Território do SDK Expert

- Cobertura: add-ons, listeners, services e action buttons

- JAPE, EntityFacade, DynamicVO, NativeSql e JdbcWrapper

- Fronteira com Screen Builder na tela e com Dados no modelo

- Limites: não inventa classe, evento ou listener inexistente

- Módulo 2 · Perguntando com evidência de código

- Anexando o trecho de código e a stack trace completa

- Informando versão do SDK e do runtime

- Descrevendo o ciclo de vida esperado do evento

- Separando erro de compilação de erro de execução

- Módulo 3 · Casos de uso do dia a dia

- Desenvolvimento de add-ons e customizações Java

- Implementação de listeners e action buttons

- Diagnóstico de deadlock, rollback e problema de runtime

- Otimização de performance e validação de deploy

- Módulo 4 · Entregas, validação e governança

- Revisando a arquitetura proposta contra Clean e DDD

- Aplicando observabilidade e auditoria no add-on

- Validando commit e transação com evidência

- Homologando o deploy antes da produção

Primeira aula aberta

## Cobertura: add-ons, listeners, services e action buttons
Você procura o SEA · SDK Expert quando o problema está no código Java que estende o Sankhya Om ou no comportamento técnico dessa extensão. O território inclui add-ons, listeners de persistência, services e Action Buttons. O Add-on Studio organiza esses componentes dentro do projeto Java e usa Gradle como base do desenvolvimento de add-ons.

### Quando o SDK Expert é o dono da análise
Traga para este expert situações como:

- um add-on precisa implementar uma regra Java

- um @Listener deve reagir a uma operação de persistência

- um @Service precisa receber uma requisição e delegar um caso de uso

- um @ActionButton deve executar uma rotina manual a partir de uma tela

- uma extensão compila, mas você precisa entender seu comportamento técnico

O @Listener documentado no Add-on Studio trabalha na camada de persistência. A documentação expõe métodos como beforeInsert(PersistenceEvent event), afterInsert, beforeUpdate, afterUpdate, beforeDelete e afterDelete. Pelo PersistenceEvent, o código pode acessar o DynamicVO com event.getVo() e o JdbcWrapper da transação corrente com event.getJdbcWrapper().
Já um Action Button representa ação iniciada pelo usuário, não um substituto genérico para evento automático. Na API moderna documentada, uma classe implementa AcaoRotinaJava; sua lógica entra em doAction(ContextoAcao contexto). O contexto fornece registros selecionados, parâmetros do formulário e mensagem de retorno.

### O que esperar da resposta
Quando você trouxer um desses componentes, o SDK Expert não deve olhar apenas para a classe isolada. Ele separa gatilho, código, persistência e resultado observado. Um listener existente não prova que disparou; um botão visível não prova que sua rotina terminou; uma chamada a service não prova o efeito de negócio.
Você também verá atenção à versão. Por exemplo, @ActionButton está documentado a partir do Add-on Studio 2.0, enquanto o changelog 2.3.0 registra a depreciação de accessControlled, inclusive com impacto de compilação. Portanto, uma resposta correta para uma versão pode estar errada para outra.
Esse comportamento segue a operação do SDK Expert: capacidade documentada e código são evidências diferentes de execução observada.

Caso

### quatro pontos de entrada, quatro contratos
O mesmo código Java pode ser acionado de formas diferentes, e cada uma entrega um contexto distinto:

- listener — disparado por uma operação de persistência, com o ciclo da entidade em volta

- action button — disparado por ação explícita do usuário, com registros selecionados

- service — chamado por outra camada, com o contrato que o chamador definiu

- add-on — empacota o conjunto e define como ele é instalado

Escolher o ponto de entrada errado produz um código correto que roda na hora errada — ou que não roda.
A pergunta que orienta é sempre a mesma: quem dispara a regra? Uma validação que precisa impedir a gravação não pode viver num botão que o usuário talvez não clique.
E o contrato de cada ponto de entrada muda por versão. Um mecanismo disponível numa combinação de Add-on Studio e Sankhya Om pode não existir em outra.

Leve para a prática
Classifique sua demanda como add-on, listener, service ou Action Button e leve ao SDK Expert o componente real e a versão em que ele precisa funcionar.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
