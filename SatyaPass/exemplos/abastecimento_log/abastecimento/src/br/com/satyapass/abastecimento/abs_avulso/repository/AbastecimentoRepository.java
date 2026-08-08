package br.com.satyapass.abastecimento.abs_avulso.repository;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;

public class AbastecimentoRepository {

    public void atualizarNunotaDaRequisicao(BigDecimal codAbastecimento, BigDecimal nunota) throws Exception {
        try{
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            PersistentLocalEntity finEntity = dwfFacade.findEntityByPrimaryKey ("AD_ABTAVU", new Object[]{codAbastecimento});
            DynamicVO abastecimentoVO = (DynamicVO) finEntity.getValueObject();
            abastecimentoVO.setProperty("NUNOTA", nunota);
            finEntity.setValueObject((EntityVO) abastecimentoVO);
        }catch (Exception e){
            throw new Exception("Erro ao atualizar o nunota do abastecimento: " + codAbastecimento + " - Erro: " + e.getMessage());
        }
    }
}
