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
		query.executeUpdate("UPDATE TWFITAR SET IDELEMENTO = 'UserTask_1nrezqg' WHERE IDINSTPRN = 683185 AND IDINSTTAR = 704438");

	}

}
