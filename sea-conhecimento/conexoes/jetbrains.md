# Conectar o SEA ao JetBrains — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/jetbrains  
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

- Via IDE

- API Key

- Conforme plano

Depende da versão da IDE e do plugin de IA (AI Assistant ou Junie) com licença ativa. Versões antigas só aceitam a ponte local.
Documentação oficial consultada: JetBrains — AI Assistant e MCP (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Confirme a versão e o AI Assistant
O suporte a MCP chegou nas IDEs da linha 2025.1 com o plugin AI Assistant (ou o Junie) instalado e ativo. Em Help → About, confira a versão da IDE.

- 2

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 3

### Abra a configuração de MCP
Settings/Preferences → Tools → AI Assistant → Model Context Protocol (MCP). Use o botão de adicionar e escolha a entrada por JSON.

- 4

### Declare o servidor SEA
O formato é o mesmo mcpServers dos demais clientes:
Configuração MCP do AI Assistant{
"mcpServers": {
"SEA-Systems-Enterprise-Architect": {
"url": "https://mcp.searchitect.pro/mcp",
"headers": {
"Authorization": "Bearer sea_live_xxxxxxxxxxxxxxxxx"
}
}
}
}

- 5

### Se a sua versão só aceitar comando local
Versões mais antigas do plugin só falam stdio. A ponte resolve, e requer Node.js:
Alternativa por ponte{
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

- 6

### Aplique e confirme
Clique em Apply. O servidor aparece na lista como conectado; no chat do AI Assistant, o seletor de ferramentas passa a listar as sea_*.
Esperadosea_prepare, sea_search, sea_route, sea_experts, sea_version

A configuração fica no nível da IDE, não do projeto — vale para todos os repositórios que você abrir naquela IDE. Cada produto JetBrains guarda a sua, então repita nas IDEs que você usa.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Validar uma consulta pesada antes de subir
A situaçãoA consulta funciona em homologação com base pequena. Em produção, a tabela tem milhões de linhas.

O que você pede no JetBrainsAvalie esta consulta sobre as tabelas de movimentação do Sankhya: índices envolvidos, risco de lock e alternativas.

O que voltaO Expert traz o que se sabe da modelagem do produto e o que é inferência de desempenho, marcando a diferença. Você decide com o risco explícito, em vez de descobrir no fechamento do mês.

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

- VS Code

- Cursor

- Windsurf

- Eclipse

Não é esse o sistema que você usa?
Ver todas as conexões
