package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.sankhya.util.BigDecimalUtil;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

public class EnviarFilaTesteEntrada implements EventoProcessoJava {

	public void executar(ContextoEvento ctx) throws Exception {
		Registro [] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];
			String tipoRetorno = (String) linha.getCampo("TIPOPRETORNODETALHE");
			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");

			if(tipoRetorno == null || "ES".indexOf(tipoRetorno) == 0){

				BigDecimal codUsu = ManutencaoSnkUtil.getUsuarioUltimoItemOS(numOS);
				BigDecimal proxFila = new BigDecimal(46);
				BigDecimal idinsttar = (BigDecimal) ctx.getIdInstanceTarefa();
				BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);

				Map<String, Object> props = new HashMap<String, Object>();
				props.put("CODSERV", new BigDecimal(50605));
				props.put("CODSIT", new BigDecimal(3));

				BigDecimal responsavelDemanda = ManutencaoSnkUtil.getExecutanteAnteriorFila(numOS, proxFila);

				if (responsavelDemanda != null) {
					if (codUsu.compareTo(responsavelDemanda) != 0) {

						OrdemServicoAPI.encaminhaOS(numOS, codUsu, responsavelDemanda, props);
					}

					ManutencaoSnkUtil.inserirDonoAtividade(idinsttar, responsavelDemanda.toString());
				} else {
					OrdemServicoAPI.encaminhaFila(numOS, numItem, codUsu, proxFila, props);
				}
			}else {
				ManutencaoSnkUtil.finalizaItemUsuario(numOS, ctx.getUsuarioLogado(), true);
			}

		}
	}
}
