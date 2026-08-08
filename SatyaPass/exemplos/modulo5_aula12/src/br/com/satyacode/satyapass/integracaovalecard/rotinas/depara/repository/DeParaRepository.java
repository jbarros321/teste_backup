package br.com.satyacode.satyapass.integracaovalecard.rotinas.depara.repository;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class DeParaRepository {

    public void incluirProduto(String nomeProduto) throws Exception {
        try{
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            DynamicVO finVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("AD_IMPDP");
            finVO.setProperty("PRODUTO", nomeProduto);
            dwfFacade.createEntity("AD_IMPDP", (EntityVO) finVO);
        }catch (Exception e){
            throw new Exception("Erro ao incluir o produto no de/para: " + ExceptionUtils.getStackTrace(e));
        }
    }
}
