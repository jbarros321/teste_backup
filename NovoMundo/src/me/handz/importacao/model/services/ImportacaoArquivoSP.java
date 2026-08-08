package me.handz.importacao.model.services;

import javax.ejb.EJBLocalObject;

public interface ImportacaoArquivoSP extends EJBLocalObject {

    String processarArquivosDiretorio(String idTela);

    String alterarLancadorTela(String idTela);

    String voltarLancadorTela(String idTela);

    boolean validarConfiguracao(String idTela);

    String buscarLogsImportacao(String idTela);
}
