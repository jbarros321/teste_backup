package br.com.sankhya.extensions.rateio.holding.dwfdata.listeners;

import java.math.BigDecimal;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.vo.DynamicVO;

public class ItemCriterioRateioHoldingListener extends PersistenceEventAdapter {

	public void beforeInsert(PersistenceEvent event) throws Exception {
		DynamicVO itemCndVO = (DynamicVO) event.getVo();

		aplicaDefaults(itemCndVO);
		validaCriterio(itemCndVO);
	}

	public void beforeUpdate(PersistenceEvent event) throws Exception {
		DynamicVO itemCndVO = (DynamicVO) event.getVo();

		aplicaDefaults(itemCndVO);
		validaCriterio(itemCndVO);
	}

	private void validaCriterio(DynamicVO icndVO) throws Exception {
		boolean mesmaNatureza = icndVO.asInt("CODNATREC") == icndVO.asInt("CODNATDESP");
		boolean mesmoCentroResultado = icndVO.asInt("CODCENCUSREC") == icndVO.asInt("CODCENCUSDESP");
		boolean mesmoProjeto = icndVO.asInt("CODPROJREC") == icndVO.asInt("CODPROJDESP");
		boolean mesmaEmpresa = icndVO.asInt("CODEMPREC") == icndVO.asInt("CODEMPDESP");
		boolean mesmoParceiro = icndVO.asInt("CODPARCREC") == icndVO.asInt("CODPARCDESP");

		if (mesmaNatureza && mesmoCentroResultado && mesmoProjeto && mesmaEmpresa && mesmoParceiro) {
			throw new Exception("Alguma definio na parte de Receita deve ser diferente da parte de Despesa.");
		}
	}

	private void aplicaDefaults(DynamicVO icndVO) {
		if (icndVO.getProperty("CODCENCUSDESP") == null) {
			icndVO.setProperty("CODCENCUSDESP", BigDecimal.ZERO);
		}

		if (icndVO.getProperty("CODCENCUSREC") == null) {
			icndVO.setProperty("CODCENCUSREC", BigDecimal.ZERO);
		}

		if (icndVO.getProperty("CODEMPDESP") == null) {
			icndVO.setProperty("CODEMPDESP", BigDecimal.ZERO);
		}

		if (icndVO.getProperty("CODEMPREC") == null) {
			icndVO.setProperty("CODEMPREC", BigDecimal.ZERO);
		}

		if (icndVO.getProperty("CODNATDESP") == null) {
			icndVO.setProperty("CODNATDESP", BigDecimal.ZERO);
		}

		if (icndVO.getProperty("CODNATREC") == null) {
			icndVO.setProperty("CODNATREC", BigDecimal.ZERO);
		}

		if (icndVO.getProperty("CODPARCDESP") == null) {
			icndVO.setProperty("CODPARCDESP", BigDecimal.ZERO);
		}

		if (icndVO.getProperty("CODPARCREC") == null) {
			icndVO.setProperty("CODPARCREC", BigDecimal.ZERO);
		}

		if (icndVO.getProperty("CODPROJDESP") == null) {
			icndVO.setProperty("CODPROJDESP", BigDecimal.ZERO);
		}

		if (icndVO.getProperty("CODPROJREC") == null) {
			icndVO.setProperty("CODPROJREC", BigDecimal.ZERO);
		}
	}
}
