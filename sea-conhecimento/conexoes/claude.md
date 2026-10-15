# Conectar o SEA ao Claude Desktop — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/claude  
> Coletado em: 2026-10-14

---

## Compatibilidade
Validado
Conexão com o SEA confirmada em uso real.

Autenticação
OAuth · API Key
Revisado em
out/2026

- MCP

- OAuth

- API Key

O Claude Desktop usa os mesmos conectores da conta (OAuth). A configuração por arquivo com API Key segue disponível para quem prefere.
Documentação oficial consultada: Anthropic — Conectores personalizados (MCP remoto) (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Caminho recomendado: conector da conta
No Claude Desktop, os conectores personalizados são os mesmos da sua conta Claude. Siga o roteiro do Claude (web) — por Configurações → Conectores, no app ou no navegador — e o SEA aparece nos dois.

- 2

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 3

### Alternativa: configuração por arquivo
Se preferir API Key, abra Settings → Developer → Edit Config. O arquivo abre no seu editor padrão.
Onde fica o arquivoWindows: %APPDATA%\Claude\claude_desktop_config.json
macOS: ~/Library/Application Support/Claude/claude_desktop_config.json

- 4

### Declare o servidor SEA
A configuração por arquivo fala stdio, então a ponte mcp-remote traduz para o endpoint HTTP do SEA. Requer Node.js instalado.
claude_desktop_config.json{
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

### Reinicie o Claude Desktop
Feche completamente o aplicativo e abra de novo. O SEA deve aparecer na lista de ferramentas MCP disponíveis na conversa.

Este arquivo guarda a chave em texto puro na sua máquina. Não o copie para repositórios nem o compartilhe.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Consultar o padrão sem sair do documento que você está escrevendo
A situaçãoVocê está redigindo o desenho técnico de uma integração e precisa confirmar nomes de serviço, payloads e limites sem trocar de janela a cada parágrafo.

O que você pede no Claude DesktopQual o formato correto de chamada do serviço de inclusão de registros do Sankhya, e quais campos são obrigatórios no cabeçalho?

O que voltaA resposta vem com o contrato do serviço e a distinção entre o que é obrigatório pela API e o que é obrigatório pela regra do módulo — a diferença que costuma aparecer só no primeiro erro em produção.

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

- Gemini

Não é esse o sistema que você usa?
Ver todas as conexões
