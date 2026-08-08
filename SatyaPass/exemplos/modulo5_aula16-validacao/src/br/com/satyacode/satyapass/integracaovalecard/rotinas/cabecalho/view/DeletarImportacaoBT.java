package br.com.satyacode.satyapass.integracaovalecard.rotinas.cabecalho.view;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.satyacode.satyapass.integracaovalecard.rotinas.cabecalho.service.CabecalhoService;

import java.math.BigDecimal;

public class DeletarImportacaoBT implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {

        if (contexto.getLinhas().length == 0){
            throw new Exception("Selecione uma linha para poder realizar a deleção.");
        }

        for (Registro registro: contexto.getLinhas()){
            BigDecimal nroUnico = (BigDecimal) registro.getCampo("NROUNICO");
            CabecalhoService cabecalhoService = new CabecalhoService();
            cabecalhoService.gerenciarDelecao(nroUnico);
        }

        contexto.setMensagemRetorno("Rotina executada com sucesso!");

    }
}
