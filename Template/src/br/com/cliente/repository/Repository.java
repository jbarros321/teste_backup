package br.com.cliente.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Set;

public interface Repository {
    <T> Set<T> executarQuery(String sql, BigDecimal nunota, ResultSetMapper<T> mapper) throws Exception;

    <T> Set<T> executarQueryComParametros(String sql, ResultSetMapper<T> mapper, SqlConfigurator configurador) throws Exception;

    <T> T executarQueryCustomizada(QueryExecutor<T> executor) throws Exception;

    <T> T executarQueryUnica(String sql, SqlConfigurator configurador, ResultSetExtractor<T> extractor) throws Exception;

    BigDecimal executarQueryValorUnico(String sql, SqlConfigurator configurador) throws Exception;

    String executarQueryStringUnica(String sql, SqlConfigurator configurador) throws Exception;

    Timestamp executarQueryTimestampUnico(String sql, SqlConfigurator configurador) throws Exception;
}
