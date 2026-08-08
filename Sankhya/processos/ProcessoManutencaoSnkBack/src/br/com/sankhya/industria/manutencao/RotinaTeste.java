package br.com.sankhya.industria.manutencao;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

public class RotinaTeste implements AcaoRotinaJava, EventoProcessoJava {

	public void doAction(ContextoAcao ctx) throws Exception {
		BigDecimal numOS = new BigDecimal((Integer) ctx.getParam("NUMOS"));
		BigDecimal itemFechar = new BigDecimal((Integer) ctx.getParam("ITEM"));
		BigDecimal executante = new BigDecimal((Integer) ctx.getParam("EXECUTANTE"));
		BigDecimal servico = new BigDecimal((Integer) ctx.getParam("CODSERV"));

		Map<String, Object> props = new HashMap<String, Object>();
		props.put("CODSERV", servico);
		props.put("CODSIT", new BigDecimal(3));
		props.put("CODUSU", executante);

		BigDecimal codigoCompiladorWeb = new BigDecimal(1721);

		AuthenticationInfo oldUserAuthentication = AuthenticationInfo.getCurrentOrNull();
		try{
			new AuthenticationInfo("", codigoCompiladorWeb, null, 0).makeCurrent();
			OrdemServicoAPI.encaminhaOS(numOS, codigoCompiladorWeb, codigoCompiladorWeb, props);
		}finally{
			if(oldUserAuthentication != null){
				oldUserAuthentication.makeCurrent();
			}
		}
	}

	public void executar(ContextoEvento contexto) throws Exception {
		OrdemServicoAPI.encaminhaFila(new BigDecimal(1505162), new BigDecimal(10), new BigDecimal(4663), new BigDecimal(1721));
	}
}
