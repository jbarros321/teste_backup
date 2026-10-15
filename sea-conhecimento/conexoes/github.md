# Conectar o SEA ao GitHub Copilot (agente) — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/github  
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

- Conforme plano

A documentação atual do GitHub indica que o agente de nuvem do Copilot trabalha com servidores MCP locais, e a política da organização pode bloquear a configuração. Para o Copilot no editor, use o roteiro do VS Code.
Documentação oficial consultada: GitHub — Configurar servidores MCP (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 2

### Guarde a chave como secret do repositório
No repositório: Settings → Secrets and variables → Agents → aba Secrets → New repository secret. O nome precisa começar com COPILOT_MCP_ — só secrets com esse prefixo chegam à configuração de MCP.
Nome do secretCOPILOT_MCP_SEA_API_KEY

EsperadoNo valor, cole a SEA API Key. Ela fica no cofre do repositório e nunca aparece no JSON.

- 3

### Abra a configuração de MCP
Ainda em Settings, na barra lateral em “Code, planning, and automation”: Copilot → MCP servers.

- 4

### Declare o servidor SEA
Para servidores remotos, o Copilot exige type, url e tools; headers é onde entra a autenticação, com o secret referenciado por $NOME.
Configuração MCP do repositório{
"mcpServers": {
"SEA-Systems-Enterprise-Architect": {
"type": "http",
"url": "https://mcp.searchitect.pro/mcp",
"tools": ["sea_prepare", "sea_search", "sea_route", "sea_experts", "sea_version"],
"headers": {
"Authorization": "Bearer $COPILOT_MCP_SEA_API_KEY"
}
}
}
}

- 5

### Salve
O GitHub valida a sintaxe antes de gravar. Se a configuração não for salva, é o JSON que está inválido — confira vírgulas e aspas.

- 6

### Confirme na primeira tarefa
Atribua uma issue ao Copilot ou peça uma mudança pelo Agents. Na sessão do agente, o log de inicialização lista os servidores MCP carregados e as ferramentas sea_* liberadas.
EsperadoSEA-Systems-Enterprise-Architect
sea_prepare, sea_search, sea_route, sea_experts, sea_version

Vale para o repositório onde foi configurado — em organizações, dá para definir a mesma configuração no nível da organização e herdar em todos os repositórios. O campo tools é obrigatório em servidores remotos: se preferir liberar tudo de uma vez, use ["*"] no lugar da lista.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Revisar o pull request com o padrão Sankhya como critério
A situaçãoO PR passa nos testes e nos lints, mas ninguém do time de revisão domina o módulo que está sendo tocado.

O que você pede no GitHub Copilot (agente)Revise este diff considerando as práticas de customização do Sankhya. Aponte o que viola o padrão e o que quebra em atualização de versão.

O que voltaO Copilot traz a crítica com a evidência do Expert anexada, separando preferência de estilo de risco real de upgrade. A revisão deixa de depender da agenda da única pessoa que conhece o módulo.

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

- Claude Code

- Codex

- GitHub Copilot CLI

- GitLab Duo

Não é esse o sistema que você usa?
Ver todas as conexões
