# Integração SERPA - WMS TSL

## **Objetivo:** 
Personalização para integração entre Sankhya e WMS TSL da Serpa, permitindo exportação e importação de arquivos TXT conforme padrão estabelecido no documento "Interfaces Padrões WMS - TSL V1.2".

## **Data:** Janeiro 2025
## **Versão:** 1.0.0
## **Contato:** Paulo da Serpa

---

## **VISÃO GERAL**

### **O que é esta Personalização**
Esta personalização fornece funcionalidades de integração entre o sistema Sankhya e o WMS TSL da Serpa através de:

- **Botão de Ação - Exportar TXT**: Exporta dados do Sankhya para arquivo TXT formatado conforme padrão WMS TSL
- **Botão de Ação - Importar TXT**: Importa dados de arquivo TXT formatado para o Sankhya

### **Padrão de Arquivo**
Os arquivos TXT seguem o padrão especificado no documento:
- **Documento de Referência:** `/run/media/lemoreira/BACKUP/Serpa/Interfaces Padrões WMS - TSL V1.2 (1).pdf`
- **Formato:** Texto delimitado (ajustar conforme documento)
- **Codificação:** UTF-8 ou conforme especificado

---

## **ESTRUTURA DO PROJETO**

```
SERPA/
├── docs/                           # Documentação
│   └── README.md                   # Este arquivo
├── src/                            # Código fonte
│   └── br/com/serpa/
│       ├── action/
│       │   └── botaoAcao/
│       │       ├── ExportarArquivoTXT.java    # Botão exportação
│       │       └── ImportarArquivoTXT.java    # Botão importação
│       └── shared/
│           └── SerpaTXTHelper.java            # Helper de processamento
├── pom.xml                         # Configuração Maven
└── target/                         # Arquivos compilados
```

---

## **FUNCIONALIDADES**

### **1. Exportar Arquivo TXT**

#### **Descrição**
Botão de ação que exporta dados selecionados do Sankhya para um arquivo TXT formatado conforme padrão WMS TSL.

#### **Características**
- Exporta registros selecionados na tela
- Formata dados conforme padrão WMS TSL
- Gera nome de arquivo com timestamp
- Salva em diretório configurável
- Registra log das operações

#### **Uso**
1. Selecionar registros na tela do Sankhya
2. Clicar no botão de ação "Exportar TXT"
3. Arquivo será gerado e salvado
4. Mensagem de sucesso exibida com localização do arquivo

#### **Configuração do Botão**
- **Tipo:** Rotina Java (Class)
- **Classe:** `br.com.serpa.action.botaoAcao.ExportarArquivoTXT`
- **Método:** `doAction`

---

### **2. Importar Arquivo TXT**

#### **Descrição**
Botão de ação que importa dados de um arquivo TXT formatado conforme padrão WMS TSL para o Sankhya.

#### **Características**
- Lê arquivo TXT do caminho especificado
- Processa linha por linha
- Valida formato e campos obrigatórios
- Importa dados para o banco
- Move arquivo processado para backup
- Registra log das operações

#### **Uso**
1. Preparar arquivo TXT conforme padrão
2. Clicar no botão de ação "Importar TXT"
3. Informar caminho do arquivo (via parâmetro ou interface)
4. Arquivo será processado
5. Resultado exibido com sucessos e erros

#### **Configuração do Botão**
- **Tipo:** Rotina Java (Class)
- **Classe:** `br.com.serpa.action.botaoAcao.ImportarArquivoTXT`
- **Método:** `doAction`
- **Parâmetros:** 
  - `CAMINHO_ARQUIVO`: Caminho completo do arquivo TXT a ser importado

---

## **COMPONENTES TÉCNICOS**

### **Classes Principais**

#### **ExportarArquivoTXT**
Classe que implementa `AcaoRotinaJava` para exportação de dados.

**Métodos principais:**
- `doAction()`: Método principal executado pelo botão
- `processarExportacao()`: Processa registros e gera arquivo
- `formatarLinhaTXT()`: Formata linha conforme padrão (chamado via Helper)

#### **ImportarArquivoTXT**
Classe que implementa `AcaoRotinaJava` para importação de dados.

**Métodos principais:**
- `doAction()`: Método principal executado pelo botão
- `processarImportacao()`: Lê e processa arquivo linha por linha
- `obterCaminhoArquivo()`: Obtém caminho via parâmetro

#### **SerpaTXTHelper**
Classe auxiliar com métodos utilitários.

**Métodos principais:**
- `formatarLinhaTXT()`: Formata registro para linha TXT
- `processarLinhaTXT()`: Processa linha TXT na importação
- `formatarCampo()`: Formata campo com padding
- `formatarData()`: Formata data no padrão esperado
- `registrarLog()`: Registra operações na tabela de log

---

## **CONFIGURAÇÃO**

### **Tabela de Log**
A personalização cria automaticamente a tabela `AD_SERPALOG` para registro de operações:

```sql
CREATE TABLE AD_SERPALOG (
    DTLOG DATE DEFAULT SYSDATE,
    TIPO VARCHAR2(50),        -- EXPORTACAO ou IMPORTACAO
    STATUS VARCHAR2(20),      -- SUCESSO ou ERRO
    MENSAGEM VARCHAR2(4000),
    USUARIO VARCHAR2(100),
    SEQUENCIA NUMBER
);
```

### **Diretórios**
- **Exportação:** `{tempdir}/serpa_export/`
- **Importação Backup:** `{tempdir}/serpa_import/backup/`

**Nota:** Estes caminhos podem ser configurados conforme necessidade.

---

## **PENDÊNCIAS E AJUSTES NECESSÁRIOS**

### **⚠️ IMPORTANTE - Ajustes Obrigatórios**

1. **Formato do Arquivo TXT**
   - ⚠️ Revisar documento PDF "Interfaces Padrões WMS - TSL V1.2"
   - ⚠️ Implementar formatação exata conforme especificação
   - ⚠️ Ajustar separadores, tamanhos de campo, formato de data
   - ⚠️ Definir campos obrigatórios e opcionais

2. **Mapeamento de Campos**
   - ⚠️ Definir quais campos do Sankhya são exportados
   - ⚠️ Definir quais campos do TXT são importados
   - ⚠️ Mapear tabelas e colunas específicas

3. **Tabela de Destino/Origem**
   - ⚠️ Definir tabela(s) do Sankhya envolvidas
   - ⚠️ Ajustar queries e operações de banco de dados

4. **Validações**
   - ⚠️ Implementar validações de negócio específicas
   - ⚠️ Definir regras de importação/exportação

5. **Caminho de Arquivos**
   - ⚠️ Configurar caminhos definitivos (não usar temp)
   - ⚠️ Definir estrutura de diretórios de produção

---

## **COMPILAÇÃO E INSTALAÇÃO**

### **Pré-requisitos**
- Java 8
- Maven 3.x
- Sankhya com dependências locais instaladas

### **Compilar**
```bash
cd SERPA
mvn clean compile
```

### **Gerar JAR**
```bash
mvn package
```

O arquivo JAR será gerado em: `target/integracao-serpa-1.0.0.jar`

### **Instalação no Sankhya**
1. Copiar JAR para pasta de personalizações do Sankhya
2. Configurar botões de ação conforme documentação acima
3. Testar em ambiente de desenvolvimento primeiro

---

## **TESTES**

### **Teste de Exportação**
1. Selecionar registros válidos
2. Executar botão de exportação
3. Verificar arquivo gerado
4. Validar formato conforme padrão WMS TSL

### **Teste de Importação**
1. Preparar arquivo TXT de teste conforme padrão
2. Executar botão de importação
3. Verificar dados importados no Sankhya
4. Validar logs de operação

---

## **PRÓXIMOS PASSOS**

1. ✅ Estrutura básica criada
2. ⏳ Revisar documento PDF com Paulo da Serpa
3. ⏳ Ajustar formato de arquivo conforme especificação
4. ⏳ Definir mapeamento de campos
5. ⏳ Implementar lógica específica de negócio
6. ⏳ Testes em ambiente de desenvolvimento
7. ⏳ Ajustes finais e validações
8. ⏳ Documentação funcional completa
9. ⏳ Deploy em produção

---

## **CONTATO E SUPORTE**

**Cliente:** Serpa  
**Contato:** Paulo da Serpa  
**Versão:** 1.0.0  
**Data de Criação:** Janeiro 2025  
**Compatibilidade:** Sankhya + Java 8 + Maven

---

## **CHANGELOG**

### **v1.0.0 (Janeiro 2025)**
- Criação da estrutura inicial do projeto
- Implementação básica de exportação TXT
- Implementação básica de importação TXT
- Classe Helper para processamento
- Tabela de log de operações
- Documentação inicial

### **Pendente**
- Ajustes conforme documento "Interfaces Padrões WMS - TSL V1.2"
- Definição de formato exato de arquivo
- Mapeamento de campos específicos
- Validações de negócio

