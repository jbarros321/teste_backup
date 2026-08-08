package br.com.cliente.repository;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetExtractor<T> {
    T extract(ResultSet rs) throws Exception;
}
