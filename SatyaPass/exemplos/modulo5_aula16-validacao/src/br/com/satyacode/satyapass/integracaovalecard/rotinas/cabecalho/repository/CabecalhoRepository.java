package br.com.satyacode.satyapass.integracaovalecard.rotinas.cabecalho.repository;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
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
}
