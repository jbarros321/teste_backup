package br.com.denver.tsl.model.dto;

import java.util.Date;
import java.util.Objects;

public class RecebimentoDTO {
    private String cnpj, notaFiscal, itemNotaFiscal, numeroPalete, codigoProduto, identificadorCaixa, pesoCaixa, lote, infoComplementar, valorUnitario;
    private Date dataProducao, dataVencimento;

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getNotaFiscal() { return notaFiscal; }
    public void setNotaFiscal(String notaFiscal) { this.notaFiscal = notaFiscal; }
    public String getItemNotaFiscal() { return itemNotaFiscal; }
    public void setItemNotaFiscal(String itemNotaFiscal) { this.itemNotaFiscal = itemNotaFiscal; }
    public String getNumeroPalete() { return numeroPalete; }
    public void setNumeroPalete(String numeroPalete) { this.numeroPalete = numeroPalete; }
    public String getCodigoProduto() { return codigoProduto; }
    public void setCodigoProduto(String codigoProduto) { this.codigoProduto = codigoProduto; }
    public String getIdentificadorCaixa() { return identificadorCaixa; }
    public void setIdentificadorCaixa(String identificadorCaixa) { this.identificadorCaixa = identificadorCaixa; }
    public String getPesoCaixa() { return pesoCaixa; }
    public void setPesoCaixa(String pesoCaixa) { this.pesoCaixa = pesoCaixa; }
    public Date getDataProducao() { return dataProducao; }
    public void setDataProducao(Date dataProducao) { this.dataProducao = dataProducao; }
    public Date getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(Date dataVencimento) { this.dataVencimento = dataVencimento; }
    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }
    public String getInfoComplementar() { return infoComplementar; }
    public void setInfoComplementar(String infoComplementar) { this.infoComplementar = infoComplementar; }
    public String getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(String valorUnitario) { this.valorUnitario = valorUnitario; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecebimentoDTO that = (RecebimentoDTO) o;
        return Objects.equals(cnpj, that.cnpj) &&
               Objects.equals(notaFiscal, that.notaFiscal) &&
               Objects.equals(itemNotaFiscal, that.itemNotaFiscal) &&
               Objects.equals(codigoProduto, that.codigoProduto) &&
               Objects.equals(lote, that.lote) &&
               Objects.equals(identificadorCaixa, that.identificadorCaixa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cnpj, notaFiscal, itemNotaFiscal, codigoProduto, lote, identificadorCaixa);
    }
}
