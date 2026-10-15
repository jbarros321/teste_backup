# Conectar o SEA ao GitLab Duo — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/gitlab-duo  
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

- Via IDE

- Conforme plano

Depende do tier contratado e de o MCP estar habilitado no grupo ou na instância. Menus mudam entre SaaS e self-managed.
Documentação oficial consultada: GitLab — Clientes MCP no Duo (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Confirme que o MCP está habilitado
O suporte a MCP no Duo é recente e vem desligado por padrão. No grupo ou projeto: Settings → General → GitLab Duo, e habilite o uso de servidores MCP. Em instâncias self-managed, o administrador precisa liberar antes.

- 2

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 3

### Declare o servidor SEA
O Duo lê a configuração de MCP do repositório. Crie o arquivo abaixo na raiz do projeto:
.gitlab/duo/mcp.json{
"mcpServers": {
"SEA-Systems-Enterprise-Architect": {
"url": "https://mcp.searchitect.pro/mcp",
"headers": {
"Authorization": "Bearer $SEA_API_KEY"
}
}
}
}

EsperadoReferencie a chave por variável ($SEA_API_KEY), nunca em texto puro — este arquivo é versionado junto com o código.

- 4

### Cadastre a chave como variável de CI/CD
Em Settings → CI/CD → Variables, crie SEA_API_KEY marcada como Masked e Protected. É ela que preenche o header sem expor o segredo no repositório.

- 5

### Na IDE, use a extensão GitLab Workflow
Quem trabalha pelo VS Code ou pelo plugin JetBrains configura o mesmo servidor nas preferências da extensão (GitLab Duo → MCP servers). Aí a chave fica na máquina, fora do repositório.

- 6

### Confirme no Duo Agentic Chat
Abra o chat agêntico e peça a lista de ferramentas disponíveis. As cinco sea_* devem aparecer.
Esperadosea_prepare, sea_search, sea_route, sea_experts, sea_version

O caminho exato dos menus muda entre versões do GitLab e entre SaaS e self-managed, e o recurso depende do tier contratado. Se a sua versão ainda não expõe MCP no Duo, o endpoint continua acessível pela aba Outro cliente.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Padronizar a customização entre squads
A situaçãoTrês squads mexem no mesmo ERP com convenções diferentes, e a divergência só aparece quando dois merges se encontram.

O que você pede no GitLab DuoQual é a prática recomendada de versionamento e empacotamento de customizações no Sankhya para times que trabalham em paralelo?

O que voltaA resposta vem com a prática e, mais importante, com o motivo dela — o que o ERP faz no deploy que torna a convenção necessária. É o argumento que sustenta a regra no próximo desacordo.

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

- GitHub Copilot (agente)

- GitHub Copilot CLI

Não é esse o sistema que você usa?
Ver todas as conexões
