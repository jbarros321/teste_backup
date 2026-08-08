package br.com.satyacode.satyapass.integracaovalecard.rotinas.depara.service;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.satyacode.satyapass.integracaovalecard.rotinas.depara.repository.DeParaRepository;

import java.math.BigDecimal;
import java.util.Collection;

public class DeParaService {

    private DeParaRepository deParaRepository;

    public DeParaService(){
        this.deParaRepository = new DeParaRepository();
    }

    public void gerenciarDePara(BigDecimal nroUnico) throws Exception {

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        FinderWrapper finder = new FinderWrapper("AD_IMPITE", "this.NROUNICO = ? AND NOT EXISTS (SELECT 1 FROM AD_IMPDP WHERE PRODUTO = this.PRODUTO)", new Object[]{nroUnico});
        Collection<DynamicVO> itensVO = dwfFacade.findByDynamicFinderAsVO(finder);
        if (!itensVO.isEmpty()){
            for (DynamicVO itemVO: itensVO){
                this.deParaRepository.incluirProduto(itemVO.asString("PRODUTO").trim());
            }
        }

    }
}
