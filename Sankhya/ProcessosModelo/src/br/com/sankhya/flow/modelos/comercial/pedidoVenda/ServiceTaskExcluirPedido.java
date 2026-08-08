package br.com.sankhya.flow.modelos.comercial.pedidoVenda;

import br.com.sankhya.extensions.flow.ContextoTarefa;
import br.com.sankhya.extensions.flow.TarefaJava;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ServiceTaskExcluirPedido implements TarefaJava {

	public void executar(ContextoTarefa contexto) throws Exception {
		NativeSql nativeSql = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());
		nativeSql.setNamedParameter("NUNOTA", contexto.getCampo("NUNOTA"));
		nativeSql.executeUpdate("DELETE FROM TSILIB WHERE NUCHAVE = :NUNOTA AND TABELA IN ('TGFCAB', 'TGFITE')");
		nativeSql.executeUpdate("DELETE FROM TGFITE WHERE NUNOTA = :NUNOTA");
		nativeSql.executeUpdate("DELETE FROM TGFCAB WHERE NUNOTA = :NUNOTA");
	}
}
