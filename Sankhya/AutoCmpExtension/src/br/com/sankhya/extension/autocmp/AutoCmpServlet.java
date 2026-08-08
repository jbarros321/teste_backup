package br.com.sankhya.extension.autocmp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.skw.environment.utils.ApiToken;
import br.com.sankhya.skw.environment.utils.EnvironmentUtil;
import br.com.sankhya.skw.environment.utils.FileUtil;

import com.sankhya.model.apps.servico.OrdemServicoSFLocal;
import com.sankhya.model.apps.servico.OrdemServicoSFLocalHome;
import com.sankhya.model.entities.vo.CabecalhoVO;
import com.sankhya.model.entities.vo.ItemVO;
import com.sankhya.util.ExceptionNavigator;
import com.sankhya.util.StringUtils;
import com.sankhya.util.XMLUtils;

public class AutoCmpServlet extends HttpServlet {
	private static final String	API_SALT			= "#C0MP1L4C40!";
	private static final long	serialVersionUID	= 1L;

	private static String		getPendenciasSql;
	private static String		getOrigemOSCompilacaoSql;

	static {
		getPendenciasSql = loadQueryFile("getPendencias.sql");
		getOrigemOSCompilacaoSql = loadQueryFile("getOrigemOSCompilacao.sql");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JdbcWrapper jdbc = null;
		ResultSet rs = null;

		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();

			NativeSql nativeSql = new NativeSql(jdbc);

			SimpleDateFormat skwDhFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

			if (getPendenciasSql == null) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

			rs = nativeSql.executeQuery(getPendenciasSql);

			Element pendencias = new Element("pendencias");

			while (rs.next()) {
				Element osElem = new Element("ordemServico");
				XMLUtils.addContentElement(osElem, "numOs", rs.getString(1));
				XMLUtils.addContentElement(osElem, "numItem", rs.getString(2));

				XMLUtils.addContentElement(osElem, "nuFap", rs.getString(3));

				String solucao = StringUtils.getNullAsEmpty(rs.getString(4));
				XMLUtils.addCDATAContentElement(osElem, "solucao", solucao);

				XMLUtils.addContentElement(osElem, "dhEntrada", skwDhFormat.format(rs.getTimestamp(5)));
				XMLUtils.addContentElement(osElem, "correcao", rs.getString(6));
				XMLUtils.addContentElement(osElem, "isSankhya", rs.getString(7));
				XMLUtils.addContentElement(osElem, "prioridade", rs.getString(8));
				XMLUtils.addContentElement(osElem, "remetente", rs.getString(9));
				XMLUtils.addContentElement(osElem, "nomeRemetente", rs.getString(10));

				String slack = rs.getString(11);

				if(slack == null) {
					slack = "#programadores";
				}

				XMLUtils.addContentElement(osElem, "slack", slack);

				pendencias.addContent(osElem);
			}

			response.setContentType("text/xml");
			XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
			xout.output(pendencias, response.getOutputStream());
			response.getOutputStream().flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			EnvironmentUtil.close(rs);
			JdbcWrapper.closeSession(jdbc);
		}
	}

	private static String loadQueryFile(String fileName) {
		InputStream in = null;

		try {
			in = AutoCmpServlet.class.getResourceAsStream(fileName);
			return FileUtil.readStream(in, StandardCharsets.UTF_8.displayName());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (Exception ignored) {
			}
		}

		return null;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Element reqContentElem = null;

		ActionResult result = null;

		InputStream in = null;

		try {
			in = request.getInputStream();
			reqContentElem = new SAXBuilder().build(in).getRootElement();
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} finally {
			EnvironmentUtil.close(in);
		}

		if (reqContentElem == null) {
			return;
		}

		AuthenticationInfo authInfo = null;
		OrdemServicoSFLocal sf = null;

		try {

			String token = XMLUtils.getRequiredAttributeAsString(reqContentElem, "token");
			String appName = XMLUtils.getRequiredAttributeAsString(reqContentElem, "appName");
			String action = XMLUtils.getRequiredAttributeAsString(reqContentElem, "action");

			if (StringUtils.getEmptyAsNull(token) == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			if (!token.equals(ApiToken.build(appName, API_SALT))) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			BigDecimal cmpCodUsu = XMLUtils.getRequiredAttributeAsBigDecimal(reqContentElem, "compilacaoCodUsu");
			String cmpNomeUsu = XMLUtils.getRequiredAttributeAsString(reqContentElem, "compilacaoNomeUsu");

			StringBuffer authID = new StringBuffer();

			authID.append(System.currentTimeMillis()).append(":0:").append(this.hashCode());

			authInfo = new AuthenticationInfo(cmpNomeUsu, cmpCodUsu, BigDecimal.ZERO, new Integer(authID.toString().hashCode()));
			authInfo.makeCurrent();

			InitialContext jndi = new InitialContext();
			OrdemServicoSFLocalHome osHome = (OrdemServicoSFLocalHome) jndi.lookup(OrdemServicoSFLocalHome.JNDI_NAME);
			sf = osHome.create(authInfo);

			switch (action) {
				case "encaminhar":
					result = encaminharOrdensServico(reqContentElem, sf);
					break;

				case "devolver":
					result = devolverOrdensServico(reqContentElem, sf);
					break;

				default:
					throw new IllegalArgumentException("Aco no implementada: " + action);
			}

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			pw.close();

			result = new ActionResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, sw.toString());
		} finally {
			AuthenticationInfo.unregistry();
			try {
				if (sf != null) {
					sf.remove();
				}
			} catch (Exception e) {
				System.err.println("Erro ao remover EJB (Stateful) utilizado pela compilao automtica!");
				e.printStackTrace();
			}
		}

		response.setContentType("text/plain");
		response.setStatus(result.returnCode);

		BufferedWriter bw = new BufferedWriter(response.getWriter());
		bw.write(result.getMsg());
		bw.flush();
	}

	private ActionResult devolverOrdensServico(Element reqContentElem, OrdemServicoSFLocal sf) throws Exception {
		ActionResult result = new ActionResult();

		@SuppressWarnings("unchecked")
		Iterator<Element> iterator = reqContentElem.getChildren("devolucao").iterator();

		Element devolucaoElem = null;

		boolean teveFalha = false;

		JdbcWrapper jdbc = null;
		PreparedStatement ps = null;

		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			jdbc.openSession();

			ps = jdbc.getConnection().prepareStatement(getOrigemOSCompilacaoSql);

			while (iterator.hasNext()) {
				devolucaoElem = iterator.next();

				BigDecimal numOs = XMLUtils.getContentChildAsBigDecimal(devolucaoElem, "numOs");
				BigDecimal numItem = XMLUtils.getContentChildAsBigDecimal(devolucaoElem, "numItem");
				String solucao = XMLUtils.getRequiredContentChildAsString(devolucaoElem, "solucao");

				ps.setBigDecimal(1, numOs);
				ps.setBigDecimal(2, numOs);
				ps.setBigDecimal(3, numItem);
				ps.setBigDecimal(4, numOs);
				ps.setBigDecimal(5, numOs);
				ps.setBigDecimal(6, numItem);

				ResultSet rs = null;

				try {
					sf.getOrdemServico(numOs.intValue(), numItem.intValue(), true);
					CabecalhoVO cabVo = sf.getCabecalhoCorrente();
					ItemVO itemVo = sf.getItemCorrente();

					rs = ps.executeQuery();
					rs.next();
					itemVo.codusu.setData(rs.getBigDecimal("CODUSU"));
					itemVo.codserv.setData(rs.getBigDecimal("CODSERV"));
					itemVo.codSit.setData(rs.getBigDecimal("CODSIT"));
					itemVo.codocoros.setData(rs.getBigDecimal("CODOCOROS"));
					itemVo.numEtapa.setData(rs.getBigDecimal("NUMETAPA"));
					itemVo.solucao.setData(solucao);

					sf.alterarOrdemDeServico(cabVo, itemVo);

					result.appendMsg("Ordem de servio devolvida! (").appendMsg(numOs).appendMsg("/").appendMsg(numItem).appendMsg(")");

				} catch (Exception e) {
					e.printStackTrace();
					result.appendMsg("\nNo foi possvel devolver o item ");
					result.appendMsg(numOs);
					result.appendMsg("\n\nMensagem: ");
					result.appendMsg(ExceptionNavigator.getLastNotEmptyMessage(e));
					result.appendMsg("\n");

					teveFalha = true;
				} finally {
					EnvironmentUtil.close(rs);
				}
			}
		} finally {
			EnvironmentUtil.close(ps);
			JdbcWrapper.closeSession(jdbc);
		}

		if (teveFalha) {
			result.returnCode = HttpServletResponse.SC_BAD_REQUEST;
		} else {
			result.returnCode = HttpServletResponse.SC_OK;
		}

		return result;
	}

	private ActionResult encaminharOrdensServico(Element reqContentElem, OrdemServicoSFLocal sf) {
		XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
		Element encaminhamentoElem = null;

		ActionResult result = new ActionResult();

		@SuppressWarnings("unchecked")
		Iterator<Element> iterator = reqContentElem.getChildren("encaminhamento").iterator();

		boolean teveFalhas = false;

		while (iterator.hasNext()) {
			encaminhamentoElem = iterator.next();

			ParametrosEncaminhamento params = ParametrosEncaminhamento.fromEncaminhamentoElement(encaminhamentoElem);

			try {

				if (!params.validate()) {
					result.appendMsg("\nErro interno, linha de XML invlida: ");
					result.appendMsg(xout.outputString(encaminhamentoElem));
					result.appendMsg(params.getErrorValidationMsg());
					continue;
				}

				sf.getOrdemServico(params.numOs.intValue(), params.numItem.intValue(), true);
				CabecalhoVO cabVo = sf.getCabecalhoCorrente();
				ItemVO itemVo = sf.getItemCorrente();

				itemVo.codusu.setData(params.codUsuDest);
				itemVo.solucao.setData(params.solucao);
				itemVo.codserv.setData(params.codServ);
				itemVo.codSit.setData(params.codSit);

				if (params.codOcoros != null) {
					itemVo.codocoros.setData(params.codOcoros);
				}

				if (params.etapa != null) {
					itemVo.numEtapa.setData(params.etapa);
				}

				sf.putTXProperty("podeConcluirEtapa", true);
				sf.alterarOrdemDeServico(cabVo, itemVo);

				result.appendMsg("\nEncaminhamento concludo: ").appendMsg(params.numOs).appendMsg("/").appendMsg(params.numItem);

			} catch (Exception e) {
				teveFalhas = true;
				e.printStackTrace();
				result.appendMsg("\nErro ao processar elemento 'encaminhamento':\n");
				result.appendMsg(xout.outputString(encaminhamentoElem));
				result.appendMsg("\n\nMensagem: ");
				result.appendMsg(ExceptionNavigator.getLastNotEmptyMessage(e));
				result.appendMsg("\n");
			}
		}

		if (teveFalhas) {
			result.returnCode = HttpServletResponse.SC_BAD_REQUEST;
		} else {
			result.returnCode = HttpServletResponse.SC_OK;
		}

		return result;
	}

	private static class ParametrosEncaminhamento {
		private BigDecimal	numOs;
		private BigDecimal	numItem;
		private BigDecimal	codUsuDest;
		private BigDecimal	etapa;
		private String		solucao;
		private BigDecimal	codServ;
		private BigDecimal	codSit;
		private BigDecimal	codOcoros;

		private Set<String>	invalidParams;

		public boolean validate() throws IllegalArgumentException {
			invalidParams = new TreeSet<String>();

			if (numOs == null) {
				invalidParams.add("nmero da O.S.");
			}

			if (numItem == null) {
				invalidParams.add("nmero do item da O.S.");
			}

			if (codUsuDest == null) {
				invalidParams.add("cdigo usurio de destino");
			}

			if (solucao == null) {
				invalidParams.add("soluo");
			}

			if (codServ == null) {
				invalidParams.add("servio");
			}

			if (codSit == null) {
				invalidParams.add("status (situao)");
			}

			return invalidParams.isEmpty();
		}

		public String getErrorValidationMsg() {
			if (invalidParams == null) {
				validate();
			}

			if (invalidParams.isEmpty()) {
				return null;
			}

			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("Parmetro de encaminhamento incompletos: ");

			for (Iterator<String> ite = invalidParams.iterator(); ite.hasNext();) {
				errorMsg.append(ite.next());

				if (ite.hasNext()) {
					errorMsg.append(", ");
				}
			}

			return errorMsg.toString();
		}

		public static ParametrosEncaminhamento fromEncaminhamentoElement(Element elem) {
			ParametrosEncaminhamento enc = new ParametrosEncaminhamento();

			enc.numOs = XMLUtils.getContentChildAsBigDecimal(elem, "numOs");
			enc.numItem = XMLUtils.getContentChildAsBigDecimal(elem, "numItem");
			enc.codUsuDest = XMLUtils.getContentChildAsBigDecimal(elem, "codUsuDest");
			enc.etapa = XMLUtils.getContentChildAsBigDecimal(elem, "etapa");
			enc.solucao = XMLUtils.getContentChildAsString(elem, "solucao");
			enc.codServ = XMLUtils.getContentChildAsBigDecimal(elem, "codServ");
			enc.codSit = XMLUtils.getContentChildAsBigDecimal(elem, "codSit");
			enc.codOcoros = XMLUtils.getContentChildAsBigDecimal(elem, "codOcoros");

			return enc;
		}
	}

	private static class ActionResult {
		private int				returnCode;
		private StringBuilder	msg;

		public ActionResult() {
			msg = new StringBuilder();
		}

		public ActionResult(int returnCode, String text) {
			this();
			this.returnCode = returnCode;
			this.msg.append(text);
		}

		public ActionResult appendMsg(Object content) {
			msg.append(content);
			return this;
		}

		public String getMsg() {
			return msg.toString();
		}
	}
}
