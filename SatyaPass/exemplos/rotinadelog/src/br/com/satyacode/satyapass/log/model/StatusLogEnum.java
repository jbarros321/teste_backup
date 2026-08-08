package br.com.satyacode.satyapass.log.model;

public enum StatusLogEnum {
    SUCESSO("S"),
    INCONSISTENCIA("I");

    private final String value;

    StatusLogEnum(String v) {
        value = v;
    }

    public String getValue() {
        return value;
    }

}
