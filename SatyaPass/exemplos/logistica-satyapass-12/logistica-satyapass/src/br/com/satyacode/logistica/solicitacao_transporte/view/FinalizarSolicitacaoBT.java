package br.com.satyacode.logistica.solicitacao_transporte.view;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.PersistenceException;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.satyacode.logistica.historico.model.Historico;
import br.com.satyacode.logistica.historico.repository.HistoricoRepository;
import br.com.satyacode.logistica.historico.service.HistoricoService;
import br.com.satyacode.logistica.usuario.service.UsuarioService;
import br.com.satyacode.logistica.utils.factory.LogFactory;
import br.com.satyacode.satyapass.log.model.ModalidadeEnum;
import br.com.satyacode.satyapass.log.model.StatusExecucaoEnum;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class FinalizarSolicitacaoBT  implements AcaoRotinaJava {

    private static EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        System.out.println("Inicio - Finalzar Solicitação - Daniel");

        try{
            LogFactory.incluirLogPai(new BigDecimal(4), ModalidadeEnum.BOTAO_ACAO, StatusExecucaoEnum.EM_ANDAMENTO, BigDecimal.ZERO, true);
            LogFactory.incluirItem( "INICIO Finalizacao da Solicitação",  "FinalizarSolicitacaoBT", StatusItemEnum.OK, true);

            Timestamp dataFinalizacao = (Timestamp)contexto.getParam("P_DTFINALIZACAO");
            if (StringUtils.isEmpty(dataFinalizacao)){
                dataFinalizacao = TimeUtils.getNow();
            }

            LogFactory.incluirItem( "Parametro Data Finalização: " + dataFinalizacao,  "FinalizarSolicitacaoBT", StatusItemEnum.OK, true);

            for (Registro registro: contexto.getLinhas()){
                BigDecimal nroUnico = (BigDecimal) registro.getCampo("NROUNICO");
                DynamicVO logisticaVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("AD_STLCAB", new Object[]{nroUnico});
                if(!logisticaVO.asString("STATUS").equals("EA")){
                    throw new Exception("Não é possivel processar, pois o status é diferente de 'Em Andamento'.");
                }
                atualizarSolicitacao(nroUnico, dataFinalizacao);

                String ocorrencia = new UsuarioService().getUsuarioLogado() + " - Finalizar Solicitação";
                new HistoricoService().incluirHistorico(new Historico(nroUnico,ocorrencia));

                contexto.setMensagemRetorno("Finalizado com sucesso!");
            }

        }finally {
            LogFactory.finalizarLog();
        }
        System.out.println("Fim - Finalzar Solicitação - Daniel");
    }

    private void atualizarSolicitacao(BigDecimal nroUnico, Timestamp dataFinalizacao) throws Exception {
        PersistentLocalEntity finEntity = dwfFacade.findEntityByPrimaryKey ("AD_STLCAB", new Object[]{nroUnico});
        DynamicVO logisticaVO = (DynamicVO) finEntity.getValueObject();
        logisticaVO. setProperty("DTENTREGA", dataFinalizacao);
        logisticaVO. setProperty("STATUS", "C");
        finEntity.setValueObject((EntityVO) logisticaVO);
    }

}
