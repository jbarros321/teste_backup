package br.com.sankhya.extensions.rateio.holding.facades;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.ResultSet;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jdom.Element;

import com.sankhya.util.XMLUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

public class DescontoVagSPBean  implements SessionBean{

		private SessionContext	context;

		public void fechamentoDescontos(ServiceContext ctx) throws MGEModelException {
			SessionHandle hnd = null;
			JdbcWrapper   jdbc = null;

			try {
				hnd = JapeSession.open();

				EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
				Element params = XMLUtils.getRequiredChild(ctx.getRequestBody(), "params");

				BigDecimal codParceiro = XMLUtils.getAttributeAsBigDecimalOrZero(params, "codParceiro");
				BigDecimal codUnidade = XMLUtils.getAttributeAsBigDecimalOrZero(params, "codUnidade");
				String 	   numContratos = XMLUtils.getAttributeAsString(params, "contratos");

				DescontoVagHelper descontoVag = new DescontoVagHelper(dwfEntityFacade,jdbc);
				descontoVag.setCodParceiro(codParceiro);
				descontoVag.setCodUnidade(codUnidade);
				descontoVag.setContratos(numContratos);

				descontoVag.fechamentoDescontoVag();
			} catch (Exception e) {
				MGEModelException.throwMe(e);
			} finally {
				JapeSession.close(hnd);
				JdbcWrapper.closeSession(jdbc);
			}
		}

		public void ejbActivate() throws EJBException, RemoteException {
		}

		public void ejbPassivate() throws EJBException, RemoteException {
		}

		public void ejbRemove() throws EJBException, RemoteException {
		}

		@Override
		public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
			this.context = ctx;
		}

	}
