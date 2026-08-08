package br.com.sankhya.industria.manutencao.eventosprocesso;

import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;

public class ExcluirApontamento implements EventoProcessoJava {

	public void executar(ContextoEvento ctx) throws Exception {
		throw new IllegalArgumentException("__PRETTY_MSG__A excluso de apontamento  proibida no processo de Manuteno. Invalide o apontamento preenchendo \"Dh. inicial\" igual  \"Dh. final\".");
	}
}
