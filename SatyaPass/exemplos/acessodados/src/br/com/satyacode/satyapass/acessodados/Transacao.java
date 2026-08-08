package br.com.satyacode.satyapass.acessodados;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.modelcore.MGEModelException;

public class Transacao {

    public void transacaoManual() throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            hnd.setCanTimeout(false);

            hnd.execWithTX(new JapeSession.TXBlock() {
                public void doWithTx() throws Exception {

                }
            });

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }
}
