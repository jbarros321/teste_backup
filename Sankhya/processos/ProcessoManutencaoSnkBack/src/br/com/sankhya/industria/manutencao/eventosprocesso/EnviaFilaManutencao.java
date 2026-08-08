package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

public class EnviaFilaManutencao implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro [] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];

			BigDecimal idinsttar = (BigDecimal) ctx.getIdInstanceTarefa();
			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");
			BigDecimal codUsu = ManutencaoSnkUtil.getUsuarioUltimoItemOS(numOS);
			BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);

			Map<String, Object> props = new HashMap<String, Object>();
			props.put("CODSERV", new BigDecimal(50601));
			props.put("CODSIT", new BigDecimal(2));

			BigDecimal proxFila = new BigDecimal(176);
			BigDecimal responsavelDemanda = ManutencaoSnkUtil.getExecutanteAnteriorFila(numOS, proxFila);

			if (responsavelDemanda == null) {
				OrdemServicoAPI.encaminhaFila(numOS, numItem, codUsu, proxFila, props);
			}
		}
	}
}
