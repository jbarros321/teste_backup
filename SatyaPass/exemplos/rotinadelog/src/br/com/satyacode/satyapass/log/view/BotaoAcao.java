package br.com.satyacode.satyapass.log.view;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.satyacode.satyapass.log.model.ModalidadeEnum;
import br.com.satyacode.satyapass.log.model.StatusExecucaoEnum;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import br.com.satyacode.satyapass.log.service.LogService;

import java.math.BigDecimal;

public class BotaoAcao implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        LogService logService = new LogService();
        boolean isTransacaoAutomatica = true;
        BigDecimal codLog = logService.incluirLog(BigDecimal.ONE, ModalidadeEnum.BOTAO_ACAO, StatusExecucaoEnum.EM_ANDAMENTO, BigDecimal.ZERO, isTransacaoAutomatica);
        logService.incluirItem(codLog,  "Testando nova modalidade", "Teste 123", "TESTE", StatusItemEnum.OK, isTransacaoAutomatica);
        logService.incluirItem(codLog,  "Testando nova modalidade 2", "Teste 123", "TESTE", StatusItemEnum.ERRO,isTransacaoAutomatica);
        logService.atualizarStatusLogPai(codLog, true);
        contexto.setMensagemRetorno("Rotina executada com sucesso!");
    }

}
