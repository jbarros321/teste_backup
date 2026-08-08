package br.com.denver.tsl.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

abstract class AbstractTSLRepository {

    protected interface ResultSetMapper<T> { T map(ResultSet rs) throws Exception; }

    protected <T> Set<T> executarQuery(String sql, BigDecimal nunota, ResultSetMapper<T> mapper) throws Exception {
        if (nunota == null) throw new IllegalArgumentException("NUNOTA não pode ser nulo.");

        Set<T> conjunto = new LinkedHashSet<>(1024);
        JdbcWrapper jdbc = null;
        NativeSql sqlNative = null;
        ResultSet rs = null;

        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            sqlNative = new NativeSql(jdbc);
            sqlNative.appendSql(sql).appendSql(" AND CAB.NUNOTA = :NUNOTA");
            sqlNative.setNamedParameter("NUNOTA", nunota);
            rs = sqlNative.executeQuery();
            while (rs.next()) conjunto.add(mapper.map(rs));
        } finally {
            Optional.ofNullable(rs).ifPresent(r -> { try { r.close(); } catch (Exception ignored) {} });
            fecharRecurso(sqlNative, NativeSql::releaseResources);
            fecharRecurso(jdbc, j -> JdbcWrapper.closeSession(j));
        }
        return conjunto;
    }

    private static <T> void fecharRecurso(T recurso, Consumer<T> closer) {
        try { Optional.ofNullable(recurso).ifPresent(closer); } catch (Exception ignored) {}
    }

    protected static Date toDate(Timestamp ts) {
        return ts != null ? new Date(ts.getTime()) : null;
    }
}
