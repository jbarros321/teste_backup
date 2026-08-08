package br.com.sankhya.industria.manutencao.eventoprogramados;

import java.math.BigDecimal;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.MGECoreParameter;

public class BloqueioExclusaoApontamentoManutencaoSk implements EventoProgramavelJava {

	public void beforeInsert(PersistenceEvent event) throws Exception {
	}

	public void beforeUpdate(PersistenceEvent event) throws Exception {
	}

	public void beforeDelete(PersistenceEvent event) throws Exception {
		DynamicVO vo = (DynamicVO) event.getVo();
		BigDecimal idInstPrn = vo.asBigDecimal("IDINSTPRN");

		BigDecimal codPrn = NativeSql.getBigDecimal("CODPRN", "TWFIPRN", "IDINSTPRN = ? ", new Object[] { idInstPrn });

		BigDecimal codPrnMan = BigDecimal.valueOf(MGECoreParameter.getParameterAsInt("CODPRNMANSNK"));

		if (codPrn.compareTo(codPrnMan) == 0) {
			throw new IllegalArgumentException("A excluso de apontamento  proibida no processo de Manuteno. Invalide o apontamento preenchendo \"Dh. inicial\" igual  \"Dh. final\".");
		}
	}

	public void afterInsert(PersistenceEvent event) throws Exception {
	}

	public void afterUpdate(PersistenceEvent event) throws Exception {
	}

	public void afterDelete(PersistenceEvent event) throws Exception {
	}

	public void beforeCommit(TransactionContext tranCtx) throws Exception {
	}
}
