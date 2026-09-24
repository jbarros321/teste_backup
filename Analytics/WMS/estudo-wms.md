# Doca a doca: o WMS em uma leitura

> Estudo resumido · Logística 4.0
> Base: e-book *WMS — Logística 4.0*, de Alexsandro C. Lopes (2025) — https://alexsandrowms-ux.github.io/wms-ebook/

O WMS é o sistema que decide **onde guardar, quem busca, em que ordem e dentro de qual caixa**. Este resumo cobre as nove etapas do fluxo de um centro de distribuição, os dois processos transversais que sustentam tudo, e o que separa uma implantação bem-sucedida de uma que trava no go-live.

| Indicador | Referência em operação madura |
|---|---|
| Acuracidade de estoque | 99,8% |
| Custo de mão de obra direta | −35% |
| Produtividade de picking | +60% |
| Estabilização pós go-live | 30–60 dias |

---

## 01 · Ponto de partida — o que o WMS realmente faz

Um WMS (*Warehouse Management System*) gerencia o armazém no nível do **endereço e da tarefa** — não do saldo contábil. O ERP sabe que existem 400 unidades; o WMS sabe que 120 estão no porta-palete `A-03-04-2`, lote `L2411`, validade 07/2027, e que o operador João deve buscá-las antes das 14h.

**Posição no ecossistema**
`ERP` envia pedidos e notas → `OMS` orquestra a promessa ao cliente → `WMS` executa dentro das quatro paredes → `TMS` assume a partir da doca.
Sem WMS, o ERP tenta controlar o armazém por saldo — e erra por endereço, lote e sequência.

**Sinais de que falta um**
- Inventário só fecha com ajuste manual
- O operador "sabe de cabeça" onde o item está
- Separação depende de papel
- Avaria e divergência viram e-mail
- Nenhum indicador por operador existe

**Modalidades**
- **On-premise** — controle total, exige TI própria
- **Cloud / SaaS** — implantação rápida, custo recorrente
- **Módulo do ERP** — integração nativa, menor profundidade funcional
- **Autônomo 4.0** — best-of-breed, integra com automação e robótica

---

## 02 · Macro fluxo — nove etapas, da portaria ao caminhão

A numeração não é enfeite: cada etapa consome o resultado da anterior. Uma etiqueta de palete mal gerada no recebimento reaparece como volume perdido no carregamento.

### Etapa 01 — Recebimento
Conferência cega (*blind receiving*): o conferente registra o que vê, sem enxergar a quantidade esperada. O sistema compara depois e abre a divergência — inclusive gerando NF de devolução automática.
`blind receiving` · `avaria` · `NF devolução`

### Etapa 02 — Armazenagem
Slotting decide o endereço: curva ABC por giro, zoneamento por velocidade, regra de saída (FIFO / FEFO / LIFO) e cross-docking para o que nem deve ser guardado.
`curva ABC` · `FEFO` · `cross-dock`

### Etapa 03 — Picking
A escolha da estratégia é o maior ganho isolado da operação — o mesmo conjunto de pedidos pode custar 380 m ou 80 m de caminhada por operador.
`wave` · `cluster` · `zone`

### Etapa 04 — Conferência de saída
Última chance de barrar o erro antes do cliente: leitura de código ou RFID, verificação por peso, registro fotográfico e re-picking automático quando falta item.
`barcode` · `pesagem` · `re-picking`

### Etapa 05 — Packing
Cartonização sugere a menor caixa viável; o sistema calcula peso cúbico (DIM weight), material de proteção e tratamento especial para frágil, perecível e alto valor.
`cartonization` · `cubagem`

### Etapa 06 — Etiquetagem de volume
Cada volume ganha um SSCC — código GS1 de 18 dígitos que identifica unicamente aquela embalagem no mundo — mais etiqueta da transportadora e avisos de cuidado.
`SSCC` · `GS1` · `multi-carrier`

### Etapa 07 — Conferência de volumes
Bipar cada SSCC fecha o romaneio: o pedido só é liberado quando todos os volumes existem, pesam o previsto e estão consolidados.
`romaneio` · `consolidação`

### Etapa 08 — Carregamento
Designação de doca, plano de carga em sequência LIFO (último a entrar é o primeiro a descer), emissão de CT-e e manifesto, e transmissão ao TMS.
`dock door` · `load plan` · `CT-e`

### Transversal — Gestão de estoque & mão de obra
Inventário global, cíclico e rotativo; rastreabilidade por lote, série e grade. Do outro lado, *task interleaving*, alocação por zona e KPIs por operador (picks/hora, acuracidade, distância percorrida).
`inventário cíclico` · `track & trace` · `picks/h`

---

## 03 · Picking em detalhe — seis estratégias, um critério

Não existe estratégia melhor — existe a que combina com o perfil do pedido. A pergunta decisiva: **quantas linhas tem o pedido médio e quantos pedidos compartilham os mesmos SKUs?**

| Estratégia | Como funciona | Indicada quando | Custo oculto |
|---|---|---|---|
| **Discreto** | 1 operador, 1 pedido, do começo ao fim | Pedidos grandes, volume baixo, operação simples | Maior distância percorrida por linha |
| **Onda (wave)** | Pedidos liberados em janelas programadas | Há corte de transportadora por horário | Ociosidade entre ondas |
| **Cluster** | 1 operador leva N pedidos no mesmo carrinho | Pedidos pequenos, e-commerce | Erro de caixa destino; exige *put-to-light* ou disciplina |
| **Batch** | Agrupa por SKU; separa tudo e rateia depois | Poucos SKUs, muitos pedidos iguais | Precisa de área e etapa de sorter |
| **Zone** | Cada operador cobre uma zona; a caixa passa adiante (*pick-and-pass*) | Armazém grande, SKUs muito distintos | Gargalo na zona mais lenta |
| **Wavy** | Wave + cluster combinados | Alto volume com janelas de expedição | Complexidade de parametrização |

---

## 04 · Implantação — oito fases até o dia D

Prazo típico de ponta a ponta: **4 a 7 meses**. As fases 1 a 3 definem o sucesso; as demais executam o que foi decidido nelas.

| Fase | Etapa | Duração | O que acontece |
|---|---|---|---|
| 1 | Diagnóstico AS-IS | 2–3 sem. | Fotografia honesta da operação atual, com números reais de volume e sazonalidade |
| 2 | Levantamento de processos | 2–4 sem. | Entrevistas com operadores, walk-through no armazém, dados históricos, fluxograma validado. É aqui que aparecem as exceções que ninguém documentou |
| 3 | Gap analysis (FIT × GAP) | 1–2 sem. | O que o WMS já faz nativamente (FIT) e o que exigiria customização (GAP) |
| 4 | Configuração e parametrização | 4–8 sem. | Endereçamento, regras de slotting, perfis de tarefa, integrações com ERP e TMS |
| 5 | Testes e homologação | 3–4 sem. | Cenários reais, inclusive os de exceção: divergência, avaria, recall, cancelamento |
| 6 | Treinamento | 2–3 sem. | Key users primeiro, depois end users — com coletor na mão, em homologação |
| 7 | **Go-live (cutover)** | dia D | Inventário de virada, congelamento de movimentação, saldo carregado, operação no ar. **Nunca em pico de demanda** |
| 8 | Suporte e estabilização | 30–60 d | Produtividade cai no início e se recupera em torno de 90 dias. Planeje a queda em vez de se assustar com ela |

> **Regra de ouro:** adapte o processo ao WMS, não o WMS ao processo. Cada customização vira dívida em toda atualização futura — só se justifica quando o processo é diferencial competitivo real, não hábito.

---

## 05 · Boas práticas — combinar SKU, estrutura e parâmetro

A maior parte dos erros de projeto nasce de um casamento errado entre o tipo de produto, a estrutura física que o abriga e a regra configurada no sistema.

| Tipo de SKU | Estrutura ideal | Configuração no WMS | Ponto de atenção |
|---|---|---|---|
| **Validade** (alimento, farma) | Flow rack | `FEFO` + bloqueio por shelf life mínimo | Definir quem aprova venda de lote curto |
| **Número de série** (eletrônicos) | Picking em bin / prateleira | Captura de série na saída | Leitura unitária derruba produtividade — dimensione |
| **Grade** (tamanho × cor) | Bin fracionado, zona A | Matriz de grade, não SKU por variante solta | Explosão de endereços |
| **Peso variável** (hortifrúti, açougue) | Flow rack refrigerado | Captura de peso na conferência | Divergência de peso × quantidade faturada |
| **Alto volume, palete cheio** | Porta-palete | Reabastecimento automático do picking | Ponto de ressuprimento mal ajustado gera ruptura |
| **Alta densidade, lote único** | Drive-in / push-back | `LIFO` | Incompatível com FEFO — não misture lotes |
| **Peças longas** | Cantilever | Endereço sem cubagem padrão | Cartonização não se aplica |

### ✅ Faça
- Treinar em homologação com o coletor real, não em slide
- Formar multiplicadores por turno — o operador aprende melhor com o colega
- Gamificar os KPIs individuais nas primeiras semanas
- Fazer survey de RF antes de comprar qualquer hardware: sem Wi-Fi em 100% da área, o WMS não existe
- Testar os cenários de exceção com a mesma seriedade do fluxo feliz

### ❌ Evite
- Go-live em pico de sazonalidade
- Comprar coletores e impressoras antes de escolher o WMS
- Customizar para preservar um processo que só existe por hábito
- Treinamento exclusivamente teórico, sem prática no chão
- Prometer produtividade igual ou maior já na primeira semana

---

## 06 · Glossário

| Termo | Significado |
|---|---|
| **FIFO / FEFO / LIFO** | Primeiro que entra sai primeiro / primeiro que vence sai primeiro / último que entra sai primeiro |
| **SSCC** | *Serial Shipping Container Code* — 18 dígitos GS1 que identificam unicamente um volume de transporte |
| **Slotting** | Decisão de qual endereço recebe cada SKU, com base em giro, peso, cubagem e afinidade de pedido |
| **Task interleaving** | Encadear tarefas para eliminar deslocamento vazio — guardar um palete no caminho de volta de um picking |
| **Cartonization** | Cálculo da menor caixa que comporta os itens do pedido |
| **DIM weight** | Peso cúbico; base de cobrança do frete quando o volume "pesa" mais que a balança indica |
| **Cross-docking** | Mercadoria que vai do recebimento direto à expedição, sem armazenagem |
| **Inventário cíclico** | Contagem recorrente de subconjuntos do estoque, sem parar a operação |
| **FIT / GAP** | Funcionalidade atendida nativamente pelo sistema / lacuna que exige customização ou mudança de processo |
| **Pick-and-pass** | A caixa percorre as zonas e cada operador insere os itens da sua área |

---

*Os indicadores citados são os apresentados na fonte como resultados de operações maduras — trate-os como referência de mercado, não como promessa de projeto. O e-book original traz ainda simuladores interativos e quizzes por módulo.*
