# Conectar o SEA ao Microsoft Foundry — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/foundry  
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

Depende da assinatura do Azure e da disponibilidade da ferramenta MCP no Agent Service da sua região.

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Guarde a chave no Key Vault
Grave a SEA API Key como segredo do Azure Key Vault e dê acesso à identidade gerenciada do projeto. A chave não deve aparecer em código nem em variáveis de ambiente compartilhadas.

- 3

### Pelo portal: adicione a ferramenta
No projeto do Foundry, abra o agente e vá em Tools → Add → MCP server. Informe o rótulo, a URL do servidor e o cabeçalho de autorização.
URL do servidor e cabeçalhohttps://mcp.searchitect.pro/mcp
Authorization: Bearer sea_live_xxxxxxxxxxxxxxxxx

- 4

### Ou declare pelo SDK
Em Python, a ferramenta MCP é declarada com McpTool e os cabeçalhos entram por update_headers:
Pythonimport os
from azure.ai.agents.models import McpTool

sea = McpTool(
server_label="sea",
server_url="https://mcp.searchitect.pro/mcp",
allowed_tools=["sea_search", "sea_route", "sea_experts", "sea_version"],
)
sea.update_headers("Authorization", f"Bearer {os.environ['SEA_API_KEY']}")

agent = project.agents.create_agent(
model="gpt-4o",
name="agente-sankhya",
instructions=(
"Consulte o SEA antes de responder qualquer coisa sobre Sankhya. "
"Use expert='auto' e deixe o Core rotear."
),
tools=sea.definitions,
)

- 5

### Decida a política de aprovação
Por padrão o Agent Service pede aprovação a cada chamada de ferramenta MCP. Em automações sem humano no circuito, ajuste esse comportamento conscientemente — é uma decisão de governança, não de conveniência.
sea.set_approval_mode("never")

- 6

### Confirme
Rode uma thread de teste com uma pergunta de Sankhya e verifique no rastro da execução que as ferramentas sea_* foram chamadas.

Passe allowed_tools com a lista explícita: além de reduzir a superfície exposta ao agente, deixa registrado no código o que aquele agente pode consultar.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Colocar o conhecimento do ERP dentro de um agente próprio
A situaçãoA empresa está construindo o próprio agente corporativo e precisa que ele seja confiável no domínio do ERP.

O que você pede no Microsoft FoundryRegistre o SEA como ferramenta do agente e oriente-o a consultar antes de responder qualquer demanda de Sankhya.

O que voltaO agente deixa de depender do que o modelo base sabe sobre o ERP e passa a consultar a fonte especializada a cada pergunta — com rastro de qual Expert respondeu.

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

- Microsoft Copilot Studio

- n8n

- Vercel AI SDK

- Aplicações próprias

Não é esse o sistema que você usa?
Ver todas as conexões
