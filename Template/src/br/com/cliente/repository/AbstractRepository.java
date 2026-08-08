package br.com.cliente.repository;
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

abstract class AbstractRepository implements Repository {

    @Override
    public <T> Set<T> executarQuery(String sql, BigDecimal nunota, ResultSetMapper<T> mapper) throws Exception {
        if (nunota == null) throw new IllegalArgumentException("NUNOTA não pode ser nulo.");
        return executarQueryComParametros(sql, mapper, s -> { s.appendSql(" AND CAB.NUNOTA = :NUNOTA"); s.setNamedParameter("NUNOTA", nunota); });
    }

    @Override
    public <T> T executarQueryCustomizada(QueryExecutor<T> executor) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sqlNative = null;
        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            sqlNative = new NativeSql(jdbc);
            return executor.executar(sqlNative);
        } finally {
            fecharRecurso(sqlNative, NativeSql::releaseResources);
            fecharRecurso(jdbc, j -> JdbcWrapper.closeSession(j));
        }
    }

    @Override
    public <T> Set<T> executarQueryComParametros(String sql, ResultSetMapper<T> mapper, SqlConfigurator configurador) throws Exception {
        Set<T> conjunto = new LinkedHashSet<>(1024);
        JdbcWrapper jdbc = null;
        NativeSql sqlNative = null;
        ResultSet rs = null;
        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            sqlNative = new NativeSql(jdbc);
            sqlNative.appendSql(sql);
            if (configurador != null) configurador.configurar(sqlNative);
            rs = sqlNative.executeQuery();
            while (rs.next()) conjunto.add(mapper.map(rs));
        } finally {
            fecharRecurso(rs, r -> { try { r.close(); } catch (Exception ignored) {} });
            fecharRecurso(sqlNative, NativeSql::releaseResources);
            fecharRecurso(jdbc, j -> JdbcWrapper.closeSession(j));
        }
        return conjunto;
    }

    @Override
    public <T> T executarQueryUnica(String sql, SqlConfigurator configurador, ResultSetExtractor<T> extractor) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sqlNative = null;
        ResultSet rs = null;
        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            sqlNative = new NativeSql(jdbc);
            sqlNative.appendSql(sql);
            if (configurador != null) configurador.configurar(sqlNative);
            rs = sqlNative.executeQuery();
            return rs.next() ? extractor.extract(rs) : null;
        } finally {
            fecharRecurso(rs, r -> { try { r.close(); } catch (Exception ignored) {} });
            fecharRecurso(sqlNative, NativeSql::releaseResources);
            fecharRecurso(jdbc, j -> JdbcWrapper.closeSession(j));
        }
    }

    @Override
    public BigDecimal executarQueryValorUnico(String sql, SqlConfigurator configurador) throws Exception { return executarQueryUnica(sql, configurador, rs -> rs.getBigDecimal(1)); }
    @Override
    public String executarQueryStringUnica(String sql, SqlConfigurator configurador) throws Exception { return executarQueryUnica(sql, configurador, rs -> rs.getString(1)); }
    @Override
    public Timestamp executarQueryTimestampUnico(String sql, SqlConfigurator configurador) throws Exception { return executarQueryUnica(sql, configurador, rs -> rs.getTimestamp(1)); }

    private static <T> void fecharRecurso(T recurso, Consumer<T> closer) { Optional.ofNullable(recurso).ifPresent(closer); }
    protected static Date toDate(Timestamp ts) { return Optional.ofNullable(ts).map(t -> new Date(t.getTime())).orElse(null); }
    protected static String formatarCnpj(String cnpj) { return Optional.ofNullable(cnpj).map(c -> c.replaceAll("[^0-9]", "")).orElse(""); }
    protected static String formatarCnpjCompleto(String cnpj) { return Optional.ofNullable(cnpj).map(c -> c.replace(".", "").replace("/", "").replace("-", "").replace(" ", "")).orElse(""); }
}
