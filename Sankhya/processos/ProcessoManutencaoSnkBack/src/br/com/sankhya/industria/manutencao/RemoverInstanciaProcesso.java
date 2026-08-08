package br.com.sankhya.industria.manutencao;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

public class RemoverInstanciaProcesso implements AcaoRotinaJava {

	public void doAction(ContextoAcao contexto) throws Exception {
		JdbcWrapper jdbc = null;
		try {
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfEntityFacade.getJdbcWrapper();

			BigDecimal idInstPrn = BigDecimalUtil.getBigDecimal(contexto.getParam("IDINSTPRN"));

			if (idInstPrn == null) {
				throw new Exception("Favor informar IDINSTPRN!!!");
			}

			String tabelas = "TWFIHIS-TWFIEXE-TWFIVAR-" + StringUtils.getNullAsEmpty(contexto.getParam("TABELAS"));
			for (String tabela : tabelas.split("-")) {
				if (StringUtils.getEmptyAsNull(tabela) != null) {
					removerLinhas(jdbc, idInstPrn, tabela, "");
				}
			}

			removerLinhas(jdbc, idInstPrn, "TWFUCP", " AND nullvalue(DINAMICO, 'N') = 'S' ");

			removerLinhas(jdbc, idInstPrn, "TWFITAR", "");

			removerLinhas(jdbc, idInstPrn, "TWFIPRN", "");
		} finally {
			JdbcWrapper.closeSession(jdbc);
		}
	}

	private void removerLinhas(JdbcWrapper jdbc, BigDecimal idInstPrn, String tabela, String criteriaExtra) throws Exception {
		NativeSql queryDelete = null;
		try {
			queryDelete = new NativeSql(jdbc);
			queryDelete.appendSql("	DELETE FROM ").appendSql(tabela).appendSql(" WHERE IDINSTPRN = :IDINSTPRN ");
			queryDelete.appendSql(criteriaExtra);

			queryDelete.setNamedParameter("IDINSTPRN", idInstPrn);

			queryDelete.executeUpdate();
		} finally {
			NativeSql.releaseResources(queryDelete);
		}
	}

}
