# Conectar o SEA ao Replit — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/replit  
> Coletado em: 2026-10-14

---

## Compatibilidade
Depende do ambiente/plano
O cliente documenta suporte a MCP remoto, mas a disponibilidade varia por plano, versão ou liberação do administrador.

Autenticação
API Key
Revisado em
out/2026

- MCP

- API Key

- Programático

- Conforme plano

O uso pelo Agent depende da conta. Pelo código do Repl, a conexão é programática, com a chave em Secrets.

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Guarde a chave em Secrets
No Repl, abra Tools → Secrets e crie SEA_API_KEY com o valor da chave. Secrets ficam disponíveis como variáveis de ambiente do lado servidor e não aparecem no código.
Nome do secretSEA_API_KEY

- 3

### Conecte pelo Agent, se a sua conta oferecer MCP
Nas integrações do Replit Agent, adicione um servidor MCP personalizado com a URL e o cabeçalho de autorização:
URL e cabeçalhohttps://mcp.searchitect.pro/mcp
Authorization: Bearer sea_live_xxxxxxxxxxxxxxxxx

- 4

### Ou consuma direto do código
Instale o SDK oficial de MCP no Repl e leia a chave do secret — nunca escreva a chave no arquivo:
npm install @modelcontextprotocol/sdk

- 5

### Confirme
Rode o Repl e liste as ferramentas. Se as cinco sea_* voltarem, a conexão está de pé.
Esperadosea_prepare, sea_search, sea_route, sea_experts, sea_version

Repls públicos expõem o código, mas não os Secrets. Ainda assim, mantenha a chamada ao SEA no servidor: qualquer chave que chegue ao navegador é uma chave vazada.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Testar uma hipótese de integração em minutos
A situaçãoAntes de entrar no backlog, alguém precisa confirmar que a integração é possível do jeito que foi imaginada.

O que você pede no ReplitEscreva um script que autentica no Sankhya, consulta os pedidos em aberto e imprime o resultado, para eu validar a viabilidade.

O que voltaO script nasce com a sequência de autenticação e o formato de consulta corretos. A prova de conceito testa a hipótese de negócio, não a sorte do palpite sobre a API.

## Teste se está funcionando
Primeiro um teste explícito, para confirmar que a ferramenta responde:
Use o SEA - Systems Enterprise Architect para analisar CRUDServiceProvider.loadRecords no Sankhya. Consulte o SEA antes de responder.

Depois o teste real, sem mencionar o SEA nem o Expert esperado. A ideia é verificar se o agente decide consultar o SEA automaticamente: ele deve chamar o servidor por conta própria, receber o roteamento do Core e responder com a evidência do Specialist.
Preciso consultar registros no Sankhya usando CRUDServiceProvider.loadRecords, retornando apenas alguns campos e aplicando critérios. Como devo estruturar essa chamada?

## Deu 401 Unauthorized?
Confira, nesta ordem:

- A chave foi copiada inteira, sem espaços nas pontas.

- A chave não foi revogada nem rotacionada depois de configurada.

- Sua conta Searchitect está ativa.

- O acesso MCP está habilitado para a conta.

- A chave não expirou.

- O cliente realmente está enviando o header Authorization: Bearer.

- Em conexões por OAuth: o aplicativo não foi desconectado em Minha Conta → SEA API Keys → Aplicativos conectados. Se foi, refaça a conexão pelo próprio cliente.

Chaves revogadas e contas bloqueadas param de funcionar imediatamente. Se a chave se perdeu, rotacione-a em SEA API Keys — a antiga é invalidada na hora.

## Conectar também

- Lovable

Não é esse o sistema que você usa?
Ver todas as conexões
