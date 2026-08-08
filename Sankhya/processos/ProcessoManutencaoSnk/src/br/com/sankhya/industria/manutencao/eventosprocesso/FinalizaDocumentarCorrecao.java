package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.sql.Timestamp;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.sql.NativeSql;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

public class FinalizaDocumentarCorrecao implements EventoProcessoJava {

	public void executar(ContextoEvento ctx) throws Exception {

		Registro[] solicitacoes = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoes.length > 0) {
			Registro solicitacao = solicitacoes[0];
			Registro [] linhasDoc = ctx.getLinhasFormulario("AD_TWFDOC");
			Registro documentacao = ManutencaoSnkUtil.getMaxRegistroFormulario(linhasDoc);
			BigDecimal numOS = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("NUMOS"));

			ManutencaoSnkUtil.updateSoliman(documentacao, solicitacao, new String [] {
				"DOCTIT",
				"DOCDETALHE",
				"DOCREQUISITOS",
				"EXECUTAVEL",
				"LINK",
				"ESPECIFICO",
				"JIVA"
			});

			ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(), "AD_TWFDOC", documentacao, new String [] {
				"__PADRAO__:SOLUCAO:-Ttulo:CAMPO[DOCTIT]\n\n-Problema / Soluo:CAMPO[DOCDETALHE]\n\n-Foi necessrio gerar executvel parcial?:CAMPO[EXECUTAVEL]\n\n-Link do executvel parcial:CAMPO[LINK]"
			});

			BigDecimal codProd = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("CODPROD"));
			Boolean produtoLinhaG = NativeSql.getBigDecimal("1", "TGFPRO", "CODPROD = ? AND CODGRUPOPROD IN (1002,1005)", new Object[] { codProd }) != null;

			if(produtoLinhaG) {

				BigDecimal responsavelCorrecaoBigDecimal = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("CODUSUQASAI"));
				String especificoParceiro = (String) solicitacao.getCampo("ESPECIFICO");
				String afetaJiva = (String) solicitacao.getCampo("JIVA");
				String tituloProblema = (String) solicitacao.getCampo("DOCTIT");
				String resumo = (String) solicitacao.getCampo("DOCDETALHE");
				String link = (String) solicitacao.getCampo("LINK");
				String nivel = "E";
				String situacao = "V";
				Timestamp dataAlteracao= new Timestamp(TimeUtils.getToday());

				Registro registro = ctx.novaLinha("TSIDOC");
				registro.setCampo("NUMOS", numOS);
				registro.setCampo("CODPROD", codProd);
				registro.setCampo("CODUSU", responsavelCorrecaoBigDecimal);
				registro.setCampo("ESPECIFICO", especificoParceiro);
				registro.setCampo("JIVA", afetaJiva);
				registro.setCampo("ENUNCIADO", tituloProblema);
				registro.setCampo("RESUMO", resumo);
				registro.setCampo("LINK", link);
				registro.setCampo("NIVEL", nivel);
				registro.setCampo("SITUACAO", situacao);
				registro.setCampo("DTALTER", dataAlteracao);

				registro.save();

				Registro [] versoes = ctx.getLinhasFormulario("AD_TWFVRS");

				for(Registro registroDoc : versoes) {

					String versao = "D-" + registroDoc.getCampo("VERSAO");
					String compilacao = (String) registroDoc.getCampo("COMPILACAO");

					Registro novaLinhaVersao = ctx.novaLinha("AD_TSIVRS");

					novaLinhaVersao.setCampo("NUMOS", numOS);
					novaLinhaVersao.setCampo("VERSAO", versao);
					novaLinhaVersao.setCampo("COMPILACAO", compilacao);
					novaLinhaVersao.setCampo("CODIGO", registro.getCampo("CODIGO"));

					novaLinhaVersao.save();

				}

			}
		}
	}
}
