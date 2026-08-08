package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoConstants;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

public class FinalizaMerge implements EventoProcessoJava {

	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] solicitacoes = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (solicitacoes.length > 0) {
			Registro[] linhasCorrecao = ctx.getLinhasFormulario("AD_TWFCORRE");

			BigDecimal idInstanceTarefa = (BigDecimal) ctx.getIdInstanceTarefa();
			Registro correcao = null;
			int posicao = linhasCorrecao.length - 1;
			while (posicao > -1) {

				if (idInstanceTarefa.compareTo((BigDecimal) linhasCorrecao[posicao].getCampo("IDINSTTAR")) == 0) {
					correcao = linhasCorrecao[posicao];
					break;
				}
				posicao--;
			}

			if(correcao == null){
				throw new IllegalArgumentException("__PRETTY_MSG__Erro interno ao buscar registro da \"AD_TWFCORRE\".");
			}

			boolean possuiBranch = StringUtils.isNotEmpty(correcao.getCampo("BRANCH"));

			Registro solicitacao = solicitacoes[0];
			boolean solicitouRelease = possuiBranch || "S".equals(correcao.getCampo("COMPILARELEASE"));

			solicitacao.setCampo("COMPILARELEASE", solicitouRelease ? "S" : "N");
			ManutencaoSnkUtil.updateSoliman(correcao, solicitacao, new String[] {
				"VERSAOMERGE",
				"VERSOESCORRIGIDAS"
			});

			BigDecimal numOS = (BigDecimal) solicitacao.getCampo("NUMOS");
			ManutencaoSnkUtil.updateItemOS(numOS, ctx.getUsuarioLogado(), "AD_TWFCORRE", correcao, new String []{
				"__PADRAO__:SOLUCAO:###Verses corrigidas:\nCAMPO[VERSOESCORRIGIDAS]\n\n###Verso de compilao:\nCAMPO[VERSAOMERGE]"
			});

			String versaoMerge = (String) solicitacao.getCampo("VERSAOMERGE");
			BigDecimal usuLogado = ctx.getUsuarioLogado();

			boolean compilacaoManual = !solicitouRelease && "CM".equals(solicitacao.getCampo("TIPOCOMP"));
			BigDecimal proxFila = null;
			Map<String, Object> itemProperties = new HashMap<String, Object>();

			if (compilacaoManual) {
				String origemOS = (String) solicitacao.getCampo("ORIGEMOS");
				if ("E".equals(origemOS)) {

					proxFila = new BigDecimal(125);
					itemProperties.put("CODSERV", new BigDecimal(50208));
					itemProperties.put("CODSIT", new BigDecimal(26));

				} else {

					JapeSession.putProperty("INSERINDO_OS_PELO_PROCESSO", true);
					BigDecimal solicitante = ctx.getUsuarioInclusao();

					proxFila = NativeSql.getBigDecimal("CODUSU", "TCSRUS", "CODUSUREL = ? AND TIPO = 'F' AND CODUSU IN (186) ORDER BY CODUSU DESC", new Object[] { solicitante });
					itemProperties.put("CODSERV", ManutencaoConstants.SERV_GER_COMUNICACAO_ADMINISTRATIVA);
					itemProperties.put("CODSIT", new BigDecimal(1));

					if(proxFila == null) {
						proxFila = solicitante;
					}
				}

				OrdemServicoAPI.encaminhaFila(numOS, null, usuLogado, proxFila, itemProperties);
			}else if(solicitouRelease) {

				itemProperties.put("SOLUCAO", versaoMerge);
				itemProperties.put("CODSERV", new BigDecimal(50603));
				proxFila = new BigDecimal(1721);

				if (versaoMerge == null) {
					throw new IllegalArgumentException("__PRETTY_MSG__Com a opo \"Compilar release\" marcada  necessrio informar a \"Verso de compilao\".");
				}

				boolean nroReleaseValido = ManutencaoSnkUtil.versaoReleaseValida(versaoMerge);
				if(!nroReleaseValido) {
					throw new IllegalStateException("\"Verso de compilao\" invlida.");
				}

				OrdemServicoAPI.encaminhaFila(numOS, null, usuLogado, proxFila, itemProperties);

			}else {
				ManutencaoSnkUtil.finalizaItemUsuario(numOS, usuLogado, false);
			}
		}
	}
}
