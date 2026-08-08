package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;

import com.sankhya.util.BigDecimalUtil;

public class FinalizaDocumentarCorrecao implements EventoProcessoJava {

	public void executar(ContextoEvento ctx) throws Exception {

		Registro[] solicitacoes = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoes.length > 0) {
			Registro solicitacao = solicitacoes[0];
			Registro documentacao = ctx.getLinhasFormulario("AD_TWFDOC")[0];
			BigDecimal numOS = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("NUMOS"));

			ManutencaoSnkUtil.updateSoliman(documentacao, solicitacao, new String [] {
				"DOCTIT",
				"DOCDETALHE",
				"DOCREQUISITOS"
			});

			ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(), "AD_TWFDOC", documentacao, new String [] {
				"__PADRAO__:SOLUCAO:-Ttulo:CAMPO[DOCTIT]\n\n-Problema / Soluo:CAMPO[DOCDETALHE]"
			});
		}
	}
}
