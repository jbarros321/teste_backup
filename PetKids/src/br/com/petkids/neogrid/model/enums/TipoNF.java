package br.com.petkids.neogrid.model.enums;

public enum TipoNF {

    VENDAS("01", "Vendas"),
    DEVOLUCAO("02", "Devolução"),
    CANCELAMENTO("03", "Cancelamento");

    private final String valor;
    private final String descricao;

    TipoNF(String valor, String descricao) {
        this.valor = valor;
        this.descricao = descricao;
    }

    public String getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoNF fromString(String valor) {
        if (valor == null) {
            return null;
        }

        for (TipoNF tipo : values()) {
            if (tipo.valor.equals(valor)) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Tipo de NF inválido: " + valor);
    }
}
