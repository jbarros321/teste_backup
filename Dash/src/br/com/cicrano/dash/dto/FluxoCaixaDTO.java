package br.com.cicrano.dash.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class FluxoCaixaDTO {
    private Date data;
    private BigDecimal receitas;
    private BigDecimal despesas;

    public FluxoCaixaDTO(Date data, BigDecimal receitas, BigDecimal despesas) {
        this.data = data;
        this.receitas = receitas != null ? receitas : BigDecimal.ZERO;
        this.despesas = despesas != null ? despesas : BigDecimal.ZERO;
    }

    public Date getData() {
        return data;
    }

    public BigDecimal getReceitas() {
        return receitas;
    }

    public BigDecimal getDespesas() {
        return despesas;
    }

    public BigDecimal getSaldo() {
        return receitas.subtract(despesas);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FluxoCaixaDTO that = (FluxoCaixaDTO) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}











