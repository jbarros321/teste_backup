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

public class FinalizaValidarRevisarEntrada implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] solicitacoes = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoes.length > 0) {
			Registro solicitacao = solicitacoes[0];
			Registro[] linhasTE = ctx.getLinhasFormulario("AD_TWFTESTENT");
			Registro testeEntrada = ManutencaoSnkUtil.getMaxRegistroFormulario(linhasTE);

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

			if("DSD".equals(solicitacao.getCampo("TIPOSOLUCAO"))){

				String origemOS = (String) solicitacao.getCampo("ORIGEMOS");
				Map<String, Object> props = new HashMap<String, Object>();
				BigDecimal proxFila = null;
				BigDecimal codUsu = ctx.getUsuarioLogado();
				BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);

				if ("E".equals(origemOS)) {

					proxFila = new BigDecimal(125);
					props.put("CODSERV", new BigDecimal(50208));
					props.put("CODSIT", new BigDecimal(26));

				} else {

					BigDecimal solicitante = ctx.getUsuarioInclusao();

					proxFila = NativeSql.getBigDecimal("CODUSU", "TCSRUS", "CODUSUREL = ? AND TIPO = 'F' AND CODUSU IN (5723, 186, 176, 46) ORDER BY CODUSU DESC", new Object[] { solicitante });

					if(proxFila.intValue() == 46){

						props.put("CODSERV", new BigDecimal(50605));
						props.put("CODSIT", new BigDecimal(3));

					} else if(proxFila.intValue() == 5723){

						props.put("CODSERV", new BigDecimal(50313));
						props.put("CODSIT", new BigDecimal(1));

					} else {

						props.put("CODSERV", new BigDecimal(50506));
						props.put("CODSIT", new BigDecimal(1));

					}
				}

				if(proxFila != null){
					OrdemServicoAPI.encaminhaFila(numOS, numItem, codUsu, proxFila, props);
				}
			}
		}
	}
}
