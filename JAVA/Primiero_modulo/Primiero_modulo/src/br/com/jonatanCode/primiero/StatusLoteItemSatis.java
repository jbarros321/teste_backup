package br.com.jonatanCode.primiero;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.mgecomercial.model.facades.RecalculoCustosSP;

import java.math.BigDecimal;

public class StatusLoteItemSatis implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        System.out.println("Inicio de atribuir Status no Lote.");

        //Opção selecionada do Parametro passado
        String status = (String)contextoAcao.getParam("P_STATUS");
        System.out.println("Status: " + status);

        //ler a linha selecioda pelo  Usuario
       for (int i = 0; i < contextoAcao.getLinhas().length; i ++ ){

           System.out.println("Indice :" + i);
           Registro  resgistro = contextoAcao.getLinhas()[i];
           System.out.println("Lote selecionado: " + resgistro.getCampo("CONTROLE") );

           //Executa a atualização do Status
           QueryExecutor query = contextoAcao.getQuery();
           query.setParam("CONTROLE", (String) resgistro.getCampo("CONTROLE"));
           query.setParam("CODPROD", (BigDecimal) resgistro.getCampo("CODPROD"));
           query.setParam("CODLOCAL", (BigDecimal) resgistro.getCampo("CODLOCAL"));
           query.setParam("CODEMP", (BigDecimal) resgistro.getCampo("CODEMP"));
           query.update("UPDATE TGFEST SET STATUSLOTE =" +status+ " WHERE CODPROD = {CODPROD} AND CONTROLE = {CONTROLE} AND CODLOCAL = {CODLOCAL} AND CODEMP = {CODEMP} ");
           query.close();
       }

        System.out.println("Finalizado atribuir Status no Lote.");
        contextoAcao.setMensagemRetorno("Atualização de Lotes executado com Sucesso!");

    }
}
