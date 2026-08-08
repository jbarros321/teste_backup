package br.com.satyacode.satyapass.integracaovalecard.rotinas.cabecalho.repository;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;

public class CabecalhoRepository {

    public void atualizarStatusDoCabecalho(BigDecimal nroUnico, String status) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        PersistentLocalEntity finEntity = dwfFacade.findEntityByPrimaryKey ("AD_IMPCAB", new Object[]{nroUnico});
        DynamicVO finVO = (DynamicVO) finEntity.getValueObject();
        finVO. setProperty("STATUS", status);
        finEntity.setValueObject((EntityVO) finVO);
    }

    public void atualizarStatusDoCabecalhoTM(BigDecimal nroUnico, String status) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            hnd.setCanTimeout(false);
            hnd.execWithTX(new JapeSession.TXBlock() {
                public void doWithTx() throws Exception {
                    atualizarStatusDoCabecalho(nroUnico, status);
                }
            });
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }
}
