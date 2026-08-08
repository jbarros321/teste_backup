package br.com.petkids.neogrid.model.enums;

public enum TipoFrete {

    CIF("CIF", "CIF - Custo, Seguro, Frete por conta do vendedor até o destino designado"),
    FOB("FOB", "FOB - Posto a bordo. Porto de embarque designado");

    private final String valor;
    private final String descricao;

    TipoFrete(String valor, String descricao) {
        this.valor = valor;
        this.descricao = descricao;
    }

    public String getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoFrete fromSankhyaValue(String valorSankhya) {
        if (valorSankhya == null || valorSankhya.trim().isEmpty()) {
            return FOB;
        }

        String valorNormalizado = valorSankhya.trim().toUpperCase();

        if (valorNormalizado.equals("C") || valorNormalizado.equals("CIF") ||
            valorNormalizado.startsWith("CIF") || valorNormalizado.contains("CIF")) {
            return CIF;
        }

        if (valorNormalizado.equals("F") || valorNormalizado.equals("FOB") ||
            valorNormalizado.startsWith("FOB") || valorNormalizado.contains("FOB")) {
            return FOB;
        }

        return FOB;
    }

    public static TipoFrete fromNeogridValue(String valorNeogrid) {
        if (valorNeogrid == null) {
            return FOB;
        }

        String valorNormalizado = valorNeogrid.trim().toUpperCase();
        for (TipoFrete tipo : values()) {
            if (tipo.valor.equals(valorNormalizado)) {
                return tipo;
            }
        }

        return FOB;
    }

    @Deprecated
    public static TipoFrete fromNeogridCode(String codigoNeogrid) {
        return fromNeogridValue(codigoNeogrid);
    }
}
