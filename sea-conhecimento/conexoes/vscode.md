# Conectar o SEA ao VS Code — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/vscode  
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

- Via IDE

- OAuth

- API Key

Requer VS Code com suporte nativo a MCP e o GitHub Copilot em modo agente.
Documentação oficial consultada: VS Code — Servidores MCP (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Confirme a versão do VS Code
O suporte nativo a MCP chegou no VS Code 1.102. Em Ajuda → Sobre, confira que a versão é 1.102 ou superior e atualize se for mais antiga.

- 2

### Ative o modo agente do Copilot
Abra o Chat (Ctrl + Alt + I) e troque o seletor de modo para Agent. É o modo que enxerga as ferramentas de MCP.

- 3

### Gere sua SEA API Key
Entre em searchitect.pro com sua conta, vá em Minha Conta → SEA API Keys e gere uma chave. Ela aparece uma única vez: copie e guarde em local seguro.
Formato da chavesea_live_xxxxxxxxxxxxxxxxx

- 4

### Adicione o servidor
Na paleta de comandos (Ctrl + Shift + P), execute o comando abaixo e escolha HTTP; o VS Code pergunta a URL e cria o arquivo de configuração.
MCP: Add Server

- 5

### Ou escreva a configuração direto
Para valer em todos os projetos, use MCP: Open User Configuration. Para valer só neste repositório, crie .vscode/mcp.json:
.vscode/mcp.json{
"servers": {
"SEA-Systems-Enterprise-Architect": {
"type": "http",
"url": "https://mcp.searchitect.pro/mcp",
"headers": {
"Authorization": "Bearer sea_live_xxxxxxxxxxxxxxxxx"
}
}
}
}

- 6

### Inicie e confirme
Use o botão Start que aparece acima do bloco do servidor no mcp.json, ou rode MCP: List Servers para ver o estado da conexão.
EsperadoSEA-Systems-Enterprise-Architect — Running

- 7

### Verifique as ferramentas
No Chat em modo Agent, abra o seletor de ferramentas (ícone de ferramenta). As cinco sea_* devem estar listadas e marcadas.

O .vscode/mcp.json fica dentro do repositório: se for versionar o arquivo, troque a chave por um input do próprio VS Code ("inputs" com "type": "promptString" e "password": true) em vez de deixar o Bearer em texto puro.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Entender o código herdado que ninguém documentou
A situaçãoVocê abriu um arquivo de customização de um projeto antigo e precisa saber o que ele faz antes de mexer.

O que você pede no VS CodeExplique o que esta customização faz no contexto do Sankhya, quais integrações do produto ela depende e o que quebra se eu alterar.

O que voltaA explicação vem amarrada ao comportamento real do ERP, não só à leitura literal do código. É a diferença entre saber o que a função executa e saber o que ela significa no processo.

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

- JetBrains

- Cursor

- Windsurf

- Eclipse

Não é esse o sistema que você usa?
Ver todas as conexões
