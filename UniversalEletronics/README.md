# Gera Arquivo Remessa - Universal Eletronics

## 📋 Visão Geral

Personalização para geração de arquivo de remessa bancária no sistema Sankhya.

## 🎯 Funcionalidade

Geração de arquivo de remessa bancária através do botão de ação `GerarArquivoRemessa`.

## ⚙️ Especificações Técnicas

- **Java**: JDK 8
- **Build**: Maven 3.x
- **Pacote**: `br.com.universaleletronic`
- **Artefato**: `geraArquivoremessa_Vs1.jar`

## 📁 Estrutura do Projeto

```
UniversalEletronics/
├── src/
│   └── br/com/universaleletronic/
│       └── action/botaoAcao/
│           └── GerarArquivoRemessa.java
├── pom.xml
└── README.md
```

## 🔧 Classe Principal

### `GerarArquivoRemessa.java`

Botão de ação que gera arquivo de remessa bancária.

**Funcionalidades:**
- Gera arquivo de remessa usando `GeracaoRemessaHelper` do Sankhya
- Busca financeiros da tabela `AD_TSIREMITE`
- Atualiza status em `AD_TSIREMCAB`
- Grava histórico em `HistoricoRemessaBancaria`
- Copia arquivo para diretório configurado em `AD_LAYOUTDIR`

**Campos do Registro Utilizados:**
- `CODCTABCOINT` - Conta bancária
- `CODIGO` - Código do layout
- `NROUNICO` - Número único da remessa
- `TIPO` - Tipo (Receita/Despesa)
- `AGRUPAPAGTO` - Agrupar pagamentos (S/N)
- `GERANOSSONRO` - Gerar nosso número (S/N)
- `GERALINHADIG` - Gerar linha digitável (S/N)
- `REGISTRABCOCTA` - Registrar banco conta (S/N)
- `UTILIZACTATITULO` - Utilizar conta título (S/N)

## 🚀 Compilação

```bash
mvn clean package install
```

O artefato gerado será: `target/geraArquivoremessa_Vs1.jar`

---

**Cliente**: Universal Eletronics  
**Versão**: 1.0  
**Status**: ✅ Ativo
