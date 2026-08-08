package br.com.sankhya.industria.manutencao.tarefaservico;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoTarefa;
import br.com.sankhya.extensions.flow.TarefaJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;

public class TarefaEncerrarOrdemServico implements TarefaJava {
	public void executar(ContextoTarefa ctx) throws Exception {

		Registro [] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];
			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");
			BigDecimal codUsu = ctx.getUsuarioLogado();

			ManutencaoSnkUtil.finalizaItemUsuario(numOS, codUsu, true);
		}
	}
}
