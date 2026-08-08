package br.com.satyacode.logistica.solicitacao_transporte.model;

import java.math.BigDecimal;

public class SolicitacaoParametro {

    private BigDecimal nroUnico;
    private BigDecimal codMotorista;
    private BigDecimal codVeiculo;

    public SolicitacaoParametro() {
    }

    public SolicitacaoParametro(BigDecimal nroUnico, BigDecimal codMotorista, BigDecimal codVeiculo) {
        this.nroUnico = nroUnico;
        this.codMotorista = codMotorista;
        this.codVeiculo = codVeiculo;
    }

    public BigDecimal getNroUnico() {
        return nroUnico;
    }

    public void setNroUnico(BigDecimal nroUnico) {
        this.nroUnico = nroUnico;
    }

    public BigDecimal getCodMotorista() {
        return codMotorista;
    }

    public void setCodMotorista(BigDecimal codMotorista) {
        this.codMotorista = codMotorista;
    }

    public BigDecimal getCodVeiculo() {
        return codVeiculo;
    }

    public void setCodVeiculo(BigDecimal codVeiculo) {
        this.codVeiculo = codVeiculo;
    }
}
