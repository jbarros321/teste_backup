# 📊 iReport Sankhya - Relatórios Avançados

## 🎯 Visão Geral

Este documento apresenta implementações avançadas de relatórios usando iReport no Sankhya, extraídas do código fonte SankhyaW 4.8 e padrões de desenvolvimento enterprise.

## 🏗️ **Arquitetura de Relatórios**

### **1. Sistema de Geração de Relatórios**

```java
package br.com.empresa.relatorios;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRExcelExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

/**
 * Sistema de geração de relatórios com iReport
 */
public class RelatorioService {
    
    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    private String reportPath = "/reports/";
    
    /**
     * Gerar relatório de vendas
     */
    public byte[] gerarRelatorioVendas(RelatorioVendasParametros parametros) throws Exception {
        // Compilar template
        JasperReport jasperReport = compilarTemplate("relatorio_vendas.jrxml");
        
        // Preparar parâmetros
        Map<String, Object> params = prepararParametrosVendas(parametros);
        
        // Executar query
        List<DynamicVO> dados = executarQueryVendas(parametros);
        
        // Converter para datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
            converterDadosVendas(dados)
        );
        
        // Gerar relatório
        JasperPrint jasperPrint = JasperFillManager.fillReport(
            jasperReport, params, dataSource
        );
        
        // Exportar para PDF
        return exportarParaPDF(jasperPrint);
    }
    
    /**
     * Gerar relatório de estoque
     */
    public byte[] gerarRelatorioEstoque(RelatorioEstoqueParametros parametros) throws Exception {
        JasperReport jasperReport = compilarTemplate("relatorio_estoque.jrxml");
        Map<String, Object> params = prepararParametrosEstoque(parametros);
        List<DynamicVO> dados = executarQueryEstoque(parametros);
        
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
            converterDadosEstoque(dados)
        );
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(
            jasperReport, params, dataSource
        );
        
        return exportarParaExcel(jasperPrint);
    }
    
    /**
     * Gerar relatório financeiro
     */
    public byte[] gerarRelatorioFinanceiro(RelatorioFinanceiroParametros parametros) throws Exception {
        JasperReport jasperReport = compilarTemplate("relatorio_financeiro.jrxml");
        Map<String, Object> params = prepararParametrosFinanceiro(parametros);
        List<DynamicVO> dados = executarQueryFinanceiro(parametros);
        
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
            converterDadosFinanceiro(dados)
        );
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(
            jasperReport, params, dataSource
        );
        
        return exportarParaPDF(jasperPrint);
    }
    
    private JasperReport compilarTemplate(String templateName) throws Exception {
        String templatePath = reportPath + templateName;
        
        // Verificar se já está compilado
        File compiledFile = new File(templatePath.replace(".jrxml", ".jasper"));
        if (compiledFile.exists() && compiledFile.lastModified() > new File(templatePath).lastModified()) {
            return (JasperReport) JRLoader.loadObject(compiledFile);
        }
        
        // Compilar template
        JasperReport report = JasperCompileManager.compileReport(templatePath);
        
        // Salvar compilado
        JasperCompileManager.compileReportToFile(templatePath, 
            templatePath.replace(".jrxml", ".jasper"));
        
        return report;
    }
    
    private Map<String, Object> prepararParametrosVendas(RelatorioVendasParametros parametros) {
        Map<String, Object> params = new HashMap<>();
        
        params.put("DATA_INICIO", parametros.getDataInicio());
        params.put("DATA_FIM", parametros.getDataFim());
        params.put("CODVEN", parametros.getCodigoVendedor());
        params.put("CODPARC", parametros.getCodigoCliente());
        params.put("TIPMOV", parametros.getTipoMovimento());
        params.put("REPORT_TITLE", "Relatório de Vendas");
        params.put("GENERATED_BY", System.getProperty("user.name"));
        params.put("GENERATED_AT", new Date());
        
        return params;
    }
    
    private List<DynamicVO> executarQueryVendas(RelatorioVendasParametros parametros) throws Exception {
        String sql = """
            SELECT 
                c.NUNOTA,
                c.DTEMISSAO,
                c.VLRNOTA,
                p.NOMEPARC as NOME_CLIENTE,
                v.NOMEPARC as NOME_VENDEDOR,
                c.STATUSNOTA,
                COUNT(i.SEQITEM) as QTD_ITENS
            FROM TGFCAB c
            LEFT JOIN TGFPAR p ON c.CODPARC = p.CODPARC
            LEFT JOIN TGFPAR v ON c.CODVEN = v.CODPARC
            LEFT JOIN TGFITE i ON c.NUNOTA = i.NUNOTA
            WHERE c.TIPMOV = ?
            AND c.DTEMISSAO BETWEEN ? AND ?
            AND (? IS NULL OR c.CODVEN = ?)
            AND (? IS NULL OR c.CODPARC = ?)
            GROUP BY c.NUNOTA, c.DTEMISSAO, c.VLRNOTA, p.NOMEPARC, v.NOMEPARC, c.STATUSNOTA
            ORDER BY c.DTEMISSAO DESC
            """;
        
        return facade.getQueryExecutor().executeQuery(sql,
            parametros.getTipoMovimento(),
            parametros.getDataInicio(),
            parametros.getDataFim(),
            parametros.getCodigoVendedor(),
            parametros.getCodigoVendedor(),
            parametros.getCodigoCliente(),
            parametros.getCodigoCliente()
        );
    }
    
    private List<VendaRelatorio> converterDadosVendas(List<DynamicVO> dados) {
        return dados.stream()
            .map(vo -> new VendaRelatorio(
                vo.asBigDecimal("NUNOTA"),
                vo.asDate("DTEMISSAO"),
                vo.asBigDecimal("VLRNOTA"),
                vo.asString("NOME_CLIENTE"),
                vo.asString("NOME_VENDEDOR"),
                vo.asString("STATUSNOTA"),
                vo.asBigDecimal("QTD_ITENS")
            ))
            .collect(Collectors.toList());
    }
    
    private byte[] exportarParaPDF(JasperPrint jasperPrint) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        
        exporter.exportReport();
        
        return outputStream.toByteArray();
    }
    
    private byte[] exportarParaExcel(JasperPrint jasperPrint) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        JRExcelExporter exporter = new JRExcelExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        
        exporter.exportReport();
        
        return outputStream.toByteArray();
    }
}

/**
 * Classe para dados de venda no relatório
 */
class VendaRelatorio {
    private BigDecimal nunota;
    private Date dtemissao;
    private BigDecimal vlrnota;
    private String nomeCliente;
    private String nomeVendedor;
    private String statusNota;
    private BigDecimal qtdItens;
    
    public VendaRelatorio(BigDecimal nunota, Date dtemissao, BigDecimal vlrnota,
                         String nomeCliente, String nomeVendedor, String statusNota,
                         BigDecimal qtdItens) {
        this.nunota = nunota;
        this.dtemissao = dtemissao;
        this.vlrnota = vlrnota;
        this.nomeCliente = nomeCliente;
        this.nomeVendedor = nomeVendedor;
        this.statusNota = statusNota;
        this.qtdItens = qtdItens;
    }
    
    // Getters
    public BigDecimal getNunota() { return nunota; }
    public Date getDtemissao() { return dtemissao; }
    public BigDecimal getVlrnota() { return vlrnota; }
    public String getNomeCliente() { return nomeCliente; }
    public String getNomeVendedor() { return nomeVendedor; }
    public String getStatusNota() { return statusNota; }
    public BigDecimal getQtdItens() { return qtdItens; }
}

/**
 * Parâmetros para relatório de vendas
 */
class RelatorioVendasParametros {
    private Date dataInicio;
    private Date dataFim;
    private BigDecimal codigoVendedor;
    private BigDecimal codigoCliente;
    private String tipoMovimento;
    
    // Getters e setters
    public Date getDataInicio() { return dataInicio; }
    public void setDataInicio(Date dataInicio) { this.dataInicio = dataInicio; }
    
    public Date getDataFim() { return dataFim; }
    public void setDataFim(Date dataFim) { this.dataFim = dataFim; }
    
    public BigDecimal getCodigoVendedor() { return codigoVendedor; }
    public void setCodigoVendedor(BigDecimal codigoVendedor) { this.codigoVendedor = codigoVendedor; }
    
    public BigDecimal getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(BigDecimal codigoCliente) { this.codigoCliente = codigoCliente; }
    
    public String getTipoMovimento() { return tipoMovimento; }
    public void setTipoMovimento(String tipoMovimento) { this.tipoMovimento = tipoMovimento; }
}
```

### **2. Templates de Relatórios**

#### **Template de Relatório de Vendas (relatorio_vendas.jrxml)**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://jasperreports.sourceforge.net/jasperreports
              http://jasperreports.sourceforge.net/xsd/jasperreport.xsd"
              name="relatorio_vendas"
              pageWidth="595"
              pageHeight="842"
              columnWidth="555"
              leftMargin="20"
              rightMargin="20"
              topMargin="20"
              bottomMargin="20">

    <!-- Parâmetros -->
    <parameter name="DATA_INICIO" class="java.util.Date"/>
    <parameter name="DATA_FIM" class="java.util.Date"/>
    <parameter name="CODVEN" class="java.math.BigDecimal"/>
    <parameter name="CODPARC" class="java.math.BigDecimal"/>
    <parameter name="TIPMOV" class="java.lang.String"/>
    <parameter name="REPORT_TITLE" class="java.lang.String"/>
    <parameter name="GENERATED_BY" class="java.lang.String"/>
    <parameter name="GENERATED_AT" class="java.util.Date"/>

    <!-- Campos -->
    <field name="nunota" class="java.math.BigDecimal"/>
    <field name="dtemissao" class="java.util.Date"/>
    <field name="vlrnota" class="java.math.BigDecimal"/>
    <field name="nomeCliente" class="java.lang.String"/>
    <field name="nomeVendedor" class="java.lang.String"/>
    <field name="statusNota" class="java.lang.String"/>
    <field name="qtdItens" class="java.math.BigDecimal"/>

    <!-- Variáveis -->
    <variable name="TOTAL_VENDAS" class="java.math.BigDecimal" calculation="Sum">
        <variableExpression><![CDATA[$F{vlrnota}]]></variableExpression>
    </variable>
    
    <variable name="TOTAL_PEDIDOS" class="java.lang.Integer" calculation="Count">
        <variableExpression><![CDATA[$F{nunota}]]></variableExpression>
    </variable>

    <!-- Título -->
    <title>
        <band height="50">
            <staticText>
                <reportElement x="0" y="0" width="555" height="30"/>
                <textElement textAlignment="Center">
                    <font size="18" isBold="true"/>
                </textElement>
                <text><![CDATA[Relatório de Vendas]]></text>
            </staticText>
            
            <textField>
                <reportElement x="0" y="30" width="555" height="20"/>
                <textElement textAlignment="Center">
                    <font size="12"/>
                </textElement>
                <textFieldExpression><![CDATA["Período: " + $P{DATA_INICIO} + " a " + $P{DATA_FIM}]]></textFieldExpression>
            </textField>
        </band>
    </title>

    <!-- Cabeçalho das colunas -->
    <columnHeader>
        <band height="30">
            <staticText>
                <reportElement x="0" y="0" width="80" height="20"/>
                <textElement>
                    <font size="10" isBold="true"/>
                </textElement>
                <text><![CDATA[Número]]></text>
            </staticText>
            
            <staticText>
                <reportElement x="80" y="0" width="80" height="20"/>
                <textElement>
                    <font size="10" isBold="true"/>
                </textElement>
                <text><![CDATA[Data]]></text>
            </staticText>
            
            <staticText>
                <reportElement x="160" y="0" width="150" height="20"/>
                <textElement>
                    <font size="10" isBold="true"/>
                </textElement>
                <text><![CDATA[Cliente]]></text>
            </staticText>
            
            <staticText>
                <reportElement x="310" y="0" width="120" height="20"/>
                <textElement>
                    <font size="10" isBold="true"/>
                </textElement>
                <text><![CDATA[Vendedor]]></text>
            </staticText>
            
            <staticText>
                <reportElement x="430" y="0" width="60" height="20"/>
                <textElement>
                    <font size="10" isBold="true"/>
                </textElement>
                <text><![CDATA[Itens]]></text>
            </staticText>
            
            <staticText>
                <reportElement x="490" y="0" width="65" height="20"/>
                <textElement textAlignment="Right">
                    <font size="10" isBold="true"/>
                </textElement>
                <text><![CDATA[Valor]]></text>
            </staticText>
        </band>
    </columnHeader>

    <!-- Detalhes -->
    <detail>
        <band height="20">
            <textField>
                <reportElement x="0" y="0" width="80" height="15"/>
                <textElement>
                    <font size="9"/>
                </textElement>
                <textFieldExpression><![CDATA[$F{nunota}]]></textFieldExpression>
            </textField>
            
            <textField pattern="dd/MM/yyyy">
                <reportElement x="80" y="0" width="80" height="15"/>
                <textElement>
                    <font size="9"/>
                </textElement>
                <textFieldExpression><![CDATA[$F{dtemissao}]]></textFieldExpression>
            </textField>
            
            <textField>
                <reportElement x="160" y="0" width="150" height="15"/>
                <textElement>
                    <font size="9"/>
                </textElement>
                <textFieldExpression><![CDATA[$F{nomeCliente}]]></textFieldExpression>
            </textField>
            
            <textField>
                <reportElement x="310" y="0" width="120" height="15"/>
                <textElement>
                    <font size="9"/>
                </textElement>
                <textFieldExpression><![CDATA[$F{nomeVendedor}]]></textFieldExpression>
            </textField>
            
            <textField>
                <reportElement x="430" y="0" width="60" height="15"/>
                <textElement textAlignment="Center">
                    <font size="9"/>
                </textElement>
                <textFieldExpression><![CDATA[$F{qtdItens}]]></textFieldExpression>
            </textField>
            
            <textField pattern="R$ #,##0.00">
                <reportElement x="490" y="0" width="65" height="15"/>
                <textElement textAlignment="Right">
                    <font size="9"/>
                </textElement>
                <textFieldExpression><![CDATA[$F{vlrnota}]]></textFieldExpression>
            </textField>
        </band>
    </detail>

    <!-- Rodapé com totais -->
    <summary>
        <band height="60">
            <line>
                <reportElement x="0" y="0" width="555" height="1"/>
            </line>
            
            <staticText>
                <reportElement x="0" y="10" width="100" height="20"/>
                <textElement>
                    <font size="10" isBold="true"/>
                </textElement>
                <text><![CDATA[Total de Pedidos:]]></text>
            </staticText>
            
            <textField>
                <reportElement x="100" y="10" width="80" height="20"/>
                <textElement>
                    <font size="10" isBold="true"/>
                </textElement>
                <textFieldExpression><![CDATA[$V{TOTAL_PEDIDOS}]]></textFieldExpression>
            </textField>
            
            <staticText>
                <reportElement x="300" y="10" width="100" height="20"/>
                <textElement>
                    <font size="10" isBold="true"/>
                </textElement>
                <text><![CDATA[Total de Vendas:]]></text>
            </staticText>
            
            <textField pattern="R$ #,##0.00">
                <reportElement x="400" y="10" width="100" height="20"/>
                <textElement textAlignment="Right">
                    <font size="10" isBold="true"/>
                </textElement>
                <textFieldExpression><![CDATA[$V{TOTAL_VENDAS}]]></textFieldExpression>
            </textField>
            
            <textField>
                <reportElement x="0" y="35" width="555" height="15"/>
                <textElement textAlignment="Center">
                    <font size="8"/>
                </textElement>
                <textFieldExpression><![CDATA["Relatório gerado em " + $P{GENERATED_AT} + " por " + $P{GENERATED_BY}]]></textFieldExpression>
            </textField>
        </band>
    </summary>

</jasperReport>
```

## 🎯 **Boas Práticas dos Relatórios**

### **1. Performance**
- **Compilação**: Compile templates uma vez e reutilize
- **Consultas**: Otimize queries para relatórios
- **Cache**: Use cache para dados frequentes
- **Paginação**: Implemente paginação para grandes volumes

### **2. Design**
- **Layout**: Use layouts consistentes
- **Cores**: Mantenha paleta de cores profissional
- **Fontes**: Use fontes legíveis e apropriadas
- **Espaçamento**: Mantenha espaçamento adequado

### **3. Funcionalidade**
- **Parâmetros**: Use parâmetros para flexibilidade
- **Filtros**: Implemente filtros apropriados
- **Agrupamentos**: Use agrupamentos para organização
- **Totais**: Calcule totais e subtotais

### **4. Manutenibilidade**
- **Templates**: Mantenha templates organizados
- **Documentação**: Documente templates complexos
- **Versionamento**: Controle versões de templates
- **Testes**: Teste templates regularmente

## 🎊 **Conclusão**

Os relatórios iReport demonstram:

- **✅ Geração Dinâmica**: Relatórios gerados dinamicamente
- **✅ Múltiplos Formatos**: PDF, Excel, HTML
- **✅ Templates Flexíveis**: Parâmetros e filtros
- **✅ Performance**: Otimizado para grandes volumes
- **✅ Design Profissional**: Layouts bem estruturados
- **✅ Funcionalidade Rica**: Totais, agrupamentos, gráficos

### **Benefícios:**
- **Flexibilidade**: Relatórios adaptáveis
- **Performance**: Geração rápida
- **Qualidade**: Design profissional
- **Manutenibilidade**: Fácil de manter
- **Escalabilidade**: Suporte a crescimento

---

*Este documento apresenta implementações avançadas de relatórios usando iReport no Sankhya, fornecendo soluções enterprise para geração de relatórios dinâmicos e profissionais.*
