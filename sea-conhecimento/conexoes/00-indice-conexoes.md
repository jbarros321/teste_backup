# Conexões — leve o SEA para o Claude, ChatGPT, Copilot, Codex e mais — SEA

> Fonte: https://www.searchitect.pro/conexoes  
> Coletado em: 2026-10-14

---

## Selecione o sistema que você deseja integrar
Cada um tem a sua página, com o passo a passo, o status de validação e um caso de uso real. Se o seu não estiver aqui, o roteiro genérico no fim da lista vale para clientes compatíveis com MCP.
Validado
Conexão com o SEA confirmada em uso real.

Validado com restrições
Conexão confirmada, com uma limitação conhecida descrita abaixo.

Depende do ambiente/plano
O cliente documenta suporte a MCP remoto, mas a disponibilidade varia por plano, versão ou liberação do administrador.

Experimental
O próprio cliente trata o recurso como prévia; pode mudar sem aviso.

Compatibilidade a validar
Roteiro baseado na documentação do cliente. Ainda não confirmamos a conexão do nosso lado.

IAOs assistentes de uso geral, no chat que a equipe já abre todo dia.

- ChatGPTValidado com restriçõesConectar

- Claude (web)ValidadoConectar

- Claude DesktopValidadoConectar

- GeminiValidado com restriçõesConectar

Coding AgentsO SEA consultado durante o trabalho, sem sair do agente que escreve o código.

- Claude CodeValidadoConectar

- CodexValidadoConectar

- GitHub Copilot (agente)Compatibilidade a validarConectar

- GitHub Copilot CLICompatibilidade a validarConectar

- GitLab DuoDepende do ambiente/planoConectar

- Amazon Q DeveloperCompatibilidade a validarConectar

- ClineCompatibilidade a validarConectar

- AntigravityValidado com restriçõesConectar

IDEsConhecimento especializado no editor, junto do projeto aberto.

- VS CodeValidadoConectar

- JetBrainsDepende do ambiente/planoConectar

- CursorCompatibilidade a validarConectar

- WindsurfCompatibilidade a validarConectar

- EclipseCompatibilidade a validarConectar

App BuildersPrototipagem e geração de aplicações com o contexto Sankhya correto desde o início.

- LovableDepende do ambiente/planoConectar

- ReplitDepende do ambiente/planoConectar

Agentes / AutomaçãoOnde o SEA sai do editor e entra no processo: agentes corporativos e automação empresarial.

- Microsoft Copilot StudioDepende do ambiente/planoConectar

- Microsoft FoundryDepende do ambiente/planoConectar

- n8nCompatibilidade a validarConectar

- Vercel AI SDKCompatibilidade a validarConectar

- Aplicações própriasValidadoConectar

Qualquer outro cliente MCPNão achou o seu? O roteiro genérico vale para todo cliente compatível.

- Outro clienteDepende do ambiente/planoConectar

## Antes de tudo: como o SEA te reconhece
São duas portas, e a que você usa depende do sistema. Com OAuth (Claude, ChatGPT, Codex, Claude Code, VS Code e outros que suportam), não há chave: você autoriza e entra com a conta Searchitect no próprio searchitect.pro — a senha nunca é digitada no cliente de IA. Nos demais, use uma SEA API Key: exibida uma única vez ao ser gerada, rotacionável e revogável a qualquer momento.

Gerar minha API Key

## O que o servidor expõe
Um endpoint e cinco ferramentas, iguais em qualquer sistema. O agente usa a sea_search nas perguntas sobre Sankhya, a sea_prepare quando o trabalho envolve o seu projeto, e escolhe sozinho quando usar as demais.
Endpoint de produçãohttps://mcp.searchitect.pro/mcp
Authorization: Bearer sea_live_xxxxxxxxxxxxxxxxx

sea_prepare
Fase de preparação, quando o trabalho envolve o seu projeto ou quando você pede o grafo: confere conexão e parâmetros e orienta o agente a montar o grafo do projeto no seu ambiente. Não envia nada do projeto ao SEA. O agente apresenta "SEA pronto para uso" e o link para o grafo. Para ter a preparação em toda conversa, peça isso nas instruções do agente (CLAUDE.md no Claude Code, AGENTS.md no Codex).

sea_search
Ferramenta principal. Devolve conhecimento com evidência do especialista certo. Use expert="auto" e deixe o Agent Core escolher.

sea_route
Informa qual especialista responde por uma pergunta. Diagnóstico — não é necessário antes do sea_search.

sea_experts
Lista o Agent Core e os especialistas disponíveis, com o status de cada um.

sea_version
Versão do SEA e estado de prontidão do servidor. Útil para diagnóstico.

Prefere usar o SEA direto no chat, sem configurar nada?
Os especialistas também existem como assistentes publicados no ChatGPT — abra pela página de cada um.
Conhecer os 24 especialistas
