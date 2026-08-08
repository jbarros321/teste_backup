package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;

public class InserirDonoTarefa implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];

			BigDecimal idinsttar = (BigDecimal) ctx.getIdInstanceTarefa();
			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");
			BigDecimal codUsu = ManutencaoSnkUtil.getUsuarioUltimoItemOS(numOS, "CODUSUREM");

			ManutencaoSnkUtil.inserirDonoAtividade(idinsttar, codUsu.toString());
		}
	}
}
