package br.com.sankhya.industria.manutencao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class AtualizarVersaoProcesso implements AcaoRotinaJava {

	public void doAction(ContextoAcao contexto) throws Exception {

		NativeSql query = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());

		query.executeUpdate("UPDATE TWFEVE SET QUANDO = 'D' WHERE NUEVENT = 12028");

	}

}
