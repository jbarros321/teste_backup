package br.com.sankhya.portfolio.venda.base;

import java.math.BigDecimal;

public class RespostaContrato {

	private BigDecimal	codPerg;
	private BigDecimal	codQuest;
	private BigDecimal	codRespContratado;
	private BigDecimal	codRespNao;

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

	public BigDecimal getCodRespContratado() {
		return codRespContratado;
	}

	public void setCodRespContratado(BigDecimal codRespContratado) {
		this.codRespContratado = codRespContratado;
	}

	public BigDecimal getCodRespNao() {
		return codRespNao;
	}

	public void setCodRespNao(BigDecimal codRespNao) {
		this.codRespNao = codRespNao;
	}
}
