# Conectar o SEA ao Antigravity — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/antigravity  
> Coletado em: 2026-10-14

---

## Compatibilidade
Validado com restrições
Conexão confirmada, com uma limitação conhecida descrita abaixo.

Autenticação
API Key
Revisado em
out/2026

- MCP

- Via IDE

- API Key

Conecte pela API Key no cabeçalho. O login OAuth automático do editor pode entrar em laço sem concluir.

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Abra o painel de MCP Servers
No Antigravity, vá em Settings → MCP Servers e escolha a opção de editar a configuração bruta (raw config / JSON).

- 3

### Declare o servidor SEA
Para clientes que aceitam servidores remotos direto pela URL:
mcp_config.json{
"mcpServers": {
"SEA-Systems-Enterprise-Architect": {
"serverUrl": "https://mcp.searchitect.pro/mcp",
"headers": {
"Authorization": "Bearer sea_live_xxxxxxxxxxxxxxxxx"
}
}
}
}

- 4

### Se o editor não aceitar URL remota
Use a mesma ponte dos demais clientes — o resultado é idêntico do ponto de vista do agente:
mcp_config.json (alternativa){
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

### Se aparecer uma tela de login do SEA, ignore
O Antigravity pode tentar se conectar sozinho por login (OAuth), abrir esse fluxo e repeti-lo em segundo plano sem concluir. Não é preciso: com a SEA API Key no cabeçalho, como acima, a conexão funciona sem login. Se o laço continuar, remova o servidor, salve a configuração com a chave e recarregue.

- 6

### Recarregue os servidores
Use o botão de refresh do painel de MCP Servers. O SEA deve aparecer conectado, com as ferramentas sea_* listadas.

Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Planejar a tarefa antes de escrever a primeira linha
A situaçãoA demanda chegou como uma frase. O risco não é implementar errado — é implementar a coisa errada.

O que você pede no AntigravityAntes de codificar: quais decisões de arquitetura esta demanda de Sankhya exige, e quais delas já têm resposta padrão no produto?

O que voltaO agente separa o que o ERP já resolve do que exige desenvolvimento. A maior economia do SEA costuma estar aqui: no código que não precisou existir.

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
