package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

import com.sankhya.util.BigDecimalUtil;

public class FinalizaValidarCorrecao implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] solicitacoes = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoes.length > 0) {
			Registro solicitacao = solicitacoes[0];

			BigDecimal numOS = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("NUMOS"));
			ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(), "AD_TWFSOLIMAN", solicitacao, new String [] {
				"__PADRAO__:SOLUCAO:###Solicitante validou a correo?:\nCAMPO[REVAL]\n\n###Detalhes de validao da manuteno:\nCAMPO[VALIDACAOSOLIMAN]"
			});

			String validada = (String) solicitacao.getCampo("REVAL");

			if ("S".equals(validada)) {
				ManutencaoSnkUtil.finalizaItemUsuario(numOS, ctx.getUsuarioLogado(), true);
			}else {

				Map<String, Object> props = new HashMap<String, Object>();
				props.put("CODSERV", new BigDecimal(50605));
				props.put("CODSIT", new BigDecimal(3));

				BigDecimal proxFila = new BigDecimal(46);
				BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, ctx.getUsuarioLogado(), false);

				OrdemServicoAPI.encaminhaFila(numOS, numItem, ctx.getUsuarioLogado(), proxFila, props);

			}
		}
	}
}
