# Conectar o SEA ao Aplicações próprias — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/aplicacoes-proprias  
> Coletado em: 2026-10-14

---

## Compatibilidade
Validado
Conexão com o SEA confirmada em uso real.

Autenticação
API Key
Revisado em
out/2026

- MCP

- Programático

- API Key

Qualquer aplicação que fale MCP sobre Streamable HTTP com Bearer token. A chamada deve sair do seu servidor.
Documentação oficial consultada: Model Context Protocol (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### TypeScript — SDK oficial
Instale com npm install @modelcontextprotocol/sdk e conecte:
TypeScriptimport { Client } from "@modelcontextprotocol/sdk/client/index.js"
import { StreamableHTTPClientTransport } from "@modelcontextprotocol/sdk/client/streamableHttp.js"

const transport = new StreamableHTTPClientTransport(new URL("https://mcp.searchitect.pro/mcp"), {
requestInit: {
headers: { Authorization: `Bearer ${process.env.SEA_API_KEY}` },
},
})

const client = new Client({ name: "minha-aplicacao", version: "1.0.0" })
await client.connect(transport)

const resultado = await client.callTool({
name: "sea_search",
arguments: { query: "rateio de centro de custo", expert: "auto", limit: 3 },
})

- 3

### Python — SDK oficial
Instale com pip install mcp e use o cliente Streamable HTTP:
Pythonimport os
from mcp import ClientSession
from mcp.client.streamable_http import streamablehttp_client

headers = {"Authorization": f"Bearer {os.environ['SEA_API_KEY']}"}

async with streamablehttp_client("https://mcp.searchitect.pro/mcp", headers=headers) as (ler, escrever, _):
async with ClientSession(ler, escrever) as sessao:
await sessao.initialize()
resultado = await sessao.call_tool(
"sea_search",
{"query": "rateio de centro de custo", "expert": "auto", "limit": 3},
)

- 4

### HTTP puro — para conferir a credencial
Útil para testar a chave antes de escrever qualquer código. O cabeçalho Accept com os dois tipos é exigido pelo transporte Streamable HTTP:
curl -X POST https://mcp.searchitect.pro/mcp \
-H "Authorization: Bearer sea_live_xxxxxxxxxxxxxxxxx" \
-H "Content-Type: application/json" \
-H "Accept: application/json, text/event-stream" \
-d '{"jsonrpc":"2.0","id":1,"method":"tools/list"}'

Esperadosea_prepare, sea_search, sea_route, sea_experts, sea_version

- 5

### Os parâmetros das ferramentas
É o contrato completo — nenhuma outra ferramenta é exposta.
Assinaturassea_search(query: str, expert: str = "auto", limit: int = 3)
sea_route(query: str)
sea_experts()
sea_version()

Uma chave por aplicação, não uma chave para todas: assim dá para revogar uma integração sem derrubar as outras. Toda chamada sai do seu servidor — chave em cliente é chave vazada.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Usar o SEA como serviço dentro do seu backend
A situaçãoNão há agente nem chat no meio: é um sistema interno que precisa de uma resposta especializada em uma etapa do processo.

O que você pede no Aplicações própriasChame sea_search com a descrição do problema do usuário e use a evidência retornada como contexto da etapa seguinte do fluxo.

O que voltaO SEA entra como dependência de serviço, com contrato estável e resposta rastreável. Sem acoplar seu sistema a um cliente de chat específico.

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

- Microsoft Foundry

- n8n

- Vercel AI SDK

Não é esse o sistema que você usa?
Ver todas as conexões
