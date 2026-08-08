package br.com.sankhya.industria.manutencao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.workflow.utils.UsuarioCandidatoResolver;

public class PopularUCP implements AcaoRotinaJava {

	public void doAction(ContextoAcao ctx) throws Exception {
		JdbcWrapper jdbc = null;
		try {

			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			UsuarioCandidatoResolver.resolveDynamicUserBackground(dwfEntityFacade, jdbc, true);
		} finally {
			JdbcWrapper.closeSession(jdbc);
		}

	}

}
