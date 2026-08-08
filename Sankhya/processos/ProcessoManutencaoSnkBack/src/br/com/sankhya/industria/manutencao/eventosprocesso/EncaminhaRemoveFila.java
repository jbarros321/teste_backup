package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.sql.ResultSet;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.eventoprogramados.BloqueioAlteracaoOSManutencaoSk;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.TXManagerUtil;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import com.sankhya.util.BigDecimalUtil;

public class EncaminhaRemoveFila implements EventoProcessoJava {

	public final static String OS_MANUTENCAO_EM_INCLUSAO = "OS_MANUTENCAO_EM_INCLUSAO";

	public BigDecimal getFilaAnterior(BigDecimal numOS, BigDecimal codUsu) throws Exception {
		JdbcWrapper jdbc = null;

		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();

			BigDecimal codFila = null;

			NativeSql query = new NativeSql(jdbc);

			query.appendSql(" SELECT ");
			query.appendSql("   I.CODUSU ");
			query.appendSql(" FROM ");
			query.appendSql("   TCSITE I ");
			query.appendSql(" WHERE ");
			query.appendSql("   I.NUMOS = :NUMOS");
			query.appendSql("   AND EXISTS(SELECT 1 FROM TCSRUS R WHERE R.CODUSU = I.CODUSU AND R.TIPO = 'F' AND CODUSUREL = :CODUSU) ");
			query.appendSql(" ORDER BY ");
			query.appendSql("   I.NUMITEM DESC ");

			query.setNamedParameter("NUMOS", numOS);
			query.setNamedParameter("CODUSU", codUsu);

			ResultSet rs = query.executeQuery();

			if (rs.next()) {
				codFila = rs.getBigDecimal("CODUSU");
			}

			if (codFila == null) {
				throw new Exception("No existe nenhum item para uma das filas do usurio " + codUsu + ". No  possvel devolver a solicitao.");
			}

			return codFila;
		} finally {
			JdbcWrapper.closeSession(jdbc);
		}
	}

	public static BigDecimal getItemAbertoFila(BigDecimal numOs, BigDecimal codUsu) throws Exception {
		JdbcWrapper jdbc = null;

		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			BigDecimal numItem = null;

			NativeSql query = new NativeSql(jdbc);
			query.appendSql(" SELECT ");
			query.appendSql("   NUMITEM ");
			query.appendSql(" FROM ");
			query.appendSql(" 	TCSITE ");
			query.appendSql(" WHERE ");
			query.appendSql(" 	NUMOS = :NUMOS AND ");
			query.appendSql(" 	HRINICIAL IS NULL AND ");
			query.appendSql(" 	HRFINAL IS NULL AND ");
			query.appendSql(" 	EXISTS(SELECT 1 FROM TCSRUS RUS WHERE RUS.CODUSU = TCSITE.CODUSU AND RUS.TIPO = 'F' AND RUS.CODUSUREL = :CODUSU) ");
			query.appendSql(" ORDER BY ");
			query.appendSql(" 	NUMITEM DESC ");

			query.setNamedParameter("NUMOS", numOs);
			query.setNamedParameter("CODUSU", codUsu);

			ResultSet rs = query.executeQuery();

			if (rs.next()) {
				numItem = rs.getBigDecimal("NUMITEM");
			}

			if (numItem == null) {
				throw new Exception("No existe um item aberto para qualquer fila do usurio " + codUsu + ".");
			}

			return numItem;
		} finally {
			JdbcWrapper.closeSession(jdbc);
		}
	}

	public void executar(ContextoEvento ctx) throws Exception {
		Registro [] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];

			final BigDecimal numOS = new BigDecimal(linha.getCampo("NUMOS").toString());
			final BigDecimal novoDono = ctx.getNovoDono();

			if (novoDono == null) {
				BigDecimal donoAnterior = ctx.getDonoAnterior();
				BigDecimal codFila = getFilaAnterior(numOS, donoAnterior);
				BigDecimal itemFechar = OrdemServicoAPI.getItemAbertoUsuario(numOS, donoAnterior, false);

				OrdemServicoAPI.encaminhaFila(numOS, itemFechar, donoAnterior, codFila);
			} else {
				boolean temItemAberto = false;

				try {
					temItemAberto = OrdemServicoAPI.getItemAbertoUsuario(numOS, novoDono, false) != null;
				} catch (Exception ignored) {}

				if (!temItemAberto) {
					final BigDecimal codFila =  getItemAbertoFila(numOS, novoDono);
					boolean incluindoOS = numOS.compareTo(BigDecimalUtil.getValueOrZero(JapeSession.getPropertyAsBigDecimal(OS_MANUTENCAO_EM_INCLUSAO))) == 0;

					if (incluindoOS) {

						 TXManagerUtil.addOnSucessListener(new Runnable() {
							public void run() {
								SessionHandle hnd = null;
								try {
									hnd = JapeSession.open();
									JapeSession.putProperty(BloqueioAlteracaoOSManutencaoSk.IGNORAR_VALIDACAO_FLOW, true);
									execUnder(novoDono, new Runnable() {
										public void run() {
											try{
												JapeSession.putProperty(BloqueioAlteracaoOSManutencaoSk.IGNORAR_VALIDACAO_FLOW, true);
												OrdemServicoAPI.tiraDaFilaPorItem(numOS, codFila, novoDono);
											} catch (Exception e){
												throw new RuntimeException(e);
											}
										}
									});
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									JapeSession.close(hnd);
								}
							}

						 });
					} else {
						execUnder(novoDono, new Runnable() {
							public void run() {
								try{
									JapeSession.putProperty(BloqueioAlteracaoOSManutencaoSk.IGNORAR_VALIDACAO_FLOW, true);
									OrdemServicoAPI.tiraDaFilaPorItem(numOS, codFila, novoDono);
								} catch (Exception e){
									throw new RuntimeException(e);
								}
							}
						});
					}
				}
			}
		}
	}

	private void execUnder(BigDecimal loggedUser, Runnable action) {
		AuthenticationInfo userAuthentication = AuthenticationInfo.getCurrentOrNull();

		if (userAuthentication != null && userAuthentication.getUserID().compareTo(loggedUser) != 0) {
			AuthenticationInfo fake = new AuthenticationInfo("", loggedUser, null, 0);
			try{
				fake.makeCurrent();
				action.run();
			} finally{
				userAuthentication.makeCurrent();
			}
		} else {
			action.run();
		}

	}
}
