package br.com.sankhya.industria.manutencao.eventoprogramados;

import java.math.BigDecimal;
import java.util.HashSet;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;

public class BloqueioAlteracaoOSManutencaoSk implements EventoProgramavelJava {

	public static final String		IGNORAR_VALIDACAO_FLOW	= "IGNORAR_VALIDACAO_FLOW";
	private static HashSet<String>	allowedPaths			= new HashSet<String>();

	static {
		allowedPaths.add("br.com.sankhya.extension.autocmp");
		allowedPaths.add("br.com.sankhya.workflow.model.services");
		allowedPaths.add("br.com.sankhya.workflow.crudlistener.ApontamentoExecucaoTarefaCrudListener");
		allowedPaths.add("br.com.sankhya.industria.manutencao.RotinaTeste");
		allowedPaths.add("br.com.sankhya.wmrs.zendesksupport.services");
		allowedPaths.add("br.com.sankhya.wmrs.zendesksupport.helper");
		allowedPaths.add("br.com.sankhya.industria.manutencao.eventoprogramados.FormularioSolicitacaoManutencao");
		allowedPaths.add("br.com.sankhya.mgeschedule");
	}

	public void beforeInsert(PersistenceEvent event) throws Exception {
		verificaAlteracaoFlow(event);
	}

	public void beforeUpdate(PersistenceEvent event) throws Exception {
		verificaAlteracaoFlow(event);
	}

	private void verificaAlteracaoFlow(PersistenceEvent e) throws Exception {
		if (JapeSession.getPropertyAsBoolean(IGNORAR_VALIDACAO_FLOW, false)) {
			return;
		}
		DynamicVO vo = (DynamicVO) e.getVo();
		BigDecimal numOS = vo.asBigDecimal("NUMOS");
		if (!alterandoPeloFlow() && temSoliman(numOS)) {
			BigDecimal codUsuAlter = vo.asBigDecimalOrZero("CODUSUALTER");
			if (ehUsuarioIndustria(codUsuAlter)) {
				if (!ehResponsavelCompilacao(numOS, codUsuAlter)) {
					throw new IllegalStateException("Esta Ordem de servio est vinculada ao processo de manuteno e no deve ser alterada fora do processo.");
				}
			}
		}
	}

	private boolean ehResponsavelCompilacao(BigDecimal numOS, BigDecimal codUsuAlter) throws Exception {
		return NativeSql.getBigDecimal("1", "TCSITE", "NUMOS = ? AND CODUSUALTER = ? AND CODUSU = 1721 AND INICEXEC IS NOT NULL AND HRINICIAL IS NOT NULL AND HRFINAL IS NOT NULL", new Object[] { numOS, codUsuAlter }) != null;
	}

	private boolean ehUsuarioIndustria(BigDecimal codUsuAlter) throws Exception {
		return NativeSql.getBigDecimal("1", "TSIUSU", "CODUSU = ? AND CODCENCUSPAD IN(10001401, 10001406, 10001404)", new Object[] { codUsuAlter }) != null;
	}

	private boolean alterandoPeloFlow() {
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		for (StackTraceElement ste : st) {
			String className = ste.getClassName();
			if (allowedPaths.contains(className)) {
				return true;
			}
			int lastDot = className.lastIndexOf('.');
			if (lastDot > -1) {
				String packageName = className.substring(0, lastDot);
				if (allowedPaths.contains(packageName)) {
					return true;
				}
			}
		}
		return false;
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

	private boolean temSoliman(BigDecimal numOS) throws Exception {
		return NativeSql.getBigDecimal("NUMOS", "AD_TWFSOLIMAN", "NUMOS = ?", new Object[] { numOS }) != null;
	}
}
