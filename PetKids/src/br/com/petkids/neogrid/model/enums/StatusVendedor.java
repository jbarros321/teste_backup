package br.com.petkids.neogrid.model.enums;

public enum StatusVendedor {

    ATIVO("A", "Ativo"),
    INATIVO("I", "Inativo");

    private final String valor;
    private final String descricao;

    StatusVendedor(String valor, String descricao) {
        this.valor = valor;
        this.descricao = descricao;
    }

    public String getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public static StatusVendedor fromString(String valor) {
        if (valor == null) {
            return null;
        }

        String valorUpper = valor.toUpperCase();
        for (StatusVendedor status : values()) {
            if (status.valor.equals(valorUpper)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Status de vendedor inválido: " + valor);
    }

    public static StatusVendedor fromAtivo(String ativo) {
        if (ativo == null) {
            return INATIVO;
        }
        return "S".equalsIgnoreCase(ativo) ? ATIVO : INATIVO;
    }
}
