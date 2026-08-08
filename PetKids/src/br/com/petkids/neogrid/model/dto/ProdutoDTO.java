package br.com.petkids.neogrid.model.dto;

import java.math.BigDecimal;

public class ProdutoDTO {
    private String cnpjIndustria;
    private String codigoItem;
    private String codigoProduto;
    private String tipoItem;
    private BigDecimal quantidadeEmbalagem;
    private BigDecimal precoTabelaUnidade;
    private String descricaoInterna;
    private String statusProduto;

    public String getCnpjIndustria() { return cnpjIndustria; }
    public void setCnpjIndustria(String cnpjIndustria) { this.cnpjIndustria = cnpjIndustria; }
    public String getCodigoItem() { return codigoItem; }
    public void setCodigoItem(String codigoItem) { this.codigoItem = codigoItem; }
    public String getCodigoProduto() { return codigoProduto; }
    public void setCodigoProduto(String codigoProduto) { this.codigoProduto = codigoProduto; }
    public String getTipoItem() { return tipoItem; }
    public void setTipoItem(String tipoItem) { this.tipoItem = tipoItem; }
    public BigDecimal getQuantidadeEmbalagem() { return quantidadeEmbalagem; }
    public void setQuantidadeEmbalagem(BigDecimal quantidadeEmbalagem) { this.quantidadeEmbalagem = quantidadeEmbalagem; }
    public BigDecimal getPrecoTabelaUnidade() { return precoTabelaUnidade; }
    public void setPrecoTabelaUnidade(BigDecimal precoTabelaUnidade) { this.precoTabelaUnidade = precoTabelaUnidade; }
    public String getDescricaoInterna() { return descricaoInterna; }
    public void setDescricaoInterna(String descricaoInterna) { this.descricaoInterna = descricaoInterna; }
    public String getStatusProduto() { return statusProduto; }
    public void setStatusProduto(String statusProduto) { this.statusProduto = statusProduto; }
}
