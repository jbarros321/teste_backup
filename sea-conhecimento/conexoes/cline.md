# Conectar o SEA ao Cline — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/cline  
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

- Via IDE

- API Key

Extensão de terceiros para VS Code. O formato do arquivo de configuração pode variar entre versões.
Documentação oficial consultada: Cline — Adicionar servidores MCP (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Abra a configuração de MCP
No painel do Cline, clique no ícone de MCP Servers (barra superior) → Configure MCP Servers. O botão abre o cline_mcp_settings.json no editor, sem você precisar caçar o caminho do arquivo.

- 3

### Declare o servidor SEA
O Cline chama o transporte Streamable HTTP de streamableHttp. autoApprove vazio significa que o Cline pede sua confirmação antes de cada chamada — deixe assim até confiar no fluxo.
cline_mcp_settings.json{
"mcpServers": {
"SEA-Systems-Enterprise-Architect": {
"type": "streamableHttp",
"url": "https://mcp.searchitect.pro/mcp",
"headers": {
"Authorization": "Bearer sea_live_xxxxxxxxxxxxxxxxx"
},
"disabled": false,
"autoApprove": []
}
}
}

- 4

### Salve o arquivo
O Cline recarrega os servidores sozinho ao salvar. Se não recarregar, use o botão de restart no cartão do servidor.

- 5

### Confirme
No painel de MCP Servers, o cartão do SEA fica com o indicador verde e lista as ferramentas.
Esperadosea_prepare, sea_search, sea_route, sea_experts, sea_version

O cline_mcp_settings.json fica no armazenamento global da extensão, fora do repositório — é o lugar certo para a chave.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Fazer uma alteração de ponta a ponta sem sair do editor
A situaçãoA mudança toca código, consulta e parametrização, e cada parte está documentada em um lugar diferente.

O que você pede no ClinePreciso adicionar um campo customizado que seja preenchido na inclusão e consultado no relatório. Me mostre o caminho completo no Sankhya e implemente a parte de código.

O que voltaO Cline percorre o roteiro completo com a evidência do Expert e implementa o que é código, deixando explícito o que precisa ser feito na tela do ERP. O que fica de fora fica dito, não esquecido.

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
