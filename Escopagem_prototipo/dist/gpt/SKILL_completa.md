---
name: escopo-comercial
description: Monta um escopo comercial de projeto (proposta de escopo, visão de negócio, módulos, roadmap e esforço) no padrão Neuon Soluções. Use quando o usuário pedir "escopo comercial", "proposta de escopo", "documento comercial", "escopagem" de um cliente, ou quiser transformar um escopo técnico/levantamento em documento de negócio. Sempre pergunta quem está elaborando o escopo e estima esforço em HORAS.
---

# Escopo comercial — padrão Neuon

Produz o documento comercial que vai ao cliente: linguagem de negócio, sem SQL,
sem nome de tabela, sem jargão de implementação. O leitor é o decisor que assina.

Cada execução produz um escopo **novo**, para o cliente e o insumo daquela
conversa. A skill não carrega escopo de cliente anterior: o padrão que ela aplica
está descrito aqui e nas `references/` — estrutura, catálogo de horas, modelo de
fases e identidade visual. Nunca copie conteúdo, números ou nome de cliente de um
documento que esteja por perto no disco; eles só podem entrar no documento se o
usuário apresentar aquele material como insumo desta conversa.

## Quatro regras que nunca são dispensadas

1. **Autoria é obrigatória.** Antes de escrever uma linha, pergunte o nome de quem
   está elaborando o escopo (e o cargo/papel, se houver). Vai no quadro de capa como
   "Escopo elaborado por" e na assinatura do rodapé. Não invente, não use o nome do
   cliente, não use "Neuon Soluções" como autor, não deduza do git. Se o usuário não
   responder, pergunte de novo — este é o único bloqueio real da skill.
2. **Nunca se fala em gordura.** As palavras gordura, buffer, folga, contingência,
   reserva e margem de segurança não aparecem no documento, nem como linha de tabela,
   nem como percentual, nem nas premissas. O acréscimo que cobre suporte e go-live é
   **diluído dentro das horas dos blocos de desenvolvimento e das fases** — o cliente
   lê um número por entrega, não um número mais um colchão. Se o usuário pedir "20% de
   gordura", isso define o total; a distribuição é silenciosa.
3. **Esforço sempre em horas.** Toda estimativa — por módulo, por onda, total — é
   expressa em **quantidade de horas**. Nunca em meses, semanas, sprints, pontos,
   percentuais de esforço isolados ou faixas de preço. Se o material de origem fala
   em meses, converta para horas e diga a premissa de conversão.
   Prazo em meses só pode aparecer como informação derivada, ao lado das horas, e
   apenas se o usuário pedir.

4. **Dois arquivos, sempre, com a identidade Neuon.** Todo escopo sai em **Word e
   PDF**, gerados por `assets/gerar.py`, com a paleta e a tipografia do site da
   Neuon. O `.md` é a fonte de trabalho e fica ao lado, mas o que vai ao cliente
   são os dois arquivos formatados. Nunca entregue só o markdown.

## Identidade visual

Derivada de https://neuonagentes.netlify.app/. Sem logo — a marca aparece pela cor,
pela tipografia e pela assinatura em texto.

| Elemento | Valor |
|---|---|
| Azul da marca | `#2563eb` |
| Azul de fundo claro | `#eff6ff` |
| Fundo | `#f8fafc` · superfície `#ffffff` · alternada `#f1f5f9` |
| Texto | `#0f172a` · secundário `#475569` |
| Borda | `#e2e8f0` |
| Tipografia | Inter (400/500/600/700), fallback Segoe UI |

Aplicada por `assets/neuon.css`: barra de acento azul no topo da capa, selo
"PROPOSTA DE ESCOPO" em azul caixa-alta, cabeçalho de tabela em azul com texto
branco, linhas alternadas, títulos com filete azul. A4 com margens de 16-20 mm.

Se o usuário fornecer um arquivo de logo depois, coloque-o em `assets/` e acrescente
a regra de cabeçalho no CSS — o resto da identidade não muda.

## Processo

### 1. Levantar o insumo

Leia o que existir: escopo técnico, ata de reunião, planilhas do cliente, e-mails,
transcrição. Não escreva o comercial a partir de suposição — cada número do
documento precisa de origem. Se o insumo for fino, liste o que falta e siga com
premissas explicitamente marcadas.

### 2. Entrevista mínima

Pergunte em **uma** rodada (use AskUserQuestion quando houver opções fechadas):

- **Quem está elaborando o escopo** (nome e papel) — obrigatório.
- **Anexo ou descritivo?** — obrigatório, muda a estrutura inteira do documento:
  - **Anexo** — enxuto, 7 seções, para grampear em proposta ou contrato. Siga
    `references/anexo.md`.
  - **Descritivo** — os 12 capítulos completos. Siga `references/estrutura.md`.
- Cliente: razão social e CNPJ. Se o usuário não tiver o CNPJ ou disser para não
  incluir, **omita a linha inteira** da capa — não deixe "a confirmar" no documento
  que vai ao cliente.
- Se já existe documento anterior deste cliente, qual o **modelo de fases** adotado
  (ver regra em `references/fases-e-ciclos.md`).
- Plataforma-base (default: Sankhya ERP + módulos de personalização).
- Perfil de horas: valor/hora ou faixa, se o documento deve mostrar investimento.
  Se o usuário não quiser preço, o documento fica só em horas.
- Carga semanal considerada para derivar prazo (default: 160 h/mês por frente).

O resto você infere do insumo e marca como premissa.

### 3. Escrever o documento

Conforme a resposta da entrevista:

- **Descritivo** → `references/estrutura.md`, os 12 capítulos.
- **Anexo** → `references/anexo.md`, as 7 seções.

A capa é a mesma nos dois, trocando só o selo do topo, e o tom é o mesmo:
linguagem de negócio, sem jargão de implementação.

**Quando o insumo é um levantamento do que já existe** (código lido, sistema em
produção), separe o que é retrato do que é venda:

- O que já está em produção entra no capítulo 4 com a coluna `Situação` marcada
  **"Já entregue"** e **zero horas**, só para o leitor situar o conjunto. Diga isso
  em uma linha sob a tabela. Cobrar de novo o que já existe é o erro mais caro aqui.
- Pendência, bug e lacuna encontrados no levantamento **não entram nas horas**: vão
  para um **Anexo A — Pontos mapeados no levantamento**, tabela `# | Ponto | Efeito
  hoje`, com a frase em negrito de que nenhum está incluído no total e que são
  estimados à parte quando contratados. Replique a exclusão no capítulo 4.2 e nas
  premissas. Isso entrega o diagnóstico (que tem valor) sem dar trabalho de graça.
- **Módulo dentro de uma solução maior**: quando o escopo é uma parte de um sistema
  que já existe, a capa leva duas linhas — `Solução` e `Módulo desta fase` — e o
  capítulo 5 se chama "Módulo desta fase". No capítulo 6, a coluna do meio é
  "Solução atual", não "Plataforma padrão".

### 4. Estimar

Siga `references/estimativa-horas.md`: catálogo bottom-up por tipo de item,
overheads (GP, homologação, treinamento, migração) e o formato das tabelas de
horas por módulo e por onda. Toda onda fecha com subtotal de horas; o documento
fecha com total de horas.

### 5. Diluir o acréscimo e distribuir em fases

Se houver acréscimo contratado sobre o escopo (suporte, go-live, estabilização),
multiplique as horas de cada bloco pelo fator correspondente e arredonde em 0,5 h,
conferindo que a soma fecha o total. Nenhuma linha separada, nenhum percentual
exposto. Siga `references/fases-e-ciclos.md`. Cada onda é um ciclo de entrega com
as 7 fases (detalhamento, parametrização, desenvolvimento, integrações, migração,
testes/homologação, treinamento/go-live), e cada fase recebe horas e percentual.
A soma das fases de todas as ondas tem que fechar com o total da capa.

### 6. Conferir as somas

Antes de entregar, some de verdade — não confie na leitura. Precisam fechar no
mesmo total: blocos/módulos do capítulo 5, natureza do esforço do capítulo 6,
fases do capítulo 7 e **cada tabela de detalhamento de fase**. Quem somar linha
por linha tem que chegar ao total sem encontrar sobra — é assim que a diluição da
regra 2 se sustenta. Rode também uma busca pelas palavras proibidas antes de fechar.

### 7. Entregar

Rode o gerador — ele produz os dois arquivos de uma vez, com a identidade visual:

```
python <skill>/assets/gerar.py <caminho>/Escopo_Comercial_<Cliente>.md
```

O que ele faz:

- **Word** — pandoc com `--reference-doc`, aplicando Inter e os títulos em azul da
  marca. O reference.docx é montado na primeira execução e reaproveitado depois.
- **PDF** — pandoc gera um HTML com `neuon.css` e o Chrome ou Edge em modo headless
  imprime com `--print-to-pdf`. Não precisa de LaTeX nem de wkhtmltopdf.
- Recusa de saída quando o `.docx` está aberto no Word, apontando o arquivo.

Regras de entrega:

- Os três arquivos ficam juntos: `.md` (fonte), `.docx` e `.pdf` (cliente).
- **Nunca** use `--toc` — insere um "Table of Contents" em inglês que não pertence
  ao documento.
- Se pandoc não existir: `winget install --id JohnMacFarlane.Pandoc -e
  --accept-source-agreements --accept-package-agreements`. Ele pode cair em
  `%LOCALAPPDATA%\Pandoc\pandoc.exe` e não entrar no PATH — o gerador já procura lá.
- Ao final, informe no chat: autor registrado, formato (anexo ou descritivo), total
  de horas, como as fases ficaram e o que ficou como premissa em aberto.

## Erros a evitar

- Escopo sem capítulo "Fora do escopo" — é ele que evita discussão de aditivo.
- Ganho escrito como funcionalidade ("tela de orçamento") em vez de efeito no
  negócio ("estouro de teto vira bloqueio, não devolução de recurso").
- Número sem origem. Todo indicador do capítulo 1 sai do insumo do cliente.
- Estimativa em um bloco único. Horas precisam ser rastreáveis até o item e até a fase.
- Onda sem fase de testes ou sem treinamento dimensionado.
- Tabelas de horas que não fecham entre os capítulos 5, 6 e 7.
- Qualquer linha, nota de rodapé ou premissa revelando buffer, gordura ou contingência.
- Promessa de integração por API de terceiro sem confirmação de que a API existe.
- Cobrar horas por bloco que já está em produção.
- Trocar o modelo de fases que o cliente já conhece pelo modelo padrão da skill.
- Deixar "a confirmar" em dado cadastral na capa do documento que vai ao cliente.
- Somar os capítulos de olho, sem conferir.
- Entregar só o markdown, ou só um dos dois formatos.
- Escrever um anexo com capítulo de sumário executivo ou de diagnóstico.
