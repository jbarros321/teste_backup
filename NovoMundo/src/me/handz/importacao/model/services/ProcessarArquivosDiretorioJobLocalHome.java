package me.handz.importacao.model.services;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface ProcessarArquivosDiretorioJobLocalHome extends EJBLocalHome {

    ProcessarArquivosDiretorioJobLocal create() throws CreateException;
}
