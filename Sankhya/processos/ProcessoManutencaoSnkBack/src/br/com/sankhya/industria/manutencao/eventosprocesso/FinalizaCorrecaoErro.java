package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

public class FinalizaCorrecaoErro implements EventoProcessoJava {

	public void executar(ContextoEvento ctx) throws Exception {

		Registro[] solicitacoe = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoe.length > 0) {
			Registro solicitacao = solicitacoe[0];
			Registro[] correcoes = ctx.getLinhasFormulario("AD_TWFCORRE");
			if(correcoes.length < 1){
				throw new IllegalStateException("Para finalizar a tarefa, voc deve informar os detalhes da correo.");
			}

			Registro correcao = correcoes[correcoes.length - 1];

			boolean compilaRelease = "S".equals(correcao.getCampo("COMPILARELEASE"));

			solicitacao.setCampo("CODUSUDEV", ctx.getUsuarioLogado());
			solicitacao.setCampo("COMPILARELEASE", compilaRelease ? "S" : "N");
			ManutencaoSnkUtil.updateSoliman(correcao, solicitacao, new String[] {
				"PROIDEDEV",
				"SOLUCAO",
				"FONTESALTER",
				"VERSAOMERGE",
				"BRANCH",
				"TIPORETORNOCOR",
				"A:AD_TWFCORRE:ANEXODEV",
				"TIPORETORNOCOR->TIPOSOLUCAO"
			});

			final BigDecimal numOS = (BigDecimal) solicitacao.getCampo("NUMOS");
			ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(), "AD_TWFCORRE", correcao, new String [] {
				"__PADRAO__:SOLUCAO:###Problema identificado(DEV):\nCAMPO[PROIDEDEV]\n\n###Soluo:\nCAMPO[SOLUCAO]\n\n###Fontes alterados:\nCAMPO[FONTESALTER]\n\n###Nro da branch:\nCAMPO[BRANCH]\n\n###Tipo de retorno:\nCAMPO[TIPORETORNOCOR]"
			});

			if ("PC".equals(correcao.getCampo("TIPORETORNOCOR"))) {

				Map<String, Object> camposOS = new HashMap<String, Object>();
				camposOS.put("AD_CORRELATO", correcao.getCampo("AD_CORRELATO"));
				camposOS.put("AD_NROUNICO", BigDecimalUtil.getBigDecimal(correcao.getCampo("NROUNICO")));
				camposOS.put("AD_NU", BigDecimalUtil.getBigDecimal(correcao.getCampo("NU")));

				ManutencaoSnkUtil.atualizaCamposOs(numOS, camposOS);

				String branch = StringUtils.getEmptyAsNull(correcao.getCampo("BRANCH"));
				if (branch == null) {
					throw new IllegalArgumentException("__PRETTY_MSG__O tipo de retorno \"Problema corrigido\" encaminha a solicitao para compilao sendo assim  necessrio informar o \"Nro da Branch\".");
				}
				Map<String, Object> itemProperties = new HashMap<String, Object>();
				itemProperties.put("CODSERV", new BigDecimal(50603));

				boolean encaminhouFila = false;
				BigDecimal compilacao = new BigDecimal(1721);
				BigDecimal codUsuItem = ManutencaoSnkUtil.getUsuarioUltimoItemOS(numOS);
				BigDecimal item = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsuItem, false);

				if (!aguardandoCompilacao(numOS, branch, true)) {
					itemProperties.put("SOLUCAO", branch);
					OrdemServicoAPI.encaminhaFila(numOS, item, codUsuItem, compilacao, itemProperties);
					encaminhouFila = true;
				}

				if (compilaRelease) {

					String versaoMerge = (String) correcao.getCampo("VERSAOMERGE");
					if (versaoMerge == null) {
						throw new IllegalArgumentException("__PRETTY_MSG__Com a opo \"Compilar release imediatamente\" marcada  necessrio informar a \"Verso de compilao\".");
					}

					if (aguardandoCompilacao(numOS, branch, false)) {
						throw new IllegalArgumentException("__PRETTY_MSG__J existe solicitao pendente para compilar a release. Voc no deve marcar \"Compilar release imediatamente\".");
					}

					if (encaminhouFila) {
						NativeSql query = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());
						query.setNamedParameter("NUMOS", numOS);
						query.setNamedParameter("BRANCH", branch);
						query.setNamedParameter("NUMITEM", item);
						query.executeUpdate("UPDATE TCSITE SET INICEXEC = NULL, HRINICIAL = NULL, HRFINAL = NULL WHERE NUMOS = :NUMOS AND NUMITEM = :NUMITEM");
					}

					itemProperties.put("INICEXEC", null);
					itemProperties.put("HRINICIAL", null);
					itemProperties.put("HRFINAL", null);
					itemProperties.put("SOLUCAO", versaoMerge);
					OrdemServicoAPI.encaminhaFila(numOS, item, codUsuItem, compilacao, itemProperties);
					encaminhouFila = true;
				}

				if (!encaminhouFila) {
					ManutencaoSnkUtil.finalizaItemUsuario(numOS, codUsuItem, false);
				}
			}

		}

	}

	private boolean aguardandoCompilacao(BigDecimal numOS, String branch, boolean release) throws Exception {
		String solucao = release ? " SOLUCAO = ? " : " SOLUCAO <> ? ";
		return NativeSql.getBigDecimal("COUNT(1)", "TCSITE", " NUMOS = ? AND CODUSU = 1721 AND HRFINAL IS NULL AND " + solucao, new Object[] { numOS, branch }).intValue() > 0;
	}
}
