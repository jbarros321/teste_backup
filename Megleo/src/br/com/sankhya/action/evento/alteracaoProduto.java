package br.com.sankhya.action.evento;

import br.com.sankhya.action.funcoes.AlteracaoSKU;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

public class alteracaoProduto
implements EventoProgramavelJava {
    private String ncm;
    private String embalagem;
    private String sku;
    private String quantidade;

    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO prodVO = (DynamicVO)persistenceEvent.getVo();
        if (prodVO.asString("NCM") != null && prodVO.asString("DESCRPROD") != null) {
            AlteracaoSKU alteracaoSKU = new AlteracaoSKU();
            alteracaoSKU.enviaAlteracaoAPI(prodVO);
        }
    }

    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        ModifingFields modifingFields = persistenceEvent.getModifingFields();
        DynamicVO prodVO = (DynamicVO)persistenceEvent.getVo();
        if (modifingFields.isModifingAny("DESCRPROD,NCM") && prodVO.asString("NCM") != null) {
            AlteracaoSKU alteracaoSKU = new AlteracaoSKU();
            alteracaoSKU.enviaAlteracaoAPI(prodVO);
        }
    }

    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void beforeCommit(TransactionContext transactionContext) throws Exception {
    }
}
