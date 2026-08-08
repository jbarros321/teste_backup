package br.com.sankhya.portfolio.venda.base;

import java.math.BigDecimal;

public class RespostaDadosBasicos {

	private BigDecimal	codPerg;
	private BigDecimal	codQuest;
	private BigDecimal	codRespNaoOrVendaBase;
	private BigDecimal  codRespTipoVendaContrato;

	public BigDecimal getCodPerg() {
		return codPerg;
	}
	public void setCodPerg(BigDecimal codPerg) {
		this.codPerg = codPerg;
	}
	public BigDecimal getCodQuest() {
		return codQuest;
	}
	public BigDecimal getCodRespTipoVendaContrato() {
		return codRespTipoVendaContrato;
	}
	public void setCodRespTipoVendaContrato(BigDecimal codRespTipoVendaContrato) {
		this.codRespTipoVendaContrato = codRespTipoVendaContrato;
	}
	public void setCodQuest(BigDecimal codQuest) {
		this.codQuest = codQuest;
	}
	public BigDecimal getCodRespNaoOrVendaBase() {
		return codRespNaoOrVendaBase;
	}
	public void setCodRespNaoOrVendaBase(BigDecimal codRespNaoOrVendaBase) {
		this.codRespNaoOrVendaBase = codRespNaoOrVendaBase;
	}

	@Override
	public boolean equals (Object obj) {
		RespostaDadosBasicos objRD = (RespostaDadosBasicos) obj;

		return (this.codPerg.equals(objRD.getCodPerg())
				&&  this.codQuest.equals(objRD.getCodQuest())
				&& this.codRespNaoOrVendaBase.equals(objRD.getCodRespNaoOrVendaBase())
				&& this.codRespTipoVendaContrato.equals(objRD.getCodRespTipoVendaContrato())
				);
	}

}
