# Conectar o SEA ao Microsoft Copilot Studio — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/copilot-studio  
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

Depende da licença do Copilot Studio, da orquestração generativa ativa no agente e das políticas de DLP do ambiente.
Documentação oficial consultada: Microsoft — Adicionar servidor MCP ao agente (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Abra as ferramentas do agente
No Copilot Studio, abra o agente e vá em Tools → Add a tool → New tool → Model Context Protocol.

- 3

### Informe o servidor
Preencha o nome, a descrição e a URL do servidor. Em autenticação, escolha a opção de chave de API enviada no cabeçalho Authorization.
URL do servidor e cabeçalhohttps://mcp.searchitect.pro/mcp
Authorization: Bearer sea_live_xxxxxxxxxxxxxxxxx

- 4

### Ou publique como conector personalizado
Quem precisa de governança do Power Platform pode declarar o SEA como conector personalizado. O que marca o conector como MCP é a extensão x-ms-agentic-protocol:
Definição OpenAPI do conectorswagger: '2.0'
info:
title: SEA - Systems Enterprise Architect
description: Conhecimento especializado em Sankhya e arquitetura empresarial
version: '1.0'
host: mcp.searchitect.pro
basePath: /
schemes:
- https
paths:
/mcp:
post:
summary: SEA MCP Server
operationId: InvokeMCP
x-ms-agentic-protocol: mcp-streamable-1.0
responses:
'200':
description: Success

- 5

### Crie a conexão com a chave
Ao adicionar a ferramenta ao agente, o Copilot Studio pede uma conexão. É nela que a SEA API Key é gravada — dentro do ambiente do Power Platform, não no agente.

- 6

### Teste e publique
No painel de testes, faça uma pergunta de Sankhya. As ferramentas sea_* aparecem no rastro de atividade quando o agente as usa. Publique depois de confirmar.
Esperadosea_prepare, sea_search, sea_route, sea_experts, sea_version

A conexão vive por ambiente do Power Platform: configure em cada ambiente (dev, homologação, produção) e aplique as políticas de DLP da sua organização sobre o conector.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Dar um atendente interno para o time que usa o ERP
A situaçãoO time de suporte interno responde as mesmas vinte dúvidas de Sankhya toda semana, por chat, manualmente.

O que você pede no Microsoft Copilot StudioConfigure o agente para consultar o SEA sempre que a pergunta do colaborador envolver Sankhya, e responder citando a origem da informação.

O que voltaO agente corporativo passa a responder com evidência governada em vez de improviso, dentro do Teams que a empresa já usa. O suporte humano fica para o que é de fato exceção.

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

- Microsoft Foundry

- n8n

- Vercel AI SDK

- Aplicações próprias

Não é esse o sistema que você usa?
Ver todas as conexões
