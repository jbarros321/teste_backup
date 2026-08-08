package br.com.sankhya.action.acaoAgendada;

import br.com.sankhya.action.funcoes.EnviaPedido;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.sql.ResultSet;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

public class EnviaNotasConfirmadas
implements ScheduledAction {
    public void onTime(ScheduledActionContext scheduledActionContext) {
        try {
            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            JdbcWrapper jdbc = dwfEntityFacade.getJdbcWrapper();
            NativeSql sql = new NativeSql(jdbc);
            EnviaPedido enviaPedido = new EnviaPedido();
            String query = "SELECT CAB.NUNOTA AS NUNOTA\nFROM TGFCAB CAB\n         INNER JOIN TGFTOP TOPP ON TOPP.CODTIPOPER = CAB.CODTIPOPER AND TOPP.DHALTER = CAB.DHTIPOPER\n         LEFT JOIN AD_LOGMEG MEG ON MEG.NUNOTA = CAB.NUNOTA\nWHERE CAB.CODPARCTRANSP IS NOT NULL\n  AND CAB.CODPARCTRANSP > 0\n  AND TOPP.AD_MEGENVIADOC = 'S' \nAND MEG.NUNOTA IS NULL";
            ResultSet buscaNotasConfirmadas = sql.executeQuery(query);
            while (buscaNotasConfirmadas.next()) {
                BigDecimal nunota = buscaNotasConfirmadas.getBigDecimal("NUNOTA");
                enviaPedido.enviaPedidoMegleo(nunota, false);
            }
        }
        catch (Exception var3) {
            System.out.println(var3.toString());
        }
    }
}
