package br.com.sankhya.extensions.celula.dynaform;

import java.util.ArrayList;
import java.util.Collection;

import br.com.sankhya.modelcore.dynaform.DynaformHelperAdapter;

public class CronogramaDynaformHelper extends DynaformHelperAdapter {

	public Collection<String> getIgnoredFields() throws Exception {
		Collection<String> fields = new ArrayList<String>();

		fields.add("AD_TCSSBLD.USERALTER");
		fields.add("AD_TCSSBLD.DHALTER");

		return fields;
	}
}
