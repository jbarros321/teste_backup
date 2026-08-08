package br.com.cliente.repository;

import br.com.sankhya.jape.sql.NativeSql;

@FunctionalInterface
public interface QueryExecutor<T> {
    T executar(NativeSql sqlNative) throws Exception;
}
