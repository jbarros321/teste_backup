# Curso Logística Ponta a Ponta no Sankhya ERP (SEA-O06)

> Fonte: https://www.searchitect.pro/universidade/SEA-O06  
> Coletado em: 2026-10-14

---

## SEA-O06 — Logística Ponta a Ponta no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 5 horas, com certificado SEA Practitioner — Logística na trilha Supply Chain e Operações. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP. Aprofunda o trabalho do SEA - Logística Expert.

### Ementa

- Módulo 1 · Território do Logística Expert

- Cobertura: expedição, ordens de carga, frete, MDF-e e CT-e

- Como ele se posiciona entre o WMS e o TMS especializados

- Quando usar o generalista logístico e quando ir ao específico

- Limites: não assume separação concluída sem conferência

- Módulo 2 · Perguntando com evidência logística

- Anexando a ordem de carga e o status da separação

- Descrevendo a jornada operacional real do armazém

- Informando a configuração de formação de carga

- Isolando a etapa exata onde o fluxo travou

- Módulo 3 · Casos de uso do dia a dia

- Problemas de separação e conferência na expedição

- Formação e gestão de ordens de carga

- Análise de performance e runtime logístico

- Dashboards operacionais e SQL analítica

- Módulo 4 · Entregas, validação e governança

- Lendo o fluxo operacional documentado e a arquitetura

- Aplicando o checklist operacional e de Go-Live

- Montando a estratégia de integração e BI

- Estabelecendo o plano de observabilidade

Primeira aula aberta

## Cobertura: expedição, ordens de carga, frete, MDF-e e CT-e

### Onde o Logística Expert entra na operação
Você usa o Logística Expert quando a pergunta atravessa a logística e precisa conectar documentos, carga, expedição, transporte e evidência operacional. O território dele inclui Formação de Carga, Ordem de Carga, expedição, frete, transporte e os pontos de contato logísticos com CT-e e MDF-e. A cadeia que o expert interpreta não transforma uma etapa na prova automática da seguinte: pedido ou nota pode estar associado a uma Ordem de Carga sem que isso prove separação, conferência, expedição ou entrega.
Na Formação de Carga, por exemplo, a documentação oficial define a Ordem de Carga como agrupamento de notas em uma carga e expõe informações como peso, metros cúbicos, quantidade de volumes, quantidade de notas e valor de frete. Parâmetros reais também entram na análise. CALCPESOCUMUOC recalcula informações do resumo conforme pedidos e notas são vinculados à OC; FRETEPORCID participa da disponibilização da geração de fretes por cidade.

### O fiscal é uma fronteira, não um atalho
CT-e e MDF-e entram no território do expert quando fazem parte da jornada logística. A documentação Sankhya relaciona CT-e modelo 57, Ordem de Carga, Formação de Carga e Viagens de Transporte, com o MDF-e. Em um fluxo documentado, o CT-e pode ser vinculado à Ordem de Carga e informações de unidade de transporte e carga podem alimentar o XML do MDF-e.
Isso não autoriza o expert a declarar um documento fiscal autorizado porque existe XML ou porque uma tela apresenta determinado status. Para MDF-e, a própria definição fiscal exige autorização de uso pelo Ambiente Autorizador. Quando a questão deixa de ser jornada logística e passa a ser legislação, tributação, rejeição ou validade fiscal, a análise precisa da evidência fiscal correspondente.

### O que você recebe do expert
Você recebe uma leitura ponta a ponta: onde o documento está, qual etapa deveria ocorrer, quais dependências existem e qual evidência falta. Não recebe uma conclusão fabricada para preencher lacunas. Runtime confirma execução; documento e tela, isoladamente, não provam toda a jornada.

Caso

### a carga que passou por seis estados
Pedido → formação de carga → Ordem de Carga → WMS → separação → conferência → expedição → transporte.
Cada seta é uma transição que precisa de evidência própria. E nenhuma delas prova a seguinte.
O erro clássico: "o pedido está na OC, então a separação vai acontecer". A OC organiza a logística; ela não executa nada.
CT-e e MDF-e entram nessa cadeia quando fazem parte da jornada — a documentação relaciona CT-e modelo 57, Ordem de Carga, Formação de Carga e Viagens de Transporte com o MDF-e, e informações de unidade de transporte podem alimentar o XML do MDF-e.
Mas existir XML não é documento autorizado: para MDF-e, a definição fiscal exige autorização de uso pelo Ambiente Autorizador.
Localizar a dúvida numa etapa específica dessa cadeia é o que impede a investigação de começar perguntando "por que a carga não saiu?" — pergunta que tem seis respostas possíveis.

Leve para a prática
Localize sua dúvida na cadeia que vai de pedido ou nota até formação de carga, Ordem de Carga, expedição, transporte e fiscal, e acione o expert para conectar as etapas sem tratar uma delas como prova automática das demais.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
