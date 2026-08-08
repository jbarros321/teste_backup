package br.com.denver.tsl.model.dto;

import java.util.Date;
import java.util.Objects;

public class ExpedicaoDTO {
    private String cnpj, ordemFrete, numeroPedido, itemPedido, codigoProduto, numeroPalete, quantidade, peso, lote, cnpjCliente;
    private Date dataFabricacaoDe, dataFabricacaoAte;

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getOrdemFrete() { return ordemFrete; }
    public void setOrdemFrete(String ordemFrete) { this.ordemFrete = ordemFrete; }
    public String getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }
    public String getItemPedido() { return itemPedido; }
    public void setItemPedido(String itemPedido) { this.itemPedido = itemPedido; }
    public String getCodigoProduto() { return codigoProduto; }
    public void setCodigoProduto(String codigoProduto) { this.codigoProduto = codigoProduto; }
    public String getNumeroPalete() { return numeroPalete; }
    public void setNumeroPalete(String numeroPalete) { this.numeroPalete = numeroPalete; }
    public String getQuantidade() { return quantidade; }
    public void setQuantidade(String quantidade) { this.quantidade = quantidade; }
    public String getPeso() { return peso; }
    public void setPeso(String peso) { this.peso = peso; }
    public Date getDataFabricacaoDe() { return dataFabricacaoDe; }
    public void setDataFabricacaoDe(Date dataFabricacaoDe) { this.dataFabricacaoDe = dataFabricacaoDe; }
    public Date getDataFabricacaoAte() { return dataFabricacaoAte; }
    public void setDataFabricacaoAte(Date dataFabricacaoAte) { this.dataFabricacaoAte = dataFabricacaoAte; }
    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }
    public String getCnpjCliente() { return cnpjCliente; }
    public void setCnpjCliente(String cnpjCliente) { this.cnpjCliente = cnpjCliente; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpedicaoDTO that = (ExpedicaoDTO) o;
        return Objects.equals(cnpj, that.cnpj) &&
               Objects.equals(numeroPedido, that.numeroPedido) &&
               Objects.equals(itemPedido, that.itemPedido) &&
               Objects.equals(codigoProduto, that.codigoProduto) &&
               Objects.equals(lote, that.lote) &&
               Objects.equals(quantidade, that.quantidade) &&
               Objects.equals(peso, that.peso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cnpj, numeroPedido, itemPedido, codigoProduto, lote, quantidade, peso);
    }
}
