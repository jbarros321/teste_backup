package br.com.petkids.neogrid.model.enums;

public enum TipoFaturamento {

    A_VISTA("01", "À Vista"),
    A_PRAZO("02", "A Prazo"),
    OUTROS("03", "Outros");

    private final String valor;
    private final String descricao;

    TipoFaturamento(String valor, String descricao) {
        this.valor = valor;
        this.descricao = descricao;
    }

    public String getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoFaturamento fromString(String valor) {
        if (valor == null) {
            return null;
        }

        for (TipoFaturamento tipo : values()) {
            if (tipo.valor.equals(valor)) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Tipo de faturamento inválido: " + valor);
    }
}
