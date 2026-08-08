package br.com.sankhya.industria.manutencao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.workflow.api.SankhyaFlow;

public class EnviaSinal implements AcaoRotinaJava {

	public void doAction(ContextoAcao ctx) throws Exception {
		String name = (String) ctx.getParam("NOMESINAL");
		String idInstPrn = (String) ctx.getParam("IDINSTPRN");
		SankhyaFlow.sendSignal(name, new String [] {idInstPrn});
	}

}
