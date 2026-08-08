package br.com.satyacode.logistica.solicitacao_transporte.repository;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.PersistenceException;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.satyacode.logistica.solicitacao_transporte.model.SolicitacaoTransporte;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;

public class SolicitacaoTransporteRepository {

    public void atualizarSolicitacaoTransporte(SolicitacaoTransporte solicitacao) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        PersistentLocalEntity finEntity = dwfFacade.findEntityByPrimaryKey ("AD_STLCAB", new Object[]{solicitacao.getNroUnico()});
        DynamicVO logisticaVO = (DynamicVO) finEntity.getValueObject();
        if (solicitacao.getCodMotorista().compareTo(BigDecimal.ZERO) != 0)
            logisticaVO. setProperty("CODPARCMOT", solicitacao.getCodMotorista());
        if (solicitacao.getCodVeiculo().compareTo(BigDecimal.ZERO) != 0)
            logisticaVO.setProperty("CODVEICULO", solicitacao.getCodVeiculo());
        if (StringUtils.isNotEmpty(solicitacao.getStatus()))
            logisticaVO.setProperty("STATUS", solicitacao.getStatus());
        finEntity.setValueObject((EntityVO) logisticaVO);
    }
}
