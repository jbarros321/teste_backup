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

public class VerificaInserirDonoQA implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];

			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");
			String tipoRetorno = (String) linha.getCampo("TIPORETORNOANA");
			String reVal = (String) linha.getCampo("REVAL");
			BigDecimal idinsttar = (BigDecimal) ctx.getIdInstanceTarefa();
			BigDecimal donoAtividade = ManutencaoSnkUtil.getUsuarioUltimoItemOS(numOS);

			if ("ES".equals(tipoRetorno)) {
				BigDecimal proxFila = new BigDecimal(46);

				Map<String, Object> props = new HashMap<String, Object>();
				props.put("CODSERV", new BigDecimal(50605));
				props.put("CODSIT", new BigDecimal(3));

				BigDecimal responsavelDemanda = ManutencaoSnkUtil.getExecutanteAnteriorFila(numOS, proxFila);

				if (responsavelDemanda != null) {

					OrdemServicoAPI.encaminhaOS(numOS, donoAtividade, responsavelDemanda, props);
					ManutencaoSnkUtil.inserirDonoAtividade(idinsttar, responsavelDemanda.toString());
				} else {
					BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, donoAtividade, false);
					OrdemServicoAPI.encaminhaFila(numOS, numItem, donoAtividade, proxFila, props);
				}
			} else if (!"N".equals(reVal)) {
				BigDecimal codUsu = NativeSql.getBigDecimal("CODUSU", "TCSRUS", "CODUSU = 46 AND CODUSU != CODUSUREL AND CODUSUREL = ?", new Object[] { donoAtividade });

				if (codUsu != null) {
					ManutencaoSnkUtil.inserirDonoAtividade(idinsttar, donoAtividade.toString());
				}
			}
		}
	}
}
