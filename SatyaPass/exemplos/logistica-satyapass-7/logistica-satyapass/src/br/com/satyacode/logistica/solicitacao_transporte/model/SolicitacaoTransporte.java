package br.com.satyacode.logistica.solicitacao_transporte.model;

import java.math.BigDecimal;

public class SolicitacaoTransporte {

    private BigDecimal nroUnico;
    private BigDecimal codVeiculo;

    private BigDecimal codMotorista;

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

    @Override
    public String toString() {
        return "SolicitacaoTransporte{" +
                "nroUnico=" + nroUnico +
                ", codVeiculo=" + codVeiculo +
                ", codMotorista=" + codMotorista +
                '}';
    }
}
