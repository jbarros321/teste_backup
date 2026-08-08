package br.com.sankhya.mgeserv.integracao;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.sql.NativeSql;

public class EncaminharFila implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao contexto) throws Exception {
		BigDecimal numOS = new BigDecimal((Integer) contexto.getParam("NUMOS"));
		BigDecimal codFila = new BigDecimal(46);
		BigDecimal executante = new BigDecimal(3947);
		BigDecimal numItem = NativeSql.getBigDecimal("NUMITEM", "TCSITE", "NUMOS = " + numOS + " AND CODUSU = " + executante + " AND INICEXEC IS NULL AND HRINICIAL IS NULL AND HRFINAL IS NULL ORDER BY NUMITEM DESC");
		OrdemServicoAPI.encaminhaFila(numOS, numItem, executante, codFila);
	}

}
