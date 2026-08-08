package br.com.satyacode.satyapass.acessodados.crud;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;

import java.lang.annotation.Native;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class NativeSqlExemplo {

    public void buscarInformacaoDeUmaColuna() throws Exception {

        BigDecimal qtd = NativeSql.getBigDecimal("COUNT(*)", "TGFPAR", "TIPPESSOA = 'F'");
        Timestamp dataAlteracao = NativeSql.getTimestamp("DTALTER", "TGFPAR", "CODPARC = ?" , new Object[]{new BigDecimal(40)});
    }

    private void fazerConsulta() throws MGEModelException {
        EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = entityFacade.getJdbcWrapper();
        try{
            jdbc.openSession();
            NativeSql nativeSql = new NativeSql(jdbc);
            nativeSql.appendSql("SELECT * FROM TSIUSU WHERE CODUSU = :CODUSU");
            nativeSql.setNamedParameter("CODUSU", BigDecimal.ZERO);
            ResultSet rs = nativeSql.executeQuery();
            while (rs.next()){
                System.out.println(rs.getBigDecimal("CODUSU"));
            }

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            jdbc.closeSession();
        }
    }

    private void fazerConsultaSQLExterno() throws MGEModelException {
        EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = entityFacade.getJdbcWrapper();
        try{
            jdbc.openSession();
            NativeSql nativeSql = new NativeSql(jdbc);

            nativeSql.loadSql(NativeSqlExemplo.class, "queConsulta.sql");
            nativeSql.setNamedParameter("CODUSU",BigDecimal.ZERO );
            ResultSet rs = nativeSql.executeQuery();
            while (rs.next()) {
                rs.getBigDecimal("NOMEUSU");
            }
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            jdbc.closeSession();
        }
    }

    public BigDecimal incluirDados(String nome) throws Exception {
        EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
        BigDecimal seqeuncia = NativeSql.getBigDecimal("MAX(SEQUENCIA) + 1", "AD_STAACESSO", "1=1");
        JdbcWrapper jdbc = entityFacade.getJdbcWrapper();
        try{
            jdbc.openSession();
            NativeSql nativeSql = new NativeSql(jdbc);
            StringBuffer sql = new StringBuffer();
            sql.append("INSERT INTO AD_STAACESSO (SEQUENCIA, NOME)")
                    .append("VALUES ( ").append(String.valueOf(seqeuncia)).append(", '")
                    .append(nome).append("')");
            System.out.println(sql.toString());
            nativeSql.executeUpdate(sql.toString());
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            jdbc.closeSession();
        }
        return seqeuncia;
    }

    public void atualizar(BigDecimal sequencia) throws Exception {
        EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = entityFacade.getJdbcWrapper();
        try{
            jdbc.openSession();
            NativeSql nativeSql = new NativeSql(jdbc);
            StringBuffer sql = new StringBuffer();
            sql.append("UPDATE AD_STAACESSO SET DATA = SYSDATE, VALOR = ").append(sequencia).
                    append(" WHERE SEQUENCIA = ").append(sequencia);
            System.out.println(sql.toString());
            nativeSql.executeUpdate(sql.toString());
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            jdbc.closeSession();
        }
    }

    public void remover(BigDecimal sequencia) throws Exception {
        EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = entityFacade.getJdbcWrapper();
        try{
            jdbc.openSession();
            NativeSql nativeSql = new NativeSql(jdbc);
            StringBuffer sql = new StringBuffer();
            sql.append("DELETE FROM AD_STAACESSO ").
                    append(" WHERE SEQUENCIA = ").append(sequencia);
            System.out.println(sql.toString());
            nativeSql.executeUpdate(sql.toString());
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            jdbc.closeSession();
        }
    }

}
