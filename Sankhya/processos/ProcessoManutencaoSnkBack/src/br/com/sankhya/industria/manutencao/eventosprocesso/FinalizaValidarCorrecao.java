package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;

import com.sankhya.util.BigDecimalUtil;

public class FinalizaValidarCorrecao implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] solicitacoes = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoes.length > 0) {
			Registro solicitacao = solicitacoes[0];

			BigDecimal numOS = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("NUMOS"));
			ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(), "AD_TWFSOLIMAN", solicitacao, new String [] {
				"__PADRAO__:SOLUCAO:###Detalhes de validao da manuteno:\nCAMPO[VALIDACAOSOLIMAN]"
			});

			String validada = (String) solicitacao.getCampo("REVAL");

			if ("S".equals(validada)) {
				ManutencaoSnkUtil.finalizaItemUsuario(numOS, ctx.getUsuarioLogado(), true);
			}
		}
	}
}
