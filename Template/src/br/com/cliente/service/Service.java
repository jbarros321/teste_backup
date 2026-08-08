package br.com.cliente.service;

import java.math.BigDecimal;

public interface Service {
    String gerarArquivo(String caminhoExportacao, BigDecimal nunota) throws Exception;
}
