package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

public class FinalizaMerge implements EventoProcessoJava {

	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] solicitacoes = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoes.length > 0) {
			Registro solicitacao = solicitacoes[0];
			Registro[] linhasCorrecao = ctx.getLinhasFormulario("AD_TWFCORRE");
			Registro correcao = linhasCorrecao[linhasCorrecao.length - 1];
			boolean solicitouRelease = "S".equals(correcao.getCampo("COMPILARELEASE"));

			solicitacao.setCampo("COMPILARELEASE", solicitouRelease ? "S" : "N");
			ManutencaoSnkUtil.updateSoliman(correcao, solicitacao, new String[] {
				"VERSAOMERGE",
				"VERSOESCORRIGIDAS"
			});

			BigDecimal numOS = (BigDecimal) solicitacao.getCampo("NUMOS");
			ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(), "AD_TWFCORRE", correcao, new String []{
				"__PADRAO__:SOLUCAO:###Verses corrigidas:\nCAMPO[VERSOESCORRIGIDAS]\n\n###Verso de compilao:\nCAMPO[VERSAOMERGE]"
			});

			String versaoMerge = (String) solicitacao.getCampo("VERSAOMERGE");
			BigDecimal usuLogado = ctx.getUsuarioLogado();

			if (solicitouRelease) {
				Map<String, Object> itemProperties = new HashMap<String, Object>();
				itemProperties.put("SOLUCAO", versaoMerge);
				itemProperties.put("CODSERV", new BigDecimal(50603));
				if (versaoMerge == null) {
					throw new IllegalArgumentException("__PRETTY_MSG__Com a opo \"Compilar release\" marcada  necessrio informar a \"Verso de compilao\".");
				}

				OrdemServicoAPI.encaminhaFila(numOS, null, usuLogado, new BigDecimal(1721), itemProperties);
			} else {
				ManutencaoSnkUtil.finalizaItemUsuario(numOS, usuLogado, false);
			}
		}
	}
}
