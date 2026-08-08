package br.com.satyacode.logistica.solicitacao_transporte.view;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.satyacode.logistica.utils.factory.LogFactory;
import br.com.satyacode.satyapass.log.model.ModalidadeEnum;
import br.com.satyacode.satyapass.log.model.StatusExecucaoEnum;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class CancelarSolicitacaoBT  implements AcaoRotinaJava {

    private static EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        System.out.println("Inicio - Cancelar Solicitação - Daniel");

        try{
            LogFactory.incluirLogPai(new BigDecimal(4), ModalidadeEnum.BOTAO_ACAO, StatusExecucaoEnum.EM_ANDAMENTO, BigDecimal.ZERO, true);
            LogFactory.incluirItem( "INICIO Cancelar da Solicitação",  "CancelarSolicitacaoBT", StatusItemEnum.OK, true);

            String motivoDoCancelamento = (String) contexto.getParam("P_MOTIVO");

            LogFactory.incluirItem( "Parametro:: " + motivoDoCancelamento,  "CancelarSolicitacaoBT", StatusItemEnum.OK, true);

            for (Registro registro: contexto.getLinhas()){
                BigDecimal nroUnico = (BigDecimal) registro.getCampo("NROUNICO");
                DynamicVO logisticaVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("AD_STLCAB", new Object[]{nroUnico});

                if(logisticaVO.asString("STATUS").equals("CA")){
                    throw new Exception("A solicitação já foi cancelada, não é possivel cancelar novamente.");
                }

                if(logisticaVO.asString("STATUS").equals("C")){
                    throw new Exception("Solicitação já foi finalizada, não é possivel cancelar.");
                }

                if(logisticaVO.asString("STATUS").equals("S")){
                    throw new Exception("Como a solicitação não foi processada, basta excluir a solicitação.");
                }

                atualizarSolicitacao(nroUnico,  motivoDoCancelamento);
                contexto.setMensagemRetorno("Cancelado com sucesso!");
            }

        }finally {
            LogFactory.finalizarLog();
        }
        System.out.println("Fim - Finalzar Solicitação - Daniel");
    }

    private void atualizarSolicitacao(BigDecimal nroUnico, String motivoCancelamento) throws Exception {
        PersistentLocalEntity finEntity = dwfFacade.findEntityByPrimaryKey ("AD_STLCAB", new Object[]{nroUnico});
        DynamicVO logisticaVO = (DynamicVO) finEntity.getValueObject();
        logisticaVO. setProperty("DTCANCEL", TimeUtils.getNow());
        logisticaVO. setProperty("MOTIVOCANCEL", motivoCancelamento);
        logisticaVO. setProperty("STATUS", "CA");
        finEntity.setValueObject((EntityVO) logisticaVO);
    }

}
