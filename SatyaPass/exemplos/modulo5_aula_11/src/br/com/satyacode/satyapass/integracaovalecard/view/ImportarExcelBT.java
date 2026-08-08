package br.com.satyacode.satyapass.integracaovalecard.view;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.satyacode.satyapass.integracaovalecard.service.ImportacaoService;

import java.math.BigDecimal;

public class ImportarExcelBT implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {

        if (contexto.getLinhas().length == 0){
            throw new Exception("Selecione uma linha para poder realizar a importação.");
        }

        for (Registro registro: contexto.getLinhas()){
            BigDecimal nroUnico = (BigDecimal) registro.getCampo("NROUNICO");
            ImportacaoService importacaoService = new ImportacaoService();
            importacaoService.gerenciarImportacao(nroUnico);
        }

    }
}
