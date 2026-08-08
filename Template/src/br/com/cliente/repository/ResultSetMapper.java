package br.com.cliente.repository;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetMapper<T> {
    T map(ResultSet rs) throws Exception;
}
