package br.com.satyacode.satyapass.simulacao.integracao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.satyacode.satyapass.log.model.ModalidadeEnum;
import br.com.satyacode.satyapass.log.model.StatusExecucaoEnum;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import br.com.satyacode.satyapass.log.service.LogService;
import br.com.satyacode.satyapass.simulacao.integracao.utils.factory.LogFactory;

import java.math.BigDecimal;

public class BotaoDeAcao implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        System.out.println("ROTINA DE SIMULAÇÃO DO LOG - INTEGRACAO");

        try{

            LogFactory.incluirLogPai(new BigDecimal(3), ModalidadeEnum.BOTAO_ACAO, StatusExecucaoEnum.EM_ANDAMENTO, BigDecimal.ZERO, true);

            LogFactory.incluirItem( "Testando Rotina De Simulacao",  "INFO", StatusItemEnum.OK, true);

            Controller controller = new Controller();
            controller.gerenciarIntegracao();

        }finally {
            LogFactory.finalizarLog();
        }
    }
}
