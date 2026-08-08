package br.com.sankhya.flow.tools.migracao.actbtn;

import java.util.Map;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.flow.tools.migracao.BPMElement;
import br.com.sankhya.flow.tools.migracao.MigracaoUtil;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ImportarAtividadesProcesso implements AcaoRotinaJava{

	public void doAction(ContextoAcao contexto) throws Exception {
		for(Registro plano:contexto.getLinhas()){

			EntityFacadeFactory.getDWFFacade().removeByCriteria(new FinderWrapper("AD_MAPAMPRN", "this.NUPLANO = ?", new Object [] {plano.getCampo("NUPLANO")}));

			Map<String, BPMElement> source = MigracaoUtil.getElementosProcesso(plano.getCampo("CODPRNATUAL"), plano.getCampo("VERSAOATUAL"));
			Map<String, BPMElement> target = MigracaoUtil.getElementosProcesso(plano.getCampo("CODPRNNOVO"), plano.getCampo("VERSAONOVO"));

			for(BPMElement e:source.values()){
				Registro r = contexto.novaLinha("AD_MAPAMPRN");
				r.setCampo("NUPLANO", plano.getCampo("NUPLANO"));
				r.setCampo("ATIVIDADEATUAL", e.getId());
				r.setCampo("DESCRATVATUAL", e.getName());
				BPMElement ne = target.remove(e.getId());
				if(ne != null){
					r.setCampo("ATIVIDADENOVO", ne.getId());
					r.setCampo("DESCRATVNOVO", ne.getName());
				}
				r.save();
			}

			for(BPMElement e:target.values()){
				Registro r = contexto.novaLinha("AD_MAPAMPRN");
				r.setCampo("NUPLANO", plano.getCampo("NUPLANO"));
				r.setCampo("ATIVIDADENOVO", e.getId());
				r.setCampo("DESCRATVNOVO", e.getName());
				r.save();
			}
		}
	}
}
