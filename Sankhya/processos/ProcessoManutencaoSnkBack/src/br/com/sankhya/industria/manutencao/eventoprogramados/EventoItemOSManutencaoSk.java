package br.com.sankhya.industria.manutencao.eventoprogramados;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.EntityDAO;
import br.com.sankhya.jape.dao.EntityPrimaryKey;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.sankhya.workflow.api.SankhyaFlow;
import br.com.sankhya.workflow.model.helper.ListaTarefaHelper;
import br.com.sankhya.workflow.utils.WorkflowUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class EventoItemOSManutencaoSk implements EventoProgramavelJava {

	public void beforeInsert(PersistenceEvent event) throws Exception {
	}

	public void beforeUpdate(PersistenceEvent event) throws Exception {
	}

	public void beforeDelete(PersistenceEvent event) throws Exception {
	}

	public void afterInsert(PersistenceEvent event) throws Exception {
		enviaSinalCompilacao((DynamicVO) event.getVo());
	}

	public void afterUpdate(PersistenceEvent event) throws Exception {
	}

	public void afterDelete(PersistenceEvent event) throws Exception {
	}

	public void beforeCommit(TransactionContext tranCtx) throws Exception {
		SessionHandle hnd = null;

		try{
			hnd = JapeSession.open();
			Collection<EntityPrimaryKey> itens = tranCtx.getInserted();
			for(EntityPrimaryKey itemPk:itens){
				DynamicVO vo = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO("ItemOrdemServico", itemPk);
				if(!iniciaProcesso(vo)){
					verificaSinalFechamentoSD(vo);
				}
			}

			itens = tranCtx.getUpdated();
			for(EntityPrimaryKey itemPk:itens){
				DynamicVO vo = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO("ItemOrdemServico", itemPk);
				verificaSinalFechamentoSD(vo);
			}
		} finally{
			JapeSession.close(hnd);
		}
	}

	private boolean usaAtendimentoFlow(JdbcWrapper jdbc, DynamicVO itemVO) throws Exception{

		BigDecimal codProd = itemVO.asBigDecimal("CODPROD");
		BigDecimal numOS = itemVO.asBigDecimal("NUMOS");
		BigDecimal numItem = itemVO.asBigDecimal("NUMITEM");
		Integer [] naoAtendidos = {30806,162518,30748,30756,30745,30675,30667,30346,20455,30362,20508,30603,30755,30424,30422};
		for (int value : naoAtendidos) {
			if (codProd.intValue() == value) {
				return false;
			}
		}

		NativeSql query = null;
		try{
			query = new NativeSql(jdbc);
			query.appendSql(" SELECT 1 FROM TGFPRO WHERE CODPROD = :CODPROD AND LINHA = 'WEB' AND CODGRUPOPROD NOT IN (1004) ");

			query.appendSql(" AND NOT EXISTS( SELECT 1 FROM TCSITE WHERE NUMITEM <> :NUMITEM AND NUMOS = :NUMOS AND ((CODSERV = 50605 AND CODUSU=46) OR CODSERV IN(50506, 50601))) AND ROWNUM = 1");
			query.setNamedParameter("CODPROD", codProd);
			query.setNamedParameter("NUMOS", numOS);
			query.setNamedParameter("NUMITEM", numItem);
			ResultSet rs = query.executeQuery();

			return rs.next();
		} finally {
			NativeSql.releaseResources(query);
		}
	}

	private boolean temSoliman(BigDecimal numOS) throws Exception{
		return NativeSql.getBigDecimal("NUMOS", "AD_TWFSOLIMAN", "NUMOS = ?", new Object [] { numOS }) != null;
	}

	private boolean ehUsuarioSD(BigDecimal codUsu) throws Exception{
		return NativeSql.getBigDecimal("CODUSU", "TSIUSU", "CODUSU = ? AND CODCENCUSPAD IN (10001303, 10001304, 1001104, 2001204)", new Object [] { codUsu }) != null;
	}

	private boolean iniciaProcesso(DynamicVO itemVO) throws Exception {
		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

		if (itemVO.asInt("CODUSU") == 46 && itemVO.asInt("CODSERV") == 50605){
			BigDecimal codPrn = BigDecimal.valueOf(MGECoreParameter.getParameterAsInt("CODPRNMANSNK"));

			if (codPrn == null) {

				return false;
			}

			if (!ehUsuarioSD(itemVO.asBigDecimalOrZero("CODUSUREM"))) {

				return false;
			}

			BigDecimal numOS = itemVO.asBigDecimalOrZero("NUMOS");

			if (temSoliman(numOS)) {

				return false;
			}

			JdbcWrapper jdbc = null;

			try {
				jdbc = dwfFacade.getJdbcWrapper();
				if(!usaAtendimentoFlow(jdbc, itemVO)){
					return false;
				}

				if(itemVO.asInt("CODOCOROS") != 6){
					throw new IllegalStateException("Para encaminhar OS ao processo de manuteno, o item do \"TESTE DE ENTRADA\" deve ser \"ERRO DE SISTEMA\".");
				}

				String processKey = WorkflowUtils.getProcessKeyByProcessId(codPrn);
				EntityDAO daoCab = DataDictionaryUtils.getDao(DynamicEntityNames.ORDEM_SERVICO, dwfFacade);
				EntityDAO daoIte = DataDictionaryUtils.getDao(DynamicEntityNames.ITEM_ORDEM_SERVICO, dwfFacade);

				DynamicVO cabVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.ORDEM_SERVICO, new Object [] {itemVO.getProperty("NUMOS")});

				String codProd = getStringElemento(daoIte, itemVO, "CODPROD");
				String codParc = getStringElemento(daoCab, cabVO, "CODPARC");

				Map<String, String> dados = new HashMap<String, String>();
				dados.put("INDCELPROD_AD001.NOMECELULA", "FLOW GERAL");
				dados.put("NUMCONTRATO", getStringElemento(daoCab, cabVO, "NUMCONTRATO"));
				dados.put("DESCSERV", getStringElemento(daoCab, cabVO, "DESCRICAO"));
				dados.put("DETALHEPROBLEMA", NativeSql.getString("I.SOLUCAO", "TCSITE I", "NUMITEM = 1 AND I.NUMOS = ?", new Object[] {numOS}));
				dados.put("CODCELPROD", getStringElemento(daoCab, cabVO, "AD_CELULALEAN"));
				dados.put("PRIORIDADE", getStringElemento(daoIte, itemVO, "PRIORIDADE"));
				dados.put("CODCONTATO", getStringElemento(daoCab, cabVO, "CODCONTATO"));
				dados.put("NUMOS", getStringElemento(daoCab, cabVO, "NUMOS"));
				dados.put("CODPARC", codParc);
				dados.put("Contrato.NOMEPARC", "PARCEIRO " + codParc);
				dados.put("CODPROD", codProd);
				dados.put("Produto.DESCRPROD", "SERVICO " + codProd);

				JsonArray records = new JsonArray();

				Set<Entry<String, String>> st = dados.entrySet();
				Iterator<Entry<String, String>> it = st.iterator();

				while (it.hasNext()) {
					Map.Entry<String, String> elemento = it.next();

					JsonObject record = new JsonObject();
					record.addProperty("name", elemento.getKey());
					record.addProperty("value", elemento.getValue());

					records.add(record);
				}

				JsonObject recordsJSON = new JsonObject();
				recordsJSON.add("record", records);

				JsonArray recordsArray = new JsonArray();
				recordsArray.add(recordsJSON);

				JsonObject formFormatado = new JsonObject();
				formFormatado.addProperty("entityName", "AD_TWFSOLIMAN");
				formFormatado.addProperty("parentEntity", "-99999999");
				formFormatado.add("records", recordsArray);

				JsonArray formFormatadoArray = new JsonArray();
				formFormatadoArray.add(formFormatado);

				new ListaTarefaHelper().startProcess(jdbc, codPrn, processKey, null, formFormatadoArray, null);
				return true;
			} finally {
				JdbcWrapper.closeSession(jdbc);
			}
		}
		return false;
	}

	private String getStringElemento(EntityDAO dao, DynamicVO entity, String elemento) throws Exception {
		return dao.getFieldAsString(entity.getProperty(elemento), elemento);
	}

	private boolean enviadoDaCompilacao(DynamicVO itemVO) throws Exception{
		BigDecimal remetente = itemVO.asBigDecimalOrZero("CODUSUREM");
		BigDecimal numOS = itemVO.asBigDecimal("NUMOS");

		if(remetente.intValue() == 1721){
			return true;
		}

		JdbcWrapper jdbc = null;
		NativeSql query = null;
		try{
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			query = new NativeSql(jdbc);

			query.appendSql("SELECT 1 FROM TCSITE ITE WHERE ");
			query.appendSql("	 NUMOS = :NUMOS ");
			query.appendSql("	 AND ITE.CODUSU = 1721 ");
			query.appendSql("	 AND ITE.INICEXEC IS NOT NULL ");
			query.appendSql("	 AND ITE.HRINICIAL IS NOT NULL ");
			query.appendSql("	 AND ITE.HRFINAL IS NOT NULL ");

			query.appendSql("    AND EXISTS(SELECT 1 FROM TCSITE I WHERE ");
			query.appendSql("         I.NUMOS = ITE.NUMOS ");
			query.appendSql("         AND CODUSU = :REMETENTE ");
			query.appendSql("         AND CODSERV = 50603");
			query.appendSql("         AND INICEXEC IS NOT NULL ");
			query.appendSql("         AND HRINICIAL IS NOT NULL ");
			query.appendSql("         AND HRFINAL IS NOT NULL ");
			query.appendSql("    ) ");

			query.setNamedParameter("NUMOS", numOS);
			query.setNamedParameter("REMETENTE", remetente);

			return query.executeQuery().next();
		} finally {
			NativeSql.releaseResources(query);
			JdbcWrapper.closeSession(jdbc);
		}
	}

	private void enviaSinalCompilacao(DynamicVO itemVO) throws Exception {
		if (enviadoDaCompilacao(itemVO)) {
			EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

			BigDecimal numOS = itemVO.asBigDecimal("NUMOS");

			FinderWrapper finder = new FinderWrapper("AD_TWFSOLIMAN", "this.NUMOS = ?", new Object [] {numOS});
			finder.setOrderBy("IDINSTPRN DESC");
			Collection<PersistentLocalEntity> soliMan = dwfFacade.findByDynamicFinder(finder);

			if (soliMan.size() > 0) {
				PersistentLocalEntity pleSoliMan = soliMan.iterator().next();
				DynamicVO soliManVO = (DynamicVO) pleSoliMan.getValueObject();

				BigDecimal destinatario = itemVO.asBigDecimal("CODUSU");
				BigDecimal remetente = NativeSql.getBigDecimal("CODUSUREM", "TCSITE", "NUMOS = ? AND CODUSU = 1721 ORDER BY NUMITEM DESC", new Object [] {numOS});

				BigDecimal remetenteAtual = itemVO.asBigDecimalOrZero("CODUSUREM");
				BigDecimal idInstPrn = soliManVO.asBigDecimal("IDINSTPRN");
				if(destinatario.longValue() == 2052 && remetenteAtual.longValue() == 1721){

					SankhyaFlow.sendSignal("COMPILACAO_RELEASE_EFETUADA", new String [] {idInstPrn.toString()});
					boolean aguardandoSinalAntigo = NativeSql.getBigDecimal("COUNT(1)", "CMD_ACT_HI_ACTINST", "ACT_ID_ = 'IntermediateThrowEvent_00yxxbk' AND END_TIME_ IS NULL AND PROC_INST_ID_ = ?", new Object [] {idInstPrn}).intValue() > 0;
					if(aguardandoSinalAntigo){
						SankhyaFlow.sendSignal("COMPILACAO_EFETUADA", new String [] {idInstPrn.toString()});
					}
				} else {

					soliManVO.setProperty("COMPQUEBRADA", destinatario.compareTo(remetente) == 0 ? "S" : "N");
					soliManVO.setProperty("RETORNOCOMPILADOR", itemVO.getProperty("SOLUCAO"));
					pleSoliMan.setValueObject((EntityVO) soliManVO);

					SankhyaFlow.sendSignal("COMPILACAO_EFETUADA", new String [] {idInstPrn.toString()});
				}
			}
		}
	}

	private void verificaSinalFechamentoSD(DynamicVO itemVO) throws Exception {
		BigDecimal numOS = itemVO.asBigDecimal("NUMOS");

		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		FinderWrapper finder = new FinderWrapper("AD_TWFSOLIMAN", "this.NUMOS = ?", new Object [] {numOS});
		finder.setOrderBy("IDINSTPRN DESC");

		Collection<PersistentLocalEntity> soliMan = dwfFacade.findByDynamicFinder(finder);

		if (soliMan.size() > 0) {
			PersistentLocalEntity pleSoliMan = soliMan.iterator().next();
			DynamicVO soliManVO = (DynamicVO) pleSoliMan.getValueObject();

			boolean reagir = false;
			String situacao = NativeSql.getString("SITUACAO", "TCSOSE", "NUMOS = ?", new Object [] {numOS});

			if ("F".equals(situacao)) {

				soliManVO.setProperty("REVAL", "S");
				reagir = true;
			} else {
				BigDecimal codUsu = itemVO.asBigDecimal("CODUSU");
				BigDecimal codServ = itemVO.asBigDecimal("CODSERV");

				if ((new BigDecimal(46)).compareTo(codUsu) == 0 && (new BigDecimal(50605)).compareTo(codServ) == 0) {

					soliManVO.setProperty("REVAL", "N");
					reagir = true;
				}
			}

			if (reagir) {
				pleSoliMan.setValueObject((EntityVO) soliManVO);
				SankhyaFlow.sendSignal("ACAO_SD_EXECUTADA", new String [] {soliManVO.asBigDecimal("IDINSTPRN").toString()});
			}
		}
	}
}
