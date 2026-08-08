package br.com.petkids.neogrid.model.dto;

import br.com.petkids.neogrid.model.enums.TipoNF;
import java.math.BigDecimal;

public class ItemVendaDTO {
    private BigDecimal numeroNF;
    private String serieNF;
    private TipoNF tipoNF;
    private String codigoItem;
    private BigDecimal quantidade;
    private BigDecimal valorUnitario;
    private String bonificacao;
    private BigDecimal valorTotalBruto;
    private BigDecimal valorTotalLiquido;
    private BigDecimal valorIPI;
    private BigDecimal valorPisConfins;
    private BigDecimal valorSubstituicaoTributaria;
    private BigDecimal valorICMS;
    private BigDecimal valorDescontos;

    public BigDecimal getNumeroNF() { return numeroNF; }
    public void setNumeroNF(BigDecimal numeroNF) { this.numeroNF = numeroNF; }
    public String getSerieNF() { return serieNF; }
    public void setSerieNF(String serieNF) { this.serieNF = serieNF; }
    public TipoNF getTipoNF() { return tipoNF; }
    public void setTipoNF(TipoNF tipoNF) { this.tipoNF = tipoNF; }
    public String getCodigoItem() { return codigoItem; }
    public void setCodigoItem(String codigoItem) { this.codigoItem = codigoItem; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }
    public String getBonificacao() { return bonificacao; }
    public void setBonificacao(String bonificacao) { this.bonificacao = bonificacao; }
    public BigDecimal getValorTotalBruto() { return valorTotalBruto; }
    public void setValorTotalBruto(BigDecimal valorTotalBruto) { this.valorTotalBruto = valorTotalBruto; }
    public BigDecimal getValorTotalLiquido() { return valorTotalLiquido; }
    public void setValorTotalLiquido(BigDecimal valorTotalLiquido) { this.valorTotalLiquido = valorTotalLiquido; }
    public BigDecimal getValorIPI() { return valorIPI; }
    public void setValorIPI(BigDecimal valorIPI) { this.valorIPI = valorIPI; }
    public BigDecimal getValorPisConfins() { return valorPisConfins; }
    public void setValorPisConfins(BigDecimal valorPisConfins) { this.valorPisConfins = valorPisConfins; }
    public BigDecimal getValorSubstituicaoTributaria() { return valorSubstituicaoTributaria; }
    public void setValorSubstituicaoTributaria(BigDecimal valorSubstituicaoTributaria) { this.valorSubstituicaoTributaria = valorSubstituicaoTributaria; }
    public BigDecimal getValorICMS() { return valorICMS; }
    public void setValorICMS(BigDecimal valorICMS) { this.valorICMS = valorICMS; }
    public BigDecimal getValorDescontos() { return valorDescontos; }
    public void setValorDescontos(BigDecimal valorDescontos) { this.valorDescontos = valorDescontos; }
}
