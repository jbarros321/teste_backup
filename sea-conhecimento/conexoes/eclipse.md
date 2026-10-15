# Conectar o SEA ao Eclipse — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/eclipse  
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

O suporte vem do plugin de IA instalado, não da IDE. Menus e formato variam conforme o plugin.

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Instale um plugin de IA com suporte a MCP
O Eclipse não fala MCP sozinho: quem fala é o assistente de IA que você instala. Pelo Eclipse Marketplace (Help → Eclipse Marketplace), escolha um plugin de assistente que anuncie suporte a Model Context Protocol e instale.

- 2

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 3

### Abra a configuração de MCP do plugin
Em Window → Preferences, procure a seção do plugin instalado e localize a entrada de servidores MCP. Praticamente todos aceitam o JSON padrão do protocolo.

- 4

### Declare o servidor SEA
O formato é o mesmo dos demais clientes — o que muda entre plugins é apenas onde você cola:
Configuração MCP do plugin{
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

### Se o plugin só aceitar processo local
A ponte mcp-remote resolve, e requer Node.js instalado na máquina:
npx -y mcp-remote https://mcp.searchitect.pro/mcp --header "Authorization: Bearer sea_live_xxxxxxxxxxxxxxxxx"

- 6

### Confirme
Reinicie o Eclipse e abra o chat do plugin. Peça a lista de ferramentas disponíveis — as cinco sea_* devem aparecer.
Esperadosea_prepare, sea_search, sea_route, sea_experts, sea_version

Como o suporte vem do plugin e não da IDE, o caminho exato dos menus muda conforme o que você instalou. O que não muda é o endpoint e o cabeçalho: se o seu plugin aceita MCP over HTTP, ele conecta no SEA.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Sustentar um projeto Java corporativo de longa vida
A situaçãoO projeto roda há anos, a equipe mudou, e as decisões antigas não têm mais quem as explique.

O que você pede no EclipseQual é o comportamento esperado do Sankhya neste ponto de extensão e quais contratos eu não posso quebrar ao evoluir esta classe?

O que voltaO Expert devolve o contrato e o que depende dele. Em base legada, saber o que não pode ser tocado vale mais do que saber o que pode.

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

- JetBrains

- Cursor

- Windsurf

Não é esse o sistema que você usa?
Ver todas as conexões
