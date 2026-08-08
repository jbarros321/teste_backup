package br.com.satyacode.satyapass.integracaovalecard.rotinas.item.listener;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

public class ItemListener implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO itemVO  = (DynamicVO) event.getVo();
        itemVO.setProperty("DTALTER", TimeUtils.getNow());
    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO itemVO  = (DynamicVO) event.getVo();
        ModifingFields modifingFields = event.getModifingFields();
        if (!modifingFields.isModifing("NUNOTA"))
            bloquearAlteracaoOuDeletacao(itemVO);
    }

    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {
        DynamicVO itemVO  = (DynamicVO) event.getVo();
        bloquearAlteracaoOuDeletacao(itemVO);
    }

    private void bloquearAlteracaoOuDeletacao(DynamicVO itemVO) throws Exception {
        if(StringUtils.isNotEmpty(itemVO.asBigDecimal("NUNOTA"))){
            throw new Exception("Nota já foi gerada, não é possivel editar ou excluir");
        }
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
}
