# 📊 Dashboards Sankhya - Guia Completo

## 📋 Índice
1. [Estrutura de Componentes XML](#estrutura-de-componentes-xml)
2. [Parâmetros e Filtros](#parâmetros-e-filtros)
3. [Gráficos e Visualizações](#gráficos-e-visualizações)
4. [Grids e Tabelas](#grids-e-tabelas)
5. [Navegação entre Níveis](#navegação-entre-níveis)
6. [Queries Complexas para Dashboards](#queries-complexas-para-dashboards)
7. [Padrões de Layout](#padrões-de-layout)

## 🏗️ Estrutura de Componentes XML

### Template Base de Gadget
```xml
<gadget>
  <prompt-parameters>
    <!-- Parâmetros de entrada -->
  </prompt-parameters>
  <level id="lvl_principal" description="Principal">
    <args>
      <!-- Argumentos do nível -->
    </args>
    <container orientacao="V" tamanhoRelativo="100">
      <!-- Conteúdo do dashboard -->
    </container>
  </level>
</gadget>
```

### Estrutura de Parâmetros
```xml
<prompt-parameters>
  <!-- Parâmetro de Período -->
  <parameter id="P_PERIODO" description="Período" 
             metadata="datePeriod" required="true" 
             keep-last="true" keep-date="false" order="0"/>
  
  <!-- Parâmetro de Lista SQL -->
  <parameter id="P_EMPRESA" description="Empresa" 
             metadata="multiList:Text" listType="sql" 
             required="false" keep-last="true" 
             keep-date="false" order="1">
    <expression type="SQL">
      <![CDATA[
        SELECT 
          CAB.CODEMP AS VALUE,
          CAB.CODEMP || ' - ' || EMP.RAZAOSOCIAL AS LABEL
        FROM TGFCAB CAB
        INNER JOIN TSIEMP EMP ON CAB.CODEMP = EMP.CODEMP
        GROUP BY CAB.CODEMP, EMP.RAZAOSOCIAL
        ORDER BY CAB.CODEMP
      ]]>
    </expression>
  </parameter>
  
  <!-- Parâmetro de Entidade -->
  <parameter id="P_CLIENTE" description="Cliente" 
             metadata="entity:Parceiro@CODPARC" 
             required="false" keep-last="true" 
             keep-date="false" order="2"/>
</prompt-parameters>
```

## 🎛️ Parâmetros e Filtros

### Tipos de Parâmetros Comuns

#### 1. Período de Data
```xml
<parameter id="P_PERIODO" description="Período" 
           metadata="datePeriod" required="true" 
           keep-last="true" keep-date="false" order="0"/>
```

#### 2. Lista Múltipla com SQL
```xml
<parameter id="P_TOP" description="Tipo Operação" 
           metadata="multiList:Text" listType="sql" 
           required="false" keep-last="true" 
           keep-date="false" order="1">
  <expression type="SQL">
    <![CDATA[
      SELECT
        CAB.CODTIPOPER AS VALUE,
        CAB.CODTIPOPER || ' - ' || TOP.DESCROPER AS LABEL
      FROM TGFCAB CAB
      INNER JOIN TGFTOP TOP ON CAB.CODTIPOPER = TOP.CODTIPOPER
      WHERE CAB.STATUSNFE = 'A' AND CAB.TIPMOV = 'V'
      GROUP BY CAB.CODTIPOPER, TOP.DESCROPER
      ORDER BY 1
    ]]>
  </expression>
</parameter>
```

#### 3. Lista Simples
```xml
<parameter id="P_STATUS" description="Status" 
           metadata="singleList:Text" listType="text" 
           required="false" keep-last="true" 
           keep-date="false" order="2">
  <expression type="text">
    <![CDATA[
      A|Ativo
      I|Inativo
      T|Todos
    ]]>
  </expression>
</parameter>
```

#### 4. Parâmetro de Entidade
```xml
<parameter id="P_VENDEDOR" description="Vendedor" 
           metadata="entity:Parceiro@CODPARC" 
           required="false" keep-last="true" 
           keep-date="false" order="3">
  <expression type="SQL">
    <![CDATA[
      SELECT CODPARC FROM TGFPAR 
      WHERE TIPPARC = 'V' AND ATIVO = 'S'
    ]]>
  </expression>
</parameter>
```

## 📊 Gráficos e Visualizações

### 1. Gráfico de Barras
```xml
<chart type="bar" title="Vendas por Mês" 
       width="100%" height="300px">
  <query>
    <![CDATA[
      SELECT 
        TO_CHAR(CAB.DTNEG, 'YYYY-MM') AS MES,
        SUM(ITE.VLRTOT) AS VALOR
      FROM TGFCAB CAB
      INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
      WHERE CAB.DTNEG BETWEEN :P_PERIODO_INICIO AND :P_PERIODO_FIM
        AND CAB.TIPMOV = 'V'
        AND CAB.STATUSNFE = 'A'
      GROUP BY TO_CHAR(CAB.DTNEG, 'YYYY-MM')
      ORDER BY MES
    ]]>
  </query>
  <x-axis field="MES" title="Mês"/>
  <y-axis field="VALOR" title="Valor (R$)"/>
  <series field="VALOR" color="#007bff"/>
</chart>
```

### 2. Gráfico de Pizza
```xml
<chart type="pie" title="Vendas por Vendedor" 
       width="100%" height="300px">
  <query>
    <![CDATA[
      SELECT 
        PAR.NOMEPARC AS VENDEDOR,
        SUM(ITE.VLRTOT) AS VALOR
      FROM TGFCAB CAB
      INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
      INNER JOIN TGFPAR PAR ON CAB.CODVEND = PAR.CODPARC
      WHERE CAB.DTNEG BETWEEN :P_PERIODO_INICIO AND :P_PERIODO_FIM
        AND CAB.TIPMOV = 'V'
        AND CAB.STATUSNFE = 'A'
      GROUP BY PAR.NOMEPARC
      ORDER BY VALOR DESC
      FETCH FIRST 10 ROWS ONLY
    ]]>
  </query>
  <label field="VENDEDOR"/>
  <value field="VALOR"/>
  <colors>
    <color>#007bff</color>
    <color>#28a745</color>
    <color>#ffc107</color>
    <color>#dc3545</color>
    <color>#6f42c1</color>
  </colors>
</chart>
```

### 3. Gráfico de Linha
```xml
<chart type="line" title="Evolução de Vendas" 
       width="100%" height="300px">
  <query>
    <![CDATA[
      SELECT 
        TO_CHAR(CAB.DTNEG, 'YYYY-MM') AS MES,
        SUM(ITE.VLRTOT) AS VALOR,
        COUNT(DISTINCT CAB.NUNOTA) AS QTD_PEDIDOS
      FROM TGFCAB CAB
      INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
      WHERE CAB.DTNEG BETWEEN :P_PERIODO_INICIO AND :P_PERIODO_FIM
        AND CAB.TIPMOV = 'V'
        AND CAB.STATUSNFE = 'A'
      GROUP BY TO_CHAR(CAB.DTNEG, 'YYYY-MM')
      ORDER BY MES
    ]]>
  </query>
  <x-axis field="MES" title="Mês"/>
  <y-axis field="VALOR" title="Valor (R$)"/>
  <series field="VALOR" color="#007bff" name="Valor"/>
  <series field="QTD_PEDIDOS" color="#28a745" name="Qtd Pedidos"/>
</chart>
```

### 4. Gráfico de Área
```xml
<chart type="area" title="Estoque por Produto" 
       width="100%" height="300px">
  <query>
    <![CDATA[
      SELECT 
        PRO.DESCRPROD AS PRODUTO,
        EST.QTDEST AS ESTOQUE,
        PRO.VLRVENDA AS PRECO
      FROM TGFPRO PRO
      INNER JOIN TGFEST EST ON PRO.CODPROD = EST.CODPROD
      WHERE EST.QTDEST > 0
      ORDER BY EST.QTDEST DESC
      FETCH FIRST 15 ROWS ONLY
    ]]>
  </query>
  <x-axis field="PRODUTO" title="Produto"/>
  <y-axis field="ESTOQUE" title="Quantidade"/>
  <series field="ESTOQUE" color="#17a2b8"/>
</chart>
```

## 📋 Grids e Tabelas

### 1. Grid Simples
```xml
<grid title="Top 10 Clientes" width="100%" height="400px">
  <query>
    <![CDATA[
      SELECT 
        PAR.NOMEPARC AS CLIENTE,
        COUNT(DISTINCT CAB.NUNOTA) AS QTD_PEDIDOS,
        SUM(ITE.VLRTOT) AS VALOR_TOTAL,
        AVG(ITE.VLRTOT) AS TICKET_MEDIO
      FROM TGFCAB CAB
      INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
      INNER JOIN TGFPAR PAR ON CAB.CODPARC = PAR.CODPARC
      WHERE CAB.DTNEG BETWEEN :P_PERIODO_INICIO AND :P_PERIODO_FIM
        AND CAB.TIPMOV = 'V'
        AND CAB.STATUSNFE = 'A'
      GROUP BY PAR.NOMEPARC
      ORDER BY VALOR_TOTAL DESC
      FETCH FIRST 10 ROWS ONLY
    ]]>
  </query>
  <columns>
    <column field="CLIENTE" title="Cliente" width="200px"/>
    <column field="QTD_PEDIDOS" title="Qtd Pedidos" width="100px" align="center"/>
    <column field="VALOR_TOTAL" title="Valor Total" width="120px" align="right" format="currency"/>
    <column field="TICKET_MEDIO" title="Ticket Médio" width="120px" align="right" format="currency"/>
  </columns>
  <pagination pageSize="10"/>
  <export formats="excel,pdf"/>
</grid>
```

### 2. Grid com Filtros
```xml
<grid title="Produtos Mais Vendidos" width="100%" height="400px">
  <query>
    <![CDATA[
      SELECT 
        PRO.DESCRPROD AS PRODUTO,
        CAT.DESCRCATEGORIA AS CATEGORIA,
        SUM(ITE.QTDNEG) AS QTD_VENDIDA,
        SUM(ITE.VLRTOT) AS VALOR_TOTAL,
        AVG(ITE.VLRUNIT) AS PRECO_MEDIO
      FROM TGFITE ITE
      INNER JOIN TGFPRO PRO ON ITE.CODPROD = PRO.CODPROD
      INNER JOIN TGFCAT CAT ON PRO.CODCATEGORIA = CAT.CODCATEGORIA
      INNER JOIN TGFCAB CAB ON ITE.NUNOTA = CAB.NUNOTA
      WHERE CAB.DTNEG BETWEEN :P_PERIODO_INICIO AND :P_PERIODO_FIM
        AND CAB.TIPMOV = 'V'
        AND CAB.STATUSNFE = 'A'
        AND (:P_CATEGORIA IS NULL OR PRO.CODCATEGORIA = :P_CATEGORIA)
        AND (:P_VENDEDOR IS NULL OR CAB.CODVEND = :P_VENDEDOR)
      GROUP BY PRO.DESCRPROD, CAT.DESCRCATEGORIA
      ORDER BY QTD_VENDIDA DESC
    ]]>
  </query>
  <columns>
    <column field="PRODUTO" title="Produto" width="250px"/>
    <column field="CATEGORIA" title="Categoria" width="150px"/>
    <column field="QTD_VENDIDA" title="Qtd Vendida" width="100px" align="center"/>
    <column field="VALOR_TOTAL" title="Valor Total" width="120px" align="right" format="currency"/>
    <column field="PRECO_MEDIO" title="Preço Médio" width="120px" align="right" format="currency"/>
  </columns>
  <filters>
    <filter field="PRODUTO" type="text" placeholder="Filtrar por produto"/>
    <filter field="CATEGORIA" type="select" placeholder="Filtrar por categoria"/>
  </filters>
  <pagination pageSize="20"/>
  <export formats="excel,pdf"/>
</grid>
```

### 3. Grid com Ações
```xml
<grid title="Pedidos Pendentes" width="100%" height="400px">
  <query>
    <![CDATA[
      SELECT 
        CAB.NUNOTA AS NUMERO,
        PAR.NOMEPARC AS CLIENTE,
        CAB.DTNEG AS DATA,
        CAB.VLRTOT AS VALOR,
        CAB.STATUSNFE AS STATUS
      FROM TGFCAB CAB
      INNER JOIN TGFPAR PAR ON CAB.CODPARC = PAR.CODPARC
      WHERE CAB.STATUSNFE = 'A'
        AND CAB.TIPMOV = 'V'
        AND CAB.DTNEG >= SYSDATE - 30
      ORDER BY CAB.DTNEG DESC
    ]]>
  </query>
  <columns>
    <column field="NUMERO" title="Número" width="100px" align="center"/>
    <column field="CLIENTE" title="Cliente" width="200px"/>
    <column field="DATA" title="Data" width="100px" align="center" format="date"/>
    <column field="VALOR" title="Valor" width="120px" align="right" format="currency"/>
    <column field="STATUS" title="Status" width="100px" align="center"/>
  </columns>
  <actions>
    <action name="visualizar" title="Visualizar" icon="eye" 
            onclick="visualizarPedido" />
    <action name="editar" title="Editar" icon="edit" 
            onclick="editarPedido" />
    <action name="faturar" title="Faturar" icon="check" 
            onclick="faturarPedido" />
  </actions>
  <pagination pageSize="15"/>
</grid>
```

## 🔗 Navegação entre Níveis

### 1. Nível Principal com Drill-Down
```xml
<level id="lvl_principal" description="Principal">
  <args>
    <arg name="P_PERIODO_INICIO" value=":P_PERIODO_INICIO"/>
    <arg name="P_PERIODO_FIM" value=":P_PERIODO_FIM"/>
  </args>
  
  <container orientacao="V" tamanhoRelativo="100">
    <!-- Gráfico de vendas por mês -->
    <chart type="bar" title="Vendas por Mês" 
           width="100%" height="300px">
      <query>
        <![CDATA[
          SELECT 
            TO_CHAR(CAB.DTNEG, 'YYYY-MM') AS MES,
            SUM(ITE.VLRTOT) AS VALOR
          FROM TGFCAB CAB
          INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
          WHERE CAB.DTNEG BETWEEN :P_PERIODO_INICIO AND :P_PERIODO_FIM
            AND CAB.TIPMOV = 'V'
            AND CAB.STATUSNFE = 'A'
          GROUP BY TO_CHAR(CAB.DTNEG, 'YYYY-MM')
          ORDER BY MES
        ]]>
      </query>
      <x-axis field="MES" title="Mês"/>
      <y-axis field="VALOR" title="Valor (R$)"/>
      <series field="VALOR" color="#007bff"/>
      <on-click action="openLevel" level="lvl_detalhe_mes" 
                parameter="P_MES" value=":MES"/>
    </chart>
  </container>
</level>
```

### 2. Nível de Detalhe
```xml
<level id="lvl_detalhe_mes" description="Detalhe do Mês">
  <args>
    <arg name="P_MES" value=":P_MES"/>
    <arg name="P_PERIODO_INICIO" value=":P_PERIODO_INICIO"/>
    <arg name="P_PERIODO_FIM" value=":P_PERIODO_FIM"/>
  </args>
  
  <container orientacao="V" tamanhoRelativo="100">
    <!-- Grid com vendas do mês selecionado -->
    <grid title="Vendas do Mês :P_MES" width="100%" height="400px">
      <query>
        <![CDATA[
          SELECT 
            CAB.NUNOTA AS NUMERO,
            PAR.NOMEPARC AS CLIENTE,
            CAB.DTNEG AS DATA,
            SUM(ITE.VLRTOT) AS VALOR
          FROM TGFCAB CAB
          INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
          INNER JOIN TGFPAR PAR ON CAB.CODPARC = PAR.CODPARC
          WHERE TO_CHAR(CAB.DTNEG, 'YYYY-MM') = :P_MES
            AND CAB.TIPMOV = 'V'
            AND CAB.STATUSNFE = 'A'
          GROUP BY CAB.NUNOTA, PAR.NOMEPARC, CAB.DTNEG
          ORDER BY CAB.DTNEG DESC
        ]]>
      </query>
      <columns>
        <column field="NUMERO" title="Número" width="100px" align="center"/>
        <column field="CLIENTE" title="Cliente" width="200px"/>
        <column field="DATA" title="Data" width="100px" align="center" format="date"/>
        <column field="VALOR" title="Valor" width="120px" align="right" format="currency"/>
      </columns>
      <actions>
        <action name="visualizar" title="Visualizar" icon="eye" 
                onclick="visualizarPedido" />
      </actions>
    </grid>
  </container>
</level>
```

## 📊 Queries Complexas para Dashboards

### 1. Dashboard de Performance de Vendedores
```sql
-- Query para ranking de vendedores
SELECT 
    VEND.NOMEPARC AS VENDEDOR,
    COUNT(DISTINCT CAB.NUNOTA) AS QTD_PEDIDOS,
    SUM(ITE.VLRTOT) AS VALOR_TOTAL,
    AVG(ITE.VLRTOT) AS TICKET_MEDIO,
    ROUND(SUM(ITE.VLRTOT) / COUNT(DISTINCT CAB.NUNOTA), 2) AS TICKET_MEDIO_CALC,
    RANK() OVER (ORDER BY SUM(ITE.VLRTOT) DESC) AS RANKING
FROM TGFCAB CAB
INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
INNER JOIN TGFPAR VEND ON CAB.CODVEND = VEND.CODPARC
WHERE CAB.DTNEG BETWEEN :P_PERIODO_INICIO AND :P_PERIODO_FIM
  AND CAB.TIPMOV = 'V'
  AND CAB.STATUSNFE = 'A'
  AND CAB.CODVEND IS NOT NULL
GROUP BY VEND.NOMEPARC
ORDER BY VALOR_TOTAL DESC;
```

### 2. Dashboard de Análise de Produtos
```sql
-- Query para análise de produtos
SELECT 
    PRO.DESCRPROD AS PRODUTO,
    CAT.DESCRCATEGORIA AS CATEGORIA,
    COUNT(DISTINCT CAB.NUNOTA) AS QTD_PEDIDOS,
    SUM(ITE.QTDNEG) AS QTD_VENDIDA,
    SUM(ITE.VLRTOT) AS VALOR_TOTAL,
    AVG(ITE.VLRUNIT) AS PRECO_MEDIO,
    EST.QTDEST AS ESTOQUE_ATUAL,
    CASE 
        WHEN EST.QTDEST = 0 THEN 'Sem Estoque'
        WHEN EST.QTDEST < 10 THEN 'Estoque Baixo'
        WHEN EST.QTDEST > 100 THEN 'Estoque Alto'
        ELSE 'Estoque Normal'
    END AS STATUS_ESTOQUE
FROM TGFITE ITE
INNER JOIN TGFPRO PRO ON ITE.CODPROD = PRO.CODPROD
INNER JOIN TGFCAT CAT ON PRO.CODCATEGORIA = CAT.CODCATEGORIA
INNER JOIN TGFCAB CAB ON ITE.NUNOTA = CAB.NUNOTA
LEFT JOIN TGFEST EST ON PRO.CODPROD = EST.CODPROD
WHERE CAB.DTNEG BETWEEN :P_PERIODO_INICIO AND :P_PERIODO_FIM
  AND CAB.TIPMOV = 'V'
  AND CAB.STATUSNFE = 'A'
GROUP BY PRO.DESCRPROD, CAT.DESCRCATEGORIA, EST.QTDEST
ORDER BY VALOR_TOTAL DESC;
```

### 3. Dashboard de Análise Financeira
```sql
-- Query para análise financeira
SELECT 
    TO_CHAR(CAB.DTNEG, 'YYYY-MM') AS MES,
    COUNT(DISTINCT CAB.NUNOTA) AS QTD_PEDIDOS,
    SUM(ITE.VLRTOT) AS VALOR_BRUTO,
    SUM(ITE.VLRDESC) AS VALOR_DESCONTO,
    SUM(ITE.VLRTOT - ITE.VLRDESC) AS VALOR_LIQUIDO,
    SUM(ITE.VLRICMS) AS VALOR_ICMS,
    SUM(ITE.VLRIPI) AS VALOR_IPI,
    SUM(ITE.VLRPIS) AS VALOR_PIS,
    SUM(ITE.VLRCOFINS) AS VALOR_COFINS,
    ROUND(AVG(ITE.VLRTOT), 2) AS TICKET_MEDIO
FROM TGFCAB CAB
INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
WHERE CAB.DTNEG BETWEEN :P_PERIODO_INICIO AND :P_PERIODO_FIM
  AND CAB.TIPMOV = 'V'
  AND CAB.STATUSNFE = 'A'
GROUP BY TO_CHAR(CAB.DTNEG, 'YYYY-MM')
ORDER BY MES;
```

### 4. Dashboard de Análise de Clientes
```sql
-- Query para análise de clientes
SELECT 
    PAR.NOMEPARC AS CLIENTE,
    PAR.CGCCPF AS CPF_CNPJ,
    COUNT(DISTINCT CAB.NUNOTA) AS QTD_PEDIDOS,
    SUM(ITE.VLRTOT) AS VALOR_TOTAL,
    AVG(ITE.VLRTOT) AS TICKET_MEDIO,
    MIN(CAB.DTNEG) AS PRIMEIRA_COMPRA,
    MAX(CAB.DTNEG) AS ULTIMA_COMPRA,
    ROUND((MAX(CAB.DTNEG) - MIN(CAB.DTNEG)) / 30, 1) AS MESES_ATIVO,
    CASE 
        WHEN SUM(ITE.VLRTOT) > 100000 THEN 'A'
        WHEN SUM(ITE.VLRTOT) > 50000 THEN 'B'
        WHEN SUM(ITE.VLRTOT) > 10000 THEN 'C'
        ELSE 'D'
    END AS CLASSIFICACAO
FROM TGFCAB CAB
INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
INNER JOIN TGFPAR PAR ON CAB.CODPARC = PAR.CODPARC
WHERE CAB.DTNEG BETWEEN :P_PERIODO_INICIO AND :P_PERIODO_FIM
  AND CAB.TIPMOV = 'V'
  AND CAB.STATUSNFE = 'A'
GROUP BY PAR.NOMEPARC, PAR.CGCCPF
ORDER BY VALOR_TOTAL DESC;
```

## 🎨 Padrões de Layout

### 1. Layout em Colunas
```xml
<container orientacao="H" tamanhoRelativo="100">
  <!-- Coluna 1: Gráficos -->
  <container orientacao="V" tamanhoRelativo="60">
    <chart type="bar" title="Vendas por Mês" 
           width="100%" height="300px">
      <!-- Query do gráfico -->
    </chart>
    
    <chart type="pie" title="Vendas por Vendedor" 
           width="100%" height="300px">
      <!-- Query do gráfico -->
    </chart>
  </container>
  
  <!-- Coluna 2: Grids -->
  <container orientacao="V" tamanhoRelativo="40">
    <grid title="Top 10 Clientes" width="100%" height="400px">
      <!-- Query do grid -->
    </grid>
    
    <grid title="Produtos Mais Vendidos" width="100%" height="400px">
      <!-- Query do grid -->
    </grid>
  </container>
</container>
```

### 2. Layout em Linhas
```xml
<container orientacao="V" tamanhoRelativo="100">
  <!-- Linha 1: Gráficos -->
  <container orientacao="H" tamanhoRelativo="50">
    <chart type="line" title="Evolução de Vendas" 
           width="50%" height="300px">
      <!-- Query do gráfico -->
    </chart>
    
    <chart type="area" title="Estoque por Produto" 
           width="50%" height="300px">
      <!-- Query do gráfico -->
    </chart>
  </container>
  
  <!-- Linha 2: Grids -->
  <container orientacao="H" tamanhoRelativo="50">
    <grid title="Pedidos Pendentes" width="50%" height="400px">
      <!-- Query do grid -->
    </grid>
    
    <grid title="Análise Financeira" width="50%" height="400px">
      <!-- Query do grid -->
    </grid>
  </container>
</container>
```

### 3. Layout Misto
```xml
<container orientacao="V" tamanhoRelativo="100">
  <!-- Linha 1: Gráfico principal -->
  <chart type="bar" title="Dashboard Principal" 
         width="100%" height="400px">
    <!-- Query do gráfico -->
  </chart>
  
  <!-- Linha 2: Grids em colunas -->
  <container orientacao="H" tamanhoRelativo="60">
    <grid title="Análise de Vendas" width="50%" height="300px">
      <!-- Query do grid -->
    </grid>
    
    <grid title="Análise de Produtos" width="50%" height="300px">
      <!-- Query do grid -->
    </grid>
  </container>
  
  <!-- Linha 3: Gráficos menores -->
  <container orientacao="H" tamanhoRelativo="40">
    <chart type="pie" title="Distribuição" 
           width="33%" height="250px">
      <!-- Query do gráfico -->
    </chart>
    
    <chart type="line" title="Tendência" 
           width="33%" height="250px">
      <!-- Query do gráfico -->
    </chart>
    
    <chart type="area" title="Comparativo" 
           width="34%" height="250px">
      <!-- Query do gráfico -->
    </chart>
  </container>
</container>
```

## 🎯 Casos de Uso Avançados

### 1. Dashboard com Filtros Dinâmicos
```xml
<gadget>
  <prompt-parameters>
    <parameter id="P_PERIODO" description="Período" 
               metadata="datePeriod" required="true" 
               keep-last="true" keep-date="false" order="0"/>
    
    <parameter id="P_EMPRESA" description="Empresa" 
               metadata="multiList:Text" listType="sql" 
               required="false" keep-last="true" 
               keep-date="false" order="1">
      <expression type="SQL">
        <![CDATA[
          SELECT 
            EMP.CODEMP AS VALUE,
            EMP.CODEMP || ' - ' || EMP.RAZAOSOCIAL AS LABEL
          FROM TSIEMP EMP
          WHERE EMP.ATIVO = 'S'
          ORDER BY EMP.CODEMP
        ]]>
      </expression>
    </parameter>
    
    <parameter id="P_VENDEDOR" description="Vendedor" 
               metadata="multiList:Text" listType="sql" 
               required="false" keep-last="true" 
               keep-date="false" order="2">
      <expression type="SQL">
        <![CDATA[
          SELECT 
            PAR.CODPARC AS VALUE,
            PAR.NOMEPARC AS LABEL
          FROM TGFPAR PAR
          WHERE PAR.TIPPARC = 'V' 
            AND PAR.ATIVO = 'S'
            AND (:P_EMPRESA IS NULL OR PAR.CODEMP = :P_EMPRESA)
          ORDER BY PAR.NOMEPARC
        ]]>
      </expression>
    </parameter>
  </prompt-parameters>
  
  <level id="lvl_principal" description="Principal">
    <args>
      <arg name="P_PERIODO_INICIO" value=":P_PERIODO_INICIO"/>
      <arg name="P_PERIODO_FIM" value=":P_PERIODO_FIM"/>
      <arg name="P_EMPRESA" value=":P_EMPRESA"/>
      <arg name="P_VENDEDOR" value=":P_VENDEDOR"/>
    </args>
    
    <container orientacao="V" tamanhoRelativo="100">
      <!-- Conteúdo do dashboard -->
    </container>
  </level>
</gadget>
```

### 2. Dashboard com Métricas em Tempo Real
```xml
<container orientacao="H" tamanhoRelativo="100">
  <!-- Métricas -->
  <container orientacao="V" tamanhoRelativo="25">
    <metric title="Vendas Hoje" value=":VENDAS_HOJE" 
            format="currency" color="#28a745"/>
    <metric title="Pedidos Hoje" value=":PEDIDOS_HOJE" 
            format="number" color="#007bff"/>
    <metric title="Ticket Médio" value=":TICKET_MEDIO" 
            format="currency" color="#ffc107"/>
    <metric title="Clientes Ativos" value=":CLIENTES_ATIVOS" 
            format="number" color="#17a2b8"/>
  </container>
  
  <!-- Gráficos -->
  <container orientacao="V" tamanhoRelativo="75">
    <chart type="line" title="Vendas em Tempo Real" 
           width="100%" height="400px">
      <query>
        <![CDATA[
          SELECT 
            TO_CHAR(CAB.DTNEG, 'HH24:MI') AS HORA,
            SUM(ITE.VLRTOT) AS VALOR
          FROM TGFCAB CAB
          INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
          WHERE CAB.DTNEG >= TRUNC(SYSDATE)
            AND CAB.TIPMOV = 'V'
            AND CAB.STATUSNFE = 'A'
          GROUP BY TO_CHAR(CAB.DTNEG, 'HH24:MI')
          ORDER BY HORA
        ]]>
      </query>
    </chart>
  </container>
</container>
```

### 3. Dashboard com Comparativo Períodos
```xml
<chart type="bar" title="Comparativo de Vendas" 
       width="100%" height="400px">
  <query>
    <![CDATA[
      SELECT 
        'Atual' AS PERIODO,
        TO_CHAR(CAB.DTNEG, 'YYYY-MM') AS MES,
        SUM(ITE.VLRTOT) AS VALOR
      FROM TGFCAB CAB
      INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
      WHERE CAB.DTNEG BETWEEN :P_PERIODO_INICIO AND :P_PERIODO_FIM
        AND CAB.TIPMOV = 'V'
        AND CAB.STATUSNFE = 'A'
      GROUP BY TO_CHAR(CAB.DTNEG, 'YYYY-MM')
      
      UNION ALL
      
      SELECT 
        'Anterior' AS PERIODO,
        TO_CHAR(CAB.DTNEG, 'YYYY-MM') AS MES,
        SUM(ITE.VLRTOT) AS VALOR
      FROM TGFCAB CAB
      INNER JOIN TGFITE ITE ON CAB.NUNOTA = ITE.NUNOTA
      WHERE CAB.DTNEG BETWEEN :P_PERIODO_INICIO - 365 AND :P_PERIODO_FIM - 365
        AND CAB.TIPMOV = 'V'
        AND CAB.STATUSNFE = 'A'
      GROUP BY TO_CHAR(CAB.DTNEG, 'YYYY-MM')
      
      ORDER BY MES, PERIODO
    ]]>
  </query>
  <x-axis field="MES" title="Mês"/>
  <y-axis field="VALOR" title="Valor (R$)"/>
  <series field="VALOR" groupBy="PERIODO" 
          colors="#007bff,#28a745"/>
</chart>
```

## 🛠️ Boas Práticas

### **1. Design de Dashboards**
- **Simplicidade**: Manter interface limpa e organizada
- **Consistência**: Usar cores e estilos consistentes
- **Responsividade**: Adaptar para diferentes tamanhos de tela
- **Performance**: Otimizar queries para carregamento rápido
- **Usabilidade**: Facilitar navegação e interação

### **2. Otimização de Performance**
- **Índices**: Usar índices adequados nas consultas
- **Filtros**: Aplicar filtros para reduzir dados
- **Cache**: Implementar cache para dados frequentes
- **Paginação**: Usar paginação em grids grandes
- **Lazy Loading**: Carregar dados sob demanda

### **3. Tratamento de Dados**
- **Validação**: Validar parâmetros de entrada
- **Formatação**: Formatar dados adequadamente
- **Agregação**: Usar agregações para resumir dados
- **Ordenação**: Ordenar dados de forma lógica
- **Limitação**: Limitar resultados para performance

### **4. Interatividade**
- **Drill-Down**: Permitir navegação entre níveis
- **Filtros**: Implementar filtros dinâmicos
- **Ações**: Adicionar ações contextuais
- **Exportação**: Permitir exportação de dados
- **Atualização**: Implementar atualização automática

## 🔍 Troubleshooting

### **Problemas Comuns**
- **Performance lenta**: Otimizar queries e índices
- **Dados incorretos**: Verificar filtros e parâmetros
- **Layout quebrado**: Verificar estrutura XML
- **Gráficos não carregam**: Verificar queries e dados
- **Filtros não funcionam**: Verificar configuração de parâmetros

### **Soluções**
- **Logs**: Analisar logs de execução
- **Debug**: Usar ferramentas de debug
- **Testes**: Testar em ambiente isolado
- **Documentação**: Consultar documentação oficial
- **Suporte**: Contatar suporte técnico

## 🚀 Evolução e Tendências

### **Melhorias Contínuas**
- **Novos Tipos**: Novos tipos de gráficos
- **Melhor Performance**: Otimizações de performance
- **Integração**: Melhor integração com dados
- **Interatividade**: Mais recursos interativos
- **Mobile**: Suporte para dispositivos móveis

### **Tendências Futuras**
- **Real-time**: Dashboards em tempo real
- **IA**: Integração com inteligência artificial
- **Cloud**: Execução em cloud
- **Big Data**: Suporte para grandes volumes
- **VR/AR**: Visualizações em realidade virtual

---

*Este documento foi criado com base na documentação oficial do Sankhya Developer sobre Dashboards e melhores práticas de desenvolvimento.*
