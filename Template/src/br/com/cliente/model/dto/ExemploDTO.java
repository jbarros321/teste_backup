package br.com.cliente.model.dto;
import java.util.Date;
import java.util.Objects;

public class ExemploDTO {
    private String cnpj, codigoProduto, descricao;
    private Date dataEmissao;

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getCodigoProduto() { return codigoProduto; }
    public void setCodigoProduto(String codigoProduto) { this.codigoProduto = codigoProduto; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Date getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(Date dataEmissao) { this.dataEmissao = dataEmissao; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExemploDTO that = (ExemploDTO) o;
        return Objects.equals(cnpj, that.cnpj) && Objects.equals(codigoProduto, that.codigoProduto);
    }

    @Override
    public int hashCode() { return Objects.hash(cnpj, codigoProduto); }
}
