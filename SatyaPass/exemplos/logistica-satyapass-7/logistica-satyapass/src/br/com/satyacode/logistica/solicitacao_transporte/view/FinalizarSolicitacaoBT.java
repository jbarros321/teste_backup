package br.com.satyacode.logistica.solicitacao_transporte.view;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.satyacode.logistica.utils.factory.LogFactory;
import br.com.satyacode.satyapass.log.model.ModalidadeEnum;
import br.com.satyacode.satyapass.log.model.StatusExecucaoEnum;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;

import java.math.BigDecimal;

public class FinalizarSolicitacaoBT  implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        System.out.println("Inicio - Finalzar Solicitação - Daniel");
        try{
            LogFactory.incluirLogPai(new BigDecimal(4), ModalidadeEnum.BOTAO_ACAO, StatusExecucaoEnum.EM_ANDAMENTO, BigDecimal.ZERO, true);
            LogFactory.incluirItem( "INICIO Processamento da Solicitação",  "INFO", StatusItemEnum.OK, true);

            for (Registro registro: contexto.getLinhas()){
                BigDecimal nroUnico = (BigDecimal) registro.getCampo("NROUNICO");

            }

        }finally {
            LogFactory.finalizarLog();
        }
        System.out.println("Fim - Finalzar Solicitação - Daniel");
    }

}
