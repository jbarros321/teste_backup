package br.com.sankhya.industria.manutencao.eventoprogramados;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class EventoOSManutencaoSk implements EventoProgramavelJava {

	public void beforeInsert(PersistenceEvent event) throws Exception {
	}

	public void beforeUpdate(PersistenceEvent event) throws Exception {
	}

	public void beforeDelete(PersistenceEvent event) throws Exception {
	}

	public void afterInsert(PersistenceEvent event) throws Exception {
	}

	public void afterUpdate(PersistenceEvent event) throws Exception {

		DynamicVO OS = (DynamicVO) event.getVo();
		BigDecimal numOS = OS.asBigDecimal("NUMOS");

		if(ManutencaoSnkUtil.temSoliman(numOS)) {

			BigDecimal celulaSoliman = NativeSql.getBigDecimal("CODCELPROD", "AD_TWFSOLIMAN", "NUMOS = ?", new Object [] { numOS });
			String problemaSoliman = StringUtils.getNullAsEmpty(NativeSql.getString("DESCSERV", "AD_TWFSOLIMAN", "NUMOS = ?", new Object [] { numOS }));

			BigDecimal celulaOS = OS.asBigDecimal("AD_CELULALEAN");
			String problemaOS = StringUtils.getNullAsEmpty(OS.asString("DESCRICAO"));

			NativeSql query = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());

			if (BigDecimalUtil.getValueOrZero(celulaSoliman).compareTo(BigDecimalUtil.getValueOrZero(celulaOS)) != 0) {
				query.setNamedParameter("CODCELPROD", celulaOS);
				query.setNamedParameter("NUMOS", numOS);

				query.executeUpdate(" UPDATE AD_TWFSOLIMAN SET CODCELPROD = :CODCELPROD WHERE NUMOS = :NUMOS ");
			}

			if (!problemaSoliman.equals(problemaOS)) {
				query.setNamedParameter("DESCSERV", problemaOS);
				query.setNamedParameter("NUMOS", numOS);

				query.executeUpdate(" UPDATE AD_TWFSOLIMAN SET DESCSERV = :DESCSERV WHERE NUMOS = :NUMOS ");
			}
		}
	}

	public void afterDelete(PersistenceEvent event) throws Exception {
	}

	public void beforeCommit(TransactionContext tranCtx) throws Exception {

	}

}
