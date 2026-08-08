package br.com.sankhya.prevenda.simulacao;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.EasySQL;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.mgeserv.model.services.SimulacaoPrecoSPBean;
import br.com.sankhya.mgeserv.model.services.simulacao.GeradorPropostaPrevenda;
import br.com.sankhya.mgeserv.model.services.simulacao.RecordSet;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.ComercialUtils.PrecoTabela;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;
import com.sankhya.util.stream.NumberStreamList;

public class GeradorPropostaPrevendaImpl implements GeradorPropostaPrevenda {

	private RecordSet vars;
	private RecordSet produtos;
	private RecordSet naturezas;
	private EasySQL sql;
	private BigDecimal usuarioLogado;
	private Collection<String> mensagens;

	private static final int PADRAO = 1;
	private static final int ADICIONAL = 2;
	private static final int HORA = 6;
	private static final int FUNCIONAL = 7;
	private static final int ECOMMERCE = 8;
	private static final int NP = 9;

	private static final MathContext INT_CEILING_CTX = new MathContext(0, RoundingMode.CEILING);
	private static final MathContext DEC_DUASCASAS_CTX = new MathContext(2, RoundingMode.UP);

	private static final String USULITE = "USULITE";
	private static final String USUFULL = "USUFULL";
	private static final String AD_USUCLD = "AD_USUCLD";
	private static final String HRBASE = "HRBASE";
	private static final String HRIMP = "HRIMP";
	private static final String APROVADA = "APROVADA";
	private static final String CODPROD = "CODPROD";
	private static final String VLRTOT = "VLRTOT";
	private static final String QTDCERT = "QTDCERT";
	private static final String QTDHABI = "QTDHABI";
	private static final String QTDEAD = "QTDEAD";
	private static final String TPBANCO = "TPBANCO";
	private static final String PORTE = "PORTE";
	private static final String MATRIZES = "MATRIZES";
	private static final String FILIAIS = "FILIAIS";
	private static final String QTDHRNEG = "QTDHRNEG";
	private static final String USUARIO_FIT = "Usurios Fit";
	private static final String CORRETAMENTE = " corretamente no diagnstico!<br />";
	private static final BigDecimal CINCOPORCENTO = BigDecimal.valueOf(0.05);
	private static final BigDecimal DEZPORCENTO = BigDecimal.valueOf(0.10);

	boolean booPossuiCloud = false;
	private boolean booSnkPacks = false;
	private static final String SQL_PAI3_RESP = "AND NOT EXISTS (SELECT 1 FROM TPQDPD DPD, TPQRPE R2, TPQRES RE2 "
			+ "       WHERE R2.NUPESQ = POS.NUPESQ AND DPD.CODPERGDEP = RPE.CODPERG AND DPD.CODPERGMESTRE = R2.CODPERG "
			+ "       AND R2.CODPERG = RE2.CODPERG AND R2.CODRESP = RE2.CODRESP AND RE2.DESCRRESP IN ( 'No', 'Ocultar', '2 Fase')) "
			+ "AND NOT EXISTS (SELECT 1 FROM TPQDPD DPD, TPQRPE R2, TPQRES RE2, TPQDPD DPD3, TPQRPE R3, TPQRES RE3 "
			+ "                   WHERE R2.NUPESQ = POS.NUPESQ AND DPD.CODPERGDEP = RPE.CODPERG AND DPD.CODPERGMESTRE = R2.CODPERG "
			+ "				   AND R2.CODPERG = RE2.CODPERG AND R2.CODRESP = RE2.CODRESP AND RE2.DESCRRESP not IN ('No', 'Ocultar', '2 Fase') "
			+ "                    AND R3.NUPESQ = POS.NUPESQ AND DPD3.CODPERGDEP = R2.CODPERG AND DPD3.CODPERGMESTRE = R3.CODPERG "
			+ "                    AND R3.CODPERG = RE3.CODPERG AND R3.CODRESP = RE3.CODRESP AND RE3.DESCRRESP IN ('No', 'Ocultar', '2 Fase')) "
			+ "AND NOT EXISTS (SELECT 1 FROM TPQDPD DPD, TPQRPE R2,TPQRES RE2, TPQDPD DPD3, TPQRPE R3, TPQRES RE3,TPQDPD DPD4, TPQRPE R4, TPQRES RE4 "
			+ "                    WHERE R2.NUPESQ = POS.NUPESQ AND DPD.CODPERGDEP = RPE.CODPERG AND DPD.CODPERGMESTRE = R2.CODPERG "
			+ "                    AND R2.CODPERG = RE2.CODPERG AND R2.CODRESP = RE2.CODRESP AND RE2.DESCRRESP not IN ('No', 'Ocultar', '2 Fase') "
			+ "                    AND R3.NUPESQ = POS.NUPESQ AND DPD3.CODPERGDEP = R2.CODPERG AND DPD3.CODPERGMESTRE = R3.CODPERG "
			+ "                    AND R3.CODPERG = RE3.CODPERG AND R3.CODRESP = RE3.CODRESP AND RE3.DESCRRESP not IN ('No', 'Ocultar', '2 Fase') "
			+ "                    AND R4.NUPESQ = POS.NUPESQ AND DPD4.CODPERGDEP = R3.CODPERG AND DPD4.CODPERGMESTRE = R4.CODPERG "
			+ "					AND R4.CODPERG = RE4.CODPERG AND R4.CODRESP = RE4.CODRESP AND RE4.DESCRRESP   IN ('No', 'Ocultar', '2 Fase'))";

	private static final String SQL_PAI_RESP = " AND NOT EXISTS (SELECT 1 FROM TPQDPD DPD, TPQRPE R2, TPQRES RE2 "
			+ " WHERE R2.NUPESQ = POS.NUPESQ AND DPD.CODPERGDEP = RPE.CODPERG AND DPD.CODPERGMESTRE = R2.CODPERG "
			+ " AND R2.CODPERG = RE2.CODPERG AND R2.CODRESP = RE2.CODRESP AND RE2.DESCRRESP IN ( ";
	private static final String SQL_NAO_EXISTE_PAI_RESP_2 = SQL_PAI_RESP + " 'No', 'Ocultar', 'No respondido'))";
	private static final String SQL_NAO_EXISTE_PAI_RESP_1 = SQL_PAI_RESP + " 'Sim', 'Exibir'))";
	private static final String SQL_MAX_NUTAB = " AND E.NUTAB = (SELECT MAX(EM.NUTAB) FROM TGFEXC EM, TGFTAB TM WHERE TM.CODTAB = T.CODTAB AND TM.NUTAB = EM.NUTAB AND EM.CODPROD = E.CODPROD) ";
	private static final String SQL_LIGACAO_ON_RES_RPE = " RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP ";
	private static final String SQL_LIGACAO_RES_RPE = " AND " + SQL_LIGACAO_ON_RES_RPE;
	private static final String SQL_RPE_CODRESP = "RPE.CODRESP";
	private static final String SQL_RPE_TEXTO = "RPE.TEXTO";
	private static final String SQL_TPQRPE_TCSPOS = "TPQRPE RPE, TCSPOS POS";
	private static final String SQL_TPQRPE_TCSPOS_TPQRES = SQL_TPQRPE_TCSPOS + ", TPQRES RES ";
	private static final String SQL_TPQRPE_TCSPOS_TPQPER = SQL_TPQRPE_TCSPOS + ", TPQPER PER ";
	private static final String SQL_ANEXO_BASE = "(SELECT DISTINCT RES2.AD_PRODWEB AS  CODPROD, PER.DESCRPERG, ANE.DESCRICAO, ANE.HORAS "
			+ "FROM TCSPOS POS " + "INNER JOIN TPQANE ANE ON ANE.NUPESQ = POS.NUPESQ "
			+ "INNER JOIN TPQDPD DPD1 ON DPD1.CODPERGDEP = ANE.CODPERG "
			+ "INNER JOIN TPQRES RES1 ON RES1.CODPERG = DPD1.CODPERGMESTRE AND RES1.CODRESP = DPD1.CODRESPMESTRE "
			+ "INNER JOIN TPQDPD DPD2 ON DPD2.CODPERGDEP = RES1.CODPERG "
			+ "INNER JOIN TPQRES RES2 ON RES2.CODPERG = DPD2.CODPERGMESTRE  AND RES2.CODRESP = DPD2.CODRESPMESTRE AND RES2.AD_TEMPO > 0 "
			+ "INNER JOIN TPQPER PER  ON PER.CODPERG = RES2.CODPERG "
			+ "INNER JOIN TPQRPE RPE  ON RPE.CODPERG = RES2.CODPERG AND RES2.CODRESP = RPE.CODRESP AND RPE.NUPESQ = POS.NUPESQ "
			+ "WHERE POS.NUMOS = ";

	private boolean booInstJav = false;
	private boolean booFMC = false;
	private boolean possuiFuncional = false;
	private boolean possuiAdicional = false;
	private boolean possuiAdicionalOuHoraOuMensDBA;
	private boolean possuiHora = false;
	private boolean possuiMensDBA = false;
	private boolean booSnkExpress = false;
	private boolean booSnkExpressIND = false;
	private boolean booSnkExpressServ = false;
	private boolean booJivaBox = false;
	private boolean possuiSnkExpress;
	private boolean booLibAditivo = false;
	private boolean booTemProdWebService = false;
	private boolean booDiagUsuario = false;
	private boolean booMigracao;
	private boolean booFGV = false;
	private boolean booSnkCloud = false;
	private boolean booVendaBaseNP = false;
	private boolean booVendaSaas = false;
	private int qtdcld = 0;
	private int tipNeg = 0;
	private int intPorte = 0;
	private int intEDI = 0;
	private int intPEDWEB = 0;
	private int intDataSinc = 0;
	private int intTipImplantador = 0;
	private int qtdUsuFast = 0;
	private int qtdUsuCheckout = 0;
	private JdbcWrapper jdbc = null;

	private BigDecimal percTipImplantador = BigDecimal.ONE;
	private BigDecimal percIncidenciaLU = BigDecimal.ONE;
	private BigDecimal percIncidenciaImp = BigDecimal.ONE;
	private BigDecimal percIncidenciaMens = BigDecimal.ONE;
	private BigDecimal percIncidenciaGP = BigDecimal.ONE;
	private BigDecimal decDescMigra = BigDecimal.ZERO;
	private BigDecimal decSomaPrecos = BigDecimal.ZERO;
	private BigDecimal decVlrFMC = BigDecimal.ZERO;
	private BigDecimal decVlrMensFMC = BigDecimal.ZERO;
	private BigDecimal decVlrBD = BigDecimal.ZERO;
	private BigDecimal decVlrMensDBA = BigDecimal.ZERO;
	private BigDecimal decVlrOraEmb = BigDecimal.ZERO;
	private BigDecimal decVlrJAV = BigDecimal.ZERO;
	private BigDecimal decVlrEAD = BigDecimal.ZERO;
	private BigDecimal decVlrCNPJ = BigDecimal.ZERO;
	private BigDecimal decVlrImpBD = BigDecimal.ZERO;
	private BigDecimal decVlrInsJAV = BigDecimal.ZERO;
	private BigDecimal decPercDescUsuDU = BigDecimal.ZERO;
	private BigDecimal decPercDescUsuSer = BigDecimal.ZERO;
	private BigDecimal decQtdUsuTV = BigDecimal.ZERO;
	private BigDecimal decQtdUsuBIA = BigDecimal.ZERO;
	private BigDecimal decVlrHora = BigDecimal.ZERO;
	private BigDecimal decVlrLU = BigDecimal.ZERO;
	private BigDecimal decVlrMen = BigDecimal.ZERO;
	private BigDecimal decVlrImp = BigDecimal.ZERO;
	private BigDecimal decVlrFGV = BigDecimal.ZERO;
	private BigDecimal decVlrMensFGV = BigDecimal.ZERO;

	private BigDecimal decVlrSnkCloud = BigDecimal.ZERO;
	private BigDecimal decVlrMensSnkCloud = BigDecimal.ZERO;
	private BigDecimal decVlrBonusSnkCloud = BigDecimal.ZERO;
	private BigDecimal decVlrMinimoSnkCloud = BigDecimal.ZERO;
	private boolean booPermDescCloud = false;

	private BigDecimal vlrLuEspecial = BigDecimal.ZERO;
	private BigDecimal vlrMensalEspecial = BigDecimal.ZERO;

	private BigDecimal vlrMinMensalidade = BigDecimal.ZERO;
	private BigDecimal qtdHrBase = BigDecimal.ZERO;
	private BigDecimal qtdHrSemInc = BigDecimal.ZERO;
	private BigDecimal decQtdBD = BigDecimal.ONE;
	private BigDecimal qtdHrPers = BigDecimal.ZERO;

	private BigDecimal decAliqServ = BigDecimal.ZERO;
	private BigDecimal decAliqTrei = BigDecimal.ZERO;
	private BigDecimal decAliqDU = BigDecimal.ZERO;
	private BigDecimal porcentFluxoSaas = new BigDecimal("0.0").setScale(2, BigDecimal.ROUND_HALF_UP);
	private BigDecimal porcentagemDescontoFluxo = new BigDecimal("0.0").setScale(2, BigDecimal.ROUND_HALF_UP);
	private BigDecimal percacrdecSaas = BigDecimal.ZERO;

	private String strVWprodutos = null;
	private String strCodPap;

	private static final int PERGUNTA_MODELO_VENDA = 5506;
	private static final int COD_RESP_VENDA_SAAS = 3;

	private HashSet<Integer> possuiProd = new HashSet<>();
	private NativeSql nsql;
	private NativeSql sqlValidacaoModeloVendaNovoPortifolio;
	private NativeSql sqlValidacaoRespostasVendasNP;
	private NativeSql sqlValidacaoValorProutosJaContratados;
	private NativeSql sqlValidacaoModeloVendasSaas;
	private NativeSql sqlValidacaoPorcentagemFluxoSaas;
	private NativeSql sqlValidacaoPorcentagemDescontoFluxoSaas;
	private NativeSql sqlProdutosNovoPortifolio;
	private NativeSql sqlPorcentagensFluxo;
	private NativeSql sqlTipoVenda;

	private static final BigDecimal COD_FLUXO_PADRAO = new BigDecimal(30);

	@Override
	public void inicializaGerador(RecordSet vars, RecordSet produtos, RecordSet naturezas, EasySQL sql,
			BigDecimal usuario_logado, Collection<String> mensagens) {
		this.vars = vars;
		this.produtos = produtos;
		this.naturezas = naturezas;
		this.sql = sql;
		this.usuarioLogado = usuario_logado;
		this.mensagens = mensagens;
	}

	private BigDecimal zeroSeNull(BigDecimal varBig) {
		return varBig == null ? BigDecimal.ZERO : varBig;
	}

	private boolean menorZero(BigDecimal varBig) {
		return varBig.compareTo(BigDecimal.ZERO) < 0;
	}

	private boolean maiorZero(BigDecimal varBig) {
		return varBig != null && BigDecimal.ZERO.compareTo(varBig) < 0;
	}

	private boolean menorQue(BigDecimal varBig, int varInt) {
		return varBig.compareTo(BigDecimal.valueOf(varInt)) < 0;
	}

	private boolean menorIgual(BigDecimal varBig, int varInt) {
		return varBig.compareTo(BigDecimal.valueOf(varInt)) < 1;
	}

	private BigDecimal ceil(BigDecimal varBig) {
		return new BigDecimal(varBig.round(INT_CEILING_CTX).intValue()).setScale(0);
	}

	private String msgNaoInteiro(String msg) {
		return "Voc informou <b>" + msg + "</b> e este valor no  um nmero inteiro vlido!";
	}

	private String msgNecessarioInf(String msg, boolean quantidade) {
		return "Para aplicar o clculo  necessrio informar " + (quantidade ? "a quantidade de " : "") + "<b>" + msg
				+ "</b>" + CORRETAMENTE;
	}

	private String msgNecessarioInformar(String msg) {
		return msgNecessarioInf(msg, false);
	}

	private String msgNecessarioInformarQtd(String msg) {
		return msgNecessarioInf(msg, true);
	}

	private String msgInformeQtd(String msg) {
		return "Informe a quantidade de <b>" + msg + "</b>" + CORRETAMENTE;
	}

	private String sqlNumOS(BigDecimal numOS) {
		return " RPE.NUPESQ = POS.NUPESQ AND POS.NUMOS = " + numOS + " ";
	}

	private String sqlCodPerg(String codPerg) {
		return " AND RPE.CODPERG IN (" + codPerg + ") ";
	}

	private boolean perguntaRespondidaSim(String codPerg, BigDecimal numOS) throws Exception {
		sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS_TPQRES, sqlNumOS(numOS) + sqlCodPerg(codPerg)
				+ SQL_LIGACAO_RES_RPE + "    AND RES.DESCRRESP in ('Sim', 'Exibir') ");
		boolean result = sql.next();
		sql.reset();
		return result;
	}

	private boolean perguntaRespondidaSimSemPaiNao(String codPerg, BigDecimal numOS) throws Exception {
		sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS_TPQRES, sqlNumOS(numOS) + sqlCodPerg(codPerg)
				+ SQL_LIGACAO_RES_RPE + "    AND RES.DESCRRESP in ('Sim', 'Exibir') " + SQL_NAO_EXISTE_PAI_RESP_2);
		boolean result = sql.next();
		sql.reset();
		return result;
	}

	private void validarPergunta(String codPerg, BigDecimal numOS, String msg) throws Exception {
		sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg(codPerg));
		if (sql.next()) {
			if (sql.getInt(1) <= 1) {
				erro("O <b>" + msg
						+ "</b> selecionado no diagnstico no est homologado!<br />Entre em contato com a central para esclarecimentos!");
			}
		} else {
			erro(msgNecessarioInformar(msg));
		}
		sql.reset();
	}

	private String sqlNomeVARSIMPREC(String nomeVar) {
		return " AND PER.NOMEVARSIMPREC = '" + nomeVar + "' AND RPE.CODPERG = PER.CODPERG ";
	}

	private Integer getQtdCld(BigDecimal numOS) throws Exception {
		int qtd = 0;
		sql.select("nvl(SUM (QTDUSU),0)", "(SELECT PSC.NUMUSUARIOS AS QTDUSU,PSC.NUMSERIE FROM TCSOSE OSE"
				+ " INNER JOIN TCSPAP PAP ON PAP.CODPAP = OSE.CODPAP "
				+ "INNER JOIN TCSCON CON ON PAP.CODPARC = CON.CODPARC "
				+ "INNER JOIN TCSPSC PSC ON  CON.NUMCONTRATO = PSC.NUMCONTRATO "
				+ "INNER JOIN TGFPRO PRO ON PSC.CODPROD = PRO.CODPROD"
				+ "  WHERE NOT EXISTS (SELECT 1 FROM TCSPSC P1,TCSCON C2  WHERE CON.CODPARC = C2.CODPARC AND P1.NUMCONTRATO =  C2.NUMCONTRATO AND P1.CODPROD = 20510 AND P1.SITPROD <> 'C') "
				+ "  AND PSC.SITPROD IN ('A','B')" + " AND NVL(PRO.AD_NCLOUD,'N') <> 'S' " + "   AND OSE.NUMOS = "
				+ numOS + " " + "  GROUP BY PSC.NUMUSUARIOS,PSC.NUMSERIE) T");

		if (sql.next() && (sql.getInt(1) > 0)) {
			qtd = sql.getInt(1);
		}
		sql.reset();
		return Integer.valueOf(qtd);
	}

	private Integer tipoVenda(BigDecimal numOS) throws Exception {
		int tipo = -1;

		sql.nativeSelect("SELECT COUNT (*) AS tipoVenda " + " FROM TCSPOS POS "
				+ " INNER JOIN TPQRPE RPE ON POS.NUPESQ = RPE.NUPESQ "
				+ " INNER JOIN TPQPER PER ON PER.CODPERG = RPE.CODPERG"
				+ " INNER JOIN TPQRES RES ON RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP"
				+ " WHERE POS.NUMOS = " + numOS + " AND POS.APLICAVEL = 'S'" + " AND POS.NUMITEM = 0"
				+ " AND PER.CODPERG = 5506" + "  AND RPE.CODRESP= 2");

		if (sql.next() && (sql.getInt(1) >= 0)) {
			tipo = sql.getInt(1);
		}
		sql.reset();
		return Integer.valueOf(tipo);
	}

	private Integer vendaNFSE(BigDecimal numOS) throws Exception {
		int nfse = -1;

		sql.select("NVL(RPE.CODRESP,1) AS CODRESP ", SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("5297"));
		if (sql.next() && sql.getInt(1) == 1) {
			nfse = sql.getInt(1);
		}
		sql.reset();
		return Integer.valueOf(nfse);
	}

	private int naoCld = 0;
	private Object codVendedor;
	private BigDecimal decVlrGP;
	private int impMatriz;
	private String booPermDescCloud2;

	private void addUsuLiteTask1(BigDecimal numOS, boolean obrigatoria) throws Exception {
		naoCld = naoCld + addUsuLite(numOS, obrigatoria, "4688", "Usurios Sankhya Tasks");
	}

	private void addUsuLiteVision1(BigDecimal numOS, boolean obrigatoria) throws Exception {
		naoCld = naoCld + addUsuLite(numOS, obrigatoria, "4472", "Usurios Vision App");
	}

	private void addUsuLiteBIA1(BigDecimal numOS, boolean obrigatoria) throws Exception {
		naoCld = naoCld + addUsuLite(numOS, obrigatoria, "4834", "Usurios BIA");
	}

	private void addUsuLiteGPD1(BigDecimal numOS, boolean obrigatoria) throws Exception {
		naoCld = naoCld + addUsuLite(numOS, obrigatoria, "4833", "Usurios GPD");
	}

	private void addUsuLiteVisionApp1(BigDecimal numOS, boolean obrigatoria) throws Exception {
		naoCld = naoCld + addUsuLite(numOS, obrigatoria, "4538", "Usurios Jiva Vision App");
	}

	private void carregarVarsImpRec(String nomeVar, BigDecimal numOS, String msg, BigDecimal padrao,
			boolean treinamento, boolean obrigatoria) throws Exception {
		sql.select("RPE.TEXTO, PER.DESCRPERG", SQL_TPQRPE_TCSPOS_TPQPER,
				sqlNumOS(numOS) + sqlNomeVARSIMPREC(nomeVar) + (treinamento
						? " AND EXISTS (SELECT 1 FROM TPQRPE R, TPQPER P, TPQRES RES WHERE R.NUPESQ = RPE.NUPESQ AND R.CODPERG = P.CODPERG AND UPPER(P.DESCRPERG) = 'TREINAMENTO' AND R.CODPERG = RES.CODPERG AND R.CODRESP = RES.CODRESP AND RES.DESCRRESP in ('Sim', 'Exibir'))"
						: ""));
		if (sql.next() && !"".equals(sql.getString(1))) {
			try {
				vars.set(nomeVar, sql.getBigDecimal(1));
			} catch (Exception e) {
				erro("Informe <b>" + sql.getString(2) + "</b>" + CORRETAMENTE + msgNaoInteiro(sql.getString(1)));
			}
		} else if (possuiFuncional || possuiAdicionalOuHoraOuMensDBA
				|| ("SEGMENTOS".equals(nomeVar) && possuiSnkExpress) || booJivaBox) {
			vars.set(nomeVar, padrao);
		} else if (obrigatoria) {
			erro(msgNecessarioInformarQtd(msg));
		}
		sql.reset();
	}

	private void addUsuLitePedWeb(BigDecimal numOS, boolean obrigatoria) throws Exception {
		sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS_TPQPER, sqlNumOS(numOS) + sqlNomeVARSIMPREC("PEDWEB"));
		if (sql.next() && !"".equals(sql.getString(1))) {
			try {
				vars.set(USULITE, vars.asInt(USULITE) + sql.getInt(1));
			} catch (Exception e) {
				erro(msgInformeQtd(USUARIO_FIT) + msgNaoInteiro(sql.getString(1)));
			}
			if ((obrigatoria && sql.getInt(1) == 0) || sql.getInt(1) < 0) {
				erro(msgNecessarioInformarQtd(USUARIO_FIT));
			}
		} else if (obrigatoria) {
			erro(msgNecessarioInformarQtd(USUARIO_FIT));
		}
		sql.reset();
	}

	private void addUsuLiteVisionApp(BigDecimal numOS, boolean obrigatoria) throws Exception {
		addUsuLite(numOS, obrigatoria, "4538", "Usurios Jiva Vision App");
	}

	private void addUsuLiteControleServico(BigDecimal numOS, boolean obrigatoria) throws Exception {
		addUsuLite(numOS, obrigatoria, "4390", "Usurios Controle de Servios-OS");
	}

	private void addUsuLiteVision(BigDecimal numOS, boolean obrigatoria) throws Exception {
		addUsuLite(numOS, obrigatoria, "4472", "Usurios Vision App");
	}

	private void addUsuLiteBIA(BigDecimal numOS, boolean obrigatoria) throws Exception {
		decQtdUsuBIA = BigDecimal.valueOf(addUsuLite(numOS, obrigatoria, "4834", "Usurios BIA"));
	}

	private void addUsuLiteGPD(BigDecimal numOS, boolean obrigatoria) throws Exception {
		addUsuLite(numOS, obrigatoria, "4833", "Usurios GPD");
	}

	private void addUsuLiteTask(BigDecimal numOS, boolean obrigatoria) throws Exception {
		addUsuLite(numOS, obrigatoria, "4688", "Usurios Sankhya Tasks");
	}

	private void addUsuLiteTarefa(BigDecimal numOS, boolean obrigatoria) throws Exception {
		addUsuLite(numOS, obrigatoria, "5078", "Usurios Lista de Tarefas");
	}

	private int addUsuLite(BigDecimal numOS, boolean obrigatoria, String codPerg, String msg) throws Exception {
		return sqlIntToVars(numOS, obrigatoria, codPerg, msg, USULITE);
	}

	private int sqlIntToVars(BigDecimal numOS, boolean obrigatoria, String codPerg, String msg, String variavel)
			throws Exception {
		int result = 0;
		sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg(codPerg));
		if (sql.next() && !"".equals(sql.getString(1))) {
			try {
				result = sql.getInt(1);
				vars.set(variavel, vars.asInt(variavel) + sql.getInt(1));
			} catch (Exception e) {
				erro(msgInformeQtd(msg) + msgNaoInteiro(sql.getString(1)));
			}
			if ((obrigatoria && sql.getInt(1) == 0) || sql.getInt(1) < 0) {
				erro(msgNecessarioInformarQtd(msg));
			}
		} else if (obrigatoria) {
			erro(msgNecessarioInformarQtd(msg));
		}
		sql.reset();
		return result;
	}

	private int getEDISoma(BigDecimal numOS, String codPerg, String msg) throws Exception {
		int result = 0;
		String msgErro = "Informe a <b>Quantidade de layouts necessrios (EDI)</b> no processo <b>" + msg + "</b>"
				+ CORRETAMENTE;

		sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg(codPerg));
		if (!sql.next() || "".equals(sql.getString(1))) {
			erro(msgErro);
		} else {
			try {
				if (sql.getInt(1) <= 0) {
					erro(msgErro + msgNaoInteiro(sql.getString(1)));
				} else {
					result = sql.getInt(1);
				}
				intEDI++;
			} catch (Exception e) {
				erro(msgErro + msgNaoInteiro(sql.getString(1)));
			}
		}
		sql.reset();
		return result;
	}

	private int getQtdOraEmb(BigDecimal numOS) throws Exception {
		int result = 0;
		if (possuiAdicional) {
			sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("4690"));
			if (sql.next() && !"".equals(sql.getString(1))) {
				try {
					result = sql.getInt(1);
				} catch (Exception e) {
					erro(msgInformeQtd("Usurios Oracle Embarcado") + msgNaoInteiro(sql.getString(1)));
				}
			} else {
				erro(msgNecessarioInformarQtd("Usurios Oracle Embarcado"));
			}
			sql.reset();
		} else {
			result = vars.asInt(USUFULL) + vars.asInt(USULITE);
		}
		if (result < 10) {
			result = 10;
		}
		return result;
	}

	private void addHR(BigDecimal numOS, String codPerg1, String codPerg2) throws Exception {
		sql.select("cast(nvl(trim(RP2.TEXTO),'0') as number)", SQL_TPQRPE_TCSPOS_TPQRES + ", TPQRPE RP2", sqlNumOS(
				numOS) + sqlCodPerg(codPerg1) + " AND RP2.CODPERG = " + codPerg2 + SQL_LIGACAO_RES_RPE
				+ " AND RES.DESCRRESP in ('Sim', 'Exibir') AND RP2.NUPESQ = POS.NUPESQ AND RP2.TEXTO IS NOT NULL");
		if (sql.next() && (sql.getBigDecimal(1).compareTo(BigDecimal.ZERO) > 0)) {
			produtos.set(HRBASE, produtos.asBigDecimal(HRBASE).add(sql.getBigDecimal(1)));
			produtos.set(HRIMP, produtos.asBigDecimal(HRIMP).add(sql.getBigDecimal(1)));
			if ("1167".equals(codPerg1) || "4607".equals(codPerg1) || "4771".equals(codPerg1)) {
				qtdHrSemInc = qtdHrSemInc.add(sql.getBigDecimal(1));
			}
		}
		sql.reset();
	}

	private void incluirNat(int codNat, int gridMult, BigDecimal valor, BigDecimal vlrLiq, BigDecimal vlrBruto,
			BigDecimal vlrLoc) {
		naturezas.append();
		naturezas.set("CODNAT", codNat);
		naturezas.set("GRIDMULT", gridMult);
		naturezas.set("VALOR", valor);
		naturezas.set("VLRLIQ", vlrLiq);
		naturezas.set("VLRBRUTO", vlrBruto);
		naturezas.set("AD_VLRLOC", vlrLoc);
		naturezas.set("PERCACRDEC", 0);
		naturezas.set("VLRDESC", 0);
		naturezas.set("VLRACRES", 0);
		naturezas.set("VLRIMP", naturezas.asDouble("VALOR") - naturezas.asDouble("VLRLIQ"));
		if (!vlrBruto.equals(valor)) {
			if (maiorZero(valor) && maiorZero(vlrBruto)) {
				naturezas.set("PERCACRDEC", valor.divide(vlrBruto, 4, BigDecimal.ROUND_HALF_UP)
						.multiply(BigDecimal.valueOf(100), DEC_DUASCASAS_CTX).subtract(BigDecimal.valueOf(100)));
			} else {
				naturezas.set("PERCACRDEC", BigDecimal.valueOf(100));
			}
			if (vlrBruto.compareTo(valor) > 0) {
				naturezas.set("VLRDESC", vlrBruto.subtract(valor));
			} else if (valor != null) {
				naturezas.set("VLRACRES", valor.subtract(vlrBruto));
			}
		}

		if (codNat == 110204 && booVendaSaas) {
			percacrdecSaas = naturezas.asBigDecimal("PERCACRDEC");
		}

	}

	@Override
	public void executaSimulacao() throws Exception {
		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			calculos(jdbc);

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			br.com.sankhya.jape.dao.JdbcWrapper.closeSession(jdbc);
		}
	}

	private String getSelPerg(BigDecimal numOS) {
		return "( SELECT R.TEXTO FROM TCSPOS P, TPQRPE R WHERE P.NUMOS = " + numOS
				+ " AND P.NUPESQ = R.NUPESQ AND R.CODPERG = ";
	}

	private BigDecimal getQtdHorasTotal(BigDecimal numOS, int codProd, int qtd) throws Exception {
		String hql = "(" + "	SELECT Q1.CODPROD, TRUNC((SUM(Q1.TEMPO) * " + qtd
				+ ") / 100) AS TEMPO, SUM(Q1.QTDE) AS QTDE  " + "	FROM (  "
				+ "		SELECT RES.CODPROD, '1' QTDE , NVL(RES.AD_TEMPO, 0) AS TEMPO " + "		FROM TPQRPE RPE "
				+ "		INNER JOIN TPQRES RES ON   RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP "
				+ "		INNER JOIN TCSPOS POS ON (POS.NUPESQ = RPE.NUPESQ AND POS.NUMOS = " + numOS + " "
				+ "		AND POS.APLICAVEL = 'S' AND POS.NUMITEM = 0) " + "		WHERE ( RES.CODPROD IS NOT NULL ) "
				+ "	) Q1 GROUP BY Q1.CODPROD " + ") T ";
		sql.select("T.TEMPO", hql, "T.CODPROD = " + codProd);
		if (sql.next()) {
			return sql.getBigDecimal(1);
		} else {
			return BigDecimal.ZERO;
		}
	}

	private void calculos(JdbcWrapper jdbc) throws Exception {
		boolean booJivaW = false;
		boolean booSankhya = false;
		boolean booLojon = false;
		BigDecimal numOS = vars.asBigDecimal("NUMOS");
		int intQtdCNPJ = 0;

		String strBonifica = "N";
		BigDecimal codVendedor = BigDecimal.ZERO;
		BigDecimal decPercDescUsuDUJ = BigDecimal.ZERO;
		BigDecimal decPercDescUsuSerJ = BigDecimal.ZERO;
		BigDecimal decPercDescUsuOraEmb = BigDecimal.ZERO;

		BigDecimal decVlrHoraGP = BigDecimal.ZERO;
		BigDecimal decVlrHoraBaseGP = BigDecimal.ZERO;

		this.sqlValidacaoModeloVendaNovoPortifolio = new NativeSql(jdbc);

		this.sqlValidacaoModeloVendaNovoPortifolio.loadSql(this.getClass(),
				"ValidacaoNovoPortifolioHelper_queModeloVenda.sql");

		this.sqlValidacaoRespostasVendasNP = new NativeSql(jdbc);

		this.sqlValidacaoRespostasVendasNP.loadSql(this.getClass(),
				"ValidacaoNovoPortifolioHelper_queRespostasVendasNP.sql");

		sql.select("1", "TCSOSE", "CODTPN = 9 AND   NUMOS = " + numOS);
		if (sql.next()) {
			booVendaBaseNP = true;

			sqlValidacaoModeloVendaNovoPortifolio.setNamedParameter("NUMOS", numOS);
			sqlValidacaoModeloVendaNovoPortifolio.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);
			sqlValidacaoModeloVendaNovoPortifolio.setNamedParameter("CODPERG", PERGUNTA_MODELO_VENDA);

			ResultSet rset = sqlValidacaoModeloVendaNovoPortifolio.executeQuery();

			if (!rset.next()) {
				erro("Na Venda Base - NP o Modelo de Venda no pode ser diferente do Contrato. Favor alterar para prosseguir.");
			}

		} else {

			sqlValidacaoRespostasVendasNP.setNamedParameter("NUMOS", numOS);
			sqlValidacaoRespostasVendasNP.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);

			ResultSet result = sqlValidacaoRespostasVendasNP.executeQuery();

			StringBuilder textMsg = new StringBuilder("");

			while (result.next()) {

				String linha = result.getString("RESP");
				textMsg.append(linha + "\n");

			}

			if (textMsg.length() > 0) {
				erro(textMsg.toString());
			}

		}

		sql.select("1", "TCSOSE", "SITUACAO <> 'F' AND NUMOS = " + numOS);
		if (!sql.next()) {
			erro("No  permitido <b>Simular Preo</b> em negociaes concludas!");
		}
		sql.reset();

		sql.select(
				"NVL(VEN.PERCDESC, -1) as coluna01, NVL(VEN.PERCDESCSERV, -1) as col02, NVL(VEN.BONIFPROD, 'N') AS col03, "
						+ "VEN.CODVEND as col04, NVL(VEN.AD_PERCDESCJIVA,-1) as col05, NVL(VEN.AD_PERCDESCSERVJIVA, -1) as col06, "
						+ "NVL(VEN.AD_LIBADITIVO,'N') as col07, NVL(VEN.AD_LIBORAEMB,0) as col08",
				"TGFVEN VEN, TSIUSU USU", "VEN.CODVEND = USU.CODVEND AND USU.CODUSU = " + usuarioLogado);
		if (sql.next()) {
			decPercDescUsuDU = sql.getBigDecimal(1);
			decPercDescUsuSer = sql.getBigDecimal(2);
			strBonifica = sql.getString(3);
			codVendedor = sql.getBigDecimal(4);
			decPercDescUsuDUJ = sql.getBigDecimal(5);
			decPercDescUsuSerJ = sql.getBigDecimal(6);
			booLibAditivo = "SIM".equals(sql.getString(7));
			decPercDescUsuOraEmb = sql.getBigDecimal(8);
		} else {
			erro("Usurio/Vendedor no configurado para utilizar <b>Simulao de Preo</b> !<br />Entre em contato com a unidade.");
		}
		sql.reset();

		sql.select(
				"MIN(CASE WHEN CODFLD IN (10,15)   THEN 3 " + "         WHEN CODFLD IN (9,14) THEN 2 "
						+ "         WHEN CODFLD IN (5,6,7,8,11) THEN 1 " + "         ELSE CODFLD END) AS TIPO ",
				"TCSPOS", "NUMOS = " + numOS);
		if (sql.next()) {
			switch (sql.getInt(1)) {
			case 1:
				possuiFuncional = true;
				break;
			case 2:
				possuiAdicional = true;
				break;
			case 3:
				possuiHora = true;
				break;
			case 12:
				erro("Proposta do tipo ABAD est suspensa. Entre em contato com a Central!");
				break;
			case 13:
				possuiAdicional = true;
				booDiagUsuario = true;
				break;
			case 17:
				booSnkExpress = true;
				break;
			case 20:
				booSnkExpressIND = true;
				break;
			case 22:
				possuiMensDBA = true;
				break;
			case 23:
				booSnkExpressServ = true;
				break;
			case 25:
				booJivaBox = true;
				break;
			case 30:
				booSnkPacks = true;
				break;
			}
		}

		this.sqlValidacaoModeloVendasSaas = new NativeSql(jdbc);

		this.sqlValidacaoModeloVendasSaas.loadSql(this.getClass(), "ValidacaoVendaSaasHelper_queModeloVenda.sql");

		sqlValidacaoModeloVendasSaas.setNamedParameter("NUMOS", numOS);
		sqlValidacaoModeloVendasSaas.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);
		sqlValidacaoModeloVendasSaas.setNamedParameter("CODPERG", PERGUNTA_MODELO_VENDA);
		sqlValidacaoModeloVendasSaas.setNamedParameter("CODSAAS", COD_RESP_VENDA_SAAS);

		ResultSet rset = sqlValidacaoModeloVendasSaas.executeQuery();

		if (rset.next()) {
			booVendaSaas = true;

			this.sqlValidacaoPorcentagemFluxoSaas = new NativeSql(jdbc);

			this.sqlValidacaoPorcentagemFluxoSaas.loadSql(this.getClass(),
					"ValidacaoVendaSaasHelper_quePorcentagemMensalFluxo.sql");

			sqlValidacaoPorcentagemFluxoSaas.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);

			ResultSet rsetFluxo = sqlValidacaoPorcentagemFluxoSaas.executeQuery();

			BigDecimal porcentFluxo = new BigDecimal("0.0").setScale(2, BigDecimal.ROUND_HALF_UP);

			if (rsetFluxo.next()) {
				porcentFluxo = rsetFluxo.getBigDecimal("AD_PERCMENSAS");
			}

			porcentFluxoSaas = porcentFluxo.divide(new BigDecimal("100.00"));

		}

		sql.reset();
		possuiAdicionalOuHoraOuMensDBA = possuiAdicional || possuiHora || possuiMensDBA;
		possuiSnkExpress = booSnkExpress || booSnkExpressIND || booSnkExpressServ;

		sql.select("CODPAP", "TCSOSE", "NUMOS = " + numOS);
		if (sql.next()) {
			strCodPap = sql.getString(1);
		}
		sql.reset();

		sql.select("COUNT(*)", "TCSPSC PSC, TCSCON CON, TCSPAP PAP",
				"CON.CODPARC = PAP.CODPARC AND CON.NUMCONTRATO = PSC.NUMCONTRATO AND PSC.SITPROD = 'A' AND PAP.CODPAP = "
						+ strCodPap);
		booMigracao = sql.next() && sql.getInt(1) > 0;
		sql.reset();

		String strNegocio = null;
		sql.select("(CASE WHEN OSE.CODTPN = 1 AND FXO.CODFLD IN (1,4,12,17,20,23,25,29,30)  THEN 1 " +
				"      WHEN OSE.CODTPN = 2 AND FXO.CODFLD IN (9,13,14" + (booMigracao ? ",1" : "") + ")    THEN 1 " +

				"      WHEN OSE.CODTPN = 6 AND FXO.CODFLD IN (10,15,22)   THEN 1 " +
				"      WHEN OSE.CODTPN = 7 AND FXO.CODFLD IN (5,6,7,8,11) THEN 1 " +
				"      WHEN OSE.CODTPN = 8 AND FXO.CODFLD IN (16,19,26)   THEN 1 " +
				"      WHEN OSE.CODTPN = 9 AND FXO.CODFLD IN (30)   THEN 1" +
				"      ELSE 0 " + "END) AS TIPO, " + "UPPER(TPN.DESCRICAO) AS TIPO_VENDA, "
				+ "UPPER(FLD.DESCRICAO) AS DIAGNOSTICO, " + "FLD.SKJV ",
				"TCSOSE OSE INNER JOIN TCSFXO FXO ON OSE.NUMOS = FXO.NUMOS "
						+ "           INNER JOIN TCSTPN TPN ON TPN.CODTPN = OSE.CODTPN "
						+ "           INNER JOIN TCSFLD FLD ON FLD.CODFLD = FXO.CODFLD ",
				"OSE.NUMOS = " + numOS);
		if (sql.next()) {
			strNegocio = sql.getString(4);
			if (sql.getInt(1) == 0) {
				erro("Tipo de Venda: <b><i>'" + sql.getString(2) + "'</i></b> e Fluxo de Diagnstico: <b><i>'"
						+ sql.getString(3) + "'</i></b>, no permitido para a simulao!");
			}
		}
		sql.reset();

		sql.select("count(*)",
				"TCSOSE OSE INNER JOIN TCSPOS POS ON OSE.NUMOS = POS.NUMOS "
						+ "           INNER JOIN TPQRPE RPE ON POS.NUPESQ = RPE.NUPESQ "
						+ "           INNER JOIN TPQRES RES ON RPE.CODPERG = RES.CODPERG AND RPE.CODRESP = RES.CODRESP",
				"(RES.CODPROD = 20510 OR AD_PRODWEB = 20510) AND OSE.NUMOS=" + numOS);

		booSnkCloud = sql.next() && sql.getInt(1) > 0;
		sql.reset();

		if (booDiagUsuario) {
			sql.select("count(*)", "TCSPRN", "NUMOS = " + numOS);
			if (sql.next() && sql.getInt(1) < 1) {
				sql.reset();
				sql.select("DISTINCT PSC.CODPROD, PRO.LINHA",
						"TCSOSE OSE, TCSPSC PSC, TCSCON CON, TGFPRO PRO, TCSPAP PAP",
						"OSE.CODPAP = PAP.CODPAP AND CON.CODPARC = PAP.CODPARC AND PSC.NUMCONTRATO = CON.NUMCONTRATO AND PSC.SITPROD <> 'C' AND PSC.CODPROD = PRO.CODPROD AND NUMOS = "
								+ numOS);
				while (sql.next()) {
					produtos.append();
					produtos.set(CODPROD, sql.getInt(1));
					produtos.set("LINHA", sql.getString(2));
					produtos.set("BONIFICADO", "N");
					produtos.set("CODVEND", codVendedor);
					produtos.set(VLRTOT, BigDecimal.valueOf(0.0001));
					produtos.set("AD_CERTHAB", "S");
					produtos.set(HRBASE, BigDecimal.ZERO);
					produtos.set(HRIMP, BigDecimal.ZERO);
					produtos.set("QTDE", 1);
				}
			}
			sql.reset();
		}

		qtdcld = getQtdCld(numOS);

		sql.select(
				"NVL(OSE.CODTPN, -1), CUS.SKJV, NVL(CUS.VLRHORA, -1), NVL(CUS.VLRHORAGP, -1), NVL(CUS.VLRIMPBD, -1), "
						+ "NVL(1-CUS.ALIQSERV/100, 0), NVL(1-CUS.ALIQTREINA/100, 0), NVL(CUS.VLRKM, -1), NVL(CUS.TEMPOVISITA, 2), NVL(CUS.INTERVISITA, 90), "
						+ "NVL(CUS.CODCENCUS, 0), NVL(CUS.VLRINSTJAV, 0) AS VLR12, NVL(1-CUS.AD_ALIQLU/100, 1-0.1343) AS VLR13, NVL(AD_VLRCER, 120) as VLR14",
				"TCSOSE OSE, TSICUS CUS", "CUS.CODCENCUS = OSE.CODCENCUS AND OSE.NUMOS = " + numOS);
		if (sql.next()) {
			tipNeg = sql.getInt(1);
			if (tipNeg == ECOMMERCE) {
				booLojon = true;
			} else if ("J".equals(strNegocio)) {
				booJivaW = true;
				if (menorZero(decPercDescUsuDUJ)) {
					erro("<b>% Mx.Desc.em Negociaes Jiva<b> do Vendedor no configurado!<br />Entre em contato com a unidade.");
				}
				decPercDescUsuDU = decPercDescUsuDUJ;
				decPercDescUsuSer = decPercDescUsuSerJ;
			} else if ("S".equals(strNegocio)) {
				booSankhya = true;
				if (menorZero(decPercDescUsuDU) || menorZero(decPercDescUsuSer)) {
					erro("Usurio/Vendedor no configurado para utilizar <b>Simulao de Preo</b> !<br />Entre em contato com a unidade.");
				}
			}
			decVlrImpBD = sql.getBigDecimal(5);
			decAliqServ = sql.getBigDecimal(6);
			decAliqTrei = sql.getBigDecimal(7);
			decAliqDU = sql.getBigDecimal(13);

			BigDecimal decVlrHoraBase = sql.getBigDecimal(3);
			if (booLojon && decVlrHoraBase.compareTo(BigDecimal.valueOf(100)) < 0) {
				decVlrHoraBase = BigDecimal.valueOf(100);
			} else if (booJivaBox) {
				decVlrHoraBase = BigDecimal.valueOf(90).multiply(decAliqServ);
			}
			if (vars.isNull("VLRKM")) {
				vars.set("VLRKM", sql.getBigDecimal(8));
			}
			if (vars.isNull("TEMPOVISITA")) {
				vars.set("TEMPOVISITA", sql.getBigDecimal(9));
			}
			if (vars.isNull("INTERVISITA")) {
				vars.set("INTERVISITA", sql.getBigDecimal(10));
			}
			decVlrInsJAV = sql.getBigDecimal(12);

			if (vars.isNull("AD_VLRBRUTOHORA")) {
				if (possuiSnkExpress) {
					vars.set("VLRHORA", BigDecimal.valueOf(107.82));
				} else {
					vars.set("VLRHORA", decVlrHoraBase);
				}
			} else {
				vars.set("VLRHORA",
						BigDecimalUtil.getRounded(vars.asBigDecimal("AD_VLRBRUTOHORA").multiply(decAliqServ), 2));
			}
			decVlrHora = vars.asBigDecimal("VLRHORA");

			decVlrHoraBaseGP = BigDecimalUtil.getRounded(sql.getBigDecimal(4).multiply(decAliqServ), 2);
			if (vars.isNull("AD_VLRBRUTOHORAGP")) {
				vars.set("VLRHORAGP", sql.getBigDecimal(4));
			} else {
				vars.set("VLRHORAGP",
						BigDecimalUtil.getRounded(vars.asBigDecimal("AD_VLRBRUTOHORAGP").multiply(decAliqServ), 2));
			}
			decVlrHoraGP = vars.asBigDecimal("VLRHORAGP");

			if (menorZero(decVlrHora) || menorZero(decVlrHoraGP) || menorZero(decVlrImpBD) || menorZero(decAliqServ)
					|| menorZero(decAliqTrei)) {
				erro("Processo de <b>Simulao de Preos</b> no homologado para sua unidade!!<br />Unidade da Simulao: <b>"
						+ sql.getBigDecimal(11) + "</b>");
			}

			if (tipNeg == HORA) {
				decQtdBD = BigDecimal.ZERO;
			}
		} else {
			erro("No encontramos o CR para este Processo de <b>Simulao de Preos</b>.");
		}
		sql.reset();

		sql.select("DISTINCT 1", "TCSPOS P, TCSFLD F",
				"P.NUMOS = " + numOS + " AND P.CODFLD = F.CODFLD AND F.SKJV = '" + strNegocio + "'");
		if (!sql.next()) {
			erro("A negociao no possui diagnstico respondido, ou o diagnstico selecionado no corresponde ao <b>Centro de Resultado</b> selecionado na aba <b>Geral</b>!<br />Impossvel fazer <b>Simulao de Preos</b> sem antes responder o diagnstico!");
		}
		sql.reset();

		if (tipNeg == 4) {
			erro("Sistema no preparado para realizar Simulao de Preos para <b>Customizao</b>!");
		} else if (!(tipNeg == PADRAO || tipNeg == FUNCIONAL || tipNeg == HORA || tipNeg == ADICIONAL || tipNeg == NP
				|| booLojon)) {
			erro("Sistema no preparado para realizar Simulao de Preos para este Tipo de Negociao!");
		}

		sql.select("COUNT(DISTINCT CODFLD)", "TCSPOS", "APLICAVEL = 'S' AND NUMOS = " + numOS);
		if (sql.next() && sql.getInt(1) > 1) {
			erro("No  permitido indicar mais que um <b>diagnstico</b>!<br />Existem <b>" + sql.getString(1)
					+ "</b> diagnsticos selecionados!");
		}
		sql.reset();

		vars.set("AD_QTDPROVCER", 0);
		vars.set("AD_QTDPROVHAB", 0);
		vars.set(USUFULL, 0);
		vars.set(MATRIZES, 0);
		vars.set(FILIAIS, 0);
		vars.set(AD_USUCLD, 0);

		if (!possuiSnkExpress && !booLojon) {
			if (booJivaW) {
				sql.select("CODSERV", "TCSITE", "CODSERV IN (50403, 50404, 50414, 50415) AND NUMOS = " + numOS);
				if (!sql.next()) {
					erro("Para aplicar a simulao  necessrio chegar at a etapa <b>COM-NEGOCIAO DA PROPOSTA</b> ou <b>COM-APRES. SOLUO</b> ou <b>COM-LEVANTAMENTO / DIAGNSTICO</b> da negociao!");
				}
				sql.reset();

				if (!booJivaBox) {

					sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("4975"));
					if (sql.next()) {
						switch (sql.getInt(1)) {
						case 1:
							vars.set(USUFULL, 3);
							break;
						case 2:
							vars.set(USUFULL, 5);
							break;
						case 3:
							vars.set(USUFULL, 7);
							break;
						}
					}
				}
			}

			if (possuiAdicional && !booJivaBox) {
				sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("1628,1110"));
				if (sql.next() && sql.getInt(1) == 1) {
					sql.reset();
					if (booJivaW) {
						sql.select("COUNT(*)", SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("1628,1110")
								+ " AND RPE.CODRESP = 1 "
								+ " AND EXISTS (SELECT 1 FROM TPQDPD DPD, TPQRPE R2 WHERE R2.NUPESQ = POS.NUPESQ AND DPD.CODPERGDEP = R2.CODPERG AND DPD.CODPERGMESTRE = RPE.CODPERG AND R2.TEXTO IS NOT NULL AND TRIM(R2.TEXTO) <> '0')");
						if (sql.next() && sql.getInt(1) > 0) {
							sql.reset();
							sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS_TPQPER,
									sqlNumOS(numOS) + sqlNomeVARSIMPREC(USUFULL) + " AND RPE.CODPERG <> 4738 ");
							if (sql.next() && !"".equals(sql.getString(1))) {
								try {
									vars.set(USUFULL, sql.getInt(1));
								} catch (Exception e) {
									erro(msgInformeQtd("Usurios") + msgNaoInteiro(sql.getString(1)));
								}
							}
						} else {
							erro(msgNecessarioInformarQtd("Usurios"));
						}
					} else {
						sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS_TPQPER,
								sqlNumOS(numOS) + sqlNomeVARSIMPREC(USUFULL));
						if (sql.next() && !"".equals(sql.getString(1))) {
							try {
								vars.set(USUFULL, sql.getInt(1));
							} catch (Exception e) {
								erro(msgInformeQtd("Usurios") + msgNaoInteiro(sql.getString(1)));
							}
						} else {
							erro(msgNecessarioInformarQtd("Usurios"));
						}
					}

				}
			} else if (!possuiHora && !possuiMensDBA && !booJivaBox && !booSnkPacks) {
				sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("878,1110,2105"));
				if (sql.next() && !"".equals(sql.getString(1))) {
					try {
						vars.set(USUFULL, sql.getInt(1));
					} catch (Exception e) {
						erro(msgInformeQtd("Usurios") + msgNaoInteiro(sql.getString(1)));
					}
					if ((!booMigracao || !booJivaW) && sql.getInt(1) < 3) {
						erro("A quantidade de <b>Usurios</b> deve ser maior ou igual a tres.");
					}
				} else if (!possuiFuncional) {
					erro(msgNecessarioInformarQtd("Usurios"));
				}
				if ((!booMigracao || !booJivaW) && vars.asInt(USUFULL) < 1) {
					erro(" necessrio informar pelo menos 1 <b>Usurio Simultneo</b>!<br /><b>Atualize o Diagnstico!</b>");
				}

			}

			sql.reset();
			if (!booSnkPacks) {
				carregarVarsImpRec("SEGMENTOS", numOS, "Segmentos", BigDecimal.ONE, false, true);

				sql.select("RPE.TEXTO, RES.CODPROD", SQL_TPQRPE_TCSPOS_TPQRES,
						sqlNumOS(numOS) + sqlCodPerg("4570") + SQL_LIGACAO_RES_RPE);
				if (sql.next() && !"".equals(sql.getString(1))) {
					try {
						intQtdCNPJ = sql.getInt(1);
						decVlrCNPJ = decVlrCNPJ
								.add(getPrecoProduto(sql.getBigDecimal(2)).multiply(BigDecimal.valueOf(intQtdCNPJ)))
								.setScale(2, BigDecimal.ROUND_HALF_UP);
					} catch (Exception e) {
						erro("Informe a <b>Quantidade de CNPJ adicionais</b>" + CORRETAMENTE
								+ msgNaoInteiro(sql.getString(1)));
					}
				}
				sql.reset();

				sql.select("E.VLRVENDA, RES.CODPROD, RES.AD_JIVA",
						SQL_TPQRPE_TCSPOS + ", TPQRES RES, TGFEXC E, TGFTAB T",
						sqlNumOS(numOS) + sqlCodPerg("4696") + SQL_LIGACAO_RES_RPE
								+ " AND E.CODPROD = RES.CODPROD AND E.NUTAB = T.NUTAB" + SQL_MAX_NUTAB);
				if (sql.next()) {
					decVlrMensDBA = sql.getBigDecimal(1);
					decVlrLU = sql.getBigDecimal(3);

				}
				sql.reset();
			}
		}

		if (!possuiSnkExpress && !possuiMensDBA && !booJivaBox && !booSnkPacks) {
			sql.select("RPE.CODRESP - 1", SQL_TPQRPE_TCSPOS_TPQPER, sqlNumOS(numOS) + sqlNomeVARSIMPREC(PORTE));
			if (sql.next()) {
				if (tipNeg == ECOMMERCE) {
					intPorte = "J".equals(strNegocio) ? 1 : 2;
				} else {
					intPorte = sql.getInt(1);
				}
				if (booJivaW) {

					vars.set(PORTE, sql.getBigDecimal(1).add(BigDecimal.valueOf(10)).toString());
				} else if (booSankhya) {
					vars.set(PORTE, sql.getBigDecimal(1).toString());
				} else {
					vars.set(PORTE, sql.getBigDecimal(1).add(BigDecimal.valueOf(15)).toString());
				}
			} else if (possuiFuncional || possuiAdicional || possuiHora) {
				if (booJivaW) {

					vars.set(PORTE, "10");
				} else {
					vars.set(PORTE, "0");
				}
			} else if (tipNeg == ECOMMERCE) {
				intPorte = "J".equals(strNegocio) ? 1 : 2;
			} else {
				erro(msgNecessarioInformar("Faturamento da Empresa"));
			}
			sql.reset();
		}

		sql.select("DECODE(RPE.CODRESP, 1, 'OP', 2, 'SP', 3, 'OF', 4, 'SF', 5, 'ND','ND')", SQL_TPQRPE_TCSPOS_TPQPER,
				sqlNumOS(numOS) + sqlNomeVARSIMPREC(TPBANCO));
		if (sql.next()) {
			vars.set(TPBANCO, sql.getString(1));
			booFMC = "FMC".equals(vars.get(TPBANCO));
		} else if (possuiFuncional || possuiAdicionalOuHoraOuMensDBA || booLojon || booJivaBox) {
			vars.set(TPBANCO, "ND");
		} else {
			erro(msgNecessarioInformar("Serv. p/ Instalao de BD"));
		}
		sql.reset();

		sql.select("NVL(RPE.CODRESP,1) AS CODRESP, "
				+ "round(RES.AD_JIVA * (SELECT COTACAO FROM TSICOT WHERE CODMOEDA = 7 AND DTMOV = (SELECT MAX(DTMOV) FROM TSICOT CX WHERE CX.CODMOEDA = 7)) / 0.8875,2)",
				SQL_TPQRPE_TCSPOS_TPQRES, sqlNumOS(numOS) + sqlCodPerg("4687") + SQL_LIGACAO_RES_RPE);
		if (sql.next() && (sql.getInt(1) != 1)) {
			if (!"ND".equals(vars.get(TPBANCO)) && !"OP".equals(vars.get(TPBANCO))) {
				erro("Para <B>Oracle Embarcado<b>, <b>Serv. p/ Instalao de BD</b> deve ser 'Oracle Pago + Linux' ou 'Instalao ser feita pelo cliente'!");
			}
			decVlrOraEmb = sql.getBigDecimal(2);
		}
		sql.reset();

		boolean booFMCContrato = false;
		if (!booFMC && possuiAdicional) {
			sql.select("PAP.CODPARC", "TCSOSE OSE, TCSPAP PAP, TCSCON CON, TCSPSC PSC",
					"PAP.CODPAP = OSE.CODPAP AND CON.CODPARC = PAP.CODPARC AND PSC.NUMCONTRATO = CON.NUMCONTRATO AND PSC.CODPROD = 20469 AND OSE.NUMOS = "
							+ numOS);
			booFMCContrato = sql.next();
			sql.reset();
		}

		if (!booFMC && !booFMCContrato && !booJivaBox && !booSnkCloud) {
			sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("2310,5522"));
			if (sql.next()) {
				booInstJav = sql.getInt(1) == 1;
			} else if (booJivaW && (tipNeg == PADRAO)) {
				erro(msgNecessarioInformar("Serv. p/ Instalao do Servidor Java"));
			}
			sql.reset();
		}

		if (booSnkPacks) {
			validarPacks(numOS);
		} else {
			nsql = new br.com.sankhya.jape.sql.NativeSql(jdbc);
			nsql.setReuseStatements(true);
			if (booJivaW) {
				validarJiva(numOS);
			} else if (booSankhya) {
				validarSankhya(numOS);
			} else if (booLojon) {
				validarLojon(numOS);
			}
			NativeSql.releaseResources(nsql);
		}

		Map<BigDecimal, BigDecimal> precos = new HashMap<>();
		if (booJivaW) {
			NativeSql precoSql = new NativeSql(jdbc);
			precoSql.appendSql("SELECT RES.AD_PRODWEB, SUM(case when rpe.codperg = 880 then (case when " + tipNeg
					+ " = " + PADRAO
					+ " then cast(rpe.texto as int)-1 else cast(rpe.texto as int) end) else 1 end * RES.AD_JIVA) AS PRECO ");
			precoSql.appendSql("FROM " + SQL_TPQRPE_TCSPOS_TPQRES);
			precoSql.appendSql("WHERE " + sqlNumOS(numOS) + SQL_NAO_EXISTE_PAI_RESP_2 + SQL_LIGACAO_RES_RPE
					+ " AND RES.AD_PRODWEB NOT IN (20501,20500) "
					+ " AND RES.AD_JIVA IS NOT NULL AND POS.APLICAVEL = 'S' AND POS.NUMITEM = 0 "
					+ " AND NOT EXISTS (SELECT 1 FROM TPQDPD DPD, TPQRPE R2, TPQDPD DPD3, TPQRPE R3 "
					+ "                 WHERE R2.NUPESQ = POS.NUPESQ AND DPD.CODPERGDEP = RPE.CODPERG AND DPD.CODPERGMESTRE = R2.CODPERG AND R2.CODRESP <> 2 "
					+ " 				AND R3.NUPESQ = POS.NUPESQ AND DPD3.CODPERGDEP = R2.CODPERG AND DPD3.CODPERGMESTRE = R3.CODPERG AND R3.CODRESP = 2) "
					+ " AND NOT EXISTS (SELECT 1 FROM TPQDPD DPD, TPQRPE R2, TPQDPD DPD3, TPQRPE R3, TPQDPD DPD4, TPQRPE R4 "
					+ "                 WHERE R2.NUPESQ = POS.NUPESQ AND DPD.CODPERGDEP = RPE.CODPERG AND DPD.CODPERGMESTRE = R2.CODPERG AND R2.CODRESP <> 2 "
					+ " 				AND R3.NUPESQ = POS.NUPESQ AND DPD3.CODPERGDEP = R2.CODPERG AND DPD3.CODPERGMESTRE = R3.CODPERG AND R3.CODRESP <> 2 "
					+ " 				AND R4.NUPESQ = POS.NUPESQ AND DPD4.CODPERGDEP = R3.CODPERG AND DPD4.CODPERGMESTRE = R4.CODPERG AND R4.CODRESP = 2) "
					+ " AND (RPE.CODPERG <> 4910 OR NOT EXISTS(SELECT 1 FROM TPQRPE R, TCSPOS P WHERE R.NUPESQ = P.NUPESQ AND P.NUMOS = "
					+ numOS + " AND R.CODPERG = 1121 AND R.CODRESP = 1))");
			precoSql.appendSql("GROUP BY RES.AD_PRODWEB ");
			ResultSet sqlPreco = precoSql.executeQuery();

			while (sqlPreco.next()) {
				precos.put(sqlPreco.getBigDecimal(1), sqlPreco.getBigDecimal(2));
				decSomaPrecos = decSomaPrecos.add(sqlPreco.getBigDecimal(2));

			}

			NativeSql.releaseResources(precoSql);
		}

		NativeSql nsql2 = new NativeSql(jdbc);
		nsql2.appendSql("SELECT T.CODPROD, T.TEMPO, ");
		if (possuiSnkExpress) {
			nsql2.appendSql(" 'WEB' AS LINHA,");
		} else {
			nsql2.appendSql(" PRO.LINHA,");
		}
		nsql2.appendSql(" PRO.DESCRPROD, T.QTDE, PRO.CODNAT ");
		if (booSnkPacks) {
			nsql2.appendSql(",T.PRECO, T.VLRMENSALESPECIAL ");
		}
		nsql2.appendSql(" FROM ");
		nsql2.appendSql(strVWprodutos);
		nsql2.appendSql(" , TGFPRO PRO");
		nsql2.appendSql(" WHERE PRO.CODPROD = T.CODPROD");
		if (!possuiSnkExpress && !booLojon) {
			nsql2.appendSql(" AND (T.TEMPO > 0 OR 'true' = '" + booJivaBox + "' OR 'true' = '" + booSnkPacks + "')");
		}
		nsql2.appendSql(" ORDER BY PRO.DESCRPROD");

		ResultSet sqlMontaProdutos = nsql2.executeQuery();
		while (sqlMontaProdutos.next()) {

			boolean achei = false;
			int codProd = sqlMontaProdutos.getInt(1);
			produtos.goToStart();
			while (produtos.next() && !achei) {
				if (codProd == produtos.asInt(CODPROD)) {
					achei = true;
					if ("S".equals(produtos.get("BONIFICADO"))) {
						if ("N".equals(strBonifica)) {
							erro("Voc no tem permisso para bonificar produtos!<br /><b>"
									+ sqlMontaProdutos.getString(4) + "</b>");
						} else {
							produtos.set(VLRTOT, BigDecimal.ZERO);
						}
					} else if (booDiagUsuario) {
						produtos.set(VLRTOT, BigDecimal.ZERO);
					} else if (booSnkPacks) {
						produtos.set(VLRTOT, sqlMontaProdutos.getBigDecimal("PRECO"));
						if (!sqlMontaProdutos.getBigDecimal("VLRMENSALESPECIAL").equals(BigDecimal.ZERO)) {
							vlrLuEspecial = vlrLuEspecial.add(sqlMontaProdutos.getBigDecimal("PRECO"));
							vlrMensalEspecial = vlrMensalEspecial
									.add(sqlMontaProdutos.getBigDecimal("VLRMENSALESPECIAL"));
						}
					} else {
						produtos.set(VLRTOT, zeroSeNull(getPrecoProduto(sqlMontaProdutos.getBigDecimal(1))));
					}

					break;
				}
			}
			if (!achei) {
				produtos.append();
				produtos.set(CODPROD, codProd);
				produtos.set("LINHA", sqlMontaProdutos.getString(3));
				produtos.set("BONIFICADO", "N");
				produtos.set("CODVEND", codVendedor);
				if (booSnkPacks) {
					produtos.set(VLRTOT, sqlMontaProdutos.getBigDecimal("PRECO"));
					if (!sqlMontaProdutos.getBigDecimal("VLRMENSALESPECIAL").equals(BigDecimal.ZERO)) {
						vlrLuEspecial = vlrLuEspecial.add(sqlMontaProdutos.getBigDecimal("PRECO"));
						vlrMensalEspecial = vlrMensalEspecial.add(sqlMontaProdutos.getBigDecimal("VLRMENSALESPECIAL"));
					}
				} else {
					produtos.set(VLRTOT, zeroSeNull(getPrecoProduto(sqlMontaProdutos.getBigDecimal(1))));
				}
			}
			if (sqlMontaProdutos.getDouble(5) > 0) {
				produtos.set("QTDE", sqlMontaProdutos.getInt(5));
			}

			possuiProd.add(codProd);
			BigDecimal vlrTotalHrsImp;
			if (booSnkPacks) {
				vlrTotalHrsImp = sqlMontaProdutos.getBigDecimal(2);
			} else {
				switch (codProd) {
				case 10003:
				case 20410:
					if (booJivaBox) {
						vlrTotalHrsImp = sqlMontaProdutos.getBigDecimal(5).multiply(BigDecimal.valueOf(5))
								.add(BigDecimal.valueOf(3));
					} else {
						vlrTotalHrsImp = sqlMontaProdutos.getBigDecimal(2).add(sqlMontaProdutos.getBigDecimal(5)
								.subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(5)));
					}
					break;
				case 30748:
					vlrTotalHrsImp = getQtdHorasTotal(numOS, 30748, qtdUsuCheckout);
					break;
				case 30633:
					vlrTotalHrsImp = sqlMontaProdutos.getBigDecimal(2).multiply(BigDecimal.valueOf(intEDI))
							.add(sqlMontaProdutos.getBigDecimal(5).subtract(BigDecimal.valueOf(intEDI))
									.multiply(BigDecimal.valueOf(6)));
					break;
				case 20441:
				case 20403:
					vlrTotalHrsImp = BigDecimalUtil.getRounded(
							sqlMontaProdutos.getBigDecimal(2).multiply(sqlMontaProdutos.getBigDecimal(5)), 2);
					break;
				case 20500:
				case 30360:
					sql.select("NVL(RPE.CODRESP,1) AS CODRESP ", SQL_TPQRPE_TCSPOS,
							sqlNumOS(numOS) + sqlCodPerg("4687"));
					if (sql.next() && sql.getInt(1) == 4) {
						produtos.set("QTDE", 2);
					} else {
						produtos.set("QTDE", 1);
					}
					sql.reset();
					if (booSankhya) {
						decVlrOraEmb = BigDecimalUtil.getRounded(
								zeroSeNull(produtos.asBigDecimal(VLRTOT)).multiply(produtos.asBigDecimal("QTDE")), 2);
					}
					produtos.set(VLRTOT, BigDecimal.ZERO);
					vlrTotalHrsImp = sqlMontaProdutos.getBigDecimal(2).multiply(sqlMontaProdutos.getBigDecimal(5));
					break;
				case 20501:
				case 30361:
					produtos.set("QTDE", getQtdOraEmb(numOS));
					if (booJivaW) {
						decVlrOraEmb = BigDecimalUtil.getRounded(decVlrOraEmb.multiply(produtos.asBigDecimal("QTDE")),
								2);
					} else {
						decVlrOraEmb = BigDecimalUtil.getRounded(
								zeroSeNull(produtos.asBigDecimal(VLRTOT)).multiply(produtos.asBigDecimal("QTDE")), 2);
					}
					produtos.set(VLRTOT, BigDecimal.ZERO);
					vlrTotalHrsImp = BigDecimalUtil.getRounded(
							sqlMontaProdutos.getBigDecimal(2).multiply(sqlMontaProdutos.getBigDecimal(5)), 2);
					break;
				case 30346:
				case 30358:
					vlrTotalHrsImp = BigDecimalUtil.getRounded(
							sqlMontaProdutos.getBigDecimal(2).multiply(sqlMontaProdutos.getBigDecimal(5)), 2);
					intDataSinc = sqlMontaProdutos.getInt(5);
					break;
				case 20433:
					if (booJivaBox) {
						vlrTotalHrsImp = sqlMontaProdutos.getBigDecimal(5).multiply(BigDecimal.valueOf(4))
								.add(BigDecimal.valueOf(2));
					} else {
						BigDecimal hrsAjustado = sqlMontaProdutos.getBigDecimal(2).subtract(BigDecimal.valueOf(2));
						vlrTotalHrsImp = BigDecimalUtil
								.getRounded(sqlMontaProdutos.getBigDecimal(5).multiply(hrsAjustado), 2)
								.add(BigDecimal.valueOf(2));
					}
					intDataSinc = sqlMontaProdutos.getInt(5);
					break;
				default:
					vlrTotalHrsImp = sqlMontaProdutos.getBigDecimal(2).multiply(sqlMontaProdutos.getBigDecimal(5));
					break;
				}
				if (booJivaW) {
					if (codProd == 20506) {
						BigDecimal valor = precos.get(sqlMontaProdutos.getBigDecimal(1))
								.multiply(sqlMontaProdutos.getBigDecimal(5).subtract(BigDecimal.ONE));
						decSomaPrecos = decSomaPrecos.add(valor);
					} else if (codProd != 20465 && codProd != 20500 && codProd != 20501) {
						produtos.set(VLRTOT, zeroSeNull(precos.get(sqlMontaProdutos.getBigDecimal(1))));
					}

				} else if (booSankhya) {
				} else {
					vlrTotalHrsImp = sqlMontaProdutos.getBigDecimal(2);
				}
				vlrTotalHrsImp = vlrTotalHrsImp.multiply(percTipImplantador);
			}
			if (codProd == 20500 || codProd == 20501 || codProd == 20510) {
				produtos.set(HRBASE, BigDecimal.ZERO);
				produtos.set(HRIMP, BigDecimal.ZERO);
			} else {
				produtos.set(HRBASE, sqlMontaProdutos.getBigDecimal(2));
				produtos.set(HRIMP, ceil(vlrTotalHrsImp));
			}

			if (sqlMontaProdutos.getDouble(5) <= 0 && (!booSnkPacks)) {
				erro("Informe a quantidade do produto <b>" + sqlMontaProdutos.getString(4) + "</b>" + CORRETAMENTE);
			}
		}
		NativeSql.releaseResources(nsql2);

		if (possuiProd.contains(30346) && possuiProd.contains(30358)) {
			erro("Voce marcou Sincronizador de Tabelas Web e Grfico, por favor, escolha somente um deles.");
		}
		produtos.goToStart();

		if (!booDiagUsuario) {
			while (produtos.next()) {
				if (!possuiProd.contains(produtos.asInt(CODPROD))) {
					produtos.removeRow();
					produtos.currentIndex--;
				}
			}
		}
		if (booJivaW) {

			sql.select("T.DESCRPERG, T.DESCRICAO", SQL_ANEXO_BASE + numOS + ") T", "T.HORAS IS NULL");
			if (sql.next()) {
				erro("O modelo personalizado <b>" + sql.getString(2) + "</b> contido na pergunta <b>" + sql.getString(1)
						+ "</b> est sem estimativa de Horas.<br />Favor levantar junto a rea de servios o tempo necessrio para a formatao e informe.");
			}
			sql.reset();
		} else if (booSankhya && !booDiagUsuario && !possuiProd.contains(30611)) {

			if (!possuiSnkExpress && (tipNeg == PADRAO || tipNeg == FUNCIONAL)) {
				produtos.append();
				produtos.set(CODPROD, 30611);
				produtos.set(HRBASE, BigDecimal.valueOf(8));
				produtos.set(HRIMP, BigDecimal.valueOf(8));
				produtos.set("LINHA", "WEB");
				produtos.set("BONIFICADO", "N");
				produtos.set("CODVEND", codVendedor);
				produtos.set("QTDE", 1);
				if (booSnkPacks) {
					produtos.set(VLRTOT, BigDecimal.valueOf(0.0001));
				} else {
					produtos.set(VLRTOT, zeroSeNull(getPrecoProduto(BigDecimal.valueOf(30611))));
				}
			}
		}

		produtos.goToStart();

		while (produtos.next()) {
			int codProd = produtos.asInt(CODPROD);

			if (!booSnkPacks) {

				if (produtos.asInt("QTDE") > 1) {
					if (codProd == 20410 ||
							codProd == 10003) {
						decVlrLU = decVlrLU.add(BigDecimal.valueOf((produtos.asInt("QTDE") - 1) * 500f));

					} else if (codProd == 20433 ||
							codProd == 20489 ||
							codProd == 30346) {
						decVlrLU = decVlrLU
								.add(BigDecimal.valueOf((produtos.asInt("QTDE") - 1) * produtos.asDouble(VLRTOT)));

					}
				}
			}
			if (!booDiagUsuario) {

				if (codProd == 20426 || codProd == 20457 || codProd == 30619 || codProd == 30706) {
					String msgErroNFSe = "A cidade do Prospect no est homologada para <b>NFS-e</b> (<b>Nota Fiscal Eletronica de Servios</b>)!<br />Retire este produto do diagnstico!";
					String sqlNFSe;
					if (booJivaW) {
						sqlNFSe = "(SELECT COUNT(1) AS TOTAL FROM " + SQL_TPQRPE_TCSPOS + " WHERE " + sqlNumOS(numOS)
								+ sqlCodPerg("887") + ") T";
						msgErroNFSe = "A cidade da <b>NFS-e</b> (<b>Nota Fiscal Eletronica de Servios</b>) no foi selecionada no Diagnstico!";
					} else {
						sqlNFSe = "(SELECT COUNT(1) AS TOTAL FROM TCSOSE OSE INNER JOIN TCSPAP PAP ON PAP.CODPAP = OSE.CODPAP"
								+ " INNER JOIN TSICID CID ON CID.CODCID = PAP.CODCID" + " WHERE OSE.NUMOS = " + numOS
								+ " AND CID.AD_NFSE = 'S') T";
					}

					sql.select("T.TOTAL", sqlNFSe, "1=1");
					if (sql.next()) {
						if (sql.getInt(1) == 0 && (!booSnkPacks)) {
							erro(msgErroNFSe);
						} else if (booJivaW) {
							produtos.set("QTDE", sql.getInt(1));
						}

					}
					sql.reset();
					if (vars.asInt(FILIAIS) > 0) {
						msg("A proposta possui <b>NFS-e</b> e o cliente possui filiais.<br />"
								+ "Verifique a disponibilidade da <b>NFS-e " + (booJivaW ? "Jiva" : "Sankhya")
								+ "</b> na cidade da filial!");
					}
				} else if (codProd == 10003) {
					validarPergunta("1203,5541", numOS, "Modelo da Impressora Fiscal");
				} else if (codProd == 30352) {
					validarPergunta("1204", numOS, "Modelo do Microterminal");
				} else if (codProd == 30336 || codProd == 80084) {
					validarPergunta("1205,5542", numOS, "Modelo do Coletor WMS");
				} else if (codProd == 30616) {
					addHR(numOS, "1160", "4799");
				} else if (codProd == 30615) {

					addHR(numOS, "1167", "4769");
				} else if (codProd == 30677) {

					addHR(numOS, "4607", "4608");

					booTemProdWebService = true;
				} else if (codProd == 30611) {

					addHR(numOS, "4771", "4772");

				}

				if (booJivaW) {
					sql.select("NVL(SUM(T.HORAS), 0)", SQL_ANEXO_BASE + numOS + ") T",
							"T.HORAS > 0 AND T.CODPROD = " + Integer.toString(codProd));
					if (sql.next()) {
						produtos.set(HRIMP, ceil(produtos.asBigDecimal(HRIMP).add(sql.getBigDecimal(1))));
					}
					if (codProd == 20459) {
						sql.reset();
						sql.select("NVL(SUM(T.HORAS), 0)", SQL_ANEXO_BASE + numOS + ") T",
								"T.HORAS > 0 AND T.CODPROD = 20437");
						if (sql.next()) {
							produtos.set(HRIMP, ceil(produtos.asBigDecimal(HRIMP).add(sql.getBigDecimal(1))));
						}
					}
					sql.reset();
				} else {

					decVlrLU = decVlrLU.add(zeroSeNull(produtos.asBigDecimal(VLRTOT)));

					if (booMigracao) {
						if (

						codProd == 30318 ||
								codProd == 30325 ||
								codProd == 30359 ||
								codProd == 30309) {
							sql.select("COUNT(*), MAX(NVL(PSC.NUMUSUARIOS,0))", "TCSPSC PSC, TCSCON CON, TCSPAP PAP",
									" PAP.CODPAP = " + strCodPap + " AND PAP.CODPARC = CON.CODPARC "
											+ " AND CON.NUMCONTRATO = PSC.NUMCONTRATO " + " AND PSC.SITPROD = 'A' "
											+ " AND PSC.CODPROD = " + codProd);
							if (sql.next() && sql.getInt(2) > 0) {

								decDescMigra = decDescMigra.add(zeroSeNull(produtos.asBigDecimal(VLRTOT)));
							}
						} else {
							sql.select("NVL(DES.PERCENTUAL/100,0)", "VGFDESCMIGRA DES, TCSOSE OSE",
									"DES.CODPAP = OSE.CODPAP AND DES.PROJAVA = " + produtos.asInt(CODPROD)
											+ " AND OSE.NUMOS = " + numOS);
							if (sql.next() && (sql.getDouble(1) > 0)) {
								decDescMigra = decDescMigra
										.add(sql.getBigDecimal(1).multiply(zeroSeNull(produtos.asBigDecimal(VLRTOT))));
								if (codProd != 30635 && codProd != 30425) {
									if (sql.getDouble(1) >= 1) {
										produtos.set(HRBASE, BigDecimal.ZERO);
										produtos.set(HRIMP, BigDecimal.ZERO);
									} else {
										produtos.set(HRBASE,
												ceil(produtos.asBigDecimal(HRIMP).multiply(BigDecimal.valueOf(0.60))));
										produtos.set(HRIMP, produtos.asBigDecimal(HRBASE));
									}
								}
							}
							sql.reset();
						}
					}
				}
				qtdHrBase = qtdHrBase.add(produtos.asBigDecimal(HRIMP));
			}
		}

		booPossuiCloud = possuiCloud(numOS, 20510);
		decVlrMensSnkCloud = BigDecimal.ZERO;
		if (possuiAdicional) {

			if (booSnkCloud && booPossuiCloud) {
				erro("Este parceiro j possui o produto CLOUD ativo em seu contrato");
			}
			booSnkCloud = booPossuiCloud || possuiProd.contains(20510);
		}

		if (booSnkCloud) {

			int qtdUsu = vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld;
			sql.select("T.BONUS,T.VLRMINIMO, T.PERMDESC",
					"(SELECT TPP.CODPROD,NVL(TPP.BONUS,0) AS BONUS,NVL(TPP.VLRMINIMO,600) AS VLRMINIMO,NVL(TPP.PERMDESC,'N')AS PERMDESC, NVL((SELECT MAX(QTD) + 1 FROM AD_TCSTPP WHERE CODPROD = TPP.CODPROD AND QTD < TPP.QTD), 1) AS MIN, TPP.QTD AS MAX, TPP.VLR FROM AD_TCSTPP TPP) T",
					"T.MIN <= " + qtdUsu + " AND T.MAX >= " + qtdUsu + " AND T.CODPROD =20510");
			if (sql.next()) {
				decVlrBonusSnkCloud = sql.getBigDecimal("BONUS");
				decVlrMinimoSnkCloud = sql.getBigDecimal("VLRMINIMO");
				booPermDescCloud2 = sql.getString("PERMDESC");
			}
			sql.reset();

			if (possuiProd.contains(20510)) {
				decVlrSnkCloud = this.getPrecoProduto(new BigDecimal(20510));

			}

			boolean temEcommerce = false;
			if (!booPossuiCloud) {
				sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS,
						sqlNumOS(numOS) + sqlCodPerg("5143,5543") + " AND RPE.CODRESP = 4 ");
				if (sql.next()) {
					temEcommerce = true;
				}
			}

			decVlrMensSnkCloud = getVlrMenPorUsu(20510, vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld,
					temEcommerce, true);

			if ((decVlrMensSnkCloud.doubleValue() > 0.00)
					&& (decVlrMensSnkCloud.doubleValue() < decVlrMinimoSnkCloud.doubleValue()) && (tipNeg == PADRAO)) {
				decVlrMensSnkCloud = decVlrMinimoSnkCloud;
			}

			vars.set(AD_USUCLD, vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld);
		}

		if (booDiagUsuario) {
			qtdHrBase = BigDecimal.ZERO;
			decVlrLU = BigDecimal.ZERO;
			decVlrMen = BigDecimal.ZERO;
		}

		if (possuiMensDBA) {
			qtdHrBase = BigDecimal.ZERO;
			decVlrMen = BigDecimal.ZERO;
		}

		if (possuiHora) {

			String sqlHora = "(SELECT SUM(T.TEXTO) AS TEXTO, T.DESCRPERG FROM ( "
					+ "SELECT TO_NUMBER(RPE.TEXTO) AS TEXTO, PER.DESCRPERG " + "FROM " + SQL_TPQRPE_TCSPOS_TPQPER
					+ "WHERE " + sqlNumOS(numOS) + sqlNomeVARSIMPREC("QTDHORA") + "  UNION ALL "
					+ "SELECT SUM(ANE.HORAS) AS TEXTO, 'Quantidade de Horas:' AS DESCRPERG "
					+ "FROM TPQANE ANE INNER JOIN TCSPOS POS ON POS.NUPESQ = ANE.NUPESQ " + "WHERE POS.NUMOS = " + numOS
					+ " ) T GROUP BY T.DESCRPERG) A";
			sql.select("A.TEXTO, A.DESCRPERG", sqlHora);
			if (sql.next() && !"".equals(sql.getString(1))) {
				try {
					qtdHrBase = sql.getBigDecimal(1);
				} catch (Exception e) {
					erro("Informe <b>" + sql.getString(2) + "</b>" + CORRETAMENTE + msgNaoInteiro(sql.getString(1)));
				}
			}
			sql.reset();
		}

		if (booSnkPacks) {
			calcularPacks(numOS);
		} else if (booJivaW) {
			calcularJiva(numOS);
			percIncidenciaGP = BigDecimal.valueOf(0.10);
		} else if (booSankhya) {
			calcularSankhya(numOS);
			percIncidenciaGP = BigDecimal.valueOf(0.15);
		} else if (booLojon) {
			calcularLojon(numOS);
		}

		BigDecimal decVlrGP = BigDecimalUtil.getRounded(qtdHrBase.multiply(percIncidenciaGP), 0);
		decVlrGP = BigDecimalUtil.getRounded(BigDecimal.valueOf(decVlrGP.doubleValue() * decVlrHoraGP.doubleValue()),
				2);
		BigDecimal decVlrGPPadrao = BigDecimalUtil.getRounded(qtdHrBase.multiply(percIncidenciaGP), 0);

		decVlrGPPadrao = BigDecimalUtil
				.getRounded(BigDecimal.valueOf(decVlrGPPadrao.doubleValue() * decVlrHoraBaseGP.doubleValue()), 2);

		decVlrGPPadrao = decVlrGPPadrao.divide(decAliqServ, BigDecimal.ROUND_HALF_UP);

		if (!possuiAdicionalOuHoraOuMensDBA && decVlrMen.doubleValue() < vlrMinMensalidade.doubleValue()
				&& (!booMigracao || !booJivaW)) {
			decVlrMen = BigDecimal.valueOf(vlrMinMensalidade.doubleValue());
		}

		decVlrEAD = BigDecimal.ZERO;
		if (possuiAdicional) {
			if (perguntaRespondidaSim("5206", numOS)) {
				decVlrEAD = getVlrContratoBase(numOS).setScale(2, RoundingMode.HALF_UP);
				decVlrMen = BigDecimalUtil.getRounded(decVlrMen.add(decVlrEAD.multiply(CINCOPORCENTO)), 2);
			}
		} else {
			if (tipNeg == PADRAO) {

				if (!booVendaSaas) {
					decVlrEAD = decVlrLU.multiply(DEZPORCENTO).setScale(2, RoundingMode.HALF_UP);
					decVlrMen = BigDecimalUtil.getRounded(decVlrMen.add(decVlrMen.multiply(DEZPORCENTO)), 2);
				}

				produtos.append();
				produtos.set(CODPROD, 51112);
				produtos.set("LINHA", "WEB");
				produtos.set("BONIFICADO", "N");
				produtos.set("CODVEND", codVendedor);
				produtos.set(VLRTOT, BigDecimal.valueOf(0.0001));
				produtos.set("AD_CERTHAB", "S");
				produtos.set(HRBASE, BigDecimal.ZERO);
				produtos.set(HRIMP, BigDecimal.ZERO);
				produtos.set("QTDE", 1);
			}
		}

		BigDecimal vlrLiqBD = decVlrBD;
		BigDecimal vlrLiqMensDBA = decVlrMensDBA;
		BigDecimal vlrLiqJAV = decVlrJAV;
		BigDecimal vlrLiqImp = decVlrImp;
		BigDecimal vlrLiqFMC = decVlrFMC;
		BigDecimal vlrLiqMensFMC = decVlrMensFMC;
		BigDecimal vlrLiqFGV = decVlrFGV;
		BigDecimal vlrLiqFGVMen = decVlrMensFGV;
		BigDecimal vlrLiqSnkCloud = decVlrSnkCloud;
		BigDecimal vlrLiqSnkCloudMen = decVlrMensSnkCloud;
		BigDecimal vlrLiqEAD = decVlrEAD;
		BigDecimal vlrLiqLU = decVlrLU;
		BigDecimal vlrLiqMen = decVlrMen;
		BigDecimal vlrLiqCNPJ = decVlrCNPJ;

		MathContext mctx = new MathContext(64, RoundingMode.HALF_UP);

		if (booVendaBaseNP) {

			this.sqlValidacaoValorProutosJaContratados = new NativeSql(jdbc);
			this.sqlValidacaoValorProutosJaContratados.loadSql(this.getClass(),
					"ValidacaoNovoPortifolioHelper_queValorProdutosJaContratados.sql");
			this.sqlValidacaoValorProutosJaContratados.setNamedParameter("NUMOS", numOS);
			this.sqlValidacaoValorProutosJaContratados.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);

			ResultSet result = this.sqlValidacaoValorProutosJaContratados.executeQuery();

			BigDecimal totalJaContratado = new BigDecimal("0.0").setScale(2, RoundingMode.HALF_UP);
			totalJaContratado.setScale(2, RoundingMode.CEILING);

			int count = 0;
			while (result.next()) {
				BigDecimal valor = result.getBigDecimal("VALOR").setScale(2, RoundingMode.HALF_UP);
				totalJaContratado = totalJaContratado.add(valor);
				count += 1;

			}
			decVlrLU = decVlrLU.subtract(totalJaContratado, mctx).setScale(2, RoundingMode.HALF_UP);
			vlrLiqLU = decVlrLU;
		}

		decVlrBD = decVlrBD.divide(decAliqServ, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrMensDBA = decVlrMensDBA.divide(decAliqServ, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrJAV = decVlrJAV.divide(decAliqServ, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrGP = decVlrGP.divide(decAliqServ, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrImp = decVlrImp.divide(decAliqServ, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrFMC = decVlrFMC.divide(decAliqDU, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrMensFMC = decVlrMensFMC.divide(decAliqDU, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrFGV = decVlrFGV.divide(decAliqDU, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrMensFGV = decVlrMensFGV.divide(decAliqDU, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrSnkCloud = decVlrSnkCloud.divide(decAliqDU, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrMensSnkCloud = decVlrMensSnkCloud.divide(decAliqDU, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrEAD = decVlrEAD.divide(decAliqTrei, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrLU = decVlrLU.divide(decAliqDU, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrMen = decVlrMen.divide(decAliqDU, mctx).setScale(2, RoundingMode.HALF_UP);
		decVlrCNPJ = decVlrCNPJ.divide(decAliqDU, mctx).setScale(2, RoundingMode.HALF_UP);

		BigDecimal decVlrHoraImp = decVlrHora.divide(decAliqServ, mctx).setScale(2, RoundingMode.HALF_UP);

		BigDecimal vlrBrutoBD = decVlrBD;
		BigDecimal vlrBrutoMensDBA = decVlrMensDBA;
		BigDecimal vlrBrutoJAV = decVlrJAV;
		BigDecimal vlrBrutoImp = decVlrImp;
		BigDecimal vlrBrutoFMC = decVlrFMC;
		BigDecimal vlrBrutoMensFMC = decVlrMensFMC;
		BigDecimal vlrBrutoFGV = decVlrFGV;
		BigDecimal vlrBrutoFGVMen = decVlrMensFGV;
		BigDecimal vlrBrutoSnkCloud = decVlrSnkCloud;
		BigDecimal vlrBrutoSnkCloudMen = decVlrMensSnkCloud;
		BigDecimal vlrBrutoEAD = decVlrEAD;
		BigDecimal vlrBrutoLU = decVlrLU;
		BigDecimal vlrBrutoMen = decVlrMen;
		BigDecimal vlrBrutoCNPJ = decVlrCNPJ;

		if (!vars.isNull("VLRSINAL")) {
			if (vars.asBigDecimal("VLRSINAL").compareTo(decVlrLU) > 0) {
				erro("No  permitido colocar o <b>Valor do Sinal</b>, na sesso <b>Fator de Reduo do Sinal</b>, maior que o valor calculado para o sinal!");
			} else if (menorZero(vars.asBigDecimal("VLRSINAL"))) {
				erro("No  permitido colocar o <b>Valor do Sinal</b>, na sesso <b>Fator de Reduo do Sinal</b>, menor que zero!");
			}

			double fatorReducao = 1d - (vars.asDouble("VLRSINAL") / decVlrLU.doubleValue());
			decVlrLU = vars.asBigDecimal("VLRSINAL");
			decVlrMen = decVlrMen.multiply(BigDecimal.valueOf(fatorReducao)).add(decVlrMen);
		}

		BigDecimal decAcrDesLU = BigDecimal.ZERO;
		BigDecimal decAcrDesMen = BigDecimal.ZERO;
		BigDecimal decAcrDesImp = BigDecimal.ZERO;
		BigDecimal decAcrDesEad = BigDecimal.ZERO;
		BigDecimal decAcrDesFMC = BigDecimal.ZERO;
		BigDecimal decAcrDesCloud = BigDecimal.ZERO;
		BigDecimal decAcrDesFGV = BigDecimal.ZERO;
		BigDecimal decAcrDesCNPJ = BigDecimal.ZERO;
		BigDecimal decAcrDesMensFMC = BigDecimal.ZERO;
		BigDecimal decAcrDesMensFGV = BigDecimal.ZERO;
		BigDecimal decAcrDesMensSnkCloud = BigDecimal.ZERO;

		double acrdesMEN = vars.asDouble("ACRDESMEN");
		double acrdesIMP = vars.asDouble("ACRDESIMP");

		if (!vars.isNull("ACRDESNEG")) {
			decAcrDesLU = BigDecimal.valueOf(vars.asDouble("ACRDESNEG") - decVlrLU.doubleValue());
			decVlrLU = BigDecimal.valueOf(vars.asDouble("ACRDESNEG"));

			vlrLiqLU = BigDecimalUtil.getRounded(decVlrLU.multiply(decAliqDU), 2);
		} else if (decDescMigra.doubleValue() > 0) {
			decDescMigra = decDescMigra.divide(decAliqDU, BigDecimal.ROUND_HALF_UP);
			decVlrLU = decVlrLU.subtract(decDescMigra);
			vlrLiqLU = BigDecimalUtil.getRounded(decVlrLU.multiply(decAliqDU), 2);
		}

		if (!vars.isNull("ACRDESMEN")) {
			if (booJivaW
					&& ((100.0 - (acrdesMEN / decVlrMen.doubleValue() * 100.0)) > decPercDescUsuDU.doubleValue())) {
				erro("Desconto de mensalidade no pode ser " + (100.0 - (acrdesMEN / decVlrMen.doubleValue() * 100.0))
						+ "%.");
			}
			decAcrDesMen = BigDecimal.valueOf(acrdesMEN - decVlrMen.doubleValue());
			decVlrMen = BigDecimal.valueOf(acrdesMEN);
			vlrLiqMen = BigDecimalUtil.getRounded(decVlrMen.multiply(decAliqDU), 2);
			if (!booJivaW && decVlrMen.doubleValue() < vlrMinMensalidade.doubleValue() && !possuiAdicional
					&& !possuiHora) {
				erro("Mensalidade no pode ser menor que  " + vlrMinMensalidade.toString() + ".");
			}
		}
		if (!vars.isNull("ACRDESBD")) {
			decVlrBD = BigDecimal.valueOf(vars.asDouble("ACRDESBD"));
			vlrLiqBD = BigDecimalUtil.getRounded(decVlrBD.multiply(decAliqServ), 2);
		}
		if (!vars.isNull("AD_MENSDBA")) {
			decVlrMensDBA = BigDecimal.valueOf(vars.asDouble("AD_MENSDBA"));
			vlrLiqMensDBA = BigDecimalUtil.getRounded(decVlrMensDBA.multiply(decAliqTrei), 2);
		}
		if (!vars.isNull("AD_ACRDESJAV")) {
			decVlrJAV = BigDecimal.valueOf(vars.asDouble("AD_ACRDESJAV"));
			vlrLiqJAV = BigDecimalUtil.getRounded(decVlrJAV.multiply(decAliqServ), 2);
		}
		if (!vars.isNull("AD_ACRDESGERPROJ")) {
			decVlrGP = BigDecimal.valueOf(vars.asDouble("AD_ACRDESGERPROJ"));
		}
		if (!vars.isNull("ACRDESIMP")) {
			decAcrDesImp = BigDecimal.valueOf(acrdesIMP - decVlrImp.doubleValue());
			decVlrImp = BigDecimal.valueOf(acrdesIMP);
			vlrLiqImp = BigDecimalUtil.getRounded(decVlrImp.multiply(decAliqServ), 2);
			vars.set(QTDHRNEG, (int) (decVlrImp.doubleValue() / decVlrHoraImp.doubleValue()));
		}
		if (!vars.isNull("ACRDESEAD")) {
			decAcrDesEad = vars.asBigDecimal("ACRDESEAD").subtract(decVlrEAD);
			decVlrEAD = vars.asBigDecimal("ACRDESEAD");
			vlrLiqEAD = BigDecimalUtil.getRounded(decVlrEAD.multiply(decAliqTrei), 2);
		}
		if (!vars.isNull("AD_VLRFMC")) {
			decAcrDesFMC = vars.asBigDecimal("AD_VLRFMC").subtract(decVlrFMC);
			decVlrFMC = vars.asBigDecimal("AD_VLRFMC");
			vlrLiqFMC = BigDecimalUtil.getRounded(decVlrFMC.multiply(decAliqDU), 2);
		}

		if (!vars.isNull("AD_VLRCLOUD")) {
			decAcrDesCloud = vars.asBigDecimal("AD_VLRCLOUD").subtract(decVlrSnkCloud);
			decVlrSnkCloud = vars.asBigDecimal("AD_VLRCLOUD");
			vlrLiqSnkCloud = BigDecimalUtil.getRounded(decVlrSnkCloud.multiply(decAliqDU), 2);
		}

		if (!vars.isNull("AD_MENCLOUD")) {
			decAcrDesMensSnkCloud = vars.asBigDecimal("AD_MENCLOUD").subtract(decVlrMensSnkCloud);
			decVlrMensSnkCloud = vars.asBigDecimal("AD_MENCLOUD");
			vlrLiqSnkCloudMen = BigDecimalUtil.getRounded(decVlrMensSnkCloud.multiply(decAliqDU), 2);
		}

		if (decVlrBonusSnkCloud.doubleValue() > 0 && (tipNeg == PADRAO)) {

			decAcrDesMensSnkCloud = decVlrBonusSnkCloud;
			decVlrMensSnkCloud = decVlrMensSnkCloud.subtract(decVlrBonusSnkCloud);
			vlrLiqSnkCloudMen = BigDecimalUtil.getRounded(decVlrMensSnkCloud.multiply(decAliqDU), 2);
		}

		if (!vars.isNull("AD_MENSDC")) {
			decAcrDesMensFMC = vars.asBigDecimal("AD_MENSDC").subtract(decVlrMensFMC);
			decVlrMensFMC = vars.asBigDecimal("AD_MENSDC");
			vlrLiqMensFMC = BigDecimalUtil.getRounded(decVlrMensFMC.multiply(decAliqDU), 2);
		}

		if (!vars.isNull("AD_ACRDESCNPJ")) {
			decAcrDesCNPJ = vars.asBigDecimal("AD_ACRDESCNPJ").subtract(decVlrCNPJ);
			decVlrCNPJ = vars.asBigDecimal("AD_ACRDESCNPJ");
			vlrLiqCNPJ = BigDecimalUtil.getRounded(decVlrCNPJ.multiply(decAliqDU), 2);
		}
		if (!vars.isNull("AD_VLRFGV")) {
			decAcrDesFGV = vars.asBigDecimal("AD_VLRFGV").subtract(decVlrFGV);
			decVlrFGV = vars.asBigDecimal("AD_VLRFGV");
			vlrLiqFGV = BigDecimalUtil.getRounded(decVlrFGV.multiply(decAliqDU), 2);
		}
		if (!vars.isNull("AD_VLRMENFGV")) {
			decAcrDesMensFGV = vars.asBigDecimal("AD_VLRMENFGV").subtract(decVlrMensFGV);
			decVlrMensFGV = vars.asBigDecimal("AD_VLRMENFGV");
			vlrLiqFGVMen = BigDecimalUtil.getRounded(decVlrMensFGV.multiply(decAliqDU), 2);
		}

		if (!possuiSnkExpress && !possuiHora && !"S".equals(vars.get("USAGP")) && vars.asInt(QTDHRNEG) > 20) {
			vars.set("USAGP", "S");

		}

		if (booJivaBox) {
			boolean menor = (vlrBrutoLU.doubleValue() > decVlrLU.doubleValue())
					|| (vlrBrutoBD.doubleValue() > decVlrBD.doubleValue())
					|| (vlrBrutoMen.doubleValue() > decVlrMen.doubleValue())
					|| (vlrBrutoImp.doubleValue() > decVlrImp.doubleValue())
					|| (vlrBrutoFMC.doubleValue() > decVlrFMC.doubleValue())
					|| (vlrBrutoFGV.doubleValue() > decVlrFGV.doubleValue())
					|| (vlrBrutoSnkCloud.doubleValue() > decVlrSnkCloud.doubleValue())
					|| (vlrBrutoJAV.doubleValue() > decVlrJAV.doubleValue());

			if (menor) {
				erro("No  permitido aplicar desconto em nenhuma natureza de negociaes JivaBox!");
			}
		}

		if (booSnkCloud) {

			incluirNat(110123, 1, decVlrSnkCloud, vlrLiqSnkCloud, vlrBrutoSnkCloud, vlrBrutoSnkCloud);
			incluirNat(110227, 0, decVlrMensSnkCloud, vlrLiqSnkCloudMen, vlrBrutoSnkCloudMen, vlrBrutoSnkCloudMen);

		}

		if (booVendaSaas) {

			if (vars.asBigDecimal("ACRDESNEG").equals(BigDecimal.ZERO)) {

				this.sqlValidacaoPorcentagemFluxoSaas = new NativeSql(jdbc);

				this.sqlValidacaoPorcentagemFluxoSaas.loadSql(this.getClass(),
						"ValidacaoVendaSaasHelper_quePorcentagemFluxo.sql");

				sqlValidacaoPorcentagemFluxoSaas.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);

				ResultSet rsetFluxo = sqlValidacaoPorcentagemFluxoSaas.executeQuery();

				BigDecimal porcentagemFluxo = new BigDecimal("0.0").setScale(2, BigDecimal.ROUND_HALF_UP);

				if (rsetFluxo.next()) {
					porcentagemFluxo = rsetFluxo.getBigDecimal("AD_PERCLUSAS");
				}

				porcentagemFluxo = porcentagemFluxo.divide(new BigDecimal("100.00"));

				decVlrLU = decVlrLU.multiply(porcentagemFluxo);

				vlrLiqLU = vlrLiqLU.multiply(porcentagemFluxo);

				vlrBrutoLU = vlrBrutoLU.multiply(porcentagemFluxo);

			}else {
				vlrBrutoLU = vlrBrutoLU.multiply(DEZPORCENTO);
			}

			incluirNat(110124, 1, decVlrLU, vlrLiqLU, vlrBrutoLU, BigDecimal.ZERO);

		} else {
			incluirNat(110106, 1, decVlrLU, vlrLiqLU, vlrBrutoLU, BigDecimal.ZERO);

		}
		BigDecimal vlrBrutoOraEmb = BigDecimal.ZERO;
		if (booLojon) {
			incluirNat(110204, 0, decVlrMen, vlrLiqMen, vlrBrutoMen,
					vlrBrutoMen.add(vlrBrutoLU.multiply(CINCOPORCENTO)));

			if ("E".equals(vars.get("TPNEGIMP")) && menorZero(decAcrDesImp)) {
				erro("No  possvel reduzir a quantidade de horas, quando o <b>Tipo de Neg. na Implantao</b> for <b>Escopo Fechado</b>!");
			}
		} else {
			if ((booFMC || vars.get(TPBANCO).equals("JVB2")) && (!booSnkCloud && !booPossuiCloud)) {
				incluirNat(110113, 1, decVlrFMC, vlrLiqFMC, vlrBrutoFMC, vlrBrutoFMC);
			}
			if (intQtdCNPJ > 0) {
				incluirNat(110120, 1, decVlrCNPJ, vlrLiqCNPJ, vlrBrutoCNPJ, vlrBrutoCNPJ);
			}

			if (possuiHora) {
				incluirNat(110204, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
			} else {
				BigDecimal vlrBrutoAux;
				if (!possuiAdicional && !possuiMensDBA && vlrBrutoMen.doubleValue() < vlrMinMensalidade.doubleValue()) {
					vlrBrutoAux = vlrMinMensalidade;
				} else {
					vlrBrutoAux = vlrBrutoMen;
				}

				if(booVendaBaseNP) {

					int tipoVenda = 0;

					this.sqlTipoVenda = new NativeSql(jdbc);

					this.sqlTipoVenda.loadSql(this.getClass(),
							"ValidacaoNovoPortifolioHelper_queRespostaModeloVenda.sql");

					sqlTipoVenda.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);
					sqlTipoVenda.setNamedParameter("NUMOS", numOS);
					sqlTipoVenda.setNamedParameter("CODPERG", PERGUNTA_MODELO_VENDA);

					ResultSet rsetModeloVenda = sqlTipoVenda.executeQuery();

					if (rsetModeloVenda.next()) {
						tipoVenda = rsetModeloVenda.getInt("CODRESP");
					}

					BigDecimal porcentCalc = new BigDecimal("0.0").setScale(2, BigDecimal.ROUND_HALF_UP);;

					this.sqlPorcentagensFluxo = new NativeSql(jdbc);

					this.sqlPorcentagensFluxo.loadSql(this.getClass(),
							"ValidacaoNovoPortifolioHelper_PorcentagensFluxo.sql");

					sqlPorcentagensFluxo.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);

					ResultSet rsetFluxoPorcen = sqlPorcentagensFluxo.executeQuery();

					if (rsetFluxoPorcen.next()) {

						switch (tipoVenda) {
						case 1:
							porcentCalc  = rsetFluxoPorcen.getBigDecimal("AD_PERCMEN");
							break;
						case 2:
							porcentCalc  = rsetFluxoPorcen.getBigDecimal("AD_PERCMENUSU");
							break;
						case 3:
							porcentCalc  = rsetFluxoPorcen.getBigDecimal("AD_PERCMENSAS");
							break;
						}

						porcentCalc = porcentCalc.divide(new BigDecimal("100.00"));
					}

					this.sqlProdutosNovoPortifolio = new NativeSql(jdbc);

					this.sqlProdutosNovoPortifolio.loadSql(this.getClass(),
							"ValidacaoNovoPortifolioHelper_queValorProdutosContratadosParaSubtrair.sql");

					sqlProdutosNovoPortifolio.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);
					sqlProdutosNovoPortifolio.setNamedParameter("NUMOS", numOS);
					ResultSet rsetProdutosNovoP = sqlProdutosNovoPortifolio.executeQuery();

					BigDecimal somaProdutos = new BigDecimal("0.0").setScale(2);

						while(rsetProdutosNovoP.next()) {

							BigDecimal valor = rsetProdutosNovoP.getBigDecimal("VALOR");

							String calcularPorentagem = rsetProdutosNovoP.getString("CALCULAR_PORCENTAGEM");

							if(calcularPorentagem.equals("Y")) {
								valor = valor.multiply(porcentCalc);
							}

							somaProdutos = somaProdutos.add(valor);

							}

					BigDecimal somaProdutosAliq = somaProdutos.divide(decAliqDU, mctx).setScale(2, RoundingMode.HALF_UP);

					decVlrMen = decVlrMen.subtract(somaProdutosAliq);
					vlrLiqMen = vlrLiqMen.subtract(somaProdutos);
					vlrBrutoAux	 = vlrBrutoAux.subtract(somaProdutosAliq);
					vlrBrutoLU = vlrBrutoLU.subtract(somaProdutosAliq);

				}

				BigDecimal vlrLoc = vlrBrutoLU.multiply(CINCOPORCENTO).setScale(2, BigDecimal.ROUND_HALF_UP);

				incluirNat(110204, 0, decVlrMen, vlrLiqMen, vlrBrutoAux, vlrBrutoAux.add(vlrLoc));

				String fidelizacao = vars.get("AD_TEMPOFIDELIZACAO").toString();

				if (booVendaSaas) {

					this.sqlValidacaoPorcentagemDescontoFluxoSaas = new NativeSql(jdbc);

					this.sqlValidacaoPorcentagemDescontoFluxoSaas.loadSql(this.getClass(),
							"ValidacaoVendaSaasHelper_quePorcentagemDescontoFluxo.sql");

					sqlValidacaoPorcentagemDescontoFluxoSaas.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);

					ResultSet rsetFluxoDesconto = sqlValidacaoPorcentagemDescontoFluxoSaas.executeQuery();

						if (rsetFluxoDesconto.next()) {
						porcentagemDescontoFluxo = rsetFluxoDesconto.getBigDecimal("AD_PERCDESCFID");
					}

					if (percacrdecSaas.compareTo(BigDecimal.ZERO) == -1) {

						if (percacrdecSaas.abs().compareTo(porcentagemDescontoFluxo) == 1
								&& fidelizacao.length() == 0) {
							erro("Favor preencher o Prazo de Fidelidade, pois o desconto para a natureza 110204 - MENSALIDADE LICENA DE USO"
									+ "  MAIOR que " + porcentagemDescontoFluxo + "%");
						} else if (percacrdecSaas.abs().compareTo(porcentagemDescontoFluxo) == -1
								&& fidelizacao.length() > 0) {
							erro("Favor limpar o Prazo de Fidelidade, pois o desconto para a natureza 110204 - MENSALIDADE LICENA DE USO"
									+ "  MENOR que " + porcentagemDescontoFluxo + "%");
						}

					} else if (percacrdecSaas.compareTo(BigDecimal.ZERO) == 1
							|| percacrdecSaas.compareTo(BigDecimal.ZERO) == 0) {
						if (fidelizacao.length() > 0) {
							erro("Favor limpar o Prazo de Fidelidade, pois o desconto para a natureza 110204 - MENSALIDADE LICENA DE USO"
									+ "  MENOR que " + porcentagemDescontoFluxo + "%");
						}
					}

					BigDecimal valorMensalidadeAposFidelidade = vars.asBigDecimal("AD_VLRMENINT").setScale(2,BigDecimal.ROUND_HALF_UP);

						if(fidelizacao.length() > 0) {

							BigDecimal porcentagemDescontoFideli = porcentagemDescontoFluxo.divide(new BigDecimal("100.00"));
							BigDecimal  porcentagemFidelidade =  new BigDecimal("1.00").subtract(porcentagemDescontoFideli);

							BigDecimal  vlrfideli =  vlrBrutoAux.multiply(porcentagemFidelidade).setScale(2,BigDecimal.ROUND_HALF_UP);

							if(valorMensalidadeAposFidelidade.compareTo(new BigDecimal("0.00").setScale(2,BigDecimal.ROUND_HALF_UP))== 0) {
								vars.set("AD_VLRMENINT", vlrfideli);

							}else {

								int compare = valorMensalidadeAposFidelidade.compareTo(vlrfideli);
								if(compare == -1) {
									erro("O Vlr.mensalidade aps fidelidade informado  menor que o valor calculado, favor verificar.");
								}else {
									vars.set("AD_VLRMENINT", valorMensalidadeAposFidelidade);
								}

							}

						}else {

							if(valorMensalidadeAposFidelidade.compareTo(new BigDecimal("0.00").setScale(2,BigDecimal.ROUND_HALF_UP))== 1) {
								erro("Favor limpar o campo  Vlr.mensalidade aps fidelidade, pois no tem Prazo de Fidelidade informado.");
							}

							vars.set("AD_VLRMENINT", "");
						}

				}

				else if (fidelizacao.length() > 0) {
					erro("Favor limpar o Prazo de Fidelidade, pois s pode ser utilizado com o Modelo de Venda SaaS.");

				}else {
					vars.set("AD_VLRMENINT", "");
				}

			}

			if (vlrBrutoMensFMC.compareTo(BigDecimal.ZERO) != 0 && (!booSnkCloud && !booPossuiCloud)) {
				incluirNat(110208, 0, decVlrMensFMC, vlrLiqMensFMC, vlrBrutoMensFMC, vlrBrutoMensFMC);
			}
			if (!booJivaBox) {
				incluirNat(110214, 0, decVlrMensDBA, vlrLiqMensDBA, vlrBrutoMensDBA, vlrBrutoMensDBA);
			}
			if (vlrBrutoEAD.compareTo(BigDecimal.ZERO) != 0 && !booVendaSaas) {
				incluirNat(110601, 1, decVlrEAD, vlrLiqEAD, vlrBrutoEAD, vlrBrutoEAD);
			}
			if (booFGV) {
				incluirNat(110121, 1, decVlrFGV, vlrLiqFGV, vlrBrutoFGV, vlrBrutoFGV);
				incluirNat(110226, 0, decVlrMensFGV, vlrLiqFGVMen, vlrBrutoFGVMen, vlrBrutoFGVMen);
			}

			if (booSnkCloud) {
				if (menorZero(decAcrDesMensSnkCloud)
						&& (decAcrDesMensSnkCloud.doubleValue() / vlrBrutoSnkCloudMen.doubleValue() * 100.0) < -25.0) {
					erro("Desconto na Mensalidade da CLOUD no pode ser maior que 25%!");
				}

				if (menorZero(decAcrDesMensSnkCloud)
						&& decAcrDesMensSnkCloud.doubleValue() > decVlrBonusSnkCloud.doubleValue()
						&& (booPermDescCloud)) {
					erro("No  permitido dar desconto para o Cloud. Pois, j foi aplicado o bnus");

				}

				if (decAcrDesMensSnkCloud.doubleValue() < 0.00) {
					if (decVlrMensSnkCloud.doubleValue() < decVlrMinimoSnkCloud.doubleValue() && (tipNeg == PADRAO)) {
						erro("Mensalidade Cloud no pode ser menor que" + decVlrMinimoSnkCloud.doubleValue()
								+ " reais!");
					}
				}

				if (!this.temPermissao(this.usuarioLogado.intValue(), "USUDESCCLOUD")) {
					if (this.menorZero(decAcrDesCloud) && (decAcrDesCloud.doubleValue() / vlrBrutoSnkCloud.doubleValue()
							* 100.0) < (decPercDescUsuDU.doubleValue() * -1.0)) {
						this.erro(
								"Desconto do Cloud no pode ser maior que " + this.decPercDescUsuDU.toString() + "%!");
					}
				}
				if (this.temPermissao(this.usuarioLogado.intValue(), "USUDESCCLOUD")) {
					this.vars.set("APROVADA", (Object) "S");
				}
			}

			if (booSankhya) {

				if (menorZero(decAcrDesFGV)
						&& (decAcrDesFGV.doubleValue() / vlrBrutoFGV.doubleValue() * 100.0) < -10.0) {
					erro("Desconto na Licena da INTEGRAO FGV no pode ser maior que 10%!");
				}
				if (menorZero(decAcrDesMensFGV)
						&& (decAcrDesMensFGV.doubleValue() / vlrBrutoFGVMen.doubleValue() * 100.0) < -10.0) {
					erro("Desconto na Mensalidade da INTEGRAO FGV no pode ser maior que 10%!");
				}

			}

			if ("E".equals(vars.get("TPNEGIMP")) && menorZero(decAcrDesImp)) {
				erro("No  possvel reduzir a quantidade de horas, quando o <b>Tipo de Neg. na Implantao</b> for <b>Escopo Fechado</b>!");
			}

			incluirNat(110401, 1, decVlrImp, vlrLiqImp, vlrBrutoImp, vlrBrutoImp);

			if (!vars.get(TPBANCO).equals("JVB2")) {
				incluirNat(110403, 1, decVlrBD, vlrLiqBD, vlrBrutoBD, vlrBrutoBD);
				incluirNat(110405, 1, decVlrJAV, vlrLiqJAV, vlrBrutoJAV, vlrBrutoJAV);
			}
			if ("S".equals(vars.get("USAGP"))) {
				BigDecimal qtdHrGP = BigDecimal.valueOf(vars.asDouble(QTDHRNEG)).multiply(percIncidenciaGP).setScale(0,
						BigDecimal.ROUND_HALF_UP);
				BigDecimal valVlrGP = BigDecimalUtil
						.getRounded(BigDecimal.valueOf(qtdHrGP.doubleValue() * decVlrHoraGP.doubleValue()), 2);

				valVlrGP = valVlrGP.divide(decAliqServ, BigDecimal.ROUND_HALF_UP);
				if ("N".equals(strBonifica) && decVlrGP.doubleValue() < valVlrGP.doubleValue()) {
					erro("Valor das horas de Gerncia de Projeto deve ser no mnimo " + valVlrGP.doubleValue() + "!");
				}
				BigDecimal vlrLiqGP = BigDecimalUtil.getRounded(decVlrGP.multiply(decAliqServ), 2);
				incluirNat(110404, 1, decVlrGP, vlrLiqGP, decVlrGPPadrao, decVlrGPPadrao);

			} else {
				decVlrGP = BigDecimal.ZERO;
				decVlrGPPadrao = BigDecimal.ZERO;
			}

			if (decVlrOraEmb.compareTo(BigDecimal.ZERO) != 0) {
				if (booSankhya) {
					sql.select("COTACAO", "TSICOT",
							"CODMOEDA = 7 AND DTMOV = (SELECT MAX(DTMOV) FROM TSICOT CX WHERE CX.CODMOEDA = 7)");
					if (sql.next()) {
						decVlrOraEmb = decVlrOraEmb.multiply(sql.getBigDecimal(1)).divide(BigDecimal.valueOf(0.8875),
								BigDecimal.ROUND_HALF_UP);
					}
					sql.reset();
				}

				BigDecimal vlrLiqOraEmb = decVlrOraEmb;
				decVlrOraEmb = decVlrOraEmb.divide(decAliqDU, BigDecimal.ROUND_HALF_UP);
				vlrBrutoOraEmb = decVlrOraEmb;
				if (!vars.isNull("AD_VLRORAEMB")) {
					decVlrOraEmb = BigDecimal.valueOf(vars.asDouble("AD_VLRORAEMB"));
					vlrLiqOraEmb = BigDecimalUtil.getRounded(decVlrOraEmb.multiply(decAliqDU), 2);
				}

				incluirNat(110901, 1, decVlrOraEmb, vlrLiqOraEmb, vlrBrutoOraEmb, vlrBrutoOraEmb);
			}

		}

		if (!vars.isNull("AD_VLRORAEMB")) {
			BigDecimal dobVlrBrutoOraEmb = vlrBrutoOraEmb;
			if (dobVlrBrutoOraEmb.compareTo(BigDecimal.ZERO) == 0) {
				dobVlrBrutoOraEmb = BigDecimal.ONE;
			}
			BigDecimal decPerc = BigDecimalUtil.getRounded(BigDecimal.valueOf(100)
					.subtract(vars.asBigDecimal("AD_VLRORAEMB").divide(dobVlrBrutoOraEmb, DEC_DUASCASAS_CTX)
							.multiply(BigDecimal.valueOf(100), DEC_DUASCASAS_CTX)),
					2);
			if (decPerc.compareTo(decPercDescUsuOraEmb) > 0) {
				msg("Desconto do Oracle Embarcado " + decPerc + "% maior que o permitido.");
				vars.set(APROVADA, "N");
			}
		}
		if (!vars.isNull("AD_MENSDBA") && vlrBrutoMensDBA.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal decPerc = BigDecimalUtil.getRounded(BigDecimal.valueOf(100)
					.subtract(vars.asBigDecimal("AD_MENSDBA").divide(vlrBrutoMensDBA, DEC_DUASCASAS_CTX)
							.multiply(BigDecimal.valueOf(100), DEC_DUASCASAS_CTX)),
					2);
			if (decPerc.doubleValue() > decPercDescUsuOraEmb.doubleValue()) {
				msg("Desconto de Servios DBA " + decPerc + "% maior que o permitido.");
				vars.set(APROVADA, "N");
			}
		}

		if ("S".equals(vars.get(APROVADA))) {
			if (booJivaW) {
				if ("N".equals(strBonifica)) {
					if (!"".equals(vars.get("ADITIVO"))) {
						vars.set(APROVADA, "T");
					}
				}
			} else if (!booLibAditivo) {
				if (!"".equals(vars.get("ADITIVO"))) {
					vars.set(APROVADA, "T");
					msg("Negociaes com Aditivo precisam de aprovao da Central Comercial !");
				}
				if (perguntaRespondidaSim("2421", numOS)) {
					vars.set(APROVADA, "T");
					msg("Propostas de Degustao precisam de aprovao da Central Comercial !");
				}
			}
		}

		msg("Clculo Realizado com Sucesso!");
	}

	private boolean temPermissao(final int codusu, final String parametro) throws Exception {
		this.sql.select("'S'", "TSIPAR", "CHAVE = '" + parametro + "' AND (TEXTO LIKE '%," + codusu
				+ ",%' OR TEXTO LIKE '" + codusu + ",%' OR TEXTO LIKE '%," + codusu + "')");
		return this.sql.next() && "S".equals(this.sql.getString(1));
	}

	public void msg(String msg) {
		mensagens.add(msg);
	}

	public void erro(String decVlrMensSnkCloud2) throws Exception {
		throw new SimulacaoPrecoSPBean.ScriptError(decVlrMensSnkCloud2);
	}

	public BigDecimal getPrecoProduto(BigDecimal codProd, BigDecimal codTab) throws Exception {
		BigDecimal nuTab = ComercialUtils.getNumeroUnicoTabela(null, codTab);
		return ComercialUtils.obtemPreco3(nuTab, codProd, null, new Timestamp(TimeUtils.getToday()), null);
	}

	public BigDecimal getPrecoProduto(java.math.BigDecimal codProd) throws Exception {
		PrecoTabela preco = ComercialUtils.obtemPrecoTabelaZero(codProd, BigDecimal.ZERO,
				new Timestamp(TimeUtils.getToday()), null);
		return preco.getValorVenda();
	}

	private void validarSankhya(BigDecimal numOS) throws Exception {
		sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("1152"));
		if (sql.next()) {
			try {
				vars.set("TPNEGIMP", (sql.getInt(1) == 1) ? "E" : ((sql.getInt(1) == 2) ? "M" : "B"));
			} catch (Exception e) {
				erro("Informe o <b>Tipo de Neg. na Implantao</b> no diagnstico!");
			}
		} else if (!possuiFuncional && !booDiagUsuario && !possuiHora && !possuiMensDBA && !possuiSnkExpress) {
			erro(msgNecessarioInformar("Tipo de Neg. na Implantao"));
		}
		sql.reset();

		if (possuiHora || possuiMensDBA) {
			vars.set(MATRIZES, 0);
		} else {
			sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("1112"));
			if (sql.next() && !"".equals(sql.getString(1))) {
				try {
					vars.set(MATRIZES, sql.getInt(1));
				} catch (Exception e) {
					erro(msgInformeQtd("Matrizes/Emp.Coligadas") + msgNaoInteiro(sql.getString(1)));
				}
			} else if (possuiAdicional || possuiSnkExpress) {
				vars.set(MATRIZES, 1);
			} else {
				erro(msgNecessarioInformarQtd("Matrizes/Emp. Coligadas"));
			}
			sql.reset();
			if (vars.asInt(MATRIZES) < 1) {
				erro(" necessrio indicar pelo menos 1 <b>Matriz/Empresas Coligada</b>!<br /><b>Atualize o Diagnstico!</b>");
			}
		}

		if (possuiHora || possuiMensDBA || possuiSnkExpress) {
			vars.set(FILIAIS, 0);
		} else {
			sqlIntToVars(numOS, false, "1113", FILIAIS, FILIAIS);
		}

		sql.select("RPE.CODRESP, DECODE(RPE.CODRESP, 2,1.1, 3,1.15, 4,1.2, 1) AS PERC", SQL_TPQRPE_TCSPOS,
				sqlNumOS(numOS) + sqlCodPerg("1200"));
		if (sql.next()) {
			try {
				intTipImplantador = sql.getInt(1);
				percTipImplantador = sql.getBigDecimal(2);
				vars.set("IMPLANTADOR", sql.getBigDecimal(1));
			} catch (Exception e) {
				erro("Selecione uma das opes de <b>Implantador</b> no <b>Diagnstico</b>!");
			}
		} else if (possuiAdicionalOuHoraOuMensDBA || possuiSnkExpress) {
			vars.set("IMPLANTADOR", BigDecimal.ONE);
		} else {
			erro("Para aplicar o clculo  necessrio selecionar uma opo de <b>Implantador</b> no <b>Diagnstico</b>!");
		}
		sql.reset();

		vars.set(USULITE, 0);
		if (perguntaRespondidaSimSemPaiNao("1155", numOS)) {
			addUsuLitePedWeb(numOS, true);
		} else if (possuiAdicional) {
			addUsuLitePedWeb(numOS, false);
		}

		if (perguntaRespondidaSimSemPaiNao("4389", numOS) || possuiAdicional) {
			addUsuLiteControleServico(numOS, false);
		}

		if (perguntaRespondidaSimSemPaiNao("4382", numOS)) {
			addUsuLiteVision1(numOS, true);
		} else if (possuiAdicional) {
			addUsuLiteVision1(numOS, false);
		}

		if (perguntaRespondidaSimSemPaiNao("4832", numOS)) {
			addUsuLiteBIA1(numOS, true);
		} else if (possuiAdicional) {
			addUsuLiteBIA1(numOS, false);
		}

		if (perguntaRespondidaSimSemPaiNao("4831", numOS)) {
			addUsuLiteGPD1(numOS, true);
		} else if (possuiAdicional) {
			addUsuLiteGPD1(numOS, false);
		}

		if (perguntaRespondidaSimSemPaiNao("4678", numOS)) {
			addUsuLiteTask1(numOS, true);
		} else if (possuiAdicional) {
			addUsuLiteTask1(numOS, false);
		}

		if (perguntaRespondidaSimSemPaiNao("5076", numOS)) {
			addUsuLiteTarefa(numOS, true);
		} else if (possuiAdicional) {
			addUsuLiteTarefa(numOS, false);
		}

		if (perguntaRespondidaSimSemPaiNao("3151", numOS)) {
			sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("3218"));
			if (sql.next() && !"".equals(sql.getString(1))) {
				try {
					decQtdUsuTV = decQtdUsuTV.add(sql.getBigDecimal(1));
				} catch (Exception e) {
					erro("Informe a <b>Quantidade de Usurios TV Corporativa</b>" + CORRETAMENTE
							+ msgNaoInteiro(sql.getString(1)));
				}
			} else {
				erro("Para aplicar o clculo  necessrio informar a <b>Quantidade de Usurios TV Corporativa</b> no <b>Diagnstico</b>!");
			}
			sql.reset();
		}

		sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS_TPQRES, sqlNumOS(numOS) + sqlCodPerg("4616") + SQL_LIGACAO_RES_RPE
				+ "    AND RES.DESCRRESP in ('Sim', 'Exibir') " + SQL_NAO_EXISTE_PAI_RESP_1);
		if (sql.next()) {
			erro("Para ter <b>Recrutamento web/ W (captao de currculos pelo site da empresa)</b>  necessrio escolher tambm <b>Recrutamento/ W (recrutamento e seleo de pessoal)</b>!");
		}
		sql.reset();

		boolean possuiFast = false;

		if (perguntaRespondidaSim("5148", numOS)) {
			possuiFast = true;
			sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("5149"));
			if (sql.next()) {
				try {
					qtdUsuCheckout = sql.getInt(1);
					try {
						vars.set(USULITE, vars.asInt(USULITE) + qtdUsuCheckout);
					} catch (Exception e) {
						erro("Erro na soma de usurios Checkout!");
					}
				} catch (Exception e) {
					erro("Informe a <b>Quantidade (em nmero) de Usurios Checkout</b>" + CORRETAMENTE
							+ msgNaoInteiro(sql.getString(1)));
				}
			}
			sql.reset();
		}

		if (possuiSnkExpress) {

			vars.set(USULITE, 0);
			addUsuLite(numOS, false, "1111", USUARIO_FIT);
			if (booSnkExpressServ) {
				addUsuLiteControleServico(numOS, true);
			}

			if (perguntaRespondidaSim("198", numOS)) {
				possuiFast = true;
				sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("1073"));
				if (sql.next()) {
					try {
						qtdUsuFast = sql.getInt(1);
					} catch (Exception e) {
						erro("Informe a <b>Quantidade (em nmero) de Terminais Fast</b>" + CORRETAMENTE
								+ msgNaoInteiro(sql.getString(1)));
					}
				}
				sql.reset();
			}

			strVWprodutos = "(SELECT PRO.CODPROD, 0 AS TEMPO, (CASE WHEN PRO.CODPROD = 10003 THEN "
					+ Integer.toString(qtdUsuFast) + " ELSE 1 END) AS QTDE, PRO.CODNAT " + "FROM TGFPRO PRO "
					+ "WHERE PRO.CODPROD IN (" + "30601," +
					"30602," +
					"30610," +
					"30363," +
					"30611," +
					(booSnkExpressServ ? "" : "30612,") +
					"30617," +
					"30620," +
					"30621," +

					(possuiFast ? "10003," : "") +
					(booSnkCloud ? "20510," : "") +
					(booSnkExpressIND ? "30635,30425,30647,30585,30656," : "") +

					(booSnkExpress ? "30650,30656," : "") +

					(booSnkExpressServ ? "30619,30660,30661,30606,30605,30315," : "") +
					(booSnkExpressServ ? "" : "30627,") +
					"30630," +
					"30633," +
					"30634," +

					"30646," +
					"30655," +
					(booSnkExpressServ ? "" : "30657,") +
					(booSnkExpressServ ? "" : "30678,") +
					"30672)";
			strVWprodutos = strVWprodutos + ") T ";

			nsql.appendSql("SELECT T.CODPROD FROM ");
			nsql.appendSql(strVWprodutos);
			nsql.appendSql(" WHERE T.CODPROD = :CODPROD");
		} else {

			String strSelPerg = getSelPerg(numOS);
			strVWprodutos = "(SELECT RES.CODPROD " + ", CASE WHEN RES.CODPROD = 30309 "
					+ "    THEN AVG(NVL((SELECT DECODE(SEG.CODRESP, 1, 76, 2, 76, 3, 76, 4, 112, 5, 112, 6, 112, 128) FROM TPQRPE SEG WHERE SEG.NUPESQ = RPE.NUPESQ AND SEG.CODPERG = 4618),128) + "
					+ "         NVL((SELECT SIND.CODRESP - 1 FROM TPQRPE SIND WHERE SIND.NUPESQ = RPE.NUPESQ AND SIND.CODPERG = 4619),4) * "
					+ "         NVL((SELECT DECODE(SEG.CODRESP, 1, 16, 2, 16, 3, 16, 4, 16, 5, 18, 6, 18, 16) FROM TPQRPE SEG WHERE SEG.NUPESQ = RPE.NUPESQ AND SEG.CODPERG = 4618),16)) "
					+ "       WHEN RES.CODPROD = 30325 "
					+ "    THEN AVG(NVL((SELECT DECODE(SEG.CODRESP, 1, 36, 2, 36, 3, 36, 4, 50, 5, 50, 6, 50, 76) FROM TPQRPE SEG WHERE SEG.NUPESQ = RPE.NUPESQ AND SEG.CODPERG = 4618),76) + "
					+ "         NVL((SELECT SIND.CODRESP - 1 FROM TPQRPE SIND WHERE SIND.NUPESQ = RPE.NUPESQ AND SIND.CODPERG = 4619),4) * "
					+ "         NVL((SELECT DECODE(SEG.CODRESP, 1, 6, 2, 6, 3, 6, 4, 8, 5, 8, 6, 8, 6) FROM TPQRPE SEG WHERE SEG.NUPESQ = RPE.NUPESQ AND SEG.CODPERG = 4618),6)) "
					+ "ELSE NVL(CEIL(SUM(NVL(FLOOR(RES.AD_TEMPO/100) + (MOD(RES.AD_TEMPO,100)/60),0))),0) END AS TEMPO "
					+ ", (CASE WHEN RES.CODPROD IN (10003) THEN NVL(" + strSelPerg + "1073), '0') " +
					"  WHEN RES.CODPROD IN (30346,30358) THEN NVL(" + strSelPerg + "2117), '0') " +

					"  WHEN RES.CODPROD = 30352 THEN NVL(" + strSelPerg + "1074), '0') " +
					"  WHEN RES.CODPROD = 30204 THEN NVL(" + strSelPerg + "1075), '0') " +
					"  WHEN RES.CODPROD = 30633 THEN NVL(" + strSelPerg + "2311), '0') " +
					"  WHEN RES.CODPROD = 30653 THEN NVL((SELECT to_char(SUM(cast(nvl(R.TEXTO,'1') as number))) FROM TCSPOS P, TPQRPE R WHERE P.NUMOS = "
					+ numOS + " AND P.NUPESQ = R.NUPESQ AND R.CODPERG IN (2723,2724,3218)), '1')" + "  ELSE '1' "
					+ "END) QTDE " + "FROM TPQRPE RPE " + "INNER JOIN TPQRES RES ON " + SQL_LIGACAO_ON_RES_RPE
					+ "INNER JOIN TCSPOS POS ON (POS.NUPESQ = RPE.NUPESQ AND POS.NUMOS = " + numOS
					+ " AND NUMITEM = 0) "
					+ "WHERE POS.APLICAVEL = 'S' AND RES.AD_TEMPO IS NOT NULL AND RES.AD_TEMPO > 0 ";

			nsql.appendSql("SELECT DISTINCT RES.CODPROD ");
			nsql.appendSql("FROM " + SQL_TPQRPE_TCSPOS_TPQRES);
			nsql.appendSql("WHERE " + sqlNumOS(numOS) + " AND POS.APLICAVEL = 'S' AND POS.NUMITEM = 0 ");
			nsql.appendSql(SQL_LIGACAO_RES_RPE);
			nsql.appendSql("AND RES.AD_TEMPO > 0 ");
			nsql.appendSql("AND RES.CODPROD = :CODPROD ");

			sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS,
					sqlNumOS(numOS) + sqlCodPerg("198") + " AND RPE.CODRESP = 2 ");
			if (sql.next()) {
				strVWprodutos = strVWprodutos + "AND RES.CODPROD NOT IN (10003) ";
				nsql.appendSql("AND RES.CODPROD NOT IN (10003) ");
			}
			sql.reset();

			sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS,
					sqlNumOS(numOS) + sqlCodPerg("849") + " AND RPE.CODRESP = 2 ");
			if (sql.next()) {
				strVWprodutos = strVWprodutos + "AND RES.CODPROD NOT IN (30309) ";
				nsql.appendSql("AND RES.CODPROD NOT IN (30309) ");
			}
			strVWprodutos = strVWprodutos + "GROUP BY RES.CODPROD) T ";
			sql.reset();
		}

		produtos.goToStart();
		if (!booDiagUsuario) {
			while (produtos.next()) {
				if (!possuiProd.contains(produtos.asInt(CODPROD))) {
					nsql.setNamedParameter(CODPROD, produtos.get(CODPROD));
					ResultSet rset = nsql.executeQuery();
					if (rset.next()) {
						possuiProd.add(produtos.asInt(CODPROD));
					} else {
						produtos.removeRow();
						produtos.currentIndex--;
					}
					rset.close();
				}
			}
		}

	}

	private void calcularSankhya(BigDecimal numOS) throws Exception {

		sql.select("cast(nvl(trim(RPE.TEXTO),'0') as float) * 1000", SQL_TPQRPE_TCSPOS,
				sqlNumOS(numOS) + sqlCodPerg("2723"));
		if (sql.next() && maiorZero(sql.getBigDecimal(1))) {
			decVlrLU = decVlrLU.add(sql.getBigDecimal(1));
		}
		sql.reset();

		sql.select("cast(nvl(trim(RPE.TEXTO),'0') as float) * 500", SQL_TPQRPE_TCSPOS,
				sqlNumOS(numOS) + sqlCodPerg("2724"));
		if (sql.next() && maiorZero(sql.getBigDecimal(1))) {
			decVlrLU = decVlrLU.add(sql.getBigDecimal(1));
		}
		sql.reset();

		if (vars.asInt(MATRIZES) > 1) {
			percIncidenciaLU = percIncidenciaLU.add(BigDecimal.valueOf((vars.asInt(MATRIZES) - 1) * 0.20));
			percIncidenciaImp = percIncidenciaImp.add(BigDecimal.valueOf((vars.asInt(MATRIZES) - 1) * 0.20));
		}

		if (vars.asInt(FILIAIS) > 0) {
			percIncidenciaLU = percIncidenciaLU.add(BigDecimal.valueOf(vars.asInt(FILIAIS) * 0.10));
			percIncidenciaImp = percIncidenciaImp.add(BigDecimal.valueOf(vars.asInt(FILIAIS) * 0.10));
		}

		qtdHrBase = qtdHrBase.subtract(qtdHrSemInc);
		switch (intPorte) {
		case 1:
			decVlrLU = decVlrLU.multiply(BigDecimal.valueOf(1.3));

			decDescMigra = decDescMigra.multiply(BigDecimal.valueOf(1.3));
			qtdHrBase = qtdHrBase.multiply(BigDecimal.valueOf(1.3));
			break;
		case 2:
			decVlrLU = decVlrLU.multiply(BigDecimal.valueOf(1.6));
			decDescMigra = decDescMigra.multiply(BigDecimal.valueOf(1.6));
			qtdHrBase = qtdHrBase.multiply(BigDecimal.valueOf(1.6));
			break;
		case 3:
			decVlrLU = decVlrLU.multiply(BigDecimal.valueOf(2));
			decDescMigra = decDescMigra.multiply(BigDecimal.valueOf(2));
			qtdHrBase = qtdHrBase.multiply(BigDecimal.valueOf(2));
			break;
		default:
			break;
		}

		if (possuiAdicional || perguntaRespondidaSim("1169", numOS)) {
			addUsuLite(numOS, false, "2705", "Usurios Venda Consultiva (FIT)");
		}

		decVlrLU = BigDecimalUtil.getRounded(decVlrLU.multiply(percIncidenciaLU), 2);

		decDescMigra = BigDecimalUtil.getRounded(decDescMigra.multiply(percIncidenciaLU), 2);
		if (maiorZero(decQtdUsuTV)) {
			decVlrLU = decVlrLU.add(decQtdUsuTV.multiply(BigDecimal.valueOf(1000)));
		}
		BigDecimal usuLite = new BigDecimal(vars.asInt(USULITE) - qtdUsuCheckout);

		if (usuLite.intValue() > 0) {
			if (decVlrLU.compareTo(BigDecimal.valueOf(100000)) > 0) {

				decVlrLU = decVlrLU.multiply(BigDecimal.ONE
						.add(vars.asBigDecimal(USULITE).add(decQtdUsuBIA).multiply(BigDecimal.valueOf(0.01))));
			} else {
				decVlrLU = decVlrLU.add(usuLite.add(decQtdUsuBIA).multiply(BigDecimal.valueOf(1000)));
			}
		}

		addUsuLite(numOS, false, "2723", "Usurios DashViewer");

		addUsuLite(numOS, false, "2724", "Usurios FormViewer");

		if (possuiAdicional) {
			sql.select("DECODE(RPE.CODRESP, 1, 1977.73, 2, 2423.16, 3198.22)", SQL_TPQRPE_TCSPOS,
					sqlNumOS(numOS) + sqlCodPerg("1820"));
			if (sql.next()) {
				decVlrLU = decVlrLU.add(vars.asBigDecimal(USUFULL).multiply(sql.getBigDecimal(1)));

			}
			sql.reset();

			sql.select("cast(nvl(trim(RPE.TEXTO),'0') as float) * 1000", SQL_TPQRPE_TCSPOS,
					sqlNumOS(numOS) + sqlCodPerg("4542"));
			if (sql.next()) {

			}
			sql.reset();
		} else if (vars.asInt(USUFULL) > 3) {
			decVlrLU = decVlrLU.add(BigDecimal.valueOf((vars.asInt(USUFULL) - 3) * 1611.00));
			if (decDescMigra.doubleValue() > 0) {
				sql.select("COUNT(*), MAX(PSC.NUMUSUARIOS)", "TCSOSE OSE, TCSPSC PSC, TCSCON CON, TCSPAP PAP",
						"OSE.CODPAP = PAP.CODPAP AND CON.CODPARC = PAP.CODPARC AND PSC.NUMCONTRATO = CON.NUMCONTRATO AND PSC.CODPROD in (30101, 30403, 30404) AND OSE.NUMOS = "
								+ numOS);
				if (sql.next() && (sql.getInt(1) > 0)) {
					if (sql.getInt(2) >= vars.asInt(USUFULL)) {
						decDescMigra = decDescMigra.add(BigDecimal.valueOf((vars.asInt(USUFULL) - 3) * 805.5));
					} else {
						decDescMigra = decDescMigra.add(BigDecimal.valueOf((sql.getInt(2) - 3) * 805.5));
					}
				}
				sql.reset();
			}
			qtdHrBase = qtdHrBase.add(BigDecimal.valueOf((vars.asInt(USUFULL) - 3) * 2f));
		}

		if (possuiProd.contains(30755) && (!booSnkPacks)) {
			sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("5205"));
			if (sql.next()) {
				decVlrLU = decVlrLU.add(getVlrMenPorUsu(30755, sql.getInt(1), false, false));
			}
		}

		booFGV = possuiProd.contains(30753);

		if (booFGV) {
			sql.select("RPE.TEXTO, RES.CODPROD", SQL_TPQRPE_TCSPOS_TPQRES,
					sqlNumOS(numOS) + sqlCodPerg("5090") + SQL_LIGACAO_RES_RPE);
			if (sql.next() && !"".equals(sql.getString(1))) {
				try {
					decVlrFGV = decVlrFGV.add(getPrecoProduto(sql.getBigDecimal(2))).setScale(2,
							BigDecimal.ROUND_HALF_UP);
				} catch (Exception e) {
					erro("Informe a <b>Quantidade de CNPJ para FGV</b>" + CORRETAMENTE
							+ msgNaoInteiro(sql.getString(1)));
				}
			}
			sql.reset();

			sql.select("RPE.TEXTO, RES.CODPROD", SQL_TPQRPE_TCSPOS_TPQRES,
					sqlNumOS(numOS) + sqlCodPerg("5091") + SQL_LIGACAO_RES_RPE);
			if (sql.next() && !"".equals(sql.getString(1))) {
				try {
					decVlrFGV = decVlrFGV.add(BigDecimal.valueOf(1000).multiply(sql.getBigDecimal(1)));
				} catch (Exception e) {
					erro("Informe a <b>Quantidade de CNPJ adicionais para FGV</b>" + CORRETAMENTE
							+ msgNaoInteiro(sql.getString(1)));
				}
			}
			sql.reset();
			decVlrLU = decVlrLU.subtract(decVlrFGV);

			sql.select("RPE.TEXTO, RES.CODPROD", SQL_TPQRPE_TCSPOS_TPQRES,
					sqlNumOS(numOS) + sqlCodPerg("5093") + SQL_LIGACAO_RES_RPE);
			if (sql.next() && !"".equals(sql.getString(1))) {
				try {
					decVlrMensFGV = decVlrMensFGV.add(BigDecimal.valueOf(100).multiply(sql.getBigDecimal(1)));
				} catch (Exception e) {
					erro("Informe a <b>Quantidade de pacote adicional de 10 produtos para FGV</b>" + CORRETAMENTE
							+ msgNaoInteiro(sql.getString(1)));
				}
			}
			sql.reset();

			decVlrMensFGV = decVlrMensFGV.add(BigDecimalUtil.getRounded(decVlrFGV.multiply(CINCOPORCENTO), 2));
		}

		if (booSnkCloud) {

			int qtdUsu = vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld;
			sql.select("T.BONUS,T.VLRMINIMO, T.PERMDESC",
					"(SELECT TPP.CODPROD,NVL(TPP.BONUS,0) AS BONUS,NVL(TPP.VLRMINIMO,0) AS VLRMINIMO,NVL(TPP.PERMDESC,'N')AS PERMDESC, NVL((SELECT MAX(QTD) + 1 FROM AD_TCSTPP WHERE CODPROD = TPP.CODPROD AND QTD < TPP.QTD), 1) AS MIN, TPP.QTD AS MAX, TPP.VLR FROM AD_TCSTPP TPP) T",
					"T.MIN <= " + qtdUsu + " AND T.MAX >= " + qtdUsu + " AND T.CODPROD =20510");
			if (sql.next()) {
				decVlrBonusSnkCloud = sql.getBigDecimal("BONUS");
				decVlrMinimoSnkCloud = sql.getBigDecimal("VLRMINIMO");
				booPermDescCloud2 = sql.getString("PERMDESC");
			}
			sql.reset();

			boolean temEcommerce = false;
			if (!booPossuiCloud) {
				sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS,
						sqlNumOS(numOS) + sqlCodPerg("5143,5543") + " AND RPE.CODRESP = 4 ");
				if (sql.next()) {
					temEcommerce = true;
				}
			}

			decVlrMensSnkCloud = getVlrMenPorUsu(20510, vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld,
					temEcommerce, true);

			if ((decVlrMensSnkCloud.doubleValue() > 0.00)
					&& (decVlrMensSnkCloud.doubleValue() < decVlrMinimoSnkCloud.doubleValue()) && (tipNeg == PADRAO)) {
				decVlrMensSnkCloud = decVlrMinimoSnkCloud;
			}

			vars.set(AD_USUCLD, vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld);
		}

		if (!booTemProdWebService && perguntaRespondidaSimSemPaiNao("4607", numOS)) {
			sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("4608"));
			if (sql.next() && !"".equals(sql.getString(1))) {
				try {
					qtdHrBase = qtdHrBase.add(sql.getBigDecimal(1));
				} catch (Exception e) {
					erro("Informe a <b>Quantidade de horas para integrao de WebServices</b>" + CORRETAMENTE
							+ msgNaoInteiro(sql.getString(1)));
				}
			} else {
				erro("Para aplicar o clculo  necessrio informar a <b>Quantidade de horas para integrao de WebServices</b> no <b>Diagnstico</b>!");
			}
			sql.reset();
		}

		if (qtdHrBase != null) {
			qtdHrBase = qtdHrBase.multiply(percIncidenciaImp).setScale(0, BigDecimal.ROUND_HALF_UP);
			qtdHrBase = qtdHrBase.add(qtdHrSemInc);
		} else {
			qtdHrBase = BigDecimal.ZERO;
		}

		if ("ND".equals(vars.get(TPBANCO))) {
			decVlrBD = BigDecimal.ZERO;
		} else {
			decVlrBD = decVlrImpBD;
		}

		if (booInstJav) {
			decVlrJAV = decVlrInsJAV;
		}

		sql.select(
				"nvl(SUM(case when trim(translate(TRIM(RPE.TEXTO), '+-0123456789.', ' ')) is null then TO_NUMBER(RPE.TEXTO) else 0 END),0) * 1000",
				SQL_TPQRPE_TCSPOS,
				sqlNumOS(numOS) + sqlCodPerg("4692,4693,4694,4695") + " AND TRIM(RPE.TEXTO) IS NOT NULL ");
		if (sql.next()) {
			decVlrLU = decVlrLU.add(sql.getBigDecimal(1));
		}
		sql.reset();

		if (possuiAdicional) {
			decVlrMen = BigDecimalUtil.getRounded(decVlrLU.multiply(CINCOPORCENTO), 2);
			decVlrEAD = BigDecimal.valueOf(190);
		} else if (!possuiMensDBA) {
			decVlrMen = BigDecimalUtil.getRounded(decVlrLU.multiply(CINCOPORCENTO), 2);
			decVlrMen = BigDecimalUtil.getRounded(decVlrMen.multiply(percTipImplantador), 2);
			if (decVlrMen.doubleValue() < 600) {
				decVlrMen = BigDecimal.valueOf(600);
			}
		}

		if (possuiSnkExpress) {

			if (menorIgual(decPercDescUsuDU, 20)) {
				decPercDescUsuDU = decPercDescUsuSer = BigDecimal.ZERO;
			}

			int intTipoSnkExpress = 0;

			sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("2702,4606"));
			if (sql.next()) {
				intTipoSnkExpress = sql.getInt(1);
			}
			sql.reset();

			if (booSnkExpress) {
				qtdHrBase = BigDecimal.valueOf(392.00);

				switch (intTipoSnkExpress) {
				case 1:
					decVlrLU = BigDecimal.valueOf(24708.75);

					decVlrMen = BigDecimal.valueOf(1358.98);
					break;
				case 2:
					decVlrLU = BigDecimal.valueOf(44925.00);

					decVlrMen = BigDecimal.valueOf(2695.50);
					break;
				default:
					decVlrLU = BigDecimal.valueOf(60648.75);

					decVlrMen = BigDecimal.valueOf(3638.93);
					break;
				}
			} else if (booSnkExpressIND) {
				qtdHrBase = BigDecimal.valueOf(454.00);

				switch (intTipoSnkExpress) {
				case 1:
					decVlrLU = BigDecimal.valueOf(33693.75);

					decVlrMen = BigDecimal.valueOf(1853.16);
					break;
				case 2:
					decVlrLU = BigDecimal.valueOf(53910.00);

					decVlrMen = BigDecimal.valueOf(3234.60);
					break;
				default:
					decVlrLU = BigDecimal.valueOf(74126.25);

					decVlrMen = BigDecimal.valueOf(4447.58);
					break;
				}
			} else if (booSnkExpressServ) {
				qtdHrBase = BigDecimal.valueOf(465.00);

				switch (intTipoSnkExpress) {
				case 1:
					decVlrLU = BigDecimal.valueOf(27853.50);

					decVlrMen = BigDecimal.valueOf(1671.21);
					break;
				case 2:
					decVlrLU = BigDecimal.valueOf(51214.50);

					decVlrMen = BigDecimal.valueOf(3072.87);
					break;
				default:
					decVlrLU = BigDecimal.valueOf(70083.00);

					decVlrMen = BigDecimal.valueOf(4204.98);
					break;
				}
			}

			decVlrEAD = BigDecimal.valueOf(205.42);

			decVlrLU = decVlrLU.add(BigDecimal.valueOf(qtdUsuFast * 1125.41f));

			qtdHrBase = qtdHrBase.add(BigDecimal.valueOf(qtdUsuFast * 20f));
			decVlrMen = decVlrMen.add(BigDecimal.valueOf(qtdUsuFast * 56.27f));

			decVlrLU = decVlrLU.add(vars.asBigDecimal(USULITE).multiply(BigDecimal.valueOf(605.99)));

			decVlrMen = decVlrMen.add(vars.asBigDecimal(USULITE).multiply(BigDecimal.valueOf(30.30)));

			int qtdEAD = 15;
			if (intTipoSnkExpress == 1) {
				qtdEAD = 5;
			} else if (intTipoSnkExpress == 2) {
				qtdEAD = 10;
			}

			vars.set("USAGP", "S");
			vars.set(PORTE, "0");
			vars.set(QTDCERT, 1);
			vars.set(QTDHABI, 0);
			vars.set(QTDEAD, qtdEAD);
			vars.set("TPNEGIMP", "E");
			vars.set(USUFULL, qtdEAD);
			vars.set(AD_USUCLD, vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld);

			if (!booSnkCloud) {
				vars.set(AD_USUCLD, 0);
			}
		}

		if (booSnkCloud) {

			int qtdUsu = vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld;
			sql.select("T.BONUS,T.VLRMINIMO, T.PERMDESC",
					"(SELECT TPP.CODPROD,NVL(TPP.BONUS,0) AS BONUS,NVL(TPP.VLRMINIMO,600) AS VLRMINIMO,NVL(TPP.PERMDESC,'N')AS PERMDESC, NVL((SELECT MAX(QTD) + 1 FROM AD_TCSTPP WHERE CODPROD = TPP.CODPROD AND QTD < TPP.QTD), 1) AS MIN, TPP.QTD AS MAX, TPP.VLR FROM AD_TCSTPP TPP) T",
					"T.MIN <= " + qtdUsu + " AND T.MAX >= " + qtdUsu + " AND T.CODPROD =20510");
			if (sql.next()) {
				decVlrBonusSnkCloud = sql.getBigDecimal("BONUS");
				decVlrMinimoSnkCloud = sql.getBigDecimal("VLRMINIMO");
				booPermDescCloud2 = sql.getString("PERMDESC");
			}
			sql.reset();

			boolean temEcommerce = false;
			if (!booPossuiCloud) {
				sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS,
						sqlNumOS(numOS) + sqlCodPerg("5143,5543") + " AND RPE.CODRESP = 4 ");
				if (sql.next()) {
					temEcommerce = true;
				}
			}

			decVlrMensSnkCloud = getVlrMenPorUsu(20510, vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld,
					temEcommerce, true);

			if ((decVlrMensSnkCloud.doubleValue() > 0.00)
					&& (decVlrMensSnkCloud.doubleValue() < decVlrMinimoSnkCloud.doubleValue()) && (tipNeg == PADRAO)) {
				decVlrMensSnkCloud = decVlrMinimoSnkCloud;
			}

			vars.set(AD_USUCLD, vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld);

		}

		decVlrLU = decVlrLU
				.add(BigDecimal.valueOf(qtdUsuCheckout).multiply(getPrecoProduto(BigDecimal.valueOf(30748))));

		decVlrMen = decVlrMen.add(BigDecimal.valueOf(qtdUsuCheckout)
				.multiply(getPrecoProduto(BigDecimal.valueOf(30748))).multiply(CINCOPORCENTO));

		vars.set("QTDHRTOT", qtdHrBase.intValue());
		vars.set(QTDHRNEG, qtdHrBase.intValue());

		if(booVendaBaseNP) {
			vlrMinMensalidade = BigDecimal.ZERO;
		}else {
			vlrMinMensalidade = BigDecimal.valueOf(790);
		}

		decVlrImp = BigDecimalUtil.getRounded(qtdHrBase.multiply(decVlrHora), 2);
		decVlrMen = decVlrMen.add(decVlrCNPJ.multiply(CINCOPORCENTO));
	}

	private void validarPreenchimento(BigDecimal numOS, boolean booJivaW) throws Exception {
		String query = "(SELECT POS.CODFLD, COUNT(DISTINCT POS.CODFLD) AS QTDFLUXO, COUNT(DISTINCT POS.NUPESQ) AS QTDQUESRESP "
				+ ", COUNT(DISTINCT QXF.CODQUEST) AS QTDQUESTOTAL " + "FROM TCSPOS POS "
				+ "INNER JOIN TCSQXF QXF ON POS.CODFLD = QXF.CODFLD " + "WHERE POS.NUMOS = " + numOS
				+ " AND EXISTS(SELECT 1 FROM TCSFLD WHERE FLUXO LIKE '%codQuest=\"' || QXF.CODQUEST ||'\"%') "
				+ "GROUP BY POS.CODFLD) T";
		sql.select("T.CODFLD, T.QTDFLUXO, T.QTDQUESRESP, T.QTDQUESTOTAL", query);
		if (!sql.next()) {
			erro("Para aplicar a simulao  necessrio responder o diagnstico!");
		} else if (sql.getInt(2) != 1) {
			erro("Foi preenchido mais de um <b>Diagnstico</b>! Voc deve preencher apenas um!");
		} else if (booJivaW && !booJivaBox) {
			if (sql.getInt(1) != 1 && !possuiFuncional && !possuiAdicionalOuHoraOuMensDBA) {
				erro("Voc no preencheu o <b>Diagnstico Padro Jiva</b>!");
			} else if (sql.getInt(3) < sql.getInt(4)) {
				erro("Voc no preencheu todos os questionrios do <b>Diagnstico</b>!  necessrio <b>Responder</b> ou indicar <b>Questionrio no aplicvel</b>!");
			}
		}
		sql.reset();
	}

	private void validarJiva(BigDecimal numOS) throws Exception {
		validarPreenchimento(numOS, true);

		sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("884"));
		if (sql.next() && !"".equals(sql.getString(1))) {
			try {
				sql.getBigDecimal(1);
			} catch (Exception e) {
				erro("Informe O <b>Nmero de Impressoras Fiscais (PDV)</b>" + CORRETAMENTE
						+ msgNaoInteiro(sql.getString(1)));
			}
		}
		sql.reset();

		sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("2119"));
		if (sql.next() && !"".equals(sql.getString(1))) {
			try {
				if (sql.getInt(1) <= 0) {
					erro("Informe a <b>Quantidade de Usurio exclusivo p/Vendas</b>" + CORRETAMENTE
							+ msgNaoInteiro(sql.getString(1)));
				} else {
					intPEDWEB = intPEDWEB + sql.getInt(1);
				}
			} catch (Exception e) {
				erro("Informe a <b>Quantidade de Usurio exclusivo p/Vendas</b>" + CORRETAMENTE
						+ msgNaoInteiro(sql.getString(1)));
			}
		}
		sql.reset();

		sqlIntToVars(numOS, false, "880", "Matrizes e/ou Coligadas", MATRIZES);

		vars.set(FILIAIS, 0);

		if (perguntaRespondidaSim("1340", numOS)) {
			sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("1341"));
			if (sql.next() && !"".equals(sql.getString(1))) {
				try {
					decQtdBD = decQtdBD.add(sql.getBigDecimal(1));
				} catch (Exception e) {
					erro(msgInformeQtd("Bases") + msgNaoInteiro(sql.getString(1)));
				}
			} else {
				erro(msgNecessarioInformarQtd("Bases"));
			}
			sql.reset();
		}

		int intEDISoma = 0;

		if (perguntaRespondidaSimSemPaiNao("712", numOS)) {
			intEDISoma = intEDISoma + getEDISoma(numOS, "891", "PAGAMENTO");
		}

		if (perguntaRespondidaSimSemPaiNao("763,1451,1511", numOS)) {
			intEDISoma = intEDISoma + getEDISoma(numOS, "890", "COBRANA e RECEBIMENTO");
		}
		if (intEDISoma <= 0) {
			intEDISoma = 1;
		}

		String strSelPerg = getSelPerg(numOS);
		strVWprodutos = "(SELECT * FROM (SELECT Q1.CODPROD "
				+ ", Q1.QTDE, CEIL(SUM(NVL(GET_VALOR_PESQUISA (Q1.NUPESQ, Q1.CODPRODRES),0))/60) AS TEMPO "
				+ " FROM (SELECT DISTINCT RES.AD_PRODWEB AS CODPROD "
				+ " , (CASE WHEN RES.AD_PRODWEB = 20441 THEN TO_CHAR(" + Integer.toString(intEDISoma) + ")"
				+ "   WHEN RES.AD_PRODWEB = 20410          THEN NVL(" + strSelPerg + "884), '0') " +
				"   WHEN RES.AD_PRODWEB IN (20433,30346) THEN NVL(" + strSelPerg + "885), '0') " +
				"   WHEN RES.AD_PRODWEB = 20465          THEN NVL(" + strSelPerg + "2119), '0') " +
				"   WHEN RES.AD_PRODWEB = 20489          THEN NVL(" + strSelPerg + "4538), '0') " +
				"   WHEN RES.AD_PRODWEB = 20506          THEN NVL(" + strSelPerg + "4866), '0') " +
				"   ELSE '1' END) QTDE" + " , RES.CODPROD AS CODPRODRES, POS.NUPESQ " + " FROM "
				+ SQL_TPQRPE_TCSPOS_TPQRES + " WHERE " + sqlNumOS(numOS)
				+ " AND POS.APLICAVEL = 'S' AND POS.NUMITEM = 0" + SQL_LIGACAO_RES_RPE
				+ " AND RES.CODPROD IS NOT NULL) Q1  " + "GROUP BY Q1.CODPROD, Q1.QTDE "
				+ ") WHERE TEMPO > 0 OR 'true' = '" + booJivaBox + "') T ";

		vars.set(USULITE, 0);
		sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("2119")
				+ " AND NOT EXISTS (SELECT 1 FROM TPQRPE R2 WHERE R2.CODPERG = 1628 AND R2.NUPESQ = POS.NUPESQ AND R2.CODRESP = 2)");
		if (sql.next() && !"".equals(sql.getString(1))) {
			try {
				vars.set(USULITE, sql.getInt(1));
			} catch (Exception e) {
				erro("Informe a <b>Quantidade de Usurios exclusivo para Vendas</b>" + CORRETAMENTE
						+ msgNaoInteiro(sql.getString(1)));
			}
		}
		sql.reset();

		boolean possuiFast = false;

		if (perguntaRespondidaSim("5148", numOS)) {
			possuiFast = true;
			sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("5149"));
			if (sql.next()) {
				try {
					qtdUsuCheckout = sql.getInt(1);
					try {
						vars.set(USULITE, vars.asInt(USULITE) + qtdUsuCheckout);
					} catch (Exception e) {
						erro("Erro na soma de usurios Checkout!");
					}
				} catch (Exception e) {
					erro("Informe a <b>Quantidade (em nmero) de Usurios Checkout</b>" + CORRETAMENTE
							+ msgNaoInteiro(sql.getString(1)));
				}
			}
			sql.reset();
		}

		if (perguntaRespondidaSimSemPaiNao("4537", numOS)) {
			addUsuLiteVisionApp1(numOS, true);
		} else if (possuiAdicional) {
			sql.reset();
			addUsuLiteVisionApp1(numOS, false);
		}
		sql.reset();
	}

	private void calcularJiva(BigDecimal numOS) throws Exception {

		if (booJivaBox) {
			boolean temDataSinc = perguntaRespondidaSim("1061", numOS);
			boolean temFast = perguntaRespondidaSim("602", numOS);

			vars.set(TPBANCO, "");
			vars.set("TPNEGIMP", "E");
			double usuAdd = 0;
			if (vars.asDouble(USUFULL) > 3) {
				usuAdd = vars.asDouble(USUFULL) - 3;
			}

			BigDecimal luBox = BigDecimal.valueOf(7000);
			BigDecimal cmBox = BigDecimal.valueOf(420);

			if (temFast) {
				sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("884"));
				if (sql.next() && !"".equals(sql.getString(1))) {
					qtdUsuFast = sql.getInt(1);
					luBox = luBox.add(BigDecimal.valueOf(750 + (750 * qtdUsuFast)));
					cmBox = cmBox.add(BigDecimal.valueOf(35 + (35 * qtdUsuFast)));
				} else {
					msgNecessarioInformar("Nmero de impressoras fiscais (PDV)");
				}

				if (temDataSinc) {
					sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("885"));
					if (sql.next() && !"".equals(sql.getString(1))) {
						luBox = luBox.add(BigDecimal.valueOf(50 + (50 * intDataSinc)));
						cmBox = cmBox.add(BigDecimal.valueOf(5 + (5 * intDataSinc)));
					}
				}
			}

			if (perguntaRespondidaSim("4974", numOS)) {
				luBox = luBox.add(BigDecimal.valueOf(1320));
				cmBox = cmBox.add(BigDecimal.valueOf(75));
			}

			MathContext mctx = new MathContext(64, RoundingMode.HALF_UP);
			qtdHrBase = qtdHrBase.add(BigDecimal.valueOf(1 * usuAdd));
			decVlrLU = luBox.add(BigDecimal.valueOf(usuAdd * 890)).multiply(decAliqDU, mctx);

			decVlrMen = cmBox.add(BigDecimal.valueOf(usuAdd * 40)).multiply(decAliqDU, mctx);

			sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("4976"));
			if (sql.next()) {
				switch (sql.getInt(1)) {
				case 1:
					vars.set(TPBANCO, "JVB1");
					decVlrImpBD = BigDecimal.valueOf(30).multiply(decAliqServ, mctx);

					decVlrJAV = BigDecimal.valueOf(70).multiply(decAliqServ, mctx);

					break;
				case 2:
					vars.set(TPBANCO, "JVB2");
					decVlrFMC = BigDecimal.valueOf(100).multiply(decAliqDU, mctx);

					decVlrMensFMC = BigDecimal.valueOf(50 * vars.asDouble(USUFULL)).multiply(decAliqDU, mctx);

					break;
				}
			} else {
				erro(msgNecessarioInformar("Instalao Jiva Box"));
			}

			if (vars.get(TPBANCO).equals("JVB1") && temDataSinc) {
				erro("Quando a instalao  local no deve ser informado <b>PDV offline</b> !");
			}
			if (vars.get(TPBANCO).equals("JVB2") && temFast && !temDataSinc) {
				erro("Instalao de FAST na nuvem  necessrio informar <b>PDV offline</b> !");
			}

		} else if (booMigracao) {
			decVlrLU = decVlrLU.add(decSomaPrecos);

			if (vars.asInt(MATRIZES) > 0) {
				percIncidenciaImp = percIncidenciaImp.add(BigDecimal.valueOf(vars.asInt(MATRIZES) * 0.10));

				decVlrLU = decVlrLU.add(BigDecimal.valueOf(vars.asInt(MATRIZES) * 3000));
			}

			if (vars.asDouble("SEGMENTOS") > 1) {
				percIncidenciaImp = percIncidenciaImp.add(BigDecimal.valueOf(vars.asInt("SEGMENTOS") * 0.25));
			}
			decVlrLU = BigDecimalUtil.getRounded(decVlrLU.multiply(percIncidenciaLU), 2);

			decVlrMen = BigDecimalUtil.getRounded(decVlrLU.multiply(CINCOPORCENTO), 2);

			decVlrMen = decVlrMen.add(BigDecimal.valueOf(intDataSinc * 100f));

			if (intPEDWEB > 0) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(intPEDWEB * 40f));
				decVlrLU = decVlrLU.add(BigDecimal.valueOf(intPEDWEB * 500f));

			}
			decVlrMen = decVlrMen.add(BigDecimal.valueOf(vars.asInt(USUFULL) * 40f));

			decVlrLU = decVlrLU.add(BigDecimal.valueOf(vars.asInt(USUFULL) * 800f));

			percIncidenciaImp = percIncidenciaImp.add(BigDecimal.valueOf(vars.asInt(USUFULL) * 0.02));
		} else {
			decVlrLU = decVlrLU.add(decSomaPrecos);

			if (possuiAdicional && vars.asInt(MATRIZES) > 0) {
				percIncidenciaImp = percIncidenciaImp.add(BigDecimal.valueOf(vars.asInt(MATRIZES) * 0.10));
				percIncidenciaLU = percIncidenciaLU.add(BigDecimal.valueOf(vars.asInt(MATRIZES) * 0.10));
				if (decVlrLU.equals(BigDecimal.ZERO)) {
					decVlrLU = BigDecimal.valueOf(vars.asInt(MATRIZES) * 3000);

				}
			} else if (vars.asInt(MATRIZES) > 1) {
				percIncidenciaImp = percIncidenciaImp.add(BigDecimal.valueOf((vars.asInt(MATRIZES) - 1) * 0.10));
				percIncidenciaLU = percIncidenciaLU.add(BigDecimal.valueOf((vars.asInt(MATRIZES) - 1) * 0.10));
			}

			if (vars.asDouble("SEGMENTOS") > 1) {
				percIncidenciaImp = percIncidenciaImp.add(BigDecimal.valueOf((vars.asInt("SEGMENTOS") - 1) * 0.25));
			}

			decVlrLU = BigDecimalUtil.getRounded(decVlrLU.multiply(percIncidenciaLU), 2);

			decVlrMen = BigDecimalUtil.getRounded(decVlrLU.multiply(CINCOPORCENTO), 2);

			decVlrMen = decVlrMen.add(BigDecimal.valueOf(intDataSinc * 100f));

			if (intPEDWEB > 0) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(15f));

			}
			if (intPEDWEB > 1) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf((intPEDWEB - 1) * 40f));
				decVlrLU = decVlrLU.add(BigDecimal.valueOf((intPEDWEB - 1) * 500f));

			}
			if (possuiAdicional) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(vars.asInt(USUFULL) * 40f));
				decVlrLU = decVlrLU.add(BigDecimal.valueOf(vars.asInt(USUFULL) * 800f));

				percIncidenciaImp = percIncidenciaImp.add(BigDecimal.valueOf(vars.asInt(USUFULL) * 0.02));
			} else {
				if (vars.asDouble(USUFULL) > 3) {
					decVlrMen = decVlrMen.add(BigDecimal.valueOf((vars.asInt(USUFULL) - 3) * 40f));

					decVlrLU = decVlrLU.add(BigDecimal.valueOf((vars.asInt(USUFULL) - 3) * 800f));

					percIncidenciaImp = percIncidenciaImp.add(BigDecimal.valueOf((vars.asInt(USUFULL) - 3) * 0.02));
				}
				if (menorQue(decVlrMen, 470)) {
					decVlrMen = BigDecimal.valueOf(470);
				}
			}
			vlrMinMensalidade = BigDecimal.valueOf(540);
		}

		if (booSnkCloud) {

			int qtdUsu = vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld;
			sql.select("T.BONUS,T.VLRMINIMO, T.PERMDESC",
					"(SELECT TPP.CODPROD,NVL(TPP.BONUS,0) AS BONUS,NVL(TPP.VLRMINIMO,600) AS VLRMINIMO,NVL(TPP.PERMDESC,'N')AS PERMDESC, NVL((SELECT MAX(QTD) + 1 FROM AD_TCSTPP WHERE CODPROD = TPP.CODPROD AND QTD < TPP.QTD), 1) AS MIN, TPP.QTD AS MAX, TPP.VLR FROM AD_TCSTPP TPP) T",
					"T.MIN <= " + qtdUsu + " AND T.MAX >= " + qtdUsu + " AND T.CODPROD =20510");
			if (sql.next()) {
				decVlrBonusSnkCloud = sql.getBigDecimal("BONUS");
				decVlrMinimoSnkCloud = sql.getBigDecimal("VLRMINIMO");
				booPermDescCloud2 = sql.getString("PERMDESC");
			}
			sql.reset();

			boolean temEcommerce = false;
			if (!booPossuiCloud) {
				sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS,
						sqlNumOS(numOS) + sqlCodPerg("5143,5543") + " AND RPE.CODRESP = 4 ");
				if (sql.next()) {
					temEcommerce = true;
				}
			}

			decVlrMensSnkCloud = getVlrMenPorUsu(20510, vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld,
					temEcommerce, true);

			if ((decVlrMensSnkCloud.doubleValue() > 0.00)
					&& (decVlrMensSnkCloud.doubleValue() < decVlrMinimoSnkCloud.doubleValue()) && (tipNeg == PADRAO)) {
				decVlrMensSnkCloud = decVlrMinimoSnkCloud;
			}

			vars.set(AD_USUCLD, vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld);

		}

		if (!booMigracao) {
			sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS,
					sqlNumOS(numOS) + sqlCodPerg("1579,4571") + " AND RPE.CODRESP = 2 ");
			if (sql.next()) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(75));
				vlrMinMensalidade = vlrMinMensalidade.add(BigDecimal.valueOf(75));
			}
			sql.reset();
		}

		sql.select("COUNT(*)", SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("4632,4633,4634")
				+ " AND RPE.CODRESP = 1 "
				+ " AND EXISTS (SELECT 1 FROM TPQDPD DPD, TPQRPE R2 WHERE R2.NUPESQ = POS.NUPESQ AND DPD.CODPERGDEP = RPE.CODPERG AND DPD.CODPERGMESTRE = R2.CODPERG AND R2.CODRESP = 29)");
		if (sql.next() && (sql.getInt(1) > 0)) {
			decVlrMen = decVlrMen.add(BigDecimal.valueOf(sql.getInt(1) * 32.46));

		}
		sql.reset();

		qtdHrBase = qtdHrBase.multiply(percIncidenciaImp).setScale(0, BigDecimal.ROUND_HALF_UP);

		decVlrEAD = BigDecimal.valueOf(200);

		if ((("ND".equals(vars.get(TPBANCO))) || (booFMC)) && !booJivaBox) {
			decVlrBD = BigDecimal.ZERO;
		} else {
			decVlrBD = decVlrImpBD;
			decVlrBD = BigDecimalUtil.getRounded(decVlrBD.multiply(decQtdBD), 2);

			if (("OF".equals(vars.get(TPBANCO)) || "SF".equals(vars.get(TPBANCO))) && maiorZero(decVlrBD)) {
				decVlrBD = BigDecimalUtil.getRounded(decVlrBD.multiply(BigDecimal.valueOf(0.5)), 2);
			}
		}

		if (!possuiAdicional && booInstJav) {
			decVlrJAV = decVlrInsJAV;
		}

		if (perguntaRespondidaSim("4539", numOS)) {
			decVlrLU = BigDecimalUtil.getRounded(decVlrLU.multiply(BigDecimal.valueOf(1.35)), 2);

			decVlrMen = BigDecimalUtil.getRounded(decVlrMen.multiply(BigDecimal.valueOf(1.35)), 2);
			qtdHrBase = BigDecimal.ZERO;
		}

		if (perguntaRespondidaSim("4540", numOS)) {
			decVlrMen = BigDecimalUtil.getRounded(decVlrMen.multiply(BigDecimal.valueOf(1.30)), 2);
		}

		StringBuilder strCodContrato = new StringBuilder();
		if (possuiAdicional) {
			produtos.goToStart();
			while (produtos.next()) {
				strCodContrato.append((strCodContrato.length() > 0 ? "," : "") + produtos.asInt(CODPROD));
			}

			sql.select("DISTINCT PSC.CODPROD", "TCSOSE OSE, TCSPSC PSC, TCSCON CON, TCSPAP PAP",
					"OSE.CODPAP = PAP.CODPAP AND CON.CODPARC = PAP.CODPARC AND PSC.NUMCONTRATO = CON.NUMCONTRATO AND PSC.SITPROD <> 'C' AND NUMOS = "
							+ numOS);
			while (sql.next()) {
				strCodContrato.append((strCodContrato.length() > 0 ? "," : "") + sql.getString(1));
			}
			sql.reset();

			if (strCodContrato.length() > 0) {
				sql.select("NVL(CEIL(SUM(AD_MINUTOSHAB)/60),0)",
						"(SELECT DISTINCT RES.CODRESP, RES.AD_MINUTOSHAB FROM TPQRES RES "
								+ " WHERE RES.AD_MINUTOSHAB > 0 " + " AND ( RES.CODPROD IN ("
								+ strCodContrato.toString() + ") OR RES.AD_PRODWEB IN (" + strCodContrato.toString()
								+ ") ))",
						"");
				if (sql.next()) {
					qtdHrBase = qtdHrBase.add(vars.asBigDecimal(QTDHABI).multiply(sql.getBigDecimal(1)));
				}
			}
		} else {
			sql.select("NVL(CEIL(SUM(RES.AD_MINUTOSHAB)/60),0)", SQL_TPQRPE_TCSPOS_TPQRES, sqlNumOS(numOS)
					+ SQL_LIGACAO_RES_RPE + " AND RES.AD_MINUTOSHAB > 0"
					+ " and NOT EXISTS (SELECT 1 FROM TPQRPE R2, TPQRES RES2, TPQDPD DPD"
					+ "                 WHERE R2.NUPESQ = POS.NUPESQ" + "                 AND R2.CODPERG = RES2.CODPERG"
					+ "                 AND R2.CODRESP = RES2.CODRESP" + "                 AND RES2.DESCRRESP = 'No'"
					+ "                 AND R2.CODPERG = DPD.CODPERGMESTRE"
					+ "                 AND DPD.CODPERGDEP = RPE.CODPERG)");
			if (sql.next()) {
				qtdHrBase = qtdHrBase.add(vars.asBigDecimal(QTDHABI).multiply(sql.getBigDecimal(1)));
			}
			sql.reset();
		}

		sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("4918") + " AND RPE.CODRESP = 21 ");
		if (sql.next()) {
			qtdHrBase = qtdHrBase.add(BigDecimal.valueOf(6));
		}
		sql.reset();

		vars.set("QTDHRTOT", qtdHrBase.intValue());
		vars.set(QTDHRNEG, qtdHrBase.intValue());

		decVlrImp = BigDecimalUtil.getRounded(qtdHrBase.multiply(decVlrHora), 2);

		decVlrLU = decVlrLU
				.add(BigDecimal.valueOf(qtdUsuCheckout).multiply(getPrecoProduto(BigDecimal.valueOf(30748))));
		decVlrMen = decVlrMen.add(BigDecimal.valueOf(qtdUsuCheckout)
				.multiply(getPrecoProduto(BigDecimal.valueOf(30748))).multiply(CINCOPORCENTO));

		sql.reset();

		if (booFMC) {
			decVlrFMC = BigDecimal.valueOf(1350);
		}
		int qtdDC = 0;
		if (possuiAdicional) {
			sql.select("TEXTO", SQL_TPQRPE_TCSPOS_TPQPER, sqlNumOS(numOS) + " AND RPE.CODPERG = 4919");
			if (sql.next() && !"".equals(sql.getString(1))) {
				try {
					qtdDC = sql.getInt(1);
					booFMC = true;
				} catch (Exception e) {
					qtdDC = 0;
				}
			}
			sql.reset();
		} else if (booFMC) {
			qtdDC = vars.asInt(USUFULL) + vars.asInt(USULITE);
		} else if (booMigracao) {
			sql.select("NVL(PSC.NUMUSUARIOS,0)", "TCSOSE OSE, TCSPAP PAP, TCSCON CON, TCSPSC PSC",
					"PAP.CODPAP = OSE.CODPAP AND CON.CODPARC = PAP.CODPARC AND PSC.NUMCONTRATO = CON.NUMCONTRATO AND PSC.CODPROD = 20469 AND SITPROD <> 'C' AND OSE.NUMOS = "
							+ numOS);
			if (sql.next() && (sql.getInt(1) > 0)) {
				qtdDC = vars.asInt(USUFULL) + vars.asInt(USULITE);
				if (qtdDC > 0) {
					qtdDC = qtdDC + sql.getInt(1);
				}
			}
			sql.reset();
		}

		if (qtdDC > 0) {
			if (qtdDC < 6) {
				decVlrMensFMC = BigDecimal.valueOf(qtdDC * 73.58);
			} else if (qtdDC < 11) {
				decVlrMensFMC = BigDecimal.valueOf(qtdDC * 70.12);
			} else if (qtdDC < 16) {
				decVlrMensFMC = BigDecimal.valueOf(qtdDC * 66.66);
			} else if (qtdDC < 21) {
				decVlrMensFMC = BigDecimal.valueOf(qtdDC * 64.06);
			} else {
				decVlrMensFMC = BigDecimal.valueOf(qtdDC * 60.60);
			}
		}
	}

	private boolean possuiCloud(BigDecimal numOS, int codProd) throws Exception {
		boolean possui = false;

		String hql = "(SELECT 'S' AS TEM " + " FROM TCSOSE OSE " + " INNER JOIN TCSPAP PAP ON PAP.CODPAP = OSE.CODPAP "
				+ " INNER JOIN TCSCON CON ON PAP.CODPARC = CON.CODPARC "
				+ " INNER JOIN TCSPSC PSC ON  CON.NUMCONTRATO = PSC.NUMCONTRATO " + " WHERE PSC.CODPROD = " + codProd
				+ " AND PSC.SITPROD IN ('A','B') " + " AND OSE.NUMOS = " + numOS + ") T";

		sql.select("T.TEM", hql, " 1 = 1 ");
		if (sql.next() && !"".equals(sql.getString(1))) {
			possui = true;
		}
		sql.reset();
		return possui;
	}

	private BigDecimal getVlrContratoBase(BigDecimal numOS) throws Exception {

		BigDecimal vlr = BigDecimal.ZERO;
		String hql = " TCSOSE OSE " + " INNER JOIN TCSPAP PAP ON PAP.CODPAP = OSE.CODPAP "
				+ " INNER JOIN CND_CLIENTE CON ON CON.CODPARC = PAP.CODPARC ";

		sql.select("CON.VLR_ATV", hql, "OSE.NUMOS = " + numOS);

		if (sql.next() && !"".equals(sql.getString(1))) {
			vlr = sql.getBigDecimal(1);
		}
		sql.reset();

		return vlr;
	}

	private BigDecimal getVlrMenPorUsu(int codProd, int qtdUsu, boolean temEcommerce, boolean multiplica)
			throws Exception {
		BigDecimal vlr = BigDecimal.ZERO;
		sql.select("(T.VLR * " + ((multiplica) ? qtdUsu : 1) + ")" + ((temEcommerce) ? " + 150 " : ""),
				"(SELECT TPP.CODPROD, NVL((SELECT MAX(QTD) + 1 FROM AD_TCSTPP WHERE CODPROD = TPP.CODPROD AND QTD < TPP.QTD), 1) AS MIN, TPP.QTD AS MAX, TPP.VLR FROM AD_TCSTPP TPP) T",
				"T.MIN <= " + qtdUsu + " AND T.MAX >= " + qtdUsu + " AND T.CODPROD = " + codProd);
		if (sql.next() && !"".equals(sql.getString(1))) {
			vlr = sql.getBigDecimal(1);
		}
		sql.reset();
		return vlr;
	}

	private void validarLojon(BigDecimal numOS) throws Exception {
		validarPreenchimento(numOS, false);

		vars.set(QTDEAD, 0);
		vars.set(QTDHABI, 0);
		vars.set(MATRIZES, 1);
		vars.set(FILIAIS, 0);
		vars.set(USULITE, 0);

		percIncidenciaGP = BigDecimal.valueOf(0.12);

		strVWprodutos = "(SELECT Q1.CODPROD, SUM(Q1.TEMPO) AS TEMPO, SUM(Q1.QTDE) AS QTDE " + "FROM ( "
				+ "SELECT RES.CODPROD, '1' QTDE , NVL(RES.AD_TEMPO, 0) AS TEMPO " + "FROM TPQRPE RPE "
				+ "INNER JOIN TPQRES RES ON " + SQL_LIGACAO_ON_RES_RPE
				+ "INNER JOIN TCSPOS POS ON (POS.NUPESQ = RPE.NUPESQ AND POS.NUMOS = " + numOS
				+ " AND POS.APLICAVEL = 'S' AND POS.NUMITEM = 0) " + "WHERE ( RES.CODPROD IS NOT NULL ) "
				+ ") Q1 GROUP BY Q1.CODPROD " + ") T ";

		nsql.appendSql("SELECT DISTINCT RES.CODPROD ");
		nsql.appendSql("FROM " + SQL_TPQRPE_TCSPOS_TPQRES);
		nsql.appendSql("WHERE POS.NUPESQ = RPE.NUPESQ AND POS.NUMOS =  " + numOS
				+ " AND POS.APLICAVEL = 'S' AND POS.NUMITEM = 0 ");
		nsql.appendSql(SQL_LIGACAO_RES_RPE);
		nsql.appendSql("AND ( RES.CODPROD IS NOT NULL ) ");
		nsql.appendSql("AND RES.AD_TEMPO > 0 ");
		nsql.appendSql("AND RES.CODPROD = :CODPROD ");

		produtos.goToStart();
		while (produtos.next()) {
			if (!possuiProd.contains(produtos.asInt(CODPROD))) {
				nsql.setNamedParameter(CODPROD, produtos.get(CODPROD));
				ResultSet rset = nsql.executeQuery();
				if (rset.next()) {
					possuiProd.add(produtos.asInt(CODPROD));
				} else {
					produtos.removeRow();
					produtos.currentIndex--;
				}
				rset.close();
			}
		}
	}

	private void calcularLojon(BigDecimal numOS) throws Exception {
		int escopoEcomm = 0;
		sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("4748"));
		if (sql.next() && !"".equals(sql.getString(1))) {
			escopoEcomm = sql.getInt(1);
		}
		sql.reset();

		if (intPorte == 2 && escopoEcomm != 4) {
			erro("Quando o <b>Perfil do cliente</b> no for integrado com ERP Sankhya ou Jiva  necessrio preencher o <b>escopo detalhado do projeto</b>!");
		}

		switch (escopoEcomm) {
		case 1:
			decVlrLU = BigDecimal.valueOf(2600);

			decVlrMen = BigDecimal.valueOf(150);
			break;
		case 2:
			decVlrLU = BigDecimal.valueOf(2800);

			decVlrMen = BigDecimal.valueOf(80);
			break;
		default:
			decVlrLU = BigDecimal.valueOf(3300);

			decVlrMen = BigDecimal.valueOf(100);
			break;
		}

		vlrMinMensalidade = BigDecimal.valueOf(0);
		boolean contratouEcommerce = false;
		boolean contratouVitrine = false;
		if (possuiProd.contains(30518) || possuiProd.contains(30501) || possuiProd.contains(30502)
				|| possuiProd.contains(30503) || possuiProd.contains(30504) || possuiProd.contains(30505)) {
			contratouEcommerce = true;
		} else if (possuiProd.contains(30517) || possuiProd.contains(30512) || possuiProd.contains(30513)
				|| possuiProd.contains(30514) || possuiProd.contains(30515) || possuiProd.contains(30516)) {
			contratouVitrine = true;
		}

		boolean contratouIntegracao = possuiProd.contains(30543);
		boolean contratouAtacado = possuiProd.contains(30569);
		boolean contratouBlog = possuiProd.contains(30545);
		boolean contratouML = possuiProd.contains(30530);
		boolean contratouB2W = possuiProd.contains(30571);
		boolean contratouListaP = possuiProd.contains(30560);
		boolean contratouServNuvem = possuiProd.contains(30561);
		boolean contratouSIGEP = possuiProd.contains(30562);

		int qtdProdutos = 0;
		sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("3415"));
		if (sql.next() && !"".equals(sql.getString(1))) {
			try {
				qtdProdutos = sql.getInt(1);
			} catch (Exception e) {
				erro("Informe a <b>Quantidade de produtos</b>" + CORRETAMENTE + msgNaoInteiro(sql.getString(1)));
			}
		}
		sql.reset();

		if (qtdProdutos > 0) {
			if (qtdProdutos <= 500) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(250));
			} else if (qtdProdutos <= 1000) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(310));
			} else if (qtdProdutos <= 3000) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(370));
			} else if (qtdProdutos <= 5000) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(440));
			} else if (qtdProdutos <= 10000) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(500));
			} else if (qtdProdutos <= 20000) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(650));
			} else if (qtdProdutos <= 30000) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(850));
			} else if (qtdProdutos <= 50000) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(1000));
			} else {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(1500));
			}
		}

		int qtdTabPreco = 0;
		sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("4737"));
		if (sql.next() && !"".equals(sql.getString(1))) {
			try {
				qtdTabPreco = sql.getInt(1);
			} catch (Exception e) {
				erro("Informe a <b>Quantidade de tabelas de preos</b>" + CORRETAMENTE
						+ msgNaoInteiro(sql.getString(1)));
			}
		}
		sql.reset();

		if (qtdTabPreco > 3) {

			qtdHrBase = qtdHrBase
					.add(BigDecimal.valueOf((qtdTabPreco - 3) * 0.5).setScale(0, BigDecimal.ROUND_HALF_UP));
		}
		decVlrMen = decVlrMen.add(decVlrLU.multiply(BigDecimal.valueOf(0.04)));
		if (contratouIntegracao && escopoEcomm == 4) {
			qtdHrBase = qtdHrBase.add(BigDecimal.valueOf(8));
			if (contratouEcommerce) {
				decVlrMen = decVlrMen.multiply(BigDecimal.valueOf(1.40));
			} else if (contratouVitrine) {
				decVlrMen = decVlrMen.multiply(BigDecimal.valueOf(1.30));
			}
		}
		if (contratouAtacado) {
			decVlrMen = decVlrMen.multiply(BigDecimal.valueOf(1.20));
		}
		if (contratouBlog) {
			decVlrMen = decVlrMen.multiply(BigDecimal.valueOf(1.05));
		}
		if (contratouML) {
			decVlrMen = decVlrMen.add(BigDecimal.valueOf(250 + (qtdProdutos * 0.03)));
		}
		if (contratouB2W) {
			decVlrMen = decVlrMen.add(BigDecimal.valueOf(250 + (qtdProdutos * 0.03)));
		}
		if (contratouListaP) {
			decVlrMen = decVlrMen.add(BigDecimal.valueOf(90 + (qtdProdutos * 0.01)));
		}
		if (contratouServNuvem) {
			decVlrMen = decVlrMen.add(BigDecimal.valueOf(120 + (qtdProdutos * 0.01)));
		}
		if (contratouSIGEP) {
			decVlrMen = decVlrMen.add(BigDecimal.valueOf(80 + (qtdProdutos * 0.02)));
		}

		BigDecimal decQtdDep = BigDecimal.ZERO;
		if (decQtdDep.doubleValue() > 1) {
			decVlrMen = decVlrMen.multiply(BigDecimal.valueOf(1 + ((4 * decQtdDep.doubleValue()) / 100)));
		}

		int qtdParcVend = 0;
		if (escopoEcomm == 4) {
			sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS,
					sqlNumOS(numOS) + sqlCodPerg("3458") + " AND RPE.CODRESP = 3 ");
			if (sql.next()) {
				sql.reset();
				sql.select(SQL_RPE_TEXTO, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("4767"));
				if (sql.next() && !"".equals(sql.getString(1))) {
					try {
						qtdParcVend = sql.getInt(1);
					} catch (Exception e) {
						erro("Informe a <b>Quantidade de parceiros/vendedores</b>" + CORRETAMENTE
								+ msgNaoInteiro(sql.getString(1)));
					}
				}
			}
			sql.reset();
			if (qtdParcVend > 0) {
				decVlrMen = decVlrMen.add(BigDecimal.valueOf(qtdParcVend * 30f));
			}
		}

		vars.set("QTDHRTOT", qtdHrBase.intValue());
		vars.set(QTDHRNEG, qtdHrBase.intValue());

		booLibAditivo = true;
		boolean booTemVolumetria = false;

		sql.select("COUNT(1)", "TCSSPN", "UPPER(ADITIVO) LIKE '%VOLUMETRIA%' AND NUMOS = " + numOS);
		if (sql.next() && sql.getInt(1) > 0) {
			booTemVolumetria = true;
		} else {
			vlrMinMensalidade = vlrMinMensalidade.add(BigDecimal.valueOf(300));
			if (contratouIntegracao) {
				vlrMinMensalidade = vlrMinMensalidade.add(BigDecimal.valueOf(200));
			}
			if (contratouAtacado) {
				vlrMinMensalidade = vlrMinMensalidade.add(BigDecimal.valueOf(60));
			}
			if (escopoEcomm == 3) {
				vlrMinMensalidade = vlrMinMensalidade.add(BigDecimal.valueOf(50));
			} else if (escopoEcomm == 4 && qtdParcVend > 0) {
				vlrMinMensalidade = vlrMinMensalidade.add(BigDecimal.valueOf(qtdParcVend * 15f));
			}
			if (contratouML) {
				vlrMinMensalidade = vlrMinMensalidade.add(BigDecimal.valueOf(250));
			}
			if (contratouB2W) {
				vlrMinMensalidade = vlrMinMensalidade.add(BigDecimal.valueOf(250));
			}
			if (contratouListaP) {
				vlrMinMensalidade = vlrMinMensalidade.add(BigDecimal.valueOf(50));
			}
			if (contratouServNuvem) {
				vlrMinMensalidade = vlrMinMensalidade.add(BigDecimal.valueOf(100));
			}
			if (contratouSIGEP) {
				vlrMinMensalidade = vlrMinMensalidade.add(BigDecimal.valueOf(60));
			}
		}

		switch (intPorte) {
		case 1:
			decVlrLU = decVlrLU.multiply(BigDecimal.valueOf(1.10));

			decVlrMen = decVlrMen.multiply(BigDecimal.valueOf(1.10));
			qtdHrBase = qtdHrBase.multiply(BigDecimal.valueOf(1.10));
			vlrMinMensalidade = vlrMinMensalidade.multiply(BigDecimal.valueOf(1.05));
			break;
		case 2:
			decVlrLU = decVlrLU.multiply(BigDecimal.valueOf(1.40));

			decVlrMen = decVlrMen.multiply(BigDecimal.valueOf(1.40));
			qtdHrBase = qtdHrBase.multiply(BigDecimal.valueOf(1.40));
			vlrMinMensalidade = vlrMinMensalidade.multiply(BigDecimal.valueOf(1.25));
			break;
		}

		if (booTemVolumetria) {
			vlrMinMensalidade = BigDecimal.valueOf(500);
		}

		decVlrImp = BigDecimalUtil.getRounded(qtdHrBase.multiply(decVlrHora), 2);
		decVlrLU = decVlrLU.add(decVlrImp);

	}

	private void validarPacks(BigDecimal numOS) throws Exception {
		sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("5514"));
		if (sql.next()) {
			try {
				vars.set("TPNEGIMP", (sql.getInt(1) == 1) ? "E" : ((sql.getInt(1) == 2) ? "M" : "B"));
			} catch (Exception e) {
				erro("Informe o <b>Tipo de Neg. na Implantao</b> no diagnstico!");
			}
		} else if (!possuiFuncional && !booDiagUsuario && !possuiHora && !possuiMensDBA && !possuiSnkExpress) {
			erro(msgNecessarioInformar("Tipo de Neg. na Implantao"));
		}
		sql.reset();

		sql.nativeSelect(
				"SELECT RES.DESCRRESP as MATRIZES, (NVL(AD_PERCINCIDENCIALU,0)/100) as PERCINCIDENCIALU, (NVL(AD_PERCINCIDENCIAIMP,0)/100) as PERCINCIDENCIAIMP"
						+ "	FROM " + "	TCSPOS POS " + "	INNER JOIN TPQRPE RPE ON POS.NUPESQ = RPE.NUPESQ "
						+ "	INNER JOIN TPQPER PER ON PER.CODPERG = RPE.CODPERG "
						+ "	INNER JOIN TPQRES RES ON RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP "
						+ "	WHERE POS.NUMOS =" + numOS + "	AND POS.APLICAVEL = 'S' " + "	AND POS.NUMITEM = 0 "
						+ "	AND PER.AD_CONTABILIZA = ('MT') " + SQL_PAI3_RESP);
		String ssl = "SELECT RES.DESCRRESP as MATRIZES, (NVL(AD_PERCINCIDENCIALU,0)/100) as PERCINCIDENCIALU, (NVL(AD_PERCINCIDENCIAIMP,0)/100) as PERCINCIDENCIAIMP"
				+ "	FROM " + "	TCSPOS POS " + "	INNER JOIN TPQRPE RPE ON POS.NUPESQ = RPE.NUPESQ "
				+ "	INNER JOIN TPQPER PER ON PER.CODPERG = RPE.CODPERG "
				+ "	INNER JOIN TPQRES RES ON RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP "
				+ "	WHERE POS.NUMOS =" + numOS + "	AND POS.APLICAVEL = 'S' " + "	AND POS.NUMITEM = 0 "
				+ "	AND PER.AD_CONTABILIZA = ('MT') " + SQL_PAI3_RESP;

		if (sql.next() && !"".equals(sql.getString("MATRIZES"))) {

			if(!sql.getString("MATRIZES").equals("Venda na Base")) {
				vars.set(MATRIZES, sql.getInt("MATRIZES"));

				if (vars.asInt(MATRIZES) > 1) {
					percIncidenciaLU = percIncidenciaLU.add(
							sql.getBigDecimal("PERCINCIDENCIALU").multiply(BigDecimal.valueOf((vars.asInt(MATRIZES) - 1))));
					percIncidenciaImp = percIncidenciaImp.add(sql.getBigDecimal("PERCINCIDENCIAIMP")
							.multiply(BigDecimal.valueOf((vars.asInt(MATRIZES) - 1))));
				}

			}

		}

		impMatriz = 0;
		sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("5519"));
		if (sql.next()) {
			impMatriz = sql.getInt(1);
		}

		sql.nativeSelect(
				"SELECT RES.DESCRRESP as FILIAIS, (NVL(AD_PERCINCIDENCIALU,0)/100) as PERCINCIDENCIALU, (NVL(AD_PERCINCIDENCIAIMP,0)/100) as PERCINCIDENCIAIMP"
						+ " FROM " + " TCSPOS POS " + " INNER JOIN TPQRPE RPE ON POS.NUPESQ = RPE.NUPESQ "
						+ " INNER JOIN TPQPER PER ON PER.CODPERG = RPE.CODPERG "
						+ " INNER JOIN TPQRES RES ON RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP "
						+ " WHERE POS.NUMOS =" + numOS + " AND POS.APLICAVEL = 'S' " + " AND POS.NUMITEM = 0 "
						+ " AND PER.AD_CONTABILIZA = ('FIL') " + SQL_PAI3_RESP);
		if (sql.next() && !"".equals(sql.getString("FILIAIS"))) {
			vars.set(FILIAIS, sql.getInt("FILIAIS"));

			if (vars.asInt(FILIAIS) > 0 && impMatriz == 1) {
				percIncidenciaLU = percIncidenciaLU
						.add(sql.getBigDecimal("PERCINCIDENCIALU").multiply(BigDecimal.valueOf(vars.asInt(FILIAIS))));
				percIncidenciaImp = percIncidenciaImp
						.add(sql.getBigDecimal("PERCINCIDENCIAIMP").multiply(BigDecimal.valueOf(vars.asInt(FILIAIS))));
			}
		}

		sql.nativeSelect(
				"SELECT RES.DESCRRESP as SEGMENTOS, (NVL(AD_PERCINCIDENCIALU,0)/100) as PERCINCIDENCIALU, (NVL(AD_PERCINCIDENCIAIMP,0)/100) as PERCINCIDENCIAIMP"
						+ "	FROM " + "	TCSPOS POS " + "	INNER JOIN TPQRPE RPE ON POS.NUPESQ = RPE.NUPESQ "
						+ "	INNER JOIN TPQPER PER ON PER.CODPERG = RPE.CODPERG "
						+ "	INNER JOIN TPQRES RES ON RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP "
						+ "	WHERE POS.NUMOS =" + numOS + "	AND POS.APLICAVEL = 'S' " + "	AND POS.NUMITEM = 0 "
						+ "	AND PER.AD_CONTABILIZA = ('SG') " + SQL_PAI3_RESP);
		if (sql.next() && !"".equals(sql.getString("SEGMENTOS"))) {
			vars.set("SEGMENTOS", sql.getInt("SEGMENTOS"));

			if (vars.asDouble("SEGMENTOS") > 1) {
				percIncidenciaImp = percIncidenciaImp.add(sql.getBigDecimal("PERCINCIDENCIAIMP")
						.multiply(BigDecimal.valueOf((vars.asInt("SEGMENTOS") - 1))));

			}
		}

		sql.nativeSelect("SELECT (NVL(AD_PERCINCIDENCIAGP,0)/100) as PERCINCIDENCIAGP" + "	FROM " + "	TCSPOS POS "
				+ "	INNER JOIN TPQRPE RPE ON POS.NUPESQ = RPE.NUPESQ "
				+ "	INNER JOIN TPQPER PER ON PER.CODPERG = RPE.CODPERG "
				+ "	INNER JOIN TPQRES RES ON RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP "
				+ "	WHERE POS.NUMOS =" + numOS + "	AND POS.APLICAVEL = 'S' " + "	AND POS.NUMITEM = 0 "
				+ "	AND PER.AD_PERCINCIDENCIAGP IS NOT NULL " + SQL_PAI3_RESP);
		if (sql.next()) {
			percIncidenciaGP = sql.getBigDecimal("PERCINCIDENCIAGP");
		}
		sql.reset();

		String strSelPerg = getSelPerg(numOS);
		String strPerg;
		int tipoVenda = tipoVenda(numOS);
		if (tipoVenda == 1) {
			strPerg = "421";
		} else {
			strPerg = "420";
		}

		if (booVendaBaseNP) {
			strPerg = " 420, " + strPerg;
		}

		strVWprodutos = "(SELECT Q1.CODPROD, SUM(Q1.TEMPO) AS TEMPO, SUM(Q1.QTDE) AS QTDE, SUM(Q1.PRECO) AS PRECO, SUM(Q1.VLRMENSALESPECIAL) AS VLRMENSALESPECIAL "
				+ " FROM ( "
				+ "SELECT PROD.CODPROD, CEIL((NVL(RES.AD_MINUTOS,0)/60)/(select count (*) from AD_TCSPROCPRO P1 WHERE  PROD.NUPROC = P1.NUPROC)) AS TEMPO, NVL(RES.AD_QTDUSU,0) AS QTDE, NVL(RES.AD_JIVA,0)/(select count (*) from AD_TCSPROCPRO P1 WHERE  PROD.NUPROC = P1.NUPROC) AS PRECO "
				+ " , (nvl(RES.AD_MENSAL,0)) AS VLRMENSALESPECIAL" + " FROM  TCSPOS POS "
				+ " INNER JOIN TPQRPE RPE ON RPE.NUPESQ = POS.NUPESQ "
				+ " INNER JOIN TPQPER PER ON PER.CODPERG = RPE.CODPERG"
				+ " INNER JOIN TPQRES RES ON RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP"
				+ " INNER JOIN AD_TCSPROCPRO PROD ON PROD.NUPROC = RPE.CODPERG " + " WHERE POS.NUMOS =" + numOS
				+ "	AND PER.CODQUEST in (" + strPerg + ",422)" + " AND POS.APLICAVEL = 'S' "
				+ " AND RES.AD_JIVA IS NOT NULL " + " AND POS.NUMITEM = 0 " + SQL_PAI3_RESP
				+ ") Q1 GROUP BY Q1.CODPROD " + ") T ";

		int vendaNFSE = vendaNFSE(numOS);
		if (vendaNFSE == 1) {

			String msgErroNFSe = "A cidade do Prospect no est homologada para <b>NFS-e</b> (<b>Nota Fiscal Eletronica de Servios</b>)!<br />Retire este produto do diagnstico!";
			String sqlNFSe;
			sqlNFSe = "(SELECT COUNT(1) AS TOTAL FROM TCSOSE OSE INNER JOIN TCSPAP PAP ON PAP.CODPAP = OSE.CODPAP"
					+ " INNER JOIN TSICID CID ON CID.CODCID = PAP.CODCID" + " WHERE OSE.NUMOS = " + numOS
					+ " AND CID.AD_NFSE = 'S') T";

			sql.select("T.TOTAL", sqlNFSe, "1=1");

			if (sql.next()) {
				if (sql.getInt(1) == 0) {
					erro(msgErroNFSe);
				}

			}

			sql.reset();
		}

	}

	private void calcularPacks(BigDecimal numOS) throws Exception {

		String strPerg;
		int tipoVenda = tipoVenda(numOS);
		if (tipoVenda == 1) {
			strPerg = "421";
		} else {
			strPerg = "420";
		}

		sql.nativeSelect(
				"SELECT (NVL(AD_PERCMEN,0)/100) as PERCINCIDENCIAMENS, (NVL(AD_PERCMENUSU,0)/100) as PERCMENUSU"
						+ "	FROM " + "	TCSPOS POS " + "	INNER JOIN TCSFLD FLD ON FLD.CODFLD = POS.CODFLD "
						+ "	WHERE POS.NUMOS =" + numOS);
		if (sql.next()) {
			if (tipoVenda == 1) {
				percIncidenciaMens = sql.getBigDecimal("PERCMENUSU");
			} else {
				percIncidenciaMens = sql.getBigDecimal("PERCINCIDENCIAMENS");
			}

		}
		sql.reset();

		if (perguntaRespondidaSim("5387", numOS)) {
			if (perguntaRespondidaSim("5388", numOS)) {
				sql.select("NVL(RPE.TEXTO,0) AS CODRESP ", SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("5389"));
				if (sql.next()) {
					qtdHrPers = qtdHrPers.add(sql.getBigDecimal(1));
				}
				sql.reset();
			}

			if (perguntaRespondidaSim("5391", numOS)) {
				sql.select("NVL(RPE.TEXTO,0) AS CODRESP ", SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("5393"));
				if (sql.next()) {
					qtdHrPers = qtdHrPers.add(sql.getBigDecimal(1));
				}
				sql.reset();
			}

			if (perguntaRespondidaSim("5396", numOS)) {
				sql.select("NVL(RPE.TEXTO,0) AS CODRESP ", SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("5397"));
				if (sql.next()) {
					qtdHrPers = qtdHrPers.add(sql.getBigDecimal(1));
				}
				sql.reset();
			}
		}

		if (perguntaRespondidaSim("5513", numOS)) {
			if (perguntaRespondidaSim("5516", numOS)) {
				sql.select("NVL(RPE.TEXTO,0) AS CODRESP ", SQL_TPQRPE_TCSPOS, sqlNumOS(numOS) + sqlCodPerg("5517"));
				if (sql.next()) {
					qtdHrPers = qtdHrPers.add(sql.getBigDecimal(1));
				}
				sql.reset();
			}
		}

		sql.nativeSelect("SELECT Nvl(Ceil(Sum(Nvl(ad_minutos, 0)) / 60),0) AS QTDHORAS " + "	FROM " + "	TCSPOS POS "
				+ "	INNER JOIN TPQRPE RPE ON POS.NUPESQ = RPE.NUPESQ "
				+ "	INNER JOIN TPQRES RES ON RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP "
				+ "	WHERE POS.NUMOS =" + numOS + "	AND POS.APLICAVEL = 'S' " + "	AND POS.NUMITEM = 0 "
				+ " AND RES.AD_JIVA IS NOT NULL AND RES.AD_MINUTOS IS NOT NULL " + SQL_PAI3_RESP);
		if (sql.next()) {
			qtdHrBase = (qtdHrPers.add(sql.getBigDecimal("QTDHORAS")).multiply(percIncidenciaImp).setScale(0,
					BigDecimal.ROUND_HALF_UP));
			vars.set("QTDHRTOT", qtdHrBase.intValue());
			vars.set(QTDHRNEG, qtdHrBase.intValue());
		}
		sql.reset();

		decSomaPrecos = BigDecimal.ZERO;
		produtos.goToStart();
		while (produtos.next()) {
			decSomaPrecos.add(produtos.asBigDecimal(VLRTOT));
		}

		int codProd = 0;
		if (booSnkCloud || codProd == 20510) {

			produtos.append();
			produtos.set(CODPROD, 20510);
			produtos.set("LINHA", "WEB");
			produtos.set("BONIFICADO", "N");
			produtos.set("CODVEND", codVendedor);
			produtos.set(VLRTOT, getPrecoProduto(new BigDecimal(20510)));
			produtos.set("AD_CERTHAB", "N");
			produtos.set(HRBASE, BigDecimal.ZERO);
			produtos.set(HRIMP, BigDecimal.ZERO);
			produtos.set("QTDE", 1);
			decVlrSnkCloud = this.getPrecoProduto(new BigDecimal(20510));
		}

		sql.nativeSelect(
				"SELECT COUNT(*)" + "	FROM 	TCSPOS POS " + "	INNER JOIN TPQRPE RPE ON POS.NUPESQ = RPE.NUPESQ "
						+ " INNER JOIN TPQPER PER ON PER.CODPERG = RPE.CODPERG"
						+ "	INNER JOIN TPQRES RES ON RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP "
						+ "	WHERE POS.NUMOS =" + numOS + "	AND POS.APLICAVEL = 'S' " + "	AND POS.NUMITEM = 0 "
						+ " AND nvl(PER.AD_CLOUD,'SIM') = 'NAO'" + " AND RES.DESCRRESP = 'Sim'"
						+ "	AND PER.CODQUEST in (420,422)" + SQL_PAI3_RESP);
		if (sql.next() && !"".equals(sql.getString(1)) && (!booSnkCloud)) {
			if (sql.getInt(1) == 0) {
				msg("Esse prospect tem perfil para a venda Cloud e essa opo no foi selecionada.");
			}
		}

		sql.nativeSelect(
				"SELECT COUNT(*)" + "	FROM 	TCSPOS POS " + "	INNER JOIN TPQRPE RPE ON POS.NUPESQ = RPE.NUPESQ "
						+ " INNER JOIN TPQPER PER ON PER.CODPERG = RPE.CODPERG"
						+ "	INNER JOIN TPQRES RES ON RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP "
						+ "	WHERE POS.NUMOS =" + numOS + "	AND POS.APLICAVEL = 'S' " + "	AND POS.NUMITEM = 0 "
						+ " AND nvl(PER.AD_CLOUD,'SIM') = 'NAO'" + " AND RES.DESCRRESP = 'Sim'"
						+ "	AND PER.CODQUEST in (420,422)" + SQL_PAI3_RESP);
		if (sql.next() && !"".equals(sql.getString(1)) && (booSnkCloud)) {
			if (sql.getInt(1) != 0) {
				erro("Esse prospect NO tem perfil para a venda Cloud e essa opo foi selecionada. Favor desmarcar essa opo para prosseguir.");
			}
		}

		sql.nativeSelect("SELECT SUM(CASE WHEN PER.AD_CONTABILIZA = 'FL' THEN RES.AD_QTDUSU ELSE 0 END) as USUFULL"
				+ " , SUM(CASE WHEN PER.AD_CONTABILIZA = 'FT' THEN RES.AD_QTDUSU ELSE 0 END) as USULITE"
				+ "	FROM 	TCSPOS POS " + "	INNER JOIN TPQRPE RPE ON POS.NUPESQ = RPE.NUPESQ "
				+ " INNER JOIN TPQPER PER ON PER.CODPERG = RPE.CODPERG"
				+ "	INNER JOIN TPQRES RES ON RES.CODPERG = RPE.CODPERG AND RES.CODRESP = RPE.CODRESP "
				+ "	WHERE POS.NUMOS =" + numOS + "	AND POS.APLICAVEL = 'S' " + "	AND POS.NUMITEM = 0 "
				+ "	AND PER.CODQUEST in (" + strPerg + ",422)" + "	AND PER.AD_CONTABILIZA IN ('FL','FT') "
				+ SQL_PAI3_RESP);
		if (sql.next()) {
			vars.set(USUFULL, sql.getInt(1));
			vars.set(USULITE, sql.getInt(2));
		}
		sql.reset();

		decVlrLU = decVlrLU.add(decSomaPrecos);

		if (booSnkCloud) {

			int qtdUsu = vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld;

			sql.select("T.BONUS,T.VLRMINIMO, T.PERMDESC",
					"(SELECT TPP.CODPROD,NVL(TPP.BONUS,0) AS BONUS,NVL(TPP.VLRMINIMO,600) AS VLRMINIMO,NVL(TPP.PERMDESC,'N')AS PERMDESC, NVL((SELECT MAX(QTD) + 1 FROM AD_TCSTPP WHERE CODPROD = TPP.CODPROD AND QTD < TPP.QTD), 1) AS MIN, TPP.QTD AS MAX, TPP.VLR FROM AD_TCSTPP TPP) T",
					"T.MIN <= " + qtdUsu + " AND T.MAX >= " + qtdUsu + " AND T.CODPROD =20510");
			if (sql.next()) {
				decVlrBonusSnkCloud = sql.getBigDecimal("BONUS");
				decVlrMinimoSnkCloud = sql.getBigDecimal("VLRMINIMO");
				booPermDescCloud2 = sql.getString("PERMDESC");
			}
			sql.reset();

			if (possuiProd.contains(20510)) {
				decVlrSnkCloud = this.getPrecoProduto(new BigDecimal(20510));
			}

			boolean temEcommerce = false;
			if (!booPossuiCloud) {
				sql.select(SQL_RPE_CODRESP, SQL_TPQRPE_TCSPOS,
						sqlNumOS(numOS) + sqlCodPerg("5143,5543") + " AND RPE.CODRESP = 4 ");
				if (sql.next()) {
					temEcommerce = true;
				}
			}

			decVlrMensSnkCloud = getVlrMenPorUsu(20510, vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld,
					temEcommerce, true);

			if ((decVlrMensSnkCloud.doubleValue() > 0.00)
					&& (decVlrMensSnkCloud.doubleValue() < decVlrMinimoSnkCloud.doubleValue()) && (tipNeg == PADRAO)) {
				decVlrMensSnkCloud = decVlrMinimoSnkCloud;
			}

			vars.set(AD_USUCLD, vars.asInt(USUFULL) + vars.asInt(USULITE) + qtdcld - naoCld);
		}

		if ("ND".equals(vars.get(TPBANCO))) {
			decVlrBD = BigDecimal.ZERO;
		} else {
			decVlrBD = decVlrImpBD;
		}

		if (booInstJav) {
			decVlrJAV = decVlrInsJAV;
		}

		vars.set("QTDHRTOT", qtdHrBase.intValue());
		vars.set(QTDHRNEG, qtdHrBase.intValue());
		if(booVendaBaseNP) {
			vlrMinMensalidade = BigDecimal.ZERO;
		}else {
			vlrMinMensalidade = BigDecimal.valueOf(790);
		}

		if (!possuiMensDBA) {
			if (decVlrMen.doubleValue() < vlrMinMensalidade.doubleValue()) {
				decVlrMen = vlrMinMensalidade;
			}
		}

		decVlrImp = BigDecimalUtil.getRounded(qtdHrBase.multiply(decVlrHora), 2);

		decVlrLU = BigDecimalUtil.getRounded(decVlrLU.multiply(percIncidenciaLU), 2);

		if (booVendaSaas) {

			percIncidenciaMens = porcentFluxoSaas;
		}

		decVlrMen = BigDecimalUtil
				.getRounded(decVlrLU.subtract(vlrLuEspecial).multiply(percIncidenciaMens).add(vlrMensalEspecial), 2);

		NativeSql sqlValidacaoRespostasMensalidadeNP = new NativeSql(jdbc);

		sqlValidacaoRespostasMensalidadeNP.loadSql(this.getClass(),
				"ValidacaoNovoPortifolioHelper_queValorProdutosMensalidade.sql");

		sqlValidacaoRespostasMensalidadeNP.setNamedParameter("NUMOS", numOS);

		ResultSet result = sqlValidacaoRespostasMensalidadeNP.executeQuery();

		BigDecimal soma = new BigDecimal("0.0");
		soma.setScale(2, RoundingMode.HALF_UP);
		MathContext mctx = new MathContext(64, RoundingMode.HALF_UP);

		if (booVendaBaseNP) {
			while (result.next()) {

				BigDecimal valor = result.getBigDecimal("AD_MENSAL");
				soma = soma.add(valor, mctx).setScale(2, RoundingMode.HALF_UP);
			}

			decVlrMen = decVlrMen.add(soma, mctx).setScale(2, RoundingMode.HALF_UP);

		}

	}

}
