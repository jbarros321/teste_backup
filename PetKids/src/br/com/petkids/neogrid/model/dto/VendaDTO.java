package br.com.petkids.neogrid.model.dto;

import br.com.petkids.neogrid.model.enums.TipoFaturamento;
import br.com.petkids.neogrid.model.enums.TipoFrete;
import br.com.petkids.neogrid.model.enums.TipoNF;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class VendaDTO {
    private TipoFaturamento tipoFaturamento;
    private BigDecimal numeroNF;
    private String serieNF;
    private TipoNF tipoNF;
    private Timestamp dataEmissao;
    private String codigoCliente;
    private String codigoVendedor;
    private String ufEmissor;
    private String cepEmissor;
    private String ufDestinatario;
    private String cepDestinatario;
    private TipoFrete tipoFrete;
    private BigDecimal diasPagamento;
    private BigDecimal metodoVenda;
    private String cnpjIndustria;
    private List<ItemVendaDTO> itens;

    public VendaDTO() { this.itens = new ArrayList<>(); }

    public TipoFaturamento getTipoFaturamento() { return tipoFaturamento; }
    public void setTipoFaturamento(TipoFaturamento tipoFaturamento) { this.tipoFaturamento = tipoFaturamento; }
    public BigDecimal getNumeroNF() { return numeroNF; }
    public void setNumeroNF(BigDecimal numeroNF) { this.numeroNF = numeroNF; }
    public String getSerieNF() { return serieNF; }
    public void setSerieNF(String serieNF) { this.serieNF = serieNF; }
    public TipoNF getTipoNF() { return tipoNF; }
    public void setTipoNF(TipoNF tipoNF) { this.tipoNF = tipoNF; }
    public Timestamp getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(Timestamp dataEmissao) { this.dataEmissao = dataEmissao; }
    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String codigoCliente) { this.codigoCliente = codigoCliente; }
    public String getCodigoVendedor() { return codigoVendedor; }
    public void setCodigoVendedor(String codigoVendedor) { this.codigoVendedor = codigoVendedor; }
    public String getUfEmissor() { return ufEmissor; }
    public void setUfEmissor(String ufEmissor) { this.ufEmissor = ufEmissor; }
    public String getCepEmissor() { return cepEmissor; }
    public void setCepEmissor(String cepEmissor) { this.cepEmissor = cepEmissor; }
    public String getUfDestinatario() { return ufDestinatario; }
    public void setUfDestinatario(String ufDestinatario) { this.ufDestinatario = ufDestinatario; }
    public String getCepDestinatario() { return cepDestinatario; }
    public void setCepDestinatario(String cepDestinatario) { this.cepDestinatario = cepDestinatario; }
    public TipoFrete getTipoFrete() { return tipoFrete; }
    public void setTipoFrete(TipoFrete tipoFrete) { this.tipoFrete = tipoFrete; }
    public BigDecimal getDiasPagamento() { return diasPagamento; }
    public void setDiasPagamento(BigDecimal diasPagamento) { this.diasPagamento = diasPagamento; }
    public BigDecimal getMetodoVenda() { return metodoVenda; }
    public void setMetodoVenda(BigDecimal metodoVenda) { this.metodoVenda = metodoVenda; }
    public String getCnpjIndustria() { return cnpjIndustria; }
    public void setCnpjIndustria(String cnpjIndustria) { this.cnpjIndustria = cnpjIndustria; }
    public List<ItemVendaDTO> getItens() { return itens; }
    public void setItens(List<ItemVendaDTO> itens) { this.itens = itens; }
    public void addItem(ItemVendaDTO item) { this.itens.add(item); }
}
