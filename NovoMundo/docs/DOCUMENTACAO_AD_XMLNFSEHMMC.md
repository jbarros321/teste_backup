# Documentação Completa - Tela Adicional AD_XMLNFSEHMMC

## 📋 Visão Geral

A tela adicional **AD_XMLNFSEHMMC** foi criada para importação de XMLs de NFS-e (Nota Fiscal de Serviços Eletrônica) do **Hotel Mega Modas**, suportando tanto o formato **Nacional** quanto o formato **Regional/Goiânia**.

## 🏗️ Estrutura da Tela Adicional

### Tabela Principal: AD_XMLNFSEHMMC

**Descrição:** Importar NFS-e Hotel Mega Modas

**Chave Primária:**
- `SEQUENCIA` (Número - Auto-incremento)

**Campos:**

| Campo | Tipo | Descrição | Obrigatório | Visível |
|-------|------|-----------|-------------|---------|
| `SEQUENCIA` | Integer | Sequência | Sim | Sim |
| `DHREGISTRO` | DateTime | Data Registro (Calculado: `$ctx_dh_atual`) | Não | Sim (ReadOnly) |
| `CODUSU` | Integer | Código do Usuário (Calculado: `$ctx_usuario_logado`) | Não | Sim (ReadOnly) |
| `OBSERVACAO` | String | Observação | Sim | Sim |
| `XML` | CLOB | Conteúdo do XML | Não | Não (ReadOnly) |

**Relacionamentos:**
- `Usuario` (TSIUSU) - Via `CODUSU`
- `AD_XMLNFSEHMM` - Via `SEQUENCIA` (1:N)

### Tabela Filha: AD_XMLNFSEHMM

**Descrição:** NFSe (Notas Fiscais de Serviços importadas)

**Chave Primária Composta:**
- `NUMERO` (Número da NFS-e)
- `NUMERORPS` (Número do RPS)
- `SEQUENCIA` (Referência à tabela pai)

**Principais Campos:**

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `NUMERO` | Integer | Número da NFS-e |
| `NUMERORPS` | Integer | Número do RPS |
| `SEQUENCIA` | Integer | FK para AD_XMLNFSEHMMC |
| `CODIGOVERIFICACAO` | String | Código de verificação |
| `DATAEMISSAO` | DateTime | Data de emissão |
| `BASECALCULO` | Float | Base de cálculo do ISS |
| `ALIQUOTA` | Float | Alíquota do ISS |
| `VALORISS` | Float | Valor do ISS |
| `SERIE` | String | Série da nota |
| `TIPO` | String | Tipo (1-RPS, 2-Nota Mista, 3-Cupom) |
| `VALORSERVICOS` | Float | Valor dos serviços |
| `DEDUCOES` | Float | Valor das deduções |
| `DADOS_PARTICIPANTES` | String | Dados do prestador e tomador |
| `STATUS` | String | Status do processamento (S/N) |
| `OBS PROC` | String | Observações do processamento |
| `NUNOTA` | Integer | Número da nota gerada no sistema |
| `CODPARC` | Integer | Código do parceiro/cliente |
| `CODEMP` | Integer | Código da empresa |

## 🔧 Procedures e Ações

### 1. PROC_IMP_XMLNFSEHMM_MARTINS

**Localização:** Tela AD_XMLNFSEHMMC  
**Tipo:** Botão de Ação  
**Descrição:** Reprocessar XML já importado

**Assinatura (conforme metadata.xml):**
```sql
PROCEDURE PROC_IMP_XMLNFSEHMM_MARTINS (
    p_codusu    number,
    p_idsessao  varchar2,
    p_qtdlinhas number,
    p_mensagem  varchar2,  -- SEM OUT (conforme metadata original)
    p_imp_xml   varchar2 default 'N'
)
```

**Nota:** O parâmetro `p_mensagem` não possui `OUT` declarado, conforme o metadata original. O Sankhya trata este parâmetro de forma especial mesmo sem a declaração explícita de OUT.

**Funcionalidade:**
- Importa notas fiscais de serviço a partir de XMLs armazenados na tabela AD_XMLNFSEHMMC
- Suporta dois formatos de XML:
  - **Formato Nacional:** Com `GerarNfseResponse` e `TomadorServico`
  - **Formato Regional:** Com `GerarNfseResposta` e `Tomador`/`Prestador`
- Valida XML antes do processamento
- Detecta automaticamente o formato do XML
- Permite reprocessamento forçado quando `p_imp_xml = 'S'`
- Extrai dados das notas e insere na tabela AD_XMLNFSEHMM

**Parâmetros:**
- `p_codusu`: Código do usuário logado
- `p_idsessao`: Identificador da execução
- `p_qtdlinhas`: Quantidade de registros selecionados (deve ser 1)
- `p_mensagem`: Mensagem de retorno (sem OUT - conforme metadata original)
- `p_imp_xml`: 'S' para forçar reprocessamento, 'N' para perguntar ao usuário

### 2. PROC_GERA_NFSE_PORTAIS_MARTINS

**Localização:** Tela AD_XMLNFSEHMM  
**Tipo:** Botão de Ação  
**Descrição:** Gerar Notas Portal de Vendas

**Assinatura:**
```sql
PROCEDURE PROC_GERA_NFSE_PORTAIS_MARTINS (
    p_codusu    number,
    p_idsessao  varchar2,
    p_qtdlinhas number,
    p_mensagem  out varchar2
)
```

**Parâmetros de Entrada (Prompt):**
- `REGISTROS`: Tipo SO (Select Option)
  - `SELECIONADO`: Registro Selecionado
  - `TODOS`: Todos os registros da grade
- `CADPARC`: Tipo B (Boolean)
  - Cadastrar Parceiros Faltantes?

**Funcionalidade:**
- Gera notas fiscais de serviço no portal de vendas do Sankhya
- Processa registros da tabela AD_XMLNFSEHMM
- Identifica/cadastra parceiros (clientes) automaticamente
- Identifica empresa prestadora
- Verifica se nota já existe antes de gerar
- Cria cabeçalho (TGFCAB) e itens (TGFITE) da nota
- Processa retenções (PIS, COFINS, INSS, IR, CSLL)
- Gera financeiros (TGFFIN) baseado em pagamentos do XML
- Atualiza status das notas processadas

**Configurações Necessárias:**
A procedure utiliza o parâmetro de relatório número **3** (`v_codfiltro := 3`) para obter:
- Tipo de Operação (AD_RELPARMTOP)
- Natureza (AD_RELPARMNAT)
- Centro de Resultado (AD_RELPARMCUS)
- Serviço (AD_RELPARMSERV)
- Tipo de Negociação (AD_RELPARMTPV)
- Tipos de Título para formas de pagamento (AD_RELPARMTIT)

### 3. PROC_IMP_XMLNFSEHMM_IM_MARTINS

**Localização:** Tela de Importação (TIMPORTCONF)  
**Tipo:** Procedure Intermediária  
**Descrição:** Wrapper para importação via sistema de importação automática

**Assinatura:**
```sql
PROCEDURE PROC_IMP_XMLNFSEHMM_IM_MARTINS (
    p_codusu    number,
    p_idsessao  varchar2,
    p_qtdlinhas number,
    p_mensagem  out varchar2
)
```

**Funcionalidade:**
- Chamada automaticamente pelo sistema de importação de arquivos
- Valida que apenas uma linha seja selecionada
- Chama PROC_IMP_XMLNFSEHMM_MARTINS com `p_imp_xml = 'S'` (força reprocessamento)

## 📊 Fluxo de Processamento

### Fluxo Completo de Importação

```
1. Upload do XML
   └─> TIMPORTCONF (Sistema de Importação)
       └─> AD_XMLNFSEHMMC.XML (Armazenado)
           └─> PROC_IMP_XMLNFSEHMM_IM_MARTINS (Via sistema de importação)
               └─> PROC_IMP_XMLNFSEHMM_MARTINS
                   └─> AD_XMLNFSEHMM (Notas extraídas)
                       └─> PROC_GERA_NFSE_PORTAIS_MARTINS
                           └─> TGFCAB, TGFITE, TGFIMN, TGFFIN (Notas geradas)
```

### Fluxo Detalhado PROC_IMP_XMLNFSEHMM_MARTINS

1. **Validação Inicial:**
   - Verifica parâmetros obrigatórios
   - Valida que apenas 1 linha foi selecionada
   - Obtém SEQUENCIA do registro

2. **Verificação de Registros Existentes:**
   - Verifica se já existem notas importadas para a SEQUENCIA
   - Se `p_imp_xml = 'S'`: Apaga e reprocessa
   - Se `p_imp_xml = 'N'`: Pergunta ao usuário

3. **Validação do XML:**
   - Verifica se XML existe e não está vazio
   - Faz parse do XML para validar estrutura
   - Detecta formato (Nacional ou Regional)

4. **Processamento do XML:**
   - Extrai notas usando XMLTABLE
   - Processa formato Nacional (GerarNfseResponse)
   - Processa formato Regional (GerarNfseResposta)
   - Valida campos obrigatórios (NUMERO, NUMERORPS)

5. **Inserção na Tabela:**
   - Insere notas na AD_XMLNFSEHMM
   - Trata duplicatas (ignora)
   - Trata erros de constraint (registra mas continua)

6. **Finalização:**
   - Atualiza registro principal (AD_XMLNFSEHMMC)
   - Monta mensagem de retorno
   - Retorna quantidade processada

### Fluxo Detalhado PROC_GERA_NFSE_PORTAIS_MARTINS

1. **Validação de Configurações:**
   - Verifica parâmetro 3 (configurações)
   - Valida Tipo de Operação, Natureza, Centro de Resultado, Serviço, Tipo de Negociação

2. **Processamento de Registros:**
   - Para cada registro selecionado:
     a. Identifica/Cadastra parceiro (cliente)
     b. Identifica empresa prestadora
     c. Verifica se nota já existe
     d. Se não existe, gera nova nota

3. **Geração da Nota:**
   - Gera NUNOTA
   - Insere TGFCAB (cabeçalho)
   - Insere TGFITE (item)
   - Processa retenções (TGFIMN)
   - Gera financeiros (TGFFIN) baseado em pagamentos

4. **Atualização:**
   - Atualiza status na AD_XMLNFSEHMM
   - Registra NUNOTA gerado

## 🔍 Formato dos XMLs

### Formato Nacional

**Estrutura:**
```xml
<GerarNfseResponse>
  <GerarNfseResposta>
    <ListaNfse>
      <CompNfse>
        <Nfse>
          <InfNfse>
            <Numero>...</Numero>
            <CodigoVerificacao>...</CodigoVerificacao>
            <DeclaracaoPrestacaoServico>
              <InfDeclaracaoPrestacaoServico>
                <Rps>
                  <IdentificacaoRps>
                    <Numero>...</Numero>
                  </IdentificacaoRps>
                </Rps>
                <Servico>...</Servico>
                <Prestador>...</Prestador>
                <TomadorServico>...</TomadorServico>
              </InfDeclaracaoPrestacaoServico>
            </DeclaracaoPrestacaoServico>
          </InfNfse>
        </Nfse>
      </CompNfse>
    </ListaNfse>
  </GerarNfseResposta>
</GerarNfseResponse>
```

**Detecção:** Presença de `GERARNFSERESPONSE` ou `TOMADORSERVICO`

### Formato Regional

**Estrutura:**
```xml
<GerarNfseResposta>
  <ListaNfse>
    <CompNfse>
      <Nfse>
        <InfNfse>
          <Numero>...</Numero>
          <DeclaracaoPrestacaoServico>
            <IdentificacaoRps>
              <Numero>...</Numero>
            </IdentificacaoRps>
            <Servico>...</Servico>
            <Prestador>...</Prestador>
            <Tomador>...</Tomador>
          </DeclaracaoPrestacaoServico>
        </InfNfse>
      </Nfse>
    </CompNfse>
  </ListaNfse>
</GerarNfseResposta>
```

**Detecção:** Ausência de `GERARNFSERESPONSE` e `TOMADORSERVICO`

## ⚙️ Configurações Necessárias

### Parâmetro de Relatório 3

O parâmetro de relatório número 3 deve estar configurado com:

**Localização:** Centro > Telas Adicionais > Parâmetro de Relatórios Personalizados > Parâmetro 3

**Tabelas de Configuração:**
- `AD_RELPARMTOP`: Tipo de Operação
- `AD_RELPARMNAT`: Natureza
- `AD_RELPARMCUS`: Centro de Resultado
- `AD_RELPARMSERV`: Serviço/Produto
- `AD_RELPARMTPV`: Tipo de Negociação
- `AD_RELPARMTIT`: Tipos de Título (para formas de pagamento)

## 🛠️ Manutenção e Troubleshooting

### Erros Comuns

1. **ORA-00900: Instrução SQL inválida**
   - Verificar se há `/` no final da procedure (DBeaver não precisa)
   - Verificar estrutura BEGIN/END balanceada

2. **PLS-00201: Identificador não declarado**
   - Verificar escopo de variáveis
   - Verificar se variáveis estão declaradas no nível correto

3. **Erro ao processar XML**
   - Verificar formato do XML (Nacional vs Regional)
   - Verificar se XML está bem formado
   - Verificar namespace do XML

4. **Parceiro não identificado**
   - Verificar CPF/CNPJ na tabela TGFPAR
   - Verificar se parceiro está ativo e marcado como cliente

5. **Empresa não identificada**
   - Verificar CNPJ na tabela TSIEMP/TGFEMP
   - Verificar se empresa está ativa

### Validações de Debug

As procedures incluem sistema de debug detalhado:
- Validação de parâmetros
- Validação de XML
- Detecção de formato
- Tratamento de erros com códigos específicos (-20001 a -20099)
- Mensagens detalhadas de erro

## 📝 Notas de Implementação

### Histórico de Versões

- **17/08/2021** (Rodrigo Coutinho): Implementação inicial
- **29/10/2025** (Leandro Marcos Moreira): Migração para formato Nacional
- **31/10/2025** (Leandro Marcos Moreira): Suporte para ambos formatos (Nacional e Regional/Goiânia)
- **31/10/2025** (Leandro Marcos Moreira): Adicionado debug e tratamento de erros detalhado

### Diferenças entre Metadata e Implementação Atual

1. **p_mensagem OUT:**
   - Metadata original: `p_mensagem varchar2` (sem OUT)
   - Implementação atual: `p_mensagem varchar2` (sem OUT)
   - **Status:** ✅ Conforme metadata original. O Sankhya trata este parâmetro de forma especial mesmo sem OUT declarado.

2. **Melhorias de Debug:**
   - Adicionado tratamento detalhado de erros
   - Adicionada validação de XML antes do processamento
   - Adicionada detecção automática de formato

3. **Estrutura de Exception:**
   - Simplificado bloco exception principal
   - Melhorado tratamento de SQLCODE

## 🔗 Integração com Sistema de Importação

A procedure `PROC_IMP_XMLNFSEHMM_IM_MARTINS` é chamada automaticamente pelo sistema de importação de arquivos configurado em `TIMPORTCONF`:

**Configuração em TIMPORTCONF:**
- `IDTELA`: Identificador da tela
- `STPFINAL`: `PROC_IMP_XMLNFSEHMM_IM_MARTINS`
- `CAMPOARQUIVO`: `XML`
- `CAMPONOME`: Campo para nome do arquivo

## 📚 Referências

- Documentação Sankhya - Procedures e Botões de Ação
- Manual NFS-e Nacional - ABRASF
- Manual NFS-e Goiânia - Formato Regional

---

**Última Atualização:** 01/11/2025  
**Versão da Documentação:** 1.0

