package br.com.sankhya.industria.manutencao.eventoprogramados;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.HashSet;

import com.sankhya.util.SQLUtils;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoConstants;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class BloqueioAlteracaoOSManutencaoSk implements EventoProgramavelJava {

	public static final String			IGNORAR_VALIDACAO_FLOW	= "IGNORAR_VALIDACAO_FLOW";
	private static HashSet<String>		allowedPaths			= new HashSet<String>();
	private static HashSet<BigDecimal>	filasLiberadas			= new HashSet<BigDecimal>();

	static {
		allowedPaths.add("br.com.sankhya.extension.autocmp");
		allowedPaths.add("br.com.sankhya.workflow.model.services");
		allowedPaths.add("br.com.sankhya.workflow.crudlistener.ApontamentoExecucaoTarefaCrudListener");
		allowedPaths.add("br.com.sankhya.industria.manutencao.RotinaTeste");
		allowedPaths.add("br.com.sankhya.wmrs.zendesksupport.services");
		allowedPaths.add("br.com.sankhya.wmrs.zendesksupport.helper");
		allowedPaths.add("br.com.sankhya.industria.manutencao.eventoprogramados.FormularioSolicitacaoManutencao");
		allowedPaths.add("br.com.sankhya.mgeschedule");

		filasLiberadas.add(ManutencaoConstants.FILA_COMPILACAO_WEB);
		filasLiberadas.add(ManutencaoConstants.FILA_OSRELEASE);
		filasLiberadas.add(ManutencaoConstants.FILA_CLOUD);
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

		if (!alterandoPeloFlow() && ManutencaoSnkUtil.temSoliman(numOS)) {

			BigDecimal codUsuAlter = vo.asBigDecimalOrZero("CODUSUALTER");
			BigDecimal codUsu = vo.asBigDecimalOrZero("CODUSU");
			BigDecimal crUsuario = NativeSql.getBigDecimal("CODCENCUSPAD", "TSIUSU", "CODUSU = ?", new Object[] { codUsuAlter });
			boolean crPaiBloqueado = (NativeSql.getBigDecimal("CODCENCUS", "TSICUS", "CODCENCUSPAI IN (?,?) AND CODCENCUS = ?", new Object[] { ManutencaoConstants.CR_DIRETORIA_DE_DESENVOLVIMENTO_DE_NEGOCIOS, ManutencaoConstants.CR_DIRETORIA_DE_TECNOLOGIA, crUsuario }) != null);

			if(crPaiBloqueado && !crUsuario.equals(ManutencaoConstants.CR_TI)) {
				if (!filasLiberadas.contains(codUsu) && !pertenceFilasLiberada(numOS, codUsuAlter)) {
					throw new IllegalStateException("Esta OS pertence ao \"Processo de Manuteno\" por isso no pode ser alterada pelo mdulo de OS. Utilize o SankhyaFlow para continuar o trabalho.");
				}
			}
		}
	}

	private boolean pertenceFilasLiberada(BigDecimal numOS, BigDecimal codUsuAlter) throws Exception {
		boolean pertence = false;
		JdbcWrapper jdbc = null;
		NativeSql query = null;
		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			query = new NativeSql(jdbc);
			query.appendSql(" SELECT ");
			query.appendSql(" 	1 ");
			query.appendSql(" FROM ");
			query.appendSql(" 	TCSITE");
			query.appendSql(" WHERE  ");
			query.appendSql("	NUMOS = :NUMOS ");
			query.appendSql(" 	AND CODUSUALTER = :CODUSUALTER ");
			query.appendSql(" 	AND ").appendSql(SQLUtils.buildINClauseByValues("CODUSU", filasLiberadas));
			query.appendSql(" 	AND INICEXEC IS NOT NULL ");
			query.appendSql(" 	AND HRINICIAL IS NOT NULL ");
			query.appendSql(" 	AND HRFINAL IS NOT NULL");

			query.setNamedParameter("NUMOS", numOS);
			query.setNamedParameter("CODUSUALTER", codUsuAlter);

			ResultSet rs = query.executeQuery();

			pertence = rs.next();

			rs.close();
		} finally {
			NativeSql.releaseResources(query);
			JdbcWrapper.closeSession(jdbc);
		}

		return pertence;
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

}
