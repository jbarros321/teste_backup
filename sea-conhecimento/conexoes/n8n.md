# Conectar o SEA ao n8n — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/n8n  
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

- API Key

Pelo nó MCP Client Tool ligado a um AI Agent. Em instâncias self-hosted, a rede precisa permitir saída HTTPS.
Documentação oficial consultada: n8n — MCP Client Tool (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Crie a credencial de autenticação
Em Credentials → New, escolha Header Auth. No nome do cabeçalho use Authorization e, no valor, Bearer seguido da sua chave. A credencial fica criptografada no n8n.
Header AuthName: Authorization
Value: Bearer sea_live_xxxxxxxxxxxxxxxxx

- 3

### Monte o fluxo com um AI Agent
Adicione o nó AI Agent e conecte um modelo de linguagem a ele. É o agente que decide quando consultar o SEA.

- 4

### Acrescente o MCP Client Tool
Na entrada de ferramentas do AI Agent, adicione o nó MCP Client Tool e configure o endpoint, o transporte e a credencial criada no primeiro passo.
MCP Client ToolEndpoint: https://mcp.searchitect.pro/mcp
Server Transport: HTTP Streamable
Authentication: Header Auth (credencial criada acima)
Tools to Include: All

- 5

### Oriente o agente no system prompt
Sem instrução, o agente pode responder de memória. Diga explicitamente para consultar o SEA antes de afirmar qualquer coisa sobre Sankhya.
Consulte o SEA (sea_search) antes de responder sobre Sankhya. Use expert="auto" e cite a evidência retornada.

- 6

### Execute e confirme
Rode o fluxo com uma pergunta real. Abra a saída do nó MCP Client Tool: ela mostra a chamada feita e a evidência devolvida pelo SEA.
Esperadosea_prepare, sea_search, sea_route, sea_experts, sea_version

Em n8n self-hosted, garanta que a instância tenha saída HTTPS para mcp.searchitect.pro — em redes corporativas fechadas esse é o motivo mais comum de o nó falhar na conexão.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Classificar e enriquecer chamados automaticamente
A situaçãoOs chamados chegam por e-mail, sem categoria e sem diagnóstico, e alguém precisa triar tudo manualmente antes de distribuir.

O que você pede no n8nNo fluxo: ao receber um chamado, consulte o SEA com o texto do usuário, identifique o módulo do Sankhya envolvido e anexe o diagnóstico preliminar antes de atribuir à fila.

O que voltaO chamado chega na fila já com módulo identificado e primeira análise anexada. A triagem roda de madrugada sem ninguém acordado, e o analista começa do diagnóstico, não do zero.

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

- Vercel AI SDK

- Aplicações próprias

Não é esse o sistema que você usa?
Ver todas as conexões
