# Escopo Proposto FGF

## Objetivo

Realizar a inclusão automática de registros na tabela `TGFCTT` (Contatos do Parceiro) a partir da lista de dados de contatos informada através do parâmetro `CONTATOS`.

Cada linha da lista utiliza o caractere `;` como separador entre os campos. Cada linha deverá ser tratada de forma individual, gerando um registro distinto na `TGFCTT`.

---

## Regras e Detalhamentos da Implementação

### 1. Origem dos dados

A lista de contatos é informada através do parâmetro `CONTATOS` (campo de texto extenso/textarea) do botão de ação. Cada linha representa um contato a ser cadastrado.

**Formato esperado por linha:**
- `email@exemplo.com` (apenas e-mail - o nome será o próprio e-mail)
- `email@exemplo.com;Nome do Contato` (e-mail e nome separados por `;`)

### 2. Processamento

- A lista é processada **linha a linha**.
- Cada linha deve conter o separador `;` para separar os campos.
- Para cada linha válida, criar um novo registro na `TGFCTT`.
- Validação de formato de e-mail obrigatória.
- Linhas vazias são ignoradas.

### 3. Campos obrigatórios para cada novo contato

| Campo | Descrição | Valor |
|-------|-----------|-------|
| **CODCONTATO** | Código do contato | Gerado automaticamente conforme sequencial padrão da tabela (`TGFNUM` onde `ARQUIVO = 'TGFCTT'`) |
| **NOMECONTATO** | Nome do contato | Preencher com o nome informado na linha, ou com o próprio e-mail se não informado |
| **EMAIL** | E-mail do contato | Preencher com o e-mail da primeira coluna da linha |
| **APELIDO** | Apelido do contato | Preencher com o mesmo valor de `NOMECONTATO` |
| **RECEBEBOLETOEMAIL** | Recebe boleto por e-mail | Definido como `"S"` (Sim) |
| **ATIVO** | Contato ativo | Definido como `"S"` (Sim) |
| **CODPARC** | Código do parceiro | Obtido do parâmetro `CODPARC` |

### 4. Tratamento adicional

- ✅ **Verificação de duplicidade**: Sistema verifica se o e-mail já existe para o parceiro antes de criar novo registro. E-mails duplicados são ignorados.
- ✅ **Validação de formato**: Apenas e-mails válidos são processados.
- ✅ **Validação de separador**: Cada linha deve conter o separador `;`. Linhas sem separador são ignoradas.
- ⚠️ **Limpar contatos anteriores**: Funcionalidade não implementada (conforme escopo original era opcional).

---

## Parâmetros do Botão de Ação

### Parâmetros Obrigatórios

| Parâmetro | Tipo | Descrição | Fonte |
|-----------|------|-----------|-------|
| `CODPARC` | BigDecimal | Código do parceiro | Contexto da linha selecionada ou parâmetro do botão |
| `CONTATOS` | String | Lista de contatos em formato texto (textarea) | Parâmetro do botão (campo de texto extenso) |

### Obtenção do CODPARC

- Se o botão for acionado na tela de parceiros (`TGFPAR`) com uma linha selecionada, o `CODPARC` será obtido automaticamente da linha selecionada.
- Caso contrário, deve ser informado explicitamente como parâmetro `CODPARC`.

---

## Exemplo de Uso

### Entrada (Campo CONTATOS):

```
email1@exemplo.com;João Silva
email2@exemplo.com;Maria Santos
email3@exemplo.com
email4@exemplo.com;Pedro Oliveira
```

### Resultado Esperado:

1. **Linha 1**: Criado contato com e-mail `email1@exemplo.com` e nome "João Silva"
2. **Linha 2**: Criado contato com e-mail `email2@exemplo.com` e nome "Maria Santos"
3. **Linha 3**: Criado contato com e-mail `email3@exemplo.com` e nome "email3@exemplo.com" (usa o e-mail como nome)
4. **Linha 4**: Criado contato com e-mail `email4@exemplo.com` e nome "Pedro Oliveira"

Todos os contatos criados terão:
- `RECEBEBOLETOEMAIL = "S"`
- `ATIVO = "S"`
- `CODPARC` = valor informado

---

## Resultado Esperado

Ao final da execução, todos os e-mails informados no campo `CONTATOS` estarão cadastrados individualmente na `TGFCTT` como contatos válidos, aptos a receber boletos por e-mail.

O sistema retorna mensagem informativa com:
- Quantidade de contatos criados com sucesso
- Quantidade de linhas ignoradas (com motivo)
- Detalhamento de erros, se houver

---

## Validações Implementadas

✅ Validação de `CODPARC` obrigatório  
✅ Validação de `CONTATOS` obrigatório  
✅ Validação de separador `;` presente em cada linha  
✅ Validação de formato de e-mail (regex)  
✅ Verificação de duplicidade de e-mail  
✅ Validação de campos não vazios  

---

## Tecnologias Utilizadas

- **JDK 8**: Streams, Optional, Lambdas
- **100% DynamicVO**: Utiliza apenas APIs de alto nível do Sankhya
  - `JapeWrapper` para operações CRUD
  - `EntityFacade` para consultas
  - `FinderWrapper` para buscas com critérios
  - **Não utiliza INSERT/UPDATE diretos via SQL**

---

## Notas Técnicas

- Sequencial obtido da tabela `TGFNUM` onde `ARQUIVO = 'TGFCTT'`
- Validação de e-mail com regex: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$`
- Processamento linha a linha com tratamento de erro individual
- Mensagens de retorno detalhadas por linha processada

---

**Versão do Documento**: 1.0  
**Data**: Dezembro 2024
