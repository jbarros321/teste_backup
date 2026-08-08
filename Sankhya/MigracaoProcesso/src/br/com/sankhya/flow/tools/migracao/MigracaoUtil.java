package br.com.sankhya.flow.tools.migracao;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.xml.sax.InputSource;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class MigracaoUtil {

	public static Map<String, BPMElement> getElementosProcesso(Object codPrn, Object versao) throws Exception {

		NativeSql query = null;
		JdbcWrapper jdbc = null;
		InputStream is = null;

		Document doc = null;
		try {
			query = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());
			query.appendSql("SELECT XMLBPMN FROM TWFPRN WHERE CODPRN = :CODPRN AND VERSAO = :VERSAO");
			query.setNamedParameter("CODPRN", codPrn);
			query.setNamedParameter("VERSAO", versao);
			ResultSet rs = query.executeQuery();
			if (rs.next()) {
				is = rs.getBinaryStream("XMLBPMN");
				InputSource source = new InputSource(is);
				source.setEncoding("ISO-8859-1");
				SAXBuilder saxBuilder = new SAXBuilder();

				doc = saxBuilder.build(source);
			}
			rs.close();
		} finally {
			if (is != null) {
				is.close();
			}
			NativeSql.releaseResources(query);
			JdbcWrapper.closeSession(jdbc);
		}

		StringBuffer buf = new StringBuffer();
		buf.append("/bpmn:definitions/bpmn:process/bpmn:startEvent");
		buf.append("|/bpmn:definitions/bpmn:process/bpmn:userTask");
		buf.append("|/bpmn:definitions/bpmn:process/bpmn:serviceTask");
		buf.append("|/bpmn:definitions/bpmn:process/bpmn:exclusiveGateway");
		buf.append("|/bpmn:definitions/bpmn:process/bpmn:parallelGateway");
		buf.append("|/bpmn:definitions/bpmn:process/bpmn:intermediateCatchEvent");
		buf.append("|/bpmn:definitions/bpmn:process/bpmn:noneEndEvent");

		Namespace ns = Namespace.getNamespace("bpmn", "http:
		XPath filterXpression = XPath.newInstance(buf.toString());

		filterXpression.addNamespace(ns);
		Map<String, BPMElement> elements = new LinkedHashMap<String, BPMElement>();
		for (Element e : (List<Element>) filterXpression.selectNodes(doc)) {
			BPMElement b = BPMElement.fromElement(e);
			elements.put(b.getId(), b);
		}

		return elements;
	}

	public static void migrarInstancias(BigDecimal nuPlano, BigDecimal codPrnAtual, BigDecimal versaoAtual, BigDecimal codPrnNovo, BigDecimal versaoNovo) throws Exception {
		NativeSql query = null;
		JdbcWrapper jdbc = null;

		List<String> instancias = new ArrayList<String>();
		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			query = new NativeSql(jdbc);
			query.setNamedParameter("CODPRNATUAL", codPrnAtual);
			query.setNamedParameter("VERSAOATUAL", versaoAtual);
			ResultSet rs = query.executeQuery("SELECT IDINSTPRN FROM TWFIPRN WHERE CODPRN = :CODPRNATUAL AND VERSAO = :VERSAOATUAL AND DHCONCLUSAO IS NULL");
			while (rs.next()) {
				instancias.add(rs.getString("IDINSTPRN"));
			}
		} finally {
			NativeSql.releaseResources(query);
			JdbcWrapper.closeSession(jdbc);
		}

		migrarInstancias(nuPlano, codPrnAtual, versaoAtual, codPrnNovo, versaoNovo, instancias);
	}

	public static void migrarInstancias(BigDecimal nuPlano, BigDecimal codPrnAtual, BigDecimal versaoAtual, BigDecimal codPrnNovo, BigDecimal versaoNovo, List<String> instancias) throws Exception {
		if (!instancias.isEmpty()) {
			NativeSql query = null;
			JdbcWrapper jdbc = null;
			try {
				EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
				jdbc = entityFacade.getJdbcWrapper();
				query = new NativeSql(jdbc);

				ProcessEngine pe = ProcessEngines.getDefaultProcessEngine();
				RuntimeService rs = pe.getRuntimeService();

				String processoAntigo = getIdPrn(codPrnAtual, versaoAtual);
				String processoNovo = getIdPrn(codPrnNovo, versaoNovo);

				MigrationPlanBuilder migrationPlanBuilder = rs.createMigrationPlan(processoAntigo, processoNovo);

				query.setNamedParameter("NUPLANO", nuPlano);
				ResultSet resSet = query.executeQuery("SELECT ATIVIDADEATUAL, ATIVIDADENOVO FROM AD_MAPAMPRN WHERE NUPLANO = :NUPLANO");

				while (resSet.next()) {
					String origem = resSet.getString("ATIVIDADEATUAL");
					String destino = resSet.getString("ATIVIDADENOVO");
					if(origem != null && destino != null){

						migrationPlanBuilder.mapActivities(origem, destino);
					}
				}
				resSet.close();

				Timestamp dhMigracao = TimeUtils.getNow();

				rs.newMigration(migrationPlanBuilder.build()).processInstanceIds(instancias).execute();

				BigDecimal codUsuLogado = getUserLogado();

				FinderWrapper finder = new FinderWrapper("InstanciaProcesso", "this.IDINSTPRN inCollection[]", new Object[] { instancias });
				EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
				for (PersistentLocalEntity ple : (Collection<PersistentLocalEntity>) dwfFacade.findByDynamicFinder(finder)) {
					DynamicVO vo = (DynamicVO) ple.getValueObject();
					vo.setProperty("CODPRN", codPrnNovo);
					vo.setProperty("VERSAO", versaoNovo);
					ple.setValueObject((EntityVO) vo);

					DynamicVO historicoVO = (DynamicVO) entityFacade.getDefaultValueObjectInstance("AD_HISMPRN");
					historicoVO.setProperty("IDINSTPRN", vo.getProperty("IDINSTPRN"));
					historicoVO.setProperty("CODPRNATUAL", codPrnAtual);
					historicoVO.setProperty("VERSAOATUAL", versaoAtual);
					historicoVO.setProperty("CODPRNNOVO", codPrnNovo);
					historicoVO.setProperty("VERSAONOVO", versaoNovo);
					historicoVO.setProperty("DHMIGRACAO", dhMigracao);
					historicoVO.setProperty("CODUSUMIGRA", codUsuLogado);

					entityFacade.createEntity("AD_HISMPRN", (EntityVO) historicoVO);
				}
			} finally {
				NativeSql.releaseResources(query);
				JdbcWrapper.closeSession(jdbc);
			}
		}
	}

	private static String getIdPrn(BigDecimal codPrn, BigDecimal versao) throws Exception {
		return NativeSql.getString("ID_", "CMD_ACT_RE_PROCDEF", "KEY_ = 'processo_" + codPrn + "' AND version_ = " + versao);
	}

	public static Map<String, BPMElement> getElementosInstanciaProcesso(BigDecimal idInstPrn) throws Exception {

		NativeSql query = null;
		JdbcWrapper jdbc = null;

		Map<String, BPMElement> elements = new LinkedHashMap<String, BPMElement>();
		try {
			query = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());
			query.appendSql("SELECT * FROM CMD_ACT_HI_ACTINST WHERE PROC_INST_ID_ = :IDINSTPRN AND END_TIME_ IS NULL");
			query.setNamedParameter("IDINSTPRN", idInstPrn);
			ResultSet rs = query.executeQuery();
			while (rs.next()) {
				BPMElement e = new BPMElement(rs.getString("ACT_TYPE_"), rs.getString("ACT_NAME_"), rs.getString("ACT_ID_"));
				elements.put(e.getId(), e);
			}
			rs.close();
		} finally {
			NativeSql.releaseResources(query);
			JdbcWrapper.closeSession(jdbc);
		}

		return elements;
	}

	public static BigDecimal getUserLogado() {
		AuthenticationInfo authInfo = AuthenticationInfo.getCurrentOrNull();
		return authInfo != null ? authInfo.getUserID() : BigDecimal.ZERO;
	}
}
