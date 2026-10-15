# Conectar o SEA ao Outro cliente — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/outro  
> Coletado em: 2026-10-14

---

## Compatibilidade
Depende do ambiente/plano
O cliente documenta suporte a MCP remoto, mas a disponibilidade varia por plano, versão ou liberação do administrador.

Autenticação
API Key · OAuth
Revisado em
out/2026

- MCP

- API Key

- OAuth

Funciona em clientes compatíveis com MCP sobre HTTP. A forma de cadastrar cabeçalho ou login varia por cliente e versão.
Documentação oficial consultada: Model Context Protocol (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Aponte o cliente para o endpoint
O SEA é um servidor MCP HTTP (streamable). A autenticação é por Bearer token em todas as chamadas.
Endpoint e headerhttps://mcp.searchitect.pro/mcp
Authorization: Bearer sea_live_xxxxxxxxxxxxxxxxx

- 3

### Se o cliente só aceitar stdio
Use a ponte mcp-remote, que expõe o endpoint HTTP como um servidor local:
npx -y mcp-remote https://mcp.searchitect.pro/mcp --header "Authorization: Bearer sea_live_xxxxxxxxxxxxxxxxx"

A forma de cadastrar headers e credenciais varia por cliente e versão. Nunca grave a chave em arquivos versionados, repositórios Git ou scripts compartilhados — prefira o secret store do próprio cliente.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Qualquer cliente que fale MCP, pelo mesmo endpoint
A situaçãoA ferramenta que seu time usa não está na lista — mas suporta Model Context Protocol, nativamente ou por ponte stdio.

O que você pede no Outro clienteConsulte o SEA antes de responder qualquer questão sobre Sankhya ERP e cite a origem da evidência.

O que voltaAs mesmas cinco ferramentas, o mesmo endpoint, a mesma chave. Não existe um SEA diferente por cliente: o que muda é só onde você cola a configuração.

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

Não é esse o sistema que você usa?
Ver todas as conexões
