# Conectar o SEA ao Vercel AI SDK — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/vercel-ai-sdk  
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

- Programático

- API Key

Integração programática no seu código, não um chat. O nome das funções de cliente MCP muda entre versões do SDK.
Documentação oficial consultada: AI SDK — Ferramentas MCP (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Instale as dependências
O cliente MCP do AI SDK usa o transporte oficial do protocolo:
npm install ai @ai-sdk/anthropic @modelcontextprotocol/sdk

- 3

### Guarde a chave no ambiente
Em .env.local (e nas variáveis de ambiente do projeto na Vercel). Sem prefixo NEXT_PUBLIC_ — a chave nunca pode ir para o bundle do navegador.
.env.localSEA_API_KEY=sea_live_xxxxxxxxxxxxxxxxx

- 4

### Conecte e carregue as ferramentas
O cliente devolve as ferramentas já no formato que generateText e streamText esperam:
app/api/chat/route.tsimport { anthropic } from "@ai-sdk/anthropic"
import { experimental_createMCPClient as createMCPClient, generateText, stepCountIs } from "ai"
import { StreamableHTTPClientTransport } from "@modelcontextprotocol/sdk/client/streamableHttp.js"

const sea = await createMCPClient({
transport: new StreamableHTTPClientTransport(new URL("https://mcp.searchitect.pro/mcp"), {
requestInit: {
headers: { Authorization: `Bearer ${process.env.SEA_API_KEY}` },
},
}),
})

try {
const { text } = await generateText({
model: anthropic("claude-opus-5"),
tools: await sea.tools(),
stopWhen: stepCountIs(5),
system: "Consulte o SEA antes de responder sobre Sankhya. Use expert=\"auto\".",
prompt: "Como é feito o rateio de centro de custo no Sankhya?",
})
console.log(text)
} finally {
await sea.close()
}

- 5

### Confirme
Antes de plugar no agente, imprima a lista de ferramentas — é o teste mais rápido de que a chave e o endpoint estão certos.
console.log(Object.keys(await sea.tools()))

Esperado[ 'sea_search', 'sea_route', 'sea_experts', 'sea_version' ]

Feche o cliente ao fim da requisição (sea.close()) para não vazar conexões em ambiente serverless. Em versões 4.x do AI SDK, troque stopWhen: stepCountIs(5) por maxSteps: 5.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Embutir o especialista no produto que você vende
A situaçãoSua aplicação já tem um assistente, e ele é genérico justamente no assunto em que precisaria ser bom.

O que você pede no Vercel AI SDKConecte as ferramentas do SEA ao assistente da aplicação e deixe o modelo decidir quando consultá-las.

O que voltaO assistente do seu produto passa a responder sobre Sankhya com a mesma evidência dos Experts, sem você manter base de conhecimento própria sobre o ERP.

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

- Aplicações próprias

Não é esse o sistema que você usa?
Ver todas as conexões
