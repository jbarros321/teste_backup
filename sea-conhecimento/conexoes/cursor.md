# Conectar o SEA ao Cursor — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/cursor  
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

Servidores remotos por URL e cabeçalho, no arquivo global ou do projeto.
Documentação oficial consultada: Cursor — MCP (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Abra as configurações de MCP
Cursor Settings → MCP & Integrations → Add new global MCP server. O botão abre o ~/.cursor/mcp.json direto no editor.

- 3

### Declare o servidor SEA
A presença do campo url é o que diz ao Cursor que o servidor é remoto — sem command nem args.
~/.cursor/mcp.json{
"mcpServers": {
"SEA-Systems-Enterprise-Architect": {
"url": "https://mcp.searchitect.pro/mcp",
"headers": {
"Authorization": "Bearer sea_live_xxxxxxxxxxxxxxxxx"
}
}
}
}

- 4

### Salve e confirme
Volte ao painel de MCP. O SEA deve aparecer com o indicador verde e a lista de ferramentas; se ficar vermelho, use o botão de refresh.
EsperadoSEA-Systems-Enterprise-Architect — 4 tools enabled

- 5

### Use em modo Agent
As ferramentas de MCP só são chamadas no modo Agent do Cursor. Faça uma pergunta de Sankhya sem citar o Expert e observe a chamada sea_search aparecer no fluxo.

Também existe o .cursor/mcp.json por projeto, que é versionado junto com o código. Prefira o global (~/.cursor/mcp.json) para a chave: ele fica na sua máquina e não vai para o repositório.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Gerar a customização já no padrão do produto
A situaçãoO modelo escreve rápido e escreve plausível. No ERP, plausível e correto não são a mesma coisa.

O que você pede no CursorCrie a regra de validação que impede o faturamento quando o parceiro estiver com limite de crédito estourado, seguindo o padrão de customização do Sankhya.

O que voltaO Cursor consulta o SEA antes de gerar e produz a regra no ponto de extensão correto do produto — e não uma implementação paralela que funciona até a próxima atualização.

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

- Windsurf

- Eclipse

Não é esse o sistema que você usa?
Ver todas as conexões
