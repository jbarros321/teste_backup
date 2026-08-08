package br.com.sankhya.maha2sw;

import java.util.List;

class Processo {
	private int				numero;
	private int				codigo_sankhya;
	private List<Atividade>	atividades;

	public int getNumero() {
		return this.numero;
	}

	public int getCodigoSankhya() {
		return this.codigo_sankhya;
	}

	public List<Atividade> getAtividades() {
		return this.atividades;
	}
}
