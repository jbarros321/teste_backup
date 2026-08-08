package br.com.satyacode.satyapass.log.model;

public enum ModalidadeEnum {
    BOTAO_ACAO("BT"),
    ACAO_AGENDADA("AG");

    private final String value;

    ModalidadeEnum(String v) {
        value = v;
    }

    public String getValue() {
        return value;
    }

}
