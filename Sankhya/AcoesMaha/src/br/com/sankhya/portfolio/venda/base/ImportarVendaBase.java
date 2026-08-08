package br.com.sankhya.portfolio.venda.base;

import java.math.BigDecimal;
import java.nio.channels.IllegalSelectorException;
import java.sql.ResultSet;

import com.sankhya.util.BigDecimalUtil;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ImportarVendaBase implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao ctx) throws Exception {
		JdbcWrapper jdbc = null;

		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();

			validarTipoNegociacao(ctx, jdbc);

			BigDecimal numOS = ((BigDecimal) ctx.getLinhaPai().getCampo("NUMOS"));
			BigDecimal codParc = getCodParc(ctx, jdbc);
			BigDecimal codUsu = ctx.getUsuarioLogado();

			ImportadorVendaBaseHelper importadorVendaBase = new ImportadorVendaBaseHelper(numOS, codParc, codUsu, jdbc);
			importadorVendaBase.importar();

		} catch (Exception e) {
			throw new IllegalStateException("Erro ao tentar importar os dados do contrato!", e);
		}
	}

	private void validarTipoNegociacao(ContextoAcao ctx, JdbcWrapper jdbc) throws Exception {
		BigDecimal codTpn = ((BigDecimal) ctx.getLinhaPai().getCampo("CODTPN"));

		NativeSql query = new NativeSql(jdbc);
		query.appendSql("SELECT 1 FROM TCSTPN WHERE CODTPN = :CODTPN AND UPPER(DESCRICAO) LIKE 'VENDA%BASE%'");

		query.setNamedParameter("CODTPN", codTpn);

		ResultSet rs = query.executeQuery();

		if ( ! rs.next()) {
			throw new IllegalStateException("Negociao no  do tipo Venda Base - NP. No  possvel importar do contrato.");
		}
	}

	private BigDecimal getCodParc(ContextoAcao ctx, JdbcWrapper jdbc) throws Exception {
		BigDecimal codParc = BigDecimalUtil.getValueOrZero(((BigDecimal) ctx.getLinhaPai().getCampo("CODPARC")));

		if (BigDecimal.ZERO.equals(codParc)) {
			BigDecimal codProsp = BigDecimalUtil.getValueOrZero(((BigDecimal) ctx.getLinhaPai().getCampo("CODPAP")));

			NativeSql query = new NativeSql(jdbc);
			query.appendSql("SELECT CODPARC FROM TCSPAP WHERE CODPAP = :CODPAP");

			query.setNamedParameter("CODPAP", codProsp);

			ResultSet rs = query.executeQuery();

			if (rs.next()) {
				codParc = rs.getBigDecimal("CODPARC");
			}

			rs.close();
		}

		return codParc;
	}
}
