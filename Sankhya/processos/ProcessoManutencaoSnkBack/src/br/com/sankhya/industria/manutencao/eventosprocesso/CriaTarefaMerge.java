package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

import com.sankhya.util.BigDecimalUtil;

public class CriaTarefaMerge implements EventoProcessoJava {

	public void executar(ContextoEvento ctx) throws Exception {
		Registro solicitacao = ctx.getLinhasFormulario("AD_TWFSOLIMAN")[0];
		BigDecimal idinsttar = (BigDecimal) ctx.getIdInstanceTarefa();
		BigDecimal idinstprn = BigDecimalUtil.getBigDecimal(ctx.getIdInstanceProcesso());

		BigDecimal responsavelDemanda = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("CODUSUDEV"));

		Map<String, Object> props = new HashMap<String, Object>();
		props.put("CODSERV", new BigDecimal(50601));
		props.put("CODSIT", new BigDecimal(2));

		OrdemServicoAPI.encaminhaOS(BigDecimalUtil.getBigDecimal(solicitacao.getCampo("NUMOS")), ctx.getUsuarioLogado(), responsavelDemanda, props);
		ManutencaoSnkUtil.inserirDonoAtividade(idinsttar, responsavelDemanda.toString());

		Registro registroCorre = ctx.novaLinhaFormulario("AD_TWFCORRE");
		registroCorre.setCampo("IDINSTPRN", idinstprn);
		registroCorre.setCampo("IDINSTTAR", idinsttar);
		registroCorre.setCampo("IDTAREFA", ManutencaoSnkUtil.getIdTarefa(idinstprn, idinsttar));
		registroCorre.setCampo("COMPILARELEASE", "S".equals(solicitacao.getCampo("COMPILARELEASE")) ? "N" : "S");
		registroCorre.save();
	}
}
