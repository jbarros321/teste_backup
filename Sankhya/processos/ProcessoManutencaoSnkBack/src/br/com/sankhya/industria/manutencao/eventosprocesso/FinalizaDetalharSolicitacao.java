package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;

import com.sankhya.util.BigDecimalUtil;

public class FinalizaDetalharSolicitacao implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {

		Registro solicitacao = ctx.getLinhasFormulario("AD_TWFSOLIMAN")[0];
		BigDecimal numOS = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("NUMOS"));
		ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(), "AD_TWFSOLIMAN", solicitacao, new String [] {"DETALHEPROBLEMA->SOLUCAO"});

	}
}
