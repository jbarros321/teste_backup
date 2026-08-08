package br.com.sankhya.industria.manutencao;

import java.math.BigDecimal;

import com.sankhya.util.BigDecimalUtil;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class AtualizaExpressaoCandidato implements AcaoRotinaJava {

	public void doAction(ContextoAcao ctx) throws Exception {

		BigDecimal codProcesso = BigDecimalUtil.getBigDecimal(ctx.getParam("CODPRN"));

		BigDecimal versaoCorrente = NativeSql.getBigDecimal("MAX(VERSAO)", "TWFPRN", "CODPRN = ?", new Object[] { codProcesso });

		JdbcWrapper jdbc = null;
		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			NativeSql query = new NativeSql(jdbc);
			query.appendSql(" UPDATE TWFELE SET EXPRESSCANDITADE = (SELECT EXPRESSCANDITADE FROM TWFELE WHERE IDELEMENTO = :IDELEMENTO AND CODPRN = :CODPRN AND VERSAO = :VERSAO) ");
			query.appendSql(" WHERE  ");
			query.appendSql(" 	IDELEMENTO = :IDELEMENTO ");
			query.appendSql(" 	AND CODPRN = :CODPRN  ");
			query.appendSql(" 	AND VERSAO <> :VERSAO  ");
			query.appendSql(" 	AND EXISTS (  ");
			query.appendSql(" 		SELECT  ");
			query.appendSql(" 			1  ");
			query.appendSql(" 		FROM  ");
			query.appendSql(" 			TWFITAR ITAR  ");
			query.appendSql(" 		JOIN TWFIPRN IPRN ON  ");
			query.appendSql(" 			ITAR.IDINSTPRN = IPRN.IDINSTPRN  ");
			query.appendSql(" 		WHERE  ");
			query.appendSql(" 			ITAR.IDELEMENTO = :IDELEMENTO ");
			query.appendSql(" 			AND ITAR.DHCONCLUSAO IS NULL  ");
			query.appendSql(" 			AND ITAR.CODUSUDONO IS NULL  ");
			query.appendSql(" 			AND IPRN.CODPRN = :CODPRN) ");

			query.setNamedParameter("IDELEMENTO", ctx.getParam("IDELEMENTO"));
			query.setNamedParameter("CODPRN", codProcesso);
			query.setNamedParameter("VERSAO", versaoCorrente);

			query.executeUpdate();
		} finally {
			JdbcWrapper.closeSession(jdbc);
		}
	}
}
