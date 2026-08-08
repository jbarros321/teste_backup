package br.com.petkids.neogrid.model.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class EstoqueDTO {
    private Timestamp dataHoraEstoque;
    private String codigoItem;
    private BigDecimal quantidadeEstoque;
    private BigDecimal quantidadeEstoqueTransito;
    private String cnpjIndustria;

    public Timestamp getDataHoraEstoque() { return dataHoraEstoque; }
    public void setDataHoraEstoque(Timestamp dataHoraEstoque) { this.dataHoraEstoque = dataHoraEstoque; }
    public String getCodigoItem() { return codigoItem; }
    public void setCodigoItem(String codigoItem) { this.codigoItem = codigoItem; }
    public BigDecimal getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(BigDecimal quantidadeEstoque) { this.quantidadeEstoque = quantidadeEstoque; }
    public BigDecimal getQuantidadeEstoqueTransito() { return quantidadeEstoqueTransito; }
    public void setQuantidadeEstoqueTransito(BigDecimal quantidadeEstoqueTransito) { this.quantidadeEstoqueTransito = quantidadeEstoqueTransito; }
    public String getCnpjIndustria() { return cnpjIndustria; }
    public void setCnpjIndustria(String cnpjIndustria) { this.cnpjIndustria = cnpjIndustria; }
}
