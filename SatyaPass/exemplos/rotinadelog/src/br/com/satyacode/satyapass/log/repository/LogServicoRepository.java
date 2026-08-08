package br.com.satyacode.satyapass.log.repository;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.Collection;

public class LogServicoRepository {

    private static String nomeTabela = "AD_STPSER";
    public boolean isServicoEstaAtivo(BigDecimal codServico) throws Exception {
        EntityFacade dwffacade = EntityFacadeFactory.getDWFFacade();
        FinderWrapper finder = new FinderWrapper(nomeTabela, "this.CODSER = ? AND this.ATIVO = 'S'", codServico);
        Collection<DynamicVO> servicosVO = dwffacade.findByDynamicFinderAsVO(finder);
        if (servicosVO.isEmpty())
            return false;
        else
            return true;
    }
}
