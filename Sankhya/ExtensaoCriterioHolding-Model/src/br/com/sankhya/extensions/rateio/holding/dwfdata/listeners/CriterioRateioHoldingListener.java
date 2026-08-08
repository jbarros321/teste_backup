package br.com.sankhya.extensions.rateio.holding.dwfdata.listeners;

import java.math.BigDecimal;
import java.sql.ResultSet;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class CriterioRateioHoldingListener extends PersistenceEventAdapter {

	public void beforeInsert(PersistenceEvent event) throws Exception {
		DynamicVO criterioVO = (DynamicVO) event.getVo();

		aplicaDefaults(criterioVO);
		validaCriterio(criterioVO);
		validaSeCriterioEhUnico(event);
	}

	public void beforeUpdate(PersistenceEvent event) throws Exception {
		DynamicVO criterioVO = (DynamicVO) event.getVo();

		aplicaDefaults(criterioVO);
		validaCriterio(criterioVO);
		validaSeCriterioEhUnico(event);
	}

	private void aplicaDefaults(DynamicVO criterioVO) {
		if (criterioVO.getProperty("CODEMP") == null) {
			criterioVO.setProperty("CODEMP", BigDecimal.ZERO);
		}

		if (criterioVO.getProperty("ORDEM") == null) {
			criterioVO.setProperty("ORDEM", BigDecimal.ZERO);
		}

		if (criterioVO.getProperty("CODCENCUS") == null) {
			criterioVO.setProperty("CODCENCUS", BigDecimal.ZERO);
		}

		if (criterioVO.getProperty("CODNAT") == null) {
			criterioVO.setProperty("CODNAT", BigDecimal.ZERO);
		}

		if (criterioVO.getProperty("CODPROJ") == null) {
			criterioVO.setProperty("CODPROJ", BigDecimal.ZERO);
		}
	}

	private void validaCriterio(DynamicVO criterioVO) throws Exception {
		if (criterioVO.asInt("CODNAT") == 0 && criterioVO.asInt("CODCENCUS") == 0 && criterioVO.asInt("CODPROJ") == 0) {
			throw new Exception("Natureza e/ou Centro de Resultado e/ou Projeto devem ser informados");
		}
	}

	private void validaSeCriterioEhUnico(PersistenceEvent event) throws Exception {
		DynamicVO criterioVO = (DynamicVO) event.getVo();

		if ((event.getType() == PersistenceEvent.BEFORE_INSERT || event.getModifingFields().isModifingAny("CODNAT,CODCENCUS,CODEMP,CODPROJ,ATIVO")) && "S".equals(criterioVO.asString("ATIVO"))) {

			JdbcWrapper jdbc = null;
			try {
				jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
				NativeSql queryCND = new NativeSql(jdbc);
				queryCND.appendSql("SELECT COUNT(1) ");
				queryCND.appendSql("  FROM TGFCND ");
				queryCND.appendSql(" WHERE ATIVO='S' AND ");
				queryCND.appendSql(" 	   CODEMP = :CODEMP AND ");
				queryCND.appendSql(" 	   CODNAT = :CODNAT AND ");
				queryCND.appendSql(" 	   CODCENCUS = :CODCENCUS AND ");
				queryCND.appendSql("	   CODPROJ = :CODPROJ ");

				queryCND.setNamedParameter("CODEMP", criterioVO.asInt("CODEMP"));
				queryCND.setNamedParameter("CODNAT", criterioVO.asInt("CODNAT"));
				queryCND.setNamedParameter("CODCENCUS", criterioVO.asInt("CODCENCUS"));
				queryCND.setNamedParameter("CODPROJ", criterioVO.asInt("CODPROJ"));

				ResultSet rset = null;
				rset = queryCND.executeQuery();
				if (rset.next()) {
					if (rset.getInt(1) > 0) {
						throw new Exception("Já existe critério ATIVO para a Natureza/Centro de Resultado/Projeto/Empresa.");
					}
				}
			} finally {
				JdbcWrapper.closeSession(jdbc);
			}
		}
	}
}
