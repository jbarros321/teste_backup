package com.credparapp.model.services;

import java.rmi.RemoteException;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;

public class OperadoraCelularJobBean implements SessionBean{

	private SessionContext		ctx;

	@Override
	public void ejbActivate() throws EJBException, RemoteException {
	}

	@Override
	public void ejbPassivate() throws EJBException, RemoteException {
	}

	@Override
	public void ejbRemove() throws EJBException, RemoteException {
	}

	@Override
	public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
		this.ctx = ctx;
	}

	public String getScheduleConfig() throws Exception {
		String strConfig = (String) MGECoreParameter.getParameter("credparapp.conf", "credpar.agendamento.consulta.produto.operadora");
		log("configurand job para " + strConfig);
		return strConfig;
	}

	@SuppressWarnings("unchecked")
	public void onSchedule() throws Exception {
		log("executando job ..." + new Date());

		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {

			hnd = JapeSession.open();

			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			OperadoraCelularHelper helper = new OperadoraCelularHelper(null);
			helper.setPropertyJapeSession();
			helper.consultarProdutos();

		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
			log("fim da sincronizacao");
		}
	}

	private void log(String msg) {
		System.out.println("[OperadoraCelularJobBean] "+msg);
	}
}
