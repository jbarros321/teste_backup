package com.credparapp.model.services;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Random;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jdom.Element;

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

import com.sankhya.util.Base64Impl;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import com.sankhya.util.XMLUtils;

public class CredParClienteSPBean extends BaseSPBean implements SessionBean {

	private static final SimpleDateFormat	SDF_VENCIMENTO_CARTAO	= new SimpleDateFormat("MM/yyyy");
	public static void main(String[] args) {
		System.getProperty("jboss.node.name");
	}

	public void loginCliente(ServiceContext ctx) throws MGEModelException {
		SessionHandle hnd = null;

		try {
			String login = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "ID");
			login = new String(Base64Impl.decode(login));
			String interno = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "INTERNO");

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

			Collection<DynamicVO> clientes = dwfEntityFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.PARCEIRO, "this.CGC_CPF = ? OR this.CODPARC = ?", new Object[] { login, login }));

			if (clientes.size() == 0) {
				throw new Exception("Usurio ou senha invlidos.");
			} else {
				DynamicVO clienteVO = clientes.iterator().next();

				if (StringUtils.getEmptyAsNull(clienteVO.asString("INTERNOCREDPAR")) == null) {
					if (!getMd5(login).equals(interno)) {
						throw new Exception("Usurio ou senha invlidos.");
					}
				} else if (!clienteVO.asString("INTERNOCREDPAR").equals(interno)) {
					throw new Exception("Usurio ou senha invlidos.");
				}

				String nomeCliente = clienteVO.asString("NOMEPARC");
				if (nomeCliente.indexOf(" ") > -1) {
					nomeCliente = nomeCliente.substring(0, nomeCliente.indexOf(" "));
				}
				nomeCliente = toFirstCaseUp(nomeCliente);

				ctx.getBodyElement().addContent(new Element("name").setText(Base64Impl.encode(nomeCliente.getBytes())));
				ctx.getBodyElement().addContent(new Element("id").setText(Base64Impl.encode(clienteVO.asString("CGC_CPF").trim().getBytes())));
			}

		} catch (Exception e) {
				throwExceptionRollingBack(e);
		} finally {
			JapeSession.close(hnd);
		}
	}

	public void getInfoSaldo(ServiceContext ctx) throws MGEModelException {
		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();
			jdbc.openSession();

			String id = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "ID");
			id = new String(Base64Impl.decode(id));

			Collection<DynamicVO> clientes = dwfEntityFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.PARCEIRO, "this.CGC_CPF = ?", new Object[] { id }));

			if (clientes.size() == 0) {
				ctx.getBodyElement().addContent(new Element("mensagem").setText("Cliente no localizado.\nEntre em contato com a Credpar."));
				return;
			} else {
				DynamicVO clienteVO = clientes.iterator().next();
				BigDecimal limiteUtilizado = BigDecimal.ZERO;

				NativeSql sqlLimiteUtilizado = new NativeSql(jdbc, CredParClienteSPBean.class, "CredParClienteSPBean_limiteUtilizado.sql");
				sqlLimiteUtilizado.setNamedParameter("CODPARC", clienteVO.asBigDecimal("CODPARC"));

				ResultSet rsSaldo = sqlLimiteUtilizado.executeQuery();

				if (rsSaldo.next()) {
					limiteUtilizado = BigDecimalUtil.getValueOrZero(rsSaldo.getBigDecimal(1));
				}

				ctx.getBodyElement().addContent(new Element("nomeCartao").setText(clienteVO.asString("NOMEPARC")));
				ctx.getBodyElement().addContent(new Element("numeroCartao").setText(clienteVO.asBigDecimal("CODPARC").toString()));
				ctx.getBodyElement().addContent(new Element("limiteTotal").setText(BigDecimalUtil.toCurrency(clienteVO.asBigDecimalOrZero("LIMCRED"))));
				ctx.getBodyElement().addContent(new Element("limiteUtilizado").setText(BigDecimalUtil.toCurrency(limiteUtilizado)));
				ctx.getBodyElement().addContent(new Element("saldo").setText(BigDecimalUtil.toCurrency(clienteVO.asBigDecimalOrZero("LIMCRED").subtract(limiteUtilizado))));

				rsSaldo.close();
			}

		} catch (Exception e) {
			MGEModelException.throwMe(e);
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void getInfoExtrato(ServiceContext ctx) throws MGEModelException {
		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();
			jdbc.openSession();

			String id = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "ID");
			id = new String(Base64Impl.decode(id));

			Collection<DynamicVO> clientes = dwfEntityFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.PARCEIRO, "this.CGC_CPF = ?", new Object[] { id }));

			if (clientes.size() == 0) {
				ctx.getBodyElement().addContent(new Element("mensagem").setText("Cliente no localizado.\nEntre em contato com a Credpar."));
				return;
			} else {
				DynamicVO clienteVO = clientes.iterator().next();

				BigDecimal limiteUtilizado = BigDecimal.ZERO;
				BigDecimal codParc = clienteVO.asBigDecimal("CODPARC");

				NativeSql sqlExtrato = new NativeSql(jdbc, CredParClienteSPBean.class, "CredParClienteSPBean_extrato.sql");
				sqlExtrato.setNamedParameter("CODPARC", codParc);
				ResultSet rsExtrato = sqlExtrato.executeQuery();

				Element itens = new Element("itens");

				ResultSet rsParcelas = null;

				NativeSql sqlBuscaQtdParcelas = new NativeSql(jdbc);
				sqlBuscaQtdParcelas.appendSql(" SELECT COUNT(1) FROM TGFFIN FIN");
				sqlBuscaQtdParcelas.appendSql(" WHERE ");
				sqlBuscaQtdParcelas.appendSql(" FIN.CODPARC = :CODPARC AND");
				sqlBuscaQtdParcelas.appendSql(" FIN.NUMNOTA = :NUMNOTA ");
				sqlBuscaQtdParcelas.setReuseStatements(true);

				while (rsExtrato.next()) {
					BigDecimal numNota = rsExtrato.getBigDecimal("NUMNOTA");

					String nomeLojista = rsExtrato.getString("NOMELOGISTA");
					BigDecimal codLojista = rsExtrato.getBigDecimal("CODLOGISTA");

					sqlBuscaQtdParcelas.setNamedParameter("CODPARC", codParc);
					sqlBuscaQtdParcelas.setNamedParameter("NUMNOTA", numNota);

					rsParcelas = sqlBuscaQtdParcelas.executeQuery();

					BigDecimal numeroParc = BigDecimal.ZERO;

					if(rsParcelas.next()){
						numeroParc = rsParcelas.getBigDecimal(1);
					}else{
						continue;
					}

					limiteUtilizado = limiteUtilizado.add(BigDecimalUtil.getRounded(rsExtrato.getBigDecimal("VLRDESDOB"), 2));

					Element item = new Element("item");
					item.addContent(new Element("local").setText(nomeLojista));
					item.addContent(new Element("parcela").setText(rsExtrato.getBigDecimal("NROPARCELA").toString()));
					item.addContent(new Element("codloj").setText(codLojista.toString()));
					item.addContent(new Element("parcelaTotal").setText(numeroParc.toString()));
					item.addContent(new Element("valor").setText(BigDecimalUtil.toCurrency(rsExtrato.getBigDecimal("VLRDESDOB"))));
					item.addContent(new Element("data").setText(TimeUtils.formataDDMMYYYY(rsExtrato.getTimestamp("DTVENC"))));
					itens.addContent(item);
				}

				if(rsParcelas != null){
					rsParcelas.close();
				}

				BigDecimal valorDisponivel = BigDecimalUtil.getRounded(clienteVO.asBigDecimalOrZero("LIMCRED").subtract(limiteUtilizado), 2);
				BigDecimal percentualUsado = BigDecimal.ZERO;

				if(limiteUtilizado.compareTo(BigDecimal.ZERO) != 0){
					if(clienteVO.asBigDecimalOrZero("LIMCRED").doubleValue() > 0){
						percentualUsado = BigDecimalUtil.getRounded(limiteUtilizado.divide(clienteVO.asBigDecimalOrZero("LIMCRED"), BigDecimalUtil.MATH_CTX).multiply(BigDecimalUtil.CEM_VALUE), 2);
					}
				}

				ctx.getBodyElement().addContent(itens);

				ctx.getBodyElement().addContent(new Element("numeroCartao").setText(clienteVO.asBigDecimal("CODPARC").toString()));
				ctx.getBodyElement().addContent(new Element("valorUsado").setText(limiteUtilizado.toString()));
				ctx.getBodyElement().addContent(new Element("valorDisponivel").setText(valorDisponivel.toString()));
				ctx.getBodyElement().addContent(new Element("valorDisponivelFormatado").setText(BigDecimalUtil.toCurrency(valorDisponivel)));
				ctx.getBodyElement().addContent(new Element("percUsado").setText(percentualUsado.toString()));
				ctx.getBodyElement().addContent(new Element("valorUsadoFormatado").setText(BigDecimalUtil.toCurrency(limiteUtilizado)));
			}
		} catch (Exception e) {
			MGEModelException.throwMe(e);
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	public void getInfoContato(ServiceContext ctx) throws MGEModelException {
		SessionHandle hnd = null;

		try {
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

			String id = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "ID");
			id = new String(Base64Impl.decode(id));

			Collection<DynamicVO> clientes = dwfEntityFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.PARCEIRO, "this.CGC_CPF = ?", new Object[] { id }));

			if (clientes.size() > 0) {
				DynamicVO clienteVO = clientes.iterator().next();

				ctx.getBodyElement().addContent(new Element("email").setText(clienteVO.asString("EMAIL")));
				ctx.getBodyElement().addContent(new Element("telefone").setText(clienteVO.asString("TELEFONE")));
			}

		} catch (Exception e) {
			MGEModelException.throwMe(e);
		} finally {
			JapeSession.close(hnd);
		}
	}

	public void enviarEmail(ServiceContext ctx) throws MGEModelException {
		SessionHandle hnd = null;

		try {
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

			String cpf = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "cpf");
			String email = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "email");
			String telefone = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "telefone");

			Collection<DynamicVO> clientes = dwfEntityFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.PARCEIRO, "this.CGC_CPF = ?", new Object[] { cpf }));

			if (clientes.size() == 0) {
				throw new Exception("Cliente no localizado.\nEntre em contato com a Credpar.");
			} else {
				DynamicVO clienteVO = clientes.iterator().next();

				StringBuffer mensagem = new StringBuffer();
				mensagem.append("Ol,");
				mensagem.append("<br><br>O cliente abaixo solicitou um contato atravs do aplicativo CredPar. Seguem os dados:");
				mensagem.append("<br><br><b>Nome:</b> ").append(clienteVO.asString("NOMEPARC"));
				mensagem.append("<br><b>CPF:</b> ").append(cpf);
				mensagem.append("<br><b>E-mail:</b> ").append(email);
				mensagem.append("<br><b>Telefone:</b> ").append(telefone);

				DynamicVO emailFilaVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.FILA_MSG);
				emailFilaVO.setProperty("ASSUNTO", "Solicitao de contato ( Cliente )");
				emailFilaVO.setProperty("MENSAGEM", new String(mensagem.toString().getBytes("ISO-8859-1"), "ISO-8859-1").toCharArray());
				emailFilaVO.setProperty("EMAIL", MGECoreParameter.getParameter("credparapp.conf", "credpar.email.contato"));
				emailFilaVO.setProperty("CODUSUREMET", BigDecimal.ZERO);
				emailFilaVO.setProperty("TIPOENVIO", "E");
				emailFilaVO.setProperty("CODCON", BigDecimal.ZERO);
				emailFilaVO.setProperty("MAXTENTENVIO", BigDecimal.valueOf(3));
				emailFilaVO.setProperty("STATUS", "Pendente");
				emailFilaVO.setProperty("MIMETYPE", "text/html");

				dwfEntityFacade.createEntity(DynamicEntityNames.FILA_MSG, (EntityVO) emailFilaVO);
			}
		} catch (Exception e) {
			MGEModelException.throwMe(e);
		} finally {
			JapeSession.close(hnd);
		}
	}

	public void alterarSenha(ServiceContext ctx) throws MGEModelException {
		SessionHandle hnd = null;

		try {
			String atual = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "atual");
			String nova = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "nova");
			String id = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "ID");
			id = new String(Base64Impl.decode(id));

			if (atual.equals(nova)) {
				throw new Exception("A nova senha deve ser diferente da senha atual.");
			}
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

			Collection<PersistentLocalEntity> clientes = dwfEntityFacade.findByDynamicFinder(new FinderWrapper(DynamicEntityNames.PARCEIRO, "this.CGC_CPF = ?", new Object[] { id }));

			if (clientes.size() == 0) {
				throw new Exception("Cliente no localizado.\nEntre em contato com a Credpar.");
			} else {
				PersistentLocalEntity cliente = clientes.iterator().next();
				DynamicVO clienteVO = (DynamicVO) cliente.getValueObject();

				if (StringUtils.getEmptyAsNull(clienteVO.asString("INTERNOCREDPAR")) == null) {
					if (!getMd5(id).equals(atual)) {
						throw new Exception("Senha atual no confere.");
					}
				} else if (!clienteVO.asString("INTERNOCREDPAR").equals(atual)) {
					throw new Exception("Senha altual no confere.");
				}

				clienteVO.setProperty("INTERNOCREDPAR", nova);

				cliente.setValueObject((EntityVO) clienteVO);
			}

		} catch (Exception e) {
			throwExceptionRollingBack(e);
		} finally {
			JapeSession.close(hnd);
		}
	}

	public void esqueciMinhaSenhaCliente(ServiceContext ctx) throws MGEModelException {
		SessionHandle hnd = null;

		try {
			String cpf = XMLUtils.getRequiredContentChildAsString(ctx.getRequestBody(), "cpf");

			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

			Collection<PersistentLocalEntity> clientes = dwfEntityFacade.findByDynamicFinder(new FinderWrapper(DynamicEntityNames.PARCEIRO, "this.CGC_CPF = ?", new Object[] { cpf }));

			if (clientes.size() == 0) {
				throw new Exception("CPF no localizado, verifique se o cpf informado est correto e tente novamente.");
			} else {
				PersistentLocalEntity cliente = clientes.iterator().next();
				DynamicVO clienteVO = (DynamicVO) cliente.getValueObject();

				if (StringUtils.getEmptyAsNull(clienteVO.asString("EMAIL")) == null) {
					throw new Exception("E-mail no encontrato.\nEntre em contato com a Credpar.");
				}

				Random gerador = new Random();
				int novaSenha = gerador.nextInt(999999);

				clienteVO.setProperty("INTERNOCREDPAR", getMd5(String.valueOf(novaSenha)));

				cliente.setValueObject((EntityVO) clienteVO);

				StringBuffer mensagem = new StringBuffer();
				mensagem.append("Ol ").append(clienteVO.asString("NOMEPARC")).append(",");
				mensagem.append("<br><br>Foi solicitado uma alterao de senha para entrar no Aplicativo Credpar.");
				mensagem.append("<br>Sua nova senha : ").append(novaSenha);
				mensagem.append("<br><br>Caso voc no tenha solicitado esta alterao, entre em contato com a Credpar.");

				DynamicVO emailFilaVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.FILA_MSG);
				emailFilaVO.setProperty("ASSUNTO", "Alterao de senha Credpar");
				emailFilaVO.setProperty("MENSAGEM", new String(mensagem.toString().getBytes("ISO-8859-1"), "ISO-8859-1").toCharArray());
				emailFilaVO.setProperty("EMAIL", clienteVO.asString("EMAIL"));
				emailFilaVO.setProperty("CODUSUREMET", BigDecimal.ZERO);
				emailFilaVO.setProperty("TIPOENVIO", "E");
				emailFilaVO.setProperty("CODCON", BigDecimal.ZERO);
				emailFilaVO.setProperty("MAXTENTENVIO", BigDecimal.valueOf(3));
				emailFilaVO.setProperty("STATUS", "Pendente");
				emailFilaVO.setProperty("MIMETYPE", "text/html");

				dwfEntityFacade.createEntity(DynamicEntityNames.FILA_MSG, (EntityVO) emailFilaVO);
			}

		} catch (Exception e) {
			throwExceptionRollingBack(e);
		} finally {
			JapeSession.close(hnd);
		}
	}

	private String getMd5(String value) throws Exception {
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(value.getBytes("UTF-8"), 0, value.length());

		String newValue = new BigInteger(1, m.digest()).toString(16);

		while (newValue.length() < 32) {
			newValue = "0" + newValue;
		}
		return newValue;
	}

	public static String toFirstCaseUp(String value) {

		if (value != null && value.length() > 0) {
			char[] stringArray = value.toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);
			value = new String(stringArray);
		}

		return value;
	}

	public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
		this.context = ctx;
	}
}
