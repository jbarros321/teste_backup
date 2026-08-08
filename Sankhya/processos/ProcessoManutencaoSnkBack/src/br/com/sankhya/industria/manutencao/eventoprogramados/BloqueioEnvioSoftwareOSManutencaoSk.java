package br.com.sankhya.industria.manutencao.eventoprogramados;

import java.math.BigDecimal;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;

public class BloqueioEnvioSoftwareOSManutencaoSk implements EventoProgramavelJava {

	public void beforeInsert(PersistenceEvent event) throws Exception {
		validaCorrecaoFilaSoftware((DynamicVO) event.getVo());
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
			if (itemVO.asInt("CODUSU") == 186 && itemVO.asInt("CODSERV") == 50506 && !remetenteAutorizadoSoftware(itemVO)) {
				BigDecimal numOS = itemVO.asBigDecimalOrZero("NUMOS");
				if (!temSoliman(numOS)) {
					throw new IllegalStateException("Encaminhamento de IND-ANLISE DE ERROS  fila do SOFTWARE s  permitido atravs do processo de manuteno.");
				}
			}
		}
	}

	private boolean remetenteAutorizadoSoftware(DynamicVO itemVO) throws Exception {
		return NativeSql.getBigDecimal("1", "TCSRUS", "CODUSUREL = ? AND TIPO = 'F' AND CODUSU IN(46, 176, 186) AND TIPO = 'F' AND ROWNUM = 1 ", new Object[] { itemVO.asBigDecimalOrZero("CODUSUREM") }) != null;
	}

	private boolean temSoliman(BigDecimal numOS) throws Exception {
		return NativeSql.getBigDecimal("NUMOS", "AD_TWFSOLIMAN", "NUMOS = ?", new Object[] { numOS }) != null;
	}
}
