# Estimativa em horas

Regra dura: **toda estimativa do documento é em horas**. Meses, semanas e sprints
não aparecem como unidade de esforço. Se o insumo está em meses, converta e
registre a premissa de conversão no capítulo 11.

## Método

Bottom-up, sempre. Nunca jogue um número redondo por módulo: decomponha o módulo
em itens, estime cada item pelo catálogo, some, aplique overheads.

```
horas do módulo = Σ horas dos itens
horas da onda   = Σ módulos da onda + migração da onda + treinamento da onda
total           = Σ ondas + GP + homologação
```

Arredonde cada item para a hora cheia e cada módulo para múltiplo de 4 h.

## Catálogo de referência (horas por item)

Faixas para implantação sobre Sankhya. Escolha dentro da faixa pela complexidade
real do item e, quando usar o teto da faixa, diga por quê.

### Configuração / parametrização do padrão
| Item | Horas |
|---|---|
| Cadastro parametrizado simples (domínio, lista controlada) | 2–4 |
| Campos customizados em entidade padrão (por entidade) | 4–8 |
| Configuração de rotina financeira padrão | 8–16 |
| Fluxo de workflow / alçada (por fluxo) | 8–16 |
| Perfil de acesso e matriz de permissão (por perfil) | 2–4 |

### Telas e personalizações
| Item | Horas |
|---|---|
| Tela CRUD simples | 8–16 |
| Tela com regras de validação e abas | 16–32 |
| Grade editável em linha (estilo Excel), com subtotais | 32–56 |
| Tela comparativa / diff entre versões | 32–48 |
| Portal ou tela de consulta externa (somente leitura) | 24–40 |
| Assistente / wizard multi-etapa | 24–40 |

### Regras e motores
| Item | Horas |
|---|---|
| Regra de validação isolada | 2–6 |
| Motor de regras parametrizável (base) | 60–100 |
| Cada família de regra adicional no motor | 6–12 |
| Versionamento de entidade com justificativa por linha | 40–60 |
| Rateio multidimensional com validação de saldo | 40–60 |

### Relatórios e painéis
| Item | Horas |
|---|---|
| Relatório / exportação em layout oficial de órgão | 12–24 |
| Painel BI (por painel) | 12–24 |
| Exportação Excel/PDF de tela existente | 4–8 |

### Integrações
| Item | Horas |
|---|---|
| Importação de arquivo XML/CSV com idempotência | 40–60 |
| Retorno bancário / conciliação automática (por banco) | 24–40 |
| Remessa de pagamentos (CNAB / PIX em lote) | 40–60 |
| Exportação em lote para plataforma de órgão (por órgão) | 16–32 |
| Webhook de entrada (por origem) | 12–20 |
| Consulta a serviço público (CNPJ/CPF) | 8–12 |

### Migração
| Item | Horas |
|---|---|
| Bloco de carga simples (cadastro) | 8–16 |
| Bloco de carga com transformação / deduplicação | 16–32 |
| Bloco de carga financeira com conciliação de saldo | 24–40 |

## Overheads (aplicar por cima da soma dos itens)

| Overhead | Percentual | Observação |
|---|---|---|
| Gestão de projeto | 15% | sobre o total de execução |
| Homologação e ajustes de aceite | 10% | sobre a onda |
| Treinamento | 8 h por onda + 4 h por perfil | presencial ou remoto |
| Documentação de entrega | 4% | manuais e parametrização |
| Contingência técnica | 10% | só quando houver integração de terceiro ou migração financeira |

Declare no capítulo 11 quais overheads foram aplicados e com que percentual.

## Conversão para prazo (somente se o usuário pedir)

Premissa default: **160 h/mês por frente de trabalho**. O prazo é informação
derivada e vem sempre depois das horas:

> Onda 1 — 480 horas (≈ 3 meses a 160 h/mês, uma frente)

Se houver frentes paralelas, diga quantas; horas totais não mudam, prazo sim.

## Formato das tabelas no documento

Capítulo 5 — por módulo:

| Módulo | Entrega de negócio | Horas |
|---|---|---|
| M01 · Fundação | Cada lei entra por cadastro, sem desenvolvimento | 120 |

Capítulo 6 — por natureza (horas antes do percentual):

| Natureza | Horas | % | Onde se concentra |
|---|---|---|---|
| Aproveitamento do padrão | 1.010 | 45% | Financeiro, parceiros, fiscal, workflow, BI |

Capítulo 7 — por onda:

| Onda | Horas | Conteúdo | Entregável de negócio |
|---|---|---|---|
| 1 · Fundação e controle financeiro | 480 | ... | A equipe para de digitar pagamento em planilha |

As três tabelas precisam fechar no mesmo total. Confira a soma antes de entregar;
divergência entre capítulos invalida o documento comercial.
