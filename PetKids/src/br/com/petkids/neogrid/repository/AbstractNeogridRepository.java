package br.com.petkids.neogrid.repository;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.sql.Timestamp;

public abstract class AbstractNeogridRepository {
    protected <T> T executarConsulta(ConsultaCallback<T> callback) throws Exception {
        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        NativeSql sqlNative = null;
        try {
            jdbc.openSession();
            sqlNative = new NativeSql(jdbc);
            return callback.executar(sqlNative);
        } finally {
            if (sqlNative != null) NativeSql.releaseResources(sqlNative);
            if (jdbc != null) JdbcWrapper.closeSession(jdbc);
        }
    }

    protected <T> T executarConsultaComJdbc(ConsultaComJdbcCallback<T> callback) throws Exception {
        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        NativeSql sqlNative = null;
        try {
            jdbc.openSession();
            sqlNative = new NativeSql(jdbc);
            return callback.executar(sqlNative, jdbc);
        } finally {
            if (sqlNative != null) NativeSql.releaseResources(sqlNative);
            if (jdbc != null) JdbcWrapper.closeSession(jdbc);
        }
    }

    protected Timestamp ajustarPeriodoFinal(Timestamp periodoFin) {
        if (periodoFin == null) return null;
        return new Timestamp(periodoFin.getTime() + 86399999 - (periodoFin.getTime() % 86400000));
    }

    protected interface ConsultaCallback<T> {
        T executar(NativeSql sqlNative) throws Exception;
    }

    protected interface ConsultaComJdbcCallback<T> {
        T executar(NativeSql sqlNative, JdbcWrapper jdbc) throws Exception;
    }
}
