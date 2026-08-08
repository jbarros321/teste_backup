package br.com.petkids.neogrid.model.enums;

public enum TipoRelatorio {
    VENDEDORES("RELVEN", "050", "Vendedores"),
    CLIENTES("RELCLI", "050", "Clientes"),
    PRODUTOS("RELPRO", "051", "Produtos"),
    VENDAS("VENDAS", "052", "Vendas"),
    ESTOQUE("RELEST", "050", "Estoque");

    private final String identificacao;
    private final String versao;
    private final String descricao;

    TipoRelatorio(String identificacao, String versao, String descricao) {
        this.identificacao = identificacao;
        this.versao = versao;
        this.descricao = descricao;
    }

    public String getIdentificacao() {
        return identificacao;
    }

    public String getVersao() {
        return versao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoRelatorio fromString(String tipo) {
        if (tipo == null) return null;
        String tipoUpper = tipo.toUpperCase();
        for (TipoRelatorio tr : values()) {
            if (tr.name().equals(tipoUpper) || tr.getIdentificacao().equals(tipoUpper)) {
                return tr;
            }
        }
        throw new IllegalArgumentException("Tipo de relatório inválido: " + tipo);
    }
}
