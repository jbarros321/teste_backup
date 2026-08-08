package br.com.sankhya.industria.manutencao.eventoprogramados;

import java.math.BigDecimal;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoConstants;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;

public class BloqueioEnvioSoftwareOSManutencaoSk implements EventoProgramavelJava {

	public void beforeInsert(PersistenceEvent event) throws Exception {
		validaCorrecaoFilaSoftware((DynamicVO) event.getVo());
		validaEntradaIndevidaSoftware((DynamicVO) event.getVo());
	}

	public void beforeUpdate(PersistenceEvent event) throws Exception {
	}

	public void beforeDelete(PersistenceEvent event) throws Exception {
	}

	public void afterInsert(PersistenceEvent event) throws Exception {
	}

	public void afterUpdate(PersistenceEvent event) throws Exception {
	}

	public void afterDelete(PersistenceEvent event) throws Exception {
	}

	public void beforeCommit(TransactionContext tranCtx) throws Exception {
	}

	private void validaCorrecaoFilaSoftware(DynamicVO itemVO) throws Exception {
		if (!JapeSession.getPropertyAsBoolean("INSERINDO_OS_PELO_PROCESSO", false)) {
			if (ManutencaoConstants.FILA_SOFTWARE.compareTo(itemVO.asBigDecimal("CODUSU")) == 0 && ManutencaoConstants.SERV_IND_ANALISE_DE_ERROS.compareTo(itemVO.asBigDecimal("CODSERV")) == 0 && !remetenteAutorizadoSoftware(itemVO) && !ManutencaoSnkUtil.temSoliman(itemVO.asBigDecimalOrZero("NUMOS"))) {
				throw new IllegalStateException("Encaminhamento de IND-ANLISE DE ERROS  fila do SOFTWARE s  permitido atravs do processo de manuteno.");
			}
		}
	}

	private void validaEntradaIndevidaSoftware(DynamicVO itemVO) throws Exception {
		if (!JapeSession.getPropertyAsBoolean("INSERINDO_OS_PELO_PROCESSO", false)) {
			BigDecimal numOS = itemVO.asBigDecimalOrZero("NUMOS");
			BigDecimal codServ = itemVO.asBigDecimalOrZero("CODSERV");
			BigDecimal remetente = itemVO.asBigDecimal("CODUSUREM");

			if (ManutencaoConstants.FILA_SOFTWARE.compareTo(itemVO.asBigDecimal("CODUSU")) == 0 && ManutencaoSnkUtil.temSoliman(numOS) && ManutencaoSnkUtil.ehUsuarioSD(remetente)) {
				String descrProdString = NativeSql.getString("DESCRPROD", "TGFPRO", "CODPROD = ?", new Object[] { codServ });
				throw new IllegalStateException("Esta OS j foi utilizada no processo de manuteno por isso no pode ser utilizada para " + descrProdString + ". Favor abrir uma nova OS especifica para o novo tipo de atendimento.");
			}
		}
	}

	private boolean remetenteAutorizadoSoftware(DynamicVO itemVO) throws Exception {
		return NativeSql.getBigDecimal("1", "TCSRUS", "CODUSUREL = ? AND TIPO = 'F' AND CODUSU IN(46, 176, 186) AND TIPO = 'F' AND ROWNUM = 1 ", new Object[] { itemVO.asBigDecimalOrZero("CODUSUREM") }) != null;
	}
}
