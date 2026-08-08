package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoConstants;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

import com.sankhya.util.BigDecimalUtil;

public class FinalizaDocumentarRetorno implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] solicitacoes = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoes.length > 0) {
			Registro solicitacao = solicitacoes[0];
			solicitacao.setCampo("CODUSUQARE", ctx.getUsuarioLogado());
			Registro[] linhasDocRetorno = ctx.getLinhasFormulario("AD_TWFDOCSCORR");
			Registro documentacao = ManutencaoSnkUtil.getMaxRegistroFormulario(linhasDocRetorno);
			ManutencaoSnkUtil.updateSoliman(documentacao, solicitacao, new String [] {
				"TIPORETORNODOCSCORR",
				"DOCRET",
				"TIPORETORNODOCSCORR->TIPOSOLUCAO",
				"A:AD_TWFDOCSCORR:ANEXORETSCORR"
			});

			BigDecimal numOS = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("NUMOS"));
			ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(), "AD_TWFDOCSCORR", documentacao, new String [] {
				"__PADRAO__:SOLUCAO:###Documentao do retorno:\nCAMPO[DOCRET]"
			});

			Map<String, Object> props = new HashMap<String, Object>();
			BigDecimal proxFila = null;
			BigDecimal codUsu = ctx.getUsuarioLogado();
			BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);

			String tipoRetorno = (String) documentacao.getCampo("TIPORETORNODOCSCORR");
			if(tipoRetorno == null || "DC-RN".indexOf(tipoRetorno) < 0){
				String origemOS = (String) solicitacao.getCampo("ORIGEMOS");
				if ("E".equals(origemOS)) {

					proxFila = new BigDecimal(125);
					props.put("CODSERV", new BigDecimal(50208));
					props.put("CODSIT", new BigDecimal(26));

				} else {

					JapeSession.putProperty("INSERINDO_OS_PELO_PROCESSO", true);
					BigDecimal solicitante = ctx.getUsuarioInclusao();

					props.put("CODSERV", ManutencaoConstants.SERV_GER_COMUNICACAO_ADMINISTRATIVA);
					props.put("CODSIT", new BigDecimal(1));

					if (ManutencaoSnkUtil.isMembroFila(solicitante, ManutencaoConstants.FILA_SOFTWARE)) {
						proxFila = ManutencaoConstants.FILA_SOFTWARE;
					} else if (ManutencaoSnkUtil.isMembroFila(solicitante, ManutencaoConstants.FILA_IMPLATACAO)) {
						proxFila = ManutencaoConstants.FILA_IMPLATACAO;
					} else {
						proxFila = solicitante;
					}
				}
			}

			if(proxFila != null){
				OrdemServicoAPI.encaminhaFila(numOS, numItem, codUsu, proxFila, props);
			}
		}
	}
}
