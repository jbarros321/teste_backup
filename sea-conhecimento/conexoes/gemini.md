# Conectar o SEA ao Gemini — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/gemini  
> Coletado em: 2026-10-14

---

## Compatibilidade
Validado com restrições
Conexão confirmada, com uma limitação conhecida descrita abaixo.

Autenticação
API Key · OAuth
Revisado em
out/2026

- MCP

- CLI

- API Key

- OAuth

- App publicado

O caminho confirmado é o Gemini CLI. No app do Gemini, o suporte a apps MCP personalizados varia por tipo de conta e região; o Expert publicado como Gem é um caminho separado.
Documentação oficial consultada: Gemini CLI — Servidores MCP (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gemini CLI: adicione o servidor
O caminho confirmado é o Gemini CLI. Gere a chave (próximo passo) e cadastre o servidor por comando, trocando SUA_CHAVE_SEA pela chave gerada:
gemini mcp add --transport http --header "Authorization: Bearer SUA_CHAVE_SEA" SEA-Systems-Enterprise-Architect https://mcp.searchitect.pro/mcp

- 2

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 3

### Ou escreva a configuração direto
O arquivo do usuário fica em ~/.gemini/settings.json. Para Streamable HTTP, o campo é httpUrl — url é reservado a servidores SSE:
~/.gemini/settings.json{
"mcpServers": {
"SEA-Systems-Enterprise-Architect": {
"httpUrl": "https://mcp.searchitect.pro/mcp",
"headers": {
"Authorization": "Bearer SUA_CHAVE_SEA"
}
}
}
}

- 4

### Sem chave: login OAuth
Se você cadastrar o servidor sem o cabeçalho, o CLI detecta que o SEA pede login e inicia o fluxo OAuth no navegador. Dentro da sessão, autentique com:
/mcp auth SEA-Systems-Enterprise-Architect

EsperadoO OAuth precisa de um navegador local; em ambiente sem interface, use a API Key.

- 5

### Confirme
Dentro da sessão do CLI, liste os servidores e as ferramentas carregadas:
/mcp

EsperadoSEA-Systems-Enterprise-Architect — Connected
sea_prepare, sea_search, sea_route, sea_experts, sea_version

- 6

### E no app do Gemini?
O suporte a apps MCP personalizados no app do Gemini depende do tipo de conta e da região. O Expert Fiscal também está publicado como Gem, um caminho separado do MCP, aberto pela página do Expert.

Nunca grave a chave real em arquivos versionados. Prefira o settings.json do usuário, fora do repositório.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Traduzir uma demanda de negócio em requisito técnico
A situaçãoA área pede 'um relatório de margem por vendedor'. Ninguém sabe ainda se isso é um BI, uma consulta SQL ou um campo calculado.

O que você pede no GeminiA área comercial pediu margem por vendedor no Sankhya. Quais tabelas e campos sustentam esse cálculo e quais caminhos existem para entregar isso?

O que voltaO Expert devolve a origem do dado e compara os caminhos possíveis com o custo de cada um. A conversa com a área passa a ser sobre escolha, não sobre viabilidade.

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

- ChatGPT

- Claude (web)

- Claude Desktop

Não é esse o sistema que você usa?
Ver todas as conexões
