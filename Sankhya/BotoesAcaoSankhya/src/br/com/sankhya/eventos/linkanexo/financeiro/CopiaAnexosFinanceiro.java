package br.com.sankhya.eventos.linkanexo.financeiro;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sankhya.util.StringUtils;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.EntityPrimaryKey;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class CopiaAnexosFinanceiro implements EventoProgramavelJava{

	public void beforeInsert(PersistenceEvent event) throws Exception {}

	public void beforeUpdate(PersistenceEvent event) throws Exception {}

	public void beforeDelete(PersistenceEvent event) throws Exception {}

	public void afterInsert(PersistenceEvent event) throws Exception {}

	public void afterUpdate(PersistenceEvent event) throws Exception {}

	public void afterDelete(PersistenceEvent event) throws Exception {

		DynamicVO vo = (DynamicVO) event.getVo();
		if(vo.asString("LINK") != null){
			return;
		}

		JdbcWrapper jdbc = null;
		try{
			jdbc = event.getJdbcWrapper();

			NativeSql query = new NativeSql(jdbc);
			query.appendSql("DELETE FROM TSIANX WHERE LINK IS NOT NULL AND CHAVEARQUIVO LIKE '" + vo.asString("CHAVEARQUIVO") + "_%'");

			query.executeUpdate();

		} finally {
			JdbcWrapper.closeSession(jdbc);
		}
	}

	public void beforeCommit(TransactionContext tranCtx) throws Exception {

		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;
		try{
			hnd = JapeSession.open();
			EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = dwfFacade.getJdbcWrapper();

			for(EntityPrimaryKey pk:tranCtx.getInserted()){
				DynamicVO vo = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.ANEXO_SISTEMA, pk);
				if(vo.asString("LINK") != null){
					return;
				}
				String pkRegistro = StringUtils.getNullAsEmpty(vo.asString("PKREGISTRO"));
				Pattern pattern = Pattern.compile("(\\d+)_Financeiro");
				Matcher m = pattern.matcher(pkRegistro);

				if(m.find()){
					String nufin = m.group(1);
					DynamicVO voLimpo = vo.buildClone();
					voLimpo.clearReferences();
					voLimpo.setPrimaryKey(null);
					voLimpo.setProperty("NUATTACH", null);
					voLimpo.setProperty("NOMEARQUIVO", " ");

					NativeSql query = new NativeSql(jdbc);
					query.appendSql("SELECT NUFIN FROM TGFFIN FIN WHERE EXISTS(SELECT 1 FROM TGFFIN FIN2 WHERE FIN2.NUFIN = :NUFIN AND FIN2.NUMNOTA = FIN.NUMNOTA AND FIN2.CODPARC = FIN.CODPARC AND FIN2.CODEMP = FIN.CODEMP) AND FIN.NUFIN <> :NUFIN AND FIN.NUMNOTA IS NOT NULL");
					query.setNamedParameter("NUFIN", nufin);

					ResultSet rs = query.executeQuery();
					while (rs.next()) {
						DynamicVO novo = voLimpo.buildClone();
						BigDecimal nuFinIrmao = rs.getBigDecimal("NUFIN");
						String chaveArquivo = vo.asString("CHAVEARQUIVO");
						novo.setProperty("PKREGISTRO", nuFinIrmao + "_Financeiro");
						novo.setProperty("CHAVEARQUIVO", chaveArquivo + "_" + nuFinIrmao);
						novo.setProperty("LINK", "/mge/download.mge?fileName=Repo:
						dwfFacade.createEntity(DynamicEntityNames.ANEXO_SISTEMA, (EntityVO) novo);
					}
				}
			}
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

}
