# Resumo da Estrutura do Projeto NovoMundo

## 📦 Visão Geral do Projeto

O projeto **NovoMundo** é uma extensão para o sistema **Sankhya** que implementa funcionalidades para importação de arquivos e processamento de NFS-e (Notas Fiscais de Serviços Eletrônicas) do Hotel Mega Modas.

## 🗂️ Estrutura de Diretórios

```
NovoMundo/
├── datadictionary/          # Metadados das tabelas do sistema de importação
│   └── metadata.xml
├── dbscripts/               # Scripts SQL (Procedures e DDL)
│   ├── oracle.sql          # Scripts DDL para Oracle
│   ├── mssqlserver.sql     # Scripts DDL para SQL Server
│   ├── PROC_IMP_XMLNFSEHMM_MARTINS.sql        # Importação de XMLs NFS-e
│   ├── PROC_IMP_XMLNFSEHMM_IM_MARTINS.sql     # Wrapper para importação automática
│   └── PROC_GERA_NFSE_PORTAIS_MARTINS.sql     # Geração de notas no portal
├── docs/                    # Documentação do projeto
│   ├── README.md           # Documentação principal
│   ├── DOCUMENTACAO_TECNICA.md
│   ├── DOCUMENTACAO_AD_XMLNFSEHMMC.md  # Documentação da tela adicional
│   ├── FUNCIONAMENTO_IMPORTACAO_NFSE.md
│   └── Metadados_AD_XMLNFSEHMMC/       # Metadados extraídos
│       └── metadata.xml
├── src/                     # Código-fonte Java (EJBs)
│   ├── me/handz/importacao/
│   └── META-INF/
├── web/                     # Interfaces HTML5/Flex
│   ├── html5/
│   └── flex/
├── extension.xml            # Descritor da extensão
└── pom.xml                  # Configuração Maven
```

## 🎯 Componentes Principais

### 1. Sistema de Importação de Arquivos

**Tabelas:**
- `TIMPORTCONF`: Configuração de telas de importação
- `TIMPORTLOG`: Log de importações

**Procedures:**
- Processamento automático via jobs

**Interfaces:**
- HTML5: Configuração de importação
- Flex: Upload de arquivos

### 2. Tela Adicional AD_XMLNFSEHMMC

**Tabelas:**
- `AD_XMLNFSEHMMC`: Tabela principal (XMLs importados)
- `AD_XMLNFSEHMM`: Tabela filha (Notas extraídas)

**Procedures:**
- `PROC_IMP_XMLNFSEHMM_MARTINS`: Importa XMLs de NFS-e
- `PROC_GERA_NFSE_PORTAIS_MARTINS`: Gera notas no portal
- `PROC_IMP_XMLNFSEHMM_IM_MARTINS`: Wrapper para importação automática

## 🔄 Fluxos de Processamento

### Fluxo 1: Importação Manual de XML

```
Usuário → AD_XMLNFSEHMMC (Upload XML)
         → PROC_IMP_XMLNFSEHMM_MARTINS
         → AD_XMLNFSEHMM (Notas extraídas)
```

### Fluxo 2: Importação Automática

```
Sistema de Importação → TIMPORTCONF (Config)
                      → Arquivo XML no diretório
                      → PROC_IMP_XMLNFSEHMM_IM_MARTINS
                      → PROC_IMP_XMLNFSEHMM_MARTINS
                      → AD_XMLNFSEHMM
```

### Fluxo 3: Geração de Notas

```
AD_XMLNFSEHMM → PROC_GERA_NFSE_PORTAIS_MARTINS
              → TGFCAB, TGFITE, TGFIMN, TGFFIN
              → Notas geradas no sistema
```

## 📋 Procedures Principais

| Procedure | Descrição | Localização | Parâmetros |
|-----------|-----------|-------------|------------|
| `PROC_IMP_XMLNFSEHMM_MARTINS` | Importa XMLs de NFS-e | AD_XMLNFSEHMMC | p_codusu, p_idsessao, p_qtdlinhas, p_mensagem, p_imp_xml |
| `PROC_GERA_NFSE_PORTAIS_MARTINS` | Gera notas no portal | AD_XMLNFSEHMM | p_codusu, p_idsessao, p_qtdlinhas, p_mensagem (OUT) |
| `PROC_IMP_XMLNFSEHMM_IM_MARTINS` | Wrapper para importação automática | TIMPORTCONF | p_codusu, p_idsessao, p_qtdlinhas, p_mensagem (OUT) |

## 🔧 Configurações Necessárias

### Parâmetro de Relatório 3

Configurado em: Centro > Telas Adicionais > Parâmetro de Relatórios Personalizados > Parâmetro 3

**Tabelas relacionadas:**
- AD_RELPARMTOP: Tipo de Operação
- AD_RELPARMNAT: Natureza
- AD_RELPARMCUS: Centro de Resultado
- AD_RELPARMSERV: Serviço/Produto
- AD_RELPARMTPV: Tipo de Negociação
- AD_RELPARMTIT: Tipos de Título (formas de pagamento)

### TIMPORTCONF

Para importação automática, configurar:
- IDTELA: Identificador único
- STPFINAL: `PROC_IMP_XMLNFSEHMM_IM_MARTINS`
- CAMPOARQUIVO: `XML`
- CAMPONOME: Campo para nome do arquivo

## 📊 Estrutura de Dados

### AD_XMLNFSEHMMC (Principal)

Armazena os XMLs importados.

**Campos principais:**
- SEQUENCIA (PK)
- XML (CLOB)
- DHREGISTRO
- CODUSU
- OBSERVACAO

### AD_XMLNFSEHMM (Filha)

Armazena as notas fiscais extraídas dos XMLs.

**Campos principais:**
- NUMERO, NUMERORPS, SEQUENCIA (PK composta)
- Dados da nota (valores, datas, etc.)
- Dados do prestador e tomador
- STATUS, OBS PROC, NUNOTA, CODPARC, CODEMP

## 🎨 Formatos de XML Suportados

### Formato Nacional
- Estrutura: GerarNfseResponse → GerarNfseResposta → ListaNfse
- Detecção: Presença de `GERARNFSERESPONSE` ou `TOMADORSERVICO`

### Formato Regional (Goiânia)
- Estrutura: GerarNfseResposta → ListaNfse
- Detecção: Ausência dos marcadores do formato nacional

## 🛠️ Tecnologias Utilizadas

- **Java 8+**
- **EJB 3.0**
- **Oracle PL/SQL**
- **Sankhya Core 3.17+**
- **HTML5/Flex** (Interfaces)

## 📚 Documentação Disponível

1. **README.md**: Visão geral do projeto
2. **DOCUMENTACAO_TECNICA.md**: Detalhes técnicos da arquitetura
3. **DOCUMENTACAO_AD_XMLNFSEHMMC.md**: Documentação completa da tela adicional
4. **FUNCIONAMENTO_IMPORTACAO_NFSE.md**: Fluxos de importação
5. **Metadados_AD_XMLNFSEHMMC/**: Metadados extraídos do sistema

## ✅ Status das Procedures

Todas as procedures foram revisadas e corrigidas:
- ✅ Sintaxe Oracle corrigida
- ✅ Compatibilidade com DBeaver verificada
- ✅ Tratamento de erros melhorado
- ✅ Documentação atualizada
- ✅ Conformidade com metadados verificada

---

**Última Atualização:** 01/11/2025

