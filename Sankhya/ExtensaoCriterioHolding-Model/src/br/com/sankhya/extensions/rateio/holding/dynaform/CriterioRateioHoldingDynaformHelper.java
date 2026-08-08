package br.com.sankhya.extensions.rateio.holding.dynaform;

import java.util.ArrayList;
import java.util.Collection;

import br.com.sankhya.modelcore.dynaform.DynaformHelperAdapter;

public class CriterioRateioHoldingDynaformHelper extends DynaformHelperAdapter {

	@Override
	public Collection<String> getInvisibleOneToManyRelations() throws Exception {
		Collection<String> ligacoesInvisiveis = new ArrayList<String>();

		ligacoesInvisiveis.add("ItemCriterioRateioHolding");

		return ligacoesInvisiveis;
	}

}
