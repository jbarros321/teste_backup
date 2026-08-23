# Dashboard Financeiro - CICRANO

## 📋 Descrição

Dashboard financeiro completo para visualização de:
- **Fluxo de Caixa Real**: Receitas e despesas já baixadas (DHBAIXA IS NOT NULL)
- **Provisão de Receita**: Títulos a receber provisionados (RECDESP = 1, PROVISAO = 'S')
- **Provisão de Despesas**: Títulos a pagar provisionados (RECDESP = -1, PROVISAO = 'S')

## 🎯 Objetivo

Fornecer uma visão consolidada e em tempo real da situação financeira através de gráficos interativos e tabelas detalhadas, utilizando dados nativos da tabela TGFFIN do Sankhya.

## 🏗️ Estrutura do Projeto

```
Dash/
├── src/
│   └── br/com/cicrano/dash/
│       ├── dto/
│       │   ├── FluxoCaixaDTO.java
│       │   └── ProvisaoDTO.java
│       ├── repository/
│       │   └── FinanceiroRepository.java
│       └── servlet/
│           └── DashboardServlet.java
├── web/
│   └── dashboard.jsp
├── lib/                          # Dependências Sankhya (criar manualmente)
│   ├── SankhyaW-extensions.jar
│   ├── jape.jar
│   ├── mge-modelcore.jar
│   ├── sanutil.jar
│   └── sanws.jar
├── pom.xml
└── README.md
```

## 📦 Dependências

### ⚠️ IMPORTANTE: JARs Sankhya Necessários

**Antes de compilar**, você precisa criar a pasta `lib/` e adicionar os seguintes JARs:

```bash
mkdir -p lib
```

Os seguintes JARs devem estar na pasta `lib/`:
- `SankhyaW-extensions.jar` - Extensões do Sankhya
- `jape.jar` - Java Persistence API do Sankhya
- `mge-modelcore.jar` - Core do modelo Sankhya
- `sanutil.jar` - Utilitários Sankhya
- `sanws.jar` - ServiceContext necessário

**Como obter os JARs:**
1. **SDK Sankhya**: Os JARs estão disponíveis no SDK do Sankhya (pasta `api_sankhya/`)
2. **Copiar de outro projeto**: Se houver outro projeto com estes JARs, copie de lá
3. **Extrair do servidor Sankhya**: Os JARs geralmente estão na pasta de instalação do Sankhya

## 🔧 Instalação

### 1. Compilar o Projeto

```bash
cd Dash
mvn clean package install
```

### 2. Configurar no Sankhya

1. Copie o JAR gerado (`target/personalizacao-dash-1.0.0.jar`) para a pasta de extensões do Sankhya
2. Configure o servlet no `web.xml` do Sankhya:

```xml
<servlet>
    <servlet-name>DashboardServlet</servlet-name>
    <servlet-class>br.com.cicrano.dash.servlet.DashboardServlet</servlet-class>
</servlet>

<servlet-mapping>
    <servlet-name>DashboardServlet</servlet-name>
    <url-pattern>/dash/*</url-pattern>
</servlet-mapping>
```

3. Copie o arquivo `web/dashboard.jsp` para a pasta web do Sankhya (ou configure o caminho correto no servlet)

### 3. Acessar o Dashboard

Acesse via navegador:
```
http://seu-servidor-sankhya:porta/dash/dashboard.jsp
```

## 📊 Funcionalidades

### Fluxo de Caixa Real
- Exibe receitas e despesas já baixadas (com data de baixa)
- Filtra por período selecionado
- Calcula saldo diário e total
- Gráfico de linha mostrando evolução temporal

### Provisão de Receita
- Títulos a receber provisionados (não baixados)
- Agrupados por data de vencimento
- Visualização em gráfico de barras

### Provisão de Despesas
- Títulos a pagar provisionados (não baixados)
- Agrupados por data de vencimento
- Visualização em gráfico de barras

### Filtros
- Data inicial e final para consulta
- Atualização em tempo real dos dados
- Período padrão: último mês

## 🗄️ Consultas SQL

### Fluxo de Caixa Real
```sql
SELECT 
    TRUNC(FIN.DHBAIXA) AS DATA,
    SUM(CASE WHEN FIN.RECDESP = 1 THEN FIN.VLRDESDOB ELSE 0 END) AS RECEITAS,
    SUM(CASE WHEN FIN.RECDESP = -1 THEN FIN.VLRDESDOB ELSE 0 END) AS DESPESAS
FROM TGFFIN FIN
WHERE FIN.DHBAIXA IS NOT NULL
    AND FIN.PROVISAO = 'N'
    AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99)
    AND TRUNC(FIN.DHBAIXA) BETWEEN :DATA_INI AND :DATA_FIM
GROUP BY TRUNC(FIN.DHBAIXA)
ORDER BY TRUNC(FIN.DHBAIXA)
```

### Provisão de Receita
```sql
SELECT 
    TRUNC(FIN.DTVENC) AS DATA,
    SUM(FIN.VLRDESDOB) AS VALOR
FROM TGFFIN FIN
WHERE FIN.RECDESP = 1
    AND FIN.PROVISAO = 'S'
    AND FIN.DHBAIXA IS NULL
    AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99)
    AND TRUNC(FIN.DTVENC) BETWEEN :DATA_INI AND :DATA_FIM
GROUP BY TRUNC(FIN.DTVENC)
ORDER BY TRUNC(FIN.DTVENC)
```

### Provisão de Despesa
```sql
SELECT 
    TRUNC(FIN.DTVENC) AS DATA,
    SUM(FIN.VLRDESDOB) AS VALOR
FROM TGFFIN FIN
WHERE FIN.RECDESP = -1
    AND FIN.PROVISAO = 'S'
    AND FIN.DHBAIXA IS NULL
    AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99)
    AND TRUNC(FIN.DTVENC) BETWEEN :DATA_INI AND :DATA_FIM
GROUP BY TRUNC(FIN.DTVENC)
ORDER BY TRUNC(FIN.DTVENC)
```

## 🎨 Tecnologias Utilizadas

- **Backend**: Java 8, Servlets, NativeSql (Sankhya)
- **Frontend**: HTML5, CSS3, JavaScript (ES6+)
- **Gráficos**: Chart.js 3.9.1
- **Framework**: Sankhya DWF

## 📝 Notas Técnicas

- Todas as consultas seguem os padrões obrigatórios do Sankhya:
  - Filtro `PROVISAO = 'N'` para fluxo real
  - Filtro `PROVISAO = 'S'` para provisões
  - Exclusão de tipos de título específicos: `CODTIPTIT NOT IN (0, 18, 27, 99)`
- Uso de `NativeSql` com parâmetros nomeados (`:PARAM`)
- Tratamento adequado de recursos (JdbcWrapper, NativeSql)
- DTOs com equals/hashCode implementados
- Interface responsiva e moderna

## 🔍 Validações

- Validação de parâmetros de data
- Tratamento de erros com mensagens amigáveis
- Valores nulos tratados como zero
- Formatação monetária em R$ (BRL)

## 📚 Referências

- [Template/REFERENCIA_SANKHYA.md](../Template/REFERENCIA_SANKHYA.md) - Referência completa de tabelas Sankhya
- [Template/INSTRUCOES_DESENVOLVIMENTO.md](../Template/INSTRUCOES_DESENVOLVIMENTO.md) - Instruções de desenvolvimento

## 👤 Cliente

**CICRANO**

## 📅 Versão

**1.0.0** - Versão inicial











