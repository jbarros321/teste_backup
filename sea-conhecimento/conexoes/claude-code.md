# Conectar o SEA ao Claude Code — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/claude-code  
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

Funciona no terminal e na extensão do VS Code, com login OAuth ou API Key.
Documentação oficial consultada: Claude Code — MCP (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Instale o Claude Code
No VS Code, instale a extensão “Claude Code” pelo marketplace e faça login na sua conta Claude. Para o terminal, instale o CLI:
Instalação do CLImacOS / Linux: curl -fsSL https://claude.ai/install.sh | bash
Windows: irm https://claude.ai/install.ps1 | iex

- 2

### Confirme a instalação
Feche e abra o terminal para recarregar o PATH, e confira:
claude --version

EsperadoSe retornar uma versão, o CLI está instalado corretamente.

- 3

### Cadastre o SEA
O --scope user deixa o SEA disponível em qualquer projeto. Sem cabeçalho, a autenticação acontece por login OAuth no passo seguinte.
claude mcp add --transport http --scope user SEA-Systems-Enterprise-Architect https://mcp.searchitect.pro/mcp

- 4

### Faça login
Dentro do Claude Code, abra o gerenciador de MCP, escolha o SEA e autentique. O navegador abre o searchitect.pro; entre com a sua conta e autorize.
/mcp

EsperadoSEA-Systems-Enterprise-Architect
✔ Connected

- 5

### Alternativa: API Key no cabeçalho
Para ambientes sem navegador, gere uma SEA API Key em Minha Conta e cadastre o servidor com o cabeçalho, trocando SUA_CHAVE_SEA pela chave:
claude mcp add --transport http --scope user SEA-Systems-Enterprise-Architect https://mcp.searchitect.pro/mcp --header "Authorization: Bearer SUA_CHAVE_SEA"

- 6

### Confirme
claude mcp list

EsperadoSEA-Systems-Enterprise-Architect: https://mcp.searchitect.pro/mcp (HTTP) - ✔ Connected

Se você configurar o cabeçalho com uma chave inválida, o Claude Code não cai para o login OAuth: ele reporta falha. Corrija a chave ou remova o cabeçalho para usar o login.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Escrever uma integração sem inventar assinatura de serviço
A situaçãoVocê vai implementar a sincronização de pedidos entre um e-commerce e o Sankhya. O modelo sozinho acerta a forma do código e erra o nome dos serviços.

O que você pede no Claude CodeImplemente o cliente de integração que cria pedidos de venda no Sankhya a partir do payload do e-commerce, com tratamento de erro e reprocessamento.

O que voltaO Claude Code chama o SEA por conta própria antes de escrever, recebe o contrato documentado do serviço e escreve o código a partir dele, não de um palpite. O ganho não é velocidade — é não descobrir o erro na homologação.

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

- Codex

- GitHub Copilot (agente)

- GitHub Copilot CLI

- GitLab Duo

Não é esse o sistema que você usa?
Ver todas as conexões
