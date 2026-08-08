package br.com.satyacode.satyapass.integracaovalecard.rotinas.notas.repository;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.ListenerParameters;

import java.math.BigDecimal;

public class NotaRepository {

    public void deletarNota(BigDecimal nunota) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);
            JapeWrapper empresaDAO = JapeFactory.dao("CabecalhoNota");
            empresaDAO.delete(nunota);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }
}
