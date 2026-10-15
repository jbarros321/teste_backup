# Conectar o SEA ao Windsurf — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/windsurf  
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

O Cascade limita o número de ferramentas ativas ao mesmo tempo.
Documentação oficial consultada: Windsurf — MCP no Cascade (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Abra a configuração do Cascade
No painel do Cascade, clique no ícone de ferramentas/plugins e escolha a opção de ver a configuração bruta (View raw config). Ela abre o arquivo abaixo:
Onde fica o arquivo~/.codeium/windsurf/mcp_config.json

- 3

### Declare o servidor SEA
O Windsurf usa serverUrl (e não url) para identificar servidores remotos:
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

### Recarregue
Salve o arquivo e use o botão Refresh no painel de MCP do Cascade. O SEA aparece com as ferramentas listadas.
Esperadosea_prepare, sea_search, sea_route, sea_experts, sea_version

O Cascade limita o número de ferramentas ativas simultaneamente. Se você já tem muitos servidores conectados, desative os que não estiver usando para garantir que as sea_* fiquem disponíveis.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Fazer uma refatoração ampla sem perder o fio
A situaçãoA mudança atravessa vários arquivos e precisa manter coerência com regras do ERP em todos eles.

O que você pede no WindsurfRefatore este módulo de integração mantendo o comportamento e aplicando o padrão atual de chamadas ao Sankhya em todos os pontos.

O que voltaO agente mantém o mesmo contrato de serviço em todos os arquivos porque consulta a mesma fonte a cada ponto — a inconsistência típica da refatoração assistida não aparece.

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

- Eclipse

Não é esse o sistema que você usa?
Ver todas as conexões
