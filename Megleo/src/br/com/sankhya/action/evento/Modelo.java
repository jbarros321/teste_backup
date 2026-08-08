package br.com.sankhya.action.evento;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

public class Modelo
implements EventoProgramavelJava {
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO)persistenceEvent.getVo();
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
