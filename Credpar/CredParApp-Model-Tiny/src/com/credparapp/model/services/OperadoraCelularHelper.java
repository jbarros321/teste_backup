package com.credparapp.model.services;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jdom.Document;
import org.jdom.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sankhya.util.Base64Impl;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.JsonUtils;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import com.sankhya.util.XMLUtils;
import com.sankhya.util.TimeUtils.TipoPeriodo;
import com.sankhya.util.XMLUtils.SimpleXPath;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.core.JapeSession.TXBlock;
import br.com.sankhya.jape.dao.EntityDAO;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.KeyGenerateEvent;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.jape.wrapper.fluid.FluidVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.dwfdata.keygen.TgfNumKeyGen;
import br.com.sankhya.modelcore.util.ConfigUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ParameterUtils;
import br.com.sankhya.modelcore.util.ParceiroHellper;
import br.com.sankhya.ws.ServiceContext;

public class OperadoraCelularHelper {
	private static final String VERSAO_INTEGRACAO = "3.96";
	private static final String NOME_LOJA_PRIMARIA = "appcredpar";
	private static final String STATUS_DESATIVADO_PELA_SINCRONIZACAO = "X";

	private static final SimpleDateFormat	SDF_DATE_PATTERN	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private ConfigInfo config;
	private ServiceContext ctx;
	private DynamicVO voHist;
	private BigDecimal nuCompra;
	private JapeWrapper historicoRecargaDAO;
	private JapeWrapper topDAO;
	private JapeWrapper parceiroEmpDAO;
	private JapeWrapper parceiroDAO;
	private JapeWrapper financieroDAO;
	private JapeWrapper produtoDAO;
	private EntityFacade dwfFacade;
	private RecargaInfo recargaInfo;
	private boolean recargaConfirmada;
	private RetornoRecargaInfo retRecargaInfo;
	private BigDecimal codConta;
	private BigDecimal codTipoTitulo;
	private DynamicVO produtoVO;
	private BigDecimal codNatDespesa;
	private BigDecimal codTipOperDespesa;

	public OperadoraCelularHelper(ServiceContext ctx) throws Exception {
		this.ctx = ctx;
		Element configElem = ConfigUtils.getConfigAsXML(BigDecimal.ZERO, "conf.credpar.recarga");
		if(configElem != null){
			String conteudo = new String(Base64Impl.decode(XMLUtils.getContentAsString(configElem)));

			Gson gson = new GsonBuilder().create();
            config = gson.fromJson(conteudo, ConfigInfo.class);

            if(config == null) {
            	throw new Exception("Configurao para recarga no existe.");
            }

            if("P".equals(config.tipoAmbiente)) {

            	if(config.urlProd == null || config.userProd == null) {
            		throw new Exception("Ambiente configurado para produo, mas as configuraes esto incompletas.");
            	}
            } else {

            	if(config.urlHomolog == null || config.userHomolog == null) {
            		throw new Exception("Ambiente configurado para homologao, mas as configuraes esto incompletas.");
            	}
            }
		}

		codConta = (BigDecimal) ParameterUtils.getParameter("fin.autorizacao.credito.conta");
		if(codConta == null || codConta.equals(BigDecimal.ZERO)) {
			throw new IllegalArgumentException("Parametro de conta bancria no configurado. Deve ser configurado em: Operaes de Crdito > Configuraes");
		}

		codTipoTitulo = (BigDecimal) ParameterUtils.getParameter("fin.autorizacao.credito.tiptitulo");
		if(codTipoTitulo == null || codTipoTitulo.equals(BigDecimal.ZERO)) {
			throw new IllegalArgumentException("Parametro de tipo de ttulo no configurado. Deve ser configurado em: Operaes de Crdito > Configuraes");
		}

		codNatDespesa = (BigDecimal) ParameterUtils.getParameter("fin.autorizacao.credito.natureza.despesa");
		if(codNatDespesa == null || codNatDespesa.equals(BigDecimal.ZERO)) {
			throw new IllegalArgumentException("Parametro de natureza para despesa no configurado. Deve ser configurado em: Operaes de Crdito > Configuraes");
		}

		codTipOperDespesa = (BigDecimal) ParameterUtils.getParameter("fin.autorizacao.credito.topdespesa");
		if(codTipOperDespesa == null || codTipOperDespesa.equals(BigDecimal.ZERO)) {
			throw new IllegalArgumentException("Parametro de tipo de operao para despesa no configurado. Deve ser configurado em: Operaes de Crdito > Configuraes");
		}
		financieroDAO = JapeFactory.dao("Financeiro");
		historicoRecargaDAO = JapeFactory.dao("HistoricoRecargaCel");
		dwfFacade = EntityFacadeFactory.getDWFFacade();
	}

	@SuppressWarnings("rawtypes")
	public void consultarProdutos() throws Exception {
		if(config == null) {
			throw new Exception("A busca de produto no pode ser executada. No foi encontrato configurao.  necessrio configurar o ambiente.");
		}

		Collection<NameValuePair> postData = new ArrayList<NameValuePair>();
		adicionaCamposComuns(postData);
		postData.add(new NameValuePair("codigo_transacao", Transacao.LISTA_PRODUTOS.getCodigo()));

		final Document doc = executeRequest(postData);

		SessionHandle hnd = null;

		try {
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

			hnd.execWithTX(new JapeSession.TXBlock() {

				@Override
				public void doWithTx() throws Exception {
					Collection c = SimpleXPath.selectNodes(doc.getRootElement(), "

					JapeWrapper operadoraDAO = JapeFactory.dao("OperadoraCelular");
					JapeWrapper estadoOperadoraDAO = JapeFactory.dao("EstadoOperadora");
					JapeWrapper produtoDAO = JapeFactory.dao("ProdutoOperadora");
					JapeWrapper dddProdutoDAO = JapeFactory.dao("DddOperadora");

					for (Iterator iterator = c.iterator(); iterator.hasNext();) {
						Element opeElem = (Element) iterator.next();

						String codOperadora = getElementValue(opeElem, "codigoOperadora");
						String nomeOperadora = getElementValue(opeElem, "nomeOperadora");

						log("sincronizando operadora: " + codOperadora + " - " + nomeOperadora);

						DynamicVO opeVO = operadoraDAO.findOne("this.CODOPERADORA = ?", codOperadora);
						boolean novaOperadora = true;

						if(opeVO == null) {
							opeVO = operadoraDAO.create()
									.set("CODOPERADORA", getElementValue(opeElem, "codigoOperadora"))
									.set("NOMEOPERADORA", nomeOperadora)
									.set("TIPO", getElementValue(opeElem, "tipoOperadora"))
									.set("DHULTATU", getElementValueAsTimestamp(opeElem, "ultimaAtualizacaoOperadora"))
									.save();
						} else {
							novaOperadora = false;
						}
						BigDecimal nuOperadora = opeVO.asBigDecimal("NUOPERADORA");

						Collection<String> ufsList = new ArrayList<String>();
						Collection estadosElem = SimpleXPath.selectNodes(opeElem, "
						for (Iterator iterator2 = estadosElem.iterator(); iterator2.hasNext();) {
							Element estadoElem = (Element) iterator2.next();
							String uf = estadoElem.getValue();
							if(novaOperadora) {
								DynamicVO estOpeVO = estadoOperadoraDAO.create()
										.set("NUOPERADORA", nuOperadora)
										.set("UF", uf)
										.set("ATIVO", "S")
										.save();
							} else {

								DynamicVO estOpeVO = estadoOperadoraDAO.findByPK(nuOperadora, uf);
								if(estOpeVO == null) {
									estOpeVO = estadoOperadoraDAO.create()
											.set("NUOPERADORA", nuOperadora)
											.set("UF", uf)
											.set("ATIVO", "S")
											.save();
								} else {
									estadoOperadoraDAO.prepareToUpdate(estOpeVO).set("ATIVO", "S").update();
								}
								ufsList.add(uf);
							}
						}

						if(!novaOperadora) {
							if(ufsList.size() == 0) {
								log("operadora sem estado <estadosAtuantes>: " + nomeOperadora);
							} else {
								Collection<DynamicVO> estadosList = estadoOperadoraDAO.find("this.NUOPERADORA = ? and $IN{this.UF}IN$ not inCollection[1]", nuOperadora, ufsList);
								for (Iterator iterator2 = estadosList.iterator(); iterator2.hasNext();) {
									DynamicVO vo = (DynamicVO) iterator2.next();
									estadoOperadoraDAO.prepareToUpdate(vo).set("ATIVO", STATUS_DESATIVADO_PELA_SINCRONIZACAO).update();
								}
							}

						}

						Collection<String> prodRetornados = new ArrayList<String>();
						Collection<BigDecimal> dddProdRet = new ArrayList<BigDecimal>();

						Collection produtosElem = SimpleXPath.selectNodes(opeElem, "
						for (Iterator iterator2 = produtosElem.iterator(); iterator2.hasNext();) {
							Element prodElem = (Element) iterator2.next();

							String codProd = getElementValue(prodElem, "codigoProduto");

							DynamicVO prodVO = produtoDAO.findByPK(nuOperadora, codProd);

							if(prodVO == null) {
								prodVO = produtoDAO.create()
										.set("NUOPERADORA", nuOperadora)
										.set("CODPROD", codProd)
										.set("NOMEPRODUTO", getElementValue(prodElem, "nomeProduto"))
										.set("MODELORECARGA", getElementValue(prodElem, "modeloRecarga"))
										.set("VALIDADEPROD", getElementValueAsBigDecimal(prodElem, "validadeProduto"))
										.set("PRECOVARPROD", getElementValue(prodElem, "precoVariavelProduto"))
										.set("PRECOVENDAPROD", getElementValueAsBigDecimal(prodElem, "precovendaProduto"))
										.set("PRECOCOMPRAPROD", getElementValueAsBigDecimal(prodElem, "precocompraProduto"))
										.set("DHULTATU", getElementValueAsTimestamp(prodElem, "ultima_atualizacaoProduto"))
										.set("VLRMINPROD", getElementValueAsBigDecimal(prodElem, "valorMinimoProduto"))
										.set("VLRMAXPROD", getElementValueAsBigDecimal(prodElem, "valorMaximoProduto"))
										.set("VLRINCPROD", getElementValueAsBigDecimal(prodElem, "valorIncrementoProduto"))
										.set("ATIVO", "N")
										.save();
							}

							prodRetornados.add(codProd);

							Collection dddsProdElem = SimpleXPath.selectNodes(prodElem, "
							for (Iterator iterator3 = dddsProdElem.iterator(); iterator3.hasNext();) {
								Element dddElem = (Element) iterator3.next();
								BigDecimal ddd = new BigDecimal(dddElem.getValue());

								DynamicVO dddProdVO = dddProdutoDAO.findByPK(nuOperadora, codProd, ddd);

								if(dddProdVO == null) {
									dddProdVO = dddProdutoDAO.create()
											.set("NUOPERADORA", nuOperadora)
											.set("DDD", ddd)
											.set("CODPROD", codProd)
											.set("ATIVO", "S")
											.save();
								} else {
									dddProdutoDAO.prepareToUpdate(dddProdVO).set("ATIVO", "S").update();;
								}
								dddProdRet.add(ddd);
							}

							if(!novaOperadora) {
								if(dddProdRet.size() == 0) {
									log("operadora sem ddd: " + nomeOperadora);
								} else {
									Collection<DynamicVO> prodList = dddProdutoDAO.find("this.NUOPERADORA = ? and this.CODPROD = ? and $IN{this.DDD}IN$ not inCollection[2]", nuOperadora, codProd, dddProdRet);
									for (Iterator iterator3 = prodList.iterator(); iterator3.hasNext();) {
										DynamicVO vo = (DynamicVO) iterator3.next();
										dddProdutoDAO.prepareToUpdate(vo).set("ATIVO", STATUS_DESATIVADO_PELA_SINCRONIZACAO).update();
									}
								}
							}
						}

						if(!novaOperadora) {
							if(prodRetornados.size() == 0) {
								log("operadora sem produtos: " + nomeOperadora);
							} else {
								Collection<DynamicVO> prodList = produtoDAO.find("this.NUOPERADORA = ? and $IN{this.CODPROD}IN$ not inCollection[1]", nuOperadora, prodRetornados);
								for (Iterator iterator2 = prodList.iterator(); iterator2.hasNext();) {
									DynamicVO vo = (DynamicVO) iterator2.next();
									produtoDAO.prepareToUpdate(vo).set("ATIVO", STATUS_DESATIVADO_PELA_SINCRONIZACAO).update();
								}
							}
						}

					}

				}
			});
		}finally {
			JapeSession.close(hnd);
		}

	}

	private String getElementValue(Element opeElem, String elemName) {
		Element elem = SimpleXPath.selectSingleNode(opeElem, elemName);
		if(elem != null ) {
			return elem.getValue();
		}

		return null;
	}

	private Object getElementValueAsBigDecimal(Element opeElem, String elemName) {
		String strValue = (String) getElementValue(opeElem, elemName);
		if(strValue != null ) {
			BigDecimal b = null;
			try {
				b = new BigDecimal(strValue);
			} catch (NumberFormatException e) {
				IllegalArgumentException ie = new IllegalArgumentException("Valor recebido no elemento: " + elemName + " com valor invlido: " + strValue, e);
				throw ie;
			}
			return b;
		}

		return null;
	}

	private Timestamp getElementValueAsTimestamp(Element opeElem, String elemName) {
		String value = (String) getElementValue(opeElem, elemName);
		if(value != null ) {
			Timestamp ts = null;
			try {
				Date d = SDF_DATE_PATTERN.parse(value);
				ts = new Timestamp(d.getTime());
			} catch (ParseException e) {
				IllegalArgumentException ie = new IllegalArgumentException("Valor recebido no elemento: " + elemName + " com valor invlido: " + value, e);
				throw ie;
			}
			return ts;
		}

		return null;
	}

	private void adicionaCamposComuns(Collection<NameValuePair> postData) {

		postData.add(new NameValuePair("loja_primaria", "P".equals(config.getTipoAmbiente()) ? NOME_LOJA_PRIMARIA: "teste"));
		postData.add(new NameValuePair("nome_primario", "P".equals(config.getTipoAmbiente()) ? config.getUsuario(): config.getUserHomolog()));
		postData.add(new NameValuePair("senha_primaria", "P".equals(config.getTipoAmbiente()) ? config.getSenha(): config.getSenhaHomolog()));
		postData.add(new NameValuePair("versao", VERSAO_INTEGRACAO));

	}

	private Document executeRequest(Collection<NameValuePair> postData) throws Exception {

		HttpClient hc = new HttpClient();
		PostMethod post = new PostMethod(config.getUrl());

		post.setRequestBody(postData.toArray(new NameValuePair[0]));
		int responseCode = hc.executeMethod(post);

		InputStream in = post.getResponseBodyAsStream();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] buf = new byte[512];
		int lidos = 0;
		while((lidos = in.read(buf)) != -1) {
			bout.write(buf, 0, lidos);
		}
		in.close();

		String strXmlRet = new String(bout.toByteArray());
		System.out.println(strXmlRet);
		if(responseCode != 200) {
			log("Erro na requisicao para obter a lista de produtos. httpError: " + responseCode + " resposta do servidor: " + strXmlRet);
			throw new RuntimeException("Erro na requisio para obter a lista de produtos para recarga de celular: " + responseCode);
		}

		Document doc = null;
		try {
			doc = XMLUtils.buildDocumentFromString(strXmlRet);
		} catch (Exception e) {
			log("Erro no retorno da lista de produto. No foi retornado um xml vlido: " + strXmlRet);
			RuntimeException re = new RuntimeException("Erro no retorno da lista de produto. No foi retornado um xml vlido.", e);
			throw re;
		}

		return doc;
	}

	public Collection<DynamicVO> getOperadorasAtivasPorDdd(String ddd) throws Exception {
		SessionHandle hnd = null;

		try {
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			FinderWrapper finder = new FinderWrapper("OperadoraCelular", "nullValue(this.ATIVO, 'N') = 'S' AND EXISTS(SELECT 1 FROM TRCOPEDDD DDD WHERE DDD.NUOPERADORA = this.NUOPERADORA AND DDD.DDD = ? AND nullValue(DDD.ATIVO, 'N') = 'S')", new Object[]{ddd});
			finder.setOrderBy("this.NOMEOPERADORA");

			Collection<DynamicVO> operadoras = dwfEntityFacade.findByDynamicFinderAsVO(finder);

			return operadoras;
		}finally {
			JapeSession.close(hnd);
		}
	}

	public Collection<DynamicVO> getOperadorasAtivas() throws Exception {
		SessionHandle hnd = null;

		try {
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			FinderWrapper finder = new FinderWrapper("OperadoraCelular", "nullValue(this.ATIVO, 'N') = 'S'");
			finder.setOrderBy("this.NOMEOPERADORA");

			Collection<DynamicVO> operadoras = dwfEntityFacade.findByDynamicFinderAsVO(finder);

			return operadoras;
		}finally {
			JapeSession.close(hnd);
		}
	}

	public Collection<DynamicVO> getProdutos(BigDecimal nuOperadora) throws Exception {
		SessionHandle hnd = null;

		try {
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			FinderWrapper finder = new FinderWrapper("ProdutoOperadora", "this.NUOPERADORA = ? AND nullValue(this.ATIVO, 'N') = 'S'", new Object[] {nuOperadora});
			finder.setOrderBy("this.NOMEPRODUTO");

			Collection<DynamicVO> produtos = dwfEntityFacade.findByDynamicFinderAsVO(finder);

			return produtos;
		}finally {
			JapeSession.close(hnd);
		}
	}

	public static void log(String msg) {
		System.out.println("[CredParRecargaCel] " + msg);
	}

	enum Transacao {
		LISTA_PRODUTOS("1"), RECARGA_ONLINE("5"), CONSULTA_STATUS("6"), CONFIRMACAO("7");

		private String codigo;

		Transacao(String codigo) {
			this.codigo = codigo;
		}

		String getCodigo() {
			return this.codigo;
		}
	}

	@SuppressWarnings("unused")
	private static class ConfigInfo {
		String tipoAmbiente;
		String userProd;
		String senhaProd;
		String urlProd;
		String userHomolog;
		String senhaHomolog;
		String urlHomolog;
		ConfFin confFin;

		public String getTipoAmbiente() {
			return tipoAmbiente;
		}
		public void setTipoAmbiente(String tipoAmbiente) {
			this.tipoAmbiente = tipoAmbiente;
		}
		public String getUserProd() {
			return userProd;
		}

		public void setUserProd(String userProd) {
			this.userProd = userProd;
		}
		public String getSenhaProd() {
			return senhaProd;
		}
		public void setSenhaProd(String senhaProd) {
			this.senhaProd = senhaProd;
		}
		public String getUrlProd() {
			return urlProd;
		}
		public void setUrlProd(String urlProd) {
			this.urlProd = urlProd;
		}
		public String getUserHomolog() {
			return userHomolog;
		}
		public void setUserHomolog(String userHomolog) {
			this.userHomolog = userHomolog;
		}
		public String getSenhaHomolog() {
			return senhaHomolog;
		}
		public void setSenhaHomolog(String senhaHomolog) {
			this.senhaHomolog = senhaHomolog;
		}
		public String getUrlHomolog() {
			return urlHomolog;
		}
		public void setUrlHomolog(String urlHomolog) {
			this.urlHomolog = urlHomolog;
		}

		public String getUsuario() {
			return ("P".equals(tipoAmbiente)? userProd: userHomolog);
		}

		public String getSenha() {
			return ("P".equals(tipoAmbiente)? senhaProd: senhaHomolog);
		}

		public String getUrl() {
			return ("P".equals(tipoAmbiente)? urlProd: urlHomolog);
		}
		public ConfFin getConfFin() {
			return confFin;
		}
		public void setConfFin(ConfFin confFin) {
			this.confFin = confFin;
		}
	}

	private static class ConfFin {
		BigDecimal codTipOperRecarga;
		BigDecimal codNatRecarga;
		BigDecimal codTipOperTarifa;
		BigDecimal codNatTarifa;
		BigDecimal tarifa;
		BigDecimal codParc;
		BigDecimal valorLimitMensal;

		public BigDecimal getCodTipOperRecarga() {
			return codTipOperRecarga;
		}
		public void setCodTipOperRecarga(BigDecimal codTipOperRecarga) {
			this.codTipOperRecarga = codTipOperRecarga;
		}
		public BigDecimal getCodNatRecarga() {
			return codNatRecarga;
		}
		public void setCodNatRecarga(BigDecimal codNatRecarga) {
			this.codNatRecarga = codNatRecarga;
		}
		public BigDecimal getCodTipOperTarifa() {
			return codTipOperTarifa;
		}
		public void setCodTipOperTarifa(BigDecimal codTipOperTarifa) {
			this.codTipOperTarifa = codTipOperTarifa;
		}
		public BigDecimal getCodNatTarifa() {
			return codNatTarifa;
		}
		public void setCodNatTarifa(BigDecimal codNatTarifa) {
			this.codNatTarifa = codNatTarifa;
		}
		public BigDecimal getTarifa() {
			return tarifa;
		}
		public void setTarifa(BigDecimal tarifa) {
			this.tarifa = tarifa;
		}
		public BigDecimal getCodParc() {
			return codParc;
		}
		public void setCodParc(BigDecimal codParc) {
			this.codParc = codParc;
		}
		public BigDecimal getValorLimitMensal() {
			return valorLimitMensal;
		}
		public void setValorLimitMensal(BigDecimal valorLimitMensal) {
			this.valorLimitMensal = valorLimitMensal;
		}

	}

	public void fazerRecarga(JsonObject jo2) throws Exception {
		JsonObject jo = JsonUtils.getJsonElement(jo2, "recarga").getAsJsonObject();
		Gson gson = new Gson();

		recargaInfo = gson.fromJson(jo, RecargaInfo.class);

		parceiroDAO = JapeFactory.dao("Parceiro");

		if(!recargaInfo.ehB2B && !ehRecargaNoSistema()) {

			if(StringUtils.isEmpty(recargaInfo.id)) {
				throw new Exception("Erro interno. Identificador do cliente invlido.");
			}
			DynamicVO vo = parceiroDAO.findOne("this.CGC_CPF = ?", recargaInfo.id);
			if(vo == null) {
				throw new Exception("Cliente no localizado.\\nEntre em contato com a Credpar.");
			}
			recargaInfo.codParc = vo.asBigDecimal("CODPARC");
		}

		produtoDAO = JapeFactory.dao("ProdutoOperadora");

		produtoVO = produtoDAO.findByPK(recargaInfo.nuOperadora, recargaInfo.produto);

		validaSenha();
		validaLimiteCreditoCliente();
		validaLimiteMensal();

		incluiHistorico(recargaInfo, StatusRecarga.SOLICITADO);

		Document doc = executaRecargaIntegrador(recargaInfo);
		salvarRetHistorico(doc);

		if(retRecargaInfo.temErro) {
			String codErro = (StringUtils.isEmpty(retRecargaInfo.codErro)? "No retornado.": retRecargaInfo.codErro.trim());
			String msgErro = (StringUtils.isEmpty(retRecargaInfo.mensagemErro)? "Sem mensagem.": retRecargaInfo.mensagemErro.trim());

			String msg = String.format("No foi possvel efetuar a recarga. Erro retornado: cdigo: %s mensagem: %s ", codErro, msgErro);
			throw new RuntimeException(msg);
		}

		confirmaRecargaIntegrador();
	}

	private boolean ehRecargaNoSistema() {
		return !StringUtils.isEmpty(recargaInfo.prodSis) && "celular".equals(recargaInfo.prodSis);
	}

	private void validaSenha() throws Exception {

		if(ehRecargaNoSistema()) {
			return;
		}

		if(StringUtils.isEmpty(recargaInfo.senha)) {
			throw new Exception(" necessrio informar a senha para concluir a operao");
		}

		DynamicVO vo = parceiroDAO.findByPK(recargaInfo.codParc);

		if(!recargaInfo.senha.equals(vo.asString("INTERNOCREDPAR"))) {
			throw new Exception("Senha incorreta.");
		}
	}

	private void validaLimiteCreditoCliente() throws Exception {
		ParceiroHellper parceiroHelper = new ParceiroHellper();

		BigDecimal valorRecarga = getValorRecarga();
		BigDecimal valorLancamento = valorRecarga.add(config.confFin.tarifa);

		BigDecimal limiteExcedido = parceiroHelper.validarLimiteCreditoParceiro(recargaInfo.codParc.intValue(), 0, valorLancamento, true);

		if(limiteExcedido != null) {
			throw new Exception("Operao no autorizada. Entre em contato com a Central de Atendimento da sua regio.");
		}
	}

	private void validaLimiteMensal() throws Exception {
		SessionHandle hnd = null;
		JdbcWrapper jdbcWrapper = null;
		try {
			hnd = JapeSession.open();
			jdbcWrapper = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();

			Timestamp now = new Timestamp(System.currentTimeMillis());
			Timestamp dtInicioPeriodo = TimeUtils.getInicioPeriodo(now, TipoPeriodo.MES);
			Timestamp dtFinalPeriodo = TimeUtils.getFinalPeriodo(now, TipoPeriodo.MES);

			NativeSql sql = new NativeSql(jdbcWrapper);
			sql.appendSql("SELECT SUM(NVL(VLRRECARGA, 0)) FROM TRCRECCEL WHERE CODPARC = :codParc AND DHALTER BETWEEN :dini AND :dfin AND STATUS='C'");
			sql.setNamedParameter("codParc", recargaInfo.codParc);
			sql.setNamedParameter("dini", dtInicioPeriodo);
			sql.setNamedParameter("dfin", dtFinalPeriodo);

			ResultSet rs = sql.executeQuery();
			try {
				if(rs.next()) {
					BigDecimal totalMes = rs.getBigDecimal(1);
					if(totalMes != null) {
						BigDecimal valorLimiteMensal = config.confFin.getValorLimitMensal();

						BigDecimal valorRecarga = getValorRecarga();
						BigDecimal valorLancamento = valorRecarga.add(config.confFin.tarifa);

						if(valorLimiteMensal != null && totalMes.add(valorLancamento).compareTo(valorLimiteMensal) > 0) {
							throw new Exception("Limite de recarga excedido. Entre em contato com a Central de Atendimento da sua regio.");
						}
					}
				}
			} finally {
				if(rs != null) {
					rs.close();
				}
			}
		} finally {
			JapeSession.close(hnd);
			JdbcWrapper.closeSession(jdbcWrapper);
		}
	}

	private boolean ehProdutoVariavel() {
		BigDecimal vlrProdMin = produtoVO.asBigDecimalOrZero("VLRMINPROD");
		BigDecimal vlrProdMax = produtoVO.asBigDecimalOrZero("VLRMAXPROD");
		return vlrProdMin.compareTo(vlrProdMax) != 0;
	}

	private BigDecimal getValorRecarga() {
		if(ehProdutoVariavel()) {
			return recargaInfo.vlrRecarga;
		} else {
			return produtoVO.asBigDecimal("PRECOVENDAPROD");
		}
	}

	private void confirmaRecargaIntegrador() throws Exception {

		incluiHistorico(recargaInfo, StatusRecarga.SOLICITATO_CONFIRMACAO);
		Collection<NameValuePair> postData = new ArrayList<NameValuePair>();
		adicionaCamposComuns(postData);

		postData.add(new NameValuePair("codigo_transacao", Transacao.CONFIRMACAO.getCodigo()));
		postData.add(new NameValuePair("compra", nuCompra.toString()));
		postData.add(new NameValuePair("cod_retorno", "0"));

		Document doc = executeRequest(postData);
		try {
			incluirRetConfHistorico(doc, StatusRecarga.SOLICITATO_CONFIRMACAO);
		} catch (Exception e) {
		}

		String xml = XMLUtils.documentToString(doc);
		System.out.println("xmlRet=[" + xml + "]");

		Element cellCardElem = doc.getRootElement();
		String codRetorno = getElementValue(cellCardElem, ".

		try {
			int cod = Integer.parseInt(codRetorno);
			recargaConfirmada = cod == 0;
			retRecargaInfo.codRetConfirmacao = cod;
		} catch (NumberFormatException e) {
			throw new Exception("Erro no retorno da integradora. Foi retornado: " + codRetorno);
		}

		StatusRecarga status = StatusRecarga.CONFIRMADO;
		if(!recargaConfirmada) {
			status = StatusRecarga.NAO_CONFIRMADO;
		}

		incluirRetConfHistorico(doc, status);
		gerarFinanceiros();
	}

	private void gerarFinanceiros() throws Exception {
		topDAO = JapeFactory.dao("TipoOperacao");

		parceiroEmpDAO = JapeFactory.dao("Parceiro");

		BigDecimal valorRecarga = getValorRecarga();
		BigDecimal valorAPagar = produtoVO.asBigDecimal("PRECOCOMPRAPROD");

		Timestamp now = new Timestamp(System.currentTimeMillis());

		final FluidCreateVO tarifaFinVO = iniciaVoTarifa(config.confFin.getTarifa(), now);
		final FluidCreateVO recargaFinVO = iniciaVoRecarga(valorRecarga, now);
		final FluidCreateVO aPagarFinVO = iniciaVoOperadora(valorAPagar, now);

		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();

			JapeSessionContext.putProperty("usuario_logado", BigDecimal.ZERO);
			hnd.execWithTX(new TXBlock() {

				@Override
				public void doWithTx() throws Exception {
					tarifaFinVO.save();
					recargaFinVO.save();
					aPagarFinVO.save();
				}
			});
		} finally {
			JapeSession.close(hnd);
		}

	}

	private FluidCreateVO iniciaFinVo(Timestamp now) throws Exception {
		FluidCreateVO fluidFinVO = financieroDAO.create();
		fluidFinVO.set("CODPARC", recargaInfo.codParc);
		fluidFinVO.set("CODEMP", getCodEmp(recargaInfo.codParc));

		fluidFinVO.set("DTVENC", buildDtVenc());
		fluidFinVO.set("CODCTABCOINT", codConta);
		fluidFinVO.set("CODTIPTIT", codTipoTitulo);
		fluidFinVO.set("DHMOV", now);
		fluidFinVO.set("DTNEG", now);

		fluidFinVO.set("HISTORICO", "Recarga Celular");
		fluidFinVO.set("NUMNOTA", retRecargaInfo.codOnline);
		fluidFinVO.set("PROVISAO", "N");
		fluidFinVO.set("DESDOBRAMENTO", "1");
		fluidFinVO.set("SEQUENCIA", BigDecimal.ONE);
		fluidFinVO.set("CODUSU", BigDecimal.ZERO);
		fluidFinVO.set("DTALTER", now);

		return fluidFinVO;
	}

	private FluidCreateVO iniciaVoTarifa(BigDecimal valorTarifa, Timestamp now) throws Exception {
		FluidCreateVO fluidFinVO = iniciaFinVo(now);
		fluidFinVO.set("VLRDESDOB", valorTarifa);
		fluidFinVO.set("RECDESP", BigDecimalUtil.valueOf(1));
		fluidFinVO.set("CODNAT", config.getConfFin().getCodNatTarifa());

		DynamicVO topVo = buscaTOP(config.getConfFin().getCodTipOperTarifa());
		fluidFinVO.set("CODTIPOPER", config.getConfFin().getCodTipOperTarifa());
		fluidFinVO.set("DHTIPOPER", topVo.getProperty("DHALTER"));

		return fluidFinVO;
	}

	private FluidCreateVO iniciaVoRecarga(BigDecimal valorRecarga, Timestamp now) throws Exception {
		FluidCreateVO fluidFinVO = iniciaFinVo(now);
		fluidFinVO.set("VLRDESDOB", valorRecarga);
		fluidFinVO.set("RECDESP", BigDecimalUtil.valueOf(1));
		fluidFinVO.set("CODNAT", config.getConfFin().getCodNatRecarga());

		DynamicVO topVo = buscaTOP(config.getConfFin().getCodTipOperRecarga());
		fluidFinVO.set("CODTIPOPER", config.getConfFin().getCodTipOperRecarga());
		fluidFinVO.set("DHTIPOPER", topVo.getProperty("DHALTER"));

		return fluidFinVO;
	}

	private FluidCreateVO iniciaVoOperadora(BigDecimal valorPagarRecarga, Timestamp now) throws Exception {
		FluidCreateVO fluidFinVO = iniciaFinVo(now);
		fluidFinVO.set("VLRDESDOB", valorPagarRecarga);
		fluidFinVO.set("RECDESP", BigDecimalUtil.valueOf(-1));
		fluidFinVO.set("CODPARC", config.confFin.codParc);
		fluidFinVO.set("CODNAT", codNatDespesa);

		DynamicVO topVo = buscaTOP(codTipOperDespesa);
		fluidFinVO.set("CODTIPOPER", codTipOperDespesa);
		fluidFinVO.set("DHTIPOPER", topVo.getProperty("DHALTER"));
		return fluidFinVO;
	}

	private Timestamp buildDtVenc() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.DAY_OF_MONTH, 30);
		return new Timestamp(cal.getTimeInMillis());
	}

	private Document executaRecargaIntegrador(final RecargaInfo recargaInfo) throws Exception {
		Collection<NameValuePair> postData = new ArrayList<NameValuePair>();
		adicionaCamposComuns(postData);

		postData.add(new NameValuePair("codigo_transacao", Transacao.RECARGA_ONLINE.getCodigo()));
		postData.add(new NameValuePair("compra", nuCompra.toString()));
		postData.add(new NameValuePair("produto", recargaInfo.produto));
		postData.add(new NameValuePair("ddd", recargaInfo.ddd));
		postData.add(new NameValuePair("fone", recargaInfo.nroTelefone));
		if(ehProdutoVariavel()) {
			postData.add(new NameValuePair("valor", recargaInfo.vlrRecarga.toString()));
		}

		AuthenticationInfo authInfo = (AuthenticationInfo) ctx.getAutentication();
		String usuarioLocal = "online";
		if(authInfo != null) {
			usuarioLocal = "" + authInfo.getUserID() + "-" + authInfo.getName();
		}

		postData.add(new NameValuePair("usuario_local", usuarioLocal));

		Document doc = executeRequest(postData);
		Element retElem = doc.getRootElement();

		retRecargaInfo = new RetornoRecargaInfo();

		Element erroElem = SimpleXPath.selectSingleNode(retElem, ".
		if(erroElem != null) {
			retRecargaInfo.codErro = getElementValue(erroElem, "codigo");
			retRecargaInfo.mensagemErro = getElementValue(erroElem, "mensagem");
			retRecargaInfo.erroOperadora = getElementValue(erroElem, "erroOperadora");
			retRecargaInfo.temErro = true;
		} else {
			retRecargaInfo.codOnline = (BigDecimal) getElementValueAsBigDecimal(retElem, "cod_online");
			retRecargaInfo.mensagem = getElementValue(retElem, "mensagem");
			retRecargaInfo.nsu = (BigDecimal) getElementValueAsBigDecimal(retElem, "nsu");
			retRecargaInfo.temErro = false;
		}

		return doc;
	}

	private void incluiHistorico(final RecargaInfo recargaInfo, final StatusRecarga status) throws Exception {
		SessionHandle hnd = null;

		try {
			hnd = JapeSession.open();

			if(status == StatusRecarga.SOLICITADO) {
				hnd.execWithTX(new TXBlock() {

					@Override
					public void doWithTx() throws Exception {
						nuCompra = getProximoNumeroCompra();
					}
				});
			}

			hnd.execWithTX(new TXBlock() {
				@Override
				public void doWithTx() throws Exception {
					voHist = historicoRecargaDAO.create().
							set("STATUS", status.getStatus()).
							set("NUMCOMPRA", nuCompra).
							set("NUOPERADORA", recargaInfo.nuOperadora).
							set("CODPROD", recargaInfo.produto).
							set("CODPARC", recargaInfo.codParc).
							set("DDD", recargaInfo.ddd).
							set("NROCELULAR", recargaInfo.nroTelefone).
							set("VLRRECARGA", recargaInfo.vlrRecarga).save();

				}
			});
		} finally {
			JapeSession.close(hnd);
		}
	}

	private void salvarRetHistorico(Document doc) throws Exception {
		Element cellCardElem = doc.getRootElement();

		final String xml = XMLUtils.documentToString(doc);
		System.out.println("xmlret[" + xml +"]");

		final String msgRet = getElementValue(cellCardElem, ".

		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();
			hnd.execWithTX(new TXBlock() {

				@Override
				public void doWithTx() throws Exception {
					historicoRecargaDAO.prepareToUpdate(voHist)
						.set("XML", xml.toCharArray())
						.set("MENSAGEMRET", msgRet)
						.set("CODONLINE", retRecargaInfo.codOnline)
						.set("NSU", retRecargaInfo.nsu)
						.set("MENSAGEMERRO", retRecargaInfo.mensagemErro)
						.set("ERROOPERADORA", retRecargaInfo.erroOperadora)
						.update();
				}
			});
		} finally {
			JapeSession.close(hnd);
		}
	}

	private void incluirRetConfHistorico(Document doc, final StatusRecarga status) throws Exception {
		final String xml = XMLUtils.documentToString(doc);

		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();
			hnd.execWithTX(new TXBlock() {

				@Override
				public void doWithTx() throws Exception {
					historicoRecargaDAO.prepareToUpdate(voHist)
						.set("XML", xml.toCharArray())
						.set("STATUS", status.getStatus())
						.set("VLRRECARGA", getValorRecarga())
						.update();
				}
			});
		} finally {
			JapeSession.close(hnd);
		}
	}

	private BigDecimal getProximoNumeroCompra() throws Exception {
		JdbcWrapper jdbc = null;

		try {
			EntityDAO dao = dwfFacade.getDAOInstance("HistoricoRecargaCel");
			jdbc = dao.buildJdbcWrapper();

			TgfNumKeyGen keyGen = new TgfNumKeyGen("HistoricoRecargaCel", "TRCRECCEL", "NUMCOMPRA");
			keyGen.setUseTxLock(true);

			return (BigDecimal) keyGen.generateKey(new KeyGenerateEvent(dao, jdbc, null));
		} finally {
			if (jdbc != null) {
				jdbc.closeSession();
			}
		}
	}

	public boolean recargaConfirmada() {
		return this.recargaConfirmada;
	}

	public RetornoRecargaInfo getRetornoRecarga() {
		return retRecargaInfo;
	}

	private Timestamp getDataTop(BigDecimal codTipOper)  throws Exception {
		DynamicVO  vo = topDAO.findOne("this.CODTIPOPER = ? AND this.DHALTER = (SELECT MAX(TPO.DHALTER) FROM TGFTOP TPO WHERE TPO.CODTIPOPER = this.CODTIPOPER)", codTipOper);
		return vo.asTimestamp("DHALTER");
	}

	private BigDecimal getCodEmp(BigDecimal codParc) throws Exception {
		DynamicVO parceiroVO = parceiroEmpDAO.findByPK(codParc);
		BigDecimal codEmp = parceiroVO.asBigDecimal("CODEMP");
		if(codEmp == null || codEmp.intValue()  == 0) {
			codEmp = BigDecimal.ONE;
		}
		return codEmp;
	}

	public void setPropertyJapeSession() {
		JapeSessionContext.putProperty("credpar.ext.recarga", "true");
	}

	private DynamicVO buscaTOP(BigDecimal codTipOper) throws Exception {
		FinderWrapper finder = new FinderWrapper(DynamicEntityNames.TIPO_OPERACAO, "this.CODTIPOPER = ?", new Object[] { codTipOper });

		Collection tiposOperacoes = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(finder);

		DynamicVO result = null;
		Iterator ite = tiposOperacoes.iterator();

		if (ite.hasNext()) {
			result = (DynamicVO) ite.next();
		}

		return result;
	}

	enum StatusRecarga {
		SOLICITADO("S"), SOLICITATO_CONFIRMACAO("O"), CONFIRMADO("C"), NAO_CONFIRMADO("N");

		String status;
		private StatusRecarga(String status) {
			this.status = status;
		}

		String getStatus() {
			return status;
		}
	}

	private static class RecargaInfo {
		BigDecimal codParc;
		String ddd;
		String nroTelefone;
		BigDecimal nuOperadora;
		BigDecimal vlrRecarga;
		String produto;
		String senha;
		String prodSis;
		boolean ehB2B;
		String id;
	}

	static class RetornoRecargaInfo {
		public boolean temErro;
		BigDecimal codOnline;
		BigDecimal nsu;
		String mensagem;
		Timestamp dataIntegradora;

		String codErro;
		String mensagemErro;
		String erroOperadora;

		int codRetConfirmacao;
	}
}
