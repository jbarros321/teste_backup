# E-mail de Entrega - Personalização P&D

---

**Assunto:** Entrega da Personalização de Orçamento - P&D

---

Prezado Rogério,

Espero que este e-mail o encontre bem.

Venho por meio deste formalizar a entrega da personalização de **Cadastro de Cargos e Precificação de Mão de Obra**, desenvolvida conforme o pré-escopo documentado no arquivo "P&D - Pré-Escopo de personalização Orçamento docx pdf-D4Sign.pdf".

## Resumo da Entrega

A personalização implementada contempla as seguintes funcionalidades principais:

### 1. Cadastro de Cargos (`AD_CARGOS`)
- Tela personalizada para cadastro de cargos, níveis (Júnior, Pleno, Sênior) e valores por unidade
- Suporte a unidade padrão (diárias) e unidade alternativa (horas)
- Integração com a Central de Vendas para exibição de descrição do cargo + nível nos itens

### 2. Precificação Dinâmica
- **Procedure `STP_PRECO_DINAMICO`**: Calcula preços considerando:
  - Preço base da tabela ou valor do cargo cadastrado
  - Margem de lucro diferenciada para produtos e serviços
  - Fator de custos indiretos para produtos
  - Fator de margem de lucro para serviços com cargo
  - Acréscimo por prazo de pagamento (2% a cada 30 dias)
  - Carga tributária

### 3. Validação de Margem de Lucro Mínima
- **Procedure `STP_MARGEM_LUCRO_MINIMA`**: Valida margens mínimas configuráveis separadamente para produtos e serviços
- Regra de negócio configurada para bloqueio automático quando margem estiver abaixo do mínimo
- Campos AD no cabeçalho da nota para exibição de margens e composição percentual

### 4. Consolidação de Itens de Nota
- **Botão de Ação Java `ConsolidarItensNota`**: Consolida múltiplos itens em um único item de serviço
- Aplicação de margens diferenciadas para mão de obra e material
- Geração automática de texto de composição percentual
- Integração com APIs nativas do Sankhya para garantir aplicação de todas as regras de negócio

### 5. Triggers de Cálculo Automático
- **Trigger `TRG_INC_UPD_TGFITE_REGRA_PD`**: Calcula automaticamente preços e totais dos itens conforme fórmula da planilha de custos
- Validações de unidade e conversão automática entre unidades padrão e alternativas
- Ajuste de quantidade para itens com unidade DI (dias) baseado em `AD_DIASTRAB`

## Arquivos Entregues

Todos os artefatos estão versionados no repositório Git na pasta `P&D/`:

### Scripts SQL
- `STP_PRECO_DINAMICO.SQL` - Procedure de cálculo de preço dinâmico
- `STP_MARGEM_LUCRO_MINIMA.SQL` - Procedure de validação de margem mínima
- `TRG_INC_UPD_TGFITE_REGRA_PD.SQL` - Trigger de cálculo automático de preços

### Código Java
- `src/br/com/pd/action/botaoAcao/ConsolidarItensNota.java` - Botão de ação para consolidação de itens
- JAR compilado: `target/consolidar-itens-nota-1.0.0.jar`

### Documentação
- `docs/Documentacao_Tecnica.md` - Documentação técnica completa
- `docs/Documentacao_Tecnica.pdf` - Versão em PDF
- `docs/Documentacao_Tecnica.html` - Versão em HTML
- `README.md` - Guia de instalação e uso do botão de ação
- `docs/imagens/` - Capturas de tela das telas personalizadas e configurações

### Metadados
- `docs/Metadados_AD_CARGOS.zip` - Metadados exportados da tela `AD_CARGOS` para importação em outros ambientes

### Vídeos Demonstrativos
- Cadastro de Cargos
- Cadastro de Serviços
- Cadastro de Tipos de Operação (TOP)
- Faturamento Consolidado
- Limite de Margem de Regra de Negócio
- Parâmetros do Sistema
- Precificação Dinâmica
- Validação Preço Total de Produtos e Serviços

## Parâmetros do Sistema Configurados

Os seguintes parâmetros devem ser configurados no sistema:

| Parâmetro | Localização | Valor Padrão | Função |
|-----------|-------------|--------------|--------|
| `NOMPROCCALCPRE` | Preferências > Comercial > Diversas | `STP_PRECO_DINAMICO` | Define a procedure de preço dinâmico |
| `MINDIASCODPRAZO` | Preferências > Financeiro > Diversas | `2` | Dias mínimos para acréscimo de prazo |
| `FATORCUSINDERET` | Preferências > Comercial > Preços Alternativos | `0,8285` | Fator de custos indiretos para produtos |
| `FATORMARGLUCSER` | Preferências > Comercial > Preços Alternativos | `2,00` | Fator de margem de lucro para serviços |
| `MARGLUCROSERV` | Preferências > Comercial > Preços Alternativos > Diversas | Configurável | Margem mínima para serviços |
| `MARGLUCROPROD` | Preferências > Comercial > Preços Alternativos > Diversas | Configurável | Margem mínima para produtos |

## Campos Personalizados (AD) Criados

### TGFCAB (Cabeçalho de Notas)
- `AD_MARGLUCPROD` - Margem de Lucro Produtos
- `AD_MARGLUCSERV` - Margem de Lucro Serviços
- `AD_COMPOSICAO` - Composição percentual de mão de obra e material
- `AD_DIASTRAB` - Dias trabalhados
- `AD_TOTALPROD`, `AD_TOTALSERV`, `AD_TOTALPROP` - Totais calculados
- `AD_LUCRLIQPROD`, `AD_LUCRLIQSERV`, `AD_LUCRLIQPROP` - Lucros líquidos calculados

### TGFTOP (Tipos de Operação)
- `AD_AGRUPATDITENS` - Flag para agrupamento de itens
- `AD_SERVEMPREITADA` - Código do produto de serviço consolidado

### TGFITE (Itens)
- `AD_NUCARGO` - Número do cargo selecionado

### TGFPRO (Produtos)
- `AD_PRECOCARGO` - Flag para uso de preço por cargo
- `AD_CARGATRIBUTARIA` - Carga tributária percentual

## Observações Importantes

1. **Botão de Ação**: O botão de ação `ConsolidarItensNota` substituiu a trigger `TRG_INC_UPD_TGFCAB_REGRA_PD` para proporcionar maior controle e transparência ao usuário. A trigger antiga deve ser removida antes de configurar o botão de ação.

2. **Lógica de Diferenciação**: Todos os objetos utilizam o campo `USOPROD` da tabela `TGFPRO` para diferenciar produtos (`USOPROD <> 'S'`) de serviços (`USOPROD = 'S'`), garantindo consistência em todo o sistema.

3. **Fórmulas da Planilha**: Todos os cálculos seguem exatamente as fórmulas da planilha de custos fornecida, garantindo alinhamento com os requisitos de negócio.

4. **APIs Nativas**: O botão de ação utiliza APIs nativas do Sankhya (CACHelper), garantindo que todas as regras de negócio, validações e cálculos automáticos sejam aplicados corretamente.

## Próximos Passos Recomendados

1. **Testes em Homologação**: Recomendamos realizar testes completos em ambiente de homologação antes de implantar em produção.

2. **Configuração de Parâmetros**: Verificar e ajustar os parâmetros do sistema conforme necessidade do negócio.

3. **Cadastro de Cargos**: Realizar o cadastro inicial dos cargos, níveis e valores conforme necessidade.

4. **Configuração de TOPs**: Configurar as TOPs que utilizarão agrupamento de itens (`AD_AGRUPATDITENS = 'S'`).

5. **Treinamento**: Realizar treinamento com os usuários sobre as novas funcionalidades, especialmente:
   - Cadastro de cargos
   - Uso do botão de consolidação de itens
   - Configuração de margens no cabeçalho da nota

## Suporte

Em caso de dúvidas ou necessidade de suporte, estou à disposição para esclarecimentos adicionais.

A documentação técnica completa está disponível em `P&D/docs/Documentacao_Tecnica.md` e contém todos os detalhes de implementação, configuração e uso das funcionalidades.

Agradeço pela confiança e parceria neste projeto.

Atenciosamente,

[Seu Nome]  
[Seu Cargo]  
[Contato]

---

**Anexos:**
- Documentação Técnica (PDF)
- Metadados da tela AD_CARGOS (ZIP)
- JAR do botão de ação (se necessário)

