package br.com.sankhya.flow.modelos.comercial.pedidoVenda;
import java.math.BigDecimal;
import java.sql.ResultSet;

import com.sankhya.util.BigDecimalUtil;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
public class EventoTarefaInsereLiberacaoLimites implements EventoProcessoJava {

	public void executar(ContextoEvento ctx) throws Exception {

		Registro [] solicitacoes = ctx.getLinhasFormulario("AD_PEDIDOVENDA");

		if (solicitacoes.length <= 0) {

			throw new IllegalStateException("No existe registro no formulrio principal (AD_PEDIDOVENDA).");

		} else {

			BigDecimal nunota = BigDecimalUtil.getBigDecimal(solicitacoes[0].getCampo("NUNOTA"));
			BigDecimal idinstprn = (BigDecimal) ctx.getIdInstanceProcesso();
			BigDecimal idinsttar = (BigDecimal) ctx.getIdInstanceTarefa();

			JdbcWrapper jdbc = null;

			try {
				jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
				NativeSql query = new NativeSql(jdbc);
				query.appendSql(" SELECT ");
				query.appendSql("   NUCHAVE, TABELA, EVENTO, CODUSUSOLICIT, VLRATUAL, VLRLIMITE, SEQUENCIA, SEQCASCATA, NUCLL ");
				query.appendSql(" FROM ");
				query.appendSql("   TSILIB ");
				query.appendSql(" WHERE ");
				query.appendSql("  	TABELA IN('TGFCAB', 'TGFITE') ");
				query.appendSql("   AND NUCHAVE = :NUCHAVE ");
				query.appendSql("   AND DHLIB IS NULL ");

				query.setNamedParameter("NUCHAVE", nunota);
				ResultSet rs = query.executeQuery();

				int codRegistro = 0;

				if (rs.next()) {

					codRegistro++;

				    BigDecimal nuchave = rs.getBigDecimal("NUCHAVE");
				    String tabela = rs.getString("TABELA");
				    BigDecimal evento = rs.getBigDecimal("EVENTO");
				    BigDecimal sequencia = rs.getBigDecimal("SEQUENCIA");
				    BigDecimal seqCascata = rs.getBigDecimal("SEQCASCATA");
				    BigDecimal nuCll = rs.getBigDecimal("NUCLL");
				    Double vlratual = rs.getDouble("VLRATUAL");
				    Double vlrlimite = rs.getDouble("VLRLIMITE");

				    Registro copiaLiberacao = ctx.novaLinhaFormulario("AD_LIBERACOESVENDA");

				    copiaLiberacao.setCampo("IDINSTPRN", idinstprn);
				    copiaLiberacao.setCampo("IDINSTTAR", idinsttar);
				    copiaLiberacao.setCampo("CODREGISTRO", new BigDecimal(codRegistro));

				    copiaLiberacao.setCampo("NUCHAVE", nuchave);
				    copiaLiberacao.setCampo("TABELA", tabela);
				    copiaLiberacao.setCampo("EVENTO", evento);
				    copiaLiberacao.setCampo("SEQUENCIA", sequencia);
				    copiaLiberacao.setCampo("SEQCASCATA", seqCascata);
				    copiaLiberacao.setCampo("NUCLL", nuCll);

			        copiaLiberacao.setCampo("VLRATUAL", vlratual);
			        copiaLiberacao.setCampo("VLRLIMITE", vlrlimite);
			        copiaLiberacao.save();
				}
			} finally {
				if (jdbc != null) {
					JdbcWrapper.closeSession(jdbc);
				}
			}
		}
	}
}
