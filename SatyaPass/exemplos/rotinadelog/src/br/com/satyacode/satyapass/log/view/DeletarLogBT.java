package br.com.satyacode.satyapass.log.view;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.satyacode.satyapass.log.service.LogService;

public class DeletarLogBT implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        System.out.printf("Iniciar Deleção dos logs");
        LogService logService = new LogService();
        logService.gerenciarExclusaodaRotinaDeLogs(true);
        contexto.setMensagemRetorno("Rotina executada com sucesso");
        System.out.printf("Fim Deleção dos logs");
    }
}
