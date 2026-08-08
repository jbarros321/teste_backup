package me.handz.importacao.model.services;

import javax.ejb.Local;

@Local
public interface ProcessarArquivosDiretorioJobLocal {

    void executarProcessamento();
}
