package br.com.satyacode.satyapass.log.model;

public enum StatusExecucaoEnum {
    EXECUTADO("E"),
    EM_ANDAMENTO("A");

    private final String value;

    StatusExecucaoEnum(String v) {
        value = v;
    }

    public String getValue() {
        return value;
    }

}
