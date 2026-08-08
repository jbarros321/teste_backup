package br.com.satyacode.logistica.historico.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Historico {
    private BigDecimal nroUnico;
    private BigDecimal sequencia;
    private Timestamp data;
    private String ocorrencia;

    public Historico(){

    }

    public Historico(BigDecimal nroUnico, String ocorrencia) {
        this.nroUnico = nroUnico;
        this.ocorrencia = ocorrencia;
    }

    public Historico(BigDecimal nroUnico, Timestamp data, String ocorrencia) {
        this.nroUnico = nroUnico;
        this.data = data;
        this.ocorrencia = ocorrencia;
    }

    public BigDecimal getNroUnico() {
        return nroUnico;
    }

    public void setNroUnico(BigDecimal nroUnico) {
        this.nroUnico = nroUnico;
    }

    public BigDecimal getSequencia() {
        return sequencia;
    }

    public void setSequencia(BigDecimal sequencia) {
        this.sequencia = sequencia;
    }

    public Timestamp getData() {
        return data;
    }

    public void setData(Timestamp data) {
        this.data = data;
    }

    public String getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(String ocorrencia) {
        this.ocorrencia = ocorrencia;
    }

    @Override
    public String toString() {
        return "Historico{" +
                "nroUnico=" + nroUnico +
                ", sequencia=" + sequencia +
                ", data=" + data +
                ", ocorrencia='" + ocorrencia + '\'' +
                '}';
    }
}
