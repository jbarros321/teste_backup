# Conectar o SEA ao GitHub Copilot CLI — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/copilot-cli  
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

- API Key

- Conforme plano

Requer assinatura do GitHub Copilot. A sintaxe dos comandos pode mudar entre versões do CLI.
Documentação oficial consultada: GitHub — Copilot CLI (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Instale o Copilot CLI
Requer Node.js 22 ou superior. Pelo npm, em qualquer sistema:
npm install -g @github/copilot

EsperadoNo Windows também há winget install GitHub.Copilot; no macOS e Linux, brew install --cask copilot-cli.

- 2

### Abra e faça login
Rode copilot no terminal. Na primeira execução, use /login para entrar com sua conta GitHub.
copilot

- 3

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 4

### Adicione o SEA
Direto do terminal, sem entrar na sessão interativa, trocando SUA_CHAVE_SEA pela chave gerada:
copilot mcp add --transport http --header "Authorization: Bearer SUA_CHAVE_SEA" SEA-Systems-Enterprise-Architect https://mcp.searchitect.pro/mcp

EsperadoO servidor é gravado na configuração do seu usuário, em ~/.copilot/mcp-config.json, e passa a valer em qualquer pasta.

- 5

### Ou escreva a configuração direto
Mesmo resultado, útil para revisar ou versionar por projeto (.mcp.json ou .github/mcp.json na raiz do repositório):
~/.copilot/mcp-config.json{
"mcpServers": {
"SEA-Systems-Enterprise-Architect": {
"type": "http",
"url": "https://mcp.searchitect.pro/mcp",
"headers": {
"Authorization": "Bearer sea_live_xxxxxxxxxxxxxxxxx"
},
"tools": ["*"]
}
}
}

- 6

### Confirme
Pelo terminal, listando todas as origens de configuração:
copilot mcp list

EsperadoSEA-Systems-Enterprise-Architect http https://mcp.searchitect.pro/mcp

- 7

### Ou dentro da sessão
Na sessão interativa, /mcp show lista os servidores e o estado de cada um; /mcp add abre o formulário para cadastrar outro sem sair do terminal.
/mcp show

O mcp-config.json do usuário fica fora do repositório — é o lugar certo para a chave. Se optar pelo .mcp.json do projeto, ele é versionado junto com o código: nunca deixe o Bearer em texto puro ali.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Diagnosticar no terminal, onde o erro aconteceu
A situaçãoUm job de carga falhou na madrugada e o que você tem é a stack trace no log do servidor.

O que você pede no GitHub Copilot CLIRecebi esta stack trace do Sankhya em um processo de importação. O que ela indica e qual é o caminho de correção?

O que voltaO Expert lê o rastro, separa a causa do sintoma e classifica o diagnóstico por confiança — comportamento documentado, evidência de runtime ou hipótese a confirmar. Você sabe o que já pode agir e o que ainda precisa reproduzir.

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

- GitLab Duo

Não é esse o sistema que você usa?
Ver todas as conexões
