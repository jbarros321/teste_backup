package com.credparapp.model.services;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jdom.Element;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.SQLUtils;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import com.sankhya.util.XMLUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.BaseSPBean;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.sankhya.ws.ServiceContext;

public class CredParSPBean extends BaseSPBean implements SessionBean {

	private static final SimpleDateFormat ddMMyyy = new SimpleDateFormat("dd/MM/yyyy");

	public void buscarServidores(ServiceContext ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			Collection<DynamicVO> colServidores = dwfEntityFacade.findByDynamicFinderAsVO(new FinderWrapper("CADSERV", "this.ATIVO = 'S'"));

			Element elementServ = new Element("servidores");

			for (DynamicVO servidorVO : colServidores) {
				Element servidor = new Element("options");

				XMLUtils.addContentElement(servidor, "IPSERV", servidorVO.asString("IPSERV"));
				XMLUtils.addContentElement(servidor, "NOMESERV", servidorVO.asString("NOMESERV"));
				XMLUtils.addContentElement(servidor, "CODSERV", String.valueOf(servidorVO.asInt("CODSERV")));
				XMLUtils.addContentElement(servidor, "PORTSERV", String.valueOf(servidorVO.asInt("PORTSERVER")));
				XMLUtils.addContentElement(servidor, "ATIVO", servidorVO.asString("ATIVO"));

				elementServ.addContent(servidor);
			}

			ctx.getBodyElement().addContent(elementServ);

		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	@SuppressWarnings("unlikely-arg-type")
	public void buscaCidadesSegmentos(ServiceContext ctx) throws Exception {

		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();
			jdbc.openSession();
			String pesquisa = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "DESCRICAO");
			String tipoPesquisa = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "TIPOPESQUISA");
			NativeSql sqlBuscarCidadesSeguimentos = new NativeSql(jdbc);

			if ("cidade".equals(tipoPesquisa) || pesquisa.isEmpty()) {

				sqlBuscarCidadesSeguimentos.loadSql(CredParClienteSPBean.class, "CredParClienteSPBean_buscaCidades.sql");

			} else if("segmento".equals(tipoPesquisa)) {

				sqlBuscarCidadesSeguimentos.loadSql(CredParClienteSPBean.class, "CredParClienteSPBean_buscaSegmentos.sql");

			}

			sqlBuscarCidadesSeguimentos.setNamedParameter("DESCRICAO", pesquisa);
			ResultSet rs = sqlBuscarCidadesSeguimentos.executeQuery();
			JsonArray json = new JsonArray();
			JsonObject segmento = null;
			JsonObject cidade = null;
			JsonArray parceiros = null;
			JsonObject parceiro = null;
			JsonArray segmentos = null;
			JsonObject endereco = null;
			JsonArray enderecos = null;

			while (rs.next()) {

				if (cidade == null || !rs.getString("NOMECID").equals(cidade.get("label").getAsString())) {
					cidade = new JsonObject();
					segmentos = new JsonArray();
					cidade.addProperty("label", rs.getString("NOMECID"));
					cidade.add("children", segmentos);
					json.add(cidade);

					if ("segmento".equals(tipoPesquisa)) {

						segmento = new JsonObject();
						segmento.addProperty("label", rs.getString("SEGMENTO"));
						segmentos.add(segmento);
						parceiros = new JsonArray();
						segmento.add("children", parceiros);

					}
				}

				if (segmento == null || !rs.getString("SEGMENTO").equals(segmento.get("label").getAsString())) {
						segmento = new JsonObject();
						segmento.addProperty("label", rs.getString("SEGMENTO"));
						segmentos.add(segmento);
						parceiros = new JsonArray();
						segmento.add("children", parceiros);
				}

				parceiro = new JsonObject();
				parceiro.addProperty("label", rs.getString("NOMEPARC"));
				parceiros.add(parceiro);
				enderecos = new JsonArray();
				parceiro.add("children", enderecos);
				endereco = new JsonObject();
				endereco.addProperty("label", rs.getString("ENDERECO"));
				endereco.addProperty("labelLast", "Telefone: " +  StringUtils.formataTelefone3(rs.getString("TELEFONE")));
				enderecos.add(endereco);

			}
			ctx.getBodyElement().addContent((json.toString()));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void buscarAutorizacoesBordero(ServiceContext ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			Element paramsElem = XMLUtils.getRequiredChild(ctx.getRequestBody(), "params");

			BigDecimal codParc = XMLUtils.getRequiredAttributeAsBigDecimal(paramsElem, "CODPARC");
			Timestamp dtNegIni = new Timestamp(Long.valueOf(XMLUtils.getRequiredAttributeAsString(paramsElem, "DTNEGINI")));
			Timestamp dtNegFin = new Timestamp(Long.valueOf(XMLUtils.getRequiredAttributeAsString(paramsElem, "DTNEGFIN")));

			dtNegIni = new Timestamp(TimeUtils.getDayStart(dtNegIni.getTime()));
			dtNegFin = new Timestamp(TimeUtils.getDayEnd(dtNegFin.getTime()));

			System.out.println(dtNegIni);
			System.out.println(dtNegFin);

			NativeSql sqlBuscarExtrato = new NativeSql(jdbc);
			sqlBuscarExtrato.loadSql(CredParClienteSPBean.class, "CredParApp_buscarExtratoBordero.sql");
			sqlBuscarExtrato.setNamedParameter("CODPARC", codParc);
			sqlBuscarExtrato.setNamedParameter("DTINI", dtNegIni);
			sqlBuscarExtrato.setNamedParameter("DTFIN", dtNegFin);

			insertFiltroTopExtratoBordero(sqlBuscarExtrato);

			ResultSet rs = sqlBuscarExtrato.executeQuery();

			Element elementServ = new Element("extrato");

			NativeSql sqlBuscarCliente = new NativeSql(jdbc);
			sqlBuscarCliente.loadSql(CredParClienteSPBean.class, "CredParApp_buscaClienteBordero.sql");
			sqlBuscarCliente.setReuseStatements(true);

			BigDecimal totalExtrato = BigDecimal.ZERO;
			while (rs.next()) {
				Element bordero = new Element("bordero");

				XMLUtils.addContentElement(bordero, "CONTRATO", String.valueOf(rs.getBigDecimal("NUMNOTA")));
				XMLUtils.addContentElement(bordero, "DESDOBRAMENTO", String.valueOf(rs.getInt("DESDOBRAMENTO")));
				XMLUtils.addContentElement(bordero, "DTVENC", ddMMyyy.format(rs.getTimestamp("DTVENC")));
				XMLUtils.addContentElement(bordero, "DTNEG", ddMMyyy.format(rs.getTimestamp("DTNEG")));
				XMLUtils.addContentElement(bordero, "VALOR", BigDecimalUtil.toCurrency(rs.getBigDecimal("VLRDESDOB")));

				sqlBuscarCliente.setNamedParameter("CODPARC", codParc);
				sqlBuscarCliente.setNamedParameter("NUMNOTA", rs.getBigDecimal("NUMNOTA"));
				ResultSet rsCliente = sqlBuscarCliente.executeQuery();

				if (rsCliente.next()) {
					XMLUtils.addContentElement(bordero, "NOME", rsCliente.getString("NOMEPARC"));
				}
				rsCliente.close();

				totalExtrato = totalExtrato.add(rs.getBigDecimal("VLRDESDOB"));

				elementServ.addContent(bordero);
			}

			NativeSql.releaseResources(sqlBuscarCliente);

			XMLUtils.addContentElement(elementServ, "totalBordero", BigDecimalUtil.toCurrency(totalExtrato).toString());

			ctx.getBodyElement().addContent(elementServ);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void buscarExtratoVendas(ServiceContext ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			Element paramsElem = XMLUtils.getRequiredChild(ctx.getRequestBody(), "params");

			BigDecimal codParc = XMLUtils.getRequiredAttributeAsBigDecimal(paramsElem, "CODPARC");
			Timestamp dtNegIni = new Timestamp(Long.valueOf(XMLUtils.getRequiredAttributeAsString(paramsElem, "DTNEGINI")));
			Timestamp dtNegFim = new Timestamp(Long.valueOf(XMLUtils.getRequiredAttributeAsString(paramsElem, "DTNEGFIM")));

			dtNegIni = new Timestamp(TimeUtils.getDayStart(dtNegIni.getTime()));
			dtNegFim = new Timestamp(TimeUtils.getDayEnd(dtNegFim.getTime()));

			NativeSql sqlBuscarExtrato = new NativeSql(jdbc);
			sqlBuscarExtrato.loadSql(CredParClienteSPBean.class, "CredParApp_buscarExtratoVendas.sql");
			sqlBuscarExtrato.setNamedParameter("CODPARC", codParc);
			sqlBuscarExtrato.setNamedParameter("DTINI", dtNegIni);
			sqlBuscarExtrato.setNamedParameter("DTFIN", dtNegFim);

			insertFiltroTopExtratoBordero(sqlBuscarExtrato);

			ResultSet rs = sqlBuscarExtrato.executeQuery();

			Element elementServ = new Element("extrato");

			BigDecimal totalExtrato = BigDecimal.ZERO;

			while (rs.next()) {
				Element vendas = new Element("vendas");

				XMLUtils.addContentElement(vendas, "CONTRATO", String.valueOf(rs.getBigDecimal("NUMNOTA")));
				XMLUtils.addContentElement(vendas, "NOMEPARC", rs.getString("NOMEPARC"));
				XMLUtils.addContentElement(vendas, "DTVENDA", ddMMyyy.format(rs.getTimestamp("DTNEG")));
				XMLUtils.addContentElement(vendas, "PLANO", rs.getString("HISTORICO"));
				XMLUtils.addContentElement(vendas, "VALOR", BigDecimalUtil.toCurrency(rs.getBigDecimal("VLRDESDOB")));

				totalExtrato = totalExtrato.add(rs.getBigDecimal("VLRDESDOB"));

				elementServ.addContent(vendas);
			}

			XMLUtils.addContentElement(elementServ, "totalExtrato", BigDecimalUtil.toCurrency(totalExtrato));

			ctx.getBodyElement().addContent(elementServ);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void insertFiltroTopExtratoBordero(NativeSql sql) throws Exception {
		String filtroTopParam = (String) MGECoreParameter.getParameter("credparapp.conf", "credpar.ignora.top.bordero.extrato");

		if (StringUtils.isNotEmpty(filtroTopParam)) {
			String inClause = SQLUtils.buildNOTINClauseByValues("FIN.CODTIPOPER", filtroTopParam);
			sql.replaceSQLComment("FILTRO_TOP", "AND " + inClause);
		}
	}

	public void buscarCidades(ServiceContext ctx) throws Exception {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			FinderWrapper finder = new FinderWrapper("Cidade", "this.CODCID > 0 AND this.CODCID < 600");
			finder.setOrderBy("this.NOMECID");
			finder.setMaxResults(-1);

			Collection<DynamicVO> cidadesCol = dwfEntityFacade.findByDynamicFinderAsVO(finder);

			Element cidades = new Element("result");

			for (DynamicVO cidadeVO : cidadesCol) {
				Element row = new Element("row");
				row.setAttribute("CODCID", cidadeVO.asBigDecimal("CODCID").toString());
				XMLUtils.addContentElement(row, "NOMECID", cidadeVO.asString("NOMECID"));
				XMLUtils.addContentElement(row, "CODCID", cidadeVO.asBigDecimal("CODCID"));

				cidades.addContent(row);
			}

			ctx.getBodyElement().addContent(cidades);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void esqueciMinhaSenha(ServiceContext ctx) throws MGEModelException {
		SessionHandle hnd = null;

		try {
			String usuario = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "usuario");

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

			Collection<PersistentLocalEntity> contatos = dwfEntityFacade.findByDynamicFinder(new FinderWrapper(DynamicEntityNames.CONTATO, "upper(this.EMAIL) = ?", new Object[] { StringUtils.getNullAsEmpty(usuario).trim().toUpperCase() }));

			if (contatos.isEmpty() || (contatos.size() > 1)) {
				throw new IllegalArgumentException("Cadastro no localizado, verifique se o e-mail informado est correto e tente novamente.");
			}

			PersistentLocalEntity contato = contatos.iterator().next();
			DynamicVO contatoVO = (DynamicVO) contato.getValueObject();

			DynamicVO parceiroVO = contatoVO.asDymamicVO("Parceiro");

			Random gerador = new Random();
			int novaSenha = gerador.nextInt(999999);

			contatoVO.setProperty("SENHAACESSO", getMD5Hexa(String.valueOf(novaSenha)));

			contato.setValueObject((EntityVO) contatoVO);

			StringBuffer mensagem = new StringBuffer();
			mensagem.append("Ol ").append(contatoVO.asString("NOMECONTATO")).append(",");
			mensagem.append("<br><br>Foi solicitado uma alterao de senha para entrar no Aplicativo Credpar.");
			mensagem.append("<br>Sua nova senha : ").append(novaSenha);
			mensagem.append("<br><br>Caso voc no tenha solicitado esta alterao, entre em contato com a Credpar.");

			DynamicVO emailFilaVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.FILA_MSG);
			emailFilaVO.setProperty("ASSUNTO", "Alterao de senha Credpar");
			emailFilaVO.setProperty("MENSAGEM", new String(mensagem.toString().getBytes("ISO-8859-1"), "ISO-8859-1").toCharArray());
			emailFilaVO.setProperty("EMAIL", parceiroVO.asString("EMAIL"));
			emailFilaVO.setProperty("CODUSUREMET", BigDecimal.ZERO);
			emailFilaVO.setProperty("TIPOENVIO", "E");
			emailFilaVO.setProperty("CODCON", BigDecimal.ZERO);
			emailFilaVO.setProperty("MAXTENTENVIO", BigDecimal.valueOf(3));
			emailFilaVO.setProperty("STATUS", "Pendente");
			emailFilaVO.setProperty("MIMETYPE", "text/html");

			dwfEntityFacade.createEntity(DynamicEntityNames.FILA_MSG, (EntityVO) emailFilaVO);

		} catch (Exception e) {
			throwExceptionRollingBack(e);
		} finally {
			JapeSession.close(hnd);
		}
	}

	public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
		this.context = ctx;
	}

	private String getMD5Hexa(String texto) throws Exception {

		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] textoMD5 = md.digest(texto.getBytes("ISO-8859-1"));
		return StringUtils.toHexString(textoMD5);

	}
}
