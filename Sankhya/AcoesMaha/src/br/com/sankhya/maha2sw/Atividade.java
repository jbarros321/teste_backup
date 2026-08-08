package br.com.sankhya.maha2sw;

import java.util.List;

class Atividade {
	private int					numero;
	private int					codigo_sankhya;
	private List<Procedimento>	procedimentos;

	public int getNumero() {
		return this.numero;
	}

	public int getCodigoSankhya() {
		return this.codigo_sankhya;
	}

	public List<Procedimento> getProcedimentos() {
		return this.procedimentos;
	}
}
