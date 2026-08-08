package br.com.sankhya.portfolio.venda.base;

import java.math.BigDecimal;

public class RespostaContratoMestra {

	private BigDecimal codPerg;
	private BigDecimal codQuest;
	private BigDecimal codRespSim;

	public BigDecimal getCodPerg() {
		return codPerg;
	}
	public void setCodPerg(BigDecimal codPerg) {
		this.codPerg = codPerg;
	}
	public BigDecimal getCodQuest() {
		return codQuest;
	}
	public void setCodQuest(BigDecimal codQuest) {
		this.codQuest = codQuest;
	}
	public BigDecimal getCodRespSim() {
		return codRespSim;
	}
	public void setCodRespSim(BigDecimal codRespSim) {
		this.codRespSim = codRespSim;
	}

	@Override
	public boolean equals(Object obj) {
		RespostaContratoMestra compar = (RespostaContratoMestra) obj;

		return (this.codPerg.equals(compar.getCodPerg())
				&&  this.codQuest.equals(compar.getCodQuest())
				&& this.codRespSim.equals(compar.getCodRespSim()));
	}

}
