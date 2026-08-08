package br.com.sankhya.industria.manutencao.tarefaservico;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoTarefa;
import br.com.sankhya.extensions.flow.TarefaJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

public class EnviarFilaSD implements TarefaJava {
	public void executar(ContextoTarefa ctx) throws Exception {
		Registro [] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];

			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");
			BigDecimal remetente = NativeSql.getBigDecimal("CODUSUREM", "TCSITE", "NUMOS = ? ORDER BY NUMITEM DESC", new Object[] { numOS });
			BigDecimal filaCompilador = new BigDecimal(1721);

			if (filaCompilador.compareTo(remetente) != 0) {
				BigDecimal proxFila = new BigDecimal(125);
				BigDecimal codUsu = ManutencaoSnkUtil.getUsuarioUltimoItemOS(numOS);
				BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);

				Map<String, Object> props = new HashMap<String, Object>();
				props.put("CODSERV", new BigDecimal(50208));
				props.put("CODSIT", new BigDecimal(26));

				OrdemServicoAPI.encaminhaFila(numOS, numItem, codUsu, proxFila, props);
			}
		}
	}
}
