package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.sankhya.util.StringUtils;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

public class EnviaFilaManutencao implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro [] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];

			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");
			BigDecimal codUsu = ManutencaoSnkUtil.getUsuarioUltimoItemOS(numOS);
			BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);
			BigDecimal codProd = (BigDecimal) linha.getCampo("CODPROD");
			BigDecimal proxFila = null;
			Boolean produtoLinhaG = NativeSql.getBigDecimal("1", "TGFPRO", "CODPROD = ? AND CODGRUPOPROD IN (1002,1005)", new Object[] { codProd }) != null;
			String executavelParcial = (String) linha.getCampo("EXECUTAVEL");
			String linkExec = (String) linha.getCampo("LINK");

			Map<String, Object> props = new HashMap<String, Object>();

			if (produtoLinhaG) {

				if ("SIM".equals(executavelParcial) && linkExec == null) {
					throw new IllegalArgumentException("__PRETTY_MSG__Favor informar o \"Link do executvel parcial\".");
				}

				props.put("CODSERV", new BigDecimal(50329));
				props.put("CODSIT", new BigDecimal(13));
				props.put("INICEXEC", null);
				props.put("HRINICIAL", null);
				props.put("HRFINAL", null);
				proxFila = new BigDecimal(160);

				OrdemServicoAPI.encaminhaFila(numOS, numItem, codUsu, proxFila, props);

				if ("SIM".equals(executavelParcial)) {

					props.put("CODSERV", new BigDecimal(50208));
					props.put("CODSIT", new BigDecimal(26));
					props.put("INICEXEC", null);
					props.put("HRINICIAL", null);
					props.put("HRFINAL", null);
					proxFila = new BigDecimal(125);

					OrdemServicoAPI.encaminhaFila(numOS, numItem, codUsu, proxFila, props);

				}

			} else {

				props.put("CODSERV", new BigDecimal(50601));
				props.put("CODSIT", new BigDecimal(2));
				proxFila = new BigDecimal(176);
				BigDecimal responsavelDemanda = ManutencaoSnkUtil.getExecutanteAnteriorFila(numOS, proxFila);

				if (responsavelDemanda == null) {
					OrdemServicoAPI.encaminhaFila(numOS, numItem, codUsu, proxFila, props);
				}

			}

		}
	}
}
