package br.com.sankhya.extensions.rateio.holding.facades;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.FinderException;

import org.jdom.Element;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.comercial.ComercialUtils;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.JdbcUtils;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import com.sankhya.util.XMLUtils;

public class RateioHoldingHelper {
	private EntityFacade									dwfEntityFacade;

	private BigDecimal										nuCnd;
	private BigDecimal										ordem;

	private Timestamp										dtPeriodoInicial;
	private Timestamp										dtPeriodoFinal;
	private Timestamp										dtNegociacao;
	private Timestamp										dtVencimento;
	private BigDecimal										codTipTitulo;
	private BigDecimal										codTipOperacao;
	private String											historico;
	private boolean											calcImpRetidos;

	private Map<CamposAgrupamentoFinanceiro, BigDecimal>	financeirosSintetico;
	private Map<CamposAgrupamentoFinanceiro, BigDecimal>	descontoTotalVag;

	public RateioHoldingHelper(EntityFacade dwfEntityFacade) {
		this.dwfEntityFacade = dwfEntityFacade;
	}

	public Element getSimulacaoRateioSinteticoDataProvider(JdbcWrapper jdbcWrapper) throws Exception {
		NativeSql queSimulaSintetico = new NativeSql(jdbcWrapper);
		queSimulaSintetico.loadSql(this.getClass(), "queSimulaSintetico.sql");

		queSimulaSintetico.removeSQLComment("$COM_DESCVAG$");
		queSimulaSintetico.removeSQLComment("$COM_DESCOMISJUR$");

		if (calcImpRetidos) {
			queSimulaSintetico.removeSQLComment("$COM_IMPOSTO$");
		} else {
			queSimulaSintetico.removeSQLComment("$SEM_IMPOSTO$");
		}

		queSimulaSintetico.removeSQLComment("$DESCONSIDERA_RATEADOS$");

		if (BigDecimalUtil.getValueOrZero(ordem).intValue() > 0) {
			queSimulaSintetico.removeSQLComment("$INCLUI_ORDEM$");
			queSimulaSintetico.setNamedParameter("ORDEM", BigDecimalUtil.getValueOrZero(ordem));
		}

		queSimulaSintetico.setNamedParameter("NUCND", BigDecimalUtil.getValueOrZero(nuCnd));
		queSimulaSintetico.setNamedParameter("DATINI", dtPeriodoInicial);
		queSimulaSintetico.setNamedParameter("DATFIM", dtPeriodoFinal);

		ResultSet rset = null;

		Element itensSimulacaoSintetica = new Element("itensSimulacaoSintetica");

		try {
			rset = queSimulaSintetico.executeQuery();

			while (rset.next()) {
				Element item = new Element("item");

				XMLUtils.setAttibuteValue(item, "CODNAT", rset.getBigDecimal("CODNAT"));
				XMLUtils.setAttibuteValue(item, "CODCENCUS", rset.getBigDecimal("CODCENCUS"));
				XMLUtils.setAttibuteValue(item, "CODPROJ", rset.getBigDecimal("CODPROJ"));
				XMLUtils.setAttibuteValue(item, "CODEMP", rset.getBigDecimal("CODEMP"));
				XMLUtils.setAttibuteValue(item, "CODPARC", rset.getBigDecimal("CODPARC"));
				XMLUtils.setAttibuteValue(item, "DESCRNAT", rset.getString("DESCRNAT"));
				XMLUtils.setAttibuteValue(item, "DESCRCENCUS", rset.getString("DESCRCENCUS"));
				XMLUtils.setAttibuteValue(item, "IDENTIFICACAO", rset.getString("IDENTIFICACAO"));
				XMLUtils.setAttibuteValue(item, "NOMEPARC", rset.getString("NOMEPARC"));
				XMLUtils.setAttibuteValue(item, "VALOR", rset.getBigDecimal("VALOR"));

				if (rset.getInt("RECDESP") == 1) {
					XMLUtils.setAttibuteValue(item, "RECDESP", "Receita");
				} else {
					XMLUtils.setAttibuteValue(item, "RECDESP", "Despesa");
				}

				itensSimulacaoSintetica.addContent(item);
			}
		} finally {
			JdbcUtils.closeResultSet(rset);
		}

		return itensSimulacaoSintetica;
	}

	public void gerarRateioSintetico(JdbcWrapper jdbcWrapper) throws Exception {
		if (BigDecimalUtil.getValueOrZero(codTipTitulo).intValue() == 0) {
			throw new Exception("O Tipo de Ttulo deve ser informado.");
		}

		validarPeriodo();

		long doisMesesAtras = TimeUtils.add(TimeUtils.getToday(), -60, Calendar.DAY_OF_MONTH);

		if (dtNegociacao.getTime() < doisMesesAtras) {
			throw new Exception("A Data de Negociao no pode ser anterior a 2 (dois) meses atrs.");
		}

		if (dtVencimento.getTime() < doisMesesAtras) {
			throw new Exception("A Data de Vencimento no pode ser anterior a 2 (dois) meses atrs.");
		}

		if (dtVencimento.getTime() < dtNegociacao.getTime()) {
			throw new Exception("Data de Vencimento deve ser maior ou igual a data de Negociao.");
		}

		financeirosSintetico = new HashMap<RateioHoldingHelper.CamposAgrupamentoFinanceiro, BigDecimal>();
		descontoTotalVag = new HashMap<RateioHoldingHelper.CamposAgrupamentoFinanceiro, BigDecimal>();

		BigDecimal ordemParam = BigDecimalUtil.getValueOrZero(ordem);

		Collection<Object> params = new ArrayList<Object>();

		StringBuffer filtroSimulacoes = new StringBuffer();

		if (ordem.intValue() != 0) {
			filtroSimulacoes.append(" (? = 0 OR this.ORDEM = ?) AND ");

			params.add(ordemParam);
			params.add(ordemParam);
		}

		filtroSimulacoes.append(" this.DHBAIXA BETWEEN ? AND ? ");
		filtroSimulacoes.append(" AND NOT EXISTS (SELECT 1 FROM TGFFRP FRP WHERE FRP.NUFINORIG = this.NUFIN) ");

		params.add(new Timestamp(TimeUtils.getDayStart(dtPeriodoInicial.getTime())));
		params.add(new Timestamp(TimeUtils.getDayEnd(dtPeriodoFinal.getTime())));

		FinderWrapper finderSimulacoes = new FinderWrapper("ViewRateioHoldingSimAnalitica", filtroSimulacoes.toString(), params.toArray());

		if (calcImpRetidos) {
			finderSimulacoes.setOrderBy("VALOR_COM_IMPOSTO desc");
		} else {
			finderSimulacoes.setOrderBy("VALOR_SEM_IMPOSTO desc");
		}

		Collection<?> simulacoes = dwfEntityFacade.findByDynamicFinderAsVO(finderSimulacoes);

		for (Iterator<?> ite = simulacoes.iterator(); ite.hasNext();) {
			DynamicVO simVO = (DynamicVO) ite.next();

			int multiplicadorRecDesp;

			if (simVO.asInt("RECDESPCONFIG") == 0 || simVO.asInt("RECDESPCONFIG") == simVO.asInt("RECDESP")) {
				multiplicadorRecDesp = 1;
			} else {
				multiplicadorRecDesp = -1;
			}

			DynamicVO repasseVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("Repasse");

			CamposAgrupamentoFinanceiro finRec = new CamposAgrupamentoFinanceiro();
			finRec.codEmp = simVO.asBigDecimal("CODEMPREC");
			finRec.codParc = simVO.asBigDecimal("CODPARCREC");
			finRec.codNat = simVO.asBigDecimal("CODNATREC");
			finRec.codCenCus = simVO.asBigDecimal("CODCENCUSREC");
			finRec.codProj = simVO.asBigDecimal("CODPROJREC");
			finRec.recDesp = BigDecimal.valueOf(1 * multiplicadorRecDesp);

			CamposParaCalculoDescontoVag vag = new CamposParaCalculoDescontoVag();
			vag.percRateio = simVO.asBigDecimal("PERCRATEIO");

			vag.vlrBaixa = simVO.asBigDecimalOrZero("VLRBAIXA").subtract(simVO.asBigDecimalOrZero("VLRCOMISJUR")).add(calcImpRetidos ? simVO.asBigDecimalOrZero("TOTAL_IMPOSTO_RETIDO") : BigDecimal.ZERO);

			repasseVO.setProperty("NUFINREC", persisteFinanceiro(finRec, getValorSimulacao(simVO), simVO.asBigDecimal("NUFIN"), vag));

			CamposAgrupamentoFinanceiro finDesp = new CamposAgrupamentoFinanceiro();
			finDesp.codEmp = simVO.asBigDecimal("CODEMPDESP");
			finDesp.codParc = simVO.asBigDecimal("CODPARCDESP");
			finDesp.codNat = simVO.asBigDecimal("CODNATDESP");
			finDesp.codCenCus = simVO.asBigDecimal("CODCENCUSDESP");
			finDesp.codProj = simVO.asBigDecimal("CODPROJDESP");
			finDesp.recDesp = BigDecimal.valueOf(-1 * multiplicadorRecDesp);

			repasseVO.setProperty("NUFINDESP", persisteFinanceiro(finDesp, getValorSimulacao(simVO), simVO.asBigDecimal("NUFIN"), vag));

			repasseVO.setProperty("NUFINORIG", simVO.asBigDecimal("NUFIN"));
			repasseVO.setProperty("NUCND", simVO.asBigDecimal("NUCND"));
			repasseVO.setProperty("PERCRATEIO", simVO.asBigDecimal("PERCRATEIO"));
			repasseVO.setProperty("VALORRATEIO", getValorSimulacao(simVO));

			try{
				dwfEntityFacade.findEntityByPrimaryKey("Repasse", new Object [] {
						repasseVO.asBigDecimal("NUFINORIG"),
						repasseVO.asBigDecimal("NUFINREC"),
						repasseVO.asBigDecimal("NUFINDESP"),
						repasseVO.asBigDecimal("NUCND"),
				} );
			} catch(FinderException finderException){

				dwfEntityFacade.createEntity("Repasse", (EntityVO) repasseVO);
			}

			alteraDadosDescontoRepasseVisita(repasseVO.asBigDecimal("NUFINORIG"), repasseVO.asBigDecimal("NUFINDESP"), repasseVO.asBigDecimal("NUFINREC"));
			arredondaFinanceirosGerados();
		}
	}

	private void alteraDadosDescontoRepasseVisita(BigDecimal nuFinOrigem, BigDecimal nuFinDescDesp, BigDecimal nuFinDescRec) throws Exception {
		try {
			PersistentLocalEntity descVagEntity = dwfEntityFacade.findEntityByPrimaryKey("DescontoRepasseVisita", nuFinOrigem);
			DynamicVO descVagVO = (DynamicVO) descVagEntity.getValueObject();

			descVagVO.setProperty("NUFINDESC", nuFinDescDesp);
			descVagVO.setProperty("NUFINDESCREC", nuFinDescRec);

			descVagEntity.setValueObject((EntityVO) descVagVO);
		} catch (FinderException ignored) {
		}
	}

	private void arredondaFinanceirosGerados() throws Exception {
		for (Iterator<Map.Entry<CamposAgrupamentoFinanceiro, BigDecimal>> ite = financeirosSintetico.entrySet().iterator(); ite.hasNext();) {
			Map.Entry<CamposAgrupamentoFinanceiro, BigDecimal> entry = ite.next();

			BigDecimal nuFin = entry.getValue();

			PersistentLocalEntity finEntity = dwfEntityFacade.findEntityByPrimaryKey("Financeiro", nuFin);
			DynamicVO finVO = (DynamicVO) finEntity.getValueObject();

			finVO.setProperty("VLRDESDOB", BigDecimalUtil.getRounded(finVO.asBigDecimal("VLRDESDOB"), 2));

			finEntity.setValueObject((EntityVO) finVO);
		}
	}

	private BigDecimal getValorSimulacao(DynamicVO simVO) {
		if (calcImpRetidos) {
			return simVO.asBigDecimalOrZero("VALOR_COM_IMPOSTO");
		} else {
			return simVO.asBigDecimalOrZero("VALOR_SEM_IMPOSTO");
		}
	}

	private void validarPeriodo() throws Exception {
		if (dtPeriodoInicial == null || dtPeriodoFinal == null) {
			throw new Exception("Perodo de baixa deve ser informado.");
		}

		if (dtPeriodoInicial.getTime() > dtPeriodoFinal.getTime()) {
			throw new Exception("A data inicial no pode ser maior que a final.");
		}
	}

	private void inicializaFinanceiro(DynamicVO finVO) throws Exception {

		Timestamp now = TimeUtils.getNow();

		if (BigDecimalUtil.getValueOrZero(codTipOperacao).intValue() > 0) {
			DynamicVO topVO = ComercialUtils.getTipoOperacao(codTipOperacao);

			finVO.setProperty("CODTIPOPER", topVO.asBigDecimal("CODTIPOPER"));
			finVO.setProperty("DHTIPOPER", topVO.asTimestamp("DHALTER"));
		}

		finVO.setProperty("CODTIPTIT", codTipTitulo);
		finVO.setProperty("HISTORICO", historico);
		finVO.setProperty("DTALTER", now);
		finVO.setProperty("DTNEG", dtNegociacao);
		finVO.setProperty("DTENTSAI", dtNegociacao);
		finVO.setProperty("DHMOV", now);
		finVO.setProperty("DTVENCINIC", dtVencimento);
		finVO.setProperty("DTVENC", dtVencimento);
		finVO.setProperty("ORIGEM", "F");
		finVO.setProperty("PROVISAO", "N");
	}

	private BigDecimal persisteFinanceiro(CamposAgrupamentoFinanceiro camposAgrupamento, BigDecimal valor, BigDecimal nuFinOrigem, CamposParaCalculoDescontoVag campoFinDesp) throws Exception {
		PersistentLocalEntity finEntity = null;
		DynamicVO finVO = null;

		long codNat = BigDecimalUtil.getValueOrZero(camposAgrupamento.codNat).longValue();
		long codCenCus = BigDecimalUtil.getValueOrZero(camposAgrupamento.codCenCus).longValue();
		long codProj = BigDecimalUtil.getValueOrZero(camposAgrupamento.codProj).longValue();

		if (codNat == 0 && codCenCus == 0 && codProj == 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal	vlrDesdobFinal = BigDecimal.ZERO;
		String 		observacaoVag = null;
		boolean		valorMinimo = false;

		if (camposAgrupamento.codNat.compareTo(BigDecimal.valueOf(110206)) == 0) {
			DynamicVO finDescVag;
			try{
				finDescVag = (DynamicVO) dwfEntityFacade.findEntityByPrimaryKeyAsVO("DescontoRepasseVisita", nuFinOrigem);
				BigDecimal vlrDescVag = finDescVag.asBigDecimalOrZero("VLRDESCREPASSE");

				if(vlrDescVag.compareTo(BigDecimal.ZERO) > 0 ){
					vlrDesdobFinal = valor.subtract(vlrDescVag);

					if(vlrDesdobFinal.doubleValue() <= 0.0){
						vlrDesdobFinal = BigDecimal.valueOf(0.01).setScale(2);
						valorMinimo = true;
					}

					if (!descontoTotalVag.containsKey(camposAgrupamento)) {
						descontoTotalVag.put(camposAgrupamento, vlrDescVag);
					} else {
						descontoTotalVag.put(camposAgrupamento, descontoTotalVag.get(camposAgrupamento).add(vlrDescVag));
					}

					observacaoVag = "Desconto VAG: R$" + descontoTotalVag.get(camposAgrupamento).setScale(2);
				}
			}catch(FinderException e){
			}
		}

		if (vlrDesdobFinal.compareTo(BigDecimal.ZERO) == 0) {
			vlrDesdobFinal = valor;
		}

		if (financeirosSintetico.containsKey(camposAgrupamento)) {
			finEntity = dwfEntityFacade.findEntityByPrimaryKey("Financeiro", financeirosSintetico.get(camposAgrupamento));
			finVO = (DynamicVO) finEntity.getValueObject();

			if (!StringUtils.isEmpty(observacaoVag)) {
				if (StringUtils.isEmpty(finVO.asString("HISTORICO"))) {
					finVO.setProperty("HISTORICO", observacaoVag);
				} else {
					int indexHistoricoDescAnterior = finVO.asString("HISTORICO").indexOf(". Desconto VAG: R$");

					if (indexHistoricoDescAnterior > -1) {
						finVO.setProperty("HISTORICO", finVO.asString("HISTORICO").substring(0, indexHistoricoDescAnterior));
					}

					finVO.setProperty("HISTORICO", finVO.getProperty("HISTORICO") + ". " + observacaoVag);
				}
			}

			if (!valorMinimo) {

				finVO.setProperty("VLRDESDOB", finVO.asBigDecimalOrZero("VLRDESDOB").add(vlrDesdobFinal));
			}
		} else {
			finVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("Financeiro");

			inicializaFinanceiro(finVO);

			DynamicVO finOrigVO = null;

			if (BigDecimalUtil.getValueOrZero(camposAgrupamento.codEmp).intValue() != 0) {
				finVO.setProperty("CODEMP", camposAgrupamento.codEmp);
			} else {
				finOrigVO = (DynamicVO) dwfEntityFacade.findEntityByPrimaryKeyAsVO("Financeiro", nuFinOrigem);
				finVO.setProperty("CODEMP", finOrigVO.asBigDecimalOrZero("CODEMP"));
			}

			if (BigDecimalUtil.getValueOrZero(camposAgrupamento.codParc).intValue() != 0) {
				finVO.setProperty("CODPARC", camposAgrupamento.codParc);
			} else {
				if (finOrigVO == null) {
					finOrigVO = (DynamicVO) dwfEntityFacade.findEntityByPrimaryKeyAsVO("Financeiro", nuFinOrigem);
				}

				finVO.setProperty("CODPARC", finOrigVO.asBigDecimalOrZero("CODPARC"));
			}

			finVO.setProperty("CODNAT", camposAgrupamento.codNat);
			finVO.setProperty("CODCENCUS", camposAgrupamento.codCenCus);
			finVO.setProperty("CODPROJ", camposAgrupamento.codProj);
			finVO.setProperty("RECDESP", camposAgrupamento.recDesp);

			finVO.setProperty("VLRDESDOB", vlrDesdobFinal);
			finVO.setProperty("NUMNOTA", BigDecimal.ZERO);

			finEntity = dwfEntityFacade.createEntity("Financeiro", (EntityVO) finVO);

			BigDecimal nuFin = finVO.asBigDecimal("NUFIN");

			finVO.setProperty("NUMNOTA", nuFin);

			if (!StringUtils.isEmpty(observacaoVag)) {
				finVO.setProperty("HISTORICO",finVO.getProperty("HISTORICO") ==  null ? observacaoVag :
					finVO.getProperty("HISTORICO") + ". " + observacaoVag);
			}

			financeirosSintetico.put(camposAgrupamento, nuFin);
		}

		finEntity.setValueObject((EntityVO) finVO);

		return finVO.asBigDecimal("NUFIN");
	}

	public Timestamp getDtPeriodoInicial() {
		return dtPeriodoInicial;
	}

	public void setDtPeriodoInicial(Timestamp dtPeriodoInicial) {
		this.dtPeriodoInicial = dtPeriodoInicial;
	}

	public Timestamp getDtPeriodoFinal() {
		return dtPeriodoFinal;
	}

	public void setDtPeriodoFinal(Timestamp dtPeriodoFinal) {
		this.dtPeriodoFinal = dtPeriodoFinal;
	}

	public BigDecimal getOrdem() {
		return ordem;
	}

	public void setOrdem(BigDecimal ordem) {
		this.ordem = ordem;
	}

	public BigDecimal getNuCnd() {
		return nuCnd;
	}

	public void setNuCnd(BigDecimal nuCnd) {
		this.nuCnd = nuCnd;
	}

	public Timestamp getDtNegociacao() {
		return dtNegociacao;
	}

	public void setDtNegociacao(Timestamp dtNegociacao) {
		this.dtNegociacao = dtNegociacao;
	}

	public Timestamp getDtVencimento() {
		return dtVencimento;
	}

	public void setDtVencimento(Timestamp dtVencimento) {
		this.dtVencimento = dtVencimento;
	}

	public BigDecimal getCodTipTitulo() {
		return codTipTitulo;
	}

	public void setCodTipTitulo(BigDecimal codTipTitulo) {
		this.codTipTitulo = codTipTitulo;
	}

	public BigDecimal getCodTipOperacao() {
		return codTipOperacao;
	}

	public void setCodTipOperacao(BigDecimal codTipOperacao) {
		this.codTipOperacao = codTipOperacao;
	}

	public String getHistorico() {
		return historico;
	}

	public void setHistorico(String historico) {
		this.historico = historico;
	}

	public boolean isCalcImpRetidos() {
		return calcImpRetidos;
	}

	public void setCalcImpRetidos(boolean calcImpRetidos) {
		this.calcImpRetidos = calcImpRetidos;
	}

	private static class CamposParaCalculoDescontoVag{
		private BigDecimal vlrBaixa;
		private BigDecimal percRateio;
	}

	private static class CamposAgrupamentoFinanceiro {
		private BigDecimal	codEmp;
		private BigDecimal	codParc;
		private BigDecimal	codNat;
		private BigDecimal	codCenCus;
		private BigDecimal	codProj;
		private BigDecimal	recDesp;

		@Override
		public int hashCode() {
			return (attStr(codEmp) + ":" + attStr(codParc) + ":" + attStr(codNat) + ":" + attStr(codCenCus) + ":" + attStr(codProj) + ":" + attStr(recDesp)).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			CamposAgrupamentoFinanceiro outroFin = (CamposAgrupamentoFinanceiro) obj;

			boolean mesmaEmpresa = codEmp.compareTo(outroFin.codEmp) == 0;
			boolean mesmoParceiro = codParc.compareTo(outroFin.codParc) == 0;
			boolean mesmaNatureza = codNat.compareTo(outroFin.codNat) == 0;
			boolean mesmoCentroResultado = codCenCus.compareTo(outroFin.codCenCus) == 0;
			boolean mesmoProjeto = codProj.compareTo(outroFin.codProj) == 0;
			boolean mesmoRecDesp = recDesp.compareTo(outroFin.recDesp) == 0;

			return mesmaEmpresa && mesmoParceiro && mesmaNatureza && mesmoCentroResultado && mesmoProjeto && mesmoRecDesp;
		}

		private String attStr(BigDecimal att) {
			return BigDecimalUtil.getValueOrZero(att).toString();
		}
	}
}
