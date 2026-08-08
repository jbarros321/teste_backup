package br.com.sankhya.industria.manutencao.eventoprogramados;

import java.math.BigDecimal;
import java.util.HashSet;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;

public class BloqueioTesteEntradaAberto implements EventoProgramavelJava {

	public void beforeInsert(PersistenceEvent event) throws Exception {
		verificaAlteracaoFlow(event);
	}

	public void beforeUpdate(PersistenceEvent event) throws Exception {
	}

	private void verificaAlteracaoFlow(PersistenceEvent e) throws Exception {
		DynamicVO vo = (DynamicVO) e.getVo();
		if(vo.asLong("CODUSU") == 46){
			BigDecimal numOS = vo.asBigDecimal("NUMOS");
			boolean existeTesteEntradaPendente = NativeSql.getBigDecimal("1", "TCSITE", "NUMOS = ? AND CODUSU = 46 AND HRFINAL IS NULL", new Object[] { numOS}) != null;
			if(existeTesteEntradaPendente){
				throw new IllegalStateException("Esta Ordem de servio j est aguardando o Teste de entrada.");
			}
		}
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

}
