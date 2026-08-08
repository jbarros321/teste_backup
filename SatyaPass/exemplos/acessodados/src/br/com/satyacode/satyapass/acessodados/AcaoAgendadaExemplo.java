package br.com.satyacode.satyapass.acessodados;

import br.com.sankhya.modelcore.MGEModelException;
import br.com.satyacode.satyapass.acessodados.crud.JapewrapperExemplo;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

public class AcaoAgendadaExemplo implements ScheduledAction {
    @Override
    public void onTime(ScheduledActionContext schedule) {
        schedule.info("INICIO TESTANDO LOG");
        JapewrapperExemplo japewrapperExemplo = new JapewrapperExemplo();
        try {
            japewrapperExemplo.inserirDados("ACAO AGENDADA");
        } catch (MGEModelException e) {
            schedule.info("ERRP: " + e.getMessage());
            throw new RuntimeException(e);
        }
        schedule.info("FIM TESTANDO LOG");

    }
}
