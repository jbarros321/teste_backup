package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;

public class FinalizaValidarRevisarEntrada implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] solicitacoes = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoes.length > 0) {
			Registro solicitacao = solicitacoes[0];
			Registro[] linhasTE = ctx.getLinhasFormulario("AD_TWFTESTENT");
			Registro testeEntrada = linhasTE[linhasTE.length - 1];

			solicitacao.setCampo("CODUSUQAENT", ctx.getUsuarioLogado());
			ManutencaoSnkUtil.updateSoliman(testeEntrada, solicitacao, new String [] {
				"AD_IDTELA",
				"CONTRIBUICOESQA",
				"OSIMPLEMENTACAO",
				"PROIDEQA",
				"RESESP",
				"SIMULACAO",
				"VEREXEC",
				"NOMEUSUARIOSQL",
				"VERDB",
				"BASE",
				"IPSERV",
				"SID",
				"TIPORETORNOENT",
				"A:AD_TWFTESTENT:ANEXOQAENT",
				"TIPORETORNOENT->TIPOSOLUCAO",
			});

			BigDecimal numOS = (BigDecimal) solicitacao.getCampo("NUMOS");
			ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(), "AD_TWFSOLIMAN", solicitacao, new String [] {
				"__PADRAO__:SOLUCAO:###Situao reproduzida em QA:\nCAMPO[CONTRIBUICOESQA]\n\n###OS de implementao:\nCAMPO[OSIMPLEMENTACAO]\n\n###Problema identificado (QA):\nCAMPO[PROIDEQA]\n\n###Resultado esperado:\nCAMPO[RESESP]\n\n###Simulao:\nCAMPO[SIMULACAO]\n\n###Verso do executvel:\nCAMPO[VEREXEC]\n\n###Nome do usurio(SQL):\nCAMPO[NOMEUSUARIOSQL]\n\n###Verso do banco de dados:\nCAMPO[VERDB]\n\n###Nome da base:\nCAMPO[BASE]\n\n###Endereo do servidor(Banco de dados):\nCAMPO[IPSERV]\n\n###SID:\nCAMPO[SID]\n\n###Tipo de retorno:\nCAMPO[TIPORETORNOENT]"
			});

			Map<String, Object> camposOS = new HashMap<String, Object>();
			camposOS.put("AD_IDTELA", testeEntrada.getCampo("AD_IDTELA"));
			ManutencaoSnkUtil.atualizaCamposOs(numOS, camposOS);
		}
	}
}
