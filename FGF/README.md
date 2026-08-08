# Personalização FGF - Contatos

## 📋 Documentação

- **[ESCOPO.md](ESCOPO.md)** - Documento completo do escopo proposto
- **[VALIDACAO_ESCOPO.md](VALIDACAO_ESCOPO.md)** - Validação de atendimento ao escopo

## Descrição

Personalização para inclusão automática de registros na tabela `TGFCTT` (Contatos do Parceiro) a partir da lista de dados informada através do parâmetro `CONTATOS` (campo de texto extenso).

## Funcionalidade

O botão de ação processa a lista de e-mails do parceiro (separados por `;`), valida cada e-mail e cria registros individuais na tabela `TGFCTT` com as seguintes características:

- **CODCONTATO**: Gerado automaticamente pelo sequencial da tabela
- **NOMECONTATO**: Preenchido com o próprio e-mail
- **EMAIL**: E-mail da lista
- **APELIDO**: Preenchido com o próprio e-mail
- **RECEBEBOLETOEMAIL**: Definido como "S" (Sim)
- **ATIVO**: Definido como "S" (Sim)

## Estrutura do Projeto

```
FGF/
├── src/
│   └── br/com/fgf/
│       ├── action/
│       │   └── botaoAcao/
│       │       └── IncluirContatos.java
│       ├── repository/
│       │   └── ContatoRepository.java
│       ├── service/
│       │   └── ContatoService.java
│       └── util/
│           └── Constants.java
├── pom.xml
├── README.md
├── ESCOPO.md
└── VALIDACAO_ESCOPO.md
```

## Instalação

1. **Compilar o projeto**:
```bash
cd FGF
mvn clean package
```

2. **Copiar JAR gerado**:
   - Localização: `target/personalizacao-fgf-1.0.0.jar`
   - Destino: Diretório de extensões do Sankhya

3. **Configurar Botão de Ação no Sankhya**:
   - **Tela**: Configuração → Rotinas → Botões de Ação
   - **Classe**: `br.com.fgf.action.botaoAcao.IncluirContatos`
   - **Método**: `doAction`
   - **Tabela**: `TGFPAR` (Parceiro)
   - **Nome da Instância**: Contato

## Configuração de Parâmetros

### Parâmetros do Contexto

| Parâmetro | Tipo | Obrigatório | Fonte | Descrição |
|-----------|------|-------------|-------|-----------|
| `CODPARC` | BigDecimal | Sim | Contexto/Parâmetro | Código do parceiro |
| `LIMPAR_CONTATOS` | String | Não | Parâmetro | "S"/"SIM"/"TRUE" para limpar contatos anteriores antes de inserir novos |

**Obtenção do CODPARC**:
- Se o botão for acionado na tela de parceiros com uma linha selecionada, o `CODPARC` será obtido automaticamente da linha selecionada
- Caso contrário, deve ser informado como parâmetro `CODPARC`

## Campo de E-mails

O sistema busca os e-mails do campo `AD_EMAILS` na tabela `TGFPAR`.

**Importante**: Certifique-se de que o campo adicional `AD_EMAILS` existe na tabela `TGFPAR`. Caso o campo tenha outro nome, será necessário ajustar o código no método `buscarEmailsParceiro` da classe `ContatoRepository`.

## Processamento

1. **Extração de E-mails**: A lista de e-mails é extraída do campo `AD_EMAILS` do parceiro
2. **Divisão**: Os e-mails são separados pelo caractere `;`
3. **Validação**: Cada e-mail é validado com expressão regular de e-mail
4. **Verificação de Duplicidade**: Por padrão, e-mails já existentes são ignorados
5. **Criação**: Para cada e-mail válido e não duplicado, é criado um registro em `TGFCTT`

## Comportamento

### Comportamento Padrão
- E-mails já existentes na `TGFCTT` são ignorados
- Apenas e-mails válidos são processados
- E-mails vazios ou inválidos são ignorados

### Com `LIMPAR_CONTATOS = "S"`
- Todos os contatos do parceiro podem ser substituídos (dependendo da lógica implementada)
- Novos contatos são criados normalmente

## Exemplo de Uso

### Exemplo 1: Botão na Tela de Parceiros

1. Acessar a tela de parceiros (TGFPAR)
2. Selecionar um parceiro que possui e-mails configurados
3. Clicar no botão "Incluir Contatos"
4. O sistema processa os e-mails e cria os contatos automaticamente

**E-mails do parceiro**: `email1@exemplo.com;email2@exemplo.com;email3@exemplo.com`

**Resultado**: 3 registros criados na `TGFCTT` (um para cada e-mail)

### Exemplo 2: Com Parâmetro CODPARC

Se o botão for configurado em outra tela, informar o `CODPARC` como parâmetro:

```
CODPARC = 1234
```

### Exemplo 3: Limpar Contatos Antigos

Para limpar contatos anteriores antes de criar novos:

```
CODPARC = 1234
LIMPAR_CONTATOS = S
```

## Validações

- **CODPARC**: Deve ser informado e válido
- **E-mails**: Devem estar em formato válido (regex de validação)
- **Duplicidade**: Por padrão, evita criar contatos duplicados

## Tratamento de Erros

- Se `CODPARC` não for informado, exibe mensagem de erro
- Se não houver e-mails configurados, informa que nenhum e-mail foi encontrado
- Se nenhum e-mail válido for encontrado, informa que todos já existem ou são inválidos
- Erros individuais na criação de contatos são registrados, mas não interrompem o processamento dos demais

## Mensagens de Retorno

O sistema retorna mensagens informativas sobre o processamento:

- **Sucesso**: "Processamento concluído: X contato(s) criado(s), Y ignorado(s)."
- **Sem e-mails**: "Nenhum e-mail encontrado para o parceiro."
- **Sem e-mails válidos**: "Nenhum e-mail válido encontrado ou todos já existem."
- **Erro**: "Erro ao processar contatos: [detalhes do erro]"

## Notas Técnicas

- Utiliza `JapeWrapper` para criação de registros na `TGFCTT`
- Utiliza `EntityFacade` e `DynamicVO` para consultas
- Sequencial obtido da tabela `TGFNUM` onde `ARQUIVO = 'TGFCTT'`
- Validação de e-mail com regex: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$`
- **100% DynamicVO**: Não utiliza INSERT/UPDATE diretos, apenas APIs de alto nível

## Dependências

- JDK 8
- Sankhya W Framework
- Jape (JapeWrapper, JapeFactory)
- EntityFacade (DynamicVO, FinderWrapper)

## Autor

Desenvolvido conforme especificação FGF.

## Versão

1.0.0
