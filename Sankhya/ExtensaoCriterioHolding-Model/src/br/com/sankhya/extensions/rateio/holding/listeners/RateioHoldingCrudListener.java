package br.com.sankhya.extensions.rateio.holding.listeners;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.CRUDServiceListenerAdapter;

public class RateioHoldingCrudListener extends CRUDServiceListenerAdapter {

	@Override
	public void beforeLoadRecord(DynamicVO vo) throws Exception {
		boolean valorComImposto = JapeSession.getPropertyAsBoolean("br.sankhya.mgefin.rateio.holding.valor.com.imposto", Boolean.FALSE);

		if (valorComImposto) {
			vo.setProperty("VALOR", vo.asBigDecimalOrZero("VALOR_COM_IMPOSTO").subtract(vo.asBigDecimalOrZero("VLRDESCVAG")));
		} else {
			vo.setProperty("VALOR", vo.asBigDecimalOrZero("VALOR_SEM_IMPOSTO").subtract(vo.asBigDecimalOrZero("VLRDESCVAG")));
		}
	}

}
