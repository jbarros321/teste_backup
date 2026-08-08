package br.com.sankhya.industria.manutencao.tarefaservico;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoTarefa;
import br.com.sankhya.extensions.flow.TarefaJava;
import br.com.sankhya.industria.manutencao.eventoprogramados.BloqueioAlteracaoOSManutencaoSk;
import br.com.sankhya.industria.manutencao.eventosprocesso.EncaminhaRemoveFila;
import br.com.sankhya.industria.manutencao.util.ManutencaoConstants;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

public class VerificarVinculoOS implements TarefaJava {

	public void getContextualizaPrimeiroItem(BigDecimal solicitante, Map<String, Object> item) throws Exception {
		BigDecimal verificaFila = NativeSql.getBigDecimal("CODUSU", "TCSRUS", "CODUSU IN (186, 125, 46) AND TIPO = 'F' AND CODUSUREL = ? ORDER BY CODUSU DESC", new Object[] { solicitante });

		if(verificaFila != null) {

			if (verificaFila.equals(ManutencaoConstants.FILA_SOFTWARE)) {
				item.put("CODUSU", ManutencaoConstants.FILA_SOFTWARE);
				item.put("CODSERV", ManutencaoConstants.SERV_IND_ANALISE_DE_ERROS);
				item.put("CODSIT", BigDecimal.ONE);
			} else {
				item.put("CODUSU", ManutencaoConstants.FILA_TESTE_ENTRADA);
				item.put("CODSERV", ManutencaoConstants.SERV_IND_TESTE);
				item.put("CODSIT", new BigDecimal(3));
			}

		}else {
			item.put("CODUSU", solicitante);
			item.put("CODSERV", ManutencaoConstants.SERV_GER_COMUNICACAO_ADMINISTRATIVA);
			item.put("CODSIT", BigDecimal.ONE);
		}

	}

	public void executar(ContextoTarefa ctx) throws Exception {
		JapeSession.putProperty("INSERINDO_OS_PELO_PROCESSO", true);
		Registro[] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];

			BigDecimal codProd = (BigDecimal) linha.getCampo("CODPROD");
			BigDecimal celula = (BigDecimal) linha.getCampo("CODCELPROD");

			if (celula == null) {
				celula = NativeSql.getBigDecimal("AD_CELULALEAN", "TGFPRO", "CODPROD = " + codProd);
				linha.setCampo("CODCELPROD", celula);
			}

			BigDecimal solicitante = ctx.getUsuarioInclusao();
			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");
			BigDecimal idInstPrn = (BigDecimal) linha.getCampo("IDINSTPRN");

			if (numOS != null) {
				linha.setCampo("ORIGEMOS", "E");
				ManutencaoSnkUtil.copiaAnexosOSProcesso(idInstPrn, numOS);
			} else {
				Map<String, Object> cabecalho = new HashMap<String, Object>();
				cabecalho.put("CODPARC", linha.getCampo("CODPARC"));
				cabecalho.put("NUMCONTRATO", linha.getCampo("NUMCONTRATO"));
				cabecalho.put("DHCHAMADA", new Timestamp(System.currentTimeMillis()));
				cabecalho.put("CODCONTATO", linha.getCampo("CODCONTATO"));
				cabecalho.put("CODUSURESP", solicitante);
				if(celula != null){
					cabecalho.put("AD_CELULALEAN", celula);
				}
				cabecalho.put("CODCENCUS", new BigDecimal(10001601));
				cabecalho.put("DESCRICAO", linha.getCampo("DESCSERV"));
				cabecalho.put("CODCOS", BigDecimal.ZERO);

				Map<String, Object> item = new HashMap<String, Object>();
				item.put("CODPROD", codProd);
				item.put("PRIORIDADE", new BigDecimal(linha.getCampo("PRIORIDADE").toString()));
				item.put("CODOCOROS", new BigDecimal(6));
				item.put("DHPREVISTA", new Timestamp(System.currentTimeMillis()));
				item.put("COBRAR", "N");
				item.put("RETRABALHO", "S");

				getContextualizaPrimeiroItem(solicitante, item);
				JapeSession.putProperty(BloqueioAlteracaoOSManutencaoSk.IGNORAR_VALIDACAO_FLOW, true);
				numOS = OrdemServicoAPI.incluirOrdemServico(cabecalho, item, solicitante, (BigDecimal) item.get("CODUSU"));
				JapeSession.putProperty(EncaminhaRemoveFila.OS_MANUTENCAO_EM_INCLUSAO, numOS);

				BigDecimal temLog = NativeSql.getBigDecimal("1", "AD_TWFSOLIMAN", "LOG IS NOT NULL AND IDINSTPRN = ?", new Object [] { idInstPrn });
				BigDecimal anexoOS = NativeSql.getBigDecimal("1", "TSIATA", "ARQUIVO = 'server.log' AND DESCRICAO = 'Log' AND CODATA = ?", new Object [] { numOS });

				if (temLog != null && anexoOS == null) {
					Map<String,Object> infoAnexo = new HashMap<String,Object>();
					infoAnexo.put("NUMOS", numOS);
					infoAnexo.put("CODREGISTRO", linha.getCampo("CODREGISTRO"));
					infoAnexo.put("IDINSTTAR", linha.getCampo("IDINSTTAR"));
					infoAnexo.put("IDINSTPRN", linha.getCampo("IDINSTPRN"));

					ManutencaoSnkUtil.salvaLogAnexoOS(infoAnexo, solicitante);
				}

				linha.setCampo("NUMOS", numOS);
				linha.setCampo("ORIGEMOS", "I");
			}

			linha.save();
		}
	}
}
