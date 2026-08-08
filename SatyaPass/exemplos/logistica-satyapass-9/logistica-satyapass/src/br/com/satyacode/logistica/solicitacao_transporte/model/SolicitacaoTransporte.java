package br.com.satyacode.logistica.solicitacao_transporte.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SolicitacaoTransporte {

    private BigDecimal nroUnico;
    private BigDecimal codVeiculo;
    private BigDecimal codMotorista;

    private String status;

    private Timestamp dataEntrega;

    public BigDecimal getNroUnico() {
        return nroUnico;
    }

    public void setNroUnico(BigDecimal nroUnico) {
        this.nroUnico = nroUnico;
    }

    public BigDecimal getCodVeiculo() {
        return codVeiculo;
    }

    public void setCodVeiculo(BigDecimal codVeiculo) {
        this.codVeiculo = codVeiculo;
    }

    public BigDecimal getCodMotorista() {
        return codMotorista;
    }

    public void setCodMotorista(BigDecimal codMotorista) {
        this.codMotorista = codMotorista;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(Timestamp dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    @Override
    public String toString() {
        return "SolicitacaoTransporte{" +
                "nroUnico=" + nroUnico +
                ", codVeiculo=" + codVeiculo +
                ", codMotorista=" + codMotorista +
                ", status='" + status + '\'' +
                ", dataEntrega=" + dataEntrega +
                '}';
    }
}
