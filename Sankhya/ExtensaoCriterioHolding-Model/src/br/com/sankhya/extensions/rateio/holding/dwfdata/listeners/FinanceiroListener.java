package br.com.sankhya.extensions.rateio.holding.dwfdata.listeners;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

@SuppressWarnings("serial")
public class FinanceiroListener extends PersistenceEventAdapter {

	@Override
	public void beforeDelete(PersistenceEvent event) throws Exception {
		DynamicVO finVO = (DynamicVO) event.getVo();
		BigDecimal nuFin = finVO.asBigDecimal("NUFIN");

		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

		Collection<?> descontosRV = dwfEntityFacade.findByDynamicFinder(new FinderWrapper("DescontoRepasseVisita", "this.NUFINDESC = ? OR this.NUFINDESCREC = ?", new Object[] { nuFin, nuFin }));

		for (Iterator<?> ite = descontosRV.iterator(); ite.hasNext();) {
			PersistentLocalEntity drvEntity = (PersistentLocalEntity) ite.next();
			DynamicVO drvVO = (DynamicVO) drvEntity.getValueObject();

			if (drvVO.asBigDecimalOrZero("NUFINDESC").compareTo(nuFin) == 0) {
				drvVO.setProperty("NUFINDESC", null);
			} else if (drvVO.asBigDecimalOrZero("NUFINDESCREC").compareTo(nuFin) == 0) {
				drvVO.setProperty("NUFINDESCREC", null);
			}

			drvEntity.setValueObject((EntityVO) drvVO);
		}
	}
}
