package br.com.sankhya.industria.manutencao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class MigrarInstancias implements AcaoRotinaJava {

	public void doAction(ContextoAcao ctx) throws Exception {
		NativeSql query = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());

		ProcessEngine pe = ProcessEngines.getDefaultProcessEngine();
		RuntimeService rs = pe.getRuntimeService();

		MigrationPlanBuilder migrationPlanBuilder = rs.createMigrationPlan("processo_224:44:734827", "processo_224:45:735000");

		ResultSet resSet = query.executeQuery("SELECT DISTINCT(ACT_ID_) AS ACTIVITY FROM CMD_ACT_HI_ACTINST WHERE PROC_DEF_KEY_ = 'processo_224' AND PROC_DEF_ID_ LIKE 'processo_224:44%'");
		while(resSet.next()){
			String activity = resSet.getString("ACTIVITY");
			migrationPlanBuilder.mapActivities(activity,activity);
		}
		resSet.close();

		resSet = query.executeQuery("SELECT IDINSTPRN FROM TWFIPRN WHERE CODPRN = 224 AND VERSAO = 44");

		List<String> processInstanceIds = new ArrayList<String>();
		while(resSet.next()){
			processInstanceIds.add(resSet.getString("IDINSTPRN"));
		}

		if(!processInstanceIds.isEmpty()){
			rs.newMigration(migrationPlanBuilder.build())
			  .processInstanceIds(processInstanceIds)
			  .execute();
			query.executeUpdate("UPDATE TWFIPRN SET VERSAO = 45 WHERE CODPRN = 224 AND VERSAO = 44");
		}
	}

}
