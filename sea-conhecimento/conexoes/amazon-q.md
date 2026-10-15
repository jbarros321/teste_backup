# Conectar o SEA ao Amazon Q Developer — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/amazon-q  
> Coletado em: 2026-10-14

---

## Compatibilidade
Compatibilidade a validar
Roteiro baseado na documentação do cliente. Ainda não confirmamos a conexão do nosso lado.

Autenticação
API Key
Revisado em
out/2026

- MCP

- CLI

- Via IDE

- API Key

O suporte a servidores MCP remotos chegou nas versões recentes do CLI. Versões antigas exigem a ponte local mcp-remote.
Documentação oficial consultada: AWS — Amazon Q Developer e MCP remoto (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Escolha o alcance da configuração
O Amazon Q lê dois arquivos: um global, que vale em qualquer pasta, e um por workspace. Prefira o global para a chave não encostar no repositório.
Onde fica o arquivoGlobal: ~/.aws/amazonq/mcp.json
Workspace: .amazonq/mcp.json

- 3

### Declare o servidor SEA
Nas versões que já suportam transporte HTTP nativo, o servidor remoto é declarado direto pela URL:
~/.aws/amazonq/mcp.json{
"mcpServers": {
"SEA-Systems-Enterprise-Architect": {
"type": "http",
"url": "https://mcp.searchitect.pro/mcp",
"headers": {
"Authorization": "Bearer sea_live_xxxxxxxxxxxxxxxxx"
}
}
}
}

- 4

### Se a sua versão só aceitar stdio
Use a ponte mcp-remote — do ponto de vista do agente o resultado é idêntico. Requer Node.js instalado.
~/.aws/amazonq/mcp.json (alternativa){
"mcpServers": {
"SEA-Systems-Enterprise-Architect": {
"command": "npx",
"args": [
"-y", "mcp-remote", "https://mcp.searchitect.pro/mcp",
"--header", "Authorization: Bearer sea_live_xxxxxxxxxxxxxxxxx"
]
}
}
}

- 5

### Confirme pelo CLI
q mcp list

EsperadoSEA-Systems-Enterprise-Architect

- 6

### E dentro do chat
Na sessão do q chat, o comando abaixo lista as ferramentas carregadas de cada servidor.
/tools

Esperadosea_prepare, sea_search, sea_route, sea_experts, sea_version

A extensão do Amazon Q para VS Code e JetBrains lê o mesmo mcp.json do CLI — configurar uma vez vale para as duas. Reinicie o q chat depois de editar o arquivo: os servidores são carregados na inicialização.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Avaliar impacto de infraestrutura antes de escalar
A situaçãoO volume de integrações cresceu e a dúvida é se o gargalo está no dimensionamento ou no desenho das chamadas.

O que você pede no Amazon Q DeveloperTemos picos de lentidão nas integrações com o Sankhya em horário de fechamento. Quais limites e comportamentos do ERP devo considerar antes de escalar infraestrutura?

O que voltaO Expert aponta o que é limite do produto e o que é consequência do desenho da chamada. Dimensionar sem essa separação costuma custar caro e resolver pouco.

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

- Claude Code

- Codex

- GitHub Copilot (agente)

- GitHub Copilot CLI

Não é esse o sistema que você usa?
Ver todas as conexões
