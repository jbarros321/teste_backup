# Conectar o SEA ao Lovable — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/lovable  
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

- Conforme plano

A integração com servidores MCP depende do plano e do que o projeto expõe. A chave nunca deve ir para o front-end publicado.

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Abra as integrações do projeto
No projeto Lovable, vá em Settings → Integrations e procure a opção de adicionar um servidor MCP personalizado.

- 3

### Cadastre o endpoint e o cabeçalho
Informe a URL do servidor e o header de autorização. É o mesmo par usado em qualquer outro cliente:
URL e cabeçalhohttps://mcp.searchitect.pro/mcp
Authorization: Bearer sea_live_xxxxxxxxxxxxxxxxx

- 4

### Confirme no chat do projeto
Peça algo que dependa de conhecimento Sankhya. O agente deve consultar o SEA antes de gerar o código, em vez de inventar nomes de tabela e regras.

- 5

### Se preferir consumir pelo código gerado
Como o Lovable gera uma aplicação real, dá para chamar o SEA de dentro dela. Faça isso sempre no lado servidor — uma edge function do Supabase, por exemplo — lendo a chave de um secret.
const r = await fetch("https://mcp.searchitect.pro/mcp", {
method: "POST",
headers: {
"Authorization": `Bearer ${Deno.env.get("SEA_API_KEY")}`,
"Content-Type": "application/json",
"Accept": "application/json, text/event-stream",
},
body: JSON.stringify({ jsonrpc: "2.0", id: 1, method: "tools/list" }),
})

Nunca deixe a SEA API Key no código de front-end que o Lovable publica — qualquer visitante consegue lê-la. Use os secrets do projeto e mantenha a chamada no servidor.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Prototipar uma aplicação que fala com o ERP de verdade
A situaçãoO protótipo precisa demonstrar o fluxo real para a área de negócio, não uma tela com dados inventados.

O que você pede no LovableMonte um painel de acompanhamento de pedidos que consome os dados do Sankhya. Use os serviços e campos corretos do ERP.

O que voltaO Lovable gera a aplicação já com o contrato de integração certo. O protótipo vira ponto de partida do desenvolvimento, em vez de um descarte com dados falsos.

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

- Replit

Não é esse o sistema que você usa?
Ver todas as conexões
