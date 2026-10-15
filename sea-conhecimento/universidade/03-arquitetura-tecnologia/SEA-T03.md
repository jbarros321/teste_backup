# Curso Metadados e Dicionário de Dados no Sankhya ERP (SEA-T03)

> Fonte: https://www.searchitect.pro/universidade/SEA-T03  
> Coletado em: 2026-10-14

---

## SEA-T03 — Metadados e Dicionário de Dados no Sankhya ERP
Curso gratuito para quem trabalha com Sankhya ERP: 4 módulos, 16 aulas e 5 horas, com certificado SEA Practitioner — Dicionário de Dados na trilha Plataforma Técnica e Dados. Escrito pelo especialista do domínio, com casos trabalhados a partir de situações reais do ERP. Aprofunda o trabalho do SEA - Dicionário de Dados.

### Ementa

- Módulo 1 · Território do Dicionário de Dados

- Cobertura: TDDTAB, TDDCAM, TDDLIG e DBExplorer

- Cardinalidade e relacionamento: o que o metadado prova

- Fronteira com SQL/BI na consulta e com SDK no acesso

- Limites: não deduz regra de negócio a partir do modelo

- Módulo 2 · Perguntando com evidência de modelo

- Anexando a estrutura da tabela e as ligações declaradas

- Informando a versão do dicionário e o ambiente

- Descrevendo o que se quer descobrir, não a tabela suposta

- Por que SQL que retorna dado não confirma comportamento

- Módulo 3 · Casos de uso do dia a dia

- Descoberta de tabelas e validação de relacionamentos

- Engenharia reversa de estrutura desconhecida

- Construção de catálogo corporativo de dados

- Avaliação de impacto estrutural antes de uma mudança

- Módulo 4 · Entregas, validação e governança

- Lendo o mapa de metadados entregue

- Aplicando governança e catálogo de dados

- Validando o comportamento em runtime

- Estruturando a documentação técnica do modelo

Primeira aula aberta

## Cobertura: TDDTAB, TDDCAM, TDDLIG e DBExplorer

### O que o Dicionário de Dados cobre
Você aciona o expert de Dicionário de Dados quando precisa transformar uma dúvida sobre a estrutura do Sankhya em uma afirmação verificável sobre tabelas, campos, entidades, metadados e ligações. O terreno dele começa no Dicionário de Dados e inclui a correlação com a estrutura física exposta pelo DBExplorer.
Na arquitetura oficial do Dicionário, TDDTAB mantém informações relativas ao mapeamento das tabelas; sua chave natural documentada é NOMETAB. TDDCAM participa do mapeamento dos campos e TDDLIG das ligações. Essas estruturas fazem parte de um conjunto maior de tabelas de metadados consumido pela plataforma. O próprio Sankhya usa esse conjunto para montar telas, manter informações estruturais e alimentar componentes como JAPE e Dynaform.
Isso define o primeiro limite operacional: o expert não trata uma tabela encontrada como uma entidade automaticamente compreendida. Se você perguntar "qual campo devo usar?", ele precisa primeiro estabelecer qual entidade e qual estrutura instalada sustentam essa resposta.

### Onde entra o DBExplorer
O DBExplorer complementa essa leitura porque permite confrontar o metadado com o banco. Ao selecionar uma tabela, a documentação oficial informa que você pode verificar campos, tipos, tamanhos, PK, nulabilidade, valor default e índices. A própria documentação descreve essas informações como complementares ao Dicionário de Dados.
Essa correlação é essencial. Há inclusive troubleshooting oficial para a mensagem que informa metadados de um campo não inicializados, em que a primeira validação é confirmar no DBExplorer se o campo realmente existe na tabela e, depois, verificar a entidade correspondente no Dicionário de Dados.
Quando o expert responder, leia separadamente:

- metadado declarado, que descreve como a plataforma conhece a estrutura

- estrutura física, que mostra o que realmente existe no banco

- inferência, quando as evidências ainda não permitem confirmação

Não use apenas uma dessas camadas como prova das demais.

Caso

### a tabela encontrada e a entidade compreendida
Alguém localiza uma tabela com nome sugestivo e começa a escrever a consulta.
O primeiro limite do domínio é exatamente esse: uma tabela encontrada não é uma entidade automaticamente compreendida.
A arquitetura oficial inclui TDDTAB, TDDCAM e TDDLIG, entre outras tabelas de metadados — TDDTAB representa o mapeamento de tabelas, TDDCAM o de campos e TDDLIG participa do mapeamento das ligações entre instâncias.
Isso significa que existem duas realidades a confrontar: o que o Dicionário declara e o que o DBExplorer mostra fisicamente instalado.
Quando a pergunta for "qual campo devo usar?", a resposta começa estabelecendo qual entidade e qual estrutura instalada sustentam a resposta.
Pular essa etapa é como escolher a palavra pelo som: às vezes funciona, e quando não funciona ninguém entende por quê.

Leve para a prática
Antes de pedir ao expert o significado de uma estrutura, identifique a entidade candidata e confronte o Dicionário de Dados com a estrutura física exibida pelo DBExplorer.

Continuar o curso — conta gratuita Ver todos os cursos de Sankhya
As demais aulas, as avaliações e o certificado ficam disponíveis após o cadastro. Iniciativa independente, sem vínculo oficial com a fornecedora do Sankhya ERP.
