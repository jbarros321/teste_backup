package br.com.satyapass.abastecimento.abs_avulso.view;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.satyapass.abastecimento.abs_avulso.service.AbastecimentoService;

import java.math.BigDecimal;

public class GerarRequisicaoBT implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        for (Registro registro : contexto.getLinhas()){
            BigDecimal codAbastecimento = (BigDecimal) registro.getCampo("CODABAST");
            AbastecimentoService abastecimentoService = new AbastecimentoService();
            abastecimentoService.gerenciarAbastecimento(codAbastecimento);
            contexto.setMensagemRetorno("Rotina executada com sucesso!");

        }
    }
}
