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

public class VerificaCompilacao implements EventoProcessoJava {

	public void executar(ContextoEvento ctx) throws Exception {
		Registro [] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];

			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");
			BigDecimal idinsttar = (BigDecimal) ctx.getIdInstanceTarefa();

			if ("S".equals(linha.getCampo("COMPQUEBRADA"))) {

				BigDecimal executante = NativeSql.getBigDecimal("CODUSUREM", "TCSITE", "NUMOS = ? AND CODUSU = 1721 AND ROWNUM = 1 ORDER BY NUMITEM DESC", new Object [] { numOS });

				ManutencaoSnkUtil.inserirDonoAtividade(idinsttar, executante.toString());
			} else {
				BigDecimal proxFila = new BigDecimal(176);
				BigDecimal codUsu = ManutencaoSnkUtil.getUsuarioUltimoItemOS(numOS);
				BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);

				Map<String, Object> props = new HashMap<String, Object>();
				props.put("CODSERV", new BigDecimal(50601));
				props.put("CODSIT", new BigDecimal(2));

				BigDecimal responsavelDemanda = "PNC".equals(linha.getCampo("TIPOSOLUCAO")) ? ManutencaoSnkUtil.getExecutanteAnteriorFila(numOS, proxFila) : null;

				if (responsavelDemanda != null) {
					if ("NC".equals(linha.getCampo("TIPORETORNOSAI"))) {
						OrdemServicoAPI.encaminhaOS(numOS, codUsu, responsavelDemanda, props);
					}

					ManutencaoSnkUtil.inserirDonoAtividade(idinsttar, responsavelDemanda.toString());
				} else {
					OrdemServicoAPI.encaminhaFila(numOS, numItem, codUsu, proxFila, props);
				}
			}
		}
	}
}
