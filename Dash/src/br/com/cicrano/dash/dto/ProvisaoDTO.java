package br.com.cicrano.dash.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class ProvisaoDTO {
    private Date data;
    private BigDecimal valor;

    public ProvisaoDTO(Date data, BigDecimal valor) {
        this.data = data;
        this.valor = valor != null ? valor : BigDecimal.ZERO;
    }

    public Date getData() {
        return data;
    }

    public BigDecimal getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProvisaoDTO that = (ProvisaoDTO) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}











