package br.com.satyacode.satyapass.log.view;

import br.com.satyacode.satyapass.log.model.ModalidadeEnum;
import br.com.satyacode.satyapass.log.service.LogService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.math.BigDecimal;

public class DeletarLogAcaoAgendada implements ScheduledAction {
    @Override
    public void onTime(ScheduledActionContext schedule) {
        schedule.info("INICIO DA ACAO AGENDADA DE DELECAO DOS LOGS \n");
        try{
            boolean isTransacaoAutomatica = true;
            LogService logService = new LogService();
            logService.gerenciarExclusaodaRotinaDeLogs(isTransacaoAutomatica);
        }catch (Exception e){
            schedule.log("Erro: "+ ExceptionUtils.getStackTrace(e));
            e.getStackTrace();
        }
        schedule.info("FIM DA ACAO AGENDADA DE DELECAO DOS LOGS \n");
    }
}
