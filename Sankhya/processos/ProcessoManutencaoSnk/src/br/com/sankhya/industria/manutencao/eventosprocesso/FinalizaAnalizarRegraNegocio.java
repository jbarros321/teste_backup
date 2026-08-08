package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;

import com.sankhya.util.BigDecimalUtil;

public class FinalizaAnalizarRegraNegocio implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {

		Registro[] solicitacoes = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoes.length > 0) {

			Registro solicitacao = solicitacoes[0];
			Registro [] linhasAN = ctx.getLinhasFormulario("AD_TWFANALINEG");
			Registro analiseNegocio = ManutencaoSnkUtil.getMaxRegistroFormulario(linhasAN);
			solicitacao.setCampo("CODUSUANALISE", ctx.getUsuarioLogado());
			ManutencaoSnkUtil.updateSoliman(analiseNegocio, solicitacao, new String [] {
				"REGRANEGOCIO",
				"TIPORETORNOANA",
				"A:AD_TWFANALINEG:ANEXOANALISTA",
				"TIPORETORNOANA->TIPOSOLUCAO"
			});

			BigDecimal numOS = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("NUMOS"));
			ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(),"AD_TWFANALINEG", analiseNegocio, new String [] {
				"__PADRAO__:SOLUCAO:###Detalhamento da regra de negcio:\nCAMPO[REGRANEGOCIO]\n\n###Tipo de retorno:\nCAMPO[TIPORETORNOANA]"
			});
		}
	}
}
