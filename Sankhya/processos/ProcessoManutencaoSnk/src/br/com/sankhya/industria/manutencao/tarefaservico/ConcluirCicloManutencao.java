package br.com.sankhya.industria.manutencao.tarefaservico;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoTarefa;
import br.com.sankhya.extensions.flow.TarefaJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoConstants;

public class ConcluirCicloManutencao implements TarefaJava {
	public void executar(ContextoTarefa ctx) throws Exception {

		Registro [] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];
			linha.setCampo("STATUSCICLOMAN", ManutencaoConstants.CONCLUIDO);
		}
	}
}
