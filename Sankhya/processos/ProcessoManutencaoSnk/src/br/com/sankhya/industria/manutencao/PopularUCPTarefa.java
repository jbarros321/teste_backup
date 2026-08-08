package br.com.sankhya.industria.manutencao;

import java.math.BigDecimal;

import com.sankhya.util.BigDecimalUtil;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.workflow.utils.UsuarioCandidatoResolver;

public class PopularUCPTarefa implements AcaoRotinaJava {
	public void doAction(ContextoAcao ctx) throws Exception {
		JdbcWrapper jdbc = null;
		try {
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			BigDecimal idTarefa = BigDecimalUtil.getBigDecimal(ctx.getParam("IDINSTTAR"));
			BigDecimal idProcesso = NativeSql.getBigDecimal("IDINSTPRN", "TWFITAR", "IDINSTTAR= ?", new Object[] { idTarefa });

			UsuarioCandidatoResolver.resolveDynamicUserBackground(dwfEntityFacade, jdbc, idProcesso, idTarefa, false);
		} finally {
			JdbcWrapper.closeSession(jdbc);
		}
	}
}
