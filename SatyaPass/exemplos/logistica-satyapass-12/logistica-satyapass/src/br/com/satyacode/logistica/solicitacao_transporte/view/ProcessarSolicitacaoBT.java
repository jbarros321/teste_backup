package br.com.satyacode.logistica.solicitacao_transporte.view;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.satyacode.logistica.historico.model.Historico;
import br.com.satyacode.logistica.historico.repository.HistoricoRepository;
import br.com.satyacode.logistica.historico.service.HistoricoService;
import br.com.satyacode.logistica.solicitacao_transporte.model.SolicitacaoParametro;
import br.com.satyacode.logistica.solicitacao_transporte.model.SolicitacaoTransporte;
import br.com.satyacode.logistica.solicitacao_transporte.repository.SolicitacaoTransporteRepository;
import br.com.satyacode.logistica.solicitacao_transporte.service.SolicitacaoDeTransporteService;
import br.com.satyacode.logistica.usuario.service.UsuarioService;
import br.com.satyacode.logistica.utils.factory.LogFactory;
import br.com.satyacode.satyapass.log.model.ModalidadeEnum;
import br.com.satyacode.satyapass.log.model.StatusExecucaoEnum;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

public class ProcessarSolicitacaoBT implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        System.out.println("Inicio - Processar Solicitação - Daniel");
        try {
            LogFactory.incluirLogPai(new BigDecimal(4), ModalidadeEnum.BOTAO_ACAO, StatusExecucaoEnum.EM_ANDAMENTO, BigDecimal.ZERO, true);
            LogFactory.incluirItem("INICIO Processamento da Solicitação", "INFO", StatusItemEnum.OK, true);

            BigDecimal codMotorista = BigDecimal.ZERO;
            BigDecimal codVeiculo = BigDecimal.ZERO;
            if (StringUtils.isNotEmpty(contexto.getParam("P_CODPARCMOT"))) {
                codMotorista = BigDecimalUtil.valueOf((String) contexto.getParam("P_CODPARCMOT"));
            }
            if (StringUtils.isNotEmpty(contexto.getParam("P_CODVEICULO"))) {
                codVeiculo = BigDecimalUtil.valueOf((String) contexto.getParam("P_CODVEICULO"));
            }

            LogFactory.incluirItem("Parametros: ", "Cód. Motorista: " + codMotorista + " - Veiculo:" + codMotorista, "INFO", StatusItemEnum.OK, true);

            if (contexto.getLinhas().length == 0) {
                contexto.setMensagemRetorno("Não foi selecionada a linha. Selecione para continuar!");
                return;
            }

            for (Registro registro : contexto.getLinhas()) {
                BigDecimal nroUnico = (BigDecimal) registro.getCampo("NROUNICO");
                SolicitacaoParametro solicitacaoParametro = new SolicitacaoParametro(
                        nroUnico, codMotorista, codVeiculo
                );
                new SolicitacaoDeTransporteService().processarSolicitacao(solicitacaoParametro);

            }

        } finally {
            LogFactory.finalizarLog();
        }
        System.out.println("Fim - Processar Solicitação - Daniel");
    }

}
