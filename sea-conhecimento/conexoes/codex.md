# Conectar o SEA ao Codex — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/codex  
> Coletado em: 2026-10-14

---

## Compatibilidade
Validado
Conexão com o SEA confirmada em uso real.

Autenticação
OAuth · API Key
Revisado em
out/2026

- MCP

- CLI

- Via IDE

- OAuth

- API Key

A extensão e o CLI compartilham o mesmo config.toml. Prefira a chave em variável de ambiente ou o login OAuth.
Documentação oficial consultada: OpenAI — Codex e MCP (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Instale e faça login
No VS Code, instale a extensão oficial do Codex pelo marketplace e entre com a sua conta ChatGPT/OpenAI pelo painel. A extensão e o CLI compartilham a mesma configuração, em ~/.codex/config.toml.

- 2

### Adicione o servidor SEA
Abra o config.toml (no painel do Codex: engrenagem → Codex Settings → Open config.toml) e declare o servidor:
~/.codex/config.toml[mcp_servers.SEA-Systems-Enterprise-Architect]
url = "https://mcp.searchitect.pro/mcp"

- 3

### Caminho 1 — login OAuth
No terminal, rode o login do servidor. O navegador abre o searchitect.pro para você autorizar; o Codex guarda a credencial sozinho.
codex mcp login SEA-Systems-Enterprise-Architect

- 4

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 5

### Caminho 2 — chave em variável de ambiente
Para não deixar a chave no arquivo, guarde-a numa variável de ambiente e aponte o Codex para ela com bearer_token_env_var:
Variável de ambienteWindows (PowerShell): setx SEA_API_KEY "SUA_CHAVE_SEA"
macOS / Linux: echo 'export SEA_API_KEY="SUA_CHAVE_SEA"' >> ~/.zshrc # ou ~/.bashrc

EsperadoFeche e abra o terminal e o VS Code — só assim os processos recebem a nova variável.

- 6

### E no config.toml
~/.codex/config.toml[mcp_servers.SEA-Systems-Enterprise-Architect]
url = "https://mcp.searchitect.pro/mcp"
bearer_token_env_var = "SEA_API_KEY"

- 7

### Confirme no painel do Codex
O comando /mcp lista os servidores MCP ativos.
/mcp

EsperadoSEA-Systems-Enterprise-Architect
Connected

bearer_token_env_var guarda o NOME da variável, não a chave. Assim o config.toml pode ser lido por qualquer pessoa sem expor o segredo.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Migrar uma rotina antiga para o padrão atual
A situaçãoExiste uma customização escrita há anos que ainda usa um caminho desaconselhado, e ninguém tem certeza do que deve substituí-la.

O que você pede no CodexEsta rotina usa uma abordagem antiga de acesso a dados no Sankhya. Qual é o padrão atual e como fica a reescrita preservando o comportamento?

O que voltaO Codex consulta o SEA para confirmar qual é o padrão vigente e o que mudou de comportamento entre os dois, e só então reescreve. Sem isso, a migração troca um problema conhecido por um desconhecido.

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

- GitHub Copilot (agente)

- GitHub Copilot CLI

- GitLab Duo

Não é esse o sistema que você usa?
Ver todas as conexões
