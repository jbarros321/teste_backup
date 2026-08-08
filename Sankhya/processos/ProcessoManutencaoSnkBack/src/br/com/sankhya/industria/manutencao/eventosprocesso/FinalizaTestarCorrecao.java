package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;

import com.sankhya.util.BigDecimalUtil;

public class FinalizaTestarCorrecao implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] solicitacoes = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoes.length > 0) {
			Registro solicitacao = solicitacoes[0];
			Registro [] linhasTesteSai = ctx.getLinhasFormulario("AD_TWFTESTSAI");
			Registro testeSaida = linhasTesteSai[linhasTesteSai.length - 1];

			solicitacao.setCampo("CODUSUQASAI", ctx.getUsuarioLogado());
			String tipoSolucao = (String) testeSaida.getCampo("TIPORETORNOSAI");
			if("NC".equals(tipoSolucao)){

				tipoSolucao = "PNC";
			}
			solicitacao.setCampo("TIPOSOLUCAO", tipoSolucao);

			ManutencaoSnkUtil.updateSoliman(testeSaida, solicitacao, new String []{
				"VALICACAOQA",
				"ALTERHELP",
				"TIPORETORNOSAI",
				"A:AD_TWFTESTSAI:ANEXOQASAI"
			});

			BigDecimal numOS = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("NUMOS"));
			ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(), "AD_TWFTESTSAI", testeSaida, new String []{
				"__PADRAO__:SOLUCAO:###Detalhes da validao:\nCAMPO[VALICACAOQA]\n\n###Atualizao/alterao do Help:\nCAMPO[ALTERHELP]\n\n###Tipo de retorno:\nCAMPO[TIPORETORNOSAI]"
			});
		}
	}
}
