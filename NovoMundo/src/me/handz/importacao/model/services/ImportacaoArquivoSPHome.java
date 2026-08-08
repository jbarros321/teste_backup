package me.handz.importacao.model.services;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface ImportacaoArquivoSPHome extends EJBLocalHome {

    ImportacaoArquivoSP create() throws CreateException;
}
