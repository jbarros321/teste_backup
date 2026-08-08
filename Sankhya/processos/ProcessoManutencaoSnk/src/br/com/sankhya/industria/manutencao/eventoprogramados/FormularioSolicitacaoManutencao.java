package br.com.sankhya.industria.manutencao.eventoprogramados;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class FormularioSolicitacaoManutencao implements EventoProgramavelJava {

	public void beforeInsert(PersistenceEvent event) throws Exception {
		DynamicVO vo = (DynamicVO) event.getVo();

		vo.setProperty("QTDCICLOSMAN", new BigDecimal(1));
		vo.setProperty("STATUSCICLOMAN", "P");

	}

	public void beforeUpdate(PersistenceEvent event) throws Exception {
	}

	public void beforeDelete(PersistenceEvent event) throws Exception {
	}

	public void afterInsert(PersistenceEvent event) throws Exception {
		DynamicVO vo = (DynamicVO) event.getVo();
		Map<String, Object> camposOS = new HashMap<String, Object>();
		camposOS.put("AD_CELULALEAN", vo.asBigDecimal("CODCELPROD"));
		camposOS.put("NUMOSRELACIONADA", vo.asBigDecimal("NUMOSRELACIONADA"));
		ManutencaoSnkUtil.atualizaCamposOs(vo.asBigDecimal("NUMOS"), camposOS);
	}

	public void afterUpdate(PersistenceEvent event) throws Exception {

		Map<String, Object> camposOS = new HashMap<String, Object>();
		DynamicVO solimanVO = (DynamicVO) event.getVo();

		if (event.getModifingFields().isModifingAny("NUMOS,CODCELPROD")) {
			camposOS.put("AD_CELULALEAN", solimanVO.asBigDecimal("CODCELPROD"));
		}
		if (event.getModifingFields().isModifing("NUMOSRELACIONADA")) {
			camposOS.put("NUMOSRELACIONADA", solimanVO.asBigDecimal("NUMOSRELACIONADA"));
		}
		if (event.getModifingFields().isModifing("DESCSERV")) {
			camposOS.put("DESCRICAO", solimanVO.asString("DESCSERV"));
		}

		if (event.getModifingFields().isModifing("LOG")) {
			insereArquivoLogOS(solimanVO);
		}

		if (event.getModifingFields().isModifing("CODPROD")) {
			BigDecimal numOs = solimanVO.asBigDecimal("NUMOS");
			BigDecimal codProd = solimanVO.asBigDecimal("CODPROD");
			if (numOs != null && codProd != null) {
				alteraProdutoOS(numOs, codProd);
			}
		}

		if (event.getModifingFields().isModifing("PRIORIDADE")) {
			atualizaPrioridade(solimanVO);
		}

		ManutencaoSnkUtil.atualizaCamposOs(solimanVO.asBigDecimal("NUMOS"), camposOS);
	}

	private void alteraProdutoOS(BigDecimal numOs, BigDecimal codProduto) throws Exception {

		EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = null;
		NativeSql sql = null;
		try {
			jdbc = entityFacade.getJdbcWrapper();
			sql = new NativeSql(jdbc);
			sql.appendSql(" SELECT DISTINCT ");
			sql.appendSql(" 	CODSERV, ");
			sql.appendSql(" 	CODUSU ");
			sql.appendSql(" FROM ");
			sql.appendSql(" 	TCSITE ITE ");
			sql.appendSql(" WHERE ");
			sql.appendSql(" 	ITE.NUMOS = :NUMOS AND ");
			sql.appendSql(" 	NOT EXISTS( ");
			sql.appendSql(" 		SELECT 1 FROM ");
			sql.appendSql(" 		TGFSEU SEU ");
			sql.appendSql(" 		WHERE SEU.CODSERV = ITE.CODSERV AND ");
			sql.appendSql(" 		SEU.CODPROD = :CODPROD AND ");
			sql.appendSql(" 		SEU.CODUSU = ITE.CODUSU) ");
			sql.setNamedParameter("NUMOS", numOs);
			sql.setNamedParameter("CODPROD", codProduto);

			ResultSet rset = sql.executeQuery();
			StringBuffer bufInvalidos = new StringBuffer();
			while (rset.next()) {
				if (bufInvalidos.length() > 0) {
					bufInvalidos.append(",<br />");
				}
				bufInvalidos.append("Executante: ");
				bufInvalidos.append(rset.getString("CODUSU"));
				bufInvalidos.append(" -> Servio: ");
				bufInvalidos.append(rset.getString("CODSERV"));
			}

			if (bufInvalidos.length() > 0) {
				throw new Exception("Existem executantes que no esto autorizados a executar determinados servios para o produto: " + codProduto + ".<br /><b>" + bufInvalidos.toString() + "</b><br /><br />");
			}

			JapeSessionContext.putProperty("subos.aplica.validacoes", Boolean.FALSE);
			JapeSessionContext.putProperty("os.alterando.produto", Boolean.TRUE);

			Collection<PersistentLocalEntity> itensOS = entityFacade.findByDynamicFinder(new FinderWrapper(DynamicEntityNames.ITEM_ORDEM_SERVICO, "this.NUMOS = ?", new Object[] { numOs }));

			for (PersistentLocalEntity subOS : itensOS) {
				DynamicVO itemVO = (DynamicVO) subOS.getValueObject();
				itemVO.setProperty("CODPROD", codProduto);
				subOS.setValueObject((EntityVO) itemVO);
			}

			JapeSessionContext.removeProperty("os.alterando.produto");

		} finally {
			NativeSql.releaseResources(sql);
			JdbcWrapper.closeSession(jdbc);
		}
	}

	public void afterDelete(PersistenceEvent event) throws Exception {
	}

	public void beforeCommit(TransactionContext tranCtx) throws Exception {
	}

	private void insereArquivoLogOS(DynamicVO vo) throws Exception {
		byte[] logSoliMan = vo.asBlob("LOG");

		BigDecimal numOS = vo.asBigDecimal("NUMOS");
		BigDecimal anexoOS = NativeSql.getBigDecimal("1", "TSIATA", "ARQUIVO = 'server.log' AND DESCRICAO = 'Log' AND CODATA = ?", new Object[] { numOS });

		if (logSoliMan != null && anexoOS == null) {
			BigDecimal solicitante = ManutencaoSnkUtil.getUsuarioUltimoItemOS(numOS);
			Map<String, Object> infoAnexo = new HashMap<String, Object>();
			infoAnexo.put("NUMOS", numOS);
			infoAnexo.put("CODREGISTRO", vo.asBigDecimal("CODREGISTRO"));
			infoAnexo.put("IDINSTTAR", vo.asBigDecimal("IDINSTTAR"));
			infoAnexo.put("IDINSTPRN", vo.asBigDecimal("IDINSTPRN"));
			ManutencaoSnkUtil.salvaLogAnexoOS(infoAnexo, solicitante);
		}
	}

	private void atualizaPrioridade(DynamicVO vo) throws Exception {

		BigDecimal numOS = vo.asBigDecimal("NUMOS");
		BigDecimal prioridade = vo.asBigDecimal("PRIORIDADE");
		BigDecimal codUsu = null;

		AuthenticationInfo ai = AuthenticationInfo.getCurrentOrNull();
		if (ai != null) {
			codUsu = ai.getUserID();
		}

		if (numOS != null && prioridade != null && codUsu != null) {

			FinderWrapper finder = new FinderWrapper(DynamicEntityNames.ITEM_ORDEM_SERVICO, "this.NUMOS = ? AND this.NUMITEM = (SELECT MAX(NUMITEM) FROM TCSITE ITE2 WHERE ITE2.NUMOS = this.NUMOS AND ITE2.CODUSU = ?)", new Object[] { numOS, codUsu });
			Collection<PersistentLocalEntity> result = EntityFacadeFactory.getDWFFacade().findByDynamicFinder(finder);

			if (!result.isEmpty()) {

				PersistentLocalEntity ple = result.iterator().next();
				DynamicVO itemVO = (DynamicVO) ple.getValueObject();
				itemVO.setProperty("PRIORIDADE", prioridade);
				ple.setValueObject((EntityVO) itemVO);
			}
		}
	}
}
