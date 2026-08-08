package com.credparapp.model.services;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jdom.Element;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sankhya.util.JsonUtils;
import com.sankhya.util.StringUtils;
import com.sankhya.util.XMLUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.BaseSPBean;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

public class RecargaCelularSPBean extends BaseSPBean implements SessionBean {

	public void getOperadorasAtivas(ServiceContext ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			OperadoraCelularHelper helper = new OperadoraCelularHelper(ctx);
			Collection<DynamicVO> operadoras = helper.getOperadorasAtivas();

			JsonArray opeJson = new JsonArray();
			for (DynamicVO dynamicVO : operadoras) {
				JsonObject jo = new JsonObject();
				jo.addProperty("data", dynamicVO.asBigDecimal("NUOPERADORA").toString());
				jo.addProperty("value", dynamicVO.asString("NOMEOPERADORA"));

				opeJson.add(jo);
			}

			JsonObject jo = new JsonObject();
			jo.add("operadoras", opeJson);
			ctx.setJsonResponse(jo);
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void getProdutosOperadoras(ServiceContext ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			BigDecimal nuOperadora = JsonUtils.getBigDecimal(ctx.getJsonRequestBody(), "nuOperadora");
			if(nuOperadora == null) {
				throw new Exception("Operadora no informado.");
			}

			OperadoraCelularHelper helper = new OperadoraCelularHelper(ctx);
			Collection<DynamicVO> produtos = helper.getProdutos(nuOperadora);

			JsonArray prodJson = new JsonArray();
			for (DynamicVO dynamicVO : produtos) {
				JsonObject jo = new JsonObject();
				jo.addProperty("data", dynamicVO.asString("CODPROD"));
				jo.addProperty("value", dynamicVO.asString("NOMEPRODUTO"));

				BigDecimal vlrProdMin = dynamicVO.asBigDecimalOrZero("VLRMINPROD");
				BigDecimal vlrProdMax = dynamicVO.asBigDecimalOrZero("VLRMAXPROD");
				jo.addProperty("vlrProdMin", vlrProdMin.toString());
				jo.addProperty("vlrProdMax", vlrProdMax.toString());

				jo.addProperty("variavel", "1".equals(dynamicVO.asString("PRECOVARPROD")));

				BigDecimal vlrRecarga = dynamicVO.asBigDecimalOrZero("PRECOVENDAPROD");
				jo.addProperty("vlrRecarga", vlrRecarga.toString());
				prodJson.add(jo);
			}

			JsonObject jo = new JsonObject();
			jo.add("produtos", prodJson);
			ctx.setJsonResponse(jo);
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void consultar(ServiceContext ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {

			hnd = JapeSession.open();

			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			OperadoraCelularHelper helper = new OperadoraCelularHelper(ctx);
			helper.setPropertyJapeSession();
			helper.consultarProdutos();

		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void confirmaRecarga(ServiceContext ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {

			hnd = JapeSession.open();

			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			OperadoraCelularHelper helper = new OperadoraCelularHelper(ctx);
			helper.setPropertyJapeSession();
			JsonObject jo = ctx.getJsonRequestBody();
			helper.fazerRecarga(jo);

			if(!helper.recargaConfirmada()) {
				throw new Exception("No foi possvel realizar a recarga. Cdigo retornado pela integradora: " + helper.getRetornoRecarga().codRetConfirmacao);
			} else {
				JsonObject joRet = new JsonObject();
				String msgStatus = "Recarga efetuada.";

				if(!StringUtils.isEmpty(helper.getRetornoRecarga().mensagem)) {
					msgStatus += " <br>Mensagem da operadora: " + helper.getRetornoRecarga().mensagem;
				}

				joRet.addProperty("status", msgStatus);

				ctx.setJsonResponse(joRet);
			}

		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			JapeSessionContext.removeProperty("credpar.ext.recarga");
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void getOperadorasAtivasPorDdd(ServiceContext ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {
			String ddd = XMLUtils.getContentChildAsString(ctx.getRequestBody(), "ddd");
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			OperadoraCelularHelper helper = new OperadoraCelularHelper(ctx);
			Collection<DynamicVO> operadoras = helper.getOperadorasAtivasPorDdd(ddd);

			Element operadorasElem = new Element("operadoras");
			for (DynamicVO dynamicVO : operadoras) {
				Element operadoraElem = new Element("operadora");
				Element nomeElem = new Element("nome");
				nomeElem.addContent(dynamicVO.asString("NOMEOPERADORA"));

				Element valorElem = new Element("valor");
				valorElem.addContent(dynamicVO.asBigDecimal("NUOPERADORA").toString());

				operadoraElem.addContent(nomeElem);
				operadoraElem.addContent(valorElem);
				operadorasElem.addContent(operadoraElem);
			}
			ctx.getBodyElement().addContent(operadorasElem);
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void getOperadorasAtivasPorDddAsJson(ServiceContext ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {
			String ddd = JsonUtils.getString(ctx.getJsonRequestBody(), "ddd");
			if(ddd == null) {
				throw new Exception("DDD deve ser informado");
			}

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			OperadoraCelularHelper helper = new OperadoraCelularHelper(ctx);
			Collection<DynamicVO> operadoras = helper.getOperadorasAtivasPorDdd(ddd);

			JsonArray operadorasJson = new JsonArray();
			for (DynamicVO dynamicVO : operadoras) {
				JsonObject joOp = new JsonObject();
				joOp.addProperty("value", dynamicVO.asString("NOMEOPERADORA"));

				joOp.addProperty("data", dynamicVO.asBigDecimal("NUOPERADORA").toString());
				operadorasJson.add(joOp);
			}
			JsonObject joRet = new JsonObject();
			joRet.add("operadoras", operadorasJson);
			ctx.setJsonResponse(joRet);
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void getProdutosOperadora(ServiceContext ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {
			BigDecimal nuOperadora = XMLUtils.getContentChildAsBigDecimal(ctx.getRequestBody(), "operadora");
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			OperadoraCelularHelper helper = new OperadoraCelularHelper(ctx);
			Collection<DynamicVO> operadoras = helper.getProdutos(nuOperadora);

			Element produtosElem = new Element("produtos");
			for (DynamicVO dynamicVO : operadoras) {
				Element produtoElem = new Element("produto");

				BigDecimal vlrProdMin = dynamicVO.asBigDecimalOrZero("VLRMINPROD");
				BigDecimal vlrProdMax = dynamicVO.asBigDecimalOrZero("VLRMAXPROD");
				BigDecimal vlrRecarga = dynamicVO.asBigDecimalOrZero("PRECOVENDAPROD");

				XMLUtils.addContentElement(produtoElem, "vlrMin",  vlrProdMin.toString());
				XMLUtils.addContentElement(produtoElem, "vlrMax", vlrProdMax.toString());

				XMLUtils.addContentElement(produtoElem, "variavel", "1".equals(dynamicVO.asString("PRECOVARPROD")));
				XMLUtils.addContentElement(produtoElem, "vlrRecarga", vlrRecarga.toString());

				Element nomeElem = new Element("nome");
				nomeElem.addContent(dynamicVO.asString("NOMEPRODUTO"));

				Element valorElem = new Element("codprod");
				valorElem.addContent(dynamicVO.asString("CODPROD").toString());

				produtoElem.addContent(nomeElem);
				produtoElem.addContent(valorElem);
				produtosElem.addContent(produtoElem);
			}
			ctx.getBodyElement().addContent(produtosElem);
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
		this.context = ctx;
	}

}
