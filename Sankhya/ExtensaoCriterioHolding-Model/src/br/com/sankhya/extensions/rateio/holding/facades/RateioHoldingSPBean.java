package br.com.sankhya.extensions.rateio.holding.facades;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Timestamp;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jdom.Element;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.SPBeanUtils;
import br.com.sankhya.ws.ServiceContext;

import com.sankhya.util.XMLUtils;

public class RateioHoldingSPBean implements SessionBean {

	private SessionContext	sessionContext;

	public void gerarRateioSintetico(ServiceContext ctx) throws Exception {
		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {
			hnd = JapeSession.open();

			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

			jdbc = dwfEntityFacade.getJdbcWrapper();

			Element params = XMLUtils.getRequiredChild(ctx.getRequestBody(), "params");

			BigDecimal nuCnd = XMLUtils.getAttributeAsBigDecimalOrZero(params, "nuCnd");
			BigDecimal ordem = XMLUtils.getAttributeAsBigDecimalOrZero(params, "ordem");
			Timestamp datIni = XMLUtils.getRequiredAttributeAsTimestamp(params, "datIni");
			Timestamp datFim = XMLUtils.getRequiredAttributeAsTimestamp(params, "datFim");
			Boolean calcImpRetidos = XMLUtils.getAttributeAsBoolean(params, "calcImpRetidos");
			Timestamp dtNegociacao = XMLUtils.getRequiredAttributeAsTimestamp(params, "dtNegociacao");
			Timestamp dtVencimento = XMLUtils.getRequiredAttributeAsTimestamp(params, "dtVencimento");
			BigDecimal codTipTitulo = XMLUtils.getAttributeAsBigDecimalOrZero(params, "codTipTitulo");
			BigDecimal codTipOperacao = XMLUtils.getAttributeAsBigDecimalOrZero(params, "codTipOperacao");
			String historico = XMLUtils.getAttributeAsString(params, "historico");

			RateioHoldingHelper helper = new RateioHoldingHelper(dwfEntityFacade);
			helper.setNuCnd(nuCnd);
			helper.setOrdem(ordem);
			helper.setDtPeriodoInicial(datIni);
			helper.setDtPeriodoFinal(datFim);
			helper.setCalcImpRetidos(calcImpRetidos);
			helper.setDtNegociacao(dtNegociacao);
			helper.setDtVencimento(dtVencimento);
			helper.setCodTipTitulo(codTipTitulo);
			helper.setCodTipOperacao(codTipOperacao);
			helper.setHistorico(historico);

			helper.gerarRateioSintetico(jdbc);
		} catch (Exception e) {
			SPBeanUtils.throwExceptionRollingBack(e, sessionContext);
		} finally {
			JapeSession.close(hnd);
			JdbcWrapper.closeSession(jdbc);
		}

	}

	public void simulaRateioSintetico(ServiceContext ctx) throws MGEModelException {
		SessionHandle hnd = null;

		JdbcWrapper jdbc = null;

		try {
			hnd = JapeSession.open();

			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

			jdbc = dwfEntityFacade.getJdbcWrapper();

			Element parametrosElem = XMLUtils.getRequiredChild(ctx.getRequestBody(), "parametros");

			BigDecimal nuCnd = XMLUtils.getAttributeAsBigDecimalOrZero(parametrosElem, "nuCnd");
			BigDecimal ordem = XMLUtils.getAttributeAsBigDecimalOrZero(parametrosElem, "ordem");
			Timestamp datIni = XMLUtils.getRequiredAttributeAsTimestamp(parametrosElem, "datIni");
			Timestamp datFim = XMLUtils.getRequiredAttributeAsTimestamp(parametrosElem, "datFim");
			Boolean calcImpRetidos = XMLUtils.getAttributeAsBoolean(parametrosElem, "calcImpRetidos");

			RateioHoldingHelper helper = new RateioHoldingHelper(dwfEntityFacade);
			helper.setNuCnd(nuCnd);
			helper.setOrdem(ordem);
			helper.setDtPeriodoInicial(datIni);
			helper.setDtPeriodoFinal(datFim);
			helper.setCalcImpRetidos(calcImpRetidos);

			Element simulacaoSinteticaDataProvider = helper.getSimulacaoRateioSinteticoDataProvider(jdbc);

			ctx.getBodyElement().addContent(simulacaoSinteticaDataProvider);
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

	public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
		sessionContext = ctx;
	}
}
