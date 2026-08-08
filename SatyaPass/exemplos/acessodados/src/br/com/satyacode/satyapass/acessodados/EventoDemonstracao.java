package br.com.satyacode.satyapass.acessodados;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

public class EventoDemonstracao implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO eventoVO = (DynamicVO)event.getVo();
        eventoVO.setProperty("DTCRIACAO", TimeUtils.getNow());
        eventoVO.setProperty("DTALTER", TimeUtils.getNow());

        if(StringUtils.isEmpty(eventoVO.asString("NOME"))){
            throw new Exception("O campo nome não pode ser vazio na inserção!");
        }

    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO eventoVO = (DynamicVO)event.getVo();
        eventoVO.setProperty("DTALTER", TimeUtils.getNow());

        if(StringUtils.isEmpty(eventoVO.asBigDecimal("VALOR"))){
            throw new Exception("O valor não pode ser vazio!");
        }

        DynamicVO eventoOldVO = (DynamicVO)event.getOldVO();
        throw new Exception("O conteudo de valor anteriormente era: " + eventoOldVO.asBigDecimal("VALOR") +
                " - Valor atual: " + eventoVO.asBigDecimal("VALOR") );

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

}
