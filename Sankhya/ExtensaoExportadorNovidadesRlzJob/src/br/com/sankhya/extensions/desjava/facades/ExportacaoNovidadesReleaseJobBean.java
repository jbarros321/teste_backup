package br.com.sankhya.extensions.desjava.facades;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.Timestamp;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jboss.logging.Logger;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.DateTimeUtil;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ExportacaoNovidadesReleaseJobBean implements SessionBean {

	private SessionContext	ctx;
	private final Logger	LOGGER	= Logger.getLogger("JOB: ExportacaoNovidadesRelease");
	private final Boolean	debug	= true;

	public void ejbActivate() throws EJBException, RemoteException {
	}

	public void ejbPassivate() throws EJBException, RemoteException {
	}

	public void ejbRemove() throws EJBException, RemoteException {
	}

	public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
		this.ctx = ctx;
	}

	@SuppressWarnings("unchecked")
	public void onSchedule() throws Exception {
		logInfo("Iniciando exportao de registros para tela Novidades de Releases!");
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;
		ResultSet rsDocOS = null;
		String osNaoAtualizadas = "";
		String osParaAtualizacao = "";

		try {
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();
			jdbc.openSession();
			Timestamp dhInicio = DateTimeUtil.getCurrentDateTimeFromDB(jdbc);
			NativeSql sqlDocOS = new NativeSql(jdbc);
			sqlDocOS.appendSql("SELECT * FROM AD_NOVIDADESRELEASES WHERE EXPORTADO = 0");
			rsDocOS = sqlDocOS.executeQuery();
			Timestamp dhTermino = DateTimeUtil.getCurrentDateTimeFromDB(jdbc);
			logDebug("Tempo de Busca de OS no exportadas: "+((dhTermino.getTime() - dhInicio.getTime())) + " ms.");
			int countReg = 0;
			int naoAtualizados = 0;
			dhInicio = DateTimeUtil.getCurrentDateTimeFromDB(jdbc);
			while (rsDocOS.next()) {
				countReg++;
				BigDecimal numOS = rsDocOS.getBigDecimal("NUMOS");

				PersistentLocalEntity novRelEntity = null;
				DynamicVO novRelVO = null;

				try {
					novRelEntity = dwfEntityFacade.findEntityByPrimaryKey("NOVIDADESRELEASES", numOS);
					novRelVO = (DynamicVO) novRelEntity.getValueObject();
				} catch (FinderException e) {
					novRelVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("NOVIDADESRELEASES");
					novRelVO.setProperty("NUMOS", numOS);
				}

				novRelVO.setProperty("TITULO", rsDocOS.getString("TITULO"));
				novRelVO.setProperty("DESCRICAO", rsDocOS.getString("DESCRICAO"));
				novRelVO.setProperty("DHALTER", rsDocOS.getDate("DHALTER").toString());

				try {
					if (novRelEntity == null) {
						dwfEntityFacade.createEntity("NOVIDADESRELEASES", (EntityVO) novRelVO);
					} else {
						novRelEntity.setValueObject((EntityVO) novRelVO);
					}

					osParaAtualizacao += ", "+numOS;
				} catch (Exception e) {
					this.ctx.setRollbackOnly();
					osNaoAtualizadas += ", "+numOS;
					naoAtualizados++;
				}
			}
			dhTermino = DateTimeUtil.getCurrentDateTimeFromDB(jdbc);
			logDebug("Tempo de Exportao (ORACLE -> MYSQL) da documentao das OS no exportadas: "+((dhTermino.getTime() - dhInicio.getTime())) + " ms.");

			if (osParaAtualizacao != "") {
				dhInicio = DateTimeUtil.getCurrentDateTimeFromDB(jdbc);
				osParaAtualizacao = osParaAtualizacao.substring(2);
				NativeSql sqlUpdDocOS = new NativeSql(jdbc);
				sqlUpdDocOS.appendSql("UPDATE AD_NOVIDADESRELEASES SET EXPORTADO = 1 WHERE NUMOS IN ("+osParaAtualizacao+")");
				if (osNaoAtualizadas != "") {
					osNaoAtualizadas = osNaoAtualizadas.substring(2);
					sqlUpdDocOS.appendSql(" AND NUMOS NOT IN ("+osNaoAtualizadas+")");
				}
				boolean atualizado = sqlUpdDocOS.executeUpdate();
				dhTermino = DateTimeUtil.getCurrentDateTimeFromDB(jdbc);
				logDebug("Tempo de Atualizao da documentao j exportada: "+((dhTermino.getTime() - dhInicio.getTime())) + " ms.");
				if (!atualizado) {
					logError("Erro ao atualizar OS (" + osParaAtualizacao + ").");
				}

			}
			logInfo("Exportado "+(countReg-naoAtualizados)+" de "+countReg+" registros.");
			if(!osParaAtualizacao.isEmpty()){
				logInfo("OS Filtradas para atualizao: ("+osParaAtualizacao+")");
			}
			if(!osNaoAtualizadas.isEmpty()){
				logError("OS com erro de atualizao: ("+osNaoAtualizadas+")");
			}
		} catch (Exception e) {
			this.ctx.setRollbackOnly();
			logError(e);
		} finally {
			if(rsDocOS != null) rsDocOS.close();
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void logDebug(String msg) {
		if (this.debug) {
			LOGGER.info(msg);
		}
	}

	public void logInfo(String msg) {
		LOGGER.info(msg);
	}

	public void logError(Exception e) {
		for (StackTraceElement ste : e.getStackTrace()) {
			LOGGER.error(ste);
		}
		LOGGER.error(e.getLocalizedMessage());
	}

	public void logError(String msg) {
		LOGGER.error(msg);
	}
}
