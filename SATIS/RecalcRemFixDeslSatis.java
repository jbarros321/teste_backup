package br.com.satis.extensions;

import java.math.BigDecimal;
import java.sql.ResultSet;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.MGEModelException;

/**
 * Versão Java da Procedure STP_RECALCREMFIXDESLSATIS
 * Implementada como Ação Java para o Sankhya Om.
 */
public class RecalcRemFixDeslSatis implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        
        // No contexto de Ação Java, iteramos pelos registros selecionados
        for (Registro reg : ctx.getLinhas()) {
            
            BigDecimal nuFech = (BigDecimal) reg.getCampo("NUFECH");
            BigDecimal sequencia = (BigDecimal) reg.getCampo("SEQUENCIA");
            
            if (nuFech == null || sequencia == null) {
                continue;
            }

            JdbcWrapper jdbc = null;
            NativeSql sql = null;
            ResultSet rs = null;

            try {
                jdbc = JapeFactory.getEntityFacade().getJdbcWrapper();
                sql = new NativeSql(jdbc);

                sql.appendSql("SELECT DESLIGADO, DIASTRAB, REMFIXA FROM DBFECHCOMFIN WHERE SEQUENCIA = :SEQ AND NUFECH = :NUFECH");
                sql.setNamedParameter("SEQ", sequencia);
                sql.setNamedParameter("NUFECH", nuFech);
                
                rs = sql.executeQuery();

                if (rs.next()) {
                    String desligado = rs.getString("DESLIGADO");
                    BigDecimal diasTrab = rs.getBigDecimal("DIASTRAB");
                    BigDecimal remFixa = rs.getBigDecimal("REMFIXA");

                    // Lógica: IF P_DESLIGADO = 'SIM' AND P_REMFIXA > 0 THEN
                    // Lógica Final da Procedure: (P_REMFIXA / 30) * P_DIASTRAB
                    if ("SIM".equalsIgnoreCase(desligado) && remFixa != null && remFixa.compareTo(BigDecimal.ZERO) > 0) {
                        
                        BigDecimal dias = diasTrab != null ? diasTrab : BigDecimal.ZERO;
                        BigDecimal vlrDiario = remFixa.divide(new BigDecimal(30), 10, RoundingMode.HALF_UP);
                        BigDecimal remFixa2 = vlrDiario.multiply(dias).setScale(2, RoundingMode.HALF_UP);
                        
                        // Executa o Update no Banco (Conforme Procedure SQL)
                        sql.clean();
                        sql.appendSql("UPDATE AD_DBFECHCOMFIN SET REMFIXA = :VAL WHERE SEQUENCIA = :SEQ AND NUFECH = :NUFECH");
                        sql.setNamedParameter("VAL", remFixa2);
                        sql.setNamedParameter("SEQ", sequencia);
                        sql.setNamedParameter("NUFECH", nuFech);
                        sql.executeUpdate();

                        // Atualiza na tela do usuário também (se o botão for na grade)
                        reg.setCampo("REMFIXA", remFixa2);
                    }
                }
            } finally {
                if (rs != null) rs.close();
                if (sql != null) NativeSql.releaseResources(sql);
            }
        }
        
        ctx.setMensagemRetorno("Recálculo processado com sucesso.");
    }
}
