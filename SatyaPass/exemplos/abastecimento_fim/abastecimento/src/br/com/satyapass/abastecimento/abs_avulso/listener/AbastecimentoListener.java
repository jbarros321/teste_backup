package br.com.satyapass.abastecimento.abs_avulso.listener;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

public class AbastecimentoListener implements EventoProgramavelJava {

    private EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO abastecimentoVO = (DynamicVO)event.getVo();
        abastecimentoVO.setProperty("DTCAD", TimeUtils.getNow());
        abastecimentoVO.setProperty("DTALTER", TimeUtils.getNow());
        abastecimentoVO.setProperty("CODUSUCAD", AuthenticationInfo.getCurrent().getUserID());
        abastecimentoVO.setProperty("CODUSUALTER", AuthenticationInfo.getCurrent().getUserID());
    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO abastecimentoVO = (DynamicVO)event.getVo();
        abastecimentoVO.setProperty("DTALTER", TimeUtils.getNow());
        abastecimentoVO.setProperty("CODUSUALTER", AuthenticationInfo.getCurrent().getUserID());
        if (StringUtils.isNotEmpty(abastecimentoVO.asBigDecimal("NUNOTA"))){
            validarCamposAposAGeracaoNoPortal(event);
        }

        if (abastecimentoVO.asBigDecimalOrZero("CODPROD").compareTo(BigDecimal.ZERO) != 0){
            DynamicVO produtoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("Produto", new Object[]{abastecimentoVO.asBigDecimal("CODPROD")});
            if (produtoVO.asString("USALOCAL").equals("N")){
                abastecimentoVO.setProperty("CODLOCAL", BigDecimal.ZERO);
            }
        }
    }

    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {

    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {

    }

    private void validarCamposAposAGeracaoNoPortal(PersistenceEvent event) throws Exception {
        ModifingFields modifingFields = event.getModifingFields();
        if (!(modifingFields.isModifing("NUNOTA")
                || modifingFields.isModifing("CODUSUALTER")
                || modifingFields.isModifing("DTALTER"))){
            throw new Exception("Não é possivel alterar informações do abastecimento, pois já foi sincronizado com os portais");
        }
    }
}
