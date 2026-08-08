package br.com.satyapass.abastecimento.abs_avulso.repository;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;

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
