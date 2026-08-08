package br.com.satyacode.satyapass.integracaovalecard.rotinas.item.repository;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.satyacode.satyapass.integracaovalecard.shared.model.ItemSankhya;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.math.BigDecimal;

public class ItemRespository {

    public void incluirItens(ItemSankhya itemSankhya) throws Exception {
        try{
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            DynamicVO finVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("AD_IMPITE");
            finVO.setProperty("NROUNICO", itemSankhya.getNroUnico());
            finVO.setProperty("DATA", itemSankhya.getData());
            finVO. setProperty("NROCARTAO", itemSankhya.getNroCartao());
            finVO. setProperty("MOTORISTA", itemSankhya.getMotorista());
            finVO. setProperty("PLACA", itemSankhya.getPlaca());
            finVO. setProperty("HORIMETRO", itemSankhya.getHorimetro());
            finVO. setProperty("DISTANCIA", itemSankhya.getDistancia());
            finVO. setProperty("PRODUTO", itemSankhya.getProduto());
            finVO. setProperty("QUANTIDADE", itemSankhya.getQuantidade());
            dwfFacade.createEntity("AD_IMPITE", (EntityVO) finVO);
        }catch (Exception e){
            throw new Exception("Erro ao incluir item da planilha: " + ExceptionUtils.getStackTrace(e));
        }
    }

    public void atualizarCamposDeParaNosItens(BigDecimal nroUnico, BigDecimal sequencia,  String campo, Object valor) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        PersistentLocalEntity finEntity = dwfFacade.findEntityByPrimaryKey ("AD_IMPITE", new Object[]{nroUnico,sequencia });
        DynamicVO finVO = (DynamicVO) finEntity.getValueObject();
        finVO. setProperty(campo,  valor);
        finEntity.setValueObject((EntityVO) finVO);
    }

    public void deletarItens(BigDecimal nroUnico) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper configDao = JapeFactory.dao("AD_IMPITE");
            configDao.deleteByCriteria("NROUNICO = ? ", nroUnico);

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        }finally {
            JapeSession.close(hnd);
        }

    }
}
