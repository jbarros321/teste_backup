package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;

public class InserirDonoTarefa implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {

		BigDecimal idinsttar = (BigDecimal) ctx.getIdInstanceTarefa();
		BigDecimal codUsu = ctx.getUsuarioInclusao();

		ManutencaoSnkUtil.inserirDonoAtividade(idinsttar, codUsu.toString());
	}
}
