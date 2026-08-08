package br.com.cliente.repository;

import br.com.sankhya.jape.sql.NativeSql;

@FunctionalInterface
public interface SqlConfigurator {
    void configurar(NativeSql sql) throws Exception;
}
