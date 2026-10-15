# Conectar o SEA ao ChatGPT — Sankhya ERP via MCP — SEA

> Fonte: https://www.searchitect.pro/conexoes/chatgpt  
> Coletado em: 2026-10-14

---

## Compatibilidade
Validado com restrições
Conexão confirmada, com uma limitação conhecida descrita abaixo.

Autenticação
OAuth
Revisado em
out/2026

- MCP

- OAuth

- App publicado

- Conforme plano

Disponibilidade pode variar conforme plano e modo suportado pelo cliente. O conector MCP personalizado depende do modo desenvolvedor, liberado hoje principalmente em workspaces Business, Enterprise e Edu.
Documentação oficial consultada: OpenAI — Developer mode e apps MCP (abre em nova aba)

## Como conectar
O endpoint é sempre o mesmo. O que muda de sistema para sistema é só onde a configuração entra.

- 1

### Confirme se a sua conta oferece conectores MCP
Conectores MCP personalizados dependem do modo desenvolvedor do ChatGPT na web. Hoje ele é liberado principalmente em workspaces Business, Enterprise e Edu, e em workspaces o administrador pode precisar habilitá-lo antes. Se a opção não aparecer na sua conta, use um dos outros clientes desta página.

- 2

### Crie o conector apontando para o SEA
Em Configurações → Apps e conectores, com o modo desenvolvedor ativo, crie um conector e informe o endpoint do SEA. Escolha OAuth como autenticação.
URL do servidor MCPhttps://mcp.searchitect.pro/mcp

- 3

### Autorize no searchitect.pro
O ChatGPT abre uma página do searchitect.pro. Entre com a sua conta Searchitect e clique em Autorizar.
EsperadoSua senha é digitada apenas no searchitect.pro. O ChatGPT recebe só um acesso temporário, que você revoga quando quiser em Minha Conta → SEA API Keys → Aplicativos conectados.

- 4

### Use na conversa
Ative o conector na conversa e faça a pergunta de Sankhya como faria a um consultor. As ferramentas sea_* ficam disponíveis para o modelo.

Os Experts também existem como assistentes publicados no ChatGPT (GPTs), abertos pela página de cada Expert. É um caminho separado do MCP: não passa pelo Agent Core e consome a cota da sua conta ChatGPT.
Precisa da chave? Ela é exibida uma única vez ao ser gerada, e pode ser rotacionada ou revogada a qualquer momento.

Gerar minha API Key

## Caso de uso
Entender um erro de nota fiscal antes de abrir chamado
A situaçãoA emissão parou com uma rejeição da SEFAZ e o time fiscal precisa saber se é parametrização, cadastro ou regra tributária — antes de escalar para a consultoria.

O que você pede no ChatGPTA emissão de NF-e no Sankhya está retornando rejeição 'Código de Município inválido'. Onde isso é parametrizado e o que devo conferir, na ordem?

O que voltaO Expert fiscal responde com a sequência de verificação — cadastro de cidade, código IBGE, parceiro e TOP envolvida — classificando cada ponto por tipo de evidência. Você chega ao chamado com o diagnóstico pronto, ou sem precisar abrir um.

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

- Claude (web)

- Claude Desktop

- Gemini

Não é esse o sistema que você usa?
Ver todas as conexões
