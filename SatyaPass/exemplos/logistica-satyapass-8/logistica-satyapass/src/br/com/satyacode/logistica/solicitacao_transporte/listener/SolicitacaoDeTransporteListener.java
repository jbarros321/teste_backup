package br.com.satyacode.logistica.solicitacao_transporte.listener;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

public class SolicitacaoDeTransporteListener implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO solicitacaoVO = (DynamicVO) event.getVo();
        solicitacaoVO.setProperty("DTCRIACAO", TimeUtils.getNow());
        if (StringUtils.isEmpty(solicitacaoVO.asString("IDENTIFICADOR")))
            solicitacaoVO.setProperty("IDENTIFICADOR", "SK-"+solicitacaoVO.asBigDecimal("NROUNICO"));
        if (StringUtils.isEmpty(solicitacaoVO.asString("PRIORIDADE")))
            solicitacaoVO.setProperty("PRIORIDADE", "B");
        solicitacaoVO.setProperty("STATUS", "S");
    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO solicitacaoVO = (DynamicVO) event.getVo();
        if (StringUtils.isEmpty(solicitacaoVO.asString("IDENTIFICADOR")))
            solicitacaoVO.setProperty("IDENTIFICADOR", "SK-"+solicitacaoVO.asBigDecimal("NROUNICO"));

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
