package br.com.satyacode.satyapass.integracaovalecard.rotinas.cabecalho.view;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.satyacode.satyapass.integracaovalecard.rotinas.cabecalho.service.CabecalhoService;

import java.math.BigDecimal;

public class DeParaBT implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {

        if (contexto.getLinhas().length == 0){
            throw new Exception("Selecione uma linha para poder realizar o de/para.");
        }

        for (Registro registro: contexto.getLinhas()){
            BigDecimal nroUnico = (BigDecimal) registro.getCampo("NROUNICO");
            CabecalhoService cabecalhoService = new CabecalhoService();
            cabecalhoService.gerenciarDePara(nroUnico);
        }

    }
}
