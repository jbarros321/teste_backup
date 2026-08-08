package br.com.sankhya.flow.tools.migracao.actbtn;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.flow.tools.migracao.BPMElement;
import br.com.sankhya.flow.tools.migracao.MigracaoUtil;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ImportarAtividadesInstancia implements AcaoRotinaJava{

	public void doAction(ContextoAcao contexto) throws Exception {

		Registro plano = contexto.getLinhaPai();
		EntityFacadeFactory.getDWFFacade().removeByCriteria(new FinderWrapper("AD_MAPAMPRN", "this.NUPLANO = ?", new Object [] {plano.getCampo("NUPLANO")}));

		Map<String, BPMElement> source = new LinkedHashMap<String, BPMElement>();
		Map<String, BPMElement> target = MigracaoUtil.getElementosProcesso(plano.getCampo("CODPRNNOVO"), plano.getCampo("VERSAONOVO"));

		for(Registro instancia:contexto.getLinhas()){
			source.putAll(MigracaoUtil.getElementosInstanciaProcesso((BigDecimal) instancia.getCampo("IDINSTPRN")));
		}

		for(BPMElement e:source.values()){
			Registro r = contexto.novaLinha("AD_MAPAMPRN");
			r.setCampo("NUPLANO", plano.getCampo("NUPLANO"));
			r.setCampo("ATIVIDADEATUAL", e.getId());
			r.setCampo("DESCRATVATUAL", e.getName());
			BPMElement ne = target.get(e.getId());
			if(ne != null){
				r.setCampo("ATIVIDADENOVO", ne.getId());
				r.setCampo("DESCRATVNOVO", ne.getName());
			}
			r.save();
		}

	}
}
