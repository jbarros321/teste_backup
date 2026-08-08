package br.com.satyacode.satyapass.log.model;

public enum StatusItemEnum {
    OK("OK"),
    INFO("INFO"),
    ERRO("ERRO");

    private final String value;

    StatusItemEnum(String v) {
        value = v;
    }

    public String getValue() {
        return value;
    }

}
