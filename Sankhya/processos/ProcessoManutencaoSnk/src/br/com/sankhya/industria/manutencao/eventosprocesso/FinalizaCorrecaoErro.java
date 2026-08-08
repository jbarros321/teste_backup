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

			Registro correcao = ManutencaoSnkUtil.getMaxRegistroFormulario(correcoes);
			boolean compilaRelease = "S".equals(correcao.getCampo("COMPILARELEASE"));

			solicitacao.setCampo("CODUSUDEV", ctx.getUsuarioLogado());
			solicitacao.setCampo("COMPILARELEASE", compilaRelease ? "S" : "N");
			ManutencaoSnkUtil.updateSoliman(correcao, solicitacao, new String[] {
				"PROIDEDEV",
				"SOLUCAO",
				"FONTESALTER",
				"TESTECOMPLEMENTAR",
				"VERSAOMERGE",
				"BRANCH",
				"TIPORETORNOCOR",
				"A:AD_TWFCORRE:ANEXODEV",
				"TIPORETORNOCOR->TIPOSOLUCAO",
				"TIPOCOMP",
				"COMPILABRANCH"
			});

			BigDecimal codUsuLogado = ctx.getUsuarioLogado();
			BigDecimal numOS = (BigDecimal) solicitacao.getCampo("NUMOS");
			ManutencaoSnkUtil.updateItemOS(numOS, codUsuLogado, "AD_TWFCORRE", correcao, new String [] {
				"__PADRAO__:SOLUCAO:###Problema identificado(DEV):\nCAMPO[PROIDEDEV]\n\n###Soluo:\nCAMPO[SOLUCAO]\n\n###Fontes alterados:\nCAMPO[FONTESALTER]\n\n###Teste complementar:\nCAMPO[TESTECOMPLEMENTAR]\n\n###Nro da branch:\nCAMPO[BRANCH]\n\n###Verso de compilao:\nCAMPO[VERSAOMERGE]\n\n###Tipo da compilao:\nCAMPO[TIPOCOMP]\n\n###Tipo de retorno:\nCAMPO[TIPORETORNOCOR]"
			});

			Map<String, Object> camposOS = new HashMap<String, Object>();
			camposOS.put("AD_CORRELATO", correcao.getCampo("AD_CORRELATO"));
			camposOS.put("AD_NROUNICO", BigDecimalUtil.getBigDecimal(correcao.getCampo("NROUNICO")));
			camposOS.put("AD_NU", BigDecimalUtil.getBigDecimal(correcao.getCampo("NU")));

			ManutencaoSnkUtil.atualizaCamposOs(numOS, camposOS);

			if ("BG".equals(correcao.getCampo("TIPORETORNOCOR")) || ("PC".equals(correcao.getCampo("TIPORETORNOCOR")) && "CM".equals(correcao.getCampo("TIPOCOMP")) ) ) {

				if(compilaRelease) {
					throw new IllegalArgumentException("__PRETTY_MSG__No  possvel \"Compilar release imediatamente\" quando a compilao  manual.");
				}

				Map<String, Object> itemProperties = new HashMap<String, Object>();
				itemProperties.put("CODSERV", new BigDecimal(50605));
				itemProperties.put("CODSIT", new BigDecimal(3));

				BigDecimal item = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsuLogado, false);
				OrdemServicoAPI.encaminhaFila(numOS, item, codUsuLogado, new BigDecimal(2060), itemProperties);

			} else if ("DQ".equals(correcao.getCampo("TIPORETORNOCOR"))) {

				Map<String, Object> itemProperties = new HashMap<String, Object>();
				itemProperties.put("CODSERV", new BigDecimal(50605));
				itemProperties.put("CODSIT", new BigDecimal(3));

				BigDecimal item = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsuLogado, false);
				OrdemServicoAPI.encaminhaFila(numOS, item, codUsuLogado, new BigDecimal(46), itemProperties);

			} else if ("PC".equals(correcao.getCampo("TIPORETORNOCOR"))) {

				String tipoCompilacao = StringUtils.getEmptyAsNull(correcao.getCampo("TIPOCOMP"));
				if (tipoCompilacao == null) {
					throw new IllegalArgumentException("__PRETTY_MSG__Favor informar o \"Tipo da compilao\".");
				}

				boolean compilaBranch = StringUtils.toBoolean((String) correcao.getCampo("COMPILABRANCH"));
				if ("CA".equals(tipoCompilacao) && !compilaBranch && !compilaRelease) {
					throw new IllegalArgumentException("__PRETTY_MSG__Para \"Tipo da compilao = Automtica\" voc precisa \"Compilar Branch\" ou \"Compilar Release imediatamente\".");
				}

				Map<String, Object> itemProperties = new HashMap<String, Object>();
				itemProperties.put("CODSERV", new BigDecimal(50603));

				boolean encaminhouFila = false;
				BigDecimal compilacao = new BigDecimal(1721);
				BigDecimal codUsuItem = ManutencaoSnkUtil.getUsuarioUltimoItemOS(numOS);
				BigDecimal item = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsuItem, false);

				String branch = StringUtils.getEmptyAsNull(correcao.getCampo("BRANCH"));

				if (!aguardandoCompilacao(numOS, branch, true) && compilaBranch) {

					if (compilaBranch && branch == null) {
						throw new IllegalArgumentException("__PRETTY_MSG__Para \"Compilar Branch\"  necessrio informar o \"Nome da Branch\".");
					}

					boolean nomeBranchEhValido = ManutencaoSnkUtil.nomeBranchValido(branch, numOS);
					if(!nomeBranchEhValido) {
						throw new IllegalStateException("\"Nome da branch\" invlido.");
					}

					itemProperties.put("SOLUCAO", branch);
					OrdemServicoAPI.encaminhaFila(numOS, item, codUsuItem, compilacao, itemProperties);
					encaminhouFila = true;
				}

				if (compilaRelease) {

					String versaoMerge = (String) correcao.getCampo("VERSAOMERGE");
					if (versaoMerge == null) {
						throw new IllegalArgumentException("__PRETTY_MSG__Com a opo \"Compilar release imediatamente\" marcada  necessrio informar a \"Verso de compilao\".");
					}

					boolean nroReleaseValido = ManutencaoSnkUtil.versaoReleaseValida(versaoMerge);
					if(!nroReleaseValido) {
						throw new IllegalStateException("\"Verso de compilao\" invlida.");
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
