package br.com.sankhya.industria.manutencao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.sankhya.util.StringUtils;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.PersistenceException;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.workflow.model.bpmn.DefaultBpmnModel;
import br.com.sankhya.workflow.model.bpmn.Elemento;
import br.com.sankhya.workflow.model.bpmn.ProcessoNegocio;

public class RecriaExpressaoTWFELE implements AcaoRotinaJava {

	public void doAction(ContextoAcao ctx) throws Exception {

		JdbcWrapper jdbc = null;
		try {

			EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
			jdbc = entityFacade.getJdbcWrapper();

			NativeSql query = new NativeSql(jdbc);

			query.appendSql(" SELECT PRN.CODPRN, PRN.VERSAO, ELE.IDELEMENTO, ELE.NUELE ");
			query.appendSql("  FROM TWFPRN PRN INNER JOIN TWFELE ELE ON ELE.CODPRN = PRN.CODPRN AND ELE.VERSAO = PRN.VERSAO   ");
			query.appendSql(" WHERE ELE.EXPRESSCANDITADE IS NOT NULL ");

			NativeSql updateQuery = new NativeSql(jdbc);
			updateQuery.appendSql(" UPDATE TWFELE SET EXPRESSCANDITADE = :EXPRESSCANDITADE WHERE NUELE = :NUELE");
			updateQuery.setReuseStatements(true);

			Map<String, ProcessoNegocio> instancieProcessos = new HashMap<String, ProcessoNegocio>();
			ResultSet rs = query.executeQuery();
			while (rs.next()) {
				BigDecimal codPrn = rs.getBigDecimal("CODPRN");
				BigDecimal versao = rs.getBigDecimal("VERSAO");
				BigDecimal nuele = rs.getBigDecimal("NUELE");
				String idelemento = rs.getString("IDELEMENTO");

				ProcessoNegocio processo = instancieProcessos.get(codPrn + "_" + versao);
				if (processo == null) {
					processo = getInstanciepProcesso(entityFacade, codPrn, versao);
					instancieProcessos.put(codPrn + "_" + versao, processo);
				}

				for (Elemento element : processo.getElementos()) {

					if (idelemento.equals(element.getIdElemento())) {
						updateQuery.cleanParameters();

						updateQuery.setNamedParameter("NUELE", nuele);

						updateQuery.setNamedParameter("EXPRESSCANDITADE", element.getExpressaoCanditado());

						updateQuery.executeUpdate();

						break;
					}
				}
			}
		} finally {
			JdbcWrapper.closeSession(jdbc);
		}

	}

	private ProcessoNegocio getInstanciepProcesso(EntityFacade entityFacade, BigDecimal codPrn, BigDecimal versao) throws Exception, PersistenceException {
		PersistentLocalEntity persistentProcess = entityFacade.findEntityByPrimaryKey(DynamicEntityNames.PROCESSO_NEGOCIO, new Object[] { codPrn, versao });
		DynamicVO processoNegocioVO = (DynamicVO) persistentProcess.getValueObject();

		String xml = StringUtils.getEmptyAsNull(processoNegocioVO.asString("XMLBPMN"));

		String xmlBpmn = DefaultBpmnModel.getInstance().prepareXMLReading(xml);

		ProcessoNegocio processo = DefaultBpmnModel.getInstance().getProcessMetadata(xmlBpmn, codPrn, versao);
		return processo;
	}
}
