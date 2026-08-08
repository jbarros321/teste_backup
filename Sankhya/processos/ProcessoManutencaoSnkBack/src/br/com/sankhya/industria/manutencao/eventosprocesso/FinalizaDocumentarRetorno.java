package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

import com.sankhya.util.BigDecimalUtil;

public class FinalizaDocumentarRetorno implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] solicitacoes = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoes.length > 0) {
			Registro solicitacao = solicitacoes[0];
			Registro documentacao = ctx.getLinhasFormulario("AD_TWFDOCSCORR")[0];
			ManutencaoSnkUtil.updateSoliman(documentacao, solicitacao, new String [] {"DOCRET", "A:AD_TWFDOCSCORR:ANEXORETSCORR"});

			BigDecimal numOS = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("NUMOS"));
			ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(), "AD_TWFDOCSCORR", documentacao, new String [] {
				"__PADRAO__:SOLUCAO:###Documentao do retorno:\nCAMPO[DOCRET]"
			});

			String origemOS = (String) solicitacao.getCampo("ORIGEMOS");

			Map<String, Object> props = new HashMap<String, Object>();
			BigDecimal proxFila = null;
			BigDecimal codUsu = ctx.getUsuarioLogado();
			BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);

			if ("E".equals(origemOS)) {

				proxFila = new BigDecimal(125);
				props.put("CODSERV", new BigDecimal(50208));
				props.put("CODSIT", new BigDecimal(26));

			} else {

				BigDecimal solicitante = ctx.getUsuarioInclusao();

				proxFila = NativeSql.getBigDecimal("CODUSU", "TCSRUS", "CODUSUREL = ? AND TIPO = 'F' AND CODUSU IN (5723, 186, 176, 46) ORDER BY CODUSU DESC", new Object[] { solicitante });

				if(proxFila.intValue() == 46){

					props.put("CODSERV", new BigDecimal(50605));
					props.put("CODSIT", new BigDecimal(3));

				} else if(proxFila.intValue() == 5723){

					props.put("CODSERV", new BigDecimal(50313));
					props.put("CODSIT", new BigDecimal(1));

				} else {

					props.put("CODSERV", new BigDecimal(50506));
					props.put("CODSIT", new BigDecimal(1));

				}
			}

			if(proxFila != null){
				OrdemServicoAPI.encaminhaFila(numOS, numItem, codUsu, proxFila, props);
			}
		}
	}
}
