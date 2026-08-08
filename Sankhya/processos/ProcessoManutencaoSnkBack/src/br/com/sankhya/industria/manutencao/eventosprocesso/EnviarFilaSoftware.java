package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

public class EnviarFilaSoftware implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro [] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];

			BigDecimal proxFila = new BigDecimal(186);
			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");
			BigDecimal codUsu = ManutencaoSnkUtil.getUsuarioUltimoItemOS(numOS);
			BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);

			Map<String, Object> props = new HashMap<String, Object>();
			props.put("CODSERV", new BigDecimal(50506));
			props.put("CODSIT", new BigDecimal(1));

			OrdemServicoAPI.encaminhaFila(numOS, numItem, codUsu, proxFila, props);
		}
	}
}
