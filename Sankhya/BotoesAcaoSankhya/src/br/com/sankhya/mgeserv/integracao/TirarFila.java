package br.com.sankhya.mgeserv.integracao;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class TirarFila implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao contexto) throws Exception {
		BigDecimal numOS = new BigDecimal((Integer) contexto.getParam("NUMOS"));
		BigDecimal codFila = new BigDecimal(46);
		BigDecimal executante = new BigDecimal(3947);
		OrdemServicoAPI.tiraDaFila(numOS, codFila, executante);

	}

}
