package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;

import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;

public class EnviarFilaValidarCorrecao implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {

		ManutencaoSnkUtil.inserirDonoAtividade((BigDecimal) ctx.getIdInstanceTarefa(), ctx.getUsuarioInclusao().toString());
	}
}
