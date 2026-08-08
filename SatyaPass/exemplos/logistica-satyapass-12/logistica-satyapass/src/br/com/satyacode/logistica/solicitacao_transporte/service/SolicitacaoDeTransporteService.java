package br.com.satyacode.logistica.solicitacao_transporte.service;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.satyacode.logistica.historico.model.Historico;
import br.com.satyacode.logistica.historico.service.HistoricoService;
import br.com.satyacode.logistica.solicitacao_transporte.model.SolicitacaoParametro;
import br.com.satyacode.logistica.solicitacao_transporte.model.SolicitacaoTransporte;
import br.com.satyacode.logistica.solicitacao_transporte.repository.SolicitacaoTransporteRepository;
import br.com.satyacode.logistica.usuario.service.UsuarioService;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;

public class SolicitacaoDeTransporteService {

    private static EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
    private SolicitacaoTransporteRepository solicitacaoTransporteRepository;

    public SolicitacaoDeTransporteService(){
        this.solicitacaoTransporteRepository = new SolicitacaoTransporteRepository();
    }

    public void processarSolicitacao(SolicitacaoParametro solicitacaoParametro) throws Exception {
        DynamicVO logisticaVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("AD_STLCAB", new Object[]{solicitacaoParametro.getNroUnico()});
        if (!(logisticaVO.asString("STATUS").equals("S") || logisticaVO.asString("STATUS").equals("EA"))){
            throw new Exception("Não é possivel processar a solcitação, verifique o status atual");
        }

        if (StringUtils.isNotEmpty(logisticaVO.asBigDecimal("CODPARCMOT"))) {
            if(logisticaVO.asBigDecimal("CODPARCMOT").compareTo(solicitacaoParametro.getCodMotorista()) > 0){

                System.out.println("Alteração de Motorista");

            }
        }
        SolicitacaoTransporte solicitacaoTransporte = new SolicitacaoTransporte();
        solicitacaoTransporte.setNroUnico(solicitacaoParametro.getNroUnico());
        solicitacaoTransporte.setCodMotorista(solicitacaoParametro.getCodMotorista());
        solicitacaoTransporte.setCodVeiculo(solicitacaoParametro.getCodVeiculo());
        solicitacaoTransporte.setStatus("EA");
        SolicitacaoTransporteRepository solicitacaoTransporteRepository = new SolicitacaoTransporteRepository();
        solicitacaoTransporteRepository.atualizarSolicitacaoTransporte(solicitacaoTransporte);

        String ocorrencia = new UsuarioService().getUsuarioLogado() + " - Processar Solicitação";
        new HistoricoService().incluirHistorico(new Historico(solicitacaoParametro.getNroUnico(),ocorrencia));
    }

}
