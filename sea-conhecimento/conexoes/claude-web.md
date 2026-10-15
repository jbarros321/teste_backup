# Conectar o SEA ao Claude (web) — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/claude-web  
> Coletado em: 2026-10-14

---

## Compatibilidade
Validado
Conexão com o SEA confirmada em uso real.

Autenticação
OAuth
Revisado em
out/2026

- MCP

- OAuth

Conectores personalizados estão disponíveis nos planos do Claude, em beta; no plano gratuito há limite de um conector. Em Team e Enterprise, o administrador da organização pode precisar adicionar o conector antes.
Documentação oficial consultada: Anthropic — Conectores personalizados (MCP remoto) (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Abra os conectores
Em claude.ai: Configurações → Conectores → Adicionar conector personalizado. Em planos Team e Enterprise, o administrador da organização pode precisar adicionar o conector primeiro.

- 2

### Informe nome e endereço
Em “Nome”, use o que preferir — é só o rótulo da lista. Em “URL do servidor MCP remoto”, cole o endpoint do SEA.
URL do servidor MCP remotohttps://mcp.searchitect.pro/mcp

- 3

### Deixe as configurações avançadas em branco
Client ID e Client Secret existem para servidores que exigem registro manual. O SEA registra o cliente sozinho, então não preencha nada ali.

- 4

### Clique em Adicionar e autorize
O Claude abre uma página do searchitect.pro. Entre com sua conta e clique em Autorizar. Você volta ao Claude já conectado.
EsperadoSua senha é digitada apenas no searchitect.pro. O Claude nunca a recebe — ele recebe só um acesso temporário, renovado sozinho, que você pode cortar quando quiser.

- 5

### Confirme
O conector aparece na lista com as ferramentas sea_* disponíveis na conversa.

Este caminho não usa SEA API Key: a autorização vale por conta logada, e você desconecta em Minha Conta → SEA API Keys → Aplicativos conectados. No plano gratuito do Claude, o limite é de um conector personalizado.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Revisar uma especificação funcional com um especialista do lado
A situaçãoVocê recebeu um documento de requisitos de um processo de faturamento e precisa achar o que não vai funcionar no Sankhya antes de aprovar o escopo.

O que você pede no Claude (web)Vou colar uma especificação de customização de faturamento. Aponte o que conflita com o comportamento padrão do Sankhya e o que exige regra de negócio em vez de parametrização.

O que voltaO Claude consulta o SEA durante a leitura e marca os pontos conflitantes com a evidência de origem, separando o que é comportamento documentado do que é inferência técnica. A revisão deixa de depender de quem lembra do módulo.

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

- ChatGPT

- Claude Desktop

- Gemini

Não é esse o sistema que você usa?
Ver todas as conexões
